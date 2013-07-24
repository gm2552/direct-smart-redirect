package org.nhindirect.gateway.smart.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;


import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.junit.Test;

import org.nhindirect.gateway.smart.utils.MockMail;

public class ContainsCCDA_matchTest 
{
	@Test
	public void match_attachedCCDA_assertRecips() throws Exception
	{
		final File file = new File("./src/test/resources/messages/attachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		ContainsCCDA matcher = new ContainsCCDA();
		
		@SuppressWarnings("unchecked")
		Collection<MailAddress> recips = matcher.match(mockMail);
		
		assertEquals(1, recips.size());
	}
	
	@Test
	public void match_attachedCCDA_multipRecips_assertRecips() throws Exception
	{
		final File file = new File("./src/test/resources/messages/attachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org"),
				new MailAddress("direct-inpatient2@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		ContainsCCDA matcher = new ContainsCCDA();
		
		@SuppressWarnings("unchecked")
		Collection<MailAddress> recips = matcher.match(mockMail);
		
		assertEquals(2, recips.size());
	}
	
	@Test
	public void match_nonAttachedCCDA_assertRecips() throws Exception
	{
		final File file = new File("./src/test/resources/messages/nonAttachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		ContainsCCDA matcher = new ContainsCCDA();
		
		@SuppressWarnings("unchecked")
		Collection<MailAddress> recips = matcher.match(mockMail);
		
		assertEquals(1, recips.size());
	}
	
	@Test
	public void match_noCCD_assertRecips() throws Exception
	{
		final File file = new File("./src/test/resources/messages/nonCCD.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		ContainsCCDA matcher = new ContainsCCDA();
		
		@SuppressWarnings("unchecked")
		Collection<MailAddress> recips = matcher.match(mockMail);
		
		assertNull(recips);
	}	
}
