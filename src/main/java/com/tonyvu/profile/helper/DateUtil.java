package com.tonyvu.profile.helper;

import java.util.Date;
import org.joda.time.DateTime;

public final class DateUtil {
	
	/**
	 * Get a date which is days ago from a certain date
	 * 
	 * @param date a point of time
	 * @param days number of days to minus from
	 * @return a date which is days ago
	 */
	public static Date getPastDate(Date date, int days) {
		Date pastDate = (new DateTime(date)).minusDays(days).toDate();
		return pastDate;
	}
	
	/**
	 * 
	 * Get a date which is days ahead from a certain date
	 * 
     * @param date a point of time
	 * @param days number of days to add
	 * @return a date which is days ahead
	 */
	public static Date getFutureDate(Date date, int days) {
		Date futureDate = (new DateTime(date)).plusDays(days).toDate();
		return futureDate;
	}

}
