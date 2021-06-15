package cn.js.fan.module.cms.ui;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DesktopItemCache extends ObjectCache {
    String posCachePrefix = "DesktopItemCache_";

    public DesktopItemCache(DesktopItemDb di) {
        super(di);
    }

    public void refreshPosition(String systemCode, String position) {
        try {
            RMCache.getInstance().remove(posCachePrefix + systemCode + "_" + position, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshPosition:" + e.getMessage());
        }
    }

    public DesktopItemDb getDesktopItemDb(String systemCode, String position) {
        RMCache rc = RMCache.getInstance();
        DesktopItemDb di = null;
        try {
            di = (DesktopItemDb)rc.getFromGroup(posCachePrefix + systemCode + "_" + position, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getDesktopItemDb1:" + e.getMessage());
        }
        if (di==null) {
            DesktopItemDb did = (DesktopItemDb)objectDb;
            di = did.getDesktopItemDbByPositionRaw(systemCode, position);
            if (di!=null) {
                try {
                    rc.putInGroup(posCachePrefix + systemCode + "_" + position, group, di);
                }
                catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getDesktopItemDb2:" + e.getMessage());
                }
            }
        }
        return di;
    }
}
