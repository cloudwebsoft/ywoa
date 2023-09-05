package com.redmoon.oa.hr;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ParamConfig;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;

public class TrainFlow {
	public boolean execute(HttpServletRequest request) throws ErrMsgException {
        String flowCode = "pxdd";
        Leaf lf = new Leaf();
        lf = lf.getLeaf(flowCode);

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(flowCode);
        boolean isPredefined = wpd != null && wpd.isLoaded();
        if (!isPredefined) {
            LogUtil.getLog(getClass()).error(lf.getName() + " 预定义流程不存在！");
            return false;
        }
        String toSender = ParamUtil.get(request, "users");
        String pxfam = ParamUtil.get(request, "pxfam");
        String pxrs = ParamUtil.get(request, "pxrs");
        String kssj = ParamUtil.get(request, "kssj");
        String jssj = ParamUtil.get(request, "jssj");
        String pxmd = ParamUtil.get(request, "pxmd");
        String pxks = ParamUtil.get(request, "pxks");
        String pxch = ParamUtil.get(request, "pxch");
        String pxjs = ParamUtil.get(request, "pxjs");
        String pxcl = ParamUtil.get(request, "pxcl");
        String pxyq = ParamUtil.get(request, "pxyq");
        String person_no = "";
        String sql ="";
        WorkflowMgr wm = new WorkflowMgr();
        boolean re = true;
        
        long myActionId = -1;
        Vector v = new Vector();
        String arrSender[] = null;
        if(toSender.indexOf(",")!=-1){
        	arrSender = toSender.split(",");
        	for(int i = 0;i<arrSender.length;i++){
        		v.addElement(arrSender[i]);
        	}
        }else{
        	v.addElement(toSender);
        }
        if (v.size() == 0) {
            LogUtil.getLog(getClass()).error(lf.getName() +
                                             " 预定义流程中的发起者无效，不能用于调度！");
            return false;
        }
        int noticeId = 0;
        WorkflowDb wf = new WorkflowDb();
        MyActionDb mad = new MyActionDb();
        UserDb ud = new UserDb();
        Iterator ir = v.iterator();
        TrainNoticeDb tnd = new TrainNoticeDb();
        String formCode = "train_notice_create";
        ParamConfig pc = new ParamConfig(tnd.getTable().
                                         getFormValidatorFile());
        ParamChecker pck = new ParamChecker(request);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        try {
            re = tnd.create(pck);
        } catch (ResKeyException rsKeyException) {
            throw new ErrMsgException(rsKeyException.getMessage(request));
        }
		
        String sqr = "select id from oa_hr_train_notice order by id desc";
		JdbcTemplate jte = new JdbcTemplate();
		ResultIterator ite = null;
		try {
			ite = jte.executeQuery(sqr);
		} catch (SQLException ex) {
			// throw ex;
			LogUtil.getLog(getClass()).error(ex);
		}
		if (ite.hasNext()) {
			ResultRecord rre = (ResultRecord) ite.next();
			noticeId = rre.getInt("id");
		}
        while (ir.hasNext()) {
            String userName = (String) ir.next();
            ud = ud.getUserDb(userName);
            
            myActionId = -1;
            try {
            	myActionId = (int)wm.initWorkflow(ud.getName(), flowCode, lf.getName(), -1, WorkflowDb.LEVEL_NORMAL);
                
        		mad = mad.getMyActionDb(myActionId);
        		
        		wf = wf.getWorkflowDb((int)mad.getFlowId());
        		wf.setStatus(WorkflowDb.STATUS_NOT_STARTED);
        		wf.save();
        		
                FormDb fd = new FormDb();
        		fd = fd.getFormDb("peixunbd");
        		FormDAO fdao = new FormDAO();
        		fdao = fdao.getFormDAO((int)mad.getFlowId(), fd);
        		fdao.setFieldValue("pxfam",pxfam);
        		fdao.setFieldValue("pxrs",pxrs);
        		fdao.setFieldValue("pxdx",ud.getName());
        		fdao.setFieldValue("kssj",kssj);
        		fdao.setFieldValue("jssj", jssj);
        		fdao.setFieldValue("pxks",pxks);
        		fdao.setFieldValue("pxmd",pxmd);
        		fdao.setFieldValue("pxch",pxch);
        		fdao.setFieldValue("pxjs", pxjs);
        		fdao.setFieldValue("pxcl",pxcl);
        		fdao.setFieldValue("pxyq", pxyq);
        		fdao.setFieldValue("notice_id",noticeId+"" );
        		fdao.setFieldValue("person_no", ud.getPersonNo());
        		re = fdao.save();                
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("execute:" + e.getMessage());
            }
            
            // 发送消息通知
            boolean isToMobile = SMSFactory.isUseSms;
            // 发送信息
            MessageDb md = new MessageDb();
            String t = "系统调度：" + lf.getName();
            String c = "系统已自动为您发起流程：" + lf.getName() + " 请及时办理！";
            try {
                String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + myActionId;
                md.sendSysMsg(ud.getName(), t, c, action);

                if (isToMobile) {
                    IMsgUtil imu = SMSFactory.getMsgUtil();
                    if (imu != null) {
                        imu.send(ud, c, MessageDb.SENDER_SYSTEM);
                    }
                }
            }
            catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("execute2:" + e.getMessage());
            }
        }        
        
		return re;
    }
}
