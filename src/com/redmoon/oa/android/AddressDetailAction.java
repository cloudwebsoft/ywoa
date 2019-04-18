package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.address.AddressDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;

public class AddressDetailAction {

	private String skey = "";
	private String result = "";  
	private String id;
	private int addressType;
	
	public int getAddressType() {
		return addressType;
	}
	public void setAddressType(int addressType) {
		this.addressType = addressType;
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
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
		
		if(addressType == -1){
			UserDb userDb = new UserDb();
			userDb = userDb.getUserDb(id);
			DeptUserDb deptUser = new DeptUserDb();
			Vector<DeptDb> v = deptUser.getDeptsOfUser(id);
			Iterator<DeptDb> it =v.iterator();
			String deptname ="";
			while (it.hasNext()){
				DeptDb dept = it.next();
				if(deptname!=null && !deptname.equals("")){
					deptname += ","+dept.getName();
				}else{
					deptname = dept.getName();
				}
			}
			
			try {
				json.put("res","0");
				json.put("msg","操作成功");
				json.put("姓名",StrUtil.getNullStr(userDb.getRealName()));
				json.put("昵称", StrUtil.getNullStr(userDb.getRealName()));
				json.put("部门", StrUtil.getNullStr(deptname));
				json.put("科室", StrUtil.getNullStr(userDb.getParty()));
				json.put("职务",  StrUtil.getNullStr(userDb.getDuty()));
				json.put("手机",  StrUtil.getNullStr(userDb.getMobile()));
				json.put("办公室电话", StrUtil.getNullStr(userDb.getPhone()));
				json.put("小灵通", StrUtil.getNullStr(userDb.getPhone()));
				json.put("Email",StrUtil.getNullStr(userDb.getEmail()));
				json.put("住宅所在", StrUtil.getNullStr(userDb.getAddress()));
				json.put("住宅电话", StrUtil.getNullStr(userDb.getMobile()));
				json.put("住宅传真","");
				json.put("QQ",StrUtil.getNullStr(userDb.getQQ()));
				json.put("短号",StrUtil.getNullStr(userDb.getPhone()));
				json.put("网页", StrUtil.getNullStr(userDb.getState()));
				json.put("邮政编码", StrUtil.getNullStr(userDb.getPostCode()));
				json.put("业务传真","");
				json.put("地址", StrUtil.getNullStr(userDb.getAddress()));
				json.put("附注","");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			AddressDb addr = new AddressDb();
			addr = addr.getAddressDb(Integer.parseInt(id));
			try {
				json.put("res","0");
				json.put("msg","操作成功");
				json.put("姓名", addr.getPerson());
				json.put("昵称", addr.getNickname());
				json.put("部门", addr.getDepartment());
				json.put("科室", addr.getCompany());
				json.put("职务", addr.getJob());
				json.put("手机", addr.getMobile());
				json.put("办公室电话", addr.getOperationPhone());
				json.put("小灵通",addr.getBeepPager());
				json.put("Email",addr.getEmail());
				json.put("住宅所在", addr.getStreet());
				json.put("住宅电话", addr.getTel());
				json.put("住宅传真", addr.getFax());
				json.put("QQ", addr.getQQ());
				json.put("短号",addr.getMSN());
				json.put("网页", addr.getWeb());
				json.put("邮政编码", addr.getCompanyPostcode());
				json.put("业务传真", addr.getOperationFax());
				json.put("地址", addr.getAddress());
				json.put("附注", addr.getIntroduction());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
			
		setResult(json.toString());
		return "SUCCESS";
	}	
}
