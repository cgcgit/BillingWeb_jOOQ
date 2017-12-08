/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.MT_DISCOUNT_TYPE;
import static es.billingweb.model.Sequences.MT_DISCOUNT_TYPE_DISCOUNT_TYPE_ID_SEQ;
import es.billingweb.model.tables.pojos.MtDiscountType;
import es.billingweb.model.tables.records.MtDiscountTypeRecord;
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
public class MT_DiscountTypeEJB implements MT_DiscountTypeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();
    
    
    @Override
    public List<MtDiscountType> findAllDiscountType() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtDiscountType> discountTypes = null;
        try {
            discountTypes = create.selectFrom(MT_DISCOUNT_TYPE).fetch()
                    .into(MtDiscountType.class);
            LOGGER.info("List of discount types returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of discount types " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return discountTypes;
        
    }
    
    private Integer getNewDiscountTypeId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);        
        
        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new discount type id");
       
        // Obtains the id
        id = toIntExact(create.nextval(MT_DISCOUNT_TYPE_DISCOUNT_TYPE_ID_SEQ));

         return id;

    }
    

    
    @Override
    public int insertDiscountType(MtDiscountType discountType) throws BillingWebDataAccessException {
     DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id=0;
        Integer r=0;

        LOGGER.info("Inserting a new discount type");
        
        id=this.getNewDiscountTypeId();
        discountType.setDiscountTypeId(id);
        
        // Record for the new discountType
        MtDiscountTypeRecord record = create.newRecord(MT_DISCOUNT_TYPE);

        record.from(discountType);        

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new discount type was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new discount type was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }        
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error insert discount type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteDiscountType(MtDiscountType discountType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing discount type");

        MtDiscountTypeRecord record = create.fetchOne(MT_DISCOUNT_TYPE, MT_DISCOUNT_TYPE.DISCOUNT_TYPE_ID.eq(discountType.getDiscountTypeId()));

        try {

            // Deleting the discount type
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The discount type was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The discount type was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting discount type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateDiscountType(MtDiscountType discountType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing discount type");

        // Record for the new discountType
        MtDiscountTypeRecord record = create.newRecord(MT_DISCOUNT_TYPE);

        record.from(discountType);

        try {

            // Deleting the discount type
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The discount type was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The discount type was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
           
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating discount type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }
    
}
