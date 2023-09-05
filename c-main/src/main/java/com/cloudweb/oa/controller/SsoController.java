package com.cloudweb.oa.controller;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.ILicense;
import com.cloudweb.oa.api.IMyflowUtil;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.ui.SkinMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Vector;

@Controller
public class SsoController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    AuthUtil authUtil;

    /**
     * 从第三方系统跳转
     * /public/jump?mode=1&token=userName&data={type=module, moduleCode=模块的编码}
     * @param token
     * @param mode
     * @param data
     * @param model
     * @return
     */
    @RequestMapping(value = "/public/jump", method={RequestMethod.GET})
    public String jump(String token, @RequestParam(required = true, defaultValue = "0") Integer mode, String data, Model model) {
        String userName;
        if (mode == 0) {
            String desKey = Config.getInstance().getKey();
            String plainText = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, token);
            String[] ary = StrUtil.split(plainText, "\\|"); // 格式为：userName|timestamp
            if (ary == null || ary.length != 2) {
                model.addAttribute("msg", "token 格式非法");
                return "th/error/error";
            }

            String timestamp = ary[1];
            Date d = DateUtil.parse(timestamp);
            // 大于 30 秒则超时
            if ((d.getTime() - (new Date()).getTime())/1000 > 30) {
                model.addAttribute("msg", "token超时");
                return "th/error/error";
            }
            userName = ary[0];
        }
        else {
            userName = token;
        }

        String type;
        JSONObject json = JSONObject.parseObject(data);
        if (!json.containsKey("type")) {
            model.addAttribute("msg", "参数非法");
            return "th/error/error";
        }
        else {
            type = json.getString("type");
        }

        authUtil.doLoginByUserName(request, userName);

        if ("module".equals(type)) {
            if (json.containsKey("moduleCode")) {
                String moduleCode = json.getString("moduleCode");
                return "redirect:/visual/moduleListPage.do?moduleCode=" + moduleCode;
            }
            else {
                model.addAttribute("msg", type + " 参数非法");
                return "th/error/error";
            }
        }

        model.addAttribute("msg", type + " 非法");
        return "th/error/error";
    }
}
