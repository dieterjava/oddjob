package org.oddjob.jmx.client;

import java.util.LinkedList;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * Synchronises asynchronous notifications with a synchronous class to
 * get initial state.
 * <p>
 * During the synchronisation phase any asynchronous events are queued and
 * processed after synchronisation. Duplicates are detected by the
 * notification number and removed.
 * 
 * @author rob
 *
 */
public class Synchronizer implements NotificationListener {

	private final NotificationListener listener;
	
	private LinkedList<Notification> pending = new LinkedList<Notification>();
	
	public Synchronizer(NotificationListener listener) {
		this.listener = listener;
	}
	
	public void handleNotification(Notification notification, Object handback) {
		synchronized(this) {
			if (pending != null) {
				pending.addLast(notification);
				return;
			}
		}
		listener.handleNotification(notification, null);
	}

	/**
	 * Synchronous synchronisation with notifications.
	 * 
	 * @param last The last notifications.
	 */
	public void synchronize(Notification[] last) {
		long seq = 0;
		for (Notification notification : last) {
			listener.handleNotification(notification, null);
			seq = notification.getSequenceNumber();
		}
		
		while (true) {
			Notification notification = null;
			synchronized (this) {
				if (pending.isEmpty()) {
					pending = null;
					return;
				}
				notification = pending.removeFirst();
				if (notification.getSequenceNumber() < seq) {
					continue;
				}
			}
			listener.handleNotification(notification, null);
		}
	}	
}
