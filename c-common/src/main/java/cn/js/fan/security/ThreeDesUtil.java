package cn.js.fan.security;

/*
 在java中调用sun公司提供的3DES加密解密算法时，需要使用到$JAVA_HOME/jre/lib/目录下如下的4个jar包：
 jce.jar
 security/US_export_policy.jar
 security/local_policy.jar
 ext/sunjce_provider.jar

 Java运行时会自动加载这些包，因此对于带main函数的应用程序不需要设置到CLASSPATH环境变量中。对于WEB应用，不需要把这些包加到WEB-INF/lib目录下。
 */

import java.security.*;
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
     private static final String Algorithm = "DESede"; // 定义 加密算法,可用 DES,DESede,Blowfish

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
        if (r!=null)
            return new String(r);
        else
            return "";
    }

    // keybyte为加密密钥，长度为24字节
    // src为被加密的数据缓冲区（源）
    public static byte[] encryptMode(byte[] keybyte, byte[] src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);

            // 加密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    //keybyte为加密密钥，长度为24字节
    //src为加密后的缓冲区
    public static byte[] decryptMode(byte[] keybyte, byte[] src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);

            // 解密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

/*
    public static void main(String[] args) {
        byte[] keyBytes = {0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10,
                                0x40, 0x38
                                , 0x28, 0x25, 0x79, 0x51, (byte) 0xCB,
                                (byte) 0xDD, 0x55, 0x66
                                , 0x77, 0x29, 0x74, (byte) 0x98, 0x30, 0x40,
                                0x36, (byte) 0xE2}; // 24字节的密钥
        String key = "bluewindbluewindbluewind";
        String szSrc = "This is a 3DES test. 测试";
        String r = encrypt2hex(key, szSrc);
        System.out.println("r=" + r);
        System.out.println(decrypthexstr(key, r));

    }
*/
}
