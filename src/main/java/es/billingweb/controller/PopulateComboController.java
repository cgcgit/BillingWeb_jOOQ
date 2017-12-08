/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.controller;

import es.billingweb.ejb.ProfilesEJBLocal;
import es.billingweb.ejb.StatusEJBLocal;
import es.billingweb.model.tables.records.MtProfileRecord;
import es.billingweb.model.tables.records.MtStatusRecord;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Result;

/**
 *
 * @author catuxa
 */
@Named(value = "populateComboController")
@SessionScoped
public class PopulateComboController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger();
    
    @EJB
    private ProfilesEJBLocal ejbProfile;

    @EJB
    private StatusEJBLocal ejbStatus;
    /**
     * Creates a new instance of PopulateComboController
     */
    public PopulateComboController() {
    }
    
    
    public List<SelectItem> populateProfileMenu() {
        List<SelectItem> profileItem=new ArrayList<>();
        Result<MtProfileRecord> profileRecord = ejbProfile.findAllProfiles();

        if (profileRecord.isEmpty()) {
            LOGGER.error("ERROR - Not find profiles");
        } else {
            for (MtProfileRecord p : profileRecord) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getProfileCode() + "-" + p.getDescription());
                item.setValue(p.getProfileId());
                boolean add;
                add = profileItem.add(item);
            }
        }
        return profileItem;

    }
    
    
    public List<SelectItem> populateStatusMenu() {
        List<SelectItem> statusItem=new ArrayList<>();
        Integer entityId = ejbStatus.findUserEntityId();
        Result<MtStatusRecord> statusRecord = ejbStatus.findAllStatusForEntity(entityId);

        if (statusRecord.isEmpty()) {
            LOGGER.error("ERROR - Not find status");
        } else {
            for (MtStatusRecord p : statusRecord) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getStatusCode() + "-" + p.getDescription());
                item.setValue(p.getStatusId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }
    
    
    
    
}
