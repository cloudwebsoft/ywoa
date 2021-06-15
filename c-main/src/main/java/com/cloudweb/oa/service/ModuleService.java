package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Component
public class ModuleService {

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
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
        String addToUrl = "";
        com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
        re = fdm.create(request.getServletContext(), request, msd);
        // 如果指定了添加跳转URL
        addToUrl = StrUtil.getNullStr(msd.getString("add_to_url"));
        if (!"".equals(addToUrl)) {
            addToUrl = ModuleUtil.parseUrl(request, addToUrl, fdm.getFormDAO());
            if (!addToUrl.startsWith("http")) {
                addToUrl = request.getContextPath() + "/" + addToUrl;
            }
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("addToUrl", addToUrl);
            json.put("id", fdm.getVisualObjId());
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }

        return json.toString();
    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public String update(HttpServletRequest request) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        Privilege privilege = new Privilege();
        String code = ParamUtil.get(request, "code");
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        if (msd==null) {
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
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
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
            json.put("msg", SkinUtil.LoadString(request, "缺少父模块记录的ID"));
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

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
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
        if (id==-1) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "err_id"));
            return json.toString();
        }

        // 检查数据权限，判断用户是否可以存取此条数据
        ModuleSetupDb parentMsd = new ModuleSetupDb();
        parentMsd = parentMsd.getModuleSetupDb(moduleCode);
        if (parentMsd==null) {
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
        if (parentId==-1) {
            json.put("ret", "0");
            json.put("msg", "缺少父模块记录的ID");
            return json.toString();
        }
        else {
            if (!isSubTagRelated) {
                com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
                relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
                if (relateFieldValue==null) {
                    // 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
                    relateFieldValue = SQLBuilder.IS_NOT_RELATED;
                }
            }
        }
        if (!ModulePrivMgr.canAccessDataRelated(request, msd, relateFieldValue, id)) {
            I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
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
}
