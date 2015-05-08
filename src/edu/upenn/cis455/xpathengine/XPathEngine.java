package edu.upenn.cis455.xpathengine;

import org.w3c.dom.Document;

// TODO: Auto-generated Javadoc
/**
 * The Interface XPathEngine.
 */
interface XPathEngine {

  // Sets the XPath expression(s) that are to be evaluated. 
  /**
   * Sets the x paths.
   *
   * @param expressions the new x paths
   */
  void setXPaths(String[] expressions);

  // Returns true if the i.th XPath expression given to the last setXPaths() call
  // was valid, and false otherwise. If setXPaths() has not yet been called, the
  // return value is undefined. 
  /**
   * Checks if is valid.
   *
   * @param i the i
   * @return true, if is valid
   */
  boolean isValid(int i);

  // Takes a DOM root node as its argument, which contains the representation of the 
  // HTML or XML document. Returns an array of the same length as the 'expressions'
  // argument to setXPaths(), with the i.th element set to true if the document 
  // matches the i.th XPath expression, and false otherwise. If setXPaths() has not
  // yet been called, the return value is undefined.
  /**
   * Evaluate.
   *
   * @param d the d
   * @return the boolean[]
   */
  boolean[] evaluate(Document d);
  
}
