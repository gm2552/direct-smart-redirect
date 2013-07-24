package org.nhindirect.gateway.smart;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import javax.mail.internet.ContentType;

import org.junit.Test;

public class CCDAContent_isContentTypeCCDATest 
{
	@Test
	public void isContentTypeCCDA_appXML_assertTrue() throws Exception
	{
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType("application/xml")));
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType("application/XML")));
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType("APPLICATION/xml")));
	}
	
	@Test
	public void isContentTypeCCDA_testXML_assertTrue() throws Exception
	{
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType("text/xml")));
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType("text/XML")));
		assertTrue(CCDAContent.isContentTypeCCDA(new ContentType("TEXT/xml")));
	}
	
	@Test
	public void isContentTypeCCDA_nonXML_assertFalse() throws Exception
	{
		assertFalse(CCDAContent.isContentTypeCCDA(new ContentType("application/json")));
		assertFalse(CCDAContent.isContentTypeCCDA(new ContentType("text/plain")));
	}	
}
