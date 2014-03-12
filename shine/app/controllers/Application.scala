package controllers

import play.api._
import play.api.mvc._
import scala.collection.JavaConverters._
import uk.bl.wa.shine.Shine
import uk.bl.wa.shine.Query
import uk.bl.wa.shine.Pagination
import uk.bl.wa.shine.GraphData
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.commons.lang3.StringUtils
import scala.collection.mutable.ListBuffer
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

object Application extends Controller {

  val config = play.Play.application().configuration().getConfig("shine");

  val solr = new Shine(config);

  val recordsPerPage = solr.getPerPage()
  val maxNumberOfLinksOnPage = config.getInt("max_number_of_links_on_page")
  val maxViewablePages = config.getInt("max_viewable_pages")
  val facetLimit = config.getInt("facet_limit")

  var pagination = new Pagination(recordsPerPage, maxNumberOfLinksOnPage, maxViewablePages);

  def search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>

    val q = doSearch(query, request.queryString, pageNo, sort, order)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)

    Ok(views.html.search.search(q, pagination, sort, order, facetLimit))
  }

  def advanced_search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
    println("advanced_search")

    val q = doSearch(query, request.queryString, pageNo, sort, order)

    val totalRecords = q.res.getResults().getNumFound().intValue()

    println("Page #: " + pageNo)
    println("totalRecords #: " + totalRecords)

    pagination.update(totalRecords, pageNo)
    Ok(views.html.search.advanced(q, pagination, sort, order))
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
    q.res = solr.search(query, q.filters, pageNo, sort, order)
    q.processFacetsAsParamValues
    q
  }

  def suggest(query: String) = Action { implicit request =>
    val javaMap = request.queryString.map { case (k, v) => (k, v.asJava) }.asJava;

    val q = new Query()
    q.query = query
    q.parseParams(javaMap)
    q.res = solr.search(query, q.filters)
    q.processFacetsAsParamValues
    var jsObject = JsObject(
      "name" -> JsString("AND") ::
        "name" -> JsString("OR") :: Nil)
    Ok(jsObject)

  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(routes.javascript.Application.suggest)).as("text/javascript")
  }

}