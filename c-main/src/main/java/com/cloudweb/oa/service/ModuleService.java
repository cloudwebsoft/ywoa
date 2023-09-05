package com.cloudweb.oa.service;

import bsh.EvalError;
import bsh.Interpreter;
import cn.hutool.core.lang.mutable.MutableDouble;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IBasicSelectCtl;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.api.ISQLCtl;
import com.cloudweb.oa.bean.ExportExcelItem;
import com.cloudweb.oa.cache.ExportExcelCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.permission.ModuleTreePermission;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.*;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.shell.BSHShell;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.*;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormDAOMgr;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Label;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.lang.Boolean;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

@Component
public class ModuleService {

    @Autowired
    private UserCache userCache;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    ModuleTreePermission moduleTreePermission;

    @Autowired
    HttpServletRequest request;

    @Autowired
    SysProperties sysProperties;

    @Autowired
    ConfigUtil configUtil;

    @Autowired
    ExportExcelCache exportExcelCache;

    // @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result<Object> create(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        String code = ParamUtil.get(request, "moduleCode");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "formCode");
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            json.put("ret", "0");
            json.put("msg", "模块不存在！");
            return new Result<>(false, "模块不存在！");
        }

        String formCode = msd.getString("form_code");
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);
        ModulePrivDb mpd = new ModulePrivDb(code);

        boolean isTreeView = ParamUtil.getBoolean(request, "isTreeView", false);
        if (isTreeView) {
            String nodeCode = ParamUtil.get(request, "treeNodeCode");
            if (!moduleTreePermission.canAdd(authUtil.getUserName(), nodeCode)) {
                return new Result<>(false, i18nUtil.get("pvg_invalid"));
            }
        } else {
            if (!mpd.canUserAppend(privilege.getUser(request))) {
                return new Result<>(false, SkinUtil.LoadString(request, "pvg_invalid"));
            }
        }

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        boolean re = fdm.create(request.getServletContext(), request, msd);
        // 如果指定了添加跳转URL 20211116作废，改为使用pageSetup
        // addToUrl = StrUtil.getNullStr(msd.getString("add_to_url"));

        String pageSetup = msd.getString("page_setup");
        if (StringUtils.isEmpty(pageSetup)) {
            pageSetup = "{}";
        }
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
        int pageAddRedirect = ConstUtil.PAGE_ADD_REDIRECT_TO_DEFAULT;
        String redirectUrl = "";
        com.alibaba.fastjson.JSONObject addJson = null;
        if (jsonObject.containsKey("addPage")) {
            addJson = jsonObject.getJSONObject("addPage");
            pageAddRedirect = addJson.getIntValue("pageAddRedirect");
            if (pageAddRedirect == ConstUtil.PAGE_ADD_REDIRECT_TO_URL) {
                redirectUrl = addJson.getString("redirectUrl");
            } else if (pageAddRedirect == ConstUtil.PAGE_ADD_REDIRECT_TO_SHOW) {
                String visitKeyShow = SecurityUtil.makeVisitKey(fdm.getVisualObjId());
                redirectUrl = "visual/moduleShowPage.do?moduleCode=" + code + "&id=" + fdm.getVisualObjId() + "&visitKey=" + visitKeyShow;
            }
        }

        if (!"".equals(redirectUrl)) {
            redirectUrl = ModuleUtil.parseUrl(request, redirectUrl, fdm.getFormDAO());
            if (!redirectUrl.startsWith("http")) {
                redirectUrl = request.getContextPath() + "/" + redirectUrl;
            }
        }

        if (re) {
            json.put("addToUrl", redirectUrl);
            json.put("id", fdm.getVisualObjId());
            return new Result<>(json);
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");

            return new Result<>(false, "操作失败！");
        }
    }

    // @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result<Object> update(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        String code = ParamUtil.get(request, "moduleCode");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            return new Result<>(false, "模块不存在！");
        }

        ModulePrivDb mpd = new ModulePrivDb(code);
        boolean isAttend = false;
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        if (flowId != -1) {
            MyActionDb myActionDb = new MyActionDb();
            // 如果用户参与了流程，则不检查权限
            isAttend = myActionDb.isUserAttendFlow(flowId, privilege.getUser(request));
        }

        if (!isAttend) {
            boolean isTreeView = ParamUtil.getBoolean(request, "isTreeView", false);
            if (isTreeView) {
                String nodeCode = ParamUtil.get(request, "treeNodeCode");
                if (!moduleTreePermission.canSee(authUtil.getUserName(), nodeCode)) {
                    return new Result<>(false, i18nUtil.get("pvg_invalid"));
                }
            } else if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
                return new Result<>(false, SkinUtil.LoadString(request, "pvg_invalid"));
            }
        }

        String formCode = msd.getString("form_code");
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        return new Result<>(fdm.update(request.getServletContext(), request, msd));
    }

    // @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result<Object> createRelate(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();

        String formCode = ParamUtil.get(request, "formCode");
        if ("".equals(formCode)) {
            return new Result<>(false, SkinUtil.LoadString(request, "err_id"));
        }

        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        ModuleSetupDb msdRelated = new ModuleSetupDb();
        msdRelated = msdRelated.getModuleSetupDb(moduleCodeRelated);
        String formCodeRelated = msdRelated.getString("form_code");

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);

        long parentId = ParamUtil.getLong(request, "parentId", -1); // 父模块的ID
        String pageType = ParamUtil.get(request, "pageType");
        if (!ConstUtil.PAGE_TYPE_ADD_RELATE.equals(pageType)) {
            if (parentId == -1) {
                return new Result<>(false, "缺少父模块记录的ID");
            }
        }

        boolean isAttend = false;
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        if (flowId != -1) {
            MyActionDb myActionDb = new MyActionDb();
            // 如果用户参与了流程，则不检查权限
            isAttend = myActionDb.isUserAttendFlow(flowId, privilege.getUser(request));
        }

        if (!isAttend) {
            ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
            if (!mpd.canUserAppend(privilege.getUser(request))) {
                return new Result<>(false, SkinUtil.LoadString(request, "pvg_invalid"));
            }
        }

        if (fd == null || !fd.isLoaded()) {
            return new Result<>(false, "表单不存在");
        }

        boolean re;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            re = fdm.create(request.getServletContext(), request);
        } catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    // @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Result<Object> updateRelate(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();

        // 取从模块编码
        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        if ("".equals(moduleCodeRelated)) {
            return new Result<>(false, "缺少关联模块编码");
        }

        // 取主模块编码
        String moduleCode = ParamUtil.get(request, "code");

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        if (msd == null) {
            return new Result<>(false, "模块不存在");
        }
        String formCodeRelated = msd.getString("form_code");

        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        if (!mpd.canUserModify(privilege.getUser(request))) {
            return new Result<>(false, SkinUtil.LoadString(request, "pvg_invalid"));
        }

        int flowId = ParamUtil.getInt(request, "flowId", -1);
        long id = ParamUtil.getLong(request, "id", -1);
        if (id == -1) {
            return new Result<>(false, SkinUtil.LoadString(request, "err_id"));
        }

        // 检查数据权限，判断用户是否可以存取此条数据
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDb(moduleCode);
        if (parentMsd == null) {
            return new Result<>(false, "父模块不存在");
        }
        String parentFormCode = parentMsd.getString("form_code");
        String mode = ParamUtil.get(request, "mode");
        // 是否通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);
        String relateFieldValue = "";
        long parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
        if (parentId == -1) {
            if (flowId != -1) {
                return new Result<>(false, "缺少父模块记录的ID");
            }
        } else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
                if (relateFieldValue == null) {
                    // 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                }
            }
        }
        /*if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
            return new Result<>(false, i18nUtil.get("info_access_data_fail"));
        }*/

        boolean re = false;
        try {
            FormMgr fm = new FormMgr();
            FormDb fd = fm.getFormDb(formCodeRelated);
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
            re = fdm.update(request.getServletContext(), request);
        } catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }

        return new Result<>(re);
    }

    public String getColOperate(HttpServletRequest request, String userName, ModuleSetupDb msd, FormDb fd, FormDAO fdao, ModulePrivDb mpd, boolean canView) {
        String code = msd.getCode();
        String formCode = msd.getString("form_code");

        String mainFormCode = code;
        int is_workLog = msd.getInt("is_workLog");
        if (!msd.getString("code").equals(msd.getString("form_code"))) {
            ModuleSetupDb msdMain = msd.getModuleSetupDb(msd.getString("form_code"));
            is_workLog = msdMain.getInt("is_workLog");
            mainFormCode = msd.getString("form_code");
        }

        boolean canLog = mpd.canUserLog(userName);
        boolean canManage = mpd.canUserManage(userName);
        boolean canModify = mpd.canUserModify(userName);
        boolean canDel = mpd.canUserDel(userName);

        com.redmoon.oa.Config cfg = new Config();
        boolean isModuleHistory = cfg.getBooleanProperty("isModuleHistory");
        boolean isModuleLogRead = cfg.getBooleanProperty("isModuleLogRead");
        boolean isModuleLogModify = cfg.getBooleanProperty("isModuleLogModify");

        Privilege privilege = new Privilege();
        long id = fdao.getId();

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        String desKey = ssoCfg.get("key");
        StringBuilder sb = new StringBuilder();

        if (msd.getInt("btn_display_show") == 1 && canView) {
            if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                if (urlShow.contains("?")) {
                    sb.append("<a href=\"javascript:;\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "&parentId=" + id + "&id=" + id + "&code=" + code + "')\">查看</a>");
                } else {
                    sb.append("<a href=\"javascript:;\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "?parentId=" + id + "&id=" + id + "&code=" + code + "')\">查看</a>");
                }
            } else {
                sb.append("<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/moduleShowPage.do?parentId=" + id + "&id=" + id + "&code=" + code + "')\">查看</a>");
            }
        }

        if (msd.getInt("btn_flow_show") == 1) {
            if (fd.isFlow() && fdao.getFlowId() != -1) {
                String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));
                sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + fdao.getFlowId() + "&visitKey=" + visitKey + "')\">" + i18nUtil.get("btn_flow") + "</a>");
            }
        }

        /*if (msd.getInt("btn_edit_show")==1 && canModify) {
            if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
                sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_edit") + "?parentId=" + id + "&id=" + id + "&code=" + code + "&formCode=" + formCode + "')\">修改</a>");
            }
            else {
                sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/moduleEditPage.do?parentId=" + id + "&id=" + id + "&code=" + code + "&formCode=" + formCode + "')\">修改</a>");
            }
        }

        if (canDel || canManage) {
            if (msd.getInt("btn_edit_show")==1) {
                sb.append("&nbsp;&nbsp;<a onclick=\"del('" + id + "')\" href=\"javascript:;\">删除</a>");
            }
        }*/
        if (canLog || canManage) {
            if (msd.getInt("btn_log_show") == 1 && fd.isLog()) {
                String btnLogName = "日志";
                if (isModuleLogRead) {
                    btnLogName = "修改日志";
                }
                String btnHisName = "历史";

                if (isModuleHistory) {
                    sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('历史记录', '" + request.getContextPath() + "/visual/module_his_list.jsp?op=search&code=" + code + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnHisName + "</a>");
                }
                if (isModuleLogModify) {
                    sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('修改日志', '" + request.getContextPath() + "/visual/module_log_list.jsp?op=search&code=" + code + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnLogName + "</a>");
                }

                if (isModuleLogRead) {
                    sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('浏览日志', '" + request.getContextPath() + "/visual/moduleListPage.do?op=search&code=module_log_read&read_type=" + FormDAOLog.READ_TYPE_MODULE + "&module_code=" + code + "&module_id=" + id + "&form_code=" + formCode + "')\">浏览日志</a>");
                }
            }
        }

        String op_link_name = StrUtil.getNullStr(msd.getString("op_link_name"));
        String[] linkNames = StrUtil.split(op_link_name, ",");
        String op_link_href = StrUtil.getNullStr(msd.getString("op_link_url"));
        String[] linkHrefs = StrUtil.split(op_link_href, ",");

        String op_link_field = StrUtil.getNullStr(msd.getString("op_link_field"));
        String[] linkFields = StrUtil.split(op_link_field, ",");
        String op_link_cond = StrUtil.getNullStr(msd.getString("op_link_cond"));
        String[] linkConds = StrUtil.split(op_link_cond, ",");
        String op_link_value = StrUtil.getNullStr(msd.getString("op_link_value"));
        String[] linkValues = StrUtil.split(op_link_value, ",");
        String op_link_event = StrUtil.getNullStr(msd.getString("op_link_event"));
        String[] linkEvents = StrUtil.split(op_link_event, ",");
        String op_link_role = StrUtil.getNullStr(msd.getString("op_link_role"));
        String[] linkRoles = StrUtil.split(op_link_role, "#");

        // 为兼容以前的版本，初始化tRole
        if (linkNames != null) {
            if (linkRoles == null || linkRoles.length != linkNames.length) {
                linkRoles = new String[linkNames.length];
                for (int i = 0; i < linkNames.length; i++) {
                    linkRoles[i] = "";
                }
            }
        }

        if (linkNames != null) {
            for (int i = 0; i < linkNames.length; i++) {
                String linkName = linkNames[i];

                String linkField = linkFields[i];
                String linkCond = linkConds[i];
                String linkValue = linkValues[i];
                String linkEvent = linkEvents[i];
                String linkRole = linkRoles[i];

                // 检查是否拥有权限
                if (!privilege.isUserPrivValid(request, "admin")) {
                    boolean canSeeLink = false;
                    if (!"".equals(linkRole)) {
                        String[] codeAry = StrUtil.split(linkRole, ",");
                        if (codeAry != null) {
                            UserDb user = new UserDb();
                            user = user.getUserDb(privilege.getUser(request));
                            RoleDb[] rdAry = user.getRoles();
                            if (rdAry != null) {
                                for (RoleDb rd : rdAry) {
                                    String roleCode = rd.getCode();
                                    for (String codeAllowed : codeAry) {
                                        if (roleCode.equals(codeAllowed)) {
                                            canSeeLink = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            canSeeLink = true;
                        }
                    } else {
                        canSeeLink = true;
                    }

                    if (!canSeeLink) {
                        continue;
                    }
                }

                if ("#".equals(linkField)) {
                    linkField = "";
                }
                if ("#".equals(linkCond)) {
                    linkCond = "";
                }
                if ("#".equals(linkValue)) {
                    linkValue = "";
                }
                if ("#".equals(linkEvent)) {
                    linkEvent = "";
                }
                if ("".equals(linkField) || ModuleUtil.isLinkShow(request, msd, fdao, linkField, linkCond, linkValue)) {
                    if ("click".equals(linkEvent)) {
                        sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code) + "\">" + linkName + "</a>");
                    } else {
                        sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + linkName + "', '" + request.getContextPath() + "/" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code) + "')\">" + linkName + "</a>");
                    }
                }
            }
        }

        if (is_workLog == 1) {
            sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "汇报', '" + request.getContextPath() + "/queryMyWork.action?code=" + mainFormCode + "&id=" + id + "')\">汇报</a>");
        }
        if (mpd.canUserReActive(privilege.getUser(request))) {
            MyActionDb mad = new MyActionDb();
            long flowId = fdao.getFlowId();
            if (flowId != 0 && flowId != -1) {
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb((int) flowId);
                WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
                boolean isReactive = false;
                if (wpd != null) {
                    isReactive = wpd.isReactive();
                }

                if (isReactive) {
                    mad = mad.getMyActionDbFirstChecked(flowId, privilege.getUser(request));
                    if (mad != null) {
                        sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "变更', '" + request.getContextPath() + "/flowDispose.do?myActionId=" + mad.getId() + "')\">变更</a>");
                    }
                }
            }
        }

        return sb.toString();
    }

    public com.alibaba.fastjson.JSONArray getColOperateBtn(HttpServletRequest request, String userName, ModuleSetupDb msd, FormDb fd, FormDAO fdao, ModulePrivDb mpd, boolean canView, boolean canModify, boolean canDel, boolean canManage, boolean canLog, boolean canReActive) {
        com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();

        String code = msd.getCode();
        // String formCode = msd.getString("form_code");

        /*String mainFormCode = code;
        int is_workLog = msd.getInt("is_workLog");
        if (!msd.getString("code").equals(msd.getString("form_code"))) {
            ModuleSetupDb msdMain = msd.getModuleSetupDb(msd.getString("form_code"));
            is_workLog = msdMain.getInt("is_workLog");
            mainFormCode = msd.getString("form_code");
        }*/

        /*com.redmoon.oa.Config cfg = Config.getInstance();
        boolean isModuleHistory = cfg.getBooleanProperty("isModuleHistory");
        boolean isModuleLogRead = cfg.getBooleanProperty("isModuleLogRead");
        boolean isModuleLogModify = cfg.getBooleanProperty("isModuleLogModify");*/

        Privilege privilege = new Privilege();
        long id = fdao.getId();

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        String desKey = ssoCfg.get("key");

        JSONObject props = JSONObject.parseObject(msd.getString("props"));
        if (props == null) {
            props = new com.alibaba.fastjson.JSONObject();
        }

        if (msd.getInt("btn_display_show") == 1 && canView) {
            String cond = props.getString("btnDisplayCond");
            boolean re = true;
            if (!StrUtil.isEmpty(cond)) {
                cond = ModuleUtil.parseConds(request, fdao, cond);
                javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
                javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
                try {
                    re = (Boolean) engine.eval(cond);
                } catch (javax.script.ScriptException ex) {
                    LogUtil.getLog(ModuleUtil.class).error(ex);
                }
            }
            if (re) {
                JSONObject json = new JSONObject();
                json.put("type", "SHOW");
                if (StrUtil.isEmpty(props.getString("btnDisplayName"))) {
                    json.put("name", "查看");
                } else {
                    json.put("name", props.getString("btnDisplayName"));
                }
                json.put("moduleCode", code);
                json.put("id", id);

                if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                    String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                    json.put("link", urlShow);
                } else {
                    json.put("link", "");
                }
                ary.add(json);
            }
        }

        boolean isBtnEditShow = msd.getInt("btn_edit_show") == 1 && (canModify || canManage);
        if (isBtnEditShow) {
            String cond = props.getString("btnEditCond");
            boolean re = true;
            if (!StrUtil.isEmpty(cond)) {
                cond = ModuleUtil.parseConds(request, fdao, cond);
                javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
                javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
                try {
                    re = (Boolean) engine.eval(cond);
                } catch (javax.script.ScriptException ex) {
                    LogUtil.getLog(ModuleUtil.class).error(ex);
                }
            }
            if (re) {
                JSONObject json = new JSONObject();
                json.put("type", "EDIT");
                if (StrUtil.isEmpty(props.getString("btnEditName"))) {
                    json.put("name", "修改");
                } else {
                    json.put("name", props.getString("btnEditName"));
                }
                json.put("id", id);
                json.put("moduleCode", code);
                ary.add(json);
            }
        }
        boolean isBtnDelShow = msd.getInt("btn_del_show") == 1 && (canDel || canManage);
        if (isBtnDelShow) {
            String cond = props.getString("btnDelCond");
            boolean re = true;
            if (!StrUtil.isEmpty(cond)) {
                cond = ModuleUtil.parseConds(request, fdao, cond);
                javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
                javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
                try {
                    re = (Boolean) engine.eval(cond);
                } catch (javax.script.ScriptException ex) {
                    LogUtil.getLog(ModuleUtil.class).error(ex);
                }
            }
            if (re) {
                JSONObject json = new JSONObject();
                json.put("type", "DEL");
                if (StrUtil.isEmpty(props.getString("btnDelName"))) {
                    json.put("name", "删除");
                } else {
                    json.put("name", props.getString("btnDelName"));
                }
                json.put("id", id);
                json.put("moduleCode", code);
                ary.add(json);
            }
        }

        if (msd.getInt("btn_flow_show") == 1) {
            if (fd.isFlow() && fdao.getFlowId() != -1) {
                String cond = props.getString("btnFlowCond");
                boolean re = true;
                if (!StrUtil.isEmpty(cond)) {
                    cond = ModuleUtil.parseConds(request, fdao, cond);
                    javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
                    javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
                    try {
                        re = (Boolean) engine.eval(cond);
                    } catch (javax.script.ScriptException ex) {
                        LogUtil.getLog(ModuleUtil.class).error(ex);
                    }
                }
                if (re) {
                    JSONObject json = new JSONObject();
                    json.put("type", "FLOW_SHOW");
                    if (StrUtil.isEmpty(props.getString("btnFlowName"))) {
                        json.put("name", "流程");
                    } else {
                        json.put("name", props.getString("btnFlowName"));
                    }
                    json.put("flowId", fdao.getFlowId());
                    String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));
                    json.put("visitKey", visitKey);
                    ary.add(json);
                }
            }
        }

        if (mpd.canUserRollBack(userName)) {
            if (fd.isFlow() && fdao.getFlowId() != -1) {
                JSONObject json = new JSONObject();
                json.put("type", "ROLL_BACK");
                json.put("name", "回滚");
                json.put("flowId", fdao.getFlowId());
                ary.add(json);
            }
        }

        /*if (msd.getInt("btn_edit_show")==1 && canModify) {
            if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
                sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + msd.getString("url_edit") + "?parentId=" + id + "&id=" + id + "&code=" + code + "&formCode=" + formCode + "')\">修改</a>");
            }
            else {
                sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/moduleEditPage.do?parentId=" + id + "&id=" + id + "&code=" + code + "&formCode=" + formCode + "')\">修改</a>");
            }
        }

        if (canDel || canManage) {
            if (msd.getInt("btn_edit_show")==1) {
                sb.append("&nbsp;&nbsp;<a onclick=\"del('" + id + "')\" href=\"javascript:;\">删除</a>");
            }
        }*/

        /*if (canLog || canManage) {
            if (msd.getInt("btn_log_show") == 1 && fd.isLog()) {
                String btnLogName = "日志";
                if (isModuleLogRead) {
                    btnLogName = "修改日志";
                }
                // String btnHisName = "历史";

                if (isModuleHistory) {
                    JSONObject json = new JSONObject();
                    json.put("type", "HISTORY");
                    json.put("name", "历史");
                    json.put("code", code);
                    json.put("fdaoId", id);
                    json.put("formCode", formCode);
                    ary.add(json);
                }
                if (isModuleLogModify) {
                    JSONObject json = new JSONObject();
                    json.put("type", "LOG_EDIT");
                    json.put("name", btnLogName);
                    json.put("code", code);
                    json.put("fdaoId", id);
                    json.put("formCode", formCode);
                    ary.add(json);
                }

                if (isModuleLogRead) {
                    JSONObject json = new JSONObject();
                    json.put("type", "LOG_SHOW");
                    json.put("name", "浏览日志");
                    json.put("code", "module_log_read");
                    json.put("module_code", code);
                    json.put("form_code", formCode);
                    json.put("read_type", FormDAOLog.READ_TYPE_MODULE);
                    json.put("isModule", true);
                    ary.add(json);
                }
            }
        }*/

        String op_link_name = StrUtil.getNullStr(msd.getString("op_link_name"));
        String[] linkNames = StrUtil.split(op_link_name, ",");
        String op_link_href = StrUtil.getNullStr(msd.getString("op_link_url"));
        String[] linkHrefs = StrUtil.split(op_link_href, ",");

        String op_link_field = StrUtil.getNullStr(msd.getString("op_link_field"));
        String[] linkFields = StrUtil.split(op_link_field, ",");
        String op_link_cond = StrUtil.getNullStr(msd.getString("op_link_cond"));
        String[] linkConds = StrUtil.split(op_link_cond, ",");
        String op_link_value = StrUtil.getNullStr(msd.getString("op_link_value"));
        String[] linkValues = StrUtil.split(op_link_value, ",");
        String op_link_event = StrUtil.getNullStr(msd.getString("op_link_event"));
        String[] linkEvents = StrUtil.split(op_link_event, ",");
        String op_link_role = StrUtil.getNullStr(msd.getString("op_link_role"));
        String[] linkRoles = StrUtil.split(op_link_role, "#");
        String op_link_icon = StrUtil.getNullStr(msd.getString("op_link_icon"));
        String[] linkIcons = StrUtil.split(op_link_icon, ",");
        String op_link_color = StrUtil.getNullStr(msd.getString("op_link_color"));
        String[] linkColors = StrUtil.split(op_link_color, ",");

        // 为兼容以前的版本，初始化tRole
        if (linkNames != null) {
            if (linkRoles == null || linkRoles.length != linkNames.length) {
                linkRoles = new String[linkNames.length];
                for (int i = 0; i < linkNames.length; i++) {
                    linkRoles[i] = "";
                }
            }
            if (linkIcons == null || linkIcons.length != linkNames.length) {
                linkIcons = new String[linkNames.length];
                for (int i = 0; i < linkNames.length; i++) {
                    linkIcons[i] = "";
                }
            }
            if (linkColors == null || linkColors.length != linkNames.length) {
                linkColors = new String[linkNames.length];
                for (int i = 0; i < linkNames.length; i++) {
                    linkColors[i] = "";
                }
            }
        }

        if (linkNames != null) {
            for (int i = 0; i < linkNames.length; i++) {
                String linkName = linkNames[i];

                String linkField = linkFields[i];
                String linkCond = linkConds[i];
                String linkValue = linkValues[i];
                String linkEvent = linkEvents[i];
                String linkRole = linkRoles[i];
                String linkIcon = linkIcons[i];
                String linkColor = linkColors[i];

                // 检查是否拥有权限
                if (!privilege.isUserPrivValid(request, "admin")) {
                    boolean canSeeLink = false;
                    if (!"".equals(linkRole)) {
                        String[] codeAry = StrUtil.split(linkRole, ",");
                        if (codeAry != null) {
                            UserDb user = new UserDb();
                            user = user.getUserDb(privilege.getUser(request));
                            RoleDb[] rdAry = user.getRoles();
                            if (rdAry != null) {
                                for (RoleDb rd : rdAry) {
                                    String roleCode = rd.getCode();
                                    for (String codeAllowed : codeAry) {
                                        if (roleCode.equals(codeAllowed)) {
                                            canSeeLink = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            canSeeLink = true;
                        }
                    } else {
                        canSeeLink = true;
                    }

                    if (!canSeeLink) {
                        continue;
                    }
                }

                if ("#".equals(linkField)) {
                    linkField = "";
                }
                if ("#".equals(linkCond)) {
                    linkCond = "";
                }
                if ("#".equals(linkValue)) {
                    linkValue = "";
                }
                if ("#".equals(linkEvent)) {
                    linkEvent = "";
                }
                if ("".equals(linkField) || ModuleUtil.isLinkShow(request, msd, fdao, linkField, linkCond, linkValue)) {
                    if ("click".equals(linkEvent)) {
                        JSONObject json = new JSONObject();
                        json.put("type", "CLICK");
                        json.put("name", linkName);
                        json.put("script", ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code));
                        json.put("color", linkColor);
                        json.put("icon", linkIcon);
                        ary.add(json);
                    } else {
                        JSONObject json = new JSONObject();
                        json.put("type", "LINK");
                        json.put("name", linkName);
                        json.putAll(ModuleUtil.renderLinkUrlToJson(request, fdao, linkHrefs[i], linkName, code));
                        json.put("color", linkColor);
                        json.put("icon", linkIcon);
                        ary.add(json);
                    }
                }
            }
        }

        if (canReActive) {
            MyActionDb mad = new MyActionDb();
            long flowId = fdao.getFlowId();
            if (flowId != 0 && flowId != -1) {
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb((int) flowId);
                WorkflowPredefineDb wpd = new WorkflowPredefineDb();
                wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
                boolean isReactive = false;
                if (wpd != null) {
                    isReactive = wpd.isReactive();
                }

                if (isReactive) {
                    mad = mad.getMyActionDbFirstChecked(flowId, privilege.getUser(request));
                    if (mad != null) {
                        JSONObject json = new JSONObject();
                        json.put("type", "PROCESS");
                        json.put("name", "变更");
                        json.put("myActionId", mad.getId());
                        ary.add(json);

                        // sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + msd.getString("name") + "变更', '" + request.getContextPath() + "/flowDispose.do?myActionId=" + mad.getId() + "')\">变更</a>");
                    }
                }
            }
        }

        return ary;
    }

    public com.alibaba.fastjson.JSONArray getColOperateForListSel(HttpServletRequest request, String userName, ModuleSetupDb msd, FormDb fd, FormDAO fdao, ModulePrivDb mpd, boolean canView) {
        com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();
        String code = msd.getCode();
        long id = fdao.getId();

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        String desKey = ssoCfg.get("key");
        StringBuilder sb = new StringBuilder();

        if (msd.getInt("btn_display_show") == 1 && canView) {
            JSONObject json = new JSONObject();
            json.put("type", "SHOW");
            json.put("name", "查看");
            json.put("moduleCode", code);
            json.put("id", id);

            if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                json.put("link", urlShow);
            } else {
                json.put("link", "");
            }
            ary.add(json);
        }

        /*boolean isBtnEditShow = msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(userName);
        if (isBtnEditShow) {
            JSONObject json = new JSONObject();
            json.put("type", "EDIT");
            json.put("name", "修改");
            json.put("id", id);
            json.put("moduleCode", code);
            ary.add(json);
        }*/
/*
        if (msd.getInt("btn_flow_show") == 1) {
            if (fd.isFlow() && fdao.getFlowId() != -1) {
                JSONObject json = new JSONObject();
                json.put("type", "FLOW");
                json.put("name", "流程");
                json.put("flowId", fdao.getFlowId());
                String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));
                json.put("visitKey", visitKey);
                ary.add(json);
            }
        }*/

        return ary;
    }

    public String getColOperateForListSelXXX(HttpServletRequest request, JSONObject moduleFieldSelectCtlDesc, int k, String openerFormCode, String openerFieldName, String showValue, boolean isShowFieldFound, FormDAO fdao) throws ErrMsgException {
        FormDb openerFd = new FormDb();
        openerFd = openerFd.getFormDb(openerFormCode);
        if (!openerFd.isLoaded()) {
            throw new ErrMsgException("表单：" + openerFormCode + " 不存在");
        }
        FormField openerField = openerFd.getFormField(openerFieldName);
        if (openerField == null) {
            throw new ErrMsgException("字段：" + openerFieldName + " 在表单：" + openerFormCode + " 中不存在");
        }

        com.alibaba.fastjson.JSONArray mapAry = moduleFieldSelectCtlDesc.getJSONArray("maps");

        // String sourceFormCode = moduleFieldSelectCtlDesc.getString("sourceFormCode");
        String byFieldName = moduleFieldSelectCtlDesc.getString("idField");
        String showFieldName = moduleFieldSelectCtlDesc.getString("showField");

        long id = fdao.getId();
        MacroCtlMgr mm = new MacroCtlMgr();
        String byValue = "";
        if ("id".equals(byFieldName)) {
            byValue = String.valueOf(id);
        } else {
            byValue = fdao.getFieldValue(byFieldName);
        }
        // @task:id在设置宏控件时，还不能被配置为被显示
        if ("id".equals(showFieldName)) {
            showValue = String.valueOf(id);
            isShowFieldFound = true;
        }

        if (!isShowFieldFound) {
            FormField ff = fdao.getFormField(showFieldName);
            if (ff == null) {
                showValue = "字段：" + showFieldName + " 不存在";
            } else {
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        showValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(showFieldName));
                    }
                } else {
                    showValue = fdao.getFieldValue(showFieldName);
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        ParamChecker pck = new ParamChecker(request);
        String funs = "";
        if (mapAry != null) {
            for (int i = 0; i < mapAry.size(); i++) {
                JSONObject json = mapAry.getJSONObject(i);
                String destF = (String) json.get("destField");    // 父页面
                String sourceF = (String) json.get("sourceField");    // module_list_sel.jsp页面
                Vector vector = openerFd.getFields();
                Iterator it = vector.iterator();
                FormField tempFf = null;
                while (it.hasNext()) {
                    tempFf = (FormField) it.next();
                    if (tempFf.getName().equals(destF)) {
                        break;
                    }
                }
                boolean isMacro = false;
                // setValue为module_list_sel.jsp页面中所选择的值
                String setValue = "";
                if ("id".equals(sourceF)) {
                    setValue = String.valueOf(fdao.getId());
                } else if ("cws_id".equals(sourceF)) {
                    setValue = fdao.getCwsId();
                } else {
                    setValue = fdao.getFieldValue(sourceF);
                }
                String checkJs = com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, tempFf);
                IFormMacroCtl ifmc = null;
                // 如果这个值将被赋值至父页面中的一个宏控件中的时候，则需要将父页面中的宏控件用convertToHTMLCtl重新替换赋值，需要注意的是宏控件传入参数中FormField需要用setValue赋值
                if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
                    tempFf.setValue(setValue);
                    isMacro = true;
                    request.setAttribute("cwsMapSourceFormField", fdao.getFormField(sourceF));
                    // setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().convertToHTMLCtl(request, tempFf).replaceAll("\\'", "\\\\'").replaceAll("\"", "&quot;");
                    ifmc = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl();
                    setValue = ifmc.convertToHTMLCtl(request, tempFf);
                }

                // 增加辅助表单域，以免算式中出现引号问题，helper为outerHTML
                sb.append("<textarea id=\"helper" + k + "_" + i + "\" class=\"helper-ctl\">" + setValue + "</textarea>\n");
                sb.append("<textarea id=\"helperSource" + k + "_" + i + "\" class=\"helper-ctl\">" + fdao.getFieldValue(sourceF) + "</textarea>\n");
                sb.append("<textarea id=\"helperJs" + k + "_" + i + "\" class=\"helper-ctl\">" + checkJs + "</textarea>\n");

                funs += "setOpenerFieldValue('" + destF + "', o('helper" + k + "_" + i + "').value," + isMacro + ", o('helperSource" + k + "_" + i + "').value, o('helperJs" + k + "_" + i + "').value);\n";
                if (ifmc instanceof ISQLCtl) {
                    // 调用onSQLCtlRelateFieldChange_，以使得控件被映射时能够生成
                    // 因为当convertToHtmlCtl时，生成的是input，且已经有value，
                    // 而在macro_sql_ctl_js.jsp是用setInterval来检测变化的，而值在映射过来后并不变化，所以不这样处理，无法生成控件，而只能看到一个带有字段值的文本框
                    funs += "window.opener.onSQLCtlRelateFieldChange_" + destF + "();\n";
                }
            }
        }

        sb.append("<script>\n");
        sb.append("function doFuns" + k + "() {\n");
        sb.append(" $('body').showLoading();\n");
        sb.append(" " + funs + "\n");
        sb.append("}\n");
        sb.append("</script>\n");
        sb.append("<textarea id=\"helperJsShowValue" + k + "\" style=\"display:none\">" + showValue + "</textarea>");
        sb.append("<a href=\"javascript:sel('" + byValue + "', o('helperJsShowValue" + k + "').value);doFuns" + k + "();doAtferSel();\">选择</a>");
        return sb.toString();
    }

    public String getItemsForListSel(HttpServletRequest request, JSONObject moduleFieldSelectCtlDesc, long k, String openerFormCode, String openerFieldName, String showValue, boolean isShowFieldFound, FormDAO fdao) throws ErrMsgException {
        FormDb openerFd = new FormDb();
        openerFd = openerFd.getFormDb(openerFormCode);
        if (!openerFd.isLoaded()) {
            throw new ErrMsgException("表单：" + openerFormCode + " 不存在");
        }
        FormField openerField = openerFd.getFormField(openerFieldName);
        if (openerField == null) {
            throw new ErrMsgException("字段：" + openerFieldName + " 在表单：" + openerFormCode + " 中不存在");
        }

        com.alibaba.fastjson.JSONArray mapAry = moduleFieldSelectCtlDesc.getJSONArray("maps");

        // String sourceFormCode = moduleFieldSelectCtlDesc.getString("sourceFormCode");
        String byFieldName = moduleFieldSelectCtlDesc.getString("idField");
        String showFieldName = moduleFieldSelectCtlDesc.getString("showField");
        // 默认为选择窗体
        int mode = 1;
        if (moduleFieldSelectCtlDesc.containsKey("mode")) {
            mode = moduleFieldSelectCtlDesc.getIntValue("mode");
        }

        long id = fdao.getId();
        MacroCtlMgr mm = new MacroCtlMgr();
        String byValue = "";
        if ("id".equals(byFieldName)) {
            byValue = String.valueOf(id);
        } else {
            byValue = fdao.getFieldValue(byFieldName);
        }
        // @task:id在设置宏控件时，还不能被配置为被显示
        if ("id".equals(showFieldName)) {
            showValue = String.valueOf(id);
            isShowFieldFound = true;
        }

        if (!isShowFieldFound) {
            FormField ff = fdao.getFormField(showFieldName);
            if (ff == null) {
                showValue = "字段：" + showFieldName + " 不存在";
            } else {
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        showValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(showFieldName));
                    }
                } else {
                    showValue = fdao.getFieldValue(showFieldName);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='helper-module-list-sel'>");

        ParamChecker pck = new ParamChecker(request);
        String funs = "";
        if (mapAry != null) {
            for (int i = 0; i < mapAry.size(); i++) {
                JSONObject json = mapAry.getJSONObject(i);
                String destF = (String) json.get("destField");    // 父页面
                // String sourceF = (String) json.get("sourceField");    // module_list_sel.jsp页面
                FormField tempFf = openerFd.getFormField(destF);

                // TODO 20220526此处不需要映射，因为在macro_module_field_select_ctl_js.jsp中会轮询字段值是否有变化，如有变化会调用onSelect
                /*boolean isMacro = false;
                // setValue为module_list_sel.jsp页面中所选择的值
                String setValue = "";
                if ("id".equals(sourceF)) {
                    setValue = String.valueOf(fdao.getId());
                } else if ("cws_id".equals(sourceF)) {
                    setValue = fdao.getCwsId();
                } else {
                    setValue = fdao.getFieldValue(sourceF);
                }
                String checkJs = com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, tempFf);
                IFormMacroCtl ifmc = null;
                // 如果这个值将被赋值至父页面中的一个宏控件中的时候，则需要将父页面中的宏控件用convertToHTMLCtl重新替换赋值，需要注意的是宏控件传入参数中FormField需要用setValue赋值
                if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
                    tempFf.setValue(setValue);
                    isMacro = true;
                    request.setAttribute("cwsMapSourceFormField", fdao.getFormField(sourceF));
                    // setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().convertToHTMLCtl(request, tempFf).replaceAll("\\'", "\\\\'").replaceAll("\"", "&quot;");
                    ifmc = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl();
                    setValue = ifmc.convertToHTMLCtl(request, tempFf);
                }

                // 增加辅助表单域，以免算式中出现引号问题，helper为outerHTML
                sb.append("<textarea id=\"helper" + k + "_" + i + "\" class=\"helper-ctl\">" + setValue + "</textarea>\n");
                sb.append("<textarea id=\"helperSource" + k + "_" + i + "\" class=\"helper-ctl\">" + fdao.getFieldValue(sourceF) + "</textarea>\n");
                sb.append("<textarea id=\"helperJs" + k + "_" + i + "\" class=\"helper-ctl\">" + checkJs + "</textarea>\n");

                funs += "setFieldValueForMapped('" + destF + "', o('helper" + k + "_" + i + "').value," + isMacro + ", o('helperSource" + k + "_" + i + "').value, o('helperJs" + k + "_" + i + "').value, '" + tempFf.getMacroType() + "');\n";
                */

                String formName = ParamUtil.get(request, "cwsFormName");
                IFormMacroCtl ifmc = null;
                if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
                    ifmc = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl();
                    if (ifmc instanceof ISQLCtl) {
                        // 调用onSQLCtlRelateFieldChange_，以使得控件被映射时能够生成
                        // 因为当convertToHtmlCtl时，生成的是input，且已经有value，
                        // 而在macro_sql_ctl_js.jsp是用setInterval来检测变化的，而值在映射过来后并不变化，所以不这样处理，无法生成控件，而只能看到一个带有字段值的文本框
                        funs += "await onSQLCtlRelateFieldChange_" + formName + "_" + destF + "();\n";
                    }
                }
            }
        }
        sb.append("<textarea id=\"helperJsShowValue" + k + "\" style=\"display:none\">" + showValue + "</textarea>");
        sb.append("</div>");

        sb.append("<script id='script-module-list-sel-" + k + "'>\n");
        sb.append("async function doFuns" + k + "() {\n");
        sb.append(" " + funs + "\n");
        // sb.append(" doAtferSel();\n"); // 调用显示规则，改为在macro_module_field_select_ctl_js.jsp的onSelect方法中调用，以免在onSelect方法前被调用了，致显示规则看起来好像不生效
        sb.append("}\n");
        sb.append("</script>\n");
        sb.append("<script id='script-module-list-sel-" + (k + 1) + "'>selByModuleListSel('" + byValue + "', " + mode + ", '" + openerFieldName + "', o('helperJsShowValue" + k + "').value);doFuns" + k + "();</script>");
        return sb.toString();
    }

    public String getSelBatchForNestScript(HttpServletRequest request, int flowId, String parentFormCode, long parentId, ModuleSetupDb msdSource, String nestFormCode, String nestFieldName, String nestType, com.alibaba.fastjson.JSONArray mapAry) throws ErrMsgException {
        String formCodeSource = msdSource.getString("form_code");
        FormDb fdSource = new FormDb();
        fdSource = fdSource.getFormDb(formCodeSource);

        FormDb nestFd = new FormDb();
        nestFd = nestFd.getFormDb(nestFormCode);

        StringBuilder sbScript = new StringBuilder();
        Privilege privilege = new Privilege();
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fdSource);
        com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO(nestFd);
        String ids = ParamUtil.get(request, "ids");
        String[] ary = StrUtil.split(ids, ",");
        if (ary != null) {
            // 检查嵌套表中是否有重复记录
            if (!com.redmoon.oa.flow.FormDAOMgr.checkFieldIsUniqueNestOnPull(request, parentId, parentFormCode, fdSource, nestFd, ary)) {
                StringBuilder sbFields = new StringBuilder();
                for (FormField ff : nestFd.getFields()) {
                    if (ff.isUniqueNest()) {
                        StrUtil.concat(sbFields, "+", ff.getTitle());
                    }
                }
                throw new ErrMsgException(String.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), sbFields));
            }

            if ("detaillist".equals(nestType)) {
                sbScript.append("clearDetailList(\"" + nestFieldName + "\");\n");
            }

            ModuleSetupDb msdNest = new ModuleSetupDb();
            msdNest = msdNest.getModuleSetupDbOrInit(nestFormCode);
            String[] fields = msdNest.getColAry(false, "list_field");

            int len = 0;
            if (fields != null) {
                len = fields.length;
            }

            // 取出待插入的数据
            for (int i = 0; i < ary.length; i++) {
                long id = StrUtil.toLong(ary[i]);
                fdao = fdao.getFormDAO(id, fdSource);

                // 根据映射关系赋值
                JSONObject jsonObj2 = new JSONObject();
                for (int k = 0; k < mapAry.size(); k++) {
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = mapAry.getJSONObject(k);
                        String sfield = (String) jsonObj.get("sourceField");
                        String dfield = (String) jsonObj.get("destField");
                        String fieldValue = "";
                        if (sfield.startsWith("main:")) {
                            String[] subFields = sfield.split(":");
                            if (subFields.length == 3) {
                                FormDb subfd = new FormDb(subFields[1]);
                                com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
                                // FormField subff = subfd.getFormField(subFields[2]);
                                String subsql = "select id from " + subfdao.getTableName() + " where cws_id='" + id + "' order by cws_order";
                                JdbcTemplate jt = new JdbcTemplate();
                                try {
                                    ResultIterator ri = jt.executeQuery(subsql);
                                    if (ri.hasNext()) {
                                        ResultRecord rr = ri.next();
                                        int subid = rr.getInt(1);
                                        subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
                                        fieldValue = subfdao.getFieldValue(subFields[2]);
                                    }
                                } catch (SQLException e) {
                                    LogUtil.getLog(getClass()).error(e);
                                }
                            }
                        } else if (sfield.startsWith("other:")) {
                            fieldValue = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, sfield);
                        } else {
                            fieldValue = fdao.getFieldValue(sfield);
                        }

                        if (sfield.equals(com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID) || "FormDAO_ID".equals(sfield)) {
                            fieldValue = String.valueOf(fdao.getId());
                        }

                        jsonObj2.put(dfield, fieldValue);

                        fdaoNest.setFieldValue(dfield, fieldValue);
                    } catch (JSONException ex) {
                        LogUtil.getLog(getClass()).error(ex);
                    }
                }

                if ("detaillist".equals(nestType)) {
                    JSONArray jsonAry = new JSONArray();
                    jsonAry.put(jsonObj2);

                    // 如果有父窗口
                    sbScript.append("    try {\n");
                    sbScript.append("        window.insertRow(\"" + nestFormCode + "\", '" + jsonAry.toString() + "', \"" + nestFieldName + "\");\n");
                    sbScript.append("    } catch (e) {}\n");
                    continue;
                }

                fdaoNest.setFlowId(flowId);
                fdaoNest.setCwsId(String.valueOf(parentId));
                fdaoNest.setCreator(privilege.getUser(request));
                fdaoNest.setUnitCode(privilege.getUserUnitCode(request));
                fdaoNest.setCwsQuoteId(id);
                fdaoNest.setCwsParentForm(parentFormCode);
                fdaoNest.setCwsQuoteForm(formCodeSource);
                if (flowId != com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
                    fdaoNest.setCwsStatus(com.redmoon.oa.flow.FormDAO.STATUS_NOT);
                }
                boolean re = false;
                try {
                    re = fdaoNest.create();
                } catch (SQLException throwables) {
                    throw new ErrMsgException(throwables.getMessage());
                }
                if (re) {
                    RequestUtil.setFormDAO(request, fdaoNest);

                    long fdaoId = fdaoNest.getId();
                    // 如果是嵌套表格2
                    if (nestType.equals(ConstUtil.NEST_SHEET)) {
                        MacroCtlMgr mm = new MacroCtlMgr();
                        // LogUtil.getLog(getClass()).info(getClass() + " nestFormCode=" + nestFormCode);
                        String tds = "";
                        String token = "#@#";
                        for (int n = 0; n < len; n++) {
                            String fieldName = fields[n];
                            String v = "";
                            if (fieldName.startsWith("main:")) {
                                String[] subFields = fieldName.split(":");
                                if (subFields.length == 3) {
                                    FormDb subfd = new FormDb(subFields[1]);
                                    com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
                                    FormField subff = subfd.getFormField(subFields[2]);
                                    String subsql = "select id from " + subfdao.getTableName() + " where cws_id='" + id + "' order by cws_order";
                                    JdbcTemplate jt = new JdbcTemplate();
                                    try {
                                        ResultIterator ri = jt.executeQuery(subsql);
                                        if (ri.hasNext()) {
                                            ResultRecord rr = ri.next();
                                            int subid = rr.getInt(1);
                                            subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
                                            String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                            if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                                MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
                                                if (mu != null) {
                                                    subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                                }
                                            }
                                            v = subFieldValue;
                                        }
                                    } catch (SQLException e) {
                                        LogUtil.getLog(getClass()).error(e);
                                    }
                                }
                            } else if (fieldName.startsWith("other:")) {
                                v = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
                            } else {
                                v = StrUtil.getNullStr(fdaoNest.getFieldHtml(request, fieldName));
                            }
                            if (n == 0) {
                                tds = v;
                            } else {
                                tds += token + v;
                            }
                        }

                        // 如果有父窗口
                        // sbScript.append("try {\n");
                        if (parentId == -1) {
                            sbScript.append("window.addTempCwsId(\"" + nestFormCode + "\", " + fdaoId + ");\n");
                            sbScript.append("window.insertRow_" + nestFormCode + "(\"" + nestFormCode + "\", " + fdaoId + ", \"" + tds.replaceAll("\"", "\\\\\"") + "\", \"" + token + "\", true);\n");
                        }
                        // sbScript.append("    } catch (e) {}\n");
                    }
                }
            }

            if (nestType.equals(ConstUtil.NEST_SHEET) || nestType.equals(ConstUtil.NEST_DETAIL_LIST)) {
                sbScript.append("fireEventSelect_" + nestFormCode + "();\n");
            } else if (ConstUtil.NEST_TABLE.equals(nestType)) {
                sbScript.append("refreshNestTableCtl" + nestFieldName + "();\n");
            } else {
                sbScript.append("window.location.reload();\n");
            }
        }
        return sbScript.toString();
    }

    // @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public void doImport(HttpServletRequest request, com.alibaba.fastjson.JSONArray ary, long parentId, int templateId, String code, String moduleCodeRelated, FileUpload fu) throws ErrMsgException {
        Privilege privilege = new Privilege();
        String unitCode = privilege.getUserUnitCode(request);
        MacroCtlMgr mm = new MacroCtlMgr();
        ModuleSetupDb msd = new ModuleSetupDb();
        if (parentId == -1) {
            msd = msd.getModuleSetupDbOrInit(code);
        } else {
            msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        }

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");

        com.alibaba.fastjson.JSONArray arr = null;
        com.alibaba.fastjson.JSONArray aryCleans = null;
        if (templateId != -1) {
            ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
            mid = mid.getModuleImportTemplateDb(templateId);
            String rules = mid.getString("rules");
            // DebugUtil.i(getClass(), "doImport", rules);
            arr = com.alibaba.fastjson.JSONArray.parseArray(rules);
            if (arr.size() > 0) {
                fields = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject) arr.get(i);
                    fields[i] = json.getString("name");
                }
            }

            String strJson = StrUtil.getNullStr(mid.getString("cleans"));
            if (!"".equals(strJson)) {
                aryCleans = com.alibaba.fastjson.JSONArray.parseArray(strJson);
            }
        }

        String userName = privilege.getUser(request);

        FormDb fdRelate = new FormDb();
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);

        Vector<FormDAO> records = new Vector<>();

        JdbcTemplate jt = new JdbcTemplate();
        jt.setAutoClose(false);
        // 先创建主模块记录
        try {
            // 记录不允许重复的字段组合
            Vector<String> vFieldCanNotRepeat = new Vector<>();
            Vector<String> vFieldCanNotEmpty = new Vector<>();

            if (templateId != -1) {
                ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
                mid = mid.getModuleImportTemplateDb(templateId);
                String rules = mid.getString("rules");
                arr = com.alibaba.fastjson.JSONArray.parseArray(rules);
                if (arr.size() > 0) {
                    fields = new String[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject) arr.get(i);
                        fields[i] = json.getString("name");
                        int canNotRepeat = json.getIntValue("canNotRepeat");
                        if (canNotRepeat == 1) {
                            vFieldCanNotRepeat.addElement(fields[i]);
                        }

                        int canNotEmpty = json.getIntValue("canNotEmpty");
                        if (canNotEmpty == 1) {
                            vFieldCanNotEmpty.addElement(fields[i]);
                        }
                    }
                }
            }

            // 检查是否存在重复记录
            StringBuilder sbError = new StringBuilder();
            Set<String> setEmpty = new HashSet<>();
            Set<String> setRepeat = new HashSet<>();
            Map<String, Set> map = new HashMap<>();

            int rowCount = ary.size();
            for (int r = 0; r < rowCount; r++) {
                com.alibaba.fastjson.JSONObject jo = (com.alibaba.fastjson.JSONObject) ary.get(r);

                // 检查主表中是否已存在重复记录，如果已存在，则提取出记录的ID
                if (templateId != -1) {
                    // StringBuffer conds = new StringBuffer();
                    for (String fieldName : vFieldCanNotRepeat) {
                        if (!fieldName.startsWith("nest.")) {
                            String sql = "select id from ft_" + formCode + " where " + fieldName + "=" + StrUtil.sqlstr(jo.getString(fieldName));
                            ResultIterator ri = jt.executeQuery(sql);
                            if (ri.hasNext()) {
                                // sbRepeat.append(fd.getFormField(fieldName).getTitle() + " 存在重复值 " + jo.getString(fieldName) + "\r\n");
                                setRepeat.add(fd.getFormField(fieldName).getTitle() + " 存在重复值 " + jo.getString(fieldName));
                            }
                        }

                        if (map.get(fieldName) == null) {
                            Set<String> set = new HashSet<>();
                            set.add(jo.getString(fieldName));
                            map.put(fieldName, set);
                        } else {
                            Set<String> set = map.get(fieldName);
                            // 判断上传的Excel记录中是否存在重复值
                            if (!set.contains(jo.getString(fieldName))) {
                                set.add(jo.getString(fieldName));
                            } else {
                                setRepeat.add("表格文件中 " + fd.getFormField(fieldName).getTitle() + " 存在重复值 " + jo.getString(fieldName));
                            }
                        }
                    }

                    for (String fieldName : vFieldCanNotEmpty) {
                        if (!fieldName.startsWith("nest.")) {
                            if (StrUtil.isEmpty(jo.getString(fieldName))) {
                                setEmpty.add(fd.getFormField(fieldName).getTitle() + " 存在空值");
                            }
                        }
                    }
                }
            }

            if (setEmpty.size() > 0) {
                sbError.append(String.join("; ", setEmpty));
            }
            if (setRepeat.size() > 0) {
                sbError.append("; ");
                sbError.append(String.join("; ", setRepeat));
            }
            if (sbError.length() > 0) {
                throw new ErrMsgException(sbError.toString());
            }

            for (int r = 0; r < rowCount; r++) {
                com.alibaba.fastjson.JSONObject jo = (com.alibaba.fastjson.JSONObject) ary.get(r);

                FormDAO fdao = new FormDAO(fd);
                for (String field : fields) {
                    if ("cws_creator".equals(field)) {
                        fdao.setCreator(userName);
                    } else {
                        if (templateId != -1) {
                            if (field.startsWith("nest.")) {
                                continue;
                            }
                        }
                        String val = "";
                        if (jo.containsKey(field)) {
                            val = jo.getString(field);
                        } else {
                            LogUtil.getLog(getClass()).error("字段：" + field + " 在导入的文件中不存在");
                            continue;
                        }
                        FormField ff = fd.getFormField(field);
                        if (ff == null) {
                            LogUtil.getLog(getClass()).error("字段：" + field + " 已不存在");
                            continue;
                        }
                        if (ff.getType().equals(FormField.TYPE_SELECT)) {
                            String[][] optionsArray = FormParser.getOptionsArrayOfSelect(fd, ff);
                            for (String[] optionsItem : optionsArray) {
                                if (optionsItem[0].equals(val)) {
                                    val = optionsItem[1];
                                    break;
                                }
                            }
                        } else if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null && !"macro_raty".equals(mu.getCode())) {
                                // 如果是基础数据宏控件
                                boolean isClean = false;
                                if ("macro_flow_select".equals(mu.getCode())) {
                                    com.alibaba.fastjson.JSONObject json = null;
                                    if (aryCleans != null) {
                                        for (int i = 0; i < aryCleans.size(); i++) {
                                            json = aryCleans.getJSONObject(i);
                                            if (ff.getName().equals(json.get("fieldName"))) {
                                                isClean = true;
                                                break;
                                            }
                                        }
                                    }
                                    // 如果需清洗数据
                                    if (isClean) {
                                        if (json.containsKey(val)) {
                                            val = json.getString(val);
                                        } else {
                                            DebugUtil.w(getClass(), json.get("fieldName") + " 清洗", val + "不存在");
                                        }
                                    }
                                }
                                if (!isClean) {
                                    val = mu.getIFormMacroCtl().getValueByName(ff, val);
                                }
                            }
                        }
                        fdao.setFieldValue(field, val);
                    }
                }
                fdao.setCreator(userName);
                fdao.setUnitCode(unitCode);
                if (parentId != -1) {
                    fdao.setCwsId(String.valueOf(parentId));
                }
                fdao.create();
                // 如果需要记录历史
                if (fd.isLog()) {
                    FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
                }
                long mainId = fdao.getId();

                records.addElement(fdao);

                // 创建从模块记录
                ModuleRelateDb mrd = new ModuleRelateDb();
                Vector<ModuleRelateDb> v = mrd.getModulesRelated(formCode);
                // 遍历所有从模块，并创建从模块的记录
                for (ModuleRelateDb moduleRelateDb : v) {
                    mrd = moduleRelateDb;

                    String relateCode = mrd.getString("relate_code");
                    fdRelate = fdRelate.getFormDb(relateCode);
                    if (!fdRelate.isLoaded()) {
                        DebugUtil.e(getClass(), "doImport", "关联模块表单: " + relateCode + " 不存在");
                        continue;
                    }

                    FormDAO fdaoRelate = new FormDAO(fdRelate);

                    // 在配置中是否有从模块中的字段
                    boolean isFind = false;

                    for (int m = 0; m < fields.length; m++) {
                        if ("cws_creator".equals(fields[m])) {
                            fdaoRelate.setCreator(userName);
                        } else {
                            String fieldName = fields[m];
                            if (templateId != -1) {
                                if (!fieldName.startsWith("nest.")) {
                                    continue;
                                }
                                int p = fieldName.indexOf(".");
                                int q = fieldName.lastIndexOf(".");
                                String formCodeRelate = fieldName.substring(
                                        p + 1, q);
                                // 如果不是对应的从模块，则跳过
                                if (!formCodeRelate.equals(relateCode)) {
                                    continue;
                                }

                                isFind = true;
                                fieldName = fieldName.substring(q + 1);
                            }
                            String val = jo.getString(fields[m]);
                            FormField ff = fdRelate.getFormField(fieldName);
                            if (ff == null) {
                                LogUtil.getLog(getClass()).error(
                                        "字段：" + fieldName + " 已不存在");
                                continue;
                            }
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null
                                        && !"macro_raty".equals(mu.getCode())) {
                                    // 如果是基础数据宏控件
                                    val = mu.getIFormMacroCtl().getValueByName(ff, val);
                                }
                            }
                            fdaoRelate.setFieldValue(fieldName, val);
                        }
                    }

                    if (isFind) {
                        String relateFieldValue = fdm.getRelateFieldValue(mainId, relateCode);
                        fdaoRelate.setCwsId(relateFieldValue);
                        fdaoRelate.setUnitCode(unitCode);
                        fdaoRelate.create();
                        // 如果需要记录历史
                        if (fdRelate.isLog()) {
                            FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdaoRelate);
                        }
                    }
                }
            }

            // 导入后事件
            ModuleSetupDb vsd = new ModuleSetupDb();
            vsd = vsd.getModuleSetupDbOrInit(formCode);
            String script = vsd.getScript("import_create");
            if (script != null && !"".equals(script)) {
                BSHShell bs = new BSHShell();
                Privilege pvg = new Privilege();
                bs.set("userName", pvg.getUser(request));

                bs.set("records", records);
                bs.set("request", request);
                bs.set("fileUpload", fu);

                bs.eval(script);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            jt.close();
        }
    }

    /**
     * 取得在位编辑的字段相关属性
     *
     * @param aryEditable
     * @param aryEditableOpt
     * @param aryChkPresent
     * @param mpd
     * @param fd
     */
    public void getEditInplaceProps(com.alibaba.fastjson.JSONArray aryEditable, com.alibaba.fastjson.JSONArray aryEditableOpt, com.alibaba.fastjson.JSONArray aryChkPresent, ModulePrivDb mpd, FormDb fd) {
        String userName = authUtil.getUserName();
        // 取得当前用户的可写字段，对在位编辑的字段进行初始化
        String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
        if (fieldWrite != null && !"".equals(fieldWrite)) {
            String[] fds = StrUtil.split(fieldWrite, ",");
            if (fds != null) {
                for (String fieldName : fds) {
                    FormField ff = fd.getFormField(fieldName);
                    if (ff.getType().equals(FormField.TYPE_TEXTAREA) || ff.getType().equals(FormField.TYPE_TEXTFIELD)
                            || ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME) || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                        com.alibaba.fastjson.JSONObject jsonEditable = new com.alibaba.fastjson.JSONObject();
                        jsonEditable.put("fieldName", fieldName);
                        jsonEditable.put("type", ff.getType());
                        aryEditable.add(jsonEditable);
                        if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                            ff.setValue("1");
                            // 取得present
                            com.alibaba.fastjson.JSONObject jsonChkPresent = new com.alibaba.fastjson.JSONObject();
                            jsonChkPresent.put("fieldName", fieldName);
                            jsonChkPresent.put("present", ff.convertToHtml());
                            aryChkPresent.add(jsonChkPresent);
                        }
                    }
                }
            }
        } else {
            MacroCtlMgr mm = new MacroCtlMgr();
            for (FormField ff : fd.getFields()) {
                switch (ff.getType()) {
                    case FormField.TYPE_MACRO:
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                            String type = ifmc.getControlType();
                            if (type.equals(FormField.TYPE_TEXTFIELD)) {
                                com.alibaba.fastjson.JSONObject jsonEditable = new com.alibaba.fastjson.JSONObject();
                                jsonEditable.put("fieldName", ff.getName());
                                jsonEditable.put("type", FormField.TYPE_TEXTFIELD);
                                aryEditable.add(jsonEditable);
                            } else if (type.equals(FormField.TYPE_SELECT)) {
                                String opts = ifmc.getControlOptions(userName, ff);
                                com.alibaba.fastjson.JSONArray ary = com.alibaba.fastjson.JSONArray.parseArray(opts);

                                com.alibaba.fastjson.JSONObject jsonEditable = new com.alibaba.fastjson.JSONObject();
                                jsonEditable.put("fieldName", ff.getName());
                                jsonEditable.put("type", FormField.TYPE_SELECT);
                                aryEditable.add(jsonEditable);

                                com.alibaba.fastjson.JSONObject jsonOpt = new com.alibaba.fastjson.JSONObject();
                                jsonOpt.put("fieldName", ff.getName());
                                jsonOpt.put("options", ary);
                                aryEditableOpt.add(jsonOpt);
                            }
                        }
                        break;
                    case FormField.TYPE_SELECT: {
                        String[][] aryOpt = FormParser.getOptionsArrayOfSelect(fd, ff);
                        com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();
                        for (String[] strings : aryOpt) {
                            JSONObject json = new JSONObject();
                            json.put("value", strings[1]);
                            json.put("name", strings[0]);
                            ary.add(json);
                        }
                        com.alibaba.fastjson.JSONObject jsonEditable = new com.alibaba.fastjson.JSONObject();
                        jsonEditable.put("fieldName", ff.getName());
                        jsonEditable.put("type", FormField.TYPE_SELECT);
                        aryEditable.add(jsonEditable);

                        com.alibaba.fastjson.JSONObject jsonOpt = new com.alibaba.fastjson.JSONObject();
                        jsonOpt.put("fieldName", ff.getName());
                        jsonOpt.put("options", ary);
                        aryEditableOpt.add(jsonOpt);
                        break;
                    }
                    case FormField.TYPE_TEXTAREA:
                    case FormField.TYPE_TEXTFIELD:
                    case FormField.TYPE_DATE:
                    case FormField.TYPE_DATE_TIME:
                    case FormField.TYPE_CHECKBOX: {
                        com.alibaba.fastjson.JSONObject jsonEditable = new com.alibaba.fastjson.JSONObject();
                        jsonEditable.put("fieldName", ff.getName());
                        jsonEditable.put("type", ff.getType());
                        aryEditable.add(jsonEditable);

                        if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                            ff.setValue("1");
                            // 取得present
                            com.alibaba.fastjson.JSONObject jsonChkPresent = new com.alibaba.fastjson.JSONObject();
                            jsonChkPresent.put("fieldName", ff.getName());
                            jsonChkPresent.put("present", ff.convertToHtml());
                            aryChkPresent.add(jsonChkPresent);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 用于列表页中取得嵌套表格记录
     *
     * @param request
     * @param parentId
     * @param moduleCode
     * @param moduleCodeRelated
     * @return
     */
    public com.alibaba.fastjson.JSONArray getNestRows(HttpServletRequest request, long parentId, String moduleCode, String moduleCodeRelated) {
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDbOrInit(moduleCode);
        String formCode = parentMsd.getString("form_code");

        String mode = ParamUtil.get(request, "mode");
        String tagName = ParamUtil.get(request, "tagName");

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        if (msd == null) {
            DebugUtil.e(getClass(), "getRowsRelated", "模块：" + moduleCodeRelated + "不存在！");
            return new com.alibaba.fastjson.JSONArray();
        }

        String formCodeRelated = msd.getString("form_code");

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
        String relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
        if (relateFieldValue == null) {
            // 20171016 fgf 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
            relateFieldValue = SQLBuilder.IS_NOT_RELATED;
            // out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
            // return;
        }

        // String op = ParamUtil.get(request, "op");
        String op = "search"; // 应该始终为search，否则进入module_list.jsp时，如果op为空，则unitCode不会被处理，因为在search时，ModuleSetupDb中，unitCode默认为0，表示本单位
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");
        String[] fieldsLink = msd.getColAry(false, "list_field_link");

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCodeRelated);

        String sort = "";
        String orderBy = ""; // orderBy
        String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
        boolean isComb = filter.startsWith("<items>") || "".equals(filter);
        // 如果是组合条件，则赋予后台设置的排序字段
        if (isComb) {
            orderBy = StrUtil.getNullStr(msd.getString("orderby"));
            sort = StrUtil.getNullStr(msd.getString("sort"));
        }
        if ("".equals(orderBy)) {
            orderBy = "id";
        }

        if ("ascend".equals(sort)) {
            sort = "asc";
        } else if ("descend".equals(sort)) {
            sort = "desc";
        }
        // 注意在组合条件中可能设了sort
        if ("".equals(sort)) {
            sort = "desc";
        }

        // 用于传过滤条件
        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
        String[] ary = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);

        String sql = ary[0];

        DebugUtil.log(getClass(), "getRowsRelated", "sql=" + sql);

        FormDAO fdao = new FormDAO();

        int pagesize = ParamUtil.getInt(request, "pageSize", 20);
        int curpage = ParamUtil.getInt(request, "page", 1);

        ListResult lr = null;
        try {
            lr = fdao.listResult(formCodeRelated, sql, curpage, pagesize);
        } catch (ErrMsgException e1) {
            DebugUtil.e(getClass(), "getRowsRelated", e1.getMessage());
            return new com.alibaba.fastjson.JSONArray();
        }

        String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
        String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
        String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
        boolean isPrompt = false;
        if (!"".equals(promptField) && !"".equals(promptIcon)) {
            isPrompt = true;
        }

        boolean canView = true;
        boolean canModify = mpd.canUserModify(userName);
        boolean canDel = mpd.canUserDel(userName);
        boolean canManage = mpd.canUserManage(userName);
        boolean canLog = mpd.canUserLog(userName);
        boolean canReActive = mpd.canUserReActive(privilege.getUser(request));

        MacroCtlMgr mm = new MacroCtlMgr();
        UserMgr um = new UserMgr();
        WorkflowDb wf = new WorkflowDb();
        com.alibaba.fastjson.JSONArray rows = new com.alibaba.fastjson.JSONArray();
        /*jobject.put("list", rows);
        jobject.put("page", curpage);
        jobject.put("total", lr.getTotal());*/

        for (Object o : lr.getResult()) {
            fdao = (FormDAO) o;

            RequestUtil.setFormDAO(request, fdao);

            JSONObject jo = new JSONObject();

            long id = fdao.getId();
            jo.put("id", String.valueOf(id));

            for (String fieldName : fields) {
                String val = "";

                if (fieldName.startsWith("main:")) {
                    String[] subFields = fieldName.split(":");
                    if (subFields.length == 3) {
                        // 20180730 fgf 此处查询的结果可能为多个，但是这时关联的是主表单，cws_id是唯一的，应该不需要查多个
                        FormDb subfd = new FormDb(subFields[1]);
                        FormDAO subfdao = new FormDAO(subfd);
                        FormField subff = subfd.getFormField(subFields[2]);
                        String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
                        StringBuilder sb = new StringBuilder();
                        try {
                            JdbcTemplate jt = new JdbcTemplate();
                            ResultIterator ri = jt.executeQuery(subsql);
                            while (ri.hasNext()) {
                                ResultRecord rr = ri.next();
                                int subid = rr.getInt(1);
                                subfdao = new FormDAO(subid, subfd);
                                String subFieldValue = subfdao.getFieldValue(subFields[2]);
                                if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
                                    if (mu != null) {
                                        subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
                                    }
                                }
                                sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
                            }
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error(e);
                        }
                        val += sb.toString();
                    }
                } else if (fieldName.startsWith("other:")) {
                    // 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
                    String fName = fieldName;
                    int logType = ParamUtil.getInt(request, "log_type", 0);
                    if (logType == FormDAOLog.LOG_TYPE_DEL) {
                        if ("module_log".equals(formCode)) {
                            if (fName.contains("module_id:")) {
                                int p = fName.indexOf(":");
                                p = fName.indexOf(":", p + 1);
                                String prefix = fName.substring(0, p);
                                fName = fName.substring(p + 1);
                                p = fName.indexOf(":");
                                String endStr = fName.substring(p);
                                if (endStr.startsWith(":id:")) {
                                    // 将id替换为***_log表中的cws_log_id
                                    endStr = ":cws_log_id" + endStr.substring(3);
                                }
                                fName = fName.substring(0, p);
                                fName += "_log";
                                fName = prefix + ":" + fName + endStr;
                            }
                        }
                    }
                    val = FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
                } else if ("ID".equals(fieldName)) {
                    fieldName = "CWS_MID"; // module_list.jsp中也作了同样转换
                    val = String.valueOf(fdao.getId());
                } else if ("cws_progress".equals(fieldName)) {
                    val = String.valueOf(fdao.getCwsProgress());
                } else if ("cws_flag".equals(fieldName)) {
                    val = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
                } else if ("cws_creator".equals(fieldName)) {
                    String realName = "";
                    if (fdao.getCreator() != null) {
                        UserDb user = um.getUserDb(fdao.getCreator());
                        if (user != null) {
                            realName = user.getRealName();
                        }
                    }
                    val = realName;
                } else if ("flowId".equals(fieldName)) {
                    val = String.valueOf(fdao.getFlowId());
                } else if ("cws_status".equals(fieldName)) {
                    val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                } else if ("cws_create_date".equals(fieldName)) {
                    val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
                } else if ("flow_begin_date".equals(fieldName)) {
                    int flowId = fdao.getFlowId();
                    if (flowId != -1) {
                        wf = wf.getWorkflowDb(flowId);
                        val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
                    }
                } else if ("flow_end_date".equals(fieldName)) {
                    int flowId = fdao.getFlowId();
                    if (flowId != -1) {
                        wf = wf.getWorkflowDb(flowId);
                        val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
                    }
                } else if ("cws_id".equals(fieldName)) {
                    val = fdao.getCwsId();
                } else if ("cws_visited".equals(fieldName)) {
                    val = fdao.isCwsVisited() ? "是" : "否";
                } else {
                    FormField ff = fdao.getFormField(fieldName);
                    if (ff == null) {
                        val += "不存在！";
                    } else {
                        if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null) {
                                val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
                            }
                        } else {
                            val = FuncUtil.renderFieldValue(fdao, ff);
                        }
                    }
                }

                jo.put(fieldName, val);
            }

            // jo.put("colOperate", getColOperateBtn(request, userName, msd, fd, fdao, canView, canModify, canDel, canManage, canLog, canReActive));

            rows.add(jo);
        }
        return rows;
    }

    public JSONObject list(ModuleSetupDb msd, String userName, boolean isTreeView, boolean isModuleListSel, boolean isModuleListNestSel, ModulePrivDb mpd, boolean canView) throws ErrMsgException {
        String op = "search"; // 应该始终为search，否则进入module_list.jsp时，如果op为空，则unitCode不会被处理，因为在search时，ModuleSetupDb中，unitCode默认为0，表示本单位

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");
        String[] fieldsLink = msd.getColAry(false, "list_field_link");

        String formCode = msd.getString("form_code");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        String sort = ParamUtil.get(request, "order"); // "sort";
        String orderBy = ParamUtil.get(request, "field"); // orderBy
        if ("".equals(orderBy)) {
            String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
            boolean isComb = filter.startsWith("<items>") || "".equals(filter);
            // 如果是组合条件，则赋予后台设置的排序字段
            if (isComb) {
                orderBy = StrUtil.getNullStr(msd.getString("orderby"));
                sort = StrUtil.getNullStr(msd.getString("sort"));
            }
            if ("".equals(orderBy)) {
                orderBy = "id";
            }
        }

        if ("ascend".equals(sort)) {
            sort = "asc";
        } else if ("descend".equals(sort)) {
            sort = "desc";
        }
        // 注意在组合条件中可能设了sort
        if ("".equals(sort)) {
            sort = "desc";
        }

        // 用于传过滤条件
        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

        // ---------------module_list_sel 开始----------------------------
        com.alibaba.fastjson.JSONObject moduleFieldSelectCtlDesc = null;
        FormField openerField;
        String showFieldName = "", openerFormCode = "", openerFieldName = "";
        if (isModuleListSel) {
            FormDb openerFd;
            openerFormCode = ParamUtil.get(request, "openerFormCode");
            if ("".equals(openerFormCode)) {
                throw new ErrMsgException("openerFormCode不能为空");
            }
            openerFieldName = ParamUtil.get(request, "openerFieldName");
            openerFd = new FormDb();
            openerFd = openerFd.getFormDb(openerFormCode);
            if (!openerFd.isLoaded()) {
                throw new ErrMsgException("表单：" + openerFormCode + " 不存在");
            }
            openerField = openerFd.getFormField(openerFieldName);
            if (openerField == null) {
                throw new ErrMsgException("字段：" + openerFieldName + " 在表单：" + openerFormCode + " 中不存在");
            }
            MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
            IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
            String desc = moduleFieldSelectCtl.formatJSONString(openerField.getDescription());
            moduleFieldSelectCtlDesc = com.alibaba.fastjson.JSONObject.parseObject(desc);
            // String byFieldName = moduleFieldSelectCtlDesc.getString("idField");
            showFieldName = moduleFieldSelectCtlDesc.getString("showField");

            boolean isUseModuleFilter = true;
            if (moduleFieldSelectCtlDesc.containsKey("isUseModuleFilter")) {
                isUseModuleFilter = moduleFieldSelectCtlDesc.getBoolean("isUseModuleFilter");
            }
            String filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(moduleFieldSelectCtlDesc.getString("filter"));
            if ("none".equals(filter)) {
                filter = "";
            }
            request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);
            request.setAttribute(ModuleUtil.NEST_SHEET_FILTER_USE_MODULE, isUseModuleFilter);
        }
        // --------------------module_list_sel 结束-------------------------

        double t = System.currentTimeMillis();
        // --------------------module_list_sel_nest.jsp 开始-------------------------
        // boolean isModuleListNestSel = ParamUtil.getBoolean(request, "isModuleListNestSel", false);
        if (isModuleListNestSel) {
            String nestFormCode = ParamUtil.get(request, "nestFormCode");
            String nestType = ParamUtil.get(request, "nestType");
            String parentFormCode = ParamUtil.get(request, "parentFormCode");
            String nestFieldName = ParamUtil.get(request, "nestFieldName");
			/*long parentId = ParamUtil.getLong(request, "parentId", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
			int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;*/

            FormDb pForm = new FormDb();
            pForm = pForm.getFormDb(parentFormCode);
            FormField nestField = pForm.getFormField(nestFieldName);

            com.alibaba.fastjson.JSONObject nestCtlDesc;
            String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
            nestCtlDesc = com.alibaba.fastjson.JSONObject.parseObject(defaultVal);
            String sourceForm = nestCtlDesc.getString("sourceForm");

            // @task:需优化，之前已取msd，这儿又重复取
            msd = msd.getModuleSetupDb(sourceForm);
            fields = msd.getColAry(false, "list_field");
            fieldsLink = msd.getColAry(false, "list_field_link");

            nestFormCode = nestCtlDesc.getString("destForm");
            String filter = nestCtlDesc.getString("filter");
            request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
            request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);
        }

        // 如果设了前端显示哪些列，则只取这些列
        String frontColProps = msd.getString("front_col_props");
        com.alibaba.fastjson.JSONArray colProps = com.alibaba.fastjson.JSONArray.parseArray(frontColProps);
        if (colProps != null && colProps.size() > 0) {
            List<String> fieldList = new ArrayList<>();
            List<String> fieldLinkList = new ArrayList<>();
            for (Object o : colProps) {
                JSONObject json = (JSONObject) o;
                for (int i = 0; i < fields.length; i++) {
                    if (json.getString("field").equals(fields[i])) {
                        fieldList.add(fields[i]);
                        fieldLinkList.add(fieldsLink[i]);
                        break;
                    }
                }
            }
            fields = fieldList.toArray(new String[fieldList.size()]);
            fieldsLink = fieldLinkList.toArray(new String[fieldLinkList.size()]);
        }

        String[] ary;
        try {
            ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
        } catch (ErrMsgException e) {
            throw new ErrMsgException(e.getMessage());
        }

        String sql = ary[0];
        // 如果是日志表，则与需模块相关联，并能支持副模块
        if ("module_log".equals(formCode) || "module_log_read".equals(formCode)) {
            sql = SQLBuilder.getListSqlForLogRelateModule(request, sql, "module_log_read".equals(formCode));
        }
        DebugUtil.log(getClass(), "moduleList", "sql=" + sql);
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list getListSqlForLogRelateModule time", "" + (System.currentTimeMillis() - t) / 1000);
        }
        // 嵌套表
        boolean hasNestTable = false;
        Vector<ModuleRelateDb> v = null;
        if (msd.getInt("is_expand") == 1) {
            ModuleRelateDb mrd = new ModuleRelateDb();
            v = mrd.getModulesRelated(formCode);
            if (v.size() > 0) {
                hasNestTable = true;
            }
        }

        com.alibaba.fastjson.JSONObject jobject = new com.alibaba.fastjson.JSONObject();

        FormDAO fdao = new FormDAO();

        int pagesize = ParamUtil.getInt(request, "pageSize", 20);
        int curpage = ParamUtil.getInt(request, "page", 1);

        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list listResult before ", "" + (double) (System.currentTimeMillis() - t) / 1000);
        }
        ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);

        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list listResult time", "" + (double) (System.currentTimeMillis() - t) / 1000);
        }

        // String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
        String promptCond = StrUtil.getNullStr(msd.getString("prompt_cond"));
        String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
        boolean isPrompt = false;
        if (!"".equals(promptCond) && !"".equals(promptIcon)) {
            isPrompt = true;
        }

        com.alibaba.fastjson.JSONArray rows = new com.alibaba.fastjson.JSONArray();
        jobject.put("list", rows);
        jobject.put("page", curpage);
        jobject.put("total", lr.getTotal());

        for (Object o : lr.getResult()) {
            fdao = (FormDAO) o;

            JSONObject jo = getRow(msd, fd, fdao, isPrompt, promptIcon, fields, showFieldName, fieldsLink,
                    canView, mpd, hasNestTable, isTreeView, v, isModuleListSel, isModuleListNestSel);
            rows.add(jo);
        }

        DebugUtil.i(getClass(), "hasNestTable", String.valueOf(hasNestTable) + " propStat=" + msd.getString("prop_stat"));

        if (rows.size() > 0) {
            String propStat = msd.getString("prop_stat");
            if (StringUtils.isNotEmpty(propStat)) {
                if ("".equals(propStat)) {
                    propStat = "{}";
                }
                int n = 0;
                JSONObject json = JSONObject.parseObject(propStat);
                com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();
                for (String fieldName : json.keySet()) {
                    String modeStat = json.getString(fieldName);

                    FormField ff = fd.getFormField(fieldName);
                    if (ff == null) {
                        DebugUtil.e(getClass(), "moduleList", "field:" + fieldName + " is not exist");
                        continue;
                    }
                    int fieldType = ff.getFieldType();

                    double sumVal = FormSQLBuilder.getSUMOfSQL(sql, fieldName);
                    if ("0".equals(modeStat)) {
                        if (fieldType == FormField.FIELD_TYPE_INT
                                || fieldType == FormField.FIELD_TYPE_LONG) {
                            jo.put(fieldName, "合计：" + (long) sumVal);
                        } else {
                            jo.put(fieldName, "合计：" + NumberUtil.round(sumVal, 2));
                        }
                    } else if ("1".equals(modeStat)) {
                        jo.put(fieldName, "平均：" + NumberUtil.round(sumVal / lr.getTotal(), 2));
                    }
                    n++;
                }
                if (n > 0) {
                    /*jo.put("id", String.valueOf(ConstUtil.MODULE_ID_STAT));
                    rows.add(jo);*/
                    // jo.put("flag", "INDEX");
                    // 让前端在afterFetch中识别出是合计行
                    jo.put("_row", "合计");
                    rows.add(jo);
                    // summary.add(jo);
                }
            }
        }
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list time", "" + (double) (System.currentTimeMillis() - t) / 1000);
        }
        return jobject;
    }

    public JSONObject getRow(ModuleSetupDb msd, FormDb fd, FormDAO fdao, boolean isPrompt,
                             String promptIcon, String[] fields, String showFieldName, String[] fieldsLink,
                             boolean canView, ModulePrivDb mpd, boolean hasNestTable, boolean isTreeView, Vector<ModuleRelateDb> v,
                             boolean isModuleListSel, boolean isModuleListNestSel) {
        RequestUtil.setFormDAO(request, fdao);

        String userName = authUtil.getUserName();
        WorkflowDb wf = new WorkflowDb();
        boolean isShowFieldFound = false;
        com.alibaba.fastjson.JSONObject jo = new com.alibaba.fastjson.JSONObject();

        // prompt 图标
        if (isPrompt) {
            // 判断条件
            if (ModuleUtil.isPrompt(request, msd, fdao)) {
                jo.put("colPrompt", promptIcon);
            }
        }

        long id = fdao.getId();
        // id小写兼容flexigrid
        jo.put("id", String.valueOf(id));
        // layui table需对应大写ID
        jo.put("ID", String.valueOf(id));

        MacroCtlMgr mm = new MacroCtlMgr();
        long tRow = System.currentTimeMillis();
        // DebugUtil.i(getClass(), "--- list row start id", String.valueOf(id));
        MutableDouble tFieldSum = new MutableDouble(0);
        int k = 0;
        for (String fieldName : fields) {
            setRowField(jo, fieldName, msd, fd, fdao, mm, wf, isShowFieldFound, showFieldName, fieldsLink, canView, k, tFieldSum, tRow);
            k++;
        }

        String tabTitle = "";
        String strProps = msd.getString("props");
        if (!StrUtil.isEmpty(strProps)) {
            JSONObject props = JSONObject.parseObject(msd.getString("props"));
            if (props.containsKey("tabTitle") && !StrUtil.isEmpty(props.getString("tabTitle"))) {
                tabTitle = props.getString("tabTitle");
                tabTitle = ModuleUtil.parseField(fdao, tabTitle);
                if (tabTitle.length() > sysProperties.getTabTitleMaxLen()) {
                    tabTitle = tabTitle.substring(0, sysProperties.getTabTitleMaxLen());
                }
            }
        }
        jo.put("tabTitle", tabTitle);

        //设置子线程共享，否则会报：No thread-bound request found，注意parallelStream数据量少的时候，反而运行效率不高
        /*ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(servletRequestAttributes,true);

        java.util.ArrayList<String> list = new ArrayList<>(fields.length);
        Collections.addAll(list, fields);
        list.parallelStream().forEach((fieldName) -> {
            setRowField(jo, fieldName, msd, fd, fdao, mm, wf,
                    isShowFieldFound, showFieldName, fieldsLink, canView, k, tFieldSum, tRow);
        });
        */
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list row end", " " + (double) (System.currentTimeMillis() - tRow) / 1000);
        }
        long tBtn = System.currentTimeMillis();

        // 置颜色及是否加粗，以便于前端在插槽中处理
        FormField ffColor = fdao.getFormField("color");
        if (ffColor != null) {
            jo.put("color", ffColor.getValue());
        }
        FormField ffIsBold = fdao.getFormField("is_bold");
        if (ffIsBold != null) {
            jo.put("is_bold", ffIsBold.getValue());
        }

        long tPriv = System.currentTimeMillis();
        boolean canModify;
        boolean canDel;
        boolean canManage = mpd.canUserManage(userName);
        boolean canLog = mpd.canUserLog(userName);
        boolean canReActive = mpd.canUserReActive(userName);

        if (isTreeView) {
            String nodeCode = ParamUtil.get(request, "treeNodeCode");
            canModify = moduleTreePermission.canEdit(userName, nodeCode);
            canDel = moduleTreePermission.canDel(userName, nodeCode);
        } else {
            canModify = mpd.canUserModify(userName);
            canDel = mpd.canUserDel(userName);
        }
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list tPriv", String.valueOf((double) (System.currentTimeMillis() - tPriv) / 1000));
        }
        if (isModuleListSel || isModuleListNestSel) {
            jo.put("colOperate", getColOperateForListSel(request, userName, msd, fd, fdao, mpd, canView));
        } else {
            jo.put("colOperate", getColOperateBtn(request, userName, msd, fd, fdao, mpd, canView, canModify, canDel, canManage, canLog, canReActive));
        }
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list operateBtn", id + " " + (double) (System.currentTimeMillis() - tBtn) / 1000);
        }
        // 取嵌套表格记录
        if (hasNestTable) {
            com.alibaba.fastjson.JSONArray nestTableAry = new com.alibaba.fastjson.JSONArray();
            for (ModuleRelateDb mrd : v) {
                String moduleCodeRelated = mrd.getString("relate_code");
                com.alibaba.fastjson.JSONArray nestAry = getNestRows(request, fdao.getId(), msd.getCode(), moduleCodeRelated);
                nestTableAry.add(nestAry);
            }
            jo.put("nestTableAry", nestTableAry);
        }

        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "-----list row end id", id + " " + (double) (System.currentTimeMillis() - tRow) / 1000);
        }
        return jo;
    }

    public void setRowField(JSONObject jo, String fieldName, ModuleSetupDb msd, FormDb fd, FormDAO fdao, MacroCtlMgr mm, WorkflowDb wf,
                            boolean isShowFieldFound, String showFieldName, String[] fieldsLink, boolean canView, int i, MutableDouble tFieldSum, double tRow) {
        // DebugUtil.i(getClass(), "list field start", fieldName);

        long id = fdao.getId();
        long tField = System.currentTimeMillis();

        String val = ""; // fdao.getFieldValue(fieldName);
        com.alibaba.fastjson.JSONObject fieldLink = new com.alibaba.fastjson.JSONObject();

        if (fieldName.startsWith("main:")) {
            String[] mainFields = fieldName.split(":");
            if (mainFields.length == 3) {
                // 20180730 fgf 此处查询的结果可能为多个，但是这时关联的是主表单，cws_id是唯一的，应该不需要查多个
                FormDb mainfd = new FormDb();
                mainfd = mainfd.getFormDb(mainFields[1]);
                FormDAO maindao = new FormDAO(mainfd);
                FormField mainff = mainfd.getFormField(mainFields[2]);
                String mainsql = "select id from " + maindao.getTableName() + " where id=" + fdao.getCwsId() + " order by id asc";
                StringBuilder sb = new StringBuilder();
                try {
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(mainsql);
                    while (ri.hasNext()) {
                        ResultRecord rr = ri.next();
                        long subid = rr.getLong(1);
                        maindao = new FormDAO();
                        maindao = maindao.getFormDAOByCache(subid, mainfd);
                        String mainFieldValue = maindao.getFieldValue(mainFields[2]);
                        if (mainff != null && mainff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(mainff.getMacroType());
                            if (mu != null) {
                                mainFieldValue = mu.getIFormMacroCtl().converToHtml(request, mainff, mainFieldValue);
                            }
                        }
                        sb.append(mainFieldValue).append(ri.hasNext() ? "|" : "");
                    }
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                val += sb.toString();
            }
        } else if (fieldName.startsWith("other:")) {
            // 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
            String fName = fieldName;
            int logType = ParamUtil.getInt(request, "log_type", 0);
            if (logType == FormDAOLog.LOG_TYPE_DEL) {
                if ("module_log".equals(fd.getCode())) {
                    if (fName.contains("module_id:")) {
                        int p = fName.indexOf(":");
                        p = fName.indexOf(":", p + 1);
                        String prefix = fName.substring(0, p);
                        fName = fName.substring(p + 1);
                        p = fName.indexOf(":");
                        String endStr = fName.substring(p);
                        if (endStr.startsWith(":id:")) {
                            // 将id替换为***_log表中的cws_log_id
                            endStr = ":cws_log_id" + endStr.substring(3);
                        }
                        fName = fName.substring(0, p);
                        fName += "_log";
                        fName = prefix + ":" + fName + endStr;
                    }
                }
            }
            val = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
        } else if ("ID".equals(fieldName)) {
            fieldName = "CWS_MID"; // module_list.jsp中也作了同样转换
            val = String.valueOf(fdao.getId());
        } else if ("cws_progress".equals(fieldName)) {
            val = String.valueOf(fdao.getCwsProgress());
        } else if ("cws_flag".equals(fieldName)) {
            val = com.redmoon.oa.flow.FormDAO.getCwsFlagDesc(fdao.getCwsFlag());
        } else if ("cws_creator".equals(fieldName)) {
            String realName = "";
            if (fdao.getCreator() != null) {
                User user = userCache.getUser(fdao.getCreator());
                if (user != null) {
                    realName = user.getRealName();
                } else {
                    LogUtil.getLog(getClass()).warn("用户: " + fdao.getCreator() + " 不存在");
                }
            }
            val = realName;
        } else if ("flowId".equals(fieldName)) {
            // val = "<a href=\"javascript:;\" onclick=\"addTab('流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + fdao.getFlowId() + "')\">" + fdao.getFlowId() + "</a>";
            val = String.valueOf(fdao.getFlowId());
            fieldLink.put("type", "FLOW");
            fieldLink.put("visitKey", SecurityUtil.makeVisitKey(fdao.getFlowId()));
        } else if ("cws_status".equals(fieldName)) {
            val = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
        } else if ("cws_create_date".equals(fieldName)) {
            val = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
        } else if ("cws_modify_date".equals(fieldName)) {
            val = DateUtil.format(fdao.getCwsModifyDate(), "yyyy-MM-dd");
        } else if ("flow_begin_date".equals(fieldName)) {
            int flowId = fdao.getFlowId();
            if (flowId != -1) {
                wf = wf.getWorkflowDb(flowId);
                val = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
            }
        } else if ("flow_end_date".equals(fieldName)) {
            int flowId = fdao.getFlowId();
            if (flowId != -1) {
                wf = wf.getWorkflowDb(flowId);
                val = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
            }
        } else if ("cws_cur_handler".equals(fieldName)) {
            int flowId = fdao.getFlowId();
            if (flowId != -1) {
                wf = wf.getWorkflowDb(flowId);
                MyActionDb mad = new MyActionDb();
                for (MyActionDb madCur : mad.getMyActionDbDoingOfFlow(wf.getId())) {
                    if (!"".equals(val)) {
                        val += "、";
                    }
                    User user = userCache.getUser(madCur.getUserName());
                    if (user != null) {
                        val += user.getRealName();
                    } else {
                        LogUtil.getLog(getClass()).warn("用户: " + madCur.getUserName() + " 不存在");
                    }
                }
            }
        } else if ("cws_id".equals(fieldName)) {
            val = fdao.getCwsId();
        } else if ("cws_visited".equals(fieldName)) {
            val = fdao.isCwsVisited() ? "是" : "否";
        } else {
            FormField ff = fdao.getFormField(fieldName);
            if (ff == null) {
                val += "不存在！";
            } else {
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        IFormMacroCtl macroCtl = mu.getIFormMacroCtl();
                        val = StrUtil.getNullStr(macroCtl.converToHtml(request, ff, fdao.getFieldValue(fieldName)));
                        if (Global.getInstance().isDebug()) {
                            DebugUtil.i(getClass(), "list field converToHtml", fieldName + " " + (double) (System.currentTimeMillis() - tField) / 1000 + " val=" + val);
                        }
                        // 注意得判断，否则会影响效率
                        if (val.length() > 100) {
                            val = StrUtil.getAbstract(request, val, 100, "\r\n");
                        }
                        // IconCtl在列表渲染的时候需用到
                        String mData = macroCtl.getMetaData(ff);
                        // sql宏控件中的mData并不是json格式
                        if (!StrUtil.isEmpty(mData) && mData.startsWith("{")) {
                            jo.put(ff.getName() + "_meta_data", JSONObject.parseObject(mData));
                        }
                        if (Global.getInstance().isDebug()) {
                            DebugUtil.i(getClass(), "list field getMetaData", fieldName + " " + (double) (System.currentTimeMillis() - tField) / 1000);
                        }
                    }
                } else {
                    // DebugUtil.i(getClass(), "list field before renderFieldValue", fieldName + " " + (double)(System.currentTimeMillis() - tField)/1000);
                    val = FuncUtil.renderFieldValue(fdao, ff);
                    // DebugUtil.i(getClass(), "list field after renderFieldValue", fieldName + " " + (double)(System.currentTimeMillis() - tField)/1000);
                }
                if (!isShowFieldFound && ff.getName().equals(showFieldName)) {
                    isShowFieldFound = true;
                    // showValue = val;
                }
            }
        }

        // long tLink = System.currentTimeMillis();
        if (!"#".equals(fieldsLink[i]) && !"&".equals(fieldsLink[i]) && !"@".equals(fieldsLink[i])) {
            fieldLink.put("type", "LINK");
            String route;
            com.alibaba.fastjson.JSONObject query = new com.alibaba.fastjson.JSONObject();
            if (!StrUtil.isEmpty(fieldsLink[i])) {
                String fieldLinkParams = FormUtil.parseAndSetFieldValue(fieldsLink[i], fdao);
                int p = fieldLinkParams.indexOf("?");
                if (p != -1) {
                    route = fieldLinkParams.substring(0, p);
                    String params = fieldLinkParams.substring(p);
                    if (params.length() > 1) {
                        params = params.substring(1);
                        String[] paramAry = params.split("&");
                        for (String str : paramAry) {
                            String[] pair = str.split("=");
                            if (pair.length > 1) {
                                query.put(pair[0], pair[1]);
                            }
                        }
                    }
                } else {
                    route = fieldLinkParams;
                }
            } else {
                route = "";
            }
            fieldLink.put("route", route);
            fieldLink.put("query", query);
        } else if (((i == 0 && "#".equals(fieldsLink[i])) || "&".equals(fieldsLink[i])) && canView) {
            // 在第一列或者fieldsLink[i]为&的列上，生成查看链接
            if (msd.getInt("btn_display_show") == 1) {
                if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                    String visitKey = SecurityUtil.makeVisitKey(id);
                    String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                    fieldLink.put("type", "LINK");
                    fieldLink.put("route", urlShow);
                    com.alibaba.fastjson.JSONObject query = new com.alibaba.fastjson.JSONObject();
                    query.put("parentId", id);
                    query.put("id", id);
                    query.put("code", msd.getCode());
                    query.put("visitKey", visitKey);
                    fieldLink.put("query", query);
                } else {
                    fieldLink.put("type", "SHOW");
                }
            }
        } else if ("@".equals(fieldsLink[i]) && canView) {
            if (fdao.getFlowId() != -1) {
                fieldLink.put("type", "FLOW");
                fieldLink.put("visitKey", SecurityUtil.makeVisitKey(fdao.getFlowId()));
            }
        }

        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list field fieldLink", fieldName + " " + (double) (System.currentTimeMillis() - tField) / 1000 + " fieldsLink[" + i + "]=" + fieldsLink[i]);
        }

        jo.put(fieldName + "_link", fieldLink);

        jo.put(fieldName, val);
        jo.put("flowId", fdao.getFlowId());

        double tSpace = (double) (System.currentTimeMillis() - tField) / (double) 1000;
        tFieldSum.set(tFieldSum.doubleValue() + tSpace);
        if (Global.getInstance().isDebug()) {
            DebugUtil.i(getClass(), "list field end", fieldName + " " + tSpace + " sum=" + tFieldSum.doubleValue() + " rowSum=" + (double) (System.currentTimeMillis() - tRow) / (double) 1000);
        }
    }

    @Async("threadPoolTaskExecutor")
    public void exportExcelAsync(HttpServletResponse response, String userName, ModuleSetupDb msd, String uid, SecurityContext securityContext) throws IOException, ErrMsgException {
        SecurityContextHolder.setContext(securityContext);

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        exportExcel(request, response, userName, msd, uid);
    }

    public void exportExcel(HttpServletRequest request, HttpServletResponse response, String userName, ModuleSetupDb msd, String uid) throws IOException, ErrMsgException {
        // 是否是通过选择列来导出
        boolean isExporBySelCol = false;
        String[] exportCols = null;
        String exportColProps = ParamUtil.get(request, "exportColProps");
        ModulePrivDb mpd = new ModulePrivDb(msd.getCode());
        if (!StrUtil.isEmpty(exportColProps)) {
            if (!mpd.canUserExportXlsCol(userName)) {
                throw new ErrMsgException(i18nUtil.get("pvg_invalid"));
            }
            isExporBySelCol = true;
            if (!exportColProps.equals(msd.getString("export_col_props"))) {
                msd.set("export_col_props", exportColProps);
                try {
                    msd.save();
                } catch (ResKeyException e) {
                    throw new ErrMsgException(e.getMessage(request));
                }
            }

            exportCols = StrUtil.split(exportColProps, ",");
        } else {
            if (!mpd.canUserExport(userName)) {
                throw new ErrMsgException(i18nUtil.get("pvg_invalid"));
            }
        }

        long templateId = -1;
        ModuleExportTemplateDb metd = new ModuleExportTemplateDb();
        if (!isExporBySelCol) {
            templateId = ParamUtil.getLong(request, "templateId", -1);
            // 取默认模板
            metd = metd.getDefault(msd.getCode());
            if (metd != null) {
                templateId = metd.getInt("id");
            }
        }

        request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

        String formCode = msd.getString("form_code");

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        if (!fd.isLoaded()) {
            throw new ErrMsgException("表单不存在！");
        }
        String op = ParamUtil.get(request, "op");
        String orderBy = ParamUtil.get(request, "orderBy");
        String sort = ParamUtil.get(request, "sort");

        if ("".equals(orderBy)) {
            String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
            boolean isComb = filter.startsWith("<items>") || "".equals(filter);
            // 如果是组合条件，则赋予后台设置的排序字段
            if (isComb) {
                orderBy = StrUtil.getNullStr(msd.getString("orderby"));
                sort = StrUtil.getNullStr(msd.getString("sort"));
            }
            if ("".equals(orderBy)) {
                orderBy = "id";
            }
        }

        if ("".equals(sort)) {
            sort = "desc";
        }

        String sql;
        String[] ary = null;

        String rowIds = ParamUtil.get(request, "rowIds");
        if (!"".equals(rowIds)) {
            sql = "select id from " + FormDb.getTableName(fd.getCode()) + " where id in (" + rowIds + ")";
        } else {
            boolean isMine = "true".equals(ParamUtil.get(request, "isMine"));
            if (isMine) {
                ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort, userName, "user_name");
            } else {
                ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
            }
            sql = ary[0];
        }

        if ("module_log".equals(formCode) || "module_log_read".equals(formCode)) {
            sql = SQLBuilder.getListSqlForLogRelateModule(request, sql, "module_log_read".equals(formCode));
        }

        DebugUtil.i(getClass(), "sql", sql);
        // String sqlUrlStr = ary[1];

        String listFieldOrgi = StringUtils.join(msd.getColAry(false,"list_field"), ",");
        String listField;
        String cols = ParamUtil.get(request, "cols");
        // 当表格带有序号时，cols为逗号开头
        if (cols.startsWith(",")) {
            cols = cols.substring(1);
        }
        if (templateId==-1 && !"".equals(cols)) {
            listField = cols;
        }
        else {
            listField = listFieldOrgi;
        }

        // 如果用户自行调整了列，则cols在split后的数组与fieldsTitle不对应，此处重新调整了对应关系
        String[] fieldsTitle;
        String[] fields;
        // 选择列导出时，其中含有的嵌套表
        List<String> nestFormList = new ArrayList<>();
        if (isExporBySelCol) {
            fields = new String[exportCols.length];
            fieldsTitle = new String[exportCols.length];
            int n = 0;
            for (String s : exportCols) {
                fields[n] = s;
                // 嵌套表
                if (s.contains(":")) {
                    String[] aryPair = s.split(":");
                    FormDb fdNest = fd.getFormDb(aryPair[0]);
                    nestFormList.add(aryPair[0]);
                    FormField ff = fdNest.getFormField(aryPair[1]);
                    if (ff != null) {
                        fieldsTitle[n] = ff.getTitle();
                    } else {
                        fieldsTitle[n] = s + " 不存在";
                    }
                }
                else if ("ID".equals(s)) {
                    fieldsTitle[n] = "ID";
                } else if ("flowId".equals(s)) {
                    fieldsTitle[n] = "流程号";
                }
                else {
                    FormField ff = fd.getFormField(s);
                    if (ff != null) {
                        fieldsTitle[n] = ff.getTitle();
                    } else {
                        fieldsTitle[n] = s + " 不存在";
                    }
                }
                n++;
            }
        } else {
            fieldsTitle = msd.getColAry(false, "list_field_title");
            fields = msd.getColAry(false, "list_field");
            if (templateId == -1) {
                if (!"".equals(cols) && !listFieldOrgi.equals(cols)) {
                    fields = StrUtil.split(listField, ",");
                    String[] t = new String[fields.length];
                    // String[] fieldsOrgi = StrUtil.split(listFieldOrgi, ",");
                    String[] fieldsOrgi = msd.getColAry(false, "list_field");

                    LogUtil.getLog(getClass()).info("fields: " + StringUtils.join(fields, ","));
                    LogUtil.getLog(getClass()).info("fieldsTitle: " + StringUtils.join(fieldsTitle, ","));
                    LogUtil.getLog(getClass()).info("fieldsOrgi: " + listFieldOrgi);

                    for (int i = 0; i < fields.length; i++) {
                        for (int j = 0; j < fieldsOrgi.length; j++) {
                            if (fieldsOrgi[j].equals(fields[i])) {
                                t[i] = fieldsTitle[j];
                            }
                        }
                    }
                    fieldsTitle = t;
                }
            }
        }

        String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
        String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
        String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
        boolean isPrompt = false;
        if (!"".equals(promptField) && !"".equals(promptIcon)) {
            isPrompt = true;
        }

        // 是否导出全部字段
        boolean isAll = ParamUtil.getBoolean(request, "isAll", false);
        // 主表字段与嵌套表formCode对应关系
        HashMap<String, String> nestMapping = new HashMap<String, String>();
        // 嵌套表需显示的字段
        HashMap<String, String> nestFieldName = new HashMap<String, String>();
        // 嵌套表需显示的字段的对应名称
        HashMap<String, String[]> nestFields = new HashMap<String, String[]>();
        // 嵌套表的id数据集
        HashMap<String, Vector> nestData = new HashMap<String, Vector>();
        // 列宽
        HashMap<Integer, Integer> columnWidthMap = new HashMap<Integer, Integer>();
        // 所有嵌套表formCode
        ArrayList<String> nestList = new ArrayList<>();
        // isAll = true;
        if (true) {
            Vector<FormField> vt = fd.getFields();
            Iterator<FormField> ir = vt.iterator();
            while (ir.hasNext()) {
                FormField ff = ir.next();
                // 当默认未用模板时，如果嵌套表不显示，则使得list为空，否则会导致表头两行合并为一行，如果嵌套表中有数据，也会出现多行合并为一行的情况
                if (templateId == -1) {
                    boolean isShow = false;
                    for (String field : fields) {
                        if (field.endsWith(ff.getName())) {
                            isShow = true;
                            break;
                        }
                    }
                    if (!isShow) {
                        continue;
                    }
                }
                if ("nest_table".equals(ff.getMacroType()) || "nest_sheet".equals(ff.getMacroType())) {
                    String nestFormCode = ff.getDescription();
                    String defaultVal = StrUtil.decodeJSON(ff.getDescription());
                    JSONObject json = JSONObject.parseObject(defaultVal);
                    nestFormCode = json.getString("destForm");
                    nestMapping.put(ff.getName(), nestFormCode);
                    nestList.add(nestFormCode);
                }
            }
        }

        MacroCtlMgr mm = new MacroCtlMgr();
        String fileName = fd.getName();

        if (templateId != -1) {
            // metd = metd.getModuleExportTemplateDb(templateId);
            fileName = metd.getString("name");
        }

        OutputStream os;
        // 当异步导出Excel时
        if (sysProperties.isExportExcelAsync() && Global.getInstance().isUseCache()) {
           os = new FileOutputStream(FileUpload.getTempPath() + "/" + uid + ".xls");
        } else {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(fileName) + ".xls");
            os = response.getOutputStream();
        }

        String strProps = msd.getString("props");
        com.alibaba.fastjson.JSONObject props = com.alibaba.fastjson.JSONObject.parseObject(strProps);
        if (props == null) {
            props = new com.alibaba.fastjson.JSONObject();
        }

        int exportFormat = 0; // 0表示按系统配置，1为xls, 2为xls(xml)
        if (props.containsKey("exportFormat")) {
            exportFormat = props.getIntValue("exportFormat");
        }

        LogUtil.getLog(getClass()).info("props: " + props.toString());
        LogUtil.getLog(getClass()).info("exportFormat: " + exportFormat);

        if (exportFormat == 0) {
            Config cfg = Config.getInstance();
            if (cfg.getBooleanProperty("moduleExportQuick")) {
                ModuleUtil.exportXml(request, os, fields, fieldsTitle, fd, sql, templateId, isExporBySelCol, nestFormList, uid, msd);
                return;
            }
        } else if (exportFormat == 2) {
            ModuleUtil.exportXml(request, os, fields, fieldsTitle, fd, sql, templateId, isExporBySelCol, nestFormList, uid, msd);
            return;
        }

        Workbook wb = null;
        WritableWorkbook wwb = null;
        try {
            // File file = new File(Global.getAppPath() + "visual/template/blank.xls");
            // wb = Workbook.getWorkbook(file);
            InputStream inputStream = configUtil.getFile("templ/blank.xls");

            wb = Workbook.getWorkbook(inputStream);
            WorkbookSettings settings = new WorkbookSettings();
            settings.setWriteAccess(null);

            UserMgr um = new UserMgr();
            Map map = new HashMap();

            // 打开一个文件的副本，并且指定数据写回到原文件
            wwb = Workbook.createWorkbook(os, wb, settings);
            WritableSheet ws = wwb.getSheet(0);

            for (String ntCode : nestList) {
                ModuleSetupDb nestmsd = new ModuleSetupDb();
                nestmsd = nestmsd.getModuleSetupDbOrInit(ntCode);

                FormDb ntfd = new FormDb();
                ntfd = ntfd.getFormDb(ntCode);

                String ntlistField = StrUtil.getNullStr(nestmsd.getString("list_field"));

                String[] ntfields = StrUtil.split(ntlistField, ",");
                String[] ntfiledsName = new String[ntfields.length];

                Vector<FormField> ntvt = ntfd.getFields();

                if (ntvt.size() == 0) {
                    continue;
                }

                for (FormField ff : ntvt) {
                    for (int i = 0; i < ntfields.length; i++) {
                        if (ff.getName().equals(ntfields[i])) {
                            ntfiledsName[i] = ff.getTitle();
                        }
                    }
                }
                nestFields.put(ntCode, ntfiledsName);
                nestFieldName.put(ntCode, ntlistField);
            }

            int len = 0;
            if (fields != null) {
                len = fields.length;
            }
            int index = 0;

            /*
             * WritableFont.createFont("宋体")：设置字体为宋体
             * 10：设置字体大小
             * WritableFont.NO_BOLD:设置字体非加粗（BOLD：加粗     NO_BOLD：不加粗）
             * false：设置非斜体
             * UnderlineStyle.NO_UNDERLINE：没有下划线
             */

            boolean isBar = false;
            int rowHeader = 0;
            Map mapWidth = new HashMap();
            WritableFont font;
            String backColor = "", foreColor = "";
            if (templateId != -1) {
                String barName = StrUtil.getNullStr(metd.getString("bar_name"));
                if (!"".equals(barName)) {
                    isBar = true;
                }

                String fontFamily = metd.getString("font_family");
                int fontSize = metd.getInt("font_size");
                backColor = metd.getString("back_color");
                foreColor = metd.getString("fore_color");
                boolean isBold = metd.getInt("is_bold") == 1;
                if (isBold) {
                    font = new WritableFont(WritableFont.createFont(fontFamily),
                            fontSize,
                            WritableFont.BOLD);
                } else {
                    font = new WritableFont(WritableFont.createFont(fontFamily),
                            fontSize,
                            WritableFont.NO_BOLD);
                }

                if (!"".equals(foreColor)) {
                    Color color = Color.decode(foreColor); // 自定义的颜色
                    wwb.setColourRGB(Colour.BLUE, color.getRed(), color.getGreen(), color.getBlue());
                    font.setColour(Colour.BLUE);
                }

                String columns = metd.getString("cols");
                // 第一列的序号
                boolean isSerialNo = metd.getString("is_serial_no").equals("1");
                if (isSerialNo) {
                    columns = columns.substring(1); // [{}, {},...]去掉[
                    columns = "[{\"field\":\"serialNoForExp\",\"title\":\"序号\",\"link\":\"#\",\"width\":80,\"name\":\"serialNoForExp\"}," + columns;
                }

                com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(columns);
                StringBuffer colsSb = new StringBuffer();
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject json = arr.getJSONObject(i);

                    ws.setColumnView(i, (int) (json.getIntValue("width") * 0.09 * 0.94)); // 设置列的宽度 ，单位是自己根据实际的像素值推算出来的

                    StrUtil.concat(colsSb, ",", json.getString("field"));
                    mapWidth.put(json.getString("field"), json.getIntValue("width"));
                }

                listField = colsSb.toString();
                fields = StrUtil.split(listField, ",");
                len = fields.length;

                if (isBar) {
                    WritableFont barFont;
                    String barBackColor = metd.getString("bar_back_color");
                    String barForeColor = metd.getString("bar_fore_color");
                    String barFontFamily = metd.getString("bar_font_family");
                    int barFontSize = metd.getInt("bar_font_size");
                    boolean isBarbBold = metd.getInt("bar_is_bold") == 1;
                    if (isBarbBold) {
                        barFont = new WritableFont(WritableFont.createFont(barFontFamily),
                                barFontSize,
                                WritableFont.BOLD);
                    } else {
                        barFont = new WritableFont(WritableFont.createFont(barFontFamily),
                                barFontSize,
                                WritableFont.NO_BOLD);
                    }

                    if (!"".equals(barForeColor)) {
                        Color color = Color.decode(barForeColor); // 自定义的颜色
                        wwb.setColourRGB(Colour.RED, color.getRed(), color.getGreen(), color.getBlue());
                        barFont.setColour(Colour.RED);
                    }

                    WritableCellFormat barFormat = new WritableCellFormat(barFont);
                    // 水平居中对齐
                    barFormat.setAlignment(Alignment.CENTRE);
                    // 竖直方向居中对齐
                    barFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    barFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

                    if (!"".equals(barBackColor)) {
                        Color bClr = Color.decode(barBackColor); // 自定义的颜色
                        wwb.setColourRGB(Colour.GREEN, bClr.getRed(), bClr.getGreen(), bClr.getBlue());
                        barFormat.setBackground(Colour.GREEN);
                    }

                    jxl.write.Label a = new jxl.write.Label(0, 0, barName, barFormat);
                    ws.addCell(a);

                    ws.mergeCells(0, 0, len - 1, 0);

                    ws.setRowView(0, metd.getInt("bar_line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素

                    rowHeader = 1;
                }
                ws.setRowView(rowHeader, metd.getInt("line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素
            } else {
                font = new WritableFont(WritableFont.createFont("宋体"),
                        12,
                        WritableFont.BOLD);
            }

            WritableCellFormat wcFormat = new WritableCellFormat(font);
            //水平居中对齐
            wcFormat.setAlignment(Alignment.CENTRE);
            //竖直方向居中对齐
            wcFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            wcFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            if (templateId != -1) {
                if (!"".equals(backColor)) {
                    Color color = Color.decode(backColor); // 自定义的颜色
                    wwb.setColourRGB(Colour.ORANGE, color.getRed(), color.getGreen(), color.getBlue());
                    wcFormat.setBackground(Colour.ORANGE);
                }
            }

            for (int i = 0; i < len; i++) {
                String fieldName = fields[i];

                String fieldTitle;
                if (templateId != -1) {
                    if (!"serialNoForExp".equals(fieldName)) {
                        fieldTitle = fd.getFieldTitle(fieldName);
                    }
                    else {
                        fieldTitle = "";
                    }
                }
                else {
                    fieldTitle = fieldsTitle[i];
                }
                DebugUtil.i(getClass(), "fieldName", fieldName + "," + fieldTitle);

                String title = "";
                if ("serialNoForExp".equals(fieldName)) {
                    title = "序号";
                } else if (fieldName.startsWith("main:")) {
                    String[] mainToSub = StrUtil.split(fieldName, ":");
                    if (mainToSub != null && mainToSub.length == 3) {
                        FormDb ntfd = new FormDb();
                        ntfd = ntfd.getFormDb(mainToSub[1]);
                        com.redmoon.oa.visual.FormDAO ntfdao = new com.redmoon.oa.visual.FormDAO(ntfd);
                        FormField ff = ntfdao.getFormField(mainToSub[2]);
                        title = ff.getTitle();
                    } else {
                        title = fieldName;
                    }
                } else if (fieldName.startsWith("other:")) {
                    String[] otherFields = StrUtil.split(fieldName, ":");
                    if (otherFields.length == 5) {
                        FormDb otherFormDb = new FormDb(otherFields[2]);
                        String showFieldName = otherFields[4];
                        if ("id".equalsIgnoreCase(showFieldName)) {
                            title = otherFormDb.getName() + "ID";
                        } else {
                            title = otherFormDb.getFieldTitle(showFieldName);
                        }
                    }
                } else if ("cws_creator".equals(fieldName)) {
                    title = "创建者";
                } else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
                    title = "ID";
                } else if ("cws_status".equals(fieldName)) {
                    title = "状态";
                } else if ("cws_flag".equals(fieldName)) {
                    title = "冲抵状态";
                } else if ("flowId".equalsIgnoreCase(fieldName)) {
                    title = "流程号";
                } else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
                    title = "流程开始时间";
                } else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
                    title = "流程结束时间";
                } else if ("cws_id".equals(fieldName)) {
                    title = "关联ID";
                }
                else if ("cws_visited".equals(fieldName)) {
                    title = "是否已读";
                }
                else if ("colPrompt".equals(fieldName)) {
                    title = "colPrompt"; //
                }
                else if ("cws_create_date".equals(fieldName)) {
                    title = "创建时间";
                }
                else if ("cws_modify_date".equals(fieldName)) {
                    title = "修改时间";
                }
                else if ("cws_cur_handler".equals(fieldName)) {
                    title = "当前处理";
                }
                else {
                    title = fd.getFieldTitle(fieldName);
                    if (!"#".equals(fieldTitle)) {
                        title = fieldTitle;
                    }
                }

                // 判断字段是不是嵌套表，如果是则需要在第0行显示嵌套表这个字段
                if (!nestMapping.containsKey(fieldName)) {
                    jxl.write.Label a = new jxl.write.Label(i + index, rowHeader, title, wcFormat);
                    ws.addCell(a);
                } else {
                    jxl.write.Label a = new jxl.write.Label(i + index, 0, title, wcFormat);
                    ws.addCell(a);
                }

                // 加粗+4
                int titleLen = 20;
                if (title!=null) {
                    titleLen = title.getBytes().length;
                }
                columnWidthMap.put(i + index, titleLen + 4);

                if ("ID".equals(fieldName)) {
                    ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
                } else if ("cws_creator".equals(fieldName)) {
                    ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
                } else if ("cws_flag".equals(fieldName)) {
                    ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
                } else if (fieldName.startsWith("main:")) {
                    ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
                } else if (fieldName.startsWith("other:")) {
                    ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
                } else if ("CWS_MID".equalsIgnoreCase(fieldName)) {
                    ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
                } else {
                    FormField myFf = fd.getFormField(fieldName);
                    if (myFf == null) {
                        LogUtil.getLog(getClass()).warn(fieldName + " 不存在");
                        fieldName = null;
                    } else {
                        fieldName = nestMapping.get(myFf.getName());
                    }
                    if (fieldName == null) { // && !nestFields.containsKey(fieldName)) {
                        if (templateId == -1) {
                            ws.mergeCells(i + index, 0, i + index, nestList.isEmpty() ? 0 : 1);
                        }
                    } else {
                        String[] ntFields = nestFields.get(fieldName);
                        if (templateId == -1) {
                            ws.mergeCells(i + index, 0, i + index + ntFields.length - 1, 0);
                        }

                        for (int j = 0; j < ntFields.length; j++) {
                            columnWidthMap.put(i + index, ntFields[j].getBytes().length + 4);
                            if (j < ntFields.length - 1) {
                                index++;
                            }
                            if (templateId == -1) {
                                jxl.write.Label b = new jxl.write.Label(i + j, 1, ntFields[j], wcFormat);
                                ws.addCell(b);
                            } else {
                                jxl.write.Label b = new jxl.write.Label(i + j, rowHeader, ntFields[j], wcFormat);
                                ws.addCell(b);
                            }
                        }
                    }
                }
            }

            // 如果存在嵌套表，则表头会变为合并的两行，因为上面调用了mergeCells
            if (!nestList.isEmpty()) {
                rowHeader += 1;
            }

            // int j = nestList.isEmpty() ? 0 : 1;
            int j = rowHeader + 1;
            int group = 0;
            int serialNo = 0;
            WorkflowDb wf = new WorkflowDb();
            request.setAttribute(ConstUtil.IS_FOR_EXPORT, "true");
            long tDebugAll = System.currentTimeMillis();

            int totalPages = 1;
            int pageSize = 200;
            int row = 0;
            com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
            // 为防止内存不足，每次处理记录数为pageSize
            for (int curPage = 1; curPage <= totalPages; curPage++) {
                try {
                    ListResult lr = fdao.listResult(fd.getCode(), sql, curPage, pageSize);
                    Vector<FormDAO> v = lr.getResult();
                    if (v.size() == 0) {
                        LogUtil.getLog(getClass()).error("导出结果为空: " + sql);
                        return;
                    }
                    if (totalPages == 1) {
                        totalPages = (int) Math.ceil((double) lr.getTotal() / pageSize);
                    }
                    // 当异步导出Excel时
                    if (sysProperties.isExportExcelAsync() && Global.getInstance().isUseCache()) {
                        if (curPage == 1) {
                            FormDb fdItem = new FormDb();
                            fdItem = fdItem.getFormDb(ConstUtil.FORM_EXPORT_EXCEL);
                            FormDAO daoItem = new FormDAO(fdItem);
                            daoItem.setFieldValue("sql_text", sql);
                            daoItem.setFieldValue("operator", authUtil.getUserName());
                            daoItem.setFieldValue("rows", lr.getTotal());
                            daoItem.setFieldValue("module_code", msd.getCode());
                            daoItem.setFieldValue("ext", "xls");
                            daoItem.setCreator(authUtil.getUserName()); // 参数为用户名（创建记录者）
                            daoItem.setUnitCode(authUtil.getUserUnitCode()); // 置单位编码
                            daoItem.create();

                            ExportExcelItem exportExcelItem = new ExportExcelItem();
                            exportExcelItem.setUid(uid);
                            exportExcelItem.setRows((int)lr.getTotal());
                            exportExcelItem.setCurRow(0);
                            exportExcelItem.setId(daoItem.getId());
                            exportExcelCache.put(uid, exportExcelItem);
                        }
                    }

                    for (FormDAO formDAO : v) {
                        fdao = formDAO;

                        index = 0;
                        int logType = StrUtil.toInt(fdao.getFieldValue("log_type"), FormDAOLog.LOG_TYPE_CREATE);

                        long tDebug = System.currentTimeMillis();

                        // 置SQL、表单域选择宏控件中需要用到的fdao
                        RequestUtil.setFormDAO(request, fdao);

                        long fid = fdao.getId();
                        // 嵌套表的最大行数，如果有多个嵌套表，取行数最大的值
                        int maxCount = 1;
                        if (templateId != -1) {
                            ws.setRowView(j, metd.getInt("line_height") * 10); // 设置行的高度 ，setRowView(row, 200) 在excel中的实际高度为10像素
                        }
                        for (String ntCode : nestList) {
                            //String ntsql = "select " + nestFieldName.get(ntCode) + " from ft_" + ntCode + " where cws_id=" + fid;
                            String ntsql = "select id from ft_" + ntCode + " where cws_id='" + fid + "'";
                            JdbcTemplate jt = new JdbcTemplate();
                            ResultIterator ri = jt.executeQuery(ntsql);
                            nestData.put(ntCode, ri.getResult());
                            if (ri.getRows() > maxCount) {
                                maxCount = ri.getRows();
                            }
                        }

                        // 取出嵌套表的记录
                        Map<String, List<com.redmoon.oa.visual.FormDAO>> mapNest = new HashMap<>();
                        if (isExporBySelCol) {
                            for (String nestFormCode : nestFormList) {
                                mapNest.put(nestFormCode, fdao.listNest(nestFormCode));
                            }
                        }

                        for (int i = 0; i < len; i++) {
                            boolean isSingle = true; // false表示带有嵌套表
                            String fieldName = fields[i];
                            String fieldValue = "";
                            if ("serialNoForExp".equals(fieldName)) {
                                fieldValue = String.valueOf(++serialNo);
                            } else if (fieldName.startsWith("main:")) {
                                String[] mainToSub = StrUtil.split(fieldName, ":");
                                if (mainToSub != null && mainToSub.length == 3) {
                                    // 此时关联的是主表单，应该只有一条记录
                                    FormDb subfd = new FormDb(mainToSub[1]);
                                    FormDAO subfdao = new FormDAO(subfd);
                                    FormField subff = subfd.getFormField(mainToSub[2]);
                                    String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
                                    try {
                                        JdbcTemplate jt = new JdbcTemplate();
                                        ResultIterator ri = jt.executeQuery(subsql);
                                        if (ri.hasNext()) {
                                            ResultRecord rr = (ResultRecord) ri.next();
                                            int subid = rr.getInt(1);
                                            subfdao = new FormDAO(subid, subfd);
                                            fieldValue = subfdao.getFieldValue(mainToSub[2]);
                                            if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
                                                MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
                                                if (mu != null) {
                                                    RequestUtil.setFormDAO(request, subfdao);
                                                    fieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, fieldValue);
                                                    // 恢复request中原来的fdao，以免ModuleController中setFormDAO的值被修改为本方法中的fdao
                                                    RequestUtil.setFormDAO(request, fdao);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        LogUtil.getLog(getClass()).error(e);
                                    }
                                }
                            } else if (fieldName.startsWith("other:")) {
                                // 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
                                String fName = fieldName;
                                if (logType == FormDAOLog.LOG_TYPE_DEL) {
                                    if ("module_log".equals(formCode)) {
                                        if (fName.contains("module_id:")) {
                                            int p = fName.indexOf(":");
                                            p = fName.indexOf(":", p + 1);
                                            String prefix = fName.substring(0, p);
                                            fName = fName.substring(p + 1);
                                            p = fName.indexOf(":");
                                            String endStr = fName.substring(p);
                                            if (endStr.startsWith(":id:")) {
                                                // 将id替换为***_log表中的cws_log_id
                                                endStr = ":cws_log_id" + endStr.substring(3);
                                            }
                                            fName = fName.substring(0, p);
                                            fName += "_log";
                                            fName = prefix + ":" + fName + endStr;
                                        }
                                    }
                                }
                                fieldValue = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
                            } else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
                                fieldValue = String.valueOf(fdao.getId());
                            } else if ("cws_flag".equals(fieldName)) {
                                fieldValue = String.valueOf(fdao.getCwsFlag());
                            } else if ("cws_creator".equals(fieldName)) {
                                fieldValue = StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
                            } else if ("cws_status".equals(fieldName)) {
                                fieldValue = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
                            } else if ("flowId".equalsIgnoreCase(fieldName)) {
                                fieldValue = String.valueOf(fdao.getFlowId());
                            } else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
                                int flowId = fdao.getFlowId();
                                if (flowId != -1) {
                                    wf = wf.getWorkflowDb(flowId);
                                    fieldValue = String.valueOf(DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss"));
                                }
                            } else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
                                int flowId = fdao.getFlowId();
                                if (flowId != -1) {
                                    wf = wf.getWorkflowDb(flowId);
                                    fieldValue = String.valueOf(DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss"));
                                }
                            } else if ("cws_id".equals(fieldName)) {
                                fieldValue = fdao.getCwsId();
                            }
                            else if ("cws_visited".equals(fieldName)) {
                                fieldValue = fdao.isCwsVisited()?"是":"否";
                            }
                            else if ("colPrompt".equals(fieldName)) {
                                if (isPrompt) {
                                    // 判断条件
                                    if (ModuleUtil.isPrompt(request, msd, fdao)) {
                                        fieldValue = "<img src=\"" + Global.getFullRootPath(request) + promptIcon + "\" style=\"width:16px;\" align=\"absmiddle\" />";
                                    }
                                }
                            }
                            else if ("cws_create_date".equals(fieldName)) {
                                fieldValue = DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd");
                            } else if ("cws_modify_date".equals(fieldName)) {
                                fieldValue = DateUtil.format(fdao.getCwsModifyDate(), "yyyy-MM-dd");
                            } else if ("cws_cur_handler".equals(fieldName)) {
                                int flowId = fdao.getFlowId();
                                if (flowId != -1) {
                                    wf = wf.getWorkflowDb(flowId);
                                    MyActionDb mad = new MyActionDb();
                                    for (MyActionDb madCur : mad.getMyActionDbDoingOfFlow(wf.getId())) {
                                        if (!"".equals(fieldValue)) {
                                            fieldValue += "、";
                                        }
                                        fieldValue += um.getUserDb(madCur.getUserName()).getRealName();
                                    }
                                }
                            }
                            else {
                                FormField ff = fd.getFormField(fieldName);
                                if (ff == null) {
                                    if (isExporBySelCol) {
                                        // 嵌套表
                                        if (fieldName.contains(":")) {
                                            String[] aryField = fieldName.split(":");
                                            List<com.redmoon.oa.visual.FormDAO> nestDaoList = mapNest.get(aryField[0]);
                                            StringBuilder stringBuilder = new StringBuilder();
                                            for (com.redmoon.oa.visual.FormDAO nestDao : nestDaoList) {
                                                FormField nestFf = nestDao.getFormField(aryField[1]);
                                                if (nestFf.getType().equals(FormField.TYPE_MACRO)) {
                                                    MacroCtlUnit mu = mm.getMacroCtlUnit(nestFf.getMacroType());
                                                    if (mu == null) {
                                                        DebugUtil.e(ModuleUtil.class, "exportExcel", nestFf.getTitle() + " 嵌套表 宏控件: " + nestFf.getMacroType() + " 不存在");
                                                    } else if (!"macro_raty".equals(mu.getCode())) {
                                                        StrUtil.concat(stringBuilder, ",", StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, nestFf, nestFf.getValue()), 1000, ""));
                                                    }
                                                } else {
                                                    StrUtil.concat(stringBuilder, ",", nestFf.convertToHtml());
                                                }
                                            }
                                            fieldValue = stringBuilder.toString();
                                        } else {
                                            fieldValue = "不存在！";
                                        }
                                    } else {
                                        fieldValue = "不存在！";
                                    }
                                } else {
                                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                        if (mu != null) {
                                            if ("nest_sheet".equals(mu.getCode()) || "nest_table".equals(mu.getCode())) {
                                                isSingle = false;
                                                String ntFormCode = nestMapping.get(ff.getName());
                                                if (ntFormCode != null) {
                                                    String ntFieldNames = nestFieldName.get(ntFormCode);
                                                    String[] ntFieldAry = StrUtil.split(ntFieldNames, ",");
                                                    Vector riData = nestData.get(ntFormCode);
                                                    if (riData != null) {
                                                        int rowInc = 0;
                                                        Iterator it = riData.iterator();
                                                        while (it.hasNext()) {
                                                            int columnInc = 0;
                                                            Vector rrv = (Vector) it.next();
                                                            long ntid = StrUtil.toLong(rrv.get(0).toString(), 0);
                                                            FormDb ntfd = new FormDb(ntFormCode);
                                                            FormDAO ntfdao = new FormDAO(ntid, ntfd);

                                                            if (ntfdao != null && ntfdao.isLoaded()) {
                                                                for (int k = 0; k < ntFieldAry.length; k++) {
                                                                    int width = columnWidthMap.get(i + index + columnInc);
                                                                    String content = ntfdao.getFieldValue(ntFieldAry[k]);

                                                                    FormField ntff = ntfdao.getFormField(ntFieldAry[k]);

                                                                    if (ntff.getType().equals(FormField.TYPE_MACRO)) {
                                                                        MacroCtlUnit ntmu = mm.getMacroCtlUnit(ntff.getMacroType());
                                                                        if (ntmu != null) {
                                                                            content = StrUtil.getAbstract(request, ntmu.getIFormMacroCtl().converToHtml(request, ntff, ntfdao.getFieldValue(ntFieldAry[k])), 1000, "");
                                                                        }
                                                                    }

                                                                    if (content != null && content.getBytes().length > width) {
                                                                        columnWidthMap.put(i + index + columnInc, content.getBytes().length);
                                                                    }

                                                                    int fieldType = FormField.FIELD_TYPE_TEXT;
                                                                    if (ntff != null) {
                                                                        fieldType = ntff.getFieldType();
                                                                    }
                                                                    WritableCellFormat wcf = setCellFormat(fieldType, group, map);

                                                                    // 设置列格式
                                                                    // 如果是嵌套表，则数据从第三行开始，所以要在j+rowInc 基础上+1
                                                                    if (templateId == -1) {
                                                                        WritableCell wc = createWritableCell(fieldType, i + index + columnInc++, j + rowInc + 1, content, wcf);
                                                                        ws.addCell(wc);
                                                                    } else {
                                                                        WritableCell wc = createWritableCell(fieldType, i + index + columnInc++, j + rowInc, content, wcf);
                                                                        ws.addCell(wc);
                                                                    }
                                                                }
                                                                rowInc++;
                                                            }
                                                        }
                                                        // 将没有值的单元格补色
                                                        for (int m = rowInc; m < maxCount; m++) {
                                                            for (int k = 0; k < ntFieldAry.length; k++) {
                                                                WritableCellFormat wcf = setCellFormat(FormField.FIELD_TYPE_TEXT, group, map);
                                                                if (templateId == -1) {
                                                                    jxl.write.Label a = new jxl.write.Label(i + index + k, j + m + 1, "", wcf);
                                                                    ws.addCell(a);
                                                                } else {
                                                                    jxl.write.Label a = new jxl.write.Label(i + index + k, j + m, "", wcf);
                                                                    ws.addCell(a);
                                                                }
                                                            }
                                                        }
                                                        index += ntFieldAry.length - 1;
                                                    }
                                                }
                                            } else if (!"macro_raty".equals(mu.getCode())) {
                                                // fieldValue = StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)), 1000, "");
                                                fieldValue = mu.getIFormMacroCtl().getValueForExport(request, ff, fdao.getFieldValue(fieldName));
                                            } else {
                                                fieldValue = FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName));
                                            }
                                        }
                                    } else {
                                        // 如果是日期型的，则需注意，如果日期型字段做了格式化设置，则FuncUtil.renderFieldValue会生成格式化过的值，当createWritableCell时再解析就会报错
                                        if (ff.getFieldType()==FormField.FIELD_TYPE_DATETIME || ff.getFieldType()==FormField.FIELD_TYPE_DATE) {
                                            fieldValue = fdao.getFieldValue(fieldName);
                                        } else {
                                            fieldValue = FuncUtil.renderFieldValue(fdao, fdao.getFormField(fieldName));
                                        }
                                    }
                                }
                            }

                            if (isSingle) {
                                int width = columnWidthMap.get(i + index);

                                if (fieldValue != null && fieldValue.getBytes().length > width) {
                                    columnWidthMap.put(i + index, fieldValue.getBytes().length);
                                }

                                FormField ff = fdao.getFormField(fieldName);
                                int fieldType = FormField.FIELD_TYPE_VARCHAR;
                                if (ff != null) {
                                    if (!ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                                        fieldType = ff.getFieldType();
                                    }

                                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                        // 导出时如果不按varchar类型，则Excel会自动将其转为-1
                                        if (mu.getIFormMacroCtl() instanceof IBasicSelectCtl || mu.getIFormMacroCtl() instanceof IModuleFieldSelectCtl) {
                                            fieldType = FormField.FIELD_TYPE_VARCHAR;
                                        }
                                    }

                                    // 日期字段中如果配置格式化了，则在createWritableCell无法解析，会导致null错误
                                    if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                                        if (!"".equals(ff.getDescription())) {
                                            fieldType = FormField.FIELD_TYPE_VARCHAR;
                                        }
                                    }
                                }

                                wcFormat = setCellFormat(fieldType, group, map);
                                WritableCell wc = createWritableCell(fieldType, i + index, j, fieldValue, wcFormat);
                                ws.addCell(wc);

                                // 设置每个单元格的值
                                if (templateId != -1) {
                                    for (int a = j + 1; a <= j + maxCount - 1; a++) {
                                        WritableCell wc1 = createWritableCell(fieldType, i + index, a, fieldValue, wcFormat);
                                        ws.addCell(wc1);
                                    }
                                }
                                if (i == 0) {
                                    DebugUtil.i(getClass(), "j", j + " fieldName=" + fieldName + ", fieldValue=" + fieldValue);
                                }
                                if (templateId == -1 && maxCount>1) {
                                    // 扩展至多行,合并单元格
                                    ws.mergeCells(i + index, j, i + index, j + maxCount - 1);
                                }
                            }
                        }
                        group++;
                        j += maxCount;

                        row++;

                        ExportExcelItem exportExcelItem = exportExcelCache.get(uid);
                        if (exportExcelItem == null) {
                            LogUtil.getLog(getClass()).error("uid: " + uid + " is not found，it is may be expired.");
                        } else {
                            exportExcelItem.setUid(uid);
                            exportExcelItem.setCurRow(row);
                            exportExcelCache.put(uid, exportExcelItem);
                        }
                    }
                } catch (ErrMsgException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }

            if (row > 0) {
                String propStat = StrUtil.getNullStr(msd.getString("prop_stat"));
                if (propStat.equals("")) {
                    propStat = "{}";
                }
                JSONObject json = JSONObject.parseObject(propStat);
                Iterator ir3 = json.keySet().iterator();
                while (ir3.hasNext()) {
                    String key = (String) ir3.next();
                    String mode = json.getString(key);

                    FormField ff = fd.getFormField(key);
                    int fieldType = ff.getFieldType();

                    String cellVal = "";
                    double sumVal = FormSQLBuilder.getSUMOfSQL(sql, key);
                    if (mode.equals("0")) {
                        if (fieldType == FormField.FIELD_TYPE_INT
                                || fieldType == FormField.FIELD_TYPE_LONG) {
                            cellVal = "合计：" + (long) sumVal;
                        } else {
                            cellVal = "合计：" + NumberUtil.round(sumVal, 2);
                        }
                    } else if (mode.equals("1")) {
                        cellVal = "平均：" + NumberUtil.round(sumVal / row, 2);
                    }

                    for (int i = 0; i < len; i++) {
                        String fieldName = fields[i];
                        if (fieldName.equals(key)) {
                            jxl.write.Label label = new Label(i, row + 1, cellVal);
                            ws.addCell(label);
                            break;
                        }
                    }
                }
            }

            // 如果未选择导出模板
            if (templateId == -1) {
                // 设置列宽
                for (int i = 0; i < ws.getColumns(); i++) {
                    ws.setColumnView(i, columnWidthMap.get(i) > 30 ? 30 : columnWidthMap.get(i));
                }
            }

            wwb.write();

            // 当异步导出Excel时
            if (sysProperties.isExportExcelAsync() && Global.getInstance().isUseCache()) {
                ExportExcelItem exportExcelItem = exportExcelCache.get(uid);
                if (exportExcelItem == null) {
                    LogUtil.getLog(getClass()).error("uid: " + uid + " is not found，it is may be expired.");
                } else {
                    exportExcelItem.setUid(uid);
                    exportExcelItem.setCurRow(exportExcelItem.getRows());
                    exportExcelCache.put(uid, exportExcelItem);

                    FormDb fdItem = new FormDb();
                    fdItem = fdItem.getFormDb(ConstUtil.FORM_EXPORT_EXCEL);
                    FormDAO daoItem = new FormDAO();
                    daoItem = daoItem.getFormDAOByCache(exportExcelItem.getId(), fdItem);
                    daoItem.setFieldValue("finish_time", new Date());
                    daoItem.save();
                }
            }

            DebugUtil.i(getClass(), "exportExcel", "all record: " + (System.currentTimeMillis() - tDebugAll) + " ms");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            try {
                if (wwb != null) {
                    try {
                        wwb.close();
                    } catch (WriteException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
                if (wb != null) {
                    wb.close();
                }
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            os.close();
        }
    }


    /**
     * 设置单元格格式
     * 一个WritableCellFormat不能被重复引用多次，否则会报错
     * 优化了setCellFormat，使其从map中复用取值，但是map不能置于中作为本页的全局变量，而必须作为一个参数来传
     *
     * @param fieldType
     * @param row
     * @param map
     * @return
     */
    private WritableCellFormat setCellFormat(int fieldType, int row, Map map) {
        WritableCellFormat wcf = null;
        boolean isFirst = false;
        try {
            // 单元格格式
            switch (fieldType) {
                case FormField.FIELD_TYPE_DOUBLE:
                case FormField.FIELD_TYPE_FLOAT:
                case FormField.FIELD_TYPE_PRICE:
                    if (map.get("double") != null) {
                        wcf = (WritableCellFormat) map.get("double");
                    } else {
                        NumberFormat nf1 = new NumberFormat("0.00");
                        wcf = new WritableCellFormat(nf1);
                        map.put("double", wcf);
                        isFirst = true;
                    }
                    break;
                case FormField.FIELD_TYPE_INT:
                case FormField.FIELD_TYPE_LONG:
                    if (map.get("long") != null) {
                        wcf = (WritableCellFormat) map.get("long");
                    } else {
                        NumberFormat nf2 = new NumberFormat("0");
                        wcf = new WritableCellFormat(nf2);
                        map.put("long", wcf);
                        isFirst = true;
                    }
                    break;
                case FormField.FIELD_TYPE_DATE:
                    if (map.get("date") != null) {
                        wcf = (WritableCellFormat) map.get("date");
                    } else {
                        jxl.write.DateFormat df1 = new jxl.write.DateFormat("yyyy-MM-dd");
                        wcf = new jxl.write.WritableCellFormat(df1);
                        map.put("date", wcf);
                        isFirst = true;
                    }
                    break;
                case FormField.FIELD_TYPE_DATETIME:
                    if (map.get("datetime") != null) {
                        wcf = (WritableCellFormat) map.get("datetime");
                    } else {
                        jxl.write.DateFormat df2 = new jxl.write.DateFormat("yyyy-MM-dd HH:mm:ss");
                        wcf = new jxl.write.WritableCellFormat(df2);
                        map.put("datetime", wcf);
                        isFirst = true;
                    }
                    break;
                default:
                    if (map.get("str") != null) {
                        wcf = (WritableCellFormat) map.get("str");
                    } else {
                        wcf = new WritableCellFormat();
                        map.put("str", wcf);
                        isFirst = true;
                    }

                    break;
            }

            if (isFirst) {
                // 不能修改已指向的format， jxl.write.biff.JxlWriteException: Attempt to modify a referenced format
                // 对齐方式
                wcf.setAlignment(Alignment.CENTRE);
                wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
                // 边框
                wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
                //自动换行
                wcf.setWrap(true);

                // 背景色
	        /*
	        if (row % 2 == 0) {
	        	wcf.setBackground(jxl.format.Colour.ICE_BLUE);
			} else {
				wcf.setBackground(jxl.format.Colour.WHITE);
			}
			*/
            }

        } catch (WriteException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return wcf;
    }

    // 创建单元格
    private WritableCell createWritableCell(int fieldType, int column, int row, String data, WritableCellFormat wcf) {
        WritableCell wc = null;
        if (data == null || "".equals(data)) {
            wc = new Label(column, row, "", wcf);
        } else {
            switch (fieldType) {
                case FormField.FIELD_TYPE_TEXT:
                case FormField.FIELD_TYPE_VARCHAR:
                    wc = new Label(column, row, data, wcf);
                    break;
                case FormField.FIELD_TYPE_DOUBLE:
                case FormField.FIELD_TYPE_FLOAT:
                case FormField.FIELD_TYPE_PRICE:
                    wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
                    break;
                case FormField.FIELD_TYPE_INT:
                case FormField.FIELD_TYPE_LONG:
                    wc = new jxl.write.Number(column, row, StrUtil.toLong(data), wcf);
                    break;
                case FormField.FIELD_TYPE_DATE:
                    wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd"), wcf);
                    break;
                case FormField.FIELD_TYPE_DATETIME:
                    wc = new jxl.write.DateTime(column, row, DateUtil.parse(data, "yyyy-MM-dd HH:mm:ss"), wcf);
                    break;
                default:
                    wc = new jxl.write.Number(column, row, StrUtil.toDouble(data), wcf);
                    break;
            }
        }
        return wc;
    }
}
