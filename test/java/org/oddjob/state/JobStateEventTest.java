/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.MockStateful;
import org.oddjob.OddjobException;

/**
 * 
 */
public class JobStateEventTest extends TestCase {
	private static final Logger logger = Logger.getLogger(JobStateEvent.class);

	String message = "This should serialize.";
	
	class NotSerializable {
		
	}
	
	class OurStateful extends MockStateful {
		
		public void addJobStateListener(JobStateListener listener) {
			throw new RuntimeException("Unexpected.");
		}
		
		public void removeJobStateListener(JobStateListener listener) {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	class NotSerializableException extends Exception {
		private static final long serialVersionUID = 2009021000L; 
			
		public NotSerializableException() {
			super(message);
		}
		NotSerializable ns = new NotSerializable();
	}
	
	public void testSerialize1() throws IOException, ClassNotFoundException {
		OurStateful source = new OurStateful();
		JobStateEvent event = new JobStateEvent(source, 
				JobState.EXCEPTION, new Date(1234), new OddjobException(message));

		JobStateEvent event2 = (JobStateEvent) outAndBack(event);
		logger.debug(event2);
		// source is transient in eventObject.
		assertNull(event2.getSource());
		assertEquals(JobState.EXCEPTION, event2.getJobState());
		assertEquals(1234, event2.getTime().getTime());
		assertEquals(message, event2.getException().getMessage());
	}
	
	public void testSerialize2() throws IOException, ClassNotFoundException {
		OurStateful source = new OurStateful();
		JobStateEvent event = new JobStateEvent(source, 
				JobState.EXCEPTION, new Date(1234), new NotSerializableException());

		JobStateEvent event2 = (JobStateEvent) outAndBack(event);
		logger.debug(event2);
		logger.debug("Exception:", event2.getException());
		// source is transient in eventObject.
		assertNull(event2.getSource());
		assertEquals(JobState.EXCEPTION, event2.getJobState());
		assertEquals(1234, event2.getTime().getTime());
		assertEquals(JobStateEvent.REPLACEMENT_EXCEPTION_TEXT + message, event2.getException().getMessage());
	}
	
	public void testSerializeComplete() throws IOException, ClassNotFoundException {
		OurStateful source = new OurStateful();
		JobStateEvent event = new JobStateEvent(source, 
				JobState.COMPLETE, new Date(1234), null);

		JobStateEvent event2 = (JobStateEvent) outAndBack(event);
		logger.debug(event2);
		// source is transient in eventObject.
		assertNull(event2.getSource());
		assertEquals(JobState.COMPLETE, event2.getJobState());
		assertEquals(1234, event2.getTime().getTime());
		assertEquals(null, event2.getException());
	}
	
	
	Object outAndBack(Object object) throws IOException, ClassNotFoundException {
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutput oo = new ObjectOutputStream(os);
		oo.writeObject(object);
		oo.close();

		ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
		ObjectInput oi = new ObjectInputStream(in);
		
		Object o = oi.readObject();
		return o;
	}
}
