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
import es.billingweb.exception.BillingWebDataAccessException;
import java.util.List;
import javax.ejb.Local;
import org.jooq.exception.DataAccessException;

/**
 *
 * @author catuxa
 */
@Local
public interface ManageUsersEJBLocal {
    
    public List<ItUser> findAllUsersByProfile(Integer profileId)throws DataAccessException;
    
    int addUser(ItUser user)throws BillingWebDataAccessException;
    //int addUser(ItUser user);
    
    int deleteUser(Integer userId)throws DataAccessException;

    int updateUser(ItUser user)throws DataAccessException;
    
    int updateUserPassword (Integer userId, String userPassword)throws DataAccessException;
    
    
    
    
    
}
