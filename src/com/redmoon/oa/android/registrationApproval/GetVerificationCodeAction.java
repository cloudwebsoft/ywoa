package com.redmoon.oa.android.registrationApproval;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.verificationCode.VerificationCodeMgr;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.verificationCode.HttpClientVerificationCode;


/**
 * @author lichao
 * 用户注册验证码获取接口
 */
public class GetVerificationCodeAction {
	private static int GET_VERIFI_SUCCESS = 0;       //获取验证码成功
	private static int MESSAGE_PASS_50 = 1;          //免费短消息超过50条
	private static int NOT_HAVE_ENTERPRISE_NO = 2;   //没有企业号，提示注册
	
	private static int TYPE_LOCAL = 0 ;              //短信发送方，本地
	private static int TYPE_YIMIHOME = 1 ;           //短信发送方，一米之家
	
	private String enterpriseNo = "";
	private String name = "";
	private String result = "";
	private String verificationCode = "";
	
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

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public String execute() {
		boolean flag = true;
		JSONObject jSend = new JSONObject();
		JSONObject jReturn = new JSONObject(); 
		JSONObject jResult = new JSONObject();
		
		enterpriseNo = License.getInstance().getEnterpriseNum();
		try {
			if ("".equals(enterpriseNo) || "yimi".equals(enterpriseNo)) {
				jReturn.put("res", 0);
				jReturn.put("msg", "请先注册企业号，才能使用短信验证功能。");
				jResult.put("verificationCode", "");
				jResult.put("returnCode", NOT_HAVE_ENTERPRISE_NO);
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		UserDb ud = new UserDb(name);
		
		try {
			if(ud==null || !ud.isLoaded()){
				jSend.put("name",name);
			}else{
				jSend.put("name",ud.getMobile());
			}
			jSend.put("enterpriseNo",enterpriseNo);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		try {
			// 检查本地oa是否配置了短信服务。
			IMsgUtil imu = SMSFactory.getMsgUtil();

			if (imu == null) {
				// 向yimihome服务器发送获取验证码请求
				Config cg = new Config();
				String yimihomeURL = cg.get("yimihome_url");

				HttpClientVerificationCode he = new HttpClientVerificationCode();
				jReturn = he.getVerificationCode(yimihomeURL + "/httpClientServer/httpclient_server_get_verification_code.jsp", jSend);
				int result = jReturn.getInt("result");
				verificationCode = jReturn.getString("verificationCode");
				jReturn.remove("verificationCode");
				
				if (result == GET_VERIFI_SUCCESS) {
					jReturn.remove("result");

					jReturn.put("res", 0);
					jReturn.put("msg", "获取验证码成功");
					jResult.put("verificationCode", verificationCode);
					jResult.put("returnCode", GET_VERIFI_SUCCESS);
					jResult.put("type", TYPE_YIMIHOME);
					jReturn.put("result", jResult);
				}else if (result == MESSAGE_PASS_50) {
					jReturn.remove("result");

					jReturn.put("res", 0);
					jReturn.put("msg", "免费短消息超过50条");
					jResult.put("verificationCode", verificationCode);
					jResult.put("returnCode", MESSAGE_PASS_50);					
					jResult.put("type", TYPE_YIMIHOME);
					jReturn.put("result", jResult);
				}else{
					flag = false;
				}
			} else {
				VerificationCodeMgr vr = new VerificationCodeMgr();
				boolean res = true;
				
				String mobile="";
				if(ud==null || !ud.isLoaded()){
					mobile = name;
				}else{
					mobile = ud.getMobile();
				}
				
				res = vr.isNew(mobile);// 判断是否为新手机号

				if (res) {
					jReturn = vr.create(mobile, enterpriseNo);// 新建
				} else {
					jReturn = vr.setNewVerificationCode(mobile, enterpriseNo);// 更新
				}

				int result = jReturn.getInt("result");
				verificationCode = jReturn.getString("verificationCode");
				jReturn.remove("verificationCode");
		
				if (result == GET_VERIFI_SUCCESS) {
					jReturn.remove("result");

					jReturn.put("res", 0);
					jReturn.put("msg", "获取验证码成功");
					jResult.put("verificationCode", verificationCode);
					jResult.put("returnCode", GET_VERIFI_SUCCESS);
					jResult.put("type", TYPE_LOCAL);
					jReturn.put("result", jResult);
				}else{
					flag = false;
				}
			}
		} catch (JSONException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (ClassNotFoundException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (ErrMsgException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (ResKeyException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (!flag) {
				try {
					jReturn.put("res", -1);
					jReturn.put("msg", "获取验证码失败");
					jResult.put("verificationCode", "");
					jReturn.put("result", jResult);
				} catch (JSONException e) {
					e.printStackTrace();
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
		}
		
		setResult(jReturn.toString());
		return "SUCCESS";
	}	
}
