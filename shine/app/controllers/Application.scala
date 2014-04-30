package controllers

import play.api._
import play.api.mvc._
import scala.collection.JavaConverters._
import uk.bl.wa.shine.Shine
import uk.bl.wa.shine.Query
import uk.bl.wa.shine.Pagination
import uk.bl.wa.shine.GraphData
import uk.bl.wa.shine.model.FacetValue
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.commons.lang3.StringUtils
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.Map
import scala.collection.mutable.MutableList
import play.api.libs.json._
import scala.util.parsing.json.JSONObject
import play.api.Play.current
import play.api.cache.Cache

object Application extends Controller {

  val config = play.Play.application().configuration().getConfig("shine")

  val solr = new Shine(config)

  val recordsPerPage = solr.getPerPage()
  val maxNumberOfLinksOnPage = config.getInt("max_number_of_links_on_page")
  val maxViewablePages = config.getInt("max_viewable_pages")
  val facetLimit = config.getInt("facet_limit")

  var pagination = new Pagination(recordsPerPage, maxNumberOfLinksOnPage, maxViewablePages);

  var facetValues: Map[String, FacetValue] = Cache.getOrElse[Map[String, FacetValue]]("facet.values") {
    solr.getFacetValues.asScala.toMap
  }

  def index = Action {
    Ok(views.html.index("Shine Application"))
  }

  def search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>

    val action = request.getQueryString("action")
    val selectedFacet = request.getQueryString("selected.facet")
    val removeFacet = request.getQueryString("remove.facet")
    var parameters = collection.immutable.Map(request.queryString.toSeq: _*)

    action match {
		case Some(parameter) => {
			println("parameter: " + parameter)
			println("action " + parameter)
			if (parameter.equals("reset-facets")) {
				println("resetting facets")
				solr.resetFacets()
				parameters = collection.immutable.Map(resetParameters(parameters).toSeq: _*)
				// also remove this stuff - facet.in.crawl_year="2008"&facet.out.public_suffix="co.uk"
			} 
		}
		case None => {
			println("None")
		}
	}

    val q = doSearch(query, parameters)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)

    Cache.getAs[Map[String, FacetValue]]("facet.values") match {
	    case Some(value) => {
	    	play.api.Logger.debug("getting value from cache ...")
	    	Ok(views.html.search.search("Search", q, pagination, sort, order, facetLimit, solr.getOptionalFacets().asScala.toMap, value))
		}
		case None => {
			println("None")
	    	Ok(views.html.search.search("Search", q, pagination, sort, order, facetLimit, solr.getOptionalFacets().asScala.toMap, null))
		}
    }
  }

  def advanced_search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
    println("advanced_search")

    val q = doAdvanced(query, request.queryString)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)
    Ok(views.html.search.advanced("Advanced Search", q, pagination, sort, order))
  }

  def browse(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
    println("browse")
    val q = doBrowse(query, request.queryString)

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
    val q = doSearch(query, request.queryString)
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

  def createQuery(query: String, parameters: Map[String, Seq[String]]) = {
    val map = parameters
    val parametersAsJava = map.map { case (k, v) => (k, v.asJava) }.asJava;
    println("doInit: " + parametersAsJava);
    new Query(query, parametersAsJava)
  }

  def doSearch(query: String, parameters: Map[String, Seq[String]]) = {
    // parses parameters and creates me a query object
    var q = createQuery(query, parameters)
    println("new query created: " + q.facets)
    solr.search(q)

  }

  def doAdvanced(query: String, parameters: Map[String, Seq[String]]) = {
    val q = createQuery(query, parameters)
    solr.advancedSearch(q)
  }

  def doBrowse(query: String, parameters: Map[String, Seq[String]]) = {
    val q = createQuery(query, parameters)
    solr.browse(q)
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

  def getFacets = Action { implicit request =>
    println("queryString: " + request.queryString)
    val pageParameter = request.getQueryString("page")
    val sortParameter = request.getQueryString("sort")
    val orderParameter = request.getQueryString("order")
    var page = 1
    var sort = "crawl_date"
    var order = "asc"
    //{page=[1], query=[*:*], order=[asc], facet.in.collection=["Acute Trusts"], selected.facet=[author], sort=[content_type_norm]}
    if (pageParameter != None) {
      page = pageParameter.get.toInt
    }
    if (sortParameter != None) {
      sort = sortParameter.get
    }
    if (orderParameter != None) {
      order = orderParameter.get
    }

    val queryResponse = doBrowse("*:*", request.queryString).res
    var results = queryResponse.getResults()

    val subCollections = queryResponse.getFacetField("collections")
    println("facetQuery: " + subCollections)

    val totalRecords = results.getNumFound().intValue()

    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, page)

    //    http://192.168.1.204:8983/solr/ldwa/select?start=0&sort=crawl_date+asc&q=*%3A*&fq={!tag%3Dcollection}collection%3A%28%22Acute+Trusts%22%29
    //    http://192.168.1.204:8983/solr/ldwa/select?start=0&sort=crawl_date+asc&q=*%3A*&facet.mincount=1&fq=%7B%21tag%3Dcollection%7Dcollection%3A%28%22Acute+Trusts%22%29
    //http://localhost:9000/search?query=*%3A*&page=2&sort=content_type_norm&facet.in.collection=%22Acute%20Trusts%22

    //  for sub collections
    ///select?start=0&q=*%3A*&fq={!tag%3Dcollections}collections%3A("Acute Trusts")&facet=true&facet.field=collections&facet.mincount=1&rows=0
    //    var resultList = List[JsObject]()
    var resultList = Json.arr()

    if (page == 1) {
      val it = subCollections.getValues().iterator()
      var jsArray = Json.arr()
      while (it.hasNext) {
        val subCollection = it.next
        val jsonSub = Json.obj("name" -> JsString(subCollection.getName()), "count" -> JsNumber(subCollection.getCount()))
        jsArray = jsArray :+ jsonSub
      }
      val jsonObject = Json.obj("subcollection" -> jsArray)
      resultList = resultList :+ jsonObject
      println("jsArray: " + jsArray)
    }

    for (i <- 1 until results.size()) {
      val result = results.get(i)
      val url = result.getFirstValue("url")
      println("title: " + result.getFirstValue("title"))
      if (url ne null) {
        //	    	resultList = result.getFirstValue("url").toString() :: resultList
        val jsonObject = Json.obj("url" -> JsString(result.getFirstValue("url").toString()))
        resultList = resultList :+ jsonObject
      }
    }

    val jsonPages = Json.obj()
    println("jsonPages: " + jsonPages)
    var collectionJson =
      Json.obj(
        "collection" -> resultList,
        "pages" -> JsNumber(pagination.getTotalPages))

    println("collectionJson: " + collectionJson)

    Ok(collectionJson)
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
        routes.javascript.Application.suggestCollections,
        routes.javascript.Application.getFacets)).as("text/javascript")
  }

  def resetParameters(parameters: collection.immutable.Map[String, Seq[String]]) = {
    val map = collection.mutable.Map(parameters.toSeq: _*)
    println("pre: " + map)
    //    val javaMap = map.map { case (k, v) => (k, v.asJava) }.asJava;
    for ((k, v) <- map) {
      if (k != "query") {
        map.remove(k)
        println("removed... " + k)
      }
    }
    println("post: " + map)
    map
  }

}
