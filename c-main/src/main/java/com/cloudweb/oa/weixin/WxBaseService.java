package com.cloudweb.oa.weixin;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.User;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.system.OaSysVerDb;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.mgr.WXDeptMgr;
import com.redmoon.weixin.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-7-22下午02:54:44
 */
public class WxBaseService {
    static final String group = "TOKEN";

	public Config config;

	public static final long cacheTime = 7200000; // 7200秒

	String userId;

	public WxBaseService(){
		this.config = Config.getInstance();
	}

	/**
	 * 通用微信请求接口
	 * @param url
	 * @param body
	 * @param methodRequest
	 * @return
	 */
	public int baseRequestWxAdd(String url,String body,int methodRequest){
		int errorCode = -1;
		String result = "";
		try {
			switch (methodRequest) {
				case Constant.REQUEST_METHOD_GET:
					 result = HttpUtil.MethodGet(url);
				break;
				case Constant.REQUEST_METHOD_POST:
					result = HttpUtil.MethodPost(url, body);
				break;
				default:
				break;
			}
			if(result!=null && !"".equals(result)){
				JSONObject json = new JSONObject(result);
				if(!json.isNull(Constant.ERRCODE)){
					errorCode = json.getInt(Constant.ERRCODE);
				} 
				if (json.has("errmsg")) {
					if (errorCode!=0) {
						LogUtil.getLog(WxBaseService.class).error(result);
						LogUtil.getLog(WxBaseService.class).error("url=" + url + " body=" + body);
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(WXDeptMgr.class).error(e.getMessage());
			LogUtil.getLog(getClass()).error(e);
		}
		return errorCode;
	}
	
/*	*//**
	 * 只能用于通讯录同步用户
	 * @return
	 *//*
	public String getTokenContacts() {
		Config config = Config.getInstance();
		boolean isWork = config.getBooleanProperty("isWork");
		if (!isWork) {
			return getToken();
		}
		else {
			String res = "";
			String result = "";
			try {
				String secretContacts = config.getProperty("secretContacts");
				String sCorpID = config.getProperty("corpId");
				String url = Constant.GET_TOKEN+"corpid="+sCorpID+"&corpsecret="+secretContacts;
				result = HttpUtil.MethodGet(url);
				if(result != null && !result.equals("")){
					LogUtil.getLog(WXBaseMgr.class).info(result);
					JSONObject json = new JSONObject(result);
					if(!json.isNull("access_token")){
						res = json.getString("access_token");
					}
				}
			} catch (JSONException e) {
				LogUtil.getLog(WXBaseMgr.class).error(e.getMessage() + " " + result);
			}
			return res;
		}
	}*/

	/**
	 * 根据secret获取token
	 * @param agentSecret
	 * @return
	 */
	public String getToken(String agentSecret) {
		Config config = Config.getInstance();
		boolean isWork = config.getBooleanProperty("isWork");
		if (!isWork) {
			// 如果是微信企业号
			agentSecret = config.getProperty("secret");
		}
		String res = "";
		try {
			OaSysVerDb osv = new OaSysVerDb();
			osv = osv.getOaSysVerDb(1);
			res = StrUtil.getNullString(osv.getString("weixin_accesstoken"));
			Date t = osv.getDate("weixin_accesstoken_time");
			long weixinAccesstokenTime = 0;
			if (t!=null) {
				weixinAccesstokenTime = t.getTime();
			}

			// 当前时间
			long now = System.currentTimeMillis();
			// 判断accessToken是否已经存在或者token是否过期，7200秒
			if(now - weixinAccesstokenTime > cacheTime ) {
				String sCorpID = config.getProperty("corpId");
				String url = Constant.GET_TOKEN+"corpid="+sCorpID+"&corpsecret="+agentSecret;
				String result = HttpUtil.MethodGet(url);
				if(result != null && !"".equals(result)){
					// LogUtil.getLog(WXBaseMgr.class).info(result);
					JSONObject json = new JSONObject(result);
					// {"expires_in":7200,"errmsg":"ok","access_token":"5_nG3fIPHamgL_oyhdEHJSFQ_WY59dm-xfEnHFxRWclxqITHypxN4hOgU7g1tfEhzTQPuwQ_0vQrt9H4tsmzW7PXCBGbnvwob7ik-i-_PJa78S5cV3bepIDSrf_1gmZbpDyErK1fcqDKg60WDKrRUxQGtCXaTosm9IC97p9-NJd1gInN2Qsa2OhNfZWVjXI-bb0-cdXJAKIXM_jpxC9lcg","errcode":0}
					if (json.has("errcode")) {
						int errcode = json.getInt("errcode");
						if (errcode!=0) {
							LogUtil.getLog(getClass()).error("getToken2: " + json.toString());
							DebugUtil.log(WxBaseService.class, "getToken", json.toString());
						}
						else {
							if(!json.isNull("access_token")){
								res = json.getString("access_token");
								// ti.tokenExpireTime = json.getLong("expires_in");
								osv.set("weixin_accesstoken", res);
								osv.set("weixin_accesstoken_time", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
								osv.save();
							}
						}
					}
					else {
						LogUtil.getLog(getClass()).error("getToken3: " + json.toString());
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(WxBaseService.class).error(e.getMessage());
			LogUtil.getLog(getClass()).error(e);
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return res;
	}
	
	/**
	 * 为微信企业号获得accessToken
	 * @Description: 
	 * @return
	 */
	public String getToken(){
		String secret = config.getProperty("secret");
		return getToken(secret);
	}

	/**
	 * 获得联系人TOKEN
	 * @return
	 */
	public String getTokenContacts(){
		String secret = config.getProperty("secretContacts");
		return getToken(secret);
	}

	/**
	 * 创建用户 组装json { "userid": "zhangsan", "name": "张三", "department": [1, 2],
	 * "position": "产品经理", "mobile": "15913215421", "gender": "1", "email":
	 * "zhangsan@gzdev.com", "weixinid": "zhangsan4dev", "avatar_mediaid":
	 * "2-G6nrLmr5EC3MNb_-zL1dDdzkd0p7cNliYu9V5w7o8K0", "extattr":
	 * {"attrs":[{"name":"爱好","value":"旅游"},{"name":"卡号","value":"1234567234"}]}
	 * }
	 *
	 * @Description: 组装用户参数
	 * @param user
	 * @return
	 */
	public  String getUserParam(User user) {
		JSONObject userObj = new JSONObject();
		try {
			Config config = Config.getInstance();
			boolean isUserIdUseEmail = config.isUserIdUseEmail();
			boolean isUserIdUseAccount = config.isUserIdUseAccount();
			boolean isUserIdUseMobile = config.isUserIdUseMobile();
			// 企业微信帐号只能由字母、数字及符号（.-_@）组成，故仍应根据配置文件中的微信帐户与系统用户关联字段来配置
			// 以便于当OA帐户为中文时，对应生成微信端帐户，因而在实施时，默认应该采用isUserIdUseMobile=true
			userId = user.getName();
			if (isUserIdUseAccount) {
				//使用工号登录
				AccountDb accountDb = new AccountDb();
				accountDb = accountDb.getUserAccount(userId);
				userId = accountDb.getName();
			}
			else if (isUserIdUseEmail) {
				userId = user.getEmail();
			}
			else if (isUserIdUseMobile) {
				userId = user.getMobile();
			}
			userObj.put("userid", userId);
			userObj.put("name", user.getRealName());
			userObj.put("mobile", user.getMobile());
			userObj.put("gender", user.getGender());
			userObj.put("email", user.getEmail());
			DeptUserDb dud = new DeptUserDb();
			Vector<DeptDb> vector = dud.getDeptsOfUser(user.getName());
			Iterator<DeptDb> it = vector.iterator();
			JSONArray deptArr = new JSONArray();
			while (it.hasNext()) {
				DeptDb dd = (DeptDb) it.next();
				int deptId = dd.getId();
				if (dd.getCode().equals(DeptDb.ROOTCODE)) {
					deptId = 1;
				}
				deptArr.put(deptId);
			}
			userObj.put("department", deptArr);

			userObj.put("position", "");
/*			PostUserMgr postUserMgr = new PostUserMgr();
			postUserMgr.setUserName(user.getName());
			PostUserDb postUserDb = postUserMgr.postByUserName();
			if (postUserDb != null && postUserDb.isLoaded()) {
				int post_id = postUserDb.getInt("post_id");
				PostDb pd = new PostDb();
				pd = pd.getPostDb(post_id);
				if (pd != null && pd.isLoaded()) {
					userObj.put("position", pd.getString("name"));
				}
			}*/

		} catch (JSONException e) {
		}
		return userObj.toString();
	}

	/**
	 * 创建部门 传递body 参数
	 * @Description:
	 * @param deptDb
	 * @return
	 */
	public  String getDeptParam(DeptDb deptDb) {
		JSONObject deptObj = new JSONObject();
		try {
			deptObj.put("name", deptDb.getName());
			deptObj.put("id", deptDb.getId());
			String parentCode = deptDb.getParentCode();
			DeptDb p_deptdb = new DeptDb(parentCode);
			deptObj.put("parentid", parentCode.equals(DeptDb.ROOTCODE)?1:p_deptdb.getId());
			deptObj.put("order", 1000 - deptDb.getOrders()); // 微信文档称按从小到大排序，实测却是从大到小排序
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return deptObj.toString();
	}
	
/*	class TokenItem implements Serializable {
	    private Long getTokenTime = 0L;
	    //参数的有效时间,单位是秒(s)
	    private Long tokenExpireTime = 0L;

		private String token = "";
	}*/

}
