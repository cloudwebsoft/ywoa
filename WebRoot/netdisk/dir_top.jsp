<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>目录</title>
<LINK href="../admin/default.css" type=text/css rel=stylesheet>
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
	{
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
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.netdisk.Directory"/>
<jsp:useBean id="leafPriv" scope="page" class="com.redmoon.oa.netdisk.LeafPriv"/>
<%
String root_code = ParamUtil.get(request, "root_code");
if (root_code.equals("")) {
	// root_code = "root";
	root_code = privilege.getUser(request);
}

LeafPriv lp = new LeafPriv();
lp.setDirCode(root_code);
if (!lp.canUserModify(privilege.getUser(request))) {
	// out.print(StrUtil.makeErrMsg(privilege.MSG_INVALID));
	// return;
}
%>
<Script>
var root_code = "<%=root_code%>";
// 使框架的bottom能得到此root_code
function getRootCode() {
	return root_code;
}
</Script>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("AddChild")) {
	boolean re = false;
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
if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "delcode");
	try {
		dir.del(delcode);
		out.print(StrUtil.Alert("删除成功！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
if (op.equals("modify")) {
	boolean re = true;
	try {
		re = dir.update(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert("修改完成"));
}
if (op.equals("move")) {
	try {
		dir.move(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
if (op.equals("removecache")) {
	String curcode = ParamUtil.get(request, "code");
	LeafChildrenCacheMgr.remove(curcode);
	out.print(StrUtil.Alert(curcode + "缓存已被清除！"));
}

Leaf leaf = dir.getLeaf(root_code);
if (leaf==null || !leaf.isLoaded()) {
	// 为用户初始化网盘
	leaf = new Leaf();
	leaf.initRootOfUser(root_code);
	leaf = leaf.getLeaf(root_code);
}
String root_name = leaf.getName();
int root_layer = leaf.getLayer();
String root_description = leaf.getDescription();
boolean isHome = false;
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">管理&nbsp;<%=root_name%>&nbsp;&nbsp;<a href="netdisk_frame.jsp" target="_parent">浏览视图</a></td>
  </tr>
</table>
<br>
<TABLE class="frame_gray"  
cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD height=200 valign="top" bgcolor="#FFFBFF">
<table class="tbg1" cellspacing=0 cellpadding=0 width="100%" align=center onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" 
border=0>
          <tbody>
            <tr>
              <td width="66%" height="13" align=left nowrap>              &nbsp;&nbsp;&nbsp;&nbsp;                             </td>
            <td width="34%" align=right nowrap>>>&nbsp;<%=root_name%>&nbsp;&nbsp;<a target="_parent" href="document_list_m.jsp?dir_code=<%=StrUtil.UrlEncode(root_code)%>&dir_name=<%=StrUtil.UrlEncode(root_name)%>"></a><a href="dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(root_code)%>" target="_parent">权限</a>&nbsp;&nbsp;<a target=dirbottomFrame href="dir_bottom.jsp?parent_code=<%=StrUtil.UrlEncode(root_code, "utf-8")%>&parent_name=<%=StrUtil.UrlEncode(root_name, "utf-8")%>&op=AddChild">添子目录</a> <a target="dirbottomFrame" href="dir_bottom.jsp?op=modify&code=<%=StrUtil.UrlEncode(root_code, "utf-8")%>&name=<%=StrUtil.UrlEncode(root_name,"utf-8")%>&description=<%=StrUtil.UrlEncode(root_description,"utf-8")%>">修改</a> <!--<a target=_self href="#" onClick="if (window.confirm('您确定要删除<%=root_name%>吗?')) window.location.href='dir_top.jsp?op=del&delcode=<%=root_code%>'">删除</a>-->
			  </td>
            </tr>
          </tbody>
        </table>
<%
DirectoryView tv = new DirectoryView(leaf);
tv.list(out);
%></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
</html>
