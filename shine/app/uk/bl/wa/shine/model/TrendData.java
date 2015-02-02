/**
 * 
 */
package uk.bl.wa.shine.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author andy
 *
 */
public class TrendData {
	
	private int start;
	private int end;
	private int steps;
	private int size;
	private long hits[];

	/**
	 * 
	 * @param start year
	 * @param end end year
	 * @param steps number of data points per year
	 */
	public TrendData(int start, int end, int steps) {
		this.start = start;
		this.end = end;
		this.steps = steps;
		this.size = steps*((end - start) + 1);
		this.hits = new long[size];
	}
	
	/**
	 * 
	 * @param year
	 * @param hits
	 */
	public void setHitsForYear(int year, long hits) {
		int index = year - start;
		if( index < 0 || index >= size ) return;
		this.hits[index] = hits;
	}
	
	/**
	 * 
	 * @return a map for generating plots
	 */
	public Map<Date,Long> getHits() {
		Map<Date,Long> hitmap = new LinkedHashMap<Date,Long>();
		boolean dataBegun = false;
		for( int i = 0; i < size; i++ ) {
			// Set up the date:
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(0);
			cal.set(Calendar.YEAR, start + i/steps);
			cal.set(Calendar.MONTH, (int)(12.0*i%steps));
			// Skip adding data until hit count goes > 0:
			if( hits[i] > 0 )
				dataBegun = true;
			// Add it to the map:
			if( !dataBegun ) {
				hitmap.put( cal.getTime(), null);
			} else {
				hitmap.put( cal.getTime(), hits[i]);
			}
		}
		return hitmap;
	}
	
}
