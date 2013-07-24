package org.nhindirect.gateway.smart.mailet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.MailetConfig;
import org.junit.Before;
import org.junit.Test;

import org.nhindirect.gateway.smart.resources.CCDAServiceRunner;
import org.nhindirect.gateway.smart.resources.MockCCDAApplication;
import org.nhindirect.gateway.smart.utils.MockMail;
import org.nhindirect.gateway.smart.utils.MockMailetConfig;


public class SmartCCDAReDirect_serviceTest 
{
	@Before
	public void setUp() throws Exception
	{
		if (!CCDAServiceRunner.isServiceRunning())
			CCDAServiceRunner.startService();
		
		if (MockCCDAApplication.getCurrentAuditResouces() != null)
			MockCCDAApplication.getCurrentAuditResouces().clearPostedCCDAs();
	}
	
	protected MailetConfig getMailetConfig(String URITemplate) throws Exception
	{
		Map<String,String> params = new HashMap<String, String>();
		
		params.put("CCDAPostUrl", CCDAServiceRunner.getCCDAServiceURL() + URITemplate);
		
		
		return new MockMailetConfig(params, "SmartCCDAReDirect");	
	}
	
	@Test
	public void service_attachedCDA_noToFromTemplate_assertSentToService() throws Exception
	{
		SmartCCDAReDirect theMailet = new SmartCCDAReDirect();

		MailetConfig config = getMailetConfig("");
		
		theMailet.init(config);
		
		final File file = new File("./src/test/resources/messages/attachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		theMailet.service(mockMail);
		
		assertEquals(1, MockCCDAApplication.getCurrentAuditResouces().getPostedCCDA().size());
	}
	
	@Test
	public void service_attachedCDA_multipleRecips_toTemplate_assertSentToService() throws Exception
	{
		SmartCCDAReDirect theMailet = new SmartCCDAReDirect();

		MailetConfig config = getMailetConfig("foruser/{to}");
		
		theMailet.init(config);
		
		final File file = new File("./src/test/resources/messages/attachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org"),
				new MailAddress("direct-inpatient2@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		
		theMailet.service(mockMail);

		
		assertEquals(2, MockCCDAApplication.getCurrentAuditResouces().getPostedCCDA().size());
	}
	
	@Test
	public void service_attachedCDA_multipleRecips_toAndFromTemplate_assertSentToService() throws Exception
	{
		SmartCCDAReDirect theMailet = new SmartCCDAReDirect();

		MailetConfig config = getMailetConfig("foruser/{to}/{from}");
		
		theMailet.init(config);
		
		final File file = new File("./src/test/resources/messages/attachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org"),
				new MailAddress("direct-inpatient2@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		theMailet.service(mockMail);
		
		assertEquals(2, MockCCDAApplication.getCurrentAuditResouces().getPostedCCDA().size());
	}
	
	@Test
	public void service_nonAttachedCDA_noToFromTemplate_assertSentToService() throws Exception
	{
		SmartCCDAReDirect theMailet = new SmartCCDAReDirect();

		MailetConfig config = getMailetConfig("");
		
		theMailet.init(config);
		
		final File file = new File("./src/test/resources/messages/nonAttachedCCDBase64Encoded.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		theMailet.service(mockMail);
		
		assertEquals(1, MockCCDAApplication.getCurrentAuditResouces().getPostedCCDA().size());
	}	
	
	@Test
	public void service_noCDA_noToFromTemplate_assertSentToService() throws Exception
	{
		SmartCCDAReDirect theMailet = new SmartCCDAReDirect();

		MailetConfig config = getMailetConfig("");
		
		theMailet.init(config);
		
		final File file = new File("./src/test/resources/messages/nonCCD.eml");
		final MimeMessage msg = new MimeMessage(null, FileUtils.openInputStream(file));
		
		final Mail mockMail = mock(MockMail.class, CALLS_REAL_METHODS);
		when(mockMail.getRecipients()).thenReturn(Arrays.asList(new MailAddress("direct-inpatient@ttt.transport-testing.org")));
		when(mockMail.getSender()).thenReturn(new MailAddress("gm2552@direct.securehealthemail.com"));

		mockMail.setMessage(msg);
		
		theMailet.service(mockMail);
		
		assertEquals(0, MockCCDAApplication.getCurrentAuditResouces().getPostedCCDA().size());
	}	
	
	protected static class MailSubmitter implements Runnable
	{
		protected final SmartCCDAReDirect mailet;
		protected final Mail mailToSend;
		
		public MailSubmitter(SmartCCDAReDirect mailet, Mail mailToSend)
		{
			this.mailet = mailet;
			this.mailToSend = mailToSend;
		}
		
		public void run()
		{
			try
			{
				mailet.service(mailToSend);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
