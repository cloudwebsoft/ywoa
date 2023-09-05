package com.redmoon.oa.flow;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.util.LinkedList;
import java.util.ArrayList;

import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.io.*;
import org.jdom.*;
import java.util.Iterator;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FormulaParser2 {
    private boolean isRightFormat = true;

    public double getResult(String formula) {
        double returnValue = 0;
        try {
            returnValue = doAnalysis(formula);
        } catch (NumberFormatException e) {
            LogUtil.getLog(getClass()).error("函数格式有误1，请检查:" + formula);
            LogUtil.getLog(getClass()).error(e);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        if (!isRightFormat) {
            LogUtil.getLog(getClass()).error("函数格式有误，请检查:" + formula);
        }
        return returnValue;
    }

    private double doAnalysis(String formula) {
        double returnValue = 0;
        LinkedList stack = new LinkedList();

        int curPos = 0;
        String beforePart = "";
        String afterPart = "";
        String calculator = "";
        isRightFormat = true;
        while (isRightFormat &&
               (formula.indexOf('(') >= 0 || formula.indexOf(')') >= 0)) {
            curPos = 0;
            char[] ary = formula.toCharArray();
            int len = ary.length;
            for (int i=0; i<len; i++) {
                char s = ary[i];
                if (s == '(') {
                    stack.add(new Integer(curPos));
                } else if (s == ')') {
                    if (stack.size() > 0) {
                        beforePart = formula.substring(0, ((Integer)stack.getLast()).intValue());
                        afterPart = formula.substring(curPos + 1);
                        calculator = formula.substring(((Integer)stack.getLast()).intValue() + 1,
                                curPos);
                        formula = beforePart + doCalculation(calculator) +
                                  afterPart;
                        stack.clear();
                        break;
                    } else {
                        LogUtil.getLog(getClass()).error("有未关闭的右括号");
                        isRightFormat = false;
                    }
                }
                curPos++;
            }
            if (stack.size() > 0) {
                LogUtil.getLog(getClass()).error("有未关闭的左括号");
                break;
            }
        }
        if (isRightFormat) {
            returnValue = doCalculation(formula);
        }
        return returnValue;
    }

    private double doCalculation(String formula) {
        ArrayList values = new ArrayList();
        ArrayList operators = new ArrayList();
        int curPos = 0;
        int prePos = 0;
        char[] ary = formula.toCharArray();
        int len = ary.length;
        for (int i=0; i<len; i++) {
            char s = ary[i];
            if (s == '+' || s == '-' || s == '*' || s == '/') {
                values.add(new Double(Double.parseDouble(formula.substring(prePos, curPos)
                                              .trim())));
                operators.add("" + s);
                prePos = curPos + 1;
            }
            curPos++;
        }
        values.add(new Double(Double.parseDouble(formula.substring(prePos).trim())));
        char op;
        for (curPos = operators.size() - 1; curPos >= 0; curPos--) {
            op = ((String)operators.get(curPos)).charAt(0);
            switch (op) {
            case '*':
                values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() * ((Double)values.get(curPos + 1)).doubleValue()));
                values.remove(curPos + 1);
                values.remove(curPos + 1);
                operators.remove(curPos);
                break;
            case '/':
                values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() / ((Double)values.get(curPos + 1)).doubleValue()));
                values.remove(curPos + 1);
                values.remove(curPos + 1);
                operators.remove(curPos);
                break;
            }
        }
        for (curPos = operators.size() - 1; curPos >= 0; curPos--) {
            op = ((String)operators.get(curPos)).charAt(0);
            switch (op) {
            case '+':
                values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() + ((Double)values.get(curPos + 1)).doubleValue()));
                values.remove(curPos + 1);
                values.remove(curPos + 1);
                operators.remove(curPos);
                break;
            case '-':
                values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() - ((Double)values.get(curPos + 1)).doubleValue()));
                values.remove(curPos + 1);
                values.remove(curPos + 1);
                operators.remove(curPos);
                break;
            }
        }
        return ((Double)values.get(0)).doubleValue();
    }

     public static void main(String[] args) {
         String content = "xx<IMG onclick='SelectDate(\"date_ng\",\"yyyy-mm-dd\")' name=date_ng_btnImg align=absMiddle src=\"http://localhost:8080/oa/images/form/calendar.gif\" width=26 height=26>ee<input abc deb ><IMG onclick='SelectDate(\"date_ng\",\"yyyy-mm-dd\")' name=date_ng_btnImg align=absMiddle src=\"http://localhost:8080/oa/images/form/calendar.gif\" width=26 height=26>kkk";
         String pat = "<img([^>]*?)calendar.gif([^>]*?)>";

         Pattern pattern = Pattern.compile(pat,
                                   Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
         Matcher matcher = pattern.matcher(content);
         content = matcher.replaceAll("");
         System.out.print(content);

         if (true) return;

         SAXBuilder parser = new SAXBuilder();
         String str = "<?xml version='1.0' encoding='utf-8'?>";
         str += "<actions>";
         str += "<action internalName='abc'>";
         str += "<kind>subFlow</kind>";
         str += "<property>";
         str += "{'subFlowTypeCode':'subFlowTypeCode',toSubMap:{parentField:subField},toParentMap:{subField:parentField}}";
         str += "</property>";
         str += "</action>";
         str += "</actions>";

         str = "<?xml version=\"1.0\"?><actions><action internalName=\"757b92a149ca45cfaa7caa0568f13d91\"><property>{\"subFlowTypeCode\":\"pc\",\"parentToSubMap\":[{\"parentField\":\"ztc\", \"subField\":\"target\", \"parentTitle\":\"主题词\", \"subTitle\":\"目的地\"}], \"subToParentMap\":[{\"parentField\":\"ngr\", \"subField\":\"person\", \"parentTitle\":\"拟稿人\", \"subTitle\":\"用车人\"},{\"parentField\":\"ztc\", \"subField\":\"target\", \"parentTitle\":\"主题词\", \"subTitle\":\"目的地\"}]}</property></action><action internalName=\"ecc1c3b614a24db2af6af64aeb961df6\"><property>{\"subFlowTypeCode\":\"pc\",\"parentToSubMap\":[{\"parentField\":\"ngr\", \"subField\":\"person\", \"parentTitle\":\"拟稿人\", \"subTitle\":\"用车人\"}], \"subToParentMap\":[{\"parentField\":\"ztc\", \"subField\":\"reason\", \"parentTitle\":\"主题词\", \"subTitle\":\"事由\"}]}</property></action></actions>";

         try {
             org.jdom.Document doc = parser.build(new InputSource(new StringReader(str)));

             Element root = doc.getRootElement();
             Iterator ir = root.getChildren().iterator();
             if (ir.hasNext()) {
                 Element e = (Element)ir.next();
                 String prop = e.getChildText("property");
                 JSONObject jobj = new JSONObject(prop);
                 JSONArray ary = jobj.getJSONArray("parentToSubMap");
             }
         } catch (IOException ex) {
             LogUtil.getLog(FormulaParser2.class).error(ex);
         } catch (JDOMException ex) {
             LogUtil.getLog(FormulaParser2.class).error(ex);
         } catch (JSONException ex) {
            LogUtil.getLog(FormulaParser2.class).error(ex);
        }

        if (true)
             return;

         FormulaParser2 fc = new FormulaParser2();
         str = "1+2/3+(5*(-2))*(1-3)";
         long k = System.currentTimeMillis();
         double r = fc.getResult(str);
         long interval = System.currentTimeMillis() - k;
         LogUtil.getLog(FormulaParser2.class).info( str + "=" + r + " 用时：" + interval);
     }

}

