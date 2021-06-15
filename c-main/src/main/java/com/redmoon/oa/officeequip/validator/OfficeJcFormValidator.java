package com.redmoon.oa.officeequip.validator;

import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.officeequip.OfficeOpDb;
import com.redmoon.oa.officeequip.OfficeStocktakingDb;

public class OfficeJcFormValidator implements IFormValidator {

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
		String officeName = fdao.getFieldValue("officeName");
		String count = fdao.getFieldValue("borrowCount");
		String person = fdao.getFieldValue("person");
		String opDate = fdao.getFieldValue("opDate");
		String abstracts = fdao.getFieldValue("abstracts");

		if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {
			OfficeStocktakingDb osd = new OfficeStocktakingDb();
			osd.changeStocknum(Integer.parseInt(count), officeName);
			OfficeOpDb office = new OfficeOpDb();
			office.setOfficeCode(officeName);
			office.setCount(Integer.parseInt(count));
			office.setType(OfficeOpDb.TYPE_BORROW);
			Date d = null;
			if (opDate.equals("")) {
				d = new Date();
			} else {
				d = DateUtil.parse(opDate, "yyyy-MM-dd");
			}
			office.setOpDate(d);
			office.setPerson(person);
			office.setRemark(abstracts);
			office.setFlowid(wf.getId());
			office.create();
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

	private int isCountOverload(String count, String code) {
		int iCount = Integer.parseInt(count);

		String sql = "select stock_num from office_stocktaking where equipment_code=? and is_tookstock=0";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[] { code });
			ResultRecord rr1 = null;
			if (ri.hasNext()) {
				rr1 = (ResultRecord) ri.next();
				if (rr1.getInt(1) >= iCount) {
					return 0;
				}
				return 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 2;
	}

	@Override
	public boolean validate(HttpServletRequest request, FileUpload fu,
			int flowId, Vector fields) throws ErrMsgException {
		// 检查数量是否超过库存
		int actionId = StrUtil.toInt(fu.getFieldValue("actionId"));
		WorkflowActionDb wa = new WorkflowActionDb();
		wa = wa.getStartAction(flowId);
		if (wa.getId() == actionId) {
			String count = fu.getFieldValue("borrowCount");
			String code = fu.getFieldValue("officeName");
			TreeSelectDb tsd = new TreeSelectDb(code);
			if (tsd == null || !tsd.isLoaded() || tsd.getChildCount() > 0) {
				throw new ErrMsgException("请选择办公用品！");
			}
			if(tsd.getChildCount() > 0)
			{
				throw new ErrMsgException("请选择子节点的办公用品！");
			}
			int i = isCountOverload(count, code);
			if (i == 1) {
				throw new ErrMsgException("该办公用品库存不够！");
			}
			if (i == 2) {
				throw new ErrMsgException("该办公用品还未入库！");
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
