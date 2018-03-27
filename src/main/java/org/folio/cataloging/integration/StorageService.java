package org.folio.cataloging.integration;

import io.vertx.core.Context;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.folio.cataloging.business.codetable.Avp;
import org.folio.cataloging.business.common.DataAccessException;
import org.folio.cataloging.dao.*;
import org.folio.cataloging.dao.common.HibernateSessionProvider;
import org.folio.cataloging.dao.persistence.*;
import org.folio.cataloging.log.Log;
import org.folio.cataloging.log.MessageCatalog;
import org.folio.cataloging.shared.CorrelationValues;
import org.folio.cataloging.shared.Validation;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.folio.cataloging.F.locale;

/**
 * Storage layer service.
 * This is the interface towards our storage service. Any R/W access to the persistence layer needs to pass through
 * this interface.
 *
 * @author agazzarini
 * @author carment
 * @author nbianchini
 * @since 1.0
 */
public class StorageService implements Closeable {

    private final Session session;
    private final Context context;
    private static final Log logger = new Log(StorageService.class);

    private final static Map<Integer, Class> firstCorrelationHeadingClassMap = new HashMap<Integer, Class>(){
        {
            put(1, T_BIB_HDR.class);
            put(2, NameType.class);
            put(17, NameType.class); //from heading
            put(3, TitleFunction.class);
            put(4, SubjectType.class);
            put(18, SubjectType.class); //from heading
            put(5, ControlNumberType.class);
            put(19, ControlNumberType.class); //from heading
            put(6, ClassificationType.class);
            put(20, ClassificationType.class); //from heading
            put(7, BibliographicNoteType.class); //note
            put(8, BibliographicRelationType.class);//relationship
            put(11, T_NME_TTL_FNCTN.class); //nt
        }
    };
    private final static Map<Integer, Class> thirdCorrelationHeadingClassMap = new HashMap<Integer, Class>(){
        {
            put(2, NameFunction.class);
            put(4, SubjectSource.class);
            put(11, NameSubType.class);
        }
    };
    private final static Map<Integer, Class> secondCorrelationClassMap = new HashMap<Integer, Class>(){
        {
            put(2, NameSubType.class);
            put(3, TitleSecondaryFunction.class);
            put(4, SubjectFunction.class);
            put(5, ControlNumberFunction.class);
            put(6, ClassificationFunction.class);
            put(11, NameType.class);
        }
    };

    /**
     * Builds a new {@link StorageService} with the given session.
     *
     * @param session the Hibernate session, which will be used for gathering a connection to the RDBMS.
     * @param context the vertx context.
     */
    StorageService(final Session session, final Context context) {
        this.session = session;
        this.context = context;
    }

    /**
     * Returns the configuration client associated with this module.
     *
     * @return the configuration client associated with this module.
     */
    public Context context() {
        return context;
    }

    /**
     * Returns the logical views associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the logical view associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getLogicalViews(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, DB_LIST.class, locale(lang));
    }

    /**
     * Returns the note types associated to the given language
     * and with the given note group type code.
     *
     * @param noteGroupTypeCode the note group type used here as filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the note type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getNoteTypesByGroupTypeCode(final String noteGroupTypeCode, final String lang) throws DataAccessException {
        final DAOBibliographicCorrelation daoBibliographicCorrelation = new DAOBibliographicCorrelation();
        return daoBibliographicCorrelation.getFirstCorrelationByNoteGroupCode(session, noteGroupTypeCode, locale(lang));
    }

    /**
     * Returns the record status types associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the record status type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getRecordStatusTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_REC_STUS.class, locale(lang));
    }


    /**
     * Returns the acquisition types associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the acquisition type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getAcquisitionTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ORDR_AQSTN_TYP.class, locale(lang));
    }

    /**
     * Returns the multipart resource level associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the multipart resource level associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getMultipartResourceLevels(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_LNK_REC.class, locale(lang));
    }

    /**
     * Returns the record types associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the record type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getRecordTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_REC_TYP.class, locale(lang));
    }

    /**
     * Returns the currencies associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the currency associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getCurrencies(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_CURCY_TYP.class, locale(lang));
    }

    /**
     * Returns the subscription associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the subscription associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getSubscriptions(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_HLDG_SBCPT_STUS.class, locale(lang));
    }


    /**
     * Returns the record display format associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the record display format associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getRecordDisplayFormats(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, RecordItemDisplay.class, locale(lang));
    }

    /**
     * Returns the verification levels associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the verification level associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getVerificationLevels(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_VRFTN_LVL.class, locale(lang));
    }

    /**
     * Returns the shelf list types associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the shelf list types associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getShelfListTypes(final String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_SHLF_LIST_TYP.class, locale(lang));
    }

    /**
     * Returns the detail levels associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the detail levels associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getDetailLevels(final String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_HLDG_LVL_OF_DTL.class, locale(lang));
    }

    /**
     * Returns the retentions associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the retentions associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getRetentions(final String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_SRL_GENRETENTION.class, locale(lang));
    }

    /**
     * Returns the status types associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the status types associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getStatusTypes(final String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_HLDG_STUS_TYP.class, locale(lang));
    }

    /**
     * Returns the series treatment types associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the series treatment types associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getSeriesTreatmentTypes(String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_HLDG_SRS_TRMT.class, locale(lang));
    }

    /**
     * Returns the authority sources associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the suthority sources associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getAuthoritySources(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_AUT_HDG_SRC.class, locale(lang));
    }



    /**
     * Returns the language types associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the language type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getLanguageTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_LANG.class, locale(lang));
    }

    /**
     * Returns the material types associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the material type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getMaterialTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_MTRL_TYP .class, locale(lang));
    }

    /**
     * Returns a list of all categories belonging to the requested type.
     *
     * @param type the index type, used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @return a list of categories by index type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<Integer>> getIndexCategories(final String type, final String lang) throws DataAccessException {
        final DAOSearchIndex searchIndexDao = new DAOSearchIndex();
        return searchIndexDao.getIndexCategories(session, type, locale(lang));
    }

    /**
     * Returns the categories belonging to the requested type.
     *
     * @param type the index type, used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @return a list of categories by index type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getIndexes(final String type, final int categoryCode, final String lang) throws DataAccessException {
        DAOSearchIndex searchIndexDao = new DAOSearchIndex();
        return searchIndexDao.getIndexes(session, type, categoryCode, locale(lang));
    }

    /**
     * Return the item type list.
     *
     * @param code the heading type code (first correlation), used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @param category the category of field, used here as a filter criterion.
     * @return a list of item type codes associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getSecondCorrelation(final int category, final int code, final String lang) throws DataAccessException {

        DAOBibliographicCorrelation daoBC = new DAOBibliographicCorrelation();
        final Class subTypeClass = secondCorrelationClassMap.get(category);
        return daoBC.getSecondCorrelationList(session, category, code, subTypeClass, locale(lang))
                .stream()
                .collect(toList());
    }

    /**
     * Return the function code list.
     *
     * @param category the category of field, used here as a filter criterion.
     * @param code1 the heading type code (first correlation), used here as a filter criterion.
     * @param code2 the item type code (second correlation), used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @return a list of function codes associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getThirdCorrelation(final int category,
                                                 final int code1,
                                                 final int code2,
                                                 final String lang) {

        final Class clazz = thirdCorrelationHeadingClassMap.get(category);
        DAOBibliographicCorrelation daoBC = new DAOBibliographicCorrelation();
        return daoBC.getThirdCorrelationList(session, category, code1, code2, clazz, locale(lang))
                .stream()
                .collect(toList());
    }

    /**
     * Returns the constraints (optional) to the requested index.
     *
     * @param code the index code, used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @return a list of all constraints (optional) to the requested index.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getIndexesByCode(final String code, final String lang) throws DataAccessException {
        final DAOIndexList daoIndex = new DAOIndexList();
        final String tableName = daoIndex.getCodeTableName(session, code, locale(lang));

        final DAOCodeTable dao = new DAOCodeTable();
        final Optional<Class> className  = ofNullable(HibernateSessionProvider.getHibernateClassName(tableName));
        return className.isPresent()
                ? dao.getList(session, className.get(), locale(lang))
                : Collections.emptyList();
    }

    /**
     * Returns the description for index code.
     *
     * @param code the index code, used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @return the description for index code associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public String getIndexDescription(final String code, final String lang) throws DataAccessException {
        DAOSearchIndex searchIndexDao = new DAOSearchIndex();
        return searchIndexDao.getIndexDescription(session, code, locale(lang));
    }

    /**
     * Returns the frbr types associated to the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of frbr type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getFrbrTypes(String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_FRBR.class, locale(lang));
    }

    /**
     * Returns the description for frbr type entity.
     *
     * @param code the frbr code, used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @return the description for index code associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public String getFrbrDescriptionByCode(final String code, final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        final short s_code = Short.parseShort(code);
        return dao.getLongText(session, s_code, T_FRBR.class, locale(lang));
    }

    @Override
    public void close() throws IOException {
        try {
            session.close();
        } catch (final HibernateException exception) {
            throw new IOException(exception);
        }
    }

    /**
     * Returns a list of {@link Avp} which represents a short version of the available bibliographic templates.
     *
     * @return a list of {@link Avp} which represents a short version of the available bibliographic templates.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<Integer>> getBibliographicRecordTemplates() throws DataAccessException {
        final BibliographicModelDAO dao = new BibliographicModelDAO();
        try {
            return dao.getBibliographicModelList(session);
        } catch (final HibernateException exception) {
            logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
            throw new DataAccessException(exception);
        }
    }

    /**
     * Returns a list of {@link Avp} which represents a short version of the available bibliographic templates.
     *
     * @return a list of {@link Avp} which represents a short version of the available bibliographic templates.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<Integer>> getAuthorityRecordTemplates() throws DataAccessException {
        final AuthorityModelDAO dao = new AuthorityModelDAO();
        try {
            return dao.getAuthorityModelList(session);
        } catch (final HibernateException exception) {
            logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
            throw new DataAccessException(exception);
        }
    }

    /**
     * update db with object c
     * @param c
     * @throws DataAccessException
     */

    public void updateCodeTable(final Object c) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        dao.updateCodeTable(c, session);
    }

    /**
     * get single row from db by language and code
     * @param c table class name
     * @param codeTable
     * @return
     * @throws DataAccessException
     */

    public CodeTable getCodeTableByCode (final Class c, final CodeTable codeTable) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.loadCodeTableEntry(session, c, codeTable);
    }

    /**
     * Gets the heading category.
     * @param lang the language code, used here as a filter criterion.
     * @return
     */
    public List<Avp<String>> getMarcCategories(String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_BIB_TAG_CAT.class, locale(lang));
    }

    /**
     *
     * @param lang the language code, used here as a filter criterion.
     * @param category the category, used here as a filter criterion.
     * @return  a list of heading item types by marc category code associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getFirstCorrelation(final String lang, final int category) throws DataAccessException {
        DAOCodeTable daoCT = new DAOCodeTable();
        return daoCT.getList(session, firstCorrelationHeadingClassMap.get(category), locale(lang))
                .stream()
                .collect(toList());
    }

    
    /**
     * Returns the descriptive catalog forms associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the descriptive catalog forms associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getDescriptiveCatalogForms(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_DSCTV_CTLG.class, locale(lang));
    }

    /**
     * Returns the control types associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the control type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getControlTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_CNTL_TYP.class, locale(lang));
    }

    /**
     * Returns the encoding levels associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the encoding level associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getEncodingLevels(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_ENCDG_LVL.class, locale(lang));
    }

    /**
     * Returns the character encoding schemas associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the character encoding schema associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getCharacterEncodingSchemas(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_CCS.class, locale(lang));
    }

    /**
     * Returns the bibliographic levels associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the bibliographic level associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getBibliographicLevels(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_BIB_LVL.class, locale(lang));
    }

    /**

     * Gets the note group type list.
     * @param lang the language code, used here as a filter criterion.
     * @return
     */
    public List<Avp<String>> getNoteGroupTypeList(String lang) {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, BibliographicNoteGroupType.class, locale(lang));
    }
    
     /**
     * Gets Validation for variable field.
      * Validation is related to sub-fields: valid, mandatory and default subfield
      *
     * @param marcCategory the marc category used here as filter criterion.
     * @param code1 the first correlation used here as filter criterion.
     * @param code2 the second correlation used here as filter criterion.
     * @param code3 the third correlation used here as filter criterion.
     * @return Validation object containing subfield list.
     */
    public Validation getSubfieldsByCorrelations(final int marcCategory,
                                                 final int code1,
                                                 final int code2,
                                                 final int code3) throws DataAccessException {
        final DAOBibliographicValidation daoBibliographicValidation = new DAOBibliographicValidation();
        try
        {
            final CorrelationValues correlationValues = new CorrelationValues(code1, code2, code3);
            return daoBibliographicValidation.load(session, marcCategory, correlationValues);
        } catch (final HibernateException exception) {
            logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
            throw new DataAccessException(exception);
        }
    }
    
   /**
     * Returns the date types associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the date type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getDateTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_ITM_DTE_TYP.class, locale(lang));
    }

    /**
     * Returns the modified record types associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the modified record type associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getModifiedRecordTypes(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_REC_MDFTN.class, locale(lang));
    }

    /**
     * Returns the catalog source associated with the given language.
     *
     * @param lang the language code, used here as a filter criterion.
     * @return a list of code / description tuples representing the catalog source associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public List<Avp<String>> getCatalogSources(final String lang) throws DataAccessException {
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getList(session, T_REC_CTLGG_SRC.class, locale(lang));
    }

    /**
     * Return a correlation values for a specific tag number.
     *
     * @param category the category code used here as filter criterion.
     * @param indicator1 the first indicator used here as filter criterion.
     * @param indicator2 the second indicator used here as filter criterion.
     * @param code the tag number code used here as filter criterion.
     * @return correlation values
     * @throws DataAccessException in case of data access failure.
     */
    public CorrelationValues getCorrelationVariableField(final Integer category,
                                              final String indicator1,
                                              final String indicator2,
                                              final String code) throws DataAccessException {

        final DAOBibliographicCorrelation daoBibliographicCorrelation = new DAOBibliographicCorrelation();
        try {

            final Optional<BibliographicCorrelation> bibliographicCorrelation = ofNullable(daoBibliographicCorrelation.getBibliographicCorrelation(session, code, indicator1.charAt(0), indicator2.charAt(0), category.shortValue()));
            return bibliographicCorrelation.isPresent() ? bibliographicCorrelation.get().getValues() : null;

        } catch (final HibernateException exception) {
            logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
            throw new DataAccessException(exception);
        }
    }


    /**
     * Gets the Material description information.
     * The values depend on mtrl_dsc and bib_itm data (leader).
     *
     * @param recordTypeCode the record type code (leader 05) used here as filter criterion.
     * @param bibligraphicLevel the bibliographic level (leader 06) used here as filter criterion.
     * @param code the tag number code used here as filter criterion.
     * @return a map with RecordTypeMaterial info.
     * @throws DataAccessException in case of data access failure.
     */
    public Map<String, Object> getMaterialTypeInfosByLeaderValues(final char recordTypeCode, final char bibligraphicLevel, final String code)  throws DataAccessException {

        Map<String, Object> mapRecordTypeMaterial = null;
        final DAORecordTypeMaterial dao = new DAORecordTypeMaterial();

        try {
            mapRecordTypeMaterial = new HashMap<>();
            RecordTypeMaterial rtm = dao.getMaterialHeaderCode(session, recordTypeCode, bibligraphicLevel);
            mapRecordTypeMaterial.put(GlobalStorage.HEADER_TYPE_LABEL, (code.equals(GlobalStorage.MATERIAL_TAG_CODE) ? rtm.getBibHeader008() : rtm.getBibHeader006()));
            mapRecordTypeMaterial.put(GlobalStorage.FORM_OF_MATERIAL_LABEL, rtm.getAmicusMaterialTypeCode());

            return mapRecordTypeMaterial;
        } catch (final HibernateException exception) {
            logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
            throw new DataAccessException(exception);
        }

	}

    /**
     * Gets the material type information.
     * Used for 006 field.
     *
     * @param headerCode the header code used here as filter criterion.
     * @param code the tag number code used here as filter criterion.
     * @return a string representing form of material.
     * @throws DataAccessException in case of data access failure.
     */
    public Map<String, Object> getMaterialTypeInfosByHeaderCode(final int headerCode, final String code) throws DataAccessException {

        final Map<String, Object> mapRecordTypeMaterial = new HashMap<>();
        final DAORecordTypeMaterial dao = new DAORecordTypeMaterial();
        try {

            return ofNullable(dao.getDefaultTypeByHeaderCode(session, headerCode, code))
                    .map( rtm -> {
                        mapRecordTypeMaterial.put(GlobalStorage.FORM_OF_MATERIAL_LABEL, rtm.getAmicusMaterialTypeCode());
                        mapRecordTypeMaterial.put(GlobalStorage.MATERIAL_TYPE_CODE_LABEL, rtm.getRecordTypeCode());

                        return mapRecordTypeMaterial;
                }).orElse(null);
        } catch (final HibernateException exception) {
            logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
            throw new DataAccessException(exception);
        }
    }


    /**
     * Returns the description for heading type entity.
     *
     * @param code the heading marc category code, used here as a filter criterion.
     * @param lang the language code, used here as a filter criterion.
     * @param category the category field.
     * @return the description for index code associated with the requested language.
     * @throws DataAccessException in case of data access failure.
     */
    public String getHeadingTypeDescription(final int code, final String lang, final int category) throws DataAccessException {
        Class clazz = firstCorrelationHeadingClassMap.get(category);
        final DAOCodeTable dao = new DAOCodeTable();
        return dao.getLongText(session, code, clazz, locale(lang));
    }

    /**
     * Check if exist the first correlation list for the category code.
     *
     * @param category the category associated to field/tag.
     * @return a true if exist, false otherwise.
     */
    public boolean existHeadingTypeByCategory(final int category){
        return ofNullable(firstCorrelationHeadingClassMap.get(category))
                .map(className -> {return true;}).orElse(false);
    }

    /**
     * Check if exist the second correlation list for the category code.
     *
     * @param category the category associated to field/tag.
     * @return a true if exist, false otherwise.
     */
    public boolean existItemTypeByCategory(final int category){
        return ofNullable(secondCorrelationClassMap.get(category))
                .map(className -> {return true;}).orElse(false);
    }

    /**
     * Check if exist the third correlation list for the category code.
     *
     * @param category the category associated to field/tag.
     * @return a true if exist, false otherwise.
     */
    public boolean existFunctionCodeByCategory(final int category){
        return ofNullable(thirdCorrelationHeadingClassMap.get(category))
                .map(className -> {return true;}).orElse(false);
    }

}