package org.nhindirect.gateway.smart;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;

import javax.mail.BodyPart;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class CCDAContent_getCCDAPartsTest 
{
	@Test
	public void getCCDAParts_attachedBased64CCDA_assertOnePart() throws Exception
	{
		final File file = new File("./src/test/resources/messages/attachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Collection<BodyPart> parts = CCDAContent.getCCDAParts(msg);
		
		assertEquals(1, parts.size());
		
		final BodyPart part = parts.iterator().next();
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType(part.getContentType())));
	}
	
	@Test
	public void getCCDAParts_nonAttachedBased64CCDA_assertOnePart() throws Exception
	{
		final File file = new File("./src/test/resources/messages/nonAttachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Collection<BodyPart> parts = CCDAContent.getCCDAParts(msg);
		
		assertEquals(1, parts.size());
		
		final BodyPart part = parts.iterator().next();
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType(part.getContentType())));
	}	
	
	@Test
	public void getCCDAParts_noCCDA_assertNoParts() throws Exception
	{
		final File file = new File("./src/test/resources/messages/nonCCD.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Collection<BodyPart> parts = CCDAContent.getCCDAParts(msg);
		
		assertEquals(0, parts.size());
	}		
}
