package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.api.INestSheetCtl;
import com.cloudweb.oa.api.IWorkflowUtil;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.FlowService;
import com.cloudweb.oa.service.MacroCtlService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FuncUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Vector;

@Slf4j
@Service
public class FlowServiceImpl implements FlowService {

    @Autowired
    HttpServletRequest request;

    @Autowired
    private IWorkflowUtil workflowUtil;

    @Autowired
    AuthUtil authUtil;

    public JSONObject init(MyActionDb mad, String mutilDept) {
        JSONArray fields = new JSONArray();
        JSONObject result = new JSONObject();
        boolean hasAttach = true;
        FormDb fd = new FormDb();

        String res = "0";
        String msg = "操作成功";
        int flowId = (int)mad.getFlowId();
        MacroCtlMgr mm = new MacroCtlMgr();
        JSONObject json = new JSONObject();
        try {
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Leaf lf = new Leaf();
            lf = lf.getLeaf(wf.getTypeCode());
            fd = fd.getFormDb(lf.getFormCode());
            hasAttach = fd.isHasAttachment();

            FormDAO fdao = new FormDAO();
            fdao = fdao.getFormDAO(wf.getId(), fd);

            // 扫码传值
            String scanActionType = ParamUtil.get(request, "scanActionType");
            if (ConstUtil.SCAN_ACTION_TYPE_FLOW.equals(scanActionType)) {
                String scanTargetField = ParamUtil.get(request, "scanTargetField");
                if (!StrUtil.isEmpty(scanTargetField)) {
                    String scanId = ParamUtil.get(request, "scanId");
                    fdao.setFieldValue(scanTargetField, String.valueOf(scanId));
                    // 此处得进行保存操作，因为下面表单域选择宏控件autoMap中会重新获取fdao，且后面在本方法中进行了fdao.load()操作
                    fdao.save();
                    DebugUtil.w(getClass(), "init", "scanId=" + scanId);
                } else {
                    DebugUtil.w(getClass(), "init", "scanTargetField 不存在");
                }
            }

            // 处理表单域自动映射
            Vector<FormField> v = fdao.getFields();
            Iterator<FormField> ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = ir.next();
                if ("macro".equals(ff.getType())) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu == null) {
                        log.error("MactoCtl " + ff.getTitle() + "：" + ff.getMacroType() + " is not exist.");
                        continue;
                    }

                    String macroCode = mu.getCode();
                    if ("module_field_select".equals(macroCode)) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                        try {
                            String value = StrUtil.getNullStr(ff.getValue());
                            if ("".equals(value) || value.equals(ff.getDefaultValueRaw()) || value.equals(ff.getDescription())) {
                                if (json.has("requestParam") && !"".equals(json.getString("requestParam"))) {
                                    // 来自于指定的参数名称
                                    value = ParamUtil.get(request, json.getString("requestParam"));
                                } else {
                                    // 默认以字段名作为参数从request中获取
                                    value = ParamUtil.get(request, ff.getName());
                                }
                            }
                            // 如果value中存在值，则说明需自动映射
                            if (!"".equals(value)) {
                                moduleFieldSelectCtl.autoMap(request, flowId, value, ff);
                            }
                        } catch (JSONException e) {
                            LogUtil.getLog(getClass()).error(e);
                        }
                    }
                }
            }

            // 重新再载入一次，以免出现缓存问题
            fdao.load();

            UserMgr um = new UserMgr();
            // 这段用来验证字段是否可写
            WorkflowActionDb wa = new WorkflowActionDb();
            int actionId = (int) mad.getActionId();
            wa = wa.getWorkflowActionDb(actionId);
            if (wa == null || !wa.isLoaded()) {
                res = "-1";
                msg = "流程中的相应动作不存在";
                json.put("res", res);
                json.put("msg", msg);
                return json;
            }

            WorkflowPredefineDb wfp = new WorkflowPredefineDb();
            wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

            String strIsShowNextUsers = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "isShowNextUsers");
            boolean isShowNextUsers = !"0".equals(strIsShowNextUsers);

            String strIsBtnSaveShow = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "isBtnSaveShow");
            boolean isBtnSaveShow = !"0".equals(strIsBtnSaveShow);

            String btnAgreeName = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "btnAgreeName");
            String btnRefuseName = WorkflowActionDb.getActionProperty(wfp, wa.getInternalName(), "btnRefuseName");

            // 取可写表单域
            String fieldWrite = StrUtil.getNullString(wa.getFieldWrite()).trim();
            String[] fds = fieldWrite.split(",");
            int len = fds.length;

            String userName = authUtil.getUserName();

            // 自由流程根据用户所属的角色，得到可写表单域
            if (lf.getType() == Leaf.TYPE_FREE) {
                WorkflowPredefineDb wfpd = new WorkflowPredefineDb();
                wfpd = wfpd.getPredefineFlowOfFree(wf.getTypeCode());

                fds = wfpd.getFieldsWriteOfUser(wf, userName);
                len = fds.length;
            }

            String fieldHide = StrUtil.getNullString(wa.getFieldHide()).trim();
            // 将不显示的字段加入fieldHide
            ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.getHide() == FormField.HIDE_EDIT || ff.getHide() == FormField.HIDE_ALWAYS) {
                    if ("".equals(fieldHide)) {
                        fieldHide = ff.getName();
                    } else {
                        fieldHide += "," + ff.getName();
                    }
                }
            }
            String[] fdsHide = fieldHide.split(",");
            int lenHide = fdsHide.length;

            MacroCtlUnit mu;
            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("sender", um.getUserDb(wf.getUserName()).getRealName());
            json.put("cwsWorkflowTitle", wf.getTitle());
            json.put("actionId", String.valueOf(actionId));
            json.put("myActionId", String.valueOf(mad.getId()));
            json.put("flowId", String.valueOf(flowId));
            json.put("url", "public/flow_dispose_do.jsp");
            json.put("formCode", fdao.getFormCode());

            json.put("isShowNextUsers", isShowNextUsers);
            json.put("isBtnSaveShow", isBtnSaveShow);
            json.put("btnAgreeName", btnAgreeName);
            json.put("btnRefuseName", btnRefuseName);

            // 是否为加签
            boolean canPlus = false;
            com.redmoon.oa.flow.FlowConfig conf = new com.redmoon.oa.flow.FlowConfig();
            if (conf.getIsDisplay("FLOW_BUTTON_PLUS") && wfp.isPlus()) {
                canPlus = true;
            }
            json.put("canPlus", canPlus);

            json.put("isFlowStarted", wf.isStarted());

            // fgf 20180814 显示控制
            String viewJs = workflowUtil.doGetViewJSMobile(request, fd, fdao, userName, false);
            json.put("viewJs", viewJs);

            WorkflowPredefineDb wfd = new WorkflowPredefineDb();
            wfd = wfd.getPredefineFlowOfFree(wf.getTypeCode());
            boolean isLight = wfd.isLight();
            json.put("isLight", isLight);// 判断时候是@liuchen
            json.put("isFree", lf.getType() == Leaf.TYPE_FREE);

            boolean canDel = false;
            if (conf.getIsDisplay("FLOW_BUTTON_DEL")) {
                if (WorkflowMgr.canDelFlowOnAction(request, wf, wa, mad)) {
                    canDel = true;
                }
            }
            json.put("canDel", canDel);

            // 结束流程标志
            boolean canFinishAgree = false;
            if (conf.getIsDisplay("FLOW_BUTTON_FINISH")) {
                String flag = wa.getFlag();
                if (wf.isStarted() && flag.length() >= 12 && flag.substring(11, 12).equals("1")) {
                    if (mad.getCheckStatus() != MyActionDb.CHECK_STATUS_SUSPEND) {
                        canFinishAgree = true;
                    }
                }
            }
            json.put("canFinishAgree", canFinishAgree);

            // 遍历表单字段-------------------------------------------------
            ir = v.iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();

                // 置可写表单域
                boolean finded = false;
                for (int i = 0; i < len; ++i) {
                    if (ff.getName().equals(fds[i])) {
                        finded = true;
                        break;
                    }
                }
                if (!finded) {
                    ff.setEditable(false);
                }

                // 如果不是自由流程
                if (lf.getType() != Leaf.TYPE_FREE) {
                    // 置隐藏表单域
                    finded = false;
                    for (int i = 0; i < lenHide; ++i) {
                        if (ff.getName().equals(fdsHide[i])) {
                            finded = true;
                            break;
                        }
                    }
                    if (finded) {
                        log.info(
                                "field:" + ff.getTitle() + " is hidden.");
                        ff.setHidden(true);
                    }
                }

                JSONObject field = new JSONObject();
                String desc = StrUtil.getNullStr(ff.getDescription());
                field.put("title", ff.getTitle());
                field.put("code", ff.getName());
                field.put("desc", desc);

                field.put("present", StrUtil.getNullStr(ff.getPresent()));
                // 如果是计算控件，则取出精度和四舍五入属性
                if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                    FormParser fp = new FormParser();
                    String isroundto5 = fp.getFieldAttribute(fd, ff, "isroundto5");
                    String digit = fp.getFieldAttribute(fd, ff, "digit");
                    field.put("formula", ff.getDefaultValueRaw());
                    field.put("isroundto5", isroundto5);
                    field.put("digit", digit);
                }
                String options = "";
                String macroType = "";
                String controlText = "";
                String controlValue = "";
                JSONArray opinionArr = null;
                JSONObject opinionVal = null;

                String macroCode = "";

                String metaData = "";

                JSONArray js = new JSONArray();
                if ("macro".equals(ff.getType())) {
                    mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu == null) {
                        log.error("MactoCtl " + ff.getTitle() + "：" + ff.getMacroType() + " is not exist.");
                        continue;
                    }

                    IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                    ifmc.setIFormDAO(fdao);

                    macroCode = mu.getCode();

                    macroType = ifmc.getControlType();
                    controlText = ifmc.getControlText(userName, ff);
                    controlValue = ifmc.getControlValue(userName, ff);

                    // 须放在此位置，因为在当前用户宏控件的getContextValue中更改了ff的值
                    metaData = ifmc.getMetaData(ff);

                    options = ifmc.getControlOptions(userName, ff);
                    // options = options.replaceAll("\\\"", "");
                    if (options != null && !"".equals(options)) {
                        // options = options.replaceAll("\\\"", "");
                        try {
                            js = new JSONArray(options);
                        } catch (JSONException e) {
                            DebugUtil.e(getClass(), "execute", ff.getTitle() + " macro ctl's options cann't convert to JSONArray," + options);
                        }
                    }
                } else {
                    String fieldType = ff.getType();
                    if (fieldType != null && !"".equals(fieldType)) {
                        if ("DATE".equals(fieldType) || "DATE_TIME".equals(fieldType)) {
                            // 有可能已经通过表单域选择宏控件传值映射带过来了值
                            if (ff.getValue() != null && !"".equals(ff.getValue())) {
                                controlValue = ff.getValue();
                            } else {
                                controlValue = ff.getDefaultValueRaw();
                            }
                        } else {
                            if (ff.getValue() != null && !"".equals(ff.getValue())) {
                                controlValue = ff.getValue();
                            } else {
                                controlValue = ff.getDefaultValue();
                            }
                        }
                    } else {
                        controlValue = ff.getDefaultValue();
                    }
                }
                // 判断是否为意见输入框
                if (macroCode != null && !macroCode.equals("")) {
                    if (macroCode.equals("macro_opinion") || macroCode.equals("macro_opinionex")) {
                        if (controlText != null
                                && !controlText.trim().equals("")) {
                            opinionArr = new JSONArray(controlText);
                        }
                        if (controlValue != null
                                && !controlValue.trim().equals("")) {
                            opinionVal = new JSONObject(controlValue);
                        }
                    }

                    if (macroCode.equals("nest_sheet") || macroCode.equals("nest_table") || macroCode.equals("macro_detaillist_ctl")) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        INestSheetCtl ntc = macroCtlService.getNestSheetCtl();
                        JSONObject jsonObj = ntc.getCtlDescription(ff);
                        if (jsonObj != null) {
                            field.put("desc", jsonObj);
                        }
                    } else if (macroCode.equals("module_field_select")) {
                        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
                        com.alibaba.fastjson.JSONObject jsonObj = moduleFieldSelectCtl.getCtlDescription(ff);
                        if (jsonObj != null) {
                            field.put("desc", jsonObj);
                        }
                    }
                }
                field.put("type", ff.getType());
                if (ff.getType().equals("select")) {
                    // options = fp.getOptionsOfSelect(fd, ff);
                    String[][] optionsArray = FormParser.getOptionsArrayOfSelect(fd, ff);
                    for (int i = 0; i < optionsArray.length; i++) {
                        String[] optionsItem = optionsArray[i];
                        if (optionsItem[0].equals(controlValue)) {
                            controlText = optionsItem[1];
                        }
                        JSONObject option = new JSONObject();
                        try {
                            option.put("value", optionsItem[1]);
                            option.put("name", optionsItem[0]);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error(e);
                        }
                        js.put(option);
                    }
                } else if (ff.getType().equals("radio")) {
                    // options = fp.getOptionsOfSelect(fd, ff);
                    String[] optionsArray = FormParser.getValuesOfInput(fd, ff);
                    for (int i = 0; i < optionsArray.length; i++) {
                        JSONObject option = new JSONObject();
                        option.put("value", optionsArray[i]);
                        option.put("name", optionsArray[i]);
                        js.put(option);
                    }
                }
                field.put("options", js);
                field.put("text", controlText);
                String level = "";
                if ("checkbox".equals(ff.getType())) {
                    // level = "个人兴趣";
                    level = ff.getTitle();
                }
                field.put("level", level);
                field.put("macroType", macroType);
                field.put("editable", String.valueOf(ff.isEditable()));
                field.put("isHidden", String.valueOf(ff.isHidden()));
                field.put("isNull", String.valueOf(ff.isCanNull()));
                field.put("fieldType", ff.getFieldType());
                if (opinionVal != null) {
                    field.put("value", opinionVal);
                } else {
                    field.put("value", controlValue);
                }
                if (opinionArr != null && opinionArr.length() > 0) {
                    field.put("text", opinionArr);
                } else {
                    field.put("text", controlText);
                }
                field.put("macroCode", macroCode);

                // 可传SQL控件条件中的字段
                field.put("metaData", metaData);
                field.put("isReadonly", ff.isReadonly());

                fields.put(field);
            }
            // 遍历表单字段---------------------------------------------------------

            // 置异或发散
            StringBuffer condBuf = new StringBuffer();
            boolean flagXorRadiate = wa.isXorRadiate();
            Vector vMatched = null;
            if (flagXorRadiate) {
                vMatched = WorkflowRouter.matchNextBranch(wa, userName, condBuf, mad.getId());
                String conds = condBuf.toString();
                boolean hasCond = !"".equals(conds); // 是否含有条件
                if (hasCond) {
                    flagXorRadiate = true;
                } else {
                    flagXorRadiate = false;
                }
            }
            json.put("flagXorRadiate", String.valueOf(flagXorRadiate));
            WorkflowRuler wr = new WorkflowRuler();
            // 取得下一步提交的用户--------------------------------------------------
            JSONArray users = new JSONArray();
            Iterator userir;
            Vector vto = wa.getLinkToActions();
            Iterator toir = vto.iterator();
            while (toir.hasNext()) {
                WorkflowActionDb towa = (WorkflowActionDb) toir.next();
                if (towa.getJobCode().equals( WorkflowActionDb.PRE_TYPE_USER_SELECT)) {
                    JSONObject user = new JSONObject();
                    user.put("actionTitle", towa.getTitle());
                    user.put("roleName", towa.getJobName());
                    user.put("internalname", towa.getInternalName());
                    user.put("name", "WorkflowAction_" + towa.getId());
                    user.put("value", WorkflowActionDb.PRE_TYPE_USER_SELECT);
                    user.put("realName", "自选用户");
                    user.put("isSelectable", "true");
                    // 标志位，能否选择用户
                    boolean canSelUser = wr.canUserSelUser(request, towa);
                    user.put("canSelUser", String.valueOf(canSelUser));

                    boolean isStragegyGoDown = towa.isStrategyGoDown(); // 是否为下达
                    user.put("isGoDown", String.valueOf(isStragegyGoDown));

                    users.put(user);
                } else {
                    WorkflowRouter workflowRouter = new WorkflowRouter();
                    Vector vuser = workflowRouter.matchActionUser(request, towa, wa, false, mutilDept);
                    userir = vuser.iterator();
                    boolean isStrategySelectable = towa.isStrategySelectable();
                    boolean isStrategySelected = towa.isStrategySelected();

                    while (userir != null && userir.hasNext()) {
                        UserDb ud = (UserDb) userir.next();
                        JSONObject user = new JSONObject();
                        user.put("actionTitle", towa.getTitle());
                        user.put("roleName", towa.getJobName());
                        user.put("internalname", towa.getInternalName());
                        user.put("name", "WorkflowAction_" + towa.getId());
                        user.put("value", ud.getName());
                        user.put("realName", ud.getRealName());
                        user.put("isSelectable", String.valueOf(isStrategySelectable));
                        user.put("isSelected", String.valueOf(isStrategySelected));

                        // 标志位，能否选择用户
                        boolean canSelUser = wr.canUserSelUser(request, towa);
                        user.put("canSelUser", String.valueOf(canSelUser));
                        // LogUtil.getLog(getClass()).info(getClass() +
                        // " 3 actionUserRealName=" + towa.getUserRealName() +
                        // " canSelUser=" + canSelUser);
                        users.put(user);
                    }
                }
            }
            result.put("users", users);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
            res = "-1";
            msg = "JSON解析异常";
            LogUtil.getLog(getClass()).error(e.getMessage());
        } catch (ErrMsgException e1) {
            res = "-1";
            msg = e1.getMessage();
            LogUtil.getLog(getClass()).error(e1.getMessage());
        } catch (MatchUserException e2) {
            res = "0";
            msg = "手机端兼职处理";
            String userName = authUtil.getUserName();
            DeptUserDb dud = new DeptUserDb();
            Vector vu = dud.getDeptsOfUser(userName);
            Iterator irdu = vu.iterator();
            JSONArray deptArr = new JSONArray();
            try {
                while (irdu.hasNext()) {
                    DeptDb dept = (DeptDb) irdu.next();
                    if (dept.isHide()) {
                        continue;
                    }
                    String deptCode = dept.getCode();
                    String name = dept.getName();
                    if (!dept.getParentCode().equals(DeptDb.ROOTCODE) && !dept.getCode().equals(DeptDb.ROOTCODE)) {
                        name = dept.getDeptDb(dept.getParentCode()).getName() + "->" + dept.getName();
                    }
                    JSONObject deptObj = new JSONObject();
                    deptObj.put("name", name);
                    deptObj.put("code", deptCode);
                    deptArr.put(deptObj);
                }
                result.put("multiDepts", deptArr);
            } catch (JSONException e) {
                log.error(e2.getMessage());
            }
        } finally {
            try {
                json.put("res", res);
                json.put("msg", msg);
                result.put("fields", fields);
                json.put("result", result);
                json.put("hasAttach", hasAttach);

                // 算式相关的字段
                json.put("funcRelatedOnChangeFields", FuncUtil.doGetFieldsRelatedOnChangeMobile(fd));
            } catch (final JSONException e) {
                log.error(e.getMessage());
            }
        }
        return json;
    }
}
