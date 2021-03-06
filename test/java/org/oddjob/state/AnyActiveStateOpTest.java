package org.oddjob.state;

import junit.framework.TestCase;

public class AnyActiveStateOpTest extends TestCase {

	public void testEvaluateSingleJobOp() {
		
		AnyActiveStateOp test = new AnyActiveStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION));
				
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE));
				
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE));
		
	}
	
	public void testAssociative() {

		AnyActiveStateOp test = new AnyActiveStateOp();
		
		ParentState[] values = ParentState.values();
		
		for (int i = 0; i < values.length - 1; ++i) {
			for (int j = 0; j < values.length - 1; ++j) {
				ParentState oneWay = test.evaluate(values[i], values[j]);
				ParentState otherWay = test.evaluate(values[j], values[i]);
				
				assertSame("Failed on i=" + i + ", j = " + j,
						oneWay, otherWay);
			}
		}
	}
	
	public void testEvaluateSingleServiceOp() {
		
		AnyActiveStateOp test = new AnyActiveStateOp();
				
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STARTABLE));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED));
				
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION));
				
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.STOPPED));
		
	}
	
	public void testEvaluateTwoJobOps() {
		
		AnyActiveStateOp test = new AnyActiveStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.READY, JobState.EXECUTING));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.READY, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.READY, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.READY, JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.EXECUTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.COMPLETE));

		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.INCOMPLETE));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXECUTING, JobState.EXCEPTION));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXECUTING));
		
		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.INCOMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.INCOMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(JobState.COMPLETE, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.COMPLETE, JobState.EXECUTING));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(JobState.COMPLETE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.COMPLETE, JobState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(JobState.EXCEPTION, JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(JobState.EXCEPTION, JobState.EXCEPTION));
	}
	
	public void testEvaluateTwoServiceOps() {
		
		AnyActiveStateOp test = new AnyActiveStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STARTABLE, ServiceState.STARTABLE));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTABLE, ServiceState.STARTING));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTABLE, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STARTABLE, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STARTABLE, ServiceState.STOPPED));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.STARTABLE));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.STARTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, ServiceState.STOPPED));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, ServiceState.STARTABLE));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, ServiceState.STARTED));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, ServiceState.STOPPED));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STARTABLE, ServiceState.STARTABLE));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STOPPED, ServiceState.STARTING));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STOPPED, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STOPPED, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.STOPPED, ServiceState.STOPPED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.STARTABLE));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.STARTING));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.STARTED));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, ServiceState.STOPPED));
		
	}
	
	public void testEvaluateAJobAndAServiceOp() {
		
		AnyActiveStateOp test = new AnyActiveStateOp();
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STARTABLE, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTABLE, JobState.EXECUTING));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STARTABLE, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(ServiceState.STARTABLE, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STARTABLE, JobState.EXCEPTION));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.EXECUTING));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.COMPLETE));

		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.INCOMPLETE));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTING, JobState.EXCEPTION));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STARTED, JobState.EXECUTING));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, JobState.COMPLETE));

		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, JobState.INCOMPLETE));
		
		assertEquals(ParentState.STARTED, 
				test.evaluate(ServiceState.STARTED, JobState.EXCEPTION));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.EXCEPTION, JobState.EXECUTING));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.COMPLETE));

		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.EXCEPTION, JobState.EXCEPTION));
		
		assertEquals(ParentState.READY, 
				test.evaluate(ServiceState.STOPPED, JobState.READY));
		
		assertEquals(ParentState.ACTIVE, 
				test.evaluate(ServiceState.STOPPED, JobState.EXECUTING));
		
		assertEquals(ParentState.COMPLETE, 
				test.evaluate(ServiceState.STOPPED, JobState.COMPLETE));

		assertEquals(ParentState.INCOMPLETE, 
				test.evaluate(ServiceState.STOPPED, JobState.INCOMPLETE));
		
		assertEquals(ParentState.EXCEPTION, 
				test.evaluate(ServiceState.STOPPED, JobState.EXCEPTION));
		
	}
	
	public void testDestroyed() {

		
		AnyActiveStateOp test = new AnyActiveStateOp();
		
		try {
			assertEquals(JobState.DESTROYED, 
					test.evaluate(JobState.DESTROYED, JobState.DESTROYED));
			fail("Should fail");
		} catch (IllegalStateException e) {
			// expected.
		}
	}
}
