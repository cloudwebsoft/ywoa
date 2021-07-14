package com.redmoon.oa.officeequip.validator;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.officeequip.OfficeOpDb;
import com.redmoon.oa.officeequip.OfficeStocktakingDb;

public class OfficeGhFormValidator implements IFormValidator {

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
		String officeCode = fdao.getFieldValue("officeCode");
		String count = fdao.getFieldValue("borrowCount");
		String equipId = fdao.getFieldValue("equip_id");

		if (wf.getStatus() == WorkflowDb.STATUS_FINISHED) {

			// 修改盘点表记录
			OfficeStocktakingDb osd = new OfficeStocktakingDb();
			osd.returnChangeStocknum(StrUtil.toInt(count, 0), officeCode);

			// 修改操作表中的记录状态字段为归还
			OfficeOpDb ooDb = new OfficeOpDb(StrUtil.toInt(equipId, 0));
			if (ooDb != null && ooDb.isLoaded()) {
				ooDb.setReturnDate(DateUtil.parse(
						fdao.getFieldValue("endDate"), "yyyy-MM-dd"));
				ooDb.setType(OfficeOpDb.TYPE_RETURN);
				ooDb.setRemark(fdao.getFieldValue("abstracts"));
				ooDb.save();
			}
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

	@Override
	public boolean validate(HttpServletRequest request, FileUpload fu,
			int flowId, Vector fields) throws ErrMsgException {
		return true;
	}

	@Override
	public void onActionFinished(HttpServletRequest arg0, int arg1,
			FileUpload arg2) {
		// TODO Auto-generated method stub

	}
}
