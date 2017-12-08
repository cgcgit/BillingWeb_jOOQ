/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtBusinessScope;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the business scope
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_BusinessScopeEJBLocal {
    
    /**
     * Return all data of the business scope in the system
     * @return a list with all business scope data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtBusinessScope> findAllBusinessScope() throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the business scope
     * @param businessScope the business scope to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertBusinessScope (MtBusinessScope businessScope) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the business scope
     * @param businessScope the business scope to delete
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteBusinessScope (MtBusinessScope businessScope) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the business scope
     * @param businessScope the business scope to update
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateBusinessScope (MtBusinessScope businessScope) throws BillingWebDataAccessException;
    
}
