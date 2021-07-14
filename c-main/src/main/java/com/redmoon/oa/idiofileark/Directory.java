package com.redmoon.oa.idiofileark;

import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserDb;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 * ╋ 女性话题      一级目录
 *   ├『花样年华』  二级目录
 *   ├『花样年华』
 *   ╋ 女性话题     二级目录
 *     ├『花样年华』 三级目录
 * @author not attributable
 * @version 1.0
 */

public class Directory implements IDirectory {
    String connname = "";
    Logger logger = Logger.getLogger(Directory.class.getName());
    private String code;

    public Directory() {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            logger.info("Directory:默认数据库名不能为空");
        }
    }

    public boolean AddRootChild(HttpServletRequest request) throws
            ErrMsgException {
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0;
        String root_code = "", name = "", code = "", parent_code = "-1";
        int parent_layer = 1;
        boolean isParentRoot = false; //父目录是不是一级目录

        name = ParamUtil.get(request, "name", false);
        if (name == null) {
            throw new ErrMsgException("名称不能为空！");
        }
        code = ParamUtil.get(request, "code", false);
        if (code == null) {
            throw new ErrMsgException("编码不能为空！");
        }
        String description = ParamUtil.get(request, "description");

        root_code = code;

        String insertsql = "insert into oa_idiofileark_dir (code,name,parent_code,description,orders,root_code,child_count,layer) values (";
        insertsql += StrUtil.sqlstr(code) + "," + StrUtil.sqlstr(name) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr(description) + "," +
                orders + "," + StrUtil.sqlstr(root_code) + "," +
                child_count + ",1)";

        if (!SecurityUtil.isValidSql(insertsql)) {
            throw new ErrMsgException("请勿输入非法字符如;号等！");
        }
        int r = 0;
        RMConn conn = new RMConn(connname);
        try {
            r = conn.executeUpdate(insertsql);
        } catch (SQLException e) {
            logger.error("AddRootChild:" + e.getMessage());
            throw new ErrMsgException("请检查编码" + code + "是否重复！");
        }
        return r == 1 ? true : false;
    }

    public boolean AddRootChild(String code, String name) throws
            ErrMsgException {
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0, type = Leaf.TYPE_NONE;
        String root_code = "", parent_code = "-1";
        int parent_layer = 1;
        boolean isParentRoot = false; //父目录是不是一级目录

        if (name == null) {
            throw new ErrMsgException("名称不能为空！");
        }

        if (code == null) {
            throw new ErrMsgException("编码不能为空！");
        }
        String description = "";

        root_code = code;

        String insertsql = "insert into oa_idiofileark_dir (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date) values (";
        insertsql += StrUtil.sqlstr(code) + "," + StrUtil.sqlstr(name) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr(description) + "," +
                orders + "," + StrUtil.sqlstr(root_code) + "," +
                child_count + ",1," + type + "," + SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss") + ")";

        logger.info(insertsql);
        if (!SecurityUtil.isValidSql(insertsql)) {
            throw new ErrMsgException("请勿输入非法字符如;号等！");
        }
        int r = 0;
        RMConn conn = new RMConn(connname);
        try {
            r = conn.executeUpdate(insertsql);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("请检查编码" + code + "是否重复！");
        }
        return r == 1 ? true : false;
    }

    public boolean AddChild(HttpServletRequest request) throws
            ErrMsgException {
        // 取出被回复的贴子的有关信息
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0;
        String root_code = "", name = "", code = "", parent_code = "";

        name = ParamUtil.get(request, "name").trim();
        if (name == null || name.equals("")) {
            throw new ErrMsgException("名称不能为空！");
        }
        code = ParamUtil.get(request, "code").trim();
        if (code.equals("")) {
            throw new ErrMsgException("编码不能为空！");
        }
        if (!StrUtil.isSimpleCode(code)) {
            throw new ErrMsgException("编码请使用字母、数字、-或_！");
        }

        parent_code = ParamUtil.get(request, "parent_code").trim();
        if (parent_code.equals("")) {
            throw new ErrMsgException("父结点不能为空！");
        }
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type");
        String pluginCode = ParamUtil.get(request, "pluginCode");
        boolean isSystem = ParamUtil.get(request, "isSystem").equals("1") ? true : false;
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        String target = ParamUtil.get(request, "target");

        /**
                Privilege privilege = new Privilege();
                LeafPriv lp = new LeafPriv();
                lp.setDirCode(parent_code);
                if (!lp.canUserModify(privilege.getUser(request))) {
                    throw new ErrMsgException(SkinUtil.LoadString(request,
                            "pvg_invalid"));
                }
         */

        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        if (lf != null && lf.isLoaded()) {
            throw new ErrMsgException("已存在相同编码的节点：" + lf.getName());
        }
        lf = new Leaf();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setPluginCode(pluginCode);
        lf.setSystem(isSystem);
        lf.setIsHome(isHome);
        lf.setTarget(target);

        Leaf leaf = getLeaf(parent_code);
        return leaf.AddChild(lf);
    }

    public boolean AddChild(String parentCode, String code, String name,
                            int orders, int type) throws
            ErrMsgException {

        int child_count = 0, parent_orders = 1,
                                             islocked = 0;
        String root_code = "", parent_code = "";

        if (name == null || name.equals("")) {
            throw new ErrMsgException("名称不能为空！");
        }

        if (code.equals("")) {
            throw new ErrMsgException("编码不能为空！");
        }
        if (!StrUtil.isSimpleCode(code)) {
            throw new ErrMsgException("编码请使用字母、数字、-或_！");
        }

        parent_code = parentCode;
        if (parent_code.equals("")) {
            throw new ErrMsgException("父结点不能为空！");
        }
        String description = "";
        String pluginCode = "";
        boolean isSystem = true;
        boolean isHome = true;
        String target = "";

        /**
                Privilege privilege = new Privilege();
                LeafPriv lp = new LeafPriv();
                lp.setDirCode(parent_code);
                if (!lp.canUserModify(privilege.getUser(request))) {
                    throw new ErrMsgException(SkinUtil.LoadString(request,
                            "pvg_invalid"));
                }
         */

        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        if (lf != null && lf.isLoaded()) {
            throw new ErrMsgException("已存在相同编码的节点：" + lf.getName());
        }
        lf = new Leaf();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setPluginCode(pluginCode);
        lf.setSystem(isSystem);
        lf.setIsHome(isHome);
        lf.setTarget(target);

        Leaf leaf = getLeaf(parent_code);
        return leaf.AddChild(lf);
    }


    public void del(HttpServletRequest request, String delcode) throws
            ErrMsgException {
        Leaf lf = getLeaf(delcode);
        if (lf == null) {
            throw new ErrMsgException("节点 " + delcode + " 已被删除！");
        }
        if (lf.isSystem()) {
            throw new ErrMsgException(lf.getName() + " 是系统保留节点， 无法被删除！");
        }
        lf.del(lf);
    }

    public synchronized boolean update(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String name = ParamUtil.get(request, "name", false);
        String description = ParamUtil.get(request, "description");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        int type = ParamUtil.getInt(request, "type");
        if (code == null || name == null) {
            throw new ErrMsgException("code与name项必填！");
        }
        int templateId = ParamUtil.getInt(request, "templateId");
        String parentCode = ParamUtil.get(request, "parentCode");
        String pluginCode = ParamUtil.get(request, "pluginCode");
        boolean isSystem = ParamUtil.get(request, "isSystem").equals("1") ? true : false;
        String target = ParamUtil.get(request, "target");

        /**
                 Privilege privilege = new Privilege();
                 LeafPriv lp = new LeafPriv();
                 lp.setDirCode(code);
                 if (!lp.canUserModify(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
                 }
         */

        Leaf leaf = getLeaf(code); // new Leaf();
        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            Leaf lf = getLeaf(parentCode); // 取得新的父节点
            while (lf != null && !lf.getCode().equals(lf.ROOTCODE)) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode())) {
                    throw new ErrMsgException("不能将其子节点更改为父节点");
                }
                lf = getLeaf(pCode);
            }
        }

        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        leaf.setType(type);
        leaf.setTemplateId(templateId);
        leaf.setPluginCode(pluginCode);
        leaf.setSystem(isSystem);
        leaf.setTarget(target);
        boolean re = false;
        if (leaf.getCode().equals(leaf.ROOTCODE) ||
            parentCode.equals(leaf.getParentCode())) {
            re = leaf.update();
        } else {
            re = leaf.update(parentCode);
        }

        return re;
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String direction = ParamUtil.get(request, "direction", false);
        if (code == null || direction == null) {
            throw new ErrMsgException("编码与方向项必填！");
        }
        /**
                 Privilege privilege = new Privilege();
                 LeafPriv lp = new LeafPriv();
                 lp.setDirCode(code);
                 if (!lp.canUserModify(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
                 }
         */

        Leaf lf = new Leaf(code);
        return lf.move(direction);
    }


    /**
     * 取得菜单，以layer级目录为大标题，layer+1级目录为小标题
     * @param root_code String 所属的根目录
     */
    public Menu getMenu(String root_code) throws ErrMsgException {
        Directory dir = new Directory();
        Leaf leaf = dir.getLeaf(root_code);

        Menu menu = new Menu();
        MenuItem mi = new MenuItem();
        menu.addItem(mi);
        mi.setHeadLeaf(leaf);

        Vector children = leaf.getChildren();
        Iterator ir = children.iterator();
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            mi.addChildLeaf(lf);
        }

        return menu;
    }

    public Leaf getLeaf(String code) {
        Leaf leaf = new Leaf();
        return leaf.getLeaf(code);
    }

    public Leaf getRootNodeOfUserOrInit(String userName) throws ErrMsgException {
        if (!IsIdiofilearkDirInitialized(userName)) {
            initIdiofilearkUserRootNode(userName);
        }
        return getLeaf(userName);
    }

    public Leaf getBrother(String code, String direction) throws
            ErrMsgException {
        Leaf lf = getLeaf(code);
        return lf.getBrother(direction);
    }

    public Vector getChildren(String code) throws ErrMsgException {
        Leaf leaf = getLeaf(code);
        if (leaf == null || !leaf.isLoaded()) {
            return new Vector();
        }
        return leaf.getChildren();
    }

    /**
     * 取得目录的子目录
     * @param layer int 级数 一级或者二级
     * @return String
     */
    public String getSubLeaves(String parentCode, int layer) {
        if (layer > 2) {
            return "";
        }

        String str = "";
        LeafChildrenCacheMgr lcc = new LeafChildrenCacheMgr(parentCode);
        Vector v = lcc.getList();
        Iterator ir = v.iterator();
        // 进入第一级
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            if (layer == 2) {
                // 进入第二级
                lcc = new LeafChildrenCacheMgr(lf.getCode());
                Iterator ir2 = lcc.getList().iterator();
                while (ir2.hasNext()) {
                    lf = (Leaf) ir2.next();
                    if (str.equals("")) {
                        str = StrUtil.sqlstr(lf.getCode());
                    } else {
                        str += "," + StrUtil.sqlstr(lf.getCode());
                    }
                }
            } else {
                if (str.equals("")) {
                    str = StrUtil.sqlstr(lf.getCode());
                } else {
                    str += "," + StrUtil.sqlstr(lf.getCode());
                }
            }

        }
        return str;
    }

    public Vector getLeafOfUserAndLayer(HttpServletRequest request,
                                        String layerStr) throws
            ErrMsgException {
        String userName = "";
        Privilege privilege = new Privilege();
        userName = privilege.getUser(request);
        int layer = StrUtil.toInt(layerStr);

        Vector vt = new Vector();

        String LOAD = "select code from oa_idiofileark_dir where  user_name = ? and layer = ? order by orders";

        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(LOAD);
            ps.setString(1, userName);
            ps.setInt(2, layer);
            rs = conn.executePreQuery();
            while (rs != null && rs.next()) {
                this.code = rs.getString(1);
                //leaf = new Leaf();
                //vt.add(leaf.getLeaf(code));
                vt.add(getLeaf(code));
            }
        } catch (SQLException e) {
            logger.error("getLeafOfUserAndLayer: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return vt;
    }

    /**
     * 判断用户个人文件柜目录是否已初始化
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean IsIdiofilearkDirInitialized(String userName) throws
            ErrMsgException {
        return getLeaf(userName)!=null;
    }

    /**
     * 用户个人文件柜目录初始化
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public void idiofilearkDirInitialize(String users) throws
            ErrMsgException {
        String[] ary = users.split(",");
        int len = ary.length;
        for (int i = 0; i < len; i++) {
            if (!IsIdiofilearkDirInitialized(ary[i])) {
                initIdiofilearkUserRootNode(ary[i]);
            }
        }
    }

    /**
     * 建立用户个人文件柜目录根节点
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean initIdiofilearkUserRootNode(String userName) throws
            ErrMsgException {
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        return AddRootChild(userName, "个人文件柜");
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
