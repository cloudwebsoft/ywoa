package com.redmoon.forum;

import cn.js.fan.kernel.BaseSchedulerUnit;
import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.util.Date;
import java.sql.PreparedStatement;
import com.redmoon.forum.message.MessageDb;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;


/**
 * <p>Title: 论坛调度</p>
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
public class ForumSchedulerUnit extends BaseSchedulerUnit {
    public static int refreshOnlineInterval = 5; // 5分钟
    public static long lastRefreshOnlineIntervalTime;

    public static long lastClearUserMessageInerval = System.currentTimeMillis();
    public static long lastClearTmpAttachmentTime = System.currentTimeMillis();

    public static long clearUserMessageInterval = 24 * 60 * 60 * 1000; // 每隔24小时清除用户超出容量的短信息
    public static long clearTmpAttachmentInterval = 4 * 60 * 60 * 1000; // 每隔24小时删除临时图片
    public static int message_expire_day = 150; // 天

    static {
        initParam();
    }

    public ForumSchedulerUnit() {
        lastTime = System.currentTimeMillis();
        interval = 600000; // 每隔10分钟刷新一次
        lastRefreshOnlineIntervalTime = System.currentTimeMillis();
        lastClearUserMessageInerval = System.currentTimeMillis();
        name = "Forum Scheduler";
    }

    public static void initParam() {
        Config cfg = Config.getInstance();
        refreshOnlineInterval = cfg.getIntProperty("forum.refreshOnlineInterval") * 60 * 1000;
        message_expire_day = cfg.getIntProperty("forum.message_expire_day");
        clearUserMessageInterval = cfg.getIntProperty("interval_clear_message") * 60 * 60 * 1000;
    }

    /**
     * OnTimer
     *
     * @param currentTime long
     * @todo Implement this cn.js.fan.kernal.ISchedulerUnit method
     */
    @Override
    public void OnTimer(long curTime) {
        // logger.info("curTime=" + curTime);
        try {
            if (curTime - lastTime >= interval) {
                action();
                lastTime = curTime;
            }
            if (curTime - lastRefreshOnlineIntervalTime >=
                refreshOnlineInterval) {
                OnlineUserDb oud = new OnlineUserDb();
                oud.refreshOnlineUser();
                lastRefreshOnlineIntervalTime = curTime;
            }

            if (curTime - lastClearUserMessageInerval >=
                clearUserMessageInterval) {
                clearMessageExpired(message_expire_day);
                lastClearUserMessageInerval = curTime;
            }

            if (curTime - lastClearTmpAttachmentTime >=
                clearTmpAttachmentInterval) {
                clearTmpAttachment();
                lastClearTmpAttachmentTime = curTime;
            }
        }
        catch (Throwable e) {
            // 防止运行有异常，导致线程退出
            LogUtil.getLog(getClass()).error("OnTimer:" + StrUtil.trace(e));
        }
    }

    public void clearMessageExpired(int expireDay) {
        MessageDb md = new MessageDb();
        md.clearMessageExpired(expireDay);
    }

    @Override
    public synchronized void action() {
        refreshColor();
        refreshBold();
        refreshLevel();
    }

    /**
     * 清除两天前至前十天的临时图片文件
     */
    public void clearTmpAttachment() {
        java.util.Date today = new Date();
        Date d2 = DateUtil.addDate(today, -2);
        Date d10 = DateUtil.addDate(today, -10);
        // logger.info("d2=" + DateUtil.format(d2, "yyyy-MM-dd") + " d10=" + DateUtil.format(d10, "yyyy-MM-dd"));
        String sql = "select id from sq_message_attach where msgId=-1 and UPLOAD_DATE>? and UPLOAD_DATE<?";
        // 如果加粗显示已到期
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "" + d10.getTime());
            ps.setString(2, "" + d2.getTime());
            rs = conn.executePreQuery();
            // logger.info("rows=" + conn.getRows() + " d10=" + d10.getTime());
            while (rs.next()) {
                long id = rs.getLong(1);
                LogUtil.getLog(getClass()).info("clearTmpAttachment: Delete temp attchment id=" + id);
                Attachment att = new Attachment(id);
                att.delTmpAttach();
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("clearTmpAttachment:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void refreshBold() {
        MsgDb md = new MsgDb();
        String sql = "select id from sq_message where isBold=1 and boldExpire<?";
        // 如果加粗显示已到期
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "" + System.currentTimeMillis());
            rs = conn.executePreQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                // logger.info("msgRootId=" + msgRootId);
                md = md.getMsgDb(id);
                try {
                    md.ChangeBold("", 0, new Date(), "127.0.0.1");
                } catch (ResKeyException e) {
                    LogUtil.getLog(getClass()).error("refreshBold:" + e.getMessage());
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("refreshBold:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void refreshLevel() {
        MsgDb md = new MsgDb();
        String sql = "select id from sq_message where msg_level>" + MsgDb.LEVEL_NONE + " and level_expire<?";
        // 置顶已到期
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "" + System.currentTimeMillis());
            rs = conn.executePreQuery();
            while (rs.next()) {
                long id = rs.getLong(1);
                // logger.info("msgRootId=" + msgRootId);
                md = md.getMsgDb(id);
                try {
                    md.setOnTop(MsgDb.LEVEL_NONE, null);
                } catch (ResKeyException e) {
                    LogUtil.getLog(getClass()).error("action:" + e.getMessage());
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("refreshBold:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void refreshColor() {
        MsgDb md = new MsgDb();
        String sql = "select id from sq_message where color<>'' and colorExpire<?";
        // 如果颜色值已到期
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "" + System.currentTimeMillis());
            rs = conn.executePreQuery();
            // logger.info("rs=" + rs);
            while (rs.next()) {
                long id = rs.getLong(1);
                // logger.info("msgRootId=" + msgRootId);
                md = md.getMsgDb(id);
                try {
                    md.ChangeColor("", "", new Date(), "127.0.0.1");
                } catch (ResKeyException e) {
                    LogUtil.getLog(getClass()).error("refreshColor:" + e.getMessage());
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("refreshColor1:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

}
