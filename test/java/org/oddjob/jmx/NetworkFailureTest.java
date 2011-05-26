package org.oddjob.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.remote.rmi.RMIConnectorServer;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.Stateful;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jobs.WaitJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;

public class NetworkFailureTest extends TestCase {

	private static final Logger logger = Logger.getLogger(NetworkFailureTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		logger.debug("------------------- " + getName() + " --------------");
	}
	
	public void testSimpleExample() throws Exception {
	
		FlagState root = new FlagState();
		root.setName("Our Job");
		
		Map<String, Object> env = new HashMap<String, Object>();

//		FailableSocketFactory csf =  
//			new FailableSocketFactory(); 
		
		FailableSocketFactory ssf =  
			new FailableSocketFactory(); 
		
//		env.put(RMIConnectorServer. 
//				RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,csf); 
		
		env.put(RMIConnectorServer. 
				RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,ssf); 

		JMXServerJob server = new JMXServerJob();
		server.setRoot(root);
		server.setArooaSession(new StandardArooaSession());
		server.setUrl("service:jmx:rmi://");
		server.setEnvironment(env);
		
		server.start();
		
		JMXClientJob client = new JMXClientJob();
		client.setUrl(server.getAddress());
		client.setArooaSession(new StandardArooaSession());
		client.setHeartbeat(500);
		client.run();
		
		Object[] children = Helper.getChildren(client);
		
		assertEquals(1, children.length);
		
		Stateful child = (Stateful) children[0];
		
		assertEquals("Our Job", child.toString()); 
		
		ssf.setFail(true);
		
		logger.debug("Server Job Running.");
		
		root.run();
		
		WaitJob wait = new WaitJob();
		wait.setFor(client);
		wait.setState("!executing");
		wait.setPause(1000);
		wait.run();
		
		ssf.setFail(false);
		
		logger.debug("Client Running Again.");
		
		client.hardReset();
		
		client.run();
		
		children = Helper.getChildren(client);
		
		assertEquals(1, children.length);
		
		child = (Stateful) children[0];
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(child));
		assertEquals("Our Job", child.toString()); 
		
		client.stop();
		
		server.stop();
	}
	
	
}
