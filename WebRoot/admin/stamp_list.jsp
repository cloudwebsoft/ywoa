<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.stamp.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>印章管理</title>
<script src="../inc/common.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style type="text/css">
/*Tooltips*/
.tooltips{
position:relative; /*这个是关键*/
z-index:2;
}
.tooltips:hover{
z-index:3;
background:none; /*没有这个在IE中不可用*/
}
.tooltips span{
display: none;
}
.tooltips:hover span img {
max-width:300px;
width:expression(this.width>300?"300px":this.width);
}
.tooltips:hover span{ /*span 标签仅在 :hover 状态时显示*/
display:block;
position:absolute;
top:21px;
left:9px;
width:5px;
border:0px solid black;
background-color: #FFFFFF;
padding: 3px;
color:black;
}
</style>
<script>
var selUserNames,selUserRealNames,curFormObj;
function openWinUsers(formObj) {
	curFormObj = formObj;
	selUserNames = formObj.userNames.value;
	selUserRealNames = formObj.userRealNames.value;
	//showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
	openWin('../user_multi_sel.jsp','900','730');
}

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function setUsers(users, userRealNames) {
	curFormObj.userNames.value = users;
	curFormObj.userRealNames.value = userRealNames;
}

var curFormObj
function openWinUserRoles(formObj) {
	curFormObj = formObj;
	var roleCodes = "";
	roleCodes = curFormObj.roleCodes.value
	showModalDialog('../role_multi_sel.jsp?roleCodes=' + roleCodes,window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;');
	return;
}

function setRoles(roleCodes, roleDescs) {
	curFormObj.roleCodes.value = roleCodes;
	curFormObj.roleNames.value = roleDescs;
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String by = ParamUtil.get(request, "by");
if (by.equals(""))
	by = "name";
String what = ParamUtil.get(request, "what");

String querystr = "by=" + by + "&what=" + StrUtil.UrlEncode(what);

StampMgr lm = new StampMgr();
StampDb ld = new StampDb();
String kind = StampDb.KIND_DEFAULT;
String op = ParamUtil.get(request, "op");

if (op.equals("add")) {
	try {
		if (lm.add(application, request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "stamp_list.jsp?kind=" + kind));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("edit")) {
	try {
		if (lm.modify(application, request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "stamp_list.jsp?kind=" + kind));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("del")) {
	if (lm.del(application, request)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "stamp_list.jsp?kind=" + kind));
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_del")));
	}
	return;
}
%>
<%@ include file="stamp_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form action="stamp_list.jsp" name=form1 method=get>
  <table width="80%" border="0" align="center">
  <tr>
    <td width="100%" align="center">
    <select id="by" name="by" style="display:none">
      <option value='name'>名称</option>
    </select>
    名称&nbsp;
    <input id="what" name="what" value="<%=what%>" size="20">
    &nbsp;
    <input name="submit" type="submit" class="btn" value="搜索" />
    <script>
	o("by").value = "<%=by%>";
	</script>
    <input name="op" type="hidden" value="search" />
    </td>
    </tr>
</table>
</form>
<%
int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String sql = ld.getListSql(kind);

if (!what.equals("")) {
	if (by.equals("name")) {
    	sql = "select id from " + ld.getTableName() + " where kind=" + StrUtil.sqlstr(kind) + " and title like " + StrUtil.sqlstr("%" + what + "%") + " order by sort";
	}
}

ListResult lr = ld.listResult(sql, curpage, pagesize);
Iterator ir = lr.getResult().iterator();

int total1 = lr.getTotal();
Vector v = lr.getResult();
Iterator ir1 = null;
if (v!=null)
	ir1 = v.iterator();
paginator.init(total1, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<table width="98%" border="0" cellpadding="0" cellspacing="0" class="percent98">
  <tr><td align="right">
&nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>
</td></tr></table>
<table cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1">
  <thead>
    <tr>
      <td class="tabStyle_1_title" width="4%">编号</td>
      <td class="tabStyle_1_title" width="19%">名称</td>
      <td class="tabStyle_1_title" width="21%">授权角色</td>
      <td class="tabStyle_1_title" width="18%">授权用户</td>
      <td class="tabStyle_1_title" width="18%">图片</td>
	  <td class="tabStyle_1_title" width="20%"><lt:Label key="op"/></td>
    </tr>
  </thead>
  <tbody>
<%
UserMgr um = new UserMgr();
int i=100;
while (ir.hasNext()) {
 	ld = (StampDb)ir.next();
	i++;
	String userNames = "";
	String userRealNames = "";
	String[] aryusers = StrUtil.split(ld.getUserNames(), ",");
	if (aryusers!=null) {
		int len = aryusers.length;
		for (int k=0; k<len; k++) {
			UserDb ud = um.getUserDb(aryusers[k]);
			if (!ud.isLoaded()) {
				// System.out.println(getClass() + " " + aryusers[k]);
				continue;
			}
			if (userNames.equals("")) {
				userNames = aryusers[k];
				userRealNames = ud.getRealName();
			}
			else {
				userNames += "," + aryusers[k];
				userRealNames += "," + ud.getRealName();
			}
		}
	}
	
	String roleCodes = "";
	String roleNames = "";
	RoleMgr rm = new RoleMgr();
	String[] aryRoles = StrUtil.split(ld.getRoleCodes(), ",");
	if (aryRoles!=null) {
		int len = aryRoles.length;
		for (int k=0; k<len; k++) {
			RoleDb rd = rm.getRoleDb(aryRoles[k]);
			if (!rd.isLoaded()) {
				continue;
			}
			if (roleCodes.equals("")) {
				roleCodes = aryRoles[k];
				roleNames = rd.getDesc();
			}
			else {
				roleCodes += "," + aryRoles[k];
				roleNames += "," + rd.getDesc();
			}
		}
	}	
	
	%>
	  <form name="form<%=i%>" action="?op=edit&kind=<%=kind%>" method="post" enctype="MULTIPART/FORM-DATA">
    <tr>
      <td><%=ld.getId()%></td>
      <td>
      <input name=title value="<%=ld.getTitle()%>">
      <%if (ld.getImage()==null || ld.getImage().equals("")) {%>
	  <%}else{%>
      <a class="tooltips" href="#">查看
      <span><img src="<%=request.getContextPath() %>/img_show.jsp?path=<%=ld.getImageUrl(request)%>"></span>
      </a>
      <%}%>
      </td>
      <td>
	  <input name="roleCodes" value="<%=roleCodes%>" type="hidden" />
	  <input name="roleNames" value="<%=roleNames%>" size="20" readonly="readonly" />
      <input class="btn" title="" onclick="openWinUserRoles(form<%=i%>)" type="button" value="选择" name="button3" /></td>
      <td><input name="userRealNames" value="<%=userRealNames%>" size="15" readonly>
          <input name="userNames" value="<%=userNames%>" type="hidden">
          <input class="btn" title="" onclick="openWinUsers(form<%=i%>)" type="button" value="选择" name="button" />
          <input name="pwd" value="<%=ld.getPwd()%>" size="8" type="hidden" />      </td>
      <td class="highlight">
        <input name="filename" type="file" style="width: 150px" size="10">		</td>
      <td>
	  [ <a href="javascript:;" onclick="if (check(form<%=i%>)) form<%=i%>.submit()"><lt:Label key="op_edit"/></a> ] [ <a onClick="if (!confirm('<lt:Label key="confirm_del"/>')) return false" href="stamp_list.jsp?op=del&id=<%=ld.getId()%>&kind=<%=kind%>&userNames=<%=StrUtil.UrlEncode(userNames)%>"><lt:Label key="op_del"/></a> ] 
	  [ <a href="stamp_log.jsp?stampId=<%=ld.getId()%>&op=search">日志</a> ]	
	  <input name="id" value="<%=ld.getId()%>" type="hidden">
	  <input name="kind" value="<%=kind%>" type="hidden">	  </td>
    </tr>
    </form>
<%}%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td>&nbsp;</td>
	<form action="?op=add&kind=<%=kind%>" method="post" onsubmit="return check(addform1)" enctype="multipart/form-data" name="addform1">
      <td><input name=title value=""></td>
      <td><input name="roleCodes" value="" type="hidden" />
        <input name="roleNames" value="" size="20" readonly="readonly" />
        <input class="btn" title="" onclick="openWinUserRoles(addform1)" type="button" value="选择" name="button32" /></td>
      <td><input name="userRealNames" value="" size="15" readonly>
	  <input name="userNames" value="" size="15" type="hidden">
      <input class="btn" title="添加收件人" onclick="openWinUsers(addform1)" type="button" value="选择" name="button2" />
	  <input name="pwd" value="111111" type="hidden" />	  </td>
      <td><span class="stable">
        <input type="file" name="filename" style="width: 150px" size="10">
      </span></td>
      <td><INPUT type=submit height=20 width=80 value="添加" class="btn">
        <input name="kind" value="<%=kind%>" type="hidden">        </td>
	</form>
    </tr>
    <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
      <td colspan="6" style="PADDING-LEFT: 10px; text-align:left">
	  注意：<br/>
	  1、如果编辑时上传了图片，则会替换原来的图片<br/>
	  2、意见输入框中使用的是私章，即只根据授权用户取私章<br/>
	  3、图片签名框宏控件、Office宏控件中签章则根据授权角色及授权用户获取印章
      </td>
    </tr>
  </tbody>
</table>
<table width="98%" class="percent98">
      <tr>
        <td align="right">
		<%
		out.print(paginator.getCurPageBlock("?"+querystr));
		%>
        </td>
      </tr>
</table>
</body>
<script>
function check(frm) {
	if (isNumeric(frm.title.value)) {
		alert("名称不能为数字！");
		return false;
	}
	return true;
}
</script>
</html>