/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.values.properties;

import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Describeable;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * Tests for EnvrionmentType. 
 */
public class PropertiesEnvironmentTest extends TestCase {
	private static final Logger logger = Logger.getLogger(PropertiesEnvironmentTest.class);

	protected void setUp() {
		logger.debug("================== " + getName() + " ===================");
				
	}
		
	/**
	 * Test that a property gets set from the environment..
	 *
	 */
	public void testInOddjob() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/properties/PropertiesJobEnvironment.xml", 
				getClass().getClassLoader()));
		
		oddjob.load();

		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object echo = lookup.lookup("echo-path");
		((Runnable) echo).run();
		
		String text = lookup.lookup(
				"echo-path.text", String.class);
		
		assertEquals("Path is ", text);
		
		((Resetable) echo).hardReset();
		
		oddjob.run();
		
		text = lookup.lookup(
				"echo-path.text", String.class);
		
		assertEquals("Path is " + System.getenv("PATH"), text);
		
		Object test = lookup.lookup("props");
		
		Map<String, String> description = 
				((Describeable) test).describe();
		
		assertTrue(description.size() > 0);
		
		((Resetable) test).hardReset();
		((Resetable) echo).hardReset();
		((Runnable) echo).run();
		
		text = lookup.lookup(
				"echo-path.text", String.class);
		
		assertEquals("Path is ", text);
		
		oddjob.destroy();
	}
}
