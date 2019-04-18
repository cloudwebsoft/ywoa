package com.redmoon.forum.tools;

/**
 * <p>Title: 输出RSS</p>
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

import java.io.*;
import java.util.*;

import javax.servlet.jsp.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.blog.*;
import com.redmoon.forum.*;
import com.redmoon.forum.Leaf;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedOutput;
import org.apache.log4j.*;

/**
 * @author   bbjwerner
 */
public class RSSGenerator {
    Logger logger = Logger.getLogger(RSSGenerator.class.getName());

    public final int CONTENT_LENGTH = 500;

    private static String fileName = null;
    private static String feedType = null;

    public RSSGenerator() {
    }

    public void generateForumRSS(JspWriter out, String feedType, int n) {
        this.feedType = feedType;
        populateForumArrayToJSP(out, n);
    }

    public void generateBoardRSS(JspWriter out, String feedType, int n, String boardCode) {
        this.feedType = feedType;
        populateBoardArrayToJSP(out, n, boardCode);
    }

    public void generateForumRSS(String feedType, String fileName, int n) {
        this.feedType = feedType;
        this.fileName = fileName;
        populateForumArrayToFile(n);
    }

    public void generateBlogRSS(JspWriter out, String feedType, int n, long blogId, String blogUserDir) {
        this.feedType = feedType;
        this.fileName = fileName;
        populateBlogArrayToJSP(out, n, blogId, blogUserDir);
    }

    private void populateForumArrayToFile(int n) {
        Leaf lf = new Leaf();
        ArrayList entries = new ArrayList();
        ArrayList categories = new ArrayList();
        MsgDb md = new MsgDb();
        long[] newIds = md.getForumNewMsgIds(n);
        int len = newIds.length;
        for (int i=0; i<len; i++) {
            md = md.getMsgDb((int)newIds[i]);
            String content = md.getContent();
            // 过滤非法的xml不允许的字符，否则会使得JDOM解析时出错
            content = toXmlChars(content);
            addEntry(entries, categories, md.getTitle(),
                     Global.getRootPath() + "/forum/showtopic.jsp?rootid=" +
                     md.getId(),
                     md.getAddDate(),
                     StrUtil.getLeft(content, CONTENT_LENGTH),
                     lf.getLeaf(md.getboardcode()).getName(),
                     md.getName());
        }

        this.doSyndicationToFile(entries, Global.AppName,
                           Global.getRootPath() + "/forum/index.jsp",
                           Global.desc,
                           Global.copyright,
                               fileName);
    }

    private void populateForumArrayToJSP(JspWriter out, int n) {
        Leaf lf = new Leaf();
        ArrayList entries = new ArrayList();
        ArrayList categories = new ArrayList();
        MsgDb md = new MsgDb();
        long[] newIds = md.getForumNewMsgIds(n);
        int len = newIds.length;
        for (int i=0; i<len; i++) {
            md = md.getMsgDb((int)newIds[i]);
            String content = md.getContent();
            // 过滤非法的xml不允许的字符，否则会使得JDOM解析时出错
            content = toXmlChars(content);
            addEntry(entries, categories, md.getTitle(),
                     Global.getRootPath() + "/forum/showtopic.jsp?rootid=" +
                     md.getId(),
                     md.getAddDate(),
                     content,
                     lf.getLeaf(md.getboardcode()).getName(),
                     md.getName());
        }

        this.doSyndicationToJSP(entries, Global.AppName,
                           Global.getRootPath() + "/forum/index.jsp",
                           Global.desc,
                           Global.copyright, out);
    }

    public static boolean isXMLChar(int ucs4char) {
        return ((ucs4char >= 0x0020 && ucs4char <= 0xD7FF)
                || ucs4char == 0x000A || ucs4char == 0x0009
                || ucs4char == 0x000D
                || (ucs4char >= 0xE000 && ucs4char <= 0xFFFD)
                || (ucs4char >= 0x10000 && ucs4char <= 0x10ffff));
    }

    /**
     *   过滤非法xml字符
     *   @param   xml
     *   @return
     */
    public static String toXmlChars(String xml) {
        StringBuffer newXml = new StringBuffer();
        int p = 0;
        for (int i = 0; i < xml.length(); i++) {
            char ch = xml.charAt(i);
            if (!isXMLChar(ch)) {
                // Parse and format to hexadecimal
                String s = Integer.toString(ch, 16); // 3ff

                // System.out.println(RSSGenerator.class +" invalid xml ch=" + ch +
                //        " hex=" + s);
            }
            else {
                newXml.append(ch);
            }
        }
        return newXml.toString();
    }

    private void populateBoardArrayToJSP(JspWriter out, int n, String boardCode) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(boardCode);
        if (lf==null || !lf.isLoaded())
            return;
        ArrayList entries = new ArrayList();
        ArrayList categories = new ArrayList();
        MsgDb md = new MsgDb();

        String sql = SQLBuilder.getListtopicSql(null, null, null, boardCode, "", "all", ThreadTypeDb.THREAD_TYPE_NONE); // "select id from sq_thread where boardcode="+StrUtil.sqlstr(boardCode)+" and level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY level desc,redate desc";

        ThreadBlockIterator ir = md.getThreads(sql, boardCode, 0, n-1);
        while (ir.hasNext()) {
            md = (MsgDb) ir.next();
            String content = md.getContent();
            // 过滤非法的xml不允许的字符，否则会使得JDOM解析时出错
            content = toXmlChars(content);
            // content = StringEscapeUtils.escapeXml(content); // 这个转换只作很简单的转换< > &

            addEntry(entries, categories, md.getTitle(),
                     Global.getRootPath() + "/forum/showtopic.jsp?rootid=" +
                     md.getId(),
                     md.getAddDate(),
                     content,
                     lf.getName(),
                     md.getName());
        }

        this.doSyndicationToJSP(entries, Global.AppName + "-" + lf.getName(),
                           Global.getRootPath() + "/forum/index.jsp",
                           Global.desc,
                           Global.copyright, out);
    }

    private void populateBlogArrayToJSP(JspWriter out, int n, long blogId, String blogUserDir) {
        ArrayList entries = new ArrayList();
        ArrayList categories = new ArrayList();

        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        if (ucd==null || !ucd.isLoaded()) {
            return;
        }

        UserDirDb udd = new UserDirDb();
        udd = udd.getUserDirDb(blogId, blogUserDir);
        String blogUserDirName = "";
        if (udd!=null && udd.isLoaded())
            blogUserDirName = udd.getDirName();

        String sql = SQLBuilder.getMyblogSql(blogUserDir, blogId);
        MsgDb md = new MsgDb();
        ThreadBlockIterator ir = md.getThreads(sql, md.getVirtualBoardcodeOfBlog(blogId, blogUserDir), 0, n-1);
        while (ir.hasNext()) {
            md = (MsgDb) ir.next();

            String content = md.getContent();
            // 过滤非法的xml不允许的字符，否则会使得JDOM解析时出错
            content = toXmlChars(content);

            addEntry(entries, categories, md.getTitle(),
                     Global.getRootPath() + "/blog/showblog.jsp?rootid=" +
                     md.getId(),
                     md.getAddDate(),
                     content,
                     ucd.getTitle(),
                     md.getName());
        }

        if (blogUserDirName.equals(""))
            doSyndicationToJSP(entries, Global.AppName + "-blog-" + ucd.getTitle(),
                           Global.getRootPath() + "/blog/index.jsp",
                           Global.desc,
                           Global.copyright, out);
        else
            doSyndicationToJSP(entries, Global.AppName + "-blog-" + ucd.getTitle() + "-" + blogUserDirName,
                           Global.getRootPath() + "/blog/index.jsp",
                           Global.desc,
                           Global.copyright, out);
    }

    /**
     * This method adds an entry to the ArrayList() which will be published when GenerateRSS()
     * is called.
     * <p>
     * @param title The title of the blog entry (not the blog itself)
     * @param link The PermaLink that will point to your entry
     * @param date The date of the blog entry
     * @param cat The category of the entry. This has been added to integrate
     *        with Technorati and match WordPress functionality
     * @param author The author of the entry to be published.
     *
     */
    private void addEntry(ArrayList entries, ArrayList categories, String title, String link, java.util.Date date,
                          String content, String cat, String author) {

        try {
            SyndEntry entry;
            SyndContent description;
            SyndCategory category;

            entry = new SyndEntryImpl();
            entry.setAuthor(author);
            entry.setTitle(title);
            entry.setLink(link);
            // System.out.println("RSSGenerator: date=" + date);
            entry.setPublishedDate(date);
            description = new SyndContentImpl();
            description.setType("text/plain");
            description.setValue(content);
            entry.setDescription(description);

            category = new SyndCategoryImpl();
            category.setName(cat);
            categories.add(category);
            entry.setCategories(categories);
            categories.remove(category);
            entries.add(entry);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            logger.error("addEntry: " + ex.getMessage());
        }

    }

    /**
     * This method is called last after you have added all your entries and have specified your
     * feed type and filename. This actually does the work
     * <p>
     * NOTE: This has static content entered in to the fields! You must have access to the source
     * code edit this method or else you will be publishing content as the Post Modern Banter Blog
     * Yes, I should change this immediately. Ideally, it would take values from the web.xml file itself.
     * <p>
     * @throws Exception
     */
    private void doSyndicationToFile(ArrayList entries, String title, String link,
                               String description_loc, String copyright,
                               String xml) {
        try {
            final SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(feedType);

            feed.setTitle(title);
            feed.setLink(link);
            feed.setDescription(description_loc);
            feed.setCopyright(copyright);

            feed.setEntries(entries);

            final Writer writer = new FileWriter(xml);
            final SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
            System.out.println(
                    "************* The feed has been written to the file [" +
                    xml + "]");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            logger.error("doSyndication: " + ex.getMessage());
        }
    }

    /**
     * 输出至页面，rss_2.0时，在rss.jsp页面中有时看不到输出
     * @param entries ArrayList
     * @param title String
     * @param link String
     * @param description_loc String
     * @param copyright String
     * @param out JspWriter
     */
    private void doSyndicationToJSP(ArrayList entries, String title, String link,
                               String description_loc, String copyright,
                               JspWriter out) {
        try {
            final SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(feedType);

            feed.setTitle(title);
            feed.setLink(link);
            feed.setDescription(description_loc);
            feed.setCopyright(copyright);

            feed.setEntries(entries);
            SyndImage si = new SyndImageImpl();
            si.setTitle(title);
            si.setLink(link);
            si.setUrl(Global.getRootPath() + "/logo.gif");
            feed.setImage(si);

            // final Writer writer = new FileWriter(xml);
            final SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, out);
            out.flush();
            // out.close();
            // System.out.println(
            //         "RSSGenerator: doSyndicationToJSP--The feed has been written!");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            logger.error("doSyndication: " + ex.getMessage());
        }
    }
}
