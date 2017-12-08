/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_EquipmentTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtEquipmentType;
import es.billingweb.structure.ListedEquipmentTypeObject;
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
@Named(value = "mT_EquipmentTypeController")
@ViewScoped
public class MT_EquipmentTypeController extends ListedEquipmentTypeObject implements Serializable, ListedEditableEntityInterface  {

//\\-----------//\\
//\\ VARIABLES //\\    
//\\-----------//\\
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    // Client Id of the UI
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

    
    @Inject
    private MtEquipmentType injectEquipmentType;

    @EJB
    private MT_EquipmentTypeEJBLocal ejbEquipmentType;

   

    /**
     * List whit the equipmentType scope data
     */
    private static List<MtEquipmentType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtEquipmentType> filteredDataList;

    /**
     * Selected data row in the table
     */
    @Inject
    private MtEquipmentType selectedData;

  

//\\---------------------//\\
//\\ GETTERS AND SETTERS //\\    
//\\---------------------//\\    
    public MtEquipmentType getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(MtEquipmentType selectedData) {
        this.selectedData = selectedData;
    }

    public MtEquipmentType getInjectEquipmentType() {
        return injectEquipmentType;
    }

    public void setInjectEquipmentType(MtEquipmentType injectEquipmentType) {
        this.injectEquipmentType = injectEquipmentType;
    }

    public List<MtEquipmentType> getDataList() {
        return dataList;
    }

    public void setDataList(List<MtEquipmentType> dataList) {
        MT_EquipmentTypeController.dataList = dataList;
    }

    public List<MtEquipmentType> getFilteredDataList() {
        return filteredDataList;
    }

    public void setFilteredDataList(List<MtEquipmentType> filteredDataList) {
        MT_EquipmentTypeController.filteredDataList = filteredDataList;
    }
    
//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtEquipmentType();
        }

        if (injectEquipmentType == null) {
            injectEquipmentType = new MtEquipmentType();
            resetNewObjectValues();
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
        String message = "LOAD EQUIPMENT TYPE";
        String message_detail;

        //dataList = ejbEquipmentType.findAllEquipmentType();
        dataList = ejbEquipmentType.findEquipmentTypeByDate(this.searchDate);

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

        MT_EquipmentTypeController.editingMode = false;
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
    public void pushNewEquipmentType() {
        resetNewObjectValues();
        RequestContext.getCurrentInstance().execute("PF('createDialog').show();");
        //RequestContext.getCurrentInstance().reset(CREATE_FORM_DATA_PANEL);
    }

    /**
     * Creates a new equipment type record. It's stored inmediately into the
     * database
     */
    @Override
    public void createNew() {
        int r;
        boolean result;
        String message, message_detail;
        MtEquipmentType newObject;

        message = "NEW EQUIPMENT TYPE";
        message_detail = "";

        result = dataValidation(injectEquipmentType);

        if (result) {

            try {
                r = ejbEquipmentType.insertEquipmentType(injectEquipmentType);

                switch (r) {
                    case 1:
                        message_detail = "OK - The equipment type was created";
                        LOGGER.info(message_detail);
                        newObject = new MtEquipmentType();
                        newObject.from(injectEquipmentType);
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
    
    @Override
    public void pushNewButton() {
        resetNewObjectValues();
        RequestContext.getCurrentInstance().execute("PF('createDialog').show();");
    }
    

//\\---------------//\\
//\\ OTHER METHODS //\\    
//\\---------------//\\     
    @Override
    public void resetNewObjectValues() {
        injectEquipmentType.setEquipmentTypeId(null);
        injectEquipmentType.setEquipmentTypeCode(null);
        injectEquipmentType.setDescription(null);
        injectEquipmentType.setStartDate(BillingWebDates.DEFAULT_START_DATE_COMPLETE);
        injectEquipmentType.setEndDate(BillingWebDates.DEFAULT_END_DATE_COMPLETE);
        injectEquipmentType.setStatusId(null);
        injectEquipmentType.setBusinessScopeId(null);
        injectEquipmentType.setTechnologyScopeId(null);
        injectEquipmentType.setInputDate(BillingWebDates.getCurrentTimestamp());
        injectEquipmentType.setInputUser(CURRENT_USER_LOGIN);
        injectEquipmentType.setModifDate(null);
        injectEquipmentType.setModifUser(null);
        injectEquipmentType.setEntityTypeId(ENTITY_TYPE_EQUIPMENT_TYPE_ID);
    }

    @Override
    public boolean dataValidation(Object entity) {
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
                message_detail = "ERROR - The entity type code must be SERV";
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
