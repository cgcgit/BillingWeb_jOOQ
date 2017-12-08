/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.MT_ENTITY_TYPE;
import static es.billingweb.model.Sequences.MT_ENTITY_TYPE_ENTITY_TYPE_ID_SEQ;
import es.billingweb.model.tables.pojos.MtEntityType;
import es.billingweb.model.tables.records.MtEntityTypeRecord;
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
public class MT_EntityTypeEJB implements MT_EntityTypeEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();
    
    
    @Override
    public List<MtEntityType> findAllEntityType() throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtEntityType> entityTypes = null;
        try {
            entityTypes = create.selectFrom(MT_ENTITY_TYPE).fetch()
                    .into(MtEntityType.class);
            LOGGER.info("List of entity types returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of entity types " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return entityTypes;
        
    }
    
        @Override
    public MtEntityType findEntityTypeForCode(String entityCode) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        List<MtEntityType> entityTypes = null;
        try {
            entityTypes = create.selectFrom(MT_ENTITY_TYPE)
                    .where(MT_ENTITY_TYPE.ENTITY_TYPE_CODE.equal(entityCode))
                    .fetch()
                    .into(MtEntityType.class);
            LOGGER.info("List of entity types returns sucessfully ");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error obtaining the list of entity types " + e.getCause().toString(), e);
        } finally {
            create.close();
        }
        
        return entityTypes.get(0);
    }
    
    
    private Integer getNewEntityTypeId() throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);        
        
        Integer id = (Integer) 0; // Id of the new User
        Integer r;

        LOGGER.info("Creating a new entity type id");
       
        // Obtains the id
        id = toIntExact(create.nextval(MT_ENTITY_TYPE_ENTITY_TYPE_ID_SEQ));

         return id;

    }
    

    
    @Override
    public int insertEntityType(MtEntityType entityType) throws BillingWebDataAccessException {
     DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id=0;
        Integer r=0;

        LOGGER.info("Inserting a new entity type");
        
        id=this.getNewEntityTypeId();
        entityType.setEntityTypeId(id);
        
        // Record for the new entityType
        MtEntityTypeRecord record = create.newRecord(MT_ENTITY_TYPE);

        record.from(entityType);        

        try {
            r = record.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The new entity type was inserted");
                    break;
                case 0:
                    LOGGER.error("ERROR - new entity type was not inserted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the insertion was not as expected");
            }        
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error insert entity type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int deleteEntityType(MtEntityType entityType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Deleting an existing entity type");

        MtEntityTypeRecord record = create.fetchOne(MT_ENTITY_TYPE, MT_ENTITY_TYPE.ENTITY_TYPE_ID.eq(entityType.getEntityTypeId()));

        try {

            // Deleting the entity type
            r = record.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The entity type was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The entity type was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the deletion whas not as expected");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error deleting entity type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int updateEntityType(MtEntityType entityType) throws BillingWebDataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Upating an existing entity type");

        // Record for the new entityType
        MtEntityTypeRecord record = create.newRecord(MT_ENTITY_TYPE);

        record.from(entityType);

        try {

            // Deleting the entity type
            r = record.update();
            switch (r) {
                case 1:
                    LOGGER.info("The entity type was updated");
                    break;
                case 0:
                    LOGGER.warn("WARNING - The entity type was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result of the update whas not as expected");
            }
           
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error updating entity type " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    
    
}
