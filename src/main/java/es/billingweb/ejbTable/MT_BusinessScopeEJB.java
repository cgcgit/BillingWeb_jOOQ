/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.MT_BUSINESS_SCOPE;
import static es.billingweb.model.Sequences.MT_BUSINESS_SCOPE_BUSINESS_SCOPE_ID_SEQ;
import es.billingweb.model.tables.pojos.MtBusinessScope;
import es.billingweb.model.tables.records.MtBusinessScopeRecord;
import static java.lang.Math.toIntExact;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 *
 * @author catuxa
 */
@Stateless
public class MT_BusinessScopeEJB implements MT_BusinessScopeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<MtBusinessScope> findAllBusinessScope() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtBusinessScope> applicationLevels = null;
        try {
            applicationLevels = create.selectFrom(MT_BUSINESS_SCOPE).fetch()
                    .into(MtBusinessScope.class);
            LOGGER.info("List of business scopes returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of business scopes " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return applicationLevels;

    }

    private Integer getNewBusinessScopeId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new business scope id");

        // Obtains the id
        id = toIntExact(create.nextval(MT_BUSINESS_SCOPE_BUSINESS_SCOPE_ID_SEQ));

        return id;

    }

    @Override
    public int insertBusinessScope(MtBusinessScope applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new business scope");

        id = this.getNewBusinessScopeId();
        applicationLevel.setBusinessScopeId(id);

        // Record for the new applicationLevel
        MtBusinessScopeRecord record = create.newRecord(MT_BUSINESS_SCOPE);

        record.from(applicationLevel);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new business scope was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new business scope was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting business scope " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteBusinessScope(MtBusinessScope applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing business scope");

        MtBusinessScopeRecord record = create.fetchOne(MT_BUSINESS_SCOPE, MT_BUSINESS_SCOPE.BUSINESS_SCOPE_ID.eq(applicationLevel.getBusinessScopeId()));

        try {

            // Deleting the business scope
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The business scope was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The business scope was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting business scope " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateBusinessScope(MtBusinessScope applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing business scope");

        // Record for the new applicationLevel
        MtBusinessScopeRecord record = create.newRecord(MT_BUSINESS_SCOPE);

        record.from(applicationLevel);

        try {

            // Deleting the business scope
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The business scope was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The business scope was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating business scope " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

}
