package cn.js.fan.module.cms;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.sql.SQLException;
import com.cloudwebsoft.framework.base.QObjectBlockIterator;

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
public class TemplateCatalogDb extends QObjectDb {
    public static String CATALOG_CODE_DEFAULT = "-1";

    public TemplateCatalogDb() {
    }

    public boolean save(JdbcTemplate jt, Object[] params) throws SQLException {
        boolean re = super.save(jt, params);
        refreshList(); // 刷新列表缓存，因为有可能更改了orders
        return re;
    }

    public TemplateCatalogDb getTemplateCatalogDb(String code) {
        return (TemplateCatalogDb)getQObjectDb(code);
    }

    /**
     * 取出默认模板套系
     * @return TemplateCatalogDb
     */
    public TemplateCatalogDb getDefaultTemplateCatalogDb() {
        String sql = "select code from " + getTable().getName() + " order by orders asc";
        // System.out.println(getClass() + " sql=" + sql);
        QObjectBlockIterator oir = getQObjects(sql, 0, 1);
        if (oir.hasNext()) {
            return (TemplateCatalogDb)oir.next();
        }
        return null;
    }
}
