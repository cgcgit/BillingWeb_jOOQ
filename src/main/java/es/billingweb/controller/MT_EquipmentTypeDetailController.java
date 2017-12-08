/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_EquipmentTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtEquipmentType;
import es.billingweb.utils.BillingWebDates;
import es.billingweb.utils.BillingWebUtilities;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import es.billingweb.structure.ListedEquipmentTypeObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import es.billingweb.structure.ListedEditableEntityInterface;

/**
 *
 * Managed Bean to control the equipment type's detail page.
 *
 * @author catuxa
 */

@Named(value = "mT_EquipmentTypeDetailController")
@ViewScoped
public class MT_EquipmentTypeDetailController extends ListedEquipmentTypeObject implements Serializable, ListedEditableEntityInterface  {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    
    // Client Id of the UI
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

   
    // Id of the equipmentType to show and modify
    private static Integer equipmentTypeId;

    

    /**
     * List whit the equipmentType scope data
     */
    private static List<MtEquipmentType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtEquipmentType> filteredDataList;

    /**
     * Backup list to restore from changes
     */
    private static List<MtEquipmentType> dataListBackup;

    @EJB
    protected MT_EquipmentTypeEJBLocal ejbEquipmentType;
    
    /**
     * Selected data row in the table
     */
    @Inject
    private MtEquipmentType selectedData;


    //**** CONTROL VARIABLES (to manage the actions to do) ****//
   
    
    

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\   
    /**
     * Gets the selected data into the dataTable
     *
     * @return the equipment type record selected
     */
    public MtEquipmentType getSelectedData() {
        return selectedData;
    }

    /**
     * Sets the selected data into the dataTable
     *
     * @param selectedData the equipment type record to select
     */
    public void setSelectedData(MtEquipmentType selectedData) {
        this.selectedData = selectedData;
    }

    

    /**
     * Gets the list of the equipment types in the system
     *
     * @return the list of the equipment types in the system for the specific date
     */
    public List<MtEquipmentType> getDataList() {
        return dataList;
    }

    /**
     * Sets the list of the equipment types in the system
     *
     * @param dataList the list of the equipment types in the system
     */
    public void setDataList(List<MtEquipmentType> dataList) {
        MT_EquipmentTypeDetailController.dataList = dataList;       
    }

    /**
     * Gets the list of the equipment types in the system filtered by the specific
     * criterias
     *
     * @return the partial list of the equipment types, after the filters were
     * applied
     */
    public List<MtEquipmentType> getFilteredDataList() {
        return filteredDataList;
    }

    /**
     * Sets the list of the equipment types in the system filtered by the specific
     * criterias
     *
     * @param filteredDataList the partial list of the equipment type
     */
    public void setFilteredDataList(List<MtEquipmentType> filteredDataList) {
        MT_EquipmentTypeDetailController.filteredDataList = filteredDataList;
    }

   
    /**
     * Get the product_type_id to view/modify
     *
     * @return the product_type_id
     */
    public Integer getEquipmentTypeId() {
        return equipmentTypeId;
    }

    /**
     * Set the product_type_id to view/modify
     *
     * @param equipmentTypeId
     */
    public void setEquipmentTypeId(Integer equipmentTypeId) {
        MT_EquipmentTypeDetailController.equipmentTypeId = equipmentTypeId;

    }

    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtEquipmentType();
        }

        if (dataListBackup == null) {
            dataListBackup = new ArrayList<>();
        }

    }

    /**
     * Get the data of the equipmentType scope from database and put them into a
     * list.
     *
     * @return the list with the data equipmentType scope
     */
    @Override
    public String loadDataList() {
        String message = "LOAD EQUIPMENT TYPE DETAIL";
        String message_detail;

        MT_EquipmentTypeDetailController.dataList = ejbEquipmentType.findEquipmentTypeDetail(MT_EquipmentTypeDetailController.equipmentTypeId);

        if (dataList.isEmpty()) {
            message_detail = "No data to show";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);

        } else {
            message_detail = "Load data sucessful";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
        }
        return null;
    }

    /**
     * Sets the control variables to default value
     */
    @Override
    public void defaultValueControlVariables() {
        MT_EquipmentTypeDetailController.fromAddingRow = false;
        MT_EquipmentTypeDetailController.editingMode = false;
        MT_EquipmentTypeDetailController.toCancel = false;
        MT_EquipmentTypeDetailController.pretendPushEdit = false;
        MT_EquipmentTypeDetailController.disableSaveButton = true;
        MT_EquipmentTypeDetailController.prevStatusId = -1;

        this.fromDate_SD=null;
        this.fromDate_ED=null;
        this.fromDate_ID=null;
        this.fromDate_MD=null;
        
        this.toDate_SD=null;
        this.toDate_ED=null;
        this.toDate_ID=null;
        this.toDate_MD=null;
        
    }

    /**
     * Sets the init control variables to default value
     */
    @Override
    public void defaultValueControlVariablesIni() {
        defaultValueControlVariables();
        this.searchDate = BillingWebDates.getCurrentTimestamp();
        MT_EquipmentTypeDetailController.changes = false;
    }

//\\---------------//\\
//\\ EVENT METHODS //\\    
//\\---------------//\\ 
    /**
     * Init the row edit
     *
     * @param event
     */
    @Override
    public void onRowInit(RowEditEvent event) {

        MtEquipmentType dataRow;
        
        String message, message_detail, client_id;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the user to modify
        dataRow = (MtEquipmentType) event.getObject();

        // If we are editing a row, we must disabled all the other buttons
        MT_EquipmentTypeDetailController.editingMode = true;
        MT_EquipmentTypeDetailController.disableSaveButton = true;
        int totalRows = MT_EquipmentTypeDetailController.dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the addButtons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        try {
            if (MT_EquipmentTypeDetailController.pretendPushEdit) {
                // the edition of the row come from programatically order (i.e adding row)
                LOGGER.info("Editing the new row added");
            } else {
                // The user was pushed the edit button
                message_detail = "Editing equipment type " + dataRow.getEquipmentTypeCode() + " data";
                LOGGER.info(message_detail);

                // Store backup from the current data of the table
                BillingWebUtilities.copyGenericList(dataList, dataListBackup);
                // Sets the modified fields to the new values
                dataRow.setModifDate(BillingWebDates.getCurrentTimestamp());
                dataRow.setModifUser(CURRENT_USER_LOGIN);
                // Show an informative message
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            }
        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                message_detail = "PARSE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
        }
    }

    /**
     * Storage the data for the row edited. If the data has some invalid value,
     * it throws a ValidatorException
     *
     * @param event
     */
    @Override
    public void onRowEdit(RowEditEvent event) {
        MtEquipmentType dataRow;
        String message, message_detail;
        boolean r;
        boolean error=true;
        FacesMessage faces_message = new FacesMessage();
        message = "SAVE EDIT ROW";

        // Retrieved the data that was modified
        dataRow = (MtEquipmentType) event.getObject();

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        int pos = dataTable.getRowIndex();

        //Validates the data
        r = dataValidation(dataRow);

        if (r) {
            //Evaluates if the data was by adding or update
            if (MT_EquipmentTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for equipment type " + dataRow.getEquipmentTypeCode() + " has done";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {
                message_detail = "The changes for equipment type " + dataRow.getEquipmentTypeCode() + " has done";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            }

            // return the default values of the control variables
            defaultValueControlVariables();

            // Changes are made --> enabled saveButton
            MT_EquipmentTypeDetailController.changes = true;

            message_detail = "Storage the modified data sucessfully";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
        } else {
            message_detail = "ERROR - Data values are incorrect";
            faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
            faces_message.setSummary(message);
            faces_message.setDetail(message_detail);
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            throw new ValidatorException(faces_message);
        }
    }

    /**
     * Cancel the adding/editing row
     *
     * @param event
     */
    @Override
    public void onRowCancel(RowEditEvent event) {

        MtEquipmentType dataRow;
        String message, message_detail;

        message = "CANCEL ROW EDIT";

        // Retrieved the data that was modified
        dataRow = (MtEquipmentType) event.getObject();

        try {
            //Retrieve the backup data table
            //BillingWebUtilities.copyItUserList(userListDetailBackup, userListDetail);
            BillingWebUtilities.copyGenericList(MT_EquipmentTypeDetailController.dataListBackup, MT_EquipmentTypeDetailController.dataList);

            if (MT_EquipmentTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for equipment type " + dataRow.getEquipmentTypeCode() + " was cancelled";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {

                message_detail = "The changes for equipment type" + dataRow.getEquipmentTypeCode() + " was cancelled";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            }

            // return the default values of the control variables
            defaultValueControlVariables();
        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
        }
    }

    /**
     * Adds a new row for the dataTable
     */
    @Override
    public void addRow() {

        String message, message_detail;
        DataTable dataTable;
        int pos, totalRows;
        MtEquipmentType currentEquipmentType = null;
        MtEquipmentType nextEquipmentType = null;
        MtEquipmentType newEquipmentType = null;
        Timestamp inputDate;

        message = "ADD ROW";

        try {
            inputDate = BillingWebDates.getCurrentTimestamp();

            //Sets the fromAddingRow value to true
            MT_EquipmentTypeDetailController.fromAddingRow = true;

            // Store the current data
            BillingWebUtilities.copyGenericList(MT_EquipmentTypeDetailController.dataList, MT_EquipmentTypeDetailController.dataListBackup);

            //Gets the currentUser data
            dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
            pos = dataTable.getRowIndex();

            totalRows = dataTable.getRowCount();

            currentEquipmentType = (MtEquipmentType) dataTable.getRowData();

            //Create and initialise the equipment type to add to the new row
            newEquipmentType = new MtEquipmentType();
            newEquipmentType.from(currentEquipmentType);
            newEquipmentType.setStartDate(null);
            newEquipmentType.setInputDate(inputDate);
            newEquipmentType.setInputUser(CURRENT_USER_LOGIN);

            //Modify the mofify fields of the curren row        
            currentEquipmentType.setModifDate(inputDate);
            currentEquipmentType.setModifUser(CURRENT_USER_LOGIN);
            if (nextEquipmentType != null) {
                // If exists, modify the fields of the subsequent row
                nextEquipmentType.setModifDate(inputDate);
                nextEquipmentType.setModifUser(CURRENT_USER_LOGIN);
                nextEquipmentType.setEndDate(null);
            } else {
                //populates the endDate of the new row to the defaultEndDate
                newEquipmentType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
            }

            message_detail = "Adding new row for equipment type " + currentEquipmentType.getEquipmentTypeCode() + " data";

            // Adding the new row to the table
            pos = pos + 1;
            MT_EquipmentTypeDetailController.dataList.add(pos, newEquipmentType);
            dataTable.setRowIndex(pos);

            //Updates the table
            Ajax.update(dataTable.getClientId());

            // It pretends to press the edit button (to show the new row in editing mode)
            MT_EquipmentTypeDetailController.pretendPushEdit = true;
            // Push the edit button
            RequestContext.getCurrentInstance().execute("jQuery('span.ui-icon-pencil').eq(" + pos + ").each(function(){jQuery(this).click()});");
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                message_detail = "PARSE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
        }
    }

    /**
     * Deletes the selected row
     */
    @Override
    public void deleteRow() {

        DataTable dataTable;
        MtEquipmentType prevEquipmentType;
        String message, message_detail;
        int pos;

        message = "DELETE ROW";

        try {
            //Gets the currentUser data
            dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
            pos = dataTable.getRowIndex();
            if (pos > 0) {
                // if the row isn't the last row, set the endDate of the previous row to default endDate
                dataTable.setRowIndex(pos - 1);
                prevEquipmentType = (MtEquipmentType) dataTable.getRowData();
                prevEquipmentType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
                dataTable.setRowIndex(pos);
                message_detail = "The row was deleted and the End Date of the previous row was set to 31/12/9999";
            } else {
                message_detail = "The row was deleted. No rows exists to this user.";
            }
            // Delete the row
            MT_EquipmentTypeDetailController.dataList.remove(pos);

            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);

        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                message_detail = "PARSE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
        }
    }

    /**
     * Saves the changes into the database. It's supossed that the data was
     * validated on edit mode.
     */
    @Override
    public void saveChanges() {

        String message, message_detail;
        int r;

        message = "SAVE CHANGES INTO THE SISTEM";

        try {
            r = ejbEquipmentType.manageUpdateEquipmentTypeList(MT_EquipmentTypeDetailController.dataList);
            if (r == 1) {
                message_detail = "The changes was saved";

                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {
                message_detail = "The changes was not saved";

                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            }

        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebDataAccessException")) {
                message_detail = "DATABASE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
        }
    }

    /**
     * Evaluates if the new status is valid. If the previous status was cancel
     * the status can't change.
     *
     * @param e
     */
    @Override
    public void changeStatus(ValueChangeEvent e) {
        Integer oldStatusId = (Integer) e.getOldValue();
        Integer newStatusId = (Integer) e.getNewValue();
        Integer backupStatusId;

        // Gets the current User
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        MtEquipmentType equipmentType = (MtEquipmentType) dataTable.getRowData();

        // Gets the row of the current User
        String row = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("currentRow");
        this.setCurrentRow(Integer.parseInt(row));

        // Gets the status Id previous to the edition of the row
        backupStatusId = MT_EquipmentTypeDetailController.dataListBackup.get(this.getCurrentRow()).getStatusId();

        if (CANCEL_STATUS_EQUIPMENT_TYPE_ID.equals(backupStatusId)) {
            // The original status was cancel status --> not to change
            createMessage(FacesMessage.SEVERITY_ERROR, "STATUS ERROR", "The status CANCEL can not be changed");
            equipmentType.setStatusId(backupStatusId);
            Ajax.updateColumn(dataTable, dataTable.getRowIndex());
        } else {// The original status was not cancel --> could be change
            if (CANCEL_STATUS_EQUIPMENT_TYPE_ID.equals(newStatusId)) {
                // Change to CANCEL
                this.setToCancel(true);
                // Show confirmation dialog to change to cancel status
                RequestContext.getCurrentInstance().execute("PF('confirmCancelDialog').show();");
            } else if (MT_EquipmentTypeDetailController.prevStatusId > -1) {
                if (CANCEL_STATUS_EQUIPMENT_TYPE_ID.equals(MT_EquipmentTypeDetailController.prevStatusId)) {
                    // the previous selection into the edition was cancel --> revert changes
                    retrieveOldStatus();
                }
            }
        
            
            MT_EquipmentTypeDetailController.prevStatusId = newStatusId;
        }
    }

    /**
     * Sets the status of the current row and subsequents rows to cancel (OK
     * button ialog for cancelation)
     */
    @Override
    public void cancelStatusOK() {

        DataTable dataTable;
        int currentPos, lastPos, i;
        MtEquipmentType equipmentTypeToCancel;

        //Gets the currentUser data
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        currentPos = MT_EquipmentTypeDetailController.currentRow;
        lastPos = dataTable.getRowCount() - 1;

        if (this.isToCancel()) {
            if (currentPos < lastPos) {
                for (i = currentPos + 1; i <= lastPos; i++) {
                    dataTable.setRowIndex(i);
                    equipmentTypeToCancel = (MtEquipmentType) dataTable.getRowData();
                    equipmentTypeToCancel.setStatusId(CANCEL_STATUS_EQUIPMENT_TYPE_ID);
                    Ajax.updateRow(dataTable, i);
                }
            }
        }
    }

    /**
     * Retrieves the old status for all the rows in the table (KO button dialog
     * for cancelation)
     */
    @Override
    public void cancelStatusKO() {

        retrieveOldStatus();

    }
   
//\\---------------//\\
//\\ OTHER METHODS //\\    
//\\---------------//\\ 
    

    /**
     * Retrieve old status to the data
     */
    @Override
    public void retrieveOldStatus() {
        DataTable dataTable;
        int currentPos, lastPos;
        MtEquipmentType currentEquipmentType, equipmentTypeBackup;

        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        lastPos = dataTable.getRowCount() - 1;
        //currentPos = dataTable.getRowIndex();
        currentPos = MT_EquipmentTypeDetailController.currentRow;

        if (this.isToCancel()) {
            //Recovery the old values
            currentEquipmentType = (MtEquipmentType) dataTable.getRowData();
            //System.out.println("actualStatusId: " + currentUser.getStatusId());
            if (MT_EquipmentTypeDetailController.fromAddingRow) {
                //The old value of was equal to the previous row
                equipmentTypeBackup = MT_EquipmentTypeDetailController.dataListBackup.get(currentPos - 1);
                currentEquipmentType.setStatusId(equipmentTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, currentPos);
            }

            for (int i = currentPos; i <= lastPos; i++) {
                dataTable.setRowIndex(i);
                currentEquipmentType = (MtEquipmentType) dataTable.getRowData();
                equipmentTypeBackup = MT_EquipmentTypeDetailController.dataListBackup.get(i);
                currentEquipmentType.setStatusId(equipmentTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, i);
            }

        }
    }

   
    @Override
    public boolean dataValidation(Object entity) {
        //TODO
        String message, message_detail;
        MtEquipmentType objectToValidate;

        boolean result = true;

        message = "NEW EQUIPMENT TYPE";
        
        objectToValidate = (MtEquipmentType) entity;

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else {

            if (objectToValidate.getEquipmentTypeCode() != null) {
                objectToValidate.setEquipmentTypeCode(objectToValidate.getEquipmentTypeCode().trim());
            }
            if (objectToValidate.getDescription() != null) {
                objectToValidate.setDescription(objectToValidate.getDescription().trim());
            }
            if (objectToValidate.getInputUser() != null) {
                objectToValidate.setInputUser(objectToValidate.getInputUser().trim());
            }
            if (objectToValidate.getModifUser() != null) {
                objectToValidate.setModifUser(objectToValidate.getModifUser().trim());
            }

            if (!objectToValidate.getEntityTypeId().equals(ENTITY_TYPE_EQUIPMENT_TYPE_ID)) {
                message_detail = "ERROR - The entity type code must be PROD";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getEquipmentTypeCode() == null || objectToValidate.getEquipmentTypeCode().length() == 0) {
                message_detail = "ERROR - The code of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getEquipmentTypeCode().length() > 10) {
                message_detail = "Error - The code of the equipment type exceeds the limits of 10 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
                message_detail = "ERROR - The description of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getDescription().length() > 100) {
                message_detail = "Error - The description of the equipment type exceeds the limits of 100 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() == null) {
                message_detail = "ERROR - The start date of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getEndDate() == null) {
                message_detail = "ERROR - The end date of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() != null && objectToValidate.getEndDate() != null && objectToValidate.getStartDate().after(objectToValidate.getEndDate())) {
                message_detail = "ERROR - The start date of the equipment type must be less or equal that end date";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStatusId() == null) {
                message_detail = "ERROR - The status of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getBusinessScopeId() == null) {
                message_detail = "ERROR - The business scope of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getTechnologyScopeId() == null) {
                message_detail = "ERROR - The technology scope of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getInputDate() == null) {
                message_detail = "ERROR - The input date of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getInputUser() == null || objectToValidate.getInputUser().length() == 0) {
                message_detail = "ERROR - The input user of the equipment type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getInputUser().length() > 10) {
                message_detail = "Error - The input user for the equipment type exceeds the limits of 10 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }
        }

        return result;
    }

    @Override
    public void createNew() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetNewObjectValues() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushNewButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushResetButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushDeleteButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushAddButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
