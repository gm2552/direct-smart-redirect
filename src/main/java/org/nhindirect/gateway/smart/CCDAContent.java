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

public class CCDAContent 
{
	protected static final Collection<ContentType> types = new ArrayList<ContentType>();

	static
	{
		types.add(safeContentType("application/xml"));
		types.add(safeContentType("text/xml"));
	}
	
	static ContentType safeContentType(String type)
	{
		try
		{
			return new ContentType(type);
		}
		catch (ParseException e) {}
		
		return null;
	}
	
    protected static boolean isMultiPart(ContentType type)
    {
    	return type.getPrimaryType().equalsIgnoreCase("multipart");   
    } 
	
	public static boolean isContentTypeCCDA(ContentType contType)
	{
		for (ContentType type : types)
			if (type.match(contType))
				return true;
		
		return false;
	}
	
	public static boolean hasCCDAContent(MimeMessage msg)
	{
		try
		{
			ContentType type = new ContentType(msg.getContentType());
			
			// check if the message itself is just an XML message
			if (isContentTypeCCDA(type))
				return true;
			
			else if (isMultiPart(type))
			{
				final MimeMultipart multipart = (MimeMultipart) msg.getContent();
				for (int i = 0; i < multipart.getCount(); ++ i)
				{
					final BodyPart part = multipart.getBodyPart(i);
					try
					{
						type = new ContentType(part.getContentType());
						if (isContentTypeCCDA(type))
							return true;
					}
					catch (Exception e)
					{
						//TODO: log exception
					}
				}
			}
		}
		catch (Exception e)
		{
			//TODO: log exception
		}
		
		return false;
	}
	
	public static Collection<BodyPart> getCCDAParts(MimeMessage msg)
	{
		final Collection<BodyPart> retVal = new ArrayList<BodyPart>();
		
		try
		{
			ContentType type = new ContentType(msg.getContentType());
			
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
					try
					{
						type = new ContentType(part.getContentType());
						if (isContentTypeCCDA(type))
							retVal.add(part);
					}
					catch (Exception e)
					{
						//TODO: log exception
					}
				}
			}
		}
		catch (Exception e)
		{
			//TODO: log exception
		}
		
		return retVal;
	}

}
