package edu.upenn.cis455.crawler;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.RawFile;

public class XPathCrawlerTest {
	XPathCrawler crawler;
	String directory = "/Users/peach/Documents/cis555/testDB/";
	DBWrapper db;
	RobotsTxtInfo robots;

	@Before
	public void setUp() throws Exception {
		crawler = new XPathCrawler();
		crawler.setMaxSize(1.0);
		robots = new RobotsTxtInfo();
		db = new DBWrapper(directory);
		
		RawFile file = new RawFile("http://www.example.com/text.html");
		String html = "<html><body><a href = \"abcdefg\"></a></body></html>";
		file.setFile(html);
		db.addRawFile(file);
		
		db.closeEnv();
	}



	@Test
	public void testCheckSizeType() {
		assertEquals(true, crawler.checkSizeType("text/html", 1024));
		assertEquals(crawler.checkSizeType("text/plain", 1024), false);
		assertEquals(crawler.checkSizeType("text/xml", 1024), true);
	}

	@Test
	public void testProcessRobotTxt() {
		String body = "# These defaults shouldn't apply to your crawler\n"
				+ "User-agent: *\n"
				+ "Disallow: /crawltest/marie/\n"
				+ "Crawl-delay: 10\n"
				+ "\n"
				+ "# Below is the directive your crawler should use:\n"
				+ "User-agent: cis455crawler\n"
				+ "Disallow: /crawltest/marie/private/\n"
				+ "Disallow: /crawltest/foo/\n"
				+ "Disallow: /infrastructure/\n"
				+ "Disallow: /maven/\n"
				+ "Disallow: /ppod/\n"
				+ "Crawl-delay: 5\n"
				+ "\n"
				+ "# This should be ignored by your crawler\n"
				+ "User-agent: evilcrawler\n"
				+ "Disallow: /\n";
		String host = "dbappserv.cis.upenn.edu";
		crawler.processRobotTxt(host, body, robots);
		assertEquals(robots.isAllowed("dbappserv.cis.upenn.edu", "/crawltest/foo/"), false);
		assertEquals(robots.isAllowed("dbappserv.cis.upenn.edu", "/crawltest/marie/private"), true);
	
	}



}
