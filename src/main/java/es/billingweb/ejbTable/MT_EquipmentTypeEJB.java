/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Sequences.TID_EQUIPMENT_TYPE_ID_EQUIPMENT_TYPE_ID_SEQ;
import static es.billingweb.model.Tables.MT_EQUIPMENT_TYPE;
import static es.billingweb.model.Tables.TID_EQUIPMENT_TYPE_ID;
import es.billingweb.model.tables.pojos.MtEquipmentType;
import es.billingweb.model.tables.records.MtEquipmentTypeRecord;
import es.billingweb.model.tables.records.TidEquipmentTypeIdRecord;
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
public class MT_EquipmentTypeEJB implements MT_EquipmentTypeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Creates a new equipment_type_id. If an exception occurs, it will be
     * propagated.
     *
     * @return the new equipment_type_id. If an error occurrs the return value
     * will be 0.
     */
    private Integer newEquipmentTypeId() throws BillingWebDataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);

        // Id of the new User
        Integer id = (Integer) 0;
        Integer r;

        LOGGER.info("Creating a new equipment_type_id");
        // Record for the user
        TidEquipmentTypeIdRecord record = create.newRecord(TID_EQUIPMENT_TYPE_ID);

        // Obtains the id for the user
        id = toIntExact(create.nextval(TID_EQUIPMENT_TYPE_ID_EQUIPMENT_TYPE_ID_SEQ));

        // Set the id for the user
        record.setEquipmentTypeId(id);

        try {
            // Update the record
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new equipment_type_id was created");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new equipment_type_id was not created");
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
    public List<MtEquipmentType> findAllEquipmentType() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);
        List<MtEquipmentType> equipmentTypeList = null;
        try {
            equipmentTypeList = create.selectFrom(MT_EQUIPMENT_TYPE)
                    .orderBy(MT_EQUIPMENT_TYPE.EQUIPMENT_TYPE_ID, MT_EQUIPMENT_TYPE.START_DATE)
                    .fetch()
                    .into(MtEquipmentType.class);
            LOGGER.info("List of equipment type returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of equipmentTypeList :" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return equipmentTypeList;
    }

    @Override
    public List<MtEquipmentType> findEquipmentTypeByDate(Timestamp date) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);
        List<MtEquipmentType> equipmentTypeList = null;
        try {
            equipmentTypeList = create.selectFrom(MT_EQUIPMENT_TYPE)
                    .where(val(date).between(MT_EQUIPMENT_TYPE.START_DATE).and(MT_EQUIPMENT_TYPE.END_DATE))
                    .orderBy(MT_EQUIPMENT_TYPE.EQUIPMENT_TYPE_ID, MT_EQUIPMENT_TYPE.START_DATE)
                    .fetch()
                    .into(MtEquipmentType.class);
            LOGGER.info("List of equipment type for the date " + BillingWebDates.timestampToStringShortView(date) + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of equipmentTypeList for the date " + BillingWebDates.timestampToStringShortView(date) + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return equipmentTypeList;
    }

    @Override
    public List<MtEquipmentType> findEquipmentTypeDetail(Integer equipmentTypeId) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);
        List<MtEquipmentType> equipmentTypeList = null;
        try {
            equipmentTypeList = create.selectFrom(MT_EQUIPMENT_TYPE)
                    .where(MT_EQUIPMENT_TYPE.EQUIPMENT_TYPE_ID.equal(equipmentTypeId))
                    .orderBy(MT_EQUIPMENT_TYPE.EQUIPMENT_TYPE_ID, MT_EQUIPMENT_TYPE.START_DATE)
                    .fetch()
                    .into(MtEquipmentType.class);
            LOGGER.info("List of equipment type detail for equipment_type_id " + equipmentTypeId + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of equipmentTypeList for for equipment_type_id " + equipmentTypeId + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return equipmentTypeList;
    }

    @Override
    public int insertEquipmentType(MtEquipmentType equipmentType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new equipment_type");

        id = this.newEquipmentTypeId();
        equipmentType.setEquipmentTypeId(id);

        // Record for the new status
        MtEquipmentTypeRecord record = create.newRecord(MT_EQUIPMENT_TYPE);

        record.from(equipmentType);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new equipment type was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new equipment type was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting equipment type: " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteEquipmentType(MtEquipmentType equipmentType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);

        int r = 0;

        LOGGER.info("Deleting an existing equipment type");

        MtEquipmentTypeRecord record = create.newRecord(MT_EQUIPMENT_TYPE);
        record.from(equipmentType);

        try {

            // Deleting the status
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The equipment type was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The equipment type was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting equipment type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateEquipmentType(MtEquipmentType equipmentType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);

        int r = -1;

        LOGGER.info("Upating an existing equipment type");

        MtEquipmentTypeRecord record = create.newRecord(MT_EQUIPMENT_TYPE);
        record.from(equipmentType);

        try {

            // Deleting the status
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The equipment type was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The equipment type was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating equipment type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteEquipmentTypeById(Integer equipmentTypeId) throws BillingWebDataAccessException {

        int r = 0;

        LOGGER.info("Deleting data for the equipment type with id: " + equipmentTypeId);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);

            r = create.delete(MT_EQUIPMENT_TYPE)
                    .where(MT_EQUIPMENT_TYPE.EQUIPMENT_TYPE_ID.eq(equipmentTypeId))
                    .execute();
            LOGGER.info("Data for the equipment type with id " + equipmentTypeId + " was deleted");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the equipment type with id " + equipmentTypeId + ": " + e.getCause().toString(), e);
        } finally {
        }

        return r;
    }

    @Override
    public int addEquipmentTypeList(List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);

        int r;

        r = 0;

        LOGGER.info("Adding data for the equipment type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);

            for (MtEquipmentType data : equipmentTypeList) {
                MtEquipmentTypeRecord userRecord = create.newRecord(MT_EQUIPMENT_TYPE);
                userRecord.from(data);
                userRecord.insert();
            }

            LOGGER.info("Data for the equipment type list was saved");

            create.close();
            r = 1;

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the equipment type list on the system" + e.getCause().toString(), e);
        } finally {

        }

        return r;

    }

    @Override
    public int updateEquipmentTypeList(List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);

        int r;

        r = 0;

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);

            for (MtEquipmentType data : equipmentTypeList) {
                MtEquipmentTypeRecord userRecord = create.newRecord(MT_EQUIPMENT_TYPE);
                userRecord.from(data);
                userRecord.update();
            }

            LOGGER.info("Data for the equipment type list was updated");

            create.close();
            r = 1;

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to update the equipment type list on the system" + e.getCause().toString(), e);
        } finally {

        }
        return r;

    }

    @Override
    public int deleteEquipmentTypeList(List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException {

        int r, r2;

        r = 0;

        LOGGER.info("Deleting data for the equipment type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);

            Integer equipmentTypeId = equipmentTypeList.get(0).getEquipmentTypeId();

            create.delete(MT_EQUIPMENT_TYPE)
                    .where(MT_EQUIPMENT_TYPE.EQUIPMENT_TYPE_ID.eq(equipmentTypeId))
                    .execute();

            for (MtEquipmentType data : equipmentTypeList) {
                MtEquipmentTypeRecord userRecord = create.newRecord(MT_EQUIPMENT_TYPE);
                userRecord.from(data);
                userRecord.delete();
            }

            LOGGER.info("Data for the equipment type list was deleted");

            create.close();
            r = 1;

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the equipment type list on the system: " + e.getCause().toString(), e);
        } finally {
            //create.close();

        }

        return r;

    }

    @Override
    public int manageUpdateEquipmentTypeList(List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException {

        int r = 0;

        LOGGER.info("Manage update for the equipment type list");

        r = onlyOneEquipmentTypeIdInTheList(equipmentTypeList);

        if (r != 1) {
            switch (r) {
                case -1:
                    throw new BillingWebDataAccessException("Error while try to manage update for the equipment type list - The list is empty");
                case 0:
                    throw new BillingWebDataAccessException("Error while try to manage update for the equipment type list - Not all equipment type in the list have the same id");
            }
            return r;
        }

        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_5);
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_5);

            Integer equipmentTypeId = equipmentTypeList.get(0).getEquipmentTypeId();

            create.delete(MT_EQUIPMENT_TYPE)
                    .where(MT_EQUIPMENT_TYPE.EQUIPMENT_TYPE_ID.eq(equipmentTypeId))
                    .execute();

            for (MtEquipmentType data : equipmentTypeList) {
                MtEquipmentTypeRecord userRecord = create.newRecord(MT_EQUIPMENT_TYPE);
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
     * Validates if all the equipment_type contained in the list have the same id
     *
     * @param equipmentTypeList
     * @return 1: OK - all the equipment_type data in the list have the same id;
     * 0: KO - almost one equipment_type in the list have another id -1: KO - the
     * list is empty
     */
    private int onlyOneEquipmentTypeIdInTheList(List<MtEquipmentType> equipmentTypeList) {

        Integer id;
        int r = 0;

        if (equipmentTypeList.isEmpty()) {
            LOGGER.error("ERROR - The list is empty");
            return r;
        } else {
            id = equipmentTypeList.get(0).getEquipmentTypeId();
            r = 1;
        }

        for (MtEquipmentType data : equipmentTypeList) {
            if (!Objects.equals(data.getEquipmentTypeId(), id)) {
                LOGGER.error("ERROR - Not all equipment type in the list have the same id");
                r = 0;
                break;
            }
        }

        return r;

    }

}
