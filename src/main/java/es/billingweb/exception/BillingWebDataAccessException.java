/*
 * Copyright 2016 catuxa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.billingweb.exception;


/**
 *
 * @author catuxa
 *
 * source
 * http://www.programcreek.com/java-api-examples/index.php?source_dir=steve-master/src/main/java/de/rwth/idsg/steve/SteveException.java
 */

import static java.lang.String.format; 
 

/**
 *
 * @author catuxa
 * 
 * source http://www.programcreek.com/java-api-examples/index.php?source_dir=steve-master/src/main/java/de/rwth/idsg/steve/SteveException.java
 */
public class BillingWebDataAccessException extends RuntimeException {

    //private static final long serialVersionUID = 3081743035434873349L; 
    private static final long serialVersionUID = 1L; 
    
    public BillingWebDataAccessException(String message) {
        super(message);
    }    
    
        public BillingWebDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }    
    
        
     // ------------------------------------------------------------------------- 
    // No String/variable interpolation in Java. Use format instead. 
    // ------------------------------------------------------------------------- 
 
    public BillingWebDataAccessException(String template, Object arg1) { 
        this(format(template, arg1)); 
    } 
 
    public BillingWebDataAccessException(String template, Object arg1, Throwable cause) { 
        this(format(template, arg1), cause); 
    } 
 
    public BillingWebDataAccessException(String template, Object arg1, Object arg2) { 
        this(format(template, arg1, arg2)); 
    } 
 
    public BillingWebDataAccessException(String template, Object arg1, Object arg2, Throwable cause) { 
        this(format(template, arg1, arg2), cause); 
    } 
}
