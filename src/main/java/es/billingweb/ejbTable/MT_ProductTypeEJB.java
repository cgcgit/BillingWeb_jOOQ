/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Sequences.TID_PRODUCT_TYPE_ID_PRODUCT_TYPE_ID_SEQ;
import static es.billingweb.model.Tables.MT_PRODUCT_TYPE;
import static es.billingweb.model.Tables.TID_PRODUCT_TYPE_ID;
import es.billingweb.model.tables.pojos.MtProductType;
import es.billingweb.model.tables.records.MtProductTypeRecord;
import es.billingweb.model.tables.records.TidProductTypeIdRecord;
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
public class MT_ProductTypeEJB implements MT_ProductTypeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Creates a new product_type_id. If an exception occurs, it will be
     * propagated.
     *
     * @return the new product_type_id. If an error occurrs the return value
     * will be 0.
     */
    private Integer newProductTypeId() throws BillingWebDataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = (Integer) 0;
        Integer r;

        LOGGER.info("Creating a new product_type_id");
        // Record for the user
        TidProductTypeIdRecord record = create.newRecord(TID_PRODUCT_TYPE_ID);

        // Obtains the id for the user
        id = toIntExact(create.nextval(TID_PRODUCT_TYPE_ID_PRODUCT_TYPE_ID_SEQ));

        // Set the id for the user
        record.setProductTypeId(id);

        try {
            // Update the record
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new product_type_id was created");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new product_type_id was not created");
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
    public List<MtProductType> findAllProductType() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtProductType> productTypeList = null;
        try {
            productTypeList = create.selectFrom(MT_PRODUCT_TYPE)
                    .orderBy(MT_PRODUCT_TYPE.PRODUCT_TYPE_ID, MT_PRODUCT_TYPE.START_DATE)
                    .fetch()
                    .into(MtProductType.class);
            LOGGER.info("List of product type returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of productTypeList :" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return productTypeList;
    }

    @Override
    public List<MtProductType> findProductTypeByDate(Timestamp date) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtProductType> productTypeList = null;
        try {
            productTypeList = create.selectFrom(MT_PRODUCT_TYPE)
                    .where(val(date).between(MT_PRODUCT_TYPE.START_DATE).and(MT_PRODUCT_TYPE.END_DATE))
                    .orderBy(MT_PRODUCT_TYPE.PRODUCT_TYPE_ID, MT_PRODUCT_TYPE.START_DATE)
                    .fetch()
                    .into(MtProductType.class);
            LOGGER.info("List of product type for the date " + BillingWebDates.timestampToStringShortView(date) + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of productTypeList for the date " + BillingWebDates.timestampToStringShortView(date) + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return productTypeList;
    }

    @Override
    public List<MtProductType> findProductTypeDetail(Integer productTypeId) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtProductType> productTypeList = null;
        try {
            productTypeList = create.selectFrom(MT_PRODUCT_TYPE)
                    .where(MT_PRODUCT_TYPE.PRODUCT_TYPE_ID.equal(productTypeId))
                    .orderBy(MT_PRODUCT_TYPE.PRODUCT_TYPE_ID, MT_PRODUCT_TYPE.START_DATE)
                    .fetch()
                    .into(MtProductType.class);
            LOGGER.info("List of product type detail for product_type_id " + productTypeId + " returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of productTypeList for for product_type_id " + productTypeId + ": " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return productTypeList;
    }

    @Override
    public int insertProductType(MtProductType productType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = 0;
        Integer r = 0;

        LOGGER.info("Inserting a new product_type");

        id = this.newProductTypeId();
        productType.setProductTypeId(id);

        // Record for the new status
        MtProductTypeRecord record = create.newRecord(MT_PRODUCT_TYPE);

        record.from(productType);

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new product type was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The new product type was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error inserting product type: " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteProductType(MtProductType productType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Deleting an existing product type");

        MtProductTypeRecord record = create.newRecord(MT_PRODUCT_TYPE);
        record.from(productType);

        try {

            // Deleting the status
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The product type was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The product type was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting product type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateProductType(MtProductType productType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing product type");

        MtProductTypeRecord record = create.newRecord(MT_PRODUCT_TYPE);
        record.from(productType);

        try {

            // Deleting the status
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The product type was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The product type was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating product type: " + e.getCause().toString(), e);

        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteProductTypeById(Integer productTypeId) throws BillingWebDataAccessException {

        int r = 0;

        LOGGER.info("Deleting data for the product type with id: " + productTypeId);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            create.delete(MT_PRODUCT_TYPE)
                    .where(MT_PRODUCT_TYPE.PRODUCT_TYPE_ID.eq(productTypeId))
                    .execute();
            r = 1;
            LOGGER.info("Data for the product type with id " + productTypeId + " was deleted");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the product type with id " + productTypeId + ": " + e.getCause().toString(), e);
        } finally {

        }

        return r;

    }

    @Override
    public int addProductTypeList(List<MtProductType> productTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r;

        r = 0;

        LOGGER.info("Adding data for the product type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            for (MtProductType data : productTypeList) {
                MtProductTypeRecord userRecord = create.newRecord(MT_PRODUCT_TYPE);
                userRecord.from(data);
                userRecord.insert();
            }
            r = 1;

            LOGGER.info("Data for the product type list was saved");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the product type list on the system" + e.getCause().toString(), e);
        } finally {
        }

        return r;

    }

    @Override
    public int updateProductTypeList(List<MtProductType> productTypeList) throws BillingWebDataAccessException {
        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r;

        r = 0;

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            for (MtProductType data : productTypeList) {
                MtProductTypeRecord userRecord = create.newRecord(MT_PRODUCT_TYPE);
                userRecord.from(data);
                userRecord.update();
            }
            r = 1;
            LOGGER.info("Data for the product type list was updated");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to update the product type list on the system" + e.getCause().toString(), e);
        } finally {

        }
        return r;

    }

    @Override
    public int deleteProductTypeList(List<MtProductType> productTypeList) throws BillingWebDataAccessException {

        int r, r2;

        r = 0;

        LOGGER.info("Deleting data for the product type list ");

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            Integer productTypeId = productTypeList.get(0).getProductTypeId();

            create.delete(MT_PRODUCT_TYPE)
                    .where(MT_PRODUCT_TYPE.PRODUCT_TYPE_ID.eq(productTypeId))
                    .execute();

            for (MtProductType data : productTypeList) {
                MtProductTypeRecord userRecord = create.newRecord(MT_PRODUCT_TYPE);
                userRecord.from(data);
                userRecord.delete();
            }

            r = 1;

            LOGGER.info("Data for the product type list was deleted");

            create.close();

        } catch (DataAccessException | SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the product type list on the system: " + e.getCause().toString(), e);
        } finally {
            //create.close();

        }
        return r;

    }

    @Override
    public int manageUpdateProductTypeList(List<MtProductType> productTypeList) throws BillingWebDataAccessException {

        int r = -1;

        LOGGER.info("Manage update for the product type list");

        r = onlyOneProductTypeIdInTheList(productTypeList);

        if (r != 1) {
            switch (r) {
                case -1:
                    throw new BillingWebDataAccessException("Error while try to manage update for the product type list - The list is empty");
                case 0:
                    throw new BillingWebDataAccessException("Error while try to manage update for the product type list - Not all product type in the list have the same id");
            }
            return r;
        }

        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            Integer productTypeId = productTypeList.get(0).getProductTypeId();

            create.delete(MT_PRODUCT_TYPE)
                    .where(MT_PRODUCT_TYPE.PRODUCT_TYPE_ID.eq(productTypeId))
                    .execute();

            for (MtProductType data : productTypeList) {
                MtProductTypeRecord userRecord = create.newRecord(MT_PRODUCT_TYPE);
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
     * Validates if all the product_type contained in the list have the same id
     *
     * @param productTypeList
     * @return  1: OK - all the product_type data in the list have the same id
     *          0 --> KO - almost one product_type in the list have another id
     *         -1: KO - the list is empty
     */
    private int onlyOneProductTypeIdInTheList(List<MtProductType> productTypeList) {

        Integer id;
        int r = -1;

        if (productTypeList.isEmpty()) {
            LOGGER.error("ERROR - The list is empty");
            return r;
        } else {
            id = productTypeList.get(0).getProductTypeId();
            r = 1;
        }

        for (MtProductType data : productTypeList) {
            if (!Objects.equals(data.getProductTypeId(), id)) {
                LOGGER.error("ERROR - Not all product type in the list have the same id");
                r = 0;
                break;
            }
        }

        return r;

    }

}
