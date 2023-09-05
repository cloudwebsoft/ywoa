package com.redmoon.oa.flow;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;

import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.kernel.JobUnitDb;
import com.redmoon.oa.ui.LocalUtil;

public class Leaf implements Serializable, ITagSupport {
    transient RMCache rmCache = RMCache.getInstance();
    String connname = "";

    int docId;

    public static final String CODE_ROOT = "root";
    public static final String CODE_PERFORMANCE = "performance";

    public static final int TYPE_LIST = 2; // 流程
    public static final int TYPE_FREE = 1; // 自由流程
    public static final int TYPE_NONE = 0; // 分类

    public static final String UNIT_CODE_PUBLIC = "-1";

    public static final String PLUGIN_NONE = "default";
    /**
     * 公文型
     */
    public static final String PLUGIN_PAPER = "paper";

    private String code = "", name = "", description = "", parent_code = "-1",
            root_code = "";
    private int orders = 1, layer = 1, child_count = 0;
    final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,doc_id,template_id,pluginCode,formCode,dept,is_open,unit_code,is_debug,is_mobile_start,is_mobile_location,is_mobile_camera,query_id,query_role,query_cond_map,col_props,cond_props,params,export_col_props,icon from flow_directory where code=?";
    boolean isHome = false;
    final String dirCache = "FLOW_DIR";

    private boolean open = true;

    private String unitCode;

    public static final long QUERY_NONE = 0;

    private long queryId = QUERY_NONE;
    private String queryRole;
    private String queryCondMap;

    /**
     * 列表显示设置
     */
    private String colProps;

    /**
     * 列表中的查询条件设置
     */
    private String condProps;

    public String getExportColProps() {
        return exportColProps;
    }

    public void setExportColProps(String exportColProps) {
        this.exportColProps = exportColProps;
    }

    /**
     * 导出表头中的字段设置
     */
    private String exportColProps;

    /**
     * 用来缓存各个流程列表中的显示设置
     */
    private static Map mapColProps = new HashMap();

    public String getColProps() {
        return colProps;
    }

    public void setColProps(String colProps) {
        this.colProps = colProps;
    }

    public String getCondProps() {
        return condProps;
    }

    public void setCondProps(String condProps) {
        this.condProps = condProps;
    }

    public static String getFlowColTitle(String field) {
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        switch (field) {
            case "f.id":
                return "ID";
            case "f.flow_level":
                return i18nUtil.get("rating");
            case "f.title":
                return i18nUtil.get("tit");
            case "f.type_code":
                return i18nUtil.get("type");
            case "f.userName":
                return i18nUtil.get("organ");
            case "f.begin_date":
                return i18nUtil.get("startTime");
            case "f.finallyApply":
                return i18nUtil.get("finallyApply");
            case "f.currentHandle":
                return i18nUtil.get("currentHandle");
            case "f.remainTime":
                return i18nUtil.get("remainTime");
            case "f.status":
                return i18nUtil.get("state");
            case "f.end_date":
                return i18nUtil.get("endTime");
        }
        return "";
    }

    /**
     * 默认搜索结果列表所显示的列
     *
     * @param request
     * @return
     */
    public static com.alibaba.fastjson.JSONArray getDefaultColProps(HttpServletRequest request, String typeCode, int displayMode) {
        com.alibaba.fastjson.JSONArray aryColProps = null;
        if ("".equals(typeCode)) {
            typeCode = Leaf.CODE_ROOT;
            Leaf lf = new Leaf();
            lf = lf.getLeaf(typeCode);
            String colProps = lf.getColProps();
            if (!StrUtil.isEmpty(colProps)) {
                aryColProps = com.alibaba.fastjson.JSONArray.parseArray(colProps);
                if (aryColProps == null) {
                    DebugUtil.e(Leaf.class, "json格式非法：" , colProps);
                }
            }
        }
        if (aryColProps == null) {
            aryColProps = new com.alibaba.fastjson.JSONArray();

            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.id"));
            json.put("field", "f.id");
            json.put("width", 65);
            json.put("sort", true);
            json.put("align", "center");
            json.put("hide", false);
            aryColProps.add(json);

            com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
            if (cfg.getBooleanProperty("isFlowLevelDisplay")) {
                json = new com.alibaba.fastjson.JSONObject();
                json.put("title", getFlowColTitle("f.flow_level"));
                json.put("field", "f.flow_level");
                json.put("width", 80);
                json.put("sort", true);
                json.put("align", "center");
                json.put("hide", false);
                aryColProps.add(json);
            }

            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.title"));
            json.put("field", "f.title");
            json.put("width", 350);
            json.put("sort", true);
            json.put("align", "left");
            json.put("hide", false);
            aryColProps.add(json);

            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.type_code"));
            json.put("field", "f.type_code");
            json.put("width", 150);
            json.put("sort", true);
            json.put("align", "center");
            json.put("hide", false);
            aryColProps.add(json);

            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.userName"));
            json.put("field", "f.userName");
            json.put("width", 100);
            json.put("sort", true);
            json.put("align", "center");
            json.put("hide", false);
            aryColProps.add(json);

            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.begin_date"));
            json.put("field", "f.begin_date");
            json.put("width", 105);
            json.put("sort", true);
            json.put("align", "center");
            json.put("hide", false);
            aryColProps.add(json);

            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.finallyApply"));
            json.put("field", "f.finallyApply");
            json.put("width", 100);
            json.put("sort", false);
            json.put("align", "center");
            json.put("hide", false);
            aryColProps.add(json);

            if (displayMode != WorkflowMgr.DISPLAY_MODE_DOING) {
                json = new com.alibaba.fastjson.JSONObject();
                json.put("title", getFlowColTitle("f.currentHandle"));
                json.put("field", "f.currentHandle");
                json.put("width", 100);
                json.put("sort", false);
                json.put("align", "center");
                json.put("hide", false);
                aryColProps.add(json);
            } else {
                json = new com.alibaba.fastjson.JSONObject();
                json.put("title", getFlowColTitle("f.remainTime"));
                json.put("field", "f.remainTime");
                json.put("width", 100);
                json.put("sort", false);
                json.put("align", "center");
                json.put("hide", false);
                aryColProps.add(json);
            }

            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.status"));
            json.put("field", "f.status");
            json.put("width", 80);
            json.put("sort", true);
            json.put("align", "center");
            json.put("hide", false);
            aryColProps.add(json);

            json = new com.alibaba.fastjson.JSONObject();
            json.put("title", getFlowColTitle("f.end_date"));
            json.put("field", "f.end_date");
            json.put("width", 100);
            json.put("sort", true);
            json.put("align", "center");
            json.put("hide", true);
            aryColProps.add(json);

            // 加入所有字段
            /*Leaf lf = new Leaf();
            lf = lf.getLeaf(typeCode);
            String formCode = lf.getFormCode();
            if (!"".equals(formCode)) {
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                for (FormField ff : fd.getFields()) {
                    json = new com.alibaba.fastjson.JSONObject();
                    json.put("title", ff.getTitle());
                    json.put("field", ff.getName());
                    json.put("width", 100);
                    json.put("sort", true);
                    json.put("align", "center");
                    json.put("hide", true);
                    aryColProps.add(json);
                }
            }*/

            /*json = new com.alibaba.fastjson.JSONObject();
            json.put("title", "操作");
            json.put("field", "operate");
            json.put("width", 150);
            json.put("sort", false);
            json.put("align", "center");
            json.put("hide", false);
            json.put("fixed", "right");
            aryColProps.add(json);*/
        }
        return aryColProps;
    }

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
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空");
        }
    }

    public Leaf(String code) {
        connname = Global.getDefaultDB();
        if ("".equals(connname)) {
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空");
        }
        this.code = code;
        loadFromDb();
    }

    public void renew() {
        if (rmCache == null) {
            rmCache = RMCache.getInstance();
        }
    }

    public void loadFromDb() {
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(LOAD);
            ps.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                code = rs.getString(1);
                name = rs.getString(2);
                description = rs.getString(3);
                parent_code = rs.getString(4);
                root_code = rs.getString(5);
                orders = rs.getInt(6);
                layer = rs.getInt(7);
                child_count = rs.getInt(8);
                /*add_date = DateUtil.format(rs.getTimestamp(9), "yyyy-MM-dd HH:mm:ss");
                islocked = rs.getInt(10);*/
                type = rs.getInt(11);
                isHome = rs.getInt(12) > 0;
                docId = rs.getInt(13);
                templateId = rs.getInt(14);
                pluginCode = rs.getString(15);
                formCode = StrUtil.getNullString(rs.getString(16));
                dept = StrUtil.getNullString(rs.getString(17));
                open = rs.getInt(18) == 1;
                unitCode = rs.getString(19);
                debug = rs.getInt(20) == 1;
                mobileStart = rs.getInt(21) == 1;
                mobileLocation = rs.getInt(22) == 1;
                mobileCamera = rs.getInt(23) == 1;
                queryId = rs.getLong(24);
                queryRole = StrUtil.getNullStr(rs.getString(25));
                queryCondMap = StrUtil.getNullStr(rs.getString(26));
                colProps = StrUtil.getNullStr(rs.getString(27));
                condProps = StrUtil.getNullStr(rs.getString(28));
                params = StrUtil.getNullStr(rs.getString(29));
                exportColProps = StrUtil.getNullStr(rs.getString(30));
                icon = StrUtil.getNullStr(rs.getString(31));
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
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

    public String getFormCode() {
        return formCode;
    }

    public String getDept() {
        return dept;
    }

    public int getChildCount() {
        return child_count;
    }

    public Vector<Leaf> getChildren() {
        Vector<Leaf> v = new Vector<>();
        String sql = "select code from flow_directory where parent_code=? order by orders asc";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            rs = conn.executePreQuery();
            while (rs.next()) {
                String c = rs.getString(1);
                v.addElement(getLeaf(c));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getChildren: " + e.getMessage());
        } finally {
            conn.close();
        }
        return v;
    }

    /**
     * 取得单位为unitCode的孩子节点或者公共节点(unitCode=-1)
     *
     * @param unitCode
     * @return
     */
    public Vector<Leaf> getChildren(String unitCode) {
        Vector<Leaf> v = new Vector<>();
        String sql = "select code from flow_directory where parent_code=? and (unit_code=? or unit_code='-1') order by orders asc";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            pstmt.setString(2, unitCode);
            rs = conn.executePreQuery();
            while (rs.next()) {
                String c = rs.getString(1);
                v.addElement(getLeaf(c));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;
    }

    /**
     * 取出code结点的所有孩子结点
     *
     * @return ResultIterator
     */
    public Vector<Leaf> getAllChild(Vector<Leaf> vt, Leaf leaf) throws ErrMsgException {
        Vector<Leaf> children = leaf.getChildren();
        if (children.isEmpty()) {
            return children;
        }
        vt.addAll(children);
        for (Leaf lf : children) {
            getAllChild(vt, lf);
        }
        return vt;
    }

    /**
     * 取出code结点的所有单位为unitCode的孩子结点
     *
     * @return ResultIterator
     */
    public Vector<Leaf> getAllChildOfUnit(Vector<Leaf> vt, Leaf leaf, String unitCode) throws ErrMsgException {
        Vector<Leaf> children = leaf.getChildren(unitCode);
        if (children.isEmpty()) {
            return children;
        }
        vt.addAll(children);
        for (Leaf lf : children) {
            getAllChildOfUnit(vt, lf, unitCode);
        }
        return vt;
    }

    @Override
    public String toString() {
        return "Flow leaf is " + code + " " + name;
    }

    private int type;

    public synchronized boolean update() {
        String sql = "update flow_directory set name=" + StrUtil.sqlstr(name) +
                ",description=" + StrUtil.sqlstr(description) +
                ",parent_code=" + StrUtil.sqlstr(parent_code) +
                ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                ",orders=" + orders + ",layer=" + layer + ",child_count=" + child_count + ",pluginCode=" + StrUtil.sqlstr(pluginCode) +
                ",formCode=" + StrUtil.sqlstr(formCode) + ",dept=" + StrUtil.sqlstr(dept) + ",is_open=" + (open ? 1 : 0) + ",unit_code=" + StrUtil.sqlstr(unitCode) + ",is_debug=" + (debug ? 1 : 0) +
                ",is_mobile_start=" + (mobileStart ? 1 : 0) +
                ",is_mobile_location=" + (mobileLocation ? 1 : 0) +
                ",is_mobile_camera=" + (mobileCamera ? 1 : 0) +
                ",query_id=" + queryId +
                ",query_role=" + StrUtil.sqlstr(queryRole) +
                ",query_cond_map=" + StrUtil.sqlstr(queryCondMap) +
                ",col_props=" + StrUtil.sqlstr(colProps) +
                ",cond_props=" + StrUtil.sqlstr(condProps) +
                ",params=" + StrUtil.sqlstr(params) +
                ",export_col_props=" + StrUtil.sqlstr(exportColProps) +
                ",icon=" + StrUtil.sqlstr(icon) +
                " where code=" + StrUtil.sqlstr(code);
        RMConn conn = new RMConn(connname);
        int r = -1;
        try {
            r = conn.executeUpdate(sql);
            try {
                if (r == 1) {
                    removeFromCache(code);
                    LeafChildrenCacheMgr.remove(parent_code);
                }
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        boolean re = r > 0;
        if (re) {
            removeFromCache(code);
        }
        return re;
    }

    /**
     * 更改了分类
     *
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
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        Leaf lfparent = getLeaf(newParentCode);
        int oldorders = orders;
        int neworders = lfparent.getChildCount() + 1;
        int parentLayer = lfparent.getLayer();
        String sql = "update flow_directory set name=" + StrUtil.sqlstr(name) +
                ",description=" + StrUtil.sqlstr(description) +
                ",type=" + type + ",isHome=" + (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id=" + templateId +
                ",parent_code=" + StrUtil.sqlstr(newParentCode) + ",orders=" + neworders +
                ",layer=" + (parentLayer + 1) +
                ",formCode=" + StrUtil.sqlstr(formCode) + ",dept=" + StrUtil.sqlstr(dept) + ",is_open=" + (open ? 1 : 0) + ",unit_code=" + StrUtil.sqlstr(unitCode) + ",is_debug=" + (debug ? 1 : 0) +
                ",is_mobile_start=" + (mobileStart ? 1 : 0) +
                ",is_mobile_location=" + (mobileLocation ? 1 : 0) +
                ",is_mobile_camera=" + (mobileCamera ? 1 : 0) +
                ",query_id=" + queryId +
                ",query_role=" + StrUtil.sqlstr(queryRole) +
                ",query_cond_map=" + StrUtil.sqlstr(queryCondMap) +
                ",col_props=" + StrUtil.sqlstr(colProps) +
                ",cond_props=" + StrUtil.sqlstr(condProps) +
                ",params=" + StrUtil.sqlstr(params) +
                ",export_col_props=" + StrUtil.sqlstr(exportColProps) +
                ",icon=" + StrUtil.sqlstr(icon) +
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
                    sql = "select code from flow_directory where parent_code=" + StrUtil.sqlstr(oldParentCode) +
                            " and orders>" + oldorders;
                    ResultIterator ri = conn.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        Leaf clf = getLeaf(rr.getString(1));
                        clf.setOrders(clf.getOrders() - 1);
                        clf.update();
                    }

                    // 更新其所有子结点的layer
                    Vector<Leaf> vt = new Vector<>();
                    getAllChild(vt, this);
                    for (Leaf childlf : vt) {
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
                    if (oldParentLeaf != null) {
                        oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
                        oldParentLeaf.update();
                    } else {
                        DebugUtil.e(getClass(), "update", "节点 oldParentCode:" + oldParentCode + "不存在");
                    }
                    // 将其新父结点的孩子数 + 1
                    Leaf newParentLeaf = getLeaf(newParentCode);
                    newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
                    newParentLeaf.update();
                }
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        boolean re = r == 1;
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
        String insertsql = "insert into flow_directory (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date,formCode,dept,is_open,unit_code,is_debug,is_mobile_start,is_mobile_location,is_mobile_camera,query_id,query_role,query_cond_map,template_id,params,icon) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        if (!SecurityUtil.isValidSql(insertsql)) {
            throw new ErrMsgException("请勿输入非法字符如;号等！");
        }
        Conn conn = new Conn(connname);
        try {
            // 更改根结点的信息
            updatesql = "Update flow_directory set child_count=child_count+1" +
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
            ps.setInt(8, layer + 1);
            ps.setInt(9, childleaf.getType());
            ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
            ps.setString(11, childleaf.getFormCode());
            ps.setString(12, childleaf.getDept());
            ps.setInt(13, childleaf.isOpen() ? 1 : 0);
            ps.setString(14, childleaf.getUnitCode());
            ps.setInt(15, childleaf.isDebug() ? 1 : 0);
            ps.setInt(16, childleaf.isMobileStart() ? 1 : 0);

            ps.setInt(17, childleaf.isMobileLocation() ? 1 : 0);
            ps.setInt(18, childleaf.isMobileCamera() ? 1 : 0);

            ps.setLong(19, childleaf.getQueryId());
            ps.setString(20, childleaf.getQueryRole());
            ps.setString(21, childleaf.getQueryCondMap());

            ps.setInt(22, childleaf.getTemplateId());
            ps.setString(23, childleaf.getParams());
            ps.setString(24, childleaf.getIcon());

            conn.executePreUpdate();
            ps.close();

            conn.executeUpdate(updatesql);
            removeFromCache(code);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            LogUtil.getLog(getClass()).error(e);
            return false;
        } finally {
            conn.close();
        }

        return true;
    }

    /**
     * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
     *
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

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
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
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        } else {
            leaf.renew();
        }

        return leaf;
    }

    public Vector<Leaf> getLeavesUseForm(String formCode) {
        Vector<Leaf> v = new Vector<>();
        ResultSet rs;
        String sql = "select code from flow_directory where formCode=?";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, formCode);
            rs = conn.executePreQuery();
            if (rs.next()) {
                String mycode = rs.getString(1);
                Leaf lf = getLeaf(mycode);
                if (lf!=null) {
                    v.addElement(lf);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;
    }

    public boolean delsingle(Leaf leaf) {
        // 删除该目录下的所有流程及预定义流程
        if (leaf.getType() == TYPE_LIST) {
            WorkflowDb wd = new WorkflowDb();
            wd.delWorkflowDbOfType(leaf.getCode());

            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            wpd.delWorkflowPredefineDbOfType(leaf.getCode());
        }

        // 流程节点上的删除权限
        LeafPriv lp = new LeafPriv();
        lp.delPrivsOfDir(leaf.getCode());

        // 删被被调度项
        JobUnitDb jud = new JobUnitDb();
        jud.delJobOfWorkflow(leaf.getCode());

        Conn conn = new Conn(connname);
        try {
            conn.beginTrans();
            String sql = "delete from flow_directory where code=" +
                    StrUtil.sqlstr(leaf.getCode());
            boolean r = conn.executeUpdate(sql) == 1;
            sql = "update flow_directory set orders=orders-1 where parent_code=" +
                    StrUtil.sqlstr(leaf.getParentCode()) + " and orders>" +
                    leaf.getOrders();
            conn.executeUpdate(sql);
            sql = "update flow_directory set child_count=child_count-1 where code=" +
                    StrUtil.sqlstr(leaf.getParentCode());
            conn.executeUpdate(sql);

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            LogUtil.getLog(getClass()).error(e);
            return false;
        } finally {
            removeAllFromCache();
            conn.close();
        }
        return true;
    }

    public synchronized void del(Leaf leaf) {
        delsingle(leaf);
        for (Leaf lf : leaf.getChildren()) {
            del(lf);
        }
    }

    public Leaf getBrother(String direction) {
        String sql;
        RMConn rmconn = new RMConn(connname);
        Leaf bleaf = null;
        try {
            if ("down".equals(direction)) {
                sql = "select code from flow_directory where parent_code=" +
                        StrUtil.sqlstr(parent_code) +
                        " and orders=" + (orders + 1);
            } else {
                sql = "select code from flow_directory where parent_code=" +
                        StrUtil.sqlstr(parent_code) +
                        " and orders=" + (orders - 1);
            }

            ResultIterator ri = rmconn.executeQuery(sql);
            if (ri != null && ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                bleaf = getLeaf(rr.getString(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
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
                if ("down".equals(direction)) {
                    sql = "update flow_directory set orders=orders+1" +
                            " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update flow_directory set orders=orders-1" +
                            " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }

                if ("up".equals(direction)) {
                    sql = "update flow_directory set orders=orders-1" +
                            " where code=" + StrUtil.sqlstr(code);
                    conn.executeUpdate(sql);
                    sql = "update flow_directory set orders=orders+1" +
                            " where code=" + StrUtil.sqlstr(bleaf.getCode());
                    conn.executeUpdate(sql);
                }
                conn.commit();
                removeFromCache(code);
                removeFromCache(bleaf.getCode());
            } catch (SQLException e) {
                conn.rollback();
                LogUtil.getLog(getClass()).error(e);
                return false;
            } finally {
                conn.close();
            }
        }

        return true;
    }

    /**
     * 弃用了，改用getLeavesByForm
     *
     * @param formCode String
     * @return Leaf
     */
    public Leaf getLeafByFormCode(String formCode) {
        String sql = "select code from flow_directory where formCode=?";
        ResultSet rs;
        Leaf lf = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, formCode);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                lf = getLeaf(rs.getString(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return lf;
    }

    /**
     * 通过名称找到节点
     *
     * @param name
     * @return
     * @Description:
     */
    public Leaf getLeafByName(String name) {
        String sql = "select code from flow_directory where name=?";
        ResultSet rs = null;
        Leaf lf = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                lf = getLeaf(rs.getString(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return lf;
    }


    //add by wm
    public Vector<Leaf> getTreeChildren(String parentCdoe) {
        Vector<Leaf> v = new Vector<>();
        String sql = "select code from flow_directory where parent_code=? order by orders asc";
        Conn conn = new Conn(connname);
        ResultSet rs;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, parentCdoe);
            rs = conn.executePreQuery();
            while (rs.next()) {
                String c = rs.getString(1);
                //LogUtil.getLog(getClass()).info("child=" + c);
                v.addElement(getLeaf(c));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return v;
    }


    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setChildCount(int childCount) {
        this.child_count = childCount;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setMobileStart(boolean mobileStart) {
        this.mobileStart = mobileStart;
    }

    public boolean isMobileStart() {
        return mobileStart;
    }

    private int templateId = -1;
    private boolean loaded = false;
    private String pluginCode = PLUGIN_NONE;
    private String formCode;
    private String dept; // 流程可见的部门，如果为空，则表示对所有部门可见

    private boolean debug = false;

    private boolean mobileStart = true;
    private boolean mobileLocation = false;
    private boolean mobileCamera = true;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    private String icon;

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    /**
     * 发起流程时带入的参数
     */
    private String params;

    public boolean isMobileLocation() {
        return mobileLocation;
    }

    public void setMobileLocation(boolean mobileLocation) {
        this.mobileLocation = mobileLocation;
    }

    public boolean isMobileCamera() {
        return mobileCamera;
    }

    public void setMobileCamera(boolean mobileCamera) {
        this.mobileCamera = mobileCamera;
    }

    public void setQueryId(long queryId) {
        this.queryId = queryId;
    }

    public long getQueryId() {
        return queryId;
    }

    public void setQueryRole(String queryRole) {
        this.queryRole = queryRole;
    }

    public String getQueryRole() {
        return queryRole;
    }

    public void setQueryCondMap(String queryCondMap) {
        this.queryCondMap = queryCondMap;
    }

    public String getQueryCondMap() {
        return queryCondMap;
    }

    public String getName(HttpServletRequest request) {
        if (request==null) {
            return name;
        }
        if (name.startsWith("#")) {
            return LocalUtil.LoadString(request, "res.ui.menu", code);
        } else {
            return name;
        }
    }

}
