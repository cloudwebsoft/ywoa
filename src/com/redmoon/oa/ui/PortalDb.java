package com.redmoon.oa.ui;

import java.sql.SQLException;
import java.util.Iterator;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PortalDb extends QObjectDb {
	
	/**
	 * 表示不属于系统门户，如用户或者admin新建一门户
	 */
	public static final int SYSTEM_ID_NONE = 0;
	
    public PortalDb() {
    }

    /**
     * 取得下一个排序号
     * @param userName String
     * @return int
     */
    public int getNextOrders(String userName) {
        String sql = "select max(orders) from " + getTable().getName() + " where user_name=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {userName});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return rr.getInt(1) + 1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    /**
     * 取得默认门户
     * @param userName String
     * @return PortalDb
     */
    public PortalDb getDefaultPortalOfUser(String userName) {
    	if (userName==null) {
    		return null;
    	}
    	
    	Privilege pvg = new Privilege();
        String sql = "select id from " + getTable().getName() + " where user_name='" + UserDb.SYSTEM + "' order by orders asc";    	
    	if (!pvg.isUserPrivValid(userName, "admin") && pvg.isUserPrivValid(userName, "desk.default.forbid")) {
    		// 禁用默认界面，默认桌面为orders为1的门户
            sql = "select id from " + getTable().getName() + " where user_name='" + UserDb.SYSTEM + "' and orders<>1 order by orders asc";    		
    	}
    	
        // String sql = "select id from " + getTable().getName() + " where user_name=? and orders=1";
    	// fgf 20161013 改为取系统用户的门户

		Iterator ir = list(sql).iterator();
	    while (ir.hasNext()) {
	       PortalDb pd = (PortalDb)ir.next();
	       if (pd.canUserSee(userName)) {
	    	   return pd;
	       }
	    }			

        return null;
    }

    /**
     * 删除某用户的门户
     * @param userName String
     */
    public void deleteOfUser(String userName) {
    	if (!userName.equals("system"))
    	{
	    	UserDesktopSetupDb udsd = new UserDesktopSetupDb();
	        
	        String sql = "select id from " + getTable().getName() + " where user_name=? order by orders";
	        Iterator ir = list(sql, new Object[] {userName}).iterator();
	        while (ir.hasNext()) {
	            PortalDb pd = (PortalDb) ir.next();
	            
	            udsd.delDesktopOfPortal(pd.getLong("id"));
	
	            try {
	                pd.del();
	            } catch (ResKeyException ex) {
	                ex.printStackTrace();
	            }
	        }
    	}


    }
        
    public boolean isSystem() {
    	return getString("user_name").equals(UserDb.SYSTEM);
    }

    /**
     * 初始化某用户的门户
     * @param userName String
     */
    public void init(String userName) {
        // 删除用户的门户
        deleteOfUser(userName);

        // 过滤掉固定的门户 fgf 20160929
        String sql = "select id from " + getTable().getName() + " where user_name=? and is_fixed=0 order by orders";

        UserDesktopSetupDb udsd = new UserDesktopSetupDb();

        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(UserDb.ADMIN);
        
        // 创建门户
        if (!userName.equals("system"))
        {
	        PortalDb pd2 = new PortalDb();
	        String sqlGetPortalId = "select id from " + getTable().getName() + " where user_name=? and orders=?";
	        Iterator ir = list(sql, new Object[]{UserDb.SYSTEM}).iterator();
	        while (ir.hasNext()) {
	            PortalDb pd = (PortalDb)ir.next();
	            // 取出groupId
	            long portalId = -1;
	            try {
	                int orders = pd.getInt("orders");
	                int isFixed = pd.getInt("is_fixed");
	                String depts = pd.getString("depts");
	                String roles = pd.getString("roles");
	                pd2.create(new JdbcTemplate(), new Object[] {
	                    userName, pd.getString("name"), new Integer(orders), new Long(pd.getLong("id")), pd.getString("icon"),isFixed,depts,roles
	                });
	
	                JdbcTemplate jt = new JdbcTemplate();
	                ResultIterator ri = jt.executeQuery(sqlGetPortalId, new Object[]{userName, new Integer(orders)});
	                if (ri.hasNext()) {
	                    ResultRecord rr = (ResultRecord)ri.next();
	                    portalId = rr.getLong(1);
	                }
	            } catch (ResKeyException ex1) {
	                ex1.printStackTrace();
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	
	            udsd.initDesktopOfUser(pd, portalId, userName);
	            
	            // 置用户的天气、时钟、日历与管理员一样
	            /*
	            UserSetupDb usdUser = new UserSetupDb();
	            usdUser = usdUser.getUserSetupDb(userName);
	            usdUser.setWeatherCode(usd.getWeatherCode());
	            usdUser.setClockCode(usd.getClockCode());
	            usdUser.setCalendarCode(usd.getCalendarCode());
	            usdUser.save();
	            */
	            
	        	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	        	String weatherCode = cfg.get("weatherCode");
	        	String clockCode = cfg.get("clockCode");
	        	String calendarCode = cfg.get("calendarCode");
	        	
	            UserSetupDb usdUser = new UserSetupDb();
	            usdUser = usdUser.getUserSetupDb(userName);
	            usdUser.setWeatherCode(weatherCode);
	            usdUser.setClockCode(clockCode);
	            usdUser.setCalendarCode(calendarCode);
	            usdUser.save();        	
	        }
        }
    }
    
    /**
     * 取得與systemId對應的所有用戶的門戶

     * @param systemId 系统门户的id
     * @return
     */
    public PortalDb getPortal(String userName, long systemId) {
        String sql = "select id from oa_portal where user_name=" + StrUtil.sqlstr(userName) + " and system_id=" + systemId;
        Iterator ir = list(sql).iterator();
        if (ir.hasNext()) {
        	return (PortalDb)ir.next();
        }
        return null;
    }
    
    public boolean canUserSee(String userName) {
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(userName, Privilege.ADMIN)) {
    		return true;
    	}
    	
    	String depts = StrUtil.getNullStr(getString("depts"));
    	String roles = StrUtil.getNullStr(getString("roles"));
    	
    	if (depts.equals("") && roles.equals(""))
    		return true;
    	
    	if (!"".equals(depts)) {
    		String[] ary = StrUtil.split(depts, ",");
    		if (ary!=null) {
    			DeptUserDb dud = new DeptUserDb();
    			for (int i=0; i<ary.length; i++) {
    				if (dud.isUserBelongToDept(userName, ary[i])) {
    					return true;
    				}
    			}
    		}
    	}
    	
    	if (!"".equals(roles)) {
    		String[] ary = StrUtil.split(roles, ",");
    		if (ary!=null) {
    			UserDb user = new UserDb();
    			user = user.getUserDb(userName);
    			for (int i=0; i<ary.length; i++) {
    				if (user.isUserOfRole(ary[i])) {
    					return true;
    				}
    			}
    		}
    	}    	
    	
    	return false;
    }
}
