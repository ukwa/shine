/*
* Copyright 2012 Steve Chaloner
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import uk.bl.wa.shine.Const;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "role")
public class Role extends Model
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5670206529564297517L;

	@Id @JsonIgnore
    public Long id;
    
	@Required
	@Column(columnDefinition = "TEXT")
    public String name;

    @Column(columnDefinition = "TEXT")
    public String url;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    public String description;
    
    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    public String revision; 
    
    @JsonIgnore
    @Version
    public Timestamp lastUpdate;

    public static final Finder<Long, Role> find = new Finder<Long, Role>(Long.class, Role.class);

    public String getName()
    {
        return name;
    }

    /**
     * Retrieve an object by Id (id).
     * @param nid
     * @return object 
     */
    public static Role findById(Long id) {
    	Role res = find.where().eq(Const.ID, id).findUnique();
    	return res;
    }          
    
    public static Role findByName(String name)
    {
        return find.where()
                   .eq("name",
                       name)
                   .findUnique();
    }
    
    /**
     * Retrieve a role by URL.
     * @param url
     * @return role name
     */
    public static Role findByUrl(String url) {
//    	Logger.info("role findByUrl: " + url);
    	Role res = new Role();
    	if (url != null && url.length() > 0 && !url.equals(Const.NONE)) {
    		res = find.where().eq(Const.URL, url).findUnique();
    	} else {
    		res.name = Const.NONE;
    	}
    	return res;
    }

	/**
	 * This method filters roles by name and returns a list of filtered Role objects.
	 * @param name
	 * @return
	 */
	public static List<Role> filterByName(String name) {
		List<Role> res = new ArrayList<Role>();
        ExpressionList<Role> ll = find.where().icontains(Const.NAME, name);
    	res = ll.findList();
		return res;
	}
        
    /**
     * This method checks if this Role has a permission passed as string parameter.
     * @param permissionName
     * @return true if exists
     */
    public boolean hasPermission(String permissionName) {
    	boolean res = false;
    	if (permissionName != null && permissionName.length() > 0 
    			&& this.getPermissions().contains(permissionName)) {
    		res = true;
    	}
    	return res;
    }
    
    /**
     * This method returns permissions assigned to this role.
     * @return list of Permission objects
     */
    public List<Permission> getPermissions() {
    	List<Permission> res = new ArrayList<Permission>();
        return res;
    }
    
    /**
     * Retrieve all roles.
     */
    public static List<Role> findAll() {
        return find.all();
    }
    
    public String toString() {
        return "Role(" + name + ")" + ", id:" + id;
    }
}