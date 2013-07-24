package org.nhindirect.gateway.smart;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import javax.mail.internet.ContentType;


import org.junit.Test;

public class CCDAContent_isMutlipartTest 
{

	@Test
	public void testIsMultiPart_multipartMixed_assertTrue() throws Exception
	{
		assertTrue(CCDAContent.isMultiPart(new ContentType("multipart/mixed")));
	}
	
	@Test
	public void testIsMultiPart_multipartAlternative_assertTrue() throws Exception
	{
		assertTrue(CCDAContent.isMultiPart(new ContentType("multipart/alternative")));
	}
	
	@Test
	public void testIsMultiPart_textXML_assertFalse() throws Exception
	{
		assertFalse(CCDAContent.isMultiPart(new ContentType("text/xml")));
	}
}
