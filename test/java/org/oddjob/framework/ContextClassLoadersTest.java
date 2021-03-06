package org.oddjob.framework;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import org.oddjob.tools.OurDirs;
import org.oddjob.util.URLClassLoaderTypeTest;

public class ContextClassLoadersTest extends TestCase {

	public void testSimple() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		
		OurDirs dirs = new OurDirs();
		
		ClassLoader existing = Thread.currentThread().getContextClassLoader();
		
		File check = dirs.relative("test/classloader/AJob.class");
		if (!check.exists()) {
			URLClassLoaderTypeTest.compileSample(dirs);
		}
		
		URLClassLoader classLoader = new URLClassLoader(new URL[] {
				dirs.relative("test/classloader").toURI().toURL() });
		
		Object comp = Class.forName("AJob", true, classLoader).newInstance();
		
		ContextClassloaders.push(comp);
		
		assertEquals(classLoader, 
				Thread.currentThread().getContextClassLoader());
		
		ContextClassloaders.pop();
		
		assertEquals(existing, Thread.currentThread().getContextClassLoader());
	}
}
