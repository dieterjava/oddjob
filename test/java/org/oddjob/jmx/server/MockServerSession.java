package org.oddjob.jmx.server;

import javax.management.ObjectName;

public class MockServerSession implements ServerSession {

	public ObjectName nameFor(Object object) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public Object objectFor(ObjectName objectName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public ObjectName createMBeanFor(Object child, ServerContext childContext) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
	
	public void destroy(ObjectName childName) {
		throw new RuntimeException("Unexpected from " + getClass());
	}
}
