/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.structure;

import javax.faces.event.ValueChangeEvent;
import org.primefaces.event.RowEditEvent;


/**
 *
 * @author catuxa
 */
public interface ListedEditableEntityInterface  {
    
    /**
     * Sets the control variables to default value
     */
    public void defaultValueControlVariables();
    
    
    /**
     * Sets the init control variables to default value
     */
    public void defaultValueControlVariablesIni();
    
    
    /**
     * Get the data of the application level from database and put them into a
     * list.
     *
     * @return the list with the data application level
     */
    public String loadDataList();
    
    /**
     * Adds a new row for the dataTable
     */
    public void addRow();
    
    /**
     * Deletes the selected row
     */
    public void deleteRow();
    
    /**
     * Initializes the row edition
     *
     * @param event
     */
    public void onRowInit(RowEditEvent event) ;
    
    /**
     * Storages the data for the row edited. If the data has some invalid value,
     * it throws a ValidatorException
     *
     * @param event
     */
    public void onRowEdit(RowEditEvent event);
    
    /**
     * Cancels the adding/editing row
     *
     * @param event
     */
    public void onRowCancel(RowEditEvent event);
   
    
    /**
     * Evaluates if the new status is valid. If the previous status was cancel
     * the status can't change.
     *
     * @param e
     */
    public void changeStatus(ValueChangeEvent e);
    
    /**
     * Sets the status of the current row and subsequents rows to cancel (OK
     * button dialog for cancelation)
     */
    public void cancelStatusOK();
    
    /**
     * Retrieves the old status for all the rows in the table (KO button dialog
     * for cancelation)
     */
    public void cancelStatusKO();
    
    
    
    /**
     * Retrieve old status to the data
     */
    public void retrieveOldStatus();
    
    
    /**
     * Validates if the entity has all the correct fields.
     *
     * @param entity entity to validate
     * @return true: the entity is correct - false: otherwise
     */
    public boolean dataValidation(Object entity);
    
    /**
     * Creates a new entity record. 
     * It's stored inmediately into the database
     */
    public void createNew();
    
    
    /**
     * Reset the values of the new entity object to null valuesexcept those 
     * that must have a default value
     */
    public void resetNewObjectValues();
    

    
    /**
     * Saves the changes into the database. It's supossed that the data was
     * validated on edit mode.
     */
    public void saveChanges();
    
    /**
     * Action to push the create button
     */
    public void pushNewButton() ;
    
    
    /**
     * Clears the create dialog form
     */
    public void pushResetButton();
    
    /**
     * Push deleted button
     */
    public void pushDeleteButton();
            
            
   /**
     * Push deleted button
     */
    public void pushAddButton();          
    
   
}


