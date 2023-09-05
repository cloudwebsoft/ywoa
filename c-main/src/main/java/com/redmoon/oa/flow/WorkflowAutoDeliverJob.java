package com.redmoon.oa.flow;

import com.redmoon.oa.ui.LocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import com.redmoon.oa.person.UserDb;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.aop.base.Advisor;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import java.util.Vector;
import com.redmoon.oa.message.MobileAfterAdvice;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.Config;
import cn.js.fan.web.Global;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

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
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class WorkflowAutoDeliverJob extends QuartzJobBean {
    public WorkflowAutoDeliverJob() {
    }

    /**
     * 自动转交超期动作
     */
    public void autoDeliverActionExpired() {
        // 取得三天内的过期待办记录
        java.util.Date d = DateUtil.addDate(new java.util.Date(), -3);

        // LogUtil.getLog(getClass()).info(getClass() + " WorkflowJob autoDeliverActionExpired:" + DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"));

        MyActionDb mad2 = new MyActionDb();
        // LogUtil.getLog(getClass()).info(DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"));
        String sql = "select id from " + mad2.getTableName() +
                     " where is_checked=0 and expire_date>? and expire_date<?";
        WorkflowActionDb wad = new WorkflowActionDb();
        WorkflowLinkDb wld = null;
        WorkflowDb wf = new WorkflowDb();
        MessageDb md = new MessageDb();
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql, new Object[] {d, new java.util.Date()});
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("autoDeliverActionExpired:" + StrUtil.trace(e));
        }

        // LogUtil.getLog(getClass()).info("ri.size=" + ri.size());

        while (ri.hasNext()) {
        	wld = new WorkflowLinkDb();
            ResultRecord rr = ri.next();
            MyActionDb mad = mad2.getMyActionDb(rr.getLong(1));
            
            String curUserName = mad.getUserName();
            
            // 检查mad连接线上超期处理设置
            MyActionDb privMad = mad2.getMyActionDb(mad.getPrivMyActionId());
            WorkflowActionDb privAction = wad.getWorkflowActionDb((int) privMad.getActionId());
            WorkflowActionDb curAction = wad.getWorkflowActionDb((int) mad.getActionId());
            wld = wld.getWorkflowLinkDbForward(privAction, curAction);
            String actionInnerName = (wld != null ? wld.getExpireAction() : "");

            // LogUtil.getLog(getClass()).info("actionInnerName=" + actionInnerName);
            
            // 如果设置了超期处理
            if (!StrUtil.isEmpty(actionInnerName)) {
                if ("next".equals(actionInnerName)) {
                    // 更改状态
                    // mad.setActionStatus(WorkflowActionDb.STATE_IGNORED);
                    LogUtil.getLog(getClass()).info("actionInnerName=" + actionInnerName + " actionStatus=" +
                                                    mad.getActionStatus());
                    mad.setChecked(true);
                    mad.setCheckDate(new java.util.Date());
                    mad.setChecker(UserDb.SYSTEM);
                    mad.setResult(LocalUtil.LoadString(null, "res.flow.Flow", "skipOverTime"));
                    mad.save();

                    // 结束日程
                    mad.onChecked();

                    wf = wf.getWorkflowDb((int) mad.getFlowId());

                    // 置待办记录对应的action的状态为被忽略
                    curAction.setStatus(WorkflowActionDb.STATE_IGNORED);
                    try {
                        curAction.save();
                    } catch (ErrMsgException ex3) {
                        ex3.printStackTrace();
                    }

                    IMessage imsg = null;
                    ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
                    Advisor adv = new Advisor();
                    MobileAfterAdvice mba = new MobileAfterAdvice();
                    adv.setAdvice(mba);
                    adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
                    proxyFactory.addAdvisor(adv);
                    imsg = (IMessage) proxyFactory.getProxy();

                    // 发送给待办记录人员超时被跳过短信
                    try {
                        String t = SkinUtil.LoadString(null, "res.module.flow", "msg_user_expired_title");
                        String c = SkinUtil.LoadString(null, "res.module.flow", "msg_user_expired_content");
                        t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                        c = c.replaceFirst("\\$flowTitle", wf.getTitle());

                        // getFormAbstractTable中SpringUtil.getRequest()会报错：No thread-bound request found: Are you referring to request attributes outside of an actual web request, or processing a request outside of the originally receiving thread?
                        // c += WorkflowMgr.getFormAbstractTable(wf);

                        if (imsg != null) {
                            imsg.sendSysMsg(mad.getUserName(), t, c);
                        } else {
                            md.sendSysMsg(mad.getUserName(), t, c);
                        }
                    } catch (ErrMsgException ex2) {
                        ex2.printStackTrace();
                    }

                    long myActionId = mad.getId();
                    MyActionDb m = new MyActionDb();
                    m = m.getMyActionDb(myActionId);
                    WorkflowActionDb wa1 = new WorkflowActionDb();
                    wa1 = wa1.getWorkflowActionDb((int) m.getActionId());

                    WorkflowActionDb wa = new WorkflowActionDb();
                    try {
                        HttpServletRequest request = null;
                        
                        // 赋予为被跳过的用户，以便于在autoPassActionNoUserMatched中matchActionUser时，因为request为null，但是仍需取得用户名，所以利用wa的userName来传值
                        curAction.setUserName(curUserName);
                        
                        wa = wa.autoPassActionNoUserMatched(request, myActionId, wa1, curAction, null, true);
                        // 如果连续存在未匹配到用户的节点，则继续跳过
                        while (wa != null) {
                            wa = wa.autoPassActionNoUserMatched(request, myActionId, wa1, wa, null, true);
                        }
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                    }
                } else if ("starter".equals(actionInnerName)) { // 超时返回给发起人
                    // 更改状态
                    // mad.setActionStatus(WorkflowActionDb.STATE_IGNORED);
                    LogUtil.getLog(getClass()).info("starter actionInnerName=" + actionInnerName + " actionStatus=" +
                                                    mad.getActionStatus());
                    mad.setChecked(true);
                    mad.setCheckDate(new java.util.Date());
                    mad.setChecker(UserDb.SYSTEM);
                    // mad.setResult("超期未能处理，系统自动返回给发起人");
                    mad.setResult(LocalUtil.LoadString(null, "res.flow.Flow", "skipOverTimeReturnToStarter"));

                    mad.save();

                    // 结束日程
                    mad.onChecked();

                    LogUtil.getLog(getClass()).info("starter mad.onChecked");

                    wf = wf.getWorkflowDb((int) mad.getFlowId());

                    // 置待办记录对应的action的状态为被忽略
                    // curAction.setStatus(WorkflowActionDb.STATE_IGNORED);
                    curAction.setStatus(WorkflowActionDb.STATE_RETURN);
                    try {
                        curAction.save();
                    } catch (ErrMsgException ex3) {
                        ex3.printStackTrace();
                    }

                    LogUtil.getLog(getClass()).info("starter wf=" + wf);

                    IMessage imsg = null;
                    ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
                    Advisor adv = new Advisor();
                    MobileAfterAdvice mba = new MobileAfterAdvice();
                    adv.setAdvice(mba);
                    adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
                    proxyFactory.addAdvisor(adv);
                    imsg = (IMessage) proxyFactory.getProxy();

                    LogUtil.getLog(getClass()).info("starter imsg=" + imsg);

                    // 发送给待办记录人员超时被跳过短信
                    try {
                        String t = SkinUtil.LoadString(null, "res.module.flow", "msg_user_expired_return_to_starter_title");
                        String c = SkinUtil.LoadString(null, "res.module.flow", "msg_user_expired_return_to_starter_content");
                        t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                        c = c.replaceFirst("\\$flowTitle", wf.getTitle());

                        c += WorkflowMgr.getFormAbstractTable(wf);

                        if (imsg != null) {
                            imsg.sendSysMsg(mad.getUserName(), t, c);
                        } else {
                            md.sendSysMsg(mad.getUserName(), t, c);
                        }
                    } catch (ErrMsgException ex2) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex2));
                    }

                    // 返回给发起人
                    HttpServletRequest request = null;

                    wf.getStartActionId();

                    String reason = ""; // "超期未能处理，系统自动返回"; 因为此处WorkflowActionDb在返回时，与原来的处理记录在flow_modify.jsp中是共用的，会出现重复提示
                    String[] returnIds = new String[] {String.valueOf(wf.getStartActionId())};
                    curAction.setReturnIds(returnIds);
                    boolean re = false;

                    try {
                        re = curAction.changeStatus(request, wf,
                                                    UserDb.SYSTEM,
                                                    WorkflowActionDb.STATE_NOTDO, reason, "",
                                                    curAction.getResultValue(),
                                                    mad.getId());
                    } catch (ErrMsgException ex4) {
                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex4));
                    }

                    LogUtil.getLog(getClass()).info("starter re=" + re + " returnIds[0]=" + returnIds[0]);

                    if (re) {
                        mad.returnMyAction();

                        boolean isUseMsg = true;
                        boolean isToMobile = SMSFactory.isUseSMS();

                        UserMgr um = new UserMgr();
                        String userRealName = um.getUserDb(mad.getUserName()).
                                              getRealName();

                        Config cfg = new Config();
                        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
                        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail();
                        String senderName = StrUtil.GBToUnicode(Global.AppName);
                        senderName += "<" + Global.getEmail() + ">";
                        if (flowNotifyByEmail) {
                            String mailserver = Global.getSmtpServer();
                            int smtp_port = Global.getSmtpPort();
                            String name = Global.getSmtpUser();
                            String pwd_raw = Global.getSmtpPwd();
                            try {
                                sendmail.initSession(mailserver, smtp_port, name,
                                                     pwd_raw);
                            } catch (Exception ex) {
                                LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
                            }
                        }

                        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();

                        UserDb user = new UserDb();
                        String t = SkinUtil.LoadString(request,
                                                       "res.module.flow",
                                                       "msg_user_actived_title");
                        String c = SkinUtil.LoadString(request,
                                                       "res.module.flow",
                                                       "msg_user_returned_content");

                        String tail = WorkflowMgr.getFormAbstractTable(wf);

                        for (MyActionDb mad3 : curAction.getTmpUserNameActived()) {
                            t = t.replaceFirst("\\$flowTitle", wf.getTitle());
                            String fc = c.replaceFirst("\\$flowTitle", wf.getTitle());
                            fc = fc.replaceFirst("\\$fromUser", userRealName);

                            if (isToMobile) {
                                IMsgUtil imu = SMSFactory.getMsgUtil();
                                if (imu != null) {
                                    UserDb ud = um.getUserDb(mad3.getUserName());
                                    try {
                                        imu.send(ud, fc, MessageDb.SENDER_SYSTEM);
                                    } catch (ErrMsgException ex5) {
                                        LogUtil.getLog(getClass()).error(StrUtil.trace(ex5));
                                    }
                                }
                            }

                            fc += tail;
                            if (isUseMsg) {
                                // 发送信息
                                String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad3.getId();
                                try {
                                    md.sendSysMsg(mad3.getUserName(), t, fc, action);
                                } catch (ErrMsgException ex6) {
                                    LogUtil.getLog(getClass()).error(StrUtil.trace(ex6));
                                }
                            }

                            if (flowNotifyByEmail) {
                                user = user.getUserDb(mad3.getUserName());
                                if (!StrUtil.isEmpty(user.getEmail())) {
                                    String action = "userName=" + user.getName() + "|" + "myActionId=" + mad3.getId();
                                    action = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), action);
                                    fc += "<BR />>>&nbsp;<a href='" +
                                            WorkflowUtil.getJumpUrl(WorkflowUtil.OP_FLOW_PROCESS, action) +
                                            "' target='_blank'>请点击此处办理</a>";
                                    sendmail.initMsg(user.getEmail(), senderName, t, fc, true);
                                    sendmail.send();
                                    sendmail.clear();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        autoDeliverActionExpired();
    }
}
