package com.redmoon.weixin.mgr;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.Post;
import com.cloudweb.oa.entity.PostUser;
import com.cloudweb.oa.service.IPostService;
import com.cloudweb.oa.service.IPostUserService;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.system.OaSysVerDb;
import com.redmoon.oa.ui.Skin;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.util.HttpUtil;

import cn.js.fan.cache.jcs.RMCache;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * @Description:
 * @author:
 * @Date: 2016-7-22下午02:54:44
 */
public class WXBaseMgr {
    static final String group = "TOKEN";

    public Config config;

    public static final long cacheTime = 7200000; // 7200秒

    protected String userId;

    private int lastErrorCode = -1;
    private String errMsg = "";

    public WXBaseMgr() {
        this.config = Config.getInstance();
    }

    /**
     * 通用微信请求接口
     *
     * @param url
     * @param body
     * @param methodRequest
     * @return
     */
    public int baseRequestWxAdd(String url, String body, int methodRequest) {
        // 加上debug以后，可以通过返回中的hint，在企业微信错误码查询工具中查看发送的参数
        /*if (Global.getInstance().isDebug()) {
            url += "&debug=1";
        }*/
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
            if (result != null && !"".equals(result)) {
                JSONObject json = new JSONObject(result);
                if (!json.isNull(Constant.ERRCODE)) {
                    errorCode = json.getInt(Constant.ERRCODE);
                }
                if (json.has("errmsg")) {
					errMsg = json.getString("errmsg");
                    if (errorCode != 0) {
                        LogUtil.getLog(WXBaseMgr.class).error("baseRequestWxAdd: url=" + url + " body=" + body);
                        LogUtil.getLog(WXBaseMgr.class).error("baseRequestWxAdd:" + result);
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        lastErrorCode = errorCode;
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
     *
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
            /*OaSysVerDb osv = new OaSysVerDb();
            osv = osv.getOaSysVerDb(1);
            res = StrUtil.getNullString(osv.getString("weixin_accesstoken"));
            Date t = osv.getDate("weixin_accesstoken_time");*/

            RMCache rmCache = RMCache.getInstance();
            res = (String)rmCache.get("weixin_accesstoken" + agentSecret);
            long weixinAccesstokenTime = 0;
            Date t = (Date)rmCache.get("weixin_accesstoken_time" + agentSecret);
            if (t != null) {
                weixinAccesstokenTime = t.getTime();
            }

            // 当前时间
            long now = System.currentTimeMillis();
            // 判断accessToken是否已经存在或者token是否过期，7200秒
            if (now - weixinAccesstokenTime > cacheTime) {
                String corpId = config.getProperty("corpId");
                String url = Constant.GET_TOKEN + "corpid=" + corpId + "&corpsecret=" + agentSecret;
                String result = HttpUtil.MethodGet(url);
                if (result != null && !"".equals(result)) {
                    // LogUtil.getLog(WXBaseMgr.class).info(result);
                    JSONObject json = new JSONObject(result);
                    // {"expires_in":7200,"errmsg":"ok","access_token":"5_nG3fIPHamgL_oyhdEHJSFQ_WY59dm-xfEnHFxRWclxqITHypxN4hOgU7g1tfEhzTQPuwQ_0vQrt9H4tsmzW7PXCBGbnvwob7ik-i-_PJa78S5cV3bepIDSrf_1gmZbpDyErK1fcqDKg60WDKrRUxQGtCXaTosm9IC97p9-NJd1gInN2Qsa2OhNfZWVjXI-bb0-cdXJAKIXM_jpxC9lcg","errcode":0}
                    if (json.has("errcode")) {
                        int errcode = json.getInt("errcode");
                        if (errcode != 0) {
                            LogUtil.getLog(getClass()).error("getToken2: " + json.toString());
                            DebugUtil.log(WXBaseMgr.class, "getToken", json.toString());
                        } else {
                            if (!json.isNull("access_token")) {
                                res = json.getString("access_token");
                                // ti.tokenExpireTime = json.getLong("expires_in");
                                /*osv.set("weixin_accesstoken", res);
                                osv.set("weixin_accesstoken_time", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                                osv.save();*/

                                rmCache.put("weixin_accesstoken" + agentSecret, res);
                                rmCache.put("weixin_accesstoken_time" + agentSecret, new Date());
                            }
                        }
                    } else {
                        LogUtil.getLog(getClass()).error("getToken3: " + json.toString());
                    }
                }
            }
        } catch (JSONException e) {
            LogUtil.getLog(WXBaseMgr.class).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
        return res;
    }

    /**
     * 获得accessToken
     *
     * @return
     * @Description:
     */
    public String getToken() {
        String secret = config.getProperty("secret");
        return getToken(secret);
    }

    /**
     * 获得联系人TOKEN
     *
     * @return
     */
    public String getTokenContacts() {
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
     * @param userDb
     * @return
     * @Description: 组装用户参数
     */
    public String getUserParam(UserDb userDb) {
        JSONObject userObj = new JSONObject();
        try {
            Config config = Config.getInstance();
            boolean isUserIdUseEmail = config.isUserIdUseEmail();
            boolean isUserIdUseAccount = config.isUserIdUseAccount();
            boolean isUserIdUseMobile = config.isUserIdUseMobile();
            // 企业微信帐号只能由字母、数字及符号（.-_@）组成，故仍应根据配置文件中的微信帐户与系统用户关联字段来配置
            // 以便于当OA帐户为中文时，对应生成微信端帐户，因而在实施时，默认应该采用isUserIdUseMobile=true
            userId = userDb.getName();
            if (isUserIdUseAccount) {
                //使用工号登录
                AccountDb accountDb = new AccountDb();
                accountDb = accountDb.getUserAccount(userId);
                userId = accountDb.getName();
            } else if (isUserIdUseEmail) {
                userId = userDb.getEmail();
            } else if (isUserIdUseMobile) {
                userId = userDb.getMobile();
            }
            userObj.put("userid", userId);
            userObj.put("name", userDb.getRealNameRaw());
            userObj.put("mobile", userDb.getMobile());
            userObj.put("gender", userDb.getGender());
            userObj.put("email", userDb.getEmail());
            DeptUserDb dud = new DeptUserDb();
            Vector<DeptDb> vector = dud.getDeptsOfUser(userDb.getName());
            Iterator<DeptDb> it = vector.iterator();
            JSONArray deptArr = new JSONArray();
            int mainDeptId = -1;
            while (it.hasNext()) {
                DeptDb dd = it.next();
                if (mainDeptId == -1) {
                    mainDeptId = dd.getId();
                }
                int deptId = dd.getId();
                if (dd.getCode().equals(DeptDb.ROOTCODE)) {
                    deptId = 1;
                }
                deptArr.put(deptId);
            }
            userObj.put("main_department", mainDeptId);
            userObj.put("department", deptArr);

            IPostUserService postUserService = SpringUtil.getBean(IPostUserService.class);
            PostUser postUser = postUserService.getPostUserByUserName(userDb.getName());
            if (postUser != null) {
                int post_id = postUser.getPostId();
                IPostService postService = SpringUtil.getBean(IPostService.class);
                Post post = postService.getById(post_id);
                if (post != null) {
                    userObj.put("position", post.getName());
                }
            }

        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return userObj.toString();
    }

    /**
     * 创建部门 传递body 参数
     *
     * @param department
     * @return
     * @Description:
     */
    public String getDeptParam(Department department) {
        JSONObject deptObj = new JSONObject();
        try {
            deptObj.put("name", department.getName());
            deptObj.put("id", department.getId());
            String parentCode = department.getParentCode();
            DeptDb p_deptdb = new DeptDb(parentCode);
            deptObj.put("parentid", parentCode.equals(DeptDb.ROOTCODE) ? 1 : p_deptdb.getId());
            deptObj.put("order", 1000 - (department.getOrders()!=null ?department.getOrders():1)); // 微信文档称按从小到大排序，实测却是从大到小排序
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return deptObj.toString();
    }

	public int getLastErrorCode() {
		return lastErrorCode;
	}

	public void setLastErrorCode(int lastErrorCode) {
		this.lastErrorCode = lastErrorCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
/*	class TokenItem implements Serializable {
	    private Long getTokenTime = 0L;
	    //参数的有效时间,单位是秒(s)
	    private Long tokenExpireTime = 0L;

		private String token = "";
	}*/

}
