package com.redmoon.oa.kaoqin;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: 用于PC端在线考勤，已弃用</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class KaoqinMgr {
    Logger logger = Logger.getLogger(KaoqinMgr.class.getName());

    public KaoqinMgr() {

    }

    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        KaoqinDb kd = getKaoqinDb(id);

        Privilege privilege = new Privilege();
        if (!privilege.canAdminUser(request, kd.getName())) {
            throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(
                    request, "pvg_invalid"));
        }

        boolean re = true;
        String errmsg = "";

        String reason = ParamUtil.get(request, "reason");
        String direction = ParamUtil.get(request, "direction");
        String type = ParamUtil.get(request, "type");
        java.util.Date myDate = DateUtil.parse(ParamUtil.get(request, "myDate"), "yyyy-MM-dd HH:mm:ss");
        if (!errmsg.equals(""))
            throw new ErrMsgException(errmsg);

		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.util.Properties props = cfgparser.getProps();
		int latevalue = Integer.parseInt(props.getProperty("latevalue"));	// 上班时间后多少分钟后算迟到
		
		java.util.Calendar kqTime = Calendar.getInstance();
		kqTime.setTime(myDate);
		
		OACalendarDb oaCalendarDb = new OACalendarDb();
		oaCalendarDb = (OACalendarDb)oaCalendarDb.getQObjectDb(DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd"), "yyyy-MM-dd"));		
		String checkBeginA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_a"));
		String checkEndA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_a"));
		String checkBeginB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_b"));
		String checkEndB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_b"));
		String checkBeginC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_c"));
		String checkEndC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_c"));
		
		int timeMin = 0;
		int flag = 0;
		int kind = kd.getKind();

		if (kind==1) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginA;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==2) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndA;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}
		else if (kind==3) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginB;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==4) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndB;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}		
		else if (kind==5) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginC;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==6) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndC;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}
		
		kd.setTimeMin(timeMin);
		kd.setFlag(flag);
		
        kd.setReason(reason);
        kd.setDirection(direction);
        kd.setType(type);
        kd.setMyDate(myDate);
        re = kd.save();
        return re;
    }

    public KaoqinDb getKaoqinDb(int id) {
        KaoqinDb addr = new KaoqinDb();
        return addr.getKaoqinDb(id);
    }
    
    public static boolean canCheck(HttpServletRequest request, String userName, int kind, java.util.Date kqTime) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "err_not_login"));

		OACalendarDb oaCalendarDb = new OACalendarDb();
		oaCalendarDb = (OACalendarDb)oaCalendarDb.getQObjectDb(DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd"), "yyyy-MM-dd"));
		if (oaCalendarDb==null) {
			throw new ErrMsgException("工作日历未初始化！");
		}
        
        KaoqinPrivilege kpvg = new KaoqinPrivilege();
		if (kpvg.canAdminUser(request, userName)) {
			return true;
		}		
		
    	KaoqinDb kq = new KaoqinDb();		
		kq = kq.getKaoqinDb(userName, kqTime, kind);
		// 如果已考勤过
		if (kq!=null) {
			if (!kpvg.canAdminUser(request, userName))
				throw new ErrMsgException("该项考勤记录已存在！");
		}		
		
		String checkTime = "";

		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config.xml");
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
				if (kpvg.canAdminUser(request, userName)) {
					canCheck = true;
				}
				else {
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
    
    /**
     * 管理员对单个用户进行考勤
     * @param request
     * @return
     * @throws ErrMsgException
     */
	public boolean kaoqinSingle(HttpServletRequest request)
			throws ErrMsgException {
		String userName = ParamUtil.get(request, "userName");
		String kqTimeStr = ParamUtil.get(request, "kqTime");
		java.util.Date kqTime = DateUtil.parse(kqTimeStr, "yyyy-MM-dd");
		if (kqTime == null)
			kqTime = new java.util.Date();
		
        canCheck(request, userName, 0, kqTime);

		String kqTime1Str = ParamUtil.get(request, "kqTime1");
		String kqTime2Str = ParamUtil.get(request, "kqTime2");
		String kqTime3Str = ParamUtil.get(request, "kqTime3");
		String kqTime4Str = ParamUtil.get(request, "kqTime4");
		String kqTime5Str = ParamUtil.get(request, "kqTime5");
		String kqTime6Str = ParamUtil.get(request, "kqTime6");

		java.util.Date kqt1 = DateUtil.parse(kqTime1Str, "yyyy-MM-dd HH:mm");
		java.util.Date kqt2 = DateUtil.parse(kqTime2Str, "yyyy-MM-dd HH:mm");
		java.util.Date kqt3 = DateUtil.parse(kqTime3Str, "yyyy-MM-dd HH:mm");
		java.util.Date kqt4 = DateUtil.parse(kqTime4Str, "yyyy-MM-dd HH:mm");
		java.util.Date kqt5 = DateUtil.parse(kqTime5Str, "yyyy-MM-dd HH:mm");
		java.util.Date kqt6 = DateUtil.parse(kqTime6Str, "yyyy-MM-dd HH:mm");

		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.util.Properties props = cfgparser.getProps();
		int latevalue = Integer.parseInt(props.getProperty("latevalue")); // 上班时间后多少分钟后算迟到

		OACalendarDb oaCalendarDb = new OACalendarDb();
		oaCalendarDb = (OACalendarDb) oaCalendarDb.getQObjectDb(DateUtil.parse(
				DateUtil.format(kqTime, "yyyy-MM-dd"), "yyyy-MM-dd"));
		String checkBeginA = StrUtil.getNullStr(oaCalendarDb
				.getString("work_time_begin_a"));
		String checkEndA = StrUtil.getNullStr(oaCalendarDb
				.getString("work_time_end_a"));
		String checkBeginB = StrUtil.getNullStr(oaCalendarDb
				.getString("work_time_begin_b"));
		String checkEndB = StrUtil.getNullStr(oaCalendarDb
				.getString("work_time_end_b"));
		String checkBeginC = StrUtil.getNullStr(oaCalendarDb
				.getString("work_time_begin_c"));
		String checkEndC = StrUtil.getNullStr(oaCalendarDb
				.getString("work_time_end_c"));
		
		// 如果未填写日期,则初始化为正常考勤
		if (kqt1==null)
			kqt1 = DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginA, "yyyy-MM-dd HH:mm");
		if (kqt2==null)
			kqt2 = DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndA, "yyyy-MM-dd HH:mm");
		if (kqt3==null)
			kqt3 = DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginB, "yyyy-MM-dd HH:mm");
		if (kqt4==null)
			kqt4 = DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndB, "yyyy-MM-dd HH:mm");
		if (kqt5==null)
			kqt5 = DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginC, "yyyy-MM-dd HH:mm");
		if (kqt6==null)
			kqt6 = DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndC, "yyyy-MM-dd HH:mm");

		int timeMin = 0;
		int flag = 0;

		String t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginA;

		timeMin = DateUtil.datediffMinute(kqt1, DateUtil.parse(t,
				"yyyy-MM-dd HH:mm"));
		if (timeMin < 0)
			timeMin = 0;
		else if (timeMin > latevalue) {
			flag = 1; // 上班迟到
		}

		boolean re = false;

		KaoqinDb kq2 = new KaoqinDb();

		int kind = 1;
		String direction = "c";
		KaoqinDb kq = kq2.getKaoqinDb(userName, kqTime, kind);
		boolean isNew = true;
		// 如果已考勤过
		if (kq == null)
			kq = new KaoqinDb();
		else
			isNew = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqt1);
		if (isNew) {
			kq.setReason("");
			kq.setType("考勤");
			kq.setName(userName);
			kq.setDirection(direction);
			re = kq.create();
		} else {
			re = kq.save();
		}

		t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndA;
		timeMin = DateUtil.datediffMinute(kqt2, DateUtil.parse(t,
				"yyyy-MM-dd HH:mm"));
		if (timeMin < 0) {
			timeMin = -timeMin;
			if (timeMin > latevalue)
				flag = 2; // 下班早退
		} else
			timeMin = 0;
		kind = 2;
		direction = "l";
		kq = kq2.getKaoqinDb(userName, kqTime, kind);
		isNew = true;
		// 如果已考勤过
		if (kq == null)
			kq = new KaoqinDb();
		else
			isNew = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqt2);
		if (isNew) {
			kq.setReason("");
			kq.setType("考勤");
			kq.setName(userName);
			kq.setDirection(direction);
			re = kq.create();
		} else {
			re = kq.save();
		}

		t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginB;
		timeMin = DateUtil.datediffMinute(kqt3, DateUtil.parse(t,
				"yyyy-MM-dd HH:mm"));
		if (timeMin < 0)
			timeMin = 0;
		else if (timeMin > latevalue) {
			flag = 1; // 上班迟到
		}
		kind = 3;
		direction = "c";
		kq = kq2.getKaoqinDb(userName, kqTime, kind);
		isNew = true;
		// 如果已考勤过
		if (kq == null)
			kq = new KaoqinDb();
		else
			isNew = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqt3);
		if (isNew) {
			kq.setReason("");
			kq.setType("考勤");
			kq.setName(userName);
			kq.setDirection(direction);
			re = kq.create();
		} else {
			re = kq.save();
		}

		t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndB;
		timeMin = DateUtil.datediffMinute(kqt4, DateUtil.parse(t,
				"yyyy-MM-dd HH:mm"));
		if (timeMin < 0) {
			timeMin = -timeMin;
			if (timeMin > latevalue)
				flag = 2; // 下班早退
		} else
			timeMin = 0;
		kind = 4;
		direction = "l";
		kq = kq2.getKaoqinDb(userName, kqTime, kind);
		isNew = true;
		// 如果已考勤过
		if (kq == null)
			kq = new KaoqinDb();
		else
			isNew = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqt4);
		if (isNew) {
			kq.setReason("");
			kq.setType("考勤");
			kq.setName(userName);
			kq.setDirection(direction);
			re = kq.create();
		} else {
			re = kq.save();
		}

		t = DateUtil.format(kqt5, "yyyy-MM-dd") + " " + checkBeginC;
		timeMin = DateUtil.datediffMinute(kqt5, DateUtil.parse(t,
				"yyyy-MM-dd HH:mm"));
		if (timeMin < 0)
			timeMin = 0;
		else if (timeMin > latevalue) {
			flag = 1; // 上班迟到
		}
		LogUtil.getLog(getClass()).info("kqt5=" + DateUtil.format(kqt5, "yyyy-MM-dd HH:mm:ss") + " timeMin=" + timeMin + " flag=" + flag + " t=" + t);

		kind = 5;
		direction = "c";
		kq = kq2.getKaoqinDb(userName, kqTime, kind);
		isNew = true;
		// 如果已考勤过
		if (kq == null)
			kq = new KaoqinDb();
		else
			isNew = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqt5);
		if (isNew) {
			kq.setReason("");
			kq.setType("考勤");
			kq.setName(userName);
			kq.setDirection(direction);
			re = kq.create();
		} else {
			re = kq.save();
		}

		t = DateUtil.format(kqt6, "yyyy-MM-dd") + " " + checkEndC;
		timeMin = DateUtil.datediffMinute(kqt6, DateUtil.parse(t,
				"yyyy-MM-dd HH:mm"));
		if (timeMin < 0) {
			timeMin = -timeMin;
			if (timeMin > latevalue)
				flag = 2; // 下班早退
		} else
			timeMin = 0;
		kind = 6;
		direction = "l";
		kq = kq2.getKaoqinDb(userName, kqTime, kind);
		isNew = true;
		// 如果已考勤过
		if (kq == null)
			kq = new KaoqinDb();
		else
			isNew = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqt6);
		if (isNew) {
			kq.setReason("");
			kq.setType("考勤");
			kq.setName(userName);
			kq.setDirection(direction);
			re = kq.create();
		} else {
			re = kq.save();
		}

		return re;

	}
    
    /**
     * 管理员对多个人员考勤
     * @param request
     */
    public void kaoqinBatch(HttpServletRequest request) throws ErrMsgException {
    	String deptCode = ParamUtil.get(request, "deptCode");
    	if (!Privilege.canUserAdminDept(request, deptCode)) {
    		throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
    	}
    	
		java.util.Date kqTime = DateUtil.parse(ParamUtil.get(request, "kqTime"), "yyyy-MM-dd");
		LogUtil.getLog(getClass()).info("kqTime=" + DateUtil.format(kqTime, "yyyy-MM-dd HH:mm:ss"));

		int flag = ParamUtil.getInt(request, "flag", 0);
		int timeMin = 0;
		int kind = ParamUtil.getInt(request, "kind", 1);
		String direction = ParamUtil.get(request, "direction");
		
		String[] names = StrUtil.split(ParamUtil.get(request, "names"), ",");
		if (names==null)
			throw new ErrMsgException("请选择用户!");
		
		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		java.util.Properties props = cfgparser.getProps();
		int latevalue = Integer.parseInt(props.getProperty("latevalue")) + 1;	// 上班时间后多少分钟后算迟到
		
		if (flag!=0)
			timeMin = latevalue;
		
       	KaoqinDb kq2 = new KaoqinDb();
       	
		OACalendarDb oaCalendarDb = new OACalendarDb();
		oaCalendarDb = (OACalendarDb)oaCalendarDb.getQObjectDb(DateUtil.parse(DateUtil.format(kqTime, "yyyy-MM-dd"), "yyyy-MM-dd"));		
		String checkBeginA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_a"));
		String checkEndA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_a"));
		String checkBeginB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_b"));
		String checkEndB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_b"));
		String checkBeginC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_c"));
		String checkEndC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_c"));     
		
		String t = "";
		if (kind==1)
			t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginA;
		else if (kind==2)
			t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndA;
		else if (kind==3)
			t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginB;
		else if (kind==4)
			t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndB;
		else if (kind==5)
			t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginC;
		else if (kind==6)
			t = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndC;
		
		LogUtil.getLog(getClass()).info("t=" + t + " timeMin=" + timeMin + " flag=" + flag);

		java.util.Date dt = DateUtil.parse(t, "yyyy-MM-dd HH:mm");
		
		LogUtil.getLog(getClass()).info("dt=" + DateUtil.format(dt, "yyyy-MM-dd HH:mm"));
		if (dt==null) {
			throw new ErrMsgException("考勤时间段未设定!");
		}
		
		if (flag==1)
			kqTime = DateUtil.addMinuteDate(dt, timeMin);
		else
			kqTime = DateUtil.addMinuteDate(dt, -timeMin);
       			
       	for (int i=0; i<names.length; i++) {
			KaoqinDb kq = kq2.getKaoqinDb(names[i], kqTime, kind);
			boolean isNew = true;
			// 如果已考勤过
			if (kq==null)
				kq = new KaoqinDb();
			else
				isNew = false;
			
			kq.setFlag(flag);
			kq.setTimeMin(timeMin);
			kq.setKind(kind);
			kq.setMyDate(kqTime);
			if (isNew) {
				kq.setReason("");
				kq.setType("考勤");
				kq.setName(names[i]);
				kq.setDirection(direction);			
				try {
					kq.create();
				} catch (ErrMsgException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					kq.save();
				} catch (ErrMsgException e) {
					e.printStackTrace();
				}
			}
       	}
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
    	//String userName = ParamUtil.get(request, "userName");
        String userName = request.getParameter("userName");
    	if (userName == null || userName.equals(""))
    		userName = privilege.getUser(request);

		String direction = "";
		int kind = ParamUtil.getInt(request, "kind", 1);
		
		String reason = ParamUtil.get(request, "reason");
		//String reason = StrUtil.getNullStr(request.getParameter("reason"));
		
		Calendar kqTime = Calendar.getInstance();
		String timeStr = ParamUtil.get(request, "time");
		if (!timeStr.equals("")) {
			timeStr = ParamUtil.get(request, "kqDate") + " " + timeStr;
			LogUtil.getLog(getClass()).info("timeStr=" + timeStr);
			java.util.Date d = DateUtil.parse(timeStr, "yyyy-MM-dd HH:mm:ss");
			if (d!=null)
				kqTime.setTime(d);
		}
		
        canCheck(request, userName, kind, kqTime.getTime());
		
    	KaoqinDb kq = new KaoqinDb();
		kq = kq.getKaoqinDb(userName, kqTime.getTime(), kind);
		boolean isNew = true;
		// 如果已考勤过
		if (kq==null)
			kq = new KaoqinDb();
		else
			isNew = false;
		
		LogUtil.getLog(getClass()).info("isNew=" + isNew + " kind=" + kind);
		
		
		if (kind==1 || kind==3 || kind==5) {
			direction = "c";
		}
		else {
			direction = "l";
		}
		
		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config.xml");
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
		int flag = 0;
		if (kind==1) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginA;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==2) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndA;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}
		else if (kind==3) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginB;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==4) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndB;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}		
		else if (kind==5) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginC;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==6) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndC;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}
		
		boolean re = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqTime.getTime());
		kq.setReason(reason);
		if (isNew) {
			kq.setReason(reason);
			kq.setType("考勤");
			kq.setName(userName);		
			kq.setDirection(direction);			
			re = kq.create();
		}
		else {
			re = kq.save();
		}

        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int id = ParamUtil.getInt(request, "id");
        KaoqinDb kd = getKaoqinDb(id);
        if (kd == null || !kd.isLoaded())
            throw new ErrMsgException("该项已不存在！");

        Privilege privilege = new Privilege();
        if (!privilege.canAdminUser(request, kd.getName()))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
        return kd.del();
    }
    

	public static int[] getLateCountAndBeforeCount(String userName, int showyear, int showmonth) {
		Calendar cal = Calendar.getInstance();
		cal.set(showyear,showmonth-1,1,0,0,0);
		java.util.Date d1 = cal.getTime();
		cal.set(showyear,showmonth-1,DateUtil.getDayCount(showyear, showmonth-1),23,59,59);
		java.util.Date d2 = cal.getTime();
		
		String sql = "select id from kaoqin where name=" + StrUtil.sqlstr(userName) + " and myDate>=" + SQLFilter.getDateStr(DateUtil.format(d1, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " and myDate<=" + SQLFilter.getDateStr(DateUtil.format(d2, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + " order by mydate asc";

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		int latevalue = cfg.getInt("latevalue");

		KaoqinDb kd = new KaoqinDb();
		Vector v = kd.list(sql);
		Iterator ir = v.iterator();

		OACalendarDb oad = new OACalendarDb();

		java.util.Date dt;
		int myhour = 0; //用于计算迟到时间
		int latecount = 0;//迟到次数
		int beforecount = 0; //早退次数
		int overtimecount = 0; // 加班次数

		Calendar cld = Calendar.getInstance();

		while (ir.hasNext()) {
			kd = (KaoqinDb) ir.next();

			String direction = kd.getDirection();
			String type = kd.getType();

			String mydate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
			String strTempDate = DateUtil.format(kd.getMyDate(), "yyyy-MM-dd 00:00:00");
			dt = DateUtil.parse(strTempDate, "yyyy-MM-dd 00:00:00");
			cld.setTime(dt);

			mydate = mydate.substring(11, 19);

			// 计算是否迟到
			myhour = cld.get(Calendar.HOUR_OF_DAY);
			if (type.equals("考勤")) {
				OACalendarDb oad2 = (OACalendarDb) oad.getQObjectDb(dt);

				if (oad2.getInt("date_type") == OACalendarDb.DATE_TYPE_WORK) {
					if (kd.getFlag()==1) {
						latecount ++;						
					}
					else if (kd.getFlag()==2) {
						beforecount ++;							
					}					
				}
			}
			else if (type.equals("加班")) {
				overtimecount++;
			}
		}
		
		int[] ary = new int[3];
		ary[0] = latecount;
		ary[1] = beforecount;
		ary[2] = overtimecount;
		return ary;
	}    
	/**
	 * 获取没有考勤的天数
	 * @param userName
	 * @param year
	 * @param month
	 * @return
	 */
	public int getNonKqCount(String userName,int showyear, int showmonth){
		Calendar cal = Calendar.getInstance();
		cal.set(showyear,showmonth-1,1,0,0,0);
		java.util.Date d1 = cal.getTime();
		cal.set(showyear,showmonth-1,DateUtil.getDayCount(showyear, showmonth-1),23,59,59);
		java.util.Date d2 = cal.getTime();
		JdbcTemplate JTemplate = new JdbcTemplate();
		String beginDate = DateUtil.format(d1, "yyyy-MM-dd");
		String endDate = DateUtil.format(d2, "yyyy-MM-dd");
		String sql = "select ca.oa_date from oa_calendar ca where ca.oa_date >= "+SQLFilter.sqlstr(beginDate)+" and ca.oa_date <= "+SQLFilter.sqlstr(endDate)+" and ca.date_type = 0 ";
		KaoqinDb kd = new KaoqinDb();
		int count = 0;
		try {
			ResultIterator ri = JTemplate.executeQuery(sql);
			while(ri!=null&&ri.hasNext()){
				ResultRecord rd = (ResultRecord)ri.next();
				Date workDate = rd.getDate(1);
				String workDateString = DateUtil.format(workDate, "yyyy-MM-dd");
				sql = "select id from kaoqin where DATE_FORMAT(myDate,'%Y-%m-%d') = "+SQLFilter.sqlstr(workDateString)+" and name = "+SQLFilter.sqlstr(userName);
				Vector v = kd.list(sql);
				if (v!=null&&v.size()>0)
					continue;
				else 
					count++;
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("在线考勤获取未打卡数量出现异常："+StrUtil.trace(e));
		} finally {
			if (JTemplate != null)
				JTemplate.close();
		}
		
		return count;
	}

	// 用于admin_dept_user_kaoqin.jsp，该文件已弃用
	public static int getLeaveDays(String userName, int year, int month) {
		int dayCount = DateUtil.getDayCount(year, month-1);
		
		String beginDate = year + "-" + month + "-1";
		String endDate = year + "-" + month + "-" + dayCount;
		java.util.Date bDate = DateUtil.parse(beginDate, "yyyy-MM-dd");
		java.util.Date eDate = DateUtil.parse(endDate, "yyyy-MM-dd");
		
		String sql = "select f.flowId from form_table_qjsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='qj' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
		sql += " and f.applier=" + StrUtil.sqlstr(userName);
		sql += " and ((f.qjkssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.qjkssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + ") or (f.qjjssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " and f.qjjssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + ") or (f.qjkssj<=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.qjjssj>=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + "))";
		sql += " and f.result='1'";
		
		FormDAO fdao = new FormDAO();
		FormDb fd = new FormDb();
		fd = fd.getFormDb("qjsqd");		
		
		OACalendarDb oacal = new OACalendarDb();
		
		// 检查销假时间，得出本月事假、病假天数
		WorkflowDb wf = new WorkflowDb();
		Iterator irwf = wf.list(sql).iterator();
		int qjDays = 0; // 实际请假天数，计算销假是否超期，并减去节假日
		java.util.Date qjbDate = null; // 本月请假于本月的实际开始日期
		java.util.Date qjeDate = null; // 本月请假于本月的实际结束日期
		while (irwf.hasNext()) {
			wf = (WorkflowDb)irwf.next();
			fdao = fdao.getFormDAO(wf.getId(), fd);

			java.util.Date ksDate = DateUtil.parse(fdao.getFieldValue("qjkssj"), "yyyy-MM-dd");
			java.util.Date jsDate = DateUtil.parse(fdao.getFieldValue("qjjssj"), "yyyy-MM-dd");
			java.util.Date xjDate = DateUtil.parse(fdao.getFieldValue("xjrq"), "yyyy-MM-dd");
			
			qjbDate = ksDate;
			qjeDate = jsDate;
			
			// 如果请假开始时间早于本月第一天
			if (DateUtil.compare(ksDate, bDate)==2) {
				qjbDate = bDate;
			}
			if (DateUtil.compare(jsDate, eDate)==1) {
				qjeDate = eDate;
			}
			if (DateUtil.compare(xjDate, eDate)==1) {
				qjeDate = eDate;
			}		
			
			// 去除本月请假区间中的节假日
			try {
				qjDays += oacal.getWorkDayCount(DateUtil.addDate(qjbDate, -1), qjeDate);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return qjDays;		
	}

	/**
	 * 取得加班天数，注意：开始时间与结束时间不能用来判别加班天数，因为跨天时无法确定在一天中加班了多长时间
	 * @param userName
	 * @param year
	 * @param month
	 * @return
	 */
	public static double getJbDays(String userName, int year, int month) {
		int dayCount = DateUtil.getDayCount(year, month-1);
		
		String beginDate = year + "-" + month + "-1";
		String endDate = year + "-" + month + "-" + dayCount;
		java.util.Date bDate = DateUtil.parse(beginDate, "yyyy-MM-dd");
		java.util.Date eDate = DateUtil.parse(endDate, "yyyy-MM-dd");
		
		String sql = "select f.flowId from form_table_jbsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='jbsq' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
		sql += " and f.applier=" + StrUtil.sqlstr(userName);
		sql += " and ((f.kssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.kssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + ") or (f.jssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " and f.jssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + ") or (f.kssj<=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.jssj>=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + "))";
		sql += " and f.result='1'";
		
		FormDAO fdao = new FormDAO();
		FormDb fd = new FormDb();
		fd = fd.getFormDb("jbsqd");
				
		WorkflowDb wf = new WorkflowDb();
		Iterator irwf = wf.list(sql).iterator();
		double jbDays = 0;
		java.util.Date jbbDate = null;
		java.util.Date jbeDate = null;
		while (irwf.hasNext()) {
			wf = (WorkflowDb)irwf.next();
			fdao = fdao.getFormDAO(wf.getId(), fd);

			java.util.Date ksDate = DateUtil.parse(fdao.getFieldValue("kssj"), "yyyy-MM-dd");
			java.util.Date jsDate = DateUtil.parse(fdao.getFieldValue("jssj"), "yyyy-MM-dd");
			
			jbbDate = ksDate;
			jbeDate = jsDate;
			
			// 如果加班开始时间早于本月第一天
			if (DateUtil.compare(ksDate, bDate)==2) {
				jbbDate = bDate;
			}
			if (DateUtil.compare(jsDate, eDate)==1) {
				jbeDate = eDate;
			}
			
			jbDays += StrUtil.toDouble(fdao.getFieldValue("day_count"));
		}
		return jbDays;		
	}	
	
	//定位时，检查是否能考勤
	public static int locationCanCheck(String userName, java.util.Date kqTime) throws ErrMsgException {
		int kind = -1; 

		OACalendarDb oaCalendarDb = new OACalendarDb();
		oaCalendarDb = (OACalendarDb) oaCalendarDb.getQObjectDb(DateUtil.parse(
				DateUtil.format(kqTime, "yyyy-MM-dd"), "yyyy-MM-dd"));
		
		if (oaCalendarDb == null) {
			return kind;
		}

		String checkTime = "";
		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		java.util.Properties props = cfgparser.getProps();
		
		Config cg = new Config();
		boolean isKaoqinNight = cg.getBooleanProperty("isKaoqinNight");
		
		int minsAheadOfWorkBegin = Integer.parseInt(props.getProperty("mins_ahead_of_workbegin")); // 上班时间前多少分钟可以开始上班考勤
		int minsAfterWorkBegin = Integer.parseInt(props.getProperty("mins_after_workbegin"));      // 上班时间后多少分钟内可以上班考勤
		int minsAheadOfWorkEnd = Integer.parseInt(props.getProperty("mins_ahead_of_workend"));     // 下班时间前多少分钟内可以下班考勤
		int minsAfterWorkEnd = Integer.parseInt(props.getProperty("mins_after_workend"));          // 下班时间后多少分钟内可以下班考勤

		String checkBeginA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_a"));
		String checkEndA = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_a"));
		String checkBeginB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_b"));
		String checkEndB = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_b"));
		String checkBeginC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_begin_c"));
		String checkEndC = StrUtil.getNullStr(oaCalendarDb.getString("work_time_end_c"));

		boolean canCheck = false;
		java.util.Date dCheckTime;
		int diffMins;

		if (!checkBeginA.equals("")) {
			kind = 1;
			checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " "	+ checkBeginA;
			dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
			diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
			
			canCheck = false;
			if (diffMins > 0) {
				if (diffMins <= minsAfterWorkBegin)
					canCheck = true;
			} else {
				if (-diffMins <= minsAheadOfWorkBegin)
					canCheck = true;
			}
			
			if (canCheck) {
				return kind;
			}
		}  
		if (!checkEndA.equals("")) {
			kind = 2;
			checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndA;
			dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
			diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
			
			canCheck = false;
			if (diffMins > 0) {
				if (diffMins < minsAfterWorkEnd)
					canCheck = true;
			} else {
				if (-diffMins < minsAheadOfWorkEnd)
					canCheck = true;
			}
			
			if (canCheck) {
				return kind;
			}
		}
		if (!checkBeginB.equals("")) {
			kind = 3;
			checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " "	+ checkBeginB;
			dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
			diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
			
			canCheck = false;
			if (diffMins > 0) {
				if (diffMins <= minsAfterWorkBegin)
					canCheck = true;
			} else {
				if (-diffMins <= minsAheadOfWorkBegin)
					canCheck = true;
			}
			
			if (canCheck) {
				return kind;
			}
		}
		if (!checkEndB.equals("")) {
			kind = 4;
			checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndB;
			dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
			diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
			
			canCheck = false;
			if (diffMins > 0) {
				if (diffMins < minsAfterWorkEnd)
					canCheck = true;
			} else {
				if (-diffMins < minsAheadOfWorkEnd)
					canCheck = true;
			}
			
			if (canCheck) {
				return kind;
			}
		} 
		if (isKaoqinNight && !checkBeginC.equals("")) {
			kind = 5;
			checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkBeginC;
			dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
			diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
			
			canCheck = false;
			if (diffMins > 0) {
				if (diffMins <= minsAfterWorkBegin)
					canCheck = true;
			} else {
				if (-diffMins <= minsAheadOfWorkBegin)
					canCheck = true;
			}
			
			if (canCheck) {
				return kind;
			}
		}
		if (isKaoqinNight && !checkEndC.equals("")) {
			kind = 6;
			checkTime = DateUtil.format(kqTime, "yyyy-MM-dd") + " " + checkEndC;
			dCheckTime = DateUtil.parse(checkTime, "yyyy-MM-dd HH:mm");
			diffMins = DateUtil.datediffMinute(kqTime, dCheckTime);
			
			canCheck = false;
			if (diffMins > 0) {
				if (diffMins < minsAfterWorkEnd)
					canCheck = true;
			} else {
				if (-diffMins < minsAheadOfWorkEnd)
					canCheck = true;
			}
			
			if (canCheck) {
				return kind;
			}
		}
		
	 	kind = -1;
	    return kind;
	 }
	
	//定位时新建考勤记录
    public boolean locationCreate(String userName, int kind ,java.util.Date kqDate ,String reason) throws ErrMsgException {
    	String direction = "";
		Calendar kqTime = Calendar.getInstance();
		
    	KaoqinDb kq = new KaoqinDb();
		kq = kq.getKaoqinDb(userName, kqTime.getTime(), kind);
		boolean isNew = true;
		// 如果已考勤过
		if (kq==null)
			kq = new KaoqinDb();
		else
			isNew = false;
		
		LogUtil.getLog(getClass()).info("isNew=" + isNew + " kind=" + kind);
		
		
		if (kind==1 || kind==3 || kind==5) {
			direction = "c";
		}
		else {
			direction = "l";
		}
		
		CFGParser cfgparser = new CFGParser();
		try {
			cfgparser.parse("config.xml");
		} catch (Exception e) {
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
		int flag = 0;
		if (kind==1) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginA;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==2) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndA;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}
		else if (kind==3) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginB;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==4) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndB;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}		
		else if (kind==5) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkBeginC;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0)
				timeMin = 0;
			else if (timeMin > latevalue) {
				flag = 1; // 上班迟到
			}
		}
		else if (kind==6) {
			String t = DateUtil.format(kqTime.getTime(), "yyyy-MM-dd") + " " + checkEndC;
			timeMin = DateUtil.datediffMinute(kqTime.getTime(), DateUtil.parse(t, "yyyy-MM-dd HH:mm"));
			if (timeMin < 0) {
				timeMin = -timeMin;
				if (timeMin > latevalue)
					flag = 2; // 下班早退
			}
			else
				timeMin = 0;
		}
		
		boolean re = false;

		kq.setFlag(flag);
		kq.setTimeMin(timeMin);
		kq.setKind(kind);
		kq.setMyDate(kqTime.getTime());
		kq.setReason(reason);
		if (isNew) {
			kq.setReason(reason);
			kq.setType("考勤");
			kq.setName(userName);		
			kq.setDirection(direction);			
			re = kq.create();
		}
		else {
			//re = kq.save();
		}
		
        return re;
    }
}
