<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.netdisk.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String root_code = ParamUtil.get(request, "root_code");
if (root_code.equals("")) {
	// root_code = "root";
	root_code = privilege.getUser(request);
}
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	boolean re = true;
	try {
		Directory dir = new Directory();
		re = dir.update(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re) {
		String code = ParamUtil.get(request, "code");
		%>
		<script>
		window.parent.mainFileFrame.location.href = "dir_prop.jsp?op=modify&code=<%=StrUtil.UrlEncode(code)%>";
		</script>
		<%
		out.print(StrUtil.Alert_Redirect("修改完成！", "netdisk_left.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
	}
	return;
}
else if (op.equals("repair")) {
	Directory dir = new Directory();
	dir.repairTree(dir.getLeaf(root_code));
	out.print(StrUtil.Alert_Redirect("操作完成！", "netdisk_left.jsp?root_code=" + StrUtil.UrlEncode(root_code)));
	return;
}
%>
<html>
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<LINK href="common.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>目录</title>
<style>
<!--
body {
	margin-left: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
	overflow: auto;
}
.skin0 {
padding-top:2px;
cursor:default;
font:menutext;
position:absolute;
text-align:left;
font-family: "宋体";
font-size: 9pt;
width:80px;              /*宽度，可以根据实际的菜单项目名称的长度进行适当地调整*/
background-color:menu;    /*菜单的背景颜色方案，这里选择了系统默认的菜单颜色*/
border:1 solid buttonface;
visibility:hidden;        /*初始时，设置为不可见*/
border:2 outset buttonhighlight;
}

/*定义菜单条的显示样式*/
.menuitems {
padding:2px 1px 2px 10px;
}
-->
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

var curDirCode = "";
var curDirName = "";

var curLeftClickDirCodeOld = "";
var curLeftClickDirCode = "";
var oldsrc = "";

function onMouseUp(dirCode, dirName) {
	if (event.button==1) {
		// 点击左键时切换folder图片
		curLeftClickDirCode = dirCode;
		if (curLeftClickDirCodeOld!=curLeftClickDirCode) {
			var curImgObj = findObj("img" + curLeftClickDirCode);
			var oldImgObj = findObj("img" + curLeftClickDirCodeOld);
			if (oldImgObj!=null) {
				oldImgObj.src = oldsrc; // "images/folder_01.gif";
			}
			oldsrc = curImgObj.src;

			if (curImgObj.src.indexOf("images/folder_share.gif")==-1)
				curImgObj.src = "images/folder_open.gif";
			else
				curImgObj.src = "images/folder_share_open.gif";
			
			curLeftClickDirCodeOld = curLeftClickDirCode;
		}
	}
	if (event.button==2) {
		curDirCode = dirCode;
		curDirName = dirName;
	}
}

var spanInnerHTML = "";
function changeName() {
	if (curDirCode!="") {
		// alert(curDirCode);
		spanObj = findObj("span" + curDirCode);
		spanInnerHTML = spanObj.innerHTML;
		spanObj.innerHTML = "<input name='newName' class=singleboarder size=10 value='" + curDirName + "' onblur=\"doChange('" + curDirCode + "',this,'" + curDirName + "'," + spanObj.name + ")\" onKeyDown=\"if (event.keyCode==13) this.blur()\">";
		newName.focus();
		newName.select();
	}
}

function doChange(dirCode, newName, oldName, spanObj) {
	if (newName.value=="") {
		alert("目录名称不能为空！");
		return;
	}
	if (newName.value!=oldName) {
		form10.op.value = "changeName";
		form10.dirCode.value = dirCode;
		form10.newName.value = newName.value;
		form10.root_code.value = "<%=root_code%>";
		form10.submit();
		// 下句发过去会有中文问题
		// window.location.href="?op=changeName&dirCode=" + dirCode + "&newName=" + newName + "&root_code=<%=StrUtil.UrlEncode(root_code)%>";
		// alert(window.location.href);
	}
	else {
		spanObj.innerHTML = spanInnerHTML;
	}
	curDirCode = "";
}

function share() {
	if (curDirCode!="")
		window.parent.mainFileFrame.location.href = "dir_priv_m.jsp?dirCode=" + curDirCode;
	curDirCode = "";
}

function del() {
	if (curDirCode!="") {
		if (confirm("您确定要删除文件夹“" + curDirName + "”吗？"))
			window.location.href="?op=del&dirCode=" + curDirCode + "&root_code=<%=StrUtil.UrlEncode(root_code)%>";
		curDirCode = "";
	}
}

function create() {
	if (curDirCode!="") {
		window.location.href="?op=AddChild&type=<%=Leaf.TYPE_DOCUMENT%>&parent_code=" + curDirCode + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&code=<%=Leaf.getAutoCode()%>&name=<%=StrUtil.UrlEncode("新建文件夹")%>";
		curDirCode = "";
	}
}

function search() {
	window.location.href = "netdisk_search.jsp?dirCode=" + curDirCode;
}

function move(towhere) {
	if (curDirCode!="") {
		window.location.href = "?op=move&direction=" + towhere + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&code=" + curDirCode;
		curDirCode = "";
	}
}

function props() {
	if (curDirCode!="")
		window.parent.mainFileFrame.location.href = "dir_prop.jsp?op=modify&code=" + curDirCode;
	curDirCode = "";
}
</script>
</head>
<body onLoad="window_onload()">
<table width="100%"  border="0" style="background-image:url(images/bg_left.jpg); background-repeat:no-repeat">
<%
String priv = "read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
else if (op.equals("changeName")) {
	String newName = ParamUtil.get(request, "newName");
	String dirCode = ParamUtil.get(request, "dirCode");
	Directory dir = new Directory();
	Leaf lf = dir.getLeaf(dirCode);
	Leaf plf = dir.getLeaf(lf.getParentCode());
	if (plf!=null) {
		java.util.Iterator ir = plf.getChildren().iterator();
		boolean isFound = false;
		while (ir.hasNext()) {
			Leaf lf2 = (Leaf)ir.next();
			if (lf2.getName().equals(newName)) {
				isFound = true;
				break;
			}
		}
		if (isFound) {
			out.print(StrUtil.Alert_Back("指定的文件夹与现有文件夹重名！"));
			return;
		}
	}
	lf.rename(newName);
}
else if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "dirCode");
	UserMgr um = new UserMgr();
	UserDb ud = um.getUserDb(privilege.getUser(request));
	if (delcode.equals("" + ud.getName())) {
		out.print(StrUtil.Alert("根目录不能被删除"));
	}
	else {	
		Directory dir = new Directory();
		Leaf lf = dir.getLeaf(delcode);
		if (lf!=null) { // 防止反复刷新
			try {
				dir.del(delcode);
			}
			catch (ErrMsgException e) {
				out.print(StrUtil.Alert(e.getMessage()));
			}
			response.sendRedirect("netdisk_left.jsp");
			return;
		}
	}
}
else if (op.equals("AddChild")) {
	boolean re = false;
	Directory dir = new Directory();
	try {
		re = dir.AddChild(request);
		if (re) {
			response.sendRedirect("netdisk_left.jsp?root_code=" + StrUtil.UrlEncode(root_code));
			return;
		}		
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (!re) {
		out.print(StrUtil.Alert_Back("添加节点失败，请检查编码是否重复！"));
		return;
	}
}
else if (op.equals("move")) {
	Directory dir = new Directory();
	try {
		dir.move(request);
		if (true) {
			response.sendRedirect("netdisk_left.jsp?root_code=" + StrUtil.UrlEncode(root_code));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
  <tr>
    <td height="73"><table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td height="40" style="FILTER: glow(color=ffffff)">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<strong><font color="#0099FF">网络硬盘</font></strong></td>
        </tr>
        <tr>
          <td>
		  <!--<a href="dir_frame.jsp" target="_parent">管理视图</a>
          &nbsp;&nbsp;<a href="netdisk_dir_share.jsp?userName=<%=StrUtil.UrlEncode(privilege.getUser(request))%>">我的共享</a>-->&nbsp;<img src="images/network.gif" width="16" height="16" align="absbottom">&nbsp;<a href="netdisk_neighbor_frame.jsp" target="_parent">网络邻居</a>&nbsp;&nbsp;<img src="images/public_share.gif" width="16" height="16" align="absmiddle">&nbsp;<a href="netdisk_public_share_frame.jsp" target="_parent">公共共享</a></td>
        </tr>
        <tr>
          <td>
		  &nbsp;<img src="images/network.gif" width="16" height="16" align="absbottom">&nbsp;<a href="netdisk_recycle_frame.jsp" target="_parent">回收站</a>&nbsp;&nbsp;<img src="images/folder_share.gif" width="16" height="16" align="absmiddle">&nbsp;<a href="netdisk_kp_share_frame.jsp" target="_parent">分享管理</a></td>
        </tr>
    </table></td>
  </tr>
</table>
<table width="100%"  border="0">
  <tr>
    <td width="5" align="left">&nbsp;</td>
  <td align="left"><%
Directory dir = new Directory();
Leaf leaf = dir.getLeaf(root_code);
DirectoryView tv = new DirectoryView(leaf);
String mode = ParamUtil.get(request, "mode");

UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));

String pageUrl = usd.isWebedit()?"dir_list.jsp":"dir_list_new.jsp";

tv.ListSimple(out, "mainFileFrame", pageUrl, "op=editarticle&mode=" + mode, "", "" ); // "tbg1", "tbg1sel");
%></td>
  </tr>
</table>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
<form name=form10 action="?">
<tr><td>&nbsp;
<input name="op" type="hidden">
<input name="dirCode" type="hidden">
<input name="newName" type="hidden">
<input name="root_code" type="hidden">
</td></tr>
</form>
</table>
<div id="ie5menu" class="skin0" onMouseover="highlightie5(event)" onMouseout="lowlightie5(event)" onClick="jumptoie5(event)" display:none>
<div class="menuitems" url="javascript:changeName()">重命名</div>
<div class="menuitems" url="javascript:share()">共享</div>
<div class="menuitems" url="javascript:del()">删除</div>
<div class="menuitems" url="javascript:create()">新建目录</div>
<div class="menuitems" url="javascript:move('up')">上移</div>
<div class="menuitems" url="javascript:move('down')">下移</div>
<div class="menuitems" url="javascript:search()">搜索</div>
<div class="menuitems" url="javascript:props()">属性</div>
<hr>
<div class="menuitems" url="netdisk_left.jsp?root_code=<%=StrUtil.UrlEncode(root_code)%>">刷新</div>
</div>

<script language="JavaScript1.2">

//set this variable to 1 if you wish the URLs of the highlighted menu to be displayed in the status bar
var display_url=0

var ie5=document.all&&document.getElementById
var ns6=document.getElementById&&!document.all
if (ie5||ns6)
var menuobj=document.getElementById("ie5menu")

function showmenuie5(e){
if (curDirCode=="")
	return;
//Find out how close the mouse is to the corner of the window
var rightedge=ie5? document.body.clientWidth-event.clientX : window.innerWidth-e.clientX
var bottomedge=ie5? document.body.clientHeight-event.clientY : window.innerHeight-e.clientY

//if the horizontal distance isn't enough to accomodate the width of the context menu
if (rightedge<menuobj.offsetWidth)
//move the horizontal position of the menu to the left by it's width
menuobj.style.left=ie5? document.body.scrollLeft+event.clientX-menuobj.offsetWidth : window.pageXOffset+e.clientX-menuobj.offsetWidth
else
//position the horizontal position of the menu where the mouse was clicked
menuobj.style.left=ie5? document.body.scrollLeft+event.clientX : window.pageXOffset+e.clientX

//same concept with the vertical position
if (bottomedge<menuobj.offsetHeight)
menuobj.style.top=ie5? document.body.scrollTop+event.clientY-menuobj.offsetHeight : window.pageYOffset+e.clientY-menuobj.offsetHeight
else
menuobj.style.top=ie5? document.body.scrollTop+event.clientY : window.pageYOffset+e.clientY

menuobj.style.visibility="visible"
return false
}

function hidemenuie5(e){
menuobj.style.visibility="hidden"
}

function highlightie5(e){
var firingobj=ie5? event.srcElement : e.target
if (firingobj.className=="menuitems"||ns6&&firingobj.parentNode.className=="menuitems"){
if (ns6&&firingobj.parentNode.className=="menuitems") firingobj=firingobj.parentNode //up one node
firingobj.style.backgroundColor="highlight"
firingobj.style.color="white"
if (display_url==1)
window.status=event.srcElement.url
}
}

function lowlightie5(e){
var firingobj=ie5? event.srcElement : e.target
if (firingobj.className=="menuitems"||ns6&&firingobj.parentNode.className=="menuitems"){
if (ns6&&firingobj.parentNode.className=="menuitems") firingobj=firingobj.parentNode //up one node
firingobj.style.backgroundColor=""
firingobj.style.color="black"
window.status=''
}
}

function jumptoie5(e){
var firingobj=ie5? event.srcElement : e.target
if (firingobj.className=="menuitems"||ns6&&firingobj.parentNode.className=="menuitems"){
if (ns6&&firingobj.parentNode.className=="menuitems") firingobj=firingobj.parentNode
if (firingobj.getAttribute("target"))
window.open(firingobj.getAttribute("url"),firingobj.getAttribute("target"))
else
window.location=firingobj.getAttribute("url")
}
}

if (ie5||ns6){
menuobj.style.display=''
document.oncontextmenu=showmenuie5
document.onclick=hidemenuie5
}

function handlerOnClick() {
	var obj = window.event.srcElement;
	alert(obj.type);
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

function window_onload() {
	shrink();
	// window.document.body.onclick = handlerOnClick;
}
</script>
</body>
</html>