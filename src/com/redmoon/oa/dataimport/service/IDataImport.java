package com.redmoon.oa.dataimport.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.dataimport.bean.DataImportBean;

/**
 * @Description: 接口
 * @author: 古月圣
 * @Date: 2015-11-25下午04:06:05
 */
public interface IDataImport {
	public abstract String getTemplatePath(HttpServletRequest request);

	public abstract String importExcel(ServletContext application,
			HttpServletRequest request);

	public abstract DataImportBean getDataImportBean();

	public abstract void setDataImportBean(DataImportBean dataImportBean);

	public abstract String create(ServletContext application,
			HttpServletRequest request);

	public abstract String getErrorMessages();
	
	public abstract String getRangeErrMsg(String field, String value);
}
