/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.utils;

import es.billingweb.model.tables.pojos.VUserProfile;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author catuxa
 */
public class BillingWebSessionUtils {

    public final static String USER_CODE = "user_code";
    public final static String USER_ID = "user_id";
    public final static String PROFILE_CODE = "profile_code";
    public final static String PROFILE_ID = "profile_id";
    public final static String LAST_URL="last_url";

    private final static ResourceBundle GENERAL_PROPERTIES = ResourceBundle.getBundle("properties.general_config");
    public final static String CONTEXT_KEY = GENERAL_PROPERTIES.getString("KEY_CONTEXT");

    private String currentUserCode;

    /**
     * Gets the current Faces Context
     *
     * @return the current Faces Context
     */
    public static FacesContext getBillingWebFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Gets the external FacesContext
     *
     * @return
     */
    public static ExternalContext getBillingWebExternalContext() {
        return getBillingWebFacesContext().getExternalContext();
    }

    /**
     * Gets the object HttpSession from the current session
     *
     * @return the current HttpSession
     */
    public static HttpSession getSession() {
        /* return (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);*/
        return (HttpSession) getBillingWebExternalContext().getSession(false);
    }

    /**
     * Gets the HttpServletRequest object of the application context
     *
     * @return the current HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) getBillingWebExternalContext().getRequest();
    }

    /**
     * Gets the HttpServletResponse object of the application context
     *
     * @return the current HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return (HttpServletResponse) getBillingWebExternalContext().getResponse();
    }

    /**
     *
     * @param key key for the context of the current instance
     * @param userLogin user to add to the current instance
     */
    public static void addCurrentUserLogin(String key, VUserProfile userLogin) {
        //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, userLogin);
        getBillingWebExternalContext().getSessionMap().put(key, userLogin);
    }

    /**
     *
     * @return the current user logged
     */
    public static VUserProfile getCurrentUserLogged() {
        //return (VUserProfile) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(BillingWebSessionUtils.CONTEXT_KEY);
        return (VUserProfile) getBillingWebExternalContext().getSessionMap().get(BillingWebSessionUtils.CONTEXT_KEY);
    }
    
    
    /**
     *
     * @param key key for the context of the current instance
     * @param lastURL last URL visited
     */
    public static void addlastURL(String key, String lastURL) {
        //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, userLogin);
        getBillingWebExternalContext().getSessionMap().put(key, lastURL);
    }

    /**
     *
     * @return the last URL visited
     */
    public static String getLastURL() {
        //return (VUserProfile) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(BillingWebSessionUtils.CONTEXT_KEY);
        return (String) getBillingWebExternalContext().getSessionMap().get(LAST_URL);
    }
    

    
}
