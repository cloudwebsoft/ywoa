package com.redmoon.oa.basic;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
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
 * @author not attributable
 * @version 1.0
 */

public class TreeSelectMgr {
    String connname = "";
    private String code = "";

    public TreeSelectMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals("")) {
            LogUtil.getLog(getClass()).info("Directory:默认数据库名不能为空");
        }
    }

    public boolean addChild(HttpServletRequest request) throws
            ErrMsgException {
        String name = "", parent_code = "";

        name = ParamUtil.get(request, "name", false);
        if (name == null) {
            throw new ErrMsgException("名称不能为空！");
        }
        // 指定编码
        code = ParamUtil.get(request, "code").trim();
        if ("".equals(code)) {
            // 编码自动生成
            code = FileUpload.getRandName();
            // throw new ErrMsgException("编码不能为空！");
        }

        if (!StrUtil.isSimpleCode(code)) {
            throw new ErrMsgException("编码请使用字母、数字、-或_！");
        }

        parent_code = ParamUtil.get(request, "parentCode").trim();
        if ("".equals(parent_code)) {
            throw new ErrMsgException("父结点不能为空！");
        }
        
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type",0);

        String link = ParamUtil.get(request, "link");
        String preCode = ParamUtil.get(request, "preCode");
        String formCode = ParamUtil.get(request, "formCode");
        if ("flow".equals(preCode)) {
        	formCode = ParamUtil.get(request, "flowTypeCode");
        }
        boolean isOpen = ParamUtil.getInt(request, "isOpen", 1) == 1;
        boolean isContextMenu = ParamUtil.getInt(request, "isContextMenu", 1) == 1;
        String metaData = ParamUtil.get(request, "metaData");

        TreeSelectDb plf = getTreeSelectDb(parent_code);
        if (!plf.isLoaded()) {
            throw new ErrMsgException("父节点不存在!");
        }
        
        TreeSelectDb lf = new TreeSelectDb();
        lf = lf.getTreeSelectDb(code);
        if (lf.isLoaded()) {
        	throw new ErrMsgException("相同编码的节点已存在!");
        } else {
            lf = new TreeSelectDb();
        }

        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        
        lf.setLink(link);
        lf.setPreCode(preCode);
        lf.setFormCode(formCode);
        lf.setOpen(isOpen);
        lf.setContextMenu(isContextMenu);
        lf.setMetaData(metaData);
        
        return plf.AddChild(lf);
    }

    public void del(String delcode) throws ErrMsgException {
        TreeSelectDb lf = getTreeSelectDb(delcode);
        if (!lf.isLoaded()) {
            throw new ErrMsgException("节点不存在!");
        }
        lf.del(lf);
    }

    public synchronized boolean update(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String name = ParamUtil.get(request, "name", false);
        String description = ParamUtil.get(request, "description");
        boolean isHome = "true".equals(ParamUtil.get(request, "isHome"));
        int type = ParamUtil.getInt(request, "type",0);
        if (code == null || name == null) {
            throw new ErrMsgException("code与name项必填");
        }
        String parentCode = ParamUtil.get(request, "parentCode");
        if ("#".equals(parentCode)){
        	parentCode = "-1";
        }

        String link = ParamUtil.get(request, "link");
        String preCode = ParamUtil.get(request, "preCode");
        String formCode = ParamUtil.get(request, "formCode");
        if ("flow".equals(preCode)) {
        	formCode = ParamUtil.get(request, "flowTypeCode");
        }
        boolean isOpen = ParamUtil.getInt(request, "isOpen", 1)==1;
        boolean isContextMenu = ParamUtil.getInt(request, "isContextMenu", 1) == 1;
        String metaData = ParamUtil.get(request, "metaData");

        TreeSelectDb leaf = getTreeSelectDb(code);
        if (code.equals(parentCode)) {
            throw new ErrMsgException("请选择正确的父节点！");
        }
        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            TreeSelectDb lf = getTreeSelectDb(parentCode); // 取得新的父节点
            while (lf!=null && lf.isLoaded() && !lf.getCode().equals(lf.getRootCode())) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode())) {
                    throw new ErrMsgException("不能将其子节点更改为父节点");
                }
                lf = getTreeSelectDb(pCode);
            }
        }

        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        leaf.setType(type);
        
        leaf.setLink(link);
        leaf.setPreCode(preCode);
        leaf.setFormCode(formCode);
        leaf.setOpen(isOpen);
        leaf.setContextMenu(isContextMenu);
        leaf.setMetaData(metaData);
        
        boolean re = false;
        if (parentCode.equals(leaf.getParentCode())) {
            LogUtil.getLog(getClass()).info("update:name=" + name);
            re = leaf.save();
        }
        else {
            re = leaf.save(parentCode);
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

        TreeSelectDb dd = getTreeSelectDb(code);
        return dd.move(direction);
    }

    public TreeSelectDb getTreeSelectDb(String code) {
        TreeSelectDb dd = new TreeSelectDb();
        return dd.getTreeSelectDb(code);
    }

    public TreeSelectDb getBrother(String code, String direction) throws
            ErrMsgException {
        TreeSelectDb dd = getTreeSelectDb(code);
        return dd.getBrother(direction);
    }

    public Vector<TreeSelectDb> getChildren(String code) throws ErrMsgException {
        TreeSelectDb dd = getTreeSelectDb(code);
        return dd.getChildren();
    }


    /**
     * 修复因为BUG或者误操作致树形结构被破坏的问题
     */
    public void repairLeaf(TreeSelectDb lf) {
        Vector children = lf.getChildren();
        // 重置孩子节点数
        lf.setChildCount(children.size());
        Iterator ir = children.iterator();
        int orders = 1;
        while (ir.hasNext()) {
            TreeSelectDb lfch = (TreeSelectDb)ir.next();
            // 重置孩子节点的排列顺序
            lfch.setOrders(orders);

            lfch.save();
            orders ++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = lf.getParentCode();
        if (lf.getCode().equals(lf.getRootCode())) {
            layer = 1;
        }
        else {
            if (parentCode.equals(lf.getRootCode())) {
                layer = 2;
            } else {
                while (!parentCode.equals(lf.getRootCode())) {
                    TreeSelectDb parentLeaf = getTreeSelectDb(parentCode);
                    if (parentLeaf == null || !parentLeaf.isLoaded()) {
                        break;
                    } else {
                        parentCode = parentLeaf.getParentCode();
                    }
                    layer++;
                }
            }
        }
        lf.setLayer(layer);
        lf.save();
    }

    // 修复根结点为leaf的树
    public void repairTree(TreeSelectDb leaf) throws Exception {
        repairLeaf(leaf);
        Vector children = getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            TreeSelectDb childlf = (TreeSelectDb) ri.next();
            repairTree(childlf);
        }
        // 刷新缓存
        TreeSelectCache dc = new TreeSelectCache();
        dc.removeAllFromCache();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

