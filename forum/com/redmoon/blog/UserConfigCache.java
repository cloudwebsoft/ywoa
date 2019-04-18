package com.redmoon.blog;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.RMCache;
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
public class UserConfigCache extends ObjectCache {
    String userPrefix = "user_";
    String domainPrefix = "domain_";

    public UserConfigCache() {
    }

    public UserConfigCache(UserConfigDb userConfigDb) {
        super(userConfigDb);
    }

    public void refreshUserBlogId(String userName) {
        try {
            RMCache.getInstance().remove(userPrefix + userName, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshUserBlogId:" + e.getMessage());
        }
    }

    public long getBlogIdByUserName(String userName) {
        Long ID = null;
        try {
            ID = (Long)RMCache.getInstance().getFromGroup(userPrefix + userName, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getBlogIdByUserName1:" + e.getMessage());
        }
        if (ID==null) {
            UserConfigDb ucd = new UserConfigDb();
            long id = ucd.getBlogIdByUserNameFromDb(userName);
            ID = new Long(id);
            try {
                RMCache.getInstance().putInGroup(userPrefix + userName, group,
                                                 ID);
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("getBlogIdByUserName2:" + e.getMessage());
            }
        }
        return ID.longValue();
    }

    public void refreshDomain(String domain) {
        try {
            RMCache.getInstance().remove(domainPrefix + domain, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshDomain:" + e.getMessage());
        }
    }

    public long getBlogIdByDomain(String domain) {
        Long ID = null;
        try {
            ID = (Long)RMCache.getInstance().getFromGroup(domainPrefix + domain, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getBlogIdByDomain1:" + e.getMessage());
        }
        if (ID==null) {
            UserConfigDb ucd = new UserConfigDb();
            long id = ucd.getBlogIdByDomainFromDb(domain);
            ID = new Long(id);
            try {
                RMCache.getInstance().putInGroup(domainPrefix + domain, group,
                                                 ID);
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("getBlogIdByDomain2:" + e.getMessage());
            }
        }
        return ID.longValue();
    }
}
