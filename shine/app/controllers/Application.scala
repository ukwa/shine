package controllers

import play.api._
import play.api.mvc._
import scala.collection.JavaConverters._
import uk.bl.wa.shine.Shine
import uk.bl.wa.shine.Query
import uk.bl.wa.shine.Rescued
import uk.bl.wa.shine.Pagination
import uk.bl.wa.shine.GraphData
import uk.bl.wa.shine.model.FacetValue

import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.commons.lang3.StringUtils
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.Map
import play.api.libs.json._

object Application extends Controller {

  val config = play.Play.application().configuration().getConfig("shine");

  val solr = new Shine(config);

  val rescued = new Rescued(config);

  val recordsPerPage = solr.getPerPage()
  val maxNumberOfLinksOnPage = config.getInt("max_number_of_links_on_page")
  val maxViewablePages = config.getInt("max_viewable_pages")
  val facetLimit = config.getInt("facet_limit")

  var pagination = new Pagination(recordsPerPage, maxNumberOfLinksOnPage, maxViewablePages);

  def index = Action {
    Ok(views.html.index("Shine Application"))
  }

  def halflife = Action {
    rescued.halflife();
    Ok(views.html.index("Half-life..."))
  }

  def search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
    
    val action = request.getQueryString("action")
    val selectedFacet = request.getQueryString("selected.facet")
    val removeFacet = request.getQueryString("remove.facet")
	var parameters = collection.immutable.Map(request.queryString.toSeq:_*) 

    println("action: " + action)
    if (action != None) {
	  	val parameter = action.get
	  	println("action " + parameter)
		if (parameter.equals("reset-facets")) {
	  	  println("resetting facets")
	  	  solr.resetFacets()
	  	  parameters = collection.immutable.Map(resetParameters(parameters).toSeq:_*)
	  	  // also remove this stuff - facet.in.crawl_year="2008"&facet.out.public_suffix="co.uk"
	  	} else if (parameter.equals("add-facet") && selectedFacet != None) {
	  	  val facetValue = selectedFacet.get
	  	  solr.addFacet(facetValue)
	  	} else if (parameter.equals("remove-facet") && removeFacet != None) {
	  	  val facetValue = removeFacet.get
	  	  println("removing facet: " + facetValue)
	  	  solr.removeFacet(facetValue)
	  	} 
    }
    val q = doSearch(query, parameters, pageNo, sort, order)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)
    
    println("params: " + q.filters)
    
    Ok(views.html.search.search("Search", q, pagination, sort, order, facetLimit, solr.getOptionalFacets().asScala.toMap))
  }

  def advanced_search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
    println("advanced_search")

    val q = doSearch(query, request.queryString, pageNo, sort, order)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)
    Ok(views.html.search.advanced("Advanced Search", q, pagination, sort, order))
  }

  def plot_graph(query: String, year_start: String, year_end: String) = Action { implicit request =>

    var yearStart = year_start
    var yearEnd = year_end

    if (StringUtils.isBlank(yearStart)) {
      yearStart = config.getString("default_from_year")
    }
    if (StringUtils.isBlank(yearEnd)) {
      yearEnd = config.getString("default_end_year")
    }
    val q = doSearch(query, request.queryString, 0, "", "")
    val totalRecords = q.res.getResults().getNumFound().intValue()
    println("totalRecords: " + totalRecords);

    val from_year: Int = yearStart.toInt
    val to_year: Int = yearEnd.toInt

    var data = new ListBuffer[GraphData]()

    var j = 1
    for (i <- from_year.to(to_year).by(scala.math.pow(20, j).toInt)) {
      // i = year
      // j = data
      j = j + 1
      var graphData = new GraphData(i, j, query)
      println(graphData.getYear + " " + graphData.getData + " " + graphData.getName)
      data += graphData
    }
    println("plotting.... " + data + " " + query + " " + yearStart + " " + yearEnd)

    Ok(views.html.graphs.plot("Plot Graph Test", query, "Years", "label Y", data, yearStart, yearEnd))
  }

  def doSearch(query: String, queryString: Map[String, Seq[String]], pageNo: Int, sort: String, order: String) = {
    val map = queryString
    val javaMap = map.map { case (k, v) => (k, v.asJava) }.asJava;
    println("javaMap: " + javaMap)
    val q = new Query()
    q.query = query
    q.parseParams(javaMap)
    q.res = solr.search(q, pageNo, sort, order)
    q.processFacetsAsParamValues
//    val facets = q.res.getFacetFields().asScala.toList
//    facets = 
//    println("q.facetValues: " + facets)
    q
  }

  def suggest(name: String) = Action { implicit request =>
    val result = solr.suggest(name)
    println("result: " + result.toString)
//    var jsonObject = JsObject(
//        "name" -> JsString("AND")::
//        "name" -> JsString("OR")::Nil)
    Ok(result.toString)
  }  

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(routes.javascript.Application.suggest)).as("text/javascript")
  }

  def resetParameters(parameters: collection.immutable.Map[String, Seq[String]]) = {
	val map = collection.mutable.Map(parameters.toSeq: _*) 
	println("pre: " + map)
//    val javaMap = map.map { case (k, v) => (k, v.asJava) }.asJava;
    for ((k,v) <- map) {
      if (k != "query") {
        map.remove(k)
        println("removed... " + k)
      }
    }
	println("post: " + map)
	map
  }
}