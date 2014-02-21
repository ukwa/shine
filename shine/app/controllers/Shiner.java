/**
 * 
 */
package controllers;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;

import play.*;
import play.mvc.*;
import uk.bl.wa.shine.rescued.Rescued;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Shiner extends Controller {

	static Configuration config = play.Play.application().configuration().getConfig("shine");

	static Rescued rescued = new Rescued(config);
	  
	public static Result halflife() {
		try {
			rescued.halflife();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ok( views.html.rescued.render("Half-life...") );
	}
	
	public static Result index() {
		return ok("It works!");
	}
	
}
