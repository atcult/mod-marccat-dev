/*
 * (c) LibriCore
 *
 * Created on Jan 2, 2006
 *
 * DAOTitleNameTitleReferences.java
 */
package org.folio.cataloging.dao;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.dao.persistence.REF;
import org.folio.cataloging.dao.persistence.ReferenceType;
import org.folio.cataloging.dao.persistence.TTL_NME_TTL_REF;

import java.util.List;

/**
 * @author paulm
 * @version $Revision: 1.1 $, $Date: 2006/01/05 13:25:59 $
 * @since 1.0
 */
public class DAOTitleNameTitleReferences extends DAOCrossReferences {

  /* (non-Javadoc)
   * @see DAOCrossReferences#loadReciprocal(REF, int)
   */
  public REF loadReciprocal(REF ref, int cataloguingView) throws DataAccessException {

    int reciprocalType = ReferenceType.getReciprocal(ref.getType());

    REF result = null;
    String queryString;
    if (((TTL_NME_TTL_REF) ref).isSourceTitle()) {
      queryString =
        "from TTL_NME_TTL_REF as ref "
          + " where ref.titleHeadingNumber = ? AND "
          + " ref.nameTitleHeadingNumber = ? AND "
          + " ref.sourceHeadingType = 'MH' AND "
          + " substr(ref.userViewString, ?, 1) = '1' AND "
          + " ref.type = ?";

    } else {
      queryString =
        "from TTL_NME_TTL_REF as ref "
          + " where ref.nameTitleHeadingNumber = ? AND "
          + " ref.titleHeadingNumber = ? AND "
          + " ref.sourceHeadingType = 'TH' AND "
          + " substr(ref.userViewString, ?, 1) = '1' AND "
          + " ref.type = ?";
    }
    List l =
      find(
        queryString,
        new Object[]{
          ref.getSource(),
          ref.getTarget(),
          cataloguingView,
          reciprocalType},
        new Type[]{
          Hibernate.INTEGER,
          Hibernate.INTEGER,
          Hibernate.INTEGER,
          Hibernate.INTEGER});
    if (l.size() == 1) {
      result = (REF) l.get(0);
    }

    return result;
  }

}
