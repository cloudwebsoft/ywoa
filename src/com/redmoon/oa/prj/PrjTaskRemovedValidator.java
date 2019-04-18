package com.redmoon.oa.prj;

import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.hr.LeaveFormValidator;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.worklog.WorkLogForModuleMgr;

/**
 * @Description:
 * @author:
 * @Date: 2016-1-24下午02:45:24
 */
public class PrjTaskRemovedValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PrjTaskRemovedValidator.class.getName());

	public PrjTaskRemovedValidator() {
	}

	@SuppressWarnings("unchecked")
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction)
			throws ErrMsgException {
		// 新建一个FormDb对象
		FormDb fd = new FormDb();
		// 新建一个Leaf对象
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		fd = fd.getFormDb(lf.getFormCode());
		// 新建一个FormD对象
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(wf.getId(), fd);

		int taskId = StrUtil.toInt(fdao.getFieldValue("task_id"), -1);
		
		JdbcTemplate jt = new JdbcTemplate();
		removeTask(jt, taskId, lastAction.getUserName(), lastAction.getResultValue());
		
		// 任务作废时要重新计算项目的进度
		int prjId = StrUtil.toInt(fdao.getFieldValue("prj_id"), -1);
		if (prjId!=-1) {
			WorkLogForModuleMgr wfmm = new WorkLogForModuleMgr();
			wfmm.calcuPrjProgress(jt, prjId);
		}		
	}
	
	public void removeTask(JdbcTemplate jt, int taskId, String lastActionUser, int lastActionResult) throws ErrMsgException {
		String sql = "update form_table_prj_task set status='" + PrjConfig.STATUS_REMOVED + "' where id="+taskId;
		try {
			jt.executeUpdate(sql);
			
			// 更新督办中的状态
			sql = "update form_table_prj_taskcontrol set status='" + PrjConfig.STATUS_REMOVED + "' where task_id=?";
			jt.executeUpdate(sql, new Object[]{taskId});	

			FormDb fdTask = new FormDb();
			fdTask = fdTask.getFormDb(PrjConfig.CODE_TASK);
			com.redmoon.oa.visual.FormDAO vfdao = new com.redmoon.oa.visual.FormDAO();
			vfdao = vfdao.getFormDAO(taskId, fdTask);
			
			// 置表单的状态为已放弃
			vfdao.setCwsStatus(FormDAO.STATUS_DISCARD);
			vfdao.save();			
			
			// 提醒任务的执行人，任务已作废			
			String taskTitle = vfdao.getFieldValue("task_name");
			String zrr = vfdao.getFieldValue("zrr");

	        PrjConfig pc = PrjConfig.getInstance();
			String t = pc.getProperty("taskRemovedMsgTitle");
			t = StrUtil.format(t, new Object[]{taskTitle});
			String c = pc.getProperty("taskRemovedMsgContent");
			c = StrUtil.format(c, new Object[]{taskTitle});
		    
	        IMessage imsg = null;
	        ProxyFactory proxyFactory = new ProxyFactory(
	                "com.redmoon.oa.message.MessageDb");
	        Advisor adv = new Advisor();
	        MobileAfterAdvice mba = new MobileAfterAdvice();
	        adv.setAdvice(mba);
	        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
	        proxyFactory.addAdvisor(adv);
	        imsg = (IMessage) proxyFactory.getProxy();
	        MessageDb md = new MessageDb();
	        // 是否发送短信
	        if (!SMSFactory.isUseSMS())
	            md.sendSysMsg(zrr, t, c);
	        else {
	            if (imsg != null)
	                imsg.sendSysMsg(zrr, t, c);
	        }		    			
	        
			// 撤销任务流程
			sql = "select flowId from form_table_prj_task where id="+taskId;
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				int flowId = rr.getInt(1);
				
        		// 置任务的流程状态为已放弃				
				WorkflowDb wfTask = new WorkflowDb();
				wfTask = wfTask.getWorkflowDb(flowId);
		        wfTask.setStatus(WorkflowDb.STATUS_DISCARDED);
		        wfTask.setCheckUserName(lastActionUser);
		        wfTask.setResultValue(lastActionResult);
		        wfTask.setEndDate(new java.util.Date());
		        wfTask.save();
		        
		        // 流程的待办记录需忽略
		        MyActionDb mad = new MyActionDb();
		        mad.onDiscard(flowId);
		        
		        // 置被延迟提交的评价节点的状态为已放弃，以免被自动提交
		        sql = "select id from flow_action where status=" + WorkflowActionDb.STATE_DELAYED +
                	" and flow_id=" + flowId;
		        WorkflowActionDb wa = new WorkflowActionDb();
		        ri = jt.executeQuery(sql);
		        if (ri.hasNext()) {
		        	rr = (ResultRecord)ri.next();
		        	wa = wa.getWorkflowActionDb(rr.getInt(1));
		        	wa.setStatus(WorkflowActionDb.STATE_DISCARDED);
		        	wa.save();
		        }
			}	        
		} catch (SQLException e) {
			e.printStackTrace();
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
	public boolean validate(HttpServletRequest arg0, FileUpload arg1, int arg2,
			Vector arg3) throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onActionFinished(HttpServletRequest arg0, int arg1,
			FileUpload arg2) {
		// TODO Auto-generated method stub

	}
}
