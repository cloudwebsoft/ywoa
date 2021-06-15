package com.redmoon.blog;

import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import java.util.Vector;
import java.util.Iterator;

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

public class Directory {
    String connname = "";
    Logger logger = Logger.getLogger(Directory.class.getName());

    public Directory() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:conname is empty");
    }

    public boolean AddChild(HttpServletRequest request) throws
            ErrMsgException {
        //取出被回复的贴子的有关信息
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0;
        String root_code = "", name = "", code = "", parent_code = "";

        name = ParamUtil.get(request, "name").trim();
        if (name == null || name.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_name_empty"));
        code = ParamUtil.get(request, "code").trim();
        if (code.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_code_empty"));
        if (code.equals("not"))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_code_not"));
        if (!StrUtil.isSimpleCode(code))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_simple_code")); // 编码请使用字母、数字或者符号 _ -！
            // throw new ErrMsgException("编码请使用字母、数字结合符号_-！");

        parent_code = ParamUtil.get(request, "parent_code").trim();
        if (parent_code.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_parent_code_empty"));
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type");
        String pluginCode = ParamUtil.get(request, "pluginCode");

        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        if (lf!=null && lf.isLoaded())
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_same_code") + lf.getName()); // 已存在相同编码的节点
        lf = new Leaf();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setPluginCode(pluginCode);

        Leaf leaf = getLeaf(parent_code);
        return leaf.AddChild(lf);
    }

    public void del(String delcode) throws ErrMsgException {
        Leaf lf = getLeaf(delcode);
        lf.del(lf);
    }

    public synchronized boolean update(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String name = ParamUtil.get(request, "name", false);
        String description = ParamUtil.get(request, "description");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        int type = ParamUtil.getInt(request, "type");
        if (code == null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_need_code"));
        }
        if (name==null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_need_name"));
        }

        int templateId = ParamUtil.getInt(request, "templateId");
        String parentCode = ParamUtil.get(request, "parentCode");
        String pluginCode = ParamUtil.get(request, "pluginCode");

        Leaf leaf = getLeaf(code);//new Leaf();

        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            Leaf lf = getLeaf(parentCode); // 取得新的父节点
            while (lf!=null && !lf.getCode().equals(lf.ROOTCODE)) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode()))
                    throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_parent_code"));
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
        boolean re = false;
        if (parentCode.equals(leaf.getParentCode()))
            re = leaf.update();
        else
            re = leaf.update(parentCode);

        return re;
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String direction = ParamUtil.get(request, "direction", false);
        if (code == null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_need_code"));
        }
        if ( direction == null ) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.blog.Directory", "err_need_direction"));
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
        return leaf.getChildren();
    }
}
