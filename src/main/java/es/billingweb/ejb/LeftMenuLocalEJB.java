/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejb;

import javax.ejb.Local;
import es.billingweb.model.tables.records.TestMenuRecord;
import org.jooq.Result;

/**
 *
 * @author catuxa
 */
@Local
public interface LeftMenuLocalEJB {

    /**
     * Returns all the data in the menu table
     * @return  Result<TestMenuRecord> with all the menudata
     */
    Result<TestMenuRecord> findAll(String profile);
    
    /**
     * Returns the records for a specific id
     * @param id Integer: the id of the menu record
     * @return Result<TestMenuRecord> with all the menu records with the specific id
     */
    Result<TestMenuRecord> findById(Integer id, String profile);
    
    /**
     * Returns the records for a specific level
     * @param level Integer: the level of the menu
     * @return Result<TestMenuRecord> with all the menudata for the specific level
     */
    Result<TestMenuRecord> findByLevel(Integer level, String profile);

    /**
     * Returns the records for all root menus
     * @return Result<TestMenuRecord> with all the root menudata
     */
    Result<TestMenuRecord> findAllRootParents(String profile);
    
    /**
     * Returns the records for all menus that have a parent menu
     * @return Result<TestMenuRecord> with all the children menus
     */
    Result<TestMenuRecord> findAllChildren(String profile);
    
    
    /**
     * Returns the records of menu child for a giving id menu
     * @param id Integer: the id of the parent menu
     * @return Result<TestMenuRecord> with all the menu child data for a specific parent id menu
     */
    Result<TestMenuRecord> findChildren(Integer parentId, String profile);



}
