<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html;charset=utf-8"%>  
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
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.clouddisk.*"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>

<%@page import="java.text.DecimalFormat"%>
<%@page import="cn.js.fan.util.file.FileUtil"%>
<%@ page import="cn.js.fan.db.*"%>


<html>
	<head>
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>我的网盘</title>
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet" href="css/reset.css" />
		<link type="text/css" rel="stylesheet" href="<%=Global.getRootPath(request) %>/netdisk/clouddisk.css" />
		<link type="text/css" rel="stylesheet"href="<%=Global.getRootPath(request) %>/netdisk/showDialog/showDialog.css" />
		<script type="text/javascript">
			 document.onmousemove = function () {
			 var divx = window.event.clientX+"px";
			 var divy = window.event.clientY+"px";
			 }
		</script>
		<script src="../inc/common.js"></script>
		<script src="../inc/upload.js"></script>
		<script language=JavaScript src='formpost.js'></script>
		<script language=JavaScript src='showDialog/jquery.min.js'></script>
		<link href="../js/contextMenu/css/ContextMenu.css" rel="stylesheet"
			type="text/css" />
		<script src="../js/contextMenu/jquery.contextMenu.js"
			type="text/javascript"></script>
		<script language=JavaScript src='showDialog/showDialog.js'></script>
		<script src="../js/jquery-alerts/jquery.alerts.js"
			type="text/javascript"></script>
		<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
		<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"
			type="text/css" media="screen" />
		<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
		<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />
		<script type="text/javascript" src="../js/jquery.toaster.netdisk.js"></script> 
		<!-- swfupload 文件普通上传 -->
		<script src= "swfupload/swfupload.js"></script>
		<script type="text/javascript" src="swfupload/swfupload.queue.js"></script>
		<script src= "js/swfupload.js"></script>
		<script src="js/role_template.js"></script>
		<script src= "js/clouddisk.js"></script>
		<script src= "js/clouddisk_list.js"></script>
		<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil" />
		<jsp:useBean id="docmanager" scope="page"
			class="com.redmoon.oa.netdisk.DocumentMgr" />
		<jsp:useBean id="dir" scope="page"
			class="com.redmoon.oa.netdisk.Directory" />
		<jsp:useBean id="privilege" scope="page"
			class="com.redmoon.oa.pvg.Privilege" />
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
if (root_code.equals("")) {
	 //root_code = "root";
	 root_code = userName;
}

String dir_code = ParamUtil.get(request, "dir_code");
if("".equals(dir_code)){ 
	dir_code = root_code;
}

Leaf leaf = dir.getLeaf(dir_code);
if (leaf==null || !leaf.isLoaded()) {
	Leaf leafUser = new Leaf();
	leafUser.AddUser(dir_code);
	leaf =dir.getLeaf(dir_code);
	//out.print(SkinUtil.makeErrMsg(request, "该目录已不存在！"));
	//return;
}
//获得unit_code
String unit_code = privilege.getUserUnitCode(request);
String text_content = ParamUtil.get(request,"select_content");//搜索内容
String select_sort = ParamUtil.get(request,"select_sort"); //搜索判断
String select_file = ParamUtil.get(request,"select_file");//文件类别的判断
String which = ParamUtil.get(request,"select_which");

String sort0 = ParamUtil.get(request, "sort0");
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

// 防XSS
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
		window.location.href="?mode=<%=mode%>&op=AddChild&type=<%=Leaf.TYPE_DOCUMENT%>&parent_code=" + curDirCode + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&code=<%=leaf.getAutoCode()%>&name=<%=StrUtil.UrlEncode("新建文件夹")%>";
		curDirCode = "";
	}
}

function onAddFile(index, fileName, filePath, fileSize, modifyDate) {
	
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
	        jAlert("请输入文章标题.","提示");
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
		jAlert("请输入文章标题.","提示");
		document.addform.title.focus();			
		return false;
	}
	loadDataToWebeditCtrl(addform, addform.webedit);
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
		jAlert("请输入文章标题.","提示");
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
				location.href = "clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
			else
				location.href = "clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
		}
		else {
			location.href = "clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
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
		addform.webedit.height = "52px";
		btnObj.value = "隐 藏";
	}
	else {
		ctlTable.style.display = "none";
	
		//addform.webedit.height = "0px";
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

//javascript去空格函数 
</script>
		<script>

	$(function(){
		//var mode = '<%=mode%>';
		//lzm 加  流程中选择网盘附件
		//if( mode=='select'){
		//	$(".view").hide();
		//	$(".rHead1").hide();
		//}else{
		//	$(".view").show();
		//	$(".rHead1").show();
		//}
		var type = "";
		var root_code = "";
		var code = "";
		var name = "";	
		
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
		//确认移动树文件
		 $('#SD_confirm1').live("click",function(){
			attId = curAttachId;
			dirCode = curDirCode;
			dirName = curDirName;
			if(dirName == ""){
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
		 });
		
		 //文件单击重命名 出现input框
		  $(".fileReName").click(function(){
			  var oldAmId = $(this).attr("oldAmId");//文件重命名后重命名
			  var item_id = $(this).attr("amId");
			  var docId = $(this).attr("docId");
			  var ext = $(this).attr("ext");
			  var $root = $(this).parents("dd");
			  var $fileName =  $root.find(".fileNameDetail").find(".fname")//获得文件
			  var fileNameInfo =$fileName.find("a").text();//获得默认文件名
			  $fileName.html("<input id='"+ item_id +"' oldAmId='"+ oldAmId +"' oldAmName='"+fileNameInfo+"' docId='"+docId+"' name='newName' class='single_item dirAttInputText' ext='"+ext+"'  value='"+fileNameInfo+"'>");
			  curAttachName = fileNameInfo;
		 	  curAttachId = item_id;
			  $(".single_item").get(0).select();
		   });
		  //文件重命名  文本框移除事件
		  var attachReName = function(){ 
			 	attName = $(this).val();
			 	attId = $(this).attr("id");//获得id
			 	attDocId = $(this).attr("docId");//获得Docid
			 	var oldAmId = $(this).attr("oldAmId");//文件重命名后重命名
			 	var ext = $(this).attr("ext");
			 	var curAttachName = $(this).attr("oldAmName");
			 	attId_old = attId;
			 	if($.trim(attName)==''){
			 		changeFileNameAfter(attId,attId,curAttachName,attDocId,oldAmId,ext); 
			 		$.toaster({ priority : 'info', message : '名称不能为空！' });
				 }else{
					 if(curAttachName == attName){
						 	//如果文件名称未变
					 		changeFileNameAfter(attId,attId,curAttachName,attDocId,oldAmId,ext); 
					 		return;
					 	}
						 var endLen = attName.lastIndexOf(".");//文本高亮显示
						 if(endLen == 0){
							 $.toaster({ priority : 'info', message : '名称不能为空！' });
							 changeFileNameAfter(attId,attId,curAttachName,attDocId,oldAmId,ext); 
							 return; 
						 }
					 	$.ajax({
					 		type:"post",
					 		url:"clouddisk_list_do.jsp",
							contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
					 		data:{"op":"changeFileName","type":type,"att_id":attId,"att_name":attName,"att_oldId":attId, "att_oldId":attId_old, "att_docId":attDocId},
					 		success:function(data,status){
					 			data = $.parseJSON(data);
					 			if(data.ret == "1"){
					 				 changeFileNameAfter(data.attId,data.attOldId,data.name,data.attDocId,oldAmId,data.ext,data.url,data.config);
					 				 $.toaster({ priority : 'info', message : '重命名成功！' });
					 			}else{
					 				 changeFileNameAfter(curAttachId,curAttachId,curAttachName,attDocId,oldAmId,ext);
					 				 $.toaster({ priority : 'info', message : '重命名失败！' });
					 			}
					 		},
					 		error:function(XMLHttpRequest, textStatus){
					 			alert(XMLHttpRequest.responseText);
					 		}
					 	});
				 }

		  }
		  $(".single_item").live({"blur":attachReName,"keydown":function(e){
				if(e.keyCode==13){
					this.blur();
				}
			 }});
	});
	/**
	 * 文件重命名
	 */
	function changeFileNameAfter(item_id, item_oldId, newName, item_docId,oldAmId,ext,url,config){
		if(item_id != "" && item_oldId!=""){//如果当前id与最新id都不为空
			var $dl = $("#tree"+oldAmId);
			var $fname = $dl.find("dd").find(".fileNameDetail").find(".fname");
			var $openImg = $(".open"+oldAmId);
			var $extType = $(".attExtType"+oldAmId);
			var name ;
			if(item_id == item_oldId){//如果当前id与最新id一致说明 文件名相同
				name = "<a href='clouddisk_downloadfile.jsp?attachId="+item_id +"'>"+newName+"</a>";
			}else{
				name = "<a href='clouddisk_downloadfile.jsp?attachId="+item_id +"'>"+newName+"</a>";
				var $cbox = $dl.find("dd").find(".fileNameDetail").find(".cbox");//更新checkbox属性
				$cbox.val(item_id);
				//更新extImg图标
				var $extImg = $dl.find("dd").find(".fileNameDetail").find(".extImg");
				var $extList = $dl.find("dd").find(".extT").find("span");
				$extList.html(ext);
				$extImg.attr("src","images/sort/"+url);
				$fname.attr({"id":"span"+item_id,"name":"span"+item_id,"attId":item_id,"amOldId":oldAmId,"ext":ext,"amId":item_id,"docId":item_docId});
				var $file_action = $dl.find("dd").find(".fileNameDetail").find(".file_action").find("ul");
				$file_action.find("li:eq(0)").find("a").attr({"href":"javascript:edittxt("+item_docId+", "+item_id+", \""+ext+"\")"}).unbind("click");
				$file_action.find("li:eq(1)").find("a").attr({"id":"history"+item_id,"name":"history"+item_id,"onclick":""}).unbind("click");//取消click事件为了后面不同情况绑定方法
				if(config == 5){
					$openImg.show();
					$file_action.find("li:eq(0)").find("a").attr({"href":"javascript:edittxt("+item_docId+", "+item_id+", \""+ext+"\")"});
					$extType.attr({"value":"5"});
				}else if(config == 6){
					$openImg.show();
					$file_action.find("li:eq(0)").find("a").attr({"href":"javascript:editdoc("+item_docId+", "+item_id+", \""+ext+"\")"});
					$extType.attr({"value":"6"});
				}else if(config == 1){
					$openImg.show();
					$file_action.find("li:eq(0)").find("a").attr({"href":"javascript:;","onclick":""}).click(function(){
					});
					$extType.attr({"value":"1"});
				}else{
					$openImg.hide();
					$extType.attr({"value":"0"});
				}
				$file_action.find("li:eq(1)").find("a").attr({"id":"history"+item_id,"name":"history"+item_id,"onclick":""}).click(function(){
					showDigHistory(item_id);
				});
				//$file_action.find("li:eq(1)").find("a").attr({"id":"cooperate"+item_id,"name":"cooperate"+item_id,"href":"netdisk_public_share.jsp?attachId"+item_id});
				$file_action.find("li:eq(2)").find("a").attr({"id":"download"+item_id,"name":"download"+item_id,"href":"clouddisk_downloadfile.jsp?attachId="+item_id});
				
				var $op = $dl.find("dd").find(".fileNameDetail").find(".op");
				$op.attr("id","operate"+item_id);
				$op.find("li:eq(0)").find("a").attr({"amOldId":item_oldId,"amId":item_id,"docId":item_docId});
				$op.find("li:eq(1)").find("a").attr({"href":"javascript:showDialogTree("+item_id+","+oldAmId+","+item_docId+")"})  //参数无引号，用以区分number和string
				$op.find("li:eq(2)").find("a").attr({"href":"javascript:delAttach('"+item_id+"','"+item_docId+"','"+oldAmId+"')"});
				
			}
			$fname.html(name);
		}
	}
	


</script>
	</head>

	<body oncontextmenu="return false" onload="upload_none(),getSharedDir('<%=privilege.getUser(request) %>')">
	<div id="barcode" class="barcode">
		<div class="barcode_title">扫描可下载 <img style="float:right;cursor:pointer;width:25px;height:25px;" src="images/del.gif"  onclick="delBarcode()"/> <a style="float:right;margin-right:5px;" href="pirntTdcode.jsp" target="_blank">打印</a></div>
		<div class="barcode_img"><img src="" id="barcodeImg"/></div>
	</div>
		&nbsp;
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

// OA中封装的防SQL注入
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "sort", sort, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "text_content", text_content, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
Leaf dlf = new Leaf();
if (doc!=null) {
	dlf = dlf.getLeaf(doc.getDirCode());
}

%>


		<div id="Right" class="Right">
			<div class="rHead">
				<div class="fixedDiv">
				<div class="rHead1">
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
					<div class="newFolder"></div>
					<div class="deleteFile_c" onclick="delBatch()"></div>
					
					<%if (!mode.equals("select")) {%>
					<div class="view">
						<div id="view_1"
							style="cursor: pointer; background: url(images/clouddisk/view_list_1.gif); width: 30px; height: 24px; border-right: 1px solid #cacaca; float: left"
							onclick="cloud_list('<%=dir_code %>')"></div>
						<div id="view_2"
							style="cursor: pointer; background: url(images/clouddisk/view_thumbnail_2.gif); width: 29px; height: 24px; float: left"
							onclick="tiled_list('<%=dir_code %>')"></div>
					</div>
					<%} %>
					<div class="sort" >
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
			</div>-->
					</div>
				</div>
				<div class="rHead2" style="background: #f7f7f7;display:block">
					<%
						String showPath = "";
						boolean backFlag = true;
						
						if(select_sort.equals("select_one")) {
							showPath = "<a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >    <a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"&select_sort=select_one&select_content="+StrUtil.UrlEncode(text_content)+"'>“" + text_content + "”的搜索结果</a>";
						
						%>
							 <div style="float:left;margin-left:15px;margin-top:4px;"><a href="clouddisk_list.jsp?mode=<%=mode %>&userName=<%=StrUtil.UrlEncode(userName) %>"><img src="images/clouddisk/back.png"/></a></div>
						<%}else{
							if (dir_code.equals(root_code)) {
								backFlag = false;
								showPath = "<a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>";
							} else if (leaf.getParentCode().equals(root_code)) {
								showPath = "<a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    <a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
							} else {
								Leaf pleaf = new Leaf(leaf.getParentCode());
								if (leaf.getLayer() == 3) {
									showPath = "<a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    <a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
								} else {
									showPath = "<a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    ...>>    <a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_list.jsp?mode="+mode+"&userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
								}
							}
						
						 %>
						 <%if(backFlag) {%>
						 <div style="float:left;margin-left:15px;margin-top:4px;"><a href="clouddisk_list.jsp?mode=<%=mode %>&userName=<%=StrUtil.UrlEncode(userName) %>&dir_code=<%=StrUtil.UrlEncode(leaf.getParentCode()) %>"><img src="images/clouddisk/back.png"/></a></div>
						 <%}
						}%>
						 <div class='all_file'><%=showPath %></div>
				
						<% 
          		if("".equals(text_content)){ text_content = "请输入文件名搜索..."; }
          %>
						<div class="search">
							<input maxlength="100" value="<%=text_content %>"
								id="select_content"
								onKeyDown="if (event.keyCode==13) {document.getElementById('search_one').click()}"
								onfocus="if(this.value=='请输入文件名搜索...') this.value=''" />
						</div>
						<a>
							<div id="search_one"
								style="float: right; width: 36px; height: 26px; position: relative; top: 5px; right: -184px"
								onclick="select_con()"></div> </a>
						<div class="sortShow">
							<span>分类显示：</span>
							<ul>
								<%
			   // 防XSS
				try {
					com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_file", select_file, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_which", which, getClass().getName());
				}
				catch (ErrMsgException e) {
					out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
					return;
				}
			  	if("select_file".equals(select_file)){
			  		if("1".equals(which)){	%>
								<li class="imgShow">
									<img id="img_sort" style="cursor: pointer"
										src="images/clouddisk/imgShow_2.gif"
										title = "图片"
										onclick="select_file(1)">
								</li>
								<li class="fileShow">
									<img id="file_sort" style="cursor: pointer"
										title = "文件"
										src="images/clouddisk/fileShow_1.gif" onclick="select_file(2)" />
								</li>
								<li class="videoShow">
									<img id="video_sort" style="cursor: pointer"
										src="images/clouddisk/videoShow_1.gif"
										title = "视频"
										onclick="select_file(3)" />
								</li>
								<li class="musicShow">
									<img id="music_sort" style="cursor: pointer"
										title = "音乐"
										src="images/clouddisk/musicShow_1.gif"
										onclick="select_file(4)" />
								</li>
								<%}
			  		else if ("2".equals(which)){%>
								<li class="imgShow">
									<img id="img_sort" style="cursor: pointer"
										src="images/clouddisk/imgShow_1.gif"
										title = "图片" onclick="select_file(1)">
								</li>
								<li class="fileShow">
									<img id="file_sort" style="cursor: pointer"
										title = "文件"
										src="images/clouddisk/fileShow_2.gif" onclick="select_file(2)" />
								</li>
								<li class="videoShow">
									<img id="video_sort" style="cursor: pointer"
										src="images/clouddisk/videoShow_1.gif"
										title = "视频"
										onclick="select_file(3)" />
								</li>
								<li class="musicShow">
									<img id="music_sort" style="cursor: pointer"
										src="images/clouddisk/musicShow_1.gif"
										title = "音乐"
										onclick="select_file(4)" />
								</li>
								<%}
			  		else if ("3".equals(which)){%>
								<li class="imgShow">
									<img id="img_sort" style="cursor: pointer"
										src="images/clouddisk/imgShow_1.gif"
										title = "图片" onclick="select_file(1)">
								</li>
								<li class="fileShow">
									<img id="file_sort" style="cursor: pointer"
										title = "文件"
										src="images/clouddisk/fileShow_1.gif" onclick="select_file(2)" />
								</li>
								<li class="videoShow">
									<img id="video_sort" style="cursor: pointer"
										src="images/clouddisk/videoShow_2.gif"
										title = "视频"
										onclick="select_file(3)" />
								</li>
								<li class="musicShow">
									<img id="music_sort" style="cursor: pointer"
										src="images/clouddisk/musicShow_1.gif"
										title = "音乐"
										onclick="select_file(4)" />
								</li>
								<%}
			 		else if ("4".equals(which)){%>
								<li class="imgShow">
									<img id="img_sort" style="cursor: pointer"
										src="images/clouddisk/imgShow_1.gif"
										title = "图片" onclick="select_file(1)">
								</li>
								<li class="fileShow">
									<img id="file_sort" style="cursor: pointer"
										title = "文件"
										src="images/clouddisk/fileShow_1.gif" onclick="select_file(2)" />
								</li>
								<li class="videoShow">
									<img id="video_sort" style="cursor: pointer"
										src="images/clouddisk/videoShow_1.gif"
										title = "视频"
										onclick="select_file(3)" />
								</li>
								<li class="musicShow">
									<img id="music_sort" style="cursor: pointer"
										src="images/clouddisk/musicShow_2.gif"
										title = "音乐"
										onclick="select_file(4)" />
								</li>
								<%}
			  } else {%>
								<li class="imgShow">
									<img id="img_sort" style="cursor: pointer"
										src="images/clouddisk/imgShow_1.gif"
										title = "图片" onclick="select_file(1)">
								</li>
								<li class="fileShow">
									<img id="file_sort" style="cursor: pointer"
										title = "文件"
										src="images/clouddisk/fileShow_1.gif" onclick="select_file(2)" />
								</li>
								<li class="videoShow">
									<img id="video_sort" style="cursor: pointer"
										src="images/clouddisk/videoShow_1.gif"
										title = "视频"
										onclick="select_file(3)" />
								</li>
								<li class="musicShow">
									<img id="music_sort" style="cursor: pointer"
										src="images/clouddisk/musicShow_1.gif"
										title = "音乐"
										onclick="select_file(4)" />
								</li>
								<%}%>
							</ul>
							
						</div>
					</div>
					<dl class="fileTitleDl">
					<dd class="fileTitle">
						<div class="fileNameDetailTitle">
							<span class="cbox_all"></span>
							<input id="filename_input" name="checkbox" class="title_cbox"
								type="checkbox" />
							<span class="fnameTitle" >文件名</span>
							<%if (orderBy.equals("name")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' onClick=doSort('name') width=8px height=7px style='position:absolute;top:20px;left:90px;cursor:pointer;'>");
							else
								out.print("<img src='images/arrow_down.gif' onClick=doSort('name') width=8px height=7px style='position:absolute;top:20px;left:90px;cursor:pointer;'>");
				}%>
						</div>
						<div onClick="doSort('file_size')"class="colTitle">
							<span>大小</span>
							<%if (orderBy.equals("file_size")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' onClick=doSort('file_size') width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' onClick=doSort('file_size') width=8px height=7px>");
				}%>
						</div>
						<div onClick="doSort('ext')" class="colTitle">
							<span >类型</span>
							<%if (orderBy.equals("ext")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' onClick=doSort('ext') width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' onClick=doSort('ext') width=8px height=7px>");
				}%>
						</div>
						<div onClick="doSort('version_date')" class="colTitle dateT" >
							<span >修改日期</span>
							<%if (orderBy.equals("version_date")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' onClick=doSort('version_date') width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' onClick=doSort('version_date') width=8px height=7px>");
						}%>

						</div>
						<div class="colTitle catalogT" style="display:none;" >
							<span >所在目录</span>
						</div>
						
						
						
					</dd>
				</dl>
			</div>
				<form name="addform" action="fwebedit_do.jsp" method="post"
					style="padding:0px; ">
					<input name="template_flag" type="hidden" />
					<!-- lzm 添加用于平铺界面 列表界面切换 -->
					<input type="hidden" value="<%=select_sort %>" id="isSearch"/>
					<input type="hidden" value="<%=select_file %>" id="fileType"/>
					<input type="hidden" value="<%=which%>" id="which"/>
					<input type="hidden" value="<%=StrUtil.UrlEncode(text_content) %>"  id="content"/>
					<input type="hidden" value="<%=StrUtil.UrlEncode(userName) %>"  id="userName"/>
					<!-- lzm判断列表页 平铺页 -->
					<input type="hidden" value="0" id="curPage" />
					<!-- lzm 新增文件夹界面 -->
				   <input type="hidden" value="<%=dir_code%>" id="curDirCode" />
				   <input type="hidden" value="<%=Leaf.TYPE_DOCUMENT%>" id="type" />
				   <input type="hidden" value="<%=root_code%>" id="rootCode" />
				   <input type="hidden" value="<%=Leaf.getAutoCode()%>" id="code" />
				    <input type="hidden" id="unitCode" value="<%=unit_code%>" />
				   <!-- lzm添加 角色模板 -->
				   <!-- lzm SwfUpload文件批量上传 -->
				   	<input type="hidden" value="netdisk_office_upload.jsp?jsessionid=<%=session.getId() %>&userName=<%=StrUtil.UrlEncode(userName) %>&dirCode=<%=StrUtil.UrlEncode(dir_code) %>" id="uploadUrl" />
					<input type="hidden" value="<%=file_size_limit %>" id="fileSizeLimit" />
					<input type="hidden" value="<%=file_upload_limit %>" id="fileUploadLimit" />
					<input type="hidden" value="<%=upload_file_types %>" id="uploadFileType" />
					<input type="hidden" value="<%=fixfox_upload_file_types %>" id="FixfoxUploadFileType" />
					<input type="hidden" id="cooperateId" value="" />
					
			<div class="containtCenter">
				<%
				String fileCurrent = ParamUtil.get(request,"cur");
			try {
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_sort", select_sort, getClass().getName());
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "cur", fileCurrent, getClass().getName());
			}
			catch (ErrMsgException e) {
				out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
				return;
			}%>
			<% 
			 if(!select_sort.trim().equals("select_one") && !select_file.trim().equals("select_file")){
			    
                Iterator irch = leaf.getChildren().iterator();
				while (irch.hasNext()) {
				Leaf clf = (Leaf)irch.next();
				Leaf parentLeaf = new Leaf(clf.getParentCode());
				%>
				<dl>
					<dd class="fileGroup" id="folder<%=clf.getCode()%>" type='file' isShared="<%=clf.isShared() %>">
						<div class="fileNameDetail">
							<span class="cbox_icon"></span>
							<input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" value="<%=clf.getCode()%>" />
							<%if(!clf.isShared()){ %>
								<img src="images/sort/folder.png" class="extImg" />
							<%}else{ %>
								<img src="images/folder_netdisk_share.png" class="extImg" />
							<%} %>
							<span class="fname floderNameInfo" id="span<%=clf.getCode()%>"
								dirCode="<%=clf.getCode()%>" name="span<%=clf.getCode()%>">
								<a title="<%=clf.getName()%>"
								href="clouddisk_list.jsp?userName=<%=userName %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>"><%=clf.getName()%></a>
							</span>
							<div class="file_action">
				                <ul>
				                	<li>
										<a id="download<%=clf.getCode() %>" target="_blank"
											href="clouddisk_downloaddir.jsp?code=<%=clf.getCode() %> "><img
												src="images/clouddisk/download_1.gif" title="下载" />
										</a>
									</li>
									<li>
										<a id="reName<%=clf.getCode()%>" onclick="CDirNameBefore('<%=clf.getCode() %>')" >
											<img src="images/clouddisk/rename.gif" title="重命名" />
										</a>
									</li>
									<li>
										<a id="moveDir<%=clf.getCode()%>" href="javascript:showDialogTree('<%=clf.getCode()%>','<%=clf.getCode()%>','<%=clf.getDocId() %>')" >
											<img src="images/clouddisk/move.gif" title="移动" />
											
										</a>
									</li>
									<li>
										<a id="delete<%=clf.getCode() %>" onclick = "delFolder('<%=clf.getName() %>','<%=clf.getCode() %>',0)" ><img
												src="images/clouddisk/recycler_1.gif" title="删除" />
										</a>
									</li>
								 </ul>
							</div>
						</div>
						<div class="col">
							<span></span>
						</div>
						<div class="col">
							<span>文件夹</span>
						</div>
						<div class="col dateT">
							<span></span>
						</div>
						<div class="col catalogT" style="display:none;" >
							<span ><%=parentLeaf.getName()%></span>
						</div>
					</dd>
				</dl>
				<%
                }
           }else if(select_sort.trim().equals("select_one")){ 
        		
   				String sql = "SELECT code FROM netdisk_directory WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and root_code = " + StrUtil.sqlstr(root_code)+ " and isDeleted=0 order by add_date desc";
   				Iterator irch = leaf.getSearchChildren(sql).iterator();
   				while (irch.hasNext()) {
   					Leaf clf = (Leaf)irch.next();
   					Leaf parentLeaf = new Leaf(clf.getParentCode());
           
           %>
           		<dl>
					<dd class="fileGroup" id="folder<%=clf.getCode()%>" type='file' isShared="<%=clf.isShared() %>">
						<div class="fileNameDetail">
							<span class="cbox_icon"></span>
							<input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" value="<%=clf.getCode()%>" />
							<%if(!clf.isShared()){ %>
								<img src="images/sort/folder.png" class="extImg" />
							<%}else{ %>
								<img src="images/folder_netdisk_share.png" class="extImg" />
							<%} %>
							<span class="fname floderNameInfo" id="span<%=clf.getCode()%>"
								dirCode="<%=clf.getCode()%>" name="span<%=clf.getCode()%>">
								<a title="<%=clf.getName()%>"
								href="clouddisk_list.jsp?userName=<%=userName %>&op=editarticle&dir_code=<%=StrUtil.UrlEncode(clf.getCode())%>&mode=<%=mode%>"><%=clf.getName()%></a>
							</span>
							<div class="file_action">
				                <ul>
				                	<li>
										<a id="download<%=clf.getCode() %>" target="_blank"
											href="clouddisk_downloaddir.jsp?code=<%=clf.getCode() %> "><img
												src="images/clouddisk/download_1.gif" title="下载" />
										</a>
									</li>
				               		<li>
										<a id="delete<%=clf.getCode() %>" onclick = "delFolder('<%=clf.getName() %>','<%=clf.getCode() %>',0)" ><img
												src="images/clouddisk/recycler_1.gif" title="删除" />
										</a>
									</li>
									<li>
										<a id="reName<%=clf.getCode()%>" onclick="CDirNameBefore('<%=clf.getCode() %>')" >
											<img src="images/clouddisk/rename.gif" title="重命名" />
										</a>
									</li>
									<li>
										<a id="moveDir<%=clf.getCode()%>" href="javascript:showDialogTree('<%=clf.getCode()%>','<%=clf.getCode()%>','<%=clf.getDocId() %>')" >
											<img src="images/clouddisk/edit.gif" title="移动" />
											
										</a>
									</li>
								 </ul>
							</div>
						</div>
						<div class="col">
							<span></span>
						</div>
						<div class="col">
							<span>文件夹</span>
						</div>
						<div class="col dateT">
							<span></span>
						</div>
						<div class="col catalogT"  style="display:none;">
							<span ><a href="clouddisk_list.jsp?mode=<%=mode %>&userName=<%=StrUtil.UrlEncode(userName) %>&dir_code=<%=clf.getParentCode() %>"><%=parentLeaf.getName()%></a></span>
						</div>
					</dd>
				</dl>
          	
           <% }
   				}
	   		 %>
				

				<%
			String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
			try {
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "CPages", strcurpage, getClass().getName());
			}
			catch (ErrMsgException e) {
				out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
				return;
			}
			if (strcurpage.equals(""))
				strcurpage = "1";
			if (!StrUtil.isNumeric(strcurpage)) {
				out.print(StrUtil.makeErrMsg("标识非法！"));
				return;
			}
			
			Attachment am = new Attachment();
			double fileLength = -1.0;
			DecimalFormat   df   =   new   DecimalFormat("#####0.00"); 
			int pagesize = 50;
			
			String sql = "SELECT id FROM netdisk_document_attach WHERE doc_id=" + doc.getID() + " and page_num=1 and is_current=1 and is_deleted=0 order by ";
			sql += orderBy + " " + sort;
			
			if(fileCurrent.equals("current")){
				String currentName =  ParamUtil.get(request,"attachName");
				try {
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "attachName", currentName, getClass().getName());
				}
				catch (ErrMsgException e) {
					out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
					return;
				}
				sql = "SELECT id FROM netdisk_document_attach WHERE name = " + StrUtil.sqlstr(currentName)+ " and user_name = " + StrUtil.sqlstr(root_code)+ " and page_num=1 and is_current=0 and is_deleted=1 order by ";
				sql += orderBy + " " + sort;
			}
			if (select_sort.equals("select_one")) {
				if ((text_content.trim()).equals("请输入文件名搜索...")) {
					text_content = "";
				}
				sql = "SELECT id FROM netdisk_document_attach WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and user_name = " + StrUtil.sqlstr(root_code)+ " and page_num=1 and is_current=1 and is_deleted=0 order by ";
				sql += orderBy + " " + sort;
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
					+ " order by " + "ext" + " " + sort;
			}
			// out.print(sql);
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
			long total = lr.getTotal();
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
				  	am = (Attachment)ir.next();
					String theLength = UtilTools.getFileSize(am.getSize());
					%>
				<dl id="tree<%=am.getId()%>" class="fileDl">
					<dd class="fileGroup">
						<div class="fileNameDetail">
							<span class="cbox_icon"></span>
							<input class="cbox" name="att_ids" 
								type="checkbox" value="<%=am.getId()%>" />
							<input class="attExtType<%=am.getId() %>" value="<%= UtilTools.getConfigType(am.getExt())%>" type="hidden"/>
							<img src="images/sort/<%=Attachment.getIcon(am.getExt())%>"
								class="extImg" />
							<span class="fname attNameInfo" id="span<%=am.getId() %>"
								name="span<%=am.getId() %>" attId="<%=am.getId()%>" docId="<%=am.getDocId()%>" ext="<%=am.getExt()%>" amOldId="<%=am.getId()%>" isImgSearch="<%=isImgSearch %>">
                                <a target="_blank" href="clouddisk_downloadfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>
							</span>
							<div class="file_action">
								<ul>
                                    <%
                                    String ext = am.getExt();
									if (editMap.containsKey(ext)) {
									%>
									<li class="open<%=am.getId()%>">
										<a href="javascript:editdoc('<%=id%>', '<%=am.getId()%>', '<%=am.getExt() %>')"><img
												src="images/clouddisk/edit.gif" title="编辑" />
										</a>
									</li>
									<%}else if(browseMap.containsKey(ext)){%>
									<li class="open<%=am.getId()%>">
										<a href="javascript:edittxt('<%=id%>', '<%=am.getId()%>', '<%=am.getExt() %>')"><img
												src="images/clouddisk/edit.gif" title="预览" />
										</a>
									</li>
									<%}else if(imgMap.containsKey(ext)){%>
										<li class="open<%=am.getId()%>">
											<a href="javascript:;" onclick="showImg(<%=am.getId() %>,<%=isImgSearch %>)" ><img
													src="images/clouddisk/edit.gif" title="预览" />
											</a>
										</li>
									<% }else{%>
									<li class="open<%=am.getId()%>" style="display:none">
										<a href="javascript:editdoc('<%=id%>', '<%=am.getId()%>', '<%=am.getExt() %>')"><img
												src="images/clouddisk/edit.gif" title="编辑" />
										</a>
									</li>
									<%}%>
									<li>
										<a id="download<%=am.getId() %> "
											name="download<%=am.getId()%>" target="_blank"
											href="clouddisk_downloadfile.jsp?attachId=<%=am.getId()%>"><img
												src="images/clouddisk/download_1.gif" title="下载" />
										</a>
									</li>
									<li>
										<a id="history<%=am.getId() %>" name="history<%=am.getId() %>" onclick="showDigHistory('<%=am.getId() %>')"><img
												src="images/clouddisk/look_1.gif" title="历史版本" />
										</a>
									</li>
									<li>
										<a id="barcode<%=am.getId() %>" name="barcode<%=am.getId() %>" onclick="showBarcode('<%=am.getId() %>')"><img
												src="images/clouddisk/phone_1.gif" title="点击下载至手机" />
										</a>
									</li>
									<!--  <li><a id="cooperate<%=am.getId() %>" name="cooperate<%=am.getId() %>" href="netdisk_public_share.jsp?attachId=<%=am.getId()%>"><img src="images/clouddisk/share_1.gif" title="协作"/></a></li>-->
									
									<li>
										<a><img class="pulldown"
												src="images/clouddisk/pulldown_1.gif" title="更多操作" />
										</a>
									</li>
								</ul>
							</div>
							<ul class="op" id="operate<%=am.getId() %>">
								<li>
									<a href="javascript:void(0)" class="fileReName"
										oldAmId="<%=am.getId()%>" amId="<%=am.getId()%>"
										ext="<%=am.getExt() %>" docId="<%=am.getDocId() %>">重命名</a> 
								</li>
								<li>
									<a
										href="javascript:showDialogTree(<%=am.getId()%>,<%=am.getId()%>,<%=am.getDocId() %>)">移动</a><!--备注：移动选项中的参数不加“”，以区分文件夹的String参数和附件的number参数  -->
								</li>
								<li>
									<a
										href="javascript:delAttach('<%=am.getId()%>', '<%=doc.getID()%>','<%=am.getId()%>')">删除</a>
								</li>
								<li>
									<a
										href="netdisk_public_share.jsp?pageNo=1&attachId=<%=am.getId()%>">发布</a>
								</li>
							</ul>
						</div>
						<div class="col">
							<span><%=theLength%></span>
						</div>
						<div class="col extT">
							<span><%=am.getExt()%></span>
						</div>
						<div class="col dateT">
							<span><%=DateUtil.format(am.getVersionDate(), "yyyy-MM-dd HH:mm")%></span>
						</div>
						<div class='col catalogT' style="display:none;">
							<span><a href="clouddisk_list.jsp?mode=<%=mode %>&userName=<%=StrUtil.UrlEncode(userName) %>&dir_code=<%=am.getCodeByDocId() %>"><%=am.getParentDir() %></a></span>
						</div>
					</dd>
				</dl>
				<%}
        } %>
			</div>

		<div id="treeBackground" class="treeBackground"></div>
		<div id='loading' class='loading'><img src='images/loading.gif'></div>
		

		<table id="mainTable" width="100%" border="0" cellpadding="0"
			cellspacing="0" style="border-top: 1px dashed #cccccc;">
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
					
					<tbody class="window_upload" ><tr><td class='SD_upload'></td><td class='SD_upload'></td><td class='SD_upload'></td></tr>
					<tr><td class='SD_upload'></td>
						<td id='upload_container'>
						<h3 id='upload_title'>极速上传</h3>
						<div id='upload_body' style='height:75px;width:400px;overflow:auto;'><div id='upload_content' >
						<table border="0" align="center" cellpadding="0" cellspacing="1" >
						<tr>
							<td>
								<div style="width:100%; height:75px; margin-top: 10px; border: 0px solid #cccccc">
									<object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C"
										codebase="../activex/cloudym.CAB#version=1,2,0,1"
										width=100% style="height:75px" align="middle" id="webedit">
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

					<input type="hidden" name=isuploadfile value="true">
						<input type="hidden" name=id
							value="<%=doc!=null?""+doc.getID():""%>">
							<input type="hidden" name="op" value="<%=op%>">
								<input type="hidden" name="title" value="<%=leaf.getName()%>">
									<input type="hidden" name="dir_code" value="<%=dir_code%>">
										<input type="hidden" name="examine"
											value="<%=Document.EXAMINE_PASS%>">
				</td>
			</tr>
		</table>
		</form>
		<%if (mode.equals("select")) {%>
		<table width="100%" border="0" cellspacing="0" cellpadding="0"
			style="margin-left: 50px;">
			<tr>
				<td>
					<input class="btn" name="button22" type="button" onclick="sel()"
						value="确定" />
					&nbsp;
				</td>
			</tr>
		</table>
		<%}%>
		<table width="100%" border="0" cellspacing="0" cellpadding="0"
			style="display:none">
			<tr>
				<td height="30" align="center">
					<%if (mode.equals("select")) {%>
					<input class="btn" name="button22" type="button" onclick="sel()"
						value="选择" />
					&nbsp;
					<%}%>
					<input class="btn" name="button23" type="button" onclick="send()"
						value="转发" />
					&nbsp;
					<input class="btn" name="button24" type="button"
						onclick="moveBatch()" value="移动" />
					&nbsp;
					<input class="btn" name="button25" type="button"
						onclick="shareBatch()" value="发布" />
					&nbsp;
					<input class="btn" style="margin-left: 3px" name="button2"
						type="button" onclick="delBatch()" value="删除" />
					&nbsp;
					<!--共 <b><%=paginator.getTotal() %></b> 个　每页显示 <b><%=paginator.getPageSize() %></b> 个　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b>-->
					<%
	// String querystr = "op=editarticle&orderBy=" + orderBy + "&sort=" + sort + "&dir_code=" + StrUtil.UrlEncode(dir_code);
    // out.print(paginator.getCurPageBlock("?"+querystr));
%>
					<input class="btn" name="button" type="button"
						onclick="addform.webedit.OpenFileDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()"
						value="上传文件" />
					&nbsp;
					<input class="btn" name="button" type="button"
						onclick="addform.webedit.OpenFolderDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()"
						value="上传目录" />
					&nbsp;
					<input class="btn" name="button" type="button"
						onclick="addform.webedit.StopUpload()" value="停止上传" />
					<div id="attFiles"></div>
				</td>
			</tr>
		</table>
		<table id="rmofficeTable" name="rmofficeTable"
			style="border: 1px solid #cccccc; display: none; margin-top: 10px"
			width="29%" border="0" align="center" cellpadding="0" cellspacing="1">
			<tr>
				<td height="22" align="center" bgcolor="#eeeeee">
					<strong>&nbsp;编辑Office文件</strong>
				</td>
			</tr>
			<tr>
				<td align="center">
					<div style="width: 400px; height: 43px;">
						<object id="redmoonoffice"
							classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="../activex/cloudym.CAB#version=1,2,0,1" width="316"
							viewastext="viewastext">
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
							<param name="PostScript"
								value="<%=Global.virtualPath%>/netdisk/netdisk_office_upload.jsp" />
							<param name="Organization" value="<%=license.getCompany()%>" />
							<param name="Key" value="<%=license.getKey()%>" />
						</object>
					</div>
					<!--<input name="remsg" type="button" onclick='alert(redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />-->
				</td>
			</tr>
		</table>
		<table width="100%" border="0">
			<tr>
				<td align="center">
					<form name="form3" action="?" method="post">
						<input name="newname" type="hidden">
					</form>
				</td>
			</tr>
		</table>
		<table width="100%" border="0" cellpadding="0" cellspacing="0">
			<form name=form11 action="?">
				<tr>
					<td>
						&nbsp;
						<input name="op" type="hidden">
							<input name="dirCode" type="hidden">
								<input name="newName" type="hidden">
									<input name="root_code" type="hidden">
					</td>
				</tr>
			</form>
		</table>


		<table width="100%" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td>
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
						<input name="doc_id" value="<%=doc.getID()%>" type="hidden" />
						<input name="dir_code" value="<%=dir_code%>" type="hidden" />
						<input name="docId" type="hidden" value="<%=doc.getID()%>" />
						<input name="netdiskFiles" type="hidden" />
						<input id="mode" name="mode" value="<%=mode %>" type="hidden" />
					</form>
				</td>
			</tr>
		</table>
		<iframe id="hideframe" name="hideframe" src="" width=330 height=330
			style="display: none"></iframe>

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
	window.location.href = "?mode=<%=mode%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle&orderBy=" + orderBy + "&sort=" + sort;
}

function doSort2(orderBy) {
	var ss = orderBy;
	if (orderBy==curOrderBy)
			sort = "desc";
	window.location.href = "?mode=<%=mode%>&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle&orderBy=" + orderBy + "&sort=" + sort + "&sort0="+ ss ;
	
}

</script>
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
		<li id="template" class="up">
			<a>赋予角色</a>
		</li>
		<li id="cooperateFolder" class="cooperateFolder">
			<a>发起协作</a>
		</li>
		<li id="deCooperateFolder" class="deCooperateFolder">
			<a>解散协作</a>
		</li>
	</ul>
	<ul id="contextAttachMentMenu" class="contextMenu">
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
		<li id="move" class="move">
			<a>移动</a>
		</li>
		<li id="delete" class="delete">
			<a>删除</a>
		</li>
		<li id="publicShare" class="release">
			<a>发布</a>
		</li>

	</ul>
	<script type="text/javascript">

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

function select_con(){
	var content = $("#select_content").val();
	
	if(content == "请输入文件名搜索..." || $.trim(content) == ""){
		return;
	}
	else{
		window.location.href="clouddisk_list.jsp?mode=<%=mode%>&userName=<%=StrUtil.UrlEncode(userName) %>&select_sort=select_one&select_content="+content;
	}
}

function select_file(which){
	window.location.href="clouddisk_list.jsp?mode=<%=mode%>&userName=<%=StrUtil.UrlEncode(userName) %>&select_file=select_file&select_which="+which;
}




function movefile(item_id){
	if(item_id != ""){
		window.open ('clouddisk_change.jsp?attachId='+item_id , '文件移动', 'height=100, width=400, top=0,left=0, toolbar=no, target=_blank, menubar=no, scrollbars=no, resizable=no,location=no, status=no');
	}
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



function openFile() {
	window.open("netdisk_getfile.jsp?id=" + id + "&attachId=" + curAttachId);
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


function delAttachInfo(attId){
	if (attId!="") {
		spanObj = findObj("tree" +attId);
		//spanInnerHTML = spanObj.innerHTML;  
		//$(spanObj).html("<a title='"+dirName+"' class=mainA  href='clouddisk_list.jsp?op=editarticle&dir_code="+dirCode+"' style='color:#888888' onmouseup=\"onMouseUp('"+dirCode+"', '"+dirName+"')\">"+dirName+"</a>");
		spanObj.outerHTML = "";
		//var newName = curAttachId+"";
		//addform.newName.focus();
		//addform.newName.select() ;
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

function sel() {
	var ids = getAllCheckedValue("input[name='att_ids']"); 
	if (ids=="") {
		jAlert("请先选择文件！","提示");
		return;
	}
	window.top.opener.setNetdiskFiles(ids);
	window.top.close();
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
	if(typeof(oldElement) == "number"){
		$('#SD_window').hide();
		var treeObj = findObj("tree"+attId_old); //附件ajax删除
		$(treeObj).html("");
	}else{
		$('#SD_window').hide();
		$('#folder'+oldElement).remove(); //文件夹ajax删除
	}
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
function showDigFile(){
		var upload = "<table border='0' align='center' cellpadding='0' cellspacing='1'> <tr><td>";
		upload += "<div style='width:400;margin-top:10px;border:1px solid #cccccc'>";
		upload += "<object classid='CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C' codebase='../activex/cloudym.CAB#version=1,2,0,1' width=400 style='height:180px' align='middle' id='webedit'> <param name='Encode' value='utf-8'> <param name='MaxSize' value='<%=Global.MaxSize%>'><!--上传字节-->";
		upload += "<param name='ForeColor' value='(200,200,200)'><param name='BgColor' value='(255,255,255)'> <param name='ForeColorBar' value='(255,255,255)'>";
		upload += "<param name='BgColorBar' value='(104,181,200)'> <param name='ForeColorBarPre' value='(0,0,0)'><param name='BgColorBarPre' value='(230,230,230)'>";
		upload += "<param name='FilePath' value='<%=filePath%>'><param name='Relative' value='1'><!--上传后的文件需放在服务器上的路径--> <param name='Server' value='<%=request.getServerName()%>'> <param name='Port' value='<%=request.getServerPort()%>'><param name='VirtualPath' value='<%=Global.virtualPath%>'>";
		upload += "<param name='PostScript' value='<%=Global.virtualPath%>'/netdisk/dir_list_do.jsp'><param name='PostScriptDdxc' value='<%=Global.virtualPath%>'/netdisk/netdisk_ddxc.jsp'> <param name='SegmentLen' value='204800'> ";
		upload += "<param name='info' value='文件拖放区'> <param name='Organization' value='<%=license.getCompany()%>'><param name='Key' value=alert'<%=license.getKey()%>'>";
		upload += "</object></div> </td> </tr> </table>";
		
		showDialogUpload('info',upload,'极速上传', 500);
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

///发起协作over--------------------

</script>
</html>
