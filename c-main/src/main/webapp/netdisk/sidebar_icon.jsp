<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@page import="com.redmoon.oa.netdisk.Attachment"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.redmoon.oa.netdisk.SideBarMgr"%>
<jsp:useBean id="privilege" scope="page"
			class="com.redmoon.oa.pvg.Privilege" />
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
	if (!privilege.isUserLogin(request)) {
		out.print("对不起，请先登录！");
		 return;
		}
	String priv="read";
	if (!privilege.isUserPrivValid(request,priv)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
    	String userName = ParamUtil.get(request,"userName"); 
    	if("".equals(userName)){
		userName = privilege.getUser(request);
	}
	String skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
	SkinMgr skm = new SkinMgr();
	Skin skin = skm.getSkin(skincode);
	String skinPath = skin.getPath();
%>
<html><head><title>选择图标</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js"	type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<style>
td {
	font-size:10pt;
}
</style>
<script>
function changeface(face){
	window.opener.selIcon(face);
	window.close();
}
function diyChangeface(fileId){
	jConfirm("你要选择这张图吗？", "提示", function(r){
			if(!r){
			}else{
				$.ajax({
					type:"get",
					url:"clouddisk_list_do.jsp",
					data:{"op":"sidebar_diyImg","userName":"<%=userName%>", "fileId": fileId },
					success:function(data,status){
						data = $.parseJSON(data);
						if(data.ret == "1"){
							window.opener.selIcon(data.defaultPath);
							window.close();
						}
					},
					error:function(XMLHttpRequest, textStatus){
						alert(XMLHttpRequest.responseText);
					}
				});
			}
	})
}

</script>
</head><div align="center"><center>
</center>

<body class="menu_sel_body"> 
<%
com.redmoon.forum.ui.FileViewer fileViewer = new com.redmoon.forum.ui.FileViewer(Global.getAppPath() + "netdisk/images" + "/appImages/");
fileViewer.init();
%>
<table  width="60%" class="tTable"><center>
<thead>
<tr>
  <td align=center class="tTd">请点击选择图标</td>
  </tr >
</thead>
<tr>
<td valign="center">
<table  width="100%" class="cTable">
<tbody>
<%
int k=0;
int z=0;
while(fileViewer.nextFile()){
  if ((fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1) && fileViewer.getFileName().indexOf("_default") != -1) {
	if (k==0)
		out.print("<tr align=center style='height:50px;'>");
	String fileName = fileViewer.getFileName();
%>
	<td>&nbsp;<img style="cursor:pointer;" onClick="changeface('<%=fileName%>')" alt='<lt:Label res="res.label.forum.user" key="check_selected"/>' src="<%=request.getContextPath()%>/netdisk/images/appImages/<%=fileViewer.getFileName()%>" border="0"/></td>		
<%
	k++;	
	if (k==10)
		out.write("</tr>");
	if (k==10) k=0;
 }
}%>
</tbody>
</table></td>
</tr>

<thead>
<tr>
  <td align=center class="tTd">请点击选择网盘内的图标<span style="font-size:14px;">（只显示长宽相等的图片）</span></td>
</tr >
</thead>
<tr>
<td valign="center">
<table  width="100%" class="cTable">
<tbody>
	<% SideBarMgr sbm = new SideBarMgr(); 
	   ArrayList ri = sbm.getSidebarIcon(userName);
	   com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String file_netdisk = cfg.get("file_netdisk");
		
	  	for(int i = 0 ; i<ri.size() ; i++){
	  		if (z==0) {
				out.print("<tr align=center style='height:55px;'>");
			}
	  		Attachment att = new Attachment((Integer)ri.get(i));
	  		%>
	  		<td style="margin-top:5px;">&nbsp;<img style="cursor:pointer;" onClick="diyChangeface('<%=att.getId() %>')" alt='<lt:Label res="res.label.forum.user" key="check_selected"/>' src="getfile.jsp?op=1&id=<%=att.getId() %>" border="0" width="36px" height="36px"/></td>	
	  		<%
	  		z++;
	  		if(z==10){
	  			out.print("</tr>");
	  		}
	  		if(z==10) z=0;
	  	}
	  	
	%>
</tbody>
</table></td>
</tr>
</center>
</table>
</div>
</body>