/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_ApplicationUnitEJBLocal;
import es.billingweb.model.tables.pojos.MtApplicationUnit;
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
@Named(value = "mT_ApplicationUnitController")
@ViewScoped
public class MT_ApplicationUnitController extends ListedEntityObject implements Serializable, ListedEditableEntityInterface {

//\\-----------//\\
//\\ VARIABLES //\\    
//\\-----------//\\
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

    // Context
    //private FacesContext currentContext;
    //private RequestContext currentRequestContext;
    @Inject
    private MtApplicationUnit injectApplicationUnit;

    @EJB
    private MT_ApplicationUnitEJBLocal ejbApplicationUnit;

    /**
     * List whit the application unit data
     */
    private static List<MtApplicationUnit> dataList;
    
    /**
     * List whit the status data filtered
     */
    private static List<MtApplicationUnit> filteredDataList;

    /**
     * selected data row in the table
     */
    @Inject
    private MtApplicationUnit selectedData;

    /**
     * Backup data
     */
    private static MtApplicationUnit backupData;

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\    
    public MtApplicationUnit getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(MtApplicationUnit selectedData) {
        this.selectedData = selectedData;
    }

    public MtApplicationUnit getInjectApplicationUnit() {
        return injectApplicationUnit;
    }

    public void setInjectApplicationUnit(MtApplicationUnit injectApplicationUnit) {
        this.injectApplicationUnit = injectApplicationUnit;
    }

    public List<MtApplicationUnit> getDataList() {
        return dataList;
    }

    public void setDataList(List<MtApplicationUnit> dataList) {
        MT_ApplicationUnitController.dataList = dataList;
    }

    public MtApplicationUnit getBackupData() {
        return backupData;
    }

    public void setBackupData(MtApplicationUnit backupData) {
        MT_ApplicationUnitController.backupData = backupData;
    }

    public static List<MtApplicationUnit> getFilteredDataList() {
        return filteredDataList;
    }

    public static void setFilteredDataList(List<MtApplicationUnit> filteredDataList) {
        MT_ApplicationUnitController.filteredDataList = filteredDataList;
    }

    
    
    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtApplicationUnit();
        }

        if (injectApplicationUnit == null) {
            injectApplicationUnit = new MtApplicationUnit();
        }

        if (MT_ApplicationUnitController.backupData == null) {
            MT_ApplicationUnitController.backupData = new MtApplicationUnit();
        }

    }

    /**
     * Get the data of the application unit from database and put them into a
     * list.
     *
     * @return the list with the data application unit
     */
    @Override
    public String loadDataList() {
        String message = "LOAD APPLICATION UNIT";
        String message_detail;
        MT_ApplicationUnitController.dataList = ejbApplicationUnit.findAllApplicationUnit();

        if (MT_ApplicationUnitController.dataList.isEmpty()) {
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
        MT_ApplicationUnitController.editingMode = false;
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

        MtApplicationUnit object;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the data to modify, for future restore
        object = (MtApplicationUnit) event.getObject();
        MT_ApplicationUnitController.backupData.from(object);

        // If we are editing a row, we must disabled all the other buttons
        MT_ApplicationUnitController.editingMode = true;

        int totalRows = dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the other buttons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        message_detail = "Editing application unit";
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
        MtApplicationUnit dataRow;
        int r;
        boolean result;
        boolean error=true;
        String message, message_detail;

        message = "UPDATE APPLICATION UNIT DATA";

        //Gets the current data row
        //dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        dataRow = (MtApplicationUnit) event.getObject();

        //Validates the data
        result = dataValidation(dataRow);

        if (result) {

            try {
                r = ejbApplicationUnit.updateApplicationUnit(dataRow);
                switch (r) {
                    case 1:
                        message_detail = "OK - The application unit was updated";
                        LOGGER.info(message_detail);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        error=false;
                        break;
                    case 0:
                        message_detail = "KO - The application unit was not updated";
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
                    dataRow.from(MT_ApplicationUnitController.backupData);
                }
                Ajax.update(TABLE_CLIENT_ID);
            }
        } else {
            message_detail = "ERROR - Data values are incorrect";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            dataRow.from(MT_ApplicationUnitController.backupData);
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
        MtApplicationUnit dataRow;

        message = "CANCEL UPDATE ROW";

        dataRow = (MtApplicationUnit) event.getObject();
        dataRow.from(MT_ApplicationUnitController.backupData);

        message_detail = "Cancelled the edition of the application unit";
        LOGGER.info(message_detail);

        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);

        defaultValueControlVariables();

        Ajax.update(TABLE_CLIENT_ID);
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
        MtApplicationUnit newObject;

        message = "NEW APPLICATION UNIT";
        message_detail = "";

        result = dataValidation(injectApplicationUnit);

        if (result) {
            try {

                r = ejbApplicationUnit.insertApplicationUnit(injectApplicationUnit);

                switch (r) {
                    case 1:
                        message_detail = "OK - The application unit was created";
                        LOGGER.info(message_detail);
                        newObject = new MtApplicationUnit();
                        newObject.from(injectApplicationUnit);
                        dataList.add(newObject);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The application unit was not created";
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

        //return getCurrentPage();
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
        MtApplicationUnit rowData;
        DataTable dataTable;
        int row, r;
        String message, message_detail;

        message = "DELETE APPLICATION UNIT";

        //Gets the current data row
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        row = MT_ApplicationUnitController.currentRow;
        dataTable.setRowIndex(row);
        rowData = (MtApplicationUnit) dataTable.getRowData();

        if (rowData != null) {

            try {
                r = ejbApplicationUnit.deleteApplicationUnit(rowData);
                switch (r) {
                    case 1:
                        message_detail = "OK - The application unit was deleted";
                        LOGGER.info(message_detail);
                        dataList.remove(row);
                        Ajax.update(TABLE_CLIENT_ID);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The application unit was not deleted";
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
        injectApplicationUnit.setApplicationUnitId(null);
        injectApplicationUnit.setApplicationUnitCode(null);
        injectApplicationUnit.setDescription(null);
    }

    @Override
    public boolean dataValidation(Object entity) {
        String message, message_detail;
        MtApplicationUnit objectToValidate;
        
        boolean result = true;

        message = "NEW APPLICATION UNIT VALIDATION";
        
        objectToValidate = (MtApplicationUnit) entity;

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getApplicationUnitCode() != null) {
            objectToValidate.setApplicationUnitCode(objectToValidate.getApplicationUnitCode().trim());
        }
        if (objectToValidate.getDescription() != null) {
            objectToValidate.setDescription(objectToValidate.getDescription().trim());
        }

        if (objectToValidate.getApplicationUnitCode() == null || objectToValidate.getApplicationUnitCode().length() == 0) {
            message_detail = "ERROR - The code of the application unit can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getApplicationUnitCode().length() > 10) {
            message_detail = "Error - The code of the application unit exceeds the limits of 10 character";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        }

        if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
            message_detail = "ERROR - The description of the application unit can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getDescription().length() > 100) {
            message_detail = "Error - The description of the application unit exceeds the limits of 100 character";
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
