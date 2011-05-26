/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resetable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.MockComponentPersister;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.state.JobState;

public class SerializableWrapperTest extends TestCase {
	private static final Logger logger = Logger.getLogger(SerializableWrapperTest.class);
	
	public static class Test1 implements Runnable, Serializable {
		private static final long serialVersionUID = 20051231;
		private String check;
		
		public void run() {
			if (check != null) {
				check = "good bye";
			}
			else {
				check = "hello";
			}
		}

		public String getCheck() { 
			return check; 
		}
		
		@Override
		public String toString() {
			return "Test1";
		}
	}
	
	public static class Test2 implements Runnable {
		public void run() {}
		
		@Override
		public String toString() {
			return "Test2";
		}
	}
	
	public void testSimple() throws Exception {	

		Runnable test = new Test1();
		Runnable wrapper = RunnableWrapper.wrapperFor(
				test, getClass().getClassLoader());
	
		wrapper.run();
		
		DynaBean copy = (DynaBean) Helper.copy(wrapper);
		
		assertTrue(copy instanceof Proxy);
		
		assertEquals("hello", copy.get("check"));
	}
	
	public void testNotSerializable () throws Exception {
		Runnable test = new Test2();
		
		Object wrapper = RunnableWrapper.wrapperFor(
				test, getClass().getClassLoader());
		
		assertTrue(wrapper instanceof Transient);
	}
	
	private class OurPersister implements OddjobPersister {

		Object save;
		int count;
		public ComponentPersister persisterFor(String id) {
			return new MockComponentPersister() {
				boolean closed;
				@Override
				public void persist(String id, Object proxy, ArooaSession session) {
					if (closed) {
						return;
					}
					logger.debug("Persisting [" + proxy + "] id [" + id + "]");
					assertEquals("test", id);

					try {
						save = Helper.copy(proxy);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					
					++count;
				}
				
				@Override
				public Object restore(String id, ClassLoader classLoader, 
						ArooaSession session) {
					assertEquals("test", id);
					
					return save;
				}
				
				@Override
				public void remove(String id, ArooaSession session) {
					if (closed) {
						return;
					}
					save = null;
				}

				@Override
				public void close() {
					closed = true;
				}
			};
		}		
	}
	
	public void testSerializeInOddjob() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean class='" + Test1.class.getName() + "' id='test'/>" +
			" </job>" +
			"</oddjob>";

		
		Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		OurPersister persister = new OurPersister();
		oddjob.setPersister(persister);
		
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
		
		Proxy proxy = (Proxy) new OddjobLookup(oddjob).lookup("test");
		Test1 test1 = (Test1) ((RunnableWrapper) Proxy.getInvocationHandler(
				proxy)).getWrapped();

		assertEquals(JobState.COMPLETE, Helper.getJobState(proxy));		
		assertEquals("hello", test1.check);
		
		oddjob.destroy();
		
		Oddjob oddjob2 = new Oddjob();
    	oddjob2.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob2.setPersister(persister);
		
		oddjob2.load();
		
		assertEquals(JobState.READY, oddjob2.lastJobStateEvent().getJobState());
		
		proxy = (Proxy) new OddjobLookup(oddjob2).lookup("test");
		test1 = (Test1) ((RunnableWrapper) Proxy.getInvocationHandler(
				proxy)).getWrapped();
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(proxy));
		assertEquals("hello", test1.check);
		
		assertEquals(1, persister.count);
		
		oddjob2.destroy();
	}
	
	public void testPersistCount() {
		
		Oddjob oddjob = new Oddjob();
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean class='" + Test1.class.getName() + "' id='test'/>" +
				" </job>" +
				"</oddjob>";
		
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		OurPersister persister = new OurPersister();
		
		oddjob.setPersister(persister);
		
		oddjob.run();

		assertEquals(JobState.COMPLETE, oddjob.lastJobStateEvent().getJobState());
		
		Object proxy = new OddjobLookup(oddjob).lookup("test");
		
		((Resetable) proxy).hardReset();
		
		((Runnable) proxy).run();
		
		assertEquals(3, persister.count);
		
		oddjob.destroy();
	}
}
