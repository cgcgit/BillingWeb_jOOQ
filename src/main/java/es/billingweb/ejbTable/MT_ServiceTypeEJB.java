/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Sequences.TID_SERVICE_TYPE_ID_SERVICE_TYPE_ID_SEQ;
import static es.billingweb.model.Tables.MT_SERVICE_TYPE;
import static es.billingweb.model.Tables.TID_SERVICE_TYPE_ID;
import es.billingweb.model.tables.pojos.MtServiceType;
import es.billingweb.model.tables.records.MtServiceTypeRecord;
import es.billingweb.model.tables.records.TidServiceTypeIdRecord;
import es.billingweb.utils.BillingWebDates;
import static java.lang.Math.toIntExact;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.val;

/**
 *
 * @author catuxa
 */
@Stateless
public class MT_ServiceTypeEJB implements MT_ServiceTypeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Creates a new service_type_id. If an exception occurs, it will be
     * propagated.
     *
     * @return the new service_type_id. If an error occurrs the return value
     * will be 0.
     */
    private Integer newServiceTypeId() throws BillingWebDataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = (Integer) 0;
        Integer r;

        LOGGER.info("Creating a new service_type_id");
        // Record for the user
        TidServiceTypeIdRecord record = create.newRecord(TID_SERVICE_TYPE_ID);

        // Obtains the id for the user
        id = toIntExact(create.nextval(TID_SERVICE_TYPE_ID_SERVICE_TYPE_ID_SEQ));

        // Set the id for the user
        record.setServiceTypeId(id);

        try {
            // Update the record
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new service_type_id was created");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new service_type_id was not created");
                    break;
                default:
                    LOGGER.error("ERROR - The result is not as expected");
            }

        } finally {
            create.close();
        }

        return id;
    }

    @Override
    public List<MtServiceType> findAllServiceType() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtServiceType> serviceTypeList = null;
        try {
            serviceTypeList = create.selectFrom(MT_SERVICE_TYPE)
                    .orderBy(MT_SERVICE_TYPE.SERVICE_TYPE_ID, MT_SERVICE_TYPE.START_DATE)
                    .fetch()
                    .into(MtServiceType.class);
            LOGGER.info("List of service type returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of serviceTypeList :" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return serviceTypeList;
    }

    @Override
    public List<MtServiceType> findServiceTypeByDate(Timestamp date) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtServiceType> serviceTypeList = null;
        try {
            serviceTypeList = create.selectFrom(MT_SERVICE_TYPE)
                    .where(val(date).between(MT_SERVICE_TYPE.START_DATE).and(MT_SERVICE_TYPE.END_DATE))
                    .orderBy(MT_SERVICE_TYPE.SERVICE_TYPE_ID, MT_SERVICE_TYPE.START_DATE)
                    .fetch()
                    .into(MtServiceType.class);
            LOGGER.info("List of service type for the date " + BillingWebDates.timestampToStringShortView(date) + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of serviceTypeList for the date " + BillingWebDates.timestampToStringShortView(date) + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return serviceTypeList;
    }

    @Override
    public List<MtServiceType> findServiceTypeDetail(Integer serviceTypeId) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtServiceType> serviceTypeList = null;
        try {
            serviceTypeList = create.selectFrom(MT_SERVICE_TYPE)
                    .where(MT_SERVICE_TYPE.SERVICE_TYPE_ID.equal(serviceTypeId))
                    .orderBy(MT_SERVICE_TYPE.SERVICE_TYPE_ID, MT_SERVICE_TYPE.START_DATE)
                    .fetch()
                    .into(MtServiceType.class);
            LOGGER.info("List of service type detail for service_type_id " + serviceTypeId + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of serviceTypeList for for service_type_id " + serviceTypeId + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return serviceTypeList;
    }

    @Override
    public int insertServiceType(MtServiceType serviceType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new service_type");

        id = this.newServiceTypeId();
        serviceType.setServiceTypeId(id);

        // Record for the new status
        MtServiceTypeRecord record = create.newRecord(MT_SERVICE_TYPE);

        record.from(serviceType);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new service type was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new service type was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting service type: " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteServiceType(MtServiceType serviceType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing service type");

        MtServiceTypeRecord record = create.newRecord(MT_SERVICE_TYPE);
        record.from(serviceType);

        try {

            // Deleting the status
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The service type was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The service type was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting service type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateServiceType(MtServiceType serviceType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing service type");

        MtServiceTypeRecord record = create.newRecord(MT_SERVICE_TYPE);
        record.from(serviceType);

        try {

            // Deleting the status
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The service type was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The service type was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating service type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteServiceTypeById(Integer serviceTypeId) throws BillingWebDataAccessException {

        int r = 0;

        LOGGER.info("Deleting data for the service type with id: " + serviceTypeId);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            r = create.delete(MT_SERVICE_TYPE)
                    .where(MT_SERVICE_TYPE.SERVICE_TYPE_ID.eq(serviceTypeId))
                    .execute();
            LOGGER.info("Data for the service type with id " + serviceTypeId + " was deleted");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the service type with id " + serviceTypeId + ": " + e.getCause().toString(), e);
        } finally {
        }

        return r;
    }

    @Override
    public int addServiceTypeList(List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r;

        r = 0;

        LOGGER.info("Adding data for the service type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            for (MtServiceType data : serviceTypeList) {
                MtServiceTypeRecord userRecord = create.newRecord(MT_SERVICE_TYPE);
                userRecord.from(data);
                userRecord.insert();
            }

            LOGGER.info("Data for the service type list was saved");

            create.close();
            r = 1;

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the service type list on the system" + e.getCause().toString(), e);
        } finally {

        }

        return r;

    }

    @Override
    public int updateServiceTypeList(List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r;

        r = 0;

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            for (MtServiceType data : serviceTypeList) {
                MtServiceTypeRecord userRecord = create.newRecord(MT_SERVICE_TYPE);
                userRecord.from(data);
                userRecord.update();
            }

            LOGGER.info("Data for the service type list was updated");

            create.close();
            r = 1;

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to update the service type list on the system" + e.getCause().toString(), e);
        } finally {

        }
        return r;

    }

    @Override
    public int deleteServiceTypeList(List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException {

        int r, r2;

        r = 0;

        LOGGER.info("Deleting data for the service type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            Integer serviceTypeId = serviceTypeList.get(0).getServiceTypeId();

            create.delete(MT_SERVICE_TYPE)
                    .where(MT_SERVICE_TYPE.SERVICE_TYPE_ID.eq(serviceTypeId))
                    .execute();

            for (MtServiceType data : serviceTypeList) {
                MtServiceTypeRecord userRecord = create.newRecord(MT_SERVICE_TYPE);
                userRecord.from(data);
                userRecord.delete();
            }

            LOGGER.info("Data for the service type list was deleted");

            create.close();
            r = 1;

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the service type list on the system: " + e.getCause().toString(), e);
        } finally {
            //create.close();

        }

        return r;

    }

    @Override
    public int manageUpdateServiceTypeList(List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException {

        int r = 0;

        LOGGER.info("Manage update for the service type list");

        r = onlyOneServiceTypeIdInTheList(serviceTypeList);

        if (r != 1) {
            switch (r) {
                case -1:
                    throw new BillingWebDataAccessException("Error while try to manage update for the service type list - The list is empty");
                case 0:
                    throw new BillingWebDataAccessException("Error while try to manage update for the service type list - Not all service type in the list have the same id");
            }
            return r;
        }

        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            Integer serviceTypeId = serviceTypeList.get(0).getServiceTypeId();

            create.delete(MT_SERVICE_TYPE)
                    .where(MT_SERVICE_TYPE.SERVICE_TYPE_ID.eq(serviceTypeId))
                    .execute();

            for (MtServiceType data : serviceTypeList) {
                MtServiceTypeRecord userRecord = create.newRecord(MT_SERVICE_TYPE);
                userRecord.from(data);
                userRecord.store();
            }

            create.close();

            r = 1;

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the user data on the system" + e.getCause().toString(), e);
        } finally {
            //create.close();

        }

        return r;
    }

    /**
     * Validates if all the service_type contained in the list have the same id
     *
     * @param serviceTypeList
     * @return 1: OK - all the service_type data in the list have the same id;
     * 0: KO - almost one service_type in the list have another id -1: KO - the
     * list is empty
     */
    private int onlyOneServiceTypeIdInTheList(List<MtServiceType> serviceTypeList) {

        Integer id;
        int r = 0;

        if (serviceTypeList.isEmpty()) {
            LOGGER.error("ERROR - The list is empty");
            return r;
        } else {
            id = serviceTypeList.get(0).getServiceTypeId();
            r = 1;
        }

        for (MtServiceType data : serviceTypeList) {
            if (!Objects.equals(data.getServiceTypeId(), id)) {
                LOGGER.error("ERROR - Not all service type in the list have the same id");
                r = 0;
                break;
            }
        }

        return r;

    }

}
