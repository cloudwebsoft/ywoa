package com.redmoon.oa.flow;

import com.cloudwebsoft.framework.util.LogUtil;

import java.util.*;

/**
 *
 * <p>Title:函数解析 </p>
 *
 * <p>Description: 四则运算</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormulaCalculator {
    private int leftBracket = 0; //左括号个数
    private int rightBracket = 0; //右括号个数
    private int startL = 0;
    private int startR = 0;
    private double answer = 0;
    private String leftNumber = "0";
    private String rightNumber = "0";
    public String lastError = "";
    private String formula = "";
    private Vector list = new Vector();

    public FormulaCalculator(String calRule) {
        encodeFormula(calRule);
    }

    /**
     * 计算左括号数
     * @param calRule String
     * @return int
     */
    private int getLeftBracket(String calRule) {
        leftBracket = 0;
        startL = calRule.indexOf("(");
        if (startL != -1) {
            calRule = calRule.substring(startL + 1, calRule.length());
        } while (startL != -1) {
            leftBracket++;
            startL = calRule.indexOf("(");
            calRule = calRule.substring(startL + 1, calRule.length());
        }
        // log("leftBracket=" + leftBracket);
        return leftBracket;
    }

    /**
     * 计算右括号数
     * @param calRule String
     * @return int
     */
    private int getRightBracket(String calRule) {
        rightBracket = 0;
        startR = calRule.indexOf(")");
        if (startR != -1) {
            calRule = calRule.substring(startR + 1, calRule.length());
        } while (startR != -1) {
            rightBracket++;
            startR = calRule.indexOf(")");
            calRule = calRule.substring(startR + 1, calRule.length());
        }
        return rightBracket;
    }

    /**
     * 对算式进行编码，替换减号为`，在算式外加()
     * @param calRule String
     */
    public void encodeFormula(String calRule) {
        // 替换减号为`
        formula = replaceSubtration(calRule.trim());
        formula = "(" + formula + ")";
        log("setFormula formula=" + formula);
    }

    /**
     * 设置调试模式
     * @param debug boolean
     */
    public void setDebug(boolean debug) {
        FormulaCalculator.debug = debug;
    }

    /**
     * 为了使函数中支持负数，使用“`”表示减号，使用“-”表示负号
     * @param vstr String
     * @return String
     */
    private String replaceSubtration(String vstr) {
        String tmp = "";
        StringBuilder result = new StringBuilder();
        int startS = vstr.indexOf("-");
        while (startS != -1) {
            startS = vstr.indexOf("-");
            if (startS > 0) {
                tmp = vstr.substring(startS - 1, startS);
                if (!"+".equals(tmp) && !"-".equals(tmp) && !"*".equals(tmp) &&
                    !"/".equals(tmp) &&
                    !"(".equals(tmp)) {
                    result.append(vstr.substring(0, startS)).append("`");
                } else {
                    result.append(vstr.substring(0, startS + 1));
                }
            } else {
                result.append(vstr.substring(0, startS + 1));
            }
            vstr = vstr.substring(startS + 1);
        }
        result.append(vstr);
        return result.toString();
    }

    /**
     * 解码算式
     * @return String
     */
    public String decodeFormula() {
        return formula.replace('`', '-').substring(1, formula.length() - 1);
    }

    public boolean isDebug() {
        return debug;
    }

    /**
     * 对比左右括号个数
     * @return boolean
     */
    private boolean compareLRBracket() {
        int lb = getLeftBracket(formula);
        int rb = getRightBracket(formula);
        boolean re = false;
        if (lb == rb) {
            re = true;
        } else if (lb > rb) {
            lastError = "左括弧的个数多于右括弧，请检查！";
            re = false;
        } else {
            lastError = "左括弧的个数少于右括弧，请检查！";
            re = false;
        }

        LinkedList stack = new LinkedList();
        int curPos = 0;
        String beforePart = "";
        String afterPart = "";
        boolean isRightFormat = true;
        String myStr = decodeFormula();
        while (isRightFormat &&
               (myStr.indexOf('(') >= 0 || myStr.indexOf(')') >= 0)) {
            curPos = 0;
            char[] ary = myStr.toCharArray();
            int len = ary.length;
            for (int i = 0; i < len; i++) {
                char s = ary[i];
                if (s == '(') {
                    stack.add(new Integer(curPos));
                } else if (s == ')') {
                    if (stack.size() > 0) {
                        beforePart = myStr.substring(0,
                                ((Integer) stack.getLast()).intValue());
                        afterPart = myStr.substring(curPos + 1);
                        String calculator = myStr.substring(((Integer) stack.
                                getLast()).intValue() + 1,
                                curPos);
                        myStr = beforePart + "[" + calculator + "]" +
                                afterPart;
                        stack.clear();
                        break;
                    } else {
                        myStr = myStr.replaceAll("\\[", "(");
                        myStr = myStr.replaceAll("\\]", ")");
                        lastError = "有未关闭的右括号！位置在 " + myStr.substring(0, i + 1);
                        // isRightFormat = false;
                        return false;
                    }
                }
                curPos++;
            }
            if (stack.size() > 0) {
                myStr = myStr.replaceAll("\\[", "(");
                myStr = myStr.replaceAll("\\]", ")");
                lastError = "有未关闭的左括号！位置在 " +
                            myStr.substring(0,
                                            ((Integer) stack.getLast()).intValue() +
                                            1);
                return false;
            }
        }
        return re;
    }

    /**
     * 检查函数中是否存在非法字符如(+、-)等
     * @return boolean
     */
    private boolean checkFormula() {
        String str = formula;
        for (int i = 0; i < str.length(); i++) {
            char a = str.charAt(i);
            if (a != '0' && a != '1' && a != '2' && a != '3' && a != '4' &&
                a != '5' &&
                a != '6' && a != '7' && a != '8' && a != '9' && a != '+' &&
                a != '-' &&
                a != '*' && a != '/' && a != '(' && a != ')' && a != '.' &&
                a != '`') {
                lastError = a + " - 字符非法";
                return false;
            }
        }

        if (formula.indexOf("()") > 0) {
            lastError = "函数中存在非法字符()";
            return false;
        }
        return true;
    }

    /**
     * 检查算式是否合法有效
     * @return boolean
     */
    public boolean checkValid() {
        if ((formula == null) || (formula.trim().length() <= 0)) {
            lastError = "请设置属性calRule!";
            return false;
        }
        return (compareLRBracket() && checkFormula());
    }

    /**
     * 返回函数执行结果
     * @return double
     */
    public double getResult() {
        String formulaStr = "", calRule = "";
        double value = 0.0;
        calRule = this.formula;
        if (checkValid()) {
            for (int i = 0; i < leftBracket; i++) {
                int iStart = calRule.lastIndexOf("(") + 1;
                formulaStr = calRule.substring(iStart,
                                               iStart +
                                               calRule.substring(iStart).
                                               indexOf(")")).trim();
                symbolParse(formulaStr);
                value = parseString();
                iStart = calRule.lastIndexOf("(");
                int iEnd = calRule.substring(iStart).indexOf(")") + 1;
                calRule = calRule.substring(0, iStart).trim() +
                          value +
                          calRule.substring(iStart + iEnd, calRule.length()).
                          trim();
            }
        }
        double tmp = Math.pow(10, 10);
        value = Math.round(value * tmp) / tmp;
        return value;
    }

    private void symbolParse(String str) {
       list = getSymbols(str);
    }

    private double parseString() throws NumberFormatException,
            StringIndexOutOfBoundsException {
        try {
            caculateNumber();
            return answer;
        } catch (Exception e) {
            lastError = "错误：" + e.getMessage();
            return 0.0;
        }
    }

    private void caculateNumber() {
        // 处理除法
        int spd = list.indexOf("/");
        while (spd != -1) {
            leftNumber = list.get(spd - 1).toString();
            rightNumber = list.get(spd + 1).toString();
            list.remove(spd - 1);
            list.remove(spd - 1);
            list.remove(spd - 1);
            double ln = Double.valueOf(leftNumber).doubleValue();
            double rn = Double.valueOf(rightNumber).doubleValue();
            double answer = ln / rn;
            list.add(spd - 1, String.valueOf(answer));
            spd = list.indexOf("/");
        }
        // 处理乘法
        int spm = list.indexOf("*");
        while (spm != -1) {
            leftNumber = list.get(spm - 1).toString();
            rightNumber = list.get(spm + 1).toString();
            list.remove(spm - 1);
            list.remove(spm - 1);
            list.remove(spm - 1);
            double ln = Double.valueOf(leftNumber).doubleValue();
            double rn = Double.valueOf(rightNumber).doubleValue();
            double answer = ln * rn;
            list.add(spm - 1, String.valueOf(answer));
            spm = list.indexOf("*");
        }
        // 处理减法
        int sps = list.indexOf("`");
        while (sps != -1) {
            leftNumber = list.get(sps - 1).toString();
            rightNumber = list.get(sps + 1).toString();
            list.remove(sps - 1);
            list.remove(sps - 1);
            list.remove(sps - 1);
            double ln = Double.valueOf(leftNumber).doubleValue();
            double rn = Double.valueOf(rightNumber).doubleValue();
            double answer = ln - rn;
            list.add(sps - 1, String.valueOf(answer));
            sps = list.indexOf("`");
        }
        // 处理加法
        int spa = list.indexOf("+");
        while (spa != -1) {
            leftNumber = list.get(spa - 1).toString();
            rightNumber = list.get(spa + 1).toString();
            list.remove(spa - 1);
            list.remove(spa - 1);
            list.remove(spa - 1);
            double ln = Double.valueOf(leftNumber).doubleValue();
            double rn = Double.valueOf(rightNumber).doubleValue();
            double answer = ln + rn;
            list.add(spa - 1, String.valueOf(answer));
            spa = list.indexOf("+");
        }
        if (list.size() != 0) {
            String result = list.get(0).toString();
            if (result == null || result.length() == 0) {
                result = "0";
            }
            answer = Double.parseDouble(list.get(0).toString());
        }
    }

    public static void main(String[] args) {
        String str = "1+2/3+(5*2)*(1-3)";
        str = "1/2-2*(100+200)/(199+1)-(2-10/5-3)";
        // str = "1+2/3+(5*2)*(1-3)";
        // str = "1+(-2))*((3+5)/2)-1";
        // str = "+1+2/(-3))+(5*(-2/2))*(1-3)";

        long k = System.currentTimeMillis();
        FormulaCalculator fc = new FormulaCalculator(str);
        double r = fc.getResult();
        long interval = System.currentTimeMillis() - k;
    }

    public static void log(String str) {
        if (debug) {
            LogUtil.getLog(FormulaCalculator.class).info(str);
        }
    }

    public String getLastError() {
        return lastError;
    }

    /**
     * 取得算式中的项，算式中不含括号，不含算子
     * @param str String
     */
    public static Vector getSymbols(String str) {
        log("symbolParse:str=" + str);
        if (str.startsWith("+"))
            str = str.substring(1); // 去掉开头的+号
        Vector list = new Vector();
        int curPos = 0;
        int prePos = 0;
        char[] ary = str.toCharArray();
        int len = ary.length;
        for (int i = 0; i < len; i++) {
            char s = ary[i];
            if (s == '+' || s == '`' || s == '*' || s == '/') {
                log("str.substring(" + prePos + ", " + curPos +
                    ").trim()=" + str.substring(prePos, curPos)
                    .trim());
                if (prePos<curPos) {
                    list.add(str.substring(prePos, curPos).trim());
                }
                list.add("" + s);
                prePos = curPos + 1;
            }
            curPos++;
        }
        if (prePos <= len - 1)
            list.add(str.substring(prePos).trim());
        if (debug) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                String s = (String) ir.next();
            }
        }
        return list;
    }

    /**
     * 取得算式中的项，算式中不含括号，注意式中的-号未作处理，未变换为`
     * @param str String
     */
    public static Vector getSymbolsWithBracket(String str) {
        // 去除空格
        str = str.replaceAll(" ", "");
        log("symbolParse:str=" + str);
        if (str.startsWith("+"))
            str = str.substring(1); // 去掉开头的+号
        Vector list = new Vector();
        int curPos = 0;
        int prePos = 0;
        char[] ary = str.toCharArray();
        int len = ary.length;
        for (int i = 0; i < len; i++) {
            char s = ary[i];
            if (s == '+' || s == '-' || s == '*' || s == '/' || s=='(' || s==')') {
                log("str.substring(" + prePos + ", " + curPos +
                    ").trim()=" + str.substring(prePos, curPos)
                    .trim());
                if (prePos<curPos)
                    list.add(str.substring(prePos, curPos).trim());
                list.add("" + s);
                prePos = curPos + 1;
            }
            curPos++;
        }
        if (prePos <= len - 1)
            list.add(str.substring(prePos).trim());
        if (debug) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                String s = (String) ir.next();
            }
        }
        return list;
    }

    public static boolean isOperator(String str) {
        if (str.equals("+") || str.equals("*") || str.equals("/") || str.equals("-") || str.equals("(") || str.equals(")")) {
            return true;
        }
        else {
            return false;
        }
    }

    private static boolean debug = true;
}

