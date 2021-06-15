package com.redmoon.oa.fileark.robot;

import com.cloudwebsoft.framework.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Properties extends java.util.Properties {
    private String charset = "utf-8";

    public synchronized void load(String props)
            throws IOException {
        StringBuffer StringBuffer1 = new StringBuffer(props);
        ByteArrayInputStream bai = new ByteArrayInputStream(StringBuffer1.toString().getBytes(this.charset));
        load(bai);
        bai.close();
    }

    public String getProperty(String key) {
        String str = "";
        try {
            str = new String(super.getProperty(key).getBytes("ISO8859_1"), this.charset);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getProperty:" + e.getMessage());
        }
        return str;
    }
}