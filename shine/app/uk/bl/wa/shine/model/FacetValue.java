package uk.bl.wa.shine.model;

/**
 * @author kli
 * FacetValue belongs to a facet
 */

public class FacetValue {
	
	private String name;
	private String value;
	private Integer limit;

	public FacetValue(String name, String value) {
		this(name, value, 5);
	}
	
	public FacetValue(String name, String value, Integer limit) {
		super();
		this.name = name;
		this.value = value;
		this.limit = limit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
