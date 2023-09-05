package com.cloudweb.oa.controller;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.MenuCache;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.entity.Privilege;
import com.cloudweb.oa.entity.RolePriv;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IMenuService;
import com.cloudweb.oa.service.IPrivilegeService;
import com.cloudweb.oa.service.IRolePrivService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectKindDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.CSSUtil;
import com.redmoon.oa.visual.ModuleSetupDb;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    MenuCache menuCache;

    @ApiOperation(value = "保存菜单", notes = "保存菜单", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "权限编码", dataType = "String"),
            @ApiImplicitParam(name = "priv", value = "菜单", dataType = "String"),
            @ApiImplicitParam(name = "isPriv", value = "是否菜单", dataType = "Boolean"),
    })
    @ResponseBody
    @RequestMapping(value = "/setMenuPriv", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setMenuPriv(String roleCode, boolean isPriv, String priv) {
        if (StrUtil.isEmpty(priv)) {
            return new Result<>(false, "该菜单项上未设置权限");
        }
        boolean re = false;
        if (isPriv) {
            String[] ary = StrUtil.split(priv, ",");
            if (ary != null) {
                for (String s : ary) {
                    RolePriv rolePriv = rolePrivService.getRolePriv(roleCode, s);
                    if (rolePriv == null) {
                        re = rolePrivService.create(roleCode, s);
                    } else {
                        re = true;
                    }
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
        return new Result<>(re);
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

    @ApiOperation(value = "树形菜单", notes = "树形菜单", httpMethod = "GET")
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/getMenuTree", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<JSONObject> getMenuTree(@RequestParam(defaultValue = "") String nodeSelected) {
        Result<JSONObject> result = new Result<>();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        // 原节点下的孩子节点通过修复repairTree处理
        // Menu menu = menuService.getMenu(ConstUtil.MENU_ROOT);
        Menu menu = menuCache.getMenu(ConstUtil.MENU_ROOT);

        /*QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_code", menu.getCode());
        queryWrapper.orderByAsc("orders");
        List<Menu> childrenList = menuService.list(queryWrapper);*/
        List<Menu> childrenList = menuCache.getChildren(menu.getCode());
        if (childrenList.size() > 0) {
            for (Menu children : childrenList) {
                // List<Menu> children1List = menuService.list(new QueryWrapper<Menu>().eq("parent_code", children.getCode()).orderByAsc("orders"));
                List<Menu> children1List = menuCache.getChildren(children.getCode());
                children.setChildren(findChildren(children, children1List));
            }
            menu.setIsLeaf(true);
        } else {
            menu.setIsLeaf(false);
        }
        menu.setChildren(childrenList);
        jsonArray.add(menu);

        jsonObject.put("list", jsonArray);
        jsonObject.put("nodeSelected", nodeSelected);
        result.setData(jsonObject);

        return result;
    }

    @ApiOperation(value = "添加菜单明细", notes = "添加菜单明细", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "parentCode", value = "父编码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/addMenuItem")
    public Result<Object> addMenuItem(String parentCode, Model model) {
        Result<Object> result = new Result<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("parentCode", parentCode);
        Menu parentMenu = menuService.getMenu(parentCode);

        if (parentMenu.getLayer() >= 4) {
            result.error500("菜单不能超过3级！");
        }

        jsonObject.put("parentName", parentMenu.getName());

        String code = RandomSecquenceCreator.getId(10);
        jsonObject.put("code", code);

        com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
        boolean isStyleSpecified = "specified".equals(oaCfg.get("styleMode")) || "2".equals(oaCfg.get("styleMode"));
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
        boolean isShowWidget = false;
        // 如果指定了界面风格
        if (isStyleSpecified) {
            // 如果指定的风格不是炫丽型，则不显示
            if (oaCfg.getInt("styleSpecified") == 3) {
                isShowWidget = true;
            }
        }
        jsonObject.put("isStyleSpecified", isStyleSpecified);
        jsonObject.put("isShowIcon", isShowIcon);
        jsonObject.put("isShowBigIcon", isShowBigIcon);
        jsonObject.put("isShowFontIcon", isShowFontIcon);
        jsonObject.put("isShowWidget", isShowWidget);

        // 取出字体图标
        ArrayList<String> fontAry = CSSUtil.getFontBefores();
        jsonObject.put("fontAry", fontAry);

        // 取出所有的权限
        List<Privilege> allPrivList = privilegeService.getAll();
        jsonObject.put("allPrivList", allPrivList);

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
        jsonObject.put("itemOpts", opts);

        // 取出模块
        /*ModuleSetupDb msd = new ModuleSetupDb();
        Vector<ModuleSetupDb> v = msd.listUsed();
        jsonObject.put("allModule", v);*/

        StringBuffer flowOpts = new StringBuffer();
        com.redmoon.oa.flow.Leaf flowrootlf = new com.redmoon.oa.flow.Leaf();
        flowrootlf = flowrootlf.getLeaf(Leaf.CODE_ROOT);
        if (flowrootlf != null) {
            com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(flowrootlf);
            try {
                flowdv.getDirectoryAsOptions(request, flowrootlf, flowrootlf.getLayer(), flowOpts);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        jsonObject.put("flowOpts", flowOpts);

        // 取出基础数据大类
        SelectKindDb selectKindDb = new SelectKindDb();
        Vector kindV = selectKindDb.list();
        jsonObject.put("kindV", kindV);

/*        StringBuffer sb = new StringBuffer();
        Menu menuRoot = menuService.getMenu(ConstUtil.MENU_ROOT);
        menuService.ShowDirectoryAsOptionsToString(sb, menuRoot, menuRoot.getLayer());
        model.addAttribute("menuOpts", sb);*/
        result.setData(jsonObject);

        return result;
    }

    @ApiOperation(value = "创建菜单", notes = "创建菜单", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String", required = true),
            @ApiImplicitParam(name = "parentCode", value = "父编码", dataType = "String"),
            @ApiImplicitParam(name = "link", value = "链接", dataType = "String"),
            @ApiImplicitParam(name = "preCode", value = "preCode", dataType = "String"),
            @ApiImplicitParam(name = "hasPath", value = "hasPath", dataType = "Integer"),
            @ApiImplicitParam(name = "isResource", value = "isResource", dataType = "Integer"),
            @ApiImplicitParam(name = "width", value = "width", dataType = "Integer"),
            @ApiImplicitParam(name = "nav", value = "nav", dataType = "Integer"),
            @ApiImplicitParam(name = "canRepeat", value = "canRepeat", dataType = "Integer"),
            @ApiImplicitParam(name = "bigIcon", value = "bigIcon", dataType = "Integer"),
            @ApiImplicitParam(name = "isWidget", value = "isWidget", dataType = "Integer"),
            @ApiImplicitParam(name = "widgetWidth", value = "widgetWidth", dataType = "Integer"),
            @ApiImplicitParam(name = "widgetHeight", value = "widgetHeight", dataType = "Integer"),
            @ApiImplicitParam(name = "fontIcon", value = "fontIcon", dataType = "String"),
            @ApiImplicitParam(name = "description", value = "description", dataType = "String"),
            @ApiImplicitParam(name = "front", value = "front", dataType = "Integer"),
            @ApiImplicitParam(name = "component", value = "component", dataType = "Integer"),
            @ApiImplicitParam(name = "outerLink", value = "outerLink", dataType = "Boolean"),
            @ApiImplicitParam(name = "cachable", value = "cachable", dataType = "Boolean"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/createMenuItem", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody    public Result<Menu> createMenuItem(@RequestParam(required = true) String code, String name, String formCode, @RequestParam(required = true) String parentCode, String link, String preCode, Integer hasPath, Integer isResource,
                                                        @RequestParam(defaultValue = "60") Integer width,
                                                        String target, String pvg, String icon, @RequestParam(defaultValue = "0") Integer isUse,
                                                        @RequestParam(defaultValue = "0") Integer nav,
                                                        @RequestParam(defaultValue = "0") Integer canRepeat, String bigIcon, @RequestParam(defaultValue = "0") Integer isWidget,
                                                        @RequestParam(defaultValue = "200") Integer widgetWidth, @RequestParam(defaultValue = "100") Integer widgetHeight,
                                                        String fontIcon, String description, @RequestParam(defaultValue = "true") Integer front, String component,
                                                        @RequestParam(defaultValue = "false") Boolean outerLink, @RequestParam(defaultValue = "false") Boolean cachable, @RequestParam(defaultValue = "all") String applicationCode
    ) throws ValidateException {

        Result<Menu> result = new Result<>();

        Menu menu = new Menu();
        menu.setCode(code);
        if (!"".equals(preCode)) {
            if ("module".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_MODULE);
            } else if ("flow".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_FLOW);
            } else if ("basicdata".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_BASICDATA);
            }
            else if ("iframe".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_IFRAME);
            }
            else if ("portals".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_PORTALS);
            }
            else {
                menu.setType(ConstUtil.MENU_TYPE_PRESET);
            }
        } else {
            menu.setType(ConstUtil.MENU_TYPE_LINK);
        }
        menu.setName(name);
        menu.setParentCode(parentCode);
        menu.setLink(link);
        menu.setPreCode(preCode);
        menu.setFormCode(formCode);
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
        menu.setFront(front);
        menu.setComponent(component);
        menu.setOuterLink(outerLink);
        menu.setCachable(cachable);
        menu.setApplicationCode(applicationCode);
        menuService.create(menu);
        result.setData(menu);
        return result;
    }

    @ApiOperation(value = "创建菜单明细", notes = "创建菜单明细", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/getMenuItem")
    @ResponseBody
    public Result<Object> getMenuItem(@RequestParam(required = true) String code) {
        Result<Object> result = new Result<>();
        JSONObject jsonObject = new JSONObject();
        Menu menu = menuService.getMenu(code);

        if (menu == null) {
            result.error500("节点已被删除");
            return result;
        }

        jsonObject.put("menu", menu);

        String parentName = "无";
        if (!ConstUtil.MENU_ROOT.equals(code)) {
            Menu menuParent = menuService.getMenu(menu.getParentCode());
            parentName = menuParent.getName();
        }
        jsonObject.put("parentName", parentName);

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
        boolean isShowWidget = false;
        // 如果指定了界面风格
        if (isStyleSpecified) {
            // 如果指定的风格不是炫丽型，则不显示
            if (oaCfg.getInt("styleSpecified") == 3) {
                isShowWidget = true;
            }
        }

        jsonObject.put("isStyleSpecified", isStyleSpecified);
        jsonObject.put("isShowIcon", isShowIcon);
        jsonObject.put("isShowBigIcon", isShowBigIcon);
        jsonObject.put("isShowFontIcon", isShowFontIcon);
        jsonObject.put("isShowWidget", isShowWidget);

        ArrayList<String> fontAry = CSSUtil.getFontBefores();
        jsonObject.put("fontAry", fontAry);

        List<Privilege> allPrivList = privilegeService.getAll();
        jsonObject.put("allPrivList", allPrivList);

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
        jsonObject.put("itemOpts", opts);

        // 取出模块，前端已通过api: getModulesAll获取
        /*ModuleSetupDb msd = new ModuleSetupDb();
        Vector<ModuleSetupDb> v = msd.listUsed();
        jsonObject.put("allModule", v);*/

        StringBuffer flowOpts = new StringBuffer();
        com.redmoon.oa.flow.Leaf flowrootlf = new com.redmoon.oa.flow.Leaf();
        flowrootlf = flowrootlf.getLeaf(Leaf.CODE_ROOT);
        if (flowrootlf != null) {
            com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(flowrootlf);
            try {
                flowdv.getDirectoryAsOptions(request, flowrootlf, flowrootlf.getLayer(), flowOpts);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        jsonObject.put("flowOpts", flowOpts);

        // 取出基础数据大类
        SelectKindDb selectKindDb = new SelectKindDb();
        Vector kindV = selectKindDb.list();
        jsonObject.put("kindV", kindV);

        // 取出拥有的权限
        JSONArray aryPvg = new JSONArray();
        String[] ary = StrUtil.split(menu.getPvg(), ",");
        if (ary != null) {
            for (String s : ary) {
                JSONObject json = new JSONObject();
                String desc = "";
                if ("!admin".equals(s)) {
                    desc = "非管理员";
                    json.put("priv", s);
                } else {
                    Privilege privilege = privilegeService.getByPriv(s);
                    if (privilege == null) {
                        desc = s + " 不存在";
                    } else {
                        desc = privilege.getDescription();
                    }
                }
                json.put("priv", s);
                json.put("desc", desc);
                aryPvg.add(json);
            }
        }
        jsonObject.put("aryPvg", aryPvg);

        String menuName = menu.getName();
        if (menuName.startsWith("#")) {
            menuName = LocalUtil.LoadString(request, "res.ui.menu", code);
        }
        jsonObject.put("menuName", menuName);

        jsonObject.put("realLink", menuService.getRealLink(menu));
        jsonObject.put("component", menu.getComponent());
        jsonObject.put("front", menu.getFront());
        jsonObject.put("outerLink", menu.getOuterLink());
        jsonObject.put("cachable", menu.getCachable());
        jsonObject.put("applicationCode", menu.getApplicationCode());

        result.setData(jsonObject);
        return result;
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
        if ("flow".equals(preCode)) {
            formCode = ParamUtil.get(request, "flowTypeCode");
        } else if ("basicdata".equals(preCode)) {
            formCode = ParamUtil.get(request, "basicdata");
        }

        Menu menu = new Menu();
        menu.setCode(code);
        if (!"".equals(preCode)) {
            switch (preCode) {
                case "module":
                    menu.setType(ConstUtil.MENU_TYPE_MODULE);
                    break;
                case "flow":
                    menu.setType(ConstUtil.MENU_TYPE_FLOW);
                    break;
                case "basicdata":
                    menu.setType(ConstUtil.MENU_TYPE_BASICDATA);
                    break;
                default:
                    menu.setType(ConstUtil.MENU_TYPE_PRESET);
                    break;
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
        boolean isShowWidget = false;
        // 如果指定了界面风格
        if (isStyleSpecified) {
            // 如果指定的风格不是炫丽型，则不显示
            if (oaCfg.getInt("styleSpecified") == 3) {
                isShowWidget = true;
            }
        }

        model.addAttribute("isStyleSpecified", isStyleSpecified);
        model.addAttribute("isShowIcon", isShowIcon);
        model.addAttribute("isShowBigIcon", isShowBigIcon);
        model.addAttribute("isShowFontIcon", isShowFontIcon);
        model.addAttribute("isShowWidget", isShowWidget);

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
                LogUtil.getLog(getClass()).error(e);
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
            for (String s : ary) {
                JSONObject json = new JSONObject();
                String desc = "";
                if ("!admin".equals(s)) {
                    desc = "非管理员";
                    json.put("priv", s);
                } else {
                    Privilege privilege = privilegeService.getByPriv(s);
                    if (privilege == null) {
                        desc = s + " 不存在";
                    } else {
                        desc = privilege.getDescription();
                    }
                }
                json.put("priv", s);
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

        model.addAttribute("realLink", menuService.getRealLinkBack(menu));

/*        StringBuffer sb = new StringBuffer();
        Menu menuRoot = menuService.getMenu(ConstUtil.MENU_ROOT);
        menuService.ShowDirectoryAsOptionsToString(sb, menuRoot, menuRoot.getLayer());
        model.addAttribute("menuOpts", sb);*/

        return "th/admin/menu_edit";
    }

    @ApiOperation(value = "修改菜单明细", notes = "修改菜单明细", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String"),
            @ApiImplicitParam(name = "name", value = "名称", dataType = "String"),
            @ApiImplicitParam(name = "formCode", value = "表单编码", dataType = "String"),
            @ApiImplicitParam(name = "parentCode", value = "父级菜单编码", dataType = "String"),
            @ApiImplicitParam(name = "link", value = "链接", dataType = "String"),
            @ApiImplicitParam(name = "preCode", value = "预定义编码", dataType = "String"),
            @ApiImplicitParam(name = "hasPath", value = "hasPath", dataType = "Integer"),
            @ApiImplicitParam(name = "isResource", value = "isResource", dataType = "Integer"),
            @ApiImplicitParam(name = "target", value = "target", dataType = "String"),
            @ApiImplicitParam(name = "pvg", value = "权限", dataType = "String"),
            @ApiImplicitParam(name = "icon", value = "冗余", dataType = "String"),
            @ApiImplicitParam(name = "isUse", value = "是否启用", dataType = "Integer"),
            @ApiImplicitParam(name = "nav", value = "冗余", dataType = "Integer"),
            @ApiImplicitParam(name = "canRepeat", value = "冗余", dataType = "Integer"),
            @ApiImplicitParam(name = "bigIcon", value = "冗余", dataType = "String"),
            @ApiImplicitParam(name = "isWidget", value = "冗余", dataType = "Integer"),
            @ApiImplicitParam(name = "widgetWidth", value = "冗余", dataType = "Integer"),
            @ApiImplicitParam(name = "widgetHeight", value = "冗余", dataType = "Integer"),
            @ApiImplicitParam(name = "fontIcon", value = "图标", dataType = "String"),
            @ApiImplicitParam(name = "description", value = "描述", dataType = "String"),
            @ApiImplicitParam(name = "front", value = "菜单项是属于前端或后端", dataType = "Integer"),
            @ApiImplicitParam(name = "component", value = "组件", dataType = "String"),
            @ApiImplicitParam(name = "outerLink", value = "是否外链", dataType = "Boolean"),
            @ApiImplicitParam(name = "cachable", value = "是否缓存", dataType = "Boolean"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/updateMenuItem", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Menu> updateMenuItem(@RequestParam(required = true) String code, String name, String formCode, String parentCode, String link, String preCode, Integer hasPath, Integer isResource,
                                       @RequestParam(defaultValue = "60") Integer width,
                                       String target, String pvg, String icon, @RequestParam(defaultValue = "0") Integer isUse,
                                       @RequestParam(defaultValue = "0") Integer nav,
                                       @RequestParam(defaultValue = "0") Integer canRepeat, String bigIcon, @RequestParam(defaultValue = "0") Integer isWidget,
                                       @RequestParam(defaultValue = "200") Integer widgetWidth, @RequestParam(defaultValue = "100") Integer widgetHeight,
                                       String fontIcon, String description, @RequestParam(defaultValue = "0") Integer front, String component,
                                       @RequestParam(defaultValue = "false") Boolean outerLink, @RequestParam(defaultValue = "false") Boolean cachable, @RequestParam(defaultValue = "all") String applicationCode
    ) throws ValidateException {
        Result<Menu> result = new Result<>();

        Menu menu = menuService.getMenu(code);
        if (!"".equals(preCode)) {
            if ("module".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_MODULE);
            } else if ("flow".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_FLOW);
            } else if ("basicdata".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_BASICDATA);
            }
            else if ("iframe".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_IFRAME);
            }
            else if ("portals".equals(preCode)) {
                menu.setType(ConstUtil.MENU_TYPE_PORTALS);
            }
            else {
                menu.setType(ConstUtil.MENU_TYPE_PRESET);
            }
        } else {
            menu.setType(ConstUtil.MENU_TYPE_LINK);
        }
        menu.setName(name);
        menu.setParentCode(parentCode);
        menu.setLink(link);
        menu.setFormCode(formCode);
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
        menu.setFront(front);
        menu.setComponent(component);
        menu.setOuterLink(outerLink);
        menu.setCachable(cachable);
        menu.setApplicationCode(applicationCode);
        menuService.updateByCode(menu);
        result.setData(menu);
        return result;
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

    @ApiOperation(value = "删除菜单明细", notes = "删除菜单明细", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delMenuItem", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Menu> delMenuItem(@RequestParam(required = true) String code) {
        Menu menu = menuService.getMenu(code);
        return new Result<>(menuService.del(menu));
    }
	
	@PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/delMenu", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public JSONObject delMenu(@RequestParam(required = true) String code) {
        Menu menu = menuService.getMenu(code);
        return responseUtil.getResultJson(menuService.del(menu));
    }

    @ApiOperation(value = "移除菜单明细", notes = "移除菜单明细", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "编码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/moveMenuItem", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Menu> moveMenuItem(@RequestParam(required = true) String code, String parentCode, Integer position) {
        Result<Menu> result = new Result<>();
        try {
            boolean ok = menuService.move(code, parentCode, position);
            result.setResult(ok);
        } catch (ErrMsgException e) {
            result.error500("操作失败!");
        }
        return result;
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

    /**
     * 递归查找子节点
     *
     * @param
     * @return
     */
    public List<Menu> findChildren(Menu entity, List<Menu> treeList) {
        List<Menu> iList = new ArrayList<>();
        for (Menu it : treeList) {
            /*QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_code", it.getCode());
            queryWrapper.orderByAsc("orders");
            List<Menu> childrenList = menuService.list(queryWrapper);*/
            List<Menu> childrenList = menuCache.getChildren(it.getCode());
            if (childrenList.size() > 0) {
                for (Menu menu : childrenList) {
                    it.setIsLeaf(true);
                    it.setChildren(findChildren(menu, childrenList));
                }
            } else {
                it.setIsLeaf(false);
            }
            iList.add(it);
        }
        return iList;
    }

    @ApiOperation(value = "取得应用", notes = "取得在基础数据中配置的应用", httpMethod = "GET")
    @RequestMapping(value = "/getApplications", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> getApplications() {
        JSONArray ary = new JSONArray();
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("application");
        Vector<SelectOptionDb> vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        for (SelectOptionDb sod : vType) {
            JSONObject json = new JSONObject();
            json.put("code", sod.getValue());
            json.put("name", sod.getName());
            ary.add(json);
        }
        return new Result<>(ary);
    }
}
