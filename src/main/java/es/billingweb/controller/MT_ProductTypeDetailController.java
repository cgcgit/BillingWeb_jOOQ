/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_ProductTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtProductType;
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
import es.billingweb.structure.ListedProductTypeObject;
import es.billingweb.structure.ListedEditableEntityInterface;

/**
 *
 * Managed Bean to control the product type's detail page.
 *
 * @author catuxa
 */

@Named(value = "mT_ProductTypeDetailController")
@ViewScoped
public class MT_ProductTypeDetailController extends ListedProductTypeObject implements Serializable, ListedEditableEntityInterface  {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    
    // Client Id of the UI
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

   
    // Id of the productType to show and modify
    private static Integer productTypeId;

    

    /**
     * List whit the productType scope data
     */
    private static List<MtProductType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtProductType> filteredDataList;

    /**
     * Backup list to restore from changes
     */
    private static List<MtProductType> dataListBackup;

    @EJB
    protected MT_ProductTypeEJBLocal ejbProductType;
    
    /**
     * Selected data row in the table
     */
    @Inject
    private MtProductType selectedData;


    //**** CONTROL VARIABLES (to manage the actions to do) ****//
   
    
    

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\   
    /**
     * Gets the selected data into the dataTable
     *
     * @return the product type record selected
     */
    public MtProductType getSelectedData() {
        return selectedData;
    }

    /**
     * Sets the selected data into the dataTable
     *
     * @param selectedData the product type record to select
     */
    public void setSelectedData(MtProductType selectedData) {
        this.selectedData = selectedData;
    }

    

    /**
     * Gets the list of the product types in the system
     *
     * @return the list of the product types in the system for the specific date
     */
    public List<MtProductType> getDataList() {
        return dataList;
    }

    /**
     * Sets the list of the product types in the system
     *
     * @param dataList the list of the product types in the system
     */
    public void setDataList(List<MtProductType> dataList) {
        MT_ProductTypeDetailController.dataList = dataList;       
    }

    /**
     * Gets the list of the product types in the system filtered by the specific
     * criterias
     *
     * @return the partial list of the product types, after the filters were
     * applied
     */
    public List<MtProductType> getFilteredDataList() {
        return filteredDataList;
    }

    /**
     * Sets the list of the product types in the system filtered by the specific
     * criterias
     *
     * @param filteredDataList the partial list of the product type
     */
    public void setFilteredDataList(List<MtProductType> filteredDataList) {
        MT_ProductTypeDetailController.filteredDataList = filteredDataList;
    }

   
    /**
     * Get the product_type_id to view/modify
     *
     * @return the product_type_id
     */
    public Integer getProductTypeId() {
        return productTypeId;
    }

    /**
     * Set the product_type_id to view/modify
     *
     * @param productTypeId
     */
    public void setProductTypeId(Integer productTypeId) {
        MT_ProductTypeDetailController.productTypeId = productTypeId;

    }

    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtProductType();
        }

        if (dataListBackup == null) {
            dataListBackup = new ArrayList<>();
        }

    }

    /**
     * Get the data of the productType scope from database and put them into a
     * list.
     *
     * @return the list with the data productType scope
     */
    @Override
    public String loadDataList() {
        String message = "LOAD PRODUCT TYPE DETAIL";
        String message_detail;

        //dataList = ejbProductType.findAllProductType();
        MT_ProductTypeDetailController.dataList = ejbProductType.findProductTypeDetail(MT_ProductTypeDetailController.productTypeId);

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
        MT_ProductTypeDetailController.fromAddingRow = false;
        MT_ProductTypeDetailController.editingMode = false;
        MT_ProductTypeDetailController.toCancel = false;
        MT_ProductTypeDetailController.pretendPushEdit = false;
        MT_ProductTypeDetailController.disableSaveButton = true;
        MT_ProductTypeDetailController.prevStatusId = -1;

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
        MT_ProductTypeDetailController.changes = false;
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
        MtProductType dataRow;
        String message, message_detail, client_id;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the user to modify
        dataRow = (MtProductType) event.getObject();

        // If we are editing a row, we must disabled all the other buttons
        MT_ProductTypeDetailController.editingMode = true;
        MT_ProductTypeDetailController.disableSaveButton = true;
        int totalRows = MT_ProductTypeDetailController.dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the addButtons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        try {
            if (MT_ProductTypeDetailController.pretendPushEdit) {
                // the edition of the row come from programatically order (i.e adding row)
                LOGGER.info("Editing the new row added");
            } else {
                // The user was pushed the edit button
                message_detail = "Editing product type " + dataRow.getProductTypeCode() + " data";
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
        MtProductType dataRow;
        String message, message_detail;
        boolean r;
        FacesMessage faces_message = new FacesMessage();
        message = "SAVE EDIT ROW";
        
        // Retrieved the data that was modified
        dataRow = (MtProductType) event.getObject();

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        int pos = dataTable.getRowIndex();

        //Validates the data
        r = dataValidation(dataRow);

        if (r) {
            //Evaluates if the data was by adding or update
            if (MT_ProductTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for product type " + dataRow.getProductTypeCode() + " has done";

                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {
                message_detail = "The changes for product type " + dataRow.getProductTypeCode() + " has done";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            }

            // return the default values of the control variables
            defaultValueControlVariables();

            // Changes are made --> enabled saveButton
            MT_ProductTypeDetailController.changes = true;

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

        MtProductType dataRow;
        String message, message_detail;

        message = "CANCEL ROW EDIT";

        // Retrieved the data that was modified
        dataRow = (MtProductType) event.getObject();

        try {
            //Retrieve the backup data table
            //BillingWebUtilities.copyItUserList(userListDetailBackup, userListDetail);
            BillingWebUtilities.copyGenericList(MT_ProductTypeDetailController.dataListBackup, MT_ProductTypeDetailController.dataList);

            if (MT_ProductTypeDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for product type " + dataRow.getProductTypeCode() + " was cancelled";
                LOGGER.info(message_detail);
                createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            } else {

                message_detail = "The changes for product type" + dataRow.getProductTypeCode() + " was cancelled";
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
        MtProductType currentProductType = null;
        MtProductType nextProductType = null;
        MtProductType newProductType = null;
        Timestamp inputDate;

        message = "ADD ROW";

        try {
            inputDate = BillingWebDates.getCurrentTimestamp();

            //Sets the fromAddingRow value to true
            MT_ProductTypeDetailController.fromAddingRow = true;

            // Store the current data
            BillingWebUtilities.copyGenericList(MT_ProductTypeDetailController.dataList, MT_ProductTypeDetailController.dataListBackup);

            //Gets the currentUser data
            dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
            pos = dataTable.getRowIndex();

            totalRows = dataTable.getRowCount();

            currentProductType = (MtProductType) dataTable.getRowData();

            //Create and initialise the product type to add to the new row
            newProductType = new MtProductType();
            newProductType.from(currentProductType);
            newProductType.setStartDate(null);
            newProductType.setInputDate(inputDate);
            newProductType.setInputUser(CURRENT_USER_LOGIN);

            //Modify the mofify fields of the curren row        
            currentProductType.setModifDate(inputDate);
            currentProductType.setModifUser(CURRENT_USER_LOGIN);
            if (nextProductType != null) {
                // If exists, modify the fields of the subsequent row
                nextProductType.setModifDate(inputDate);
                nextProductType.setModifUser(CURRENT_USER_LOGIN);
                nextProductType.setEndDate(null);
            } else {
                //populates the endDate of the new row to the defaultEndDate
                newProductType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
            }

            message_detail = "Adding new row for product type " + currentProductType.getProductTypeCode() + " data";

            // Adding the new row to the table
            pos = pos + 1;
            MT_ProductTypeDetailController.dataList.add(pos, newProductType);
            dataTable.setRowIndex(pos);

            //Updates the table
            Ajax.update(dataTable.getClientId());

            // It pretends to press the edit button (to show the new row in editing mode)
            MT_ProductTypeDetailController.pretendPushEdit = true;
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
        MtProductType prevProductType;
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
                prevProductType = (MtProductType) dataTable.getRowData();
                prevProductType.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
                dataTable.setRowIndex(pos);
                message_detail = "The row was deleted and the End Date of the previous row was set to 31/12/9999";
            } else {
                message_detail = "The row was deleted. No rows exists to this user.";
            }
            // Delete the row
            MT_ProductTypeDetailController.dataList.remove(pos);

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
            r = ejbProductType.manageUpdateProductTypeList(MT_ProductTypeDetailController.dataList);
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
        MtProductType productType = (MtProductType) dataTable.getRowData();

        // Gets the row of the current User
        String row = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("currentRow");
        this.setCurrentRow(Integer.parseInt(row));

        // Gets the status Id previous to the edition of the row
        backupStatusId = MT_ProductTypeDetailController.dataListBackup.get(this.getCurrentRow()).getStatusId();

        if (CANCEL_STATUS_PRODUCT_TYPE_ID.equals(backupStatusId)) {
            // The original status was cancel status --> not to change
            createMessage(FacesMessage.SEVERITY_ERROR, "STATUS ERROR", "The status CANCEL can not be changed");
            productType.setStatusId(backupStatusId);
            Ajax.updateColumn(dataTable, dataTable.getRowIndex());
        } else {// The original status was not cancel --> could be change
            if (CANCEL_STATUS_PRODUCT_TYPE_ID.equals(newStatusId)) {
                // Change to CANCEL
                this.setToCancel(true);
                // Show confirmation dialog to change to cancel status
                RequestContext.getCurrentInstance().execute("PF('confirmCancelDialog').show();");
            } else if (MT_ProductTypeDetailController.prevStatusId > -1) {
                if (CANCEL_STATUS_PRODUCT_TYPE_ID.equals(MT_ProductTypeDetailController.prevStatusId)) {
                    // the previous selection into the edition was cancel --> revert changes
                    retrieveOldStatus();
                }
            }
            
            MT_ProductTypeDetailController.prevStatusId = newStatusId;
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
        MtProductType productTypeToCancel;

        //Gets the currentUser data
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        currentPos = MT_ProductTypeDetailController.currentRow;
        lastPos = dataTable.getRowCount() - 1;

        if (this.isToCancel()) {
            if (currentPos < lastPos) {
                for (i = currentPos + 1; i <= lastPos; i++) {
                    dataTable.setRowIndex(i);
                    productTypeToCancel = (MtProductType) dataTable.getRowData();
                    productTypeToCancel.setStatusId(CANCEL_STATUS_PRODUCT_TYPE_ID);
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
        MtProductType currentProductType, productTypeBackup;

        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        lastPos = dataTable.getRowCount() - 1;
        //currentPos = dataTable.getRowIndex();
        currentPos = MT_ProductTypeDetailController.currentRow;

        if (this.isToCancel()) {
            //Recovery the old values
            currentProductType = (MtProductType) dataTable.getRowData();
            //System.out.println("actualStatusId: " + currentUser.getStatusId());
            if (MT_ProductTypeDetailController.fromAddingRow) {
                //The old value of was equal to the previous row
                productTypeBackup = MT_ProductTypeDetailController.dataListBackup.get(currentPos - 1);
                currentProductType.setStatusId(productTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, currentPos);
            }

            for (int i = currentPos; i <= lastPos; i++) {
                dataTable.setRowIndex(i);
                currentProductType = (MtProductType) dataTable.getRowData();
                productTypeBackup = MT_ProductTypeDetailController.dataListBackup.get(i);
                currentProductType.setStatusId(productTypeBackup.getStatusId());
                Ajax.updateRow(dataTable, i);
            }

        }
    }

   
    @Override
    public boolean dataValidation(Object entity) {
        MtProductType objectToValidate;
        String message, message_detail;

        boolean result = true;

        message = "NEW PRODUCT TYPE";
        
        objectToValidate = (MtProductType) entity;

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else {

            if (objectToValidate.getProductTypeCode() != null) {
                objectToValidate.setProductTypeCode(objectToValidate.getProductTypeCode().trim());
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

            if (!objectToValidate.getEntityTypeId().equals(ENTITY_TYPE_PRODUCT_TYPE_ID)) {
                message_detail = "ERROR - The entity type code must be PROD";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getProductTypeCode() == null || objectToValidate.getProductTypeCode().length() == 0) {
                message_detail = "ERROR - The code of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getProductTypeCode().length() > 10) {
                message_detail = "Error - The code of the product type exceeds the limits of 10 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
                message_detail = "ERROR - The description of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getDescription().length() > 100) {
                message_detail = "Error - The description of the product type exceeds the limits of 100 character";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() == null) {
                message_detail = "ERROR - The start date of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getEndDate() == null) {
                message_detail = "ERROR - The end date of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStartDate() != null && objectToValidate.getEndDate() != null && objectToValidate.getStartDate().after(objectToValidate.getEndDate())) {
                message_detail = "ERROR - The start date of the product type must be less or equal that end date";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getStatusId() == null) {
                message_detail = "ERROR - The status of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getBusinessScopeId() == null) {
                message_detail = "ERROR - The business scope of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getTechnologyScopeId() == null) {
                message_detail = "ERROR - The technology scope of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getInputDate() == null) {
                message_detail = "ERROR - The input date of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            }

            if (objectToValidate.getInputUser() == null || objectToValidate.getInputUser().length() == 0) {
                message_detail = "ERROR - The input user of the product type can not be null";
                LOGGER.error(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                result = false;
            } else if (objectToValidate.getInputUser().length() > 10) {
                message_detail = "Error - The input user for the product type exceeds the limits of 10 character";
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
