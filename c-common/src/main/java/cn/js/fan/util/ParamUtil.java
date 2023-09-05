/**
 * $RCSfile: ParamUtils.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2002/09/09 13:51:07 $
 * <p>
 * New Jive  from Jdon.com.
 * <p>
 * This software is the proprietary information of CoolServlets, Inc.
 * Use is subject to license terms.
 */
package cn.js.fan.util;

import javax.servlet.http.*;

import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

/**
 * This class assists skin writers in getting parameters.
 */
public class ParamUtil {

    /**
     * Gets a parameter as a string.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @return The value of the parameter or null if the parameter was not
     *      found or if the parameter is a zero-length string.
     */
    public static String get(HttpServletRequest request, String name) {
        return get(request, name, true);
    }

    /**
     * 区别于get方法，对于所有的浏览器，都进行utf8转码。因为在ajax的时候，以及手机端传参的时候，都应传过来ISO8859_1编码的参数
     * @param request
     * @param name
     * @return
     */
    public static String getParam(HttpServletRequest request, String name) {
        String temp = request.getParameter(name);
        if (temp != null) {
            temp = temp.trim();

            if (Global.requestSupportCN) {
                return temp;
            } else {
                // 如果是手机端，经测试，无论是否在ajax时设置contentType: "application/x-www-form-urlencoded; charset=iso8859-1", 中文都可通过getParameter直接获取
                if (isMobile(request)) {
                    return temp;
                }
                return StrUtil.UnicodeToUTF8(temp);
            }
        } else {
            return "";
        }
    }

    /**
     * 判断访问是否来自于手机端
     * @param request
     * @return
     */
    public static boolean isMobile(HttpServletRequest request) {
        UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        OperatingSystem os = ua.getOperatingSystem();
        if(DeviceType.MOBILE.equals(os.getDeviceType())) {
            return true;
        }
        return false;
    }

    /**
     * Gets a parameter as a string.
     * @param request The HttpServletRequest object, known as "request" in a
     * JSP page.
     * @param name The name of the parameter you want to get
     * @param emptyStringsOK Return the parameter values even if it is an empty string.
     * @return The value of the parameter or null if the parameter was not
     *      found.
     */
    public static String get(HttpServletRequest request,
                             String name, boolean emptyStringsOK) {
        String temp = request.getParameter(name);
        if (temp != null) {
            temp = temp.trim();
            if ("".equals(temp) && !emptyStringsOK) {
                return null;
            } else {
                if (Global.requestSupportCN) {
                    return temp;
                } else {
                    String xRequestedWith = request.getHeader("x-requested-with"); // 为 null，则为传统同步请求，为 XMLHttpRequest，则为 Ajax 异步请求。
                    if (xRequestedWith != null) {
                        // firefox在ajax的时候自己会将中文转码
                        if (BrowserTool.isFirefox(request)) {
                            return temp;
                        } else if (BrowserTool.isChrome(request)) {
                            return StrUtil.UnicodeToUTF8(temp);
                        } else if (BrowserTool.isSafari(request)) {
                            return temp;
                        }
                    }

                    return StrUtil.UnicodeToUTF8(temp);
                }
            }
        } else {
            if (emptyStringsOK) {
                return "";
            } else {
                return null;
            }
        }
    }

    public static String[] getParameters(HttpServletRequest request, String name) {
        String[] ary = request.getParameterValues(name);
        if (ary == null) {
            return null;
        }
        int arylen = ary.length;
        if (!Global.requestSupportCN) {
            for (int i = 0; i < arylen; i++) {
                ary[i] = StrUtil.UnicodeToUTF8(ary[i]);
            }
        }
        return ary;
    }

    /**
     * Gets a parameter as a boolean.
     * @param request The HttpServletRequest object, known as "request" in a
     * JSP page.
     * @param name The name of the parameter you want to get
     * @return True if the value of the parameter was "true", false otherwise.
     */
    public static boolean getBooleanParameter(HttpServletRequest request,
                                              String name) {
        return getBoolean(request, name, false);
    }

    /**
     * Gets a parameter as a boolean.
     * @param request The HttpServletRequest object, known as "request" in a
     * JSP page.
     * @param name The name of the parameter you want to get
     * @return True if the value of the parameter was "true", false otherwise.
     */
    public static boolean getBoolean(HttpServletRequest request,
                                     String name, boolean defaultVal) {
        String temp = request.getParameter(name);
        if (temp!=null) {
            temp = temp.trim();
        }
        if ("true".equals(temp) || "on".equals(temp)) {
            return true;
        } else if ("false".equals(temp) || "off".equals(temp)) {
            return false;
        } else if ("1".equals(temp)) {
            return true;
        }
        else {
            return defaultVal;
        }
    }

    /**
     * Gets a parameter as an int.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @return The int value of the parameter specified or the default value if
     *      the parameter is not found.
     */
    public static int getInt(HttpServletRequest request,
                             String name) throws ErrMsgException {
        return getInt(request, name, true);
    }

    public static int getInt(HttpServletRequest request,
                             String name, int defaultValue) {
        String temp = request.getParameter(name);
        if (!StrUtil.isEmpty(temp)) {
            temp = temp.trim();
        }
        else {
            return defaultValue;
        }
        try {
            return Integer.parseInt(temp);
        } catch (Exception e) {
            LogUtil.getLog(ParamUtil.class).warn(temp + " is not int value");
        }
        return defaultValue;
    }

    public static int getInt(HttpServletRequest request,
                             String name, boolean isThrow) throws ErrMsgException {
        String temp = request.getParameter(name);
        if (temp!=null) {
            temp = temp.trim();
        }
        else {
            throw new ErrMsgException(name + " is null！");
        }
        int num = 0;
        try {
            num = Integer.parseInt(temp);
        } catch (Exception e) {
            LogUtil.getLog(ParamUtil.class).error(e);
            if (isThrow) {
                throw new ErrMsgException(name + " is not of int type！");
            }
        }
        return num;
    }

    /**
     * Gets a list of int parameters.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @param defaultNum The default value of a parameter, if the parameter
     * can't be converted into an int.
     */
    public static int[] getIntParameters(HttpServletRequest request,
                                         String name, int defaultNum) {
        String[] paramValues = request.getParameterValues(name);
        if (paramValues == null) {
            return null;
        }
        if (paramValues.length < 1) {
            return new int[0];
        }
        int[] values = new int[paramValues.length];
        for (int i = 0; i < paramValues.length; i++) {
            try {
                values[i] = Integer.parseInt(paramValues[i]);
            } catch (Exception e) {
                values[i] = defaultNum;
            }
        }
        return values;
    }

    /**
     * Gets a parameter as a double.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @return The double value of the parameter specified or the default value
     *      if the parameter is not found.
     */
    public static double getDouble(HttpServletRequest request,
                                   String name, double defaultNum) {
        String temp = request.getParameter(name);
        if (temp != null) {
            temp = temp.trim();
        }
        else {
            return defaultNum;
        }
        if (!temp.equals("")) {
            double num = defaultNum;
            try {
                num = Double.parseDouble(temp);
            } catch (Exception ignored) {
            }
            return num;
        } else {
            return defaultNum;
        }
    }

    public static double getDouble(HttpServletRequest request,
                                   String name) throws ErrMsgException {
        String temp = request.getParameter(name);
        if (temp != null) {
            temp = temp.trim();
        } else {
            throw new ErrMsgException(name + " is null!");
        }
        if (!temp.equals("")) {
            double num = 0.0;
            try {
                num = Double.parseDouble(temp);
            } catch (Exception ignored) {
                throw new ErrMsgException(name + " is not of double type !");
            }
            return num;
        } else {
            throw new ErrMsgException(name + " is empty!");
        }
    }

    public static long getLong(HttpServletRequest request,
                               String name) throws ErrMsgException {
        long num = -1;
        String temp = request.getParameter(name);
        if (temp!=null) {
            temp = temp.trim();
        }
        try {
            num = Long.parseLong(temp);
        } catch (Exception e) {
            throw new ErrMsgException("name=" + name + " value=" + temp + " is not a long value!");
        }
        return num;
    }

    /**
     * Gets a parameter as a long.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @return The long value of the parameter specified or the default value if
     *      the parameter is not found.
     */
    public static long getLong(HttpServletRequest request,
                               String name, long defaultNum) {
        String temp = request.getParameter(name);
        if (temp!=null) {
            temp = temp.trim();
        }
        else {
            return defaultNum;
        }
        if (!temp.equals("")) {
            long num = defaultNum;
            try {
                num = Long.parseLong(temp);
            } catch (Exception ignored) {
            }
            return num;
        } else {
            return defaultNum;
        }
    }

    /**
     * Gets a list of long parameters.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @param defaultNum The default value of a parameter, if the parameter
     * can't be converted into a long.
     */
    public static long[] getLongParameters(HttpServletRequest request,
                                           String name, long defaultNum) {
        String[] paramValues = request.getParameterValues(name);
        if (paramValues == null) {
            return null;
        }
        if (paramValues.length < 1) {
            return new long[0];
        }
        long[] values = new long[paramValues.length];
        for (int i = 0; i < paramValues.length; i++) {
            try {
                values[i] = Long.parseLong(paramValues[i]);
            } catch (Exception e) {
                values[i] = defaultNum;
            }
        }
        return values;
    }

    /**
     * Gets a parameter as a string.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @return The value of the parameter or null if the parameter was not
     *      found or if the parameter is a zero-length string.
     */
    public static String getAttribute(HttpServletRequest request, String name) {
        return getAttribute(request, name, false);
    }

    /**
     * Gets a parameter as a string.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the parameter you want to get
     * @param emptyStringsOK Return the parameter values even if it is an empty string.
     * @return The value of the parameter or null if the parameter was not
     *      found.
     */
    public static String getAttribute(HttpServletRequest request,
                                      String name, boolean emptyStringsOK) {
        String temp = (String) request.getAttribute(name);
        if (temp != null) {
            if (temp.equals("") && !emptyStringsOK) {
                return null;
            } else {
                if (Global.requestSupportCN) {
                    return temp;
                } else
                    return StrUtil.UnicodeToUTF8(temp);
            }
        } else {
            return null;
        }
    }

    /**
     * Gets an attribute as a boolean.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the attribute you want to get
     * @return True if the value of the attribute is "true", false otherwise.
     */
    public static boolean getBooleanAttribute(HttpServletRequest request,
                                              String name) {
        String temp = (String) request.getAttribute(name);
        if (temp != null && temp.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets an attribute as a int.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the attribute you want to get
     * @return The int value of the attribute or the default value if the
     *      attribute is not found or is a zero length string.
     */
    public static int getIntAttribute(HttpServletRequest request,
                                      String name, int defaultNum) {
        String temp = (String) request.getAttribute(name);
        if (temp != null && !temp.equals("")) {
            int num = defaultNum;
            try {
                num = Integer.parseInt(temp);
            } catch (Exception ignored) {
            }
            return num;
        } else {
            return defaultNum;
        }
    }

    /**
     * Gets an attribute as a long.
     * @param request The HttpServletRequest object, known as "request" in a
     *      JSP page.
     * @param name The name of the attribute you want to get
     * @return The long value of the attribute or the default value if the
     *      attribute is not found or is a zero length string.
     */
    public static long getLongAttribute(HttpServletRequest request,
                                        String name, long defaultNum) {
        String temp = (String) request.getAttribute(name);
        if (temp != null && !temp.equals("")) {
            long num = defaultNum;
            try {
                num = Long.parseLong(temp);
            } catch (Exception ignored) {
            }
            return num;
        } else {
            return defaultNum;
        }
    }
}
