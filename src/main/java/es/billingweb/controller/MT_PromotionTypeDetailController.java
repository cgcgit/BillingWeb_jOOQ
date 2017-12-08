/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_PromotionTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtPromotionType;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import es.billingweb.structure.ListedPromotionTypeObject;
import es.billingweb.structure.ListedEditableEntityInterface;

/**
 *
 * Managed Bean to control the promotion type's detail page.
 *
 * @author catuxa
 */

@Named(value = "mT_PromotionTypeDetailController")
@ViewScoped
public class MT_PromotionTypeDetailController extends ListedPromotionTypeObject implements Serializable, ListedEditableEntityInterface  {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    
    // Client Id of the UI
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

   
    // Id of the promotionType to show and modify
    private static Integer promotionTypeId;

    

    /**
     * List whit the promotionType scope data
     */
    private static List<MtPromotionType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtPromotionType> filteredDataList;

    /**
     * Backup list to restore from changes
     */
    private static List<MtPromotionType> dataListBackup;

    @EJB
    protected MT_PromotionTypeEJBLocal ejbPromotionType;
    
    /**
     * Selected data row in the table
     */
    @Inject
    private MtPromotionType selectedData;


    //**** CONTROL VARIABLES (to manage the actions to do) ****//
   
    
    

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\   
    /**
     * Gets the selected data into the dataTable
     *
     * @return the promotion type record selected
     */
    public MtPromotionType getSelectedData() {
        return selectedData;
    }

    /**
     * Sets the selected data into the dataTable
     *
     * @param selectedData the promotion type record to select
     */
    public void setSelectedData(MtPromotionType selectedData) {
        this.selectedData = selectedData;
    }

    

    /**
     * Gets the list of the promotion types in the system
     *
     * @return the list of the promotion types in the system for the specific date
     */
    public List<MtPromotionType> getDataList() {
        return dataList;
    }

    /**
     * Sets the list of the promotion types in the system
     *
     * @param dataList the list of the promotion types in the system
     */
    public void setDataList(List<MtPromotionType> dataList) {
        MT_PromotionTypeDetailController.dataList = dataList;       
    }

    /**
     * Gets the list of the promotion types in the system filtered by the specific
     * criterias
     *
     * @return the partial list of the promotion types, after the filters were
     * applied
     */
    public List<MtPromotionType> getFilteredDataList() {
        return filteredDataList;
    }

    /**
     * Sets the list of the promotion types in the system filtered by the specific
     * criterias
     *
     * @param filteredDataList the partial list of the promotion type
     */
    public void setFilteredDataList(List<MtPromotionType> filteredDataList) {
        MT_PromotionTypeDetailController.filteredDataList = filteredDataList;
    }

   
    /**
     * Get the promotion_type_id to view/modify
     *
     * @return the promotion_type_id
     */
    public Integer getPromotionTypeId() {
        return promotionTypeId;
    }

    /**
     * Set the promotion_type_id to view/modify
     *
     * @param promotionTypeId
     */
    public void setPromotionTypeId(Integer promotionTypeId) {
        MT_PromotionTypeDetailController.promotionTypeId = promotionTypeId;

    }

    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtPromotionType();
        }

        if (dataListBackup == null) {
            dataListBackup = new ArrayList<>();
        }

    }

    /**
     * Get the data of the promotionType scope from database and put them into a
     * list.
     *
     * @return the list with the data promotionType scope
     */
    @Override
    public String loadDataList() {
        String message = "LOAD PROMOTION TYPE DETAIL";
        String message_detail;

        //dataList = ejbPromotionType.findAllPromotionType();
        MT_PromotionTypeDetailController.dataList = ejbPromotionType.findPromotionTypeDetail(MT_PromotionTypeDetailController.promotionTypeId);

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
        MT_PromotionTypeDetailController.fromAddingRow = false;
        MT_PromotionTypeDetailController.editingMode = false;
        MT_PromotionTypeDetailController.toCancel = false;
        MT_PromotionTypeDetailController.pretendPushEdit = false;
        MT_PromotionTypeDetailController.disableSaveButton = true;
        MT_PromotionTypeDetailController.prevStatusId = -1;

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
        MT_PromotionTypeDetailController.changes = false;
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
        MtPromotionType dataRow;
        String message, message_detail, client_id;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the user to modify
        dataRow = (MtPromotionType) event.getObject();

        // If we are editing a row, we must disabled all the other buttons
        MT_PromotionTypeDetailController.editingMode = true;
        MT_PromotionTypeDetailController.disableSaveButton = true;
        int totalRows = MT_PromotionTypeDetailController.dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the addButtons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        try {
            if (MT_PromotionTypeDetailController.pretendPushEdit) {
                // the edition of the row come from programatically order (i.e adding row)
                LOGGER.info("Editing the new row added");
            } else {
                // The user was pushed the edit button
                message_detail = "Editing promotion type " + dataRow.getPromotionTypeCode() + " data";
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
        MtPromotionType dataRow;
        String message, message_detail;
        boolean r;
        FacesMessage faces_message = new FacesMessage();
        message = "SAVE EDIT ROW";
        
        // Retrieved the data that was modified
        dataRow = (MtPromotionType) event.getObject();

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        int pos = dataTable.getRowIndex();

        //Validates the data
        r = dataValidation(dataRow);

        if (r) {
            //Evaluates if the data was by adding or update
            if (MT_PromotionTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for promotion type " + dataRow.getPromotionTypeCode() + " has done";

                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {
                message_detail = "The changes for promotion type " + dataRow.getPromotionTypeCode() + " has done";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            }

            // return the default values of the control variables
            defaultValueControlVariables();

            // Changes are made --> enabled saveButton
            MT_PromotionTypeDetailController.changes = true;

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

        MtPromotionType dataRow;
        String message, message_detail;

        message = "CANCEL ROW EDIT";

        // Retrieved the data that was modified
        dataRow = (MtPromotionType) event.getObject();

        try {
            //Retrieve the backup data table
            //BillingWebUtilities.copyItUserList(userListDetailBackup, userListDetail);
            BillingWebUtilities.copyGenericList(MT_PromotionTypeDetailController.dataListBackup, MT_PromotionTypeDetailController.dataList);

            if (MT_PromotionTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for promotion type " + dataRow.getPromotionTypeCode() + " was cancelled";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {

                message_detail = "The changes for promotion type" + dataRow.getPromotionTypeCode() + " was cancelled";
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
        MtPromotionType currentPromotionType = null;
        MtPromotionType nextPromotionType = null;
        MtPromotionType newPromotionType = null;
        Timestamp inputDate;

        message = "ADD ROW";

        try {
            inputDate = BillingWebDates.getCurrentTimestamp();

            //Sets the fromAddingRow value to true
            MT_PromotionTypeDetailController.fromAddingRow = true;

            // Store the current data
            BillingWebUtilities.copyGenericList(MT_PromotionTypeDetailController.dataList, MT_PromotionTypeDetailController.dataListBackup);

            //Gets the currentUser data
            dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
            pos = dataTable.getRowIndex();

            totalRows = dataTable.getRowCount();

            currentPromotionType = (MtPromotionType) dataTable.getRowData();

            //Create and initialise the promotion type to add to the new row
            newPromotionType = new MtPromotionType();
            newPromotionType.from(currentPromotionType);
            newPromotionType.setStartDate(null);
            newPromotionType.setInputDate(inputDate);
            newPromotionType.setInputUser(CURRENT_USER_LOGIN);

            //Modify the mofify fields of the curren row        
            currentPromotionType.setModifDate(inputDate);
            currentPromotionType.setModifUser(CURRENT_USER_LOGIN);
            if (nextPromotionType != null) {
                // If exists, modify the fields of the subsequent row
                nextPromotionType.setModifDate(inputDate);
                nextPromotionType.setModifUser(CURRENT_USER_LOGIN);
                nextPromotionType.setEndDate(null);
            } else {
                //populates the endDate of the new row to the defaultEndDate
                newPromotionType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
            }

            message_detail = "Adding new row for promotion type " + currentPromotionType.getPromotionTypeCode() + " data";

            // Adding the new row to the table
            pos = pos + 1;
            MT_PromotionTypeDetailController.dataList.add(pos, newPromotionType);
            dataTable.setRowIndex(pos);

            //Updates the table
            Ajax.update(dataTable.getClientId());

            // It pretends to press the edit button (to show the new row in editing mode)
            MT_PromotionTypeDetailController.pretendPushEdit = true;
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
        MtPromotionType prevPromotionType;
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
                prevPromotionType = (MtPromotionType) dataTable.getRowData();
                prevPromotionType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
                dataTable.setRowIndex(pos);
                message_detail = "The row was deleted and the End Date of the previous row was set to 31/12/9999";
            } else {
                message_detail = "The row was deleted. No rows exists to this user.";
            }
            // Delete the row
            MT_PromotionTypeDetailController.dataList.remove(pos);

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
            r = ejbPromotionType.manageUpdatePromotionTypeList(MT_PromotionTypeDetailController.dataList);
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
        MtPromotionType promotionType = (MtPromotionType) dataTable.getRowData();

        // Gets the row of the current User
        String row = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("currentRow");
        this.setCurrentRow(Integer.parseInt(row));

        // Gets the status Id previous to the edition of the row
        backupStatusId = MT_PromotionTypeDetailController.dataListBackup.get(this.getCurrentRow()).getStatusId();

        if (CANCEL_STATUS_PROMOTION_TYPE_ID.equals(backupStatusId)) {
            // The original status was cancel status --> not to change
            createMessage(FacesMessage.SEVERITY_ERROR, "STATUS ERROR", "The status CANCEL can not be changed");
            promotionType.setStatusId(backupStatusId);
            Ajax.updateColumn(dataTable, dataTable.getRowIndex());
        } else {// The original status was not cancel --> could be change
            if (CANCEL_STATUS_PROMOTION_TYPE_ID.equals(newStatusId)) {
                // Change to CANCEL
                this.setToCancel(true);
                // Show confirmation dialog to change to cancel status
                RequestContext.getCurrentInstance().execute("PF('confirmCancelDialog').show();");
            } else if (MT_PromotionTypeDetailController.prevStatusId > -1) {
                if (CANCEL_STATUS_PROMOTION_TYPE_ID.equals(MT_PromotionTypeDetailController.prevStatusId)) {
                    // the previous selection into the edition was cancel --> revert changes
                    retrieveOldStatus();
                }
            }
            
            MT_PromotionTypeDetailController.prevStatusId = newStatusId;
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
        MtPromotionType promotionTypeToCancel;

        //Gets the currentUser data
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        currentPos = MT_PromotionTypeDetailController.currentRow;
        lastPos = dataTable.getRowCount() - 1;

        if (this.isToCancel()) {
            if (currentPos < lastPos) {
                for (i = currentPos + 1; i <= lastPos; i++) {
                    dataTable.setRowIndex(i);
                    promotionTypeToCancel = (MtPromotionType) dataTable.getRowData();
                    promotionTypeToCancel.setStatusId(CANCEL_STATUS_PROMOTION_TYPE_ID);
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
        MtPromotionType currentPromotionType, promotionTypeBackup;

        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        lastPos = dataTable.getRowCount() - 1;
        //currentPos = dataTable.getRowIndex();
        currentPos = MT_PromotionTypeDetailController.currentRow;

        if (this.isToCancel()) {
            //Recovery the old values
            currentPromotionType = (MtPromotionType) dataTable.getRowData();
            //System.out.println("actualStatusId: " + currentUser.getStatusId());
            if (MT_PromotionTypeDetailController.fromAddingRow) {
                //The old value of was equal to the previous row
                promotionTypeBackup = MT_PromotionTypeDetailController.dataListBackup.get(currentPos - 1);
                currentPromotionType.setStatusId(promotionTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, currentPos);
            }

            for (int i = currentPos; i <= lastPos; i++) {
                dataTable.setRowIndex(i);
                currentPromotionType = (MtPromotionType) dataTable.getRowData();
                promotionTypeBackup = MT_PromotionTypeDetailController.dataListBackup.get(i);
                currentPromotionType.setStatusId(promotionTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, i);
            }

        }
    }

   
    @Override
    public boolean dataValidation(Object entity) {
        MtPromotionType objectToValidate;
        String message, message_detail;

        boolean result = true;

        message = "NEW PROMOTION TYPE";
        
        objectToValidate = (MtPromotionType) entity;

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else {

            if (objectToValidate.getPromotionTypeCode() != null) {
                objectToValidate.setPromotionTypeCode(objectToValidate.getPromotionTypeCode().trim());
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
            
            if (!objectToValidate.getEntityTypeId().equals(ENTITY_TYPE_PROMOTION_TYPE_ID)) {
                message_detail = "ERROR - The entity type code must be PROM";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getPromotionTypeCode() == null || objectToValidate.getPromotionTypeCode().length() == 0) {
                message_detail = "ERROR - The code of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getPromotionTypeCode().length() > 10) {
                message_detail = "Error - The code of the promotion type exceeds the limits of 10 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
                message_detail = "ERROR - The description of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getDescription().length() > 100) {
                message_detail = "Error - The description of the promotion type exceeds the limits of 100 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }
            
            if (objectToValidate.getVoucher()== null) {
                message_detail = "ERROR - The voucher value of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() == null) {
                message_detail = "ERROR - The start date of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getEndDate() == null) {
                message_detail = "ERROR - The end date of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() != null && objectToValidate.getEndDate() != null && objectToValidate.getStartDate().after(objectToValidate.getEndDate())) {
                message_detail = "ERROR - The start date of the promotion type must be less or equal that end date";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStatusId() == null) {
                message_detail = "ERROR - The status of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getBusinessScopeId() == null) {
                message_detail = "ERROR - The business scope of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getTechnologyScopeId() == null) {
                message_detail = "ERROR - The technology scope of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }
            
            if (objectToValidate.getVariable()== null) {
                message_detail = "ERROR - The variable value of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }
            
            if (objectToValidate.getMinDiscountValue()== null) {
                message_detail = "ERROR - The minimum discount value of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getMinDiscountValue() > 999999) {
                message_detail = "Error - The  minimum discount value must be between 0 and 999999";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }
            
            if (objectToValidate.getMaxDiscountValue()== null) {
                message_detail = "ERROR - The maximun discount value of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getMaxDiscountValue() > 999999) {
                message_detail = "Error - The  maximun discount value must be between 0 and 999999";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }
            
            if (objectToValidate.getMinDiscountValue()!= null && objectToValidate.getMaxDiscountValue()!= null 
                    && objectToValidate.getMinDiscountValue().compareTo(objectToValidate.getMaxDiscountValue())>0) {
                message_detail = "ERROR - The minimum discount value of the promotion type must be less or equal to the maximun discount value.";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
                
            }
                

            if (objectToValidate.getInputDate() == null) {
                message_detail = "ERROR - The input date of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getInputUser() == null || objectToValidate.getInputUser().length() == 0) {
                message_detail = "ERROR - The input user of the promotion type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getInputUser().length() > 10) {
                message_detail = "Error - The input user for the promotion type exceeds the limits of 10 character";
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
