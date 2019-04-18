package com.redmoon.oa.basic;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;

import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;

import com.redmoon.oa.*;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.fileark.*;
import com.redmoon.oa.pvg.Privilege;

import org.apache.log4j.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TreeSelectView {
	Logger logger = Logger.getLogger(TreeSelectView.class.getName());
	TreeSelectDb rootLeaf;
	Vector UprightLineNodes = new Vector(); // 用于显示竖线

	public TreeSelectView(TreeSelectDb rootLeaf) {
		this.rootLeaf = rootLeaf;
	}

	public void list(JspWriter out) throws Exception {
		ListTree(out, rootLeaf, true);
	}

	// 显示根结点为leaf的树
	void ListTree(JspWriter out, TreeSelectDb leaf, boolean isLastChild)
			throws Exception {
		ShowLeaf(out, leaf, isLastChild);
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector children = dm.getChildren(leaf.getCode());
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
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
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
	void ShowLeaf(JspWriter out, TreeSelectDb leaf, boolean isLastChild)
			throws Exception {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();
		String description = leaf.getDescription();

		if (!isLastChild) {
			TreeSelectDb brotherleaf = leaf.getBrother("down");
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
					// " count=" + count);
					UprightLineNodes.addElement(uln);
				}
			}
		}

		int childcount = leaf.getChildCount();
		// System.out.println(code + " childcount=" + childcount);

		String tableid = "table" + leaf.getCode();

		out
				.println("<table id="
						+ tableid
						+ " name="
						+ tableid
						+ " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" border=0>");
		out.println("    <tbody><tr>");
		out.println("        <td width='85%' height='13' align=left nowrap>");
		// for (int k = 1; k <= layer - 1; k++) {
		for (int k = rootLeaf.getLayer(); k <= layer - 1; k++) { // 不用上一行，是因为上一行会产生多余的空格
			boolean isShowed = false;
			Iterator ir = UprightLineNodes.iterator();
			while (ir.hasNext()) {
				UprightLineNode node = (UprightLineNode) ir.next();
				// 如果在K层上存在一个竖线结点则画出
				if (node.getLayer() == k) {
					node.show(out, "images/i_plus-2.gif");
					if (node.getCount() == 0) {
						UprightLineNodes.remove(node);
						// System.out.println("Remove " + node);
					}
					isShowed = true;
					break;
				}
			}
			if (!isShowed)
				out
						.println("<img src='' width=20 height=1 style='visibility:hidden'>");
		}

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out
					.println("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
		} else {
			if (isLastChild) { // 是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			} else { // 不是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			}
		}

		if (!leaf.getColor().equals(""))
			out.print("<font color='" + leaf.getColor() + "'>");
		out.print(name);
		if (!leaf.getColor().equals(""))
			out.print("</font>");

		out.print("     </td><td width='15%' align=right nowrap>");
		if (!leaf.getParentCode().equals("-1")) {
			out
					.print("<a target=dirbottomFrame href='basic_tree_select_bottom.jsp?parent_code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "&parent_name="
							+ StrUtil.UrlEncode(name, "utf-8")
							+ "&op=AddChild'>添子项</a>&nbsp;");
			out
					.print("<a target='dirbottomFrame' href='basic_tree_select_bottom.jsp?op=modify&code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "&name="
							+ StrUtil.UrlEncode(name, "utf-8")
							+ "&description="
							+ StrUtil.UrlEncode(description, "utf-8")
							+ "'>修改</a>&nbsp;");
			out
					.print("<a target=_self href=# onClick=\"if (window.confirm('您确定要删除"
							+ name
							+ "吗?')) window.location.href='basic_tree_select_top.jsp?op=del&rootCode="
							+ StrUtil.UrlEncode(rootLeaf.getCode())
							+ "&delcode="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "'\">删除</a>&nbsp;");
			out.print("<a href='basic_tree_select_top.jsp?op=move&rootCode="
					+ StrUtil.UrlEncode(leaf.getRootCode())
					+ "&direction=up&code=" + StrUtil.UrlEncode(code, "utf-8")
					+ "'>上移</a>&nbsp;");
			out.print("<a href='basic_tree_select_top.jsp?op=move&rootCode="
					+ StrUtil.UrlEncode(leaf.getRootCode())
					+ "&direction=down&code="
					+ StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;");
		}
		out.println("  </td></tr></tbody></table>");
	}

	void ShowTreeSelectAsOption(JspWriter out, TreeSelectDb leaf, int rootlayer)
			throws Exception {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();
		String blank = "";
		int d = layer - rootlayer;
		for (int i = 0; i < d; i++) {
			blank += "　";
		}
		if (leaf.getChildCount() > 0) {
			out.print("<option value='" + code + "'>" + blank + "╋ " + name
					+ "</option>");
		} else {
			out.print("<option value=\"" + code + "\">" + blank + "├『" + name
					+ "』</option>");
		}
	}

	public String getTreeSelectAsOption(TreeSelectDb leaf, int rootlayer) {
		String outStr = "";
		String code = leaf.getCode();
		String name = leaf.getName();

		String clr = "";
		if (!leaf.getColor().equals(""))
			clr = " style='color:" + leaf.getColor() + "' ";

		int layer = leaf.getLayer();
		String blank = "";
		int d = layer - rootlayer;
		for (int i = 0; i < d; i++) {
			blank += "　";
		}
		if (leaf.getChildCount() > 0) {
			outStr += "<option value='" + code + "'" + clr + ">" + blank + "╋ "
					+ name + "</option>";
		} else {
			outStr += "<option value=\"" + code + "\"" + clr + ">" + blank
					+ "├『" + name + "』</option>";
		}
		return outStr;
	}

	public String getTreeSelectNameAsOptionValue(TreeSelectDb leaf,
			int rootlayer) {
		String outStr = "";
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();
		String blank = "";
		int d = layer - rootlayer;
		for (int i = 0; i < d; i++) {
			blank += "　";
		}
		if (leaf.getChildCount() > 0) {
			outStr += "<option value='" + code + "'>" + blank + "╋ " + name
					+ "</option>";
		} else {
			outStr += "<option value=\"" + code + "\">" + blank + "├『" + name
					+ "』</option>";
		}
		return outStr;
	}

	// 显示根结点为leaf的树
	public void ShowTreeSelectAsOptions(JspWriter out, TreeSelectDb leaf,
			int rootlayer) throws Exception {
		ShowTreeSelectAsOption(out, leaf, rootlayer);
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return;

		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			ShowTreeSelectAsOptions(out, childlf, rootlayer);
		}
	}

	public StringBuffer getTreeSelectAsOptions(StringBuffer outStr,
			TreeSelectDb leaf, int rootlayer) throws ErrMsgException {
		outStr.append(getTreeSelectAsOption(leaf, rootlayer));
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return outStr;

		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			getTreeSelectAsOptions(outStr, childlf, rootlayer);
		}
		return outStr;
	}

	public StringBuffer getTreeSelectNameAsOptions(StringBuffer outStr,
			TreeSelectDb leaf, int rootlayer) throws ErrMsgException {
		outStr.append(getTreeSelectNameAsOptionValue(leaf, rootlayer));
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return outStr;

		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			getTreeSelectNameAsOptions(outStr, childlf, rootlayer);
		}
		return outStr;
	}

	public JSONObject getTreeSelectNameAsOptionValueForNest(TreeSelectDb leaf,
			int rootlayer) throws JSONException {
		JSONObject select = new JSONObject();
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();
		String blank = "";
		int d = layer - rootlayer;
		for (int i = 0; i < d; i++) {
			blank += "　";
		}
		select.put("value", code);
		if (leaf.getChildCount() > 0) {
			select.put("name", blank + "╋ " + name);
		} else {
			select.put("name", blank + "├『" + name + "』");
		}
		return select;
	}

	public JSONArray getTreeSelectNameAsOptionsForNest(JSONArray selects,
			TreeSelectDb leaf, int rootlayer) throws ErrMsgException,
			JSONException {
		selects.put(getTreeSelectNameAsOptionValueForNest(leaf, rootlayer));
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return selects;

		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			getTreeSelectNameAsOptionsForNest(selects, childlf, rootlayer);
		}
		return selects;
	}

	/**
	 * 树的节点在显示时，使用超链接href='func(deptCode)'
	 * 
	 * @param out
	 *            JspWriter
	 * @param leaf
	 *            TreeSelectDb
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
	void ListTreeFunc(String[] depts, JspWriter out, TreeSelectDb leaf,
			boolean isLastChild, String target, String func, String tableClass,
			String tableClassMouseOn) throws Exception {
		ShowLeafFunc(depts, out, leaf, isLastChild, target, func, tableClass,
				tableClassMouseOn);
		TreeSelectMgr dir = new TreeSelectMgr();
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
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			boolean isLastChild1 = true;
			if (size != i)
				isLastChild1 = false;
			ListTreeFunc(depts, out, childlf, isLastChild1, target, func,
					tableClass, tableClassMouseOn);
		}
		if (size > 0)
			out.print("</td></tr></table>");
	}

	void ShowLeafFunc(String[] depts, JspWriter out, TreeSelectDb leaf,
			boolean isLastChild, String target, String func, String tableClass,
			String tableClassMouseOn) throws Exception {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();
		// String description = leaf.getDescription();

		if (!isLastChild) {
			TreeSelectDb brotherleaf = leaf.getBrother("down");
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
					// " count=" + count);
					UprightLineNodes.addElement(uln);
				}
			}
		}

		int childcount = leaf.getChildCount();
		// System.out.println(code + " childcount=" + childcount);

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
						// System.out.println("Remove " + node);
					}
					isShowed = true;
					break;
				}
			}
			if (!isShowed)
				out.println("<img src='' width=20 height=1>");
		}

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out
					.println("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
		} else {
			if (isLastChild) { // 是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			} else { // 不是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			}
		}

		Config cfg = new Config();
		boolean isRestricted = true;
		if (cfg.get("restrictUserTreeSelect").equals("true")) {
			if (depts != null) {
				int len = depts.length;
				for (int i = 0; i < len; i++) {
					if (depts[i].equals(code)) {
						isRestricted = false;
						break;
					}
				}
			} else
				isRestricted = false;
		} else
			isRestricted = false;
		if (isRestricted) {
			out.print("<font color='#888888'>" + name + "</font>");
		} else
			// 三种类型节点，用同一个Link
			out.print("<a target='" + target + "' href='#' onClick=\"" + func
					+ "('" + code + "')\">" + name + "</a>");

		out.print("     </td>");
		out.println("  </tr></tbody></table>");
	}

	public void ListFuncWithCheckbox(JspWriter out, String target, String func,
			String tableClass, String tableClassMouseOn) throws Exception {
		ListTreeFuncWithCheckbox(out, rootLeaf, true, target, func, tableClass,
				tableClassMouseOn);
	}

	/**
	 * 树的节点在显示时，使用超链接href='func(deptCode)'
	 * 
	 * @param out
	 *            JspWriter
	 * @param leaf
	 *            TreeSelectDb
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
	void ListTreeFuncWithCheckbox(JspWriter out, TreeSelectDb leaf,
			boolean isLastChild, String target, String func, String tableClass,
			String tableClassMouseOn) throws Exception {
		ShowLeafFuncWithCheckbox(out, leaf, isLastChild, target, func,
				tableClass, tableClassMouseOn);
		TreeSelectMgr dir = new TreeSelectMgr();
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
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			boolean isLastChild1 = true;
			if (size != i)
				isLastChild1 = false;
			ListTreeFuncWithCheckbox(out, childlf, isLastChild1, target, func,
					tableClass, tableClassMouseOn);
		}
		if (size > 0)
			out.print("</td></tr></table>");
	}

	void ShowLeafFuncWithCheckbox(JspWriter out, TreeSelectDb leaf,
			boolean isLastChild, String target, String func, String tableClass,
			String tableClassMouseOn) throws Exception {
		String code = leaf.getCode();
		String name = leaf.getName();
		int layer = leaf.getLayer();

		if (!isLastChild) {
			TreeSelectDb brotherleaf = leaf.getBrother("down");
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
					// " count=" + count);
					UprightLineNodes.addElement(uln);
				}
			}
		}

		int childcount = leaf.getChildCount();
		// System.out.println(code + " childcount=" + childcount);

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
						// System.out.println("Remove " + node);
					}
					isShowed = true;
					break;
				}
			}
			if (!isShowed)
				out.println("<img src='' width=20 height=1>");
		}

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out
					.println("<img onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
		} else {
			if (isLastChild) { // 是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			} else { // 不是最后一个孩子结点
				if (childcount > 0)
					out
							.println("<img onClick=\"ShowChild(this, '"
									+ tableid
									+ "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
				else
					out
							.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			}
		}

		// checkbox
		if (!code.equals(leaf.getRootCode()))
			out.print("<input type='checkbox' name='" + code + "' value='"
					+ name + "'>&nbsp;");
		// 三种类型节点，用同一个Link
		out.print("<a target='" + target + "' href='#' onClick=\"" + func
				+ "('" + code + "')\">" + name + "</a>");

		out.print("     </td>");
		out.println("  </tr></tbody></table>");
	}

	public String getJsonString() throws Exception {
		TreeSelectMgr dir = new TreeSelectMgr();
		String str = "[";
		// 从根开始
		str = this.getJson(dir, "-1", str);
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
	private String getJson(TreeSelectMgr dir, String parentCode, String str)
			throws Exception {
	
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
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
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					TreeSelectDb child = (TreeSelectDb) childri.next();
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

	public List<String> getAllUnit() throws Exception {
		List<String> list = new ArrayList<String>();
		TreeSelectMgr dir = new TreeSelectMgr();
		return this.getEachUnit(dir, "-1", list);
	}

	private List<String> getEachUnit(TreeSelectMgr dir, String parentCode,
			List<String> list) throws Exception {
		Vector children = dir.getChildren(parentCode);
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			if (childlf.getType() == 0) {
				list.add(childlf.getCode());
			}
			Vector childs = dir.getChildren(childlf.getCode());
			if (!childs.isEmpty()) {
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					TreeSelectDb child = (TreeSelectDb) childri.next();
					if (child.getType() == 0) {
						list.add(child.getCode());
					}
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						this.getEachUnit(dir, child.getCode(), list);
					}
				}
			}
		}
		return list;
	}

	public void listAjax(HttpServletRequest request, JspWriter out,
			boolean isShowRoot) throws Exception {
		Privilege privilege = new Privilege();
		ListTreeAjax(request, privilege, out, rootLeaf, true, isShowRoot);
	}

	// 显示根结点为leaf的树
	void ListTreeAjax(HttpServletRequest request, Privilege privilege,
			JspWriter out, TreeSelectDb leaf, boolean isLastChild,
			boolean isShowRoot) throws Exception {
		if (isShowRoot)
			ShowLeafAjax(request, privilege, out, leaf, isLastChild, isShowRoot);

		TreeSelectMgr dir = new TreeSelectMgr();
		Vector children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return;

		int i = 0;
		if (size > 0) {
			String style = "";
			if (!leaf.getCode().equals("root")) {
				style = "style='display:'";// 设置display为none将不会显示其子节点
			}
			out
					.print("<table id='childof"
							+ leaf.getCode()
							+ "' "
							+ style
							+ " cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
		}
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			i++;
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			boolean isLastChild1 = true;
			if (size != i)
				isLastChild1 = false;
			ShowLeafAjax(request, privilege, out, childlf, isLastChild1,
					isShowRoot);
		}
		if (size > 0)
			out.print("</td></tr></table>");
	}

	/**
	 * isLastChild 是否为其父亲结点的最后一个孩子结点
	 **/
	void ShowLeafAjax(HttpServletRequest request, Privilege privilege,
			JspWriter out, TreeSelectDb leaf, boolean isLastChild,
			boolean isShowRoot) throws Exception {
		String code = leaf.getCode();

		String name = leaf.getName();
		int layer = leaf.getLayer();
		String description = leaf.getDescription();

		int childcount = leaf.getChildCount();
		// System.out.println(code + " childcount=" + childcount);

		String tableid = leaf.getCode();

		String style = "";
		/*
		 * if (!leaf.getCode().equals("root")) { style = "style='display:none'";
		 * }
		 */
		out
				.println("<table id="
						+ tableid
						+ " name="
						+ tableid
						+ " "
						+ style
						+ " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" border=0>");
		out.println("    <tbody><tr>");
		out
				.println("        <td height='13' align=left onmouseover='showModify(this)' onmouseout='hiddenModify(this)' nowrap>");

		out.println("<span>");
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
					.println("<img style='cursor:pointer' tableRelate='"
							+ tableid
							+ "' onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
		} else {
			if (childcount > 0)
				out
						.println("<img style='cursor:pointer;margin-right:3px' tableRelate='"
								+ tableid
								+ "' onClick=\"ShowChild(this, '"
								+ leaf.getCode()
								+ "')\" src='images/i_plus.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			else
				out
						.println("<img src='images/folder_01.gif' align='absmiddle'>");
		}

		String className = "";
		if (leaf.getType() == DeptDb.TYPE_UNIT)
			className = "unit";

		String spanClsName = "";

		// if (!leaf.isShow()) {
		// spanClsName = "deptNodeHidden";
		// }
		out.print("<a class='" + className
				+ "' target=_parent href='dept_user.jsp?deptCode="
				+ StrUtil.UrlEncode(code) + "'><span class='" + spanClsName
				+ "'>" + name + "</span></a>");

		out.print("</span>");
		out.print("<span style='padding-left:20px;display:none'>");

		if (childcount > 0)
			out.print("<a href=\"javascript:ShowChild(o('" + tableid
					+ "').getElementsByTagName('img')[1], '" + leaf.getCode()
					+ "')\">伸缩</a>&nbsp;");

		if (!leaf.getCode().equals("root")) {
			String root_code = (String) request.getAttribute("root_code");
			if (root_code == null)
				root_code = rootLeaf.getCode();

			if (leaf.getIsHome())
				out.print(SkinUtil.LoadString(request, "res.cms.DirectoryView",
						"info_home")
						+ "&nbsp;");
			out.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			out
					.print("<a target='dirbottomFrame' href='officeequip_bottom.jsp?parent_code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "&parent_name="
							+ StrUtil.UrlEncode(name, "utf-8")
							+ "&op=AddChild'>添加子项</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out
					.print("<a target='dirbottomFrame' href='dept_bottom.jsp?op=modify&code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "&name="
							+ StrUtil.UrlEncode(name, "utf-8")
							+ "&description="
							+ StrUtil.UrlEncode(description, "utf-8")
							+ "'>修改</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out
					.print("<a target='dirhidFrame' onClick=\"return window.confirm('您确定要删除"
							+ name
							+ "吗?')\" href=\"dept_do.jsp?op=del&root_code="
							+ StrUtil.UrlEncode(rootLeaf.getCode())
							+ "&delcode="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "\">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out
					.print("<a target='dirhidFrame' href='dept_do.jsp?op=move&direction=up&code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "'>上移</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out
					.print("<a target='dirhidFrame' href='dept_do.jsp?op=move&direction=down&code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "'>下移</a>&nbsp;&nbsp;&nbsp;&nbsp;");

		}
		out.print("</span>");

		out.print("  </td></tr></tbody></table>");
	}
	
	public void getBootstrapJson(HttpServletRequest request, TreeSelectDb tsd, JSONObject json) {
		try {
			// JSONObject json = new JSONObject();
			json.put("text", tsd.getName());
			json.put("preCode", tsd.getPreCode());
			json.put("code", tsd.getCode());
			// System.out.println(getClass() + " " + tsd.getCode() + "--" + tsd.getName() + " " + tsd.getPreCode());
			json.put("href", tsd.getLink(request));
			json.put("formCode", tsd.getFormCode());
			
			String rootLink = rootLeaf.getLink(request);
			
			JSONArray nodes = new JSONArray();
			Iterator ir = tsd.getChildren().iterator();
			while (ir.hasNext()) {
				TreeSelectDb child = (TreeSelectDb)ir.next();
				JSONObject jo = new JSONObject();
				jo.put("text", child.getName());
				jo.put("code", child.getCode());
				jo.put("preCode", child.getPreCode());
				
				String link = child.getLink(request);
				// 如果未设链接，则按照根节点的链接
				if ("".equals(link)){
					link = rootLink;
				}
				
				jo.put("href", link);
				jo.put("formCode", child.getFormCode());
				
				nodes.put(jo);
				
				if (child.getChildren().size()>0) {
					getBootstrapJson(request, child, jo);
				}
			}
			
			if (nodes.length()>0) {
				json.put("nodes", nodes);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
