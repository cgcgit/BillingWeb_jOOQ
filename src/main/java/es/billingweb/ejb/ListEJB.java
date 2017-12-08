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
package es.billingweb.ejb;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.Tables.IT_USER;
import static es.billingweb.model.tables.VUserProfile.V_USER_PROFILE;
import es.billingweb.model.tables.pojos.ItUser;
import es.billingweb.model.tables.pojos.VUserProfile;
import es.billingweb.model.tables.records.ItUserRecord;
import java.sql.Connection;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import java.sql.SQLException;

/**
 *
 * Interface bean for the application's user
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */
@Stateless
public class ListEJB implements ListEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;
    

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Gets all active user in the system (from the V_USER_PROFILE view).
     *
     * @return list of current active users in the system
     */
    @Override
    public List<VUserProfile> findAllUsers() {
        
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        List<VUserProfile> users = null;
        try {
            users = create.selectFrom(V_USER_PROFILE)
                    .fetch().into(VUserProfile.class);
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to find all users in the system" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return users;
    }

    /**
     * Gets the detail for an user (table IT_USER).
     *
     * @param userId
     * @return
     */
    @Override
    public List<ItUser> UserDetail(Integer userId) {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        List<ItUser> user = null;

        try {
            user = create.selectFrom(IT_USER)
                    .where(IT_USER.USER_ID.eq(userId))
                    .fetch().into(ItUser.class);
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to get the user's id " + userId.toString() + "detail" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return user;
    }

    @Override
    public int DeleteUserDetail(Integer userId) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        LOGGER.info("Deleting data for the user with UserId " + userId);

        try {
            create.delete(IT_USER)
                    .where(IT_USER.USER_ID.eq(userId))
                    .execute();
            r = 1;
            LOGGER.info("Data for the user with UserId " + userId + " was deleted");

        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to delete the user data on the system" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;
    }

    @Override
    public int DeleteUserListDetail(List<ItUser> listUser) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Add user detail to database
     *
     * @param listUser
     * @return 1: insert OK - 0: insert KO
     */
    @Override
    public int AddUserListDetail(List<ItUser> listUser) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r, r2;

        r = 0;

        LOGGER.info("Adding data for the user ");

        try {

            for (ItUser user : listUser) {
                ItUserRecord userRecord = create.newRecord(IT_USER);
                userRecord.from(user);
                userRecord.insert();
            }
            r = 1;
            LOGGER.info("Data for the user was saved");
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the user data on the system" + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return r;

    }

    @Override
    public int saveUserListDetail(List<ItUser> listUser) {

        //DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = 0;

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
  
            
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES_9_4);

            Integer userId = listUser.get(0).getUserId();

            create.delete(IT_USER)
                    .where(IT_USER.USER_ID.eq(userId))
                    .execute();

            for (ItUser user : listUser) {
                ItUserRecord userRecord = create.newRecord(IT_USER);
                userRecord.from(user);
                userRecord.store();
            }

            r = 1;

        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the user data on the system" + e.getCause().toString(), e);
        } catch (SQLException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to add the user data on the system" + e.getCause().toString(), e);
        } finally {
            //create.close();
            return r;
        }
    }
    
    
}
