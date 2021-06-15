<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.project.*"%>
<%@ page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
long projectId = ParamUtil.getLong(request, "projectId", -1);
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = ProjectChecker.CODE_PREFIX + projectId;
String searchKind = ParamUtil.get(request, "searchKind");
String what = ParamUtil.get(request, "what");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "modifiedDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
String kind = ParamUtil.get(request, "kind");

try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "searchKind", searchKind, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>项目中的文档</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script>
var isLeftMenuShow = true;
function closeLeftMenu() {
	if (isLeftMenuShow) {
		window.parent.setCols("0,*");
		isLeftMenuShow = false;
		btnName.innerHTML = "打开菜单";
	}
	else {
		window.parent.setCols("200,*");
		isLeftMenuShow = true;
		btnName.innerHTML = "关闭菜单";		
	}
}

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	window.location.href = "project_doc_list.jsp?op=<%=op%>&projectId=<%=projectId%>&dir_code=<%=dir_code%>&searchKind=<%=searchKind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=" + orderBy + "&sort=" + sort + "&kind=<%=StrUtil.UrlEncode(kind)%>";
}
</script>
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<%@ include file="prj_inc_menu_top.jsp"%>
<script>
o("menu5").className="current";
</script>
<div class="spacerH"></div>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_READ)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

LeafPriv lp = new LeafPriv();
Leaf leaf = dir.getLeaf(dir_code);
if (!dir_code.equals("")) {
	if (leaf==null) {
		out.print(SkinUtil.makeErrMsg(request, "目录" + dir_code + "不存在！"));
		return;
	}
	lp.setDirCode(dir_code);
	if (!lp.canUserSee(privilege.getUser(request))) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
String dir_name = "";
if (leaf!=null)
	dir_name = leaf.getName();
if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	try {
		if (docmanager.del(request, id, privilege, true))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "project_doc_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&projectId=" + projectId));
		else
			out.print(StrUtil.Alert("删除失败！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	op = "";
}
else if (op.equals("delBatch")) {
	try {
		docmanager.delBatch(request, true);
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "project_doc_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&projectId=" + projectId));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	op = "";
}
else if (op.equals("passExamine")) {
	try {
		docmanager.passExamineBatch(request);
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "project_doc_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&projectId=" + projectId));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	op = "";
}
else if (op.equals("setOnTop")) {
	try {
		int id = ParamUtil.getInt(request, "id");
		Document doc = docmanager.getDocument(id);
        lp = new LeafPriv(doc.getDirCode());
		if (!lp.canUserExamine(privilege.getUser(request))) {
			out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		int level = ParamUtil.getInt(request, "level");
		doc.setLevel(level);
		boolean re = doc.UpdateLevel();
		
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "project_doc_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&projectId=" + projectId));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	return;
}

String sql = "select class1,title,id,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level from document where examine<>" + Document.EXAMINE_DUSTBIN;
if (op.equals("search")) {
	if (searchKind.equals("title")) {
		sql += " and title like "+StrUtil.sqlstr("%"+what+"%");
	}
	else if (searchKind.equals("content")) {
		sql = "select distinct id, class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level from document as d, doc_content as c where d.id=c.doc_id and examine=" + Document.EXAMINE_PASS;
	 	sql += " and c.content like " + StrUtil.sqlstr("%" + what + "%");
	}
	else {
		sql += " and examine=" + Document.EXAMINE_PASS + " and keywords like " + StrUtil.sqlstr("%" + what + "%");
	}
}

if (!dir_code.equals("")) {
	if (!lp.canUserModify(privilege.getUser(request))) {
		sql += " and examine=" + Document.EXAMINE_PASS;
	}	
	sql += " and (parent_code=" + StrUtil.sqlstr(dir_code) + " or class1=" + StrUtil.sqlstr(dir_code) + ")";
}

if (!kind.equals("")) {
	sql += " and kind=" + StrUtil.sqlstr(kind);
}

sql += " order by doc_level desc, examine asc, " + orderBy + " " + sort;

// out.print(sql);

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(SkinUtil.makeErrMsg(request, "标识非法！"));
	return;
}

int pagesize = 15;
int curpage = Integer.parseInt(strcurpage);
PageConn pageconn = new PageConn(Global.getDefaultDB(), Integer.parseInt(strcurpage), pagesize);
ResultIterator ri = pageconn.getResultIterator(sql);
ResultRecord rr = null;

Paginator paginator = new Paginator(request, pageconn.getTotal(), pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<form name="form1" action="project_doc_list.jsp?op=search" method="post">
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
    <tr>
      <td align="center">
      <%if (leaf.getChildCount()>0) {%>
      <select id="dir_code" name="dir_code" onchange="window.location.href='project_doc_list.jsp?projectId=<%=projectId%>&what=<%=StrUtil.UrlEncode(what)%>&dir_code=' + this.value + '&op=<%=op%>&searchKind=<%=searchKind%>&kind=<%=StrUtil.UrlEncode(kind)%>'">
		<%
        Leaf prjLeaf = new Leaf();
        prjLeaf = prjLeaf.getLeaf(ProjectChecker.CODE_PREFIX + projectId);
        %>      
                <option value="<%=prjLeaf.getCode()%>"><%=prjLeaf.getName()%></option>
        <%
        if (prjLeaf.getChildCount()>0) {
            Iterator ir = prjLeaf.getChildren().iterator();
            while (ir.hasNext()) {
                Leaf lf = (Leaf)ir.next();
                %>
                <option value="<%=lf.getCode()%>">└&nbsp;<%=lf.getName()%></option>
                <%
            }
        }
        %>      
        </select>
        <script>
        o("dir_code").value = "<%=dir_code%>";
        </script>
        <%}else{%>
        <input name="dir_code" value="<%=dir_code%>" type="hidden" />
        <%}%>
      按
        <select id="searchKind" name="searchKind">
          <option value="title">标题</option>
		  <%if (!Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {%>
          <option value="content">内容</option>
		  <%}%>
          <option value="keywords">关键字</option>
        </select>
        &nbsp;
        <input name=what size=20>
        &nbsp;
        <input class="btn" name="submit" type=submit value="搜索">
        <input name="projectId" type="hidden" value="<%=projectId%>">
        <input name="dir_code" type="hidden" value="<%=dir_code%>">
        &nbsp;<input type="button" onclick="addTab('目录', '<%=request.getContextPath()%>/fileark/dir_frame.jsp?root_code=<%=StrUtil.UrlEncode(dir_code)%>')" class="btn" value="管理目录" />
        <%if (!dir_code.equals("") && leaf.getType()==2) {%>
        &nbsp;<input class="btn" name="button" type=button onclick="javascript:window.location.href='../fwebedit_new.jsp?op=add&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name, "utf-8")%>';" value="添加文件" src="images/btn_add.gif" width=80 height=20 />
        <%}%>
        <%if (op.equals("search")) {%>
        <script>
        form1.searchKind.value = "<%=searchKind%>";
        </script>
        <%}%>
  	</td>
    </tr>
</table>
</form>
<table width="98%" border="0" align="center" class="percent98">
  <tr>
    <td width="56%" height="24" align="left">
<%    
DirKindDb dkd = new DirKindDb();
Vector vkind = dkd.listOfDir(dir_code);
SelectOptionDb sod = new SelectOptionDb();
if (vkind.size()>0) {
	String clr = "";
	if (kind.equals(""))
	  	clr = "style='color:red'";
%>
  <a <%=clr%> href="project_doc_list.jsp?projectId=<%=projectId%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>">[&nbsp;全部&nbsp;]</a></li>  
  <%
  Iterator irkind = vkind.iterator();
  while (irkind.hasNext()) {
	  dkd = (DirKindDb)irkind.next();
	  clr = "";
	  if (dkd.getKind().equals(kind)) {
	  	clr = "style='color:red'";
	  }
  %>
  &nbsp;&nbsp;<a <%=clr%> href="project_doc_list.jsp?projectId=<%=projectId%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&kind=<%=StrUtil.UrlEncode(dkd.getKind())%>">[&nbsp;<%=sod.getOptionName("fileark_kind", dkd.getKind())%>&nbsp;]</a>
  <%}%>
<%}%>

    </td>
    <td width="44%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="0" width="100%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" width="2%" noWrap><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids');" /></td>
      <%
	  if (leaf.getChildCount()>0) {
	  %>
	  <td class="tabStyle_1_title" width="16%">文件夹</td>
	  <%
	  }
	  %>
      <td class="tabStyle_1_title" width="42%" height="28" noWrap>标题</td>
      <td class="tabStyle_1_title" width="9%" noWrap onClick="doSort('author')" style="cursor:pointer">作者
	  <%if (orderBy.equals("author")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>      
      </td>
      <td class="tabStyle_1_title" width="9%" noWrap onClick="doSort('modifiedDate')" style="cursor:pointer">修改时间
	  <%if (orderBy.equals("modifiedDate")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  
	  </td>
      <td class="tabStyle_1_title" width="6%" noWrap>审核</td>
      <td class="tabStyle_1_title" width="16%" noWrap>管理</td>
    </tr>
<%
Document doc = new Document();		
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next(); 
	boolean isHome = rr.getInt("isHome")==1?true:false;
	String color = StrUtil.getNullStr(rr.getString("color"));
	boolean isBold = rr.getInt("isBold")==1;
	java.util.Date expireDate = rr.getDate("expire_date");
	doc = doc.getDocument(rr.getInt("id"));	
	%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td align="center"><input name="ids" type="checkbox" value="<%=rr.getInt("id")%>" /></td>
      <%
	  if (leaf.getChildCount()>0) {
		Leaf clf = new Leaf();
		clf = clf.getLeaf(doc.getDirCode());
	  %>
	  <td><a href="project_doc_list.jsp?projectId=<%=projectId%>&what=<%=StrUtil.UrlEncode(what)%>&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&op=<%=op%>&searchKind=<%=searchKind%>"><%=clf.getName()%></a></td>
	  <%
	  }
	  %>      
      <td height="24"><%if (rr.getInt("type")==1) {%>
	  <IMG height=15 alt="" src="../forum/images/f_poll.gif" width=17 border=0>&nbsp;
	  <%}
	  if (!doc.getKind().equals("")) {
		if (sod==null)
			sod = new SelectOptionDb();
		%>
		<a href="project_doc_list.jsp?projectId=<%=projectId%>&dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>&kind=<%=StrUtil.UrlEncode(doc.getKind())%>">[&nbsp;<%=sod.getOptionName("fileark_kind", doc.getKind())%>&nbsp;]</a>
        <%
	  }	  
	  %>
      <%if (DateUtil.compare(new java.util.Date(), expireDate)==2) {%>
	  	<a href="../doc_show.jsp?id=<%=rr.getInt("id")%>" title="<%=rr.getString(2)%>">
		<%
		if (isBold)
			out.print("<B>");
		if (!color.equals("")) {
		%>
			<font color="<%=color%>">
		<%}%>
		<%=rr.getString("title")%>
		<%if (!color.equals("")) {%>
		</font>
		<%}%>
		<%
		if (isBold)
			out.print("</B>");
		%>
		</a>
	  <%}else{%>
	  	<a href="../doc_show.jsp?id=<%=rr.getInt("id")%>" title="<%=rr.getString("title")%>"><%=rr.get("title")%></a>
	  <%}%></td>
      <td align="center">
      <%
	      UserDb ud = new UserDb();
	      ud = ud.getUserDb(doc.getAuthor());
	      String userName = "";
	      if (ud!=null && ud.isLoaded())
           userName = StrUtil.getNullStr(ud.getRealName());
      
      %>
      <%=userName.equals("") ?doc.getAuthor() : userName %>
      </td>
      <td align="center"><%
	  java.util.Date d = rr.getDate("modifiedDate");
	  if (d!=null)
	  	out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
	  %>      </td>
      <td align="center">
	  <%
	  int examine = rr.getInt("examine");
	  if (examine==0)
	  	out.print("<font color='blue'>未审核</font>");
	  else if (examine==1)
	  	out.print("<font color='red'>未通过</font>");
	  else
	  	out.print("已通过");
	  %>	  </td>
      <td align="center">
	  <%
	  Leaf lf6 = dir.getLeaf(rr.getString("class1"));	  
	  lp = new LeafPriv(lf6.getCode());	  
	  if (lp.canUserModify(privilege.getUser(request))) {
	  %>
		  <a href="javascript:;" onclick="addTab('编辑文件', 'fwebedit.jsp?op=edit&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(rr.getString("class1"))%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>')">编辑</a>&nbsp;&nbsp;
  <%
	  }
	  if (lp.canUserDel(privilege.getUser(request))) {
	  %>
		  <a onClick="return confirm('您确定要删除吗？')" href="project_doc_list.jsp?op=del&id=<%=rr.getString(3)%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>&projectId=<%=projectId%>">删除</a>&nbsp;&nbsp;
	  <%}%>
	  <!--<a href="../doc_show.jsp?id=<%=rr.getInt("id")%>">查看</a>&nbsp;-->
	  <%
	  if (lp.canUserExamine(privilege.getUser(request))) {
	  %>
		  <a href="project_doc_list.jsp?op=passExamine&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=<%=rr.getInt("id")%>&projectId=<%=projectId%>">通过</a>&nbsp;&nbsp;
      <%if (rr.getInt("doc_level")!=Document.LEVEL_TOP) {%>
		  <a onClick="return confirm('您确定要置顶吗？')" href="project_doc_list.jsp?op=setOnTop&level=<%=Document.LEVEL_TOP%>&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&projectId=<%=projectId%>">置顶</a>
		  <%}else{%>
		  <a onClick="return confirm('您确定要取消置顶吗？')" href="project_doc_list.jsp?op=setOnTop&level=0&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&projectId=<%=projectId%>">取消置顶</a>
		  <%}%>
	  <%}%>
	  </td>
    </tr>
    <%}%>
  </tbody>
</table>
<table class="percent98" width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  
  <tr>
    <td width="55%" align="left">
<input class="btn" name="button3" type="button" onClick="doDel()" value="删除">
&nbsp;
<input class="btn" name="button32" type="button" onClick="passExamineBatch()" value="通过"></td>
    <td width="45%" align="right"><%
	String querystr = "op="+op+"&projectId=" + projectId + "&what="+StrUtil.UrlEncode(what) + "&dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=" + op + "&searchKind=" + searchKind + "&kind=" + StrUtil.UrlEncode(kind);
    out.print(paginator.getCurPageBlock("project_doc_list.jsp?"+querystr, "down"));
%></td>
  </tr>
</table>
</body>
<script src="../inc/common.js"></script>
<script>
function doDel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择文章！");
		return;
	}
	
	if (!confirm("您确定要删除么？"))
		return;
	
	window.location.href = "?op=delBatch&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&projectId=<%=projectId%>&ids=" + ids;
}

function passExamineBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择文章！");
		return;
	}
	window.location.href = "?op=passExamine&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&projectId=<%=projectId%>&ids=" + ids;
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}
</script>
</html>