package test.edu.upenn.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// TODO: Auto-generated Javadoc
/**
 * The Class RunAllTests.
 */
public class RunAllTests extends TestCase 
{
  
  /**
   * Suite.
   *
   * @return the test
   */
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
        /* TODO: Add the names of your unit test classes here */
        // Class.forName("your.class.name.here") 
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
}
