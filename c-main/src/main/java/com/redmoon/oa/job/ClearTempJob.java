package com.redmoon.oa.job;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.FormDAO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;

//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class ClearTempJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        clearTmpFormTable();
        clearTmpAttachment();
        clearTmpWorkflow();
    }

    /**
     * 清空嵌套表2添加的临时记录
     */
    public void clearTmpFormTable() {
        // 删除一天前的未生效嵌套表中的临时记录
        java.util.Date d = DateUtil.addDate(new java.util.Date(), -1);
        FormDAO fdao = new FormDAO();
        FormDb fd = new FormDb();
        for (Object o : fd.list()) {
            fd = (FormDb) o;
            String sql = "select id from " + fd.getTableNameByForm() + " where cws_id='" + FormDAO.TEMP_CWS_ID + "' and cws_create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
            try {
                for (FormDAO formDAO : fdao.list(fd.getCode(), sql)) {
                    fdao = formDAO;
                    fdao.del();
                    DebugUtil.i(getClass(), "execute", fd.getTableNameByForm() + " nest_sheet tempory record id：" + fdao.getId());
                }
            } catch (ErrMsgException e) {
                DebugUtil.i(getClass(), "execute", sql);
                LogUtil.getLog(getClass()).error(e);
            }

            // 删除手机端智能模块添加嵌套表时生成的临时数据
            sql = "select id from " + fd.getTableNameByForm() + " where flowTypeCode='-1' and cws_create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
            try {
                for (FormDAO formDAO : fdao.list(fd.getCode(), sql)) {
                    fdao = formDAO;
                    fdao.del();
                    DebugUtil.i(getClass(), "execute", fd.getTableNameByForm() + " nest_sheet tempory record from mobile id：" + fdao.getId());
                }
            } catch (ErrMsgException e) {
                DebugUtil.i(getClass(), "execute", sql);
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }

    // 清空智能模块添加页面产生的临时文件
    public void clearTmpAttachment() {
        java.util.Date d = DateUtil.addDate(new java.util.Date(), -1);
        String sql = "select id from visual_attach where visualId=-1 and create_date<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                Attachment att = new Attachment(rr.getInt(1));
                if (att.isLoaded()) {
                    att.del();
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }

    /**
     * 从现在往前推一天，删除一天前的未生效流程
     */
    public void clearTmpWorkflow() {
        java.util.Date d = DateUtil.addDate(new java.util.Date(), -1);
        WorkflowDb wf = new WorkflowDb();
        String sql = "select id from flow where status=" + WorkflowDb.STATUS_NONE + " and mydate<=" + SQLFilter.getDateStr(DateUtil.format(d, "yyyy-MM-dd"), "yyyy-MM-dd");
        for (WorkflowDb workflowDb : wf.list(sql)) {
            wf = workflowDb;
            try {
                wf.del();
            } catch (ErrMsgException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
        }
    }
}
