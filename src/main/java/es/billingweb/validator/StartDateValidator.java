/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.validator;

import es.billingweb.model.tables.pojos.ItUser;
import es.billingweb.model.tables.pojos.MtEquipmentType;
import es.billingweb.model.tables.pojos.MtProductType;
import es.billingweb.model.tables.pojos.MtPromotionType;
import es.billingweb.model.tables.pojos.MtServiceType;
import es.billingweb.utils.BillingWebDates;
import java.sql.Timestamp;
import java.util.Date;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Components;
import org.primefaces.component.api.UIData;
import org.primefaces.component.datatable.DataTable;

/**
 *
 * @author catuxa
 */
@FacesValidator("startDateValidator")
public class StartDateValidator implements Validator {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String error_message, error_message_detail;
        Timestamp timestampCurrentValueStartDate, timestampCurrentValueEndDate,
                timestampPrevValueStartDate, timestampPrevValueEndDate;
        String stringPrevValueStartDate, stringPrevValueEndDate;
        Date datePrevValueStartDate, datePrevValueEndDate;
        String stringCurrentValueStartDate, stringCurrentValueEndDate;
        Date dateCurrentValueStartDate, dateCurrentValueEndDate, dateDesiredValuePrevEndDate;
        int currentPos, prevPos;
        FacesMessage faces_message = new FacesMessage();
        String clientId, parentId, className;
        boolean result = true;
        boolean continueValidation = true;

        error_message = "START DATE ERROR";

        //className = value.getClass().getName();

        // The value can't be null
        if (value == null) {
            error_message_detail = "Error - The Start Date can't be null";
            faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
            faces_message.setSummary(error_message);
            faces_message.setDetail(error_message_detail);
            error_message_detail = error_message_detail + "client id " + component.getClientId(context);
            LOGGER.error(error_message_detail);
            throw new ValidatorException(faces_message);
        }
        // Gets the table
        DataTable dataTable = (DataTable) Components.getClosestParent(component, UIData.class);

        currentPos = dataTable.getRowIndex();        
        Object data = dataTable.getRowData();        
        className=data.getClass().getName();

        try {
            switch (className) {
                case "es.billingweb.model.tables.pojos.ItUser":
                    timestampCurrentValueStartDate = (Timestamp) value;
                    timestampCurrentValueEndDate = ((ItUser) dataTable.getRowData()).getEndDate();

                    if (currentPos != 0) {
                        prevPos = currentPos - 1;
                        dataTable.setRowIndex(prevPos);
                        timestampPrevValueStartDate = ((ItUser) dataTable.getRowData()).getStartDate();
                        timestampPrevValueEndDate = ((ItUser) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampPrevValueStartDate = null;
                        timestampPrevValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtProductType":
                    timestampCurrentValueStartDate = (Timestamp) value;
                    timestampCurrentValueEndDate = ((MtProductType) dataTable.getRowData()).getEndDate();

                    if (currentPos != 0) {
                        prevPos = currentPos - 1;
                        dataTable.setRowIndex(prevPos);
                        timestampPrevValueStartDate = ((MtProductType) dataTable.getRowData()).getStartDate();
                        timestampPrevValueEndDate = ((MtProductType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampPrevValueStartDate = null;
                        timestampPrevValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtServiceType":
                    timestampCurrentValueStartDate = (Timestamp) value;
                    timestampCurrentValueEndDate = ((MtServiceType) dataTable.getRowData()).getEndDate();

                    if (currentPos != 0) {
                        prevPos = currentPos - 1;
                        dataTable.setRowIndex(prevPos);
                        timestampPrevValueStartDate = ((MtServiceType) dataTable.getRowData()).getStartDate();
                        timestampPrevValueEndDate = ((MtServiceType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampPrevValueStartDate = null;
                        timestampPrevValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtEquipmentType":
                    timestampCurrentValueStartDate = (Timestamp) value;
                    timestampCurrentValueEndDate = ((MtEquipmentType) dataTable.getRowData()).getEndDate();

                    if (currentPos != 0) {
                        prevPos = currentPos - 1;
                        dataTable.setRowIndex(prevPos);
                        timestampPrevValueStartDate = ((MtEquipmentType) dataTable.getRowData()).getStartDate();
                        timestampPrevValueEndDate = ((MtEquipmentType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampPrevValueStartDate = null;
                        timestampPrevValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtPromotionType":
                    timestampCurrentValueStartDate = (Timestamp) value;
                    timestampCurrentValueEndDate = ((MtPromotionType) dataTable.getRowData()).getEndDate();

                    if (currentPos != 0) {
                        prevPos = currentPos - 1;
                        dataTable.setRowIndex(prevPos);
                        timestampPrevValueStartDate = ((MtPromotionType) dataTable.getRowData()).getStartDate();
                        timestampPrevValueEndDate = ((MtPromotionType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampPrevValueStartDate = null;
                        timestampPrevValueEndDate = null;
                    }
                    break;
                default:
                    error_message_detail = "ERROR - The class object " + className + "is not expect";

                    faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    faces_message.setSummary(error_message);
                    faces_message.setDetail(error_message_detail);
                    LOGGER.fatal(error_message_detail);
                    //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                    throw new ValidatorException(faces_message);
            }

            stringCurrentValueStartDate = BillingWebDates.timestampToStringShortView(timestampCurrentValueStartDate);
            dateCurrentValueStartDate = BillingWebDates.shortStringToDate(stringCurrentValueStartDate);
            dateDesiredValuePrevEndDate = BillingWebDates.substractDaysToDateShortView(dateCurrentValueStartDate, 1);

            if (timestampCurrentValueEndDate != null) {
                //The current startDate must be less or equal that current endDate
                stringCurrentValueEndDate = BillingWebDates.timestampToStringShortView(timestampCurrentValueEndDate);
                dateCurrentValueEndDate = BillingWebDates.shortStringToDate(stringCurrentValueEndDate);

                if (dateCurrentValueStartDate.after(dateCurrentValueEndDate)) {
                    error_message_detail = "Error - The Start Date must be less or equal to End Date";
                    faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    faces_message.setSummary(error_message);
                    faces_message.setDetail(error_message_detail);
                    //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    // context.getExternalContext().getFlash().setKeepMessages(true);
                    context.addMessage(component.getClientId(context), faces_message);
                    error_message_detail = error_message_detail + "client id " + component.getClientId(context);
                    LOGGER.error(error_message_detail);
                    //throw new ValidatorException(faces_message);
                    continueValidation = false;
                }
            }

            if ((currentPos != 0) && (continueValidation)) {
                // We must validate the dates of the previous row
                stringPrevValueStartDate = BillingWebDates.timestampToStringShortView(timestampPrevValueStartDate);
                stringPrevValueEndDate = BillingWebDates.timestampToStringShortView(timestampPrevValueEndDate);
                datePrevValueStartDate = BillingWebDates.shortStringToDate(stringPrevValueStartDate);
                datePrevValueEndDate = BillingWebDates.shortStringToDate(stringPrevValueEndDate);

                if (dateCurrentValueStartDate.before(datePrevValueStartDate)
                        || dateCurrentValueStartDate.equals(datePrevValueStartDate)) {
                    // Current startDate less or equal than previous startDate --> ERROR
                    error_message_detail = "Error - The Start Date must be greather than previous Start Date (" + stringPrevValueStartDate + ")";
                    //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    faces_message.setSummary(error_message);
                    faces_message.setDetail(error_message_detail);
                    // context.getExternalContext().getFlash().setKeepMessages(true);
                    context.addMessage(component.getClientId(context), faces_message);
                    error_message_detail = error_message_detail + "client id " + component.getClientId(context);
                    LOGGER.error(error_message_detail);
                    //throw new ValidatorException(faces_message);
                    continueValidation = false;
                }

                if (dateDesiredValuePrevEndDate.after(datePrevValueStartDate) && continueValidation) {
                    if (dateDesiredValuePrevEndDate.equals(datePrevValueEndDate)) {
                        //do nothing
                        error_message_detail = "The Start Date is the previous End Date plus one day. It's not need to change anything";
                        LOGGER.info(error_message_detail);
                    } else {
                        // Set the prevEndDate to the new value
                        // If the prevEndDate is less than the new value --> Reduced the previous interval of dates
                        // If the prevEndDate is greather than the new value --> Extended the previous interval of dates                    

                        result = updateEndDate(className, dataTable, currentPos - 1, BillingWebDates.dateToTimestamp(dateDesiredValuePrevEndDate));
                        if (!result) {
                            continueValidation = false;
                        } else {
                            dataTable.setRowIndex(currentPos - 1);
                            Ajax.updateRow(dataTable, currentPos - 1);
                            error_message_detail = "Modifies the Start Date and the previous End Date (to Start Date value minus one day)";
                            LOGGER.info(error_message_detail);
                        }
                    }
                }
            }

        } catch (EJBException e) {
            Exception ne = (Exception) e.getCause();
            switch (ne.getClass().getName()) {
                case "es.billingweb.exception.BillingWebDataAccessException":
                    //showDialogMessageFatal("DATABASE ERROR - " + ne.getMessage());
                    error_message_detail = "DATABASE ERROR - " + ne.getMessage();
                    //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    faces_message.setSummary(error_message);
                    faces_message.setDetail(error_message_detail);
                    LOGGER.fatal(error_message_detail);
                    //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                    throw new ValidatorException(faces_message);
                case "es.billingweb.exception.BillingWebParseException":
                    //showDialogMessageFatal("DATABASE ERROR - " + ne.getMessage());
                    error_message_detail = "PARSE ERROR - " + ne.getMessage();
                    //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    faces_message.setSummary(error_message);
                    faces_message.setDetail(error_message_detail);
                    LOGGER.fatal(error_message_detail);
                    //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                    throw new ValidatorException(faces_message);
                default:
                    //showDialogMessageFatal("DATABASE ERROR - " + ne.getMessage());
                    error_message_detail = "ERROR - " + ne.getMessage();
                    //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    faces_message.setSummary(error_message);
                    faces_message.setDetail(error_message_detail);
                    LOGGER.fatal(error_message_detail);
                    //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                    throw new ValidatorException(faces_message);
            }
        } catch (Exception e) {
            //showDialogMessageFatal("ERROR - " + e.getCause().toString());
            if (!e.getClass().getName().equals("javax.faces.validator.ValidatorException")) {
                error_message_detail = "ERROR - " + e.getCause().toString();
                //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                faces_message.setSummary(error_message);
                faces_message.setDetail(error_message_detail);
                LOGGER.fatal(error_message_detail);
                //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "ERROR - " + e.getCause().toString());
                throw new ValidatorException(faces_message);
            }

        } finally {
            dataTable.setRowIndex(currentPos);

        }

        if (!continueValidation) {
            if (faces_message.getDetail() == null) {
                error_message_detail = "Valid Start Date";
                LOGGER.info(error_message_detail);
            } else {
                throw new ValidatorException(faces_message);
            }
        }
    }

    public boolean updateEndDate(String className, DataTable dataTable, int pos, Timestamp endDate) {

        boolean result = true;
        FacesMessage faces_message = new FacesMessage();

        dataTable.setRowIndex(pos);

        switch (className) {
            case "es.billingweb.model.tables.pojos.ItUser":
                ((ItUser) dataTable.getRowData()).setEndDate(endDate);
                break;
            case "es.billingweb.model.tables.pojos.MtProductType":
                ((MtProductType) dataTable.getRowData()).setEndDate(endDate);
                break;
            case "es.billingweb.model.tables.pojos.MtServiceType":
                ((MtServiceType) dataTable.getRowData()).setEndDate(endDate);
                break;
            case "es.billingweb.model.tables.pojos.MtEquipmentType":
                ((MtEquipmentType) dataTable.getRowData()).setEndDate(endDate);
                break;
            case "es.billingweb.model.tables.pojos.MtPromotionType":
                ((MtPromotionType) dataTable.getRowData()).setEndDate(endDate);
                break;
            default:
                result = false;
                String error_message = "START DATE ERROR - updating startDate";
                String error_message_detail = "ERROR - The class object " + className + "is not expect";

                faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                faces_message.setSummary(error_message);
                faces_message.setDetail(error_message_detail);
                LOGGER.fatal(error_message_detail);
                //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                throw new ValidatorException(faces_message);
        }
        return result;

    }

}
