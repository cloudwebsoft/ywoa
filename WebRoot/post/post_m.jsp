<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.post.PostDb"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>岗位管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.organize.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "archive.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

PostDb pdb = new PostDb();

%>
<%@ include file="post_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%

String sql = "select id from post where 1=1 ";
String action = ParamUtil.get(request, "action");
String description = ParamUtil.get(request, "description");
String curUnitCode = ParamUtil.get(request, "curUnitCode");

if (action.equals("search")) {
	if(!description.equals("")){
		sql +=" and (name like " + StrUtil.sqlstr("%" + description + "%")
			+ " or description like" + StrUtil.sqlstr("%" + description + "%") + ")";
	}
	if(!curUnitCode.equals("")){
		sql +=" and unit_code =" +StrUtil.sqlstr(curUnitCode);
	}
}

sql += " order by orders desc";
Vector v = pdb.list(sql);

%>
<table width="98%" class="percent98">
  <tr>
    <td align="center">
    <form id="searchForm" name="searchForm" method="get">
      单位
      <select id="curUnitCode" name="curUnitCode">
      <option value="">不限</option>
      <%
		DeptDb dd = new DeptDb();
		DeptView dv = new DeptView(request, dd);
		StringBuffer sb = new StringBuffer();
		dd = dd.getDeptDb(privilege.getUserUnitCode(request));
        out.print(dv.getUnitAsOptions(sb, dd, dd.getLayer()));
      %>
      </select>
      <script>
      	$(function() {
          	$("#curUnitCode").val("<%=curUnitCode%>");
      	});
      </script>    
      名称
      <input type="hidden" name="action" value="search" />
      <input type="text" name="description" value="<%=description%>" />
      <input type="submit" class="btn" value="搜索" />
    </form></td>
  </tr>
</table>
<table id="mainTable" cellSpacing="0" class="tabStyle_1 percent98" cellPadding="3" width="95%" align="center">
<thead>
    <tr>
      <td class="tabStyle_1_title" noWrap width="10%">序号</td>
      <td class="tabStyle_1_title" noWrap width="18%">名称</td>
      <td class="tabStyle_1_title" noWrap width="18%">单位</td>
      <td class="tabStyle_1_title" noWrap width="38%">描述</td>
      <td width="16%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    </thead>
    <tbody>
<%
Iterator it = v.iterator();
while (it.hasNext()) {
 	PostDb db = (PostDb)it.next();
	int id = db.getInt("id");
	String name = db.getString("name");
	%>
    <tr id="tr_<%=id %>" class="highlight">
    	<td><%=db.getInt("orders") %></td>
      <td><a href="post_op.jsp?op=edit&post_id=<%=id%>"><%=name%></a></td>
      <td>
	  <%=new DeptDb(db.getString("unit_code")).getName()%>
	  </td>
	  <td>
	  	<%=HtmlUtil.getAbstract(request, StrUtil.getNullStr(db.getString("description")), 50) %>
	  </td>
      <td align="center">
	  <a href="post_op.jsp?op=edit&post_id=<%=id%>">编辑</a>
			&nbsp;&nbsp;<a href="javascript:;" onClick="delPost('<%=id%>')">删除</a>
      </td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
<script>
$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});

function showLoading() {
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display":"block"});
	$(".loading").css({"display":"block"});
}

function hideLoading() {
	$(".loading").css({"display":"none"});
	$(".treeBackground").css({"display":"none"});
	$(".treeBackground").removeClass("SD_overlayBG2");
}

function delPost(id){
	jConfirm("您确定要删除么？", "提示", function(r) {
		if (!r) {
			return;
		} else {
			$.ajax({
				type:"get",
				url:"post_do.jsp?op=delPost&id=" + id,
				dataType:"html",
				beforeSend: function(XMLHttpRequest){
					showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data.trim());
					if(data.ret == 1){
						$('#tr_' + id).remove();
						$.toaster({priority : 'info', message : '操作成功' });
					} else {
						$.toaster({priority : 'info', message : data.msg });
					}
				},
				complete: function(XMLHttpRequest, status){
					hideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		}
	});
}
</script>
</html>