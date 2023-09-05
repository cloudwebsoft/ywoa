package com.redmoon.oa.address;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;

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

    public Directory() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空");
    }

    public boolean AddChild(HttpServletRequest request) throws
            ErrMsgException, UnsupportedEncodingException {
        // 取出被回复的贴子的有关信息
        String name = "", code = "", parent_code = "";

        name = ParamUtil.get(request, "name").trim();
        if (name == null || name.equals("")) {
            throw new ErrMsgException("名称不能为空！");
        }
        code = ParamUtil.get(request, "code").trim();
        if ("".equals(code)){
        	code = (String)request.getAttribute("code");
        	if (code == null || code.equals("")){
        		throw new ErrMsgException("编码不能为空！");
        	}
        }
        parent_code = URLDecoder.decode(ParamUtil.get(request, "parent_code"),"utf-8");
        if (parent_code.equals("")) {
            throw new ErrMsgException("父结点不能为空！");
        }
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type", 0);
        double percent = ParamUtil.getDouble(request, "percent", 0.0);
        String roleCode = ParamUtil.get(request, "roleCode");

        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        if (lf!=null && lf.isLoaded())
            throw new ErrMsgException("已存在相同编码的节点：" + lf.getName());
        lf = new Leaf();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        
        Privilege pvg = new Privilege();
        String userName = pvg.getUser(request);
        String unitCode = pvg.getUserUnitCode(request);
        
        lf.setUserName(userName);
        lf.setUnitCode(unitCode);

        Leaf leaf = getLeaf(parent_code);

        boolean re = leaf.AddChild(lf);

        return re;
    }

    public void del(String delcode) throws ErrMsgException {
        Leaf lf = getLeaf(delcode);
        if (lf!=null)
        	lf.del(lf);
    }

    public synchronized boolean update(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code");
        String name = ParamUtil.get(request, "name");
        String description = ParamUtil.get(request, "description");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        int type = ParamUtil.getInt(request, "type");
        if (code.equals("") || name.equals("")) {
            throw new ErrMsgException("编码和名称项必填！");
        }
        int templateId = ParamUtil.getInt(request, "templateId");
        String parentCode = ParamUtil.get(request, "parentCode");
        String pluginCode = ParamUtil.get(request, "pluginCode");

        double percent = ParamUtil.getDouble(request, "percent");
        String roleCode = ParamUtil.get(request, "roleCode");
        // LogUtil.getLog(getClass()).info("update: kind=" + kind);
        double scoreScope = ParamUtil.getDouble(request, "scoreScope", -1);
        if (scoreScope==-1)
            throw new ErrMsgException("分值无效，必须为大于0的数字！");
        Leaf leaf = getLeaf(code);//new Leaf();
        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        leaf.setType(type);

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
        if (code == null || direction == null) {
            throw new ErrMsgException("编码与方向项必填！");
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

            lfch.update();
            orders ++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = lf.getParentCode();
        if (lf.getCode().equals(lf.getRootCode())) {
            layer = 1;
        }
        else {
            if (parentCode.equals(lf.getRootCode()))
                layer = 2;
            else {
                while (!parentCode.equals(lf.getRootCode())) {
                    // LogUtil.getLog(getClass()).info(getClass() + "leaf parentCode=" + parentCode);
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
        // LogUtil.getLog(getClass()).info(getClass() + "leaf name=" + leaf.getName());
        repairLeaf(leaf);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            repairTree(childlf);
        }
        // 刷新缓存
        leaf.removeAllFromCache();
    }
}
