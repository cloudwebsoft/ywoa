<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.clouddisk.*"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.opensymphony.xwork2.Action"%>
<!--  <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">-->
<html>
<head>
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>云盘————我的云盘</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=Global.getRootPath(request) %>/netdisk/clouddisk.css"/>
<link type="text/css" rel="stylesheet" href="<%=Global.getRootPath(request) %>/netdisk/showDialog/showDialog.css"/>

 <script type="text/javascript" >
 document.onmousemove = function () {
 var divx = window.event.clientX+"px";
 var divy = window.event.clientY+"px";
 //var ie5menua = document.getElementById("ie5menu");

 }

 </script>
<script src="../inc/common.js"></script>
<script src="../inc/upload.js"></script>

<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/activebar2.js"></script>
<script language=JavaScript src='formpost.js'></script>
<script language=JavaScript src='showDialog/jquery.min.js'></script>
<script language=JavaScript src='showDialog/showDialog.js'></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<link href="../js/contextMenu/css/ContextMenu.css" rel="stylesheet" type="text/css" />
<script src="../js/contextMenu/jquery.contextMenu.js" type="text/javascript"></script>
<!-- swfupload 文件普通上传 -->
<script src= "swfupload/swfupload.js"></script>
<script type="text/javascript" src="swfupload/swfupload.queue.js"></script>
<script src= "js/swfupload.js"></script>
<script src= "js/clouddisk.js"></script> 
<script src="js/clouddisk_tiled.js"></script>
<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />
<script type="text/javascript" src="../js/jquery.toaster.netdisk.js"></script> 
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.netdisk.DocumentMgr"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.netdisk.Directory"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

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

Leaf rf = new Leaf(userName); 
if (rf == null || !rf.isLoaded()) {
	rf = new Leaf();
	rf.AddUser(userName);
	rf = rf.getLeaf(userName);
	RoleTemplateMgr roleTemplateMgr = new RoleTemplateMgr();
	try {
		boolean flag = roleTemplateMgr.copyDirsAndAttToNewUser(userName);
		if (!flag) { 
			out.print(StrUtil.jAlert("角色模板初始化失败！","提示"));
		}
	} catch (ErrMsgException e1) {
		out.print(StrUtil.jAlert("角色模板初始化失败！","提示"));
	} 
}

//如果启用了客户端侧边栏的html则初始化html
com.redmoon.clouddisk.Config cloudcfg = com.redmoon.clouddisk.Config.getInstance();
if (cloudcfg.getBooleanProperty("is_openSideHTML")) {
	SideBarMgr sbMgr = new SideBarMgr();
	sbMgr.initialization(userName);
}

String root_code = ParamUtil.get(request, "root_code");
String text_content = ParamUtil.get(request,"select_content");//搜索内容
String select_sort = ParamUtil.get(request,"select_sort"); //搜索判断
String select_file = ParamUtil.get(request,"select_file");//文件类别的判断
String which = ParamUtil.get(request,"select_which");
if (root_code.equals("")) {
	 //root_code = "root";
	 root_code = userName;
}
String dir_code = ParamUtil.get(request, "dir_code");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if("".equals(dir_code)){ 
	dir_code = root_code;
}

Leaf leaf = dir.getLeaf(dir_code);
if (leaf==null || !leaf.isLoaded()) {
	Leaf leafUser = new Leaf();
	leafUser.AddUser(dir_code);
	leaf =dir.getLeaf(dir_code);
	//out.print(SkinUtil.makeErrMsg(request, "该目录已不存在！！！"));
	//return;
}

 String sort0 = ParamUtil.get(request, "sort0");
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "sort0", sort0, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
if("".equals(sort0)){
	sort0 = "时间";
}else if(sort0.equals("file_size")){
	sort0 = "大小";
}
else if(sort0.equals("name")){
	sort0 = "名称";
}
else if(sort0.equals("version_date")){
	sort0 = "时间";
}

String dir_name = "";
 
int id = 0;


String correct_result = "操作成功！";

Document doc = new Document();
try {
	doc = docmanager.getDocumentByCode(request, dir_code, privilege);
} catch ( ErrMsgException  e) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
Document template = null;


dir_name = leaf.getName();

LeafPriv lp = new LeafPriv(dir_code);
if (!lp.canUserSee(privilege.getUser(request))) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 取得上级目录
String filePath = leaf.getFilePath();

String op = ParamUtil.get(request, "op");
if("".equals(op)){
	op="editarticle";
}
String mode = ParamUtil.get(request, "mode"); // select
String work = ParamUtil.get(request, "work"); // init modify
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "work", work, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
if (op.equals("editarticle")) {
	op = "edit";
	try {
		doc = docmanager.getDocumentByCode(request, dir_code, privilege);
		dir_code = doc.getDirCode();
		
	
		
	} catch (ErrMsgException e) {
		out.print(strutil.makeErrMsg(e.getMessage(), "red", "green"));
		return;
	}
}
else if (op.equals("myclouddisk")) {
	op = "edit";
	try {
		String fileName = ParamUtil.get(request, "name");
		try {
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "name", fileName, getClass().getName());
		}
		catch (ErrMsgException e) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
			return;
		}
		
		doc = docmanager.getDocumentByName(request, dir_code, fileName, privilege);
		if (doc == null) {
			return;
		}
		dir_code = doc.getDirCode();
		leaf = dir.getLeaf(dir_code);
		if (leaf==null || !leaf.isLoaded()) {
			out.print(SkinUtil.makeErrMsg(request, "该目录已不存在！"));
			return;
		}

		dir_name = leaf.getName();

		lp = new LeafPriv(dir_code);
		if (!lp.canUserSee(privilege.getUser(request))) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	} catch (ErrMsgException e) {
		out.print(strutil.makeErrMsg(e.getMessage(), "red", "green"));
		return;
	}
}


String action = ParamUtil.get(request, "action");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (doc!=null) {
	id = doc.getID();
	Leaf lfn = new Leaf();
	lfn = lfn.getLeaf(doc.getDirCode());
	dir_name = lfn.getName();
}

//防XSS
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "work", work, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "sort0", sort0, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk");
String unit_code = privilege.getUserUnitCode(request);
//swfUpload文件上传
HashMap<String,String> explorerFileType = new HashMap<String,String>();
explorerFileType = UtilTools.uploadFileTypeByExplorer("netdisk_ext");
String file_size_limit = cloudcfg.getProperty("file_size_limit");
int file_upload_limit = cloudcfg.getIntProperty("file_upload_limit");
String upload_file_types = explorerFileType.get("ie_upload_file_types");
String fixfox_upload_file_types = explorerFileType.get("fixfox_upload_file_types");
%>

<script language="JavaScript">

function create(dir_code) { 
curDirCode=dir_code
	if (curDirCode!="") {
		window.location.href="?op=AddChild&type=<%=Leaf.TYPE_DOCUMENT%>&parent_code=" + curDirCode + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&code=<%=leaf.getAutoCode()%>&name=<%=StrUtil.UrlEncode("新建文件夹")%>";
		curDirCode = "";
	}
}


<!--
function onAddFile(index, fileName, filePath, fileSize, modifyDate) {
	// alert(index + "-" + fileName + "-" + filePath + "-" + fileSize + "-" + modifyDate);
	// o("attFiles").innerHTML += "<div id='attTmp' name='attTmp' attId='" + index + "' >" + index + "-" + fileName + "<a id='attA' name='attA' href='javascript:;' attId='" + index + "' onclick='removeFile(this.attId);'>删除</a></div>";
}

function removeFile(index) {
	addform.webedit.RemoveFile(index);
	var attTmps = document.getElementsByName("attTmp");
	var attAs = document.getElementsByName("attA");
	var obj = null;
	for (var i=0; i<attTmps.length; i++) {
		if (attTmps[i].attId==index) {
			obj = attTmps[i];
			continue;
		}
		if (attTmps[i].attId>index) {
			attTmps[i].attId -= 1;
			attAs[i].attId -= 1;
		}
	}
	if (obj!=null) {
		obj.outerHTML = "";
	}	
}

function onDropFile(filePaths) {
	// 清空之前的文件，否则当空间不足时，drop了一个小文件，因原来的大文件仍存在于待上传列表中，致需刷新后才能再上传
	addform.webedit.RemoveAllFile();
	
	var ary = filePaths.split(",");
	var hasFile = false;
	for (var i=0; i<ary.length; i++) {
		var filePath = ary[i].trim();
		if (filePath!="") {
			hasFile = true;
			addform.webedit.InsertFileToList(filePath);
		}
	}
	if (hasFile)
		SubmitWithFileThread();
}

function OfficeOperate() {
	alert(redmoonoffice.ReturnMessage.substring(0, 4)); // 防止后面跟乱码
}

// 编辑文件
function editdoc(id, attachId, ext) {
	<%if (cfg.get("isUseNTKO").equals("true")) {%>
	openWin("netdisk_office_ntko_edit.jsp?ext="+ ext +"&id=" + id + "&attachId="+attachId, 1100, 800);	
	<%}else{%>
	rmofficeTable.style.display = "";
	redmoonoffice.AddField("id", id);
	redmoonoffice.AddField("attachId", attachId);
	redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/netdisk/netdisk_office_get.jsp?id=" + id + "&attachId=" + attachId);
	<%}%>
}


<%
if (doc!=null) {
	out.println("var id=" + doc.getID() + ";");
}
%>
var id = "<%=id%>"; // 用于右键菜单
var curAttachId = "";
var curAttachName = "";
var docId_old = "";
var attId_old = "";
var op = "<%=op%>";
var work = "<%=work%>";

function SubmitWithFileDdxc() {
	addform.webedit.isDdxc = 1;
	if (document.addform.title.value.length == 0) {
		alert("请输入文章标题.");
		document.addform.title.focus();	
		return false;
	}
	loadDataToWebeditCtrl(addform, addform.webedit);
	addform.webedit.FilePath = "<%=file_netdisk%>/" + addform.webedit.FilePath;
	addform.webedit.MTUpload();
	// 因为Upload()中启用了线程的，所以函数在执行后，会立即反回，使得下句中得不到ReturnMessage的值
	// 原因是此时服务器的返回信息还没收到
	// alert("ReturnMessage=" + addform.webedit.ReturnMessage);
}

function SubmitWithFileThread() {
	upload_common();
	if (document.addform.title.value.length == 0) {
		alert("请输入文章标题.");
		document.addform.title.focus();			
		return false;
	}
	loadDataToWebeditCtrl(addform, addform.webedit);
	$("#ctlTable").css({"display":"block"});
	addform.webedit.Upload();
	// 因为Upload()中启用了线程的，所以函数在执行后，会立即反回，使得下句中得不到ReturnMessage的值
	// 原因是此时服务器的返回信息还没收到
	// alert("ReturnMessage=" + addform.webedit.ReturnMessage);
	window.setTimeout("checkResult()",200);
}

function BrowseFolder(){  
 try{  
  var Message = "请选择想要移动到的文件夹";  //选择框提示信息  
  var Shell = new ActiveXObject( "Shell.Application" );  
  var Folder = Shell.BrowseForFolder(0,Message,0x0040,0x11);//起始目录为：我的电脑  
  //var Folder = Shell.BrowseForFolder(0,Message,0); //起始目录为：桌面  
  if(Folder != null){      Folder = Folder.items();  // 返回 FolderItems 对象  
    Folder = Folder.item();  // 返回 Folderitem 对象  
    Folder = Folder.Path;   // 返回路径  
    if(Folder.charAt(Folder.length-1) != "\\"){  
      Folder = Folder + "\\";  
    }  
    document.all.savePath.value=Folder;  
    return Folder;  
  }  
 }catch(e){   
  alert(e.message);  
 }  
} 



function checkResult() {
	// o("progress").innerHTML = addform.webedit.bytesUploadNum;
	closeDialog();
	if (addform.webedit.ReturnMessage == "<%=correct_result%>") {
		// window.status = addform.webedit.ReturnMessage;
		doAfter(true);
	}
	else
		window.setTimeout("checkResult()",200);
}

function SubmitWithoutFile() {
	if (document.addform.title.value.length == 0) {
		alert("请输入文章标题.");
		document.addform.title.focus();	
		return false;
	}
	addform.isuploadfile.value = "false";
	loadDataToWebeditCtrl(addform, addform.webedit);
	addform.webedit.UploadMode = 0;
	addform.webedit.UploadArticle();
	addform.isuploadfile.value = "true";
	if (addform.webedit.ReturnMessage == "<%=correct_result%>")
		doAfter(true);
	else
		doAfter(false);		
}

function ClearAll() {
	document.addform.title.value=""
	oEdit1.putHTML(" ");
}

function doAfter(isSucceed) {
	if (isSucceed) {
		if (op=="edit")
		{
			if (work=="modify")
				location.href = "clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
			else
				location.href = "clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
		}
		else {
			location.href = "clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
		}
	}
	else {
		alert(addform.webedit.ReturnMessage);
	}
}

function checkWebEditInstalled() {
	if (!isIE())
		return true;
	var bCtlLoaded = false;
	try	{
		if (typeof(addform.webedit.AddField)=="undefined")
			bCtlLoaded = false;
		if (typeof(addform.webedit.AddField)=="unknown") {
			bCtlLoaded = true;
		}
	}
	catch (ex) {
	}
	return bCtlLoaded;
}

function checkOfficeEditInstalled() {
	if (!isIE())
		return true;
	
	var bCtlLoaded = false;
	try	{
		if (typeof(redmoonoffice.AddField)=="undefined")
			bCtlLoaded = false;
		if (typeof(redmoonoffice.AddField)=="unknown") {
			bCtlLoaded = true;
		}
	}
	catch (ex) {
	}
	return bCtlLoaded;
}

function window_onload() {
	var re = false;
	re = checkOfficeEditInstalled() || checkWebEditInstalled();
	if (!re) {
		$('<div></div>').html('您还没有安装客户端控件，请点击确定此处下载安装！').activebar({
			'icon': '../images/alert.gif',
			'highlight': '#FBFBB3',
			'url': '../activex/oa_client.EXE',
			'button': '../images/bar_close.gif'
		});
	}
}

function displayCtlTable(btnObj) {
	if (ctlTable.style.display=="none") {
		ctlTable.style.display = "";
		addform.webedit.height = "85px";
		btnObj.value = "隐 藏";
	}
	else {
		ctlTable.style.display = "none";
	
		//addform.webedit.height = "0px";
			//alert("test")
		btnObj.value = "上 传";
	}
	    
}

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
//-->


//javascript去空格函数 
</script>
<script>
	$(function(){
		var type = "";
		var root_code = "";
		var code = "";
		var name = "";
		//用户名 全局变量 
		var userName= '<%=StrUtil.UrlEncode(userName) %>'; 
		//回到顶部
		$(window).goToTop({
			showHeight : 1,//设置滚动高度时显示
			speed : 500 //返回顶部的速度以毫秒为单位
		});
		//极速上传
		$('.clfT').live("click",function(){
			$("#ctlTable").css({"display":"block","top":"150px","z-index":"1800"});
			$("#webedit").css({"height":"75px"});
			$(".treeBackground").addClass("SD_overlayBG2");
			$("#treeBackground").css({"display":"block"});
		});		 
		//极速上传取消按钮
		$("#SD_cancel,#upload_close").bind("click", function(){
			window_remove();
			//sd_closeWindow();
		});
		 $('#SD_confirm1').live("click",function(){   //移动文件
			 	attId = curAttachId;
				dirCode = curDirCode;
				dirName = curDirName;
				
				if(dirCode == ""){
					closeDialog();
					return;
				}
				$.ajax({
					type:"post",
					url:"clouddisk_list_do.jsp",
					data:{"op":"moveFile", "type":type, "att_id":attId, "dirCode":dirCode, "dirName":dirName,"type":typeof(attId)},
					success:function(data,status){
						data = $.parseJSON(data);
						if(data.ret == "1"){
							if(data.doc_id == data.doc_oldId){
								closeDialog();
								return;
							}
							treeMove(attId);
							closeDialog();
							jAlert(data.msg,"提示");
						}else if(data.ret == "2"){
							closeDialog();
							treeMove(data.oldCode);
							jAlert(data.msg,"提示");
						}
						else{
							closeDialog();	
							jAlert(data.msg,"提示");
						}
					},
					error:function(XMLHttpRequest, textStatus){
						alert(XMLHttpRequest.responseText);
					}
				});
		 })
		
		//极速上传
		$('.clfT').live("click",function(){
			$("#ctlTable").css({"display":"block"});
		});		 
		//极速上传取消按钮
		$("#SD_cancel,#upload_close").bind("click", function(){
			window_remove();
			//sd_closeWindow();
		});

	});
</script>
</head>

<body onLoad="window_onload();upload_none();getSharedDir('<%=privilege.getUser(request) %>')" oncontextmenu="return false"> &nbsp;	
<%
if (!privilege.isUserLogin(request))
{
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "version_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";	
Leaf dlf = new Leaf();
if (doc!=null) {
	dlf = dlf.getLeaf(doc.getDirCode());
}

%>
    
 
 <div id="Right" class="Right">
 	 <input type="hidden" id="userName" value="<%=StrUtil.UrlEncode(userName)%>" />	<!-- lzm添加服务于cloudisk_tiled -->
 	  <input type="hidden" value="<%=select_sort %>" id="isSearch"/> <!-- lzm添加用于搜索界面 -->
 	  <input type="hidden" id="unitCode" value="<%=unit_code%>" />	<!-- lzm添加 角色模板 -->
 	  <!-- 列表切换 -->
 	  <input type="hidden" value="<%=select_file %>" id="fileType"/>
	  <input type="hidden" value="<%=which%>" id="which"/>
	  <input type="hidden" value="<%=StrUtil.UrlEncode(text_content) %>"  id="content"/>
	  <!-- lzm判断列表页 平铺页 -->
	  <input type="hidden" value="1" id="curPage" />
	  <!--lzm 新建文件夹 -->
	  <input type="hidden" value="<%=dir_code%>" id="curDirCode" />
	  <input type="hidden" value="<%=Leaf.TYPE_DOCUMENT%>" id="type" />
	  <input type="hidden" value="<%=root_code%>" id="rootCode" />
	  <input type="hidden" value="<%=Leaf.getAutoCode()%>" id="code" />
	    <!-- lzm SwfUpload文件批量上传 -->
	   	<input type="hidden" value="netdisk_office_upload.jsp;jsessionid=<%=session.getId() %>?userName=<%=StrUtil.UrlEncode(userName) %>&dirCode=<%=StrUtil.UrlEncode(dir_code) %>" id="uploadUrl" />
		<input type="hidden" value="<%=file_size_limit %>" id="fileSizeLimit" />
		<input type="hidden" value="<%=file_upload_limit %>" id="fileUploadLimit" />
		<input type="hidden" value="<%=upload_file_types %>" id="uploadFileType" />
		<input type="hidden" value="<%=fixfox_upload_file_types %>" id="FixfoxUploadFileType" />
		<input type="hidden" id="cooperateId" value="" />
					

      <div class="rHead">
      	<div class="fixedDivTiled">
	    <div class="rHead1" >
		  <div class="uploadFile_c">
			  <div class="upload_sel" id="upload_sel" style="display:none" >
			     <ul>
			       <li>
					 <a><span class="TextStyle" id="spanButtonPlaceholder"></span></a>
					</li>
					<li class="clfT">
						<a href="javascript:void(0)">极速</a>
					</li>
			     </ul>
			  </div>
		  </div>
		  <div class="newFolder" ></div>
		  <a onclick="delBatch()"><div class="deleteFile_c"></div></a>
		  <div class="view" >
		    <div id="view_1"  style="cursor:pointer;background:url(images/clouddisk/view_list_2.gif); width:30px; height:24px; border-right:1px solid #cacaca; float:left" onclick="cloud_list('<%=dir_code %>')" ></div>
			<div id="view_2" style="cursor:pointer;background:url(images/clouddisk/view_thumbnail_1.gif); width:29px; height:24px; float:left" onclick="tiled_list('<%=dir_code %>')"></div>
            <script>
              $(function(){
				$("#view_1").click(
				  function(){
				    $(this ).css({"background":"url(images/clouddisk/view_list_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_thumbnail_2.gif)"});
				  }
				) 
				$("#view_2").click(
				  function(){
				   $(this).css({"background":"url(images/clouddisk/view_thumbnail_1.gif)"}).siblings().css({"background":"url(images/clouddisk/view_list_2.gif)"});
				  }
				) 
			  })
            </script>
            
		  </div>
		  <div class="sort"  >
		  <% 	UserDb ud = new UserDb();
			ud = ud.getUserDb(userName);
			String strDiskAllow = UtilTools.getFileSize(ud.getDiskSpaceAllowed());
			String strDiskHas = UtilTools.getFileSize(ud.getDiskSpaceAllowed() - ud.getDiskSpaceUsed());
		   %>
		   	空间：<%=strDiskAllow%>,&nbsp;剩余：<%=strDiskHas%>
		    <!-- <span>排序：</span>
			<span id="sort_sel"><%=sort0 %><img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span>
			<div id="sort_sel_pull">
			  <p style="margin-top:5px "><a style="cursor:pointer" onClick="doSort2('version_date')"><span>时间<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px; "/></span></a></p>
			  <p style="margin-top:5px"><a style="cursor:pointer" onClick="doSort2('file_size')"><span>大小<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span></a></p>
			  <p style="margin-top:5px"><a style="cursor:pointer" onClick="doSort2('name')"><span>名称<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span></a></p>
			</div> -->
            
		  </div>
		</div>
		<div class="rHead2" style=" background:#f7f7f7;  z-index:20; " >
			<div class= "checkboxDetail" >
				<span class="length"></span>
				<span class="cancle"></span>
				
			</div>
			
			<div style="float:left;width:10px;margin-left:10px;">
				<span id="dirAttAllCboxIcon"></span>
				<input id="dirAttAllCbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'),selAllCheckBox('ids_folder'); else deSelAllCheckBox('ids'),deSelAllCheckBox('ids_folder');" /><input class="check_sort" value="0" type="hidden" /></div>
			<%
			String showPath = "";
			boolean backFlag = true;
			
			if(select_sort.equals("select_one")) {
				showPath = "<a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >    <a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"&select_sort=select_one&select_content="+StrUtil.UrlEncode(text_content)+"'>“" + text_content + "”的搜索结果</a>";
			
			%>
				 <div style="float:left;margin-left:15px;margin-top:4px;"><a href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>"><img src="images/clouddisk/back.png"/></a></div>
			<%}else{
				if (dir_code.equals(root_code)) {
					backFlag = false;
					showPath = "<a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>";
				} else if (leaf.getParentCode().equals(root_code)) {
					showPath = "<a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    <a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
				} else {
					Leaf pleaf = new Leaf(leaf.getParentCode());
					if (leaf.getLayer() == 3) {
						showPath = "<a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    <a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
					} else {
						showPath = "<a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    ...>>    <a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_tiled.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
					}
				}
			
			 %>
			 <%if(backFlag) {%>
			 <div style="float:left;margin-left:15px;margin-top:4px;"><a href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&dir_code=<%=StrUtil.UrlEncode(leaf.getParentCode()) %>"><img src="images/clouddisk/back.png"/></a></div>
			 <%}
			}%>
			 <div class='all_file'><%=showPath %></div>
         
          <% 
          		if("".equals(text_content)){ text_content = "请输入文件名搜索..."; }
          %>
		  <div class="search"><input maxlength="30" value="<%=text_content %>" id="select_content"  onblur="select_con()"  onKeyDown="if (event.keyCode==13) {this.blur()}" onfocus="if(this.value=='请输入文件名搜索...') this.value=''"/></div>
		  <a>
            <div id="search_one" style=" float:right;width:36px;height:26px;position:relative; top:5px;right:-184px" onclick="select_con()"> </div>
          </a>
		  <div class="sortShow" >
		    <span>分类显示：</span>
			<ul>
			  <%
			   
			  	if("select_file".equals(select_file)){
			  		if("1".equals(which)){	%>
			  			<li class="imgShow"><img id="img_sort" style="cursor:pointer" src="images/clouddisk/imgShow_2.gif"/ onclick="select_file(1)"></li>
			  			<li class="fileShow"><img id="file_sort" style="cursor:pointer" src="images/clouddisk/fileShow_1.gif"  onclick="select_file(2)"/></li>
			  			<li class="videoShow"><img id="video_sort" style="cursor:pointer" src="images/clouddisk/videoShow_1.gif" onclick="select_file(3)"/></li>
			 			<li class="musicShow"><img id="music_sort" style="cursor:pointer" src="images/clouddisk/musicShow_1.gif" onclick="select_file(4)"/></li>
			  		<%}
			  		else if ("2".equals(which)){%>
			  			 <li class="imgShow"><img id="img_sort" style="cursor:pointer" src="images/clouddisk/imgShow_1.gif"/ onclick="select_file(1)"></li>
			  			<li class="fileShow"><img id="file_sort" style="cursor:pointer" src="images/clouddisk/fileShow_2.gif"  onclick="select_file(2)"/></li>
			  			<li class="videoShow"><img id="video_sort" style="cursor:pointer" src="images/clouddisk/videoShow_1.gif" onclick="select_file(3)"/></li>
			 			<li class="musicShow"><img id="music_sort" style="cursor:pointer" src="images/clouddisk/musicShow_1.gif" onclick="select_file(4)"/></li>
			 		<%}
			  		else if ("3".equals(which)){%>
			  			<li class="imgShow"><img id="img_sort" style="cursor:pointer" src="images/clouddisk/imgShow_1.gif"/ onclick="select_file(1)"></li>
			  			<li class="fileShow"><img id="file_sort" style="cursor:pointer" src="images/clouddisk/fileShow_1.gif"  onclick="select_file(2)"/></li>
			  			<li class="videoShow"><img id="video_sort" style="cursor:pointer" src="images/clouddisk/videoShow_2.gif" onclick="select_file(3)"/></li>
			 			<li class="musicShow"><img id="music_sort" style="cursor:pointer" src="images/clouddisk/musicShow_1.gif" onclick="select_file(4)"/></li>
			 		<%}
			 		else if ("4".equals(which)){%>
			 			<li class="imgShow"><img id="img_sort" style="cursor:pointer" src="images/clouddisk/imgShow_1.gif"/ onclick="select_file(1)"></li>
			  			<li class="fileShow"><img id="file_sort" style="cursor:pointer" src="images/clouddisk/fileShow_1.gif"  onclick="select_file(2)"/></li>
			  			<li class="videoShow"><img id="video_sort" style="cursor:pointer" src="images/clouddisk/videoShow_1.gif" onclick="select_file(3)"/></li>
			 			<li class="musicShow"><img id="music_sort" style="cursor:pointer" src="images/clouddisk/musicShow_2.gif" onclick="select_file(4)"/></li>
			 		<%}
			  } else {%>
			  <li class="imgShow"><img id="img_sort" style="cursor:pointer" src="images/clouddisk/imgShow_1.gif"/ onclick="select_file(1)"></li>
			  <li class="fileShow"><img id="file_sort" style="cursor:pointer" src="images/clouddisk/fileShow_1.gif"  onclick="select_file(2)"/></li>
			  <li class="videoShow"><img id="video_sort" style="cursor:pointer" src="images/clouddisk/videoShow_1.gif" onclick="select_file(3)"/></li>
			  <li class="musicShow"><img id="music_sort" style="cursor:pointer" src="images/clouddisk/musicShow_1.gif" onclick="select_file(4)"/></li>
			  <%}%>
			</ul>
		  </div>
		</div>
		</div>
		<form name="addform" action="fwebedit_do.jsp" method="post" style="padding:0px; margin:0px; margin-top:-40px;">
       <ul class="attDirGroup">
	
                
		 <%

			String fileCurrent = ParamUtil.get(request,"cur");
			if(select_sort.equals("select_one")){
				String sql = "SELECT code FROM netdisk_directory WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and root_code = " + StrUtil.sqlstr(root_code)+ " and isDeleted=0 order by add_date desc";
				Iterator irch = leaf.getSearchChildren(sql).iterator();
				while (irch.hasNext()) {
					Leaf clf = (Leaf)irch.next();
			%>
			 <li class="dir" title="<%=com.cloudwebsoft.framework.security.AntiXSS.clean(clf.getName())%>" id="dirAtt<%=clf.getCode()%>" isShared="<%=clf.isShared() %>" dirCode="<%=clf.getCode()%>" dirName="<%=com.cloudwebsoft.framework.security.AntiXSS.clean(clf.getName())%>">
			 	<input class="attDirCheckBox"  name="floder_ids" type="checkbox" value="<%=clf.getCode()%>" />
		    	<div class="attDirCheckIcon" ></div>
		   	 	<div class="attDirIcon">
		   	 	 	<a url="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>"   href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>" >
			   	 		<%if(!clf.isShared()){ %>
			   	 		<img src="images/clouddisk_tiled/folder.png" />
			   	 		<%}else{ %>
			   	 		<img src="images/clouddisk_tiled/cooperate.png" />
			   	 		<%} %>
			   	 	</a>
		        </div> 
		        <div class="attDirName" id="dirName<%=StrUtil.UrlEncode(clf.getCode())%>" >
		        	 <a href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>">
		        	 	<%=com.cloudwebsoft.framework.security.AntiXSS.clean(clf.getName())%>
		        	 </a>
		        </div>
		       
		    </li>
			
			  
           <%
				}
           }
			
			else  if("请输入文件名搜索...".equals(text_content)&&!("select_file".equals(select_file))){%>
     
                <%
				Iterator irch = leaf.getChildren().iterator();
				while (irch.hasNext()) {
					Leaf clf = (Leaf)irch.next();
				%>
               	 <li class="dir" id="dirAtt<%=clf.getCode()%>" title="<%=com.cloudwebsoft.framework.security.AntiXSS.clean(clf.getName())%>" dirCode="<%=clf.getCode()%>" isShared="<%=clf.isShared() %>" dirName="<%=com.cloudwebsoft.framework.security.AntiXSS.clean(clf.getName())%>">
		    		<input class="attDirCheckBox"  name="floder_ids" type="checkbox" value="<%=clf.getCode()%>" />
			    	<div class="attDirCheckIcon" ></div>
			   	 	<div class="attDirIcon">
			   	 		 <a url ="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>"  href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>" >
			   	 			<%if(!clf.isShared()){ %>
			   	 			<img src="images/clouddisk_tiled/folder.png" />
			   	 			<%}else{ %>
							<img src="images/clouddisk_tiled/cooperate.png" />
							<%} %>
			   	 		 </a>
			        </div>
			        <div class="attDirName" id="dirName<%=StrUtil.UrlEncode(clf.getCode())%>">
				        	 <a href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>">
				        	 	<%=com.cloudwebsoft.framework.security.AntiXSS.clean(clf.getName())%>
				        	 </a>
			        </div>
		       
		    </li>
                <%
                }
           } %>
        <%
			String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
			if (strcurpage.equals(""))
				strcurpage = "1";
			if (!StrUtil.isNumeric(strcurpage)) {
				out.print(StrUtil.makeErrMsg("标识非法！"));
				return;
			}
			
			Attachment am = new Attachment();
			long fileLength = -1;
			int pagesize = 50;
			
			String sql = "SELECT id FROM netdisk_document_attach WHERE doc_id=" + doc.getID() + " and page_num=1 and is_current=1 and is_deleted=0 order by ";
			sql += orderBy + " " + sort;
			
			if(fileCurrent.equals("current")){
				String currentName =  ParamUtil.get(request,"attachName");
				sql = "SELECT id FROM netdisk_document_attach WHERE name = " + StrUtil.sqlstr(currentName)+ " and user_name = " + StrUtil.sqlstr(root_code)+ " and page_num=1 and is_current=0 and is_deleted=1 order by ";
				sql += orderBy + " " + sort;
				%>
				
				<% 
			}
			if (select_sort.equals("select_one")) {
				if ((text_content.trim()).equals("请输入文件名搜索...")) {
					text_content = "";
				}
				sql = "SELECT id FROM netdisk_document_attach WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and user_name = " + StrUtil.sqlstr(root_code)+ " and page_num=1 and is_current=1 and is_deleted=0 order by ";
				sql += orderBy + " " + sort;
				%>
			
				
				<% 
			}
			if (select_file.equals("select_file")){
				// 将对应的文件写在配置文件中，方便后期维护
				String extType = cloudcfg.getProperty("exttype_" + which);
				String[] exts = extType.split(",");
				StringBuilder sb = new StringBuilder();
				for (String ext : exts) {
					sb.append(StrUtil.sqlstr(ext)).append(",");
				}
				if (sb.toString().endsWith(",")) {
					sb.deleteCharAt(sb.toString().length() - 1);
				}
				sql = "SELECT id FROM netdisk_document_attach WHERE page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=1 and is_deleted=0 and ext in" 
					+ (sb.toString().equals("") ? "" : "(" + sb.toString() + ")")
					+ " order by " + orderBy + " " + sort;%>
					
					
					<% 
			}
			// out.print(sql);
			if(select_file.equals("select_file")&& !which.equals("")){
				%>
			
				<%
			}
			HashMap<String,Boolean> imgMap = UtilTools.getExtType("exttype_1");
			HashMap<String,Boolean> browseMap = UtilTools.getExtType("exttype_5");
			HashMap<String,Boolean> editMap = UtilTools.getExtType("exttype_6");
			int isImgSearch = ParamUtil.getInt(request,"select_which",0);
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(SQLFilter.getCountSql(sql));
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				pagesize = rr.getInt(1);
			}
			int curpage = Integer.parseInt(strcurpage);
			ListResult lr = am.listResult(sql, curpage, pagesize);
			int total = lr.getTotal();
			Paginator paginator = new Paginator(request, total, pagesize);
			// 设置当前页数和总页数
			int totalpages = paginator.getTotalPages();
			if (totalpages==0)
			{
				curpage = 1;
				totalpages = 1;
			}
			  if (doc!=null) {
				  // Vector attachments = doc.getAttachments(1);
				  Vector attachments = lr.getResult();
				  Iterator ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	am = (Attachment) ir.next(); 
			
		%>
		 <li class="att" id="dirAtt<%=am.getId()%>" title=" <%=am.getName()%>" attId="<%=am.getId()%>" docId="<%=am.getDocId() %>" attExt="<%=am.getExt() %>" oldAttId="<%=am.getId()%>"  attName="<%=am.getName() %>"  isImgSearch="<%=isImgSearch%>">
		    	<input class="attDirCheckBox"  name="att_ids" type="checkbox" value="<%=am.getId()%>" />
		    	<input class="attExtType<%=am.getId() %>" value="<%= UtilTools.getConfigType(am.getExt())%>" type="hidden"/>
		    	<div class="attDirCheckIcon" ></div>
		   	 	<div class="attDirIcon"><div align="left"> 
                    <a target="_blank" url="clouddisk_downloadfile.jsp?attachId=<%=am.getId()%>" href="clouddisk_downloadfile.jsp?attachId=<%=am.getId()%>"><img src="images/clouddisk_tiled/<%=Attachment.getIcon(am.getExt()) %>" /></a></div>                  
		        </div>
		        <div class="attDirName" id="attName<%=am.getId() %>">
                    <a target="_blank" href="clouddisk_downloadfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>                  
		        </div>
		       
		    </li>
        <%}
        } %>
        </ul>
   
	
	<div id="treeBackground" class="treeBackground"></div>
	<div id='loading' class='loading'><img src='images/loading.gif'></div>
		<table id="mainTable" width="100%" border="0" cellpadding="0"
			cellspacing="0" style="border-top: 1px dashed #cccccc;" >
		</table>
		
		<table width="33%"  border="0" cellspacing="0" cellpadding="0"
			name="ctlTable" id="ctlTable"
			style=" height:150px;font-size: 15px;position:fixed;top:200px;left:350px; display:block;z-index:-100" >
			<!--<tr>
                    <td height="30" style="color:#888">&nbsp;空间：<%=strDiskAllow%>M,&nbsp;剩余：<%=strDiskHas%>M
                    &nbsp;&nbsp;<a href="dir_list_new.jsp?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle">普通方式</a>
					&nbsp;&nbsp;<a href="dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(dir_code)%>">共享管理</a></td>
                  </tr>-->
                  <tr>	
				<td>
					<tbody class="window_upload"><tr><td class='SD_upload'></td><td class='SD_upload'></td><td class='SD_upload'></td></tr>
					<tr><td class='SD_upload'></td>
						<td id='upload_container'>
						<h3 id='upload_title'>极速上传</h3>
						<div id='upload_body' style='height:75px;width:400px;overflow:auto; border:#999 4px solid;'><div id='upload_content' >
						<table border="0" align="center" cellpadding="0" cellspacing="1" >
						<tr>
							<td>
								<div style="width:100%; height:75px; margin-top: 10px; border: 0px solid #cccccc">
									<object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C"
										codebase="../activex/cloudym.CAB#version=1,2,0,1"
										width=100% style="height: 75px" align="middle" id="webedit">
										<param name="Encode" value="utf-8">
											<param name="MaxSize" value="<%=Global.MaxSize%>">
												<!--上传字节-->
												<param name="ForeColor" value="(200,200,200)">
													<param name="BgColor" value="(255,255,255)">
														<param name="ForeColorBar" value="(255,255,255)">
															<param name="BgColorBar" value="(104,181,200)">
																<param name="ForeColorBarPre" value="(0,0,0)">
																	<param name="BgColorBarPre" value="(230,230,230)">
																		<param name="FilePath" value="<%=filePath%>">
																			<param name="Relative" value="1">
																				<!--上传后的文件需放在服务器上的路径-->
																				<param name="Server"
																					value="<%=request.getServerName()%>">
																					<param name="Port"
																						value="<%=request.getServerPort()%>">
																						<param name="VirtualPath"
																							value="<%=Global.virtualPath%>">
																							<param name="PostScript"
																								value="<%=Global.virtualPath%>/netdisk/dir_list_do.jsp">
																								<param name="PostScriptDdxc"
																									value="<%=Global.virtualPath%>/netdisk/netdisk_ddxc.jsp">
																									<param name="SegmentLen" value="204800">
																										<param name="info" value="文件拖放区">
																											<%
							  License license = License.getInstance();	  
							  %>
																											<param name="Organization"
																												value="<%=license.getCompany()%>">
																												<param name="Key"
																													value="<%=license.getKey()%>">
									</object>
								</div>
								<script>//initUpload()</script>
							</td>
						</tr>
						</table>
                   </div></div>
						<div id='SD_button'>
							<div class='SD_button'>
							<a onclick="addform.webedit.OpenFolderDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()">上传目录</a>
							<a onclick="window_remove()">取消</a>
							</div>
						</div>
						<a href='javascript:;' id='upload_close' title='关闭'></a></td>
						<td class='SD_upload'></td></tr>
						<tr><td class='SD_upload'></td><td class='SD_upload'></td><td class='SD_upload'></td></tr></tbody>
                    </td>
                  </tr>
                  <tr>
                    <td align="center" height="10">
<!--
<input type="button" class="btn" value="上传文件" onClick="return SubmitWithFileThread()">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type="button" class="btn" value=" 上 传 " onClick="return SubmitWithoutFile()">
<input type="button" class="btn" value="断点上传" onClick="return SubmitWithFileDdxc()">
<input type="button" class="btn" onClick='alert(webedit.ReturnMessage)' value="返回信息"
>-->
<input type="hidden" name=isuploadfile value="true">
<input type="hidden" name=id value="<%=doc!=null?""+doc.getID():""%>">
<input type="hidden" name="op" value="<%=op%>">
<input type="hidden" name="title" value="<%=leaf.getName()%>">
<input type="hidden" name="dir_code" value="<%=dir_code%>">
<input type="hidden" name="examine" value="<%=Document.EXAMINE_PASS%>">
</td>
                  </tr>
              </table>
</form>
   
		  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="display:none">
            <tr>
              <td height="30" align="center">
			  <%if (mode.equals("select")) {%>
			  <input class="btn" name="button22" type="button" onclick="sel()" value="选择" />&nbsp;
			  <%}%>
			  <input class="btn" name="button23" type="button" onclick="send()" value="转发" />&nbsp;
			  <input class="btn" name="button24" type="button" onclick="moveBatch()" value="移动" />&nbsp;
			  <input class="btn" name="button25" type="button" onclick="shareBatch()" value="发布" />&nbsp;
              <input class="btn" style="margin-left:3px" name="button2" type="button" onclick="delBatch()" value="删除" />&nbsp;
			  <!--共 <b><%=paginator.getTotal() %></b> 个　每页显示 <b><%=paginator.getPageSize() %></b> 个　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b>-->
                <%
	// String querystr = "op=editarticle&orderBy=" + orderBy + "&sort=" + sort + "&dir_code=" + StrUtil.UrlEncode(dir_code);
    // out.print(paginator.getCurPageBlock("?"+querystr));
%>
                <input class="btn" name="button" type="button" onclick="addform.webedit.OpenFileDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()" value="上传文件" />&nbsp;
                <input class="btn" name="button" type="button" onclick="addform.webedit.OpenFolderDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()" value="上传目录" />&nbsp;
                <input class="btn" name="button" type="button" onclick="addform.webedit.StopUpload()" value="停止上传" />
				<div id="attFiles"></div>
				</td>
            </tr>
          </table>
		  <table id="rmofficeTable" name="rmofficeTable"  style="border:1px solid #cccccc; display:none; margin-top:10px" width="29%"  border="0" align="center" cellpadding="0" cellspacing="1">
            <tr>
              <td height="22" align="center" bgcolor="#eeeeee"><strong>&nbsp;编辑Office文件</strong></td>
            </tr>
            <tr>
              <td align="center">
                <div style="width:400px;height:43"><object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="../activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                  <param name="Encode" value="utf-8" />
                  <param name="BgColor" value="(255, 255, 255)" />
                  <param name="Server" value="<%=Global.server%>" />
                  <param name="Port" value="<%=Global.port%>" />
                  <!--设置是否自动上传-->
                  <param name="isAutoUpload" value="1" />
                  <!--设置文件大小不超过1M-->
                  <param name="MaxSize" value="<%=Global.FileSize%>" />
                  <!--设置自动上传前出现提示对话框-->
                  <param name="isConfirmUpload" value="1" />
                  <!--设置IE状态栏是否显示信息-->
                  <param name="isShowStatus" value="0" />
                  <param name="PostScript" value="<%=Global.virtualPath%>/netdisk/netdisk_office_upload.jsp" />
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />      
                </object></div>
                <!--<input name="remsg" type="button" onclick='alert(redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />--></td>
            </tr>
          </table>
		  <table width="100%"  border="0">
          <tr>
            <td align="center">
			<form name="form3" action="?" method="post"><input name="newname" type="hidden"></form>
			</td>
          </tr>
      </table>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
<form name=form11 action="?">
<tr><td>&nbsp;
<input name="op" type="hidden">
<input name="dirCode" type="hidden">
<input name="newName" type="hidden">
<input name="root_code" type="hidden">
</td></tr>
</form>
</table>


<table width="100%" border="0" cellpadding="0" cellspacing="0">
<tr><td>
<form name=form10 action="?">
<input name="op" type="hidden" value="editarticle">
<input name="action" type="hidden">
<input name="dir_code" type="hidden" value="<%=dir_code%>">
<input name="newName" type="hidden">
<input name="attach_id" type="hidden">
<input name="doc_id" type="hidden" value="<%=id%>">
<input name="page_num" type="hidden" value="1">
<input name="CPages" type="hidden" value="<%=curpage%>">
<input name="root_code" type="hidden">
</form>
<form name="hidForm" action="" method="post">
<input name="op" type="hidden" />
<input name="page_num" value="1" type="hidden" />
<input name="ids" type="hidden" />
<input name="ids_folder" type="hidden" />
<input name="doc_id" value="<%=doc.getID()%>" type="hidden" />
<input name="dir_code" value="<%=dir_code%>" type="hidden" />
<input name="docId" type="hidden" value="<%=doc.getID()%>"  />
<input name="netdiskFiles" type="hidden" />
</form>
</td></tr>
</table>
<iframe id="hideframe" name="hideframe" src="" width=330 height=330 style="display:none"></iframe>

</body>
<script>


var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	window.location.href = "?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle&orderBy=" + orderBy + "&sort=" + sort;
}

function doSort2(orderBy) {
	var ss = orderBy;
	if (orderBy==curOrderBy)
		
			sort = "desc";
	window.location.href = "?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle&orderBy=" + orderBy + "&sort=" + sort + "&sort0="+ ss ;
	
}

</script>
<ul id="contextMenuAttachMent" class="contextMenu">
	<li id="open" class="open">
		<a>打开</a>
	</li>
	<li id="download" class="down">
		<a>下载</a>
	</li>
	<li id="history" class="history">
		<a>历史版本</a>
	</li>
	<li id="reName" class="rename">
		<a>重命名</a>
	</li>
	<li id="delete" class="delete">
		<a>删除</a>
	</li>
	<li id="move" class="move">
		<a>移动</a>
	</li>
	<li id="publicShare" class="release">
		<a>发布</a>
	</li>
</ul>
<ul id="contextFloderMenu" class="contextMenu">
	<li id="download" class="down">
		<a>下载</a>
	</li>
	<li id="reName" class="rename">
		<a>重命名</a>
	</li>
	<li id="move" class="move">
		<a>移动</a>
	</li>
	<li id="delete" class="delete">
		<a>删除</a>
	</li>
	<li id="template" class="up" >
		<a>赋予角色</a>
	</li>
	<li id="cooperateFolder" class="cooperateFolder">
		<a>发起协作</a>
	</li>
	<li id="deCooperateFolder" class="deCooperateFolder">
		<a>解散协作</a>
	</li>	
	<!-- <li id="publicShare" >
		<a>公共共享</a>
	</li> -->
	
	
</ul>



<script language="JavaScript1.2">

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

var curDirCode = "";
var curDirName = "";

var curLeftClickDirCodeOld = "";
var curLeftClickDirCode = "";



var spanInnerHTML = "";
var operateOuterHTML = "";
var checkboxOuterHTML = "";
var historyOuterHTML ="";
var cooperateOuterHTML = "";
var downloadOuterHTML = "";
var ie5menu_tiledOuterHTML = "";
var itemExt ="";

function select_con(){
	var content = document.getElementById("select_content").value;
	if(content == "请输入文件名搜索..." ||$.trim(content) == ""){
		return;
	}else{
		window.location.href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&select_sort=select_one&select_content="+content;
	}
}

function select_file(which){

	window.location.href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&select_file=select_file&select_which="+which;
}




function movefile(item_id){
	if(item_id != ""){
		window.open ('clouddisk_change.jsp?attachId='+item_id , '文件移动', 'height=100, width=400, top=0,left=0, toolbar=no, target=_blank, menubar=no, scrollbars=no, resizable=no,location=no, status=no');
	}
}

function changeNameNew(dirCode,dirName) {
	if (dirCode!="") {
		spanObj = findObj("span" + dirCode);
		spanInnerHTML = spanObj.innerHTML; 
		//spanObj.innerHTML = "<input id='"+curAttachId+"' name='newName'  class='singleboarder'  size=22 value='" + curAttachName + "' onclick='select()'  onKeyDown=\"if (event.keyCode==13) this.blur()\">";
		//addform.newName.focus();
		//addform.newName.select();
		document.getElementById(dirCode).focus();
		document.getElementById(dirCode).select();
	}
}

function changeNameInfo(dirCode,dirName) { 
	if (dirCode!="") {
		var $span = $("#span"+dirCode);
		var name = "<a title='"+dirName+"' class=mainA  href='clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code="+dirCode+"' style='color:#888888' >"+dirName+"</a>";
		$span.html(name);
	} 
}


function doChange(dirCode, newName, oldName, spanObj) {
	if (newName.value=="") {
		jAlert("目录名称不能为空！","提示");
		return;
	}
	if (newName.value!=oldName) {
		form10.op.value = "changeName";
		form10.dir_code.value = dirCode;
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

function doChangeInfo(dirCode, newName, oldName, spanObj) {
	if (newName.value=="") {
		jAlert("目录名称不能为空！","提示");
		return;
	}
	if (newName.value!=oldName) {
		form10.op.value = "changeName";
		form10.dir_code.value = dirCode;
		form10.root_code.value = "<%=root_code%>";		
	}
	else {
		spanObj.innerHTML = spanInnerHTML;
	}
	curDirCode = "";
}

function move() {
	window.location.href = "dir_change.jsp?attachId=" + curAttachId;
}

function move(towhere) {
	if (curDirCode!="") {
		window.location.href = "?op=move&direction=" + towhere + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&code=" + curDirCode + "&dir_code=<%=dir_code%>";
		curDirCode = "";
	}
}

function del() {
	delAttach(curAttachId, id);
}

function openFile() {
	window.open("netdisk_getfile.jsp?id=" + id + "&attachId=" + curAttachId);
}

function publicShare() {
	window.location.href = "netdisk_public_share.jsp?attachId=" + curAttachId;
}

function selAllCheckBox(checkboxname){
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = true;
  }
  for (i=0; i<checkboxboxs.length; i++)
  {
	  checkboxboxs[i].checked = true;
  }
  }
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}



function moveBatch() {

	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择文件！","提示");
		return;
	}
	// window.location.href = "dir_change_batch.jsp?page_num=1&docId=<%=doc.getID()%>&ids=" + ids	
	hidForm.action = "dir_change_batch.jsp";
	hidForm.page_num.value = "1";
	hidForm.ids.value = ids;
	hidForm.submit();
}

function shareBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择文件！","提示");
		return;
	}
	// window.location.href = "netdisk_public_share_batch.jsp?page_num=1&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&ids=" + ids	
	hidForm.action = "netdisk_public_share_batch.jsp";
	hidForm.page_num.value = "1";
	hidForm.ids.value = ids;
	hidForm.submit();
	
}

function sel() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择文件！","提示");
		return;
	}
	window.top.opener.setNetdiskFiles(ids);
	window.top.close();
}
function send() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择文件！","提示");
		return;
	}
	// window.location.href = "../message_oa/message_ext/send.jsp?netdiskFiles=" + ids;
	hidForm.action = "../message_oa/message_ext/send.jsp";
	hidForm.netdiskFiles.value = ids;
	hidForm.submit();
}

function showDialogTree(attId,attOldId, docId) { 
	var type="<%=Leaf.TYPE_DOCUMENT%>";
	var user_name = "<%=userName%>";
	var mode = "<%=mode%>";
	curAttachId = attId;
	attId_old = attOldId;
	$.ajax({
	 		type:"get",
	 		url:"clouddisk_list_do.jsp",
	 		data:{"op":"showTree","type":type,"user_name":user_name,"mode":mode,"dir_code":attId+""},
	 		success:function(data,status){
	 			data = $.parseJSON(data);
	 			if(data.ret == "1"){
	 				showDialog("confirm",data.msg,"移动文件",500,400,"movefile");
	 			}else{
	 				jAlert(data.msg,"提示");
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
	});
	document.getElementById("treeBackground").style.display='block';
	//$('#SD_window').css({"display":"block"});
	//$(body).css({"filter":"alpha(opacity = 70)", "background-color": "#000"}); 
	//var a = document.getElementById("SD_window"); alert(a.innerHTML);
	//showDialogDirTree('window', 500); 
	shrink();
}

function closeDialog(){
	$('#SD_window').hide();
	$('.SD_overlayBG2').remove();
	$('.treeBackground').hide();
}

function closeBackGround(){
	$('.treeBackground').hide();
}

function treeMove(oldElement){
	$('#SD_window').hide();
	$('#dirAtt'+attId_old).remove(); //文件夹ajax删除
}
function showDigFile(){
		var upload = "<table  border='0' align='center' cellpadding='0' cellspacing='1'> <tr><td>";
		upload += "<div style='width:400;margin-top:10px;border:1px solid #cccccc'>";
		upload += "<object classid='CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C' codebase='../activex/cloudym.CAB#version=1,2,0,1' width=400 style='height:52px' align='middle' id='webedit'> <param name='Encode' value='utf-8'> <param name='MaxSize' value='<%=Global.MaxSize%>'><!--上传字节-->";
		upload += "<param name='ForeColor' value='(200,200,200)'><param name='BgColor' value='(255,255,255)'> <param name='ForeColorBar' value='(255,255,255)'>";
		upload += "<param name='BgColorBar' value='(104,181,200)'> <param name='ForeColorBarPre' value='(0,0,0)'><param name='BgColorBarPre' value='(230,230,230)'>";
		upload += "<param name='FilePath' value='<%=filePath%>'><param name='Relative' value='1'><!--上传后的文件需放在服务器上的路径--> <param name='Server' value='<%=request.getServerName()%>'> <param name='Port' value='<%=request.getServerPort()%>'><param name='VirtualPath' value='<%=Global.virtualPath%>'>";
		upload += "<param name='PostScript' value='<%=Global.virtualPath%>'/netdisk/dir_list_do.jsp'><param name='PostScriptDdxc' value='<%=Global.virtualPath%>'/netdisk/netdisk_ddxc.jsp'> <param name='SegmentLen' value='204800'> ";
		upload += "<param name='info' value='文件拖放区'> <param name='Organization' value='<%=license.getCompany()%>'><param name='Key' value='<%=license.getKey()%>'>";
		upload += "</object></div> </td> </tr> </table>";
		
		showDialogUpload('info',upload,'极速上传', 500);
}

function selectTree(dirCode, dirName){
	curDirCode = dirCode;
	curDirName = dirName;
	if (curDirCode!="") { 
		spanObj = findObj("spanTree" + curDirCode);
		spanInnerHTML = spanObj.innerHTML;  
		//spanObj.innerHTML = "<input id='"+curDirCode+"' class='singleboarder' size=22 value='" + curDirName + "' onfocus='this.select()'>";
		//$('.selectTree').css({"color":"#000"});	
		//$(spanObj).html("<span id='spanTree"+curDirCode+"' name='spanTree"+curDirCode+"' class='selectTree' style='background:#39F; color:#FFF'><a onclick='selectTree(\"" + curDirCode + "\",\"" + curDirName +"\")'>"+ curDirName+"</a></span>");
		//spanObj.innerHTML = "<span class='selectTree'><a onclick='selectTree(\"" + curDirCode + "\",\"" + curDirName +"\")'> " + curDirName+ "</a></span>";
	}
}
function window_remove() {
		$("#ctlTable").hide();
		$("#treeBackground").css({"display":"none"});
}
function upload_common(){
	$("#ctlTable").css({"z-index":"1000","top":"150px"});
	$("#webedit").css({"height":"75px"});
}

function upload_none(){
	$("#ctlTable").css({"z-index":"-1000","top":"-2000px"});
	$("#webedit").css({"height":"0px"});
}

</script>
</html>
