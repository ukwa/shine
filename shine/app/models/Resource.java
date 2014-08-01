package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity 
@Table(name="resource")
public class Resource extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    public Long resource_id;
    
    @Constraints.Required
    @Formats.NonEmpty
    public Long corpus_id;
    
    @ManyToOne
    public Corpus corpus;
    
    @Version
    public Timestamp lastUpdate;

    public static Model.Finder<String,Resource> find = new Model.Finder<String,Resource>(String.class, Resource.class);

    public Resource(Long resource_id, Long corpus_id) {
		super();
		this.resource_id = resource_id;
		this.corpus_id = corpus_id;
	}

	public static List<Resource> findByCorpus(Corpus corpus) {
        return find.where()
                .eq("corpus_id", corpus.id)
                .findList();
    }
    
    public static List<Resource> findAll() {
        return find.all();
    }

    public static Resource find(Long resourceId) {
    	return find.where().eq("resource_id", resourceId).findUnique();
    }

    /**
     * Create a new corpus.
     */
    public static Resource create(Long resourceId, Long corpusId) {
    	Resource resource = new Resource(resourceId, corpusId);
    	resource.save();
		return resource;
   }

}
