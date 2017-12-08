/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.MT_TECHNOLOGY_SCOPE;
import static es.billingweb.model.Sequences.MT_TECHNOLOGY_SCOPE_TECHNOLOGY_SCOPE_ID_SEQ;
import es.billingweb.model.tables.pojos.MtTechnologyScope;
import es.billingweb.model.tables.records.MtTechnologyScopeRecord;
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
public class MT_TechnologyScopeEJB implements MT_TechnologyScopeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<MtTechnologyScope> findAllTechnologyScope() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtTechnologyScope> applicationLevels = null;
        try {
            applicationLevels = create.selectFrom(MT_TECHNOLOGY_SCOPE).fetch()
                    .into(MtTechnologyScope.class);
            LOGGER.info("List of recurrences returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of recurrences " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return applicationLevels;

    }

    private Integer getNewTechnologyScopeId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new recurrence id");

        // Obtains the id
        id = toIntExact(create.nextval(MT_TECHNOLOGY_SCOPE_TECHNOLOGY_SCOPE_ID_SEQ));

        return id;

    }

    @Override
    public int insertTechnologyScope(MtTechnologyScope applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new recurrence");

        id = this.getNewTechnologyScopeId();
        applicationLevel.setTechnologyScopeId(id);

        // Record for the new applicationLevel
        MtTechnologyScopeRecord record = create.newRecord(MT_TECHNOLOGY_SCOPE);

        record.from(applicationLevel);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new recurrence was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new recurrence was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting recurrence " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteTechnologyScope(MtTechnologyScope applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing recurrence");

        MtTechnologyScopeRecord record = create.fetchOne(MT_TECHNOLOGY_SCOPE, MT_TECHNOLOGY_SCOPE.TECHNOLOGY_SCOPE_ID.eq(applicationLevel.getTechnologyScopeId()));

        try {

            // Deleting the recurrence
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The recurrence was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The recurrence was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting recurrence " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateTechnologyScope(MtTechnologyScope applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing recurrence");

        // Record for the new applicationLevel
        MtTechnologyScopeRecord record = create.newRecord(MT_TECHNOLOGY_SCOPE);

        record.from(applicationLevel);

        try {

            // Deleting the recurrence
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The recurrence was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The recurrence was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating recurrence " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

}
