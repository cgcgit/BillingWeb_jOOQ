/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtDiscountConcept;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the discount concept
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_DiscountConceptEJBLocal {
    
    /**
     * Return all data of the discount concept in the system
     * @return a list with all discount concept data in the database
     * @throws BillingWebDataAccessException
     */
    public List <MtDiscountConcept> findAllDiscountConcept() throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the discount concept
     * @param discountConcept the discount concept to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertDiscountConcept (MtDiscountConcept discountConcept) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the discount concept
     * @param discountConcept the discount concept to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteDiscountConcept (MtDiscountConcept discountConcept) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the discount concept
     * @param discountConcept the discount concept to insert
     * @return  1: insert OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateDiscountConcept (MtDiscountConcept discountConcept) throws BillingWebDataAccessException;
    
}
