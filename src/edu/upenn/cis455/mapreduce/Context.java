package edu.upenn.cis455.mapreduce;

// TODO: Auto-generated Javadoc
/**
 * The Interface Context.
 */
public interface Context {

  /**
   * Write.
   *
   * @param key the key
   * @param value the value
   */
  void write(String key, String value);
  
}
