package models;

import java.sql.Timestamp;
import java.util.List;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.Column;

import com.avaje.ebean.Model;

@Entity 
@Table(name="resource")
public class Resource extends Model {

	private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
	public String resource_id;
	
    @ManyToOne
    @JoinColumn(name="corpus_id")
    public Corpus corpus;
    
    public String title;
    
    public String url;

    @Column(name = "waybackdate")
    public Date waybackDate;
    
    @Version
    public Timestamp lastUpdate;

    public static Model.Finder<String,Resource> find = new Model.Finder<String,Resource>(String.class, Resource.class);

    public Resource(String title, String url, String resource_id, Date waybackDate) {
		super();
		this.title = title;
		this.url = url;
		this.resource_id = resource_id;
		this.waybackDate = waybackDate;
	}

	public static List<Resource> findByCorpus(Corpus corpus) {
        return find.where()
                .eq("corpus_id", corpus.id)
                .findList();
    }
    
    public static List<Resource> findAll() {
        return find.all();
    }

    public static Resource find(Long id) {
    	return find.where().eq("id", id).findUnique();
    }
}
