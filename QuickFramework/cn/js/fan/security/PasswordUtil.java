package cn.js.fan.security;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: </p>
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
public class PasswordUtil {
    int result = 1;
    int strenth = 0;
    int minLen = 6;
    int maxLen = 20;

    public PasswordUtil() {
    }

    /**
     * 判断是否为连续的数字
     * @param str String
     * @return boolean
     */
    public boolean isANumberSequence(String str) {
        char[] array = str.toCharArray();
        boolean eq = true;
        int num = array[1];
        int diff = num - array[0];
        for ( int i = 2; i < array.length; i++) {
            if (array[i]!= num + diff) {
                eq = false;
                break;
            }
            num = array[i];
        }

        return eq;
    }

    /**
     * 密码强度描述
     * @param request HttpServletRequest
     * @param strenth int
     * @return String
     */
    public static String getStrenthDesc(HttpServletRequest request, int strenth) {
        String desc = "";
        switch(strenth) {
        case 0:
            desc = "密码强度规则不限";
            break;
        case 1:
            desc = "密码不能由同一字符组成，不能由连续的字母组成，也不能由连续的数字组成";
            break;
        case 2:
            desc = "密码必须为字母加数字的组合";
            break;
        case 3:
            desc = "密码必须为字母、数字加字符组合";
            break;
        default:
            ;
        }
        return desc;
    }

    /**
     * 检查密码是否符合要求
     * @param pwd String
     * @param minLen int
     * @param maxLen int
     * @param strenth int 密码强度
     * @return int 1表示通过 <=0表示不通过
     */
    public int check(String pwd, int minLen, int maxLen, int strenth) {
        this.minLen = minLen;
        this.maxLen = maxLen;
        this.strenth = strenth;

        // 少于六位，则密码强度认定为不足
        if (pwd.length() >= minLen && pwd.length() <= maxLen) {
            if (!checkStrenth(pwd, strenth))
                result = -1;
            else
                result = 1;
        }
        else
            result = 0;

        return result;
    }

    public String getResultDesc(HttpServletRequest request) {
    	String desc = "";
       if (result==-1) {
    	   desc =  getStrenthDesc(request, strenth);
       }
       else if (result==0)
    	   desc =  "密码长度必须在" + minLen + "到" + maxLen + "之间";
       else
    	   desc =  "密码规则检测通过";
       
       return desc;
    }

    /**
     * 检测密码强度
     * @param pwd String
     * @param strenth int
     * @return boolean
     */
    public boolean checkStrenth(String pwd, int strenth) {
        if (strenth == 0)
            return true;

        // 小于2位，则判为不符合，以免引起数组下标溢出的的异常
        if (pwd.length()<=2)
            return false;

        if (strenth==1) {
            char[] pwdArray = pwd.toCharArray();
            // 全部由同一个字符组成的直接判为弱
            boolean allEquals = true;
            char element = pwdArray[0];
            for (int i = 1; i < pwdArray.length; i++) {
                if (pwdArray[i]!=element) {
                    allEquals = false;
                    break;
                }
                element = pwdArray[i];
            }

            if (allEquals)
                return false;

            // 不能由连续的字母组成，例如：abcdefg
            if ("abcdefghijklmnopqrstuvwxyz".indexOf(pwd) >= 0) {
                return false;
            }

            if ("abcdefghijklmnopqrstuvwxyz".toUpperCase().indexOf(pwd) >= 0) {
                return false;
            }

            if (StrUtil.isNumeric(pwd)) {
                if (isANumberSequence(pwd))
                    return false;
            }
            return true;
        }
        else if (strenth==2) {
            if (Pattern.compile("[a-zA-Z]+").matcher(pwd).find()) {
                return Pattern.compile("[\\d]+").matcher(pwd).find();
            }
        }
        else if (strenth==3) {
            if (Pattern.compile("[a-zA-Z]+").matcher(pwd).find()) {
                if (Pattern.compile("[-`=\\[\\];',./~!@#$%\\^&\\*\\(\\)_+|{}:\"<>\\?]+").matcher(pwd).find())
                    return Pattern.compile("[\\d]+").matcher(pwd).find();
            }
        }

        return false;
    }

}
