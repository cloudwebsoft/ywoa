package com.redmoon.oa.android;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ltd.getahead.dwr.util.Logger;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

public class MyinfoEditAction {
	private String skey = "";
	private String result = "";
	private String RealName = "";
	private String Email = "";
	private String phone = "";
	private String mobile = "";
	private String QQ = "";
	private String IDcard = "";
	private String Password = "";
	private String name = "";
	private int isValid;
	private String oldPwd;

	public String getRealName() {
		return RealName;
	}

	public void setRealName(String realName) {
		RealName = realName;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getQQ() {
		return QQ;
	}

	public void setQQ(String qQ) {
		QQ = qQ;
	}

	public String getIDcard() {
		return IDcard;
	}

	public void setIDcard(String iDcard) {
		IDcard = iDcard;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsValid() {
		return isValid;
	}

	public void setIsValid(int isValid) {
		this.isValid = isValid;
	}

	public String getPassword2() {
		return Password2;
	}

	public void setPassword2(String password2) {
		Password2 = password2;
	}

	private String Password2 = "";

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

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		try {
			if (re) {

				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			String userName = privilege.getUserName(getSkey());
			if(name != null && !name.trim().equals("")){
				if(!name.equals(userName)){
					json.put("res", "-1");
					json.put("msg", "不能修改用户名!");
					setResult(json.toString());
					return "SUCCESS";
				}
			}else{
				json.put("res", "-1");
				json.put("msg", "用户名不能为空!");
				setResult(json.toString());
				return "SUCCESS";
			}
			HttpServletRequest request = ServletActionContext.getRequest();
			HttpSession session = request.getSession();
			session.setAttribute(Global.AppName + "_NAME", privilege
					.getUserName(getSkey()));
			session.setAttribute("oa.unitCode", privilege
					.getUserUnitCode(getSkey()));
			boolean isSuccess = false;
			UserDb ud = new UserDb();
			ud = ud.getUserDb(privilege.getUserName(getSkey()));
			ud.setRealName(getRealName());
			ud.setEmail(getEmail());
			ud.setPhone(getPhone());
			ud.setMobile(getMobile());
			ud.setQQ(getQQ());
			ud.setIDCard(getIDcard());
			if (!getPassword().equals("")) {
				// 检查旧密码是否正确
				if (!ud.getPwdRaw().equals(getOldPwd())) {
					json.put("res", "-1");
					json.put("msg", "验证失败，旧密码错误！");
					setResult(json.toString());
					return "SUCCESS";
				}
				String pwdMD5 = "";
				try {
					pwdMD5 = SecurityUtil.MD5(getPassword());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ud.setPwdMD5(pwdMD5);
				ud.setPwdRaw(getPassword());
			}
			ud.setName(getName());
			isSuccess = ud.save();
			if (isSuccess) {
				json.put("res", "0");
				json.put("msg", "修改成功");
			} else {
				json.put("res", "-1");
				json.put("msg", "修改失败");
			}
		} catch (JSONException e) {
			Logger.getLogger(MyinfoEditAction.class).error(e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}

	public void setOldPwd(String oldPwd) {
		this.oldPwd = oldPwd;
	}

	public String getOldPwd() {
		return oldPwd;
	}
}
