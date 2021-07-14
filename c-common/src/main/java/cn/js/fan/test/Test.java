package cn.js.fan.test;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.NetUtil;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class Test {
    public Test() {
    }

    public static void main(String[] args) throws Exception {
    	boolean re = "12+1".matches("[0-9]+.*[0-9]*");
    	re = "+121.1".matches("[-+]{0,1}\\d+\\.?\\d*");
    	System.out.println("re=" + re);
    	
    	if (true)
    		return;
    	
    	// 218.6.43.27
    	System.out.println(NetUtil.isInnerIP("218.6.43.27"));
    	System.out.println(NetUtil.isInnerIP("192.168.1.1"));
    	System.out.println(NetUtil.isInnerIP("127.0.0.1"));
    	System.out.println(NetUtil.isInnerIP("localhost"));
    	
    	if (true) return;
    	
        System.out.println(StrUtil.escape("\""));

        System.out.println(StrUtil.unescape("&rqduo;"));

        String str2 = "<p>";
	str2 += "<font face=\"微软雅黑\" size=\"3\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;为提高公司全员流程参与积极性，主动发现流程缺陷，提出流程优化建议，共同推进流程优化工作，特制定员工流程积分管理办法，具体如下：</font></p>";

        String afterDecoding = StringEscapeUtils.unescapeHtml3(str2);
        System.out.println(afterDecoding);

        // String str = "<img onload='window.location.href=dd'>";
        String str = "bbbbbbbb<img onload=window.ff.href=dd>xxxxxxxxxxxxxx<img onload='window.ff.href=dd' src=\"/cwbbs/aa1234.jsp\" onclick=\"window.open('/cwbbs/aa1234.jsp')\">eeeeeeeeeeeee";

        //  str = "<BR><IMG style=\"CURSOR: hand\" onclick=\"window.open('/cwbbs/forum/upfile/2008/2/1204079031859239692486.gif')\" alt=点击开新窗口欣赏 src=\"http://localhost:8080/cwbbs/forum/upfile/2008/2/1204079031859239692486.gif\" onload=\"<b>javascript</b> :if(this.width>screen.width-333)this.width=screen.width-333\"><BR>        ";

        String pat = "<>";
        Pattern p = Pattern.compile(pat,
                                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        String content = "a<>aaaaaaaaaaaaaaaa";
        String s = "yyyy$aaa";
        s = s.replaceAll("\\$", "\\\\\\$");

        System.out.println("s=" + s);

        Matcher m = p.matcher(content);
        content = m.replaceFirst(s);
        System.out.println(content);

        // str = "3333333333333333<BR><IMG style=\"CURSOR: hand\" onclick=\"window.open('/cwbbs/forum/upfile/2008/2/1204088651953287824569.gif')\" alt=点击开新窗口欣赏 src=\"http://localhost:8080/cwbbs/forum/upfile/2008/2/1204088651953287824569.gif\" onload=\"if(this.width>screen.width-333)this.width=screen.width-333\"><BR><BR><IMG style=\"CURSOR: hand\" onclick=\"window.open('/cwbbs/forum/upfile/2008/2/12040886583591871965919.gif')\" alt=点击开新窗口欣赏 src=\"http://localhost:8080/cwbbs/forum/upfile/2008/2/12040886583591871965919.gif\" onload=\"if(this.width>screen.width-333)this.width=screen.width-333\"><BR>";
        // System.out.print(AntiXSS.antiXSS(str));

        System.out.println(new String(SecurityUtil.decodehexstr(
                "2938351972363fc3", "bluewind".getBytes())));


            String regEx = "[' ']+"; //一个或多个空格
            Pattern pp = Pattern.compile(regEx);
            Matcher mm = pp.matcher("a   b     c       d");
            System.out.println(mm.replaceAll(" ")); //替換為一個空格
    }



}
