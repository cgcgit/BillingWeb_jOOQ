/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtDiscountType;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the discount type
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_DiscountTypeEJBLocal {
    
    /**
     * Return all data of the discount type in the system
     * @return a list with all discount type data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtDiscountType> findAllDiscountType() throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the discount type
     * @param discountType the discount type to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertDiscountType (MtDiscountType discountType) throws BillingWebDataAccessException;
    
    /**
     * Deletes into the database the discount type
     * @param discountType the discount type to delete
     * @return  1: delete OK
     *          0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteDiscountType (MtDiscountType discountType) throws BillingWebDataAccessException;
    
    /**
     * Updates into the database the discount type
     * @param discountType the discount type to update
     * @return  1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateDiscountType (MtDiscountType discountType) throws BillingWebDataAccessException;
    
}
