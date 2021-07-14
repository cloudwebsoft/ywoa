package cn.js.fan.module.cms.site;

import com.cloudwebsoft.framework.base.*;
import cn.js.fan.security.AntiXSS;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.ErrMsgException;

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
public class SiteAdDb extends QObjectDb {
    public SiteAdDb() {
    }

    public boolean create(JdbcTemplate jt, ParamChecker paramChecker) throws
            ResKeyException, ErrMsgException {
        String content = (String) paramChecker.getValue("content");
        content = AntiXSS.antiXSS(content);
        paramChecker.setValue("content", "content", content);
        return super.create(jt, paramChecker);
    }

    public synchronized boolean save(JdbcTemplate jt, ParamChecker paramChecker) throws ResKeyException, ErrMsgException {
        String content = (String) paramChecker.getValue("content");
        content = AntiXSS.antiXSS(content);
        paramChecker.setValue("content", "content", content);
        return super.save(jt, paramChecker);
    }

}
