package cn.js.fan.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
	public static long SECOND = 1000;
	public static long MINUTE = 60000;
	public static long HOUR = 3600000;
	public static long DAY = HOUR * 24;
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DATE_FORMAT_CHINA = "yyyy年MM月dd日";
	public static final String MONTH_FORMAT = "yyyy-MM";
	public static final String YEAR_FORMAT = "yyyy";

	public DateUtil() {
	}

	/**
	 * 格式化为字符串
	 * 
	 * @param d
	 *            Date
	 * @param format
	 *            String
	 * @return String
	 */
	public static String format(java.util.Date d, String format) {
		if (d == null)
			return "";
		SimpleDateFormat myFormatter = new SimpleDateFormat(format);
		return myFormatter.format(d);
	}

	public static String formatDate(Date d, int format, Locale locale,
			TimeZone timeZone) {
		if (d == null)
			return "";
		DateFormat df = DateFormat.getDateInstance(format, locale);
		df.setTimeZone(timeZone);
		return df.format(d);
	}

	public static String formatDateTime(Date d, int format1, int format2,
			Locale locale, TimeZone timeZone) {
		if (d == null)
			return "";
		// System.out.println("d=" + d + " format1=" + format1 + " format2=" +
		// format2 + " locale=" + locale + " timezone=" + timeZone);
		DateFormat df = DateFormat
				.getDateTimeInstance(format1, format2, locale);
		df.setTimeZone(timeZone);
		return df.format(d);
	}

	public static long toLong(Date d) {
		if (d == null)
			return 0;
		else
			return d.getTime();
	}

	public static String toLongString(Date d) {
		return "" + toLong(d);
	}

	public static int getYear(Date date) {
		if (date == null)
			return 0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	/**
	 * 获取月份
	 * 
	 * @param date
	 *            Date
	 * @return int 从0开始
	 */
	public static int getMonth(Date date) {
		if (date == null)
			return -1;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MONTH);
	}

	public static int getDay(Date date) {
		if (date == null)
			return 0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 从15位的字符串从解析出时间
	 * 
	 * @param timeMillis
	 *            String
	 * @return Date
	 */
	public static Date parse(String timeMillis) {
		java.util.Date d = null;
		try {
			d = new java.util.Date(Long.parseLong(timeMillis.trim()));
		} catch (Exception e) {
			// System.out.println("parse:" + e.getMessage());
			// e.printStackTrace();
		}
		return d;
	}

	/**
	 * 从字符串解析出时间
	 * 
	 * @param format
	 *            String 格式，如："yy-MM-dd HH:mm:ss"
	 * @param time
	 *            String
	 * @return Date
	 */
	public static Date parse(String time, String format) {
		java.util.Date d = null;
		try {
			d = parse(time, format, java.util.Locale.CHINA);
		} catch (Exception e) {
			// 兼容格式：2005/05/06
			if (time.indexOf("/") != -1 && format.equals("yyyy-MM-dd")) {
				try {
					d = parse(time, "yyyy/MM/dd", java.util.Locale.CHINA);
				} catch (Exception ex) {
				}
			}
			// System.out.println("DateUtil.java parse:" + e.getMessage());
		}
		return d;
	}

	public static Date parse(String time, String format, Locale locale)
			throws Exception {
		if (time == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
		java.util.Date d = null;
		d = sdf.parse(time);
		return d;
	}

	public static String format(Calendar cal, String format) {
		if (cal == null)
			return "";
		SimpleDateFormat myFormatter = new SimpleDateFormat(format);
		return myFormatter.format(cal.getTime());
	}

	/**
	 * 加上天数
	 * 
	 * @param d
	 *            Date
	 * @param day
	 *            int 天数
	 * @return Calendar
	 */
	public static Calendar add(java.util.Date d, int day) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, day);
		return cal;
	}

	public static Date addDate(java.util.Date d, int day) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, day);
		return cal.getTime();
	}

	public static Date addHourDate(java.util.Date d, int h) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.HOUR, h);
		return cal.getTime();
	}

	public static Calendar addMonth(java.util.Date d, int n) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MONTH, n);
		return cal;
	}

	public static Date addMonthDate(java.util.Date d, int n) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MONTH, n);
		return cal.getTime();
	}

	public static Calendar addHour(java.util.Date d, int h) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.HOUR, h);
		return cal;
	}

	public static Date addMinuteDate(java.util.Date d, int m) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MINUTE, m);
		return cal.getTime();
	}

	public static Calendar addMinute(java.util.Date d, int m) {
		if (d == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MINUTE, m);
		return cal;
	}

	/**
	 * 比较两个时间的值
	 * 
	 * @param c1
	 *            Calendar
	 * @param c2
	 *            Calendar
	 * @return int 如果c1晚则为1，c2晚则为2，相等则为0，如果c1或c2为空则为-1
	 */
	public static int compare(Calendar c1, Calendar c2) {
		if (c1 == null || c2 == null)
			return -1;
		long r = c1.getTimeInMillis() - c2.getTimeInMillis();
		if (r > 0)
			return 1;
		else if (r == 0)
			return 0;
		else
			return 2;
	}

	/**
	 * 比较两个时间的值
	 * 
	 * @param c1
	 *            Date
	 * @param c2
	 *            Date
	 * @return int 如果c1晚则为1，c2晚则为2，相等则为0，如果c1或c2为空则为-1
	 * @return int
	 */
	public static int compare(Date c1, Date c2) {
		if (c1 == null || c2 == null)
			return -1;
		long r = c1.getTime() - c2.getTime();
		// System.out.println("compare r=" + r);
		if (r > 0)
			return 1;
		else if (r == 0)
			return 0;
		else
			return 2;
	}

	/**
	 * 比较两个日期是否为同一天
	 * 
	 * @param c1
	 *            Calendar
	 * @param c2
	 *            Calendar
	 * @return boolean
	 */
	public static boolean isSameDay(Calendar c1, Calendar c2) {
		if (c1 == null || c2 == null)
			return false;
		if (c1.get(c1.YEAR) == c2.get(c1.YEAR)
				&& c1.get(c1.MONTH) == c2.get(c1.MONTH)
				&& c1.get(c1.DAY_OF_MONTH) == c2.get(c1.DAY_OF_MONTH))
			return true;
		else
			return false;
	}

	/**
	 * 比较两个日期是否为同一天
	 * 
	 * @param d1
	 *            Date
	 * @param d2
	 *            Date
	 * @return boolean
	 */
	public static boolean isSameDay(Date d1, Date d2) {
		if (d1 == null || d2 == null)
			return false;
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
				&& c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
				&& c1.get(Calendar.DAY_OF_MONTH) == c2
						.get(Calendar.DAY_OF_MONTH))
			return true;
		else
			return false;
	}

	/**
	 * 计算两个日期相差的天数 c1-c2
	 * 
	 * @param c1
	 *            Calendar
	 * @param c2
	 *            Calendar
	 * @return int 返回天数，不足24小时的为0
	 */
	public static int datediff(Calendar c1, Calendar c2) {
		if (c1 == null || c2 == null)
			return -1;
		long r = c1.getTimeInMillis() - c2.getTimeInMillis();
		r = r / (24 * 60 * 60 * 1000);
		return (int) r;
	}

	/**
	 * 计算两个日期相差的天数 d1-d2
	 * 
	 * @param d1 Date
	 * @param d2 Date
	 * @return int 返回天数，不足24小时的为0
	 */
	public static int datediff(Date d1, Date d2) {
		if (d1 == null || d2 == null)
			return -1;
		long r = d1.getTime() - d2.getTime();
		r = r / (24 * 60 * 60 * 1000);
		return (int) r;
	}

	/**
	 * 计算两个日期相隔的天数，而datediff则为相差的天数
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int diff(Date d1, Date d2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return (int)(df.parse(df.format(d1)).getTime()-df.parse(df.format(d2)).getTime()) / (24 * 60 * 60 * 1000);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -65536;
	}

	/**
	 * 取得相差的分钟数
	 * 
	 * @param c1
	 *            Date
	 * @param c2
	 *            Date
	 * @return int
	 */
	public static int datediffMinute(Date c1, Date c2) {
		if (c1 == null || c2 == null)
			return 0;
		double r = c1.getTime() - c2.getTime();
		r = r / 60000;
		return (int) r;
	}

	public static int datediffMinute(Calendar c1, Calendar c2) {
		if (c1 == null || c2 == null)
			return 0;
		double r = c1.getTimeInMillis() - c2.getTimeInMillis();
		r = r / 60000;
		return (int) r;
	}

	public static int datediffHour(Date c1, Date c2) {
		if (c1 == null || c2 == null)
			return 0;
		double r = c1.getTime() - c2.getTime();
		r = r / (60 * 1000 * 60);
		return (int) r;
	}

	public static int datediffHour(Calendar c1, Calendar c2) {
		if (c1 == null || c2 == null)
			return 0;
		double r = c1.getTimeInMillis() - c2.getTimeInMillis();
		r = r / (60 * 1000 * 60);
		return (int) r;
	}

	/**
	 * 取得时间差中的天数、小时、分钟、秒
	 * 
	 * @param d1
	 *            Date
	 * @param d2
	 *            Date
	 * @return int[]
	 */
	public static int[] dateDiffDHMS(Date d1, Date d2) {
		int diffDay = datediff(d1, d2);

		int h1 = d1.getHours();
		int h2 = d2.getHours();
		int m1 = d1.getMinutes();
		int m2 = d2.getMinutes();
		int s1 = d1.getSeconds();
		int s2 = d2.getSeconds();

		int s = s1 - s2;
		int m = m1 - m2;
		if (s < 0) {
			s += 60;
			m -= 1;
		}
		int h = h1 - h2;
		if (m < 0) {
			m += 60;
			h -= 1;
		}
		if (h < 0) {
			h += 24;
		}
		// System.out.println("DateUtil.java diffDay=" + diffDay);
		int[] r = { diffDay, h, m, s };
		return r;
	}

	/**
	 * 取得月份中的天数
	 * 
	 * @param year
	 *            int
	 * @param month
	 *            int 从0开始
	 * @return int
	 */
	public static int getDayCount(int year, int month) {
		int daysInMonth[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		// 测试选择的年份是否是润年？
		if (1 == month)
			return ((0 == year % 4) && (0 != (year % 100)))
					|| (0 == year % 400) ? 29 : 28;
		else
			return daysInMonth[month];
	}

	/**
	 * 根据年月日，获取时间
	 * 
	 * @param year
	 *            int
	 * @param month
	 *            int 从0开始
	 * @param day
	 *            int
	 * @return Date
	 */
	public static Date getDate(int year, int month, int day) {
		String str = year + "-" + (month + 1) + "-" + day;
		return DateUtil.parse(str, "yyyy-MM-dd");

	}

	/**
	 * 取得一年的天数
	 * 
	 * @param year
	 *            int
	 * @return int
	 */
	public static int getDaysOfYear(int year) {
		GregorianCalendar now = new GregorianCalendar();
		return now.isLeapYear(year) ? 366 : 365;
	}

	/**
	 * 取得当天从0时开始，至明天0时结束时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfToday() {
		Date[] ary = new Date[2];

		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		ary[0] = calendar.getTime();
		ary[1] = DateUtil.addDate(ary[0], 1);
		return ary;
	}

	public static Date[] getDateSectOfYestoday() {
		Date[] ary = new Date[2];

		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		ary[0] = DateUtil.addDate(calendar.getTime(), -1);
		ary[1] = calendar.getTime();
		return ary;
	}

	/**
	 * 取得本周一0时至本周日0时的时间段，当本日为周六时，计算的为上周的周一
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfCurWeek() {
		// 在java中从周六至周日为一星期，而我们的习惯是周一至周日为一星期，所以需转换
		Date[] ary = new Date[2];
		Calendar calendar = Calendar.getInstance();
		// 取得周一
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;
		dayOfWeek = (dayOfWeek == -1) ? 6 : dayOfWeek;// 如果不作此行的判断，则当本日为周六时，计算的为下周的周一
		calendar.add(Calendar.DAY_OF_MONTH, (-1) * dayOfWeek);
		ary[0] = calendar.getTime(); // 周一从0时开始
		ary[1] = DateUtil.addDate(calendar.getTime(), 7); // 至周日0时结束
		return ary;
	}

	/**
	 * 取得下周的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfNextWeek() {
		// 在java中从周六至周日为一星期，而我们的习惯是周一至周日为一星期，所以需转换
		Date[] ary = new Date[2];
		Calendar calendar = Calendar.getInstance();
		// 取得周一
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;
		dayOfWeek = (dayOfWeek == -1) ? 6 : dayOfWeek;// 如果不作此行的判断，则当本日为周六时，计算的为下周的周一
		calendar.add(Calendar.DAY_OF_MONTH, (-1) * dayOfWeek);
		ary[0] = DateUtil.addDate(calendar.getTime(), 7); // 周一从0时开始
		ary[1] = DateUtil.addDate(ary[0], 7); // 至周日0时结束
		return ary;
	}

	/**
	 * 取得上周一0时至本周日0时的时间段，当本日为周六时，计算的为上上周的周一
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfLastWeek() {
		// 在java中从周六至周日为一星期，而我们的习惯是周一至周日为一星期，所以需转换
		Date[] ary = new Date[2];
		Calendar calendar = Calendar.getInstance();
		// 取得周一
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;
		dayOfWeek = (dayOfWeek == -1) ? 6 : dayOfWeek;// 如果不作此行的判断，则当本日为周六时，计算的为下周的周一
		calendar.add(Calendar.DAY_OF_MONTH, (-1) * dayOfWeek);

		ary[0] = DateUtil.addDate(calendar.getTime(), -7);
		ary[1] = DateUtil.addDate(ary[0], 7); // 至周日0时结束
		return ary;
	}

	/**
	 * 取得本月1日0时至下月1日0时的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfCurMonth() {
		Date[] ary = new Date[2];
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		int m = calendar.get(Calendar.MONTH);
		// 取得1号
		calendar.set(y, m, 1, 0, 0, 0);
		ary[0] = calendar.getTime();
		ary[1] = DateUtil.addMonthDate(ary[0], 1);
		return ary;
	}

	/**
	 * 取得下个月1日0时至再下一个月1日0时的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfNextMonth() {
		Date[] ary = new Date[2];
		Calendar calendar = Calendar.getInstance();
		calendar = DateUtil.addMonth(calendar.getTime(), 1);
		int y = calendar.get(Calendar.YEAR);
		int m = calendar.get(Calendar.MONTH);
		// 取得1号
		calendar.set(y, m, 1, 0, 0, 0);
		ary[0] = calendar.getTime();
		ary[1] = DateUtil.addMonthDate(ary[0], 1);
		return ary;
	}

	/**
	 * 取得上月1日0时至本月1日0时的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfLastMonth() {
		Date[] ary = new Date[2];
		Calendar calendar = Calendar.getInstance();
		int y = calendar.get(Calendar.YEAR);
		int m = calendar.get(Calendar.MONTH);
		// 取得1号
		calendar.set(y, m, 1, 0, 0, 0);
		ary[1] = calendar.getTime();
		calendar.add(Calendar.MONTH, -1);
		ary[0] = calendar.getTime();
		return ary;
	}

	/**
	 * 取得某个月的时间段
	 * 
	 * @param year
	 *            int
	 * @param month
	 *            int 从0开始
	 * @return Date[]
	 */
	public static Date[] getDateSectOfMonth(int year, int month) {
		Date[] ary = new Date[2];
		Calendar calendar = Calendar.getInstance();
		// 取得1号
		calendar.set(year, month, 1, 0, 0, 0);
		ary[0] = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		ary[1] = calendar.getTime();
		return ary;
	}

	/**
	 * 获得本季度的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfQuarter() {
		Date[] ary = new Date[2];

		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH) + 1;
		int array[][] = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } };
		int season = 1;
		if (month >= 1 && month <= 3) {
			season = 1;
		}
		if (month >= 4 && month <= 6) {
			season = 2;
		}
		if (month >= 7 && month <= 9) {
			season = 3;
		}
		if (month >= 10 && month <= 12) {
			season = 4;
		}
		int startMonth = array[season - 1][0];

		int y = cal.get(Calendar.YEAR);
		cal.set(y, startMonth - 1, 1, 0, 0, 0);
		ary[0] = cal.getTime();
		ary[1] = DateUtil.addMonthDate(ary[0], 3);
		return ary;
	}

	/**
	 * 取得本年的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfCurYear() {
		Date[] ary = new Date[2];

		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);

		cal.set(y, 0, 1, 0, 0, 0);
		ary[0] = cal.getTime();
		cal.set(y + 1, 0, 1, 0, 0, 0);
		ary[1] = cal.getTime();
		return ary;
	}

	/**
	 * 取得去年的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfLastYear() {
		Date[] ary = new Date[2];
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);

		cal.set(y - 1, 0, 1, 0, 0, 0);
		ary[0] = cal.getTime();
		cal.set(y, 0, 1, 0, 0, 0);
		ary[1] = cal.getTime();
		return ary;
	}

	/**
	 * 取得某年的时间段
	 * 
	 * @param year
	 *            int
	 * @return Date[]
	 */
	public static Date[] getDateSectOfYear(int year) {
		Date[] ary = new Date[2];
		Calendar cal = Calendar.getInstance();
		cal.set(year, 0, 1, 0, 0, 0);
		ary[0] = cal.getTime();
		cal.set(year + 1, 0, 1, 0, 0, 0);
		ary[1] = cal.getTime();
		return ary;
	}

	/**
	 * 取得前年的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfLastLastYear() {
		Date[] ary = new Date[2];
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);

		cal.set(y - 2, 0, 1, 0, 0, 0);
		ary[0] = cal.getTime();
		cal.set(y - 1, 0, 1, 0, 0, 0);
		ary[1] = cal.getTime();
		return ary;
	}

	/**
	 * 取得明年的时间段
	 * 
	 * @return Date[]
	 */
	public static Date[] getDateSectOfNextYear() {
		Date[] ary = new Date[2];
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);

		cal.set(y + 1, 0, 1, 0, 0, 0);
		ary[0] = cal.getTime();
		cal.set(y + 2, 0, 1, 0, 0, 0);
		ary[1] = cal.getTime();
		return ary;
	}
	/**
	 * 转化时间
	 * 1分钟内显示秒
	 *小于10秒刚刚
	 *10到60具体多少秒
	 *一分钟到1小时，显示分钟
     *1小时到24小时显示小时
     *24到48小时，显示昨天
	 *48到72小时，显示前天
	 * @param createTime
	 * @return
	 * @throws ParseException
	 */
	public static String parseDateTime(String createTime) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer sb = null;
		sb = new StringBuffer();
		java.util.Date date = df.parse(createTime);
		Calendar nowCalendar = Calendar.getInstance();
		Calendar myCalendar = Calendar.getInstance();
		myCalendar.setTime(date);
		if (nowCalendar.get(Calendar.YEAR) == myCalendar.get(Calendar.YEAR)) {
			long l = nowCalendar.getTimeInMillis()
					- myCalendar.getTimeInMillis();
			long day = nowCalendar.get(Calendar.DAY_OF_YEAR) - myCalendar.get(Calendar.DAY_OF_YEAR);
			long hour = (l / (60 * 60 * 1000) - day * 24);
			long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
			long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
			if (day > 0) {// 昨天 前天
				if (day == 1) {
					String hourMin =format(parse(myCalendar.get(Calendar.HOUR_OF_DAY) + ":" + myCalendar.get(Calendar.MINUTE), "HH:mm"),"HH:mm");
					sb.append("昨天").append(hourMin);
				} else if (day == 2) {
					String hourMin =format(parse(myCalendar.get(Calendar.HOUR_OF_DAY) + ":" + myCalendar.get(Calendar.MINUTE), "HH:mm"),"HH:mm");
					sb.append("前天").append(hourMin);
				} else {
					String monDay =format(parse((myCalendar.get(Calendar.MONTH) + 1)+ "-" + myCalendar.get(Calendar.DAY_OF_MONTH) + " "
							+ myCalendar.get(Calendar.HOUR_OF_DAY)
							+ ":" 
							+ myCalendar.get(Calendar.MINUTE), "MM-dd HH:mm"),"MM月dd日 HH:mm");
					sb.append(monDay);
				}
			} else {
				if (hour > 0 && hour < 24) {
					sb.append(hour).append("小时前");
				} else {
					if (min > 0 && min < 60) {
						sb.append(min).append("分钟前");
					} else {
						if (s >= 0 && s <= 10) {
							sb.append("刚刚");
						} else if (s > 10 && s <= 60) {
							sb.append(s).append("秒前");
						} else {
							sb.append(createTime);
						}
					}
				}
			}
		} else {
			sb.append(createTime);
		}

		return sb.toString();

	}
	/**
	 * 转化时间
	 * 1分钟内显示秒
	 *小于10秒刚刚
	 *10到60具体多少秒
	 *一分钟到1小时，显示分钟
     *1小时到24小时显示小时
     *24到48小时，显示昨天
	 *48到72小时，显示前天
	 * @param createTime
	 * @return
	 * @throws ParseException
	 */
	public static String parseDate(String createTime) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer sb = null;
		sb = new StringBuffer();
		java.util.Date date = df.parse(createTime);
		Calendar nowCalendar = Calendar.getInstance();
		Calendar myCalendar = Calendar.getInstance();
		myCalendar.setTime(date);
		if (nowCalendar.get(Calendar.YEAR) == myCalendar.get(Calendar.YEAR)) {
			long day = nowCalendar.get(Calendar.DAY_OF_YEAR) - myCalendar.get(Calendar.DAY_OF_YEAR);
			if (day > 0) {// 昨天 前天
				if (day == 1) {
					sb.append("昨天");
				} else if (day == 2) {
					sb.append("前天");
				} else {
					String monDay =format(parse(myCalendar.get(Calendar.YEAR) + "-" +(myCalendar.get(Calendar.MONTH) + 1)+ "-" + myCalendar.get(Calendar.DAY_OF_MONTH), DATE_FORMAT),DATE_FORMAT_CHINA);
					sb.append(monDay);
				}
			} else {
				sb.append("今天");
			}
		} else {
			sb.append(format(date, DATE_FORMAT_CHINA));
		}

		return sb.toString();

	}
	/**
	 *  获取当前时间所在年的周数
	 * @param date
	 * @return
	 */
    public static int getWeekOfYear(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(7);
        c.setTime(date);
 
        return c.get(Calendar.WEEK_OF_YEAR);
    }
    /**
     * 获取某年的第几周的开始日期
     * @param year
     * @param week
     * @return
     */
    public static Date getFirstDayOfWeek(int year, int week) {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);
 
        Calendar cal = (GregorianCalendar) c.clone();
        cal.add(Calendar.DATE, (week-1) * 7);
 
        return getFirstDayOfWeek(cal.getTime());
    }
    /**
 	 * 获取某年的第几周的结束日期
 	 */
    public static Date getLastDayOfWeek(int year, int week) {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, Calendar.JANUARY);
        c.set(Calendar.DATE, 1);
 
        Calendar cal = (GregorianCalendar) c.clone();
        cal.add(Calendar.DATE, (week-1) * 7);
 
        return getLastDayOfWeek(cal.getTime());
    }
 	/**
 	 * 获取当前时间所在周的开始日期
 	 * @param date
 	 * @return
 	 */
    public static Date getFirstDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
        return c.getTime();
    }
    /**
     * 获取当前时间所在周的结束日期
     */ 
    public static Date getLastDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6); // Sunday
        return c.getTime();
    }
    /**
     * 获取当前时间所在年的最大周数
     */ 
    public static int getMaxWeekNumOfYear(int year) {
        Calendar c = new GregorianCalendar();
        c.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
 
        return getWeekOfYear(c.getTime());
    }
    
    /**
     * 取得星期几
     * @param date
     * @return
     */
	public static String getDayOfWeek(Date date) {
		// 星期日为一周的第一天 SUN MON TUE WED THU FRI SAT 
		// DAY_OF_WEEK返回值       1 	2 	3 	4 	5 	6 	7 
		
		String[] weeks = {"日","一","二","三","四","五","六"};
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
		return weeks[week_index];
	}
}
