/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtPromotionType;
import java.sql.Timestamp;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the promotion type
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_PromotionTypeEJBLocal {
    
    /**
     * Return all data of the promotion type in the system
     * @return the list with all the promotion type data in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtPromotionType> findAllPromotionType() throws BillingWebDataAccessException;
    
    /**
     * Return all data of the promotion type in the system for a specific date     
     * @param date date search criteria of the data
     * @return list of all all promotion type data in the database for a specific date
     * @throws BillingWebDataAccessException 
     */
    public List <MtPromotionType> findPromotionTypeByDate(Timestamp date) throws BillingWebDataAccessException;
    
    /**
     * Return the data of a specific promotion type
     * @param promotionTypeId id of the promotion type 
     * @return list of all the records of the promotion type in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtPromotionType> findPromotionTypeDetail(Integer promotionTypeId) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the promotion type
     * @param promotionType the promotion type to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertPromotionType (MtPromotionType promotionType) throws BillingWebDataAccessException;
    
    /**
     * Deletes of the database the promotion type
     * @param promotionType the promotion type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deletePromotionType (MtPromotionType promotionType) throws BillingWebDataAccessException;
    
    
    /**
     * Update into the database the promotion type
     * @param promotionType the promotion type to update
     * @return 1: update OK
     *         0: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updatePromotionType (MtPromotionType promotionType) throws BillingWebDataAccessException;
    
    
    /**
     * Delete all the record of the promotion type
     * @param promotionTypeId the promotion type id to delete
     * @return 1: delete OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int deletePromotionTypeById (Integer promotionTypeId) throws BillingWebDataAccessException;
    
   
   
    /**
     * Add the promotion type list to the database
     * @param promotionTypeList list of promotion type to add
     * @return 1: addition OK
     *         0: addition KO
     * @throws BillingWebDataAccessException 
     */
    public int addPromotionTypeList (List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Update the promotion type list to the database
     * @param promotionTypeList list of promotion type to update
     * @return 1: update OK
     *         0: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updatePromotionTypeList (List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Delete the promotion type list to the database
     * @param promotionTypeList list of promotion type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deletePromotionTypeList (List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Manages the update into the database for a list of promotion type data for the same promotion_type_id: it deletes all the records for the promotion_type_id and add the new data of this promotion_type_id (contained in the list)
     * @param promotionTypeList list of promotion type (all for the same promotion type id) to update
     * @return  1: OK - all the promotion_type data in the list have the same id; 
     *          0: KO - almost one promotion_type in the list have another id 
     *         -1: KO - the list is empty
     * @throws BillingWebDataAccessException 
     */
    public int manageUpdatePromotionTypeList(List<MtPromotionType> promotionTypeList) throws BillingWebDataAccessException;
    
    
    
    
}
