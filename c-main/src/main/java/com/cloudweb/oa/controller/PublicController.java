package com.cloudweb.oa.controller;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONObject;
import com.baidu.ueditor.ActionEnter;
import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.JwtUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.security.AesUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.security.Config;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/public")
public class PublicController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    SysUtil sysUtil;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    SysProperties sysProperties;

    @ApiOperation(value = "取得服务端信息", notes = "取得服务端信息", httpMethod = "GET")
    @RequestMapping(value = "/getServerInfo", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Object> getServerInfo() {
        JSONObject json = sysUtil.getServerInfo();
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        json.put("isMenuGroupByApplication", cfg.getBooleanProperty("isMenuGroupByApplication"));

        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        json.put("isPwdCanReset", myconfig.getIntProperty("isPwdCanReset"));
        String pwdName = myconfig.getProperty("pwdName");
        String pwdAesKey = myconfig.getProperty("pwdAesKey");
        String pwdAesIV = myconfig.getProperty("pwdAesIV");
        json.put("pwdName", pwdName);
        json.put("pwdAesKey", pwdAesKey);
        json.put("pwdAesIV", pwdAesIV);
        int loginMode = com.redmoon.oa.Config.getInstance().getInt("loginMode");
        json.put("loginMode", loginMode);
        json.put("id", sysProperties.getId());
        json.put("showId", sysProperties.isShowId());
        json.put("isUserEmailRequired", sysProperties.isUserEmailRequired());
        json.put("isUserMobileRequired", sysProperties.isUserMobileRequired());

        String systemLoginParam = "";
        try {
            systemLoginParam = AesUtil.aesEncrypt(cfg.get("systemLoginParam"), pwdAesKey, pwdAesIV);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        json.put("systemIsOpen", cfg.getBooleanProperty("systemIsOpen"));
        json.put("systemLoginParam", systemLoginParam);
        json.put("systemStatus", cfg.get("systemStatus"));
        json.put("rootPath", sysUtil.getRootPath());

        return new Result<>(json);
    }

    // ueditor.all.js:8284 跨源读取阻止(CORB)功能阻止了 MIME 类型为 http://localhost:8085/oa/public/config?action=config&callback=bd__editor__u9fmg1 的跨源响应 text/html
    // 通过 script 标签, 本应获取到javascript内容，结果获取到 html 的内容(如果此时指定了 nosniff, 浏览器会直接认为响应内容是 text/html). 从而会触发 CORB, 响应会被清空;
    /**
     * 配置ueditor后端上传接口的初始化，注意一定要返回text/javascript，否则浏览器会报corb错误, 响应会被清空;
     * @param request
     * @param action
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/config", produces={"text/javascript;charset=UTF-8;"})
    @ResponseBody
    public String config(HttpServletRequest request, String action) throws IOException {
        log.info("UEditor action: " + action);
        if ("config".equals(action)) {
            try {
                request.setCharacterEncoding("utf-8");
            } catch (UnsupportedEncodingException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            // response.setHeader("Content-Type", "text/html");
            /*String path = getClass().getResource("/config.json").getFile();
            path = URLDecoder.decode(path, "utf-8");*/

            // String rootPath = request.getSession().getServletContext().getRealPath("/");

            // 返回：bd__editor__hrcbcw({"state": "\u914d\u7f6e\u6587\u4ef6\u521d\u59cb\u5316\u5931\u8d25"});

            // 此处用Global.getRealPath()其实是随便写的，因为在自定义的com.baidu.ueditor.ConfigManager.java中已经重写了读文件的方法
            return new ActionEnter(request, Global.getRealPath()).exec();

            // String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "config";

            /*PrintWriter printWriter = null;
            try {
                printWriter = response.getWriter();
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            printWriter.write(new ActionEnter(request, path).exec());
            printWriter.flush();
            printWriter.close();*/
        }
        return null;
    }

    @RequestMapping(value = "/jump", produces={MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Result<Object> jump(HttpServletResponse response, @RequestParam(required = true) String op, @RequestParam(required = true) String action) {
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        action = cn.js.fan.security.ThreeDesUtil.decrypthexstr(ssoCfg.getKey(), action);
        String[] ary = StrUtil.split(action, "\\|");
        if (ary==null) {
            return new Result<>(false, "操作非法！");
        }
        Map<String, String> map = new HashMap<>();
        for (String s : ary) {
            String[] pair = s.split("=");
            if (pair.length == 2) {
                map.put(pair[0], pair[1]);
            }
        }

        String userName = StrUtil.getNullStr(map.get("userName"));

        UserDb user = new UserDb();
        user = user.getUserDb(userName);

        if (!user.isLoaded()) {
            return new Result<>(false, "用户不存在！");
        }

        if (!user.isValid()) {
            return new Result<>(false, "用户非法！");
        }

        authUtil.doLoginByUserName(request, user.getName());

        // 生成token
        String authToken = jwtUtil.generate(userName);
        response.setHeader(jwtProperties.getHeader(), authToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", authToken);

        if (op.equals(WorkflowUtil.OP_FLOW_SHOW)) {
            String strFlowId = StrUtil.getNullStr(map.get("flowId"));
            jsonObject.put("flowId", StrUtil.toInt(strFlowId, -1));
        } else if (op.equals(WorkflowUtil.OP_FLOW_PROCESS)) {
            String strMyActionId = StrUtil.getNullStr(map.get("myActionId"));
            long myActionId = StrUtil.toLong(strMyActionId);
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(myActionId);
            if (!mad.isLoaded()) {
                return new Result<>(false, "动作不存在！");
            }

            if (!mad.getUserName().equals(userName)) {
                return new Result<>(false, "动作与处理者不一致！");
            }
            jsonObject.put("myActionId", myActionId);
        }

        return new Result<>(jsonObject);
    }

}
