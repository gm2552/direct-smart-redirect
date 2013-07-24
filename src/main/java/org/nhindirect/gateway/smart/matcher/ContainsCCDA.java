package org.nhindirect.gateway.smart.matcher;

import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMatcher;
import org.nhindirect.gateway.smart.CCDAContent;

/**
 * Matcher that determines if a message may contain a C-CDA document.  Determination is not 100% accurate as the algorithm simply
 * compares the content type against a know set of types that may constitute a C-CDA document.
 * @author Greg Meyer
 * @since 1.0
 */
public class ContainsCCDA extends GenericMatcher
{
	/**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("rawtypes")
    public Collection match(Mail mail) throws MessagingException 
    {
        if (mail == null) 
        	return null;
        
        final MimeMessage msg = mail.getMessage();
        if (msg == null) 
        	return null;
        
        return (CCDAContent.hasCCDAContent(msg)) ? mail.getRecipients() : null;
    }
}
