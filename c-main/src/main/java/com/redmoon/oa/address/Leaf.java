package com.redmoon.oa.address;

import java.io.*;
import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.*;

public class Leaf implements Serializable,ITagSupport {

    transient RMCache rmCache = RMCache.getInstance();
    String connname = "";

    int docId;

    public static String CODE_ROOT = "root";

    private String code = "", name = "", description = "", parent_code = PARENT_CODE_NONE,
            root_code = "", add_date = "";
    private int orders = 1, layer = 1, child_count = 0;
    final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,isHome,item_type,user_name,unit_code from address_dir where code=?";
    boolean isHome = false;
    private String unitCode;
    private String userName;

    final String dirCache = "ADDRESS_DIR";
	private int type;

    public static String PARENT_CODE_NONE = "-1";
    
	public static final String USER_NAME_PUBLIC = "public";

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
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空?");
    }

    public Leaf(String code) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空?");
        this.code = code;
        loadFromDb();
    }

    public void renew() {
        if (rmCache==null) {
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
                add_date = rs.getString(9);
                if (add_date.length()>=19)
                    add_date = add_date.substring(0, 19);
                isHome = rs.getInt(10) > 0 ? true : false;
                type = rs.getInt(11);
                userName = StrUtil.getNullStr(rs.getString(12));
                unitCode = StrUtil.getNullStr(rs.getString(13));
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadFromDb: " + e.getMessage());
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

    public boolean isLoaded() {
        return loaded;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector getChildren() {
        Vector v = new Vector();
        String sql = "select code from address_dir where parent_code=? order by orders asc";
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    String c = rs.getString(1);
                    //LogUtil.getLog(getClass()).info("child=" + c);
                    v.addElement(getLeaf(c));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getChildren: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取出code结点的所有孩子结点?
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
        return "Address leaf is " + code;
    }

    public synchronized boolean update() {
        String sql = "update address_dir set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",item_type=" + type + ",isHome=" + (isHome ? "1" : "0") +
                     ",orders=" + orders + ",layer=" + layer + ",child_count=" + child_count +
                     " where code=" + StrUtil.sqlstr(code);
        // LogUtil.getLog(getClass()).info("update:" + sql + " " + StrUtil.trace(new Throwable()));

        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeFromCache(code);
                    //LogUtil.getLog(getClass()).info("cache is removed " + code);
                    //DirListCacheMgr更新
                    LeafChildrenCacheMgr.remove(parent_code);
                }
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("update: " + e.getMessage());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("update: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    /**
     * 保存
     * @param newParentCode String
     * @return boolean
     */
    public synchronized boolean update(String newParentCode) throws ErrMsgException {
        if (newParentCode.equals(parent_code))
            return false;
        if (newParentCode.equals(code))
            throw new ErrMsgException("不能将本节点设为父节点！");

        //把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        Leaf lfparent = getLeaf(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update address_dir set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",item_type=" + type + ",isHome=" + (isHome ? "1" : "0") + 
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
                    LeafChildrenCacheMgr.remove(oldParentCode);
                    LeafChildrenCacheMgr.remove(newParentCode);

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from address_dir where parent_code=" + StrUtil.sqlstr(oldParentCode) +
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

                    //  将其新父结点的孩子数 + 1
                    Leaf newParentLeaf = getLeaf(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
                    newParentLeaf.update();
                }
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("update: " + e.getMessage());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("update: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    /**
     * 为用户创建根目录
     * @param userName String
     * @return boolean
     */
    public static boolean initRootOfUser(String userName, String unitCode) throws ErrMsgException {
        Leaf leaf = new Leaf();
        leaf.setName("全部");
        leaf.setCode("" + userName); // 使用用户名作为节点的编码，以保证根节点的唯一性?
        leaf.setParentCode(PARENT_CODE_NONE);
        leaf.setDescription(userName);
        leaf.setRootCode("" + userName);
        leaf.setOrders(1);
        leaf.setLayer(1);
        leaf.setChildCount(0);
        leaf.setUnitCode(unitCode);
        return leaf.create();
    }

    public boolean create() throws ErrMsgException {
        String insertsql = "insert into address_dir (code,name,parent_code,description,orders,root_code,child_count,layer,item_type,add_date,user_name,unit_code) values (?,?,?,?,?,?,?,?,?,?,?,?)";

        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(insertsql);
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, parent_code);
            ps.setString(4, description);
            ps.setInt(5, orders);
            ps.setString(6, root_code);
            ps.setInt(7, 0);
            ps.setInt(8, layer);
            ps.setInt(9, type);
            ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
            ps.setString(11, userName);
            ps.setString(12, unitCode);
            re = conn.executePreUpdate()==1?true:false;

            // 加入默认权限 everyone
            // LeafPriv lp = new LeafPriv();
            // lp.add(code);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean rename(String newName) {
    	// 根目录不作处理
    	setName(newName);
    	return update();
    }

    /**
     * 加入本节点的子节点
     * @param childleaf Leaf
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean AddChild(Leaf childleaf) throws
            ErrMsgException {
    	//检查文件名与同级目录是否有重复，如果有，则自动加尾数(2)，如果仍重复，则自动再加?
    	Vector v = getChildren();
    	String chName = childleaf.getName();
    	String newChName = chName;
    	Iterator ir;
    	int k = 1;
    	while (true) {
        	ir = v.iterator();
        	boolean isFound = false;
	    	while (ir.hasNext()) {
	    		Leaf lf = (Leaf)ir.next();
	    		if (lf.getName().equals(newChName)) {
	    			isFound = true;
	    			newChName = chName + "(" + k + ")";
	    			k++;
	    		}
	    	}
	    	if (!isFound)
	    		break;
    	}
    	childleaf.setName(newChName);

    	 // 计算得出插入结点的orders
        int childorders = child_count + 1;

        String updatesql = "";
        String insertsql = "insert into address_dir (code,name,parent_code,description,orders,root_code,child_count,layer,item_type,add_date) values (?,?,?,?,?,?,?,?,?,?)";
        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException("请勿输入非法字符如;号等!");
        Conn conn = new Conn(connname);
        try {
        	// 更改根结点的信息
            updatesql = "Update address_dir set child_count=child_count+1" +
                        " where code=" + StrUtil.sqlstr(code);
            conn.beginTrans();
            PreparedStatement ps = conn.prepareStatement(insertsql);
            ps.setString(1, childleaf.getCode());
            ps.setString(2, childleaf.getName());
            ps.setString(3, code);
            ps.setString(4, childleaf.getDescription());
            ps.setInt(5, childorders);
            ps.setString(6, root_code);
            ps.setInt(7, 0);
            ps.setInt(8, layer+1);
            ps.setInt(9, childleaf.getType());
            ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
            conn.executePreUpdate();
            ps.close();

            conn.executeUpdate(updatesql);
            conn.commit();

            removeFromCache(code);

        } catch (SQLException e) {
            conn.rollback();
            LogUtil.getLog(getClass()).error("AddChild: " + e.getMessage());
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
            // code对应于其父节点在LeafChildrenCacheMgr中相应的孩子节点,因其在Vector中未更新,所以childcount未得到更新,会导致出现问题
            // 故为了避免问题,应始终使用LeafChildrenCacheMgr.removeAll()
            LeafChildrenCacheMgr.remove(code);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("removeFromCache: " + e.getMessage());
        }
    }

    public void removeAllFromCache() {
        try {
            rmCache.invalidateGroup(dirCache);
            LeafChildrenCacheMgr.removeAll();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("removeAllFromCache: " + e.getMessage());
        }
    }

    public Leaf getLeaf(String code) {
        Leaf leaf = null;
        try {
            leaf = (Leaf) rmCache.getFromGroup(code, dirCache);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getLeaf: " + e.getMessage());
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
                        LogUtil.getLog(getClass()).error("getLeaf: " + e.getMessage());
                    }
                }
            }
        } else {
            leaf.renew();
        }

        return leaf;
    }

    public boolean delsingle(Leaf leaf) {
        RMConn rmconn = new RMConn(connname);
        try {
            String sql = "delete from address_dir where code=" + StrUtil.sqlstr(leaf.getCode());
            rmconn.executeUpdate(sql);
            sql = "update address_dir set orders=orders-1 where parent_code=" + StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" + leaf.getOrders();
            rmconn.executeUpdate(sql);
            sql = "update address_dir set child_count=child_count-1 where code=" + StrUtil.sqlstr(leaf.getParentCode());
            rmconn.executeUpdate(sql);

            // removeFromCache(leaf.getCode());
            // removeFromCache(leaf.getParentCode());
            removeAllFromCache();

        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delsingle: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void del(Leaf leaf) {
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
                sql = "select code from address_dir where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from address_dir where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders - 1);
            }

            ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                bleaf = getLeaf(rr.getString(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getBrother: " + e.getMessage());
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

      //如果移动方向上的兄弟结点存在则移动，否则不移动?
        if (isexist) {
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();
                if (direction.equals("down")) {
                    sql = "update address_dir set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update address_dir set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }

                if (direction.equals("up")) {
                    sql = "update address_dir set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update address_dir set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                conn.commit();
                removeFromCache(code);
                removeFromCache(bleaf.getCode());
            } catch (Exception e) {
                conn.rollback();
                LogUtil.getLog(getClass()).error("move: " + e.getMessage());
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

    public static String getAutoCode() {
        return FileUpload.getRandName();
    }

    public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	private boolean loaded = false;


}
