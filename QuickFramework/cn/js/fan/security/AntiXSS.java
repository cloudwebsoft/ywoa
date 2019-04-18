package cn.js.fan.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    static final Pattern IFRAME_TAG_PATTERN = Pattern.compile(
            "<iframe[^>]*>.*</iframe[^>]*>", Pattern.CASE_INSENSITIVE);
    
    static final Pattern IFRAME_SIMPLE_TAG_PATTERN = Pattern.compile(
            "<iframe[^>]*>", Pattern.CASE_INSENSITIVE);	// 360检测：88888</script><iframe src=http://xxooxxoo.js>

    // 非线程安全，20141026
    // static final PatternCompiler pc = new Perl5Compiler();
    // static final PatternMatcher matcher = new Perl5Matcher();

    // http://www.bitscn.com/hack/young/200708/108278.html
    public static String antiXSS(String content) {
    	// 出现了很奇怪的问题：org.apache.oro.text.regex.MalformedPatternException: Unmatched parentheses.
    	// 因此暂不启用
    	// if (true)
    	// 	return content;

        if (content == null || content.equals("")) {
            return "";
        }

        String old = content;
        String ret = _antiXSS(content);
        while (ret != null && !ret.equals(old)) {
            old = ret;
            ret = _antiXSS(ret);
        }
        return ret;
    }

    private static String _antiXSS(String content) {
        try {
            if (content == null || content.equals("")) {
                return null;
            }

            return stripAllowScriptAccess(stripProtocol(stripCssExpression(
                    stripAsciiAndHex(stripEvent(stripScriptTag(content))))));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String stripEvent(String content) throws Exception {
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
            	System.out.println(AntiXSS.class + " pattern=" + "(<[^>]*)("
	                    + events[i] + ")([^>]*>)");
            	e.printStackTrace();
            }
            if (null != p) {
                content = Util.substitute(matcher, p, new Perl5Substitution(
                        "$1" + events[i].substring(2) + "$3"), content,
                                          Util.SUBSTITUTE_ALL);
            }
        }
        // 当出现onload事件时禁止open、location
        // String patternStr = "([^>]*?)onload=([\"|'](.+?)[\"|'])";
        String patternStr = "([^>]*?)onload=([\"|']?(.+?)[\"|']?)"; // 360检测 num"><body ONLOAD=alert(42873)>
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

        // System.out.println("content=" + content);

        // 禁止onclick事件时，标签img除外，但不允许出现location字符，当
        patternStr = "([^>]*)onclick=(\"(.*?)\".*?>)";
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
                String str = "$1click=$2";
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
                            // System.out.println(getClass() + " imgUrl=" + imgUrl);

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
                                // System.out.println("param=" + param);

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
        if (null != p)
            content = Util
                      .substitute(matcher, p, new Perl5Substitution("$1$2"),
                                  content, Util.SUBSTITUTE_ALL);

        p = pc
            .compile(
                    // "(<[^>]*style=[^>]+)(expression|javascript|vbscript|-moz-binding)([^>]*>)",
                    "([^>]*style[^>]*=[^>]+)(expres\\\\sion|expression|javascript|vbscript|-moz-binding)([^>]*)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p)
            content = Util
                      .substitute(matcher, p, new Perl5Substitution("$1$3"),
                                  content, Util.SUBSTITUTE_ALL);

        p = pc.compile("(<style[^>]*>.*)/\\*.*\\*/(.*</style[^>]*>)",
                       Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p)
            content = Util
                      .substitute(matcher, p, new Perl5Substitution("$1$2"),
                                  content, Util.SUBSTITUTE_ALL);

        p = pc
            .compile(
                    "(<style[^>]*>[^>]+)(expression|javascript|vbscript|-moz-binding)(.*</style[^>]*>)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p)
            content = Util
                      .substitute(matcher, p, new Perl5Substitution("$1$3"),
                                  content, Util.SUBSTITUTE_ALL);
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
            if (null != p)
                content = Util.substitute(matcher, p, new Perl5Substitution(
                        "$1/$2"), content, Util.SUBSTITUTE_ALL);
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
        if (null != p)
            content = Util.substitute(matcher, p, new Perl5Substitution(
                    "$1Allow_Script_Access$2"), content, Util.SUBSTITUTE_ALL);
        return content;
    }
    
    private static String stripScriptTag(String content) {
        Matcher m = SCRIPT_TAG_PATTERN.matcher(content);
        content = m.replaceAll("");
        m = IFRAME_TAG_PATTERN.matcher(content);
        content = m.replaceAll("");
        
        m = IFRAME_SIMPLE_TAG_PATTERN.matcher(content);
        content = m.replaceAll("");
        
        // 过滤掉'+alert(695)+'     '==alert(743)+'
        Pattern plusPattern = Pattern.compile(
                "'.*[\\+|=|-]'", Pattern.CASE_INSENSITIVE);
        m = plusPattern.matcher(content);
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
        if (true) return;
    	PatternCompiler pc = new Perl5Compiler();    	
    	PatternMatcher matcher = new Perl5Matcher();    	
    	String content = "http://www.yimihome.com/forum/showtopic.jsp?rootid=1741&CPages=1\" style=\"background:expression(alert(1321))";
    	// http://www.yimihome.com/forum/showtopic.jsp?rootid=1741&CPages=1" style="background:expression(alert(1321))" 
        org.apache.oro.text.regex.Pattern p = pc.compile(
                    "([^>]*style=[^>]+)(expression|javascript|vbscript|-moz-binding)([^>]*)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
        if (null != p) {
            content = Util
                      .substitute(matcher, p, new Perl5Substitution("$1$3"),
                                  content, Util.SUBSTITUTE_ALL); 
            System.out.println(content);
        }
    	
    	stripEvent("ccc");

        try {
            p = pc.compile("(<[^>]*)(onmouseover)([^>]*>)",
                    Perl5Compiler.CASE_INSENSITIVE_MASK);
        }
        catch(MalformedPatternException e) {
        	e.printStackTrace();
        }
    	
    	content = "aa'+alert(123)+'";
    	
    	System.out.println(stripScriptTag(content));
    }

}

