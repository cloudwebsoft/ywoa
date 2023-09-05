package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.*;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.bean.Address;
import com.cloudweb.oa.cache.DepartmentCache;
import com.cloudweb.oa.cache.DeptUserCache;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.AddressService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.android.CloudConfig;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.SystemUpDb;
import com.redmoon.oa.android.system.MobileAppIconConfigMgr;
import com.redmoon.oa.android.tools.Des3;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.map.LocationDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.person.UserSetupMgr;
import com.redmoon.oa.security.ServerIPPriv;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.usermobile.UserMobileMgr;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.weixin.bean.SortModel;
import com.redmoon.weixin.util.CharacterParser;
import com.redmoon.weixin.util.PinyinComparator;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/public/android")
public class MobileController {
    private static int RETURNCODE_LOGIN_SUCCESS = 0;       // 登录成功
    private static int RETURNCODE_ERROR_PASSWORD = 1;      // 用户名密码错误
    private static int RETURNCODE_INCONSISTENT_BIND = 2;   // 与绑定手机不一致
    private static int RETURNCODE_USER_NOT_EXIST = 3;      // 用户不存在
    private static int RETURNCODE_NOT_VALID_USER = 4;      // 非法用户
    private static int RETURNCODE_NOT_EXAMINE = 5;         // 未审核用户
    private static int RETURNCODE_NOT_PASS_EXAMINE = 6;    // 未通过审核用户
    private static int RETURNCODE_SUCCESS_NULL = -1;       // 获取成功，但无数据

    public static int RETURNCODE_SUCCESS = 0;

    private static int RETURNCODE_REGIST_NOT_EXAMINE = 0;       //没审核
    private static int RETURNCODE_REGIST_PASS = 1;              //审核通过
    private static int RETURNCODE_REGIST_NOT_PASS= 2;           //审核不通过（用户不存在）

    private static int RETURNCODE_ADD_SUCCESS = 1;              //新增token成功
    private static int RETURNCODE_MODIFY_SUCCESS= 2;            //更新token成功
    private static int RETURNCODE_CLEAR_SUCCESS = 1;            //清空token成功

    private static int RESULT_TIME_OUT = -2;//时间过期
    private static int RESULT_SUCCESS = 0;//请求成功
    private static int RESULT_NO_DATA = -1;//列表无数据
    private static int RESULT_FORMCODE_ERROR = -3;//未传formcode
    private static int RESULT_INSERT_FAIL = -4;//插入失败
    private static int RESULT_ID_NULL = -5;//详细信息ID为空
    private static int RESULT_DATE_ISEXISTS = -7;//已新增
    private static int RESULT_MODULE_ERROR = -8;//未传formcode\
    private static int RESULT_SKEY_ERROR = -9;//skey不存在
    private static int RESULT_SERVER_ERROR = -10;//服务器异常

    public static final String SUCCESS = "SUCCESS";
    public static final String RES = "res";
    public static final String RETURNCODE = "returnCode";
    public static final String RESULT = "result";
    public static final String DATA = "data";

    private static int RES_SUCCESS = 0;                      //成功
    private static int RES_FAIL = -1;                        //失败
    private static int RES_EXPIRED = -2;                     //SKEY过期

    @Autowired
    HttpServletRequest request;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    IUserService userService;

    @Autowired
    DeptUserCache deptUserCache;

    @Autowired
    DepartmentCache departmentCache;

    /**
     * 置信鸽推送 token
     * @param name
     * @param token
     * @param client
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/registrationApproval/setClientAndToken", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String setClientAndToken(
            @RequestParam(required = true) String name,
            String token,
            int client
    ) {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        boolean flag = true;

        UserDb ud = new UserDb();
        ud = ud.getUserDb(name);

        UserSetupDb ub = new UserSetupDb();
        ub = ub.getUserSetupDb(ud.getName());

        try {
            DebugUtil.i(getClass(), "execute", ud.getName() + " registerXGPush client=" + client + " token=" + token + " dbToken=" + ub.getToken());

            if (!token.equals(ub.getToken())) {
                ub.setClient(client);
                ub.setToken(token);
                ub.save();

                DebugUtil.i(getClass(), "execute", ud.getName() + " registerXGPush success " + client + " token=" + token);

                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_MODIFY_SUCCESS);
                jReturn.put("result", jResult);
            } else {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_MODIFY_SUCCESS);
                jReturn.put("result", jResult);
            }
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if(!flag) {
                try {
                    jReturn.put("res", RES_FAIL);
                    jResult.put("returnCode", "");
                    jReturn.put("result", jResult);
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }

        return jReturn.toString();
    }

    /**
     * 清除信鸽推送token
     * @param token
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/registrationApproval/installAndClearToken", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String installAndClearToken(
            @RequestParam(required = true) String token
    ) {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        boolean re = true;
        try {
            re = userSetupService.clearToken(token);
            if (re) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_CLEAR_SUCCESS);
                jReturn.put("result", jResult);
            }
        } catch (JSONException e) {
            re = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if(!re){
                try {
                    jReturn.put("res", RES_FAIL);
                    jResult.put("returnCode", "");
                    jReturn.put("result", jResult);
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        return jReturn.toString();
    }

    /**
     * 手机端登录
     * @param name
     * @param password
     * @param deviceId
     * @param client
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/registrationApproval/passAndLogin", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String passAndLogin(
            @RequestParam String name,
            @RequestParam String password,
            String deviceId,
            String client
    ) {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        boolean re = false;

        String decrypPassWord = password;
        String encryptType = ParamUtil.get(request, "encryptType");
        if (!"none".equals(encryptType)) {
            try {
                decrypPassWord = Des3.decode(password);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

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
                try {
                    jReturn.put("res", 0);
                    jReturn.put("msg", "用户名或密码错误");
                    jResult.put("returnCode", RETURNCODE_ERROR_PASSWORD);
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
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }

                return jReturn.toString();
            }
        }

        try {
            ud = ud.getUserDb(userName);
            if (ud == null || !ud.isLoaded()) {
                jReturn.put("res", 0);
                jReturn.put("msg", "用戶不存在");
                jResult.put("returnCode", RETURNCODE_USER_NOT_EXIST);
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
            if (!ud.isValid()) {
                jReturn.put("res", 0);
                jReturn.put("msg", "非法用戶");
                jResult.put("returnCode", RETURNCODE_NOT_VALID_USER);
                jReturn.put("result", jResult);

                LogDb log = new LogDb();
                log.setUserName(name);
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
                jResult.put("returnCode", RETURNCODE_NOT_EXAMINE);
                jReturn.put("result", jResult);

                LogDb log = new LogDb();
                log.setUserName(name);
                log.setType(LogDb.TYPE_LOGIN);
                log.setDevice(LogDb.DEVICE_MOBILE);
                log.setAction(com.redmoon.oa.LogUtil.get(request, "warn_login_fail"));
                log.setIp(StrUtil.getIp(request));
                log.setUnitCode(ud.getUnitCode());
                log.setRemark("未审核用户");
                log.create();
                return jReturn.toString();
            }
            else if (ud.getIsPass() == 2) {
                jReturn.put("res", 0);
                jReturn.put("msg", "未通过审核用户");
                jResult.put("returnCode", RETURNCODE_NOT_PASS_EXAMINE);
                jReturn.put("result", jResult);

                LogDb log = new LogDb();
                log.setUserName(name);
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
            // LogUtil.getLog(getClass()).info(getClass() + " serverName=" + serverName);
            ServerIPPriv sip = new ServerIPPriv(serverName);
            if (!sip.canUserLogin(ud.getName())) {
                jReturn.put("res", 0);
                jReturn.put("msg", "禁止登录！");
                jResult.put("returnCode", RETURNCODE_NOT_VALID_USER);
                jReturn.put("result", jResult);

                LogDb log = new LogDb();
                log.setUserName(name);
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
                if (deviceId != null && !"".equals(deviceId.trim())) {
                    UserSetupMgr userSetupMgr = new UserSetupMgr();
                    boolean isAllowBindMobile = userSetupMgr.isBindMobileModify(userName);
                    UserMobileMgr userMobileMgr = new UserMobileMgr();
                    if (isAllowBindMobile) {//是否允许绑定硬件标识
                        boolean isBindMobile = userMobileMgr.isBindMobileModify(userName);//是否绑定硬件标识
                        if (!isBindMobile) {//未绑定 ，插入绑定
                            boolean result = userMobileMgr.create(userName, deviceId, client, 1);
                            if (!result) {
                                jReturn.put("res", -1);
                                jReturn.put("msg", "绑定手机失败！");
                                jResult.put("returnCode", "");
                                jReturn.put("result", jResult);
                                return jReturn.toString();
                            }
                        } else {
                            boolean isExistBindRecord = userMobileMgr.isExistBindRecord(userName, deviceId);
                            if (!isExistBindRecord) {
                                jReturn.put("res", 0);
                                jReturn.put("msg", "登陆手机与绑定手机不一致！");
                                jResult.put("returnCode", RETURNCODE_INCONSISTENT_BIND);
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

            Date now = new Date();
            String skey = ud.getName() + "|" + "OA" + "|" + now.getTime();
            LogUtil.getLog(MobileController.class).info("skey=" + skey);

            CloudConfig cloudConfig = CloudConfig.getInstance();
            String key = cloudConfig.getProperty("key");

            String des = ThreeDesUtil.encrypt2hex(key, skey);
            LogUtil.getLog(MobileController.class).info("des=" + des);

            String enterpriseNum = License.getInstance().getEnterpriseNum();
            int hasMobile = ("".equals(ud.getMobile()) || null == ud.getMobile()) ? 0 : 1;

            jReturn.put("res", 0);
            jReturn.put("msg", "登录成功");
            jResult.put("returnCode", RETURNCODE_LOGIN_SUCCESS);
            jResult.put("skey", des);

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

            authUtil.doLoginByUserName(request, userName);

            // 保存个推cid
            String cid = ParamUtil.get(request, "cid");
            DebugUtil.i(getClass(), ud.getRealName() + " passAndLogin cid", cid);
            UserSetup userSetup = userSetupService.getUserSetup(userName);
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
            jReturn.put("result", jResult);

            int photoMaxSize = cloudConfig.getIntProperty("photoMaxSize");
            jResult.put("photoMaxSize", photoMaxSize);
            int intPhotoQuality = cloudConfig.getIntProperty("photoQuality");
            jResult.put("photoQuality", intPhotoQuality);

            // 是否可以通过邮箱重置密码
            com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
            boolean isPwdCanReset = scfg.getIntProperty("isPwdCanReset")==1;
            jResult.put("isPwdCanReset", isPwdCanReset);

            // 登录模式，1表示扫码登录
            int loginMode = StrUtil.toInt(com.redmoon.oa.Config.getInstance().get("loginMode"), 0);
            jResult.put("loginMode", loginMode);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/registrationApproval/liscenseByType", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String liscenseByType() throws JSONException {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        jReturn.put(RES, String.valueOf(RESULT_SUCCESS));
        jResult.put(RETURNCODE, RETURNCODE_SUCCESS);
        License license = License.getInstance();
        String type = license.getType();
        jResult.put(RETURNCODE, RETURNCODE_SUCCESS);
        jResult.put(DATA, type);
        jReturn.put(RESULT, jResult);
        return jReturn.toString();
    }

    /**
     * 手机端检测是否有更新
     * @param client
     * @param version
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/system/upgrade", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String upgrade(@RequestParam(defaultValue = "", required = true) String client, String version) {
        JSONObject json = new JSONObject();
        try {
            if (client == "") {
                client = "ios";
            }
            String sql = "select id from oa_version where client=? order by id desc";
            SystemUpDb sd = new SystemUpDb();
            Vector v = sd.list(sql, new Object[]{client});
            Iterator ir = v.iterator();
            String ver_sql = "";
            if (ir.hasNext()) {
                sd = (SystemUpDb) ir.next();
                ver_sql = sd.getString("version_num");
                if (ver_sql != null && !ver_sql.equals("")) {
                    if (getDoubleVersion(ver_sql) > getDoubleVersion(version)) {
                        json.put("res", "0");
                        json.put("msg", sd.getString("version_name"));
                        json.put("version", ver_sql);
                        String type = License.getInstance().getType();
                        String path = "";
                        if (type.equals(License.TYPE_OEM)) {
                            path = "activex/oa.apk";
                        } else {
                            path = "activex/yimioa.apk";
                        }
                        json.put("url", path);
                    } else {
                        json.put("res", "-1");
                        json.put("msg", "无更新");
                    }
                } else {
                    json.put("res", "-1");
                    json.put("msg", "无更新");
                }

            } else {
                json.put("res", "-1");
                json.put("msg", "无更新");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    public static double getDoubleVersion(String version) {
        double ver = 0.0;
        String versionRes = "";
        double total = 0;
        try {
            ver = Double.parseDouble(version);
        } catch (java.lang.NumberFormatException e) {
            versionRes = version;
        } finally {
            if (!versionRes.trim().equals("")) {
                version = version.replace(".", ",");
                String[] result = version.split(",");
                int len = result.length;
                if (len > 0) {
                    total = Double.valueOf(result[0]) + Double.valueOf(result[1]) * 0.1;
                    for (int i = 2; i < result.length; i++) {
                        double currentVal = Double.valueOf(result[i]);
                        double tempValue = currentVal * 0.01;
                        BigDecimal b1 = new BigDecimal(Double.toString(tempValue));
                        BigDecimal b2 = new BigDecimal(Double.toString(total));
                        total = b1.add(b2).doubleValue();
                    }
                }
            } else {
                total = ver;
            }
        }
        return total;
    }

    @ResponseBody
    @RequestMapping(value = "/system/getMobileAppIcon", produces = {"application/json;charset=UTF-8;"})
    public String getMobileAppIcon(@RequestParam(defaultValue = "", required = true) String skey) {
        boolean flag = true;
        JSONArray jArray = new JSONArray();
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);

        if (re) {
            try {
                jReturn.put("res", RES_EXPIRED);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        privilege.doLogin(request, skey);

        try {
            MobileAppIconConfigMgr appMgr = new MobileAppIconConfigMgr();
            jArray = appMgr.getAppIcons(request);
            if (jArray.length() == 0) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
            } else {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS);
                jResult.put("datas", jArray);
            }
            // 是否九宫格
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean isGrid = cfg.getBooleanProperty("isMobileGridView");
            jReturn.put("isGrid", isGrid);
            jReturn.put("result", jResult);
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (!flag) {
                try {
                    jReturn.put("res", RES_FAIL);
                    jResult.put("returnCode", "");
                    jReturn.put("result", jResult);
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/system/getMobileAppIconCanStart", produces = {"application/json;charset=UTF-8;"})
    public String getMobileAppIconCanStart(@RequestParam(defaultValue = "", required = true) String skey) {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);

        if(re){
            try {
                jReturn.put("res",RES_EXPIRED);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        privilege.doLogin(request, skey);
        com.redmoon.oa.pvg.Privilege pe = new com.redmoon.oa.pvg.Privilege();
        boolean canSendNotice = true;
        if(!pe.isUserPrivValid(request, "notice") && !pe.isUserPrivValid(request, "notice.dept")){
            canSendNotice = false;
        }
        try {
            MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
            JSONArray jArr = mr.getMobileCanStartInfo(request, canSendNotice);
            if(jArr.length()==0){
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
            }else{
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS);
                jResult.put("datas", jArr);
            }
            jReturn.put("result", jResult);
            jReturn.put("res", RES_SUCCESS);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/system/getMobileFlowIcon", produces = {"application/json;charset=UTF-8;"})
    public String getMobileFlowIcon(@RequestParam(defaultValue = "", required = true) String skey) {
        boolean flag = true;
        JSONArray jArray = new JSONArray();
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);

        if(re){
            try {
                jReturn.put("res",RES_EXPIRED);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        privilege.doLogin(request, skey);
        String userName = privilege.getUserName(skey);

        //取得有权限发起的流程
        Directory dir = new Directory();
        Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
        DirectoryView dv = new DirectoryView(rootlf);
        Vector children;
        try {
            children = dir.getChildren(Leaf.CODE_ROOT);
            Iterator ri = children.iterator();

            while (ri.hasNext()) {
                Leaf childlf = (Leaf) ri.next();
                if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
                    Iterator ir = dir.getChildren(childlf.getCode()).iterator();
                    while (ir.hasNext()) {
                        Leaf chlf = (Leaf) ir.next();
                        if (chlf.isOpen()  && dv.canUserSeeWhenInitFlow(request, chlf)) {
                            boolean mobileCanStart = false;
                            if(chlf.isMobileStart()){
                                if (chlf.getType() != Leaf.TYPE_NONE) {
                                    mobileCanStart = true;
                                }
                            }

                            if (mobileCanStart) {
                                JSONObject jObject = new JSONObject();
                                jObject.put("flowCode", chlf.getCode());
                                jObject.put("flowName", chlf.getName());
                                jObject.put("flowType", chlf.getType());

                                MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
                                String imgUrl = mr.getImgUrl(chlf.getCode(), 2);

                                jObject.put("imgUrl", imgUrl);
                                jArray.put(jObject);
                            }
                        }
                    }
                }
            }

            if(jArray.length()==0){
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
            }else{
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS);
                jResult.put("datas", jArray);
            }

            jReturn.put("result", jResult);
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally{
            if(!flag){
                try {
                    jReturn.put("res", RES_FAIL);
                    jResult.put("returnCode", "");
                    jReturn.put("result", jResult);
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/sms/send", produces = {"application/json;charset=UTF-8;"})
    public String sendSms(@RequestParam(defaultValue = "", required = true) String skey,
                       String mobile,
                       String content
    ) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if(re){
            try {
                json.put("res","-2");
                json.put("msg","时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        com.redmoon.oa.sms.Config cfg = new com.redmoon.oa.sms.Config();
        try {
            int realSendUserCount = 0;
            IMsgUtil imu = SMSFactory.getMsgUtil();
            String name = privilege.getUserName(skey);
            // long batch = SMSSendRecordDb.getBatchCanUse();
            // long batch = SMSSendBatchDb.getBatchCanUse(unitCode, name);
            String[] ary = StrUtil.split(mobile, ",");

            int length = ary.length;//发送短信手机号码数
            if (ary.length>0) {
                for (int i=0; i<length; i++) {
                    try {
                        boolean sendRet = imu.send(ary[i].trim(), content, name);
                        if (sendRet) {
                            realSendUserCount ++;
                        }
                    } catch (ErrMsgException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
                // String smsSign = cfg.getSign(unitCode);
                int count = cfg.getDivNumber(content.length());
                int realSendCount = realSendUserCount * count;
                json.put("res","0");
                json.put("msg","发送完毕，本次共发送短信"+realSendCount+"条");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/users/getlist", produces = {"application/json;charset=UTF-8;"})
    public String listUser(@RequestParam(defaultValue = "", required = true) String skey,
                           String op,
                           String what,
                           @RequestParam(defaultValue = "") String deptcode,
                           Integer pagenum,
                           Integer pagesize
    ) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            try {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        try {
            if(op!=null && !op.equals("")){
                if(op.equals("search")){
                    JSONArray usersArr = new JSONArray();
                    if(what!=null && !what.trim().equals("")){
                        String QUERY_LIST = "SELECT name FROM users  WHERE isValid=1 and realname like "+StrUtil.sqlstr("%"+what+"%");
                        UserDb userDb = new UserDb();
                        Vector v = userDb.list(QUERY_LIST);
                        if(v!=null && v.size()>0){
                            Iterator it = v.iterator();
                            while(it.hasNext()){
                                UserDb user = (UserDb)it.next();
                                String userName = user.getName();
                                JSONObject userObj = new JSONObject();
                                userObj.put("id", String.valueOf(user.getId()));
                                userObj.put("name", userName);
                                userObj.put("realName",user.getRealName());
                                DeptUserDb deptUser = new DeptUserDb(userName);
                                JSONObject deptObj = new JSONObject();
                                deptObj.put("dCode",deptUser.getDeptCode());
                                deptObj.put("dName", deptUser.getDeptName());
                                userObj.put("dept", deptObj);
                                usersArr.put(userObj);
                            }
                            json.put("res", "0");
                            json.put("msg", "操作成功");
                            JSONObject result = new JSONObject();
                            result.put("users", usersArr);
                            json.put("result", result);
                            return json.toString();
                        }

                    }

                }

            }else{
                String unitCode = "";
                if ("".equals(deptcode)) {
                    String userName = privilege.getUserName(skey);
                    UserDb ud = new UserDb();
                    ud = ud.getUserDb(userName);
                    unitCode = ud.getUnitCode();
                } else {
                    unitCode = deptcode;
                }

                DeptDb dd = new DeptDb();
                dd = dd.getDeptDb(unitCode);
                if (dd == null) {
                    json.put("res", "-1");
                    json.put("msg", "部门不存在");
                    return json.toString();
                }

                json.put("res", "0");
                json.put("msg", "操作成功");

                JSONObject result = new JSONObject();

                DeptUserDb jd = new DeptUserDb();

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
                String orderField = showByDeptSort ? "du.orders" : "u.orders";

                String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.dept_code="+StrUtil.sqlstr(unitCode)+" order by du.DEPT_CODE asc, " + orderField + " asc";

                int curpage = pagenum;
                ListResult lr = jd.listResult(sql, curpage, pagesize);
                long total = lr.getTotal();
                Vector v = lr.getResult();
                json.put("total", String.valueOf(total));
                result.put("count", String.valueOf(pagesize));
                Iterator ir = v.iterator();
                JSONArray users = new JSONArray();
                while (ir.hasNext()) {
                    DeptUserDb pu = (DeptUserDb) ir.next();
                    UserDb userDb = new UserDb();
                    JSONObject user = new JSONObject();
                    if (!pu.getUserName().equals("")) {
                        userDb = userDb.getUserDb(pu.getUserName());
                    }
                    user.put("id", String.valueOf(userDb.getId()));
                    user.put("name", userDb.getName());
                    user.put("realName", userDb.getRealName());
                    JSONObject deptObj = new JSONObject();
                    deptObj.put("dCode",pu.getDeptCode());
                    deptObj.put("dName", pu.getDeptName());
                    user.put("dept", deptObj);
                    users.put(user);
                }
                result.put("users", users);

                Vector v_c = dd.getChildren();
                ir = v_c.iterator();
                JSONArray childrens = new JSONArray();
                while (ir.hasNext()) {
                    DeptDb dd_c = (DeptDb) ir.next();
                    JSONObject children = new JSONObject();
                    children.put("deptName", dd_c.getName());
                    children.put("deptCode", dd_c.getCode());
                    childrens.put(children);
                }
                result.put("childrens", childrens);

                json.put("result", result);

            }

        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/location/getlist", produces = {"application/json;charset=UTF-8;"})
    public String listLocation(@RequestParam(defaultValue = "", required = true) String skey,
                               @RequestParam(defaultValue = "") String op,
                               @RequestParam(defaultValue = "") String what,
                               Integer pagenum,
                               Integer pagesize,
                               Integer flag) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if(re){
            try {
                json.put("res","-2");
                json.put("msg","时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        String userName = privilege.getUserName(skey) ;
        String sql = "select id from oa_location where user_name="+StrUtil.sqlstr(userName)+" and type = "+ flag;
        if ("search".equals(op)) {
            sql +=" and remark like " + StrUtil.sqlstr("%" + what + "%");
        }
        sql +=" order by create_date desc";

        int curpage = pagenum;   //第几页

        LocationDb wld = new LocationDb();
        try {
            ListResult lr = wld.listResult(sql, curpage, pagesize);
            long total = lr.getTotal();
            json.put("res","0");
            json.put("msg","操作成功");
            json.put("total",String.valueOf(total));
            Vector v = lr.getResult();
            Iterator ir = null;
            if (v!=null) {
                ir = v.iterator();
            }
            JSONObject result = new JSONObject();
            result.put("count",String.valueOf(pagesize));
            JSONArray wldArray  = new JSONArray();
            while (ir!=null && ir.hasNext()) {
                wld = (LocationDb)ir.next();
                JSONObject wlds = new JSONObject();
                wlds.put("id",String.valueOf(wld.getLong("id")));
                wlds.put("date", DateUtil.format(wld.getDate("create_date"), "yyyy-MM-dd"));
                wlds.put("address", StrUtil.getNullStr(wld.getString("address")));
                wlds.put("remark", wld.getString("remark"));
                wlds.put("fileSize", wld.getLong("file_size"));
                wlds.put("lontitude", wld.getDouble("lontitude"));
                wlds.put("latitude", wld.getDouble("latitude"));
                String diskName = StrUtil.getNullStr(wld.getString("file_path"));
                int p = diskName.lastIndexOf("/");
                if (p!=-1) {
                    diskName = diskName.substring(p+1);
                }
                wlds.put("diskName", diskName);

                wldArray.put(wlds);
            }
            result.put("locations",wldArray);
            json.put("result",result);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/general/getRecentlyPerson", produces = {"application/json;charset=UTF-8;"})
    public String getRecentlyPerson(@RequestParam(defaultValue = "", required = true) String skey) {
        JSONArray jArray = new JSONArray();
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);

        if(re){
            try {
                jReturn.put("res",RES_EXPIRED);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        privilege.doLogin(request, skey);

        //取得12个使用过人员的姓名
        String sql = "select distinct userName from user_recently_selected where name=? order by times desc";

        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        ResultRecord rd = null;

        try {
            ri = jt.executeQuery(sql,new Object[]{ privilege.getUserName(skey) }, 1, 12);

            if(!ri.hasNext()){
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }

            DeptUserDb dub = new DeptUserDb();
            UserDb ub = new UserDb();
            DeptDb db = new DeptDb();
            while (ri.hasNext()) {
                rd = ri.next();

                String userName = rd.getString("userName");
                ub = ub.getUserDb(userName);
                String realName = StrUtil.getNullString(ub.getRealName());
                String headUrl = "showImg.do?path=" + StrUtil.getNullString(ub.getPhoto());
                String mobile = StrUtil.getNullString(ub.getMobile());
                String deptName = "";

                Vector vr = dub.getDeptsOfUser(userName);
                Iterator ir = vr.iterator();
                if(ir.hasNext()){
                    db = (DeptDb)ir.next();
                    deptName = StrUtil.getNullString(db.getName());
                }

                JSONObject jObject = new JSONObject();
                jObject.put("userName", userName);
                jObject.put("realName", realName);
                jObject.put("headUrl", headUrl);
                jObject.put("mobile", mobile);
                jObject.put("deptName", deptName);
                jArray.put(jObject);
            }

            jReturn.put("res", RES_SUCCESS);
            jResult.put("returnCode", RETURNCODE_SUCCESS);
            jResult.put("datas", jArray);
            jReturn.put("result", jResult);
        } catch (Exception e) {
            try {
                jReturn.put("res", RES_FAIL);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);

                return jReturn.toString();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            jt.close();
        }

        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/general/getAllPerson", produces = {"application/json;charset=UTF-8;"})
    public String getAllPerson(@RequestParam(defaultValue = "", required = true) String skey) {
        JSONArray jArray = new JSONArray();
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);

        if(re){
            try {
                jReturn.put("res",RES_EXPIRED);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        privilege.doLogin(request, skey);

        //取得所有人员
        String sql = "select name,realName,photo,mobile from users where isValid=1 and isPass=1 order by id asc ";

        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        ResultRecord rd = null;

        try {
            ri = jt.executeQuery(sql);

            if(!ri.hasNext()){
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }

            DeptUserDb dub = new DeptUserDb();
            DeptDb db;
            while (ri.hasNext()) {
                rd = (ResultRecord) ri.next();

                String userName = rd.getString("name");
                String realName = rd.getString("realName");
                String headUrl = rd.getString("photo");
                String mobile = rd.getString("mobile");
                String deptName = "";
                Vector vr = dub.getDeptsOfUser(userName);
                Iterator ir = vr.iterator();
                if(ir.hasNext()){
                    db = (DeptDb)ir.next();
                    deptName = StrUtil.getNullString(db.getName());
                }

                JSONObject jObject = new JSONObject();
                jObject.put("userName", userName);
                jObject.put("realName", realName);
                jObject.put("headUrl", headUrl);
                jObject.put("mobile", mobile);
                jObject.put("deptName", deptName);
                jArray.put(jObject);
            }

            jReturn.put("res", RES_SUCCESS);
            jResult.put("returnCode", RETURNCODE_SUCCESS);
            jResult.put("datas", jArray);
            jReturn.put("result", jResult);
        } catch (Exception e) {
            try {
                jReturn.put("res", RES_FAIL);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            LogUtil.getLog(getClass()).error(e);
        } finally {
            jt.close();
        }

        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/general/getDepartment", produces = {"application/json;charset=UTF-8;"})
    public String getDepartment(@RequestParam(defaultValue = "", required = true) String skey, String deptCode) throws JSONException {
        JSONArray jArray = new JSONArray();
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);

        if(re){
            try {
                jReturn.put("res",RES_EXPIRED);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        privilege.doLogin(request, skey);

        String sql = "select * from department where is_show=1 and parentCode=? order by orders";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        ResultRecord rd = null;
        try {
            ri = jt.executeQuery(sql,new Object[]{ deptCode });
            if(!ri.hasNext()){
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }

            while (ri.hasNext()) {
                rd = (ResultRecord) ri.next();

                String deptCodeReturn = rd.getString("code");
                String deptName = rd.getString("name");
                String parentCode = rd.getString("parentCode");
                boolean hasChild = (rd.getInt("childCount")==0) ? false : true;

                JSONObject jObject = new JSONObject();
                jObject.put("deptcode", deptCodeReturn);
                jObject.put("deptName", deptName);
                jObject.put("parentCode", parentCode);
                jObject.put("hasChild", hasChild);
                jArray.put(jObject);
            }

            jReturn.put("res", RES_SUCCESS);
            jResult.put("returnCode", RETURNCODE_SUCCESS);
            jResult.put("datas", jArray);
            jReturn.put("result", jResult);
        } catch (SQLException e) {
            try {
                jReturn.put("res", RES_FAIL);
                jResult.put("returnCode", "");
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            LogUtil.getLog(getClass()).error(e);
            log.error(StrUtil.trace(e));
        } finally {
            jt.close();
        }

        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/registrationApproval/checkRegistStatus", produces = {"application/json;charset=UTF-8;"})
    public String checkRegistStatus(
            @RequestParam(required = true) String name
    ) {
        boolean re = false;
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        String sql = "select isPass from users where name=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        ResultRecord rd = null;

        UserDb ud = new UserDb(name);

        int isPass = -1;
        try {
            ri = jt.executeQuery(sql, new Object[] { ud.getName() });
            if (ri.hasNext()) {
                rd = (ResultRecord) ri.next();
                isPass = rd.getInt("isPass");
                re = true;
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            jt.close();
        }

        try {
            if (re) {
                jReturn.put("res", 0);
                if (isPass == 0) {
                    jReturn.put("msg", "未审核");
                    jResult.put("returnCode", RETURNCODE_REGIST_NOT_EXAMINE);
                } else if (isPass == 1) {
                    jReturn.put("msg", "通过");
                    jResult.put("returnCode", RETURNCODE_REGIST_PASS);
                }
                jReturn.put("result", jResult);
            } else {
                jReturn.put("res", 0);
                jReturn.put("msg", "不通过");
                jResult.put("returnCode", RETURNCODE_REGIST_NOT_PASS);
                jReturn.put("result", jResult);
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/registrationApproval/updateCid", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String updateCid(
            @RequestParam(required = true) String skey,
            String cid
    ) {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        boolean flag = true;

        Privilege privilege = new Privilege();
        String userName = privilege.getUserName(skey);
        UserSetup userSetup = userSetupService.getUserSetup(userName);
        try {
            if (!cid.equals(userSetup.getCid())) {
                userSetup.setCid(cid);
                boolean re = userSetupService.updateByUserName(userSetup);
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_MODIFY_SUCCESS);
                jReturn.put("result", jResult);
            } else {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_MODIFY_SUCCESS);
                jReturn.put("result", jResult);
            }
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if(!flag) {
                try {
                    jReturn.put("res", RES_FAIL);
                    jResult.put("returnCode", "");
                    jReturn.put("result", jResult);
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }

        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/getCounts", produces = {"application/json;charset=UTF-8;"})
    public String getCounts(@RequestParam(required = true) String skey) throws JSONException {
        Privilege privilege = new Privilege();
        String userName = privilege.getUserName(skey);

        MessageDb messageDb = new MessageDb();
        int unReadCount = messageDb.getNewMsgCount(privilege.getUserName(skey));

        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        json.put("unReadCount", unReadCount);
        json.put("waitCount", WorkflowDb.getWaitCount(userName));
        return json.toString();
    }

    @ApiOperation(value = "验证密码，用于签名宏控件", notes = "验证密码，用于签名宏控件", httpMethod = "POST")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/getBarcode", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> getBarcode(@RequestParam(required = true) String barcode) {
        Pattern pattern = Pattern.compile("([a-zA-Z_-]+)[0-9]*");
        Matcher matcher = pattern.matcher(barcode);
        if (!matcher.find()) {
            LogUtil.getLog(getClass()).warn("条形码: " + barcode + " 未找到其前缀");
            return new Result<>(false);
        }
        String prefix = matcher.group(1);
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

        FormDb fd = new FormDb();
        List<FormField> list = fd.listMacorFields("macro_barcode");
        for (FormField ff : list) {
            String desc = ff.getDescription();
            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(desc);
            // 找到二维码宏控件对应的FormField，因只能找到第一条记录，故需注意prefix不能有重复，一个表单对应一个，且唯一
            if (jsonObject.getString("prefix").equals(prefix)) {
                String numStr = barcode.substring(prefix.length());
                long num = StrUtil.toLong(numStr, 0);

                com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                List<FormDAO> listDao = fdao.selectList(ff.getFormCode(), "select id from " + FormDb.getTableName(ff.getFormCode()) + " where " + ff.getName() + "=" + num);
                if (listDao.size() > 0) {
                    fdao = listDao.get(0);
                    json.put("id", fdao.getId());
                    json.put("moduleCode", ff.getFormCode());
                    json.put("formCode", ff.getFormCode());
                    json.put("fieldName", ff.getName());
                    json.put("fieldValue", barcode);
                }
                break;
            }
        }
        return new Result<>(json);
    }

    public int getPositionForSection(int section, List<SortModel> sortModels) {
        for (int i = 0; i < sortModels.size(); i++) {
            String sortStr = sortModels.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @ApiOperation(value = "通讯录列表", notes = "通讯录列表")
    @ResponseBody
    @RequestMapping(value = "/user/listAddress", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String listAddress() {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.auth(request);
        if (!re) {
            try {
                json.put("res", "-1");
                // json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        com.alibaba.fastjson.JSONArray arr = new com.alibaba.fastjson.JSONArray();
        List<SortModel> sortModels = new ArrayList<SortModel>();
        com.alibaba.fastjson.JSONObject obj = new com.alibaba.fastjson.JSONObject();

        String what = ParamUtil.get(request, "what");

        User addr;
        List<User> list = userService.listForAddress(what);
        for (User user : list) {
            String person = user.getRealName();
            String pinyin = CharacterParser.getInstance().getSelling(person);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            SortModel sortUserModel = new SortModel();
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortUserModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortUserModel.setSortLetters("#");
            }
            sortUserModel.setObjs(user);
            sortModels.add(sortUserModel);
        }

        if (sortModels.size() > 0) {
            PinyinComparator pinyinComparator = new PinyinComparator();
            Collections.sort(sortModels, pinyinComparator);
            for (int i = 0; i < sortModels.size(); i++) {
                com.alibaba.fastjson.JSONObject itemObj = new com.alibaba.fastjson.JSONObject();
                int sec = sortModels.get(i).getSortLetters().charAt(0);
                addr = (User) sortModels.get(i).getObjs();
                com.alibaba.fastjson.JSONObject userObj = new com.alibaba.fastjson.JSONObject();
                userObj.put("person", addr.getRealName());
                userObj.put("mobile", addr.getMobile());
                List<DeptUser> listDeptUser = deptUserCache.listByUserName(addr.getName());
                StringBuilder deptNames = new StringBuilder();
                for (DeptUser du : listDeptUser) {
                    Department dept = departmentCache.getDepartment(du.getDeptCode());
                    String deptName = "不存在";
                    if (dept != null) {
                        if (!ConstUtil.DEPT_ROOT.equals(dept.getParentCode()) && !ConstUtil.DEPT_ROOT.equals(dept.getCode())) {
                            Department parentDept = departmentCache.getDepartment(dept.getParentCode());
                            if (parentDept != null) {
                                deptName = parentDept.getName() + dept.getName();
                            }
                        } else {
                            deptName = dept.getName();
                        }
                    }

                    StrUtil.concat(deptNames, "，", deptName);
                }

                if (listDeptUser.size() > 0) {
                    userObj.put("company", deptNames);
                } else {
                    userObj.put("company", "");
                }
                userObj.put("email", addr.getEmail());
                userObj.put("id", addr.getId());

                if (i == getPositionForSection(sec, sortModels)) {
                    itemObj.put("isGroup", true);
                    itemObj.put("name", sortModels.get(i).getSortLetters());
                    itemObj.put("pyName", sortModels.get(i).getSortLetters());
                    arr.add(itemObj);

                    com.alibaba.fastjson.JSONObject itemObj2 = new com.alibaba.fastjson.JSONObject();
                    itemObj2.put("isGroup", false);
                    itemObj2.put("name", addr.getRealName());
                    itemObj2.put("pyName", CharacterParser.getInstance().getSelling(addr.getRealName()));
                    itemObj2.put("user", userObj);
                    arr.add(itemObj2);
                } else {
                    itemObj.put("isGroup", false);
                    itemObj.put("name", addr.getRealName());
                    itemObj.put("pyName", CharacterParser.getInstance().getSelling(addr.getRealName()));
                    itemObj.put("user", userObj);
                    arr.add(itemObj);
                }
            }
        }
        if (arr.size() > 0) {
            obj.put("res", 0);
            obj.put("datas", arr);
        } else {
            obj.put("res", -1);
        }
        return obj.toString();
    }
}
