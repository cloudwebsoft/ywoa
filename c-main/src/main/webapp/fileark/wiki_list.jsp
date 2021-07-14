<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.plugin.*" %>
<%@ page import="com.redmoon.oa.fileark.plugin.base.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
String dir_code = ParamUtil.get(request, "dir_code");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "modifiedDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
String pageUrl = usd.isWebedit()?"fwebedit.jsp":"fwebedit_new.jsp";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>文件列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
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
	window.location.href = "wiki_list.jsp?op=<%=op%>&dir_code=<%=dir_code%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
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

if (dir_code.indexOf("cws_prj_")==0) {
	String projectId = dir_code.substring(8);
	// 如果projectId中含有下划线_，则截取出其ID
	int p = projectId.indexOf("_");
	if (p!=-1) {
		projectId = projectId.substring(0, p);
	}						
	String pageRedirect = "../project/project_doc_list.jsp?projectId=" + projectId + "&dir_code=" + StrUtil.UrlEncode(dir_code);
	response.sendRedirect(pageRedirect);
	return;
}

String dir_name = "";
if (leaf!=null)
	dir_name = leaf.getName();
if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	try {
		if (docmanager.del(request, id, privilege, true))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
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
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
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
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
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
		
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(dir_code)));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	return;
}
else if (op.equals("changeDir")) {
	String strIds = ParamUtil.get(request, "ids");
	String[] ids = StrUtil.split(strIds, ",");
	String newDirCode = ParamUtil.get(request, "newDirCode");
	if (ids==null) {
		out.print(StrUtil.Alert_Back("请选择文件！"));
		return;
	}
	DocumentMgr dm = new DocumentMgr();
	for (int i=0; i<ids.length; i++) {
		Document doc = dm.getDocument(StrUtil.toInt(ids[i]));
		doc.UpdateDir(newDirCode);	
	}
	out.print(StrUtil.Alert_Redirect("操作成功！", "wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(newDirCode)));
	return;
}

boolean isProject = false;
if (dir_code.indexOf("cws_prj_")==0) {
	
}

String parentCode = ParamUtil.get(request, "parentCode");
LeafPriv plp = null;
if (!parentCode.equals(""))
	plp = new LeafPriv(parentCode);

String sql = "select class1,title,id,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level from document where examine<>" + Document.EXAMINE_DUSTBIN;
if (op.equals("search")) {
	if (kind.equals("title")) {
		sql += " and title like "+StrUtil.sqlstr("%"+what+"%");
	}
	else if (kind.equals("content")) {
		sql = "select distinct id, class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level from document as d, doc_content as c where d.id=c.doc_id";
	 	sql += " and c.content like " + StrUtil.sqlstr("%" + what + "%");
	}
	else {
		sql += " and keywords like " + StrUtil.sqlstr("%" + what + "%");
	}
}

if (!parentCode.equals("")) {
	if (!plp.canUserModify(privilege.getUser(request))) {
		sql += " and examine=" + Document.EXAMINE_PASS;
	}
	sql += " and (parent_code=" + StrUtil.sqlstr(parentCode) + " or class1=" + StrUtil.sqlstr(parentCode) + ")";
}
else if (!dir_code.equals("")) {
	sql += " and class1=" + StrUtil.sqlstr(dir_code);
	if (!lp.canUserModify(privilege.getUser(request))) {
		sql += " and examine=" + Document.EXAMINE_PASS;
	}
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
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
int curpage = Integer.parseInt(strcurpage);
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql, Integer.parseInt(strcurpage), pagesize);
ResultRecord rr = null;

long total = jt.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<table id="searchTable" width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
    <tr>
      <td align="center"><form name="form1" action="wiki_list.jsp?op=search" method="post">
<script>
if (typeof(window.parent.leftFileFrame)=="object") {
	// document.write("&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">关闭菜单</span></a>");
}
</script>
        &nbsp;
        <select name="kind">
          <option value="title">标题</option>
		  <%if (!Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {%>
          <option value="content">内容</option>
		  <%}%>
          <option value="keywords">关键字</option>
        </select>
&nbsp;
<input name=what size=20 value="<%=what%>">
&nbsp;
<input class="tSearch" name="submit" type=submit value="搜索">
<input name="dir_code" type="hidden" value="<%=dir_code%>">
<input name="parentCode" type="hidden" value="<%=parentCode%>">
  <%if (op.equals("search")) {%>
  <script>
  form1.kind.value = "<%=kind%>";
  </script>
  <%}%>
  </form>
</td>  
    </tr>
</table>
<table id="grid" cellSpacing="0" cellPadding="0" width="968">
  <thead>
    <tr>
    <%if (!parentCode.equals("")) {%>
      <th width="100" noWrap>文件夹</th>
    <%}%>
      <th width="360" noWrap>标题</th>
      <th width="60" abbr="author">作者</th>
      <th width="90" abbr="modifiedDate">编辑时间</th>
      <th width="60" abbr="examine" noWrap>审核</th>
      <th width="150" noWrap>操作</th>
    </tr>
  </thead>
  <tbody>
<%
String querystr = "op="+op+"&what="+StrUtil.UrlEncode(what) + "&dir_code=" + StrUtil.UrlEncode(dir_code) + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what);
Document doc = new Document();
PluginMgr pm = new PluginMgr();
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next(); 
	boolean isHome = rr.getInt("isHome")==1?true:false;
	String color = StrUtil.getNullStr(rr.getString("color"));
	boolean isBold = rr.getInt("isBold")==1;
	java.util.Date expireDate = rr.getDate("expire_date");
	doc = doc.getDocument(rr.getInt("id"));
	%>
    <tr id=<%=rr.getInt("id")%> onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
    <%if (!parentCode.equals("")) {
		Leaf clf = new Leaf();
		clf = clf.getLeaf(doc.getDirCode());	
	%>
      <td noWrap><a href="wiki_list.jsp?dir_code=<%=StrUtil.UrlEncode(doc.getDirCode())%>&orderBy=<%=orderBy%>&sort=<%=sort%>"><%=clf.getName()%></a></td>
    <%}%>    
      <td height="24">
	  <%if (rr.getInt("type")==1) {%>
	  <IMG height=15 alt="" src="../forum/skin/bluedream/images/f_poll.gif" width=17 border=0>&nbsp;
	  <%}%>
      <%
	  PluginUnit pu = pm.getPluginUnitOfDir(doc.getDirCode());
	  String viewPage = "";
	  if (pu!=null) {
		  IPluginUI ipu = pu.getUI(request);
		  viewPage = request.getContextPath() + "/" + ipu.getViewPage();
	  }
	  if (viewPage.equals(""))
		  viewPage = "../doc_show.jsp";
	  if (DateUtil.compare(new java.util.Date(), expireDate)==2) {%>
	  	<a href="javascript:;" onclick="addTab('<%=rr.getString("title")%>','<%=viewPage%>?id=<%=rr.getInt("id")%>')" title="<%=rr.getString(2)%>">
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
	  	<a href="javascript:;" onclick="addTab('<%=rr.getString("title")%>', '<%=viewPage%>?id=<%=rr.getInt("id")%>')" title="<%=rr.getString("title")%>"><%=rr.get("title")%></a>
	  <%}%></td>
      <td><%=doc.getAuthor()%></td>
      <td><%
	  java.util.Date d = rr.getDate("modifiedDate");
	  if (d!=null)
	  	out.print(DateUtil.format(d, "yy-MM-dd HH:mm"));
	  %>
      </td>
      <td align="center">
	  <%
	  int examine = rr.getInt("examine");
	  if (examine==0)
	  	out.print("<font color='blue'>未审核</font>");
	  else if (examine==1)
	  	out.print("<font color='red'>未通过</font>");
	  else
	  	out.print("已通过");
	  %>
      </td>
      <td align="center">
	  <%
	  Leaf lf6 = dir.getLeaf(rr.getString("class1"));	  
	  lp = new LeafPriv(lf6.getCode());	  
	  if (lp.canUserModify(privilege.getUser(request))) {
	  %>
		  <a href="../wiki_edit.jsp?op=edit&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(rr.getString("class1"))%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">编辑</a>&nbsp;
	  <%
	  }
	  if (lp.canUserDel(privilege.getUser(request))) {
	  %>
		  <a onClick="return confirm('您确定要删除吗？')" href="wiki_list.jsp?op=del&id=<%=rr.getString(3)%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&dir_name=<%=StrUtil.UrlEncode(dir_name)%>">删除</a>&nbsp;
	  <%}%>
	  <!--<a href="../doc_show.jsp?id=<%=rr.getInt("id")%>">查看</a>&nbsp;-->
	  <%
	  if (lp.canUserExamine(privilege.getUser(request))) {
	  %>
		  <a href="wiki_list.jsp?op=passExamine&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=<%=rr.getInt("id")%>">通过</a>
		  <%if (rr.getInt("doc_level")!=Document.LEVEL_TOP) {%>
		  &nbsp;<a onClick="return confirm('您确定要置顶吗？')" href="wiki_list.jsp?op=setOnTop&level=<%=Document.LEVEL_TOP%>&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>">置顶</a>
		  <%}else{%>
		  &nbsp;<a onClick="return confirm('您确定要取消置顶吗？')" href="wiki_list.jsp?op=setOnTop&level=0&id=<%=rr.getInt("id")%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>">取消置顶</a>
		  <%}%>
	  <%}%>
	  </td>
    </tr>
    <%}%>
  </tbody>
</table>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "wiki_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&parentCode=<%=parentCode%>";
}

function changePage(newp) {
	if (newp)
		window.location.href = "wiki_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>" + "&parentCode=<%=parentCode%>";
}

function rpChange(pageSize) {
	window.location.href = "wiki_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize + "&orderBy=<%=orderBy%>" + "&sort=<%=sort%>" + "&parentCode=<%=parentCode%>";
}

function onReload() {
	window.location.reload();
}

var buttonObj;
buttonObj = [
<%if (!dir_code.equals("") && leaf.getType()==2) {%>	
		{name: '添加', bclass: 'add', onpress : action},
<%}%>		
		{name: '编辑', bclass: 'edit', onpress : action},
		{name: '通过', bclass: 'pass', onpress : action},
		{name: '删除', bclass: 'delete', onpress : action},
	<%
	if (lp.canUserExamine(privilege.getUser(request))) {
	%>
		{name: '统计', bclass: 'stata', onpress : action},
	<%}%>
	<%if (!dir_code.equals("") && lp.canUserModify(privilege.getUser(request))) {%>
		{name: '目录', bclass: 'directory', onpress : action},
		{name: '迁移', bclass: 'changeDir', onpress : action},		
	<%}%>
	<%if (false && leaf!=null && leaf.getChildCount()>0) {%>
		{name: '全部', bclass: 'listIncChild', onpress : action},
	<%}%>
		{name: '排行', bclass: 'rank', onpress : action},
	<%if (lp.canUserExamine(privilege.getUser(request))) {%>
		{name: '审核', bclass: 'check', onpress : action},
	<%}%>
		{separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		];

flex = $("#grid").flexigrid
(
	{
	buttons : buttonObj,
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	*/
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",	
	url: false,
	usepager: true,
	checkbox : true,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	// title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: false,
	
	onChangeSort: changeSort,
	
	onChangePage: changePage,
	onRpChange: rpChange,
	onReload: onReload,
	/*
	onRowDblclick: rowDbClick,
	onColSwitch: colSwitch,
	onColResize: colResize,
	onToggleCol: toggleCol,
	*/
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {
	if (com=='添加')	{
		window.location.href='../wiki_post.jsp?op=add&dir_code=<%=StrUtil.UrlEncode(dir_code)%>'
	}
	else if (com=='统计') {
		window.location.href='fileark_stat_year.jsp?dirCode=<%=StrUtil.UrlEncode(dir_code)%>';
	}
	else if (com=="目录") {
		window.location.href='dir_frame.jsp?root_code=<%=StrUtil.UrlEncode(dir_code)%>';
	}
	else if (com=='编辑') {
		selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			alert('请选择一条记录!');
			return;
		}
		if (selectedCount > 1) {
			alert('只能选择一条记录!');
			return;
		}
		
		var id = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).val();
		window.location.href = "../<%=pageUrl%>?op=edit&id=" + id + "&dir_code=<%=StrUtil.UrlEncode(dir_code)%>";
	}
	else if (com=='删除') {
		selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			alert('请选择一条记录!');
			return;
		}
		if (!confirm("您确定要删除么？"))
			return;
		
		var ids = "";
		$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
			if (ids=="")
				ids = $(this).val();
			else
				ids += "," + $(this).val();
		});			
			
		window.location.href = "?op=delBatch&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids + "&parentCode=<%=leaf!=null?leaf.getCode():""%>";
	}
	else if (com=='通过') {
		selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			alert('请选择一条记录!');
			return;
		}
		var ids = "";
		$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
			if (ids=="")
				ids = $(this).val();
			else
				ids += "," + $(this).val();
		});			
		window.location.href = "?op=passExamine&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids + "&parentCode=<%=leaf!=null?leaf.getCode():""%>";
	}
	else if (com=='全部') {
		window.location.href = "wiki_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>&sort=<%=sort%>&parentCode=<%=leaf!=null?leaf.getCode():""%>";		
	}
	else if (com=='审核') {
		addTab("审核", "cms/plugin/wiki/admin/wiki_frame.jsp");
	}
	else if (com=="排行") {
		addTab("排行", "cms/plugin/wiki/admin/wiki_doc_edit_rank.jsp");
	}
	else if(com=="迁移"){
	    selectedCount = $(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).length;
		if (selectedCount == 0) {
			alert('请选择一条记录!');
			return;
		}
		var ids = "";
		$(".cth input[type='checkbox'][value!='on']:checked", grid.bDiv).each(function(i) {
			if (ids=="")
				ids = $(this).val();
			else
				ids += "," + $(this).val();
		});
		
		idsSelected = ids;
		
		openWin("dir_sel.jsp?dirCode=<%=Leaf.CODE_WIKI%>", 640, 480);
	}	
}

function selectNode(code, name) {
	window.location.href = "wiki_list.jsp?op=changeDir&ids=" + idsSelected + "&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&newDirCode=" + code;
}
</script>
</html>