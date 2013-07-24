package org.nhindirect.gateway.smart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.mail.BodyPart;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for working with generic C-CDA content.
 * @author Greg Meyer
 * @since 1.0
 */
public class CCDAContent 
{
	private static final Log LOGGER = LogFactory.getFactory().getInstance(CCDAContent.class);	
	
	protected static final Collection<ContentType> types = new ArrayList<ContentType>();

	static
	{
		types.add(safeContentType("application/xml"));
		types.add(safeContentType("text/xml"));
	}
	
	/**
	 * Creates a {@link javax.mail.internet.ContentType} object from a string suppressing exceptions.
	 * @param type The content type as a raw string.
	 * @return A {@link javax.mail.internet.ContentType} object representing the requested type.  If the type cannot be converted into an
	 * appropriate ContentType object, then null is returned.
	 */
	protected static ContentType safeContentType(String type)
	{
		try
		{
			return new ContentType(type);
		}
		catch (ParseException e) {}
		
		return null;
	}
	
	/**
	 * Determines if the content type represents a multipart.
	 * @param type The content type.
	 * @return True if the content type represents a multipart.  False otherwise.
	 */
    protected static boolean isMultiPart(ContentType type)
    {
    	return type.getPrimaryType().equalsIgnoreCase("multipart");   
    } 
	
    /**
     * Determines if the content type represents an entity that could be a C-CDA document.  Determination is not 100% accurate as the algorithm simply
     * compares the type against a know set of types that may constitute a C-CDA document.
     * @param contType The content type
     * @return True if the content type matches a known type that may constitute a C-CDA document.  False otherwise.
     */
	public static boolean isContentTypeCCDA(ContentType contType)
	{
		for (ContentType type : types)
			if (type.match(contType))
				return true;
		
		return false;
	}
	
	/**
	 * Determines if a MIME message contains a C-CDA document.  The algorithm checks first checks the content type of the message using the {#link {@link #isContentTypeCCDA(ContentType)}
	 * method.  If the message content type does not match a known C-CDA content type, then the message is searched for attachments whose content type matches the same
	 * known list.
	 * @param msg The MIME message to search for C-CDA Content.
	 * @return True if either the message or an attachment may contain a C-CDA content.  False otherwise.
	 */
	public static boolean hasCCDAContent(MimeMessage msg)
	{
		try
		{
			ContentType type = safeContentType(msg.getContentType());
			
			// check if the message itself is just an XML message
			if (isContentTypeCCDA(type))
				return true;
			
			// if the message itself is not a C-CDA, then search attachments
			// this algorithm only searches the first level of a multipart...
			// may want to use a recursive algorithm in the future to search multiparts within multiparts
			else if (isMultiPart(type))
			{
				final MimeMultipart multipart = (MimeMultipart) msg.getContent();
				for (int i = 0; i < multipart.getCount(); ++ i)
				{
					final BodyPart part = multipart.getBodyPart(i);

					type = safeContentType(part.getContentType());
					if (type != null && isContentTypeCCDA(type))
						return true;

				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Error retrieving content from MIME message.", e);
		}
		
		return false;
	}
	
	/**
	 * Retrieves parts from a MIME message that may contain C-CDA documents.  Each returned part will have a content type that 
	 * corresponds to one of the known types that may constitute a C-CDA.  However, the part's document may not necessarily represent
	 * a C-CDA; deep inspection may be necessary for positive determination.
	 * @param msg The message to retrieve C-CDA parts from.
	 * @return A collection of body parts that may contain C-CDA documents.  Returns an empty collection
	 * if not parts containing C-CDAs can be found.
	 */
	public static Collection<BodyPart> getCCDAParts(MimeMessage msg)
	{
		final Collection<BodyPart> retVal = new ArrayList<BodyPart>();
		
		try
		{
			ContentType type = safeContentType(msg.getContentType());
			
			// check if the message itself is just an XML message
			if (isContentTypeCCDA(type))
			{
				final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
				InputStream iStream = null;;
				try
				{
					
					msg.writeTo(oStream);
					oStream.flush();
					iStream = new ByteArrayInputStream(oStream.toByteArray());
					final BodyPart part = new MimeBodyPart(iStream);
					retVal.add(part);
				}
				finally
				{
					IOUtils.closeQuietly(oStream);
					IOUtils.closeQuietly(iStream);
				}
			}
			else if (isMultiPart(type))
			{
				// iterate through the parts and pull out
				// any XML parts
				final MimeMultipart multipart = (MimeMultipart) msg.getContent();
				for (int i = 0; i < multipart.getCount(); ++ i)
				{
					final BodyPart part = multipart.getBodyPart(i);

					type = safeContentType(part.getContentType());
					if (type != null && isContentTypeCCDA(type))
						retVal.add(part);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Error extracting C-CDAs parts from MIME message.", e);
		}
		
		return retVal;
	}

}
