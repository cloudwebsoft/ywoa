package com.redmoon.oa.workplan;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpp.MPPReader;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ParamConfig;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.base.QObjectMgr;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;

public class WorkPlanTaskMgr extends QObjectMgr {
	Logger logger = Logger.getLogger( WorkPlanTaskMgr.class.getName() );
	private FileUpload fileUpload;

	public FileUpload getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(FileUpload fileUpload) {
		this.fileUpload = fileUpload;
	}

	public boolean changeTask(HttpServletRequest request, JSONObject obj, WorkPlanTaskDb wptd, int orders)
			throws JSONException, ErrMsgException {
		// String code = obj.getString("code");
		String name = obj.getString("name");
		String strStatus = obj.getString("status");
		String start = "";
		try {
			start = obj.getString("start");
		} catch (JSONException e) {
			start = DateUtil.format(new Date(obj.getLong("start")), "yyyy-MM-dd HH:mm:ss");
		}
		String end = "";
		try {
			end = obj.getString("end");
		} catch (JSONException e) {
			end = DateUtil.format(new Date(obj.getLong("end")), "yyyy-MM-dd HH:mm:ss");
		}
		int duration = obj.getInt("duration");
		boolean startIsMilestone = obj.getBoolean("startIsMilestone");
		boolean endIsMilestone = obj.getBoolean("endIsMilestone");
		String resource = obj.getString("resource");
		int level = 0;
		try {
			level = StrUtil.toInt(obj.getString("level"));
		} catch (JSONException e) {
			level = obj.getInt("level");
		}

		int status = WorkPlanTaskDb.getStatusByDesc(strStatus);

		java.util.Date sDate = DateUtil.parse(start, "yyyy-MM-dd HH:mm:ss");
		java.util.Date eDate = DateUtil.parse(end, "yyyy-MM-dd HH:mm:ss");

		int progress = 0;
		try {
			progress = StrUtil.toInt(obj.getString("progress"));
		} catch (JSONException e) {
			progress = obj.getInt("progress");
		}
		
		String workplanRelated = obj.getString("workplanRelated");
		long longWorkplanRelated = StrUtil.toLong(workplanRelated, -1);
		int assess = 0;
		try {
			assess = StrUtil.toInt(obj.getString("assess"));
		} catch (JSONException e) {
			assess = obj.getInt("assess");
		}
		
		String reportFlowType = obj.getString("reportFlowType");
		if (reportFlowType.equals("''")){
			reportFlowType = "";
		}
		//sql注入校验
		Privilege privilege = new Privilege();
		try
		{
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "name", name, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "strStatus", strStatus, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "start", start, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "end", end, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "duration", String.valueOf(duration), getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "startIsMilestone", String.valueOf(startIsMilestone), getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "endIsMilestone", String.valueOf(endIsMilestone), getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "resource", resource, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "level", String.valueOf(level), getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "status", String.valueOf(status), getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "progress", String.valueOf(progress), getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "workplanRelated", workplanRelated, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "workplanRelated", workplanRelated, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "assess", String.valueOf(assess), getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "reportFlowType", reportFlowType, getClass().getName());
		}catch(Exception e)
		{
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		}
		
		String depends = "";
		if (obj.has("depends")) {
			try {
				depends = StrUtil.getNullStr(obj.getString("depends"));
			}
			catch (JSONException e) {
				depends = String.valueOf(obj.getLong("depends"));
			}
		}
		
		// name=?,level=?,status=?,start=?,end=?,duration=?,startIsMilestone=?,endIsMilestone=?,resource=?,resouce_type=?,progress=?,workplan_related=?,assess=?
		boolean re = false;
		try {
			String oldResource = wptd.getString("task_resource");
			re = wptd.save(new JdbcTemplate(), new Object[] {
					name,
					new Integer(level), new Integer(status), sDate, eDate,
					new Integer(duration),
					new Integer(startIsMilestone ? 1 : 0),
					new Integer(endIsMilestone ? 1 : 0), resource,
					new Integer(WorkPlanTaskDb.RESOURCE_TYPE_USER),
					new Integer(progress), new Long(longWorkplanRelated),
					new Integer(assess), new Integer(orders), reportFlowType, depends,
					new Long(wptd.getLong("id")) });

			if (level == 0) {
				WorkPlanDb wpd = new WorkPlanDb();
				wpd = wpd.getWorkPlanDb(wptd.getInt("work_plan_id"));
				// 根据第一条任务更新workplan
				wpd.setBeginDate(sDate);
				wpd.setEndDate(eDate);
				wpd.setProgress(progress);
				wpd.save();
			}

			// 如果更改了责任人，则检查该任务项参与人中，新责任人是否已存在，如果存在则不处理，如果不存在，则增加参与人
			if (!oldResource.equals(resource)) {
				WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
				//删除任务对应的原人力资源
				wptud.delByTaskAndUser(wptd.getLong("id"), oldResource);
				if (!wptud.isTaskUserExist(wptd.getLong("id"), resource)) {
					try {
						wptud.create(new JdbcTemplate(), new Object[] {
								new Long(wptd.getLong("id")), resource,
								new java.util.Date(), new Integer(100),
								new Integer(duration), new Integer(0) });
					} catch (ResKeyException ex) {
						ex.printStackTrace();
					}
				}
				sendMsg(wptd.getInt("work_plan_id"), oldResource, "任务修改", "您负责的任务【" + wptd.getString("name") + "】已修改,责任人改为【" + resource + "】");
				sendMsg(wptd.getInt("work_plan_id"), resource, "任务修改", "新增任务【" + wptd.getString("name") + "】，负责人为您，请关注！");
			}
			else
			{
				sendMsg(wptd.getInt("work_plan_id"), oldResource, "任务修改", "任务【" + wptd.getString("name") + "】已修改，请关注！");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return re;
	}

	/**
	 * 创建任务项，用于从任务项界面创建任务
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean create(HttpServletRequest request) throws ErrMsgException {
		// long workplanId = ParamUtil.getLong(request, "work_plan_id");

		boolean re = false;
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		ParamConfig pc = new ParamConfig(wptd.getTable().getFormValidatorFile());
		ParamChecker pck = new ParamChecker(request);

		try {
			pck.doCheck(pc.getFormRule("workplan_task_create"));
		} catch (CheckErrException e) {
			// 如果onError=exit，则会抛出异常
			throw new ErrMsgException(e.getMessage());
		}

		String code = RandomSecquenceCreator.getId(20);

		pck.setValue("code", "编码", code);

		int level = 1;
		int orders = 0;
		long parentTaskId = ParamUtil.getLong(request, "parentTaskId", -1);
		if (parentTaskId != -1) {
			WorkPlanTaskDb wptd4MaxOrder = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(parentTaskId));
			level = wptd4MaxOrder.getInt("task_level") + 1;
			orders = wptd4MaxOrder.getMaxOrdersOfTaskChildren(
					pck.getInt("work_plan_id"), parentTaskId) + 1;
		}
		pck.setValue("task_level", "缩进值", new Integer(level));
		pck.setValue("orders", "顺序号", new Integer(orders));

		// pck.setValue("status", "状态", WorkPlanTaskDb.STATUS_ACTIVE);

		pck.setValue("resource_type", "资源类别",
						WorkPlanTaskDb.RESOURCE_TYPE_USER);

		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect("workplan_assess");

		pck.setValue("assess", "评价", Integer.valueOf(sd.getDefaultValue()));

		java.util.Date start = pck.getDate("start_date");
		java.util.Date end = pck.getDate("end_date");
		// duration
		OACalendarDb acd = new OACalendarDb();
		// 因为不包含开始日期，所以开始日期需减一天
		int duration = acd.getWorkDayCountFromDb(DateUtil.addDate(start, -1),
				end);
		pck.setValue("duration", "工期", new Integer(duration));

		// insert into work_plan_task
		// (name,code,level,status,start,end,duration,startIsMilestone,endIsMilestone,resource,resouce_type,work_plan_id,progress,workplan_related,assess,orders)
		// values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)</create>

		try {
			if (parentTaskId != -1) {
				// 如果不是根节点，则需要将位于其后的任务的orders + 1  
				WorkPlanTaskDb wptd4Order = new WorkPlanTaskDb();
				String sql = "select id from " + wptd4Order.getTable().getName()
						+ " where work_plan_id=? and orders>=" + orders;
				Iterator ir = wptd4Order.list(sql, new Object[] { new Integer(pck.getInt("work_plan_id")) }).iterator();
				while (ir.hasNext()) {
					wptd4Order = (WorkPlanTaskDb) ir.next();
					wptd4Order.set("orders", new Integer(wptd4Order.getInt("orders") + 1));
					wptd4Order.save();
				}
			}
			re = wptd.create(pck);

			if (re) {
				
				try {
					refreshGantt(pck.getInt("work_plan_id"));
				} catch (JSONException ex) {
					ex.printStackTrace();
				}

				Privilege privilege = new Privilege();
				String reason = ParamUtil.get(request, "reason");
				WorkPlanLogDb.log(privilege.getUser(request), pck
						.getInt("work_plan_id"), reason);

				wptd = wptd.getTaskByOrders(pck.getInt("work_plan_id"), orders);

				// 创建任务用户
				String resource = StrUtil.getNullStr(pck
						.getString("task_resource"));
				if (!resource.equals("")) {
					WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
					wptud.create(new JdbcTemplate(), new Object[] {
							new Long(wptd.getLong("id")), resource,
							new java.util.Date(), new Integer(100),
							new Integer(duration), new Integer(0) });
				}
				
				if(parentTaskId != -1){
					//如果不是根节点，获得这个节点的父节点，然后获得它所有的子节点，把顺序反排
					//获得本节点子节点开始和结束的orders
					int beginOrders = 0;
					int endOrders = -1;
					WorkPlanTaskDb wptdparent = new WorkPlanTaskDb();
					wptdparent = (WorkPlanTaskDb)wptdparent.getQObjectDb(parentTaskId);
					beginOrders = wptdparent.getInt("orders");
					String sql = "select id from "+wptdparent.getTable().getName()
							+ " where work_plan_id = ? and orders > ? and task_level <= ? order by orders asc";
					Iterator ir = wptdparent.list(sql, new Object[] { new Integer(wptdparent.getInt("work_plan_id")),new Integer(beginOrders),new Integer(wptdparent.getInt("task_level")) }).iterator();
					if(ir.hasNext()){
						WorkPlanTaskDb wptdBrother = (WorkPlanTaskDb)ir.next();
						endOrders = wptdBrother.getInt("orders");
					}
					if(endOrders==-1){
						sql = "select id from "+wptdparent.getTable().getName()
						+ " where work_plan_id = ? and orders > ? order by orders desc";
						ir = wptdparent.list(sql, new Object[] { new Integer(wptdparent.getInt("work_plan_id")),new Integer(beginOrders) }).iterator();
						if(ir.hasNext()){
							WorkPlanTaskDb wptdBrother = (WorkPlanTaskDb)ir.next();
							endOrders = wptdBrother.getInt("orders")+1;
						}
					}
					if(endOrders>beginOrders){
						sql = "select id from "+wptdparent.getTable().getName()
							+ " where work_plan_id = ? and orders > ? and orders <? order by orders asc";
						WorkPlanTaskDb wptdchild = new WorkPlanTaskDb();
						Vector vchild = wptdchild.list(sql,new Object[]{new Integer(wptdparent.getInt("work_plan_id")),new Integer(beginOrders),new Integer(endOrders)});
						if(vchild!=null){
							int childs = vchild.size();
							int tmpEnd = endOrders-1;
							int tmpBeign = beginOrders +1;
							for(int k =1;k<childs; k ++){
								WorkPlanTaskDb tmp = (WorkPlanTaskDb)vchild.get(k);
								tmp.set("orders", new Integer(tmpBeign));
								tmp.save();
								tmpBeign ++;
							}
							WorkPlanTaskDb tmp = (WorkPlanTaskDb)vchild.get(0);
							tmp.set("orders", new Integer(tmpBeign));
							tmp.save();
						}
					}
				}
				sendMsg(wptd.getInt("work_plan_id"), resource, "新增任务", "新增一项您负责的任务【" + wptd.getString("name") + "】，请关注！");
			}
		} catch (ResKeyException rsKeyException) {
			rsKeyException.printStackTrace();
			throw new ErrMsgException(rsKeyException.getMessage(request));
		}

		return re;
	}

	/**
	 * 删除任务
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean del(HttpServletRequest request) throws ErrMsgException {
		long taskId = ParamUtil.getLong(request, "taskId");
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(taskId));

		Privilege pvg = new Privilege();
		if (!pvg.canUserManageWorkPlan(request, wptd.getInt("work_plan_id")))
			throw new ErrMsgException("权限非法！");

		// 如果是根任务，则不能删除
		int level = wptd.getInt("task_level");
		if (level == 0)
			throw new ErrMsgException("根任务不能被删除！");

		// 如果带有子任务，则不能被删除
		String sql = "select id from " + wptd.getTable().getName()
				+ " where work_plan_id=? and orders>" + wptd.getInt("orders")
				+ " order by orders";
		Vector v = wptd.list(sql, new Object[] { new Integer(wptd
				.getInt("work_plan_id")) });
		Iterator ir = v.iterator();
		if (ir.hasNext()) {
			WorkPlanTaskDb subwptd = (WorkPlanTaskDb) ir.next();
			if (subwptd.getInt("task_level") > level)
				throw new ErrMsgException("请先删除子任务");
		}

		boolean re = false;
		try {
			int id = wptd.getInt("id");
			String userName = wptd.getString("task_resource");
			String name = wptd.getString("name");
			re = wptd.del();
			if (re) {
				//删除对应的用户
				WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
				wptud.delOfTask(id);
				try {
					refreshGantt(wptd.getInt("work_plan_id"));
				} catch (JSONException ex1) {
					ex1.printStackTrace();
				}
				//发送消息
				sendMsg(wptd.getInt("work_plan_id"), userName,"任务删除","您负责的任务【"+ name +"】已被删除！");
				Privilege privilege = new Privilege();
				String reason = ParamUtil.get(request, "reason");
				WorkPlanLogDb.log(privilege.getUser(request), wptd
						.getInt("work_plan_id"), reason);
			}
		} catch (ResKeyException ex) {
			throw new ErrMsgException(ex.getMessage(request));
		}

		return re;
	}

	/**
	 * 编辑任务，用于任务项界面编辑任务
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean edit(HttpServletRequest request) throws ErrMsgException {
		long id = ParamUtil.getLong(request, "id");
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(id));
		
		if (wptd == null || !wptd.isLoaded()) {
			throw new ErrMsgException("该任务项不存在！");
		}

		Privilege pvg = new Privilege();
		if (!pvg.canUserManageWorkPlan(request, wptd.getInt("work_plan_id")))
			throw new ErrMsgException("权限非法！");

		boolean re = false;
		ParamConfig pc = new ParamConfig(wptd.getTable().getFormValidatorFile());
		ParamChecker pck = new ParamChecker(request);

		try {
			pck.doCheck(pc.getFormRule("workplan_task_edit"));
		} catch (CheckErrException e) {
			// 如果onError=exit，则会抛出异常
			throw new ErrMsgException(e.getMessage());
		}

		pck.setValue("task_level", "缩进值",
				new Integer(wptd.getInt("task_level")));
		pck.setValue("orders", "顺序号", new Integer(wptd.getInt("orders")));

		pck.setValue("resource_type", "资源类别",
						WorkPlanTaskDb.RESOURCE_TYPE_USER);

		pck.setValue("assess", "评价", wptd.getInt("assess"));

		java.util.Date start = pck.getDate("start_date");
		java.util.Date end = pck.getDate("end_date");
		// duration
		OACalendarDb acd = new OACalendarDb();
		// 因为不包含开始日期，所以开始日期需减一天
		int duration = acd.getWorkDayCountFromDb(DateUtil.addDate(start, -1),
				end);
		pck.setValue("duration", "工期", new Integer(duration));
		
		pck.setValue("depends", "前置任务", StrUtil.getNullStr(wptd.getString("depends")));

		String oldResource = wptd.getString("task_resource");
		try {
			re = wptd.save(pck);

			refreshGantt(wptd.getInt("work_plan_id"));

			Privilege privilege = new Privilege();
			String reason = ParamUtil.get(request, "reason");
			WorkPlanLogDb.log(privilege.getUser(request), wptd
					.getInt("work_plan_id"), reason);

			// 如果更改了责任人，则检查该任务项参与人中，新责任人是否已存在，如果存在则不处理，如果不存在，则增加参与人
			String resource = pck.getString("task_resource");
			if (!oldResource.equals(resource)) {
				WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
				//删除任务对应原人力资源
				wptud.delByTaskAndUser(wptd.getLong("id"), oldResource);
				if (!wptud.isTaskUserExist(wptd.getLong("id"), resource)) {
					try {
						wptud.create(new JdbcTemplate(), new Object[] {
								new Long(wptd.getLong("id")), resource,
								new java.util.Date(), new Integer(100),
								new Integer(duration), new Integer(0) });
					} catch (ResKeyException ex) {
						ex.printStackTrace();
					}
				}
				sendMsg(wptd.getInt("work_plan_id"), oldResource, "任务修改", "您负责的任务【" + wptd.getString("name") + "】已修改,责任人改为【" + resource + "】");
				sendMsg(wptd.getInt("work_plan_id"), resource, "任务修改", "新增任务【" + wptd.getString("name") + "】，负责人为您，请关注！");
			}
			else
			{
				sendMsg(wptd.getInt("work_plan_id"), resource, "任务修改", "任务【" + wptd.getString("name") + "】已修改，请关注！");
			}
			
		} catch (ResKeyException rsKeyException) {
			throw new ErrMsgException(rsKeyException.getMessage(request));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return re;
	}

	public boolean checkTasks(JSONObject gantt) throws ErrMsgException {
		JSONArray jsonAry = null;
		try {
			jsonAry = (JSONArray) gantt.get("tasks");
		} catch (JSONException ex1) {
			ex1.printStackTrace();
			return false;
		}

		for (int i = 0; i < jsonAry.length(); i++) {
			JSONObject obj = null;
			try {
				obj = (JSONObject) jsonAry.get(i);
				String resource = obj.getString("resource");
				if (resource.equals(""))
					throw new ErrMsgException("责任人不能为空！");

			} catch (JSONException ex) {
				ex.printStackTrace();
				return false;
			}
		}

		return true;
	}

	/**
	 * 比较两个任务是否相同
	 * 
	 * @param obj0
	 *            JSONObject
	 * @param obj1
	 *            JSONObject
	 * @return boolean
	 */
	public static boolean compareTask(JSONObject obj0, JSONObject obj1) {
		try {
			try {
				if (!obj0.getString("startIsMilestone").equals(
						obj1.getString("startIsMilestone"))) {
					return false;
				}
			} catch (JSONException e) {
				if (obj0.getBoolean("startIsMilestone") != obj1.getBoolean("startIsMilestone")) {
					return false;
				}
			}
			if (!obj0.getString("workplanRelated").equals(
					obj1.getString("workplanRelated"))) {
				return false;
			}
			try {
				if (!obj0.getString("progress").equals(obj1.getString("progress"))) {
					return false;
				}
			} catch (JSONException e) {
				if (obj0.getInt("progress") != obj1.getInt("progress")) {
					return false;
				}
			}
			try {
				if (!obj0.getString("status").equals(obj1.getString("status"))) {
					return false;
				}
			} catch (JSONException e) {
				if (obj0.getInt("status") != obj1.getInt("status")) {
					return false;
				}
			}
			if (!obj0.getString("resource").equals(obj1.getString("resource"))) {
				return false;
			}
			if (!obj0.getString("name").equals(obj1.getString("name"))) {
				return false;
			}
			try {
				if (!obj0.getString("assess").equals(obj1.getString("assess"))) {
					return false;
				}
			} catch (JSONException e) {
				if (obj0.getInt("assess") != obj1.getInt("assess")) {
					return false;
				}
			}
			if (!obj0.getString("depends").equals(obj1.getString("depends"))) {
				return false;
			}
			try {
				if (!obj0.getString("end").equals(obj1.getString("end"))) {
					return false;
				}
			} catch (JSONException e) {
				if (obj0.getInt("end") != obj1.getInt("end")) {
					return false;
				}
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * 修改任务，用于保存甘特图操作
	 * 
	 * @param request
	 * @param workplanId
	 * @return
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public boolean modify(HttpServletRequest request, long workplanId)
			throws ErrMsgException, ResKeyException, JSONException {

		Privilege privilege = new Privilege();
		String data = ParamUtil.get(request, "data");

		// 遍历新的task
		JSONObject json = new JSONObject(data);

		checkTasks(json);
		
		WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();

		// System.out.println(getClass() + " data=======================" +
		// data);

		WorkPlanMgr wpm = new WorkPlanMgr();
		WorkPlanDb wpd = wpm.getWorkPlanDb(request, (int) workplanId, "edit");

		WorkPlanTaskDb wptd = new WorkPlanTaskDb();

		Map mapExisted = new HashMap();
		
		//sql注入校验
		try
		{
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "workplanId", String.valueOf(workplanId), getClass().getName());
		}catch(Exception e)
		{
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		}
		
		String sql = "select id from " + wptd.getTable().getName()
				+ " where work_plan_id=" + workplanId;
		Iterator ir = wptd.list(sql).iterator();
		
		while (ir.hasNext()) {
			wptd = (WorkPlanTaskDb) ir.next();
			String code = wptd.getString("code");

			// 找出不存在的task予以删除
			try {
				boolean isFound = false;
				JSONArray jsonAry = (JSONArray) json.get("tasks");
				for (int i = 0; i < jsonAry.length(); i++) {
					JSONObject obj = (JSONObject) jsonAry.get(i);
					String codeNew = obj.getString("code");
					// 更新已存在的任务
					if (codeNew.equals(code)) {
						
						changeTask(request, obj, wptd, i);

						mapExisted.put(codeNew, codeNew);
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					//删除待删除任务对应的人力资源
					Long taskId = wptd.getLong("id");
					wptud.delOfTask(taskId);
					// 未找到则说明修改后的计划中无该任务项，需删除
					wptd.del();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				throw e;
			}

		}
		
		// wptd.delTasks(workplanId);
		try {
			// 加入tasks
			JSONArray jsonAry = (JSONArray) json.get("tasks");
			for (int i = 0; i < jsonAry.length(); i++) {
				JSONObject obj = (JSONObject) jsonAry.get(i);
				String code = obj.getString("code");
				if (code.equals("")) {
					code = RandomSecquenceCreator.getId(20);
					obj.put("code", code);
				} else {
					if (mapExisted.get(code) != null)
						continue;
				}
				String name = obj.getString("name");
				String strStatus = obj.getString("status");
				String start = "";
				try {
					start = obj.getString("start");
				}
				catch (JSONException e) {
					start = String.valueOf(obj.getLong("start"));
				}
				String end = "";
				try {
					end = obj.getString("end");
				}
				catch (JSONException e) {
					end = String.valueOf(obj.getLong("end"));
				}
				
				int duration = obj.getInt("duration");
				boolean startIsMilestone = obj.getBoolean("startIsMilestone");
				boolean endIsMilestone = obj.getBoolean("endIsMilestone");
				String resource = obj.getString("resource");
				int level = 0;
				try {
					level = obj.getInt("level");
				}
				catch (JSONException e) {
					level = StrUtil.toInt(obj.getString("level"));
				}

				int status = WorkPlanTaskDb.getStatusByDesc(strStatus);

				java.util.Date sDate = DateUtil.parse(start);
				java.util.Date eDate = DateUtil.parse(end);

				int progress = 0;
				try {
					progress = obj.getInt("progress");
				}
				catch (JSONException e) {
					progress = StrUtil.toInt(obj.getString("progress"), 0);
				}				
				String workplanRelated = obj.getString("workplanRelated");
				long longWorkplanRelated = StrUtil.toLong(workplanRelated, -1);
				int assess = StrUtil.toInt(obj.getString("assess"), 0);
				String reportFlowType = obj.getString("reportFlowType");
				
				
				//sql注入校验
				try
				{
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "name", name, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "strStatus", strStatus, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "start", start, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "end", end, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "duration", String.valueOf(duration), getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "startIsMilestone", String.valueOf(startIsMilestone), getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "endIsMilestone", String.valueOf(endIsMilestone), getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "resource", resource, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "level", String.valueOf(level), getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "status", String.valueOf(status), getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "progress", String.valueOf(progress), getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "workplanRelated", workplanRelated, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "assess", String.valueOf(assess), getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "reportFlowType", reportFlowType, getClass().getName());
				}
				catch(Exception e){
					throw new ErrMsgException(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
				}
				
				String depends = "";
				if (obj.has("depends")) {
					try {
						depends = StrUtil.getNullStr(obj.getString("depends"));
					}
					catch (JSONException e) {
						depends = String.valueOf(obj.getLong("depends"));
					}
				}
				
				wptd.create(new JdbcTemplate(), new Object[] { name, code,
						new Integer(level), new Integer(status), sDate, eDate,
						new Integer(duration),
						new Integer(startIsMilestone ? 1 : 0),
						new Integer(endIsMilestone ? 1 : 0), resource,
						new Integer(WorkPlanTaskDb.RESOURCE_TYPE_USER),
						new Long(workplanId), new Integer(progress),
						new Long(longWorkplanRelated), new Integer(assess),
						new Integer(i), reportFlowType, depends });

				if (level == 0) {
					// 根据第一条任务更新workplan
					wpd.setBeginDate(sDate);
					wpd.setEndDate(eDate);
					wpd.setProgress(progress);
					wpd.save();
				}

				// 创建任务用户
				if (!resource.equals("")) {
					wptd = wptd.getTaskByOrders((int) workplanId, i);

					wptud.create(new JdbcTemplate(), new Object[] {
							new Long(wptd.getLong("id")), resource,
							new java.util.Date(), new Integer(100),
							new Integer(duration), new Integer(0) });
				
					//发送消息
					sendMsg(wptd.getInt("work_plan_id"), resource, "新增任务", "新增任务【" +wptd.getString("name")  + "】，请关注!");					
				}				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}

		wpd.setGantt(json.toString());
		boolean re = wpd.save();

		String reason = ParamUtil.get(request, "reason");
		WorkPlanLogDb.log(privilege.getUser(request), wpd.getId(), reason);

		return re;
	}

	public boolean move(HttpServletRequest request) throws ErrMsgException {
		long id = ParamUtil.getLong(request, "id");
		//int workPlanId = ParamUtil.getInt(request, "workplanId");
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(id));

		boolean isUp = ParamUtil.get(request, "direction").equals("true");

		int orders = wptd.getInt("orders");

		// 如果level与上一个或下一个节点不相等，则不允许移动
		if (isUp) {
			// 如果orders为0，则不允许上移
			if (orders == 0)
				throw new ErrMsgException("不能上移！");

			WorkPlanTaskDb perTask = canMoveUp(wptd);
			if (perTask==null)
				throw new ErrMsgException("不能上移！");

			try {
				//获得要上移的节点及其子节点
				Vector<WorkPlanTaskDb> tasks = getChildTask(id,wptd.getInt("work_plan_id"));
				//获得上一个节点及其子节点
				Vector<WorkPlanTaskDb> perTasks = getChildTask(perTask.getLong("id"),perTask.getInt("work_plan_id"));
				
				int taskCount = tasks.size();
				int perCount = perTasks.size();
				
				//要上移的节点及其子节点每个的order-perCount
				Iterator<WorkPlanTaskDb> irtasks = tasks.iterator();
				while(irtasks.hasNext()){
					WorkPlanTaskDb tmp = irtasks.next();
					int tmpOrders = tmp.getInt("orders");
					tmp.set("orders", Integer.valueOf((tmpOrders-perCount)));
					tmp.save();
				}
				//上一节点及其子节点每个的order+taskCount
				Iterator<WorkPlanTaskDb> irPerTasks = perTasks.iterator();
				while(irPerTasks.hasNext()){
					WorkPlanTaskDb tmp = irPerTasks.next();
					int tmpOrders = tmp.getInt("orders");
					tmp.set("orders", Integer.valueOf(tmpOrders+taskCount));
					tmp.save();
				}
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ErrMsgException(e.getMessage(request));
			}

		} else {
			// 如果没有下一节点，则不允许下移
			WorkPlanTaskDb task = wptd.getTaskByOrders(wptd
					.getInt("work_plan_id"), orders + 1);
			if (task == null)
				throw new ErrMsgException("不能下移！");

			WorkPlanTaskDb nextTask = canMoveDown(wptd);
			if (nextTask==null)
				throw new ErrMsgException("不能下移！");

			try {
				//获得要上移的节点及其子节点
				Vector<WorkPlanTaskDb> tasks = getChildTask(id,wptd.getInt("work_plan_id"));
				//获得上一个节点及其子节点
				Vector<WorkPlanTaskDb> nextTasks = getChildTask(nextTask.getLong("id"),nextTask.getInt("work_plan_id"));
				
				int taskCount = tasks.size();
				int nextCount = nextTasks.size();
				
				//要下移的节点及其子节点每个的order+nextCount
				Iterator<WorkPlanTaskDb> irtasks = tasks.iterator();
				while(irtasks.hasNext()){
					WorkPlanTaskDb tmp = irtasks.next();
					int tmpOrders = tmp.getInt("orders");
					tmp.set("orders", Integer.valueOf(tmpOrders+nextCount));
					tmp.save();
				}
				//下一节点及其子节点每个的order-taskCount
				Iterator<WorkPlanTaskDb> irNextTasks = nextTasks.iterator();
				while(irNextTasks.hasNext()){
					WorkPlanTaskDb tmp = irNextTasks.next();
					int tmpOrders = tmp.getInt("orders");
					tmp.set("orders", Integer.valueOf(tmpOrders-taskCount));
					tmp.save();
				}
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ErrMsgException(e.getMessage(request));
			}
		}
		return true;
	}
	
	/**
	 * 判断是否能上移,如果能上移返回上一个节点，否则返回null
	 * @Description: 
	 * @param wptd
	 * @return
	 */
	public WorkPlanTaskDb canMoveUp(WorkPlanTaskDb wptd){
		int workPlanId = wptd.getInt("work_plan_id");
		int orders = wptd.getInt("orders");
		int taskLevel = wptd.getInt("task_level");
		if(orders==0){
			return null;
		}
		String sql = "select id from "+wptd.getTable().getName()
			+" where work_plan_id="+workPlanId+" and orders < ? and task_level <= ? order by orders desc";
		Iterator ir = wptd.list(sql, new Object[] {new Integer(orders),new Integer(taskLevel) }).iterator();
		if(ir.hasNext()){
			WorkPlanTaskDb wptdBrother = (WorkPlanTaskDb)ir.next();
			int tmpLevel = wptdBrother.getInt("task_level");
			if(tmpLevel==taskLevel){
				return wptdBrother;
			}
		}
		
		return null;
	}
	
	/**
	 * 判断是否能下移,如果能下移返回下一个节点，否则返回null
	 * @Description: 
	 * @param wptd
	 * @return
	 */
	public WorkPlanTaskDb canMoveDown(WorkPlanTaskDb wptd){
		int workPlanId = wptd.getInt("work_plan_id");
		int orders = wptd.getInt("orders");
		int taskLevel = wptd.getInt("task_level");
		
		String sql = "select id from "+wptd.getTable().getName()
			+" where work_plan_id="+workPlanId+" and orders > ? and task_level <= ? order by orders asc";
		Iterator ir = wptd.list(sql, new Object[] {new Integer(orders),new Integer(taskLevel) }).iterator();
		if(ir.hasNext()){
			WorkPlanTaskDb wptdBrother = (WorkPlanTaskDb)ir.next();
			int tmpLevel = wptdBrother.getInt("task_level");
			if(tmpLevel==taskLevel){
				return wptdBrother;
			}
		}
		
		return null;
	}
	
	/**
	 * 获得task节点和其子节点
	 * @Description: 
	 * @param taskid
	 * @param workplanId
	 * @return
	 */
	public Vector<WorkPlanTaskDb> getChildTask(long taskid,int workplanId){
		Vector<WorkPlanTaskDb> res = new Vector<WorkPlanTaskDb>();
		
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(taskid));
		int beginOrders = wptd.getInt("orders");
		int endOrders = -1;
		int endtaskId = -1;
		//找到当前节点的后续同级、高级节点
		String sql = "select id from "+wptd.getTable().getName()
			+" where work_plan_id=" + workplanId+" and orders > ? and task_level <= ? order by orders asc";
		Iterator ir = wptd.list(sql, new Object[] {new Integer(beginOrders),new Integer(wptd.getInt("task_level")) }).iterator();
		System.out.println(getClass()+":::::::111:::"+sql+","+beginOrders+","+wptd.getInt("task_level"));
		if(ir.hasNext()){
			WorkPlanTaskDb wptdBrother = (WorkPlanTaskDb)ir.next();
			endOrders = wptdBrother.getInt("orders");
			endtaskId = wptdBrother.getInt("id");
		}
		
		if(endOrders==-1){
			//没找到，说明该节点以后没有比他高级或同级的节点，也就是说，他下面有节点的话也是他的子节点
			sql = "select id from " +wptd.getTable().getName()
				+ " where work_plan_id='"+workplanId+"' and orders > ? order by orders desc";
			Vector v = wptd.list(sql,new Object[]{new Integer(beginOrders)});
			System.out.println(getClass()+":::::::222:::"+sql+","+beginOrders);
			if(v!=null){
				ir = v.iterator();
				if(ir.hasNext()){
					WorkPlanTaskDb wptdBrother = (WorkPlanTaskDb)ir.next();
					endOrders = wptdBrother.getInt("orders")+1;
				}
			}
		}
		
		if(endOrders!=-1){//找到了
			//获得beginOrders到endOrders中间的所有节点，这些都是本节点的下属节点
			res.add(wptd);//加入父节点
			
			sql = "select id from "+wptd.getTable().getName()
			+ " where work_plan_id = "+workplanId+" and orders > ? and orders <? order by orders asc";
			WorkPlanTaskDb wptdchild = new WorkPlanTaskDb();
			Vector vchild = wptdchild.list(sql,new Object[]{new Integer(beginOrders),new Integer(endOrders)});
			if(vchild!=null){
				Iterator irtmp = vchild.iterator();
				while(irtmp.hasNext()){
					WorkPlanTaskDb tmp = (WorkPlanTaskDb)irtmp.next();
					System.out.println(getClass()+":::"+tmp.getString("name"));
					res.add(tmp);
				}
			}
		}else{//没有找到
			res.add(wptd);
		}
		return res;
	}

	/**
	 * 重新刷新gantt
	 * 
	 * @param workplanId
	 * @return
	 * @throws JSONException
	 */
	public static boolean refreshGantt(int workplanId) throws JSONException {
		// {"tasks":[{"id":-1,"name":"<%=wpd.getTitle()%>","code":"","level":0,"status":"STATUS_ACTIVE","start":<%=DateUtil.toLong(wpd.getBeginDate())%>,"duration":<%=DateUtil.datediff(wpd.getEndDate(),
		// wpd.getBeginDate())%>,"end":<%=DateUtil.toLong(wpd.getEndDate())%>,"startIsMilestone":true,"endIsMilestone":false,"assigs":[]}],
		// }
		JSONObject ganttObj = new JSONObject();

		ganttObj.put("selectedRow", "0");
		ganttObj.put("deletedTaskIds", new JSONArray());
		ganttObj.put("canWrite", true);
		ganttObj.put("canWriteOnParent", false);

		WorkPlanDb wpd = new WorkPlanDb();
		wpd = wpd.getWorkPlanDb(workplanId);

		UserDb user = new UserDb();

		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		JSONArray jsonAry = new JSONArray(); // json.get("tasks");
		String sql = "select id from " + wptd.getTable().getName()
				+ " where work_plan_id=? order by orders";
		Iterator ir = wptd.list(sql, new Object[] { new Integer(workplanId) })
				.iterator();
		while (ir.hasNext()) {
			wptd = (WorkPlanTaskDb) ir.next();

			JSONObject obj = new JSONObject();
			obj.put("id", wptd.getLong("id"));
			obj.put("code", wptd.getString("code"));
			obj.put("name", wptd.getString("name"));
			obj.put("status", WorkPlanTaskDb.getStatusDesc(wptd
					.getInt("status")));
			obj.put("start", wptd.getDate("start_date").getTime());
			obj.put("end", wptd.getDate("end_date").getTime());
			obj.put("duration", wptd.getInt("duration"));
			obj.put("startIsMilestone", wptd.getInt("startIsMilestone") == 1);
			obj.put("endIsMilestone", wptd.getInt("endIsMilestone") == 1);
			obj.put("resource", wptd.getString("task_resource"));
			String realName = "";
			if (!StrUtil.getNullStr(wptd.getString("task_resource")).equals("")) {
				realName = user.getUserDb(wptd.getString("task_resource"))
						.getRealName();
			}
			obj.put("resourceDesc", realName);
			obj.put("level", wptd.getInt("task_level"));
			obj.put("progress", wptd.getInt("progress"));
			obj.put("workplanRelated",
					wptd.getInt("workplan_related") == -1 ? "" : ""
							+ wptd.getInt("workplan_related"));
			obj.put("assess", wptd.getInt("assess"));
			obj.put("depends", wptd.getString("depends"));
			obj.put("reportFlowType", StrUtil.getNullStr(wptd
					.getString("report_flow_type")));

			jsonAry.put(obj);

		}

		ganttObj.put("tasks", jsonAry);

		wpd.setGantt(ganttObj.toString());
		return wpd.save();
	}

	public void copyGantt(HttpServletRequest request, WorkPlanDb destWpd,
			int sourceWorkplanId) throws ErrMsgException {
		WorkPlanDb wp = new WorkPlanDb();
		wp = wp.getWorkPlanDb(sourceWorkplanId);

		// wptd.delTasks(workplanId);
		try {
			JSONObject json = new JSONObject(wp.getGantt());
			WorkPlanTaskDb wptd = new WorkPlanTaskDb();

			// 加入tasks
			JSONArray jsonAry = (JSONArray) json.get("tasks");
			for (int i = 0; i < jsonAry.length(); i++) {
				JSONObject obj = (JSONObject) jsonAry.get(i);
				String code = obj.getString("code");
				String name = obj.getString("name");
				String strStatus = obj.getString("status");
				String start = "";
				try {
					start = String.valueOf(obj.getLong("start"));
				}
				catch (JSONException e) {
					start = obj.getString("start");
				}					
				String end = "";
				try {
					end = String.valueOf(obj.getLong("end"));
				}
				catch (JSONException e) {
					end = obj.getString("end");
				}				
				int duration = obj.getInt("duration");
				boolean startIsMilestone = obj.getBoolean("startIsMilestone");
				boolean endIsMilestone = obj.getBoolean("endIsMilestone");
				String resource = obj.getString("resource");
				int level = 1;
				try {
					level = obj.getInt("level");
				}
				catch (JSONException e) {
					level = StrUtil.toInt(obj.getString("level"), 1);
				}
				int status = WorkPlanTaskDb.getStatusByDesc(strStatus);

				java.util.Date sDate = DateUtil.parse(start);
				java.util.Date eDate = DateUtil.parse(end);

				int progress = 0;
				try {
					progress = obj.getInt("progress");
				}
				catch (JSONException e) {
					progress = StrUtil.toInt(obj.getString("progress"), 0);
				}
				
				String workplanRelated = obj.getString("workplanRelated");
				long longWorkplanRelated = StrUtil.toLong(workplanRelated, -1);

				int assess;
				try {
					assess = obj.getInt("assess");
				}
				catch (JSONException e) {
					assess = StrUtil.toInt(obj.getString("assess"), 0);
				}				
				
				String reportFlowType = "";
				if (obj.has("report_flow_type")) {
					reportFlowType = obj.getString("report_flow_type");
				}
				String depends = "";
				if (obj.has("depends")) {
					depends = StrUtil.getNullStr(obj.getString("depends"));
				}
				wptd.create(new JdbcTemplate(), new Object[] { name, code,
						new Integer(level), new Integer(status), sDate, eDate,
						new Integer(duration),
						new Integer(startIsMilestone ? 1 : 0),
						new Integer(endIsMilestone ? 1 : 0), resource,
						new Integer(WorkPlanTaskDb.RESOURCE_TYPE_USER),
						new Long(destWpd.getId()), new Integer(progress),
						new Long(longWorkplanRelated), new Integer(assess),
						new Integer(i), reportFlowType, depends });

				// 创建任务用户
				if (!resource.equals("")) {
					wptd = wptd.getTaskByOrders((int) destWpd.getId(), i);

					WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
					wptud.create(new JdbcTemplate(), new Object[] {
							new Long(wptd.getLong("id")), resource,
							new java.util.Date(), new Integer(100),
							new Integer(duration), new Integer(0) });
					//发送消息
					// sendMsg(wptd.getInt("work_plan_id"), resource, "新增任务", "新增一项您负责的任务【" + name + "】，请关注！");
				}
			}

			destWpd.setGantt(json.toString());
			destWpd.save();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}

	}

	/**
	 * 导入project
	 * 
	 * @param application
	 * @param request
	 * @param workPlanId
	 * @return
	 * @throws ErrMsgException
	 */
	public void importProject(ServletContext application,
			HttpServletRequest request, long workPlanId) throws ErrMsgException {
		Privilege privilege = new Privilege();
		if (!privilege.isUserLogin(request))
			throw new ErrMsgException("请先登录！");

		FileUpload fu = doUpload(application, request);
		Vector filesV = fu.getFiles(); 
		Iterator it = filesV.iterator();
		String filePath = null;
		String ext = null;
		while (it.hasNext()) {
			FileInfo fileInfo = (FileInfo) it.next();
			filePath = fileInfo.getTmpFilePath();
			ext = fileInfo.getExt();
			break;
		}
		if (!"mpp".equalsIgnoreCase(ext)) {
			throw new ErrMsgException("文件类型错误，请上传mpp格式文件");
		}
		File file = new File(filePath);
		MPPReader mppRead = new MPPReader();
		ProjectFile pf = null;
		List tasks = null;
		try {
			pf = mppRead.read(file);
			tasks = pf.getAllTasks();
		} catch (MPXJException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally{
			if (file != null) {
				file.delete();
			}
		}

		// 创建根任务
		WorkPlanDb wpd = new WorkPlanDb();
		wpd = wpd.getWorkPlanDb((int) workPlanId);
		// 判断该任务根节点是否存在，不存在创建，存在则修改
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		String taskSql = "select * from " + wptd.getTable().getName()
				+ " where work_plan_id=" + workPlanId;
		Vector rootVector = wptd.list(taskSql);

		if (rootVector.isEmpty()) {
			try {
				// 插入数据至数据库中
				insertData(tasks, wpd, wptd);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				delTaskAndUser(workPlanId, wptd);
				throw new ErrMsgException("导入失败！" + e.getMessage());

			}
		} else if (rootVector.size() == 1) {
			try {
				wptd = (WorkPlanTaskDb) rootVector.iterator().next();
				// 若是存在则删除
				wptd.del();
				// 插入数据至数据库中
				insertData(tasks, wpd, wptd);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				delTaskAndUser(workPlanId, wptd);
				throw new ErrMsgException("导入失败！" + e.getMessage());
			}
		} else {
			throw new ErrMsgException("该项目已经部署完成，无法导入新内容");
		}

		// 刷新gantt图
		try {
			refreshGantt(wpd.getId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	/**
	 * 导入失败，删除导入数据
	 * 
	 * @param workPlanId
	 * @param wptd
	 */
	private void delTaskAndUser(long workPlanId, WorkPlanTaskDb wptd) {
		String sql = "select * from " + wptd.getTable().getName()
				+ " where work_plan_id=" + workPlanId;
		Vector vector = wptd.list(sql);
		Iterator delIt = vector.iterator();
		WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
		while (delIt.hasNext()) {
			wptd = (WorkPlanTaskDb) delIt.next();
			int id = wptd.getInt("id");
			sql = "select * from " + wptud.getTable().getName()
					+ " where task_id=" + id;
			Vector uVector = wptud.list(sql);
			Iterator uIt = uVector.iterator();
			while (uIt.hasNext()) {
				wptud = (WorkPlanTaskUserDb) uIt.next();
				try {
					wptud.del();
				} catch (ResKeyException e1) {
					// TODO Auto-generated catch block

				}
			}

		}
	}

	/**
	 * 插入数据至数据库中
	 * 
	 * @param tasks
	 * @param wpd
	 * @param wptd
	 * @throws ResKeyException
	 * @throws ErrMsgException
	 */
	private void insertData(List tasks, WorkPlanDb wpd, WorkPlanTaskDb wptd)
			throws ResKeyException, ErrMsgException {
		int orders = 0;
		// 创建任务用户db类
		WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
		for (int i = 1; i < tasks.size(); i++) {
			Task task = ((Task) tasks.get(i));

			String name = task.getName();
			// 若是name为空，则不允许插入数据
			if (name == null) {
				break;
			}
			String code = RandomSecquenceCreator.getId(20);
			task.getResourceNames();
			int level = task.getOutlineLevel() - 1;
			Date endDate = task.getFinish();
			Date beginDate = task.getStart();
			int status = endDate.after(new Date()) ? WorkPlanTaskDb.STATUS_ACTIVE
					: WorkPlanTaskDb.STATUS_DONE;
			int duration = (int) task.getDuration().getDuration();
			int startIsMilestone = 0;
			int endIsMilestone = task.getMilestone() ? 1 : 0;
			String resource = getResource(task);
			Number process = task.getPercentageComplete();
			int workplanRelated = -1;
			int assess = 0;
			String reportFlowType = "";
			String[] resources = null;
			List<String> persons = null;
			// 获取人员信息
			if (resource != null) {
				resources = resource.split(",");
				if (resources.length > 0) {
					persons = new ArrayList<String>();
					for (String str : resources) {
						persons.add(str);
					}
				}

			}
			// 获取所有用户名，判断当前用户名是否为系统账户，若不是，则该条不允许导入
			List<String> userNames = null;
			List<String> portions = null;
			// 获取负责人信息，资源名称第一个人名作为负责人
			if (persons != null && persons.size() > 0) {
				userNames = new ArrayList<String>();
				portions = new ArrayList<String>();
				for (String person : persons) {
					String[] userInfo = person.split("-");
					userNames.add(userInfo.length > 0 ? userInfo[0] : "");
					portions.add(userInfo.length > 1 ? userInfo[1] : "");
				}
			}
			// 账户校验,若不存在账户，则该project不允许导入
			UserDb user = new UserDb();
			boolean flag = true;
			for (String userName : userNames) {
				user = user.getUserDb(userName);
				if (user.getRealName() == null) {
					flag = false;
					break;
				}
			}
			if (!flag) {
				throw new ErrMsgException("参与人含有系统无法识别账户，无法导入新内容");
			}
			if (userNames.size() > 0) {
				// 插入到work_plan_task表中
				wptd.create(new JdbcTemplate(), new Object[] { name, code,
						level, status, beginDate, endDate, duration,
						startIsMilestone, endIsMilestone, userNames.get(0),
						WorkPlanTaskDb.RESOURCE_TYPE_USER,
						new Long(wpd.getId()), process.intValue(),
						new Long(workplanRelated), assess, orders,
						reportFlowType});
				// 获取插入数据ID
				WorkPlanTaskDb wptd4User = wptd.getTaskByOrders(wpd.getId(),
						orders);
				// 插入到work_plan_task_user表中，遍历人员
				for (int j = 0; j < userNames.size(); j++) {
					
					// 若存在人员
					wptud.create(new JdbcTemplate(), new Object[] {
							new Long(wptd4User.getLong("id")),
							userNames.get(j), new java.util.Date(),
							portions.get(j), duration, new Integer(0) });
					//检查工作计划中是否含有该人员，无则添加
					if (!wpd.queryByWorkPlanIdAndName(wpd.getId(), userNames.get(j)))
					{
						if (j == 0)
						{
							wpd.insertIntoWorkplanUser(wpd.getId(), userNames.get(j), WorkPlanDb.KIND_PRINCIPAL);
						}
						else
						{
							wpd.insertIntoWorkplanUser(wpd.getId(), userNames.get(j), WorkPlanDb.KIND_MEMBER);
						}
					}
				}
				//发送消息
				sendMsg(wpd.getId(), userNames.get(0), "新增任务", "新增一项您负责的任务，请关注！");
			}

			orders++;
		}
	}

	/**
	 * 根据task获取其资源名称内容
	 * 
	 * @param task
	 * @return
	 */
	private String getResource(Task task) {
		StringBuffer buf = new StringBuffer();
		List assignments = task.getResourceAssignments();
		for (int i = 0; i < assignments.size(); i++) {
			ResourceAssignment assignment = (ResourceAssignment) assignments
					.get(i);
			Resource resource = assignment.getResource();

			if (resource != null) {
				// buf.append(resource.getName()).append(" ");

				buf.append(resource.getName() + "-" + assignment.getUnits()
						+ ",");// assignment.getUnits()获得单位百分比
			}
		}
		return buf.toString();
	}
	/**
	 * 文件上传
	 * @param application
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public FileUpload doUpload(ServletContext application,
			HttpServletRequest request) throws ErrMsgException {
		fileUpload = new FileUpload();
		fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
		// String[] extnames = {"jpg", "gif", "png"};
		// fileUpload.setValidExtname(extnames);//设置可上传的文件类型

		int ret = 0;
		try {
			ret = fileUpload.doUpload(application, request);
			if (ret != FileUpload.RET_SUCCESS) {
				throw new ErrMsgException("ret=" + ret + " "
						+ fileUpload.getErrMessage());
			}
		} catch (IOException e) {
			logger.error("doUpload:" + e.getMessage());
		}
		return fileUpload;
	}
	/**
     * 发送消息
     * @param title
     * @param content
     */
	private void sendMsg(int workPlanId, String name, String title, String content) {
		WorkPlanDb wpd = new WorkPlanDb();
		wpd = wpd.getWorkPlanDb(workPlanId);
		if (wpd!=null) {
			// 如果审核未通过，则不发送消息
			if (wpd.getCheckStatus()==WorkPlanDb.CHECK_STATUS_NOT) {
				return;
			}
		}
		
        String action = "action=" + MessageDb.ACTION_WORKPLAN + "|id=" + wpd.getId();
		
		//判断是否需要发送短信
		// boolean isToMobile = config.getBooleanProperty("flowAutoSMSRemind");
		boolean isToMobile = com.redmoon.oa.sms.SMSFactory.isUseSMS();
		
		IMessage imsg = null;
		IMsgUtil imu = null;
		//发送系统消息
		try {
			ProxyFactory proxyFactory = new ProxyFactory(
            "com.redmoon.oa.message.MessageDb");
			imsg = (IMessage) proxyFactory.getProxy();
			imsg.sendSysMsg(name , title, content, action);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//发送短信
		UserDb ud = new UserDb();
		ud = ud.getUserDb(name);
		
		if (isToMobile){
			imu = SMSFactory.getMsgUtil();
			try {
				imu.send(ud, title, MessageDb.SENDER_SYSTEM);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
    }

}
