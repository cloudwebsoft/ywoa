package com.cloudweb.oa.controller;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.entity.Privilege;
import com.cloudweb.oa.entity.RolePriv;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IMenuService;
import com.cloudweb.oa.service.IPrivilegeService;
import com.cloudweb.oa.service.IRolePrivService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.redmoon.oa.basic.SelectKindDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.CSSUtil;
import com.redmoon.oa.visual.ModuleSetupDb;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.RolePrivDb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @Description:
 * @author:
 * @Date: 2017-12-3下午05:20:45
 */
@Controller
@Slf4j
@RequestMapping("/admin")
public class MenuController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    IRolePrivService rolePrivService;

    @Autowired
    IMenuService menuService;

    @Autowired
    IPrivilegeService privilegeService;

    @Autowired
    ResponseUtil responseUtil;

    @ResponseBody
    @RequestMapping(value = "/setMenuPriv", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String setMenuPriv(String roleCode, boolean isPriv, String priv) {
        boolean re = false;

        if (isPriv) {
            String[] ary = StrUtil.split(priv, ",");
            if (ary != null) {
                for (int k = 0; k < ary.length; k++) {
                    RolePriv rolePriv = new RolePriv();
                    rolePriv.setPriv(ary[k]);
                    rolePriv.setRoleCode(roleCode);
                    re = rolePriv.insert();
                }
            }
        } else {
            String[] ary = StrUtil.split(priv, ",");
            if (ary != null) {
                for (int k = 0; k < ary.length; k++) {
                    re = rolePrivService.del(roleCode, priv);
                }
            }
        }

        JSONObject json = new JSONObject();
        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/menuFrame")
    public String menuFrame(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        return "th/admin/menu_frame";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/menuTree")
    public String menuTree(@RequestParam(defaultValue = "") String nodeSelected, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        // 原节点下的孩子节点通过修复repairTree处理
        Menu menu = menuService.getMenu(ConstUtil.MENU_ROOT);
        // menuService.repairTree(menu);

        long t = System.currentTimeMillis();
        String jsonData = menuService.getJsonString();
        model.addAttribute("jsonData", jsonData);

        if (Global.getInstance().isDebug()) {
            double s = (double)(System.currentTimeMillis() - t) / 1000;
            log.info("获取菜单 time:" + s + " s");
        }

        List list = menuService.getAllChild(new ArrayList<>(), menu);
        model.addAttribute("all", list);

        if (Global.getInstance().isDebug()) {
            double s = (double)(System.currentTimeMillis() - t) / 1000;
            log.info("获取全部菜单 time:" + s + " s");
        }

        model.addAttribute("nodeSelected", nodeSelected);

        return "th/admin/menu_tree";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addMenu")
    public String addMenu(String parentCode, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("parentCode", parentCode);
        Menu parentMenu = menuService.getMenu(parentCode);

        if (parentMenu.getLayer() >= 4) {
            model.addAttribute("msg", "菜单不能超过3级！");
            return "th/error/info";
        }

        model.addAttribute("parentName", parentMenu.getName());

        String code = RandomSecquenceCreator.getId(10);
        model.addAttribute("code", code);

        com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
        boolean isStyleSpecified = oaCfg.get("styleMode").equals("specified") || oaCfg.get("styleMode").equals("2");
        // 如果指定了界面风格
        boolean isShowIcon = true, isShowBigIcon = true, isShowFontIcon = true;
        if (isStyleSpecified) {
            if (oaCfg.getInt("styleSpecified") != 1 && oaCfg.getInt("styleSpecified") != 4) {
                isShowIcon = false;
            }
            // 如果指定的风格不是时尚型或炫丽型，则不显示
            if (oaCfg.getInt("styleSpecified") != 2 && oaCfg.getInt("styleSpecified") != 3) {
                isShowBigIcon = false;
            }
            if (oaCfg.getInt("styleSpecified") != 5) {
                isShowFontIcon = false;
            }
        }
        boolean isShowWidget = true;
        // 如果指定了界面风格
        if (isStyleSpecified) {
            // 如果指定的风格不是炫丽型，则不显示
            if (oaCfg.getInt("styleSpecified") != 3) {
                isShowWidget = false;
            }
        }
        model.addAttribute("isStyleSpecified", isStyleSpecified);
        model.addAttribute("isShowIcon", isShowIcon);
        model.addAttribute("isShowBigIcon", isShowBigIcon);
        model.addAttribute("isShowFontIcon", isShowFontIcon);
        model.addAttribute("isShowWidget", isShowWidget);

        boolean isPlatform = com.redmoon.oa.kernel.License.getInstance().isPlatform();
        model.addAttribute("isPlatform", isPlatform);

        // 取出字体图标
        ArrayList<String> fontAry = CSSUtil.getFontBefores();
        model.addAttribute("fontAry", fontAry);

        // 取出所有的权限
        List<Privilege> allPrivList = privilegeService.getAll();
        model.addAttribute("allPrivList", allPrivList);

        // 取出预定义的菜单项
        String opts = "";
        com.redmoon.oa.ui.menu.Config cfg = com.redmoon.oa.ui.menu.Config.getInstance();
        List list = cfg.root.getChild("items").getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element e = (Element) ir.next();
                opts += "<option value='" + e.getChildText("code") + "'>" + e.getChildText("desc") + "</option>";
            }
        }
        model.addAttribute("itemOpts", opts);

        // 取出模块
        ModuleSetupDb msd = new ModuleSetupDb();
        Vector<ModuleSetupDb> v = msd.listUsed();
        model.addAttribute("allModule", v);

        StringBuffer flowOpts = new StringBuffer();
        com.redmoon.oa.flow.Leaf flowrootlf = new com.redmoon.oa.flow.Leaf();
        flowrootlf = flowrootlf.getLeaf(Leaf.CODE_ROOT);
        if (flowrootlf != null) {
            com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(flowrootlf);
            try {
                flowdv.getDirectoryAsOptions(request, flowrootlf, flowrootlf.getLayer(), flowOpts);
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("flowOpts", flowOpts);

        // 取出基础数据大类
        SelectKindDb selectKindDb = new SelectKindDb();
        Vector kindV = selectKindDb.list();
        model.addAttribute("kindV", kindV);

/*        StringBuffer sb = new StringBuffer();
        Menu menuRoot = menuService.getMenu(ConstUtil.MENU_ROOT);
        menuService.ShowDirectoryAsOptionsToString(sb, menuRoot, menuRoot.getLayer());
        model.addAttribute("menuOpts", sb);*/

        return "th/admin/menu_add";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/createMenu", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String createMenu(@RequestParam(required = true) String code, String name, @RequestParam(required = true) String parentCode, String link, String preCode, Integer hasPath, Integer isResource,
                             @RequestParam(defaultValue = "60") Integer width,
                             String target, String pvg, String icon, @RequestParam(defaultValue = "0") Integer isUse,
                             @RequestParam(defaultValue = "0") Integer nav,
                             @RequestParam(defaultValue = "0") Integer canRepeat, String bigIcon, @RequestParam(defaultValue = "0") Integer isWidget,
                             @RequestParam(defaultValue = "200") Integer widgetWidth, @RequestParam(defaultValue = "100") Integer widgetHeight,
                             String fontIcon, String description
    ) throws ValidateException {
        String formCode = ParamUtil.get(request, "formCode");
        if (preCode.equals("flow")) {
            formCode = ParamUtil.get(request, "flowTypeCode");
        } else if (preCode.equals("basicdata")) {
            formCode = ParamUtil.get(request, "basicdata");
        }

        Menu menu = new Menu();
        menu.setCode(code);
        if (!"".equals(preCode)) {
            if ("module".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_MODULE);
            } else if ("flow".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_FLOW);
            } else if ("basicdata".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_BASICDATA);
            } else {
                menu.setType(ConstUtil.MENU_TYPE_PRESET);
            }
        } else {
            menu.setType(ConstUtil.MENU_TYPE_LINK);
        }
        menu.setName(name);
        menu.setParentCode(parentCode);
        menu.setFormCode(formCode);
        menu.setLink(link);
        menu.setPreCode(preCode);
        menu.setIsHasPath(hasPath);
        menu.setIsResource(isResource);
        menu.setWidth(width);
        menu.setTarget(target);
        menu.setPvg(pvg);
        menu.setIcon(icon);
        menu.setIsUse(isUse);
        menu.setIsNav(nav);
        menu.setCanRepeat(canRepeat);
        menu.setBigIcon(bigIcon);
        menu.setIsWidget(isWidget);
        menu.setWidgetWidth(widgetWidth);
        menu.setWidgetHeight(widgetHeight);
        menu.setFontIcon(fontIcon);
        menu.setDescription(description);
        menu.setChildCount(0);
        boolean re = menuService.create(menu);
        return responseUtil.getResultJson(re).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/editMenu")
    public String editMenu(@RequestParam(required = true) String code, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        Menu menu = menuService.getMenu(code);

        if (menu==null) {
            model.addAttribute("msg", "节点已被删除");
            return "th/error/info";
        }

        model.addAttribute("menu", menu);

        String parentName = "无";
        if (!ConstUtil.MENU_ROOT.equals(code)) {
            Menu menuParent = menuService.getMenu(menu.getParentCode());
            parentName = menuParent.getName();
        }
        model.addAttribute("parentName", parentName);

        com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
        boolean isStyleSpecified = oaCfg.get("styleMode").equals("specified") || oaCfg.get("styleMode").equals("2");
        // 如果指定了界面风格
        boolean isShowIcon = true, isShowBigIcon = true, isShowFontIcon = true;
        if (isStyleSpecified) {
            if (oaCfg.getInt("styleSpecified") != 1 && oaCfg.getInt("styleSpecified") != 4) {
                isShowIcon = false;
            }
            // 如果指定的风格不是时尚型或炫丽型，则不显示
            if (oaCfg.getInt("styleSpecified") != 2 && oaCfg.getInt("styleSpecified") != 3) {
                isShowBigIcon = false;
            }
            if (oaCfg.getInt("styleSpecified") != 5) {
                isShowFontIcon = false;
            }
        }
        boolean isShowWidget = true;
        // 如果指定了界面风格
        if (isStyleSpecified) {
            // 如果指定的风格不是炫丽型，则不显示
            if (oaCfg.getInt("styleSpecified") != 3) {
                isShowWidget = false;
            }
        }
        boolean isPlatform = com.redmoon.oa.kernel.License.getInstance().isPlatform();

        model.addAttribute("isStyleSpecified", isStyleSpecified);
        model.addAttribute("isShowIcon", isShowIcon);
        model.addAttribute("isShowBigIcon", isShowBigIcon);
        model.addAttribute("isShowFontIcon", isShowFontIcon);
        model.addAttribute("isShowWidget", isShowWidget);
        model.addAttribute("isPlatform", isPlatform);

        ArrayList<String> fontAry = CSSUtil.getFontBefores();
        model.addAttribute("fontAry", fontAry);

        List<Privilege> allPrivList = privilegeService.getAll();
        model.addAttribute("allPrivList", allPrivList);

        // 取出预定义的菜单项
        String opts = "";
        com.redmoon.oa.ui.menu.Config cfg = com.redmoon.oa.ui.menu.Config.getInstance();
        List list = cfg.root.getChild("items").getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element e = (Element) ir.next();
                opts += "<option value='" + e.getChildText("code") + "'>" + e.getChildText("desc") + "</option>";
            }
        }
        model.addAttribute("itemOpts", opts);

        // 取出模块
        ModuleSetupDb msd = new ModuleSetupDb();
        Vector<ModuleSetupDb> v = msd.listUsed();
        model.addAttribute("allModule", v);

        StringBuffer flowOpts = new StringBuffer();
        com.redmoon.oa.flow.Leaf flowrootlf = new com.redmoon.oa.flow.Leaf();
        flowrootlf = flowrootlf.getLeaf(Leaf.CODE_ROOT);
        if (flowrootlf != null) {
            com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(flowrootlf);
            try {
                flowdv.getDirectoryAsOptions(request, flowrootlf, flowrootlf.getLayer(), flowOpts);
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("flowOpts", flowOpts);

        // 取出基础数据大类
        SelectKindDb selectKindDb = new SelectKindDb();
        Vector kindV = selectKindDb.list();
        model.addAttribute("kindV", kindV);

        // 取出拥有的权限
        JSONArray aryPvg = new JSONArray();
        String[] ary = StrUtil.split(menu.getPvg(), ",");
        if (ary != null) {
            for (int j = 0; j < ary.length; j++) {
                JSONObject json = new JSONObject();
                String desc = "";
                if (ary[j].equals("!admin")) {
                    desc = "非管理员";
                    json.put("priv", ary[j]);
                } else {
                    Privilege privilege = privilegeService.getByPriv(ary[j]);
                    if (privilege == null) {
                        desc = ary[j] + " 不存在";
                    }
                    else {
                        desc = privilege.getDescription();
                    }
                }
                json.put("priv", ary[j]);
                json.put("desc", desc);
                aryPvg.add(json);
            }
        }
        model.addAttribute("aryPvg", aryPvg);

        String menuName = menu.getName();
        if(menuName.startsWith("#")) {
            menuName =  LocalUtil.LoadString(request, "res.ui.menu",code);
        }
        model.addAttribute("menuName", menuName);

        model.addAttribute("realLink", menuService.getRealLink(menu));

/*        StringBuffer sb = new StringBuffer();
        Menu menuRoot = menuService.getMenu(ConstUtil.MENU_ROOT);
        menuService.ShowDirectoryAsOptionsToString(sb, menuRoot, menuRoot.getLayer());
        model.addAttribute("menuOpts", sb);*/

        return "th/admin/menu_edit";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/updateMenu", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String updateMenu(@RequestParam(required = true) String code, String name, String parentCode, String link, String preCode, Integer hasPath, Integer isResource,
                             @RequestParam(defaultValue = "60") Integer width,
                             String target, String pvg, String icon, @RequestParam(defaultValue = "0") Integer isUse,
                             @RequestParam(defaultValue = "0") Integer nav,
                             @RequestParam(defaultValue = "0") Integer canRepeat, String bigIcon, @RequestParam(defaultValue = "0") Integer isWidget,
                             @RequestParam(defaultValue = "200") Integer widgetWidth, @RequestParam(defaultValue = "100") Integer widgetHeight,
                             String fontIcon, String description
    ) throws ValidateException {
        String formCode = ParamUtil.get(request, "formCode");
        if (preCode.equals("flow")) {
            formCode = ParamUtil.get(request, "flowTypeCode");
        } else if (preCode.equals("basicdata")) {
            formCode = ParamUtil.get(request, "basicdata");
        }

        Menu menu = menuService.getMenu(code);
        if (!"".equals(preCode)) {
            if ("module".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_MODULE);
            } else if ("flow".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_FLOW);
            } else if ("basicdata".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_BASICDATA);
            } else {
                menu.setType(ConstUtil.MENU_TYPE_PRESET);
            }
        } else {
            menu.setType(ConstUtil.MENU_TYPE_LINK);
        }
        menu.setName(name);
        menu.setParentCode(parentCode);
        menu.setFormCode(formCode);
        menu.setLink(link);
        menu.setPreCode(preCode);
        menu.setIsHasPath(hasPath);
        menu.setIsResource(isResource);
        menu.setWidth(width);
        menu.setTarget(target);
        menu.setPvg(pvg);
        menu.setIcon(icon);
        menu.setIsUse(isUse);
        menu.setIsNav(nav);
        menu.setCanRepeat(canRepeat);
        menu.setBigIcon(bigIcon);
        menu.setIsWidget(isWidget);
        menu.setWidgetWidth(widgetWidth);
        menu.setWidgetHeight(widgetHeight);
        menu.setFontIcon(fontIcon);
        menu.setDescription(description);
        boolean re = menuService.updateByCode(menu);
        return responseUtil.getResultJson(re).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delMenu", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public JSONObject delMenu(@RequestParam(required = true) String code) {
        Menu menu = menuService.getMenu(code);
        return responseUtil.getResultJson(menuService.del(menu));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/moveMenu", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public JSONObject moveMenu(@RequestParam(required = true) String code, String parentCode, Integer position) {
        String msg = "";
        try {
            menuService.move(code, parentCode, position);
        } catch (ErrMsgException e) {
            msg = e.getMessage();
            return responseUtil.getResultJson(false, msg);
        }
        return responseUtil.getResultJson(true);
    }
}
