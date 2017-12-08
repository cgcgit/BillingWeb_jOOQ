/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.structure;

import es.billingweb.model.tables.pojos.MtStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author catuxa
 */
public class ListedPromotionTypeObject extends ListedEntityObject {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Internal Id for the entity promotion type
    protected final Integer ENTITY_TYPE_PROMOTION_TYPE_ID = Integer.parseInt(OTHERS.getString("TYPE_ENTITY_PROMOTION_TYPE_ID"));
    
    /// Internal Id for the promotion equipment type
    protected final Integer CANCEL_STATUS_PROMOTION_TYPE_ID = Integer.parseInt(OTHERS.getString("CANCEL_STATUS_PROMOTION_TYPE_ID"));

    
    //Min and max values for the minumun value of discounts
    protected Integer valueMinFrom;
    protected Integer valueMinTo;
    
    //Min and max values for the maximun value of discounts
    protected Integer valueMaxFrom;
    protected Integer valueMaxTo;
    

    public Integer getValueMinFrom() {
        return valueMinFrom;
    }

    public void setValueMinFrom(Integer valueMinFrom) {
        this.valueMinFrom = valueMinFrom;
    }

    public Integer getValueMinTo() {
        return valueMinTo;
    }

    public void setValueMinTo(Integer valueMinTo) {
        this.valueMinTo = valueMinTo;
    }

    public Integer getValueMaxFrom() {
        return valueMaxFrom;
    }

    public void setValueMaxFrom(Integer valueMaxFrom) {
        this.valueMaxFrom = valueMaxFrom;
    }

    public Integer getValueMaxTo() {
        return valueMaxTo;
    }

    public void setValueMaxTo(Integer valueMaxTo) {
        this.valueMaxTo = valueMaxTo;
    }
   
   
    
    
    /**
     * Custom filter for the integer value fields into the datatable to the min value
     *
     * @param value
     * @param filter
     * @param locale
     * @return true: the record is between from and to integers - false: otherwise
     */
    public boolean filterByMinValue(Object value, Object filter, Locale locale) {

        String message, message_detail;
        Integer filterValue = null;

        message = "FILTER BY MIN VALUE";

        if (value == null) {
            return true;
        } else {
            try {
                filterValue= (Integer) value;            

            } catch (Exception e) {
                message_detail = "ERROR - " + e.getCause().toString();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            }
        }

        Integer valueFrom = this.valueMinFrom;
        Integer valueTo = this.valueMinTo;

        return (valueFrom == null || (filterValue.compareTo(valueFrom)) == 0 || (filterValue.compareTo(valueFrom)) > 0)                
                && (valueTo == null || (filterValue.compareTo(valueTo)) == 0 || (filterValue.compareTo(valueTo)) < 0);
    }
    
    
    /**
     * Custom filter for the integer value fields into the datatable to the max value
     *
     * @param value
     * @param filter
     * @param locale
     * @return true: the record is between from and to integers - false: otherwise
     */
    public boolean filterByMaxValue(Object value, Object filter, Locale locale) {

        String message, message_detail;
        Integer filterValue = null;

        message = "FILTER BY MIN VALUE";

        if (value == null) {
            return true;
        } else {
            try {
                filterValue= (Integer) value;            

            } catch (Exception e) {
                message_detail = "ERROR - " + e.getCause().toString();
                LOGGER.fatal(message_detail);
                createMessage(FacesMessage.SEVERITY_ERROR, message, message_detail);
            }
        }

        Integer valueFrom = this.valueMaxFrom;
        Integer valueTo = this.valueMaxTo;

        return (valueFrom == null || (filterValue.compareTo(valueFrom)) == 0 || (filterValue.compareTo(valueFrom)) > 0)                
                && (valueTo == null || (filterValue.compareTo(valueTo)) == 0 || (filterValue.compareTo(valueTo)) < 0);
    }
    
    
    /**
     * Return the list to populate the one select menu for the equipment type
     * status
     *
     * @return the list of all the status into the system for the equipment types
     */
    public List<SelectItem> populateStatusMenuForPromotionType() {
        List<SelectItem> statusItem = new ArrayList<>();
        List<MtStatus> list = ejbStatus.findAllStatusForEntity(ENTITY_TYPE_PROMOTION_TYPE_ID);

        if (list.isEmpty()) {
            LOGGER.error("ERROR - Not find status");
        } else {
            for (MtStatus p : list) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getStatusCode() + "-" + p.getDescription());
                item.setValue(p.getStatusId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }
    
}
