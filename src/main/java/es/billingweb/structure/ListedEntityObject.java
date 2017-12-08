/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.structure;

import es.billingweb.ejbTable.MT_ApplicationLevelEJBLocal;
import es.billingweb.ejbTable.MT_BusinessScopeEJBLocal;
import es.billingweb.ejbTable.MT_DiscountTypeEJBLocal;
import es.billingweb.ejbTable.MT_EntityTypeEJBLocal;
import es.billingweb.ejbTable.MT_StatusEJBLocal;
import es.billingweb.ejbTable.MT_TechnologyScopeEJBLocal;
import es.billingweb.model.tables.pojos.MtApplicationLevel;
import es.billingweb.model.tables.pojos.MtBusinessScope;
import es.billingweb.model.tables.pojos.MtDiscountType;
import es.billingweb.model.tables.pojos.MtEntityType;
import es.billingweb.model.tables.pojos.MtTechnologyScope;
import es.billingweb.utils.BillingWebDates;
import es.billingweb.utils.BillingWebSessionUtils;
import es.billingweb.utils.BillingWebUtilities;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author catuxa
 */
public class ListedEntityObject {

    protected static final Logger LOGGER = LogManager.getLogger();
    protected final String MESSAGE_CLIENT_ID = "messageGrowl";

    protected final ResourceBundle OTHERS = ResourceBundle.getBundle("properties.tables");

    // Current profile for the user logged
    protected final String CURRENT_PROFILE_LOGIN = BillingWebSessionUtils.getCurrentUserLogged().getProfileCode();

    // Current user code logged
    protected final String CURRENT_USER_LOGIN = BillingWebSessionUtils.getCurrentUserLogged().getUserCode();

    /**
     * Current row of the table
     */
    protected static int currentRow;

    /**
     * Indicates if there are any rows in edit mode
     */
    protected static boolean editingMode = false;

    /**
     * Identifies if the push button is from managedBean.
     */
    protected static boolean pretendPushEdit = false;

    /**
     * Specify if the save button must be disabled.
     */
    protected static boolean disableSaveButton = true;

    /**
     * Indicates whether the add button has been pressed.
     */
    protected static boolean fromAddingRow = false;

    /**
     * Indicates if there has been any change in the data
     */
    protected static boolean changes = false;

    /**
     * Indicates if the status change to Cancel
     */
    protected static boolean toCancel = false;

    /**
     * Previous status id to control the changes for the edition. Value = -1 -->
     * the first change into the edition
     */
    protected static Integer prevStatusId;
    
    /**
     * Previous page 
     */
    protected String previousPage;

    // Data values for filter and search data    
    protected Timestamp fromDate_SD;      // fromDate for startDate
    protected Timestamp toDate_SD;        // toDate for startDate
    protected Timestamp fromDate_ED;      // fromDate for endDate
    protected Timestamp toDate_ED;        // toDate for endDate
    protected Timestamp fromDate_ID;      // fromDate for inputDate
    protected Timestamp toDate_ID;        // toDate for inputDate
    protected Timestamp fromDate_MD;      // fromDate for modifDate
    protected Timestamp toDate_MD;        // toDate for modifDate
    protected Timestamp searchDate;       // search date chriteria

    @EJB
    protected MT_EntityTypeEJBLocal ejbEntityType;

    @EJB
    protected MT_StatusEJBLocal ejbStatus;

    @EJB
    protected MT_BusinessScopeEJBLocal ejbBusinessScope;

    @EJB
    protected MT_TechnologyScopeEJBLocal ejbTechnologyScope;
    
    @EJB
    protected MT_DiscountTypeEJBLocal ejbDiscountType;
    
    @EJB
    protected MT_ApplicationLevelEJBLocal ejbApplicationLevel;

    /**
     * Gets if the user can editing data
     *
     * @return true: the current user can edit data - false: otherwise
     */
    public boolean isEditingMode() {
        return ListedEntityObject.editingMode;
    }

    /**
     * Sets if the user can editing data
     *
     * @param editingMode true: the user can edit - false: otherwise
     */
    public void setEditingMode(boolean editingMode) {
        ListedEntityObject.editingMode = editingMode;
    }

    public static boolean isPretendPushEdit() {
        return pretendPushEdit;
    }

    public static void setPretendPushEdit(boolean pretendPushEdit) {
        ListedEntityObject.pretendPushEdit = pretendPushEdit;
    }

    public static boolean isFromAddingRow() {
        return fromAddingRow;
    }

    public static void setFromAddingRow(boolean fromAddingRow) {
        ListedEntityObject.fromAddingRow = fromAddingRow;
    }

    public static Integer getPrevStatusId() {
        return prevStatusId;
    }

    public static void setPrevStatusId(Integer prevStatusId) {
        ListedEntityObject.prevStatusId = prevStatusId;
    }

        
    /**
     * Gets the current row of the dataTable
     *
     * @return number of the current row of the dataTable
     */
    public int getCurrentRow() {
        return currentRow;
    }

    /**
     * Sets the current row of the dataTable
     *
     * @param currentRow number of the current row of the dataTable
     */
    public void setCurrentRow(int currentRow) {
        ListedEntityObject.currentRow = currentRow;
    }

    /**
     * Gets if the save button must be disabled.
     *
     * @return true: the button must be disabled - false: otherwise
     */
    public boolean isDisableSaveButton() {
        return ListedEntityObject.disableSaveButton;
    }

    /**
     * Sets if the save button must be disabled.
     *
     * @param disableSaveButton
     */
    public void setDisableSaveButton(boolean disableSaveButton) {
        ListedEntityObject.disableSaveButton = disableSaveButton;
    }

    /**
     * Gets if there has been any change.
     *
     * @return true: there has been any change - false: otherwise
     */
    public boolean isChanges() {
        return ListedEntityObject.changes;
    }

    /**
     * Setis if there has been any change.
     *
     * @param changes true: there has been any change - false: otherwise
     */
    public void setChanges(boolean changes) {
        ListedEntityObject.changes = changes;
    }

    /**
     * Get's if the record has change to cancel status.
     *
     * @return true: the record has the cancel status - false: otherwise
     */
    public boolean isToCancel() {
        return toCancel;
    }

    /**
     * Set's if the record has change to cancel status.
     *
     * @param toCancel true: the record has the cancel status - false: otherwise
     */
    public void setToCancel(boolean toCancel) {
        ListedEntityObject.toCancel = toCancel;
    }

    /**
     * Evaluates the condition to disable the save changes button
     *
     * @return true if the saveChanges button must be disabled, false otherwise
     */
    public boolean isDisabledSave() {

        if (!ListedEntityObject.disableSaveButton) {
            return ListedEntityObject.changes;
        } else {
            return ListedEntityObject.editingMode;
        }
    }

    /**
     * Gets the date "from" to filtered the data into the dataTable by startDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @return the fromDate for the filter
     */
    public Timestamp getFromDate_SD() {
        return fromDate_SD;
    }

    /**
     * Sets the date "from" to filtered the data into the dataTable by startDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @param fromDate_SD the fromDate for the filter
     */
    public void setFromDate_SD(Timestamp fromDate_SD) {
        this.fromDate_SD = fromDate_SD;
    }

    /**
     * Gets the date "to" to filtered the data into the dataTable by startDate
     * (the data must have the date field less or equal to the toDate)
     *
     * @return the toDate for the filter
     */
    public Timestamp getToDate_SD() {
        return toDate_SD;
    }

    /**
     * Sets the date "to" to filtered the data into the dataTable by startDate
     * (the data must have the date field greather or equal to the toDate)
     *
     * @param toDate_SD the toDate for the filter
     */
    public void setToDate_SD(Timestamp toDate_SD) {
        this.toDate_SD = toDate_SD;
    }

    /**
     * Gets the date "from" to filtered the data into the dataTable by endDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @return the fromDate for the filter
     */
    public Timestamp getFromDate_ED() {
        return fromDate_ED;
    }

    /**
     * Sets the date "from" to filtered the data into the dataTable by endDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @param fromDate_ED the fromDate for the filter
     */
    public void setFromDate_ED(Timestamp fromDate_ED) {
        this.fromDate_ED = fromDate_ED;
    }

    /**
     * Gets the date "to" to filtered the data into the dataTable by endDate
     * (the data must have the date field less or equal to the toDate)
     *
     * @return the toDate for the filter
     */
    public Timestamp getToDate_ED() {
        return toDate_ED;
    }

    /**
     * Sets the date "to" to filtered the data into the dataTable by endDate
     * (the data must have the date field greather or equal to the toDate)
     *
     * @param toDate_ED the toDate for the filter
     */
    public void setToDate_ED(Timestamp toDate_ED) {
        this.toDate_ED = toDate_ED;
    }

    /**
     * Gets the date "from" to filtered the data into the dataTable by inputDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @return the fromDate for the filter
     */
    public Timestamp getFromDate_ID() {
        return fromDate_ID;
    }

    /**
     * Sets the date "from" to filtered the data into the dataTable by inputDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @param fromDate_ID the fromDate for the filter
     */
    public void setFromDate_ID(Timestamp fromDate_ID) {
        this.fromDate_ID = fromDate_ID;
    }

    /**
     * Gets the date "to" to filtered the data into the dataTable by inputDate
     * (the data must have the date field less or equal to the toDate)
     *
     * @return the toDate for the filter
     */
    public Timestamp getToDate_ID() {
        return toDate_ID;
    }

    /**
     * Sets the date "to" to filtered the data into the dataTable by inputDate
     * (the data must have the date field greather or equal to the toDate)
     *
     * @param toDate_ID the toDate for the filter
     */
    public void setToDate_ID(Timestamp toDate_ID) {
        this.toDate_ID = toDate_ID;
    }

    /**
     * Gets the date "from" to filtered the data into the dataTable by modifDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @return the fromDate for the filter
     */
    public Timestamp getFromDate_MD() {
        return fromDate_MD;
    }

    /**
     * Sets the date "from" to filtered the data into the dataTable by modifDate
     * (the data must have the date field greather or equal to the fromDate)
     *
     * @param fromDate_MD the fromDate for the filter
     */
    public void setFromDate_MD(Timestamp fromDate_MD) {
        this.fromDate_MD = fromDate_MD;
    }

    /**
     * Gets the date "to" to filtered the data into the dataTable by modifDate
     * (the data must have the date field less or equal to the toDate)
     *
     * @return the toDate for the filter
     */
    public Timestamp getToDate_MD() {
        return toDate_MD;
    }

    /**
     * Sets the date "to" to filtered the data into the dataTable by modifDate
     * (the data must have the date field greather or equal to the toDate)
     *
     * @param toDate_MD the toDate for the filter
     */
    public void setToDate_MD(Timestamp toDate_MD) {
        this.toDate_MD = toDate_MD;
    }

    /**
     * Gets the date "search" to obtain the list of data to show in the
     * dataTable (the data will have the closest startDate and endDate to the
     * searchData)
     *
     * @return the searchDate
     */
    public Timestamp getSearchDate() {
        return searchDate;
    }

    /**
     * Dets the date "search" to obtain the list of data to show in the
     * dataTable (the data will have the closest startDate and endDate to the
     * searchData)
     *
     * @param searchDate the searchDate chriteria
     */
    public void setSearchDate(Timestamp searchDate) {
        this.searchDate = searchDate;
    }

    public String getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
    }
    
    
    

    /**
     * Custom filter for the data fields into the datatable
     *
     * @param value
     * @param filter
     * @param locale
     * @return true: the record is between from and to dates - false: otherwise
     */
    public boolean filterByStartDateController(Object value, Object filter, Locale locale) {

        String message, message_detail;
        Timestamp filterDate = null;

        message = "FILTER BY DATES";

        if (value == null) {
            return true;
        } else {
            try {
                filterDate = BillingWebDates.shortStringToTimestamp(BillingWebDates.dateToStringShortView((Date) value));
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

        Timestamp dateFrom = this.fromDate_SD;
        Timestamp dateTo = this.toDate_SD;

        return (dateFrom == null || filterDate.after(dateFrom) || filterDate.equals(dateFrom))
                && (dateTo == null || filterDate.before(dateTo) || filterDate.equals(dateTo));
    }

    /**
     * Custom filter for the data fields into the datatable
     *
     * @param value
     * @param filter
     * @param locale
     * @return true: the record is between from and to dates - false: otherwise
     */
    public boolean filterByEndDateController(Object value, Object filter, Locale locale) {

        String message, message_detail;
        Timestamp filterDate = null;

        message = "FILTER BY DATES";

        if (value == null) {
            return true;
        } else {
            try {
                filterDate = BillingWebDates.shortStringToTimestamp(BillingWebDates.dateToStringShortView((Date) value));
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

        Timestamp dateFrom = this.fromDate_ED;
        Timestamp dateTo = this.toDate_ED;

        return (dateFrom == null || filterDate.after(dateFrom) || filterDate.equals(dateFrom))
                && (dateTo == null || filterDate.before(dateTo) || filterDate.equals(dateTo));
    }

    /**
     * Custom filter for the data fields into the datatable
     *
     * @param value
     * @param filter
     * @param locale
     * @return true: the record is between from and to dates - false: otherwise
     */
    public boolean filterByInputDateController(Object value, Object filter, Locale locale) {

        String message, message_detail;
        Timestamp filterDate = null;

        message = "FILTER BY DATES";

        if (value == null) {
            return true;
        } else {
            try {
                filterDate = BillingWebDates.shortStringToTimestamp(BillingWebDates.dateToStringShortView((Date) value));
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

        Timestamp dateFrom = this.fromDate_ID;
        Timestamp dateTo = this.toDate_ID;

        return (dateFrom == null || filterDate.after(dateFrom) || filterDate.equals(dateFrom))
                && (dateTo == null || filterDate.before(dateTo) || filterDate.equals(dateTo));
    }

    /**
     * Return the list to populate the one select menu for the entity types
     *
     * @return the list of all the entity types into the system
     */
    public List<SelectItem> populateEntityTypeMenu() {
        List<SelectItem> statusItem = new ArrayList<>();
        List<MtEntityType> list = ejbEntityType.findAllEntityType();

        if (list.isEmpty()) {
            LOGGER.error("ERROR - Not find entity type");
        } else {
            for (MtEntityType p : list) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getEntityTypeCode() + "-" + p.getDescription());
                item.setValue(p.getEntityTypeId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }
   
    

    /**
     * Return the list to populate the one select menu for the business scope
     *
     * @return the list of all the business scope into the system
     */
    public List<SelectItem> populateBusinessScopeMenu() {
        List<SelectItem> statusItem = new ArrayList<>();
        List<MtBusinessScope> list = ejbBusinessScope.findAllBusinessScope();

        if (list.isEmpty()) {
            LOGGER.error("ERROR - Not find status");
        } else {
            for (MtBusinessScope p : list) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getBusinessScopeCode() + "-" + p.getDescription());
                item.setValue(p.getBusinessScopeId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }

    /**
     * Return the list to populate the one select menu for the technology scope
     *
     * @return the list of all the technology scope into the system
     */
    public List<SelectItem> populateTechnologyScopeMenu() {
        List<SelectItem> statusItem = new ArrayList<>();
        List<MtTechnologyScope> list = ejbTechnologyScope.findAllTechnologyScope();

        if (list.isEmpty()) {
            LOGGER.error("ERROR - Not find status");
        } else {
            for (MtTechnologyScope p : list) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getTechnologyScopeCode() + "-" + p.getDescription());
                item.setValue(p.getTechnologyScopeId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }

    
    /**
     * Return the list to populate the one select menu for the discount type
     *
     * @return the list of all the discount type into the system
     */
    public List<SelectItem> populateDiscountTypeMenu() {
        List<SelectItem> statusItem = new ArrayList<>();
        List<MtDiscountType> list = ejbDiscountType.findAllDiscountType();

        if (list.isEmpty()) {
            LOGGER.error("ERROR - Not find status");
        } else {
            for (MtDiscountType p : list) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getDiscountTypeCode() + "-" + p.getDescription());
                item.setValue(p.getDiscountTypeId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }
    
    /**
     * Return the list to populate the one select menu for the application level
     *
     * @return the list of all the application level the system
     */
    public List<SelectItem> populateApplicationLevelMenu() {
        List<SelectItem> statusItem = new ArrayList<>();
        List<MtApplicationLevel> list = ejbApplicationLevel.findAllApplicationLevel();

        if (list.isEmpty()) {
            LOGGER.error("ERROR - Not find status");
        } else {
            for (MtApplicationLevel p : list) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getApplicationLevelCode() + "-" + p.getDescription());
                item.setValue(p.getApplicationLevelId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }

    /**
     * Return the list to populate the one select menu for the boolean menu to Yes or Not
     * @return the list of all the boolean values (true/false) for the yes or not values
     */
    public List<SelectItem> populateBooleanYesNoMenu() {
        List<SelectItem> statusItem = new ArrayList<>();     

        SelectItem itemTrue = new SelectItem();        
        itemTrue.setLabel("YES");
        itemTrue.setValue(Boolean.TRUE);
        statusItem.add(itemTrue);
        
        SelectItem itemFalse = new SelectItem();
        
        itemFalse.setLabel("NO");
        itemFalse.setValue(Boolean.FALSE);
        statusItem.add(itemFalse);
         
        
        return statusItem;
    }
    
    
    /**
     * Custom filter for the data fields into the datatable
     *
     * @param value
     * @param filter
     * @param locale
     * @return true: the record is between from and to dates - false: otherwise
     */
    public boolean filterByModifDateController(Object value, Object filter, Locale locale) {

        String message, message_detail;
        Timestamp filterDate = null;

        message = "FILTER BY DATES";

        if (value == null) {
            return true;
        } else {
            try {
                filterDate = BillingWebDates.shortStringToTimestamp(BillingWebDates.dateToStringShortView((Date) value));
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

        Timestamp dateFrom = this.fromDate_MD;
        Timestamp dateTo = this.toDate_MD;

        return (dateFrom == null || filterDate.after(dateFrom) || filterDate.equals(dateFrom))
                && (dateTo == null || filterDate.before(dateTo) || filterDate.equals(dateTo));
    }

    /**
     * Indicates if the logged user has permission to modify data
     *
     * @return true: the user can edit/modify data false: otherwise
     */
    public boolean canUserEditModify() {
        return BillingWebUtilities.canModify(CURRENT_PROFILE_LOGIN);
    }

    /**
     * Returns the current page
     *
     * @return
     */
    public String getCurrentPage() {
        UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
        return view.getViewId() + "?faces-redirect=true";
    }

    /**
     * Shows a message (with a delay)
     *
     * source:
     * http://www.it1me.com/it-answers?id=23791039&ttl=Primefaces+datatable+date+range+filter+with+filterFunction
     *
     * @param messageConcept concept of the message (acesMessage.SEVERITY_*)
     * @param textMessage the message to show
     * @param textMessageDetail the message detail to show
     */
    public void createMessage(FacesMessage.Severity messageConcept, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(messageConcept, textMessage, textMessageDetail);

        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(MESSAGE_CLIENT_ID, message);
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, textMessage);

    }

}
