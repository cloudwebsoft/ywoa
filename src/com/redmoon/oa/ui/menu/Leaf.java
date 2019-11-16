package com.redmoon.oa.ui.menu;

import cn.js.fan.base.ITagSupport;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.Conn;
import cn.js.fan.db.RMConn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.redmoon.oa.basic.SelectKindPriv;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.visual.ModulePrivDb;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

public class Leaf implements Serializable, ITagSupport {
    public static String CODE_ROOT = "root";
    public static String CODE_BOTTOM = "bottom";

    final String tableName = "oa_menu";
    String connname = "";

    transient RMCache rmCache = RMCache.getInstance();
    transient Logger logger = Logger.getLogger(Leaf.class.getName());

    public static final int TYPE_PRESET = 1;
    public static final int TYPE_LINK = 0;
    public static final int TYPE_MODULE = 2;
    public static final int TYPE_FLOW = 3;
    /**
     * 基础数据
     */
    public static final int TYPE_BASICDATA = 4;
    
    /**
     * 默认型
     */
    public static final int KIND_DEFAULT = 0;
    /**
     * 政府型
     */
    public static final int KIND_GOV = 1;
    
    /**
     * 企业型
     */
    public static final int KIND_COM = 2;    

    private String target;
    private boolean widget = false;

    private String code = "", name = "", link = "", parent_code = "-1",
            root_code = "";
    
    /**
     * 存储模块编码或流程类型
     */
    private String formCode;
    
    java.util.Date add_date;
    private int orders = 1, layer = 1, child_count = 0, islocked = 0;
    final String LOAD = "select code,name,link,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,pre_code,width,is_has_path,is_resource,target,pvg,icon,is_use,is_nav,form_code,can_repeat,big_icon,is_widget,widget_width,widget_height,kind,is_system,font_icon,description from " + tableName + " where code=?";
    boolean isHome = false;
    final String dirCache = "OA_MENU";
    private String bigIcon;
    private int widgetHeight;
    private int widgetWidth;
    private boolean isSystem;     //判断是否是系统菜单

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    public String get(String field) {
        if (field.equals("code"))
            return getCode();
        else if (field.equals("name"))
            return getName();
        else if (field.equals("desc"))
            return getLink();
        else if (field.equals("parent_code"))
            return getParentCode();
        else if (field.equals("root_code"))
            return getRootCode();
        else if (field.equals("layer"))
            return "" + getLayer();
        else
            return "";
    }

    public Leaf() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Leaf:DB is empty!");
    }

    public Leaf(String code) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Leaf:DB is empty!");
        this.code = code;
        loadFromDb();
    }
    
    public void renew() {
        if (logger == null) {
            logger = Logger.getLogger(Leaf.class.getName());
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
                link = StrUtil.getNullStr(rs.getString(3));
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
                preCode = StrUtil.getNullStr(rs.getString(13));
                width = rs.getInt(14);
                hasPath = rs.getInt(15)==1;
                resource = rs.getInt(16)==1;
                target = StrUtil.getNullStr(rs.getString(17));
                pvg = StrUtil.getNullStr(rs.getString(18));
                icon = StrUtil.getNullStr(rs.getString(19));
                use = rs.getInt(20)==1;
                nav = rs.getInt(21)==1;
                formCode = StrUtil.getNullStr(rs.getString(22));
                canRepeat = rs.getInt(23)==1;
                bigIcon = StrUtil.getNullStr(rs.getString(24));

                widget = rs.getInt(25)==1;
                widgetWidth = rs.getInt(26);
                widgetHeight = rs.getInt(27);
                kind = rs.getInt(28);
                isSystem = rs.getInt(29) > 0 ? true : false ;
                fontIcon = StrUtil.getNullStr(rs.getString(30));
                description = StrUtil.getNullStr(rs.getString(31));
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

    public void setLink(String link) {
        this.link = link;
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

    public String getRootCode() {
        return root_code;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public String getLink() {
        return link;
    }

    public int getType() {
        return type;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getPreCode() {
        return preCode;
    }

    public int getWidth() {
        return width;
    }

    public boolean isHasPath() {
        return hasPath;
    }

    public boolean isResource() {
        return resource;
    }

    public String getTarget() {
        return target;
    }

    public String getPvg() {
        return pvg;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isUse() {
        return use;
    }

    public boolean isNav() {
        return nav;
    }

    public String getFormCode() {
        return formCode;
    }

    public boolean isCanRepeat() {
        return canRepeat;
    }

    public String getBigIcon() {
        return bigIcon;
    }

    public boolean isWidget() {
        return widget;
    }

    public int getWidgetWidth() {
        return widgetWidth;
    }

    public int getWidgetHeight() {
        return widgetHeight;
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
                    v.addElement(getLeaf(c));
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
    public Vector getAllChild(Vector vt, Leaf leaf) throws ErrMsgException {
        Vector children = leaf.getChildren();
        if (children.isEmpty())
            return children;
        vt.addAll(children);
        Iterator ir = children.iterator();
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            getAllChild(vt, lf);
        }
        // return children;
        return vt;
    }

    public String toString() {
        return "menu is " + code;
    }

    private int type;

    public synchronized boolean update() {
        String sql = "update " + tableName + " set name=" + StrUtil.sqlstr(name) +
                     ",link=" + StrUtil.sqlstr(link) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") +
                     ",orders=" + orders + ",layer=" + layer + ",child_count=" + child_count + ",pre_code=" + StrUtil.sqlstr(preCode) + ",width=" + width +
                     ",parent_code=" + StrUtil.sqlstr(parent_code) +
                     ",is_has_path=" + (hasPath?1:0) + ",is_resource=" + (resource?1:0) + ",target=" + StrUtil.sqlstr(target) + ",pvg=" + StrUtil.sqlstr(pvg) + ",icon=" + StrUtil.sqlstr(icon) + ",is_use=" + (use?1:0) + ",is_nav=" + (nav?1:0) + ",form_code=" + StrUtil.sqlstr(formCode) +
                     ",can_repeat=" + (canRepeat?1:0) +  ",big_icon=" + StrUtil.sqlstr(bigIcon) + ",is_widget=" + (widget?1:0) + ",widget_width=" + widgetWidth + ",widget_height=" + widgetHeight + ",font_icon=" + StrUtil.sqlstr(fontIcon) + ",description=" + StrUtil.sqlstr(description) + " where code=" + StrUtil.sqlstr(code);
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
                    LeafChildrenCacheMgr.remove(parent_code);
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
     * 更改了父节点
     * @param newParentCode String
     * @return boolean
     */
    public synchronized boolean update(String newParentCode) throws ErrMsgException {
        if (newParentCode.equals(parent_code))
            return false;
        if (newParentCode.equals(code))
            throw new ErrMsgException("Node's parent can not be self！");
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        Leaf lfparent = getLeaf(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update " + tableName + " set name=" + StrUtil.sqlstr(name) +
                     ",link=" + StrUtil.sqlstr(link) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") +
                     ",parent_code=" + StrUtil.sqlstr(newParentCode) + ",orders=" + neworders +
                     ",layer=" + (parentLayer+1) + ",pre_code=" + StrUtil.sqlstr(preCode) + ",width=" + width + ",target=" + StrUtil.sqlstr(target) + ",pvg=" + StrUtil.sqlstr(pvg) + ",icon=" + StrUtil.sqlstr(icon) + ",is_use=" + (use?1:0) + ",is_nav=" + (nav?1:0) + ",form_code=" + StrUtil.sqlstr(formCode) +
                     ",can_repeat=" + (canRepeat?1:0) + ",big_icon=" + StrUtil.sqlstr(bigIcon) + ",is_widget=" + (widget?1:0) + ",widget_width=" + widgetWidth + ",widget_height=" + widgetHeight + ",font_icon=" + StrUtil.sqlstr(fontIcon) + ",description=" + StrUtil.sqlstr(description) + " where code=" + StrUtil.sqlstr(code);

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
                    LeafChildrenCacheMgr.remove(oldParentCode);
                    LeafChildrenCacheMgr.remove(newParentCode);

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from " + tableName + " where parent_code=" + StrUtil.sqlstr(oldParentCode) +
                          " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord)ri.next();
                        Leaf clf = getLeaf(rr.getString(1));
                        clf.setOrders(clf.getOrders() - 1);
                        clf.update();
                    }

                    // 更新其所有子结点的layer
                    Vector vt = new Vector();
                    getAllChild(vt, this);
                    int childcount = vt.size();
                    Iterator ir = vt.iterator();
                    while (ir.hasNext()) {
                        Leaf childlf = (Leaf)ir.next();
                        int layer = parentLayer + 1 + 1;
                        String pcode = childlf.getParentCode();
                        while (!pcode.equals(code)) {
                            layer ++;
                            Leaf lfp = getLeaf(pcode);
                            pcode = lfp.getParentCode();
                        }

                        childlf.setLayer(layer);
                        childlf.update();
                    }

                    // 将其原来的父结点的孩子数-1
                    Leaf oldParentLeaf = getLeaf(oldParentCode);
                    oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
                    oldParentLeaf.update();

                    // 将其新父结点的孩子数 + 1
                    Leaf newParentLeaf = getLeaf(newParentCode);
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

    public boolean AddChild(Leaf childleaf) throws
            ErrMsgException {

        //计算得出插入结点的orders
        int childorders = child_count + 1;

        String updatesql = "";
        String insertsql = "insert into " + tableName + " (code,name,parent_code,link,orders,root_code,child_count,layer,type,add_date,pre_code,width,is_has_path,is_resource,target,pvg,icon,is_use,is_nav,form_code,can_repeat,big_icon,is_widget,widget_width,widget_height,font_icon,description) values (";
        insertsql += StrUtil.sqlstr(childleaf.getCode()) + "," +
                StrUtil.sqlstr(childleaf.getName()) +
                "," + StrUtil.sqlstr(code) +
                "," + StrUtil.sqlstr(childleaf.getLink()) + "," +
                childorders + "," + StrUtil.sqlstr(root_code) +
                ",0," + (layer+1) + "," + childleaf.getType() +
                "," + StrUtil.sqlstr("" + System.currentTimeMillis()) + "," + StrUtil.sqlstr(childleaf.getPreCode()) + "," + childleaf.getWidth() + "," + (childleaf.isHasPath()?1:0) + "," + (childleaf.isResource()?1:0) + "," + StrUtil.sqlstr(childleaf.getTarget()) + "," + StrUtil.sqlstr(childleaf.getPvg()) + "," + StrUtil.sqlstr(childleaf.getIcon()) + "," + (childleaf.isUse()?1:0) + "," + (childleaf.isNav()?1:0) + "," + StrUtil.sqlstr(childleaf.getFormCode()) +
                "," + (childleaf.isCanRepeat()?1:0) + "," + StrUtil.sqlstr(childleaf.getBigIcon()) + "," + (childleaf.isWidget()?1:0) + "," + childleaf.getWidgetWidth() + "," + childleaf.getWidgetHeight() + "," + StrUtil.sqlstr(childleaf.getFontIcon()) + "," + StrUtil.sqlstr(childleaf.getDescription()) + ")";

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
            LeafChildrenCacheMgr.remove(code);
        } catch (Exception e) {
            logger.error("removeFromCache: " + e.getMessage());
        }
    }

    public void removeAllFromCache() {
        try {
            rmCache.invalidateGroup(dirCache);
            LeafChildrenCacheMgr.removeAll();
        } catch (Exception e) {
            logger.error("removeAllFromCache: " + e.getMessage());
        }
    }

    public Leaf getLeaf(String code) {
        Leaf leaf = null;
        try {
            leaf = (Leaf) rmCache.getFromGroup(code, dirCache);
        } catch (Exception e) {
            logger.error("getLeaf1: " + e.getMessage());
        }
        if (leaf == null) {
            leaf = new Leaf(code);
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
    /**
     * 根据Code从数据库中获取leaf
     * @param code
     * @return
     */
    public int getLeafFromDb(String code) {
        int isUse  = 0;
        RMConn rmconn = new RMConn(connname);
        try {
        	String sql = "SELECT is_Use from " + tableName + " where code=" + StrUtil.sqlstr(code);
        	ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                isUse = rr.getInt(1);
            }
        } catch (Exception e) {
            logger.error("getLeaf1: " + e.getMessage());
        }
       

        return isUse;
    }
    /**
     * 修改leaf的状态（停用启用）
     * @param leaf
     * @return
     */
    public synchronized boolean modifyLeafStyle(Leaf leaf) throws ErrMsgException
    {
        RMConn conn = new RMConn(connname);
        boolean isUse = leaf.isUse();
        int useStyle = 0;
        if (isUse)
        {
        	useStyle = 1;
        }
        String sql = "update " + tableName +  " set is_Use=" + useStyle + " where code='" + leaf.getCode() + "'";
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                	//删除该节点对应的cache
                    removeFromCache(leaf.getCode());
                    //删除父节点对应的cache
                    removeFromCache(leaf.getParentCode());
                }
            } catch (Exception e) {
                logger.error("update: " + e.getMessage());
                throw new ErrMsgException("refresh cache error！");
            }
        } catch (SQLException e) {
            logger.error("update: " + e.getMessage());
            throw new ErrMsgException("update error！");
        }
        boolean re = r == 1 ? true : false;
        if (re) {
        	//删除该节点对应的cache
            removeFromCache(leaf.getCode());
            //删除父节点对应的cache
            removeFromCache(leaf.getParentCode());
        }
        return re;
    }

    public boolean delsingle(Leaf leaf) {
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

    public void del(Leaf leaf) {
        delsingle(leaf);
        Iterator children = getChildren().iterator();
        while (children.hasNext()) {
            Leaf lf = (Leaf) children.next();
            del(lf);
        }
    }

    public Leaf getBrother(String direction) {
        String sql;
        RMConn rmconn = new RMConn(connname);
        Leaf bleaf = null;
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
                bleaf = getLeaf(rr.getString(1));
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

        Leaf bleaf = getBrother(direction);
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
                LeafChildrenCacheMgr.removeAll();
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

    public void setPreCode(String preCode) {
        this.preCode = preCode;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHasPath(boolean hasPath) {
        this.hasPath = hasPath;
    }

    public void setResource(boolean resource) {
        this.resource = resource;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setPvg(String pvg) {
        this.pvg = pvg;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    public void setNav(boolean nav) {
        this.nav = nav;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public void setCanRepeat(boolean canRepeat) {
        this.canRepeat = canRepeat;
    }

    public void setBigIcon(String bigIcon) {
        this.bigIcon = bigIcon;
    }

    public void setWidget(boolean widget) {
        this.widget = widget;
    }

    public void setWidgetWidth(int widgetWidth) {
        this.widgetWidth = widgetWidth;
    }

    public void setWidgetHeight(int widgetHeight) {
        this.widgetHeight = widgetHeight;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    public String getName(HttpServletRequest request) {
    	if(name.startsWith("#")){
			return LocalUtil.LoadString(request, "res.ui.menu",code);
		}else{
			return name;
		}
        /*if (resource) {
            return SkinUtil.LoadString(request, "res.label.forum.menu", name);
        }
        else
            return name;*/
    }
   

    public String getLink(HttpServletRequest request) {
        if (type==TYPE_MODULE) {
            // ModulePrivDb mpd = new ModulePrivDb();
            return "visual/module_list.jsp?code=" + StrUtil.UrlEncode(formCode);
        }
        else if (type==TYPE_FLOW) {
        	return "flow_initiate1.jsp?op=" + StrUtil.UrlEncode(formCode);
        }
        else if (type==TYPE_BASICDATA) {
        	return "admin/basic_select_list.jsp?kind=" + formCode;
        }
        else if (type==TYPE_LINK) {
        	if (link.indexOf("$")!=-1) {
        		UserSetupDb usd = new UserSetupDb();
        		Privilege pvg = new Privilege();
        		usd = usd.getUserSetupDb(pvg.getUser(request));
        		String lk = link;
        		lk = lk.replaceFirst("\\$emailName", StrUtil.UrlEncode(usd.getEmailName()));
        		lk = lk.replaceFirst("\\$emailPwd", StrUtil.UrlEncode(usd.getEmailPwd()));
        		// 替换为当前用户
        		try {
        			// 先转成gbk的编码，用于润乾报表，对应的在showReport.jsp中接收参数时要加下行
        			// paramValue = new String(paramValue.getBytes("iso-8859-1"), "GB2312");
					lk = lk.replaceFirst("\\$userName", java.net.URLEncoder.encode(pvg.getUser(request),"GBK"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		// lk = lk.replaceFirst("\\$userName", pvg.getUser(request));
        		// lk = lk.replaceFirst("\\$userName", StrUtil.UTF8ToUnicode(pvg.getUser(request)));
        		return lk;
        	}
        	
            // 链接需替换路径变量$u
            if (hasPath) {
                return link.replaceFirst("\\$u", request.getContextPath());
            } else
                return link;
        }
        else {
            String lk = PresetLeaf.getLink(request, this);
            if (lk.equals("")) {
                return link;
            }
            else
                return lk;
        }
    }

    /**
     * 判断用户能否看到菜单项
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean canUserSee(HttpServletRequest request) {
        if (!use)
            return false;

        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        if (type==TYPE_FLOW) {
        	com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
        	lf = lf.getLeaf(formCode);
        	if (lf==null)
        		return false;
        	com.redmoon.oa.flow.DirectoryView dv = new com.redmoon.oa.flow.DirectoryView(lf);
        	return dv.canUserSeeWhenInitFlow(request, lf);
        }
        
        if (getCode().equals(MenuController.SALES)) {
	        License lic = License.getInstance();
	        // 平台版才可以用CRM模块
	        if (!lic.isPlatformSrc())
	            return false;
	        // 平台版才可以用CRM模块，如果许可证中的解决方案中未勾选CRM模块
		    if (lic.isSolutionVer() && !lic.canUseSolution(License.SOLUTION_CRM)) {
	            return false;
		    }
		}        

        if (type==TYPE_MODULE) {
            ModulePrivDb mpd = new ModulePrivDb(formCode);
            return mpd.canUserSee(privilege.getUser(request));
        }
        else if (type==TYPE_BASICDATA) {
            SelectKindPriv skp = new SelectKindPriv();
            int kindId = StrUtil.toInt(formCode, -1);
            return skp.canUserAppend(userName, kindId) || skp.canUserModify(userName, kindId) || skp.canUserDel(userName, kindId);
        }
        else {
            boolean re = false;
            if (!pvg.equals("")) {
                // 替换全角逗号
                pvg = pvg.replaceAll("，", ",");
                String[] ary = StrUtil.split(pvg, ",");
                for (int i = 0; i < ary.length; i++) {
                    // 如果禁止管理员看到
                    if (ary[i].trim().equalsIgnoreCase("!admin")) {
                        if (privilege.isUserPrivValid(request, "admin")) {
                            re = false;
                        }
                    }
                }
                // layer=2，对应于菜单上的一级大类项
                if (layer == 2) {
                    if (privilege.isUserPrivValid(request, "admin"))
                        re = true;
                }

                for (int i = 0; i < ary.length; i++) {
                    if (layer == 2) {
                        // 20161115 qcg发现BUG，如果菜单项置为admin可见，则admin.flow即具有流程查询权限的也可见
                        if (privilege.isUserPrivValid(request, ary[i].trim())
                                || (!ary[i].trim().equals(Privilege.ADMIN) && privilege.isUserHasPrivStartWith(request, ary[i].trim()))) {
                            re = true;
                        }
                    } else {
                        if (privilege.isUserPrivValid(request, ary[i].trim()))
                            re = true;
                    }
                }
            }
            else {
                re = MenuController.canUserSee(request, this);
            }
            return re;
        }
    }

    /**
     * 取得菜单对应记录的条数
     * @return int
     */
    public int getCount(String userName) {
        if (code.equals("message")) {
            // 取得未读短消息的条数
            MessageDb md = new MessageDb();
            return md.getNewMsgCount(userName);
        }
        else if (code.equals("flow_wait")) {
            // 取得待办流程的条数
            return WorkflowDb.getWaitCount(userName);
        }
        else
            return 0;
    }

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getKind() {
		return kind;
	}
	
	private boolean loaded = false;
    private String preCode;
    private int width = 60;
    private boolean hasPath = false;
    private boolean resource = true;
    private String pvg;
    private String icon;
    private boolean use = true;
    private boolean nav = false;
    private boolean canRepeat = false;
    
    /**
     * 字体图标
     */
    private String fontIcon;
    
    public String getFontIcon() {
		return fontIcon;
	}

	public void setFontIcon(String fontIcon) {
		this.fontIcon = fontIcon;
	}

	/**
     * 20140228暂未启用，因为在前台及管理菜单、滑动菜单的时候都需控制显示，工作量较大
     */
    private int kind = KIND_DEFAULT;

	public boolean getSystem() {
		return isSystem;
	}

	public void setSystem(boolean isSystem) {
		this.isSystem = isSystem;
	}

	

	

	

}
