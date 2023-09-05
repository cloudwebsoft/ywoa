package com.redmoon.oa.usermobile;

import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserSetupMgr;

public class UserMobileMgr {
	private UserMobileDb userMobileDb;
	public UserMobileMgr(){
		userMobileDb = new UserMobileDb();
	}
	/**
	 * 新增userMobile
	 * @param userName
	 * @param deviceId
	 * @param client
	 * @param status
	 * @return
	 */
	public boolean create(String userName,String deviceId,String client,int status){
		JdbcTemplate jt = new JdbcTemplate();
		boolean flag = false;
		try {
			flag = userMobileDb.create(jt, new Object[]{userName,deviceId,client,status});
		} catch (ResKeyException e) {
			flag = false;
			LogUtil.getLog(UserMobileMgr.class).error(e.getMessage());
		}
		return flag;
	}
	/**
	 * 更新userMobile
	 * @param id
	 * @param status
	 * @return
	 */
	public boolean update(long id,int status){
		boolean flag = false;
		userMobileDb = userMobileDb.getUserMobileDb(id);
		userMobileDb.set("status", status);
		try {
			flag = userMobileDb.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			flag = false;
			LogUtil.getLog(UserMobileMgr.class).error(e.getMessage());
		}
		return flag;
	}
	/**
	 * 根据用户名  硬件设备ID 删除手机状态表数据
	 * @param name
	 * @return
	 */
	public boolean deleteByName(String name){
		String sql = "delete from user_mobile where userName = "+StrUtil.sqlstr(name);
		JdbcTemplate jt = new JdbcTemplate();
		boolean  flag = false;
		int result;
		try {
			result = jt.executeUpdate(sql);
			if(result >= 0){
				flag = true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			flag = false;
			LogUtil.getLog(UserMobileMgr.class).error(e.getMessage());
		}
		return flag;
	}
	/**
	 * jdbcTemplate基本查询
	 * @param sql
	 * @return
	 */
	public boolean query(String sql){
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
			LogUtil.getLog(UserSetupMgr.class).error(e.getMessage());
		}
		return flag;
	}
	/**
	 * 判断是否绑定硬件标识
	 * @param userName
	 * @return
	 */
	public  boolean isBindMobile(String userName){
		String sql = "SELECT count(id)  FROM  user_mobile where userName = "+StrUtil.sqlstr(userName) +" and status = 1";
		boolean result = false;
		result = query(sql);
		return result;
	}
	
	/**
	 * 判断是否绑定硬件标识
	 * @param userName
	 * @author lichao
	 * @return
	 */
	public  boolean isBindMobileModify(String userName){
		String sql = "SELECT * FROM  user_mobile where userName = ? and status = 1";
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
			LogUtil.getLog(UserSetupMgr.class).error(e.getMessage());
		}
		return flag;
	}
	
	/**
	 * 根据用户名 deviceId判断 绑定硬件标识是否正确
	 * @param userName
	 * @param deviceId
	 * @return
	 */
	public  boolean isExistBindRecord(String userName,String deviceId){
		String sql = "SELECT count(id)  FROM  user_mobile where userName = "+StrUtil.sqlstr(userName) +"and deviceId = "+StrUtil.sqlstr(deviceId)+" and status = 1";
		boolean result = false;
		result = query(sql);
		return result;
	}
}
