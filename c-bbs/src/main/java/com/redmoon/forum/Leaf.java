package com.redmoon.forum;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin2.*;
import com.redmoon.forum.ui.*;
import org.apache.log4j.*;

/**
 *
 * <p>Title:论坛版块 </p>
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
public class Leaf implements Serializable, ITagSupport {
    public static String CODE_BLOG = "blog";

    public static String CODE_ROOT = "root";

    public static final int TYPE_DOMAIN = 0;
    public static final int TYPE_BOARD = 1;

    public static final int WEBEDIT_ALLOW_TYPE_UBB = 1; // 只能用UBB方式，暂时放弃
    public static final int WEBEDIT_ALLOW_TYPE_UBB_NORMAL = 2; // 能用UBB及NORMAL
    public static final int WEBEDIT_ALLOW_TYPE_UBB_NORMAL_REDMOON = 3; // 三种方式都可以
    public static final int WEBEDIT_ALLOW_TYPE_REDMOON_FIRST = 4; // 以高级方式优先

    public static final int CHECK_NOT = 0; // 不审核
    public static final int CHECK_TOPIC = 1; // 审核主题贴
    public static final int CHECK_TOPIC_REPLY = 2; // 审核主题贴、回贴

    public static final int DEL_DUSTBIN = 0; // 删贴时将贴子放至回收站
    public static final int DEL_FOREVER = 1; // 彻底删除

    public static final int DISPLAY_STYLE_VERTICAL = 0;
    public static final int DISPALY_STYLE_HORIZON = 1;

    transient RMCache rmCache = RMCache.getInstance();
    String connname = "";
    public transient Logger logger = Logger.getLogger(Leaf.class.getName());

    long addId;

    private String code = "", name = "", description = "", parent_code = "-1",
            root_code = "", add_date = "";
    private int orders = 1, layer = 1, child_count = 0;

    final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,add_id,logo,today_count,today_date,topic_count,post_count,theme,skin,boardRule,color,webeditAllowType,plugin2Code,check_msg,del_mode,display_style,is_bold from sq_board where code=?";
    boolean isHome = false;
    final String dirCache = "BOARD_";

    public void renew() {
        if (logger == null) {
            logger = Logger.getLogger(Leaf.class.getName());
        }
        if (rmCache == null) {
            rmCache = RMCache.getInstance();
        }
    }

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

    public Leaf() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Leaf:connname is empty");
    }

    public Leaf(String code) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Leaf:connname is empty");
        this.code = code;
        loadFromDb();
    }

    public void loadFromDb() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(LOAD);
            ps.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                this.code = rs.getString(1);
                name = rs.getString(2);
                description = StrUtil.getNullStr(rs.getString(3));
                parent_code = rs.getString(4);
                root_code = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                child_count = rs.getInt(8);
                add_date = DateUtil.format(DateUtil.parse(rs.getString(9)),
                                           "yyyy-MM-dd HH:mm:ss");
                locked = rs.getInt(10) == 1 ? true : false;
                type = rs.getInt(11);
                isHome = rs.getInt(12) > 0 ? true : false;
                addId = rs.getInt(13);
                logo = rs.getString(14);

                todayCount = rs.getInt(15);
                todayDate = DateUtil.parse(rs.getString(16));
                topicCount = rs.getInt(17);
                postCount = rs.getInt(18);
                theme = StrUtil.getNullStr(rs.getString(19));
                skin = StrUtil.getNullStr(rs.getString(20));
                boardRule = StrUtil.getNullString(rs.getString(21));
                color = StrUtil.getNullString(rs.getString(22));
                webeditAllowType = rs.getInt(23);
                plugin2Code = StrUtil.getNullStr(rs.getString(24));
                checkMsg = rs.getInt(25);
                delMode = rs.getInt(26);
                displayStyle = rs.getInt(27);
                bold = rs.getInt(28)==1;
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load: code= " + code + " " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public long getAddId() {
        return addId;
    }

    public void setAddId(long d) {
        this.addId = d;
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

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setTodayCount(int todayCount) {
        this.todayCount = todayCount;
    }

    public void setTodayDate(Date todayDate) {
        this.todayDate = todayDate;
    }

    public void setTopicCount(int topicCount) {
        this.topicCount = topicCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getRootCode() {
        return root_code;
    }

    public int getLayer() {
        return layer;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public String getLogo() {
        return logo;
    }

    public int getTodayCount() {
        if (todayCount != 0) {
            // 如果today_date字段中不为当前日期，则today_count为0
            if (!DateUtil.isSameDay(todayDate, new java.util.Date())) {
                todayCount = 0;
                todayDate = new java.util.Date();
                update();
            }
        }
        return todayCount;
    }

    public java.util.Date getTodayDate() {
        return todayDate;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public String getTheme() {
        return theme;
    }

    public String getSkin() {
        return skin;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getBoardRule() {
        return boardRule;
    }

    public String getColor() {
        return color;
    }

    public int getWebeditAllowType() {
        return webeditAllowType;
    }

    public String getPlugin2Code() {
        return plugin2Code;
    }

    public int getCheckMsg() {
        return checkMsg;
    }

    public int getDelMode() {
        return delMode;
    }

    public int getDisplayStyle() {
        return displayStyle;
    }

    public boolean isBold() {
        return bold;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector getChildren() {
        Vector v = new Vector();
        String sql =
                "select code from sq_board where parent_code=? order by orders";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
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
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
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
        return children;
    }

    public String toString() {
        return "Leaf is " + name;
    }

    private int type;

    public synchronized boolean update() {
        String sql = "update sq_board set name=?,description=?,type=?,isHome=?,add_id=?,logo=?,today_count=?,today_date=?,topic_count=?,post_count=?,theme=?,skin=?,islocked=?,boardRule=?,color=?,child_count=?,webeditAllowType=?,plugin2Code=?,check_msg=?,del_mode=?,orders=?,display_style=?,is_bold=? where code=?";
        int r = 0;
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, type);
            ps.setInt(4, isHome ? 1 : 0);
            ps.setLong(5, addId);
            ps.setString(6, logo);
            ps.setInt(7, todayCount);
            ps.setString(8, DateUtil.toLongString(todayDate));
            ps.setInt(9, topicCount);
            ps.setInt(10, postCount);
            ps.setString(11, theme);
            ps.setString(12, skin);
            ps.setInt(13, locked ? 1 : 0);
            ps.setString(14, boardRule);
            ps.setString(15, color);
            ps.setInt(16, child_count);
            ps.setInt(17, webeditAllowType);
            ps.setString(18, plugin2Code);
            ps.setInt(19, checkMsg);
            ps.setInt(20, delMode);
            ps.setInt(21, orders);
            ps.setInt(22, displayStyle);
            ps.setInt(23, bold?1:0);
            ps.setString(24, code);
            r = conn.executePreUpdate();
        } catch (Exception e) {
            logger.error("update:" + e.getMessage());
        } finally {
            removeAllFromCache();

            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {}
                ps = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return r == 1;
    }

    /**
     * 更改了分类
     * @param newDirCode String
     * @return boolean
     */
    public synchronized boolean updateParent(String newParentCode) throws
            ErrMsgException {
        if (newParentCode.equals(parent_code))
            return false;
        if (newParentCode.equals(code))
            throw new ErrMsgException("不能将本节点设为父节点！");

        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        Leaf lfparent = getLeaf(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();

        String sql = "update sq_board set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") +
                     ",add_id=" + addId + ",logo=" + StrUtil.sqlstr(logo) +
                     ",parent_code=" + StrUtil.sqlstr(newParentCode) +
                     ",orders=" +
                     neworders +
                     ",layer=" + (parentLayer + 1) + ",theme=" +
                     StrUtil.sqlstr(theme) +
                     ",skin=" + StrUtil.sqlstr(skin) + ",islocked=" +
                     ((locked) ? 1 : 0) + ",color=" + StrUtil.sqlstr(color) +
                     ",plugin2Code=" + StrUtil.sqlstr(plugin2Code) +
                     ",check_msg=" +
                     checkMsg + ",del_mode=" + delMode + ",display_style=" +
                     displayStyle + ",is_bold=" + (bold?1:0) +
                     " where code=" + StrUtil.sqlstr(code);

        String oldParentCode = parent_code;
        parent_code = newParentCode;
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeAllFromCache();

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from sq_board where parent_code=" +
                          StrUtil.sqlstr(oldParentCode) +
                          " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        Leaf clf = getLeaf(rr.getString(1));
                        clf.setOrders(clf.getOrders() - 1);
                        clf.update();
                    }

                    // 更新其所有子结点的layer
                    Vector vt = new Vector();
                    getAllChild(vt, this);
                    Iterator ir = vt.iterator();
                    while (ir.hasNext()) {
                        Leaf childlf = (Leaf) ir.next();
                        int layer = parentLayer + 1 + 1;
                        String pcode = childlf.getParentCode();
                        while (!pcode.equals(code)) {
                            layer++;
                            Leaf lfp = getLeaf(pcode);
                            pcode = lfp.getParentCode();
                        }

                        childlf.setLayer(layer);
                        childlf.update();
                    }

                    // 将其原来的父结点的孩子数-1
                    Leaf oldParentLeaf = getLeaf(oldParentCode);
                    oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() -
                                                1);
                    oldParentLeaf.update();

                    // 将其新父结点的孩子数 + 1
                    Leaf newParentLeaf = getLeaf(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() +
                                                1);
                    newParentLeaf.update();
                }
            } catch (Exception e) {
                logger.error("updateParent1: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("updateParent2: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        removeAllFromCache();

        return re;
    }

    public boolean AddChild(Leaf childleaf) throws
            ErrMsgException {
        // 计算得出插入结点的orders
        int childorders = child_count + 1;
        String updatesql = "";
        String insertsql = "insert into sq_board (code,name,parent_code,description,orders,root_code,child_count,layer,type,logo,theme,skin,islocked,color,add_date,today_date,webeditAllowType,isHome,plugin2Code,check_msg,del_mode,display_style,is_bold) values (";
        insertsql += StrUtil.sqlstr(childleaf.getCode()) + "," +
                StrUtil.sqlstr(childleaf.getName()) +
                "," + StrUtil.sqlstr(code) +
                "," + StrUtil.sqlstr(childleaf.getDescription()) + "," +
                childorders + "," + StrUtil.sqlstr(root_code) +
                ",0," + (layer + 1) + "," + childleaf.getType() + "," +
                StrUtil.sqlstr(childleaf.getLogo()) + "," +
                StrUtil.sqlstr(childleaf.getTheme()) + "," +
                StrUtil.sqlstr(childleaf.getSkin()) +
                "," + (childleaf.isLocked() ? 1 : 0) + "," +
                StrUtil.sqlstr(childleaf.getColor()) + "," +
                StrUtil.sqlstr("" + System.currentTimeMillis()) + "," +
                StrUtil.sqlstr("" + System.currentTimeMillis()) + "," +
                childleaf.getWebeditAllowType() + "," +
                (childleaf.isHome ? 1 : 0) + "," +
                StrUtil.sqlstr(childleaf.getPlugin2Code()) + "," +
                childleaf.getCheckMsg() + "," + childleaf.getDelMode() + "," +
                displayStyle + "," + (childleaf.isBold()?1:0) + ")";

        Conn conn = new Conn(connname);
        try {
            // 更改根结点的信息
            updatesql = "Update sq_board set child_count=child_count+1" +
                        " where code=" + StrUtil.sqlstr(code);
            conn.beginTrans();
            conn.executeUpdate(insertsql);
            conn.executeUpdate(updatesql);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            logger.error("AddChild: " + e.getMessage());
            return false;
        } finally {
            removeAllFromCache();
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
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
        } else {
            leaf.renew();
        }
        return leaf;
    }

    public boolean delsingle(Leaf leaf) {
        // 删除版块下对应的贴子
        if (leaf.getType() == Leaf.TYPE_BOARD) {
            MsgDb md = new MsgDb();
            md.delMsgOfBoard(leaf.getCode());
        }

        // 删除版块下的threadType
        ThreadTypeDb ttd = new ThreadTypeDb();
        ttd.delThreadTypesOfBoard(leaf.getCode());

        // 删除版块下对应的render
        BoardRenderDb brd = new BoardRenderDb();
        brd = brd.getBoardRenderDb(leaf.getCode());
        if (brd != null && brd.isLoaded()) {
            brd.del();
        }
        // 删除版块下对应的entrance
        EntranceMgr em = new EntranceMgr();
        Vector vEntrancePlugin = em.getAllEntranceUnitOfBoard(leaf.getCode());
        if (vEntrancePlugin.size() > 0) {
            Iterator irpluginentrance = vEntrancePlugin.iterator();
            while (irpluginentrance.hasNext()) {
                EntranceUnit eu = (EntranceUnit) irpluginentrance.next();
                BoardEntranceDb bed = new BoardEntranceDb();
                bed = bed.getBoardEntranceDb(leaf.getCode(), eu.getCode());
                bed.del();
            }
        }
        // 删除版块下对应的版主
        BoardManagerDb bmd = new BoardManagerDb();
        Iterator ir = bmd.getBoardManagers(leaf.getCode()).iterator();
        while (ir.hasNext()) {
            UserDb ud = (UserDb) ir.next();
            bmd = bmd.getBoardManagerDb(leaf.getCode(), ud.getName());
            bmd.del();
        }
        // 删除版块下对应的boardCode
        BoardScoreDb bsd = new BoardScoreDb();
        bsd.delBoardScoreDbsOfBoard(leaf.getCode());

        String sql = "delete from sq_board where code=" +
                     StrUtil.sqlstr(leaf.getCode());
        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            conn.executeUpdate(sql);
            sql = "update sq_board set orders=orders-1 where parent_code=" +
                  StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" +
                  leaf.getOrders();
            conn.executeUpdate(sql);
            sql = "update sq_board set child_count=child_count-1 where code=" +
                  StrUtil.sqlstr(leaf.getParentCode());
            conn.executeUpdate(sql);
            conn.commit();
            String lg = leaf.getLogo();
            // 如果原有文件存在，则删除文件
            if (lg != null && !lg.equals("")) {
                try {
                    String realpath = Global.getRealPath() + "forum/images/board_logo/";
                    File file = new File(realpath + lg);
                    file.delete();
                } catch (Exception e) {
                    logger.info("delsingle:" + e.getMessage());
                }
            }

            removeAllFromCache();
        } catch (SQLException e) {
            conn.rollback();
            logger.error("delsingle:" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
    }

    public synchronized void del(Leaf leaf) {
        delsingle(leaf);
        Iterator children = leaf.getChildren().iterator();
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
                sql = "select code from sq_board where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from sq_board where parent_code=" +
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

        //如果移动方向上的兄弟结点存在则移动，否则不移动
        if (isexist) {
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();
                if (direction.equals("down")) {
                    sql = "update sq_board set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update sq_board set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }

                if (direction.equals("up")) {
                    sql = "update sq_board set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update sq_board set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                logger.error("move: " + e.getMessage());
                return false;
            } finally {
                removeAllFromCache();

                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
        }

        return true;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setBoardRule(String boardRule) {
        this.boardRule = boardRule;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setWebeditAllowType(int webeditAllowType) {
        this.webeditAllowType = webeditAllowType;
    }

    public void setPlugin2Code(String plugin2Code) {
        this.plugin2Code = plugin2Code;
    }

    public void setCheckMsg(int checkMsg) {
        this.checkMsg = checkMsg;
    }

    public void setDelMode(int delMode) {
        this.delMode = delMode;
    }

    public void setDisplayStyle(int displayStyle) {
        this.displayStyle = displayStyle;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public Vector getAllPlugin2() {
        Vector v = new Vector();
        String[] ary = StrUtil.split(plugin2Code, ",");
        if (ary != null) {
            Plugin2Mgr pm = new Plugin2Mgr();
            Plugin2Unit pu = null;
            int len = ary.length;
            for (int i = 0; i < len; i++) {
                pu = pm.getPlugin2Unit(ary[i]);
                if (pu != null)
                    v.addElement(pu);
            }
        }
        return v;
    }

    public boolean isUsePlugin2(String code) {
        String[] ary = StrUtil.split(plugin2Code, ",");
        if (ary != null) {
            int len = ary.length;
            Plugin2Mgr pm = new Plugin2Mgr();
            Plugin2Unit pu = null;
            for (int i = 0; i < len; i++) {
                if (ary[i].equals(code))
                    return true;
            }
        }
        return false;
    }

    /**
     * 用于在position.jsp中获取版块下拉菜单
     * @param request
     * @param lf
     * @return
     * @throws ErrMsgException
     */
    public String getMenuString(HttpServletRequest request, Leaf lf) throws
             ErrMsgException {
         if (lf.getChildCount() == 0)
             return "";

         boolean isBoardCannotEnterShow = true;
         if (!Config.getInstance().getBooleanProperty(
                 "forum.isBoardCannotEnterShow"))
             isBoardCannotEnterShow = false;

         String str = null;
         if (isBoardCannotEnterShow) {
             try {
                 str = (String) rmCache.getFromGroup("menu_" + lf.getCode(),
                         dirCache);
             } catch (Exception e) {
                 logger.error("getMenuString: " + e.getMessage());
             }
         }
         if (str == null) {
             StringBuffer sb = new StringBuffer(200);
             sb.append("one");
             ShowBoardsToString(request, sb, lf, lf.getLayer());
             str = sb.toString();
             if (isBoardCannotEnterShow) {
                 try {
                     rmCache.putInGroup("menu_" + lf.getCode(), dirCache, str);
                 } catch (Exception e) {
                     logger.error("getMenuString2:" + e.getMessage());
                 }
             }
         }
         return str;
    }

    // 显示根结点为leaf的树
    public void ShowBoardsToString(HttpServletRequest request, StringBuffer sb,
                                   Leaf leaf, int rootlayer) throws
            ErrMsgException {
        // 不显示根
        if (sb.toString().equals("one")) {
            sb.delete(0, sb.length());
        } else
            ShowLeafToString(request, sb, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Privilege privilege = new Privilege();
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            if (childlf.isDisplay(request, privilege))
                ShowBoardsToString(request, sb, childlf, rootlayer);
        }
    }

    /**
     * 在position.jsp中的下拉菜单中显示版块链接,里面只能用单引号，以便于在页面脚本中写出
     * @param sb StringBuffer
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    public void ShowLeafToString(HttpServletRequest request, StringBuffer sb,
                                 Leaf leaf, int rootlayer) {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer - rootlayer;
        for (int i = 0; i < d; i++) {
            blank += "&nbsp;&nbsp;";
        }
        if (leaf.getIsHome()) {
            if (leaf.getChildCount() > 0) {
                boolean isDomain = false;
                if (leaf.getType() == Leaf.TYPE_DOMAIN) {
                    if (leaf.getParentCode().equals(Leaf.CODE_ROOT)) {
                        isDomain = true;
                    }
                }
                if (isDomain) {
                    sb.append("<a href='" + request.getContextPath() +
                              "/forum/index.jsp?boardField=" +
                              StrUtil.UrlEncode(leaf.getCode()) + "'>" + blank +
                              "╋ " + name + "</a>");
                }
                else {
                    String sName = name;
                    if (!leaf.getColor().equals(""))
                        sName = "<font color='" + leaf.getColor() + "'>" + name + "</font>";
                    if (leaf.isBold())
                        sName = "<strong>" + sName + "</strong>";
                    sb.append("<a href='" + request.getContextPath() + "/forum/" +
                              ForumPage.getListTopicPage(request, code) + "'>" +
                              blank + "├『" +
                              sName + "』</a>");
                }
            } else {
                boolean isDomain = false;
                if (leaf.getType() == Leaf.TYPE_DOMAIN) {
                    if (leaf.getParentCode().equals(Leaf.CODE_ROOT)) {
                        isDomain = true;
                    }
                }
                if (isDomain) {
                    sb.append("<a href='" + request.getContextPath() + "/forum/index.jsp?boardField=" + StrUtil.UrlEncode(leaf.getCode()) + "'>" + blank + "╋ " + name + "</a>");
                }
                else {
                    String sName = name;
                    if (!leaf.getColor().equals(""))
                        sName = "<font color='" + leaf.getColor() + "'>" + name + "</font>";
                    if (leaf.isBold())
                        sName = "<strong>" + sName + "</strong>";
                    sb.append("<a href='" + request.getContextPath() + "/forum/" +
                              ForumPage.getListTopicPage(request, code) + "'>" +
                              blank + "├『" +
                              sName +
                              "』</a>");
                }
            }
        }
    }

    public String getNameWithStyle() {
    	String sName = name;
        if (!color.equals(""))
            sName = "<font color='" + color + "'>" + name + "</font>";
        if (isBold())
            sName = "<strong>" + sName + "</strong>";
        return sName;
    }

    /**
     * 版块在前台是否显示，当前台isHome为false即隐藏时不显示，如果未被隐藏，且后台配置无权进入的版块不予显示，则根据是否有权限进入来判断是否显示
     * @param request HttpServletRequest
     * @param privilege Privilege
     * @return boolean
     */
    public boolean isDisplay(HttpServletRequest request, Privilege privilege) {
        if (isHome) {
            if (!Config.getInstance().getBooleanProperty(
                    "forum.isBoardCannotEnterShow")) {
                if (privilege.canUserDo(request, code, "enter_board")) {
                    return true;
                } else
                    return false;
            } else
                return true;
        } else
            return false;
    }

    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     * @param code String
     */
    /*    public void removeFromCache(String code) {
            try {
              // 节点如果有增加、删除、修改时，全用removeAllFromCache
                Leaf lf = getLeaf(code);
                // 更新其孩子结点缓存
                LeafChildrenCacheMgr.remove(code);

                // 更新其祖先结点的孩子结点缓存
                String pcode = lf.getParentCode();
                // while (!pcode.equals("-1")) {
                // 只需更改其父结点的孩子缓存
                if (!pcode.equals("-1")) {
                    LeafChildrenCacheMgr.remove(pcode);
                    Leaf leaf = getLeaf(pcode);
                    pcode = leaf.getParentCode();
                }

                // 删除其本身的缓存
                rmCache.remove(code, dirCache);
            } catch (Exception e) {
                logger.error("removeFromCache: " + e.getMessage());
            }
        }
     */

    public Vector getBoardsByTodayPost(int count) {
        Vector v = new Vector();
        String sql = "select code from sq_board order by today_count desc";
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            conn.setMaxRows(count);
            rs = conn.executePreQuery();
            conn.setFetchSize(count);
            Directory dir = new Directory();
            while (rs.next()) {
                Leaf lf = dir.getLeaf(rs.getString(1));
                if (lf != null)
                    v.addElement(lf);
            }
        } catch (SQLException e) {
            logger.error("getBoardsByTodayPost:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    private String logo;
    private int todayCount = 0;
    private java.util.Date todayDate;
    private int topicCount = 0;
    private int postCount = 0;
    private String theme = "default";
    private String skin = "default";
    private boolean loaded = false;
    private boolean locked;
    private String boardRule;
    private String color = "";
    private int webeditAllowType = WEBEDIT_ALLOW_TYPE_UBB_NORMAL;
    private String plugin2Code;
    private int checkMsg = 0;
    private int delMode = 0;
    private int displayStyle;
    private boolean bold = false;

}
