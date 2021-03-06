/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.client;

import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.oddjob.framework.Transportable;
import org.oddjob.jmx.ObjectNames;

/**
 * This object represents a component as it travels across the network between
 * client and server.
 *
 * @author Rob Gordon.
 */
public class ComponentTransportable implements Transportable {
	private static final long serialVersionUID=20051116;
	
	private static final Logger logger = Logger.getLogger(ComponentTransportable.class);
	
	/** The address which identify this component. */
	private ObjectName name;
	
	public ComponentTransportable(ObjectName name) {
		this.name = name;
	}
		
	public Object importResolve(ObjectNames names) {
		Object resolved = names.objectFor(name);
		logger.debug("Resolved [" + resolved + "] from addresses [" + name + "]");
		return resolved;
	}
	
	public String toString() {
		return "ComponentTransportable: " + name;
	}
}
