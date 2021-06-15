package com.redmoon.oa.visual;

import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormField;

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
    boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException;
    boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector<FormField> fields) throws ErrMsgException;
    boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException;
    boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out);
}
