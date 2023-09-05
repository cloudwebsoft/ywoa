package com.cloudwebsoft.framework.security;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

/**
 * aes加密解密工具类
 * @author
 *
 */

public class AesUtil {

    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * base 64 decode
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     */
    public static byte[] base64Decode(String base64Code) {
        /* sun.misc.BASE64Decoder是java内部类，有时候会报错，
         * 用org.apache.commons.codec.binary.Base64替代，效果一样。
         */
        //Base64 base64 = new Base64();
        //byte[] bytes = base64.decodeBase64(new String(base64Code).getBytes());
        //new BASE64Decoder().decodeBuffer(base64Code);
        return StringUtils.isEmpty(base64Code) ? null : Base64.decodeBase64(base64Code.getBytes());
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String key, String keyIv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] temp = keyIv.getBytes(US_ASCII);
        IvParameterSpec iv = new IvParameterSpec(temp);

        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(US_ASCII), "AES"), iv);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes);
    }

    /**
     * 将base 64 code AES解密
     * @param encryptStr 待解密的base 64 code
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String key, String keyIv) throws Exception {
        return StringUtils.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64Decode(encryptStr), key, keyIv);
    }

    @SuppressWarnings("unused")
    public static String aesEncrypt(String str, String key, String keyIv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(keyIv.getBytes(US_ASCII));
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(US_ASCII), "AES"), iv);
            byte[] encryptBytes = cipher.doFinal(str.getBytes(UTF_8));

            return new String(Base64.encodeBase64(encryptBytes),US_ASCII);
            //return java.util.Base64.getEncoder().encodeToString(encryptBytes);
        }
        catch (Exception e) {
            LogUtil.getLog(AesUtil.class).error(e);
            return "";
        }
    }

    /**
     * 生成一个可选长度的随机数列
     * @param
     */
    public static String getRandomCode(int length){
        String[] arr = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G",
                "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
                "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
                "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        String str="";
        for (int i = 0; i < length; i++) {
            int r = (int) (Math.random()*61);
            str += arr[r];
        }
        return str;
    }

    public static void main(String[] args) {
        String a1=aesEncrypt("123", "njTzcjsomecloudw", "njTzcjsomecloudw");
        try {
            String a2 = aesDecrypt(a1, "njTzcjsomecloudw", "njTzcjsomecloudw");
        } catch (Exception e) {
            LogUtil.getLog(AesUtil.class).error(e);
        }

    }

}