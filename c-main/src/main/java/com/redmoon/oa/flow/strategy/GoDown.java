package com.redmoon.oa.flow.strategy;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

import cn.js.fan.util.ErrMsgException;

public class GoDown implements IStrategy {
	public GoDown() {
	}

	/**
	 * 全部用戶
	 */
	@Override
    public Vector selectUser(Vector userVector) {
		return userVector;
	}
	
	/**
	 * 處理者是否可以選擇
	 * @return
	 */
	@Override
	public boolean isSelectable() {
		return true;
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
	
	/**
     * 是否为下达模式，如果是，则自选用户时，不能勾选其他人已选的下一节点的用户
	 * @return
	 */
	@Override
    public boolean isGoDown() {
		return true;
	}
}
