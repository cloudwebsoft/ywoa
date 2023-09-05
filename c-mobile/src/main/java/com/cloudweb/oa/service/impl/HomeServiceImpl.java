package com.cloudweb.oa.service.impl;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.controller.mobile.HomeController;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.JwtUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.security.AesUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.android.CloudConfig;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.security.ServerIPPriv;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.usermobile.UserMobileMgr;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    IMobileService mobileService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    IUserService userService;

    @Autowired
    private UserCache userCache;

    @Autowired
    IDepartmentService departmentService;

    @Override
    public String loginByLoginInfo(HttpServletRequest request, String userName, String deviceId, String client, HttpServletResponse response) {
        com.alibaba.fastjson.JSONObject jReturn = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONObject jResult = new com.alibaba.fastjson.JSONObject();
        jReturn.put("result", jResult);

        // PC端登录模式，当为1或2时允许扫码登录
        int loginMode = StrUtil.toInt(com.redmoon.oa.Config.getInstance().get("loginMode"), 0);
        jReturn.put("loginMode", loginMode);

        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        return onLoginSuccess(request, response, ud, "", deviceId, client, jReturn, jResult);
    }

    @Override
    public String loginByMiniLoginInfo(HttpServletRequest request, JSONObject userInfoJson, String deviceId, String client, HttpServletResponse response) {
        com.alibaba.fastjson.JSONObject jReturn = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONObject jResult = new com.alibaba.fastjson.JSONObject();
        jReturn.put("result", jResult);

        com.redmoon.weixin.Config cfg = com.redmoon.weixin.Config.getInstance();
        int wxMiniLoginMode = StrUtil.toInt(cfg.getProperty("wxMiniLoginMode"), com.redmoon.weixin.Config.LOGIN_MODE_OPENID);
        jReturn.put("wxMiniLoginMode", wxMiniLoginMode);

        // PC端登录模式，当为1或2时允许扫码登录
        int loginMode = StrUtil.toInt(com.redmoon.oa.Config.getInstance().get("loginMode"), 0);
        jReturn.put("loginMode", loginMode);

        User user;
        String miniId = "";
        String curId = "";
        if (wxMiniLoginMode == com.redmoon.weixin.Config.LOGIN_MODE_OPENID || wxMiniLoginMode == com.redmoon.weixin.Config.LOGIN_MODE_ACCOUNT) {
            curId = "openid";
            user = userService.getUserByOpenId(userInfoJson.getString("openId"));
            jReturn.put("openId", userInfoJson.getString("openId"));
            miniId = userInfoJson.getString("openId");
        }
        else {
            curId = "unionid";
            user = userService.getUserByUnionId(userInfoJson.getString("unionId"));
            // 手机端始终返回openId
            jReturn.put("openId", userInfoJson.getString("unionId"));
            miniId = userInfoJson.getString("unionId");
        }
        if (user == null) {
            jReturn.put(com.cloudweb.oa.utils.ConstUtil.RES, HomeController.RETURNCODE_OPENID_NONE);
            jReturn.put(com.cloudweb.oa.utils.ConstUtil.MSG, curId + "配对失败");
            return jReturn.toString();
        }
        else {
            UserDb ud = new UserDb();
            ud = ud.getUserDb(user.getId());
            return onLoginSuccess(request, response, ud, miniId, deviceId, client, jReturn, jResult);
        }
    }

    @Override
    public String login(HttpServletRequest request,
                        String name,
                        String password,
                        String deviceId,
                        String client,
                        String openId,
                        HttpServletResponse response
    ) {
        com.alibaba.fastjson.JSONObject jReturn = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONObject jResult = new com.alibaba.fastjson.JSONObject();
        boolean re = false;

        String decrypPassWord = password;
        /*String encryptType = ParamUtil.get(request, "encryptType");
        if (!"none".equals(encryptType)) {
            try {
                decrypPassWord = Des3.decode(password);
            } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
            }
        }*/

        com.redmoon.weixin.Config cfg = com.redmoon.weixin.Config.getInstance();
        int wxMiniLoginMode = StrUtil.toInt(cfg.getProperty("wxMiniLoginMode"), com.redmoon.weixin.Config.LOGIN_MODE_OPENID);
        jReturn.put("wxMiniLoginMode", wxMiniLoginMode);

        // PC端登录模式，当为1或2时允许扫码登录
        int loginMode = StrUtil.toInt(com.redmoon.oa.Config.getInstance().get("loginMode"), 0);
        jReturn.put("loginMode", loginMode);

        User user = userService.getUserByLoginName(name);
        String userName = null;

        UserDb ud = new UserDb();
        if (user != null) {
            userName = user.getName();
            re = ud.Auth(userName, decrypPassWord);
        }
        if (!re) {
            // 检查是否使用了工号登录
            AccountDb ad = new AccountDb();
            ad = ad.getAccountDb(name);
            if (ad.isLoaded()) {
                userName = ad.getUserName();
                String pwdMD5 = "";
                try {
                    pwdMD5 = SecurityUtil.MD5(decrypPassWord);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
                try {
                    if (!"".equals(userName) && pvg.Authenticate(userName, pwdMD5)) {
                        re = true;
                    }
                } catch (ErrMsgException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }

            if (!re) {
                jReturn.put("res", HomeController.RES_FAIL);
                jReturn.put("msg", "用户名或密码错误");
                jResult.put("returnCode", HomeController.RETURNCODE_ERROR_PASSWORD);
                jReturn.put("result", jResult);

                LogDb log = new LogDb();
                log.setUserName(name);
                log.setType(LogDb.TYPE_LOGIN);
                log.setDevice(LogDb.DEVICE_MOBILE);
                log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
                log.setIp(StrUtil.getIp(request));
                log.setUnitCode(ud.getUnitCode());
                log.setRemark("用户名或密码错误");

                log.create();

                return jReturn.toString();
            }
        }

        ud = ud.getUserDb(userName);
        if (ud == null || !ud.isLoaded()) {
            jReturn.put("res", HomeController.RES_FAIL);
            jReturn.put("msg", "用戶不存在");
            jResult.put("returnCode", HomeController.RETURNCODE_USER_NOT_EXIST);
            jReturn.put("result", jResult);

            LogDb log = new LogDb();
            log.setUserName(name);
            log.setType(LogDb.TYPE_LOGIN);
            log.setDevice(LogDb.DEVICE_MOBILE);
            log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
            log.setIp(StrUtil.getIp(request));
            log.setRemark("用戶不存在");
            log.create();

            return jReturn.toString();
        }

        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        if (scfg.isForceChangeInitPassword()) {
            // 判断是否初始密码
            if (password.equals(scfg.getInitPassword())) {
                jReturn.put("res", HomeController.RES_FAIL);
                jReturn.put("msg", "请修改密码");
                jReturn.put("isForceChangePwd", true);
                String skey = mobileService.generateSkey(ud.getName());
                jResult.put("skey", skey);
                response.setHeader(com.cloudweb.oa.utils.ConstUtil.SKEY, skey);
                return jReturn.toString();
            }
        }

        return onLoginSuccess(request, response, ud, openId, deviceId, client, jReturn, jResult);
    }

    public String onLoginSuccess(HttpServletRequest request, HttpServletResponse response, UserDb ud, String openId, String deviceId, String client, com.alibaba.fastjson.JSONObject jReturn, com.alibaba.fastjson.JSONObject jResult) {
        if (!ud.isValid()) {
            jReturn.put("res", 0);
            jReturn.put("msg", "非法用戶");
            jResult.put("returnCode", HomeController.RETURNCODE_NOT_VALID_USER);
            jReturn.put("result", jResult);

            LogDb log = new LogDb();
            log.setUserName(ud.getName());
            log.setType(LogDb.TYPE_LOGIN);
            log.setDevice(LogDb.DEVICE_MOBILE);
            log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
            log.setIp(StrUtil.getIp(request));
            log.setUnitCode(ud.getUnitCode());
            log.setRemark("非法用户");
            log.create();

            return jReturn.toString();
        }
        if (ud.getIsPass() == 0) {
            jReturn.put("res", 0);
            jReturn.put("msg", "未审核用户");
            jResult.put("returnCode", HomeController.RETURNCODE_NOT_EXAMINE);
            jReturn.put("result", jResult);

            LogDb log = new LogDb();
            log.setUserName(ud.getName());
            log.setType(LogDb.TYPE_LOGIN);
            log.setDevice(LogDb.DEVICE_MOBILE);
            log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
            log.setIp(StrUtil.getIp(request));
            log.setUnitCode(ud.getUnitCode());
            log.setRemark("未审核用户");
            log.create();
            return jReturn.toString();
        } else if (ud.getIsPass() == 2) {
            jReturn.put("res", 0);
            jReturn.put("msg", "未通过审核用户");
            jResult.put("returnCode", HomeController.RETURNCODE_NOT_PASS_EXAMINE);
            jReturn.put("result", jResult);

            LogDb log = new LogDb();
            log.setUserName(ud.getName());
            log.setType(LogDb.TYPE_LOGIN);
            log.setDevice(LogDb.DEVICE_MOBILE);
            log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
            log.setIp(StrUtil.getIp(request));
            log.setUnitCode(ud.getUnitCode());
            log.setRemark("未通过审核用户");
            log.create();
            return jReturn.toString();
        }

        String serverName = request.getServerName();
        ServerIPPriv sip = new ServerIPPriv(serverName);
        if (!sip.canUserLogin(ud.getName())) {
            jReturn.put("res", 0);
            jReturn.put("msg", "禁止登录！");
            jResult.put("returnCode", HomeController.RETURNCODE_NOT_VALID_USER);
            jReturn.put("result", jResult);

            LogDb log = new LogDb();
            log.setUserName(ud.getName());
            log.setType(LogDb.TYPE_LOGIN);
            log.setDevice(LogDb.DEVICE_MOBILE);
            log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
            log.setIp(StrUtil.getIp(request));
            log.setUnitCode(ud.getUnitCode());
            log.setRemark("禁止登录");
            log.create();

            return jReturn.toString();
        }

        // 许可证校验
        try {
            License.getInstance().validate(request);
        } catch (ErrMsgException e) {
            // LogUtil.getLog(getClass()).error(e);
            jReturn.put("res", "-1");
            jReturn.put("msg", e.getMessage());
            jResult.put("returnCode", "");
            jReturn.put("result", jResult);
            return jReturn.toString();
        }

        // 手机端绑定硬件标识
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean is_bind_mobile = cfg.getBooleanProperty("is_bind_mobile");
        if (is_bind_mobile) {
            if (deviceId != null && !deviceId.trim().equals("")) {
                UserSetupMgr userSetupMgr = new UserSetupMgr();
                boolean isAllowBindMobile = userSetupMgr.isBindMobileModify(ud.getName());
                UserMobileMgr userMobileMgr = new UserMobileMgr();
                if (isAllowBindMobile) {//是否允许绑定硬件标识
                    boolean isBindMobile = userMobileMgr.isBindMobileModify(ud.getName());//是否绑定硬件标识
                    if (!isBindMobile) {//未绑定 ，插入绑定
                        boolean result = userMobileMgr.create(ud.getName(), deviceId, client, 1);
                        if (!result) {
                            jReturn.put("res", -1);
                            jReturn.put("msg", "绑定手机失败！");
                            jResult.put("returnCode", "");
                            jReturn.put("result", jResult);
                            return jReturn.toString();
                        }
                    } else {
                        boolean isExistBindRecord = userMobileMgr.isExistBindRecord(ud.getName(), deviceId);
                        if (!isExistBindRecord) {
                            jReturn.put("res", 0);
                            jReturn.put("msg", "登陆手机与绑定手机不一致！");
                            jResult.put("returnCode", HomeController.RETURNCODE_INCONSISTENT_BIND);
                            jReturn.put("result", jResult);
                            return jReturn.toString();
                        }
                    }
                }
            }
        }

        // 记录登录日志
        LogDb log = new LogDb();
        log.setUserName(ud.getName());
        log.setType(LogDb.TYPE_LOGIN);
        log.setDevice(LogDb.DEVICE_MOBILE);
        log.setAction(com.redmoon.oa.LogUtil.get(request, "action_login"));
        log.setIp(StrUtil.getIp(request));
        log.setUnitCode(ud.getUnitCode());
        log.setRemark("");
        log.create();

        String enterpriseNum = License.getInstance().getEnterpriseNum();
        int hasMobile = ("".equals(ud.getMobile()) || null == ud.getMobile()) ? 0 : 1;

        jReturn.put("res", 0);
        jReturn.put("msg", "登录成功");
        jResult.put("returnCode", HomeController.RETURNCODE_LOGIN_SUCCESS);

        // 小程序中的web-view需要用到skey
        String skey = mobileService.generateSkey(ud.getName());
        jResult.put("skey", skey);
        response.setHeader(com.cloudweb.oa.utils.ConstUtil.SKEY, skey);

        String authToken = jwtUtil.generate(ud.getName());
        response.setHeader(jwtProperties.getHeader(), authToken);

        // 绑定openId
        if (StringUtils.isNotEmpty(openId)) {
            // 检查是否曾绑定过
            User myUser = userCache.getUser(ud.getName());
            com.redmoon.weixin.Config wxCfg = com.redmoon.weixin.Config.getInstance();
            int wxMiniLoginMode = StrUtil.toInt(wxCfg.getProperty("wxMiniLoginMode"), com.redmoon.weixin.Config.LOGIN_MODE_OPENID);
            if (wxMiniLoginMode == com.redmoon.weixin.Config.LOGIN_MODE_OPENID) {
                myUser.setOpenId(openId);
            }
            else {
                myUser.setUnionId(openId);
            }
            userService.updateByUserName(myUser);
        }

        // 如果角色可切换，则取默认角色
        boolean isRoleSwitchable = cfg.getBooleanProperty("isRoleSwitchable");
        jResult.put("isRoleSwitchable", isRoleSwitchable);
        String curRoleCode = Privilege.getDefaultCurRoleCode(ud.getName());
        if (isRoleSwitchable) {
            // 置当前切换角色
            if (curRoleCode != null) {
                // 置当前默认curRoleCode，以便于在取当前curDeptRole时，通过curRoleCode去取值
                Privilege.setCurRoleCode(curRoleCode);
                response.setHeader(com.cloudweb.oa.utils.ConstUtil.CUR_ROLE_CODE, curRoleCode);
            }

            // 取出所有的角色
            List<Role> roleList = userCache.getRoles(ud.getName());
            JSONArray ary = new JSONArray();
            for (Role role : roleList) {
                JSONObject json = new JSONObject();
                json.put("code", role.getCode());
                json.put("name", role.getDescription());
                ary.add(json);
            }
            jResult.put("roleList", ary);
        }

        if (cfg.getBooleanProperty("isDeptSwitchable")) {
            jResult.put("isDeptSwitchable", true);
            // 置当前切换部门
            String curDeptCode = Privilege.getDefaultCurDeptCode(ud.getName());
            if (curDeptCode!=null) {
                Privilege.setCurDeptCode(curDeptCode);
                response.setHeader(com.cloudweb.oa.utils.ConstUtil.CUR_DEPT_CODE, curDeptCode);
            }

            List<Department> deptList = departmentService.getDeptsOfUser(ud.getName());
            if (isRoleSwitchable) {
                // 取出与curRoleCode对应的部门
                if (curRoleCode != null) {
                    IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
                    // 取得第一个角色与当前部门均对应的记录作为默认的当前部门
                    deptList.removeIf(department -> !userOfRoleService.isRoleOfDept(ud.getName(), curRoleCode, department.getCode()));
                }
            }
            JSONArray ary = new JSONArray();
            for (Department dept : deptList) {
                JSONObject json = new JSONObject();
                json.put("code", dept.getCode());
                json.put("name", dept.getName());
                ary.add(json);
            }
            jResult.put("deptList", ary);
        }
        else {
            jResult.put("isDeptSwitchable", false);
        }

        jResult.put("realName", ud.getRealName()); // 用于 5+ 手机端

        String photo;
        if (!"".equals(ud.getPhoto())) {
            photo = "showImg.do?path=" + ud.getPhoto();
        } else {
            if (ud.getGender() == 0) {
                photo = "images/man.png";
            } else {
                photo = "images/woman.png";
            }
        }
        jResult.put("photo", photo);

        // 保存个推cid
        String cid = ParamUtil.get(request, "cid");
        DebugUtil.i(getClass(), ud.getRealName() + " passAndLogin cid", cid);
        UserSetup userSetup = userSetupService.getUserSetup(ud.getName());
        userSetup.setCid(cid);
        userSetupService.updateByUserName(userSetup);

        License lic = License.getInstance();
        String liscenseType = lic.getType();
        if (lic.isSrc()) {
            // 如果是开发版，则发至手机端TYPE_BIZ，即收费版，以免显示为免费版
            liscenseType = License.TYPE_BIZ;
        }
        // 3.0以后，免费版改为了标准版，所以传biz，以免显示为免费版
        double ver = StrUtil.toDouble(License.getInstance().getVersion(), 1.0);
        if (ver > 2) {
            liscenseType = License.TYPE_BIZ;
        }
        jResult.put("liscenseType", liscenseType);
        jResult.put("enterpriseNum", enterpriseNum);
        jResult.put("hasMobile", hasMobile);

        CloudConfig cloudConfig = CloudConfig.getInstance();
        int photoMaxSize = cloudConfig.getIntProperty("photoMaxSize");
        jResult.put("photoMaxSize", photoMaxSize);
        int intPhotoQuality = cloudConfig.getIntProperty("photoQuality");
        jResult.put("photoQuality", intPhotoQuality);

        // 是否可以通过邮箱重置密码
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        boolean isPwdCanReset = scfg.getIntProperty("isPwdCanReset") == 1;
        jResult.put("isPwdCanReset", isPwdCanReset);

        jReturn.put("result", jResult);
        return jReturn.toString();
    }
}
