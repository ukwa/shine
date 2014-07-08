package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import play.db.ebean.Model;

import com.avaje.ebean.ExpressionList;

import play.data.validation.Constraints;
import play.data.format.Formats;

import java.util.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;


@Entity 
@Table(name="saved_search")
public class Search extends play.db.ebean.Model {

	@Id
    @Constraints.Required
    @Formats.NonEmpty
    public Long id;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String name;
    
    @Constraints.Required
    @Formats.NonEmpty
    @Column(columnDefinition = "TEXT")
    public String url;
    
    @Constraints.Required
    @Formats.NonEmpty
    public Long user_id;
    
    public static Model.Finder<String,Search> find = new Model.Finder<String,Search>(String.class, Search.class);

    
    public Search(String name, String url, Long userId) {
        this.name = name;
        this.url = url;
        this.user_id = userId;
    }
    
    /**
     * Retrieve searches by userid.
     * @param name
     * @return
     */
    public static List<Search> findByUser(User user) {
        return find.fetch("search")
                .where()
                     .eq("search.user_id", user.uid)
                .findList();

    }
    
    /**
     * Retrieve all saved searches.
     */
    public static List<Search> findAll() {
        return find.all();
    }

    /**
     * Create a new search.
     */
    public static Search create(String name, String url, Long userId) {
		Search search = new Search(name, url, userId);
		search.save();
		return search;
   }

}
