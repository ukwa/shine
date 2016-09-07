package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;
import uk.bl.wa.shine.Const;

import javax.persistence.*;

@Entity
public class Permission extends Model {

	private static final long serialVersionUID = -2250099575468302989L;

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;
    
	@Required
    @Column(columnDefinition = "TEXT")
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Version
    public Timestamp lastUpdate;

    @ManyToMany
    @JoinTable(
        name="role_permissions",
        joinColumns={@JoinColumn(name="permission_id", referencedColumnName="id")},
        inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName="id")})
    public List<Role> roles = new ArrayList<Role>(); 
    
    public static final Model.Finder<Long, Permission> find = new Model.Finder<Long, Permission>(Long.class, Permission.class);

    public Permission(String name, String description) {
		this.name = name;
		this.description = description;
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
    
    public static Permission findByName(String name) {
        return find.where()
                   .eq("name",
                       name)
                   .findUnique();
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