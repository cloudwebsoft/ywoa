package com.redmoon.oa.attendance;

import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.module.cms.Config;
import cn.js.fan.util.CFGParser;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.chat.chatservlet;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.kaoqin.KaoqinMgr;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.FormDAO;
import com.sun.org.apache.bcel.internal.generic.FDIV;

public class AttendanceMgr {
	/**
	 * 正常
	 */
	public static final int NORMAL = 0;
	/**
	 * 迟到
	 */
	public static final int LATE = 1;
	/**
	 * 早退
	 */
	public static final int EARLY = 2;
	
	/*
	public static final int TYPE_1 = 1;
	public static final int TYPE_2 = 2;
	public static final int TYPE_3 = 3;
	public static final int TYPE_4 = 4;
	public static final int TYPE_5 = 5;
	public static final int TYPE_6 = 6;
	*/
	
	/**
	 * 上班
	 */
	public static final int TYPE_ON_DUTY_1 = 1;
	
	/**
	 * 下班然后中间休息
	 */
	public static final int TYPE_OFF_DUTY_1 = 2;
	
	/**
	 * 中间休息完上班
	 */
	public static final int TYPE_ON_DUTY_2 = 3;
	
	/**
	 * 下班
	 */
	public static final int TYPE_OFF_DUTY_2 = 4;
	
	/**
	 * 打卡
	 * @param userName
	 * @param lat
	 * @param lng
	 * @param type 上班、下班，中间休息上班、下班 或当用工作日历时，上午上班、上午下班、下午上班、下午下班、晚上上班、晚上下班
	 * @return long[] 0存储迟到或早退，1存储迟到或早退的分钟数，2存储考勤记录的ID
	 * @throws ErrMsgException 
	 */
	public static long[] punch(String userName, double lat, double lng, String address, int type) throws ErrMsgException {		
		// 判断是否在考勤指定的距离点范围内
		Date dt = new Date();
		Object[] ary = ShiftScheduleMgr.getShiftDAO(userName, dt);
		FormDAO fdao = (FormDAO)ary[0];
		
		int radius = StrUtil.toInt(fdao.getFieldValue("radius"), -1);
		
		// 位置异常时是否允许打卡
		boolean isAbnormal = "1".equals(fdao.getFieldValue("is_abnormal"));
		
		String location = fdao.getFieldValue("location");
		String[] locAry = StrUtil.split(location, ",");
		if (locAry==null) {
			return new long[] {-1, 0, -1}; // 未指定位置，不需要打卡
		}
		
		double lngLoc = StrUtil.toDouble(locAry[0]);
		double latLoc = StrUtil.toDouble(locAry[1]);
		
		double r = BMapUtil.getDistanceFromTwoPoints(lat, lng, latLoc, lngLoc);
		
		boolean isDistanceValid = false;
		if (r<radius) {
			// 有效距离范围内
			isDistanceValid = true;
		}

		if (!isDistanceValid && !isAbnormal) {
			throw new ErrMsgException("位置异常");
		}
		
		// 根据用户班次，写入考勤信息
		if (isDistanceValid || (!isDistanceValid && isAbnormal)) {
			Date d = new Date();
			long[] aryResult = new long[3];
			long[] aryCheck = check(userName, d, type, isDistanceValid);
			aryResult[0] = aryCheck[0];
			aryResult[1] = aryCheck[1];
			
			// 插入考勤记录
			UserDb userDb = new UserDb();
			userDb = userDb.getUserDb(userName);
			String personNo = userDb.getPersonNo();
			FormDb fd = new FormDb();
			fd = fd.getFormDb("kaoqin_time_sign");
			FormDAO fdaoKq = new FormDAO(fd);
			fdaoKq.setFieldValue("name", userName);
			fdaoKq.setFieldValue("number", personNo);
			// fdaoKq.setFieldValue("remark", "");
			fdaoKq.setFieldValue("is_loc_abnormal", isDistanceValid?"0":"1");
			// fdaoKq.setFieldValue("exception", lat + "," + lng);
			fdaoKq.setFieldValue("sign_time", DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"));
			fdaoKq.setFieldValue("sign_type", String.valueOf(type));
			if (isDistanceValid) {
				fdaoKq.setFieldValue("location", location);
			}
			else {
				fdaoKq.setFieldValue("location", lng + "," + lat + "," + address);
			}
			// 20181104 fgf 增加考勤结果
			fdaoKq.setFieldValue("sign_result", String.valueOf(aryCheck[0]));
			if (aryCheck[0]==LATE || aryCheck[0]==EARLY) {
				long m = Math.abs(aryCheck[1]);
				fdaoKq.setFieldValue("minutes", String.valueOf(m));
			}
			fdaoKq.create();
			
			aryResult[2] = fdaoKq.getId();
			
			return aryResult;
		}
		return new long[] {-1, 0, -1};
	}
	
	public static String getPunchTypeDesc(int type) {
		switch (type){
		case TYPE_ON_DUTY_1:
		case TYPE_ON_DUTY_2:
			return "上班";
		case TYPE_OFF_DUTY_1:
		case TYPE_OFF_DUTY_2:
			return "下班";
		default:
			return "";
		}
	}
	
	/**
	 * 取得當天上一次考勤的類型
	 * @param userName
	 * @param dt
	 * @return
	 */
	public static int getLastPunchType(String userName, java.util.Date dt) {
		String workDateString = DateUtil.format(dt, "yyyy-MM-dd");
		// 根据人员编号查询form_table_kaoqin_sign_time表中的考勤数据
		String sql = "select sign_type from form_table_kaoqin_time_sign where name = "+StrUtil.sqlstr(userName)+
			" and sign_time >= "+SQLFilter.getDateStr(workDateString +" 00:00:00", "yyyy-MM-dd HH:mm:ss")+" and sign_time <= "+SQLFilter.getDateStr(workDateString+" 23:59:59", "yyyy-MM-dd HH:mm:ss")+" order by sign_time desc";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getInt(1);
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * 取得當前可以打卡的類型
	 * @param userName
	 * @return
	 */
	public static int getPunchType(String userName, Date kqTime) {
		Object[] aryShift = ShiftScheduleMgr.getShiftDAO(userName, kqTime);
		if (aryShift!=null) {
			FormDAO fdao = (FormDAO)aryShift[0];
			if (fdao!=null) {
				int[] ary = new int[2];
				String begin_hour = fdao.getFieldValue("begin_hour");
				String begin_time = fdao.getFieldValue("begin_time");
				String end_hour = fdao.getFieldValue("end_hour");
				String end_time = fdao.getFieldValue("end_time");
				
				String rest_begin_hour = fdao.getFieldValue("rest_begin_hour");
				String rest_begin_time = fdao.getFieldValue("rest_begin_time");
				String rest_end_hour = fdao.getFieldValue("rest_end_hour");
				String rest_end_time = fdao.getFieldValue("rest_end_time");
				
				String rest = fdao.getFieldValue("rest");
				// int flex_minute = StrUtil.toInt(fdao.getFieldValue("flex_minute"), 0);

				com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
				int mins_after_workbegin = cfg.getInt("mins_after_workbegin");
				int mins_ahead_of_workend = cfg.getInt("mins_ahead_of_workend");
				String dtStr = DateUtil.format(kqTime, "yyyy-MM-dd");
				// 如果中间休息
				if ("是".equals(rest)) {
					// 如果是第1次上班
					if (true) {
						String strDtBegin = dtStr + " " + begin_hour + ":" + begin_time;
						Date dtBegin = DateUtil.parse(strDtBegin, "yyyy-MM-dd HH:mm");
						int diffM = DateUtil.datediffMinute(kqTime, dtBegin);
						ary[1] = diffM;
						if (diffM>0) { // 迟到
							if (diffM <= mins_after_workbegin) {
								// 在弹性时间内
								return TYPE_ON_DUTY_1;
							}
						}
						else {
							// <0表示提前上班
							return TYPE_ON_DUTY_1;
						}
					}
					if (true) {
						// 判断是否为第2次下班
						String strDtEnd = dtStr + " " + end_hour + ":" + end_time;				
						Date dtEnd = DateUtil.parse(strDtEnd, "yyyy-MM-dd HH:mm");
						int diffM = DateUtil.datediffMinute(kqTime, dtEnd);
						ary[1] = diffM;				
						if (diffM<0) { // 早退，diffM为负值
							if (-diffM <= mins_ahead_of_workend){ // 在弹性时间内
								return TYPE_OFF_DUTY_2;
							}
						}
						else {
							return TYPE_OFF_DUTY_2; // 推迟下班， diffM为正值
						}
					}
					// 判斷是否爲第1次下班
					if (true) {
						String strDtEnd = dtStr + " " + rest_begin_hour + ":" + rest_begin_time;				
						Date dtEnd = DateUtil.parse(strDtEnd, "yyyy-MM-dd HH:mm");
						int diffM = DateUtil.datediffMinute(kqTime, dtEnd);
						ary[1] = diffM;				
						if (diffM<0) { // 早退，diffM为负值
							if (-diffM <= mins_ahead_of_workend){ // 在弹性时间内
								return TYPE_OFF_DUTY_1;
							}
						}
						else {
							// 如果時間在第1次下班後，且在第2次上班前
							int lastType = getLastPunchType(userName, kqTime);
							if (lastType == TYPE_ON_DUTY_1) {
								return TYPE_OFF_DUTY_1; // 推迟下班， diffM为正值
							}
							else if (lastType == TYPE_OFF_DUTY_1) {
								// 如果未過第二次上班打卡時間
								String strDtBegin = dtStr + " " + rest_end_hour + ":" + rest_end_time;
								Date dtBegin = DateUtil.parse(strDtBegin, "yyyy-MM-dd HH:mm");
								if (kqTime.before(dtBegin)) {
									return TYPE_ON_DUTY_2;
								}
								// 如果没有第1次下班，则说明是早退
							}
						}				
					}					
					// 判斷是否爲第2次上班
					if (true) {
						String strDtBegin = dtStr + " " + rest_end_hour + ":" + rest_end_time;
						Date dtBegin = DateUtil.parse(strDtBegin, "yyyy-MM-dd HH:mm");
						int diffM = DateUtil.datediffMinute(kqTime, dtBegin);
						ary[1] = diffM;
						if (diffM>0) { // 迟到
							if (diffM <= mins_after_workbegin) {
								// 在弹性时间内
								return TYPE_ON_DUTY_2;
							}
						}
						else {
							return TYPE_ON_DUTY_2; // 提前上班
						}
					}		
				}
				else { // 如果中间不休息
					// 如果是上班
					if (true) {
						String strDtBegin = dtStr + " " + begin_hour + ":" + begin_time;
						Date dtBegin = DateUtil.parse(strDtBegin, "yyyy-MM-dd HH:mm");
						int diffM = DateUtil.datediffMinute(kqTime, dtBegin);
						ary[1] = diffM;
						if (diffM>0) { // 迟到
							if (diffM <= mins_after_workbegin) {
								// 在弹性时间内
								return TYPE_ON_DUTY_1;
							}
						}
						else {
							return TYPE_ON_DUTY_1;
						}
					}
					if (true) {
						String strDtEnd = dtStr + " " + end_hour + ":" + end_time;				
						Date dtEnd = DateUtil.parse(strDtEnd, "yyyy-MM-dd HH:mm");
						int diffM = DateUtil.datediffMinute(kqTime, dtEnd);
						ary[1] = diffM;				
						if (diffM<0) { // 早退，diffM为负值
							if (-diffM <= mins_ahead_of_workend){ // 在弹性时间内
								return TYPE_OFF_DUTY_1;
							}
						}
						else {
							return TYPE_OFF_DUTY_1; // 推迟下班， diffM为正值
						}
					}
				}
			}
			else {
				return -1;
			}
		}

		return -1;
	}
	
	/**
	 * 判斷某天是否已存在某種類型的打卡
	 * @param userName
	 * @param type
	 * @return
	 */
	public static boolean isPunched(String userName, int type) {
		String workDateString = DateUtil.format(new Date(), "yyyy-MM-dd");
		// 根据人员编号查询form_table_kaoqin_sign_time表中的考勤数据
		String sql = "select sign_type from form_table_kaoqin_time_sign where name = "+StrUtil.sqlstr(userName)+
			" and sign_type=" + type + " and sign_time >= "+SQLFilter.getDateStr(workDateString +" 00:00:00", "yyyy-MM-dd HH:mm:ss")+" and sign_time <= "+SQLFilter.getDateStr(workDateString+" 23:59:59", "yyyy-MM-dd HH:mm:ss") + " order by sign_time desc";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				return true;
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}	
	
	/**
	 * 检查是否迟到、早退
	 * @param userName
	 * @param kqTime
	 * @param type
	 * @return
	 * @throws ErrMsgException 
	 */
	public static long[] check(String userName, java.util.Date kqTime, int type, boolean isValidDistance) throws ErrMsgException {
		// 檢查是否存在重覆考勤
		if (isPunched(userName, type)){
			throw new ErrMsgException("您已打卡，请勿重复打卡！");
		}
		// 默认班次
		Object[] ary = ShiftScheduleMgr.getShiftDAO(userName, kqTime);
		if (ary!=null) {
			FormDAO fdao = (FormDAO)ary[0];
			// Boolean isDefault = (Boolean)ary[2];
			if (fdao!=null) {
				return checkByShift(userName, kqTime, fdao, type);
			}
			else {
				return new long[]{-1, 0};
			}
			/*
			if (isDefault.booleanValue()) {
				isOACalendar = ShiftScheduleMgr.isDefaultUseOACalendar();
			}
			if (isOACalendar) {
				return checkByOACalendar(userName, kqTime);
			}			
			else {
				if (fdao!=null) {
					return checkByShift(userName, kqTime, fdao, type);
				}
				else {
					return new int[]{-1, 0};
				}
			}
			*/
		}
		else {
			return new long[]{-1, 0};
		}
	}
	
	/**
	 * 根据排班判断是否迟到早退， 以及迟到早退的时长
	 * @param userName
	 * @param kqTime
	 * @param fdao
	 * @param type
	 * @return int[] 索引0表示是否正常、迟到或早退，索引1表示 1迟到或早退的分钟，如果为负值，表示早于规定的时间，如果是上班，表示提前上班，如果是下班，则表示提前下班
	 */
	public static long[] checkByShift(String userName, java.util.Date kqTime, FormDAO fdao, int type) {
		long[] ary = new long[2];
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

		String dtStr = DateUtil.format(kqTime, "yyyy-MM-dd");
		// 如果中间休息
		if ("是".equals(rest)) {
			// 如果是上班
			if (type==TYPE_ON_DUTY_1) {
				String strDtBegin = dtStr + " " + begin_hour + ":" + begin_time;
				Date dtBegin = DateUtil.parse(strDtBegin, "yyyy-MM-dd HH:mm");
				int diffM = DateUtil.datediffMinute(kqTime, dtBegin);
				ary[1] = diffM;
				if (diffM>0) { // 迟到
					if (diffM <= flex_minute) {
						// 在弹性时间内
						ary[0] = NORMAL;
					}
					else {
						ary[0] = LATE;
						ary[1] = diffM;
					}
				}
				else {
					ary[0] = NORMAL;
				}
			}
			else if (type==TYPE_OFF_DUTY_2) {
				String strDtEnd = dtStr + " " + end_hour + ":" + end_time;				
				Date dtEnd = DateUtil.parse(strDtEnd, "yyyy-MM-dd HH:mm");
				int diffM = DateUtil.datediffMinute(kqTime, dtEnd);
				ary[1] = diffM;				
				if (diffM<0) { // 早退，diffM为负值
					if (-diffM <= flex_minute){ // 在弹性时间内
						ary[0] = NORMAL;
					}
					else {
						ary[0] = EARLY;
						ary[1] = -diffM;
					}
				}
				else {
					ary[0] = NORMAL; // 推迟下班， diffM为正值
				}
			}
			else if (type==TYPE_ON_DUTY_2) {
				String strDtBegin = dtStr + " " + rest_end_hour + ":" + rest_end_time;
				Date dtBegin = DateUtil.parse(strDtBegin, "yyyy-MM-dd HH:mm");
				int diffM = DateUtil.datediffMinute(kqTime, dtBegin);
				ary[1] = diffM;
				if (diffM>0) { // 迟到
					if (diffM <= flex_minute) {
						// 在弹性时间内
						ary[0] = NORMAL;
					}
					else {
						ary[0] = LATE;
						ary[1] = diffM;
					}
				}
				else {
					ary[0] = NORMAL;
				}				
			}
			else if (type==TYPE_OFF_DUTY_1) {
				String strDtEnd = dtStr + " " + rest_begin_hour + ":" + rest_begin_time;				
				Date dtEnd = DateUtil.parse(strDtEnd, "yyyy-MM-dd HH:mm");
				int diffM = DateUtil.datediffMinute(kqTime, dtEnd);
				ary[1] = diffM;				
				if (diffM<0) { // 早退，diffM为负值
					if (-diffM <= flex_minute){ // 在弹性时间内
						ary[0] = NORMAL;
					}
					else {
						ary[0] = EARLY;
						ary[1] = -diffM;
					}
				}
				else {
					ary[0] = NORMAL; // 推迟下班， diffM为正值
				}				
			}			
		}
		else { // 如果中间不休息
			// 如果是上班
			if (type==TYPE_ON_DUTY_1) {
				String strDtBegin = dtStr + " " + begin_hour + ":" + begin_time;
				Date dtBegin = DateUtil.parse(strDtBegin, "yyyy-MM-dd HH:mm");
				int diffM = DateUtil.datediffMinute(kqTime, dtBegin);
				ary[1] = diffM;
				if (diffM>0) { // 迟到
					if (diffM <= flex_minute) {
						// 在弹性时间内
						ary[0] = NORMAL;
					}
					else {
						ary[0] = LATE;
						ary[1] = diffM;
					}
				}
				else {
					ary[0] = NORMAL;
				}
			}
			else if (type==TYPE_OFF_DUTY_2) {
				String strDtEnd = dtStr + " " + end_hour + ":" + end_time;				
				Date dtEnd = DateUtil.parse(strDtEnd, "yyyy-MM-dd HH:mm");
				int diffM = DateUtil.datediffMinute(kqTime, dtEnd);
				ary[1] = diffM;				
				if (diffM<0) { // 早退，diffM为负值
					if (-diffM <= flex_minute){ // 在弹性时间内
						ary[0] = NORMAL;
					}
					else {
						ary[0] = EARLY;
						ary[1] = -diffM;
					}
				}
				else {
					ary[0] = NORMAL; // 推迟下班， diffM为正值
				}
			}
		}

		return ary;
	}
	
	/**
	 * 根据工作日历检查是否迟到早退
	 * @param userName
	 * @param kqTime
	 * @return int[] 0是否正常、迟到或早退， 1迟到或早退的时间
	 * @throws ErrMsgException
	 */
	public int[] checkByOACalendarXXX(String userName, java.util.Date kqTime) throws ErrMsgException {	
		// 如果根据工作日历，则判断是否为工作日，如果是工作日，则根据默认班次来检查？ 还是根据工作日历吧，免得工作日历上面的时间仅用于流程，不太合理
		// 而且实施的时候，对于一般正常白班的单位，只需设置好工作日历即可
		int kind = 0;
		for (int k=1; k<6; k++) {
			if (canCheckByOACalendar(userName, k, kqTime)) {
				kind = k;
				break;
			}
		}
		
		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config_oa.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.util.Properties props = cfgparser.getProps();
		int latevalue = Integer.parseInt(props.getProperty("latevalue"));	// 上班时间后多少分钟后算迟到

		OACalendarDb oaCalendarDb = new OACalendarDb();
		oaCalendarDb = (OACalendarDb)oaCalendarDb.getQObjectDb(DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd"), "yyyy-MM-dd"));		
		String checkBeginA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_a"));
		String checkEndA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_a"));
		String checkBeginB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_b"));
		String checkEndB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_b"));
		String checkBeginC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_c"));
		String checkEndC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_c"));
		
		int timeMin = 0;
		int flag = NORMAL;
		if (kind==1) {
			String t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginA;
			timeMin = DateUtil.datediffMinute(kqTime, DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = LATE; // 上班迟到
			}
		}
		else if (kind==2) {
			String t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndA;
			timeMin = DateUtil.datediffMinute(kqTime, DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = EARLY; // 下班早退
			}
			else
				timeMin = 0;
		}
		else if (kind==3) {
			String t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginB;
			timeMin = DateUtil.datediffMinute(kqTime, DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = LATE; // 上班迟到
			}
		}
		else if (kind==4) {
			String t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndB;
			timeMin = DateUtil.datediffMinute(kqTime, DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = EARLY; // 下班早退
			}
			else
				timeMin = 0;
		}		
		else if (kind==5) {
			String t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginC;
			timeMin = DateUtil.datediffMinute(kqTime, DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = LATE; // 上班迟到
			}
		}
		else if (kind==6) {
			String t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndC;
			timeMin = DateUtil.datediffMinute(kqTime, DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = EARLY; // 下班早退
			}
			else
				timeMin = 0;
		}		
		return new int[]{flag, timeMin};
	}
	
    public static boolean canCheckByOACalendar(String userName, int kind, java.util.Date kqTime) throws ErrMsgException {
		OACalendarDb oaCalendarDb = new OACalendarDb();
		oaCalendarDb = (OACalendarDb)oaCalendarDb.getQObjectDb(DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd"), "yyyy-MM-dd"));
		if (oaCalendarDb==null) {
			throw new ErrMsgException("工作日历未初始化！");
		}
		
		// 如果已考勤过
		/*
		if (kq!=null) {
			if (!kpvg.canAdminUser(request, userName))
				throw new ErrMsgException("该项考勤记录已存在！");
		}		
		*/
		
		String checkTime = "";

		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config_oa.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.util.Properties props = cfgparser.getProps();
		int minsAheadOfWorkBegin = Integer.parseInt(props.getProperty("mins_ahead_of_workbegin"));//上班时间前多少分钟可以开始上班考勤
		int minsAfterWorkBegin = Integer.parseInt(props.getProperty("mins_after_workbegin"));//上班时间后多少分钟内可以上班考勤
		int minsAheadOfWorkEnd = Integer.parseInt(props.getProperty("mins_ahead_of_workend"));//下班时间前多少分钟内可以下班考勤
		int minsAfterWorkEnd = Integer.parseInt(props.getProperty("mins_after_workend"));//下班时间后多少分钟内可以下班考勤
		
		String checkBeginA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_a"));
		String checkEndA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_a"));
		String checkBeginB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_b"));
		String checkEndB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_b"));
		String checkBeginC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_c"));
		String checkEndC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_c"));    	

		boolean canCheck = false;
		java.util.Date dCheckTime;
		int diffMins;

		if (kind==1) {
			if (!checkBeginA.equals("")) {
				checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginA;
				dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
				diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
				canCheck = false;
	
				if (diffMins>0) {
					if (diffMins <= minsAfterWorkBegin)
						canCheck = true;
				}
				else {
					if (-diffMins <= minsAheadOfWorkBegin)
						canCheck = true;
				}
			}
    	}
		else if (kind==2) {
			if (!checkEndA.equals("")) {
				checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndA;
				dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
				diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
				canCheck = false;
				if (diffMins>0) {
					if (diffMins < minsAfterWorkEnd)
						canCheck = true;
				}
				else {
					if (-diffMins < minsAheadOfWorkEnd)
						canCheck = true;
				}
			}
		}
		else if (kind==3) {
			if (!checkBeginB.equals("")) {
				checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginB;
				dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
				diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
				canCheck = false;
				if (diffMins>0) {
					if (diffMins <= minsAfterWorkBegin)
						canCheck = true;
				}
				else {
					if (-diffMins <= minsAheadOfWorkBegin)
						canCheck = true;
				}
			}			
		}
		else if (kind==4) {
			if (!checkEndB.equals("")) {
				checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndB;
				dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
				diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
				canCheck = false;
				if (diffMins>0) {
					if (diffMins < minsAfterWorkEnd)
						canCheck = true;
				}
				else {
					if (-diffMins < minsAheadOfWorkEnd)
						canCheck = true;
				}
			}
		}
		else if (kind==5) {
			if (!checkBeginC.equals("")) {		
				checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginC;
				dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
				diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
				canCheck = false;
				if (diffMins>0) {
					if (diffMins <= minsAfterWorkBegin)
						canCheck = true;
				}
				else {
					if (-diffMins <= minsAheadOfWorkBegin)
						canCheck = true;
				}
			}
		}
		else if (kind==6) {
			if (!checkEndC.equals("")) {				
				checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndC;
				dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
				diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
				canCheck = false;
				if (diffMins>0) {
					if (diffMins < minsAfterWorkEnd)
						canCheck = true;
				}
				else {
					if (-diffMins < minsAheadOfWorkEnd)
						canCheck = true;
				}
			}
		}
 	
    	return canCheck;
    }	
}
