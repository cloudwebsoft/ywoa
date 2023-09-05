package com.cloudweb.oa.controller;

import cn.js.fan.security.Login;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.MenuCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.permission.MenuPermission;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.visual.PersonBasicService;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.web.UserAgentParser;
import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.person.UserSet;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.PortalDb;
import com.redmoon.oa.ui.SkinMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

@Slf4j
@Controller
public class IndexController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    IUserService userService;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    MenuCache menuCache;

    @Autowired
    MenuPermission menuPermission;

    @Autowired
    IMenuService menuService;

    @Autowired
    UserCache userCache;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IRoleService roleServic;

    @Autowired
    PersonBasicService personBasicService;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("info", "登录错误！");
        model.addAttribute("loginError", true);
        return "error";
    }

    @RequestMapping("/info")
    public String info(Model model, String info) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("msg", info);
        return "th/error/info";
    }

    /**
     * 扫码登录页
     * @param response
     * @param model
     * @return
     */
    @GetMapping("/public/qrLogin")
    public String qrLogin(HttpServletResponse response, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        int loginMode = StrUtil.toInt(com.redmoon.oa.Config.getInstance().get("loginMode"), 0);
        // 如果只允许帐户密码登录，则重定向至登录页
        if (loginMode == 0) {
            return "redirect:/index.do";
        }
        else {
            return "th/public/index_qrcode_login";
        }
    }

    @GetMapping("/index")
    public String index(HttpServletResponse response, Model model) {
        Privilege privilege = new Privilege();
        try {
            privilege.logout(request, response);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        // 如果只允许扫码登录，则重定向至扫码登录页
        int loginMode = StrUtil.toInt(com.redmoon.oa.Config.getInstance().get("loginMode"), 0);
        if (loginMode == 1) {
            return "th/public/index_qrcode_login";
        }

        String skincode = ParamUtil.get(request, "skincode");
        if (skincode == null || "".equals(skincode)) {
            skincode = UserSet.getSkin(request);
            if (skincode == null || "".equals(skincode)) {
                skincode = UserSet.defaultSkin;
            }
        }

        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        String browserValid = cfg.get("browserValid");
        boolean isBrowserForbid = !"".equals(browserValid) && !"*".equals(browserValid);
        double browserIEMinVersion = StrUtil.toDouble(cfg.get("browserIEMinVersion"), 8);

        boolean systemIsOpen = cfg.getBooleanProperty("systemIsOpen");
        if (!systemIsOpen) {
            String op = ParamUtil.get(request, "op");
            String loginParam = cfg.get("systemLoginParam");
            // 判断登录参数是否相符
            if (!op.equals(loginParam)) {
                String systemStatus = cfg.get("systemStatus");
                request.setAttribute("info", systemStatus);
                return "error";
            }
        }

        String appName = cfg.get("enterprise");
        String mainTitle = ParamUtil.get(request, "mainTitle");
        String mainPage = ParamUtil.get(request, "mainPage");

        String tokenHidden = cn.js.fan.security.Form.getTokenHideInput(request);

        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        if (scfg.isDefendBruteforceCracking()) {
            Login.initlogin(request, "redmoonoa");
        }

        String browserType = UserAgentParser.getBrowser(request.getHeader("user-agent"));

        String qrcode_mobile_png_path = cfg.get("qrcode_mobile_png_path");
        String qrcode_andriod_download_path = cfg.get("qrcode_andriod_download_path");

        model.addAttribute("appName", appName);
        model.addAttribute("isRememberUserName", scfg.isRememberUserName());
        model.addAttribute("tokenHidden", tokenHidden);
        model.addAttribute("skincode", skincode);
        model.addAttribute("mainTitle", mainTitle);
        model.addAttribute("mainPage", mainPage);
        model.addAttribute("qrcode_mobile_png_path", qrcode_mobile_png_path);
        model.addAttribute("qrcode_andriod_download_path", qrcode_andriod_download_path);

        boolean isRememberPwdDisplay = scfg.getBooleanProperty("isRememberPwdDisplay");
        int isPwdCanReset = scfg.getIntProperty("isPwdCanReset");
        model.addAttribute("isRememberPwdDisplay", isRememberPwdDisplay);
        model.addAttribute("isPwdCanReset", isPwdCanReset);

        model.addAttribute("browserType", browserType);
        model.addAttribute("isBrowserForbid", isBrowserForbid);
        model.addAttribute("browserValid", browserValid.toLowerCase());
        model.addAttribute("browserIEMinVersion", browserIEMinVersion);

        if (isBrowserForbid) {
            String msg = "";
            boolean isLte = skincode.equals(SkinMgr.SKIN_CODE_LTE);
            if (isLte) {
                msg = "建议使用" + browserValid + "浏览器，IE需为9以上版本";
                if ("ie7".equals(browserType) || "ie8".equals(browserType)) {
                    ;
                }
            } else {
                msg = "建议使用" + browserValid + "浏览器";
            }
            model.addAttribute("browserInfo", msg);
        }

        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        String pwdName = myconfig.getProperty("pwdName");
        String pwdAesKey = myconfig.getProperty("pwdAesKey");
        String pwdAesIV = myconfig.getProperty("pwdAesIV");
        model.addAttribute("pwdName", pwdName);
        model.addAttribute("pwdAesKey", pwdAesKey);
        model.addAttribute("pwdAesIV", pwdAesIV);

        return "th/index";
    }

    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        return "th/error/403";
    }

    @GetMapping("/setup")
    public String setup() {
        return "redirect:setup/index.jsp";
    }

    @GetMapping("/wap")
    public String wap() {
        return "redirect:wap/index.jsp";
    }

    @GetMapping("/getMyInfo")
    @ResponseBody
    public Result<Object> getMyInfo(HttpServletResponse response) {
        JSONObject object = new JSONObject();
        User user = userCache.getUser(SpringUtil.getUserName());
        String portrait = userSetupService.getPortraitForFront(user);
        object.put("portrait", portrait);
        object.put("user", user);

        // 登录时，已经在Privilege.doLoginSession中设置了默认角色和部门，当请求时，JwtFilter根据头部已经设置了当前角色和部门，所以这儿可以直接取
        com.redmoon.oa.Config cfg = Config.getInstance();
        boolean isRoleSwitchable = cfg.getBooleanProperty("isRoleSwitchable");
        object.put("isRoleSwitchable", isRoleSwitchable);
        if (isRoleSwitchable) {
            String curRoleCode = Privilege.getCurRoleCode();
            List<Role> roleList = userCache.getRoles(SpringUtil.getUserName());

            // 将当前角色置顶
            boolean isOnlyOne = roleList.size() == 1;
            if (roleList.size() > 1) {
                Iterator<Role> ir = roleList.iterator();
                while (ir.hasNext()) {
                    Role role = ir.next();
                    if (role.getCode().equals(curRoleCode)) {
                        ir.remove();
                        break;
                    }
                }
            }
            if (curRoleCode != null && !isOnlyOne) {
                Role curRole = roleServic.getRole(curRoleCode);
                roleList.add(0, curRole);
            }

            object.put(ConstUtil.CUR_ROLE_CODE, curRoleCode);
            object.put("roleList", roleList);
        }
        boolean isDeptSwitchable = cfg.getBooleanProperty("isDeptSwitchable");
        object.put("isDeptSwitchable", isDeptSwitchable);
        if (isDeptSwitchable) {
            String curDeptCode = Privilege.getCurDeptCode();
            List<Department> deptList = departmentService.getDeptsOfUser(SpringUtil.getUserName());

            // 将当前置顶
            boolean isOnlyOne = deptList.size() == 1;
            if (deptList.size() > 1) {
                Iterator<Department> ir = deptList.iterator();
                while (ir.hasNext()) {
                    Department dept = ir.next();
                    if (dept.getCode().equals(curDeptCode)) {
                        ir.remove();
                        break;
                    }
                }
            }
            if (curDeptCode != null && !isOnlyOne) {
                Department curDepartment = departmentService.getDepartment(curDeptCode);
                deptList.add(0, curDepartment);
            }

            // 仅保留当前角色在部门中生效的记录
            if (isRoleSwitchable) {
                String curRoleCode = Privilege.getCurRoleCode();
                // 取得第一个角色与当前部门均对应的记录作为默认的当前部门
                deptList.removeIf(department -> !userOfRoleService.isRoleOfDept(user.getName(), curRoleCode, department.getCode()));
            }

            object.put(ConstUtil.CUR_DEPT_CODE, curDeptCode);
            object.put("deptList", deptList);
        }

        return new Result<>(object);
    }

    @GetMapping("/home")
    @ResponseBody
    public Result<Object> home(String item) {
        JSONObject object = new JSONObject();
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        String appName = cfg.get("enterprise");
        String refreshMessage = cfg.get("refresh_message");
        object.put("appName", appName);
        object.put("refreshMessage", refreshMessage);

        /*String mainTitle = ParamUtil.get(request, "mainTitle");
        String mainPage = ParamUtil.get(request, "mainPage");
        if ("".equals(mainPage)) {
            mainTitle = "桌面";
            if (2==cfg.getInt("styleDesktop")) {
                mainPage = "../desktop_lte.jsp";
            }
            else {
                mainPage = "../desktop.jsp";
            }
        } else {
            mainPage = "../" + mainPage;
        }
        object.put("mainTitle", mainTitle);
        object.put("mainPage", mainPage);*/

        boolean isMenuDisplay = true;
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin") && privilege.isUserPrivValid(request, "menu.main.forbid")) {
            isMenuDisplay = false;
        }
        object.put("isMenuDisplay", isMenuDisplay);

        // User user = userCache.getUser(SpringUtil.getUserName());
        /*String portrait = userSetupService.getPortraitForFront(user);
        object.put("portrait", portrait);
        object.put("user", user);*/

        boolean isMenuGroupByApplication = cfg.getBooleanProperty("isMenuGroupByApplication");
        String applicationCode = ParamUtil.get(request, "applicationCode");
        if (isMenuGroupByApplication) {
            // 前端指定的applicationCode优先
            if (StrUtil.isEmpty(applicationCode)) {
                applicationCode = cfg.get("applicationCode");
                if (StrUtil.isEmpty(applicationCode)) {
                    applicationCode = ConstUtil.MENU_APPLICATION_ALL;
                }
            }
        }

        List<Menu> list = menuCache.getChildren(ConstUtil.MENU_ROOT);
        List<Menu> childrenOfRoot = new ArrayList<>();

        for (Menu lf : list) {
            if (item!=null && !"all".equals(item)) {
                if (!item.equals(lf.getCode())) {
                    continue;
                }
            }
            if (lf.getIsUse() == 0 || lf.getCode().equals(ConstUtil.MENU_CODE_BOTTOM) || !menuPermission.canUserSee(request, lf)) {
                continue;
            }
            // 如果是叶子节点且不是前端菜单项
            if (lf.getFront() != ConstUtil.MENU_BOTH && lf.getFront() != ConstUtil.MENU_FRONT) {
                continue;
            }
            if (isMenuGroupByApplication) {
                if (!(lf.getApplicationCode().equals(ConstUtil.MENU_APPLICATION_ALL) || lf.getApplicationCode().equals(applicationCode))) {
                    continue;
                }
            }

            lf.setRealLink(menuService.getRealLink(lf));

            childrenOfRoot.add(lf);

            List<Menu> list2 = menuCache.getChildren(lf.getCode());
            List<Menu> children2 = new ArrayList<>();
            lf.setChildren(children2);

            if (list2.size() > 0) {
                for (Menu lf2 : list2) {
                    if (lf2.getIsUse() == 0 || !menuPermission.canUserSee(request, lf2)) {
                        continue;
                    }
                    // 如果是叶子节点且不是前端菜单项
                    if (lf2.getFront() != ConstUtil.MENU_BOTH && lf2.getFront() != ConstUtil.MENU_FRONT) {
                        continue;
                    }
                    if (isMenuGroupByApplication) {
                        if (!(lf2.getApplicationCode().equals(ConstUtil.MENU_APPLICATION_ALL) || lf2.getApplicationCode().equals(applicationCode))) {
                            continue;
                        }
                    }

                    lf2.setRealLink(menuService.getRealLink(lf2));
                    children2.add(lf2);

                    List<Menu> list3 = menuCache.getChildren(lf2.getCode());
                    List<Menu> children3 = new ArrayList<>();
                    lf2.setChildren(children3);

                    for (Menu lf3 : list3) {
                        if (lf3.getIsUse() == 0 || !menuPermission.canUserSee(request, lf3)) {
                            continue;
                        }
                        // 如果是叶子节点且不是前端菜单项
                        if (lf3.getFront() != ConstUtil.MENU_BOTH && lf3.getFront() != ConstUtil.MENU_FRONT) {
                            continue;
                        }
                        if (isMenuGroupByApplication) {
                            if (!(lf3.getApplicationCode().equals(ConstUtil.MENU_APPLICATION_ALL) || lf3.getApplicationCode().equals(applicationCode))) {
                                continue;
                            }
                        }

                        lf3.setRealLink(menuService.getRealLink(lf3));
                        children3.add(lf3);

                        // 第4级
                        List<Menu> list4 = menuCache.getChildren(lf3.getCode());
                        List<Menu> children4 = new ArrayList<>();
                        lf3.setChildren(children4);

                        for (Menu lf4 : list4) {
                            if (lf4.getIsUse() == 0 || !menuPermission.canUserSee(request, lf4)) {
                                continue;
                            }
                            // 如果是叶子节点且不是前端菜单项
                            if (lf4.getFront() != ConstUtil.MENU_BOTH && lf4.getFront() != ConstUtil.MENU_FRONT) {
                                continue;
                            }
                            if (isMenuGroupByApplication) {
                                if (!(lf4.getApplicationCode().equals(ConstUtil.MENU_APPLICATION_ALL) || lf4.getApplicationCode().equals(applicationCode))) {
                                    continue;
                                }
                            }

                            lf4.setRealLink(menuService.getRealLink(lf4));
                            children4.add(lf4);
                        }
                    }
                }
            }
        }
        object.put("tree", childrenOfRoot);

        boolean isDefaultDeskForbid = false;
        if (!privilege.isUserPrivValid(request, "admin") && privilege.isUserPrivValid(request, "desk.default.forbid")) {
            isDefaultDeskForbid = true;
        }

        int desktopIndex = 1;
        JSONArray portalAry = new JSONArray();
        PortalDb pd = new PortalDb();
        Vector<PortalDb> v = pd.list(isDefaultDeskForbid);
        for (PortalDb portalDb : v) {
            boolean canSee = portalDb.canUserSee(privilege.getUser(request));

            String desktopName = portalDb.getString("name");
            String desktopUrl = "desktop.jsp";
            // 如果是排在第1位的门户，则其页面为desktop.jsp，无需带参数，以免重复打开
            if (isDefaultDeskForbid && desktopIndex == 1) {
                desktopName = "桌面";
            } else {
                if (desktopIndex != 1) {
                    desktopUrl += "?portalId=" + portalDb.getLong("id");
                } else {
                    desktopName = "桌面";
                }
            }
            desktopIndex++;
            if (canSee) {
                JSONObject json = new JSONObject();
                json.put("desktopName", desktopName);
                json.put("desktopUrl", desktopUrl);
                portalAry.add(json);
            }
        }
        object.put("portalAry", portalAry);

        object.put("clusterNo", Global.getInstance().getClusterNo());

        boolean isArchiveShowAsUserInfo = cfg.getBooleanProperty("isArchiveShowAsUserInfo");
        if (isArchiveShowAsUserInfo) {
            long personId = personBasicService.getIdByUserName(SpringUtil.getUserName());
            if (personId == -1) {
                isArchiveShowAsUserInfo = false;
                DebugUtil.d(getClass(), "", SpringUtil.getUserName() + "'s personbasic info is not exist.");
            }
            else {
                object.put("personId", personId);
                String visitKey = SecurityUtil.makeVisitKey(personId);
                object.put("personVisitKey", visitKey);
            }
        }
        object.put("isArchiveShowAsUserInfo", isArchiveShowAsUserInfo);

        return new Result<>(object);
    }

    @GetMapping("/lte/index")
    public String lteIndex(String item, Model model) {
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        String appName = cfg.get("enterprise");
        String refreshMessage = cfg.get("refresh_message");

        String mainTitle = ParamUtil.get(request, "mainTitle");
        String mainPage = ParamUtil.get(request, "mainPage");
        if ("".equals(mainPage)) {
            mainTitle = "桌面";
            if (2==cfg.getInt("styleDesktop")) {
                mainPage = "../desktop_lte.jsp";
            }
            else {
                mainPage = "../desktop.jsp";
            }
        } else {
            mainPage = "../" + mainPage;
        }

        model.addAttribute("appName", appName);
        model.addAttribute("refreshMessage", refreshMessage);
        model.addAttribute("mainTitle", mainTitle);
        model.addAttribute("mainPage", mainPage);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        boolean isMenuDisplay = true;
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "admin") && privilege.isUserPrivValid(request, "menu.main.forbid")) {
            isMenuDisplay = false;
        }
        model.addAttribute("isMenuDisplay", isMenuDisplay);

        User user = userCache.getUser(SpringUtil.getUserName());
        String portrait = userSetupService.getPortrait(user);
        model.addAttribute("portrait", portrait);
        model.addAttribute("user", user);

        long t = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();
        List<Menu> list = menuCache.getChildren(ConstUtil.MENU_ROOT);
        for (Menu lf : list) {
            if (item!=null && !"all".equals(item)) {
                if (!item.equals(lf.getCode())) {
                    continue;
                }
            }

            if (lf.getIsUse() == 0 || lf.getCode().equals(ConstUtil.MENU_CODE_BOTTOM) || lf.getFront()==2 || !menuPermission.canUserSee(request, lf)) {
                continue;
            }
            String faIcon = "fa-columns";
            if (!"".equals(lf.getFontIcon())) {
                faIcon = lf.getFontIcon();
            }
            List<Menu> list2 = menuCache.getChildren(lf.getCode());
            sb.append("<li>");
            String link = menuService.getRealLinkBack(lf);
            if (list2.size()==0 && !StrUtil.isEmpty(link)) {
                if (!link.startsWith("http")) {
                    link = "../" + link;
                }
                String target = lf.getTarget();
                if ("_blank".equals(target) || "_top".equals(target)) {
                    target = " target=" + target;
                }
                sb.append("<a class=\"J_menuItem\" href=\"" + link + "\" " + target + " ><i class=\"fa " + faIcon + "\"></i> <span class=\"nav-label\">" + menuService.getName(lf) + "</span></a>");
            } else {
                sb.append("<a href=\"#\"><i class=\"fa " + faIcon + "\"></i><span class=\"nav-label\">" + menuService.getName(lf) + "</span>");
                if (list2.size() > 0) {
                    sb.append("<span class=\"fa arrow\"></span>");
                }
                sb.append("</a>");
            }
            if (list2.size() > 0) {
                sb.append("<ul class=\"nav nav-second-level\">");
                for (Menu lf2 : list2) {
                    if (lf2.getIsUse() == 0 || lf2.getFront()==2 || !menuPermission.canUserSee(request, lf2)) {
                        continue;
                    }
                    sb.append("<li>");
                    String link2 = menuService.getRealLinkBack(lf2);
                    List<Menu> list3 = menuCache.getChildren(lf2.getCode());
                    if (list3.size() == 0) {
                        if (!link2.startsWith("http")) {
                            link2 = "../" + link2;
                        }
                        String target = lf2.getTarget();
                        if ("_blank".equals(target) || "_top".equals(target)) {
                            target = " target=" + target;
                        }
                        sb.append("<a class=\"J_menuItem\" href=\"" + link2 + "\" " + target + " >" + menuService.getName(lf2) + "</a>");
                    } else {
                        sb.append("<a href=\"javascript:;\">" + menuService.getName(lf2) + "<span class=\"fa arrow\"></span></a>");
                        sb.append("    <ul class=\"nav nav-third-level\">");
                        for (Menu lf3 : list3) {
                            if (lf3.getIsUse() == 0 || lf3.getFront()==2 || !menuPermission.canUserSee(request, lf3)) {
                                continue;
                            }
                            String link3 = menuService.getRealLinkBack(lf3);
                            if (link3!=null) {
                                if (!link3.startsWith("http")) {
                                    link3 = "../" + link3;
                                }
                            } else {
                                // log.warn(lf3.getName() + "'s link is null");
                                DebugUtil.w(getClass(), "lteIndex", lf3.getName() + "'s link is null");
                            }
                            String target = lf3.getTarget();
                            if ("_blank".equals(target) || "_top".equals(target)) {
                                target = " target=" + target;
                            }
                            sb.append("    <li>");
                            sb.append("        <a class=\"J_menuItem\" href=\"" + link3 + "\" " + target + ">" + menuService.getName(lf3) + "</a>");
                            sb.append("    </li>");
                        }
                        sb.append("    </ul>");
                    }
                    sb.append("</li>");
                }
                sb.append("</ul>");
            }
            sb.append("</li>");
        }
        model.addAttribute("tree", sb.toString());

        if (Global.getInstance().isDebug()) {
            double s = (double)(System.currentTimeMillis() - t) / 1000;
            log.info("显示菜单 time:" + s + " s");
        }

        boolean isDefaultDeskForbid = false;
        if (!privilege.isUserPrivValid(request, "admin") && privilege.isUserPrivValid(request, "desk.default.forbid")) {
            isDefaultDeskForbid = true;
        }

        int desktopIndex = 1;
        JSONArray portalAry = new JSONArray();
        PortalDb pd = new PortalDb();
        Vector<PortalDb> v = pd.list(isDefaultDeskForbid);
        for (PortalDb portalDb : v) {
            boolean canSee = portalDb.canUserSee(privilege.getUser(request));

            String desktopName = portalDb.getString("name");
            String desktopUrl = "desktop.jsp";
            // 如果是排在第1位的门户，则其页面为desktop.jsp，无需带参数，以免重复打开
            if (isDefaultDeskForbid && desktopIndex == 1) {
                desktopName = "桌面";
            } else {
                if (desktopIndex != 1) {
                    desktopUrl += "?portalId=" + portalDb.getLong("id");
                } else {
                    desktopName = "桌面";
                }
            }
            desktopIndex++;
            if (canSee) {
                JSONObject json = new JSONObject();
                json.put("desktopName", desktopName);
                json.put("desktopUrl", desktopUrl);
                portalAry.add(json);
            }
        }
        model.addAttribute("portalAry", portalAry);

        model.addAttribute("clusterNo", Global.getInstance().getClusterNo());

        boolean isRoleSwitchable = cfg.getBooleanProperty("isRoleSwitchable");
        model.addAttribute("isRoleSwitchable", isRoleSwitchable);
        if (isRoleSwitchable) {
            String curRoleCode = Privilege.getCurRoleCode();
            List<Role> roleList = userCache.getRoles(SpringUtil.getUserName());
            /*
            // 将当前角色置顶
            boolean isOnlyOne = roleList.size() == 1;
            if (roleList.size() > 1) {
                Iterator<Role> ir = roleList.iterator();
                while (ir.hasNext()) {
                    Role role = ir.next();
                    if (role.getCode().equals(curRoleCode)) {
                        ir.remove();
                        break;
                    }
                }
            }
            if (curRoleCode != null && !isOnlyOne) {
                Role curRole = roleServic.getRole(curRoleCode);
                roleList.add(0, curRole);
            }*/

            model.addAttribute(ConstUtil.CUR_ROLE_CODE, curRoleCode);
            model.addAttribute("roleList", roleList);
        }
        boolean isDeptSwitchable = cfg.getBooleanProperty("isDeptSwitchable");
        model.addAttribute("isDeptSwitchable", isDeptSwitchable);
        if (isDeptSwitchable) {
            String curDeptCode = Privilege.getCurDeptCode();
            List<Department> deptList = departmentService.getDeptsOfUser(SpringUtil.getUserName());
            /*
            // 将当前置顶
            boolean isOnlyOne = deptList.size() == 1;
            if (deptList.size() > 1) {
                Iterator<Department> ir = deptList.iterator();
                while (ir.hasNext()) {
                    Department dept = ir.next();
                    if (dept.getCode().equals(curDeptCode)) {
                        ir.remove();
                        break;
                    }
                }
            }
            if (curDeptCode != null && !isOnlyOne) {
                Department curDepartment = departmentService.getDepartment(curDeptCode);
                deptList.add(0, curDepartment);
            }*/

            // 仅保留当前角色在部门中生效的记录
            if (isRoleSwitchable) {
                String curRoleCode = Privilege.getCurRoleCode();
                // 取得第一个角色与当前部门均对应的记录作为默认的当前部门
                deptList.removeIf(department -> !userOfRoleService.isRoleOfDept(user.getName(), curRoleCode, department.getCode()));
            }

            model.addAttribute(ConstUtil.CUR_DEPT_CODE, curDeptCode);
            model.addAttribute("deptList", deptList);
        }

        boolean isArchiveShowAsUserInfo = cfg.getBooleanProperty("isArchiveShowAsUserInfo");
        if (isArchiveShowAsUserInfo) {
            long personId = personBasicService.getIdByUserName(SpringUtil.getUserName());
            if (personId == -1) {
                isArchiveShowAsUserInfo = false;
                DebugUtil.d(getClass(), "", SpringUtil.getUserName() + "'s personbasic info is not exist.");
            }
            else {
                model.addAttribute("personId", personId);
                String visitKey = SecurityUtil.makeVisitKey(personId);
                model.addAttribute("personVisitKey", visitKey);
            }
        }
        model.addAttribute("isArchiveShowAsUserInfo", isArchiveShowAsUserInfo);

        return "th/lte/index";
    }
}
