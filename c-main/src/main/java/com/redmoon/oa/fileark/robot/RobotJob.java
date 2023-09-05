package com.redmoon.oa.fileark.robot;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.scheduling.quartz.QuartzJobBean;

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
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class RobotJob extends QuartzJobBean {
    public RobotJob() {
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
        String strId = data.getString("id");
        int id = Integer.parseInt(strId);

        RobotDb rd = new RobotDb();
        rd = (RobotDb) rd.getQObjectDb(new Integer(id));
        Roboter rt = new Roboter();
        try {
            rt.gatherList(null, rd);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("execute:" + e.getMessage());
        }
    }
}
