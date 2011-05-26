package org.oddjob.state;



/**
 * Implementors of this interface are able to listen to state events.
 * 
 * @author Rob Gordon
 */

public interface JobStateListener {


	/**
	 * Triggered when the job state changes.
	 * 
	 * @param event The job state event.
	 */	
	public void jobStateChange(JobStateEvent event);
	
}
