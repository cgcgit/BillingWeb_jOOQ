/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_ServiceTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtServiceType;
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
import es.billingweb.structure.ListedServiceTypeObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import es.billingweb.structure.ListedEditableEntityInterface;

/**
 *
 * Managed Bean to control the service type's detail page.
 *
 * @author catuxa
 */

@Named(value = "mT_ServiceTypeDetailController")
@ViewScoped
public class MT_ServiceTypeDetailController extends ListedServiceTypeObject implements Serializable, ListedEditableEntityInterface  {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    
    // Client Id of the UI
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

   
    // Id of the serviceType to show and modify
    private static Integer serviceTypeId;

    

    /**
     * List whit the serviceType scope data
     */
    private static List<MtServiceType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtServiceType> filteredDataList;

    /**
     * Backup list to restore from changes
     */
    private static List<MtServiceType> dataListBackup;

    @EJB
    protected MT_ServiceTypeEJBLocal ejbServiceType;
    
    /**
     * Selected data row in the table
     */
    @Inject
    private MtServiceType selectedData;


    //**** CONTROL VARIABLES (to manage the actions to do) ****//
   
    
    

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\   
    /**
     * Gets the selected data into the dataTable
     *
     * @return the service type record selected
     */
    public MtServiceType getSelectedData() {
        return selectedData;
    }

    /**
     * Sets the selected data into the dataTable
     *
     * @param selectedData the service type record to select
     */
    public void setSelectedData(MtServiceType selectedData) {
        this.selectedData = selectedData;
    }

    

    /**
     * Gets the list of the service types in the system
     *
     * @return the list of the service types in the system for the specific date
     */
    public List<MtServiceType> getDataList() {
        return dataList;
    }

    /**
     * Sets the list of the service types in the system
     *
     * @param dataList the list of the service types in the system
     */
    public void setDataList(List<MtServiceType> dataList) {
        MT_ServiceTypeDetailController.dataList = dataList;       
    }

    /**
     * Gets the list of the service types in the system filtered by the specific
     * criterias
     *
     * @return the partial list of the service types, after the filters were
     * applied
     */
    public List<MtServiceType> getFilteredDataList() {
        return filteredDataList;
    }

    /**
     * Sets the list of the service types in the system filtered by the specific
     * criterias
     *
     * @param filteredDataList the partial list of the service type
     */
    public void setFilteredDataList(List<MtServiceType> filteredDataList) {
        MT_ServiceTypeDetailController.filteredDataList = filteredDataList;
    }

   
    /**
     * Get the service_type_id to view/modify
     *
     * @return the service_type_id
     */
    public Integer getServiceTypeId() {
        return serviceTypeId;
    }

    /**
     * Set the service_type_id to view/modify
     *
     * @param serviceTypeId
     */
    public void setServiceTypeId(Integer serviceTypeId) {
        MT_ServiceTypeDetailController.serviceTypeId = serviceTypeId;

    }

    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtServiceType();
        }

        if (dataListBackup == null) {
            dataListBackup = new ArrayList<>();
        }

    }

    /**
     * Get the data of the serviceType scope from database and put them into a
     * list.
     *
     * @return the list with the data serviceType scope
     */
    @Override
    public String loadDataList() {
        String message = "LOAD SERVICE TYPE DETAIL";
        String message_detail;

        //dataList = ejbServiceType.findAllServiceType();
        MT_ServiceTypeDetailController.dataList = ejbServiceType.findServiceTypeDetail(MT_ServiceTypeDetailController.serviceTypeId);

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
        MT_ServiceTypeDetailController.fromAddingRow = false;
        MT_ServiceTypeDetailController.editingMode = false;
        MT_ServiceTypeDetailController.toCancel = false;
        MT_ServiceTypeDetailController.pretendPushEdit = false;
        MT_ServiceTypeDetailController.disableSaveButton = true;
        MT_ServiceTypeDetailController.prevStatusId = -1;

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
        MT_ServiceTypeDetailController.changes = false;
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
        MtServiceType dataRow;
        String message, message_detail, client_id;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the user to modify
        dataRow = (MtServiceType) event.getObject();

        // If we are editing a row, we must disabled all the other buttons
        MT_ServiceTypeDetailController.editingMode = true;
        MT_ServiceTypeDetailController.disableSaveButton = true;
        int totalRows = MT_ServiceTypeDetailController.dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the addButtons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        try {
            if (MT_ServiceTypeDetailController.pretendPushEdit) {
                // the edition of the row come from programatically order (i.e adding row)
                LOGGER.info("Editing the new row added");
            } else {
                // The user was pushed the edit button
                message_detail = "Editing service type " + dataRow.getServiceTypeCode() + " data";
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
        MtServiceType dataRow;
        String message, message_detail;
        boolean r;
        FacesMessage faces_message = new FacesMessage();
        message = "SAVE EDIT ROW";
        
        // Retrieved the data that was modified
        dataRow = (MtServiceType) event.getObject();

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        int pos = dataTable.getRowIndex();

        //Validates the data
        r = dataValidation(dataRow);

        if (r) {
            //Evaluates if the data was by adding or update
            if (MT_ServiceTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for service type " + dataRow.getServiceTypeCode() + " has done";

                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {
                message_detail = "The changes for service type " + dataRow.getServiceTypeCode() + " has done";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            }

            // return the default values of the control variables
            defaultValueControlVariables();

            // Changes are made --> enabled saveButton
            MT_ServiceTypeDetailController.changes = true;

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

        MtServiceType dataRow;
        String message, message_detail;

        message = "CANCEL ROW EDIT";

        // Retrieved the data that was modified
        dataRow = (MtServiceType) event.getObject();

        try {
            //Retrieve the backup data table
            //BillingWebUtilities.copyItUserList(userListDetailBackup, userListDetail);
            BillingWebUtilities.copyGenericList(MT_ServiceTypeDetailController.dataListBackup, MT_ServiceTypeDetailController.dataList);

            if (MT_ServiceTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for service type " + dataRow.getServiceTypeCode() + " was cancelled";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {

                message_detail = "The changes for service type" + dataRow.getServiceTypeCode() + " was cancelled";
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
        MtServiceType currentServiceType = null;
        MtServiceType nextServiceType = null;
        MtServiceType newServiceType = null;
        Timestamp inputDate;

        message = "ADD ROW";

        try {
            inputDate = BillingWebDates.getCurrentTimestamp();

            //Sets the fromAddingRow value to true
            MT_ServiceTypeDetailController.fromAddingRow = true;

            // Store the current data
            BillingWebUtilities.copyGenericList(MT_ServiceTypeDetailController.dataList, MT_ServiceTypeDetailController.dataListBackup);

            //Gets the currentUser data
            dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
            pos = dataTable.getRowIndex();

            totalRows = dataTable.getRowCount();

            currentServiceType = (MtServiceType) dataTable.getRowData();

            //Create and initialise the service type to add to the new row
            newServiceType = new MtServiceType();
            newServiceType.from(currentServiceType);
            newServiceType.setStartDate(null);
            newServiceType.setInputDate(inputDate);
            newServiceType.setInputUser(CURRENT_USER_LOGIN);

            //Modify the mofify fields of the curren row        
            currentServiceType.setModifDate(inputDate);
            currentServiceType.setModifUser(CURRENT_USER_LOGIN);
            if (nextServiceType != null) {
                // If exists, modify the fields of the subsequent row
                nextServiceType.setModifDate(inputDate);
                nextServiceType.setModifUser(CURRENT_USER_LOGIN);
                nextServiceType.setEndDate(null);
            } else {
                //populates the endDate of the new row to the defaultEndDate
                newServiceType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
            }

            message_detail = "Adding new row for service type " + currentServiceType.getServiceTypeCode() + " data";

            // Adding the new row to the table
            pos = pos + 1;
            MT_ServiceTypeDetailController.dataList.add(pos, newServiceType);
            dataTable.setRowIndex(pos);

            //Updates the table
            Ajax.update(dataTable.getClientId());

            // It pretends to press the edit button (to show the new row in editing mode)
            MT_ServiceTypeDetailController.pretendPushEdit = true;
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
        MtServiceType prevServiceType;
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
                prevServiceType = (MtServiceType) dataTable.getRowData();
                prevServiceType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
                dataTable.setRowIndex(pos);
                message_detail = "The row was deleted and the End Date of the previous row was set to 31/12/9999";
            } else {
                message_detail = "The row was deleted. No rows exists to this user.";
            }
            // Delete the row
            MT_ServiceTypeDetailController.dataList.remove(pos);

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
            r = ejbServiceType.manageUpdateServiceTypeList(MT_ServiceTypeDetailController.dataList);
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
        MtServiceType serviceType = (MtServiceType) dataTable.getRowData();

        // Gets the row of the current User
        String row = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("currentRow");
        this.setCurrentRow(Integer.parseInt(row));

        // Gets the status Id previous to the edition of the row
        backupStatusId = MT_ServiceTypeDetailController.dataListBackup.get(this.getCurrentRow()).getStatusId();

        if (CANCEL_STATUS_SERVICE_TYPE_ID.equals(backupStatusId)) {
            // The original status was cancel status --> not to change
            createMessage(FacesMessage.SEVERITY_ERROR, "STATUS ERROR", "The status CANCEL can not be changed");
            serviceType.setStatusId(backupStatusId);
            Ajax.updateColumn(dataTable, dataTable.getRowIndex());
        } else {// The original status was not cancel --> could be change
            if (CANCEL_STATUS_SERVICE_TYPE_ID.equals(newStatusId)) {
                // Change to CANCEL
                this.setToCancel(true);
                // Show confirmation dialog to change to cancel status
                RequestContext.getCurrentInstance().execute("PF('confirmCancelDialog').show();");
            } else if (MT_ServiceTypeDetailController.prevStatusId > -1) {
                if (CANCEL_STATUS_SERVICE_TYPE_ID.equals(MT_ServiceTypeDetailController.prevStatusId)) {
                    // the previous selection into the edition was cancel --> revert changes
                    retrieveOldStatus();
                }
            }
            
            MT_ServiceTypeDetailController.prevStatusId = newStatusId;
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
        MtServiceType serviceTypeToCancel;

        //Gets the currentUser data
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        currentPos = MT_ServiceTypeDetailController.currentRow;
        lastPos = dataTable.getRowCount() - 1;

        if (this.isToCancel()) {
            if (currentPos < lastPos) {
                for (i = currentPos + 1; i <= lastPos; i++) {
                    dataTable.setRowIndex(i);
                    serviceTypeToCancel = (MtServiceType) dataTable.getRowData();
                    serviceTypeToCancel.setStatusId(CANCEL_STATUS_SERVICE_TYPE_ID);
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
        MtServiceType currentServiceType, serviceTypeBackup;

        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        lastPos = dataTable.getRowCount() - 1;
        //currentPos = dataTable.getRowIndex();
        currentPos = MT_ServiceTypeDetailController.currentRow;

        if (this.isToCancel()) {
            //Recovery the old values
            currentServiceType = (MtServiceType) dataTable.getRowData();
            //System.out.println("actualStatusId: " + currentUser.getStatusId());
            if (MT_ServiceTypeDetailController.fromAddingRow) {
                //The old value of was equal to the previous row
                serviceTypeBackup = MT_ServiceTypeDetailController.dataListBackup.get(currentPos - 1);
                currentServiceType.setStatusId(serviceTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, currentPos);
            }

            for (int i = currentPos; i <= lastPos; i++) {
                dataTable.setRowIndex(i);
                currentServiceType = (MtServiceType) dataTable.getRowData();
                serviceTypeBackup = MT_ServiceTypeDetailController.dataListBackup.get(i);
                currentServiceType.setStatusId(serviceTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, i);
            }

        }
    }

   
    @Override
    public boolean dataValidation(Object entity) {
        MtServiceType objectToValidate;
        String message, message_detail;

        boolean result = true;

        message = "NEW SERVICE TYPE";
        
        objectToValidate = (MtServiceType) entity;

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else {

            if (objectToValidate.getServiceTypeCode() != null) {
                objectToValidate.setServiceTypeCode(objectToValidate.getServiceTypeCode().trim());
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

            if (!objectToValidate.getEntityTypeId().equals(ENTITY_TYPE_SERVICE_TYPE_ID)) {
                message_detail = "ERROR - The entity type code must be PROD";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getServiceTypeCode() == null || objectToValidate.getServiceTypeCode().length() == 0) {
                message_detail = "ERROR - The code of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getServiceTypeCode().length() > 10) {
                message_detail = "Error - The code of the service type exceeds the limits of 10 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
                message_detail = "ERROR - The description of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getDescription().length() > 100) {
                message_detail = "Error - The description of the service type exceeds the limits of 100 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() == null) {
                message_detail = "ERROR - The start date of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getEndDate() == null) {
                message_detail = "ERROR - The end date of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() != null && objectToValidate.getEndDate() != null && objectToValidate.getStartDate().after(objectToValidate.getEndDate())) {
                message_detail = "ERROR - The start date of the service type must be less or equal that end date";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStatusId() == null) {
                message_detail = "ERROR - The status of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getBusinessScopeId() == null) {
                message_detail = "ERROR - The business scope of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getTechnologyScopeId() == null) {
                message_detail = "ERROR - The technology scope of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getInputDate() == null) {
                message_detail = "ERROR - The input date of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getInputUser() == null || objectToValidate.getInputUser().length() == 0) {
                message_detail = "ERROR - The input user of the service type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getInputUser().length() > 10) {
                message_detail = "Error - The input user for the service type exceeds the limits of 10 character";
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
