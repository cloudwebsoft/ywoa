package com.redmoon.forum;

import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.kit.util.FileInfo;
import java.io.File;
import javax.servlet.ServletContext;

/**
 *
 * <p>Title: 论坛版块管理代理类</p>
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

    public boolean AddRootChild(HttpServletRequest request) throws
            ErrMsgException {
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0;
        String root_code = "", name = "", code = "", parent_code = "-1";
        int parent_layer = 1;
        boolean isParentRoot = false; //父目录是不是一级目录

        name = ParamUtil.get(request, "name", false);
        if (name == null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_name_empty"));
        code = ParamUtil.get(request, "code", false);
        if (code == null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_code_empty"));
        String description = ParamUtil.get(request, "description");

        root_code = code;

        String insertsql = "insert into sq_board (code,name,parent_code,description,orders,root_code,child_count,layer) values (";
        insertsql += StrUtil.sqlstr(code) + "," + StrUtil.sqlstr(name) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr(description) + "," +
                orders + "," + StrUtil.sqlstr(root_code) + "," +
                child_count + ",1)";

        // logger.info(insertsql);
        // if (!SecurityUtil.isValidSql(insertsql))
        //    throw new ErrMsgException("请勿输入非法字符如;号等！");
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

    public boolean AddChild(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        String logo = "";

        FileUpload fileupload = new FileUpload();
        // 置保存路径
        String realpath = Global.getRealPath() + "forum/images/board_logo/";
        fileupload.setSavePath(realpath);
        // 置总的上传文件的大小的最大值为600K
        fileupload.setMaxAllFileSize(600);
        // 设置单个文件的最大值为200k
        fileupload.setMaxFileSize(200);
        // 设置合法的扩展名
        String[] ext = {"gif", "jpg", "png", "jpeg"};
        fileupload.setValidExtname(ext);
        // 处理上传数据，编码为utf-8
        int ret = 0;
        try {
            ret = fileupload.doUpload(application, request, "utf-8");
        }
        catch (Exception e) {
            logger.error("AddChild: " + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        // 如果上传出错，则写相应的出错信息
        if (ret==-4 || ret==-3 ) {
            throw new ErrMsgException(fileupload.getErrMessage(request));
        } else {
            if (ret==fileupload.RET_SUCCESS) {
                // 使用随机名称写入磁盘
                fileupload.writeFile(true);
                Vector v = fileupload.getFiles();
                if (v.size()>0) {
                    FileInfo fi = (FileInfo) v.elementAt(0);
                    if (fi != null)
                        logo = fi.getDiskName();
                }
            }
        }

        String name = "", code = "", parent_code = "";

        name = fileupload.getFieldValue("name");
        if (name == null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_name_empty"));
        code = fileupload.getFieldValue("code");
        if (code == null || code.trim().equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_code_empty"));
        if (code.equals("not"))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_code_not"));
        code = code.trim();

        if (!StrUtil.isSimpleCode(code))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_simple_code")); // 编码请使用字母、数字或者符号 _ -！

        parent_code = fileupload.getFieldValue("parent_code");
        if (parent_code==null || parent_code.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_parent_code_empty"));
        String description = fileupload.getFieldValue("description");
        String strtype = fileupload.getFieldValue("type");
        int type = Integer.parseInt(strtype);
        String theme = fileupload.getFieldValue("theme");
        String skin = fileupload.getFieldValue("skin").trim();
        String locked = fileupload.getFieldValue("isLocked");
        int isLocked = Integer.parseInt(locked);
        String color = fileupload.getFieldValue("color");
        String isHome = fileupload.getFieldValue("isHome");
        String strWebeditAllowType = fileupload.getFieldValue("webeditAllowType");
        int webeditAllowType = Leaf.WEBEDIT_ALLOW_TYPE_UBB_NORMAL;
        if (StrUtil.isNumeric(strWebeditAllowType))
            webeditAllowType = Integer.parseInt(strWebeditAllowType);
        String plugin2Code = StrUtil.getNullStr(fileupload.getFieldValue("plugin2Code"));
        String strCheckMsg = fileupload.getFieldValue("checkMsg");
        int checkMsg = StrUtil.toInt(strCheckMsg, 0);
        int delMode = StrUtil.toInt(fileupload.getFieldValue("delMode"), 0);
        int displayStyle = StrUtil.toInt(fileupload.getFieldValue("displayStyle"), Leaf.DISPLAY_STYLE_VERTICAL);
        boolean isBold = StrUtil.getNullStr(fileupload.getFieldValue("isBold")).equals("true");

        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        if (lf!=null && lf.isLoaded())
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_same_code") + lf.getName()); // 已存在相同编码的节点

        lf = new Leaf();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setLogo(logo);
        lf.setTheme(theme);
        lf.setSkin(skin);
        lf.setLocked((isLocked==1)?true:false);
        lf.setColor(color);
        if (isHome!=null && isHome.equals("true"))
            lf.setIsHome(true);
        else
            lf.setIsHome(false);
        lf.setWebeditAllowType(webeditAllowType);
        lf.setPlugin2Code(plugin2Code);
        lf.setCheckMsg(checkMsg);
        lf.setDelMode(delMode);
        lf.setDisplayStyle(displayStyle);
        lf.setBold(isBold);

        Leaf leaf = getLeaf(parent_code);
        return leaf.AddChild(lf);
    }

    public void del(ServletContext application, String delcode) throws ErrMsgException {
        Leaf lf = getLeaf(delcode);
        lf.del(lf);
    }

    public synchronized boolean update(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        String logo = "";
        FileUpload fileupload = new FileUpload();
        // 置保存路径
        String realpath = Global.getRealPath() + "forum/images/board_logo/";
        fileupload.setSavePath(realpath);
        // 置总的上传文件的大小的最大值为600K
        fileupload.setMaxAllFileSize(600);
        // 设置单个文件的最大值为200k
        fileupload.setMaxFileSize(200);
        // 设置合法的扩展名
        String[] ext = {"gif", "jpg", "png", "jpeg"};
        fileupload.setValidExtname(ext);
        // 处理上传数据，编码为utf-8
        int ret = 0;
        try {
            ret = fileupload.doUpload(application, request, "utf-8");
        }
        catch (Exception e) {
            logger.error("AddChild: " + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        // 如果上传出错，则写相应的出错信息
        if (ret==-4 || ret==-3 ) {
            throw new ErrMsgException(fileupload.getErrMessage(request));
        } else {
            if (ret==fileupload.RET_SUCCESS) {
                // 使用随机名称写入磁盘
                fileupload.writeFile(true);
                Vector v = fileupload.getFiles();
                if (v.size()>0) {
                    FileInfo fi = (FileInfo) v.elementAt(0);
                    if (fi != null)
                        logo = fi.getDiskName();
                }
            }
        }

        String code = fileupload.getFieldValue("code");
        String name = fileupload.getFieldValue("name");
        if (code == null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_need_code"));
        }
        if (name==null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_need_name"));
        }

        Leaf leaf = getLeaf(code);

        String dellogo = StrUtil.getNullString(fileupload.getFieldValue("dellogo"));
        if (fileupload.getFiles().size()>0 || dellogo.equals("1")) {
            String lg = leaf.getLogo();
            // 如果原有文件存在，则删除文件
            if (lg!=null && !lg.equals("")) {
                try {
                    File file = new File(realpath + lg);
                    file.delete();
                }
                catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }
        }

        String description = fileupload.getFieldValue("description");
        String ih = StrUtil.getNullString(fileupload.getFieldValue("isHome"));
        boolean isHome = ih.equals("true") ? true : false;
        String strtype = fileupload.getFieldValue("type");
        int type = Integer.parseInt(strtype);
        String theme = StrUtil.getNullStr(fileupload.getFieldValue("theme")).trim();
        String skin = StrUtil.getNullStr(fileupload.getFieldValue("skin")).trim();
        String locked = fileupload.getFieldValue("isLocked");
        int isLocked = Integer.parseInt(locked);
        String color = fileupload.getFieldValue("color");
        String strWebeditAllowType = fileupload.getFieldValue("webeditAllowType");
        int webeditAllowType = Leaf.WEBEDIT_ALLOW_TYPE_UBB_NORMAL;
        if (StrUtil.isNumeric(strWebeditAllowType))
            webeditAllowType = Integer.parseInt(strWebeditAllowType);
        String plugin2Code = StrUtil.getNullStr(fileupload.getFieldValue("plugin2Code"));
        String strCheckMsg = fileupload.getFieldValue("checkMsg");
        int checkMsg = StrUtil.toInt(strCheckMsg, 0);
        int delMode = StrUtil.toInt(fileupload.getFieldValue("delMode"), 0);
        int displayStyle = StrUtil.toInt(fileupload.getFieldValue("displayStyle"), Leaf.DISPLAY_STYLE_VERTICAL);

        String parentCode = fileupload.getFieldValue("parentCode");

        if (code.equals(parentCode)) {
            throw new ErrMsgException("请选择正确的父节点！");
        }
        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            Leaf lf = getLeaf(parentCode); // 取得新的父节点
            while (lf!=null && !lf.getCode().equals(lf.CODE_ROOT)) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode()))
                    throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_parent_code"));
                    // throw new ErrMsgException("不能将其子节点更改为父节点");
                lf = getLeaf(pCode);
            }
        }
        boolean isBold = StrUtil.getNullStr(fileupload.getFieldValue("isBold")).equals("true");

        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        //logger.info("isHome:" + isHome);
        leaf.setType(type);
        leaf.setTheme(theme);
        leaf.setSkin(skin);
        leaf.setLocked(isLocked==1?true:false);

        if (dellogo.equals("1"))
            leaf.setLogo("");
        else if (fileupload.getFiles().size() > 0)
            leaf.setLogo(logo);

        leaf.setColor(color);
        leaf.setWebeditAllowType(webeditAllowType);
        leaf.setPlugin2Code(plugin2Code);
        leaf.setCheckMsg(checkMsg);
        leaf.setDelMode(delMode);
        leaf.setDisplayStyle(displayStyle);
        leaf.setBold(isBold);

        boolean re = false;
        if (parentCode.equals(leaf.getParentCode()))
            re = leaf.update();
        else
            re = leaf.updateParent(parentCode);
        return re;
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String direction = ParamUtil.get(request, "direction", false);
        if (code == null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_need_code"));
        }
        if ( direction == null ) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Directory", "err_need_direction"));
        }

        Leaf lf = new Leaf(code);
        return lf.move(direction);
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
        return leaf.getChildren();
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

    /**
     * 修复因为BUG或者误操作致树形结构被破坏的问题
     */
    public void repairLeaf(Leaf lf) {
        Vector children = lf.getChildren();
        // 重置孩子节点数
        lf.setChildCount(children.size());
        Iterator ir = children.iterator();
        int orders = 1;
        while (ir.hasNext()) {
            Leaf lfch = (Leaf)ir.next();
            // 重置孩子节点的排列顺序
            lfch.setOrders(orders);
            // System.out.println(getClass() + " leaf name=" + lfch.getName() + " orders=" + orders);

            lfch.update();
            orders ++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = lf.getParentCode();
        if (lf.getCode().equals(lf.CODE_ROOT)) {
            layer = 1;
        }
        else {
            if (parentCode.equals(lf.CODE_ROOT))
                layer = 2;
            else {
                while (!parentCode.equals(lf.CODE_ROOT)) {
                    // System.out.println(getClass() + "leaf parentCode=" + parentCode);
                    Leaf parentLeaf = getLeaf(parentCode);
                    if (parentLeaf == null || !parentLeaf.isLoaded())
                        break;
                    else {
                        parentCode = parentLeaf.getParentCode();
                    }
                    layer++;
                }
            }
        }
        lf.setLayer(layer);
        lf.update();
    }

    // 修复根结点为leaf的树
    public void repairTree(Leaf leaf) throws Exception {
        // System.out.println(getClass() + "leaf name=" + leaf.getName());
        repairLeaf(leaf);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            repairTree(childlf);
        }
        // 刷新缓存
        leaf.removeAllFromCache();
    }
}
