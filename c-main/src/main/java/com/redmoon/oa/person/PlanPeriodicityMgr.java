package com.redmoon.oa.person;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.db.SequenceManager;
import java.util.*;
import java.sql.SQLException;

public class PlanPeriodicityMgr {
    public PlanPeriodicityMgr() {
    }
    
    public boolean create(HttpServletRequest request) throws ErrMsgException,
            ResKeyException {
        PlanPeriodicityDb ppd = new PlanPeriodicityDb();
        Privilege privilege = new Privilege();
        boolean re = false;
        String errmsg = "";
        int id = (int) SequenceManager.nextID(SequenceManager.PLAN_PERIODICITY);
        String content = "", startTime = "", beginDate = "",
                endDate = "", endTime = "", beginTime = "";
        int type = 0;String week = "";
        String remindDate = "", remindTime = "", day = "", month = "";
        String title = ParamUtil.get(request, "title");
    
        String startdate = ParamUtil.get(request, "startdate");
        //startTime = ParamUtil.get(request, "starttime");
        endDate = ParamUtil.get(request, "enddate");
        //endTime = ParamUtil.get(request, "endtime");
        content = ParamUtil.get(request, "content");

        remindTime = ParamUtil.get(request, "remindtime");
        if (remindTime.equals("")) {
            errmsg += "提醒时间不能为空！\\n";
        }
        else {
            if (remindTime.length()==8) {
                //remindTime = remindTime.substring(0, 5);
            }
        }

        type = ParamUtil.getInt(request, "type");

		String userName = ParamUtil.get(request, "userName");
		if (userName.equals("")) {
			userName = privilege.getUser(request);
		}

		if (!userName.equals(privilege.getUser(request))) {
			if (!(privilege.canAdminUser(request, userName))) {
				throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
			}
		}

        if (type == 1) {
            remindDate = " ";
        } else if (type == 2) {
            week = ParamUtil.get(request, "week");
            remindDate = week;
        } else if (type == 3) {
            day = ParamUtil.get(request, "day");
            remindDate = day;
        } else {
            month = ParamUtil.get(request, "month");
            day = ParamUtil.get(request, "day");
            remindDate = month + "-" +day;
        }
        Date begin = null;
        Date end = null;
        //Date remind = null;
        try {
            begin = DateUtil.parse(startdate,"yyyy-MM-dd HH:mm:ss");
            end = DateUtil.parse(endDate , "yyyy-MM-dd HH:mm:ss");
            //remind = DateUtil.parse(remindTime,"HH:mm:ss");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }

        if (title.length()==0) {
            errmsg += "标题不能为空！\\n";
        }
        if (content.length()==0) {
            errmsg += "内容不能为空！\\n";
        }
        if (begin == null || end == null) {
            errmsg += "日期格式错误！\\n";
        }
        
        if (DateUtil.compare(begin, end)==1) {
      	  errmsg += "开始日期不能大于结束日期！\\n";
        }        
        
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        re = ppd.create(new JdbcTemplate(), new Object[] {
            new Integer(id),
                    title,
                    begin,
                    end,
                    remindTime,
                    new Integer(type),
                    content,
                    remindDate,
                    userName
        });
        return re;
    }
    public boolean modify(HttpServletRequest request) throws ErrMsgException,
            ResKeyException, SQLException {
        PlanPeriodicityDb ppd = new PlanPeriodicityDb();
        Privilege privilege = new Privilege();
        boolean re = false;
        String errmsg = "";
        int id = ParamUtil.getInt(request,"id");
        String title = "", content = "", startTime = "", beginDate = "",
                endDate = "", endTime = "", beginTime = "";
        int type = 0;String week = "";
        String remindDate = "", remindTime = "", day = "", month = "";
        title = ParamUtil.get(request, "title");
        
        String startdate = ParamUtil.get(request, "startdate");
        startTime = ParamUtil.get(request, "starttime");
        endDate = ParamUtil.get(request, "enddate");
        endTime = ParamUtil.get(request, "endtime");
        content = ParamUtil.get(request, "content");
        remindTime = ParamUtil.get(request, "remindtime");
        if (remindTime.equals("")) {
            errmsg += "提醒时间不能为空！\\n";
        }
        else {
            if (remindTime.length()==8) {
                remindTime = remindTime.substring(0, 5);
            }
        }

        type = ParamUtil.getInt(request, "type");
        String userName = privilege.getUser(request);
        if (type == 1) {
            remindDate = " ";
        } else if (type == 2) {
            week = ParamUtil.get(request, "week");
            remindDate = week;
        } else if (type == 3) {
            day = ParamUtil.get(request, "day");
            remindDate = day;
        } else {
            month = ParamUtil.get(request, "month");
            day = ParamUtil.get(request, "day");
            remindDate = month + "-" +day;
        }
        Date begin = null;
        Date end = null;
       // Date remind = null;

       // Time t = new Time();
        try {
            begin = DateUtil.parse(startdate + " " + startTime,
                                   "yyyy-MM-dd HH:mm:ss");
            end = DateUtil.parse(endDate + " " + endTime, "yyyy-MM-dd HH:mm:ss");
           // remind = DateUtil.parse(remindTime,"HH:mm:ss");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }

        if (title.length()==0) {
            errmsg += "标题不能为空！\\n";
        }
        if (content.length()==0) {
            errmsg += "内容不能为空！\\n";
        }

        if (begin == null || end == null) {
            errmsg += "日期格式错误！\\n";
        }
        
        if (DateUtil.compare(begin, end)==1) {
        	  errmsg += "开始日期不能大于结束日期！\\n";
        }   
        
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        ppd = ppd.getPlanPeriodicityDb(id);
        ppd.set("title",title);
        ppd.set("begin_date",begin);
        ppd.set("end_date",end);
        ppd.set("remind_time",remindTime);
        ppd.set("remind_type",new Integer(type));
        ppd.set("content",content);
        ppd.set("remind_date",remindDate);
        ppd.set("user_name",userName);
        ppd.set("remind_count",0);
        re = ppd.save();
        /** re = ppd.save(new JdbcTemplate(), new Object[] {
                    title,
                    begin,
                    end,
                    remind,
                    new Integer(type),
                    content,
                    remindDate,
                    userName,
                    new Integer(id)
        });**/
        return re;
    }
    public boolean del(HttpServletRequest request) throws ErrMsgException,
            ResKeyException {
        PlanPeriodicityDb ppd = new PlanPeriodicityDb();
        boolean re = false;
        int id = ParamUtil.getInt(request,"id");
        ppd = ppd.getPlanPeriodicityDb(id);
        re = ppd.del();
        return re;
    }
}
