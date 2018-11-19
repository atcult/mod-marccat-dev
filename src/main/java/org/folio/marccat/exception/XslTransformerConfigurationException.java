/*
 * (c) LibriCore
 *
 * Created on Aug 9, 2004
 *
 * XslTransformerConfigurationException.java
 */
package org.folio.marccat.exception;

/**
 * This exception is thrown when there is a XSLT configuration exception.
 *
 * @author Wim Crols
 * @version $Revision: 1.1 $, $Date: 2004/08/09 11:43:44 $
 * @since 1.0
 */
public class XslTransformerConfigurationException extends ModMarccatException {

  /**
   * @see Exception#Exception(Throwable)
   */
  public XslTransformerConfigurationException(Throwable cause) {
    super(cause);
  }

}
