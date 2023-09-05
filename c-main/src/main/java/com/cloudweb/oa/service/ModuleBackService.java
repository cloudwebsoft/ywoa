package com.cloudweb.oa.service;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IModuleFieldSelectCtl;
import com.cloudweb.oa.api.ISQLCtl;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.shell.BSHShell;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.BeanShellUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.*;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormDAOMgr;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

@Component
public class ModuleBackService {

    @Autowired
    private UserCache userCache;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private I18nUtil i18nUtil;

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String create(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        String code = ParamUtil.get(request, "code");
        if ("".equals(code)) {
            code = ParamUtil.get(request, "formCode");
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            json.put("ret", "0");
            json.put("msg", "模块不存在！");
            return json.toString();
        }

        String formCode = msd.getString("form_code");
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserAppend(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        boolean re;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        re = fdm.create(request.getServletContext(), request, msd);
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
            }
            else if (pageAddRedirect == ConstUtil.PAGE_ADD_REDIRECT_TO_SHOW) {
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
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("addToUrl", redirectUrl);
            json.put("id", fdm.getVisualObjId());
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }

        return json.toString();
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String update(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        String code = ParamUtil.get(request, "code");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd == null) {
            json.put("ret", "0");
            json.put("msg", "模块不存在！");
            return json.toString();
        }

        ModulePrivDb mpd = new ModulePrivDb(code);
        if (!mpd.canUserModify(privilege.getUser(request)) && !mpd.canUserManage(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        String formCode = msd.getString("form_code");
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        boolean re = fdm.update(request.getServletContext(), request, msd);
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String createRelate(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();

        String formCode = ParamUtil.get(request, "formCode");
        if (formCode.equals("")) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "err_id"));
            return json.toString();
        }

        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        ModuleSetupDb msdRelated = new ModuleSetupDb();
        msdRelated = msdRelated.getModuleSetupDb(moduleCodeRelated);
        String formCodeRelated = msdRelated.getString("form_code");

        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCodeRelated);

        int parentId = ParamUtil.getInt(request, "parentId"); // 父模块的ID
        if (parentId == -1) {
            json.put("ret", "0");
            json.put("msg","缺少父模块记录的ID");
            return json.toString();
        }

        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        if (!mpd.canUserAppend(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        if (fd == null || !fd.isLoaded()) {
            json.put("ret", "0");
            json.put("msg", "表单不存在");
            return json.toString();
        }

        boolean re = false;
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        try {
            if (formCode.equals("project") && formCodeRelated.equals("project_members")) {
                re = fdm.createPrjMember(request.getServletContext(), request);
            } else {
                re = fdm.create(request.getServletContext(), request);
            }

        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("id", fdm.getVisualObjId());
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }

        return json.toString();
    }

    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String updateRelate(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();

        // 取从模块编码
        String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        if ("".equals(moduleCodeRelated)) {
            json.put("ret", "0");
            json.put("msg", "缺少关联模块编码");
            return json.toString();
        }

        // 取主模块编码
        String moduleCode = ParamUtil.get(request, "code");

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        if (msd == null) {
            json.put("ret", "0");
            json.put("msg", "模块不存在");
            return json.toString();
        }
        String formCodeRelated = msd.getString("form_code");

        ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
        if (!mpd.canUserModify(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        int id = ParamUtil.getInt(request, "id", -1);
        if (id == -1) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "err_id"));
            return json.toString();
        }

        // 检查数据权限，判断用户是否可以存取此条数据
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDb(moduleCode);
        if (parentMsd == null) {
            json.put("ret", "0");
            json.put("msg", "父模块不存在");
            return json.toString();
        }
        String parentFormCode = parentMsd.getString("form_code");
        String mode = ParamUtil.get(request, "mode");
        // 是否通过选项卡标签关联
        boolean isSubTagRelated = "subTagRelated".equals(mode);
        String relateFieldValue = "";
        int parentId = ParamUtil.getInt(request, "parentId", -1); // 父模块的ID
        if (parentId == -1) {
            json.put("ret", "0");
            json.put("msg", "缺少父模块记录的ID");
            return json.toString();
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
        if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
            json.put("ret", "0");
            json.put("msg", i18nUtil.get("info_access_data_fail"));
            return json.toString();
        }

        boolean re = false;
        try {
            FormMgr fm = new FormMgr();
            FormDb fd = fm.getFormDb(formCodeRelated);
            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
            re = fdm.update(request.getServletContext(), request);
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }

        return json.toString();
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
        String strProps = msd.getString("props");
        boolean isColOperateTextShow = false;
        if (!StrUtil.isEmpty(strProps)) {
            com.alibaba.fastjson.JSONObject props = com.alibaba.fastjson.JSONObject.parseObject(strProps);
            if (props != null) {
                if (props.containsKey("isColOperateTextShow")) {
                    isColOperateTextShow = props.getBoolean("isColOperateTextShow");
                }
            }
        }
        if (msd.getInt("btn_display_show") == 1 && canView) {
            String showName = "<i class=\"fa fa-file-text-o link-icon link-icon-show\" />";
            if (isColOperateTextShow) {
                showName += "查看";
            }

            if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
                String urlShow = FormUtil.parseAndSetFieldValue(msd.getString("url_show"), fdao);
                if (urlShow.contains("?")) {
                    sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"查看\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "&parentId=" + id + "&id=" + id + "&code=" + code + "')\">" + showName + "</a>");
                } else {
                    sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"查看\" onClick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/" + urlShow + "?parentId=" + id + "&id=" + id + "&code=" + code + "')\">" + showName + "</a>");
                }
            } else {
                sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"查看\" onclick=\"addTab('" + msd.getString("name") + "', '" + request.getContextPath() + "/visual/moduleShowPage.do?parentId=" + id + "&id=" + id + "&code=" + code + "')\">" + showName + "</a>");
            }
        }

        boolean isBtnEditShow = msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(userName);
        if (isBtnEditShow) {
            String editName = "<i class='fa fa-edit link-icon link-icon-edit'></i>";
            if (isColOperateTextShow) {
                editName += "修改";
            }
            sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"修改\" onclick=\"edit(" + id + ")\">" + editName + "</a>");
        }
        boolean isBtnDelShow = msd.getInt("btn_del_show") == 1 && (mpd.canUserDel(userName) || canManage);
        if (isBtnDelShow) {
            // 注意不能写成<i class="fa fa-trash-o link-icon link-icon-del" />，在module_list_log.jsp中会产生两个图标，可能跟flexigrid有关
            String delName = "<i class=\"fa fa-trash-o link-icon link-icon-del\"></i>";
            if (isColOperateTextShow) {
                delName += "删除";
            }
            sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"删除\" onclick=\"del('" + id + "')\">" + delName + "</a>");
        }

        if (msd.getInt("btn_flow_show") == 1) {
            if (fd.isFlow() && fdao.getFlowId() != -1) {
                String btnFlowName = "<i class=\"fa fa-check-circle-o link-icon link-icon-flow\" />";
                if (isColOperateTextShow) {
                    btnFlowName += i18nUtil.get("btn_flow");
                }
                String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));
                sb.append("<a href=\"javascript:;\" class=\"link-btn\" onclick=\"addTab('查看流程', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + fdao.getFlowId() + "&visitKey=" + visitKey + "')\">" + btnFlowName + "</a>");
            }
        }

        if (canLog || canManage) {
            if (msd.getInt("btn_log_show") == 1 && fd.isLog()) {
                String btnHisName = "<i class=\"fa fa-history link-icon link-icon-history\" />";
                if (isColOperateTextShow) {
                    btnHisName += "历史";
                }
                if (isModuleHistory) {
                    sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"历史记录\" onclick=\"addTab('历史记录', '" + request.getContextPath() + "/visual/module_his_list.jsp?op=search&code=" + code + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnHisName + "</a>");
                }

                String btnLogModifyName = "<i class=\"fa fa-pencil link-icon link-icon-log-edit\" />";
                if (isColOperateTextShow) {
                    btnLogModifyName += "修改日志";
                }
                if (isModuleLogModify) {
                    sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"修改日志\" onclick=\"addTab('修改日志', '" + request.getContextPath() + "/visual/module_log_list.jsp?op=search&code=" + code + "&fdaoId=" + id + "&formCode=" + formCode + "')\">" + btnLogModifyName + "</a>");
                }

                String btnLogReadName = "<i class=\"fa fa-tv link-icon link-icon-log-read\" />";
                if (isColOperateTextShow) {
                    btnLogReadName += "浏览日志";
                }
                if (isModuleLogRead) {
                    sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"浏览日志\" onclick=\"addTab('浏览日志', '" + request.getContextPath() + "/visual/moduleListPage.do?op=search&code=module_log_read&read_type=" + FormDAOLog.READ_TYPE_MODULE + "&module_code=" + code + "&module_id=" + id + "&form_code=" + formCode + "')\">" + btnLogReadName + "</a>");
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
                String linkBtnName = "";
                if (!"".equals(linkIcons[i])) {
                    linkBtnName = "<i class=\"fa " + linkIcons[i] + " link-icon\" style=\"color:" + linkColors[i] + "\" />";
                }
                if (isColOperateTextShow) {
                    linkBtnName += linkNames[i];
                }

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
                        sb.append("<a href=\"javascript:;\" title=\"" + linkName + "\" class=\"link-btn\" onclick=\"" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code) + "\">" + linkBtnName + "</a>");
                    } else {
                        sb.append("<a href=\"javascript:;\" title=\"" + linkName + "\" class=\"link-btn\" onclick=\"addTab('" + linkName + "', '" + request.getContextPath() + "/" + ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code) + "')\">" + linkBtnName + "</a>");
                    }
                }
            }
        }

        if (is_workLog == 1) {
            sb.append("&nbsp;&nbsp;<a href=\"javascript:;\" class=\"link-btn\" onclick=\"addTab('" + msd.getString("name") + "汇报', '" + request.getContextPath() + "/queryMyWork.action?code=" + mainFormCode + "&id=" + id + "')\">汇报</a>");
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
                        String btnAltName = "<i class=\"fa fa-leaf link-icon link-icon-alt\" />";
                        if (isColOperateTextShow) {
                            btnAltName += "变更";
                        }
                        sb.append("<a href=\"javascript:;\" class=\"link-btn\" title=\"变更\" onclick=\"addTab('" + msd.getString("name") + "变更', '" + request.getContextPath() + "/flowDispose.do?myActionId=" + mad.getId() + "')\">" + btnAltName + "</a>");
                    }
                }
            }
        }

        return sb.toString();
    }

    public String getColOperateForListSel(HttpServletRequest request, JSONObject moduleFieldSelectCtlDesc, int k, String openerFormCode, String openerFieldName, String showValue, boolean isShowFieldFound, FormDAO fdao) throws ErrMsgException {
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
            if (ff==null) {
                showValue = "字段：" + showFieldName + " 不存在";
            }
            else {
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

                // LogUtil.getLog(getClass()).info(getClass() + " " + destF + "-" + setValue);
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
                sbScript.append("if (window.opener) {\n");
                sbScript.append("    window.opener.clearDetailList(\"" + nestFieldName + "\");\n");
                sbScript.append("}\n");
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
                    sbScript.append("if (window.opener) {\n");
                    sbScript.append("    try {\n");
                    sbScript.append("        window.opener.insertRow(\"" + nestFormCode + "\", '" + jsonAry.toString() + "', \"" + nestFieldName + "\");\n");
                    sbScript.append("    } catch (e) {}\n");
                    sbScript.append("}\n");
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
                        sbScript.append("if (window.opener) {\n");
                        sbScript.append("    try {\n");
                        if (parentId == -1) {
                            sbScript.append("window.opener.addTempCwsId(\"" + nestFormCode + "\", " + fdaoId + ");\n");
                        }
                        sbScript.append("window.opener.insertRow_" + nestFormCode + "(\"" + nestFormCode + "\", " + fdaoId + ", \"" + tds.replaceAll("\"", "\\\\\"") + "\", \"" + token + "\", true);\n");
                        sbScript.append("    } catch (e) {}\n");
                        sbScript.append("    }\n");
                    }
                }
            }

            if (nestType.equals(ConstUtil.NEST_SHEET) || nestType.equals(ConstUtil.NEST_DETAIL_LIST)) {
                sbScript.append("window.opener.fireEventSelect_" + nestFormCode + "();\n");
            } else if (ConstUtil.NEST_TABLE.equals(nestType)) {
                sbScript.append("window.opener.refreshNestTableCtl" + nestFieldName + "();\n");
            } else {
                sbScript.append("window.opener.location.reload();\n");
            }
        }
        return sbScript.toString();
    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public void doImport(HttpServletRequest request, JSONArray ary, long parentId, int templateId, String code, String moduleCodeRelated, String formCode, String menuItem, String importRecords) throws ErrMsgException {
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        Privilege privilege = new Privilege();
        String unitCode = privilege.getUserUnitCode(request);
        MacroCtlMgr mm = new MacroCtlMgr();
        ModuleSetupDb msd = new ModuleSetupDb();
        if (parentId==-1) {
            msd = msd.getModuleSetupDbOrInit(code);
        }
        else {
            msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        }

        // String listField = StrUtil.getNullStr(msd.getString("list_field"));
        String[] fields = msd.getColAry(false, "list_field");

        JSONArray arr = null;
        JSONArray aryCleans = null;
        if (templateId != -1) {
            ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
            mid = mid.getModuleImportTemplateDb(templateId);
            String rules = mid.getString("rules");
            // DebugUtil.i(getClass(), "doImport", rules);
            try {
                arr = new JSONArray(rules);
                if (arr.length() > 0) {
                    fields = new String[arr.length()];
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject json = (org.json.JSONObject) arr.get(i);
                        fields[i] = json.getString("name");
                    }
                }

                String strJson = StrUtil.getNullStr(mid.getString("cleans"));
                if (!"".equals(strJson)) {
                    aryCleans = new JSONArray(strJson);
                }
            } catch (org.json.JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        String userName = privilege.getUser(request);


        FormDb fdRelate = new FormDb();
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);

        // 记录不允许重复的字段组合
        Vector vFieldCanNotRepeat = new Vector();
        if (templateId != -1) {
            ModuleImportTemplateDb mid = new ModuleImportTemplateDb();
            mid = mid.getModuleImportTemplateDb(templateId);
            String rules = mid.getString("rules");
            try {
                arr = new JSONArray(rules);
                if (arr.length() > 0) {
                    fields = new String[arr.length()];
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject json = (org.json.JSONObject) arr.get(i);
                        fields[i] = json.getString("name");
                        int canNotRepeat = json.getInt("canNotRepeat");
                        if (canNotRepeat == 1) {
                            vFieldCanNotRepeat.addElement(fields[i]);
                        }
                    }
                }
            } catch (org.json.JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        // 判断配置中是否设置了同步帐户
        // com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        // boolean isArchiveUserSynAccount = cfg.getBooleanProperty("isArchiveUserSynAccount");
        Vector records = new Vector();

        JdbcTemplate jt = new JdbcTemplate();
        jt.setAutoClose(false);
        // 先创建主模块记录
        try {
            int rowCount = ary.length();
            for (int r = 0; r < rowCount; r++) {
                org.json.JSONObject jo = (org.json.JSONObject) ary.get(r);

                long mainId = -1;
                // 检查主表中是否已存在重复记录，如果已存在，则提取出记录的ID
                if (templateId!=-1) {
                    StringBuffer conds = new StringBuffer();
                    Iterator ir = vFieldCanNotRepeat.iterator();
                    while (ir.hasNext()) {
                        String fieldName = (String) ir.next();
                        if (!fieldName.startsWith("nest.")) {
                            StrUtil.concat(conds, " and ", FormDb.getTableName(formCode)
                                    + "."
                                    + fieldName
                                    + "="
                                    + StrUtil.sqlstr(jo.getString(fieldName)));
                        }
                    }
                    if (conds.length()>0) {
                        String sql = "select id from ft_" + formCode + " where " + conds.toString();
                        ResultIterator ri = jt.executeQuery(sql);
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord)ri.next();
                            mainId = rr.getLong(1);
                        }
                    }
                }
                // 如果未找到重复的主模块记录，则插入本行记录
                if (mainId==-1) {
                    FormDAO fdao = new FormDAO(fd);
                    for (int m = 0; m < fields.length; m++) {
                        if ("cws_creator".equals(fields[m])) {
                            fdao.setCreator(userName);
                        } else {
                            if (templateId != -1) {
                                if (fields[m].startsWith("nest.")) {
                                    continue;
                                }
                            }
                            String val = "";
                            if (jo.has(fields[m])) {
                                val = jo.getString(fields[m]);
                            } else {
                                LogUtil.getLog(getClass()).error("字段：" + fields[m] + " 在导入的文件中不存在");
                                continue;
                            }
                            FormField ff = fd.getFormField(fields[m]);
                            if (ff == null) {
                                LogUtil.getLog(getClass()).error("字段：" + fields[m] + " 已不存在");
                                continue;
                            }
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null && !"macro_raty".equals(mu.getCode())) {
                                    // 如果是基础数据宏控件
                                    boolean isClean = false;
                                    if ("macro_flow_select".equals(mu.getCode())) {
                                        org.json.JSONObject json = null;
                                        if (aryCleans != null) {
                                            for (int i = 0; i < aryCleans.length(); i++) {
                                                json = aryCleans.getJSONObject(i);
                                                if (ff.getName().equals(json.get("fieldName"))) {
                                                    isClean = true;
                                                    break;
                                                }
                                            }
                                        }
                                        // 如果需清洗数据
                                        if (isClean) {
                                            val = json.getString(val);
                                        }
                                    }
                                    if (!isClean) {
                                        val = mu.getIFormMacroCtl().getValueByName(ff, val);
                                    }
                                }
                            }
                            fdao.setFieldValue(fields[m], val);
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

                    records.addElement(fdao);

                    // 改写在导入后事件里
                    /*if ("personbasic".equals(formCode) && isArchiveUserSynAccount) {
                        UserDb ud = new UserDb();
                        // 为新增用户自动创建帐户
                        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
                        // 默认密码
                        String defaultPwd = scfg.getInitPassword();
                        String un = fdao.getFieldValue("user_name");
                        if (un != null && !"".equals(un)) {
                            ud.create(un, fdao.getFieldValue("realname"), defaultPwd, "", unitCode);
                            userCache.refreshCreate();

                            String deptCode = fdao.getFieldValue("dept");
                            if (deptCode != null && !"".equals(deptCode)) {
                                DeptUserDb dud = new DeptUserDb();
                                try {
                                    dud.create(deptCode, fdao.getFieldValue("user_name"), "");
                                } catch (ErrMsgException e) {
                                    LogUtil.getLog(getClass()).error(e);
                                }
                            }
                        }
                    }*/

                    mainId = fdao.getId();
                }

                // 创建从模块记录
                ModuleRelateDb mrd = new ModuleRelateDb();
                Vector v = mrd.getModulesRelated(formCode);
                // 遍历所有从模块，并创建从模块的记录
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    mrd = (ModuleRelateDb) ir.next();

                    String relateCode = mrd.getString("relate_code");
                    fdRelate = fdRelate.getFormDb(relateCode);

                    FormDAO fdao = new FormDAO(fdRelate);

                    // 在配置中是否有从模块中的字段
                    boolean isFind = false;

                    for (int m = 0; m < fields.length; m++) {
                        if ("cws_creator".equals(fields[m])) {
                            fdao.setCreator(userName);
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
                            fdao.setFieldValue(fieldName, val);
                        }
                    }

                    if (isFind) {
                        String relateFieldValue = fdm.getRelateFieldValue(mainId, relateCode);
                        fdao.setCwsId(relateFieldValue);
                        fdao.setUnitCode(unitCode);
                        fdao.create();
                        // 如果需要记录历史
                        if (fdRelate.isLog()) {
                            FormDAO.log(userName, FormDAOLog.LOG_TYPE_CREATE, fdao);
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

                bs.eval(script);
            }
        }
        catch (org.json.JSONException | SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            jt.close();
        }
    }

    /**
     * 取得在位编辑的字段相关属性
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
        if (fieldWrite!=null && !"".equals(fieldWrite)) {
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
        }
        else {
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
                                StringBuffer sb = new StringBuffer();
                                String opts = ifmc.getControlOptions(userName, ff);
                                try {
                                    JSONArray arr = new JSONArray(opts);
                                    for (int i = 0; i < arr.length(); i++) {
                                        org.json.JSONObject json = arr.getJSONObject(i);
                                        // 不能用getString，因为有些可能为int型
                                        StrUtil.concat(sb, ",", json.get("name") + ":" + json.get("value"));
                                    }
                                } catch (org.json.JSONException e) {
                                    DebugUtil.e("moduleListPage.do", "选项json解析错误，字段：", ff.getTitle() + " " + ff.getName() + " 中选项为：" + opts);
                                    // LogUtil.getLog(getClass()).error(e);
                                }
                                com.alibaba.fastjson.JSONObject jsonEditable = new com.alibaba.fastjson.JSONObject();
                                jsonEditable.put("fieldName", ff.getName());
                                jsonEditable.put("type", FormField.TYPE_SELECT);
                                aryEditable.add(jsonEditable);

                                com.alibaba.fastjson.JSONObject jsonOpt = new com.alibaba.fastjson.JSONObject();
                                jsonOpt.put("fieldName", ff.getName());
                                jsonOpt.put("opt", sb.toString());
                                aryEditableOpt.add(jsonOpt);
                            }
                        }
                        break;
                    case FormField.TYPE_SELECT: {
                        String[][] aryOpt = FormParser.getOptionsArrayOfSelect(fd, ff);
                        StringBuffer sb = new StringBuffer();
                        for (String[] strings : aryOpt) {
                            StrUtil.concat(sb, ",", strings[0] + ":" + strings[1]);
                        }
                        com.alibaba.fastjson.JSONObject jsonEditable = new com.alibaba.fastjson.JSONObject();
                        jsonEditable.put("fieldName", ff.getName());
                        jsonEditable.put("type", FormField.TYPE_SELECT);
                        aryEditable.add(jsonEditable);

                        com.alibaba.fastjson.JSONObject jsonOpt = new com.alibaba.fastjson.JSONObject();
                        jsonOpt.put("fieldName", ff.getName());
                        jsonOpt.put("opt", sb.toString());
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
}
