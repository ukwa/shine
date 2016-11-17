package models;


import java.util.ArrayList;
import java.util.Date;

public class NetarchiveDoc {
   private long waybackDate;
    private int year;
    private String arc_full;
    private String arc_harvest;
    private String arc_job;
    private String arc_harvesttime;
    private Date crawlDate;
    private String content;
    private int contentTextLength;
    private String content_type_ext;
    private String content_type_served;
    private String content_type_droid;
    private String content_type_tika;
    private String content_type_full;
    private String content_type_version;
    private String last_modified_year;
    private Date last_modified;
    private String id;
    private String hash;
    private String url;
    private String host;
    private String domain;
    private String publicSuffix;
    private String server;
    private String title;
    private String contentLanguage;
    private String description;
    private String author;
    private String url_norm;
    private String keywords;
    private String source_file_s;
    private ArrayList<String> contentType;
    private ArrayList<String> generator;
    private ArrayList<String> parseError;
    private ArrayList<String> licenseUrl;
    public long getWaybackDate() {
        return waybackDate;
    }
    public void setWaybackDate(long waybackDate) {
        this.waybackDate = waybackDate;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public Date getCrawlDate() {
        return crawlDate;
    }
    public void setCrawlDate(Date crawlDate) {
        this.crawlDate = crawlDate;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getPublicSuffix() {
        return publicSuffix;
    }
    public void setPublicSuffix(String publicSuffix) {
        this.publicSuffix = publicSuffix;
    }
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public int getContentTextLength() {
        return contentTextLength;
    }
    public void setContentTextLength(int contentTextLength) {
        this.contentTextLength = contentTextLength;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl_norm() {
        return url_norm;
    }

    public void setUrl_norm(String url_norm) {
        this.url_norm = url_norm;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getSource_file_s() {
        return source_file_s;
    }

    public void setSource_file_s(String source_file_s) {
        this.source_file_s = source_file_s;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getArc_full() {
        return arc_full;
    }

    public void setArc_full(String arc_full) {
        this.arc_full = arc_full;
    }

    public String getArc_harvest() {
        return arc_harvest;
    }

    public void setArc_harvest(String arc_harvest) {
        this.arc_harvest = arc_harvest;
    }

    public String getArc_job() {
        return arc_job;
    }

    public void setArc_job(String arc_job) {
        this.arc_job = arc_job;
    }

    public String getArc_harvesttime() {
        return arc_harvesttime;
    }

    public void setArc_harvesttime(String arc_harvesttime) {
        this.arc_harvesttime = arc_harvesttime;
    }

    public String getContent_type_ext() {
        return content_type_ext;
    }

    public void setContent_type_ext(String content_type_ext) {
        this.content_type_ext = content_type_ext;
    }

    public String getContent_type_served() {
        return content_type_served;
    }

    public void setContent_type_served(String content_type_served) {
        this.content_type_served = content_type_served;
    }

    public String getContent_type_droid() {
        return content_type_droid;
    }

    public void setContent_type_droid(String content_type_droid) {
        this.content_type_droid = content_type_droid;
    }

    public String getContent_type_tika() {
        return content_type_tika;
    }

    public void setContent_type_tika(String content_type_tika) {
        this.content_type_tika = content_type_tika;
    }

    public String getContent_type_full() {
        return content_type_full;
    }

    public void setContent_type_full(String content_type_full) {
        this.content_type_full = content_type_full;
    }

    public ArrayList<String> getContentType() {
        return contentType;
    }

    public void setContentType(ArrayList<String> contentType) {
        this.contentType = contentType;
    }

    public String getContent_type_version() {
        return content_type_version;
    }

    public void setContent_type_version(String content_type_version) {
        this.content_type_version = content_type_version;
    }

    public String getLast_modified_year() {
        return last_modified_year;
    }

    public void setLast_modified_year(String last_modified_year) {
        this.last_modified_year = last_modified_year;
    }

    public Date getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(Date last_modified) {
        this.last_modified = last_modified;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public ArrayList<String> getGenerator() {
        return generator;
    }

    public void setGenerator(ArrayList<String> generator) {
        this.generator = generator;
    }

    public ArrayList<String> getParseError() {
        return parseError;
    }

    public void setParseError(ArrayList<String> parseError) {
        this.parseError = parseError;
    }

    public ArrayList<String> getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(ArrayList<String> licenseUrl) {
        this.licenseUrl = licenseUrl;
    }
}


