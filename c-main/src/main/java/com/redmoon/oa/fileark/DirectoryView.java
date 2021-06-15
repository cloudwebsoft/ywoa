package com.redmoon.oa.fileark;

import cn.js.fan.util.StrUtil;
import java.util.Vector;
import javax.servlet.jsp.JspWriter;
import java.util.Iterator;
import org.apache.log4j.Logger;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.fileark.plugin.*;
import com.redmoon.oa.fileark.plugin.base.IPluginUI;
import com.redmoon.oa.pvg.Privilege;

public class DirectoryView {
    Logger logger = Logger.getLogger(Leaf.class.getName());
    public Leaf rootLeaf;
    public Vector UprightLineNodes = new Vector(); //用于显示竖线
    protected HttpServletRequest request;

    public DirectoryView(Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
    }

    public DirectoryView(HttpServletRequest request, Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
        this.request = request;
    }

    public void ListSimple(JspWriter out, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeSimple(out, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    public void ListTreeSimple(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
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
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTreeSimple(out, childlf, isLastChild1, target, link, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    /**
     * 简单方式显示
     * @param out JspWriter
     * @param leaf Leaf
     * @param isLastChild boolean
     * @param target String
     * @param link String
     * @param tableClass String
     * @param tableClassMouseOn String
     * @throws Exception
     */
    public void ShowLeafSimple(JspWriter out, Leaf leaf,
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
                out.println("<img src='' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            }
        }

        // 三种类型节点，用同一个Link
        out.print(
                "<a target='" + target + "' href='" + link + "?dir_code=" +
                StrUtil.UrlEncode(code) + "'>" + name + "</a>");

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    public void list(JspWriter out) throws Exception {
        ListTree(out, rootLeaf, true);
    }

    public void listAjax(HttpServletRequest request, JspWriter out, boolean isShowRoot) throws Exception {
        Privilege privilege = new Privilege();
        ListTreeAjax(request, privilege, out, rootLeaf, true, isShowRoot);
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

    // 显示根结点为leaf的树
    void ListTreeAjax(HttpServletRequest request, Privilege privilege, JspWriter out, Leaf leaf,
                  boolean isLastChild, boolean isShowRoot) throws Exception {
        if (isShowRoot)
            ShowLeafAjax(request, privilege, out, leaf, isLastChild, isShowRoot);

        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0) {
            out.print("<table id='childof" + leaf.getCode() +
                      "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ShowLeafAjax(request, privilege, out, childlf, isLastChild1, isShowRoot);
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
        String description = leaf.getDescription();

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
                if (count > 0) { // =0的也计入的话会在树底端的结点产生多余竖线
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

        out.println("<table id=" + tableid + " name=" + tableid + " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" border=0>");
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
                out.println("<img src='' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            }
        }

        if (leaf.getType() == Leaf.TYPE_LIST) {
            out.print("<a target=_parent href='document_list_m.jsp?dir_code=" +
                      StrUtil.UrlEncode(code) + "&dir_name=" +
                      StrUtil.UrlEncode(name) + "'>" + name +
                      "</a>");
        } else if (leaf.getType() == 1)
            out.print(
                    "<a target=_parent href='../fwebedit.jsp?op=editarticle&dir_code=" +
                    StrUtil.UrlEncode(code) + "&dir_name=" +
                    StrUtil.UrlEncode(name) + "'>" + name + "</a>");
        else if (leaf.getType() == leaf.TYPE_LINK) {
            out.print(
                    "<a target='" + leaf.getTarget() + "' href='" +
                    leaf.getDescription() + "'>" + name + "</a>");
        } else if (leaf.getType() == 0) {
            out.print(name);
        }

        out.print("     </td><td width='15%' align=right nowrap>");
        if (!leaf.getCode().equals("root")) {
            if (leaf.getIsHome())
                out.print("前台&nbsp;");
            // out.print("<a href='dir_top.jsp?op=removecache&code=" + code +
            //          "'>清缓存</a>&nbsp;");
            if (leaf.getType() == 1)
                out.print("文章&nbsp;");
            else if (leaf.getType() == 2)
                out.print("列表&nbsp;");
            else
                out.print("无内容&nbsp;");

            out.print(
                    "<a target=_parent href='dir_priv_m.jsp?dirCode=" +
                    StrUtil.UrlEncode(code, "utf-8") + "'>权限</a>&nbsp;");
            out.print(
                    "<a target=dirbottomFrame href='dir_bottom.jsp?parent_code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                    StrUtil.UrlEncode(name, "utf-8") +
                    "&op=AddChild'>添子目录</a>&nbsp;");
            out.print(
                    "<a target='dirbottomFrame' href='dir_bottom.jsp?op=modify&code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&name=" +
                    StrUtil.UrlEncode(name, "utf-8") + "&description=" +
                    StrUtil.UrlEncode(description, "utf-8") + "'>修改</a>&nbsp;");
            if (!leaf.isSystem()) {
                out.print(
                        "<a target=_self href=# onClick=\"jConfirm('您确定要删除" +
                        name +
                        "吗?','提示',function(r){if(!r){return;}else{window.location.href='dir_top.jsp?op=del&root_code=" +
                        StrUtil.UrlEncode(rootLeaf.getCode()) + "&delcode=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'}}) \">删除</a>&nbsp;");
            }
            out.print("<a href='dir_top.jsp?op=move&direction=up&root_code=" +
                      StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>上移</a>&nbsp;");
            out.print("<a href='dir_top.jsp?op=move&direction=down&root_code=" +
                      StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;");
            out.print("<a href='dir_top.jsp?root_code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>管理</a>");
        }
        out.println("  </td></tr></tbody></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     *
     **/
    void ShowLeafAjax(HttpServletRequest request, Privilege privilege, JspWriter out, Leaf leaf,
                  boolean isLastChild, boolean isShowRoot) throws Exception {
		if (!leaf.isShow()) {
			LeafPriv lp = new LeafPriv(leaf.getCode());
			// 如果节点被置为不显示于前台，则只有当用户具有审核权时才能看到该节点
			if (!lp.canUserExamine(privilege.getUser(request)))
				return;
    	}    	
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

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
                if (count > 0) { // =0的也计入的话会在树底端的结点产生多余竖线
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    // System.out.println(leaf.getCode() + " layer=" + layer +
                    //                   " count=" + count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();
        // System.out.println(code + " childcount=" + childcount);

        String tableid = leaf.getCode();

        out.println("<table id=" + tableid + " name=" + tableid + " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td width='85%' height='13' align=left nowrap>");

        int padWidth = 0;
        for (int k = 1; k <= layer - 1; k++) {
            padWidth += 21;
        }
        if (childcount==0)
            padWidth += 16;
        out.print("<img src='" + request.getContextPath() + "/images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img tableRelate='' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0)
                out.println("<img tableRelate='" + tableid +
                            "' onClick=\"ShowChild(this, '" + leaf.getCode() + "')\" src='images/i_plus.gif' align='absmiddle'><img src='" + request.getContextPath() + "/images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='images/folder_01.gif' align='absmiddle'>");
            else
                out.println(
                        "<img src='images/folder_01.gif' align='absmiddle'>");
        }

        String leafClass = "";
        if (!leaf.isShow())
        	leafClass = "class='node_hide' ";
        if (leaf.getType() == 2) {
            out.print("<a " + leafClass + "target=_parent href='document_list_m.jsp?dir_code=" +
                      StrUtil.UrlEncode(code) + "&dir_name=" +
                      StrUtil.UrlEncode(name) + "'>" + name +
                      "</a>");
        } else if (leaf.getType() == 1)
            out.print(
                    "<a " + leafClass + "target=_parent href='../fwebedit.jsp?op=editarticle&dir_code=" +
                    StrUtil.UrlEncode(code) + "&dir_name=" +
                    StrUtil.UrlEncode(name) + "'>" + name + "</a>");
        else if (leaf.getType() == Leaf.TYPE_LINK) {
            out.print(
                    "<a " + leafClass + "target='" + leaf.getTarget() + "' href='" +
                    leaf.getDescription() + "'>" + name + "</a>");
        } else if (leaf.getType() == 0) {
            out.print("<span " + leafClass + ">" + name + "</span>");
        }

        out.print("     </td><td width='15%' align=right nowrap>");
                
        if (!leaf.getCode().equals("root")) {
            if (leaf.getIsHome())
                out.print("前台&nbsp;");
        
            //out.print("<a href='javascript:;' onclick=\"addTab('" + leaf.getName() + "类别', '" + request.getContextPath() + "/fileark/dir_kind_list.jsp?dirCode=" + StrUtil.UrlEncode(leaf.getCode()) + "')\">类别</a>&nbsp;");
            
            // out.print("<a href='dir_top.jsp?op=removecache&code=" + code +
            //          "'>清缓存</a>&nbsp;");
            if (leaf.getType() == 1)
                out.print("文章&nbsp;");
            else if (leaf.getType() == 2)
                out.print("列表&nbsp;");
            else
                out.print("无内容&nbsp;");

            out.print(
                    "<a target=_parent href='dir_priv_m.jsp?dirCode=" +
                    StrUtil.UrlEncode(code, "utf-8") + "'>权限</a>&nbsp;");
            out.print(
                    "<a target=dirbottomFrame href='dir_bottom.jsp?parent_code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                    StrUtil.UrlEncode(name, "utf-8") +
                    "&op=AddChild'>添子目录</a>&nbsp;");
            out.print(
                    "<a target='dirbottomFrame' href='dir_bottom.jsp?op=modify&code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&name=" +
                    StrUtil.UrlEncode(name, "utf-8") + "&description=" +
                    StrUtil.UrlEncode(description, "utf-8") + "'>修改</a>&nbsp;");
            if (!leaf.isSystem()) {
                out.print(
                        "<a target=_self href=# onClick=\"jConfirm('您确定要删除" +
                        StrUtil.toHtml(name) +
                        "吗?','提示',function(r){if(!r){return;}else{window.location.href='dir_top_ajax.jsp?op=del&root_code=" +
                        StrUtil.UrlEncode(rootLeaf.getCode()) + "&delcode=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'}}) \">删除</a>&nbsp;");
            }
            out.print("<a href='dir_top_ajax.jsp?op=move&direction=up&root_code=" +
                      StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>上移</a>&nbsp;");
            out.print("<a href='dir_top_ajax.jsp?op=move&direction=down&root_code=" +
                      StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;");
            out.print("<a href='dir_top_ajax.jsp?root_code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>管理</a>");
        }
        out.println("  </td></tr></tbody></table>");
    }

    void ShowLeafAsOption(JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
    	if (!leaf.isShow())
    		return;
    	
    	LeafPriv lp = new LeafPriv(leaf.getCode());
    	if (!lp.canUserSee(request))
    		return;
    	
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            if (leaf.getType()==leaf.TYPE_LIST || leaf.getType()==leaf.TYPE_DOCUMENT)
                out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
            else
                out.print("<option value='not'>" + blank + "╋ " + name + "</option>");
        }
        else {
            if (leaf.getType()==leaf.TYPE_LIST || leaf.getType()==leaf.TYPE_DOCUMENT)
                out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
            else
                out.print("<option value='not'>" + blank + "├『" + name +
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

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptions(out, childlf, rootlayer);
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
        String description = leaf.getDescription();
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
    public void ListTreeFunc(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafFunc(out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
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
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTreeFunc(out, childlf, isLastChild1, target, func, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    public void ShowLeafFunc(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

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
                out.println("<img src='' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
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
    
    /**
     * 用于显示维基文章页中的维基目录
     * @param out
     * @param target
     * @param isJsp
     * @throws Exception
     */
    public void ListUl(JspWriter out, String target, boolean isJsp) throws Exception {
        ListTreeUl(out, rootLeaf, true, target, isJsp);
    }

    // 显示根结点为leaf的树
    void ListTreeUl(JspWriter out, Leaf leaf,
                        boolean isLastChild, String target, boolean isJsp) throws
            Exception {
    	out.print("<li>");
    	
        // 非前台节点不显示
        if (leaf.getIsHome()) {
            ShowLeafUl(out, leaf, isLastChild, target, isJsp);
        }

        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0)
            out.print("<ul>");
        Iterator ri = children.iterator();
        //写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            // 非前台节点不显示
            if (childlf.getIsHome()) {
                ListTreeUl(out, childlf, isLastChild1, target, isJsp);
            }
        }
        if (size > 0)
            out.print("</ul>");
        out.print("</li>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafUl(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, boolean isJsp) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();

        // System.out.println(code + " childcount=" + childcount);
        String cls;
        if (leaf.getChildCount()>0)
        	cls = "folder";
        else
        	cls = "file";
        
        String rootPath = request.getContextPath();
        
        String link = rootPath + "/fileark/wiki_list.jsp?dir_code=";
        
        out.print("<span class='" + cls + "'>");

        // 三种类型节点，用同一个Link
        if (leaf.getType() == Leaf.TYPE_LIST) {
        	String page = "";
        	PluginMgr pm = new PluginMgr();
        	PluginUnit pu = pm.getPluginUnitOfDir(leaf.getCode());
        	String listPage = "";
        	if (pu!=null) {
        		IPluginUI ipu = pu.getUI(request);
        		listPage = ipu.getListPage();
        		page = rootPath + "/" + ipu.getListPage() + "?dirCode=";
        		
        	}
        	if (!listPage.equals("")) {
        		link = page;
        	}
            out.print("<a href=\"javascript:;\" onclick=\"addTab('" + name + "', '" + link +
                    StrUtil.UrlEncode(code) + "')\" class='list'>" + name +
                    "</a>");
        }
        else if (leaf.getType()==Leaf.TYPE_LINK) {
            out.print("<a target='" + target + "' href='" + leaf.getDescription() + "' class='list'>" + name +
                    "</a>");        	
        }
        else {
        	String page = "";
        	PluginMgr pm = new PluginMgr();
        	PluginUnit pu = pm.getPluginUnitOfDir(leaf.getCode());
        	if (pu!=null) {
        		IPluginUI ipu = pu.getUI(request);
        		
        		// System.out.println(getClass() + " ipu.getViewPage()=" + ipu.getViewPage());
        		
        		page = rootPath + "/" + ipu.getViewPage() + "?id=" + leaf.getDocID();
        	}
        	if (page.equals("")) {
	        	if (isJsp) {
	        		page = rootPath + "/doc_show.jsp?id=" + leaf.getDocID();
	        	}
	        	else {
	        		page = rootPath + "/doc_view.jsp?id=" + leaf.getDocID();
	        	}     
        	}
        	
            out.print(
                    "<a target='" + target + "' href='" + page + "'>" + name + "</a>");
        }
        out.print("</span>");
    }
    
    public String getDirAsOption(HttpServletRequest request, Leaf leaf, int rootlayer) {
    	if (!leaf.isShow())
    		return "";
    	
    	LeafPriv lp = new LeafPriv(leaf.getCode());
    	if (!lp.canUserSee(request))
    		return "";
    	
        String outStr = "";
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }

        if (leaf.getType()==Leaf.TYPE_LIST || leaf.getType()==Leaf.TYPE_DOCUMENT)
        	;
        else
        	code = "";
        
        if (leaf.getChildCount()>0) {
            outStr += "<option value='" + code + "'>" + blank + "╋ " + name + "</option>";
        }
        else {
            outStr += "<option value=\"" + code + "\">" + blank + "├『" + name +
                      "』</option>";
        }
        return outStr;
    }    
    
    /**
     * 取得目录options
     * @param request
     * @param outStr
     * @param leaf
     * @param rootlayer
     * @return
     * @throws ErrMsgException
     */
    public StringBuffer getDirAsOptions(HttpServletRequest request, StringBuffer outStr, Leaf leaf, int rootlayer) throws ErrMsgException {
        outStr.append(getDirAsOption(request, leaf, rootlayer));
        Directory dm = new Directory();
        Vector children = dm.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return outStr;

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            getDirAsOptions(request, outStr, childlf, rootlayer);
        }
        return outStr;
    } 
    
    public String getJsonString(String rootCode) throws Exception {
		Leaf dir = new Leaf();
		Leaf dir1 = new Leaf();
		String parentCdoe = "";
		if("".equals(rootCode)){
			parentCdoe = "-1";
		}else{
			dir1 = dir1.getLeaf(rootCode);
			parentCdoe = dir1.getParentCode();
		}
		String str = "[";
		// 从根开始
		str = this.getJson(dir, parentCdoe, str);
		str = str.substring(0, str.length() - 1);
		str += "]";
		return str;
	}

	/**
	 * 递归获得jsTree的json字符串
	 * 
	 * @param parentCode
	 *            父节点parentCode
	 * @return str
	 */
	private String getJson(Leaf dir, String parentCode, String str)
			throws Exception {
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getTreeChildren(parentCode);
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			Leaf childlf = (Leaf) ri.next();
			if (!childlf.getRootCode().equals(rootLeaf.getCode())) {
				continue;
			}
			i++;
			if ("-1".equals(parentCode)) {
				str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"") + "\",state:{opened:true}} ,";
			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"") + "\" },";
			}
			Vector childs = dir.getTreeChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					Leaf child = (Leaf) childri.next();
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName().replaceAll("\"", "\\\\\"") + "\" },";
					// 还有子节点(递归调用)
					Vector ch = dir.getTreeChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = this.getJson(dir, child.getCode(), str);
					}
				}
			}
		}
		return str;
	}
        
}
