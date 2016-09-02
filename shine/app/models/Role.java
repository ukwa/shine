package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Version;

import com.avaje.ebean.ExpressionList;

import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;
import uk.bl.wa.shine.Const;

@Entity
public class Role extends Model {

	private static final long serialVersionUID = 5670206529564297517L;

	@Id
    public Long id;
    
	@Required
	@Column(columnDefinition = "TEXT")
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;
    
    @Version
    public Timestamp lastUpdate;

//    @OneToMany(cascade=CascadeType.ALL)
//    @JoinTable(name = "user_roles",
//              joinColumns = @JoinColumn(name = "role_id"),
//              inverseJoinColumns = @JoinColumn(name = "user_id"))

    @ManyToMany
    @JoinTable(
        name="user_roles",
        joinColumns={@JoinColumn(name="role_id", referencedColumnName="id")},
        inverseJoinColumns={@JoinColumn(name="user_id", referencedColumnName="id")})
    public List<User> users = new ArrayList<User>();
    
    
    @ManyToMany
    @JoinTable(
        name="role_permissions",
        joinColumns={@JoinColumn(name="role_id", referencedColumnName="id")},
        inverseJoinColumns={@JoinColumn(name="permission_id", referencedColumnName="id")})
    public List<Permission> permissions = new ArrayList<Permission>(); 
    
    public static final Finder<Long, Role> find = new Finder<Long, Role>(Long.class, Role.class);

    public Role(String name, String description) {
		this.name = name;
		this.description = description;
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
     * Retrieve all roles.
     */
    public static List<Role> findAll() {
        return find.all();
    }
    
    public String toString() {
        return "Role(" + name + ")" + ", id:" + id;
    }
}