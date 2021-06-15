package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import com.alibaba.fastjson.JSONObject;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.pvg.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Vector;

@Controller
@RequestMapping("/admin")
public class WorkflowPredefineController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/setFlowDebug", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setFlowDebug() throws ErrMsgException {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        String code = ParamUtil.get(request, "code");
        boolean isDebug = ParamUtil.getBoolean(request, "isDebug", true);
        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        String myUnitCode = privilege.getUserUnitCode(request);
        LeafPriv lp = new LeafPriv(code);
        if (privilege.isUserPrivValid(request, "admin.unit") && lf.getUnitCode().equals(myUnitCode)) {
            ;
        } else if (!lp.canUserExamine(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", "权限非法！");
            return json.toString();
        }

        boolean hasChild = false;
        if (lf.getChildCount() > 0) {
            hasChild = true;
            Vector v = new Vector();
            lf.getAllChild(v, lf);
            v.addElement(lf);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                lf = (Leaf) ir.next();
                if (lf.getType() != Leaf.TYPE_NONE) {
                    lf.setDebug(isDebug);
                    lf.update();
                }
            }
        } else {
            lf.setDebug(isDebug);
            lf.update();
        }
        json.put("ret", "1");
        if (hasChild) {
            if (isDebug) {
                json.put("msg", lf.getName() + "下的流程已置为调试模式！");
            } else {
                json.put("msg", lf.getName() + "下的流程已置为正常模式！");
            }
        } else {
            if (isDebug) {
                json.put("msg", lf.getName() + " 已置为调试模式！");
            } else {
                json.put("msg", lf.getName() + " 已置为正常模式！");
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moveFlowNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moveFlowNode() throws ErrMsgException {
        JSONObject json = new JSONObject();
        String code = ParamUtil.get(request, "code");
        String parent_code = ParamUtil.get(request, "parent_code");
        int position = Integer.parseInt(ParamUtil.get(request, "position"));
        if ("root".equals(code)) {
            json.put("ret", "0");
            json.put("msg", "根节点不能移动！");
            return json.toString();
        }
        if ("#".equals(parent_code)) {
            json.put("ret", "0");
            json.put("msg", "不能与根节点平级！");
            return json.toString();
        }

        Directory dir = new Directory();
        Leaf moveleaf = dir.getLeaf(code);
        String oldParentCode = moveleaf.getParentCode();
        int old_position = moveleaf.getOrders();//得到被移动节点原来的位置，从1开始

        Leaf oldParentLeaf = dir.getLeaf(oldParentCode);
        Leaf newParentLeaf;
        if (parent_code.equals(oldParentCode)) {
            newParentLeaf = oldParentLeaf;
        } else {
            newParentLeaf = dir.getLeaf(parent_code);
        }
        // 移动后的层级需一致
        if (oldParentLeaf.getLayer() != newParentLeaf.getLayer()) {
            json.put("ret", "0");
            json.put("msg", "层级不一致，不能移动！");
            return json.toString();
        }
        int p = position + 1;  // jstree的position是从0开始的，而orders是从1开始的
        moveleaf.setParentCode(parent_code);
        moveleaf.setOrders(p);
        moveleaf.update();

        boolean isSameParent = oldParentCode.equals(parent_code);

        // 重新梳理orders
        Iterator ir = newParentLeaf.getChildren().iterator();
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            // 跳过自己
            if (lf.getCode().equals(code)) {
                continue;
            }

            // 如果移动后父节点变了
            if (!isSameParent) {
                if (lf.getOrders() >= p) {
                    lf.setOrders(lf.getOrders() + 1);
                    lf.update();
                }
            } else {
                if (p < old_position) {//上移
                    if (lf.getOrders() >= p) {
                        lf.setOrders(lf.getOrders() + 1);
                        lf.update();
                    }
                } else {//下移
                    if (lf.getOrders() <= p && lf.getOrders() > old_position) {
                        lf.setOrders(lf.getOrders() - 1);
                        lf.update();
                    }
                }
            }
        }

        // 原节点下的孩子节点通过修复repairTree处理
        Leaf rootLeaf = dir.getLeaf(Leaf.CODE_ROOT);
        Directory dm = new Directory();
        try {
            dm.repairTree(rootLeaf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 父节点有变化
        if (!isSameParent) {
            // 只有二级节点的父节点才会有变化，此时需变动其表单所属的类别
            FormDb fd = new FormDb();
            fd = fd.getFormDb(moveleaf.getFormCode());
            fd.setFlowTypeCode(parent_code);
            fd.saveContent();
        }

        json.put("ret", "1");
        json.put("msg", "操作成功！");
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delFlowNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delFlowNode(String code) throws ErrMsgException {
        JSONObject json = new JSONObject();
        Directory dir = new Directory();
        try {
            dir.del(request, code);
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        json.put("ret", "1");
        json.put("msg", "操作成功！");
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/modifyFlowNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String modifyFlowNode() throws ErrMsgException {
        JSONObject json = new JSONObject();
        Directory dir = new Directory();
        boolean re;
        try {
            re = dir.update(request);
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

    @ResponseBody
    @RequestMapping(value = "/addFlowNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addFlowNode() throws ErrMsgException {
        JSONObject json = new JSONObject();
        Directory dir = new Directory();
        boolean re = false;
        try {
            re = dir.AddChild(request);
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

    @ResponseBody
    @RequestMapping(value = "/getFormColumn", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getFormColumn() throws ErrMsgException {
        JSONObject json = new JSONObject();
        String formCode = ParamUtil.get(request, "formCode");
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        Iterator field_v = fd.getFields().iterator();
        String str = "";
        while (field_v.hasNext()) {
            FormField ff = (FormField) field_v.next();
            str += "<span id='{" + ff.getName() + "}' name='list_field' onMouseOut='outtable(this)' onMouseOver='overtable(this)' style='width:200px;'>" + ff.getTitle() + "</span><br/>";
        }
        json.put("ret", "1");
        json.put("msg", str);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/createFlowPredefined", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String createFlowPredefined() throws ErrMsgException {
        JSONObject json = new JSONObject();
        WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
        boolean re = false;
        try {
            re = wpm.create(request);
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("newId", wpm.getNewId());
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/modifyFlowPredefined", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String modifyFlowPredefined() throws ErrMsgException {
        JSONObject json = new JSONObject();
        boolean re = false;
        WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
        try {
            re = wpm.modify(request);
        } catch (Exception e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/applyFlow", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String applyFlow(String flowTypeCode) throws ErrMsgException {
        JSONObject json = new JSONObject();
        boolean re;
        try {
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            wpd = wpd.getDefaultPredefineFlow(flowTypeCode);
            String templateCode = ParamUtil.get(request, "templateCode");

            String title = "";
            Leaf lf = new Leaf();
            lf = lf.getLeaf(flowTypeCode);
            if (lf != null) {
                title = lf.getName();
            }

            // 如果还没有预定义流程
            if (wpd == null) {
                wpd = new WorkflowPredefineDb();
                WorkflowPredefineDb twpd = wpd.getDefaultPredefineFlow(templateCode);
                if (twpd == null || !twpd.isLoaded()) {
                    json.put("ret", "0");
                    json.put("msg", "流程图不存在！");
                    return json.toString();
                }
                wpd.setTypeCode(flowTypeCode);
                wpd.setFlowString(twpd.getFlowString());
                wpd.setTitle(title);
                wpd.setReturnBack(twpd.isReturnBack());
                wpd.setReactive(twpd.isReactive());
                wpd.setRecall(twpd.isRecall());
                wpd.setReturnMode(twpd.getReturnMode());
                wpd.setReturnStyle(twpd.getReturnStyle());
                wpd.setRoleRankMode(twpd.getRoleRankMode());
                wpd.setProps(twpd.getProps());
                wpd.setViews(twpd.getViews());
                wpd.setScripts(twpd.getScripts());
                wpd.setLinkProp(twpd.getLinkProp());
                wpd.setFlowJson(twpd.getFlowJson());
                re = wpd.create();
            } else {
                WorkflowPredefineDb twpd = wpd.getDefaultPredefineFlow(templateCode);
                if (twpd == null || !twpd.isLoaded()) {
                    json.put("ret", "0");
                    json.put("msg", "流程图不存在！");
                    return json.toString();
                }
                wpd.setFlowString(twpd.getFlowString());
                wpd.setReturnBack(twpd.isReturnBack());
                wpd.setReactive(twpd.isReactive());
                wpd.setRecall(twpd.isRecall());
                wpd.setReturnMode(twpd.getReturnMode());
                wpd.setReturnStyle(twpd.getReturnStyle());
                wpd.setRoleRankMode(twpd.getRoleRankMode());
                wpd.setProps(twpd.getProps());
                wpd.setViews(twpd.getViews());
                wpd.setScripts(twpd.getScripts());
                wpd.setLinkProp(twpd.getLinkProp());
                wpd.setFlowJson(twpd.getFlowJson());
                re = wpd.save();
            }
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
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/getFieldsAsOptions", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getFieldsAsOptions(@RequestParam(required = true)String flowTypeCode) throws ErrMsgException {
        JSONObject json = new JSONObject();
        StringBuffer sb = new StringBuffer();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(flowTypeCode);
        if (lf == null) {
            json.put("ret", "0");
            json.put("msg", "流程类型不存在");
            return json.toString();
        }

        String formCode = lf.getFormCode();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        Iterator ir = fd.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            sb.append("<option value=" + ff.getName() + ">" + ff.getTitle() + "</option>");
        }

        json.put("ret", "1");
        json.put("msg", "操作成功！");
        json.put("options", sb.toString());

        return json.toString();
    }
}
