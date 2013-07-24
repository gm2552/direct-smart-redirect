package org.nhindirect.gateway.smart.matcher;

import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMatcher;
import org.nhindirect.gateway.smart.CCDAContent;

public class ContainsCCDA extends GenericMatcher
{
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
