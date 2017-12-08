/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_StatusEJBLocal;
import es.billingweb.model.tables.pojos.MtStatus;
import es.billingweb.structure.ListedEntityObject;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import es.billingweb.structure.ListedEditableEntityInterface;

/**
 *
 * @author catuxa
 */
@Named(value = "mT_StatusController")
@ViewScoped
public class MT_StatusController extends ListedEntityObject implements Serializable, ListedEditableEntityInterface {

//\\-----------//\\
//\\ VARIABLES //\\    
//\\-----------//\\
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

    // To manage the create dialog
    @Inject
    private MtStatus injectStatus;

    
    @EJB
    private MT_StatusEJBLocal ejbStatusPrivate;

    /**
     * List whit the status data
     */
    private static List<MtStatus> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtStatus> filteredDataList;

    /**
     * selected data row in the table
     */
    @Inject
    private MtStatus selectedData;

    /**
     * Backup data
     */
    private static MtStatus backupData;

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\    
    public MtStatus getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(MtStatus selectedData) {
        this.selectedData = selectedData;
    }

    public MtStatus getInjectStatus() {
        return injectStatus;
    }

    public void setInjectStatus(MtStatus injectStatus) {
        this.injectStatus = injectStatus;
    }

    public List<MtStatus> getDataList() {
        return dataList;
    }

    public void setDataList(List<MtStatus> dataList) {
        MT_StatusController.dataList = dataList;
    }

    public List<MtStatus> getFilteredDataList() {
        return filteredDataList;
    }

    public void setFilteredDataList(List<MtStatus> filteredDataList) {
        MT_StatusController.filteredDataList = filteredDataList;
    }

    public MtStatus getBackupData() {
        return backupData;
    }

    public void setBackupData(MtStatus backupData) {
        MT_StatusController.backupData = backupData;
    }

//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtStatus();
        }

        if (injectStatus == null) {
            injectStatus = new MtStatus();
        }

        if (MT_StatusController.backupData == null) {
            MT_StatusController.backupData = new MtStatus();
        }

    }

    /**
     * Get the data of the status from database and put them into a list.
     *
     * @return the list with the data status
     */
    @Override
    public String loadDataList() {
        String message = "LOAD STATUS";
        String message_detail;
        dataList = ejbStatusPrivate.findAllStatus();

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
        MT_StatusController.editingMode = false;
    }

    /**
     * Sets the init control variables to default value
     */
    @Override
    public void defaultValueControlVariablesIni() {
        defaultValueControlVariables();
    }

//\\----------------------//\\
//\\ ACTION EVENT METHODS //\\    
//\\----------------------//\\    
    /**
     * Init the row edit
     *
     * @param event
     */
    @Override
    public void onRowInit(RowEditEvent event) {

        String message, message_detail;

        MtStatus dataRow;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the data to modify, for future restore
        dataRow = (MtStatus) event.getObject();
        MT_StatusController.backupData.from(dataRow);

        // If we are editing a row, we must disabled all the other buttons
        MT_StatusController.editingMode = true;

        int totalRows = dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the other buttons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        message_detail = "Editing status";
        LOGGER.info(message_detail);

        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);

    }

    /**
     * Storage the data for the row edited
     *
     * @param event
     */
    @Override
    public void onRowEdit(RowEditEvent event) {
        MtStatus dataRow;        
        int r;
        boolean result;
        boolean error=true;
        String message, message_detail;

        message = "UPDATE STATUS";

        //Gets the current data row
        //dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        dataRow = (MtStatus) event.getObject();

        //Validates the data
        result = dataValidation(dataRow);

        if (result) {
            try {
                r = ejbStatusPrivate.updateStatus(dataRow);
                switch (r) {
                    case 1:
                        message_detail = "OK - The status was updated";
                        LOGGER.info(message_detail);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        error=false;
                        break;
                    case 0:
                        message_detail = "KO - The status was not updated";
                        LOGGER.error(message_detail);
                        createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);                        
                        break;
                    default:
                        message_detail = "KO - The result of the updating whas not as expected";
                        LOGGER.error(message_detail);
                        createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);                        
                }
            } catch (EJBException e) {
                Exception ne = (Exception) e.getCause();
                if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                    message_detail = "PARSE ERROR - " + ne.getMessage();
                    LOGGER.fatal(message_detail);
                    createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);
                    dataRow.from(MT_StatusController.backupData);
                } else {
                    message_detail = "ERROR - " + ne.getMessage();
                    LOGGER.fatal(message_detail);
                    createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);
                    dataRow.from(MT_StatusController.backupData);
                }
            } catch (Exception e) {
                message_detail = "ERROR - " + e.getCause().toString();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);
            } finally {
                defaultValueControlVariables();
                if (error) {
                    dataRow.from(MT_StatusController.backupData);
                }
                Ajax.update(TABLE_CLIENT_ID);
            }
        } else {
            message_detail = "ERROR - Data values are incorrect";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            dataRow.from(MT_StatusController.backupData);
            defaultValueControlVariables();
            Ajax.update(TABLE_CLIENT_ID);
        }
    }

    /**
     * Cancel the adding/editing row
     *
     * @param event
     */
    @Override
    public void onRowCancel(RowEditEvent event) {
        String message, message_detail;
        MtStatus dataRow;

        message = "CANCEL UPDATE ROW";

        dataRow = (MtStatus) event.getObject();
        dataRow.from(MT_StatusController.backupData);

        message_detail = "Cancelled the edition of the status";
        LOGGER.info(message_detail);

        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);

        defaultValueControlVariables();

        Ajax.update(TABLE_CLIENT_ID);
    }

    public void changeEntityType(ValueChangeEvent e) {
        Integer oldEntityTypeId = (Integer) e.getOldValue();
        Integer newEntityTypeId = (Integer) e.getNewValue();
        Integer backupEntityTypeId;

        // Gets the current User
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        MtStatus dataRow = (MtStatus) dataTable.getRowData();

        // Gets the row of the current User
        //String row = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("currentRowEntityId");
        //this.setCurrentRow(Integer.parseInt(row));

        // Gets the status Id previous to the edition of the row
        backupEntityTypeId = backupData.getEntityTypeId();
        dataRow.setEntityTypeId(backupEntityTypeId);
        Ajax.updateColumn(dataTable, dataTable.getRowIndex());
    }

//\\---------------//\\
//\\ OTHER METHODS //\\    
//\\---------------//\\   
    /**
     * Creates a new record
     */
    @Override
    public void createNew() {
        int r;
        boolean result;
        String message, message_detail;
        MtStatus newObject;

        message = "NEW STATUS";
        message_detail = "";

        result = dataValidation(injectStatus);

        if (result) {
            try {

                r = ejbStatusPrivate.insertStatus(injectStatus);

                switch (r) {
                    case 1:
                        message_detail = "OK - The status was created";
                        LOGGER.info(message_detail);
                        newObject = new MtStatus();
                        newObject.from(injectStatus);
                        dataList.add(newObject);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The status was not created";
                        LOGGER.error(message_detail);
                        createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                        break;
                    default:
                        message_detail = "KO - The result of the creation whas not as expected";
                        LOGGER.error(message_detail);
                        createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                }

            } catch (EJBException e) {
                Exception ne = (Exception) e.getCause();
                if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                    message_detail = "PARSE ERROR - " + ne.getMessage();
                    LOGGER.fatal(message_detail);
                    createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);

                } else {
                    message_detail = "ERROR - " + ne.getMessage();
                    LOGGER.fatal(message_detail);
                    createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);
                }

            } catch (Exception e) {
                message_detail = "ERROR - " + e.getCause().toString();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);
            } finally {
                RequestContext.getCurrentInstance().execute("PF('createDialog').hide();");
                RequestContext.getCurrentInstance().execute("PF('filteredTable').filter();");
                //Ajax.update(TABLE_CLIENT_ID);

            }
        } else {
            message_detail = "EMPTY OR INVALID DATA - Please fill out the form correctly";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
        }

        pushResetButton();
    }

    /**
     * Push deleted button
     */
    @Override
    public void pushDeleteButton() {
        // Gets the row of the current User
        String row = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("currentRow");
        this.setCurrentRow(Integer.parseInt(row));

    }

    /**
     * Deletes the selected row
     */
    @Override
    public void deleteRow() {
        MtStatus rowData;
        DataTable dataTable;
        int row, r;
        String message, message_detail;

        message = "DELETE STATUS";

        //Gets the current data row
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        row = MT_StatusController.currentRow;
        dataTable.setRowIndex(row);
        rowData = (MtStatus) dataTable.getRowData();

        if (rowData != null) {

            try {
                r = ejbStatusPrivate.deleteStatus(rowData);
                switch (r) {
                    case 1:
                        message_detail = "OK - The status was deleted";
                        LOGGER.info(message_detail);
                        dataList.remove(row);
                        Ajax.update(TABLE_CLIENT_ID);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The status was not deleted";
                        LOGGER.error(message_detail);
                        createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                        break;
                    default:
                        message_detail = "KO - The result of the deletion whas not as expected";
                        LOGGER.error(message_detail);
                        createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
                }
            } catch (EJBException e) {
                Exception ne = (Exception) e.getCause();
                if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                    message_detail = "PARSE ERROR - " + ne.getMessage();
                    LOGGER.fatal(message_detail);
                    createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);

                } else {
                    message_detail = "ERROR - " + ne.getMessage();
                    LOGGER.fatal(message_detail);
                    createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);
                }
            } catch (Exception e) {
                message_detail = "ERROR - " + e.getCause().toString();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_FATAL, message, message_detail);
            }
        } else {
            message_detail = "No selected data to delete";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
        }

    }

    @Override
    public void pushResetButton() {
        resetNewObjectValues();
        Ajax.update(CREATE_FORM_DATA_PANEL);
        //RequestContext.getCurrentInstance().reset(CREATE_FORM_DATA_PANEL);
    }
    
    /**
     * Action to push the create button
     */
    @Override
    public void pushNewButton() {
        resetNewObjectValues();
        RequestContext.getCurrentInstance().execute("PF('createDialog').show();");
        //RequestContext.getCurrentInstance().reset(CREATE_FORM_DATA_PANEL);
    }

    @Override
    public void resetNewObjectValues() {
        injectStatus.setStatusId(null);
        injectStatus.setStatusCode(null);
        injectStatus.setDescription(null);
    }

    @Override
    public boolean dataValidation(Object entity) {
        String message, message_detail;
        MtStatus objectToValidate;
        
        boolean result = true;

        message = "NEW STATUS VALIDATION";
        
        objectToValidate = (MtStatus) entity;
        

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getStatusCode() != null) {
            objectToValidate.setStatusCode(objectToValidate.getStatusCode().trim());
        }
        if (objectToValidate.getDescription() != null) {
            objectToValidate.setDescription(objectToValidate.getDescription().trim());
        }

        if (objectToValidate.getStatusCode() == null || objectToValidate.getStatusCode().length() == 0) {
            message_detail = "ERROR - The code of the status can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getStatusCode().length() > 10) {
            message_detail = "Error - The code of the status exceeds the limits of 10 character";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        }

        if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
            message_detail = "ERROR - The description of the status can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getDescription().length() > 100) {
            message_detail = "Error - The description of the status exceeds the limits of 100 character";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        }

        Ajax.update(CREATE_FORM_DATA_PANEL);

        return result;
    }

    @Override
    public void addRow() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeStatus(ValueChangeEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelStatusOK() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelStatusKO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void retrieveOldStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveChanges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushAddButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
