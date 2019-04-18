package com.redmoon.oa.pointsys;

import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.pointsys.PointSystemUtil;

/**
 * @Description:
 * @author:
 * @Date: 2016-2-26下午02:54:32
 */
public class PointExchangeValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PointExchangeValidator.class.getName());

	public PointExchangeValidator() {
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
					.getNullStr(dao.getFieldValue("user_name"));
			Date date = DateUtil.parse(StrUtil.getNullStr(dao
					.getFieldValue("cur_date")), "yyyy-MM");
			if (date == null) {
				return;
			}
			int year = DateUtil.getYear(date);
			int month = DateUtil.getMonth(date) + 1;
			int score = StrUtil.toInt(dao.getFieldValue("score"), 0);
			PointSystemUtil psu = new PointSystemUtil();
			psu.refreshUser(userName, year, month, score,
					PointSystemUtil.SCORE_CHANGED_USED);
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
		int score = StrUtil.toInt(fileupload.getFieldValue("score"), 0);
		int totalScore = StrUtil.toInt(fileupload
				.getFieldValue("score_residual"), 0);

		if (score == 0) {
			throw new ErrMsgException("请选择需要兑换的礼品！");
		}
		if (score > totalScore) {
			throw new ErrMsgException("剩余的分数尚不够兑换此礼品！");
		}
		return true;
	}

}