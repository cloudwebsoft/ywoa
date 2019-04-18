package com.redmoon.oa.flow.strategy;

import java.util.Vector;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

public class OnlyOne implements IStrategy {
    public OnlyOne() {
    }

    public Vector selectUser(Vector userVector) {
        return userVector;
    }

    public boolean onActionFinished(HttpServletRequest request, WorkflowDb wf, MyActionDb mad) {
    	Iterator ir = mad.getOthersOfActionDoing().iterator();
    	while (ir.hasNext()) {
    		MyActionDb ma = (MyActionDb)ir.next();
    		ma.setCheckStatus(MyActionDb.CHECK_STATUS_PASS);
    		ma.setCheckDate(new java.util.Date());
    	    ma.setChecked(true);
    		ma.save();
    	}
    	return true;
    }
    
    /**
     * 用户是否可勾选，置为false表示用户默认选中，且不可去掉勾选
     */
	public boolean isSelectable() {
		return false;
	}
	
	public void setX(int x) {
		
	}
	
	public void validate(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction, long myActionId, FileUpload fu, String[] userNames, WorkflowActionDb nextAction) throws ErrMsgException {
		
	}	
	
	/**
	 * 默认全部人员是否选中
	 */
	public boolean isSelected() {
		return false;
	}		

	public boolean isGoDown() {
		return false;
	}	
}
