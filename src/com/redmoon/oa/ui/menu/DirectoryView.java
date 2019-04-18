package com.redmoon.oa.ui.menu;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.opensymphony.xwork2.util.logging.LoggerUtils;
import com.redmoon.oa.basic.SelectKindDb;
import com.redmoon.oa.basic.SelectKindPriv;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.flow.LeafPriv;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserSet;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.ui.Skin;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.visual.ModulePrivDb;
import com.redmoon.oa.visual.ModuleSetupDb;

public class DirectoryView {
    Logger logger = Logger.getLogger(Leaf.class.getName());
    Leaf rootLeaf;
    Vector UprightLineNodes = new Vector(); //用于显示竖线
    HttpServletRequest request;
    String skinPath;

    public DirectoryView(HttpServletRequest request, Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
        this.request = request;

        String skincode = UserSet.getSkin(request);
        if (skincode == null || skincode.equals("")) skincode = UserSet.
                defaultSkin;
        SkinMgr skm = new SkinMgr();
        Skin skin = skm.getSkin(skincode);
        skinPath = skin.getPath();
    }

    public void ListSimple(JspWriter out, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeSimple(out, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    void ListTreeSimple(JspWriter out, Leaf leaf,
                        boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws
            Exception {
        if (leaf.getCode().equals(Leaf.CODE_BOTTOM))
            return;
        if (leaf.canUserSee(request))
            ShowLeafSimple(out, leaf, isLastChild, target, link, tableClass, tableClassMouseOn);

        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0)
            out.print("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        Iterator ri = children.iterator();
        //写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            if (childlf.canUserSee(request))
                ListTreeSimple(out, childlf, isLastChild1, target, link, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    @SuppressWarnings("unchecked")
	void ShowLeafSimple(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();

        if (!isLastChild) {
            Leaf brotherleaf = leaf.getBrother("down");
            // System.out.println("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) {
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    // System.out.println(leaf.getCode() + " layer=" + layer +
                    //                   " count=" + count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();
        // System.out.println(code + " childcount=" + childcount);

        String tableid = "table" + leaf.getCode();

        out.println("<table id=" + tableid + " name=" + tableid + " class='" + tableClass + "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='" + tableClassMouseOn + "'\" onMouseOut=\"this.className='" + tableClass + "'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td height='13' align=left nowrap>");
        // for (int k = 1; k <= layer - 1; k++) {
        for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) {
            boolean isShowed = false;
            Iterator ir = UprightLineNodes.iterator();
            while (ir.hasNext()) {
                UprightLineNode node = (UprightLineNode) ir.next();
                //如果在K层上存在一个竖线结点则画出
                if (node.getLayer() == k) {
                    node.show(out, "images/i_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //System.out.println("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='" + request.getContextPath() + "/images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle' height='30px'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle' height='30px'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle' height='30px;'><img src='images/folder_01.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle' height='30px'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle' height='30px;'><img src='images/folder_01.gif' align='absmiddle'>");
            }
        }

        // 三种类型节点，用同一个Link
        if (link.indexOf("javascript")==0)
            ;
        else {
            if (link.indexOf("?") != -1)
                link += "&dir_code=" + StrUtil.UrlEncode(code);
            else
                link += "?dir_code=" + StrUtil.UrlEncode(code);
        }
        String codeStr = "";
        // if (childcount==0)
            codeStr = " code='" + code + "'";
        out.print(
                    "<a target='" + target + "' href='" + link + "'" + codeStr + ">" + name + "</a>");
        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    public void list(JspWriter out) throws Exception {
        ListTree(out, rootLeaf, true);
    }

    // 显示根结点为leaf的树
    void ListTree(JspWriter out, Leaf leaf,
                  boolean isLastChild) throws Exception {
        ShowLeaf(out, leaf, isLastChild);
/*
        // 初始化权限数据库
        LeafPriv lp = new LeafPriv();
        lp.setDirCode(leaf.getCode());
        try {
            lp.add("Everyone", lp.TYPE_USERGROUP);
        }
        catch (ErrMsgException e) {
        }
*/
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0)
            out.print("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        Iterator ri = children.iterator();
        //写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTree(out, childlf, isLastChild1);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeaf(JspWriter out, Leaf leaf,
                  boolean isLastChild) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String link = leaf.getLink();

        if (!isLastChild) {
            // System.out.println("get leaf brother" + leaf.getName());
            Leaf brotherleaf = leaf.getBrother("down");
            // System.out.println("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) { // =0的也计入的话会在树底端的结点产生多余竖线
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    // System.out.println(leaf.getCode() + " layer=" + layer +
                    //                   " count=" + count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();
        // System.out.println(code + " childcount=" + childcount);

        String tableid = "table" + leaf.getCode();

        out.println("<table id=" + tableid + " name=" + tableid + " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" style='border-bottom-color:#e9e9e9;border-bottom-width:1px;border-bottom-style:solid'>");
        out.println("    <tbody><tr>");
        out.println("        <td width='85%' height='13' align=left nowrap>");
        // for (int k = 1; k <= layer - 1; k++) {
        for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) { // 不用上一行，是因为上一行会产生多余的空格
            boolean isShowed = false;
            Iterator ir = UprightLineNodes.iterator();
            while (ir.hasNext()) {
                UprightLineNode node = (UprightLineNode) ir.next();
                //如果在K层上存在一个竖线结点则画出
                if (node.getLayer() == k) {
                    node.show(out, "images/i_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //System.out.println("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='" + request.getContextPath() + "/images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
        }

        String icon = leaf.getIcon();
        if (!icon.equals("")) {
            icon = "<img src='" + request.getContextPath() + "/" + skinPath + "/icons/" + icon + "' align='absmiddle'>";
        }
        else {
            icon = "<img src='images/folder_01.gif' align='absmiddle'>";
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle' height='30px'>" + icon);
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle' height='30px'>" + icon);
                else
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle' height='30px;'>" + icon);
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle' height='30px'>" + icon);
                else
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle' height='30px;'>" + icon);
            }
        }

        if (leaf.getType()==Leaf.TYPE_LINK) {
            if (leaf.getLink().equals("")) {
                out.print("<a class='link' target=_self href='#'>" + leaf.getName(request) + "</a>");
            }
            else
                out.print("<a class='link' target=_blank href='" + Global.getRootPath() + "/" + leaf.getLink(request) + "'>" + leaf.getName(request) + "</a>");
        }
        else if (leaf.getType()==Leaf.TYPE_PRESET) {
            out.print("<a class='pre' target=_blank href='" + Global.getRootPath() + "/" + leaf.getLink(request) + "'>" + leaf.getName(request) + "</a>");
        }
        else if (leaf.getType()==Leaf.TYPE_MODULE) {
            out.print("<a class='module' target=_blank href='" + Global.getRootPath() + "/visual/module_list.jsp?code=" + leaf.getFormCode() + "'>" + leaf.getName(request) + "</a>");
        }
        else
            out.print(leaf.getName(request));
        if (leaf.isNav() && leaf.getLayer()==2) {
            out.print("&nbsp;(导航)");
        }

        out.print("     </td><td width='15%' align=right nowrap>");
        if (!leaf.getCode().equals("root")) {
            if (leaf.isUse())
                out.print("启用&nbsp;");
            else
                out.print("<font color='red'>停用</font>&nbsp;");
            if (leaf.getLayer()<=3) {
                out.print(
                        "<a target=dirbottomFrame href='menu_bottom.jsp?parent_code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                        StrUtil.UrlEncode(name, "utf-8") +
                        "&op=AddChild'>添子菜单</a>&nbsp;");
            }
            if(!leaf.getSystem()){     //如果是系统默认的菜单隐藏修改按钮
	            out.print(
	                    "<a target='dirbottomFrame' href='menu_bottom.jsp?op=modify&code=" +
	                    StrUtil.UrlEncode(code) + "&parent_name=" +
	                    StrUtil.UrlEncode(name) + "&link=" +
	                    StrUtil.UrlEncode(link) + "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_modify") + "</a>&nbsp;");
            }
            else if (License.getInstance().isBiz() &&code.equalsIgnoreCase("344394775")){
            	 out.print(
 	                    "<a target='dirbottomFrame' href='menu_bottom.jsp?op=modify&code=" +
 	                    StrUtil.UrlEncode(code) + "&name=" +
 	                    StrUtil.UrlEncode(name) + "&link=" +
 	                    StrUtil.UrlEncode(link) + "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_modify") + "</a>&nbsp;");
            }
            if (!code.equals(Leaf.CODE_BOTTOM)&&!leaf.getSystem()) {    //如果是系统默认的菜单隐藏删除按钮
                out.print(
                        "<a target=_self href=# onClick=\"jConfirm('" +
                        SkinUtil.LoadString(request, "res.forum.DirectoryView",
                                            "confirm_del") +
                        "','提示',function(r){ if(!r){return;}else{window.location.href='menu_top.jsp?op=del&delcode=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'}}) \">" +
                        SkinUtil.
                        LoadString(request, "res.forum.DirectoryView", "link_del") +
                        "</a>&nbsp;");
            }
            else  if (License.getInstance().isBiz() && code.equalsIgnoreCase("344394775")){
            	out.print(
                        "<a target=_self href=# onClick=\"jConfirm('" +
                        SkinUtil.LoadString(request, "res.forum.DirectoryView",
                                            "confirm_del") +
                        "','提示',function(r){ if(!r){return;}else{window.location.href='menu_top.jsp?op=del&delcode=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'}}) \">" +
                        SkinUtil.
                        LoadString(request, "res.forum.DirectoryView", "link_del") +
                        "</a>&nbsp;");
           }
            out.print("<a href='menu_top.jsp?op=move&direction=up&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_move_up") + "</a>&nbsp;");
            out.print("<a href='menu_top.jsp?op=move&direction=down&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_move_down") + "</a>&nbsp;");
        }
        out.println("  </td></tr></tbody></table>");
    }

    void ShowLeafAsOption(JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
        }
        else {
            out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
        }
    }

    public void ShowLeafAsOptionToString(StringBuffer sb, Leaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            sb.append("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
        }
        else {
            sb.append("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptions(JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        ShowLeafAsOption(out, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptions(out, childlf, rootlayer);
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptionsToString(StringBuffer sb, Leaf leaf, int rootlayer) throws Exception {
        ShowLeafAsOptionToString(sb, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptionsToString(sb, childlf, rootlayer);
        }
    }

    /**
     * 把列表或无内容显示为蓝色
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    void ShowLeafAsOptionWithCode(JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        out.print("<option value='" + code + "'>" + blank + "╋ " + name + "</option>");
    }

    // 显示根结点为leaf的树，value中全为code
    public void ShowDirectoryAsOptionsWithCode(JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        ShowLeafAsOptionWithCode(out, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptionsWithCode(out, childlf, rootlayer);
        }
    }
    
    
    
	/**
	 * 递归获得jsTree的json字符串
	 * 
	 * @param parentCode
	 *            父节点parentCode
	 * @return str
	 */
	private String getJson(Directory dir, String parentCode, String str)
			throws Exception {
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
        License lic = License.getInstance();
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			i++;
			Leaf childlf = (Leaf) ri.next();		
			
			if ("-1".equals(parentCode)) {
				if (str.indexOf("{id:\"" + childlf.getCode()) == -1) {
					str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
					+ childlf.getName() + "\",state:{opened:true}} ,";
				}
			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName() + "\", isUse:\"" + childlf.isUse() + "\" },";
			}
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					Leaf child = (Leaf) childri.next();
					
					// System.out.println("getJson: code=" + child.getCode() + " name=" + child.getName() + "  lic.isPlatformSrc()=" + lic.isPlatformSrc());
					
					if (child.getCode().equals(MenuController.SALES)) {
				        if (!lic.isPlatformSrc())
				            continue;
				        // 平台版才可以用CRM模块，如果许可证中的解决方案中未勾选CRM模块
					    if (lic.isSolutionVer() && !lic.canUseSolution(License.SOLUTION_CRM)) {
					       	continue;
					    }
					}						
					
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName() + "\" },";
					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = getJson(dir, child.getCode(), str);
					}
				}
			}
		}
		return str;
	}
	
	public String getJsonString() throws Exception {
    	Directory dir = new Directory();
    	String str = "[";  
    	// 从根开始  
    	str = getJson(dir,"-1",str);  
    	str = str.substring(0,str.length()-1);
    	str += "]";  
    	return str;
    }	
	
	public String getJsonTreeString(String roleCode) throws Exception {
		RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(roleCode);
		String[] privs = rd.getRolePriv(roleCode);
		
    	Directory dir = new Directory();
    	String str = "[";
    	// 从根开始  
    	str = getJsonTree(roleCode, privs, dir, "-1", str);  
    	str += "]";  
    	return str;
    }		
	
	public boolean canRoleSee(String roleCode, Leaf childlf, String[] rolePrivs) {
		if (childlf.getType()==Leaf.TYPE_MODULE) {
			String moduleCode = childlf.getFormCode();		
			ModulePrivDb mpd = new ModulePrivDb();
			Vector v = mpd.getModulePrivsOfModule(moduleCode);
	        Iterator ir = v.iterator();
	        while (ir.hasNext()) {
	            // 遍历每个权限项
	            ModulePrivDb lp = (ModulePrivDb) ir.next();
	            if (lp.getType()==ModulePrivDb.TYPE_ROLE) {
	            	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
	            		if (lp.getSee() == 1 || lp.getManage()==1) {
	            			// System.out.println(getClass() + " " + lp.getName() + " " + moduleCode);
	            			return true;
	            		}	            		
	            	}
	            	else if (roleCode.equals(lp.getName())) {
	            		if (lp.getSee() == 1 || lp.getManage()==1) {
	            			return true;
	            		}
	            	}
	            }
	        }
		}
		else if (childlf.getType()==Leaf.TYPE_FLOW) {
            LeafPriv leafPriv = new LeafPriv(childlf.getFormCode());
            // list该节点的所有拥有权限的用户
            Vector v = leafPriv.listPriv(LeafPriv.PRIV_SEE);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
                LeafPriv lp = (LeafPriv) ir.next();
                if (lp.getType()==LeafPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		        		if (lp.getSee()==1) {
		        			return true;
		        		}
		        	}
		        	else if (roleCode.equals(lp.getName())) {
                        if (lp.getSee() == 1) {
                            return true;
                        }
                	}
                }
            }
		}
		else if (childlf.getType()==Leaf.TYPE_LINK) {
			if (childlf.getPvg().equals("")) {
				return true;
			}
			if (rolePrivs!=null) {
				for (String pv : rolePrivs) {
                    String[] ary = StrUtil.split(childlf.getPvg(), ",");
                    if (ary!=null) {
                        for (int k = 0; k < ary.length; k++) {
                            if (pv.equals(ary[k])) {
                                return true;
                            }
                        }
                    }
				}
			}
		}
		else if (childlf.getType()==Leaf.TYPE_BASICDATA) { // 如果是基礎數據管理
        	int kindId = StrUtil.toInt(childlf.getFormCode(), -1);        	
            SelectKindPriv skp = new SelectKindPriv();
            skp.setKindId(kindId);
            // list该节点的所有拥有权限的用户?
            Vector r = skp.listPriv(SelectKindPriv.PRIV_APPEND);
            Iterator ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
            	SelectKindPriv lp = (SelectKindPriv) ir.next();
            	if (lp.getType()==SelectKindPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		                return true;
		            } 
		            else {
	                    if (roleCode.equals(lp.getName())) {
	                        return true;
	                    }
		            }
            	}
            }
            r = skp.listPriv(SelectKindPriv.PRIV_MODIFY);
            ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
            	SelectKindPriv lp = (SelectKindPriv) ir.next();
            	if (lp.getType()==SelectKindPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		                return true;
		            } 
		            else {
	                    if (roleCode.equals(lp.getName())) {
	                        return true;
	                    }
		            }
            	}
            }      
            r = skp.listPriv(SelectKindPriv.PRIV_DEL);
            ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
            	SelectKindPriv lp = (SelectKindPriv) ir.next();
            	if (lp.getType()==SelectKindPriv.TYPE_ROLE) {
		        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
		                return true;
		            } 
		            else {
	                    if (roleCode.equals(lp.getName())) {
	                        return true;
	                    }
		            }
            	}
            }             
		}
        	
        return false;
	}
    
	/**
	 * 递归获得TreeGrid的json字符串
	 * 
	 * @param parentCode
	 *            父节点parentCode
	 * @return str
	 */
	private String getJsonTree(String roleCode, String[] rolePrivs, Directory dir, String parentCode, String str)
			throws Exception {
		// 把顶层的查出来
		int i = 0;		
		Vector children = dir.getChildren(parentCode);
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			Leaf childlf = (Leaf) ri.next();
			
			if (!childlf.isUse()) {
				i++;
				continue;
			}
			
			boolean canSee = false;
			String link = childlf.getLink(request);
			
			String priv = childlf.getPvg();
			String privName = "";

            String[] ary = StrUtil.split(priv, ",");
            if (ary!=null) {
                for (int k = 0; k < ary.length; k++) {
                    if (ary[k].equals("!admin")) {
                        if ("".equals(privName)) {
                            privName = "非管理员";
                        }
                        else {
                            privName += "，" + "非管理员";
                        }
                    } else {
                        PrivDb pd = new PrivDb(ary[k]);
                        if ("".equals(privName)) {
                            privName = pd.getDesc();
                        }
                        else {
                            privName += ", " + pd.getDesc();
                        }
                    }
                }
            }
			
			canSee = canRoleSee(roleCode, childlf, rolePrivs);
			
			String moduleCode = "", moduleName = "", formCode = "";
			String aliasCode = "", aliasName = "";
			if (childlf.getType()==Leaf.TYPE_MODULE) {
				ModuleSetupDb msd = new ModuleSetupDb();
				moduleCode = childlf.getFormCode();
				msd = msd.getModuleSetupDb(moduleCode);
				if (msd!=null) {
					moduleName = msd.getString("name");
					formCode = msd.getString("form_code");
					aliasName = moduleName;
					aliasCode = formCode;
					String desc = StrUtil.getNullStr(msd.getString("description"));
					if (!"".equals(desc)) {
						privName = "(" + desc + ")";
					}
				}
			}
			else if (childlf.getType()==Leaf.TYPE_FLOW) {
				com.redmoon.oa.flow.Leaf lfFlow = new com.redmoon.oa.flow.Leaf();
				lfFlow = lfFlow.getLeaf(childlf.getFormCode());
				if (lfFlow!=null) {
					aliasName = lfFlow.getName();
					aliasCode = childlf.getFormCode();
				}
			}
			else if (childlf.getType()==Leaf.TYPE_BASICDATA) {
				SelectKindDb skd = new SelectKindDb();
				skd = skd.getSelectKindDb(StrUtil.toInt(childlf.getFormCode(), -1));
				if (skd.isLoaded()) {
					aliasName = skd.getName();
					aliasCode = childlf.getFormCode();
				}
			}
			
			if ("-1".equals(parentCode)) {
				if (str.indexOf("{id:\"" + childlf.getCode()) == -1) {
					str += "{id:\"" + childlf.getCode() + "\", link:\"" + link + "\", aliasCode:\"" + aliasCode + "\", aliasName:\"" + aliasName + "\", type:\"" + childlf.getType() + "\", canSee:true, parent:\"#\",name:\""
					+ childlf.getName() + "\", formCode:\"" + formCode + "\", moduleCode:\"" + moduleCode + "\", moduleName:\"" + moduleName + "\", priv:\"" + priv + "\", privName:\"" + privName + "\", state:{opened:true}";
				}
			} else {
				str += "{id:\"" + childlf.getCode() + "\", link:\"" + link + "\", aliasCode:\"" + aliasCode + "\", aliasName:\"" + aliasName + "\", type:\"" + childlf.getType() + "\", canSee:" + canSee + ", parent:\""
						+ childlf.getParentCode() + "\",name:\""
						+ childlf.getName() + "\", formCode:\"" + formCode + "\", moduleCode:\"" + moduleCode + "\", moduleName:\"" + moduleName + "\", priv:\"" + priv + "\", privName:\"" + privName + "\", isUse:\"" + childlf.isUse() + "\"";
			}
			
			Vector childs = dir.getChildren(childlf.getCode());
			int size1 = childs.size() - 1;
			// 如果有子节点
			if (!childs.isEmpty()) {
				str += ", children:[";
				int size2 = childs.size()-1;
				// 遍历它的子节点
				int j = 0;				
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					Leaf child = (Leaf) childri.next();
					if (!child.isUse()) {
						j++;
						continue;
					}
					
					link = child.getLink(request);

					priv = child.getPvg();
					privName = "";
                    ary = StrUtil.split(priv, ",");
                    if (ary!=null) {
                        for (int k = 0; k < ary.length; k++) {
                            if (ary[k].equals("!admin")) {
                                if ("".equals(privName)) {
                                    privName = "非管理员";
                                }
                                else {
                                    privName += "，" + "非管理员";
                                }
                            } else {
                                PrivDb pd = new PrivDb(ary[k]);
                                if ("".equals(privName)) {
                                    privName = pd.getDesc();
                                }
                                else {
                                    privName += ", " + pd.getDesc();
                                }
                            }
                        }
                    }

					if (child.getType()==Leaf.TYPE_MODULE) {
						ModuleSetupDb msd = new ModuleSetupDb();
						moduleCode = child.getFormCode();
						msd = msd.getModuleSetupDb(moduleCode);
						if (msd!=null) {
							moduleName = msd.getString("name");
							formCode = msd.getString("form_code");
							aliasName = moduleName;
							aliasCode = formCode;							
						}
					}		
					else if (child.getType()==Leaf.TYPE_FLOW) {
						com.redmoon.oa.flow.Leaf lfFlow = new com.redmoon.oa.flow.Leaf();
						lfFlow = lfFlow.getLeaf(child.getFormCode());
						if (lfFlow!=null) {
							aliasName = lfFlow.getName();
							aliasCode = child.getFormCode();
						}
					}
					else if (child.getType()==Leaf.TYPE_BASICDATA) {
						SelectKindDb skd = new SelectKindDb();
						skd = skd.getSelectKindDb(StrUtil.toInt(child.getFormCode(), -1));
						if (skd.isLoaded()) {
							aliasName = skd.getName();
							aliasCode = child.getFormCode();
						}
					}					
					
					canSee = canRoleSee(roleCode, child, rolePrivs);
					
					str += "{id:\"" + child.getCode() + "\", link:\"" + link + "\", aliasCode:\"" + aliasCode + "\", aliasName:\"" + aliasName + "\", type:\"" + child.getType() + "\", canSee:" + canSee + ", parent:\""
							+ child.getParentCode() + "\", formCode:\"" + formCode + "\", moduleCode:\"" + moduleCode + "\", moduleName:\"" + moduleName + "\", priv:\"" + priv + "\", privName:\"" + privName + "\", name:\""
							+ child.getName() + "\"";

					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str += ", children:[";
						str = getJsonTree(roleCode, rolePrivs, dir, child.getCode(), str);
						str += "]";
					}
					str += "}";
					if (j!=size2) {
						str += ",";
					}
					j++;	
				}
				str += "]";
			}
			str += "}";
			if (i != size-1) {
				str += ",";
			}
			i++;			
		}
		return str;
	}	
}
