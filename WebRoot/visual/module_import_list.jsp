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
if ("setFieldWrite".equals(op)) {
	int id = ParamUtil.getInt(request, "id");
	String fields = ParamUtil.get(request, "fields");
	ModulePrivDb mpd = new ModulePrivDb();
	mpd = mpd.getModulePrivDb(id);
	mpd.setFieldWrite(fields);
	boolean re = mpd.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	out.print(json);
	return;
}
else if ("setFieldHide".equals(op)) {
	int id = ParamUtil.getInt(request, "id");
	String fields = ParamUtil.get(request, "fields");
	ModulePrivDb mpd = new ModulePrivDb();
	mpd = mpd.getModulePrivDb(id);
	mpd.setFieldHide(fields);
	boolean re = mpd.save();
	JSONObject json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	out.print(json);
	return;
}
String code = ParamUtil.get(request, "code");
String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计 - 导入设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
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
			
	window.location.href = "module_import_list.jsp?code=<%=code%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort;
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
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu6").className="current"; 
</script>
<div class="spacerH"></div>
<table class="percent60" width="80%" align="center">
	<tr>
		<td align="left">
			<input class="btn" name="button" type="button" onclick="javascript:location.href='module_import_add.jsp?code=${code}&formCode=${formCode}';"
								value="添加" width=80 height=20/>
		</td>
	</tr>
</table>
<table class="tabStyle_1 percent60" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tr>
      <td width="62%" class="tabStyle_1_title">名称</td>
        <td width="23%" class="tabStyle_1_title">操作</td>
    </tr>
    <q:forEach items="${items}" var="item" varStatus="status">
        <tr id="tr${item.id}">
            <td>${item.name}</td>
            <td align="center"><input class="btn" type="button" value="修改" onclick="window.location.href='module_import_edit.jsp?id=${item.id}&code=${code}&formCode=${formCode}'" />
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
				url: "module_import_del.do",
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