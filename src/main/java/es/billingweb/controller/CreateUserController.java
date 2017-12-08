/*
 * Copyright 2016 catuxa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.billingweb.controller;

import es.billingweb.ejb.ManageUsersEJBLocal;
import es.billingweb.ejb.ProfilesEJBLocal;
import es.billingweb.ejb.StatusEJBLocal;
import es.billingweb.model.tables.pojos.ItUser;
import es.billingweb.model.tables.pojos.VUserProfile;
import es.billingweb.utils.BillingWebUtilities;
import es.billingweb.utils.BillingWebDates;
import es.billingweb.utils.BillingWebMessages;
import es.billingweb.utils.BillingWebSessionUtils;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.faces.view.ViewScoped;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javax.ejb.EJBException;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.context.RequestContext;

/**
 * Managed Bean to control the create user page.
 *
 * @author catuxa
 */
@Named
@ViewScoped
public class CreateUserController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ItUser managedUser;
    private VUserProfile currentUserLogin;
    private static final ResourceBundle PAGE_PROPERTIES = ResourceBundle.getBundle("properties.pages");

    private static final Logger LOGGER = LogManager.getLogger();

    private Date startDate;
    private Date endDate;
    private Date inputDate;

    //private final Date startHour = BillingWebDates.getDefaultStartHour_D();
    //private final Date endHour = BillingWebDates.getDefaultEndHour_D();
    //private final String constantURI = pageProperties.getString("constantURI");
    private final String TAIL_URL = PAGE_PROPERTIES.getString("TAIL_URL");
    private final FacesContext CURRENT_CONTEXT = FacesContext.getCurrentInstance();
    private final RequestContext CURRENT_REQUEST_CONTEXT = RequestContext.getCurrentInstance();
    private final String CURRENT_URL = CURRENT_CONTEXT.getViewRoot().getViewId();
    private final String BACK_URL = PAGE_PROPERTIES.getString("USER_LIST_PAGE");
    private static String fromURL;

    @EJB
    private ManageUsersEJBLocal ejbUser;

    @EJB
    private ProfilesEJBLocal ejbProfile;

    @EJB
    private StatusEJBLocal ejbStatus;

    /* private final Date defaultStartDate = Utilities.getDefaultStartDate_D();
    private final Date defaultEndDate = Utilities.getDefaultEndDate_D();
    private final Date currentDate= Utilities.getCurrentDate();
     */
    @PostConstruct
    public void init() {
        try {
            this.managedUser = new ItUser();

            //this.currentUserLogin = (VUserProfile) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(SessionUtils.CONTEXT_KEY);
            this.currentUserLogin = BillingWebSessionUtils.getCurrentUserLogged();
            //initialized default values
            this.managedUser.setInputUser(currentUserLogin.getUserCode());

            this.inputDate = BillingWebDates.getCurrentDateLongView();
            this.startDate = BillingWebDates.getDateDefaultStartDateLongView();
            this.endDate = BillingWebDates.getDateDefaultEndDateLongView();
            //  this.startHour=BillingWebDates.getDefaultStartHour_D();
            //  this.endHour=BillingWebDates.getDefaultEndHour_D();
            //   this.inputHour=BillingWebDates.getCurrentHour_D();

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

    }

    public String getFromURL() {
        System.out.println("getFromURL: " + fromURL);
        return fromURL;
    }

    public void setFromURL(String fromURL) {
        System.out.println("setFromURL: " + fromURL);
        CreateUserController.fromURL = fromURL;
    }

    public String returnToPage() {
        String page;

        System.out.println("eoooo....");
        //this.backURL = pageProperties.getString("USER_LIST");
        LOGGER.debug("Return to previous page: " + this.BACK_URL);
        page = BACK_URL + TAIL_URL;
        LOGGER.debug("Redirecting: " + page);

        return page;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getInputDate() {
        return inputDate;
    }

    public void setInputDate(Date inputDate) {
        this.inputDate = inputDate;
    }

    /**
     *
     * @return the user to manage
     */
    public ItUser getManagedUser() {
        return managedUser;
    }

    /**
     *
     * @param managedUser
     */
    public void setManagedUser(ItUser managedUser) {
        this.managedUser = managedUser;
    }

    public String saveUser() {
        int r = 0;
        String url = this.CURRENT_URL;

        try {
            Timestamp inputTimeStamp = BillingWebDates.dateToTimestamp(this.inputDate);
            Timestamp startTimeStamp = BillingWebDates.dateToTimestamp(this.startDate);
            Timestamp endTimeStamp = BillingWebDates.dateToTimestamp(this.endDate);

            if (startTimeStamp.compareTo(endTimeStamp) >= 0) {
                //addMessageError("Start date must be less or equal than end date");
                BillingWebMessages.billingWebLocalMessageError(CURRENT_CONTEXT, "messages", "Start date must be less or equal than end date");
                LOGGER.error("Start date must be less or equal than end date");
            } else {

                String newPassword = BillingWebUtilities.getMD5(this.managedUser.getPassword());

                this.managedUser.setInputDate(inputTimeStamp);
                this.managedUser.setStartDate(startTimeStamp);
                this.managedUser.setEndDate(endTimeStamp);
                this.managedUser.setPassword(newPassword);

                LOGGER.info("Saving user " + this.managedUser.getUserCode() + " SD: " + this.managedUser.getStartDate().toString() + " - ED" + this.managedUser.getEndDate().toString() + "(modify by " + this.currentUserLogin.getProfileCode() + ")");

                r = ejbUser.addUser(this.managedUser);

                switch (r) {
                    case 1:
                        BillingWebMessages.billingWebGlobalMessageInfo(CURRENT_CONTEXT, "User was saved sucessfully.");
                        url = this.returnToPage();
                        break;
                    case 0:
                        //showDialogMessageError("The user was not saved.");
                        BillingWebMessages.billingWebDialogMessageError(CURRENT_REQUEST_CONTEXT, "The user was not saved.");
                        break;
                    default:
                        //showDialogMessageError("DATABASE ERROR - the user whas not saved");
                        BillingWebMessages.billingWebDialogMessageError(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - the user whas not saved.");
                }
            }

        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            if (ne.getClass().getName().equals("es.billingweb.exception.BillingWebDataAccessException")) {
                //showDialogMessageFatal("DATABASE ERROR - " + ne.getMessage());
                BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                LOGGER.fatal("DATABASE ERROR - " + ne.getMessage());
            }
        } catch (Exception e) {
            //showDialogMessageFatal("ERROR - " + e.getCause().toString());
            BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "ERROR - " + e.getCause().toString());
            LOGGER.fatal("ERROR - " + e.getCause().toString());
        } finally {

        }

        LOGGER.debug("after save redirecting to " + url);
        return url;
    }

    public void deleteUser(VUserProfile user) {
        LOGGER.info("Deleting user:" + user.getUserCode());
        ejbUser.deleteUser(user.getUserId());
    }

    public void modifyUser() {
        LOGGER.info("Modifying user:" + this.managedUser.getUserCode());
        ejbUser.updateUser(this.managedUser);
    }
    
    /*

    public void showDialogMessageError(String error) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void showDialogMessageInfo(String info) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, info, null);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void showDialogMessageFatal(String fatal) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, fatal, null);
        RequestContext.getCurrentInstance().showMessageInDialog(message);

    }

    ///
    public void addMessageError(String error) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null);
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage("messages", message);
    }

    public void addMessageInfo(String info) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, info, null);
        FacesContext context = FacesContext.getCurrentInstance();
        //context.addMessage(null, message);
        context.addMessage("messages", message);
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(),
                null,
                "save");
    }

    public void addMessageFatal(String fatal) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, fatal, null);
        FacesContext context = FacesContext.getCurrentInstance();
        //context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);
    }

*/

    public void cleanInfo() {
        LOGGER.debug("limpiando");
        BillingWebMessages.billingWebLocalMessageInfo(CURRENT_CONTEXT, null, "Cleaning data");
        System.out.println("fromURL: " + this.fromURL);
    }

    public String returnClick() {
        String returnPage = CreateUserController.fromURL + TAIL_URL;
        return returnPage;
    }

    public String saveUser_() {
        System.out.println("fromURL: " + this.fromURL);
        return null;
    }

    public String saveURL() {
        String url = BillingWebSessionUtils.getLastURL();
        System.out.println("saveURL!!! - url: " + url);
        CreateUserController.fromURL = url;
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest servletRequest = (HttpServletRequest) ctx.getExternalContext().getRequest();
        // returns something like "/myapplication/home.faces" --> Equivale a la expresion EL #{request.requestURI}
        //Exactly this value is reuseable in <h:outputLink value> or plain <a href>. Note that you can't use it as navigation case outcome.
        String fullURI = servletRequest.getRequestURI();

        System.out.println("currentURL: " + fullURI);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(BillingWebSessionUtils.LAST_URL, fullURI);
        //return fullURI;    
        BillingWebSessionUtils.addlastURL(BillingWebSessionUtils.LAST_URL, fullURI);
        return null;
    }
}
