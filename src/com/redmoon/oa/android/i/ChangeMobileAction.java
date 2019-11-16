package com.redmoon.oa.android.i;

import java.io.IOException;
import org.json.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.verificationCode.VerificationCodeMgr;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.verificationCode.HttpClientVerificationCode;


 /**
 * @Description: 更换手机号码接口
 * @author: lichao
 * @Date: 2015-7-15上午10:41:40
 */
public class ChangeMobileAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期

	private static int RETURNCODE_SUCCESS = 0;               //手机号更换成功
	private static int RETURNCODE_REGISTED_MOBILE = 2;       //手机号已经注册
	private static int RETURNCODE_EXPIRE_VERIFI = 3;	     //验证码已过期
	private static int RETURNCODE_ERROR_VERIFI = 4;		     //验证码错误
	
	private static int TYPE_LOCAL = 0 ;                      //短信发送方，本地
	private static int TYPE_YIMIHOME = 1 ;                   //短信发送方
	
	private String skey = "";
	private String mobile = "";
	private String verificationCode = "";
	private int type;
	private String result = "";
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
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
		UserDb ud = new UserDb(privilege.getUserName(skey));
		
		//先验证验证码是否过期 ，根据type区分是本地发送的验证码 ，还是云端
		try {
			int result = 0; //验证码有效

			if (type == TYPE_YIMIHOME) {
				Config cg = new Config();
				String yimihomeURL = cg.get("yimihome_url");
				
				HttpClientVerificationCode he = new HttpClientVerificationCode();

				jSend.put("name",getMobile());
				jSend.put("verificationCode",getVerificationCode());
				
				jReturn = he.checkVerificationCode(yimihomeURL + "/httpClientServer/httpclient_server_check_verification_code.jsp",	jSend);

				result = jReturn.getInt("result");
				jReturn.remove("remark");
			} else if (type == TYPE_LOCAL) {
				VerificationCodeMgr vr = new VerificationCodeMgr();
				result = vr.checkVerificationCodeValid(getMobile(),	getVerificationCode());
			}

			if (result == RETURNCODE_EXPIRE_VERIFI) {
				jReturn.put("res", 0);
				jResult.put("returnCode", RETURNCODE_EXPIRE_VERIFI);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			} else if (result == RETURNCODE_ERROR_VERIFI) {
				jReturn.put("res", 0);
				jResult.put("returnCode", RETURNCODE_ERROR_VERIFI);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		
		try {
			// 检验新手机号否注册过
			String sql = "select mobile from users where mobile=?";
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = null;

			ri = jt.executeQuery(sql, new Object[] { mobile });
			if (ri.hasNext()) {
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_REGISTED_MOBILE);
				jReturn.put("result", jResult);

				setResult(jReturn.toString());
				return "SUCCESS";
			}
			
			ud.setMobile(mobile);
			ud.save();
			
			jReturn.put("res", RES_SUCCESS);
			jResult.put("returnCode", RETURNCODE_SUCCESS);
			jReturn.put("result", jResult);
		} catch (JSONException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}  catch (Exception e) {
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
