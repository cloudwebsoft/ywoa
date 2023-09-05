package com.redmoon.oa.verificationCode;


import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.util.HttpClientConnectUtil;

/**
 * @Description: 手机端获取验证码及验证验证码正确性
 * @author: lichao
 * @Date: 2015-8-28下午02:00:40
 */
public class HttpClientVerificationCode {
	// 手机端注册用户时，获取yimihome上生成的验证码。
	public JSONObject getVerificationCode(String URL, JSONObject jobject) throws JSONException, IOException {
		JSONObject jReturn = null;

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, jobject.toString());

			jReturn = new JSONObject(sb.toString());
		} catch (JSONException | IOException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		return jReturn;
	}

	// 手机端注册用户时，输入收到的验证码后，提交时验证。
	public JSONObject checkVerificationCode(String URL, JSONObject jobject) throws JSONException, IOException {
		JSONObject jReturn = null;

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, jobject.toString());

			jReturn = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			LogUtil.getLog(getClass()).error(e);
		} catch (IOException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			LogUtil.getLog(getClass()).error(e);
		}

		return jReturn;
	}
	
}
