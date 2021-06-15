package com.redmoon.oa.worklog;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.prj.PrjConfig;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;

public class WorkLogForModuleMgr {
	
	public void updatePrj(String code,int workLogId,int prjId,int progress, String logType, boolean isCreateWorkLog){
		if (isCreateWorkLog) {
			// 与创建的日报记录生成关联
			JdbcTemplate jt = new JdbcTemplate();		
			WorkLogForModuleDb wlmdb = new WorkLogForModuleDb();
			String sql = "select id from visual_module_worklog where form_code = "+StrUtil.sqlstr(code) +" and workLog_id ="+workLogId+" and cws_id="+prjId;
			try{
				ResultIterator ri = jt.executeQuery(sql);
				ResultRecord rr = null;
				if(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					int id = rr.getInt(1);
					wlmdb.save(jt,new Object[]{workLogId,code,prjId,id});		
				}else{
					wlmdb.create(jt,new Object[]{workLogId,code,prjId});
				}
			}catch(SQLException e){
				LogUtil.getLog(getClass()).error(e.getMessage() + StrUtil.trace(e));							
				e.printStackTrace();
			} catch (ResKeyException e2) {
				LogUtil.getLog(getClass()).error(e2.getMessage() + StrUtil.trace(e2));							
				e2.printStackTrace();
			}
		}
		
		FormDb fd = new FormDb(code);
		FormDAO fdao = new FormDAO(prjId,fd);  //prjId 这里是项目任务单Id
		int flowId = (int)fdao.getFlowId();

		int processOld = StrUtil.toInt(fdao.getFieldValue("prj_progress"), 0);
		
		if (processOld==progress) {
			return;
		}
		
		// 如果不是日报
		if (!logType.equals(ConstUtil.TYPE_DAY)) {
			return;
		}
		
		// 如果项目的进度已达100%
		if (processOld>=100) {
			LogUtil.getLog(getClass()).info("项目" + prjId + "的进度已达" + processOld + "！");
			return;
		}
		
			if(progress!=0){
				fdao.setFieldValue("prj_progress",String.valueOf(progress));
			}
			fdao.setFieldValue("status", progress == 100 ? PrjConfig.STATUS_DONE : PrjConfig.STATUS_DOING);
			fdao.setFieldValue("real_time", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));		
			try {
				fdao.save();
			} catch (ErrMsgException e1) {
				e1.printStackTrace();
			}

			String sql = "select id from form_table_prj_taskcontrol where prj_id=?";
			JdbcTemplate jt = new JdbcTemplate();			
			ResultIterator ri;
			try {
				ri = jt.executeQuery(sql, new Object[]{new Long(prjId)});
				if(ri.hasNext()){
					ResultRecord rr = (ResultRecord)ri.next();
					sql = "update form_table_prj_taskcontrol set progress=? where prj_id=?";
					jt.executeUpdate(sql, new Object[]{new Integer(progress), new Long(prjId)});
				}					
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// 如果进度等于100%，则不再调用，以免反复触发延迟接收流程
			// 当项目 进度达到100时，使延迟接收的流程提交
			if (progress==100) {
				if (flowId!=-1) {
					WorkflowActionDb wad = new WorkflowActionDb();
					wad = wad.getWorkflowActionDbByTitle(PrjConfig.CHECK, flowId);
					wad.setDateDelayed(new java.util.Date());
					try {
						wad.saveOnlyToDb();
					} catch (ErrMsgException e) {
						// TODO Auto-generated catch block
						LogUtil.getLog(getClass()).error("项目" + prjId + "的流程不存在！");
					}				
				}
				else {
					LogUtil.getLog(getClass()).error("项目" + prjId + "的流程不存在！");
				}
			}
	}
	
	/**
	 * 当提交日报时，更新任务的进度，同时更新任务所属项目的进度
	 * @Description: 
	 * @param code
	 * @param workLogId
	 * @param taskId
	 */
	public void updatePrjTask(String code,int workLogId,int taskId,int progress, String logType, boolean isCreateWorkLog){
		if (isCreateWorkLog) {
			// 与创建的日报记录生成关联
			WorkLogForModuleDb wlmdb = new WorkLogForModuleDb();
			JdbcTemplate jt = new JdbcTemplate();		
			String sql = "select id from visual_module_worklog where form_code = "+StrUtil.sqlstr(code) +" and workLog_id ="+workLogId+" and cws_id="+taskId;
			ResultIterator ri;
			try {
				ri = jt.executeQuery(sql);
				if(ri.hasNext()){
					ResultRecord rr = (ResultRecord)ri.next();
					int id = rr.getInt(1);
					wlmdb.save(jt,new Object[]{workLogId,code,taskId,id});		
				}else{
					wlmdb.create(jt,new Object[]{workLogId,code,taskId});
				}				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		FormDb fd = new FormDb(code);
		FormDAO fdao = new FormDAO(taskId,fd);  //taskId 这里是项目任务单Id
		
		int processOld = StrUtil.toInt(fdao.getFieldValue("task_progress"), 0);
		
		if (processOld==progress) {
			return;
		}
		
		// 如果不是日报
		if (!logType.equals(ConstUtil.TYPE_DAY)) {
			return;
		}
		
		// 如果项目的进度已达100%
		if (processOld>=100) {
			LogUtil.getLog(getClass()).info("任务" + taskId + "的进度已达" + processOld + "！");
			return;
		}
		
		// 如果修改的不是最后一个日报，则progress不变
		if (!isCreateWorkLog) {
			JdbcTemplate jt = new JdbcTemplate();			
			String sql = "select id from visual_module_worklog where form_code = "+StrUtil.sqlstr(code) +" and workLog_id>"+workLogId+" and cws_id="+taskId;
			ResultIterator ri;
			try {
				ri = jt.executeQuery(sql);
				if(ri.hasNext()){
					return;
				}
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 
		if(progress!=0){
			fdao.setFieldValue("task_progress", String.valueOf(progress));
		}
		if (progress==100) {
			fdao.setFieldValue("real_time", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
		}
		fdao.setFieldValue("status", progress == 100 ? PrjConfig.STATUS_DONE : PrjConfig.STATUS_DOING);
		try {
			fdao.save();
		} catch (ErrMsgException e1) {
			e1.printStackTrace();
		}
		
		// 当任务 进度达到100时，使延迟接收的任务分配流程提交
		if (progress==100) {
			WorkflowActionDb wad = new WorkflowActionDb();
			wad = wad.getWorkflowActionDbByTitle(PrjConfig.CHECK, (int)fdao.getFlowId());
			wad.setDateDelayed(new java.util.Date());
			try {
				wad.saveOnlyToDb();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error(e.getMessage() + StrUtil.trace(e));
			}
		}			
		
		try{
			String sql = "select prj_id from form_table_prj_task where id="+taskId;
			JdbcTemplate jt = new JdbcTemplate();			
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			int prjId = -1 ;  //cws_id 这里是项目单Id
			if(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				prjId = StrUtil.toInt(rr.getString(1), -1);
			}
			
			// double prjHour = 0;//得到项目单的总工作量
			
			LogUtil.getLog(getClass()).error("progress=" + progress + " task_id=" + taskId + "！");		
			
			
			// 更新督办中的任务进度
			sql = "select id from form_table_prj_taskcontrol where task_id=?";
			ri = jt.executeQuery(sql, new Object[]{taskId});
			if(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				sql = "update form_table_prj_taskcontrol set progress=? where task_id=?";
				jt.executeUpdate(sql, new Object[]{new Integer(progress), taskId});
			}			
			
			if (prjId!=-1) {
/*				// 得到项目单所分配的每一个项目任务的各自工作进度  然后换算得到新的项目单进度
				sql = "select work_load,task_progress from form_table_prj_task where prj_id = '"+ prjId + "' and status=" + StrUtil.sqlstr(PrjConfig.STATUS_DOING);
				ri = jt.executeQuery(sql);
				while(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					int taskHour = rr.getInt(1);
					int taskProgress = rr.getInt(2);
					
					double a = (double)taskHour;
					double c = (double)(taskProgress * (a / prjHour));
					prjProgress += (int)(c + 0.5);   //项目任务单的进度换算为项目单进度(四舍五入)
				}*/
				
				calcuPrjProgress(jt, prjId);
			}
		}catch(SQLException e){
			LogUtil.getLog(getClass()).error(e.getMessage() + StrUtil.trace(e));							
			e.printStackTrace();
		} catch (ErrMsgException e1) {
			LogUtil.getLog(getClass()).error(e1.getMessage() + StrUtil.trace(e1));			
			e1.printStackTrace(); 
		}
		
	}
	
	/**
	 * 计算项目的进度
	 * @Description: 
	 * @param jt
	 * @param prjId
	 * @return
	 * @throws ErrMsgException 
	 */
	public void calcuPrjProgress(JdbcTemplate jt, int prjId) throws ErrMsgException {
		double prjHour = 0;
		String sql = "select sum(work_load) from form_table_prj_task where prj_id = '"+ prjId + "' and (status=" + StrUtil.sqlstr(PrjConfig.STATUS_DOING) +"or status=" +StrUtil.sqlstr(PrjConfig.STATUS_DONE)+")";//yst 不仅是未完成的还包括已完成的。
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				prjHour = rr.getDouble(1);
			}			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		double prjProgress = 0;
		// 得到项目单所分配的每一个项目任务的各自工作进度  然后换算得到新的项目单进度
		sql = "select work_load,task_progress from form_table_prj_task where prj_id = '"+ prjId + "' and (status=" + StrUtil.sqlstr(PrjConfig.STATUS_DOING) +"or status=" +StrUtil.sqlstr(PrjConfig.STATUS_DONE)+")";//yst 不仅是未完成的还有已完成的。
		try {
			ri = jt.executeQuery(sql);
			while(ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				int taskHour = rr.getInt(1);
				int taskProgress = rr.getInt(2);
				
				double a = (double)taskHour;
				double c = (double)(taskProgress * (a / prjHour));
				prjProgress += c;   //项目任务单的进度换算为项目单进度(四舍五入)
			}
			
			if (prjProgress!=0) {
				prjProgress = (int)(prjProgress + 0.5);
			}
			
			if (prjProgress>100) {
				prjProgress = 100;
			}
			
			FormDb fd = new FormDb("prj");
			FormDAO fdao = new FormDAO(prjId, fd);
			// int prjProgressOld = StrUtil.toInt(fdao.getFieldValue("prj_progress"), -1);
			fdao.setFieldValue("prj_progress", String.valueOf((int)prjProgress));
			if (prjProgress>=100) { 
				fdao.setFieldValue("real_time", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
				fdao.setFieldValue("status", PrjConfig.STATUS_DONE);//yst 给项目的的状态变成已完成
			}
			long flowId = fdao.getFlowId();
			fdao.save();
			
			// 更新督办中的项目进度
			LogUtil.getLog(getClass()).error("prjId=" + prjId + " prjProgress is " + prjProgress + "!");
			
			sql = "select id from form_table_prj_taskcontrol where prj_id=? and task_id='' order by id asc";
			ri = jt.executeQuery(sql, new Object[]{String.valueOf(prjId)});
			if(ri.hasNext()){
				// LogUtil.getLog(getClass()).error("prjId=" + prjId + " in taskcontrol is found.");
				// rr = (ResultRecord)ri.next();
				sql = "update form_table_prj_taskcontrol set progress=? where prj_id=? and task_id=''";
				jt.executeUpdate(sql, new Object[]{new Integer((int)prjProgress), String.valueOf(prjId)});
			}
			// LogUtil.getLog(getClass()).error("prjProgress=" + prjProgress + " after update taskcontrol.");				
			
			// 当项目 进度达到100时，使延迟接收的项目交接流程提交
			if (prjProgress>=100) {
				if (flowId!=-1) {
					WorkflowActionDb wad = new WorkflowActionDb();
					wad = wad.getWorkflowActionDbByTitle(PrjConfig.CHECK, (int)flowId);
					if (wad==null) {
						LogUtil.getLog(getClass()).error("项目流程节点中的标题不是" + PrjConfig.CHECK + "！");
					}
					wad.setDateDelayed(new java.util.Date());
					wad.saveOnlyToDb();
				}
				else {
					LogUtil.getLog(getClass()).error("项目的流程不存在！");
				}
			}				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//此项目是否已经分配给多人，如果是多人，则不能填写项目 日报
	public boolean isShared(int prjId){
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select id from form_table_prj_task where prj_id ="+prjId;
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				re = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return re;
		
	}
	/**
	 * 
	 * @Description: yst 查看是否先复命在汇报
	 * @param userName
	 * @param flowId
	 * @return
	 */
		public boolean isCanReport(String userName, long flowId) {
			boolean re = false;
			String sql = "";
			JdbcTemplate jt = new JdbcTemplate();
			try {
				sql = "select is_checked from flow_action a,flow b,flow_my_action c where a.flow_id="+flowId+" and b.id=a.flow_id and c.flow_id=a.flow_id and (c.user_name='" + userName + "' or c.proxy='" + userName + "'" +
						") and b.status<>-10 and a.title='" + PrjConfig.REPORT + "' and c.action_id=a.id";
				ResultIterator ri = jt.executeQuery(sql);
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord) ri.next();
						int isCheck = rr.getInt("is_checked");
						if (isCheck == 1) {
							re = true;
						}
					}
			}
			catch(SQLException e){
				e.printStackTrace();
			} finally {
				jt.close();
			}
			return re;
		}
	//判断该项目（任务）的受命人（执行人）是否是当前用户，若不是则不给汇报
	public boolean isReport(String userName, String code , long id){
		/*Privilege pri = new Privilege();
		String userName = pri.getUser(request);
		String code = ParamUtil.get(request, "code");
		int id = ParamUtil.getInt(request, "id",0);*/
		boolean re = false;
		String sql = "";
		JdbcTemplate jt = new JdbcTemplate();
		try{
			if(code.equals(PrjConfig.CODE_PRJ)) {
				sql = "select prj_manager from form_table_prj where id = "+ id;
			}else if(code.equals(PrjConfig.CODE_TASK)) {
				sql = "select zrr from form_table_prj_task where id = "+ id;
			}
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			if(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				if(rr.getString(1).equals(userName)){
					re = true;
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return re;
	}
	
	//根据code，prjId 判断该项目或者该任务的负责人，以查看他们的汇报
	public String managerUserName(String code , long id){
		String sql = "";
		String userName = "";
		JdbcTemplate jt = new JdbcTemplate();
		try{
			if(code.equals(PrjConfig.CODE_PRJ)){
				sql = "select prj_manager from form_table_prj where id = "+ id;
			}else if(code.equals(PrjConfig.CODE_TASK)){
				sql = "select zrr from form_table_prj_task where id = "+ id;
			}else{
				return "";
			}
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			if(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				userName = rr.getString(1);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return userName;
	}
}
