package com.redmoon.forum;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;

/**
 * <p>Title: 贴子举报代理类</p>
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
public class MessageRecommendMgr {
    public MessageRecommendMgr() {
    }

    public boolean create(HttpServletRequest request, QObjectDb qObjectDb,
                          String formCode) throws
            ErrMsgException {

        boolean re = false;
        ParamConfig pc = new ParamConfig(qObjectDb.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = qObjectDb.create(jt, pck);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean save(HttpServletRequest request,
                        QObjectDb qObjectDb, String formCode) throws
            ErrMsgException {

        ParamConfig pc = new ParamConfig(qObjectDb.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }
        boolean re = false;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = qObjectDb.save(jt, pck);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

}
