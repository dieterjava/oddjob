package org.oddjob.beanbus.mega;

import org.oddjob.beanbus.BusConductor;

/**
 * Something that can be part of a bus.
 * 
 * @see MegaBeanBus
 * 
 * @author rob
 *
 */
public interface BusPart {

	/**
	 * Prepare this part of a bus. Intended to allow components
	 * to configure themselves.
	 * 
	 * @param busConductor The bus conductor. Will never be null.
	 */
	public void prepare(BusConductor busConductor);
	
	/**
	 * A Bodge method to allow a Bus Conductor to be adpator for logging.
	 * 
	 * @param busConductor
	 * @return
	 */
	public BusConductor conductorForService(BusConductor busConductor);	
	
}
