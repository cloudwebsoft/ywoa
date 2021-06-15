package com.redmoon.oa.fileark;

import com.redmoon.oa.db.SequenceManager;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.web.Global;
import java.io.File;
import cn.js.fan.db.Conn;
import java.sql.ResultSet;
import cn.js.fan.util.StrUtil;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

/**
 * <p>Title:用于处理当webedit时数据库中保存的图片和flash </p>
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
public class DocImage {
    public static String KIND_DOCUMENT = "document";

    public DocImage() {
    }

    public boolean create(String filePath, String mainKey, String kind,
                          int subKey) {
        long imgId = SequenceManager.nextID(SequenceManager.CMS_IMAGES);
        /*
         String sql =
         "insert into cms_images (id,path,mainkey,kind,subkey) values (" +
                            imgId + "," +
         StrUtil.sqlstr(filePath) + "," + StrUtil.sqlstr(mainKey) +
                            ",'document'," + this.pageNum + ")";
         */
        String sql =
                "insert into cms_images (id,path,mainkey,kind,subkey) values (?,?,?,?,?)";
        boolean re = false;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = jt.executeUpdate(sql, new Object[] {new Long(imgId), filePath,
                                  mainKey, kind, new Integer(subKey)}) == 1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        return re;
    }

    /**
     * 删除文章中的image，当高级编辑时
     * @param docId int
     */
    public void delOfDoc(int docId, int pageNum) {
        // 删除图像文件
        String sql = "select path from cms_images where mainkey=" + docId +
                     " and kind='document' and subkey=" + pageNum;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            ResultSet rs = conn.executeQuery(sql);
            if (rs != null) {
                String fpath = "";
                while (rs.next()) {
                    fpath = rs.getString(1);
                    if (fpath != null) {
                        File virtualFile = new File(Global.getRealPath() +
                                fpath);
                        virtualFile.delete();
                    }
                }
            }
            // 从数据库中删除图像
            sql = "delete from cms_images where mainkey=" + docId +
                  " and kind='document' and subkey=" + pageNum;
            conn.executeUpdate(sql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     *  如果文章页是从中间某页后插入，而不是被加至末尾，则更新页码
     * * @param docId int
     * @param pageNum int
     */
    public void onInsertDocPage(int docId, int pageNum) {
        String sql = "update cms_images set subkey=subkey+1 where mainkey=? and subkey>=? and kind='document'";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            jt.executeUpdate(sql, new Object[] {new Integer(docId),
                             new Integer(pageNum)});
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("onInsertDocPage:" + e.getMessage());
        }
    }

    /**
     * 当删除文章页时，更新页码
     * @param docId int
     * @param pageNum int
     */
    public void onDelDocPage(int docId, int pageNum) {
        String sql = "update cms_images set subkey=subkey-1 where mainkey=? and subkey>? and kind='document'";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            jt.executeUpdate(sql, new Object[] {new Integer(docId),
                             new Integer(pageNum)});
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("onDelDocPage:" + e.getMessage());
        }
    }

    /**
     * 取得文章中的第一个图片的路径，注意只有当为高级发布的文章时，才通过此方法获取，而当采用普通方式时，图片是放在attachment中的
     * @param docId int
     * @return String
     */
    public String getFirstImagePathOfDoc(int docId) {
        String sql =
                "select path from cms_images where mainkey=? order by id asc";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {""+docId});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return rr.getString(1);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getFirstImagePathOfDoc:" + e.getMessage());
        }
        return null;
    }
}
