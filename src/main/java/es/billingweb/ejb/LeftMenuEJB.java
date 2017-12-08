/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.ejb;

import es.billingweb.exception.BillingWebDataAccessException;
import static es.billingweb.model.tables.TestMenu.TEST_MENU;
import es.billingweb.model.tables.records.TestMenuRecord;

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
import static org.jooq.impl.DSL.val;

/**
 *
 * Interface bean for the left menu of the pages
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */
@Stateless
public class LeftMenuEJB implements LeftMenuLocalEJB {

    @Resource(lookup = "jdbc/db_billing")
    private DataSource ds;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * findAll order by menu_level and position
     *
     * @param profile
     * @return
     */
    @Override
    public Result<TestMenuRecord> findAll(String profile) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        Result<TestMenuRecord> menu = null;
        try {
            menu = create.selectFrom(TEST_MENU)
                    .where(TEST_MENU.PROFILE_CODE.equal(profile))
                    .orderBy(TEST_MENU.MENU_LEVEL, TEST_MENU.POSITION)
                    .fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to find the menu of the application - " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return menu;
    }

    /**
     * findById order by menu_level and position
     *
     * @param id
     * @param profile
     * @return
     */
    @Override
    public Result<TestMenuRecord> findById(Integer id, String profile
    ) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Result<TestMenuRecord> menu = null;

        try {
            menu = create.selectFrom(TEST_MENU)
                    .where(TEST_MENU.MENU_ID.equal(id))
                    .and(TEST_MENU.PROFILE_CODE.equal(profile))
                    .fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to find the menu of the application - " + e.getCause().toString(), e);
        } finally {
            create.close();
        }
        return menu;
    }

    /**
     * find AllRootParents order by position
     *
     * @param profile
     * @return
     */
    @Override
    public Result<TestMenuRecord> findAllRootParents(String profile
    ) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Result<TestMenuRecord> menu = null;
        try {
            menu = create.selectFrom(TEST_MENU)
                    .where(TEST_MENU.MENU_ID.isNull())
                    .and(TEST_MENU.PROFILE_CODE.equal(profile))
                    .orderBy(TEST_MENU.POSITION).fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to find the menu of the application - " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return menu;
    }

    /**
     * findAllChildren order by level and position
     *
     * @param profile
     * @return
     */
    @Override
    public Result<TestMenuRecord> findAllChildren(String profile
    ) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Result<TestMenuRecord> menu = null;

        try {
            menu = create.selectFrom(TEST_MENU)
                    .where(TEST_MENU.SUBMENU_ID.isNotNull())
                    .and(TEST_MENU.PROFILE_CODE.equal(val(profile)))
                    .orderBy(TEST_MENU.MENU_LEVEL, TEST_MENU.POSITION).fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to find the menu of the application - " + e.getCause().toString(), e);
        } finally {
            create.close();
        }

        return menu;
    }

    /**
     * findChildren order by level, parent_id position and
     *
     * @param id
     * @param profile
     * @return
     */
    @Override
    public Result<TestMenuRecord> findChildren(Integer parentId, String profile
    ) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);

        Result<TestMenuRecord> menu = null;
        try {
            menu=create.selectFrom(TEST_MENU)
                    .where(TEST_MENU.SUBMENU_ID.equal(parentId))
                    .and(TEST_MENU.PROFILE_CODE.equal(profile))
                    .orderBy(TEST_MENU.MENU_LEVEL, TEST_MENU.SUBMENU_ID, TEST_MENU.POSITION).fetch();

        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to find the menu of the application - " + e.getCause().toString(), e);
        } finally {
            create.close();
        }
        return menu;
    }

    /**
     * findByLevel order by parent id and position
     *
     * @param level
     * @param profile
     * @return
     */
    @Override
    public Result<TestMenuRecord> findByLevel(Integer level, String profile
    ) {
        DSLContext create = DSL.using(ds, SQLDialect.POSTGRES_9_4);
        Result<TestMenuRecord> menu = null;

        try {
            menu = create.selectFrom(TEST_MENU)
                    .where(TEST_MENU.MENU_LEVEL.equal(level))
                    .and(TEST_MENU.PROFILE_CODE.equal(profile))
                    .orderBy(TEST_MENU.SUBMENU_ID, TEST_MENU.POSITION).fetch();
        } catch (DataAccessException e) {
            LOGGER.error("ERROR - " + e.getCause().toString());
            throw new BillingWebDataAccessException("Error while try to find the menu of the application - " + e.getCause().toString(), e);
        } finally {
            create.close();
        }
        return menu;
    }

}
