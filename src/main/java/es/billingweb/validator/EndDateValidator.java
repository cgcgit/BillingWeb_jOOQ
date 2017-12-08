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
@FacesValidator("endDateValidator")
public class EndDateValidator implements Validator {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String error_message, error_message_detail;
        Timestamp timestampCurrentValueStartDate, timestampCurrentValueEndDate,
                timestampNextValueStartDate, timestampNextValueEndDate;
        String stringPostValueStartDate, stringPostValueEndDate;
        Date datePostValueStartDate, datePostValueEndDate;
        String stringCurrentValueStartDate, stringCurrentValueEndDate;
        Date dateCurrentValueStartDate, dateCurrentValueEndDate, dateDesiredValuePostStartDate;
        int currentPos, nextPos, lastPos;
        FacesMessage faces_message = new FacesMessage();
        String className;
        boolean result = true;
        boolean continueValidation = true;

        error_message = "END DATE ERROR";

        // The value can't be null
        if (value == null) {
            error_message_detail = "Error - The End Date can't be null";
            //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
            faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
            faces_message.setSummary(error_message);
            faces_message.setDetail(error_message_detail);
            LOGGER.error(error_message_detail);
            throw new ValidatorException(faces_message);
        }

        // Gets the table
        DataTable dataTable = (DataTable) Components.getClosestParent(component, UIData.class);
        currentPos = dataTable.getRowIndex();
        Object data = dataTable.getRowData();
        className = data.getClass().getName();
        lastPos = dataTable.getRowCount() - 1;

        try {

            switch (className) {
                case "es.billingweb.model.tables.pojos.ItUser":
                    timestampCurrentValueStartDate = ((ItUser) dataTable.getRowData()).getStartDate();
                    timestampCurrentValueEndDate = (Timestamp) value;

                    if (currentPos != lastPos) {
                        nextPos = currentPos + 1;
                        dataTable.setRowIndex(nextPos);
                        timestampNextValueStartDate = ((ItUser) dataTable.getRowData()).getStartDate();
                        timestampNextValueEndDate = ((ItUser) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampNextValueStartDate = null;
                        timestampNextValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtProductType":
                    timestampCurrentValueStartDate = ((MtProductType) dataTable.getRowData()).getStartDate();
                    timestampCurrentValueEndDate = (Timestamp) value;

                    if (currentPos != lastPos) {
                        nextPos = currentPos + 1;
                        dataTable.setRowIndex(nextPos);
                        timestampNextValueStartDate = ((MtProductType) dataTable.getRowData()).getStartDate();
                        timestampNextValueEndDate = ((MtProductType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampNextValueStartDate = null;
                        timestampNextValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtServiceType":
                    timestampCurrentValueStartDate = ((MtServiceType) dataTable.getRowData()).getStartDate();
                    timestampCurrentValueEndDate = (Timestamp) value;

                    if (currentPos != lastPos) {
                        nextPos = currentPos + 1;
                        dataTable.setRowIndex(nextPos);
                        timestampNextValueStartDate = ((MtServiceType) dataTable.getRowData()).getStartDate();
                        timestampNextValueEndDate = ((MtServiceType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampNextValueStartDate = null;
                        timestampNextValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtEquipmentType":
                    timestampCurrentValueStartDate = ((MtEquipmentType) dataTable.getRowData()).getStartDate();
                    timestampCurrentValueEndDate = (Timestamp) value;

                    if (currentPos != lastPos) {
                        nextPos = currentPos + 1;
                        dataTable.setRowIndex(nextPos);
                        timestampNextValueStartDate = ((MtEquipmentType) dataTable.getRowData()).getStartDate();
                        timestampNextValueEndDate = ((MtEquipmentType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampNextValueStartDate = null;
                        timestampNextValueEndDate = null;
                    }
                    break;
                case "es.billingweb.model.tables.pojos.MtPromotionType":
                    timestampCurrentValueStartDate = ((MtPromotionType) dataTable.getRowData()).getStartDate();
                    timestampCurrentValueEndDate = (Timestamp) value;

                    if (currentPos != lastPos) {
                        nextPos = currentPos + 1;
                        dataTable.setRowIndex(nextPos);
                        timestampNextValueStartDate = ((MtPromotionType) dataTable.getRowData()).getStartDate();
                        timestampNextValueEndDate = ((MtPromotionType) dataTable.getRowData()).getEndDate();
                    } else {
                        timestampNextValueStartDate = null;
                        timestampNextValueEndDate = null;
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

            stringCurrentValueEndDate = BillingWebDates.timestampToStringShortView(timestampCurrentValueEndDate);
            dateCurrentValueEndDate = BillingWebDates.shortStringToDate(stringCurrentValueEndDate);
            dateDesiredValuePostStartDate = BillingWebDates.addDaysToDateShortView(dateCurrentValueEndDate, 1);

            if (timestampCurrentValueStartDate == null) {
                // The startDate can't be null
                error_message_detail = "Error - The Start Date can't be null. Please fill the current Start Date field.";
                //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                faces_message.setSummary(error_message);
                faces_message.setDetail(error_message_detail);
                context.addMessage(component.getClientId(context), faces_message);
                LOGGER.error(error_message_detail);
                //throw new ValidatorException(faces_message);
                continueValidation = false;
            } else {
                //The current startDate must be less or equal that current endDate
                stringCurrentValueStartDate = BillingWebDates.timestampToStringShortView(timestampCurrentValueStartDate);
                dateCurrentValueStartDate = BillingWebDates.shortStringToDate(stringCurrentValueStartDate);

                if (dateCurrentValueStartDate.after(dateCurrentValueEndDate)) {
                    error_message_detail = "Error - The End Date must be greather or equal to Start Date";
                    //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);

                    faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    faces_message.setSummary(error_message);
                    faces_message.setDetail(error_message_detail);
                    // context.getExternalContext().getFlash().setKeepMessages(true);
                    context.addMessage(component.getClientId(context), faces_message);
                    LOGGER.error(error_message_detail);
                    //throw new ValidatorException(faces_message);
                    continueValidation = false;
                }
            }

            if ((continueValidation) && (currentPos == lastPos) && (!dateCurrentValueEndDate.equals(BillingWebDates.getDateDefaultEndDateShortView()))) {
                // If the current row is the last row the endDate must be 31/12/9999
                error_message_detail = "Error - The End Date of the last row must be " + BillingWebDates.getStringDefaultEndDateShortView();
                //faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);

                faces_message.setSeverity(FacesMessage.SEVERITY_ERROR);
                faces_message.setSummary(error_message);
                faces_message.setDetail(error_message_detail);
                context.addMessage(component.getClientId(context), faces_message);
                LOGGER.error(error_message_detail);
                //throw new ValidatorException(faces_message);
                continueValidation = false;
            }

            if ((continueValidation) && (currentPos != lastPos)) {
                // We must validate the dates of the next row

                stringPostValueStartDate = BillingWebDates.timestampToStringShortView(timestampNextValueStartDate);
                stringPostValueEndDate = BillingWebDates.timestampToStringShortView(timestampNextValueEndDate);
                datePostValueStartDate = BillingWebDates.shortStringToDate(stringPostValueStartDate);
                datePostValueEndDate = BillingWebDates.shortStringToDate(stringPostValueEndDate);

                if (dateCurrentValueEndDate.after(datePostValueEndDate)
                        || dateCurrentValueEndDate.equals(datePostValueEndDate)) {
                    // Current endDate greather or equal than next endDate --> ERROR
                    error_message_detail = "Error - The End Date must be less than next Start Date (" + stringPostValueEndDate + ")";
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

                if ((continueValidation) && (dateDesiredValuePostStartDate.before(datePostValueEndDate))) {
                    if (dateDesiredValuePostStartDate.equals(datePostValueStartDate)) {
                        //do nothing
                        error_message_detail = "The End Date is the next Start Date minus one day. It's not need to change anything";
                        LOGGER.info(error_message_detail);
                    } else {
                        // Set the nextStartDate to the new value
                        // If the nextStartDate is less than the new value --> Extended the previous interval of dates
                        // If the nextStartDate is greather than the new value --> Reduced the previous interval of dates                    
                        result = updateStartDate(className, dataTable, currentPos + 1, BillingWebDates.dateToTimestamp(dateDesiredValuePostStartDate));
                        if (!result) {
                            continueValidation = false;
                        } else {
                            dataTable.setRowIndex(currentPos + 1);
                            Ajax.updateRow(dataTable, currentPos + 1);
                            error_message_detail = "Modifies the End Date and the next Start Date (to End Date value plus one day)";
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
                    faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    LOGGER.fatal(error_message_detail);
                    //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                    throw new ValidatorException(faces_message);
                case "es.billingweb.exception.BillingWebParseException":
                    //showDialogMessageFatal("DATABASE ERROR - " + ne.getMessage());
                    error_message_detail = "PARSE ERROR - " + ne.getMessage();
                    faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    LOGGER.fatal(error_message_detail);
                    //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                    throw new ValidatorException(faces_message);
                default:
                    //showDialogMessageFatal("DATABASE ERROR - " + ne.getMessage());
                    error_message_detail = "ERROR - " + ne.getMessage();
                    faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
                    LOGGER.fatal(error_message_detail);
                    //BillingWebMessages.billingWebDialogMessageFatal(CURRENT_REQUEST_CONTEXT, "DATABASE ERROR - " + ne.getMessage());
                    throw new ValidatorException(faces_message);
            }
        } catch (Exception e) {
            //showDialogMessageFatal("ERROR - " + e.getCause().toString());
            if (!e.getClass().getName().equals("javax.faces.validator.ValidatorException")) {
                error_message_detail = "ERROR - " + e.getCause().toString();
                faces_message = new FacesMessage(FacesMessage.SEVERITY_ERROR, error_message, error_message_detail);
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

    public boolean updateStartDate(String className, DataTable dataTable, int pos, Timestamp startDate) {

        boolean result = true;
        FacesMessage faces_message = new FacesMessage();

        dataTable.setRowIndex(pos);

        switch (className) {
            case "es.billingweb.model.tables.pojos.ItUser":
                ((ItUser) dataTable.getRowData()).setStartDate(startDate);
                break;
            case "es.billingweb.model.tables.pojos.MtProductType":
                ((MtProductType) dataTable.getRowData()).setStartDate(startDate);
                break;
            case "es.billingweb.model.tables.pojos.MtServiceType":
                ((MtServiceType) dataTable.getRowData()).setStartDate(startDate);
                break;
            case "es.billingweb.model.tables.pojos.MtEquipmentType":
                ((MtEquipmentType) dataTable.getRowData()).setStartDate(startDate);
                break;
            case "es.billingweb.model.tables.pojos.MtPromotionType":
                ((MtPromotionType) dataTable.getRowData()).setStartDate(startDate);
                break;
            default:
                result = false;
                String error_message = "START DATE ERROR - updating endDate";
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
