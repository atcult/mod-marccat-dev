/*
 * (c) LibriCore
 *
 * Created on 08-feb-2005
 *
 * UserCommunicationKey.java
 */
package org.folio.cataloging.dao.persistence;

import java.io.Serializable;

/**
 * @author Elena
 * @version $Revision: 1.1 $, $Date: 2005/07/25 13:09:23 $
 * @since 1.0
 */
public class UserCommunicationKey implements Serializable {
  private int userNumber;
  private int indexNumber;


  /**
   * Class constructor
   */
  public UserCommunicationKey() {
    super();
  }

  public UserCommunicationKey(int userNumber, int indexNumber) {
    this.setUserNumber(userNumber);
    this.setIndexNumber(indexNumber);
  }

  /**
   * override equals and hashcode for hibernate key comparison
   */

  public boolean equals(Object anObject) {
    if (anObject instanceof UserCommunicationKey) {
      UserCommunicationKey aKey = (UserCommunicationKey) anObject;
      return (
        userNumber == aKey.getUserNumber()
          && indexNumber == aKey.getIndexNumber());
    } else {
      return false;
    }
  }

  public int hashCode() {
    return userNumber + indexNumber;
  }

  public int getIndexNumber() {
    return indexNumber;
  }

  public void setIndexNumber(int indexNumber) {
    this.indexNumber = indexNumber;
  }

  public int getUserNumber() {
    return userNumber;
  }

  public void setUserNumber(int userNumber) {
    this.userNumber = userNumber;
  }
}
