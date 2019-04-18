package com.redmoon.oa.android;

import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.Config;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupMgr;
import com.redmoon.oa.pvg.OnlineUserDb;
import com.redmoon.oa.security.ServerIPPriv;
import com.redmoon.oa.usermobile.UserMobileMgr;

import org.apache.struts2.ServletActionContext;

public class LoginAction extends ActionSupport {
	private String result = "";
	private String username = ""; // 登陆的用户名
	private String password = "";
	private String unitcode = ""; // shortname

	private String relogin = "";

	private String client;

	private String deviceId;

	private String ip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getResult() {
		return result;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUnitcode() {
		return unitcode;
	}

	public void setUnitcode(String unitcode) {
		this.unitcode = unitcode;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setRelogin(String relogin) {
		this.relogin = relogin;
	}

	public String getRelogin() {
		return relogin;
	}

	public String execute() {
		JSONObject json = new JSONObject();
		UserDb ud = new UserDb();
		DeptDb dd = new DeptDb();

		String unitCode = "";
		boolean re = false;

		/*
		 * if (getUnitcode().equals("")||getUnitcode()==null) { //
		 * shortName为空表示登录的是root级用户 dd =
		 * dd.getDeptDbByShortName(DeptDb.ROOTCODE); }else{ dd =
		 * dd.getDeptDbByShortName(getUnitcode()); }
		 * 
		 * if (dd == null) { try { json.put("res","-1");
		 * json.put("msg","单位不存在"); } catch (JSONException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 * setResult(json.toString()); return "SUCCESS"; }
		 * 
		 * unitCode = dd.getUnitOfDept(dd).getCode();
		 */

		// ud = ud.getUserDbByUnitAndNick(unitCode, getUsername());
		ud = ud.getUserDb(getUsername().trim());
		boolean isExist = ud.isExist(getUsername());
		try {
			if (!isExist) {
				json.put("res", "-1");
				json.put("msg", "用戶不存在");
				setResult(json.toString());
				return "SUCCESS";
			}

			if (!ud.isValid()) {
				json.put("res", "-1");
				json.put("msg", "非法用戶");
				setResult(json.toString());
				return "SUCCESS";
			}
			
			// 许可证校验
			HttpServletRequest request = ServletActionContext.getRequest();  			
	        try {
				License.getInstance().validate(request);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				json.put("res", "-1");
				json.put("msg", e.getMessage());
				setResult(json.toString());
				return "SUCCESS";				
			}

			// 手机端绑定硬件标识
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			boolean is_bind_mobile = cfg.getBooleanProperty("is_bind_mobile");
			if(is_bind_mobile){
				if(deviceId !=null && !deviceId.trim().equals("")){
					UserSetupMgr userSetupMgr = new UserSetupMgr();
					String userName = ud.getName();
					boolean isAllowBindMobile = userSetupMgr.isBindMobile(userName);
					UserMobileMgr userMobileMgr = new UserMobileMgr();
					if (isAllowBindMobile) {//是否允许绑定硬件标识
						boolean isBindMobile = userMobileMgr.isBindMobile(userName);//是否绑定硬件标识
						if (!isBindMobile) {//未绑定 ，插入绑定
							boolean result = userMobileMgr.create(userName, deviceId,
									client, 1);
							if (!result) {
								json.put("res", "-1");
								json.put("msg", "绑定手机失败！");
								setResult(json.toString());
								return "SUCCESS";
							}
						}else{
							boolean isExistBindRecord = userMobileMgr.isExistBindRecord(userName, deviceId);
							if(!isExistBindRecord){
								json.put("res", "-1");
								json.put("msg", "登陆手机与绑定手机不一致！");
								setResult(json.toString());
								return "SUCCESS";
							}
						}
					}
				}
			}
		
			/*
			 * if (ud.getPwdRaw() == null || ud.getPwdRaw().equals("")) {
			 * com.redmoon.forum.sms.RandomNumManager randNumMgr = new
			 * com.redmoon.forum.sms.RandomNumManager(); String mobilePwd =
			 * randNumMgr.getRandNumStr(5); IMsgUtil imu =
			 * SMSFactory.getMsgUtil(); try { // 检查短信发送状态 imu.checkSmsStatus();
			 * imu.send(getUsername(), mobilePwd, "system"); //将该随即动态密码存入数据库
			 * String pwdMD5 = ""; try { pwdMD5 = SecurityUtil.MD5(mobilePwd); }
			 * catch (Exception e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } ud.setPwdMD5(pwdMD5);
			 * ud.setPwdRaw(mobilePwd); ud.save(); } catch (ErrMsgException e) {
			 * // TODO Auto-generated catch block e.printStackTrace(); } try {
			 * json.put("res", "0"); json.put("msg", "请等待手机密码短信");
			 * json.put("mobilePwd", mobilePwd); setResult(json.toString());
			 * return "SUCCESS"; } catch (JSONException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } }
			 */

			if (ud != null) {
				re = ud.Auth(ud.getName(), getPassword());
			}
			
			boolean canUserLoginIP = true;
			String serverName = request.getServerName();
			// System.out.println(getClass() + " serverName=" + serverName);
			ServerIPPriv sip = new ServerIPPriv(serverName);
			if (!sip.canUserLogin(ud.getName())) {	
				re = false;
				canUserLoginIP = false;
			}

			if (re) {
				String appName = cfg.get("enterprise");
				json.put("res", "0");
				json.put("msg", "操作成功");
				json.put("id", String.valueOf(ud.getId()));
				json.put("appName", appName);
				json.put("username", ud.getName());
				json.put("realname", ud.getRealName());
				Date now = new Date();
				CloudConfig cloudConfig = CloudConfig.getInstance();
				String skey = ud.getName() + "|" + "OA" + "|" + now.getTime();
				LogUtil.getLog(LoginAction.class).info("skey=" + skey);
				// String skeyBase64 = new String(Base64.encodeBase64(new
				// String(
				// skey.getBytes()).getBytes()));
				// json.put("skey", skeyBase64);
				// com.redmoon.oa.sso.Config ssocfg = new
				// com.redmoon.oa.sso.Config();
				// String key = ssocfg.get("key");
				// if(key == null || key.trim().equals("")){
				// key = this.KEY;
				// }
				String key = cloudConfig.getProperty("key");
				String des = ThreeDesUtil.encrypt2hex(key, skey);
				LogUtil.getLog(LoginAction.class).error("des=" + des);
				json.put("skey", des);
				json.put("roleid", String.valueOf(ud.getId()));
				HttpSession session = request.getSession();
				session.setAttribute(Constant.OA_NAME, ud.getName());
				session.setAttribute(Constant.OA_UNITCODE, ud.getUnitCode());
				com.redmoon.oa.pvg.Privilege p = new com.redmoon.oa.pvg.Privilege();
				// 再检查是否有发起流程的权限
				if (p.isUserPrivValid(request, "flow.init")) {
					json.put("isFlow", "true");
				} else {
					json.put("isFlow", "false");
				}
				if (p.isUserPrivValid(request, "sms")) {
					json.put("isSms", "true");
				} else {
					json.put("isSms", "false");
				}
			/*	boolean isSales = false;
				if (p.isUserPrivValid(request, "sales.user")
						|| p.isUserPrivValid(request, "sales")) {
					isSales =true;
				}*/
				//lzm 修改  ios json解析isSales是string类型 android是boolean类型 临时解决方案
				if(client!=null && !client.trim().equals("")){
					if(client.equals( OnlineUserDb.CLIENT_ANDROID)){
						json.put("isSales",false);
					}else{
						json.put("isSales","false");
					}
				}else{
					json.put("isSales","false");
				}

				// 判断是否显示定位签到模块
				boolean isLocation = cloudConfig
						.getBooleanProperty("is_location");
				json.put("isLocation", isLocation);
				String strIsCaptureFlow = CloudConfig.getInstance()
						.getProperty("isCaptureFlow");
				json.put("isCaptureFlow", strIsCaptureFlow);

				if (client == null) {
					client = OnlineUserDb.CLIENT_IOS;
				}

				OnlineUserDb oud = new OnlineUserDb();
				oud = oud.getOnlineUserDb(ud.getName());

				if (oud.isLoaded()) {
					// 如果不是pc端则刷新
					if (!oud.getClient().equals(OnlineUserDb.CLIENT_PC)) {
						oud.setStayTime(new java.util.Date());
						oud.save();
					}
				} else {
					oud.setName(ud.getName());
					oud.setIp(ip);
					oud.setGuest(false);
					oud.setStayTime(new java.util.Date());
					// oud.setSessionId(sessionId);
					oud.setClient(client);
					oud.create();
				}

				if ("true".equals(relogin)) {

					// String sessionId = req.getSession().getId();

					/*
					 * Iterator ir =
					 * SessionListener.getSessionMaps().keySet().iterator();
					 * while (ir.hasNext()) { HttpSession session =
					 * (HttpSession)
					 * SessionListener.getSessionMaps().get(ir.next());
					 * System.out.println(getClass() + " sessiondId=" +
					 * session.getId()); }
					 */
					// 如果该用户已处于在线记录中
				} else {
					// 判断是否已存在

					LogDb log = new LogDb();
					log.setUserName(ud.getName());
					log.setType(LogDb.TYPE_LOGIN);
					log.setDevice(LogDb.DEVICE_MOBILE);
					log.setAction("登录系统");
					log.setIp(ip);
					log.setUnitCode(ud.getUnitCode());
					log.setRemark("");
					log.create();
				}
			} else {
				json.put("res", "-1");
				if (!canUserLoginIP) {
					json.put("msg", "禁止登录！");					
				}
				else {
					json.put("msg", "用户名或密码错误！");
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getClient() {
		return client;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceId() {
		return deviceId;
	}
}
