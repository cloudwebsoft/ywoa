package com.cloudweb.oa.controller;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IRoleService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.Base64Util;
import com.cloudweb.oa.utils.PasswordUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.Config;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.tools.Des3;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.verificationCode.HttpClientVerificationCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/public/android")
public class IController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    IUserService userService;

    public static int RETURNCODE_SUCCESS = 0;
    public static int RES_SUCCESS = 0;                      //成功
    public static int RES_FAIL = -1;                        //失败
    public static int RES_EXPIRED = -2;                     //SKEY过期

    private static int RETURNCODE_USER_NOT_EXIST = 1;        //帐号不存在
    private static int RETURNCODE_NOT_VALID_USER = 2;        //非法用户
    private static int RETURNCODE_ERORR_SEND_MSG = 3;        //短信发送失败
    private static int RETURNCODE_SUCCESS_SEND_MSG = 4;      //短信发送成功
    private static int RETURNCODE_EXPIRE_VERIFI = 5;         //验证码已过期
    private static int RETURNCODE_ERROR_VERIFI = 6;             //验证码错误
    private static int RETURNCODE_NOT_HAVE_MOBILE = 7;         //用户没有手机，请联系管理员

    private static int MESSAGE_PASS_50 = 8;                  //免费短消息超过50条
    private static int NOT_HAVE_ENTERPRISE_NO = 9;           //没有企业号，提示注册

    private static int TYPE_LOCAL = 0;                      //短信发送方，本地
    private static int TYPE_YIMIHOME = 1;                   //短信发送方，云端

    private static String INIT_PASSWORD = "123";             //初始密码
    private static int RETURNCODE_WRONG_PASSWORD = 1;        //原密码输入不正确
    private static int RETURNCODE_PASSWORD_NOT_VALID = 2;        // 密码格式非法
    private static int RETURNCODE_SUCCESS_NULL = -1;         //获取成功，但无数据
    private static int RETURNCODE_REGISTED_MOBILE = 2;       //手机号已经注册

    @Autowired
    IRoleService roleService;

    @Autowired
    UserCache userCache;

    @ResponseBody
    @RequestMapping(value = "/i/getInforList", produces = {"application/json;charset=UTF-8;"})
    public String getInforList(@RequestParam(required = true) String skey) throws JSONException {
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
        jReturn.put("res", RES_SUCCESS);

        jResult.put("returnCode", RETURNCODE_SUCCESS);
        jResult.put("datas", jArray);
        // 20180418 fgf 将消息从原来的聚合待办流程、通知公告改为新消息，包括系统消息和内部邮件
        MessageDb messageDb = new MessageDb();
        int unReadCount = messageDb.getNewMsgCount(privilege.getUserName(skey));

        jResult.put("unReadCount", unReadCount);

        jReturn.put("result", jResult);
        return jReturn.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/i/forgetPassword", produces = {"application/json;charset=UTF-8;"})
    public String forgetPassword(
            @RequestParam(required = true) String name,
            @RequestParam(required = true) int type,
            String verificationCode
    ) {
        boolean flag = true;
        JSONObject jSend = new JSONObject();
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        String enterpriseNo = License.getInstance().getEnterpriseNum();
        try {
            if ("".equals(enterpriseNo) || "yimi".equals(enterpriseNo)) {
                jReturn.put("res", 0);
                jReturn.put("msg", "请先注册企业号，才能使用短信验证功能。");
                jResult.put("verificationCode", "");
                jResult.put("returnCode", NOT_HAVE_ENTERPRISE_NO);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        UserDb ud = new UserDb(name);
        try {
            if (ud == null || !ud.isLoaded()) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_USER_NOT_EXIST);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }

            if (!ud.isValid()) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_NOT_VALID_USER);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }

            if ("".equals(ud.getMobile()) || null == ud.getMobile()) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_NOT_HAVE_MOBILE);
                jReturn.put("result", jResult);

                return jReturn.toString();
            }

            // 先验证验证码是否过期 ，根据type区分是本地发送的验证码 ，还是云端
            Config cg = new Config();
            String yimihomeURL = cg.get("yimihome_url");

            int result = 0; // 验证码有效
            if (type == TYPE_YIMIHOME) {
                HttpClientVerificationCode he = new HttpClientVerificationCode();

                jSend.put("name", ud.getMobile());
                jSend.put("verificationCode", verificationCode);

                jReturn = he.checkVerificationCode(yimihomeURL + "/httpClientServer/httpclient_server_check_verification_code.jsp", jSend);

                result = jReturn.getInt("result");
                jReturn.remove("remark");
            } else if (type == TYPE_LOCAL) {
/*                VerificationCodeMgr vr = new VerificationCodeMgr();
                result = vr.checkVerificationCodeValid(ud.getMobile(), verificationCode);*/
            }

            if (result == RETURNCODE_EXPIRE_VERIFI) {
                jReturn.put("res", 0);
                jReturn.put("msg", "验证码已过期");
                jResult.put("returnCode", RETURNCODE_EXPIRE_VERIFI);
                jReturn.put("result", jResult);
                return jReturn.toString();
            } else if (result == RETURNCODE_ERROR_VERIFI) {
                jReturn.put("res", 0);
                jReturn.put("msg", "验证码错误");
                jResult.put("returnCode", RETURNCODE_ERROR_VERIFI);
                jReturn.put("result", jResult);

                return jReturn.toString();
            }

            // 检查本地oa是否配置了短信服务。
            IMsgUtil imu = SMSFactory.getMsgUtil();

            result = 1;
            boolean re = true;
            if (imu == null) {
                // 向yimihome服务器发送获取验证码请求
/*                HttpClientGetInitializtionPassword hd = new HttpClientGetInitializtionPassword();

                jSend.put("mobile", ud.getMobile());
                jSend.put("enterpriseNo", enterpriseNo);
                jReturn = hd.getInitializtionPassword(yimihomeURL + "/httpClientServer/httpclient_server_send_password.jsp", jSend);
                result = jReturn.getInt("result");*/
            } else {
                re = SMSFactory.getMsgUtil().send(ud.getMobile(), "【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】初始密码：" + INIT_PASSWORD + "，请您登录后及时修改初始密码。", "");

                if (re) {
                    result = 0;
                    LogUtil.getLog(getClass()).info("-----手机" + ud.getMobile() + ",【" + com.redmoon.oa.Config.getInstance().get("enterprise") + "】初始密码已经发送-----");
                } else {
                    result = 1;
                }
            }

            //result=-1 手机号为空 ；result=0 短信成功发送 ；result=1 短信发送失败   ；result=2 免费短消息超过50条
            if (result == 1) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_ERORR_SEND_MSG);
                jReturn.put("result", jResult);
            } else if (result == 0) {   //发送短信成功后，更新原始密码
                String pwdMD5 = SecurityUtil.MD5(INIT_PASSWORD);

                ud.setPwdMD5(pwdMD5);
                ud.setPwdRaw(INIT_PASSWORD);
                re = ud.save();

                if (re) {
                    jReturn.put("res", RES_SUCCESS);
                    jResult.put("returnCode", RETURNCODE_SUCCESS);
                    jReturn.put("result", jResult);
                }
            } else if (result == 2) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", MESSAGE_PASS_50);
                jReturn.put("result", jResult);
            }
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (IOException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (Exception e) {
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
    @RequestMapping(value = "/i/resetPassword", produces = {"application/json;charset=UTF-8;"})
    public String resetPassword(
            @RequestParam(required = true) String skey,
            @RequestParam(defaultValue = "false", required = true) boolean wap,
            @RequestParam(required = true) String oldPassword,
            @RequestParam(required = true) String password
    ) {
        boolean flag = true;
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

        String decrypOldPassWord = "";
        if (!wap) {
            try {
                decrypOldPassWord = Des3.decode(oldPassword);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
        } else {
            decrypOldPassWord = oldPassword;
        }

        String name = privilege.getUserName(skey);
        UserDb ub = new UserDb();
        ub = ub.getUserDb(name);

        re = ub.Auth(name, decrypOldPassWord);

        if (!re) {
            try {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_WRONG_PASSWORD);
                jReturn.put("result", jResult);
                return jReturn.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        try {
            String decrypPassWord = "";
            if (!wap) {
                try {
                    decrypPassWord = Des3.decode(password);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            } else {
                decrypPassWord = password;

                com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
                int minLen = scfg.getIntProperty("password.minLen");
                int maxLen = scfg.getIntProperty("password.maxLen");
                int strenth = scfg.getIntProperty("password.strenth");

                PasswordUtil pu = new PasswordUtil();
                if (pu.check(decrypPassWord, minLen, maxLen, strenth) != 1) {
                    try {
                        jReturn.put("res", RES_FAIL);
                        jResult.put("returnCode", RETURNCODE_PASSWORD_NOT_VALID);
                        jReturn.put("result", jResult);
                        jReturn.put("msg", pu.getResultDesc(request));
                        return jReturn.toString();
                    } catch (JSONException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
            }

            String pwdRaw = decrypPassWord;

            String pwdMD5 = SecurityUtil.MD5(pwdRaw);

            ub.setPwdMD5(pwdMD5);
            // ub.setPwdRaw(pwdRaw);
            ub.setPwdRaw(userService.encryptPwd(pwdRaw));

            re = ub.save();

            if (re) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS);
                jReturn.put("result", jResult);
            }
        } catch (SQLException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (Exception e) {
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
    @RequestMapping(value = "/i/getPersonInfor", produces = {"application/json;charset=UTF-8;"})
    public String getPersonInfor(
            @RequestParam(required = true) String skey
    ) {
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

        try {
            String userName = privilege.getUserName(skey);

            UserDb ud = new UserDb(userName);
            ud = ud.getUserDb(userName);

            if (!ud.isLoaded()) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
                jReturn.put("result", jResult);
                return jReturn.toString();
            } else {
                String realName = ud.getRealName();
                String mobile = ud.getMobile();
                String birthday;
                java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
                Date de = ud.getBirthday();
                if (de == null) {
                    birthday = "";
                } else {
                    birthday = f.format(de);
                }

                String qq = ud.getQQ();
                String address = ud.getAddress();
                String deptName = "";

                DeptUserDb dub = new DeptUserDb();
                DeptDb db = new DeptDb();

                Vector vr = dub.getDeptsOfUser(userName);
                Iterator ir = vr.iterator();

                String temp = "";
                boolean fg = true;
                while (ir.hasNext()) {
                    db = (DeptDb) ir.next();
                    temp = StrUtil.getNullString(db.getName());

                    if ("".equals(temp)) {
                        continue;
                    }

                    if (fg) {
                        deptName = temp;
                        fg = false;
                    } else {
                        deptName = deptName + "," + temp;
                    }
                }

                String photo = ud.getPhoto();
                String gender = String.valueOf(ud.getGender());

                StringBuilder sb = new StringBuilder();
                List<Role> list = roleService.getAllRolesOfUser(userName, false);
                for (Role role : list) {
                    StrUtil.concat(sb, ",", role.getDescription());
                }
                String roleDesc = sb.toString();

                JSONObject jObject = new JSONObject();
                jObject.put("userName", userName);
                jObject.put("realName", realName);
                jObject.put("mobile", mobile);
                jObject.put("birthday", birthday);
                jObject.put("qq", qq);
                jObject.put("address", address);
                jObject.put("deptName", deptName);
                jObject.put("headUrl", photo);
                jObject.put("gender", gender);
                jObject.put("role", roleDesc);
                jArray.put(jObject);

                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS);
                jResult.put("datas", jArray);
                jReturn.put("result", jResult);
            }
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
    @RequestMapping(value = "/i/modifyPersonInfor", produces = {"application/json;charset=UTF-8;"})
    public String modifyPersonInfor(
            HttpServletRequest request
    ) {
        boolean flag = true;
        JSONObject jReturn = new JSONObject();
        JSONObject jResult = new JSONObject();

        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        /*if (!multipartResolver.isMultipart(request)) {
        }*/
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

        String skey = ParamUtil.get(request, "skey");
        String realName = ParamUtil.get(request, "realName");
        String birthday = ParamUtil.get(request, "birthday");
        String qq = ParamUtil.get(request, "qq");
        String address = ParamUtil.get(request, "address");
        int gender = ParamUtil.getInt(request, "gender", 0);
        String weixin = ParamUtil.get(request, "weixin");

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

        String name = privilege.getUserName(skey);
        UserDb ud = new UserDb();
        ud = ud.getUserDb(name);
        String photo = ud.getPhoto();

        try {
            ud.setRealName(realName);
            if (!"".equals(birthday)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(birthday);
                ud.setBirthday(date);
            }

            ud.setQQ(qq);
            ud.setAddress(address);
            ud.setGender(gender);
            // 只有 5+ 才会传入weixin
            if (!StringUtils.isEmpty(weixin)) {
                ud.setWeixin(weixin);
            }

            int i = 0;
            MultipartFile[] files = null;
            files = new MultipartFile[multiRequest.getFileMap().size()];
            Iterator<String> names = multiRequest.getFileNames();
            while (names.hasNext()) {
                MultipartFile file = multiRequest.getFile(names.next().toString());
                if (file != null) {
                    files[i] = file;
                    i++;
                }
            }

            if (files != null && files.length > 0) {
                // 循环获取file数组中得文件
                for (i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    if (!file.isEmpty()) {
                        // 如果上传了照片，则检查原来是否已有照片，如有则删除
                        File f = new File(Global.getRealPath() + ud.getPhoto());
                        if (!"".equals(ud.getPhoto()) && f.exists()) {
                            f.delete();
                        }

                        byte[] bytes = file.getBytes();
                        String fileName = file.getOriginalFilename(); // 带有完整路径
                        int p = fileName.lastIndexOf(File.separator);
                        if (p != -1) {
                            fileName = fileName.substring(p + 1);
                        }

                        String ext = StrUtil.getFileExt(fileName);
                        if (!StrUtil.isImage(ext)) {
                            flag = false;
                            jReturn.put("msg", "请选择图片");
                            break;
                        }
                        String diskName = FileUpload.getRandName() + "." + ext;
                        String vpath = "public/users/"; // 放在public目录下方便手机端获取

                        String filePath = Global.getRealPath() + vpath + "/" + diskName;

                        f = new File(filePath);
                        if (!f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(filePath));
                        stream.write(bytes);
                        stream.close();

                        photo = vpath + diskName;
                        ud.setPhoto(photo);
                    }
                }
            }

            if (flag) {
                re = ud.save();
                if (re) {
                    jReturn.put("res", RES_SUCCESS);
                    jResult.put("returnCode", RETURNCODE_SUCCESS);
                    jReturn.put("result", jResult);
                    jReturn.put("photo", photo);
                }
            }
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (Exception e) {
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
    @RequestMapping(value = "/i/changeMobile", produces = {"application/json;charset=UTF-8;"})
    public String changeMobile(
            @RequestParam(required = true) String skey,
            Integer type,
            String mobile,
            String verificationCode
    ) {
        boolean flag = true;
        JSONObject jSend = new JSONObject();
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
        UserDb ud = new UserDb(privilege.getUserName(skey));

        //先验证验证码是否过期 ，根据type区分是本地发送的验证码 ，还是云端
        try {
            int result = 0; //验证码有效

            if (type == TYPE_YIMIHOME) {
                Config cg = new Config();
                String yimihomeURL = cg.get("yimihome_url");

                HttpClientVerificationCode he = new HttpClientVerificationCode();

                jSend.put("name", mobile);
                jSend.put("verificationCode", verificationCode);

                jReturn = he.checkVerificationCode(yimihomeURL + "/httpClientServer/httpclient_server_check_verification_code.jsp", jSend);

                result = jReturn.getInt("result");
                jReturn.remove("remark");
            } else if (type == TYPE_LOCAL) {
/*                VerificationCodeMgr vr = new VerificationCodeMgr();
                result = vr.checkVerificationCodeValid(mobile, verificationCode);*/
            }

            if (result == RETURNCODE_EXPIRE_VERIFI) {
                jReturn.put("res", 0);
                jResult.put("returnCode", RETURNCODE_EXPIRE_VERIFI);
                jReturn.put("result", jResult);
                return jReturn.toString();
            } else if (result == RETURNCODE_ERROR_VERIFI) {
                jReturn.put("res", 0);
                jResult.put("returnCode", RETURNCODE_ERROR_VERIFI);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }

        try {
            // 检验新手机号否注册过
            String sql = "select mobile from users where mobile=?";
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = null;

            ri = jt.executeQuery(sql, new Object[]{mobile});
            if (ri.hasNext()) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_REGISTED_MOBILE);
                jReturn.put("result", jResult);
                return jReturn.toString();
            }

            ud.setMobile(mobile);
            ud.save();

            jReturn.put("res", RES_SUCCESS);
            jResult.put("returnCode", RETURNCODE_SUCCESS);
            jReturn.put("result", jResult);
        } catch (Exception e) {
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
    @RequestMapping(value = "/i/uploadHeadImage", produces = {"application/json;charset=UTF-8;"})
    public String uploadHeadImage(
            @RequestParam(required = true) String skey,
            @RequestParam(required = false) MultipartFile image
    ) {
        boolean flag = true;
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

        FileOutputStream fos = null;
        InputStream is = null;
        try {
            String imageSavePath = Global.getRealPath() + "public/users/";
            //LogUtil.getLog(getClass()).info(imageSavePath);

            File file = new File(imageSavePath);
            if (!file.exists()) {
                file.mkdir();
            }

            String now = System.currentTimeMillis() + "" ;
            String imageName = now.substring(4) + new Random().nextInt(10);

            String imageType = ".jpg" ;
            String imageFullPath = imageSavePath + imageName + imageType;
            String imageUrl = "public/users/" +  imageName + imageType;

            fos = new FileOutputStream(imageFullPath);
            is = image.getInputStream();

            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            is.close();

            String sql = "update users set photo=? where name=?";

            JdbcTemplate jt = new JdbcTemplate();
            int n = jt.executeUpdate(sql, new Object[] { imageUrl, privilege.getUserName(skey)});

            if (n == 1) {
                jReturn.put("res", RES_SUCCESS);
                jResult.put("returnCode", RETURNCODE_SUCCESS);
                jResult.put("imageUrl", imageUrl);
                jReturn.put("result", jResult);
            }
        } catch (FileNotFoundException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (IOException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (SQLException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (JSONException e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }catch (Exception e) {
            flag = false;
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).info("FileInputStream关闭失败");
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).info("FileOutputStream关闭失败");
                    LogUtil.getLog(getClass()).error(e);
                }
            }
            if(!flag){
                try {
                    jReturn.put("res", RES_FAIL);
                    jResult.put("returnCode", "");
                    jReturn.put("result", jResult);
                } catch (JSONException e) {
                    LogUtil.getLog(getClass()).error(e);
                    LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                }
            }
        }
        return jReturn.toString();
    }

    /**
     * 我的详情接口
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/i/info", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8;"})
    public String info() throws ErrMsgException{
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String userName = pvg.getUser(request);

        com.redmoon.oa.person.UserDb userDb = new com.redmoon.oa.person.UserDb();
        userDb = userDb.getUserDb(userName);

        com.alibaba.fastjson.JSONObject userObj = new com.alibaba.fastjson.JSONObject();

        User user = userCache.getUser(userName);
        userObj.put("wx_name", user.getRealName());
        if (!user.getGender()) {
            userObj.put("gender", "男");
        } else {
            userObj.put("gender", "女");
        }

        // String curRoleCode = com.redmoon.oa.pvg.Privilege.getCurRoleCode();

        userObj.put("phone", user.getMobile());
        userObj.put("real_name", user.getRealName());
        userObj.put("user_name",user.getName());
        userObj.put("id_number",user.getIDCard());
        userObj.put("remark","");
        userObj.put("address_detail", user.getAddress());
        userObj.put("phone2",user.getPhone());

        try {
            json.put("res", 0);
            json.put("result",userObj);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }
}
