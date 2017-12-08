/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtStatus;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the status
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_StatusEJBLocal {
    
    /**
     * Return all data of the status in the system
     * @return a list with all status data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtStatus> findAllStatus() throws BillingWebDataAccessException;
    
    /**
     * Return all data of the status in the system
     * @param entityTypeId entity type id of the entity to obtain this status
     * @return a list with all status data for the entity type in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtStatus> findAllStatusForEntity(Integer entityTypeId) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the status
     * @param status the status to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertStatus (MtStatus status) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the status
     * @param status the status to add
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteStatus (MtStatus status) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the status
     * @param status the status to add
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateStatus (MtStatus status) throws BillingWebDataAccessException;
    
}
