/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.converter;

import es.billingweb.exception.BillingWebParseException;
import es.billingweb.utils.BillingWebDates;
import java.sql.Timestamp;
import java.util.Date;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author catuxa
 *
 * Source:
 * http://stackoverflow.com/questions/20565829/how-to-get-selected-date-with-current-timestamp-in-primefaces7
 */
@FacesConverter("timestampShortConverter")
public class TimestampShortConverter implements Converter {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Timestamp timestamp = null;
        String format;

        if (value == null) {
            return null;
        }

        try {
            timestamp = BillingWebDates.shortStringToTimestamp(value);
        } catch (BillingWebParseException e) {
            LOGGER.error("ERROR conversion dates - " + e.getMessage());
        }

        //System.out.println("Component: " + component.getClientId() + " - old value: " + value + " - new value" + timestamp.toString() );
        
        return timestamp;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String string = null;

        if (value == null) {
            return null;
        }

        /*  
        if (value.getClass().getName().equals("java.util.Date")) {
            string = BillingWebDates.dateToStringShortView((Date) value);                    
        } else {
            string = BillingWebDates.timestampToStringShortView((Timestamp) value);                    
        }
         */
        switch (value.getClass().getName()) {
            case "java.util.Date":
                string = BillingWebDates.dateToStringShortView((Date) value);
                break;
            case "java.sql.Timestamp":
                string = BillingWebDates.timestampToStringShortView((Timestamp) value);
                break;
            case "java.lang.String":
                string = (String) value;
                break;
            default:
                LOGGER.error("ERROR conversion dates. The class value " + value.getClass().getName() + " was not expected");
        }

        //System.out.println("Component: " + component.getClientId() + " - old value: " + value.toString() + " - new value" + string );
        
        return string;
    }

}
