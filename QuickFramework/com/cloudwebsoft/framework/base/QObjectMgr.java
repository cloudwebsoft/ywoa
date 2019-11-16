package com.cloudwebsoft.framework.base;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.kit.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class QObjectMgr {
    public QObjectMgr() {
    }

    public boolean create(HttpServletRequest request, QObjectDb qObjectDb, String formCode) throws
            ErrMsgException {
        boolean re = false;
        ParamConfig pc = new ParamConfig(qObjectDb.getTable().getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        try {
            re = qObjectDb.create(pck);
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean save(HttpServletRequest request, QObjectDb qObjectDb, String formCode) throws ErrMsgException {
        ParamConfig pc = new ParamConfig(qObjectDb.getTable().getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }
        boolean re = false;
        try {
            re = qObjectDb.save(pck);
        }
        catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean create(ServletContext application,
                          HttpServletRequest request, QObjectDb qObjectDb,
                          String formCode) throws
            ErrMsgException {
        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        FileUpload fu = new FileUpload();
        try {
            fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }

        boolean re = false;
        ParamConfig pc = new ParamConfig(qObjectDb.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request, fu);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        try {
            re = qObjectDb.create(pck);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean save(ServletContext application, HttpServletRequest request,
                        QObjectDb qObjectDb, String formCode) throws
            ErrMsgException {
        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        FileUpload fu = new FileUpload();
        try {
            fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }

        ParamConfig pc = new ParamConfig(qObjectDb.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request, fu);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }
        boolean re = false;
        try {
            re = qObjectDb.save(pck);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }
}
