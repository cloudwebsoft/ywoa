package com.redmoon.oa.post;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.pointsys.PointSystemUtil;
import com.redmoon.oa.pointsys.PostAssessBean;

/**
 * @Description:
 * @author:
 * @Date: 2016-2-26下午02:54:32
 */
public class PostKPIValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PostKPIValidator.class.getName());

	public PostKPIValidator() {
	}

	/**
	 * @Description:
	 * @return
	 */
	@Override
	public String getExtraData() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @Description:
	 * @return
	 */
	@Override
	public boolean isUsed() {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @Description:
	 * @param httpservletrequest
	 * @param i
	 * @param fileupload
	 */
	@Override
	public void onActionFinished(HttpServletRequest httpservletrequest, int i,
			FileUpload fileupload) {
		// TODO Auto-generated method stub

	}

	/**
	 * @Description:
	 * @param workflowdb
	 * @param workflowactiondb
	 * @throws ErrMsgException
	 */
	@Override
	public void onWorkflowFinished(WorkflowDb workflowdb,
			WorkflowActionDb workflowactiondb) throws ErrMsgException {
		Leaf leaf = new Leaf(workflowdb.getTypeCode());
		FormDb fd = new FormDb(leaf.getFormCode());
		int flowId = workflowdb.getId();
		FormDAO dao = new FormDAO();
		dao = dao.getFormDAO(flowId, fd);
		if (dao != null && dao.isLoaded()) {
			String userName = StrUtil
					.getNullStr(dao.getFieldValue("curr_name"));
			int year = StrUtil.toInt(dao.getFieldValue("year"), -1);
			if (year == -1) {
				return;
			}
			int month = StrUtil.toInt(dao.getFieldValue("month"), -1);
			if (month == -1) {
				return;
			}
			if (month == 12) {
				year++;
				month = 1;
			} else {
				month++;
			}
			PostAssessBean pab = new PostAssessBean();
			pab.setFlowId(flowId);
			pab.setAssessScore((int) (StrUtil.toFloat(dao
					.getFieldValue("assess_score"), 0.0f)));
			pab.setSumSelfScore((int) (StrUtil.toFloat(dao
					.getFieldValue("sum_self_score"), 0.0f)));
			pab.setSumCheckScore((int) (StrUtil.toFloat(dao
					.getFieldValue("sum_check_score"), 0.0f)));
			pab.setScoreGrade(StrUtil.getNullStr(dao
					.getFieldValue("score_grade")));
			PointSystemUtil psu = new PointSystemUtil();
			psu.setPostAssessBean(pab);
			psu.refreshUser(userName, year, month, 0,
					PointSystemUtil.SCORE_CHANGED_ASSESS);
		}
	}

	/**
	 * @Description:
	 * @param s
	 */
	@Override
	public void setExtraData(String s) {
		// TODO Auto-generated method stub

	}

	/**
	 * @Description:
	 * @param flag
	 */
	@Override
	public void setIsUsed(boolean flag) {
		// TODO Auto-generated method stub

	}

	/**
	 * @Description:
	 * @param httpservletrequest
	 * @param fileupload
	 * @param i
	 * @param vector
	 * @return
	 * @throws ErrMsgException
	 */
	@Override
	public boolean validate(HttpServletRequest httpservletrequest,
			FileUpload fileupload, int i, Vector vector) throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

}