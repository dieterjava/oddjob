/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.standard.StandardFragmentParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;
import org.oddjob.io.BufferType;
import org.oddjob.io.CopyJob;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * Useful utility methods and constants for tests.
 * 
 * @author Rob Gordon.
 */
public class OddjobTestHelper {

	public static final long TEST_TIMEOUT = 10000L;
	
	public static final String LS = System.getProperty("line.separator");
	
	/**
	 * Get the state of a component. Assumes the component is 
	 * {@link Stateful} and throw an Exception if it isn't.
	 * 
	 * @param o
	 * @return
	 */
	public static State getJobState(Object o) {
	    class StateCatcher implements StateListener {
		    State state;
	        public void jobStateChange(StateEvent event) {
	            state = event.getState();
	        }
	    };
	    
		Stateful stateful = (Stateful) o;
	    StateCatcher listener = new StateCatcher();
	    stateful.addStateListener(listener);
	    stateful.removeStateListener(listener);
	    return listener.state;
	}
    
	public static Object[] getChildren(Object o) {
		class ChildCatcher implements StructuralListener {
			List<Object> results = new ArrayList<Object>();
			public void childAdded(StructuralEvent event) {
				synchronized (results) {
					results.add(event.getIndex(), event.getChild());
				}
			}
			public void childRemoved(StructuralEvent event) {
				synchronized (results) {
					results.remove(event.getIndex());
				}
			}
		}
		Structural structural = (Structural) o;
		ChildCatcher cc = new ChildCatcher();
		structural.addStructuralListener(cc);
		structural.removeStructuralListener(cc);		
		return cc.results.toArray();
	}
    
	public static String getIconId(Object object) {
		class IconCatcher implements IconListener {
			String iconId;
			
			public void iconEvent(IconEvent e) {
				iconId = e.getIconId();
			}
		}
		
		Iconic iconic = (Iconic) object;
		
		IconCatcher listener = new IconCatcher();
		iconic.addIconListener(listener);
		iconic.removeIconListener(listener);
		return listener.iconId;
	}

	public static <T> T copy(T object) throws IOException, ClassNotFoundException {
		return ArooaTestHelper.copy(object);
    }

    public static class Surrogate {
    	Object value;
    	public void addConfiguredWhatever(Object value) {
    		this.value = value;
    	}
    }
    
    public static Object createValueFromXml(String xml) 
    throws ArooaParseException {
	    
    	return createValueFromConfiguration(new XMLConfiguration("TEST", xml));
    }

    public static Object createValueFromConfiguration(ArooaConfiguration config) 
    throws ArooaParseException {
	    
    	ArooaDescriptor descriptor = new OddjobDescriptorFactory(
			).createDescriptor(null);
    	
    	return ArooaTestHelper.createValueFromConfiguration(
    			config, descriptor);
    }
    
    public static Object createComponentFromXml(String xml) 
    throws IOException, ArooaParseException {
	    
    	return createComponentFromConfiguration(new XMLConfiguration("TEST", xml));
    }
    
    public static Object createComponentFromConfiguration(ArooaConfiguration config) 
    throws ArooaParseException {
	    
    	ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	StandardFragmentParser parser = new StandardFragmentParser(session);
    	
    	parser.setArooaType(ArooaType.COMPONENT);
    	
    	parser.parse(config);

    	return parser.getRoot();        
    }
    
    public static void register(Object component, final ArooaSession session, String id) {
    	
		class OurContext extends MockArooaContext {
			@Override
			public ArooaSession getSession() {
				return session;
			}
			@Override
			public RuntimeConfiguration getRuntime() {
				return new MockRuntimeConfiguration() {
					@Override
					public void configure() {
					}
				};
			}
		}

		session.getComponentPool().registerComponent(
				new ComponentTrinity(
			component, component, new OurContext()), id);
    }
    
    public static File getWorkDir() {
    	
    	File file = new File("work");
    	
    	if (!file.exists()) {
    		file.mkdir();
    	}
    	
    	return file;
    }
    
    public static String[] streamToLines(InputStream inputStream) {
    	
		BufferType buffer = new BufferType();
		buffer.configured();
		
		CopyJob copy = new CopyJob();
		copy.setInput(inputStream);
		copy.setOutput(buffer.toOutputStream());
		copy.run();
		
		return buffer.getLines();
    }
}