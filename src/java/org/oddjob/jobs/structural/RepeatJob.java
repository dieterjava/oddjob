/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.jobs.structural;

import java.util.Iterator;

import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaElement;
import org.oddjob.framework.StructuralJob;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.state.AnyActiveStateOp;
import org.oddjob.state.IsStoppable;
import org.oddjob.state.State;
import org.oddjob.state.StateOperator;
import org.oddjob.structural.OddjobChildException;
import org.oddjob.values.types.SequenceIterable;

/**
 * @oddjob.description This job will repeatedly run its child job either for:
 * <ul>
 * 	<li>Each value of a collection.</li>
 *  <li>Or a given number times.</li>
 *  <li>Or until the until property is true.</li>
 * </ul> 
 * <p>
 * Without either a until or a times or values the job will loop indefinitely.
 * 
 * @oddjob.example
 * 
 * Repeat a job 3 times.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/structural/RepeatExample.xml}
 * 
 * @author Rob Gordon.
 * 
 */
public class RepeatJob extends StructuralJob<Runnable>
implements Stoppable {
	private static final long serialVersionUID = 20120121;
	
    /**
     * @oddjob.property 
     * @oddjob.description Repeat will repeat until the value of
     * this property is true.
     * @oddjob.required No.
     */
    private volatile boolean until;
    
    /**
     * @oddjob.property 
     * @oddjob.description The count of repeats.
     * @oddjob.required Read Only.
     */
	private volatile int count;
	
	/**
     * @oddjob.property 
     * @oddjob.description The number of times to repeat.
     * @oddjob.required No.
	 */
	private volatile int times;
    
	private transient volatile Iterable<?> values;
	
	private transient volatile Iterator<?> iterator;
	
	private transient volatile Object current;
	
	@Override
	protected StateOperator getInitialStateOp() {
		return new AnyActiveStateOp();
	}
	
	/** 
	 * @oddjob.property job
	 * @oddjob.description The job who's execution 
	 * to schedule. 
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public void setJob(Runnable child) {
		if (child == null) {
			childHelper.removeChildAt(0);
		}
		else {
			if (childHelper.size() > 0) {
				throw new IllegalArgumentException("Child Job already set.");
			}
			childHelper.insertChild(0, child);
		}
	}
	

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jobs.AbstractJob#execute()
     */
	protected void execute() {
		
		Runnable job = childHelper.getChild(); 
		if (job == null) {
			return;
		}
		
		
		if (iterator == null) {
			if (times > 0) {
				iterator = new SequenceIterable(1, times, 1).iterator();
			}
			else {
				if (values == null) {
					iterator = null;
				}
				else {
					iterator = values.iterator();
				}
			}
		}
		
		while (!stop && !until && (iterator == null || iterator.hasNext())) {

			++count;
		    if (iterator != null) {
		    	current = iterator.next();
		    }
			
		    ResetActions.AUTO.doWith(job);
	        
			try {
				job.run();
			}
			finally {
			}
			
			State state = null;
			Throwable throwable = null;
			if (job instanceof Stateful) {
				state = ((Stateful) job).lastStateEvent().getState();
				throwable = ((Stateful) job).lastStateEvent().getException();
			}
			
			if (state == null) {
				continue;
			}
			
			if (state.isException()) {
				logger().debug("Job [" + job + "] Exception");
				throw new OddjobChildException(throwable, job.toString());			
			}
			else if (new IsStoppable().test(state)) {
				logger().debug("Job state for [" + job + 
						"] is: " + state + ", Will not repeat.");
				break;
			}
		} // end while
		
		stop = false;
	}

	@Override
	protected void onHardReset() {
		iterator = null;
		count = 0;
		until = false;
	}
	
	public void setValues(Iterable<?> values) {
		this.values = values;
	}
	
	public Iterable<?> getValues() {
		return values;
	}
	
	public boolean isUntil() {
		return until;
	}

	@ArooaElement
	public void setUntil(boolean until) {
		this.until = until;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public int getCount() {
		return count;
	}

	/**
	 * @oddjob.property index
	 * @oddjob.description The same as count. Provided so configurations
	 * can be swapped between this and {@link ForEachJob} job.
	 * 
	 * @return The index.
	 */
	public int getIndex() {
		return count;
	}
	
	public Object getCurrent() {
		return current;
	}
}
