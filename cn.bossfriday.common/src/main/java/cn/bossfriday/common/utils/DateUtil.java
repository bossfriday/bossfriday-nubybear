package cn.bossfriday.common.utils;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static final String DEFAULT_DATE_FORMAT_MILL = "yyyyMMddHHmmssSSS";
	public static final String DEFAULT_DATE_FORMAT = "yyyyMMddHHmmss";
	public static final String DEFAULT_DATE_HYPHEN_FORMAT = "yyyyMMdd";
	public static final String DEFAULT_DATETIME_HYPHEN_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/***
	 * date2Str
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String date2Str(Date date, String pattern) {
		if (date == null)
			return null;
		if (StringUtils.isEmpty(pattern))
			pattern = DEFAULT_DATETIME_HYPHEN_FORMAT;
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}

	/**
	 * str2Date
	 * 
	 * @param dateStr
	 * @param formatStr
	 * @return
	 */
	public static Date str2Date(String dateStr, String formatStr) {
		SimpleDateFormat sdformat = new SimpleDateFormat(formatStr);
		try {
			return sdformat.parse(dateStr);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * timestampToLong
	 * 
	 * @param timestamp
	 * @return
	 */
	public static long timestampToLong(Timestamp timestamp) {
		return timestamp.getTime();
	}
	
	/**
	 * longToTimestamp
	 * 
	 * @param time
	 * @return
	 */
	public static Timestamp longToTimestamp(long time) {
		return new Timestamp(time);
	}
}
