package com.redmoon.oa.flow;

import com.redmoon.oa.person.UserDb;
import cn.js.fan.util.ErrMsgException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowActionChecker {
    private String errMsg = "";
    public WorkflowActionChecker() {
    }

    public boolean check(WorkflowActionDb wad) {
        if (wad.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT) || wad.getJobCode().equals(WorkflowActionDb.PRE_TYPE_USER_SELECT_IN_ADMIN_DEPT)) {
            return true;
        }
        if (wad.getNodeMode()== WorkflowActionDb.NODE_MODE_USER) {
            // 检查用户是否存在
            //
        }
        return true;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
