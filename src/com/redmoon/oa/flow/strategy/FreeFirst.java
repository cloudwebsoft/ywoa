package com.redmoon.oa.flow.strategy;

import org.apache.log4j.Logger;
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

    public boolean onActionFinished(HttpServletRequest request, WorkflowDb wf, MyActionDb mad) {
    	return true;
    }
    
	public boolean isSelectable() {
		return true;
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
