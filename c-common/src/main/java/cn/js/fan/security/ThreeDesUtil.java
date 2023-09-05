package cn.js.fan.security;

/*
 在java中调用sun公司提供的3DES加密解密算法时，需要使用到$JAVA_HOME/jre/lib/目录下如下的4个jar包：
 jce.jar
 security/US_export_policy.jar
 security/local_policy.jar
 ext/sunjce_provider.jar
 Java运行时会自动加载这些包
 */

import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>Title:字符串 DESede(3DES) 加密 </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ThreeDesUtil {
    private static final String ALGORITHM = "DESede"; // 定义 加密算法,可用 DES,DESede,Blowfish

    public ThreeDesUtil() {
        // 添加新安全算法,如果用JCE就要把它添加进去
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
    }

    public static String encrypt2hex(String key, String content) {
        byte[] r = encryptMode(key.getBytes(), content.getBytes());
        return SecurityUtil.byte2hex(r);
    }

    public static String decrypthexstr(String key, String hexStr) {
        byte[] r = decryptMode(key.getBytes(), SecurityUtil.hexstr2byte(hexStr));
        if (r != null) {
            return new String(r);
        } else {
            return "";
        }
    }

    // keybyte为加密密钥，长度为24字节
    // src为被加密的数据缓冲区（源）
    public static byte[] encryptMode(byte[] keybyte, byte[] src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);

            // 加密
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    //keybyte为加密密钥，长度为24字节
    //src为加密后的缓冲区
    public static byte[] decryptMode(byte[] keybyte, byte[] src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);

            // 解密
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public String encodeToken(String key) {
        // uId
        String uId = "300";
        // 帐户
        String account = "项目处帐户";
        // 姓名
        String name = "项目处";
        // 部门编码按层级排列，以 \ 分隔
        String deptCode = "0001\\00010001";
        // 部门名称按层级排列，以 \ 分隔
        String deptName = "市局\\项目处";
        // 时间戳
        String timestamp = String.valueOf(new java.util.Date().getTime());
        String str = uId + "|" + account + "|" + name + "|" + deptCode + "|" + deptName + "|" + timestamp;
        return encrypt2hex(key, str);
    }

    public String decodeToken(String key, String hexStr) {
        return decrypthexstr(key, hexStr);
    }

    public static void main(String[] args) {
        // 密钥
        String key = "tzcjtzcjtzcjtzcjtzcjtzcj";

        ThreeDesUtil threeDesUtil = new ThreeDesUtil();
        // 生成 token
        String token = threeDesUtil.encodeToken(key);
        System.out.println("token=" + token);

        // 解码 token
        String str = threeDesUtil.decodeToken(key, token);
        System.out.println("str=" + str);
    }
}
