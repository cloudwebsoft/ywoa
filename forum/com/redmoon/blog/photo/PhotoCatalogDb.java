package com.redmoon.blog.photo;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import java.sql.SQLException;

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
public class PhotoCatalogDb extends QObjectDb {
    public PhotoCatalogDb() {
        super();
    }

    public boolean create(JdbcTemplate jt, Object[] params) throws
            ResKeyException {
        boolean re = super.create(jt, params);
        if (re) {
            refreshList(getString("blog_id"));
        }
        return re;
    }


}
