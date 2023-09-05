package com.redmoon.oa.oacalendar;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import org.apache.commons.jcs3.access.exception.CacheException;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;

@Api(tags = "工作日历")
public class OACalendarDb extends QObjectDb {
	public static int DATE_TYPE_WORK = 0; // 工作日
	public static int OAWORK_WORK = 0;  //oa_work_date工作日
	public static int OAWORK_HOLIDAY = 1;  //oa_work_date休息日
	public static int DATE_TYPE_SAT_SUN = 1; // 周六、日
	public static int DATE_TYPE_HOLIDAY = 2; // 假日
	public static int DATE_TYPE_WORK_DATE_ID = 0; // 假日


	public OACalendarDb() {
		super();
	}

	public static int getDayOfWeek(int year, int month, int date) {
		Calendar now = Calendar.getInstance();
		now.set(year, month, date);
		return now.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 取得相应年份的所有假日（有可能含周六、周日，因为在设置时有可能将周六、周日设为了假日）
	 *
	 * @param beginYear
	 * @param endYear
	 * @return
	 */
	public Vector getHolidays(int beginYear, int endYear) {
		java.util.Date beginDate = DateUtil.getDate(beginYear, 0, 1);
		String b = DateUtil.format(beginDate, "yyyy-MM-dd");
		java.util.Date endDate = DateUtil.getDate(endYear, 11, 31);
		String e = DateUtil.format(endDate, "yyyy-MM-dd");
		String sql = "SELECT oa_date FROM oa_calendar where date_type="
				+ DATE_TYPE_HOLIDAY + " and oa_date>="
				+ SQLFilter.getDateStr(b, "yyyy-MM-dd") + " and oa_date<="
				+ SQLFilter.getDateStr(e, "yyyy-MM-dd");
		// LogUtil.getLog(getClass()).info("sql=" + sql);
		return list(sql);
	}

	public static boolean isWorkday(Date d) {
		if (d==null)
			return false;
		OACalendarDb cdb = new OACalendarDb();
        cdb = (OACalendarDb)cdb.getQObjectDb(d);
        if (cdb==null) {
			LogUtil.getLog(OACalendarDb.class).error("isWorkday: 工作日历未初始化");
        	return false;
		}
        else {
			return cdb.getInt("date_type") == DATE_TYPE_WORK;
		}
	}

	/**
	 * 取得星期六是工作日的日期，用于计划甘特图
	 *
	 * @param beginYear
	 * @param endYear
	 * @return
	 */
	public Vector getSatdaysIsWorking(int beginYear, int endYear) {
		java.util.Date beginDate = DateUtil.getDate(beginYear, 0, 1);
		String b = DateUtil.format(beginDate, "yyyy-MM-dd");
		java.util.Date endDate = DateUtil.getDate(endYear, 11, 31);
		String e = DateUtil.format(endDate, "yyyy-MM-dd");
		String sql = "SELECT oa_date FROM oa_calendar where date_type="
				+ DATE_TYPE_WORK + " and oa_date>="
				+ SQLFilter.getDateStr(b, "yyyy-MM-dd") + " and oa_date<="
				+ SQLFilter.getDateStr(e, "yyyy-MM-dd") + " and week_day="
				+ Calendar.SATURDAY;
		// LogUtil.getLog(getClass()).info("sql=" + sql);
		return list(sql);
	}

	/**
	 * 取得星期天是工作日的日期，用于计划甘特图
	 *
	 * @param beginYear
	 * @param endYear
	 * @return
	 */
	public Vector getSundaysIsWorking(int beginYear, int endYear) {
		java.util.Date beginDate = DateUtil.getDate(beginYear, 0, 1);
		String b = DateUtil.format(beginDate, "yyyy-MM-dd");
		java.util.Date endDate = DateUtil.getDate(endYear, 11, 31);
		String e = DateUtil.format(endDate, "yyyy-MM-dd");
		String sql = "SELECT oa_date FROM oa_calendar where date_type="
				+ DATE_TYPE_WORK + " and oa_date>="
				+ SQLFilter.getDateStr(b, "yyyy-MM-dd") + " and oa_date<="
				+ SQLFilter.getDateStr(e, "yyyy-MM-dd") + " and week_day="
				+ Calendar.SUNDAY;
		// LogUtil.getLog(getClass()).info("sql=" + sql);
		return list(sql);
	}

	/**
	 *
	 * @param year
	 * @param month
	 *            从0开始
	 * @return
	 */
	public static int[][] initMonthlyCalendar(int year, int month) {
		int count = 0;
		int m = getDayOfWeek(year, month, 1) - 1; // 该月第一天为星期几
		int n = DateUtil.getDayCount(year, month); // 该月的天数
		int[][] monthlyCalendar = new int[6][7]; // 月历数组
		for (int i = 0; i < monthlyCalendar.length; i++) {
			for (int j = 0; j < monthlyCalendar[i].length; j++) {
				if (i == 0 && j < m) {
					monthlyCalendar[i][j] = -1;
					continue;
				} else if (count > n - 1) {
					monthlyCalendar[i][j] = -1;
					continue;
				} else {
					monthlyCalendar[i][j] = count + 1;
				}
				count++;
			}
		}
		return monthlyCalendar;
	}

	public String getTheCalendar(String currentDate, String unitCode, String deptCode) throws SQLException{
		String sql = "select oa_date from oa_calendar where dept_user_code = "+ StrUtil.sqlstr(deptCode) +" and unit_code = " + StrUtil.sqlstr(unitCode)+" and oa_date = "+StrUtil.sqlstr(currentDate);
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		ResultRecord rr= null;
		String oa_date = "";
		if(ri.hasNext()){
			rr = (ResultRecord) ri.next();
			oa_date = rr.getString(1);
		}
		return oa_date;
	}
	/**
	 * 显示日历
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	public String renderMonthlyCalendar(int year, int month, String deptCode,int deptType, String unitCode) {
		int[][] monthlyCalendar = initMonthlyCalendar(year, month);
		StringBuffer html = new StringBuffer();
		html
				.append("<table border='1' cellspacing='0' cellpadding='0' width='90%' height='90%' class='tabStyle_1'>");
		html.append("<tr>");
		html
				.append("<td class='tabStyle_1_title' bgcolor='lightskyblue' colspan='7' style='font-family:黑体;font-weight:bold;'>");
		html.append((month + 1) + "  月");
		html.append("</td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=\"red\">日</font></td>");
		html.append("<td>一</td>");
		html.append("<td>二</td>");
		html.append("<td>三</td>");
		html.append("<td>四</td>");
		html.append("<td>五</td>");
		html.append("<td><font color=\"red\">六</font></td>");
		html.append("</tr>");
		try {
			RMCache.getInstance().clear();
		} catch (CacheException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		for (int i = 0; i < monthlyCalendar.length; i++) {
			html.append("<tr>");
			for (int j = 0; j < monthlyCalendar[i].length; j++) {
				if (monthlyCalendar[i][j] == -1) {
					html.append("<td>&nbsp;</td>");
					continue;
				} else {
					String strCurrentDate = year + "-" + (month + 1) + "-"
							+ monthlyCalendar[i][j];
					java.util.Date currentDate = DateUtil.parse(strCurrentDate,
							"yyyy-MM-dd");
					OACalendarDb oacdb = new OACalendarDb();
					oacdb = (OACalendarDb) oacdb.getQObjectDb(currentDate);

					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
					String oa_date =sdf.format(currentDate);
					String newDeptCode = searchParentCode(unitCode,deptCode,deptType,year);
					String sql = "select * from oa_calendar where dept_user_code = "+ StrUtil.sqlstr(newDeptCode) +" and unit_code = " + StrUtil.sqlstr(unitCode)+" and oa_date = "+ StrUtil.sqlstr(oa_date);
					JdbcTemplate jt = new JdbcTemplate();
					ResultIterator ri ;
					int dateType = 0;
					String work_time_begin_a = "";
					String work_time_end_a = "";
					String work_time_begin_b = "";
					String work_time_end_b = "";
					String work_time_begin_c = "";
					String work_time_end_c = "";
					String work_time_begin_d = "";
					String work_time_end_d = "";
					String work_time_begin_e = "";
					String work_time_end_e = "";
					try {
						ri = jt.executeQuery(sql);
						ResultRecord rr= null;
						while(ri.hasNext()){
							rr = (ResultRecord) ri.next();
							dateType = rr.getInt("date_type");
							work_time_begin_a = rr.getString("work_time_begin_a");
							work_time_end_a = rr.getString("work_time_end_a");
							work_time_begin_b = rr.getString("work_time_begin_b");
							work_time_end_b = rr.getString("work_time_end_b");
							work_time_begin_c = rr.getString("work_time_begin_c");
							work_time_end_c = rr.getString("work_time_end_c");
							work_time_begin_d = rr.getString("work_time_begin_d");
							work_time_end_d = rr.getString("work_time_end_d");
							work_time_begin_e = rr.getString("work_time_begin_e");
							work_time_end_e = rr.getString("work_time_end_e");
						}
					} catch (SQLException e) {
						LogUtil.getLog(getClass()).error(e);
					}

					if (oacdb != null) {
						if (dateType == DATE_TYPE_SAT_SUN) {
							html
									.append("<td class='holiday' bgcolor='lightcoral' title=\"休息日\"><a onclick=\"initModifydate("
											+ "'"
											+ strCurrentDate
											+ "'"
											+ ",'"
											+ work_time_begin_a
											+ "','"
											+ work_time_end_a
											+ "','"
											+ work_time_begin_b
											+ "','"
											+ work_time_end_b
											+ "','"
											+ work_time_begin_c
											+ "','"
											+ work_time_end_c
											+ "','"
											+ work_time_begin_d
											+ "','"
											+ work_time_end_d
											+ "','"
											+ work_time_begin_e
											+ "','"
											+ work_time_end_e
											+ "','"
											+ dateType
											+ "','"
											+ (j + 1) + "'" + ")\">");
						} else if (dateType == DATE_TYPE_HOLIDAY) {
							if (j == 0 || j == 6) {
								html
										.append("<td class='holiday' bgcolor='lightcoral' title=\"休息日\"><a onclick=\"initModifydate("
												+ "'"
												+ strCurrentDate
												+ "'"
												+ ",'"
												+ work_time_begin_a
												+ "','"
												+ work_time_end_a
												+ "','"
												+ work_time_begin_b
												+ "','"
												+ work_time_end_b
												+ "','"
												+work_time_begin_c
												+ "','"
												+ work_time_end_c
												+ "','"
												+ work_time_begin_d
												+ "','"
												+ work_time_end_d
												+ "','"
												+ work_time_begin_e
												+ "','"
												+ work_time_end_e
												+ "','"
												+

												DATE_TYPE_HOLIDAY
												+ "','"
												+ (j + 1) + "'" + ")\">");
							} else {
								html
										.append("<td bgcolor='greenyellow' title=\"休息日\"><a onclick=\"initModifydate("
												+ "'"
												+ strCurrentDate
												+ "'"
												+ ",'"
												+ work_time_begin_a
												+ "','"
												+ work_time_end_a
												+ "','"
												+ work_time_begin_b
												+ "','"
												+ work_time_end_b
												+ "','"
												+work_time_begin_c
												+ "','"
												+ work_time_end_c
												+ "','"
												+ work_time_begin_d
												+ "','"
												+ work_time_end_d
												+ "','"
												+ work_time_begin_e
												+ "','"
												+ work_time_end_e
												+ "','"
												+

												DATE_TYPE_HOLIDAY
												+ "','"
												+ (j + 1) + "'" + ")\">");
							}
						} else {
							String title = "工作日" + "&#10";
							title += "时间1："
									+ work_time_begin_a
									+ "~"
									+ work_time_end_a
									+ "&#10";
							// 20091105
							title += "时间2："
									+ work_time_begin_b
									+ "~"
									+ work_time_end_b
									+ "&#10";
							title += "时间3："
									+ work_time_begin_c
									+ "~"
									+ work_time_end_c
									+ "&#10";
							title += "时间4："
								+ work_time_begin_d
								+ "~"
								+ work_time_end_d
								+ "&#10";
							title += "时间5："
								+ work_time_begin_e
								+ "~"
								+ work_time_end_e
								+ "&#10";
							html.append("<td title=\""
									+ title
									+ "\"><a onclick=\"initModifydate("
									+ "'"
									+ strCurrentDate
									+ "'"
									+ ",'"
									+ work_time_begin_a
									+ "','"
									+ work_time_end_a
									+ "','"
									+ work_time_begin_b
									+ "','"
									+ work_time_end_b
									+ "','"
									+
									// 20091105
									work_time_begin_c
									+ "','"
									+ work_time_end_c
									+ "','"
									+ work_time_begin_d
									+ "','"
									+ work_time_end_d
									+ "','"
									+work_time_begin_e
									+ "','"
									+ work_time_end_e
									+ "','"
									+ DATE_TYPE_WORK + "','" + (j + 1)
									+ "'" + ")\">");
						}
					} else {
						// 如果尚示初使化
						html.append("<td bgcolor='#cccccc'><a>");
					}
					html.append(monthlyCalendar[i][j] + "</a></td>");
				}
			}
			html.append("</tr>");
		}
		html.append("</table>");
		return html.toString();
	}

	/**
	 * 显示日历
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	public String renderMonthlyCalendar(int year, int month) {
		int[][] monthlyCalendar = initMonthlyCalendar(year, month);
		StringBuffer html = new StringBuffer();
		html
				.append("<table border='1' cellspacing='0' cellpadding='0' width='90%' height='90%' class='tabStyle_1'>");
		html.append("<tr>");
		html
				.append("<td class='tabStyle_1_title' bgcolor='lightskyblue' colspan='7' style='font-family:黑体;font-weight:bold;'>");
		html.append((month + 1) + "  月");
		html.append("</td>");
		html.append("</tr>");
		html.append("<tr>");
		html.append("<td><font color=\"red\">日</font></td>");
		html.append("<td>一</td>");
		html.append("<td>二</td>");
		html.append("<td>三</td>");
		html.append("<td>四</td>");
		html.append("<td>五</td>");
		html.append("<td><font color=\"red\">六</font></td>");
		html.append("</tr>");
		for (int i = 0; i < monthlyCalendar.length; i++) {
			html.append("<tr>");
			for (int j = 0; j < monthlyCalendar[i].length; j++) {
				if (monthlyCalendar[i][j] == -1) {
					html.append("<td>&nbsp;</td>");
					continue;
				} else {
					String strCurrentDate = year + "-" + (month + 1) + "-"
							+ monthlyCalendar[i][j];
					java.util.Date currentDate = DateUtil.parse(strCurrentDate,
							"yyyy-MM-dd");
					OACalendarDb oacdb = new OACalendarDb();
					oacdb = (OACalendarDb) oacdb.getQObjectDb(currentDate);
					if (oacdb != null) {
						if (oacdb.getInt("date_type") == DATE_TYPE_SAT_SUN) {
							html
									.append("<td class='holiday' bgcolor='lightcoral' title=\"休息日\"><a onclick=\"initModifydate("
											+ "'"
											+ strCurrentDate
											+ "'"
											+ ",'"
											+ StrUtil
													.getNullStr(oacdb
															.getString("work_time_begin_a"))
											+ "','"
											+ StrUtil
													.getNullStr(oacdb
															.getString("work_time_end_a"))
											+ "','"
											+ StrUtil
													.getNullStr(oacdb
															.getString("work_time_begin_b"))
											+ "','"
											+ StrUtil
													.getNullStr(oacdb
															.getString("work_time_end_b"))
											+ "','"
											+
											// 20091105
											StrUtil
													.getNullStr(oacdb
															.getString("work_time_begin_c"))
											+ "','"
											+ StrUtil
													.getNullStr(oacdb
															.getString("work_time_end_c"))
											+ "','"
											+ DATE_TYPE_HOLIDAY
											+ "','"
											+ (j + 1) + "'" + ")\">");
						} else if (oacdb.getInt("date_type") == DATE_TYPE_HOLIDAY) {
							if (j == 0 || j == 6) {
								html
										.append("<td class='holiday' bgcolor='lightcoral' title=\"休息日\"><a onclick=\"initModifydate("
												+ "'"
												+ strCurrentDate
												+ "'"
												+ ",'"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_begin_a"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_end_a"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_begin_b"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_end_b"))
												+ "','"
												+
												// 20091105
												StrUtil
														.getNullStr(oacdb
																.getString("work_time_begin_c"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_end_c"))
												+ "','"
												+

												DATE_TYPE_HOLIDAY
												+ "','"
												+ (j + 1) + "'" + ")\">");
							} else {
								html
										.append("<td bgcolor='greenyellow' title=\"休息日\"><a onclick=\"initModifydate("
												+ "'"
												+ strCurrentDate
												+ "'"
												+ ",'"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_begin_a"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_end_a"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_begin_b"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_end_b"))
												+ "','"
												+
												// 20091105
												StrUtil
														.getNullStr(oacdb
																.getString("work_time_begin_c"))
												+ "','"
												+ StrUtil
														.getNullStr(oacdb
																.getString("work_time_end_c"))
												+ "','"
												+

												DATE_TYPE_HOLIDAY
												+ "','"
												+ (j + 1) + "'" + ")\">");
							}
						} else {
							String title = "工作日" + "&#10";
							title += "上午"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_begin_a"))
									+ "~"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_end_a"))
									+ "&#10";
							// 20091105
							title += "下午"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_begin_b"))
									+ "~"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_end_b"))
									+ "&#10";
							title += "晚上"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_begin_c"))
									+ "~"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_end_c"));
							html.append("<td title=\""
									+ title
									+ "\"><a onclick=\"initModifydate("
									+ "'"
									+ strCurrentDate
									+ "'"
									+ ",'"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_begin_a"))
									+ "','"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_end_a"))
									+ "','"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_begin_b"))
									+ "','"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_end_b"))
									+ "','"
									+
									// 20091105
									StrUtil.getNullStr(oacdb
											.getString("work_time_begin_c"))
									+ "','"
									+ StrUtil.getNullStr(oacdb
											.getString("work_time_end_c"))
									+ "','" + DATE_TYPE_WORK + "','" + (j + 1)
									+ "'" + ")\">");
						}
					} else {
						// 如果尚示初使化
						html.append("<td bgcolor='#cccccc'><a>");
					}
					html.append(monthlyCalendar[i][j] + "</a></td>");
				}
			}
			html.append("</tr>");
		}
		html.append("</table>");
		return html.toString();
	}

	public boolean isYearInitialized(int year) {
		String strBeginDate = year + "-01-01";
		String strEndDate = (year + 1) + "-01-01";
		Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
		String sql = "select count(*) from oa_calendar where oa_date>=? and oa_date<=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[] { beginDate,
					endDate });
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				if (rr.getInt(1) > 0)
					return true;
				else
					return false;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(
					"isYearInitialized:" + StrUtil.trace(e));
		}
		return false;
	}

	//日历排序，用于清除空白日期，按照顺序排列
	public ArrayList dateOrder(String beginA, String endA, String beginB, String endB, String beginC, String endC, String beginD, String endD, String beginE, String endE) throws ErrMsgException{
		if(beginA.equals("") && !endA.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(!beginA.equals("") && endA.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(beginB.equals("") && !endB.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(!beginB.equals("") && endB.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(beginC.equals("") && !endC.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(!beginC.equals("") && endC.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(beginD.equals("") && !endD.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(!beginD.equals("") && endD.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(beginE.equals("") && !endE.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}
		if(!beginE.equals("") && endE.equals("")){
			throw new ErrMsgException("填写时间必须对称");
		}

		ArrayList<String> al = new ArrayList<String>();   //非空的时间加入al
		if(!beginA.equals("")){
			al.add(beginA);
		}
		if(!endA.equals("")){
			al.add(endA);
		}
		if(!beginB.equals("")){
			al.add(beginB);
		}
		if(!endB.equals("")){
			al.add(endB);
		}
		if(!beginC.equals("")){
			al.add(beginC);
		}
		if(!endC.equals("")){
			al.add(endC);
		}
		if(!beginD.equals("")){
			al.add(beginD);
		}
		if(!endD.equals("")){
			al.add(endD);
		}
		if(!beginE.equals("")){
			al.add(beginE);
		}
		if(!endE.equals("")){
			al.add(endE);
		}
		for(int len = al.size(); len<10 ; len++){   //将剩余空的时间补空值进去
			al.add("");
		}
		return al;
	}

	//添加日历规则
	public boolean addRule(HttpServletRequest request) throws ErrMsgException{
		boolean re = false;
		String unitCode = ParamUtil.get(request, "unitCode");
		String startTime = ParamUtil.get(request, "startTime");
		String endTime = ParamUtil.get(request, "endTime");
		String week = ParamUtil.get(request, "week");
		if(startTime.equals(endTime)){
			week = "";
		}
		String work_time_begin_a = ParamUtil.get(request, "timeBeginA");
		String work_time_end_a = ParamUtil.get(request, "timeEndA");
		String work_time_begin_b = ParamUtil.get(request, "timeBeginB");
		String work_time_end_b = ParamUtil.get(request, "timeEndB");
		String work_time_begin_c = ParamUtil.get(request, "timeBeginC");
		String work_time_end_c = ParamUtil.get(request, "timeEndC");
		String work_time_begin_d = ParamUtil.get(request, "timeBeginD");
		String work_time_end_d = ParamUtil.get(request, "timeEndD");
		String work_time_begin_e = ParamUtil.get(request, "timeBeginE");
		String work_time_end_e = ParamUtil.get(request, "timeEndE");
		ArrayList al = new ArrayList();
		try {
			al = dateOrder(work_time_begin_a,work_time_end_a,work_time_begin_b,work_time_end_b,work_time_begin_c,work_time_end_c,work_time_begin_d,work_time_end_d,work_time_begin_e,work_time_end_e);
		} catch (ErrMsgException e1) {
			throw new ErrMsgException(e1.getMessage());
		}

		//根据整合过以后的数据，按照顺序重新获得日期的顺序
		work_time_begin_a = (String)al.get(0);
		work_time_end_a = (String)al.get(1);
		work_time_begin_b = (String)al.get(2);
		work_time_end_b = (String)al.get(3);
		work_time_begin_c = (String)al.get(4);
		work_time_end_c = (String)al.get(5);
		work_time_begin_d = (String)al.get(6);
		work_time_end_d = (String)al.get(7);
		work_time_begin_e = (String)al.get(8);
		work_time_end_e = (String)al.get(9);

		int dateType = ParamUtil.getInt(request, "dateType",0);
		int userType = ParamUtil.getInt(request, "userType",0);
		//String userCode = ParamUtil.get(request, "userCode");
		String deptCode = "";
		if(userType == 0){   //用户
			int userId = ParamUtil.getInt(request, "userCode",0);
			deptCode = userId + "";
			//DeptUserDb dud = new DeptUserDb(userId);
			//deptCode = dud.getDeptCode();
		}else{  //部门
			deptCode = ParamUtil.get(request, "userCode");
		}
		JdbcTemplate jt = new JdbcTemplate();
		try{
		//为了给oa_work_date表增添外键信息，先得到可能会增加的新ID
		String sql = "select id from oa_work_date order by id desc limit 1";
		ResultIterator ri_workDate = jt.executeQuery(sql);
		ResultRecord rr = null;
		int workId = 0;  //oa_work_id 外键
		if(ri_workDate.hasNext()){
			rr = (ResultRecord)ri_workDate.next();
			workId = rr.getInt(1);
		}
		int flagId = isNeedAddWorkId(workId,week,dateType);  //  -1:表示需要新增 ， -2：单日修改表示无须新增， 其余表示已有的workId
		String newWorkId = "";
		int newId = 0;
		if(flagId == -1){
			newId = workId + 1;   //记录新增newId
			newWorkId = ",work_date_id="+newId;
		}
		sql = "select oa_date from oa_calendar where unit_code = "+StrUtil.sqlstr(unitCode)+" and dept_user_code = "+StrUtil.sqlstr(deptCode)+" and oa_date>="+ StrUtil.sqlstr(startTime)+" and oa_date<="+ StrUtil.sqlstr(endTime);
			ResultIterator ri = jt.executeQuery(sql);
			if(ri.hasNext()){ //已存在该组日历
				//Date beginDate = DateUtil.parse(startTime, "yyyy-MM-dd");
				//Date endDate = DateUtil.parse(endTime, "yyyy-MM-dd");
				if(week.equals("")){
					sql = "select oa_date from oa_calendar where oa_date>="+ StrUtil.sqlstr(startTime)+" and oa_date<="+ StrUtil.sqlstr(endTime) +" and dept_user_code ="+ StrUtil.sqlstr(deptCode) +" and unit_code="+StrUtil.sqlstr(unitCode);
				}else{
					sql = "select oa_date from oa_calendar where oa_date>="+ StrUtil.sqlstr(startTime)+" and oa_date<="+ StrUtil.sqlstr(endTime) +" and dept_user_code ="+ StrUtil.sqlstr(deptCode) +" and unit_code="+StrUtil.sqlstr(unitCode) +" and week_day in ("+week+") ";
				}
				OACalendarDb oacdb = new OACalendarDb();

				//Iterator i = oacdb.list(sql, new Object[] { beginDate, endDate ,week}).iterator();
				Vector v = oacdb.list(sql);
				Iterator i = v.iterator();
				while (i.hasNext()) {
					oacdb = (OACalendarDb) i.next();
					try {
						String oaDate = oacdb.getString("oa_date");
						//String oaWeek = oacdb.getString("week_day");
						//int date_type = oacdb.getInt("date_type");
						//if(week.contains(oaWeek)){
							//date_type = 0;
						/*}else{
							if(date_type!=1){
								date_type = 2;
							}
						}*/
						sql = "update oa_calendar set date_type="+dateType+",work_time_begin_a="+StrUtil.sqlstr(work_time_begin_a)+",work_time_end_a="+StrUtil.sqlstr(work_time_end_a)
						+",work_time_begin_b="+StrUtil.sqlstr(work_time_begin_b)+",work_time_end_b="+StrUtil.sqlstr(work_time_end_b)
						+",work_time_begin_c="+StrUtil.sqlstr(work_time_begin_c)+",work_time_end_c="+StrUtil.sqlstr(work_time_end_c)
						+",work_time_begin_d="+StrUtil.sqlstr(work_time_begin_d)+",work_time_end_d="+StrUtil.sqlstr(work_time_end_d)
						+",work_time_begin_e="+StrUtil.sqlstr(work_time_begin_e)+",work_time_end_e="+StrUtil.sqlstr(work_time_end_e)
						+",dept_user_type="+userType
						+ newWorkId
						+" where oa_date ="+ StrUtil.sqlstr(oaDate)+" and unit_code ="+StrUtil.sqlstr(unitCode)+" and dept_user_code ="+ StrUtil.sqlstr(deptCode)+"";

						re = jt.executeUpdate(sql)>=1 ? true : false;
						//oacdb.del();
						/*re = oaCalendarDb.save(new JdbcTemplate(), new Object[]{0,unitCode,work_time_begin_a,work_time_end_a,
											work_time_begin_b,work_time_end_b,work_time_begin_c,work_time_end_c,
											work_time_begin_d,work_time_end_d,work_time_begin_e,work_time_end_e,
											userCode,userType,oaDate});*/
					} catch (Exception e) {
						LogUtil.getLog(getClass()).error(
								"addRule:" + StrUtil.trace(e));
					}
				}
				//数据修改成功后，添加oa_word_id新数据
				if(flagId != -2){
					addOaWordId(flagId,newId,week,dateType,startTime,endTime,unitCode,deptCode,userType);
				}
			}else{ //不存在该组日历
				//Calendar cal = Calendar.getInstance();
				//int year = cal.get(Calendar.YEAR);//获取年份
				int year = Integer.parseInt(startTime.substring(0,4));
				String parentUnitCode = searchParentCode(unitCode,deptCode,userType,year);
				initCalendar(year,parentUnitCode,deptCode,unitCode,userType);

				//更改修改的数据
				if(week.equals("")){
					sql = "select oa_date from oa_calendar where oa_date>="+ StrUtil.sqlstr(startTime)+" and oa_date<="+ StrUtil.sqlstr(endTime) +" and dept_user_code ="+ StrUtil.sqlstr(deptCode) +" and unit_code="+StrUtil.sqlstr(unitCode);
				}else{
					sql = "select oa_date from oa_calendar where oa_date>="+ StrUtil.sqlstr(startTime)+" and oa_date<="+ StrUtil.sqlstr(endTime) +" and dept_user_code ="+ StrUtil.sqlstr(deptCode) +" and unit_code="+StrUtil.sqlstr(unitCode) +" and week_day in ("+week+") ";
				}
				OACalendarDb oacdb = new OACalendarDb();

				//Iterator i = oacdb.list(sql, new Object[] { beginDate, endDate ,week}).iterator();
				Vector v = oacdb.list(sql);
				Iterator i = v.iterator();
				while (i.hasNext()) {
					oacdb = (OACalendarDb) i.next();
					try {
						String oaDate = oacdb.getString("oa_date");
						//String oaWeek = oacdb.getString("week_day");
						//int date_type = oacdb.getInt("date_type");
						//if(week.contains(oaWeek)){
							//date_type = 0;
						/*}else{
							if(date_type!=1){
								date_type = 2;
							}
						}*/
						sql = "update oa_calendar set date_type="+dateType+",work_time_begin_a="+StrUtil.sqlstr(work_time_begin_a)+",work_time_end_a="+StrUtil.sqlstr(work_time_end_a)
						+",work_time_begin_b="+StrUtil.sqlstr(work_time_begin_b)+",work_time_end_b="+StrUtil.sqlstr(work_time_end_b)
						+",work_time_begin_c="+StrUtil.sqlstr(work_time_begin_c)+",work_time_end_c="+StrUtil.sqlstr(work_time_end_c)
						+",work_time_begin_d="+StrUtil.sqlstr(work_time_begin_d)+",work_time_end_d="+StrUtil.sqlstr(work_time_end_d)
						+",work_time_begin_e="+StrUtil.sqlstr(work_time_begin_e)+",work_time_end_e="+StrUtil.sqlstr(work_time_end_e)
						+",dept_user_type="+userType
						+ newWorkId
						+" where oa_date ="+ StrUtil.sqlstr(oaDate)+" and unit_code ="+StrUtil.sqlstr(unitCode)+" and dept_user_code ="+ StrUtil.sqlstr(deptCode)+"";
						re = jt.executeUpdate(sql)>=1 ? true : false;


					} catch (Exception e) {
						LogUtil.getLog(getClass()).error(
								"addRule:" + StrUtil.trace(e));
					}
				}
				//数据修改成功后，添加oa_word_id新数据
				if(flagId != -2){
					addOaWordId(flagId,newId,week,dateType,startTime,endTime,unitCode,deptCode,userType);
				}
			}
		}catch(Exception e){
			LogUtil.getLog(getClass()).error(e);
		}
		return re;
	}

	//删除该组日历规则
	public boolean delCalendar(HttpServletRequest request){
		boolean re = false;
		String unitCode = ParamUtil.get(request, "unitCode");
		String deptCode = ParamUtil.get(request, "deptCode");
		int deptType = ParamUtil.getInt(request, "deptType",0);
		String sql = "delete from oa_calendar where unit_code="+StrUtil.sqlstr(unitCode)+" and dept_user_code="+StrUtil.sqlstr(deptCode)+" and dept_user_type="+deptType;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			re = jt.executeUpdate(sql) >= 0 ? true : false;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return re;
	}

	//判断是否需要添加新的workId 如果需要则添加
	public int isNeedAddWorkId (int workId ,String week,int dateType){
		int flag = -1;
		if(week.equals("")){ //说明是单独修改，无须增添新workId;
			flag = -2;
			return flag;
		}
		while(workId >= 0){
			String sql = "select * from oa_work_date where id ="+workId;
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri;
			try {
				ri = jt.executeQuery(sql);
				ResultRecord rr = null;
				if(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					String str = "";
					int theWorkId = rr.getInt("id");
					int weekDay1 = rr.getInt("mon");
					int weekDay2 = rr.getInt("tue");
					int weekDay3 = rr.getInt("wed");
					int weekDay4 = rr.getInt("thu");
					int weekDay5 = rr.getInt("fri");
					int weekDay6 = rr.getInt("sat");
					int weekDay7 = rr.getInt("sun");
					if(dateType==0){  //如果是选择工作日
						if(weekDay1==0){
							str = "2,";
						}
						if(weekDay2==0){
							str += "3,";
						}
						if(weekDay3==0){
							str += "4,";
						}
						if(weekDay4==0){
							str += "5,";
						}
						if(weekDay5==0){
							str += "6,";
						}
						if(weekDay6==0){
							str += "7,";
						}
						if(weekDay7==0){
							str += "1,";
						}
					}else{  //如果是选择休息日
						if(weekDay1==1){
							str = "2,";
						}
						if(weekDay2==1){
							str += "3,";
						}
						if(weekDay3==1){
							str += "4,";
						}
						if(weekDay4==1){
							str += "5,";
						}
						if(weekDay5==1){
							str += "6,";
						}
						if(weekDay6==1){
							str += "7,";
						}
						if(weekDay7==1){
							str += "1,";
						}
					}
					str = str.substring(0,str.length()-1);
					if(str.equals(week)){
						flag = theWorkId;
						break;
					}
				}
			} catch (SQLException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			workId--;
		}
		return flag;
	}

	//list页面需要通过选择时间来判断周几是否打勾
	public String findDate(String beginTime ,String endTime,String unitCode,String deptCode,int deptType,int year){
		JdbcTemplate jt = new JdbcTemplate();
		deptCode = searchParentCode(unitCode,deptCode,deptType,year);
		String sql = "select distinct(work_date_id) from oa_calendar where oa_date >= "+StrUtil.sqlstr(beginTime)+" and oa_date<="+StrUtil.sqlstr(endTime)+" and unit_code="+StrUtil.sqlstr(unitCode)+" and dept_user_code="+StrUtil.sqlstr(deptCode);
		ArrayList<Integer> al = new ArrayList<Integer>();
		String str = "";
		try{
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			while(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				al.add(rr.getInt(1));
			}
			if(al.size() == 1){
				int workId = al.get(0);
				sql = "select * from oa_work_date where id ="+workId;
				ri = jt.executeQuery(sql);
				if(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					str = rr.getString(8)+",";
					str += rr.getString(2)+",";
					str += rr.getString(3)+",";
					str += rr.getString(4)+",";
					str += rr.getString(5)+",";
					str += rr.getString(6)+",";
					str += rr.getString(7);
				}
			}else if(al.size() ==0){
				str = "kong";
			}else{
				return str;
			}
		}catch(SQLException e){
			LogUtil.getLog(getClass()).error(e);
		}
		return str;
	}

	//数据修改成功后，添加oa_word_id新数据
	public void addOaWordId(int flagId ,int newWorkId, String week,int dateType, String startDate, String endDate, String unitCode, String deptCode, int deptType) throws SQLException{
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "";
		if(newWorkId != 0){
			sql = "insert into oa_work_date (id,mon,tue,wed,thu,fri,sat,sun) values ("+newWorkId+",0,0,0,0,0,1,1)";
			jt.executeUpdate(sql);
		}
			String[] workDay = week.split(",");
			int weekDay1 = 0;
			int weekDay2 = 0;
			int weekDay3 = 0;
			int weekDay4 = 0;
			int weekDay5 = 0;
			int weekDay6 = 0;
			int weekDay7 = 0;
			if(dateType == 2){
				if(week.equals("")){
					weekDay1 = OAWORK_HOLIDAY;
					weekDay2 = OAWORK_HOLIDAY;
					weekDay3 = OAWORK_HOLIDAY;
					weekDay4 = OAWORK_HOLIDAY;
					weekDay5 = OAWORK_HOLIDAY;
					weekDay6 = OAWORK_HOLIDAY;
					weekDay7 = OAWORK_HOLIDAY;
				}else{
					for(int s = 0 ; s<workDay.length ; s++){
						int theDay = Integer.parseInt(workDay[s]);
						if(theDay==1) {weekDay7 = OAWORK_HOLIDAY;}  //周日
						if(theDay==2) {weekDay1 = OAWORK_HOLIDAY;}
						if(theDay==3) {weekDay2 = OAWORK_HOLIDAY;}
						if(theDay==4) {weekDay3 = OAWORK_HOLIDAY;}
						if(theDay==5) {weekDay4 = OAWORK_HOLIDAY;}
						if(theDay==6) {weekDay5 = OAWORK_HOLIDAY;}
						if(theDay==7) {weekDay6 = OAWORK_HOLIDAY;}
					}
				}
			}else{
				weekDay1 = 1;
				weekDay2 = 1;
				weekDay3 = 1;
				weekDay4 = 1;
				weekDay5 = 1;
				weekDay6 = 1;
				weekDay7 = 1;
				if(week.equals("")){
					weekDay1 = OAWORK_WORK;
					weekDay2 = OAWORK_WORK;
					weekDay3 = OAWORK_WORK;
					weekDay4 = OAWORK_WORK;
					weekDay5 = OAWORK_WORK;
					weekDay6 = OAWORK_WORK;
					weekDay7 = OAWORK_WORK;
				}else{
					for(int s = 0 ; s<workDay.length ; s++){
						int theDay = Integer.parseInt(workDay[s]);
						if(theDay==1) {weekDay7 = OAWORK_WORK;}  //周日
						if(theDay==2) {weekDay1 = OAWORK_WORK;}
						if(theDay==3) {weekDay2 = OAWORK_WORK;}
						if(theDay==4) {weekDay3 = OAWORK_WORK;}
						if(theDay==5) {weekDay4 = OAWORK_WORK;}
						if(theDay==6) {weekDay5 = OAWORK_WORK;}
						if(theDay==7) {weekDay6 = OAWORK_WORK;}
					}
				}
			}

			if(newWorkId != 0){
				sql = "update oa_work_date set mon="+weekDay1+",tue= "+weekDay2+",wed="+weekDay3+",thu="+weekDay4+",fri="+weekDay5+",sat="+weekDay6+",sun="+weekDay7+" where id="+newWorkId;
				jt.executeUpdate(sql);
			}

			//判断是否是新ID还是旧有的ID
			if(newWorkId == 0){
				newWorkId = flagId;
			}
			//修改oa_calendar表的date_type值
			for(int i = 1 ; i<=7 ;i++){
				int weekDay = 0;
				if(i==1){weekDay = weekDay7;}
				else if(i==2){weekDay = weekDay1;}
				else if(i==3){weekDay = weekDay2;}
				else if(i==4){weekDay = weekDay3;}
				else if(i==5){weekDay = weekDay4;}
				else if(i==6){weekDay = weekDay5;}
				else if(i==7){weekDay = weekDay6;}

				sql = "update oa_calendar set date_type="+weekDay+",work_date_id="+newWorkId+" where oa_date>="+StrUtil.sqlstr(startDate)+" and oa_date<="+StrUtil.sqlstr(endDate)+" and week_day = "+i+" and unit_code="+StrUtil.sqlstr(unitCode)+" and dept_user_code="+StrUtil.sqlstr(deptCode)+" and dept_user_type="+deptType;
				jt.executeUpdate(sql);
			}
	}

	//循环查找父类CODE
	public String searchParentCode(String unitCode,String deptCode,int deptType,int year){
		String parentDeptCode = "";
		String startDate = year +"-01-01";
		String endDate = year + "-12-31";
		if(deptType == 0){
			int deptId = Integer.parseInt(deptCode);
			DeptUserDb dud = new DeptUserDb(deptId);
			parentDeptCode = dud.getDeptCode();
		}else{
			DeptDb dd= new DeptDb(deptCode);
			parentDeptCode = dd.getParentCode();
		}
		if(parentDeptCode.equals("-1")){
			return "root";
		}else{
			String sql = "select oa_date from oa_calendar where unit_code = "+StrUtil.sqlstr(unitCode)+" and dept_user_code = "+StrUtil.sqlstr(deptCode)+" and dept_user_type = "+deptType+" and oa_date>="+StrUtil.sqlstr(startDate)+" and oa_date<="+StrUtil.sqlstr(endDate);
			JdbcTemplate jt = new JdbcTemplate();
			try{
				ResultIterator ri = jt.executeQuery(sql);
				if(!ri.hasNext()){
					deptType = 1;  //开始查找部门  用于原本是用户的情况下（也包括原本是部门的情况）
					parentDeptCode = searchParentCode(unitCode,parentDeptCode,deptType,year);
				}else{
					parentDeptCode = deptCode;
				}
			}catch (Exception e) {
			}
			return parentDeptCode;
		}
	}

	public boolean initCalendar(int year) {
		License lic = License.getInstance();
		if (lic.canUseSolution(License.SOLUTION_HR)) {
			String strBeginDate = year + "-01-01";
			String strEndDate = (year + 1) + "-01-01";
			Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
			Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
			String sql = "select oa_date from oa_calendar where oa_date>=? and oa_date<?";
			OACalendarDb oacdb = new OACalendarDb();
			Iterator i = oacdb.list(sql, new Object[] { beginDate, endDate }).iterator();
			while (i.hasNext()) {
				oacdb = (OACalendarDb) i.next();
				try {
					oacdb.del();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error("initCalendar:" + StrUtil.trace(e));
				}
			}
			for (int month = 0; month < 12; month++) {
				int dayOfMon = DateUtil.getDayCount(year, month);
				for (int date = 1; date <= dayOfMon; date++) {
					OACalendarDb oaCalendarDb = new OACalendarDb();
					String strOADate = year + "-" + (month + 1) + "-" + date;
					Date oaDate = DateUtil.parse(strOADate, "yyyy-MM-dd");
					int weekDay = OACalendarDb.getDayOfWeek(year, month, date);
					try {
						if (weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY) {
							oaCalendarDb.create(
											new JdbcTemplate(),
											new Object[] {
													oaDate,
													DeptDb.ROOTCODE,
													new Integer(DATE_TYPE_SAT_SUN),
													"",
													"",
													"",
													"",
													"",
													"",
													new Integer(weekDay),
													"",
													"",
													"",
													"",
													1,
													new Integer(DATE_TYPE_WORK_DATE_ID),
													DeptDb.ROOTCODE, 1 });
						} else {
							Config config = new Config();
							oaCalendarDb.create(
											new JdbcTemplate(),
											new Object[] {
													oaDate,
													DeptDb.ROOTCODE,
													new Integer(0),
													config.get("morningbegin"),
													config.get("morningend"),
													config.get("afternoonbegin"),
													config.get("afternoonend"),
													// 20091105
													config.get("nightbegin"),
													config.get("nightend"),
													new Integer(weekDay),
													"",
													"",
													"",
													"",
													1,
													new Integer(DATE_TYPE_WORK_DATE_ID),
													DeptDb.ROOTCODE, 1 });
						}
					} catch (ResKeyException e) {
						LogUtil.getLog(getClass()).error("initCalendar2:" + StrUtil.trace(e));

					}
				}
			}
			return true;
		} else {
			String strBeginDate = year + "-01-01";
			String strEndDate = (year + 1) + "-01-01";
			Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
			Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
			String sql = "select oa_date from oa_calendar where oa_date>=? and oa_date<=?";
			OACalendarDb oacdb = new OACalendarDb();
			Iterator i = oacdb.list(sql, new Object[] { beginDate, endDate }).iterator();
			while (i.hasNext()) {
				oacdb = (OACalendarDb) i.next();
				try {
					oacdb.del();
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error("initCalendar:" + StrUtil.trace(e));
				}
			}
			for (int month = 0; month < 12; month++) {
				int dayOfMon = DateUtil.getDayCount(year, month);
				for (int date = 1; date <= dayOfMon; date++) {
					OACalendarDb oaCalendarDb = new OACalendarDb();
					String strOADate = year + "-" + (month + 1) + "-" + date;
					Date oaDate = DateUtil.parse(strOADate, "yyyy-MM-dd");
					int weekDay = OACalendarDb.getDayOfWeek(year, month, date);
					try {
						if (weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY) {
							oaCalendarDb.create(new JdbcTemplate(),
									new Object[] { oaDate,
											DeptDb.ROOTCODE,
											new Integer(DATE_TYPE_SAT_SUN), "",
											"", "", "", "", "",
											new Integer(weekDay),
											"", "", "", "", 1, 0, DeptDb.ROOTCODE, 1 });
						} else {
							Config config = new Config();
							oaCalendarDb.create(new JdbcTemplate(),
									new Object[] { oaDate,
											DeptDb.ROOTCODE,
											new Integer(0),
											config.get("morningbegin"),
											config.get("morningend"),
											config.get("afternoonbegin"),
											config.get("afternoonend"),
											// 20091105
											config.get("nightbegin"),
											config.get("nightend"),
											new Integer(weekDay),
											 "", "", "", "", 1, 0, DeptDb.ROOTCODE, 1 });
						}
					} catch (ResKeyException e) {
						LogUtil.getLog(getClass()).error("initCalendar2:" + StrUtil.trace(e));
						return false;
					}
				}
			}
			return true;
		}
	}

	//新页面初始化
	public boolean initCalendarNew(int year) {
		boolean re = false;
		String strBeginDate = year + "-01-01";
		String strEndDate = (year + 1) + "-01-01";
		Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
		String sql = "select oa_date from oa_calendar where oa_date>=? and oa_date<?";
		OACalendarDb oacdb = new OACalendarDb();
		Iterator i = oacdb.list(sql, new Object[] { beginDate, endDate })
				.iterator();
		while (i.hasNext()) {
			oacdb = (OACalendarDb) i.next();
			try {
				oacdb.del();
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(
						"initCalendarNew:" + StrUtil.trace(e));
			}
		}

		String oldBeginDate = (year-1) + "-01-01";
		String oldEndDate = year + "-01-01";
		JdbcTemplate jt = new JdbcTemplate();
		ArrayList<String> alDept = new ArrayList<String>();
		ArrayList<Integer> alType = new ArrayList<Integer>();
		try {
			//找出去年的所有部门数据(用户)
			sql = "select distinct(dept_user_code),dept_user_type from oa_calendar where oa_date >="+StrUtil.sqlstr(oldBeginDate) +" and oa_date <"+StrUtil.sqlstr(oldEndDate)+" and unit_code = 'root'";
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			while(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				String deptCode = rr.getString(1);
				int deptType = rr.getInt(2);
				alDept.add(deptCode);
				alType.add(deptType);
			}
			//找出去年的所有用户（数据）
			/*sql = "select instinct(dept_user_code) from oa_calendar where oa_date >="+StrUtil.sqlstr(oldBeginDate) +" and oa_date <"+StrUtil.sqlstr(oldEndDate)+" and dept_user_type=0 and unit_code = 'root'";
			ri = jt.executeQuery(sql);
			while(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				String userCode = rr.getString(1);
				alUsers.add(userCode);
			}
			*/

			//部门初始化
			for(int s = 0 ; s<alDept.size() ; s++){
				String theCode = alDept.get(s);
				int theType = alType.get(s);
				//初始化....
				for (int month = 0; month < 12; month++) {
					for (int date = 1; date <= DateUtil.getDayCount(year, month); date++) {
						OACalendarDb oaCalendarDb = new OACalendarDb();
						String strOADate = year + "-" + (month + 1) + "-" + date;
						Date oaDate = DateUtil.parse(strOADate, "yyyy-MM-dd");
						int weekDay = OACalendarDb.getDayOfWeek(year, month, date);
						try {
							if (weekDay == Calendar.SATURDAY
									|| weekDay == Calendar.SUNDAY) {
								oaCalendarDb.create(new JdbcTemplate(), new Object[] {
										oaDate,"root", new Integer(DATE_TYPE_SAT_SUN), "", "",
										"", "", "", "", new Integer(weekDay),
										"", "", "", "",1,new Integer(DATE_TYPE_WORK_DATE_ID),theCode,theType });
							} else {
								Config config = new Config();
								oaCalendarDb.create(new JdbcTemplate(), new Object[] {
										oaDate,"root", new Integer(0),
										config.get("morningbegin"),
										config.get("morningend"),
										config.get("afternoonbegin"),
										config.get("afternoonend"),
										// 20091105
										config.get("nightbegin"),
										config.get("nightend"), new Integer(weekDay),
										"", "", "", "",1,new Integer(DATE_TYPE_WORK_DATE_ID),theCode,theType});
							}
						} catch (ResKeyException e) {
							LogUtil.getLog(getClass()).error(
									"initCalendar2:" + StrUtil.trace(e));

						}
					}
				}


				int workDeptId = 0;
				//找出当前code用的最多的workId
				sql = "select count(work_date_id),work_date_id from oa_calendar where dept_user_code="+ StrUtil.sqlstr(theCode)+" and oa_date >="+StrUtil.sqlstr(oldBeginDate) +" and oa_date <"+StrUtil.sqlstr(oldEndDate)+" group by work_date_id order by count(work_date_id) desc;";
				ri = jt.executeQuery(sql);
				if(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					workDeptId = rr.getInt("work_date_id");
				}
				sql = "select * from oa_calendar where unit_code='root' and dept_user_code="+StrUtil.sqlstr(theCode)+" and date_type=0 and work_time_begin_a <> '' and work_date_id="+ workDeptId;
				ri = jt.executeQuery(sql);
				Config config = new Config();
				String beginA = config.get("morningbegin");
				String endA = config.get("morningend");
				String beginB = config.get("afternoonbegin");
				String endB = config.get("afternoonend");
				String beginC = config.get("nightbegin");
				String endC = config.get("nightend");
				String beginD = "";
				String endD = "";
				String beginE = "";
				String endE = "";
				//int deptType = 0;
				if(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					beginA = rr.getString("work_time_begin_a");
					endA = rr.getString("work_time_end_a");
					beginB = rr.getString("work_time_begin_b");
					endB = rr.getString("work_time_end_b");
					beginC = rr.getString("work_time_begin_c");
					endC = rr.getString("work_time_end_c");
					beginD = rr.getString("work_time_begin_d");
					endD = rr.getString("work_time_end_d");
					beginE = rr.getString("work_time_begin_e");
					endE = rr.getString("work_time_end_e");
					//deptType = rr.getInt("dept_user_type");
				}

				//第一次赋值-- 时间和workId
				sql = "update oa_calendar set work_time_begin_a="+StrUtil.sqlstr(beginA)+", work_time_end_a="+StrUtil.sqlstr(endA)
				+", work_time_begin_b="+StrUtil.sqlstr(beginB)
				+", work_time_end_b="+StrUtil.sqlstr(endB)
				+", work_time_begin_c="+StrUtil.sqlstr(beginC)
				+", work_time_end_c="+StrUtil.sqlstr(endC)
				+", work_time_begin_d="+StrUtil.sqlstr(beginD)
				+", work_time_end_d="+StrUtil.sqlstr(endD)
				+", work_time_begin_e="+StrUtil.sqlstr(beginE)
				+", work_time_end_e="+StrUtil.sqlstr(endE)
				+", date_type=0"
				+", work_date_id = "+workDeptId + " where unit_code='root' and dept_user_code="+StrUtil.sqlstr(theCode)+" and oa_date >="+StrUtil.sqlstr(strBeginDate) +" and oa_date <"+StrUtil.sqlstr(strEndDate);
				re = jt.executeUpdate(sql) >=1 ? true : false;

				//第二次赋值 -- date_type
				sql = "select * from oa_work_date where id ="+workDeptId;
				ri = jt.executeQuery(sql);
				String week = "";
				if(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					if(rr.getInt("mon") == 1){
						week += "2,";
					}
					if(rr.getInt("tue") == 1){
						week += "3,";
					}
					if(rr.getInt("wed") == 1){
						week += "4,";
					}
					if(rr.getInt("thu") == 1){
						week += "5,";
					}
					if(rr.getInt("fri") == 1){
						week += "6,";
					}
					if(rr.getInt("sat") == 1){
						week += "7,";
					}
					if(rr.getInt("sun") == 1){
						week += "1,";
					}
					week = week.substring(0,week.length()-1);
				}

				sql = "update oa_calendar set date_type=1,work_time_begin_a='',work_time_end_a='',work_time_begin_b='',work_time_end_b='',work_time_begin_c='',work_time_end_c='',work_time_begin_d='',work_time_end_d='',work_time_begin_e='',work_time_end_e='' where week_day in ("+week+") and unit_code='root' and dept_user_code="+StrUtil.sqlstr(theCode)+" and oa_date >="+StrUtil.sqlstr(strBeginDate) +" and oa_date <"+StrUtil.sqlstr(strEndDate);
				re = jt.executeUpdate(sql) >=1 ? true : false;

			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return re;
	}

	//如果没有该组规则，则添加新日历组
	public boolean initCalendar(int year, String parentUnitCode, String deptCode, String unitCode, int deptType) {
		String strBeginDate = year + "-01-01";
		String strEndDate = (year + 1) + "-01-01";
		//Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		//Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
		String sql = "select * from oa_calendar where oa_date>="+StrUtil.sqlstr(strBeginDate)+" and oa_date<="+StrUtil.sqlstr(strEndDate)+" and dept_user_code="+StrUtil.sqlstr(parentUnitCode)+" and unit_code="+StrUtil.sqlstr(unitCode);
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		ResultRecord rr = null;
		try {
			ri = jt.executeQuery(sql);
			while(ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				OACalendarDb oaCalendarDb = new OACalendarDb();
				oaCalendarDb.create(new JdbcTemplate(), new Object[] {
					rr.getDate("oa_date"),unitCode, rr.getInt("date_type"),
					rr.getString("work_time_begin_a"), rr.getString("work_time_end_a"),
					rr.getString("work_time_begin_b"), rr.getString("work_time_end_b"),
					rr.getString("work_time_begin_c"), rr.getString("work_time_end_c"),
					rr.getInt("week_day"),
					rr.getString("work_time_begin_d"), rr.getString("work_time_end_d"),
					rr.getString("work_time_begin_e"), rr.getString("work_time_end_e"),
					1,rr.getInt("work_date_id"),deptCode,deptType });
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(
					"initCalendar4:" + StrUtil.trace(e));
		}
		return true;
	}

	public boolean modifyDates(HttpServletRequest request) throws ErrMsgException {
		String strBeginDate = ParamUtil.get(request, "modifyBeginDate");
		String strEndDate = ParamUtil.get(request, "modifyEndDate");
		String contains = ParamUtil.get(request, "contains");
		//获取页面设置是否包含周六周日
		boolean containSat = false;
		boolean containSun = false;
		if (contains != null && contains.length() > 0) {
			if (contains.contains("containSat")) {
				containSat = true;
			}
			if (contains.contains("containSun")) {
				containSun = true;
			}
		}
		if (strBeginDate.equals("") && strEndDate.equals("")) {
			throw new ErrMsgException("请输入开始和结束时间");
		}
		Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
		String sql = "select oa_date from oa_calendar where oa_date>=? and oa_date<=?";
		int type = ParamUtil.getInt(request, "type", 1);
		if (type == 2) {
			sql += " and date_type=" + DATE_TYPE_WORK;
		}
		OACalendarDb oacdb = new OACalendarDb();
		List<String> list = new ArrayList<String>();
		for (Object o : oacdb.list(sql, new Object[]{beginDate, endDate})) {
			oacdb = (OACalendarDb) o;
			list.add(oacdb.getString("oa_date"));
			try {
				oacdb.del();
			} catch (ResKeyException e) {
				throw new ErrMsgException(e.getMessage(request));
			}
		}
		int dateType = 0;

		String workTimeBeginA = ParamUtil.get(request, "work_time_begin_a");
		String workTimeEndA = ParamUtil.get(request, "work_time_end_a");
		String workTimeBeginB = ParamUtil.get(request, "work_time_begin_b");
		String workTimeEndB = ParamUtil.get(request, "work_time_end_b");
		// 20091105
		String workTimeBeginC = ParamUtil.get(request, "work_time_begin_c");
		String workTimeEndC = ParamUtil.get(request, "work_time_end_c");
		String workTimeBeginD = ParamUtil.get(request, "work_time_begin_d");
		String workTimeEndD = ParamUtil.get(request, "work_time_end_d");
		String workTimeBeginE = ParamUtil.get(request, "work_time_begin_e");
		String workTimeEndE = ParamUtil.get(request, "work_time_end_e");
		Calendar beginDateCalendar = Calendar.getInstance();
		Calendar endDateCalendar = Calendar.getInstance();
		beginDateCalendar.setTime(DateUtil.parse(strBeginDate, "yyyy-MM-dd"));
		endDateCalendar.setTime(DateUtil.parse(strEndDate, "yyyy-MM-dd"));
		for (int month = beginDateCalendar.get(Calendar.MONTH); month <= endDateCalendar.get(Calendar.MONTH); month++) {
			int minDate = month == beginDateCalendar.get(Calendar.MONTH) ? beginDateCalendar.get(Calendar.DATE) : 1;
			int maxDate = month == endDateCalendar.get(Calendar.MONTH) ? endDateCalendar.get(Calendar.DATE)
					: DateUtil.getDayCount(endDateCalendar.get(Calendar.YEAR), month);
			for (int date = minDate; date <= maxDate; date++) {
				dateType = ParamUtil.getInt(request, "date_type", 0);
				OACalendarDb oaCalendarDb = new OACalendarDb();
				String strOADate = endDateCalendar.get(Calendar.YEAR) + "-" + (month + 1) + "-" + date;
				if (type != 2) {
					//判断是否包含周六周日 不包含则判断插入时间是否为周六周日，是则记为休息日
					Calendar cal = Calendar.getInstance();
					if (!containSat) {
						Date now = DateUtil.parse(strOADate, "yyyy-MM-dd");
						cal.setTime(now);
						int w = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
						if (w == 6)//周六为休息日
						{
							dateType = DATE_TYPE_SAT_SUN;
						}

					}
					if (!containSun) {
						Date now = DateUtil.parse(strOADate, "yyyy-MM-dd");
						cal.setTime(now);
						int w = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
						if (w == 0) {
							dateType = DATE_TYPE_SAT_SUN;
						}
					}
				} else {
					//判断是否为工作日，若是则进行操作，否则跳出本次循环
					boolean flag = false;
					String strOaDate = DateUtil.format(DateUtil.parse(strOADate, "yyyy-MM-dd"), "yyyy-MM-dd");
					for (String dataStr : list) {
						if (strOaDate.equals(dataStr)) {
							flag = true;
							break;
						}
					}
					if (!flag) {
						continue;
					}
				}
				// java.util.Date d =
				// DateUtil.getDate(endDateCalendar.get(Calendar.YEAR), month,
				// date);
				int weekDay = getDayOfWeek(endDateCalendar.get(Calendar.YEAR), month, date);
				String unitCode = new Privilege().getUserUnitCode(request);
				try {
					// 20091105
					oaCalendarDb.create(new JdbcTemplate(),
							new Object[]{strOADate, unitCode, dateType,
									workTimeBeginA, workTimeEndA,
									workTimeBeginB, workTimeEndB,
									workTimeBeginC, workTimeEndC,
									weekDay,
									workTimeBeginD, workTimeEndD,
									workTimeBeginE, workTimeEndE,
									1,
									DATE_TYPE_WORK_DATE_ID,
									DeptDb.ROOTCODE, 1});
				} catch (ResKeyException e) {
					LogUtil.getLog(getClass()).error("modifyDates:" + StrUtil.trace(e));
				}
			}
		}
		return true;
	}

	/**
	 * 取得当fromDate与toDate在同一天时的工作时间（小时）
	 *
	 * @param fromDate
	 *            Date
	 * @param toDate
	 *            Date
	 * @return double
	 */
	public double getWorkHourOnSameDay(java.util.Date fromDate,
			java.util.Date toDate) throws ErrMsgException {
		String dateStr = DateUtil.format(fromDate, "yyyy-MM-dd");
		Date dt = DateUtil.parse(dateStr, "yyyy-MM-dd");

		OACalendarDb oad = new OACalendarDb();
		oad = (OACalendarDb) oad.getQObjectDb(dt);
		// 未初始化日期
		if (oad == null) {
			throw new ErrMsgException(DateUtil.format(dt, "yyyy-MM-dd")
					+ " 在工作日历中没有初始化！");
		}
		// 不是工作日
		if (oad.getInt("date_type") != OACalendarDb.DATE_TYPE_WORK) {
			return 0;
		}

		double h = 0;
		String ta1 = StrUtil.getNullStr(oad.getString("work_time_begin_a"));
		String ta2 = StrUtil.getNullStr(oad.getString("work_time_end_a"));
		String tb1 = StrUtil.getNullStr(oad.getString("work_time_begin_b"));
		String tb2 = StrUtil.getNullStr(oad.getString("work_time_end_b"));

		String tc1 = StrUtil.getNullStr(oad.getString("work_time_begin_c"));
		String tc2 = StrUtil.getNullStr(oad.getString("work_time_end_c"));

		java.util.Date da1 = null; // 为空则表示上午上班时间未设，该时间段不计入工作时间，下同
		if (!ta1.equals(""))
			da1 = DateUtil.parse(DateUtil.format(fromDate, "yyyy-MM-dd") + " "
					+ ta1, "yyyy-MM-dd HH:mm");
		java.util.Date da2 = null;
		if (!ta2.equals(""))
			da2 = DateUtil.parse(DateUtil.format(fromDate, "yyyy-MM-dd") + " "
					+ ta2, "yyyy-MM-dd HH:mm");
		java.util.Date db1 = null;
		if (!tb1.equals(""))
			db1 = DateUtil.parse(DateUtil.format(fromDate, "yyyy-MM-dd") + " "
					+ tb1, "yyyy-MM-dd HH:mm");
		java.util.Date db2 = null;
		if (!tb2.equals(""))
			db2 = DateUtil.parse(DateUtil.format(fromDate, "yyyy-MM-dd") + " "
					+ tb2, "yyyy-MM-dd HH:mm");

		java.util.Date dc1 = null;
		if (!tc1.equals(""))
			dc1 = DateUtil.parse(DateUtil.format(fromDate, "yyyy-MM-dd") + " "
					+ tc1, "yyyy-MM-dd HH:mm");

		java.util.Date dc2 = null;
		if (!tc2.equals(""))
			dc2 = DateUtil.parse(DateUtil.format(fromDate, "yyyy-MM-dd") + " "
					+ tc2, "yyyy-MM-dd HH:mm");

		// 开始时间在上午上班前
		if (da1 != null && DateUtil.compare(da1, fromDate) <= 1) {
			// toDate在上午上班前
			if (DateUtil.compare(da1, toDate) <= 1) {
				h = 0;
			} else {
				// toDate在上午下班前
				if (DateUtil.compare(da2, toDate) <= 1) {
					h = ((double) DateUtil.datediffMinute(toDate, da1)) / 60;
				} else {
					if (db1 != null) {
						// toDate在下午上班前
						if (DateUtil.compare(db1, toDate) <= 1) {
							h = ((double) DateUtil.datediffMinute(da2, da1)) / 60;
						} else {
							// toDate在下午下班前
							if (DateUtil.compare(db2, toDate) <= 1) {
								h = ((double) (DateUtil
										.datediffMinute(da2, da1) + DateUtil
										.datediffMinute(toDate, db1))) / 60;
							} else {
								if (dc1 != null) {
									// toDate在晚上上班前
									if (DateUtil.compare(dc1, toDate) <= 1) {
										h = ((double) (DateUtil.datediffMinute(
												da2, da1) + DateUtil
												.datediffMinute(db2, db1))) / 60;
									} else {
										// toDate在晚上下班前
										if (DateUtil.compare(dc2, toDate) <= 1) {
											h = ((double) (DateUtil
													.datediffMinute(da2, da1)
													+ DateUtil.datediffMinute(
															db2, db1) + DateUtil
													.datediffMinute(toDate, dc1))) / 60;

										} else {
											// toDate在晚上下班后
											h = ((double) (DateUtil
													.datediffMinute(da2, da1)
													+ DateUtil.datediffMinute(
															db2, db1) + DateUtil
													.datediffMinute(dc2, dc1))) / 60;

										}
									}
								} else {
									// 晚上休息
									h = ((double) (DateUtil.datediffMinute(da2,
											da1) + DateUtil.datediffMinute(db2,
											db1))) / 60;
								}
							}
						}
					} else {
						// 下午休息
						h = ((double) DateUtil.datediffMinute(da2, da1)) / 60;
					}
				}
			}
		} else if (da1 != null && DateUtil.compare(fromDate, da1) <= 1
				&& DateUtil.compare(da2, fromDate) <= 1) {
			// fromDate在上午上班后，上午下班前
			// toDate在上午下班前
			if (DateUtil.compare(da2, toDate) <= 1) {
				h = ((double) DateUtil.datediffMinute(toDate, fromDate)) / 60;
			} else {
				if (db1 != null) {
					// toDate在下午上班前
					if (DateUtil.compare(db1, toDate) <= 1) {
						h = ((double) DateUtil.datediffMinute(da2, fromDate)) / 60;
					} else {
						// toDate在下午下班前
						if (DateUtil.compare(db2, toDate) <= 1) {
							h = ((double) (DateUtil.datediffMinute(da2,
									fromDate) + DateUtil.datediffMinute(toDate,
									db1))) / 60;
						} else {
							// toDate在下午下班后
							if (dc1 != null) {
								// toDate在晚上上班前
								if (DateUtil.compare(dc1, toDate) <= 1) {
									h = ((double) (DateUtil.datediffMinute(da2,
											fromDate) + DateUtil
											.datediffMinute(db2, db1))) / 60;
								} else {
									// toDate在晚上下班前
									if (DateUtil.compare(dc2, toDate) <= 1) {
										h = ((double) (DateUtil.datediffMinute(
												da2, fromDate)
												+ DateUtil.datediffMinute(db2,
														db1) + DateUtil
												.datediffMinute(toDate, dc1))) / 60;
									} else {
										// toDate在晚上下班后
										h = ((double) (DateUtil.datediffMinute(
												da2, fromDate)
												+ DateUtil.datediffMinute(db2,
														db1) + DateUtil
												.datediffMinute(dc2, dc1))) / 60;
									}
								}
							} else {
								// 晚上休息
								h = ((double) (DateUtil.datediffMinute(da2,
										fromDate) + DateUtil.datediffMinute(
										db2, db1))) / 60;
							}
						}
					}
				} else {
					// 下午休息
					h = ((double) DateUtil.datediffMinute(da2, fromDate)) / 60;
				}
			}
		} else if (db1 != null && DateUtil.compare(fromDate, da2) <= 1
				&& DateUtil.compare(db1, fromDate) <= 1) {
			// fromDate在上午下班后，下午上班前
			// toDate在下午上班前
			if (DateUtil.compare(db1, toDate) <= 1) {
				h = 0;
			} else {
				// toDate在下午下班前
				if (DateUtil.compare(db2, toDate) <= 1) {
					h = ((double) DateUtil.datediffMinute(toDate, db1)) / 60;
				} else {
					// toDate在下午下班后
					if (dc1 != null) {
						// toDate在晚上上班前
						if (DateUtil.compare(dc1, toDate) <= 1) {
							h = ((double) DateUtil.datediffMinute(db2, db1)) / 60;
						} else {
							// toDate在晚上下班前
							if (DateUtil.compare(dc2, toDate) <= 1) {
								h = ((double) DateUtil.datediffMinute(db2, db1) + (double) DateUtil
										.datediffMinute(toDate, dc1)) / 60;
							} else {
								// toDate在晚上下班后
								h = ((double) DateUtil.datediffMinute(db2, db1) + (double) DateUtil
										.datediffMinute(dc2, dc1)) / 60;
							}
						}
					} else {
						// 晚上休息
						h = ((double) DateUtil.datediffMinute(db2, db1)) / 60;
					}
				}
			}
		} else if (db1 != null && DateUtil.compare(fromDate, db1) <= 1
				&& DateUtil.compare(db2, fromDate) <= 1) {
			// fromDate在下午上班后，下午下班前
			// toDate在下午下班前
			if (DateUtil.compare(db2, toDate) <= 1) {
				h = ((double) DateUtil.datediffMinute(toDate, fromDate)) / 60;
			} else {
				// toDate在下午下班后
				if (dc1 != null) {
					// toDate在晚上上班前
					if (DateUtil.compare(dc1, toDate) <= 1) {
						h = ((double) DateUtil.datediffMinute(db2, fromDate)) / 60;
					} else {
						// toDate在晚上下班前
						if (DateUtil.compare(dc2, toDate) <= 1) {
							h = ((double) DateUtil
									.datediffMinute(db2, fromDate) + (double) DateUtil
									.datediffMinute(toDate, dc1)) / 60;
						} else {
							// toDate在晚上下班后
							h = ((double) DateUtil
									.datediffMinute(db2, fromDate) + (double) DateUtil
									.datediffMinute(dc2, dc1)) / 60;
						}
					}
				} else {
					// 晚上休息
					h = ((double) DateUtil.datediffMinute(db2, fromDate)) / 60;
				}
			}
		} else if (dc1 != null && DateUtil.compare(fromDate, db2) <= 1
				&& DateUtil.compare(dc1, fromDate) <= 1) {
			// fromDate在下午下班后，晚上上班前
			// toDate在晚上上班前
			if (DateUtil.compare(dc1, toDate) <= 1) {
				h = 0;
			} else {
				// toDate在晚上下班前
				if (DateUtil.compare(dc2, toDate) <= 1) {
					h = ((double) DateUtil.datediffMinute(toDate, dc1)) / 60;
				} else {
					// toDate在晚上下班后
					h = ((double) DateUtil.datediffMinute(dc2, dc1)) / 60;
				}
			}
		} else if (dc1 != null && DateUtil.compare(fromDate, dc1) <= 1
				&& DateUtil.compare(dc2, fromDate) <= 1) {
			// fromDate在晚上上班后，晚上下班前
			// toDate在晚上下班前
			if (DateUtil.compare(dc2, toDate) <= 1) {
				h = ((double) DateUtil.datediffMinute(toDate, fromDate)) / 60;
			} else {
				// toDate在晚上下班后
				h = ((double) DateUtil.datediffMinute(dc2, fromDate)) / 60;
			}
		} else {
			// fromDate在晚上下班后，或者da1=da2=db1=db2=dc1=dc2=null
			h = 0;
		}
		return h;
	}

	/**
	 * 计算fromDate至toDate之间的工作小时
	 *
	 * @param fromDate
	 *            Date
	 * @param toDate
	 *            Date
	 * @return int
	 */
	public double getWorkHourCount(java.util.Date fromDate,
			java.util.Date toDate) throws ErrMsgException {
		// 检查fromDate与toDate的大小
		if (DateUtil.compare(fromDate, toDate) == 1)
			return -1;
		double h = -1;

		if (fromDate == null || toDate == null)
			return 0;

		if (DateUtil.isSameDay(fromDate, toDate))
			return getWorkHourOnSameDay(fromDate, toDate);

		// 检查当天内所剩的工作小时数，能否满足expireHour条件，如果能，则获得expireDate
		// 检查是否为休息日
		Calendar cal = Calendar.getInstance();
		cal.setTime(fromDate);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH);
		int d = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(y, m, d, 0, 0, 0);
		java.util.Date dt = cal.getTime();

		int k = 1;
		java.util.Date curDay = fromDate;
		java.util.Date dtDayEnd;
		cal.set(y, m, d, 23, 59, 59);
		dtDayEnd = cal.getTime();

		h = getWorkHourOnSameDay(curDay, dtDayEnd);

		while (true) {
			dt = DateUtil.addDate(dt, 1);

			if (DateUtil.isSameDay(dt, toDate)) {
				h += getWorkHourOnSameDay(dt, toDate);
				break;
			} else {
				cal.setTime(dt);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				dtDayEnd = cal.getTime();

				h += getWorkHourOnSameDay(dt, dtDayEnd);
			}
			k++;
		}

		return h;
	}

	/**
	 * 从数据库中计算fromDate至toDate之间的工作日期，在算法中包含了结束日，但不包含开始日
	 * fromDate与toDate间隔时间长的时候，用此方法
	 *
	 * @param fromDate
	 *            Date
	 * @param toDate
	 *            Date
	 * @return int
	 * @throws ErrMsgException
	 */
	public int getWorkDayCountFromDb(java.util.Date fromDate,
			java.util.Date toDate) {
		if (fromDate == null || toDate == null)
			return 0;
		String sql = "select count(*) from "
				+ getTable().getName()
				+ " where date_type="
				+ OACalendarDb.DATE_TYPE_WORK
				+ " and oa_date>"
				+ SQLFilter.getDateStr(DateUtil.format(fromDate, "yyyy-MM-dd"),
						"yyyy-MM-dd")
				+ " and oa_date<"
				+ SQLFilter.getDateStr(DateUtil.format(DateUtil.addDate(toDate,
						1), "yyyy-MM-dd"), "yyyy-MM-dd");
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				return rr.getInt(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return -1;
	}

	/**
	 * 计算fromDate至toDate之间的工作日期，在算法中包含了结束日，但不包含开始日，通过遍历获取
	 * fromDate与toDate间隔时间短的时候，用此方法 *
	 *
	 * @param fromDate
	 *            Date
	 * @param toDate
	 *            Date
	 * @return int
	 */
	public int getWorkDayCount(java.util.Date fromDate, java.util.Date toDate)
			throws ErrMsgException {
		// 检查fromDate与toDate的大小
		if (DateUtil.compare(fromDate, toDate) == 1)
			return -1;

		if (fromDate == null || toDate == null)
			return 0;

		if (DateUtil.isSameDay(fromDate, toDate))
			return 0;

		String dateStr = DateUtil.format(fromDate, "yyyy-MM-dd");
		Date dt = DateUtil.parse(dateStr, "yyyy-MM-dd");

		int k = 0;

		OACalendarDb oad = new OACalendarDb();

		while (true) {
			dt = DateUtil.addDate(dt, 1);

			oad = (OACalendarDb) oad.getQObjectDb(dt);
			// 未初始化日期
			if (oad == null) {
				throw new ErrMsgException(DateUtil.format(dt, "yyyy-MM-dd")
						+ " 在工作日历中没有初始化！");
			}
			// 不是工作日
			if (oad.getInt("date_type") != OACalendarDb.DATE_TYPE_WORK) {
				if (DateUtil.isSameDay(dt, toDate))
					break;
				else
					continue;
			}

			k++;

			if (DateUtil.isSameDay(dt, toDate)) {
				break;
			}
		}

		return k;
	}

	/**
	 * 计算当前时间加上工作小时
	 *
	 * @param hour
	 * @return
	 */
	public static java.util.Date addWorkHour(double hour) {
		// 加上对于休息日的处理，如果在指定处理时间（小时）范围内，有工作日，则顺延
		// 如果关联
		Calendar cal = Calendar.getInstance();
		OACalendarDb oad = new OACalendarDb();

		java.util.Date cur = new java.util.Date();
		java.util.Date expireDate = null;
		// 检查当天内所剩的工作小时数，能否满足expireHour条件，如果能，则获得expireDate
		// 检查是否为休息日
		cal.setTime(cur);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH);
		int d = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(y, m, d, 0, 0, 0);
		java.util.Date dt = cal.getTime();
		OACalendarDb oad2 = (OACalendarDb) oad.getQObjectDb(dt);
		double hourRemained = 0; // 剩余的小时数
		if (oad2 != null) {
			if (oad2.getInt("date_type") == OACalendarDb.DATE_TYPE_WORK) {
				String ta1 = StrUtil.getNullStr(oad2
						.getString("work_time_begin_a"));
				String ta2 = StrUtil.getNullStr(oad2
						.getString("work_time_end_a"));
				String tb1 = StrUtil.getNullStr(oad2
						.getString("work_time_begin_b"));
				String tb2 = StrUtil.getNullStr(oad2
						.getString("work_time_end_b"));
				String tc1 = StrUtil.getNullStr(oad2
						.getString("work_time_begin_c"));
				String tc2 = StrUtil.getNullStr(oad2
						.getString("work_time_end_c"));

				java.util.Date da1 = null; // 为空则表示上午上班时间未设，该时间段不计入工作时间，下同
				if (!ta1.equals(""))
					da1 = DateUtil.parse(DateUtil.format(cur, "yyyy-MM-dd")
							+ " " + ta1, "yyyy-MM-dd HH:mm");
				java.util.Date da2 = null;
				if (!ta2.equals(""))
					da2 = DateUtil.parse(DateUtil.format(cur, "yyyy-MM-dd")
							+ " " + ta2, "yyyy-MM-dd HH:mm");
				java.util.Date db1 = null;
				if (!tb1.equals(""))
					db1 = DateUtil.parse(DateUtil.format(cur, "yyyy-MM-dd")
							+ " " + tb1, "yyyy-MM-dd HH:mm");
				java.util.Date db2 = null;
				if (!tb2.equals(""))
					db2 = DateUtil.parse(DateUtil.format(cur, "yyyy-MM-dd")
							+ " " + tb2, "yyyy-MM-dd HH:mm");

				java.util.Date dc1 = null;
				if (!tc1.equals(""))
					dc1 = DateUtil.parse(DateUtil.format(cur, "yyyy-MM-dd")
							+ " " + tc1, "yyyy-MM-dd HH:mm");
				java.util.Date dc2 = null;
				if (!tc2.equals(""))
					dc2 = DateUtil.parse(DateUtil.format(cur, "yyyy-MM-dd")
							+ " " + tc2, "yyyy-MM-dd HH:mm");

				if (DateUtil.compare(cur, da1) == 2) {
					// 当天上班前，动作就已经完成
					// 取得上午工作小时数
					double h = ((double) DateUtil.datediffMinute(da2, da1)) / 60;
					if (h >= hour) {
						expireDate = addHour(da1, hour);
					} else {
						double h2 = ((double) DateUtil.datediffMinute(db2, db1)) / 60;
						// 下午能完成
						if (h + h2 >= hour) {
							expireDate = addHour(db1, hour - h);
						} else {
							double h3 = ((double) DateUtil.datediffMinute(dc2,
									dc1)) / 60;
							// 晚上能完成
							if (h + h2 + h3 >= hour) {
								expireDate = addHour(dc1, hour - h - h2);
							} else
								hourRemained = h + h2 + h3; // 取得当日剩余小时数
						}
					}
				} else if (DateUtil.compare(cur, da1) == 1
						&& DateUtil.compare(cur, da2) == 2) {
					// 上午班
					double h = ((double) DateUtil.datediffMinute(da2, cur)) / 60; // 上午班结束前剩余时间
					if (h >= hour) { // 上午能完成
						expireDate = addHour(cur, hour);
					} else {
						double h2 = ((double) DateUtil.datediffMinute(db2, db1)) / 60;
						if ((h + h2) >= hour) { // 下午能完成
							expireDate = addHour(db1, hour - h);
						} else {
							double h3 = ((double) DateUtil.datediffMinute(dc2,
									dc1)) / 60;
							// 晚上能完成
							if (h + h2 + h3 >= hour) {
								expireDate = addHour(dc1, hour - h - h2);
							} else
								// 取得当日剩余小时数
								hourRemained = h + h2 + h3;
						}
					}
				} else if (DateUtil.compare(cur, da2) == 1
						&& DateUtil.compare(cur, db1) == 2) {
					// 上午班结束，下午班开始前
					double h2 = ((double) DateUtil.datediffMinute(db2, db1)) / 60;
					// 下午能结束
					if (h2 >= hour) {
						expireDate = addHour(db1, hour);
					} else {
						double h3 = ((double) DateUtil.datediffMinute(dc2, dc1)) / 60;
						if (h2 + h3 >= hour) {
							expireDate = addHour(dc1, hour - h2);
						} else
							hourRemained = h2 + h3;
					}
				} else if (DateUtil.compare(cur, db1) == 1
						&& DateUtil.compare(cur, db2) == 2) {
					// 下午班开始后，下午下班之前
					// 取得距离下午下班时的小时数
					double h2 = ((double) DateUtil.datediffMinute(db2, cur)) / 60;

					// LogUtil.getLog(getClass()).info(DateUtil.format(da1,
					// "yyyy-MM-dd HH:mm:ss") + " " + DateUtil.format(da2,
					// "yyyy-MM-dd HH:mm:ss") + " " + DateUtil.format(db1,
					// "yyyy-MM-dd HH:mm:ss") + " " + DateUtil.format(db2,
					// "yyyy-MM-dd HH:mm:ss"));
					// LogUtil.getLog(getClass()).info("h2=" + h2 + " cur=" +
					// DateUtil.format(cur, "yyyy-MM-dd HH:mm:ss"));

					if (h2 >= hour) {
						expireDate = addHour(cur, hour);
					} else {
						double h3 = ((double) DateUtil.datediffMinute(dc2, dc1)) / 60;
						if (h2 + h3 >= hour) {
							expireDate = addHour(dc1, hour - h2);
						} else
							hourRemained = h2 + h3;

						hourRemained = h2;
					}
				} else if (DateUtil.compare(cur, db2) == 1
						&& DateUtil.compare(cur, dc1) == 2) {
					// 下午下班之后，晚上上班之前
					double h3 = ((double) DateUtil.datediffMinute(dc2, dc1)) / 60;
					if (h3 >= hour) {
						expireDate = addHour(dc1, hour);
					} else {
						hourRemained = h3;
					}
				} else if (DateUtil.compare(cur, dc1) == 1
						&& DateUtil.compare(cur, dc2) == 2) {
					// 晚上上班之后，晚上下班之前
					double h3 = ((double) DateUtil.datediffMinute(dc2, cur)) / 60;
					if (h3 >= hour) {
						expireDate = addHour(cur, hour);
					} else {
						hourRemained = h3;
					}
				} else if (DateUtil.compare(cur, dc2) == 1) {
					// 晚上下班之后
				}
			}
		}
		// 如果到期时间不在当天，则继续往后推算
		if (expireDate == null) {
			int k = 1;
			java.util.Date curDay = dt;
			do {
				dt = DateUtil.addDate(curDay, k);
				// LogUtil.getLog(getClass()).info("k=" + k + " dt=" +
				// DateUtil.format(dt, "yyyy-MM-dd HH:mm:ss"));
				// LogUtil.getLog(getClass()).info("hourRemained1=" +
				// hourRemained);

				oad2 = (OACalendarDb) oad.getQObjectDb(dt);
				if (oad2 == null) {
					LogUtil.getLog(OACalendarDb.class)
							.error(
									DateUtil.format(dt, "yyyy-MM-dd")
											+ " 在工作日历中没有初始化！");
					break;
				} else if (oad2.getInt("date_type") != OACalendarDb.DATE_TYPE_WORK) {
					k++;
					continue;
				}
				String ta1 = StrUtil.getNullStr(oad2
						.getString("work_time_begin_a"));
				String ta2 = StrUtil.getNullStr(oad2
						.getString("work_time_end_a"));
				String tb1 = StrUtil.getNullStr(oad2
						.getString("work_time_begin_b"));
				String tb2 = StrUtil.getNullStr(oad2
						.getString("work_time_end_b"));

				String tc1 = StrUtil.getNullStr(oad2
						.getString("work_time_begin_c"));
				String tc2 = StrUtil.getNullStr(oad2
						.getString("work_time_end_c"));

				java.util.Date da1 = null; // 为空则表示上午上班时间未设，该时间段不计入工作时间，下同
				if (!ta1.equals(""))
					da1 = DateUtil.parse(DateUtil.format(dt, "yyyy-MM-dd")
							+ " " + ta1, "yyyy-MM-dd HH:mm");
				java.util.Date da2 = null;
				if (!ta2.equals(""))
					da2 = DateUtil.parse(DateUtil.format(dt, "yyyy-MM-dd")
							+ " " + ta2, "yyyy-MM-dd HH:mm");
				java.util.Date db1 = null;
				if (!tb1.equals(""))
					db1 = DateUtil.parse(DateUtil.format(dt, "yyyy-MM-dd")
							+ " " + tb1, "yyyy-MM-dd HH:mm");
				java.util.Date db2 = null;
				if (!tb2.equals(""))
					db2 = DateUtil.parse(DateUtil.format(dt, "yyyy-MM-dd")
							+ " " + tb2, "yyyy-MM-dd HH:mm");

				java.util.Date dc1 = null;
				if (!tc1.equals(""))
					dc1 = DateUtil.parse(DateUtil.format(dt, "yyyy-MM-dd")
							+ " " + tc1, "yyyy-MM-dd HH:mm");
				java.util.Date dc2 = null;
				if (!tc2.equals(""))
					dc2 = DateUtil.parse(DateUtil.format(dt, "yyyy-MM-dd")
							+ " " + tc2, "yyyy-MM-dd HH:mm");

				LogUtil.getLog(OACalendarDb.class).info(
						DateUtil.format(da1, "yyyy-MM-dd HH:mm:ss") + " "
								+ DateUtil.format(da2, "yyyy-MM-dd HH:mm:ss")
								+ " "
								+ DateUtil.format(db1, "yyyy-MM-dd HH:mm:ss")
								+ " "
								+ DateUtil.format(db2, "yyyy-MM-dd HH:mm:ss"));

				// 上午班
				double h = ((double) DateUtil.datediffMinute(da2, da1)) / 60;
				if (da1 != null && da2 != null) {
					if (hourRemained + h >= hour) {
						expireDate = addHour(da1, hour - hourRemained);
						break;
					} else {
						hourRemained += h;
					}
				}
				// 下午班
				if (db1 != null && db2 != null) {
					double h2 = ((double) DateUtil.datediffMinute(db2, db1)) / 60;
					if (hourRemained + h2 >= hour) {
						expireDate = addHour(db1, hour - hourRemained);
						break;
					} else {
						hourRemained += h2;
					}
				}
				// 晚上班
				if (dc1 != null && dc2 != null) {
					double h3 = ((double) DateUtil.datediffMinute(dc2, dc1)) / 60;
					if (hourRemained + h3 >= hour) {
						expireDate = addHour(dc1, hour - hourRemained);
						break;
					} else {
						hourRemained += h3;
					}
				}

				// LogUtil.getLog(getClass()).info("hourRemained2=" +
				// hourRemained);

				k++;
			} while (expireDate == null);
		}
		return expireDate;

	}

	/**
	 * 计算当前时间加上工作日
	 *
	 * @param day
	 * @return
	 */
	public static Date addWorkDay(int day) {
		return addWorkDay(new Date(), day);
	}

	/**
	 * @Description: 计算某天加上工作日
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date addWorkDay(Date date, int day) {
		// 当天不计入超时时间
		// 遍历指定的当天其后的expire天，如果是休息日，则不计入，往后顺延
		Calendar cal = Calendar.getInstance();
		OACalendarDb oad = new OACalendarDb();
		java.util.Date dt;
		for (int i = 1; i <= day; i++) {
			dt = DateUtil.addDate(date, i);
			// 检查是否为休息日
			cal.setTime(dt);
			int y = cal.get(Calendar.YEAR);
			int m = cal.get(Calendar.MONTH);
			int d = cal.get(Calendar.DAY_OF_MONTH);
			cal.set(y, m, d, 0, 0, 0);
			dt = cal.getTime();

			OACalendarDb oad2 = (OACalendarDb) oad.getQObjectDb(dt);
			if (oad2 != null) {
				if (oad2.getInt("date_type") != OACalendarDb.DATE_TYPE_WORK) {
					day++;
				}
			} else {
				LogUtil.getLog(OACalendarDb.class).error(
						DateUtil.format(dt, "yyyy-MM-dd") + " 在工作日历中没有初始化！");
				break;
			}
		}

		java.util.Date expireDate = DateUtil.addDate(date, day);
		cal.setTime(expireDate);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH);
		int d = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(y, m, d, 23, 59, 59);
		return cal.getTime();
	}

	/**
	 * 时间加上带小数点的小时，小数点后在相加的时候换算为分钟
	 *
	 * @param dt
	 * @param expireHour
	 * @return
	 */
	public static java.util.Date addHour(java.util.Date dt, double expireHour) {
		int h = (int) expireHour;
		double mi = expireHour - h;
		int m = 0;
		if (mi != 0) {
			m = (int) (mi * 60);
		}

		java.util.Date d = DateUtil.addHourDate(dt, h);
		if (m != 0) {
			d = DateUtil.addMinuteDate(d, m);
		}
		return d;
	}
}
