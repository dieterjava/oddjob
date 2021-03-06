package org.oddjob.jmx.handlers;

import org.apache.log4j.Logger;
import org.oddjob.tools.OddjobTestHelper;

import junit.framework.TestCase;

public class OddjobTransportableExceptionTest extends TestCase{

	private static final Logger logger = Logger.getLogger(OddjobTransportableExceptionTest.class);
	
	public void testCreate() throws Exception {
		
		Exception e = new Exception("An error occurred!", 
				new Exception("Because of this", 
						new Exception("And this")));
		
		OddjobTransportableException test = 
				new OddjobTransportableException(e);

		OddjobTransportableException copy1 = OddjobTestHelper.copy(test);
		
		logger.info("The exception.", copy1);
		
		assertEquals("java.lang.Exception: An error occurred!", copy1.toString());
		
		Throwable copy2 = copy1.getCause();

		assertEquals("java.lang.Exception: Because of this", copy2.toString());
		
		Throwable copy3 = copy2.getCause();

		assertEquals("java.lang.Exception: And this", copy3.toString());
	}
	
	public void testNullMessage() {
		
		Exception e = new Exception();
		
		OddjobTransportableException test = 
				new OddjobTransportableException(e);

		assertNull(test.getMessage());
	}
}
