/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_PromotionTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtPromotionType;
import es.billingweb.utils.BillingWebDates;
import es.billingweb.structure.ListedPromotionTypeObject;
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
@Named(value = "mT_PromotionTypeController")
@ViewScoped
public class MT_PromotionTypeController extends ListedPromotionTypeObject implements Serializable, ListedEditableEntityInterface {

//\\-----------//\\
//\\ VARIABLES //\\    
//\\-----------//\\
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

   
    // Client Id of the UI
    private final String TABLE_CLIENT_ID = "form:table";
    private final String CREATE_FORM_DATA_PANEL = "form:createDialogPanelData";

    // Context
    //private FacesContext currentContext;
    //private RequestContext currentRequestContext;
    @Inject
    private MtPromotionType injectPromotionType;

    /**
     * List whit the promotionType scope data
     */
    private static List<MtPromotionType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtPromotionType> filteredDataList;

    @EJB
    protected MT_PromotionTypeEJBLocal ejbPromotionType;

    /**
     * Selected data row in the table
     */
    @Inject
    private MtPromotionType selectedData;

    /**
     * Current row of the table
     */
    //private static int currentRow;
    /**
     * Indicates if there are any rows in edit mode
     */
    //private static boolean editingMode = false;
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
     * Gets the injectPromotionType for the new record
     *
     * @return the injectPromotionType data
     */
    public MtPromotionType getInjectPromotionType() {
        return injectPromotionType;
    }

    /**
     * Sets the injectPromotionType data for the new record
     *
     * @param injectPromotionType the data of the new promotion type
     */
    public void setInjectPromotionType(MtPromotionType injectPromotionType) {
        this.injectPromotionType = injectPromotionType;
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
        MT_PromotionTypeController.dataList = dataList;
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
        MT_PromotionTypeController.filteredDataList = filteredDataList;
    }

//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtPromotionType();
        }

        if (injectPromotionType == null) {
            injectPromotionType = new MtPromotionType();
            resetNewObjectValues();
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
        String message = "LOAD PROMOTION TYPE";
        String message_detail;

        //dataList = ejbPromotionType.findAllPromotionType();
        dataList = ejbPromotionType.findPromotionTypeByDate(this.searchDate);

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

    @Override
    public void defaultValueControlVariables() {

        MT_PromotionTypeController.editingMode = false;
        this.fromDate_SD = null;
        this.toDate_SD = null;
        this.fromDate_ED = null;
        this.toDate_ED = null;
        this.fromDate_ID = null;
        this.toDate_ID = null;
        this.fromDate_MD = null;
        this.toDate_MD = null;

    }

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
     * Creates a new promotion type record. It's stored inmediately into the
     * database
     */
    @Override
    public void createNew() {
        int r;
        boolean result;
        String message, message_detail;
        MtPromotionType newObject;

        message = "NEW PROMOTION TYPE";
        message_detail = "";

        result = dataValidation(injectPromotionType);

        if (result) {

            try {

                r = ejbPromotionType.insertPromotionType(injectPromotionType);

                switch (r) {
                    case 1:
                        message_detail = "OK - The promotion type was created";
                        LOGGER.info(message_detail);
                        newObject = new MtPromotionType();
                        newObject.from(injectPromotionType);
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
        //RequestContext.getCurrentInstance().reset(CREATE_FORM_DATA_PANEL);
    }

//\\---------------//\\
//\\ OTHER METHODS //\\    
//\\---------------//\\ 
    /**
     * Reset the values of the inject object injectPromotionType to null values,
     * except startDate, endDate, inputDate and inputUser (whose values ​​are
     * set to default startDate, default endDate, current date and current
     * userLogin values ​​respectively)
     */
    @Override
    public void resetNewObjectValues() {
        injectPromotionType.setPromotionTypeId(null);
        injectPromotionType.setPromotionTypeCode(null);
        injectPromotionType.setDescription(null);
        injectPromotionType.setVoucher(Boolean.TRUE);
        injectPromotionType.setStartDate(BillingWebDates.DEFAULT_START_DATE_COMPLETE);
        injectPromotionType.setEndDate(BillingWebDates.DEFAULT_END_DATE_COMPLETE);
        injectPromotionType.setStatusId(null);
        injectPromotionType.setApplicationLevelId(null);
        injectPromotionType.setBusinessScopeId(null);
        injectPromotionType.setTechnologyScopeId(null);
        injectPromotionType.setVariable(Boolean.TRUE);
        injectPromotionType.setMinDiscountValue(null);
        injectPromotionType.setMaxDiscountValue(null);
        injectPromotionType.setInputDate(BillingWebDates.getCurrentTimestamp());
        injectPromotionType.setInputUser(CURRENT_USER_LOGIN);
        injectPromotionType.setModifDate(null);
        injectPromotionType.setModifUser(null);
        injectPromotionType.setEntityTypeId(ENTITY_TYPE_PROMOTION_TYPE_ID);
        
    }


    @Override
    public boolean dataValidation(Object entity) {
        String message, message_detail;
        MtPromotionType objectToValidate;
        
        boolean result = true;

        message = "NEW PROMOTION TYPE VALIDATION";
        
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
