/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Sequences.TID_PROMOTION_TYPE_ID_PROMOTION_TYPE_ID_SEQ;
import static es.billingweb.model.Tables.MT_PROMOTION_TYPE;
import static es.billingweb.model.Tables.TID_PROMOTION_TYPE_ID;
import es.billingweb.model.tables.pojos.MtPromotionType;
import es.billingweb.model.tables.records.MtPromotionTypeRecord;
import es.billingweb.model.tables.records.TidPromotionTypeIdRecord;
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
public class MT_PromotionTypeEJB implements MT_PromotionTypeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Creates a new promotion_type_id. If an exception occurs, it will be
     * propagated.
     *
     * @return the new promotion_type_id. If an error occurrs the return value
     * will be 0.
     */
    private Integer newPromotionTypeId() throws BillingWebDataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = (Integer) 0;
        Integer r;

        LOGGER.info("Creating a new promotion_type_id");
        // Record for the user
        TidPromotionTypeIdRecord record = create.newRecord(TID_PROMOTION_TYPE_ID);

        // Obtains the id for the user
        id = toIntExact(create.nextval(TID_PROMOTION_TYPE_ID_PROMOTION_TYPE_ID_SEQ));

        // Set the id for the user
        record.setPromotionTypeId(id);

        try {
            // Update the record
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new promotion_type_id was created");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new promotion_type_id was not created");
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
    public List<MtPromotionType> findAllPromotionType() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtPromotionType> promotionTypeList = null;
        try {
            promotionTypeList = create.selectFrom(MT_PROMOTION_TYPE)
                    .orderBy(MT_PROMOTION_TYPE.PROMOTION_TYPE_ID, MT_PROMOTION_TYPE.START_DATE)
                    .fetch()
                    .into(MtPromotionType.class);
            LOGGER.info("List of promotion type returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of promotionTypeList :" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return promotionTypeList;
    }

    @Override
    public List<MtPromotionType> findPromotionTypeByDate(Timestamp date) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtPromotionType> promotionTypeList = null;
        try {
            promotionTypeList = create.selectFrom(MT_PROMOTION_TYPE)
                    .where(val(date).between(MT_PROMOTION_TYPE.START_DATE).and(MT_PROMOTION_TYPE.END_DATE))
                    .orderBy(MT_PROMOTION_TYPE.PROMOTION_TYPE_ID, MT_PROMOTION_TYPE.START_DATE)
                    .fetch()
                    .into(MtPromotionType.class);
            LOGGER.info("List of promotion type for the date " + BillingWebDates.timestampToStringShortView(date) + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of promotionTypeList for the date " + BillingWebDates.timestampToStringShortView(date) + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return promotionTypeList;
    }

    @Override
    public List<MtPromotionType> findPromotionTypeDetail(Integer promotionTypeId) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtPromotionType> promotionTypeList = null;
        try {
            promotionTypeList = create.selectFrom(MT_PROMOTION_TYPE)
                    .where(MT_PROMOTION_TYPE.PROMOTION_TYPE_ID.equal(promotionTypeId))
                    .orderBy(MT_PROMOTION_TYPE.PROMOTION_TYPE_ID, MT_PROMOTION_TYPE.START_DATE)
                    .fetch()
                    .into(MtPromotionType.class);
            LOGGER.info("List of promotion type detail for promotion_type_id " + promotionTypeId + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of promotionTypeList for for promotion_type_id " + promotionTypeId + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return promotionTypeList;
    }

    @Override
    public int insertPromotionType(MtPromotionType promotionType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new promotion_type");

        id = this.newPromotionTypeId();
        promotionType.setPromotionTypeId(id);

        // Record for the new status
        MtPromotionTypeRecord record = create.newRecord(MT_PROMOTION_TYPE);

        record.from(promotionType);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new promotion type was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new promotion type was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting promotion type: " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deletePromotionType(MtPromotionType promotionType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Deleting an existing promotion type");

        MtPromotionTypeRecord record = create.newRecord(MT_PROMOTION_TYPE);
        record.from(promotionType);

        try {

            // Deleting the status
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The promotion type was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The promotion type was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting promotion type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updatePromotionType(MtPromotionType promotionType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing promotion type");

        MtPromotionTypeRecord record = create.newRecord(MT_PROMOTION_TYPE);
        record.from(promotionType);

        try {

            // Deleting the status
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The promotion type was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The promotion type was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating promotion type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deletePromotionTypeById(Integer promotionTypeId) throws BillingWebDataAccessException {

        int r = 0;

        LOGGER.info("Deleting data for the promotion type with id: " + promotionTypeId);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            create.delete(MT_PROMOTION_TYPE)
                    .where(MT_PROMOTION_TYPE.PROMOTION_TYPE_ID.eq(promotionTypeId))
                    .execute();
            r = 1;
            LOGGER.info("Data for the promotion type with id " + promotionTypeId + " was deleted");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the promotion type with id " + promotionTypeId + ": " + e.getCause().toString(), e);
        } finally {

        }

        return r;

    }

    @Override
    public int addPromotionTypeList(List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r;

        r = 0;

        LOGGER.info("Adding data for the promotion type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            for (MtPromotionType data : promotionTypeList) {
                MtPromotionTypeRecord userRecord = create.newRecord(MT_PROMOTION_TYPE);
                userRecord.from(data);
                userRecord.insert();
            }
            r = 1;

            LOGGER.info("Data for the promotion type list was saved");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the promotion type list on the system" + e.getCause().toString(), e);
        } finally {
        }

        return r;

    }

    @Override
    public int updatePromotionTypeList(List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r;

        r = 0;

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            for (MtPromotionType data : promotionTypeList) {
                MtPromotionTypeRecord userRecord = create.newRecord(MT_PROMOTION_TYPE);
                userRecord.from(data);
                userRecord.update();
            }
            r = 1;
            LOGGER.info("Data for the promotion type list was updated");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to update the promotion type list on the system" + e.getCause().toString(), e);
        } finally {

        }
        return r;

    }

    @Override
    public int deletePromotionTypeList(List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException {

        int r, r2;

        r = 0;

        LOGGER.info("Deleting data for the promotion type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            Integer promotionTypeId = promotionTypeList.get(0).getPromotionTypeId();

            create.delete(MT_PROMOTION_TYPE)
                    .where(MT_PROMOTION_TYPE.PROMOTION_TYPE_ID.eq(promotionTypeId))
                    .execute();

            for (MtPromotionType data : promotionTypeList) {
                MtPromotionTypeRecord userRecord = create.newRecord(MT_PROMOTION_TYPE);
                userRecord.from(data);
                userRecord.delete();
            }

            r = 1;

            LOGGER.info("Data for the promotion type list was deleted");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the promotion type list on the system: " + e.getCause().toString(), e);
        } finally {
            //create.close();

        }
        return r;

    }

    @Override
    public int manageUpdatePromotionTypeList(List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException {

        int r = -1;

        LOGGER.info("Manage update for the promotion type list");

        r = onlyOnePromotionTypeIdInTheList(promotionTypeList);

        if (r != 1) {
            switch (r) {
                case -1:
                    throw new BillingWebDataAccessException("Error while try to manage update for the promotion type list - The list is empty");
                case 0:
                    throw new BillingWebDataAccessException("Error while try to manage update for the promotion type list - Not all promotion type in the list have the same id");
            }
            return r;
        }

        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            Integer promotionTypeId = promotionTypeList.get(0).getPromotionTypeId();

            create.delete(MT_PROMOTION_TYPE)
                    .where(MT_PROMOTION_TYPE.PROMOTION_TYPE_ID.eq(promotionTypeId))
                    .execute();

            for (MtPromotionType data : promotionTypeList) {
                MtPromotionTypeRecord userRecord = create.newRecord(MT_PROMOTION_TYPE);
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
     * Validates if all the promotion_type contained in the list have the same id
     *
     * @param promotionTypeList
     * @return  1: OK - all the promotion_type data in the list have the same id
     *          0 --> KO - almost one promotion_type in the list have another id
     *         -1: KO - the list is empty
     */
    private int onlyOnePromotionTypeIdInTheList(List<MtPromotionType> promotionTypeList) {

        Integer id;
        int r = -1;

        if (promotionTypeList.isEmpty()) {
            LOGGER.error("ERROR - The list is empty");
            return r;
        } else {
            id = promotionTypeList.get(0).getPromotionTypeId();
            r = 1;
        }

        for (MtPromotionType data : promotionTypeList) {
            if (!Objects.equals(data.getPromotionTypeId(), id)) {
                LOGGER.error("ERROR - Not all promotion type in the list have the same id");
                r = 0;
                break;
            }
        }

        return r;

    }

}
