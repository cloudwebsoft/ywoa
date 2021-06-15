package com.redmoon.forum.person;

import java.io.*;
import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

public class FactionDb implements Serializable, ITagSupport {
    public static final String TYPE_NONE_OPTION_VALUE = "not";

    public static String CODE_ROOT = "root";

    final String tableName = "sq_faction";
    String connname = "";

    transient RMCache rmCache = RMCache.getInstance();
    transient Logger logger = Logger.getLogger(FactionDb.class.getName());

    int docId;

    public static final int TYPE_LIST = 1;
    public static final int TYPE_NONE = 0;

    private String code = "", name = "", description = "", parent_code = "-1",
            root_code = "";
    java.util.Date add_date;
    private int orders = 1, layer = 1, child_count = 0, islocked = 0;
    final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,doc_id,template_id from " + tableName + " where code=?";
    boolean isHome = false;
    final String dirCache = "FACTION_CATALOG";

    public String get(String field) {
        if (field.equals("code"))
            return getCode();
        else if (field.equals("name"))
            return getName();
        else if (field.equals("desc"))
            return getDescription();
        else if (field.equals("parent_code"))
            return getParentCode();
        else if (field.equals("root_code"))
            return getRootCode();
        else if (field.equals("layer"))
            return "" + getLayer();
        else
            return "";
    }

    public FactionDb() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("FactionDb:DB is empty!");
    }

    public FactionDb(String code) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("FactionDb:DB is empty!");
        this.code = code;
        loadFromDb();
    }

    public void renew() {
        if (logger == null) {
            logger = Logger.getLogger(FactionDb.class.getName());
        }
        if (rmCache == null) {
            rmCache = RMCache.getInstance();
        }
    }

    public void loadFromDb() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(LOAD);
            ps.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                this.code = rs.getString(1);
                name = rs.getString(2);
                description = rs.getString(3);
                parent_code = rs.getString(4);
                root_code = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                child_count = rs.getInt(8);
                try {
                    add_date = DateUtil.parse(rs.getString(9));
                }
                catch (Exception e) {

                }
                islocked = rs.getInt(10);
                type = rs.getInt(11);
                isHome = rs.getInt(12) > 0 ? true : false;
                docId = rs.getInt(13);
                templateId = rs.getInt(14);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("loadFromDb: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public int getDocID() {
        return docId;
    }

    public void setDocID(int d) {
        this.docId = d;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setRootCode(String c) {
        this.root_code = c;
    }

    public void setType(int t) {
        this.type = t;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public int getOrders() {
        return orders;
    }

    public boolean getIsHome() {
        return isHome;
    }

    public void setParentCode(String p) {
        this.parent_code = p;
    }

    public String getParentCode() {
        return this.parent_code;
    }

    public void setIsHome(boolean b) {
        this.isHome = b;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public String getRootCode() {
        return root_code;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public int getTemplateId() {
        return templateId;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector getChildren() {
        Vector v = new Vector();
        String sql = "select code from " + tableName + " where parent_code=? order by orders asc";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    String c = rs.getString(1);
                    //logger.info("child=" + c);
                    v.addElement(getFactionDb(c));
                }
            }
        } catch (SQLException e) {
            logger.error("getChildren: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取出code结点的所有孩子结点
     * @param code String
     * @return ResultIterator
     * @throws ErrMsgException
     */
    public Vector getAllChild(Vector vt, FactionDb leaf) throws ErrMsgException {
        Vector children = leaf.getChildren();
        if (children.isEmpty())
            return children;
        vt.addAll(children);
        Iterator ir = children.iterator();
        while (ir.hasNext()) {
            FactionDb lf = (FactionDb) ir.next();
            getAllChild(vt, lf);
        }
        // return children;
        return vt;
    }

    public String toString() {
        return "FactionDb is " + name;
    }

    private int type;

    public synchronized boolean update() {
        String sql = "update " + tableName + " set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                     ",orders=" + orders + ",layer=" + layer + ",child_count=" + child_count +
                     " where code=" + StrUtil.sqlstr(code);
        // logger.info(sql);
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeFromCache(code);
                    //logger.info("cache is removed " + code);
                    //DirListCacheMgr更新
                    FactionChildrenCacheMgr.remove(parent_code);
                }
            } catch (Exception e) {
                logger.error("update: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("update: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    /**
     * 更改了分类
     * @param newDirCode String
     * @return boolean
     */
    public synchronized boolean update(String newParentCode) throws ErrMsgException {
        if (newParentCode.equals(parent_code))
            return false;
        if (newParentCode.equals(code))
            throw new ErrMsgException("不能将本节点设为父节点！");
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        FactionDb lfparent = getFactionDb(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update " + tableName + " set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                     ",parent_code=" + StrUtil.sqlstr(newParentCode) + ",orders=" + neworders +
                     ",layer=" + (parentLayer+1) +
                     " where code=" + StrUtil.sqlstr(code);

        String oldParentCode = parent_code;
        parent_code = newParentCode;
        RMConn conn = new RMConn(connname);

        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeFromCache(code);
                    removeFromCache(newParentCode);
                    removeFromCache(oldParentCode);
                    //DirListCacheMgr更新
                    FactionChildrenCacheMgr.remove(oldParentCode);
                    FactionChildrenCacheMgr.remove(newParentCode);

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from " + tableName + " where parent_code=" + StrUtil.sqlstr(oldParentCode) +
                          " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord)ri.next();
                        FactionDb clf = getFactionDb(rr.getString(1));
                        clf.setOrders(clf.getOrders() - 1);
                        clf.update();
                    }

                    // 更新其所有子结点的layer
                    Vector vt = new Vector();
                    getAllChild(vt, this);
                    int childcount = vt.size();
                    Iterator ir = vt.iterator();
                    while (ir.hasNext()) {
                        FactionDb childlf = (FactionDb)ir.next();
                        int layer = parentLayer + 1 + 1;
                        String pcode = childlf.getParentCode();
                        while (!pcode.equals(code)) {
                            layer ++;
                            FactionDb lfp = getFactionDb(pcode);
                            pcode = lfp.getParentCode();
                        }

                        childlf.setLayer(layer);
                        childlf.update();
                    }

                    // 将其原来的父结点的孩子数-1
                    FactionDb oldParentLeaf = getFactionDb(oldParentCode);
                    oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
                    oldParentLeaf.update();

                    // 将其新父结点的孩子数 + 1
                    FactionDb newParentLeaf = getFactionDb(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
                    newParentLeaf.update();
                }
            } catch (Exception e) {
                logger.error("update: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("update: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    public boolean AddChild(FactionDb childleaf) throws
            ErrMsgException {
        //计算得出插入结点的orders
        int childorders = child_count + 1;

        String updatesql = "";
        String insertsql = "insert into " + tableName + " (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date) values (";
        insertsql += StrUtil.sqlstr(childleaf.getCode()) + "," +
                StrUtil.sqlstr(childleaf.getName()) +
                "," + StrUtil.sqlstr(code) +
                "," + StrUtil.sqlstr(childleaf.getDescription()) + "," +
                childorders + "," + StrUtil.sqlstr(root_code) +
                ",0," + (layer+1) + "," + childleaf.getType() +
                "," + StrUtil.sqlstr("" + System.currentTimeMillis()) + ")";

        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException("请勿输入非法字符如;号等！");
        Conn conn = new Conn(connname);
        try {
            //更改根结点的信息
            updatesql = "Update " + tableName + " set child_count=child_count+1" +
                        " where code=" + StrUtil.sqlstr(code);
            conn.beginTrans();
            conn.executeUpdate(insertsql);
            conn.executeUpdate(updatesql);
            removeFromCache(code);
            conn.commit();

            // 加入默认权限 everyone
            //LeafPriv lp = new LeafPriv();
            //lp.add(childleaf.getCode());
        } catch (SQLException e) {
            conn.rollback();
            logger.error("AddChild: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
    }

    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     * @param code String
     */
    public void removeFromCache(String code) {
        try {
            rmCache.remove(code, dirCache);
            FactionChildrenCacheMgr.remove(code);
        } catch (Exception e) {
            logger.error("removeFromCache: " + e.getMessage());
        }
    }


    public void removeAllFromCache() {
        try {
            rmCache.invalidateGroup(dirCache);
            FactionChildrenCacheMgr.removeAll();
        } catch (Exception e) {
            logger.error("removeAllFromCache: " + e.getMessage());
        }
    }

    public FactionDb getFactionDb(String code) {
        FactionDb leaf = null;
        try {
            leaf = (FactionDb) rmCache.getFromGroup(code, dirCache);
        } catch (Exception e) {
            logger.error("getLeaf1: " + e.getMessage());
        }
        if (leaf == null) {
            leaf = new FactionDb(code);
            if (leaf != null) {
                if (!leaf.isLoaded())
                    leaf = null;
                else {
                    try {
                        rmCache.putInGroup(code, dirCache, leaf);
                    } catch (Exception e) {
                        logger.error("getLeaf2: " + e.getMessage());
                    }
                }
            }
        } else
            leaf.renew();

        return leaf;
    }

    public boolean delsingle(FactionDb leaf) {
        RMConn rmconn = new RMConn(connname);
        try {
            String sql = "delete from " + tableName + " where code=" + StrUtil.sqlstr(leaf.getCode());
            boolean r = rmconn.executeUpdate(sql) == 1 ? true : false;
            sql = "update " + tableName + " set orders=orders-1 where parent_code=" + StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" + leaf.getOrders();
            rmconn.executeUpdate(sql);
            sql = "update " + tableName + " set child_count=child_count-1 where code=" + StrUtil.sqlstr(leaf.getParentCode());
            rmconn.executeUpdate(sql);

            // removeFromCache(leaf.getCode());
            // removeFromCache(leaf.getParentCode());
            removeAllFromCache();

            // 删除该目录下的所有权限
            //LeafPriv lp = new LeafPriv(leaf.getCode());
            //lp.delPrivsOfDir();

/*
            // 删除该目录下的所有文章
            Document doc = new Document();
            try {
                doc.delDocumentByDirCode(leaf.getCode());
            }
            catch (ErrMsgException e) {
                logger.error("delsingle:" + e.getMessage());
            }
*/
        } catch (SQLException e) {
            logger.error("delsingle: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void del(FactionDb leaf) {
        delsingle(leaf);
        Iterator children = getChildren().iterator();
        while (children.hasNext()) {
            FactionDb lf = (FactionDb) children.next();
            del(lf);
        }
    }

    public FactionDb getBrother(String direction) {
        String sql;
        RMConn rmconn = new RMConn(connname);
        FactionDb bleaf = null;
        try {
            if (direction.equals("down")) {
                sql = "select code from " + tableName + " where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from " + tableName + " where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders - 1);
            }

            ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                bleaf = getFactionDb(rr.getString(1));
            }
        } catch (SQLException e) {
            logger.error("getBrother: " + e.getMessage());
        }
        return bleaf;
    }

    public boolean move(String direction) {
        String sql = "";

        // 取出该结点的移动方向上的下一个兄弟结点的orders
        boolean isexist = false;

        FactionDb bleaf = getBrother(direction);
        if (bleaf != null) {
            isexist = true;
        }

        // 如果移动方向上的兄弟结点存在则移动，否则不移动
        if (isexist) {
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();
                if (direction.equals("down")) {
                    sql = "update " + tableName + " set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update " + tableName + " set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }

                if (direction.equals("up")) {
                    sql = "update " + tableName + " set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update " + tableName + " set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                conn.commit();
                removeFromCache(code);
                removeFromCache(bleaf.getCode());
            } catch (Exception e) {
                conn.rollback();
                logger.error("move: " + e.getMessage());
                return false;
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        }

        return true;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    private int templateId = -1;
    private boolean loaded = false;

}
