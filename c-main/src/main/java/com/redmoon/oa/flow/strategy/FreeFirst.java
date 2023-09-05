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
 
public class FreeFirst implements IStrategy {
    public FreeFirst() {
    }

    @Override
    public Vector selectUser(Vector userVector) {
        UserDb user = null;
        Iterator ir = userVector.iterator();

        // 取得用户的侍办工作条数，选择待办数量最少的用户
        long minCount = 900000;
        MyActionDb mad = new MyActionDb();
        while (ir.hasNext()) {
            UserDb ud = (UserDb)ir.next();
            long userCount = mad.getActionAccessByUserCount(ud.getName());
            if (minCount >= userCount) {
                user = ud;
                minCount = userCount;
            }
        }
        Vector v = new Vector();        
        if (user==null) {
            user = (UserDb)userVector.get(0);
            v.addElement(user);
        }
        return v;
    }

    @Override
    public boolean onActionFinished(HttpServletRequest request, WorkflowDb wf, MyActionDb mad) {
    	return true;
    }
    
	@Override
    public boolean isSelectable() {
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
