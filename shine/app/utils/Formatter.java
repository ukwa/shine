package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatter {
  
    public static String formatDate(Object dateText) {
    	if (dateText != null) {
			String dateValue = dateText.toString();
			DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
			Date date;
			try {
				date = formatter.parse(dateValue);
			} catch (ParseException e) {
				e.printStackTrace();
				return "";
			}
			String newDate = new java.text.SimpleDateFormat("MMM dd yyyy HH:mm:ss z").format(date);
			return newDate.toString();
    	}
		return "";
	}
}