package com.redmoon.oa.workplan;

import net.sf.mpxj.mpp.*;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.ProjectFile;
import java.util.List;
import java.io.*;

import org.json.JSONException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.oacalendar.OACalendarDb;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

/**
 * @author Matianyi
 * 
 */
public class mpxjtest {

	/**
	 * @param args
	 * @throws MPXJException
	 */
	public static void main(String[] args) throws MPXJException {
		// TODO Au9to-generated method stub
		File file = new File("c://123.mpp");
		MPPReader mppRead = new MPPReader();
		ProjectFile pf = mppRead.read(file);
		System.out.println("项目文件 : " + pf.getProjectFilePath());

		// 创建根任务
		WorkPlanDb wpd = new WorkPlanDb();
		wpd = wpd.getWorkPlanDb(111);
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		
		String code = RandomSecquenceCreator.getId(20);
		String name = wpd.getTitle();
		int level = 0;
		OACalendarDb oacal = new OACalendarDb();
		try {
			List tasks = pf.getAllTasks();
			mpxjtest mt = new mpxjtest();
			System.out.println("总任务数: " + tasks.size());
			System.out.println("ID|任务名|OutlineLevel|资源|开始时间|结束时间|完成百分比|Text1");
			for (int i = 1; i < tasks.size(); i++) {
				System.out.println(((Task) tasks.get(i)).getUniqueID() + "|"
						+ ((Task) tasks.get(i)).getName() + "|"
						+ ((Task) tasks.get(i)).getOutlineLevel() + "|"
						+ mt.getResource((Task) tasks.get(i)) + "|"
						+ ((Task) tasks.get(i)).getStart() + "|"
						+ ((Task) tasks.get(i)).getFinish() + "|"
						+ ((Task) tasks.get(i)).getPercentageComplete() + "|"
						+ ((Task) tasks.get(i)).getText(1));
			}

			int dur = oacal.getWorkDayCount(wpd.getBeginDate(), wpd
					.getEndDate());
			String resource = wpd.getPrincipals()[0];
			int status = WorkPlanTaskDb.STATUS_UNDEFINED;
			String reportFlowType = "";
			int orders = 0;
			try {
				int assess = 0;
				int workplanRelated = -1;
				int startIsMilestone = 0;
				int endIsMilestone = 0;
				wptd.create(new JdbcTemplate(), new Object[] { name, code,
						new Integer(level), new Integer(status),
						wpd.getBeginDate(), wpd.getEndDate(), new Integer(dur),
						new Integer(startIsMilestone), new Integer(endIsMilestone), resource,
						new Integer(WorkPlanTaskDb.RESOURCE_TYPE_USER),
						new Long(wpd.getId()), new Integer(wpd.getProgress()),
						new Long(workplanRelated), new Integer(assess), new Integer(orders),
						StrUtil.sqlstr(reportFlowType) });
			} catch (ResKeyException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// 取得刚创建的task
			wptd = wptd.getTaskByOrders(wpd.getId(), orders);

			// 创建任务用户
			WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
			wptud.create(new JdbcTemplate(), new Object[] {
					new Long(wptd.getLong("id")), resource,
					new java.util.Date(), new Integer(100), new Integer(dur),
					new Integer(0) });

			// 刷新gantt图
			WorkPlanTaskMgr.refreshGantt(wpd.getId());
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}

	public String getResource(Task task) {
		StringBuffer buf = new StringBuffer();
		List assignments = task.getResourceAssignments();
		for (int i = 0; i < assignments.size(); i++) {
			ResourceAssignment assignment = (ResourceAssignment) assignments
					.get(i);
			Resource resource = assignment.getResource();

			if (resource != null) {
				// buf.append(resource.getName()).append(" ");
				
				buf.append(resource.getName()+" - " + assignment.getUnits()  + ",");//assignment.getUnits()获得单位百分比				
			}
		}
		return buf.toString();
	}
}
