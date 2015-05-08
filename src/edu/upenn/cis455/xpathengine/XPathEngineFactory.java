package edu.upenn.cis455.xpathengine;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating XPathEngine objects.
 */
public class XPathEngineFactory {
	
	/**
	 * Gets the x path engine.
	 *
	 * @return the x path engine
	 */
	public static XPathEngine getXPathEngine() {
		return new XPathEngineImpl();
	}
}
