package com.redmoon.dingding.servlet;

import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.util.LogUtil;
import com.dingtalk.oapi.lib.aes.DingTalkEncryptException;
import com.dingtalk.oapi.lib.aes.DingTalkEncryptor;
import com.redmoon.dingding.Config;
import com.redmoon.dingding.service.eventchange.EventChangeService;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class EventChangeReceiveServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        /**url中的签名**/
        String msgSignature = request.getParameter("signature");
        /**url中的时间戳**/
        String timeStamp = request.getParameter("timestamp");
        /**url中的随机字符串**/
        String nonce = request.getParameter("nonce");
        /**post数据包数据中的加密数据**/
        ServletInputStream sis = request.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(sis));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        JSONObject jsonEncrypt = JSONObject.parseObject(sb.toString());
        String encrypt = jsonEncrypt.getString("encrypt");
        Config _config = Config.getInstance();
        String _token = _config.getToken();
        String aes_key = _config.getAesKey();
        String corp_id = _config.getCropId();
        // 对回调的参数进行解密，确保请求合法
        /**对encrypt进行解密**/
        DingTalkEncryptor dingTalkEncryptor = null;
        String plainText = null;
        try {
            // 根据用户注册的token和AES_KEY进行解密
            dingTalkEncryptor = new DingTalkEncryptor(_token, aes_key,corp_id);
            plainText = dingTalkEncryptor.getDecryptMsg(msgSignature, timeStamp, nonce, encrypt);
            EventChangeService.disposeEventChange(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /**对从encrypt解密出来的明文进行处理**/
      /*  String eventType = plainTextJson.getString("EventType");
        switch (eventType) {
            case "user_add_org"://通讯录用户增加 do something
                break;
            case "user_modify_org"://通讯录用户更改 do something
                break;
            case "user_leave_org"://通讯录用户离职  do something
                break;
            case "org_admin_add"://通讯录用户被设为管理员 do something
                break;
            case "org_admin_remove"://通讯录用户被取消设置管理员 do something
                break;
            case "org_dept_create"://通讯录企业部门创建 do something
                break;
            case "org_dept_modify"://通讯录企业部门修改 do something
                break;
            case "org_dept_remove"://通讯录企业部门删除 do something
                break;
            case "org_remove"://企业被解散 do something
                break;
            case "check_url"://do something
            default: //do something
                break;
        }*/
        /**对返回信息进行加密**/
        long timeStampLong = Long.parseLong(timeStamp);
        Map<String, String> jsonMap = null;
        try {
            jsonMap = dingTalkEncryptor.getEncryptedMap("success", timeStampLong, nonce);
        } catch (DingTalkEncryptException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        JSONObject json = new JSONObject();
        json.putAll(jsonMap);
        resp.getWriter().append(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req,resp);
    }
}
