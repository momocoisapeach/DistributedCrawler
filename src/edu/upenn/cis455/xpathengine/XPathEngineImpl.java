package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XPathEngineImpl implements XPathEngine {
	String[] xpaths;
	Document doc;

  public XPathEngineImpl() {
    // Do NOT add arguments to the constructor!!
  }
	
  /*
   * this method put the string array passed from the xpath servlet into 
   * its own xpaths string array.
   * */
  public void setXPaths(String[] s) {
	  xpaths = s;
  }

  /*
   * this method verifies whether the ith xpath in the xpaths array
   * is valid or invalid.
   * 
   * it passes the ith xpath string into a method called validHelper
   * and this string has already be got rid of the first '/'
   * 
   *  returns a boolean
   * */
  public boolean isValid(int i) {
	  String path = xpaths[i];
	  if(!path.startsWith("/")){
		  return false;
	  }
	  path = path.substring(1);
//	  System.out.println(path);

	  return validHelper(path);
  }
	
  
  /*
   * this method verifies whether the passed in string is valid or invalid
   * 
   * first it checks the first index of '/' and '[', respectively
   * 
   * four cases in all
   * 
   * 1. no slash and no bracket 
   *   then it tries to match the whole string with the p_node_name pattern regex, 
   *   returns the result of a boolean
   *   
   * 2. no bracket but with at least a slash
   *   which means it has no test case, and it has at least one next step if it is valid
   *   then it tries to match the string before the first '/' with the p_node_name pattern regex
   *   and call itself with the string after '/'
   *   
   * 3. no slash but with at least one bracket
   *    it tries to match the string before the first '[' with the p_node_name pattern regex
   *   and call itself with the string from '['
   * 
   * 
   * 4. has both slash and bracket
   *    go through the whole string, if it meets
   *      1) a '/' first, then pass the rest string to itself
   *      2) a '[' first, then pass it to another method called splitTest
   * */
  private boolean validHelper(String path) {
//	  System.out.println("Now in the validHelper \nthe xpath now is "+path);
	  Pattern p_node_name;
	  Matcher matcher;
	  p_node_name = Pattern.compile("^\\s*([A-Z]|[a-z]|\\_)+([A-Z]|[a-z]|[0-9]|\\_|\\.|\\-)*\\s*$");
	int slash = path.indexOf("/");
	int bracket = path.indexOf("[");
	if(slash==-1 && bracket == -1){
//		System.out.println("xpath has no slash and brackets");
		if(path.toLowerCase().startsWith("xml")) {
//			System.out.println("starts with xml illegal&&&&&&&&&&&&&&&&&");
			return false;
		}
		matcher = p_node_name.matcher(path);
		return matcher.find();
//		System.out.println(matcher.find());
//		return matcher.find();
	}
	else if(bracket == -1){
		String childrenPath = path.substring(slash+1);
		path = path.substring(0,slash);
		if(path.toLowerCase().startsWith("xml")) return false;
		matcher = p_node_name.matcher(path);
		if(matcher.find()){
			return validHelper(childrenPath);
		}
		else return false;
	}
	else if(slash == -1){
		String tests = path.substring(bracket);
		path = path.substring(0,bracket);
		if(path.toLowerCase().startsWith("xml")) return false;
		matcher = p_node_name.matcher(path);
		if(matcher.find()){
			return split(tests);
		}
	}
	else{
//		System.out.println("the xpath has both slash and bracket");
		String next;
		int j;
		for(j = 0; j <path.length(); j++){
			//first meet a '/'
			if(path.charAt(j)=='/'){
				next = path.substring(j+1);
				path = path.substring(0,j);
				if(path.toLowerCase().startsWith("xml")) return false;
				matcher = p_node_name.matcher(path);
				if(matcher.find()){
					return validHelper(next);
				}
				else return false;
			}
			//first meet a [
			else if(path.charAt(j)=='['){
				next = path.substring(j);
				path = path.substring(0,j);
				if(path.toLowerCase().startsWith("xml")) return false;
				matcher = p_node_name.matcher(path);
				if(matcher.find()){
					return split(next);
				}
				else return false;
			}
		}
		
	}
	return false;
}

  
  
/* three cases might be passed into this function
 * 1. tests = [test] ([test])*
 * 2. tests = [test]/ nextStep
 * 3. tests = [test]([test])*  /nextStep
 * 
 * test has three cases
 * 1. test = text()="String"
 * 2. test = @attname = "String"
 * 3. test = contains( text(), "String")
 * 4. test = step
 *
 * Once a single test is recognized, it would be passed into validTest function
 * Once a nextStep is recognized, it would be passed into validHelper function
 * */
private boolean split(String tests) {

//	System.out.println("Now in the split function\nand the tests are "+tests);
	ArrayList<String> conditions = new ArrayList<String>();
	Pattern p_node_name;
	Matcher matcher;
	int count = 0, start=-1, j, end = -1, quot = 0;
	p_node_name = Pattern.compile("^\\s*([A-Z]|[a-z]|\\_)+([A-Z]|[a-z]|[0-9]|\\_|\\.|\\-)*\\s*$");
	String childrenStep;
	for(j = 0; j < tests.length(); j++){
		if(quot == 0 && tests.charAt(j)=='['){
//			System.out.println("char at "+j+" is [\n");
			if(count==0){
				start = j+1;
			}
			count++;
//			System.out.println("and the count is "+count);
		}
		if(tests.charAt(j)=='"' && quot == 0){
//			System.out.println("char at "+j+" is \"\n");
			quot = 1;
			j++;
		}
		if(tests.charAt(j)=='"' && quot == 1){
			if(tests.charAt(j-1)!='\\'){
//				System.out.println("char at "+j+" is \"\nand it is the second \"");
				j++;
				while(j<tests.length() && (tests.charAt(j)==' ')){
					j++;		
				}
				if(j== tests.length()) return false;
				if(tests.charAt(j)==')'){
					j++;
					while(j<tests.length() && (tests.charAt(j)==' ')){
						j++;		
					}
					if(j== tests.length()) return false;
				}
				if(tests.charAt(j)!=']') return false;
				quot = 0;
			}
		}
		if(quot ==0 && tests.charAt(j)==']'){
//			System.out.println("char at "+j+" is ]\n");
			count--;
			end = j;
//			System.out.println("and the count is "+count);
		}
		if(tests.charAt(j)=='/' && quot == 0 && end == -1 && start == -1){
			childrenStep = tests.substring(j+1);
			tests = tests.substring(0,j);
			if(validHelper(tests)){
				return validHelper(childrenStep);
			}
		}
		if(start!= -1 && count==0){
//			System.out.println("the condition is "+tests.substring(start,end));
			conditions.add(tests.substring(start,end));
			start=-1;
			end = -1;
			quot = 0;
		}
	}
//	System.out.println(conditions.size());
	if(conditions.size()!=0){
		for(j = 0; j <conditions.size(); j++){
			String temp = conditions.get(j);
			if(!validTest(temp)){
//				System.out.println("######1");
				return false;
			}
		}
//		System.out.println("######2");
		if(j==conditions.size()) return true;
	}
//	System.out.println("######3");
	return false;
	
}


/*
 * test if it is a valid test case
 * four cases
 * 1. @attname = "..."
 * 2. text() = "..."
 * 3. contains(text(),"...")
 * 4. a step
 * 
 * */
public boolean validTest(String test) {
//	System.out.println("Now in the valid Test");
	Pattern p_test_att, p_test_text, p_test_contain, p_test_step;
	Matcher matcher_att, matcher_text, matcher_contain;
	p_test_att = Pattern.compile("^\\s*@([A-Z]|[a-z]|\\_)+([A-Z]|[a-z]|[0-9]|\\_|\\.|\\-)*\\s*=\\s*\".*\"\\s*$");
	p_test_text = Pattern.compile("^\\s*text\\s*\\(\\)\\s*=\\s*\".*\"\\s*$");
	p_test_contain = Pattern.compile("^\\s*contains\\s*\\(\\s*text\\s*\\(\\)\\s*,\\s*\".*\"\\)$");
	matcher_att = p_test_att.matcher(test);
	matcher_text = p_test_text.matcher(test);
	matcher_contain = p_test_contain.matcher(test);
	if(matcher_att.find()){
//		System.out.println("it's an att test");
		return true;
	}
	else if(matcher_text.find()){
//		System.out.println("it's a text test");
		return true;
	}
	else if(matcher_contain.find()){
//		System.out.println("it's a contains text test");
		return true;
	}
	else{
//		System.out.println("it's a step");
//		System.out.println("and the step is "+test);
		return validHelper(test);
	}
}


/*
 * evaluate the xpaths with the document, return a boolean array, indicating the match
 * result of every xpath in the xpath array
 * 
 * first test whether the ith xpath is valid, if it's not, return false
 * then test if it matches any node in the document
 * 
 * */
public boolean[] evaluate(Document d) { 
    boolean[] result = new boolean[xpaths.length];
    doc = d;
    for(int i = 0; i <xpaths.length; i++){
    	if(!isValid(i)){
//    		System.out.println("invalid xpath");
    		result[i] = false;
    	}
    	else{
    		
    		String step = xpaths[i];
    		step = step.substring(1);

    		if(d==null){
    			result[i] = false;
    		}
    		else{
    			Node root = null;
    			NodeList nodeList = d.getChildNodes();
    		    for (int j = 0; j < nodeList.getLength(); j++) {
    		        Node currentNode = nodeList.item(j);
    		        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
    		            //calls this method for all the children which is Element
    		        	root = currentNode;
    		        	break;
    		        }
    		    }
    			if(checkStep(step, root)){
    		
	    			result[i] = true;
	    		}
	    		else{
	    			result[i] = false;
	    		}
    		}
    	
    	}
    }
    return result; 
  }


/*
 * check whether the xpath (a string) matches the node
 * 
 * the basic idea is the same with the isValid method
 * however, since the xpath has already passed the isValid method, the match
 * becomes easier
 * 
 * */
private boolean checkStep(String step, Node d) {
	
	int slash = step.indexOf("/");
	int bracket = step.indexOf("[");
	int i;
//	System.out.println("the dom node now is"+d.getNodeName());
	//no bracket no slash
	if(slash ==-1 && bracket == -1){
		step = step.trim();
//		System.out.println("no bracket no slash\nstep = "+step+"\nnode ="+d.getNodeName());
		return step.matches(d.getNodeName());
	}
	// no bracket but with slash
	else if(bracket == -1){
		
		String childrenStep = step.substring(slash+1);
		
		step = step.substring(0,slash).trim();
//		System.out.println("no bracket but with slash\nstep = "+step+"\nnode="+d.getNodeName());
		if(step.matches(d.getNodeName())){
			NodeList childrenNode = d.getChildNodes();
			for(i = 0; i < childrenNode.getLength(); i++){
				Node currentNode = childrenNode.item(i);
//				System.out.println("childrenStep"+i+"="+childrenStep+"\nchildreNode"+i+"="+currentNode.getNodeName());
				if(checkStep(childrenStep, currentNode)){
					return true;
				}
			}
			if(i == childrenNode.getLength()) return false;
		}
		else return false;
	}
	else if(slash==-1){
		String tests = step.substring(bracket);
		step = step.substring(0,bracket).trim();
		if(step.matches(d.getNodeName())){
			return splitTest(tests, d);
		}
		else return false;
	}
	//with bracket and slash
	else{
		String next;
		int j;
		for(j = 0; j <step.length(); j++){
			//first meet a '/'
			if(step.charAt(j)=='/'){
				next = step.substring(j+1);
				step = step.substring(0,j).trim();
				if(step.matches(d.getNodeName())){
					NodeList childrenNode = d.getChildNodes();
					for(i = 0; i < childrenNode.getLength(); i++){
						Node currentNode = childrenNode.item(i);
//						System.out.println("childrenStep"+i+"="+childrenStep+"\nchildreNode"+i+"="+currentNode.getNodeName());
						if(checkStep(next, currentNode)){
							return true;
						}
					}
					if(i == childrenNode.getLength()) return false;
				}
				else return false;
			}
			//first meet a [
			else if(step.charAt(j)=='['){
				next = step.substring(j);
				step = step.substring(0,j).trim();
				if(step.matches(d.getNodeName())){
					return splitTest(next, d);
				}
				else return false;
			}
		}
			
	}
	return false;
	
	
}

private boolean splitTest(String tests, Node d) {
//	System.out.println("Now in the split function\nand the tests are "+tests);
	ArrayList<String> conditions = new ArrayList<String>();
	Pattern p_node_name;
	Matcher matcher;
	int count = 0, start=-1, j, end = -1, quot = 0, i;
	p_node_name = Pattern.compile("^\\s*([A-Z]|[a-z]|\\_)+([A-Z]|[a-z]|[0-9]|\\_|\\.|\\-)*\\s*$");
	String childrenStep;
	for(j = 0; j < tests.length(); j++){
		if(quot == 0 && tests.charAt(j)=='['){
//			System.out.println("char at "+j+" is [\n");
			if(count==0){
				start = j+1;
			}
			count++;
//			System.out.println("and the count is "+count);
		}
		if(tests.charAt(j)=='"' && quot == 0){
//			System.out.println("char at "+j+" is \"\n");
			quot = 1;
			j++;
		}
		if(tests.charAt(j)=='"' && quot == 1){
			if(tests.charAt(j-1)!='\\'){
//				System.out.println("char at "+j+" is \"\nand it is the second \"");
				j++;
				while(j<tests.length() && (tests.charAt(j)==' ')){
					j++;		
				}
				if(j== tests.length()) return false;
				if(tests.charAt(j)==')'){
					j++;
					while(j<tests.length() && (tests.charAt(j)==' ')){
						j++;		
					}
					if(j== tests.length()) return false;
				}
				if(tests.charAt(j)!=']') return false;
				quot = 0;
			}
		}
		if(quot ==0 && tests.charAt(j)==']'){
//			System.out.println("char at "+j+" is ]\n");
			count--;
			end = j;
//			System.out.println("and the count is "+count);
		}
		if(tests.charAt(j)=='/' && quot == 0 && end == -1 && start == -1){
			childrenStep = tests.substring(j+1);
			tests = tests.substring(0,j);
			if(splitTest(tests, d)){
				NodeList childrenNode = d.getChildNodes();
				for(i = 0; i < childrenNode.getLength(); i++){
					Node currentNode = childrenNode.item(i);
//					System.out.println("childrenStep"+i+"="+childrenStep+"\nchildreNode"+i+"="+currentNode.getNodeName());
					if(checkStep(childrenStep, currentNode)){
						return true;
					}
				}
				if(i == childrenNode.getLength()) return false;
			}
		}
		if(start!= -1 && count==0){
//			System.out.println("the condition is "+tests.substring(start,end));
			conditions.add(tests.substring(start,end));
			start=-1;
			end = -1;
			quot = 0;
		}
	}
//	System.out.println(conditions.size());
	if(conditions.size()!=0){
		for(j = 0; j <conditions.size(); j++){
			String temp = conditions.get(j);
//			if(isStep(temp)){
//				if(!validHelper(temp)){
//					return false;
//				}
//			}
//			else{
				if(!checkTest(temp, d)){
//					System.out.println("######1");
					return false;
				}
//			}
		}
//		System.out.println("######2");
		if(j==conditions.size()) return true;
	}
//	System.out.println("######3");
	return false;
	

}

/*
 * check whether this test string matched the given node
 * four cases 
 * 1. @attname = "..."
 * 2. text() = "..."
 * 3. contains(text(),"...")
 * 4. a step
 * */
public boolean checkTest(String test, Node d) {
	int i, flag = -1;
	if(test.contains("\\\"")){
//		System.out.println("@@@@@@@@@contains a \\\"@@@@@@@@@@@@@@@@@");
		test = test.replace("\\\"", "\"");
	}
//	System.out.println("now in the checkTest function, the test is "+test+"\nand the node now is "+d.getNodeName());
	//@attname="..."
		Pattern p = Pattern.compile("^\\s*@([A-Z]|[a-z]|\\_)+([A-Z]|[a-z]|[0-9]|\\_|\\.|\\-)*\\s*=\\s*");
		Matcher matcher1 = p.matcher(test);
		if(matcher1.find()){
//			System.out.println("found a @attname test");
			String attName = test.substring(test.indexOf("@")+1,test.indexOf("="));
			attName = attName.trim();
			String attValue = test.substring(test.indexOf("\"")+1,test.lastIndexOf("\""));
			NamedNodeMap atts = d.getAttributes();		
			for(i = 0; i <atts.getLength(); i++){
				Node attNode = atts.item(i);
				String name = attNode.getNodeName();
				String value = attNode.getNodeValue();
				if(attName.equals(name) && attValue.equals(value)){
					return true;
				}
			}
			if(i==atts.getLength()) return false;
		}
		//text()="..."
		p = Pattern.compile("^\\s*text\\s*\\(\\)\\s*=\\s*.*");
		matcher1 = p.matcher(test);
		if(matcher1.find()){
//			System.out.println("found a text test");
			String txt = test.substring(test.indexOf("\"")+1, test.lastIndexOf("\""));
//			System.out.println("the text test in the xpath is"+txt);
			String context = d.getTextContent();
//			NodeList childrenNode = d.getChildNodes();
//			for(i = 0; i <childrenNode.getLength(); i++){
//				Node currentNode = childrenNode.item(i);
//				if(currentNode.getNodeType()==Node.TEXT_NODE){
//					Text text = new Text(currentNode);
//				}
//			}
//			System.out.println("and the text in the node"+d.getNodeName()+" is "+context);
			return txt.equals(context);
		}
		//contains(text(),"...")
		p = Pattern.compile("^\\s*contains\\s*\\(\\s*text\\s*\\(\\)\\s*,\\s*\".*\"\\)");
		matcher1 = p.matcher(test);
		if(matcher1.find()){
//			System.out.println("found a contains text test");
			String c_txt = test.substring(test.indexOf("\"")+1,test.lastIndexOf("\""));
			String context = d.getTextContent();
			return context.contains(c_txt);
		}
	
	//step
	//System.out.println("no bracket but with slash\nstep = "+step+"\nnode="+d.getNodeName());
		NodeList childrenNode = d.getChildNodes();
		for(i = 0; i < childrenNode.getLength(); i++){
			Node currentNode = childrenNode.item(i);
//			System.out.println("childrenStep"+i+"="+test+"\nchildreNode"+i+"="+currentNode.getNodeName());
			if(checkStep(test, currentNode)){
				return true;
			}
		}
		if(i == childrenNode.getLength()) return false;	

	
	return false;
}
        
}
