
package org.folio.cataloging.business.cataloguing.bibliographic;

import org.folio.cataloging.business.cataloguing.common.HeaderField;
import org.folio.cataloging.business.cataloguing.common.HeaderFieldHelper;
import org.folio.cataloging.business.cataloguing.common.Tag;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.dao.persistence.CorrelationKey;
import org.folio.cataloging.shared.CorrelationValues;

import java.util.List;

/**
 * @author paulm
 * @since 1.0
 */
public abstract class FixedField extends Tag implements HeaderField {
	private HeaderFieldHelper headerField = new BibliographicHeaderFieldHelper();

	/**
	 * Class constructor
	 *
	 * 
	 * @since 1.0
	 */
	public FixedField() {
	}

	/**
	 * Class constructor
	 *
	 * @param itemNumber
	 * @since 1.0
	 */
	public FixedField(int itemNumber) {
		super(itemNumber);
	}

	public boolean isBrowsable() {
		return false;
	}

	public abstract String getDisplayString();

	public boolean isFixedField() {
		return true;
	}

	public boolean isEditableHeader() {
		return true;
	}

	public boolean isAbleToBeDeleted() {
		return false; //default implementation
	}

	/**
	 * 
	 * @since 1.0
	 */
	public HeaderFieldHelper getHeaderField() {
		return headerField;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setHeaderField(HeaderFieldHelper helper) {
		headerField = helper;
	}

	/**
	 * 
	 * @since 1.0
	 */
	public short getCategory() {
		return headerField.getCategory();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public List getFirstCorrelationList() throws DataAccessException {
		return headerField.getFirstCorrelationList();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public List getSecondCorrelationList(short value1)
		throws DataAccessException {
		return headerField.getSecondCorrelationList(value1);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public List getThirdCorrelationList(short value1, short value2)
		throws DataAccessException {
		return headerField.getThirdCorrelationList(value1, value2);
	}

	/**
	 * 
	 * @since 1.0
	 */
	public boolean isHeaderField() {
		return headerField.isHeaderField();
	}

	public CorrelationKey getMarcEncoding()
		throws DataAccessException {
		CorrelationKey key = super.getMarcEncoding();
		return new CorrelationKey(
			key.getMarcTag(),
			' ',
			' ',
			key.getMarcTagCategoryCode());
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getRequiredEditPermission()
	 */
	public String getRequiredEditPermission() {
		return "editHeader";
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#getCorrelationValues()
	 */
	public CorrelationValues getCorrelationValues() {
		return headerField.getCorrelationValues();
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#setCorrelationValues(librisuite.business.common.CorrelationValues)
	 */
	public void setCorrelationValues(CorrelationValues v) {
		headerField.setCorrelationValues(v);
	}

	/* (non-Javadoc)
	 * @see librisuite.business.cataloguing.bibliographic.Tag#isWorksheetEditable()
	 */
	public boolean isWorksheetEditable() {
		return false;
	}


	/**
	 * 
	 * @since 1.0
	 */
	public short getHeaderType() {
		return headerField.getHeaderType();
	}

	/**
	 * 
	 * @since 1.0
	 */
	public void setHeaderType(short s) {
		headerField.setHeaderType(s);
	}

}
