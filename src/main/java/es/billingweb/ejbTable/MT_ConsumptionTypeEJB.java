/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.MT_CONSUMPTION_TYPE;
import static es.billingweb.model.Sequences.MT_CONSUMPTION_TYPE_CONSUMPTION_TYPE_ID_SEQ;
import es.billingweb.model.tables.pojos.MtConsumptionType;
import es.billingweb.model.tables.records.MtConsumptionTypeRecord;
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
public class MT_ConsumptionTypeEJB implements MT_ConsumptionTypeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();
    
    
    @Override
    public List<MtConsumptionType> findAllConsumptionType() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtConsumptionType> consumptionTypes = null;
        try {
            consumptionTypes = create.selectFrom(MT_CONSUMPTION_TYPE).fetch()
                    .into(MtConsumptionType.class);
            LOGGER.info("List of consumption types returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of consumption types " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return consumptionTypes;
        
    }
    
    private Integer getNewConsumptionTypeId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);        
        
        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new consumption type id");
       
        // Obtains the id
        id = toIntExact(create.nextval(MT_CONSUMPTION_TYPE_CONSUMPTION_TYPE_ID_SEQ));

         return id;

    }
    

    
    @Override
    public int insertConsumptionType(MtConsumptionType consumptionType) throws BillingWebDataAccessException {
     DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id=0;
        Integer r=0;

        LOGGER.info("Inserting a new consumption type");
        
        id=this.getNewConsumptionTypeId();
        consumptionType.setConsumptionTypeId(id);
        
        // Record for the new consumptionType
        MtConsumptionTypeRecord record = create.newRecord(MT_CONSUMPTION_TYPE);

        record.from(consumptionType);        

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new consumption type was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new consumption type was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }        
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error insert consumption type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteConsumptionType(MtConsumptionType consumptionType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting an existing consumption type");

        MtConsumptionTypeRecord record = create.fetchOne(MT_CONSUMPTION_TYPE, MT_CONSUMPTION_TYPE.CONSUMPTION_TYPE_ID.eq(consumptionType.getConsumptionTypeId()));

        try {

            // Deleting the consumption type
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The consumption type was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The consumption type was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting consumption type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateConsumptionType(MtConsumptionType consumptionType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing consumption type");

        // Record for the new consumptionType
        MtConsumptionTypeRecord record = create.newRecord(MT_CONSUMPTION_TYPE);

        record.from(consumptionType);

        try {

            // Deleting the consumption type
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The consumption type was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The consumption type was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
           
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating consumption type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }
    
}
