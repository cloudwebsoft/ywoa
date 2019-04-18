package com.redmoon.blog.photo;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.cloudwebsoft.framework.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import com.redmoon.forum.Config;
import com.redmoon.forum.util.*;
import com.redmoon.kit.util.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.file.FileUtil;

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

    public static final String photoBasePath = "blog/photo";

    public static final String CACHE_CATALOG_PREFIX = "catalog_";

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

    public String getListPhotoSql() {
        return "select id from blog_photo ORDER BY score desc";
    }

    public void initDB() {
        this.tableName = "blog_photo";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new PhotoCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (title,image,blog_id,sort,addDate,id,is_remote,dir_code,is_locked,catalog,image_small) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT title,image,blog_id,sort,addDate,is_remote,dir_code,score,is_locked,catalog,hit,image_small FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET title=?,image=?,blog_id=?,sort=?,is_remote=?,dir_code=?,score=?,is_locked=?,catalog=?,image_small=? WHERE id=?";
        isInitFromConfigDB = false;
    }

    /**
     * 增加点击
     * @return boolean
     */
    public boolean hit() {
        boolean re = false;
        try {
            String sql = "update blog_photo set hit=? where id=?";
            JdbcTemplate jt = new JdbcTemplate();
            re = jt.executeUpdate(sql, new Object[] {new Integer(hit + 1),
                                  new Long(id)}) == 1;
            if (re) {
                PhotoCache mc = new PhotoCache(this);
                primaryKey.setValue(new Long(id));
                mc.refreshSave(primaryKey);
                return true;
            } else
                return false;
        } catch (SQLException e) {
            logger.error("hit:" + e.getMessage());
        }
        return re;
    }

    public boolean save(JdbcTemplate jt) throws ErrMsgException {
        boolean re = false;
        try {
            Config cfg = Config.getInstance();
            remote = cfg.getBooleanProperty("forum.ftpUsed");
            int rem = remote?1:0;
            re = jt.executeUpdate(QUERY_SAVE, new Object[] {title, image, new Long(blogId), new Integer(sort), new Integer(rem),
            dirCode, new Integer(score), new Integer(locked?1:0), new Long(catalog), imageSmall, new Long(id)
            })==1;
            if (re) {
                PhotoCache mc = new PhotoCache(this);
                primaryKey.setValue(new Long(id));
                mc.refreshSave(primaryKey);
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
        // delImage(Global.getRealPath());
        writeImage(fu);

        // 生成缩略图
        if (!image.equals("")) {
            com.redmoon.blog.Config bcfg = com.redmoon.blog.Config.getInstance();
            int w = bcfg.getIntProperty("miniatureWidth");
            int h = bcfg.getIntProperty("miniatureHeight");

            String srcF = Global.getRealPath() + "upfile/" + photoBasePath + "/" + image;
            // System.out.println(getClass() + srcF);
            String desF = Global.getRealPath() + "upfile/" + photoBasePath + "/" + imageSmall;
            cn.js.fan.util.file.image.ImageUtil.resizeImagePoportionAndClip(srcF,
                    desF, w, h);
        }

        return save(new JdbcTemplate());
    }

    public void load(JdbcTemplate jt) {
        try {
            ResultIterator ri = jt.executeQuery(QUERY_LOAD, new Object[] {new Long(id)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                title = rr.getString(1);
                image = StrUtil.getNullStr(rr.getString(2));
                blogId = rr.getLong(3);
                sort = rr.getInt(4);
                addDate = DateUtil.parse(rr.getString(5));
                remote = rr.getInt(6)==1;
                dirCode = rr.getString(7);
                score = rr.getInt(8);
                locked = rr.getInt(9)==1;
                catalog = rr.getLong(10);
                hit = rr.getInt(11);
                imageSmall = StrUtil.getNullStr(rr.getString(12));
                primaryKey.setValue(new Long(id));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
    }

    public boolean del(JdbcTemplate jt) throws ErrMsgException {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_DEL, new Object[] {new Long(id)})==1;
            if (re) {
                PhotoCache pc = new PhotoCache(this);
                pc.refreshDel(primaryKey);
                pc.refreshList(CACHE_CATALOG_PREFIX + getCatalog());
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("删除出错！");
        }
    }

    public boolean del(String realPath) throws
            ErrMsgException {
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
                    logger.error(e.getMessage());
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
                // 删除原来的图片
                if (!image.equals("")) {
                    delImage(Global.getRealPath());
                    try {
                        File file = new File(Global.getRealPath() + "upfile/" +
                                             photoBasePath + "/" + imageSmall);
                        // System.out.println(getClass() + Global.getRealPath() + "upfile/" +
                        //                     photoBasePath + "/" + imageSmall);
                        file.delete();
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
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

                String s = FileUtil.getFileNameWithoutExt(fi.getDiskName());

                image = year + "/" + month + "/" + fi.getDiskName();

                // @task":注意imageSmall未做上传至FTP
                imageSmall = year + "/" + month + "/" + s + "_s." + FileUtil.getFileExt(fi.getDiskName());
                return image;
            }
        }
        return image;
    }

    public boolean create(JdbcTemplate jt) throws ErrMsgException {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_CREATE, new Object[] {title, image, new Long(blogId), new Integer(sort+1), "" + System.currentTimeMillis(), new Long(id),
            new Integer(remote?1:0), dirCode, new Integer(locked?1:0), new Long(catalog), imageSmall
            })==1;
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        return re;
    }

    public boolean create(ForumFileUpload fu) throws ErrMsgException {
        writeImage(fu);

        // 生成缩略图
        if (!image.equals("")) {
            com.redmoon.blog.Config bcfg = com.redmoon.blog.Config.getInstance();
            int w = bcfg.getIntProperty("miniatureWidth");
            int h = bcfg.getIntProperty("miniatureHeight");

            String srcF = Global.getRealPath() + "upfile/" + photoBasePath + "/" + image;
            String desF = Global.getRealPath() + "upfile/" + photoBasePath + "/" + imageSmall;
            cn.js.fan.util.file.image.ImageUtil.resizeImagePoportionAndClip(srcF, desF, w, h);
        }

        id = (int) SequenceMgr.nextID(SequenceMgr.PHOTO);
        Config cfg = Config.getInstance();
        remote = cfg.getBooleanProperty("forum.ftpUsed");

        boolean re = create(new JdbcTemplate());
        if (re) {
            PhotoCache pc = new PhotoCache(this);
            pc.refreshList(CACHE_CATALOG_PREFIX + getCatalog());
        }
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

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setCatalog(long catalog) {
        this.catalog = catalog;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public void setImageSmall(String imageSmall) {
        this.imageSmall = imageSmall;
    }

    public String getImage() {
        return image;
    }

    public java.util.Date getAddDate() {
        return addDate;
    }

    public long getBlogId() {
        return blogId;
    }

    public boolean isRemote() {
        return remote;
    }

    public String getDirCode() {
        return dirCode;
    }

    public int getScore() {
        return score;
    }

    public boolean isLocked() {
        return locked;
    }

    public long getCatalog() {
        return catalog;
    }

    public int getHit() {
        return hit;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public int getMaxOrders(long blogId) {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            String GETMAXORDERS = "select max(sort) from " + tableName + " where blog_id=?";
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setLong(1, blogId);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("getMaxOrders:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    private String title;
    private String image = "";

    private int sort;
    private java.util.Date addDate;
    private long blogId;
    private boolean remote = false;
    private String dirCode = DirDb.ROOTCODE;
    private int score = 0;
    private boolean locked = false;
    private long catalog = 0;
    private int hit = 0;
    private String imageSmall = "";
}
