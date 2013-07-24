package org.nhindirect.gateway.smart.resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.util.AvailablePortFinder;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class CCDAServiceRunner 
{
	private static Server server;
	private static int HTTPPort;
	private static String ccdaServiceURL;
	
	public synchronized static void startService() throws Exception
	{
		
		if (server == null)
		{
			/*
			 * Setup the configuration service server
			 */
			server = new Server();
			SocketConnector connector = new SocketConnector();
			
			HTTPPort = AvailablePortFinder.getNextAvailable( 1024 );
			connector.setPort(HTTPPort);
	
		    Context context = new Context(Context.SESSIONS|Context.SECURITY);
			
		    
	    	Map<String, String> contextInitParams = new HashMap<String, String>();
	    	context.setInitParams(contextInitParams);
	    	context.setContextPath("/");	 
	    	
	    	ServletHolder holder = context.addServlet("com.sun.jersey.spi.container.servlet.ServletContainer", "/*");
	    	holder.setInitParameter("javax.ws.rs.Application", "org.nhindirect.gateway.smart.resources.MockCCDAApplication");
	    	holder.setInitOrder(1); 
	    	 
	    	server.setSendServerVersion(false);
	    	server.addConnector(connector);
	    	server.addHandler(context);
	    	
	    	try
	    	{
	    		server.start();
	    	}
	    	catch (Exception e)
	    	{
	    		throw new RuntimeException(e);
	    	}
		    
	    	ccdaServiceURL = "http://localhost:" + HTTPPort + "/ccda/";
		}
	}
	
	public synchronized static boolean isServiceRunning()
	{
		return (server != null && server.isRunning());
	}
	
	public synchronized static void shutDownService() throws Exception
	{
		if (isServiceRunning())
		{
			server.stop();
			server = null;
		}
	}
	
	public synchronized static String getCCDAServiceURL()
	{
		return ccdaServiceURL;
	}
}
