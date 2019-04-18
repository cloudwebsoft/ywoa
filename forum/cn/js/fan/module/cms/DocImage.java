package cn.js.fan.module.cms;

import com.redmoon.forum.SequenceMgr;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;

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
        long imgId = SequenceMgr.nextID(SequenceMgr.CMS_IMAGES);
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
}
