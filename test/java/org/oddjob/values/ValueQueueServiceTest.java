package org.oddjob.values;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.oddjob.tools.WaitForChildren;

public class ValueQueueServiceTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(ValueQueueService.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-------------------------------------------  " + getName() +
				"  -----------------------------------------");
	}

	public void testQueueWithTwoConsumers() throws InterruptedException {
		
		final BlockingQueue<Object> results = new LinkedBlockingQueue<Object>();
				
		final ValueQueueService test = new ValueQueueService();
		
		class Puller implements Runnable {
			@Override
			public void run() {
				for (Object value : test.getValues()) {
					results.add(value);
				}
			}
		}

		test.start();
		
		Thread t1 = new Thread(new Puller());
		Thread t2 = new Thread(new Puller());
		
		t1.start();
		t2.start();
		
		test.setValue("apples");
		
		assertEquals("apples", results.take());
		
		test.setValue("oranges");
		
		assertEquals("oranges", results.take());
		
		test.stop();
		
		t1.join();
		t2.join();
	}
	
	public void testInOddjobWithFor() throws FailedToStopException, InterruptedException {
		
		Oddjob server = new Oddjob();
		server.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/ValueQueueExample.xml",
				getClass().getClassLoader()));
		server.load();
		
		OddjobLookup lookup = new OddjobLookup(server);
		
		Object jobs = lookup.lookup("jobs");
		
		StateSteps serverState = new StateSteps((Stateful) jobs);
		serverState.startCheck(ParentState.READY, ParentState.EXECUTING);
		
		logger.info("** Starting Server **");
		
		Thread t = new Thread(server);
		t.start();
		
		serverState.checkWait();
		
		Oddjob client1 = new Oddjob();
		client1.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/ValueQueueClient.xml",
				getClass().getClassLoader()));
		client1.setArgs(new String[] { "apples" });
		client1.run();
		
		assertEquals(ParentState.COMPLETE, 
				client1.lastStateEvent().getState());
		client1.destroy();
		
		Oddjob client2 = new Oddjob();
		client2.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/ValueQueueClient.xml",
				getClass().getClassLoader()));
		client2.setArgs(new String[] { "oranges" });
		client2.run();
		
		assertEquals(ParentState.COMPLETE, 
				client2.lastStateEvent().getState());
		client2.destroy();
		
		logger.info("** Waiting For 2 Children On Server For Each **");
		
		Object foreach = lookup.lookup("foreach");
		
		WaitForChildren waitForChildren = new WaitForChildren(foreach);
		waitForChildren.waitFor(2);
		
		logger.info("** Stoping Server **");
		
		// Stop queue first or ForEach won't stop.
		Object queue = lookup.lookup("queue");
		((Stoppable) queue).stop();
		
		server.stop();
		t.join();
		
		logger.info("** Destroying Server **");
		
		server.destroy();
	}
}
