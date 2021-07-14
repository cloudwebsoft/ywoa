package com.redmoon.forum;

import java.sql.ResultSet;
import cn.js.fan.web.Global;
import java.sql.SQLException;
import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import org.apache.log4j.Logger;
import java.sql.PreparedStatement;
import java.util.Vector;
import cn.js.fan.cache.jcs.RMCache;
import java.util.Iterator;

/**
 *
 * <p>Title:论坛版块子节点的缓存管理 </p>
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
public class LeafChildrenCacheMgr {
    String parentCode;
    String connname;
    RMCache rmCache = RMCache.getInstance();
    static String cachePrix = "boardlist_";
    static String group = "BoardLeafChildren";
    static Logger logger = Logger.getLogger(LeafChildrenCacheMgr.class.getName());
    Vector children = null;

    public LeafChildrenCacheMgr(String parentCode) {
        this.parentCode = parentCode;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("LeafChildrenCacheMgr:conname is empty.");
        children = getLeafChildren();
    }

    public Vector getChildren() {
        return children;
    }

    public Vector getLeafChildren() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(cachePrix + parentCode, group);
            if (v == null) {
                v = load();
                rmCache.putInGroup(cachePrix + parentCode, group, v);
            }
            else {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    Leaf lf = (Leaf) ir.next();
                    lf.renew();
                }
            }
        } catch (Exception e) {
            logger.error("getLeafChildren:" + e.getMessage());
        }
        return v;
    }

    public Vector load() {
        Directory dir = new Directory();
        Leaf leaf = dir.getLeaf(parentCode);
        //Vector v = new Vector();
        // v.addElement(leaf);
        return leaf.getChildren();
        //if (!childv.isEmpty())
        //    v.addAll(childv);
        //return v;
    }

    public static void remove(String parentCode) {
        try {
            // RMCache rmCache = RMCache.getInstance();
            // rmCache.remove(cachePrix + parentCode, group);

            removeAll();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static void removeAll() {
        try {
            RMCache rmCache = RMCache.getInstance();
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
