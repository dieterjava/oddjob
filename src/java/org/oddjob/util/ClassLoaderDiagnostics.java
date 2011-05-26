/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.oddjob.util;

import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Display Diagnostics about the ClassLoader.
 * <pre>
 * &lt;class class='org.oddjob.util.ClassLoaderDiagnostics
 *     resource="/log4j.properties"
 *   &lt;classLoader&gt;
 *    &lt;contextClassLoader/&gt;
 *   &lt;/classLoader&gt;
 * </pre>
 * 
 * @author Rob, Based heavily on WhichResource from Ant.
 * 
 */
public class ClassLoaderDiagnostics implements Runnable {
	
	private static final Logger logger = Logger.getLogger(ClassLoaderDiagnostics.class);
	
	private String name;
	
    /**
     * class to look for
     */
    private String className;

    /**
     * resource to look for
     */
    private String resource;

    private ClassLoader classLoader;

    private String location;

	public void run() {
    	location = null;
		
    	ClassLoader classLoader = this.classLoader;
    	if (classLoader == null) {
    		classLoader = getClass().getClassLoader();
    	}
    	
    	logClassLoaderStack(classLoader);
    	
        if (className != null) {
            //convert a class name into a resource
            resource = className.replace('.', '/') + ".class";
        }

        if (resource == null) {
            return;
        }

        if (resource.startsWith("/")) {
            resource = resource.substring(1);
        }

        URL url = classLoader.getResource(resource);
        if (url != null) {
            location = url.toExternalForm();
            logger.info("Location of " + resource + " is " + location);
        } else {
            logger.info("Resource " + resource + " not found.");
        }
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResource() {
		return resource;
	}

    /**
     * Name the resource to look for.
     * 
     * @param resource the name of the resource to look for.
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getClassName() {
		return className;
	}

	public void setClassName(String classname) {
		this.className = classname;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public String getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}

	public static void logClassLoaderStack(ClassLoader loader) {
		if (logger.isInfoEnabled()) {
	    	logger.info("ClassLoader stack:");
	    	for (ClassLoader next = loader; next != null; next = next.getParent()) {
	    		logger.info("  " + next);
	    	}    			
		}
	}
}
