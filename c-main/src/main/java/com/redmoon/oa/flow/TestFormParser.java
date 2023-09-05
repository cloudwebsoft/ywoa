package com.redmoon.oa.flow;


import com.cloudwebsoft.framework.util.LogUtil;

import java.util.regex.*;
import java.util.Vector;
import java.util.Iterator;

public class TestFormParser {
    public static void main(String[] args) throws Exception {
        Pattern patt = Pattern.compile("Windows NT 10\\.0");
        Matcher mat = patt.matcher("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
        if(mat.find()){
            LogUtil.getLog(TestFormParser.class).info("find");
        }
        else {
            LogUtil.getLog(TestFormParser.class).info("not find");
        }
        if (true) return;

        // String content = "<INPUT title=单选框 value=测试0 CHECKED type=radio name=dxk canNull=\"1\" fieldType=\"0\" maxV=\"\" maxT=\"x=\" minV=\"\" minT=\"d=\"><INPUT title=单选框 value=测试1 CHECKED type=radio name=dxk canNull=\"1\" fieldType=\"0\" maxV=\"\" maxT=\"x=\" minV=\"\" minT=\"d=\">&nbsp;测试1 &nbsp; <INPUT title=单选框 value=测试2 CHECKED type=radio name=dxk canNull=\"1\" fieldType=\"0\" maxV=\"\" maxT=\"x=\" minV=\"\" minT=\"d=\">&nbsp;测试2&nbsp;<INPUT title=单选框 value=测试3 type=radio name=dxk canNull=\"1\" fieldType=\"0\" maxV=\"\" maxT=\"x=\" minV=\"\" minT=\"d=\">测试3&nbsp;</P>";
        String content = "<P><INPUT title=aaa value=宏控件：客户选择窗体 name=aaa canNull=\"1\" kind=\"macro\" macroType=\"macro_customer_list_win\" macroDefaultValue=\"\"><INPUT title=单选框 value=测试1 CHECKED type=radio name=dxk canNull=\"1\" minT=\"d=\" minV=\"\" maxT=\"x=\" maxV=\"\" fieldType=\"0\">&nbsp;测试1 &nbsp;<INPUT title=单选框 value=测试2 type=radio name=dxk canNull=\"1\" minT=\"d=\" minV=\"\" maxT=\"x=\" maxV=\"\" fieldType=\"0\">&nbsp;测试2&nbsp;<INPUT title=单选框 value=测试3 type=radio name=dxk canNull=\"1\" minT=\"d=\" minV=\"\" maxT=\"x=\" maxV=\"\" fieldType=\"0\">测试3&nbsp;</P>";
        // <INPUT title=单选框 value=测试1 CHECKED type=radio name=dxk canNull="1" fieldType="0" maxV="" maxT="x=" minV="" minT="d=">
        // String pat = "<input([^>]*?) title=['|\"]?(.*?)['|\"]? .*?(value=['|\"]?([^'\" >]*?)['|\"]?)?([^>]*?)name=['|\"]?(dxk)['|\"]?(.*?)>";
        String pat = "<input title=['|\"]?(\\S*)['|\"]? value=['|\"]?(\\S*)['|\"]?([^>]*?)name=['|\"]?(dxk)['|\"]?(.*?)>";
        String ieVersion = "8";
        if (ieVersion.equals("6")) {
            // <INPUT title=来文单位 style="WIDTH: 115px; HEIGHT: 21px" size=15 value=vvv name=lwdw canNull="0" maxV="300" maxT="x=" minV="1" minT="d=">
            pat = "<input([^>]*?) title=['|\"]?([^>'\" ]+)['|\"]?.*? (value=['|\"]?([^'\" >]*)['|\"]?)? ?name=['|\"]?(dxk)['|\"]?(.*?)>";
        } else if (ieVersion.equals("9")) {
            // 注意下行中(value前的.*?一定要换为 ?才行
            // pat = "<input([^>]*?) title=['|\"]?(.*?)['|\"]? .*?name=['|\"]?([^>'\" ]+)['|\"]?.*?(value=['|\"]?([^'\" >]*)['|\"]?)?(.*?)>";
            pat = "<input([^>]*?) title=['|\"]?(.*?)['|\"]? .*?name=['|\"]?([^>'\" ]+)['|\"]? ?(value=['|\"]?(dxk)['|\"]?)?(.*?)>";
        }

        // LogUtil.getLog("FormParser").info("getValuesOfRadio pat=" + pat);
        Pattern p = Pattern.compile(pat,
                                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);

            Vector v = new Vector();
            while (m.find()) {
                LogUtil.getLog(TestFormParser.class).info(m.group() + "---" + m.group(1));
                String val = m.group(2);
                v.addElement(val);
            }
            String[] ary = new String[v.size()];
            Iterator ir = v.iterator();
            int i = 0;
            while (ir.hasNext()) {
                String str = (String) ir.next();
                LogUtil.getLog(TestFormParser.class).info("getValuesOfInput:" + str);
                ary[i] = str;
                i++;
            }


        if (true)return;

        /*
                // *、+和?限定符都是贪婪的，因为它们会尽可能多的匹配文字，只有在它们的后面加上一个?就可以实现非贪婪或最小匹配。
                String pat = "<select.*name=['|\"]{0,1}([^'\"> ]*)['|\"]{0,1}.*?title=['|\"]{0,1}([^'\" >]*)['|\"]{0,1}.*?size=['|\"]{0,1}([^'\"1 >]+)['|\"]{0,1}.*?>.*?<option value=['|\"]{0,1}([^'\" >]*)['|\"]{0,1} selected>.*?</option>.*?</select>";
                Pattern p = Pattern.compile(pat, Pattern.DOTALL);
                Matcher m = p.matcher("<select style=\"color\" name=ss' id=\"color\" title=hh' style=\"color\" size='2' value=蓝风><option value=xx selected>xx</option></select>");
                boolean result = m.find();
                while (result) {
                    LogUtil.getLog(getClass()).info("该次查找获得匹配组的数量为：" + m.groupCount());
                    LogUtil.getLog(getClass()).info("group=" + m.group());
                    for (int i = 1; i <= m.groupCount(); i++) {
                        LogUtil.getLog(getClass()).info("第" + i + "组的子串内容为： " + m.group(i));
                    }
                    result = m.find();
                }
         */
//String str = "<SELECT title=a name=a fieldType=\"0\"><OPTION value=b selected>b</OPTION><OPTION value=b>b</OPTION><OPTION value=b>b</OPTION></SELECT>";
//FormParser fp = new FormParser(str);

//str = "<SELECT title=a name=a><OPTION value=b selected>b</OPTION><OPTION value=b>b</OPTION><OPTION value=b>b</OPTION></SELECT>";
//fp = new FormParser(str);

        String str = "<SELECT title=aacc style=\"WIDTH: 60px\" size=5 name=aacc fieldType=\"6\"><OPTION value=1>1</OPTION><OPTION value=2 selected>2</OPTION><OPTION value=3>3</OPTION></SELECT>";
        FormParser fp = new FormParser(str);

        str = "ddd,fff,kkk,,,s";
        ary = str.split(",");
        LogUtil.getLog(TestFormParser.class).info("length=" + ary.length);

        str = "<td width=\"226\" align=\"left\"><select title=\"加班类别\" name=\"jblb\" canNull=\"1\" maxV=\"\" maxT=\"x=\" minV=\"\" minT=\"d=\" fieldType=\"0\"><option selected=\"\" value=\"平时\">平时</option><option value=\"休息日\">休息日</option><option value=\"节假日\">节假日</option></select>&nbsp;</td></tr>";
        fp = new FormParser(str);

        // fp.parseSelect();

        testReplace();
    }


    public static void testGroup() {
        // *、+和?限定符都是贪婪的，因为它们会尽可能多的匹配文字，只有在它们的后面加上一个?就可以实现非贪婪或最小匹配。
        Pattern p = Pattern.compile("(\"ca)(.*?)(t)", Pattern.DOTALL);
        Matcher m = p.matcher("one \"cat,two cats cafts in the yard");
        boolean result = m.find();
        while (result) {
            LogUtil.getLog(TestFormParser.class).info("该次查找获得匹配组的数量为：" + m.groupCount());
            LogUtil.getLog(TestFormParser.class).info("group=" + m.group());
            for (int i = 1; i <= m.groupCount(); i++) {
                LogUtil.getLog(TestFormParser.class).info("第" + i + "组的子串内容为： " + m.group(i));
            }
            result = m.find();
        }
    }

    public static void testReplace() {
        //生成Pattern对象并且编译一个简单的正则表达式"Kelvin"
        Pattern p = Pattern.compile("Kelvin");
        //用Pattern类的matcher()方法生成一个Matcher对象
        Matcher m = p.matcher("Kelvin Li and Kelvin Chan are both working in Kelvin Chen's KelvinSoftShop company");
        StringBuffer sb = new StringBuffer();
        int i = 0;
        //使用find()方法查找第一个匹配的对象
        boolean result = m.find();
        //使用循环将句子里所有的kelvin找出并替换再将内容加到sb里
        while (result) {
            i++;
            m.appendReplacement(sb, "Kevin");
            LogUtil.getLog(TestFormParser.class).info("第" + i + "次匹配后sb的内容是：" + sb);
            //继续查找下一个匹配对象
            result = m.find();
        }
        //最后调用appendTail()方法将最后一次匹配后的剩余字符串加到sb里；
        m.appendTail(sb);
        LogUtil.getLog(TestFormParser.class).info("调用m.appendTail(sb)后sb的最终内容是:" + sb.toString());
    }
}
