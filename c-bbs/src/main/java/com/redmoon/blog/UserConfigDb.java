package com.redmoon.blog;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.module.cms.robot.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.blog.ui.*;
import com.redmoon.forum.*;
import com.redmoon.forum.util.*;
import com.redmoon.kit.util.*;

/**
 * <p>Title:个人博客的有关配置信息 </p>
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
public class UserConfigDb extends ObjectDb {
    public static int TYPE_PERSON = 0;
    public static int TYPE_GROUP = 1;

    public static int NO_BLOG = -1;

    public static final String iconBasePath = "blog/icon";
    public static final String cssBasePath = "blog/css";

    public String oldDomain;

    public UserConfigDb() {
    }

    public UserConfigDb(long id) {
        this.id = id;
        init();
        load();
    }

    public String writeIcon(ForumFileUpload fu) throws ErrMsgException {
        if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            String vpath = "";
            Iterator ir = v.iterator();
            if (ir.hasNext()) {
                fi = (FileInfo) ir.next();
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                vpath = "upfile/" + iconBasePath + "/" + year + "/";
                String filepath = Global.getRealPath() + vpath;
                File f = new File(vpath);
                if (!f.isDirectory())
                    f.mkdirs();
                fu.setRemoteBasePath(iconBasePath + "/" + year);
                fu.setSavePath(filepath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                return year + "/" + fi.getDiskName();
            }
        }
        return "";
    }

    public String getIconUrl(HttpServletRequest request) {
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String basePath = request.getContextPath() + "/upfile/" +
                          iconBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            basePath = cfg.getRemoteBaseUrl();
            basePath += iconBasePath + "/";
        }
        return basePath + icon;
    }
    
    /**
     * 为某个用户自动创建博客，用在个人站点中需要发布音乐时
     * @param userName
     * @return
     * @throws ErrMsgException
     */
    public boolean create(String userName, boolean isOpen) throws ErrMsgException {
    	this.userName = userName;
    	title = userName;
    	subtitle = userName;
    	penName = userName;
    	skin = "default";
    	type = TYPE_PERSON;
    	open = isOpen;
    	kind = Leaf.ROOTCODE;
    	return create();
    }
    
    public boolean create() throws ErrMsgException {
        Conn conn = null;
        try {
            id = SequenceMgr.nextID(SequenceMgr.BLOG_ID);
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            // this.QUERY_CREATE =
            //    "INSERT " + tableName + " (userName,title,subtitle,penName,skin,notice) VALUES (?,?,?,?,?,?)";
            ps.setString(1, userName);
            ps.setString(2, title);
            ps.setString(3, subtitle);
            ps.setString(4, penName);
            ps.setString(5, skin);
            ps.setString(6, notice);
            ps.setString(7, kind);
            ps.setString(8, "" + System.currentTimeMillis());
            ps.setLong(9, id);
            ps.setInt(10, type);
            ps.setString(11, icon);
            ps.setInt(12, footprint ? 1 : 0);
            ps.setString(13, domain);
            ps.setInt(14, open?1:0);
            if (!(conn.executePreUpdate() == 1 ? true : false))
                return false;
            UserConfigCache mc = new UserConfigCache(this);
            mc.refreshCreate();
            mc.refreshUserBlogId(userName);
        } catch (SQLException e) {
            logger.error("create: " + StrUtil.trace(e));
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean create(ForumFileUpload fu) throws ErrMsgException {
        icon = writeIcon(fu);
        
        return create();
    }

    public void delIcon() throws ResKeyException {
        // 删除icon
        if (!icon.equals("")) {
            com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                ForumFileUtil ffu = new ForumFileUtil();
                ffu.delFtpFile(iconBasePath + "/" + icon);
            } else {
                File f = new File(Global.realPath + "upfile/" + iconBasePath +
                                  "/" + icon);
                f.delete();
            }
            icon = "";
            save();
        }
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException, ResKeyException {
        boolean re = false;
        // 删除用户的博客贴子
        MsgDb md = new MsgDb();
        md.delMesssagesOfBlog(id);
        // 删除用户的博客目录
        UserDirDb udd = new UserDirDb();
        udd.delDirsOfBlog(id);
        // 删除跟该博客相关的所有脚印
        FootprintDb fd = new FootprintDb();
        fd.delOfBlog(id);        
        // 删除用户博客
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setLong(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            // 删除icon
            if (!icon.equals("")) {
                File f = new File(Global.realPath + icon);
                f.delete();
            }
            UserConfigCache uc = new UserConfigCache(this);
            primaryKey.setValue(new Long(id));
            uc.refreshDel(primaryKey);
            uc.refreshList();

            // 如果是团队博客，则删除其成员
            if (type == TYPE_GROUP) {
                BlogGroupUserDb bgu = new BlogGroupUserDb();
                bgu.delUserOfBlog(id);
            }
        }
        return re;
    }

    public ListResult ListBlog(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);
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
                    UserConfigDb ug = getUserConfigDb(rs.getLong(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
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
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserConfigDb(pk.getLongValue());
    }

    public UserConfigDb getUserConfigDb(long id) {
        return (UserConfigDb) getObjectDb(new Long(id));
    }

    public long getBlogIdByUserName(String userName) {
        UserConfigCache ucc = new UserConfigCache();
        return ucc.getBlogIdByUserName(userName);
    }

    public long getBlogIdByDomain(String domain) {
        UserConfigCache ucc = new UserConfigCache();
        return ucc.getBlogIdByDomain(domain);
    }

    /**
     * 从数据库中取出用户的个人博客的ID
     * @param userName String
     * @return long
     */
    public long getBlogIdByUserNameFromDb(String userName) {
        String sql = "select id from " + tableName +
                     " where userName=? and blog_type=" + TYPE_PERSON;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("getBlogIdByUserNameFromDb:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return NO_BLOG;
    }

    /**
     * 根据domain从数据库中取出blogId
     * @param domain String
     * @return long
     */
    public long getBlogIdByDomainFromDb(String domain) {
        String sql = "select id from " + tableName + " where domain=?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, domain);
            rs = conn.executePreQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("getBlogIdByDomain:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return NO_BLOG;
    }

    public UserConfigDb getUserConfigDbByUserName(String userName) {
        long blogId = getBlogIdByUserName(userName);
        if (blogId != NO_BLOG) {
            return getUserConfigDb(blogId);
        } else
            return null;
    }

    public UserConfigDb getUserConfigDbByDomain(String domain) {
        long blogId = getBlogIdByDomain(domain);
        if (blogId != NO_BLOG) {
            return getUserConfigDb(blogId);
        } else
            return null;
    }

    public ObjectBlockIterator getGroupBlogsOwnedByUser(String userName) {
        String sql = "select id from " + tableName + " where userName=" +
                     StrUtil.sqlstr(userName) + " and blog_type=" + TYPE_GROUP;
        int count = getObjectCount(sql);

        return getObjects(sql, 0, count);
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setLong(1, id);
            rs = conn.executePreQuery();
            if (rs.next()) {
                // this.QUERY_LOAD =
                // "SELECT title,subtitle,penName,skin,notice FROM " + tableName + " WHERE userName=?";
                title = rs.getString(1);
                subtitle = rs.getString(2);
                penName = rs.getString(3);
                skin = rs.getString(4);
                notice = rs.getString(5);
                addDate = DateUtil.parse(rs.getString(6));
                // 用下句会丢失小时分钟信息
                // addDate = rs.getDate(6);
                valid = rs.getInt(7) == 1 ? true : false;
                viewCount = rs.getInt(8);
                msgCount = rs.getInt(9);
                replyCount = rs.getInt(10);
                kind = rs.getString(11);
                type = rs.getInt(12);
                userName = rs.getString(13);
                updateDate = DateUtil.parse(rs.getString(14));
                icon = StrUtil.getNullStr(rs.getString(15));
                friends = StrUtil.getNullStr(rs.getString(16));
                footprint = rs.getInt(17) == 1;
                domain = StrUtil.getNullStr(rs.getString(18));
                bkMusic = rs.getInt(19) == 1;
                userCss = rs.getInt(20) == 1;
                cssPath = StrUtil.getNullStr(rs.getString(21));
                photoDig = rs.getInt(22)==1;
                open = rs.getInt(23)==1;

                oldDomain = domain;

                loaded = true;
                primaryKey.setValue(new Long(id));
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean save(ForumFileUpload fu) throws ErrMsgException,
            ResKeyException {
        String tmpicon = writeIcon(fu);
        if (!tmpicon.equals("")) {
            delIcon();
            icon = tmpicon;
        }
        return save();
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ResKeyException {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, title);
            ps.setString(2, subtitle);
            ps.setString(3, penName);
            ps.setString(4, skin);
            ps.setString(5, notice);
            ps.setInt(6, valid ? 1 : 0);
            ps.setInt(7, viewCount);
            ps.setInt(8, msgCount);
            ps.setInt(9, replyCount);
            ps.setString(10, kind);
            ps.setInt(11, type);
            ps.setString(12, DateUtil.toLongString(updateDate));
            ps.setString(13, icon);
            ps.setString(14, friends);
            ps.setInt(15, footprint ? 1 : 0);
            ps.setString(16, domain);
            ps.setInt(17, bkMusic ? 1 : 0);
            ps.setInt(18, userCss ? 1 : 0);
            ps.setString(19, cssPath);
            ps.setInt(20, photoDig?1:0);
            ps.setInt(21, open?1:0);
            ps.setLong(22, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            UserConfigCache uc = new UserConfigCache(this);
            primaryKey.setValue(userName);
            uc.refreshSave(primaryKey);
            if (!oldDomain.equals(domain))
                uc.refreshDomain(oldDomain);
        }
        return re;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setUpdateDate(java.util.Date updateDate) {
        this.updateDate = updateDate;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public void setFootprint(boolean footprint) {
        this.footprint = footprint;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setBkMusic(boolean bkMusic) {
        this.bkMusic = bkMusic;
    }

    public void setUserCss(boolean userCss) {
        this.userCss = userCss;
    }

    public void setCssPath(String cssPath) {
        this.cssPath = cssPath;
    }

    public void setPhotoDig(boolean photoDig) {
        this.photoDig = photoDig;
    }

    public void setVideoDig(boolean videoDig) {
        this.videoDig = videoDig;
    }

    public void setMusicDig(boolean musicDig) {
        this.musicDig = musicDig;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getPenName() {
        return penName;
    }

    public String getSkin() {
        return skin;
    }

    public String getNotice() {
        return notice;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public boolean isValid() {
        return valid;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public String getKind() {
        return kind;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public java.util.Date getUpdateDate() {
        return updateDate;
    }

    public String getIcon() {
        return icon;
    }

    public String getFriends() {
        return friends;
    }

    public void initDB() {
        this.tableName = "blog_user_config";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new UserConfigCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (userName,title,subtitle,penName,skin,notice,kind,addDate,id,blog_type,icon,is_footprint,domain,is_bk_music,is_user_css,is_open) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,0,0,?)";
        this.QUERY_LOAD =
                "SELECT title,subtitle,penName,skin,notice,addDate,isValid,viewCount,msgCount,replyCount,kind,blog_type,userName,update_date,icon,friends,is_footprint,domain,is_bk_music,is_user_css,css_path,is_photo_dig,is_open FROM " +
                tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName +
                " SET title=?,subtitle=?,penName=?,skin=?,notice=?,isValid=?,viewCount=?,msgCount=?,replyCount=?,kind=?,blog_type=?,update_date=?,icon=?,friends=?,is_footprint=?,domain=?,is_bk_music=?,is_user_css=?,css_path=?,is_photo_dig=?,is_open=? WHERE id=?";
        this.QUERY_LIST = "select id from " + tableName +
                          " order by addDate desc";
        isInitFromConfigDB = false;
    }

    public boolean isFootprint() {
        return footprint;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isBkMusic() {
        return bkMusic;
    }

    public boolean isUserCss() {
        return userCss;
    }

    public String getCssPath() {
        return cssPath;
    }

    public boolean isPhotoDig() {
        return photoDig;
    }

    public boolean isVideoDig() {
        return videoDig;
    }

    public boolean isMusicDig() {
        return musicDig;
    }

    public boolean isOpen() {
        return open;
    }

    public String getCSS(HttpServletRequest request) {
        if (!userCss) {
            TemplateDb td = new TemplateDb();
            td = td.getTemplateDb(StrUtil.toInt(skin));
            if (td==null)
                return "";
            else
                return request.getContextPath() + "/blog/skin/" + td.getString("code") + "/skin.css";
        }
        else {
            // 如果样式表不存在，则创建样式表文件
            return getUserDefinedCSSUrl(request);
        }
    }

    public String getUserDefinedCSSUrl(HttpServletRequest request) {
        chkAndCreateCSSFile(request);
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String basePath = request.getContextPath() + "/upfile/" +
                                    cssBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            basePath = cfg.getRemoteBaseUrl();
            basePath += cssBasePath + "/";
        }
        return basePath + cssPath;
    }

    /**
     * 创建初始默认模板，根据用户当前选择的皮肤
     * @param request HttpServletRequest
     * @return boolean
     */
    public String createDefaultCSSFile(HttpServletRequest request) {
        String path = cssBasePath;
        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);

        if (cssPath.equals(""))
            path += "/" + year + "/" + month + "/" + id + ".css";
        else
            path += "/" + cssPath;

        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");

        // 建立tmp文件，过滤样本CSS中的路径
        FileUtil fu = new FileUtil();
        String tmpFileRealPath = Global.realPath + "upfile/tmp_blog_css_" + id + ".css";
        TemplateDb td = new TemplateDb();
        td = td.getTemplateDb(StrUtil.toInt(skin));
        try {
            String css = "";
            css = fu.ReadFile(Global.realPath + "blog/skin/" + td.getString("code") + "/skin.css");
            css = css.replaceAll("images/", request.getContextPath() + "/blog/skin/" + td.getString("code") + "/images/");
            fu.WriteFileUTF8(tmpFileRealPath, css);
        }
        catch (IOException e) {
            LogUtil.getLog(getClass()).error("chkAndCreateCSSFile:" + e.getMessage());
        }

        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            try {
                boolean retFtp = ftp.connect(cfg.getProperty(
                        "forum.ftpServer"),
                                             cfg.getIntProperty("forum.ftpPort"),
                                             cfg.getProperty("forum.ftpUser"),
                                             cfg.getProperty("forum.ftpPwd"), true);
                if (!retFtp) {
                    LogUtil.getLog(getClass()).error("writeFile:" +
                            ftp.getReplyMessage());
                } else {
                    try {
                        ftp.storeFile(path,
                                      tmpFileRealPath);
                    } catch (IOException e1) {
                        LogUtil.getLog(getClass()).error(
                                "AddNewWE: storeFile - " +
                                e1.getMessage());
                    }
                }
            } finally {
                ftp.close();
            }
        } else {
            // 检查目录是否已存在
            String fullPath = Global.realPath + "upfile/" + path;
            int p = fullPath.lastIndexOf("/");
            String pa = fullPath.substring(0, p);
            // System.out.println(getClass() + " pa=" + pa);
            File f = new File(pa);
            if (!f.isDirectory())
                f.mkdirs();
            fu.CopyFile(tmpFileRealPath,
                        fullPath);
            // System.out.println(getClass() + " " + Global.realPath + "upfile/" + path);
        }
        // 删除临时文件
        File f = new File(tmpFileRealPath);
        f.delete();
        return year + "/" + month + "/" + id + ".css";
    }

    /**
     * 检查用户自定义CSS是否已存在，如果不存在，则创建
     */
    public void chkAndCreateCSSFile(HttpServletRequest request) {
        // 不为空表示已存在
        if (!cssPath.equals(""))
            return;
        String p = createDefaultCSSFile(request);
        if (!p.equals("")) {
            cssPath = p;
            try {
                save();
            }
            catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error("chkAndCreateCSSFile:" + e.getMessage(request));
            }
        }
    }

    public String getCSSContent(HttpServletRequest request) {
        chkAndCreateCSSFile(request);

        String tmpRemoteFilePath = "";
        Config cfg = Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            String basePath = cfg.getProperty("forum.ftpUrl");

            // 从远程下载图像至本地
            String remoteFileName = RobotUtil.gatherFile(basePath + "/" +
                    cssBasePath + "/" +
                    cssPath, "utf-8", Global.realPath +
                    "upfile" + "/");

            if (!remoteFileName.equals("")) {
                try {
                    tmpRemoteFilePath = Global.realPath +
                                        "upfile" + "/" +
                                        remoteFileName;
                    String s = FileUtil.ReadFile(tmpRemoteFilePath);
                    return s;
                } catch (Exception e) {
                    logger.error("getCSSContent1:" +
                                 StrUtil.trace(e));
                }
                finally {
                    File f = new File(tmpRemoteFilePath);
                    f.delete();
                }
            }
        }
        else {
            try {
                String p = Global.realPath + "upfile/" + cssBasePath + "/" + cssPath;
                return FileUtil.ReadFile(p);
            }
            catch (IOException e) {
                LogUtil.getLog(getClass()).error("getCSSContent2:" + e.getMessage());
            }
        }
        return "";
    }

    public void modifyCSSFile(HttpServletRequest request) {
        String content = ParamUtil.get(request, "content");
        content = cn.js.fan.security.AntiXSS.antiXSS(content);

        String basePath = cssBasePath;

        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");

        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            try {
                // 建立tmp文件
                FileUtil fu = new FileUtil();
                String tmpFileRealPath = Global.realPath + "upfile/tmp_blog_" + id +
                                         ".css";
                fu.WriteFileUTF8(tmpFileRealPath, content);

                boolean retFtp = ftp.connect(cfg.getProperty(
                        "forum.ftpServer"),
                                             cfg.getIntProperty("forum.ftpPort"),
                                             cfg.getProperty("forum.ftpUser"),
                                             cfg.getProperty("forum.ftpPwd"), true);
                if (!retFtp) {
                    LogUtil.getLog(getClass()).error("writeFile:" +
                            ftp.getReplyMessage());
                } else {
                    try {
                        ftp.storeFile(basePath + "/" + cssPath,
                                      tmpFileRealPath);
                    } catch (IOException e1) {
                        LogUtil.getLog(getClass()).error(
                                "AddNewWE: storeFile - " +
                                e1.getMessage());
                    }
                }
                // 删除临时文件
                File f = new File(tmpFileRealPath);
                f.delete();
            } finally {
                ftp.close();
            }
        } else {
            FileUtil.WriteFileUTF8(Global.realPath + "upfile/" + basePath + "/" + cssPath, content);
        }
    }

    private String userName;
    private String title;
    private String subtitle;
    private String penName;
    private String skin;
    private String notice;
    private java.util.Date addDate;
    private boolean valid = true;
    private int viewCount = 0;
    private int msgCount = 0;
    private int replyCount = 0;
    private String kind;
    private long id;
    private int type = TYPE_PERSON;
    private java.util.Date updateDate;
    private String icon;
    private String friends;
    private boolean footprint = true;
    private String domain;
    private boolean bkMusic = false;
    private boolean userCss = false;
    private String cssPath = "";
    private boolean photoDig = true;
    private boolean musicDig = true;
    private boolean videoDig = true;
    private boolean open = true;
}
