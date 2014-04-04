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
    
    Ok(views.html.search.search("Search", q, pagination, sort, order, facetLimit, solr.getOptionalFacets().asScala.toMap))
  }

  def advanced_search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
    println("advanced_search")

    val q = doAdvanced(query, request.queryString, pageNo, sort, order)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)
    Ok(views.html.search.advanced("Advanced Search", q, pagination, sort, order))
  }
  
  def browse(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
    println("browse")
    val q = doBrowse(query, request.queryString, pageNo, sort, order)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)
    
    Ok(views.html.search.browse("Browse", q, pagination, sort, order))
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

  def doInit(query: String, queryString: Map[String, Seq[String]], pageNo: Int, sort: String, order: String) = {
    val map = queryString
    val parametersAsJava = map.map { case (k, v) => (k, v.asJava) }.asJava;
    new Query(query, parametersAsJava)
  }
  
  def doSearch(query: String, queryString: Map[String, Seq[String]], pageNo: Int, sort: String, order: String) = {
    val q = doInit(query, queryString, pageNo, sort, order)
    q.processQueryResponse(solr.search(q, pageNo, sort, order))
    q
  }
  
  def doAdvanced(query: String, queryString: Map[String, Seq[String]], pageNo: Int, sort: String, order: String) = {
    val q = doInit(query, queryString, pageNo, sort, order)
    q.processQueryResponse(solr.advancedSearch(q, pageNo, sort, order))
    q
  }

  def doBrowse(query: String, queryString: Map[String, Seq[String]], pageNo: Int, sort: String, order: String) = {
    val q = doInit(query, queryString, pageNo, sort, order)
    q.processQueryResponse(solr.browse(q, pageNo, sort, order))
    q
  }

  def suggestTitle(name: String) = Action { implicit request =>
    val result = solr.suggestTitle(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestUrl(name: String) = Action { implicit request =>
    val result = solr.suggestUrl(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestFileFormat(name: String) = Action { implicit request =>
    val result = solr.suggestFileFormat(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestHost(name: String) = Action { implicit request =>
    val result = solr.suggestHost(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestDomain(name: String) = Action { implicit request =>
    val result = solr.suggestDomain(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestPublicSuffix(name: String) = Action { implicit request =>
    val result = solr.suggestPublicSuffix(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestLinksHosts(name: String) = Action { implicit request =>
    val result = solr.suggestLinksHosts(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestLinksDomains(name: String) = Action { implicit request =>
    val result = solr.suggestLinksDomains(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestLinksPublicSuffixes(name: String) = Action { implicit request =>
    val result = solr.suggestLinksPublicSuffixes(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestAuthor(name: String) = Action { implicit request =>
    val result = solr.suggestAuthor(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  
  
  def suggestCollection(name: String) = Action { implicit request =>
    val result = solr.suggestCollection(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }  

  def suggestCollections(name: String) = Action { implicit request =>
    val result = solr.suggestCollections(name)
    println("result: " + result.toString)
    Ok(result.toString)
  }
  
  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
          routes.javascript.Application.suggestTitle,
          routes.javascript.Application.suggestUrl,
          routes.javascript.Application.suggestFileFormat,
          routes.javascript.Application.suggestLinksHosts,
          routes.javascript.Application.suggestLinksDomains,
          routes.javascript.Application.suggestLinksPublicSuffixes,
          routes.javascript.Application.suggestAuthor,
          routes.javascript.Application.suggestCollection,
          routes.javascript.Application.suggestCollections
      )
    ).as("text/javascript")
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