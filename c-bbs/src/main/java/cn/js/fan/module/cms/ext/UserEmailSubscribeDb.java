package cn.js.fan.module.cms.ext;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.module.cms.Config;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UserEmailSubscribeDb extends QObjectDb {
    public UserEmailSubscribeDb() {
    }

    public boolean init(String userName) {
        boolean re = false;
        try {
            re = create(new JdbcTemplate(), new Object[] {userName, "",""});
        }
        catch (ResKeyException e) {
            // LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new IllegalAccessError(e.getMessage());
        }
        return re;
    }

    public UserEmailSubscribeDb getUserEmailSubscribeDb(String userName) {
        UserEmailSubscribeDb uesd = (UserEmailSubscribeDb) getQObjectDb(
                userName);
        if (uesd == null) {
            init(userName);
            return (UserEmailSubscribeDb) getQObjectDb(userName);
        } else
            return uesd;
    }
}
