package com.redmoon.oa.android.i;

import java.sql.SQLException;

import org.json.*;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.tools.Des3;
import com.redmoon.oa.person.UserDb;


 /**
 * @Description: 更换密码接口
 * @author: lichao
 * @Date: 2015-7-15上午10:41:40
 */
public class ResetPasswordAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //密码更换成功
	private static int RETURNCODE_WRONG_PASSWORD = 1;        //原密码输入不正确
	
	private String skey = "";
	private String password = "";
	private String oldPassword = "";
	private String result = "";

	 public boolean isWap() {
		 return wap;
	 }

	 public void setWap(boolean wap) {
		 this.wap = wap;
	 }

	 private boolean wap = false;
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
		
		String decrypOldPassWord = "";
		if (!isWap()) {
			try {
				decrypOldPassWord = Des3.decode(oldPassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			decrypOldPassWord = oldPassword;
		}
		
		String name = privilege.getUserName(skey);
		UserDb ub = new UserDb(name);
		re = ub.Auth(ub.getName(), decrypOldPassWord);
		
		if(!re){
			try {
				jReturn.put("res",RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_WRONG_PASSWORD);
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		try {
			String decrypPassWord = "";
			if (!isWap()) {
				try {
					decrypPassWord = Des3.decode(password);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				decrypPassWord = password;
			}
			
			String pwdRaw = decrypPassWord;
			String pwdMD5 = SecurityUtil.MD5(pwdRaw);
			
			ub.setPwdMD5(pwdMD5);
			ub.setPwdRaw(pwdRaw);
			re = ub.save();
			
			if(re){
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS);
				jReturn.put("result", jResult);
			}			
		} catch (SQLException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
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
