package com.cloudweb.oa.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtil {
    @Autowired
    I18nUtil i18nUtil;

    public JSONObject getResultJson(boolean ret) {
        return getResultJson(ret, i18nUtil.get("info_op_success"), i18nUtil.get("info_op_fail"));
    }

    public JSONObject getResultJson(boolean ret, String msg) {
        JSONObject json = new JSONObject();
        json.put("ret", ret?1:0);
        if (msg.startsWith("#")) {
            msg = msg.substring(1);
            msg = i18nUtil.get(msg);
        }
        json.put("msg", msg);
        return json;
    }

    public JSONObject getResultJson(boolean ret, String successMsg, String failMsg) {
        if (successMsg.startsWith("#")) {
            successMsg = successMsg.substring(1);
            successMsg = i18nUtil.get(successMsg);
        }
        if (failMsg.startsWith("#")) {
            failMsg = failMsg.substring(1);
            failMsg = i18nUtil.get(failMsg);
        }
        JSONObject json = new JSONObject();
        if (ret) {
            json.put("ret", 1);
            json.put("msg", successMsg);
        }
        else {
            json.put("ret", 0);
            json.put("msg", failMsg);
        }
        return json;
    }

    public JSONObject getFailJson(String failMsg) {
        if (failMsg.startsWith("#")) {
            failMsg = failMsg.substring(1);
            failMsg = i18nUtil.get(failMsg);
        }
        return getResultJson(false, "", failMsg);
    }

}
