package com.redmoon.oa.basic;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.menu.PresetLeaf;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;

public class TreeSelectDb extends ObjectDb implements Serializable {
	
	private String code = "", name = "", description = "", parentCode = "-1",
            rootCode = "";
    private int orders = 1, layer = 1, childCount = 0;
    boolean isHome = false;
    private boolean loaded = false;
    private java.util.Date addDate;
    
    private String link;
    private String preCode;
    private String formCode;
    
    public static final String PRE_CODE_FLOW = "flow";
    public static final String PRE_CODE_MODULE = "module";

    public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPreCode() {
		return preCode;
	}

	public void setPreCode(String preCode) {
		this.preCode = preCode;
	}

	public String getFormCode() {
		return formCode;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public TreeSelectDb() {
        init();
    }

    public TreeSelectDb(String code) {
        this.code = code;
        init();
        load();
    }
    
    public boolean init(String rootCode, String rootName) {
    	TreeSelectDb tsd = getTreeSelectDb(rootCode);
    	if (tsd!=null && tsd.isLoaded())
    		return true;
        int child_count = 0, orders = 1;
        String parent_code = "-1";

        String insertsql = "insert into oa_tree_select (code,name,parentCode,description,orders,rootCode,childCount,layer, link, pre_code, form_code) values (";
        insertsql += StrUtil.sqlstr(rootCode) + "," + StrUtil.sqlstr(rootName) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr(description) + "," +
                orders + "," + StrUtil.sqlstr(rootCode) + "," +
                child_count + ",1, " + StrUtil.sqlstr(link) + "," + StrUtil.sqlstr(preCode) + "," + StrUtil.sqlstr(formCode) + ")";

        int r = 0;
        JdbcTemplate jt = new JdbcTemplate();
        try {
            r = jt.executeUpdate(insertsql);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return r==1;
    }

    @Override
	public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new TreeSelectDb(pk.getStrValue());
    }

    @Override
	public void setQueryCreate() {

    }

    @Override
	public void setQuerySave() {
        this.QUERY_SAVE = "update oa_tree_select set name=?,description=?,type=?,orders=?,childCount=?,layer=?, link=?, pre_code=?, form_code=? where code=?";
    }

    @Override
	public void setQueryDel() {

    }

    @Override
	public void setQueryLoad() {
        this.QUERY_LOAD = "select code,name,description,parentCode,rootCode,orders,layer,childCount,addDate,type, link, pre_code, form_code from oa_tree_select where code=?";
    }

    @Override
	public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                code = rs.getString(1);
                name = rs.getString(2);
                description = StrUtil.getNullStr(rs.getString(3));
                parentCode = rs.getString(4);
                rootCode = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                childCount = rs.getInt(8);
                addDate = rs.getDate(9);
                type = rs.getInt(10);
                link = StrUtil.getNullStr(rs.getString(11));
                preCode = StrUtil.getNullStr(rs.getString(12));
                formCode = StrUtil.getNullStr(rs.getString(13));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
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
        this.rootCode = c;
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
        this.parentCode = p;
    }

    public String getParentCode() {
        return this.parentCode;
    }

    public void setIsHome(boolean b) {
        this.isHome = b;
    }

    @Override
	public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getRootCode() {
        return rootCode;
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

    public int getChildCount() {
        return childCount;
    }

    @Override
	public boolean isLoaded() {
        return loaded;
    }

    public Vector getChildren() {
        Vector v = new Vector();
        String sql = "select code from oa_tree_select where parentCode=? order by orders";
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
                    v.addElement(getTreeSelectDb(c));
                }
            }
        } catch (SQLException e) {
            logger.error("getChildren:" + e.getMessage());
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
    public Vector getAllChild(Vector vt, TreeSelectDb leaf) throws ErrMsgException {
        Vector children = leaf.getChildren();
        if (children.isEmpty())
            return children;
        vt.addAll(children);
        Iterator ir = children.iterator();
        while (ir.hasNext()) {
        	TreeSelectDb lf = (TreeSelectDb) ir.next();
            getAllChild(vt, lf);
        }
        return vt;
    }

    @Override
	public String toString() {
        return "TreeSelect leaf is " + code;
    }

    private int type;

    @Override
	public synchronized boolean save() {
        Conn conn = new Conn(connname);
        int r = 0;
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, type);
            ps.setInt(4, orders);
            ps.setInt(5, childCount);
            ps.setInt(6, layer);
            
            ps.setString(7, link);
            ps.setString(8, preCode);
            ps.setString(9, formCode);
            
            ps.setString(10, code);

            r = conn.executePreUpdate();
            try {
                if (r == 1) {
                    re = true;
                    TreeSelectCache dcm = new TreeSelectCache();
                    dcm.refreshSave(code, parentCode);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 更改了分类
     * @param newDirCode String
     * @return boolean
     */
    public synchronized boolean save(String newParentCode) {
        if (newParentCode.equals(parentCode))
            return false;
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        TreeSelectDb lfparent = getTreeSelectDb(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update oa_tree_select set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type +
                     ",parentCode=" + StrUtil.sqlstr(newParentCode) + ",orders=" + neworders +
                     ",layer=" + (parentLayer+1) +
                     ",link=" + StrUtil.sqlstr(link) + ", pre_code=" + StrUtil.sqlstr(preCode) + ", form_code=" + StrUtil.sqlstr(formCode) +
                     " where code=" + StrUtil.sqlstr(code);

        String oldParentCode = parentCode;
        parentCode = newParentCode;
        TreeSelectCache dcm = new TreeSelectCache();

        int r = 0;

        RMConn conn = new RMConn(connname);
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    dcm.refreshSave(code, parentCode);

                    dcm.removeFromCache(newParentCode);
                    dcm.removeFromCache(oldParentCode);
                    // DirListCacheMgr更新
                    TreeSelectChildrenCache.remove(oldParentCode);
                    TreeSelectChildrenCache.remove(newParentCode);

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from oa_tree_select where parentCode=" + StrUtil.sqlstr(oldParentCode) +
                          " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord)ri.next();
                        TreeSelectDb dd = getTreeSelectDb(rr.getString(1));
                        dd.setOrders(dd.getOrders() - 1);
                        dd.save();
                    }

                    // 更新其所有子结点的layer
                    Vector vt = new Vector();
                    getAllChild(vt, this);
                    // int childcount = vt.size();
                    Iterator ir = vt.iterator();
                    while (ir.hasNext()) {
                        TreeSelectDb childlf = (TreeSelectDb)ir.next();
                        int layer = parentLayer + 1 + 1;
                        String pcode = childlf.getParentCode();
                        while (!pcode.equals(code)) {
                            layer ++;
                            TreeSelectDb lfp = getTreeSelectDb(pcode);
                            pcode = lfp.getParentCode();
                        }

                        childlf.setLayer(layer);
                        childlf.save();
                    }


                    // 将其原来的父结点的孩子数-1
                    TreeSelectDb oldParentLeaf = getTreeSelectDb(oldParentCode);
                    oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
                    oldParentLeaf.save();

                    // 将其新父结点的孩子数 + 1
                    TreeSelectDb newParentLeaf = getTreeSelectDb(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
                    newParentLeaf.save();

                    logger.info("save: newParentLeaf.childcount=" + newParentLeaf.getChildCount() + " oldParentLeaf.childcount=" + oldParentLeaf.getChildCount() + " oldparentCode=" + oldParentCode + " newParentCode=" + newParentCode + " neworders=" + neworders);

                }
            } catch (Exception e) {
                logger.error("save1: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("save2: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            dcm.removeFromCache(code);
        }
        return re;
    }
    
    /**
     * 更改了分类
     * @param newDirCode String
     * @return boolean
     */
    public synchronized boolean save(String newParentCode, int newOrder) {
        if (newParentCode.equals(parentCode))
            return false;
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        TreeSelectDb lfparent = getTreeSelectDb(newParentCode);
        int oldorders = orders;
        int parentLayer = lfparent.getLayer();
        String sql = "update oa_tree_select set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type +
                     ",parentCode=" + StrUtil.sqlstr(newParentCode) + ",orders=" + newOrder +
                     ",layer=" + (parentLayer+1) +
                     ",link=" + StrUtil.sqlstr(link) + ", pre_code=" + StrUtil.sqlstr(preCode) + ", form_code=" + StrUtil.sqlstr(formCode) +                     
                     " where code=" + StrUtil.sqlstr(code);

        String oldParentCode = parentCode;
        parentCode = newParentCode;
        TreeSelectCache dcm = new TreeSelectCache();

        int r = 0;

        RMConn conn = new RMConn(connname);
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    dcm.refreshSave(code, parentCode);

                    dcm.removeFromCache(newParentCode);
                    dcm.removeFromCache(oldParentCode);
                    // DirListCacheMgr更新
                    TreeSelectChildrenCache.remove(oldParentCode);
                    TreeSelectChildrenCache.remove(newParentCode);

                    // 更新原来父结点中，位于本leaf之后的orders
                    sql = "select code from oa_tree_select where parentCode=" + StrUtil.sqlstr(oldParentCode) +
                          " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord)ri.next();
                        TreeSelectDb dd = getTreeSelectDb(rr.getString(1));
                        dd.setOrders(dd.getOrders() - 1);
                        dd.save();
                    }

                    // 更新其所有子结点的layer
                    Vector vt = new Vector();
                    getAllChild(vt, this);
                    // int childcount = vt.size();
                    Iterator ir = vt.iterator();
                    while (ir.hasNext()) {
                        TreeSelectDb childlf = (TreeSelectDb)ir.next();
                        int layer = parentLayer + 1 + 1;
                        String pcode = childlf.getParentCode();
                        while (!pcode.equals(code)) {
                            layer ++;
                            TreeSelectDb lfp = getTreeSelectDb(pcode);
                            pcode = lfp.getParentCode();
                        }

                        childlf.setLayer(layer);
                        childlf.save();
                    }
                    
                 // 更新当前父结点中，位于本leaf之后的orders
                    sql = "select code from oa_tree_select where parentCode=" + StrUtil.sqlstr(newParentCode) +
                          " and orders>=" + newOrder;
                    ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord)ri.next();
                        TreeSelectDb dd = getTreeSelectDb(rr.getString(1));
                        dd.setOrders(dd.getOrders() + 1);
                        dd.save();
                    }


                    // 将其原来的父结点的孩子数-1
                    TreeSelectDb oldParentLeaf = getTreeSelectDb(oldParentCode);
                    oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
                    oldParentLeaf.save();

                    // 将其新父结点的孩子数 + 1
                    TreeSelectDb newParentLeaf = getTreeSelectDb(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
                    newParentLeaf.save();

                    logger.info("save: newParentLeaf.childcount=" + newParentLeaf.getChildCount() + " oldParentLeaf.childcount=" + oldParentLeaf.getChildCount() + " oldparentCode=" + oldParentCode + " newParentCode=" + newParentCode + " neworders=" + newOrder);

                }
            } catch (Exception e) {
                logger.error("save1: " + e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("save2: " + e.getMessage());
        }
        boolean re = r == 1 ? true : false;
        if (re) {
            dcm.removeFromCache(code);
        }
        return re;
    }
    
    public boolean AddChild(TreeSelectDb childleaf) throws
            ErrMsgException {
        // 计算得出插入结点的orders
        int childorders = childCount + 1;

        String updatesql = "";

        String insertsql = "insert into oa_tree_select (code,name,parentCode,description,orders,rootCode,childCount,layer,type,addDate,link,pre_code,form_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException("请勿输入非法字符如;号等！");
        Conn conn = new Conn(connname);
        try {
            // 更改根结点的信息
            updatesql = "Update oa_tree_select set childCount=childCount+1" +
                        " where code=" + StrUtil.sqlstr(code);
            conn.beginTrans();
            PreparedStatement ps = conn.prepareStatement(insertsql);
            ps.setString(1, childleaf.getCode());
            ps.setString(2, childleaf.getName());
            ps.setString(3, code);
            ps.setString(4, childleaf.getDescription());
            ps.setInt(5, childorders);
            ps.setString(6, rootCode);
            ps.setInt(7, 0);
            ps.setInt(8, layer + 1);
            ps.setInt(9, childleaf.getType());
            ps.setDate(10, new java.sql.Date(new java.util.Date().getTime()));
            ps.setString(11, childleaf.getLink());
            ps.setString(12, childleaf.getPreCode());
            ps.setString(13, childleaf.getFormCode());
            conn.executePreUpdate();
            if (ps!=null) {
                ps.close();
                ps = null;
            }

            conn.executeUpdate(updatesql);
            conn.commit();
            
            TreeSelectCache dcm = new TreeSelectCache();
            dcm.refreshAddChild(code);
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new ErrMsgException("请检查编码是否有重复！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return true;
    }

    public TreeSelectDb getTreeSelectDb(String code) {
        TreeSelectCache dcm = new TreeSelectCache();
        return dcm.getTreeSelectDb(code);
    }
    
    /**
     * 根据名称取到值
     * @param name
     * @return
     */
    public TreeSelectDb getTreeSelectDbByName(String rootCode, String name) {
        String sql = "select code from oa_tree_select where rootCode=? and name=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ir;
		try {
			ir = jt.executeQuery(sql, new Object[]{rootCode, name});
	        if (ir.hasNext()) {
	        	ResultRecord rr = (ResultRecord)ir.next();
	        	return getTreeSelectDb(rr.getString(1));
	        }			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return null;
    }

    public boolean delsingle(TreeSelectDb leaf) {
    	boolean re = false;
        String sql = "delete from oa_tree_select where code=" + StrUtil.sqlstr(leaf.getCode());
        RMConn rmconn = new RMConn(connname);
        try {
            re = rmconn.executeUpdate(sql) == 1 ? true : false;
            sql = "update oa_tree_select set orders=orders-1 where parentCode=" + StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" + leaf.getOrders();
            rmconn.executeUpdate(sql);
            sql = "update oa_tree_select set childCount=childCount-1 where code=" + StrUtil.sqlstr(leaf.getParentCode());
            rmconn.executeUpdate(sql);
            TreeSelectCache dcm = new TreeSelectCache();
            dcm.refreshDel(leaf.getCode(), leaf.getParentCode());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
        finally {
            TreeSelectCache dcm = new TreeSelectCache();
            dcm.refreshDel(leaf.getCode(), leaf.getParentCode());
        }
        return re;
    }

    public synchronized void del(TreeSelectDb leaf) {
        delsingle(leaf);
        Iterator children = leaf.getChildren().iterator();
        while (children.hasNext()) {
            TreeSelectDb lf = (TreeSelectDb) children.next();
            del(lf);
        }
    }

    @Override
	public boolean del() {
        del(this);
        return true;
    }

    public TreeSelectDb getBrother(String direction) {
        String sql;
        RMConn rmconn = new RMConn(connname);
        TreeSelectDb bleaf = null;
        try {
            if (direction.equals("down")) {
                sql = "select code from oa_tree_select where parentCode=" +
                      StrUtil.sqlstr(parentCode) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from oa_tree_select where parentCode=" +
                      StrUtil.sqlstr(parentCode) +
                      " and orders=" + (orders - 1);
            }

            ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                bleaf = getTreeSelectDb(rr.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return bleaf;
    }

    public boolean move(String direction) {
        String sql = "";

        // 取出该结点的移动方向上的下一个兄弟结点的orders
        boolean isexist = false;

        TreeSelectDb bleaf = getBrother(direction);
        if (bleaf != null) {
            isexist = true;
        }

        //如果移动方向上的兄弟结点存在则移动，否则不移动
        if (isexist) {
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();
                if (direction.equals("down")) {
                    sql = "update oa_tree_select set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update oa_tree_select set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }

                if (direction.equals("up")) {
                    sql = "update oa_tree_select set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update oa_tree_select set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                conn.commit();
                TreeSelectCache dcm = new TreeSelectCache();
                dcm.refreshMove(code, bleaf.getCode());
            } catch (Exception e) {
                conn.rollback();
                logger.error(e.getMessage());
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

    @Override
	public void setQueryList() {
        this.QUERY_LIST = "select code from oa_tree_select";
    }

    @Override
	public void setPrimaryKey() {
        primaryKey = new PrimaryKey("code", PrimaryKey.TYPE_STRING);
    }

    @Override
	public ObjectDb getObjectDb(Object objKey) {
        return getTreeSelectDb(objKey.toString());
    }

    /**
     * 只是单纯为了实现纯虚函数
     * @param sql String
     * @return int
     */
    @Override
	public int getObjectCount(String sql) {
        return 0;
    }

    /**
     * 只为实现纯虚函数
     * @param query String
     * @param startIndex int
     * @return Object[]
     */
    @Override
	public Object[] getObjectBlock(String query, int startIndex) {
        return null;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
    
    public java.util.Date getAddDate() {
    	return addDate;
    }
    
    public String getColor() {
    	return description;
    }
    
    private boolean isShow;

	public boolean isShow() {
		return isShow;
	}

	public void setShow(boolean isShow) {
		this.isShow = isShow;
	}
    
	/**
	 * 取得链接
	 * @param request
	 * @return
	 */
    public String getLink(HttpServletRequest request) {
        if (preCode.equals(PRE_CODE_MODULE)) {
            // ModulePrivDb mpd = new ModulePrivDb();
            return "visual/module_list.jsp?code=" + StrUtil.UrlEncode(formCode);
        }
        else if (preCode.equals(PRE_CODE_FLOW)) {
        	return "flow_initiate1.jsp?op=" + StrUtil.UrlEncode(formCode);
        }
        else {
        	if (link.indexOf("$")!=-1) {
        		UserSetupDb usd = new UserSetupDb();
        		Privilege pvg = new Privilege();
        		usd = usd.getUserSetupDb(pvg.getUser(request));
        		String lk = link;
        		try {
					lk = lk.replaceFirst("\\$userName", java.net.URLEncoder.encode(pvg.getUser(request),"GBK"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		return lk;
        	}
        	
            // 链接需替换路径变量$u
            return link.replaceFirst("\\$u", request.getContextPath());
        }
    }
}
