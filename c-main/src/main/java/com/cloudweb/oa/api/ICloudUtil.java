package com.cloudweb.oa.api;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.redmoon.weixin.util.HttpPostFileUtil;

public interface ICloudUtil {

    String getLoginCheckUrl(String cwsToken, String formCode);

    void addParam(HttpPostFileUtil post) throws ErrMsgException;

    String getUserSecret();

    String parseForm(String content) throws ErrMsgException;
}
