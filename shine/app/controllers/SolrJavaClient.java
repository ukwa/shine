package controllers;


import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import org.apache.solr.client.solrj.impl.HttpSolrServer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by teg on 11/9/16.
 */
public class SolrJavaClient {


    public static  NetarchiveDoc getById(String solrServerUrl, String id) throws Exception{

        HttpSolrServer solrServer = new HttpSolrServer(solrServerUrl);

            SolrQuery solrQuery = new SolrQuery();
            solrQuery.set("facet", "false"); //very important. Must overwrite to false. Facets are very slow and expensive.
            solrQuery.setQuery("id:\""+id+"\"");
            solrQuery.setRows(1);

            QueryResponse rsp = solrServer.query(solrQuery);
            SolrDocumentList docs = rsp.getResults();
            if (rsp.getResults().getNumFound() == 0){
                throw new IllegalArgumentException("Id not found:"+id);
            }

             NetarchiveDoc  docJava = solrDoc2NetarchiveDoc(docs).get(0);
             return docJava;

    }


    public static List<NetarchiveDoc> solrDoc2NetarchiveDoc(SolrDocumentList results){

        List<NetarchiveDoc> docs = new ArrayList<NetarchiveDoc>();

        for ( SolrDocument current : results){
            NetarchiveDoc doc = new NetarchiveDoc();
            doc.setWaybackDate(Long.parseLong((String) current.getFieldValue("wayback_date")));
            doc.setYear(Integer.parseInt((String) current.getFieldValue("crawl_year")));
            doc.setCrawlDate((Date)  current.getFieldValue("crawl_date"));
            doc.setTitle((String) current.getFieldValue("title"));
            doc.setUrl((String) current.getFieldValue("url"));
            doc.setUrl_norm((String) current.getFieldValue("url_norm"));
            doc.setDescription((String) current.getFieldValue("description"));
            doc.setAuthor((String) current.getFieldValue("author"));
            doc.setId((String)  current.getFieldValue("id"));
            doc.setHost((String)  current.getFieldValue("host"));
            doc.setDomain((String)  current.getFieldValue("domain"));
            doc.setPublicSuffix((String)  current.getFieldValue("public_suffix"));
            doc.setContentType((String)  current.getFieldValue("content_type_served"));
            doc.setKeywords((String) current.getFieldValue("keywords"));
            doc.setSource_file_s((String) current.getFieldValue("source_file_s"));

            ArrayList<String> servers= (ArrayList<String>)  current.getFieldValue("server");
            if (servers != null && servers.size() >= 1){
                doc.setServer(servers.get(0));
            }

            ArrayList<String> contents= (ArrayList<String>)  current.getFieldValue("content");
            if (contents != null && contents.size() >= 1){
                doc.setContent(contents.get(0));
            }

            ArrayList<String> linksHosts = (ArrayList<String>)  current.getFieldValue("links_hosts");
            doc.setLinksHost(linksHosts);

            Object lengthObj = current.getFieldValue("content_text_length");
            if (lengthObj != null){
                doc.setContentTextLength((Integer) current.getFieldValue("content_text_length"));
            }

            doc.setContentLanguage((String)  current.getFieldValue("content_language"));
            doc.setElementsUsed((ArrayList<String>)  current.getFieldValue("elements_used"));
            doc.setLinksDomains((ArrayList<String>)  current.getFieldValue("links_domains"));
            doc.setLinksPublicSuffixes((ArrayList<String>)  current.getFieldValue("links_public_suffixes"));

            docs.add(doc);
        }
        return docs;
    }

}
