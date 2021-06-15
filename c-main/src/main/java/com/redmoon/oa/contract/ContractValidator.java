package com.redmoon.oa.contract;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

public class ContractValidator implements IFormValidator {

	Logger logger = Logger.getLogger(ContractValidator.class.getName());

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
		int flowId = wf.getId();
		ContractMgr cmr = new ContractMgr();
		cmr.createContract(flowId);
		
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
	public boolean validate(HttpServletRequest arg0, FileUpload arg1, int arg2,
			Vector arg3) throws ErrMsgException {
		// TODO Auto-generated method stub
		return true;
	}

}
