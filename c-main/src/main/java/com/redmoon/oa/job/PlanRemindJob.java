package com.redmoon.oa.job;

import com.redmoon.oa.person.PlanDb;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class PlanRemindJob extends QuartzJobBean {

    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        PlanDb pd = new PlanDb();
        pd.makeRemindMsg();
    }
}
