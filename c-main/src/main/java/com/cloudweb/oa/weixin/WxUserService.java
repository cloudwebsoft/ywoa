package com.cloudweb.oa.weixin;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.Account;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IAccountService;
import com.cloudweb.oa.service.IUserService;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.mgr.WXUserMgr;
import com.redmoon.weixin.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @author:
 * @Date: 2016-7-21下午02:40:10
 */
@Service
public class WxUserService extends WxBaseService {

    @Autowired
    IUserService usersService;

    @Autowired
    IAccountService accountService;

    /**
     * 新增用户
     * @param user
     * @return
     */
    public int createWxUser(User user) {
        String accessToken = getTokenContacts();
        String url = Constant.CREATE_USER + accessToken;
        String userStr = getUserParam(user);
        // 保存微信号
        user.setWeixin(userId);
        user.updateById();
        return baseRequestWxAdd(url, userStr, Constant.REQUEST_METHOD_POST);
    }

    /**
     * 更新用户
     * @param user
     * @return
     */
    public int updateWxUser(User user) {
        String accessToken = getTokenContacts();
        String url = Constant.UPDATE_USER + accessToken;
        String userStr = getUserParam(user);
        return baseRequestWxAdd(url, userStr, Constant.REQUEST_METHOD_POST);
    }

    /**
     * @Description:批量删除用户 { "useridlist": ["zhangsan", "lisi"] }
     * @param ids
     * @return
     */
    public int deleteBatchWxUser(String ids) {
        String accessToken = getTokenContacts();
        String url = Constant.BATCH_DELETE_USER + accessToken;
        return baseRequestWxAdd(url, ids, Constant.REQUEST_METHOD_POST);
    }

    /**
     * 单独删除用户
     * @param id
     * @return
     */
    public int deleteUser(String id) {
        String accessToken = getTokenContacts();
        String url = Constant.DELETE_USER + accessToken + "&userid=" + id;
        return baseRequestWxAdd(url, null, Constant.REQUEST_METHOD_GET);
    }

    /**
     * 微信获取
     * @param dId  获取的部门id
     * @param fetch_child 1/0：是否递归获取子部门下面的成员
     * @return
     */
    public JSONArray wxDeptUserList(int dId, int fetch_child) {
        JSONArray _userList = null;
        String url = Constant.DEPT_USER_LIST + getTokenContacts() + "&department_id="
                + dId + "&fetch_child=" + fetch_child;
        try {
            String result = HttpUtil.MethodGet(url);
            if (result != null && !result.equals("")) {
                JSONObject json = new JSONObject(result);
                if (json != null && !json.isNull(Constant.ERRCODE)) {
                    int errorCode = json.getInt(Constant.ERRCODE);
                    if (errorCode == Constant.ERROR_CODE_SUCCESS) {
                        _userList = json.getJSONArray(Constant.USERLIST);
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).info(e.getMessage());
        }
        return _userList;
    }

    /**
     * 不递归子部门用户
     * @param dId
     * @return
     */
    public JSONArray wxDeptUserList(int dId){
        return wxDeptUserList(dId,0);
    }

    public String getUserByCodeForWxWork(String code) throws ErrMsgException {
        Config cfg = Config.getInstance();
        String agentId = cfg.getDefaultAgentId();
        WXUserMgr wxUserMgr = new WXUserMgr();
        UserDb userDb = wxUserMgr.getUserByCode(agentId, code);
        if (userDb.isLoaded()) {
            // 如果用户已被禁用，则提示
            if (!userDb.isValid()) {
                throw new ErrMsgException("帐户已被停用");
            }
            return userDb.getName();
        } else {
            return null;
        }
    }

    /**
     * 本方法仅用于微信企业号
     * 根据CODE 获得对应的用户名，注意getTokenContacts只能用于通讯录同步用户
     * 瑞齐宁改为邮箱，邮箱为微信的敏感信息，所以需要通过授权获取
     * @Description:
     * @param code
     * @return
     */
    public User getUserByCode(String code) {
        User user = null;
        String accessToken = getTokenContacts();
		/*
		 * 取腾讯微信服务器的IP地址段，以便于配置防火墙，仅允许这些地址访问OA服务器
		String urlIp = "https://qyapi.weixin.qq.com/cgi-bin/getcallbackip?access_token="+accessToken;
		String ipStr= HttpUtil.MethodGet(urlIp);
        LogUtil.getLog(getClass()).info("ipStr = " + ipStr);
        */
        // String accessToken = getToken(config.getProperty("secret"));

        // JdbcTemplate jt = new JdbcTemplate();
        String url = Constant.USER_BY_CODE + accessToken + "&code=" + code;
        // LogUtil.getLog(getClass()).info("url = " + url);
        String userInfo = HttpUtil.MethodGet(url);
        // LogUtil.getLog(getClass()).info("userInfo = " + userInfo);
        try {
            JSONObject obj = new JSONObject(userInfo);

            boolean isUserIdUseEmail = config.isUserIdUseEmail();
            boolean isUserIdUseAccount = config.isUserIdUseAccount();
            if (isUserIdUseEmail){
                // 用邮箱对应
                if (!obj.isNull("user_ticket")){
                    String userTicket = obj.getString("user_ticket");
                    JSONObject js = new JSONObject();
                    js.put("user_ticket",userTicket);
                    // LogUtil.getLog(getClass()).info("js.toString() = " + js.toString());
                    String serverName = Config.getInstance().getProperty("serverName");
                    String url1 = "https://" + serverName + "/cgi-bin/user/getuserdetail?access_token=" + accessToken;//根据user_ticket获取成员信息
                    String userInfoAll = HttpUtil.MethodPost(url1,js.toString());

                    // LogUtil.getLog(getClass()).info("userInfoAll = " + userInfoAll);
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("email")){
                        String email = obj1.getString("email");
                        // LogUtil.getLog(getClass()).info("email = " + email);
                        user = usersService.getUserByEmail(email);
                        String userId = obj.getString("UserId");
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
                else {
                    String userId = obj.getString("UserId");
                    url = Constant.GET_USER_INFO + accessToken + "&userid=" + userId;
                    String userInfoAll = HttpUtil.MethodGet(url);
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("email")) {
                        String email = obj1.getString("email");
                        user = usersService.getUserByEmail(email);
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
            } else if (isUserIdUseAccount){
                // LogUtil.getLog(getClass()).info("使用工号登录");
                // 使用工号登录
                if (!obj.isNull("UserId")) {
                    String userId = obj.getString("UserId");
                    Account account = accountService.getAccount(userId);
                    user = usersService.getUser(account.getUserName());
                }
            } else if (config.isUserIdUseMobile()) {
                // 用手机号对应
                if (!obj.isNull("user_ticket")){
                    String userTicket = obj.getString("user_ticket");
                    JSONObject js = new JSONObject();
                    js.put("user_ticket",userTicket);
                    // LogUtil.getLog(getClass()).info("js.toString() = " + js.toString());
                    String serverName = Config.getInstance().getProperty("serverName");
                    String url1 = "https://" + serverName + "/cgi-bin/user/getuserdetail?access_token=" + accessToken;//根据user_ticket获取成员信息
                    String userInfoAll = HttpUtil.MethodPost(url1,js.toString());

                    // LogUtil.getLog(getClass()).info("userInfoAll = " + userInfoAll);
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("mobile")) {
                        String mobile = obj1.getString("mobile");

                        user = usersService.getUserByMobile(mobile);
                        String userId = obj.getString("UserId");
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
                else {
                    String userId = obj.getString("UserId");
                    url = Constant.GET_USER_INFO + accessToken + "&userid=" + userId;
                    String userInfoAll = HttpUtil.MethodGet(url);
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("mobile")) {
                        String mobile = obj1.getString("mobile");
                        user = usersService.getUserByMobile(mobile);
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
            }
            else {
                // 账号登录
                if (!obj.isNull("UserId")){
                    String userId = obj.getString("UserId");
                    user = usersService.getUser(userId);
                }
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).info(e.getMessage());
        } catch (Exception e){
            LogUtil.getLog(getClass()).info(e.getMessage());
        }
        return user;
    }

    /**
     * 取得腾讯服务器IP范围
     * @param accessToken
     * @return
     */
    public String getIPScope(String accessToken) {
        String serverName = Config.getInstance().getProperty("serverName");
        String urlIp = "https://" + serverName + "/cgi-bin/getcallbackip?access_token="+accessToken;
        return  HttpUtil.MethodGet(urlIp);
    }

    /**
     * 根据CODE 获得对应的用户名，用于企业微信
     * @param agentId
     * @param code
     * @return
     */
    public User getUserByCode(String agentId, String code) {
        User user = null;
        Config cfg = Config.getInstance();
        String accessToken = getToken(cfg.getSecretOfAgent(agentId));
        String url = Constant.USER_BY_CODE + accessToken + "&code=" + code;
        String userInfo = HttpUtil.MethodGet(url);
        LogUtil.getLog(getClass()).info("getUserByCode userInfo=" + userInfo);
        try {
            JSONObject obj = new JSONObject(userInfo);
            boolean isUserIdUseEmail = config.isUserIdUseEmail();
            boolean isUserIdUseAccount = config.isUserIdUseAccount();
            if (isUserIdUseEmail) {
                // 用邮箱登录，微信新接口中，已不含user_ticket，此分支保留以向下兼容
                if (!obj.isNull("user_ticket")) {
                    String userTicket = obj.getString("user_ticket");
                    String serverName = Config.getInstance().getProperty("serverName");
                    String url1 = "https://" + serverName + "/cgi-bin/user/getuserdetail?access_token=" + accessToken;//根据user_ticket获取成员信息
                    JSONObject js = new JSONObject();
                    js.put("user_ticket",userTicket);
                    String userInfoAll = HttpUtil.MethodPost(url1,js.toString());
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("email")){
                        String email = obj1.getString("email");
                        user = usersService.getUserByEmail(email);
                        String userId = obj.getString("UserId");
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
                else {
                    String userId = obj.getString("UserId");
                    url = Constant.GET_USER_INFO + accessToken + "&userid=" + userId;
                    String userInfoAll = HttpUtil.MethodGet(url);
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("email")) {
                        String email = obj1.getString("email");
                        user = usersService.getUserByEmail(email);
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
            }else if (isUserIdUseAccount){
                if (!obj.isNull("UserId")){
                    String userId = obj.getString("UserId");
                    Account account = accountService.getAccount(userId);
                    if (account==null) {
                        LogUtil.getLog(getClass()).error("工号 " + userId + " 对应的用户不存在！");
                        return null;
                    }
                    user = usersService.getUser(account.getUserName());
                }
            } else if (config.isUserIdUseMobile()) {
                // 用手机号对应，微信新接口中，已不含user_ticket，此分支保留以向下兼容
                if (!obj.isNull("user_ticket")) {
                    String userTicket = obj.getString("user_ticket");
                    JSONObject js = new JSONObject();
                    js.put("user_ticket",userTicket);
                    // LogUtil.getLog(getClass()).info("js.toString() = " + js.toString());
                    String serverName = Config.getInstance().getProperty("serverName");
                    String url1 = "https://" + serverName + "/cgi-bin/user/getuserdetail?access_token=" + accessToken;//根据user_ticket获取成员信息
                    String userInfoAll = HttpUtil.MethodPost(url1,js.toString());

                    // LogUtil.getLog(getClass()).info("userInfoAll = " + userInfoAll);
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("mobile")) {
                        String mobile = obj1.getString("mobile");
                        user = usersService.getUserByMobile(mobile);
                        String userId = obj.getString("UserId");
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
                else {
                    String userId = obj.getString("UserId");
                    url = Constant.GET_USER_INFO + accessToken + "&userid=" + userId;
                    String userInfoAll = HttpUtil.MethodGet(url);
                    JSONObject obj1 = new JSONObject(userInfoAll);
                    if (!obj1.isNull("mobile")) {
                        String mobile = obj1.getString("mobile");
                        user = usersService.getUserByMobile(mobile);
                        // 记录微信帐号，以免微信帐号改变
                        if (!user.getWeixin().equals(userId)) {
                            user.setWeixin(userId);
                            user.updateById();
                        }
                    }
                }
            }
            else {
                //账号登录
                if (!obj.isNull("UserId")){
                    String userId = obj.getString("UserId");
                    user = usersService.getUser(userId);
                }
            }
        } catch (JSONException e) {
            LogUtil.getLog(com.redmoon.weixin.mgr.WXUserMgr.class).error(StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
        }
        return user;
    }

}
