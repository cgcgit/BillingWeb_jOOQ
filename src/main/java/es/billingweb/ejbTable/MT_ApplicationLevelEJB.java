/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.MT_APPLICATION_LEVEL;
import static es.billingweb.model.Sequences.MT_APPLICATION_LEVEL_APPLICATION_LEVEL_ID_SEQ;
import es.billingweb.model.tables.pojos.MtApplicationLevel;
import es.billingweb.model.tables.records.MtApplicationLevelRecord;
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
public class MT_ApplicationLevelEJB implements MT_ApplicationLevelEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<MtApplicationLevel> findAllApplicationLevel() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtApplicationLevel> applicationLevels = null;
        try {
            applicationLevels = create.selectFrom(MT_APPLICATION_LEVEL).fetch()
                    .into(MtApplicationLevel.class);
            LOGGER.info("List of application levels returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of application levels " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return applicationLevels;

    }

    private Integer getNewApplicationLevelId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new application level id");

        // Obtains the id
        id = toIntExact(create.nextval(MT_APPLICATION_LEVEL_APPLICATION_LEVEL_ID_SEQ));

        return id;

    }

    @Override
    public int insertApplicationLevel(MtApplicationLevel applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new application level");

        id = this.getNewApplicationLevelId();
        applicationLevel.setApplicationLevelId(id);

        // Record for the new applicationLevel
        MtApplicationLevelRecord record = create.newRecord(MT_APPLICATION_LEVEL);

        record.from(applicationLevel);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new application level was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new application level was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting application level " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteApplicationLevel(MtApplicationLevel applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing application level");

        MtApplicationLevelRecord record = create.fetchOne(MT_APPLICATION_LEVEL, MT_APPLICATION_LEVEL.APPLICATION_LEVEL_ID.eq(applicationLevel.getApplicationLevelId()));

        try {

            // Deleting the application level
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The application level was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The application level was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting application level " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateApplicationLevel(MtApplicationLevel applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing application level");

        // Record for the new applicationLevel
        MtApplicationLevelRecord record = create.newRecord(MT_APPLICATION_LEVEL);

        record.from(applicationLevel);

        try {

            // Deleting the application level
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The application level was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The application level was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating application level " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

}
