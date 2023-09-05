package com.cloudweb.oa.security;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
public class LoginUtil {
    private static final long DELAY_TIME = 30000; // 延时时间为30秒
    private static final int MAX_FAIL_COUNT = 3;
    private static final long FAIL_TIME_SPAN = 20000; // 在半分钟内的最大出错次数为3
    private static final String GROUP_NAME = "CWS_LOGIN";

    private static final String LOGIN_FAIL_COUNT = "_login_fail_count";
    private static final String LOGIN_FAIL_FIRST = "_login_fail_first";
    private static final String LOGIN_FAIL_LAST = "_login_fail_last";

    @Autowired
    I18nUtil i18nUtil;

    public LoginUtil() {
    }

    /**
     * 初始化登录失败次数为0，在登录界面中使用,用于防暴力破解
     * @param userName
     */
    public void initLogin(String userName) {
        if (!RMCache.getInstance().getCanCache()) {
            return;
        }
        Integer c = (Integer)RMCache.getInstance().getFromGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME);
        if (c == null) {
            RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME, 0);
        }
    }

    public boolean canLogin(String userName) throws
            ErrMsgException {
        if (!RMCache.getInstance().getCanCache()) {
            return true;
        }
        Integer count = (Integer)RMCache.getInstance().getFromGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME);
        if (count == null) {
            count = 0;
            RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME, count);
        }

        if (count < MAX_FAIL_COUNT) {
            return true;
        }
        Long first = 0L;
        Long last = 0L;
        try {
            first = (Long)RMCache.getInstance().getFromGroup(userName + LOGIN_FAIL_FIRST, GROUP_NAME);
            last = (Long)RMCache.getInstance().getFromGroup(userName + LOGIN_FAIL_LAST, GROUP_NAME);
        } catch (NumberFormatException e) {
            throw new ErrMsgException("时间非法！");
        }
        long timespan = last - first;
        // 出错大于maxfailcount时，如果时间间隔小于预定值，则怀疑被攻击，采取措施
        if (timespan <= FAIL_TIME_SPAN) {
            long tspan = System.currentTimeMillis() - last;
            tspan = (DELAY_TIME - tspan) / 1000;
            if (tspan > 0) {
                // throw new ErrMsgException("对不起，您已在" + FAIL_TIME_SPAN / 1000 +
                //                          "秒内登录出错超过" + MAX_FAIL_COUNT + "次，您被延时" +
                //                          DELAY_TIME / 1000 + "秒登录，您还需" + tspan +
                //                          "秒才可以登录！");
                String str = i18nUtil.get("err_login_can_not");
                str = str.replaceFirst("\\$s", "" + FAIL_TIME_SPAN/1000);
                str = str.replaceFirst("\\$c", "" + MAX_FAIL_COUNT);
                str = str.replaceFirst("\\$d", "" + DELAY_TIME/1000);
                str = str.replaceFirst("\\$t", "" + tspan);
                throw new ErrMsgException(str);
            }
        } else {
            RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME, 0);
        }
        return true;
    }

    /**
     * 根据登录是否成功修改缓存中的相应的变量
     */
    public void afterLoginFailure(String userName) throws ErrMsgException {
        if (!RMCache.getInstance().getCanCache()) {
            return;
        }

        Integer c = (Integer)RMCache.getInstance().getFromGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME);
        if (c == null) {
            RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME, 0);
        }

        if (c == null) {
            // throw new ErrMsgException("After:" + i18nUtil.get("err_login_invalid")); //非法登录，因为在登录界面时未写入session zjpages_loginfailcount的值
            c = 0;
        }
        c++;
        RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_COUNT, GROUP_NAME, c);

        long t = System.currentTimeMillis();
        // 置登录失败第一次的时间和最后次的时间
        if (c == 1) {
            RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_FIRST, GROUP_NAME, t);
            RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_LAST, GROUP_NAME, t);
        } else {
            RMCache.getInstance().putInGroup(userName + LOGIN_FAIL_LAST, GROUP_NAME, t);
        }
        Long timespan = 0L;
        Long first = 0L, last = 0L;
        if (c == 1) {
            // throw new ErrMsgException("您已失败了" + count + "次！请注意：如果" +
            //                          FAIL_TIME_SPAN / 1000 + "秒内大于" +
            //                          MAX_FAIL_COUNT + "次您将被延时" +
            //                          DELAY_TIME / 1000 + "秒登录！");
            String str = i18nUtil.get("err_login_fail_one");
            str = str.replaceFirst("\\$c", "" + c);
            str = str.replaceFirst("\\$s", "" + FAIL_TIME_SPAN / 1000);
            str = str.replaceFirst("\\$m", "" + MAX_FAIL_COUNT);
            str = str.replaceFirst("\\$d", "" + DELAY_TIME / 1000);
            throw new ErrMsgException(str);
        } else {
            last = t;
        }
        try {
            first = (Long)RMCache.getInstance().getFromGroup(userName + LOGIN_FAIL_FIRST, GROUP_NAME);
        } catch (NumberFormatException e) {
            throw new ErrMsgException("时间格式错！");
        }
        timespan = (last - first) / 1000;
        // throw new ErrMsgException("您已在" + timespan + "秒中失败了" + count +
        //                          "次！请注意：如果" + FAIL_TIME_SPAN / 1000 + "秒内大于" +
        //                          MAX_FAIL_COUNT + "次您将被延时" + DELAY_TIME / 1000 +
        //                          "秒登录！");
        String str = i18nUtil.get("err_login_fail");
        str = str.replaceFirst("\\$t", "" + timespan);
        str = str.replaceFirst("\\$c", "" + c);
        str = str.replaceFirst("\\$f", "" + FAIL_TIME_SPAN/1000);
        str = str.replaceFirst("\\$m", "" + MAX_FAIL_COUNT);
        str = str.replaceFirst("\\$s", "" + DELAY_TIME/1000);
        throw new ErrMsgException(str);
    }
}
