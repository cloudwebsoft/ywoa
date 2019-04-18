package com.redmoon.oa.android;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.address.AddressDb;

public class AddressAddAction {
	private String skey = "";
	private String result = "";
	private String groupType = "";
	private String name = "";
	private String mobile = "";
	private String operationPhone = "";
	private String email = "";
	private String department = "";
	private String job = "";
	private String operationFax = "";
	private String address = "";
	private String companyPostcode = "";
	private String qq = "";
	private String msn = "";
	
	public String getMsn() {
		return msn;
	}
	public void setMsn(String msn) {
		this.msn = msn;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	public String getOperationFax() {
		return operationFax;
	}
	public void setOperationFax(String operationFax) {
		this.operationFax = operationFax;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCompanyPostcode() {
		return companyPostcode;
	}
	public void setCompanyPostcode(String companyPostcode) {
		this.companyPostcode = companyPostcode;
	}
	public String getQq() {
		return qq;
	}
	public void setQq(String qq) {
		this.qq = qq;
	}
	public String getOperationPhone() {
		return operationPhone;
	}
	public void setOperationPhone(String operationPhone) {
		this.operationPhone = operationPhone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getGroupType() {
		return groupType;
	}
	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String execute() {
		JSONObject json = new JSONObject(); 
		
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		if(re){
			try {
				json.put("res","-2");
				json.put("msg","时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		try {
			
			int type = AddressDb.TYPE_USER;
			
			AddressDb ad = new AddressDb();
			ad.setTypeId(getGroupType());
			ad.setPerson(getName());
			ad.setUserName(privilege.getUserName(getSkey()));
			ad.setUnitCode(privilege.getUserUnitCode(getSkey()));
			ad.setMobile(getMobile());
			ad.setMSN(getMsn());
			ad.setOperationPhone(getOperationPhone());
			ad.setEmail(getEmail());
			ad.setDepartment(getDepartment());
			ad.setJob(getJob());
			ad.setOperationFax(getOperationFax());
			ad.setAddress(getAddress());
			ad.setCompanyPostcode(getCompanyPostcode());
			ad.setQQ(getQq());
			ad.setType(type); 
			/*
			AddressDb addressDb = new AddressDb();

			boolean isExist = addressDb.isExist(getName(), StrUtil.toInt(getGroupType()), privilege.getUserUnitCode(getSkey()));
			if(isExist){
				json.put("res","-1");
				json.put("msg","同类别下集团通讯录成员姓名不可以重复！");
				setResult(json.toString());
				return "SUCCESS";
			} 
			*/
			re = ad.create();
			if(re){
				json.put("res","0");
				json.put("msg","添加成功");
			}else{
				json.put("res","-1");
				json.put("msg","添加失败");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}	
}
