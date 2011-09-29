package org.oddjob.tools.includes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.oddjob.jobs.XSLTJob;

/**
 * Creates XML that can be inserted into JavaDoc or another XML document from
 * an XML file.
 * 
 * The style-sheet used is courtesy of: http://lenzconsulting.com/xml-to-string/
 * 
 * @author rob
 *
 */
public class XMLFileLoader implements IncludeLoader {

	private static final Logger logger = Logger.getLogger(XMLFileLoader.class);
	
	private static final String EOL = System.getProperty("line.separator");
	
	public static final String TAG = "@oddjob.xml.file";
	
	private final File base;
	
	public XMLFileLoader(File base) {
		this.base = base;
	}
	
	@Override
	public boolean canLoad(String tag) {
		return TAG.equals(tag);
	}
	
	@Override
	public String load(String fileName) {
		
		try {
			FilterFactory filterFactory = new FilterFactory(fileName);
						
			File file = new File(base, filterFactory.getResourcePath());
			
			logger.info("Reading file " + file);
			
			InputStream input = new FileInputStream(file);
			
			String xml = filterFactory.getTextLoader().load(input);
			
			InputStream stylesheet = 
				getClass().getResourceAsStream("xml-2-string.xsl");
			
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			
			XSLTJob transform = new XSLTJob();
			transform.setStylesheet(stylesheet);
			transform.setInput(new ByteArrayInputStream(xml.getBytes()));
			transform.setOutput(result);
			
			transform.run();
			
			return "<pre class=\"xml\">" + EOL + 
				new String(result.toByteArray()) + 
				"</pre>" + EOL;
		}
		catch (Exception e) {
			return "<p><em>" + e.toString() + "</em></p>" + EOL;
		}
	}
}
