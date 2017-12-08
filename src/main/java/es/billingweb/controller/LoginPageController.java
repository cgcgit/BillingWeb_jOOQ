package es.billingweb.controller;

import es.billingweb.ejb.BillingWebUserLocal;
import es.billingweb.model.tables.pojos.VUserProfile;
import es.billingweb.utils.BillingWebSessionUtils;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Managed Bean to control the application's login page.
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */

@Named
@SessionScoped
public class LoginPageController implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ResourceBundle pageProperties = ResourceBundle.getBundle("properties.pages");
    private final String tail_url = pageProperties.getString("TAIL_URL");
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject
    private VUserProfile applicationUser;

    private boolean loggedIn;
    private String protectedURL;

    @EJB
    private BillingWebUserLocal applicationUserEJB;

    /**
     * Gets the current user of the application.
     *
     * @return the application's current user
     */
    public VUserProfile getApplicationUser() {
        return applicationUser;
    }

    /**
     * Sets the current user of the application.
     *
     * @param applicationUser
     */
    public void setApplicationUser(VUserProfile applicationUser) {
        this.applicationUser = applicationUser;
    }

    /**
     * Gets the user code for the current user application
     *
     * @return current user code
     */
    public String getApplicationUserCode() {
        return this.applicationUser.getUserCode();
    }

    /**
     * Indicates if the user was logged into the application
     *
     * @return true if the user is logged
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Sets if the user was logged into the application (true) or not (false).
     *
     * @param loggedIn true if the user is logged
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Gets the substring of the protected URL for the application's user.
     *
     * @return substring /protected/[sub_URL]
     */
    public String getProtectedURL() {
        return protectedURL;
    }

    public void setProtectedURL(String protectedURL) {
        this.protectedURL = protectedURL;
    }

    public void initialValues() {
        //this.userLogin=new VUserProfile();
        if (this.applicationUser != null) {
            this.applicationUser.setUserId(0);
            this.applicationUser.setUserCode("");
            this.applicationUser.setProfileId(0);
            this.applicationUser.setProfileCode("");
            this.applicationUser.setPassword("");
        }

    }

    /**
     * logout event, invalidate session
     *
     * @return the welcome_page (login_page)
     */
    public String logout() {
        String url = pageProperties.getString("WELCOME_PAGE");
        HttpSession session = BillingWebSessionUtils.getSession();
        LOGGER.info("Logout user session - user" + this.getApplicationUserCode());
        session.invalidate();
        return url + this.tail_url;
    }

    /**
     * Validates the login of the application user
     *
     * @return the home page of the user, if validation is true, incorrect
     * credentials page otherwise
     */
    public String loginValidate() {
        VUserProfile user = null;
        user = applicationUserEJB.login(applicationUser);
        String page;
        page = pageProperties.getString("INVALID_CREDENTIALS_PAGE");;
        this.loggedIn = false;

        try {
            if (user != null) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(BillingWebSessionUtils.CONTEXT_KEY, user);
                //redirect = getHomePage(user.getProfileCode());
                page = pageProperties.getString("HOME_PAGE");
                //this.protectedURL=getProtectURL(user.getProfileCode());
                this.loggedIn = true;
                LOGGER.info("User " + user.getUserCode() + " has logged succesfully");
                LOGGER.debug("User " + user.getUserCode() + " has logged succesfully");
                //return redirect;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "WARN", "Incorrect Credentials"));
                LOGGER.warn("User hasn't logged succesfully");
                LOGGER.error("User hasn't logged succesfully");
                LOGGER.debug("User hasn't logged succesfully");
            }
        } catch (Exception e) {
            //
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "FATAL ERROR", "Error"));
            LOGGER.fatal("FATAL ERROR - " + e.getMessage());
        }

        LOGGER.info("Redirecting to the page " + page);
        return page + tail_url;
    }

}
