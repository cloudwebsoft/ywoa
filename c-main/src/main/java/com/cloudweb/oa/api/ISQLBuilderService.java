package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.ModuleSetupDb;

import javax.servlet.http.HttpServletRequest;

public interface ISQLBuilderService {

    String[] getModuleListSqlAndUrlStr(HttpServletRequest request,
                                                     FormDb fd, String op, String orderBy, String sort) throws ErrMsgException;

    String[] getModuleListSqlAndUrlStr(HttpServletRequest request,
                                                     FormDb fd, String op, String orderBy, String sort, String userName,
                                                     String fieldUserName) throws ErrMsgException;

    Object[] fitCondAndUrlStr(HttpServletRequest request, ModuleSetupDb msd, FormDb fd);

    String[] getModuleListRelateSqlAndUrlStr(HttpServletRequest request,
                                                           FormDb fd, String op, String orderBy, String sort,
                                                           String relateFieldValue, String userName,
                                                           String fieldUserName);

    String[] getMacroCondsAndUrlStrs(HttpServletRequest request, IFormMacroCtl ifmc, FormField ff, String name_cond, String value, String cond, String urlStr, String tableAlias);

    String getListSqlForLogRelateModule(HttpServletRequest request, String sql, boolean isRead);

    String getListSqlForLogRelateModuleBack(HttpServletRequest request, String sql);
}
