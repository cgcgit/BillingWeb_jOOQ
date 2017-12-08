/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_TechnologyScopeEJBLocal;
import es.billingweb.model.tables.pojos.MtTechnologyScope;
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
@Named(value = "mT_TechnologyScopeController")
@ViewScoped
public class MT_TechnologyScopeController extends ListedEntityObject implements Serializable, ListedEditableEntityInterface {

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
    private MtTechnologyScope injectTechnologyScope;

    @EJB
    private MT_TechnologyScopeEJBLocal ejbTechnologyScopePrivate;

    /**
     * List whit the technology scope data
     */
    private static List<MtTechnologyScope> dataList;
    
    /**
     * List whit the status data filtered
     */
    private static List<MtTechnologyScope> filteredDataList;

    /**
     * selected data row in the table
     */
    @Inject
    private MtTechnologyScope selectedData;

    /**
     * Backup data
     */
    private static MtTechnologyScope backupData;

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\    
    public MtTechnologyScope getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(MtTechnologyScope selectedData) {
        this.selectedData = selectedData;
    }

    public MtTechnologyScope getInjectTechnologyScope() {
        return injectTechnologyScope;
    }

    public void setInjectTechnologyScope(MtTechnologyScope injectTechnologyScope) {
        this.injectTechnologyScope = injectTechnologyScope;
    }

    public List<MtTechnologyScope> getDataList() {
        return dataList;
    }

    public void setDataList(List<MtTechnologyScope> dataList) {
        MT_TechnologyScopeController.dataList = dataList;
    }

    public MtTechnologyScope getBackupData() {
        return backupData;
    }

    public void setBackupData(MtTechnologyScope backupData) {
        MT_TechnologyScopeController.backupData = backupData;
    }

    public static List<MtTechnologyScope> getFilteredDataList() {
        return filteredDataList;
    }

    public static void setFilteredDataList(List<MtTechnologyScope> filteredDataList) {
        MT_TechnologyScopeController.filteredDataList = filteredDataList;
    }
    
    
    

//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtTechnologyScope();
        }

        if (injectTechnologyScope == null) {
            injectTechnologyScope = new MtTechnologyScope();
        }

        if (MT_TechnologyScopeController.backupData == null) {
            MT_TechnologyScopeController.backupData = new MtTechnologyScope();
        }

    }

    /**
     * Get the data of the technology scope from database and put them into a
     * list.
     *
     * @return the list with the data technology scope
     */
    @Override
    public String loadDataList() {
        String message = "LOAD TECNOLOGY SCOPE";
        String message_detail;
        dataList = ejbTechnologyScopePrivate.findAllTechnologyScope();

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
        MT_TechnologyScopeController.editingMode = false;
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

        MtTechnologyScope dataRow;

        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the data to modify, for future restore
        dataRow = (MtTechnologyScope) event.getObject();
        MT_TechnologyScopeController.backupData.from(dataRow);

        // If we are editing a row, we must disabled all the other buttons
        MT_TechnologyScopeController.editingMode = true;

        int totalRows = dataList.size();

        // If we are modifing a row we can't add a new row --> Disable all the other buttons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        message_detail = "Editing technology scope";
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
        MtTechnologyScope dataRow;
        int r;
        boolean result;
        boolean error=true;
        String message, message_detail;

        message = "UPDATE TECNOLOGY SCOPE";

        //Gets the current data row
        //dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        dataRow = (MtTechnologyScope) event.getObject();

        //Validates the data
        result = dataValidation(dataRow);

        if (result) {
            try {
                r = ejbTechnologyScopePrivate.updateTechnologyScope(dataRow);
                switch (r) {
                    case 1:
                        message_detail = "OK - The technology scope was updated";
                        LOGGER.info(message_detail);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        error=false;
                        break;
                    case 0:
                        message_detail = "KO - The technology scope was not updated";
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
                    dataRow.from(MT_TechnologyScopeController.backupData);
                }
                Ajax.update(TABLE_CLIENT_ID);
            }
        } else {
            message_detail = "ERROR - Data values are incorrect";
            LOGGER.info(message_detail);
            createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
            dataRow.from(MT_TechnologyScopeController.backupData);
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
        MtTechnologyScope dataRow;

        message = "CANCEL UPDATE ROW";

        dataRow = (MtTechnologyScope) event.getObject();
        dataRow.from(MT_TechnologyScopeController.backupData);

        message_detail = "Cancelled the edition of the technology scope";
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
        MtTechnologyScope newObject;

        message = "NEW TECNOLOGY SCOPE";
        message_detail = "";

        result = dataValidation(injectTechnologyScope);

        if (result) {
            try {
                r = ejbTechnologyScopePrivate.insertTechnologyScope(injectTechnologyScope);

                switch (r) {
                    case 1:
                        message_detail = "OK - The technology scope was created";
                        LOGGER.info(message_detail);
                        newObject = new MtTechnologyScope();
                        newObject.from(injectTechnologyScope);
                        dataList.add(newObject);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The technology scope was not created";
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
        MtTechnologyScope rowData;
        DataTable dataTable;
        int row, r;
        String message, message_detail;

        message = "DELETE TECNOLOGY SCOPE";

        //Gets the current data row
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(TABLE_CLIENT_ID);
        row = MT_TechnologyScopeController.currentRow;
        dataTable.setRowIndex(row);
        rowData = (MtTechnologyScope) dataTable.getRowData();

        if (rowData != null) {

            try {
                r = ejbTechnologyScopePrivate.deleteTechnologyScope(rowData);
                switch (r) {
                    case 1:
                        message_detail = "OK - The technology scope was deleted";
                        LOGGER.info(message_detail);
                        dataList.remove(row);
                        Ajax.update(TABLE_CLIENT_ID);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The technology scope was not deleted";
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
        injectTechnologyScope.setTechnologyScopeId(null);
        injectTechnologyScope.setTechnologyScopeCode(null);
        injectTechnologyScope.setDescription(null);
    }

    @Override
    public boolean dataValidation(Object entity) {
        String message, message_detail;
        MtTechnologyScope objectToValidate;

        boolean result = true;

        message = "NEW TECHNOLOGY SCOPE VALIDATION";
        
        objectToValidate = (MtTechnologyScope) entity;

        if (objectToValidate == null) {
            message_detail = "ERROR - Empty values";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getTechnologyScopeCode() != null) {
            objectToValidate.setTechnologyScopeCode(objectToValidate.getTechnologyScopeCode().trim());
        }
        if (objectToValidate.getDescription() != null) {
            objectToValidate.setDescription(objectToValidate.getDescription().trim());
        }

        if (objectToValidate.getTechnologyScopeCode() == null || objectToValidate.getTechnologyScopeCode().length() == 0) {
            message_detail = "ERROR - The code of the technology scope can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getTechnologyScopeCode().length() > 10) {
            message_detail = "Error - The code of the technology scope exceeds the limits of 10 character";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        }

        if (objectToValidate.getDescription() == null || objectToValidate.getDescription().length() == 0) {
            message_detail = "ERROR - The description of the technology scope can not be null";
            LOGGER.error(message_detail);
            createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            result = false;
        } else if (objectToValidate.getDescription().length() > 100) {
            message_detail = "Error - The description of the technology scope exceeds the limits of 100 character";
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
