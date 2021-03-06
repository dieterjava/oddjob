/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaProperty;
import org.oddjob.framework.WrapDynaClass;

public class WrapDynaClassTest extends TestCase {

	public class Bean {
		public String getFruit() { return "apples"; }
	}
	
	public void testSerialize() throws IOException, ClassNotFoundException {
		
		WrapDynaClass dc = WrapDynaClass.createDynaClass(Bean.class);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(out);
		
		oos.writeObject(dc);
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(in);
		
		WrapDynaClass dc2 = (WrapDynaClass) ois.readObject();
		
		Map<String, Class<?>> props = 
			new HashMap<String, Class<?>>();
		DynaProperty[] dps = dc2.getDynaProperties();
		for (int i = 0; i < dps.length; ++i) {
			props.put(dps[i].getName(), dps[i].getType());
		}
		assertEquals(String.class, props.get("fruit"));
	}
}
