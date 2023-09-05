package com.redmoon.oa.flow;

import com.cloudwebsoft.framework.base.QObjectMgr;
import com.cloudwebsoft.framework.base.QObjectDb;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.*;
import java.util.Vector;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormQueryReportMgr extends QObjectMgr {
    public FormQueryReportMgr() {
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
        } catch (ResKeyException rsKeyException) {
            throw new ErrMsgException(rsKeyException.getMessage(request));
        }
        return re;
    }

    @Override
    public boolean save(HttpServletRequest request, QObjectDb qObjectDb, String formCode) throws
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

        String content = ParamUtil.get(request, "content");
        Vector[] ary = FormQueryReportRender.parseCell(content);
        Vector vtY = ary[1];
        if (vtY.size()>1) {
            throw new ErrMsgException("不能同时出现两个Y轴元素！");
        }

        try {
            re = qObjectDb.save(pck);
        } catch (ResKeyException rsKeyException) {
            throw new ErrMsgException(rsKeyException.getMessage(request));
        }
        return re;
    }
}
