package com.redmoon.forum.music;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.*;

public class MusicFileDb extends ObjectDb {
    long id;
    String name;
    String diskName;
    String visualPath;

    public static final String baseMusicPath = "forum/music";

    public MusicFileDb() {
        init();
    }

    public MusicFileDb(long id) {
        init();
        this.id = id;
        load(new JdbcTemplate());
    }

    public void initDB() {
        this.tableName = "sq_forum_music_file";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_LONG);
        objectCache = new MusicFileCache(this);

        QUERY_CREATE = "insert into sq_forum_music_file (id, dir_code, name, disk_name, visual_path, file_size, ext, upload_date,is_link,url) values (?, ?,?,?,?,?,?,?,?,?)";
        QUERY_LIST = "select id from sq_forum_music_file order by upload_date desc";
        QUERY_DEL = "delete from sq_forum_music_file where id=?";

        QUERY_SAVE = "update sq_forum_music_file set name=?,url=?,download_count=? WHERE id=?";
        QUERY_LOAD = "SELECT dir_code, name, disk_name, visual_path, file_size, upload_date,is_link,url,download_count FROM sq_forum_music_file WHERE id=?";
        isInitFromConfigDB = false;
    }

    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new MusicFileDb(pk.getLongValue());
    }

    public MusicFileDb getMusicFileDb(long id) {
        return (MusicFileDb)getObjectDb(new Long(id));
    }

    public boolean create(JdbcTemplate jt) throws ErrMsgException,
            ResKeyException {
        id = SequenceMgr.nextID(SequenceMgr.CMS_IMG_STORE_FILE);
        try {
            jt.executeUpdate(QUERY_CREATE, new Object[]{new Long(id), dirCode, name, diskName, visualPath, new Long(size), ext, "" + System.currentTimeMillis(), new Integer(link?1:0), url});
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new ResKeyException("err_db");
        }
        return false;
    }

    public String getMusicUrl(HttpServletRequest request) {
        if (!link)
            return request.getContextPath() + "/upfile/" + baseMusicPath + "/" + visualPath + "/" + diskName;
        else {
            return url;
        }
    }

    public boolean del(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_DEL, new Object[] {new Long(id)})==1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del:" + e.getMessage());
            return false;
        }
        if (!link) {
            // 删除文件
            File fl = new File(Global.getRealPath() + "upfile/" + baseMusicPath +
                               "/" + visualPath + "/" + diskName);
            fl.delete();
        }
        return re;
    }

    public boolean save(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(QUERY_SAVE, new Object[] {name, url, new Integer(downloadCount), new Long(id)})==1;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
            return false;
        }
        return re;
    }

    public long getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiskName() {
        return this.diskName;
    }

    public void setDiskName(String dn) {
        this.diskName = dn;
    }

    public String getVisualPath() {
        return this.visualPath;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public java.util.Date getUploadDate() {
        return uploadDate;
    }

    public String getDirCode() {
        return dirCode;
    }

    public long getSize() {
        return size;
    }

    public String getExt() {
        return ext;
    }

    public boolean isLink() {
        return link;
    }

    public String getUrl() {
        return url;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setVisualPath(String vp) {
        this.visualPath = vp;
    }

    public void setUploadDate(java.util.Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void load(JdbcTemplate jt) {
        try {
            ResultIterator ri = jt.executeQuery(QUERY_LOAD,
                                                new Object[] {new Long(id)});
            if (id==0) {
                Exception e = new Exception();
                e.printStackTrace();
            }

            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                dirCode = rr.getString(1);
                name = rr.getString(2);
                diskName = rr.getString(3);
                visualPath = rr.getString(4);
                size = rr.getLong(5);
                uploadDate = DateUtil.parse(rr.getString(6));
                link = rr.getInt(7)==1;
                url = StrUtil.getNullStr(rr.getString(8));
                downloadCount = rr.getInt(9);
                primaryKey.setValue(new Long(id));
                loaded = true;
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        }
    }

    public Vector listOfDir(String dirCode) {
        String sql = "select id from sq_forum_music_file where dir_code=" + StrUtil.sqlstr(dirCode);
        return list(sql);
    }

    private boolean loaded = false;
    private java.util.Date uploadDate;
    private String dirCode;
    private long size;
    private String ext;
    private boolean link = false;
    private String url;
    private int downloadCount = 0;
}
