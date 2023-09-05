package com.cloudweb.oa.api;

import com.redmoon.oa.flow.FormField;

public interface IQrcodeCtl {

    String getQrcodeSteamBase64(String content, int w, int h);

    String getQrcodeStr(FormField ff);
}
