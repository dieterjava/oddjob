package org.oddjob.oddballs;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.deploy.LinkedDescriptor;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OurDirs;

public class OddballsDescriptorFactoryTest extends TestCase {
	private static final Logger logger = Logger.getLogger(
			OddballsDescriptorFactoryTest.class);
	
	public void testOddballs() throws ArooaParseException {

		new BuildOddballs().run();
		
		OurDirs dirs = new OurDirs();
		
		OddballsDirDescriptorFactory test = new OddballsDirDescriptorFactory();
		test.setOddballFactory(new DirectoryOddball());
		test.setBaseDir(new File(dirs.base(), "test/oddballs"));
				
		Oddjob oddjob = new Oddjob();
		
		oddjob.setDescriptorFactory(test);
		
		oddjob.setFile(new File(dirs.base(), 
				"test/launch/oddballs-launch.xml"));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}

	
	public void testOddballsExample() throws ArooaParseException {

		new BuildOddballs().run();
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(
				new XMLConfiguration("org/oddjob/oddballs/OddballsExample.xml",
				getClass().getClassLoader()));
				
		oddjob.setArgs(new String[] { dirs.base().getAbsolutePath() } );
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		oddjob.destroy();
	}
	
	public void testClassResolverResources() {
		
		new BuildOddballs().run();
		
		OurDirs dirs = new OurDirs();
		
		OddballsDirDescriptorFactory test = new OddballsDirDescriptorFactory();
		test.setOddballFactory(new DirectoryOddball());
		test.setBaseDir(new File(dirs.base(), "test/oddballs"));
				
		ArooaDescriptor descriptor = test.createDescriptor(
				getClass().getClassLoader());
		
		URL[] urls = descriptor.getClassResolver().getResources("META-INF/arooa.xml");
		
		assertEquals(3, urls.length);
		
		assertTrue(urls[1].toExternalForm().contains("orange"));
		assertTrue(urls[2].toExternalForm().contains("apple"));
	}
	
	public void testClassResolverClassLoaders() {
		
		new BuildOddballs().run();
		
		OurDirs dirs = new OurDirs();
		
		OddballsDirDescriptorFactory test = new OddballsDirDescriptorFactory();
		test.setOddballFactory(new DirectoryOddball());
		test.setBaseDir(new File(dirs.base(), "test/oddballs"));
				
		ArooaDescriptor descriptor = test.createDescriptor(
				getClass().getClassLoader());
		
		ArooaDescriptor mainDescriptor = 
			new OddjobDescriptorFactory(
					).createDescriptor(getClass().getClassLoader());
    	
		ArooaDescriptor oddjobDescriptor = new LinkedDescriptor(
				mainDescriptor,
				new StandardArooaDescriptor());
		
    	descriptor = new LinkedDescriptor(descriptor, 
					oddjobDescriptor);

    	ClassLoader[] classLoaders = 
    		descriptor.getClassResolver().getClassLoaders();
		
		assertEquals(3, classLoaders.length);
		
		logger.info("ClassLoader 0: " + 
				classLoaders[1].toString());
		logger.info("ClassLoader 1: " + 
				classLoaders[1].toString());
		logger.info("ClassLoader 2: " + 
				classLoaders[2].toString());
		
		assertTrue(classLoaders[0].toString().contains("orange"));
		assertTrue(classLoaders[1].toString().contains("apple"));
	}
	
	public void testNoOddballs() {
		
		OddballsDirDescriptorFactory test = new OddballsDirDescriptorFactory();
		test.setBaseDir(new File("noballs"));

		ArooaDescriptor descriptor= test.createDescriptor(
				getClass().getClassLoader());
		
		assertNull(descriptor);
	}
}
