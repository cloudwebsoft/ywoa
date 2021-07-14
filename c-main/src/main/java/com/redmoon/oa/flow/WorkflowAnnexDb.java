package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import java.util.Iterator;

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



public class WorkflowAnnexDb extends QObjectDb {
	
    public WorkflowAnnexDb() {
        super();
    }

    public boolean create(JdbcTemplate jt, ParamChecker paramChecker) throws
            ResKeyException, ErrMsgException {
        boolean re = super.create(jt, paramChecker);
        return re;
       
    }
    
    public boolean create(JdbcTemplate jt,Object[] params){
    	boolean re = false;
		try {
			re = super.create(jt, params);
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(WorkflowAnnexMgr.class).error(e.getMessage());
		}
    	return re;
    }

    public void writeAttachment(HttpServletRequest request, FileUpload fu, long annexId) throws ErrMsgException {
       if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            Iterator ir = v.iterator();
            Calendar cal = Calendar.getInstance();
            String year = "" + (cal.get(Calendar.YEAR));
            String month = "" + (cal.get(Calendar.MONTH) + 1);
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            String vpath = cfg.get("file_flow") + "/" + year + "/" + month + "/";
            // 置保存路径
            String filepath = Global.getRealPath() + vpath;
            // 置路径
            fu.setSavePath(filepath);
            while (ir.hasNext()) {
                FileInfo fi = (FileInfo) ir.next();

                // 使用随机名称写入磁盘
                fi.write(fu.getSavePath(), true);

                WorkflowAnnexAttachment wfaa = new WorkflowAnnexAttachment();
                wfaa.setAnnexId(annexId);
                String visualPath = year + "/" + month;
                wfaa.setVisualPath(visualPath);
                wfaa.setName(fi.getName());
                wfaa.setDiskName(fi.getDiskName());
                wfaa.setOrders(0);
                wfaa.setSize(fi.getSize());
                wfaa.create();
            }
        }
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = false;
        re = super.del(jt);
        if (re) {
            WorkflowAnnexAttachment wfaa = new WorkflowAnnexAttachment();
            wfaa.delAttachments(getLong("id"));
        }
        return re;
    }

    public Vector<WorkflowAnnexDb> listRoot(int flowId, String userName) {
    	JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id from flow_annex where flow_id=" + flowId + " and parent_id=-1 and (user_name=" + StrUtil.sqlstr(userName) + " or reply_name=" + StrUtil.sqlstr(userName) + " or is_secret=0) order by add_date asc";

		Vector<WorkflowAnnexDb> v = new Vector<WorkflowAnnexDb>();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int id = rr.getInt(1);
				WorkflowAnnexDb afad = new WorkflowAnnexDb();
				afad = (WorkflowAnnexDb) afad.getQObjectDb(id);
				v.add(afad);
			}
		} catch (SQLException e) {
			Logger.getLogger(getClass()).error(e.getMessage());
		}
		
		return v;
    }
    
    public Vector<WorkflowAnnexDb> listChildren(int parentId, String userName) {
    	JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id from flow_annex where parent_id=" + parentId + " and (user_name=" + StrUtil.sqlstr(userName) + " or reply_name=" + StrUtil.sqlstr(userName) + " or is_secret=0) order by add_date desc";

		Vector<WorkflowAnnexDb> v = new Vector<WorkflowAnnexDb>();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int id = rr.getInt(1);
				WorkflowAnnexDb afad = new WorkflowAnnexDb();
				afad = (WorkflowAnnexDb) afad.getQObjectDb(id);
				v.add(afad);
			}
		} catch (SQLException e) {
			Logger.getLogger(getClass()).error(e.getMessage());
		}
		
		return v;
    }
}
