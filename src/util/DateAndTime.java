package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

public class DateAndTime {
	private static Logger logger = Logger.getLogger(DateAndTime.class.getName());
	
	public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	public static final SimpleDateFormat SIMPLE_DATE_FORMAT_US = new SimpleDateFormat("dd_MM_yyyy");
	public static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	public static final SimpleDateFormat SIMPLE_DATE_TIME_AMPM_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
	public static final SimpleDateFormat SIMPLE_SQL_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public String getDateFromLongUnderScore(long data) {
		String date = "N/A";
		try {
			if (data > 0) {
				date = SIMPLE_DATE_FORMAT_US.format(new Date(data));
				if (date == null || date.length() == 0 || date.equals("1970-01-01")) {
					date = "N/A";
				}
			}
		} catch (RuntimeException e) {
			logger.fatal("RuntimeException", e);
		}
		return date;
	}
	

}
