package com.redmoon.oa.attendance;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-9-26下午12:14:19
 */
public class ShiftScheduleMgr {
    final static String group = "ShiftScheduleMgr";
    
    /**
     * 默认排班记录的ID
     */
    private static long defaultScheduleId = -1;
    
    private static boolean defaultUseOACalendar = false;
    
    public static boolean isDefaultUseOACalendar() {
    	return defaultUseOACalendar;
    }
    
    public static void refresh() {
        try  {
        	RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            LogUtil.getLog(ShiftScheduleMgr.class).error(e.getMessage());
        }
    }
    
    public Vector listShift() {
    	Vector v = new Vector();
    	String sql = "select id from ft_shift order by id desc";
    	FormDAO fdao = new FormDAO();
    	try {
			v = fdao.list("shift", sql);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return v;
    }
    
    /**
     * 取得用户的排班ID，通过缓存，如果没有排班，则返回默认排班
     * @Description: 
     * @param userName
     * @return
     */
	public static long getScheduleId(String userName) {
		long fdaoId = -1;
        try {
            fdaoId = (Long)RMCache.getInstance().getFromGroup(userName, group);
        }
        catch (Exception e) {
        	LogUtil.getLog(ShiftScheduleMgr.class).error("getSchedule:" + e.getMessage());
        }
        if (fdaoId==-1) {
			// 取得用户的排班记录
			FormDAO fdao = new FormDAO();
			try {
				long defaultId = -1;
				String sql = "select id from ft_shift_schedule";
				Iterator ir = fdao.list("shift_schedule", sql).iterator();
				while (ir.hasNext()) {
					fdao = (FormDAO)ir.next();
					if ("1".equals(fdao.getFieldValue("is_default"))) {
						defaultId = fdao.getId();
						defaultScheduleId = defaultId;
						defaultUseOACalendar = "1".equals(fdao.getFieldValue("is_oa_calendar"));
					}
					String persons = fdao.getFieldValue("persons");
					String[] ary = StrUtil.split(persons, ",");
					if (ary!=null) {
						for (String person : ary) {
							if (person.equals(userName)) {
		                        try {
		                        	RMCache.getInstance().putInGroup(person, group, fdao.getId());
		                        } catch (Exception e) {
		                        	LogUtil.getLog(ShiftScheduleMgr.class).error("getScheduleId:" + e.getMessage());
		                        }	
		                        return fdao.getId();
							}
						}
					}
				}
				if (defaultId!=-1) {
                    try {
                    	RMCache.getInstance().putInGroup(userName, group, defaultId);
                    } catch (Exception e) {
                    	LogUtil.getLog(ShiftScheduleMgr.class).error("getScheduleId:" + e.getMessage());
                    }	
                    return defaultId;
				}
			} catch (ErrMsgException e) {
				LogUtil.getLog(ShiftScheduleMgr.class).error(e);
			}
        }
		
		return fdaoId;
	}
	
	/**
	 * 根据排班表及排班调整表取得用户于某天的班次记录
	 * @Description: 
	 * @param userName
	 * @param dt
	 * @return
	 */
	public static Object[] getShiftDAO(String userName, java.util.Date dt) {
		// 是否为调整后的排班
		boolean isAdjust = false;
		// 如果排班调整表中有排班，则直接返回
		FormDAO fdao = getShiftAdjust(userName, dt);
		if (fdao!=null) {
			isAdjust = true;
			return new Object[]{fdao, isAdjust};
		}

		Boolean isDefault = false; // 是否为默认排班
		fdao = getShiftNormal(userName, dt, isDefault);
		return new Object[]{fdao, isAdjust, isDefault};
	}
	
	public static FormDAO getShiftNormal(String userName, java.util.Date dt) {
		return getShiftNormal(userName, dt, new Boolean(false));
	}	
	
	/**
	 * 取得用户某天的正常班次，如果没有，则返回null
	 * @param userName
	 * @param dt
	 * @param isDefault 是否默认排班，用于返回
	 * @return
	 */
	public static FormDAO getShiftNormal(String userName, java.util.Date dt, Boolean isDefault) {
		long id = getScheduleId(userName);
		// 如果未排班且没有默认排班，则直接返回
		if (id==-1) {
			return null;
		}
		
		if (id == defaultScheduleId) {
			isDefault = Boolean.valueOf(true);
		}

		FormDAO fdao = new FormDAO();
		FormDb fd = new FormDb();
		fd = fd.getFormDb("shift_schedule");
		fdao = fdao.getFormDAO(id, fd);
		
		String repType = fdao.getFieldValue("repeat_type");
		if (repType.equals("周")) {
			// 星期日为一周的第一天 SUN MON TUE WED THU FRI SAT 
			// DAY_OF_WEEK返回值       1 	2 	3 	4 	5 	6 	7 			
			Calendar cal = Calendar.getInstance();
			cal.setTime(dt);
			int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (weekIndex==0) {
				weekIndex = 7;
			}
			
			long shiftId = StrUtil.toLong(fdao.getFieldValue("shift_week" + weekIndex), -1);
			if (shiftId!=-1) {
				FormDAO fdaoShift = new FormDAO();
				fd = fd.getFormDb("shift");
				return fdaoShift.getFormDAO(shiftId, fd);
			}
			else {
				return null;
			}
		}
		else {
			// 按月重复
			int day = DateUtil.getDay(dt);
			long shiftId = StrUtil.toLong(fdao.getFieldValue("shift_month" + day), -1);
			if (shiftId!=-1) {
				FormDAO fdaoShift = new FormDAO();
				fd = fd.getFormDb("shift");
				return fdaoShift.getFormDAO(shiftId, fd);
			}
			else {
				return null;
			}
		}		
	}
	
	/**
	 * 取得用户某天被调整的班次
	 * @Description: 
	 * @param userName
	 * @param dt
	 * @return
	 */
	public static FormDAO getShiftAdjust(String userName, java.util.Date dt) {
		String strDate = DateUtil.format(dt, "yyyy-MM-dd");
		String sql = "select id from ft_shift_adjust where user_name=" + StrUtil.sqlstr(userName) + " and mydate=" + SQLFilter.getDateStr(strDate, "yyyy-MM-dd");
		String formCode = "shift_adjust";
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		FormDAO fdao = new FormDAO(fd);
		try {
			Vector v = fdao.list(formCode, sql);
			if (v.size()>0) {
				// 已存在，则判断值是否相等，不相等则重写
				Iterator ir = v.iterator();
				if (ir.hasNext()) {
					fdao = (FormDAO)ir.next();
					long shiftId = StrUtil.toLong(fdao.getFieldValue("shift"));
					fd = fd.getFormDb("shift");
					return fdao.getFormDAO(shiftId, fd);
				}
			}
		}
		catch (ErrMsgException e) {
			LogUtil.getLog(ShiftScheduleMgr.class).error(e);
		}
		return null;
	}
	
}
