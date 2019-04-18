package com.cloudwebsoft.framework.test;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.aop.Pointcut.RegexpMethodPointcut;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import cn.js.fan.util.StrUtil;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import cn.js.fan.web.Global;

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
public class ClientTest {

    public static void main(String[] args) {
        String htmlStr = "自由发文<style>.flowTable{width:50%;margin:0px auto;border-collapse:collapse;font-size:12px;margin-bottom:10px;margin-top:0px;float:left;clear:both;}.flowTable td{border:1px solid #cccccc;padding:1px 3px;height:22px;}</style><div>";
        String regEx_style="<style>[^>]*?<\\/style>"; //定义style的正则表达式
        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签

        System.out.println("htmlStr=" + htmlStr);



        String sql = "select id from users";
        Pattern pat = Pattern.compile(
                "(\\[([A-Za-z0-9]+)\\])?(.*+)", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        String db = null;
        Matcher mat = pat.matcher(sql);
        if (mat.find()) {
            db = mat.group(2);
            sql = mat.group(3);

            System.out.println("group=" + mat.group(1));
            System.out.println("db=" + db);
            System.out.println("sql=" + sql);
        }

        String content = "fffff<div style=\"display:none\">123456</div>asdf<input name=\"ddd\" style='width:15' type=\"hidden\" >kkk";
        content = "asdfasdf<input type=\"hidden\" name=\"checkItemsSel\" id=\"checkItemsSel\" value=\"\" />asdfasdf";
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        replacementStr = "";
        patternStr = "<div style=['|\"]?display:none['|\"]?>(.*?)</div>"; //表情
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(replacementStr);

        patternStr = "<input.*?type=['|\"]?hidden['|\"]?.*?>"; //表情
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(replacementStr);

        System.out.println(content);

        if (true)
            return;

        ProxyFactory proxyFactory = new ProxyFactory("com.cloudwebsoft.framework.test.StudentInfoServiceImpl");
        Advisor adv = new Advisor();
        TestBeforeAdvice tba = new TestBeforeAdvice();
        adv.setAdvice(tba);
        // adv.setPointcut(new MethodNamePointcut("find", true));
        adv.setPointcut(new RegexpMethodPointcut("fi.*"));
        proxyFactory.addAdvisor(adv);
        // proxyFactory.addAdvisor(adv);
        StudentInfoService studentInfo = (StudentInfoService) proxyFactory.getProxy();
        studentInfo.findInfo("阿飞");

        String a = "|";
        System.out.print("ary=" + StrUtil.split(a, "\\|").length);
    }
}
