package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;
import play.data.format.Formats;


@Entity 
@Table(name="saved_search")
public class Search extends Model {
    
	private static final long serialVersionUID = 1L;

	@Id 
    public Long id;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Constraints.Required
    @Formats.NonEmpty
    @Column(columnDefinition = "TEXT")
    public String url;
    
    @Constraints.Required
    @Formats.NonEmpty
    @Column(columnDefinition = "TEXT")
    public String summary;

    @Constraints.Required
    @Formats.NonEmpty
    public Long user_id;
    
    @Version
    public Timestamp lastUpdate;

    public static Model.Finder<String,Search> find = new Model.Finder<String,Search>(String.class, Search.class);

    
    public Search(String name, String description, String summary, String url, Long userId) {
        this.name = name;
        this.description = description;
        this.summary = summary;
        this.url = url;
        this.user_id = userId;
    }
    
    /**
     * Retrieve searches by userid.
     * @param author
     * @return
     */
    public static List<Search> findByUser(User user) {
        return find.where()
                .eq("user_id", user.id)
                .findList();

    }
    
    /**
     * Retrieve all saved searches.
     */
    public static List<Search> findAll() {
        return find.all();
    }

    public static Search find(Long id) {
    	return find.where().eq("id", id).findUnique();
    }

    /**
     * Create a new search.
     */
    public static Search create(String name, String description, String summary, String url, Long userId) {
		Search search = new Search(name, description, summary, url, userId);
		search.save();
		return search;
   }
}
