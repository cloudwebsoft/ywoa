package cn.js.fan.test;

import cn.js.fan.util.StrUtil;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.AppletTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;
import org.htmlparser.visitors.TextExtractingVisitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

//import com.jscud.util.LogMan; //一个日志记录类
/**
 * 演示了Html Parse的应用.
 *
 * @author scud http://www.jscud.com
 */
public class TestHtmlParser {
    public static void main(String[] args) throws Exception {
        String aFile = "c:/rjcs.html";
        // String content = readTextFile(aFile, "GBK");
        String content = "大家好！<a href='ddd.jsp'>GOOD！</a><img src='http://www.cloudwe";
        // test1(content);
        test2(content);

        String str = "<TABLE style=\"TABLE-LAYOUT: fixed; WORD-BREAK: break-all\" height=\"100%\" cellSpacing=0 cellPadding=0 width=\"99%\" border=0>";
        str += "<TBODY>\r\n";
        str += "<TR height=20>";
        str += "<TD colSpan=3><A name=#14198></A><A href=\"http://bbs.huizhou.gov.cn/userinfo.jsp?username=3851\"><IMG alt=都市广场的个人资料 src=\"http://bbs.huizhou.gov.cn/forum/images/profile.gif\" align=absMiddle border=0></A>&nbsp;&nbsp;&nbsp;<A click=\"hopenWin('../message/send.jsp?receiver=%E9%83%BD%E5%B8%82%E5%B9%BF%E5%9C%BA',320,260)\" href=\"http://bbs.huizhou.gov.cn/forum/t-0-13831-7.html#\">";

        // LogUtil.getLog(getClass()).info(StrUtil.fillHtmlTag(str));
    }

    /**
     * 读取文件的方式来分析内容.
     * filePath也可以是一个Url.
     *
     * @param resource 文件/Url
     */
    public static void test5(String resource) throws Exception {
        Parser myParser = new Parser(resource);
        //设置编码
        myParser.setEncoding("GBK");
        HtmlPage visitor = new HtmlPage(myParser);
        myParser.visitAllNodesWith(visitor);
        String textInPage = visitor.getTitle();
    }

    /**
     * 按页面方式处理.对一个标准的Html页面,推荐使用此种方式.
     */
    public static void test4(String content) throws Exception {
        Parser myParser;
        myParser = Parser.createParser(content, "GBK");
        HtmlPage visitor = new HtmlPage(myParser);
        myParser.visitAllNodesWith(visitor);
        String textInPage = visitor.getTitle();
    }

    /**
     * 利用Visitor模式解析html页面.
     *
     * 小优点:翻译了<>等符号
     * 缺点:好多空格,无法提取link
     *
     */
    public static void test3(String content) throws Exception {
        Parser myParser;
        myParser = Parser.createParser(content, "GBK");
        TextExtractingVisitor visitor = new TextExtractingVisitor();
        myParser.visitAllNodesWith(visitor);
        String textInPage = visitor.getExtractedText();
    }

    /**
     * 得到普通文本、图片和链接的内容.
     *
     * 使用了过滤条件.
     */
    public static void test2(String content) throws ParserException {
        Parser myParser;
        NodeList nodeList = null;
        myParser = Parser.createParser(content, "utf-8");
        NodeFilter textFilter = new NodeClassFilter(TextNode.class);
        NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
        NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
        // 暂时不处理 meta
        // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
        OrFilter lastFilter = new OrFilter();
        lastFilter.setPredicates(new NodeFilter[] {textFilter, linkFilter, imgFilter});
        nodeList = myParser.parse(lastFilter);
        Node[] nodes = nodeList.toNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            Node anode = (Node) nodes[i];
            String line = "";
            if (anode instanceof TextNode) {
                TextNode textnode = (TextNode) anode;
                //line = textnode.toPlainTextString().trim();
                line = textnode.getText();
            } else if (anode instanceof LinkTag) {
                LinkTag linknode = (LinkTag) anode;
                line = linknode.toHtml();
                // line = "<a href='" + linknode.getLink() + "'>" + linknode.getLinkText() + "</a>";
                //@todo 过滤jsp标签:可以自己实现这个函数
                //line = StringFunc.replace(line, "<%.*%>", "");
            } else if (anode instanceof AppletTag) {
                AppletTag appletnode = (AppletTag) anode;
                line = appletnode.getAppletClass() + " " +
                       appletnode.getArchive();
            } else if (anode instanceof ImageTag) {
                ImageTag imagenode = (ImageTag) anode;
                // line = imagenode.extractImageLocn();
                line = imagenode.toHtml();
                // line = imagenode.getImageURL();
            }
            if (isTrimEmpty(line))
                continue;
        }
    }

    /**
     * 解析普通文本节点.
     *
     * @param content
     * @throws ParserException
     */
    public static void test1(String content) throws ParserException {
        Parser myParser;
        Node[] nodes = null;
        myParser = Parser.createParser(content, null);
        // nodes = myParser.extractAllNodesThatAre(TextNode.class); //exception could be thrown here
        nodes = (myParser.extractAllNodesThatMatch(new NodeClassFilter(TextNode.class))).
                toNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            TextNode textnode = (TextNode) nodes[i];
            String line = textnode.toPlainTextString().trim();
            if (line.equals(""))
                continue;
        }
    }

    /**
     * 读取一个文件到字符串里.
     *
     * @param sFileName 文件名
     * @param sEncode String
     * @return 文件内容
     */
    public static String readTextFile(String sFileName, String sEncode) {
        StringBuffer sbStr = new StringBuffer();
        try {
            File ff = new File(sFileName);
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    ff),
                    sEncode);
            BufferedReader ins = new BufferedReader(read);
            String dataLine = "";
            while (null != (dataLine = ins.readLine())) {
                sbStr.append(dataLine);
                sbStr.append("\r\n");
            }
            ins.close();
        } catch (Exception e) {
            //LogMan.error("read Text File Error", e);
        }
        return sbStr.toString();
    }

    /**
     * 去掉左右空格后字符串是否为空
     * @param astr String
     * @return boolean
     */
    public static boolean isTrimEmpty(String astr) {
        if ((null == astr) || (astr.length() == 0)) {
            return true;
        }
        if (isBlank(astr.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 字符串是否为空:null或者长度为0.
     * @param astr 源字符串.
     * @return boolean
     */
    public static boolean isBlank(String astr) {
        if ((null == astr) || (astr.length() == 0)) {
            return true;
        } else {
            return false;
        }
    }
}


