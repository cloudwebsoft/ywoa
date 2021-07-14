package com.redmoon.forum;

import cn.js.fan.util.StrUtil;
import java.util.Vector;
import org.htmlparser.Node;
import org.htmlparser.util.ParserException;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 贴子处理</p>
 *
 * <p>Description: 利用HtmlParser提取贴子摘要、图片</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MsgUtil {
    public static final int MAX_LEN2 = 3000;

    public MsgUtil() {
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

    public static String getAbstract(HttpServletRequest request, MsgDb md, int len) {
        String content = md.getContent();
        // if (content.length() <= len)
        //    return content; // 考虑到上传图片路径的问题，还是必须要取摘要，以免产生相对路径问题，因为上传文件在forum/upfile目录下
        content = StrUtil.getLeft(content, len);

        // 对未完成的标签补齐，以免出现<im或<tab这样的标签
        int idx1 = content.lastIndexOf('<');
        int idx2 = content.lastIndexOf('>');
        // 如果截取时，未取到 > ，则继续往前取，直到取到为止
        // System.out.println("MsgUtil.java getAbstract: idx1=" + idx1 + " idx2=" + idx2);
        if ((idx2 == -1 && idx1 >= 0) || (idx1 > idx2)) {
            String ct3 = md.getContent();
            int idx3 = ct3.indexOf('>', idx1);
            if (idx3!=-1) {
                if (idx3 < MAX_LEN2) {
                    content = ct3.substring(0, idx3 + 1);
                }
            }
        }

        // 对于ActiveX对象进行预处理
        idx2 = content.toLowerCase().lastIndexOf("</object>");
        idx1 = content.toLowerCase().lastIndexOf("<object");
        if ((idx2 == -1 && idx1 >= 0) || idx1 > idx2) {
            String ct2 = md.getContent().toLowerCase();
            int idx3 = ct2.indexOf("</object>");
            if (idx3 != -1)
                content += md.getContent().substring(content.length(), content.length() + idx3 + 9);
            else
                content = md.getContent().substring(0, idx1);
        }

        String str = "";
        try {
            Parser myParser;
            NodeList nodeList = null;
            myParser = Parser.createParser(content, "utf-8");
            NodeFilter textFilter = new NodeClassFilter(TextNode.class);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
            // 暂时不处理 meta
            // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[] {textFilter, linkFilter,
                                     imgFilter});
            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            for (int i = 0; i < nodes.length; i++) {
                Node anode = (Node) nodes[i];
                String line = "";
                if (anode instanceof TextNode) {
                    TextNode textnode = (TextNode) anode;
                    // line = textnode.toPlainTextString().trim();
                    line = textnode.getText();
                } else if (anode instanceof ImageTag) {
                    ImageTag imagenode = (ImageTag) anode;
                    String url = imagenode.getImageURL();
                    String ext = StrUtil.getFileExt(url).toLowerCase();
                    // 如果地址完整
                    if (ext.equals("gif") || ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("bmp")) {
                        // System.out.println("MsgUtil.java getAbstract:" + imagenode.toHtml() + " url=" + imagenode.getImageURL());
                        if (imagenode.getImageURL().startsWith("http"))
                            ; // line = "<div align=center>" + imagenode.toHtml() + "</div>";
                        else if (imagenode.getImageURL().startsWith("/")) {
                            ; //line = "<div align=center>" + imagenode.toHtml() + "</div>";
                        }
                        else { // 相对路径
                            // line = "<div align=center><img src='" + request.getContextPath() + "/forum/" + imagenode.getImageURL() + "'></div>";
                            url = request.getContextPath() + "/forum/" + imagenode.getImageURL();
                        }
                        line = "<div align=center><a onfocus=this.blur() href=\"" + url + "\" target=_blank><IMG SRC=\"" + url + "\" border=0 alt=" +
                                SkinUtil.LoadString(request,
                                 "res.cn.js.fan.util.StrUtil",
                                    "click_open_win") + " onload=\"javascript:if(this.width>screen.width-333) this.width=screen.width-333\"></a></div><BR>";
                        // System.out.println(line);
                    }
                }
                if (isTrimEmpty(line))
                    continue;
                str += "<p>" + line + "</p>";
            }
        }
        catch (ParserException e) {
            LogUtil.getLog(MsgUtil.class.getName()).error("getAbstract:" + e.getMessage());
        }
        return str;
    }

    public static String getIcon(String fileName) {
        String ext = StrUtil.getFileExt(fileName).toLowerCase();
        // System.out.println("MsgUtil.java getIcon:ext=" + ext);

        if (ext.equals("jpeg"))
            ext = "jpg";
        else if (ext.equals("ini"))
            ext = "txt";
        String[] icons = {"gif", "rar", "zip", "txt", "png", "bmp", "jpg", "doc", "xls", "mp3", "swf", "wma"};
        int len = icons.length;
        for (int i=0; i<len; i++) {
            if (ext.equals(icons[i])) {
                return ext + ".gif";
            }
        }
        return "";
    }

    public static String getIconImg(MsgDb md) {
        Vector v = md.getAttachments();
        // System.out.println("MsgUtil.java getIconImg=" + v.size());
        if (v.size()>0) {
            Attachment att = (Attachment)v.get(0);
            String imgicon = getIcon(att.getDiskName());
            return imgicon;
        }
        return "";
    }
}
