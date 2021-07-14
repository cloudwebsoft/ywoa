package cn.js.fan.module.cms.plugin.software;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.module.cms.plugin.base.*;
import cn.js.fan.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * images: path|desc#@#path|desc#@#path|desc
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SoftwareDocumentDb extends ObjectDb implements IPluginDocument {
    public static final String SEPERATOR_URL = "#@#";

    public SoftwareDocumentDb() {
    }

    public SoftwareDocumentDb(int docId) {
        this.docId = docId;
        init();
        load();
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setInt(1, docId);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new SoftwareDocumentDb(pk.getIntValue());
    }

    public SoftwareDocumentDb getSoftwareDocumentDb(int docId) {
        return (SoftwareDocumentDb)getObjectDb(new Integer(docId));
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
//        doc_id,small_img,soft_rank,accredit,local_url,urls,other_urls,file_type,lang,soft_type,os,offical_url,offical_demo,file_size,download_count
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setInt(1, docId);
            ps.setString(2, smallImg);

            ps.setString(3, softRank);
            ps.setString(4, accredit);
            ps.setString(5, urls);
            ps.setString(6, fileType);
            ps.setString(7, lang);
            ps.setString(8, softType);
            ps.setString(9, os);
            ps.setString(10, officalUrl);
            ps.setString(11, officalDemo);
            ps.setInt(12, fileSize);
            ps.setLong(13, downloadCount);
            ps.setString(14, unit);
            ps.setString(15, dirCode);
            ps.setString(16, parentCode);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }


    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setInt(1, docId);
            primaryKey.setValue(new Integer(docId));
            rs = conn.executePreQuery();
            if (rs.next()) {
                smallImg = StrUtil.getNullStr(rs.getString(1));
                softRank = StrUtil.getNullStr(rs.getString(2));
                accredit = StrUtil.getNullStr(rs.getString(3));
                urls = StrUtil.getNullStr(rs.getString(4));
                urlAry = StrUtil.split(urls, SEPERATOR_URL);
                fileType = StrUtil.getNullStr(rs.getString(5));
                lang = StrUtil.getNullStr(rs.getString(6));
                softType = StrUtil.getNullStr(rs.getString(7));
                os = StrUtil.getNullStr(rs.getString(8));
                officalUrl = StrUtil.getNullStr(rs.getString(9));
                officalDemo = StrUtil.getNullStr(rs.getString(10));
                fileSize = rs.getInt(11);
                downloadCount = rs.getLong(12);
                unit = rs.getString(13);
                dirCode = rs.getString(14);
                parentCode = rs.getString(15);
                loaded = true;
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

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() {
            int rowcount = 0;
            Conn conn = null;
            try {
                conn = new Conn(connname);
                PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
                ps.setString(1, smallImg);
                ps.setString(2, softRank);
                ps.setString(3, accredit);
                ps.setString(4, urls);
                ps.setString(5, fileType);
                ps.setString(6, lang);
                ps.setString(7, softType);
                ps.setString(8, os);
                ps.setString(9, officalUrl);
                ps.setString(10, officalDemo);
                ps.setInt(11, fileSize);
                ps.setLong(12, downloadCount);
                ps.setString(13, unit);
                ps.setString(14, dirCode);
                ps.setString(15, parentCode);
                ps.setInt(16, docId);
                rowcount = conn.executePreUpdate();

                primaryKey.setValue(new Integer(docId));
                objectCache.refreshSave(primaryKey);
            } catch (SQLException e) {
                logger.error(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        return rowcount>0? true:false;
    }

    public void initDB() {
        this.tableName = "cws_cms_software_doc";

        primaryKey = primaryKey = new PrimaryKey("doc_id", PrimaryKey.TYPE_INT);
        objectCache = new SoftwareDocumentCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE doc_id=?";
        this.QUERY_CREATE =
                "INSERT into " + tableName + " (doc_id,small_img,soft_rank,accredit,urls,file_type,lang,soft_type,os,offical_url,offical_demo,file_size,download_count,unit,dir_code,parent_code) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT small_img,soft_rank,accredit,urls,file_type,lang,soft_type,os,offical_url,offical_demo,file_size,download_count,unit,dir_code,parent_code FROM " + tableName + " WHERE doc_id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET small_img=?,soft_rank=?,accredit=?,urls=?,file_type=?,lang=?,soft_type=?,os=?,offical_url=?,offical_demo=?,file_size=?,download_count=?,unit=?,dir_code=?,parent_code=? WHERE doc_id=?";
        isInitFromConfigDB = false;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getDocId() {
        return docId;
    }

    public void setSoftRank(String softRank) {
        this.softRank = softRank;
    }

    public void setAccredit(String accredit) {
        this.accredit = accredit;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setSoftType(String softType) {
        this.softType = softType;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setOfficalUrl(String officalUrl) {
        this.officalUrl = officalUrl;
    }

    public void setOfficalDemo(String officalDemo) {
        this.officalDemo = officalDemo;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getSoftRank() {
        return softRank;
    }

    public String getAccredit() {
        return accredit;
    }

    public String getFileType() {
        return fileType;
    }

    public String getLang() {
        return lang;
    }

    public String getSoftType() {
        return softType;
    }

    public String getOs() {
        return os;
    }

    public String getOfficalUrl() {
        return officalUrl;
    }

    public String getOfficalDemo() {
        return officalDemo;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getPageCount() {
        return IPluginDocument.PAGE_COUNT_NONE;
    }

    public void setSmallImg(String smallImg) {
        this.smallImg = smallImg;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getSmallImg() {
        return smallImg;
    }

    public String[] getUrlAry() {
        return urlAry;
    }

    public String getUrls() {
        return urls;
    }

    public String getUnit() {
        return unit;
    }

    public String getDirCode() {
        return dirCode;
    }

    public String getParentCode() {
        return parentCode;
    }

    private int docId;
    private String smallImg;
    private String[] urlAry = new String[0];
    private String softRank;
    private String accredit;
    private String urls;
    private String fileType;
    private String lang;
    private String softType;
    private String os;
    private String officalUrl;
    private String officalDemo;
    private int fileSize = 0;
    private long downloadCount;
    private String unit;
    /**
     * 所属CMS的目录
     */
    private String dirCode;
    /**
     * 所属CMS目录的父目录，便于统计
     */
    private String parentCode;
}
