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
import play.api.Play.current
import play.api.cache.Cache
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.response.RangeFacet
import org.apache.solr.common.SolrDocument
import scala.collection.JavaConverters._

object Search extends Controller {

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
    println("sort #: " + sort)
    println("order #: " + order)

    pagination.update(totalRecords, pageNo)

    Ok(views.html.search.browse("Browse", q, pagination, sort, order))
  }

  def plot_graph(query: String, year_start: String, year_end: String) = Action { implicit request =>

    // public suffixes and domains too?
    
    // select?q=*:*&facet=true&facet.date=crawl_date&facet.date.gap=%2B1YEAR&facet.date.start=1994-01-01T00:00:00.00Z&facet.date.end=NOW%2B1YEAR
    var yearStart = year_start
    var yearEnd = year_end

    if (StringUtils.isBlank(yearStart)) {
      yearStart = config.getString("default_from_year")
    }
    if (StringUtils.isBlank(yearEnd)) {
      yearEnd = config.getString("default_end_year")
    }
    println("yearEnd: " + yearEnd)
    
    var values = query.split(",")
    
    var graphMap:Map[Query,Map[String,ListBuffer[GraphData]]] = {
      
		var map:Map[Query,Map[String,ListBuffer[GraphData]]] = Map()

		for(text <- values) {
		    val value = text.trim
		    val q = doGraph(value, request.queryString)

		    println("query: " + q.query);

		    val totalRecords = q.res.getResults().getNumFound().intValue()
		    println("totalRecords: " + totalRecords);
		
		    var listMap:Map[String,ListBuffer[GraphData]] = getGraphData(q)
		    
		    if (StringUtils.isNotEmpty(q.dateStart)) {
		      yearStart = q.dateStart
		    }
		    if (StringUtils.isNotEmpty(q.dateEnd)) {
		      yearEnd = q.dateEnd
		    }
		    if (!listMap.isEmpty) {
			    map += (q -> listMap)
		    }
		}
		map
    }
    
//    val head = graphMap.head
//    val q = head._1
//    val listMap = head._2
//    q.query = query
    Ok(views.html.graphs.plot("NGram", query, "Years", "Count", yearStart, yearEnd, graphMap))
  }

  def processChart = Action { implicit request =>
    println("processChart: " + request.queryString)
    val query = request.getQueryString("query")
    val yearStart = request.getQueryString("year_start")
    val yearEnd = request.getQueryString("year_end")
    println("query: " + query)
    println("yearStart" + yearStart)
    println("yearEnd: " + yearEnd)

    var queryString: String = {
    	var value = " "
	    query match {
			case Some(parameter) => {
				println("parameter: " + parameter)
				value = parameter

			}
			case None => {
				println("None")
			}
		}
    	value
    }
    
	val values = queryString.split(",")

	val result:JsArray = {
		// query : 'nhs', data : array
		var jsonArray = Json.arr()
		
		for(text <- values) {
		  
		    val value = text.trim
		    val q = doGraph(value, request.queryString)

		    println("query: " + q.query);

		    val totalRecords = q.res.getResults().getNumFound().intValue()
		    println("totalRecords: " + totalRecords);
		
		    var listMap:Map[String,ListBuffer[GraphData]] = getGraphData(q)
		    
		    // val.value + " " + val.count + " " + val.query

		    var graphData:JsArray = {
	    		// "nhs" -> array
		    	var array = Json.arr()
			    for ((key, value) <- listMap) {
			    	println (key + "-->" + value)
			    	value.map(graphData => {
						val jsonObject = Json.obj("value" -> JsString(graphData.getValue()), "count" -> JsNumber(graphData.getCount()))
						println(graphData.getValue() + " " + graphData.getCount())
						array = array :+ jsonObject
			    	})
			    }
		    	array
		    }
		    
		    val jsonObjArray = Json.obj("query" -> JsString(text), "data" -> graphData)
		    jsonArray = jsonArray :+ jsonObjArray;

		}
		jsonArray
	}
	
    println("resultList: " + result)
    Ok(result)
  }
  
  def getGraphData(q: Query) = {
    
	var data:Map[String,ListBuffer[GraphData]] = {
		var map:Map[String,ListBuffer[GraphData]] = Map()
	
		for (i <- 0 until q.res.getFacetRanges().size) {
			val range = q.res.getFacetRanges().get(i)
			val facetName = range.getName()
			val counts = range.getCounts()
			val iterator = counts.iterator()
			var data = new ListBuffer[GraphData]()
			  
			while(iterator.hasNext()) {
				val count = iterator.next()
				var value = count.getValue().split("-")(0)
				var graphData = new GraphData(value, count.getCount().toInt)
				data += graphData
			}
			if (!data.isEmpty) {
				map += (facetName -> data)
			}
		}
		map
	}
	data
  }
  
  def ajaxSearch = Action { implicit request =>
    val query = request.getQueryString("query")
    val page = request.getQueryString("page")
    
    println("request: " + request)
    println("queryString: " + request.queryString)
    
    var queryString: String = {
    	var value = ""
	    query match {
			case Some(parameter) => {
				println("parameter: " + parameter)
				value = parameter

			}
			case None => {
				println("None")
			}
		}
    	value
    }

	var pageNo: Int = {
    	var value = ""
	    page match {
			case Some(parameter) => {
				println("parameter: " + parameter)
				value = parameter

			}
			case None => {
				println("None")
			}
		}
    	value.toInt
    }

    println("query: " + queryString)

    var parameters = collection.immutable.Map(request.queryString.toSeq: _*)

    val q = doSearch(queryString, parameters)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)
    
    val results = q.res.getResults()							
    
    var resultList = Json.arr()

    // should be based on pageNo
    for (i <- 0 to (results.size()-1)) {
      val result = results.get(i)
      
      val url = JsString("http://web.archive.org/web/"+ notBlank(result.getFirstValue("wayback_date")) + "/" + notBlank(result.getFirstValue("url")))
      val jsonObject = Json.obj("title" -> JsString(notBlank(result.getFirstValue("title"))), "url" -> url, "crawl_date" -> JsString(notBlank(result.getFirstValue("crawl_date"))), 
    		  				"content_type_norm" -> JsString(notBlank(result.getFirstValue("content_type_norm"))), 
    		  				"domain" -> JsString(notBlank(result.getFirstValue("domain"))), 
    		  				"sentiment_score" -> JsString(notBlank(result.getFirstValue("sentiment_score"))))
 
      resultList = resultList :+ jsonObject
    }
    val jsonSub = Json.obj("result" -> resultList)
    
    // add pagination to json
    var pageList = Json.arr()
    for (page <- pagination.getPagesList().toArray()) {
	  pageList = pageList :+ JsNumber(page.##)
    }

    println("pagesList: " + pageList)
    
	val jsonPager = Json.obj("totalItems" -> JsNumber(pagination.getTotalItems()),
				"hasPreviousPage" -> JsBoolean(pagination.hasPreviousPage()),
				"currentPage" -> JsNumber(pagination.getCurrentPage()),
				"pagesList" -> pageList,
				"maxViewablePages" -> JsNumber(pagination.getMaxViewablePages()),
				"hasNextPage" -> JsBoolean(pagination.hasNextPage()),
				"hasMaxViewablePagedReached" -> JsBoolean(pagination.hasMaxViewablePagedReached()),
				"maxNumberOfLinksOnPage" -> JsNumber(pagination.getMaxNumberOfLinksOnPage()),
				"displayingXOfY" -> JsString(pagination.getDisplayXtoYofZ(" to "," of ")))
				
	var jsonData =
      Json.obj(
        "results" -> resultList,
        "pager" -> jsonPager)

    Ok(jsonData)
  }

  def notBlank(x: Object) = {
    if (x != null) {
    	x.toString()
    } else {
      ""
    }
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

  def doGraph(query: String, parameters: Map[String, Seq[String]]) = {
    val q = createQuery(query, parameters)
    solr.graph(q)
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
      
    pageParameter match {
		case Some(parameter) => {
			page = pageParameter.get.toInt
			println("page: " + page)
		}
		case None => {
			println("None")
		}
	}

    sortParameter match {
		case Some(parameter) => {
			sort = sortParameter.get
			println("sort: " + sort)
		}
		case None => {
			println("None")
		}
	}

    orderParameter match {
		case Some(parameter) => {
			order = orderParameter.get
			println("order: " + order)
		}
		case None => {
			println("None")
		}
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
