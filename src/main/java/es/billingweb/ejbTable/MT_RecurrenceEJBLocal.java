/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtRecurrence;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the recurrence
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_RecurrenceEJBLocal {
    
    /**
     * Return all data of the recurrence in the system
     * @return a list with all recurrence data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtRecurrence> findAllRecurrence() throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the recurrence
     * @param recurrence the recurrence to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertRecurrence (MtRecurrence recurrence) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the recurrence
     * @param recurrence the recurrence to delete
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteRecurrence (MtRecurrence recurrence) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the recurrence
     * @param recurrence the recurrence to update
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateRecurrence (MtRecurrence recurrence) throws BillingWebDataAccessException;
    
}
