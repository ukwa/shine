package uk.bl.wa.shine.model;

import java.text.DecimalFormat;

/**
 * @author kli
 * FacetValue belongs to a facet
 */

public class FacetValue {
	
	private String name;
	private String value;
	private Integer limit;
	private Integer maxLimit;

	public FacetValue(String name, String value) {
		this(name, value, 5, 10);
	}
	
	public FacetValue(String name, String value, Integer limit, Integer maxLimit) {
		super();
		this.name = name;
		this.value = value;
		this.limit = limit;
		this.maxLimit = maxLimit;
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

	public Integer getMaxLimit() {
		return maxLimit;
	}

	public void setMaxLimit(Integer maxLimit) {
		this.maxLimit = maxLimit;
	}
	
	public static DecimalFormat integerDecimalFormat = new DecimalFormat("#,###");
	
}
