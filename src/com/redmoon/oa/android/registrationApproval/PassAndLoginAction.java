package com.redmoon.oa.android.registrationApproval;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.LogDb;
import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.android.CloudConfig;
import com.redmoon.oa.android.tools.Des3;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupMgr;
import com.redmoon.oa.security.ServerIPPriv;
import com.redmoon.oa.usermobile.UserMobileMgr;


/**
 * @author lichao
 * 审核通过后登录接口
 */
public class PassAndLoginAction {
	private static int RETURNCODE_LOGIN_SUCCESS = 0;       //登录成功
	private static int RETURNCODE_ERROR_PASSWORD = 1;      //用户名密码错误
	private static int RETURNCODE_INCONSISTENT_BIND = 2;   //与绑定手机不一致
	private static int RETURNCODE_USER_NOT_EXIST  = 3;     //用户不存在
	private static int RETURNCODE_NOT_VALID_USER = 4;      //非法用户
	private static int RETURNCODE_NOT_EXAMINE = 5;         //未审核用户
	private static int RETURNCODE_NOT_PASS_EXAMINE = 6;    //未通过审核用户
	
	private String name = "";
	private String password = "";
	private String deviceId = "";
	private String client = "";
	private String enterpriseNum = "";
	private int hasMobile;
	private String result = "";
	
	
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		boolean re = false;
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		HttpServletRequest request = ServletActionContext.getRequest();

		String decrypPassWord = "";
		try {
			decrypPassWord = Des3.decode(password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String userName = getName();
		UserDb ud = new UserDb();
		re = ud.Auth(userName, decrypPassWord);
		if(!re){
            // 检查是否使用了工号登录
            AccountDb ad = new AccountDb();
            ad = ad.getAccountDb(getName());
            if (ad.isLoaded()) {
                userName = ad.getUserName();
                String pwdMD5 = "";
				try {
					pwdMD5 = SecurityUtil.MD5(decrypPassWord);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
                try {
					if (!"".equals(userName) && pvg.Authenticate(userName, pwdMD5)) {
						re = true;
					}
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            if (!re){ 
				try {
					jReturn.put("res", 0);
					jReturn.put("msg", "用户名或密码错误");
					jResult.put("returnCode", RETURNCODE_ERROR_PASSWORD);
					jReturn.put("result", jResult);

					LogDb log = new LogDb();
					log.setUserName(getName());
					log.setType(LogDb.TYPE_LOGIN);
					log.setDevice(LogDb.DEVICE_MOBILE);
					log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
					log.setIp(StrUtil.getIp(request));
					log.setUnitCode(ud.getUnitCode());
					log.setRemark("用户名或密码错误");

					log.create();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				setResult(jReturn.toString());
				return "SUCCESS";
            }
		}

		try {
			ud = new UserDb(userName);

			if (ud==null || !ud.isLoaded()) {
				jReturn.put("res", 0);
				jReturn.put("msg", "用戶不存在");
				jResult.put("returnCode", RETURNCODE_USER_NOT_EXIST);
				jReturn.put("result", jResult);
				setResult(jReturn.toString());

				LogDb log = new LogDb();
				log.setUserName(getName());
				log.setType(LogDb.TYPE_LOGIN);
				log.setDevice(LogDb.DEVICE_MOBILE);
				log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
				log.setIp(StrUtil.getIp(request));
				log.setRemark("用戶不存在");
				log.create();

				return "SUCCESS";
			}
			if (!ud.isValid()) {
				jReturn.put("res", 0);
				jReturn.put("msg", "非法用戶");
				jResult.put("returnCode", RETURNCODE_NOT_VALID_USER);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());

				LogDb log = new LogDb();
				log.setUserName(getName());
				log.setType(LogDb.TYPE_LOGIN);
				log.setDevice(LogDb.DEVICE_MOBILE);
				log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
				log.setIp(StrUtil.getIp(request));
				log.setUnitCode(ud.getUnitCode());
				log.setRemark("非法用户");
				log.create();

				return "SUCCESS";
			}
			if(ud.getIsPass()==0){
				jReturn.put("res", 0);
				jReturn.put("msg", "未审核用户");
				jResult.put("returnCode", RETURNCODE_NOT_EXAMINE);
				jReturn.put("result", jResult);
				setResult(jReturn.toString());

				LogDb log = new LogDb();
				log.setUserName(getName());
				log.setType(LogDb.TYPE_LOGIN);
				log.setDevice(LogDb.DEVICE_MOBILE);
				log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
				log.setIp(StrUtil.getIp(request));
				log.setUnitCode(ud.getUnitCode());
				log.setRemark("未审核用户");
				log.create();
				return "SUCCESS";
			}
			if(ud.getIsPass()==2){
				jReturn.put("res", 0);
				jReturn.put("msg", "未通过审核用户");
				jResult.put("returnCode", RETURNCODE_NOT_PASS_EXAMINE);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());

				LogDb log = new LogDb();
				log.setUserName(getName());
				log.setType(LogDb.TYPE_LOGIN);
				log.setDevice(LogDb.DEVICE_MOBILE);
				log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
				log.setIp(StrUtil.getIp(request));
				log.setUnitCode(ud.getUnitCode());
				log.setRemark("未通过审核用户");
				log.create();
				return "SUCCESS";
			}
			
			String serverName = request.getServerName();
			// System.out.println(getClass() + " serverName=" + serverName);
			ServerIPPriv sip = new ServerIPPriv(serverName);
			if (!sip.canUserLogin(ud.getName())) {	
				jReturn.put("res", 0);
				jReturn.put("msg", "禁止登录！");					
				jResult.put("returnCode", RETURNCODE_NOT_VALID_USER);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());

				LogDb log = new LogDb();
				log.setUserName(getName());
				log.setType(LogDb.TYPE_LOGIN);
				log.setDevice(LogDb.DEVICE_MOBILE);
				log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
				log.setIp(StrUtil.getIp(request));
				log.setUnitCode(ud.getUnitCode());
				log.setRemark("禁止登录");
				log.create();

				return "SUCCESS";
			}			
			
			// 许可证校验
	        try {
				License.getInstance().validate(request);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				jReturn.put("res", "-1");
				jReturn.put("msg", e.getMessage());
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);				
				setResult(jReturn.toString());
				return "SUCCESS";				
			}			
			
			// 手机端绑定硬件标识
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			boolean is_bind_mobile = cfg.getBooleanProperty("is_bind_mobile");
			if(is_bind_mobile){
				if(getDeviceId() !=null && !getDeviceId().trim().equals("")){
					UserSetupMgr userSetupMgr = new UserSetupMgr();
					boolean isAllowBindMobile = userSetupMgr.isBindMobileModify(userName);
					UserMobileMgr userMobileMgr = new UserMobileMgr();
					if (isAllowBindMobile) {//是否允许绑定硬件标识
						boolean isBindMobile = userMobileMgr.isBindMobileModify(userName);//是否绑定硬件标识
						if (!isBindMobile) {//未绑定 ，插入绑定
							boolean result = userMobileMgr.create(userName, deviceId,client, 1);
							if (!result) {
								jReturn.put("res", -1);
								jReturn.put("msg", "绑定手机失败！");
								jResult.put("returnCode", "");
								jReturn.put("result", jResult);
								
								setResult(jReturn.toString());
								return "SUCCESS";
							}
						}else{
							boolean isExistBindRecord = userMobileMgr.isExistBindRecord(userName, deviceId);
							if(!isExistBindRecord){
								jReturn.put("res", 0);
								jReturn.put("msg", "登陆手机与绑定手机不一致！");
								jResult.put("returnCode", RETURNCODE_INCONSISTENT_BIND);
								jReturn.put("result", jResult);
								
								setResult(jReturn.toString());
								return "SUCCESS";
							}
						}
					}
				}
			}

			// 记录登录日志
			LogDb log = new LogDb();
			log.setUserName(ud.getName());
			log.setType(LogDb.TYPE_LOGIN);
			log.setDevice(LogDb.DEVICE_MOBILE);
			log.setAction(com.redmoon.oa.LogUtil.get(request, "action_login"));
			log.setIp(StrUtil.getIp(request));
			log.setUnitCode(ud.getUnitCode());
			log.setRemark("");
			log.create();
			
			Date now = new Date();
			String skey = ud.getName() + "|" + "OA" + "|" + now.getTime();
			LogUtil.getLog(PassAndLoginAction.class).info("skey=" + skey);
			
			CloudConfig cloudConfig = CloudConfig.getInstance();
			String key = cloudConfig.getProperty("key");
			
			String des = ThreeDesUtil.encrypt2hex(key, skey);
			LogUtil.getLog(PassAndLoginAction.class).info("des=" + des);
			
	    	enterpriseNum = License.getInstance().getEnterpriseNum(); 
	    	hasMobile = ("".equals(ud.getMobile()) || null==ud.getMobile()) ? 0 : 1;
	    	
			jReturn.put("res", 0);
			jReturn.put("msg", "登录成功");
			jResult.put("returnCode", RETURNCODE_LOGIN_SUCCESS);
			jResult.put("skey", des);
			
			License lic = License.getInstance();
			String liscenseType = lic.getType();
			if (lic.isSrc()) {
				// 如果是开发版，则发至手机端TYPE_BIZ，即收费版，以免显示为免费版
				liscenseType = License.TYPE_BIZ;
			}
			// 3.0以后，免费版改为了标准版，所以传biz，以免显示为免费版
			double ver = StrUtil.toDouble(License.getInstance().getVersion(), 1.0);
			if (ver>2) {
				liscenseType = License.TYPE_BIZ;
			}
			jResult.put("liscenseType", liscenseType);
			jResult.put("enterpriseNum",enterpriseNum);
			jResult.put("hasMobile",hasMobile);
			jReturn.put("result", jResult);
			
			
			int photoMaxSize = cloudConfig.getIntProperty("photoMaxSize");
			jResult.put("photoMaxSize", photoMaxSize);
			int intPhotoQuality = cloudConfig.getIntProperty("photoQuality");
			jResult.put("photoQuality", intPhotoQuality);			
	}catch (JSONException e) {
		e.printStackTrace();
	}
	
	setResult(jReturn.toString());
	return "SUCCESS";
}
}