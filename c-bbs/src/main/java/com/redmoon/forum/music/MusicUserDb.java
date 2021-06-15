package com.redmoon.forum.music;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.forum.SequenceMgr;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class MusicUserDb extends QObjectDb {
    public MusicUserDb() {
    }

    public long create(String userName, String orderUser, long musicId) {
        JdbcTemplate jt = new JdbcTemplate();
        long id = SequenceMgr.nextID(SequenceMgr.SQ_FORUM_MUSIC_USER);
        try {
            boolean re = jt.executeUpdate(getTable().getQueryCreate(), new Object[] {new Long(id), new Long(musicId), userName, orderUser, new java.util.Date()})==1;
            if (re) {
                refreshCreate();
            }
        }
        catch (SQLException e) {
            id = -1;
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return id;
    }
}
