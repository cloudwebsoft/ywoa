package com.redmoon.oa.basic;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;

import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;

import com.cloudweb.oa.permission.ModuleTreePermission;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.*;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.flow.DirectoryView;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.LeafChildrenCacheMgr;
import com.redmoon.oa.pvg.Privilege;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TreeSelectView {
	TreeSelectDb rootLeaf;

	public TreeSelectView(TreeSelectDb rootLeaf) {
		this.rootLeaf = rootLeaf;
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
			out.print("<option value='" + code + "'>" + blank + "╋ " + name + "</option>");
		} else {
			out.print("<option value=\"" + code + "\">" + blank + "├ " + name + "</option>");
		}
	}

	public String getTreeSelectAsOption(TreeSelectDb leaf, int rootlayer) {
		String outStr = "";
		String code = leaf.getCode();
		String name = leaf.getName();

		String clr = "";
		if (!"".equals(leaf.getColor())) {
			clr = " style='color:" + leaf.getColor() + "' ";
		}

		int layer = leaf.getLayer();
		StringBuilder blank = new StringBuilder();
		int d = layer - rootlayer;
		for (int i = 0; i < d; i++) {
			blank.append("　");
		}
		if (leaf.getChildCount() > 0) {
			outStr += "<option value='" + code + "'" + clr + ">" + blank + "╋ " + name + "</option>";
		} else {
			outStr += "<option value=\"" + code + "\"" + clr + ">" + blank + "├ " + name + "</option>";
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
			outStr += "<option value='" + code + "'>" + blank + "╋ " + name + "</option>";
		} else {
			outStr += "<option value=\"" + code + "\">" + blank + "├ " + name + "</option>";
		}
		return outStr;
	}

	// 显示根结点为leaf的树
	public void ShowTreeSelectAsOptions(JspWriter out, TreeSelectDb leaf,
			int rootlayer) throws Exception {
		ShowTreeSelectAsOption(out, leaf, rootlayer);
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector<TreeSelectDb> children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
			return;
		}

		for (TreeSelectDb childlf : children) {
			ShowTreeSelectAsOptions(out, childlf, rootlayer);
		}
	}

	public StringBuffer getTreeSelectAsOptions(StringBuffer outStr,
			TreeSelectDb leaf, int rootlayer) throws ErrMsgException {
		outStr.append(getTreeSelectAsOption(leaf, rootlayer));
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector<TreeSelectDb> children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
			return outStr;
		}

		for (TreeSelectDb childlf : children) {
			if (childlf.isOpen()) {
				getTreeSelectAsOptions(outStr, childlf, rootlayer);
			}
		}
		return outStr;
	}

	public String getTreeSelectAsOptionsFirstLayer(TreeSelectDb leaf, int rootlayer) throws ErrMsgException {
		StringBuilder outStr = new StringBuilder();
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector<TreeSelectDb> children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
			return outStr.toString();
		}
		for (TreeSelectDb childlf : children) {
			if (childlf.isOpen()) {
				outStr.append(getTreeSelectAsOption(childlf, rootlayer));
			}
		}
		return outStr.toString();
	}

	public StringBuffer getTreeSelectNameAsOptions(StringBuffer outStr,
			TreeSelectDb leaf, int rootlayer) throws ErrMsgException {
		outStr.append(getTreeSelectNameAsOptionValue(leaf, rootlayer));
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector<TreeSelectDb> children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
			return outStr;
		}

		for (TreeSelectDb childlf : children) {
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
			select.put("name", blank + "├ " + name);
		}
		return select;
	}

	public JSONArray getTreeSelectNameAsOptionsForNest(JSONArray selects,
			TreeSelectDb leaf, int rootlayer) throws ErrMsgException,
			JSONException {
		selects.put(getTreeSelectNameAsOptionValueForNest(leaf, rootlayer));
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector<TreeSelectDb> children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
			return selects;
		}

		for (TreeSelectDb childlf : children) {
			getTreeSelectNameAsOptionsForNest(selects, childlf, rootlayer);
		}
		return selects;
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
		Vector<TreeSelectDb> children = dir.getChildren(parentCode);
		int size = children.size();
		for (TreeSelectDb childlf : children) {
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
			Vector<TreeSelectDb> childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				for (TreeSelectDb child : childs) {
					j++;
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName().replaceAll("\"", "\\\\\"") + "\" },";
					// 还有子节点(递归调用)
					Vector<TreeSelectDb> ch = dir.getChildren(child.getCode());
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
		Vector<TreeSelectDb> children = dir.getChildren(parentCode);
		for (TreeSelectDb childlf : children) {
			if (childlf.getType() == 0) {
				list.add(childlf.getCode());
			}
			Vector<TreeSelectDb> childs = dir.getChildren(childlf.getCode());
			if (!childs.isEmpty()) {
				for (TreeSelectDb child : childs) {
					if (child.getType() == 0) {
						list.add(child.getCode());
					}
					Vector<TreeSelectDb> ch = dir.getChildren(child.getCode());
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
		if (isShowRoot) {
			ShowLeafAjax(request, privilege, out, leaf, isLastChild, isShowRoot);
		}

		TreeSelectMgr dir = new TreeSelectMgr();
		Vector<TreeSelectDb> children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
			return;
		}

		int i = 0;
		String style = "";
		if (!"root".equals(leaf.getCode())) {
			style = "style='display:'";// 设置display为none将不会显示其子节点
		}
		out.print("<table id='childof"
						+ leaf.getCode()
						+ "' "
						+ style
						+ " cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
		for (TreeSelectDb childlf : children) {
			i++;
			boolean isLastChild1 = true;
			if (size != i) {
				isLastChild1 = false;
			}
			ShowLeafAjax(request, privilege, out, childlf, isLastChild1,
					isShowRoot);
		}
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
		String tableid = leaf.getCode();

		String style = "";
		/*
		 * if (!leaf.getCode().equals("root")) { style = "style='display:none'";
		 * }
		 */
		out.println("<table id="
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
		if (childcount == 0) {
			padWidth += 16;
		}
		out.print("<img src='" + request.getContextPath()
				+ "/images/spacer.gif' width=" + padWidth
				+ " height=1 style='visibility:hidden'>");

		if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
			out.println("<img style='cursor:pointer' tableRelate='"
							+ tableid
							+ "' onClick=\"ShowChild(this, '"
							+ tableid
							+ "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
		} else {
			if (childcount > 0) {
				out.println("<img style='cursor:pointer;margin-right:3px' tableRelate='"
								+ tableid
								+ "' onClick=\"ShowChild(this, '"
								+ leaf.getCode()
								+ "')\" src='images/i_plus.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
			} else {
				out.println("<img src='images/folder_01.gif' align='absmiddle'>");
			}
		}

		String className = "";
		if (leaf.getType() == DeptDb.TYPE_UNIT) {
			className = "unit";
		}

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

		if (childcount > 0) {
			out.print("<a href=\"javascript:ShowChild(o('" + tableid
					+ "').getElementsByTagName('img')[1], '" + leaf.getCode()
					+ "')\">伸缩</a>&nbsp;");
		}

		if (!"root".equals(leaf.getCode())) {
			String root_code = (String) request.getAttribute("root_code");
			if (root_code == null) {
				root_code = rootLeaf.getCode();
			}

			if (leaf.getIsHome()) {
				out.print(SkinUtil.LoadString(request, "res.cms.DirectoryView",
						"info_home")
						+ "&nbsp;");
			}
			out.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirbottomFrame' href='officeequip_bottom.jsp?parent_code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "&parent_name="
							+ StrUtil.UrlEncode(name, "utf-8")
							+ "&op=AddChild'>添加子项</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirbottomFrame' href='dept_bottom.jsp?op=modify&code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "&name="
							+ StrUtil.UrlEncode(name, "utf-8")
							+ "&description="
							+ StrUtil.UrlEncode(description, "utf-8")
							+ "'>修改</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirhidFrame' onClick=\"return window.confirm('您确定要删除"
							+ name
							+ "吗?')\" href=\"dept_do.jsp?op=del&root_code="
							+ StrUtil.UrlEncode(rootLeaf.getCode())
							+ "&delcode="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "\">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirhidFrame' href='dept_do.jsp?op=move&direction=up&code="
							+ StrUtil.UrlEncode(code, "utf-8")
							+ "'>上移</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirhidFrame' href='dept_do.jsp?op=move&direction=down&code="
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
			json.put("href", tsd.getLink(request));
			json.put("formCode", tsd.getFormCode());
			
			String rootLink = rootLeaf.getLink(request);
			
			JSONArray nodes = new JSONArray();
			for (TreeSelectDb child : tsd.getChildren()) {
				JSONObject jo = new JSONObject();
				jo.put("text", child.getName());
				jo.put("code", child.getCode());
				jo.put("preCode", child.getPreCode());

				String link = child.getLink(request);
				// 如果未设链接，则按照根节点的链接
				if ("".equals(link)) {
					link = rootLink;
				}

				jo.put("href", link);
				jo.put("formCode", child.getFormCode());

				nodes.put(jo);

				if (child.getChildren().size() > 0) {
					getBootstrapJson(request, child, jo);
				}
			}
			
			if (nodes.length()>0) {
				json.put("nodes", nodes);
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
	}

	public void getTree(HttpServletRequest request, TreeSelectDb tsd) {
		// String rootLink = rootLeaf.getLink(request);
		for (TreeSelectDb child : tsd.getChildren()) {
			String link = child.getLink(request);
			// 如果未设链接，则按照根节点的链接
			/*if ("".equals(link)) {
				link = rootLink;
			}*/
			child.setLink(link);

			if (child.getChildren().size() > 0) {
				getTree(request, child);
			}
		}
	}

	public void getTree(HttpServletRequest request, TreeSelectDb rootLeaf, boolean canManage, com.alibaba.fastjson.JSONObject rootJson, ModuleTreePermission moduleTreePermission, com.alibaba.fastjson.JSONObject rootLinkTo) {
		// com.alibaba.fastjson.JSONObject rootLinkJson = rootLeaf.getLinkJson(request);
		if (canManage || (rootLeaf.isOpen() && moduleTreePermission.canSee(SpringUtil.getUserName(), rootLeaf.getCode()))) {
			com.alibaba.fastjson.JSONArray children = new com.alibaba.fastjson.JSONArray();
			rootJson.put("children", children);
			for (TreeSelectDb child : rootLeaf.getChildren()) {
				if (canManage || (child.isOpen() && moduleTreePermission.canSee(SpringUtil.getUserName(), child.getCode()))) {
					com.alibaba.fastjson.JSONObject linkJson = child.getLinkJson(request);
					if (linkJson.getBoolean("isShow")) {
						com.alibaba.fastjson.JSONObject jsonChild = new com.alibaba.fastjson.JSONObject();
						// 如果未设链接，则按照根节点的链接
						if (StrUtil.isEmpty(linkJson.getString("link"))) {
							jsonChild.put("linkTo", rootLinkTo);
						} else {
							jsonChild.put("linkTo", linkJson);
						}

						jsonChild.put("code", child.getCode());
						jsonChild.put("parentCode", child.getParentCode());
						jsonChild.put("name", child.getName());
						jsonChild.put("layer", child.getLayer());
						jsonChild.put("isOpen", child.isOpen());
						// 用于控制右键菜单是否显示
						jsonChild.put("canManage", moduleTreePermission.canManage(SpringUtil.getUserName(), child.getCode()));
						children.add(jsonChild);

						getTree(request, child, canManage, jsonChild, moduleTreePermission, rootLinkTo);
					}
				}
			}
		}
	}
}
