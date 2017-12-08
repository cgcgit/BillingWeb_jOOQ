/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtProductType;
import java.sql.Timestamp;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the product type
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_ProductTypeEJBLocal {
    
    /**
     * Return all data of the product type in the system
     * @return the list with all the product type data in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtProductType> findAllProductType() throws BillingWebDataAccessException;
    
    /**
     * Return all data of the product type in the system for a specific date     
     * @param date date search criteria of the data
     * @return list of all all product type data in the database for a specific date
     * @throws BillingWebDataAccessException 
     */
    public List <MtProductType> findProductTypeByDate(Timestamp date) throws BillingWebDataAccessException;
    
    /**
     * Return the data of a specific product type
     * @param productTypeId id of the product type 
     * @return list of all the records of the product type in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtProductType> findProductTypeDetail(Integer productTypeId) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the product type
     * @param productType the product type to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertProductType (MtProductType productType) throws BillingWebDataAccessException;
    
    /**
     * Deletes of the database the product type
     * @param productType the product type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteProductType (MtProductType productType) throws BillingWebDataAccessException;
    
    
    /**
     * Update into the database the product type
     * @param productType the product type to update
     * @return 1: update OK
     *         0: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateProductType (MtProductType productType) throws BillingWebDataAccessException;
    
    
    /**
     * Delete all the record of the product type
     * @param productTypeId the product type id to delete
     * @return 1: delete OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteProductTypeById (Integer productTypeId) throws BillingWebDataAccessException;
    
   
   
    /**
     * Add the product type list to the database
     * @param productTypeList list of product type to add
     * @return 1: addition OK
     *         0: addition KO
     * @throws BillingWebDataAccessException 
     */
    public int addProductTypeList (List<MtProductType> productTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Update the product type list to the database
     * @param productTypeList list of product type to update
     * @return 1: update OK
     *         0: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateProductTypeList (List<MtProductType> productTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Delete the product type list to the database
     * @param productTypeList list of product type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteProductTypeList (List<MtProductType> productTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Manages the update into the database for a list of product type data for the same product_type_id: it deletes all the records for the product_type_id and add the new data of this product_type_id (contained in the list)
     * @param productTypeList list of product type (all for the same product type id) to update
     * @return  1: OK - all the promotion_type data in the list have the same id; 
     *          0: KO - almost one promotion_type in the list have another id 
     *         -1: KO - the list is empty
     * @throws BillingWebDataAccessException 
     */
    public int manageUpdateProductTypeList(List<MtProductType> productTypeList) throws BillingWebDataAccessException;
    
    
    
    
}
