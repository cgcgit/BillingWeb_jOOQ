
package es.billingweb.ejb;

import es.billingweb.model.tables.pojos.VUserProfile;
import javax.ejb.Local;


/**
 *
 * Interface bean for the application's user
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */

@Local
public interface BillingWebUserLocal {
    
     /**
     * Login validation for user
     * @param userLogin
     * @return userLogin data. If user does not exists, return null.
     */
    VUserProfile login(VUserProfile userLogin);
    //boolean validateUser(String userCode, String password);
    //VUserProfile login(String userCode, String password);
   
    
}
