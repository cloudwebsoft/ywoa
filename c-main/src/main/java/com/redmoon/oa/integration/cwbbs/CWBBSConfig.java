package com.redmoon.oa.integration.cwbbs;

import com.cloudwebsoft.framework.util.*;

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
public class CWBBSConfig extends XMLConfig {
    public static CWBBSConfig cfg = null;
    private static Object initLock = new Object();

    public CWBBSConfig(String xmlFileName) {
        super(xmlFileName);
    }

    public static CWBBSConfig getInstance() {
        if (cfg == null) {
            synchronized (initLock) {
                cfg = new CWBBSConfig("config_cwbbs.xml");
            }
        }
        return cfg;
    }

    public static void reload() {
        cfg = null;
    }
}
