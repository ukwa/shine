package uk.bl.wa.shine;

public class GraphData {

	private int year;
	private int data;
	private String name;
	
	public GraphData(int year, int data, String name) {
		this.year = year;
		this.data = data;
		this.name = name;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
