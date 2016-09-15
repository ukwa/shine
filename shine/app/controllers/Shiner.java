package controllers;

import au.com.bytecode.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import uk.bl.wa.shine.Query;
import uk.bl.wa.shine.Shine;
import uk.bl.wa.shine.exception.ShineException;
import uk.bl.wa.shine.model.TrendData;
import uk.bl.wa.shine.vis.Rescued;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.*;

@Singleton
public class Shiner extends Controller {

    private Rescued rescued;
    private Shine solr;

    @Inject()
    Shiner(Rescued rescued, Shine solr) {
        this.rescued = rescued;
        this.solr = solr;
    }

    public Result halflife() {
        try {
            rescued.halflife();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SolrServerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ok(views.html.vis.rescued.render("Half-life...", "halflife"));
    }

    public Result trendsTsv(String query, String year_start, String year_end) throws SolrServerException, ShineException {
        Logger.info("Q: " + query + " " + year_start + " " + year_end);

        // Set up the query parameters:
        Map<String, List<String>> params = getQueryParams(year_start, year_end);

        // Grab the baseline data:
        Query baseline = solr.graph(new Query("*:*", params));
        Logger.info("BASELINE: " + baseline.res);
        TrendData baseTrend = extractTrendData(baseline);
        Map<Date, Long> baseHits = baseTrend.getHits();

        // Start building up the TSV
        StringWriter s = new StringWriter();
        CSVWriter writer = new CSVWriter(s, '\t');
        // feed in your array (or convert your data to an array)
        String[] entries = new String[]{"Query", "Date", "Hits", "Percentage", "Total Crawled"};
        writer.writeNext(entries);


        // Loop through the query terms to grab the results:
        String[] terms = query.split(",");
        String[] line = new String[5];
        for (String term : terms) {
            Query q = new Query(term, params);

            // Do the graph:
            q = solr.graph(q);
            Logger.info("Found: " + q.res.getResults().getNumFound());

            // Write the result:
            TrendData td = extractTrendData(q);
            Map<Date, Long> hits = td.getHits();
            for (Date date : hits.keySet()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                line[0] = term;
                line[1] = Integer.toString(cal.get(Calendar.YEAR));
                if (hits.get(date) != null) {
                    line[2] = hits.get(date).toString();
                    if (baseHits.get(date) > 0) {
                        line[3] = Double.toString(100.0 * (double) hits.get(date) / (double) baseHits.get(date));
                    } else {
                        line[3] = "0.0";
                    }
                } else {
                    line[2] = "0.0";
                    line[3] = "0.0";
                }
                if (baseHits.get(date) != null) {
                    line[4] = "" + baseHits.get(date);
                } else {
                    line[4] = "0.0";
                }
                writer.writeNext(line);
            }
        }

        // Get the TSV
        try {
            writer.close();
        } catch (IOException e) {
            Logger.error("Exception while closing TSV: ", e);
        }
        String tsv = s.toString();

        // Serve the result, normalising the query as this can cause
        // problems interpreting the filename in the disposition field.
        String normQuery = query.replace('"', '_');
        normQuery = normQuery.replace('\'', '_');
        response().setContentType("text/tab-separated-values; charset=utf-8");
        response().setHeader("Content-disposition", "attachment; filename=\"trend-" +
                year_start + "-" + year_end + "-" + normQuery + "\".tsv");
        return ok(tsv);
    }

    private Map<String, List<String>> getQueryParams(String year_start, String year_end) {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put("year_start", Arrays.asList(year_start));
        params.put("year_end", Arrays.asList(year_end));
        return params;
    }

    private TrendData extractTrendData(Query q) {
        int start = Integer.parseInt(q.getParameters().get("year_start").get(0));
        int end = Integer.parseInt(q.getParameters().get("year_end").get(0));
        TrendData td = new TrendData(start, end, 1);
        for (@SuppressWarnings("rawtypes") RangeFacet fr : q.res.getFacetRanges()) {
            Logger.info("FR: " + fr.getName() + " " + fr.getCounts());
            if ("crawl_date".equals(fr.getName())) {
                RangeFacet.Date frd = (RangeFacet.Date) fr;
                for (RangeFacet.Count c : frd.getCounts()) {
                    Calendar cDate = javax.xml.bind.DatatypeConverter.parseDateTime(c.getValue());
                    Logger.info("FRDC: " + cDate.get(Calendar.YEAR) + " " + c.getCount());
                    td.setHitsForYear(cDate.get(Calendar.YEAR), c.getCount());
                }
            }
        }
        return td;
    }

    public Result sampleFromRange(String query, String year) throws SolrServerException, ShineException {
        // Set up the query parameters:
        Map<String, List<String>> params = getQueryParams(year, Integer.toString(Integer.parseInt(year) + 1));

        // Grab the baseline data:
        params.put("sort", Arrays.asList(new String[]{"random_12"}));
        params.put("facet.in.crawl_year", Arrays.asList(new String[]{year}));
        Query sample = new Query(query, params);

        // Do the search:
        sample = solr.search(sample, 100);

        // Set up a Json output:
        ObjectNode result = Json.newObject();

        // Pull out the highlights:
        for (String id : sample.res.getHighlighting().keySet()) {
            ObjectNode item = result.putObject(id);
            // Pick up highlights
            Map<String, List<String>> hls = sample.res.getHighlighting().get(id);
            ArrayNode matches = item.putArray("matches");
            for (String field : hls.keySet()) {
                for (String match : hls.get(field)) {
                    ArrayNode m = matches.addArray();
                    // Merge contiguous matches:
                    // c.f. better approaches in http://stackoverflow.com/questions/19266432/highlighting-exact-phrases-with-solr
                    match = match.replace("</em> <em>", " ");
                    // And split:
                    String[] parts = match.split("<em>");
                    m.add(parts[0]);
                    String[] tail = parts[1].split("</em>");
                    m.add(tail[0]);
                    if (tail.length > 1) {
                        m.add(tail[1]);
                    } else {
                        m.add("");
                    }
                }
            }

            // And look for other info:
            for (SolrDocument doc : sample.res.getResults()) {
                if (id.equals(doc.get("id"))) {
                    item.put("domain", (String) doc.get("domain"));
                    item.put("url", (String) doc.get("url"));
                    item.put("wayback_date", (String) doc.get("wayback_date"));
                }
            }
        }

        // Return the sample summary:
        return ok(result);
    }
}