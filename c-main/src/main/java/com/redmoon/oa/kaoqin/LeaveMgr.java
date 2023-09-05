package com.redmoon.oa.kaoqin;

import java.util.Iterator;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.WorkflowDb;

public class LeaveMgr {
	
	/**
	 * 取出本年度的假期天数，用于流程 Validator检测
	 * @param userName
	 * @param leaveType
	 * @return
	 */
	public static double getLeaveCount(int year, String userName, String leaveType) {
		return getLeaveCount(year, userName, leaveType, false);
	}

	/**
	 * 取出本年度的假期天数，用于流程 Validator检测
	 * @param userName
	 * @param leaveType
	 * @param isDeduct 是否扣工资
	 * @return
	 */
	public static double getLeaveCount(int year, String userName, String leaveType, boolean isDeduct) {
		// int y = DateUtil.getYear(new java.util.Date());
		String beginDate = year + "-01-01";
		String endDate = (year + 1) + "-01-01";

		// 含跨年的休假
		String sql = "select f.flowId from ft_qjsqd f, flow fl where f.flowId=fl.id and fl.type_code='qj' and fl.status="
				+ WorkflowDb.STATUS_FINISHED;
		sql += " and ((f.qjkssj>="
				+ SQLFilter.getDateStr(beginDate, "yyyy-MM-dd")
				+ " and f.qjjssj<"
				+ SQLFilter.getDateStr(endDate, "yyyy-MM-dd")
				+ ") or (f.qjkssj<"
				+ SQLFilter.getDateStr(beginDate, "yyyy-MM-dd")
				+ " and f.qjjssj>="
				+ SQLFilter.getDateStr(beginDate, "yyyy-MM-dd")
				+ ") or (f.qjkssj<="
				+ SQLFilter.getDateStr(endDate, "yyyy-MM-dd")
				+ " and f.qjjssj>="
				+ SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + "))";
		
		sql += " and jqlb=" + StrUtil.sqlstr(leaveType);
		
		if (isDeduct) {
			sql += " and is_deduct=1";
		}

		sql += " and f.result='1' and applier=" + StrUtil.sqlstr(userName);
		
		LogUtil.getLog(LeaveMgr.class).info("sql=" + sql);

		FormDAO fdao = new FormDAO();

		FormDb fd = new FormDb();
		fd = fd.getFormDb("qjsqd");
		WorkflowDb wf = new WorkflowDb();
		Iterator<WorkflowDb> ir = wf.list(sql).iterator();
		java.util.Date qjbDate = null; // 本年请假于本年的实际开始日期
		java.util.Date qjeDate = null; // 本年请假于本年的实际结束日期
		java.util.Date bDate = DateUtil.parse(beginDate, "yyyy-MM-dd");
		java.util.Date eDate = DateUtil.parse(endDate, "yyyy-MM-dd");
		com.redmoon.oa.oacalendar.OACalendarDb oacal = new com.redmoon.oa.oacalendar.OACalendarDb();
		double count = 0;
		while (ir.hasNext()) {
			wf = ir.next();

			fdao = fdao.getFormDAO(wf.getId(), fd);

			double qjDays = 0;
			String strBeginDate = fdao.getFieldValue("qjkssj");
			String strEndDate = fdao.getFieldValue("qjjssj");
			java.util.Date ksDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
			java.util.Date jsDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

			qjbDate = ksDate;
			qjeDate = jsDate;

			// 如果请假开始时间早于本年第一天
			if (DateUtil.compare(ksDate, bDate) == 2) {
				qjbDate = bDate;
			}
			// 如果请假结束日期晚于本年最后一天
			if (DateUtil.compare(jsDate, eDate) == 1) {
				qjeDate = eDate;
			}
			// 如果销假日期晚于本年最后一天
			// if (DateUtil.compare(xjDate, eDate)==1) {
			// qjeDate = eDate;
			// }
			// 去除本月请假区间中的节假日
			try {
				qjDays = oacal.getWorkDayCount(DateUtil.addDate(qjbDate, -1),
						qjeDate);
			} catch (ErrMsgException e) {
				LogUtil.getLog(LeaveMgr.class).error(e);
			}

			// 取得表单中的请假天数
			double qjDayCount = StrUtil.toDouble(fdao
					.getFieldValue("ts"), 0.0);
			// 如果表单中的请假天数小于计算出的天数（去除节假日后），则以表单中的为准
			if (qjDayCount < qjDays)
				qjDays = qjDayCount;
			
			count += qjDays;

		}
		
		return count;

	}
}
