package com.redmoon.forum.sms;

import com.cloudwebsoft.framework.base.*;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import java.util.Vector;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class SMSReceiveRecordDb extends QObjectDb {
    public SMSReceiveRecordDb() {
    }

    public Vector listReceived(String orgAddr, Date d) {
        d = DateUtil.addMinuteDate(d, -30); // 假定服务器时间与移动信号的时间晚30分钟

        // 以24小时为限
        String sql = "select sequenceno from " + table.getName() +
                     " where destaddr=? and receivedate between ? and ?";
        Config cfg = new Config();
        String h = cfg.getIsUsedProperty("expireHour");
        int hour = 24;
        try {
            hour = Integer.parseInt(h);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("listReceived:" + e.getMessage());
        }
        Date expireDate = DateUtil.addHourDate(d, hour);

        return list(new JdbcTemplate(new Connection(table.getConnName())), sql, new Object[]{orgAddr, d, expireDate});
    }
}
