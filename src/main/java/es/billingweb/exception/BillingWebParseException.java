/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.exception;

import static java.lang.String.format;

/**
 *
 * @author catuxa
 */
public class BillingWebParseException extends Exception {
    
    //private static final long serialVersionUID = 3081743035434873349L; 
    private static final long serialVersionUID = 1L; 
    
    public BillingWebParseException(String message) {
        super(message);
    }    
    
        public BillingWebParseException(String message, Throwable cause) {
        super(message, cause);
    }    
    
        
     // ------------------------------------------------------------------------- 
    // No String/variable interpolation in Java. Use format instead. 
    // ------------------------------------------------------------------------- 
 
    public BillingWebParseException(String template, Object arg1) { 
        this(format(template, arg1)); 
    } 
 
    public BillingWebParseException(String template, Object arg1, Throwable cause) { 
        this(format(template, arg1), cause); 
    } 
 
    public BillingWebParseException(String template, Object arg1, Object arg2) { 
        this(format(template, arg1, arg2)); 
    } 
 
    public BillingWebParseException(String template, Object arg1, Object arg2, Throwable cause) { 
        this(format(template, arg1, arg2), cause); 
    } 
    
}
