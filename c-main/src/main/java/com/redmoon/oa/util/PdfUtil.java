package com.redmoon.oa.util;

import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.util.OSUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.net.FileRetrieve;
import com.itextpdf.tool.xml.net.FileRetrieveImpl;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.itextpdf.tool.xml.pipeline.html.LinkProvider;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;

public class PdfUtil {

    /**
     * 对html代码规范要求较高，所有的标签必须有闭合标签，否则生成过程中会报异常。异常提示会明确指出对应标签缺少闭合标签。
     * 需引入
     *          <dependency>
     *             <groupId>org.xhtmlrenderer</groupId>
     *             <artifactId>flying-saucer-pdf</artifactId>
     *             <version>9.0.8</version>
     *         </dependency>
     * @param response
     * @param content
     */
    /*public static void htmlToPdf(HttpServletResponse response, String content) {
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            // 图片base64支持，把图片转换为itext自己的图片对象
            // 如果携带图片则加上以下代码,将图片标签转换为Itext自己的图片对象
            renderer.getSharedContext().setReplacedElementFactory(new Base64ImgReplacedElementFactory());
            renderer.getSharedContext().getTextRenderer().setSmoothingThreshold(0);

            // 报错：The entity "nbsp" was referenced, but not declared
            // 该问题是由于未定义HTML的DOCTYPE，致使SAX将内容按照xml默认定义进行解析，而xml中&开头表示可解析的实体，这个实体被DTD预先定义
            content = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head></head><body>" + content + "</body></html>";
            renderer.setDocumentFromString(content);

            // 解决中文支持问题
            String prefixFont = "";
            if (OSUtil.isWindows()) {
                prefixFont = "C:\\Windows\\Fonts" + File.separator;
            } else {
                prefixFont = "/usr/share/fonts/chinese" + File.separator;
            }
            ITextFontResolver fontResolver = renderer.getFontResolver();
            fontResolver.addFont(prefixFont + "STXIHEI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            // renderer.getSharedContext().setBaseURL("file:/D:/z/temp/");  // 如果img标签中src是http或绝对地址可以不要本行代码,另外对于linux系统中写法暂不确认应该是不需要file:/前缀
            renderer.getSharedContext().setBaseURL("file:/" + Global.getRealPath());  // 如果img标签中src是http或绝对地址可以不要本行代码,另外对于linux系统中写法暂不确认应该是不需要file:/前缀
            renderer.layout();
            renderer.createPDF(os);
        } catch (IOException | com.lowagie.text.DocumentException e) {
			LogUtil.getLog(getClass()).error(e);
        } finally {
            try {
                os.close();
            } catch (IOException e) {
			LogUtil.getLog(getClass()).error(e);
            }
        }
    }*/

    /**
     * 将文件柜中的html转为pdf，本方法较旧，且生成图片失败
     * @param response
     * @param title
     * @param content
     * @param author
     */
    public static void htmlToPdfXXX(HttpServletResponse response, String title, String content, String author) {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, buffer);

            StyleSheet st = new StyleSheet();
            st.loadTagStyle("body", "leading", "16,0");

            document.open();

            document.addTitle(title);// 标题
            document.addAuthor("author");// 作者
            /*document.addSubject("Subject");// 主题
            document.addKeywords("Keywords");// 关键字
            document.addCreator("Creator");// 创建者*/

            // 将图片转为绝对路径
            String contentTmp = content;
            Parser parser;
            try {
                parser = new Parser(content);
                parser.setEncoding("utf-8");//
                TagNameFilter filter = new TagNameFilter("img");
                NodeList nodes = parser.parse(filter);
                if (nodes != null || nodes.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    int lastNodeEnd = 0;
                    for (int k=0; k<nodes.size(); k++) {
                        ImageTag node = (ImageTag) nodes.elementAt(k);

                        // image/png;base64,
                        String imgUrl = node.getImageURL();
                        int p = imgUrl.indexOf("http");
                        if (p == -1) {
                            /*if (!imgUrl.startsWith("/")) {
                                imgUrl = Global.getRealPath() + imgUrl;
                            }
                            else {
                                imgUrl = Global.getRealPath().substring(0, Global.getRealPath().length()-1) + imgUrl;
                            }
                            node.setImageURL("file:///" + imgUrl);*/

                            // itext访问http://.../upfile/...时，会被拒绝，所以不能用http://的方式
                            if (imgUrl.startsWith("/")) {
                                imgUrl = Global.getFullRootPath() + imgUrl;
                            }
                            else {
                                imgUrl = Global.getFullRootPath() + "/" + imgUrl;
                            }
                            node.setImageURL(imgUrl);

                            int s = node.getStartPosition();
                            int e = node.getEndPosition();
                            String c = contentTmp.substring(lastNodeEnd, s);
                            c += node.toHtml();
                            sb.append(c);
                            lastNodeEnd = e;
                        }
                        else {
                            int e = node.getEndPosition();
                            String c = contentTmp.substring(lastNodeEnd, e);
                            sb.append(c);
                            lastNodeEnd = e;
                        }
                    }
                    sb.append(StringUtils.substring(contentTmp, lastNodeEnd));
                    content = sb.toString();
                }
            } catch (ParserException e) {
                LogUtil.getLog(PdfUtil.class).error(e);
            }

            String prefixFont = "";
            if (OSUtil.isWindows()) {
                prefixFont = "C:\\Windows\\Fonts" + File.separator;
            } else {
                prefixFont = "/usr/share/fonts/chinese" + File.separator;
            }

            BaseFont bf = BaseFont.createFont(prefixFont + "STXIHEI.TTF", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            Font font = new Font(bf, 12, Font.NORMAL);

            // 创建章节
            // Chapter chapter = new Chapter(new Paragraph(title.replaceAll(" ",""), font), 1);

            /*Paragraph p = new Paragraph(title, font);
            p.setSpacingBefore(10f);
            p.add(Chunk.NEWLINE);*/

            Paragraph p = new Paragraph();
            p.setFont(font);
            StringReader reader = new StringReader(content);
            java.util.List<Element> al = HTMLWorker.parseToList( reader, st );
            for (int k = 0; k < al.size(); ++k) {
                p.add(al.get(k));
            }
            p.setSpacingBefore(10f);
            p.add(Chunk.NEWLINE);

            document.add(p);

            // 加入章节
            // chapter.add(p);
            // document.add(chapter);

            document.close();

            DataOutput output = new DataOutputStream(response.getOutputStream());
            byte[] bytes = buffer.toByteArray();
            response.setContentLength(bytes.length);
            for (int x=0; x<bytes.length; x++) {
                output.writeByte(bytes[x]);
            }
        } catch (Exception e) {
            LogUtil.getLog(PdfUtil.class).error(e);
        }
    }

    public static String changeImgUrl(String content) {
        // 将图片转为绝对路径
        String contentTmp = content;
        Parser parser;
        try {
            parser = new Parser(content);
            parser.setEncoding("utf-8");//
            TagNameFilter filter = new TagNameFilter("img");
            NodeList nodes = parser.parse(filter);
            if (nodes != null || nodes.size() > 0) {
                StringBuffer sb = new StringBuffer();
                int lastNodeEnd = 0;
                for (int k=0; k<nodes.size(); k++) {
                    ImageTag node = (ImageTag) nodes.elementAt(k);

                    // image/png;base64,
                    String imgUrl = node.getImageURL();
                    int p = imgUrl.indexOf("http");
                    if (p == -1) {
                        if (!imgUrl.startsWith("/")) {
                            imgUrl = Global.getRealPath() + imgUrl;
                        }
                        else {
                            imgUrl = Global.getRealPath().substring(0, Global.getRealPath().length()-1) + imgUrl;
                        }
                        node.setImageURL("file:///" + imgUrl);

                        /*
                        if (imgUrl.startsWith("/")) {
                            imgUrl = Global.getFullRootPath() + imgUrl;
                        }
                        else {
                            imgUrl = Global.getFullRootPath() + "/" + imgUrl;
                        }
                        node.setImageURL(imgUrl);
                        */

                        int s = node.getStartPosition();
                        int e = node.getEndPosition();
                        String c = contentTmp.substring(lastNodeEnd, s);
                        c += node.toHtml();
                        sb.append(c);
                        lastNodeEnd = e;
                    }
                    else {
                        int e = node.getEndPosition();
                        String c = contentTmp.substring(lastNodeEnd, e);
                        sb.append(c);
                        lastNodeEnd = e;
                    }
                }
                sb.append(StringUtils.substring(contentTmp, lastNodeEnd));
                content = sb.toString();
            }
        } catch (ParserException e) {
            LogUtil.getLog(PdfUtil.class).error(e);
        }
        return content;
    }

    /**
     * 将文件柜中的html转为pdf
     * @param response
     * @param title
     * @param content
     * @param author
     */
    public static void htmlToPdf(HttpServletResponse response, String title, String content, String author) {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, buffer);

            document.open();

            document.addTitle(title);// 标题
            document.addAuthor("author");// 作者
            /*document.addSubject("Subject");// 主题
            document.addKeywords("Keywords");// 关键字
            document.addCreator("Creator");// 创建者*/

            // 用XMLWorker方法，本机没问题，但服务器上会存在中文不能显示的问题（仅能显示h2标题）
            /*CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(false);
            FileRetrieve retrieve = new FileRetrieveImpl(Global.getRealPath());
            cssResolver.setFileRetrieve(retrieve);

            // HTML
            HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
            htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
            htmlContext.setImageProvider(new AbstractImageProvider() {
                @Override
                public String getImageRootPath() {
                    return Global.getRealPath();
                }
            });
            htmlContext.setLinkProvider(new LinkProvider() {
                @Override
                public String getLinkRoot() {
                    return Global.getRealPath();
                }
            });

            // Pipelines
            PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
            HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
            CssResolverPipeline css = new CssResolverPipeline(cssResolver, html);

            // body中加样式后表格内的中文能显示了，但是其它地方的中文不能显示
            content = "<html><head></head><body style=\"font-family: SimSun;\">" + content + "</body></html>";
            // XML Worker
            XMLWorker worker = new XMLWorker(css, true);
            XMLParser p = new XMLParser(worker);
            p.parse(new StringReader(content));*/

            // 将图片路径改为本地路径file:///
            content = changeImgUrl(content);
            try {
                XMLWorkerHelper.getInstance().parseXHtml(writer, document,
                        new ByteArrayInputStream(content.getBytes("utf-8")),
                        null,
                        Charset.forName("UTF-8"), new AsianFontProvider());
            } catch (IOException e) {
                LogUtil.getLog(PdfUtil.class).error(e);
            }

            // 不能放在finally块中，要放在buffer.toByteArray()前，否则生成的pdf将无法打开
            document.close();

            DataOutput output = new DataOutputStream(response.getOutputStream());
            byte[] bytes = buffer.toByteArray();
            response.setContentLength(bytes.length);
            for (int x=0; x<bytes.length; x++) {
                output.writeByte(bytes[x]);
            }
        } catch (Exception e) {
            LogUtil.getLog(PdfUtil.class).error(e);
        }
    }
}