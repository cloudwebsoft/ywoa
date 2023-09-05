package com.redmoon.oa.fileark.robot;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class RobotUtil {

    /*
    用apache HttpClient采集
     */
    public String gather(String link) {
        CloseableHttpResponse response = null;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

            HttpGet httpGet = new HttpGet(link);
            response = httpclient.execute(httpGet);
            //把内容转成字符串
            HttpEntity entity = response.getEntity();
            // EntityUtils.consume(entity);
            return EntityUtils.toString(entity);
        } catch (NoSuchAlgorithmException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (KeyStoreException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (KeyManagementException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ClientProtocolException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (response!=null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
        return "";
    }

    public String gather(String link, String charset) {
        link = UrlDecode(link, charset);

        BufferedReader reader = null;
        HttpURLConnection huc = null;
        String str = "";
        try {
            URL url = new URL(link);
            huc = (HttpURLConnection) url.openConnection();

            if (huc instanceof HttpsURLConnection) {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());
                ((HttpsURLConnection) huc).setSSLSocketFactory(sc.getSocketFactory());
                ((HttpsURLConnection) huc).setHostnameVerifier(new TrustAnyHostnameVerifier());
            }

            huc.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");

            String cookieVal = huc.getHeaderField("Set-Cookie");
            String sessionId = null;
            if (cookieVal != null) {
                sessionId = cookieVal.substring(0, cookieVal.indexOf(";"));
            }

            if (sessionId != null) {
                huc.disconnect();
                huc = (HttpURLConnection) url.openConnection();
                huc.setRequestProperty("Cookie", sessionId);
            }

            reader = new BufferedReader(new InputStreamReader(huc.getInputStream(), charset));

            String line = reader.readLine();
            while (line != null) {
                str = str + line + "\r\n";
                line = reader.readLine();
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("gather2: link=" + link);
            LogUtil.getLog(getClass()).error("gather2: " + StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (huc != null) {
                huc.disconnect();
            }
        }
        return str;
    }

    public static String getFileName(String fileUrl, String mimeType, boolean isUseOriginalName)
            throws ErrMsgException {
        int p = fileUrl.lastIndexOf("/");
        String fileName = fileUrl.substring(p + 1);
        if (fileName.indexOf("?") != -1) {
            String ext;
            if (mimeType.equals("image/gif")) {
                ext = "gif";
            } else {
                if (mimeType.equals("image/bmp")) {
                    ext = "bmp";
                } else {
                    if (mimeType.equals("image/jpeg")) {
                        ext = "jpg";
                    } else {
                        if (mimeType.equals("application/x-shockwave-flash")) {
                            ext = "swf";
                        } else {
                            if (mimeType.equals("image/png"))
                                ext = "png";
                            else
                                throw new ErrMsgException("MIME type " + mimeType + " is not supported.");
                        }
                    }
                }
            }
            fileName = RandomSecquenceCreator.getId(20) + "." + ext;
        } else if (isUseOriginalName) {
            fileName = fileName.toLowerCase();
        } else {
            String ext = StrUtil.getFileExt(fileName);
            fileName = RandomSecquenceCreator.getId(20) + "." + ext;
        }

        return fileName;
    }

    public static String gatherFile(String fileUrl, String charset, String saveDirectory) {
        fileUrl = UrlDecode(fileUrl, charset);

        String fileName = "";
        URL url = null;
        HttpURLConnection huc = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        String filePath = saveDirectory;
        if (filePath.charAt(filePath.length() - 1) != File.separatorChar) {
            filePath = filePath + "/";
        }

        String mimeType = "";
        try {
            url = new URL(fileUrl);
            huc = (HttpURLConnection) url.openConnection();
            mimeType = huc.getContentType();

            fileName = getFileName(fileUrl, mimeType, false);
            filePath = filePath + fileName;
            File file = new File(filePath);

            huc.connect();
            bis = new BufferedInputStream(huc.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(file));

            byte[] b = new byte[1024];
            int len = bis.read(b);
            while (len != -1) {
                bos.write(b, 0, len);
                len = bis.read(b);
            }
        } catch (Exception e) {
            LogUtil.getLog(RobotUtil.class).error("gatherFile2:" + e.getMessage());
        } finally {
            try {
                bos.flush();
                bos.close();
                bis.close();
                huc.disconnect();
            } catch (Exception e) {
            }
        }
        return fileName;
    }

    public Vector getSegmentRegex(String content, String rule, String ruleTarget, boolean isMatchOnce, boolean isBlankCharAllowed) {
        Vector v = new Vector();
        String patString = rule;

        patString = patString.replaceAll("\\.", "\\\\.");
        patString = patString.replaceAll("\\?", "\\\\?");
        patString = patString.replaceAll("\\+", "\\\\+");

        patString = patString.replaceAll("\\*", ".*?");

        String ruleTarget2 = ruleTarget.replaceAll("\\[", "\\\\[");
        ruleTarget2 = ruleTarget2.replaceAll("\\]", "\\\\]");

        LogUtil.getLog(getClass()).info("ruleTarget2=" + ruleTarget2);

        if (isBlankCharAllowed)
            patString = patString.replaceFirst(ruleTarget2, "(.+?)");
        else {
            patString = patString.replaceFirst(ruleTarget2, "(\\\\S+?)");
        }

        patString = patString.replaceAll("\\[", "\\\\[");
        patString = patString.replaceAll("\\]", "\\\\]");

        Pattern pat = Pattern.compile(patString, 34);

        Matcher m = pat.matcher(content);
        while (m.find()) {
            if (m.groupCount() == 1) {
                v.addElement(m.group(1));
            }

            if (isMatchOnce) {
                break;
            }
        }
        return v;
    }

    public static String fixLink(String content, String prefix) {
        String pat = "<a.+?href=(\"|'|)([^(\"|'|)]+)\\s*?.*?>(.+?)</a>";
        Pattern pattern = Pattern.compile(pat, 34);

        Matcher matcher = pattern.matcher(content);
        return matcher.replaceAll("<a href=\"" + prefix + "$2\">$3</a>");
    }

    public static String fixFlashLinkAndDownload(String content, String charset, String prefix, boolean isDownload, String relativePath, Vector flashsOuter) {
        String pat = "<PARAM NAME=\"Movie\"( |.*?)value=('|\")(.*?)('|\")>";

        Pattern pattern = Pattern.compile(pat, 34);

        Matcher matcher = pattern.matcher(content);

        String result = "";

        if ((!isDownload) && (prefix.equals(""))) {
            result = content;
        } else {
            String filePath = Global.realPath + relativePath;

            File f = new File(filePath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            int p = 0;
            while (matcher.find()) {
                String flashUrl = matcher.group(3);
                String orgflashUrl = flashUrl;

                if (!flashUrl.startsWith("http:")) {
                    flashUrl = prefix + flashUrl;
                }
                if (isDownload) {
                    result = result + content.substring(p, matcher.start());
                    String flashTag = matcher.group(0);
                    String fileName = gatherFile(flashUrl, charset, filePath);

                    if (!fileName.equals("")) {
                        String path = "/";
                        if (!Global.virtualPath.equals("")) {
                            path = "/" + Global.virtualPath + "/" + relativePath;
                        }
                        flashTag = StrUtil.replace(flashTag, orgflashUrl, path + "/" + fileName);

                        flashsOuter.addElement(relativePath + "/" + fileName);
                        result = result + flashTag;
                    } else {
                        result = result + flashTag;
                    }
                    p = matcher.end();
                } else {
                    result = result + content.substring(p, matcher.start());
                    String flashTag = matcher.group(0);

                    if (!orgflashUrl.equals(flashUrl)) {
                        flashTag = StrUtil.replace(flashTag, orgflashUrl, flashUrl);
                    }

                    result = result + flashTag;
                    p = matcher.end();
                }
            }
            if (p < content.length()) {
                result = result + content.substring(p);
            }
        }

        return result;
    }

    public static String fixImageLinkAndDownload(String content, String charset, String prefix, boolean isDownload, String relativePath, Vector imgsOuter) {
        String patString = "<img.*?src.*?=\\s*?([^> ]+)\\s*.*?>";

        Pattern pattern = Pattern.compile(patString, 34);

        Matcher matcher = pattern.matcher(content);

        String result = "";

        if ((!isDownload) && (prefix.equals(""))) {
            result = content;
        } else {
            String filePath = Global.realPath + relativePath;

            File f = new File(filePath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            int p = 0;
            while (matcher.find()) {
                String imgUrl = matcher.group(1).replaceAll("\"|'", "");
                String orgImgUrl = imgUrl;

                if ((!prefix.equals("")) && (!imgUrl.startsWith("http:"))) {
                    imgUrl = prefix + imgUrl;
                }
                if (isDownload) {
                    result = result + content.substring(p, matcher.start());
                    String imgTag = matcher.group(0);
                    String fileName = gatherFile(imgUrl, charset, filePath);

                    if (!fileName.equals("")) {
                        String path = "/" + relativePath;
                        if (!Global.virtualPath.equals("")) {
                            path = "/" + Global.virtualPath + "/" + relativePath;
                        }
                        imgTag = StrUtil.replace(imgTag, orgImgUrl, path + "/" + fileName);

                        imgsOuter.addElement(relativePath + "/" + fileName);
                        result = result + imgTag;
                    } else {
                        result = result + imgTag;
                    }
                    p = matcher.end();
                } else {
                    result = result + content.substring(p, matcher.start());
                    String flashTag = matcher.group(0);

                    if (!orgImgUrl.equals(imgUrl)) {
                        flashTag = StrUtil.replace(flashTag, orgImgUrl, imgUrl);
                    }

                    result = result + flashTag;
                    p = matcher.end();
                }
            }
            if (p < content.length()) {
                result = result + content.substring(p);
            }
        }
        return result;
    }

    public static String[] parseLink(String linkStr, String charset, String prefix) {
        String[] ary = new String[2];
        try {
            NodeList nodeList = null;
            Parser myParser = Parser.createParser(linkStr, charset);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);

            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[]{linkFilter});
            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            int i = 0;
            if (i < nodes.length) {
                Node anode = nodes[i];
                if ((anode instanceof LinkTag)) {
                    LinkTag lt = (LinkTag) anode;
                    ary[0] = (prefix + lt.getLink());
                    ary[1] = lt.getLinkText();
                }
            }
        } catch (ParserException e) {
            LogUtil.getLog(RobotUtil.class.getName()).error("parseLink:" + e.getMessage());
        }

        return ary;
    }

    public String getSegment(String content, String rule, String ruleTarget, Integer pStart) {
        int p = rule.indexOf(ruleTarget);
        if (p == -1)
            return "";
        int targetLen = ruleTarget.length();
        int q = p + targetLen;

        String ruleBegin = rule.substring(0, p);

        String ruleEnd = rule.substring(q);

        p = content.indexOf(ruleBegin, pStart.intValue());
        if (p == -1) {
            return "";
        }

        p += ruleBegin.length();

        q = content.indexOf(ruleEnd, p);
        if (q == -1) {
            return "";
        }
        return content.substring(p, q);
    }

    public String replace(String data, String rule, String destStr) {
        if ((rule == null) || (rule.equals(""))) {
            return data;
        }
        String regex = rule.replaceAll("\\.", "\\\\.");
        regex = regex.replaceAll("\\?", "\\\\?");

        regex = regex.replaceAll("\\*", ".*?");
        regex = regex.replaceAll("\\[", "\\\\[");
        regex = regex.replaceAll("\\]", "\\\\]");

        String[] ary = StrUtil.split(rule, "\\|");
        if (ary == null) {
            return data;
        }
        int len = ary.length;
        for (int i = 0; i < len; i++) {
            data = data.replaceAll(regex, destStr);
        }

        return data;
    }

    public boolean hasKey(String data, String keys) {
        if ((keys == null) || (keys.equals("")))
            return true;
        String[] ary = StrUtil.split(keys, "\\|");
        if (ary == null)
            return false;
        int len = ary.length;
        for (int i = 0; i < len; i++) {
            if (data.indexOf(ary[i]) != -1)
                return true;
        }
        return false;
    }

    public boolean filterData(String data, String rule) {
        if (rule == null) {
            return false;
        }
        String patString = rule.replaceAll("\\.", "\\\\.");
        patString = patString.replaceAll("\\?", "\\\\?");

        patString = patString.replaceAll("\\*", ".*?");
        patString = patString.replaceAll("\\[", "\\\\[");
        patString = patString.replaceAll("\\]", "\\\\]");

        String[] ary = StrUtil.split(patString, "\\|");
        if (ary == null)
            return false;
        int len = ary.length;

        for (int i = 0; i < len; i++) {
            Pattern pattern = Pattern.compile(ary[i], 34);

            Matcher matcher = pattern.matcher(data);
            if (matcher.find())
                return true;
        }
        return false;
    }

    public static String encode(String value) {
        if (value == null)
            return "";
        value = value.replaceAll("\\r", "&cws1");
        value = value.replaceAll(" ", "&cws2");
        value = value.replaceAll("\\t", "&cws3");
        value = value.replaceAll("=", "&cws4");
        value = value.replaceAll("\\n", "&cws5");
        value = value.replaceAll("&nbsp;", "&amp;nbsp;");
        return value;
    }

    public static String decode(String value) {
        value = value.replaceAll("&cws1", "\r");
        value = value.replaceAll("&cws2", " ");
        value = value.replaceAll("&cws3", "\t");
        value = value.replaceAll("&cws4", "=");
        value = value.replaceAll("&cws5", "\n");
        value = value.replaceAll("&amp;nbsp;", "&nbsp;");
        return value;
    }

    public static String UrlDecode(String url, String charset) {
        try {
            url = URLDecoder.decode(url, charset);
        } catch (UnsupportedEncodingException e) {
        }
        url = url.replaceAll("&amp;", "&");
        return url;
    }

    public static void main(String[] args) {
        RobotUtil ru = new RobotUtil();

        // String str = ru.gather("https://tech.163.com/");
        String str = ru.gather("https://tech.163.com/", "gbk");

        LogUtil.getLog(RobotUtil.class).info(str);
        if (true)
            return;

        String rule = "><font color=\"red\">*aaCPages=[pagenum]</font>";
        String ruleTarget = "[pagenum]";
        String content = "哈哈><font color=\"red\">ddd ccaaCPages=我爱北京天安门</font>你好<font color哈哈><font color=\"red\">ddd ccaa我爱北京dd天安门</font>你好";

        content = "哈哈><font color=\"red\">ddd ccaaCPages=我爱北京天安门>你好<font color哈哈><font color=\"red\">ddd ccaa我爱北京dd天安门</font>你好";

        content = "dddd<IMG height=49 src=\"/cwbbs/upfile/webeditimg/2007/3/d275bf935b.gif\"/>dxxx";
        String patString = "<img.*?src.*?=\\s*?(\\S+\\b)\\s*.*?>";

        content = "<a href=\"viewthread.php?fid=48&amp;tid=41376&amp;extra=page%3D1\" class=\"";

        patString = "<a href=\"(.+?)\" class=\"";

        patString = "<img.*?src.*?=\\s*?([^> ]+)\\s*.*?>";

        content = "资料图片 <img src=\"http://news.xinhuanet.com/photo/2007-01/11/xin_02201041121128592726925.jpg\"><br>";
        content = "<a href=\"detail.jsp?id=40751817\" target=\"_blank\" class=\"txt\">";
        patString = "<a href=\"(\\S+?)\" target=\"_blank\" class=\"txt\">";
        Pattern pat = Pattern.compile(patString, 34);

        Matcher m = pat.matcher(content);
        while (m.find()) {
            LogUtil.getLog(RobotUtil.class).info("m.group(0)=" + m.group(0));
            LogUtil.getLog(RobotUtil.class).info(RobotUtil.class + " m.groupCount=" + m.groupCount());

            if (m.groupCount() == 1) {
                LogUtil.getLog(RobotUtil.class).info(RobotUtil.class + " m.group(1)=" + m.group(1));
            }

        }

        // LogUtil.getLog(RobotUtil.class).info(ru.getSegmentRegex(content, "<a href=\"[url]\" class=\"", "[url]", true, true));
        try {
            LogUtil.getLog(RobotUtil.class).info(UrlDecode("http://cn.pg.photos.yahoo.com/ph/y3dlabs1234/detail_hires?.dir=1daa&.dnm=ba84cnb.jpg", "gb2312"));
        } catch (Exception e) {
        }
    }

    public static String getText(String content, String charset, String token) {
        String str = "";
        try {
            NodeList nodeList = null;
            Parser myParser = Parser.createParser(content, charset);
            NodeFilter textFilter = new NodeClassFilter(TextNode.class);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);

            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[]{textFilter, linkFilter, imgFilter});

            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            for (int i = 0; i < nodes.length; i++) {
                Node anode = nodes[i];
                String line = "";
                if ((anode instanceof TextNode)) {
                    TextNode textnode = (TextNode) anode;

                    line = textnode.getText();
                }
                if (line.trim().equals(""))
                    continue;
                if (str.equals(""))
                    str = line;
                else
                    str = str + token + line;
            }
        } catch (ParserException e) {
            LogUtil.getLog(RobotUtil.class.getName()).error("getText:" + e.getMessage());
        }
        return str;
    }
}