package com.redmoon.oa;

import cn.js.fan.kernel.BaseSchedulerUnit;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.web.Global;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.PlanDb;
import java.sql.PreparedStatement;
import com.redmoon.oa.person.UserDb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.MyActionDb;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.pvg.OnlineUserDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FormDAO;

import java.sql.Timestamp;
import java.util.Date;
import cn.js.fan.util.DateUtil;

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
public class OASchedulerUnit extends BaseSchedulerUnit {
    static long lastRemindTime = System.currentTimeMillis(); // 日程提醒
    static long remindInterval = 600000; // 每隔10分钟日程提醒间隔

    static long lastClearProxyExpiredInterval = System.currentTimeMillis();
    static long clearProxyExpiredInterval = 300000; // 每隔5分钟清除到期代理

    static long lastClearUserMessageInerval = System.currentTimeMillis();
    static long clearUserMessageInterval = 86400000; // 每隔24小时清除用户超出容量的短信息

    public static int refreshOnlineInterval = 5000; // 5秒
    public static long lastRefreshOnlineIntervalTime = System.currentTimeMillis();
    
    public static long lastClearTmpAttachmentTime = System.currentTimeMillis();
    public static long clearTmpAttachmentInterval = 4 * 60 * 60 * 1000; // 每隔24小时删除临时图片    
    
    public OASchedulerUnit() {
        lastTime = System.currentTimeMillis();
        interval = 1800000; // 每隔30分钟刷新一次

        name= "OA Scheduler Unit";

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String str1 = cfg.get("interval_remind_user_plan");
        remindInterval = Long.parseLong(str1);

        String str2 = cfg.get("interval_clear_proxy_expired");
        clearProxyExpiredInterval = Long.parseLong(str2);

        String str3 = cfg.get("interval_flow_hurry");
        interval = Long.parseLong(str3);

        String str4 = cfg.get("interval_clear_message");
        clearUserMessageInterval = Long.parseLong(str4);

        String str5 = cfg.get("refreshOnlineInterval");
        refreshOnlineInterval = StrUtil.toInt(str5, 5)*1000;
        
    }

    /**
     * OnTimer
     *
     * @param currentTime long
     * @todo Implement this cn.js.fan.kernal.ISchedulerUnit method
     */
    public void OnTimer(long curTime) {
        // logger.info("curTime=" + curTime);
        if (curTime-lastTime>=interval) {
            action();
            lastTime = curTime;
        }
        // System.out.println("curTime=" + curTime);
        // System.out.println("lastRemindTime=" + lastRemindTime);
        // System.out.println("remindInterval=" + remindInterval);
        if (curTime - lastRemindTime >= remindInterval) {
            sendRemindMsg();
            lastRemindTime = curTime;
        }

        if (curTime - lastClearProxyExpiredInterval >= clearProxyExpiredInterval) {
            try {
                clearProxyExpired();
            }
            catch (ErrMsgException e) {
                com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("OnTimer:" + e.getMessage());
            }
            lastClearProxyExpiredInterval = curTime;
        }

        if (curTime - lastClearUserMessageInerval >= clearUserMessageInterval) {
            clearUserMessage();
            lastClearUserMessageInerval = curTime;
        }

        if (curTime - lastRefreshOnlineIntervalTime >= refreshOnlineInterval) {
            refreshOnlineUser();
            lastRefreshOnlineIntervalTime = curTime;
        }
        
        if (curTime - lastClearTmpAttachmentTime >=
            clearTmpAttachmentInterval) {
        	clearTmpFilearkAttachment();
        	
        	clearTmpFormTable();
        	
            lastClearTmpAttachmentTime = curTime;
        }        
    }

    public void clearUserMessage() {
        UserDb ud = new UserDb();
        Iterator ir = ud.list().iterator();
        MessageDb md = new MessageDb();
        while (ir.hasNext()) {
        	ud = (UserDb)ir.next();
            md.clearMsgOfUser(ud.getName());
        }
    }

    public synchronized void action() {
        sendFlowHurryMsg();
    }

    public void sendRemindMsg() {
        PlanDb pd = new PlanDb();
        // logger.info("here!" + System.currentTimeMillis());
        pd.makeRemindMsg();
    }

    public void sendFlowHurryMsg() {
        // logger.info("sendFlowHurryMsg sql=" + sql);
        /*
        MessageDb md = new MessageDb();
        WorkflowDb wf = new WorkflowDb();
        // 如果link的to节点是正处理状态，link是催办且已到催办日期
        String sql = "select a.flow_id,a.jobCode,l.speedup_date,l.title from flow_link l,flow_action a where l.isSpeedup=1 and l.speedup_date<NOW() and l.action_to=a.internal_name and a.status=" +
                              WorkflowActionDb.STATE_DOING;
        PostDb pd = new PostDb();
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt(1);
                wf = wf.getWorkflowDb(id);
                pd = pd.getPostDb(rs.getString(2));
                java.util.Date d = rs.getDate(3);

                String title = "请尽快办理：" + wf.getTitle();
                String content = rs.getString(4) + " --催办时间：" + DateUtil.format(d, "yyyy-MM-dd");
                md.sendSysMsg(pd.getUserName(), title, content);
            }
        } catch (SQLException e) {
            logger.error("sendFlowHurryMsg:" + e.getMessage());
        } catch (ErrMsgException e) {
            logger.error("sendFlowHurryMsg:" + e.getMessage());
        }
        finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        */
    }

    /**
     * 代理时间到后自动清除代理
     * @throws ErrMsgException
     * @deprecated
     */
    void clearProxyExpired() throws ErrMsgException {
    	if (true)
    		return;
    	
        // 找出代理期限已到期的用户
        String sql =
                "select name from users where proxyBeginDate>? and proxyEndDate<? and proxy<>''";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
            pstmt.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
            rs = conn.executePreQuery();
            while (rs.next()) {
                String userName = rs.getString(1);
                MyActionDb mad = new MyActionDb();
                // mad.clearProxyOfUser(userName);
            }
        } catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("clearProxyExpired:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    void refreshOnlineUser() {
        OnlineUserDb oud = new OnlineUserDb();
        oud.refreshOnlineUser();
    }

    /**
     * 清除两天前至前十天的临时图片文件
     */
    public void clearTmpIdioAttachment() {
        java.util.Date today = new Date();
        Date d2 = DateUtil.addDate(today, -2);
        Date d10 = DateUtil.addDate(today, -10);
        // logger.info("d2=" + DateUtil.format(d2, "yyyy-MM-dd") + " d10=" + DateUtil.format(d10, "yyyy-MM-dd"));
        String sql = "select id from oa_idiofileark_attach where msgId=" + com.redmoon.oa.idiofileark.IdioAttachment.TEMP_MSG_ID + " and upload_date>? and upload_date<?";
        // 如果加粗显示已到期
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(d10.getTime()));
            ps.setTimestamp(2, new Timestamp(d2.getTime()));
            rs = conn.executePreQuery();
            // logger.info("rows=" + conn.getRows() + " d10=" + d10.getTime());
            while (rs.next()) {
                int id = rs.getInt(1);
                // com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info("clearTmpAttachment: Delete temp attchment id=" + id);
                com.redmoon.oa.idiofileark.IdioAttachment att = new com.redmoon.oa.idiofileark.IdioAttachment(id);
                att.delTmpAttach();
            }
        } catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("clearTmpIdioAttachment:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }
    

    /**
     * 清除两天前至前十天文件柜的临时图片文件
     */
    public void clearTmpFilearkAttachment() {
        java.util.Date today = new Date();
        Date d2 = DateUtil.addDate(today, -2);
        Date d10 = DateUtil.addDate(today, -10);
        // logger.info("d2=" + DateUtil.format(d2, "yyyy-MM-dd") + " d10=" + DateUtil.format(d10, "yyyy-MM-dd"));
        String sql = "select id from document_attach where doc_id=" + com.redmoon.oa.fileark.Attachment.TEMP_DOC_ID + " and upload_date>? and upload_date<?";
        // 如果加粗显示已到期
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(d10.getTime()));
            ps.setTimestamp(2, new Timestamp(d2.getTime()));
            rs = conn.executePreQuery();
            // logger.info("rows=" + conn.getRows() + " d10=" + d10.getTime());
            while (rs.next()) {
                int id = rs.getInt(1);
                // com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info("clearTmpAttachment: Delete temp attchment id=" + id);
                com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment(id);
                att.delTmpAttach();
            }
        } catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("clearFilearkTmpAttachment:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }    

    /**
     * 清空嵌套表2添加的临时记录
     */
    public void clearTmpFormTable() {
    	FormDb fd = new FormDb();
    	Iterator ir = fd.list().iterator();
    	JdbcTemplate jt = new JdbcTemplate();
    	while (ir.hasNext()) {
    		fd = (FormDb)ir.next();
    		if (!fd.isLoaded()) {
    		    continue;
            }
    		int maxId = -1;
    		String sql = "select max(id) from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(FormDAO.FormDAO_NEW_ID);
    		try {
				ResultIterator ri = jt.executeQuery(sql);
				if (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					maxId = rr.getInt(1);
					
					int lastId = maxId - 100; // 100条以内不删除，以免误删新增的
					if (lastId>0) {
						sql = "delete from " + fd.getTableNameByForm() + " where id<=" + lastId;
						jt.executeUpdate(sql);
					}
				}
			} catch (SQLException e) {
                DebugUtil.log(getClass(), "clearTmpFormTable", sql);
				e.printStackTrace();
			}
    	}
    	
    }        
    
}
