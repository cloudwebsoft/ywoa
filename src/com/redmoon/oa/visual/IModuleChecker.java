package com.redmoon.oa.visual;

import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

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
public interface IModuleChecker {
    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException;
    public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException;
    public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    /**
     * 用于nest_table_view.jsp中添加记录时初始化
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param out JspWriter
     * @return boolean 如果需初始化，则返回true，否则返回false
     */
    public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out);
}
