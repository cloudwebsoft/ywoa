package com.cloudweb.oa.controller;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.entity.UserOfRole;
import com.cloudweb.oa.service.IUserOfRoleService;
import com.cloudweb.oa.service.LoginService;
import com.cloudweb.oa.utils.JwtUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.TwoDimensionCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class QRCodeScanLoginController {

    private static final String QRCODE_LOGIN_GROUP = "cws_qrcode_login_group";

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    HttpServletRequest request;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    SysUtil sysUtil;

    @Autowired
    LoginService loginService;

    @Autowired
    UserCache userCache;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping(value="/public/getQrCodeForLogin", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> getQrCodeForLogin() {
        String uuid = UUID.randomUUID().toString().substring(0, 20);
        // 二维码内容
        // String url = Global.getFullRootPath(request) + "/mobile/qrCodeScanLogin?uuid=" + uuid;
        JSONObject jsonQRCode = new JSONObject();
        // jsonQRCode.put("url", url);
        jsonQRCode.put("path", "mobile/qrCodeScanLogin?uuid=" + uuid);
        jsonQRCode.put("uuid", uuid);
        //生成二维码
        String imgName =  uuid + ".png";
        String imgPath = Global.getRealPath() + "public/images/QRCodeLogin/" + imgName;

        TwoDimensionCode.generate2DCode(jsonQRCode.toString(), imgPath, "png");

        try {
            RMCache.getInstance().putInGroup(uuid, QRCODE_LOGIN_GROUP, "");
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        // 生成的图片访问地址
        String qrCodeImg = sysUtil.getRootPath() + "/public/images/QRCodeLogin/" + imgName;
        JSONObject json = new JSONObject();
        json.put("res", 0);
        json.put("uuid", uuid);
        json.put("QRCodeImg", qrCodeImg);
        int loginMode = com.redmoon.oa.Config.getInstance().getInt("loginMode");
        json.put("loginMode", loginMode);
        return new Result<>(json);
    }

    /**
     * 长连接检测
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/public/qrCodeLoginCheck", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> qrCodeLoginCheck(HttpServletResponse response, String uuid) {
        Config config = Config.getInstance();
        int waitTime = config.getInt("QRCodeLoginWaitTime");
        long inTime = System.currentTimeMillis();
        boolean isCheck = true;
        RMCache rmCache = RMCache.getInstance();
        while (isCheck) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            try {
                String userName = (String)rmCache.getFromGroup(uuid, QRCODE_LOGIN_GROUP);
                DebugUtil.i(getClass(), "qrCodeLoginCheck", uuid + " userName=" + userName);
                if(userName != null && !"".equals(userName)) {
                    RMCache.getInstance().remove(uuid, QRCODE_LOGIN_GROUP);
                    JSONObject json = responseUtil.getResJson(true);
                    json.put("userName", userName);

                    // 置为登录状态
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userName, userDetails.getPassword(), userDetails.getAuthorities());
                    // 存放authentication到SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
                    pvg.doLogin(request, userName, userDetails.getPassword());

                    String page = loginService.getUIModePage("");
                    json.put("url", request.getContextPath() + "/" + page);

                    String authToken = jwtUtil.generate(authentication.getName());
                    response.setHeader(jwtProperties.getHeader(), authToken);
                    json.put(jwtProperties.getHeader(),authToken);

                    com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
                    JSONObject jsonObject = sysUtil.getServerInfo();
                    jsonObject.put("isMenuGroupByApplication", cfg.getBooleanProperty("isMenuGroupByApplication"));
                    json.put("serverInfo", jsonObject);

                    return new Result<>(json);
                } else {
                    if(System.currentTimeMillis() - inTime > waitTime) {
                        isCheck = false;
                        RMCache.getInstance().remove(uuid, QRCODE_LOGIN_GROUP);
                        // 删除失效的二维码图片
                        FileUtil.del(Global.getRealPath() + "/public/images/QrCodeLogin/" + uuid + ".png");
                        // return responseUtil.getResJson(-2, "二维码超期，时长为" + waitTime/1000 + "秒<br/>请刷新后重新扫码登录").toString();
                        return new Result<>(false, "二维码超期，时长为" + waitTime/1000 + "秒，请刷新后重新扫码登录");
                    }
                }
            } catch (CacheException | IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return new Result<>(false);
    }

    @RequestMapping(value = "/mobile/qrCodeScanLogin", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String qrCodeScanLogin(String uuid, String skey) {
        Privilege pvg = new Privilege();
        try {
            String someName = (String)RMCache.getInstance().getFromGroup(uuid, QRCODE_LOGIN_GROUP);
            if (someName == null) {
                return responseUtil.getResJson(false, "扫码失败，二维码可能已过期").toString();
            }

            String userName = pvg.getUserName(skey);
            if ("".equals(userName)) {
                return responseUtil.getResJson(false, "标识非法").toString();
            }
            DebugUtil.i(getClass(), "qrCodeScanLogin", uuid + " userName=" + userName);
            RMCache.getInstance().putInGroup(uuid, QRCODE_LOGIN_GROUP, userName);
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        // 手机端出现授权登录按钮
        return responseUtil.getResJson(true).toString();
    }
}
