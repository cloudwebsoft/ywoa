package com.redmoon.oa.person;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.ui.menu.WallpaperDb;
import com.redmoon.oa.usermobile.UserMobileMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

public class UserSetupMgr {

	/**
	 * 根据用户名 判断 是否绑定手机
	 * @param userName
	 * @return
	 */
	public boolean isBindMobile(String userName){
		String sql = "SELECT count(user_name)  FROM  user_setup where user_name = "+StrUtil.sqlstr(userName)+" and is_bind_mobile = 1";
		boolean flag = false;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				int result = rr.getInt(1);
				if(result >= 1){
					flag = true;
				}
			}
		} catch (SQLException e) {
			flag = false;
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
		return flag;
	}
	
	/**
	 * 根据用户名 判断 是否绑定手机 
	 * @param userName
	 * @author lichao
	 * @return
	 */
	public boolean isBindMobileModify(String userName){
		String sql = "SELECT * FROM  user_setup where user_name = ? and is_bind_mobile = 1";
		boolean flag = true;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql,new Object[]{userName});
			if (!ri.hasNext()){
				flag = false;
			}
		} catch (SQLException e) {
			flag = false;
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
		return flag;
	}
}
