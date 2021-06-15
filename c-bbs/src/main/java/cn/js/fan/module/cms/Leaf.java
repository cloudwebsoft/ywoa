package cn.js.fan.module.cms;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.module.cms.template.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.db.*;
import org.apache.log4j.*;
import cn.js.fan.module.cms.ext.UserGroupPrivDb;

public class Leaf implements Serializable, ITagSupport {
    transient RMCache rmCache = RMCache.getInstance();
    String connname = "";
    transient Logger logger = Logger.getLogger(Leaf.class.getName());

    int docId;

    public static final int TYPE_LIST = 2;
    public static final int TYPE_DOCUMENT = 1;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_COLUMN = 3;

    public static final int TYPE_SUB_SITE = 4;

    public static final String ROOTCODE = "root";

    public static final String CODE_SITE = "site"; // 建站节点

    private String code = "", name = "", description = "", parent_code = "-1",
            root_code = "", add_date = "";
    private int orders = 1, layer = 1, child_count = 0, islocked = 0;
    final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,doc_id,template_id,pluginCode,price,template_doc_id,doc_count,template_catalog,logo,is_post from directory where code=?";
    boolean isHome = false;
    final String dirCache = "CMSDIR";

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
            logger.info("Directory:conname is empty");
    }

    public Leaf(String code) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:conname is empty");
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
                description = rs.getString(3);
                parent_code = rs.getString(4);
                root_code = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                child_count = rs.getInt(8);
                java.util.Date d = DateUtil.parse(rs.getString(9));
                add_date = DateUtil.format(d, "yyyy-MM-dd HH:mm:ss");
                islocked = rs.getInt(10);
                type = rs.getInt(11);
                isHome = rs.getInt(12) > 0 ? true : false;
                docId = rs.getInt(13);
                templateId = rs.getInt(14);
                pluginCode = rs.getString(15);
                price = rs.getDouble(16);
                templateDocId = rs.getInt(17);
                docCount = rs.getInt(18);
                templateCatalog = rs.getString(19);
                logo = StrUtil.getNullStr(rs.getString(20));
                post = rs.getInt(21)==1;
                loaded = true;
            }
        } catch (Exception e) {
            logger.error("loadFromDb: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public static Leaf getSubsiteOfLeaf(String dirCode) {
        Leaf plf = new Leaf();
        plf = plf.getLeaf(dirCode);
        if (plf.getType() == Leaf.TYPE_SUB_SITE) {
            return plf;
        }
        String parentCode = plf.getParentCode();
        while (!parentCode.equals(Leaf.ROOTCODE)) {
            plf = plf.getLeaf(parentCode);
            if (plf == null || !plf.isLoaded())
                break;
            if (plf.getType() == Leaf.TYPE_SUB_SITE) {
                return plf;
            }
            parentCode = plf.getParentCode();
        }
        return null;
    }

    public static boolean isLeafOfSubsite(String dirCode) {
        return getSubsiteOfLeaf(dirCode)==null?false:true;
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

    public double getPrice() {
        return price;
    }

    public int getTemplateDocId() {
        return templateDocId;
    }

    public int getDocCount() {
        return docCount;
    }

    public String getTemplateCatalog() {
        return templateCatalog;
    }

    public String getLogo() {
        return logo;
    }

    public boolean isPost() {
        return post;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector getChildren() {
        Vector v = new Vector();
        String sql =
                "select code from directory where parent_code=? order by orders asc";
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
        return "Leaf is " + name;
    }

    private int type;

    public boolean save(JdbcTemplate jt) {
        String sql = "update directory set doc_count=? where code=?";
        boolean re = false;
        try {
            re = jt.executeUpdate(sql, new Object[] {new Integer(docCount),
                                  code}) == 1;
            // System.out.println(getClass() + " save:" + docCount);
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    public synchronized boolean update() {
        String sql = "update directory set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") +
                     ",doc_id=" + docId + ",template_id=" + templateId +
                     ",orders=" + orders + ",layer=" + layer + ",child_count=" +
                     child_count + ",pluginCode=" + StrUtil.sqlstr(pluginCode) +
                     ",price=" + price +
                     ",template_doc_id=" + templateDocId + ",template_catalog=" +
                     StrUtil.sqlstr(templateCatalog) + ",logo=" + StrUtil.sqlstr(logo) + ",is_post=" + (post?1:0) +
                     " where code=" + StrUtil.sqlstr(code);
        // logger.info(sql);
        RMConn conn = new RMConn(connname);
        int r = 0;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeFromCache(code);
                    // DirListCacheMgr更新,因为isHome有可能发生变化
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
     * 更改了分类
     * @param newDirCode String
     * @return boolean
     */
    public synchronized boolean update(String newParentCode) throws
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
        String sql = "update directory set name=" + StrUtil.sqlstr(name) +
                     ",description=" + StrUtil.sqlstr(description) +
                     ",type=" + type + ",isHome=" + (isHome ? "1" : "0") +
                     ",doc_id=" + docId + ",template_id=" + templateId +
                     ",parent_code=" + StrUtil.sqlstr(newParentCode) +
                     ",orders=" + neworders + ",price=" + price +
                     ",layer=" + (parentLayer + 1) + ",template_doc_id=" +
                     templateDocId + ",template_catalog=" + StrUtil.sqlstr(templateCatalog) + ",logo=" + StrUtil.sqlstr(logo) +
                     ",is_post=" + (post?1:0) +
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
                    sql = "select code from directory where parent_code=" +
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
                    int childcount = vt.size();
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
        // 计算得出插入结点的orders
        int childorders = child_count + 1;

        String updatesql = "";
        String insertsql = "insert into directory (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date,price,isHome,pluginCode,template_doc_id,logo,is_post) values (";
        insertsql += StrUtil.sqlstr(childleaf.getCode()) + "," +
                StrUtil.sqlstr(childleaf.getName()) +
                "," + StrUtil.sqlstr(code) +
                "," + StrUtil.sqlstr(childleaf.getDescription()) + "," +
                childorders + "," + StrUtil.sqlstr(root_code) +
                ",0," + (layer + 1) + "," + childleaf.getType() +
                "," + StrUtil.sqlstr("" + System.currentTimeMillis()) + "," +
                childleaf.getPrice() + "," +
                (childleaf.getIsHome() ? "1" : "0") + "," +
                StrUtil.sqlstr(childleaf.getPluginCode()) + "," +
                childleaf.getTemplateDocId() + "," + StrUtil.sqlstr(childleaf.getLogo()) + "," + (post?1:0) +
                ")";

        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException("SQL error.");
        Conn conn = new Conn(connname);
        try {
            // 更改根结点的信息
            updatesql = "Update directory set child_count=child_count+1" +
                        " where code=" + StrUtil.sqlstr(code);
            conn.beginTrans();
            conn.executeUpdate(insertsql);
            conn.executeUpdate(updatesql);
            removeFromCache(code);
            conn.commit();

            // 加入默认权限 everyone
            LeafPriv lp = new LeafPriv();
            lp.add(childleaf.getCode());
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
            logger.error("delsingle:" + e.getMessage());
        }

        // 删除该目录下的所有权限
        LeafPriv lp = new LeafPriv(leaf.getCode());
        lp.delPrivsOfDir();

        // 删除该目录下的所有论坛用户组的权限
        UserGroupPrivDb ugpd = new UserGroupPrivDb();
        ugpd.delUserGroupPrivOfDir(leaf.getCode());

        RMConn rmconn = new RMConn(connname);
        try {
            String sql = "delete from directory where code=" +
                         StrUtil.sqlstr(leaf.getCode());
            boolean r = rmconn.executeUpdate(sql) == 1 ? true : false;
            sql = "update directory set orders=orders-1 where parent_code=" +
                  StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" +
                  leaf.getOrders();
            rmconn.executeUpdate(sql);
            sql = "update directory set child_count=child_count-1 where code=" +
                  StrUtil.sqlstr(leaf.getParentCode());
            rmconn.executeUpdate(sql);

            // removeFromCache(leaf.getCode());
            // removeFromCache(leaf.getParentCode());
            removeAllFromCache();
        } catch (SQLException e) {
            logger.error("delsingle: " + e.getMessage());
            return false;
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

                LeafChildrenCacheMgr.remove(parent_code);

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

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setTemplateDocId(int templateDocId) {
        this.templateDocId = templateDocId;
    }

    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }

    public void setTemplateCatalog(String templateCatalog) {
        this.templateCatalog = templateCatalog;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setPost(boolean post) {
        this.post = post;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    /**
     * 获取节点的默认模板套系
     * @return TemplateCatalogDb
     * @throws ErrMsgException
     */
    public TemplateCatalogDb getTemplateCatalogDb() throws ErrMsgException {
        TemplateCatalogDb tcd = new TemplateCatalogDb();
        // 如果本节点为栏目型或者子站点型、列表型、文章型
        if (type == TYPE_COLUMN || type == TYPE_SUB_SITE || type==TYPE_LIST || type==TYPE_DOCUMENT) {
            if (!templateCatalog.equals(TemplateCatalogDb.CATALOG_CODE_DEFAULT)) {
                tcd = tcd.getTemplateCatalogDb(templateCatalog);
                return tcd;
            }
        }

        // 往上遍历父节点，直到找到栏目或子站点型的节点
        String parentcode = getParentCode();
        Leaf plf = new Leaf();
        while (!parentcode.equals(Leaf.ROOTCODE)) {
            plf = plf.getLeaf(parentcode);
            if (plf == null || !plf.isLoaded())
                break;
            if (plf.getType() == Leaf.TYPE_COLUMN ||
                plf.getType() == Leaf.TYPE_SUB_SITE) {
                if (!plf.getTemplateCatalog().equals(TemplateCatalogDb.
                        CATALOG_CODE_DEFAULT)) {
                    tcd = tcd.getTemplateCatalogDb(plf.getTemplateCatalog());
                    return tcd;
                }
            }
            parentcode = plf.getParentCode();
        }

        return tcd.getDefaultTemplateCatalogDb();
    }

    /**
     * 获取节点上使用的模板
     * @return TemplateDb
     * @throws ErrMsgException
     */
    public TemplateDb getTemplateDb() throws ErrMsgException {
        TemplateCatalogDb tcd = getTemplateCatalogDb();
        TemplateDb td = new TemplateDb();
        if (getType() == Leaf.TYPE_COLUMN || getType() == Leaf.TYPE_SUB_SITE) {
            int columnId = tcd.getInt("doc_column");
            if (columnId != TemplateDb.TYPE_CODE_DEFAULT) {
                return td.getTemplateDb(columnId);
            }
        } else if (getType() == Leaf.TYPE_LIST) {
            int listId = tcd.getInt("doc_list");
            if (listId != TemplateDb.TYPE_CODE_DEFAULT) {
                return td.getTemplateDb(listId);
            }
        } else {
            int docId = tcd.getInt("doc");
            if (docId != TemplateDb.TYPE_CODE_DEFAULT) {
                return td.getTemplateDb(docId);
            }
        }

        return td.getDefaultTemplate(this);
    }

    /*
        public TemplateDb getTemplateDb() throws ErrMsgException {
            TemplateDb td2 = new TemplateDb();
            TemplateDb td = null;
            if (templateId == -1) {
                if (type==TYPE_LIST)
                    td = td2.getDefaultTemplate(TemplateDb.TYPE_CODE_LIST);
                else if (type==TYPE_COLUMN || type==TYPE_SUB_SITE)
                    td = td2.getDefaultTemplate(TemplateDb.TYPE_CODE_COLUMN);
                if (td == null)
                    throw new ErrMsgException("默认模板不存在!");

            } else {
                td = (TemplateDb) td2.getQObjectDb(new Integer(templateId));
                if (td == null)
                    throw new ErrMsgException("模板" + templateId + "不存在!");
            }
            return td;
        }
     */

    public String getListHtmlPath() {
        String path = code;
        String pCode = getParentCode();
        while (!pCode.equals(Leaf.ROOTCODE)) {
            Leaf pleaf = getLeaf(pCode);
            // 防止当parentCode出错时，陷入死循环
            if (pleaf == null || !pleaf.isLoaded())
                break;

            pCode = pleaf.getParentCode();
            path = pleaf.getCode() + "/" + path;
        }
        return "doc/root/" + path;
    }

    public String getListHtmlNameByPageNo(int pageNo) {
        Config cfg = new Config();
        return getListHtmlPath() + "/" + code + "_" + pageNo + "." +
                cfg.getProperty("cms.html_ext");
    }

    /**
     * 根据页码取得列表页的静态页面文件名
     * @param request HttpServletRequest
     * @param pageNum int
     * @return String
     */
    public String getListHtmlNameByPageNum(HttpServletRequest request,
                                           int pageNum) {
        String sql = SQLBuilder.getDirDocListSql(code);
        Document doc = new Document();
        int total = doc.getDocCount(sql);
        Config cfg = new Config();
        int pageSize = cfg.getIntProperty("cms.listPageSize");
        ListDocPagniator paginator = new ListDocPagniator(request, total,
                pageSize);

        int pageNo = paginator.pageNum2No(pageNum);

        return getListHtmlNameByPageNo(pageNo);
    }

    private int templateId = -1;
    private boolean loaded = false;
    private String pluginCode;
    private double price;
    private int templateDocId;
    private int docCount = 0;
    private String templateCatalog = TemplateCatalogDb.CATALOG_CODE_DEFAULT;
    private String logo;
    private boolean post = false;

}
