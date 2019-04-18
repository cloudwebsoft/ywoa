<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.post.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>修改岗位</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
  .unit {
	  background-color:#CCC;
  }
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.organize.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "archive.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String op = ParamUtil.get(request, "op");
int id = ParamUtil.getInt(request, "post_id" , 0);

PostDb pdb = new PostDb();
boolean isEdit = false;

if (op.equals("edit")) {
	pdb = pdb.getPostDb(id);
	if (!pdb.isLoaded()) {
		out.println(SkinUtil.makeErrMsg(request, "岗位不存在！"));
		return;
	}
	isEdit = true;
}
%>
<%if (op.equals("edit")) {%>
<%@ include file="post_op_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<%} else {%>
<%@ include file="post_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<%} %>
<div class="spacerH"></div>
<form>
<table class="tabStyle_1 percent80" width="65%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td height="31" colspan="2" align="center" class="tabStyle_1_title">
        <%if (isEdit) {%>
        修改岗位
        <%}else{%>
        添加岗位
        <%}%>
      </td>
    </tr>
    <tr>
      <td width="30%" height="32" align="center">名称</td>
      <td align="left"><input id="name" name="name" value="<%=isEdit ? pdb.getString("name") : ""%>">
      </td>
    </tr>
    
    <tr>
      <td height="32" align="center">单位</td>
      <td align="left">
              <select id="unitCode" name="unitCode">
              <%
			  DeptDb dd = new DeptDb();
			  DeptView dv = new DeptView(request, dd);
			  StringBuffer sb = new StringBuffer();
			  dd = dd.getDeptDb(privilege.getUserUnitCode(request));
			  out.print(dv.getUnitAsOptions(sb, dd, dd.getLayer()));
			  %>
              </select>
	  </td>
    </tr>
    <tr>
      <td height="32" align="center">序号</td>
      <td align="left"><input id="orders" name="orders" value="<%=isEdit ? pdb.getInt("orders") : ""%>">
      </td>
    </tr>
    <tr>
      <td height="32" align="center">描述</td>
      <td align="left"><textarea id="description" name="description" rows=5 cols=70><%=isEdit ? StrUtil.getNullStr(pdb.getString("description")) : ""%></textarea></td>
    </tr>
    <tr>
      <td height="43" colspan="2" align="center"><input class="btn" name="Submit" type="button" onclick="dosummit()" value="确定">
        &nbsp;&nbsp;&nbsp;
        <input name="Submit" type="button" onclick="window.location.href='post_m.jsp'" class="btn" value="返回">
        </td>
    </tr>
</table>
</form>
</body>
<script>
var name, orders, des;
$(function() {
	name = new LiveValidation('name');
  	name.add( Validate.Presence );
	name.add(Validate.Length, { minimum: 1, maximum: 20 } );
	orders = new LiveValidation('orders');
  	orders.add( Validate.Numericality, { minimum: 1, tooLowMessage: '不能小于0' } );
  	orders.add( Validate.Numericality, { maximum: 100, tooLowMessage: '不能大于100' } );
  	orders.add( Validate.Numericality, { onlyInteger: true } );
  	des = new LiveValidation('description');
	des.add(Validate.Length, { minimum: 1, maximum: 200 } );

	<%if (isEdit) { %>
		$('#unitCode').val('<%=pdb.getString("unit_code")%>');
    <%} %>
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

function dosummit() {
	if (!LiveValidation.massValidate(orders.formObj.fields)
			|| !LiveValidation.massValidate(name.formObj.fields)
			|| !LiveValidation.massValidate(des.formObj.fields)) {
		return;
	}
	
	$.ajax({
		type:"post",
		url:"post_do.jsp",
		data:{
			op: '<%=isEdit ? "editPost" : "addPost"%>',
			id: '<%=id%>',
			name: $('#name').val(),
			unitCode: $('#unitCode').val(),
			description: $('#description').val(),
			orders: $('#orders').val()
		},
		dataType:"html",
		beforeSend: function(XMLHttpRequest){
			showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data.trim());
			if(data.ret == 1){
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
</script>
</html>