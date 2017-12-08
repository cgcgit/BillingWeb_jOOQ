/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_ConsumptionTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtConsumptionType;
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
@Named(value = "mT_ConsumptionTypeController")
@ViewScoped
public class MT_ConsumptionTypeController extends ListedEntityObject implements Serializable, ListedEditableEntityInterface {

//\\-----------//\\
//\\ VARIABLES //\\    
//\\-----------//\\
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    // Data of the Logged User
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

    // Context
    //private FacesContext currentContext;
    //private RequestContext currentRequestContext;
    @Inject
    private MtConsumptionType injectConsumptionType;

    @EJB
    private MT_ConsumptionTypeEJBLocal ejbConsumptionType;

    /**
     * List whit the consumption type data
     */
    private static List<MtConsumptionType> dataList;
    
    /**
     * List whit the status data filtered
     */
    private static List<MtConsumptionType> filteredDataList;

    /**
     * selected data row in the table
     */
    @Inject
    private MtConsumptionType selectedData;

    /**
     * Backup data
     */
    private static MtConsumptionType backupData;

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\    
    public MtConsumptionType getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(MtConsumptionType selectedData) {
        this.selectedData = selectedData;
    }

    public MtConsumptionType getInjectConsumptionType() {
        return injectConsumptionType;
    }

    public void setInjectConsumptionType(MtConsumptionType injectConsumptionType) {
        this.injectConsumptionType = injectConsumptionType;
    }

    public List<MtConsumptionType> getDataList() {
        return dataList;
    }

    public void setDataList(List<MtConsumptionType> dataList) {
        MT_ConsumptionTypeController.dataList = dataList;
    }

    public MtConsumptionType getBackupData() {
        return backupData;
    }

    public void setBackupData(MtConsumptionType backupData) {
        MT_ConsumptionTypeController.backupData = backupData;
    }

    public static List<MtConsumptionType> getFilteredDataList() {
        return filteredDataList;
    }

    public static void setFilteredDataList(List<MtConsumptionType> filteredDataList) {
        MT_ConsumptionTypeController.filteredDataList = filteredDataList;
    }

    
    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtConsumptionType();
        }

        if (injectConsumptionType == null) {
            injectConsumptionType = new MtConsumptionType();
        }

        if (MT_ConsumptionTypeController.backupData == null) {
            MT_ConsumptionTypeController.backupData = new MtConsumptionType();
        }

    }

    /**
     * Get the data of the consumption type from database and put them into a
     * list.
     *
     * @return the list with the data consumption type
     */
    @Override
    public String loadDataList() {
        String message = "LOAD CONSUMPTION TYPE";
        String message_detail;
        dataList = ejbConsumptionType.findAllConsumptionType();

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
        MT_ConsumptionTypeController.editingMode = false;
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

        MtConsumptionType object;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the data to modify, for future restore
        object = (MtConsumptionType) event.getObject();
        MT_ConsumptionTypeController.backupData.from(object);

        // If we are editing a row, we must disabled all the other buttons
        MT_ConsumptionTypeController.editingMode = true;

        int totalRows = dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the other buttons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        message_detail = "Editing consumption type";
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
        MtConsumptionType dataRow;
        int r;
        boolean result;
        boolean error=true;
        String message, message_detail;

        message = "UPDATE CONSUMPTION TYPE";

        //Gets the current data row        
        dataRow = (MtConsumptionType) event.getObject();
        //Validates the data
        result = dataValidation(dataRow);

        if (result) {

            try {
                r = ejbConsumptionType.updateConsumptionType(dataRow);
                switch (r) {
                    case 1:
                        message_detail = "OK - The consumption type was updated";
                        LOGGER.info(message_detail);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        error=false;
                        break;
                    case 0:
                        message_detail = "KO - The consumption type was not updated";
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
                defaultValueControlVariables();
                if (error) {
                    dataRow.from(MT_ConsumptionTypeController.backupData);
                }
                Ajax.update(TABLE_CLIENT_ID);
            }

        } else {
            message_detail = "ERROR - Data values are incorrect";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            dataRow.from(MT_ConsumptionTypeController.backupData);
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
        MtConsumptionType dataRow;

        message = "CANCEL UPDATE ROW";

        dataRow = (MtConsumptionType) event.getObject();
        dataRow.from(MT_ConsumptionTypeController.backupData);

        message_detail = "Cancelled the edition of the consumption type";
        LOGGER.info(message_detail);

        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);

        defaultValueControlVariables();

        Ajax.update(TABLE_CLIENT_ID);
    }

//\\---------------//\\
//\\ OTHER METHODS //\\    
//\\---------------//\\   
    
     /**
     * Action to push the create button
     */
    public void pushNewConsumptionType() {
        resetNewObjectValues();
        RequestContext.getCurrentInstance().execute("PF('createDialog').show();");
        //RequestContext.getCurrentInstance().reset(CREATE_FORM_DATA_PANEL);
    }
    /**
     * Creates a new record
     */
    @Override
    public void createNew() {
        int r;
        boolean result;
        String message, message_detail;
        MtConsumptionType newObject;

        message = "NEW CONSUMPTION TYPE";
        message_detail = "";

        result = dataValidation(injectConsumptionType);

        if (result) {
            try {

                r = ejbConsumptionType.insertConsumptionType(injectConsumptionType);

                switch (r) {
                    case 1:
                        message_detail = "OK - The consumption type was created";
                        LOGGER.info(message_detail);
                        newObject = new MtConsumptionType();
                        newObject.from(injectConsumptionType);
                        dataList.add(newObject);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The consumption type was not created";
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
        MtConsumptionType rowData;
        DataTable dataTable;
        int row, r;
        String message, message_detail;

        message = "DELETE CONSUMPTION TYPE";

        //Gets the current data row
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        row = MT_ConsumptionTypeController.currentRow;
        dataTable.setRowIndex(row);
        rowData = (MtConsumptionType) dataTable.getRowData();

        if (rowData != null) {

            try {
                r = ejbConsumptionType.deleteConsumptionType(rowData);
                switch (r) {
                    case 1:
                        message_detail = "OK - The consumption type was deleted";
                        LOGGER.info(message_detail);
                        dataList.remove(row);
                        Ajax.update(TABLE_CLIENT_ID);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The consumption type was not deleted";
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
    
    @Override
    public void pushNewButton() {
        resetNewObjectValues();
        RequestContext.getCurrentInstance().execute("PF('createDialog').show();");
    }

    @Override
    public void resetNewObjectValues() {
        injectConsumptionType.setConsumptionTypeId(null);
        injectConsumptionType.setConsumptionTypeCode(null);
        injectConsumptionType.setDescription(null);
    }

    @Override
    public boolean dataValidation(Object entity) {
        String message, message_detail;
        MtConsumptionType objectToValidate;

        boolean result = true;

        message = "NEW CONSUMPTION TYPE VALIDATION";
        
        objectToValidate = (MtConsumptionType) entity;

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getConsumptionTypeCode() != null) {
            objectToValidate.setConsumptionTypeCode(objectToValidate.getConsumptionTypeCode().trim());
        }
        if (objectToValidate.getDescription() != null) {
            objectToValidate.setDescription(objectToValidate.getDescription().trim());
        }

        if (objectToValidate.getConsumptionTypeCode() == null || objectToValidate.getConsumptionTypeCode().length() == 0) {
            message_detail = "ERROR - The code of the consumption type can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getConsumptionTypeCode().length() > 10) {
            message_detail = "Error - The code of the consumption type exceeds the limits of 10 character";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        }

        if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
            message_detail = "ERROR - The description of the consumption type can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getDescription().length() > 100) {
            message_detail = "Error - The description of the consumption type exceeds the limits of 100 character";
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
