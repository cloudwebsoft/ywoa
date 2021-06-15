package com.redmoon.oa.flow;

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
import com.redmoon.oa.message.*;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.FormDAO;
import org.quartz.*;
import cn.js.fan.db.SQLFilter;

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
 *
 * @version 1.0
 */
public class WorkflowJob implements Job {
    public WorkflowJob() {
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

        Vector v = mad.listWillExpire();
        Iterator ir = v.iterator();
        WorkflowActionDb wad = new WorkflowActionDb();
        WorkflowDb wd = new WorkflowDb();
        while (ir.hasNext()) {
            mad = (MyActionDb) ir.next();
            // 发送信息
            MessageDb md = new MessageDb();
            wad = wad.getWorkflowActionDb((int) mad.getActionId());
            wd = wd.getWorkflowDb((int) mad.getFlowId());
            Leaf leaf = new Leaf(wd.getTypeCode());
            String t = "";
            String c = "";
            //自由流程提示信息 modify by jfy 2015-06-24
            if(Leaf.TYPE_FREE == leaf.getType()){
            	t = StrUtil.format(cfg.get("flowActionExpireRemindTitle"),
                        new Object[] {wd.getTitle()});
            	c = StrUtil.format(cfg.get("atFlowActionExpireRemindContent"),
                         new Object[] {wd.getTitle(),
                         DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm:ss")});
            }else{
            	t = StrUtil.format(cfg.get("flowActionExpireRemindTitle"),
                        new Object[] {wd.getTitle()});
            	c = StrUtil.format(cfg.get("flowActionExpireRemindContent"),
                         new Object[] {wad.getTitle(),
                         DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm:ss")});
            }
            
            try {
                if (!isToMobile)
                    md.sendSysMsg(mad.getUserName(), t, c);
                else {
                    if (imsg != null)
                        imsg.sendSysMsg(mad.getUserName(), t, c);
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("execute2:" + e.getMessage());
            }
        }

        // 从现在往前推一天，删除一天前的未生效流程
        WorkflowDb wf = new WorkflowDb();
        java.util.Date d = DateUtil.addDate(new java.util.Date(), -1);
        String sql = "select id from flow where status=" + WorkflowDb.STATUS_NONE + " and mydate<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
        ir = wf.list(sql).iterator();
        while (ir.hasNext()) {
            wf = (WorkflowDb)ir.next();
            try {
                wf.del();
            } catch (ErrMsgException ex) {
                ex.printStackTrace();
            }
        }

        // 删除一天前的未生效嵌套表中的临时记录
        FormDAO fdao = new FormDAO();
        FormDb fd = new FormDb();
        ir = fd.list().iterator();
        while (ir.hasNext()) {
            fd = (FormDb)ir.next();
            sql = "select id from " + fd.getTableNameByForm() + " where cws_id='" + FormDAO.NAME_TEMP_CWS_IDS + "' and cws_create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
            try {
                Iterator irFdao = fdao.list(fd.getCode(), sql).iterator();
                while (irFdao.hasNext()) {
                    fdao = (FormDAO)irFdao.next();
                    fdao.del();
                    DebugUtil.i(getClass(), "execute", fd.getTableNameByForm() + " nest_sheet tempory record id：" + fdao.getId());
                }
            } catch (ErrMsgException e) {
                DebugUtil.i(getClass(), "execute", sql);
                e.printStackTrace();
            }

            // 删除手机端智能模块添加嵌套表时生成的临时数据
            sql = "select id from " + fd.getTableNameByForm() + " where flowTypeCode='-1' and cws_create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
            try {
                Iterator irFdao = fdao.list(fd.getCode(), sql).iterator();
                while (irFdao.hasNext()) {
                    fdao = (FormDAO)irFdao.next();
                    fdao.del();
                    DebugUtil.i(getClass(), "execute", fd.getTableNameByForm() + " nest_sheet tempory record from mobile id：" + fdao.getId());
                }
            } catch (ErrMsgException e) {
                DebugUtil.i(getClass(), "execute", sql);
                e.printStackTrace();
            }
        }

        // 删除因为ntko控件，在增加页面时产生的临时文件，当创建记录时，该临时文件并不会变为正式文件，后台会创建一个新的visual_attach记录
        sql = "select id from visual_attach where visualId=-1 and create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                Attachment att = new Attachment(rr.getInt(1));
                if (att.isLoaded()) {
                    att.del();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
