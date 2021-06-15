package cn.js.fan.module.cms;

import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import java.util.Vector;
import java.util.Iterator;
import java.io.File;
import cn.js.fan.util.file.FileUtil;
import java.io.IOException;
import com.redmoon.forum.media.MediaDirDb;
// import cn.js.fan.module.cms.site.SiteDb;
import cn.js.fan.module.cms.plugin.PluginUnit;
import cn.js.fan.module.pvg.User;
import com.redmoon.forum.person.UserDb;
import com.cloudwebsoft.framework.util.LogUtil;

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

    public Directory() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:conname is empty.");
    }

    /**
     * 已经不用
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean AddRootChild(HttpServletRequest request) throws
            ErrMsgException {
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0;
        String root_code = "", name = "", code = "", parent_code = "-1";
        int parent_layer = 1;
        boolean isParentRoot = false; //父目录是不是一级目录

        name = ParamUtil.get(request, "name", false);
        if (name == null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_name_empty"));
        code = ParamUtil.get(request, "code", false);
        if (code == null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_code_empty"));
        String description = ParamUtil.get(request, "description");

        root_code = code;

        String insertsql = "insert into directory (code,name,parent_code,description,orders,root_code,child_count,layer) values (";
        insertsql += StrUtil.sqlstr(code) + "," + StrUtil.sqlstr(name) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr(description) + "," +
                orders + "," + StrUtil.sqlstr(root_code) + "," +
                child_count + ",1)";

        // logger.info(insertsql);
        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_SQL));
        int r = 0;
        RMConn conn = new RMConn(connname);
        try {
            r = conn.executeUpdate(insertsql);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_DB)); // "请检查编码" + code + "是否重复！");
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
        if (name == null || name.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_name_empty"));
        code = ParamUtil.get(request, "code").trim();
        if (code.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_code_empty"));
        if (!StrUtil.isSimpleCode(code))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_simple_code")); // 编码请使用字母、数字或者符号 _ -！

        parent_code = ParamUtil.get(request, "parent_code").trim();
        if (parent_code.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_parent_code_empty"));
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type");
        String pluginCode = ParamUtil.get(request, "pluginCode");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;

        String logo = ParamUtil.get(request, "logo");

        String strPrice = ParamUtil.get(request, "price");

        int isPost = ParamUtil.getInt(request, "isPost", 0);

        double price = 0.0;
        try {
            price = Double.parseDouble(strPrice);
        }
        catch (Exception e) {}

        int templateDocId = ParamUtil.getInt(request, "templateDocId", -1);

        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        if (lf!=null && lf.isLoaded())
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_same_code") + lf.getName()); // 已存在相同编码的节点
        lf = new Leaf();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setPluginCode(pluginCode);
        lf.setPrice(price);
        lf.setIsHome(isHome);
        lf.setTemplateDocId(templateDocId);
        lf.setLogo(logo);
        lf.setPost(isPost==1);

        Leaf leaf = getLeaf(parent_code);
        boolean re = leaf.AddChild(lf);
        if (re) {
            // 如果管理员加入的是子站点，区别于用户自建站点时，在初始化站点时有所不一样
            /*
            if (type==Leaf.TYPE_SUB_SITE) {
                // 为其在媒体管理的图片目录上创建目录
                initImgDirOfSubsite(lf);
                initFlashDirOfSubsite(lf);
                // 初始化站点
                SiteDb sd = new SiteDb();
                sd.init(code, name, "", parent_code);
            }
            */
        }
        return re;
    }

    /**
     * 初始化子站点对应的图片目录节点
     * @param lf Leaf
     * @throws ErrMsgException
     */
    public static void initImgDirOfSubsite(Leaf lf) throws ErrMsgException {
        ImgStoreDirDb isdd = new ImgStoreDirDb();
        isdd = isdd.getImgStoreDirDb(MediaDirDb.ROOTCODE);
        ImgStoreDirDb childlf = new ImgStoreDirDb();
        childlf.setName(lf.getName());
        childlf.setCode(lf.getCode());
        childlf.setParentCode(MediaDirDb.ROOTCODE);
        childlf.setDescription("");
        childlf.setType(0);
        // System.out.print(getClass() + " isdd" + isdd + " name=" + name);
        isdd.AddChild(childlf);
    }

    /**
     * 当用户申请建站时，初始化子站点，并为其创建用户及权限
     * @param code String
     * @param name String
     * @param userName String
     * @return boolean
     * @throws ErrMsgException
     */
    public static boolean initLeafOfSubsite(String code, String name, String kind, UserDb user) throws
            ErrMsgException {
        Leaf leaf = new Leaf();
        leaf = leaf.getLeaf(kind);
        if (leaf==null) {
            throw new ErrMsgException("类别" + kind + " 不存在！");
        }
        Leaf childlf = new Leaf();
        childlf.setName(name);
        childlf.setCode(code);
        childlf.setParentCode(kind);
        childlf.setDescription("");
        childlf.setType(Leaf.TYPE_SUB_SITE);

        childlf.setIsHome(true);
        childlf.setTemplateDocId(-1);
        childlf.setPluginCode(PluginUnit.DEFAULT);
        boolean re = leaf.AddChild(childlf);
        if (re) {
            // 创建子站点管理员
            cn.js.fan.module.pvg.User u = new cn.js.fan.module.pvg.User();
            // 管理员使用nick作为name
            u = u.getUser(user.getNick());
            // 如果子站点管理员未曾被创建，则创建
            if (!u.isLoaded()) {
                re = u.insert(user.getNick(), user.getNick(), "", "", true);
            }
            // 初始化子站点管理权限
            LeafPriv lp = new LeafPriv(code);
            lp.setAppend(1);
            lp.setModify(1);
            lp.setDel(1);
            lp.setSee(1);
            lp.setExamine(1); // 具有审核权限的人员，为站点管理员

            re = lp.add(user.getNick(), LeafPriv.TYPE_USER);

            // 初始化图片和Flash目录
            leaf = leaf.getLeaf(code);

            initImgDirOfSubsite(leaf);
            initFlashDirOfSubsite(leaf);
        }
        return re;
    }

    /**
     * 初始化子站点对应的Flash目录节点
     * @param lf Leaf
     * @throws ErrMsgException
     */
    public static void initFlashDirOfSubsite(Leaf lf) throws ErrMsgException {
        FlashStoreDirDb isdd = new FlashStoreDirDb();
        isdd = isdd.getFlashStoreDirDb(MediaDirDb.ROOTCODE);
        FlashStoreDirDb childlf = new FlashStoreDirDb();
        childlf.setName(lf.getName());
        childlf.setCode(lf.getCode());
        childlf.setParentCode(MediaDirDb.ROOTCODE);
        childlf.setDescription("");
        childlf.setType(0);
        // System.out.print(getClass() + " isdd" + isdd + " name=" + name);
        isdd.AddChild(childlf);
    }

    public void del(String delcode) throws ErrMsgException {
        Leaf lf = getLeaf(delcode);

        String htmlPath = lf.getListHtmlPath();

        lf.del(lf);

        // 删除列表页面
        Config cfg = new Config();
        boolean isDelHtml = cfg.getBooleanProperty("cms.html_auto");
        if (isDelHtml) {
            // 删除目录
            try {
                System.out.println(getClass() + " del: htmlPath=" + htmlPath);
                FileUtil.del(Global.realPath + "/" + htmlPath);
            }
            catch (IOException e) {
                logger.error("del:" + e.getMessage());
            }
        }
        /*
        if (lf.getType()==Leaf.TYPE_SUB_SITE) {
            // 删除站点的图片和Flash文件夹
            SiteDb sd = new SiteDb();
            sd = sd.getSiteDb(lf.getCode());
            if (sd==null)
                return;

            FlashStoreDirDb fsdd = new FlashStoreDirDb();
            fsdd = fsdd.getFlashStoreDirDb(lf.getCode());
            fsdd.del();
            ImgStoreDirDb isdd = new ImgStoreDirDb();
            isdd = isdd.getImgStoreDirDb(lf.getCode());
            isdd.del();

            // 如果用户创建的站点数为1，则删除站点管理员
            User user = new User();
            com.redmoon.forum.person.UserDb ud = new com.redmoon.forum.person.UserDb();
            ud = ud.getUser(sd.getString("owner"));
            Vector vsite = sd.getSubsitesOfUser(ud.getName());
            if (vsite.size()==1) {
                user.del(ud.getNick());
            }
            // 删除站点
            try {
                sd.del();
            }
            catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error("del:" + StrUtil.trace(e));
            }
        }
        */
    }

    public synchronized boolean update(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String name = ParamUtil.get(request, "name", false);
        String description = ParamUtil.get(request, "description");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        int type = ParamUtil.getInt(request, "type");
        if (code == null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_need_code"));
        }
        if (name==null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_need_name"));
        }

        int templateId = ParamUtil.getInt(request, "templateId", -1);
        String parentCode = ParamUtil.get(request, "parentCode");
        String pluginCode = ParamUtil.get(request, "pluginCode");

        String strPrice = ParamUtil.get(request, "price");
        double price = 0.0;
        try {
            price = Double.parseDouble(strPrice);
        }
        catch (Exception e) {}

        int templateDocId = ParamUtil.getInt(request, "templateDocId", -1);
        String templateCatalog = ParamUtil.get(request, "templateCatalog");
        String logo = ParamUtil.get(request, "logo");
        int isPost = ParamUtil.getInt(request, "isPost", 0);

        Leaf leaf = getLeaf(code);//new Leaf();
        if (code.equals(parentCode)) {
            throw new ErrMsgException("请选择正确的父节点！");
        }
        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            Leaf lf = getLeaf(parentCode); // 取得新的父节点
            while (lf!=null && !lf.getCode().equals(lf.ROOTCODE)) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode()))
                    throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_parent_code"));
                    // throw new ErrMsgException("不能将其子节点更改为父节点");
                lf = getLeaf(pCode);
            }
        }

        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        leaf.setType(type);
        leaf.setTemplateId(templateId);
        leaf.setPluginCode(pluginCode);
        leaf.setPrice(price);
        leaf.setTemplateDocId(templateDocId);
        leaf.setTemplateCatalog(templateCatalog);
        leaf.setLogo(logo);
        leaf.setPost(isPost==1);
        boolean re = false;
        if (parentCode.equals(leaf.getParentCode()))
            re = leaf.update();
        else {
            re = leaf.update(parentCode);
            // 如果为子站点，则更改站点所属类别
            /*
            if (leaf.getType()==Leaf.TYPE_SUB_SITE) {
                try {
                    SiteDb sd = new SiteDb();
                    sd = sd.getSiteDb(leaf.getCode());
                    sd.set("kind", parentCode);
                    sd.save();
                }
                catch (ResKeyException e) {
                    throw new ErrMsgException(e.getMessage(request));
                }
            }
            */
        }
        return re;
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String direction = ParamUtil.get(request, "direction", false);
        if (code == null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_need_code"));
        }
        if ( direction == null ) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.cms.Directory", "err_need_direction"));
        }

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

    public Leaf getBrother(String code, String direction) throws
            ErrMsgException {
        Leaf lf = getLeaf(code);
        return lf.getBrother(direction);
    }

    public Vector getChildren(String code) throws ErrMsgException {
        Leaf leaf = getLeaf(code);
        if (leaf==null || !leaf.isLoaded())
            return new Vector();
        return leaf.getChildren();
    }

    /**
     * 取得目录的子目录
     * @param layer int 级数 一级或者二级
     * @return String
     */
    public String getSubLeaves(String parentCode, int layer) {
        if (layer>2)
            return "";

        String str = "";
        LeafChildrenCacheMgr lcc = new LeafChildrenCacheMgr(parentCode);
        Vector v = lcc.getList();
        Iterator ir = v.iterator();
        // 进入第一级
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            if (layer==2) {
                 // 进入第二级
                 lcc = new LeafChildrenCacheMgr(lf.getCode());
                 Iterator ir2 = lcc.getList().iterator();
                 while (ir2.hasNext()) {
                     lf = (Leaf) ir2.next();
                     if (str.equals(""))
                         str = StrUtil.sqlstr(lf.getCode());
                     else
                         str += "," + StrUtil.sqlstr(lf.getCode());
                 }
            }
            else {
                if (str.equals(""))
                    str = StrUtil.sqlstr(lf.getCode());
                else
                    str += "," + StrUtil.sqlstr(lf.getCode());
            }

        }
        return str;
    }
}
