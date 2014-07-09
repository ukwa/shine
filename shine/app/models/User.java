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
@SuppressWarnings("serial")
@Entity 
@Table(name="creator")
public class User extends Model {

    @JsonIgnore
    @Constraints.Required
    @Formats.NonEmpty
    public String email;
    
    @Constraints.Required
    public String name;
    
    @JsonIgnore
    public String password;
    
    @JsonIgnore
    public String field_affiliation;
    @Id @JsonIgnore
    public Long uid;
    public String url;
    @JsonIgnore
    public String edit_url;
    @JsonIgnore
    public String last_access;
    @JsonIgnore
    public String last_login;
    @JsonIgnore
    public String created;
    @JsonIgnore
    public Long status;
    @JsonIgnore
    public String language;
    @JsonIgnore
    public Long feed_nid;
    
    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    public String revision; 

    @JsonIgnore
    @Version
    public Timestamp lastUpdate;

    // -- Queries
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Model.Finder<String,User> find = new Model.Finder(String.class, User.class);
    
    public User() {
    	this.revision = "";
    }

    public User(String name) {
    	this.name = name;
    	this.revision = "";
    }

    public User(String name, String email, String password) {
    	this.name = name;
    	this.email = email;
    	this.password = password;
    	this.revision = "";
    }
    
    /**
     * This method checks if this User has a role passed as string parameter.
     * @param roleName
     * @return true if exists
     */
    public boolean hasRole(String roleName) {
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
     * Retrieve a User by name.
     * @param name
     * @return
     */
    public static User findByName(String name) {
        return find.where().eq("name", name).findUnique();
    }
    
    /**
     * Retrieve a User by URL.
     * @param url
     * @return user name
     */
    public static User findByUrl(String url) {
        return find.where().eq(Const.URL, url).findUnique();
    }

    /**
     * Retrieve a User by UID
     * @param id
     * @return
     */
    public static User findByUid(Long id) {
        return find.where().eq(Const.UID, id).findUnique();
    }
    
	/**
	 * This method filters users by name and returns a list of filtered User objects.
	 * @param name
	 * @return
	 */
	public static List<User> filterByName(String name) {
		List<User> res = new ArrayList<User>();
        ExpressionList<User> ll = find.where().contains(Const.EMAIL, name.toLowerCase());
    	res = ll.findList();
		return res;
	}
    
    public String toString() {
        return "User(" + name + ")" + ", url:" + url;
    }
    
    /**
     * This method shows user in HTML page.
     * @param userUrl The link to user in Target object field 'author'
     * @return
     */
    public static String showUser(String userUrl) {
        return User.findByUrl(userUrl).name; 
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

