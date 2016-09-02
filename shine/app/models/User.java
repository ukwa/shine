package models;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.JoinColumn;

import play.Logger;
import play.data.validation.Constraints;
import com.avaje.ebean.Model;
import uk.bl.wa.shine.Const;
import uk.bl.wa.shine.PasswordHash;


/**
 * User entity managed by Ebean
 */
@Entity 
@Table(name="account")
public class User extends Model {

	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
	@SequenceGenerator(name="seq_gen_account", sequenceName="account_seq")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq_gen_account") 
    public Long id;

    @Constraints.Required
    public String email;
    
    @Constraints.Required
    public String password;
    
    @Version
    public Timestamp lastUpdate;
    
    @ManyToMany
    @JoinTable(
        name="user_roles",
        joinColumns={@JoinColumn(name="user_id", referencedColumnName="id")},
        inverseJoinColumns={@JoinColumn(name="role_id", referencedColumnName="id")})
    public List<Role> roles = new ArrayList<Role>(); 

    // -- Queries
    
	public static Model.Finder<String,User> find = new Finder<String, User>(String.class, User.class);

    public User(String email, String password) {
    	this.email = email;
    	this.password = password;
    }
    
    /**
     * This method checks if this User has a role passed as string parameter.
     * @param roleName
     * @return true if exists
     */
    public boolean hasRole(Role role) {
    	boolean res = false;
    	return res;
    }
    
    public List<? extends Role> getRoles()
    {
    	List<Role> res = new ArrayList<Role>();
        return res;
    }

    /**
     * Retrieve all users.
     */
    public static List<User> findAll() {
        return find.all();
    }

    /**
     * Retrieve an object by Id (uid).
     * @param nid
     * @return object 
     */
    public static User findById(Long uid) {
    	User res = find.where().eq(Const.UID, uid).findUnique();
    	return res;
    }

    /**
     * Retrieve a User from email.
     */
    public static User findByEmail(String email) {
        return find.where().eq("email", email).findUnique();
    }
    
    /**
     * Retrieve a User by UID
     * @param id
     * @return
     */
    public static User findByUid(Long id) {
        return find.where().eq(Const.UID, id).findUnique();
    }
    	
	public static User create(String email, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		User user = new User(email, password);
    	String passwordHash = PasswordHash.createHash(password);
    	user.password = passwordHash;
		return user;
	}
	
    public static User updatePassword(String email, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
    	User user = find.where().ieq("email", email).findUnique();
    	String passwordHash = PasswordHash.createHash(password);
//    	Logger.info("convert: " + password + " - " + passwordHash);
    	user.password = passwordHash;
    	user.save();
        return user;
    }
    
    public static User updateEmail(String oldEmail, String newEmail) throws NoSuchAlgorithmException, InvalidKeySpecException {
    	Logger.info(oldEmail + " to " + newEmail);
    	User user = User.findByEmail(oldEmail);
    	user.email = newEmail;
    	user.save();
        return user;
    }

}

