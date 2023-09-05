package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.shell.BSHShell;

import javax.servlet.http.HttpServletRequest;

public interface IWorkflowScriptUtil {

    BSHShell runFinishScript(HttpServletRequest request, WorkflowDb wfd, FormDAO fdao, WorkflowActionDb lastAction, String script, boolean isTest) throws ErrMsgException;

    BSHShell runDeliverScript(HttpServletRequest request, String curUserName, WorkflowDb wf, FormDAO fdao, MyActionDb mad, String script, boolean isTest, FileUpload fu) throws ErrMsgException;

    BSHShell runReturnScript(HttpServletRequest request, String curUserName, int flowId, FormDAO fdao, String script, FileUpload fu) throws ErrMsgException;

    BSHShell runDiscardScript(HttpServletRequest request, String curUserName, int flowId, FormDAO fdao, String script, FileUpload fu) throws ErrMsgException;

    BSHShell runPreInitScript(HttpServletRequest request, String curUserName, int flowId, String script, FormDAO fdao) throws ErrMsgException;

    BSHShell runPreDisposeScript(HttpServletRequest request, String curUserName, WorkflowDb wf, FormDAO fdao, MyActionDb mad, String script) throws ErrMsgException;

    BSHShell runDeleteValidateScript(HttpServletRequest request, Privilege pvg, WorkflowDb wf, FormDAO fdao, WorkflowActionDb action, boolean isTest) throws ErrMsgException;

    boolean recallMyAction(HttpServletRequest request, long myActionId) throws ErrMsgException;

    BSHShell runValidateScript(HttpServletRequest request, Privilege pvg, WorkflowDb wf, FormDAO fdao, WorkflowActionDb action, boolean isTest, FileUpload fu) throws ErrMsgException;

    BSHShell runActiveScript(HttpServletRequest request, WorkflowDb wf, long myActionId, WorkflowActionDb actionActived, boolean isTest) throws ErrMsgException;
}
