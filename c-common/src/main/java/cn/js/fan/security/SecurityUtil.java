package cn.js.fan.security;

/**
 * Title:        风青云[商城]
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      船艇学院
 * @author 		 风青云
 * @version 1.0
 */
import java.security.*;
import java.sql.*;
import javax.crypto.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.security.interfaces.RSAPublicKey;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.FileNotFoundException;
import java.io.IOException;
import cn.js.fan.db.SQLFilter;
import com.cloudwebsoft.framework.util.LogUtil;

public class SecurityUtil {
    String defaulturl = "../index.jsp";
    static boolean debug = false;
    private static String Algorithm = "DES"; // "DES"; //定义 加密算法,可用 DES,DESede,Blowfish

    public SecurityUtil() {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
    }

    //字节码转换成16进制字符串
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            //if (n< b.length-1)  hs=hs+":";
        }
        return hs.toLowerCase();
    }

    public static String MD5(String input) throws Exception {
        java.security.MessageDigest alg = java.security.MessageDigest.
                                          getInstance("MD5"); //or "SHA-1"
        alg.update(input.getBytes("utf-8"));
        byte[] digest = alg.digest();
        return byte2hex(digest);
    }

    public static String MD5(byte[] input) throws Exception {
        java.security.MessageDigest alg = java.security.MessageDigest.
                                          getInstance("MD5"); //or "SHA-1"
        alg.update(input);
        byte[] digest = alg.digest();
        return byte2hex(digest);
    }

    public static byte[] MD5Raw(String input) throws Exception {
        java.security.MessageDigest alg = java.security.MessageDigest.
                                          getInstance("MD5"); // or "SHA-1"
        alg.update(input.getBytes());
        return alg.digest();
    }

    public void setdefaulturl(String myurl) {
        this.defaulturl = myurl;
    }

    public String getdefaulturl() {
        return (this.defaulturl);
    }

    public boolean isRequestValid(HttpServletRequest request) throws
            SQLException {
        return request.getRequestURL().indexOf(request.getServerName()) != -1;
    }

    //生成密钥, 注意此步骤时间比较长
    public static byte[] getKey() throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance(Algorithm);
        SecretKey deskey = keygen.generateKey();
        return deskey.getEncoded();
    }

    //加密
    public static byte[] encode(byte[] input, byte[] key) throws Exception {
        SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
        Cipher c1 = Cipher.getInstance(Algorithm);
        c1.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] cipherByte = c1.doFinal(input);
        return cipherByte;
    }

    public static String encode2hex(byte[] input, byte[] key) throws Exception {
        SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
        Cipher c1 = Cipher.getInstance(Algorithm);
        c1.init(Cipher.ENCRYPT_MODE, deskey);
        byte[] cipherByte = c1.doFinal(input);
        return byte2hex(cipherByte);
    }

    //解密
    public static byte[] decode(byte[] input, byte[] key) throws Exception {
        SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
        Cipher c1 = Cipher.getInstance(Algorithm);
        c1.init(Cipher.DECRYPT_MODE, deskey);
        byte[] clearByte = c1.doFinal(input);
        return clearByte;
    }

    public static byte hex2byte(char hex) {
        int k = 0;
        if (hex >= '0' && hex <= '9') {
            k = hex - '0';
        } else if (hex >= 'A' && hex <= 'F') {
            k = 10 + hex - 'A';
        } else if (hex >= 'a' && hex <= 'f') {
            k = 10 + hex - 'a';
        } else {
            LogUtil.getLog(SecurityUtil.class).warn("Wrong hex digit!");
        }
        return (byte) (k & 0xFF);
    }

    //#define HEX2BYTE(ch) ((BYTE) (((ch)<='9') ? (ch)-'0' : tolower(ch) - 'a' + 10))

    public static byte hex2byte(char a1, char a2) {
        int k;

        if (a1 >= '0' && a1 <= '9') {
            k = (int) (a1 - '0');
        } else if (a1 >= 'a' && a1 <= 'f') {
            k = (int) (a1 - 'a' + 10);
        } else if (a1 >= 'A' && a1 <= 'F') {
            k = (int) (a1 - 'A' + 10);
        } else {
            k = 0;
        }

        k <<= 4;

        if (a2 >= '0' && a2 <= '9') {
            k += (int) (a2 - '0');
        } else if (a2 >= 'a' && a2 <= 'f') {
            k += (int) (a2 - 'a' + 10);
        } else if (a2 >= 'A' && a2 <= 'F') {
            k += (int) (a2 - 'A' + 10);
        } else {
            k += 0;
        }

        return (byte) (k & 0xFF);
    }

    public static byte[] hexstr2byte(String str) {
        int len = str.length();
        if (len % 2 != 0) {
            LogUtil.getLog(SecurityUtil.class).warn("十六进制字符串的长度为" + len + ",不为2的倍数!");
            return null; //经过byte2hex后结果的长度应为双数
        }
        byte[] r = new byte[len / 2];
        int k = 0;
        for (int i = 0; i < str.length() - 1; i += 2) {
            r[k] = hex2byte(str.charAt(i), str.charAt(i + 1));
            k++;
        }
        return r;
    }

    public static byte[] decodehexstr(String hexstr, byte[] key) throws
            Exception {
        byte[] input = hexstr2byte(hexstr);
        if (input == null) {
            return null;
        }
        SecretKey deskey = new javax.crypto.spec.SecretKeySpec(key, Algorithm);
        Cipher c1 = Cipher.getInstance(Algorithm);
        c1.init(Cipher.DECRYPT_MODE, deskey);
        byte[] clearByte = c1.doFinal(input);
        return clearByte;
    }

    public boolean verifysignature(String filename, byte[] oridata,
                                   byte[] signatureData) {
        X509Certificate cert = null;
        try {
            InputStream inStream = new FileInputStream(filename);
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                cert = (X509Certificate) cf.generateCertificate(inStream);
            } catch (java.security.cert.CertificateException e) {
                LogUtil.getLog(SecurityUtil.class).error(e);
                return false;
            }
            inStream.close();
        } catch (FileNotFoundException e) {
            LogUtil.getLog(SecurityUtil.class).error(e);
            return false;
        } catch (IOException e) {
            LogUtil.getLog(SecurityUtil.class).error(e);
            return false;
        }

        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

        Signature signer = null;
        try {
            signer = Signature.getInstance("MD5withRSA");
            signer.initVerify(publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LogUtil.getLog(getClass()).error(e);
            return false;
        }

        try {
            signer.update(oridata);
        } catch (SignatureException e) {
            LogUtil.getLog(SecurityUtil.class).error(e);
            return false;
        }

        boolean signatureVerified = false;
        try {
            signatureVerified = signer.verify(signatureData);
        } catch (SignatureException e) {
            LogUtil.getLog(SecurityUtil.class).error(e);
            return false;
        }
        return signatureVerified;
    }

    public static boolean isValidSqlParam(String param) {
        // 防止非法植入攻击
        return SQLFilter.isValidSqlParam(param);
    }

    public static boolean isValidSql(String sql) {
        return SQLFilter.isValidSql(sql);
    }

    /**
     * 防注入
     * @param str String
     * @return boolean
     */
    public static boolean sql_inj(String str) {
        return SQLFilter.sql_inj(str);
    }

  /**
   * 用于james中的sha算法
   * @param input
   * @return
   * @throws java.lang.Exception
   */
  public static String SHA_BASE64_24(String input) throws Exception {
    java.security.MessageDigest alg = java.security.MessageDigest.getInstance(
        "SHA");
    alg.update(input.getBytes());
    byte[] digest = alg.digest();
    return (new sun.misc.BASE64Encoder()).encode(digest).substring(0, 24);//取前二十四位
  }

  public static void main(String[] args) throws Exception {
      SecurityUtil su = new SecurityUtil();
      SecurityUtil.debug = true;
      String str = SecurityUtil.encode2hex("测试一下中文及englisth text".getBytes("utf-8"), "82986728".getBytes());

      // AEF6DCF78F7B10E06BDFF04C3FA6D95E38D813B37D42254C17
      // 852A6B5575FD3496298E7B1DC9
      // 852a6b5575fd3496c8f92e72a5480d6e
      // 852A6B5575FD349643BA8B0B6245E27AD7
      // 852a6b5575fd349643ba8b0b6245e27abdd7f6348e8d7c72

      // A1FC30933962D59B5700239D
      // a1fc30933962d59b11067454ab759e27

      // 5298E6D5129C27FBDE916AC47DEB2FDDE12D4968AD815DBC

      LogUtil.getLog(SecurityUtil.class).info(str);

  }

}
