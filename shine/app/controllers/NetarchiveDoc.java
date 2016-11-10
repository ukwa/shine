package controllers;


import java.util.ArrayList;
import java.util.Date;

public class NetarchiveDoc {
   private long waybackDate;
    private int year;
    private Date crawlDate;
    private String content;
    private String id;
    private String url;
    private String host;
    private String domain;
    private String publicSuffix;
    private String server;
    private int contentTextLength;
    private String contentType;
    private String title;
    private String contentLanguage;
    private String description;
    private String author;
    private String url_norm;
    private String keywords;
    private String source_file_s;
    private ArrayList<String> elementsUsed;
    private ArrayList<String> linksDomains;
    private ArrayList<String> linksHost;
    private ArrayList<String> linksPublicSuffixes;

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
    public ArrayList<String> getElementsUsed() {
        return elementsUsed;
    }
    public void setElementsUsed(ArrayList<String> elementsUsed) {
        this.elementsUsed = elementsUsed;
    }
    public ArrayList<String> getLinksDomains() {
        return linksDomains;
    }
    public void setLinksDomains(ArrayList<String> linksDomains) {
        this.linksDomains = linksDomains;
    }
    public ArrayList<String> getLinksPublicSuffixes() {
        return linksPublicSuffixes;
    }
    public void setLinksPublicSuffixes(ArrayList<String> linksPublicSuffixes) {
        this.linksPublicSuffixes = linksPublicSuffixes;
    }
    public int getContentTextLength() {
        return contentTextLength;
    }
    public void setContentTextLength(int contentTextLength) {
        this.contentTextLength = contentTextLength;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getLinksHost() {
        return linksHost;
    }
    public void setLinksHost(ArrayList<String> linksHost) {
        this.linksHost = linksHost;
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

    @Override
    public String toString() {
        return "NetarchiveDoc{" +
                "waybackDate=" + waybackDate +
                ", year=" + year +
                ", crawlDate=" + crawlDate +
                ", content='" + content + '\'' +
                ", id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", domain='" + domain + '\'' +
                ", publicSuffix='" + publicSuffix + '\'' +
                ", server='" + server + '\'' +
                ", contentTextLength=" + contentTextLength +
                ", contentType='" + contentType + '\'' +
                ", title='" + title + '\'' +
                ", contentLanguage='" + contentLanguage + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", url_norm='" + url_norm + '\'' +
                ", keywords='" + keywords + '\'' +
                ", source_file_s='" + source_file_s + '\'' +
                ", elementsUsed=" + elementsUsed +
                ", linksDomains=" + linksDomains +
                ", linksHost=" + linksHost +
                ", linksPublicSuffixes=" + linksPublicSuffixes +
                '}';
    }
}
