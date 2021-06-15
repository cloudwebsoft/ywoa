package com.redmoon.oa.sms;

import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import org.quartz.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.ResultIterator;

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
public class ReceiveJob implements Job {

    public ReceiveJob() {
    }

    public void receiveSms() {
        try {
            IMsgUtil imu = SMSFactory.getMsgUtil();
            imu.receive();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("receiveSms:" + e.getMessage());
        }
   }

    /**
     * execute
     *
     * @param jobExecutionContext JobExecutionContext
     * @throws JobExecutionException
     * @todo Implement this org.quartz.Job method
     */
    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        try {
            receiveSms();

            // System.out.println(getClass() + " sql=" + System.currentTimeMillis());

        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("execute:" + e.getMessage());
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
    }
}
