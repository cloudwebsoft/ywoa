package com.redmoon.oa.fileark;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class DirView extends DirectoryView {
	String userName = "";

	String FOLDER_IMG_CANSEE = "folder_cansee.gif";
	String FOLDER_IMG_CANNOTSEE = "folder_cannotsee.gif";
	String FOLDER_IMG_CANMODIFY = "folder_canmodify.gif";

	public DirView(HttpServletRequest request, Leaf rootLeaf) {
		super(request, rootLeaf);

		Privilege privilege = new Privilege();
		userName = privilege.getUser(request);
	}

	public String getFolderImg(Leaf leaf) {
		LeafPriv lp = new LeafPriv(leaf.getCode());
		if (!lp.canUserSee(userName))
			return FOLDER_IMG_CANNOTSEE;
		if (lp.canUserModify(userName))
			return FOLDER_IMG_CANMODIFY;
		return FOLDER_IMG_CANSEE;
	}

	/**
	 * 显示文件柜框架左侧的目录树，不显示isHome为false的节点
	 * 
	 * @param out
	 *            JspWriter
	 * @param leaf
	 *            Leaf
	 * @param isLastChild
	 *            boolean
	 * @param target
	 *            String
	 * @param link
	 *            String
	 * @param tableClass
	 *            String
	 * @param tableClassMouseOn
	 *            String
	 * @throws Exception
	 */
	public void ListTreeSimple(JspWriter out, Leaf leaf, boolean isLastChild,
			String target, String link, String tableClass,
			String tableClassMouseOn) throws Exception {
		LeafPriv lp = new LeafPriv(leaf.getCode());
		if (leaf.getIsHome()) {
			if (lp.canUserSee(userName))
				ShowLeafSimple(out, leaf, isLastChild, target, link,
						tableClass, tableClassMouseOn);
		}
		Directory dir = new Directory();
		Vector children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return;

		int i = 0;
		if (size > 0)
			out
					.print("<table id='childoftable"
							+ leaf.getCode()
							+ "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
		Iterator ri = children.iterator();
		// 写跟贴
		while (ri.hasNext()) {
			i++;
			Leaf childlf = (Leaf) ri.next();
			boolean isLastChild1 = true;
			if (size != i)
				isLastChild1 = false;
			if (childlf.getIsHome()) {
				lp.setDirCode(childlf.getCode());

				if (lp.canUserSee(userName))
					ListTreeSimple(out, childlf, isLastChild1, target, link,
							tableClass, tableClassMouseOn);
			}
		}
		if (size > 0)
			out.print("</td></tr></table>");
	}

	public void ListSimpleAjax(JspWriter out, String target, String link,
			String tableClass, String tableClassMouseOn, boolean isShowRoot)
			throws Exception {
		Privilege privilege = new Privilege();
		ListTreeSimpleAjax(privilege, out, rootLeaf, true, target, link,
				tableClass, tableClassMouseOn, isShowRoot);
	}

	// 显示根结点为leaf的树
	void ListTreeSimpleAjax(Privilege privilege, JspWriter out, Leaf leaf,
			boolean isLastChild, String target, String link, String tableClass,
			String tableClassMouseOn, boolean isShowRoot) throws Exception {
		LeafPriv lp = new LeafPriv(leaf.getCode());
		if (isShowRoot) {
			if (leaf.getIsHome() && lp.canUserSee(userName)) {
				ShowLeafSimpleAjax(privilege, out, leaf, isLastChild, target,
						link, tableClass, tableClassMouseOn);
			}
		}
		Directory dir = new Directory();
		Vector children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return;

		int i = 0;
		if (size > 0)
			out
					.print("<table id='childof"
							+ leaf.getCode()
							+ "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
		Iterator ri = children.iterator();
		// 写跟贴
		while (ri.hasNext()) {
			i++;
			Leaf childlf = (Leaf) ri.next();
			boolean isLastChild1 = true;
			if (size != i)
				isLastChild1 = false;

			lp.setDirCode(childlf.getCode());

			if (childlf.getIsHome() && lp.canUserSee(userName)) {
				ShowLeafSimpleAjax(privilege, out, childlf, isLastChild,
						target, link, tableClass, tableClassMouseOn);
			}
		}
		if (size > 0)
			out.print("</td></tr></table>");
	}

	/**
	 * isLastChild 是否为其父亲结点的最后一个孩子结点
	 **/
	void ShowLeafSimpleAjax(Privilege privilege, JspWriter out, Leaf leaf,
			boolean isLastChild, String target, String link, String tableClass,
			String tableClassMouseOn) throws Exception {
		if (!leaf.isShow())
			return;
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();

		String folderImg = getFolderImg(leaf);

		int childcount = leaf.getChildCount();

		String tableid = leaf.getCode();

		out
				.println("<table id="
						+ tableid
						+ " name="
						+ tableid
						+ " class='"
						+ tableClass
						+ "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='"
						+ tableClassMouseOn
						+ "'\" onMouseOut=\"this.className='" + tableClass
						+ "'\" border=0>");
		out.println("    <tbody><tr>");
		out.println("        <td height='20' align=left nowrap>");

		int padWidth = 0;
		for (int k = 1; k <= layer - 1; k++) {
			padWidth += 21;
		}
		if (childcount == 0)
			padWidth += 16;
		out.print("<img src='" + request.getContextPath()
				+ "/images/spacer.gif' width=" + padWidth
				+ " height=1 style='visibility:hidden'>");

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out
					.println("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='"
							+ Global.getRootPath()
							+ "/fileark/images/i_puls-root.gif' align='absmiddle'><img src='"
							+ Global.getRootPath() + "/fileark/images/"
							+ folderImg + "' align='absmiddle'>");
		} else {
			if (childcount > 0)
				out
						.println("<img onClick=\"ShowChild(this, '"
								+ tableid
								+ "')\" src='"
								+ Global.getRootPath()
								+ "/fileark/images/i_plus.gif' align='absmiddle'><img src='"
								+ request.getContextPath()
								+ "/images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='"
								+ Global.getRootPath() + "/fileark/images/"
								+ folderImg + "' align='absmiddle'>");
			else
				out.println("<img src='" + Global.getRootPath()
						+ "/fileark/images/" + folderImg
						+ "' align='absmiddle'>");
		}

		// 三种类型节点，用同一个Link
		if (leaf.getType() == Leaf.TYPE_LINK) {
			if (!leaf.getTarget().equals(""))
				target = leaf.getTarget();
			out
					.print("<a target='" + target + "' href='"
							+ leaf.getDescription() + "' class='link'>" + name
							+ "</a>");
		} else {
			out.print("<a target='" + target + "' href='" + link
					+ StrUtil.UrlEncode(code) + "'>" + name + "</a>");
		}
		out.print("     </td>");
		out.println("  </tr></tbody></table>");

		/*
		 * LeafPriv lp = new LeafPriv(leaf.getCode()); if
		 * (!lp.canUserSeeWithAncestorNode(userName)) { if
		 * (!leaf.getCode().equals("root")) { out.println("<script>\n");
		 * out.println(tableid + ".style.display='none';\n");
		 * out.println("</script>\n"); } }
		 */
	}

	public void ShowLeafSimple(JspWriter out, Leaf leaf, boolean isLastChild,
			String target, String link, String tableClass,
			String tableClassMouseOn) throws Exception {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();
		String folderImg = getFolderImg(leaf);

		if (!isLastChild) {
			Leaf brotherleaf = leaf.getBrother("down");
			// 如果兄弟结点存在
			if (brotherleaf != null) {
				// 取其所有的孩子结点
				Vector r = new Vector();
				leaf.getAllChild(r, leaf);
				int count = r.size();
				if (count > 0) {
					UprightLineNode uln = new UprightLineNode(layer, count);
					UprightLineNodes.addElement(uln);
				}
			}
		}

		int childcount = leaf.getChildCount();

		String tableid = "table" + leaf.getCode();

		out
				.println("<table id="
						+ tableid
						+ " name="
						+ tableid
						+ " class='"
						+ tableClass
						+ "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='"
						+ tableClassMouseOn
						+ "'\" onMouseOut=\"this.className='" + tableClass
						+ "'\" border=0>");
		out.println("    <tbody><tr>");
		out.println("        <td height='13' align=left nowrap>");
		// for (int k = 1; k <= layer - 1; k++) {
		for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) {
			boolean isShowed = false;
			Iterator ir = UprightLineNodes.iterator();
			while (ir.hasNext()) {
				UprightLineNode node = (UprightLineNode) ir.next();
				// 如果在K层上存在一个竖线结点则画出
				if (node.getLayer() == k) {
					node.show(out, "images/i_plus-2.gif");
					if (node.getCount() == 0) {
						UprightLineNodes.remove(node);
					}
					isShowed = true;
					break;
				}
			}
			if (!isShowed)
				out.println("<img src='" + request.getContextPath()
						+ "/images/spacer.gif' width=20 height=1>");
		}

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out
					.println("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/"
							+ folderImg + "' align='absmiddle'>");
		} else {
			if (isLastChild) { // 是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/"
									+ folderImg + "' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/"
									+ folderImg + "' align='absmiddle'>");
			} else { // 不是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/"
									+ folderImg + "' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/"
									+ folderImg + "' align='absmiddle'>");
			}
		}

		// 三种类型节点，用同一个Link
		String linkstr = link;
		String targetstr = target;

		if (leaf.getType() == leaf.TYPE_LINK) {
			linkstr = leaf.getDescription();
			if (!leaf.getTarget().equals(""))
				targetstr = leaf.getTarget();
		} else
			linkstr += "?dir_code=" + StrUtil.UrlEncode(code);

		out.print("<a target='" + targetstr + "' href='" + linkstr + "'>"
				+ name + "</a>");

		out.print("     </td>");
		out.println("  </tr></tbody></table>");
	}

	/**
	 * 树的节点在显示时，使用超链接href='func(deptCode)'
	 * 
	 * @param out
	 *            JspWriter
	 * @param leaf
	 *            DeptDb
	 * @param isLastChild
	 *            boolean
	 * @param target
	 *            String
	 * @param func
	 *            String JS中的函数名称
	 * @param tableClass
	 *            String
	 * @param tableClassMouseOn
	 *            String
	 * @throws Exception
	 */
	public void ListTreeFunc(JspWriter out, Leaf leaf, boolean isLastChild,
			String target, String func, String tableClass,
			String tableClassMouseOn) throws Exception {
		LeafPriv lp = new LeafPriv(leaf.getCode());
		// 链接型节点不显示
		if (leaf.getIsHome() && leaf.getType() != leaf.TYPE_LINK) {
			if (lp.canUserSee(userName))
				ShowLeafFunc(out, leaf, isLastChild, target, func, tableClass,
						tableClassMouseOn);
		}
		Directory dir = new Directory();
		Vector children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return;

		int i = 0;
		if (size > 0)
			out
					.print("<table id='childoftable"
							+ leaf.getCode()
							+ "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
		Iterator ri = children.iterator();
		// 写跟贴
		while (ri.hasNext()) {
			i++;
			Leaf childlf = (Leaf) ri.next();
			boolean isLastChild1 = true;
			if (size != i)
				isLastChild1 = false;
			if (childlf.getIsHome() && childlf.getType() != leaf.TYPE_LINK) {
				if (lp.canUserSee(userName))
					ListTreeFunc(out, childlf, isLastChild1, target, func,
							tableClass, tableClassMouseOn);
			}
		}
		if (size > 0)
			out.print("</td></tr></table>");
	}

	public void ShowLeafFunc(JspWriter out, Leaf leaf, boolean isLastChild,
			String target, String func, String tableClass,
			String tableClassMouseOn) throws Exception {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();
		// String description = leaf.getDescription();

		String folderImg = getFolderImg(leaf);

		if (!isLastChild) {
			Leaf brotherleaf = leaf.getBrother("down");
			// 如果兄弟结点存在
			if (brotherleaf != null) {
				// 取其所有的孩子结点
				Vector r = new Vector();
				leaf.getAllChild(r, leaf);
				int count = r.size();
				if (count > 0) {
					UprightLineNode uln = new UprightLineNode(layer, count);
					// " count=" + count);
					UprightLineNodes.addElement(uln);
				}
			}
		}

		int childcount = leaf.getChildCount();

		String tableid = "table" + leaf.getCode();

		out
				.println("<table id="
						+ tableid
						+ " name="
						+ tableid
						+ " class='"
						+ tableClass
						+ "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='"
						+ tableClassMouseOn
						+ "'\" onMouseOut=\"this.className='" + tableClass
						+ "'\" border=0>");
		out.println("    <tbody><tr>");
		out.println("        <td height='13' align=left nowrap>");
		// for (int k = 1; k <= layer - 1; k++) {
		for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) {
			boolean isShowed = false;
			Iterator ir = UprightLineNodes.iterator();
			while (ir.hasNext()) {
				UprightLineNode node = (UprightLineNode) ir.next();
				// 如果在K层上存在一个竖线结点则画出
				if (node.getLayer() == k) {
					node.show(out, "images/i_plus-2.gif");
					if (node.getCount() == 0) {
						UprightLineNodes.remove(node);
					}
					isShowed = true;
					break;
				}
			}
			if (!isShowed)
				out
						.println("<img src='"
								+ request.getContextPath()
								+ "/images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
		}

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out
					.println("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='"
							+ Global.getRootPath() + "/fileark/images/"
							+ folderImg + "' align='absmiddle'>");
		} else {
			if (isLastChild) { // 是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='"
									+ Global.getRootPath() + "/fileark/images/"
									+ folderImg + "' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='"
									+ Global.getRootPath()
									+ "/fileark/images/"
									+ folderImg + "' align='absmiddle'>");
			} else { // 不是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='"
									+ Global.getRootPath() + "/fileark/images/"
									+ folderImg + "' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='"
									+ Global.getRootPath()
									+ "/fileark/images/"
									+ folderImg + "' align='absmiddle'>");
			}
		}

		// 三种类型节点，用同一个Link
		out.print("<a target='" + target + "' href='#' onClick=\"" + func
				+ "('" + code + "', '" + name + "')\">" + name + "</a>");

		out.print("     </td>");
		out.println("  </tr></tbody></table>");
	}

	/**
	 * 选择某个节点
	 * 
	 * @param out
	 *            JspWriter
	 * @param tableClass
	 *            String
	 * @param tableClassMouseOn
	 *            String
	 * @param isShowRoot
	 *            boolean
	 * @throws Exception
	 */
	public void SelectSingleAjax(JspWriter out, String func, String tableClass,
			String tableClassMouseOn, boolean isShowRoot) throws Exception {
		Privilege privilege = new Privilege();
		SelectTreeSingleAjax(privilege, out, rootLeaf, func, tableClass,
				tableClassMouseOn, isShowRoot);
	}

	void SelectTreeSingleAjax(Privilege privilege, JspWriter out, Leaf leaf,
			String func, String tableClass, String tableClassMouseOn,
			boolean isShowRoot) throws Exception {
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
			out
					.print("<table id='childof"
							+ leaf.getCode()
							+ "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
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
			String func, String tableClass, String tableClassMouseOn)
			throws Exception {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();

		int childcount = leaf.getChildCount();

		String tableid = leaf.getCode();

		out
				.println("<table id="
						+ tableid
						+ " name="
						+ tableid
						+ " class='"
						+ tableClass
						+ "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='"
						+ tableClassMouseOn
						+ "'\" onMouseOut=\"this.className='" + tableClass
						+ "'\" border=0>");
		out.println("    <tbody><tr>");
		out.println("        <td height='20' align=left nowrap>");

		int padWidth = 0;
		for (int k = 1; k <= layer - 1; k++) {
			padWidth += 21;
		}
		if (childcount == 0)
			padWidth += 16;
		out.print("<img src='" + request.getContextPath()
				+ "/images/spacer.gif' width=" + padWidth
				+ " height=1 style='visibility:hidden'>");

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out
					.println("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
		} else {
			if (childcount > 0)
				out
						.println("<img onClick=\"ShowChild(this, '"
								+ tableid
								+ "')\" src='images/i_plus.gif' align='absmiddle'><img src='"
								+ request.getContextPath()
								+ "/images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='images/folder_01.gif' align='absmiddle'>");
			else
				out
						.println("<img src='images/folder_01.gif' align='absmiddle'>");
		}

		// 三种类型节点，用同一个Link
		out.print("<a href=\"javascript:" + func + "('" + code + "','" + name
				+ "')\">" + name + "</a>");

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

	public void SelectSingleAjaxHtml(StringBuffer sb, String func, String tableClass,
								 String tableClassMouseOn, boolean isShowRoot) throws ErrMsgException {
		Privilege privilege = new Privilege();
		SelectTreeSingleAjaxHtml(privilege, sb, rootLeaf, func, tableClass, tableClassMouseOn, isShowRoot);
	}

	void SelectTreeSingleAjaxHtml(Privilege privilege, StringBuffer sb, Leaf leaf,
							  String func, String tableClass, String tableClassMouseOn,
							  boolean isShowRoot) throws ErrMsgException {
		if (isShowRoot) {
			ShowLeafSingleAjaxHtml(privilege, sb, leaf, func, tableClass, tableClassMouseOn);
		}

		Directory dir = new Directory();
		Vector children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
			return;
		}

		int i = 0;
		if (size > 0) {
			sb.append("<table id='childof"
							+ leaf.getCode()
							+ "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
		}
		Iterator ri = children.iterator();
		// 写跟贴
		while (ri.hasNext()) {
			i++;
			Leaf childlf = (Leaf) ri.next();
			ShowLeafSingleAjaxHtml(privilege, sb, childlf, func, tableClass, tableClassMouseOn);
		}
		if (size > 0) {
			sb.append("</td></tr></table>");
		}
	}

	/**
	 * isLastChild 是否为其父亲结点的最后一个孩子结点
	 **/
	void ShowLeafSingleAjaxHtml(Privilege privilege, StringBuffer sb, Leaf leaf,
							String func, String tableClass, String tableClassMouseOn) {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();

		int childcount = leaf.getChildCount();

		String tableid = leaf.getCode();

		sb.append("<table id="
						+ tableid
						+ " name="
						+ tableid
						+ " class='"
						+ tableClass
						+ "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='"
						+ tableClassMouseOn
						+ "'\" onMouseOut=\"this.className='" + tableClass
						+ "'\" border=0>");
		sb.append("    <tbody><tr>");
		sb.append("        <td height='20' align=left nowrap>");

		int padWidth = 0;
		for (int k = 1; k <= layer - 1; k++) {
			padWidth += 21;
		}
		if (childcount == 0) {
			padWidth += 16;
		}
		sb.append("<img src='" + request.getContextPath()
				+ "/images/spacer.gif' width=" + padWidth
				+ " height=1 style='visibility:hidden'>");

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			sb.append("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
		} else {
			if (childcount > 0) {
				sb.append("<img onClick=\"ShowChild(this, '"
								+ tableid
								+ "')\" src='images/i_plus.gif' align='absmiddle'><img src='"
								+ request.getContextPath()
								+ "/images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='images/folder_01.gif' align='absmiddle'>");
			} else {
				sb.append("<img src='images/folder_01.gif' align='absmiddle'>");
			}
		}

		// 三种类型节点，用同一个Link
		sb.append("<a href=\"javascript:" + func + "('" + code + "','" + name
				+ "')\">" + name + "</a>");

		sb.append("     </td>");
		sb.append("  </tr></tbody></table>");

		LeafPriv lp = new LeafPriv(leaf.getCode());
		if (!lp.canUserSeeWithAncestorNode(privilege.getUser(request))) {
			if (!leaf.getCode().equals("root")) {
				sb.append("<script>\n");
				sb.append("tableid.style.display='none';\n");
				sb.append("</script>\n");
			}
		}
	}

	public String getJsonString(Leaf leaf) throws Exception {
		Directory dir = new Directory();
		String str = "[{id:\"" + leaf.getCode() + "\",parent:\"#\",text:\""
				+ leaf.getName() + "\",state:{opened:true}} ,";
		// 从根开始
		str = this.getJson(dir, leaf.getCode(), str);
		str = str.substring(0, str.length() - 1);
		str += "]";
		return str;
	}

	public String getJsonStringByUser(Leaf leaf, String userName) {
		Directory dir = new Directory();
		String str = "[{id:\"" + leaf.getCode() + "\",parent:\"#\",text:\""
				+ leaf.getName().replaceAll("\"", "\\\\\"") + "\",state:{opened:true}} ,";

		// 从根开始
		ArrayList<String> list = new ArrayList<String>();
		getJsonByUser(dir, leaf.getCode(), userName, list);
		for (String node : list) {
			if (node.equals(Leaf.ROOTCODE)) {
				continue;
			}
			if (node.equals(leaf.getCode())) {
				continue;
			}
			Leaf childlf = new Leaf(node);
			str += "{id:\"" + childlf.getCode() + "\",parent:\""
					+ childlf.getParentCode() + "\",text:\""
					+ childlf.getName().replaceAll("\"", "\\\\\"") + "\" },";
		}
		str = str.substring(0, str.length() - 1);
		str += ",{id:\"" + Leaf.CODE_DRAFT + "\",parent:\"" + Leaf.ROOTCODE + "\",text:\"草稿箱\", icon: \"images/draft.png\"} ";
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
	private String getJson(Directory dir, String parentCode, String str)
			throws Exception {
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			i++;
			Leaf childlf = (Leaf) ri.next();
			if (!childlf.isShow()) {
				continue;
			}
			str += "{id:\"" + childlf.getCode() + "\",parent:\""
					+ childlf.getParentCode() + "\",text:\""
					+ childlf.getName().replaceAll("\"", "\\\\\"") + "\" },";
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					Leaf child = (Leaf) childri.next();
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName().replaceAll("\"", "\\\\\"") + "\" },";
					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = this.getJson(dir, child.getCode(), str);
					}
				}
			}
		}
		return str;
	}

	public void getJsonByUser(Directory dir, String parentCode, String userName, ArrayList<String> list) {
		Leaf lf = new Leaf();
		lf = lf.getLeaf(parentCode);
		// DebugUtil.i(getClass(), "getJsonByUser", lf.getName());

		boolean canSeeParent = false;
		LeafPriv lp = new LeafPriv(parentCode);
		if (!parentCode.equals(Leaf.ROOTCODE)) {
			if (lp.canUserSeeWithAncestorNode(userName)) {
				canSeeParent = true;
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
		}
		
		// 把顶层的查出来
		List<String> defaultSeeChildren= new ArrayList<String>();
		Vector<Leaf> children = null;
		try {
			children = dir.getChildren(parentCode);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
		}

		for (Leaf childlf : children) {
			lp = new LeafPriv(childlf.getCode());
			if (!childlf.isShow()) {
				if (!lp.canUserExamine(userName)) {
					continue;
				}
			}

			/**
			 * --a
			 * -----a1
			 * -----a2
			 * 如果默认可见a1，则暂不加入a1，只有当父节点可见时，才加入a1，如当a2显式赋予了权限可见时，才加入a1
			 */
			if (lp.canUserSeeWithAncestorNode(userName)) {
				// 如果能看到父节点，则直接加入
				if (canSeeParent) {
					if (!list.contains(childlf.getCode())) {
						list.add(childlf.getCode());
					}
				} else {
					// 如果不是默认可见（节点未赋予任何权限），则将子节点、父节点及之前已遍历的默认可见兄弟节点加入（如果本节点及父节点未曾加入的话）
					if (!lp.isDefaultSee()) {
						// 如果没有父节点的可见权限，则加入父节点
						if (!canSeeParent) {
							if (!list.contains(parentCode)) {
								list.add(parentCode);
							}
							canSeeParent = true;
							// 如果之前存在默认可见的兄弟节点，则加入
							if (defaultSeeChildren.size() > 0) {
								list.addAll(defaultSeeChildren);
								// 清空
								defaultSeeChildren.clear();
							}
						}
						// 加入子节点
						if (!list.contains(childlf.getCode())) {
							list.add(childlf.getCode());
						}
					} else {
						defaultSeeChildren.add(childlf.getCode());
					}
				}
			}

			if (childlf.getChildCount() > 0) {
				getJsonByUser(dir, childlf.getCode(), userName, list);
			}
		}
		// 如果父节点可见，则加入默认可见的子节点
		/*if (canSeeParent) {
			if (defaultSeeChildren.size()>0) {
				list.addAll(defaultSeeChildren);
			}
		}*/
	}
}
