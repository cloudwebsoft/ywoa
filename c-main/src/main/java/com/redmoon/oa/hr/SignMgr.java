package com.redmoon.oa.hr;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.attendance.AttendanceMgr;
import com.redmoon.oa.attendance.ShiftScheduleMgr;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAO;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

public class SignMgr {
	public static final String insertSQL = "insert into kaoqin_arrange(number,name,date,earlycount,latecount,abscount,nocount,tripday,days,sickday,thingday,yearday,marryday,maternityday,otherday,workday,sbsj,xbsj,txsc,wcday,duration,supplement_count)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public int insertBlankRecord(JdbcTemplate jt, String accountId, String userName, Date workDate) {
		String workDateString = DateUtil.format(workDate, "yyyy-MM-dd");
		try {
			return jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
                    workDateString, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0, 0});
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return -1;
	}

	/**
	 * 删除指定日期的汇总数据
	 * @param begindate
	 * @param enddate 
	 */
	public int delDataCollectByDate(String begindate,String enddate){
		String beginDate = SQLFilter.getDateStr(begindate, "yyyy-MM-dd");
		String endDate = SQLFilter.getDateStr(enddate, "yyyy-MM-dd");
		String sql = "delete from kaoqin_arrange where date >= "+beginDate +" and date <= "+endDate; 
		JdbcTemplate jt = new JdbcTemplate();
		int i = -1;
		try {
			i = jt.executeUpdate(sql);
			return i;
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("删除数据汇总出现异常："+StrUtil.trace(e));
		} finally {
			jt.close();
		}
		return i ;
	}
	
	/**
	 * 考勤数据汇总
	 * @param   beginDate 开始日期
	 * @param	endDate   结束日期
	 * 
	 */
	public void dataCollect(String beginDate, String endDate) {
		// 遍历人员
		String sql = "select u.name userName, u.person_no accountId from users u where u.name <> 'admin' and u.name <> 'system'";
		ResultIterator ri;
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord urr = (ResultRecord)ri.next();
				String userName = urr.getString(1);
				String personNo = urr.getString(2);     // 个人编号对应考勤号
				
				Date workDate = DateUtil.parse(beginDate, "yyyy-MM-dd");
				Date eDate = DateUtil.parse(endDate, "yyyy-MM-dd");
				while (DateUtil.compare(eDate, workDate)<2) {
					collect(jt, userName, personNo, workDate);
					workDate = DateUtil.addDate(workDate, 1);
				}
			}			
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		finally {
			jt.close();
		}
	}

	/**
	 * 汇总某用户在某天的数据
	 * @param jt
	 * @param userName
	 * @param personNo
	 * @param workDate
	 * @throws SQLException
     */
	public void collect(JdbcTemplate jt, String userName, String personNo, Date workDate) throws SQLException {
		String workDateString = new SimpleDateFormat("yyyy-MM-dd").format(workDate);
		// 取得当天的上班、下班时间
		Object[] ary = ShiftScheduleMgr.getShiftDAO(userName, workDate);
		if (ary!=null) {
			FormDAO fdao = (FormDAO)ary[0];
			if (fdao == null) {
				DebugUtil.e(getClass(), "collect", "未找到排班记录");
				return;
			}
			String begin_hour = fdao.getFieldValue("begin_hour");
			String begin_time = fdao.getFieldValue("begin_time");
			String end_hour = fdao.getFieldValue("end_hour");
			String end_time = fdao.getFieldValue("end_time");

			String rest_begin_hour = fdao.getFieldValue("rest_begin_hour");
			String rest_begin_time = fdao.getFieldValue("rest_begin_time");
			String rest_end_hour = fdao.getFieldValue("rest_end_hour");
			String rest_end_time = fdao.getFieldValue("rest_end_time");

			String rest = fdao.getFieldValue("rest");

			int flex_minute = StrUtil.toInt(fdao.getFieldValue("flex_minute"), 0);

			String sbsj, xbsj;
			String sbsj2 = "", xbsj2 = "";
			// 如果中间休息
			if ("是".equals(rest)) {
				sbsj = begin_hour + ":" + begin_time;
				xbsj = rest_begin_hour + ":" + rest_begin_time;
				sbsj2 = rest_end_hour + ":" + rest_end_time;
				xbsj2 = end_hour + ":" + end_time;
			}
			else {
				sbsj = begin_hour + ":" + begin_time;
				xbsj = end_hour + ":" + end_time;
			}

			// 根据人员编号查询ft_kaoqin_sign_time表中的考勤数据
			String sql;
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			boolean isUseKqj = cfg.getBooleanProperty("isUseKqj");
			if (isUseKqj) {
				sql = "select number,name,sign_time,sign_type,is_supplement from ft_kaoqin_time_sign where number = " + StrUtil.sqlstr(personNo);
			}
			else {
				sql = "select number,name,sign_time,sign_type,is_supplement from ft_kaoqin_time_sign where name = " + StrUtil.sqlstr(userName);
			}
			sql += " and sign_time >= "+SQLFilter.getDateStr(workDateString+" 00:00:00", "yyyy-MM-dd HH:mm:ss")+" and sign_time <= "+SQLFilter.getDateStr(workDateString+" 23:59:59", "yyyy-MM-dd HH:mm:ss")+" order by sign_time";
			ResultIterator signIterator = jt.executeQuery(sql);
			if (signIterator.size()==0) { // 如果当天没有考勤记录
				int txCount = txDataCollect(personNo, userName, workDate);                   // 判断是否有调休
				int tripCount = tripDataCollect(personNo, userName, workDate);               // 判断是否有出差
				int leaveCount = leaveDataCollect(personNo, userName, workDate);             // 判断是否请假
				int wcCount = wcDataCollect(personNo, userName, workDate);					  // 判断是否有外出申请
				int jbCount = jbDataCollect(personNo, userName, workDate);					  // 汇总加班数据
				// 旷工无需判断是否有加班记录，因为有加班必定有考勤
				if (txCount == -1 && tripCount == -1 && leaveCount == -1 && wcCount == -1) {
					// 记录旷工
					jt.executeUpdate(insertSQL, new Object[] {personNo, userName,
							workDateString, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 , 0, 0, 0, 0, 0, 0});
				}
			} else {
				insertDataByRecord(signIterator, sbsj, xbsj, sbsj2, xbsj2, flex_minute, userName);
			}
		}
	}
	
	/**
	 * 当存在考勤记录时统计数据
	 * @param result 某工作日某用户的考勤记录集合
	 * @param sbsj 上班时间
	 * @param xbsj 下班时间
	 * @param sbsj2 上班时间2 为空表示中间没有休息时间
	 * @param xbsj2 下班时间2
	 * @param flex_minute 弹性时间
     * @param userName 用户名
     */
	public void insertDataByRecord(ResultIterator result, String sbsj, String xbsj, String sbsj2, String xbsj2, int flex_minute, String userName ){
		int earlycount = 0;        	// 正常打卡0  早退1
		int latecount = 0;         	// 正常打卡0  迟到1
		int supplemeneCount = 0; 	// 补签次数
		String kqsbsj = null;
		String kqxbsj = null;
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			boolean sbflag = false;			   // 签到标志位
			boolean xbflag = false;			   // 签退标志位
			String account = "";
			String dateString = "";
			while(result.hasNext()) {
				ResultRecord signRecord = (ResultRecord)result.next();
				String accountId = signRecord.getString(1);
				account = accountId;
				String name = signRecord.getString(2);
				Date sign_time = signRecord.getDate(3); // 考勤时间
				int sign_type = signRecord.getInt(4); // 考勤类型，第一次上/下班，第二次上/下班
				boolean isSupplement = signRecord.getInt(5)==1; // 补签
				if (isSupplement) {
					supplemeneCount ++;
				}

				String sign_timeString = DateUtil.format(sign_time, "yyyy-MM-dd HH:mm:ss");
				String[] sign_timeArr = sign_timeString.split(" ");
				dateString = sign_timeArr[0]; // 日期
				Date sbDate = null, xbDate = null, sbDate2=null, xbDate2=null;
				if (sign_timeArr!=null){
					String sb = sign_timeArr[0] + " " + sbsj + ":00";
					String xb = sign_timeArr[0] + " " + xbsj + ":00";
					sbDate = DateUtil.parse(sb, "yyyy-MM-dd HH:mm:ss");
					xbDate = DateUtil.parse(xb, "yyyy-MM-dd HH:mm:ss");
					
					String sb2 = sign_timeArr[0] + " " + sbsj2 + ":00";
					String xb2 = sign_timeArr[0] + " " + xbsj2 + ":00";
					sbDate2 = DateUtil.parse(sb2, "yyyy-MM-dd HH:mm:ss");
					xbDate2 = DateUtil.parse(xb2, "yyyy-MM-dd HH:mm:ss");
				}

				// 此处因涉及kqsbsj kqxbsj及sbflag xbflag	，所以未用AttendanceMgr.checkByShift
				int minutes = 0;
				if (sign_type == AttendanceMgr.TYPE_ON_DUTY_1) {
					if (sign_time.after(sbDate)) { // 第一次上班签到且迟到的才记录
						int m = DateUtil.datediffMinute(sign_time, sbDate);
						if (m>flex_minute) {
							latecount += 1;
							minutes = m;
						}
					}
					kqsbsj = sign_timeString;
					sbflag = true;
				}
				else if (sign_type == AttendanceMgr.TYPE_OFF_DUTY_1) {
					if (sign_time.before(xbDate)) {
						int m = DateUtil.datediffMinute(xbDate, sign_time);
						if (m>flex_minute) { // 早退
							earlycount += 1;
							minutes = m;
						}
					}
					// 如果一天仅上一次班
					if ("".equals(sbsj2)) {
						kqxbsj = sign_timeString;
						xbflag = true;
					}
				}
				else if (sign_type == AttendanceMgr.TYPE_ON_DUTY_2) {
					if (sign_time.after(sbDate2)) { // 第二次上班签到且迟到的才记录
						int m = DateUtil.datediffMinute(sign_time, sbDate2);
						if (m>flex_minute) {
							latecount += 1;
							minutes = m;
						}
					}				
				}
				else if (sign_type == AttendanceMgr.TYPE_OFF_DUTY_2) {
					if (sign_time.before(xbDate2)) {
						int m = DateUtil.datediffMinute(xbDate2, sign_time);			
						if (m>flex_minute) { // 第二次下班早退
							earlycount += 1;
							minutes = m;
						}
					}
					kqxbsj = sign_timeString;
					xbflag = true;
				}
				int id = getKaoqinArrangeId(accountId, sign_time);
				// kaoqin_arrange中的sbsj、xbsj应该是沒用了，因爲已经采用了班次，一天中可能會有兩次上班或下班
				if (id == -1) {
					jt.executeUpdate(insertSQL, new Object[]{accountId, name,
							sign_timeArr[0], earlycount, latecount, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, kqsbsj, kqxbsj, 0, 0, minutes, supplemeneCount});
				} else {
					String set = " set earlycount = "+earlycount+",latecount = "+latecount + ",supplement_count=" + supplemeneCount;
					if (kqsbsj!=null) {
						set += ",sbsj = "+SQLFilter.getDateStr(kqsbsj, "yyyy-MM-dd HH:mm:ss");
					}
					if (kqxbsj!=null)
						set += ",xbsj = "+SQLFilter.getDateStr(kqxbsj, "yyyy-MM-dd HH:mm:ss");
					String updateSQL = "update kaoqin_arrange "+set+" where id = "+ id;
					jt.executeUpdate(updateSQL);
				}
			}
			Date d = DateUtil.parse(dateString, "yyyy-MM-dd");
			int id = getKaoqinArrangeId(account, d); // 再取1次，因为之前可能新生成了一条记录，ID是未知的
			if (id != -1 ) {
				if (sbflag && xbflag) {                    // 如果上班、下班考勤记录都有, 则记录正常出勤一天
					String updateSQL = "update kaoqin_arrange set days = 1 where id = "+ id;
					jt.executeUpdate(updateSQL);
				}

				int txCount = txDataCollect(account, userName, d);                   // 汇总调休
				int tripCount = tripDataCollect(account, userName, d);               // 汇总出差
				int leaveCount = leaveDataCollect(account, userName, d);             // 汇总请假
				int wcCount = wcDataCollect(account, userName, d);                   // 汇总外出
				int jbCount = jbDataCollect(account, userName, d);                   // 汇总加班
				if (txCount == -1 && tripCount == -1 && leaveCount == -1
						&& wcCount == -1) {
					// 存在缺勤
					String updateSQL = "update kaoqin_arrange set abscount = 1 where id = "+ id;
					jt.executeUpdate(updateSQL);
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("获取正常打卡考勤数据汇总出现异常："+StrUtil.trace(e));
		} finally {
			if (jt!=null) {
				jt.close();
			}
		}
	}

	/**
	 * 取得workDate的工作日天数，并根据上午、下午判断是否有半天的情况
	 * @param workDate
	 * @param startDate
	 * @param startAmpm
	 * @param endDate
	 * @param endAmpm
     * @return
     */
	public float getDay(Date workDate, Date startDate, String startAmpm, Date endDate, String endAmpm) {
		if (OACalendarDb.isWorkday(workDate)) {
			// 如果workDate是开始日期
			if (DateUtil.compare(workDate, startDate) == 0) {
				if (startAmpm.equals("pm")) {
					return 0.5f;
				} else {
					return 1;
				}
			} else if (DateUtil.compare(workDate, endDate) == 0) {
				// 如果workDate是结束日期
				if (endAmpm.equals("am")) {
					return 0.5f;
				} else {
					return 1;
				}
			} else {
				return 1;
			}
		} else {
			return 0;
		}
	}
	
	/**
	 * 差旅时间汇总
	 * @param accountId
	 * @param userName
	 * @param workDate
	 * @return  影响的记录数量
	 */
	public int tripDataCollect(String accountId, String userName, Date workDate){
		int count = -1;
		int id = getKaoqinArrangeId(accountId, workDate);
		SimpleDateFormat sf = new SimpleDateFormat ("yyyy-MM-dd");
		String workDateString = sf.format(workDate);
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			if (!OACalendarDb.isWorkday(workDate)) {
				if (id != -1) {
					String updateSQL = "update kaoqin_arrange set tripday=0 where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = insertBlankRecord(jt, accountId, userName, workDate);
				}
				return count;
			}

			String sql = "select id,start_date,start_date_ampm,end_date,end_date_ampm from ft_trip_apply where apply = "+SQLFilter.sqlstr(userName)+" and start_date <= "+SQLFilter.getDateStr(workDateString,
				"yyyy-MM-dd")+" and end_date >= "+SQLFilter.getDateStr(workDateString,"yyyy-MM-dd")+" and cws_status = 1";
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				Date startDate = rr.getDate(2);
				String startAmpm = rr.getString(3);
				Date endDate = rr.getDate(4);
				String endAmpm = rr.getString(5);
				float tripday = getDay(workDate, startDate, startAmpm, endDate, endAmpm);
				if (id != -1) {
					String updateSQL = "update kaoqin_arrange set tripday = " + tripday + " where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
							workDateString, 0, 0, 0, 0, tripday, 0, 0, 0, 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0, 0});
				}
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("差旅考勤汇总："+StrUtil.trace(e));
		} finally {
			jt.close();
		}
		return count;
	}
	/**
	 * 请假时间汇总
	 * @param accountId
	 * @param userName
	 * @param workDate
	 * @return  影响的记录数量
	 */
	public int leaveDataCollect(String accountId,String userName,Date workDate){
		int count = -1;
		SimpleDateFormat sf = new SimpleDateFormat ("yyyy-MM-dd");
		String workDateString = sf.format(workDate);
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			int id = getKaoqinArrangeId(accountId, workDate);
			if (!OACalendarDb.isWorkday(workDate)) {
				if (id != -1) {
					String updateSQL = "update kaoqin_arrange set sickday=0,thingday=0,yearday=0,marryday=0,maternityday=0,otherday=0 where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = insertBlankRecord(jt, accountId, userName, workDate);
				}
				return count;
			}

			String sql ="select jqlb,qjkssj,qjkssj_ampm,qjjssj,qjjssj_ampm from ft_qjsqd where applier = "+SQLFilter.sqlstr(userName)+" and qjkssj <= "+SQLFilter.getDateStr(workDateString,
					"yyyy-MM-dd")+" and qjjssj >= "+SQLFilter.getDateStr(workDateString,"yyyy-MM-dd")+" and cws_status = 1";
			ResultIterator leaveIterator = jt.executeQuery(sql);
			if (leaveIterator.hasNext()) {
				ResultRecord rd = (ResultRecord)leaveIterator.next();
				Date startDate = rd.getDate(2);
				String startAmpm = rd.getString(3);
				Date endDate = rd.getDate(4);
				String endAmpm = rd.getString(5);

				float day = getDay(workDate, startDate, startAmpm, endDate, endAmpm);

				String jqlb = rd.getString(1);
				if ("病假".equals(jqlb)) {
					if (id != -1) {
						String updateSQL = "update kaoqin_arrange set sickday = " + day + " where id = "+id;
						count = jt.executeUpdate(updateSQL);
					} else {
						count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
								workDateString, 0, 0, 0, 0, 0, 0, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
					}
				}
				else if ("事假".equals(jqlb)){
					if (id != -1) {
						String updateSQL = "update kaoqin_arrange set thingday = " + day + " where id = "+id;
						count = jt.executeUpdate(updateSQL);
					} else {
						count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
								workDateString, 0, 0, 0, 0, 0, 0, 0, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
					}
				}
				else if ("年假".equals(jqlb)){
					if (id != -1){
						String updateSQL = "update kaoqin_arrange set yearday = " + day + " where id = "+id;
						count = jt.executeUpdate(updateSQL);
					} else {
						count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
								workDateString, 0, 0, 0, 0, 0, 0, 0, 0, day, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
					}
				}
				else if ("婚假".equals(jqlb)){
					if (id != -1){
						String updateSQL = "update kaoqin_arrange set marryday = " + day + " where id = "+id;
						count = jt.executeUpdate(updateSQL);
					} else {
						count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
								workDateString, 0, 0, 0, 0, 0, 0, 0, 0, 0, day, 0, 0, 0, 0, 0, 0, 0, 0, 0});
					}
				}
				else if ("产假".equals(jqlb)){
					if (id != -1){
						String updateSQL = "update kaoqin_arrange set maternityday = " + day + " where id = "+id;
						count = jt.executeUpdate(updateSQL);
					} else {
						count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
								workDateString, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, day, 0, 0, 0, 0, 0, 0, 0, 0});
					}
				}
				else  {
					if (id != -1) {
						String updateSQL = "update kaoqin_arrange set otherday = " + day + " where id = "+id;
						count = jt.executeUpdate(updateSQL);
					} else {
						count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
								workDateString, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, day, 0, 0, 0, 0, 0, 0, 0});
					}
				}
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("请假考勤汇总："+StrUtil.trace(e));
		} finally {
			jt.close();
		}
		return count;
	}

	/**
	 * 加班时间汇总，此处汇总出来的是加班的次数，没有太大意义
	 * @param accountId
	 * @param userName
	 * @param workDate
     * @return
     */
	public int jbDataCollect(String accountId, String userName, Date workDate) {
		int count = -1;
		// 因为加班涉及到节假日，不能单纯地以加班小时记，如果开始时间与结束时间跨天
		// 比如从周五晚7点至周六晚7点，则不太好计算加班时间，所以只能手工输入
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			int id = getKaoqinArrangeId(accountId, workDate);
			if (!OACalendarDb.isWorkday(workDate)) {
				if (id != -1) {
					String updateSQL = "update kaoqin_arrange set workday=0 where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = insertBlankRecord(jt, accountId, userName, workDate);
				}
				return count;
			}
			// 加班不会跨天，故以加班开始时间作为判断依据
			String sql = "select jb.kssj,jb.day_count,jb.jssj from ft_jbsqd jb where jb.applier=" + StrUtil.sqlstr(userName) + " and jb.kssj >= "
					+ SQLFilter.getDateStr(DateUtil.format(workDate, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss") + " and jb.kssj <= "+SQLFilter.getDateStr(DateUtil.format(workDate, "yyyy-MM-dd 23:59:59"), "yyyy-MM-dd HH:mm:ss") + " and jb.result=1 and jb.cws_status = 1";
			ResultIterator ri = jt.executeQuery(sql);
			// day_count为加班时长（小时）
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				double h = rr.getDouble(2);
				if (id == -1) {
					count = jt.executeUpdate(insertSQL, new Object[]{accountId, userName,
							workDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, h, 0, 0, 0, 0, 0, 0});
				} else {
					String updateSQL = "update kaoqin_arrange set workday = " + h + " where id = " + id;
					count = jt.executeUpdate(updateSQL);
				}
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("加班时间汇总出现异常："+StrUtil.trace(e));
		} finally {
			jt.close();
		}
		return count;
	}

	/**
	 * 调休时间汇总 
	 * @param accountId
	 * @param userName
	 * @param workDate
	 * @return 影响的记录数量
	 */
	public int txDataCollect(String accountId,String userName,Date workDate) {
		int count = -1;
		SimpleDateFormat sf = new SimpleDateFormat ("yyyy-MM-dd");
		String workDateString = sf.format(workDate);
		String sql = "select txsc from ft_txsqd where txsqr="+StrUtil.sqlstr(userName)+" and startrq <= "+SQLFilter.getDateStr(workDateString+" 23:59:59", "yyyy-MM-dd HH:mm:ss")+
			" and endrq >= "+SQLFilter.getDateStr(workDateString+" 00:00:00", "yyyy-MM-dd HH:mm:ss")+" and cws_status = 1";
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			int id = getKaoqinArrangeId(accountId, workDate); // 查询指定日期和工号是否存在记录
			if (!OACalendarDb.isWorkday(workDate)) {
				if (id != -1) {
					String updateSQL = "update kaoqin_arrange set txsc=0 where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = insertBlankRecord(jt, accountId, userName, workDate);
				}
				return count;
			}

			ResultIterator txIterator = jt.executeQuery(sql);
			if (txIterator.hasNext()) {
				ResultRecord rd = (ResultRecord)txIterator.next();
				// int txsc = rd.getInt(1);             // 调休时长
				if (id != -1) {
					String updateSQL = "update kaoqin_arrange set txsc = 1 where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
							workDateString, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,1, 0, 0, 0, 0, 0});
				}
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("调休时间汇总出现异常："+StrUtil.trace(e));
		} finally{
			jt.close();
		}
		return count;
	}

	/**
	 * 外出汇总
	 * @param accountId
	 * @param userName
	 * @param workDate
	 * @return
	 */
	public int wcDataCollect(String accountId,String userName, Date workDate){
		int count = -1;
		SimpleDateFormat sf = new SimpleDateFormat ("yyyy-MM-dd");
		String workDateString = sf.format(workDate);
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			int id = getKaoqinArrangeId(accountId, workDate); // 查询指定日期和工号是否存在记录
			if (!OACalendarDb.isWorkday(workDate)) {
				if (id != -1) {
					String updateSQL = "update kaoqin_arrange set wcday=0 where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = insertBlankRecord(jt, accountId, userName, workDate);
				}
				return count;
			}

			String sql = "select id from ft_wcsq wc where wc.applier = "+StrUtil.sqlstr(userName)+" and wc.qjkssj <= "+SQLFilter.getDateStr(workDateString+" 23:59:59", "yyyy-MM-dd HH:mm:ss")+
					" and wc.qjjssj >= "+SQLFilter.getDateStr(workDateString+" 00:00:00", "yyyy-MM-dd HH:mm:ss")+" and cws_status = 1";
			ResultIterator txIterator = jt.executeQuery(sql);
			if (txIterator.hasNext()) {
				ResultRecord rd = (ResultRecord)txIterator.next();
				float day = 1;
				if (id != -1) {
					// wcday=1 表示当前日期存在外出记录
					String updateSQL = "update kaoqin_arrange set wcday = " + day + " where id = "+id;
					count = jt.executeUpdate(updateSQL);
				} else {
					count = jt.executeUpdate(insertSQL, new Object[] {accountId, userName,
							workDateString, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, day, 0, 0, 0, 0});
				}
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("外出汇总出现异常："+StrUtil.trace(e));
		} finally{
			jt.close();
		}
		return count;
	}

	/**
	 * 根据工号和日期查询汇总表中是否存在记录
	 * @param accountId   用户编号
	 * @param workdate	  工作日期
	 * @return            记录的id 
	 */
	public int getKaoqinArrangeId(String accountId, Date workdate){
		int id = -1;
		String workDateString = new SimpleDateFormat("yyyy-MM-dd").format(workdate);
		String sql = "select id from kaoqin_arrange where number = "+StrUtil.sqlstr(accountId)+" and date = "+SQLFilter.getDateStr(workDateString, "yyyy-MM-dd");
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()){
				ResultRecord rd = (ResultRecord)ri.next();
				id = rd.getInt(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("获取考勤id出现异常："+StrUtil.trace(e));
		}
		return id ;
	}
	
	/**
	 * 获取上一个月的开始日期和结束日期
	 * @return  {begindate,enddate}
	 */
	public String[] getPreMonDate(){
		Calendar currDateTime = Calendar.getInstance();
		currDateTime.setTime(new Date());
		currDateTime.add(Calendar.MONTH,-1);
		int  year = currDateTime.get(Calendar.YEAR);
		int  month = currDateTime.get(Calendar.MONTH)+1;
		String monthString = "";
		if (month<10)
			monthString = "0"+month;
		else 
			monthString = month + "";
		int minDay = currDateTime.getMinimum(Calendar.DATE);
		String minDayString = "";
		if (minDay<10)
			minDayString = "0"+minDay;
		else 
			minDayString = minDay + "";
		int maxDay = currDateTime.getMaximum(Calendar.DATE);
		String begindate = year+"-"+monthString+"-"+minDayString;
		String enddate = year+"-"+monthString+"-"+maxDay;
		return new String[]{begindate,enddate};
	}

	/**
	 * 根据工作日历取得工作天数
	 * @param startDate 开始日期
	 * @param startAmpm 开始时段am/pm
	 * @param endDate 结束日期
	 * @param endAmpm 结束时段am/pm
	 * @return
     * @throws ErrMsgException
     */
	public static double getWorkDays(Date startDate, String startAmpm, Date endDate, String endAmpm) throws ErrMsgException {
		if (DateUtil.compare(startDate, endDate)==1) {
			throw new ErrMsgException("开始日期不能大于结束日期！");
		}

		com.redmoon.oa.oacalendar.OACalendarDb oacal = new com.redmoon.oa.oacalendar.OACalendarDb();
		double days = 0;
		// 如果相差天数大于1天，getWorkDayCout从startDate + 1天开始算起
		if (DateUtil.datediff(endDate, startDate)>1) {
			days = oacal.getWorkDayCount(startDate, DateUtil.addDate(endDate, -1));
		}
		if (DateUtil.compare(startDate, endDate)==0) {
			if (startAmpm.equalsIgnoreCase("pm") && endAmpm.equalsIgnoreCase("am")) {
				throw new ErrMsgException("时段不正确，应该是上午在前，下午在后");
			}
			else {
				if (startAmpm.equalsIgnoreCase("am") && endAmpm.equalsIgnoreCase("pm")) {
					days = 1;
				}
				else {
					days = 0.5;
				}
			}
		}
		else {
			// 如果开始日期是工作日
			if (OACalendarDb.isWorkday(startDate)) {
				if (startAmpm.equalsIgnoreCase("pm")) {
					days += 0.5;
				}
				else {
					days += 1;
				}
			}
			// 如果结束日期是工作日
			if (OACalendarDb.isWorkday(endDate)) {
				if (endAmpm.equalsIgnoreCase("pm")) {
					days += 1;
				}
				else {
					days += 0.5;
				}
			}
		}
		return days;
	}

	/**
	 * 取得某月的假期天数
	 * @param userName 用户名
	 * @param year 年份
	 * @param month 月份 开始于1
	 * @param jqlb 假期类别
	 * @return
     * @throws ErrMsgException
     */
	public static double getLeaveDays(String userName, int year, int month, String jqlb) throws ErrMsgException {
		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect("leave_type");
		Vector v = sd.getOptions(new JdbcTemplate());
		Iterator ir = v.iterator();
		boolean isFound = false;
		while (ir.hasNext()) {
			SelectOptionDb sod = (SelectOptionDb) ir.next();
			if (jqlb.equals(sod.getName())) {
				isFound = true;
				break;
			}
		}

		if (!isFound) {
			throw new ErrMsgException("假期类别参数" + jqlb + "不正确！");
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("病假", "");

		Date bd = DateUtil.getDate(year, month-1, 1);
		int days = DateUtil.getDayCount(year, month-1);
		Date ed = DateUtil.addDate(bd, days-1);

		double ts = 0; // 假期天数
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select qjkssj, qjjssj, qjkssj_ampm, qjjssj_ampm, jqlb from ft_qjsqd where applier=? and qjkssj <=? and qjjssj >= ? and jqlb=? and cws_status = 1";
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[]{userName, ed, bd, jqlb});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				// 取出本月的天数
				Date startDate = rr.getDate(1);
				Date endDate = rr.getDate(2);
				String startAmpm = rr.getString(3);
				String endAmpm = rr.getString(4);

				if (DateUtil.compare(startDate, bd)==2) {
					startDate = bd;
				}
				if (DateUtil.compare(endDate, ed)==1) {
					endDate = ed;
				}

				ts += SignMgr.getWorkDays(startDate, startAmpm, endDate, endAmpm);
			}
		} catch (SQLException e) {
			LogUtil.getLog(SignMgr.class).error(e);
		}

		return ts;
	}

	/**
	 * 取得某月的出差天数
	 * @param userName
	 * @param year
	 * @param month
	 * @return
	 * @throws ErrMsgException
     */
	public static double getTripDays(String userName, int year, int month) throws ErrMsgException {
		Date bd = DateUtil.getDate(year, month-1, 1);
		int days = DateUtil.getDayCount(year, month-1);
		Date ed = DateUtil.addDate(bd, days-1);

		double ccts = 0; // 出差天数
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select start_date, end_date from ft_trip_apply where apply=? and start_date <=? and end_date >= ? and cws_status = 1";
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[]{userName, ed, bd});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				// 取出本月的天数
				Date startDate = rr.getDate(1);
				Date endDate = rr.getDate(2);
				if (DateUtil.compare(startDate, bd)==2) {
					startDate = bd;
				}
				if (DateUtil.compare(endDate, ed)==1) {
					endDate = ed;
				}
				ccts += DateUtil.datediff(endDate, startDate);
			}
		} catch (SQLException e) {
			LogUtil.getLog(SignMgr.class).error(e);
		}

		return ccts;
	}

	/**
	 * 取得某月的外出
	 * @param userName
	 * @param year
	 * @param month
	 * @return
	 * @throws ErrMsgException
     */
	public static double getWcDays(String userName, int year, int month) throws ErrMsgException {
		Date bd = DateUtil.getDate(year, month-1, 1);
		int days = DateUtil.getDayCount(year, month-1);
		Date ed = DateUtil.addDate(bd, days-1);

		double ts = 0; // 出差天数

		String sql = "select id from ft_wcsq wc where wc.applier = ? and wc.qjkssj <= ? and wc.qjjssj >=? and cws_status = 1";
		ResultIterator ri = null;
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ri = jt.executeQuery(sql, new Object[]{userName, ed, bd});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				// 取出本月的天数
				Date startDate = rr.getDate(1);
				Date endDate = rr.getDate(2);
				if (DateUtil.compare(startDate, bd)==2) {
					startDate = bd;
				}
				if (DateUtil.compare(endDate, ed)==1) {
					endDate = ed;
				}
				ts += DateUtil.datediff(endDate, startDate);
			}
		} catch (SQLException e) {
			LogUtil.getLog(SignMgr.class).error(e);
		}

		return ts;
	}

	/**
	 * 统计迟到、早退的次数及时长
	 * @param userName
	 * @param year
	 * @param month
	 * @param kind  earlycount latecount
	 * @return int[] 迟到latecount或早退earlycount，时长minutes
     * @throws ErrMsgException
     */
	public static int[] getAttendanceDays(String userName, int year, int month, String kind) throws ErrMsgException {
		Date bd = DateUtil.getDate(year, month-1, 1);
		int days = DateUtil.getDayCount(year, month-1);
		Date ed = DateUtil.addDate(bd, days-1);

		int[] r = new int[2];
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select count(*), sum(duration) from kaoqin_arrange where name=? and date <=? and date >= ? and " + kind + "=1";
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[]{userName, ed, bd});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				r[0] = rr.getInt(1);
				r[1] = rr.getInt(2);
			}
		} catch (SQLException e) {
			LogUtil.getLog(SignMgr.class).error(e);
		}

		return r;
	}

	/**
	 * 统计考勤相关数据
	 * @param userName
	 * @param year
	 * @param month
	 * @return double[] 出勤days 旷工nocount 缺勤abscount
	 * @throws ErrMsgException
	 */
	public static int[] getAttendanceDays(String userName, int year, int month) throws ErrMsgException {
		Date bd = DateUtil.getDate(year, month-1, 1);
		int days = DateUtil.getDayCount(year, month-1);
		Date ed = DateUtil.addDate(bd, days-1);

		int[] r = new int[6];
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select sum(days), sum(nocount), sum(abscount) from kaoqin_arrange where name=? and date <=? and date >= ?";
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[]{userName, ed, bd});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				r[0] = rr.getInt(1);
				r[1] = rr.getInt(2);
				r[2] = rr.getInt(3);
			}
		} catch (SQLException e) {
			LogUtil.getLog(SignMgr.class).error(e);
		}

		return r;
	}

	/**
	 * 取得某月的补签次数
	 * @param userName
	 * @param year
	 * @param month
	 * @return
	 * @throws ErrMsgException
     */
	public int getSupplementCounts(String userName, int year, int month) throws ErrMsgException {
		Date bd = DateUtil.getDate(year, month-1, 1);
		int days = DateUtil.getDayCount(year, month-1);
		Date ed = DateUtil.addDate(bd, days-1);

		int r = 0;
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select count(*) from ft_kaoqin_supplement where name=? and date <=? and date >= ? and cws_status = 1";
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[]{userName, ed, bd});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				r = rr.getInt(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return r;
	}

	/**
	 * 补签
	 * @param userName
	 * @param bqsj 补签时间，格式为 yyyy-MM-dd HH:mm:ss
     */
	public static boolean supplement(String userName, String bqsj) {
		UserDb user = new UserDb();
		user = user.getUserDb(userName);

		String personNo = user.getPersonNo();

		Date supplementTime = DateUtil.parse(bqsj, "yyyy-MM-dd HH:mm:ss");
		Date workDate = DateUtil.parse(DateUtil.format(supplementTime, "yyyy-MM-dd"), "yyyy-MM-dd");
		if (!OACalendarDb.isWorkday(workDate)) {
			return false;
		}

		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			// 插入补签的考勤数据至考勤明细表
			FormDb fd =new FormDb();
			fd = fd.getFormDb("kaoqin_time_sign");

			DeptUserDb dud = new DeptUserDb();
			String unitCode = dud.getUnitOfUser(userName).getCode();

			int[] signType = AttendanceMgr.getPunchType(userName, supplementTime);

			FormDAO fdao = new FormDAO(fd);
			fdao.setFieldValue("number", personNo);
			fdao.setFieldValue("name", userName);
			fdao.setFieldValue("minutes", "0");
			fdao.setFieldValue("sign_time", bqsj);
			fdao.setFieldValue("sign_type", String.valueOf(signType[0]));
			fdao.setFieldValue("sign_result", String.valueOf(AttendanceMgr.NORMAL));
			fdao.setFieldValue("is_supplement", "1");
			fdao.setCreator(UserDb.SYSTEM);
			fdao.setUnitCode(unitCode);
			fdao.create();

			// 插入或刷新当天的汇总数据
			SignMgr sm = new SignMgr();
			sm.collect(jt, userName, personNo, workDate);
		} catch (Exception e) {
			LogUtil.getLog(SignMgr.class).error("补签异常："+StrUtil.trace(e));
		} finally{
			jt.close();
		}
		return true;
	}
}
