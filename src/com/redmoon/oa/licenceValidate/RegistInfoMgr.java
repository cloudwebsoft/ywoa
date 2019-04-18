package com.redmoon.oa.licenceValidate;

import org.json.JSONObject;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.db.*;

import java.sql.SQLException;
import java.util.*;


/**
 * @Description: 注册信息保存类
 * @author: lichao	
 * @Date: 2015-8-28下午02:08:08
 */
public class RegistInfoMgr {
	
	//新建
	public boolean create(String enterpriseNo,String entEmail) throws ErrMsgException,ResKeyException {
		boolean re = false;
		
		RegistInfoMgr rr = new RegistInfoMgr();
		re = rr.deleteTable();
		int n =-1;
		
		if(re){
			String sql = "insert into regist_info (enterpriseNo,entEmail,receiveTime,status) values(?,?,?,?)";
			JdbcTemplate jt = new JdbcTemplate();
			
			try {
				n = jt.executeUpdate(sql,new Object[]{enterpriseNo,entEmail,new Date(),1});
			} catch (SQLException e) {
				LogUtil.getLog(getClass()).error(e);
				e.printStackTrace();
			}
		}
		if(n==1){
			re = true;
		}
		
		return re;
	}
	
	//清空表
	public boolean deleteTable(){
		boolean res = false;
		String sql = "delete from regist_info";
		JdbcTemplate jt = new JdbcTemplate();
		int n = -1 ;
		
		try{
			n = jt.executeUpdate(sql);
			if(n>=0){
				res = true;
			}
		}catch(Exception e){
			LogUtil.getLog(getClass()).error(e);
			e.printStackTrace();
		}finally {
			jt.close();
		}
		return res;
	}
	
	//获取企业号
	public boolean getEnterpriseNo(){
		boolean res = false;
		String sql = "select id from regist_info";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		
		try{
			ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				res = true;
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			jt.close();
		}
		
		return res;
	}
	
	//获取企业号
	public JSONObject getEnterpriseJsonObject(){
		String sql = "select max(id),enterpriseNo,entEmail from regist_info";
		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		
		JSONObject jot = new JSONObject();
		
		try{
			ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				jot.put("enterpriseNo", StrUtil.getNullString(rd.getString("enterpriseNo")));
				jot.put("email", StrUtil.getNullString(rd.getString("entEmail")));
			}
		}catch(Exception e){
			LogUtil.getLog(getClass()).error(e);
			e.printStackTrace();
		}finally {
			jt.close();
		}
		
		return jot;
	}

}
