package org.oddjob.sql;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class SQLScriptProcessorTest extends TestCase {

	String EOL = System.getProperty("line.separator");
	
	class SqlCapture implements SQLExecutor {
		
		List<String> results = new ArrayList<String>();
		
		@Override
		public void accept(String sql) {
			String s = sql.trim();
			if (s.length() > 0) {
				results.add(s);
			}
		}
	}
	
	public void testStandardDelimiter() throws Exception {
		
		String script =  
			EOL +
			"create" + EOL + 
			"table" + EOL +
			"test1;" + EOL +
			"   " + EOL +
			"create table test2;" + EOL;			
			
		ScriptParser test = new ScriptParser();
		
		SqlCapture capture = new SqlCapture();
		
		test.setInput(new ByteArrayInputStream(script.getBytes()));
		test.setTo(capture);
		
		test.go();
		
		assertEquals(2, capture.results.size());
		
		assertEquals("create table test1", capture.results.get(0));
		assertEquals("create table test2", capture.results.get(1));
	}	
}
