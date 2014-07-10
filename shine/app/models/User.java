package models;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import uk.bl.wa.shine.Const;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.bl.wa.shine.PasswordHash;


/**
 * User entity managed by Ebean
 */
@Entity 
@Table(name="creator")
public class User extends Model {

    @SequenceGenerator(name="seq_gen_name", sequenceName="creator_seq")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq_gen_name") 
    @Id
    public Long uid;

    @JsonIgnore
    @Constraints.Required
    @Formats.NonEmpty
    public String email;
    
    @JsonIgnore
    public String password;
    
    @JsonIgnore
    @Version
    public Timestamp lastUpdate;

    // -- Queries
    
	public static Model.Finder<String,User> find = new Model.Finder(String.class, User.class);

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
		user.save();
		return user;
	}
	
    public static User update(String email, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
    	User user = User.findByEmail(email);
    	String passwordHash = PasswordHash.createHash(password);
    	Logger.info("convert: " + password + " - " + passwordHash);
    	user.password = passwordHash;
    	user.save();
        return user;
    }
}

