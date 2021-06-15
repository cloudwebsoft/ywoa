<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.exam.MajorPriv"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@page import="com.redmoon.oa.exam.MajorView"%>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<jsp:useBean id="majorPriv" scope="page" class="com.redmoon.oa.exam.MajorPriv"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理目录权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery.bgiframe.js"></script>
</head>
<body>
<br/>
<% 
String majorCode = ParamUtil.get(request,"majorCode"); 
String op = ParamUtil.get(request, "op");
majorPriv.setMajorCode(majorCode);
String sql = "select id from oa_exam_major_priv"+ " where major_code = "+StrUtil.sqlstr(majorCode);
Vector result = null;
result = majorPriv.list(sql);
Iterator ir = result.iterator();
%>
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="right">
	    <input id ="addPriv" class="btn" name="button" type="button" onclick="javascript:location.href='major_priv_add.jsp?majorCode=<%=StrUtil.UrlEncode(majorCode)%>';" value="添加权限" width=80 height=20 />
	    <help:HelpDocTag id="915" type="content" size="200"></help:HelpDocTag>
	</td>
  </tr>
</table>
<form id="form1" name="form1" action="?op=modify" method=post>
<table class="tabStyle_1 percent98" cellspacing="0" cellpadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" nowrap width="14%" style="cursor:pointer" onclick="doSort('name')">名称
	  </td>
      <td class="tabStyle_1_title" nowrap width="13%" style="cursor:pointer" onclick="doSort('priv_type')">类型<span class="right-title" style="cursor:pointer">
      </span></td>
      <td class="tabStyle_1_title" nowrap width="14%" onclick="doSort('dir_code')" style="cursor:pointer">目录<span class="right-title" style="cursor:pointer">
      </span></td>
      <td class="tabStyle_1_title" noWrap width="20%">权限</td>
      <td width="19%" nowrap class="tabStyle_1_title">操作</td>
    </tr>
    <% 
    int i = 0;
	//Directory dir = new Directory();
	while (ir.hasNext()) {
	 	MajorPriv mp = (MajorPriv)ir.next();
	 	TreeSelectDb tsd = new TreeSelectDb();
		tsd =tsd.getTreeSelectDb(mp.getMajorCode());
		if (tsd==null)
			continue;
		i++;
		%>
    <tr class="highlight" id="tr1">
      <td>
      <%
	  if (mp.getPriveType()==majorPriv.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(mp.getName());
		out.print(ud.getRealName());
	  }else if (mp.getPriveType()==majorPriv.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(mp.getName());
	  	out.print(rd.getDesc());
	  }
       %>
      <input type=hidden name="id" value="<%=mp.getId()%>" />
      </td>
      <%
      if(mp.getPriveType()==majorPriv.TYPE_USER){
       %>
       <td>用户 </td>
       <%
      }else if(mp.getPriveType()==majorPriv.TYPE_ROLE){
      %>
      <td>角色 </td>
      <%}
       %>
      <td><%=tsd.getName() %></td>
      <td> <input id="manage<%=mp.getId() %>" name="manage" type=checkbox <%=mp.getCanManage()==1?"checked":""%> value="1" />管理题库试卷&nbsp;
	  <input style="display: none;" id ="invigilate<%=mp.getId() %>" name="invigilate" type=checkbox <%=mp.getInvigilate()==1?"checked":""%> value="1"/><span style="display: none;">监考</span></td>
      <td align="center">
	  <input class="btn" type="button" onclick="modifyPriv('<%=mp.getId() %>')" value="修改" />
	  &nbsp;<input class="btn" type="button" onclick="delPriv('<%=mp.getId() %>')" value="删除" /></td>
    </tr>
	<%}
	%>
</table>
</form>
<br/>
</body>
<%  
	TreeSelectDb tsd = new TreeSelectDb();
	tsd =tsd.getTreeSelectDb(majorCode);
%>
<script>
var majorCode = "<%=majorCode%>";
$(function(){
	var parentCode = "<%=tsd.getParentCode()%>";
	var rootCode = "<%=MajorView.ROOT_CODE%>";
	var majorCode = "<%=majorCode%>";
})
function ajaxPost(path,parameter,func){
	$.ajax({
		type: "post",
		url: path,
		data: parameter,
		dataType: "html",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",			
		success: function(data, status){
			func(data);
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});
}
function delPriv(id){
	jConfirm("您确定要删除吗?","提示",function(r){
		if(!r){
			return;
		}else{
			ajaxPost('../majorPriv/majorDel.do',{id:id},function(data){
				data = $.parseJSON(data);
				if(data.ret=="1"){
					jAlert_Redirect(data.msg,"提示","major_priv_manage.jsp?majorCode="+majorCode);
				}else if(data.ret=="0"){
					jAlert(data.msg,"提示");
				}
			});								
		}
	})
}
function modifyPriv(id){
	var manage=0;
	if($('#manage'+id).is(':checked')) {
    	manage=1;
	}
	var invigilate = 0;
	if($('#invigilate'+id).is(':checked')) {
    	invigilate=1;
	}
	ajaxPost('../majorPriv/majorModify.do',{id:id,manage:manage,invigilate:invigilate},function(data){
		data = $.parseJSON(data);
		if(data.ret=="1"){
			jAlert_Redirect(data.msg,"提示","major_priv_manage.jsp?majorCode="+majorCode);
		}else if(data.ret=="0"){
			jAlert(data.msg,"提示");
		}
	});	
}
</script>
</html>