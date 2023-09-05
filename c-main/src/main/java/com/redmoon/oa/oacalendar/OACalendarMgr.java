package com.redmoon.oa.oacalendar;

import com.cloudwebsoft.framework.base.QObjectMgr;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.base.QObjectDb;

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

public class OACalendarMgr extends QObjectMgr {
    public OACalendarMgr() {
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

        String a = pck.getString("work_time_begin_a");
        String b = pck.getString("work_time_end_a");
        String c = pck.getString("work_time_begin_b");
        String d = pck.getString("work_time_end_b");
        //20011105
        String e = pck.getString("work_time_begin_c");
        String f = pck.getString("work_time_end_c");

        if (!a.equals("") && b.equals(""))
            throw new ErrMsgException("上午工作时段仅填写了上班时间");
        if (a.equals("") && !b.equals(""))
            throw new ErrMsgException("上午工作时段仅填写了下班时间");
        if (!c.equals("") && d.equals(""))
            throw new ErrMsgException("下午工作时段仅填写了上班时间");
        if (c.equals("") && !d.equals(""))
            throw new ErrMsgException("下午工作时段仅填写了下班时间");
        if (!e.equals("") && f.equals(""))
            throw new ErrMsgException("晚上工作时段仅填写了上班时间");
        if (e.equals("") && !f.equals(""))
            throw new ErrMsgException("晚上工作时段仅填写了下班时间");
        
        java.util.Date dt = (java.util.Date)pck.getValue("oa_date");
		int weekDay = OACalendarDb.getDayOfWeek(DateUtil.getYear(dt), DateUtil.getMonth(dt), DateUtil.getDay(dt));

        pck.setValue("week_day", "星期几", weekDay);
        
        try {
            re = qObjectDb.create(pck);
        } catch (ResKeyException rsKeyException) {
            throw new ErrMsgException(rsKeyException.getMessage(request));
        }
        return re;
    }
}
