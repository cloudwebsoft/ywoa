package com.redmoon.oa.fileark;

import java.sql.*;

import javax.servlet.http.*;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.util.BeanShellUtil;

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
    
    String code;

    /**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

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

        String insertsql = "insert into directory (code,name,parent_code,description,orders,root_code,child_count,layer) values (";
        insertsql += StrUtil.sqlstr(code) + "," + StrUtil.sqlstr(name) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr(description) + "," +
                orders + "," + StrUtil.sqlstr(root_code) + "," +
                child_count + ",1)";

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

    @Override
    public boolean AddChild(HttpServletRequest request) throws
            ErrMsgException {
        // 取出被回复的贴子的有关信息
        int child_count = 0, orders = 1, parent_orders = 1,
                islocked = 0;
        String root_code = "", name = "", parent_code = "";

        name = ParamUtil.get(request, "name").trim();
        if (name == null || name.equals("")) {
            throw new ErrMsgException("名称不能为空！");
        }
        code = ParamUtil.get(request, "code").trim();
        if (code.equals("")) {
        	code = RandomSecquenceCreator.getId(20);
            // throw new ErrMsgException("编码不能为空！");
        }
        if (!StrUtil.isSimpleCode(code)) {
            throw new ErrMsgException("编码请使用字母、数字、-或_！");
        }

        parent_code = ParamUtil.get(request, "parent_code").trim();
        if (parent_code.equals("")) {
            throw new ErrMsgException("父节点不能为空！");
        }
        //判断同级目录下子目录名称不能重复
        Directory dir = new Directory();
        Vector children = dir.getChildren(parent_code);
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
        	Leaf childlf = (Leaf) ri.next();
        	String eachName = childlf.getName();
        	if(eachName.equals(name)){
        		throw new ErrMsgException("请检查名称是否有重复！");
        	}
        }
        
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type");
        String pluginCode = ParamUtil.get(request, "pluginCode");
        boolean isSystem = ParamUtil.get(request, "isSystem").equals("1") ? true : false;
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        String target = ParamUtil.get(request, "target");
        
        boolean isShow = ParamUtil.getInt(request, "isShow", 1)==1;
        boolean isOfficeNTKOShow = ParamUtil.getInt(request, "isOfficeNTKOShow", 0)==1;
        boolean islog = ParamUtil.getInt(request, "isLog")==1;
        boolean isFulltext = ParamUtil.getInt(request, "isFulltext", 0)==1;
        boolean isExamine = ParamUtil.getInt(request, "isExamine", 0)==1;
        boolean isCopyable = ParamUtil.getInt(request, "isCopyable", 0)==1;

        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(parent_code);
        if (!lp.canUserExamine(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

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
        lf.setShow(isShow);
        lf.setOfficeNTKOShow(isOfficeNTKOShow);
        lf.setLog(islog);
        lf.setFulltext(isFulltext);
        lf.setExamine(isExamine);
        lf.setCopyable(isCopyable);

        Leaf leaf = getLeaf(parent_code);
        boolean re = leaf.AddChild(lf);
        if (re) {
        	// 如果是文章型节点,则创建文章
        	if (lf.getType()==Leaf.TYPE_DOCUMENT) {
        		Document doc = new Document();
        		Privilege pvg = new Privilege();
        		doc.getIDOrCreateByCode(code, pvg.getUser(request));
        	}
        }        
        return re;
    }

    @Override
    public void del(HttpServletRequest request, String delcode) throws ErrMsgException {
        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(delcode);
        if (!lp.canUserExamine(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "pvg_invalid"));
        }
        Leaf lf = getLeaf(delcode);
        if (lf == null) {
            throw new ErrMsgException("节点 " + delcode + " 已被删除！");
        }
        lf.del(lf);
    }

    @Override
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
        boolean isShow = ParamUtil.getInt(request, "isShow", 1)==1;
        boolean isOfficeNTKOShow = ParamUtil.getInt(request, "isOfficeNTKOShow", 0)==1;
        boolean islog = ParamUtil.getInt(request, "isLog")==1;
        boolean isFulltext = ParamUtil.getInt(request, "isFulltext", 0)==1;
        boolean isExamine = ParamUtil.getInt(request, "isExamine", 0)==1;
        String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
        boolean isCopyable = ParamUtil.getInt(request, "isCopyable", 0)==1;

        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(code);
        if (!lp.canUserExamine(privilege.getUser(request))) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        }

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
        leaf.setShow(isShow);
        leaf.setOfficeNTKOShow(isOfficeNTKOShow);
        leaf.setLog(islog);
        leaf.setFulltext(isFulltext);
        leaf.setExamine(isExamine);
        leaf.setFlowTypeCode(flowTypeCode);
        leaf.setCopyable(isCopyable);
        boolean re = false;
        if (leaf.getCode().equals(Leaf.ROOTCODE) || parentCode.equals(leaf.getParentCode())) {
            re = leaf.update();
        } else {
            re = leaf.update(parentCode);
        }

        return re;
    }

    @Override
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
        if (!lp.canUserExamine(privilege.getUser(request))) {
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
    @Override
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

    public Vector<Leaf> getChildren(String code) throws ErrMsgException {
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
        if (lf.getCode().equals(Leaf.ROOTCODE)) {
            layer = 1;
        }
        else {
            if (parentCode.equals(Leaf.ROOTCODE))
                layer = 2;
            else {
                while (!parentCode.equals(Leaf.ROOTCODE)) {
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
    
    /**
     * 
     * @param doc
     * @param attIds
     * @param userName
     * @param isZip 如果为true，则attIds为null
     * @return
     * @throws ErrMsgException
     */
    public static String onDownloadValidate(HttpServletRequest request, String ids, String userName, boolean isZip) throws ErrMsgException {
        // 执行验证脚本
    	boolean ret = false;
    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(Leaf.ROOTCODE);
    	String info = null;
		String script = lf.getScript(Leaf.SCRIPTS_DOWNLOAD_VALIDATE);
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();
				// 赋值用户
				sb.append("userName=\"" + userName + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("ids", ids);
				bsh.set("isZip", isZip);
				bsh.set("request", request);

				bsh.eval(script);
				Object obj = bsh.get("ret");
				if (obj != null) {
					ret = ((Boolean) obj).booleanValue();
					info = (String)bsh.get("info");					
					if (!ret) {
						String errMsg = (String) bsh.get("errMsg");
						if (errMsg != null)
							throw new ErrMsgException(errMsg);
						else {
							throw new ErrMsgException("验证非法，但errMsg为空！");
						}
					}
				} else {
					// 需要判断action进行检测，因为delete事件中可能要验证，而当创建验证时，该情况下脚本中未写ret值
					// throw new ErrMsgException("该节点脚本中未配置ret=...");
				}
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}     
		return info;
    }
    
    public static boolean onDownload(HttpServletRequest request, String ids, String userName, boolean isZip) throws ErrMsgException {
        // 执行验证脚本
    	boolean ret = false;
    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(Leaf.ROOTCODE);  	
		String script = lf.getScript(Leaf.SCRIPTS_DOWNLOAD);
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();
				// 赋值用户
				sb.append("userName=\"" + userName + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("ids", ids);
				bsh.set("request", request);
				bsh.set("isZip", isZip);
				bsh.eval(script);

/*				String errMsg = (String) bsh.get("errMsg");
				if (errMsg != null)
					throw new ErrMsgException(errMsg);*/
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}     
		return ret;
    }    
}
