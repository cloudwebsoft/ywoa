package com.redmoon.oa.prj;

import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-1-24下午04:10:38
 */
public class PrjDelayedValidator implements IFormValidator {

	Logger logger = Logger.getLogger(PrjDelayedValidator.class.getName());

	public PrjDelayedValidator() {
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
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(wf.getId(), fd);

		String prjId = fdao.getFieldValue("prj_id");
		Date dt = DateUtil.parse(fdao.getFieldValue("delayed_time"), "yyyy-MM-dd HH:mm:ss");
		
		String sql = "update form_table_prj set prj_enddate=? where id="+prjId;
		try {
			JdbcTemplate jt = new JdbcTemplate();
			jt.executeUpdate(sql, new Object[]{dt});
			
			sql = "update form_table_prj_taskcontrol set pre_time=? where prj_id=? and task_id=''";
			jt.executeUpdate(sql, new Object[]{dt, prjId});			
		} catch (SQLException e) {
			e.printStackTrace();
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
