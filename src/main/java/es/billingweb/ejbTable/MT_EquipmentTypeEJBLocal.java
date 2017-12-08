/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejbTable;

import es.billingweb.exception.BillingWebDataAccessException;
import es.billingweb.model.tables.pojos.MtEquipmentType;
import java.sql.Timestamp;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * Interface bean for the equipment type
 *
 * @author catuxa
 * @since july 2017
 * @version 1.0.0
 *
 */
@Local
public interface MT_EquipmentTypeEJBLocal {
    
    /**
     * Return all data of the equipment type in the system
     * @return the list with all the equipment type data in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtEquipmentType> findAllEquipmentType() throws BillingWebDataAccessException;
    
    /**
     * Return all data of the equipment type in the system for a specific date     
     * @param date date search criteria of the data
     * @return the list with all equipment type data in the database for a specific date
     * @throws BillingWebDataAccessException 
     */
    public List <MtEquipmentType> findEquipmentTypeByDate(Timestamp date) throws BillingWebDataAccessException;
    
    /**
     * Return the data of a specific equipment type 
     * @param equipmentTypeId id of the equipment type 
     * @return the list with all the records of the equipment type in the database
     * @throws BillingWebDataAccessException 
     */
    public List <MtEquipmentType> findEquipmentTypeDetail(Integer equipmentTypeId) throws BillingWebDataAccessException;
    
    /**
     * Inserts into the database the equipment type
     * @param equipmentType the equipment type to insert
     * @return  1: insert OK
     *          0: insert KO
     * @throws BillingWebDataAccessException 
     */
    public int insertEquipmentType (MtEquipmentType equipmentType) throws BillingWebDataAccessException;
    
    /**
     * Deletes of the database the equipment type
     * @param equipmentType the equipment type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteEquipmentType (MtEquipmentType equipmentType) throws BillingWebDataAccessException;
    
    
    /**
     * Update into the database the equipment type
     * @param equipmentType the equipment type to update
     * @return 1: update OK
     *          0: update WARNING - no changes are made
     *         -1: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateEquipmentType (MtEquipmentType equipmentType) throws BillingWebDataAccessException;
    
    
    /**
     * Delete all the record of the equipment type
     * @param equipmentTypeId the equipment type id to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteEquipmentTypeById (Integer equipmentTypeId) throws BillingWebDataAccessException;
    
   
   
    /**
     * Add the equipment type list to the database
     * @param equipmentTypeList list of equipment type to add
     * @return 1: addition OK
     *         0: addition KO
     * @throws BillingWebDataAccessException 
     */
    public int addEquipmentTypeList (List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Update the equipment type list to the database
     * @param equipmentTypeList list of equipment type to update
     * @return 1: update OK
     *         0: update KO
     * @throws BillingWebDataAccessException 
     */
    public int updateEquipmentTypeList (List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Delete the equipment type list to the database
     * @param equipmentTypeList list of equipment type to delete
     * @return 1: delete OK
     *         0: delete KO
     * @throws BillingWebDataAccessException 
     */
    public int deleteEquipmentTypeList (List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException;
    
    
    /**
     * Manages the update into the database for a list of equipment type data for the same product_type_id: it deletes all the records for the product_type_id and add the new data of this product_type_id (contained in the list)
     * @param equipmentTypeList list of equipment type (all for the same equipment type id) to update
     * @return  1: OK - all the equipment_type data in the list have the same id; 
     *          0: KO - almost one equipment_type in the list have another id 
     *         -1: KO - the list is empty
     * @throws BillingWebDataAccessException 
     */
    public int manageUpdateEquipmentTypeList(List<MtEquipmentType> equipmentTypeList) throws BillingWebDataAccessException;
    
    
    
    
}
