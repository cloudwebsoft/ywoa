package com.redmoon.forum.plugin.entrance;

import cn.js.fan.module.cms.DirectoryView;
import cn.js.fan.module.cms.Leaf;
import javax.servlet.jsp.JspWriter;
import java.util.Iterator;
import java.util.Vector;
import cn.js.fan.module.cms.UprightLineNode;
import cn.js.fan.util.NumberUtil;

/**
 * <p>Title: </p>
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
public class VIPCMSDirectoryView extends DirectoryView {
    public VIPCMSDirectoryView(Leaf rootLeaf) {
        super(rootLeaf);
    }

    public void ShowLeafFuncWithCheckbox(JspWriter out, Leaf leaf,
                                         boolean isLastChild, String target,
                                         String func, String tableClass,
                                         String tableClassMouseOn) throws
            Exception {
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
                if (count > 0) {
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

        out.println("<table id=" + tableid + " name=" + tableid + " class='" +
                    tableClass + "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='" +
                    tableClassMouseOn + "'\" onMouseOut=\"this.className='" +
                    tableClass + "'\" border=0>");
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
        if (leaf.getType() != leaf.TYPE_NONE) {
            // checkbox
            if (!code.equals(Leaf.ROOTCODE))
                out.print("<input type='checkbox' name='" + code + "' value='" +
                          name + "' onClick=\"" + func +
                          "('" +
                          code + "'," + NumberUtil.roundRMB(leaf.getPrice()) + ", this)\"> &nbsp; ");
            out.print(name + "&nbsp;&nbsp;价格：" +
                    NumberUtil.roundRMB(leaf.getPrice()));
        }
        else
            out.print(
                    "<a target='" + target + "' href='#'>" +
                    name + "</a>");
        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }
}
