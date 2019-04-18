package com.redmoon.oa.android.i;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.*;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.person.UserDb;


 /**
 * @Description: 修改个人信息接口
 * @author: lichao
 * @Date: 2015-7-15上午10:41:40
 */
public class ModifyPersonInforAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //修改个人信息成功
	
	private String skey = "";
	private String realName = "";
	private String birthday = "";
	private String qq = "";
	private String address = "";
	private int gender ;
	private String result = "";
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		boolean flag = true;
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(skey);
		
		if(re){
			try {
				jReturn.put("res",RES_EXPIRED);
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		try {
			String name = privilege.getUserName(skey);
			UserDb ud = new UserDb(name);
			
			ud.setRealName(realName);
			
			if(!"".equals(birthday)){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(birthday);
				ud.setBirthday(date);
			}
			
			ud.setQQ(qq);
			ud.setAddress(address);
			ud.setGender(gender);
			re = ud.save();
			
			if(re){
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS);
				jReturn.put("result", jResult);
			}	
		} catch (JSONException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally{
			if(!flag){
				try {
					jReturn.put("res", RES_FAIL);
					jResult.put("returnCode", "");
					jReturn.put("result", jResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
