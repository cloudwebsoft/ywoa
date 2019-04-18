package com.redmoon.oa.dept;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.*;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;

import com.redmoon.dingding.service.user.UserService;
import com.redmoon.weixin.mgr.WXUserMgr;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.*;
import rtx.RTXUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.Config;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sso.SyncUtil;

public class DeptUserMgr {

    public DeptUserMgr() {
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        DeptUserCheck du = new DeptUserCheck();
        du.checkAdd(request);
        
        String userNames = du.getUserName();
        String[] ary = StrUtil.split(userNames, ",");
        for (int i=0; i<ary.length; i++) {
        	changeDeptOfUser(ary[i], du.getDeptCode(), new Privilege().getUser(request));
        }
        return true;
    }

    /**
     * 该方法已无用20120515，统一使用调入功能，也便于与Spark对接
     * @param request
     * @return
     */
    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        DeptUserCheck ugc = new DeptUserCheck();
        ugc.checkModify(request);

        DeptUserDb jd = getDeptUserDb(ugc.getId());
        String oldUserName = jd.getUserName();
        jd.setUserName(ugc.getUserName());
        jd.setDeptCode(ugc.getDeptCode());
        jd.setRank(ugc.getRank());
        boolean re = jd.save();
        if (re) {
            // if (!oldUserName.equals(ugc.getUserName())) {
                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
                if (isRTXUsed) {
                    DeptDb dd = new DeptDb();
                    dd = dd.getDeptDb(ugc.getDeptCode());
                    UserDb user = new UserDb();
                    user = user.getUserDb(ugc.getUserName());
                    RTXUtil.addUserToDept(user, "", dd.getName(), true);
                    if (!oldUserName.equals(ugc.getUserName())) {
	                    user = user.getUserDb(oldUserName);
	                    RTXUtil.delUserFromDept(user, dd.getName());
                    }
                }
            // }
        }
        return re;
    }

    public DeptUserDb getDeptUserDb(int id) {
        DeptUserDb du = new DeptUserDb();
        return du.getDeptUserDb(id);
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        DeptUserCheck jc = new DeptUserCheck();
        jc.checkDel(request);

        DeptUserDb jd = getDeptUserDb(jc.getId());
        return jd.del();
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        DeptUserCheck jc = new DeptUserCheck();
        jc.checkMove(request);

        DeptUserDb job = getDeptUserDb(jc.getId());
        return job.move(jc.getDirection());
    }
    
    
    /**
     * 调动部门，调动时将会脱离原来所处的部门，用户如果原来处于多个部门，都会被脱离
     * @param userName
     * @param deptCodes
     * @return
     */
    public boolean changeDeptOfUser(String userName, String deptCodes, String opUser) throws ErrMsgException {
    	// 脱离所来所处的部门
    	DeptUserDb dud = new DeptUserDb();
    	// 重新安排部门
    	String[] ary = StrUtil.split(deptCodes, ",");
    	// 删除原来所属的部门
    	// dud.delUser(userName);
    	    	
    	String deptCode = "";
    	boolean re = true;
    	if (ary!=null) {
    		deptCode = ary[0];
    		
        	Vector v = dud.getDeptsOfUser(userName);
        	
    		int len = ary.length;

    		// 检查用户原来所属的部门， 如果原来的部门不变，则不作处理
    		
    		// 如果原部门在新部门中不存在，则删除，并整理顺序
        	Iterator ir = v.iterator();
        	while (ir.hasNext()) {
        		DeptDb dd = (DeptDb)ir.next();
        		boolean isFound = false;
	    		for (int i=0; i<len; i++) {
	    			if (ary[i].equals(dd.getCode())) {
	    				isFound = true;
	    				break;
	    			}
	    		}
	    		if (!isFound) {
	    			// 在新选部门中没找到，则删除
	    			DeptUserDb duOld = dud.getDeptUserDb(userName, dd.getCode());
	    			duOld.del();
	    		}
        	}
        	
        	// 取得新增部门，并添加至新选部门末尾
    		for (int i=0; i<len; i++) {
    			ir = v.iterator();
				boolean isFound = false;
    			while (ir.hasNext()) {
    				DeptDb dd = (DeptDb)ir.next();
	    			if (ary[i].equals(dd.getCode())) {
	    				isFound = true;
	    				break;
	    			}
    			}
    			if (!isFound) {
    				re = dud.create(ary[i], userName, "");
    			}
    		}
    		
    		if (len>=1) {
    			// 置为排在第一个的部门所在的单位
    			DeptDb dd = new DeptDb();
            	dd = dd.getDeptDb(deptCode);
            	String unitCode = dd.getUnitOfDept(dd).getCode();

            	UserDb user = new UserDb();
            	user = user.getUserDb(userName);
            	user.setUnitCode(unitCode);
            	user.save();
            	
            	// 人事档案信息
            	Config cfg = new Config();
            	boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");
            	if (isArchiveUserSynAccount) {
            		String sql = "update form_table_personbasic set dept=" + StrUtil.sqlstr(deptCode) + " where user_name=" + StrUtil.sqlstr(userName);
            		JdbcTemplate jt = new JdbcTemplate();
            		try {
						jt.executeUpdate(sql);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}

				com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
				if (ssoCfg.getBooleanProperty("isUse")) {
					SyncUtil su = new SyncUtil();
					user = user.getUserDb(userName);
					// 同步排在第一的那个部门
					su.userSync(user, deptCode, SyncUtil.MODIFY, opUser);
				}

				// 同步到微信企业号
				com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
				if (weixinCfg.getBooleanProperty("isUse") && !weixinCfg.getBooleanProperty("isSyncWxToOA")) {
					WXUserMgr _wxUserMgr = new WXUserMgr();
					_wxUserMgr.updateWxUser(user);
				}
				com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
				if(dingdingCfg.isUseDingDing() && !dingdingCfg.getBooleanProperty("isSyncDingDingToOA")) {
					UserService _userService = new UserService();
					_userService.updateUser(user);
				}
    		}
    	}    	
    	else {
        	Vector v = dud.getDeptsOfUser(userName);
        	Iterator ir = v.iterator();
        	while (ir.hasNext()) {
        		DeptDb dd = (DeptDb)ir.next();
	    		DeptUserDb duOld = dud.getDeptUserDb(userName, dd.getCode());
	    		duOld.del();
        	}
    	}

    	return re;
    }
    
    /**
     * 同步所有用户的单位
     * @Description:
     */
    public void syncUnit() {
    	String sql = "select code from department where dept_type="
    		+ DeptDb.TYPE_UNIT + " and code<>"
    		+ StrUtil.sqlstr(DeptDb.ROOTCODE)
    		+ " order by layer asc,orders desc";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri = null;
    	ArrayList<String> list = new ArrayList<String>();
    	try {
    		ri = jt.executeQuery(sql);
    		while (ri != null && ri.hasNext()) {
    			ResultRecord rr = (ResultRecord)ri.next();
    			list.add(rr.getString(1));
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	
    	Config cfg = new Config();
    	boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");
    	
    	for (String deptCode : list) {
    		DeptDb deptDb = new DeptDb(deptCode);
    		Vector vector = new Vector();
    		try {
				vector = deptDb.getAllChild(vector, deptDb);
			} catch (ErrMsgException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		vector.add(deptDb);				
    		Iterator it = vector.iterator();
    		while (it.hasNext()) {
    			DeptDb ddb = (DeptDb)it.next();
    			/*sql = "update users set unit_code=" 
    				+ StrUtil.sqlstr(deptCode) 
    				+ " where unit_code<>"
    				+ StrUtil.sqlstr(ddb.getCode())
    				+ " and exists (select id from dept_user where user_name=name and dept_code=" 
    				+ StrUtil.sqlstr(ddb.getCode())
    				+ ")";*/
    			
    			sql = "update users set unit_code="
    				+ StrUtil.sqlstr(deptCode)
    				+ " where name in (select user_name from dept_user d where d.dept_code=" 
    				+ StrUtil.sqlstr(ddb.getCode())
    				+ ")";
    			try {
    				jt.executeUpdate(sql);
    			} catch (SQLException e) {
    				e.printStackTrace();
    			}
    			
    			if (isArchiveUserSynAccount) {
        			sql = "update form_table_personbasic set unit_code=" 
        				+ StrUtil.sqlstr(deptCode)
        				+ " where user_name in (select user_name from dept_user d where d.dept_code=" 
        				+ StrUtil.sqlstr(ddb.getCode())
        				+ ")";        		
        			
        			// System.out.println(getClass() + " sql=" + sql);
        			try {
        				jt.executeUpdate(sql);
        			} catch (SQLException e) {
        				e.printStackTrace();
        			}    				
    			}
    			
    		}
    	}
    	try {
    		// 清缓存
    		RMCache rmcache = RMCache.getInstance();
    		rmcache.clear();				
    	} catch (CacheException e) {
    		e.printStackTrace();
    	}    	
    }

}
