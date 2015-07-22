/**
 * 
 */
package uk.bl.wa.shine;

import java.util.Properties;

import org.apache.solr.client.solrj.impl.HttpSolrServer;

import play.Configuration;
import play.Logger;


/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public abstract class Solr {

	private String host = null;
	private HttpSolrServer solr;
	
	public Solr( Configuration config ) {
		// Host:
		host = config.getString("host");
		Logger.info("Setting up Solr client for host = "+host);
		// Set the proxy up:
		Properties systemProperties = System.getProperties();
		if( config.getString("http.proxyHost") != null ) {
			systemProperties.setProperty("http.proxyHost", config.getString("http.proxyHost") );
			systemProperties.setProperty("http.proxyPort", config.getString("http.proxyPort") );
		}
		// Set up Solr client:
		solr = new HttpSolrServer(host);
		// Timeouts:
		solr.setConnectionTimeout(5000);
		solr.setSoTimeout(1000);
		// Max connections:
		solr.setDefaultMaxConnectionsPerHost(100);
		solr.setMaxTotalConnections(100);
	}
	
	public HttpSolrServer getSolrServer() {
		return solr;
	}
}
