<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String unitCode = ParamUtil.get(request, "unitCode");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "unitCode", unitCode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}
if (unitCode.equals("")) {
	// String unitCode = privilege.getUserUnitCode(request);
	unitCode = DeptDb.ROOTCODE;
}

DeptMgr dm = new DeptMgr();
DeptDb dd = dm.getDeptDb(unitCode);
DeptView tv = new DeptView(dd);
String jsonData = tv.getJsonString();
//tv.ListFuncAjaxString(request, sb, "", "updateResults", "", "", true);
int pagesize = 10;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<base target="_self">
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache,must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>选择用户</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=Global.getRootPath(request) %>/netdisk/clouddisk.css" />
<script src='../inc/common.js'></script>
<script src='../dwr/interface/DeptUserDb.js'></script>
<script src='../dwr/engine.js'></script>
<script src='../dwr/util.js'></script>
<script src="../js/jquery.my.js"></script>
<script src="../js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css" />
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<style type="text/css">
#floater {
	margin:0px;
	cursor:pointer;
	top: 141px;
	width: 122px;
	visibility: visible;
	height: 222px;
	background-color: #fefefe;
	border:1px solid #cccccc;
}

.floaterTitle {
	font-weight:bold;
	background-color:#eeeeee;
}

.activePageNum {
	color:red;
	font-weight:bold;
	font-size:14px;
}
.tbg1 tr td {border:0px}
.dirTable td {width:30%}
#deptTree a{ text-decoration:none; }
-->
</style>
<script>
var deptCode;
jQuery(function(){
	jQuery('#floater').css({'left':jQuery(window).width() - jQuery('#floater').width() - 8});
	jQuery(".userList").bind("mouseenter",function(){
		jQuery(this).find("td").css({"background":"#f0f8fd"});
	});
	jQuery(".userList").bind("mouseleave",function(){
		jQuery(this).find("td").css({"background":""});
	});
	jQuery('#directoryTree').jstree({
    	"core" : {
            "data" :  <%=jsonData%>,
            "themes" : {
			   "theme" : "default" ,
			   "dots" : true,  
			   "icons" : true  
			},
			"check_callback" : true,	
 		},
 		"plugins" : ["wholerow", "themes", "ui", ,"types","state"],
	}).bind('select_node.jstree', function (e, data) {//绑定选中事件
			deptCode = data.node.id;
			updateResults();
		});
	
})
var allUserOfDept="";
var allUserRealNameOfDept = "";
function updateResults() {
	//o("selDeptAllBtn").style.display = "";
	DWRUtil.removeAllRows("postsbody");
	allUserOfDept="";
	allUserRealNameOfDept = "";
	if(deptCode=="<%=DeptDb.ROOTCODE%>"){
		document.getElementById("deptUsers").style.display = "";
		document.getElementById("pagesize").style.display = "";
		document.getElementById("searchIt").style.display = "";
		ajaxExchange(1, <%=pagesize%>);
	}else{
		document.getElementById("deptUsers").style.display = "";
		document.getElementById("pagesize").style.display = "";
		// document.getElementById("searchIt").style.display = "none";
		//DeptUserDb.list2DWR(fillTable, deptCode);
		ajaxList(1, <%=pagesize%>);
	}
	o("resultTable").style.display = '';
}

function initPages() {
   //var dlg = window.opener ? window.opener : window.dialogArguments;
  // if (typeof(dlg.getDept)!="undefined") {
	//   var depts = dlg.getDept().trim();
	//   if (depts!="")
	//	   depts = "," + depts + ",";
	   // 如果有部门选择的权限控制，且其中没有根部门，则不允许显示
	//   if (depts!="" && depts.indexOf(",<%=DeptDb.ROOTCODE%>,")==-1)
		//  return;
  // }
	 
   document.getElementById("deptUsers").style.display = "";
   document.getElementById("pagesize").style.display = "";
   document.getElementById("searchIt").style.display = "";
   ajaxExchange(1, <%=pagesize%>);
}

var getCode = function(unit) { return unit.deptCode };
var getName = function(unit) { return unit.deptName };
var getUserName = function(unit) { 
	if (unit.userName!=null) {
		if (allUserOfDept=="") {
		  allUserOfDept = unit.userName;
		  allUserRealNameOfDept = unit.userRealName;
		}
		else {
		  allUserOfDept += "," + unit.userName;
		  allUserRealNameOfDept += "、"  + unit.userRealName;
		}
	}
	var u = unit.userRealName; //职员姓名
	if (u!=null && u!="")
	  return "<a href=# onClick=\"selPerson('" + unit.deptCode + "', '" + unit.deptName + "', '" + unit.userName + "','" + unit.userRealName + "')\">" + u + "</a>" 
	else
	  return "无";
};

var getCancelSelUser = function(unit) {  //取消选择
	var u = unit.userName;
	if (u!=null && u!="")
	  return "[<a href=# onClick=\"cancelSelPerson('" + unit.deptCode + "', '" + unit.deptName + "', '" + unit.userName + "','" + unit.userRealName + "')\">取消选择</a>]" 
	else
	  return "无";
}

function fillTable(apartment) {//alert(document.getElementById("postsbody").innerHTML);
  DWRUtil.addRows("postsbody", apartment, [ getName, getUserName, getCancelSelUser ]);
}

function setUsers() {
  // window.returnValue = users.innerText;
  var s = "";
  var ary = userRealNames.innerText.split("、");
  
  for(var i=0;i<ary.length;i++) {
	  if(s == "") {
		  var offset = ary[i].length;
		  if(ary[i].indexOf("[×]")!=-1){
			  offset = ary[i].length - 3;
		  }
		  s += ary[i].substr(0,offset);
	  } else {
		  var offset = ary[i].length;
		  if(ary[i].indexOf("[×]")!=-1){
			  offset = ary[i].length - 3;
		  }
		  s += "、" + ary[i].substr(0,offset);
	  }
  }
  
  s = s.replace(/、/g,",");//alert(s);
  try
  {
  var dlg = window.opener ? window.opener : dialogArguments;
  <%
  // 不能直接调用dlg.setPerson，因为有可能是从表单中UserSelectWinCtl调用
  String isForm = ParamUtil.get(request, "isForm");
  if (isForm.equals("true")) {
	  %>
	  dlg.setIntpuObjValue(users.innerText, s);
	  <%
  }
  else {
  %>
	  dlg.setUsers(users.innerText, s);
  <%}%>
   }
   catch(e)
   {
   
   }

  window.close();
}

function initUsers() {
  var dlg = window.opener ? window.opener : window.dialogArguments;
  var ary, aryRealName, selUserNames;
  <%
  // 不能直接调用dlg.setPerson，因为有可能是从表单中UserSelectWinCtl调用
  if (isForm.equals("true")) {
	  %>
	  //selUserNames = dlg.getMultiSelUserNames();
	//  users.innerText = selUserNames;
	  //ary = selUserNames.split(",");
	  //aryRealName = dlg.getMultiSelUserRealNames().split(",");
	  <%
  }
  else {
  %>
  	 // selUserNames = dlg.getSelUserNames();
	 // users.innerText = selUserNames;
	 // ary = selUserNames.split(",");
  	 // aryRealName = dlg.getSelUserRealNames().split(",");	
  	  //alert(aryRealName);  alert(selUserNames);
  <%}%>
    
  var s = "";
  /***if(selUserNames!="") {
	  for(var i=0;i<aryRealName.length;i++) {
		  if(s == "") {
			  s += aryRealName[i]+ "<a href=\"#\" onClick=\"cancelSelPerson('','','" + ary[i] + "','" + aryRealName[i] + "')\">[×]</a>";
		  } else {
			  s += "、" + aryRealName[i] + "<a href=\"#\" onClick=\"cancelSelPerson('','','" + ary[i] + "','" + aryRealName[i] + "')\">[×]</a>";
		  }
	  }
  }
  userRealNames.innerHTML = s;
  */
  initDepts();

  initPages();
}

function initDepts() {
 // 初始化可以选择的部门
 try {
  	 //var dlg = window.opener ? window.opener : dialogArguments;
	 
	 var depts = dlg.getDept().trim();
	 if (depts!="" && depts!='<%=DeptDb.ROOTCODE%>') {
		 var ary = depts.split(",");
		 var isFinded = true;
		 isFinded = false;
		 var len = document.getElementById('deptTree').getElementsByTagName('a').length;
		 for(var i=0; i<len; i++) {
			  try {
				  var aObj = document.getElementById('deptTree').getElementsByTagName('a')[i];//tags('A')[i];
				  var canSel = false;
				  for (var j=0; j<ary.length; j++) {
					  if (aObj.outerHTML.indexOf("'" + ary[j] + "'")!=-1) {
						  canSel = true;
						  // alert(canSel);
						  break;
					  }
				  }
				  if (!canSel) {
					  aObj.innerHTML = "<font color='#888888'>" + aObj.innerText + "</font>";
					  aObj.outerHTML = aObj.outerHTML.replace(/onClick/gi, "''");
				  }
					  
				  isFinded = true;
			  }
			  catch (e) {}
		 }
	 }
 }
 catch (e) {}	  
}

function selPerson(deptCode, deptName, userName, userRealName) {
  // 检查用户是否已被选择
  if (users.innerText.indexOf(userName)!=-1) {
	  alert("用户" + userRealName + "已被选择！");
	  return;
  }
  if (users.innerText=="") {
	  users.innerText += userName;
	  userRealNames.innerHTML += userRealName;
	  userRealNames.innerHTML += "<a href=\"#\" onClick=\"cancelSelPerson('" + deptCode + "', '" + deptName + "', '" + userName + "','" + userRealName + "')\">[×]</a>";
  }
  else {
	  users.innerText += "," + userName;
	  userRealNames.innerHTML += "、" + userRealName;
	  userRealNames.innerHTML += "<a href=\"#\" onClick=\"cancelSelPerson('" + deptCode + "', '" + deptName + "', '" + userName + "','" + userRealName + "')\">[×]</a>";
  }
}

function cancelSelPerson(deptCode, deptName, userName) {
  // 检查用户是否已被选择
  var strUsers = users.innerText;
  if (strUsers=="")
	  return;
  if (strUsers.indexOf(userName)==-1) {
	  return;
  }
  
  var strUserRealNames = userRealNames.innerText;
  var ary = strUsers.split(",");
  var aryRealName = strUserRealNames.split("、");
  var len = ary.length;
  var ary1 = new Array();
  var aryRealName1 = new Array();
  var k = 0;
  for (i=0; i<len; i++) {
	  if (ary[i]!=userName) {
		  ary1[k] = ary[i];
		  aryRealName1[k] = aryRealName[i];
		  k++;
	  }
  }
  var str = "";
  var str1 = "";
  for (i=0; i<k; i++) {
	  if (str=="") {
		  str += ary1[i];
		  if(aryRealName1[i].indexOf("[×]")!=-1) {
			  var offset = aryRealName1[i].length - 3;
			  str1 += aryRealName1[i].substr(0,offset);
		  } else {
			  str1 += aryRealName1[i];
		  }
		  str1 += "<a href=\"#\" onClick=\"cancelSelPerson('" + deptCode + "', '" + deptName + "', '" + ary1[i] + "','" + aryRealName1[i] + "')\">[×]</a>";
	  }
	  else {
		  str += "," + ary1[i];
		  if(aryRealName1[i].indexOf("[×]")!=-1) {
			  var offset = aryRealName1[i].length - 3;
			  str1 += "、" + aryRealName1[i].substr(0,offset);
		  } else {
			  str1 += "、" + aryRealName1[i];
		  }
		  str1 += "<a href=\"#\" onClick=\"cancelSelPerson('" + deptCode + "', '" + deptName + "', '" + ary1[i] + "','" + aryRealName1[i] + "')\">[×]</a>";
	  }
  }
  users.innerText = str;
  userRealNames.innerHTML = str1;
}

function selAllUserOfDept() {
  if (allUserOfDept=="")
	  return;
  var allusers = users.innerText;
  var allUserRealNames = userRealNames.innerText;
  if (allusers=="") {
	  allusers += allUserOfDept;
	  var ary = allUserOfDept.split(",");
	  var aryRealName = allUserRealNameOfDept.split("、");
	  var s = "";
	  for(var i=0;i<aryRealName.length;i++) {
		  if(s=="") {
			  s += aryRealName[i] + "<a href=\"#\" onClick=\"cancelSelPerson('','','" + ary[i] + "','" + aryRealName[i] + "')\">[×]</a>";
		  } else {
			  s += "、" + aryRealName[i] + "<a href=\"#\" onClick=\"cancelSelPerson('','','" + ary[i] + "','" + aryRealName[i] + "')\">[×]</a>";;
		  }
	  }
	  allUserRealNames += s;
	  //alert(s);
  }
  else {
	  allusers += "," + allUserOfDept;
	  var ary = allUserOfDept.split(",");
	  var aryRealName = allUserRealNameOfDept.split("、");
	  var s = "";
	  for(var i=0;i<aryRealName.length;i++) {
		  if(s=="") {
			  s += aryRealName[i] + "<a href=\"#\" onClick=\"cancelSelPerson('','','" + ary[i] + "','" + aryRealName[i] + "')\">[×]</a>";
		  } else {
			  s += "、" + aryRealName[i] + "<a href=\"#\" onClick=\"cancelSelPerson('','','" + ary[i] + "','" + aryRealName[i] + "')\">[×]</a>";;
		  }
	  }
	  allUserRealNames += "、" + s;
  }
  // alert(allUserRealNames);
  var r = clearRepleatUser(allusers, allUserRealNames);
  users.innerText = r[0];
  userRealNames.innerHTML = r[1];
}
 
function clearRepleatUser(strUsers, strUserRealNames) {
  var ary = strUsers.split(",");
  var aryRealName = strUserRealNames.split("、");
  //alert(strUserRealNames);
  
  var len = ary.length;
  // 创建二维数组
  var ary1 = new Array();
  for (i=0; i<len; i++) {
	  ary1[i] = new Array(2);
	  ary1[i][0] = ary[i];
	  ary1[i][1] = 0; // 1 表示重复
  }
  
  // 标记重复的用户
  for (i=0; i<len; i++) {
	  var user = ary[i];
	  for (j=i+1; j<len; j++) {
		  if (ary1[j][1]==1)
			  continue;
		  if (ary[j]==user)
			  ary1[j][1] = 1;
	  }
  }

  // 重组为字符串
  var str = "";
  var str1 = "";
  for (i=0; i<len; i++) {
	  if (ary1[i][1]==0) {
		  u = ary1[i][0];
		  if (str=="") {
			  str = u;
			  str1 = aryRealName[i];
		  }
		  else {
			  str += "," + u;
			  str1 += "、" + aryRealName[i];
		  }
	  }
  }
  var retary = new Array();
  retary[0] = str;
  retary[1] = str1;
  return retary;
}
</script>
<script>
function ShowChild(imgobj, name) {
	var tableobj = $("childof"+name);
	if (tableobj==null) {
		document.frames.ifrmGetChildren.location.href = "../admin/dept_ajax_getchildren.jsp?op=func&target=&parentCode=" + name;
		if (imgobj.src.indexOf("i_root_style.gif")!=-1)
			imgobj.src = "../images/i_puls_style.jpg";
		if (imgobj.src.indexOf("i_plus_style.jpg")!=-1) {
			imgobj.src = "../images/i_root_style.gif";
		}
		else
			imgobj.src = "../images/i_plus_style.jpg";
		return;
	}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "../images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus_style.gif")!=-1)
			imgobj.src = "../images/i_minus.gif";
		else
			imgobj.src = "../images/i_root_style.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "../images/i_minus.gif";
		else
			imgobj.src = "../images/i_plus_style.jpg";
	}	
}

function insertAdjacentHTML(objId,code,isStart){ 
	var obj = document.getElementById(objId);
	if(isIE()) 
		obj.insertAdjacentHTML(isStart ? "afterbegin" : "afterEnd",code); 
	else{ 
		var range=obj.ownerDocument.createRange(); 
		range.setStartBefore(obj); 
		var fragment = range.createContextualFragment(code); 
		if(isStart) 
			obj.insertBefore(fragment,obj.firstChild); 
		else 
			obj.appendChild(fragment); 
	}
	
	initDepts();
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
</script>
<script LANGUAGE="JavaScript">
// -----------实现floater-------------------
self.onError=null;
currentX = currentY = 0; 
whichIt = null; 
lastScrollX = 0; lastScrollY = 0;
NS = (document.layers) ? 1 : 0;
IE = isIE(); // (document.all) ? 1: 0;
<!-- STALKER CODE -->
function heartBeat() {
try {
	if(IE) { diffY = document.body.scrollTop; diffX = document.body.scrollLeft; }
	if(NS) { diffY = self.pageYOffset; diffX = self.pageXOffset; }
	if(diffY != lastScrollY) {
	percent = .1 * (diffY - lastScrollY);
	if(percent > 0) percent = Math.ceil(percent);
	else percent = Math.floor(percent);
	if(IE) document.all.floater.style.pixelTop += percent;
	if(NS) document.floater.top += percent; 
	lastScrollY = lastScrollY + percent;
	}
	if(diffX != lastScrollX) {
	percent = .1 * (diffX - lastScrollX);
	if(percent > 0) percent = Math.ceil(percent);
	else percent = Math.floor(percent);
	if(IE) document.all.floater.style.pixelLeft += percent;
	if(NS) document.floater.left += percent;
	lastScrollX = lastScrollX + percent;
	} 
}
catch (e) {}
}
<!-- /STALKER CODE -->
<!-- DRAG DROP CODE -->
function checkFocus(x,y) { 
stalkerx = document.floater.pageX;
stalkery = document.floater.pageY;
stalkerwidth = document.floater.clip.width;
stalkerheight = document.floater.clip.height;
if( (x > stalkerx && x < (stalkerx+stalkerwidth)) && (y > stalkery && y < (stalkery+stalkerheight))) return true;
else return false;
}
function grabIt(e) {
if(IE) {
whichIt = event.srcElement;
// IE9下面会因拖动滚动条时whichIt.id取不到而报错
if(typeof(whichIt.id) == 'undefined' || typeof(whichIt.id) != 'string'  ) {
	return ;
}
while (whichIt.id.indexOf("floater") == -1) {
whichIt = whichIt.parentElement;
if (whichIt == null) { return true; }
}
whichIt.style.pixelLeft = whichIt.offsetLeft;
whichIt.style.pixelTop = whichIt.offsetTop;
currentX = (event.clientX + document.body.scrollLeft);
currentY = (event.clientY + document.body.scrollTop); 
} else { 
window.captureEvents(Event.MOUSEMOVE);
if(checkFocus (e.pageX,e.pageY)) { 
whichIt = document.floater;
StalkerTouchedX = e.pageX-document.floater.pageX;
StalkerTouchedY = e.pageY-document.floater.pageY;
} 
}
return true;
}
function moveIt(e) {
if (whichIt == null) { return false; }
if(typeof(whichIt.id) == 'undefined' || typeof(whichIt.id) != 'string'  ) {
	return false;
}
if(IE) {
newX = (event.clientX + document.body.scrollLeft);
newY = (event.clientY + document.body.scrollTop);
distanceX = (newX - currentX); distanceY = (newY - currentY);
currentX = newX; currentY = newY;
whichIt.style.pixelLeft += distanceX;
whichIt.style.pixelTop += distanceY;
if(whichIt.style.pixelTop < document.body.scrollTop) whichIt.style.pixelTop = document.body.scrollTop;
if(whichIt.style.pixelLeft < document.body.scrollLeft) whichIt.style.pixelLeft = document.body.scrollLeft;
if(whichIt.style.pixelLeft > document.body.offsetWidth - document.body.scrollLeft - whichIt.style.pixelWidth - 20) whichIt.style.pixelLeft = document.body.offsetWidth - whichIt.style.pixelWidth - 20;
if(whichIt.style.pixelTop > document.body.offsetHeight + document.body.scrollTop - whichIt.style.pixelHeight - 5) whichIt.style.pixelTop = document.body.offsetHeight + document.body.scrollTop - whichIt.style.pixelHeight - 5;
event.returnValue = false;
} else { 
whichIt.moveTo(e.pageX-StalkerTouchedX,e.pageY-StalkerTouchedY);
if(whichIt.left < 0+self.pageXOffset) whichIt.left = 0+self.pageXOffset;
if(whichIt.top < 0+self.pageYOffset) whichIt.top = 0+self.pageYOffset;
if( (whichIt.left + whichIt.clip.width) >= (window.innerWidth+self.pageXOffset-17)) whichIt.left = ((window.innerWidth+self.pageXOffset)-whichIt.clip.width)-17;
if( (whichIt.top + whichIt.clip.height) >= (window.innerHeight+self.pageYOffset-17)) whichIt.top = ((window.innerHeight+self.pageYOffset)-whichIt.clip.height)-17;
return false;
}
return false;
}
function dropIt() {
whichIt = null;
if(NS) window.releaseEvents (Event.MOUSEMOVE);
return true;
}
<!-- DRAG DROP CODE -->
if(NS) {
window.captureEvents(Event.MOUSEUP|Event.MOUSEDOWN);
window.onmousedown = grabIt;
window.onmousemove = moveIt;
window.onmouseup = dropIt;
}
if(IE) {
document.onmousedown = grabIt;
document.onmousemove = moveIt;
document.onmouseup = dropIt;
}
// if(NS || IE) action = window.setInterval("heartBeat()",1);
</script>  
</HEAD>
<BODY onLoad="initUsers();" style="overflow:auto; margin:0px; padding:0px">
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
%>

<table border="0" align="center" cellpadding="0" cellspacing="0" class="dirTable percent98">
<thead>
  
    <th height="24" colspan="4" align="center"><span>选 择 用 户</span></th>
  
</thead>
<tbody>
  <tr> 
    <td style="width:10% !important" height="87"  valign="top" id="deptTree" colspan="1">
    <table  class="tbg1" ><tr><td width="100%" >
    <div id="directoryTree"></div>

	</td></tr></table>
	</td>
    <td width="80%" align="center" valign="top" colSpan="3">
   	<div id="searchIt" style="text-align:left;">&nbsp;&nbsp;&nbsp;&nbsp;姓&nbsp;&nbsp;&nbsp;名
   	<input type="text" id="search" name="search" class="colInput" onkeypress="searchByName()">
    <input class="btn" type="button" onclick="searchByName()" value="查找" />
    </div>
	<div id="resultTable">
<table width="100%" style="padding:0px" border="0" cellpadding="0" cellspacing="0" class="">
        <thead>
        <tr>
          <th width="98" align="center" style='width:30%' class="tabStyle_1_subTab_title">部门</th>
          <th width="91" align="center" style='width:30%' class="tabStyle_1_subTab_title">职员</th>
          <th width="74" align="center" style='width:30%' class="tabStyle_1_subTab_title">操作</th>
        </tr>
        </thead>
      <tbody id="postsbody">

      </tbody>
    <tr>
    <td id="deptUsers" colspan="3" style="display:none;padding:0px">  	 
    </td>
    </tr>
    <tr>
    <td id="pagesize" colspan="3" align="center" style="display:none;">  	 
	<%
	String strcurpage = "1";
	int curpage = Integer.parseInt(strcurpage);
	// String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by du.DEPT_CODE asc, orders asc";
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";
	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 order by du.DEPT_CODE asc, " + orderField + " asc";
	DeptUserDb du = new DeptUserDb();
	ListResult lr = du.listResult(sql, curpage, pagesize);
	long total = lr.getTotal();
	Paginator paginator = new Paginator(request, total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	int pageBlock = 3;
	int curPageMax = 10;
	if (totalpages < pageBlock)
		curPageMax = totalpages;
	else
		curPageMax = pageBlock;
	%>
    <a href="javascript:;" onclick="firstPage()">首页</a>&nbsp;
    <a href="javascript:;" onclick="pageUp()">上一页</a>&nbsp;
    <span id="pageSpan">
	<%
	for(int i=1;i<=curPageMax;i++) {
	%>
        <a id="pageNum<%=i%>" href="javascript:;" onclick="ajaxList(<%=i%>,<%=pagesize%>)"><%=i%></a>&nbsp;
	<%}%>
    </span>
    &nbsp;<a href="javascript:;" onclick="pageDown()">下一页</a>
    &nbsp;<a href="javascript:;" onclick="lastPage()">末页</a>
    </td>
    </tr>
    </table>
	</div>
    </td>
    <!-- 
    <td width="21%" valign="top" >
		  <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
		    <tr style="height:30px">
		      <td class="floaterTitle" align="center" style="height:30px">已选人员</td>
		    </tr>
		  <tr style="height:99%"><td valign="top">
		  <div id="users" name="users" style="display:none"></div>
		  <div id="userRealNames" name="userRealNames" style="padding:2px"></div>
		  </td>
		  </tr>
		  </table>
    </td> -->
  </tr></tbody>
</table>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</BODY>

<script>
var j$ = jQuery.noConflict();
var curPage = 1;
var totalpages = <%=curPageMax%>
function ajaxExchange(cPages, pagesize) {
	jQuery("a[id^='pageNum']").removeClass("activePageNum");
	jQuery("#pageNum" + cPages).addClass("activePageNum");
	
	curPage = cPages;
	j$.ajax({
		type: "post",
		url: "../user_multi_sel_ajax2.jsp",
		data : {
		    op: "getResult",
        	CPages : cPages,
			pagesize : pagesize,
			temp : "harddriver",
			deptCode : deptCode
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = j$.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("deptUsers").innerHTML = re.result;
				allUserOfDept = re.allUserOfDept;
				allUserRealNameOfDept = re.allUserRealNameOfDept;
				totalpages = re.totalpages;
				var str = "";
				for (i = 1; i <= totalpages; i++) {
					str += "<a id=\"pageNum" + i + "\" href=\"javascript:;\" onclick=\"ajaxList(" + i + ",<%=pagesize%>)\">" + i + "</a>&nbsp;";
				}
				o("pageSpan").innerHTML = str;
				jQuery("#pageNum" + cPages).addClass("activePageNum");
				//alert(re.result);
			}		
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});
}

function ajaxList(cPages, pagesize){
	jQuery("a[id^='pageNum']").removeClass("activePageNum");
	jQuery("#pageNum" + cPages).addClass("activePageNum");
	
	curPage = cPages;
	j$.ajax({
		type: "post",
		url: "../user_multi_sel_ajax2.jsp",
		data : {
		    op: "getResult",
        	CPages : cPages,
			pagesize : pagesize,
			temp : "ajaxList",
			deptCode : deptCode
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = j$.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("deptUsers").innerHTML = re.result;
				allUserOfDept = re.allUserOfDept;
				allUserRealNameOfDept = re.allUserRealNameOfDept;
				totalpages = re.totalpages;
				var str = "";
				for (i = 1; i <= totalpages; i++) {
					str += "<a id=\"pageNum" + i + "\" href=\"javascript:;\" onclick=\"ajaxList(" + i + ",<%=pagesize%>)\">" + i + "</a>&nbsp;";
				}
				o("pageSpan").innerHTML = str;
				jQuery("#pageNum" + cPages).addClass("activePageNum");
				//alert(re.result);
			}		
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});
}

var curPageMax = <%=curPageMax%>;
function pageDown() {
	if (curPage == totalpages) {
		return;
	}
	if (curPage == curPageMax) {
		var oldCurPageMax = curPageMax;
		curPageMax += <%=pageBlock%>;
		if (curPageMax > totalpages) {
			curPageMax = totalpages;
		}
		initPageSpan(oldCurPageMax+1, curPageMax);
	}
	ajaxList(curPage + 1, <%=pagesize%>);
}

function initPageSpan(fromPage, toPage) { 
	var str = "";
	for (i=fromPage; i<=toPage; i++) {
		str += "<a id=\"pageNum" + i + "\" href=\"javascript:;\" onclick=\"ajaxList(" + i + ",<%=pagesize%>)\">" + i + "</a>&nbsp;";
	}
	o("pageSpan").innerHTML = str;
}

function pageUp() {
	if (curPage==1)
		return;
	if (curPage == Math.floor(curPageMax/<%=pageBlock%>) * <%=pageBlock%> + 1) {
		// alert(curPage);
		curPageMax = Math.floor(curPageMax/<%=pageBlock%>) * <%=pageBlock%>;
		curPageStart = curPageMax - <%=pageBlock%> + 1;
		// alert(curPageMax + "--" + <%=pageBlock%>);
		initPageSpan(curPageStart, curPageMax);
	}
	ajaxList(curPage - 1, <%=pagesize%>);
}

function firstPage() { 
	if (<%=pageBlock%> < totalpages) {
		initPageSpan(1, <%=pageBlock%>);
	}
	else
		initPageSpan(1, totalpages);
	ajaxList(1, <%=pagesize%>);		
}

function lastPage() {
	var p = Math.floor(totalpages / <%=pageBlock%>);
	var lastPage = p + <%=pageBlock%> + 1;
	if (lastPage > totalpages) {
		lastPage = totalpages;
	}
	initPageSpan(p * <%=pageBlock%> + 1, lastPage);
	ajaxList(totalpages, <%=pagesize%>);		
}

function searchByName(){
  var name = j$("#search").val();
  searchByNameAjax(name, <%=pagesize%>);
}

function searchByNameAjax(name, pagesize) {
	document.getElementById("deptUsers").style.display = "none";
	document.getElementById("pagesize").style.display = "none";
	// document.getElementById("searchIt").style.display = "none";
	//o("selDeptAllBtn").style.display = "none";

	var dlg = window.opener ? window.opener : window.dialogArguments;
	var depts = "";
	//if (typeof(dlg.getDept)!="undefined")
		//depts = dlg.getDept().trim();
	j$.ajax({
		type: "post",
		url: "../user_multi_sel_ajax2.jsp?name=" + encodeURI(name),
		data : {
		    op: "getResult",
        	//name: name,	// 中文会有乱码,如果用encodeURI(name)无法自动decode
			depts : depts,
			pagesize: pagesize,
			temp : "harddriver"
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = j$.parseJSON(data);
						
			if (re.ret=="1") {
				document.getElementById("deptUsers").innerHTML = re.result;
				o("deptUsers").style.display = "";
				// 清空原来所选部门的用户
				DWRUtil.removeAllRows("postsbody");
			}		
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});	
}

if(!isIE()){ // firefox innerText define
   HTMLElement.prototype.__defineGetter__("innerText", 
    function(){
     var anyString = "";
     var childS = this.childNodes;
     for(var i=0; i<childS.length; i++) {
      if(childS[i].nodeType==1)
       anyString += childS[i].tagName=="BR" ? '\n' : childS[i].textContent;
      else if(childS[i].nodeType==3)
       anyString += childS[i].nodeValue;
     }
     return anyString;
    } 
   ); 
   HTMLElement.prototype.__defineSetter__("innerText", 
    function(sText){
     this.textContent=sText;
    }
   ); 
}


</script>
</HTML>
