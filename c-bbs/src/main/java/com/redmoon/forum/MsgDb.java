package com.redmoon.forum;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.mail.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.blog.*;
import com.redmoon.forum.message.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.forum.security.*;
import com.redmoon.forum.sms.*;
import com.redmoon.forum.util.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

/**
 *
 * <p>Title:论坛贴子的插入、修改、删除等数据库的相关处理 </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MsgDb implements java.io.Serializable {
    public static int LEVEL_TOP_BOARD = 100;
    public static int LEVEL_TOP_FORUM = 200;
    public static int LEVEL_NONE = 0;

    public static int WEBEDIT_UBB = 0;
    public static int WEBEDIT_REDMOON = 1;
    public static int WEBEDIT_NORMAL = 2;

    public static int MIN_CONTENT_LEN = 10;
    public static int MAX_CONTENT_LEN = 3000;
    public static int MAX_CONTENT_LEN_WE = 20000;
    public static int MAX_TOPIC_LEN = 100;
    public static int MIN_TOPIC_LEN = 1;

    public static final int TYPE_MSG = 0;
    public static final int TYPE_VOTE = 1;

    public static final int EXPRESSION_NONE = 0;

    public static final int CHECK_STATUS_NOT = 0; // 未审核
    public static final int CHECK_STATUS_PASS = 1; //　审核通过
    public static final int CHECK_STATUS_DUSTBIN = 10; // 删除

    public static int LAST_OPERATE_NONE = -1; // 无操作

    public transient Vector tempTagNameVector = null; // 用于检测标签是否合法

    static {
        initParam();
    }

    String connname = Global.getDefaultDB();

    // 序列化时，类的所有数据成员应可序列化除了声明为transient或static的成员。
    // 将变量声明为transient告诉JVM我们会负责将变元序列化。
    // 将数据成员声明为transient后，序列化过程就无法将其加进对象字节流中，
    // transient Logger Logger = Logger.getLogger(MsgDb.class.getName());

    int ret = 1;
    String boardcode, name, pwd,
                       title = "", content = "", ip;
    int show_smile = 1, show_ubbcode = 1, email_notify = 0;
    long rootid = -1;
    long id;
    int layer;
    java.util.Date addDate;
    int orders;
    int expression = EXPRESSION_NONE;
    long replyid = -1;
    int isWebedit = WEBEDIT_NORMAL;
    private String plugin2Code;

    public MsgDb() {
        init();
    }

    public MsgDb(long id) {
        init();
        loadFromDb(id);
    }

    public static void initParam() {
        Config cfg = Config.getInstance();
        MIN_TOPIC_LEN = cfg.getIntProperty("forum.msgTitleLengthMin");
        MAX_TOPIC_LEN = cfg.getIntProperty("forum.msgTitleLengthMax");
        MIN_CONTENT_LEN = cfg.getIntProperty("forum.msgLengthMin");
        MAX_CONTENT_LEN = cfg.getIntProperty("forum.msgLengthMax");

        MAX_CONTENT_LEN_WE = MAX_CONTENT_LEN;
    }

    public void init() {
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    private int recount;

    public String getboardcode() {
        return boardcode;
    }

    /**
     * 取得当前处理的boardcode，用于oscache刷新缓存，oscache已放弃，主要是其原理适合于页面的缓存，但对于对象的缓存不是很方便
     * @return String
     */
    public String getCurBoardCode() {
        return boardcode;
    }

    /**@task:需优化
     * 取得year年month月中每天的日志数
     * @param year int
     * @param month int First month begin with 1
     * @return int[] First day begin with 1
     */
    public int[] getBlogMsgDayCount(long blogId, int year, int month) {
        // System.out.println("month=" + month);
        // 取得year-month这个月的天数
        int dayCount = DateUtil.getDayCount(year, month - 1);
        // System.out.println("day=" + dayCount);
        int[] ary = new int[dayCount + 1];
        for (int i = 1; i <= dayCount; i++) {
            ary[i] = 0;
        }

        Calendar calStart = Calendar.getInstance();
        calStart.set(year, month - 1, 1);
        String start = "" + calStart.getTimeInMillis();
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(year, month - 1, dayCount, 24, 60);
        String end = "" + calEnd.getTimeInMillis();

        String sql = "select lydate from sq_thread where blog_id=? and isBlog=1 and check_status=" + CHECK_STATUS_PASS + " and lydate>? and lydate<? order by lydate asc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, blogId);
            pstmt.setString(2, start);
            pstmt.setString(3, end);
            rs = conn.executePreQuery();
            Calendar cal = Calendar.getInstance();
            while (rs.next()) {
                java.util.Date d = DateUtil.parse(rs.getString(1));
                cal.setTime(d);
                ary[cal.get(cal.DAY_OF_MONTH)]++;
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("getBlogMsgDayCount:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return ary;
    }

    /**
     * 取得y年m月d日的用户日志
     * @param userName String
     * @param y int
     * @param m int
     * @param d int
     * @return Vector
     */
    public Vector getBlogDayList(long blogId, int y, int m, int d) {
        String sql = "select id from sq_thread where blog_id=? and isBlog=1 and check_status=" + CHECK_STATUS_PASS + " and lydate>? and lydate<? order by lydate asc";
        Calendar calStart = Calendar.getInstance();
        calStart.set(y, m - 1, d, 0, 0, 0);
        String start = "" + calStart.getTimeInMillis();
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(y, m - 1, d, 23, 59, 59);
        String end = "" + calEnd.getTimeInMillis();

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, blogId);
            pstmt.setString(2, start);
            pstmt.setString(3, end);
            // url,title,image,userName,sort,kind
            rs = conn.executePreQuery();
            Calendar cal = Calendar.getInstance();
            while (rs.next()) {
                MsgDb md = getMsgDb(rs.getLong(1));
                v.addElement(md);
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("getBlogDayList:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public static String LoadString(HttpServletRequest request, String key) {
        return SkinUtil.LoadString(request, "res.forum.MsgDb", key);
    }

    public boolean CheckTopic(HttpServletRequest req, FileUpload TheBean) throws
            ErrMsgException {
        if (ret == FileUpload.RET_TOOLARGESINGLE) {
            throw new ErrMsgException(LoadString(req, "err_too_large")); // "您上传的文件太大!");
        }
        if (ret == FileUpload.RET_INVALIDEXT) {
            throw new ErrMsgException(TheBean.getErrMessage(req));
        }

        String errMsg = "";

        title = TheBean.getFieldValue("topic");
        if (title == null || title.trim().equals(""))
            errMsg += LoadString(req, "err_need_title"); // 请输入主题;
        boardcode = TheBean.getFieldValue("boardcode");
        if (boardcode == null || boardcode.trim().equals(""))
            errMsg += LoadString(req, "err_need_board"); // 请选择论坛;
        content = TheBean.getFieldValue("Content");

        // 20171230 fgf 取消对内容的XSS过滤
        // content = AntiXSS.antiXSS(content);

        String sIsWebedit = TheBean.getFieldValue("isWebedit");
        if (sIsWebedit == null || sIsWebedit.equals(""))
            isWebedit = WEBEDIT_UBB;
        else
            isWebedit = WEBEDIT_NORMAL;

        // 过滤title与content
        title = ForumFilter.filterMsg(req, title);
        content = ForumFilter.filterMsg(req, content);

        // Logger.getLogger(MsgDb.class.getName()).error("content=" + content);
        expression = StrUtil.toInt(TheBean.getFieldValue("expression"), EXPRESSION_NONE);
        ip = req.getRemoteAddr();

        String strshow_smile = TheBean.getFieldValue("show_smile");
        if (strshow_smile == null || strshow_smile.equals(""))
            show_smile = 1;
        else
            show_smile = Integer.parseInt(strshow_smile);
        String strshow_ubbcode = TheBean.getFieldValue("show_ubbcode");
        if (strshow_ubbcode == null || strshow_ubbcode.equals(""))
            show_ubbcode = 1;
        else
            show_ubbcode = Integer.parseInt(strshow_ubbcode);
        String stremail_notify = TheBean.getFieldValue("email_notify");
        if (stremail_notify == null || stremail_notify.equals(""))
            email_notify = 0;
        else
            email_notify = Integer.parseInt(stremail_notify);

        String strmsg_notify = TheBean.getFieldValue("msg_notify");
        if (strmsg_notify == null || strmsg_notify.equals(""))
            msgNotify = 0;
        else
            msgNotify = Integer.parseInt(strmsg_notify);
        String strsms_notify = TheBean.getFieldValue("sms_notify");
        if (strsms_notify == null || strsms_notify.equals(""))
            smsNotify = 0;
        else
            smsNotify = Integer.parseInt(strsms_notify);

        String strIsBlog = StrUtil.getNullStr(TheBean.getFieldValue("isBlog"));
        if (strIsBlog.equals("1"))
            blog = true;
        else {
            if (boardcode.equals(Leaf.CODE_BLOG))
                blog = true;
            else
                blog = false;
        }

        if (blog) {
            blogUserDir = TheBean.getFieldValue("blogUserDir");
            UserConfigDb ucd = new UserConfigDb();
            String strBlogId = StrUtil.getNullStr(TheBean.getFieldValue(
                    "blogId"));
            blogId = UserConfigDb.NO_BLOG;
            if (!strBlogId.equals("")) {
                try {
                    blogId = Long.parseLong(strBlogId);
                } catch (Exception e) {
                }
            }
            // LogUtil.getLog(getClass()).info("ucd " + ucd.isLoaded() + " blogId=" + blogId);
            if (blogId == UserConfigDb.NO_BLOG) {
                ucd = ucd.getUserConfigDbByUserName(Privilege.getUser(req));
            } else {
                ucd = ucd.getUserConfigDb(blogId);
            }
            // LogUtil.getLog(getClass()).info("ucd " + ucd.isLoaded() + " id=" + ucd.getId());
            blogId = ucd.getId();

            isLocked = StrUtil.toInt(TheBean.getFieldValue("isLocked"), 0);

            blogDirCode = StrUtil.getNullStr(TheBean.getFieldValue("blogDirCode"));

            // System.out.println(getClass() + " blogDirCode=" + blogDirCode);
            /*
            if (!blogUserDir.equals(UserDirDb.DEFAULT)) {
                UserDirDb udd = new UserDirDb();
                udd = udd.getUserDirDb(blogId, blogUserDir);
                if (udd.isLoaded()) {
                    blogDirCode = udd.getCatalogCode();
                }
            }
            */
        }
        plugin2Code = StrUtil.getNullStr(TheBean.getFieldValue("plugin2Code")).
                      trim();
        pluginCode = StrUtil.getNullStr(TheBean.getFieldValue("pluginCode")).
                     trim();
        if (!errMsg.equals("")) {
            throw new ErrMsgException(errMsg);
        }

        // 检查发贴是否需审核
        TimeConfig tc = new TimeConfig();
        if (tc.isPostNeedCheck(req)) {
            checkStatus = CHECK_STATUS_NOT;
        } else {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(boardcode);
            checkStatus = lf.getCheckMsg() == lf.CHECK_NOT ? CHECK_STATUS_PASS :
                          CHECK_STATUS_NOT;
        }
        String strThreadType = StrUtil.getNullStr(TheBean.getFieldValue(
                "threadType"));
        if (strThreadType.equals("") || !StrUtil.isNumeric(strThreadType))
            threadType = ThreadTypeDb.THREAD_TYPE_NONE;
        else
            threadType = Integer.parseInt(strThreadType);

        Config cfg = Config.getInstance();
        Privilege pvg = new Privilege();
        // 匿名发贴不生成标签
        if (cfg.getBooleanProperty("forum.isTag") && pvg.isUserLogin(req)) {
            // 检查tag
            String tag = StrUtil.getNullStr(TheBean.getFieldValue("tag")).
                         trim();
            if (!tag.equals("")) {
                // 检查总长度是否合法
                int tagLenMax = cfg.getIntProperty("forum.tagLenMax");
                if (tag.length()>tagLenMax) {
                    String limit = StrUtil.format(SkinUtil.LoadString(req, "res.label.forum.showtopic", "tag_limit"), new Object[] { new Integer(cfg.getIntProperty("forum.tagLenMax")) });
                    throw new ErrMsgException(limit);
                }

                tag = tag.replaceAll("　", " "); // 賛换全角的空格
                tag = tag.replaceAll(" +", " "); // 替换多余的空格

                // 检查是否含有非法字符，只允许中文和字母及数字
                Pattern pa = Pattern.compile("[^\u4e00-\u9fa5 \\w]+",
                                             Pattern.CANON_EQ);
                Matcher m = pa.matcher(tag);
                if (m.find()) {
                    throw new ErrMsgException(SkinUtil.LoadString(req, "res.label.forum.showtopic", "tag_format"));
                }

                int tagSingleLenMax = cfg.getIntProperty("forum.tagSingleLenMax");

                String[] ary = tag.split(" ");
                // 去除重复的tag，并检查单个标签的长度是否合法
                int len = ary.length;
                tempTagNameVector = new Vector();
                for (int i = 0; i < len; i++) {
                    String tagName = ary[i];
                    boolean isRepeat = false;
                    for (int j = i + 1; j < len; j++) {
                        if (ary[j].equals(tagName)) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        if (tagName.length()>tagSingleLenMax) {
                            String limit = tagName + " - " + StrUtil.format(SkinUtil.LoadString(req, "res.label.forum.showtopic", "tag_single_limit"), new Object[] { new Integer(tagSingleLenMax) });
                            throw new ErrMsgException(limit);
                        }
                        tempTagNameVector.addElement(tagName);
                    }
                }
            }
        }
        return true;
    }

    /**
     * 发新贴，适用于普通和ubb方式
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param name String
     * @param TheBean FileUpload
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     */
    public boolean AddNew(ServletContext application,
                          HttpServletRequest request,
                          String name, FileUpload fu) throws
            ErrMsgException, ResKeyException {
        this.name = name;

        CheckTopic(request, fu);

        // 投票处理
        String isvote = fu.getFieldValue("isvote");
        String[] voptions = null;
        int msgType = 0;
        if (isvote != null && isvote.equals("1")) {
            String voteoption = fu.getFieldValue("vote").trim();
            if (!voteoption.equals("")) {
                voptions = voteoption.split("\n");
            }
            if (voptions != null) {
                msgType = TYPE_VOTE;
            }
        }

        String sql = "";

        int length = 0;
        if (title != null)
            length = title.length();
        if (length < MIN_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_short_title") +
                                      MIN_TOPIC_LEN); // "您输入的主题内容太短了，最短不能少于" + MIN_TOPIC_LEN);
        if (length > MAX_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_large_title") +
                                      MAX_TOPIC_LEN); // "您输入的主题内容太长了，最长不能超过" + MAX_TOPIC_LEN);
        if (content != null)
            length = content.length();
        if (length < MIN_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_short_content") +
                                      MIN_CONTENT_LEN);
        if (length > MAX_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_large_content") +
                                      MAX_CONTENT_LEN);

        id = SequenceMgr.nextID();
        int intIsBlog = blog ? 1 : 0;

        FileInfo fi = null;
        Vector v = fu.getFiles();
        int size = v.size();

        String[] fileNameAry = null;
        if (size > 0) {
            fi = (FileInfo) v.get(0); // 取得第一个附件
            // 为每个附件随机生成文件名
            fileNameAry = new String[size];
            for (int i = 0; i < size; i++) {
                fileNameAry[i] = FileUpload.getRandName() + "." +
                                 ((FileInfo) v.get(i)).getExt();
            }
        }

        String virtualpath = "";

        Config cfg = Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");

        String attachmentBasePath = request.getContextPath() + "/" + Config.getInstance().getAttachmentPath() + "/";
        if (isFtpUsed) {
            attachmentBasePath = cfg.getProperty("forum.ftpUrl");
            if (attachmentBasePath.lastIndexOf("/")!=attachmentBasePath.length()-1)
                attachmentBasePath += "/";
        }

        if (ret == FileUpload.RET_SUCCESS) {
            virtualpath = getCurAttVisualPath();

            String filepath = Global.getRealPath() + Config.getInstance().getAttachmentPath() + "/" +
                              virtualpath + "/";
            if (!isFtpUsed) {
                File f = new File(filepath);
                if (!f.isDirectory()) {
                    f.mkdirs();
                }
            }

            fu.setSavePath(filepath); // 设置保存的目录

            /*
            // 往贴子中插入附件中的图片及Flash
            Iterator ir = v.iterator();
            int k = 0;
            String imgStr = "";

            while (ir.hasNext()) {
                fi = (FileInfo) ir.next();
                String ext = fi.getExt();
                String ubbtype = "";
                if (ext.equalsIgnoreCase("gif") ||
                    ext.equalsIgnoreCase("jpg") ||
                    ext.equalsIgnoreCase("png") ||
                    ext.equalsIgnoreCase("bmp"))
                    ubbtype = "img";
                else if (ext.equalsIgnoreCase("swf"))
                    ubbtype = "flash";
                else
                    ubbtype = "URL";
                if (isWebedit == WEBEDIT_UBB) {
                    if (ubbtype.equals("img"))
                        imgStr += "\n[" + ubbtype + "]" + attachmentBasePath +
                                virtualpath + "/" +
                                fileNameAry[k] + "[/" + ubbtype + "]\n";
                    else if (ubbtype.equals("flash"))
                        imgStr += "\n[" + ubbtype + "]" + attachmentBasePath +
                                virtualpath + "/" +
                                fileNameAry[k] + "[/" +
                                ubbtype + "]\n";
                } else {
                    if (ubbtype.equals("img"))
                        imgStr += "<BR><a onfocus=this.blur() href=\"" +
                                attachmentBasePath + virtualpath + "/" +
                                fileNameAry[k] + "\" target=_blank><IMG SRC=\"" +
                                attachmentBasePath + virtualpath + "/" +
                                fileNameAry[k] + "\" border=0 alt=" +
                                SkinUtil.
                                LoadString(request, "res.cn.js.fan.util.StrUtil",
                                           "click_open_win") + " onmousewheel='return zoomimg(this)' onload=\"javascript:if(this.width>screen.width-333)this.width=screen.width-333\"></a><BR>";
                    else if (ubbtype.equals("flash"))
                        imgStr += "\n[" + ubbtype + "]" + attachmentBasePath +
                                virtualpath + "/" +
                                fileNameAry[k] + "[/" +
                                ubbtype + "]\n";
                }
                k++;
            }
            content = imgStr + content;
            */
        }

        boolean re = false;

        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            // 插入thread，thread仅用来排序、索引、列表，所以无需进行对象的缓存，只有列表的缓存
            String insertThreadSql = "insert into sq_thread (id,boardcode,msg_level,iselite,lydate,redate,name,blogUserDir,isBlog,check_status,thread_type,blog_id,blog_dir_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(insertThreadSql);
            ps.setLong(1, id);
            ps.setString(2, boardcode);
            ps.setInt(3, level);
            ps.setInt(4, isElite);
            ps.setString(5, "" + System.currentTimeMillis());
            ps.setString(6, "" + System.currentTimeMillis());
            ps.setString(7, name);
            ps.setString(8, blogUserDir);
            ps.setInt(9, blog ? 1 : 0);
            ps.setInt(10, checkStatus);
            ps.setInt(11, threadType);
            ps.setLong(12, blogId);
            ps.setString(13, blogDirCode);
            conn.executePreUpdate();

            if (ps != null) {
                ps.close();
                ps = null;
            }

            // 需使用preparestatement，比如在oracel中sql字符串的长度是有限制的 20060909
            sql = "insert into sq_message (id,rootid,boardcode,name,title,content,length,expression,lydate,ip,MSG_TYPE,show_ubbcode,show_smile,iswebedit,redate,colorExpire,boldExpire,isBlog,blogUserDir,plugin2Code,email_notify,thread_type,pluginCode,check_status,replyid,blog_id,islocked,blog_dir_code,msg_notify,sms_notify) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,-1,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setString(3, boardcode);
            ps.setString(4, name);
            ps.setString(5, title);
            ps.setString(6, content);
            ps.setInt(7, length);
            ps.setInt(8, expression);
            ps.setString(9, "" + System.currentTimeMillis());
            ps.setString(10, ip);
            ps.setInt(11, msgType);
            ps.setInt(12, show_ubbcode);
            ps.setInt(13, show_smile);
            ps.setInt(14, isWebedit);
            ps.setString(15, "" + System.currentTimeMillis());
            ps.setString(16, "" + System.currentTimeMillis());
            ps.setString(17, "" + System.currentTimeMillis());
            ps.setInt(18, intIsBlog);
            ps.setString(19, blogUserDir);
            ps.setString(20, plugin2Code);
            ps.setInt(21, email_notify);
            ps.setInt(22, threadType);
            ps.setString(23, pluginCode);
            ps.setInt(24, checkStatus);
            ps.setLong(25, blogId);
            ps.setInt(26, isLocked);
            ps.setString(27, blogDirCode);
            ps.setInt(28, msgNotify);
            ps.setInt(29, smsNotify);
            conn.executePreUpdate();

            if (ps != null) {
                ps.close();
                ps = null;
            }

            // 保存上传的图片
            if (ret == FileUpload.RET_SUCCESS && fi != null) {
                Iterator ir = v.iterator();
                // 将附件保存至数据库
                int orders = 1;
                int i = 0;

                FTPUtil ftp = new FTPUtil();
                if (isFtpUsed) {
                    boolean retFtp = ftp.connect(cfg.getProperty(
                            "forum.ftpServer"),
                                     cfg.getIntProperty("forum.ftpPort"),
                                      cfg.getProperty("forum.ftpUser"),
                                      cfg.getProperty("forum.ftpPwd"), true);
                    if (!retFtp) {
                        String errMsg = ftp.getReplyMessage();
                        ftp.close();
                        throw new ErrMsgException("FTP:" + errMsg);
                    }
                }

                int intIsRemote = isFtpUsed?1:0;

                while (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    if (isFtpUsed) {
                        try {
                            ftp.storeFile(virtualpath + "/" + fileNameAry[i], fi.getTmpFilePath());
                        }
                        catch (IOException e) {
                            LogUtil.getLog(getClass()).error("AddNew: storeFile - " + e.getMessage());
                        }
                    }
                    else {
                        fi.write(fu.getSavePath(), fileNameAry[i]);
                    }
                    long attachId = SequenceMgr.nextID(SequenceMgr.
                            SQ_MESSAGE_ATTACH);

                    String attDesc = fu.getFieldValue(fi.getFieldName() + "Desc");

                    sql =
                            "insert into sq_message_attach (id,msgId,name,diskname,visualpath,orders,UPLOAD_DATE,FILE_SIZE,USER_NAME,is_remote,ext,att_desc) values (" +
                            attachId + "," +
                            id + "," +
                            StrUtil.sqlstr(fi.getName()) + "," +
                            StrUtil.sqlstr(fileNameAry[i]) + "," +
                            StrUtil.sqlstr(virtualpath) + "," + orders +
                            "," + StrUtil.sqlstr("" + System.currentTimeMillis()) +
                            "," + fi.getSize() + "," + StrUtil.sqlstr(name) + "," + intIsRemote + "," + StrUtil.sqlstr(fi.getExt()) +
                            "," + StrUtil.sqlstr(attDesc) +
                            ")";


                    conn.executeUpdate(sql);
                    orders++;
                    i++;
                }

                if (isFtpUsed) {
                    ftp.close();
                }

                if (!name.equals("")) {
                    if (i > 0) {
                        // 加分
                        ScoreMgr sm = new ScoreMgr();
                        Vector vatt = sm.getAllScore();
                        Iterator iratt = vatt.iterator();
                        while (iratt.hasNext()) {
                            ScoreUnit su = (ScoreUnit) iratt.next();
                            IPluginScore ips = su.getScore();
                            if (ips != null)
                                ips.onAddAttachment(name, i);
                        }

                        UserPrivDb upd = new UserPrivDb();
                        upd = upd.getUserPrivDb(name);
                        upd.addAttachTodayUploadCount(i);
                    }
                }
            }

            conn.commit();

            // 匿名发贴不生成标签
            if (!name.equals("")) {
                if (cfg.getBooleanProperty("forum.isTag")) {
                    // 创建tag
                    if (tempTagNameVector != null) {
                        // 创建贴子的tag
                        TagMsgDb tmd = new TagMsgDb();
                        tmd.createForMsg(id, tempTagNameVector, name);
                    }
                }
            }

            if (voptions != null) {
                MsgPollDb mpd = new MsgPollDb();
                String epdate = StrUtil.getNullString(fu.getFieldValue(
                        "expire_date"));
                java.util.Date expireDate = DateUtil.parse(epdate, "yyyy-MM-dd");
                String strMaxChoices = StrUtil.getNullString(fu.getFieldValue(
                        "max_choice"));
                int maxChoices = StrUtil.toInt(strMaxChoices, 1);
                // 创建投票项
                mpd.create(new JdbcTemplate(), new Object[] {
                    new Long(id), expireDate, new Integer(maxChoices)
                });

                int vlen = voptions.length;
                // 创建投票选项
                MsgPollOptionDb mpod = new MsgPollOptionDb();
                for (int i = 0; i < vlen; i++) {
                    mpod.create(new JdbcTemplate(), new Object[] {
                        new Long(id), new Integer(i), voptions[i]
                    });
                }
            }

            re = true;

            // 将msgId与上传的临时图片文件相关联
            String[] tmpAttachIds = fu.getFieldValues("tmpAttachId");
            if (tmpAttachIds != null) {
                int len = tmpAttachIds.length;
                for (int k = 0; k < len; k++) {
                    Attachment att = new Attachment(Long.parseLong(tmpAttachIds[
                            k]));
                    att.setMsgId(id);
                    att.save();
                }
                if (!name.equals("")) {
                    // 加分
                    ScoreMgr sm = new ScoreMgr();
                    Vector vatt = sm.getAllScore();
                    Iterator iratt = vatt.iterator();
                    while (iratt.hasNext()) {
                        ScoreUnit su = (ScoreUnit) iratt.next();
                        IPluginScore ips = su.getScore();
                        if (ips != null)
                            ips.onAddAttachment(name, tmpAttachIds.length);
                    }
                }
            }

            // 更改用户发贴数 经验值 信用值
            if (!name.equals("")) {
                UserDb user = new UserDb();
                user = user.getUser(name);
                user.setAddCount(user.getAddCount() + 1);
                user.save();
            }
            // 更改用户博客的文章统计信息
            if (blog) {
                UserConfigDb ucd = new UserConfigDb();
                ucd = ucd.getUserConfigDb(blogId);
                ucd.setMsgCount(ucd.getMsgCount() + 1);
                ucd.setUpdateDate(new java.util.Date());
                ucd.save();
            }

            // 更改版面最新发贴信息
            if (checkStatus == CHECK_STATUS_PASS) {
                setBoardNewAddId(id);
                // 更改版面统计信息
                setBoardStatistic(true, id);
            }
        } catch (SQLException e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("AddNew:" +
                    e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (re) {
                // 更新缓存
                MsgCache mc = new MsgCache();
                mc.refreshAdd(boardcode, blogId, blog, blogUserDir);
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 用于采集时创建贴子
     * @return boolean
     */
    public boolean create() {
        boolean re = false;

        id = SequenceMgr.nextID();
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            // 插入thread，thread仅用来排序、索引、列表，所以无需进行对象的缓存，只有列表的缓存
            String sql = "insert into sq_thread (id,boardcode,msg_level,iselite,lydate,redate,name,blogUserDir,isBlog,check_status,thread_type) values (?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setString(2, boardcode);
            ps.setInt(3, level);
            ps.setInt(4, isElite);
            ps.setString(5, "" + System.currentTimeMillis());
            ps.setString(6, "" + System.currentTimeMillis());
            ps.setString(7, name);
            ps.setString(8, blogUserDir);
            ps.setInt(9, blog ? 1 : 0);
            ps.setInt(10, checkStatus);
            ps.setInt(11, threadType);
            conn.executePreUpdate();

            if (ps != null) {
                ps.close();
                ps = null;
            }

            sql = "insert into sq_message (id,rootid,boardcode,name,title,content,length,expression,lydate,ip,MSG_TYPE,show_ubbcode,show_smile,iswebedit,redate,colorExpire,boldExpire,isBlog,blogUserDir,plugin2Code,email_notify,thread_type,pluginCode,check_status,replyid) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,-1)";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setString(3, boardcode);
            ps.setString(4, name);
            ps.setString(5, title);
            ps.setString(6, content);
            ps.setInt(7, content.length());
            ps.setInt(8, expression);
            ps.setString(9, "" + System.currentTimeMillis());
            ps.setString(10, ip);
            ps.setInt(11, type);
            ps.setInt(12, show_ubbcode);
            ps.setInt(13, show_smile);
            ps.setInt(14, isWebedit);
            ps.setString(15, "" + System.currentTimeMillis());
            ps.setString(16, "" + System.currentTimeMillis());
            ps.setString(17, "" + System.currentTimeMillis());
            ps.setInt(18, blog?1:0);
            ps.setString(19, blogUserDir);
            ps.setString(20, plugin2Code);
            ps.setInt(21, email_notify);
            ps.setInt(22, threadType);
            ps.setString(23, pluginCode);
            ps.setInt(24, checkStatus);
            conn.executePreUpdate();

            if (ps != null) {
                ps.close();
                ps = null;
            }
            conn.commit();

            re = true;
        }
        catch (SQLException e) {
            conn.rollback();
            LogUtil.getLog("create:" + e.getMessage());
        }
        finally {
            if (re) {
                // 更新缓存
                MsgCache mc = new MsgCache();
                mc.refreshAdd(boardcode, blogId, blog, blogUserDir);
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 审核贴子或将其放至回收站 @task 要解决在树状结构中，当贴子被置于垃圾箱中时，其回贴未同时置于垃圾箱中
     * @param newCheckStatus int
     * @return boolean
     * @throws ResKeyException
     */
    public boolean checkMsg(int newCheckStatus) throws ResKeyException {
        checkStatus = newCheckStatus;

        String sql = "update sq_message set check_status=? where id=?";
        String tsql = "update sq_thread set check_status=? where id=?";
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, newCheckStatus);
            ps.setLong(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            ps.close();
            if (isRootMsg()) {
                ps = conn.prepareStatement(tsql);
                ps.setInt(1, newCheckStatus);
                ps.setLong(2, id);
                re = conn.executePreUpdate() == 1 ? true : false;
            }
            if (re) {
                MsgCache mc = new MsgCache();
                if (isRootMsg()) {
                    conn.commit();
                    // 审核的是主题贴
                    mc.refreshAdd(boardcode, blogId, blog, blogUserDir);
                    if (checkStatus == CHECK_STATUS_PASS) {
                        setBoardNewAddId(id);
                        // 更改版面统计信息
                        setBoardStatistic(true, id);
                    }
                } else {
                    // 审核的是回复贴
                    // 如果审核为通过状态
                    if (checkStatus == CHECK_STATUS_PASS) {
                        sql = "Update sq_thread set redate=? where id=?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, "" + System.currentTimeMillis());
                        ps.setLong(2, rootid);
                        conn.executePreUpdate();
                        if (ps != null) {
                            ps.close();
                            ps = null;
                        }

                        conn.commit();

                        // 更新主题的回复者，回复数
                        MsgDb rootmsg = getMsgDb(rootid);
                        rootmsg.setRename(name);
                        rootmsg.setRedate(new java.util.Date());
                        rootmsg.setRecount(rootmsg.getRecount() + 1);
                        try {
                            rootmsg.save();
                        } catch (ResKeyException e2) {
                            Logger.getLogger(MsgDb.class.getName()).info(
                                    "AddReplyWE2:" +
                                    StrUtil.trace(e2));
                        }
                        setBoardNewAddId(id);
                        // 更改版面统计信息
                        setBoardStatistic(false, id);
                    } else
                        conn.commit();
                    mc.refreshReply(boardcode, rootid);
                }
            }
            else
                conn.rollback();
        } catch (Exception e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("checkMsg:" +
                    e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB); // "锁定出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return re;
    }

    /**
     * 发贴、回贴或审核贴子后置本版的今日发贴数及总的发贴和回贴数
     * 注意此方法中用到了getMsgDb，所以需放在AddTopicNew、AddTopic AddReply等的末尾，以免贴子的信息(如附件)未全部写入数据库，而被提前缓存了，致某些信息(如附件)显示不出来
     * @param isAddTopic boolean true发表发新贴 false表示回贴
     * @param msgId long
     */
    public void setBoardStatistic(boolean isAddTopic, long msgId) {
        MsgDb md = getMsgDb(msgId);

        // 更新版面的当日发贴信息
        Leaf lf = new Leaf();
        lf = lf.getLeaf(boardcode);

        // 从数据库中取出今天日期
        java.util.Date d = lf.getTodayDate();
        Calendar todaydb = Calendar.getInstance();
        todaydb.setTime(d);
        Calendar today = Calendar.getInstance();

        // 如果today_date字段中为当前日期，则today_count加1
        boolean isSameDay = DateUtil.isSameDay(todaydb, today);
        if (isSameDay) {
            lf.setTodayCount(lf.getTodayCount() + 1);
        } else { // 如果字段日期与本日不一致，则说明是本日第一贴
            lf.setTodayCount(1);
            lf.setTodayDate(new java.sql.Date(today.getTimeInMillis()));
        }
        if (isAddTopic)
            lf.setTopicCount(lf.getTopicCount() + 1);
        lf.setPostCount(lf.getPostCount() + 1);
        lf.update();

        Config cfg = Config.getInstance();
        boolean topicBubbleToParentBoard = cfg.getBooleanProperty(
                "forum.topicBubbleToParentBoard");
        // 往上传递至新父节点的新加贴子的ID及更新父节点的贴子数
        if (topicBubbleToParentBoard) {
            Leaf leaf = lf.getLeaf(lf.getParentCode());

            if (isSameDay) {
                leaf.setTodayCount(leaf.getTodayCount() + 1);
            } else { // 如果字段日期与本日不一致，则说明是本日第一贴
                leaf.setTodayCount(1);
                leaf.setTodayDate(new java.sql.Date(today.getTimeInMillis()));
            }
            if (isAddTopic)
                leaf.setTopicCount(leaf.getTopicCount() + 1);
            leaf.setPostCount(leaf.getPostCount() + 1);
            leaf.update();
        }

        if (md.isBlog()) {
            // 博客贴数统计
            BlogDb bd = BlogDb.getInstance();
            bd.setStatics(isAddTopic);
        }
        // 当贴子是发至博客（不管是主题贴还是根贴），且没有发至论坛时，不计入论坛的贴数
        if (md.isBlog() && md.getboardcode().equals(Leaf.CODE_BLOG)) {
            ;
        }
        else {
            ForumDb forum = ForumDb.getInstance();
            forum.setStatics(isAddTopic);
        }
    }

    public boolean isRootMsg() {
        return id == rootid;
    }

    public static boolean isOwner(String userName, MsgDb md) {
        if (!md.isRootMsg())
            md = md.getMsgDb(md.getRootid());
        if (userName.equals(md.getName()))
            return true;
        else
            return false;
    }

    public boolean CheckTopicWE(HttpServletRequest request, FileUpload mfu) throws
            ErrMsgException {
        String errMsg = "";
        name = Privilege.getUser(request);

        title = mfu.getFieldValue("topic");
        if (title == null || title.trim().equals(""))
            errMsg += LoadString(request, "err_need_title");
        boardcode = mfu.getFieldValue("boardcode");
        if (boardcode == null || boardcode.trim().equals(""))
            errMsg += LoadString(request, "err_need_board");
        content = mfu.getFieldValue("htmlcode");

        content = AntiXSS.antiXSS(content);

        expression = StrUtil.toInt(mfu.getFieldValue("expression"), EXPRESSION_NONE);
        ip = mfu.getFieldValue("ip");

        String stremail_notify = mfu.getFieldValue("email_notify");
        if (stremail_notify == null || stremail_notify.equals(""))
            email_notify = 0;
        else {
            email_notify = Integer.parseInt(stremail_notify);
        }

        String strmsg_notify = mfu.getFieldValue("msg_notify");
        if (strmsg_notify == null || strmsg_notify.equals(""))
            msgNotify = 0;
        else
            msgNotify = Integer.parseInt(strmsg_notify);
        String strsms_notify = mfu.getFieldValue("sms_notify");
        if (strsms_notify == null || strsms_notify.equals(""))
            smsNotify = 0;
        else
            smsNotify = Integer.parseInt(strsms_notify);

        String strIsBlog = StrUtil.getNullStr(mfu.getFieldValue("isBlog"));
        if (strIsBlog.equals("1"))
            blog = true;
        else {
            if (boardcode.equals(Leaf.CODE_BLOG))
                blog = true;
            else
                blog = false;
        }
        if (blog) {
            blogUserDir = mfu.getFieldValue("blogUserDir");
            UserConfigDb ucd = new UserConfigDb();
            String strBlogId = StrUtil.getNullStr(mfu.getFieldValue("blogId"));
            blogId = UserConfigDb.NO_BLOG;
            if (!strBlogId.equals("")) {
                try {
                    blogId = Long.parseLong(strBlogId);
                } catch (Exception e) {
                }
            }
            if (blogId == UserConfigDb.NO_BLOG) {
                ucd = ucd.getUserConfigDbByUserName(Privilege.getUser(request));
            } else {
                ucd = ucd.getUserConfigDb(blogId);
            }
            blogId = ucd.getId();

            isLocked = StrUtil.toInt(mfu.getFieldValue("isLocked"), 0);

            blogDirCode = StrUtil.getNullStr(mfu.getFieldValue("blogDirCode"));

            /*
            if (!blogUserDir.equals(UserDirDb.DEFAULT)) {
                UserDirDb udd = new UserDirDb();
                udd = udd.getUserDirDb(blogId, blogUserDir);
                if (udd.isLoaded()) {
                    blogDirCode = udd.getCatalogCode();
                }
            }
            */
        }
        plugin2Code = StrUtil.getNullStr(mfu.getFieldValue("plugin2Code")).trim();
        pluginCode = StrUtil.getNullStr(mfu.getFieldValue("pluginCode")).trim();

        if (!errMsg.equals("")) {
            throw new ErrMsgException(errMsg);
        }

        // 过滤title与content
        title = ForumFilter.filterMsg(request, title);
        content = ForumFilter.filterMsg(request, content);

        isWebedit = this.WEBEDIT_REDMOON;

        // 检查发贴是否需审核
        TimeConfig tc = new TimeConfig();
        if (tc.isPostNeedCheck(request)) {
            checkStatus = CHECK_STATUS_NOT;
        } else {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(boardcode);
            checkStatus = lf.getCheckMsg() == lf.CHECK_NOT ? CHECK_STATUS_PASS :
                          CHECK_STATUS_NOT;
        }
        String strThreadType = StrUtil.getNullStr(mfu.getFieldValue(
                "threadType"));
        if (strThreadType.equals(""))
            threadType = ThreadTypeDb.THREAD_TYPE_NONE;
        else
            threadType = Integer.parseInt(strThreadType);

        Config cfg = Config.getInstance();
        Privilege pvg = new Privilege();
        // 匿名发贴不生成标签
        if (cfg.getBooleanProperty("forum.isTag") && pvg.isUserLogin(request)) {
            // 检查tag
            String tag = StrUtil.getNullStr(mfu.getFieldValue("tag")).
                         trim();
            if (!tag.equals("")) {
                // 检查总长度是否合法
                int tagLenMax = cfg.getIntProperty("forum.tagLenMax");
                if (tag.length()>tagLenMax) {
                    String limit = StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_limit"), new Object[] { new Integer(cfg.getIntProperty("forum.tagLenMax")) });
                    throw new ErrMsgException(limit);
                }
                tag = tag.replaceAll("　", " "); // 賛换全角的空格
                tag = tag.replaceAll(" +", " "); // 替换多余的空格

                // 检查是否含有非法字符，只允许中文、空格、字母及数字
                Pattern pa = Pattern.compile("[^\u4e00-\u9fa5 \\w]+",
                                             Pattern.CANON_EQ);
                Matcher m = pa.matcher(tag);
                if (m.find()) {
                    throw new ErrMsgException(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_format"));
                }

                int tagSingleLenMax = cfg.getIntProperty("forum.tagSingleLenMax");

                String[] ary = tag.split(" ");
                // 去除重复的tag
                int len = ary.length;
                tempTagNameVector = new Vector();
                for (int i = 0; i < len; i++) {
                    String tagName = ary[i];
                    boolean isRepeat = false;
                    for (int j = i + 1; j < len; j++) {
                        if (ary[j].equals(tagName)) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        if (tagName.length()>tagSingleLenMax) {
                            String limit = tagName + " - " + StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.showtopic", "tag_single_limit"), new Object[] { new Integer(tagSingleLenMax) });
                            throw new ErrMsgException(limit);
                        }
                        tempTagNameVector.addElement(tagName);
                    }
                }
            }
        }
        return true;
    }

    /**
     * 设置版块新加贴子的ID
     * @param id long
     * @return boolean
     */
    public boolean setBoardNewAddId(long id) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(boardcode);
        if (lf==null)
            return false;
        lf.setAddId(id);
        // System.out.println(getClass().getName() + " setBoardNewAddId: id=" + id);
        boolean re = lf.update();
        if (re) {
            Config cfg = Config.getInstance();
            boolean topicBubbleToParentBoard = cfg.getBooleanProperty(
                    "forum.topicBubbleToParentBoard");
            // 更新父节点的新加贴子的ID
            if (topicBubbleToParentBoard) {
                lf = lf.getLeaf(lf.getParentCode());
                lf.setAddId(id);
                lf.update();
            }
        }
        return re;
    }

    /**
     * 取得当前附件应取的虚拟路径，该路径通常处于forum/upfile目录下
     * @return String
     */
    public static String getCurAttVisualPath() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(cal.YEAR);
        int month = cal.get(cal.MONTH) + 1;
        return year + "/" + month;
    }

    /**
     * 使用高级方式发新贴
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param name String
     * @param mfu MultiFileUpload
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean AddNewWE(ServletContext application,
                            HttpServletRequest request,
                            String name, MultiFileUpload mfu) throws
            ErrMsgException {
        CheckTopicWE(request, mfu);

        // 投票处理
        int msgType = 0;
        String isvote = mfu.getFieldValue("isvote");
        String[] voptions = null;
        if (isvote != null && isvote.equals("1")) {
            String voteoption = mfu.getFieldValue("vote").trim();
            if (!voteoption.equals("")) {
                voptions = voteoption.split("\n");
            }
            if (voptions != null) { // 投票处理
                msgType = TYPE_VOTE;
            }
            // if (voteoption.indexOf("|") != -1)
            //    throw new ErrMsgException(LoadString(request, "err_vote_option")); // "投票选项中不能包含|");
        }

        int writeAttachmentResult = mfu.WRITE_ATTACHMENT_SUCCEED;

        String sql = "";

        int length = 0;
        if (title != null)
            length = title.length();

        if (length < MIN_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_short_title") +
                                      MIN_TOPIC_LEN); // "您输入的主题内容太短了，最短不能少于" + MIN_TOPIC_LEN);
        if (length > MAX_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_large_title") +
                                      MAX_TOPIC_LEN); // "您输入的主题内容太长了，最长不能超过" + MAX_TOPIC_LEN);
        if (content != null)
            length = content.length();
        if (length < MIN_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_short_content") +
                                      MIN_CONTENT_LEN);
        if (length > MAX_CONTENT_LEN_WE)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_large_content") +
                                      MAX_CONTENT_LEN);

        id = SequenceMgr.nextID();
        int intIsBlog = blog ? 1 : 0;

        int ret = mfu.getRet();

        Config cfg = Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");

        if (ret == FileUpload.RET_SUCCESS) {
            boolean isDdxc = false;
            String sisDdxc = StrUtil.getNullString(mfu.getFieldValue("isDdxc"));
            // 断点续传
            if (sisDdxc.equals("true"))
                isDdxc = true;
            String filepath = StrUtil.getNullString(mfu.getFieldValue(
                    "filepath"));

            String virtualpath = getCurAttVisualPath();

            // 2006.1.31 application.getRealPath("/")取得的d:\zjrj在插入mysql后，\不见了，变成了d:zjrj
            // 解决方法：改用Global.getRealPath
            // String tempAttachFilePath = application.getRealPath("/") + filepath +
            //                            "/";
            String tempAttachFilePath = Global.getRealPath() + Config.getInstance().getAttachmentPath() + "/" +
                                        getCurAttVisualPath() +
                                        "/";

            mfu.setSavePath(tempAttachFilePath); // 置保存目录

            File f = new File(tempAttachFilePath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            FTPUtil ftp = new FTPUtil();
            if (isFtpUsed) {
                boolean retFtp = ftp.connect(cfg.getProperty(
                        "forum.ftpServer"),
                                             cfg.getIntProperty("forum.ftpPort"),
                                             cfg.getProperty("forum.ftpUser"),
                                             cfg.getProperty("forum.ftpPwd"), true);
                if (!retFtp) {
                    ftp.close();
                    throw new ErrMsgException("FTP:" + ftp.getReplyMessage());
                }
            }

            boolean issuccess = true;
            Conn conn = new Conn(connname);
            boolean re = false;
            try {
                conn.beginTrans();
                // 插入thread，thread仅用来排序、索引、列表，所以无需缓存
                String insertThreadSql = "insert into sq_thread (id,boardcode,msg_level,iselite,lydate,redate,name,blogUserDir,isBlog,check_status,thread_type,blog_id,blog_dir_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(insertThreadSql);
                ps.setLong(1, id);
                ps.setString(2, boardcode);
                ps.setInt(3, level);
                ps.setInt(4, isElite);
                ps.setString(5, "" + System.currentTimeMillis());
                ps.setString(6, "" + System.currentTimeMillis());
                ps.setString(7, name);
                ps.setString(8, blogUserDir);
                ps.setInt(9, blog ? 1 : 0);
                ps.setInt(10, checkStatus);
                ps.setInt(11, threadType);
                ps.setLong(12, blogId);
                ps.setString(13, blogDirCode);
                conn.executePreUpdate();
                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                sql = "insert into sq_message (id,rootid,boardcode,name,title,content,length,expression,lydate,ip,MSG_TYPE,show_ubbcode,show_smile,iswebedit,redate,colorExpire,boldExpire,isBlog,blogUserDir,plugin2Code,email_notify,thread_type,pluginCode,check_status,replyid,blog_id,islocked,blog_dir_code,msg_notify,sms_notify) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,-1,?,?,?,?,?)";
                ps = conn.prepareStatement(sql);
                ps.setLong(1, id);
                ps.setLong(2, id);
                ps.setString(3, boardcode);
                ps.setString(4, name);
                ps.setString(5, title);
                ps.setString(6, content);
                ps.setInt(7, length);
                ps.setInt(8, expression);
                ps.setString(9, "" + System.currentTimeMillis());
                ps.setString(10, ip);
                ps.setInt(11, msgType);
                ps.setInt(12, show_ubbcode);
                ps.setInt(13, show_smile);
                ps.setInt(14, isWebedit);
                ps.setString(15, "" + System.currentTimeMillis());
                ps.setString(16, "" + System.currentTimeMillis());
                ps.setString(17, "" + System.currentTimeMillis());
                ps.setInt(18, intIsBlog);
                ps.setString(19, blogUserDir);
                ps.setString(20, plugin2Code);
                ps.setInt(21, email_notify);
                ps.setInt(22, threadType);
                ps.setString(23, pluginCode);
                ps.setInt(24, checkStatus);
                ps.setLong(25, blogId);
                ps.setInt(26, isLocked);
                ps.setString(27, blogDirCode);
                ps.setInt(28, msgNotify);
                ps.setInt(29, smsNotify);
                re = conn.executePreUpdate() == 1 ? true : false;
                conn.commit();

                // 匿名发贴不生成标签
                if (!name.equals("")) {
                    if (cfg.getBooleanProperty("forum.isTag")) {
                        // 创建tag
                        if (tempTagNameVector != null) {
                            // 创建贴子的tag
                            TagMsgDb tmd = new TagMsgDb();
                            tmd.createForMsg(id, tempTagNameVector, name);
                        }
                    }
                }

                if (voptions != null) {
                    MsgPollDb mpd = new MsgPollDb();
                    String epdate = StrUtil.getNullString(mfu.getFieldValue(
                            "expire_date"));
                    java.util.Date expireDate = DateUtil.parse(epdate,
                            "yyyy-MM-dd");
                    String strMaxChoices = StrUtil.getNullString(mfu.
                            getFieldValue("max_choice"));
                    int maxChoices = StrUtil.toInt(strMaxChoices, 1);
                    // 创建投票项
                    mpd.create(new JdbcTemplate(), new Object[] {
                        new Long(id), expireDate, new Integer(maxChoices)
                    });

                    int vlen = voptions.length;
                    // 创建投票选项
                    MsgPollOptionDb mpod = new MsgPollOptionDb();
                    for (int i = 0; i < vlen; i++) {
                        mpod.create(new JdbcTemplate(), new Object[] {
                            new Long(id), new Integer(i), voptions[i]
                        });
                    }
                }

                // 更改用户发贴数
                if (!name.equals("")) {
                    UserDb user = new UserDb();
                    user = user.getUser(name);
                    user.setAddCount(user.getAddCount() + 1);
                    user.save();
                }
                // 更改用户博客的文章统计信息
                if (blog) {
                    UserConfigDb ucd = new UserConfigDb();
                    ucd = ucd.getUserConfigDb(blogId);
                    ucd.setMsgCount(ucd.getMsgCount() + 1);
                    ucd.setUpdateDate(new java.util.Date());
                    ucd.save();
                }
            } catch (SQLException e) {
                conn.rollback();
                Logger.getLogger(MsgDb.class.getName()).error("AddNewWE: " +
                        e.getMessage());
                issuccess = false;
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        SkinUtil.ERR_DB));
            } catch (ResKeyException e) {
                issuccess = false;
                Logger.getLogger(MsgDb.class.getName()).error("AddNewWE: " +
                        e.getMessage(request));
                throw new ErrMsgException(e.getMessage(request));
            } finally {
                if (re) {
                    // 更新缓存
                    MsgCache mc = new MsgCache();
                    mc.refreshAdd(boardcode, blogId, blog, blogUserDir);
                }
                if (!issuccess) {
                    if (isFtpUsed)
                        ftp.close();

                   if (conn != null) {
                       conn.close();
                       conn = null;
                   }
                }
            }

            sql = "";
            int orders = 1;

            try {
                // 保存HTMLCODE中的文件
                Vector files = mfu.getFiles();
                if (isFtpUsed) {
                    Iterator ir = files.iterator();
                    while (ir.hasNext()) {
                        FileInfo fi = (FileInfo) ir.next();
                        try {
                            ftp.storeFile(virtualpath + "/" +
                                          fi.getName(),
                                          fi.getTmpFilePath());
                        } catch (IOException e) {
                            LogUtil.getLog(getClass()).error(
                                    "AddNew: storeFile - " +
                                    e.getMessage());
                        }
                    }
                } else {
                    mfu.writeFile(false); // 用文件本来的名称命名文件
                }
                // 将HTMLCODE中的文件保存至数据库
                java.util.Enumeration e = files.elements();
                while (e.hasMoreElements()) {
                    FileInfo fi = (FileInfo) e.nextElement();
                    filepath = virtualpath + "/" + fi.getName();
                    long imgId = SequenceMgr.nextID(SequenceMgr.SQ_IMAGES);
                    // System.out.println("filepath=" + filepath);
                    conn.executeUpdate(
                            "insert into sq_images (id,path,otherid,kind,is_remote) values (" +
                            imgId + "," +
                            StrUtil.sqlstr(filepath) + "," + id +
                            ",'sq_message'," + (isFtpUsed?1:0) + ")");
                }

                // 断点续传
                if (isDdxc) {
                    String[] attachFileNames = mfu.getFieldValues(
                            "attachFileName");
                    String[] clientFilePaths = mfu.getFieldValues(
                            "clientFilePath");

                    int len = 0;
                    if (attachFileNames != null)
                        len = attachFileNames.length;
                    int filenameIndex = -1;
                    String attachFileName = StrUtil.getNullString(mfu.
                            getFieldValue("filename"));
                    if (!attachFileName.equals("")) {
                        String strIndex = attachFileName.substring("attachment".
                                length(), attachFileName.length());
                        if (StrUtil.isNumeric(strIndex))
                            filenameIndex = Integer.parseInt(strIndex);
                    }
                    // 将断点续传文件的相关信息保存至数据库
                    for (int i = 0; i < len; i++) {
                        // 跳过filename文件
                        if (filenameIndex == i)
                            continue;
                        String fname = mfu.getUploadFileName(clientFilePaths[i]);
                        long attachId = SequenceMgr.nextID(SequenceMgr.
                                SQ_MESSAGE_ATTACH);
                        sql =
                                "insert into sq_message_attach (id,msgId,name,diskname,visualpath,orders,UPLOAD_DATE,USER_NAME,is_remote,ext) values (" +
                                attachId + "," +
                                id + "," +
                                StrUtil.sqlstr(fname) + "," +
                                StrUtil.sqlstr(attachFileNames[i]) + "," +
                                StrUtil.sqlstr(virtualpath) + "," + orders + "," +
                                StrUtil.sqlstr("" + System.currentTimeMillis()) +
                                "," + StrUtil.sqlstr(name) + "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(StrUtil.getFileExt(fname)) + ")";
                        conn.executeUpdate(sql);
                        orders++;
                    }
                    if (!name.equals("") && len > 0) {
                        // 加分
                        ScoreMgr sm = new ScoreMgr();
                        Vector vatt = sm.getAllScore();
                        Iterator iratt = vatt.iterator();
                        while (iratt.hasNext()) {
                            ScoreUnit su = (ScoreUnit) iratt.next();
                            IPluginScore ips = su.getScore();
                            if (ips != null)
                                ips.onAddAttachment(name, len);
                        }
                        UserPrivDb upd = new UserPrivDb();
                        upd = upd.getUserPrivDb(name);
                        upd.addAttachTodayUploadCount(len);
                    }

                } else {
                    writeAttachmentResult = mfu.writeAttachment(true); // 用随机名称命名文件

                    // 保存成功（磁盘空间允许）
                    if (writeAttachmentResult == MultiFileUpload.WRITE_ATTACHMENT_SUCCEED) {
                        Vector attachs = mfu.getAttachments();
                        Iterator ir = attachs.iterator();
                        ir = attachs.iterator();
                        // 将附件保存至数据库
                        while (ir.hasNext()) {
                            FileInfo fi = (FileInfo) ir.next();
                            long attachId = SequenceMgr.nextID(SequenceMgr.
                                    SQ_MESSAGE_ATTACH);
                            sql =
                                    "insert into sq_message_attach (id,msgId,name,diskname,visualpath,orders,UPLOAD_DATE,FILE_SIZE,USER_NAME,is_remote,ext) values (" +
                                    attachId + "," +
                                    id + "," +
                                    StrUtil.sqlstr(fi.getName()) + "," +
                                    StrUtil.sqlstr(fi.getDiskName()) + "," +
                                    StrUtil.sqlstr(virtualpath) + "," + orders +
                                    "," +
                                    StrUtil.sqlstr("" + System.currentTimeMillis()) +
                                    "," + fi.getSize() + "," +
                                    StrUtil.sqlstr(name) + "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(fi.getExt()) + ")";
                            conn.executeUpdate(sql);
                            orders++;
                        }
                        if (!name.equals("") && attachs.size() > 0) {
                            // 加分
                            ScoreMgr sm = new ScoreMgr();
                            Vector vatt = sm.getAllScore();
                            Iterator iratt = vatt.iterator();
                            while (iratt.hasNext()) {
                                ScoreUnit su = (ScoreUnit) iratt.next();
                                IPluginScore ips = su.getScore();
                                if (ips != null)
                                    ips.onAddAttachment(name, attachs.size());
                            }

                            UserPrivDb upd = new UserPrivDb();
                            upd = upd.getUserPrivDb(name);
                            upd.addAttachTodayUploadCount(attachs.size());
                        }
                    }
                }
            } catch (Exception e1) {
                Logger.getLogger(getClass()).error("AddNewWE:" +
                        e1.getMessage());
            } finally {
                if (isFtpUsed)
                    ftp.close();
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        } else {
            throw new ErrMsgException(mfu.getErrMessage(request));
        }

        // 更改版面最新发贴信息
        if (checkStatus == CHECK_STATUS_PASS) {
            setBoardNewAddId(id);
            // 更改版面统计信息,注意此方法中用到了getMsgDb，所以需放在AddTopicNew的末尾，以免贴子的信息(如附件)未全部写入数据库，而被提前缓存了，致附件显示不出来
            setBoardStatistic(true, id);
        }

        if (writeAttachmentResult == mfu.DISKSPACEUSED_TOO_LARGE)
            throw new ErrMsgException(LoadString(request, "err_space_full")); // "虚拟磁盘空间已满，附件未能写入！");
        return true;
    }

    public boolean CheckReply(HttpServletRequest req, FileUpload fu) throws
            ErrMsgException {
        String strreplyid = fu.getFieldValue("replyid");
        if (strreplyid == null)
            throw new ErrMsgException(LoadString(req, "err_need_reply_id")); // "缺少回贴标识！");
        if (!strreplyid.equals(""))
            replyid = Integer.parseInt(strreplyid);

        CheckTopic(req, fu);

        // 检查发贴是否需审核
        TimeConfig tc = new TimeConfig();
        if (tc.isPostNeedCheck(req)) {
            checkStatus = CHECK_STATUS_NOT;
        } else {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(boardcode);
            // 回贴是否需审核
            if (lf.getCheckMsg() == Leaf.CHECK_TOPIC_REPLY) {
                checkStatus = CHECK_STATUS_NOT;
            } else
                checkStatus = CHECK_STATUS_PASS;
        }
        return true;
    }

    public boolean CheckReplyWE(HttpServletRequest req, FileUpload fu) throws
            ErrMsgException {
        String strreplyid = fu.getFieldValue("replyid");
        if (strreplyid == null)
            throw new ErrMsgException(LoadString(req, "err_need_reply_id")); // "缺少回贴标识！");
        if (!strreplyid.equals(""))
            replyid = Integer.parseInt(strreplyid);
        CheckTopicWE(req, fu);
        isWebedit = WEBEDIT_REDMOON;

        // 检查发贴是否需审核
        TimeConfig tc = new TimeConfig();
        if (tc.isPostNeedCheck(req)) {
            checkStatus = CHECK_STATUS_NOT;
        } else {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(boardcode);
            if (lf.getCheckMsg() == Leaf.CHECK_TOPIC_REPLY) {
                checkStatus = CHECK_STATUS_NOT;
            } else
                checkStatus = CHECK_STATUS_PASS;
        }
        return true;
    }

    /**
     * 发回复贴（普通和UBB方式）
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param name String
     * @param fu FileUpload
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean AddReply(ServletContext application,
                                         HttpServletRequest request,
                                         String name, FileUpload fu) throws
            ErrMsgException {
        this.name = name;
        // 回复合法性检查
        CheckReply(request, fu);
        int length = 0;
        if (title != null)
            length = title.length();

        if (length < MIN_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_short_title") +
                                      MIN_TOPIC_LEN); // "您输入的主题内容太短了，最短不能少于" + MIN_TOPIC_LEN);
        if (length > MAX_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_large_title") +
                                      MAX_TOPIC_LEN); // "您输入的主题内容太长了，最长不能超过" + MAX_TOPIC_LEN);
        if (content != null)
            length = content.length();
        if (length < MIN_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_short_content") +
                                      MIN_CONTENT_LEN);
        if (length > MAX_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_large_content") +
                                      MAX_CONTENT_LEN);

        int islocked = 0;

        // 取出被回复的贴子的有关信息
        int recount = 0, layer = 1, orders = 1, parentorders = 1;
        int parentlayer = 1;
        long parentreplyid = -1;

        MsgDb msgReplied = getMsgDb(replyid);
        if (!msgReplied.isLoaded()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "err_msg_replied_lost")); // "回贴不存在！");
        }

        recount = msgReplied.getRecount();
        parentlayer = msgReplied.getLayer();
        layer = parentlayer + 1;
        rootid = msgReplied.getRootid();

        blogId = msgReplied.getBlogId();
        blog = msgReplied.isBlog();

        parentorders = msgReplied.getOrders();
        parentreplyid = msgReplied.getReplyid();
        islocked = msgReplied.getIsLocked();
        if (islocked == 1)
            throw new ErrMsgException(LoadString(request, "err_locked")); // "该贴已被锁定!");

        ResultSet rs = null;
        String sql = "";

        boolean isvalid = true;
        MsgDb rootmsg = getMsgDb(rootid);
        if (rootmsg.getIsLocked()==1) {
            throw new ErrMsgException(LoadString(request, "err_locked")); // "该贴已被锁定!");
        }
        if (!msgReplied.isRootMsg()) { // 如果被回贴不是根贴则从根贴中取出回复数
            recount = rootmsg.getRecount();
            islocked = rootmsg.getIsLocked();
            blogId = rootmsg.getBlogId();
            blog = rootmsg.isBlog();
        }

        int intIsBlog = blog ? 1 : 0;

        Conn conn = new Conn(connname);

        boolean updateorders = true;
        if (msgReplied.isRootMsg() && recount == 0)
            orders = parentorders + 1; // 如果是根贴且尚未有回贴，则orders=parentorders=1;
        else {
            if (parentreplyid == -1) { // 父结点为根贴
                orders = recount + 2;
                updateorders = false;
            } else {
                // 取出被回贴的下一个兄弟结点或当无兄弟结点时取最靠近的layer较小的结点的orders
                sql = "select min(orders) from sq_message where rootid=" + rootid + " and orders>" +
                      parentorders +
                      " and msg_layer<=" + parentlayer;
                try {
                    rs = conn.executeQuery(sql);
                    if (rs != null && rs.next())
                        orders = rs.getInt(1); // 如果orders=0，则表示未搜索到符合条件的贴子，回贴是位于最后的一个节点
                } catch (SQLException e) {
                    isvalid = false;
                    Logger.getLogger(MsgDb.class.getName()).error(
                            "AddReply SQLexception: " + e.getMessage());
                    throw new ErrMsgException(SkinUtil.LoadString(request,
                            SkinUtil.ERR_DB));
                } finally {
                    try {
                        if (rs != null)
                            rs.close();
                    } catch (Exception e) {}
                    if (!isvalid && conn != null) {
                        conn.close();
                        conn = null;
                    }
                }
                if (orders == 0) {
                    updateorders = false;
                    orders = recount + 2;
                }
            }
        }

        recount++;

        id = SequenceMgr.nextID();

        String virtualpath = "";

        FileInfo fi = null;
        Vector v = fu.getFiles();
        int size = v.size();
        String[] fileNameAry = null;
        if (size > 0) {
            fi = (FileInfo) v.get(0);
            // 为每个附件生成文件名
            fileNameAry = new String[size];
            for (int i = 0; i < size; i++) {
                fileNameAry[i] = FileUpload.getRandName() + "." +
                                 ((FileInfo) v.get(i)).getExt();
            }
        }

        int msgType = TYPE_MSG;

        Config cfg = Config.getInstance();
        String attachmentBasePath = request.getContextPath() + "/" + Config.getInstance().getAttachmentPath() + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            attachmentBasePath = cfg.getProperty("forum.ftpUrl");
            if (attachmentBasePath.lastIndexOf("/") !=
                attachmentBasePath.length() - 1)
                attachmentBasePath += "/";
        }

        if (ret == FileUpload.RET_SUCCESS) {
            virtualpath = getCurAttVisualPath();

            String filepath = Global.getRealPath() + Config.getInstance().getAttachmentPath() + "/" +
                              virtualpath + "/";
            File f = new File(filepath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            fu.setSavePath(filepath); // 设置保存的目录

            /*
            Iterator ir = v.iterator();
            int k = 0;
            String imgStr = "";
            while (ir.hasNext()) {
                fi = (FileInfo) ir.next();
                String ext = fi.getExt();
                String ubbtype = "";
                if (ext.equalsIgnoreCase("gif") ||
                    ext.equalsIgnoreCase("jpg") ||
                    ext.equalsIgnoreCase("png") ||
                    ext.equalsIgnoreCase("bmp"))
                    ubbtype = "img";
                else if (ext.equalsIgnoreCase("swf"))
                    ubbtype = "flash";
                else
                    ubbtype = "URL";

                if (isWebedit >= WEBEDIT_REDMOON) {
                    if (ubbtype.equals("img"))
                        imgStr += "<BR><a onfocus=this.blur() href=\"" +
                                attachmentBasePath + virtualpath + "/" +
                                fileNameAry[k] + "\" target=_blank><IMG SRC=\"" +
                                attachmentBasePath + virtualpath + "/" +
                                fileNameAry[k] + "\" border=0 alt=" +
                                SkinUtil.
                                LoadString(request, "res.cn.js.fan.util.StrUtil",
                                           "click_open_win") + " onmousewheel='return zoomimg(this)' onload=\"javascript:if(this.width>screen.width-333)this.width=screen.width-333\"></a><BR>";

                    else if (ubbtype.equals("flash"))
                        imgStr += "\n[" + ubbtype + "]" + attachmentBasePath +
                                virtualpath + "/" +
                                fileNameAry[k] + "[/" +
                                ubbtype + "]\n";
                } else {
                    if (ubbtype.equals("img"))
                        imgStr += "\n[" + ubbtype + "]" + attachmentBasePath +
                                virtualpath + "/" +
                                fileNameAry[k] + "[/" + ubbtype + "]\n";
                    else if (ubbtype.equals("flash"))
                        imgStr += "\n[" + ubbtype + "]" + attachmentBasePath +
                                virtualpath + "/" +
                                fileNameAry[k] + "[/" +
                                ubbtype + "]\n";
                }
                k++;
            }
            content = imgStr + content;
            */
        }

        boolean re = false;
        try {
            PreparedStatement ps = null;
            conn.beginTrans();

            if (updateorders) {
                sql = "select id from sq_message where rootid=" + rootid +
                      " and orders>=" + orders;
                rs = conn.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        MsgDb md = getMsgDb(rs.getInt(1));
                        try {
                            md.setOrders(md.getOrders() + 1);
                        } catch (ResKeyException e) {
                            throw new ErrMsgException(e.getMessage(request));
                        }
                    }
                    rs.close();
                    rs = null;
                }
            }

            sql = "insert into sq_message (id,rootid,boardcode,name,title,content,length,expression,lydate,ip,MSG_TYPE,show_ubbcode,show_smile,iswebedit,orders,msg_layer,isBlog,blogUserDir,email_notify,replyid,blog_id,pluginCode,check_status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setLong(2, rootid);
            ps.setString(3, boardcode);
            ps.setString(4, name);
            ps.setString(5, title);
            ps.setString(6, content);
            ps.setInt(7, length);
            ps.setInt(8, expression);
            ps.setString(9, "" + System.currentTimeMillis());
            ps.setString(10, ip);
            ps.setInt(11, msgType);
            ps.setInt(12, show_ubbcode);
            ps.setInt(13, show_smile);
            ps.setInt(14, isWebedit);
            ps.setInt(15, orders);
            ps.setInt(16, layer);
            ps.setInt(17, intIsBlog);
            ps.setString(18, blogUserDir);
            ps.setInt(19, email_notify);
            ps.setLong(20, replyid);
            ps.setLong(21, blogId);
            ps.setString(22, pluginCode);
            ps.setInt(23, checkStatus);

            conn.executePreUpdate();

            // 保存上传的附件
            if (ret == FileUpload.RET_SUCCESS) {
                // fi.write(TheBean.getSavePath(), picturename);
                FTPUtil ftp = new FTPUtil();
                if (isFtpUsed && v.size() > 0) {
                    boolean retFtp = ftp.connect(cfg.getProperty(
                            "forum.ftpServer"),
                                                 cfg.getIntProperty("forum.ftpPort"),
                                                 cfg.getProperty("forum.ftpUser"),
                                                 cfg.getProperty("forum.ftpPwd"), true);
                    if (!retFtp) {
                        ftp.close();
                        throw new ErrMsgException(ftp.getReplyMessage());
                    }
                }
                Iterator ir = v.iterator();
                // 将附件保存至数据库
                orders = 1;
                int i = 0;
                while (ir.hasNext()) {
                    fi = (FileInfo) ir.next();

                    if (isFtpUsed) {
                        try {
                            ftp.storeFile(virtualpath + "/" + fileNameAry[i],
                                          fi.getTmpFilePath());
                        } catch (IOException e) {
                            LogUtil.getLog(getClass()).error(
                                    "AddNew: storeFile - " +
                                    e.getMessage());
                        }
                    } else {
                        fi.write(fu.getSavePath(), fileNameAry[i]);
                    }

                    long attachId = SequenceMgr.nextID(SequenceMgr.
                            SQ_MESSAGE_ATTACH);

                    sql =
                            "insert into sq_message_attach (id,msgId,name,diskname,visualpath,orders,UPLOAD_DATE,FILE_SIZE,USER_NAME,is_remote,ext) values (" +
                            attachId + "," +
                            id + "," +
                            StrUtil.sqlstr(fi.getName()) + "," +
                            StrUtil.sqlstr(fileNameAry[i]) + "," +
                            StrUtil.sqlstr(virtualpath) +
                            "," +
                            orders +
                            "," + StrUtil.sqlstr("" + System.currentTimeMillis()) +
                            "," + fi.getSize() + "," + StrUtil.sqlstr(name) +
                            "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(fi.getExt()) +
                            ")";
                    conn.executeUpdate(sql);
                    orders++;
                    i++;
                }

                if (isFtpUsed && v.size()>0) {
                    ftp.close();
                }

                // 加分
                if (!name.equals("") && i > 0) {
                    ScoreMgr sm = new ScoreMgr();
                    Vector vatt = sm.getAllScore();
                    Iterator iratt = vatt.iterator();
                    while (iratt.hasNext()) {
                        ScoreUnit su = (ScoreUnit) iratt.next();
                        IPluginScore ips = su.getScore();
                        if (ips != null)
                            ips.onAddAttachment(name, i);
                    }

	                UserPrivDb upd = new UserPrivDb();
	                upd = upd.getUserPrivDb(name);
	                upd.addAttachTodayUploadCount(i);
                }
            }

            conn.commit();

            // 将msgId与上传的临时图片文件相关联
            String[] tmpAttachIds = fu.getFieldValues("tmpAttachId");
            if (tmpAttachIds != null) {
                int len = tmpAttachIds.length;
                for (int k = 0; k < len; k++) {
                    Attachment att = new Attachment(Long.parseLong(tmpAttachIds[
                            k]));
                    att.setMsgId(id);
                    att.save();
                }
            }

            // 更改根贴所对应用户博客的文章统计信息
            if (blog) {
                try {
                    UserConfigDb ucd = new UserConfigDb();
                    ucd = ucd.getUserConfigDb(blogId);
                    ucd.setReplyCount(ucd.getReplyCount() + 1);
                    ucd.save();
                } catch (ResKeyException e) {
                    Logger.getLogger(MsgDb.class.getName()).info(
                            "AddReply1: ResKeyException " +
                            e.getMessage());
                }
            }

            if (checkStatus == CHECK_STATUS_PASS) {
                // 更新主题的回复者，回复数
                rootmsg.setRename(name);
                rootmsg.setRedate(new java.util.Date());
                rootmsg.setRecount(rootmsg.getRecount() + 1);
                try {
                    rootmsg.save();
                }
                catch (ResKeyException e) {
                    Logger.getLogger(MsgDb.class.getName()).info(
                            "AddReply2:" +
                            e.getMessage(request));
                }

                sql = "Update sq_thread set redate=? where id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, "" + System.currentTimeMillis());
                ps.setLong(2, rootid);
                conn.executePreUpdate();
                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                // 更改版面最新发贴信息
                setBoardNewAddId(id);
                // 更改版面统计信息
                setBoardStatistic(false, id);
            }

            re = true;
        } catch (SQLException e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("AddReply3:" +
                    e.getMessage());
        } finally {
            if (re) {
                // 刷新缓存
                MsgCache mc = new MsgCache();
                mc.refreshReply(boardcode, rootid);
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        sendNotify(request, rootid, id);

        return true;
    }

    /**
     * 发新贴（高级方式，使用WebEdit控件）
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param name String
     * @param fu MultiFileUpload
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean AddReplyWE(ServletContext application,
                                           HttpServletRequest request,
                                           String name, MultiFileUpload fu) throws
            ErrMsgException {
        this.name = name;
        // 回复合法性检查
        CheckReplyWE(request, fu);

        int length = 0;
        if (title != null)
            length = title.length();
        if (length < MIN_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_short_title") +
                                      MIN_TOPIC_LEN); // "您输入的主题内容太短了，最短不能少于" + MIN_TOPIC_LEN);
        if (length > MAX_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_large_title") +
                                      MAX_TOPIC_LEN); // "您输入的主题内容太长了，最长不能超过" + MAX_TOPIC_LEN);
        if (content != null)
            length = content.length();
        if (length < MIN_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_short_content") +
                                      MIN_CONTENT_LEN);
        if (length > MAX_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_large_content") +
                                      MAX_CONTENT_LEN);

        int islocked = 0;

        int writeAttachmentResult = fu.WRITE_ATTACHMENT_SUCCEED;

        // 取出被回复的贴子的有关信息
        int recount = 0, layer = 1, orders = 1, parentorders = 1;
        int parentlayer = 1;
        long parentreplyid = -1;

        MsgDb msgReplied = getMsgDb(replyid);
        if (!msgReplied.isLoaded()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "err_msg_replied_lost")); // "回贴不存在！");
        }

        recount = msgReplied.getRecount();
        parentlayer = msgReplied.getLayer();
        layer = parentlayer + 1;
        rootid = msgReplied.getRootid();
        blogId = msgReplied.getBlogId();
        blog = msgReplied.isBlog();

        parentorders = msgReplied.getOrders();
        parentreplyid = msgReplied.getReplyid();
        islocked = msgReplied.getIsLocked();
        if (islocked == 1)
            throw new ErrMsgException(LoadString(request, "err_locked")); // "该贴已被锁定!");

        ResultSet rs = null;
        String sql = "";

        boolean isvalid = true;
        MsgDb rootmsg = getMsgDb(rootid);
        if (rootmsg.getIsLocked()==1) {
            throw new ErrMsgException(LoadString(request, "err_locked")); // "该贴已被锁定!");
        }
        if (!msgReplied.isRootMsg()) { // 如果被回贴不是根贴则从根贴中取出回复数
            recount = rootmsg.getRecount();
            islocked = rootmsg.getIsLocked();
            blogId = rootmsg.getBlogId();
            blog = rootmsg.isBlog();
        }

        Config cfg = Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");

        int intIsBlog = blog ? 1 : 0;

        Conn conn = new Conn(connname);
        boolean updateorders = true;
        if (msgReplied.isRootMsg() && recount == 0)
            orders = parentorders + 1; // 如果是根贴且尚未有回贴，则orders=parentorders=1;
        else {
            if (parentreplyid == -1) { // 父结点为根贴
                orders = recount + 2;
                updateorders = false;
            } else {
                // 取出被回贴的下一个兄弟结点或当无兄弟结点时取最靠近的layer较小的结点的orders
                sql = "select min(orders) from sq_message where rootid=" + rootid + " and orders>" +
                      parentorders +
                      " and msg_layer<=" + parentlayer;
                try {
                    rs = conn.executeQuery(sql);
                    if (rs != null && rs.next())
                        orders = rs.getInt(1); // 如果orders=0，则表示未搜索到符合条件的贴子，回贴是位于最后的一个节点
                } catch (SQLException e) {
                    isvalid = false;
                    Logger.getLogger(MsgDb.class.getName()).error(
                            "AddReply SQLexception: " + e.getMessage());
                    throw new ErrMsgException(SkinUtil.LoadString(request,
                            SkinUtil.ERR_DB));
                } finally {
                    try {
                        if (rs != null)
                            rs.close();
                    } catch (Exception e) {}
                    if (!isvalid && conn != null) {
                        conn.close();
                        conn = null;
                    }
                }
                if (orders == 0) {
                    updateorders = false;
                    orders = recount + 2;
                }
            }
        }

        recount++;

        id = SequenceMgr.nextID();

        String insertsql = "";
        FileInfo fInfo = null;
        boolean isDdxc = false;

        String virtualpath = getCurAttVisualPath();

        if (ret == FileUpload.RET_SUCCESS) {
            String sisDdxc = StrUtil.getNullString(fu.getFieldValue("isDdxc"));
            // 断点续传
            if (sisDdxc.equals("true"))
                isDdxc = true;
            String attachFilePath = Global.getRealPath() + Config.getInstance().getAttachmentPath() + "/" + virtualpath +
                                    "/";
            fu.setSavePath(attachFilePath); // 设置保存的目录
        } else {
            throw new ErrMsgException(fu.getErrMessage(request));
        }
        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    SkinUtil.ERR_SQL));

        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            boolean retFtp = ftp.connect(cfg.getProperty(
                    "forum.ftpServer"),
                                         cfg.getIntProperty("forum.ftpPort"),
                                         cfg.getProperty("forum.ftpUser"),
                                         cfg.getProperty("forum.ftpPwd"), true);
            if (!retFtp) {
                ftp.close();
                throw new ErrMsgException(ftp.getReplyMessage());
            }
        }

        boolean re = false;
        try {
            PreparedStatement ps = null;
            conn.beginTrans();

            if (updateorders) {
                sql = "select id from sq_message where rootid=" + rootid +
                      " and orders>=" + orders;
                rs = conn.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        MsgDb md = getMsgDb(rs.getInt(1));
                        try {
                            md.setOrders(md.getOrders() + 1);
                        } catch (ResKeyException e) {
                            throw new ErrMsgException(e.getMessage(request));
                        }
                    }
                    rs.close();
                    rs = null;
                }
            }

            int msgType = TYPE_MSG;
            sql = "insert into sq_message (id,rootid,boardcode,name,title,content,length,expression,lydate,ip,MSG_TYPE,show_ubbcode,show_smile,iswebedit,orders,msg_layer,isBlog,blogUserDir,email_notify,replyid,blog_id,pluginCode,check_status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setLong(2, rootid);
            ps.setString(3, boardcode);
            ps.setString(4, name);
            ps.setString(5, title);
            ps.setString(6, content);
            ps.setInt(7, length);
            ps.setInt(8, expression);
            ps.setString(9, "" + System.currentTimeMillis());
            ps.setString(10, ip);
            ps.setInt(11, msgType);
            ps.setInt(12, show_ubbcode);
            ps.setInt(13, show_smile);
            ps.setInt(14, isWebedit);
            ps.setInt(15, orders);
            ps.setInt(16, layer);
            ps.setInt(17, intIsBlog);
            ps.setString(18, blogUserDir);
            ps.setInt(19, email_notify);
            ps.setLong(20, replyid);
            ps.setLong(21, blogId);
            ps.setString(22, pluginCode);
            ps.setInt(23, checkStatus);

            conn.executePreUpdate();

            if (ps != null) {
                ps.close();
                ps = null;
            }

            int attorders = 1;

            // 保存HTMLCODE中的文件
            Vector files = fu.getFiles();
            if (isFtpUsed) {
                Iterator ir = files.iterator();
                while (ir.hasNext()) {
                    FileInfo fi = (FileInfo) ir.next();
                    try {
                        ftp.storeFile(virtualpath + "/" +
                                      fi.getName(),
                                      fi.getTmpFilePath());
                    } catch (IOException e) {
                        LogUtil.getLog(getClass()).error(
                                "AddNew: storeFile - " +
                                e.getMessage());
                    }
                }
            } else {
                fu.writeFile(false); // 用文件本来的名称命名文件
            }

            // 将HTMLCODE中的文件保存至数据库
            java.util.Enumeration e = files.elements();
            while (e.hasMoreElements()) {
                FileInfo fi = (FileInfo) e.nextElement();
                long imgId = SequenceMgr.nextID(SequenceMgr.SQ_IMAGES);
                conn.executeUpdate(
                        "insert into sq_images (id,path,otherid,kind,is_remote) values (" +
                        imgId + "," +
                        StrUtil.sqlstr(virtualpath + "/" + fi.getName()) + "," +
                        id +
                        ",'sq_message'," + (isFtpUsed?1:0) + ")");
            }

            // 断点续传
            if (isDdxc) {
                String[] attachFileNames = fu.getFieldValues(
                        "attachFileName");
                String[] clientFilePaths = fu.getFieldValues(
                        "clientFilePath");

                int len = 0;
                if (attachFileNames != null)
                    len = attachFileNames.length;

                int filenameIndex = -1;
                String attachFileName = StrUtil.getNullString(fu.getFieldValue(
                        "filename"));
                if (!attachFileName.equals("")) {
                    String strIndex = attachFileName.substring("attachment".
                            length(), attachFileName.length());
                    if (StrUtil.isNumeric(strIndex))
                        filenameIndex = Integer.parseInt(strIndex);
                }
                // 将断点续传文件的相关信息保存至数据库
                for (int i = 0; i < len; i++) {
                    // 跳过filename文件
                    if (filenameIndex == i)
                        continue;

                    String fname = fu.getUploadFileName(clientFilePaths[i]);
                    long attachId = SequenceMgr.nextID(SequenceMgr.
                            SQ_MESSAGE_ATTACH);

                    sql =
                            "insert into sq_message_attach (id,msgId,name,diskname,visualpath,orders,UPLOAD_DATE,USER_NAME,is_remote,ext) values (" +
                            attachId + "," +
                            id + "," +
                            StrUtil.sqlstr(fname) + "," +
                            StrUtil.sqlstr(attachFileNames[i]) + "," +
                            StrUtil.sqlstr(virtualpath) + "," + attorders + "," +
                            StrUtil.sqlstr("" + System.currentTimeMillis()) +
                            "," + StrUtil.sqlstr(name) + "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(StrUtil.getFileExt(fname)) + ")";
                    conn.executeUpdate(sql);
                    attorders++;
                }
                if (!name.equals("") && len > 0) {
                    // 加分
                    ScoreMgr sm = new ScoreMgr();
                    Vector vatt = sm.getAllScore();
                    Iterator iratt = vatt.iterator();
                    while (iratt.hasNext()) {
                        ScoreUnit su = (ScoreUnit) iratt.next();
                        IPluginScore ips = su.getScore();
                        if (ips != null)
                            ips.onAddAttachment(name, len);
                    }

                    UserPrivDb upd = new UserPrivDb();
                    upd = upd.getUserPrivDb(name);
                    upd.addAttachTodayUploadCount(len);
                }
            } else {
                // 将附件保存至磁盘
                writeAttachmentResult = fu.writeAttachment(true); // 用随机名称命名文件
                if (writeAttachmentResult == fu.WRITE_ATTACHMENT_SUCCEED) {
                    Vector attachs = fu.getAttachments();
                    Iterator ir = attachs.iterator();
                    // 将附件保存至数据库
                    while (ir.hasNext()) {
                        FileInfo fi = (FileInfo) ir.next();
                        long attachId = SequenceMgr.nextID(SequenceMgr.
                                SQ_MESSAGE_ATTACH);
                        sql =
                                "insert into sq_message_attach (id, msgId,name,diskname,visualpath,UPLOAD_DATE,orders,FILE_SIZE,USER_NAME,is_remote,ext) values (" +
                                attachId + "," +
                                id + "," +
                                StrUtil.sqlstr(fi.getName()) + "," +
                                StrUtil.sqlstr(fi.getDiskName()) + "," +
                                StrUtil.sqlstr(virtualpath) + "," +
                                StrUtil.sqlstr("" + System.currentTimeMillis()) +
                                "," + attorders + "," + fi.getSize() + "," +
                                StrUtil.sqlstr(name) + "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(fi.getExt()) + ")";
                        conn.executeUpdate(sql);
                    }
                    if (!name.equals("") && attachs.size() > 0) {
                        // 加分
                        ScoreMgr sm = new ScoreMgr();
                        Vector vatt = sm.getAllScore();
                        Iterator iratt = vatt.iterator();
                        while (iratt.hasNext()) {
                            ScoreUnit su = (ScoreUnit) iratt.next();
                            IPluginScore ips = su.getScore();
                            if (ips != null)
                                ips.onAddAttachment(name, attachs.size());
                        }
                        UserPrivDb upd = new UserPrivDb();
                        upd = upd.getUserPrivDb(name);
                        upd.addAttachTodayUploadCount(attachs.size());
                    }
                }
            }

            conn.commit();

            // 更改根贴所对应用户博客的文章统计信息
            if (blog) {
                try {
                    UserConfigDb ucd = new UserConfigDb();
                    ucd = ucd.getUserConfigDb(blogId);
                    ucd.setReplyCount(ucd.getReplyCount() + 1);
                    ucd.save();
                } catch (ResKeyException e1) {
                    Logger.getLogger(MsgDb.class.getName()).info(
                            "AddReplyWE1: ResKeyException " +
                            e1.getMessage());
                }
            }

            if (checkStatus == CHECK_STATUS_PASS) {
                // 更新主题的回复者，回复数
                rootmsg.setRename(name);
                rootmsg.setRedate(new java.util.Date());
                rootmsg.setRecount(rootmsg.getRecount() + 1);
                try {
                    rootmsg.save();
                }
                catch (ResKeyException e2) {
                    Logger.getLogger(MsgDb.class.getName()).info(
                            "AddReplyWE2:" +
                            e2.getMessage(request));
                }

                sql = "Update sq_thread set redate=? where id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, "" + System.currentTimeMillis());
                ps.setLong(2, rootid);
                conn.executePreUpdate();
                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                // 更改版面最新发贴信息
                setBoardNewAddId(id);
                // 更改版面统计信息
                setBoardStatistic(false, id);
            }

            re = true;
        } catch (SQLException e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("AddReplyWE3:" +
                    e.getMessage());
        } finally {
            if (re) {
                // 刷新缓存
                MsgCache mc = new MsgCache();
                mc.refreshReply(boardcode, rootid);
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
            if (isFtpUsed) {
                ftp.close();
            }
        }
        sendNotify(request, rootid, id);

        if (writeAttachmentResult == fu.DISKSPACEUSED_TOO_LARGE)
            throw new ErrMsgException(LoadString(request, "err_space_full")); // "虚拟磁盘空间已满，附件未能写入！");
        return true;
    }

    /**
     * 快速回复，无论快速回复还是正常回复，只对根贴的回复数进行记录
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param name String 回复者的用户名
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean AddQuickReply(ServletContext application,
                                              HttpServletRequest request,
                                              String name) throws
            ErrMsgException {
        int islocked = 0; // 用以检验贴子是否已被锁定

        // 回复合法性检查
        replyid = ParamUtil.getInt(request, "replyid");
        title = ParamUtil.get(request, "topic");
        content = ParamUtil.get(request, "Content");

        content = AntiXSS.antiXSS(content);

        expression = ParamUtil.getInt(request, "expression");

        // 过滤title与content
        ForumFilter.filterMsg(request, title);
        ForumFilter.filterMsg(request, content);

        int length = 0;
        if (title != null)
            length = title.length();
        if (length < MIN_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_short_title") +
                                      MIN_TOPIC_LEN); // "您输入的主题内容太短了，最短不能少于" + MIN_TOPIC_LEN);
        if (length > MAX_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_large_title") +
                                      MAX_TOPIC_LEN); // "您输入的主题内容太长了，最长不能超过" + MAX_TOPIC_LEN);
        if (content != null)
            length = content.length();
        if (length < MIN_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_short_content") +
                                      MIN_CONTENT_LEN);
        if (length > MAX_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_large_content") +
                                      MAX_CONTENT_LEN);

        String strshow_smile = ParamUtil.get(request, "show_smile");
        if (strshow_smile == null || strshow_smile.equals(""))
            show_smile = 1;
        else
            show_smile = Integer.parseInt(strshow_smile);
        String strshow_ubbcode = ParamUtil.get(request, "show_ubbcode");
        if (strshow_ubbcode == null || strshow_ubbcode.equals(""))
            show_ubbcode = 1;
        else
            show_ubbcode = Integer.parseInt(strshow_ubbcode);
        ip = request.getRemoteAddr();

        // 取出被回复的贴子的有关信息
        int recount = 0, layer = 1, orders = 1, parentorders = 1;
        int parentlayer = 1;
        long parentreplyid = -1;
        // boolean isMsgRepliedRoot = false;
        ResultSet rs = null;
        String sql;

        MsgDb msgReplied = getMsgDb(replyid);
        if (!msgReplied.isLoaded())
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "err_msg_replied_lost")); // "回贴不存在！");

        boolean isvalid = true;

        boardcode = msgReplied.getboardcode();

        // 检查发贴是否需审核
        TimeConfig tc = new TimeConfig();
        if (tc.isPostNeedCheck(request)) {
            checkStatus = CHECK_STATUS_NOT;
        } else {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(boardcode);
            if (lf.getCheckMsg() == Leaf.CHECK_TOPIC_REPLY) {
                checkStatus = CHECK_STATUS_NOT;
            } else
                checkStatus = CHECK_STATUS_PASS;
        }

        recount = msgReplied.getRecount();
        parentlayer = msgReplied.getLayer();
        layer = parentlayer + 1;
        rootid = msgReplied.getRootid();
        blogId = msgReplied.getBlogId();

        boolean isRootBlog = msgReplied.isBlog(); // 根贴是否为blog贴

        parentorders = msgReplied.getOrders();
        parentreplyid = msgReplied.replyid;
        islocked = msgReplied.getIsLocked();

        MsgDb rootmsg = getMsgDb(rootid);

        if (!msgReplied.isRootMsg()) { // 如果被回贴不是根贴则从根贴中取出回复数
            recount = rootmsg.getRecount();
            islocked = rootmsg.getIsLocked();
            blogId = rootmsg.getBlogId();
            isRootBlog = rootmsg.isBlog();
        }

        blog = isRootBlog ? true : false;
        int intIsBlog = blog ? 1 : 0;
        isWebedit = WEBEDIT_NORMAL;

        if (islocked == 1)
            throw new ErrMsgException(LoadString(request, "err_locked")); // 该贴已被锁定;

        Conn conn = new Conn(connname);
        boolean updateorders = true;
        if (msgReplied.isRootMsg() && recount == 0)
            orders = parentorders + 1; // 如果是根贴且尚未有回贴，则orders=parentorders=1;
        else {
            if (parentreplyid == -1) { // 父结点为根贴
                orders = recount + 2;
                updateorders = false;
            } else {
                // 取出被回贴的下一个兄弟结点或当无兄弟结点时取最靠近的layer较小的结点的orders
                sql = "select min(orders) from sq_message where rootid=" + rootid + " and orders>" +
                      parentorders +
                      " and msg_layer<=" + parentlayer;
                try {
                    rs = conn.executeQuery(sql);
                    if (rs != null && rs.next())
                        orders = rs.getInt(1); // 如果orders=0，则表示未搜索到符合条件的贴子，回贴是位于最后的一个节点
                } catch (SQLException e) {
                    isvalid = false;
                    Logger.getLogger(MsgDb.class.getName()).error(
                            "AddReply SQLexception: " + e.getMessage());
                    throw new ErrMsgException(SkinUtil.LoadString(request,
                            SkinUtil.ERR_DB));
                } finally {
                    try {
                        if (rs != null)
                            rs.close();
                    } catch (Exception e) {}
                    if (!isvalid && conn != null) {
                        conn.close();
                        conn = null;
                    }
                }
                // 如果orders=0，则表示未搜索到符合条件的贴子，回贴是位于最后的一个节点
                if (orders == 0) {
                    updateorders = false;
                    orders = recount + 2;
                }
            }
        }
        recount++;

        id = SequenceMgr.nextID();

        boolean re = false;
        try {
            PreparedStatement ps = null;
            conn.beginTrans();

            if (updateorders) {
                // 此处如果不更新缓存，而orders在平板列表及树状列表中在缓存中不更新并不会使其显示顺序及是否显示受到影响
                // 因为每次refreshReply缓存之后，将boardcode+rootid的组清掉了，所有平板及树状对应的sql语句的block也清掉了
                // 但是如果在删除贴子时要根据其orders去处理子节点及后继节点，所以此处必须更新缓存，在addreply中也是
                sql = "select id from sq_message where rootid=" + rootid +
                      " and orders>=" + orders;
                rs = conn.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        MsgDb msg = getMsgDb(rs.getInt(1));
                        try {
                            msg.setOrders(msg.getOrders() + 1);
                        } catch (ResKeyException e) {
                            throw new ErrMsgException(e.getMessage(request));
                        }
                    }
                    rs.close();
                    rs = null;
                }
            }

            int msgType = 0;

            sql = "insert into sq_message (id,rootid,boardcode,name,title,content,length,expression,lydate,ip,MSG_TYPE,show_ubbcode,show_smile,iswebedit,orders,msg_layer,isBlog,blogUserDir,email_notify,replyid,blog_id,pluginCode,check_status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setLong(2, rootid);
            ps.setString(3, boardcode);
            ps.setString(4, name);
            ps.setString(5, title);
            ps.setString(6, content);
            ps.setInt(7, length);
            ps.setInt(8, expression);
            ps.setString(9, "" + System.currentTimeMillis());
            ps.setString(10, ip);
            ps.setInt(11, msgType);
            ps.setInt(12, show_ubbcode);
            ps.setInt(13, show_smile);
            ps.setInt(14, isWebedit);
            ps.setInt(15, orders);
            ps.setInt(16, layer);
            ps.setInt(17, intIsBlog);
            ps.setString(18, blogUserDir);
            ps.setInt(19, email_notify);
            ps.setLong(20, replyid);
            ps.setLong(21, blogId);
            ps.setString(22, pluginCode);
            ps.setInt(23, checkStatus);
            conn.executePreUpdate();

            ps.close();
            ps = null;

            conn.commit();

            // 回贴未被审核，则不能更改主题贴的回贴数@task
            if (checkStatus == CHECK_STATUS_PASS) {
                // 更新主题的回复者，回复数
                rootmsg.setRename(name);
                rootmsg.setRedate(new java.util.Date());
                rootmsg.setRecount(rootmsg.getRecount() + 1);

                try {
                    rootmsg.save();
                }
                catch (ResKeyException e) {
                    Logger.getLogger(MsgDb.class.getName()).error(
                            "AddQuickReply1:" +
                            e.getMessage(request));
                }

                sql = "update sq_thread set redate=? where id=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, "" + System.currentTimeMillis());
                ps.setLong(2, rootid);
                conn.executePreUpdate();
                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                // 更改版面最新发贴信息
                setBoardNewAddId(id);
                // 更改版面统计信息
                setBoardStatistic(false, id);
            }

            // 更改根贴所对应用户博客的文章统计信息
            if (blog) {
                try {
                    UserConfigDb ucd = new UserConfigDb();
                    ucd = ucd.getUserConfigDb(blogId);
                    ucd.setReplyCount(ucd.getReplyCount() + 1);
                    ucd.save();
                } catch (ResKeyException e) {
                    Logger.getLogger(MsgDb.class.getName()).info(
                            "AddQuickReply2:" +
                            e.getMessage(request));
                }
            }

            re = true;
        } catch (SQLException e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("AddQuickReply3: " +
                    e.getMessage());
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    SkinUtil.ERR_DB));
        } finally {
            if (re) {
                // 刷新缓存
                MsgCache mc = new MsgCache();
                mc.refreshReply(boardcode, rootid);
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        sendNotify(request, rootid, id);

        return re;
    }

    /**
     * 贴子升降
     * @param request HttpServletRequest
     * @param id long
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean riseOrFallTopic(HttpServletRequest request, long id, java.util.Date d) throws ErrMsgException {
        boolean re = false;
        try {
            redate = d;
            re = save();
            if (re) {
                String sql = "update sq_thread set redate=? where id=?";
                JdbcTemplate jt = new JdbcTemplate();
                try {
                    jt.executeUpdate(sql, new Object[] {"" + d.getTime(), new Long(id)});
                }
                catch (SQLException e) {
                    LogUtil.getLog(getClass()).error("riseOrFallTopic:" + e.getMessage());
                    throw new ResKeyException("err_db");
                }
                MsgCache mc = new MsgCache();
                mc.refreshThreadList(boardcode);
            }
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public void sendNotify(HttpServletRequest request,
                           long rootid,
                           long id) {
        MsgDb msgRoot = getMsgDb(rootid);
        MsgDb md = getMsgDb(id);
        if (msgRoot.getName().equals(md.getName()))
            return;

        sendNotifyEmail(request, msgRoot, md);
        sendNotifyMsg(request, msgRoot, md);
        sendNotifySms(request, msgRoot, md);
    }

    /**
     * 发送EMAIL回复提醒，只提醒根贴的回复
     * @param request HttpServletRequest
     * @param parentUserName String
     * @param title String
     * @param rootid int
     * @param id int
     */
    public void sendNotifyEmail(HttpServletRequest request,
                                MsgDb msgRoot,
                                MsgDb msgReply) {
        com.redmoon.forum.Config cfg = Config.getInstance();
        boolean isEmailNotify = cfg.getBooleanProperty("forum.email_notify");
        if (!isEmailNotify)
            return;

        isEmailNotify = msgRoot.getEmailNotify() == 1;
        if (!isEmailNotify)
            return;

        try {
            SendMail sm = new SendMail();
            String mailserver = Global.getSmtpServer();
            int smtp_port = Global.getSmtpPort();
            String smtp_name = Global.getSmtpUser();
            String pwd_raw = Global.getSmtpPwd();
            sm.initSession(mailserver, smtp_port, smtp_name, pwd_raw);

            String senderName = Global.AppRealName;
            senderName = StrUtil.GBToUnicode(senderName);
            senderName += "<" + Global.getEmail() + ">";

            UserDb ud = new UserDb();
            ud = ud.getUser(msgRoot.getName());
            String to = ud.getEmail();
            String subject = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                                 "email_notify_subject");
            String c = SkinUtil.LoadString(request, "res.forum.MsgDb", "email_notify_content");

            subject = subject.replaceFirst("\\$s", Global.AppName);
            c = c.replaceFirst("\\$p", Global.getRootPath());
            c = c.replaceFirst("\\$id1", "" + msgRoot.getId());

            c = c.replaceFirst("\\$t", msgReply.getTitle());
            c = c.replaceFirst("\\$id2", "" + msgReply.getId());

            sm.initMsg(to, senderName, subject, c, true);
            sm.send();
            sm.clear();
        } catch (Exception e) {
            Logger.getLogger(MsgDb.class.getName()).error("sendNotifyEmail:" +
                    e.getMessage());
        }
    }

    /**
     * 短消息提醒
     * @param request HttpServletRequest
     * @param msgRoot MsgDb
     * @param msgReply MsgDb
     */
    public void sendNotifySms(HttpServletRequest request,
                              MsgDb msgRoot,
                              MsgDb msgReply) {
        com.redmoon.forum.Config cfg = Config.getInstance();
        boolean isSmsNotify = cfg.getBooleanProperty("forum.sms_notify");

        if (!isSmsNotify)
            return;

        if (!com.redmoon.forum.sms.SMSFactory.isUseSMS())
            return;

        isSmsNotify = msgRoot.getSmsNotify() == 1;
        if (!isSmsNotify)
            return;

        UserMgr um = new UserMgr();
        UserDb userRoot = um.getUser(msgRoot.getName());
        UserDb userReply = um.getUser(msgReply.getName());

        IMsgUtil imu = SMSFactory.getMsgUtil();

        String c = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                       "msg_notify_content");

        c = StrUtil.format(c, new String[] {Global.AppRealName,
                           userReply.getNick(),
                           msgRoot.getTitle()});

        try {
            imu.send(userRoot, c, MessageDb.USER_SYSTEM);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("sendNotifySms:" + e.getMessage());
        }

    }

    /**
     * 发送EMAIL回复提醒，只提醒根贴的回复
     * @param request HttpServletRequest
     * @param parentUserName String
     * @param title String
     * @param rootid int
     * @param id int
     */
    public void sendNotifyMsg(HttpServletRequest request,
                                MsgDb msgRoot,
                                MsgDb msgReply) {
        com.redmoon.forum.Config cfg = Config.getInstance();
        boolean isMsgNotify = cfg.getBooleanProperty("forum.msg_notify");
        if (!isMsgNotify)
            return;

        isMsgNotify = msgRoot.getMsgNotify() == 1;
        if (!isMsgNotify)
            return;

        try {
            // 发送短消息提醒用户
            MessageDb msg = new MessageDb();
            UserDb ud = new UserDb();
            ud = ud.getUser(msgRoot.getName());
            String title = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                               "msg_notify_title");
            String c = SkinUtil.LoadString(request, "res.forum.MsgDb","msg_notify_content");

            title = StrUtil.format(title, new Object[] {Global.AppRealName});

            c = StrUtil.format(c, new String[] {Global.getRootPath(), "" + msgRoot.getId(), "" + msgReply.getId(), msgReply.getTitle()});
            c = c.replaceFirst("http", "hhttttpp");

            msg.setTitle(title);

            msg.setContent(c);
            msg.setSender(msg.USER_SYSTEM);
            msg.setReceiver(msgRoot.getName());
            msg.setIp(request.getRemoteAddr());
            msg.setType(msg.TYPE_SYSTEM);
            msg.create();

        } catch (Exception e) {
            Logger.getLogger(MsgDb.class.getName()).error("sendNotifyMsg:" +
                    e.getMessage());
        }
    }

    public Vector getBoardManagers(String boardcode) {
        BoardManagerDb bmd = new BoardManagerDb();
        return bmd.getBoardManagers(boardcode);
    }

    public void loadFromDb(long id) {
        String LOAD_DOCUMENT_ATTACHMENTS =
                "SELECT id FROM sq_message_attach WHERE msgId=? order by orders";
        String QUERY_LOAD = "select title,content,name,ip,expression,show_ubbcode,show_smile,email_notify,rootid,iswebedit,id,lydate,recount,replier,redate,msg_level,MSG_TYPE,iselite,islocked,hit,orders,msg_layer,boardcode,replyid,color,isBold,colorExpire,boldExpire,isBlog,blogUserDir,plugin2Code,pluginCode,check_status,thread_type,blog_id,last_operate,msg_notify,sms_notify,score,level_expire,blog_dir_code from sq_message where id=?";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs.next()) {
                title = rs.getString(1);
                content = rs.getString(2);
                name = StrUtil.getNullStr(rs.getString(3));
                ip = rs.getString(4);
                expression = rs.getInt(5);
                show_ubbcode = rs.getInt(6);
                show_smile = rs.getInt(7);
                email_notify = rs.getInt(8);
                rootid = rs.getLong(9);
                isWebedit = rs.getInt(10);

                this.id = rs.getLong(11);
                try {
                    addDate = DateUtil.parse(rs.getString(12));
                } catch (Exception e) {
                }
                recount = rs.getInt(13);
                rename = StrUtil.getNullStr(rs.getString(14));
                try {
                    redate = DateUtil.parse(rs.getString(15));
                } catch (Exception e) {

                }
                level = rs.getInt(16);
                type = rs.getInt(17);
                isElite = rs.getInt(18);
                isLocked = rs.getInt(19);
                hit = rs.getInt(20);

                lastHit = hit;

                orders = rs.getInt(21);
                layer = rs.getInt(22);
                boardcode = rs.getString(23);
                replyid = rs.getInt(24);
                color = StrUtil.getNullString(rs.getString(25));
                bold = rs.getInt(26) == 1 ? true : false;
                if (replyid == -1) {
                    colorExpire = DateUtil.parse(rs.getString(27));
                    boldExpire = DateUtil.parse(rs.getString(28));
                }
                blog = rs.getInt(29) == 1 ? true : false;
                blogUserDir = rs.getString(30);
                plugin2Code = StrUtil.getNullString(rs.getString(31));
                pluginCode = StrUtil.getNullStr(rs.getString(32));
                checkStatus = rs.getInt(33);
                threadType = rs.getInt(34);
                blogId = rs.getLong(35);
                lastOperate = rs.getLong(36);
                msgNotify = rs.getInt(37);
                smsNotify = rs.getInt(38);
                score = rs.getDouble(39);
                levelExpire = DateUtil.parse(rs.getString(40));
                blogDirCode = StrUtil.getNullStr(rs.getString(41));

                rs.close();
                pstmt.close();

                // 取得附件
                pstmt = conn.prepareStatement(LOAD_DOCUMENT_ATTACHMENTS);
                pstmt.setLong(1, id);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        long aid = rs.getLong(1);
                        Attachment am = new Attachment(aid);
                        attachments.addElement(am);
                    }
                }
                loaded = true;

                // 取得标签
                TagMsgDb tmd = new TagMsgDb();
                tags = tmd.getTagMsgDbOfMsg(id);
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("loadFromDb: id=" +
                    this.id + " " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean isUserReplyerOfMsg(String userName) {
        String sql = "select id from sq_message where replyid=? and name=?";
        ResultSet rs = null;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setString(2, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("isUserReplyerOfMsg:" +
                    StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }

    private int hit;

    private int type;

    private String rename;
    private int isLocked;
    private java.util.Date redate;
    private int level = LEVEL_NONE;
    private int isElite = 0;

    public long getId() {
        return id;
    }

    public int getOrders() {
        return orders;
    }

    public int getLayer() {
        return layer;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getShowUbbcode() {
        return show_ubbcode;
    }

    public int getShowSmile() {
        return show_smile;
    }

    public int getEmailNotify() {
        return email_notify;
    }

    public long getRootid() {
        return rootid;
    }

    public int getExpression() {
        return expression;
    }

    public void setExpression(int expression) {
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRecount() {
        return recount;
    }

    public String getRename() {
        return rename;
    }

    public java.util.Date getRedate() {
        return redate;
    }

    public int getLevel() {
        return level;
    }

    public int getType() {
        return type;
    }

    public int getIsElite() {
        return isElite;
    }

    public int getIsLocked() {
        return isLocked;
    }

    public int getHit() {
        return hit;
    }

    public synchronized boolean delTopic(long delid) throws
            ResKeyException {
        return delTopic(delid, false);
    }

    /**
     * 用于OA中的desktop.jsp中
     * @param sql String
     * @param n int
     * @return long[]
     */
    public long[] getNewMsgs(String sql, int n) {
        // String sql = "select id from (select id,lydate from sq_thread where isBlog=0 order by lydate desc ) where rownum<=5 order by lydate desc";
        long[] v = new long[0];
        // If already in cache, return the count.
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            conn.setMaxRows(n);
            rs = conn.executeQuery(sql);
            conn.setFetchSize(n);
            v = new long[conn.getRows()];
            int id;
            int i = 0;
            while (rs.next()) {
                id = rs.getInt(1);
                v[i] = id;
                i++;
            }
        } catch (SQLException e) {
            Logger.getLogger(this.getClass().getName()).error("getNewMsgs:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取得置顶贴
     * @return long[]
     */
    public long[] getTopMsgs() {
        // 根据sql语句得出计算总数的sql查询语句
        String sql = "select id from sq_thread where msg_level=" +
                     MsgDb.LEVEL_TOP_FORUM + " and check_status=" +
                     CHECK_STATUS_PASS + " order by lydate desc";
        long[] v = new long[0];
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            rs = conn.executeQuery(sql);
            v = new long[conn.getRows()];
            int id;
            int i = 0;
            while (rs.next()) {
                id = rs.getInt(1);
                v[i] = id;
                i++;
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取出新发的贴子前n条
     * @param n int
     * @return Vector
     */
    public long[] getForumNewMsgIds(int n) {
        long[] v = new long[0];
        String sql = "select id from sq_thread order by lydate desc";
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        try {
            conn.setMaxRows(n); // 尽量减少内存的使用
            rs = conn.executeQuery(sql);
            rs.setFetchSize(n);
            v = new long[conn.getRows()];
            int i = 0;
            while (rs.next()) {
                v[i] = rs.getInt(1);
                // System.out.println("MsgDb getForumNewMsgIds id=" + v[i]);
                i++;
                if (i == n)
                    break;
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("getForumNewMsgIds:" +
                    e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     *
     * @param application ServletContext
     * @param delid String
     * @param onerror boolean 当存储插件出错时，回退用
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean delTopic(long delId, boolean onerror) throws
            ResKeyException {
        // 取得被删贴子的信息
        MsgDb mymdb = getMsgDb(delId);

        if (mymdb == null || !mymdb.isLoaded())
            throw new ResKeyException("res.forum.MsgDb", "err_msg_replied_lost");

        // 取出被回复的贴子的有关信息
        String sql = "";
        ResultSet rs = null;
        int layer = 1, orders = 1;
        long rootid = -1;

        this.id = mymdb.getId();
        layer = mymdb.getLayer();
        rootid = mymdb.getRootid();
        orders = mymdb.getOrders();
        boardcode = mymdb.getboardcode();
        name = mymdb.getName();
        level = mymdb.getLevel();
        replyid = mymdb.getReplyid();

        MsgDb rootMsgDb = getMsgDb(rootid);

        boolean updateorders = true;
        int orders1 = orders;
        String delChildrenSql = "";

        int recount = 0;

        // 如果是根贴
        if (rootid == delId) {
            // 选出将要删除的相关联的其它贴子
            delChildrenSql = "select id from sq_message where rootid=" + delId +
                             " and replyid<>-1";
            updateorders = false;
        } else {
            orders1 = 0; // 加于2005年8月17日
            sql = "select min(orders) from sq_message where rootid=" + rootid +
                  " and orders>" + orders + " and msg_layer<=" + layer;
            Conn conn = new Conn(connname);
            try {
                // 取出位于其后的第一个兄弟结点（如不存在则为第一个其父结点的兄弟结点或以此往类推）的orders
                // 即取出被删除节点的去除其自身孩子节点后，往下第一个相邻节点
                rs = conn.executeQuery(sql);
                if (rs != null && rs.next()) {
                    orders1 = rs.getInt(1);
                }
            } catch (SQLException e) {
                Logger.getLogger(MsgDb.class.getName()).error("delTopic0:" +
                        e.getMessage());
                throw new ResKeyException(SkinUtil.ERR_DB); // ErrMsgException("删除出错！");
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }

            if (orders1 == 0) { // 为0则表示不存在兄弟结点，在其后位置上也不存在其父结点的兄弟结点，或以此类推的节点
                delChildrenSql = "select id from sq_message where rootid=" +
                                 rootid +
                                 " and orders>" +
                                 orders;
                updateorders = false;
                recount = orders - 2;
            } else {
                delChildrenSql = "select id from sq_message where rootid=" +
                                 rootid +
                                 " and orders>" +
                                 orders + " and orders<" + orders1;
                recount = rootMsgDb.getRecount() - (orders1 - orders);
            }

            // Logger.getLogger(MsgDb.class.getName()).info("delTopic0:delChildrenSql=" +
            //            delChildrenSql + " orders=" + orders + " orders1=" + orders1);
        }

        Conn conn = new Conn(connname);
        // 删除其子结点及thread
        try {
            // 如果是根贴，则删除thread
            if (rootid == delId) {
                String delThreadSql = "delete from sq_thread where id=?";
                PreparedStatement ps = conn.prepareStatement(delThreadSql);
                ps.setLong(1, delId);
                conn.executePreUpdate();
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
            }

            // 删除其孩子节点
            rs = conn.executeQuery(delChildrenSql);
            if (rs != null) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    delSingleMsg(id, onerror);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("delTopic1:" +
                    e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        // 注意删除的顺序，必须先删除其孩子节点，然后再删除其本身，否则当如果为根贴时，且为blog贴时，因为删除时要getUserConfigDb，这时如果根贴先被删了，就会出错
        // 删除贴子本身
        delSingleMsg(delId, onerror);

        conn = new Conn(connname);
        try {
            if (updateorders) {
                sql = "select id from sq_message where rootid=" +
                      rootid + " and orders>=" + orders1;
                // 刷新被更新orders的贴子的缓存
                try {
                    rs = conn.executeQuery(sql);
                    if (rs != null) {
                        MsgCache mc = new MsgCache();
                        while (rs.next()) {
                            int mid = rs.getInt(1);
                            mc.refreshUpdate(mid);
                        }
                    }
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                    int dlt = orders1 - orders;
                    sql = "update sq_message set orders=orders-" + dlt +
                          " where rootid=" +
                          rootid + " and orders>=" + orders1;
                    conn.executeUpdate(sql);
                } catch (SQLException e) {
                    Logger.getLogger(MsgDb.class.getName()).error("delTopic2:" +
                            e.getMessage());
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {}
                        rs = null;
                    }
                }
            }

            // 非出错处理，非匿名，则更改用户被删贴数
            if (!onerror && !mymdb.getName().equals("")) {
                UserDb user = new UserDb();
                user = user.getUser(mymdb.getName());
                user.setDelCount(user.getDelCount() + 1);
                user.save();
            }
        } catch (Exception e) {
            Logger.getLogger(MsgDb.class.getName()).error("delTopic3:" +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
    }

    public void delMsgOfBoard(String boardCode) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        MsgDb md = null;
        String sql = "select id from sq_thread where boardcode=?";
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, boardCode);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    md = getMsgDb(rs.getInt(1));
                    md.delTopic(md.getId());
                }
            }
        } catch (Exception e) {
            Logger.getLogger(MsgDb.class.getName()).error("delMsgOfBoard: " +
                    e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public synchronized boolean delSingleMsg(long delId, boolean onerror) throws
            ResKeyException {
        MsgDb mymdb = getMsgDb(delId);
        // 删除插件中的信息
        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(mymdb.getboardcode());
        if (vplugin.size() > 0) {
            Iterator irplugin = vplugin.iterator();
            while (irplugin.hasNext()) {
                PluginUnit pu = (PluginUnit) irplugin.next();
                IPluginMsgAction ipa = pu.getMsgAction();
                // Logger.getLogger(MsgDb.class.getName()).info("plugin name:" + pu.getName(request));
                ipa.delSingleMsg(delId);
            }
        }

        // 取得被删贴子的信息
        long rootid = mymdb.getRootid();
        ResultSet rs = null;

        boolean issuccess = true;
        Conn conn = new Conn(connname);
        // 删除贴子本身
        try {
            conn.beginTrans();
            String sql = "delete from sq_message where id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, delId);
            boolean re = conn.executePreUpdate() >= 1 ? true : false;
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (!re) {
                throw new ResKeyException("res.forum.MsgDb", "err_del_topic"); // "删贴失败！");
            }

            if (mymdb.isWebedit == WEBEDIT_REDMOON) {
                // 从磁盘删除HTMLCODE中的图象
                sql =
                        "select path from sq_images where otherid=? and kind='sq_message'";
                ps = conn.prepareStatement(sql);
                ps.setLong(1, delId);
                rs = conn.executePreQuery();
                if (rs != null) {
                    String fpath = "";
                    while (rs.next()) {
                        fpath = rs.getString(1);
                        if (fpath != null) {
                            File f = new File(Global.getRealPath() +
                                    Config.getInstance().getAttachmentPath() + "/" + fpath);
                            f.delete();
                        }
                    }
                    rs.close();
                    rs = null;
                }

                if (ps != null) {
                    ps.close();
                    ps = null;
                }

                // 从数据库中删除HTMLCODE中的图像
                sql =
                        "delete from sq_images where otherid=? and kind='sq_message'";
                ps = conn.prepareStatement(sql);
                ps.setLong(1, delId);
                conn.executePreUpdate();
            }
            // 提交
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("delSingleMsg:" +
                    e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB); //"从数据库删贴失败！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        // 删除附件
        Attachment am = null;
        Vector v = mymdb.getAttachments();
        if (v != null) {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                am = (Attachment) ir.next();
                am.del();
            }

            // 非匿名用户，且非出错处理，则处理删除附件的得分
            if (!onerror && !mymdb.getName().equals("")) {
                if (v.size()>0) {
                    ScoreMgr sm = new ScoreMgr();
                    Vector vatt = sm.getAllScore();
                    Iterator iratt = vatt.iterator();
                    while (iratt.hasNext()) {
                        ScoreUnit su = (ScoreUnit) iratt.next();
                        IPluginScore ips = su.getScore();
                        if (ips != null)
                            ips.onDelAttachment(mymdb.getName(), v.size());
                    }
                }
            }
        }

        // 更改所删贴子根贴所对应用户博客的文章统计信息
        if (mymdb.isBlog()) {
            try {
                UserConfigDb ucd = new UserConfigDb();
                ucd = ucd.getUserConfigDb(mymdb.getBlogId());
                if (ucd.isLoaded()) { // 博客有可能被删除的情况
                    if (mymdb.getReplyid() == -1)
                        ucd.setMsgCount(ucd.getMsgCount() - 1);
                    else
                        ucd.setReplyCount(ucd.getReplyCount() - 1);
                    ucd.save();
                }
            } catch (ResKeyException e) {
                Logger.getLogger(MsgDb.class.getName()).info(
                        "delSingleMsg: ResKeyException " +
                        e.getMessage());
            }
        }

        // 更新主题总数，贴子总数
        boolean isToday = false;
        if (DateUtil.isSameDay(mymdb.getAddDate(), new java.util.Date()))
            isToday = true;
        Leaf lf = new Leaf();
        lf = lf.getLeaf(boardcode);
        if (lf != null) {
            if (mymdb.getReplyid() == -1)
                lf.setTopicCount(lf.getTopicCount() - 1);
            lf.setPostCount(lf.getPostCount() - 1);
            if (isToday) {
                lf.setTodayCount(lf.getTodayCount() - 1);
            }
            boolean re = lf.update();

            if (re) {
                Config cfg = Config.getInstance();
                boolean topicBubbleToParentBoard = cfg.getBooleanProperty(
                        "forum.topicBubbleToParentBoard");
                // 更新父节点的新加贴子的ID
                if (topicBubbleToParentBoard) {
                    Leaf leaf = lf.getLeaf(lf.getParentCode());
                    if (mymdb.getReplyid() == -1)
                        leaf.setTopicCount(leaf.getTopicCount() - 1);
                    leaf.setPostCount(leaf.getPostCount() - 1);
                    if (isToday) {
                        leaf.setTodayCount(leaf.getTodayCount() - 1);
                    }
                    leaf.update();
                }
            }
        }
        ForumDb fd = new ForumDb();
        fd = fd.getForumDb();
        if (mymdb.getReplyid() == -1)
            fd.setTopicCount(fd.getTopicCount() - 1);
        fd.setPostCount(fd.getPostCount() - 1);
        if (isToday) {
            fd.setTodayCount(fd.getTodayCount() - 1);
        }
        fd.save();

        if (issuccess && !onerror && !mymdb.getName().equals("")) {
            // 非匿名且不是出错回退处理，则得分处理
            ScoreMgr sm = new ScoreMgr();
            v = sm.getAllScore();
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                ScoreUnit su = (ScoreUnit) ir.next();
                IPluginScore ips = su.getScore();
                if (ips != null)
                    ips.delSingleMsg(mymdb);
            }
        }

        // 删除标签
        TagMsgDb tmd = new TagMsgDb();
        tmd.delForMsg(delId);

        // 如果贴子为论坛顶贴，则刷新论坛顶贴链表
        if (mymdb.getLevel() == this.LEVEL_TOP_FORUM) {
            ForumCache fc = new ForumCache(new ForumDb());
            fc.refreshTopMsgs();
        }
        // 刷新被删除的贴子的缓存
        MsgCache mc = new MsgCache();
        if (rootid == delId) // 如果被删除贴是根贴，则更新相应缓存
            mc.refreshDelRoot(boardcode, delId, blogId, mymdb.isBlog(),
                              blogUserDir);
        else
            mc.refreshDelReply(mymdb.getboardcode(), rootid,
                               delId);
        return issuccess;
    }

    /**
     * 置回复数
     * @param count int
     * @return boolean
     * @throws ErrMsgException
     */
    public void setRecount(int recount) {
        this.recount = recount;
    }

    /**
     * 当恢复回收站中的贴子后
     * @param msgDb
     * @throws ResKeyException
     */
    public static void afterResumeMsg(MsgDb replyMsgDb) throws ResKeyException {
    	// 如果是回贴
    	if (replyMsgDb.getReplyid()!=-1) {
	    	MsgDb rootMsgDb = replyMsgDb.getMsgDb(replyMsgDb.getRootid());
	    	// 置回复数
	        rootMsgDb.setRecount(rootMsgDb.getRecount()+1);
	        long lastReplyId = rootMsgDb.getLastReplyId();
	        // 如果该回贴是最后一个回贴，则置回复者和回复时间
	        if (lastReplyId==replyMsgDb.getId()) {
	            MsgDb replyMsg = rootMsgDb.getMsgDb(lastReplyId);
	            rootMsgDb.setRename(replyMsg.getName());
		        rootMsgDb.setRedate(replyMsg.getAddDate());
	        }
	        rootMsgDb.save();
    	}
    }

    /**
     * 当删除贴子后
     * @param msgDb
     * @throws ResKeyException
     */
    public static void afterDeleteMsg(MsgDb md) throws ResKeyException {
    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(md.getboardcode());

        // 更改版面最新发贴
		if (md.getRootid() == lf.getAddId()) {
			// 取得版面中最新的贴子ID
			long newid = md.getNewAddIdOfBoard();
			if (newid != -1)
				md.setBoardNewAddId(newid);
		}

    	if (md.getReplyid()!=-1) {
	    	// @task:在delTopic中已经根据orders对recount作了减1处理，将来需优化
	        // 贴子已在回收站中，被彻底删除时不作处理，因为在删至回收站时已处理过
	    	if (md.getCheckStatus()==MsgDb.CHECK_STATUS_DUSTBIN) {
	        	return;
	        }
	    	MsgDb rootMsgDb = md.getMsgDb(md.getRootid());
	    	// 置回复数
	        rootMsgDb.setRecount(rootMsgDb.getRecount()-1);
	        // 取最后一个回贴的ID
	        long lastReplyId = rootMsgDb.getLastReplyId();
	        // 如果最后一个回贴存在
	        if (lastReplyId!=-1) {
		        MsgDb reMsg = rootMsgDb.getMsgDb(lastReplyId);
		        // 置回复者和回复时间
		        rootMsgDb.setRename(reMsg.getName());
		        rootMsgDb.setRedate(reMsg.getAddDate());
	        }
	        else {
	        	// 如果最后一个回贴不存在
	        	rootMsgDb.setRename(null);
	        	rootMsgDb.setRedate(null);
	        }
	        rootMsgDb.save();
    	}
    }

    /**
     * 取出主题的最后一个回复贴的ID
     * @param md
     * @return -1表示没有回复
     */
	public long getLastReplyId() {
	    String sql =
	            "select id from sq_message where rootid=? and check_status=" + CHECK_STATUS_PASS + " order by lydate desc";
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    Conn conn = new Conn(connname);
	    try {
	        conn.setMaxRows(1);
	        ps = conn.prepareStatement(sql);
	        ps.setLong(1, getId());
	        rs = conn.executePreQuery();
	        conn.setFetchSize(1);
	        if (rs != null) {
	            if (rs.next()) {
	                return rs.getLong(1);
	            }
	        }
	    } catch (Exception e) {
	        Logger.getLogger(MsgDb.class.getName()).error("getNewAddIdOfBoard:" +
	                e.getMessage());
	    } finally {
	        if (conn != null) {
	            conn.close();
	            conn = null;
	        }
	    }
	    return -1;
	}

    public long getNewAddIdOfBoard() {
        String sql =
                "select id from sq_thread where boardcode=? and check_status=" + CHECK_STATUS_PASS + " order by lydate desc";
        PreparedStatement ps = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            conn.setMaxRows(1);
            ps = conn.prepareStatement(sql);
            ps.setString(1, boardcode);
            rs = conn.executePreQuery();
            conn.setFetchSize(1);
            if (rs != null) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(MsgDb.class.getName()).error("getNewAddIdOfBoard:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return -1;
    }

    public int getIsWebedit() {
        return isWebedit;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public MsgDb getMsgDb(long id) {
        MsgCache mc = new MsgCache();
        return mc.getMsgDb(id);
    }

    /**
     * 置顶
     * @param level int
     * @return boolean
     * @throws ResKeyException
     */
    public boolean setOnTop(int level, java.util.Date levelExpire) throws ResKeyException {
        String sql = "update sq_message set msg_level=?,level_expire=? where id=?";
        boolean re = false;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, level);
            ps.setString(2, DateUtil.toLongString(levelExpire));
            ps.setLong(3, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }
            sql = "update sq_thread set msg_level=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, level);
            ps.setLong(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            conn.commit();

            this.level = level;
        } catch (Exception e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("setOnTop:" +
                    e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB); // "置顶出错！");
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 更新缓存
            MsgCache mc = new MsgCache();
            mc.refreshMsgAndList(boardcode, id);
        }
        return re;
    }

    /**
     * 更改标题的颜色
     * @param id long
     * @param color String
     * @param expire Date
     * @return boolean
     * @throws ResKeyException
     */
    public boolean ChangeColor(String userName, String color, java.util.Date expire, String ip) throws
            ResKeyException {
        setColor(color);
        setColorExpire(expire);
        boolean re = save();
        if (re) {
            if (!color.equals("")) {
                MsgOperateDb mod = new MsgOperateDb();
                mod.create(new JdbcTemplate(), new Object[] {
                    new Long(SequenceMgr.nextID(SequenceMgr.TOPIC_OP)),
                            new Long(id),
                            new Integer(MsgOperateDb.OP_TYPE_COLOR), expire,
                            userName, new java.util.Date(), getTitle(), getName(),
                            getAddDate(), getboardcode(), ip
                });
            }
        }
        return re;
    }

    /**
     * 更新文章内容
     * @param id long
     * @param content String
     * @return boolean
     * @throws ResKeyException
     */
    public boolean ChangeContent(long id, String content) throws
            ResKeyException {
        MsgDb md = getMsgDb(id);
        md.setContent(content);
        return md.save();
    }

    public boolean updateScore() throws ResKeyException {
        String sql = "update sq_message set score=? where id=?";
        boolean re = false;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, score);
            ps.setLong(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                ps.close();
                sql = "update sq_thread set score=? where id=?";
                ps = conn.prepareStatement(sql);
                ps.setDouble(1, score);
                ps.setLong(2, id);
                re = conn.executePreUpdate() == 1 ? true : false;
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("save:" +
                    e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB); // ("置加粗显示出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 更新缓存
            MsgCache mc = new MsgCache();
            mc.refreshMsgAndList(boardcode, id);
        }
        return re;
    }

    public boolean save() throws ResKeyException {
        String sql = "update sq_message set isBold=?,boldExpire=?,color=?,colorExpire=?,islocked=?,content=?,replier=?,redate=?,recount=?,hit=?,last_operate=?,rootid=? where id=?";
        boolean re = false;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bold?1:0);
            ps.setString(2, DateUtil.toLongString(boldExpire));
            ps.setString(3, color);
            ps.setString(4, DateUtil.toLongString(colorExpire));
            ps.setInt(5, isLocked);
            ps.setString(6, content);
            ps.setString(7, rename);
            ps.setString(8, DateUtil.toLongString(redate));
            ps.setInt(9, recount);
            ps.setInt(10, hit); // 保存hit，当后台设置点击超过1才计数时，需要在此处进行保存，否则会出现计数被清0的问题
            ps.setLong(11, lastOperate);
            ps.setLong(12, rootid);
            ps.setLong(13, id);
            re = conn.executePreUpdate()==1;
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("save:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB); // ("置加粗显示出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 更新缓存
            MsgCache mc = new MsgCache();
            mc.refreshUpdate(id);
        }
        return re;
    }

    /**
     * 加粗标题
     * @param id long
     * @param intBold int
     * @param expire Date
     * @return boolean
     * @throws ResKeyException
     */
    public boolean ChangeBold(String userName, int intBold, java.util.Date expire, String ip) throws
            ResKeyException {
        setBold(intBold==1);
        setBoldExpire(expire);
        boolean re = save();
        if (re) {
            if (intBold!=0) {
                MsgOperateDb mod = new MsgOperateDb();
                mod.create(new JdbcTemplate(), new Object[] {
                    new Long(SequenceMgr.nextID(SequenceMgr.TOPIC_OP)),
                            new Long(id), new Integer(MsgOperateDb.OP_TYPE_BOLD),
                            expire, userName, new java.util.Date(), getTitle(),
                            getName(), getAddDate(), getboardcode(), ip
                });
            }
        }
        return re;
    }

    /**
     * 锁定贴子
     * @param id long
     * @param value int
     * @return boolean
     * @throws ResKeyException
     */
    public boolean setLocked(long id, int value) throws ResKeyException {
        MsgDb md = getMsgDb(id);
        md.setIsLocked(value);
        return md.save();
    }

    /**
     * 更改贴子所属的版块
     * @param request HttpServletRequest
     * @param id long
     * @param newboardcode String
     * @param manager String
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean ChangeBoard(HttpServletRequest request, long id,
                                            String newboardcode, String manager) throws
            ErrMsgException {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(id);
        Directory dir = new Directory();
        Leaf newlf = dir.getLeaf(newboardcode);
        if (newboardcode.equals("") || newboardcode.equals("not") || newlf==null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "sel_board"));
        }

        Leaf oldlf = dir.getLeaf(md.getboardcode());

        boardcode = md.getboardcode();

        PluginMgr pm = new PluginMgr();
        // Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
        // if (vplugin.size() > 0)
        //     throw new ErrMsgException(LoadString(request, "err_move_plugin"));
        Vector vplugin = pm.getAllPluginUnitOfBoard(newboardcode);
        if (vplugin.size() > 0) {
            // 检查目的版块所挂的插件是否为board型，如果是，当与源版块有同样插件时，则允许转移，否则不允许转移
            Iterator ir = vplugin.iterator();
            Vector vpluginsrc = pm.getAllPluginUnitOfBoard(boardcode);
            int len2 = vpluginsrc.size();
            while (ir.hasNext()) {
                PluginUnit pu = (PluginUnit)ir.next();
                if (pu.getType().equals(PluginUnit.TYPE_BOARD)) {
                    boolean isValid = false;
                    for (int i=0; i<len2; i++) {
                        PluginUnit pu2 = (PluginUnit)vpluginsrc.elementAt(i);
                        if (pu2.getCode().equals(pu.getCode())) {
                            isValid = true;
                            break;
                        }
                    }
                    if (!isValid)
                        throw new ErrMsgException(StrUtil.format(LoadString(request, "err_move_plugin"), new Object[] {pu.getName(request)}));
                }
            }
        }

        long msgid = md.getId();
        String str = LoadString(request, "info_change_board");
        String userNick = "";
        if (!manager.equals("")) {
            UserDb ud = new UserDb();
            ud = ud.getUser(manager);
            userNick = ud.getNick();
        } else
            userNick = Privilege.USER_SYSTEM;
        str = str.replaceFirst("\\$u", userNick);
        str = str.replaceFirst("\\$d",
                               ForumSkin.formatDateTime(request,
                new java.util.Date()));
        String content = md.getContent();
        boolean re = false;
        MsgCache mc = new MsgCache();
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            String sql = "select id from sq_message where rootid=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    // 清缓存
                    mc.removeFromCache(rs.getLong(1));
                }
                rs.close();
                rs = null;
            }

            if (ps != null) {
                ps.close();
                ps = null;
            }

            sql = "update sq_message set content=?,boardcode=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, content + str);
            ps.setString(2, newboardcode);
            ps.setLong(3, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }

            sql = "update sq_message set boardcode=? where rootid=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newboardcode);
            ps.setLong(2, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }

            sql = "update sq_thread set boardcode=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newboardcode);
            ps.setLong(2, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }

            conn.commit();

            re = true;

        } catch (Exception e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error(e.getMessage());
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    SkinUtil.ERR_DB));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            // 更新缓存
            mc.refreshChangeBoard(id, md.getboardcode(), newboardcode);
        }

        if (re) {
            // 操作记录
            try {
                MsgOperateDb mod = new MsgOperateDb();
                mod.create(new JdbcTemplate(), new Object[] {
                    new Long(SequenceMgr.nextID(SequenceMgr.TOPIC_OP)), new Long(id),
                            new Integer(MsgOperateDb.OP_TYPE_MOVE), null,
                            manager,
                            new java.util.Date(),md.getTitle(),md.getName(),md.getAddDate(),md.getboardcode(), request.getRemoteAddr()
                });
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
            // 发送短消息提醒用户
            MessageDb msg = new MessageDb();
            msg.setTitle(SkinUtil.LoadString(request, "res.forum.MsgDb",
                                             "shortmsg_changeboard_title"));
            String s = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                           "shortmsg_changeboard_content");
            String reason = ParamUtil.get(request, "reason");

            s = StrUtil.format(s, new String[] {md.getTitle(), oldlf.getName(),
                               newlf.getName()});
            if (!reason.equals("")) {
                s += StrUtil.format(SkinUtil.LoadString(request, "res.forum.MsgDb", "op_reason"), new Object[] {reason});
            }
            msg.setContent(s);
            msg.setSender(msg.USER_SYSTEM);
            msg.setReceiver(md.getName());
            msg.setIp(request.getRemoteAddr());
            msg.setType(msg.TYPE_SYSTEM);
            msg.create();

            // 更改版面最新发贴信息
            Leaf lf = new Leaf();
            lf = lf.getLeaf(md.getboardcode());
            if (lf.getAddId() == msgid) {
                // 取得版面中最新的贴子ID
                long newid = getNewAddIdOfBoard();
                if (newid != -1)
                    setBoardNewAddId(newid);
            }
        }
        return re;
    }

    /**
     * 更改贴子所属的子类别
     * @param request HttpServletRequest
     * @param id long
     * @param newboardcode String
     * @param threadType int
     * @param manager String
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean ChangeBoardThreadType(HttpServletRequest
            request, long id, String newboardcode, int threadType) throws
            ErrMsgException {
        MsgDb md = new MsgDb();
        md = md.getMsgDb(id);
        boardcode = md.getboardcode();

        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(newboardcode);
        if (vplugin.size() > 0) {
            // 检查目的版块所挂的插件是否为board型，如果是，当与源版块有同样插件时，则允许转移，否则不允许转移
            Iterator ir = vplugin.iterator();
            Vector vpluginsrc = pm.getAllPluginUnitOfBoard(boardcode);
            int len2 = vpluginsrc.size();
            while (ir.hasNext()) {
                PluginUnit pu = (PluginUnit)ir.next();
                if (pu.getType().equals(PluginUnit.TYPE_BOARD)) {
                    boolean isValid = false;
                    for (int i=0; i<len2; i++) {
                        PluginUnit pu2 = (PluginUnit)vpluginsrc.elementAt(i);
                        if (pu2.getCode().equals(pu.getCode())) {
                            isValid = true;
                            break;
                        }
                    }
                    if (!isValid)
                        throw new ErrMsgException(StrUtil.format(LoadString(request, "err_move_plugin"), new Object[] {pu.getName(request)}));
                }
            }
        }

        long msgid = md.getId();
        String str = LoadString(request, "info_change_board");
        String userNick = "";
        Privilege privilege = new Privilege();
        if (privilege.isUserLogin(request)) {
            UserDb ud = new UserDb();
            ud = ud.getUser(privilege.getUser(request));
            userNick = ud.getNick();
        } else
            userNick = Privilege.USER_SYSTEM;
        str = str.replaceFirst("\\$u", userNick);
        str = str.replaceFirst("\\$d",
                               ForumSkin.formatDateTime(request,
                new java.util.Date()));
        String content = md.getContent();
        boolean re = false;
        MsgCache mc = new MsgCache();
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            String sql = "select id from sq_message where rootid=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    // 清缓存
                    mc.removeFromCache(rs.getLong(1));
                }
                rs.close();
                rs = null;
            }

            if (ps != null) {
                ps.close();
                ps = null;
            }

            // 更新主题贴的内容 newboardcode及新的threadType
            sql =
                    "update sq_message set content=?,boardcode=?,thread_type=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, content + str);
            ps.setString(2, newboardcode);
            ps.setInt(3, threadType);
            ps.setLong(4, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }

            // 更新主题贴及其它贴为newboardcode及新的threadType
            sql =
                    "update sq_message set boardcode=?,thread_type=? where rootid=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newboardcode);
            ps.setInt(2, threadType);
            ps.setLong(3, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }

            // 更新thread
            sql = "update sq_thread set boardcode=?,thread_type=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newboardcode);
            ps.setInt(2, threadType);
            ps.setLong(3, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }

            conn.commit();

            re = true;

        } catch (Exception e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error(e.getMessage());
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    SkinUtil.ERR_DB));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            // 更新缓存
            mc.refreshChangeBoard(id, md.getboardcode(), newboardcode);
        }

        if (re) {
            // 发送短消息提醒用户
            MessageDb msg = new MessageDb();
            msg.setTitle(SkinUtil.LoadString(request, "res.forum.MsgDb",
                                             "shortmsg_changeboard_title"));
            String s = SkinUtil.LoadString(request, "res.forum.MsgDb",
                                           "shortmsg_changeboard_content");
            Directory dir = new Directory();
            Leaf oldlf = dir.getLeaf(md.getboardcode());
            Leaf newlf = dir.getLeaf(newboardcode);
            s = StrUtil.format(s, new String[] {md.getTitle(), oldlf.getName(),
                               newlf.getName()});
            msg.setContent(s);
            msg.setSender(msg.USER_SYSTEM);
            msg.setReceiver(md.getName());
            msg.setIp(request.getRemoteAddr());
            msg.setType(msg.TYPE_SYSTEM);
            msg.create();

            // 更改版面最新发贴信息
            Leaf lf = new Leaf();
            lf = lf.getLeaf(md.getboardcode());
            if (lf.getAddId() == msgid) {
                // 取得版面中最新的贴子ID
                long newid = getNewAddIdOfBoard();
                if (newid != -1)
                    setBoardNewAddId(newid);
            }
        }
        return re;
    }

    /**
     * 加为精华
     * @param value int
     * @return boolean
     * @throws ResKeyException
     */
    public boolean setElite(int value) throws ResKeyException {
        String sql = "update sq_message set iselite=? where id=?";
        boolean re = false;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, value);
            ps.setLong(2, id);
            conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }
            sql = "update sq_thread set iselite=? where id=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, value);
            ps.setLong(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            conn.commit();
            this.isElite = value;
            if (re) {
                if (value == 1) {
                    UserDb user = new UserDb();
                    user = user.getUser(getName());
                    user.setEliteCount(user.getEliteCount() + 1);
                    user.save();
                } else {
                    UserDb user = new UserDb();
                    user = user.getUser(getName());
                    user.setEliteCount(user.getEliteCount() - 1);
                    user.save();
                }
            }
        } catch (Exception e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error(e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        MsgCache mc = new MsgCache();
        mc.refreshMsgAndList(getboardcode(), id);

        // 处理得分
        if (re) {
            ScoreMgr sm = new ScoreMgr();
            Vector v = sm.getAllScore();
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                ScoreUnit su = (ScoreUnit) ir.next();
                IPluginScore ips = su.getScore();
                // 类是否存在
                if (ips != null)
                    ips.setElite(this, value);
            }
        }

        return re;
    }

    public boolean setOrders(int orders) throws ResKeyException {
        String sql = "update sq_message set orders=? where id=?";
        boolean re = false;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orders);
            ps.setLong(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            Logger.getLogger(MsgDb.class.getName()).error(e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        MsgCache mc = new MsgCache();
        mc.refreshUpdate(id);
        return re;
    }

    /**
     * 编辑由高级发贴方式发布的贴子
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param username String
     * @param mfu MultiFileUpload
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean editTopicWE(ServletContext application,
                                            HttpServletRequest request,
                                            String username,
                                            MultiFileUpload mfu) throws
            ErrMsgException {
        String oldBlogUserDir = blogUserDir;
        boolean oldIsBlog = blog;

        int oldThreadType = threadType;

        CheckTopicWE(request, mfu);

        int length = 0;
        if (title != null)
            length = title.length();
        if (length < MIN_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_short_title") +
                                      MIN_TOPIC_LEN); // "您输入的主题内容太短了，最短不能少于" + MIN_TOPIC_LEN);
        if (length > MAX_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_large_title") +
                                      MAX_TOPIC_LEN); // "您输入的主题内容太长了，最长不能超过" + MAX_TOPIC_LEN);
        if (content != null)
            length = content.length();
        if (length < MIN_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_short_content") +
                                      MIN_CONTENT_LEN);
        if (length > MAX_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_large_content") +
                                      MAX_CONTENT_LEN);

        int writeAttachmentResult = mfu.WRITE_ATTACHMENT_SUCCEED;

        // java.text.SimpleDateFormat formatter
        //        = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 将显示"1999-10-1 21:03:10"的格式.
        Calendar cal = Calendar.getInstance();
        java.util.Date currentTime = cal.getTime();
        String dateString = ForumSkin.formatDateTime(request, currentTime); // formatter.format(currentTime);
        String tmp = LoadString(request, "info_edit");

        UserDb ud = new UserDb();
        ud = ud.getUser(username);
        tmp = tmp.replaceFirst("\\$u", ud.getNick());
        tmp = tmp.replaceFirst("\\$d", dateString);

        String sql;
        int intIsBlog = blog ? 1 : 0;

        String virtualpath = getCurAttVisualPath();

        Config cfg = Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            boolean retFtp = ftp.connect(cfg.getProperty(
                    "forum.ftpServer"),
                                         cfg.getIntProperty("forum.ftpPort"),
                                         cfg.getProperty("forum.ftpUser"),
                                         cfg.getProperty("forum.ftpPwd"), true);
            if (!retFtp) {
                ftp.close();
                throw new ErrMsgException(ftp.getReplyMessage());
            }
        }

        int ret = mfu.getRet();
        if (ret == mfu.RET_SUCCESS) {
            String filepath = StrUtil.getNullString(mfu.getFieldValue(
                    "filepath"));
            String tempAttachFilePath = Global.getRealPath() + Config.getInstance().getAttachmentPath() + "/" +
                                        virtualpath +
                                        "/";
            mfu.setSavePath(tempAttachFilePath); // 取得目录

            if (!isFtpUsed) {
                // 创建附件目录
                File f = new File(tempAttachFilePath);
                if (!f.isDirectory()) {
                    f.mkdirs();
                }
            }

            boolean isDdxc = false;
            String sisDdxc = StrUtil.getNullString(mfu.getFieldValue(
                    "isDdxc"));
            // 断点续传
            if (sisDdxc.equals("true"))
                isDdxc = true;

            ResultSet rs = null;
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();

                content += "<div>" + tmp + "</div>";
                sql = "update sq_message set title=?,content=?,length=?,expression=?,ip=?,show_ubbcode=?,show_smile=?,email_notify=?,isBlog=?,blogUserDir=?,thread_type=?,blog_id=?,islocked=?,blog_dir_code=?,msg_notify=?,sms_notify=? where id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, title);
                ps.setString(2, content);
                ps.setInt(3, length);
                ps.setInt(4, expression);
                ps.setString(5, ip);
                ps.setInt(6, show_ubbcode);
                ps.setInt(7, show_smile);
                ps.setInt(8, email_notify);
                ps.setInt(9, intIsBlog);
                ps.setString(10, blogUserDir);
                ps.setInt(11, threadType);
                ps.setLong(12, blogId);
                ps.setInt(13, isLocked);
                ps.setString(14, blogDirCode);
                ps.setInt(15, msgNotify);
                ps.setInt(16, smsNotify);
                ps.setLong(17, id);

                boolean re = conn.executePreUpdate() == 1;
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (!re)
                    throw new ErrMsgException(SkinUtil.LoadString(request,
                            SkinUtil.ERR_DB));
                if (re) {
                    // 如果是根贴，则更新thread
                    if (replyid == -1) {
                        String updateThreadSql =
                                "update sq_thread set blogUserDir=?,isBlog=?,thread_type=?,blog_dir_code=?,blog_id=? where id=?";
                        ps = conn.prepareStatement(
                                updateThreadSql);
                        ps.setString(1, blogUserDir);
                        ps.setInt(2, intIsBlog);
                        ps.setInt(3, threadType);
                        ps.setString(4, blogDirCode);
                        ps.setLong(5, blogId);
                        ps.setLong(6, id);
                        conn.executePreUpdate();
                        if (ps != null) {
                            ps.close();
                            ps = null;
                        }
                    }
                }


                mfu.writeFile(false); // 用文件本来的名称命名文件
                // 删除HTMLCODE图像文件
                sql = "select path,is_remote from sq_images where otherid=" + id +
                      " and kind='sq_message'";

                rs = conn.executeQuery(sql);
                int isRemote = 0;
                if (rs != null) {
                    String fpath = "";
                    while (rs.next()) {
                        fpath = rs.getString(1);
                        isRemote = rs.getInt(2);
                        if (fpath != null) {
                            if (isRemote==1) {
                                ftp.del(fpath);
                            } else {
                                File virtualFile = new File(Global.getRealPath() +
                                        Config.getInstance().getAttachmentPath() +
                                        "/" + fpath);
                                virtualFile.delete();
                            }
                        }
                    }
                }
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                // 从数据库中删除HTMLCODE图像
                sql = "delete from sq_images where otherid=" + id +
                      " and kind='sq_message'";
                conn.executeUpdate(sql);

                // 保存HTMLCODE中的文件
                Vector files = mfu.getFiles();
                if (isFtpUsed) {
                    Iterator ir = files.iterator();
                    while (ir.hasNext()) {
                        FileInfo fi = (FileInfo) ir.next();
                        try {
                            ftp.storeFile(virtualpath + "/" +
                                          fi.getName(),
                                          fi.getTmpFilePath());
                        } catch (IOException e) {
                            LogUtil.getLog(getClass()).error(
                                    "editTopicWE: storeFile - " +
                                    e.getMessage());
                        }
                    }
                } else {
                    mfu.writeFile(false); // 用文件本来的名称命名文件
                }

                java.util.Enumeration e = files.elements();
                while (e.hasMoreElements()) {
                    FileInfo fi = (FileInfo) e.nextElement();
                    filepath = virtualpath + "/" + fi.getName();
                    long imgId = SequenceMgr.nextID(SequenceMgr.SQ_IMAGES);
                    sql =
                            "insert into sq_images (id,path,otherid,kind,is_remote) values (" +
                            imgId + "," +
                            StrUtil.sqlstr(filepath) + "," + id +
                            ",'sq_message'," + (isFtpUsed?1:0) + ")";
                    conn.executeUpdate(sql);
                }

                // 断点续传
                if (isDdxc) {
                    String[] attachFileNames = mfu.getFieldValues(
                            "attachFileName");
                    String[] clientFilePaths = mfu.getFieldValues(
                            "clientFilePath");

                    int len = 0;
                    if (attachFileNames != null)
                        len = attachFileNames.length;
                    int orders = 1;

                    int filenameIndex = -1;
                    String attachFileName = StrUtil.getNullString(mfu.
                            getFieldValue("filename"));
                    if (!attachFileName.equals("")) {
                        String strIndex = attachFileName.substring("attachment".
                                length(), attachFileName.length());
                        if (StrUtil.isNumeric(strIndex))
                            filenameIndex = Integer.parseInt(strIndex);
                    }
                    // 将断点续传文件的相关信息保存至数据库
                    for (int i = 0; i < len; i++) {
                        // 跳过filename文件
                        if (filenameIndex == i)
                            continue;

                        String fname = mfu.getUploadFileName(clientFilePaths[i]);
                        long attachId = SequenceMgr.nextID(SequenceMgr.
                                SQ_MESSAGE_ATTACH);

                        sql =
                                "insert into sq_message_attach (id,msgId,name,diskname,visualpath,orders,UPLOAD_DATE,USER_NAME,is_remote,ext) values (" +
                                attachId + "," +
                                id + "," +
                                StrUtil.sqlstr(fname) + "," +
                                StrUtil.sqlstr(attachFileNames[i]) + "," +
                                StrUtil.sqlstr(virtualpath) + "," + orders + "," +
                                StrUtil.sqlstr("" + System.currentTimeMillis()) +
                                "," + StrUtil.sqlstr(name) + "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(StrUtil.getFileExt(fname)) + ")";
                        conn.executeUpdate(sql);
                        orders++;
                    }
                    if (len > 0) {
                        // 加分
                        ScoreMgr sm = new ScoreMgr();
                        Vector vatt = sm.getAllScore();
                        Iterator iratt = vatt.iterator();
                        while (iratt.hasNext()) {
                            ScoreUnit su = (ScoreUnit) iratt.next();
                            IPluginScore ips = su.getScore();
                            if (ips != null)
                                ips.onAddAttachment(name, len);
                        }

                        UserPrivDb upd = new UserPrivDb();
                        upd = upd.getUserPrivDb(name);
                        upd.addAttachTodayUploadCount(len);
                    }
                } else {
                    // 将附件保存至磁盘
                    writeAttachmentResult = mfu.writeAttachment(true); // 用随机名称命名文件
                    if (writeAttachmentResult == mfu.WRITE_ATTACHMENT_SUCCEED) {
                        Vector attachs = mfu.getAttachments();
                        Iterator ir = attachs.iterator();
                        // 将附件保存至数据库
                        while (ir.hasNext()) {
                            FileInfo fi = (FileInfo) ir.next();
                            long attachId = SequenceMgr.nextID(SequenceMgr.
                                    SQ_MESSAGE_ATTACH);
                            sql =
                                    "insert into sq_message_attach (id,msgId,name,diskname,visualpath,UPLOAD_DATE,FILE_SIZE,USER_NAME,is_remote,ext) values (" +
                                    attachId + "," +
                                    id + "," +
                                    StrUtil.sqlstr(fi.getName()) + "," +
                                    StrUtil.sqlstr(fi.getDiskName()) + "," +
                                    StrUtil.sqlstr(virtualpath) + "," +
                                    StrUtil.
                                    sqlstr("" + System.currentTimeMillis()) +
                                    "," + fi.getSize() + "," +
                                    StrUtil.sqlstr(name) + "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(fi.getExt()) + ")";
                            conn.executeUpdate(sql);
                        }
                        if (attachs.size() > 0) {
                            // 加分
                            ScoreMgr sm = new ScoreMgr();
                            Vector vatt = sm.getAllScore();
                            Iterator iratt = vatt.iterator();
                            while (iratt.hasNext()) {
                                ScoreUnit su = (ScoreUnit) iratt.next();
                                IPluginScore ips = su.getScore();
                                if (ips != null)
                                    ips.onAddAttachment(name, attachs.size());
                            }

                            UserPrivDb upd = new UserPrivDb();
                            upd = upd.getUserPrivDb(name);
                            upd.addAttachTodayUploadCount(attachs.size());
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new ErrMsgException("MsgDb.editTopicWE failed！" +
                                          e.getMessage());
            } finally {
                // 更新缓存
                MsgCache mc = new MsgCache();
                if (oldThreadType != threadType) {
                    mc.refreshThreadList(boardcode);
                }
                if (!blogUserDir.equals(oldBlogUserDir) ||
                    (oldIsBlog != blog))
                    mc.refreshUpdate(id, blogId, blog, blogUserDir,
                                     oldBlogUserDir);
                else
                    mc.refreshUpdate(id);

                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {}
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
                if (isFtpUsed) {
                    ftp.close();
                }
            }
        } else {
            throw new ErrMsgException(mfu.getErrMessage(request));
        }

        // 匿名贴子不生成标签
        if (!username.equals("")) {
            if (cfg.getBooleanProperty("forum.isTag")) {
                // 更新tag
                TagMsgDb tmd = new TagMsgDb();
                tmd.editForMsg(id, tempTagNameVector, username);
            }
        }
        if (writeAttachmentResult == mfu.DISKSPACEUSED_TOO_LARGE)
            throw new ErrMsgException(LoadString(request, "err_space_full")); // "虚拟磁盘空间已满，附件未能写入！");
        return true;
    }

    /**
     * 编辑普通和UBB方式发的贴子
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param username String
     * @param fu FileUpload
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean editTopic(ServletContext application,
                                          HttpServletRequest request,
                                          String username, FileUpload fu) throws
            ErrMsgException {
        String streditid = fu.getFieldValue("editid");
        if (streditid == null || !StrUtil.isNumeric(streditid))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_id")); // "请选择要编辑的贴子！");
        long editid = Long.parseLong(streditid);

        String oldBlogUserDir = blogUserDir;
        boolean oldIsBlog = blog;

        UserDb ud = new UserDb();
        ud = ud.getUser(username);

        int oldThreadType = threadType;

        CheckTopic(request, fu);

        int length = 0;
        if (title != null)
            length = title.length();
        if (length < MIN_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_short_title") +
                                      MIN_TOPIC_LEN); // "您输入的主题内容太短了，最短不能少于" + MIN_TOPIC_LEN);
        if (length > MAX_TOPIC_LEN)
            throw new ErrMsgException(LoadString(request, "err_too_large_title") +
                                      MAX_TOPIC_LEN); // "您输入的主题内容太长了，最长不能超过" + MAX_TOPIC_LEN);
        if (content != null)
            length = content.length();
        if (length < MIN_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_short_content") +
                                      MIN_CONTENT_LEN);
        if (length > MAX_CONTENT_LEN)
            throw new ErrMsgException(LoadString(request,
                                                 "err_too_large_content") +
                                      MAX_CONTENT_LEN);

        Calendar cal = Calendar.getInstance();
        java.util.Date currentTime = cal.getTime();
        String dateString = ForumSkin.formatDateTime(request, currentTime);
        String sql;

        int intIsBlog = blog ? 1 : 0;

        FileInfo fi = null;
        Vector v = fu.getFiles();
        int size = v.size();
        String[] fileNameAry = null;
        if (size > 0) {
            fi = (FileInfo) v.get(0); // 取得第一个附件
            // 为每个附件生成文件名
            fileNameAry = new String[size];
            for (int i = 0; i < size; i++) {
                fileNameAry[i] = FileUpload.getRandName() + "." +
                                 ((FileInfo) v.get(i)).getExt();
            }
        }

        Config cfg = Config.getInstance();
        String attachmentBasePath = request.getContextPath() + "/" + cfg.getAttachmentPath() + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed && v.size()>0) {
            attachmentBasePath = cfg.getProperty("forum.ftpUrl");
            if (attachmentBasePath.lastIndexOf("/") !=
                attachmentBasePath.length() - 1)
                attachmentBasePath += "/";
        }

        String virtualpath = "";
        if (ret == 1) { // 上传了图片
            virtualpath = getCurAttVisualPath();
            String filepath = Global.getRealPath() +
                              Config.getInstance().getAttachmentPath() + "/" +
                              virtualpath + "/";
            File f = new File(filepath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            fu.setSavePath(filepath); // 设置保存的目录
            /*
            String ubbtype = "";

            Iterator ir = v.iterator();
            int k = 0;
            while (ir.hasNext()) {
                fi = (FileInfo) ir.next();
                String ext = fi.getExt();
                if (ext.equalsIgnoreCase("gif") ||
                    ext.equalsIgnoreCase("jpg") ||
                    ext.equalsIgnoreCase("png") ||
                    ext.equalsIgnoreCase("bmp"))
                    ubbtype = "img";
                else if (ext.equalsIgnoreCase("swf"))
                    ubbtype = "flash";
                else
                    ubbtype = "URL";

                if (isWebedit == WEBEDIT_UBB) {
                    if (ubbtype.equals("img"))
                        content = "\n[" + ubbtype + "]" + attachmentBasePath +
                                  virtualpath + "/" +
                                  fileNameAry[k] + "[/" + ubbtype + "]\n" +
                                  content;
                    else if (ubbtype.equals("flash"))
                        content = "\n[" + ubbtype + "]" + attachmentBasePath +
                                  virtualpath + "/" +
                                  fileNameAry[k] + "[/" +
                                  ubbtype + "]\n" + content;
                } else {
                    if (ubbtype.equals("img"))
                        content = "<BR><a onfocus=this.blur() href=\"" +
                                  attachmentBasePath + virtualpath + "/" +
                                  fileNameAry[k] +
                                  "\" target=_blank><IMG SRC=\"" + attachmentBasePath + virtualpath + "/" + fileNameAry[k] +
                                  "\" border=0 alt=" +
                                  SkinUtil.LoadString(request,
                                "res.cn.js.fan.util.StrUtil", "click_open_win") + " onmousewheel='return zoomimg(this)' onload=\"javascript:if(this.width>screen.width-333)this.width=screen.width-333\"></a><BR>" +
                                  content;
                    else if (ubbtype.equals("flash"))
                        content = "\n[" + ubbtype + "]" + attachmentBasePath +
                                  virtualpath + "/" +
                                  fileNameAry[k] + "[/" +
                                  ubbtype + "]\n" + content;
                }
                k++;
            }
            */

            String tmp = "";
            if (isWebedit == WEBEDIT_UBB)
                tmp = LoadString(request, "info_edit_ubb");
            else
                tmp = LoadString(request, "info_edit");

            tmp = tmp.replaceFirst("\\$u", ud.getNick());
            tmp = tmp.replaceFirst("\\$d", dateString);

            if (isWebedit == WEBEDIT_UBB)
                content += tmp;
            else
                content += tmp;

        } else {
            String tmp = "";
            if (isWebedit == WEBEDIT_UBB)
                tmp = LoadString(request, "info_edit_ubb");
            else
                tmp = LoadString(request, "info_edit");

            tmp = tmp.replaceFirst("\\$u", ud.getNick());
            tmp = tmp.replaceFirst("\\$d", dateString);

            if (isWebedit == WEBEDIT_UBB)
                content += tmp;
            else
                content += tmp;
        }

        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            // 如果是根贴，则更新thread
            if (replyid == -1) {
                String updateThreadSql =
                        "update sq_thread set blogUserDir=?,isBlog=?,thread_type=?,blog_dir_code=?,blog_id=? where id=?";
                PreparedStatement ps = conn.prepareStatement(updateThreadSql);
                ps.setString(1, blogUserDir);
                ps.setInt(2, intIsBlog);
                ps.setInt(3, threadType);
                ps.setString(4, blogDirCode);
                ps.setLong(5, blogId);
                ps.setLong(6, editid);
                conn.executePreUpdate();
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
            }

            sql = "update sq_message set title=?,content=?,length=?,expression=?,ip=?,show_ubbcode=?,show_smile=?,email_notify=?,isBlog=?,blogUserDir=?,thread_type=?,blog_id=?,islocked=?,blog_dir_code=?,msg_notify=?,sms_notify=? where id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, length);
            ps.setInt(4, expression);
            ps.setString(5, ip);
            ps.setInt(6, show_ubbcode);
            ps.setInt(7, show_smile);
            ps.setInt(8, email_notify);
            ps.setInt(9, intIsBlog);
            ps.setString(10, blogUserDir);
            ps.setInt(11, threadType);
            ps.setLong(12, blogId);
            ps.setInt(13, isLocked);
            ps.setString(14, blogDirCode);
            ps.setInt(15, msgNotify);
            ps.setInt(16, smsNotify);
            ps.setLong(17, editid);

            re = conn.executePreUpdate() == 1;
            if (ps != null) {
                ps.close();
                ps = null;
            }

            if (re) {
                // 保存附件
                if (ret == FileUpload.RET_SUCCESS && fi != null) {
                    // fi.write(fu.getSavePath(), picturename);
                    Iterator ir = v.iterator();
                    int orders = attachments == null ? 0 :
                                 attachments.size() + 1;
                    int i = 0;

                    FTPUtil ftp = new FTPUtil();
                    if (isFtpUsed) {
                        boolean retFtp = ftp.connect(cfg.getProperty(
                                "forum.ftpServer"),
                                cfg.getIntProperty("forum.ftpPort"),
                                cfg.getProperty("forum.ftpUser"),
                                cfg.getProperty("forum.ftpPwd"), true);
                        if (!retFtp) {
                            ftp.close();
                            throw new ErrMsgException(ftp.getReplyMessage());
                        }
                    }
                    while (ir.hasNext()) {
                        fi = (FileInfo) ir.next();
                        if (isFtpUsed) {
                            try {
                                ftp.storeFile(virtualpath + "/" + fileNameAry[i],
                                              fi.getTmpFilePath());
                            } catch (IOException e) {
                                LogUtil.getLog(getClass()).error(
                                        "AddNew: storeFile - " +
                                        e.getMessage());
                            }
                        } else {
                            fi.write(fu.getSavePath(), fileNameAry[i]);
                        }

                        long attachId = SequenceMgr.nextID(SequenceMgr.
                                SQ_MESSAGE_ATTACH);

                        String attDesc = fu.getFieldValue(fi.getFieldName() + "Desc");

                        sql =
                                "insert into sq_message_attach (id, msgId,name,diskname,visualpath,orders,UPLOAD_DATE,FILE_SIZE,USER_NAME,is_remote,ext,att_desc) values (" +
                                attachId + "," +
                                id + "," +
                                StrUtil.sqlstr(fi.getName()) + "," +
                                StrUtil.sqlstr(fileNameAry[i]) + "," +
                                StrUtil.sqlstr(virtualpath) +
                                "," + orders +
                                "," +
                                StrUtil.sqlstr("" + System.currentTimeMillis()) +
                                "," + fi.getSize() + "," +
                                StrUtil.sqlstr(username) + "," + (isFtpUsed?1:0) + "," + StrUtil.sqlstr(fi.getExt()) +
                                "," + StrUtil.sqlstr(attDesc) +
                                ")";
                        conn.executeUpdate(sql);
                        orders++;
                        i++;
                    }

                    if (isFtpUsed) {
                        ftp.close();
                    }

                    if (i > 0) {
                        // 加分
                        ScoreMgr sm = new ScoreMgr();
                        Vector vatt = sm.getAllScore();
                        Iterator iratt = vatt.iterator();
                        while (iratt.hasNext()) {
                            ScoreUnit su = (ScoreUnit) iratt.next();
                            IPluginScore ips = su.getScore();
                            if (ips != null)
                                ips.onAddAttachment(name, i);
                        }

                        UserPrivDb upd = new UserPrivDb();
                        upd = upd.getUserPrivDb(name);
                        upd.addAttachTodayUploadCount(i);
                    }
                }
            }

            conn.commit();

            // 将msgId与上传的临时图片文件相关联
            String[] tmpAttachIds = fu.getFieldValues("tmpAttachId");
            if (tmpAttachIds != null) {
                int len = tmpAttachIds.length;
                for (int k = 0; k < len; k++) {
                    Attachment att = new Attachment(Long.parseLong(tmpAttachIds[
                            k]));
                    att.setMsgId(id);
                    att.save();
                }
                // 加分
                ScoreMgr sm = new ScoreMgr();
                Vector vatt = sm.getAllScore();
                Iterator iratt = vatt.iterator();
                while (iratt.hasNext()) {
                    ScoreUnit su = (ScoreUnit) iratt.next();
                    IPluginScore ips = su.getScore();
                    if (ips != null)
                        ips.onAddAttachment(name, len);
                }
            }

            // 匿名贴子不生成标签
            if (!name.equals("")) {
                if (cfg.getBooleanProperty("forum.isTag")) {
                    // 更新tag
                    TagMsgDb tmd = new TagMsgDb();
                    tmd.editForMsg(id, tempTagNameVector, name);
                }
            }
        } catch (Exception e) {
            conn.rollback();
            Logger.getLogger(MsgDb.class.getName()).error("editTopic:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            // 更新缓存
            MsgCache mc = new MsgCache();
            if (oldThreadType != threadType) {
                mc.refreshThreadList(boardcode);
            }
            if (!blogUserDir.equals(oldBlogUserDir) || (oldIsBlog != blog))
                mc.refreshUpdate(editid, blogId, blog, blogUserDir,
                                 oldBlogUserDir);
            else
                mc.refreshUpdate(editid);
        }
        return re;
    }

    /**
     * 贴子及其跟贴的列表
     * @param query String
     * @param boardcode String
     * @param rootid long
     * @param startIndex long
     * @param endIndex long
     * @return MsgBlockIterator
     */
    public MsgBlockIterator getMsgs(String query, String boardcode, long rootid,
                                    long startIndex,
                                    long endIndex) {
        if (!SecurityUtil.isValidSql(query))
            return null;
        // 可能取得的infoBlock中的元素的顺序号小于endIndex
        long[] docBlock = getMsgBlock(query, boardcode, rootid, startIndex);

        return new MsgBlockIterator(docBlock, query, boardcode + rootid,
                                    startIndex, endIndex);
    }

    /**
     * 贴子的列表
     * @param query String
     * @param boardcode String
     * @param startIndex long
     * @param endIndex long
     * @return ThreadBlockIterator
     */
    public ThreadBlockIterator getThreads(String query, String boardcode,
                                          long startIndex,
                                          long endIndex) {
        if (!SecurityUtil.isValidSql(query))
            return null;
        // 可能取得的infoBlock中的元素的顺序号小于endIndex
        long[] docBlock = getThreadsBlock(query, boardcode, startIndex);

        return new ThreadBlockIterator(docBlock, query, boardcode,
                                       startIndex, endIndex);
    }

    protected long[] getMsgBlock(String query, String boardcode, long rootid,
                                 long startIndex) {
        MsgCache mc = new MsgCache();
        return mc.getMsgBlock(query, boardcode + rootid, startIndex);
    }

    protected long[] getThreadsBlock(String query, String boardcode,
                                     long startIndex) {
        MsgCache mc = new MsgCache();
        return mc.getThreadsBlock(query, boardcode, startIndex);
    }

    /**
     * 贴子数量
     * @param sql String
     * @return int -1 表示sql语句不合法
     */
    public long getMsgCount(String sql, String boardcode, long rootid) {
        //根据sql语句得出计算总数的sql查询语句
        MsgCache mc = new MsgCache();
        return mc.getMsgCount(sql, boardcode, rootid);
    }

    public int getThreadsCount(String sql, String boardcode) {
        // 根据sql语句得出计算总数的sql查询语句
        MsgCache mc = new MsgCache();
        return mc.getThreadsCount(sql, boardcode);
    }

    /**
     * 迁移整个版块
     * @param fromCode String
     * @param toCode String
     * @return int
     * @throws ResKeyException
     */
    public synchronized int moveBoardMessages(HttpServletRequest request, String fromCode, String toCode) throws
            ErrMsgException,ResKeyException {
        if (ForumDb.getInstance().getStatus() != ForumDb.STATUS_STOP)
            throw new ResKeyException("res.forum.MsgDb", "err_need_stop_forum"); // "请先停止论坛的运行！");

        Directory dir = new Directory();
        Leaf fromlf = dir.getLeaf(fromCode);
        Leaf tolf = dir.getLeaf(toCode);
        if (fromlf == null)
            throw new ResKeyException("res.forum.MsgDb", "err_board_lost");
        if (tolf == null)
            throw new ResKeyException("res.forum.MsgDb", "err_board_lost");

        if (fromCode.equals(toCode))
            throw new ResKeyException("res.forum.MsgDb", "err_board_same"); // "所选版块是同一版块！");

        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(toCode);
        if (vplugin.size() > 0) {
            // 检查目的版块所挂的插件是否为board型，如果是，当与源版块有同样插件时，则允许转移，否则不允许转移
            Iterator ir = vplugin.iterator();
            Vector vpluginsrc = pm.getAllPluginUnitOfBoard(fromCode);
            int len2 = vpluginsrc.size();
            while (ir.hasNext()) {
                PluginUnit pu = (PluginUnit)ir.next();
                if (pu.getType().equals(PluginUnit.TYPE_BOARD)) {
                    boolean isValid = false;
                    for (int i=0; i<len2; i++) {
                        PluginUnit pu2 = (PluginUnit)vpluginsrc.elementAt(i);
                        if (pu2.getCode().equals(pu.getCode())) {
                            isValid = true;
                            break;
                        }
                    }
                    if (!isValid)
                        throw new ErrMsgException(StrUtil.format(LoadString(request, "err_move_plugin"), new Object[] {pu.getName(request)}));
                }
            }
        }

        int count = 0;

        Conn conn = new Conn(connname);
        try {
            String sql =
                    "update sq_thread set boardcode=? where boardcode=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, toCode);
            ps.setString(2, fromCode);
            count = conn.executePreUpdate();
            if (ps != null) {
                ps.close();
                ps = null;
            }
            sql = "update sq_message set boardcode=? where boardcode=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, toCode);
            ps.setString(2, fromCode);
            count = conn.executePreUpdate();
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("moveBoardMessages:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        tolf.setPostCount(tolf.getPostCount() + fromlf.getPostCount());
        tolf.setTopicCount(tolf.getTopicCount() + fromlf.getTopicCount());
        tolf.update();

        fromlf.setPostCount(0);
        fromlf.setTopicCount(0);
        fromlf.update();

        try {
            // 清除所有缓存
            RMCache.getInstance().clear();
        } catch (Exception e) {
            Logger.getLogger(MsgDb.class.getName()).error("moveBoardMessages2:" +
                    e.getMessage());
        }
        return count;
    }

    public void setRename(String rename) {
        this.rename = rename;
    }

    public void setRedate(java.util.Date redate) {
        this.redate = redate;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setIsElite(int isElite) {
        this.isElite = isElite;
    }

    public void setIsLocked(int isLocked) {
        this.isLocked = isLocked;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public void setColorExpire(java.util.Date colorExpire) {
        this.colorExpire = colorExpire;
    }

    public void setBoldExpire(java.util.Date boldExpire) {
        this.boldExpire = boldExpire;
    }

    public void setBlogUserDir(String blogUserDir) {
        this.blogUserDir = blogUserDir;
    }

    public void setBlog(boolean blog) {
        this.blog = blog;
    }

    public void setAttachments(Vector attachs) {
        this.attachments = attachs;
    }

    public void setPlugin2Code(String plugin2Code) {
        this.plugin2Code = plugin2Code;
    }

    public void setThreadType(int threadType) {
        this.threadType = threadType;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public void setRootid(long rootid) {
    	this.rootid = rootid;
    }

    /**
     * 增加点击率，当增长数达到hitUpdateCount时，才更新数据库，受缓存失效的影响，此数字不宜设置得太大，一般置为1，服务器负荷较大时，可以设置高一些
     * @param id long
     * @return boolean
     */
    public boolean increaseHit() {
        hit++;

        Config cfg = Config.getInstance();
        int hitUpdateCount = cfg.getIntProperty("forum.hitUpdateCount");

        boolean canCache = RMCache.getInstance().getCanCache(); // 是否启用了缓存
        boolean isUpdate = false;
        if (canCache && hit - lastHit >= hitUpdateCount)
            isUpdate = true;
        else if (!canCache)
            isUpdate = true;
        // System.out.println(getClass() + " hitUpdateCount=" + hitUpdateCount + " hit=" + hit + " lastHit=" + lastHit + " isUpdate=" + isUpdate);
        if (isUpdate) {
            Conn conn = null;
            try {
                conn = new Conn(connname);
                conn.beginTrans();

                String sql = "update sq_message set hit=? where id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, hit);
                ps.setLong(2, id);
                conn.executePreUpdate();
                ps.close();
                ps = null;
                sql = "update sq_thread set hit=? where id=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, hit);
                ps.setLong(2, id);
                conn.executePreUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                Logger.getLogger(MsgDb.class.getName()).error("increaseHit:" +
                        e.getMessage());
                return false;
            } finally {
                // 更新缓存
                MsgCache mc = new MsgCache();
                mc.refreshUpdate(id);
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            lastHit = hit;
        }
        return true;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getColor() {
        return color;
    }

    public long getReplyid() {
        return replyid;
    }

    public boolean isBold() {
        return bold;
    }

    public java.util.Date getColorExpire() {
        return colorExpire;
    }

    public java.util.Date getBoldExpire() {
        return boldExpire;
    }

    public String getBlogUserDir() {
        return blogUserDir;
    }

    public boolean isBlog() {
        return blog;
    }

    public Vector getAttachments() {
        return attachments;
    }

    public String getPlugin2Code() {
        return plugin2Code;
    }

    public int getThreadType() {
        return threadType;
    }

    public String getRootMsgPluginCode() {
        if (isRootMsg())
            return pluginCode;
        else {
            return getMsgDb(rootid).getPluginCode();
        }
    }

    public String getPluginCode() {
        return pluginCode;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public long getBlogId() {
        return blogId;
    }

    public long getLastOperate() {
        return lastOperate;
    }

    public String getBlogDirCode() {
        return blogDirCode;
    }

    public Vector getTags() {
        return tags;
    }

    public int getMsgNotify() {
        return msgNotify;
    }

    public int getSmsNotify() {
        return smsNotify;
    }

    public double getScore() {
        return score;
    }

    public java.util.Date getLevelExpire() {
        return levelExpire;
    }

    /**
     * 为用户虚拟boardcode，以便于从缓存IteratorBlock中读取
     * @param userName String
     * @return String
     */
    public static String getVirtualBoardcodeOfBlog(long blogId,
            String blogUserDir) {
        return "blog_" + blogId + "_" + blogUserDir;
    }

    /**
     * 将主题贴fromMsgId的回贴合并至主题贴toMsgId
     * @param toMsgId
     * @param fromMsgId
     * @return
     */
    public int mergeReplyMsgs(MsgDb fromMd) {
    	String sql = "select id from sq_message where rootid=" + fromMd.getId() + " and id<>" + fromMd.getId();

    	Vector v = list(sql);

    	// 更新toMd的回复数
    	setContent(getContent() + "<BR>" + fromMd.getContent());
    	setRecount(getRecount() + v.size());
    	try {
    		save();
    	}
    	catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return -1;
    	}

    	Iterator ir = v.iterator();
    	while (ir.hasNext()) {
    		MsgDb md = (MsgDb)ir.next();
    		md.setRootid(getId());
    		try {
    			md.save();
    		}
    		catch (ResKeyException e) {
    			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
    		}
    	}


    	// 刷新列表缓存
    	MsgCache mc = new MsgCache();
        mc.refreshReply(getboardcode(), getId());
        mc.refreshReply(fromMd.getboardcode(), fromMd.getId());

    	return v.size();
    }

    /**
     * 取出贴子
     * @param sql
     * @return
     */
    public Vector list(String sql) {
    	Vector v = new Vector();
    	try {
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri = jt.executeQuery(sql);
    	while (ri.hasNext()) {
    		ResultRecord rr = (ResultRecord)ri.next();
    		v.addElement(getMsgDb(rr.getLong(1)));
    	}
    	}
    	catch (SQLException e) {
    		LogUtil.getLog(getClass()).error(StrUtil.trace(e));
    	}
    	return v;
    }

    /**
     * 用于显示博客中的评论
     * @param listsql String
     * @param curPage int
     * @param pageSize int
     * @return ListResult
     * @throws ErrMsgException
     */
    public ListResult list(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        ListResult lr = new ListResult();
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        lr.setResult(result);
        lr.setTotal(0);
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages)
                curPage = totalpages;
            if (curPage <= 0)
                curPage = 1;

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    MsgDb ug = getMsgDb(rs.getLong(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("list:" + e.getMessage());
            throw new ErrMsgException("list: db error！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     * 更改附件名称
     * @param attachId int
     * @param newname String
     * @return boolean
     */
    public synchronized boolean updateAttachmentName(int attachId,
            String newname, String newDesc) {
        String sql = "update sq_message_attach set name=?,att_desc=? where id=?";
        boolean re = false;
        PreparedStatement pstmt = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newname);
            pstmt.setString(2, newDesc);
            pstmt.setInt(3, attachId);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                // 更新缓存
                MsgCache dcm = new MsgCache();
                dcm.refreshUpdate(id);
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error(
                    "updateAttachmentName:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public synchronized boolean delAttachment(long attachId) {
        Attachment am = new Attachment(attachId);
        boolean re = am.del();
        // 更新缓存
        if (re) {
            // 更新缓存
            MsgCache dcm = new MsgCache();
            dcm.refreshUpdate(id);

            // 减分
            ScoreMgr sm = new ScoreMgr();
            Vector vatt = sm.getAllScore();
            Iterator iratt = vatt.iterator();
            while (iratt.hasNext()) {
                ScoreUnit su = (ScoreUnit) iratt.next();
                IPluginScore ips = su.getScore();
                if (ips != null)
                    ips.onDelAttachment(name, 1);
            }
        }
        return re;
    }

    public Attachment getAttachment(long attachId) {
        if (attachments == null)
            return null;
        Iterator ir = attachments.iterator();
        while (ir.hasNext()) {
            Attachment att = (Attachment) ir.next();
            if (att.getId() == attachId)
                return att;
        }
        return null;
    }

    /**
     * 取得附件中最大的orders
     * @return int
     */
    public int getAttachmentMaxOrders() {
        String GETMAXORDERS =
                "select max(orders) from sq_message_attach where msgId=?";
        ResultSet rs = null;
        int maxorders = -1;
        PreparedStatement pstmt = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error(
                    "getAttachmentMaxOrders:" + e.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    /**
     * 上移下移附件
     * @param attachId int
     * @param direction String
     * @return boolean
     */
    public boolean moveAttachment(int attachId, String direction) {
        Attachment attach = new Attachment(attachId);
        boolean re = false;
        int orders = attach.getOrders();
        if (direction.equals("up")) {
            if (orders == 1)
                return true;
            else {
                Attachment upperAttach = new Attachment(orders - 1, id);
                if (upperAttach.isLoaded()) {
                    upperAttach.setOrders(orders);
                    upperAttach.save();
                }
                attach.setOrders(orders - 1);
                re = attach.save();
            }
        } else {
            int maxorders = getAttachmentMaxOrders();
            if (orders == maxorders) {
                return true;
            } else {
                Attachment lowerAttach = new Attachment(orders + 1, id);
                if (lowerAttach.isLoaded()) {
                    lowerAttach.setOrders(orders);
                    lowerAttach.save();
                }
                attach.setOrders(orders + 1);
                re = attach.save();
            }
        }
        // 更新缓存
        MsgCache dcm = new MsgCache();
        dcm.refreshUpdate(id);
        return re;
    }

    /**
     * 删除用户博客中的所有贴子
     * @param userName String
     * @return int
     */
    public int delMesssagesOfBlog(long blogId) {
        String sql =
                "select id from sq_thread where blog_id=? and isBlog=1 ORDER BY lydate desc";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, blogId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int delId = rs.getInt(1);
                    try {
                        delTopic(delId, false);
                    } catch (ResKeyException e) {
                        Logger.getLogger(MsgDb.class.getName()).error(
                                "delMesssagesOfBlog1:" + e.getMessage());
                    }
                    count++;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("delMesssagesOfBlog:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return count;
    }

    /**
     * 删除用户的所有贴子
     * @param userName String
     * @return int
     */
    public int delMessagesOfUser(String userName) {
        String sql = "select id from sq_message where name=?";
        boolean re = false;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    int delId = rs.getInt(1);
                    try {
                        delTopic(delId, false);
                    } catch (ResKeyException e) {
                        Logger.getLogger(MsgDb.class.getName()).error(
                                "delMessagesOfUser1:" + e.getMessage());
                    }
                    count++;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(MsgDb.class.getName()).error("delMessagesOfUser:" +
                    e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return count;
    }

    /**
     * 用于Lunce中填充索引
     * @param beginDate long
     * @param endDate long
     * @return Vector
     */
    public Vector list(long beginDate, long endDate) {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        String sql = "";
        if(beginDate != 0 && endDate != 0){
            sql = "select id from sq_message where lydate>=" + beginDate +
                  " and lydate<=" + endDate + " and check_status=" +
                  CHECK_STATUS_PASS;
        } else if (beginDate == 0 && endDate != 0) {
            sql = "select id from sq_message where lydate<=" + endDate +
                  " and check_status=" + CHECK_STATUS_PASS;
        } else if (beginDate!=0 && endDate==0) {
            sql = "select id from sq_message where lydate>=" + beginDate +
                  " and check_status=" + CHECK_STATUS_PASS;
        }
        else {
            sql = "select id from sq_message where check_status=" +
                  CHECK_STATUS_PASS;
        }
        try {
            rs = conn.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    v.addElement(getMsgDb(rs.getLong(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list(long beginDate, long endDate): " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public void setBoardcode(String boardcode) {
        this.boardcode = boardcode;
    }

    public void setLastOperate(long lastOperate) {
        this.lastOperate = lastOperate;
    }

    public void setBlogDirCode(String blogDirCode) {
        this.blogDirCode = blogDirCode;
    }

    public void setTags(Vector tags) {
        this.tags = tags;
    }

    public void setMsgNotify(int msgNotify) {
        this.msgNotify = msgNotify;
    }

    public void setSmsNotify(int smsNotify) {
        this.smsNotify = smsNotify;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setLevelExpire(java.util.Date levelExpire) {
        this.levelExpire = levelExpire;
    }

    /**
     * 查找自afterDate后，是否存在标题为title的主题贴，用于采集时进行判断
     * @param title String
     * @param afterDate Date
     * @return boolean
     */
    public boolean isMsgWithTitleExist(String title, java.util.Date afterDate) {
        boolean re = false;
        String sql = "select id from sq_message where title=? and lydate>=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {title, "" + DateUtil.toLongString(afterDate)});
            re = ri.size()>0;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("" + e.getMessage());
        }
        return re;
    }

    /**
     * 取得新贴
     * @param count
     * @return
     */
    public Vector getNewMsgs(int count) {
        Vector v = new Vector();
        try {
            JdbcTemplate jt = new JdbcTemplate(new DataSource());
            String sql = "select id from sq_thread where boardcode<>" + StrUtil.sqlstr(Leaf.CODE_BLOG) + " and check_status=" + MsgDb.CHECK_STATUS_PASS + " order by lydate desc";
            ResultIterator ri = jt.executeQuery(sql, 1, count);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                v.addElement(getMsgDb(rr.getInt(1)));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getNewMsgs:" + e.getMessage());
        }
        return v;
    }

    private boolean loaded = false;
    private String color;
    private boolean bold;
    private java.util.Date colorExpire;
    private java.util.Date boldExpire;
    private String blogUserDir = UserDirDb.DEFAULT;
    private boolean blog = false;
    private Vector attachments = new Vector();

    /**
     * 子类别
     */
    private int threadType = ThreadTypeDb.THREAD_TYPE_NONE;

    private String pluginCode;
    private int checkStatus = 1;
    private int lastHit = 0; // 用于更新hit

    private long blogId = UserConfigDb.NO_BLOG;
    private long lastOperate = LAST_OPERATE_NONE;
    private String blogDirCode;
    private Vector tags;
    private int msgNotify = 0;
    private int smsNotify = 0;
    private double score = 0;
    private java.util.Date levelExpire;
}
