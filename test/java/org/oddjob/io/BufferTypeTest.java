/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.ConverterHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.Resetable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

/**
 * Test for BufferType.
 */
public class BufferTypeTest extends TestCase {
	private static final Logger logger = Logger.getLogger(BufferTypeTest.class);
	
	public void testConversions() 
	throws NoConversionAvailableException, ConversionFailedException, IOException {
		
		ArooaConverter converter = new ConverterHelper().getConverter();
		
		BufferType test = new BufferType();
		test.setText("Hello");
		test.configured();
		
		String string = converter.convert(test, String.class);
		
		assertEquals("Hello", string);
	}
	
	/**
	 * Test writing to a buffer and then reading from it
	 * as a String and using an InputStream.
	 * 
	 * @throws Exception
	 */
	public void testOutputInputStream() throws Exception {
		BufferType bt = new BufferType();
		bt.configured();
		
		ArooaConverter converter = new ConverterHelper().getConverter();
		
		OutputStream os = converter.convert(bt, OutputStream.class);

		os.write('A');
		os.close();
		
		assertEquals("A", converter.convert(bt, String.class));
		
		InputStream is = converter.convert(bt, InputStream.class);
		int i = is.read();
		assertEquals(65, i);
		is.close();
	}
	
	/**
	 * Test appending to a buffer.
	 * 
	 * @throws Exception
	 */
	public void testAppend() throws Exception {
		BufferType bt = new BufferType();
		bt.configured();
		
		ArooaConverter converter = new ConverterHelper().getConverter();
		
		OutputStream os = converter.convert(bt, OutputStream.class);
		
		PrintStream out = new PrintStream(os);
		out.print("Hello World");
		out.close();
		
		os = converter.convert(bt, OutputStream.class);
		
		out = new PrintStream(os);
		out.print(" and Goodbye.");
		out.close();
		
		String result = converter.convert(bt, String.class);
		
		assertEquals("Hello World and Goodbye.", result);
		
	}
	
	public void testBufferAppendExample() {
		
		Oddjob oddjob = new Oddjob(); 
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/BufferAppendExample.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		Object jobs = new OddjobLookup(oddjob).lookup("jobs");
		
		((Resetable) jobs).hardReset();
		((Runnable) jobs).run();
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(6, lines.length);
		
		assertEquals("apples", lines[0].trim());
		assertEquals("oranges", lines[1].trim());
		assertEquals("", lines[2].trim());
		assertEquals("apples", lines[3].trim());
		assertEquals("oranges", lines[4].trim());
		assertEquals("", lines[5].trim());
		
		oddjob.destroy();		
	}
	
	public void testFileWritingAndCaptureExamples() {
				
		OurDirs dirs = new OurDirs();
		
		Oddjob oddjob1 = new Oddjob(); 
		oddjob1.setArgs(new String[] { dirs.base().toString() });
		oddjob1.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/BufferToFileExample.xml",
				getClass().getClassLoader()));
		
		oddjob1.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob1.lastJobStateEvent().getJobState());
		
		oddjob1.destroy();
		
		Oddjob oddjob2 = new Oddjob(); 
		oddjob2.setArgs(new String[] { dirs.base().toString() });
		oddjob2.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/BufferFileCaptureExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob2.run();
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(3, lines.length);
		
		assertEquals("apples", lines[0].trim());
		assertEquals("oranges", lines[1].trim());
		assertEquals("", lines[2].trim());
		
		oddjob2.destroy();
	}
	
	public void testByIdInOddjob() throws ArooaPropertyException, ArooaConversionException {
		
		String xml =  
			"<oddjob id='this'>" +
			" <job>" +
			"    <copy>" +
			"     <input>" +
			"      <value value='Some Text'/>" +
			"     </input>" +
			"     <output>" +
			"      <buffer id='result'/>" +
			"     </output>" +
			"     </copy>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob(); 
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		String result =  new OddjobLookup(oj).lookup("result", String.class);

		assertEquals("Some Text", result);
		
		oj.destroy();
	}
	
	public void testTextToOutputStreamInOddjob() throws ArooaPropertyException, ArooaConversionException {
		
		String xml =  
			"<oddjob id='this'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <variables id='v'>" +
			"     <buff>" +
			"      <buffer>Some Text</buffer>" +
			"     </buff>" +
			"    </variables>" +
			"    <copy>" +
			"     <input>" +
			"      <value value='${v.buff}'/>" +
			"     </input>" +
			"     <output>" +
			"      <buffer id='result'/>" +
			"     </output>" +
			"    </copy>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob(); 
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		String result =  new OddjobLookup(oj).lookup("result", String.class);

		assertEquals("Some Text", result);
		
		oj.destroy();
	}
		
	public void testToStringArray() throws IOException {
		
		BufferType test = new BufferType();
		test.setText(String.format("Apple%nOrange%n"));
		test.configured();
		
		String[] result = test.getLines();
		
		assertEquals("Apple", result[0]);
		assertEquals("Orange", result[1]);
		
	}
	
	public void testBufferAsLinesExample() throws ArooaPropertyException, ArooaConversionException {
		
		OurDirs dirs = new OurDirs();
		
		Oddjob oj = new Oddjob(); 
		oj.setArgs(new String[] { dirs.base().toString() });
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/BufferAsLinesExample.xml", 
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oj.run();
		
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(2, lines.length);
		
		assertEquals("Line 0 is apples.", lines[0].trim());
		assertEquals("Line 1 is oranges.", lines[1].trim());
		
		oj.destroy();
	}
	
	public void testLinesInOddjob() throws ArooaPropertyException, ArooaConversionException {
		
		String xml =  
			"<oddjob id='this'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <copy>" +
			"     <input>" +
			"      <buffer>" +
			"       <lines>" +
			"        <list>" +
			"         <values>" +
			"          <value value='Apple'/>" +
			"          <value value='Orange'/>" +
			"         </values>" +
			"        </list>" +
			"       </lines>" +
			"      </buffer>" +
			"     </input>" +
			"     <output>" +
			"      <buffer id='result'/>" +
			"     </output>" +
			"    </copy>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob(); 
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		String result =  new OddjobLookup(oj).lookup("result", String.class);

		assertEquals(String.format("Apple%nOrange%n"), result);
	}
	
	public void testCapturingXML() throws ArooaPropertyException, ArooaConversionException {
		
		String xml =  
			"<oddjob id='this'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <variables id='v'>" +
			"     <xml>" +
			"        <buffer><![CDATA[<some-xml>Will this work?</some-xml>]]></buffer>" +
			"     </xml>" +
			"    </variables>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob(); 
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		
		oj.run();
		
		String result =  new OddjobLookup(oj).lookup(
				"v.xml", String.class);

		assertEquals(String.format("<some-xml>Will this work?</some-xml>"), result);
	}
}
