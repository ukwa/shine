/**
 * 
 */
package uk.bl.wa.shine.vis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * @author andy
 *
 */
public class Concordance {

	/**
	 * @param args
	 * @throws SolrServerException 
	 */
	public static void main(String[] args) throws SolrServerException {
		String server = "http://openstack9.ad.bl.uk:8983/solr/jisc2_shard1_replica1";
		
		SolrServer ss = new HttpSolrServer(server);
		
		SolrQuery baseline = new SolrQuery("*:*");
		baseline.setFacet(true);
		baseline.addFacetField("year_s");
		baseline.setFacetSort("index");
		baseline.setFacetLimit(5000);
		QueryResponse b = ss.query(baseline);
		FacetField f  = b.getFacetField("year_s");
		Map<String,Long> pages = new HashMap<String,Long>();
		long total_pages = 0;
		for( Count y : f.getValues() ) {
			pages.put(y.getName(), y.getCount());
			total_pages += y.getCount();
			//sSystem.out.println("Putted "+y.getName()+" "+y.getCount());
		}
		System.out.println("Total Pages: "+total_pages);
		
		SolrQuery p = new SolrQuery("\"sherlock holmes\"");
		p.addSort("pubdate_dt", ORDER.asc);
		p.setRows(15);
		p.setHighlight(true);
		p.addHighlightField("content");
		p.setHighlightFragsize(256);
	    p.setParam("hl.maxAnalyzedChars", "512000");
		p.setFacet(true);
		p.addFacetField("year_s");
		p.setFacetMinCount(1);
		p.setFacetLimit(5000);
		p.setFacetSort("index");
		
		QueryResponse r = ss.query(p);
		
		// Number of results:
		System.out.println("Matches: "+r.getResults().getNumFound()+" of " +total_pages+" pages.");

		// Facet summary:
		f  = r.getFacetField("year_s");
		for( Count y : f.getValues() ) {
			System.out.println(y.getName()+": "+y.getCount()+"/"+pages.get(y.getName()));
		}

		// Fragments:
		int counter = 0;
		for( String k : r.getHighlighting().keySet() ) {
			Map<String, List<String>> hl = r.getHighlighting().get(k);
			for( String hl_k : hl.keySet() ) {
				List<String> hl_v = hl.get(hl_k);
				for( String hl_l : hl_v ) {
					counter ++;
					System.out.println(k+" "+counter+ " " +" "+hl_l);
				}
			}
		}
		
	}

}
