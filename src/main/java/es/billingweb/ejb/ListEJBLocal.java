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

import es.billingweb.model.tables.pojos.ItUser;
import es.billingweb.model.tables.pojos.VUserProfile;
import java.util.List;
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
public interface ListEJBLocal {
    
    /**
     * Find all user's in the system that are active in the current date
     * @return the list of all the current active users in the system
     */
    public List<VUserProfile> findAllUsers();
    
    
    /**
     * Return the data detail for an user
     * @param userId internal identifier for the user
     * @return the list of all data record for the user
     */
    public List<ItUser> UserDetail(Integer userId);
    
    /**
     * Deletes all data for the userDetail
     * @param userId the userId for the user to delete all data
     * @return 1: ok ; 0: error
     */
    public int DeleteUserDetail (Integer userId) ;
    
    /**
     * Deletes all data of the list for the userDetail
     * @param listUser list of data to delete
     * @return 
     */
    public int DeleteUserListDetail (List<ItUser> listUser) ;
    
    
    /**
     * Adds all data of the list for the userDetail
     * @param listUser list of data to add
     * @return 
     */
    public int AddUserListDetail (List<ItUser> listUser);
    
    
    public int saveUserListDetail (List<ItUser> listUser);
    
}
