package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import uk.bl.wa.shine.Const;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "permission")
public class Permission extends Model
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2250099575468302989L;

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

    public static final Model.Finder<Long, Permission> find = new Model.Finder<Long, Permission>(Long.class, Permission.class);

    public String getName()
    {
        return name;
    }

    /**
     * Retrieve an object by Id (id).
     * @param nid
     * @return object 
     */
    public static Permission findById(Long id) {
    	Permission res = find.where().eq(Const.ID, id).findUnique();
    	return res;
    }          
    
    public static Permission findByName(String name)
    {
        return find.where()
                   .eq("name",
                       name)
                   .findUnique();
    }
    
    /**
     * Retrieve a permission by URL.
     * @param url
     * @return permission name
     */
    public static Permission findByUrl(String url) {
//    	Logger.info("permission findByUrl: " + url);
    	Permission res = new Permission();
    	if (url != null && url.length() > 0 && !url.equals(Const.NONE)) {
    		res = find.where().eq(Const.URL, url).findUnique();
    	} else {
    		res.name = Const.NONE;
    	}
    	return res;
    }

	/**
	 * This method filters permissions by name and returns a list of filtered Permission objects.
	 * @param name
	 * @return
	 */
	public static List<Permission> filterByName(String name) {
		List<Permission> res = new ArrayList<Permission>();
        ExpressionList<Permission> ll = find.where().icontains(Const.NAME, name);
    	res = ll.findList();
		return res;
	}
        
    /**
     * Retrieve all permissions.
     */
    public static List<Permission> findAll() {
        return find.all();
    }
    
    public String toString() {
        return "Permission(" + name + ")" + ", id:" + id;
    }
}