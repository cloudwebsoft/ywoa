package com.redmoon.oa.netdisk;

import cn.js.fan.util.StrUtil;
import java.util.Vector;
import javax.servlet.jsp.JspWriter;
import java.util.Iterator;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;

public class DirectoryView {
    Logger logger = Logger.getLogger(Leaf.class.getName());
    Leaf rootLeaf;
    Vector UprightLineNodes = new Vector(); //用于显示竖线

    public DirectoryView(Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
    }

    public void ListSimple(JspWriter out, String target, String link, String queryString, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeSimple(out, rootLeaf, true, target, link, queryString, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    void ListTreeSimple(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String queryString, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafSimple(out, leaf, isLastChild, target, link, queryString, tableClass, tableClassMouseOn);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0)
            out.print("<table id='childoftable" + StrUtil.UrlEncode(leaf.getCode()) +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        Iterator ri = children.iterator();
        //写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTreeSimple(out, childlf, isLastChild1, target, link, queryString, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }
    
    public String ListSimple(StringBuilder sb, String target, String link, String queryString, String tableClass, String tableClassMouseOn,String dirCode) throws Exception {
        return ListTreeSimple(sb, rootLeaf, true, target, link, queryString, tableClass, tableClassMouseOn, dirCode);
    }

    // 显示根结点为leaf的树
    String ListTreeSimple(StringBuilder sb, Leaf leaf,
                  boolean isLastChild, String target, String link, String queryString, String tableClass, String tableClassMouseOn,String dirCode) throws Exception {
        
    	ShowLeafSimple(sb, leaf, isLastChild, target, link, queryString, tableClass, tableClassMouseOn);
        Directory dir = new Directory();
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
            Leaf childlf = (Leaf) ri.next();
            if(!childlf.getCode().equals(dirCode)){
	            boolean isLastChild1 = true;
	            if (size != i)
	                isLastChild1 = false;
	            ListTreeSimple(sb, childlf, isLastChild1, target, link, queryString, tableClass, tableClassMouseOn, dirCode);
            }
        }
        if (size > 0)
        	sb.append("</td></tr></table>");
        
        return sb.toString();
    }
    
    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSimple(StringBuilder sb, Leaf leaf,
                  boolean isLastChild, String target, String link, String queryString, String tableClass, String tableClassMouseOn) throws Exception {
        // String code = leaf.getCode();
        String code = leaf.getCode();

        String name = leaf.getName();
        if (name.equals(""))
        	name = "&nbsp;"; // 防止当出错时，如果没有文件夹的名称则无法删除文件夹
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
         
        String rootPath = Global.getRootPath() + "/netdisk/";

        String tableid = "table" + code;

        sb.append("<table id=" + tableid + " name=" + tableid + " class='" + tableClass + "' onclick=selectTree('"+code+"','"+name+"') cellspacing=0 cellpadding=0 width='100%' align=center  border=0>");
        sb.append("    <tbody><tr>");
        sb.append("        <td height='13' align=left nowrap> ");
        // for (int k = 1; k <= layer - 1; k++) {
        for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) {
            boolean isShowed = false;
            Iterator ir = UprightLineNodes.iterator();
            while (ir.hasNext()) {
                UprightLineNode node = (UprightLineNode) ir.next();
                //如果在K层上存在一个竖线结点则画出
                if (node.getLayer() == k) {
                    //node.show(sb, rootPath + "images/n_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //System.out.println("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
            	sb.append("<img src='" + Global.getRootPath() + "/images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
        }


        String folderImg = "folder_netdisk.gif";
        if (leaf.isShared()) {
            folderImg = "folder_netdisk_share.gif";
        }
        String folderImgStr = "<img id='img" + code + "' name='img" + code + "' src='" + rootPath + "images/" + folderImg + "' align='absmiddle'>";

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
        	sb.append("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "images/n_puls-root.gif' align='absmiddle'>" + folderImgStr);
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                	sb.append("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "images/n_plus2-2.gif' align='absmiddle'>" + folderImgStr);
                else
                	sb.append(folderImgStr);
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                	sb.append("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "images/n_plus2-1.gif' align='absmiddle'>" + folderImgStr);
                else
                	sb.append(folderImgStr);
            }
        }

        // 三种类型节点，用同一个Link
//        if (!queryString.equals(""))
//            out.print(
//                    "<span id='span" + code + "' name='span" + code + "'> <a target='" + target + "' href='" + link + "?dir_code=" +
//                code + "&" + queryString + "' onmouseup='onMouseUp(\"" + code + "\",\"" + name +"\")'>" + name + "</a></span>");
//        else
//            out.print(
//                "<span id='span" + code + "' name='span" + code + "'> <a target='" + target + "' href='" + link + "?dir_code=" +
//                code + "' onmouseup='onMouseUp(\"" + code + "\",\"" + name + "\")>" + name + "</a></span>");
        
        //新网盘Dialog树
        if (!queryString.equals(""))
        	sb.append(
                    "<span id='spanTree" + code + "' name='spanTree" + code + "' class='selectTree'>" + name+ "</span>");
        else
        	sb.append(
                "<span id='spanTree" + code + "' name='spanTree" + code + "'><a onclick='selectTree()'> " + name + code+ "</a></span>");
        
        
        sb.append("     </td>");
        sb.append("  </tr></tbody></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSimple(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String queryString, String tableClass, String tableClassMouseOn) throws Exception {
        // String code = leaf.getCode();
        String code = StrUtil.UrlEncode(leaf.getCode());

        String name = leaf.getName();
        if (name.equals(""))
        	name = "&nbsp;"; // 防止当出错时，如果没有文件夹的名称则无法删除文件夹
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
         
        String rootPath = Global.getRootPath() + "/netdisk/";

        String tableid = "table" + code;

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
                    //node.show(out, rootPath + "images/n_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //System.out.println("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='" + Global.getRootPath() + "/images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
        }


        String folderImg = "folder_netdisk.gif";
        if (leaf.isShared()) {
            folderImg = "folder_netdisk_share.gif";
        }
        String folderImgStr = "<img id='img" + code + "' name='img" + code + "' src='" + rootPath + "images/" + folderImg + "' align='absmiddle'>";

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "images/n_puls-root.gif' align='absmiddle'>" + folderImgStr);
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "images/n_plus2-2.gif' align='absmiddle'>" + folderImgStr);
                else
                    out.println(folderImgStr);
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "images/n_plus2-1.gif' align='absmiddle'>" + folderImgStr);
                else
                    out.println(folderImgStr);
            }
        }

        // 三种类型节点，用同一个Link
//        if (!queryString.equals(""))
//            out.print(
//                    "<span id='span" + code + "' name='span" + code + "'> <a target='" + target + "' href='" + link + "?dir_code=" +
//                code + "&" + queryString + "' onmouseup='onMouseUp(\"" + code + "\",\"" + name +"\")'>" + name + "</a></span>");
//        else
//            out.print(
//                "<span id='span" + code + "' name='span" + code + "'> <a target='" + target + "' href='" + link + "?dir_code=" +
//                code + "' onmouseup='onMouseUp(\"" + code + "\",\"" + name + "\")>" + name + "</a></span>");
        
        //新网盘Dialog树
        if (!queryString.equals(""))
            out.print(
                    "<span id='spanTree" + code + "' name='spanTree" + code + "' class='selectTree'><a onclick='selectTree(\"" + code + "\",\"" + name +"\")' > " + name+ "</a></span>");
        else
            out.print(
                "<span id='spanTree" + code + "' name='spanTree" + code + "'><a onclick='selectTree()'> " + name + code+ "</a></span>");
        
        
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
                   // node.show(out, "images/n_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //System.out.println("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='' width=20 height=1>");
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

        if (leaf.getType() == 2) {
            out.print("<a target=_parent href='document_list_m.jsp?dir_code=" +
                      StrUtil.UrlEncode(code) + "&dir_name=" +
                      StrUtil.UrlEncode(name) + "'>" + name +
                      "</a>");
        } else if (leaf.getType() == 1)
            out.print(
                    "<a target=_parent href='dir_list.jsp?op=editarticle&dir_code=" +
                    StrUtil.UrlEncode(code) + "&dir_name=" +
                    StrUtil.UrlEncode(name) + "'>" + name + "</a>");
        else if (leaf.getType() == 0) {
            out.print(name);
        }

        out.print("     </td><td width='15%' align=right nowrap>");
        if (!leaf.getCode().equals("root")) {
            out.print(
                    "<a target=_parent href='dir_priv_m.jsp?dirCode=" +
                    StrUtil.UrlEncode(code, "utf-8") + "'>共享</a>&nbsp;");
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
            out.print(
                    "<a target=_self href=# onClick=\"if (window.confirm('您确定要删除" +
                    name +
                    "吗?')) window.location.href='dir_top.jsp?op=del&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&delcode=" +
                    StrUtil.UrlEncode(code, "utf-8") + "'\">删除</a>&nbsp;");
            out.print("<a href='dir_top.jsp?op=move&direction=up&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>上移</a>&nbsp;");
            out.print("<a href='dir_top.jsp?op=move&direction=down&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;");
        }
        out.println("  </td></tr></tbody></table>");
    }

    void ShowLeafAsOption(JspWriter out, Leaf leaf, int rootlayer)
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
        if (leaf.getChildCount() > 0) {
            out.print("<option value='" + code + "'>" +
                      blank + "╋ " + name + "</option>");
        } else {
            out.print("<option value=\"" + code + "\">" +
                      blank + "├" + name +
                      "</option>");
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
        out.print("<option value='" + code + "'>" +  blank + "╋ <img src='images/folder.gif' align='absmiddle' />" + name + "</option>");
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
    void ListTreeFunc(JspWriter out, Leaf leaf,
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

    void ShowLeafFunc(JspWriter out, Leaf leaf,
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
                    //node.show(out, "images/n_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //System.out.println("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='' width=20 height=1>");
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
