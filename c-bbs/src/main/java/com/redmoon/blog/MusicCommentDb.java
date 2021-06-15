package com.redmoon.blog;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;

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
public class MusicCommentDb extends QObjectDb {
    public MusicCommentDb() {
        super();
    }

    public boolean create(JdbcTemplate jt, Object[] params) throws
            ResKeyException {
        boolean re = super.create(jt, params);
        if (re) {
            refreshList("" + ((Long) params[1]).longValue());
        }
        return re;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = super.del(jt);
        if (re) {
            refreshList("" + getLong("music_id"));
        }
        return re;
    }


}
