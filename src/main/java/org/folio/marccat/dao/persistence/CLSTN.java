package org.folio.marccat.dao.persistence;

import org.folio.marccat.business.cataloguing.authority.AuthorityClassificationAccessPoint;
import org.folio.marccat.business.descriptor.SortFormParameters;
import org.folio.marccat.dao.AbstractDAO;
import org.folio.marccat.dao.ClassificationDescriptorDAO;
import org.folio.marccat.shared.CorrelationValues;
import org.folio.marccat.util.StringText;

/**
 * Hibernate class for table CLSTN.
 *
 * @author paulm
 * @author carment
 */
public class CLSTN extends Descriptor {

  /**
   * The dewey edition number.
   */
  private Integer deweyEditionNumber;

  /**
   * The type code.
   */
  private int typeCode;

  /**
   * Instantiates a new clstn.
   */
  public CLSTN() {
    super();
  }


  /* (non-Javadoc)
   * @see Descriptor#getReferenceClass(java.lang.Class)
   */
  public Class getReferenceClass(Class targetClazz) {
    return null;
  }


  /**
   * Gets the dao.
   *
   * @return the dao
   */
  public AbstractDAO getDAO() {
    return new ClassificationDescriptorDAO();
  }


  /* (non-Javadoc)
   * @see Descriptor#getAccessPointClass()
   */
  public Class getAccessPointClass() {
    return ClassificationAccessPoint.class;
  }


  /* (non-Javadoc)
   * @see Descriptor#getAuthorityAccessPointClass()
   */
  public Class getAuthorityAccessPointClass() {
    return AuthorityClassificationAccessPoint.class;
  }


  /* (non-Javadoc)
   * @see Descriptor#getDefaultBrowseKey()
   */
  public String getDefaultBrowseKey() {
    return "23P5";
  }

  /* (non-Javadoc)
   * @see Descriptor#getNextNumberKeyFieldCode()
   */
  public String getNextNumberKeyFieldCode() {
    return "LN";
  }


  /* (non-Javadoc)
   * @see Descriptor#getCorrelationValues()
   */
  public CorrelationValues getCorrelationValues() {
    return new CorrelationValues(
      getTypeCode(),
      CorrelationValues.UNDEFINED,
      CorrelationValues.UNDEFINED);
  }

  /* (non-Javadoc)
   * @see Descriptor#setCorrelationValues(CorrelationValues)
   */
  public void setCorrelationValues(CorrelationValues v) {
    setTypeCode(v.getValue(1));
  }


  /* (non-Javadoc)
   * @see Descriptor#getSortFormParameters()
   */
  public SortFormParameters getSortFormParameters() {
    return new SortFormParameters(400, getTypeCode(), 0, 0, 0);
  }


  /* (non-Javadoc)
   * @see Descriptor#getCategory()
   */
  public int getCategory() {
    return 20;
  }


  /**
   * Gets the dewey edition number.
   *
   * @return the dewey edition number
   */
  public Integer getDeweyEditionNumber() {
    return deweyEditionNumber;
  }

  /**
   * Sets the dewey edition number.
   *
   * @param short1 the new dewey edition number
   */
  public void setDeweyEditionNumber(Integer short1) {
    deweyEditionNumber = short1;
  }

  /**
   * Gets the type code.
   *
   * @return the type code
   */
  public int getTypeCode() {
    return typeCode;
  }

  /**
   * Sets the type code.
   *
   * @param s the new type code
   */
  public void setTypeCode(int s) {
    typeCode = s;
  }

  /* (non-Javadoc)
   * @see Descriptor#getHeadingNumberSearchIndexKey()
   */
  public String getHeadingNumberSearchIndexKey() {
    return "233P";
  }

  /* (non-Javadoc)
   * @see Descriptor#getLockingEntityType()
   */
  public String getLockingEntityType() {
    return "LN";
  }

  @Override
  public void setStringText(String string) {
    if (ClassificationType.isDewey(getTypeCode())) {
      StringText st = new StringText(string);
      StringText sub2 = st.getSubfieldsWithCodes("2");
      if (!sub2.isEmpty()) {
        try {
          setDeweyEditionNumber(Integer.valueOf(sub2.getSubfield(0).getContent()));
        } catch (NumberFormatException e) {
          // ignore non numeric $2
        }
        st = st.getSubfieldsWithoutCodes("2");
      }
      super.setStringText(st.toString());
    } else {
      super.setStringText(string);
    }
  }
}
