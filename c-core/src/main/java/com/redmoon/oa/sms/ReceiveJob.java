package com.redmoon.oa.sms;

import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.ResultIterator;
import org.springframework.scheduling.quartz.QuartzJobBean;

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
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class ReceiveJob extends QuartzJobBean {

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
     */
    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        try {
            receiveSms();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("execute:" + e.getMessage());
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
    }
}
