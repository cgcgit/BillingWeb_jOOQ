/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtEntityType;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the application's user
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_EntityTypeEJBLocal {
    
    /**
     * Return all data of the entity type in the system
     * @return a list with all entity type data in the database
     */
    public List <MtEntityType> findAllEntityType() throws BillingWebDataAccessException;
    
    /**
     * Return the entity type data for a entity code
     * @param entityCode entity code of the entity to obtain the data
     * @return the record for those entity code
     * @throws BillingWebDataAccessException
     */
    public MtEntityType findEntityTypeForCode(String entityCode) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the entity type
     * @param entityType the entity type to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertEntityType (MtEntityType entityType) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the entity type
     * @param entityType the entity type to delete
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteEntityType (MtEntityType entityType) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the entity type
     * @param entityType the entity type to update
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateEntityType (MtEntityType entityType) throws BillingWebDataAccessException;
    
}
