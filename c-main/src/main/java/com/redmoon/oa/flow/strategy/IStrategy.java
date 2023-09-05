package com.redmoon.oa.flow.strategy;

import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;

public interface IStrategy {
    Vector<UserDb> selectUser(Vector<UserDb> userVector);
    
    boolean onActionFinished(HttpServletRequest request, WorkflowDb wf, MyActionDb mad);

    /**
     * 分配策略中的人員是否可以由前一節點人員選擇
     * @return
     */
	boolean isSelectable();
	
	void setX(int x);
	
	void validate(HttpServletRequest request, WorkflowDb wf, WorkflowActionDb myAction, long myActionId, FileUpload fu, String[] userNames, WorkflowActionDb nextAction) throws ErrMsgException;
	
	/**
	 * 默认全部人员是否选中
	 * @return
	 */
	boolean isSelected();
	
	/**
	 * 是否为下达模式，如果是自选用户，下一节点中已存在被其他人选择的用户，则当返回为true时不能对其变动
	 * @return
	 */
	boolean isGoDown();
	
}
