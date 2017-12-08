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

import static es.billingweb.model.Sequences.TID_USER_ID_USER_ID_SEQ;
import static es.billingweb.model.Tables.IT_USER;
import static es.billingweb.model.Tables.TID_USER_ID;
import es.billingweb.model.tables.pojos.ItUser;
import es.billingweb.model.tables.records.ItUserRecord;
import es.billingweb.model.tables.records.TidUserIdRecord;
import es.billingweb.exception.BillingWebDataAccessException;
import static java.lang.Math.toIntExact;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.*;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;

/**
 *
 * @author catuxa
 */
@Stateless
public class ManageUsersEJB implements ManageUsersEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Find all user by Profile Id. If an exception occurs, it will be
     * propagated.
     *
     * @param profileId
     * @return all the users with a specific profile in a current date.
     */
    @Override
    public List<ItUser> findAllUsersByProfile(Integer profileId) throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        List<ItUser> users = null;
        try {
            users = create.selectFrom(IT_USER)
                    .where(IT_USER.PROFILE_ID.equal(profileId)
                            .and(currentTimestamp().between(IT_USER.START_DATE).and(IT_USER.END_DATE)))
                    .orderBy(IT_USER.USER_CODE, IT_USER.START_DATE)
                    .fetch().into(ItUser.class);
        } catch (DataAccessException e) {
            throw e;
        } finally {
            create.close();
        }

        return users;
    }

    /**
     * This method inserts a new user into the database. We need to create a new
     * user id before add the new user to the database If an exception occurs,
     * it will be propagated.
     *
     * @param user the user to insert
     * @return 1 if the user was inserted, 0 if the user was not inserted, -1
     * otherwise (exception)
     *
     */
    @Override
    public int addUser(ItUser user) throws BillingWebDataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;
        Integer id = 0;
        //We need an id for the new User --> creates a new user id
        id = createUserId();

        user.setUserId(id);

        LOGGER.info("Adding a new user");

        // Record for the user
        ItUserRecord userRecord = create.newRecord(IT_USER);

        userRecord.from(user);

        try {
            r = userRecord.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The user was added");
                    break;
                case 0:
                    LOGGER.error("ERROR - The user was not added");
                    this.deleteUserId(id);
                    break;
                default:
                    LOGGER.error("ERROR - The result is not as expected");
            }        
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            throw new BillingWebDataAccessException("Error adding new user " + e.getCause().toString(), e);
            //throw (EJBException) new EJBException(e).initCause(e);

        } finally {

            create.close();
        }

        return r;

    }

    /**
     * Deletes an user by id. If an exception occurs, it will be propagated.
     *
     * @param userId the id for the user to delete
     * @return 1 if the user was deleted, 0 if the user was not deleted, -1
     * otherwise (exception)
     */
    @Override
    public int deleteUser(Integer userId) throws DataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Deleting an existing user");

        ItUserRecord userRecord = create.fetchOne(IT_USER, IT_USER.USER_ID.eq(userId));

        try {

            // Deleting the user
            r = userRecord.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The user was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The user was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result is not as expected");
            }
            // If we can deleted the user --> We must delete it's user id
            deleteUserId(userId);

        } finally {
            create.close();
        }

        return r;
    }

    /**
     * Updates an User. If an exception occurs, it will be propagated.
     *
     * @param user the user to update
     * @return 1 if the user was updated, 0 if the user was not updated, -1
     * otherwise (exception)
     */
    @Override
    public int updateUser(ItUser user) throws DataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        int r = -1;

        LOGGER.info("Updating user");
        // Record for the user             
        ItUserRecord userRecord = create.newRecord(IT_USER);

        userRecord.from(user);

        try {
            // Updating the user
            r = userRecord.update();

            switch (r) {
                case 1:
                    LOGGER.info("The user was updated");
                    break;
                case 0:
                    LOGGER.error("ERROR - The user was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result is not as expected");
            }

        } finally {
            create.close();
        }
        return r;
    }

    /**
     * Update the user's password. If an exception occurs, it will be
     * propagated.
     *
     * @param userId id from the user to modify
     * @param userPassword new password
     * @return 1 if the user's password was updated, 0 if the user's password
     * was not updated, -1 otherwise (exception)
     */
    @Override
    public int updateUserPassword(Integer userId, String userPassword) throws DataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Updating password for user");

        ItUserRecord userRecord = create.fetchOne(IT_USER, IT_USER.USER_ID.eq(userId));
        userRecord.setPassword(userPassword);

        try {
            r = userRecord.update();
            switch (r) {
                case 1:
                    LOGGER.info("The user's password was updated");
                    break;
                case 0:
                    LOGGER.error("ERROR - The user's password was not updated");
                    break;
                default:
                    LOGGER.error("ERROR - The result is not as expected");
            }
        } finally {
            create.close();
        }

        return r;

    }

    /**
     * Creates a new user_id. If an exception occurs, it will be propagated.
     *
     * @return the new user_id. If an error occurrs the return value will be 0.
     */
    private Integer createUserId() throws DataAccessException {

        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = (Integer) 0;
        Integer r;

        LOGGER.info("Creating a new user_id");
        // Record for the user
        TidUserIdRecord userRecord = create.newRecord(TID_USER_ID);

        // Obtains the id for the user
        id = toIntExact(create.nextval(TID_USER_ID_USER_ID_SEQ));

        // Set the id for the user
        userRecord.setUserId(id);

        try {
            // Update the record
            r = userRecord.insert();
            switch (r) {
                case 1:
                    LOGGER.info("User id created");
                    break;
                case 0:
                    LOGGER.error("ERROR - The user id was not created");
                    break;
                default:
                    LOGGER.error("ERROR - The result is not as expected");
            }

        } finally {
            create.close();
        }

        return id;

    }

    /**
     * Deletes an user_id. If an exception occurs, it will be propagated.
     *
     * @param userId id from the user to delete
     * @return 1 if the user_id was deleted, 0 if the user_id was not deleted,
     * -1 otherwise (exception)
     */
    private Integer deleteUserId(Integer userId) throws DataAccessException {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Delete an user id");
        // Record for the user
        TidUserIdRecord userRecord = create.fetchOne(TID_USER_ID, TID_USER_ID.USER_ID.eq(userId));

        try {
            // Update the record
            r = userRecord.delete();
            switch (r) {
                case 1:
                    LOGGER.info("User id deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The user id was not deleted");
                    break;
                default:
                    LOGGER.error("ERROR - The result is not as expected");
            }
        } finally {
            create.close();
        }

        return r;

    }

}
