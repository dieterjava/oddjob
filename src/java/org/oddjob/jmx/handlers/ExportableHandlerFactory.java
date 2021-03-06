package org.oddjob.jmx.handlers;

import java.lang.reflect.Proxy;

import org.oddjob.framework.Exportable;
import org.oddjob.framework.Transportable;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;

/**
 * Provide Handlers for the {@link org.oddjob.framework.Exportable} interface.
 * <p>
 * This is a special handler because Exportable is a
 * fake client side interface.
 * 
 * @author rob
 */
public class ExportableHandlerFactory 
implements ClientInterfaceHandlerFactory<Exportable> {
	
	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.jmx.server.ServerInterfaceHandlerFactory#interfaceClass()
	 */
	public Class<Exportable> interfaceClass() {
		return Exportable.class;
	}
	
	public HandlerVersion getVersion() {
		return VERSION;
	}
	
	public Exportable createClientHandler(Exportable proxy, ClientSideToolkit toolkit) {
		return new ClientExportableHandler(proxy);
	}
	
	class ClientExportableHandler implements Exportable {

		private final Exportable invocationHandler;
		
		ClientExportableHandler(Exportable proxy) {			
			invocationHandler = (Exportable) Proxy.getInvocationHandler(proxy);
		}

		public Transportable exportTransportable() {
			return invocationHandler.exportTransportable();
		}
		
	}
	
}