package com.redmoon.oa.prj;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.pvg.Privilege;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-1-24下午03:25:43
 */
public class PrjTaskValidator  implements IFormValidator {

	Logger logger = Logger.getLogger(PrjTaskValidator.class.getName());

	public PrjTaskValidator() {
	}

	@SuppressWarnings("unchecked")
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction)
			throws ErrMsgException {
    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(wf.getTypeCode()); // wf为内置变量，可以直接引用

    	FormDb fdScript = new FormDb();
    	fdScript = fdScript.getFormDb(lf.getFormCode());

    	// 取出当前流程的表单记录
    	FormDAO fdaoScript = new FormDAO();
    	fdaoScript = fdaoScript.getFormDAO(wf.getId(), fdScript);  

    	long fdaoScriptId = fdaoScript.getId();
    	
    	if (fdaoScript.getCwsId() == null || fdaoScript.getCwsId().equals("")) {
    		String sql = "update form_table_prj_taskcontrol set status='" + PrjConfig.STATUS_DONE + "' where task_id=?";
	    	JdbcTemplate jt = new JdbcTemplate();
	    	try {
				jt.executeUpdate(sql, new Object[]{fdaoScriptId});
	    	} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
	}

	@Override
	public void setExtraData(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getExtraData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsed() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setIsUsed(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean validate(HttpServletRequest request, FileUpload fileUpload, int flowId,
			Vector fields) throws ErrMsgException {
		// TODO Auto-generated method stub
		//yst 任务开始时间要小于结束时间
		String begindate = fileUpload.getFieldValue("task_begintime");
		String enddate = fileUpload.getFieldValue("task_endtime");
		if (begindate == null || begindate.equals("")) {
			throw new ErrMsgException("开始时间不能为空，请检查");
		}
		if (enddate == null || enddate.equals("")) {
			throw new ErrMsgException("开始时间不能为空，请检查");
		}
		if (DateUtil.parse(begindate, "yyyy-MM-dd HH:mm:ss").after(
			DateUtil.parse(enddate, "yyyy-MM-dd HH:mm:ss"))) {
			throw new ErrMsgException("结束时间小于开始时间，请检查！");
		}
		return true;
	}

	@Override
    public void onActionFinished(HttpServletRequest request, int flowId, FileUpload fu) {
		// TODO Auto-generated method stub
    	WorkflowDb wf = new WorkflowDb();
    	wf = wf.getWorkflowDb(flowId);

    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(wf.getTypeCode()); // wf为内置变量，可以直接引用

    	FormDb fdScript = new FormDb();
    	fdScript = fdScript.getFormDb(lf.getFormCode());

    	// 取出当前流程的表单记录
    	FormDAO fdaoScript = new FormDAO();
    	fdaoScript = fdaoScript.getFormDAO(wf.getId(), fdScript);  

    	long fdaoScriptId = fdaoScript.getId();
    	    	
    	if (fdaoScript.getCwsId() == null || fdaoScript.getCwsId().equals("")) {
    		String cwsId = StrUtil.getNullStr(fu.getFieldValue("prj_id"));
    		String sql = "update form_table_prj_task set cws_creator=?,cws_id=? where id=" + fdaoScriptId;
	    	JdbcTemplate jt = new JdbcTemplate();
	    	try {
	    		int rows = jt.executeUpdate(sql, new Object[]{new Privilege().getUser(request), cwsId});
	    	} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	String strActionId = fu.getFieldValue("actionId");
    	int actionId = StrUtil.toInt(strActionId);
    	WorkflowActionDb wa = new WorkflowActionDb();
    	wa = wa.getWorkflowActionDb(actionId);
    	
    	if (wa.getTitle().equals(PrjConfig.ARRANGE)) {
    		// 回写下达时间
	    	String sql = "update form_table_prj_task set deliver_time=? where id=" + fdaoScriptId;
	    	JdbcTemplate jt = new JdbcTemplate();
	    	try {
	    		int rows = jt.executeUpdate(sql, new Object[]{new java.util.Date()});
	    	} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if (wa.getTitle().equals(PrjConfig.REPORT)) {
    		// 回写复命时间
	    	String sql = "update form_table_prj_task set reply_time=? where id=" + fdaoScriptId;
	    	JdbcTemplate jt = new JdbcTemplate();
	    	try {
				int rows = jt.executeUpdate(sql, new Object[]{new java.util.Date()});
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String supervision = fdaoScript.getFieldValue("supervision");
			String prj_id = StrUtil.getNullStr(fdaoScript.getFieldValue("prj_id"));
			
    	    // 生成督办记录
    	    if ("".equals(prj_id) && "是".equals(supervision)) {
        	    FormDb fdDb = new FormDb();
        	    fdDb = fdDb.getFormDb("prj_taskcontrol");
        	    
        	    com.redmoon.oa.visual.FormDAO fdaoDb = new com.redmoon.oa.visual.FormDAO(fdDb);
        	    fdaoDb.setFieldValue("begin_time", fdaoScript.getFieldValue("task_begintime"));
        	    fdaoDb.setFieldValue("content", fdaoScript.getFieldValue("task_name"));
        	    fdaoDb.setFieldValue("person", fdaoScript.getFieldValue("zzr"));
        	    fdaoDb.setFieldValue("pre_time", fdaoScript.getFieldValue("task_endtime"));
        	    fdaoDb.setFieldValue("progress", "0");
        	    fdaoDb.setFieldValue("work_load", fdaoScript.getFieldValue("work_load"));
        	    fdaoDb.setFieldValue("prj_id", fdaoScript.getFieldValue("prj_id"));
        	    fdaoDb.setFieldValue("task_id", String.valueOf(fdaoScriptId));
        	    fdaoDb.setUnitCode(DeptDb.ROOTCODE);	                   
        	    fdaoDb.setFieldValue("item_type", PrjConfig.PRJ_TASK);   
        	    fdaoDb.setFieldValue("status", PrjConfig.STATUS_DOING);     	    
        	    fdaoDb.create();
    	    }			
    	}

	}
}
