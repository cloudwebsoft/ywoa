package com.redmoon.oa.hr;

import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

public class LeaveFormValidator implements IFormValidator {

	public LeaveFormValidator() {
	}

	@SuppressWarnings("unchecked")
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction)
			throws ErrMsgException {
		// 新建一个FormDb对象
		FormDb fd = new FormDb();
		// 新建一个Leaf对象
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		fd = fd.getFormDb(lf.getFormCode());
		// 新建一个FormD对象
		FormDAO fdao = new FormDAO(wf.getId(), fd);
		fdao.load();
		// fdao.getFieldValue("result");
		String name = fdao.getFieldValue("xm");
		String userName = fdao.getFieldValue("user_name");
		UserDb user = new UserDb();
		user = user.getUserDb(userName);
		user.setValid(0);
		user.save();

		String sql = "select id from ft_personbasic where user_name=" + StrUtil.sqlstr(userName);
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				long id = rr.getLong(1);
				FormDb formDb = new FormDb("personbasic");
				com.redmoon.oa.visual.FormDAO formdao = new com.redmoon.oa.visual.FormDAO();
				formdao = formdao.getFormDAO(id, formDb);
				formdao.setFieldValue("zzqk", "0");
				formdao.save();	// 可以直接update,但调用FormDAO的save方法可以触发onUpdate方法中的事件
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
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
	public void onActionFinished(HttpServletRequest arg0, int arg1,
			FileUpload arg2) {
		// TODO Auto-generated method stub
		
	}

}
