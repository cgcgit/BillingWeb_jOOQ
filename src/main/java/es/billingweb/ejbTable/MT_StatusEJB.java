/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Sequences.MT_STATUS_STATUS_ID_SEQ;
import static es.billingweb.model.Tables.MT_STATUS;
import es.billingweb.model.tables.pojos.MtStatus;
import es.billingweb.model.tables.records.MtStatusRecord;
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
public class MT_StatusEJB implements MT_StatusEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<MtStatus> findAllStatus() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtStatus> statusList = null;
        try {
            statusList = create.selectFrom(MT_STATUS)
                    .orderBy(MT_STATUS.ENTITY_TYPE_ID,MT_STATUS.STATUS_CODE)
                    .fetch()
                    .into(MtStatus.class);
            LOGGER.info("List of status returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of statusList " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return statusList;
    }

    
     @Override
    public List<MtStatus> findAllStatusForEntity(Integer entityTypeId) throws BillingWebDataAccessException {
       DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        List<MtStatus> statusList = null;
        try {
            statusList = create.selectFrom(MT_STATUS)
                    .where(MT_STATUS.ENTITY_TYPE_ID.eq(entityTypeId))
                    .orderBy(MT_STATUS.STATUS_CODE)
                    .fetch().into(MtStatus.class);
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage() );
        } finally {
            create.close();
        }
        return statusList;
    }
    private Integer getNewStatusId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new status id");

        // Obtains the id
        id = toIntExact(create.nextval(MT_STATUS_STATUS_ID_SEQ));

        return id;

    }

    @Override
    public int insertStatus(MtStatus status) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new status");

        id = this.getNewStatusId();
        status.setStatusId(id);

        // Record for the new status
        MtStatusRecord record = create.newRecord(MT_STATUS);

        record.from(status);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new status was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new status was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting status " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteStatus(MtStatus status) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing status");

        MtStatusRecord record = create.fetchOne(MT_STATUS, MT_STATUS.STATUS_ID.eq(status.getStatusId()));

        try {

            // Deleting the status
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The status was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The status was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting status " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateStatus(MtStatus status) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing status");

        // Record for the new status
        MtStatusRecord record = create.newRecord(MT_STATUS);

        record.from(status);

        try {

            // Deleting the status
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The status was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The status was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating status " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

   

}
