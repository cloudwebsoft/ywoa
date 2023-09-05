package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

public interface IFlowRender {

    String report(WorkflowDb wf, FormDb fd, boolean isHideField);

    String reportForView(WorkflowDb wf, FormDb fd, int formViewId, boolean isHideField);

    String reportForArchive(WorkflowDb wf, FormDb fd, IFormDAO fdao);

    String rendFree(WorkflowDb wf, FormDb fd, WorkflowActionDb wfa);

    String rend(WorkflowDb wf, FormDb fd, WorkflowActionDb wfa, boolean isForFormEdit) throws ErrMsgException;

    String rendForNestCtl(HttpServletRequest request, String formCode, WorkflowActionDb wfa);

    String rendScriptByAction(int actionId, boolean canWriteAll);

    String reportScript(FormDb fd, FormDAO fdao);
}
