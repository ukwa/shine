package controllers

import play.api._
import play.api.mvc._
import scala.collection.JavaConverters._
import uk.bl.wa.shine.Shine
import uk.bl.wa.shine.Query
import uk.bl.wa.shine.Pagination
import uk.bl.wa.shine.rescued.Rescued

object Application extends Controller {
  
	val config = play.Play.application().configuration().getConfig("shine");
  
	val solr = new Shine(config);
  
	var pagination = new Pagination();
  
	def index = Action {
		Ok(views.html.index("Shine Application"))
	}
  
	def graph = Action {
		val data = List.fill(50)(scala.util.Random.nextInt)
		Ok(views.html.graphs.plot("Plot Graph Test", "label x", "label y", data))
	}
	
	def search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
	    val map = request.queryString
	    val javaMap = map.map { case (k,v) => (k, v.asJava) }.asJava;
	    val q = new Query()
	    q.query = query
	    q.parseParams(javaMap)
	    q.res = solr.search(query, q.filters, pageNo, sort, order)
	    
	    val totalRecords = q.res.getResults().getNumFound().intValue()
		val recordsPerPage = solr.getPerPage()
		val currentPageSize = q.res.getResults().size()
		
		println("Page #: " + pageNo)
	  	println("totalRecords #: " + totalRecords)
	  	println("recordsPerPage #: " + recordsPerPage)
		
		pagination.update(totalRecords, recordsPerPage, pageNo)
	
	    Ok(views.html.search.search(q, pagination, sort, order))
	}
  
	def advanced_search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
		println("advanced_search")
		val map = request.queryString
		val javaMap = map.map { case (k,v) => (k, v.asJava) }.asJava;
		val q = new Query()
		q.query = query
		q.parseParams(javaMap)
		q.res = solr.search(query, q.filters, pageNo, sort, order)
  
		val totalRecords = q.res.getResults().getNumFound().intValue()
		val recordsPerPage = solr.getPerPage()
		val currentPageSize = q.res.getResults().size()
	
		println("Page #: " + pageNo)
		println("totalRecords #: " + totalRecords)
		println("recordsPerPage #: " + recordsPerPage)
		
		pagination.update(totalRecords, recordsPerPage, pageNo)
	  	Ok(views.html.search.advanced(q, pagination, sort, order))
	}
}