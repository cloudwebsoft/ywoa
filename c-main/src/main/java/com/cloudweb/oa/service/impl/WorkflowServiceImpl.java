package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.service.WorkflowService;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.shell.BSHShell;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.LocalUtil;
import com.cloudweb.oa.utils.ThreadContext;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FormUtil;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JSONObject finishAction(HttpServletRequest request, Privilege privilege) throws ErrMsgException {
        boolean re;
        WorkflowMgr wfm = new WorkflowMgr();
        JSONObject json = new JSONObject();

        try {
            wfm.doUpload(request.getServletContext(), request);
        } catch (ErrMsgException e) {
            // 可能会抛出验证非法的错误信息
            e.printStackTrace();
            DebugUtil.e(getClass(), "doUpload", e.getMessage());

            json.put("ret", "0");
            json.put("msg", e.getMessage());
            json.put("op", "");
            return json;
        }

        request.setAttribute("workflowParams", new WorkflowParams(request, wfm.getFileUpload()));

        String op = wfm.getFieldValue("op");
        String strFlowId = wfm.getFieldValue("flowId");
        int flowId = Integer.parseInt(strFlowId);
        String strActionId = wfm.getFieldValue("actionId");
        int actionId = Integer.parseInt(strActionId);
        String strMyActionId = wfm.getFieldValue("myActionId");
        long myActionId = Long.parseLong(strMyActionId);

        WorkflowDb wf = wfm.getWorkflowDb(flowId);

        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb(actionId);
        if (!wa.isLoaded()) {
            // out.print(SkinUtil.makeErrMsg(request, "没有正在办理的节点！"));
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "notBeingHandle");
            json.put("ret", "0");
            json.put("op", op);
            json.put("msg", str);
            return json;
        }

        MyActionDb myActionDb = new MyActionDb();
        myActionDb = myActionDb.getMyActionDb(myActionId);
        if (myActionDb.getCheckStatus() == MyActionDb.CHECK_STATUS_PASS) {
            json.put("ret", "0");
            json.put("op", op);
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "noNeedToDealWith");
            json.put("msg", str);
            return json;
        } else if (myActionDb.getCheckStatus() == MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
            json.put("ret", "0");
            json.put("op", op);
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "upcomingProcess");
            json.put("msg", str);
            return json;
        }

        String result = wfm.getFieldValue("cwsWorkflowResult");
        myActionDb.setResult(result);
        /*
        // 如果FinishAction在处理时抛出了异常，则不能置状态为checked，否则回到待办记录列表后，会找不到此记录
        if(op!= null && !op.trim().equals("saveformvalue") && !op.trim().equals("saveformvalueBeforeXorCondSelect")){
             myActionDb.setChecked(true);
        }
        */

        myActionDb.save();

        // 退回
        if ("return".equals(op)) {
            re = wfm.ReturnAction(request, wf, wa, myActionId);
            if (re) {
                json.put("ret", "1");
                json.put("op", op);
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                json.put("msg", str);
            } else {
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
            }
            return json;
        }

        if ("finish".equals(op) || "AutoSaveArchiveNodeCommit".equals(op)) {
            try {
                wfm.checkLock(request, wf);
            } catch (ErrMsgException e1) {
                myActionDb.setChecked(false);
                myActionDb.save();
                json.put("ret", "0");
                json.put("msg", e1.getMessage());
                json.put("op", op);
                return json;
            }
            re = wfm.FinishAction(request, wf, wa, myActionId);
            if (re) {
                // 自动存档
                if ("AutoSaveArchiveNodeCommit".equals(op)) {
                    re = wfm.autoSaveArchive(request, wf, wa);
                }

                // 如果后继节点中有一个节点是由本人继续处理，且已处于激活状态，则继续处理这个节点
                MyActionDb mad = wa.getNextActionDoingWillBeCheckedByUserSelf(privilege.getUser(request));

                op = "finish";

                if (mad != null) {
                    json.put("ret", "1");
                    json.put("op", op);
                    json.put("nextMyActionId", "" + mad.getId());
                    String str = LocalUtil.LoadString(request, "res.flow.Flow", "clickOk");
                    json.put("msg", str);
                    return json;
                } else {
                    json.put("ret", "1");
                    json.put("op", op);
                    json.put("nextMyActionId", "");
                    String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                    json.put("msg", str);
                    return json;
                }
            } else {
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
                return json;
            }
        }
        if ("read".equals(op)) {
            re = wfm.read(request, actionId, myActionId);
            if (re) {
                json.put("ret", "1");
                json.put("op", op);
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                json.put("msg", str);
            } else {
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
            }
            return json;
        }
        if ("manualFinish".equals(op) || "AutoSaveArchiveNodeManualFinish".equals(op) || "manualFinishAgree".equals(op)) {
            re = wfm.saveFormValue(request, wf, wa);
            if (re) {
                re = wfm.ManualFinish(request, flowId, myActionId);
            }
            if (re) {
                json.put("ret", "1");
                json.put("op", op);
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                json.put("msg", str);
            } else {
                // out.print(StrUtil.Alert_Back("操作失败！"));
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
            }
            return json;
        }

        // 自动存档前先保存数据，然后获取flow_displose.jsp中iframe中的report表单数据在 办理完毕 时存档
        if ("editFormValue".equals(op) || "saveformvalue".equals(op) || "saveformvalueBeforeXorCondSelect".equals(op)) {
            // 2013-06-29 fgf 注意保存草稿已经不再进行有效性验证
            re = wfm.saveFormValue(request, wf, wa);
            if (re) {
                json.put("ret", "1");
                json.put("op", op);
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                json.put("msg", str);
            } else {
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
            }
        }

        return json;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JSONObject finishActionFree(HttpServletRequest request, Privilege privilege) throws ErrMsgException {
        WorkflowMgr wfm = new WorkflowMgr();
        JSONObject json = new JSONObject();
        try {
            wfm.doUpload(request.getServletContext(), request);
        } catch (ErrMsgException e) {
            e.printStackTrace();
            DebugUtil.e(getClass(), "doUpload", e.getMessage());

            json.put("ret", "0");
            json.put("msg", e.getMessage());
            json.put("op", "");
            return json;
        }

        String op = wfm.getFieldValue("op");
        String strFlowId = wfm.getFieldValue("flowId");
        int flowId = Integer.parseInt(strFlowId);
        String strActionId = wfm.getFieldValue("actionId");
        int actionId = Integer.parseInt(strActionId);
        String strMyActionId = wfm.getFieldValue("myActionId");
        long myActionId = Long.parseLong(strMyActionId);

        WorkflowDb wf = wfm.getWorkflowDb(flowId);

        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb(actionId);
        if (!wa.isLoaded()) {
            String str = LocalUtil.LoadString(request, "res.flow.Flow", "notBeingHandle");
            json.put("ret", "0");
            json.put("msg", str);
            json.put("op", op);
            return json;
        }

        MyActionDb myActionDb = new MyActionDb();
        myActionDb = myActionDb.getMyActionDb(myActionId);
        String result = wfm.getFieldValue("cwsWorkflowResult");
        myActionDb.setResult(result);
        if (op != null && !"saveformvalue".equals(op.trim())) {
            myActionDb.setChecked(true);
        }
        myActionDb.save();

        if ("return".equals(op)) {
            boolean re = wfm.ReturnAction(request, wf, wa, myActionId);
            if (re) {
                json.put("ret", "1");
                json.put("op", op);
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                json.put("msg", str);
                return json;
            } else {
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
                return json;
            }
        } else if ("finish".equals(op)) {
            try {
                wfm.checkLock(request, wf);
            } catch (ErrMsgException e1) {
                myActionDb.setChecked(false);
                myActionDb.save();
                json.put("ret", "0");
                json.put("msg", e1.getMessage());
                json.put("op", op);
                return json;
            }
            boolean re = wfm.FinishActionFree(request, wf, wa, myActionId);
            if (re) {
                // 如果后继节点中有一个节点是由本人继续处理，且已处于激活状态，则继续处理这个节点
                MyActionDb mad = wa.getNextActionDoingWillBeCheckedByUserSelf(privilege.getUser(request));
                if (mad != null) {
                    // out.print(StrUtil.Alert_Redirect("操作成功！请点击确定，继续处理下一节点！", "flow_dispose_free.jsp?myActionId=" + mad.getId()));
                    json.put("ret", "1");
                    json.put("op", op);
                    json.put("nextMyActionId", "" + mad.getId());
                    String str = LocalUtil.LoadString(request, "res.flow.Flow", "clickOk");
                    json.put("msg", str);
                    return json;
                } else {
                    json.put("ret", "1");
                    json.put("op", op);
                    json.put("nextMyActionId", "");
                    String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                    json.put("msg", str);
                    return json;
                }
            } else {
                // out.print(StrUtil.Alert_Redirect("操作失败！", "flow_dispose_free.jsp?myActionId=" + myActionId));
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
                return json;
            }
        } else if ("manualFinish".equals(op)) {
            boolean re = wfm.saveFormValue(request, wf, wa);
            if (re) {
                re = wfm.ManualFinish(request, flowId, myActionId);
                if (re) {
                    myActionDb.setResultValue(WorkflowActionDb.RESULT_VALUE_DISAGGREE);
                    myActionDb.save();
                }
            }

            if (re) {
                json.put("ret", "1");
                json.put("op", op);
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                json.put("msg", str);
                return json;
            } else {
                json.put("ret", "0");
                String str = LocalUtil.LoadString(request, "res.common", "info_op_fail");
                json.put("msg", str);
                json.put("op", op);
                return json;
            }
        }
        // 保存草稿
        else if ("saveformvalue".equals(op)) {
            boolean re = wfm.saveFormValue(request, wf, wa);
            // afterXorCondNodeCommit通知flow_dispose.jsp页面，已保存完毕，匹配条件后，自动重定向
            if (re) {
                json.put("ret", "1");
                json.put("op", op);
                String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
                json.put("msg", str);
                return json;
            }
        }
        return json;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JSONObject createNestSheetRelated(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String formCode = ParamUtil.get(request, "formCode");
        String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);

        String moduleCode = ParamUtil.get(request, "moduleCode");
        if ("".equals(moduleCode)) {
            moduleCode = formCodeRelated;
        }

        String relateFieldValue = "" + com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID;
        int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
        if (parentId==-1) {
            ModuleRelateDb mrd = new ModuleRelateDb();
            mrd = mrd.getModuleRelateDb(formCode, moduleCode);
            if (mrd==null) {
                json.put("ret","0");
                json.put("msg", "请检查模块是否相关联");
                return json;
            }
        }
        else {
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
            relateFieldValue = fdm.getRelateFieldValue(parentId, moduleCode);
            if (relateFieldValue==null) {
                json.put("ret","0");
                json.put("msg", "请检查模块是否相关联");
                return json;
            }
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCode);
        if (msd==null) {
            json.put("ret","0");
            json.put("msg", "模块不存在");
            return json;
        }

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");
        ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
        if (isNestSheetCheckPrivilege && !mpd.canUserAppend(privilege.getUser(request))) {
            json.put("ret","0");
            json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return json;
        }

        long actionId = ParamUtil.getLong(request, "actionId", -1);
        request.setAttribute("actionId", String.valueOf(actionId));

        // 用于区分嵌套表是在流程还是智能模块
        boolean isVisual = false;
        boolean re = false;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            if ("project".equals(formCode) && "project_members".equals(formCodeRelated)) {
                re = fdm.createPrjMember(request.getServletContext(), request);
            } else {
                re = fdm.create(request.getServletContext(), request, msd);
            }
        }
        catch (ErrMsgException e) {
            e.printStackTrace();
            json.put("ret","0");
            json.put("msg", e.getMessage());
            return json;
        }
        if (re) {
            String[] fields = msd.getColAry(false, "list_field");

            int len = 0;
            if (fields!=null) {
                len = fields.length;
            }
            StringBuilder tds = new StringBuilder();
            String token = "#@#";
            // int cwsId = ParamUtil.getInt(request, "cws_id", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
            int cwsId = StrUtil.toInt(fdm.getFieldValue("cws_id"), com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
            // 在智能模块中添加操作时，添加嵌套表格2中的记录
            if (cwsId==com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID) {
                com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO();
                RequestUtil.setFormDAO(request, fdao);
                for (int i=0; i<len; i++) {
                    String fieldName = fields[i];
                    String v = StrUtil.getNullStr(fdao.getFieldHtml(request, fieldName)); // fdao.getFieldValue(fieldName);
                    if (i==0) {
                        tds = new StringBuilder(v);
                    }
                    else {
                        tds.append(token).append(v);
                    }
                }
                isVisual = true;
            }
            else {
                com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO();
                RequestUtil.setFormDAO(request, fdao);
                for (int i=0; i<len; i++) {
                    String fieldName = fields[i];
                    String v = StrUtil.getNullStr(fdao.getFieldHtml(request, fieldName)); // fdao.getFieldValue(fieldName);
                    if (i==0) {
                        tds = new StringBuilder(v);
                    } else {
                        tds.append(token).append(v);
                    }
                }
            }
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("isVisual",isVisual);
            json.put("token",token);
            json.put("tds", tds.toString());
            json.put("fdaoId",fdm.getVisualObjId());

            FormDb pForm = new FormDb();
            pForm = pForm.getFormDb(formCode);
            json.put("sums", JSONObject.parse(FormUtil.getSums(fd, pForm, String.valueOf(parentId)).toString()));
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JSONObject updateNestSheetRelated(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

        String formCode = ParamUtil.get(request, "formCode"); // 主模块编码
        if ("".equals(formCode)) {
            json.put("ret", "0");
            json.put("msg", "编码不能为空！");
            return json;
        }

        String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

        Privilege privilege = new Privilege();
        ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
        if (isNestSheetCheckPrivilege && !mpd.canUserManage(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
            return json;
        }

        long actionId = ParamUtil.getLong(request, "actionId", -1);
        request.setAttribute("actionId", String.valueOf(actionId));

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);

        long id = ParamUtil.getLong(request, "id", -1);
        if (id == -1) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "err_id"));
            return json;
        }

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);

        String moduleCode = ParamUtil.get(request, "moduleCode");
        if ("".equals(moduleCode)) {
            moduleCode = formCodeRelated;
        }
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCode);

        // 用于区分嵌套表是在流程还是智能模块
        boolean isVisual;

        boolean re;
        try {
            re = fdm.update(request.getServletContext(), request, msd);
        }
        catch (ErrMsgException e) {
            json.put("ret","0");
            json.put("msg", e.getMessage());
            return json;
        }
        if (re) {
            StringBuilder tds = new StringBuilder();
            String token = "#@#";
            if (fdao.getCwsId().equals("" + com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID) || fdao.getCwsId().equals("" + com.redmoon.oa.visual.FormDAO.NAME_TEMP_CWS_IDS)) {
                String[] fields = msd.getColAry(false, "list_field");

                int len = 0;
                if (fields!=null) {
                    len = fields.length;
                }
                fdao = fdm.getFormDAO(id);

                for (int i=0; i<len; i++) {
                    String fieldName = fields[i];
                    String v = fdao.getFieldHtml(request, fieldName); // fdao.getFieldValue(fieldName);
                    if (i==0) {
                        tds = new StringBuilder(v);
                    } else {
                        tds.append(token).append(v);
                    }
                }
                isVisual = true;
            }
            else {
                isVisual = false;
            }
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("isVisual",isVisual);
            json.put("token",token);
            json.put("tds", tds.toString());
        }
        else{
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public JSONObject runFinishScript(HttpServletRequest request, int flowId, int actionId) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        BSHShell shell = null;
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);

        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());

        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(flowId, fd);

        WorkflowActionDb wa = new WorkflowActionDb();
        wa = wa.getWorkflowActionDb(actionId);

        WorkflowPredefineDb wpd = new WorkflowPredefineDb();
        wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
        WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
        String script = wpm.getOnFinishScript(wpd.getScripts());

        if (script != null && !"".equals(script.trim())) {
            shell = wf.runFinishScript(request, wf, fdao, wa, script, true);
        }

        json.put("ret", "1");
        if (shell == null) {
            json.put("msg", "请检查脚本是否存在！");
        } else {
            String errDesc = shell.getConsole().getLogDesc();
            json.put("msg", StrUtil.toHtml(errDesc));
        }
        return json;
    }
}
