package com.cloudweb.oa.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * ========================
 * Base64加密解密工具类
 * Created with IntelliJ IDEA.
 * User：pyy
 * Date：2019/7/18 9:25
 * Version: v1.0
 * ========================
 */
@Slf4j
public class Base64Util {
    private static final String charset = "utf-8";

    /**
     * 解密
     *
     * @param data
     * @return
     */
    public static String decode(String data) {
        try {
            if (null == data) {
                return null;
            }
            return new String(Base64.decodeBase64(data.getBytes(charset)), charset);
        } catch (UnsupportedEncodingException e) {
            log.error(String.format("字符串：%s，解密异常", data), e);
        }
        return null;
    }

    /**
     * 加密
     *
     * @param data
     * @return
     */
    public static String encode(String data) {
        try {
            if (null == data) {
                return null;
            }
            return new String(Base64.encodeBase64(data.getBytes(charset)), charset);
        } catch (UnsupportedEncodingException e) {
            log.error(String.format("字符串：%s，加密异常", data), e);
        }
        return null;
    }

}