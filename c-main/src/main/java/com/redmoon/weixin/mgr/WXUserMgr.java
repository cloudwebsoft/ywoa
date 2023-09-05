package com.redmoon.weixin.mgr;

import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserService;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Description:
 * @author:
 * @Date: 2016-7-21下午02:40:10
 */
public class WXUserMgr extends WXBaseMgr {

	/**
	 * 新增用户
	 * @param userDb
	 * @return
	 */
	public int createWxUser(UserDb userDb) {
		String accessToken = getTokenContacts();
		String url = Constant.CREATE_USER + accessToken;
		String userStr = getUserParam(userDb);
		// 保存微信号
		userDb.setWeixin(userId);
		userDb.save();
		return baseRequestWxAdd(url, userStr, Constant.REQUEST_METHOD_POST);
	}

	/**
	 * 更新用户
	 * @param userDb
	 * @return
	 */
	public int updateWxUser(UserDb userDb) {
		String accessToken = getTokenContacts();
		String url = Constant.UPDATE_USER + accessToken;
		String userStr = getUserParam(userDb);
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
			// TODO Auto-generated catch block
			LogUtil.getLog(WXUserMgr.class).info(e.getMessage());
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

	/**
	 * 本方法仅用于微信企业号
	 * 根据CODE 获得对应的用户名，注意getTokenContacts只能用于通讯录同步用户
	 * 瑞齐宁改为邮箱，邮箱为微信的敏感信息，所以需要通过授权获取
	 * @Description:
	 * @param code
	 * @return
	 */
	public UserDb getUserByCode(String code) {
		UserDb userDb = null;
		String accessToken = getTokenContacts();
		/*
		 * 取腾讯微信服务器的IP地址段，以便于配置防火墙，仅允许这些地址访问OA服务器
		String urlIp = "https://qyapi.weixin.qq.com/cgi-bin/getcallbackip?access_token="+accessToken;
		String ipStr= HttpUtil.MethodGet(urlIp);
        */
		// String accessToken = getToken(config.getProperty("secret"));

		String url = Constant.USER_BY_CODE + accessToken + "&code=" + code;
		String userInfo = HttpUtil.MethodGet(url);
		try {
			JSONObject obj = new JSONObject(userInfo);

			boolean isUserIdUseEmail = config.isUserIdUseEmail();
			boolean isUserIdUseAccount = config.isUserIdUseAccount();
			userDb = new UserDb();
			if (isUserIdUseEmail){
				// 用邮箱对应
				if (!obj.isNull("user_ticket")){
					String userTicket = obj.getString("user_ticket");
					JSONObject js = new JSONObject();
					js.put("user_ticket",userTicket);
					String serverName = Config.getInstance().getProperty("serverName");
					String url1 = "https://" + serverName + "/cgi-bin/user/getuserdetail?access_token=" + accessToken;//根据user_ticket获取成员信息
					String userInfoAll = HttpUtil.MethodPost(url1,js.toString());

					JSONObject obj1 = new JSONObject(userInfoAll);
					if (!obj1.isNull("email")){
						String email = obj1.getString("email");
						userDb = userDb.getUserDbByEmail(email);
						String userId = obj.getString("UserId");			
						// 记录微信帐号，以免微信帐号改变
						if (!userDb.getWeixin().equals(userId)) {
							userDb.setWeixin(userId);
							userDb.save();
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
						userDb = userDb.getUserDbByEmail(email);
						// 记录微信帐号，以免微信帐号改变
						if (!userDb.getWeixin().equals(userId)) {
							userDb.setWeixin(userId);
							userDb.save();
						}
					}
				}
			} else if (isUserIdUseAccount){
				// 使用工号登录
				if (!obj.isNull("UserId")) {
					String userId = obj.getString("UserId");
					AccountDb accountDb = new AccountDb();
					accountDb = accountDb.getAccountDb(userId);
					userDb = userDb.getUserDb(accountDb.getUserName());
				}
			} else if (config.isUserIdUseMobile()) {
				// 用手机号对应
				if (!obj.isNull("user_ticket")){
					String userTicket = obj.getString("user_ticket");
					JSONObject js = new JSONObject();
					js.put("user_ticket",userTicket);
					String serverName = Config.getInstance().getProperty("serverName");
					String url1 = "https://" + serverName + "/cgi-bin/user/getuserdetail?access_token=" + accessToken;//根据user_ticket获取成员信息
					String userInfoAll = HttpUtil.MethodPost(url1,js.toString());

					JSONObject obj1 = new JSONObject(userInfoAll);
					if (!obj1.isNull("mobile")) {
						String mobile = obj1.getString("mobile");
						userDb = userDb.getUserDbByMobile(mobile);
						String userId = obj.getString("UserId");			
						// 记录微信帐号，以免微信帐号改变
						if (!userDb.getMobile().equals(userId)) {
							userDb.setWeixin(userId);
							userDb.save();
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
						userDb = userDb.getUserDbByMobile(mobile);
						// 记录微信帐号，以免微信帐号改变
						if (!userDb.getMobile().equals(userId)) {
							userDb.setWeixin(userId);
							userDb.save();
						}
					}
				}
			}
			else {
				//账号登录
				if (!obj.isNull("UserId")){
					String userId = obj.getString("UserId");
					userDb = userDb.getUserDb(userId);
				}
			}
			/*if (!obj.isNull("UserId")) {
				String userId = obj.getString("UserId");
				userDb = new UserDb();
				Config config = Config.getInstance();
				boolean isUserIdUseEmail = config.isUserIdUseEmail();
				if (isUserIdUseEmail) {
					userDb = userDb.getUserDbByEmail(userId);
				}
				else {
					userDb = userDb.getUserDb(userId);
				}
			}*/
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(WXUserMgr.class).info(e.getMessage());
		} catch (Exception e){
			LogUtil.getLog(WXUserMgr.class).info(e.getMessage());
		}
		return userDb;
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
	public UserDb getUserByCode(String agentId, String code) {
		UserDb userDb = null;
		Config cfg = Config.getInstance();
		String accessToken = getToken(cfg.getSecretOfAgent(agentId));
		String url = Constant.USER_BY_CODE + accessToken + "&code=" + code;
		String userInfo = HttpUtil.MethodGet(url);
        LogUtil.getLog(getClass()).info("getUserByCode userInfo=" + userInfo);
		try {
			JSONObject obj = new JSONObject(userInfo);
			boolean isUserIdUseEmail = config.isUserIdUseEmail();
			boolean isUserIdUseAccount = config.isUserIdUseAccount();
			userDb = new UserDb();
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
						userDb = userDb.getUserDbByEmail(email);
						String userId = obj.getString("UserId");
						// 记录微信帐号，以免微信帐号改变
						if (!userDb.getWeixin().equals(userId)) {
							userDb.setWeixin(userId);
							userDb.save();
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
						userDb = userDb.getUserDbByEmail(email);
						// 记录微信帐号，以免微信帐号改变
						if (!userDb.getWeixin().equals(userId)) {
							userDb.setWeixin(userId);
							userDb.save();
						}
					}
				}
			}else if (isUserIdUseAccount){
				if (!obj.isNull("UserId")){
					String userId = obj.getString("UserId");
					AccountDb accountDb = new AccountDb();
					accountDb = accountDb.getAccountDb(userId);
					if (!accountDb.isLoaded()) {
						LogUtil.getLog(getClass()).error("工号 " + userId + " 对应的用户不存在！");
						return null;
					}
					userDb = userDb.getUserDb(accountDb.getUserName());
				}
			} else if (config.isUserIdUseMobile()) {
					// 用手机号对应，微信新接口中，已不含user_ticket，此分支保留以向下兼容
					if (!obj.isNull("user_ticket")) {
						String userTicket = obj.getString("user_ticket");
						JSONObject js = new JSONObject();
						js.put("user_ticket",userTicket);
						String serverName = Config.getInstance().getProperty("serverName");
						String url1 = "https://" + serverName + "/cgi-bin/user/getuserdetail?access_token=" + accessToken;//根据user_ticket获取成员信息
						String userInfoAll = HttpUtil.MethodPost(url1,js.toString());

						JSONObject obj1 = new JSONObject(userInfoAll);
						if (!obj1.isNull("mobile")) {
							String mobile = obj1.getString("mobile");
							userDb = userDb.getUserDbByMobile(mobile);
							String userId = obj.getString("UserId");			
							// 记录微信帐号，以免微信帐号改变
							if (!userDb.getMobile().equals(userId)) {
								userDb.setWeixin(userId);
								userDb.save();
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
							userDb = userDb.getUserDbByMobile(mobile);
							// 记录微信帐号，以免微信帐号改变
							if (userDb!=null && !userDb.getMobile().equals(userId)) {
								userDb.setWeixin(userId);
								userDb.save();
							}
						}
					}
				}
			else {
				//账号登录
				if (!obj.isNull("UserId")){
					String userId = obj.getString("UserId");
					IUserService userService = SpringUtil.getBean(IUserService.class);
					User user = userService.getUserByLoginName(userId);
					if (user != null) {
						userDb.getFromUser(user, userDb);
					} else {
						DebugUtil.i(getClass(), "getUserByCode", "用户: " + userId + "不存在");
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		} 
		return userDb;
	}	

}
