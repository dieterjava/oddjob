/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.input.InputHandler;
import org.oddjob.input.InputRequest;
import org.oddjob.jobs.EchoJob;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OurDirs;
import org.oddjob.tools.StateSteps;

/**
 *
 * @author Rob Gordon.
 */
public class OddjobTest extends TestCase {
	
	private static final Logger logger = Logger.getLogger(OddjobTest.class);
	
	/**
	 * Test resetting Oddjob
	 *
	 */
	public void testReset() {
		class MyStructuralListener implements StructuralListener {
			int count;
			public void childAdded(StructuralEvent event) {
				count++;
			}
			public void childRemoved(StructuralEvent event) {
				count--;
			}
		}
		
		String xml = 
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:flag id='flag' state='COMPLETE'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		StateSteps ojSteps = new StateSteps(oj);
		
		ojSteps.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		MyStructuralListener childListener = new MyStructuralListener();
		oj.addStructuralListener(childListener);
		assertEquals(0, childListener.count);
		
		oj.run();
		
		assertEquals(1, childListener.count);
		
		ojSteps.checkNow();
		
		Stateful flag = (Stateful) new OddjobLookup(oj).lookup("flag");
		
		StateSteps flagSteps = new StateSteps(flag);
		flagSteps.startCheck(JobState.COMPLETE);

		flagSteps.checkNow();
		
		flagSteps.startCheck(JobState.COMPLETE, JobState.READY,
				JobState.DESTROYED);
		
		ojSteps.startCheck(ParentState.COMPLETE, ParentState.READY);
		
		oj.hardReset();
		
		flagSteps.checkNow();
		ojSteps.checkNow();
		
		ojSteps.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		oj.run();
		
		ojSteps.checkNow();
		
		ojSteps.startCheck(ParentState.COMPLETE, ParentState.READY);
		
		oj.hardReset();
		
		ojSteps.checkNow();
		
		ojSteps.startCheck(ParentState.READY, ParentState.EXECUTING,
				ParentState.COMPLETE);
		
		oj.run();
				
		ojSteps.checkNow();
		
		ojSteps.startCheck(ParentState.COMPLETE, ParentState.DESTROYED);
		
		oj.destroy();
	}
	
	/**
	 * Test reseting Oddjob
	 *
	 */
	public void testSoftReset() {
		class MyL implements StructuralListener {
			int count;
			public void childAdded(StructuralEvent event) {
				count++;
			}
			public void childRemoved(StructuralEvent event) {
				count--;
			}
		}

		String xml =
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:flag state='INCOMPLETE'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		
		MyL l = new MyL();
		oj.addStructuralListener(l);
		
		assertEquals(0, l.count);
		
		oj.run();
		
		assertEquals(1, l.count);
		assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());
		
		oj.softReset();
		
		assertEquals(1, l.count);
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
		
		oj.run();
		
		assertEquals(1, l.count);		
		assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());
		
		oj.softReset();
		
		assertEquals(1, l.count);
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
	
		oj.run();
		
		assertEquals(1, l.count);		
		assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());
		
		oj.destroy();
	}
	
	/**
	 * Test Oddjob can be rerun after a configuration
	 * failure using a soft reset.
	 *
	 */
	public void testSoftResetOnFailure() {

		String xml =
			"<oddjob>" +
			" <job>" +
			"  <idontexist/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
				
		oj.run();
		
		assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());
		
		String xml2 =
			"<oddjob xmlns:state='http://rgordon.co.uk/oddjob/state'>" +
			" <job>" +
			"  <state:flag state='COMPLETE'/>" +
			" </job>" +
			"</oddjob>";
		
		oj.setConfiguration(new XMLConfiguration("XML", xml2));
		oj.softReset();
		
		assertEquals(ParentState.READY, oj.lastStateEvent().getState());
		
		oj.run();
		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());		
		
		oj.destroy();
	}
	
	/**
	 * Test loading an Oddjob without a child.
	 * @throws ArooaParseException 
	 *
	 */
    public void testLoadNoChild() throws ArooaParseException {
        String config = "<oddjob id='this'/>";
        
        Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", config));
        test.run();
        Object root = new OddjobLookup(test).lookup("this");
        
        assertEquals(Oddjob.OddjobRoot.class, root.getClass());
        
        assertEquals(ParentState.READY, test.lastStateEvent().getState());
        
        test.hardReset();
        
        test.run();
        
        assertEquals(ParentState.READY, test.lastStateEvent().getState());
        
        test.destroy();
    }

    public void testLoadOddjobClassloader() {
        String config = 
        	"<oddjob>" +
        	" <job>" +
        	"  <oddjob id='nested'>" +
            "   <classLoader>" +
            "    <url-class-loader>" +
            "     <files>" +
            "      <files files='build/*.jar'/>" +
            "     </files>" +
            "    </url-class-loader>" +
            "   </classLoader>" +
            "  </oddjob>" +
            " </job>" +
            "</oddjob>";
                
        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", config));

        oj.load();
        
        String nestedConf = "<oddjob/>";
        Oddjob test = (Oddjob) new OddjobLookup(oj).lookup("nested");
		oj.setConfiguration(new XMLConfiguration("TEST", nestedConf));
        test.run();
        
        assertNotNull("Nested classloader", 
                test.getClassLoader());
        
        oj.destroy();
    }
    
    
    /**
     * Test a nested lookup.
     * @throws Exception
     */
    public void testLookup() throws Exception {

    	String config = 
    		"<oddjob id='this'>" +
    		" <job>" +
	    	"  <oddjob id='nested'>" +
	    	"   <configuration>" +
	    	"    <value value='${inner-config}'/>" +
	    	"   </configuration>" +
	    	"  </oddjob>" +
	    	" </job>" +
			"</oddjob>";

    	String nested = 
    		"<oddjob>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"   <fruit>" +
    		"    <value value='apple'/>" +
    		"   </fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>";
        
        Oddjob oj = new Oddjob();
        
		oj.setConfiguration(new XMLConfiguration("XML", config));
		
		XMLConfigurationType configType = new XMLConfigurationType();
		configType.setXml(nested);
		
        oj.setExport("inner-config", configType);
        oj.run();
        
        String fruit = new OddjobLookup(oj).lookup(
        		"nested/fruits.fruit", String.class); 
        
        assertEquals("apple", fruit);
        
        oj.destroy();
    }

    public void testArgs() throws Exception {
    	
    	String config = 
    		"<oddjob id='this'>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"	<fruit>" +
    		"    <value value='${this.args[0]}'/>" +
    		"	</fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>";
    	
        Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", config));
		
		// Test without arg.
		
        test.run();

        assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
        
        OddjobLookup lookup = new OddjobLookup(test);
        
        String fruit = lookup.lookup("fruits.fruit", String.class);
        assertEquals(null, fruit);
        
        test.hardReset();
        
        // And with arg.
        
        test.setArgs(new String[] { "apple" });
        
        test.run();
        
        fruit = lookup.lookup("fruits.fruit", String.class);
        assertEquals("apple", fruit);
        
        test.destroy();
    }    
    
	private class OurInputHandler implements InputHandler {
		
		@Override
		public Properties handleInput(InputRequest[] requests) {
			
			Properties properties = new Properties();
			
			properties.setProperty("our.file.name", "Fruit.txt");
			
			return properties;
		}
	}
	
    public void testOptionalArgumentExample() {
    
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/OptionalFileNameArg.xml",
    			getClass().getClassLoader()));
    	oddjob.setInputHandler(new OurInputHandler());
    	
    	ConsoleCapture console = new ConsoleCapture();
    	console.capture(Oddjob.CONSOLE);
    	
    	oddjob.run();

    	oddjob.hardReset();
    	oddjob.setArgs(new String[] { "Pizzas.txt" } );
    	
    	oddjob.run();
    	
    	console.close();
    	
    	console.dump(logger);
    	
    	String[] lines = console.getLines();
    	
    	assertEquals("File Name is Fruit.txt", lines[0].trim());    	
    	assertEquals("File Name is Pizzas.txt", lines[1].trim());
    	assertEquals(2, lines.length);
    	
    	oddjob.destroy();
    }
    
    public void testInheritedProperties() throws Exception {
    	
    	String config = 
    		"<oddjob>" +
    		" <job>" +
    		"  <sequential>" +
    		"   <jobs>" +
    		"    <properties>" +
    		"     <values>" +
    		"      <value key='fruit' value='apple'/>" +
    		"     </values>" +
    		"    </properties>" +
    		"    <oddjob id='nested'>" +
    		"     <configuration>" +
    		"      <arooa:configuration xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
    		"       <xml>" +
    		"        <xml>" +
    		"<oddjob>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"	<fruit>" +
    		"    <value value='${fruit}'/>" +
    		"	</fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>" + 
    		"        </xml>" +
    		"       </xml>" +
    		"      </arooa:configuration>" +
    		"     </configuration>" +
    		"    </oddjob>" +
    		"   </jobs>" +
    		"  </sequential>" +
    		" </job>" +
    		"</oddjob>";
        
    	    	
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
        oj.run();
        
        String fruit = new OddjobLookup(oj).lookup(
        		"nested/fruits.fruit", String.class);
        assertEquals("apple", fruit);
        
        oj.destroy();
    }    
    
    /**
     * Test Oddjob dir property.
     *
     */
    public void testDir() {
    	OurDirs dirs = new OurDirs();
    	File testFile = dirs.relative("work/xyz.xml"); 
    	
    	Oddjob oj = new Oddjob();
    	oj.setFile(testFile);
    	
    	assertEquals(dirs.relative("work"), oj.getDir());
    	
    	oj.destroy();
    }
        
    /**
     * Test Component Registry management. Test Oddjob 
     * adds and clears up it's children OK.
     * @throws ArooaParseException 
     * 
     */
    public void testRegistryManagement() throws ArooaParseException {
    	
    	final ArooaSession session = new OddjobSessionFactory().createSession();
    	
    	class OurContext extends MockArooaContext {
    		@Override
    		public ArooaSession getSession() {
    			return session;
    		}
    		
    		@Override
    		public RuntimeConfiguration getRuntime() {
    			return new MockRuntimeConfiguration() {
    				@Override
    				public void configure() throws ArooaException {
    				}
    			};
    		}
    	}
    	
    	Oddjob oj = new Oddjob();
    	oj.setArooaSession(session);
    	
    	ComponentPool pool = session.getComponentPool();
    	
    	pool.registerComponent(
    			new ComponentTrinity(
    				oj, oj, new OurContext())
    			, "oj");
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <echo id='echo'>Hello</echo>" +
    		" </job>" +
    		"</oddjob>";
    	
		oj.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	BeanDirectory dir = session.getBeanRegistry();
    	
    	assertEquals(null, dir.lookup("oj/echo"));

    	oj.run();
    	
    	assertNotNull(dir.lookup("oj/echo"));
    	
    	oj.hardReset();
    	
    	assertEquals(null, dir.lookup("oj/echo"));
    	
    	oj.run();
    	
    	assertNotNull(dir.lookup("oj/echo"));
    	
    	oj.destroy();
    }
    
    public void testCreateFile() throws FileNotFoundException {
    	
    	OurDirs dirs = new OurDirs();
    	
    	File testFile = dirs.relative("work/OddjobCreateFileTest.xml");
    	
    	EchoJob echo = new EchoJob();
    	echo.setOutput(new FileOutputStream(testFile));
    	echo.setText("<oddjob/>");
    	
    	echo.run();
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <oddjob file='" + testFile.getAbsolutePath() + "'/>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oddjob.run();
    	
    	assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
    	
    	testFile.delete();
    	
    	oddjob.hardReset();
    	
    	oddjob.run();
    	
    	assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
    	
    	assertTrue(testFile.exists());
    }
}

