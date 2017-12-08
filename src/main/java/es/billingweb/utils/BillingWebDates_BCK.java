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
public class BillingWebDates_BCK {

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
    private static final TimeZone TIMEZONE=TimeZone.getTimeZone("CEST"); 

    /*
     * Date format for dates.
     * Format to view and to store, in short and long format
     */
 /*
    private static final String DATE_FORMAT_LONG_VIEW="dd/MM/yyyy HH:mm:ss";
    private static final String DATE_FORMAT_LONG_STORE="yyyy/MM/dd HH:mm:ss";
    private static final String DATE_FORMAT_SHORT_VIEW="dd/MM/yyyy";
    private static final String DATE_FORMAT_SHORT_STORE="yyyy/MM/dd";
     */
    private final static ResourceBundle GENERAL_PROPERTIES = ResourceBundle.getBundle("properties.general_config");
    //rivate static final String JAVA_TIME_ZONE = generalProperties.getString("JAVA_TIMEZONE");

    /**
     * Gets the default long start date (Date type) - format dd/MM/yyyy HH:mm:ss
     *
     * @return the default start date Date (01/01/1900 00:00:00)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static  Date getLongDefaultStartDate() throws BillingWebParseException {
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
     * Gets the default long end date (Date type) - format dd/MM/yyyy HH:mm:ss
     *
     * @return the default end date Date (31/12/9999 23:59:59)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static  Date getLongDefaultEndDate() throws BillingWebParseException {

        Date date = null;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;
        String dateValue = DEFAULT_LONG_END_DATE;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);
            date = dateFormat.parse(dateValue);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error obtaining default long (Date) end date " + e.getCause().toString(), e);
        }
        return date;

    }

    /**
     * Gets the default short start date (Date type) - format dd/MM/yyyy
     *
     * @return the default start date Date (01/01/1900)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date getShortDefaultStartDate() throws BillingWebParseException {
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
     * Gets the default complete end date (Date type) - format dd/MM/yyyy
     *
     * @return the default end date Date (31/12/9999)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static  Date getShortDefaultEndDate() throws BillingWebParseException {

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
     * Returns the long format (dd/MM/yyyy HH:mm:ss) from a date
     * @param date 
     * @return date in long format (dd/MM/yyyy HH:mm:ss)
     * @throws BillingWebParseException 
     */
    public static Date dateToLongDate(Date date) throws BillingWebParseException {
        
        Date longDate = null;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;

        try {
            String stringDate = longDateToString(date);

            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);

            longDate = dateFormat.parse(stringDate);
        } catch (ParseException  e) {
            throw new BillingWebParseException("Error converting short date " + date.toString() + " into long date " + e.getCause().toString(), e);
        }
        //this generic but you can control another types of exception
        return longDate;
    }
    
    
    /**
     * Returns the short format (dd/MM/yyyy) from a date
     * @param date 
     * @return date in short format (dd/MM/yyyy)
     * @throws BillingWebParseException 
     */
    public static Date dateToShortDate(Date date) throws BillingWebParseException {
        
        Date shortDate = null;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;

        try {
            String stringDate = shortDateToString(date);

            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);

            shortDate = dateFormat.parse(stringDate);
            
        } catch (ParseException  e) {
//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting long date " + date.toString() + " into long date " + e.getCause().toString(), e);
        }
        //this generic but you can control another types of exception
        return shortDate;
    }

    /**
     * Converts date into string (short format dd/MM/yyyy)
     *
     * @param date
     * @return string (short format (dd/MM/yyyy)
     */
    public static String shortDateToString(Date date) {
        SimpleDateFormat dateFormat;
        String stringDate = null;

        dateFormat = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW);
        dateFormat.setTimeZone(TIMEZONE);
        stringDate = dateFormat.format(date);
        return stringDate;
    }

    /**
     * Converts date into string (long format dd/MM/yyyy HH:mm:ss)
     *
     * @param date
     * @return string (format dd/MM/yyyy HH:mm:ss)
     */
    public static String longDateToString(Date date) {
        SimpleDateFormat dateFormat;
        String stringDate = null;

        dateFormat = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_LONG_VIEW);
        dateFormat.setTimeZone(TIMEZONE);
        stringDate = dateFormat.format(date);
        return stringDate;
    }

    /**
     * Converts string into short date (short format dd/MM/yyyy)
     *
     * @param string
     * @return date in short format (dd/MM/yyyy)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static  Date stringToShortDate(String string) throws BillingWebParseException {
        SimpleDateFormat dateFormat;
        Date date = null;

        try {
            dateFormat = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW);
            dateFormat.setTimeZone(TIMEZONE);
            date = dateFormat.parse(string);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting string " + string + " into short date " + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts string into long date (long format dd/MM/yyyy HH:mm:ss)
     *
     * @param string
     * @return date in long format (dd/MM/yyyy HH:mm:ss)
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Date stringToLongDate(String string) throws BillingWebParseException {
        SimpleDateFormat dateFormat;
        Date date = null;

        try {
            dateFormat = new SimpleDateFormat(BillingWebDateFormat.DATE_FORMAT_LONG_VIEW);
            dateFormat.setTimeZone(TIMEZONE);
            date = dateFormat.parse(string);
        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting string " + string + " into long date " + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts string (short format dd/MM/yyyy) to Timestamp
     *
     * @param string to convert (format dd/MM/yyyy)
     * @return timestamp from the string
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Timestamp shortStringToTimestamp(String string) throws BillingWebParseException {
        Timestamp timestamp = null;
        SimpleDateFormat dateFormat = null;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;
        Date date = null;

        try {

            dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);

            date = dateFormat.parse(string);

            timestamp = new java.sql.Timestamp(date.getTime());

        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting short string " + string + " into timestamp type" + e.getCause().toString(), e);
        }

        return timestamp;
    }

    /**
     * Converts string (long format dd/MM/yyyy HH:mm:ss) to Timestamp
     *
     * @param string to convert (format dd/MM/yyyy HH:mm:ss)
     * @return timestamp from the string
     * @throws es.billingweb.exception.BillingWebParseException
     */
    public static Timestamp longStringToTimestamp(String string) throws BillingWebParseException {
        Timestamp timestamp = null;
        SimpleDateFormat dateFormat = null;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;
        Date date = null;

        try {

            dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TIMEZONE);
            
            date = dateFormat.parse(string);

            timestamp = new java.sql.Timestamp(date.getTime());

        } catch (ParseException e) {//this generic but you can control another types of exception
            throw new BillingWebParseException("Error converting short string " + string + " into timestamp type" + e.getCause().toString(), e);
        }

        return timestamp;
    }

    /**
     * Converts a timestam into a short string (format dd/MM/yyyy)
     *
     * @param timestamp to convert
     * @return the string for the timestamp (format dd/MM/yyyy)
     */
    public static String timestampToShortString(Timestamp timestamp) {

        String string = null;
        SimpleDateFormat dateFormat;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;

        dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TIMEZONE);
        string = dateFormat.format(timestamp);

        return string;

    }

    /**
     * Converts a timestam into a long string (format dd/MM/yyyy HH:mm:ss)
     *
     * @param timestamp to convert
     * @return the string for the timestamp (format dd/MM/yyyy HH:mm:ss)
     */
    public static String timestampToLongString(Timestamp timestamp) {

        String string = null;
        SimpleDateFormat dateFormat;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;

        dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TIMEZONE);
        string = dateFormat.format(timestamp);

        return string;

    }

    /**
     * Converts a timestam into a short date (format dd/MM/yyyy)
     *
     * @param timestamp to convert
     * @return the date for the timestamp (format dd/MM/yyyy)
     */
    public static Date timestampToShortDate(Timestamp timestamp) throws BillingWebParseException {
        Date date = null;
        String stringDate;
        SimpleDateFormat dateFormat = null;
        String format = BillingWebDateFormat.DATE_FORMAT_SHORT_VIEW;

        date = Date.from(timestamp.toInstant());
        stringDate = longDateToString(date);
        dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TIMEZONE);
        try {
            date = dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new BillingWebParseException("Error converting tmestamp " + timestamp.toString() + " into short date" + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts a timestam into a long date (format dd/MM/yyyy HH:mm:ss)
     *
     * @param timestamp to convert
     * @return the date for the timestamp (format dd/MM/yyyy HH:mm:ss)
     */
    public static Date timestampToLongDate(Timestamp timestamp) throws BillingWebParseException {
        Date date = null;
        String stringDate;
        SimpleDateFormat dateFormat = null;
        String format = BillingWebDateFormat.DATE_FORMAT_LONG_VIEW;

        date = Date.from(timestamp.toInstant());
        stringDate = longDateToString(date);
        dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TIMEZONE);
        try {
            date = dateFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new BillingWebParseException("Error converting tmestamp " + timestamp.toString() + " into long date" + e.getCause().toString(), e);
        }

        return date;
    }

    /**
     * Converts a date into a Timestamp
     *
     * @param date to convert
     * @return the timestamp for the date
     */
    public static Timestamp dateToTimestamp(Date date) {
        Timestamp timestamp = null;

        timestamp = Timestamp.from(date.toInstant());

        return timestamp;
    }

    /**
     * Gets the long current date (Date type) - format dd/MM/yyyy HH:mi:ss
     *
     * @return the current date Date
     */
    public static Date getLongCurrentDate() throws BillingWebParseException {

        Date date = null;
        Date longDate=null;
        try{
        date = new java.util.Date();
        longDate = dateToLongDate(date);
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get long current date " + e.getCause().toString(), e);
        }
        
        return longDate;

    }
    
     /**
     * Gets the short current date (Date type) - format dd/MM/yyyy
     *
     * @return the current date Date
     */
    public static Date getShortCurrentDate() throws BillingWebParseException {

        Date date = null;
        Date shortDate=null;
        try{
        date = new java.util.Date();
        shortDate = dateToShortDate(date);
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        
        return shortDate;

    }
    
    /**
     * Calculates a new date (format dd/MM/yyyy HH:mi:ss)  by adding a number of days to a date
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy HH:mi:ss
     */
    public static Date addDaysToDate_Long (Date date, int days)throws BillingWebParseException {

        Date returnDate=null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        try{
        returnDate=BillingWebDates_BCK.dateToLongDate(c.getTime()); 
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }

     /**
     * Calculates a new date (format dd/MM/yyyy)  by adding a number of days to a date
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy
     */
    public static Date addDaysToDate_Short (Date date, int days)throws BillingWebParseException {

        Date returnDate=null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        try{
        returnDate=BillingWebDates_BCK.dateToShortDate(c.getTime()); 
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }
    
    /**
     * Calculate a new date (format dd/MM/yyyy HH:mi:ss)  by substracting a number of days to a date
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy HH:mi:ss
     */
    public static Date substractDaysToDate_Long (Date date, int days)throws BillingWebParseException {

        Date returnDate=null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, -days);
        try{
        returnDate=BillingWebDates_BCK.dateToLongDate(c.getTime()); 
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }

     /**
     * Calculate a new date (format dd/MM/yyyy)  by substracting a number of days to a date
     * @param date date to add days
     * @param days number of days to add
     * @return new date (Date type) - format dd/MM/yyyy
     */
    public static Date substractDaysToDate_Short (Date date, int days)throws BillingWebParseException {

        Date returnDate=null;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, -days);
        try{
        returnDate=BillingWebDates_BCK.dateToShortDate(c.getTime()); 
        } catch (BillingWebParseException e) {
            throw new BillingWebParseException("Error get short current date " + e.getCause().toString(), e);
        }
        return returnDate;
    }
    
  }
