package com.redmoon.oa.fileark;

import java.io.*;
import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.kernel.License;

public class Leaf implements Serializable,ITagSupport {
    transient RMCache rmCache = RMCache.getInstance();
    String connname = "";

    public final static String SCRIPTS_DOWNLOAD_VALIDATE = "download_validate";
    public final static String SCRIPTS_DOWNLOAD = "download";
    public final static String SCRIPTS_ADD = "add";
    public final static String SCRIPTS_DEL = "del";

    int docId;
    
    /**
     * 事件脚本
     */
    String scripts;

    public static final int TYPE_NONE = 0;
    public static final int TYPE_DOCUMENT = 1;
    public static final int TYPE_LIST = 2;
    public static final int TYPE_LINK = 3;

    public final static String CODE_FLOW = "flow";
    public final static String CODE_PROJECT = "project";
    public final static String CODE_WIKI = "wiki";

    public final static int TEMPLATE_NONE = -1;
	public final static String FLOW_TYPE_CODE_NONE = "";

    public final static String ROOTCODE = "root";
	public final static String CODE_NONE = "-1";
    /**
     * 草稿箱
     */
	public final static String CODE_DRAFT = "draft";

	public final static int DOC_ID_NONE = 0;

    public final static String BROTHER_NEXT = "down";
    public final static String BROTHER_PRIV = "up";

    private String code = "", name = "", description = "", parent_code = CODE_NONE,
            root_code = "", add_date = "";
    private int orders = 1, layer = 1, child_count = 0, islocked = 0;
    final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,doc_id,template_id,pluginCode,IS_SYSTEM,target,is_show,is_office_ntko_show,is_log,is_fulltext,is_examine,scripts,id,flow_type_code,is_copyable from directory where code=?";
    boolean isHome = false;
    final String dirCache = "CMSDIR";
    
    private boolean show = true;
    
    private boolean examine = false;

    public String getFlowTypeCode() {
        return flowTypeCode;
    }

    public void setFlowTypeCode(String flowTypeCode) {
        this.flowTypeCode = flowTypeCode;
    }

    /**
     * 审核所用的流程类型，默认为空表示直接审核
     */
    private String flowTypeCode = FLOW_TYPE_CODE_NONE;

    public int getId() {
        return id;
    }

    private int id;

    public boolean isCopyable() {
        return copyable;
    }

    public void setCopyable(boolean copyable) {
        this.copyable = copyable;
    }

    private boolean copyable = true;

    @Override
    public String get(String field) {
        switch (field) {
            case "code":
                return getCode();
            case "name":
                return getName();
            case "desc":
                return getDescription();
            case "parent_code":
                return getParentCode();
            case "root_code":
                return getRootCode();
            case "layer":
                return "" + getLayer();
            default:
                return "";
        }
    }

    public Leaf() {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空");
        }
    }

    public Leaf(String code) {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空");
        }
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
                add_date = DateUtil.format(rs.getDate(9), "yyyy-MM-dd");
                islocked = rs.getInt(10);
                type = rs.getInt(11);
                isHome = rs.getInt(12) > 0 ? true : false;
                docId = rs.getInt(13);
                templateId = rs.getInt(14);
                pluginCode = StrUtil.getNullStr(rs.getString(15));
                system = rs.getInt(16)==1;
                target = StrUtil.getNullStr(rs.getString(17));
                show = rs.getInt(18)==1;
                OfficeNTKOShow = rs.getInt(19)==1;
                log = rs.getInt(20)==1;
                fulltext = rs.getInt(21)==1;
                examine = rs.getInt(22)==1;
                scripts = StrUtil.getNullStr(rs.getString(23));
                id = rs.getInt(24);
                flowTypeCode = StrUtil.getNullStr(rs.getString(25));
                copyable = rs.getInt(26)==1;
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

    public String getPluginCode() {
        return pluginCode;
    }

    public boolean isSystem() {
        return system;
    }

    public String getTarget() {
        return target;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector<Leaf> getChildren() {
        Vector v = new Vector();
        String sql = "select code from directory where parent_code=? order by orders asc";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
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
     * 取出code结点的所有孩子结点
     * @param vt String
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

    @Override
    public String toString() {
        return "Fileark leaf is " + code + ", name is " + name;
    }

    private int type;

    public synchronized boolean update() {
        int intIsSystem = isSystem() ? 1 : 0;
        String sql = "update directory set name=" + StrUtil.sqlstr(name) +
                ",description=" + StrUtil.sqlstr(description) +
                ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                ",orders=" + orders + ",layer=" + layer + ",child_count=" + child_count + ",pluginCode=" + StrUtil.sqlstr(pluginCode) + ",IS_SYSTEM=" + intIsSystem + ",target=" + StrUtil.sqlstr(target) +
                ",is_show=" + (show ? 1 : 0) + ",is_office_ntko_show=" + (OfficeNTKOShow ? 1 : 0) + ",is_log=" + (log ? 1 : 0) +
                ",is_fulltext=" + (fulltext ? 1 : 0) + ",is_examine=" + (examine ? 1 : 0) + ",scripts=" + StrUtil.sqlstr(scripts) +
                ",flow_type_code=" + StrUtil.sqlstr(flowTypeCode) + ", is_copyable=" + (copyable ? 1 : 0) + " where code=" + StrUtil.sqlstr(code);
        // LogUtil.getLog(getClass()).info(sql);
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
     * 更改了分类
     * @param newParentCode String
     * @return boolean
     */
    public synchronized boolean update(String newParentCode) throws ErrMsgException {
        if (newParentCode.equals(parent_code)) {
            return false;
        }
        if (newParentCode.equals(code)) {
            throw new ErrMsgException("不能将本节点设为父节点！");
        }
        int intIsSystem = isSystem()?1:0;

        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        Leaf lfparent = getLeaf(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update directory set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                     ",parent_code=" + StrUtil.sqlstr(newParentCode) + ",orders=" + neworders +
                     ",layer=" + (parentLayer+1) + ",IS_SYSTEM=" + intIsSystem + ",target=" + StrUtil.sqlstr(target) +
                     ",is_show=" + (show?1:0) + ",is_office_ntko_show=" + (OfficeNTKOShow?1:0)+",is_log="+(log?1:0) +
                    ",is_fulltext=" + (fulltext?1:0) + ",is_examine=" + (examine?1:0) + ",is_copyable=" + (copyable?1:0) + " where code=" + StrUtil.sqlstr(code);

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
                    sql = "select code from directory where parent_code=" + StrUtil.sqlstr(oldParentCode) +
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

    public boolean AddChild(Leaf childleaf) throws ErrMsgException {
        // 计算得出插入结点的orders
        int childorders = child_count + 1;

        String updatesql = "";
        int intIsSystem = childleaf.isSystem()?1:0;
        int intIsHome = childleaf.getIsHome()?1:0;
        String insertsql = "insert into directory (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date,IS_SYSTEM,isHome,target,is_show,is_office_ntko_show,is_log,is_fulltext,is_examine,is_copyable) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Conn conn = new Conn(connname);
        try {
            // 更改根结点的信息
            updatesql = "Update directory set child_count=child_count+1" + " where code=" + StrUtil.sqlstr(code);
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
            ps.setInt(11, intIsSystem);
            ps.setInt(12, intIsHome);
            ps.setString(13, childleaf.getTarget());
            ps.setInt(14, childleaf.isShow()?1:0);
            ps.setInt(15, childleaf.isOfficeNTKOShow()?1:0);
            ps.setInt(16, childleaf.isLog()?1:0);
            ps.setInt(17, childleaf.isFulltext()?1:0);
            ps.setInt(18, childleaf.isExamine()?1:0);
            ps.setInt(19, childleaf.isCopyable()?1:0);
            conn.executePreUpdate();
            ps.close();

            conn.executeUpdate(updatesql);
            removeFromCache(code);
            conn.commit();

            // 加入默认权限 everyone
            LeafPriv lp = new LeafPriv();
            lp.add(childleaf.getCode());
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
            LogUtil.getLog(getClass()).error("getLeaf1: " + e.getMessage());
        }
        if (leaf == null) {
            leaf = new Leaf(code);
            if (!leaf.isLoaded()) {
                leaf = null;
            } else {
                try {
                    rmCache.putInGroup(code, dirCache, leaf);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getLeaf2: " + e.getMessage());

                }
            }
        } else {
            leaf.renew();
        }

        return leaf;
    }

    public boolean delsingle(Leaf leaf) {
        // 删除该目录下的所有文章
        Document doc = new Document();
        try {
            doc.delDocumentByDirCode(leaf.getCode());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("delsingle:" + e.getMessage());
        }

        // 删除该目录下的所有权限
        LeafPriv lp = new LeafPriv(leaf.getCode());
        lp.delPrivsOfDir();

        RMConn rmconn = new RMConn(connname);
        try {
            String sql = "delete from directory where code=" + StrUtil.sqlstr(leaf.getCode());
            boolean r = rmconn.executeUpdate(sql) == 1 ? true : false;
            sql = "update directory set orders=orders-1 where parent_code=" + StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" + leaf.getOrders();
            rmconn.executeUpdate(sql);
            sql = "update directory set child_count=child_count-1 where code=" + StrUtil.sqlstr(leaf.getParentCode());
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
            if (direction.equals(BROTHER_NEXT)) {
                sql = "select code from directory where parent_code=" +
                      StrUtil.sqlstr(parent_code) +
                      " and orders=" + (orders + 1);
            } else {
                sql = "select code from directory where parent_code=" +
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

        //如果移动方向上的兄弟结点存在则移动，否则不移动
        if (isexist) {
            Conn conn = new Conn(connname);
            try {
                conn.beginTrans();
                if (direction.equals("down")) {
                    sql = "update directory set orders=orders+1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update directory set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }

                if (direction.equals("up")) {
                    sql = "update directory set orders=orders-1" +
                          " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update directory set orders=orders+1" +
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
    
    
    public String getFilePath() {
		// 取得文件虚拟路径
		String parentcode = getParentCode();
		Leaf plf = new Leaf();
		String filePath = getName();
		if (!parentcode.equals("-1")) {
			// 非根目录取节点名称
			filePath = getName();
			while (!parentcode.equals(ROOTCODE)) {
				plf = plf.getLeaf(parentcode);
				if (plf==null)
					break;
				parentcode = plf.getParentCode();
				filePath = plf.getName() + "/" + filePath;
			}
			// filePath = getRootCode() + "/" + filePath;
		}
		else
			filePath = "";

		return filePath;
    }
    
    public Vector getTreeChildren(String parentCode) {
        Vector v = new Vector();
        String sql = "select code from directory where parent_code=? order by orders asc";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, parentCode);
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

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    public void setShow(boolean show) {
		this.show = show;
	}

	public boolean isShow() {
		return show;
	}

	public void setOfficeNTKOShow(boolean OfficeNTKOShow) {
		this.OfficeNTKOShow = OfficeNTKOShow;
	}

	public boolean isOfficeNTKOShow() {
		return OfficeNTKOShow;
	}
	
	/**
	 * 是否记录日志
	 */
	private boolean log = false;
	private int templateId = -1;
    private boolean loaded = false;
    private String pluginCode;
    private boolean system = false;
    private String target;
    
    private boolean OfficeNTKOShow = false;
    
    private boolean fulltext = false;

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	public void setFulltext(boolean fulltext) {
		this.fulltext = fulltext;
	}

	public String getScripts() {
		return scripts;
	}

	public void setScripts(String scripts) {
		this.scripts = scripts;
	}

	public boolean isFulltext() {
		return fulltext;
	}

	public void setExamine(boolean examine) {
		this.examine = examine;
	}

	public boolean isExamine() {
		return examine;
	}
	
	public String getScript(String eventType) {
		String beginStr = "//[" + eventType + "_begin]\r\n";

		int b = scripts.indexOf(beginStr);
		if (b == -1) {
			beginStr = "//[" + eventType + "_begin]\n";
			b = scripts.indexOf(beginStr);
		}
		if (b != -1) {
			String endStr = "//[" + eventType + "_end]\r\n";
			int e = scripts.indexOf(endStr);
			if (e == -1) {
				endStr = "//[" + eventType + "_end]\n";
				e = scripts.indexOf(endStr);
			}
			if (e != -1) {
				return scripts.substring(b + beginStr.length(), e);
			}
		}
		return null;
	}	
	
	public boolean saveScript(String eventType, String script) throws ErrMsgException, ResKeyException {
		if (!License.getInstance().isSrc()) {
			throw new ErrMsgException("开发版才有脚本编写功能！");
		}
	    
		// 检查
		if (eventType.equals(SCRIPTS_DOWNLOAD_VALIDATE)) {
			if (!script.equals("") && script.indexOf("ret") == -1) {
				throw new ErrMsgException("请添加返回值，ret=true或者false");
			}
		}

		String beginStr = "//[" + eventType + "_begin]\r\n";
		String endStr = "//[" + eventType + "_end]\r\n";

		int b = scripts.indexOf(beginStr);
		int e = scripts.indexOf(endStr);
		if (b == -1 || e == -1) {
			scripts += "\r\n" + beginStr;
			scripts += script;
			scripts += "\r\n" + endStr;
		} else {
			String str = scripts.substring(0, b);
			str += "\r\n" + beginStr;
			str += script;
			str += "\r\n" + scripts.substring(e);

			scripts = str;
		}
		return update();
	}
	
}
