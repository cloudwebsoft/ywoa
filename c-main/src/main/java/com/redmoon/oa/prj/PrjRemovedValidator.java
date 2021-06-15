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
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.sms.SMSFactory;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-1-24下午04:12:48
 */
public class PrjRemovedValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PrjRemovedValidator.class.getName());

	public PrjRemovedValidator() {
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

		String prjId = fdao.getFieldValue("prj_id");
		
		String sql = "update form_table_prj set status='" + PrjConfig.STATUS_REMOVED + "' where id="+prjId;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			jt.executeUpdate(sql);
			
			// 更新督办中的状态
			sql = "update form_table_prj_taskcontrol set status='" + PrjConfig.STATUS_REMOVED + "' where prj_id=? and task_id=''";
			jt.executeUpdate(sql, new Object[]{prjId});
			
			FormDb fdPrj = new FormDb();
			fdPrj = fdPrj.getFormDb(PrjConfig.CODE_PRJ);
			com.redmoon.oa.visual.FormDAO vfdao = new com.redmoon.oa.visual.FormDAO();
			vfdao = vfdao.getFormDAO(StrUtil.toLong(prjId), fdPrj);
			
			// 置表单的状态为已放弃			
			vfdao.setCwsStatus(FormDAO.STATUS_DISCARD);
			vfdao.save();			
			
			String prjTitle = vfdao.getFieldValue("prj_name");
			String prjManager = vfdao.getFieldValue("prj_manager");

	        PrjConfig pc = PrjConfig.getInstance();
			String t = pc.getProperty("prjRemovedMsgTitle");
			t = StrUtil.format(t, new Object[]{prjTitle});
			String c = pc.getProperty("prjRemovedMsgContent");
			c = StrUtil.format(c, new Object[]{prjTitle});
		    
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
	        boolean isToMobile = SMSFactory.isUseSMS();
	        if (!isToMobile)
	            md.sendSysMsg(prjManager, t, c);
	        else {
	            if (imsg != null)
	                imsg.sendSysMsg(prjManager, t, c);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// 撤销项目流程
		sql = "select flowId from form_table_prj where id="+prjId;
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				int flowId = rr.getInt(1);
				
				// 置项目流程的状态为已作废
				WorkflowDb wfPrj = new WorkflowDb();
				wfPrj = wfPrj.getWorkflowDb(flowId);
				wfPrj.setStatus(WorkflowDb.STATUS_DISCARDED);
				wfPrj.setCheckUserName(lastAction.getUserName());
				wfPrj.setResultValue(lastAction.getResultValue());
				wfPrj.setEndDate(new java.util.Date());
				wfPrj.save();
		        
		        // 置项目任务的状态为已作废，当未复命时status为空
		        sql = "select id from form_table_prj_task where prj_id=" + prjId + " and (status=" + StrUtil.sqlstr(PrjConfig.STATUS_DOING) + " or status='prj_status')";
	        	ri = jt.executeQuery(sql);
	        	while (ri.hasNext()) {
	        		rr = (ResultRecord)ri.next();
	        		int taskId = rr.getInt(1);
	        		
	        		PrjTaskRemovedValidator ptrv = new PrjTaskRemovedValidator();
	        		ptrv.removeTask(jt, taskId, lastAction.getUserName(), lastAction.getResultValue());
	        	}
		        
		        // 置被延迟提交的评价节点的状态为已放弃，以免被自动提交
		        sql = "select id from flow_action where status=" + WorkflowActionDb.STATE_DELAYED +
                	" and flow_id=" + flowId;
		        WorkflowActionDb wa = new WorkflowActionDb();
		        try {
		        	ri = jt.executeQuery(sql);
		        	if (ri.hasNext()) {
		        		rr = (ResultRecord)ri.next();
		        		wa = wa.getWorkflowActionDb(rr.getInt(1));
		        		wa.setStatus(WorkflowActionDb.STATE_DISCARDED);
		        		wa.save();
		        	}
		        }
		        catch (SQLException e) {
					e.printStackTrace();
		        }
			}
		}
	    catch (SQLException e) {
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
