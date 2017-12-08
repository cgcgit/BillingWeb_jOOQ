/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejb;

import static es.billingweb.model.tables.VUserProfile.V_USER_PROFILE;
import es.billingweb.model.tables.pojos.VUserProfile;
import es.billingweb.model.tables.records.VUserProfileRecord;
import static es.billingweb.utils.BillingWebUtilities.*;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 * Session bean to manage the validation and authentication for a user in the system. 
 * It uses the configured {@link DataSource} to interact with the embedded POSTGRESQL
 * database.
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */

@Stateless
public class BillingWebUser implements BillingWebUserLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();
    
    
    /**
     * Validates the existence of the user in the database. 
     * The password will be codify with the MD5 algorithm before
     * validates the user.
     *
     * @param userLogin validate user
     * @return userLogin data (null if the user doesn't exists)
     */
    @Override
    public VUserProfile login(VUserProfile user) {
        
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        //Gets the password encrypted
        String pwdMD5 = getMD5(user.getPassword());
        VUserProfile userLogged = null;

        try {
            // Gets the user from the database
            Result<VUserProfileRecord> result = create.selectFrom(V_USER_PROFILE)
                    .where(V_USER_PROFILE.USER_CODE.equal(user.getUserCode())
                    .and(V_USER_PROFILE.PASSWORD.equal(pwdMD5))).fetch();

            if (result.isNotEmpty()) {
                userLogged = result.get(0).into(VUserProfile.class);
                LOGGER.info("User validation OK - the user " + userLogged.getUserCode() + " is registered into the system");
            }
            else {
                LOGGER.error("User validation KO - the user " + user.getUserCode() + " is not registered into the system");
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage() );
        } finally {
            create.close();
        }
        return userLogged;
        
    }

    
}
