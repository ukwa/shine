package controllers

import play.api._
import play.api.mvc._
import scala.collection.JavaConverters._
import java.util.List
import uk.bl.wa.shine.Shine
import uk.bl.wa.shine.Query
import uk.bl.wa.shine.Rescued

object Application extends Controller {
  
  val config = play.Play.application().configuration().getConfig("shine");
  
  val solr = new Shine(config);
  
  val rescued = new Rescued(config);
  
  
  def index = Action {
    Ok(views.html.index("Shine Application"))
  }
  
  def halflife = Action {
    rescued.halflife();
    Ok(views.html.index("Half-life..."))
  }
  
  def search (query: String, pageNo: Int, offset: Int, resultsOf: Int) = Action { implicit request =>
  	println("Page #: " + pageNo);
    val map = request.queryString;
    val javaMap = map.map { case (k,v) => (k, v.asJava) }.asJava;
    val q = new Query()
    q.query = query
    q.parseParams(javaMap)
    q.res = solr.search(query, q.filters, pageNo)
    
    val noOfRecords = q.res.getResults().getNumFound()
    val recordsPerPage = solr.getPerPage()
    val noOfPages = Math.ceil(noOfRecords * 1.0 / recordsPerPage).intValue()
    val offset = (pageNo - 1) * recordsPerPage + 1
    val resultsOf = 0

    Ok(views.html.search(q, pageNo, noOfPages, offset, resultsOf))
  }
  
}