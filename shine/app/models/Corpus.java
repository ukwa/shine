package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.data.format.Formats;
import play.data.validation.Constraints;
import com.avaje.ebean.Model;

@Entity 
@Table(name="corpus")
public class Corpus extends Model {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String name;

    public String metadata;

    public String description;

    public String tags;
    
    public String justification;

    @Constraints.Required
    @Formats.NonEmpty
    public Long user_id;
    
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE }, mappedBy = "corpus")
    public List<Resource> resources = new ArrayList<Resource>();
    
    @Version
    public Timestamp lastUpdate;

    public static Model.Finder<String,Corpus> find = new Model.Finder<String,Corpus>(String.class, Corpus.class);

    
    public Corpus(String name, String description, Long user_id) {
		super();
		this.name = name;
		this.description = description;
		this.user_id = user_id;
	}

    public static List<Corpus> findByUser(User user) {
    	if( user != null){
	        return find.where()
	                .eq("user_id", user.id)
	                .findList();
    	} else {
    		return new ArrayList<Corpus>();
    	}
    }
    
    public static List<Corpus> findAll() {
        return find.all();
    }

    public static Corpus find(Long id) {
    	return find.where().eq("id", id).findUnique();
    }

    /**
     * Create a new corpus.
     */
    public static Corpus create(String name, String description, Long userId) {
    	Corpus corpus = new Corpus(name, description, userId);
    	corpus.save();
		return corpus;
   }
}
