/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.utils;

import es.billingweb.exception.BillingWebParseException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * Class that contents methods to formatting and operating dates.
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 */
public class BillingWebDates {

    //public static Timestamp DEFAULT_START_DATE = Timestamp.valueOf(LocalDateTime.of(1900, 1, 1, 0, 0, 0));
    //public static Timestamp DEFAULT_END_DATE = Timestamp.valueOf(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
    public static Timestamp DEFAULT_START_DATE_COMPLETE = Timestamp.valueOf(LocalDateTime.parse("01/01/1900 00:00:00", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    public static Timestamp DEFAULT_END_DATE_COMPLETE = Timestamp.valueOf(LocalDateTime.parse("31/12/9999 23:59:59", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    public static Integer INVALID_ID = 0;

    /*
     * String values for default dates (start and end). 
     * Long and short format
     */
    private static final String DEFAULT_LONG_START_DATE = "01/01/1900 00:00:00";
    private static final String DEFAULT_LONG_END_DATE = "31/12/9999 23:59:59";

    private static final String DEFAULT_SHORT_START_DATE = "01/01/1900";
    private static final String DEFAULT_SHORT_END_DATE = "31/12/9999";
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Madrid");

    private final static ResourceBundle GENERAL_PROPERTIES = ResourceBundle.getBundle("properties.general_config");
    //private static final String JAVA_TIME_ZONE = generalProperties.getString("JAVA_TIMEZONE");

    /**
     * Converts a Date into string short format view (dd/MM/yyyy)
     *
     * @param date date to convert
     * @return the string value of the date in short format view (dd/MM/yyyy)
     */
    public static String dateToStringShortView(Date date) {

        DateFormat df = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW);
        df.setTimeZone(TIMEZONE);

        String string = df.format(date);

        return string;

    }

    /**
     * Converts a Date into string long format view (dd/MM/yyyy HH:mm:ss)
     *
     * @param date date to convert
     * @return the string value of the date in short format view (dd/MM/yyyy
     * HH:mm:ss)
     */
    public static String dateToStringLongView(Date date) {

        DateFormat df = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_LONG_VIEW);
        df.setTimeZone(TIMEZONE);

        String string = df.format(date);

        return string;

    }

    /**
     * Converts a date into a Timestamp
     *
     * @param date to convert
     * @return the timestamp for the date
     */
    public static Timestamp dateToTimestamp(Date date) {

        Timestamp timestamp = Timestamp.from(date.toInstant());

        return timestamp;
    }

    /**
     * Converts a timestam into string short format view (dd/MM/yyyy)
     *
     * @param timestamp to convert
     * @return the string for the timestamp (format dd/MM/yyyy)
     */
    public static String timestampToStringShortView(Timestamp timestamp) {

        String string;
        SimpleDateFormat df;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;

        df = new SimpleDateFormat(format);
        df.setTimeZone(TIMEZONE);
        string = df.format(timestamp);

        return string;

    }

    /**
     * Converts a timestam into string long format view (dd/MM/yyyy HH:mm:ss)
     *
     * @param timestamp to convert
     * @return the string for the timestamp (format dd/MM/yyyy HH:mm:ss)
     */
    public static String timestampToStringLongView(Timestamp timestamp) {

        String string;
        SimpleDateFormat df;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;

        df = new SimpleDateFormat(format);
        df.setTimeZone(TIMEZONE);
        string = df.format(timestamp);

        return string;

    }

    /**
     * Converts a timestam into a short view date (format dd/MM/yyyy)
     *
     * @param timestamp to convert
     * @return the date for the timestamp (format dd/MM/yyyy)
     */
    public static Date timestampToDateShortView(Timestamp timestamp) throws BillingWebParseException {
        Date date = null;
        String stringDate;
        SimpleDateFormat df;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;

        try {
            df = new SimpleDateFormat(format);
            df.setTimeZone(TIMEZONE);

            date = Date.from(timestamp.toInstant());
            stringDate = dateToStringShortView(date);

            date = df.parse(stringDate);
        } catch (ParseException e) {
            throw new BillingWebParseException("Error converting tmestamp " + timestamp.toString() + " into short date" + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts a timestam into a long view date (format dd/MM/yyyy HH:mm:ss)
     *
     * @param timestamp to convert
     * @return the date for the timestamp (format dd/MM/yyyy HH:mm:ss)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date timestampToDateLongView(Timestamp timestamp) throws BillingWebParseException {
        Date date = null;
        String stringDate;
        SimpleDateFormat df;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;

        try {
            df = new SimpleDateFormat(format);
            df.setTimeZone(TIMEZONE);

            date = Date.from(timestamp.toInstant());
            stringDate = dateToStringShortView(date);
            date = df.parse(stringDate);
        } catch (ParseException e) {
            throw new BillingWebParseException("Error converting tmestamp " + timestamp.toString() + " into long date" + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts short string (short format dd/MM/yyyy) into short view date
     * (same format)
     *
     * @param string in short format (dd/MM/yyyy)
     * @return date in short format (dd/MM/yyyy)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date shortStringToDate(String string) throws BillingWebParseException {
        SimpleDateFormat df;
        Date date = null;

        try {
            df = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW);
            df.setTimeZone(TIMEZONE);
            date = df.parse(string);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting string " + string + " into short date " + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts long string (short format dd/MM/yyyy HH:mm:ss) into long view
     * date (same format)
     *
     * @param string in long format (dd/MM/yyyy HH:mm:ss)
     * @return date in long format (dd/MM/yyyy HH:mm:ss)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date longStringToDate(String string) throws BillingWebParseException {
        SimpleDateFormat df;
        Date date = null;

        try {
            df = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_LONG_VIEW);
            df.setTimeZone(TIMEZONE);
            date = df.parse(string);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting string " + string + " into long date " + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts short string (short format dd/MM/yyyy) into timestamp
     *
     * @param string in short format (dd/MM/yyyy)
     * @return timestamp from string
     * @throws BillingWebParseException
     */
    public static Timestamp shortStringToTimestamp(String string) throws BillingWebParseException {
        SimpleDateFormat df;
        Timestamp timestamp = null;

        try {

            df = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW);
            df.setTimeZone(TIMEZONE);
            // you can change format of date
            Date date = df.parse(string);
            timestamp = new Timestamp(date.getTime());
        } catch (ParseException e) {
            throw new BillingWebParseException("Error converting short string " + string + " into timestamp " + e.getCause().toString(), e);

        }
        return timestamp;
    }

    /**
     * Converts short string (short format dd/MM/yyyy) into timestamp
     *
     * @param string in long format (dd/MM/yyyy HH:mm:ss)
     * @return timestamp from string
     * @throws BillingWebParseException
     */
    public static Timestamp longStringToTimestamp(String string) throws BillingWebParseException {
        SimpleDateFormat df;

        Timestamp timestamp = null;
        try {

            df = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_LONG_VIEW);
            df.setTimeZone(TIMEZONE);
            // you can change format of date
            Date date = df.parse(string);
            timestamp = new Timestamp(date.getTime());
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting short string " + string + " into timestamp " + e.getCause().toString(), e);

        }
        return timestamp;
    }

    /**
     * ***************************************
     */
    /**
     * Gets the default short start date (Date type) - format dd/MM/yyyy
     *
     * @return the default start date Date (01/01/1900)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date getDateDefaultStartDateShortView() throws BillingWebParseException {
        Date date = null;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;
        String dateValue = DEFAULT_SHORT_START_DATE;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error obtaining default short (Date) start date " + e.getCause().toString(), e);
        }
        return date;

    }

    /**
     * Gets the default long start date (Date type) - format dd/MM/yyyy HH:mm:ss
     *
     * @return the default start date Date (01/01/1900 00:00:00)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date getDateDefaultStartDateLongView() throws BillingWebParseException {
        Date date = null;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;
        String dateValue = DEFAULT_LONG_START_DATE;

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error obtaining long (Date) start date " + e.getCause().toString(), e);
        }
        return date;

    }
    
    /**
     * Gets the default start date (Timestamp type) - format dd/MM/yyyy
     *
     * @return the default start date Timestamp (01/01/1900)     
     * @throws es.billingweb.exception.BillingWebParseException     
     */
    public static Timestamp getTimestampDefaultStartDate() throws BillingWebParseException {
        
        Timestamp timestamp;
               
        timestamp = longStringToTimestamp(DEFAULT_LONG_START_DATE);
       
        return timestamp;
    }

    /**
     * Gets the default long start date (String type) - format dd/MM/yyyy HH:mm:ss
     *
     * @return the default start date String (01/01/1900 00:00:00)
     */
    public static String getStringDefaultStartDateShortView() {

        return DEFAULT_SHORT_START_DATE;

    }
    
     /**
     * Gets the default long end date (String type) - format dd/MM/yyyy HH:mm:ss
     *
     * @return the default start date String (01/01/1900 00:00:00)
     */
    public static String getStringDefaultStartDateLongView() {

        return DEFAULT_LONG_START_DATE;

    }
    

    /**
     * Gets the default short end date (Date type) - format dd/MM/yyyy
     *
     * @return the default end date Date (31/12/9999)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date getDateDefaultEndDateShortView() throws BillingWebParseException {

        Date date = null;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;
        String dateValue = DEFAULT_SHORT_END_DATE;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error obtaining default short (Date) end date " + e.getCause().toString(), e);
        }
        return date;

    }

    /**
     * Gets the default long end date (Date type) - format dd/MM/yyyy HH:mm:ss
     *
     * @return the default end date Date (31/12/9999 23:59:59)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date getDateDefaultEndDateLongView() throws BillingWebParseException {

        Date date = null;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;
        String dateValue = DEFAULT_LONG_END_DATE;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {
            throw new BillingWebParseException("Error obtaining default long (Date) end date " + e.getCause().toString(), e);
        }
        return date;
    }
    
    /**
     * Gets the default end date (Timestamp type) - format dd/MM/yyyy
     *
     * @return the default end date Timestamp (31/12/9999)     
     * @throws es.billingweb.exception.BillingWebParseException     
     */
    public static Timestamp getTimestampDefaulEndDate() throws BillingWebParseException {
        
        Timestamp timestamp;
               
        timestamp = longStringToTimestamp(DEFAULT_LONG_END_DATE);
       
        return timestamp;
    }

    
    
    /**
     * Gets the default short end date (String type) - format dd/MM/yyyy
     *
     * @return the default end date String (31/12/9999)     
     */
    public static String getStringDefaultEndDateShortView() {

        return DEFAULT_SHORT_END_DATE;
       
    }

    /**
     * Gets the default long end date (String type) - format dd/MM/yyyy HH:mm:ss
     *
     * @return the default end date String (31/12/9999 23:59:59)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static String getStringDefaultEndDateLongView() throws BillingWebParseException {

        return DEFAULT_LONG_END_DATE;

    }
    
    

    /**
     * Returns the short format (dd/MM/yyyy) from a date
     *
     * @param date
     * @return date in short format (dd/MM/yyyy)
     * @throws BillingWebParseException
     */
    public static Date dateToShortViewDate(Date date) throws BillingWebParseException {

        Date shortDate = null;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;

        try {
            String stringDate = dateToStringShortView(date);

            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);

            shortDate = dateFormat.parse(stringDate);

        } catch (ParseException e) {
            throw new BillingWebParseException("Error converting long date " + date.toString() + " into long date " + e.getCause().toString(), e);
        }

        return shortDate;
    }

    /**
     * Returns the long format (dd/MM/yyyy HH:mm:ss) from a date
     *
     * @param date
     * @return date in long format (dd/MM/yyyy HH:mm:ss)
     * @throws BillingWebParseException
     */
    public static Date dateToLongViewDate(Date date) throws BillingWebParseException {

        Date longDate = null;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;

        try {
            String stringDate = dateToStringLongView(date);

            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);

            longDate = dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new BillingWebParseException("Error converting short date " + date.toString() + " into long date " + e.getCause().toString(), e);
        }

        return longDate;
    }

    /**
     * Gets the short current date (Date type) - format dd/MM/yyyy
     *
     * @return the current date Date
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date getCurrentDateShortView() throws BillingWebParseException {
        Date date = null;
        Date shortDate = null;
        try {
            date = new java.util.Date();
            shortDate = dateToShortViewDate(date);
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }

        return shortDate;
    }

    /**
     * Gets the long current date (Date type) - format dd/MM/yyyy HH:mi:ss
     *
     * @return the current date Date
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date getCurrentDateLongView() throws BillingWebParseException {
        Date date = null;
        Date longDate = null;
        try {
            date = new java.util.Date();
            longDate = dateToLongViewDate(date);
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get long current date " + e.getCause().toString(), e);
        }

        return longDate;
    }

    /**
     * Gets the current timestamp date
     *
     * @return the current timestamp date
     */
    public static Timestamp getCurrentTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp;
    }

    
    /**
     * Gets the current date in string short format view (dd/MM/yyyy)
     *
     * @return the string value of the date in short format view (dd/MM/yyyy)     
     * @throws BillingWebParseException 
     */
    public static String getCurrentStringDateShortView() throws BillingWebParseException {
        Date date;
        String stringDate;
        try {
            date = getCurrentDateShortView();
            stringDate = dateToStringShortView(date);
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get long current date " + e.getCause().toString(), e);
        }

        return stringDate;
    }
    
    /**
     * Gets the current date in string long format view (dd/MM/yyyy HH:mm:ss)
     *
     * @return the string value of the date in long format view (dd/MM/yyyy HH:mm:ss)     
     * @throws BillingWebParseException 
     */
    public static String getCurrentStringDateLongView() throws BillingWebParseException {
        Date date;
        String stringDate;
        try {
            date = getCurrentDateLongView();
            stringDate = dateToStringLongView(date);
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get long current date " + e.getCause().toString(), e);
        }

        return stringDate;
    }

    /**
     * Calculates a new date (format dd/MM/yyyy) by adding a number of days to a
     * date
     *
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date addDaysToDateShortView(Date date, int days) throws BillingWebParseException {

        Date returnDate = null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        try {
            returnDate = BillingWebDates.dateToShortViewDate(c.getTime());
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }

    /**
     * Calculates a new date (format dd/MM/yyyy HH:mi:ss) by adding a number of
     * days to a date
     *
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy HH:mi:ss
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date addDaysToDateLongView(Date date, int days) throws BillingWebParseException {

        Date returnDate = null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        try {
            returnDate = BillingWebDates.dateToLongViewDate(c.getTime());
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }

    /**
     * Calculate a new date (format dd/MM/yyyy) by substracting a number of days
     * to a date
     *
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date substractDaysToDateShortView(Date date, int days) throws BillingWebParseException {

        Date returnDate = null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, -days);
        try {
            returnDate = BillingWebDates.dateToShortViewDate(c.getTime());
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }

    /**
     * Calculate a new date (format dd/MM/yyyy HH:mi:ss) by substracting a
     * number of days to a date
     *
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy HH:mi:ss
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date substractDaysToDateLongView(Date date, int days) throws BillingWebParseException {

        Date returnDate = null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, -days);
        try {
            returnDate = BillingWebDates.dateToLongViewDate(c.getTime());
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }

}
