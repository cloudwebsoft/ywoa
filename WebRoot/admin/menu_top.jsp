<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.ui.menu.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String root_code = ParamUtil.get(request, "root_code");
if (root_code.equals("")) {
	root_code = "root";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>菜单管理-top</title>
<style>
a.pre:link {
color:blue;
}
a.pre:visited {
color:blue;
}
</style>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

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
	if (tableobj==null)
		return;
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

// 折叠目录
function shrink() {
   for(var i=0; i<document.images.length; i++) {
		var imgObj = document.images[i];
		try {
			if (imgObj.tableRelate!="") {
				ShowChild(imgObj, imgObj.tableRelate);
			}
		}
		catch (e) {
		}
   }
}
jQuery(document).ready(function(){
	jQuery("#childoftablebottom").find("td").find("img:last").css("background-color","#016fe0");

})
</script>
</head>
<body onload="shrink()">
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.ui.menu.Directory"/>
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
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
}
else if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "delcode");
	try {
		dir.del(delcode);
		out.print(StrUtil.jAlert(SkinUtil.LoadString(request, "res.label.forum.admin.menu_bottom", "del_success"),"提示"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
}
else if (op.equals("modify")) {
	boolean re = true;
	try {
		re = dir.update(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	if (re)
		out.print(StrUtil.jAlert(SkinUtil.LoadString(request, "res.label.forum.admin.menu_bottom", "edit_success"),"提示"));
}
else if (op.equals("move")) {
	try {
		dir.move(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
}
else if (op.equals("removecache")) {
	String curcode = ParamUtil.get(request, "code");
	LeafChildrenCacheMgr.remove(curcode);
	out.print(StrUtil.jAlert(curcode + SkinUtil.LoadString(request, "res.label.forum.admin.menu_bottom", "cache_remove"),"提示"));
}
else if (op.equals("moveTo")) {
	String sourceItem = ParamUtil.get(request, "sourceItem");
	String destItem = ParamUtil.get(request, "destItem");
	Leaf slf = new Leaf();
	slf = slf.getLeaf(sourceItem);
	Leaf dlf = new Leaf();
	dlf = dlf.getLeaf(destItem);
	
	Leaf plf = new Leaf();
	plf = plf.getLeaf(dlf.getParentCode());
	
	int dorders = dlf.getOrders();
	Iterator ir = plf.getChildren().iterator();
	while (ir.hasNext()) {
		Leaf lf = (Leaf)ir.next();
		// 所有比目标菜单orders大的节点下移一位
		if (lf.getOrders()>dorders) {
			lf.setOrders(lf.getOrders() + 1);
			lf.update();
		}
	}
	
	slf.setOrders(dorders + 1);
	slf.setLayer(dlf.getLayer());
	slf.setParentCode(dlf.getParentCode());
	slf.update();
	
	Leaf rootlf = new Leaf();
	rootlf = rootlf.getLeaf(Leaf.CODE_ROOT);
	dir.repairTree(rootlf);

	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "menu_top.jsp?root_code=" + root_code));
	return;
}

Leaf leaf = dir.getLeaf(root_code);
if (op.equals("repair")) {
	dir.repairTree(leaf);
	leaf = dir.getLeaf(root_code);
}
String root_name = leaf.getName();
int root_layer = leaf.getLayer();
String root_link = leaf.getLink();
boolean isHome = false;
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="tdStyle_1"><lt:Label res="res.label.forum.admin.menu_bottom" key="manage"/>&nbsp;<%=root_name%></td>
  </tr>
</table>
<br>
<TABLE cellSpacing=0 cellPadding=0 width="95%" align=center style="line-height:30px;">
  <TBODY>
    <TR>
      <TD height=200 valign="top" style="font-family:'微软雅黑';font-size:12px;">
<table class="tbg1" cellspacing=0 cellpadding=0 width="100%" align=center onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" border=0>
          <tbody>
            <tr>
              <td width="66%" height="13" align=left nowrap>
				&nbsp;
              </td>
            <td width="34%" align=right nowrap style="font-family:'微软雅黑';font-size:12px;">
            >>&nbsp;<a href="menu_top.jsp?op=repair&root_code=<%=root_code%>"><lt:Label res="res.label.forum.admin.menu_bottom" key="repair"/></a>
            &nbsp;&nbsp;<!--<a href="dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(root_code)%>" target="_parent">权限</a>--><a target=dirbottomFrame href="menu_bottom.jsp?parent_code=<%=StrUtil.UrlEncode(root_code, "utf-8")%>&parent_name=<%=StrUtil.UrlEncode(root_name, "utf-8")%>&op=AddChild"><lt:Label res="res.label.forum.admin.menu_bottom" key="add_dir"/></a>
            &nbsp;&nbsp;<a target="dirbottomFrame" href="menu_bottom.jsp?op=modify&code=<%=StrUtil.UrlEncode(root_code, "utf-8")%>&name=<%=StrUtil.UrlEncode(root_name,"utf-8")%>&link=<%=StrUtil.UrlEncode(root_link,"utf-8")%>">修改</a> <!--<a target=_self href="#" onClick="if (window.confirm('您确定要删除<%=root_name%>吗?')) window.location.href='dir_top.jsp?op=del&delcode=<%=root_code%>'">删除</a>-->
            &nbsp;&nbsp;<a href="javascript:;" onclick="moveTo()">移动</a>
            &nbsp;&nbsp;<a href="menu_top.jsp?root_code=<%=root_code%>">刷新</a>
			  </td>
            </tr>
          </tbody>
        </table>
<%
DirectoryView tv = new DirectoryView(request, leaf);
tv.list(out);
%></TD>
    </TR>
  </TBODY>
</TABLE>

<div id="dlg" style="display:none">
<form id="frmMove" action="menu_top.jsp" method="post">
<%
StringBuffer sb = new StringBuffer();
tv.ShowDirectoryAsOptionsToString(sb, leaf, leaf.getLayer());
%>
将菜单项
<select id="sourceItem" name="sourceItem">
<%=sb%>
</select>
<br />
<br />
移至
<select id="destItem" name="destItem">
<%=sb%>
</select>
之后
<input name="root_code" value="<%=root_code%>" type="hidden" />
<input name="op" value="moveTo" type="hidden" />
</form>
</div>

</body>
<script>
function moveTo() {
  $("#dlg").dialog({
	  title: "移动菜单项的位置",
	  modal: true,
	  // bgiframe:true,
	  buttons: {
		  "取消": function() {
			  $(this).dialog("close");
		  },
		  "确定": function() {
			  if ($("#sourceItem").val()=="<%=Leaf.CODE_ROOT%>") {
				  jAlert("请选择菜单项！","提示");
			  }
			  else if ($("#destItem").val()=="<%=Leaf.CODE_ROOT%>") {
				  jAlert("请选择将移动至其后的菜单项！","提示");
			  }
			  else if ($("#sourceItem").val()==$("#destItem").val()) {
			  	  jAlert("菜单项不能相同！","提示");
			  }
			  else {
				  $("#frmMove").submit();
				  $(this).dialog("close");
			  }
		  }
	  },
	  closeOnEscape: true,
	  draggable: true,
	  resizable:true,
	  width:300					
	  });
}
</script>
</html>
