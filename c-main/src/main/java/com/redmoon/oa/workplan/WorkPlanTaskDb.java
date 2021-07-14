package com.redmoon.oa.workplan;

import java.sql.SQLException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.ErrMsgException;
import java.util.Vector;

public class WorkPlanTaskDb extends QObjectDb {

	public static final int STATUS_ACTIVE = 2;
	public static final int STATUS_DONE = 3;
	public static final int STATUS_FAILED = -1;
	public static final int STATUS_SUSPENDED = 1;
	public static final int STATUS_UNDEFINED = 0;

	public static final int RESOURCE_TYPE_USER = 0;

	public static String getStatusDesc(int status) {
		switch(status) {
			case STATUS_ACTIVE:
				return "STATUS_ACTIVE";
			case STATUS_DONE:
				return "STATUS_DONE";
			case STATUS_FAILED:
				return "STATUS_FAILED";
			case STATUS_SUSPENDED:
				return "STATUS_SUSPENDED";
			case STATUS_UNDEFINED:
				return "STATUS_UNDEFINED";
		}
		return "";
	}

	public static String getStatusDesc(HttpServletRequest request, int status) {
		switch(status) {
			case STATUS_ACTIVE:
				return "活动";
			case STATUS_DONE:
				return "完成";
			case STATUS_FAILED:
				return "失败";
			case STATUS_SUSPENDED:
				return "挂起";
			case STATUS_UNDEFINED:
				return "未定义";
		}
		return "";
	}

	public static int getStatusByDesc(String statusDesc) {
		if (statusDesc.equals("STATUS_ACTIVE"))
			return STATUS_ACTIVE;
		else if (statusDesc.equals("STATUS_DONE"))
			return STATUS_DONE;
		else if (statusDesc.equals("STATUS_FAILED"))
			return STATUS_FAILED;
		else if (statusDesc.equals("STATUS_SUSPENDED"))
			return STATUS_SUSPENDED;
		else
			return STATUS_UNDEFINED;
	}

	public boolean changeProgress(int progress) {
		if (progress>100) {
			progress = 100;
		}
		WorkPlanDb wpd = new WorkPlanDb();
		wpd = wpd.getWorkPlanDb(getInt("work_plan_id"));
		boolean re = false;
		try {
			JSONObject json = new JSONObject(wpd.getGantt());
			// 加入tasks
			JSONArray jsonAry = (JSONArray) json.get("tasks");
			for (int i = 0; i < jsonAry.length(); i++) {
				JSONObject obj = (JSONObject) jsonAry.get(i);
				String code = obj.getString("code");
				if (code.equals(getString("code"))) {
					String oldProgress = String.valueOf(obj.getInt("progress"));					
					obj.put("progress", String.valueOf(progress));
					if (progress>=100) {
						obj.put("status", getStatusDesc(STATUS_DONE));
					}
					else {
						int oldP = StrUtil.toInt(oldProgress, 0);
						if (oldP==100) {
							// 无需判断前置任务有没有完成，因为如果从100变小，说明已经开始过了
							obj.put("status", getStatusDesc(STATUS_ACTIVE));
						}
					}
					break;
				}
			}
			
			// 如果是根task，则更改整个计划的进度
			int orders = getInt("orders");
			if (orders==0) {
				wpd.setProgress(progress);
			}
			
			wpd.setGantt(json.toString());
			re = wpd.save();

			if (re) {
				int oldP = getInt("progress");				
				set("progress", new Integer(progress));
				if (progress>=100) {
					set("status", STATUS_DONE);
				}
				else {
					if (oldP==100) {
						// 无需判断前置任务有没有完成，因为如果从100变小，说明已经开始过了
						set("status", STATUS_ACTIVE);
					}
				}				
				save();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		catch (ResKeyException e) {
			e.printStackTrace();
		}
		return re;
	}

	/*
	public void delTasks(long workplanId) {
		String sql = "delete from " + getTable().getName() + " where work_plan_id=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			jt.executeUpdate(sql, new Object[]{new Long(workplanId)});
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	
	/**
	 * @Description: 根据taskId获取上级任务对象
	 * @param taskId
	 * @return
	 */
	public WorkPlanTaskDb getParentNode(long taskId) {
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = (WorkPlanTaskDb) wptd.getQObjectDb(new Long(taskId));
		String sql = "select id,min(" + taskId + "-id) c from "
				+ getTable().getName() + " where work_plan_id="
				+ wptd.getInt("work_plan_id") + " and task_level="
				+ (wptd.getInt("task_level") - 1) + " and id<" + taskId;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;

		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				if (rr.getInt("c") > 0) {
					long id = rr.getLong("id");
					return (WorkPlanTaskDb) wptd.getQObjectDb(new Long(id));
				}
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return null;
	}

	/**
	 * 取得任务最后一个孩子节点的orders
	 */
	public int getMaxOrdersOfTaskChildren(int workplanId, long parentTaskId) {
		boolean isParentFound = false;
		int parentLevel = -1;
		int orders = 0;
		String sql = "select id from " + getTable().getName() + " where work_plan_id=? order by orders asc";
		Iterator ir = list(sql, new Object[]{new Long(workplanId)}).iterator();
		while(ir.hasNext()) {
			WorkPlanTaskDb wptd = (WorkPlanTaskDb)ir.next();

			// LogUtil.getLog(getClass()).info(wptd.getString("name") + " orders=" + orders + " level=" + parentLevel + " parentTaskId=" + parentTaskId + " id=" + wptd.getInt("id"));

			if (wptd.getLong("id")==parentTaskId) {
				isParentFound = true;
				parentLevel = wptd.getInt("task_level");
				orders = wptd.getInt("orders");

				LogUtil.getLog(getClass()).info(wptd.getString("name") + " orders=" + orders + " level=" + parentLevel);
				continue;
			}
			/*if (isParentFound) {
				orders = wptd.getInt("orders");
				// 找到其下一个兄弟节点，则退出
				if (wptd.getInt("task_level")==parentLevel) {
					orders -= 1;
					break;
				}
			}*/
		}
		return orders;
	}
	
	/**
	 * 取得任务最后一个孩子节点的orders,这个是原来的，取得的是parentTaskId的orders，我觉得这个不对
	 */
	public int getMaxOrdersOfTaskChildrenOld(int workplanId, long parentTaskId) {
		boolean isParentFound = false;
		int parentLevel = -1;
		int orders = 0;
		String sql = "select id from " + getTable().getName() + " where work_plan_id=? order by orders asc";
		Iterator ir = list(sql, new Object[]{new Long(workplanId)}).iterator();
		while(ir.hasNext()) {
			WorkPlanTaskDb wptd = (WorkPlanTaskDb)ir.next();

			// LogUtil.getLog(getClass()).info(wptd.getString("name") + " orders=" + orders + " level=" + parentLevel + " parentTaskId=" + parentTaskId + " id=" + wptd.getInt("id"));

			if (wptd.getLong("id")==parentTaskId) {	
				isParentFound = true;
				parentLevel = wptd.getInt("task_level");
				orders = wptd.getInt("orders");

				LogUtil.getLog(getClass()).info(wptd.getString("name") + " orders=" + orders + " level=" + parentLevel);
				continue;
			}
			/*if (isParentFound) {
				orders = wptd.getInt("orders");
				// 找到其下一个兄弟节点，则退出
				if (wptd.getInt("task_level")==parentLevel) {
					orders -= 1;
					break;
				}
			}*/
		}
		return orders;
	}

	/**
	 * 根据顺序号取得任务，用于上移、下移
	 * @param workplanId
	 * @param orders 从0开始，0表示根任务
	 * @return
	 */
	public WorkPlanTaskDb getTaskByOrders(int workplanId, int orders) {
		String sql = "select id from " + getTable().getName() + " where work_plan_id=? and orders=?";
		Iterator ir = list(sql, new Object[]{new Integer(workplanId), new Integer(orders)}).iterator();
		if (ir.hasNext()) {
			return (WorkPlanTaskDb)ir.next();
		}
		return null;
	}

	/**
	 * 取出计划的根任务
	 * @param id 计划的ID
	 * @return
	 */
	public WorkPlanTaskDb getRootTask(int id) throws ErrMsgException {
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = wptd.getTaskByOrders(id, 0);

		if (wptd==null) {
			WorkPlanDb wpd = new WorkPlanDb();
			wpd = wpd.getWorkPlanDb(id);
			String jsonStr = wpd.getGantt();
			wptd = new WorkPlanTaskDb();
			if (jsonStr!=null && jsonStr.equals("")) {
				// 创建根任务
				String code = RandomSecquenceCreator.getId(20);
				String name = wpd.getTitle();
				int dur = 0;
				int level = 0;
				String resource = wpd.getPrincipals()[0];
				int status = WorkPlanTaskDb.STATUS_UNDEFINED;
				String reportFlowType = "";
				try {
					wptd.create(new JdbcTemplate(), new Object[] {
							name, code,
							new Integer(level), new Integer(status), wpd.getBeginDate(), wpd.getEndDate(),
							new Integer(dur),
							new Integer(0),
							new Integer(0), resource,
							new Integer(WorkPlanTaskDb.RESOURCE_TYPE_USER),
							new Long(id), new Integer(wpd.getProgress()),
							new Long(-1), new Integer(0), new Integer(0), StrUtil.sqlstr(reportFlowType)
					});
				} catch (ResKeyException e) {
					e.printStackTrace();
					throw new ErrMsgException(e.getMessage());
				}

				// 取得刚创建的task
				wptd = wptd.getTaskByOrders(id, 0);

				// 创建任务用户
				WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
				try {
					wptud.create(new JdbcTemplate(), new Object[]{new Long(wptd.getLong("id")),resource,new java.util.Date(),new Integer(100),new Integer(dur),new Integer(0)});
				} catch (ResKeyException e) {
					e.printStackTrace();
					throw new ErrMsgException(e.getMessage());
				}
				// 刷新gantt图
				try {
					WorkPlanTaskMgr.refreshGantt(id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (wptd==null) {
				throw new ErrMsgException("甘特图尚未初始化！");
			}
		}

		return wptd;
	}

	/**
	 * 删除属于某计划的任务项
	 *
	 * @param workplanId int
	 * @throws ResKeyException
	 */
	public void delOfWorkPlan(int workplanId) throws ResKeyException {
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		String sql = "select id from " + wptd.getTable().getName() + " where work_plan_id=?";
		Iterator ir = wptd.list(sql, new Object[]{new Integer(workplanId)}).iterator();
		while (ir.hasNext()) {
			wptd = (WorkPlanTaskDb) ir.next();
			wptd.del();
		}
	}

	public boolean del() throws ResKeyException {
		boolean re = super.del();
		if (re) {
			// 删除相关的任务用户
			WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
			wptud.delOfTask(getLong("id"));

			// 将位于其后的任务的orders - 1
			String sql = "select id from " + getTable().getName() + " where work_plan_id=? and orders>" +
					getInt("orders");
			Vector v = list(sql, new Object[]{new Integer(getInt("work_plan_id"))});
			Iterator ir = v.iterator();
			while (ir.hasNext()) {
				WorkPlanTaskDb wptd = (WorkPlanTaskDb) ir.next();
				wptd.set("orders", new Integer(wptd.getInt("orders") - 1));
				try {
					wptd.save();
				} catch (ResKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return re;
	}
}
