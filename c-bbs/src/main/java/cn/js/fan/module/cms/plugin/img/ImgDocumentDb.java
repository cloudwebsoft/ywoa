package cn.js.fan.module.cms.plugin.img;

import java.sql.*;
import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.module.cms.plugin.base.IPluginDocument;

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
public class ImgDocumentDb extends ObjectDb implements IPluginDocument {
    public static final int PAGE_TYPE_SINGLE = 0;
    public static final int PAGE_TYPE_MULTI = 1;

    public static final String SEPERATOR_IMG = "#@#";
    public static final String SEPERATOR_IMG_URL_DESC = "@@@";

    public ImgDocumentDb() {
    }

    public ImgDocumentDb(int docId) {
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
        return new ImgDocumentDb(pk.getIntValue());
    }

    public ImgDocumentDb getImgDocumentDb(int docId) {
        return (ImgDocumentDb)getObjectDb(new Integer(docId));
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setInt(1, docId);
            ps.setString(2, smallImg);
            ps.setInt(3, pageType);
            ps.setString(4, images);
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
                pageType = rs.getInt(2);
                images = rs.getString(3);
                String[] ary = StrUtil.split(images, SEPERATOR_IMG);
                if (ary!=null) {
                    int len = ary.length;
                    imageAry = new String[len][2];
                    for (int i=0; i<len; i++) {
                        String[] ary2 = StrUtil.split(ary[i], SEPERATOR_IMG_URL_DESC);
                        imageAry[i][0] = ary2[0];
                        if (ary2.length>1)
                            imageAry[i][1] = ary2[1];
                        else
                            imageAry[i][1] = "";
                    }
                }
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
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
                ps.setInt(2, pageType);
                ps.setString(3, images);
                ps.setInt(4, docId);
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
        this.tableName = "cws_cms_img_doc";

        primaryKey = primaryKey = new PrimaryKey("doc_id", PrimaryKey.TYPE_INT);
        objectCache = new ImgDocumentCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE doc_id=?";
        this.QUERY_CREATE =
                "INSERT " + tableName + " (doc_id,small_img,page_type,images) VALUES (?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT small_img,page_type,images FROM " + tableName + " WHERE doc_id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET small_img=?,page_type=?,images=? WHERE doc_id=?";
        isInitFromConfigDB = false;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getDocId() {
        return docId;
    }

    public void setPageType(int pageType) {
        this.pageType = pageType;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setSmallImg(String smallImg) {
        this.smallImg = smallImg;
    }

    public int getPageType() {
        return pageType;
    }

    public String getImages() {
        return images;
    }

    public String getSmallImg() {
        return smallImg;
    }

    public String[][] getImageAry() {
        return imageAry;
    }

    public int getPageCount() {
        if (pageType == PAGE_TYPE_SINGLE) {
            return IPluginDocument.PAGE_COUNT_NONE;
        }
        else {
            return imageAry.length;
        }
    }

    private int docId;
    private int pageType = PAGE_TYPE_SINGLE;
    private String images;
    private String smallImg;
    private String[][] imageAry = new String[0][0];
}
