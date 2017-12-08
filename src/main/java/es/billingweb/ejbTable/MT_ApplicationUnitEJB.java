/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Sequences.MT_APPLICATION_UNIT_APPLICATION_UNIT_ID_SEQ;
import static es.billingweb.model.Tables.MT_APPLICATION_UNIT;
import es.billingweb.model.tables.pojos.MtApplicationUnit;
import es.billingweb.model.tables.records.MtApplicationUnitRecord;
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
public class MT_ApplicationUnitEJB implements MT_ApplicationUnitEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<MtApplicationUnit> findAllApplicationUnit() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtApplicationUnit> applicationUnits = null;
        try {
            applicationUnits = create.selectFrom(MT_APPLICATION_UNIT).fetch()
                    .into(MtApplicationUnit.class);
            LOGGER.info("List of application units returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of application units " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return applicationUnits;

    }

    private Integer getNewApplicationUnitId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new application unit id");

        // Obtains the id
        id = toIntExact(create.nextval(MT_APPLICATION_UNIT_APPLICATION_UNIT_ID_SEQ));

        return id;

    }

    @Override
    public int insertApplicationUnit(MtApplicationUnit applicationUnit) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new application unit");

        id = this.getNewApplicationUnitId();
        applicationUnit.setApplicationUnitId(id);

        // Record for the new applicationUnit
        MtApplicationUnitRecord record = create.newRecord(MT_APPLICATION_UNIT);

        record.from(applicationUnit);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new application unit was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new application unit was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting application unit " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteApplicationUnit(MtApplicationUnit applicationUnit) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing application unit");

        MtApplicationUnitRecord record = create.fetchOne(MT_APPLICATION_UNIT, MT_APPLICATION_UNIT.APPLICATION_UNIT_ID.eq(applicationUnit.getApplicationUnitId()));

        try {

            // Deleting the application unit
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The application unit was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The application unit was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting application unit " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateApplicationUnit(MtApplicationUnit applicationUnit) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing application unit");

        // Record for the new applicationUnit
        MtApplicationUnitRecord record = create.newRecord(MT_APPLICATION_UNIT);

        record.from(applicationUnit);

        try {

            // Deleting the application unit
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The application unit was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The application unit was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating application unit " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

}
