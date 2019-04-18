package com.redmoon.oa.netdisk;

import cn.js.fan.util.StrUtil;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.util.Iterator;
import org.apache.log4j.Logger;

import com.redmoon.forum.Privilege;

public class PublicDirectoryView {
    Logger logger = Logger.getLogger(PublicLeaf.class.getName());
    public PublicLeaf rootLeaf;
    public Vector UprightLineNodes = new Vector(); //用于显示竖线

    public PublicDirectoryView(PublicLeaf rootLeaf) {
        this.rootLeaf = rootLeaf;
    }

    public void ListSimple(JspWriter out, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeSimple(out, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    void ListTreeSimple(JspWriter out, PublicLeaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafSimple(out, leaf, isLastChild, target, link, tableClass, tableClassMouseOn);
        PublicDirectory dir = new PublicDirectory();
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
            PublicLeaf childlf = (PublicLeaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTreeSimple(out, childlf, isLastChild1, target, link, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    public void ShowLeafSimple(JspWriter out, PublicLeaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();
        String mappingAddress = StrUtil.getNullStr(leaf.getMappingAddress());

        if (!isLastChild) {
            PublicLeaf brotherleaf = leaf.getBrother("down");
            // System.out.println("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) {
                    PublicUprightLineNode uln = new PublicUprightLineNode(layer, count);
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
//            Iterator ir = UprightLineNodes.iterator();
//            while (ir.hasNext()) {
//                PublicUprightLineNode node = (PublicUprightLineNode) ir.next();
//                //如果在K层上存在一个竖线结点则画出
//                if (node.getLayer() == k) {
//                    node.show(out, "");
//                    if (node.getCount() == 0) {
//                        UprightLineNodes.remove(node);
//                        //System.out.println("Remove " + node);
//                    }
//                    isShowed = true;
//                    break;
//                }
//            }
            if (!isShowed)
                out.println("<img src='' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_puls-root.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-2.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/folder_netdisk.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-1.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/folder_netdisk.gif' align='absmiddle'>");
            }
        }

        // 三种类型节点，用同一个Link
        if (mappingAddress.equals("")) {
            out.print(
                    "<a target='" + target + "' href='" + link + "?dir_code=" +
                    StrUtil.UrlEncode(code) + "'>" + name + "</a>");
        } else {
            out.print(
                    "<a target='" + target + "' href='netdisk_public_mapping_list.jsp?dir_code=" + StrUtil.UrlEncode(code) + "&mappingAddress=" +
                    StrUtil.UrlEncode(mappingAddress) + "'>" + name +
                    "</a>");
        }

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }
    
    
    public String ListSimple(StringBuilder sb, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        return ListTreeSimple(sb, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    public String ListTreeSimple(StringBuilder sb, PublicLeaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafSimple(sb, leaf, isLastChild, target, link, tableClass, tableClassMouseOn);
        PublicDirectory dir = new PublicDirectory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return null;

        int i = 0;
        if (size > 0)
           sb.append("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        Iterator ri = children.iterator();
        //写跟贴
        while (ri.hasNext()) {
            i++;
            PublicLeaf childlf = (PublicLeaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTreeSimple(sb, childlf, isLastChild1, target, link, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            sb.append("</td></tr></table>");
        return sb.toString();
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    public void ShowLeafSimple(StringBuilder sb, PublicLeaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();
        String mappingAddress = StrUtil.getNullStr(leaf.getMappingAddress());

        if (!isLastChild) {
	            PublicLeaf brotherleaf = leaf.getBrother("down");
	            // System.out.println("brother=" + brotherleaf);
	            // 如果兄弟结点存在
	            if (brotherleaf != null) {
	                // 取其所有的孩子结点
	                Vector r = new Vector();
	                leaf.getAllChild(r, leaf);
	                int count = r.size();
	                if (count>0) {
	                    PublicUprightLineNode uln = new PublicUprightLineNode(layer, count);
	                    // System.out.println(leaf.getCode() + " layer=" + layer +
	                    //                   " count=" + count);
	                    UprightLineNodes.addElement(uln);
	                }
	            }
	        }
	
	        int childcount = leaf.getChildCount();
	        // System.out.println(code + " childcount=" + childcount);
	
	        String tableid = "table" + leaf.getCode();
	        if(leaf.getMappingAddress().equals("")){
	
	        sb.append("<table id=" + tableid + " name=" + tableid + " class='" + tableClass + "' cellspacing=0 cellpadding=0 width='100%' align=center  border=0>");
	        sb.append("    <tbody><tr>");
	        sb.append("   <td height='13' align=left nowrap>");
	        // for (int k = 1; k <= layer - 1; k++) {
	        for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) {
	            boolean isShowed = false;
	//            Iterator ir = UprightLineNodes.iterator();
	//            while (ir.hasNext()) {
	//                PublicUprightLineNode node = (PublicUprightLineNode) ir.next();
	//                //如果在K层上存在一个竖线结点则画出
	//                if (node.getLayer() == k) {
	//                    node.show(sb, "");
	//                    if (node.getCount() == 0) {
	//                        UprightLineNodes.remove(node);
	//                        //System.out.println("Remove " + node);
	//                    }
	//                    isShowed = true;
	//                    break;
	//                }
	//            }
	            if (!isShowed)
	            	 sb.append("<img src='' width=20 height=1 style='visibility:hidden'>");
	        }
	
	        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
	        	 sb.append("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_puls-root.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
	        } else {
	            if (isLastChild) { // 是最后一个孩子结点
	                if (childcount > 0)
	                	 sb.append("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-2.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
	                else
	                	 sb.append("<img src='images/folder_netdisk.gif' align='absmiddle'>");
	            } else { // 不是最后一个孩子结点
	                if (childcount > 0)
	                	 sb.append("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-1.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
	                else
	                	 sb.append("<img src='images/folder_netdisk.gif' align='absmiddle'>");
	            }
	        }
	
	        // 三种类型节点，用同一个Link
	        if (mappingAddress.equals("")) {
	        	 sb.append(
	                    "<span>" + name + "</span>");
	        } else {
	        	 sb.append(
	                    "<a target='" + target + "' href='netdisk_public_mapping_list.jsp?dir_code=" + StrUtil.UrlEncode(code) + "&mappingAddress=" +
	                    StrUtil.UrlEncode(mappingAddress) + "'>" + name +
	                    "</a>");
	        }
	
	        sb.append("     </td>");
	        sb.append("  </tr></tbody></table>");
	    }
    }
    public void list(JspWriter out) throws Exception {
        ListTree(out, rootLeaf, true);
    }

    // 显示根结点为leaf的树
    void ListTree(JspWriter out, PublicLeaf leaf,
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
        PublicDirectory dir = new PublicDirectory();
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
            PublicLeaf childlf = (PublicLeaf) ri.next();
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
    void ShowLeaf(JspWriter out, PublicLeaf leaf,
                  boolean isLastChild) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();
        String mappingAddress = StrUtil.getNullStr(leaf.getMappingAddress());

        if (!isLastChild) {
            // System.out.println("get leaf brother" + leaf.getName());
            PublicLeaf brotherleaf = leaf.getBrother("down");
            // System.out.println("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) { // =0的也计入的话会在树底端的结点产生多余竖线
                    PublicUprightLineNode uln = new PublicUprightLineNode(layer, count);
                    // System.out.println(leaf.getCode() + " layer=" + layer +
                    //                   " count=" + count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();
        // System.out.println(code + " childcount=" + childcount);

        String tableid = "table" + leaf.getCode();

        out.println("<table id=" + tableid + " name=" + tableid + " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center  border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td width='85%' height='13' align=left nowrap>");
        // for (int k = 1; k <= layer - 1; k++) {
        for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) { // 不用上一行，是因为上一行会产生多余的空格
            boolean isShowed = false;
//            Iterator ir = UprightLineNodes.iterator();
//            while (ir.hasNext()) {
//                PublicUprightLineNode node = (PublicUprightLineNode) ir.next();
//                //如果在K层上存在一个竖线结点则画出
//                if (node.getLayer() == k) {
//                    node.show(out, "");
//                    if (node.getCount() == 0) {
//                        UprightLineNodes.remove(node);
//                        //System.out.println("Remove " + node);
//                    }
//                    isShowed = true;
//                    break;
//                }
//            }
            if (!isShowed)
                out.println("<img src='' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_puls-root.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-2.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/folder_netdisk.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-1.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/folder_netdisk.gif' align='absmiddle'>");
            }
        }

        if (mappingAddress.equals("")) {
            out.print("<span style='font-size:12px;color:#888;'>"+name+"</span>");
                   /** "<a target=_parent href='../netdisk/netdisk_public_attach_list.jsp?dir_code=" +
                    StrUtil.UrlEncode(code) + "&dir_name=" +
                    StrUtil.UrlEncode(name) + "'>" + name +
                    "</a>");&*/
        } else {
        	  out.print("<span style='font-size:12px;color:#888;'>"+name+"</span>");
//            out.print(
//                    "<a target=_parent href='netdisk_public_mapping_list.jsp?mappingAddress=" +
//                    StrUtil.UrlEncode(mappingAddress) + "'>" + name +
//                    "</a>");
        }

        out.print("     </td><td width='15%' align=right nowrap>");
        if (!leaf.getCode().equals("root")) {
            if (leaf.getIsHome())
                out.print("首页&nbsp;");
            if (mappingAddress.equals("")) {
                out.print(
                        "<a target=dirbottomFrame href='netdisk_public_dir_bottom.jsp?parent_code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                        StrUtil.UrlEncode(name, "utf-8") +
                        "&op=AddChild'>添子目录</a>&nbsp;");
                
            } else {
                out.print(
                        "<span>映射目录&nbsp;</span>");
            }
        	out.print("<a target=_parent href='../netdisk/clouddisk_public_dir_priv.jsp?dirCode=" + StrUtil.UrlEncode(code) + "'>权限</a>&nbsp;");
            out.print(
            	
                    "<a target='dirbottomFrame' href='netdisk_public_dir_bottom.jsp?op=modify&code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&name=" +
                    StrUtil.UrlEncode(name, "utf-8") + "&description=" +
                    StrUtil.UrlEncode(description, "utf-8") + "'>修改</a>&nbsp;");
            out.print(
                    "<a target=_self href=# onClick=\"delPriv('"+rootLeaf.getCode()+"','"+code+"')\">删除</a>&nbsp;");
            out.print("<a href='?op=move&direction=up&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>上移</a>&nbsp;");
            out.print("<a href='?op=move&direction=down&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;");
        }
        out.println("  </td></tr></tbody></table>");
    }

    void ShowLeafAsOption(JspWriter out, PublicLeaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
        }
        else {
            out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" +
                      blank + "├『" + name +
                      "』</option>");
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptions(JspWriter out, PublicLeaf leaf, int rootlayer) throws Exception {
        ShowLeafAsOption(out, leaf, rootlayer);
        PublicDirectory dir = new PublicDirectory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            PublicLeaf childlf = (PublicLeaf) ri.next();
            ShowDirectoryAsOptions(out, childlf, rootlayer);
        }
    }

    /**
     * 把列表或无内容显示为蓝色
     * @param out JspWriter
     * @param leaf PublicLeaf
     * @param rootlayer int
     * @throws Exception
     */
    void ShowLeafAsOptionWithCode(HttpServletRequest request, JspWriter out, PublicLeaf leaf, int rootlayer)
                  throws Exception {
    	if (!leaf.getCode().equals(PublicLeaf.ROOTCODE)) {
    		// 检查是否有添加或管理的权限
    		PublicLeafPriv plfpv = new PublicLeafPriv(leaf.getCode());
    		if (plfpv.canUserSeeByAncestor(Privilege.getUser(request)) || plfpv.canUserManage(Privilege.getUser(request)))
    			;
    		else
    			return;
    	}
        String code = leaf.getCode();
        String name = leaf.getName();
        String mappingAddress = leaf.getMappingAddress();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if(mappingAddress.equals("")){
            if (leaf.getChildCount() > 0)
                out.print("<option value='" + code + "'>" + blank + "╋ " + name +
                          "</option>");
            else
                out.print("<option value='" + code + "'>" + blank + "    " +
                          name + "</option>");
        }
    }

    // 显示根结点为leaf的树，value中全为code
    public void ShowDirectoryAsOptionsWithCode(HttpServletRequest request, JspWriter out, PublicLeaf leaf, int rootlayer) throws Exception {
        ShowLeafAsOptionWithCode(request, out, leaf, rootlayer);
        PublicDirectory dir = new PublicDirectory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            PublicLeaf childlf = (PublicLeaf) ri.next();
            ShowDirectoryAsOptionsWithCode(request, out, childlf, rootlayer);
        }
    }

    /**
     * 树的节点在显示时，使用超链接href='func(deptCode)'
     * @param out JspWriter
     * @param leaf DeptDb
     * @param isLastChild boolean
     * @param target String
     * @param func String JS中的函数名称
     * @param tableClass String
     * @param tableClassMouseOn String
     * @throws Exception
     */
    void ListTreeFunc(JspWriter out, PublicLeaf leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafFunc(out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
        PublicDirectory dir = new PublicDirectory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0)
            out.print("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            PublicLeaf childlf = (PublicLeaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTreeFunc(out, childlf, isLastChild1, target, func, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    void ShowLeafFunc(JspWriter out, PublicLeaf leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

        if (!isLastChild) {
            PublicLeaf brotherleaf = leaf.getBrother("down");
            // System.out.println("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) {
                    PublicUprightLineNode uln = new PublicUprightLineNode(layer, count);
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
//            Iterator ir = UprightLineNodes.iterator();
//            while (ir.hasNext()) {
//                PublicUprightLineNode node = (PublicUprightLineNode) ir.next();
//                //如果在K层上存在一个竖线结点则画出
//                if (node.getLayer() == k) {
//                    node.show(out, "");
//                    if (node.getCount() == 0) {
//                        UprightLineNodes.remove(node);
//                        //System.out.println("Remove " + node);
//                    }
//                    isShowed = true;
//                    break;
//                }
//            }
            if (!isShowed)
                out.println("<img src='' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_puls-root.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-2.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/folder_netdisk.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/n_plus2-1.gif' align='absmiddle'><img src='images/folder_netdisk.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/folder_netdisk.gif' align='absmiddle'>");
            }
        }

        // 三种类型节点，用同一个Link
        out.print(
                "<a target='" + target + "' href='#' onClick=\"" + func + "('" + code + "', '" + name + "')\">" + name + "</a>");

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    public void ListFunc(JspWriter out, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeFunc(out, rootLeaf, true, target, func, tableClass, tableClassMouseOn);
    }
}
