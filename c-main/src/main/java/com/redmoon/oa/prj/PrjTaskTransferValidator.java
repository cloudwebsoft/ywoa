package com.redmoon.oa.prj;

import java.sql.SQLException;
import java.util.Iterator;
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
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.sms.SMSFactory;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-2-26下午02:54:32
 */
public class PrjTaskTransferValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PrjTaskValidator.class.getName());

	public PrjTaskTransferValidator() {
	}

	@SuppressWarnings("unchecked")
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction)
			throws ErrMsgException {
		if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
			FormDb fd = new FormDb();
			fd = fd.getFormDb("prj_tasktransfer");
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(wf.getId(), fd);
			String zrr_transfered  = fdao.getFieldValue("zrr_transfered");
			String task_id = fdao.getFieldValue("task_id");
			
			// 更新任务中对应的执行人
			fd = fd.getFormDb("prj_task");
			long taskId = StrUtil.toLong(task_id, -1);
			com.redmoon.oa.visual.FormDAO dao = new com.redmoon.oa.visual.FormDAO();
			dao = dao.getFormDAO(taskId, fd);
			String oldZrr = dao.getFieldValue("zrr");
			String taskTitle = dao.getFieldValue("task_name");
			dao.setFieldValue("zrr", zrr_transfered);
			dao.save();
			
			PrjConfig pc = PrjConfig.getInstance();
			String t = pc.getProperty("taskTransferMsgTitle");
			t = StrUtil.format(t, new Object[]{taskTitle, oldZrr});
			String c = pc.getProperty("taskTransferMsgContent");
			c = StrUtil.format(c, new Object[]{taskTitle, oldZrr});
		    
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
	            md.sendSysMsg(zrr_transfered, t, c);
	        else {
	            if (imsg != null)
	                imsg.sendSysMsg(zrr_transfered, t, c);
	        }			
			
			
			// 如果任务正在复命中，则将正在处理的流程移交给新的执行人
			String sql = "select id from flow_action where title=" + StrUtil.sqlstr(PrjConfig.REPORT) + " and status=" + WorkflowActionDb.STATE_DOING + " and flow_id=?";
		    ResultIterator ri1;
			JdbcTemplate jt = new JdbcTemplate();			
			try {
				ri1 = jt.executeQuery(sql, new Object[] {new Long(dao.getFlowId())});
			    if (ri1.hasNext()) {
			    	ResultRecord rr = (ResultRecord)ri1.next();
			    	int actionId = rr.getInt(1);
			    	/*
			    	WorkflowActionDb wa = new WorkflowActionDb();
			    	wa = wa.getWorkflowActionDb(actionId);
			    	wa.setUserName(zrr_transfered);
			    	wa.save();
			    	*/
			    	
			    	sql = "select id from flow_my_action where flow_id=" + dao.getFlowId() + " and action_id=" + actionId;
			        MyActionDb mad = new MyActionDb();
			        WorkflowMgr wm = new WorkflowMgr();
			        Vector v = mad.list(sql);
			        Iterator ir = v.iterator();
			        while (ir.hasNext()) {
			            mad = (MyActionDb) ir.next();
			            wm.handover(null, mad, zrr_transfered);
			        }
			    }
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// 更新督办中对应的执行人
			sql = "update form_table_prj_taskcontrol set person=" + StrUtil.sqlstr(zrr_transfered) + " where task_id=" + StrUtil.sqlstr(task_id);
			try {
				jt.executeUpdate(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
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
	public boolean validate(HttpServletRequest arg0, FileUpload arg1, int arg2,
			Vector arg3) throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
    public void onActionFinished(HttpServletRequest request, int flowId, FileUpload fu) {
		// TODO Auto-generated method stub
    	
	}
}