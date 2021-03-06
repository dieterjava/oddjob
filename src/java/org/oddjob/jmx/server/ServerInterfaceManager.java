/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.server;

import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.oddjob.jmx.client.ClientHandlerResolver;

/**
 * An InterfaceManager collects together the interfaces to be exposed by 
 * a component. 
 *
 */
public interface ServerInterfaceManager {

	/**
	 * Return the interface supported.
	 * 
	 * @return The interfaces.
	 */
	public ClientHandlerResolver<?>[] allClientInfo();

	/**
	 * Get the MBeanInfo based on all the interfaces.
	 * 
	 * @return
	 */
	public MBeanInfo getMBeanInfo();

	/**
	 * Invoke a method using the arguments as received by an MBean.
	 * 
	 * @param actionName The action (method) name.
	 * @param params An array of object that are the parameters.
	 * @param signature An array of Strings that are class names.
	 * 
	 * @return The result of the method call.
	 * 
	 * @throws MBeanException
	 * @throws ReflectionException
	 */
	public Object invoke(String actionName, Object[] params, String[] signature)
	throws MBeanException, ReflectionException;

	/**
	 * Called when an MBean is being destroyed. Used to InterfaceHandlers
	 * a chance to remove listeners.
	 */
	public void destroy(); 
	
	
}
