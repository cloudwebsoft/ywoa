package com.redmoon.oa.db;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;

public class SQLUtil {

    private static Calendar getADayOfWeek(Calendar day, int dayOfWeek) {
        int week = day.get(Calendar.DAY_OF_WEEK);
        if (week == dayOfWeek)
            return day;
        int diffDay = dayOfWeek - week;
        if (week == Calendar.SUNDAY) {
            diffDay -= 7;
        } else if (dayOfWeek == Calendar.SUNDAY) {
            diffDay += 7;
        }
        day.add(Calendar.DATE, diffDay);
        return day;
    }

    public static Calendar getFirstDayOfWeek(Calendar day) {
        Calendar monday = (Calendar) day.clone();
        return getADayOfWeek(monday, Calendar.MONDAY);
    }
    
    /**
     * 解析脚本中的变量
     * @Description: 
     * @param request
     * @param script
     * @return
     */
    public static String parseScript(HttpServletRequest request, String script) {
    	Privilege pvg = new Privilege();
    	Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(script);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String str = m.group(1);
            String val = "";
            if (str.startsWith("request.")) {
            	String key = str.substring("request.".length());
            	val = ParamUtil.get(request, key);
            }
            else if (str.equalsIgnoreCase("admin.dept")) {
				try {
					Iterator ir = pvg.getUserAdminDepts(request).iterator();
	            	while (ir.hasNext()) {
	            		DeptDb dd = (DeptDb)ir.next();
	            		if (val.equals("")) {
	            			val = StrUtil.sqlstr(dd.getCode());
	            		}
	            		else {
	            			val += "," + StrUtil.sqlstr(dd.getCode());
	            		}
	            	}
				} catch (ErrMsgException e) {
                    LogUtil.getLog(SQLUtil.class).error(e);
				}
            }
            else if (str.equalsIgnoreCase("curUser")) {
            	val = StrUtil.sqlstr(pvg.getUser(request));
            }     
            else if (str.equalsIgnoreCase("curDate")) {
            	val = SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
            }
            else if (str.equalsIgnoreCase("curUserDept")) { // 当前用户所在的部门
            	DeptUserDb dud = new DeptUserDb();
            	Vector v = dud.getDeptsOfUser(pvg.getUser(request));
            	if (v.size()>0) {
            		Iterator ir = v.iterator();
            		while (ir.hasNext()) {
            			DeptDb dd = (DeptDb)ir.next();
            			if ("".equals(val)) {
            				val = StrUtil.sqlstr(dd.getCode());
            			}
            			else {
            				val += "," + StrUtil.sqlstr(dd.getCode());
            			}
            		}
            	}
            	else {
            		val = "''";
            	}
            }
            else if (str.equalsIgnoreCase("curUserRole")) {
            	UserDb ud = new UserDb();
            	ud = ud.getUserDb(pvg.getUser(request));
            	RoleDb[] ary = ud.getRoles();
            	if (ary!=null && ary.length>0) {
            		for (int i=0; i<ary.length; i++) {
            			if ("".equals(val)) {
            				val = StrUtil.sqlstr(ary[i].getCode());
            			}
            			else {
            				val = "," + StrUtil.sqlstr(ary[i].getCode());            				
            			}
            		}
            	}
            	else {
            		val = "''";
            	}
            }
           
            m.appendReplacement(sb, val);
        }    	
        m.appendTail(sb);    
        
        return sb.toString();
    }
    
    /**
     * 用于SQLCtl及QueryScriptUtil中替换相应符号，如：$curDate
     * @param sql
     * @param userName
     * @return
     */
	public static String change(String sql, String userName) {
		if(sql==null) {
			return "";
		}
        // 当前用户
        if (sql.indexOf("$curUser")!=-1) {
        	sql = sql.replaceAll("\\$curUser", userName);
        }
        // 本日
        if (sql.indexOf("$curDate")!=-1) {
            // 对时间进行变换
        	sql = sql.replaceAll("\\$curDate", SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss"));
        }
        // 本月第一天
        if (sql.indexOf("$firstDayOfCurMonth")!=-1) {
            java.util.Date now = new java.util.Date();
            int mon = DateUtil.getMonth(now);
            int y = DateUtil.getYear(now);
            java.util.Date d = DateUtil.getDate(y, mon, 1);
            sql = sql.replaceAll("\\$firstDayOfCurMonth", SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss"));
        }
        // 本周第一天
        if (sql.indexOf("$firstDayOfCurWeek")!=-1) {
            java.util.Date d = getFirstDayOfWeek(Calendar.getInstance()).getTime();
            sql = sql.replaceAll("\\$firstDayOfCurWeek", SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss"));
        }
        // 本年第一天
        if (sql.indexOf("$firstDayOfCurYear")!=-1) {
        	Calendar cal = Calendar.getInstance();
        	cal.set(cal.get(Calendar.YEAR), 0, 1, 1, 1, 1);
            java.util.Date d = cal.getTime();
            sql = sql.replaceAll("\\$firstDayOfCurYear", SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss"));
        }        
        return sql;
		
	}
}
