package org.oddjob;

import java.io.IOException;

import junit.framework.TestCase;

import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

public class OddjobSerializeTest extends TestCase {
	
	public void testSerializeAndReRun() throws IOException, ClassNotFoundException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <echo id='e'>Apples</echo>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		assertEquals("Apples", new OddjobLookup(test).lookup("e.text"));

		Oddjob copy = OddjobTestHelper.copy(test);
		
		assertEquals(ParentState.COMPLETE, copy.lastStateEvent().getState());
		assertEquals(null, new OddjobLookup(copy).lookup("e"));
		
		copy.load();
		
		OddjobLookup copyLookup = new OddjobLookup(copy);
		
		assertEquals("Apples", copyLookup.lookup("e.text"));
		
		Object echo = copyLookup.lookup("e");
		
		assertEquals(JobState.READY, OddjobTestHelper.getJobState(echo));
	}
	
	public void testSerializeWhenReset() throws IOException, ClassNotFoundException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <echo id='e'>Apples</echo>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());

		test.hardReset();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		Oddjob copy = OddjobTestHelper.copy(test);
		copy.setConfiguration(new XMLConfiguration("XML", xml));
		assertEquals(ParentState.READY, copy.lastStateEvent().getState());
		
		copy.run();
		
		OddjobLookup copyLookup = new OddjobLookup(copy);
		
		assertEquals("Apples", copyLookup.lookup("e.text"));
		
		Object echo = copyLookup.lookup("e");
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(echo));
	}
	
	public void testSerializeNoConfig() throws IOException, ClassNotFoundException {
		
		Oddjob test = new Oddjob();
		
		assertEquals(ParentState.READY, test.lastStateEvent().getState());
		
		Oddjob copy = OddjobTestHelper.copy(test);
		
		assertEquals(ParentState.READY, copy.lastStateEvent().getState());
	}
}
