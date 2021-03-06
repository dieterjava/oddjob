package org.oddjob.sql;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.destinations.BeanSheetTest.Fruit;
import org.oddjob.io.BufferType;
import org.oddjob.io.CopyJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

public class SQLResultsSheetTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SQLResultsSheetTest.class);
	
	String EOL = System.getProperty("line.separator");
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-----------------------------  " + getName() + "  ----------------");
	}
		
	public void testNoHeaders() throws BusCrashException {
		
		SQLResultsSheet test = new SQLResultsSheet();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Object[] values = createFruit(); 
		
		test.setOutput(out);
		test.setDataOnly(true);
		test.setArooaSession(new StandardArooaSession());
		
		BasicBeanBus<Object> bus = new BasicBeanBus<Object>();
		
		test.setBusConductor(bus.getBusConductor());
		
		bus.startBus();
		test.writeBeans(Arrays.asList(values));
		
		bus.stopBus();
		
		String expected = 
			"Red and Green  7.6    Apple   Cox" + EOL +
			"Orange         9.245  Orange  Jaffa" + EOL;
		
		assertEquals(expected, out.toString());
		
		// Check it can be re-run.
		
		out = new ByteArrayOutputStream();
		test.setOutput(out);
		
		bus.startBus();
		
		test.writeBeans(Arrays.asList(values));
		
		bus.stopBus();
		
		assertEquals(expected, out.toString());
	}
	
	private Object[] createFruit() {
		
		Fruit fruit1 = new Fruit();
		fruit1.setType("Apple");
		fruit1.setVariety("Cox");
		fruit1.setColour("Red and Green");
		fruit1.setSize(7.6);
		
		Fruit fruit2 = new Fruit();
		fruit2.setType("Orange");
		fruit2.setVariety("Jaffa");
		fruit2.setColour("Orange");
		fruit2.setSize(9.245);

		return new Object[] { fruit1, fruit2 };
	}
	
	public void testExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLResultsSheetExample.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
		
		BufferType buffer = new BufferType();
		buffer.configured();
		
		CopyJob copy = new CopyJob();
		copy.setInput(getClass().getResourceAsStream("SQLResultsSheetExample.txt"));
		copy.setOutput(buffer.toOutputStream());
		copy.run();
		
		String[] expected = buffer.getLines();
		
		String[] lines = console.getLines();
		
		assertEquals(expected.length, lines.length);
		
		for (int i = 0; i < expected.length; ++i) {
			assertTrue(expected[i] + " regexp does not match " + lines[i], 
					lines[i].trim().matches(expected[i].trim()));	
		}
		
		oddjob.destroy();
		
	}
}
