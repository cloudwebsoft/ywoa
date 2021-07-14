package com.redmoon.forum.plugin.group.photo;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.base.IObjectDb;
import com.cloudwebsoft.framework.base.ObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.Config;
import com.redmoon.forum.SequenceMgr;
import com.redmoon.forum.util.ForumFileUpload;
import com.redmoon.forum.util.ForumFileUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.forum.plugin.group.GroupThreadDb;
import com.redmoon.forum.plugin.group.GroupUserDb;

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
public class PhotoDb extends ObjectDb {

    public static final String photoBasePath = "forum/plugin/group/photo";

    private long id;

    public PhotoDb() {
        super();
    }

    public PhotoDb(long id) {
        this.id = id;
        init();
        load(new JdbcTemplate());
    }

    public PhotoDb getPhotoDb(long id) {
        return (PhotoDb)getObjectDb(new Long(id));
    }

    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new PhotoDb(pk.getLongValue());
    }

    public String getPhotoUrl(HttpServletRequest request) {
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String basePath = request.getContextPath() + "/upfile/" +
                                    photoBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            basePath = cfg.getRemoteBaseUrl();
            basePath += photoBasePath + "/";
        }
        return basePath + image;
    }

    public void initDB() {
        this.tableName = "plugin_group_photo";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new PhotoCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (title,image,group_id,sort,addDate,id,is_remote,user_name) VALUES (?,?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT title,image,group_id,sort,addDate,is_remote,user_name FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET title=?,image=?,group_id=?,sort=?,is_remote=? WHERE id=?";
        isInitFromConfigDB = false;
    }

    public boolean save(JdbcTemplate jt) throws ErrMsgException {
        try {
            Config cfg = Config.getInstance();
            remote = cfg.getBooleanProperty("forum.ftpUsed");
            int ret = jt.executeUpdate(this.QUERY_SAVE, new Object[] {title, image,
                             new Long(groupId), new Integer(sort),
                             new Integer(remote ? 1 : 0), new Long(id)});

            if (ret == 1) {
                PhotoCache mc = new PhotoCache(this);
                primaryKey.setValue(new Long(id));
                mc.refreshSave(primaryKey);
                mc.refreshList("" + groupId);
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ErrMsgException("更新时出错！");
        }
    }

    public boolean save(ForumFileUpload fu) throws ErrMsgException {
        // 删除图片
        delImage(Global.getRealPath());
        image = writeImage(fu);
        return save(new JdbcTemplate());
    }

    public void load(JdbcTemplate jt) {
        try {
            ResultIterator ri = jt.executeQuery(this.QUERY_LOAD, new Object[] {new Long(id)});
            //url,title,image,userName,sort,kind
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                this.title = rr.getString(1);
                this.image = StrUtil.getNullString(rr.getString(2));
                this.groupId = rr.getLong(3);
                this.sort = rr.getInt(4);
                addDate = DateUtil.parse(rr.getString(5));
                remote = rr.getInt(6)==1;
                userName = rr.getString(7);
                primaryKey.setValue(new Long(id));
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
    }

    /**
     * 删除用户在圈子发的照片
     * @param groupId long
     * @param userName String
     * @throws ResKeyException
     */
    public void delPhotoOfUserInGroup(long groupId, String userName) throws ResKeyException {
        String sql = "select id from plugin_group_photo where group_id=? and user_name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(groupId), userName});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                PhotoDb pd = getPhotoDb(id);
                pd.del(jt);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delPhotoOfUserInGroup:" + e.getMessage());
        }
    }

    public void delPhotoOfGroup(long groupId) throws ResKeyException {
        String sql = "select id from plugin_group_photo where group_id=" + groupId;
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                long id = rr.getLong("id");
                PhotoDb pd = getPhotoDb(id);
                pd.del(new JdbcTemplate());
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delPhotoOfGroup:" + e.getMessage());
        }
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        try {
            int ret = jt.executeUpdate(this.QUERY_DEL, new Object[] {new Long(id)});
            if (ret==1) {
                PhotoCache mc = new PhotoCache(this);
                mc.refreshDel(primaryKey, "" + groupId);

                GroupUserDb gu = new GroupUserDb();
                gu = gu.getGroupUserDb(groupId, userName);
                if (gu!=null) {
                    gu.set("photo_count",
                           new Integer(gu.getInt("photo_count") - 1));
                    gu.set("total_count",
                           new Integer(gu.getInt("total_count") - 1));
                    gu.save();
                }
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            throw new ResKeyException("err_db");
        }
    }

    public boolean del(String realPath) throws
            ErrMsgException,ResKeyException {
        boolean re = del(new JdbcTemplate());
        if (re)
            delImage(realPath);
        return re;
    }

    public void delImage(String realPath) {
        if (image != null && !image.equals("")) {
            Config cfg = Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                if (remote) {
                    ForumFileUtil ffu = new ForumFileUtil();
                    ffu.delFtpFile(photoBasePath + "/" + image);
                }
            } else {
                try {
                    File file = new File(realPath + "upfile/" + photoBasePath + "/" + image);
                    file.delete();
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }
        }
    }

    public String writeImage(ForumFileUpload fu) {
        if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0)
                fi = (FileInfo) v.get(0);
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                vpath = photoBasePath + "/" + year + "/" + month + "/";
                String filepath = Global.getRealPath() + "upfile/" + vpath;
                // 置本地路径
                fu.setSavePath(filepath);
                // 置远程路径
                fu.setRemoteBasePath(vpath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                return year + "/" + month + "/" + fi.getDiskName();
            }
        }
        return "";
    }

    public boolean create(JdbcTemplate jt) throws ErrMsgException,ResKeyException {
        boolean re = false;
        try {
            Config cfg = Config.getInstance();
            remote = cfg.getBooleanProperty("forum.ftpUsed");
            id = (int) SequenceMgr.nextID(SequenceMgr.PHOTO);
            re = jt.executeUpdate(this.QUERY_CREATE, new Object[] {title,
                                   image, new Long(groupId), new Integer(sort),
                                   "" + System.currentTimeMillis(), new Long(id),
                                   new Integer(remote ? 1 : 0),userName})==1;
            if (re) {
                PhotoCache mc = new PhotoCache(this);
                mc.refreshCreate("" + groupId);

                GroupUserDb gu = new GroupUserDb();
                gu = gu.getGroupUserDb(groupId, userName);
                gu.set("photo_count",
                       new Integer(gu.getInt("photo_count") + 1));
                gu.set("total_count",
                       new Integer(gu.getInt("total_count") + 1));
                gu.save();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        return re;
    }

    public boolean create(ForumFileUpload fu) throws ErrMsgException,ResKeyException {
        image = writeImage(fu);
        boolean re = create(new JdbcTemplate());
        return re;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getSort() {
        return sort;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImage() {
        return image;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public long getGroupId() {
        return groupId;
    }

    public boolean isRemote() {
        return remote;
    }

    public String getUserName() {
        return userName;
    }

    private String title;
    private String image = "";
    private int sort = 0;
    private java.util.Date addDate;
    private long groupId;
    private boolean remote = false;
    private String userName;
}
