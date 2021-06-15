package com.redmoon.oa.sale;

import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.prj.PrjDelayedValidator;

/**
 * @Description: 销售日报校验
 * @author: 
 * @Date: 2016-3-29上午10:26:46
 */
public class SalesDailyReportValidator  implements IFormValidator {

	Logger logger = Logger.getLogger(PrjDelayedValidator.class.getName());

	public SalesDailyReportValidator() {
	}

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
		
		// 取得嵌套表格中的数据
		// 遍历借款明细表，cws_id中存储的是流程主表单的
		String sql = "select * from form_table_sales_daily_lxr where cws_id=" + fdao.getId();
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
			    String lxr = rr.getString("lxr");
			    String customer = rr.getString("customer");
			    String contact_purpose = rr.getString("contact_purpose");
			    String solution = rr.getString("solution");
			    String contact_type = rr.getString("contact_type");
			    String sales_person = rr.getString("sales_person");
			    //String contact_result = rr.getString("contact_result");
			    
			    //sql = "insert into form_table_day_lxr (lxr,customer,contact_purpose,solution,contact_type,cws_id,unit_code, cws_creator, cws_status, flowId,contact_result) values (?,?,?,?,?,?,?,?,?,-1,?)";
			    //jt.executeUpdate(sql, new Object[]{lxr,customer,contact_purpose,solution,contact_type,lxr, wf.getUnitCode(), wf.getUserName(), FormDAO.STATUS_DONE, contact_result}); //
			    
			    sql = "insert into form_table_day_lxr (lxr,customer,contact_purpose,solution,contact_type,cws_id,unit_code, cws_creator, cws_status, flowId, sales_person) values (?,?,?,?,?,?,?,?,?,-1,?)";
			    jt.executeUpdate(sql, new Object[]{lxr,customer,contact_purpose,solution,contact_type,lxr, wf.getUnitCode(), wf.getUserName(), FormDAO.STATUS_DONE, sales_person}); //
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
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
