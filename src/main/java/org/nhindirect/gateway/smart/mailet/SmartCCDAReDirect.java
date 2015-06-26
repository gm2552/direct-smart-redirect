package org.nhindirect.gateway.smart.mailet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mailet.Mail;

import org.nhindirect.common.tx.TxUtil;
import org.nhindirect.common.tx.model.Tx;
import org.nhindirect.common.tx.model.TxMessageType;
import org.nhindirect.gateway.smart.CCDAContent;
import org.nhindirect.gateway.smtp.NotificationProducer;
import org.nhindirect.gateway.smtp.NotificationSettings;
import org.nhindirect.gateway.smtp.ReliableDispatchedNotificationProducer;
import org.nhindirect.gateway.smtp.dsn.DSNCreator;
import org.nhindirect.gateway.smtp.dsn.provider.FailedDeliveryDSNCreatorProvider;
import org.nhindirect.gateway.smtp.james.mailet.AbstractNotificationAwareMailet;
import org.nhindirect.stagent.NHINDAddress;
import org.nhindirect.stagent.NHINDAddressCollection;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.mail.notifications.NotificationMessage;

import com.google.inject.Provider;
/**
 * The SMART C-CDA reDirect mailet.  This mailet inspects each message for C-CDA content and transports each potential C-CDA to a configured
 * REST URI via an HTTP POST operation.  The URI is configured using the <b>CCDAPostUrl<b> mailet configuration parameter.
 * <p>
 * The REST URI supports template parameters based on the sender and receiver of the message.  The URI uses the templates {to} and {from}
 * to indicate the respective addresses.  For example, the URI template
 * <br>
 * <pre>
 * http://my-server.com/incoming/ccda/for-user/{to}
 * </pre>
 * <br>
 * would POST each found C-CDA to the URI based on the recipient's email address.
 * <p>
 * This class is a mailet interpretation of the <a href="https://github.com/jmandel/ccda-reDirect">SMART reDirect service</a>
 * @author Greg Meyer
 * @since 1.0
 */
public class SmartCCDAReDirect extends AbstractNotificationAwareMailet
{
	private static final Log LOGGER = LogFactory.getFactory().getInstance(SmartCCDAReDirect.class);	
	
	String contextDefFile;
	String postURLTemplate;
	CamelContext context;
	boolean ghostAllMessages;
	boolean bounceFailedPosts;
	
	// adding to support the implementation guide for delivery notification
	protected NotificationProducer notificationProducer;
	
	/**
	 * {@inheritDoc}
	 */
	public void init() throws MessagingException
	{
		LOGGER.info("Initializing SmartCCDAReDirect mailet");
		
		super.init();
		
		postURLTemplate = this.getInitParameter("CCDAPostUrl");
		final String ghostMessageSetting = this.getInitParameter("GhostCDAMessages", "false");
		final String bounceFailuresSetting = this.getInitParameter("BounceFailedPosts", "true");
		
		ghostAllMessages = Boolean.parseBoolean(ghostMessageSetting);
		bounceFailedPosts = Boolean.parseBoolean(bounceFailuresSetting);
			
		// make sure we have POST URL configured
		if (postURLTemplate == null || postURLTemplate.isEmpty())
			throw new MessagingException("CCDAPostUrl parameter cannot be empty.");
		
		context = createCamelContext();

		try
		{
			context.start();
		}
		catch (Exception e)
		{
			throw new MessagingException("Failed to start camel context.", e);
		}
		
		notificationProducer = new ReliableDispatchedNotificationProducer(new NotificationSettings(true, "Local Direct Delivery Agent", "Your message was successfully dispatched."));

	}
	
	/**
	 * Creates the camel context used for creating producer templates.
	 * @return The camel context used for creating producer templates.
	 */
	protected CamelContext createCamelContext()
	{
		return new DefaultCamelContext();
	}
	
	/**
	 * Creates a default camel exchange object from a camel context.
	 * @param context The camel context.
	 * @return A camel exchange used for sending
	 */
	protected Exchange createCamelExchange(CamelContext context)
	{
		return new DefaultExchange(context);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void service(Mail mail) throws MessagingException 
	{	
		LOGGER.info("Servicing CCDA reDirect");
		
		final MimeMessage msg = mail.getMessage();
		
		boolean dispatchSuccessful = false;
		
		// checking to see if we need to respond with the dispatch MDN
		// in compliance with the implementation guide for delivery notification
		final boolean isReliableAndTimely = TxUtil.isReliableAndTimelyRequested(msg);
		
		// could use a split and send EIP, but for simplicity we will split the message
		// into the CCDA parts and use a URL template right here in line
		
		final Collection<BodyPart> ccdaParts = CCDAContent.getCCDAParts(msg);
		
		// get the list of recipients and sender
		final NHINDAddressCollection recips = getMailRecipients(mail);
		final NHINDAddress sender = getMailSender(mail);
		
		final Tx txToTrack = this.getTxToTrack(msg, sender, recips);
		
		final Collection<String> postToURLs = getPostToURLs(recips, sender);
		
		// send each CDA to the URL based on the template
		// convert to a XML output stream
		final ProducerTemplate template = context.createProducerTemplate();
		
		try
		{
			template.start();
		}
		catch (Exception e)
		{ 
			throw new MessagingException("Failed to start producer template.", e);
		}
		
		LOGGER.debug("# of CDAs to reDirect: " + ccdaParts.size());
		
		for (BodyPart ccdaPart : ccdaParts)
		{
			try
			{
				LOGGER.debug("Creating exchange");
				
				Exchange ex = createCamelExchange(context);
				ex.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
				ex.getIn().setHeader(Exchange.CONTENT_TYPE, "text/xml");
				ex.getIn().setBody(ccdaPart.getContent().toString());
				ex.setPattern(ExchangePattern.InOut);
				for (String postToURL : postToURLs)
				{

					LOGGER.debug("Sending to URL " + postToURL);
					
					final Exchange exchange = template.send(postToURL, ex);
					
					LOGGER.debug("Exchange returned");
					
					if (exchange.getException() == null && exchange.getOut() != null)
					{
						Integer responseCode = (Integer)exchange.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE);
						if (responseCode >= 200 && responseCode < 300)
						{
							LOGGER.info("Successfully sent CCDA to endpoint " + postToURL);
							dispatchSuccessful = true;
							
							// send the MDN dispatched messages if the notification IG was 
							// invoked by the sender
							if (isReliableAndTimely && txToTrack.getMsgType() == TxMessageType.IMF)
							{

								// send back an MDN dispatched message
								final Collection<NotificationMessage> notifications = 
										notificationProducer.produce(new Message(msg), recips.toInternetAddressCollection());
								if (notifications != null && notifications.size() > 0)
								{
									LOGGER.debug("Sending MDN \"dispatched\" messages");
									// create a message for each notification and put it on James "stack"
									for (NotificationMessage message : notifications)
									{
										try
										{
											message.saveChanges();

											
											getMailetContext().sendMail(message);
										}
										///CLOVER:OFF
										catch (Throwable t)
										{
											// don't kill the process if this fails
											LOGGER.error("Error sending MDN dispatched message.", t);
										}
										///CLOVER:ON
									}
								}
							}
						}
						else
						{
							LOGGER.error("Failed to send CCDA to endpoint " + postToURL + " with error code" + responseCode);
						}
					}
					else
					{
						LOGGER.error("Failed to send CCDA to endpoint " + postToURL + " with error: " + exchange.getException().getMessage());
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to reDirect CCDA.", e);
			}
			finally
			{
				// stop the message flow for CDA messages
				if (this.ghostAllMessages)
					mail.setState(Mail.GHOST);
			}
		}
		
		// bounce the message if configured to do so if dispatch failed and GHOST it
		if (!dispatchSuccessful && bounceFailedPosts && txToTrack != null && txToTrack.getMsgType() == TxMessageType.IMF)
		{
			this.sendDSN(txToTrack, recips, false);
			mail.setState(Mail.GHOST);
		}
	
		try
		{
			template.stop();
		}
		catch (Exception e) {};
	}
	
	/**
	 * Creates a collection of URLs to post C-CDAs to based on the configured URI template, the message sender, and message recipients.
	 * @param recips Message recipients.
	 * @param sender Message sender.
	 * @return Collection of URIs to post the C-CDA to.
	 */
	protected Collection<String> getPostToURLs(NHINDAddressCollection recips, InternetAddress sender)
	{
		final Collection<String> retVal = new ArrayList<String>();
		
		// making this too hard, but just split logic into the permutations
		if (!postURLTemplate.contains("{to}") && !postURLTemplate.contains("{from}"))
			return Arrays.asList(postURLTemplate);
		if (!(postURLTemplate.contains("{to}")) && postURLTemplate.contains("{from}"))
			return Arrays.asList(postURLTemplate.replace("{from}", sender.getAddress()));
		else
		{
			for (InternetAddress recip : recips)
			{
				String postURL = postURLTemplate.replace("{to}", recip.getAddress());
				if (postURL.contains("{from}"))
					postURL = postURL.replace("{from}", sender.getAddress());
				
				retVal.add(postURL);
			}
		}
		
		return retVal;
	}	

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Provider<DSNCreator> getDSNProvider() 
	{
		return new FailedDeliveryDSNCreatorProvider(this);
	}
	
}
