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
public class FormRemindMgr extends QObjectMgr {
    public FormRemindMgr() {
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
        
        
        int ahead_day = pck.getInt("ahead_day");
        int ahead_hour = pck.getInt("ahead_hour");
        int ahead_minute = pck.getInt("ahead_minute");        
        if (ahead_day==0 && ahead_hour==0 && ahead_minute==0) {
        	throw new ErrMsgException("提前天数、小时、分钟，至少须填写其中一项！");
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
        
        int ahead_day = pck.getInt("ahead_day");
        int ahead_hour = pck.getInt("ahead_hour");
        int ahead_minute = pck.getInt("ahead_minute");
        
        if (ahead_day==0 && ahead_hour==0 && ahead_minute==0) {
        	throw new ErrMsgException("提前天数、小时、分钟，至少须填写其中一项！");
        }

        try {
            re = qObjectDb.save(pck);
        } catch (ResKeyException rsKeyException) {
            throw new ErrMsgException(rsKeyException.getMessage(request));
        }
        return re;
    }
}
