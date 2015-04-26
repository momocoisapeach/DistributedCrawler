package edu.upenn.cis455.crawler;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

import org.jsoup.Jsoup;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

public class LD {
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	
	public void init(String profileDirectory) throws LangDetectException {
        DetectorFactory.loadProfile(profileDirectory);
    }
    public String detect(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
    }
    public ArrayList<Language> detectLangs(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }

	public static void main(String[] args) throws Exception {
		
		String url = "https://twitter.com/DMOZ/status/528252424671469568";
		System.out.println(String.valueOf(toBigInteger(url)));
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
	
	public static String html2text(String content) {
		return Jsoup.parse(content).text();
		
	}
	
	
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
