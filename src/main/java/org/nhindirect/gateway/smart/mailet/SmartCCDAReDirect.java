package org.nhindirect.gateway.smart.mailet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.base.GenericMailet;

import org.nhindirect.gateway.smart.CCDAContent;


public class SmartCCDAReDirect extends GenericMailet
{
	private static final Log LOGGER = LogFactory.getFactory().getInstance(SmartCCDAReDirect.class);	
	
	String contextDefFile;
	String postURLTemplate;
	CamelContext context;
	
	/**
	 * {@inheritDoc}
	 */
	public void init() throws MessagingException
	{
		LOGGER.info("Initializing SmartCCDAReDirect mailet");
		
		super.init();
		
		postURLTemplate = this.getInitParameter("CCDAPostUrl");
				
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
	}
	
	protected CamelContext createCamelContext()
	{
		return new DefaultCamelContext();
	}
	
	protected Exchange createCamelExchange(CamelContext context)
	{
		return new DefaultExchange(context);
	}
	
	@Override
	public void service(Mail mail) throws MessagingException 
	{	
		LOGGER.info("Servicing CCDA reDirect");
		
		// could use a split and send EIP, but for simplicity we will split the message
		// into the CCDA parts and use a URL template right here in line
		
		final Collection<BodyPart> ccdaParts = CCDAContent.getCCDAParts(mail.getMessage());
		
		// get the list of recipients and sender
		Collection<InternetAddress> recips = getMailRecipients(mail);
		InternetAddress sender = getSender(mail);
		
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
			
		}
		
		try
		{
			template.stop();
		}
		catch (Exception e) {};
	}
	
	protected Collection<String> getPostToURLs(Collection<InternetAddress> recips, InternetAddress sender)
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
	
	@SuppressWarnings("unchecked")
	protected Collection<InternetAddress> getMailRecipients(Mail mail) throws MessagingException
	{
		final Collection<InternetAddress> recipients = new ArrayList<InternetAddress>();		
		
		// uses the RCPT TO commands
		final Collection<MailAddress> recips = mail.getRecipients();
		if (recips == null || recips.size() == 0)
		{
			// fall back to the mime message list of recipients
			final Address[] recipsAddr = mail.getMessage().getAllRecipients();
			for (Address addr : recipsAddr)
			{
				recipients.add((InternetAddress)addr);
			}
		}
		else
		{
			for (MailAddress addr : recips)
			{
				recipients.add(addr.toInternetAddress());
			}
		}
		
		return recipients;
	}
	
	public static InternetAddress getSender(Mail mail) 
	{
		InternetAddress retVal = null;
		
		if (mail.getSender() != null)
			retVal = mail.getSender().toInternetAddress();	
		else
		{
			// try to get the sender from the message
			Address[] senderAddr = null;
			try
			{
				if (mail.getMessage() == null)
					return null;
				
				senderAddr = mail.getMessage().getFrom();
				if (senderAddr == null || senderAddr.length == 0)
					return null;
			}
			catch (MessagingException e)
			{
				return null;
			}
						
			// not the best way to do this
			retVal = (InternetAddress)senderAddr[0];	
		}
	
		return retVal;
	}
}
