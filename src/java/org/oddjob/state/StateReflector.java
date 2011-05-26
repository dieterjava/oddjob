package org.oddjob.state;

import org.oddjob.FailedToStopException;
import org.oddjob.framework.SimultaneousStructural;

abstract public class StateReflector extends SimultaneousStructural {
	private static final long serialVersionUID = 20010082000L;
	
	public void stop() throws FailedToStopException {
		
		if (!childStateReflector.isRunning()) {
			return;
		}
		
		logger().info("[" + this + "] Stop requested.");
		
		
		childHelper.stopChildren();
		
		logger().info("[" + this + "] Message sent to stop children.");
	}
}
