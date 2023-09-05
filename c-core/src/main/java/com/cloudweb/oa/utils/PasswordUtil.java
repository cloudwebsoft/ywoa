package com.cloudweb.oa.utils;

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.security.PwdLevel;
import cn.js.fan.security.PwdUtil;
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

    int level = PwdLevel.EASY.getType();

    public PasswordUtil() {
    }

    /**
     * 判断是否为连续的数字
     *
     * @param str String
     * @return boolean
     */
    public boolean isANumberSequence(String str) {
        char[] array = str.toCharArray();
        boolean eq = true;
        int num = array[1];
        int diff = num - array[0];
        for (int i = 2; i < array.length; i++) {
            if (array[i] != num + diff) {
                eq = false;
                break;
            }
            num = array[i];
        }

        return eq;
    }

    /**
     * 密码强度描述
     *
     * @param request HttpServletRequest
     * @param strenth int
     * @return String
     */
    public static String getStrenthDesc(HttpServletRequest request, int strenth) {
        String desc = "";
        switch (strenth) {
            case 0:
                desc = "密码组合规则不限";
                break;
            case 1:
                desc = "密码必须为字母加数字的组合";
                break;
            case 2:
                desc = "密码必须为字母加数字的组合且含大写字母";
                break;
            case 3:
                desc = "密码必须为字母、数字加字符组合且含大写字母";
                break;
            default:
                ;
        }
        return desc;
    }

    /**
     * 检查密码是否符合要求
     *
     * @param pwd     String
     * @param minLen  int
     * @param maxLen  int
     * @param strenth int 密码强度
     * @return int 1表示通过 <=0表示不通过
     */
    public int check(String pwd, int minLen, int maxLen, int strenth) {
        this.minLen = minLen;
        this.maxLen = maxLen;
        this.strenth = strenth;

        // 少于六位，则密码强度认定为不足
        if (pwd.length() >= minLen && pwd.length() <= maxLen) {
            if (!checkStrenth(pwd, strenth)) {
                result = -1;
            } else {
                result = 1;
            }
        } else {
            result = 0;
        }

        if (result != 1) {
            return result;
        }
        if (pwd.length() > 0) {
            com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
            int strenthLevelMin = scfg.getStrenthLevelMin();
            level = PwdUtil.getPasswordLevel(pwd).getType();
            if (level < strenthLevelMin) {
                result = -2;
            }
        }
        return result;
    }

    public String getLevelDesc(int level) {
        String levelDesc = "";
        if (level == PwdLevel.EASY.getType()) {
            levelDesc = PwdLevel.EASY.getDesc();
        } else if (level == PwdLevel.MIDIUM.getType()) {
            levelDesc = PwdLevel.MIDIUM.getDesc();
        }
        if (level == PwdLevel.STRONG.getType()) {
            levelDesc = PwdLevel.STRONG.getDesc();
        }
        if (level == PwdLevel.VERY_STRONG.getType()) {
            levelDesc = PwdLevel.VERY_STRONG.getDesc();
        }
        if (level == PwdLevel.EXTREMELY_STRONG.getType()) {
            levelDesc = PwdLevel.EXTREMELY_STRONG.getDesc();
        }
        return levelDesc;
    }

    public String getResultDesc(HttpServletRequest request) {
        String desc = "";
        if (result == -1) {
            desc = getStrenthDesc(request, strenth);
        } else if (result == 0) {
            desc = "密码长度必须在" + minLen + "到" + maxLen + "之间";
        } else if (result == -2) {
            com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
            int strenthLevelMin = scfg.getStrenthLevelMin();
            desc = "密码强度为" + getLevelDesc(level) + "，强度需为" + getLevelDesc(strenthLevelMin) + "以上";
        } else {
            desc = "密码规则检测通过";
        }

        return desc;
    }

    /**
     * 检测密码强度
     *
     * @param pwd     String
     * @param strenth int
     * @return boolean
     */
    public boolean checkStrenth(String pwd, int strenth) {
        if (strenth == 0) {
            return true;
        }

        // 小于2位，则判为不符合，以免引起数组下标溢出的的异常
        if (pwd.length() <= 2) {
            return false;
        }

        if (strenth == 1) {
            String patStr = "[a-zA-Z]+";
            if (Pattern.compile(patStr).matcher(pwd).find()) {
                patStr = "[\\d]+";
                return Pattern.compile(patStr).matcher(pwd).find();
            }
        } else if (strenth == 2) {
            String patStr = "[a-zA-Z]+";
            if (Pattern.compile(patStr).matcher(pwd).find()) {
                patStr = "[\\d]+";
                if (Pattern.compile(patStr).matcher(pwd).find()) {
                    patStr = "[A-Z]+";
                    return Pattern.compile(patStr).matcher(pwd).find();
                }
                // 不能由连续的字母组成，例如：abcdefg
            }
        } else if (strenth == 3) {
            String patStr = "[a-zA-Z]+";
            if (Pattern.compile(patStr).matcher(pwd).find()) {
                patStr = "[-`=\\[\\];',./~!@#$%\\^&\\*\\(\\)_+|{}:\"<>\\?]+";
                if (Pattern.compile(patStr).matcher(pwd).find()) {
                    patStr = "[\\d]+";
                    if (Pattern.compile(patStr).matcher(pwd).find()) {
                        // 判断是否含有大写字母
                        patStr = "[A-Z]+";
                        return Pattern.compile(patStr).matcher(pwd).find();
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasUpperCase(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 97 && c <= 122) {
                return true;
            }
        }

        return false;
    }

}
