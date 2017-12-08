/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtApplicationLevel;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the application level
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_ApplicationLevelEJBLocal {
    
    /**
     * Return all data of the application level in the system
     * @return a list with all application level data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtApplicationLevel> findAllApplicationLevel() throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the application level
     * @param applicationLevel the application level to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertApplicationLevel (MtApplicationLevel applicationLevel) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the application level
     * @param applicationLevel the application level to delete
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteApplicationLevel (MtApplicationLevel applicationLevel) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the application level
     * @param applicationLevel the application level to update
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateApplicationLevel (MtApplicationLevel applicationLevel) throws BillingWebDataAccessException;
    
}
