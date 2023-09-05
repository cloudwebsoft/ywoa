package com.redmoon.oa.hr;

import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-29下午03:23:21
 */
public class KqRulesDB {
	int id;
	String ruleName;
	String deptUserCode;
	int deptUserType;
	String attLocation;
	String longitude;
	String latitude;
	String permissEor;
	int isOpen;
	int isAttendance;
	int isAttendFirst;
	
	public KqRulesDB select(String code){
		String sql = "select * from attedance_rules where dept_user_code ="+StrUtil.sqlstr(code);
		JdbcTemplate jt = new JdbcTemplate();
		KqRulesDB kd = new KqRulesDB();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			if(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				ruleName = rr.getString("ruleName");
				deptUserType = rr.getInt("dept_user_type");
				attLocation = rr.getString("attLocation");
				longitude = rr.getString("longitude");
				latitude = rr.getString("latitude");
				permissEor = rr.getString("permissEor");
				isOpen = rr.getInt("is_open");
				isAttendance = rr.getInt("is_attendance");
				isAttendFirst = rr.getInt("is_attend_first");
			}else{
				ruleName = "";
				deptUserType = 1;
				attLocation = "";
				longitude = "";
				latitude = "";
				permissEor = "0";
				isOpen = 1;
				isAttendance = 1;
				isAttendFirst = 1;
			}
			kd.setRuleName(ruleName);
			kd.setDeptUserCode(code);
			kd.setDeptUserType(deptUserType);
			kd.setAttLocation(attLocation);
			kd.setLongitude(longitude);
			kd.setLatitude(latitude);
			kd.setPermissEor(permissEor);
			kd.setIsOpen(isOpen);
			kd.setIsAttendance(isAttendance);
			kd.setIsAttendFirst(isAttendFirst);
			
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return kd;
	}
	
	public boolean create(String ruleName,String deptUserCode,int deptUserType,String attLocation
			, String longitude,String latitude,String permissEor, int isOpen,int isAttendance,int isAttendFirst){
		boolean re = false;
		String sql = "insert into attedance_rules (ruleName,dept_user_code,dept_user_type,attLocation,longitude,latitude,permissEor,is_open,is_attendance,is_attend_first) values (?,?,?,?,?,?,?,?,?,?)";
		//String sql = "insert into attedance_rules (ruleName,dept_user_code,dept_user_type,attLocation,longitude,latitude,permissEor,is_open,is_attendance,is_attend_first) values ("+StrUtil.sqlstr(ruleName)
		//+","+StrUtil.sqlstr(deptUserCode)+","+deptUserType+","+attLocation
		//+","+StrUtil.sqlstr(longitude)+","+StrUtil.sqlstr(latitude)
		//+","+StrUtil.sqlstr(permissEor)+","+isOpen+","+isAttendance+","+isAttendFirst+")";
		JdbcTemplate jt = new JdbcTemplate();
		try{
			re = jt.executeUpdate(sql,new Object[]{ruleName,deptUserCode,deptUserType,attLocation,longitude,latitude,permissEor,isOpen,isAttendance,isAttendFirst}) >=1 ? true : false;
			//re = jt.executeUpdate(sql) >= 0 ? true :false;
		}
		catch(SQLException e){
		}
		return re;
	}
	
	public boolean del(String code){
		boolean re = false;
		String sql = "delete from attedance_rules where dept_user_code = "+StrUtil.sqlstr(code);
		JdbcTemplate jt = new JdbcTemplate();
		try{
			re = jt.executeUpdate(sql) >= 1 ? true :false;
		}
		catch(SQLException e){
		}
		return re;
	}
	
	public boolean update(String ruleName,String deptUserCode,int deptUserType,String attLocation
			, String longitude,String latitude,String permissEor, int isOpen,int isAttendance,int isAttendFirst){
		boolean re = false;
		String sql ="";
		JdbcTemplate jt = new JdbcTemplate();
		//判断是否地址有填写
		if(attLocation.equals("")){
			sql = "update attedance_rules set ruleName=?,dept_user_code=?,dept_user_type=?,longitude=?,latitude=?,permissEor=?,is_open=?,is_attendance=?,is_attend_first=? where dept_user_code = "+StrUtil.sqlstr(deptUserCode);
			try{
				re = jt.executeUpdate(sql,new Object[]{ruleName,deptUserCode,deptUserType,longitude,latitude,permissEor,isOpen,isAttendance,isAttendFirst}) >=1 ? true : false;
			}
			catch(SQLException e){
			}
		}else{
			sql = "update attedance_rules set ruleName=?,dept_user_code=?,dept_user_type=?,attLocation=?,longitude=?,latitude=?,permissEor=?,is_open=?,is_attendance=?,is_attend_first=? where dept_user_code = "+StrUtil.sqlstr(deptUserCode);
			try{
				re = jt.executeUpdate(sql,new Object[]{ruleName,deptUserCode,deptUserType,attLocation,longitude,latitude,permissEor,isOpen,isAttendance,isAttendFirst}) >=1 ? true : false;
			}
			catch(SQLException e){
			}
		}
		
		return re;
		
	}
	
	public boolean isExit(String code){
		boolean re = false;
		String sql = "select id from attedance_rules where dept_user_code = "+StrUtil.sqlstr(code);
		JdbcTemplate jt = new JdbcTemplate();
		try{
			ResultIterator ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				re = true;
			}
		}
		catch(SQLException e){
		}
		return re;
	}
	
	//循环寻找有数据的父类
	public String searchParentCode(String code, int type){
		boolean re = false;
		String parentCode="";
		if(code.equals("root") || code.equals("-1")){
			parentCode = "root";
			return parentCode;
		}
		
		String sql = "select id from attedance_rules where dept_user_code="+StrUtil.sqlstr(code)+" and dept_user_type="+type;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				re = true;
			}
			
			if(!re){
				if(type==1){
					DeptDb dd= new DeptDb(code);
					String parentDeptCode = dd.getParentCode();
					parentCode = searchParentCode(parentDeptCode,1);
				}else{
					int deptId = Integer.parseInt(code);
					DeptUserDb dud = new DeptUserDb(deptId);
					parentCode = searchParentCode(dud.getDeptCode(),1);
				}
			}else{
				parentCode = code;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return parentCode; 
	}
	
	//判断是否为数字
	public static boolean isNumeric(String str){
	  for (int i = 0; i < str.length(); i++){
		   if (!Character.isDigit(str.charAt(i))){
			   return false;
		   }
	  }
	  return true;
	}
	
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getDeptUserCode() {
		return deptUserCode;
	}
	public void setDeptUserCode(String deptUserCode) {
		this.deptUserCode = deptUserCode;
	}
	public int getDeptUserType() {
		return deptUserType;
	}
	public void setDeptUserType(int deptUserType) {
		this.deptUserType = deptUserType;
	}
	public String getAttLocation() {
		return attLocation;
	}
	public void setAttLocation(String attLocation) {
		this.attLocation = attLocation;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getPermissEor() {
		return permissEor;
	}
	public void setPermissEor(String permissEor) {
		this.permissEor = permissEor;
	}
	public int getIsOpen() {
		return isOpen;
	}
	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}
	public int getIsAttendance() {
		return isAttendance;
	}
	public void setIsAttendance(int isAttendance) {
		this.isAttendance = isAttendance;
	}
	public int getIsAttendFirst() {
		return isAttendFirst;
	}
	public void setIsAttendFirst(int isAttendFirst) {
		isAttendFirst = isAttendFirst;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
