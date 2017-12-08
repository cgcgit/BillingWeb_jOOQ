package es.billingweb.controller;

import es.billingweb.model.tables.pojos.VUserProfile;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Managed Bean to control the main template page.
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */
@Named
@ViewScoped
public class MainTemplateController implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ResourceBundle pages = ResourceBundle.getBundle("properties.pages");
    private final ResourceBundle general = ResourceBundle.getBundle("properties.general_config");
    
    //this.currentUserLogin = SessionUtils.getCurrentUserLogged();

    private static final Logger LOGGER = LogManager.getLogger();

    // String that contains the previous URL
    private String backUrl;

    public String back() {
        return backUrl + "?faces-redirect=true";
    }

    public String getBackurl() {
        return backUrl;
    }

    public void setBackurl(String backurl) {
        this.backUrl = backurl;
    }

    /**
     * Validates that access is made by a user who has already log into the system
     */
    public void sessionValidate() {

        FacesContext context = FacesContext.getCurrentInstance();
        String viewId = context.getViewRoot().getViewId();
        //Gets the invalid credential page
        String url = pages.getString("ROOT_URL") + pages.getString("INVALID_CREDENTIALS_PAGE");
        
        String key = general.getString("KEY_CONTEXT");

        LOGGER.debug("view: " + viewId);
        try {
            VUserProfile sessionUser = (VUserProfile) context.getExternalContext().getSessionMap().get(key);

            if (sessionUser == null) {
                LOGGER.error("ERROR - Invalid access to the page " +  viewId +" - not exists an active session for this access");
                context.getExternalContext().redirect(url);
            } else {
                LOGGER.debug("VALID ACCESS to the page " +  viewId + " - User registered: " + sessionUser.getUserCode());

            }
        } catch (Exception e) {
            // log to register errors
            LOGGER.error("ERROR accesing to the page " +  viewId + " - " + e.getMessage());
        }

    }

}
