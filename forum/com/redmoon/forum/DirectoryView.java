package com.redmoon.forum;

import cn.js.fan.util.StrUtil;
import java.util.Vector;
import javax.servlet.jsp.JspWriter;
import java.util.Iterator;
import org.apache.log4j.Logger;
import cn.js.fan.web.SkinUtil;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import com.redmoon.forum.plugin.PluginMgr;
import com.redmoon.forum.plugin.PluginUnit;
import com.redmoon.forum.ui.SkinMgr;

/**
 *
 * <p>Title:版块显示视类 </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DirectoryView {
    Logger logger = Logger.getLogger(Leaf.class.getName());
    Leaf rootLeaf;
    Vector UprightLineNodes = new Vector(); // 用于显示竖线
    HttpServletRequest request;
    Privilege privilege = new Privilege();

    public DirectoryView(Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
    }

    public DirectoryView(HttpServletRequest request, Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
        this.request = request;
    }

    public void list(JspWriter out) throws Exception {
        ListTree(out, rootLeaf, true);
    }

    // 显示根结点为leaf的树
    void ListTree(JspWriter out, Leaf leaf,
                  boolean isLastChild) throws Exception {
        ShowLeaf(out, leaf, isLastChild);
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
        String root_code = "";
        if (request!=null) {
            root_code = ParamUtil.get(request, "root_code"); // 当只管理root_code节点时
        }
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
                //System.out.println(leaf.getCode() + "'s all child count=" +
                //                   count);
                if (count>0) { // =0的也计入的话会在树底端的结点产生多余竖线
                    UprightLineNode uln = new UprightLineNode(layer, count);
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
                out.println("<img src='' style='visibility:hidden' width=20 height=1>");
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

        String sName = name;
        if (!leaf.getColor().equals(""))
            sName = "<font color='" + leaf.getColor() + "'>" + name + "</font>";
        if (leaf.isBold())
                sName = "<strong>" + sName + "</strong>";

        if (leaf.getType() == Leaf.TYPE_BOARD) {
            ThreadTypeDb ttd = new ThreadTypeDb();
            Vector v = ttd.getThreadTypesOfBoard(code);
            String num = "";
            if (v.size()>0)
                num = "(" + v.size() + ")";
            out.print(
                    "<a target=_blank href='../listtopic.jsp?boardcode=" +
                    StrUtil.UrlEncode(code) + "'>" + sName + num + "</a>");
        }
        else if (leaf.getType() == Leaf.TYPE_DOMAIN) {
            out.print(sName);
        }

        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(leaf.getCode());
        if (vplugin.size() > 0) {
            out.print("<font color=#aaaaaa>");
            Iterator irpluginnote = vplugin.iterator();
            while (irpluginnote.hasNext()) {
                PluginUnit pu = (PluginUnit) irpluginnote.next();
                out.print(pu.getName(request) + "&nbsp;");
            }
            out.print("</font>");
        }

        out.print("     </td><td width='15%' align=right nowrap>");
        if (!leaf.getCode().equals("root")) {
            if (leaf.getIsHome())
                out.print(SkinUtil.LoadString(request, "res.forum.DirectoryView", "info_home") + "&nbsp;");
            if (leaf.isLocked())
                out.print(SkinUtil.LoadString(request, "res.forum.DirectoryView", "info_lock") + "&nbsp;");
            out.print("<a target=_parent href='manager_m.jsp?boardcode=" + StrUtil.UrlEncode(code) +
                      "&boardname=" + StrUtil.UrlEncode(name) +
                      "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_manager") + "</a>&nbsp;");
            if (leaf.getType() == Leaf.TYPE_BOARD)
                out.print(SkinUtil.LoadString(request, "res.forum.DirectoryView", "info_board") + "&nbsp;");
            else if (leaf.getType() == 2)
                out.print(SkinUtil.LoadString(request, "res.forum.DirectoryView", "info_sub_board") + "&nbsp;");
            else
                out.print(SkinUtil.LoadString(request, "res.forum.DirectoryView", "info_sub_field") + "&nbsp;");

            if (leaf.getType() == Leaf.TYPE_BOARD) {
                out.print(
                        "<a target='_parent' href='board_threadtype_list.jsp?boardCode=" +
                        StrUtil.UrlEncode(code, "utf-8") +
                        "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_threadtype") + "</a>&nbsp;");
            }

            out.print(
                    "<a target=dirbottomFrame href='dir_bottom.jsp?parent_code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                    StrUtil.UrlEncode(name, "utf-8") +
                    "&op=AddChild'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_addchild") + "</a>&nbsp;");
            out.print(
                    "<a target='dirbottomFrame' href='dir_bottom.jsp?op=modify&code=" +
                    StrUtil.UrlEncode(code) + "&name=" +
                    StrUtil.UrlEncode(name) + "&description=" +
                    StrUtil.UrlEncode(description) + "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_modify") + "</a>&nbsp;");
            out.print(
                    "<a target=_self href=# onClick=\"if (window.confirm('" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "confirm_del") + "')) window.location.href='dir_top.jsp?op=del&delcode=" +
                    StrUtil.UrlEncode(code) + "'\">" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_del") + "</a>&nbsp;");
            out.print("<a href='dir_top.jsp?op=move&direction=up&root_code=" + StrUtil.UrlEncode(root_code) + "&code=" +
                      StrUtil.UrlEncode(code) + "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_move_up") + "</a>&nbsp;");
            out.print("<a href='dir_top.jsp?op=move&direction=down&root_code=" + StrUtil.UrlEncode(root_code) + "&code=" +
                      StrUtil.UrlEncode(code) + "'>" + SkinUtil.LoadString(request, "res.forum.DirectoryView", "link_move_down") + "</a>&nbsp;");
            // out.print("<a href='dir_top.jsp?root_code=" +
            //          StrUtil.UrlEncode(code, "utf-8") + "'>管理</a>");
        }
        out.println("  </td></tr></tbody></table>");
    }

    void ShowLeafAsOption(JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer - rootlayer;
        for (int i = 0; i < d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            if (leaf.getType()==leaf.TYPE_BOARD)
                out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "├『" + name + "</option>");
            else
                out.print("<option value='not'>" + blank + "╋ " + name + "</option>");
        }
        else {
            if (leaf.getType()==leaf.TYPE_BOARD)
                out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
            else
                out.print("<option value='not'>" + blank + "├『" + name +
                      "』</option>");
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptions(HttpServletRequest request, Privilege privilege, JspWriter out, Leaf leaf, int rootlayer, boolean isShowHide) throws Exception {
        if (!isShowHide) {
            if (!leaf.isDisplay(request, privilege))
                return;
        }
        ShowLeafAsOption(out, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            ShowDirectoryAsOptions(request, privilege, out, childlf, rootlayer, isShowHide);
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptions(HttpServletRequest request, Privilege privilege, JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        ShowDirectoryAsOptions(request, privilege, out, leaf, rootlayer, false);
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

    public void ListFunc(HttpServletRequest request, JspWriter out, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeFunc(request, out, rootLeaf, true, target, func, tableClass, tableClassMouseOn);
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
    void ListTreeFunc(HttpServletRequest request, JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafFunc(request, out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
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
            ListTreeFunc(request, out, childlf, isLastChild1, target, func, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    void ShowLeafFunc(HttpServletRequest request, JspWriter out, Leaf leaf,
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
                "<a target='" + target + "' href='#' onClick=\"" + func + "('" + code + "', '" + StrUtil.UrlEncode(name) + "')\">" + name + "</a>");

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    public void ListFuncWithCheckbox(JspWriter out, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeFuncWithCheckbox(out, rootLeaf, true, target, func, tableClass, tableClassMouseOn);
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
    void ListTreeFuncWithCheckbox(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafFuncWithCheckbox(out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
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
            ListTreeFuncWithCheckbox(out, childlf, isLastChild1, target, func, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    public void ShowLeafFuncWithCheckbox(JspWriter out, Leaf leaf,
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
                out.println("<img src='' width=20 height=1>");
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

        // checkbox
        if (code.equals(Leaf.CODE_ROOT) || leaf.getType()==leaf.TYPE_BOARD)
            out.print("<input type='checkbox' name='" + code + "' value='" + name + "'>&nbsp;");
        // 三种类型节点，用同一个Link
        out.print(
                "<a target='" + target + "' href='#' onClick=\"" + func + "('" + code + "')\">" + name + "</a>");

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }


    public void ListSimple(HttpServletRequest request, JspWriter out, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeSimple(request, out, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    void ListTreeSimple(HttpServletRequest request, JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        if (leaf.isHome)
            ShowLeafSimple(request, out, leaf, isLastChild, target, link, tableClass, tableClassMouseOn);
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
            if (childlf.isHome)
                ListTreeSimple(request, out, childlf, isLastChild1, target, link, tableClass, tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSimple(HttpServletRequest request, JspWriter out, Leaf leaf,
                        boolean isLastChild, String target, String link,
                        String tableClass, String tableClassMouseOn) throws
            Exception {
        if (!leaf.isDisplay(request, privilege))
            return;
        String rootPath = request.getContextPath();
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

        String skinPath = SkinMgr.getSkinPath(request);

        out.println("<table id=" + tableid + " name=" + tableid + " class='" + tableClass + "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='" + tableClassMouseOn + "'\" onMouseOut=\"this.className='" + tableClass + "'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td height='13' align=left nowrap>");
        // for (int k = 1; k <= layer - 1; k++) {
        for (int k = rootLeaf.getLayer()+1; k <= layer - 1; k++) {
            boolean isShowed = false;
            Iterator ir = UprightLineNodes.iterator();
            while (ir.hasNext()) {
                UprightLineNode node = (UprightLineNode) ir.next();
                // 如果在K层上存在一个竖线结点则画出
                if (node.getLayer() == k) {
                    node.show(out, rootPath + "/forum/" + skinPath + "/images/board_tree/i_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //System.out.println("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='' style='visibility:hidden' width=20 height=1>");
        }

        String folderImg = "folder_01.gif";
        String folderImgStr = "<img id='imgFold" + code + "' name='imgFold" + code + "' src='" + rootPath + "/forum/" + skinPath + "/images/board_tree/" + folderImg + "' align='absmiddle'>";

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img id='img_" + tableid + "' tableRelate='' onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/forum/" + skinPath + "/images/board_tree/i_puls-root.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img id='img_" + tableid + "' tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/forum/" + skinPath + "/images/board_tree/i_plus2-2.gif' align='absmiddle'>" + folderImgStr);
                else
                    out.println("<img src='" + rootPath + "/forum/" + skinPath + "/images/board_tree/i_plus-2-3.gif' align='absmiddle'>" + folderImgStr);
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img id='img_" + tableid + "' tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/forum/" + skinPath + "/images/board_tree/i_plus2-1.gif' align='absmiddle'>" + folderImgStr);
                else
                    out.println("<img src='" + rootPath + "/forum/" + skinPath + "/images/board_tree/i_plus-2-2.gif' align='absmiddle'>" + folderImgStr);
            }
        }

        // 三种类型节点，用同一个Link
        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.print(
                    "<a target='" + target + "' href='" + link + "?boardcode=" +
                    StrUtil.UrlEncode(code) + "'>" + name + "</a>");
        }
        else {
            String sName = name;
            if (!leaf.getColor().equals(""))
                sName = "<font color='" + leaf.getColor() + "'>" + name + "</font>";
            if (leaf.isBold())
                sName = "<strong>" + sName + "</strong>";
            out.print(
                    "<a target='" + target + "' href='" + link + "?boardcode=" +
                    StrUtil.UrlEncode(code) + "' onmouseup='onMouseUp(\"" +
                    code + "\")'>" + sName + "</a>");
        }
        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }
}
