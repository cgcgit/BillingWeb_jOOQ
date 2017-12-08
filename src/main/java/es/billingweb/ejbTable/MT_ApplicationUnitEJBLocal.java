/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtApplicationUnit;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the application unit 
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_ApplicationUnitEJBLocal {
    
    /**
     * Return all data of the application unit in the system
     * @return a list with all application unit data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtApplicationUnit> findAllApplicationUnit() throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the application unit
     * @param applicationUnit the application unit to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertApplicationUnit (MtApplicationUnit applicationUnit) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the application unit
     * @param applicationUnit the application unit to delete
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteApplicationUnit (MtApplicationUnit applicationUnit) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the application unit
     * @param applicationUnit the application unit to update
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateApplicationUnit (MtApplicationUnit applicationUnit) throws BillingWebDataAccessException;
    
}
