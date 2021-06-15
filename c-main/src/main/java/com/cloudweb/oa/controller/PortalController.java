package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.LocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/user")
public class PortalController {
    @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/addDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addDesktopSetupItem(Model model) {
        Privilege privilege = new Privilege();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");
        long portalId = ParamUtil.getLong(request, "portalId", -1);
        String userName = privilege.getUser(request);
        String title = ParamUtil.get(request, "title");
        String moduleCode = ParamUtil.get(request, "moduleCode");

        if (moduleCode.equals("")) {
            return "desktop_setup.jsp?op=add&portalId=" + portalId + "&ret=0&msg=" + StrUtil.UrlEncode("请选择相应模块");
        }
        String[] moduleItems = ParamUtil.getParameters(request, "moduleItem");
        int count = ParamUtil.getInt(request, "count", 6);

        boolean canDelete = ParamUtil.getInt(request, "canDelete", 0) == 1;
        String icon = ParamUtil.get(request, "icon");

        int td = ParamUtil.getInt(request, "td", 0);
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid.setUserName(userName);
        udid.setTitle(title);
        udid.setModuleCode(moduleCode);
        udid.setModuleItem(StringUtils.join(moduleItems, ","));
        udid.setTd(td);
        udid.setCount(count);
        udid.setPortalId(portalId);
        udid.setCanDelete(canDelete);
        udid.setIcon(icon);

        if ("visual".equals(moduleCode)) {
            String fieldTitle = ParamUtil.get(request, "fieldTitle");
            String fieldDate = ParamUtil.get(request, "fieldDate");
            String metaData = "{\"fieldTitle\":\"" + fieldTitle + "\", \"fieldDate\":\"" + fieldDate + "\"}";
            udid.setMetaData(metaData);
        } else if ("flow".equals(moduleCode)) {
            String typeCode = ParamUtil.get(request, "typeCode");
            udid.setMetaData(typeCode);
        }

        udid.create();

        model.addAttribute("op", "add");
        model.addAttribute("ret", "1");
        model.addAttribute("msg", msg);

        return "../../user/desktop_setup";
    }

    @ResponseBody
    @RequestMapping(value = "/editDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String editDesktopSetupItem() {
        JSONObject json = new JSONObject();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");

        boolean re = false;
        int id = ParamUtil.getInt(request, "id", -1);
        String title = ParamUtil.get(request, "title");
        int rows = ParamUtil.getInt(request, "count", 5);
        int td = ParamUtil.getInt(request, "td", 0);
        int wordCount = ParamUtil.getInt(request, "wordCount", 80);
        boolean canDelete = ParamUtil.getInt(request, "canDelete", 0) == 1;
        String icon = ParamUtil.get(request, "icon");
        String[] moduleItems = ParamUtil.getParameters(request, "moduleItem");
        /*for (String item : moduleItems) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(item);
            if (lf!=null) {
                DebugUtil.i(getClass(), "item", lf.getCode() + ":" + lf.getName());
            }
        }*/
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);
        udid.setTitle(title);
        udid.setCount(rows);
        udid.setTd(td);
        udid.setWordCount(wordCount);
        udid.setCanDelete(canDelete);
        udid.setIcon(icon);
        udid.setModuleItem(StringUtils.join(moduleItems, ","));
        try {
            re = udid.save();
        } catch (ErrMsgException e) {
            msg = e.getMessage();
            e.printStackTrace();
        } catch (ResKeyException e) {
            msg = e.getMessage();
            e.printStackTrace();
        }
        json.put("ret", re ? 1 : 0);
        json.put("msg", msg);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delDesktopSetupItem() {
        JSONObject json = new JSONObject();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");

        int id = ParamUtil.getInt(request, "id", -1);
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);
        boolean re = false;
        try {
            re = udid.del();
        } catch (ErrMsgException e) {
            msg = e.getMessage();
            e.printStackTrace();
        } catch (ResKeyException e) {
            msg = e.getMessage();
            e.printStackTrace();
        }
        json.put("ret", re ? 1 : 0);
        json.put("msg", msg);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/copyDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String copyDesktopSetupItem() {
        JSONObject json = new JSONObject();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");

        int id = ParamUtil.getInt(request, "id", -1);
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);
        udid.copyToUsersDesktop();
        json.put("ret", 1);
        json.put("msg", msg);
        return json.toString();
    }
}
