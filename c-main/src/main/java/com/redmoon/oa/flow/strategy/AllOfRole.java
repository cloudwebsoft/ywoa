package com.redmoon.oa.flow.strategy;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;

public class AllOfRole implements IStrategy {
	public AllOfRole() {
	}

	/**
	 * 全部用戶
	 */
	@Override
    public Vector selectUser(Vector userVector) {
		return userVector;
	}
	
	/**
	 * 是否處理者可以選擇
	 * @return
	 */
	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public boolean onActionFinished(HttpServletRequest request, WorkflowDb wf,
									MyActionDb mad) {
		return true;
	}

	@Override
	public void setX(int x) {
		
	}
	
	@Override
	public void validate(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction, long myActionId, FileUpload fu, String[] userNames, WorkflowActionDb nextAction) throws ErrMsgException {
		
	}
	
	/**
	 * 默认全部人员是否选中
	 */
	@Override
	public boolean isSelected() {
		return false;
	}	
	
	@Override
    public boolean isGoDown() {
		return false;
	}	
}
