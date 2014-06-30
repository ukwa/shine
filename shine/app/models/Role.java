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
    public String permissions;

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
    			&& permissions.contains(permissionName)) {
    		res = true;
    	}
    	return res;
    }
    
    /**
     * This method returns permissions assigned to this role.
     * @return list of Permission objects
     */
    public List<Permission> getPermissions()
    {
    	List<Permission> res = new ArrayList<Permission>();
    	if (permissions != null && permissions.length() > 0) {
			List<String> resList = Arrays.asList(permissions.split(Const.COMMA + " "));
			Iterator<String> itr = resList.iterator();
			while (itr.hasNext()) {
				res.add(Permission.findByName(itr.next()));
			}
    	}
        return res;
    }
    
    /**
     * This method returns permissions that are not assigned to this role.
     * @return list of Permission objects
     */
    public static List<Permission> getNotAssignedPermissions(String permissionsStr)
    {
    	List<Permission> allPermissionList = Permission.findAll();
//    	Logger.info("Permissions count: " + allPermissionList.size());
        List<Permission> res = new ArrayList<Permission>();
    	if (permissionsStr != null && permissionsStr.length() > 0) {
			List<String> assignedList = Arrays.asList(permissionsStr.split(Const.COMMA + " "));
//			Logger.info("original permissions: " + permissionsStr);
//			Logger.info("assignedList: " + assignedList);
			Iterator<Permission> itrAllPermissions = allPermissionList.iterator();
			while (itrAllPermissions.hasNext()) {
				Permission curPermission = itrAllPermissions.next();
//		    	Logger.info("curPermission: " + curPermission.name);
				if (!assignedList.contains(curPermission.name)) {
					res.add(curPermission);
				}
			}
    	}
        return res;
    }
    
    /**
     * Retrieve all roles.
     */
    public static List<Role> findAll() {
        return find.all();
    }

    /**
     * This method checks if a given role is included in the list of passed user roles.
     * Simple "contains" method of string does not help for roles since part of the role name
     * like "exper_user" could be a name of the other role like "user".
     * @param roleName The given role name
     * @param roles The user roles as a string separated by comma
     * @return true if role name is included
     */
    public static boolean isIncluded(String roleName, String roles) {
    	boolean res = false;
    	if (roleName != null && roleName.length() > 0 && roles != null && roles.length() > 0 ) {
    		if (roles.contains(Const.COMMA)) {
    			List<String> resList = Arrays.asList(roles.split(Const.COMMA));
    			Iterator<String> itr = resList.iterator();
    			while (itr.hasNext()) {
        			String currentRoleName = itr.next();
        			currentRoleName = currentRoleName.replaceAll(" ", "");
        			if (currentRoleName.equals(roleName)) {
        				res = true;
        				break;
        			}
    			}
    		} else {
    			if (roles.equals(roleName)) {
    				res = true;
    			}
    		}
    	}
    	return res;
    }
    
    /**
     * This method checks if a given role is included in the list of passed user roles.
     * Simple "contains" method of string does not help for roles since part of the role name
     * like "exper_user" could be a name of the other role like "user".
     * @param roleName The given role name
     * @param roles The user roles as a string separated by comma
     * @return true if role name is included
     */
    public static boolean isIncludedByUrl(String roleName, String url) {
    	boolean res = false;
    	Logger.info("isIncludedByUrl() url: " + url);
    	try {
	    	if (StringUtils.isNotEmpty(url)) {
		    	String roles = User.findByUrl(url).roles;
		    	if (roleName != null && roleName.length() > 0 && roles != null && roles.length() > 0 ) {
		    		if (roles.contains(Const.COMMA)) {
		    			List<String> resList = Arrays.asList(roles.split(Const.COMMA));
		    			Iterator<String> itr = resList.iterator();
		    			while (itr.hasNext()) {
		        			String currentRoleName = itr.next();
		        			currentRoleName = currentRoleName.replaceAll(" ", "");
		        			if (currentRoleName.equals(roleName)) {
		        				res = true;
		        				break;
		        			}
		    			}
		    		} else {
		    			if (roles.equals(roleName)) {
		    				res = true;
		    			}
		    		}
		    	}
	    	}
    	} catch (Exception e) {
    		Logger.debug("User is not yet stored in database.");
    	}
    	return res;
    }
    
    /**
     * This method evaluates index of the role in the role enumeration.
     * @param roles
     * @return
     */
    public static int getRoleSeverity(String roles) {
    	int res = Const.Roles.values().length;
    	if (roles != null && roles.length() > 0 ) {
    		if (roles.contains(Const.COMMA)) {
    			List<String> resList = Arrays.asList(roles.split(Const.COMMA));
    			Iterator<String> itr = resList.iterator();
    			while (itr.hasNext()) {
        			String currentRoleName = itr.next();
        			currentRoleName = currentRoleName.replaceAll(" ", "");
    				int currentLevel = Const.Roles.valueOf(currentRoleName).ordinal();
    				if (currentLevel < res) {
    					res = currentLevel;
    				}
    			}
    		} else {
    			if (roles.equals(roles)) {
    				res = Const.Roles.valueOf(roles).ordinal();
    			}
    		}
    	}
    	return res;
    }
    
    /**
     * This method validates whether user is allowed to
     * change given role.
     * @param role
     * @param user
     * @return true if user is allowed
     */
    public static boolean isAllowed(Role role, User user) {
    	boolean res = false;
    	if (role != null && role.name != null && role.name.length() > 0) {
    		try {
	    		int roleIndex = Const.Roles.valueOf(role.name).ordinal();
	    		int userIndex = getRoleSeverity(user.roles);
	    		Logger.debug("roleIndex: " + roleIndex + ", userIndex: " + userIndex);
	    		if (roleIndex >= userIndex) {
	    			res = true;
	    		}  
    		} catch (Exception e) {
    			Logger.info("New created role is allowed.");
    			res = true;
    		}
    	}
    	Logger.debug("role allowance check: " + role + ", user: " + user + ", res: " + res);
    	return res;
    }
    
    public String toString() {
        return "Role(" + name + ")" + ", id:" + id;
    }
    
    /**
     * Return a page of User
     *
     * @param page Page to display
     * @param pageSize Number of Roles per page
     * @param sortBy User property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static Page<Role> page(int page, int pageSize, String sortBy, String order, String filter) {

        return find.where().icontains("name", filter)
        		.orderBy(sortBy + " " + order)
        		.findPagingList(pageSize)
        		.setFetchAhead(false)
        		.getPage(page);
    }
}