package edu.upenn.cis455.crawler;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Scanner;

import org.jsoup.Jsoup;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

// TODO: Auto-generated Javadoc
/**
 * The Class LD.
 */
public class LD {
	
	/** The Constant ENCODING. */
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	
	/** The range. */
	HashMap<Integer, BigInteger> range = new HashMap<Integer, BigInteger>();
	
	/**
	 * Inits the.
	 *
	 * @param profileDirectory the profile directory
	 * @throws LangDetectException the lang detect exception
	 */
	public void init(String profileDirectory) throws LangDetectException {
        DetectorFactory.loadProfile(profileDirectory);
    }
    
    /**
     * Detect.
     *
     * @param text the text
     * @return the string
     * @throws LangDetectException the lang detect exception
     */
    public String detect(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
    }
    
    /**
     * Detect langs.
     *
     * @param text the text
     * @return the array list
     * @throws LangDetectException the lang detect exception
     */
    public ArrayList<Language> detectLangs(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		
		LD a = new LD();
		
		String url = "https://weather.yahoo.com";
		System.out.println(String.valueOf(toBigInteger(url)));
//		String u = "weather.aol.com";
//		URL url = new URL("http://www.incrawler.com/");
//		
//		System.out.println(url.getHost());
//		a.setHashRange(18);
//		int writeTo = a.hash(toBigInteger(url.getHost()));
//		System.out.println(writeTo);
//		File dir = new File("./test/profiles/");
//		DetectorFactory.loadProfile(dir);
//		
//		String path = "/Users/peach/Documents/upenn/2015spring/cis555/db/db31/content/1215975451131256459546392193185395027229504040046";
//		
//		String text = readLargerTextFile(path);
//		System.out.println(text);
//		String after = html2text(text);
//		System.out.println(after);
//		Detector detector = DetectorFactory.create();
//        detector.append(after);
//        ArrayList<Language> langlist = detector.getProbabilities();
//        System.out.println(langlist);
//        System.out.println(detector.detect());
		

	}
	
	
	/**
	 * Hash.
	 *
	 * @param num the num
	 * @return the int
	 */
	public int hash(BigInteger num) {
//		int res = -1;
		num = num.abs();
		for (int i = 0; i < range.size(); i++) {
			if (num.compareTo(range.get(i)) < 0)
				return i;
		}
		return 0;
	}
	
	/**
	 * Sets the hash range.
	 *
	 * @param numCrawlers the new hash range
	 */
	private void setHashRange(int numCrawlers) {
//		  System.out.println("in the set hash range method");
		StringBuilder max = new StringBuilder("7");
		String num = String.valueOf(numCrawlers);
		HashMap<Integer, BigInteger> range = new HashMap<Integer, BigInteger>();
		for(int i = 0; i <39; i++){
			max.append("F");
		}
		BigInteger maxB = new BigInteger(max.toString(),16);
		BigInteger interval = maxB.divide(new BigInteger(num,16));
		BigInteger current = new BigInteger("0", 16);
		for(int i = 0; i < numCrawlers; i++){
			current = current.add(interval);
			range.put(i, current);
		}
		this.range = range;
		
	}
	
	/**
	 * To big integer.
	 *
	 * @param key the key
	 * @return the big integer
	 */
	public static BigInteger toBigInteger(String key) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(key.getBytes());
			byte[] bytes = messageDigest.digest();
			Formatter formatter = new Formatter();
			for (int i = 0; i < bytes.length; i++) {
				formatter.format("%02x", bytes[i]);
			}
			String resString = formatter.toString();
			formatter.close();
			return new BigInteger(resString, 16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return new BigInteger("0", 16);
	}
	
	/**
	 * Html2text.
	 *
	 * @param content the content
	 * @return the string
	 */
	public static String html2text(String content) {
		return Jsoup.parse(content).text();
		
	}
	
	
	/**
	 * Read larger text file.
	 *
	 * @param string the string
	 * @return the string
	 * @throws Exception the exception
	 */
	private static String readLargerTextFile(String string) throws Exception {
		Path path = Paths.get(string);
		StringBuilder text = new StringBuilder("");
		int i = 0;
	    try (Scanner scanner =  new Scanner(path, ENCODING.name())){
	      while (scanner.hasNextLine()){
	        //process each line in some way
	    	  text.append(scanner.nextLine());
	    	  i++;
	      }      
	    }
	    return text.toString();
	}

}
