package com.redmoon.oa.dept;

import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.*;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.*;

public class DeptView {
    DeptDb rootLeaf;
    Vector UprightLineNodes = new Vector(); //用于显示竖线
    Privilege privilege = new Privilege();
    HttpServletRequest request;
    
    /**
     * 是否打开js-tree的全部节点，如在用户编辑时选择部门的时候
     */
    private boolean isOpenAll = false;
    
    public DeptView(DeptDb rootLeaf) {
        this.rootLeaf = rootLeaf;
    }
   
    public DeptView(HttpServletRequest request, DeptDb rootLeaf) {
        this.rootLeaf = rootLeaf;
        this.request = request;
    }    

    public void list(JspWriter out) throws Exception {
        ListTree(out, rootLeaf, true);
    }

    // 显示根结点为leaf的树
    void ListTree(JspWriter out, DeptDb leaf,
                  boolean isLastChild) throws Exception {
        ShowLeaf(out, leaf, isLastChild);
        DeptMgr dm = new DeptMgr();
        Vector children = dm.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        //写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ListTree(out, childlf, isLastChild1);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeaf(JspWriter out, DeptDb leaf,
                  boolean isLastChild) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

        if (!isLastChild) {
            DeptDb brotherleaf = leaf.getBrother("down");
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) { // =0的也计入的话会在树底端的结点产生多余竖线
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();

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
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed) {
                out.println("<img src='images/spacer.gif' width=20 height=1>");
            }
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            } else { // 不是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            }
        }

        if (leaf.getType() == 2) {
            out.print("<a target=_parent href='dept_user.jsp?deptCode=" +
                      StrUtil.UrlEncode(code) + "'>" + name +
                      "</a>");
        } else if (leaf.getType() == 1) {
            out.print("<a target=_parent href='dept_user.jsp?deptCode=" +
                    StrUtil.UrlEncode(code) + "'>" + name + "</a>");
        } else if (leaf.getType() == 0) {
            out.print(name);
        }

        out.print("     </td><td width='15%' align=right nowrap>");
        if (!"root".equals(leaf.getCode())) {
            out.print(
                    "<a target=dirbottomFrame href='dept_bottom.jsp?parent_code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" +
                    StrUtil.UrlEncode(name, "utf-8") +
                    "&op=AddChild'>添子部门</a>&nbsp;");
            out.print(
                    "<a target='dirbottomFrame' href='dept_bottom.jsp?op=modify&code=" +
                    StrUtil.UrlEncode(code, "utf-8") + "&name=" +
                    StrUtil.UrlEncode(name, "utf-8") + "&description=" +
                    StrUtil.UrlEncode(description, "utf-8") + "'>修改</a>&nbsp;");
            out.print(
                    "<a target=_self href=# onClick=\"if (window.confirm('您确定要删除" +
                    name +
                    "吗?')) window.location.href='dept_top.jsp?op=del&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&delcode=" +
                    StrUtil.UrlEncode(code, "utf-8") + "'\">删除</a>&nbsp;");
            out.print("<a href='dept_top.jsp?op=move&direction=up&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>上移</a>&nbsp;");
            out.print("<a href='dept_top.jsp?op=move&direction=down&code=" +
                      StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;");
        }
        out.println("  </td></tr></tbody></table>");
    }

    void ShowDeptAsOption(JspWriter out, DeptDb leaf, int rootlayer)
                  throws Exception {
        if (!leaf.isShow()) {
        	return;
        }    	
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        String cls = "";
    	if (leaf.getType()==DeptDb.TYPE_UNIT) {
            cls = "class='unit_option'";
        }
        if (leaf.getChildCount()>0) {
            out.print("<option " + cls + " value='" + code + "'>" + blank + "╋ " + name + "</option>");
        }
        else {
            out.print("<option " + cls + " value=\"" + code + "\">" + blank + "├ " + name + "</option>");
        }
    }

    public String getDeptAsOption(DeptDb leaf, int rootlayer) {
    	if (!leaf.isShow()) {
            return "";
        }
    	
        String outStr = "";
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        StringBuilder blank = new StringBuilder();
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank.append("　");
        }
        if (leaf.getChildCount()>0) {
            outStr += "<option value='" + code + "'>" + blank + "╋ " + name + "</option>";
        }
        else {
            outStr += "<option value=\"" + code + "\">" + blank + "├ " + name + "</option>";
        }
        return outStr;
    }

    public String getDeptNameAsOptionValue(DeptDb leaf, int rootlayer) {
        if (!leaf.isShow()) {
        	return "";
        }
        String outStr = "";
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }
        /*
         * option的value设置部门的code @wm 20180801
         */
        if (leaf.getChildCount()>0) {
            outStr += "<option value='" + code + "'>" + blank + "╋ " + name + "</option>";
        }
        else {
            outStr += "<option value=\"" + code + "\">" + blank + "├ " + name + "</option>";
        }
        return outStr;
    }

    // 显示根结点为leaf的树
    public void ShowDeptAsOptions(JspWriter out, DeptDb leaf, int rootlayer) throws Exception {
        ShowDeptAsOption(out, leaf, rootlayer);
        /*DeptMgr dm = new DeptMgr();
        Vector children = dm.getChildren(leaf.getCode());*/
        DeptChildrenCache deptChildrenCache = new DeptChildrenCache(leaf.getCode());
        Vector<DeptDb> children = deptChildrenCache.getDirList();
        int size = children.size();
        if (size == 0) {
            return;
        }

        for (DeptDb childlf : children) {
            ShowDeptAsOptions(out, childlf, rootlayer);
        }
    }

    /**
     * 有些下级组织可能为分类，如丰县党群系统，但是为了层级关系也需显示，但是其值将置为空
     * @param outStr
     * @param leaf
     * @param rootlayer
     * @param isInclude 是否包含本身
     * @return
     * @throws ErrMsgException
     */
    public StringBuffer getDeptAsOptions(StringBuffer outStr, DeptDb leaf, int rootlayer, boolean isInclude) throws ErrMsgException {
        if (isInclude) {
        	outStr.append(getDeptAsOption(leaf, rootlayer));
        }
        
        DeptChildrenCache deptChildrenCache = new DeptChildrenCache(leaf.getCode());
        Vector children = deptChildrenCache.getDirList();
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            if (childlf.isHide()) {
            	continue;
            }
            getDeptAsOptions(outStr, childlf, rootlayer, true);
        }
        return outStr;
    }

    public StringBuffer getDeptAsOptionsOnlyUnit(StringBuffer outStr, DeptDb leaf, int rootlayer) throws ErrMsgException {
        if (DeptDb.TYPE_UNIT == leaf.getType()) {
            outStr.append(getDeptAsOption(leaf, rootlayer));
        }

        DeptMgr dm = new DeptMgr();
        Vector children = dm.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            if (childlf.isHide()) {
                continue;
            }

            getDeptAsOptionsOnlyUnit(outStr, childlf, rootlayer);
        }
        return outStr;
    }
    
    /**
     * 有些下级组织可能为分类，如丰县党群系统，但是为了层级关系也需显示，但是其值将置为空   20180801 wm
     * @param outStr
     * @param leaf
     * @param rootlayer
     * @param isInclude 是否包含本身
     * @return
     * @throws ErrMsgException
     */
    public StringBuffer getDeptAsOptions(StringBuffer outStr, DeptDb leaf, int rootlayer, boolean isInclude, boolean isOnlyChildren) throws ErrMsgException {
        if (isInclude) {
        	outStr.append(getDeptAsOption(leaf, rootlayer));
        }
        if(isOnlyChildren){
        	// 如果是仅仅显示子节点,遍历自己孩子即可
            DeptChildrenCache deptChildrenCache = new DeptChildrenCache(leaf.getCode());
            Vector<DeptDb> children = deptChildrenCache.getDirList();
            int size = children.size();
            if (size == 0) {
                return outStr;
            }

            for (DeptDb childlf : children) {
                if (childlf.isHide()) {
                    continue;
                }
                outStr.append(getDeptNameAsOptionValue(childlf, childlf.getLayer()));
            }
        	return outStr;
        }

        DeptChildrenCache deptChildrenCache = new DeptChildrenCache(leaf.getCode());
        Vector<DeptDb> children = deptChildrenCache.getDirList();
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            if (childlf.isHide()) {
            	continue;
            }
            getDeptAsOptions(outStr, childlf, rootlayer, true);
        }
        return outStr;
    }

    public String getUnitAsOption(DeptDb leaf, int rootlayer) {
    	if (!leaf.isShow()) {
            return "";
        }
    	
    	if (leaf.getType()!=DeptDb.TYPE_UNIT){
    		return "";
    	}
    	
    	// 如果其父节点不是单位，则不显示
    	if (false && !leaf.getCode().equals(DeptDb.ROOTCODE)) {
    		DeptDb dd = new DeptDb();
    		dd = dd.getDeptDb(leaf.getParentCode());
    		if (dd.getType()!=DeptDb.TYPE_UNIT) {
    			return "";
    		}
    	}
    	
    	// 如果其孩子节点数为0，且不是单位，则不显示
    	if (leaf.getChildCount()==0) {
    		if (leaf.getType()!=DeptDb.TYPE_UNIT) {
    			return "";
    		}
    	}
    	
        String outStr = "";
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer-rootlayer;
        for (int i=0; i<d; i++) {
            blank += "　";
        }

        String cls = "", val = "";
        if (leaf.getType()==DeptDb.TYPE_UNIT) {
            cls = " class='unit' ";
            val = code;
        }
        else {
        	val = "";
        }
        
        if (leaf.getChildCount()>0) {
            outStr += "<option " + cls + " value='" + val + "'>" + blank + "╋ " + name + "</option>";
        }
        else {        	
            outStr += "<option " + cls + " value=\"" + val + "\">" + blank + "├ " + name + "</option>";
        }
        return outStr;
    }    
    
    /**
     * 取出用于select的单位列表
     * @param outStr
     * @param leaf
     * @param rootlayer
     * @return
     * @throws ErrMsgException
     */
    public StringBuffer getUnitAsOptions(StringBuffer outStr, DeptDb leaf, int rootlayer) throws ErrMsgException {
    	// 不是集团版，则返回空
        if (!License.getInstance().isPlatformGroup()) {
            return new StringBuffer();
        }
    	
        outStr.append(getUnitAsOption(leaf, rootlayer));
        DeptMgr dm = new DeptMgr();
        Vector children = dm.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
           	getUnitAsOptions(outStr, childlf, rootlayer);
        }
        return outStr;
    }    
    
    
    
    /**
     * 取得用户具有权限的部门
     * @param request
     * @param outStr
     * @param leaf
     * @param rootlayer
     * @return
     * @throws ErrMsgException
     */
    public StringBuffer getUserAdminDeptAsOptions(HttpServletRequest request, StringBuffer outStr, DeptDb leaf, int rootlayer) throws ErrMsgException {
        if (privilege.canAdminDept(request, leaf.getCode())) {
            outStr.append(getDeptAsOption(leaf, rootlayer));
        }
        DeptMgr dm = new DeptMgr();
        Vector children = dm.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            if (childlf.isHide()) {
            	continue;
            }
            getUserAdminDeptAsOptions(request, outStr, childlf, rootlayer);
        }
        return outStr;
    }    

    /**
     * 取得用户具有权限的部门   20180801 wm
     * @param request
     * @param outStr
     * @param leaf
     * @param rootlayer
     * @return
     * @throws ErrMsgException
     */
    public StringBuffer getUserAdminDeptAsOptions(HttpServletRequest request, StringBuffer outStr, DeptDb leaf, int rootlayer, boolean isOnlyChildren) throws ErrMsgException {
        if (privilege.canAdminDept(request, leaf.getCode())){
        	outStr.append(getDeptAsOption(leaf, rootlayer));
        }
        if(isOnlyChildren){
            DeptChildrenCache deptChildrenCache = new DeptChildrenCache(leaf.getCode());
            Vector<DeptDb> children = deptChildrenCache.getDirList();
            int size = children.size();
            if (size == 0) {
                return outStr;
            }

            Iterator ri = children.iterator();
            while (ri.hasNext()) {
                DeptDb childlf = (DeptDb) ri.next();
                if (childlf.isHide()) {
                	continue;
                }
                //getUserAdminDeptAsOptions(request, outStr, childlf, rootlayer);
                outStr.append(getDeptNameAsOptionValue(childlf, childlf.getLayer()));
            }
        	return outStr;
        }

        DeptChildrenCache deptChildrenCache = new DeptChildrenCache(leaf.getCode());
        Vector<DeptDb> children = deptChildrenCache.getDirList();
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            if (childlf.isHide()) {
            	continue;
            }
            getUserAdminDeptAsOptions(request, outStr, childlf, rootlayer);
        }
        return outStr;
    }    

    public StringBuffer getDeptNameAsOptions(StringBuffer outStr, DeptDb leaf, int rootlayer) throws ErrMsgException {
        outStr.append(getDeptNameAsOptionValue(leaf, rootlayer));
        DeptMgr dm = new DeptMgr();
        Vector children = dm.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        int i = 0;
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            getDeptNameAsOptions(outStr, childlf, rootlayer);
        }
        return outStr;
    }

    public void ListSimple(HttpServletRequest request, JspWriter out, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());

        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");
        ListTreeSimple(depts, out, rootLeaf, true, target, link, tableClass, tableClassMouseOn);
    }

    // 显示根结点为leaf的树
    void ListTreeSimple(String[] depts, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafSimple(depts, out, leaf, isLastChild, target, link, tableClass, tableClassMouseOn);
        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ListTreeSimple(depts, out, childlf, isLastChild1, target, link, tableClass, tableClassMouseOn);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSimple(String[] depts, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        // String description = leaf.getDescription();

        if (!isLastChild) {
            DeptDb brotherleaf = leaf.getBrother("down");
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) {
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();

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
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed) {
                out.println("<img src='images/spacer.gif' width=20 height=1 style='visibility:hidden'>");
            }
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img tableRelate='' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            } else { // 不是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            }
        }

        Config cfg = new Config();
        boolean isRestricted = true;
        if ("true".equals(cfg.get("restrictUserDept"))) {
            if (depts!=null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    if (depts[i].equals(code)) {
                        isRestricted = false;
                        break;
                    }
                }
            }
            else {
                isRestricted = false;
            }
        }
        else {
            isRestricted = false;
        }

        if (isRestricted) {
            out.print(
                    "<font color='#888888'>" + name + "</font>");
        }
        else {
            // 三种类型节点，用同一个Link
            out.print(
                    "<a target='" + target + "' href='" + link + "?deptCode=" +
                    StrUtil.UrlEncode(code) + "'>" + name + "</a>");
        }

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    public void ListFuncAjax(HttpServletRequest request, JspWriter out, String target, String func, String tableClass, String tableClassMouseOn, boolean isShowRoot) throws Exception {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");        
        ListTreeFuncAjax(depts, privilege, out, rootLeaf, true, target, func, tableClass, tableClassMouseOn, isShowRoot);
    }

    public void ListFuncAjaxStyle(HttpServletRequest request, JspWriter out, String target, String func, String tableClass, String tableClassMouseOn, boolean isShowRoot) throws Exception {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");        
        ListTreeFuncAjaxStyle(depts, privilege, out, rootLeaf, true, target, func, tableClass, tableClassMouseOn, isShowRoot);
    }

    // 显示根结点为leaf的树
    void ListTreeFuncAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                        boolean isLastChild, String target, String func,
                        String tableClass, String tableClassMouseOn, boolean isShowRoot) throws
            Exception {
    	if (isShowRoot) {
            ShowLeafFuncAjax(depts, privilege, out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
        }

        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ShowLeafFuncAjax(depts, privilege, out, childlf, isLastChild, target, func, tableClass,
                           tableClassMouseOn);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }    

 // 显示根结点为leaf的树
    void ListTreeFuncAjaxStyle(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
            boolean isLastChild, String target, String func,
            String tableClass, String tableClassMouseOn, boolean isShowRoot) throws
        Exception {
		if (isShowRoot) {
            ShowLeafFuncAjaxStyle(depts, privilege, out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
        }
		
		DeptMgr dir = new DeptMgr();
		Vector children = dir.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0) {
            return;
        }
		
		int i = 0;
		if (size > 0) {
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
		Iterator ri = children.iterator();
		// 写跟贴
		while (ri.hasNext()) {
		i++;
		DeptDb childlf = (DeptDb) ri.next();
		boolean isLastChild1 = true;
		if (size != i) {
            isLastChild1 = false;
        }
		ShowLeafFuncAjaxStyle(depts, privilege, out, childlf, isLastChild, target, func, tableClass,
		               tableClassMouseOn);
		}
		if (size > 0) {
            out.print("</td></tr></table>");
        }
	}   

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafFuncAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        if (!leaf.isShow()) {
        	return;
        } 
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
        if (childcount==0) {
            padWidth += 16;
        }
        out.print("<img src='images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0) {
                out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus.gif' align='absmiddle'><img src='images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='images/folder_01.gif' align='absmiddle'>");
            } else {
                out.println("<img src='images/folder_01.gif' align='absmiddle'>");
            }
        }
        
        Config cfg = new Config();
        boolean isRestricted = true;
        if ("true".equals(cfg.get("restrictUserDept"))) {
            if (depts!=null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    if (depts[i].equals(code)) {
                        isRestricted = false;
                        break;
                    }
                }
            }
            else {
                isRestricted = false;
            }
        }
        else {
            isRestricted = false;
        }
        if (isRestricted) {
            out.print(
                    "<font color='#888888'>" + name + "</font>");
        } else {
            out.print(
                    "<a target='" + target + "' href='#' onClick=\"" + func + "('" +
                    code + "')\">" + name + "</a>");
        }

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafFuncAjaxStyle(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
            boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
	  if (!leaf.isShow()) {
	  	return;
	  } 
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
	  if (childcount==0) {
          padWidth += 16;
      }
	  out.print("<img src='images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");
	
	  if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
	      out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_root_style.gif' align='absmiddle'><img src='images/folder_03.jpg' align='absmiddle'>");
	  } else {
	      if (childcount > 0) {
              out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus_style.jpg' align='absmiddle'><img src='images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='images/folder_03.jpg' align='absmiddle'>");
          } else {
              out.println("<img src='images/folder_03.jpg' align='absmiddle'>");
          }
	  }
	  
	  Config cfg = new Config();
	  boolean isRestricted = true;
	  if ("true".equals(cfg.get("restrictUserDept"))) {
	      if (depts!=null) {
	          int len = depts.length;
	          for (int i = 0; i < len; i++) {
	              if (depts[i].equals(code)) {
	                  isRestricted = false;
	                  break;
	              }
	          }
	      }
	      else {
              isRestricted = false;
          }
	  }
	  else {
          isRestricted = false;
      }
	  if (isRestricted) {
	      out.print(
	              "<font color='#888888'>" + name + "</font>");
	  } else {
	      out.print(
	              "<a target='" + target + "' href='#' onClick=\"" + func + "('" +
	              code + "')\">" + name + "</a>");
	  }
	
	  out.print("     </td>");
	  out.println("  </tr></tbody></table>");
	}
    
    public void ListFunc(HttpServletRequest request, JspWriter out,
                         String target, String func, String tableClass,
                         String tableClassMouseOn) throws Exception {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");
        ListTreeFunc(depts, out, rootLeaf, true, target, func, tableClass, tableClassMouseOn);
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
    void ListTreeFunc(String[] depts, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafFunc(depts, out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ListTreeFunc(depts, out, childlf, isLastChild1, target, func, tableClass, tableClassMouseOn);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }

    void ShowLeafFunc(String[] depts, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        // String description = leaf.getDescription();

        if (!isLastChild) {
            DeptDb brotherleaf = leaf.getBrother("down");
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) {
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();

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
            if (!isShowed) {
                out.println("<img src='images/spacer.gif' width=20 height=1>");
            }
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            } else { // 不是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            }
        }

        Config cfg = new Config();
        boolean isRestricted = true;
        if ("true".equals(cfg.get("restrictUserDept"))) {
            if (depts!=null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    if (depts[i].equals(code)) {
                        isRestricted = false;
                        break;
                    }
                }
            }
            else {
                isRestricted = false;
            }
        }
        else {
            isRestricted = false;
        }
        if (isRestricted) {
            out.print("<font color='#888888'>" + name + "</font>");
        } else {
            out.print("<a target='" + target + "' href='#' onClick=\"" + func + "('" +
                    code + "')\">" + name + "</a>");
        }

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    public void ListFuncWithCheckboxAjax(HttpServletRequest request, JspWriter out, String target, String func, String tableClass, String tableClassMouseOn, boolean isShowRoot, boolean isOnlyUnitCheckable) throws Exception {
        this.request = request;
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");        
        ListTreeFuncWithCheckboxAjax(depts, privilege, out, rootLeaf, true, target, func, tableClass, tableClassMouseOn, isShowRoot, isOnlyUnitCheckable);
    }
    
    public void ListFuncWithCheckboxAjax(HttpServletRequest request, JspWriter out, String target, String func, String tableClass, String tableClassMouseOn, boolean isShowRoot) throws Exception {
    	ListFuncWithCheckboxAjax(request, out, target, func, tableClass, tableClassMouseOn, isShowRoot, false);
    }

    // 显示根结点为leaf的树
    void ListTreeFuncWithCheckboxAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                        boolean isLastChild, String target, String func,
                        String tableClass, String tableClassMouseOn, boolean isShowRoot, boolean isOnlyUnitCheckable) throws
            Exception {
    	if (isShowRoot) {
            ShowLeafFuncWithCheckboxAjax(depts, privilege, out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn, isOnlyUnitCheckable);
        }

        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ShowLeafFuncWithCheckboxAjax(depts, privilege, out, childlf, isLastChild, target, func, tableClass,
                           tableClassMouseOn, isOnlyUnitCheckable);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }    

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafFuncWithCheckboxAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn, boolean isOnlyUnitCheckable) throws Exception {
        if (!leaf.isShow() || leaf.isHide()) {
        	return;
        }
        
    	String rootPath = request.getContextPath();
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
        if (childcount==0) {
            padWidth += 16;
        }
        out.print("<img src='" + request.getContextPath() + "/images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/images/i_puls-root.gif' align='absmiddle'><img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0) {
                out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/images/i_plus.gif' align='absmiddle'><img src='" + request.getContextPath() + "/images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
            } else {
                out.println("<img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
            }
        }
        
        String fullDeptName = "";
        DeptMgr dm = new DeptMgr();
		if (!leaf.getParentCode().equals(DeptDb.ROOTCODE) && !leaf.getCode().equals(DeptDb.ROOTCODE)) {
			fullDeptName = dm.getDeptDb(leaf.getParentCode()).getName() + "->" + leaf.getName();
		}
		else {
            fullDeptName = leaf.getName();
        }
		
        // if (!code.equals(DeptDb.ROOTCODE))
		String disabledStr = "";
		if (isOnlyUnitCheckable) {
			if (leaf.getType()!=DeptDb.TYPE_UNIT) {
				disabledStr = "disabled";
			}
		}
		
		// 20140223 用于ajax获取子部门时，仅对当前展开的子部门中原先如选中的打勾，因为可能先展开了其它的部门，去掉了其子部门的勾选
        out.print("<input " + disabledStr + " type='checkbox' id='chk_" + code + "' name='" + code + "' value='" + name + "' fullName='" + fullDeptName + "' parentCode='" + leaf.getParentCode() + "' />&nbsp;");        

        Config cfg = new Config();
        boolean isRestricted = true;
        if ("true".equals(cfg.get("restrictUserDept"))) {
            if (depts!=null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    if (depts[i].equals(code)) {
                        isRestricted = false;
                        break;
                    }
                }
            }
            else {
                isRestricted = false;
            }
        }
        else {
            isRestricted = false;
        }
        if (isRestricted) {
            out.print("<font color='#888888'>" + name + "</font>");
        } else {
        	String cls = "";
        	if (leaf.getType()==DeptDb.TYPE_UNIT) {
                cls = "class='unit'";
            }
            out.print("<a " + cls + " target='" + target + "' href='javascript:;' onClick=\"" + func + "('" +
                    code + "')\">" + name + "</a>");
        }

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }

    public void ListUnitFuncWithCheckboxAjax(HttpServletRequest request, JspWriter out, String target, String func, String tableClass, String tableClassMouseOn, boolean isShowRoot, boolean isOnlyUnitCheckable) throws Exception {
        this.request = request;
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");        
        ListUnitTreeFuncWithCheckboxAjax(depts, privilege, out, rootLeaf, true, target, func, tableClass, tableClassMouseOn, isShowRoot, isOnlyUnitCheckable);
    }

    // 显示根结点为leaf的树
    void ListUnitTreeFuncWithCheckboxAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                        boolean isLastChild, String target, String func,
                        String tableClass, String tableClassMouseOn, boolean isShowRoot, boolean isOnlyUnitCheckable) throws
            Exception {
    	if (isShowRoot) {
            ShowUnitLeafFuncWithCheckboxAjax(depts, privilege, out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn, isOnlyUnitCheckable);
        }

        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ShowUnitLeafFuncWithCheckboxAjax(depts, privilege, out, childlf, isLastChild, target, func, tableClass,
                           tableClassMouseOn, isOnlyUnitCheckable);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }    

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowUnitLeafFuncWithCheckboxAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn, boolean isOnlyUnitCheckable) throws Exception {
        if (!leaf.isShow()) {
        	return;
        }
        
        if (leaf.getType()!=DeptDb.TYPE_UNIT) {
        	if (leaf.getChildCount()==0) {
        		return;
        	}
        	else {
        		// 提取子部门，检查是否有单位，如果没有，则不显示
        		boolean isShow = false;
        		DeptChildrenCache dl = new DeptChildrenCache(leaf.getCode());
        		java.util.Vector v = dl.getDirList();
        		Iterator ir1 = v.iterator();
        		while (ir1.hasNext()) {
        			DeptDb lf = (DeptDb) ir1.next();
        			if (lf.getType()==DeptDb.TYPE_UNIT) {
        				isShow = true;
        				break;
        			}
        		}
        		if (!isShow) {
        			return;
        		}
        	}
        }
        
    	String rootPath = request.getContextPath();
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
        if (childcount==0) {
            padWidth += 16;
        }
        out.print("<img src='" + request.getContextPath() + "/images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/images/i_puls-root.gif' align='absmiddle'><img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0) {
                out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/images/i_plus.gif' align='absmiddle'><img src='" + request.getContextPath() + "/images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
            } else {
                out.println("<img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
            }
        }
        
        String fullDeptName = "";
        DeptMgr dm = new DeptMgr();
		if (!leaf.getParentCode().equals(DeptDb.ROOTCODE) && !leaf.getCode().equals(DeptDb.ROOTCODE)) {
			fullDeptName = dm.getDeptDb(leaf.getParentCode()).getName() + "->" + leaf.getName();
		}
		else {
            fullDeptName = leaf.getName();
        }
		
        // if (!code.equals(DeptDb.ROOTCODE))
		String disabledStr = "";
		if (isOnlyUnitCheckable) {
			if (leaf.getType()!=DeptDb.TYPE_UNIT) {
				disabledStr = "isUnit='false'";
			}
			else {
				disabledStr = "isUnit='true'";
			}
		}

        out.print("<input " + disabledStr + " type='checkbox' id='chk_" + code + "' name='" + code + "' value='" + name + "' fullName='" + fullDeptName + "'onClick=\"" + func + "('" +
                    code + "')\" parentCode='" + leaf.getParentCode() + "' />&nbsp;");        

        Config cfg = new Config();
        boolean isRestricted = true;
        if ("true".equals(cfg.get("restrictUserDept"))) {
            if (depts!=null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    if (depts[i].equals(code)) {
                        isRestricted = false;
                        break;
                    }
                }
            }
            else {
                isRestricted = false;
            }
        }
        else {
            isRestricted = false;
        }
        if (isRestricted) {
            out.print(
                    "<font color='#888888'>" + name + "</font>");
        } else {
        	String cls = "";
        	if (leaf.getType()==DeptDb.TYPE_UNIT) {
                cls = "class='unit'";
            }
        	/*
            out.print(
                    "<a " + cls + " target='" + target + "' href='javascript:;' onClick=\"" + func + "('" +
                    code + "')\">" + name + "</a>");
            */
            out.print("<a " + cls + " href='javascript:;' >" + name + "</a>");        	
        }

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
    void ListTreeFuncWithCheckbox(JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        ShowLeafFuncWithCheckbox(out, leaf, isLastChild, target, func, tableClass, tableClassMouseOn);
        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childoftable" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ListTreeFuncWithCheckbox(out, childlf, isLastChild1, target, func, tableClass, tableClassMouseOn);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }

    void ShowLeafFuncWithCheckbox(JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String func, String tableClass, String tableClassMouseOn) throws Exception {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

        if (!isLastChild) {
            DeptDb brotherleaf = leaf.getBrother("down");
            // 如果兄弟结点存在
            if (brotherleaf != null) {
                // 取其所有的孩子结点
                Vector r = new Vector();
                leaf.getAllChild(r, leaf);
                int count = r.size();
                if (count>0) {
                    UprightLineNode uln = new UprightLineNode(layer, count);
                    UprightLineNodes.addElement(uln);
                }
            }
        }

        int childcount = leaf.getChildCount();

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
                    }
                    isShowed = true;
                    break;
                }
            }
            if (!isShowed) {
                out.println("<img src='images/spacer.gif' width=20 height=1>");
            }
        }

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (isLastChild) { // 是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-3.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            } else { // 不是最后一个孩子结点
                if (childcount > 0) {
                    out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus2-1.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                } else {
                    out.println("<img src='images/i_plus-2-2.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
                }
            }
        }

        // checkbox
        if (!code.equals(DeptDb.ROOTCODE)) {
            out.print("<input type='checkbox' name='" + code + "' value='" + name + "'>&nbsp;");
        }
        // 三种类型节点，用同一个Link
        out.print(
                "<a target='" + target + "' href='#' onClick=\"" + func + "('" + code + "')\">" + name + "</a>");

        out.print("     </td>");
        out.println("  </tr></tbody></table>");
    }
    
    
    
    public void ListSimpleAjax(HttpServletRequest request, JspWriter out, String target, String link, String tableClass, String tableClassMouseOn, boolean isShowRoot) throws Exception {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");        
        ListTreeSimpleAjax(request, depts, privilege, out, rootLeaf, true, target, link, tableClass, tableClassMouseOn, isShowRoot);
    }    

    // 显示根结点为leaf的树
    void ListTreeSimpleAjax(HttpServletRequest request, String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                        boolean isLastChild, String target, String link,
                        String tableClass, String tableClassMouseOn, boolean isShowRoot) throws
            Exception {
        if (isShowRoot) {
            ShowLeafSimpleAjax(request, depts, privilege, out, leaf, isLastChild, target, link, tableClass,
                           tableClassMouseOn);
        }

        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ShowLeafSimpleAjax(request, depts, privilege, out, childlf, isLastChild, target, link, tableClass,
                           tableClassMouseOn);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSimpleAjax(HttpServletRequest request, String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                  boolean isLastChild, String target, String link, String tableClass, String tableClassMouseOn) throws Exception {
        if (!leaf.isShow()) {
        	return;
        }
    	String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();

        int childcount = leaf.getChildCount();
        
        String rootPath = request.getContextPath() + "/admin";

        String tableid = leaf.getCode();

        out.println("<table id=" + tableid + " name=" + tableid + " class='" + tableClass + "' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='" + tableClassMouseOn + "'\" onMouseOut=\"this.className='" + tableClass + "'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td height='20' align=left nowrap>");

        int padWidth = 0;
        for (int k = 1; k <= layer - 1; k++) {
            padWidth += 21;
        }
        if (childcount==0) {
            padWidth += 16;
        }
        out.print("<img src='images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/images/i_puls-root.gif' align='absmiddle'><img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0) {
                out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='" + rootPath + "/images/i_plus.gif' align='absmiddle'><img src='images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
            } else {
                out.println("<img src='" + rootPath + "/images/folder_01.gif' align='absmiddle'>");
            }
        }

        // 三种类型节点，用同一个Link
        Config cfg = new Config();
        boolean isRestricted = true;
        if ("true".equals(cfg.get("restrictUserDept"))) {
            if (depts!=null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    if (depts[i].equals(code)) {
                        isRestricted = false;
                        break;
                    }
                }
            }
            else {
                isRestricted = false;
            }
        }
        else {
            isRestricted = false;
        }

        String className = "";
        if (leaf.getType()==DeptDb.TYPE_UNIT) {
            className = "class='unit'";
        }
        
        if (isRestricted) {
            out.print(
                    "<font color='#888888'>" + name + "</font>");
        }
        else {
        	String t = "?";
        	if (link.indexOf("?")!=-1) {
                t = "&";
            }
            out.print("<a " + className+ " target='" + target + "' href='" + link + t + "deptCode=" +
                    StrUtil.UrlEncode(code) + "'>" + name + "</a>");
        }
        out.print("     </td>");
        out.println("  </tr></tbody></table>");

    }
    

    public void listAjax(HttpServletRequest request, JspWriter out, boolean isShowRoot) throws Exception {
        Privilege privilege = new Privilege();
        ListTreeAjax(request, privilege, out, rootLeaf, true, isShowRoot);
    }

    // 显示根结点为leaf的树
    void ListTreeAjax(HttpServletRequest request, Privilege privilege, JspWriter out, DeptDb leaf,
                  boolean isLastChild, boolean isShowRoot) throws Exception {
        if (isShowRoot) {
            ShowLeafAjax(request, privilege, out, leaf, isLastChild, isShowRoot);
        }

        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
        	String style = "";
            if (!"root".equals(leaf.getCode())) {
				style = "style='display:'";// 设置display为none将不会显示其子节点
			}
			out.print("<table id='childof" + leaf.getCode() + "' " + style + " cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            boolean isLastChild1 = true;
            if (size != i) {
                isLastChild1 = false;
            }
            ShowLeafAjax(request, privilege, out, childlf, isLastChild1, isShowRoot);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }    
    

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafAjax(HttpServletRequest request, Privilege privilege, JspWriter out, DeptDb leaf,
                  boolean isLastChild, boolean isShowRoot) throws Exception {
        String code = leaf.getCode();

        String name = leaf.getName();
        int layer = leaf.getLayer();
        String description = leaf.getDescription();

        int childcount = leaf.getChildCount();

        String tableid = leaf.getCode();

        String style = "";
        /*
        if (!leaf.getCode().equals("root")) {
			style = "style='display:none'";
		}
		*/
        out.println("<table id=" + tableid + " name=" + tableid + " " + style + " class='tbg1' cellspacing=0 cellpadding=0 width='100%' align=center onMouseOver=\"this.className='tbg1sel'\" onMouseOut=\"this.className='tbg1'\" border=0>");
        out.println("    <tbody><tr>");
        out.println("        <td height='13' align=left onmouseover='showModify(this)' onmouseout='hiddenModify(this)' nowrap>");

        out.println("<span>");
        int padWidth = 0;
        for (int k = 1; k <= layer - 1; k++) {
            padWidth += 21;
        }
        if (childcount==0) {
            padWidth += 16;
        }
        out.print("<img src='" + request.getContextPath() + "/images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img style='cursor:pointer' tableRelate='" + tableid + "' onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0) {
                out.println("<img style='cursor:pointer;margin-right:3px' tableRelate='" + tableid +
                            "' onClick=\"ShowChild(this, '" + leaf.getCode() + "')\" src='images/i_plus.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
            } else {
                out.println(
                        "<img src='images/folder_01.gif' align='absmiddle'>");
            }
        }
        
        String className = "";
        if (leaf.getType()==DeptDb.TYPE_UNIT) {
            className = "unit";
        }
        
        String spanClsName = "";
        if (!leaf.isShow()) {
        	spanClsName = "deptNodeHidden";
        }
        out.print("<a class='" + className + "' target=_parent href='dept_user.jsp?deptCode=" + StrUtil.UrlEncode(code) + "'><span class='" + spanClsName + "'>" + name + "</span></a>");        
        
        out.print("</span>");
		out.print("<span style='padding-left:20px;display:none'>");

		if (childcount > 0) {
            out.print("<a href=\"javascript:ShowChild(o('" + tableid + "').getElementsByTagName('img')[1], '" + leaf.getCode() + "')\">伸缩</a>&nbsp;");
        }

		if (!"root".equals(leaf.getCode())) {
			String root_code = (String) request.getAttribute("root_code");
			if (root_code == null) {
                root_code = rootLeaf.getCode();
            }

			if (leaf.getIsHome()) {
                out.print(SkinUtil.LoadString(request, "res.cms.DirectoryView", "info_home") + "&nbsp;");
            }
			out.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirbottomFrame' href='dept_bottom.jsp?parent_code=" + StrUtil.UrlEncode(code, "utf-8") + "&parent_name=" + StrUtil.UrlEncode(name, "utf-8") + "&op=AddChild'>添子部门</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirbottomFrame' href='dept_bottom.jsp?op=modify&code=" + StrUtil.UrlEncode(code, "utf-8") + "&name=" + StrUtil.UrlEncode(name, "utf-8") + "&description=" + StrUtil.UrlEncode(description, "utf-8") + "'>修改</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirhidFrame' onClick=\"return window.confirm('您确定要删除" + name + "吗?')\" href=\"dept_do.jsp?op=del&root_code=" + StrUtil.UrlEncode(rootLeaf.getCode()) + "&delcode=" + StrUtil.UrlEncode(code, "utf-8") + "\">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirhidFrame' href='dept_do.jsp?op=move&direction=up&code=" + StrUtil.UrlEncode(code, "utf-8") + "'>上移</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.print("<a target='dirhidFrame' href='dept_do.jsp?op=move&direction=down&code=" + StrUtil.UrlEncode(code, "utf-8") + "'>下移</a>&nbsp;&nbsp;&nbsp;&nbsp;");

		}
		out.print("</span>");
        out.print("  </td></tr></tbody></table>");
    }

    /**
     * 选择某个节点
     * @param out JspWriter
     * @param tableClass String
     * @param tableClassMouseOn String
     * @param isShowRoot boolean
     * @throws Exception
     */
    public void SelectSingleAjax(JspWriter out, String func, String tableClass, String tableClassMouseOn, boolean isShowRoot) throws Exception {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        String strDepts = userSetup.getMessageToDept();
        String[] depts = StrUtil.split(strDepts, ",");         
        SelectTreeSingleAjax(depts, privilege, out, rootLeaf, func, tableClass, tableClassMouseOn, isShowRoot);
    }

    void SelectTreeSingleAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                        String func,
                        String tableClass, String tableClassMouseOn, boolean isShowRoot) throws
            Exception {
        if (isShowRoot) {
            ShowLeafSingleAjax(depts, privilege, out, leaf, func, tableClass,
                           tableClassMouseOn);
        }

        DeptMgr dir = new DeptMgr();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        int i = 0;
        if (size > 0) {
            out.print("<table id='childof" + leaf.getCode() +
                    "' cellspacing=0 cellpadding=0 width='100%' align=center><tr><td>");
        }
        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            i++;
            DeptDb childlf = (DeptDb) ri.next();
            ShowLeafSingleAjax(depts, privilege, out, childlf, func, tableClass,
                           tableClassMouseOn);
        }
        if (size > 0) {
            out.print("</td></tr></table>");
        }
    }

    /**
     * isLastChild 是否为其父亲结点的最后一个孩子结点
     **/
    void ShowLeafSingleAjax(String[] depts, Privilege privilege, JspWriter out, DeptDb leaf,
                  String func, String tableClass, String tableClassMouseOn) throws Exception {
        if (!leaf.isShow()) {
        	return;
        }    	
    	
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
        if (childcount==0) {
            padWidth += 16;
        }
        out.print("<img src='images/spacer.gif' width=" + padWidth + " height=1 style='visibility:hidden'>");

        if (leaf.getCode().equals(rootLeaf.getCode())) { // 如果是根结点
            out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_puls-root.gif' align='absmiddle'><img src='images/folder_01.gif' align='absmiddle'>");
        } else {
            if (childcount > 0) {
                out.println("<img onClick=\"ShowChild(this, '" + tableid + "')\" src='images/i_plus.gif' align='absmiddle'><img src='images/spacer.gif' width=3px height=1 style='visibility:hidden'><img src='images/folder_01.gif' align='absmiddle'>");
            } else {
                out.println("<img src='images/folder_01.gif' align='absmiddle'>");
            }
        }

        // 三种类型节点，用同一个Link
        Config cfg = new Config();
        boolean isRestricted = true;
        if ("true".equals(cfg.get("restrictUserDept"))) {
            if (depts!=null) {
                int len = depts.length;
                for (int i = 0; i < len; i++) {
                    if (depts[i].equals(code)) {
                        isRestricted = false;
                        break;
                    }
                }
            }
            else {
                isRestricted = false;
            }
        }
        else {
            isRestricted = false;
        }

        if (isRestricted) {
            out.print(
                    "<font color='#888888'>" + name + "</font>");
        }
        else {        
        	out.print("<a href=\"javascript:" + func + "('" + code + "','" + name + "')\">" + name + "</a>");
        }
        out.print("     </td>");
        out.println("  </tr></tbody></table>");

    }
    
    public List<String> getAllUnit() throws Exception {
    	List<String> list = new ArrayList<String>();
    	DeptMgr dir = new DeptMgr();
    	return this.getEachUnit(dir,"-1",list); 
    }
    
    private List<String> getEachUnit(DeptMgr dir, String parentCode,List<String> list) throws Exception {
    	Vector children = dir.getChildren(parentCode);
    	Iterator ri = children.iterator();
    	while (ri.hasNext()) {
    		DeptDb childlf = (DeptDb) ri.next();
    		if(childlf.getType() == DeptDb.TYPE_UNIT){
    			list.add(childlf.getCode());
    		}
    		Vector childs = dir.getChildren(childlf.getCode());
    		if(!childs.isEmpty()){
    			Iterator childri = childs.iterator();
        		while (childri.hasNext()) {
        			DeptDb child = (DeptDb) childri.next();
        			if(child.getType() == DeptDb.TYPE_UNIT){
            			list.add(child.getCode());
            		}
        			Vector ch = dir.getChildren(child.getCode());
        			if(!ch.isEmpty()){
        				this.getEachUnit(dir ,child.getCode(),list);
        			}
        		}
    		}
    	}
    	return list;
	}
    
    public List<String> getAllUnShow() throws Exception {
    	List<String> list = new ArrayList<String>();
    	DeptMgr dir = new DeptMgr();
    	return this.getEachUnShow(dir,"-1",list); 
    }
    
    private List<String> getEachUnShow(DeptMgr dir, String parentCode,List<String> list) throws Exception {
    	Vector children = dir.getChildren(parentCode);
    	Iterator ri = children.iterator();
    	while (ri.hasNext()) {
    		DeptDb childlf = (DeptDb) ri.next();
    		if(!childlf.isShow()){
    			list.add(childlf.getCode());
    		}
    		Vector childs = dir.getChildren(childlf.getCode());
    		if(!childs.isEmpty()){
    			Iterator childri = childs.iterator();
        		while (childri.hasNext()) {
        			DeptDb child = (DeptDb) childri.next();
        			if(!child.isShow()){
            			list.add(child.getCode());
            		}
        			Vector ch = dir.getChildren(child.getCode());
        			if(!ch.isEmpty()){
        				this.getEachUnShow(dir ,child.getCode(),list);
        			}
        		}
    		}
    	}
    	return list;
	}

	public String getJsonString() throws Exception {
		return getJsonString(false);
    }
	
	public String getJsonString(boolean isOpenAll) {
		return getJsonString(isOpenAll, false);
	}
	
	public String getJsonString(boolean isOpenAll, boolean isShowNodeHided) {
		this.isOpenAll = isOpenAll;
    	DeptMgr dir = new DeptMgr();
    	String str = "[";
    	// 从根开始  
        try {
            str = getJson(dir, "-1", str, isShowNodeHided);
            str = str.substring(0, str.length() - 1);
            str += "]";
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }

    	return str;
	}	
	
	public String getJsonStringUser() throws Exception {
		DeptMgr dir = new DeptMgr();
    	String str = "[";  
    	str = this.getJsonDept(dir,"-1",str);  
    	str += this.getJsonUser();
    	str = str.substring(0,str.length()-1);
    	str += "]";  
    	return str;
	}
	
	public String getJsonStringByDept(Vector depts) throws Exception {
		return getJsonStringByDept(depts, false);
	}
	
	public String getJsonStringByDept(Vector depts, boolean isShowNodeHided) throws Exception {
    	DeptMgr dir = new DeptMgr();
    	DeptDb dd = new DeptDb(DeptDb.ROOTCODE);
		String str = "[{id:\"" + dd.getCode() + "\",parent:\"#\",text:\""
				+ dd.getName().replaceAll("\"", "\\\\\"") + "\",state:{opened:true}} ,";
    	// 从根开始  
    	if (!depts.isEmpty()) {
	    	ArrayList<String> list = new ArrayList<String>();
	    	this.getJsonByDept(dir, DeptDb.ROOTCODE, depts, list);
	    	for (String node : list) {
				if (node.equals(DeptDb.ROOTCODE)) {
					continue;
				}
				DeptDb childlf = new DeptDb(node);
				if (childlf.isHide()) {
					if (!isShowNodeHided) {
						continue;
					}
				}
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"") + "\" },";
			}
    	} else {
    		str = this.getJson(dir,"-1",str, isShowNodeHided);
    	}
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
	private String getJson(DeptMgr dir, String parentCode, String str, boolean isShowNodeHided)
			throws Exception {
		String strStateOpened = "";
		if (isOpenAll) {
			strStateOpened = ", state:{opened:true}";
		}
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			DeptDb childlf = (DeptDb) ri.next();
			if (childlf.isHide()) {
				if (!isShowNodeHided) {
					continue;
				}
			}
			if ("-1".equals(parentCode)) {
				if (str.indexOf("{id:\"" + childlf.getCode()) == -1) {
					str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
					+ childlf.getName() + "\",state:{opened:true}} ,";
				}
			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName() + "\"" + strStateOpened + " },";
			}
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					DeptDb child = (DeptDb) childri.next();
					if (child.isHide()) {
						if (!isShowNodeHided) {
							continue;
						}
					}					
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName() + "\"" + strStateOpened + " },";
					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = this.getJson(dir, child.getCode(), str, isShowNodeHided);
					}
				}
			}
		}
		return str;
	}
	
	
	/**
	 * 递归获得jsTree的json字符串
	 *   最新calendar使用
	 * @param parentCode
	 *            父节点parentCode
	 * @return str
	 */
	private String getJsonDept(DeptMgr dir, String parentCode, String str)
			throws Exception {
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			i++;
			DeptDb childlf = (DeptDb) ri.next();
			if (childlf.isHide()) {
				continue;
			}
			if ("-1".equals(parentCode)) {
				str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
						+ childlf.getName() + "\",type:\"1\",state:{opened:true}} ,";
			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName() + "\",type:\"1\"},";
			}
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					DeptDb child = (DeptDb) childri.next();
					if (child.isHide()) {
						continue;
					}					
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName() + "\" ,type:\"1\"},";
					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = this.getJsonDept(dir, child.getCode(), str);
					}
				}
			}
		}
		return str;
	}
	/**
	 * 部门用户列表
	 * @return str
	 */
	private String getJsonUser()throws Exception {
		String sql = "select id,dept_code,user_name from dept_user ";
		com.cloudwebsoft.framework.db.JdbcTemplate jt = new com.cloudwebsoft.framework.db.JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		ResultRecord rr = null;
		String str = "";
		while(ri.hasNext()){
			rr = (ResultRecord) ri.next();
			int i = rr.getInt(1);
			String deptCode = rr.getString(2);
			String userName = rr.getString(3);
			str += "{id:\""+ i +"\", parent:\""+ deptCode+ "\",type:\"0\", text:\""+userName+"\"},";
		}
		return str ;
	}

	/**
	 * 递归获得jsTree的json字符串
	 * 
	 * @param parentCode
	 *            父节点parentCode
	 * @return str
	 */
	public void getJsonByDept(DeptMgr dir, String parentCode, Vector depts, ArrayList<String> list)
			throws Exception {
		if (!parentCode.equals(DeptDb.ROOTCODE)) {
			if (depts.contains(parentCode)) {
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
		}
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			DeptDb childlf = (DeptDb) ri.next();
			if (childlf.isHide()) {
				continue;
			}
			
			if (depts.contains(childlf.getCode())) {
				if (!list.contains(childlf.getCode())) {
					list.add(childlf.getCode());
				}
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
			if (childlf.getChildCount() > 0) {
				getJsonByDept(dir, childlf.getCode(), depts,
						list);
			}
			if (list.contains(childlf.getCode())) {
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
		}
	}
	
	/**
	 * @Description: 获取没有管理权限的部门列表
	 * @param request
	 * @return
	 */
	public JSONObject getNoAdminDepts(HttpServletRequest request) {
		DeptMgr dir = new DeptMgr();
		DeptDb dd = new DeptDb(DeptDb.ROOTCODE);
		Vector vec = new Vector();
		JSONObject json = new JSONObject();
		try {
			Vector v = privilege.getUserAdminDepts(request);
			Iterator it = v.iterator();
			while (it.hasNext()) {
				DeptDb d = (DeptDb) it.next();
				vec.add(d.getCode());
				Vector v1 = new Vector();
				v1 = d.getAllChild(v1, d);
				Iterator it1 = v1.iterator();
				while (it1.hasNext()) {
					DeptDb dd1 = (DeptDb) it1.next();
					vec.add(dd1.getCode());
				}
			}
			ArrayList<String> list = new ArrayList<String>();
			JSONArray ary1 = new JSONArray();
			JSONArray ary2 = new JSONArray();
			JSONArray ary3 = new JSONArray();
	    	this.getJsonByDept(dir, DeptDb.ROOTCODE, vec, list);
	    	v = new Vector();
	    	v = dd.getAllChild(v, dd);
	    	it = v.iterator();
	    	while (it.hasNext()) {
	    		DeptDb dept = (DeptDb) it.next();
	    		if (!list.contains(dept.getCode())) {
	    			ary2.add(dept.getCode());
	    		} else if (!vec.contains(dept.getCode())) {
	    			ary1.add(dept.getCode());
	    		} else {
	    			ary3.add(dept.getCode());
	    		}
	    	}
			json.put("ret", 1);
			json.put("isShow", ary1);
			json.put("isHide", ary2);
			json.put("isAdmin", ary3);
		} catch (ErrMsgException e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
		} catch (Exception e) {
			json.put("ret", 0);
			json.put("data", e.getMessage());
		}
		return json;
	}
	
	/**
	 * @Description: 获取没有管理权限的部门列表 --Old
	 * @param request
	 * @return
	 */
	public ArrayList<String> getNoAdminDeptsOld(HttpServletRequest request) {
		DeptDb dd = new DeptDb(DeptDb.ROOTCODE);
		Vector vec = new Vector();
		ArrayList<String> list = new ArrayList<String>();
		Privilege priv = new Privilege();
		try {
			vec = dd.getAllChild(vec, dd);
		} catch (ErrMsgException e) {
		}
		
		Iterator it = vec.iterator();
		while (it.hasNext()) {
			DeptDb dept = (DeptDb) it.next();
			if (!priv.canAdminDept(request, dept.getCode())) {
				list.add(dept.getCode());
			}
		}
		return list;
	}
	
	/**
	 * 取得所有隐藏的节点
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getAllHided() {
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select code from department where is_hide=1";
		JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                list.add(rr.getString(1));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
		return list;
	}	
}
