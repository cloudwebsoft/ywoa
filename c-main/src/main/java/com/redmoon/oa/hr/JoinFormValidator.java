package com.redmoon.oa.hr;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;

public class JoinFormValidator implements IFormValidator  {

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
	public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction)
			throws ErrMsgException {
		FormDb fd = new FormDb();
		// 新建一个Leaf对象
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		fd = fd.getFormDb(lf.getFormCode());
		// 新建一个FormD对象
		FormDAO fdao = new FormDAO(wf.getId(), fd);
		fdao.load();
		String photo = fdao.getFieldValue("picture");
		String personNo = fdao.getFieldValue("person_no");
		String sql = "update ft_personbasic set photo='" + photo
				+ "' where flowid =" + wf.getId() + " ";
		if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
			sql = "update ft_personbasic set person_no="
					+ StrUtil.sqlstr(personNo) + ", zzqk='在职', photo='" + photo
					+ "' where flowid =" + wf.getId() + " ";
		}
		JdbcTemplate jte = new JdbcTemplate();
		int r = 0;
		try {
			r = jte.executeUpdate(sql);
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
	}

	@Override
	public void setExtraData(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIsUsed(boolean arg0) {
		// TODO Auto-generated method stub
		
	}
	
    public boolean isPersonNoExist(String personNo) {
    	String sql = "select id from ft_personbasic where person_no=?";
    	JdbcTemplate jt = new JdbcTemplate();
    	try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{personNo});
			if (ri.hasNext()) {
				return true;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
    	return false;
    }	

	@Override
	public boolean validate(HttpServletRequest request, FileUpload fu, int flowId, Vector fields) throws ErrMsgException {
    	// 检查是否已存在
		int actionId = StrUtil.toInt(fu.getFieldValue("actionId"));
		WorkflowActionDb wa = new WorkflowActionDb();
		wa = wa.getStartAction(flowId);
		if (wa.getId()==actionId) {
			// 检查人员编号是否已存在
			String personno = fu.getFieldValue("person_no");
			if (isPersonNoExist(personno)) {
				throw new ErrMsgException("人员编号已存在！");
			}
		}
		
		return true;
	}

	@Override
	public void onActionFinished(HttpServletRequest arg0, int arg1,
			FileUpload arg2) {
		// TODO Auto-generated method stub
		
	}
}
