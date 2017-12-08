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

import static es.billingweb.model.Sequences.TID_PROFILE_ID_PROFILE_ID_SEQ;
import static es.billingweb.model.Tables.MT_PROFILE;
import static es.billingweb.model.Tables.TID_PROFILE_ID;
import es.billingweb.model.tables.pojos.MtProfile;
import es.billingweb.model.tables.records.MtProfileRecord;
import es.billingweb.model.tables.records.TidProfileIdRecord;
import static java.lang.Math.toIntExact;
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
import static org.jooq.impl.DSL.currentTimestamp;

/**
 *
 * @author catuxa
 */
@Stateless
public class ProfilesEJB implements ProfilesEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;
    //private final DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Find all profiles
     *
     * @return all the profiles from the system in the current_date
     */
    @Override
    public Result<MtProfileRecord> findAllProfiles() {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        Result<MtProfileRecord> profiles = null;
        try {
            profiles = create.selectFrom(MT_PROFILE)
                    .where(currentTimestamp().between(MT_PROFILE.START_DATE).and(MT_PROFILE.END_DATE))
                    .orderBy(MT_PROFILE.PROFILE_CODE, MT_PROFILE.START_DATE)
                    .fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
        } finally {
            create.close();
        }
        return profiles;
    }

    /**
     * This method inserts a new profile into the database
     *
     * @param profile the profile to insert
     * @return 1 if the profile was inserted, 0 if the profile was not inserted, -1 otherwise
     */
    @Override
    public int addProfile(MtProfile profile) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        int r = -1;
                       
        // Id of the new User
        Integer id = createProfileId();

        profile.setProfileId(id);
        
        LOGGER.info("Add profile: " + profile.toString());

        // Record for the user
        MtProfileRecord profileRecord = create.newRecord(MT_PROFILE);

        profileRecord.from(profile);

        try {
            r = profileRecord.insert();
            switch (r) {
                case 1:
                    LOGGER.info("The profile was added");
                    break;
                case 0:
                    LOGGER.error("ERROR - The profile was not added");
                    break;
                default:
            }
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
        } finally {
            create.close();
        }

        return r;
    }

    /**
     * Deletes a profile by id
     *
     * @param profileId the id for the profile to delete
     * @return 1 if the profile was deleted, 0 otherwise
     */
    @Override
    public int deleteProfile(Integer profileId) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        int r = 0;
        
        LOGGER.info("Delete profile");

        MtProfileRecord profileRecord = create.fetchOne(MT_PROFILE, MT_PROFILE.PROFILE_ID.eq(profileId));

        try {
            r = profileRecord.delete();
            switch (r) {
                case 1:
                    LOGGER.info("The profile was deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The profile was not deleted");
                    break;
                default:
            }
            deleteProfileId(profileId);
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
        } finally {
            create.close();
        }

        return r;
    }

    /**
     * Updates a profile
     *
     * @param profile the profile to update
     * @return 1 if the profile was updated, 0 otherwise
     */
    @Override
    public int updateProfile(MtProfile profile) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        int r = 0;
        
        LOGGER.info("Update profile: " + profile.toString());
        
        // Record for the user
        MtProfileRecord userRecord = create.newRecord(MT_PROFILE);

        userRecord.from(profile);

        try {
            r = userRecord.update();
            switch (r) {
                case 1:
                    LOGGER.info("The user was updated");
                    break;
                case 0:
                    LOGGER.error("ERROR - The user was not updated");
                    break;
                default:
            }
        } catch (DataAccessException e) {
            //System.out.println("ERROR - update profile - " + e.toString());
        } finally {
            create.close();
        }

        return r;
    }

    /**
     * Creates a new profile_id
     *
     * @return the new profile_id
     */
    private Integer createProfileId() {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        // Id of the new User
        Integer id = (Integer) 0;
        Integer r;

        LOGGER.info("Create a new profile id");
        // Record for the user
        TidProfileIdRecord profileRecord = create.newRecord(TID_PROFILE_ID);

        // Obtains the id for the user
        id = toIntExact(create.nextval(TID_PROFILE_ID_PROFILE_ID_SEQ));

        // Set the id for the user
        profileRecord.setProfileId(id);

        try {
            // Update the record
            r=profileRecord.insert();
            switch (r) {
                case 1:
                    LOGGER.info("Profile id created");
                    break;
                case 0:
                    LOGGER.error("ERROR - The profile id was not created");
                    break;
                default:
            }

        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
            id = (Integer) 0;
        } finally {
            create.close();
        }

        return id;

    }
    
    
    /**
     * Deletes an user_id
     * @param userId id from the user to delete
     * @return 1 if the user_id was deleted, 0 if the user_id was not deleted, -1  otherwise
     */
    
    private Integer deleteProfileId(Integer profileId) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        int r = -1;

        LOGGER.info("Delete a profile id");
        // Record for the user
        TidProfileIdRecord userRecord = create.fetchOne(TID_PROFILE_ID, TID_PROFILE_ID.PROFILE_ID.eq(profileId));

        try {
            // Update the record
            r = userRecord.delete();
            switch (r) {
                case 1:
                    LOGGER.info("Profile id deleted");
                    break;
                case 0:
                    LOGGER.error("ERROR - The profile id was not deleted");
                    break;
                default:
            }

        } catch (DataAccessException e) {
            LOGGER.error("ERROR " + e.toString());

        } finally {
            create.close();
        }

        return r;

    }

}
