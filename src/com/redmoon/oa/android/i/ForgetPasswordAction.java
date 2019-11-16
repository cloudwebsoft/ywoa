package com.redmoon.oa.android.i;

import java.io.IOException;
import org.json.*;

import cn.js.fan.security.SecurityUtil;
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
 * @Description: 忘记密码接口
 * @author: lichao
 * @Date: 2015-7-15上午10:41:40
 */
public class ForgetPasswordAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	
	private static int RETURNCODE_SUCCESS = 0;               //新密码下发成功
	private static int RETURNCODE_USER_NOT_EXIST = 1;        //帐号不存在
	private static int RETURNCODE_NOT_VALID_USER = 2;        //非法用户
	private static int RETURNCODE_ERORR_SEND_MSG = 3;        //短信发送失败
	private static int RETURNCODE_SUCCESS_SEND_MSG = 4;      //短信发送成功
	private static int RETURNCODE_EXPIRE_VERIFI = 5;	     //验证码已过期
	private static int RETURNCODE_ERROR_VERIFI = 6;		     //验证码错误
	private static int RETURNCODE_NOT_HAVE_MOBILE = 7;		 //用户没有手机，请联系管理员
	private static int MESSAGE_PASS_50 = 8;                  //免费短消息超过50条
	private static int NOT_HAVE_ENTERPRISE_NO = 9;           //没有企业号，提示注册
	
	private static int TYPE_LOCAL = 0 ;                      //短信发送方，本地
	private static int TYPE_YIMIHOME = 1 ;                   //短信发送方，云端
	
	private static String INIT_PASSWORD = "123";		     //初始密码
	
	private String enterpriseNo = "";
	private String name = "";
	private String verificationCode = "";
	private int type;
	private String result = "";
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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
			if (ud == null || !ud.isLoaded()) {
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_USER_NOT_EXIST);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			}
			
			if (!ud.isValid()) {
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_NOT_VALID_USER);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			}
			
	    	if("".equals(ud.getMobile()) || null==ud.getMobile()){
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_NOT_HAVE_MOBILE);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
	    	}
	    	
			// 先验证验证码是否过期 ，根据type区分是本地发送的验证码 ，还是云端
			Config cg = new Config();
			String yimihomeURL = cg.get("yimihome_url");

			int result = 0; // 验证码有效

			if (type == TYPE_YIMIHOME) {
				HttpClientVerificationCode he = new HttpClientVerificationCode();

				jSend.put("name", ud.getMobile());
				jSend.put("verificationCode", getVerificationCode());

				jReturn = he.checkVerificationCode(	yimihomeURL	+ "/httpClientServer/httpclient_server_check_verification_code.jsp", jSend);

				result = jReturn.getInt("result");
				jReturn.remove("remark");
			} else if (type == TYPE_LOCAL) {
				VerificationCodeMgr vr = new VerificationCodeMgr();
				result = vr.checkVerificationCodeValid(ud.getMobile(), getVerificationCode());
			}

			if (result == RETURNCODE_EXPIRE_VERIFI) {
				jReturn.put("res", 0);
				jReturn.put("msg", "验证码已过期");
				jResult.put("returnCode", RETURNCODE_EXPIRE_VERIFI);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			} else if (result == RETURNCODE_ERROR_VERIFI) {
				jReturn.put("res", 0);
				jReturn.put("msg", "验证码错误");
				jResult.put("returnCode", RETURNCODE_ERROR_VERIFI);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			}

			// 检查本地oa是否配置了短信服务。
			IMsgUtil imu = SMSFactory.getMsgUtil();
			
			result = 1;
			boolean re = true;
			if (imu == null) {
				// 向yimihome服务器发送获取验证码请求
				HttpClientGetInitializtionPassword hd = new HttpClientGetInitializtionPassword();
				
				jSend.put("mobile", ud.getMobile());
				jSend.put("enterpriseNo", enterpriseNo);
				jReturn = hd.getInitializtionPassword(yimihomeURL + "/httpClientServer/httpclient_server_send_password.jsp", jSend);
				result = jReturn.getInt("result");
			} else {
				re  = SMSFactory.getMsgUtil().send(ud.getMobile(),"【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】初始密码：" + INIT_PASSWORD + "，请您登录后及时修改初始密码。","");
				
				if(re){
					result = 0;
					System.out.println("-----手机"+ud.getMobile() +",【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】初始密码已经发送-----");
				}else{
					result = 1;
				}
			}
			
			//result=-1 手机号为空 ；result=0 短信成功发送 ；result=1 短信发送失败   ；result=2 免费短消息超过50条
			if (result == 1) {
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_ERORR_SEND_MSG);
				jReturn.put("result", jResult);
			} else if(result == 0){   //发送短信成功后，更新原始密码
				String pwdMD5 = SecurityUtil.MD5(INIT_PASSWORD);
				
				ud.setPwdMD5(pwdMD5);
				ud.setPwdRaw(INIT_PASSWORD);
				re = ud.save();
				
				if(re){
					jReturn.put("res", RES_SUCCESS);
					jResult.put("returnCode", RETURNCODE_SUCCESS);
					jReturn.put("result", jResult);
				}	
			}else if (result == 2) {
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", MESSAGE_PASS_50);
				jReturn.put("result", jResult);
			}
		} catch (JSONException e) {
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
