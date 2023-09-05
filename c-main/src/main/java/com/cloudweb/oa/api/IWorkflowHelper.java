package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowLinkDb;

public interface IWorkflowHelper {

    boolean renewWorkflowString(WorkflowDb wf, WorkflowActionDb wad, boolean isSaveFlow) throws ErrMsgException;

    boolean fromString(WorkflowActionDb wa, String str, boolean isCheck) throws ErrMsgException;

    boolean fromString(WorkflowLinkDb workflowLinkDb, String str) throws ErrMsgException;

    String tran(String str);
}
