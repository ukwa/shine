package uk.bl.wa.shine.model;

public class SearchData {
	private String title;
	private String host;
	private String publicSuffix;
	private String crawlYear;
	private String contentTypeNorm;
	private String contentLanguage;
	private String crawlDate;
	private String url;
	private String waybackDate;
	
	public SearchData(String title, String host, String publicSuffix,
			String crawlYear, String contentTypeNorm, String contentLanguage,
			String crawlDate, String url, String waybackDate) {
		super();
		this.title = title;
		this.host = host;
		this.publicSuffix = publicSuffix;
		this.crawlYear = crawlYear;
		this.contentTypeNorm = contentTypeNorm;
		this.contentLanguage = contentLanguage;
		this.crawlDate = crawlDate;
		this.url = url;
		this.waybackDate = waybackDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPublicSuffix() {
		return publicSuffix;
	}

	public void setPublicSuffix(String publicSuffix) {
		this.publicSuffix = publicSuffix;
	}

	public String getCrawlYear() {
		return crawlYear;
	}

	public void setCrawlYear(String crawlYear) {
		this.crawlYear = crawlYear;
	}

	public String getContentTypeNorm() {
		return contentTypeNorm;
	}

	public void setContentTypeNorm(String contentTypeNorm) {
		this.contentTypeNorm = contentTypeNorm;
	}

	public String getContentLanguage() {
		return contentLanguage;
	}

	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}

	public String getCrawlDate() {
		return crawlDate;
	}

	public void setCrawlDate(String crawlDate) {
		this.crawlDate = crawlDate;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getWaybackDate() {
		return waybackDate;
	}

	public void setWaybackDate(String waybackDate) {
		this.waybackDate = waybackDate;
	}
	
	
}
