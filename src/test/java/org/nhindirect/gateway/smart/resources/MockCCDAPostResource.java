package org.nhindirect.gateway.smart.resources;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ccda")
public class MockCCDAPostResource 
{
	static protected final CacheControl noCache;
	
	protected Collection<String> postedCCDAs;
	
	static
	{
		noCache = new CacheControl();
		noCache.setNoCache(true);
	}
	
	public MockCCDAPostResource()
	{
		postedCCDAs = new ArrayList<String>();
	}

	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response postToResource(String ccda)
	{
		System.out.println("Resource POST");
		
		postedCCDAs.add(ccda);
		
		return Response.ok().build();
	}
	
	@Path("/foruser/{to}")
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response postToResourceForRecip(String ccda)
	{
		System.out.println("Resource POST");
		
		postedCCDAs.add(ccda);
		
		return Response.ok().build();
	}
	
	@Path("/foruser/{to}/{from}")
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
	public Response postToResourceForRecipAndSender(String ccda)
	{
		System.out.println("Resource POST");
		
		postedCCDAs.add(ccda);
		
		return Response.ok().build();
	}	
	
	public Collection<String> getPostedCCDA()
	{
		return postedCCDAs;
	}
	
	public void clearPostedCCDAs()
	{
		postedCCDAs.clear();
	}
}
