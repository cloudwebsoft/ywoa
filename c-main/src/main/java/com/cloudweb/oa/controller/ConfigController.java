package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLConfig;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Group;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.redmoon.oa.android.CloudConfig;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserLevelDb;
import com.redmoon.oa.person.UserLevelMgr;
import com.redmoon.oa.ui.Skin;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.TwoDimensionCode;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import com.redmoon.oa.Config;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class ConfigController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    ResponseUtil responseUtil;

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/config")
    public String config(String kind, Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
        model.addAttribute("defaultSkinCode", sm.getDefaultSkinCode());

        Vector allSkin = sm.getAllSkin();

        model.addAttribute("allSkin", allSkin);

        model.addAttribute("isUseSMS", com.redmoon.oa.sms.SMSFactory.isUseSMS());

        if (kind==null) {
            kind = "";
        }
        model.addAttribute("kind", kind);

        Config cfg = Config.getInstance();
        Element root = cfg.getRootElement();
        List<Element> elements = root.getChild("oa").getChildren();
        License license = License.getInstance();
        boolean isA = License.FLOW_DESIGNER_A.equals(license.getFlowDesigner());

        JSONArray jsonAry = new JSONArray();
        for (Element e : elements) {
            String isDisplay = e.getAttributeValue("isDisplay");
            if ("false".equals(isDisplay)) {
                continue;
            }

            JSONObject json = new JSONObject();
            json.put("name", e.getName());
            json.put("value", e.getValue());
            json.put("desc", StrUtil.getNullStr(e.getAttributeValue("desc")));
            json.put("type", StrUtil.getNullStr(e.getAttributeValue("type")));
            boolean isFlowDesigner = "flowDesignerType".equals(e.getName());

            JSONArray optAry = null;
            String opts = e.getAttributeValue("options");
            String[] ary = StrUtil.split(opts, ",");
            if (ary != null) {
                optAry = new JSONArray();
                for (String item : ary) {
                    String[] aryOpts = StrUtil.split(item, "\\|");
                    if (aryOpts != null && aryOpts.length == 2) {
                        JSONObject jsonObject = new JSONObject();
                        if (isFlowDesigner && isA) {
                            if (aryOpts[0].equals(License.FLOW_DESIGNER_X)) {
                                continue;
                            }
                        }
                        jsonObject.put("value", aryOpts[0]);
                        jsonObject.put("name", aryOpts[1]);
                        optAry.add(jsonObject);
                    }
                }
            }
            json.put("optAry", optAry);

            json.put("kind", StrUtil.getNullStr(e.getAttributeValue("kind")));
            jsonAry.add(json);
        }
        model.addAttribute("jsonAry", jsonAry);

        return "th/admin/config";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/configEmail")
    public String configEmail(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        Global global = Global.getInstance();
        model.addAttribute("global", global);

        Config cfg = Config.getInstance();
        boolean flowNotifyByEmail = cfg.getBooleanProperty("flowNotifyByEmail");
        model.addAttribute("flowNotifyByEmail", flowNotifyByEmail);

        return "th/admin/config_email";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/setDefaultSkin", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String setDefaultSkin(@RequestParam(required = true) String defaultSkinCode) throws ValidateException {
        com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
        sm.setDefaultSkin(defaultSkinCode);
        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/editConfig", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String editConfig(@RequestParam(required = true) String name, @RequestParam(required = true) String value) throws ValidateException {
        Config myconfig = Config.getInstance();
        myconfig.put(name, value);

        // 同步系统名称至config_sys.xml
        if (name.equals("enterprise")) {
            TwoDimensionCode.generate2DCodeByMobileClient();
            XMLConfig cfg = new XMLConfig("config_sys.xml", false, "utf-8");
            cfg.set("Application.name", value);
            cfg.writemodify();
            Global.getInstance().init();
        }

        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/hideElement", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String hideElement(@RequestParam(required = true) String name) throws ValidateException {
        Config myconfig = Config.getInstance();
        myconfig.setIsDisplay(name, false);

        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/editEmailConfig", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String editEmailConfig(String flowNotifyByEmail) throws ValidateException {
        Config cfg = Config.getInstance();
        if (flowNotifyByEmail == null || flowNotifyByEmail.equals("")) {
            flowNotifyByEmail = "false";
        }
        cfg.put("flowNotifyByEmail", flowNotifyByEmail);
        cfg.writemodify();

        boolean isSmtpSSLFound = false;
        XMLConfig cfgSys = new XMLConfig("config_sys.xml", false, "utf-8");
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String fieldName = (String) e.nextElement();
            if (fieldName.startsWith("Application")) {
                String value = ParamUtil.get(request, fieldName);
                if (fieldName.equals("Application.smtpSSL")) {
                    isSmtpSSLFound = true;
                }
                if (fieldName.equals("Application.smtpCharset") && value.equals("other")) {
                    value = ParamUtil.get(request, "otherCharset");
                }
                cfgSys.set(fieldName, value);
            }
        }

        if (!isSmtpSSLFound) {
            cfgSys.set("Application.smtpSSL", "false");
        }
        cfgSys.writemodify();

        Global.getInstance().init();

        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/configFlowButton")
    public String configFlowButton(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        com.redmoon.oa.flow.FlowConfig flowConfig = new com.redmoon.oa.flow.FlowConfig();

        Element root = flowConfig.getRootElement();
        List<Element> elements = root.getChildren();

        JSONArray jsonAry = new JSONArray();
        for (Element e : elements) {
            String name = e.getName();
            if (!name.startsWith("FLOW_BUTTON_")) {
                continue;
            }
            /*if (!name.equals("FLOW_BUTTON_ATTENTION") &&
                    !name.equals("FLOW_BUTTON_TRANSFER") && !name.equals("FLOW_BUTTON_PLUS") && !name.equals("FLOW_BUTTON_DOC") &&
                    !name.equals("FLOW_BUTTON_CANCEL_ATTENTION") && !name.equals("FLOW_BUTTON_ALTER") &&
                    !name.equals("FLOW_BUTTON_SUSPEND") && !name.equals("FLOW_BUTTON_DISCARD")) {
                continue;
            }*/

            JSONObject json = new JSONObject();

            json.put("name", name);
            json.put("value", e.getValue());
            json.put("title", StrUtil.getNullStr(e.getAttributeValue("title")));
            json.put("isDisplay", StrUtil.getNullStr(e.getAttributeValue("isDisplay")));

            jsonAry.add(json);
        }
        model.addAttribute("jsonAry", jsonAry);

        return "th/admin/config_flow_button";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/editFlowButtonConfig", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String editFlowButtonConfig(@RequestParam(required = true) String name, @RequestParam(required = true) String value, String title, String isDisplay) throws ValidateException {
        com.redmoon.oa.flow.FlowConfig flowConfig = new com.redmoon.oa.flow.FlowConfig();
        if (value == null || value.equals("")) {
            throw new ValidateException("名称不能为空");
        }
        if (!flowConfig.checkChar(value)) {
            throw new ValidateException("名称中不能含有”字符");
        }
        if (!flowConfig.checkChar(title)) {
            throw new ValidateException("提示信息中不能含有“字符");
        }

        flowConfig.modify(name, value, title, isDisplay);

        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/userLevel")
    public String userLevel(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        UserLevelDb uld = new UserLevelDb();
        Vector v = uld.getAllLevel();
        model.addAttribute("v", v);

        return "th/admin/user_level";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/addUserLevel", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String addUserLevel() throws ValidateException {
        UserLevelMgr userLevelMgr = new UserLevelMgr();
        boolean re = true;
        String msg = "操作成功";
        try {
            re = userLevelMgr.create(request);
        } catch (ErrMsgException e) {
            msg = e.getMessage();
        }

        return responseUtil.getResultJson(re, msg).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/modifyUserLevel", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String modifyUserLevel() throws ValidateException {
        UserLevelMgr userLevelMgr = new UserLevelMgr();
        boolean re = true;
        String msg = "操作成功";
        try {
            re = userLevelMgr.modify(request);
        } catch (ErrMsgException e) {
            msg = e.getMessage();
        }

        return responseUtil.getResultJson(re, msg).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/delUserLevel", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String delUserLevel() throws ValidateException {
        UserLevelMgr userLevelMgr = new UserLevelMgr();
        boolean re = true;
        String msg = "操作成功";
        try {
            re = userLevelMgr.del(request);
        } catch (ErrMsgException e) {
            msg = e.getMessage();
        }

        return responseUtil.getResultJson(re, msg).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/configSecurity")
    public String configSecurity(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        JSONArray jsonAry = new JSONArray();
        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        Element root = myconfig.getRoot();
        Iterator ir = root.getChildren().iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String name = e.getName();

            String isDisplay = StrUtil.getNullStr(e.getAttributeValue("isDisplay"));
            if (isDisplay.equals("false")) {
                continue;
            }

            String value = e.getValue();
            String desc = e.getAttributeValue("desc");
            List list = e.getChildren();
            if (list.size() > 0) {
                Iterator irChild = list.iterator();
                while (irChild.hasNext()) {
                    Element eChild = (Element) irChild.next();
                    String childName = eChild.getName();
                    childName = name + "." + childName;
                    isDisplay = StrUtil.getNullStr(eChild.getAttributeValue("isDisplay"));
                    if (isDisplay.equals("false")) {
                        continue;
                    }
                    value = eChild.getValue();
                    desc = eChild.getAttributeValue("desc");

                    JSONObject json = new JSONObject();
                    json.put("name", childName);
                    json.put("value", value);
                    json.put("desc", desc);
                    json.put("isDisplay", isDisplay);

                    JSONArray optAry = null;
                    String opts = eChild.getAttributeValue("options");
                    String[] ary = StrUtil.split(opts, ",");
                    if (ary != null) {
                        optAry = new JSONArray();
                        for (String item : ary) {
                            String[] aryOpts = StrUtil.split(item, "\\|");
                            if (aryOpts != null && aryOpts.length == 2) {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("value", aryOpts[0]);
                                jsonObject.put("name", aryOpts[1]);
                                optAry.add(jsonObject);
                            }
                        }
                    }
                    json.put("optAry", optAry);

                    jsonAry.add(json);
                }
            } else {
                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("value", value);
                json.put("desc", desc);
                json.put("isDisplay", isDisplay);

                JSONArray optAry = null;
                String opts = e.getAttributeValue("options");
                String[] ary = StrUtil.split(opts, ",");
                if (ary != null) {
                    optAry = new JSONArray();
                    for (String item : ary) {
                        String[] aryOpts = StrUtil.split(item, "\\|");
                        if (aryOpts != null && aryOpts.length == 2) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("value", aryOpts[0]);
                            jsonObject.put("name", aryOpts[1]);
                            optAry.add(jsonObject);
                        }
                    }
                }
                json.put("optAry", optAry);

                jsonAry.add(json);
            }

        }
        model.addAttribute("jsonAry", jsonAry);

        return "th/admin/config_security";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/editSecurityConfig", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String editSecurityConfig(String name, String value) throws ValidateException {
        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        myconfig.setProperty(name, value);
        myconfig.refresh();

        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/setSms", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String setSms(boolean isUsed) throws ValidateException {
        com.redmoon.oa.sms.Config cfg = new com.redmoon.oa.sms.Config();
        cfg.setIsUsed(isUsed);
        com.redmoon.oa.sms.SMSFactory.init();
        com.redmoon.oa.sms.SMSFactory.getMsgUtil();
        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/configDeveloper")
    public String configDeveloper(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        CloudConfig cfg = CloudConfig.getInstance();
        model.addAttribute("developers", cfg.getDevelopers());
        return "th/admin/config_developer";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/modifyDeveloper", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String modifyDeveloper(@RequestParam(required = true)String userName, @RequestParam(required = true)String userSecret) throws ValidateException {
        CloudConfig cfg = CloudConfig.getInstance();
        boolean re = cfg.modifyDeveloper(userName, userSecret);
        return responseUtil.getResultJson(re).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/delDeveloper", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String delDeveloper(@RequestParam(required = true)String userName) throws ValidateException {
        CloudConfig cfg = CloudConfig.getInstance();
        boolean re = cfg.delDeveloper(userName);
        return responseUtil.getResultJson(re).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/addDeveloper", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String addDeveloper(@RequestParam(required = true)String userName, @RequestParam(required = true)String userSecret) throws ValidateException {
        CloudConfig cfg = CloudConfig.getInstance();
        boolean re = cfg.addDeveloper(userName, userSecret);
        return responseUtil.getResultJson(re).toString();
    }
}
