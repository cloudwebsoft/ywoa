package com.redmoon.oa.security;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

import com.redmoon.oa.pvg.*;

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
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid") + "：" + paramName + "=" + paramValue);
		}		
	}
	
	public static void antiXSS(HttpServletRequest request, Privilege privilege, String paramName, String paramValue, String sourceUrl) throws ErrMsgException {
		if (paramValue==null)
			return;
		
		String paramValue2 = com.cloudwebsoft.framework.security.AntiXSS.antiXSS(paramValue);
		if (!paramValue.equals(paramValue2)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF " + sourceUrl + " " + paramName + "=" + paramValue);
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid") + "：" + paramName + "=" + paramValue);
		}	
	}
	
	/**
	 * 對url進行合法性验证，判断url是否为本站鍊接
	 * @param request
	 * @param privilege
	 * @param url
	 * @throws ErrMsgException
	 */
	public static void validateUrl(HttpServletRequest request, Privilege privilege, String urlName, String urlValue, String sourceUrl) throws ErrMsgException {
		if (!com.cloudwebsoft.framework.security.SecurityUtil.isUrlValid(request, urlValue)) {
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF Phishing " + sourceUrl + " " + urlName + "=" + urlValue);
			throw new ErrMsgException(SkinUtil.LoadString(request, "param_invalid"));
		}		
	}

}
