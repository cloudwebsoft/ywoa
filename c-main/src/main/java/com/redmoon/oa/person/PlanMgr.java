package com.redmoon.oa.person;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import cn.js.fan.db.Conn;
import cn.js.fan.util.*;

import java.sql.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;

import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class PlanMgr {
  // 初始月份及各月份天数数组
  String[] months = {
      "一　月", "二　月", "三　月", "四　月", "五　月", "六　月", "七　月", "八　月", "九　月", "十　月",
      "十一月", "十二月"};
  int daysInMonth[] = {
      31, 28, 31, 30, 31, 30, 31, 31,
      30, 31, 30, 31};

  int displayMonth;
  int displayYear;
  
  int todayYear;
  int todayMonth;
  int todayDay;
  Privilege privilege = null;

  public PlanMgr() {
    privilege = new Privilege();
    
    Calendar cal = Calendar.getInstance();
    displayYear = cal.get(Calendar.YEAR);
    displayMonth = cal.get(Calendar.MONTH);

    todayYear = displayYear;
    todayMonth = displayMonth;
    todayDay = cal.get(Calendar.DAY_OF_MONTH);
  }

	public int getDays(int month, int year) {
		// 测试选择的年份是否是润年？
		if (1 == month)
			return ((0 == year % 4) && (0 != (year % 100)))
					|| (0 == year % 400) ? 29 : 28;
		else
			return daysInMonth[month];
	}

  public String newCalendar(String user,int displayYear,int displayMonth) {
    this.displayYear = displayYear;
    this.displayMonth = displayMonth;
    Calendar newCal = Calendar.getInstance();
    newCal.set(displayYear, displayMonth, 1);
    int day = -1;
    int startDayOfWeek = newCal.get(newCal.DAY_OF_WEEK );
    if ( (todayYear == newCal.get(newCal.YEAR)) &&
        (todayMonth == newCal.get(newCal.MONTH))) {
      day = todayDay;
    }
    int intDaysInMonth = getDays(newCal.get(newCal.MONTH), newCal.get(newCal.YEAR));
    String daysGrid = makeDaysGrid(user,startDayOfWeek, day, intDaysInMonth, newCal);
    return daysGrid;
  }
/*
  function incMonth(delta, eltName) {
    displayMonth += delta;
    if (displayMonth >= 12) {
      displayMonth = 0;
      incYear(1, eltName);
    }
    else if (displayMonth <= -1) {
      displayMonth = 11;
      incYear( -1, eltName);
    }
    else {
      newCalendar(eltName);
    }
  }

  function incYear(delta, eltName) {
    displayYear = parseInt(displayYear + '') + delta;
    newCalendar(eltName);
  }
*/
  /**
   * 此方法在2.2版中有运用,但新版中无用
   */
  public String changeCld_Script(String virtualpath) {
    String str = "";
    str = "<script language=javascript>\n";
    str += "<!--\n";
    str += "function changeCld(y,m)\n";
    str += "{\n";
    str += "document.location.href=\"" + virtualpath + "?displayMonth=\"+displayMonth.value+\"&displayYear=\"+displayYear.value\n";
    str += "}\n";
    str += "-->\n";
    str += "</script>\n";
    return str;
  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException {
	  boolean re = true;
      String title, content, mydate, endDate;
      String errmsg = "";
      boolean isRemind = false;
      int id = Integer.parseInt(request.getParameter("id"));
      title = ParamUtil.get(request, "title");
      if (title.equals("")) {
          errmsg += "标题不能为空！\\n";
      }
      content = ParamUtil.get(request, "content");
      if (content.equals("")) {
          errmsg += "内容不能为空！\\n";
      }
      mydate = ParamUtil.get(request, "myDate");
      //mydate = mydate.replace("/", "-")+":00";
		//time = ParamUtil.get(request, "time");
		endDate = ParamUtil.get(request, "endDate");

		if ("".equals(endDate)) {
            endDate = mydate;
        }
		//if(!"".equals(endDate)){
			//endDate = endDate.replace("/", "-")+":00";
		//}else{
			//endDate = mydate;
		//}
		
      isRemind = ParamUtil.getBoolean(request, "remind", false);
      int before = ParamUtil.getInt(request, "before", 0);
      if (isRemind) {
    	  if (before==0) {
              errmsg += "请选择提醒时间!\\n";
          }
      }
      
      boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
	  boolean isNotepaper = ParamUtil.getInt(request, "isNotepaper", 0)==1;
	  boolean isClosed = ParamUtil.getInt(request, "isClosed", 0)==1;

      java.util.Date d = null;
      java.util.Date end = null;
      try {
          d = DateUtil.parse(mydate, "yyyy-MM-dd HH:mm:ss");
          end = DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss");
      }
      catch (Exception e) {
          LogUtil.getLog(getClass()).error("create:" + e.getMessage());
      }
      if (d==null||end==null) {
          errmsg += "日期格式错误！\\n";
      }

      if (DateUtil.compare(d, end) == 1) {
    	  errmsg += "开始日期不能大于结束日期！\\n";
      }
		
      boolean isShared = ParamUtil.getInt(request, "shared", 0)==1;
      
      if (!errmsg.equals("")) {
          throw new ErrMsgException(errmsg);
      }
      PlanDb pd = new PlanDb();
      pd.setId(id);
      pd.setTitle(title);
      pd.setContent(content);
      pd.setMyDate(d);
      pd.setEndDate(end);
      pd.setUserName(privilege.getUser(request));
      if (isRemind) {
          pd.setRemind(isRemind);
          java.util.Date dt = DateUtil.addMinuteDate(d, -before);
          pd.setRemindDate(dt);
      }else{
    	  pd.setRemind(isRemind);
      }
      pd.setRemindBySMS(isRemindBySMS);
      pd.setNotepaper(isNotepaper);
      pd.setClosed(isClosed);
      pd.setShared(isShared);
      re = pd.save();
      return re;
  }
  
  /*
   *  发现struts 调用ParamUtil.get 方法获取含有中文的会出现乱码，估计是重复设定字符集
   *  所以重新建立个方法以request.getParameter方法来获取
   */
  public boolean modifyByParameter(HttpServletRequest request) throws ErrMsgException {
	  boolean re = true;
      String title, content, mydate, time,endDate,endTime;
      String errmsg = "";
      boolean isRemind = false;
      int id = Integer.parseInt(request.getParameter("id"));
      title = request.getParameter("title");
      if (title.equals(""))
          errmsg += "标题不能为空！\\n";
      content = request.getParameter("content");
      if (content.equals(""))
          errmsg += "内容不能为空！\\n";
      mydate = ParamUtil.get(request, "mydate");
      time = ParamUtil.get(request, "time");
      endDate = ParamUtil.get(request, "enddate");
      endTime = ParamUtil.get(request, "endtime");
      isRemind = ParamUtil.getBoolean(request, "isRemind", false);
      int before = ParamUtil.getInt(request, "before", 0);
      boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
      boolean isClosed = ParamUtil.getInt(request, "isClosed", 0) == 1;
      java.util.Date d = null;
      java.util.Date end = null;
      try {
          d = DateUtil.parse(mydate + " " + time, "yyyy-MM-dd HH:mm:ss");
          end = DateUtil.parse(endDate + " "+ endTime, "yyyy-MM-dd HH:mm:ss");
      }
      catch (Exception e) {
          LogUtil.getLog(getClass()).error("create:" + e.getMessage());
      }
      if (d==null||end==null)
          errmsg += "日期格式错误！\\n";
      if (!errmsg.equals("")) {
          throw new ErrMsgException(errmsg);
      }
      PlanDb pd = new PlanDb(id);
      if(pd!=null && pd.isLoaded()){
	     pd.setTitle(title);
	      pd.setContent(content);
	      pd.setMyDate(d);
	      pd.setEndDate(end);
	      pd.setUserName(privilege.getUser(request));
	      if (isRemind == true) {
	          pd.setRemind(isRemind);
	          java.util.Date dt = DateUtil.addMinuteDate(d, -before);
	          pd.setRemindDate(dt);
	      }else{
	    	  pd.setRemind(isRemind);
	      }
	      pd.setRemindBySMS(isRemindBySMS);
	      pd.setClosed(isClosed);
	      re = pd.save();
      }else{
    	  re = false;
      }
 
      return re;
  }
  
  public boolean modifyPeriodicity(HttpServletRequest request) throws ErrMsgException {
      boolean re = true;
      String title, startDate, endDate, content,remindDate;
      String startTime, endTime, remindTime;
      String errmsg = "";
      String isRemind;
      int type,id;
      id = Integer.parseInt(request.getParameter("id"));
      title = ParamUtil.get(request, "title");
      if (title.equals(""))
          errmsg += "标题不能为空！\\n";
      startDate = ParamUtil.get(request, "startdate");
      startTime = ParamUtil.get(request, "starttime");
      endDate = ParamUtil.get(request, "enddate");
      endTime = ParamUtil.get(request, "endtime");
      remindDate = ParamUtil.get(request, "reminddate");
      remindTime = ParamUtil.get(request, "remindtime");
      type = Integer.parseInt(ParamUtil.get(request, "type"));
      //isRemind = ParamUtil.get(request, "isRemind");
      //if (isRemind.equals(""))
          //isRemind = "0";
      //int before = ParamUtil.getInt(request, "before");
      //boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
      content = ParamUtil.get(request, "content");
      if (content.equals(""))
          errmsg += "内容不能为空！\\n";
      java.util.Date start = null;
      java.util.Date end = null;
      java.util.Date remind = null;
      try {
          start = DateUtil.parse(startDate + " " + startTime, "yyyy-MM-dd HH:mm:ss");
          end = DateUtil.parse(endDate + " " + endTime, "yyyy-MM-dd HH:mm:ss");
          remind = DateUtil.parse(remindDate + " " + remindTime, "yyyy-MM-dd HH:mm:ss");
      }
      catch (Exception e) {
          LogUtil.getLog(getClass()).error("create:" + e.getMessage());
      }
      if (start==null )
          errmsg += "日期格式错误！\\n";
      
      if (DateUtil.compare(start, end)==1) {
    	  errmsg += "开始日期不能大于结束日期！\\n";
      }
      
      if (!errmsg.equals("")) {
          throw new ErrMsgException(errmsg);
      }

      PlanDb pd = new PlanDb();
      pd.setId(id);
      pd.setTitle(title);
      pd.setContent(content);
      pd.setMyDate(start);
      pd.setEndDate(end);
      pd.setRemindDate(remind);
      pd.setRemindType(type);
      pd.setUserName(privilege.getUser(request));
      //pd.setRemindBySMS(isRemindBySMS);
      re = pd.save();
      return re;
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
		boolean re = true;
		String title, content, mydate, endDate;
		String errmsg = "";
		boolean isRemind = false;

		title = ParamUtil.get(request, "title");
		if ("".equals(title)) {
            errmsg += "标题不能为空！\n";
        }
		content = ParamUtil.get(request, "content");
		if ("".equals(content)) {
            errmsg += "内容不能为空！\n";
        }
		mydate = ParamUtil.get(request, "myDate");
		endDate = ParamUtil.get(request, "endDate");
		isRemind = ParamUtil.getBoolean(request, "remind", false);
		int before = ParamUtil.getInt(request, "before", 0);
		if (isRemind) {
			if (before == 0) {
                errmsg += "请选择提醒时间!\n";
            }
		}
	      
		boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
		
		boolean isNotepaper = ParamUtil.getInt(request, "isNotepaper", 0)==1;
		boolean isClosed = ParamUtil.getInt(request, "isClosed", 0)==1;

		java.util.Date d = null;
		java.util.Date end = null;
		try {
			d = DateUtil.parse(mydate, "yyyy-MM-dd HH:mm:ss");
			end = DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss");
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("create:" + e.getMessage());
		}
		if (d == null || end == null) {
            errmsg += "日期未填写或格式错误！\n";
        }
		
		if (DateUtil.compare(d, end)==1) {
			errmsg += "开始日期不能大于结束日期！\n";
		}
		
		if (!"".equals(errmsg)) {
			throw new ErrMsgException(errmsg);
		}

		String userName = ParamUtil.get(request, "userName");
		if ("".equals(userName)) {
			userName = privilege.getUser(request);
		}

		String maker = userName;
		if (!userName.equals(privilege.getUser(request))) {
			if (!(privilege.canAdminUser(request, userName))) {
				throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
			}
			
			maker = privilege.getUser(request);
		}
		
		boolean isShared = ParamUtil.getInt(request, "shared", 0)==1;

		PlanDb pd = new PlanDb();
		pd = pd.getLastNotepaper(userName);
		int x = PlanDb.DEFAULT_X;
		int y = PlanDb.DEFAULT_y;
		if (pd!=null) {
			x = pd.getX() + 30;
			y = pd.getY() + 30;
		}
		else {
			pd = new PlanDb();
		}
		
		pd.setTitle(title);
		pd.setContent(content);
		pd.setMyDate(d);
		pd.setEndDate(end);
		pd.setUserName(userName);
		if (isRemind) {
			pd.setRemind(isRemind);
			java.util.Date dt = DateUtil.addMinuteDate(d, -before);
			pd.setRemindDate(dt);
		} else {
			pd.setRemind(isRemind);
		}
		pd.setRemindBySMS(isRemindBySMS);
		pd.setNotepaper(isNotepaper);
		pd.setClosed(isClosed);
		pd.setMaker(maker);
		pd.setShared(isShared);
		pd.setX(x);
		pd.setY(y);
		re = pd.create();
		return re;
	}

  /*
   *  发现struts 调用ParamUtil.get 方法获取含有中文的会出现乱码，估计是重复设定字符集
   *  所以重新建立个方法以request.getParameter方法来获取
   */
  public boolean createByParameter(HttpServletRequest request) throws ErrMsgException {
		boolean re = true;
		String title, content, mydate, time, endDate, endTime;
		String errmsg = "";
		boolean isRemind = false;

		title = request.getParameter("title");
		if (title.equals(""))
			errmsg += "标题不能为空！\\n";
		content = request.getParameter("content");
		if (content.equals(""))
			errmsg += "内容不能为空！\\n";
		mydate = ParamUtil.get(request, "mydate");
		time = ParamUtil.get(request, "time");
		endDate = ParamUtil.get(request, "enddate");

		if (endDate.equals(""))
			endDate = mydate;

		endTime = ParamUtil.get(request, "endtime");
		isRemind = ParamUtil.getBoolean(request, "isRemind", false);
		int before = ParamUtil.getInt(request, "before");
		boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
		boolean isClosed = ParamUtil.getInt(request, "isClosed", 0) == 1;
		java.util.Date d = null;
		java.util.Date end = null;
		try {
			d = DateUtil.parse(mydate + " " + time, "yyyy-MM-dd HH:mm:ss");
			end = DateUtil.parse(endDate + " " + endTime, "yyyy-MM-dd HH:mm:ss");
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error("create:" + e.getMessage());
		}
		if (d == null || end == null)
			errmsg += "日期未填写或格式错误！\\n";
		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}

		String userName = ParamUtil.get(request, "userName");
		if (userName.equals("")) {
			userName = privilege.getUser(request);
		}

		if (!userName.equals(privilege.getUser(request))) {
			if (!(privilege.canAdminUser(request, userName))) {
				throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
			}
		}

		PlanDb pd = new PlanDb();
		pd.setTitle(title);
		pd.setContent(content);
		pd.setMyDate(d);
		pd.setEndDate(end);
		pd.setUserName(userName);
		if (isRemind == true) {
			pd.setRemind(isRemind);
			java.util.Date dt = DateUtil.addMinuteDate(d, -before);
			pd.setRemindDate(dt);
		} else {
			pd.setRemind(isRemind);
		}
		pd.setRemindBySMS(isRemindBySMS);
		pd.setClosed(isClosed);
		re = pd.create();
		return re;
	}    

  public boolean createPeriodicity(HttpServletRequest request) throws ErrMsgException {
      boolean re = true;
      String title, startDate, endDate, content,remindDate;
      String startTime, endTime, remindTime;
      String errmsg = "";
      String isRemind;
      int type;
      title = ParamUtil.get(request, "title");
      if (title.equals(""))
          errmsg += "标题不能为空！\\n";
      startDate = ParamUtil.get(request, "startdate");
      startTime = ParamUtil.get(request, "starttime");
      endDate = ParamUtil.get(request, "enddate");
      endTime = ParamUtil.get(request, "endtime");
      remindDate = ParamUtil.get(request, "reminddate");
      remindTime = ParamUtil.get(request, "remindtime");
      type = Integer.parseInt(ParamUtil.get(request, "type"));
      //isRemind = ParamUtil.get(request, "isRemind");
      //if (isRemind.equals(""))
          //isRemind = "0";
      //int before = ParamUtil.getInt(request, "before");
      //boolean isRemindBySMS = ParamUtil.getBoolean(request, "isToMobile", false);
      content = ParamUtil.get(request, "content");
      if (content.equals(""))
          errmsg += "内容不能为空！\\n";
      java.util.Date start = null;
      java.util.Date end = null;
      java.util.Date remind = null;
      try {
          start = DateUtil.parse(startDate + " " + startTime, "yyyy-MM-dd HH:mm:ss");
          end = DateUtil.parse(endDate + " " + endTime, "yyyy-MM-dd HH:mm:ss");
          remind = DateUtil.parse(remindDate + " " + remindTime, "yyyy-MM-dd HH:mm:ss");
      }
      catch (Exception e) {
          LogUtil.getLog(getClass()).error("create:" + e.getMessage());
      }
      if (start==null )
          errmsg += "日期格式错误！\\n";
      if (DateUtil.compare(start, end)==1) {
    	  errmsg += "开始日期不能大于结束日期！\\n";
      }
      
      if (!errmsg.equals("")) {
          throw new ErrMsgException(errmsg);
      }
      
	  boolean isNotepaper = ParamUtil.getInt(request, "isNotepaper", 0)==1;
      
      PlanDb pd = new PlanDb();
      pd.setTitle(title);
      pd.setContent(content);
      pd.setMyDate(start);
      pd.setEndDate(end);
      pd.setRemindDate(remind);
      pd.setRemindType(type);
      pd.setUserName(privilege.getUser(request));
      //pd.setRemindBySMS(isRemindBySMS);
      pd.setNotepaper(isNotepaper);
      re = pd.create();
      return re;
  }
  /**
   * 取得某月中每天的第一个计划置于数组中
   * @param user String
   * @param year int
   * @param month int
   * @return String[][]
   */
  public String[][] getPlanOfMonth(String user, int year, int month) {
      String sql =
              "select id,title,mydate from user_plan where userName=?";
      sql += " and mydate>=? and mydate<=? order by mydate asc";

      Calendar cal = Calendar.getInstance();
      cal.set(year,month,1,0,0,0);
      java.util.Date d1 = cal.getTime();
      cal.set(year,month,DateUtil.getDayCount(year, month),23,59,59);
      java.util.Date d2 = cal.getTime();

      ResultSet rs = null;
      String[][] ary = new String[32][2];
      for (int i = 0; i <= 31; i++) {
          ary[i][0] = "-1";
      }

      Conn conn = new Conn(Global.getDefaultDB());
      try {
          PreparedStatement ps = conn.prepareStatement(sql);
          ps.setString(1, user);
          ps.setTimestamp(2, new Timestamp(d1.getTime()));
          ps.setTimestamp(3, new Timestamp(d2.getTime()));
          rs = conn.executePreQuery();
          if (rs != null) {
              while (rs.next()) {
                  java.util.Date d = rs.getTimestamp(3);
                  cal.setTime(d);
                  int day = cal.get(Calendar.DAY_OF_MONTH);
                  ary[day][0] = rs.getString(1);
                  ary[day][1] = rs.getString(2);
              }
          }
      } catch (SQLException e) {
          LogUtil.getLog(getClass()).error("getPlanOfMonth:" + e.getMessage());
      } finally {
          if (conn != null) {
              conn.close();
              conn = null;
          }
      }
      return ary;
  }

  public String makeDaysGrid(String user,int startDay, int day, int intDaysInMonth, Calendar newCal) {
    String daysGrid;
    int month = newCal.get(newCal.MONTH);
    int year = newCal.get(newCal.YEAR);
    boolean isThisYear = (year == todayYear);
    boolean isThisMonth = (day > -1);
    int forwardyear = displayYear+1;
    int backwordyear = displayYear-1;
    int forwardmonth = displayMonth +1;
    int backwordmonth = displayMonth -1;

    String[][] planary = getPlanOfMonth(user,displayYear,displayMonth);

    daysGrid = "<table align=center cellSpacing='0' borderColorDark='#ffffff' bgColor='#ebebeb' borderColorLight='#000000' border='1' width=80%>";
    daysGrid += "<tr bgColor='#336699' height=30><td align=center colspan=7 nowrap>";
    daysGrid += "<font style='FONT-SIZE: 9pt' color='#ffffff'>&nbsp;&nbsp;&nbsp;&nbsp;查看&nbsp;<select style='FONT-SIZE: 9pt' onchange='changeCld()' id='displayYear' name='displayYear'>";
    for (int k=-50; k<50; k++)
    {
      daysGrid += "<option value="+(displayYear+k)+">"+(displayYear+k)+"</option>";
    }
    daysGrid += "</select>&nbsp;年";

    daysGrid += "<script language=javascript>\n";
    daysGrid += "<!--\n";
    daysGrid += "displayYear.value=\""+displayYear+"\"\n";
    daysGrid += "-->\n";
    daysGrid += "</script>\n";

    daysGrid += "<select style='FONT-SIZE: 9pt' onchange='changeCld()' name='displayMonth' id='displayMonth'>";
    for (int k=1; k<=12; k++) {
      daysGrid += "<option value="+(k-1)+">"+k+"</option>";
    }
    daysGrid += "</select>";

    daysGrid += "<script language=javascript>\n";
    daysGrid += "<!--\n";
    daysGrid += "displayMonth.value=\""+displayMonth+"\"\n";
    daysGrid += "-->\n";
    daysGrid += "</script>\n";

    daysGrid += "月</font>&nbsp;&nbsp;&nbsp;&nbsp;";
    daysGrid += "<a style='color:white' href='?displayYear="+displayYear+"&displayMonth="+backwordmonth+"')>&laquo;</a>&nbsp;";
    daysGrid += "<b>";
    if (isThisMonth) {
      daysGrid += "<font color=yellow>" + months[month] + "</font>";
    }
    else {
      daysGrid += "<font color=white>" + months[month] + "</font>";
    }
    daysGrid += "</b>";
    daysGrid += "&nbsp;<a style='color:white' href='?displayMonth="+forwardmonth+"&displayYear="+displayYear+"')>&raquo;</a>";
    daysGrid += "&nbsp;&nbsp;&nbsp;";
    daysGrid += "<a style='color:white' href='?displayMonth="+displayMonth+"&displayYear="+backwordyear+"')>&laquo;</a>&nbsp;";
    daysGrid += "<b>";
    if (isThisYear) {
      daysGrid += "<font color=yellow>" +year + "&nbsp;年</font>";
    }
    else {
      daysGrid += "<font color=white>" +year + "&nbsp;年</font>";
    }
    daysGrid += "</b>";
    daysGrid += "&nbsp;<a style='color:white' href='?displayMonth="+displayMonth+"&displayYear="+forwardyear+"')>&raquo;</a></font></td></tr>";
    daysGrid += "<tr bgColor=#bebebe><td align=center><font color=red>日</font></td><td align=center>一</td><td align=center>二</td>";
    daysGrid += "<td align=center>三</td><td align=center>四</td><td align=center>五</td><td align=center><font color=red>六</font></td></tr>";
    int dayOfMonthOfFirstSunday = 7-startDay+2;//(7 - startDay + 1)//在javascript中因为星期是从0-6计数的,而java中则是从1-7计数;
    int count = 0;
    int dayOfMonth = 0;
    for (int intWeek = 0; intWeek < 6; intWeek++) {
      daysGrid += "<tr height=40>";
      for (int intDay = 0; intDay < 7; intDay++) {
        dayOfMonth = (intWeek * 7) + intDay + dayOfMonthOfFirstSunday - 7;
        if (dayOfMonth <= 0) {
          daysGrid += "<td>&nbsp;</td>";
        }
        else if (dayOfMonth <= intDaysInMonth) {
          count++;
          String color = "black";
          String bgcolor = "yellow";
          if (intDay==0 || intDay==6) {
            color = "red";
          }
          if (day > 0 && day == dayOfMonth) {//置当前日期前景色
            color = "blue";
          }
          if (day >0 && day <=dayOfMonth) {//如果计划在当前日期之后
            bgcolor = "#99CCFF";
          }

          daysGrid += "<td align=center ";
          if (!planary[count][0].equals("-1")) //有计划则高亮显示
              daysGrid += "bgcolor=" + bgcolor;
          daysGrid += " ><b><a ";
          if (planary[count][0].equals("-1"))
              daysGrid += "href=\"#\"";
          else { // 如果有计划则生成链接
              // daysGrid += "title=\"" + planary[count][1] +
              //        "\" href='plan_show.jsp?id=" + planary[count][0] + "'";
              daysGrid += "title=\"" + planary[count][1] +
                      "\" href='plan_list.jsp?year=" + year + "&month=" + (month+1) + "&day=" + dayOfMonth + "'";
          }
          daysGrid += " style='font-size:15pt;FONT-FAMILY:Arial;color:" +
                  color + "'>";
          String dayString = dayOfMonth + "</a></b>";
          //if (dayString.length() == 13)
          //  dayString = "0" + dayString;
          daysGrid += dayString + "</td>";
        }
      }
      int dayspan = dayOfMonth - count;
      if (dayOfMonth < intDaysInMonth)
        daysGrid += "</tr>";
      else {
          if (dayspan < 7 && dayspan > 0) {
            for (int k = 0; k < dayspan; k++) {
              daysGrid += "<td>&nbsp;</td>";
            }
            daysGrid += "</tr>";
         }
        }
    }
    return daysGrid + "</table>";
  }

  public PlanDb getPlanDb(int id) {
      PlanDb pd = new PlanDb();
      return pd.getPlanDb(id);
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
		int id = ParamUtil.getInt(request, "id");
		PlanDb pd = getPlanDb(id);
		if (pd == null || !pd.isLoaded())
			throw new ErrMsgException("该计划已不存在！");
		String userName = pd.getUserName();
		if (!userName.equals(privilege.getUser(request))) {
			if (!(privilege.canAdminUser(request, userName))) {
				throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
			}
		}
		if (!pd.getMaker().equals("")) {
			// 如果是他人布置的工作安排，则不允许本人删除
			if (pd.getUserName().equals(privilege.getUser(request)) && !pd.getMaker().equals(privilege.getUser(request))) {
				throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));				
			}
			// 只有制定者及部门管理员才可以删除
			if (privilege.getUser(request).equals(pd.getMaker()) || privilege.canAdminUser(request, userName))
				;
			else
				throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
		}

		return pd.del();
	}

	public void setClosedBatch(HttpServletRequest request)
			throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");

		if (ids == "")
			return;

		String[] ary = StrUtil.split(ids, ",");
		for (int i = 0; i < ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			PlanDb pd = getPlanDb(id);
			if (pd == null || !pd.isLoaded())
				throw new ErrMsgException("该计划已不存在！");
			String userName = pd.getUserName();
			if (!userName.equals(privilege.getUser(request))) {
				if (!(privilege.canAdminUser(request, userName))) {
					throw new ErrMsgException(SkinUtil.LoadString(request,
							"pvg_invalid"));
				}
			}

			pd.setClosed(true);
			pd.save();
		}

	}
	
	public void setSharedBatch(HttpServletRequest request)
			throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");

		if (ids == "")
			return;

		String[] ary = StrUtil.split(ids, ",");
		for (int i = 0; i < ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			PlanDb pd = getPlanDb(id);
			if (pd == null || !pd.isLoaded())
				throw new ErrMsgException("该计划已不存在！");
			String userName = pd.getUserName();
			if (!userName.equals(privilege.getUser(request))) {
				if (!(privilege.canAdminUser(request, userName))) {
					throw new ErrMsgException(SkinUtil.LoadString(request,
							"pvg_invalid"));
				}
			}

			pd.setShared(true);
			pd.save();
		}
	}	
	
	public void setNotSharedBatch(HttpServletRequest request)
		throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");

		if (ids == "")
			return;

		String[] ary = StrUtil.split(ids, ",");
		for (int i = 0; i < ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			PlanDb pd = getPlanDb(id);
			if (pd == null || !pd.isLoaded())
				throw new ErrMsgException("该计划已不存在！");
			String userName = pd.getUserName();
			if (!userName.equals(privilege.getUser(request))) {
				if (!(privilege.canAdminUser(request, userName))) {
					throw new ErrMsgException(SkinUtil.LoadString(request,
							"pvg_invalid"));
				}
			}

			pd.setShared(false);
			pd.save();
		}
	}
	
	
	public void setNotClosedBatch(HttpServletRequest request)
			throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");

		if (ids == "")
			return;

		String[] ary = StrUtil.split(ids, ",");
		for (int i = 0; i < ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			PlanDb pd = getPlanDb(id);
			if (pd == null || !pd.isLoaded())
				throw new ErrMsgException("该计划已不存在！");
			String userName = pd.getUserName();
			if (!userName.equals(privilege.getUser(request))) {
				if (!(privilege.canAdminUser(request, userName))) {
					throw new ErrMsgException(SkinUtil.LoadString(request,
							"pvg_invalid"));
				}
			}

			pd.setClosed(false);
			pd.save();
		}

	}	
	
	public void setNotepaperBatch(HttpServletRequest request)
			throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");

		if (ids == "")
			return;

		String[] ary = StrUtil.split(ids, ",");
		for (int i = 0; i < ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			PlanDb pd = getPlanDb(id);
			if (pd == null || !pd.isLoaded())
				throw new ErrMsgException("该计划已不存在！");
			String userName = pd.getUserName();
			if (!userName.equals(privilege.getUser(request))) {
				if (!(privilege.canAdminUser(request, userName))) {
					throw new ErrMsgException(SkinUtil.LoadString(request,
							"pvg_invalid"));
				}
			}

			pd.setNotepaper(true);
			pd.save();
		}

	}	
	
	
	public void setNotNotepaperBatch(HttpServletRequest request)
			throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");

		if (ids == "")
			return;

		String[] ary = StrUtil.split(ids, ",");
		for (int i = 0; i < ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			PlanDb pd = getPlanDb(id);
			if (pd == null || !pd.isLoaded())
				throw new ErrMsgException("该计划已不存在！");
			String userName = pd.getUserName();
			if (!userName.equals(privilege.getUser(request))) {
				if (!(privilege.canAdminUser(request, userName))) {
					throw new ErrMsgException(SkinUtil.LoadString(request,
							"pvg_invalid"));
				}
			}

			pd.setNotepaper(false);
			pd.save();
		}

	}		
	
	/**
	 * 批量删除
	 * @param request
	 * @throws ErrMsgException
	 */
	public void delBatch(HttpServletRequest request) throws ErrMsgException {
		String ids = ParamUtil.get(request, "ids");

		if (ids == "")
			return;

		String[] ary = StrUtil.split(ids, ",");
		for (int i = 0; i < ary.length; i++) {
			int id = StrUtil.toInt(ary[i]);
			PlanDb pd = getPlanDb(id);
			if (pd == null || !pd.isLoaded())
				throw new ErrMsgException("该计划已不存在！");
			String userName = pd.getUserName();
			if (!userName.equals(privilege.getUser(request))) {
				if (!(privilege.canAdminUser(request, userName))) {
					throw new ErrMsgException(SkinUtil.LoadString(request,
							"pvg_invalid"));
				}
			}
			
			if (!pd.getMaker().equals("")) {
				// 如果是他人布置的工作安排，则不允许本人删除
				if (pd.getUserName().equals(privilege.getUser(request)) && !pd.getMaker().equals(privilege.getUser(request))) {
					throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));				
				}
				// 只有制定者及部门管理员才可以删除
				if (privilege.getUser(request).equals(pd.getMaker()) || privilege.canAdminUser(request, userName))
					;
				else
					throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));				
			}			

			pd.del();
		}

	}
  
	/**
	 * 显示链接动作
	 * @param request
	 * @return
	 */
	public static String renderAction(HttpServletRequest request, PlanDb pd) {
		if (PlanDb.ACTION_TYPE_SALES_VISIT == pd.getActionType()) {
			com.redmoon.oa.flow.FormDb fd = new com.redmoon.oa.flow.FormDb();
			String formCode = "day_lxr";
			fd = fd.getFormDb(formCode);
			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
			long id = StrUtil.toLong(pd.getActionData(), -1);
			if (id==-1)
				return "No data";
			fdao = fdao.getFormDAO(id, fd);
			String lxrId = fdao.getFieldValue("lxr");
			fd = fd.getFormDb("sales_linkman");
			fdao = fdao.getFormDAO(StrUtil.toLong(lxrId), fd);
			// return "<a target='_blank' href='" + request.getContextPath() + "/visual/module_show.jsp?id=" + pd.getActionData() + "&action=&formCode=day_lxr&isShowNav=0'>点击查看</a>";			
			return "<a href='javascript:;' onclick=\"addTab('行动', '" + request.getContextPath() + "/sales/customer_visit_list.jsp?customerId=" + fdao.getFieldValue("customer") + "')\">点击查看</a>";			
		}
		else if (PlanDb.ACTION_TYPE_FLOW == pd.getActionType()) {
			MyActionDb mad = new MyActionDb();
			mad = mad.getMyActionDb(StrUtil.toLong(pd.getActionData()));
			WorkflowMgr wfm = new WorkflowMgr();
			WorkflowDb wf = wfm.getWorkflowDb((int)mad.getFlowId());
			com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
			lf = lf.getLeaf(wf.getTypeCode());
			
			if (lf!=null) {
				WorkflowPredefineDb wpd = new WorkflowPredefineDb();
				wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
				
				if (pd.isClosed()) {
					if (lf!=null && lf.getType()==com.redmoon.oa.flow.Leaf.TYPE_LIST) {
						return "<a href='javascript:;' onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + wf.getId() + "')\">查看流程</a>";
					} else {
						if (wpd.isLight()) {
							return "<a href='javascript:;' onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flow_dispose_light_show.jsp?flowId=" + wf.getId() + "')\">查看流程</a>";
						} else {
							return "<a href='javascript:;' onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + wf.getId() + "')\">查看流程</a>";
						}
					}
				} else {
					if (lf!=null && lf.getType()==com.redmoon.oa.flow.Leaf.TYPE_LIST) {
						return "<a href='javascript:;' onclick=\"addTab('处理流程', '" + request.getContextPath() + "/flowDispose.do?myActionId=" + pd.getActionData() + "')\">处理流程</a>";
					} else {
						if (wpd.isLight()) {
							return "<a href='javascript:;' onclick=\"addTab('处理流程', '" + request.getContextPath() + "/flow_dispose_light.jsp?myActionId=" + pd.getActionData() + "')\">处理流程</a>";
						} else {
							return "<a href='javascript:;' onclick=\"addTab('处理流程', '" + request.getContextPath() + "/flowDisposeFree.do?myActionId=" + pd.getActionData() + "')\">处理流程</a>";
						}
					}
				}
			}
		}	
		else if (PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE == pd.getActionType()) {
			return "<a href='javascript:;' onclick=\"addTab('收文', '" + request.getContextPath() + "/paper/paper_show.jsp?paperId=" + pd.getActionData() + "')\">查看</a>";			
		}
		return "";
	}  
}
