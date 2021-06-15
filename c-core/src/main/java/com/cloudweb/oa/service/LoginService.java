package com.cloudweb.oa.service;

import cn.js.fan.security.Login;
import cn.js.fan.security.PwdUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.Config;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.WrongPasswordException;
import com.redmoon.oa.security.ServerIPPriv;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

@Service
public class LoginService {

    @Autowired
    IUserSetupService userSetupService;

    public String login(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        String userName = ParamUtil.get(request, "name");
        boolean re;
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        if (scfg.isDefendBruteforceCracking()) {
            try {
                Login.canlogin(request, "redmoonoa");
            }
            catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
        }

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
                    cn.js.fan.db.ResultRecord rr = (cn.js.fan.db.ResultRecord) ri.next();
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
            e.printStackTrace();
            json.put("ret", "0");
            json.put("msg", "数据库连接错误");
            return json.toString();
        }
        try {
            re = pvg.login(request, response);
            if (scfg.isDefendBruteforceCracking()) {
                Login.afterlogin(request,re,"redmoonoa",true);
            }
        }
        catch(WrongPasswordException | ErrMsgException e){
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        catch (NullPointerException e) {
            json.put("ret", "0");
            json.put("msg", "数据库连接错误！");
            e.printStackTrace();
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
            String name = userName;
            UserDb user = new UserDb();
            user = user.getUserDb(name);

            boolean isLoginAgreement = oaCfg.getBooleanProperty("isLoginAgreement");
            if (isLoginAgreement) {
                UserSetup userSetup = userSetupService.getUserSetup(user.getName());
                if (userSetup.getAgreeDate() == null) {
                    String url = "user/login_agreement.jsp";
                    json.put("ret", "1");
                    json.put("msg", "");
                    json.put("redirect", url);
                    return json.toString();
                }
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
                String pwd = ParamUtil.get(request, "pwd");
                if (pwd.equals(scfg.getInitPassword())) {
                    String url = "user/changeInitPwd.do";
                    json.put("ret", "1");
                    json.put("msg", "");
                    json.put("redirect", url);
                    return json.toString();
                }
            }

            if (scfg.isForceChangeWhenWeak()) {
                com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
                String pwdName = myconfig.getProperty("pwdName");
                String pwd = ParamUtil.get(request, pwdName);

                if (PwdUtil.getPasswordLevel(pwd).getType() < scfg.getStrenthLevelMin() && scfg.getStrenthLevelMin()!=0) {
                    String url = "user/changeMyPwd.do";
                    json.put("ret", "1");
                    json.put("msg", "");
                    json.put("redirect", url);
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

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());

        Config cfg = Config.getInstance();
        boolean isSpecified = "2".equals(cfg.get("styleMode"));
        String url = "";
        // 指定风格
        if (isSpecified) {
            int styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
            if (styleSpecified!=-1) {
                if (styleSpecified== ConstUtil.UI_MODE_PROFESSION) {
                    url = "oa.jsp" + queryStr;
                }
                else if (styleSpecified==ConstUtil.UI_MODE_FLOWERINESS) {
                    url = "mydesktop.jsp" + queryStr;
                }
                else if (styleSpecified==ConstUtil.UI_MODE_FASHION) {
                    url = "main.jsp" + queryStr;
                }
                else if (styleSpecified == ConstUtil.UI_MODE_PROFESSION_NORMAL) {
                    url = "oa_main.jsp" + queryStr;// 经典型传统菜单
                }
                else if (styleSpecified == ConstUtil.UI_MODE_LTE) {
                    url = "lte/index.do" + queryStr;
                }
                else {
                    if (userSetup.getMenuMode()==ConstUtil.MENU_MODE_NEW) {
                        url = "oa.jsp" + queryStr;
                    }
                    else {
                        url = "oa_main.jsp" + queryStr;
                    }
                }
            }
        }
        else {
            if (userSetup.getUiMode() == ConstUtil.UI_MODE_NONE) {
                com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
                if (license.isVip()) {
                    url = "ui_mode_guide.jsp" + queryStr;
                } else {
                    if (userSetup.getMenuMode() == ConstUtil.MENU_MODE_NEW) {
                        url = "oa.jsp" + queryStr;
                    } else {
                        url = "oa_main.jsp" + queryStr;
                    }
                }
            } else if (userSetup.getUiMode() == ConstUtil.UI_MODE_PROFESSION) {
                if (userSetup.getMenuMode() == ConstUtil.MENU_MODE_NEW) {
                    url = "oa.jsp" + queryStr;
                } else {
                    url = "oa_main.jsp" + queryStr;
                }
            } else if (userSetup.getUiMode() == ConstUtil.UI_MODE_FLOWERINESS) {
                url = "mydesktop.jsp" + queryStr;
            } else if (userSetup.getUiMode() == ConstUtil.UI_MODE_FASHION) {
                url = "main.jsp" + queryStr;
            } else if (userSetup.getUiMode() == ConstUtil.UI_MODE_LTE) {
                url = "lte/index.do" + queryStr;
            } else {
                if (userSetup.getMenuMode() == ConstUtil.MENU_MODE_NEW) {
                    url = "oa.jsp" + queryStr;
                } else {
                    url = "oa_main.jsp";
                }
            }
        }
        return url;
    }
}