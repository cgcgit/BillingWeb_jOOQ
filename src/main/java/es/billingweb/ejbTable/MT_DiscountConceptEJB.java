/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.MT_DISCOUNT_CONCEPT;
import static es.billingweb.model.Sequences.MT_DISCOUNT_CONCEPT_DISCOUNT_CONCEPT_ID_SEQ;
import es.billingweb.model.tables.pojos.MtDiscountConcept;
import es.billingweb.model.tables.records.MtDiscountConceptRecord;
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
public class MT_DiscountConceptEJB implements MT_DiscountConceptEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();
    
    
    @Override
    public List<MtDiscountConcept> findAllDiscountConcept() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtDiscountConcept> applicationLevels = null;
        try {
            applicationLevels = create.selectFrom(MT_DISCOUNT_CONCEPT).fetch()
                    .into(MtDiscountConcept.class);
            LOGGER.info("List of discount concepts returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of discount concepts " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return applicationLevels;
        
    }
    
    private Integer getNewDiscountConceptId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);        
        
        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new discount type id");
       
        // Obtains the id
        id = toIntExact(create.nextval(MT_DISCOUNT_CONCEPT_DISCOUNT_CONCEPT_ID_SEQ));

         return id;

    }
    

    
    @Override
    public int insertDiscountConcept(MtDiscountConcept applicationLevel) throws BillingWebDataAccessException {
     DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id=0;
        Integer r=0;

        LOGGER.info("Inserting a new discount type");
        
        id=this.getNewDiscountConceptId();
        applicationLevel.setDiscountConceptId(id);
        
        // Record for the new applicationLevel
        MtDiscountConceptRecord record = create.newRecord(MT_DISCOUNT_CONCEPT);

        record.from(applicationLevel);
        

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
            throw new BillingWebDataAccessException("Error inserting discount concept " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteDiscountConcept(MtDiscountConcept applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing discount type");

        MtDiscountConceptRecord record = create.fetchOne(MT_DISCOUNT_CONCEPT, MT_DISCOUNT_CONCEPT.DISCOUNT_CONCEPT_ID.eq(applicationLevel.getDiscountConceptId()));

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
            throw new BillingWebDataAccessException("Error deleting discount concept " + e.getCause().toString(), e);
        }finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateDiscountConcept(MtDiscountConcept applicationLevel) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing discount type");

        // Record for the new applicationLevel
        MtDiscountConceptRecord record = create.newRecord(MT_DISCOUNT_CONCEPT);

        record.from(applicationLevel);

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
            throw new BillingWebDataAccessException("Error updating discount concept " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }
    
}
