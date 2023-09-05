package com.redmoon.oa.flow;

import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import bsh.Interpreter;
import com.cloudwebsoft.framework.util.LogUtil;
import bsh.EvalError;
import cn.js.fan.util.StrUtil;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description:
 * </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CalculateString {
    private String str, s;
    private int p1, p2;

    public CalculateString(String str) {
        this.str = str;
        this.clear();
        this.check();
    }

    /**
     * 分解小括号的函数，每遇到一对括号就把其中的内容抽取出来传递到真正的计算函数中处理
     */
    public void caculate() {
        while (isEnd(str)) {
            p2 = str.indexOf(")");
            if (p2 != -1) {
                s = str.substring(0, p2);
                p1 = s.lastIndexOf("(");
                s = str.substring(p1 + 1, p2);
                if (!"".equals(s)) {
                    s = this.caculateNumber(s);
                    if (p2 == (str.length() - 1)) {
                        str = str.substring(0, p1) + s;
                    } else {
                        str = str.substring(0, p1) + s + str.substring(p2 + 1);
                    }
                } else {
                    str = str.substring(0, p1) + str.substring(p2 + 1);
                }
            } else {
                str = this.caculateNumber(str);
            }
        }
    }

    /**
     * 真正的计算函数，负责计算所有不带括号的式子
     * @param ss String
     * @return String
     */
    private String caculateNumber(String ss) {
        int p3 = 0, p4 = 0, p5 = 0;
        char b = ' ', c = ' ';
        String s1 = null, s2 = null;
        double d1 = 0.0, d2 = 0.0, d3 = 0.0;
        if (ss.charAt(0) == '+') {
            ss = ss.substring(1);
            if (ss.indexOf("+") == -1 && ss.indexOf("-") == -1 &&
                ss.indexOf("*") == -1
                && ss.indexOf("/") == -1)
                return ss;
        } else if (ss.charAt(0) == '-') {
            if (ss.indexOf("+", 1) == -1 && ss.indexOf("-", 1) == -1 &&
                ss.indexOf("*", 1) == -1 && ss.indexOf("/", 1) == -1)
                return ss;
        }
        while (isEnd2(ss)) {
            p3 = 0;
            p4 = 0;
            p5 = 0;
            if (ss.indexOf("*") == -1 && ss.indexOf("/") == -1) {
                for (int i = 0; i < ss.length(); i++) {
                    b = ss.charAt(i);
                    if (b == '+' || b == '-') {
                        if (p3 == 0 && i != 0) {
                            p3 = i;
                            c = b;
                        } else if (p3 != 0 && i != (p3 + 1)) {
                            p4 = i;
                            break;
                        }
                    }
                }
                if (p4 == 0) {
                    s1 = ss.substring(0, p3);
                    s2 = ss.substring(p3 + 1);
                } else {
                    s1 = ss.substring(0, p3);
                    s2 = ss.substring(p3 + 1, p4);
                }

                d1 = Double.parseDouble(s1);
                d2 = Double.parseDouble(s2);
                if (c == '+') {
                    d3 = d1 + d2;
                } else if (c == '-') {
                    d3 = d1 - d2;
                }
                if (p4 == 0) {
                    ss = Double.toString(d3);
                } else {
                    ss = Double.toString(d3) + ss.substring(p4);
                }
            } else {
                if (!ss.contains("*")) {
                    c = '/';
                    p4 = ss.indexOf("/");
                } else if (!ss.contains("/")) {
                    c = '*';
                    p4 = ss.indexOf("*");
                } else if (ss.indexOf("*") > ss.indexOf("/")) {
                    c = '/';
                    p4 = ss.indexOf("/");
                } else if (ss.indexOf("*") < ss.indexOf("/")) {
                    c = '*';
                    p4 = ss.indexOf("*");
                }
                for (int i = p4 - 1; i >= 0; i--) {
                    b = ss.charAt(i);
                    if (b == '+' || b == '-') {
                        if ((i - 1) != -1 && ss.charAt(i - 1) != '+' &&
                            ss.charAt(i - 1) != '-') {
                            p3 = i;
                            break;
                        } else if ((i - 1) != -1 &&
                                   (ss.charAt(i - 1) == '+' ||
                                    ss.charAt(i - 1) == '-')) {
                            p3 = i - 1;
                            break;
                        }
                    }
                }
                for (int i = p4 + 1; i < ss.length(); i++) {
                    b = ss.charAt(i);
                    if ((b == '+' || b == '-' || b == '*' || b == '/') &&
                        (i - 1) != p4) {
                        p5 = i;
                        break;
                    }
                }
                if (p3 == 0)
                    s1 = ss.substring(0, p4);
                else
                    s1 = ss.substring(p3 + 1, p4);
                if (p5 == 0)
                    s2 = ss.substring(p4 + 1);
                else
                    s2 = ss.substring(p4 + 1, p5);
                d1 = Double.parseDouble(s1);
                d2 = Double.parseDouble(s2);
                if (c == '*')
                    d3 = d1 * d2;
                else if (c == '/')
                    d3 = d1 / d2;
                if (p3 == 0 && p5 == 0)
                    ss = new Double(d3).toString();
                else if (p3 == 0 && p5 != 0)
                    ss = new Double(d3).toString() + ss.substring(p5);
                else if (p3 != 0 && p5 == 0)
                    ss = ss.substring(0, p3 + 1) + Double.toString(d3);
                else if (p3 != 0 && p5 != 0)
                    ss = ss.substring(0, p3 + 1) + Double.toString(d3) +
                         ss.substring(p5);
            }
        }
        return ss;
    }

    /**
     * 用来判断整个式子是否结束的函数
     * @param str String
     * @return boolean
     */
    private boolean isEnd(String str) {
        return str.contains("(") || str.contains(")") ||
                str.indexOf("+", 1) != -1
                || str.indexOf("-", 1) != -1 || str.indexOf("*", 1) != -1 ||
                str.indexOf("/", 1) != -1;
    }

    /**
     * 用来判断不含括号的式子是否结束的函数
     * @param ss String
     * @return boolean
     */
    private boolean isEnd2(String ss) {
        if (ss.indexOf("+", 1) != -1 || ss.indexOf("-", 1) != -1 ||
            ss.indexOf("*", 1) != -1 || ss.indexOf("/", 1) != -1)
            return true;
        return false;
    }

    /**
     * 用来去除原式中所有的空格
     */
    private void clear() {
        str = str.replaceAll(" ", "");
    }

    /**
     * 用来检查原式是否合法的函数。这个函数写得比较简单，主要检查是否含有非法字符和括号是否成套。
     * 这个函数还有待完善，有些情况我没有处理，比如式子第一个字符就是“）”，等等，所以在测试的时候，请尽量输入合法的四则运算式子
     */
    private void check() {
        int right = 0, left = 0;
        for (int i = 0; i < str.length(); i++) {
            char a = str.charAt(i);
            if (a != '0' && a != '1' && a != '2' && a != '3' && a != '4' &&
                a != '5' &&
                a != '6' && a != '7' && a != '8' && a != '9' && a != '+' &&
                a != '-' &&
                a != '*' && a != '/' && a != '(' && a != ')' && a != '.') {
            }
            if (a == '(')
                left++;
            else if (a == ')')
                right++;
        }
        if (left != right) {
            // System.exit(0);
        }

        LinkedList stack = new LinkedList();
        int curPos = 0;
        String beforePart = "";
        String afterPart = "";
        boolean isRightFormat = true;
        String myStr = str;
        while (isRightFormat &&
               (myStr.indexOf('(') >= 0 || myStr.indexOf(')') >= 0)) {
            curPos = 0;
            char[] ary = myStr.toCharArray();
            int len = ary.length;
            for (int i=0; i<len; i++) {
                char s = ary[i];
                if (s == '(') {
                    stack.add(new Integer(curPos));
                } else if (s == ')') {
                    if (stack.size() > 0) {
                        beforePart = myStr.substring(0, ((Integer)stack.getLast()).intValue());
                        afterPart = myStr.substring(curPos + 1);
                        String calculator = myStr.substring(((Integer)stack.getLast()).intValue() + 1,
                                curPos);
                        myStr = beforePart + "[" + calculator + "]" +
                                  afterPart;
                        stack.clear();
                        break;
                    } else {
                        myStr = myStr.replaceAll("\\[", "(");
                        myStr = myStr.replaceAll("\\]", ")");
                        LogUtil.getLog(getClass()).info("有未关闭的右括号！位置在 " + myStr.substring(0, i+1));
                        isRightFormat = false;
                    }
                }
                curPos++;
            }
            if (stack.size() > 0) {
                myStr = myStr.replaceAll("\\[", "(");
                myStr = myStr.replaceAll("\\]", ")");
                LogUtil.getLog(getClass()).info("有未关闭的左括号！位置在 " + myStr.substring(0, ((Integer)stack.getLast()).intValue()+1));
                break;
            }
        }
    }

    public static void main(String args[]) {
    	String str1 = "{\"sourceForm\":\"sales_customer\", \"destForm\":\"access_control\", \"filter\":\"customer like {$@client}\", \"maps\":[{\"sourceField\": \"customer\", \"destField\":\"c\"},{\"sourceField\": \"address\", \"destField\":\"description\"}]}";
        try {
			JSONObject json = new JSONObject(str1);
		} catch (JSONException e) {
            LogUtil.getLog(CalculateString.class).error(e);
		}
    	
    	String url = "http://anotherbug.blog.chinajavaworld.com/entry/4545/0/";
        // String url = "http://141.60.16.99/entry/4545/0/";

        // String patStr = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}$";
        String patStr = "";
        patStr = "(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv|mob)";
        patStr = "/^[h][t]{2}[p][:][\\/][\\/][w]{3}[\\.][0-9A-Za-z]+[\\.][a-z]{2,3}([\\/][0-9A-Za-z]+)+([\\/][0-9A-Za-z]+[.][a-z]+)?$";

        Pattern pat = Pattern.compile(patStr,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(url);
        if (matcher.find())
            LogUtil.getLog(CalculateString.class).info(matcher.group());

        if (true)
            return;

        Interpreter bsh = new Interpreter();
        try {
            // hjje=3444.0;fkfs="转账";fkfs="转账";userName="张建军";
            // hjje>3000 && (fkfs=="银行" || fkfs=="转帐")

            bsh.eval("hjje=3444.0;fkfs=\"转账\";fkfs=\"转账\";userName=\"张建军\";");
            // bsh.eval("hjje=3444.0;fkfs=\"转帐\";fkfs=\"转帐\";userName=\"张建军\";");

            bsh.eval("re=(hjje>3000 && (fkfs==\"银行\" || fkfs==\"转帐\"));");

            /*
            bsh.eval("hjje=3444.0;fkfs=\"转帐\";fkfs=\"转帐\";userName=\"张建军\";");
            bsh.eval("re=(hjje>3000 && (fkfs==\"银行\" || fkfs==\"转帐\"));");
            */
            // bsh.eval("re=(hjje>3000);"); // && (fkfs==\"银行\" || fkfs==\"转帐\"));");

            // bsh.eval("fkfs=\"" + StrUtil.escape("转账") + "\";");
            // bsh.eval("re=(fkfs==\"" + StrUtil.escape("转账") + "\");");

            // bsh.eval("fkfs=\"%u8f6c%u8d26\";");
            // bsh.eval("re=(fkfs==\"%u8f6c%u8d26\");");
            /*
            bsh.eval("fkfs=\"转账\";");
            bsh.eval("re=(fkfs==\"转账\");");
            */
            boolean re = ((Boolean) bsh.get("re")).booleanValue();

            LogUtil.getLog(CalculateString.class).info("re=" + re + " " + StrUtil.escape("转账"));
        } catch (EvalError ex) {
            LogUtil.getLog(CalculateString.class).error(ex);
        }


        if (true)
            return;
        String str = "1+2/3+(5*2)*(1-3)";
        // str = "1+(-2))*((3+5)/2)-1";
        str = "2*(100+200)/(199+1)-(2-10/5-3)";

        CalculateString cs = new CalculateString(str);
        long k = System.currentTimeMillis();
        cs.caculate();
        long interval = System.currentTimeMillis() - k;
        LogUtil.getLog(CalculateString.class).info(" 用时：" + interval);

        String scriptStr = "{$电话}==\"phone\" && {$电脑}>3";
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(scriptStr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);
            LogUtil.getLog(CalculateString.class).info(fieldTitle);
        }
    }
}
