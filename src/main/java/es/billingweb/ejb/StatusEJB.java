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

import static es.billingweb.model.Tables.MT_ENTITY_TYPE;
import static es.billingweb.model.Tables.MT_STATUS;
import es.billingweb.model.tables.records.MtStatusRecord;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 *
 * @author catuxa
 */
@Stateless
public class StatusEJB implements StatusEJBLocal {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;
    private final DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Result<MtStatusRecord> findAllStatusForEntity(Integer entityTypeId) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Result<MtStatusRecord> status = null;
        try {
            status = create.selectFrom(MT_STATUS)
                    .where(MT_STATUS.ENTITY_TYPE_ID.eq(entityTypeId))
                    .orderBy(MT_STATUS.STATUS_CODE)
                    .fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage() );
        } finally {
            create.close();
        }
        return status;
    }

    @Override
    public Integer findUserEntityId() {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        Integer result = null;
        try {
            result = create.selectFrom(MT_ENTITY_TYPE)
                    .where(MT_ENTITY_TYPE.ENTITY_TYPE_CODE.eq("USER")).fetchOne(MT_ENTITY_TYPE.ENTITY_TYPE_ID);

        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage());
        } finally {
            create.close();
        }
        return result;
    }

    @Override
    public Integer findProfileEntityId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Integer findStatusIdForEntityFromStatusCode (Integer entityTypeId, String statusCode){
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Result<Record1<Integer>> id = null;
        try {
            id=create.select(MT_STATUS.STATUS_ID).from(MT_STATUS)
                    .where(MT_STATUS.ENTITY_TYPE_ID.eq(entityTypeId))
                    .and(MT_STATUS.STATUS_CODE.eq(statusCode))
                    .fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage() );
        } finally {
            create.close();
        }
        return id.getValue(0, MT_STATUS.STATUS_ID);
    }
    
    @Override
    public String findStatusCodeForEntityFromStatusId (Integer entityTypeId, Integer statusId){
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Result<Record1<String>> code = null;
        try {
            code=create.select(MT_STATUS.STATUS_CODE).from(MT_STATUS)
                    .where(MT_STATUS.ENTITY_TYPE_ID.eq(entityTypeId))
                    .and(MT_STATUS.STATUS_ID.eq(statusId))
                    .fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getMessage() );
        } finally {
            create.close();
        }
        return code.getValue(0, MT_STATUS.STATUS_CODE);
    }

}
