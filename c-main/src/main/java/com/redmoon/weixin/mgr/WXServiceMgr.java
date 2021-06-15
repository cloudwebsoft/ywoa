package com.redmoon.weixin.mgr;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.system.OaSysVerDb;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.util.HttpUtil;

import java.sql.SQLException;
import java.util.Date;

/**
 * 微信服务号网络授权
 */
public class WXServiceMgr {
    static final String group = "TOKEN";

    public Config config;

    public static final long cacheTime = 7200000; // 7200秒

    String userId;

    public WXServiceMgr() {
        this.config = Config.getInstance();
    }

    private String getToken(String appid, String secret) {
        String _token = "";
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", appid, secret);
        String result = HttpUtil.MethodGet(url);
        JSONObject _json = JSONObject.parseObject(result);
        if (_json.containsKey("access_token")) {
            _token = _json.getString("access_token");
        }
        return _token;
    }

    /**
     * 在微信公众号菜单指向的页面中根据request中的参数code
     * @param code
     * @return
     */
    public String getOpenId(String code) {
        String appId = config.getProperty("wxServiceAppId");
        String secret = config.getProperty("wxServiceSecret");
        String url = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", appId, secret, code);
        String result = HttpUtil.MethodGet(url);
        JSONObject json = JSONObject.parseObject(result);
        String openid = "";
        if (json.containsKey("openid")) {
            openid = json.getString("openid");
        }
        return openid;
    }

    public String getTokenByDb() {
        Config config = Config.getInstance();
        String appId = config.getProperty("wxServiceAppId");
        String secret = config.getProperty("wxServiceSecret");
        String token = "";
        try {
            OaSysVerDb osv = new OaSysVerDb();
            osv = osv.getOaSysVerDb(1);
            token = StrUtil.getNullString(osv.getString("wxservice_acctoken"));
            Date t = osv.getDate("wxservice_acctoken_time");
            long weixinAccesstokenTime = 0;
            if (t != null) {
                weixinAccesstokenTime = t.getTime();
            }
            // 当前时间
            long now = System.currentTimeMillis();
            if (now - weixinAccesstokenTime > cacheTime) {
                token = getToken(appId, secret);
                if (token != null && !token.equals("")) {
                    JdbcTemplate jt = new JdbcTemplate();
                    boolean re = jt.executeUpdate("update oa_sys_ver set wxservice_acctoken=?,wxservice_acctoken_time=? where id=1", new Object[]{token, new Date()}) == 1;
                    System.out.println(re);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return token;
    }

    /**
     * 在menu下创建子菜单
     * @param menuName 一级菜单
     * @param subMenuName 子菜单
     * @param menuUrl
     * @return
     * @throws ErrMsgException
     */
    public boolean createMenu(String menuName, String subMenuName, String menuUrl) throws ErrMsgException {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s", getTokenByDb());
        String appId = config.getProperty("wxServiceAppId");
        menuUrl = String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect", appId, StrUtil.UrlEncode(menuUrl));
        String body = "  {" +
                "     \"button\":[" +
                "     " +
                "      {" +
                "           \"name\":\"" + menuName + "\"," +
                "           \"sub_button\":[" +
                "           {" +
                "               \"type\":\"view\"," +
                "               \"name\":\"" + subMenuName + "\"," +
                "               \"url\":\"" + menuUrl + "\"" +
                "            }]" +
                "       }]" +
                " }";

        // 返回格式 {"errcode":0,"errmsg":"ok"}
        String result = HttpUtil.MethodPost(url, body);
        JSONObject json = JSONObject.parseObject(result);
        if (json.getIntValue("errcode")!=0) {
            throw new ErrMsgException("code: " + json.getIntValue("errcode") + " errmsg: " + json.getString("errmsg"));
        }
        return true;
    }

}


