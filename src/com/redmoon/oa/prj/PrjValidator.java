package com.redmoon.oa.prj;

import java.sql.SQLException;
import java.util.ArrayList;
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
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.chat.Privilege;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-1-24下午03:40:53
 */
public class PrjValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PrjTaskValidator.class.getName());

	public PrjValidator() {
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
    		String sql = "update form_table_prj_taskcontrol set status='" + PrjConfig.STATUS_DONE + "' where prj_id=? and task_id=''";
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
	public boolean validate(HttpServletRequest request, FileUpload fileUpload,
            int flowId, Vector fields) throws ErrMsgException {
		// TODO Auto-generated method stub
		//yst 项目开始时间要小于结束时间
		String begindate = fileUpload.getFieldValue("prj_begindate");
		String enddate = fileUpload.getFieldValue("prj_enddate");
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
	
		String strActionId = fileUpload.getFieldValue("actionId");
    	int actionId = StrUtil.toInt(strActionId);
    	WorkflowActionDb wa = new WorkflowActionDb();
    	wa = wa.getWorkflowActionDb(actionId);
    	
    	if (!wa.getTitle().equals(PrjConfig.REPORT))
    		return true;
		
    	// 当为复命节点时，才需处理
		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
		
    	WorkflowDb wf = new WorkflowDb();
    	wf = wf.getWorkflowDb(flowId);

    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(wf.getTypeCode()); // wf为内置变量，可以直接引用

    	FormDb fdScript = new FormDb();
    	fdScript = fdScript.getFormDb(lf.getFormCode());
    	
		boolean ret = true;
		com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
		fdao = fdao.getFormDAO(flowId, fdScript);
    	String sql = "select zrr from form_table_prj_task where cws_id=" + fdao.getId();
    	JdbcTemplate jt = new JdbcTemplate();    	
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
	    	if (ri.size()==1) {
		    	if (ri.hasNext()) {
		    		ResultRecord rr = (ResultRecord)ri.next();
		    	    String taskManager = rr.getString("zrr");
		    	    if (userName.equals(taskManager)) {
		    	    	ret = false;
		    	    	throw new ErrMsgException("项目仅需本人完成时，不需要再填写明细表！");
		    	    }
		    	}    	
	    	}
	    	else {
	    	    ret = true;
	    	}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}

	@Override
    public void onActionFinished(HttpServletRequest request, int flowId, FileUpload fu) {
		WorkflowDb wf = new WorkflowDb();
    	wf = wf.getWorkflowDb(flowId);

    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(wf.getTypeCode()); // wf为内置变量，可以直接引用

    	FormDb fdScript = new FormDb();
    	fdScript = fdScript.getFormDb(lf.getFormCode());

    	// 取出当前流程的表单记录
    	FormDAO fdaoScript = new FormDAO();
    	fdaoScript = fdaoScript.getFormDAO(wf.getId(), fdScript);    	
    	
    	// String prjName = fdaoScript.getFieldValue("prj_name");
    	String prjManager = fdaoScript.getFieldValue("prj_manager");
    	
    	long fdaoScriptId = fdaoScript.getId();

    	String strActionId = fu.getFieldValue("actionId");
    	int actionId = StrUtil.toInt(strActionId);
    	WorkflowActionDb wa = new WorkflowActionDb();
    	wa = wa.getWorkflowActionDb(actionId);
    	
    	if (wa.getTitle().equals(PrjConfig.ARRANGE)) {
    		// 回写下达时间
	    	String sql = "update form_table_prj set deliver_time=? where id=" + fdaoScriptId;
	    	JdbcTemplate jt = new JdbcTemplate();
	    	try {
	    		int rows = jt.executeUpdate(sql, new Object[]{new java.util.Date()});
	    	} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
    	    // 生成项目的督办记录
    	    if ("是".equals(fdaoScript.getFieldValue("supervision"))) {
        	    FormDb fdDb = new FormDb();
        	    fdDb = fdDb.getFormDb("prj_taskcontrol");
        	    com.redmoon.oa.visual.FormDAO fdaoDb = new com.redmoon.oa.visual.FormDAO(fdDb);
        	    fdaoDb.setFieldValue("begin_time", fdaoScript.getFieldValue("prj_begindate"));
        	    fdaoDb.setFieldValue("content", fdaoScript.getFieldValue("prj_name"));
        	    fdaoDb.setFieldValue("person", fdaoScript.getFieldValue("prj_manager"));
        	    fdaoDb.setFieldValue("pre_time", fdaoScript.getFieldValue("prj_enddate"));
        	    fdaoDb.setFieldValue("progress", "0");
        	    fdaoDb.setFieldValue("prj_id", String.valueOf(fdaoScriptId));
        	    fdaoDb.setFieldValue("task_id", "");
        	    fdaoDb.setFieldValue("item_type", PrjConfig.PRJ);
        	    fdaoDb.setUnitCode(DeptDb.ROOTCODE);
        	    fdaoDb.setFieldValue("status", PrjConfig.STATUS_DOING);
        	    fdaoDb.create();
    	    }	    	
    	}
    	else if (wa.getTitle().equals(PrjConfig.REPORT)) { // 复命
        	String sql = "update form_table_prj set reply_time =? where id=" + fdaoScriptId;
        	JdbcTemplate jt = new JdbcTemplate();
        	try {
				int rows = jt.executeUpdate(sql, new Object[]{new java.util.Date()});
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	// 遍历明细表，cws_id中存储的是流程主表单的id
        	
            MyActionDb mad = new MyActionDb();
            WorkflowMgr wm = new WorkflowMgr();
            WorkflowDb wfTask = new WorkflowDb();
        	
            // 任务安排流程编码
            // String flowCode = "14525015433122138669";
            Leaf lfTask = new Leaf();
            // lfTask = lfTask.getLeaf(flowCode);
            lfTask = lfTask.getLeafByName("任务安排");
            if (lfTask==null) {
            	Logger.getLogger(getClass()).error("onActionFinished:流程名称必须为：任务安排");
            }
            String flowCode = lfTask.getCode();
            
        	FormDb fd = new FormDb();
        	fd = fd.getFormDb(lfTask.getFormCode());
        	
            // 回写任务表单中的项目ID（查询字段选择控件）
    	    // sql = "update form_table_prj_task set prj_id=? where cws_id=" + fdaoScriptId;
    	    // jt.executeUpdate(sql, new Object[]{new Long(fdaoScript.getId())});

    	    FormDb fdDb = new FormDb();
    	    fdDb = fdDb.getFormDb("prj_taskcontrol");
    	    
        	sql = "select id, zrr,task_name,task_begintime,task_endtime,work_load,supervision,why,point from form_table_prj_task where cws_id=" + fdaoScriptId;
        	ResultIterator ri;
			try {
				ri = jt.executeQuery(sql);
	        	while (ri.hasNext()) {
	        		ResultRecord rr = (ResultRecord)ri.next();
	        		
	        		int taskId = rr.getInt(1);
	        	    String taskManager = rr.getString("zrr"); // 执行人

	        	    String brief = rr.getString("task_name");
	        	    String task_begin_time = DateUtil.format(rr.getDate("task_begintime"), "yyyy-MM-dd HH:mm:ss");
	        	    String task_end_time = DateUtil.format(rr.getDate("task_endtime"), "yyyy-MM-dd HH:mm:ss");
	        	    String work_load = rr.getString("work_load");
	        	    String supervision = rr.getString("supervision");
	        	    // String why = rr.getString("why");
	        	    // String point = rr.getString("point");
	        	    
	        	    // 生成任务的督办记录
	        	    if ("是".equals(supervision)) {
		        	    com.redmoon.oa.visual.FormDAO fdaoDb = new com.redmoon.oa.visual.FormDAO(fdDb);
		        	    fdaoDb.setFieldValue("begin_time", task_begin_time);
		        	    fdaoDb.setFieldValue("content", brief);
		        	    fdaoDb.setFieldValue("person", taskManager);
		        	    fdaoDb.setFieldValue("pre_time", task_end_time);
		        	    fdaoDb.setFieldValue("progress", "0");
		        	    fdaoDb.setFieldValue("work_load", work_load);
		        	    fdaoDb.setFieldValue("prj_id", String.valueOf(fdaoScriptId));
		        	    fdaoDb.setFieldValue("task_id", String.valueOf(rr.getInt("id")));
	            	    fdaoDb.setUnitCode(DeptDb.ROOTCODE);	
	            	    fdaoDb.setFieldValue("item_type", PrjConfig.PRJ_TASK);	
	            	    fdaoDb.setFieldValue("status", PrjConfig.STATUS_DOING);
		        	    fdaoDb.create();
	        	    }
	        	    	        
	        	    // 生成任务安排流程
	                long myActionId = -1;
	                try {
	                	String flowTitle = WorkflowMgr.makeTitle(null, prjManager, lfTask, true);
	                	
	                	myActionId = wm.initWorkflow(prjManager, flowCode, flowTitle, -1, WorkflowDb.LEVEL_NORMAL);
	            		
	                	mad = mad.getMyActionDb(myActionId);
	                    wfTask = wfTask.getWorkflowDb((int)mad.getFlowId());
	                    wfTask.setStatus(WorkflowDb.STATUS_STARTED);
	                    wfTask.save();
	                    
	                    FormDAO fdao = new FormDAO();
	                    fdao = fdao.getFormDAO((int)mad.getFlowId(), fd);
	                    // 删除发起流程时自动生成的表单
	                    fdao.del();
	                    
	                    // 更新原嵌套表中的flowId，使之成为新发起流程的表单
	                    // sql = "update form_table_prj_task set flowId=" + mad.getFlowId() + " where cws_id=" + fdaoScriptId + " and zrr=" + StrUtil.sqlstr(taskManager);
	                    sql = "update form_table_prj_task set flowId=" + mad.getFlowId() + " where id=" + taskId;
	                    jt.executeUpdate(sql);
	                    
	                    fdao = fdao.getFormDAO((int)mad.getFlowId(), fd);
 	                    fdao.setFieldValue("prj_id", String.valueOf(fdaoScriptId));
 	                    fdao.setFieldValue("manager", prjManager);
 	 	                fdao.setFieldValue("deliver_time", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
 	 	                fdao.setStatus(FormDAO.STATUS_NOT);
 	 	                fdao.save();
	                    
/*
 * 	                    // 填充任务单中的数据
 * 	                    fdao.setFieldValue("prj_id", String.valueOf(fdaoScriptId));
	                    fdao.setFieldValue("task_name", brief);
	                    fdao.setFieldValue("task_begintime", task_begin_time);
	                    fdao.setFieldValue("task_endtime", task_end_time);
	                    fdao.setFieldValue("work_load", work_load);
	                    fdao.setFieldValue("supervision", supervision);
	                    fdao.setFieldValue("manager", prjManager); // 项目负责人
	                    fdao.setFieldValue("zrr", taskManager);
	                    fdao.setFieldValue("why", why);
	                    fdao.setFieldValue("point", point);
	                    fdao.setFieldValue("deliver_time", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
	                    fdao.setStatus(FormDAO.STATUS_NOT);
	                    fdao.save();*/
	                    
	                    // 置下一节点的用户为受命人
	                    WorkflowActionDb waStart = new WorkflowActionDb();
	                    waStart = waStart.getWorkflowActionDb(wfTask.getStartActionId());
	                    
	                    Vector v = waStart.getLinkToActions();
	                    if (v.size() == 1) {
	                        Iterator ir = v.iterator();
	                        ArrayList selectedActions = new ArrayList();
	                        while (ir.hasNext()) {
	                        	WorkflowActionDb nextwa = (WorkflowActionDb) ir.next();
	                            nextwa = new WorkflowActionDb(nextwa.getId());
	                            nextwa.setUserName(taskManager);
	                            nextwa.save();
	                        }
	                    }
	                    
	                    // 自动提交至下个节点
	                    StringBuffer sb = new StringBuffer();
	                    wm.finishActionSingle(null, mad, prjManager, sb);
	                    
	                } catch (ErrMsgException e) {
	                    LogUtil.getLog("onActionFinished: ").error("execute:" + e.getMessage());
	                    e.printStackTrace();
	                }  	 
	        	}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}		
	}
}
