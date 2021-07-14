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

	public static void setFormDAO(HttpServletRequest request, IFormDAO ifdao) {
		request.setAttribute(FORMDAO, ifdao);
	}
	
	public static IFormDAO getFormDAO(HttpServletRequest request) {
		return (IFormDAO)request.getAttribute(FORMDAO);
	}
}
