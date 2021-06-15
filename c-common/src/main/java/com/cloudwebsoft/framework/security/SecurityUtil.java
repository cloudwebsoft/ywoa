package com.cloudwebsoft.framework.security;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import com.redmoon.kit.util.FileUpload;

public class SecurityUtil {
	
	public static String getDomain(String curl){
		URL url = null;
		String q = "";
		try {
			url = new URL(curl);
			q = url.getHost();
		} catch (MalformedURLException e) {			
			// e.printStackTrace();
		}
		return q;
	}
	
	/**
	 * 判断重定向的网址是否为本站链接，Anti Phishing
	 * @param request
	 * @param redirectUrl
	 * @throws ErrMsgException
	 */
	public static boolean isUrlValid(HttpServletRequest request, String redirectUrl) throws ErrMsgException {
		String domain = getDomain(redirectUrl);
		if ("".equals(domain)) {
			return true;
		}
		return request.getServerName().equals(domain);
	}
	
	/**
	 * XSS及SQL注入过滤
	 * @param request
	 * @param path
	 * @throws ProtectXSSException
	 * @throws ProtectSQLInjectException
	 */
	public static void filter(HttpServletRequest request, String path) throws ProtectXSSException, ProtectSQLInjectException {
		if (isFilter(path)) {
			Enumeration paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String paramName = (String) paramNames.nextElement();
				String[] paramValues = request.getParameterValues(paramName);
				for (int i = 0; i < paramValues.length; i++) {
					String paramValue;
					// 如果值为中文，如：dir_name=%E9%A1%B9%E7%9B%AE，不转换就会报sql注入
					if (paramValues.length==1) {
						paramValue = ParamUtil.get(request, paramName);
					}
					else {
						paramValue = paramValues[i];
					}

					// XSS
					String paramValue2 = com.cloudwebsoft.framework.security.AntiXSS.antiXSS(paramValue);
					if (!paramValue.equals(paramValue2)) {
						throw new ProtectXSSException(paramName, paramValue);
					}

					// 防SQL注入
					if (!cn.js.fan.db.SQLFilter.isValidSqlParam(paramValue)) {
						throw new ProtectSQLInjectException(paramName, paramValue);
					}
				}
			}
		}
	}

    public static void filter(String paramName, String paramValue) throws ProtectXSSException, ProtectSQLInjectException {
	    filter(paramName, paramValue, true);
    }

    public static void filter(String paramName, String paramValue, boolean isGet) throws ProtectXSSException, ProtectSQLInjectException {
		// XSS
		String paramValue2 = com.cloudwebsoft.framework.security.AntiXSS.antiXSS(paramValue, isGet);
		if (!paramValue.equals(paramValue2)) {
			throw new ProtectXSSException(paramName, paramValue);
		}

		// 防SQL注入
		if (!cn.js.fan.db.SQLFilter.isValidSqlParam(paramValue)) {
			throw new ProtectSQLInjectException(paramName, paramValue);
		}
	}

	public static boolean isFilter(String url) {
		ProtectConfig pc = new ProtectConfig();
		// 如果被排除，则不再检测
		Vector<ProtectUnit> vun = pc.getAllUnProtectUnit();
		Iterator<ProtectUnit> irun = vun.iterator();
		while (irun.hasNext()) {
			ProtectUnit pu = irun.next();
			if (pu.getType() == ProtectUnit.TYPE_INCLUDE) {
				if (url.indexOf(pu.getRule())!=-1) {
					return false;
				}
			}
			else {
				// 正则
				Pattern pattern = Pattern.compile(pu.getRule(),
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(url);
				if (matcher.find()) {
					return false;
				}
			}
		}

		Vector<ProtectUnit> v = pc.getAllProtectUnit();
		Iterator<ProtectUnit> ir = v.iterator();
		while (ir.hasNext()) {
			ProtectUnit pu = ir.next();
			// 包含
			if (pu.getType()==ProtectUnit.TYPE_INCLUDE) {
				if (url.indexOf(pu.getRule())!=-1) {
					return true;
				}
			}
			else {
				// 正则
				Pattern pattern = Pattern.compile(pu.getRule(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(url);
				if (matcher.find()) {
					return true;
				}
			}
		}
		return false;
	}

	public static void main(String[] args) throws Exception {
	    SecurityUtil su = new SecurityUtil();
	    System.out.println(SecurityUtil.getDomain("oa.jsp"));
	    System.out.println(SecurityUtil.getDomain("http://192.168/oa.jsp"));
	}

}
