package com.redmoon.oa.flow.strategy;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;


import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

public class OnlySelectOne implements IStrategy {
	private int x;

	public OnlySelectOne() {
		
	}

	/**
	 * 在matchActionUser中预先匹配人员
	 */
	@Override
    public Vector selectUser(Vector userVector) {
		// 如果此处不返回null，则因为在matchActionUser会置WorkflowActionDb中的userNames，会使得userVector所有的人员会处理，而不是选中的人员
		return null;
	}
	
	/**
	 * 是否處理者可以選擇
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
		this.x = x;
	}
	
	@Override
	public void validate(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction, long myActionId, FileUpload fu, String[] userNames, WorkflowActionDb nextAction) throws ErrMsgException {
		if (userNames==null) {
        	throw new ErrMsgException("请选择一个用户");			
		}
		if (userNames.length>1) {
        	throw new ErrMsgException("只能选择一个用户");
        }
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
