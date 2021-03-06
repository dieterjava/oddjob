/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;

import junit.framework.TestCase;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

public class EchoJobTest extends TestCase {

	private static final Logger logger = Logger.getLogger(EchoJobTest.class);
	
	public void testInOddjob1() throws Exception {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <echo id='e'>Hello</echo>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		Object test = new OddjobLookup(oj).lookup("e");
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(test));
		assertEquals("Hello", PropertyUtils.getProperty(test, "text"));
	}
	
	public void testInOddjob2() throws Exception {
				
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/EchoTest2.xml",
				getClass().getClassLoader()));
		
		oj.run();
		
		Object test = new OddjobLookup(oj).lookup("2");
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(test));
		assertEquals("Hello", PropertyUtils.getProperty(test, "text"));
	}
	
	public void testLines() throws Exception {
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setArgs(new String[] { dirs.base().getPath() });
		
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/EchoLinesTest.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();

		console.dump(logger);
		
		String[] lines = console.getLines();
		assertEquals(3, lines.length);
		
		oddjob.destroy();
	}
	
	public void testExample1() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/EchoExample.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();

		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("Hello World", lines[0].trim());
		
		assertEquals(1, lines.length);
		
		oddjob.destroy();
	}
	
	public void testExample2() throws Exception {
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/EchoTwice.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();

		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals("Hello World Twice!", lines[0].trim());
		assertEquals("Hello World Twice!", lines[1].trim());
		
		assertEquals(2, lines.length);
		
		oddjob.destroy();
	}
}
