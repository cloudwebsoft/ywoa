package cn.js.fan.module.cms;

import java.util.*;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

import cn.js.fan.module.cms.plugin.*;
import cn.js.fan.module.pvg.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

public class DirectoryView {
    public Logger logger;
    public Leaf rootLeaf;
    public Vector UprightLineNodes = new Vector(); //用于显示竖线
    HttpServletRequest request;

    public DirectoryView(Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
        logger = Logger.getLogger(Leaf.class.getName());
    }

    public DirectoryView(HttpServletRequest request, Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
        this.request = request;
    }

    public void ListSimpleAjax(JspWriter out, String target, String link, String tableClass, String tableClassMouseOn, boolean isShowRoot) throws Exception {
        Privilege privilege = new Privilege();
        ListTreeSimpleAjax(privilege, out, rootLeaf, true, target, link, tableClass, tableClassMouseOn, isShowRoot);
    }

    /**
     * 选择某个节点
     * @param out JspWriter
     * @param target String
     * @param link String
     * @param tableClass String
     * @param tableClassMouseOn String
     * @param isShowRoot boolean
     * @throws Exception
     */
    public void SelectSingleAjax(JspWriter out, String func, String tableClass, String tableClassMouseOn, boolean isShowRoot) throws Exception {
        Privilege privilege = new Privilege();
        SelectTreeSingleAjax(privilege, out, rootLeaf, func, tableClass, tableClassMouseOn, isShowRoot);
    }

    void SelectTreeSingleAjax(Privilege privilege, JspWriter out, Leaf leaf,
                        String func,
                        String tableClass, String tableClassMouseOn, boolean isShowRoot) throws
            Exception {
        if (isShowRoot)
            ShowLeafSingleAjax(privilege, out, leaf, func, tableClass,
                           tableClassMouseOn);

        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0)
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            ShowLeafSingleAjax(privilege, out, childlf, func, tableClass,
                           tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }


    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSingleAjax(Privilege privilege, JspWriter out, Leaf leaf,
                  String func, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();

        int childcount = leaf.getChildCount();

        String tableid = leaf.getCode();

        out.println("<table id=" + tableid + " name=" + tableid + " class='" + tableClass + "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='" + tableClassMouseOn + "'\" onMouseOut=\"this.className='" + tableClass + "'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td height='20' align=left nowrap>");

        int padWidth = 0;
        for (int k = 1; k <= layer - 1; k++) {
            padWidth += 21;
        }
        if (childcount==0)
            padWidth += 16;
        out.print("<img src='' width=" + padWidth + " height=1>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0)
                out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus.gif' align='absmiddle'><img src='' width=3px height=1><img src='images/folder_01.gif' align='absmiddle'>");
            else
                out.println("<img src='images/folder_01.gif' align='absmiddle'>");
        }

        // 三种类型节点，用同一个Link
        if (leaf.getType()==Leaf.TYPE_COLUMN) {
            out.print(
                    "<a href=\"javascript:" + func
                     + "('" + code + "','" + name + "')\" class=\"column\">" + name + "</a>");
        }
        else if (leaf.getType() == Leaf.TYPE_SUB_SITE) {
            out.print(
                     "<a href=\"javascript:" + func
                      + "('" + code + "','" + name + "')\" class=\"subsite\">" + name + "</a>");
        }
        else {
            out.print(
                     "<a href=\"javascript:" + func
                      + "('" + code + "','" + name + "')\">" + name + "</a>");
        }
        out.print("     </td>");
        out.println("  </tr></tbody></table>");

        LeafPriv lp = new LeafPriv(leaf.getCode());
        if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
            if (!leaf.getCode().equals("root")) {
                out.println("<script>\n");
                out.println("tableid.style.display='none';\n");
                out.println("</script>\n");
            }
        }
    }

    // 显示根结点为leaf的树
    void ListTreeSimpleAjax(Privilege privilege, JspWriter out, Leaf leaf,
                        boolean isLastChild, String target, String link,
                        String tableClass, String tableClassMouseOn, boolean isShowRoot) throws
            Exception {
        if (isShowRoot)
            ShowLeafSimpleAjax(privilege, out, leaf, isLastChild, target, link, tableClass,
                           tableClassMouseOn);

        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        if (size > 0)
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ShowLeafSimpleAjax(privilege, out, childlf, isLastChild, target, link, tableClass,
                           tableClassMouseOn);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    public void ListSimple(JspWriter out, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        Privilege privilege = new Privilege();
        ListTreeSimple(privilege, out, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    void ListTreeSimple(Privilege privilege, JspWriter out, Leaf leaf,
                        boolean isLastChild, String target, String link,
                        String tableClass, String tableClassMouseOn) throws
            Exception {
        // 非前台节点不显示
        // if (leaf.getIsHome()) {
            ShowLeafSimple(privilege, out, leaf, isLastChild, target, link, tableClass,
                           tableClassMouseOn);
        // }

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
            // 非前台节点不显示
            // if (childlf.getIsHome()) {
                ListTreeSimple(privilege, out, childlf, isLastChild1, target, link, tableClass, tableClassMouseOn);
            // }
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSimple(Privilege privilege, JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
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

        // 三种类型节点，用同一个Link
        if (leaf.getType()==Leaf.TYPE_COLUMN) {
            out.print(
                    "<a target='" + target + "' href='" + link +
                    StrUtil.UrlEncode(code) + "' class='column'>" + name + "</a>");
        }
        else if (leaf.getType() == Leaf.TYPE_SUB_SITE) {
            out.print("<a target='" + target + "' href='" + link +
                      StrUtil.UrlEncode(code) + "' class='subsite'>" + name +
                      "</a>");
        }
        else {
            out.print(
                    "<a target='" + target + "' href='" + link +
                    StrUtil.UrlEncode(code) + "'>" + name + "</a>");
        }
        out.print("     </td>");
        out.println("  </tr></tbody></table>");

        LeafPriv lp = new LeafPriv(leaf.getCode());
        if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
            if (!leaf.getCode().equals("root")) {
                out.println("<script>\n");
                out.println("tableid.style.display='none';\n");
                out.println("</script>\n");
            }
        }
    }

    public void listAjax(HttpServletRequest request, JspWriter out, boolean isShowRoot) throws Exception {
        Privilege privilege = new Privilege();
        ListTreeAjax(request, privilege, out, rootLeaf, true, isShowRoot);
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
            LeafPriv lp = new LeafPriv(leaf.getCode());
            String style = "";
            if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
                if (!leaf.getCode().equals("root")) {
                    style = "style='display:'";// 设置display为none将不会显示其子节点
                }
            }
            out.print("<table id='childof" + leaf.getCode() +
                      "' " + style + " cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
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
    void ShowLeafSimpleAjax(Privilege privilege, JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();

        int childcount = leaf.getChildCount();

        String tableid = leaf.getCode();

        out.println("<table id=" + tableid + " name=" + tableid + " class='" + tableClass + "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='" + tableClassMouseOn + "'\" onMouseOut=\"this.className='" + tableClass + "'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td height='20' align=left nowrap>");

        int padWidth = 0;
        for (int k = 1; k <= layer - 1; k++) {
            padWidth += 21;
        }
        if (childcount==0)
            padWidth += 16;
        out.print("<img src='' width=" + padWidth + " height=1>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0)
                out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus.gif' align='absmiddle'><img src='' width=3px height=1><img src='images/folder_01.gif' align='absmiddle'>");
            else
                out.println("<img src='images/folder_01.gif' align='absmiddle'>");
        }

        // 三种类型节点，用同一个Link
        if (leaf.getType()==Leaf.TYPE_COLUMN) {
            out.print(
                    "<a target='" + target + "' href='" + link +
                    StrUtil.UrlEncode(code) + "' class='column'>" + name + "</a>");
        }
        else if (leaf.getType() == Leaf.TYPE_SUB_SITE) {
            out.print("<a target='" + target + "' href='" + link +
                      StrUtil.UrlEncode(code) + "' class='subsite'>" + name +
                      "</a>");
        }
        else {
            out.print(
                    "<a target='" + target + "' href='" + link +
                    StrUtil.UrlEncode(code) + "'>" + name + "</a>");
        }
        out.print("     </td>");
        out.println("  </tr></tbody></table>");

        LeafPriv lp = new LeafPriv(leaf.getCode());
        if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
            if (!leaf.getCode().equals("root")) {
                out.println("<script>\n");
                out.println(tableid + ".style.display='none';\n");
                out.println("</script>\n");
            }
        }
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafAjax(HttpServletRequest request, Privilege privilege, JspWriter out, Leaf leaf,
                  boolean isLastChild, boolean isShowRoot) throws Exception {
        String code = leaf.getCode();

        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

        int childcount = leaf.getChildCount();
        // System.out.println(code + " childcount=" + childcount);

        String tableid = leaf.getCode();

        LeafPriv lp = new LeafPriv(leaf.getCode());
        String style = "";
        if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
            if (!leaf.getCode().equals("root")) {
                style = "style='display:none'";
            }
        }

        out.println("<table id=" + tableid + " name=" + tableid + " " + style + " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td height='13' align=left nowrap>");

        out.print("<span style='float:right'>");
        if (lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
            if (!leaf.getCode().equals("root")) {
                String root_code = (String)request.getAttribute("root_code");
                if (root_code==null)
                    root_code = rootLeaf.getCode();

                if (leaf.getIsHome())
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_home") + "&nbsp;");
                // out.print("<a href='dir_top.jsp?op=removecache&code=" + code +
                //          "'>清缓存</a>&nbsp;");
                if (leaf.getType() == 1)
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_doc") + "&nbsp;");
                else if (leaf.getType() == 2)
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_list") + "&nbsp;");
                else if (leaf.getType()==3) {
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_column") + "&nbsp;");
                }
                else if (leaf.getType()==Leaf.TYPE_SUB_SITE) {
                    out.print(SkinUtil.LoadString(request,
                                                  "res.label.cms.dir",
                                                  "sub_site") + "&nbsp;");
                }
                else
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_none") + "&nbsp;");

                // 子站点不允许设置权限
                if (privilege.isUserPrivValid(request, "admin") || !Leaf.isLeafOfSubsite(code)) {
                    out.print(
                            "<a target=_parent href='dir_priv_m.jsp?dirCode=" +
                            StrUtil.UrlEncode(code, "utf-8") + "'>" +
                            SkinUtil.
                            LoadString(request, "res.cms.DirectoryView",
                                       "link_pvg") +
                            "</a>&nbsp;");
                }
                out.print(
                        "<a target=dirbottomFrame href='dir_bottom.jsp?parent_code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                        StrUtil.UrlEncode(name, "utf-8") +
                        "&op=AddChild'>" +
                        SkinUtil.LoadString(request, "res.cms.DirectoryView",
                                            "link_addchild") + "</a>&nbsp;");
                out.print(
                        "<a target='dirbottomFrame' href='dir_bottom.jsp?op=modify&code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "&name=" +
                        StrUtil.UrlEncode(name, "utf-8") + "&description=" +
                        StrUtil.UrlEncode(description, "utf-8") + "'>" +
                        SkinUtil.LoadString(request, "res.cms.DirectoryView",
                                            "link_modify") + "</a>&nbsp;");


                    out.print(
                            "<a target=_self href=# onClick=\"if (window.confirm('" +
                            SkinUtil.LoadString(request, "res.cms.DirectoryView",
                                                "confirm_del") +
                            "')) window.location.href='dir_top_ajax.jsp?op=del&root_code=" +
                            StrUtil.UrlEncode(root_code) + "&delcode=" +
                            StrUtil.UrlEncode(code, "utf-8") + "'\">" +
                            SkinUtil.
                            LoadString(request, "res.cms.DirectoryView", "link_del") +
                            "</a>&nbsp;");

                out.print(
                        "<a href='dir_top_ajax.jsp?op=move&direction=up&root_code=" +
                        StrUtil.UrlEncode(root_code) + "&code=" +
                        StrUtil.UrlEncode(code) + "'>" +
                        SkinUtil.
                        LoadString(request, "res.cms.DirectoryView", "link_move_up") +
                        "</a>&nbsp;");
                out.print(
                        "<a href='dir_top_ajax.jsp?op=move&direction=down&root_code=" +
                        StrUtil.UrlEncode(root_code) + "&code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'>" +
                        SkinUtil.
                        LoadString(request, "res.cms.DirectoryView", "link_move_down") +
                        "</a>&nbsp;");
                // out.print("<a href='dir_top.jsp?root_code=" +
                //           StrUtil.UrlEncode(code, "utf-8") + "'>管理</a>");
            }
        }
        out.print("</span>");

        int padWidth = 0;
        for (int k = 1; k <= layer - 1; k++) {
            padWidth += 21;
        }
        if (childcount==0)
            padWidth += 16;
        out.print("<img src='" + request.getContextPath() + "/images/spacer.gif' width=" + padWidth + " height=1>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img tableRelate='' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0)
                out.println("<img tableRelate='" + tableid +
                            "' onClick=\"ShowChild(this, '" + leaf.getCode() + "')\" src='images/i_plus.gif' align='absmiddle'><img src='' width=3px height=1><img src='images/folder_01.gif' align='absmiddle'>");
            else
                out.println(
                        "<img src='images/folder_01.gif' align='absmiddle'>");
        }

        if (lp.canUserSeeWithAncestorNode(privilege.getUser(request)) || leaf.getCode().equals(Leaf.ROOTCODE)) {
            if (leaf.getCode().equals(Leaf.ROOTCODE)) {
                out.print(
                        "<a target=_parent href='#'>" + name +
                        "</a>");
            }
            else if (leaf.getType()==Leaf.TYPE_COLUMN) {
                out.print(
                        "<a target=_parent href='column.jsp?dir_code=" +
                        StrUtil.UrlEncode(code) + "' class='column'>" + name +
                        "</a>");
            }
            else if (leaf.getType() == 2) {
                out.print(
                        "<a target=_parent href='document_list_m.jsp?dir_code=" +
                        StrUtil.UrlEncode(code) + "&dir_name=" +
                        StrUtil.UrlEncode(name) + "'>" + name +
                        "</a>");
            } else if (leaf.getType() == 1)
                out.print(
                        "<a target=_parent href='../" + DocumentMgr.getWebEditPage() + "?op=editarticle&dir_code=" +
                        StrUtil.UrlEncode(code) + "&dir_name=" +
                        StrUtil.UrlEncode(name) + "'>" + name + "</a>");
            else if (leaf.getType() == Leaf.TYPE_SUB_SITE) {
                out.print("<a target=_parent href='subsite.jsp?dir_code=" +
                        StrUtil.UrlEncode(code) + "' class='subsite'>" + name +
                        "</a>");
            }
            else if (leaf.getType() == 0) {
                out.print(name);
            }
        }
        else
            out.print("******");

        if (!leaf.getPluginCode().equals("")) {
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(leaf.getPluginCode());
            if (pu != null)
                out.print("-[" + pu.getName(request) + "]");
        }

        // out.print("     </td><td width='50%' align=right nowrap>");

        out.print("  </td></tr></tbody></table>");
    }

    public void list(HttpServletRequest request, JspWriter out) throws Exception {
        Privilege privilege = new Privilege();
        ListTree(request, privilege, out, rootLeaf, true);
    }

    // 显示根结点为leaf的树
    void ListTree(HttpServletRequest request, Privilege privilege, JspWriter out, Leaf leaf,
                  boolean isLastChild) throws Exception {
        ShowLeaf(request, privilege, out, leaf, isLastChild);
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
        if (size > 0) {
            LeafPriv lp = new LeafPriv(leaf.getCode());
            String style = "";
            if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
                if (!leaf.getCode().equals("root")) {
                    style = "style='display:'";// 设置display为none将不会显示其子节点
                }
            }
            out.print("<table id='childoftable" + leaf.getCode() +
                      "' " + style + " cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            ListTree(request, privilege, out, childlf, isLastChild1);
        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeaf(HttpServletRequest request, Privilege privilege, JspWriter out, Leaf leaf,
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

        LeafPriv lp = new LeafPriv(leaf.getCode());
        String style = "";
        if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
            if (!leaf.getCode().equals("root")) {
                style = "style='display:none'";
            }
        }

        out.println("<table id=" + tableid + " name=" + tableid + " " + style + " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" border=0>");
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
                out.println("<img src='' width=20 height=1>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img tableRelate='' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            }
        }

        if (lp.canUserSeeWithAncestorNode(privilege.getUser(request)) || leaf.getCode().equals(Leaf.ROOTCODE)) {
            if (leaf.getCode().equals(Leaf.ROOTCODE)) {
                out.print(
                        "<a target=_parent href='#'>" + name +
                        "</a>");
            }
            else if (leaf.getType()==Leaf.TYPE_COLUMN) {
                out.print(
                        "<a target=_parent href='doc_column_view.jsp?dir_code=" +
                        StrUtil.UrlEncode(code) + "' class='column'>" + name +
                        "</a>");
            }
            else if (leaf.getType() == 2) {
                out.print(
                        "<a target=_parent href='document_list_m.jsp?dir_code=" +
                        StrUtil.UrlEncode(code) + "&dir_name=" +
                        StrUtil.UrlEncode(name) + "'>" + name +
                        "</a>");
            } else if (leaf.getType() == 1)
                out.print(
                        "<a target=_parent href='../" +
                        DocumentMgr.getWebEditPage() +
                        "?op=editarticle&dir_code=" +
                        StrUtil.UrlEncode(code) + "&dir_name=" +
                        StrUtil.UrlEncode(name) + "'>" + name + "</a>");
            else if (leaf.getType() == Leaf.TYPE_SUB_SITE) {
                out.print("<a target=_parent href='subsite.jsp?dir_code=" +
                          StrUtil.UrlEncode(code) + "' class='subsite'>" + name +
                          "</a>");
            }
            else if (leaf.getType() == 0) {
                out.print(name);
            }
        }
        else
            out.print("******");

        if (!leaf.getPluginCode().equals("")) {
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(leaf.getPluginCode());
            if (pu != null)
                out.print("-[" + pu.getName(request) + "]");
        }

        out.print("     </td><td width='15%' align=right nowrap>");
        if (lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
            if (!leaf.getCode().equals("root")) {
                if (leaf.getIsHome())
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_home") + "&nbsp;");
                // out.print("<a href='dir_top.jsp?op=removecache&code=" + code +
                //          "'>清缓存</a>&nbsp;");
                if (leaf.getType() == 1)
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_doc") + "&nbsp;");
                else if (leaf.getType() == 2)
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_list") + "&nbsp;");
                else if (leaf.getType()==3) {
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_column") + "&nbsp;");
                }
                else
                    out.print(SkinUtil.LoadString(request,
                                                  "res.cms.DirectoryView",
                                                  "info_type_none") + "&nbsp;");

                out.print(
                        "<a target=_parent href='dir_priv_m.jsp?dirCode=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'>" +
                        SkinUtil.
                        LoadString(request, "res.cms.DirectoryView", "link_pvg") +
                        "</a>&nbsp;");
                out.print(
                        "<a target=dirbottomFrame href='dir_bottom.jsp?parent_code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                        StrUtil.UrlEncode(name, "utf-8") +
                        "&op=AddChild'>" +
                        SkinUtil.LoadString(request, "res.cms.DirectoryView",
                                            "link_addchild") + "</a>&nbsp;");
                out.print(
                        "<a target='dirbottomFrame' href='dir_bottom.jsp?op=modify&code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "&name=" +
                        StrUtil.UrlEncode(name, "utf-8") + "&description=" +
                        StrUtil.UrlEncode(description, "utf-8") + "'>" +
                        SkinUtil.LoadString(request, "res.cms.DirectoryView",
                                            "link_modify") + "</a>&nbsp;");
                out.print(
                        "<a target=_self href=# onClick=\"if (window.confirm('" +
                        SkinUtil.LoadString(request, "res.cms.DirectoryView",
                                            "confirm_del") +
                        "')) window.location.href='dir_top.jsp?op=del&root_code=" +
                        StrUtil.UrlEncode(rootLeaf.getCode()) + "&delcode=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'\">" +
                        SkinUtil.
                        LoadString(request, "res.cms.DirectoryView", "link_del") +
                        "</a>&nbsp;");
                out.print(
                        "<a href='dir_top.jsp?op=move&direction=up&root_code=" +
                        StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'>" +
                        SkinUtil.
                        LoadString(request, "res.cms.DirectoryView", "link_move_up") +
                        "</a>&nbsp;");
                out.print(
                        "<a href='dir_top.jsp?op=move&direction=down&root_code=" +
                        StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                        StrUtil.UrlEncode(code, "utf-8") + "'>" +
                        SkinUtil.
                        LoadString(request, "res.cms.DirectoryView", "link_move_down") +
                        "</a>&nbsp;");
                // out.print("<a href='dir_top.jsp?root_code=" +
                //           StrUtil.UrlEncode(code, "utf-8") + "'>管理</a>");
            }
        }
        out.print("  </td></tr></tbody></table>");
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

    /**
     * 显示可投稿的节点
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    void ShowLeafAsOptionForPost(JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.isPost()) {
            if (leaf.getChildCount() > 0) {
                if (leaf.getType() == leaf.TYPE_LIST ||
                    leaf.getType() == leaf.TYPE_DOCUMENT)
                    out.print("<option value='" + code +
                              "' style='COLOR: #0005ff'>" + blank + "╋ " + name +
                              "</option>");
                else
                    out.print("<option value='not'>" + blank + "╋ " + name +
                              "</option>");
            } else {
                if (leaf.getType() == leaf.TYPE_LIST ||
                    leaf.getType() == leaf.TYPE_DOCUMENT)
                    out.print("<option value=\"" + code +
                              "\" style='COLOR: #0005ff'>" + blank + "├『" +
                              name +
                              "』</option>");
                else
                    out.print("<option value='not'>" + blank + "├『" + name +
                              "』</option>");
            }
        }
    }


    /**
     * 显示可以投稿的目录
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    public void ShowDirectoryAsOptionsForPost(JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        if (leaf.isPost())
            ShowLeafAsOptionForPost(out, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            if (childlf.isPost())
                ShowDirectoryAsOptionsForPost(out, childlf, rootlayer);
        }
    }

    /**
     * 显示根结点为leaf的树
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
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
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0)
            out.print("<option value='" + code + "'>" + blank + "╋ " + name + "</option>");
        else
            out.print("<option value='" + code + "'>" + blank + "├『" + name + "』</option>");
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
        // if (!code.equals(Leaf.ROOTCODE))
            out.print("<input type='checkbox' name='" + code + "' value='" + name + "'>&nbsp;");
        // 三种类型节点，用同一个Link
        out.print(
                "<a target='" + target + "' href='#' onClick=\"" + func + "('" + code + "')\">" + name + "</a>");

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

}
