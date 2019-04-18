package com.redmoon.oa.flow;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.pvg.*;
import org.apache.log4j.*;

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
            logger.info("Directory:默认数据库名不能为空");
    }

    public synchronized boolean AddChild(HttpServletRequest request) throws
            ErrMsgException {
        // 取出被回复的贴子的有关信息
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0;
        String root_code = "", name = "", code = "", parent_code = "";

        name = ParamUtil.get(request, "name").trim();
        if (name == null || name.equals(""))
            throw new ErrMsgException("名称不能为空！");
        code = ParamUtil.get(request, "code").trim();
        if (code.equals(""))
            throw new ErrMsgException("编码不能为空！");
        dirCode = code;

        parent_code = ParamUtil.get(request, "parent_code").trim();
        if (parent_code.equals(""))
            throw new ErrMsgException("父结点不能为空！");
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type");
        if (!parent_code.equals(Leaf.CODE_ROOT)) {
        	if (type==Leaf.TYPE_NONE) {
        		throw new ErrMsgException("类型：分类 仅支持在一级目录中设置！");
        	}
        }
        
        String pluginCode = ParamUtil.get(request, "pluginCode");
        String formCode = ParamUtil.get(request, "formCode");
        if (formCode.equals("") && (type!=Leaf.TYPE_NONE))
            throw new ErrMsgException("请选择表单！");
        String dept = ParamUtil.get(request, "depts");
        boolean open = ParamUtil.getInt(request, "isOpen", 1)==1;
        
        boolean debug = ParamUtil.getInt(request, "isDebug", 0)==1;
        boolean isMobileStart = ParamUtil.getInt(request, "isMobileStart", 0)==1;
        boolean isMobileLocation = ParamUtil.getInt(request, "isMobileLocation", 0)==1;
        boolean isMobileCamera = ParamUtil.getInt(request, "isMobileCamera", 0)==1;

        long queryId = ParamUtil.getLong(request, "queryId", Leaf.QUERY_NONE);
        String queryRole = ParamUtil.get(request, "queryRole");
        String queryCondMap = ParamUtil.get(request, "queryCondMap");
        String params = ParamUtil.get(request, "params");
        
        Leaf lf = new Leaf();
        lf = lf.getLeaf(code);
        if (lf!=null && lf.isLoaded())
            throw new ErrMsgException("已存在相同编码的节点：" + lf.getName());

        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(parent_code);
        if (!lp.canUserSee(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
        }
        
        String unitCode = ParamUtil.get(request, "unitCode");
        if (unitCode.equals(""))
        	unitCode = privilege.getUserUnitCode(request);
        
        if (!unitCode.equals(Leaf.UNIT_CODE_PUBLIC)) {
	        DeptDb dd = new DeptDb();
	        dd = dd.getDeptDb(unitCode);
	        if (dd.getType()!=DeptDb.TYPE_UNIT) {
	        	throw new ErrMsgException("请选择单位！");
	        }
        }
        
        int templateId = ParamUtil.getInt(request, "templateId", -1);

        lf = new Leaf();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setPluginCode(pluginCode);
        lf.setFormCode(formCode);
        lf.setDept(dept);
        lf.setOpen(open);
        lf.setUnitCode(unitCode);
        lf.setDebug(debug);
        lf.setMobileStart(isMobileStart);
        
        lf.setMobileLocation(isMobileLocation);
        lf.setMobileCamera(isMobileCamera);
        
        lf.setQueryId(queryId);
        lf.setQueryRole(queryRole);
        lf.setQueryCondMap(queryCondMap);
        lf.setTemplateId(templateId);
        lf.setParams(params);

        Leaf leaf = getLeaf(parent_code);
        boolean re = leaf.AddChild(lf);
        if (re) {
        	// 如果是分类节点,则赋予Everyone查看的权限
        	if (true) { // lf.getType()==Leaf.TYPE_NONE) {
        		lp.setDirCode(lf.getCode());
        		// lp.add(UserGroupDb.EVERYONE, LeafPriv.TYPE_USERGROUP);
        		lp.add(RoleDb.CODE_MEMBER, LeafPriv.TYPE_ROLE);
        	}
        }
        return re;
    }

    public synchronized void del(HttpServletRequest request, String delcode) throws ErrMsgException {
        WorkflowDb wfd = new WorkflowDb();
        int count = wfd.getWorkflowCountOfType(delcode);
        if (count>0) {
            throw new ErrMsgException("节点 " + delcode + " 下已有流程 " + count + " 个，不能被删除！");
        }

        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(delcode);
        if (!lp.canUserSee(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
        }

        Leaf lf = getLeaf(delcode);
        if (lf==null)
            throw new ErrMsgException("节点 " + delcode + " 已不存在！");
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
            throw new ErrMsgException("名称不允许为空！");
        }
        int templateId = ParamUtil.getInt(request, "templateId", -1);
        String parentCode = ParamUtil.get(request, "parentCode");
        String pluginCode = ParamUtil.get(request, "pluginCode");
        String formCode = ParamUtil.get(request, "formCode");
        String dept = ParamUtil.get(request, "depts");
        
        boolean open = ParamUtil.getInt(request, "isOpen", 1)==1;
        boolean debug = ParamUtil.getInt(request, "isDebug", 0)==1;
        boolean isMobileStart = ParamUtil.getInt(request, "isMobileStart", 0)==1;
        boolean isMobileLocation = ParamUtil.getInt(request, "isMobileLocation", 0)==1;
        boolean isMobileCamera = ParamUtil.getInt(request, "isMobileCamera", 0)==1;

        long queryId = ParamUtil.getLong(request, "queryId", Leaf.QUERY_NONE);
        String queryRole = ParamUtil.get(request, "queryRole");
        String queryCondMap = ParamUtil.get(request, "queryCondMap");
        String params = ParamUtil.get(request, "params");

        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(code);
        if (!lp.canUserSee(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
        }

        Leaf leaf = getLeaf(code);//new Leaf();

        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            Leaf lf = getLeaf(parentCode); // 取得新的父节点
            while (lf!=null && !lf.getCode().equals(lf.CODE_ROOT)) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode()))
                    throw new ErrMsgException("不能将其子节点更改为父节点");
                lf = getLeaf(pCode);
            }
        }
        
        String unitCode = ParamUtil.get(request, "unitCode");
        if (unitCode.equals(""))
        	unitCode = privilege.getUserUnitCode(request);
        if (!unitCode.equals(Leaf.UNIT_CODE_PUBLIC)) {
	        DeptDb dd = new DeptDb();
	        dd = dd.getDeptDb(unitCode);
	        if (dd.getType()!=DeptDb.TYPE_UNIT) {
	        	throw new ErrMsgException("请选择单位！");
	        }
        }
        
        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        leaf.setType(type);
        leaf.setTemplateId(templateId);
        leaf.setPluginCode(pluginCode);
        leaf.setFormCode(formCode);
        leaf.setDept(dept);
        leaf.setOpen(open);
        leaf.setUnitCode(unitCode);
        leaf.setDebug(debug);
        leaf.setMobileStart(isMobileStart);
        
        leaf.setMobileLocation(isMobileLocation);
        leaf.setMobileCamera(isMobileCamera);
        
        leaf.setQueryId(queryId);
        leaf.setQueryRole(queryRole);
        leaf.setQueryCondMap(queryCondMap);
        leaf.setParams(params);
        
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

        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(code);
        if (!lp.canUserSee(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
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

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public String getDirCode() {
        return dirCode;
    }

    private String dirCode;


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
            Leaf lfch = (Leaf) ir.next();
            // 重置孩子节点的排列顺序
            lfch.setOrders(orders);
            // System.out.println(getClass() + " leaf name=" + lfch.getName() + " orders=" + orders);

            lfch.update();
            orders++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = lf.getParentCode();
        if (lf.getCode().equals(lf.CODE_ROOT)) {
            layer = 1;
        } else {
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
