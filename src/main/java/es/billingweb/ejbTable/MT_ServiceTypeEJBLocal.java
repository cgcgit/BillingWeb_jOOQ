/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtServiceType;
import java.sql.Timestamp;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the service type
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_ServiceTypeEJBLocal {
    
    /**
     * Return all data of the service type in the system
     * @return the list with all the service type data in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtServiceType> findAllServiceType() throws BillingWebDataAccessException;
    
    /**
     * Return all data of the service type in the system for a specific date     
     * @param date date search criteria of the data
     * @return the list with all service type data in the database for a specific date
     * @throws BillingWebDataAccessException 
     */
    public List <MtServiceType> findServiceTypeByDate(Timestamp date) throws BillingWebDataAccessException;
    
    /**
     * Return the data of a specific service type 
     * @param serviceTypeId id of the service type 
     * @return the list with all the records of the service type in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtServiceType> findServiceTypeDetail(Integer serviceTypeId) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the service type
     * @param serviceType the service type to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertServiceType (MtServiceType serviceType) throws BillingWebDataAccessException;
    
    /**
     * Deletes of the database the service type
     * @param serviceType the service type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteServiceType (MtServiceType serviceType) throws BillingWebDataAccessException;
    
    
    /**
     * Update into the database the service type
     * @param serviceType the service type to update
     * @return 1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateServiceType (MtServiceType serviceType) throws BillingWebDataAccessException;
    
    
    /**
     * Delete all the record of the service type
     * @param serviceTypeId the service type id to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteServiceTypeById (Integer serviceTypeId) throws BillingWebDataAccessException;
    
   
   
    /**
     * Add the service type list to the database
     * @param serviceTypeList list of service type to add
     * @return 1: addition OK
     *         0: addition KO
     * @throws BillingWebDataAccessException 
     */
    public int addServiceTypeList (List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Update the service type list to the database
     * @param serviceTypeList list of service type to update
     * @return 1: update OK
     *         0: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateServiceTypeList (List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Delete the service type list to the database
     * @param serviceTypeList list of service type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteServiceTypeList (List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Manages the update into the database for a list of service type data for the same product_type_id: it deletes all the records for the product_type_id and add the new data of this product_type_id (contained in the list)
     * @param serviceTypeList list of service type (all for the same service type id) to update
     * @return  1: OK - all the service_type data in the list have the same id; 
     *          0: KO - almost one service_type in the list have another id 
     *         -1: KO - the list is empty
     * @throws BillingWebDataAccessException 
     */
    public int manageUpdateServiceTypeList(List<MtServiceType> serviceTypeList) throws BillingWebDataAccessException;
    
    
    
    
}
