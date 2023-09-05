package com.redmoon.oa.visual;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Vector;

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
public class ModulePrivCache extends ObjectCache {
    static final String REFIX_PRIV = "PRIVS_";

    public ModulePrivCache(ModulePrivDb mpd) {
        super(mpd);
    }

    public Vector<ModulePrivDb> getModulePrivsOfModule(String formCode) {
        Vector<ModulePrivDb> v = null;
        try {
            v = (Vector<ModulePrivDb>) rmCache.getFromGroup(REFIX_PRIV + formCode, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getModulePrivsOfModule:" + e.getMessage());
        }
        if (v == null) {
            ModulePrivDb mpd = (ModulePrivDb)objectDb;
            v = mpd.getModulePrivsOfModuleRaw(formCode);
            try {
                rmCache.putInGroup(REFIX_PRIV + formCode, group, v);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("getModulePrivsOfModule:" + e.getMessage());
            }
        }
        return v;
    }

    public void refreshPrivs(String formCode) {
        try {
            rmCache.remove(REFIX_PRIV + formCode, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshPrivs:" + e.getMessage());
        }
    }

}
