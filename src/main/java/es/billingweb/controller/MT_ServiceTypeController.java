/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_ServiceTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtServiceType;
import es.billingweb.structure.ListedServiceTypeObject;
import es.billingweb.utils.BillingWebDates;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import es.billingweb.structure.ListedEditableEntityInterface;

/**
 *
 * @author catuxa
 */
@Named(value = "mT_ServiceTypeController")
@ViewScoped
public class MT_ServiceTypeController extends ListedServiceTypeObject implements Serializable, ListedEditableEntityInterface  {

//\\-----------//\\
//\\ VARIABLES //\\    
//\\-----------//\\
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    // Client Id of the UI
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

    
    @Inject
    private MtServiceType injectServiceType;

    @EJB
    private MT_ServiceTypeEJBLocal ejbServiceType;

   

    /**
     * List whit the serviceType scope data
     */
    private static List<MtServiceType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtServiceType> filteredDataList;

    /**
     * Selected data row in the table
     */
    @Inject
    private MtServiceType selectedData;

  

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\    
    public MtServiceType getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(MtServiceType selectedData) {
        this.selectedData = selectedData;
    }

    public MtServiceType getInjectServiceType() {
        return injectServiceType;
    }

    public void setInjectServiceType(MtServiceType injectServiceType) {
        this.injectServiceType = injectServiceType;
    }

    public List<MtServiceType> getDataList() {
        return dataList;
    }

    public void setDataList(List<MtServiceType> dataList) {
        MT_ServiceTypeController.dataList = dataList;
    }

    public List<MtServiceType> getFilteredDataList() {
        return filteredDataList;
    }

    public void setFilteredDataList(List<MtServiceType> filteredDataList) {
        MT_ServiceTypeController.filteredDataList = filteredDataList;
    }
    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtServiceType();
        }

        if (injectServiceType == null) {
            injectServiceType = new MtServiceType();
            resetNewObjectValues();
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
        String message = "LOAD SERVICE TYPE";
        String message_detail;

        //dataList = ejbServiceType.findAllServiceType();
        dataList = ejbServiceType.findServiceTypeByDate(this.searchDate);

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

        MT_ServiceTypeController.editingMode = false;
        this.fromDate_SD = null;
        this.toDate_SD = null;
        this.fromDate_ED = null;
        this.toDate_ED = null;
        this.fromDate_ID = null;
        this.toDate_ID = null;
        this.fromDate_MD = null;
        this.toDate_MD = null;

    }

    /**
     * Sets the init control variables to default value
     */
    @Override
    public void defaultValueControlVariablesIni() {
        defaultValueControlVariables();
        this.searchDate = BillingWebDates.getCurrentTimestamp();
    }

//\\---------------//\\
//\\ EVENT METHODS //\\    
//\\---------------//\\  
    
    /**
     * Action to push the create button
     */
    @Override
    public void pushNewButton() {
        resetNewObjectValues();
        RequestContext.getCurrentInstance().execute("PF('createDialog').show();");
        //RequestContext.getCurrentInstance().reset(CREATE_FORM_DATA_PANEL);
    }

    /**
     * Creates a new service type record. It's stored inmediately into the
     * database
     */
    @Override
    public void createNew() {
        int r;
        boolean result;
        String message, message_detail;
        MtServiceType newObject;

        message = "NEW SERVICE TYPE";
        message_detail = "";

        result = dataValidation(injectServiceType);

        if (result) {

            try {
                r = ejbServiceType.insertServiceType(injectServiceType);

                switch (r) {
                    case 1:
                        message_detail = "OK - The service type was created";
                        LOGGER.info(message_detail);
                        newObject = new MtServiceType();
                        newObject.from(injectServiceType);
                        dataList.add(newObject);
                        createMessage(FacesMessage.SEVERITY_INFO, message, message_detail);
                        break;
                    case 0:
                        message_detail = "KO - The produt type was not created";
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
     * Clears the create dialog form
     */
    @Override
    public void pushResetButton() {
        resetNewObjectValues();
        Ajax.update(CREATE_FORM_DATA_PANEL);
    }
    
    

//\\---------------//\\
//\\ OTHER METHODS //\\    
//\\---------------//\\     
    @Override
    public void resetNewObjectValues() {
        injectServiceType.setServiceTypeId(null);
        injectServiceType.setServiceTypeCode(null);
        injectServiceType.setDescription(null);
        injectServiceType.setStartDate(BillingWebDates.DEFAULT_START_DATE_COMPLETE);
        injectServiceType.setEndDate(BillingWebDates.DEFAULT_END_DATE_COMPLETE);
        injectServiceType.setStatusId(null);
        injectServiceType.setBusinessScopeId(null);
        injectServiceType.setTechnologyScopeId(null);
        injectServiceType.setInputDate(BillingWebDates.getCurrentTimestamp());
        injectServiceType.setInputUser(CURRENT_USER_LOGIN);
        injectServiceType.setModifDate(null);
        injectServiceType.setModifUser(null);
        injectServiceType.setEntityTypeId(ENTITY_TYPE_SERVICE_TYPE_ID);
    }

     @Override
    public boolean dataValidation(Object entity) {
        String message, message_detail;
        MtServiceType objectToValidate;

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
                message_detail = "ERROR - The entity type code must be SERV";
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
        Ajax.update(CREATE_FORM_DATA_PANEL);

        return result;
    }
    
    
    
    @Override
    public void addRow() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteRow() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onRowInit(RowEditEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onRowEdit(RowEditEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onRowCancel(RowEditEvent event) {
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
    public void pushDeleteButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pushAddButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
