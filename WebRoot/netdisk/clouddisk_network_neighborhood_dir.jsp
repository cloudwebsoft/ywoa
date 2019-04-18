<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.Iterator" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<LINK href="common.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>共享目录</title>
<style>
body{background:#fcfcfc}
tbody{color:#666}
</style>
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function ShowChild(imgobj, name)
{
	var tableobj = findObj("childof"+name);
	if (tableobj.style.display=="none")
	{alert(2);
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus-1-1.gif")!=-1)
			imgobj.src = "images/i_plus2-2.gif";
		if (imgobj.src.indexOf("i_plus-1.gif")!=-1)
			imgobj.src = "images/i_plus2-1.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_puls-root.gif")!=-1)
			imgobj.src = "images/i_puls-root-1.gif";
		if (imgobj.src.indexOf("i_plus2-2.gif")!=-1)
			imgobj.src = "images/i_plus-1-1.gif";
		if (imgobj.src.indexOf("i_plus2-1.gif")!=-1)
			imgobj.src = "images/i_plus-1.gif";
	}
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!-- <table width="100%"  border="0" style="background-image:url(images/bg_left.jpg); background-repeat:no-repeat">-->
  <%
	if (!privilege.isUserLogin(request)) {
		out.print("对不起，请先登录！");
		 return;
	}
	String userName = "";
	if("".equals(userName)){
		userName = privilege.getUser(request);
	}
String op = ParamUtil.get(request, "op");
if (op.equals("changeName")) {
	String newName = ParamUtil.get(request, "newName");
	String dirCode = ParamUtil.get(request, "dirCode");
	Directory dir = new Directory();
	Leaf lf = dir.getLeaf(dirCode);
	lf.setName(newName);
	lf.update();
}
if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "dirCode");
	Directory dir = new Directory();
	try {
		dir.del(delcode);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
if (op.equals("AddChild")) {
	boolean re = false;
	Directory dir = new Directory();
	try {
		re = dir.AddChild(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (!re) {
		out.print(StrUtil.Alert("添加节点失败，请检查编码是否重复！"));
	}	
}
if (op.equals("move")) {
	Directory dir = new Directory();
	try {
		dir.move(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
  <!-- <tr>
    <td height="73"><table width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td height="40">&nbsp;</td>
      </tr>
      <tr>
        <td><a href="dir_frame.jsp" target="_parent">管理视图</a>
          &nbsp;&nbsp;<a href="netdisk_dir_share.jsp?userName=<%=StrUtil.UrlEncode(privilege.getUser(request))%>">我的共享</a>
          &nbsp;<img src="images/network.gif" width="16" height="16" align="absbottom">&nbsp;<a href="netdisk_neighbor_frame.jsp" target="_parent">网络邻居</a>&nbsp;&nbsp;<img src="images/public_share.gif" width="16" height="16" align="absmiddle">&nbsp;<a href="netdisk_public_share_frame.jsp" target="_parent">公共共享</a></td>
      </tr>
    </table></td>
  </tr>
</table> -->
<%
String shareUser = ParamUtil.get(request, "shareUser");
if (shareUser.equals("")) {
	// shareUser = privilege.getUser(request);
	return;
}
UserDb ud = new UserDb();
ud = ud.getUserDb(shareUser);
%>
<table width="100%"  border="0" style="">
	<tr><td></td></tr>
  <tr>
    <td align="left" colspan="2" style="font-size:15px;background:#f7f7f7;border:solid 1px #e4e4e4"><%=ud.getRealName()%>的共享目录</td>
  </tr>
  <tr>
    <td width="5" align="left">&nbsp;</td>
  <td align="left">
<%
Leaf leaf = new Leaf();
Iterator ir = leaf.listSharedDirOfUser(shareUser).iterator();
while (ir.hasNext()) {
	leaf = (Leaf)ir.next();
	CooperateMgr cm = new CooperateMgr();
	int cooperateId = cm.getPrivId(leaf.getCode(), userName);
	if(cooperateId == 0){
		continue;
	}
%>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="20" height="22"><img src="images/folder_netdisk_share.png" align="absmiddle"></td>
    <td><a style="color:#666666" href="clouddisk_network_neighborhood_list.jsp?dir_code=<%=leaf.getCode()%>&op=editarticle" target="mainFileFrame" title="<%=leaf.getDescription()%>">&nbsp;&nbsp;<%=leaf.getName()%></a></td>
  </tr>
</table>
<%}%>  </td>
  </tr>
</table>
</body>
</html>
