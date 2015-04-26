package edu.upenn.cis455.xpathengine;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XPathEngineImplTest {
	String[] xpaths;
	Document doc;

	@Test
	public void testIsValid() {
		XPathEngineImpl x = (XPathEngineImpl) new XPathEngineFactory().getXPathEngine();
		xpaths = new String[]{"/note",
				"/note[from]",
				"/note//",
				"/a/b[foo[text()=\"#$(/][]\"]][bar]/hi[@asdf=\"#$(&[]\"][this][is][crazy[text()=\"He said\\\"You are such a d***\\\"\"]]",
				"/test[ a/b1[ c1[p]/d[p] ] /n1[a]/n2 [c2/d[p]/e[text()=\"/asp[&123(123*/]\"]]]",
				"/note/hello4/this[@val=\"text1\"]/that[@val=\"text2\"][something/else]",
				"/XMLillegal"};
		x.setXPaths(xpaths);
		assertEquals(true, x.isValid(0));
		assertEquals(true, x.isValid(1));
		assertEquals(false, x.isValid(2));
		assertEquals(true, x.isValid(3));
		assertEquals(true, x.isValid(4));
		assertEquals(true, x.isValid(5));
		assertEquals(false, x.isValid(6));
		
	}

	
	@Test
	public void testValidTest() {
		XPathEngineImpl x = (XPathEngineImpl) new XPathEngineFactory().getXPathEngine();
		String test1 = "text()=\"string\"";
		String test2 = "  contains  (text(),  \"String\")";
		String test3 = "  @abc = \"String\"";
		assertEquals(true, x.validTest(test1));
		assertEquals(true, x.validTest(test2));
		assertEquals(true, x.validTest(test3));

	}
	
	@Test
	public void testEvaluate() throws ParserConfigurationException, SAXException, IOException {
		XPathEngineImpl x = (XPathEngineImpl) new XPathEngineFactory().getXPathEngine();
		xpaths = new String[]{"/note"};
		x.setXPaths(xpaths);
		String finalString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><note id = \"ui\"><to>[\"To/v\"]e</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>";
		StringReader strreader = new StringReader(finalString);
		BufferedReader bodyreader = new BufferedReader(strreader);
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = fact.newDocumentBuilder();
		InputSource is = new InputSource(bodyreader);
		Document doc = db.parse(is);
		assertEquals(true, x.evaluate(doc)[0]);
	}
	
	
	@Test
	public void testCheckTest() throws Exception{
		XPathEngineImpl x = (XPathEngineImpl) new XPathEngineFactory().getXPathEngine();
		xpaths = new String[]{"/note"};
		x.setXPaths(xpaths);
		String finalString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><note id = \"ui\"><to>[\"To/v\"]e</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>";
		StringReader strreader = new StringReader(finalString);
		BufferedReader bodyreader = new BufferedReader(strreader);
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = fact.newDocumentBuilder();
		InputSource is = new InputSource(bodyreader);
		Document doc = db.parse(is);
		assertEquals(true, x.evaluate(doc)[0]);
	}

}
