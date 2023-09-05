package com.cloudweb.oa.security;

import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.service.LoginService;
import com.cloudweb.oa.utils.JwtUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.ui.PortalDb;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author qiumin
 * @create 2019/1/13 12:59
 * @desc
 **/
@Component
public class LoginSuccessAuthenticationHandler implements AuthenticationSuccessHandler {

    @Autowired
    LoginService loginService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    SysUtil sysUtil;

    @Autowired
    SysProperties sysProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String str = null;
        try {
            if (loginService==null) {
                loginService = SpringUtil.getBean(LoginService.class);
            }
            str = loginService.login(request, response, authentication);
        }
        catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        String authToken = jwtUtil.generate(authentication.getName());

        response.setHeader(jwtProperties.getHeader(), authToken);

        JSONObject json = JSONObject.parseObject(str);
        json.put("code", 200);
        json.put(jwtProperties.getHeader(), authToken);

        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        JSONObject jsonObject = sysUtil.getServerInfo();

        // 用户的首页门户
        PortalDb pd = new PortalDb();
        pd = (PortalDb)pd.getQObjectDb(PortalDb.DESKTOP_DEFAULT_ID);
        String userName = (String)authentication.getPrincipal();
        boolean canSeeHome = pd.canUserSee(userName);
        if (canSeeHome) {
            jsonObject.put("portalId", PortalDb.DESKTOP_DEFAULT_ID);
        } else {
            int portalId = PortalDb.DESKTOP_DEFAULT_ID;
            Vector<PortalDb> v = pd.listByKind(PortalDb.KIND_DESKTOP);
            for (PortalDb portalDb : v) {
                boolean canSee = portalDb.canUserSee(userName);
                if (canSee) {
                    portalId = portalDb.getInt("id");
                    break;
                }
            }
            jsonObject.put("portalId", portalId);
        }

        // 菜单项所属的应用
        jsonObject.put("isMenuGroupByApplication", cfg.getBooleanProperty("isMenuGroupByApplication"));
        jsonObject.put("isExportExcelAsync", sysProperties.isExportExcelAsync());        
		jsonObject.put("isObjStoreEnabled", sysProperties.isObjStoreEnabled());
        // 是否启用悬浮进度栏
        jsonObject.put("isUploadPanel", sysProperties.isUploadPanel());
        // 是否启用悬浮按钮，如果为否，则使用顶栏的按钮
        jsonObject.put("isUploadPanelBtnSuspension", sysProperties.isUploadPanelBtnSuspension());

        json.put("serverInfo", jsonObject);

        //设置返回请求头
        response.setContentType("application/json;charset=utf-8");
        //写出流
        PrintWriter out = response.getWriter();
        out.write(json.toJSONString());
        out.flush();
        out.close();
    }
}
