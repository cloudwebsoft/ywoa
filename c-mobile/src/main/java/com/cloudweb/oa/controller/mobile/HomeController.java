package com.cloudweb.oa.controller.mobile;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import com.cloudweb.oa.config.JwtProperties;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.HomeService;
import com.cloudweb.oa.service.IMobileService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.CommonConstant;
import com.cloudweb.oa.vo.Result;
import com.cloudweb.oa.weixin.WxUserService;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.service.user.UserService;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.SystemUpDb;
import com.redmoon.oa.android.system.MobileAppIconConfigMgr;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.Directory;
import com.redmoon.oa.flow.DirectoryView;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.map.LocationDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.mgr.WXServiceMgr;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Vector;

@Slf4j
@RestController
@RequestMapping("/mobile")
public class HomeController {
    public static int RETURNCODE_LOGIN_SUCCESS = 0;       // 登录成功
    public static int RETURNCODE_ERROR_PASSWORD = 1;      // 用户名密码错误
    public static int RETURNCODE_INCONSISTENT_BIND = 2;   // 与绑定手机不一致
    public static int RETURNCODE_USER_NOT_EXIST = 3;      // 用户不存在
    public static int RETURNCODE_NOT_VALID_USER = 4;      // 非法用户
    public static int RETURNCODE_NOT_EXAMINE = 5;         // 未审核用户
    public static int RETURNCODE_NOT_PASS_EXAMINE = 6;    // 未通过审核用户
    public static int RETURNCODE_SUCCESS_NULL = -1;       // 获取成功，但无数据

    public static int RETURNCODE_OPENID_NONE = -100;        // openId配对失败
    public static int RETURNCODE_OPENID_GET_FAIL = -99;        // openId获取失败
    public static int RETURNCODE_UNIONID_GET_FAIL = -98;        // unionId获取失败
    public static int RETURNCODE_REQUIRE_USERINFO = -97;    // 默认小程序端发送code以登录

    public static int RETURNCODE_SUCCESS = 0;

    public static int RETURNCODE_REGIST_NOT_EXAMINE = 0;       //没审核
    public static int RETURNCODE_REGIST_PASS = 1;              //审核通过
    public static int RETURNCODE_REGIST_NOT_PASS= 2;           //审核不通过（用户不存在）

    public static int RETURNCODE_ADD_SUCCESS = 1;              //新增token成功
    public static int RETURNCODE_MODIFY_SUCCESS= 2;            //更新token成功
    public static int RETURNCODE_CLEAR_SUCCESS = 1;            //清空token成功

    public static int RESULT_TIME_OUT = -2;//时间过期
    public static int RESULT_SUCCESS = 0;//请求成功
    public static int RESULT_NO_DATA = -1;//列表无数据
    public static int RESULT_FORMCODE_ERROR = -3;//未传formcode
    public static int RESULT_INSERT_FAIL = -4;//插入失败
    public static int RESULT_ID_NULL = -5;//详细信息ID为空
    public static int RESULT_DATE_ISEXISTS = -7;//已新增
    public static int RESULT_MODULE_ERROR = -8;//未传formcode\
    public static int RESULT_SKEY_ERROR = -9;//skey不存在
    public static int RESULT_SERVER_ERROR = -10;//服务器异常

    public static final String SUCCESS = "SUCCESS";
    public static final String RES = "res";
    public static final String RETURNCODE = "returnCode";
    public static final String RESULT = "result";
    public static final String DATA = "data";

    public static int RES_SUCCESS = 0;                      //成功
    public static int RES_FAIL = -1;                        //失败
    public static int RES_EXPIRED = -2;                     //SKEY过期
    public static int RES_EXPIRED_TOKEN = -1000;               //jwt token过期，且header中无skey，无法重新生成token

    @Autowired
    HttpServletRequest request;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    IMobileService mobileService;

    @Autowired
    HomeService homeService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    WxUserService wxUserService;

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
     * 获取小程序的openId
     * @param code
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getMiniOpenId", produces = {"application/json;charset=UTF-8;"})
    public String getMiniOpenId(@RequestParam(defaultValue = "", required = true) String code, String encryptedData, String iv) {
        JSONObject jReturn = new JSONObject();
        try {
            WXServiceMgr wxServiceMgr = new WXServiceMgr();
            com.alibaba.fastjson.JSONObject jResult = wxServiceMgr.getMiniLoginInfo(code, encryptedData, iv);
            jReturn.put("result", jResult);
            if (jResult.getIntValue("errCode") == 0) {
                jReturn.put("res", RES_SUCCESS);
            }
            else {
                jReturn.put("res", RES_FAIL);
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return jReturn.toString();
    }

    /**
     * 获取微信公众号openId
     * @param code
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getOpenId", produces = {"application/json;charset=UTF-8;"})
    public String getOpenId(@RequestParam(defaultValue = "", required = true) String code) {
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();
        try {
            WXServiceMgr wxServiceMgr = new WXServiceMgr();
            String openId = wxServiceMgr.getOpenId(code);

            if("".equals(openId)){
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
            }else{
                jResult.put("returnCode", RETURNCODE_SUCCESS);
                jResult.put("openId", openId);
            }
            jReturn.put("result", jResult);
            jReturn.put("res", RES_SUCCESS);
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return jReturn.toString();
    }

    @ApiOperation(value = "H5应用通过code登录企业微信", notes = "H5应用通过code登录企业微信，取得用户信息", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "企业微信端返回的code", dataType = "String"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/loginByCodeForWork", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String loginByCodeForWork(String code, String deviceId, String client, HttpServletResponse response) {
        String userName = wxUserService.getUserByCodeForWxWork(code);
        if (userName == null) {
            return responseUtil.getResJson(false, "用户不存在").toString();
        }
        return homeService.loginByLoginInfo(request, userName, deviceId, client, response);
    }

    @ApiOperation(value = "H5应用通过code登录钉钉", notes = "H5应用通过code登录钉钉，取得用户信息", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "钉钉端返回的code", dataType = "String"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/loginByCodeForDingDing", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String loginByCodeForDingDing(String code, String deviceId, String client, HttpServletResponse response) {
        String userName;
        UserService userService = new UserService();
        UserDb userDb = userService.getUserByAvoidLogin(code);
        if (userDb != null && userDb.isLoaded()) {
            userName = userDb.getName();
            String dingding = StrUtil.getNullStr(userDb.getDingding());
            if ("".equals(dingding)) {
                userDb.setDingding(code);
                userDb.save();
            }
        } else {
            return responseUtil.getResJson(false, "用户不存在").toString();
        }
        return homeService.loginByLoginInfo(request, userName, deviceId, client, response);
    }

    /**
     * 通过小程序code登录
     * @param code
     * @param deviceId
     * @param client
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/loginByCode", produces = {"application/json;charset=UTF-8;"})
    public String loginByCode(@RequestParam(defaultValue = "", required = true) String code, String deviceId, String client, HttpServletResponse response) {
        com.alibaba.fastjson.JSONObject jReturn = new com.alibaba.fastjson.JSONObject();
        WXServiceMgr wxServiceMgr = new WXServiceMgr();
        String encryptedData = ParamUtil.get(request, "encryptedData");
        String iv = ParamUtil.get(request, "iv");
        com.alibaba.fastjson.JSONObject json = wxServiceMgr.getMiniLoginInfo(code, encryptedData, iv);

        Config cfg = Config.getInstance();
        int wxMiniLoginMode = StrUtil.toInt(cfg.getProperty("wxMiniLoginMode"), Config.LOGIN_MODE_OPENID);
        jReturn.put("wxMiniLoginMode", wxMiniLoginMode);

        // PC端登录模式，当为1或2时允许扫码登录
        int loginMode = StrUtil.toInt(com.redmoon.oa.Config.getInstance().get("loginMode"), 0);
        jReturn.put("loginMode", loginMode);

        // 采用openid登录及帐户登录时，因帐户登录也是与openid绑定
        if (wxMiniLoginMode == Config.LOGIN_MODE_OPENID || wxMiniLoginMode == Config.LOGIN_MODE_ACCOUNT) {
            if (!json.containsKey("openId")) {
                jReturn.put(ConstUtil.RES, RETURNCODE_OPENID_GET_FAIL);
                jReturn.put(ConstUtil.MSG, "获取openid失败");
            } else {
                return homeService.loginByMiniLoginInfo(request, json, deviceId, client, response);
            }
        }
        else if (wxMiniLoginMode == Config.LOGIN_MODE_UNIONID) {
            if (StrUtil.isEmpty(encryptedData)) {
                jReturn.put(ConstUtil.RES, RETURNCODE_REQUIRE_USERINFO);
                jReturn.put(ConstUtil.MSG, "需要 getUserInfo");
            }
            else {
                if (!json.containsKey("unionId")) {
                    jReturn.put(ConstUtil.RES, RETURNCODE_UNIONID_GET_FAIL);
                    jReturn.put(ConstUtil.MSG, "获取unionId失败");
                } else {
                    return homeService.loginByMiniLoginInfo(request, json, deviceId, client, response);
                }
            }
        }
        return jReturn.toString();
    }

    /**
     * 对小程序端的解密以获取用户手机号码等信息
     * @param encrypdata
     * @param ivdata
     * @param sessionkey
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "decrptMini", method = RequestMethod.GET)
    public @ResponseBody String decrptMini(String encrypdata,
                                            String ivdata, String sessionkey) {
        byte[] encrypData = Base64.getDecoder().decode(encrypdata);
        byte[] ivData = Base64.getDecoder().decode(ivdata);
        byte[] sessionKey = Base64.getDecoder().decode(sessionkey);
        String str="";
        try {
            str = decrypt(sessionKey,ivData,encrypData);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return str;
    }

    public static String decrypt(byte[] key, byte[] iv, byte[] encData) throws Exception {
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        //解析解密后的字符串
        return new String(cipher.doFinal(encData), StandardCharsets.UTF_8);
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
    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8;"})
    public String login(
            @RequestParam String name,
            @RequestParam String password,
            String deviceId,
            String client,
            String openId,
            HttpServletResponse response
    ) {
        return homeService.login(request, name, password, deviceId, client, openId, response);
    }

    @ResponseBody
    @RequestMapping(value = "/tokenExpired", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> tokenExpired() {
        Result<Object> result = new Result<>(false, "会话已过期，请重新登录");
        result.setCode(CommonConstant.SC_UN_AUTHZ);
        return result;
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
        } catch (NumberFormatException e) {
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
    @RequestMapping(value = "/getAppIcons", produces = {"application/json;charset=UTF-8;"})
    public String getAppIcons() {
        boolean flag = true;
        JSONArray jArray = new JSONArray();
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

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
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
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
                rd = (ResultRecord) ri.next();

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
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
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
}
