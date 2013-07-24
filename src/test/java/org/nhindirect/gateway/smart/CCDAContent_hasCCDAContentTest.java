package org.nhindirect.gateway.smart;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class CCDAContent_hasCCDAContentTest 
{
	@Test
	public void hasCCDAContent_attachedBased64CCDA_assertTrue() throws Exception
	{
		final File file = new File("./src/test/resources/messages/attachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		assertTrue(CCDAContent.hasCCDAContent(msg));
	}
	
	@Test
	public void hasCCDAContent_nonAttachedBased64CCDA_assertTrue() throws Exception
	{
		final File file = new File("./src/test/resources/messages/nonAttachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		assertTrue(CCDAContent.hasCCDAContent(msg));
	}
	
	@Test
	public void hasCCDAContent_nonCDA_assertFalse() throws Exception
	{
		final File file = new File("./src/test/resources/messages/nonCCD.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		assertFalse(CCDAContent.hasCCDAContent(msg));
	}
}
