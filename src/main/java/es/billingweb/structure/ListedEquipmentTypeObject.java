/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.billingweb.structure;

import es.billingweb.model.tables.pojos.MtStatus;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author catuxa
 */
public class ListedEquipmentTypeObject extends ListedEntityObject {
    
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Internal Id for the entity equipment type
    protected final Integer ENTITY_TYPE_EQUIPMENT_TYPE_ID = Integer.parseInt(OTHERS.getString("TYPE_ENTITY_EQUIPMENT_TYPE_ID"));
    
    // Internal Id for the entity equipment type
    protected final Integer CANCEL_STATUS_EQUIPMENT_TYPE_ID = Integer.parseInt(OTHERS.getString("CANCEL_STATUS_EQUIPMENT_TYPE_ID"));
    
    
    
    /**
     * Return the list to populate the one select menu for the equipment type
     * status
     *
     * @return the list of all the status into the system for the equipment types
     */
    public List<SelectItem> populateStatusMenuForEquipmentType() {
        List<SelectItem> statusItem = new ArrayList<>();
        List<MtStatus> list = ejbStatus.findAllStatusForEntity(ENTITY_TYPE_EQUIPMENT_TYPE_ID);

        if (list.isEmpty()) {
            LOGGER.error("ERROR - Not find status");
        } else {
            for (MtStatus p : list) {
                SelectItem item = new SelectItem();
                item.setLabel(p.getStatusCode() + "-" + p.getDescription());
                item.setValue(p.getStatusId());
                statusItem.add(item);
            }
        }
        return statusItem;
    }
    
    
}
