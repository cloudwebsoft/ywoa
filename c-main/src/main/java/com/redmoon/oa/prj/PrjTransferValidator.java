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
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.sms.SMSFactory;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-2-26下午02:54:16
 */
public class PrjTransferValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PrjTaskValidator.class.getName());

	public PrjTransferValidator() {
	}

	@SuppressWarnings("unchecked")
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction)
			throws ErrMsgException {
		if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
			FormDb fd = new FormDb();
			fd = fd.getFormDb("prj_transfer");
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(wf.getId(), fd);
			String manger_transfered = fdao.getFieldValue("manger_transfered");
			String prj_id = fdao.getFieldValue("prj_id");
			
			// 更新项目中对应的负责人
			fd = fd.getFormDb("prj");
			long prjId = StrUtil.toLong(prj_id, -1);
			com.redmoon.oa.visual.FormDAO dao = new com.redmoon.oa.visual.FormDAO();
			dao = dao.getFormDAO(prjId, fd);
			
			String prjTitle = dao.getFieldValue("prj_name");
			String prjManager = dao.getFieldValue("prj_manager");
			
			dao.setFieldValue("prj_manager", manger_transfered);
			dao.save();
			
			PrjConfig pc = PrjConfig.getInstance();
			String t = pc.getProperty("prjTransferMsgTitle");
			t = StrUtil.format(t, new Object[]{prjTitle, prjManager});
			String c = pc.getProperty("prjTransferMsgContent");
			c = StrUtil.format(c, new Object[]{prjTitle, prjManager});
		    
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
	            md.sendSysMsg(manger_transfered, t, c);
	        else {
	            if (imsg != null)
	                imsg.sendSysMsg(manger_transfered, t, c);
	        }					
			
			// 更新任务中对应的负责人
			String sql = "update form_table_prj_task set manager=" + StrUtil.sqlstr(manger_transfered) + " where prj_id=" + StrUtil.sqlstr(prj_id);
			JdbcTemplate jt = new JdbcTemplate();
			try {
				jt.executeUpdate(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// 更新督办中对应的负责人
			sql = "update form_table_prj_taskcontrol set person=" + StrUtil.sqlstr(manger_transfered) + " where prj_id=" + StrUtil.sqlstr(prj_id) + " and task_id=''";
			try {
				jt.executeUpdate(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			// 取得项目的任务安排流程
			sql = "select flowId from form_table_prj_task where prj_id=" + StrUtil.sqlstr(prj_id);
		    ResultIterator ri;
			WorkflowActionDb nextwa = new WorkflowActionDb();
			try {
				ri = jt.executeQuery(sql);
			    while (ri.hasNext()) {
			        ResultRecord rr = (ResultRecord) ri.next();
			        int flowId = rr.getInt(1);
			        
					// 更新正在复命的任务安排流程中的发起人节点，置其为新的项目负责人，因为审阅及评价节点是指向发起人
					sql = "select id from flow_action where title=" + StrUtil.sqlstr(PrjConfig.REPORT) + " and status=" + WorkflowActionDb.STATE_DOING + " and flow_id=?";
				    ResultIterator ri1 = jt.executeQuery(sql, new Object[] {new Integer(flowId)});
				    if (ri1.hasNext()) {
				    	WorkflowDb wf2 = new WorkflowDb();
				    	wf2 = wf2.getWorkflowDb(flowId);
				    	int actionId = wf2.getStartActionId();
				    	WorkflowActionDb wa = new WorkflowActionDb();
				    	wa = wa.getWorkflowActionDb(actionId);
				    	wa.setUserName(manger_transfered);
				    	wa.save();
				    }
			        
					// 已复命并延迟提交给评价人，则置评价人为新的项目负责人			
					sql = "select id from flow_action where status=" + WorkflowActionDb.STATE_DELAYED + " and flow_id=?";
				    ResultIterator ri2 = jt.executeQuery(sql, new Object[] {new Integer(flowId)});
				    if (ri2.hasNext()) {
				        ResultRecord rr2 = (ResultRecord) ri2.next();
				        nextwa = nextwa.getWorkflowActionDb(rr2.getInt(1));
				        // System.out.println(getClass() + " nextwa.getUserName()=" + nextwa.getUserName());
				        LogUtil.getLog(getClass()).info("nextwa.getUserName()=" + nextwa.getUserName());
				
				        try {
				        	nextwa.setUserName(manger_transfered);
				            nextwa.save();
				        } catch (ErrMsgException ex1) {
				            ex1.printStackTrace();
				            LogUtil.getLog(getClass()).error(StrUtil.trace(ex1));
				            return;
				        }
				    }
			    }				
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
