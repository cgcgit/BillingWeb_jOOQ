/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.utils;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;

/**
 * Message package
 *
 * Class that contents some methods to show JSF messages
 *
 * @author catuxa
 * @since march 2017
 * @version 1.0.0
 *
 */
public class BillingWebMessages {

    
    /**____________ GLOBAL MESSAGE _____________**/
    
    
    /**
     * Shows a message (with a delay)
     *
     * @param messageType type of the message (acesMessage.SEVERITY_*)
     * @param context the current FacesContext
     * @param textMessage the message to show
     */
    public static void billingWebGlobalMessage(Severity messageType, FacesContext context, String textMessage) {

        FacesMessage message = new FacesMessage(messageType, textMessage, null);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    /**
     * Shows an informative message (with a delay)
     *
     * @param messageType type of the message (acesMessage.SEVERITY_*)
     * @param context the current FacesContext
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebGlobalMessage(Severity messageType, FacesContext context, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(messageType, textMessage, textMessageDetail);

       // context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }
    
    
    /**
     * Shows an informative message (with a delay)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     */
    public static void billingWebGlobalMessageInfo(FacesContext context, String textMessage) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, textMessage, null);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    /**
     * Shows an informative message (with a delay)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebGlobalMessageInfo(FacesContext context, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, textMessage, textMessageDetail);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }
    
     /**
     * Shows an error message (with a delay)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     */
    public static void billingWebGlobalMessageError(FacesContext context, String textMessage) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, textMessage, null);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    /**
     * Shows an error message (with a delay)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebGlobalMessageError(FacesContext context, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, textMessage, textMessageDetail);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }
    
    

    /**
     * Shows a fatal message (with a delay)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     */
    public static void billingWebGlobalMessageFatal(FacesContext context, String textMessage) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, textMessage, null);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    /**
     * Shows a fatal message (with a delay)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebGlobalMessageFatal(FacesContext context, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, textMessage, textMessageDetail);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }
    
    
    /**_________ LOCAL MESSAGE __________**/
    
    
    /**
     * Shows a message (with a delay)
     * 
     * @param messageType type of the message (acesMessage.SEVERITY_*)
     * @param currentContext the current FacesContext
     * @param externalContext the external FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     */
    public static void billingWebLocalMessage(Severity messageType, FacesContext currentContext, ExternalContext externalContext, String uiClientId, String textMessage) {
                
        FacesMessage message = new FacesMessage(messageType, textMessage, null);

        
        externalContext.getFlash().setKeepMessages(true);
        currentContext.addMessage(uiClientId, message);

        currentContext.getApplication().getNavigationHandler().handleNavigation(currentContext, null, textMessage);

    }

    /**
     * Shows a message (with a delay)
     *
     * @param messageType type of the message (acesMessage.SEVERITY_*)
     * @param context the current FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebLocalMessage(Severity messageType, FacesContext context, String uiClientId, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(messageType, textMessage, textMessageDetail);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(uiClientId, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);
   }
    
    
    /**
     * Shows an informative message (with a delay)
     *
     * @param context the current FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     */
    public static void billingWebLocalMessageInfo(FacesContext context, String uiClientId, String textMessage) {
                
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, textMessage, null);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(uiClientId, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    /**
     * Shows an informative message (with a delay)
     *
     * @param context the current FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebLocalMessageInfo(FacesContext context, String uiClientId, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, textMessage, textMessageDetail);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(uiClientId, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);
   }

   

    /**
     * Shows an error message (with a delay)
     *
     * @param context the current FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     */
    public static void billingWebLocalMessageError(FacesContext context, String uiClientId, String textMessage) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, textMessage, null);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(uiClientId, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    
    /**
     * Shows an error message (with a delay)
     *
     * @param context the current FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebLocalMessageError(FacesContext context, String uiClientId, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, textMessage, textMessageDetail);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(uiClientId, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }
    
    
    /**
     * Shows a fatal message (with a delay)
     *
     * @param context the current FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     */
    public static void billingWebLocalMessageFatal(FacesContext context, String uiClientId, String textMessage) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, textMessage, null);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(uiClientId, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    
    /**
     * Shows a fatal message (with a delay)
     *
     * @param context the current FacesContext
     * @param uiClientId the id for the jsf message ui
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebLocalMessageFatal(FacesContext context, String uiClientId, String textMessage, String textMessageDetail) {

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, textMessage, textMessageDetail);

        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(uiClientId, message);

        context.getApplication().getNavigationHandler().handleNavigation(context, null, textMessage);

    }

    /**
     * ****************************
     */
    /**
     * Show an informative dialog message (to click the confirm button)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     */
    public static void billingWebDialogMessageInfo(RequestContext context, String textMessage) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, textMessage, null);
        context.showMessageInDialog(message);

    }

    /**
     * Show an informative dialog message (to click the confirm button)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebDialogMessageInfo(RequestContext context, String textMessage, String textMessageDetail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, textMessage, textMessageDetail);
        context.showMessageInDialog(message);

    }

    /**
     * Show a fatal dialog message (to click the confirm button)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     */
    public static void billingWebDialogMessageFatal(RequestContext context, String textMessage) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, textMessage, null);
        context.showMessageInDialog(message);

    }

    /**
     * Show a fatal dialog message (to click the confirm button)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebDialogMessageFatal(RequestContext context, String textMessage, String textMessageDetail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, textMessage, textMessageDetail);
        context.showMessageInDialog(message);

    }

    /**
     * Show an error dialog message (to click the confirm button)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     */
    public static void billingWebDialogMessageError(RequestContext context, String textMessage) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, textMessage, null);
        context.showMessageInDialog(message);

    }

    /**
     * Show an error dialog message (to click the confirm button)
     *
     * @param context the current FacesContext
     * @param textMessage the message to show
     * @param textMessageDetail the detail of the message
     */
    public static void billingWebDialogMessageError(RequestContext context, String textMessage, String textMessageDetail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, textMessage, textMessageDetail);
        context.showMessageInDialog(message);

    }

}
