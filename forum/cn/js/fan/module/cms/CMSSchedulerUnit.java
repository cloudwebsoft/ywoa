package cn.js.fan.module.cms;

import cn.js.fan.kernel.BaseSchedulerUnit;
import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.util.Date;
import java.sql.PreparedStatement;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;


/**
 * <p>Title: CMS调度</p>
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
public class CMSSchedulerUnit extends BaseSchedulerUnit {
    static Logger logger = Logger.getLogger(CMSSchedulerUnit.class.getName());


    public static long lastClearTmpAttachmentTime = System.currentTimeMillis();

    public static long clearTmpAttachmentInterval = 4 * 60 * 60 * 1000; // 每隔24小时删除临时图片

    static {
        initParam();
    }

    public CMSSchedulerUnit() {
        lastTime = System.currentTimeMillis();
        interval = 600000; // 每隔10分钟刷新一次
        name = "CMS Scheduler";
    }

    public static void initParam() {
    }

    /**
     * OnTimer
     *
     * @param currentTime long
     * @todo Implement this cn.js.fan.kernal.ISchedulerUnit method
     */
    public void OnTimer(long curTime) {
        // logger.info("curTime=" + curTime);
        try {
            if (curTime - lastClearTmpAttachmentTime >=
                clearTmpAttachmentInterval) {
                clearTmpAttachment();
                lastClearTmpAttachmentTime = curTime;
            }
        }
        catch (Throwable e) {
            // 防止运行有异常，导致线程退出
            logger.error("OnTimer:" + StrUtil.trace(e));
        }
    }

    /**
     * 清除两天前至前十天的临时图片文件
     */
    public void clearTmpAttachment() {
        java.util.Date today = new Date();
        Date d2 = DateUtil.addDate(today, -2);
        Date d10 = DateUtil.addDate(today, -10);
        // logger.info("d2=" + DateUtil.format(d2, "yyyy-MM-dd") + " d10=" + DateUtil.format(d10, "yyyy-MM-dd"));
        String sql = "select id from document_attach where doc_id=-1 and upload_date>? and upload_date<?";
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
                int id = rs.getInt(1);
                logger.info("clearTmpAttachment: Delete temp attchment id=" + id);
                Attachment att = new Attachment(id);
                att.delTmpAttach();
            }
        } catch (SQLException e) {
            logger.error("clearTmpAttachment:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

}
