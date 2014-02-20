package controllers

import play.api._
import play.api.mvc._
import scala.collection.JavaConverters._
import uk.bl.wa.shine.Shine
import uk.bl.wa.shine.Query
import uk.bl.wa.shine.Rescued
import uk.bl.wa.shine.Pagination
import uk.bl.wa.shine.GraphData

import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.commons.lang3.StringUtils
import scala.collection.mutable.ListBuffer

object Application extends Controller {
  
	val config = play.Play.application().configuration().getConfig("shine");
  
	val solr = new Shine(config);
  
	val rescued = new Rescued(config);
  
	var pagination = new Pagination();
  
	def index = Action {
		Ok(views.html.index("Shine Application"))
	}
  
	def halflife = Action {
	    rescued.halflife();
	    Ok(views.html.index("Half-life..."))
	}
  
	def search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>

		val q = doSearch(query, request.queryString, sort, order)
	    
	    val totalRecords = q.res.getResults().getNumFound().intValue()
		val recordsPerPage = solr.getPerPage()
		
		println("Page #: " + pageNo)
	  	println("totalRecords #: " + totalRecords)
	  	println("recordsPerPage #: " + recordsPerPage)
		
		pagination.update(totalRecords, recordsPerPage, pageNo)
	
	    Ok(views.html.search.search(q, pagination, sort, order))
	}
  
	def advanced_search(query: String, pageNo: Int, sort: String, order: String) = Action { implicit request =>
		println("advanced_search")

		val q = doSearch(query, request.queryString, sort, order)

		val totalRecords = q.res.getResults().getNumFound().intValue()
		val recordsPerPage = solr.getPerPage()
	
		println("Page #: " + pageNo)
		println("totalRecords #: " + totalRecords)
		println("recordsPerPage #: " + recordsPerPage)
		
		pagination.update(totalRecords, recordsPerPage, pageNo)
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
		val q = doSearch(query, request.queryString, "", "")
		val totalRecords = q.res.getResults().getNumFound().intValue()
		println("totalRecords: " + totalRecords);

	    val from_year: Int = yearStart.toInt
	    val to_year: Int = yearEnd.toInt

	    var data = new ListBuffer[GraphData]()

	    var j = 1
		for(i <- from_year.to(to_year).by(scala.math.pow(20,j).toInt)) {
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
	
	def doSearch(query: String, queryString: Map[String, Seq[String]], sort: String, order: String) = {
		val map = queryString
		val javaMap = map.map { case (k,v) => (k, v.asJava) }.asJava;
		println("javaMap: " + javaMap)
		val q = new Query()
		q.query = query
		q.parseParams(javaMap)
		q.res = solr.search(query, q.filters, 0, sort, order)
		q
	}
	
	def sum(num: Integer) = {
		var prevNum = num
		prevNum = prevNum + 20 
		val newNum = prevNum
		newNum
	}
	
	def currentYear() = {
		val today = Calendar.getInstance().getTime()
		val yearFormat = new SimpleDateFormat("yyyy")
		val currentYear = yearFormat.format(today)
		println("currentYear: " + currentYear)
		currentYear
	}

	case class Mut[A](var value: A) {}
}