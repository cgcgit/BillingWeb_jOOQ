/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtConsumptionType;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the consumption type
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_ConsumptionTypeEJBLocal {
    
    /**
     * Return all data of the consumption type in the system
     * @return a list with all consumption type data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtConsumptionType> findAllConsumptionType() throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the consumption type
     * @param consumptionType the consumption type to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertConsumptionType (MtConsumptionType consumptionType) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the consumption type
     * @param consumptionType the consumption type to delete
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteConsumptionType (MtConsumptionType consumptionType) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the consumption type
     * @param consumptionType the consumption type to update
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateConsumptionType (MtConsumptionType consumptionType) throws BillingWebDataAccessException;
    
}
