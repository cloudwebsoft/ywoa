package com.cloudweb.oa.controller;

import cn.js.fan.security.Login;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.MenuCache;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.permission.MenuPermission;
import com.cloudweb.oa.service.IMenuService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.web.UserAgentParser;
import com.redmoon.oa.person.UserSet;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.PortalDb;
import com.redmoon.oa.ui.SkinMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("info", "登录错误！");
        model.addAttribute("loginError", true);
        return "error";
    }

    @GetMapping("/index")
    public String index(HttpServletResponse response, Model model) {
        Privilege privilege = new Privilege();
        try {
            privilege.logout(request, response);
        } catch (ErrMsgException e) {
            e.printStackTrace();
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

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
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

    @GetMapping("/lte/index")
    public String lteIndex(String item, Model model) {
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        String appName = cfg.get("enterprise");
        String refreshMessage = cfg.get("refresh_message");

        String mainTitle = ParamUtil.get(request, "mainTitle");
        String mainPage = ParamUtil.get(request, "mainPage");
        if ("".equals(mainTitle)) {
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

        User user = userService.getUser(SpringUtil.getUserName());
        String portrait = userSetupService.getPortrait(user);
        model.addAttribute("portrait", portrait);
        model.addAttribute("user", user);

        long t = System.currentTimeMillis();

        StringBuffer sb = new StringBuffer();
        List<Menu> list = menuCache.getChildren(ConstUtil.MENU_ROOT);
        for (Menu lf : list) {
            if (item!=null && !"all".equals(item)) {
                if (!item.equals(lf.getCode())) {
                    continue;
                }
            }
            if (lf.getIsUse() == 0 || lf.getCode().equals(ConstUtil.MENU_CODE_BOTTOM) || !menuPermission.canUserSee(request, lf)) {
                continue;
            }
            String faIcon = "fa-columns";
            if (!"".equals(lf.getFontIcon())) {
                faIcon = lf.getFontIcon();
            }
            List<Menu> list2 = menuCache.getChildren(lf.getCode());
            sb.append("<li>");
            String link = menuService.getRealLink(lf);
            if (!link.equals("")) {
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
                    if (lf2.getIsUse() == 0 || !menuPermission.canUserSee(request, lf2)) {
                        continue;
                    }
                    sb.append("<li>");
                    String link2 = menuService.getRealLink(lf2);
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
                            if (lf3.getIsUse() == 0 || !menuPermission.canUserSee(request, lf3)) {
                                continue;
                            }
                            String link3 = menuService.getRealLink(lf3);
                            if (!link3.startsWith("http")) {
                                link3 = "../" + link3;
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
        Vector v = pd.list(isDefaultDeskForbid);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            pd = (PortalDb)ir.next();
            boolean canSee = true;
            if (pd.getString("user_name").equals("system")) {
                canSee = pd.canUserSee(privilege.getUser(request));
            }
            String desktopName = pd.getString("name");
            String desktopUrl = "desktop.jsp";
            // 如果是排在第1位的门户，则其页面为desktop.jsp，无需带参数，以免重复打开
            if (isDefaultDeskForbid && desktopIndex == 1) {
                desktopName = "桌面";
            } else {
                if (desktopIndex != 1) {
                    desktopUrl += "?portalId=" + pd.getLong("id");
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

        return "th/lte/index";
    }
}
