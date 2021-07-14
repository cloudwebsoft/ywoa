package cn.js.fan.util;

/**
 * Title:        字符串处理
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author 	 风青云
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.text.*;
import java.util.Stack;
import java.util.regex.*;

import javax.servlet.http.*;

import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.*;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class StrUtil {
    public static int INT_HELPER = 0;

    public StrUtil() {
    }

    /**
     * 对字符串进行URLEncoder.encode编码
     * @param str String
     * @param charset String
     * @return String
     */
    public static String UrlEncode(String str, String charset) {
        if (str == null)
            return "";

        String s = str;

        try {
            s = URLEncoder.encode(str, charset);
        } catch (Exception e) {

        }
        return s;
    }

    /**
     * 对字符串进行utf-8编码的URLEncoder.encode
     * @param str String
     * @return String
     */
    public static String UrlEncode(String str) {
        return UrlEncode(str, "utf-8");
    }

    /**
     * 切分字符串，如果字符串为null或者空，则返回null
     * @param str String
     * @param token String
     * @return String[]
     */
    public static String[] split(String str, String token) {
        str = StrUtil.getNullStr(str).trim();
        if (str.equals("")) {
            return null;
        }
        // 20180112 fgf 不忽略任何一个分隔符 
        return str.split(token, -1);
    }

    /**
     * 获取当前访问页面的url，相比getUrl此方法能够在解析后还原为正确的url
     * @param request HttpServletRequest
     * @return String
     */
    public static String getURL(HttpServletRequest request) {
        return request.getRequestURL() + "?" + request.getQueryString();
        /*
                String q = "";
                String[] strs = split(queryString, "&");
                if (strs != null) {
                    int len = strs.length;
                    for (int i = 0; i < len; i++) {
                        String[] pair = StrUtil.split(strs[i], "=");
                        if (pair.length == 2) {
                            if (q.equals(""))
                                q = pair[0] + "=" + StrUtil.UrlEncode(pair[1]);
                            else
         q += "&" + pair[0] + "=" + StrUtil.UrlEncode(pair[1]);
                        } else {
                            if (q.equals(""))
                                q = pair[0];
                            else
                                q += "&" + pair[0];
                        }

                    }
                }
                return str + q;
         */
    }

    /**
     * 根据扩展名判断是否为图片文件
     * @param ext String 文件扩展名
     * @return boolean
     */
    public static boolean isImage(String ext) {
        return ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("gif") ||
                ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("bmp");
    }

    /**
     * 获取当前访问页面的url(绝对路径)，此方法当queryString中含有=号时会变为%3D
     * @param request HttpServletRequest
     * @return String
     */
    public static String getUrl(HttpServletRequest request) {
        return request.getRequestURL() + "?" +
              UrlEncode(request.getQueryString(), "utf-8");
    }

    /**
     * 获取当前访问页面的url(相对路径)
     * @param request HttpServletRequest
     * @return String
     */
    public static String getUrlRelative(HttpServletRequest request) {
        String visualPath = request.getContextPath();
        if (visualPath.lastIndexOf("/")==visualPath.length()-1) {
            visualPath = visualPath.substring(0, visualPath.length()-1);
        }

        return visualPath + request.getServletPath() + "?" +
              UrlEncode(request.getQueryString(), "utf-8");
    }

    /**
     * 从request中获取IP，如果访问是来自代理服务器，则获取代理的IP
     * @param request HttpServletRequest
     * @return String
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("HTTP_X_FORWARDED_FOR"); // 如果有代理
        if (ip == null) {
            ip = request.getHeader("x-forwarded-for");
            String un = "unknown";
            if (ip != null && !ip.equalsIgnoreCase(un) &&
                ip.trim().length() > 0) {
                ;
            }
            else {
                ip = request.getHeader("http_client_ip");
                if (ip != null && !ip.equalsIgnoreCase(un) &&
                    ip.trim().length() > 0) {
                    ;
                }
                else {
                    ip = StrUtil.getNullStr(request.getRemoteAddr());
                }
            }
        }
        return getNullString(ip);
    }

    /**
     * 将null转换为空字符串
     * @param str String
     * @return String
     */
    public static String getNullString(String str) {
        return (str == null) ? "" : str;
    }

    /**
     * 将null转换为空字符串
     * @param str String
     * @return String
     */
    public static String getNullStr(String str) {
        return (str == null) ? "" : str;
    }

    /**
     * 替换字符串，相比JDK自带的方法，无需使用正则表达式
     * @param strSource String
     * @param strFrom String
     * @param strTo String
     * @return String
     */
    public static String replace(String strSource, String strFrom, String strTo) {
        if (strSource.equals("") || strSource == null) {
            return strSource;
        }
        String strDest = "";
        int intFromLen = strFrom.length();
        int intPos;
        if (strSource == null || (strSource.trim()).equals("")) {
            return strSource;
        }
        while ((intPos = strSource.indexOf(strFrom)) != -1) {
            strDest = strDest + strSource.substring(0, intPos);
            strDest = strDest + strTo;
            strSource = strSource.substring(intPos + intFromLen);
        }
        strDest = strDest + strSource;

        return strDest;
    }

    /**
     * 替换字符串中的'为''，用于SQL语句中
     * @param str String
     * @return String
     */
    public static String sqlstr(String str) {
        if (str == null || (str.trim()).equals("")) {
            str = "\'\'";
            return str;
        }
        str = "\'" + replace(str, "\'", "\'\'") + "\'";
        return str;
    }

    /**
     * 为了保证与utf-8的兼容性，本方法的实现相当于UnicodeToUTF8
     * @param strIn String
     * @return String
     * @deprecated 本方法已不再使用
     */
    public static String UnicodeToGB(String strIn) {
        return UnicodeToUTF8(strIn);
    }

    /**
     * 取得UTF-8中字符所占的长度，一个中文按两位计算
     * @param str String
     * @return int
     */
    public static int UTF8Len(String str) {
        int k = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 255) {
                k += 2;
            } else {
                k += 1;
            }
        }
        return k;
    }

    /**
     * 将ISO8859_1转换为GB2312编码
     * @param strIn String
     * @return String
     */
    public static String Unicode2GB(String strIn) {
        String strOut = null;
        if (strIn == null || (strIn.trim()).equals(""))
            return strIn;
        try {
            byte[] b = strIn.getBytes("ISO8859_1");
            strOut = new String(b, "GB2312"); //转为GB2312
        } catch (Exception e) {}
        return strOut;
    }

    /**
     * 将ISO8859_1转换为utf-8
     * @param strIn String
     * @return String
     */
    public static String UnicodeToUTF8(String strIn) {
        String strOut = null;
        if (strIn != null && !(strIn.trim()).equals("")) {
            try {
                byte[] b = strIn.getBytes("ISO8859_1");
                //strOut = new String(b, "GB2312");//转为GB2312
                strOut = new String(b, "utf-8"); //转为UTF-8
            } catch (Exception e) {
            }
            return strOut;
        } else {
            return strIn;
        }
    }

    /**
     * 将GB转换为ISO8859_1
     * @param strIn String
     * @return String
     */
    public static String GBToUnicode(String strIn) {
        byte[] b;
        String strOut = null;
        if (strIn == null || (strIn.trim()).equals(""))
            return strIn;
        try {
            b = strIn.getBytes("GB2312");
            strOut = new String(b, "ISO8859_1");
        } catch (UnsupportedEncodingException e) {}
        return strOut;
    }

    /**
     * 将utf8编码转换为ISO8859_1
     * @param strIn String
     * @return String
     */
    public static String UTF8ToUnicode(String strIn) {
        byte[] b;
        String strOut = null;
        if (strIn == null || (strIn.trim()).equals(""))
            return strIn;
        try {
            b = strIn.getBytes("utf-8");
            strOut = new String(b, "ISO8859_1");
        } catch (UnsupportedEncodingException e) {}
        return strOut;
    }

    /**
     * 在浏览器中将字符中放在段落中居中显示
     * @param str String
     * @return String
     */
    public static String p_center(String str) {
        return "<p align=center>" + str + "</p>";
    }

    /**
     * 在浏览器中将字符中放在段落中居中显示，并用color颜色值显示
     * @param str String
     * @param color String
     * @return String
     */
    public static String p_center(String str, String color) {
        return "<p align=center><font color='" + color + "'>" + str +
                "</font></p>";
    }

    /**
     * 在客户端浏览器中显示出错信息
     * @param msg String
     * @return String
     */
    public static String makeErrMsg(String msg) {
        String str = "<BR><BR><BR>";
        str += "<table width='70%' height='50' border='0' align='center' cellpadding='0' cellspacing='1' bgcolor='blue'>";
        str += "<tr>";
        str += "<td bgcolor='#FFFFFF' align='center'><b><font color=red>" + msg +
                "</font></b></td>";
        str += "</tr>";
        str += "</table>";
        return str;
    }

    /**
     * 在客户端浏览器中显示出错信息
     * @param msg String 出错信息
     * @param textclr String 文字颜色
     * @param bgclr String 背景色
     * @return String
     */
    public static String makeErrMsg(String msg, String textclr, String bgclr) {
        String str = "<BR><BR><BR>";
        str += "<table width='70%' height='50' border='0' align='center' cellpadding='0' cellspacing='1' bgcolor='" +
                bgclr + "'>";
        str += "<tr>";
        str += "<td bgcolor='#FFFFFF' align='center'><font color='" + textclr +
                "'><b>" + msg + "</b></font></td>";
        str += "</tr>";
        str += "</table>";
        return str;
    }

    /**
     * 在客户端浏览器中等侍t秒后，跳转至url
     * @param msg String 等待时的提示信息
     * @param t int 等待秒数
     * @param url String
     * @return String
     */
    public static String waitJump(String msg, int t, String url) {
        String str = "";
        String spanid = "id" + System.currentTimeMillis();
        str = "\n<ol><b><span id=" + spanid + "> 3 </span>";
        str += "秒钟后系统将自动返回... </b></ol>";
        str += "<ol>" + msg + "</ol>";
        str += "<script language=javascript>\n";
        str += "<!--\n";
        str += "function tickout(secs) {\n";
        str += spanid + ".innerText = secs;\n";
        str += "if (--secs > 0) {\n";
        str += "  setTimeout('tickout(' +secs + ')', 1000);\n";
        str += "}\n";
        str += "}\n";
        str += "tickout(" + t + ");\n";
        str += "-->\n";
        str += "</script>\n";
        str += "<meta http-equiv=refresh content=" + t + ";url=" + url + ">\n";
        return str;
    }

    /**
     * 在客户端浏览器中先alert，然后重定向至toUrl
     * @param msg String
     * @param toUrl String
     * @return String
     */
    public static String Alert_Redirect(String msg, String toUrl) {
        String str = "";
        str = "<script language=javascript>\n";
        str += "<!--\n";
        str += "alert(\"" + msg + "\")\n";
        if (!toUrl.equals(""))
            str += "location.href=\"" + toUrl + "\"\n";
        str += "-->\n";
        str += "</script>\n";
        return str;
    }

    /**
     * 在客户端浏览器中先alert然后退回前页
     * @param msg String
     * @return String
     */
    public static String Alert_Back(String msg) {
        String str = "";
        str = "<script language=javascript>\n";
        str += "<!--\n";
        str += "alert(\"" + msg + "\")\n";
        str += "history.back()\n";
        str += "-->\n";
        str += "</script>\n";
        return str;
    }

    /**
     * 在客户端浏览器中显示alert窗口
     * @param msg String
     * @return String
     */
    public static String Alert(String msg) {
        String str = "";
        str = "<script language=javascript>\n";
        str += "<!--\n";
        str += "alert(\"" + msg + "\")\n";
        str += "-->\n";
        str += "</script>\n";
        return str;
    }

    public static String jAlert_Back(String msg, String title) {
        String str = "";
        str = "<script language=javascript>\n";
        str += "<!--\n";
        str += "jAlert_Back(\"" + msg + "\", \"" + title + "\")\n";
        str += "-->\n";
        str += "</script>\n";
        return str;
    }

    public static String jAlert(String msg, String title) {
        String str = "";
        str = "<script language=javascript>\n";
        str += "<!--\n";
        str += "jAlert(\"" + msg + "\", \"" + title + "\")\n";
        str += "-->\n";
        str += "</script>\n";
        return str;
    }

    /**
     * 在客户端浏览器中先alert，然后重定向至toUrl
     * @param msg String
     * @param toUrl String
     * @return String
     */
    public static String jAlert_Redirect(String msg, String title, String toUrl) {
        String str = "";
        str = "<script language=javascript>\n";
        str += "<!--\n";
        str += "jAlert_Redirect(\"" + msg + "\", \"" + title + "\", \"" + toUrl + "\")\n";
        str += "-->\n";
        str += "</script>\n";
        return str;
    }

    /**
     * 将字符串转换为float
     * @param str String
     * @param defaultValue float 如果转换不成功，赋予的默认值
     * @return float
     */
    public static float toFloat(String str, float defaultValue) {
        float d = defaultValue;
        try {
            d = Float.parseFloat(str.trim());
        } catch (Exception e) {
            LogUtil.getLog(StrUtil.class).info("toFloat:" + e.getMessage());
        }
        return d;
    }

    /**
     * 将字符串转换为float，默认值为0.0
     * @param str String
     * @return float
     */
    public static float toFloat(String str) {
        return toFloat(str, 0.0f);
    }

    /**
     * 将字符串转换为double
     * @param str String
     * @param defaultValue double 如果转换不成功，赋予的默认值
     * @return double
     */
    public static double toDouble(String str, double defaultValue) {
        double d = defaultValue;
        try {
            //增加null及空判断  modify by jfy 20150402
            if (str != null && !str.trim().equals("")) {
                d = Double.parseDouble(str.trim());
            }
        } catch (Exception e) {
            LogUtil.getLog(StrUtil.class).info("toDouble:" + StrUtil.trace(e));
        }
        return d;
    }

    /**
     * 将字符串转换为double，默认值为0.0
     * @param str String
     * @return double
     */
    public static double toDouble(String str) {
        return toDouble(str, 0.0);
    }

    /**
     * 将字符串转换为int型
     * @param str String
     * @param defaultValue int 转换不成功时的默认值
     * @return int
     */
    public static int toInt(String str, int defaultValue) {
        int d = defaultValue;
        try {
            // d = Integer.valueOf(str.trim());
            d = (int)Double.parseDouble(str.trim());
        } catch (Exception e) {
            LogUtil.getLog(StrUtil.class).info( "toInt:" + e.getMessage());
        }
        return d;
    }

    /**
     * 转换字符串为int型，默认值为-1
     * @param str String
     * @return int
     */
    public static int toInt(String str) {
        return toInt(str, -1);
    }

    /**
     * 将字符串转换为long型
     * @param str String
     * @param defaultValue int 转换不成功时的默认值
     * @return int
     */
    public static long toLong(String str, long defaultValue) {
        long d = defaultValue;
        try {
            d = Long.parseLong(str.trim());
        } catch (Exception e) {
            LogUtil.getLog(StrUtil.class).error( "info:" + e.getMessage());
        }
        return d;
    }

    /**
     * 转换字符串为long型，默认值为-1
     * @param str String
     * @return int
     */
    public static long toLong(String str) {
        return toLong(str, -1);
    }

    /**
     * 检查字符串是否全由数字组成，注意：当为负数或带小数点的时候正确不能检测
     * @param s String
     * @return boolean
     */
    public static boolean isNumeric(String s) {
        if (s == null)
            return false;
        boolean flag = true;
        char[] numbers = s.toCharArray();
        if (numbers.length == 0)
            return false; // 空字符串
        for (int i = 0; i < numbers.length; i++) {
            if (!Character.isDigit(numbers[i])) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * 判断字符串中是否含有字母及数字外的东东
     * @param s String
     * @return boolean
     */
    public static boolean isCharOrNum(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (!(((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z')) ||
                  ((ch >= '0') && (ch <= '9')))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否可以作为简单的编码形式，只能由字母或者数字及 _ - 号组成
     * @param s String
     * @return boolean
     */
    public static boolean isSimpleCode(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (!(((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z')) ||
                  ((ch >= '0') && (ch <= '9')) || ch == '-' || ch == '_')) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否由字母组成
     * @param s String
     * @return boolean
     */
    public static boolean isChars(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (!(((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z')))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否不含有中文
     * @param str String
     * @return boolean
     */
    public static boolean isNotCN(String str) {
        Pattern pa = Pattern.compile("[\u4E00-\u9FA0]", Pattern.CANON_EQ);
        Matcher m = pa.matcher(str);
        if (m.find())
            return false;
        else
            return true;
    }

    /**
     * 格式化日期
     * @param d Date
     * @param format String yyyy-MM-dd  HH:mm:ss
     * @return String
     */
    public static String FormatDate(java.util.Date d, String format) {
        if (d == null)
            return "";
        SimpleDateFormat myFormatter = new SimpleDateFormat(format);
        return myFormatter.format(d);
    }

    /**
     * 格式化价格（金额）
     * @param value double
     * @return String
     */
    public static String FormatPrice(double value) {
        String subval = "0.00";
        if (value > 0.0) {
            subval = Double.toString(value);
            int decimal_len = subval.length() - (subval.lastIndexOf('.') + 1);
            if (decimal_len > 1)
                subval = subval.substring(0, subval.lastIndexOf('.') + 3);
            else
                subval += "0";
        }
        return subval;
    }

    /**
     * 格式化价格（金额）
     * @param value String
     * @return String
     */
    public static String FormatPrice(String value) {
        if (value == null)
            return null;
        String subval = "0.00";
        if (Double.parseDouble(value) > 0.0) {
            subval = value;
            int decimal_len = subval.length() - (subval.lastIndexOf('.') + 1);
            if (decimal_len > 1)
                subval = subval.substring(0, subval.lastIndexOf('.') + 3);
            else
                subval += "0";
        }
        return subval;
    }

    /**
     * 与toHtml的区别在于不转换空格与回车，用于编辑<textarea></textarea>中的内容时使用，否则会致使&nbsp;变为空格，当用于匹配时就会带来问题
     * @param str String
     * @return String
     */
    public static String HtmlEncode(String str) {
        if (str == null || str.equals("")) {
            return "";
        }
        java.lang.StringBuffer buf = new java.lang.StringBuffer(str.length() +
                6);
        char ch = ' ';
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (ch == '<')
                buf.append("&lt;");
            else if (ch == '>')
                buf.append("&gt;");
            else if (ch == '\'')
                buf.append("&#039;");
            else if (ch == '\"')
                buf.append("&quot;");
            else if (ch == '&')
                buf.append("&amp;");
            else
                buf.append(ch);
        }
        str = buf.toString();
        return str;
    }

    /**
     * 将字符串转换为html代码，将换行转换为<BR>，将<符号转换为&lt;等
     * @param str String
     * @return String
     */
    public static String toHtml(String str) {
        if (str == null || str.equals(""))
            return "";
        java.lang.StringBuffer buf = new java.lang.StringBuffer(str.length() +
                6);
        char ch = ' ';
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (ch == '<')
                buf.append("&lt;");
            else {
                if (ch == '>')
                    buf.append("&gt;");
                else {
                    if (ch == ' ')
                        buf.append("&nbsp;");
                    else {
                        if (ch == '\n')
                            buf.append("<br>");
                        else {
                            if (ch == '\'')
                                buf.append("&#039;");
                            else {
                                if (ch == '\"')
                                    buf.append("&quot;");
                                else
                                    buf.append(ch);
                            }
                        }
                    }
                }
            }
        }
        str = buf.toString();
        return str;
    }

    /**
     * 从国际化资源文件中根据key取字符串
     * @param request HttpServletRequest
     * @param key String
     * @return String
     */
    public static String LoadString(HttpServletRequest request, String key) {
        return SkinUtil.LoadString(request, "res.cn.js.fan.util.StrUtil", key);
    }

    /**
     * 编码json字符串中的单引号及双引号
     * 在处理表单域选择窗体宏控件的时候，发现=、$符号也需要编码
     * @param jsonString String
     * @return String
     */
    public static String encodeJSON(String jsonString) {
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        patternStr = "\""; // 双引号
        replacementStr =
                "%dq"; // double quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);

        patternStr = "'"; // 单引号
        replacementStr =
                "%sq"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);
        
        patternStr = "="; // 等号
        replacementStr =
                "%eq"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);    
        
        patternStr = "\\{"; // 左括号
        replacementStr =
                "%lb"; // {
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);  
        
        patternStr = "\\}"; // 右括号
        replacementStr =
                "%rb"; // {
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);          

        patternStr = "<"; //
        replacementStr =
                "%lt"; // {
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);       
        
        patternStr = ">"; //
        replacementStr =
                "%gt"; // {
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);    
        
        patternStr = ","; //
        replacementStr =
                "%co"; // {
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);           
        
        patternStr = "\\\\r\\\\n"; //
        replacementStr =
                "%rn"; // {
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);  
        
        
        patternStr = "\\\\n"; //
        replacementStr =
                "%n"; // {
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);        
        
        return jsonString;
    }

    /**
     * 解码json字符串中的单引号及双引号
     * 在处理表单域选择窗体宏控件的时候，发现=、$符号也需要编码
     * @param jsonString String
     * @return String
     */
    public static String decodeJSON(String jsonString) {
    	if (jsonString==null) {
    		return "";
    	}
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        patternStr = "%dq"; // 双引号
        replacementStr =
                "\""; // double quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);

        patternStr = "%sq"; // 单引号
        replacementStr =
                "'"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);
        
        patternStr = "%eq"; // 等号
        replacementStr =
                "="; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);    
        
        patternStr = "%lb"; // 单引号
        replacementStr =
                "{"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);    
        
        patternStr = "%rb"; //
        replacementStr =
                "}"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);         
        
        patternStr = "%lt"; //
        replacementStr =
                "<"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);      
        
        patternStr = "%gt"; //
        replacementStr = ">"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);      
        
        patternStr = "%co"; //
        replacementStr = ","; // 逗号
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);    
        
        /*
        // 不能将回车换行转码回来，因为json解析的时候会出现，而只能parseFilter中去转码
        patternStr = "%rn"; //
        replacementStr = "\r\n"; // 回车换行
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);    
        
        patternStr = "%n"; //
        replacementStr = "\n"; // 回车换行
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(jsonString);
        jsonString = matcher.replaceAll(replacementStr);          
		*/
        
        return jsonString;
    }

    /**
     * 去掉了自动识别网址
     * @param content String
     * @return String
     */
    public static String ubbWithoutAutoLink(HttpServletRequest request,
                                            String content) {
        return ubb(request, content, true, false);
    }

    public static String ubb(HttpServletRequest request, String content,
                             boolean show_smile, boolean isAutoLink) {
        if (content == null)
            return "";
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        if (show_smile) {
            patternStr = "\\[em(.[^\\[]*)\\]"; //表情
            //patternStr = "\\[em(\\d+)\\]";//表情（也可用）
            replacementStr =
                    "<img src=" + Global.getRootPath() + "/forum/images/emot/em$1.gif border=0 align=middle>";
            pattern = Pattern.compile(patternStr);
            matcher = pattern.matcher(content);
            content = matcher.replaceAll(replacementStr);
        }
        patternStr = "(\\[URL\\])(.[^\\[]*)(\\[\\/URL\\])";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<A HREF=\"$2\" TARGET=_blank>$2</A>");
        patternStr = "\\[URL=([^\\[]*?)\\](.[^\\[]*?)(\\[\\/URL\\])";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<A HREF=\"$1\" TARGET=_blank>$2</A>");
        pattern = Pattern.compile(
                "(\\[EMAIL\\])(\\S+\\@.[^\\[]*)(\\[\\/EMAIL\\])");
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "<img align=absmiddle src=images/email1.gif><A HREF=\"mailto:$2\">$2</A>");
        pattern = Pattern.compile(
                "(\\[EMAIL=(\\S+\\@.[^\\[]*)\\])(.[^\\[]*)(\\[\\/EMAIL\\])");
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<img align=absmiddle src=images/email1.gif><A HREF=\"mailto:$2\" TARGET=_blank>$3</A>");

        //patternStr = "\\[color=(.[^\\[]*)\\](.[^\\[]*)\\[\\/color\\]";
        patternStr = "\\[color=(.[^\\[]*)\\](.*)\\[\\/color\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<font color=$1>$2</font>");

        // patternStr = "\\[face=(.[^\\[]*)\\](.*?)\\[\\/face\\]"; // 不支持嵌套
        patternStr = "\\[face=(.[^\\[]*)\\](.[^\\[]*)\\[\\/face\\]"; // 支持嵌套，但只支持最外层 [face=楷体GB2312]dddd[face=宋体123]ffff[/face]ddd[/face]
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<font face=$1>$2</font>");
        patternStr = "\\[center\\](.+)\\[\\/center\\]";
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<div align=center>$1</div>");

        patternStr = "\\[right\\](.[^\\[]*)\\[\\/right\\]"; // 防止中间出现 [ 号
        // patternStr = "\\[right\\](.*)\\[\\/right\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<div align=right>$1</div>");

        patternStr = "\\[edit\\](.[^\\[]*)\\[\\/edit\\]"; // 防止中间出现 [ 号
        // patternStr = "\\[right\\](.*)\\[\\/right\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<div align=left><BR><i>$1</i></div>");

        patternStr = "\\[QUOTE\\](.[^\\[]*)\\[\\/QUOTE\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<table align=center style=\"width:80%\" cellpadding=5 cellspacing=1 class=tableborder1><TR><TD class=123>$1</td></tr></table>");

        patternStr = "\\[CODE\\](.[^\\[]*)\\[\\/CODE\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        if (matcher.find()) {
            String code = matcher.group(1); // .toLowerCase();
            code = code.replaceAll("<br ?/?>", "\n");
            code = code.replaceAll("<BR ?/?>", "\n");
            // content = matcher.replaceAll("<table align=center style=\"width:80%\" cellpadding=5 cellspacing=1 class=singleboarder><TR><TD class=123>$1</td></tr></table>");
            String codeStr = "<div><textarea name=\"JSCode" + INT_HELPER +
                             "\" rows=\"10\" cols=\"62\">" + code +
                             "</textarea>";
            codeStr += "<br />";
            codeStr +=
                    "<input name=\"button\" type=\"button\" onclick=\"runJS(JSCode" +
                    INT_HELPER + ")\" value=\"" + LoadString(request, "runJS") +
                    "\" />&nbsp;";
            codeStr +=
                    "<input name=\"button\" type=\"button\" onclick=\"copyJS(JSCode" +
                    INT_HELPER + ")\" value=\"" + LoadString(request, "copyJS") +
                    "\" />&nbsp;";
            codeStr +=
                    "<input name=\"button\" type=\"button\" onclick=\"saveJS(JSCode" +
                    INT_HELPER + ")\" value=\"" + LoadString(request, "saveJS") +
                    "\" />";
            codeStr += LoadString(request, "JS_hint") + "</div>";
            INT_HELPER++;
            if (INT_HELPER >= 50000) {
                INT_HELPER = 0;
            }
            content = matcher.replaceAll(codeStr);
        }

        patternStr = "\\[fly\\](.*)\\[\\/fly\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "<marquee width=90% behavior=alternate scrollamount=3>$1</marquee>");
        patternStr = "\\[move\\](.*)\\[\\/move\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<MARQUEE scrollamount=3>$1</marquee>");
        patternStr =
                "\\[glow=*([0-9]*),*(#*[a-z0-9]*),*([0-9]*)\\](.*)\\[\\/glow]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "<table width=$1 style=\"filter:glow(color=$2, strength=$3)\">$4</table>");
        patternStr =
                "\\[SHADOW=*([0-9]*),*(#*[a-z0-9]*),*([0-9]*)\\](.[^\\[]*)\\[\\/SHADOW]";
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "<table width=$1 style=\"filter:shadow(color=$2, strength=$3)\">$4</table>");

        // patternStr = "\\[I\\](.[^\\[]*)\\[\\/I\\]";
        patternStr = "\\[I\\](.*?)\\[\\/I\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<i>$1</i>");
        // patternStr = "\\[U\\](.[^\\[]*)(\\[\\/U\\])"; // 防止中间出现 [ 号
        patternStr = "\\[U\\](.*?)(\\[\\/U\\])"; // 防止中间出现 [ 号
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<u>$1</u>");
        // patternStr = "\\[B\\](.[^\\[]*)(\\[\\/B\\])";
        patternStr = "\\[B\\](.*?)(\\[\\/B\\])";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<b>$1</b>");

        patternStr = "\\[size=([1-8])\\](.[^\\[]*)\\[\\/size\\]"; // 防止中间出现 [ 号
        // patternStr = "\\[size=([1-8])\\](.*)\\[\\/size\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<font size=$1>$2</font>");

        patternStr = "\\[dir=*([0-9]*),*([0-9]*)\\](.*)\\[\\/dir]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<object classid=clsid:166B1BCA-3F9C-11CF-8075-444553540000 codebase=http://download.macromedia.com/pub/shockwave/cabs/director/sw.cab#version=7,0,2,0 width=$1 height=$2><param name=src value=$3><embed src=$3 pluginspage=http://www.macromedia.com/shockwave/download/ width=$1 height=$2></embed></object><BR>");
        patternStr = "\\[QT=*([0-9]*),*([0-9]*)\\](.*)\\[\\/QT]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<embed src=$3 width=$1 height=$2 autoplay=true loop=false controller=true playeveryframe=false cache=false scale=TOFIT bgcolor=#000000 kioskmode=false targetcache=false pluginspage=hhttttpp://www.apple.com/quicktime/></embed>");
        patternStr = "\\[mp=*([0-9]*),*([0-9]*)\\](.*)\\[\\/mp]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<object align=middle classid=CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95 class=OBJECT id=MediaPlayer width=$1 height=$2 ><param name=ShowStatusBar value=-1><param name=Filename value=$3><embed type=application/x-oleobject codebase=hhttttpp://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701 flename=mp src=$3  width=$1 height=$2></embed></object><BR>");
        patternStr = "\\[rm=*([0-9]*),*([0-9]*)\\](.*)\\[\\/rm]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll("<OBJECT classid=clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA class=OBJECT id=RAOCX width=$1 height=$2><PARAM NAME=SRC VALUE=$3><PARAM NAME=CONSOLE VALUE=Clip1><PARAM NAME=CONTROLS VALUE=imagewindow><PARAM NAME=AUTOSTART VALUE=true></OBJECT><br><OBJECT classid=CLSID:CFCDAA03-8BE4-11CF-B84B-0020AFBBCCFA height=32 id=video2 width=$1><PARAM NAME=SRC VALUE=$3><PARAM NAME=AUTOSTART VALUE=-1><PARAM NAME=CONTROLS VALUE=controlpanel><PARAM NAME=CONSOLE VALUE=Clip1></OBJECT><BR>");
        patternStr = "(\\[flash\\])(.[^\\[]*)(\\[\\/flash\\])";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "<a href=\"$2\" TARGET=_blank><IMG SRC=images/pic/swf.gif border=0 alt=" +
                LoadString(request, "click_open_win") + " height=16 width=16>" +
                LoadString(request, "view_full_win") + "</a><br><OBJECT codeBase=hhttttpp://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=4,0,2,0 classid=clsid:D27CDB6E-AE6D-11cf-96B8-444553540000 width=500 height=400><PARAM NAME=movie VALUE=\"$2\"><PARAM NAME=quality VALUE=high><embed src=\"$2\" quality=high pluginspage='hhttttpp://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash' type='application/x-shockwave-flash' width=500 height=400></embed>$2</OBJECT><BR>");
        patternStr =
                "(\\[flash=*([0-9]*),*([0-9]*)\\])(.[^\\[]*)(\\[\\/flash\\])";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "<a href=\"$4\" TARGET=_blank><IMG SRC=images/pic/swf.gif border=0 alt=" +
                LoadString(request, "click_open_win") + " height=16 width=16>" +
                LoadString(request, "view_full_win") + "</a><br><OBJECT codeBase=hhttttpp://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=4,0,2,0 classid=clsid:D27CDB6E-AE6D-11cf-96B8-444553540000 width=$2 height=$3><PARAM NAME=movie VALUE=\"$4\"><PARAM NAME=quality VALUE=high><embed src=\"$4\" quality=high pluginspage='hhttttpp://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash' type='application/x-shockwave-flash' width=$2 height=$3></embed>$4</OBJECT><BR>");

        //patternStr = "\\[img\\](http|https|ftp):\\/\\/(.[^\\[]*)\\[\\/img\\]";
        //pattern = Pattern.compile(patternStr);
        //matcher = pattern.matcher(content);
        //content = matcher.replaceAll("<a onfocus=this.blur() href=\"$1://$2\" target=_blank><IMG SRC=\"$1://$2\" border=0 alt=" + LoadString(request, "click_open_win") + " onload=\"javascript:if(this.width>screen.width-333)this.width=screen.width-333\"></a>");

        patternStr = "\\[img\\](.[^\\[]*)\\[\\/img\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(
                "<a onfocus=this.blur() href=\"$1\" target=_blank><IMG SRC=\"$1\" border=0 alt=" +
                LoadString(request, "click_open_win") + " onmousewheel='return zoomimg(this)' onload=\"javascript:if(this.width>screen.width-333)this.width=screen.width-333\"></a><br>");

        // 手写板
        patternStr = "\\[whitepad\\](.[^\\[]*)\\[\\/whitepad\\]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result = matcher.find();
        while (result) {
            INT_HELPER++;
            if (INT_HELPER >= 50000) {
                INT_HELPER = 0;
            }
            String str =
                    "<textarea style='display:none' id=\"value_spwhitepad_" +
                    INT_HELPER +
                    "\">$1</textarea><iframe src=\"" + Global.getRootPath() + "/spwhitepad/show.htm\" name=\"spwhitepad_" +
                    INT_HELPER + "\" frameborder=\"0\" style=\"width:400px;height:200px;margin:5px;border:1px dashed #CCCCCC;\" scrolling=\"no\"></iframe>";
            matcher.appendReplacement(sb, str);
            result = matcher.find();
        }
        matcher.appendTail(sb);
        content = sb.toString();

        // 自动识别网址
        if (isAutoLink) {
            String imgurl = "";
            if (Global.virtualPath.equals(""))
                imgurl = "/forum/images/pic/url.gif";
            else
                imgurl = "/" + Global.virtualPath + "/forum/images/pic/url.gif";
            patternStr =
                    "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\)[A-Za-z0-9\\./=\\?%\\-&_~`@':+!]+)";
            pattern = Pattern.compile(patternStr);
            matcher = pattern.matcher(content);
            content = matcher.replaceAll(
                    "<img align=absmiddle src='" + imgurl +
                    "' border=0><a target=_blank href=$1>$1</a>");
            patternStr = "((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\)[A-Za-z0-9\\./=\\?%\\-&_~`@':+!]+)$";
            pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            content = matcher.replaceAll(
                    "<img align=absmiddle src='" + imgurl +
                    "' border=0><a target=_blank href=$1>$1</a>");
            patternStr = "([^>=\"])((http|https|ftp|rtsp|mms):(\\/\\/|\\\\\\\\)[A-Za-z0-9\\./=\\?%\\-&_~`@':+!]+)";
            pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            content = matcher.replaceAll("$1<img align=absmiddle src='" +
                                         imgurl +
                                         "' border=0><a target=_blank href=$2>$2</a>");
            // 自动识别www等开头的网址
            patternStr = "([^(http://|http:\\\\)])((www|cn)[.](\\w)+[.]{1,}(net|com|cn|org|cc)(((\\/[\\~]*|\\\\[\\~]*)(\\w)+)|[.](\\w)+)*(((([?](\\w)+){1}[=]*))*((\\w)+){1}([\\&](\\w)+[\\=](\\w)+)*)*)";
            pattern = Pattern.compile(patternStr);
            matcher = pattern.matcher(content);
            content = matcher.replaceAll("<img align=absmiddle src='" + imgurl +
                                         "' border=0><a target=_blank href=http://$2>$2</a>");
        }
        //自动识别Email地址，如打开本功能在浏览内容很多的帖子会引起服务器停顿
        //patternStr = "([^(=)])((\\w)+[@]{1}((\\w)+[.]){1,3}(\\w)+)";
        //pattern = Pattern.compile(patternStr);
        //matcher = pattern.matcher(content);
        //content = matcher.replaceAll("<img align=absmiddle src=images/pic/url.gif border=0><a target=_blank href=\"mailto:$2\">$2</a>");

        //为防止http被重复替换，所以上面的UBB转换中用hhttttpp替代http，在此处再换回去
        content = content.replaceAll("hhttttpp", "http");
        content = content.replaceAll("ffttpp", "ftp");
        return content;
    }

    public static String ubb(HttpServletRequest request, String content,
                             boolean show_smile) {
        return ubb(request, content, show_smile, true);
    }

    public static boolean isValidIP(String ip) {
        // 检测是否包含非法字符
        Pattern p = Pattern.compile(
                "[0-9\\*]{1,3}\\.[0-9\\*]{1,3}\\.[0-9\\*]{1,3}\\.[0-9\\*]{1,3}");
        Matcher m = p.matcher(ip);
        boolean result = m.find();
        if (result) {
            return true;
        }
        return false;
    }

    /**
     * 判别Eamil格式是否合法
     * @param email String
     * @return boolean
     */
    public static boolean IsValidEmail(String email) {
        String input = email;
        // 检测输入的EMAIL地址是否以 非法符号"."或"@"作为起始字符
        Pattern p = Pattern.compile("^\\.|^\\@");
        Matcher m = p.matcher(input);
        if (m.find()) {
            return false;
        }
        // 检测是否以"www."为起始
        p = Pattern.compile("^www\\.");
        m = p.matcher(input);
        if (m.find()) {
            return false;
        }
        // 检测是否包含非法字符
        p = Pattern.compile("[^A-Za-z0-9\\.\\@_\\-~#]+");
        m = p.matcher(input);
        boolean result = m.find();
        if (result) {
            return false;
        }
        if (email.indexOf("@") == -1)
            return false;
        return true;
    }

    /**
     * 在IE状态条上显示信息
     * @param msg String
     * @return String
     */
    public static String ShowStatus(String msg) {
        String str = "";
        str = "<script language=javascript>\n";
        str += "<!--\n";
        str += "window.status=(\"" + msg + "\")\n";
        str += "-->\n";
        str += "</script>\n";
        return str;
    }

    /**
     * 获取字符串左侧长度为length的部分
     * @param str String
     * @param length int
     * @return String
     */
    public static String left(String str, int length) {
        if (str.length() >= length)
            return str.substring(0, length);
        else {
            return str.substring(0);
        }
    }

    /**
     * 取得相当字符串左边的N位英文字符长度的字符串，结果不是很精确
     * @param str String
     * @return length int
     */
    public static String getLeft(String str, int length) {
        if (str == null)
            return "";
        int k = 0;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) > 255) {
                k += 2;
            } else {
                k += 1;
            }
            if (k >= length)
                return str.substring(0, i + 1);
        }
        return str;
    }

    /**
     * 用于文章编辑时，webedit中编辑时的转换
     * @param text String
     * @return String
     */
    public static String HTMLEncode(String text) {
        if (text == null) {
            return "";
        }
        StringBuffer results = null;
        char[] orig = null;
        int beg = 0, len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            switch (c) {
            case 0:
            case '&':
            case '<':
            case '>':
            case '\"':
                if (results == null) {
                    orig = text.toCharArray();
                    results = new StringBuffer(len + 10);
                }
                if (i > beg) {
                    results.append(orig, beg, i - beg);
                }
                beg = i + 1;
                switch (c) {
                default:
                    continue;
                case '&':
                    results.append("&amp;");
                    break;
                case '<':
                    results.append("&lt;");
                    break;
                case '>':
                    results.append("&gt;");
                    break;
                case '\"':
                    results.append("&quot;");
                    break;
                }
                break;
            } //switch
        } // for i
        if (results == null) {
            return text;
        }
        results.append(orig, beg, len - beg);
        return results.toString();
    }

    /**
     * 取得文件的扩展名
     * @param fileName String
     * @return String
     */
    public static String getFileExt(String fileName) {
        if (fileName == null)
            return "";
        // 下面取到的扩展名错误，只有三位，而如html的文件则有四位
        // extName = fileName.substring(fileName.length() - 3, fileName.length()); // 扩展名
        int dotindex = fileName.lastIndexOf(".");
        String extName = fileName.substring(dotindex + 1, fileName.length());
        extName = extName.toLowerCase(); //置为小写
        return extName;
    }

    /**
     * 补足字符串长度为len，如果字符串长度已超出len，则返回原字符串
     * @param str String
     * @param pad String
     * @param len int
     * @param isLeft boolean 将padChar置于字符串的左侧
     * @return String
     */
    public static String PadString(String str, char padChar, int length,
                                   boolean isLeft) {
        int strLen = str.length();
        if (strLen >= length)
            return str;
        int len = length - strLen;
        String pStr = "";
        for (int i = 0; i < len; i++)
            pStr += padChar;
        if (isLeft)
            return pStr + str;
        else
            return str + pStr;
    }

    /**
     * 格式化字符串，在jdk1.5中已经引入了带可变参数的Formatter，此处是为了兼容1.4.x版及以下的JDK
     * @param format String 定位符%s
     * @param args String[]
     * @return String
     */
    public static String format(String format, Object[] args) {
        int len = args.length;

        // System.out.println("StrUtil format2=" + format);

        for (int i = 0; i < len; i++) {
            if (args[i] == null) {
                args[i] = "null";
            }

            // 注意如果args[i]中有$，则会报错，但为了提高效率，所以注释掉
            // if (args[i].toString().indexOf("$") != -1)
            //    args[i] = args[i].toString().replaceAll("\\$", "");

            format = format.replaceFirst("%s", args[i].toString());
        }

        return format;
    }

    /**
     * 将printStackTrace的内容输出为字符串
     * @param t Throwable
     * @return String
     */
    public static String trace(Throwable t) {
    	/*只能打印出前半段
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        String result = sw.toString();
        return result;
        */
    	
    	Throwable tr = ExceptionUtils.getRootCause(t);
    	if (tr instanceof ResKeyException) {
    		ResKeyException re = (ResKeyException)tr;
    		SQLException e = re.getSqlException();
        	if (e!=null) {
                return ExceptionUtils.getStackTrace(e);
            }
    	}    	
    	
    	// 能够取出更多的信息
    	String[] ary = ExceptionUtils.getRootCauseStackTrace(t);
        return StringUtils.join(ary, "\n");      	
    }

    /**
     * 转全角的函数(SBC case) 全角空格为12288，半角空格为32，其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * @param input String
     * @return String
     */
    public static String toSBC(String input) {
        // 半角转全角：
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) {
                c[i] = (char) 12288;
                continue;
            }
            if (c[i] < 127)
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    /**
     * 转半角的函数 全角空格为12288，半角空格为32，其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * @param input String
     * @return String
     */
    public static String toDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    /**
     * 功能同javascript中的escape
     * @param src String
     * @return String
     */
    public static String escape(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j) ||
                Character.isUpperCase(j))
                tmp.append(j);
            else
            if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    /**
     * 功能同javascript中的unescape
     * @param src String
     * @return String
     */
    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6),
                                                 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3),
                                                 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    public static String getAbstract(HttpServletRequest request, String html, int len) {
        return getAbstract(request, html, len, "p");
    }
    
    public static String getAbstract(HttpServletRequest request, String html, int len, String token) {
        return getAbstract(request, html, len, token, false);
    }

    /**
     * 提取摘要
     * @param request HttpServletRequest
     * @param html String
     * @param len int
     * @param token 包裹的标签
     * @param isImg 是否保留图片
     * @return String
     */
    public static String getAbstract(HttpServletRequest request, String html, int len, String token, boolean isImg) {
        // LogUtil.getLog(StrUtil.class.getName()).info("html=" + html);
    	if (html==null) {
    		return "";
    	}

        int MAX_LEN2 = len + 100;
        String content = html;

        if (token.equals("\r\n")) {
            content = StringEscapeUtils.unescapeHtml4(content);
        }

        // 对未完成的标签补齐，以免出现<im或<tab这样的标签
        int idx1 = content.lastIndexOf('<');
        int idx2 = content.lastIndexOf('>');
        // 如果截取时，未取到 > ，则继续往前取，直到取到为止
        // System.out.println("MsgUtil.java getAbstract: idx1=" + idx1 + " idx2=" + idx2);
        if ((idx2 == -1 && idx1 >= 0) || (idx1 > idx2)) {
            String ct3 = html;
            int idx3 = ct3.indexOf('>', idx1);
            if (idx3 != -1) {
                if (idx3 < MAX_LEN2) {
                    content = ct3.substring(0, idx3 + 1);
                }
            }
        }

        // 对于ActiveX对象进行预处理
        idx2 = content.toLowerCase().lastIndexOf("</object>");
        idx1 = content.toLowerCase().lastIndexOf("<object");
        if ((idx2 == -1 && idx1 >= 0) || idx1 > idx2) {
            String ct2 = html.toLowerCase();
            int idx3 = ct2.indexOf("</object>");
            if (idx3 != -1)
                content +=
                        html.substring(content.length(),
                                       content.length() + idx3 + 9);
            else
                content = html.substring(0, idx1);
        }

        String str = "";
        try {
            Parser myParser;
            NodeList nodeList = null;
            myParser = Parser.createParser(content, "utf-8");
            NodeFilter textFilter = new NodeClassFilter(TextNode.class);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
            NodeFilter styleFilter = new NodeClassFilter(StyleTag.class);
            // 暂时不处理 meta
            // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[] {textFilter, linkFilter,
                                     imgFilter, styleFilter});
            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            for (int i = 0; i < nodes.length; i++) {
                Node anode = (Node) nodes[i];
                String line = "";
                if (anode instanceof TextNode) {
                    TextNode textnode = (TextNode) anode;
                    // line = textnode.toPlainTextString().trim();
                    if (textnode.getParent() != null) {
                        if (!(textnode.getParent() instanceof StyleTag)) {
                            line = textnode.getText();
                        }
                        // System.out.println(HtmlUtil.class + " line1=" + line);
                    }
                    else {
                        line = textnode.getText();
                    }
                } else if (anode instanceof ImageTag) {
                	// request为null，可能来自手机端
                	if (isImg && request!=null) {
	                    ImageTag imagenode = (ImageTag) anode;
	                    String url = imagenode.getImageURL();
	                    String ext = StrUtil.getFileExt(url).toLowerCase();
	                    // 如果地址完整
	                    if (ext.equals("gif") || ext.equals("png") ||
	                        ext.equals("jpg") || ext.equals("jpeg") ||
	                        ext.equals("bmp")) {
	                        // System.out.println("MsgUtil.java getAbstract:" + imagenode.toHtml() + " url=" + imagenode.getImageURL());
	                        if (imagenode.getImageURL().startsWith("http"))
	                            ; // line = "<div align=center>" + imagenode.toHtml() + "</div>";
	                        else if (imagenode.getImageURL().startsWith("/")) {
	                            ; //line = "<div align=center>" + imagenode.toHtml() + "</div>";
	                        } else { // 相对路径
	                            // line = "<div align=center><img src='" + request.getContextPath() + "/forum/" + imagenode.getImageURL() + "'></div>";
	                            url = request.getContextPath() + "/forum/" +
	                                  imagenode.getImageURL();
	                        }
	                        line =
	                                "<div align=center><a onfocus=this.blur() href=\"" +
	                                url + "\" target=_blank><IMG SRC=\"" + url +
	                                "\" border=0 alt=" +
	                                SkinUtil.LoadString(request,
	                                "res.cn.js.fan.util.StrUtil",
	                                "click_open_win") + " onload=\"javascript:if(this.width>screen.width-333) this.width=screen.width-333\"></a></div><BR>";
	                        // System.out.println(line);
	                    }
                	}
                }

                if (StrUtil.getNullStr(line).trim().equals(""))
                    continue;

                // LogUtil.getLog(StrUtil.class.getName()).info("line =" + line);

                if (token.equalsIgnoreCase("p")) {
                    str += "<p>" + line + "</p>";
                } else if (token.equalsIgnoreCase("div")) {
                    str += "<div>" + line + "</div>";
                }  else if (token.equalsIgnoreCase("span")) {
                    str += "<span>" + line + "</span>";
                }  else {
                    // 如果token为换行符，则对其中的html entity转义
                    if (str.equals("")) {
                        str = line;
                    } else {
                        str += token + line;
                    }
                }
            }
        } catch (ParserException e) {
            LogUtil.getLog(StrUtil.class.getName()).error("getAbstract:" +
                    e.getMessage());
        }
        
        str = StrUtil.getLeft(str, len);
        
        return str;
    }

    /**
     * 补齐HTML标签
     * @param html String
     * @return String
     */
    public static String fillHtmlTag(String html) {
        try {
            Parser parser = Parser.createParser(html, "utf-8");

            NodeList nodelist = parser
                                .extractAllNodesThatMatch(new NodeFilter() {
                public boolean accept(Node node) {
                    if (node instanceof CompositeTag)
                        return true;
                    return false;
                }
            });

            if (nodelist.size()==0) {
            	return html;
            }

            String str = "";
            String tmp = "";
            for (int i = 0; i < nodelist.size(); i++) {
                CompositeTag testTag = (CompositeTag) nodelist.elementAt(i);

                if (testTag.getParent() == null) {
                    // 只需循环第一层就能补齐
                    tmp = testTag.toHtml();
                    str += tmp + "\n";
                }
            }

            return str;
        } catch (Exception e) {
            return "";
        }
    }
    /**
     * 获取字符串中含有子字符串的数量
     * @param sourceStr
     * @param subStr
     * @return
     */
    public static int getSubStrNum(String sourceStr, String subStr){
    	int total=0;
        char []s = sourceStr.toCharArray();
        for(int i = 0;i<s.length;i++){
                if(String.valueOf(s[i]).equals(subStr))
               {
                  total++;  
                }
        }
        return total;
    }
    /**
     * 校验括号是否对称 true：对称  false：不对称
     * @param source
     * @return
     */
    public static boolean checkBracketSymmetry(String source){
    	Stack<Character> sc=new Stack<Character>();
        char[] c=source.toCharArray();
        boolean flag = false;
        for (int i = 0; i < c.length; i++) {
            if (c[i]=='(') {
                sc.push(c[i]);
            }
            else if (c[i]==')') {
                if (sc.size() > 0 && sc.peek()=='(') {
                    sc.pop();
                }else{
                	flag = true;
                	break;
                }
            }
        }
        if (sc.empty() && !flag) {
            return true;
        }else if (!source.contains(")") && !source.contains("(")){
        	return true;
        }
        return false;
    }
    
    /**
     * 连接条件，如concat(..., "and", "name='cws'")
     * @return String
     */
    public static StringBuffer concat(StringBuffer targetStrBuf, String opToken, String str) {
        if (targetStrBuf.length()==0) {
        	targetStrBuf.append(str);
        }
        else {
        	targetStrBuf.append(opToken).append(str);
        }
        return targetStrBuf;
    }

    public static StringBuilder concat(StringBuilder stringBuilder, String opToken, String str) {
        if (stringBuilder.length()==0) {
            stringBuilder.append(str);
        }
        else {
            stringBuilder.append(opToken).append(str);
        }
        return stringBuilder;
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
