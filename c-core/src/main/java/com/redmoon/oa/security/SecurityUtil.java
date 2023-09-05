package com.redmoon.oa.security;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

import com.redmoon.oa.Config;
import com.redmoon.oa.pvg.*;

import java.util.Date;

public class SecurityUtil {
	
	/**
	 * 防sql注入
	 * @param request
	 * @param privilege
	 * @param paramName
	 * @param paramValue
	 * @param sourceUrl
	 * @throws ErrMsgException
	 */
	public static void antiSQLInject(HttpServletRequest request, Privilege privilege, String paramName, String paramValue, String sourceUrl) throws ErrMsgException {
		
		// 防SQL注入
		if (!cn.js.fan.db.SQLFilter.isValidSqlParam(paramValue)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ " + sourceUrl + " " + paramName + "=" + paramValue);
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid") + "：" + paramName + "=" + StrUtil.toHtml(paramValue));
		}		
	}
	
	public static void antiXSS(HttpServletRequest request, Privilege privilege, String paramName, String paramValue, String sourceUrl) throws ErrMsgException {
		if (paramValue==null) {
			return;
		}
		
		String paramValue2 = com.cloudwebsoft.framework.security.AntiXSS.antiXSS(paramValue);
		if (!paramValue.equals(paramValue2)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF " + sourceUrl + " " + paramName + "=" + paramValue);
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid") + "：" + paramName + "=" + StrUtil.toHtml(paramValue));
		}	
	}
	
	/**
	 * 對url進行合法性验证，判断url是否为本站鍊接
	 * @param request
	 * @param privilege
	 * @throws ErrMsgException
	 */
	public static void validateUrl(HttpServletRequest request, Privilege privilege, String urlName, String urlValue, String sourceUrl) throws ErrMsgException {
		if (!com.cloudwebsoft.framework.security.SecurityUtil.isUrlValid(request, urlValue)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF Phishing " + sourceUrl + " " + urlName + "=" + urlValue);
			throw new ErrMsgException(SkinUtil.LoadString(request, "param_invalid"));
		}
	}

	public static String makeVisitKey(int val) {
		return makeVisitKey(String.valueOf(val), false);
	}

	public static String makeVisitKey(long val) {
		return makeVisitKey(String.valueOf(val), false);
	}

	public static String makeVisitKey(String val) {
		return makeVisitKey(val, false);
	}

	/**
	 *
	 * @param val
	 * @param isTemp 是否为临时钥匙，如果是，则在校验时不检查时间戳
	 * @return
	 */
	public static String makeVisitKey(String val, boolean isTemp) {
		String desKey = Config.getInstance().getKey();
		return cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, val + "|" + System.currentTimeMillis() + "|" + isTemp);
	}

	public static int validateVisitKey(String visitKey, String id) {
		String desKey = Config.getInstance().getKey();
		visitKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, visitKey);
		String[] ary = StrUtil.split(visitKey, "\\|"); // 格式为：id|timestamp
		if (ary != null && ary.length == 3) {
			// 判断标识是否与加密信息中的一致
			if (!String.valueOf(id).equals(ary[0])) {
				return -1;
			}

			// 判断时间戳是否超时
			boolean isTemp = "true".equals(ary[2]);
			if (isTemp) {
				long t = StrUtil.toLong(ary[1], -1);
				long visitKeyExpire = Config.getInstance().getInt("visitKeyExpire") * 60 * 1000;
				if (System.currentTimeMillis() - t > visitKeyExpire) {
					return 0;
				}
			}
		}
		return 1;
	}

	public static String getValidateVisitKeyErrMsg(int validateResult) {
		if (validateResult == 0) {
			return "时间戳已过期，请重新打开页面";
		}
		else if (validateResult == -1) {
			return "数据标识非法";
		}
		else {
			return "访问钥匙非法";
		}
	}
}
