package com.cloudweb.oa.service;

import cn.js.fan.security.Login;
import cn.js.fan.security.PwdUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONArray;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.security.AesUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.WrongPasswordException;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.security.ServerIPPriv;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;

@Service
public class LoginService {

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    UserCache userCache;

    @Autowired
    IUserService userService;

    public String login(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws JSONException {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        String loginName = ParamUtil.get(request, "name");
        // 根据登录名取得用户名
        User user = userService.getUserByLoginName(loginName);
        String userName = user.getName();

        boolean re;
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        /*if (scfg.isDefendBruteforceCracking()) {
            try {
                Login.canlogin(request, "redmoonoa");
            }
            catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
        }*/

        //比对是否和数据库中的版本相同
        com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
        com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
        String ver = StrUtil.getNullStr(oaCfg.get("version"));
        String spVer = StrUtil.getNullStr(spCfg.get("version"));
        try{
            JdbcTemplate jt = new JdbcTemplate();
            String sql = "select version,sp_version from oa_sys_ver";
            cn.js.fan.db.ResultIterator ri = jt.executeQuery(sql);
            String version = "";
            String spVersion = "";
            if (ri != null && ri.size() == 1){
                while(ri.hasNext()){
                    cn.js.fan.db.ResultRecord rr = ri.next();
                    version = StrUtil.getNullStr(rr.getString("version")).trim();
                    spVersion = StrUtil.getNullStr(rr.getString("sp_version")).trim();
                    break;
                }
            }
            if(!ver.equals(version) || !spVer.equals(spVersion)){
                json.put("ret", "0");
                json.put("msg", ver + "(" + spVer + ")与数据库版本" + version + "(" + spVersion + ")不匹配，请联系客服");
                return json.toString();
            }
        }catch(SQLException e){
            LogUtil.getLog(getClass()).error(e);
            json.put("ret", "0");
            json.put("msg", "数据库连接错误");
            return json.toString();
        }
        try {
            re = pvg.login(request, response, authentication);
            /*if (scfg.isDefendBruteforceCracking()) {
                Login.afterlogin(request,re,"redmoonoa",true);
            }*/
        }
        catch(WrongPasswordException | ErrMsgException e){
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        catch (NullPointerException e) {
            json.put("ret", "0");
            json.put("msg", "数据库连接错误！");
            LogUtil.getLog(getClass()).error(e);
            return json.toString();
        }
        if (re) {
            String serverName = request.getServerName();
            ServerIPPriv sip = new ServerIPPriv(serverName);
            if (!sip.canUserLogin(userName)) {
                json.put("ret", "0");
                json.put("msg", "禁止登录！");
                return json.toString();
            }
        }

        if (re) {
            com.redmoon.oa.Config cfg = Config.getInstance();
            // 如果角色可切换，则取默认角色，在上面的pvg.login(...)中已经调用Privilege.doLoginSession，其中已经设置了默认当前角色和部门
            String curRoleCode = Privilege.getCurRoleCode();
            if (cfg.getBooleanProperty("isRoleSwitchable")) {
                json.put("isRoleSwitchable", true);
                // 置当前切换角色
                if (curRoleCode != null) {
                    json.put(ConstUtil.CUR_ROLE_CODE, curRoleCode);
                    response.setHeader(ConstUtil.CUR_ROLE_CODE, curRoleCode);
                } else {
                    json.put(ConstUtil.CUR_ROLE_CODE, "");
                }
            } else {
                json.put("isRoleSwitchable", false);
            }

            if (cfg.getBooleanProperty("isDeptSwitchable")) {
                json.put("isDeptSwitchable", true);
                // 置当前切换部门
                String curDeptCode = Privilege.getCurDeptCode();
                if (curDeptCode!=null) {
                    json.put(ConstUtil.CUR_DEPT_CODE, curDeptCode);
                    response.setHeader(ConstUtil.CUR_DEPT_CODE, curDeptCode);
                } else {
                    json.put(ConstUtil.CUR_DEPT_CODE, "");
                }
            } else {
                json.put("isDeptSwitchable", false);
            }

            if (scfg.getIntProperty("isPwdCanReset")==1) {
                // 检查用户的邮箱信息是否已完善
                String email = user.getEmail();
                if ("".equals(email)) {
                    String url = "user/user_info_setup.jsp";
                    json.put("ret", "1");
                    json.put("msg", "");
                    json.put("redirect", url);
                    return json.toString();
                }
            }

            if (scfg.isForceChangeInitPassword()) {
                // 判断是否初始密码
                com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
                String pwdName = myconfig.getProperty("pwdName");
                String pwd = ParamUtil.get(request, pwdName);

                String pwdAesKey = myconfig.getProperty("pwdAesKey");
                String pwdAesIV = myconfig.getProperty("pwdAesIV");
                try {
                    pwd = AesUtil.aesDecrypt(pwd, pwdAesKey, pwdAesIV);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }

                if (pwd.equals(scfg.getInitPassword())) {
                    // String url = "user/changeInitPwd.do";
                    json.put("ret", "1");
                    json.put("msg", "");
                    // json.put("redirect", url);
                    json.put("isForceChangePwd", true);
                    return json.toString();
                }
            }

            if (scfg.isForceChangeWhenWeak()) {
                int minLen = scfg.getIntProperty("password.minLen");
                com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
                String pwdName = myconfig.getProperty("pwdName");
                String pwd = ParamUtil.get(request, pwdName);

                String pwdAesKey = myconfig.getProperty("pwdAesKey");
                String pwdAesIV = myconfig.getProperty("pwdAesIV");
                try {
                    pwd = AesUtil.aesDecrypt(pwd, pwdAesKey, pwdAesIV);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }

                if (pwd.length() < minLen || PwdUtil.getPasswordLevel(pwd).getType() < scfg.getStrenthLevelMin() && scfg.getStrenthLevelMin()!=0) {
                    // String url = "user/changeMyPwd.do";
                    json.put("ret", "1");
                    json.put("msg", "");
                    // json.put("redirect", url);
                    json.put("isForceChangePwd", true);
                    return json.toString();
                }
            }

            String mainTitle = ParamUtil.get(request, "mainTitle");
            String mainPage = ParamUtil.get(request, "mainPage");
            String queryStr = "";
            if (!"".equals(mainPage)) {
                mainPage = cn.js.fan.security.AntiXSS.antiXSS(mainPage);
            }
            queryStr = "?mainTitle=" + StrUtil.UrlEncode(mainTitle) + "&mainPage=" + mainPage;

            json.put("ret", "1");
            json.put("msg", "");
            // 后端登录需要用到，所以暂不删除redirect
            json.put("redirect", getUIModePage(queryStr));
        }
        else {
            json.put("ret", "0");
            json.put("msg", "登录失败，请检查用户名或密码是否正确！");
        }

        return json.toString();
    }

    public String getUIModePage(String queryStr) {
        if (!"".equals(queryStr) && !queryStr.startsWith("?")) {
            queryStr = "?" + queryStr;
        }
        return "lte/index.do" + queryStr;
    }
}