package cn.js.fan.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

/**
 *
 * <p>Title: 跨站攻击Cross Site Script过滤</p>
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
public class AntiXSS {
	/*
	  如何过滤签名档？长度限制
	  [img]http://127.0.0.1/bbsxp/admin_user.asp?menu=userok&username=linzi&membercode=5&userlife=1&posttopic=3&money=9&postrevert=0&savemoney=0&deltopic=1&regtime=2005-9-1+1%3A1%3A1&experience=9&country=%D6%D0%B9%FA&&Submit=+%B8%FC+%D0%C2+[/img]
	*/
    static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
            // "<script[^>]*>.*</script[^>]*>", Pattern.CASE_INSENSITIVE);
    		"<[^>]*script[^>]*>.*</script[^>]*>", Pattern.CASE_INSENSITIVE);
    static final Pattern IFRAME_TAG_PATTERN = Pattern.compile("<iframe[^>]*>.*</iframe[^>]*>", Pattern.CASE_INSENSITIVE);

    /**
     *  检测通过框架钓鱼
     *  http://180.101.236.31:8088/flow/form_js/form_js_zs_activity.jsp?code=123<iframe src="http://180.101.236.31:8088/test.jsp"/>
     *  nest_sheet_add_relate.jsp将参数“xm”的值设置为“1234'"><iframe id=1712 src=http://demo.testfire.net/phishing.html>”
     */
    static final Pattern IFRAME_FISHING_TAG_PATTERN = Pattern.compile("< *iframe[^>]*>", Pattern.CASE_INSENSITIVE);

    /**
     *  360检测：88888</script><iframe src=http://xxooxxoo.js>
     *  20201206改，因为攻击脚本可能不带有末尾的标签 >
     *  http://localhost:8093/tzcj/visual/nest_sheet_view.jsp?parentFormCode=xmxxgl_qx&nestFieldName=dw_ks<iframe src=javascript:alert(3647)
     */
    static final Pattern IFRAME_SIMPLE_TAG_PATTERN = Pattern.compile("< *iframe.*", Pattern.CASE_INSENSITIVE);

    // 非线程安全，20141026
    // static final PatternCompiler pc = new Perl5Compiler();
    // static final PatternMatcher matcher = new Perl5Matcher();

    // http://www.bitscn.com/hack/young/200708/108278.html

    public static String antiXSS(String content) {
        return antiXSS(content, true);
    }

    public static String antiXSS(String content, boolean isGet) {
    	// 出现了很奇怪的问题：org.apache.oro.text.regex.MalformedPatternException: Unmatched parentheses.
    	// 因此暂不启用
    	// if (true)
    	// 	return content;

        if (content == null || content.equals("")) {
            return "";
        }

        String old = content;
        String ret = _antiXSS(content, isGet);
        while (ret != null && !ret.equals(old)) {
            old = ret;
            ret = _antiXSS(ret, isGet);
        }
        return ret;
    }

    private static String _antiXSS(String content) {
        return _antiXSS(content, true);
    }

    private static String _antiXSS(String content, boolean isGet) {
        try {
            if (content == null || content.equals("")) {
                return null;
            }

            return stripAllowScriptAccess(stripProtocol(stripCssExpression(
                    stripAsciiAndHex(stripEvent(stripScriptTag(content, isGet))))));
        } catch (Exception e) {
            LogUtil.getLog(AntiXSS.class).error(e);
            return null;
        }
    }

    public static String stripEvent(String content) throws Exception {
        // if (true)  return content;
        // 清洗 onload=\"<b>javascript</b> :，编辑器会自动在onload事件中生成<b>javascript</b> :
        // content = content.replaceAll("onload=\"<b>javascript</b> :", "onload=\"");
    	PatternCompiler pc = new Perl5Compiler();
    	PatternMatcher matcher = new Perl5Matcher();
        String[] events = {"onmouseover", "onmouseout", "onmousedown",
                          "onmouseup", "onmousemove", "ondblclick",
                          "onkeypress", "onkeydown", "onkeyup", "ondragstart",
                          "onerrorupdate", "onhelp", "onreadystatechange",
                          "onrowenter",
                          "onrowexit", "onselectstart", "onunload",
                          "onbeforeunload", "onblur", "onerror", "onfocus",
                          "onresize",
                          "onscroll", "oncontextmenu"}; // onload与onClick在编辑器中有应用
        int len = events.length;
        for (int i=0; i<len; i++) {
        // for (String event : events) {
            org.apache.oro.text.regex.Pattern p = null;
            try {
	            p = pc.compile("([^>]*)("
	                    + events[i] + ")([^>]*)",
	                    Perl5Compiler.CASE_INSENSITIVE_MASK);
            }
            catch(MalformedPatternException e) {
                LogUtil.getLog(AntiXSS.class).error(e);
            }
            if (null != p) {
                content = Util.substitute(matcher, p, new Perl5Substitution(
                        "$1" + events[i].substring(2) + "$3"), content,
                                          Util.SUBSTITUTE_ALL);
            }
        }
        // 当出现onload事件时禁止open、location
        // String patternStr = "([^>]*?)onload=([\"|'](.+?)[\"|'])";
        String patternStr = "([^>]*?)onload\\s*?=([\"|']?(.+?)[\"|']?)"; // 360检测 num"><body ONLOAD=alert(42873)>
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                patternStr,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher mat = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result = mat.find();
        boolean isFind = false;
        while (result) {
            String tagStr = mat.group(0);
            boolean isValid = true;
            if (!tagStr.substring(1, 4).equalsIgnoreCase("img")) {
                isValid = false;
            }
            else {
                // 检查img的onload事件中，是否含有location或者open
                String onloadStr = mat.group(3).toLowerCase();
                if (onloadStr.indexOf("location")!=-1 || onloadStr.indexOf("open")!=-1 || onloadStr.indexOf("alert")!=-1) {
                    isValid = false;
                }
            }
            if (!isValid) {
                String str = "$1load=$2";
                mat.appendReplacement(sb, str);
                mat.appendTail(sb);
                isFind = true;
            }
            result = mat.find();
        }
        if (isFind) {
            content = sb.toString();
        }

        // LogUtil.getLog(getClass()).info("content=" + content);

        // 禁止onclick事件时，标签img除外，但不允许出现location字符，当
        patternStr = "([^>]*)onclick\\s*?=((\")?(.*?)(\")?.*?>?)";
        pattern = java.util.regex.Pattern.compile(
                patternStr,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        mat = pattern.matcher(content);
        sb = new StringBuffer();
        result = mat.find();
        isFind = false;
        while (result) {
            String imgTag = mat.group(0);
            if (!imgTag.substring(1, 4).equalsIgnoreCase("img")) {
                String str = "$1click=$2$3$4";
                mat.appendReplacement(sb, str);
                isFind = true;
            }
            else {
                // 检查img的onClick事件中，是否含有location
                boolean isValid = true;
                String imgOnClick = mat.group(3).toLowerCase();
                if (imgOnClick.indexOf("location")!=-1) {
                    isValid = false;
                }
                if (isValid) {
                    // 检查onClick事件中，是否含有open
                    if (imgOnClick.indexOf("open") != -1) {
                        boolean isSrcEqualsOpenParam = false;
                        // 如果含有open，则open中的内容必须为src的内容
                        String patString =
                                "<img.*?src.*?=\\s*?([^> ]+)\\s*.*?>";
                        java.util.regex.Pattern pat = java.util.regex.Pattern.
                                compile(patString,
                                        Pattern.DOTALL |
                                        Pattern.CASE_INSENSITIVE);
                        Matcher m = pat.matcher(imgTag);
                        if (m.find()) {
                            String imgUrl = m.group(1).replaceAll("\"|'", "");
                            // LogUtil.getLog(getClass()).info(getClass() + " imgUrl=" + imgUrl);

                            // 取出open(...)中的参数
                            patString =
                                    "(<img.*?open\\s*?\\()([^> ]+)(\\)\\s*.*?>)";
                            pat = java.util.regex.Pattern.
                                  compile(patString,
                                          Pattern.DOTALL |
                                          Pattern.CASE_INSENSITIVE);
                            m = pat.matcher(imgTag);
                            if (m.find()) {
                                String param = m.group(2).replaceAll("\"|'", "");
                                // LogUtil.getLog(getClass()).info("param=" + param);

                                if (imgUrl.indexOf(param)!=-1) {
                                    isSrcEqualsOpenParam = true;
                                }
                            }
                        }
                        if (!isSrcEqualsOpenParam) {
                            isValid = false;
                        }
                    }
                }
                if (!isValid) {
                    String str = "$1click=$2";
                    mat.appendReplacement(sb, str);
                    mat.appendTail(sb);
                    isFind = true;
                }
            }
            result = mat.find();
        }
        if (isFind) {
            content = sb.toString();
        }

        return content;
    }

    private static String stripAsciiAndHex(String content) throws Exception {
        // filter &# \00xx
    	PatternCompiler pc = new Perl5Compiler();    	    	
    	PatternMatcher matcher = new Perl5Matcher();    	
        org.apache.oro.text.regex.Pattern p = pc.compile(
                "(<[^>]*)(&#|\\\\00)([^>]*>)",
                Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p) {
            content = Util
                      .substitute(matcher, p, new Perl5Substitution("$1$3"),
                                  content, Util.SUBSTITUTE_ALL);
        }
        return content;
    }

    private static String stripCssExpression(String content) throws Exception {
    	PatternCompiler pc = new Perl5Compiler();    	
    	PatternMatcher matcher = new Perl5Matcher();    	
    	
    	// http://www.yimihome.com/forum/showtopic.jsp?rootid=1741&CPages=1" style="background:expression(alert(1321))" 
        org.apache.oro.text.regex.Pattern p = pc.compile(
                "(<[^>]*style=.*)/\\*.*\\*/([^>]*>)",
                Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p) {
            content = Util.substitute(matcher, p, new Perl5Substitution("$1$2"),
                                  content, Util.SUBSTITUTE_ALL);
        }

        // http://***:8088/flow/macro/macro_js_nestsheet.jsp?op=forRefresh&nestFormCode=tzf&fieldName=< style >html{font-family:expression(alert(568))}</style >
        // Set parameter 'privurl's value to 'http://***:9000/forum/listtopic.jsp?boardcode=tytx"style=foo:expres\73ion(alert(1514))!'
        p = pc.compile(
                    // "(<[^>]*style=[^>]+)(expression|javascript|vbscript|-moz-binding)([^>]*>)",
                    "([^>]*style[^>]*=[^>]+)(expres|expres\\\\sion|expression|xpression|javascript|vbscript|-moz-binding)([^>]*)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p) {
            content = Util.substitute(matcher, p, new Perl5Substitution("$1$3"),
                                  content, Util.SUBSTITUTE_ALL);
        }

        p = pc.compile("(<style[^>]*>.*)/\\*.*\\*/(.*</style[^>]*>)",
                       Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p) {
            content = Util.substitute(matcher, p, new Perl5Substitution("$1$2"),
                                  content, Util.SUBSTITUTE_ALL);
        }

        // username=admin&arrestreason=1234<style >html{font-family:expression(alert(1925))}</style >&arrestday=01&Submit=逮捕
        // Set parameter 'arrestreason's value to '1234<style >html{font-family:expression(alert(823))}</style >' (Variant ID: 3530)
        // Set parameter 'moneyCode's value to 'gold</script ><style/>html{font-family:expression(alert(890))}</style >'
        Pattern pattern = Pattern.compile(
                    "(<.*?style[^>]*>[^>]+)(expres|expres\\\\sion|expression|xpression|javascript|vbscript|-moz-binding)(.*</style.*?>)",
                    Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(content);
        content = m.replaceAll("");

        // 效果不如上面的实现
        /*p = pc.compile(
                    "(< *style[^>]*>[^>]+)(expres|expres\\\\sion|expression|xpression|javascript|vbscript|-moz-binding)(.*</style[^>]*>)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p) {
            content = Util.substitute(matcher, p, new Perl5Substitution("$1$3"),
                                  content, Util.SUBSTITUTE_ALL);
        }*/

        return content;
    }

    private static String stripProtocol(String content) throws Exception {
    	PatternCompiler pc = new Perl5Compiler();
    	PatternMatcher matcher = new Perl5Matcher();
    	
        String[] protocols = {"javascript", "vbscript", "livescript",
                             "ms-its", "mhtml", "data", "firefoxurl", "mocha"};
        int len = protocols.length;
        for (int i=0; i<len; i++) {
        // for (String protocol : protocols) {
            org.apache.oro.text.regex.Pattern p = pc.compile("(<[^>]*)"
                    + protocols[i] + ":([^>]*>)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
            if (null != p) {
                content = Util.substitute(matcher, p, new Perl5Substitution(
                        "$1/$2"), content, Util.SUBSTITUTE_ALL);
            }
        }
        return content;
    }

    private static String stripAllowScriptAccess(String content) throws
            Exception {
    	PatternCompiler pc = new Perl5Compiler();    
    	PatternMatcher matcher = new Perl5Matcher();    	
    	
        org.apache.oro.text.regex.Pattern p = pc.compile(
                "(<[^>]*)AllowScriptAccess([^>]*>)",
                Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p) {
            content = Util.substitute(matcher, p, new Perl5Substitution(
                    "$1Allow_Script_Access$2"), content, Util.SUBSTITUTE_ALL);
        }
        return content;
    }

    public static String stripScriptTag(String content) {
        return stripScriptTag(content, true);
    }

    public static String stripScriptTag(String content, boolean isGet) {
        Matcher m = SCRIPT_TAG_PATTERN.matcher(content);
        content = m.replaceAll("");

        // 过滤通过框架钓鱼
        m = IFRAME_FISHING_TAG_PATTERN.matcher(content);
        content = m.replaceAll("");

        m = IFRAME_TAG_PATTERN.matcher(content);
        content = m.replaceAll("");
        
        m = IFRAME_SIMPLE_TAG_PATTERN.matcher(content);
        content = m.replaceAll("");
        
        // 过滤掉'+alert(695)+'     '==alert(743)+'
        Pattern plusPattern = Pattern.compile("'.*[\\+|=|-]'", Pattern.CASE_INSENSITIVE);
        m = plusPattern.matcher(content);
        content = m.replaceAll("");

        // http://180.101.236.31:9000/admin/organize/organize.jsp?type=list%0Aalert%28154%29%2F%2F
        // type=list换行符\nalert(154)//
        // 因为参数被urldecode过了，所以不需要再按下面的方式检测
        /*Pattern p = Pattern.compile("(%0A)(.*?)(%2F%2F)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");*/
        Pattern p = Pattern.compile("(\n)(.*?)(//)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // admin/organize/organize.jsp?type=list\neval(/ale/.source+/rt/.source+/(15)/.source);
        p = Pattern.compile("(\n)(.*?)(eval\\()", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // fileark/document_list_m.jsp?dir_code=15155966312661000806";alert(723);//"&dir_name=%E8%BD%BD%E4%BD%93%E5%BA%93
        p = Pattern.compile("(;)(.*?)(//)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // /reportJsp/showReport.jsp?id=557"/><img src=javascript:alert(779) &userName=admin
        p = Pattern.compile("(< *img) *src=(.*?)javascript:(.*?)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // 过滤带有换行符的字符串，@task:需判断是否为GET访问？
        // http://localhost:8093/tzcj/reportJsp/showReport_xmxxgl_qx.jsp?id=278\nalert(17)
        /**
         * 下面的项目概况会被误报，故改为判断如果是post方式时则不检测，因为模块中的元素显示时会被toHtml
         * 一、 投资方简介
         * ****第二类增值电信业务中的信息服务业务(不含互联网信息服务);
         */
        if (isGet) {
            p = Pattern.compile("(\n)(.*?)\\(.*?\\)", Pattern.CASE_INSENSITIVE);
            m = p.matcher(content);
            content = m.replaceAll("");
        }

        // userName" alert(1159) "
        // cwsId=1200 alert(657) &flowId=81646
        p = Pattern.compile("\"? +(\\(.*?\\)) +\"?", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // 1234"==alert(24)+"
        p = Pattern.compile("\"=+(.*?)\\+", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // '1234"+alert(1135)+"
        // visual/nest_sheet_view.jsp?formCode=zs_tzf_nest&op=edit&cwsId=29883+alert(840)+&flowId=81990
        p = Pattern.compile("\\+(.*?)\\+", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // visual/nest_sheet_view.jsp?formCode=zs_tzf_nest&op=edit&cwsId=29883-alert(843)-
        p = Pattern.compile("\\-(.*?)\\(.*?\\)\\-", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // visual/nest_sheet_view.jsp?formCode=zs_tzf_nest&op=edit&cwsId=29883\n257+{toString:alert}+&flowId=81990&mainId=29883
        p = Pattern.compile("\\+(.*?)\\{.*?\\}\\+", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // http://localhost:8093/tzcj//visual/module_list.jsp?xmmc=1234&xmmc_cond=0' alert(1439) '&xmjd_cond=1&xmjd=已注册项目&lcq=00040012&lcq_cond=0&nlrq=00040012&nlrq_cond=0&op=search&code=zs_xmlz&menuItem=1&mainCode=
        p = Pattern.compile("(' *)(.*?)( *')", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // "userName\"-x=/\\x61\\x6c\\x65\\x72\\x74\\x28\\x31\\x29/.source;new Function(x)()</script >";
        // userName"^2822+{toString:alert}</script	>
        p = Pattern.compile("(.*?</script.*?>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // http://localhost:8093/tzcj/reportServlet?action=4&reportName=report1==/x/.source;127+{valueOf:alert};&isDialog=1&excelFormat=2003
        // http://localhost:8093/tzcj/reportServlet?action=4&reportName=report1==/x/.source,122+{toString:alert},&isDialog=1&excelFormat=2003
        p = Pattern.compile("(/\\.source\\s*?(;|,))", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // 已将参数“xm”的值设置为“1234'"/><iframe src=javascript:alert(2504) ”
        p = Pattern.compile("(<\\s*iframe +src=\\s*javascript\\s*:)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // reportServlet year1=2020"A==alert(1671)==1
        // reportServlet Set parameter 'reportName's value to 'report1 alert(8185) ' (Variant ID: 20980)

        // Blind MongoDB NoSQL Injection
        // '1234" && (function(){var date = new Date(); do{curDate = new Date();}while(curDate-date<11000); return Math.max();})() && "1'
        p = Pattern.compile("(function).*?Date.*?(while)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // '发送消息 and sleep(0)'
        // '发送消息 and 1=2 or sleep(11)=0 limit 1 -- '
        // 'orderBy's value to 'RegDate and sleep(0)'
        // 'orderBy's value to 'RegDate and 1=2 or sleep(11)=0 limit 1 -- '
        p = Pattern.compile(" *and.*?sleep.*?", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        // 过滤“链接注入”，http://***:8088//flow/macro/macro_js_nestsheet.jsp?op=forRefresh&nestFormCode=<IMG SRC="http://www.ANYSITE.com/ANYSCRIPT.asp">
        m = Pattern.compile("<img.*?src.*?=\\s*?([^> ]+)\\s*.*?>", Pattern.CASE_INSENSITIVE).matcher(content);
        content = m.replaceAll("");

        // 过滤“链接注入”，"'><A HREF="/WF_XSRF5928.html">Injected Link</A>
        m = Pattern.compile("<a.*?href.*?=\\s*?([^> ]+)\\s*.*?>", Pattern.CASE_INSENSITIVE).matcher(content);
        content = m.replaceAll("");

        // 过滤--><input/autofocus onfocus="alert(469)"
        /*
        Pattern pattern = Pattern.compile(
                "--><", Pattern.CASE_INSENSITIVE);
        m = pattern.matcher(content);
        content = m.replaceAll(""); 
        */
        content = content.replaceAll("--><", "");
        
        return content;
    }    
    
    public static void main(String[] args) throws Exception {
        // if (true) return;
    	PatternCompiler pc = new Perl5Compiler();    	
    	PatternMatcher matcher = new Perl5Matcher();    	
    	String content = "http://www.yimihome.com/forum/showtopic.jsp?rootid=1741&CPages=1\" style=\"background:expression(alert(1321))";
//    	content = "http://localhost:8093/tzcj/admin/slide_menu_group.jsp?userName=system\"%20STYLE=\"xss:e/**/xpression(try{a=firstTime}catch(e){firstTime=1;alert(833)})";
//    	// http://www.yimihome.com/forum/showtopic.jsp?rootid=1741&CPages=1" style="background:expression(alert(1321))"
//        org.apache.oro.text.regex.Pattern p = pc.compile(
//                    "([^>]*style=[^>]+)(expression|xpression|javascript|vbscript|-moz-binding)([^>]*)",
//                    Perl5Compiler.CASE_INSENSITIVE_MASK);
//        if (null != p) {
//            content = Util.substitute(matcher, p, new Perl5Substitution("$1$3"), content, Util.SUBSTITUTE_ALL);
//            LogUtil.getLog(getClass()).info(content);
//        }

        content = "http://180.101.236.31:9000/admin/organize/organize.jsp?type=list%0Aalert%28154%29%2F%2F";
        // http://www.yimihome.com/forum/showtopic.jsp?rootid=1741&CPages=1" style="background:expression(alert(1321))"
//        org.apache.oro.text.regex.Pattern p = pc.compile(
//                "(%0A)(.*?)(%2F%2F)",
//                Perl5Compiler.CASE_INSENSITIVE_MASK);
//        if (null != p) {
//            content = Util.substitute(matcher, p, new Perl5Substitution("$1$3"), content, Util.SUBSTITUTE_ALL);
//            LogUtil.getLog(getClass()).info(content);
//        }

        /*content = "admin/organize/organize.jsp?type=list\neval(/ale/.source+/rt/.source+/(15)/.source);";
        Pattern pattern = Pattern.compile("(\n)(.*?)(eval\\()", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(content);
        content = m.replaceAll("");
        LogUtil.getLog(getClass()).info(content);*/

        /*content = "http://localhost:8093/tzcj/visual/module_list.jsp?xmmc=1234' alert(1843) '&xmmc_cond=0&xmjd_cond=1&xmjd=%E5%B7%B2%E6%B3%A8%E5%86%8C%E9%A1%B9%E7%9B%AE&lcq=00040012&lcq_cond=0&nlrq=00040012&nlrq_cond=0&op=search&code=zs_xmlz&menuItem=1&mainCode=";
        Pattern p = Pattern.compile("(' *)(.*?)( *')", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        content = m.replaceAll("");*/

        // '1234"+alert(1135)+"
        // visual/nest_sheet_view.jsp?formCode=zs_tzf_nest&op=edit&cwsId=29883+alert(840)+&flowId=81990
        content = "1234\"+alert(1135)+";
        Pattern p = Pattern.compile("\\+(.*?)\\+", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        content = m.replaceAll("");

        content = "userName\"+alert(1159)+\"";
        p = Pattern.compile("\\+(.*?)\\+", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        content = "visual/nest_sheet_view.jsp?formCode=zs_tzf_nest&op=edit&cwsId=29883-alert(843)-";
        p = Pattern.compile("\\-(.*?)\\(.*?\\)\\-", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        content = "visual/nest_sheet_view.jsp?formCode=zs_tzf_nest&op=edit&cwsId=29883\n257+{toString:alert}+&flowId=81990&mainId=29883";
        p = Pattern.compile("\\+(.*?)\\{.*?\\}\\+", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        if (true) {
            return;
        }

        // userName"+alert(1159)+"
        // 密码加密后，可能会变为4aZU9o+FU0A76W+a5ofQ
        content = "userName\"+alert(1159)+5\"";
        // content = "4aZU9o+FU0A76W+a5ofQ";
        p = Pattern.compile("\"\\s*\\+(.*?)\\+", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        content = "1234'\"/><iframe src=javascript:alert(2504)";
        p = Pattern.compile("(<\\s*iframe +src=\\s*javascript\\s*:)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        content ="1234'\"/><img src=x onerror=alert(2213)>";
        p = Pattern.compile("(< *img)(.*?) onerror\\s*=(.*?)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        content = "http://localhost:8093/tzcj/reportJsp/showReport_xmxxgl_qx.jsp?id=278\nalert(17)";
        p = Pattern.compile("(\n)(.*?)\\(.*?\\)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        LogUtil.getLog(AntiXSS.class).info(content);
        if (true) {
            return;
        }

        content = "1234\"==alert(24)+";
        p = Pattern.compile("\"=+(.*?)\\+", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        LogUtil.getLog(AntiXSS.class).info(content);
        if (true) {
            return;
        }

        // Set parameter 'moneyCode's value to 'gold</script ><style/>html{font-family:expression(alert(890))}</style >'
        content = "gold</script ><style/>html{font-family:expression(alert(890))}</style >";
        p = Pattern.compile(
                "(< *style[^>]*>[^>]+)(expres|expres\\\\sion|expression|xpression|javascript|vbscript|-moz-binding)(.*</style[^>]*>)",
                Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        content = "userName\"-x=/\\x61\\x6c\\x65\\x72\\x74\\x28\\x31\\x29/.source;new Function(x)()</script >";
        Pattern pattern = Pattern.compile(
                "(.*?</script *>)",
                Pattern.CASE_INSENSITIVE);
        m = pattern.matcher(content);
        content = m.replaceAll("");

        content = "userName\"^2822+{toString:alert}</script	>";
        p = Pattern.compile("(.*?</script.*?>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        content = "username=admin&arrestreason=1234<style >html{font-family:expression(alert(1925))}</style >&arrestday=01&Submit=逮捕";
        pattern = Pattern.compile(
                "(<.*?style[^>]*>[^>]+)(expres|expres\\\\sion|expression|xpression|javascript|vbscript|-moz-binding)(.*</style.*?>)",
                Pattern.CASE_INSENSITIVE);
        m = pattern.matcher(content);
        content = m.replaceAll("");

        //  Set query to '>'"><script>alert(1723)</script>'
        content = ">'\"><script>alert(1723)</script>";
        p = Pattern.compile("(.*?</script.*?>)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        /*
        1) Set parameter 'orderBy's value to 'id and sleep(0)'
        2) Set parameter 'orderBy's value to 'id and 1=2 or sleep(11)=0 limit 1 -- '
        */
        content = "order by id and 1=2 or sleep(11)=0 limit 1 --";
        p = Pattern.compile("( * (and)? *(or)? *sleep.*?)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(content);
        content = m.replaceAll("");

        LogUtil.getLog(AntiXSS.class).info(content);

        if (true) {
            return;
        }

        content = "http://localhost:8093/tzcj/flow/macro/macro_js_nestsheet.jsp?op=forRefresh&nestFormCode=tzf&fieldName=< style >html{font-family:expression(alert(568))}</style >";
    	
    	stripEvent("ccc");

        /*try {
            org.apache.oro.text.regex.Pattern p = pc.compile("(<[^>]*)(onmouseover)([^>]*>)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
        }
        catch(MalformedPatternException e) {
        	LogUtil.getLog(getClass()).error(e);
        }*/
    	
    	content = "aa'+alert(123)+'";
    	
    	LogUtil.getLog(AntiXSS.class).info(stripScriptTag(content));
    }

}

