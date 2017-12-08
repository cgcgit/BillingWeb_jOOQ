/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejbTable.MT_ProductTypeEJBLocal;
import es.billingweb.model.tables.pojos.MtProductType;
import es.billingweb.utils.BillingWebDates;
import es.billingweb.structure.ListedProductTypeObject;
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
@Named(value = "mT_ProductTypeController")
@ViewScoped
public class MT_ProductTypeController extends ListedProductTypeObject implements Serializable, ListedEditableEntityInterface {

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
    private MtProductType injectProductType;

    /**
     * List whit the productType scope data
     */
    private static List<MtProductType> dataList;

    /**
     * List whit the status data filtered
     */
    private static List<MtProductType> filteredDataList;

    @EJB
    protected MT_ProductTypeEJBLocal ejbProductType;

    /**
     * Selected data row in the table
     */
    @Inject
    private MtProductType selectedData;

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
     * Gets the injectProductType for the new record
     *
     * @return the injectProductType data
     */
    public MtProductType getInjectProductType() {
        return injectProductType;
    }

    /**
     * Sets the injectProductType data for the new record
     *
     * @param injectProductType the data of the new product type
     */
    public void setInjectProductType(MtProductType injectProductType) {
        this.injectProductType = injectProductType;
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
        MT_ProductTypeController.dataList = dataList;
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
        MT_ProductTypeController.filteredDataList = filteredDataList;
    }

//\\--------------//\\
//\\ INIT METHODS //\\    
//\\--------------//\\    
    @PostConstruct
    public void init() {

        if (selectedData == null) {
            selectedData = new MtProductType();
        }

        if (injectProductType == null) {
            injectProductType = new MtProductType();
            resetNewObjectValues();
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
        String message = "LOAD PRODUCT TYPE";
        String message_detail;

        //dataList = ejbProductType.findAllProductType();
        dataList = ejbProductType.findProductTypeByDate(this.searchDate);

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

        MT_ProductTypeController.editingMode = false;
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
     * Creates a new product type record. It's stored inmediately into the
     * database
     */
    @Override
    public void createNew() {
        int r;
        boolean result;
        String message, message_detail;
        MtProductType newObject;

        message = "NEW PRODUCT TYPE";
        message_detail = "";

        result = dataValidation(injectProductType);

        if (result) {

            try {

                r = ejbProductType.insertProductType(injectProductType);

                switch (r) {
                    case 1:
                        message_detail = "OK - The product type was created";
                        LOGGER.info(message_detail);
                        newObject = new MtProductType();
                        newObject.from(injectProductType);
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
     * Reset the values of the inject object injectProductType to null values,
     * except startDate, endDate, inputDate and inputUser (whose values ​​are
     * set to default startDate, default endDate, current date and current
     * userLogin values ​​respectively)
     */
    @Override
    public void resetNewObjectValues() {
        injectProductType.setProductTypeId(null);
        injectProductType.setProductTypeCode(null);
        injectProductType.setDescription(null);
        injectProductType.setStartDate(BillingWebDates.DEFAULT_START_DATE_COMPLETE);
        injectProductType.setEndDate(BillingWebDates.DEFAULT_END_DATE_COMPLETE);
        injectProductType.setStatusId(null);
        injectProductType.setBusinessScopeId(null);
        injectProductType.setTechnologyScopeId(null);
        injectProductType.setInputDate(BillingWebDates.getCurrentTimestamp());
        injectProductType.setInputUser(CURRENT_USER_LOGIN);
        injectProductType.setModifDate(null);
        injectProductType.setModifUser(null);
        injectProductType.setEntityTypeId(ENTITY_TYPE_PRODUCT_TYPE_ID);
    }


    @Override
    public boolean dataValidation(Object entity) {
        String message, message_detail;
        MtProductType objectToValidate;
        
        boolean result = true;

        message = "NEW PRODUCT TYPE VALIDATION";
        
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
