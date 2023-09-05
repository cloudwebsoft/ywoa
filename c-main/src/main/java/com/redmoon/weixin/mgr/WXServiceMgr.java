package com.redmoon.weixin.mgr;

import cn.hutool.core.codec.Base64;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.system.OaSysVerDb;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.util.HttpUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import java.sql.SQLException;
import java.util.Arrays;
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
     * 根据code取得微信小程序的openid
     * @param code
     * @return
     */
    public JSONObject getMiniLoginInfo(String code, String encryptedData, String iv) {
        JSONObject jsonObject = new JSONObject();
        String appId = config.getProperty("wxMiniAppId");
        String secret = config.getProperty("wxMiniSecret");
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", appId, secret, code);
        DebugUtil.i(getClass(), "getMiniOpenId url", url);
        String result = HttpUtil.MethodGet(url);
        DebugUtil.i(getClass(), "getMiniOpenId result", result);

        JSONObject json = JSONObject.parseObject(result);
        if (json.containsKey("openid")) {
            // 微信用户的唯一标识，在不同的公众号小程序下这个ID是会变的
            String openId = json.getString("openid");
            jsonObject.put("openId", openId);

            Config cfg = Config.getInstance();
            int wxMiniLoginMode = StrUtil.toInt(cfg.getProperty("wxMiniLoginMode"), Config.LOGIN_MODE_OPENID);
            if (wxMiniLoginMode == Config.LOGIN_MODE_UNIONID && !StrUtil.isEmpty(encryptedData)) {
                // session_key则是微信服务器给开发者服务器颁发的身份凭证，开发者可以用session_key请求微信服务器其他接口来获取一些其他信息，
                // 故session_key不应该泄露或者下发到小程序前端
                String sessionKey = json.getString("session_key");

                JSONObject userInfoJson = getUserInfo(sessionKey, encryptedData, iv);
                if (userInfoJson != null) {
                    DebugUtil.i(getClass(), "encryptedData", userInfoJson.toString());
                    // 用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台帐号下会返回
                    String unionId = userInfoJson.getString("unionid");
                    if (unionId != null) {
                        jsonObject.put("unionId", unionId);
                    }
                }
                else {
                    DebugUtil.e(getClass(), "getMiniLoginInfo", "encryptedData 解密失败");
                }
            }
        }
        else {
            int errCode = json.getIntValue("errcode");
            json.put("errCode", errCode);
            json.put("errMsg", json.getString("errmsg"));
        }
        return jsonObject;
    }

    public static JSONObject getUserInfo(String sessionKey, String encryptedData, String iv){
        // 被加密的数据
        byte[] dataByte = Base64.decode(encryptedData);
        // 加密秘钥
        byte[] keyByte = Base64.decode(sessionKey);
        // 偏移量
        byte[] ivByte = Base64.decode(iv);

        try {
            // 如果密钥不足16位，那么就补足
            int base = 16;
            if (keyByte.length % base != 0) {
                int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
                byte[] temp = new byte[groups * base];
                Arrays.fill(temp, (byte) 0);
                System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
                keyByte = temp;
            }

            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);// 初始化
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, StandardCharsets.UTF_8);
                return JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            LogUtil.getLog(WXServiceMgr.class).error(e);
        }
        return null;
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
        DebugUtil.i(getClass(), "getOpenId result", result);
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
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
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


