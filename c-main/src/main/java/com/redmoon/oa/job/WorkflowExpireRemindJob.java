package com.redmoon.oa.job;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.*;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import cn.js.fan.db.SQLFilter;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * <p>Title: 流程超时提醒高度</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 *
 * @version 1.0
 */
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class WorkflowExpireRemindJob extends QuartzJobBean {
    public WorkflowExpireRemindJob() {
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
        // 根据快要到期的myaction，发送提醒
        MyActionDb mad = new MyActionDb();

        Config cfg = new Config();

        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();

        // 是否发送短信
        boolean isToMobile = SMSFactory.isUseSMS();

        WorkflowActionDb wad = new WorkflowActionDb();
        WorkflowDb wd = new WorkflowDb();
        Vector<MyActionDb> v = mad.listWillExpire();
        for (MyActionDb myActionDb : v) {
            mad = myActionDb;
            // 发送信息
            MessageDb md = new MessageDb();
            wad = wad.getWorkflowActionDb((int) mad.getActionId());
            wd = wd.getWorkflowDb((int) mad.getFlowId());
            Leaf leaf = new Leaf(wd.getTypeCode());
            String t = "";
            String c = "";
            // 提示信息 modify by jfy 2015-06-24
            if (Leaf.TYPE_FREE == leaf.getType()) {
                t = StrUtil.format(cfg.get("flowActionExpireRemindTitle"),
                        new Object[]{wd.getTitle()});
                c = StrUtil.format(cfg.get("atFlowActionExpireRemindContent"),
                        new Object[]{wd.getTitle(),
                                DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm:ss")});
            } else {
                t = StrUtil.format(cfg.get("flowActionExpireRemindTitle"),
                        new Object[]{wd.getTitle()});
                c = StrUtil.format(cfg.get("flowActionExpireRemindContent"),
                        new Object[]{wad.getTitle(),
                                DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm:ss")});
            }

            try {
                if (!isToMobile) {
                    md.sendSysMsg(mad.getUserName(), t, c);
                } else {
                    if (imsg != null) {
                        imsg.sendSysMsg(mad.getUserName(), t, c);
                    }
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("execute2:" + e.getMessage());
            }
        }
    }
}
