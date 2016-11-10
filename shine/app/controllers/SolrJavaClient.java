package controllers;


import models.NetarchiveDoc;
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


    public static NetarchiveDoc getById(String solrServerUrl, String id) throws Exception{

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

    @SuppressWarnings("unchecked")
    private static List<NetarchiveDoc> solrDoc2NetarchiveDoc(SolrDocumentList results){

        List<NetarchiveDoc> docs = new ArrayList<NetarchiveDoc>();



        for ( SolrDocument current : results){
            NetarchiveDoc doc = new NetarchiveDoc();

            doc.setArc_full((String) current.getFieldValue("arc_full"));
            doc.setArc_job((String) current.getFieldValue("arc_job"));
            doc.setArc_harvest((String) current.getFieldValue("arc_harvest"));
            doc.setArc_harvesttime((String)  current.getFieldValue("arc_harvesttime"));
            doc.setAuthor((String) current.getFieldValue("author"));
            doc.setCrawlDate((Date)  current.getFieldValue("crawl_date"));
            doc.setContent_type_ext((String)  current.getFieldValue("content_type_ext"));
            doc.setContent_type_served((String)  current.getFieldValue("content_type_served"));
            doc.setContent_type_droid((String) current.getFieldValue("content_type_droid"));
            doc.setContent_type_tika((String) current.getFieldValue("content_type_tika"));
            doc.setContent_type_full((String) current.getFieldValue("content_type_full"));
            doc.setContent_type_version((String) current.getFieldValue("content_type_version"));
            doc.setDescription((String) current.getFieldValue("description"));
            doc.setLast_modified((Date)  current.getFieldValue("last_modified"));
            doc.setLast_modified_year((String)current.getFieldValue("last_modified_year"));

            doc.setWaybackDate(Long.parseLong((String) current.getFieldValue("wayback_date")));
            doc.setYear(Integer.parseInt((String) current.getFieldValue("crawl_year")));
            doc.setTitle((String) current.getFieldValue("title"));
            doc.setUrl((String) current.getFieldValue("url"));
            doc.setUrl_norm((String) current.getFieldValue("url_norm"));
            doc.setId((String)  current.getFieldValue("id"));
            doc.setHost((String)  current.getFieldValue("host"));
            doc.setDomain((String)  current.getFieldValue("domain"));
            doc.setPublicSuffix((String)  current.getFieldValue("public_suffix"));
            doc.setKeywords((String) current.getFieldValue("keywords"));
            doc.setSource_file_s((String) current.getFieldValue("source_file_s"));


            String hash =  ((ArrayList<String>)  current.getFieldValue("hash")).get(0); //Always 1, should never have been defined as multivalue
            doc.setHash(hash);

            ArrayList<String> servers= (ArrayList<String>)  current.getFieldValue("server");
            if (servers != null && servers.size() >= 1){
                doc.setServer(servers.get(0));
            }


            ArrayList<String> contents= (ArrayList<String>)  current.getFieldValue("content");
            if (contents != null && contents.size() >= 1){
                doc.setContent(contents.get(0));
            }

            ArrayList<String>  contentType = (ArrayList<String>)  current.getFieldValue("content_type");
            doc.setContentType(contentType);

            ArrayList<String>  licenseUrl = (ArrayList<String>)  current.getFieldValue("license_url");
            doc.setLicenseUrl(licenseUrl);

            ArrayList<String>  generator = (ArrayList<String>)  current.getFieldValue("generator");
            doc.setGenerator(generator);
            ArrayList<String> parse_error = (ArrayList<String>)  current.getFieldValue("parse_error");
            doc.setParseError(parse_error);

            //content_text_lengths is never set in Solr. maybe fix late 2017 after next reindex
            Object lengthObj = current.getFieldValue("content_text_length");
            if (lengthObj != null){
                doc.setContentTextLength((Integer) current.getFieldValue("content_text_length"));
            }

            doc.setContentLanguage((String)  current.getFieldValue("content_language"));
            docs.add(doc);
        }
        return docs;
    }

}
