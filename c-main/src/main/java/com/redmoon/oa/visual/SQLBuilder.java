package com.redmoon.oa.visual;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.api.ISQLBuilderService;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SQLBuilder {
	/**
	 * 模糊查询，包含
	 */
	public static final String COND_TYPE_FUZZY = "0";
	
	/**
	 * 准确查询，等于
	 */
	public static final String COND_TYPE_NORMAL = "1";

	/**
	 * 一段范围，用于数值型的表单域
	 */
	public static final String COND_TYPE_SCOPE = "2";

	/**
	 * 下拉菜单，可以勾选多个
	 */
	public static final String COND_TYPE_MULTI = "3";

	/**
	 * 两个模块之间没有关联关系
	 */
	public static final String IS_NOT_RELATED = "isNotRelated";
	
	/**
	 * 不限，不含临时记录
	 */
	public static final int CWS_STATUS_NOT_LIMITED = 10000;
	/**
	 * 为空
	 */
	public static final String IS_EMPTY = "=空";
	/**
	 * 不为空
	 */
	public static final String IS_NOT_EMPTY = "<>空";

	/**
	 * 如果模块的过滤条件中是完整的
	 */
	public static final int TABLE_ALIAS_NUM_MIN = 100;

    public SQLBuilder() {
    }

    public static String[] getModuleListSqlAndUrlStr(HttpServletRequest request,
            FormDb fd, String op, String orderBy, String sort) throws ErrMsgException {
        return getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort, "", "");
    }

    /**
     * 获取关联模块列表sql语句
     * @param request HttpServletRequest
     * @param fd FormDb
     * @param op String
     * @param orderBy String
     * @param sort String
     * @param userName String
     * @param fieldUserName String
     * @return String[]
     */
    public static String[] getModuleListSqlAndUrlStr(HttpServletRequest request,
            FormDb fd, String op, String orderBy, String sort, String userName,
            String fieldUserName) throws ErrMsgException {
		return SpringUtil.getBean(ISQLBuilderService.class).getModuleListSqlAndUrlStr(request,
				fd, op, orderBy, sort, userName,fieldUserName);
    }

    public static String[] getModuleListRelateSqlAndUrlStr(HttpServletRequest
            request,
            FormDb fd, String op, String orderBy, String sort,
            String relateFieldValue) {
        return getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue, "", "");
    }

    /**
     * 获取关联模块列表sql语句
     * @param request HttpServletRequest
     * @param fd FormDb 关联模块的表单实例
     * @param op String
     * @param orderBy String
     * @param sort String
     * @param relateFieldValue String
     * @param userName String
     * @param fieldUserName String
     * @return String[]
     */
    public static String[] getModuleListRelateSqlAndUrlStr(HttpServletRequest
            request,
            FormDb fd, String op, String orderBy, String sort,
            String relateFieldValue, String userName,
            String fieldUserName) {
    	return SpringUtil.getBean(ISQLBuilderService.class).getModuleListRelateSqlAndUrlStr(request,
				fd, op, orderBy, sort,
				relateFieldValue, userName,
				fieldUserName);
    }
    
    /**
     * 取得宏控件的条件和url字符串
     * @param request
     * @param ifmc
     * @param ff
     * @param name_cond
     * @param value
     * @param cond
     * @param urlStr
     * @return
     */
    public static String[] getMacroCondsAndUrlStrs(HttpServletRequest request, IFormMacroCtl ifmc, FormField ff, String name_cond, String value, String cond, String urlStr, String tableAlias) {
    	return SpringUtil.getBean(ISQLBuilderService.class).getMacroCondsAndUrlStrs(request, ifmc, ff, name_cond, value, cond, urlStr, tableAlias);
    }
    
    public static String getCondTypeDesc(HttpServletRequest request, String condType) {
    	String desc = "";
    	if (condType.equals(COND_TYPE_FUZZY)) {
    		desc = "模糊";
    	}
    	else if (condType.equals(COND_TYPE_NORMAL)) {
    		desc = "等于";
    	}
    	return desc;
    }

	/**
	 * 对模块日志列表中的sql语句进一步处理，使之与对应的模块相关联，如：副模块、过滤条件
	 * @param request
	 * @param sql
	 * @return
	 */
	public static String getListSqlForLogRelateModule(HttpServletRequest request, String sql, boolean isRead) {
		return SpringUtil.getBean(ISQLBuilderService.class).getListSqlForLogRelateModule(request, sql, isRead);
	}

	public static String getListSqlForLogRelateModuleBack(HttpServletRequest request, String sql) {
		return SpringUtil.getBean(ISQLBuilderService.class).getListSqlForLogRelateModuleBack(request, sql);
	}
}

