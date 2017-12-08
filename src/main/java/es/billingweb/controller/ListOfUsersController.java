package es.billingweb.controller;

import es.billingweb.ejb.ListEJBLocal;
import es.billingweb.model.tables.pojos.VUserProfile;
import es.billingweb.exception.*;
import es.billingweb.utils.*;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Managed Bean to control the list of the system's users page.
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */
@Named
@SessionScoped
public class ListOfUsersController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ResourceBundle component = ResourceBundle.getBundle("properties.ui_component");
    private static final String message_client_id = component.getString("MID_LIST_OF_USERS");

    private static final ResourceBundle pages = ResourceBundle.getBundle("properties.pages");
    private static final String CURRENT_URL = pages.getString("USER_LIST_PAGE");
    
    private final FacesContext CURRENT_CONTEXT = FacesContext.getCurrentInstance();
    

    @EJB
    private ListEJBLocal ejbUsers;
    private VUserProfile selectedUser;

    // private final String CURRENT_URL= FacesContext.getCurrentInstance().getViewRoot().getViewId();
    private String currentURL;

    /**
     * Gets all the current active users in the system
     *
     * @return list of all active users in the system
     */
    public List<VUserProfile> usersList() {

        List<VUserProfile> list = null;
        try {
            list = ejbUsers.findAllUsers();            

        } catch (BillingWebDataAccessException e) {
            BillingWebMessages.billingWebLocalMessageError(CURRENT_CONTEXT, message_client_id, e.getMessage() + e.getCause().toString());
            //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage() + e.getCause().toString(), null);
            //CONTEXT.addMessage(null, message);
            LOGGER.error("ERROR - " + e.getMessage() + e.getCause().toString());
        }
        return list;
    }

    /**
     * Gets the selected user in the table
     *
     * @return the user was selected
     */
    public VUserProfile getSelectedUser() {
        return selectedUser;
    }

    /**
     * Sets the selected user in the table
     *
     * @param selectedUser the user was selected
     */
    public void setSelectedUser(VUserProfile selectedUser) {
        this.selectedUser = selectedUser;
    }

    public String getCurrentURL() {
/*
        // facesContext.getViewRoot().getViewId(); --> Equivale a la expresion EL #{view.viewId} --> para <h:button outcome>
        HttpServletRequest servletRequest = (HttpServletRequest) CURRENT_CONTEXT.getExternalContext().getRequest();
        // returns something like "/myapplication/home.faces" --> Equivale a la expresion EL #{request.requestURI}
        //Exactly this value is reuseable in <h:outputLink value> or plain <a href>. Note that you can't use it as navigation case outcome.
        String fullURI = servletRequest.getRequestURI();

        System.out.println("currentURL: " + fullURI);
        CURRENT_CONTEXT.getExternalContext().getSessionMap().put(BillingWebSessionUtils.LAST_URL, fullURI);
        return fullURI;
**/
        return CURRENT_URL;
    }

}
