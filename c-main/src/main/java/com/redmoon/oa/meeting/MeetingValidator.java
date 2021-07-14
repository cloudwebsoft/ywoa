package com.redmoon.oa.meeting;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

public class MeetingValidator implements IFormValidator {

	Logger logger = Logger.getLogger(MeetingValidator.class.getName());

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
	public void onActionFinished(HttpServletRequest request, int flowId,
			FileUpload fu) {
		
	}

	@Override
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb arg1)
			throws ErrMsgException {
	}

	@Override
	public void setExtraData(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIsUsed(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean validate(HttpServletRequest request, FileUpload fu, int flowId,
			Vector fields) throws ErrMsgException {
		WorkflowDb wf = new WorkflowDb(flowId);
		// 第一个节点需要判断会议室的使用情况
		if (!wf.isStarted()) {
			int roomId = StrUtil.toInt(fu.getFieldValue("hyshi"), 0);
			BoardroomDb bDb = new BoardroomDb(roomId);
			if (!bDb.isLoaded()) {
				throw new ErrMsgException("请选择会议室！");
			}
			String startDate = fu.getFieldValue("start_date");
			if (startDate == null || startDate.equals("")) {
				throw new ErrMsgException("请选择会议开始时间！");
			}
			String startDatetime = fu.getFieldValue("start_date_time");
			if (startDatetime != null && !startDatetime.equals("")) {
				startDate += " " + startDatetime;
			}
			String endDate = fu.getFieldValue("end_date");
			if (endDate == null || endDate.equals("")) {
				throw new ErrMsgException("请选择会议结束时间！");
			}
			String endDatetime = fu.getFieldValue("end_date_time");
			if (endDatetime != null && !endDatetime.equals("")) {
				endDate += " " + endDatetime;
			}
			if (DateUtil.parse(startDate, "yyyy-MM-dd HH:mm:ss").after(
					DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss"))) {
				throw new ErrMsgException("会议开始时间必须早于结束时间！");
			}
			BoardroomApplyDb braDb = new BoardroomApplyDb();
			if (braDb.isRoomInUse(roomId, startDate, endDate)) {
				throw new ErrMsgException(bDb.getName() + "在" + startDate + "至"
						+ endDate + "之间已经被申请！");
			}
		}
		return true;
	}

}
