package com.redmoon.oa.flow;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.servlet.jsp.JspWriter;
import java.util.Iterator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.SpringUtil;
import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.WorkflowPredefineDb;

public class DirectoryView {
    Leaf rootLeaf;
    Vector UprightLineNodes = new Vector(); //用于显示竖线

    public DirectoryView(Leaf rootLeaf) {
        this.rootLeaf = rootLeaf;
    }

    public void ListSimple(JspWriter out, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ListTreeSimple(out, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    void ListTreeSimple(JspWriter out, Leaf leaf,
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
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSimple(JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

        if (!isLastChild) {
            Leaf brotherleaf = leaf.getBrother("down");
            // LogUtil.getLog(getClass()).info("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) {
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    // LogUtil.getLog(getClass()).info(leaf.getCode() + " layer=" + layer +
                    //                   " count=" + count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();
        // LogUtil.getLog(getClass()).info(code + " childcount=" + childcount);

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
                        //LogUtil.getLog(getClass()).info("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
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

    public void list(HttpServletRequest request, JspWriter out) throws Exception {
        ListTree(request, out, rootLeaf, true);
    }

    // 显示根结点为leaf的树
    void ListTree(HttpServletRequest request, JspWriter out, Leaf leaf,
                  boolean isLastChild) throws Exception {
        Privilege privilege = new Privilege();
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
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            Leaf childlf = (Leaf) ri.next();
            boolean isLastChild1 = true;
            if (size != i)
                isLastChild1 = false;
            LeafPriv lp = new LeafPriv(childlf.getCode());
            if (lp.canUserSee(privilege.getUser(request)))
                ListTree(request, out, childlf, isLastChild1);
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
            // LogUtil.getLog(getClass()).info("get leaf brother" + leaf.getName());
            Leaf brotherleaf = leaf.getBrother("down");
            // LogUtil.getLog(getClass()).info("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) { // =0的也计入的话会在树底端的结点产生多余竖线
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    // LogUtil.getLog(getClass()).info(leaf.getCode() + " layer=" + layer +
                    //                   " count=" + count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();
        // LogUtil.getLog(getClass()).info(code + " childcount=" + childcount);

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
                        //LogUtil.getLog(getClass()).info("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
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

        if (leaf.getType()==Leaf.TYPE_LIST)
            out.print("<a class='link_list' target='_parent' href='flow_list.jsp?typeCode=" + StrUtil.UrlEncode(leaf.getCode()) + "'>" + name + "</a>");
        else if (leaf.getType()==Leaf.TYPE_FREE) {
            out.print("<a class='link_free' target='_parent' href='flow_list.jsp?typeCode=" + StrUtil.UrlEncode(leaf.getCode()) + "'>" + name + "</a>");
        }
        else
            out.print("<font class='flow_type'>" + name + "</font>");

        out.print("     </td><td width='15%' align=right nowrap>");
        if (!leaf.getCode().equals("root")) {
            // if (leaf.getIsHome())
            //    out.print("首页&nbsp;");
            // out.print("<a href='dir_top.jsp?op=removecache&code=" + code +
            //          "'>清缓存</a>&nbsp;");
            if (leaf.getType() == leaf.TYPE_LIST)
                out.print("预置流程&nbsp;");
            else if (leaf.getType()==Leaf.TYPE_FREE)
                out.print("自由流程&nbsp;");
            else
                out.print("分类节点&nbsp;");

            out.print(
                    "<a target=dirbottomFrame href='flow_dir_bottom.jsp?parent_code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                    StrUtil.UrlEncode(name, "utf-8") +
                    "&op=AddChild'>添加</a>&nbsp;");
            out.print(
                    "<a target='dirbottomFrame' href='flow_dir_bottom.jsp?op=modify&code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&name=" +
                    StrUtil.UrlEncode(name, "utf-8") + "&description=" +
                    StrUtil.UrlEncode(description, "utf-8") + "'>修改</a>&nbsp;");
            if (leaf.getType() == Leaf.TYPE_LIST || leaf.getType()==Leaf.TYPE_FREE) {
                out.print(
                        "<a target=_parent href='flow_predefine_list.jsp?dirCode=" +
                        StrUtil.UrlEncode(code) + "'>预设流程</a>&nbsp;");
                String formCode = leaf.getFormCode();
                if (!formCode.equals(""))
                    out.print(
                            "<a target=_parent title='编辑表单' href='form_edit.jsp?code=" +
                            StrUtil.UrlEncode(leaf.getFormCode()) +
                            "'>表单</a>&nbsp;");
                else
                    out.print(
                            "<a href=\"javascript:alert('流程未与表单关联')\" title='流程未与表单关联'>表单</a>&nbsp;");
            }
            else {
                out.print(
                        "<a target=_parent href='flow_dir_priv_m.jsp?dirCode=" +
                        StrUtil.UrlEncode(code) + "'>权限</a>&nbsp;");
                out.print(
                        "<a target=_parent href='form_m.jsp?flowTypeCode=" +
                        StrUtil.UrlEncode(leaf.getCode()) +
                        "' title='管理表单'>表单</a>&nbsp;");
            }

            out.print(
                    "<a target=_self href=# onClick=\"if (window.confirm('您确定要删除" +
                    name +
                    "吗？相关流程也将会一起被删除！')) window.location.href='flow_dir_top.jsp?op=del&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&delcode=" +
                    StrUtil.UrlEncode(code, "utf-8") + "'\">删除</a>&nbsp;");
            out.print("<a href='flow_dir_top.jsp?op=move&direction=up&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>上移</a>&nbsp;");
            out.print("<a href='flow_dir_top.jsp?op=move&direction=down&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;");
        }
        out.println("  </td></tr></tbody></table>");
    }

    void ShowLeafAsOption(HttpServletRequest request, JspWriter out, Leaf leaf, int rootlayer)
                  throws Exception {
    	Privilege privilege = new Privilege();
    	/*
    	if (!privilege.isUserPrivValid(request, "admin")) {
    		// 如果是公共流程
	        if (leaf.getUnitCode().equals(Leaf.UNIT_CODE_PUBLIC)) {
	        	LeafPriv lp = new LeafPriv(leaf.getCode());
				// 如果对目录有管理权限则可见
				if (lp.canUserExamine(privilege.getUser(request))) {
					;
				}
				else
					return;
	        }
	        else {
	        	// 如果不是本单位的流程则不可见
	        	if (!leaf.getUnitCode().equals(privilege.getUserUnitCode(request))) {
	        		return;
	        	}
	        }
    	}
    	*/
    	
/*    	// 如果是管理员，则能看到全部流程
    	if (!privilege.isUserPrivValid(request, "admin")) {
	    	// 如果用户不能发起流程，则不能看到
	    	if (!canUserSeeWhenInitFlow(request, leaf))
	    		return;
    	}*/

        String code = leaf.getCode();
        //String name = leaf.getName();
        String name = leaf.getName(request);
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            if (leaf.getType()==Leaf.TYPE_LIST || leaf.getType()==Leaf.TYPE_FREE)
                out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
            else
                out.print("<option value='not'>" + blank + "╋ " + name + "</option>");
        }
        else {
            /*
            if (leaf.getType()==leaf.TYPE_LIST)
                out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
            else
                out.print("<option value='not'>" + blank + "├『" + name +
                      "』</option>");
             */
        	//if(canUserSeeWhenInitFlow(request,leaf)){
            if (leaf.getType()==Leaf.TYPE_LIST || leaf.getType()==Leaf.TYPE_FREE)
                out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "   " + name +
                      "</option>");
            else
                out.print("<option value='not'>" + blank + "   " + name +
                      "</option>");
        	//}
        }
    }

    public String getLeafAsOption(HttpServletRequest request, Leaf leaf, int rootlayer) {
        String code = leaf.getCode();
        String name = leaf.getName(request);
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        String str = "";
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
            if (leaf.getType()==Leaf.TYPE_LIST || leaf.getType()==Leaf.TYPE_FREE) {
                str = "<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>";
            } else {
                str = "<option value='not'>" + blank + "╋ " + name + "</option>";
            }
        }
        else {
            if (leaf.getType()==Leaf.TYPE_LIST || leaf.getType()==Leaf.TYPE_FREE) {
                str = "<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "   " + name + "</option>";
            } else {
                str = "<option value='not'>" + blank + "   " + name + "</option>";
            }
        }
        return str;
    }

    // 显示根结点为leaf的树
    public void getDirectoryAsOptions(HttpServletRequest request, Leaf leaf, int rootlayer, StringBuffer opts) throws ErrMsgException {
        if(!canUserSeeWhenInitFlow(request,leaf)){
            Privilege pvg = new Privilege();
            LeafPriv lp = new LeafPriv(leaf.getCode());
            if (!lp.canUserModify(pvg.getUser(request))) {
                return;
            }
        }

        if (leaf.isOpen()) {
            opts.append(getLeafAsOption(request, leaf, rootlayer));
        }
        else {
            return;
        }
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            if (childlf.isOpen()) {
                getDirectoryAsOptions(request, childlf, rootlayer, opts);
            }
        }
    }

    // 显示根结点为leaf的树
    public void ShowDirectoryAsOptions(HttpServletRequest request, JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        if(!canUserSeeWhenInitFlow(request,leaf)){
            Privilege pvg = new Privilege();
            LeafPriv lp = new LeafPriv(leaf.getCode());
            if (!lp.canUserModify(pvg.getUser(request))) {
                return;
            }
        }

        if (leaf.isOpen()) {
        	ShowLeafAsOption(request, out, leaf, rootlayer);
        }
        else {
            return;
        }
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        Iterator ri = children.iterator();
        
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            if (childlf.isOpen()) {
            	ShowDirectoryAsOptions(request, out, childlf, rootlayer);
            }
        }
    }

    /**
     * 把列表或无内容显示为蓝色
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    void ShowLeafAsOptionWithCode(JspWriter out, Leaf leaf, int rootlayer) throws Exception {
    	HttpServletRequest request = null;
        String code = leaf.getCode();
        String name = leaf.getName(request);
        int layer = leaf.getLayer();
        String description = leaf.getDescription();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        // out.print("<option value='" + code + "'>" + blank + "╋ " + name + "</option>");


        if (leaf.getChildCount()>0) {
            if (leaf.getType()==Leaf.TYPE_NONE)
                out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
            else
                out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "├『" + name + "』</option>");
        }
        else {
            if (leaf.getType()==Leaf.TYPE_LIST)
                out.print("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                      "』</option>");
            else
                out.print("<option value='" + code + "'>" + blank + "├『" + name +
                      "』</option>");
        }
    }

    /**
     * 显示根结点为leaf的树，value中全为code
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    public void ShowDirectoryAsOptionsWithCode(JspWriter out, Leaf leaf, int rootlayer) throws Exception {
    	if (leaf.isOpen()) {
    		ShowLeafAsOptionWithCode(out, leaf, rootlayer);
    	}
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            if (childlf.isOpen()) {
            	ShowDirectoryAsOptionsWithCode(out, childlf, rootlayer);
            }
        }
    }

    /**
     * 只显示分类节点
     * @param out JspWriter
     * @param leaf Leaf
     * @param rootlayer int
     * @throws Exception
     */
    public void ShowFlowTypeAsOptionsWithCode(HttpServletRequest request, JspWriter out, Leaf leaf, int rootlayer) throws Exception {
        if (leaf.getType()==Leaf.TYPE_NONE)
            ShowFlowTypeLeafAsOptionWithCode(request, out, leaf, rootlayer);
        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            if (childlf.getType()==Leaf.TYPE_NONE)
                ShowFlowTypeAsOptionsWithCode(request, out, childlf, rootlayer);
        }
    }

    void ShowFlowTypeLeafAsOptionWithCode(HttpServletRequest request, JspWriter out, Leaf leaf,
                                          int rootlayer) throws Exception {
        String code = leaf.getCode();
    	
    	Privilege privilege = new Privilege();
    	if (!privilege.isUserPrivValid(request, "admin")) {
    		// 如果是公共流程
	        if (leaf.getUnitCode().equals(Leaf.UNIT_CODE_PUBLIC)) {
	        	LeafPriv lp = new LeafPriv(code);
				// 如果对目录有管理权限则可见
				if (lp.canUserExamine(privilege.getUser(request))) {
					;
				}
				else
					return;
	        }
	        else {
	        	// 如果不是本单位的流程则不可见
	        	if (!leaf.getUnitCode().equals(privilege.getUserUnitCode(request))) {
	        		return;
	        	}
	        }
    	}
    	
        String name = leaf.getName(request);
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer - rootlayer;
        for (int i = 0; i < d; i++) {
            blank += "　";
        }
        // out.print("<option value='" + code + "'>" + blank + "╋ " + name + "</option>");
        
        if (leaf.getChildCount() > 0) {
            out.print("<option value='" + code + "' style='COLOR: #0005ff'>" +
                      blank + "╋ " + name + "</option>");
        } else {
            out.print("<option value='" + code + "'>" + blank + "├『" + name +
                      "』</option>");
        }
    }
    
    void ShowLeafAsOptionForSchedule(HttpServletRequest request, JspWriter out, Leaf leaf,
			int rootlayer) throws Exception {
		Privilege privilege = new Privilege();
		// 如果是管理员，则能看到全部流程
		if (!privilege.isUserPrivValid(request, "admin")) {
			// 如果用户不能发起流程，则不能看到
			if (!canUserSeeWhenInitFlow(request, leaf))
				return;
		}

		String code = leaf.getCode();
		// String name = leaf.getName();
		String name = leaf.getName(request);
		int layer = leaf.getLayer();
		String blank = "";
		int d = layer - rootlayer;
		for (int i = 0; i < d; i++) {
			blank += "　";
		}
		if (leaf.getChildCount() > 0) {
			if (leaf.getType() == Leaf.TYPE_LIST) {
				WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		        wpd = wpd.getDefaultPredefineFlow(leaf.getCode());
		        boolean isPredefined = wpd != null && wpd.isLoaded();
		        if (!isPredefined) {
		        	out.print("<optgroup label='" + blank + "╋ " + name
							+ "'></optgroup>");
		        } else {
			        Vector v = wpd.getStarters();
			        if (v.size() == 0) {
						out.print("<optgroup label='" + blank + "╋ " + name
							+ "'></optgroup>");
					} else {
						out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
					}
		        }
			} else {
				out.print("<optgroup label='" + blank + "╋ " + name
							+ "'></optgroup>");
			}
		} else {
			if (leaf.isLoaded() && leaf.getType() == Leaf.TYPE_LIST) {
				WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		        wpd = wpd.getDefaultPredefineFlow(leaf.getCode());
		        boolean isPredefined = wpd != null && wpd.isLoaded();
		        if (!isPredefined) {
		        	out.print("<optgroup label='" + blank + name
							+ "'></optgroup>");
		        } else {
			        Vector v = wpd.getStarters();
			        if (v.size() == 0) {
						out.print("<optgroup label='" + blank + name
							+ "'></optgroup>");
					} else {
						out.print("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + name + "</option>");
					}
		        }
			} else {
				out.print("<optgroup label='" + blank + name
							+ "'></optgroup>");
			}
		}
	}

    /**
     * 
     * @Description: 流程类型宏控件遍历下拉所有流程
     * @param leaf
     * @param rootlayer
     * @param flowTypeCode
     * @throws Exception
     */
    //拼接options
    public String getFlowAsOption(Leaf leaf, int rootlayer,String flowTypeCode) {
        String outStr = "";
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount()>0) {
        	outStr += "<option value='" + code + "'>" + blank + "╋ " + name + "</option>";
        }
        else {
        	// 根据流程类型判断默认选中项
        	if(!"".equals(flowTypeCode)&&code.equals(flowTypeCode)){
            outStr += "<option value=\"" + code + "\" selected='selected'>" + blank + "├『" + name +
                      "』</option>";
        	}else{
        		outStr += "<option value=\"" + code + "\">" + blank + "├『" + name +
                "』</option>";
        	}
        }
        return outStr;
    }

    /**
     * 按树形显示流程类型
     * @param request
     * @param outStr
     * @param leaf
     * @param rootlayer
     * @param flowTypeCode 默认选中项
     * @return
     * @throws ErrMsgException
     */
    public StringBuffer getFlowTypeAsOptions(HttpServletRequest request, StringBuffer outStr, Leaf leaf, int rootlayer,String flowTypeCode) throws ErrMsgException {
    	outStr.append(getFlowAsOption(leaf, rootlayer,flowTypeCode));
    	Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            getFlowTypeAsOptions(request, outStr, childlf, rootlayer,flowTypeCode);
        }
        return outStr;
    }  
    
	// 显示根结点为leaf的树
	public void ShowDirectoryAsOptionsForSchedule(HttpServletRequest request,
			JspWriter out, Leaf leaf, int rootlayer) throws Exception {
		ShowLeafAsOptionForSchedule(request, out, leaf, rootlayer);
		Directory dir = new Directory();
		Vector children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
            return;
        }

		if (!canUserSeeWhenInitFlow(request, leaf)) {
			return;
		}
		int i = 0;
		Iterator ri = children.iterator();

		while (ri.hasNext()) {
			Leaf childlf = (Leaf) ri.next();
			ShowDirectoryAsOptionsForSchedule(request, out, childlf, rootlayer);
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
                      boolean isLastChild, String target, String func,
                      String tableClass, String tableClassMouseOn) throws
            Exception {
        // 发起流程页面
        boolean isFlowInit = StrUtil.getNullStr((String)request.getAttribute("isFlowInit")).equals("true");    
        // 流程查询页面
        boolean isSeeFlowType = StrUtil.getNullStr((String)request.getAttribute("isSeeFlowType")).equals("true");
        
        Privilege privilege = new Privilege();
        LeafPriv lp = new LeafPriv(leaf.getCode());

        ShowLeafFunc(request, out, leaf, isLastChild, target, func, tableClass,
                tableClassMouseOn);
        
        /*
        if (isFlowInit) {
        	// 发起流程界面
            if (canUserSeeWhenInitFlow(request, leaf)) {
            	ShowLeafFunc(request, out, leaf, isLastChild, target,
                             func, tableClass, tableClassMouseOn);
            }
        }
        else {
        	// 流程查询界面
        	if (isSeeFlowType) {
        		if (lp.canUserSee(privilege.getUser(request)) || lp.canUserQuery(privilege.getUser(request)) || lp.canUserExamine(privilege.getUser(request))) {
        	        ShowLeafFunc(request, out, leaf, isLastChild, target, func, tableClass,
   	                     tableClassMouseOn);        			
        		}
        	}
        	else {
    	        ShowLeafFunc(request, out, leaf, isLastChild, target, func, tableClass,
	                     tableClassMouseOn);
        	}
        }
        */
                
        // Directory dir = new Directory();
        Vector children;
        // 如果是一级目录,则全部列出
        if (leaf.getLayer()==1 || privilege.isUserPrivValid(request, "admin"))
        	children = leaf.getChildren();
        else // 如果是二级目录,则只列出本单位的节点
        	children = leaf.getChildren(privilege.getUserUnitCode(request)); // dir.getChildren(leaf.getCode());
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
            lp = new LeafPriv(childlf.getCode());
            

            if (isFlowInit) {
            	// 发起流程界面
                if (canUserSeeWhenInitFlow(request, childlf)) {
                    ListTreeFunc(request, out, childlf, isLastChild1, target,
                                 func, tableClass, tableClassMouseOn);
                }
            }
            else {
            	// 流程查询界面
            	if (isSeeFlowType) {
            		// if (lp.canUserSee(privilege.getUser(request)) || lp.canUserQuery(privilege.getUser(request)) || lp.canUserExamine(privilege.getUser(request))) {
            		if (canUserSeeWhenInitFlow(request, childlf)) {
	                    ListTreeFunc(request, out, childlf, isLastChild1, target,
                                func, tableClass, tableClassMouseOn);            			
            		}
            	}
            	else {
	                boolean canSee = false;
	        		if (leaf.getCode().equals(Leaf.CODE_ROOT) || privilege.isUserPrivValid(request, "admin"))
	        			canSee = true;
	        		else {
		        		// 如果是公共流程
		        		if (leaf.getUnitCode().equals(Leaf.UNIT_CODE_PUBLIC)) {
		        			// 公共流程也可能是子单位的流程
		        			// 如果是管理员，或者对目录有管理权限则可见
		        			if (lp.canUserExamine(privilege.getUser(request))) {
		        				canSee = true;
		        			}
		        		}
		        		else {
		        			// 如果是本单位的流程
		        			if (leaf.getUnitCode().equals(privilege.getUserUnitCode(request))) {
		        				if (privilege.isUserPrivValid(request, "admin.unit"))
		        					canSee = true;
		        				else {
						            if (lp.canUserSee(privilege.getUser(request)) || lp.canUserQuery(privilege.getUser(request)) || lp.canUserExamine(privilege.getUser(request))) {
						                canSee = true;
						            }     
		        				}
		        			}
		        		}
	        		}
	        		
	        		if (canSee) {
	                    ListTreeFunc(request, out, childlf, isLastChild1, target,
                                func, tableClass, tableClassMouseOn);	        			
	        		}
	                
            	}
            }

        }
        if (size > 0)
            out.print("</td></tr></table>");
    }

    void ShowLeafFunc(HttpServletRequest request, JspWriter out, Leaf leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
    	// 检查对于用户是否可见
        DeptUserDb du = new DeptUserDb();
        String userName = new Privilege().getUser(request);
        Vector v = du.getDeptsOfUser(userName);
        int vsize = v.size();

        Privilege privilege = new Privilege();

        boolean canSee = false;

        // 发起流程页面
        boolean isFlowInit = StrUtil.getNullStr((String)request.getAttribute("isFlowInit")).equals("true");
        boolean isSeeFlowType = StrUtil.getNullStr((String)request.getAttribute("isSeeFlowType")).equals("true");

        if (isFlowInit) {
        	// 发起流程界面
            if (canUserSeeWhenInitFlow(request, leaf)) {
                canSee = true;
            }
        }
        else {
        	if (isSeeFlowType) {
	            LeafPriv lp = new LeafPriv(leaf.getCode());        	
	            if (lp.canUserSee(privilege.getUser(request)) || lp.canUserExamine(privilege.getUser(request))) {
	                canSee = true;
	            }        	
        	}
        	else {
        		if (leaf.getCode().equals(Leaf.CODE_ROOT) || privilege.isUserPrivValid(request, "admin"))
        			canSee = true;
        		else {
		            LeafPriv lp = new LeafPriv(leaf.getCode());        	        		
	        		// 如果是公共流程
	        		if (leaf.getUnitCode().equals(Leaf.UNIT_CODE_PUBLIC)) {
	        			// 公共流程也可能是子单位的流程
	        			
	        			// 如果是管理员，或者对目录有管理权限则可见
	        			if (lp.canUserExamine(privilege.getUser(request))) {
	        				canSee = true;
	        			}
	        		}
	        		else {
	        			// 如果是本单位的流程
	        			if (leaf.getUnitCode().equals(privilege.getUserUnitCode(request))) {
	        				if (privilege.isUserPrivValid(request, "admin.unit"))
	        					canSee = true;
	        				else {
					            if (lp.canUserSee(privilege.getUser(request)) || lp.canUserExamine(privilege.getUser(request))) {
					                canSee = true;
					            }     
	        				}
	        			}
	        		}
        		}
        	}
        }
        /*
        if (privilege.isUserPrivValid(request, "admin.flow")) {
            LeafPriv lp = new LeafPriv(leaf.getCode());
            if (lp.canUserSee(privilege.getUser(request)) || lp.canUserExamine(privilege.getUser(request))) {
                canSee = true;
            }
        }
        if (!canSee) {
            String[] depts = StrUtil.split(leaf.getDept(), ",");
            if (depts != null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    for (int j = 0; j < vsize; j++) {
                        if (depts[i].equals(((DeptDb) v.elementAt(j)).
                                            getCode())) {
                            canSee = true;
                            break;
                        }
                    }
                    if (canSee)
                        break;
                }
            }
            else
                canSee = true;
        }
        */
        
        if (!canSee) // 用户所属部门不在指定的范围内
            return;

        String code = leaf.getCode();
        //String name = leaf.getName();
        String name = leaf.getName(request);
        int layer = leaf.getLayer();
        // String description = leaf.getDescription();

        if (!isLastChild) {
            Leaf brotherleaf = leaf.getBrother("down");
            // LogUtil.getLog(getClass()).info("brother=" + brotherleaf);
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChildOfUnit(r, leaf, privilege.getUserUnitCode(request));
                int count = r.size();
                if (count>0) {
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    // LogUtil.getLog(getClass()).info(leaf.getCode() + " layer=" + layer +
                    //                   " count=" + count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();
        // LogUtil.getLog(getClass()).info(code + " childcount=" + childcount);

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
                	out.print("<img src='images/i_plus-2.gif' style='visibility:hidden' align='absmiddle'>");
                    // node.show(out, "images/i_plus-2.gif");
                    if (node.getCount() == 0) {
                        UprightLineNodes.remove(node);
                        //LogUtil.getLog(getClass()).info("Remove " + node);
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed)
                out.println("<img src='" + request.getContextPath() + "/images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-3.gif' style='visibility:hidden' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            } else { // 不是最后一个孩子结点
                if (childcount > 0)
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1-0.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                else
                    out.println("<img src='images/i_plus-2-2.gif' style='visibility:hidden' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            }
        }

        if (leaf.getType()==Leaf.TYPE_LIST)
            out.print("<a class='flow_list" + (!leaf.isOpen()?" disabled":"") + "' target='" + target +
                      "' href='javascript:;' onClick=\"" + func + "('" + code +
                      "', '" + name + "', " + leaf.getType() +
                      ")\">" + name + "</a>");
        else if (leaf.getType()==Leaf.TYPE_FREE) {
            out.print("<a class='flow_free" + (!leaf.isOpen()?" disabled":"") + "' target='" + target +
                      "' href='javascript:;' onClick=\"" + func + "('" + code +
                      "', '" + name + "', " + leaf.getType() +
                      ")\">" + name + "</a>");
        }
        else
            out.print("<a class='flow_type" + (!leaf.isOpen()?" disabled":"") + "' target='" + target +
                      "' href='javascript:;' onClick=\"" + func + "('" + code +
                      "', '" + name + "', " + leaf.getType() +
                      ")\">" + name + "</a>");

        /*
        // 三种类型节点，用同一个Link
        if (leaf.getType()==leaf.TYPE_NONE)
            out.print(
                    "<a target='" + target + "' href='#' onClick=\"" + func + "('" + code + "', '" + name + "', " + leaf.getType() + ")\"><font color='#993300'>" + name + "</font></a>");
        else
            out.print(
                    "<a target='" + target + "' href='#' onClick=\"" + func + "('" + code + "', '" + name + "', " + leaf.getType() + ")\">" + name + "</a>");
        */
        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    /**
     * 检查当发起流程时，用户能否看到流程节点
     * @param request HttpServletRequest
     * @param leaf Leaf
     * @return boolean
     */
    public boolean canUserSeeWhenInitFlow(HttpServletRequest request, Leaf leaf) {
        // 检查对于用户是否可见
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, Privilege.ADMIN)) {
    	    return true;
        }
        
        // 如果不是公共流程，则判断是否为本单位流程，如果是公共流程，则继续往下判断权限
        if (!leaf.getUnitCode().equals(Leaf.UNIT_CODE_PUBLIC)) {
        	// 用户可能有兼职，得根据兼职单位判断
        	/*
        	if (!leaf.getUnitCode().equals(pvg.getUserUnitCode(request))) {
        		return false;
        	}
        	*/
        	
        	boolean isFound = false;
        	String userName = pvg.getUser(request);
        	DeptUserDb dud = new DeptUserDb();
        	String[] ary = dud.getUnitsOfUser(userName);
        	int len = 0;
        	if (ary!=null) {
                len = ary.length;
            }
        	for (int i=0; i<len; i++) {
        		if (ary[i].equals(leaf.getUnitCode())) {
        			isFound = true;
        		}
        	}
        	
        	if (!isFound) {
                return false;
            }
        }

        LeafPriv lp = new LeafPriv(leaf.getCode());
        // 如果用户能管理或者有浏览流程的权限,则判断有无部门限制
        // 20150401 fgf 因为进入待办时，显示流程类型树慢而优化，有发起的权限才能看到
        if (lp.canUserSee(pvg.getUser(request))) { // || lp.canUserExamine(pvg.getUser(request))) {
            String[] depts = StrUtil.split(leaf.getDept().trim(), ",");
            if (depts != null) {
                DeptUserDb du = new DeptUserDb();
                Vector v = du.getDeptsOfUser(pvg.getUser(request));
                int vsize = v.size();

                boolean found = false;
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    for (int j = 0; j < vsize; j++) {
                        if (depts[i].equals(((DeptDb) v.elementAt(j)).getCode())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                return found;
            }
            // @task:应加入对发起节点的判断，比较费时，暂不考虑
            /*
            WorkflowPredefineDb wpd = new WorkflowPredefineDb();
            wpd = wpd.getDefaultPredefineFlow(leaf.getCode());
            
            WorkflowDb wf = new WorkflowDb();
            wf.regeneratePredefinedFlowString(pvg.getUser(request), wpd.getFlowString());
            */
            
            return true;
        }
        else {
            return false;
        }
    }
    
    public String getJsonString() {
		Leaf dir = new Leaf();
		JSONArray ary = new JSONArray();
        getJson(dir, "-1", ary);
        return ary.toString();
	}

	/**
	 * 递归获得jsTree的json字符串
	 * 
	 * @param parentCode
	 *            父节点parentCode
	 * @return ary
	 */
    private void getJson(Leaf lf, String parentCode, JSONArray ary) {
        // 把顶层的查出来
        Vector<Leaf> children = lf.getTreeChildren(parentCode);
        for (Leaf childlf : children) {
            if (!childlf.getRootCode().equals(rootLeaf.getCode())) {
                continue;
            }

            if ("-1".equals(parentCode)) {
                JSONObject json = new JSONObject();
                json.put("id", childlf.getCode());
                json.put("parent", "#");
                json.put("text", childlf.getName().replaceAll("\"", "\\\\\""));
                JSONObject jsonState = new JSONObject();
                jsonState.put("opened", true);
                json.put("state", jsonState);
                ary.add(json);

            } else {
                JSONObject json = new JSONObject();
                json.put("id", childlf.getCode());
                json.put("parent", childlf.getParentCode());
                json.put("text", childlf.getName().replaceAll("\"", "\\\\\""));
                ary.add(json);
            }
            Vector<Leaf> childs = lf.getTreeChildren(childlf.getCode());
            // 如果有子节点
            if (!childs.isEmpty()) {
                // 遍历它的子节点
                for (Leaf child : childs) {
                    JSONObject json = new JSONObject();
                    json.put("id", child.getCode());
                    json.put("parent", child.getParentCode());
                    json.put("text", child.getName().replaceAll("\"", "\\\\\""));
                    ary.add(json);

                    // 还有子节点(递归调用)
                    Vector<Leaf> ch = lf.getTreeChildren(child.getCode());
                    if (!ch.isEmpty()) {
                        getJson(lf, child.getCode(), ary);
                    }
                }
            }
        }
    }

    public void getTreeOpened(Leaf rootLeaf, JSONObject rootJson) {
        JSONArray children = new JSONArray();
        rootJson.put("children", children);
        LeafChildrenCacheMgr leafChildrenCacheMgr = new LeafChildrenCacheMgr(rootLeaf.getCode());
        Vector<Leaf> childs = leafChildrenCacheMgr.getDirList();
        for (Leaf child : childs) {
            if (!child.isOpen()) {
                continue;
            }
            JSONObject jsonChild = new JSONObject();
            jsonChild.put("code", child.getCode());
            jsonChild.put("parentCode", child.getParentCode());
            jsonChild.put("name", child.getName());
            jsonChild.put("layer", child.getLayer());
            jsonChild.put("icon", child.getIcon());
            children.add(jsonChild);
            getTreeOpened(child, jsonChild);
        }
    }

    // 发起流程界面
    public void getTree(Leaf rootLeaf, JSONObject rootJson, boolean isIcon) {
        DirectoryView dv = new DirectoryView(rootLeaf);
        if (rootLeaf.isOpen() && dv.canUserSeeWhenInitFlow(SpringUtil.getRequest(), rootLeaf)) {
            JSONArray children = new JSONArray();
            rootJson.put("children", children);
            LeafChildrenCacheMgr leafChildrenCacheMgr = new LeafChildrenCacheMgr(rootLeaf.getCode());
            Vector<Leaf> childs = leafChildrenCacheMgr.getDirList();
            for (Leaf child : childs) {
                if (child.isOpen() && dv.canUserSeeWhenInitFlow(SpringUtil.getRequest(), child)) {
                    JSONObject jsonChild = new JSONObject();
                    jsonChild.put("code", child.getCode());
                    jsonChild.put("parentCode", child.getParentCode());
                    jsonChild.put("name", child.getName());
                    jsonChild.put("layer", child.getLayer());
                    // 注释掉，以免icon在antdv的树中带有圆点符号，不能注释，因为发起流程的页面会用到
                    if (isIcon) {
                        jsonChild.put("icon", child.getIcon());
                    }
                    children.add(jsonChild);
                    getTree(child, jsonChild, isIcon);
                }
            }
        }
    }

    public void getTreeForQuery(Leaf rootLeaf, JSONObject rootJson, boolean isIcon) {
        LeafPriv lp = new LeafPriv(rootLeaf.getCode());
        String userName = SpringUtil.getUserName();

        if (rootLeaf.isOpen()) {
            JSONArray children = new JSONArray();
            boolean canQuery = lp.canUserQuery(userName);
            if (canQuery) {
                rootJson.put("children", children);
            }

            LeafChildrenCacheMgr leafChildrenCacheMgr = new LeafChildrenCacheMgr(rootLeaf.getCode());
            Vector<Leaf> childs = leafChildrenCacheMgr.getDirList();
            for (Leaf child : childs) {
                LeafPriv lpChild = new LeafPriv(child.getCode());
                if (child.isOpen() && lpChild.canUserQuery(userName)) {
                    // 只要有一个子节点能查询，则能够看到父节点
                    if (!canQuery) {
                        canQuery = true;
                        rootJson.put("children", children);
                    }
                    JSONObject jsonChild = new JSONObject();
                    jsonChild.put("code", child.getCode());
                    jsonChild.put("parentCode", child.getParentCode());
                    jsonChild.put("name", child.getName());
                    jsonChild.put("layer", child.getLayer());
                    // 注释掉，以免icon在antdv的树中带有圆点符号，不能注释，因为发起流程的页面会用到
                    if (isIcon) {
                        jsonChild.put("icon", child.getIcon());
                    }
                    children.add(jsonChild);
                    getTreeWhenQuery(child, jsonChild, isIcon);
                }
            }
        }
    }

    // 发起流程界面
    public void getTreeWhenQuery(Leaf rootLeaf, JSONObject rootJson, boolean isIcon) {
        JSONArray children = new JSONArray();
        rootJson.put("children", children);
        LeafChildrenCacheMgr leafChildrenCacheMgr = new LeafChildrenCacheMgr(rootLeaf.getCode());
        Vector<Leaf> childs = leafChildrenCacheMgr.getDirList();
        for (Leaf child : childs) {
            LeafPriv lpChild = new LeafPriv(child.getCode());
            if (child.isOpen() && lpChild.canUserQuery(SpringUtil.getUserName())) {
                JSONObject jsonChild = new JSONObject();
                jsonChild.put("code", child.getCode());
                jsonChild.put("parentCode", child.getParentCode());
                jsonChild.put("name", child.getName());
                jsonChild.put("layer", child.getLayer());
                // 注释掉，以免icon在antdv的树中带有圆点符号，不能注释，因为发起流程的页面会用到
                if (isIcon) {
                    jsonChild.put("icon", child.getIcon());
                }
                children.add(jsonChild);
                getTree(child, jsonChild, isIcon);
            }
        }
    }
	
	/**
	 * 取得所有不启用的节点
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getAllUnused() throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select code from flow_directory where is_open=0";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		while (ri.hasNext()) {
			ResultRecord rr = ri.next();
			list.add(rr.getString(1));
		}
		return list;
	}
	
	public void getJsonByUser(Directory dir, String parentCode,
			String userName, ArrayList<String> list) throws Exception {

		LeafPriv lp = new LeafPriv(parentCode);
		if (!parentCode.equals(Leaf.CODE_ROOT)) {
			if (lp.canUserQuery(userName)) {
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
		}
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			Leaf childlf = (Leaf) ri.next();
			if (!childlf.isOpen()) {
				continue;
			}
			lp = new LeafPriv(childlf.getCode());
			if (lp.canUserQuery(userName)) {
				if (!list.contains(childlf.getCode())) {
					list.add(childlf.getCode());
				}
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
			if (childlf.getChildCount() > 0) {
				getJsonByUser(dir, childlf.getCode(), userName, list);
			}
			if (list.contains(childlf.getCode())) {
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
		}
	}

	public String getJsonStringByUser(Leaf leaf, String userName)
			throws Exception {
		Directory dir = new Directory();
		String str = "[{id:\"" + leaf.getCode() + "\",parent:\"#\",text:\""
				+ leaf.getName().replaceAll("\"", "\\\\\"") + "\",state:{opened:true}} ,";
		// 从根开始
		ArrayList<String> list = new ArrayList<String>();
		getJsonByUser(dir, leaf.getCode(), userName, list);
		for (String node : list) {
			if (node.equals(Leaf.CODE_ROOT)) {
				continue;
			}
			Leaf childlf = new Leaf(node);
			str += "{id:\"" + childlf.getCode() + "\",parent:\""
					+ childlf.getParentCode() + "\",text:\""
					+ childlf.getName().replaceAll("\"", "\\\\\"") + "\" },";
		}
		str = str.substring(0, str.length() - 1);
		str += "]";
		// LogUtil.getLog(getClass()).info(str);
		return str;
	}

    public void getTreeAll(Leaf rootLeaf, JSONObject rootJson) {
        JSONArray children = new JSONArray();
        rootJson.put("children", children);
        LeafChildrenCacheMgr leafChildrenCacheMgr = new LeafChildrenCacheMgr(rootLeaf.getCode());
        Vector<Leaf> childs = leafChildrenCacheMgr.getDirList();
        for (Leaf child : childs) {
            JSONObject jsonChild = new JSONObject();
            jsonChild.put("code", child.getCode());
            jsonChild.put("parentCode", child.getParentCode());
            jsonChild.put("name", child.getName());
            jsonChild.put("layer", child.getLayer());
            jsonChild.put("icon", child.getIcon());
            children.add(jsonChild);
            getTreeAll(child, jsonChild);
        }
    }

}
