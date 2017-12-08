/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejb.ListEJBLocal;
import es.billingweb.ejb.StatusEJBLocal;
import es.billingweb.model.tables.pojos.ItUser;
import es.billingweb.utils.BillingWebDates;
import es.billingweb.utils.BillingWebMessages;
import es.billingweb.utils.BillingWebSessionUtils;
import es.billingweb.utils.BillingWebUtilities;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;

/**
 * Managed Bean to control the user's detail page.
 *
 * @author catuxa
 */
@Named
@ViewScoped
public class UserDetailController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    private final String CURRENT_USER_LOGIN = BillingWebSessionUtils.getCurrentUserLogged().getUserCode();
    private final String CURRENT_PROFILE_LOGIN = BillingWebSessionUtils.getCurrentUserLogged().getProfileCode();

    private final ResourceBundle component = ResourceBundle.getBundle("properties.ui_component");

    private final String TABLE_CLIENT_ID = "userDetail:userDetailTable";

    @EJB
    private ListEJBLocal ejbUserDetail;
    @EJB
    private StatusEJBLocal ejbStatus;

    /**
     * selected user in the table
     */
    @Inject
    private ItUser selectedUser;

    // Context
    private FacesContext currentContext;
    private RequestContext currentRequestContext;

    // Id of the user to show and modify
    private static Integer userId;

    /**
     * List whit the userData
     */
    private static List<ItUser> userListDetail;

    /**
     * Backup list to restore from changes
     */
    private static List<ItUser> userListDetailBackup;

    //**** CONTROL VARIABLEoS (to manage the actions to do) ****//
    /**
     * Identifies if the push button is from managedBean.
     */
    private static boolean pretendPushEdit = false;

    /**
     * Indicates if there are any rows in edit mode
     */
    private static boolean editingMode = false;

    /**
     * Specify if the save button must be disabled.
     */
    private static boolean disableSaveButton = true;

    /**
     * Indicates whether the add button has been pressed.
     */
    private static boolean fromAddingRow = false;

    /**
     * Indicates if there has been any change in the data
     */
    private static boolean changes = false;

    /**
     * Indicates if the status change to Cancel
     */
    private static boolean toCancel = false;

    /**
     * Previous status id to control the changes for the edition Value = -1 -->
     * the first change into the edition
     */
    private static Integer prevStatusId;

    /**
     * Current row to manage change status options
     */
    private static int currentRow;

    @PostConstruct
    public void init() {

        if (this.currentContext == null) {
            this.currentContext = FacesContext.getCurrentInstance();
        }
        if (this.currentRequestContext == null) {
            this.currentRequestContext = RequestContext.getCurrentInstance();
        }

        if (selectedUser == null) {
            selectedUser = new ItUser();
        }

        if (userListDetailBackup == null) {
            userListDetailBackup = new ArrayList<>();
        }
    }

    ///////////////////////////////    
    // GETTER AND SETTER METHODS //
    ///////////////////////////////
    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        UserDetailController.currentRow = currentRow;
    }

    public FacesContext getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(FacesContext currentContext) {
        this.currentContext = currentContext;
    }

    public RequestContext getCurrentRequestContext() {
        return currentRequestContext;
    }

    public void setCurrentRequestContext(RequestContext currentRequestContext) {
        this.currentRequestContext = currentRequestContext;
    }

    /**
     * Gets the selected user in the table
     *
     * @return the user was selected
     */
    public ItUser getSelectedUser() {
        return selectedUser;
    }

    /**
     * Sets the selected user in the table
     *
     * @param selectedUser
     */
    public void setSelectedUser(ItUser selectedUser) {
        this.selectedUser = selectedUser;
    }

    /**
     * Get the user Id to view/modify
     *
     * @return the user Id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Set the userId to view/modify
     *
     * @param userId
     */
    public void setUserId(Integer userId) {
        UserDetailController.userId = userId;
        // userListDetail = ejbUserDetail.UserDetail(this.userId);
    }
    

    public boolean isEditingMode() {
        return UserDetailController.editingMode;
    }

    public void setEditingMode(boolean disabled) {
        UserDetailController.editingMode = disabled;
    }

    public boolean isDisableSaveButton() {
        return UserDetailController.disableSaveButton;
    }

    public void setDisableSaveButton(boolean disableSaveButton) {
        UserDetailController.disableSaveButton = disableSaveButton;
    }

    public boolean isChanges() {
        return UserDetailController.changes;
    }

    public void setChanges(boolean changes) {
        UserDetailController.changes = changes;
    }

    public boolean isToCancel() {
        return toCancel;
    }

    public void setToCancel(boolean toCancel) {
        UserDetailController.toCancel = toCancel;
    }

    /**
     * Evaluates the condition to disable the save changes button
     *
     * @return true if the saveChanges button must be disabled, false otherwise
     */
    public boolean isDisabledSave() {

        if (!UserDetailController.disableSaveButton) {
            return UserDetailController.changes;
        } else {
            return UserDetailController.editingMode;
        }
    }

    /**
     * Get the data of the user from database and put them into a list.
     *
     * @return the list with the data user
     */
    public String loadUserDetail() {
        userListDetail = ejbUserDetail.UserDetail(UserDetailController.userId);
        return null;
    }

    /**
     * Gets the list that contains the data user
     *
     * @return
     */
    public List<ItUser> getUserListDetail() {

        return userListDetail;
    }

    /**
     * Return the user who makes the changes
     *
     * @return the user who makes the changes
     */
    public String returnLoggedUser() {
        return CURRENT_USER_LOGIN;
    }

    /**
     * Sets the control variables to default value
     */
    public void defaultValueControlVariables() {
        UserDetailController.fromAddingRow = false;
        UserDetailController.editingMode = false;
        UserDetailController.toCancel = false;
        UserDetailController.pretendPushEdit = false;
        UserDetailController.disableSaveButton = true;
        UserDetailController.prevStatusId = -1;
    }

    /**
     * Sets the init control variables to default value 
     */
    public void defaultValueControlVariablesIni() {
        defaultValueControlVariables();
        UserDetailController.changes = false;
    }

    /**
     * Init the row edit
     *
     * @param event
     */
    public void onRowInit(RowEditEvent event) {

        String message, message_detail, client_id;

        DataTable dataTable = (DataTable) this.currentContext.getViewRoot().findComponent(TABLE_CLIENT_ID);

        message = "EDITING ROW";

        //Gets the user to modify
        ItUser managedUser = (ItUser) event.getObject();

        // If we are editing a row, we must disabled all the other buttons
        UserDetailController.editingMode = true;
        UserDetailController.disableSaveButton = true;
        int totalRows = userListDetail.size();

        // If we are modifing a row we can't add a new row --> Disable all the addButtons
        for (int i = 0; i < totalRows; i++) {
            Ajax.updateRow(dataTable, i);
        }

        try {
            if (UserDetailController.pretendPushEdit) {
                // the edition of the row come from programatically order (i.e adding row)
                LOGGER.info("Editing the new row added");
            } else {
                // The user was pushed the edit button
                message_detail = "Editing User " + managedUser.getUserCode() + " data";
                LOGGER.info(message_detail);

                // Store backup from the current data of the table
                //BillingWebUtilities.copyItUserList(userListDetail, userListDetailBackup);
                BillingWebUtilities.copyGenericList(userListDetail, userListDetailBackup);
                // Sets the modified fields to the new values
                managedUser.setModifDate(BillingWebDates.getCurrentTimestamp());
                managedUser.setModifUser(CURRENT_USER_LOGIN);
                // Show an informative message
                BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
            }
        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                message_detail = "PARSE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
        }
    }

    /**
     * Storage the data for the row edited
     *
     * @param event
     */
    public void onRowEdit(RowEditEvent event) {
        String message, message_detail;
        message = "SAVE EDIT ROW";

        // Retrieved the data that was modified
        ItUser managedUser = (ItUser) event.getObject();

        DataTable dataTable = (DataTable) this.currentContext.getViewRoot().findComponent(TABLE_CLIENT_ID);
        int pos = dataTable.getRowIndex();

        // Retrieve the old values from the table
        //BillingWebUtilities.copyList(userListDetailBackup, userListDetail);
        if (UserDetailController.fromAddingRow) {
            //Cancell the adding row: retrieve the old value for the prev and next row and delete the current row
            message_detail = "The adition of a new row for User " + managedUser.getUserCode() + " was cancelled";

            LOGGER.info(message_detail);
            BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
        } else {
            // Retrieves the old value for the current row
            message_detail = "The changes for User" + managedUser.getUserCode() + " was cancelled";

            LOGGER.info(message_detail);
            BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
        }

        // return the default values of the control variables
        defaultValueControlVariables();

        // Changes are made --> enabled saveButton
        UserDetailController.changes = true;

        message_detail = "Storage the modified data sucessfully";
        LOGGER.info(message_detail);
        BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
    }

    /**
     * Cancel the adding/editing row
     *
     * @param event
     */
    public void onRowCancel(RowEditEvent event) {

        ItUser managedUser;
        String message, message_detail;

        message = "CANCEL ROW EDIT";

        // Retrieved the data that was modified
        managedUser = (ItUser) event.getObject();

        try {
            //Retrieve the backup data table
            //BillingWebUtilities.copyItUserList(userListDetailBackup, userListDetail);
            BillingWebUtilities.copyGenericList(userListDetailBackup, userListDetail);

            if (UserDetailController.fromAddingRow) {
                message_detail = "The adition of a new row for User " + managedUser.getUserCode() + " was cancelled";
                LOGGER.info(message_detail);
                BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
            } else {

                message_detail = "The changes for User" + managedUser.getUserCode() + " was cancelled";
                LOGGER.info(message_detail);
                BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
            }

            // return the default values of the control variables
            defaultValueControlVariables();
        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
        }
    }

    /**
     * Adding a new row for the user
     */
    public void addRow() {

        String message, message_detail;
        DataTable dataTable;
        int pos, totalRows;
        ItUser currentUser = null;
        ItUser nextUser = null;
        ItUser newUser = null;
        Timestamp inputDate;

        message = "ADD ROW";

        try {
            inputDate = BillingWebDates.getCurrentTimestamp();

            //Sets the fromAddingRow value to true
            UserDetailController.fromAddingRow = true;

            // Store the current data
            //BillingWebUtilities.copyItUserList(userListDetail, userListDetailBackup);
            BillingWebUtilities.copyGenericList(userListDetail, userListDetailBackup);

            //Gets the currentUser data
            dataTable = (DataTable) this.currentContext.getViewRoot().findComponent(TABLE_CLIENT_ID);
            pos = dataTable.getRowIndex();

            totalRows = dataTable.getRowCount();

            currentUser = (ItUser) dataTable.getRowData();

            //Create and initialise the user to add to the new row
            newUser = new ItUser();
            newUser.from(currentUser);
            newUser.setStartDate(null);
            newUser.setInputDate(inputDate);
            newUser.setInputUser(CURRENT_USER_LOGIN);

            //Modify the mofify fields of the curren row        
            currentUser.setModifDate(inputDate);
            currentUser.setModifUser(CURRENT_USER_LOGIN);
            if (nextUser != null) {
                // If exists, modify the fields of the subsequent row
                nextUser.setModifDate(inputDate);
                nextUser.setModifUser(CURRENT_USER_LOGIN);
                newUser.setEndDate(null);
            } else {
                //populates the endDate of the new row to the defaultEndDate
                newUser.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
            }

            message_detail = "Adding new row for User " + currentUser.getUserCode() + " data";

            // Adding the new row to the table
            pos = pos + 1;
            userListDetail.add(pos, newUser);
            dataTable.setRowIndex(pos);

            //Updates the table
            Ajax.update(dataTable.getClientId());

            // It pretends to press the edit button (to show the new row in editing mode)
            UserDetailController.pretendPushEdit = true;
            // Push the edit button
            this.currentRequestContext.execute("jQuery('span.ui-icon-pencil').eq(" + pos + ").each(function(){jQuery(this).click()});");
            LOGGER.info(message_detail);
            BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                message_detail = "PARSE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
        }
    }

    public void deleteRow() {

        DataTable dataTable;
        ItUser prevUser;
        String message, message_detail;
        int pos;

        message = "DELETE ROW";

        try {
            //Gets the currentUser data
            dataTable = (DataTable) this.currentContext.getViewRoot().findComponent(TABLE_CLIENT_ID);
            pos = dataTable.getRowIndex();
            if (pos > 0) {
                // if the row isn't the last row, set the endDate of the previous row to default endDate
                dataTable.setRowIndex(pos - 1);
                prevUser = (ItUser) dataTable.getRowData();
                prevUser.setEndDate(BillingWebDates.getTimestampDefaulEndDate());
                dataTable.setRowIndex(pos);
                message_detail = "The row was deleted and the End Date of the previous row was set to 31/12/9999";
            } else {
                message_detail = "The row was deleted. No rows exists to this user.";
            }
            // Delete the row
            UserDetailController.userListDetail.remove(pos);

            BillingWebMessages.billingWebDialogMessageInfo(this.currentRequestContext, message, message_detail);
        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebParseException")) {
                message_detail = "PARSE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
        }

    }

    public void saveChanges() {

        String message, message_detail;
        int r;

        message = "SAVE CHANGES INTO THE SISTEM";
        // First of all deletes all date from the user in the database

        try {
            //Deletes the current data in the database
            r = ejbUserDetail.DeleteUserDetail(userId);

            if (UserDetailController.userListDetail.size() > 0) {
                // There are data to store
                if (r == 1) {
                    ejbUserDetail.AddUserListDetail(userListDetail);
                    message_detail = "The changes was saved";

                    LOGGER.info(message_detail);
                    BillingWebMessages.billingWebLocalMessageInfo(this.currentContext, "messages", message, message_detail);
                }
            }

        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebDataAccessException")) {
                message_detail = "DATABASE ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);

            } else {
                message_detail = "ERROR - " + ne.getMessage();
                LOGGER.fatal(message_detail);
                BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
            }

        } catch (Exception e) {
            message_detail = "ERROR - " + e.getCause().toString();
            LOGGER.fatal(message_detail);
            BillingWebMessages.billingWebDialogMessageFatal(this.currentRequestContext, message, message_detail);
        }
    }

    public void changeStatus(ValueChangeEvent e) {
        Integer oldStatusId = (Integer) e.getOldValue();
        Integer newStatusId = (Integer) e.getNewValue();
        Integer backupStatusId;

        // Gets the current User
        DataTable dataTable = (DataTable) this.currentContext.getViewRoot().findComponent(TABLE_CLIENT_ID);
        ItUser currentUser = (ItUser) dataTable.getRowData();

        // Gets the row of the current User
        String row = this.currentContext.getExternalContext().getRequestParameterMap().get("currentRow");
        this.setCurrentRow(Integer.parseInt(row));

        // Gets the status Id previous to the edition of the row
        backupStatusId = userListDetailBackup.get(this.getCurrentRow()).getStatusId();

        //MtStatusRecord statusRecordOld = (MtStatusRecord) ejbStatus.findAllStatusForEntity(oldStatusId).get(0);
        //MtStatusRecord statusRecordNew = (MtStatusRecord) ejbStatus2.findAllStatusForEntity(newStatusId).get(0);
        String statusCodeBackup = ejbStatus.findStatusCodeForEntityFromStatusId(ejbStatus.findUserEntityId(), backupStatusId);

        if ("CANCEL".equals(statusCodeBackup)) {
            // The original status was cancel status --> not to change
            BillingWebMessages.billingWebDialogMessageError(this.currentRequestContext, "STATUS ERROR", "The status CANCEL can not be changed");
            currentUser.setStatusId(backupStatusId);
            Ajax.updateColumn(dataTable, dataTable.getRowIndex());
        } else {
            // The original status was not cancel --> could be change
            //String statusCodeOld = ejbStatus.findStatusCodeForEntityFromStatusId(ejbStatus.findUserEntityId(), oldStatusId);
            String statusCodeNew = ejbStatus.findStatusCodeForEntityFromStatusId(ejbStatus.findUserEntityId(), newStatusId);
            if ("CANCEL".equals(statusCodeNew)) {
                // Change to CANCEL
                this.setToCancel(true);
                // Show confirmation dialog to change to cancel status
                this.currentRequestContext.execute("PF('confirmCancelDialog').show();");
            } else if (UserDetailController.prevStatusId > -1) {
                String prevStatusCode = ejbStatus.findStatusCodeForEntityFromStatusId(ejbStatus.findUserEntityId(), UserDetailController.prevStatusId);
                if ("CANCEL".equals(prevStatusCode)) {
                    // the previous selection into the edition was cancel --> revert changes
                    retrieveOldStatus();
                }
            }

        }
        UserDetailController.prevStatusId = newStatusId;
    }

    public void cancelEntityOK() {

        DataTable dataTable;
        int currentPos, lastPos, i;
        ItUser userToCancel;
        Integer cancelStatusId;

        //Gets the currentUser data
        dataTable = (DataTable) this.currentContext.getViewRoot().findComponent(TABLE_CLIENT_ID);
        //currentPos = dataTable.getRowIndex();
        currentPos = UserDetailController.currentRow;
        lastPos = dataTable.getRowCount() - 1;

        if (this.isToCancel()) {
            cancelStatusId = this.ejbStatus.findStatusIdForEntityFromStatusCode(this.ejbStatus.findUserEntityId(), "CANCEL");

            if (currentPos < lastPos) {
                for (i = currentPos + 1; i <= lastPos; i++) {
                    dataTable.setRowIndex(i);
                    userToCancel = (ItUser) dataTable.getRowData();
                    userToCancel.setStatusId(cancelStatusId);
                    Ajax.updateRow(dataTable, i);
                }
            }
        }
    }

    public void cancelEntityKO() {
        retrieveOldStatus();
    }

    /**
     * Retrieve old status to the data
     */
    public void retrieveOldStatus() {
        DataTable dataTable;
        int currentPos, lastPos;
        ItUser currentUser, userBackup;

        dataTable = (DataTable) this.currentContext.getViewRoot().findComponent(TABLE_CLIENT_ID);
        lastPos = dataTable.getRowCount() - 1;
        //currentPos = dataTable.getRowIndex();
        currentPos = UserDetailController.currentRow;

        if (this.isToCancel()) {
            //Recovery the old values
            currentUser = (ItUser) dataTable.getRowData();
            //System.out.println("actualStatusId: " + currentUser.getStatusId());
            if (UserDetailController.fromAddingRow) {
                //The old value of was equal to the previous row
                userBackup = userListDetailBackup.get(currentPos - 1);
                currentUser.setStatusId(userBackup.getStatusId());
                Ajax.updateRow(dataTable, currentPos);
            }

            for (int i = currentPos; i <= lastPos; i++) {
                dataTable.setRowIndex(i);
                currentUser = (ItUser) dataTable.getRowData();
                userBackup = userListDetailBackup.get(i);
                currentUser.setStatusId(userBackup.getStatusId());
                Ajax.updateRow(dataTable, i);
            }

        }
    }

}
