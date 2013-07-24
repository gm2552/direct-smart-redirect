package org.nhindirect.gateway.smart.resources;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

public class MockCCDAApplication extends Application
{
	   private final Set<Class<?>> classes = Collections.<Class<?>> emptySet();
	   private final Set<Object> singletons;

	   private volatile static MockCCDAPostResource ccdaResource;
	   
	    public MockCCDAApplication() 
	    {
	    	super();
	    	singletons = new HashSet<Object>();
	    }
	    
	    /**
	     * Create the application with the necessary resources and providers.
	     */
	    public MockCCDAApplication(@Context ServletContext _context) 
	    {
	    	this();
	    	init(_context);
	    }

		public void init(ServletContext _context)
		{
			ccdaResource = new MockCCDAPostResource();

			singletons.add(ccdaResource);
		}
	    
	    @Override
	    public Set<Class<?>> getClasses() 
	    {
	    	
	        return classes;
	    }

	    @Override
	    public Set<Object> getSingletons() 
	    {
	        return singletons;
	    }  
	    
	    /**
	     * Used for integration testing
	     * @return The current post resource POJO
	     */
	    public static MockCCDAPostResource getCurrentAuditResouces()
	    {
	    	return ccdaResource;
	    }
}
