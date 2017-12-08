package es.billingweb.controller;

import es.billingweb.model.tables.records.TestMenuRecord;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.jooq.Result;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import es.billingweb.model.tables.pojos.VUserProfile;
import es.billingweb.utils.BillingWebSessionUtils;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultSubMenu;
import es.billingweb.ejb.LeftMenuLocalEJB;

/**
 * Managed Bean to control the left menu of the page.
 *
 * @author catuxa
 * @since july 2016
 * @version 1.0.0
 *
 */
@Named
@SessionScoped
public class TopMenuController implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ResourceBundle pageProperties = ResourceBundle.getBundle("properties.pages");
    private final ResourceBundle generalProperties = ResourceBundle.getBundle("properties.general_config");
    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private LeftMenuLocalEJB ejbMenu;
    private Result<TestMenuRecord> parentList;
    private Result<TestMenuRecord> childrenListL1;
    private Result<TestMenuRecord> childrenListL2;

    private MenuModel model;

    @PostConstruct
    public void init() {
        if (model == null) {
            model = new DefaultMenuModel();
            this.createLeftMenu();
        }
    }

    public MenuModel getModel() {
        return model;
    }

    public void setModel(MenuModel model) {
        this.model = model;
    }

    /**
     * Generates the left menu of the page
     */
    public void createLeftMenu() {

        FacesContext context = FacesContext.getCurrentInstance();
        String currentUrl = context.getViewRoot().getViewId();
        VUserProfile applicationUser = (VUserProfile) context.getExternalContext().getSessionMap().get(BillingWebSessionUtils.CONTEXT_KEY);
        String baseURL = pageProperties.getString("BASE_URL");
        String tailURL = pageProperties.getString("TAIL_URL");
        String url;

        LOGGER.debug("Current URL: " + currentUrl);

        //Gets the first level menus (root menu)
        parentList = ejbMenu.findByLevel(0, applicationUser.getProfileCode());

        if (parentList.isEmpty()) {
            LOGGER.error("ERROR - Menu not find");
        } else {
            for (TestMenuRecord p : parentList) {
                DefaultSubMenu firstSubmenu;
                firstSubmenu = new DefaultSubMenu(p.getMenuCode());

                //Gets the frist level menus (the menus hanging from the root)
                childrenListL1 = ejbMenu.findChildren(p.getMenuId(), applicationUser.getProfileCode());

                if (childrenListL1.isNotEmpty()) {
                    for (TestMenuRecord c1 : childrenListL1) {
                        if (c1.getMenuType().equals("I")) {
                            //Final Item - The end of the nested menu
                            DefaultMenuItem item1 = new DefaultMenuItem(c1.getMenuCode());
                            url = baseURL + c1.getPage() + tailURL;
                            item1.setUrl(c1.getPage());
                            firstSubmenu.addElement(item1);
                        } else {
                            //This item is a parent from another item (nested menu)
                            DefaultSubMenu secondSubmenu = new DefaultSubMenu(c1.getMenuCode());
                            firstSubmenu.addElement(secondSubmenu);
                            //Gets the children items
                            childrenListL2 = ejbMenu.findChildren(c1.getMenuId(), applicationUser.getProfileCode());
                            if (childrenListL2.isNotEmpty()) {
                                for (TestMenuRecord c2 : childrenListL2) {
                                    if (c2.getMenuType().equals("I")) {
                                        //Final Item - The end of the nested menu
                                        DefaultMenuItem item2 = new DefaultMenuItem(c2.getMenuCode());
                                        url = baseURL + c2.getPage() + tailURL;
                                        item2.setUrl(c2.getPage());
                                        secondSubmenu.addElement(item2);
                                    } else {
                                        LOGGER.error("ERROR - Unexpected value for the menu item " + c2.getMenuId() + " - " + c2.getMenuCode());
                                    }
                                }
                            } else {
                                LOGGER.error("ERROR - frist level nested menu " + c1.getMenuId() + " - " + c1.getMenuId() + " - " + c1.getMenuCode() + " without child items");
                            }
                        }
                    }
                    model.addElement(firstSubmenu);
                } else {
                    LOGGER.error("ERROR - root menu " + p.getMenuId() + " - " + p.getMenuCode() + " without children");
                }
            }
        }

    }

}
