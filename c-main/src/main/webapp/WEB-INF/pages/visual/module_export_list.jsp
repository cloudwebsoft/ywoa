<%@ page contentType="text/html;charset=utf-8"%>
<%@ page isELIgnored="false" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@ taglib prefix="q" uri="http://www.yimihome.com/tags/QObject"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
String code = ParamUtil.get(request, "code");
String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计 - 导出设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "name";
String sort = ParamUtil.get(request, "sort");
%>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "module_export_list.jsp?code=<%=code%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="../../../visual/module_setup_inc_menu_top.jsp"%>
<script>
o("menu9").className="current"; 
</script>
<div class="spacerH"></div>
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="left"><input class="btn" name="button" type="button" onclick="javascript:location.href='module_export_add.jsp?code=${code}&formCode=${formCode}';" value="添加" width=80 height=20 />
	</td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tr>
      <td width="20%" class="tabStyle_1_title">名称</td>
      <td width="20%" class="tabStyle_1_title">标题栏</td>
      <td width="5%" class="tabStyle_1_title">表头字体</td>
      <td width="10%" class="tabStyle_1_title">表头字号</td>
      <td width="10%" class="tabStyle_1_title">表头背景色</td>
      <td width="10%" class="tabStyle_1_title">表头前景色</td>
      <td width="5%" class="tabStyle_1_title">表头加粗</td>
      <td width="5%" class="tabStyle_1_title">行高</td>
      <td width="25%" class="tabStyle_1_title">操作</td>
    </tr>
    <q:forEach items="${items}" var="item" varStatus="status">
        <tr id="tr${item.id}">
            <td>${item.name}</td>
            <td>${item.bar_name}</td>
            <td>${item.font_family}</td>
            <td>${item.font_size}</td>
            <td>
				<div style="border: 1px solid rgb(204, 204, 204); border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: ${item.back_color};"></div>    
            </td>
            <td>
				<div style="border: 1px solid rgb(204, 204, 204); border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: ${item.fore_color};"></div>    
            </td>
            <td>
            ${item.is_bold == 1?"是":"否" }
            </td>
            <td>${item.line_height}</td>
            <td align="center"><input class="btn" type="button" value="修改" onclick="window.location.href='module_export_edit.jsp?id=${item.id}&code=${code}&formCode=${formCode}'" />
			&nbsp;&nbsp;
			<input class="btn" type="button" onclick="del(${item.id})" value="删除" /></td>
        </tr>
    </q:forEach>
</table>
<br />
<br>
</body>
<script language="javascript">
function del(id) {
	jConfirm('您确定要删除吗?','提示',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type: "get",
				url: "delExport.do",
				data: {
					id: id
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					// $('#container').showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data);
					if (data.ret=="0") {
						jAlert(data.msg, "提示");
					}
					else {
						jAlert(data.msg, "提示");
						$('#tr' + id).remove();
					}
				},
				complete: function(XMLHttpRequest, status){
					// $('#container').hideLoading();
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