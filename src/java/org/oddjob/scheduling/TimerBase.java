package org.oddjob.scheduling;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.images.IconHelper;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.ParentState;
import org.oddjob.state.State;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.util.Clock;
import org.oddjob.util.DefaultClock;

/**
 * Common functionality for Timers.
 * 
 * @author rob
 *
 */
abstract public class TimerBase extends ScheduleBase {

	private static final long serialVersionUID = 2009091400L; 
	
	/**
	 * @oddjob.property schedule
	 * @oddjob.description The Schedule used to provide execution 
	 * times.
	 * @oddjob.required Yes.
	 */
	private transient Schedule schedule;
		
	/** 
	 * @oddjob.property
	 * @oddjob.description The time zone the schedule is to run
	 * in. This is the text id of the time zone, such as "Europe/London".
	 * More information can be found at
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/TimeZone.html">
     * TimeZone</a>.
	 * @oddjob.required Set automatically.  
	 */
	private transient TimeZone timeZone;

	/**
	 * @oddjob.property 
	 * @oddjob.description The clock to use. Tells the current time.
	 * @oddjob.required Set automatically.
	 */ 
	private transient Clock clock;
	
	/** The currently scheduled job future. */
	private transient volatile Future<?> future;
	
	/** The scheduler to schedule on. */
	private transient ScheduledExecutorService scheduler;

	/** Provided to the schedule. */
	protected final Map<Object, Object> contextData = 
			Collections.synchronizedMap(new HashMap<Object, Object>());
	
	/**
	 * @oddjob.property 
	 * @oddjob.description The time the next execution is due. This property
	 * is updated when the timer starts or after each execution.
	 * @oddjob.required Read Only.
	 */
	private volatile transient Date nextDue;
	
	/**
	 * @oddjob.property 
	 * @oddjob.description This is the current/next interval from the
	 * schedule.
	 * @oddjob.required Set automatically.
	 */ 
	private volatile ScheduleResult current;
	
	@ArooaHidden
	@Inject
	public void setScheduleExecutorService(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	protected void begin() throws ComponentPersistException {
		if (schedule == null) {
			throw new NullPointerException("No Schedule.");
		}
		
		if (scheduler == null) {
			throw new NullPointerException("No Scheduler.");
		}
		
		if (clock == null) {
			clock = new DefaultClock();
		}
	}
	
	protected void onStop() {
		super.onStop();
		
		Future<?> future = this.future;
		if (future != null) {
			future.cancel(false);
			future = null;
		}	
	}
	
	@Override
	protected void postStop() {
		stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				getStateChanger().setState(ParentState.READY);
			}
		});
	}
	
	protected void onReset() {
		contextData.clear();
		nextDue = null;
		current = null;
	}
	

	/**
	 * Get the time zone id to use in this schedule.
	 * 
	 * @return The time zone id being used.
	 */
	public String getTimeZone() {
		if (timeZone == null) {
			return null;
		}
		return timeZone.getID();
	}

	/**
	 * Set the time zone.
	 * 
	 * @param timeZoneId the timeZoneId.
 	 */
	public void setTimeZone(String timeZoneId) {
		if (timeZoneId == null) {
			this.timeZone = null; 
		} else {
			this.timeZone = TimeZone.getTimeZone(timeZoneId);
		}
	}
		
	/**
	 * Set the schedule.
	 * 
	 * @param schedule The schedule.
	 */
	public void setSchedule(Schedule schedule) {
	    this.schedule = schedule;
	}

	public Schedule getSchedule() {
		return schedule;
	}
	
	/**
	 * @throws ComponentPersistException 
	 * @oddjob.property reschedule 
	 * @oddjob.description Reschedule from the given date/time.
	 * @oddjob.required Only available when running.
	 */ 
	@ArooaHidden
	public void setReschedule(Date reSchedule) throws ComponentPersistException {
		if (future != null) {
			future.cancel(true);
			future = null;
		}
		
		scheduleFrom(reSchedule);
	}
	
	protected void scheduleFrom(Date date) throws ComponentPersistException {
	    logger().debug("Scheduling from [" + date + "]");

	    if (date == null) {
	    	setNextDue(null);
	    }
	    else {
		    ScheduleContext context = new ScheduleContext(
		    		date, timeZone, contextData, getLimits());
	
		    current = schedule.nextDue(context);
		    if (current == null) {
		    	setNextDue(null);
		    }
		    else {
		    	setNextDue(current.getFromDate());
		    }
	    }
	}

	/**
	 * Get the current clock.
	 * 
	 * @return The clock
	 */
	public Clock getClock() {
		if (clock == null) {
			clock = new DefaultClock();
		}
		return clock;
	}

	/**
	 * Set the clock. Only useful for testing.
	 * 
	 * @param clock The clock.
	 */
	public void setClock(Clock clock) {
		this.clock = clock;
	}
		
	/**
	 * Get the next due date.
	 * 
	 * @return The next due date
	 */
	public Date getNextDue() {		
		return nextDue;
	}

	/**
	 * Set the next due date.
	 * 
	 * @param nextDue The date schedule is next due.
	 * @throws ComponentPersistException 
	 */
	protected void setNextDue(Date nextDue) throws ComponentPersistException {
		
		Date oldNextDue = this.nextDue;
		this.nextDue = nextDue;	
		firePropertyChange("nextDue", oldNextDue, nextDue);
		
		if (nextDue == null) {
			logger().info("Schedule finished.");
			childStateReflector.start();
			return;
		}
		
		iconHelper.changeIcon(IconHelper.SLEEPING);
		
		// save the last complete.
		save();
		
		long delay = nextDue.getTime() - getClock().getDate().getTime();
		if (delay < 0) {
			delay = 0;
		}
		
	    future = scheduler.schedule(
	    		new Execution(), delay, TimeUnit.MILLISECONDS);
	    
		logger().info("Next due at " + nextDue +
				" in " + DateHelper.formatMilliseconds(delay) + ".");
	}

	/**
	 * Get the current/next interval.
	 * 
	 * @return The interval, null if not due again. 
	 */
	public ScheduleResult getCurrent() {
		return current;
	}

	/**
	 * @oddjob.property job
	 * @oddjob.description The job to run when it's due.
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public synchronized void setJob(Runnable job) {
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			childHelper.insertChild(0, job);
		}
	}
	
	
	abstract protected Interval getLimits();
	
	abstract protected void rescheduleOn(State state) 
	throws ComponentPersistException;

	abstract protected void reset(Resetable job);
	
	/**
	 * Listen for changed child job states. Not these could come in on
	 * a different thread to that which launched the Executor.
	 *
	 */
	class RescheduleStateListener implements StateListener {
		
		private final Thread executionThread;
		
		private State state;
		
		RescheduleStateListener(Thread executionThread) {
			this.executionThread = executionThread;
		}
		
		synchronized void changeToActive() {
			if (state.isStoppable()) {
				iconHelper.changeIcon(IconHelper.ACTIVE);
			}
		}
		
		@Override
		public void jobStateChange(StateEvent event) {
			state = event.getState();
			
			if (stop) {
			    event.getSource().removeStateListener(this);
				return;
			}
			
			if (state.isReady()) {
				return;
			}
			if (state.isStoppable()) {
				iconHelper.changeIcon(IconHelper.EXECUTING);
				return;
			}
			
			// Get this far and it's a completion state.
			
			synchronized (this) {
				// If the child job was executing asynchronously
				// we need to ensure the ACTIVE icon has been broadcast
				// so that icon progression is always predictable.
				if (Thread.currentThread() != executionThread) {
					iconHelper.changeIcon(IconHelper.ACTIVE);
				}
			}


			// Order is important! Must remove this before scheduling again.
		    event.getSource().removeStateListener(this);
		    
			logger().debug("Rescheduling based on state [" + state + "]");
			
			try {
				rescheduleOn(state);
			} catch (final ComponentPersistException e) {
				stateHandler().waitToWhen(new IsAnyState(), 
						new Runnable() {
							@Override
							public void run() {
								getStateChanger().setStateException(e);
							}
						});
			}
			
		}
	}
	
	/**
	 */
	class Execution implements Runnable {
		
		public void run() {
			
			ComponentBoundry.push(loggerName(), this);
			try {
				Runnable job = childHelper.getChild();

				if (stop) {
					logger().info("Not Executing [" + job + "] + as we have now stopped.");
					return;
				}

				logger().info("Executing [" + job + "] due at " + nextDue);

				if (job != null) {

					try {
						RescheduleStateListener rescheduleListner = 
							new RescheduleStateListener(Thread.currentThread());

						if (job instanceof Resetable) {
							reset((Resetable) job);
						}

						if (job instanceof Stateful) {

							((Stateful) job).addStateListener(rescheduleListner);
						}

						job.run();

						rescheduleListner.changeToActive();

						logger().info("Finished running [" + 
								job + "]");
					}
					catch (final Exception t) {
						logger().error("Failed running scheduled job.", t);
						stateHandler().waitToWhen(new IsAnyState(), new Runnable() {
							public void run() {
								getStateChanger().setStateException(t);
							}
						});
					}
				}
				else {
					logger().warn("Nothing to run.");
				}
			} finally {
				ComponentBoundry.pop();
			}
		}
		
		@Override
		public String toString() {
			return TimerBase.this.toString();
		}
	}
		
}
