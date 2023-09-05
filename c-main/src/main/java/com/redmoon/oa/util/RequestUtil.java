package com.redmoon.oa.util;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.base.IFormDAO;

/**
 * @Description:
 * @author:
 * @Date: 2018-1-29下午06:16:34
 */
public class RequestUtil {
	public static final String FORMDAO = "FORMDAO";
	public static final String REPORT_FOR_ARCHIVE = "reportForArchive";
	public static final String NAME_COND = "name_cond";

	public static void setAttribute(HttpServletRequest request, String attrName, Object obj) {
		request.setAttribute(attrName, obj);
	}

	public static void setFormDAO(HttpServletRequest request, IFormDAO ifdao) {
		request.setAttribute(FORMDAO, ifdao);
	}

	public static IFormDAO getFormDAO(HttpServletRequest request) {
		return (IFormDAO)request.getAttribute(FORMDAO);
	}

	public static Object getAttribute(HttpServletRequest request, String attName) {
		return request.getAttribute(attName);
	}

	public static void removeAttribute(HttpServletRequest request, String attName) {
		request.removeAttribute(attName);
	}
}
