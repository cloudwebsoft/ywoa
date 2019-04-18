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
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<%@page import="cn.js.fan.util.file.FileUtil"%>

<html>
<head>
<meta http-equiv="cache-control" content="no-cache">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="clouddisk.css"/>
<link type="text/css" rel="stylesheet" href="showDialog/showDialog.css"/>
 <script type="text/javascript" >
 document.onmousemove = function () {
 var divx = window.event.clientX+"px";
 var divy = window.event.clientY+"px";
 //var ie5menua = document.getElementById("ie5menu");

 }
 </script>
 <style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>
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
String userName = privilege.getUser(request);
String root_code = ParamUtil.get(request, "root_code");
if (root_code.equals("")) {
	 //root_code = "root";
	 root_code = userName;
}

Leaf leaf = new Leaf();
leaf.setParentCode(userName);
leaf.setName("角色模板");
String dir_code = "";

if (leaf.getCodeByName()) {
	dir_code = leaf.getCode();
} else {
	dir.setName("角色模板");
	dir.setParentCode(userName);
	dir.setType(Leaf.TYPE_DOCUMENT);
	dir.setCode("");
	dir.setUserName(userName);
	boolean res = dir.AddChild();
	if(res){
		dir_code = dir.getCode();
	}
}

leaf = leaf.getLeaf(dir_code);

String sort0 = ParamUtil.get(request, "sort0");
if("".equals(sort0)){
	sort0 = "时间";
}else if(sort0.equals("file_size")){
	sort0 = "大小";
}
else if(sort0.equals("name")){
	sort0 = "名称";
}
else if(sort0.equals("uploadDate")){
	sort0 = "时间";
}

String dir_name = "";
 
int id = 0;

String correct_result = "操作成功！";

Document doc = new Document();
doc = docmanager.getDocumentByCode(request, dir_code, privilege);
Document template = null;

if (leaf==null || !leaf.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该目录已不存在！"));
	return;
}

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

boolean res = false;

if (op.equals("template")){
	/*
	RoleTemplateMgr rt = new RoleTemplateMgr();
	try{
		res = rt.uploadRoleTemplate(request);
	}catch(ErrMsgException e){
		out.print(StrUtil.Alert_Redirect(e.getMessage(), "clouddisk_template.jsp"));
		return;
	}
	if (res) {
		out.print(StrUtil.Alert_Redirect("操作成功！","clouddisk_template.jsp"));
	} else {
		out.print(StrUtil.Alert_Redirect("操作失败！","clouddisk_template.jsp"));
	}
	*/
	return;
}
if (op.equals("editarticle")) {
	op = "edit";
	try {
		doc = docmanager.getDocumentByCode(request, dir_code, privilege);
		dir_code = doc.getDirCode();
		
	
		
	} catch (ErrMsgException e) {
		out.print(StrUtil.makeErrMsg(e.getMessage(), "red", "green"));
		return;
	}
}

else if (op.equals("myclouddisk")) {
	op = "edit";
	try {
		String fileName = ParamUtil.get(request, "name");
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
		out.print(StrUtil.makeErrMsg(e.getMessage(), "red", "green"));
		return;
	}
}
else if (op.equals("AddChilddd")) {
	String parent_code = ParamUtil.get(request, "parent_code");
	boolean re = false;
	dir = new Directory();
	try {
		re = dir.AddChild(request);
		if (re) {
			response.sendRedirect("clouddisk_list.jsp?op=editarticle&dir_code="+parent_code);
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
else if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "dirCode");
	String curDirCode = ParamUtil.get(request, "curDirCode"); 
	UserMgr um = new UserMgr();
	UserDb ud = um.getUserDb(privilege.getUser(request));
	if (delcode.equals("" + ud.getName())) {
		out.print(StrUtil.Alert("根目录不能被删除"));
	}
	
	else {	
		 dir = new Directory();
		Leaf lf = dir.getLeaf(delcode);
		if (lf!=null) { // 防止反复刷新
			try {
				dir.del(delcode);
			}
			catch (ErrMsgException e) {
				out.print(StrUtil.Alert(e.getMessage()));
			}
			
			out.print(StrUtil.Alert_Redirect("操作成功！","clouddisk_list.jsp?op=editarticle&dir_code="+dir_code));
			return;
		}
	}
}
else if (op.equals("select")) {
	//String parent_code = ParamUtil.get(request, "parent_code");
	//boolean re = false;
	//dir = new Directory();
	//try {
	//	re = dir.AddChild(request);
	//	if (re) {
	//		response.sendRedirect("clouddisk_list.jsp?op=editarticle&dir_code="+parent_code);
	//		return;
	//	}		
	//}
	//catch (ErrMsgException e) {
		//out.print(StrUtil.Alert(e.getMessage()));
	//}
	
}
else if (op.equals("changeName-----")) {
	String newName = ParamUtil.get(request, "newName");
	String dirCode = ParamUtil.get(request, "dir_code");
	 dir = new Directory();
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
	out.print(StrUtil.Alert_Redirect("修改成功！","clouddisk_list.jsp"));
}
else if (op.equals("move")) {
	 dir = new Directory();
	try {
		dir.move(request);
		if (true) {
			response.sendRedirect("clouddisk_list.jsp?root_code=" + StrUtil.UrlEncode(root_code)+"&dir_code="+dir_code);
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

String action = ParamUtil.get(request, "action");

if (doc!=null) {
	id = doc.getID();
	Leaf lfn = new Leaf();
	lfn = lfn.getLeaf(doc.getDirCode());
	dir_name = lfn.getName();
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk");

RoleDb rb =new RoleDb();
Vector vec = rb.getRolesOfUnit(privilege.getUserUnitCode(request));
Iterator it = vec.iterator();
StringBuilder sbTemplate = new StringBuilder();
sbTemplate.append("<table>");
while (it.hasNext()) {
	RoleDb roleDb = (RoleDb)it.next();
	String code = roleDb.getCode();
	String name = roleDb.getDesc();
	sbTemplate.append("<tr><td style='font-size:14px; color:#757373'><input name='template_ids' type='checkbox' value='").append(code).append("' /> ").append("&nbsp;&nbsp;&nbsp;").append(name).append("</td></tr>");
}
sbTemplate.append("</table>");
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
	curDirCode="<%=dir_code%>";
	type="<%=Leaf.TYPE_DOCUMENT%>";
	root_code = "<%=root_code%>";
	if (!confirm(root_code)){
		return;
	}
	var name ="<%="角色模板"%>";
	var ids = getCheckboxValue("ids"); 
	var root_code = "<%=root_code%>";
	var pluginCode = "default";
	
	if(ids == "") { 
		alert("请选择角色用户");
		return;
	}
	if (document.addform.title.value.length == 0) {
		alert("请输入文章标题.");
		document.addform.title.focus();			
		return false;
	}
	loadDataToWebeditCtrl(addform, addform.webedit);
	addform.webedit.Upload();
	
	// 因为Upload()中启用了线程的，所以函数在执行后，会立即反回，使得下句中得不到ReturnMessage的值
	// 原因是此时服务器的返回信息还没收到
	// alert("ReturnMessage=" + addform.webedit.ReturnMessage);
	window.setTimeout("checkResult()",200);
	window.location.href="clouddisk_template.jsp?op=template&ids="+ids+"&root_code="+root_code;
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
				location.href = "clouddisk_template.jsp?op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
			else
				location.href = "clouddisk_template.jsp?op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
		}
		else {
			location.href = "clouddisk_template.jsp?op=editarticle&dir_code=<%=StrUtil.UrlEncode(dir_code)%>&mode=<%=mode%>";
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
		addform.webedit.height = "75px";
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
	
		$(".newFolder").click(function(){
			curDirCode="<%=dir_code%>";
			type="<%=Leaf.TYPE_DOCUMENT%>";
			root_code = "<%=root_code%>";
			code = "<%=leaf.getAutoCode()%>";
			name ="<%="新建文件夹"%>";
			$.ajax({
				type:"post",
				url:"clouddisk_list_do.jsp",
				data :{"op":"AddChild","type":type,"parent_code":curDirCode,"root_code":root_code,"code":code,"name":name},
				success: function(data, status){
					data = $.parseJSON(data);
					if (data.ret=="1") {
						isDocDistributed = true;
						var d =  " <tr class='size_floder'  id='size_folder"+data.code+"' style='position:relative' onclick='removeMenu()'><td width='3%' align='center'>&nbsp;</td>";
						d+= "<td width='4%' height='20' align='center'><img src='images/folder.gif' align='absmiddle'></td>";
						d+="<td width='38%' align='left'>";
						d+="<div style='width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; padding:0px; margin:0px'><span id='span"+data.code+"' name='span"+data.name+"'>";         
						d+="<input id='"+data.code+"' name='newName' class='singleboarder' onfocus='this.select()'  size = 22 type='text'  value='"+data.name+"'/ ></span></div></td>";                  
						d+="</td><td width='12%'>&nbsp;</td><td width='18%'>&nbsp;</td><td width='25%'>&nbsp;</td></tr>";
						
						var $tr =$(d);
						 $(".file_table").append($tr);
						 //赋值，以便判定新建文件夹是否修改名字； 
						 curDirCode = data.code;
						 curDirName = data.name;
						 changeNameNew(data.code,data.name);
					}
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});
		 });
		 
		 $(".singleboarder").live("blur",function(){
		 	curDirCode="<%=dir_code%>";
			type="<%=Leaf.TYPE_DOCUMENT%>";
			root_code = "<%=root_code%>";
			code = this.id;
		 	name = $(this).val();  
		 	// 判断是否修改
		 	if (curDirName == name) {
		 		changeNameInfo(code, curDirName);
		 		return;		 	
		 	}
		 	
		 	$.ajax({
		 		type:"post",
		 		url:"clouddisk_list_do.jsp",
		 		data :{"op":"changeName","type":type,"parent_code":curDirCode,"root_code":root_code,"code":code,"name":name},
		 		success:function(data, status){
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				changeNameInfo(data.code,data.name);
		 			} else {
		 				//jAlert(data.msg, "提示");
		 				alert(data.msg);
					}
		 		},
		 		error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
		 	});
		 	
		 });
		 
		 
		 $(".single_item").live("blur",function(){
		 	attName = $(this).val();
		 	attId = this.id;
		 	attDocId = document.getElementById("hiddenText").value;
		 	attId_old = attId;
		 	if(curAttachName == attName){
		 		changeFileNameAfter(curAttachId,curAttachId,curAttachName,attDocId); 
		 		return;
		 	}
		 	$.ajax({
		 		type:"get",
		 		url:"clouddisk_list_do.jsp",
		 		data:{"op":"changeFileName","type":type,"att_id":attId,"att_name":attName,"att_oldId":attId, "att_oldId":attId_old, "att_docId":attDocId},
		 		success:function(data,status){
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				
		 				changeFileNameAfter(data.attId,data.attOldId,data.name,data.attDocId);
		 				//window.location.href="clouddisk_list.jsp"; 
		 			}else{
		 				alert(data.msg);
		 			}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
		 	});
		 });
		 
		 
		 $('#SD_confirm').click(function(){
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
				data:{"op":"moveFile", "type":type, "att_id":attId, "dirCode":dirCode, "dirName":dirName, "doc_id":docId_old},
				success:function(data,status){
					data = $.parseJSON(data);
					if(data.ret == "1"){ 
						if(data.doc_id == docId_old){
							closeDialog();
							return;
						}
						treeMove();
						alert(data.msg);
					}else{
						alert(data.msg);
					}
				},
				error:function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});
		 });
		 
		 
		 $('#template_confirm').live("click",function(){
		 	var role_codes = getCheckboxValue("template_ids"); 
		 	var dir_code = curDirCode;
		 	if (role_codes=="") {
					alert("请先选择文件！");
					return;
			}
			if (!confirm("您确定要对这些角色上传此模板么？")){ 
				sd_remove();
				return;
			}else{
				sd_remove();
				var type="<%=Leaf.TYPE_DOCUMENT%>";
				$.ajax({
				 		type:"get",
				 		url:"clouddisk_list_do.jsp",
				 		data:{"op":"role_template","type":type,"role_codes":role_codes,"dir_code":dir_code},
				 		success:function(data,status){
				 			data = $.parseJSON(data);
				 			if(data.ret == "1"){
				 				alert(data.msg);
				 			}
				 			else{
				 				alert(data.msg);
				 			}
				 		},
				 		error:function(XMLHttpRequest, textStatus){
				 			alert(XMLHttpRequest.responseText);
				 		}
				});
			}
		 })
	 
	});
	

  

</script>
</head>

<body> &nbsp;	
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
	orderBy = "uploadDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";	
Leaf dlf = new Leaf();
if (doc!=null) {
	dlf = dlf.getLeaf(doc.getDirCode());
}
UserDb ud = new UserDb();
ud = ud.getUserDb(userName);
String strDiskAllow = NumberUtil.round((double)(ud.getDiskSpaceAllowed()/1024000), 1);
String strDiskHas = NumberUtil.round((double)(ud.getDiskSpaceAllowed()-ud.getDiskSpaceUsed())/1024000, 1);
%>
    
  <table cellSpacing="0" cellPadding="0" width="100%" style="margin-top:-13px;">
		  <tbody>
		    <tr>
		      <td class="tdStyle_1">角色模板</td>
		    </tr>
		  </tbody>
	</table>
 <div id="Right" >
 	<form name="addform" action="fwebedit_do.jsp" method="post" style="padding:0px; margin:0px">
 	<input name="template_flag" value="0" type="hidden"/>
      <!-- <div class="rHead">
	    <div class="rHead1" style="position:relative">
		  <a onclick="addform.webedit.OpenFileDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()"><div class="uploadFile_c"></div></a>
         
		  <div class="upload_sel--" id="upload_sel--" style="display:none" >
		    <ul>
		      <li><a onclick="addform.webedit.OpenFileDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()">普通</a></li>
		      <li><a onclick="addform.webedit.OpenFileDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()">极速</a></li>
		    </ul>
		  </div>
		  <div class="newFolder" ></div>
		  <a onclick="delBatch()"><div class="deleteFile_c"></div></a>
		  <div class="view" >
		    <div id="view_1"  style="cursor:pointer;background:url(images/clouddisk/view_list_1.gif); width:30px; height:24px; border-right:1px solid #cacaca; float:left" onclick="cloud_list('<%=dir_code %>')"></div>
			<div id="view_2" style="cursor:pointer;background:url(images/clouddisk/view_thumbnail_2.gif); width:29px; height:24px; float:left" onclick="tiled_list('<%=dir_code %>')"></div>
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
		    <span>排序：</span>
			<span id="sort_sel"><%=sort0 %><img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span>
			<div id="sort_sel_pull">
			  <p style="margin-top:5px "><a style="cursor:pointer" onClick="doSort2('uploadDate')"><span>时间<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px; "/></span></a></p>
			  <p style="margin-top:5px"><a style="cursor:pointer" onClick="doSort2('file_size')"><span>大小<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span></a></p>
			  <p style="margin-top:5px"><a style="cursor:pointer" onClick="doSort2('name')"><span>名称<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span></a></p>
			</div>
            <script>
            
            	function tiled_list(dir_code){ 
            		window.location.href="clouddisk_tiled.jsp?dir_code="+dir_code;
            	}
            	
            	function cloud_list(dir_code){
            		window.location.href="clouddisk_list.jsp?dir_code="+dir_code;
            	}
            	
              $(function(){
				$("#sort_sel_pull").live("mouseleave",function(){
				
					$("#sort_sel_pull").hide();
				});
				
				$("#sort_sel").click(
				  function(){
					 if( $("#sort_sel_pull").is(":hidden")){
					 $("#sort_sel_pull").show(); 
				
					}
					else{
					 $("#sort_sel_pull").hide(); 
					 
					}
				  }
				)
			  
			  
			  
			  
				//$('.uploadFile_c').click(function(){
				//if($("#upload_sel").is(":visible")){
				//	$(this).css({"background":"url(images/clouddisk/uploadFile_1.gif)"});
				//	$("#upload_sel").hide();
				//}else{
				//	$(this).css({"background":"url(images/clouddisk/uploadFile_2.gif)"});
				//	$("#upload_sel").show();
				//}
			
				//});
				
				$('.uploadFile_c').mousedown(function(){
					$(this).css({"background":"url(images/clouddisk/uploadFile_2.gif)"});
				});
				
				$('.uploadFile_c').mouseup(function(){
					$(this).css({"background":"url(images/clouddisk/uploadFile_1.gif)"});
				});
				
				
				
				$('.newFolder').mousedown(function(){
					$(this).css({"background":"url(images/clouddisk/newFolder_2.gif)"});
				});
				
				$('.newFolder').mouseup(function(){
					$(this).css({"background":"url(images/clouddisk/newFolder_1.gif)"});
				});
				
				
				$('.deleteFile_c').mousedown(function(){
					$(this).css({"background":"url(images/clouddisk/deleteFile_2.gif)"});
				});
				
				$('.deleteFile_c').mouseup(function(){
					$(this).css({"background":"url(images/clouddisk/deleteFile_1.gif)"});
				});
				
				//全选
				$('.filename :checkbox').click(function(){
						//$(this) jquery对象
						//this javacript对象
						if(this.checked){
								$(".size_a :checkbox").attr("checked","checked");
								$('._dd_style').css({"background":"#f0f8fd"});
							}else{
								$(".size_a :checkbox").removeAttr("checked");
								$('._dd_style').css({"background":""});
						}
						
						
					});
					
				
					
				})
					
				
		
            </script>
            
		  </div>
		</div>
		<form name="addform" action="fwebedit_do.jsp" method="post" style="padding:0px; margin:0px">
		<!--  <div class="rHead2" style=" background:#f7f7f7;  z-index:20" >
		  <a href="clouddisk_list.jsp"><div class="all_file">所有文件</div></a>
          <script>
           $(function(){
			  $(".all_file").click(function(){
				$(".sortShow ul li").each(function(index, element) {
					var a=$(this).attr("class");
                    var src='images/clouddisk/'+$(this).attr("class")+'_1.gif';
					$(this).find("img").attr("src",src);
					var b=$(this).attr("class");
                });  
			  });
			  
			  
			  
			  
				
		   });
		   
		  
          </script>
          <% String text_content = ParamUtil.get(request,"select_content");
          		if("".equals(text_content)){ text_content = "请输入文件名搜索..."; }
          %>
		  <div class="search"><input maxlength="30" value="<%=text_content %>" id="select_content" onblur="select_con()" onKeyDown="if (event.keyCode==13) {this.blur()}" onfocus="if(this.value=='请输入文件名搜索...') this.value=''"/></div>
		  <a>
            <div id="search_one" style=" float:right;width:36px;height:26px;position:relative; top:5px;right:-184px" onclick="select_con()"></div>
          </a>
		  <div class="sortShow" >
		    <span>分类显示：</span>
			<ul>
			  <%String select_file = ParamUtil.get(request,"select_file");
			    String which = ParamUtil.get(request,"select_which");
			   
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
            <script>
              $(function(){
				$(".sortShow ul li").click(
				  function(){
					 var src='images/clouddisk/'+$(this).attr("class")+'_2.gif';
					 var srcClass=$(this).attr("class");
					 $(this).find("img").attr("src",src);
					 $(".sortShow ul li").each(function(index, element) {
						 var a=$(this).attr("class");
						 var srcInfo='images/clouddisk/'+a+'_1.gif';
                        if(a!=srcClass){
						  $(this).find("img").attr("src",srcInfo);
						}
						
						
                    });
				  }
				)  
			  })
            </script>
		  </div>
		</div>
	  </div>-->
      
        
        
        <script>
			  	
          $(function(){
			$(".pulldown").click(function(){
				//$(this).parent().parent().parent().parent().parent().find("input").attr("checked","checked");
			  if($(this).parent().parent().parent().parent().parent().find(".operate").is(":visible")){
				  $("._dd_style").bind({"mouseover":onHover,"mouseout":overHover});
				  $(this).parent().parent().parent().parent().parent().find(".operate").hide();
				
			  }else{
				  $("._dd_style").bind({"mouseover":onHover,"mouseout":overHover});
				  $(this).css({"background":"#f0f8fd"}); 
			  	  $(this).find(".more_action").show();
				  $(this).parent().parent().parent().parent().parent().find(".operate").show();
				  $(this).parent().parent().parent().parent().parent().find(".operate").css({"display":"block"});
				  $(this).parent().parent().parent().parent().parent().parent().find(".size_c").hide();
				  $(this).parent().parent().parent().parent().parent().parent().find(".type_c").hide();
			  }
			});
			
			$(".time_c").click( function () {
				  var $file = $(this).parent().find('input');
					  if($file.is(':checked')){
						  $file.removeAttr("checked");
					  }else{
						  $file.attr("checked","checked");
						  }
			 });
			$(".type_c").click( function () {
				  var $file = $(this).parent().find('input');
					  if($file.is(':checked')){
						  $file.removeAttr("checked");
					  }else{
						  $file.attr("checked","checked");
						  }
			 }); 
			$(".size_c").click( function () {
				 var $file = $(this).parent().find('input');
					  if($file.is(':checked')){
						  $file.removeAttr("checked");
					  }else{
						  $file.attr("checked","checked");
						  }
			 }); 
			
			
		  })
				
            
		function onHover(){
			   $(this).parent().find("._dd_style").css({"background":"#FFF"});
			   $(this).css({"background":"#f0f8fd"});
			   $(this).find(".more_action").show();	
			   $(this).parent().find(".size_c").hide();
			   $(this).parent().find(".type_c").hide();
		}
		function overHover(){
				 if($(this).find('.size_a').find('input').is(':checked')){
				 	$(this).css({"background":"#f0f8fd"});
				 }else{
					 $(this).css({"background":""});
				  }
			    $(this).find(".more_action").hide();	
			    $(".size_c").show();
				$(".type_c").show();
					
		}
		
		
        $(function(){ 
        	$(".size_floder").bind({"mouseover":onHover,"mouseout":overHover});
		 	$("._dd_style").bind({"mouseover":onHover,"mouseout":overHover});
			$(".filename_c").click(function(){
				 	var $file = $(this).parent().find('input');
					  if($file.is(':checked')){
						  $file.removeAttr("checked");
					  }else{
						  $file.attr("checked","checked");
						  }
					$(".operate").css({"display":"none"});
					//if($(".size_a").find('input').is(':checked')){
						//$(this).parent().parent().css({"background":"#f0f8fd"});
					//}
					//$(".more_action").hide();
				});
				
			$("._dd_style").live("mouseleave", function(){
				$(".operate").hide();
				$(".more_action").hide();
				$("._dd_style").bind({"mouseover":onHover,"mouseout":overHover});
			});
			
			$(".operate").live("mouseover",function(){
				$(this).parent().parent().parent().find(".size_c").hide();
				$(this).parent().parent().parent().find(".type_c").hide();
			})
		 })
		 
		 
        </script>
        
        <table width="100%"  border="0" cellpadding="0" cellspacing="0" class="file_table" oncontextmenu="return(false)">
                
		 <%
			String select_sort = ParamUtil.get(request,"select_sort"); 
			String fileCurrent = ParamUtil.get(request,"cur");
			
			if(fileCurrent.equals("current")){
				String currentName =  ParamUtil.get(request,"attachName");
				text_content = "";
			%>
				<tr class="size_floder" onclick="removeMenu()">
                    <td width="3%" align="center">&nbsp;</td>
                    <td width="38%" align="left" colspan=2 style="font-size:18">
                    		
      					您正在搜索<span style="color:red;"><%=currentName %></span> &nbsp; 的历史版本：   &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;以下是您的搜索结果：
                    </td>
                    <td width="12%">&nbsp;</td>
                    <td width="18%">&nbsp;</td>
                    <td width="25%">&nbsp;</td>
                  </tr>
			<%}
			if(select_sort.equals("select_one")){
				if("请输入文件名搜索...".equals(text_content)){
				text_content="";}
			%>
           
           		<tr class="size_floder" onclick="removeMenu()">
                    <td width="3%" align="center">&nbsp;</td>
                    <td width="38%" align="left" colspan=2 style="font-size:18">
                    		
      					您搜索的内容是：<span style="color:red;font-size:25px;"><%=text_content %></span>   &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;以下是您的搜索结果：
                    </td>
                    <td width="12%">&nbsp;</td>
                    <td width="18%">&nbsp;</td>
                    <td width="25%">&nbsp;</td>
                  </tr>
           
           <%}
			 %>
	  </table>
	  
        <table class="tabStyle_1 percent98" border=1>
        	<tr align="center">
				<td class="tabStyle_1_title" >
					<input id="filename_input" name="checkbox" style="float:left;left:28px;top:47px;bottom:10px;margin-top:2px;position:absolute" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /> <span style="color:#000;">角色用户</span>
				</td>
				<td class="tabStyle_1_title">
					<span  style="color:#000">模板文件</span>
				</td>
			</tr>
        <%
			String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
			
			if (strcurpage.equals(""))
				strcurpage = "1";
			if (!StrUtil.isNumeric(strcurpage)) {
				out.print(StrUtil.makeErrMsg("标识非法！"));
				return;
			}
			
			RoleDb rd = new RoleDb();
			Vector roleVec = rd.getRolesOfUnit(privilege.getUserUnitCode(request));
			Iterator roleIr = roleVec.iterator();
			
			RoleTemplateMgr rtMgr = new RoleTemplateMgr();
			while(roleIr.hasNext()){
				RoleDb rdDb = (RoleDb)roleIr.next();
				String roleCode = rdDb.getCode();
				rtMgr.setRoleCode(roleCode);
				boolean isExists = false;
				// isExits = rtMgr.isExist();
			%>
				<tr>
					<td  align=middle  style="width:48%;postion:relative;color:#000">
					<input class="_d_checkbox" name="filename<%=roleCode %>" id="ids"  style="position:absolute;left:28px;" type="checkbox" value="<%=roleCode%>" />
						<%=rdDb.getDesc() %>
					</td>
					<td  align=middle style="width:50%;color:#000">
						<%
						if (isExists) {
							rtMgr.load();
							//Leaf leafTemp = new Leaf(rtMgr.getDirCode());
							String rtString = rtMgr.getVisualPath();
							out.print(rtString);
						} else {
						%>
						<%} %>
						
					</td>
				</tr>
			<%}
			%>
	</table>
	<div id="treeBackground" class="treeBackground"></div>
				<table id="mainTable" width="100%" border="0" cellpadding="0" cellspacing="0">
              </table>
			    <table width="100%" border="0" cellspacing="0" cellpadding="0" name="ctlTable" id="ctlTable" style="border-top:1px dashed #cccccc; font-size:15px;">
                  <tr>
                    <td><table  border="0" align="center" cellpadding="0" cellspacing="1" >
                      <tr>
                        <td>
                            <div style="width:400;margin-top:10px;border:0px solid #cccccc">
							<object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C" codebase="../activex/cloudym.CAB#version=1,2,0,1" width=400 style="height:0px" align="middle" id="webedit">
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
                              <param name="Server" value="<%=request.getServerName()%>">
                              <param name="Port" value="<%=request.getServerPort()%>">
                              <param name="VirtualPath" value="<%=Global.virtualPath%>">
                              <param name="PostScript" value="<%=Global.virtualPath%>/netdisk/dir_list_do.jsp">
                              <param name="PostScriptDdxc" value="<%=Global.virtualPath%>/netdisk/netdisk_ddxc.jsp">
                              <param name="SegmentLen" value="204800">
                              <param name="info" value="文件拖放区">
                              <%
							  License license = License.getInstance();	  
							  %>
                              <param name="Organization" value="<%=license.getCompany()%>">
                              <param name="Key" value="<%=license.getKey()%>">
                          </object></div>
						  <script>//initUpload()</script>
					    </td>
                      </tr>
                    </table></td>
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
   <div><input type="button" class="btn" value="上传文件夹" onclick="addform.webedit.OpenFolderDlg();if (addform.webedit.GetFiles()=='') return false; else SubmitWithFileThread()" style="margin-left:20px;"/></div>
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
<input name="root_code" type="hidden">
</form>
<form name="hidForm" action="" method="post">
<input name="op" type="hidden" />
<input name="page_num" value="1" type="hidden" />
<input name="ids" type="hidden" />
<input name="doc_id" value="<%=doc.getID()%>" type="hidden" />
<input name="dir_code" value="<%=dir_code%>" type="hidden" />
<input name="docId" type="hidden" value="<%=doc.getID()%>"  />
<input name="netdiskFiles" type="hidden" />
</form>
</td></tr>
</table>
<iframe id="hideframe" name="hideframe" src="" width=330 height=330 style="display:none"></iframe>
</form>
</div>
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
 <div id="ie5menu" class="skin0" onMouseover="highlightie5(event)" onMouseout="lowlightie5(event)"  onClick="jumptoie5(event)" style="display:block; z-index:1000 ">
						<div class="menuitems" url="javascript:changeName()">重命名</div>
						
						 <div class="menuitems" url="javascript:share()">共享</div> 
						 <div class="menuitems" url="javascript:download()">下载</div> 
						<hr>
						<div class="menuitems" url="javascript:delFolder()">删除</div>
						<hr>
						<!-- <div class="menuitems" url="javascript:create()">新建目录</div>
						<div class="menuitems" url="javascript:move('up')">上移</div>
						
						<div class="menuitems" url="javascript:move('down')">下移</div>-->
						<!--<div class="menuitems" url="javascript:search()">搜索</div>
						<div class="menuitems" url="javascript:props()">属性</div>
						<div class="menuitems" url="javascript:template()">上传模板</div>
						<hr/>
						<div class="menuitems" url="clouddisk_list.jsp?root_code=<%=StrUtil.UrlEncode(root_code)%>">刷新</div>-->
</div>

<script language="JavaScript1.2">

//set this variable to 1 if you wish the URLs of the highlighted menu to be displayed in the status bar
var display_url=0

var ie5=document.all&&document.getElementById
var ns6=document.getElementById&&!document.all
//if (ie5||ns6)
//var menuobj=document.getElementById("ie5menu"); 

function showmenuie5(e){
if (curDirCode=="")
	return;
//Find out how close the mouse is to the corner of the window
var rightedge=ie5? document.body.clientWidth-event.clientX : window.innerWidth-e.clientX
var bottomedge=ie5? document.body.clientHeight-event.clientY : window.innerHeight-e.clientY

//if the horizontal distance isn't enough to accomodate the width of the context menu
//if (rightedge<menuobj.offsetWidth)
//move the horizontal position of the menu to the left by it's width
////menuobj.style.left=ie5? document.body.scrollLeft+event.clientX-menuobj.offsetWidth : window.pageXOffset+e.clientX-menuobj.offsetWidth
//else
//position the horizontal position of the menu where the mouse was clicked
//menuobj.style.left=ie5? document.body.scrollLeft+event.clientX : window.pageXOffset+e.clientX

//same concept with the vertical position
//if (bottomedge<menuobj.offsetHeight)
//menuobj.style.top=ie5? document.body.scrollTop+event.clientY-menuobj.offsetHeight : window.pageYOffset+e.clientY-menuobj.offsetHeight
//else
////menuobj.style.top=ie5? document.body.scrollTop+event.clientY : window.pageYOffset+e.clientY

//menuobj.style.visibility="visible"
//return false
}

function hidemenuie5(e){
//menuobj.style.visibility="hidden"
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
//menuobj.style.display=''
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

var curDirCode = "";
var curDirName = "";

var curLeftClickDirCodeOld = "";
var curLeftClickDirCode = "";
//var oldsrc = "";
function onMouseUp(dirCode, dirName) {  
	curAttachId = dirCode;
	curAttachName = dirName;
	//if (event.button==1) {
	//	 点击左键时切换folder图片
	//	curLeftClickDirCode = dirCode;
	//	if (curLeftClickDirCodeOld!=curLeftClickDirCode) {
	//		var curImgObj = findObj("img" + curLeftClickDirCode);alert(curImgObj);
	//		var oldImgObj = findObj("img" + curLeftClickDirCodeOld);
	//		if (oldImgObj!=null) {
	//			oldImgObj.src = oldsrc; // "images/folder_01.gif";
	//		}
	//	oldsrc = curImgObj.src;

	//	if (curImgObj.src.indexOf("images/folder_share.gif")==-1)
	//			curImgObj.src = "images/folder_open.gif";
	//		else
	////			curImgObj.src = "images/folder_share_open.gif";
	//		
	//		curLeftClickDirCodeOld = curLeftClickDirCode;
	//	}
	//}
	if (event.button==2) { 
		curDirCode = dirCode;
		curDirName = dirName;
		id= dirCode;
 		$(".skin0").css({"visibility":"visible"});
 		var divx = window.event.clientX+"px";
 		var divy = window.event.clientY+"px";
 		var ie5menu = document.getElementById("ie5menu");
 		ie5menu.style.left = divx;
 		ie5menu.style.top = divy;	
	}
}

function removeMenu(){
	$(".skin0").css({"visibility":"hidden"});
}

function ShowChild(imgobj, name){
	var tableobj = findObj("childof"+name);
	if (tableobj==null){ 
		return;}
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

var spanInnerHTML = "";
var operateOuterHTML = "";
var checkboxOuterHTML = "";
var historyOuterHTML ="";
var cooperateOuterHTML = "";
var downloadOuterHTML = "";

function select_con(){
	var content = document.getElementById("select_content").value;
	window.location.href="?&select_sort=select_one&select_content="+content;
}

function select_file(which){
	window.location.href="?select_file=select_file&select_which="+which;
}

function changeFileName(item_id, item_docId){
	if(item_id!=""){ 
		spanObj = findObj("span" + item_id);
		spanInnerHTML = spanObj.innerHTML;
		//spanObj.innerHTML = "<input  name='newName' class='single_item'  size=22 value='"+spanInnerHTML+"'>";
		$(spanObj).html("<input id='"+ item_id +"' name='newName' class='single_item'  size=22 value='"+spanInnerHTML+"'><input id='hiddenText' value='"+ item_docId + "' style='display:none'>");
		curAttachName = spanInnerHTML;
		curAttachId = item_id;
		document.getElementsByName("newName")[0].focus();
		document.getElementsByName("newName")[0].select();
		//addform.newName.focus();
		//addform.newName.select();
	}
}

function changeFileNameAfter(item_id, item_oldId, newName, item_docId){
	if(item_id!=""){ 
		spanObj = findObj("span" + item_oldId);
		operateObj =findObj("operate" + item_oldId);
		historyObj = findObj("history" + item_oldId);
		cooperateObj = findObj("cooperate" + item_oldId);
		downloadObj = findObj("download" + item_oldId);
		document.getElementsByName("filename"+item_oldId)[0].value=item_id;
		spanInnerHTML = spanObj.innerHTML;
		operateOuterHTML = operateObj.outerHTML;
		
		//spanObj.innerHTML = newName;
		spanObj.outerHTML = "<span id='span"+item_id+"' name='span"+item_id+"' style='margin-left:60px;'>"+ newName + "</span>";
		var a= "<div id = 'operate"+item_id +"' class='operate'>";
		a+="   <a href=\"javascript:changeFileName('"+item_id+"','"+item_docId+"')\">重命名</a>";
		a+= "  <a onclick=\"javascript:showDialogTree('"+item_id+"','"+ item_oldId +"','"+item_docId+"')\" >移动</a>";
		a+="  <a href=\"javascript:delAttach('"+item_id+"', '"+item_docId+"')\">删除</a>";
		a+="   </div>";
		operateObj.outerHTML = a;
		historyObj.outerHTML = "<a id='history"+ item_id +"' name='history"+item_id+"' href='clouddisk_history_list.jsp?cur=current&attachName="+newName+"&attachId="+ item_id +"'><img src='images/clouddisk/look_1.gif' style='margin-right:10px;border:0px;' title='查看历史版本' /></a>";
		cooperateObj.outerHTML = "<a id='cooperate"+item_id+"' name='cooperate"+ item_id+"' href='netdisk_public_share.jsp?attachId="+item_id+"'><img src='images/clouddisk/share_1.gif' style='margin-right:10px;border:0px;' title='协作'/></a>";
		downloadObj.outerHTML ="<a id='download"+ item_id+"' name='download"+ item_id+"' target='_blank' href='netdisk_downloadfile.jsp?id="+item_docId+"&attachId="+item_id+"'><img src='images/clouddisk/download_1.gif' style='margin-right:10px;border:0px;' title='下载'/></a>";
		//alert(spanObj.outerHTML+"1");
		//operateObj.outerHTML = "<div id = 'operate"+ item_id + "' name='dff' class='operate'><a href=\"javascript:changeFileName('"+ item_id + "', '"+ item_docId +"')\">重命名</a><a onclick=\"showDialogTree('"+ item_id +"','"+ item_docId+ "')\" >移动</a><a href=\"javascript:delAttach('"+ item_id +"', '"+ item_docId + "')\">删除</a></div>	";
	}
}



function movefile(item_id){
	if(item_id != ""){
		window.open ('clouddisk_change.jsp?attachId='+item_id , '文件移动', 'height=100, width=400, top=0,left=0, toolbar=no, target=_blank, menubar=no, scrollbars=no, resizable=no,location=no, status=no');
	}
}

function changeName() { document.getElementById("ie5menu").style.visibility="hidden";
	if (curDirCode!="") { 
		spanObj = findObj("span" + curDirCode);
		//spanInnerHTML = spanObj.innerHTML; 
		//spanObj.innerHTML = "<input id='"+curDirCode+"' class='singleboarder' size=22 value='" + curDirName + "' onfocus='this.select()'>";
		$(spanObj).html("<input id='"+curDirCode+"' class='singleboarder' size=22 value='" + curDirName + "' onfocus='this.select()'>");	
		document.getElementById(curAttachId).focus();
		document.getElementById(curAttachId).select();
		//addform.newName.focus();
		//addform.newName.select();
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
		spanObj = findObj("span" +dirCode);
		spanInnerHTML = spanObj.innerHTML;  
		//$(spanObj).html("<a title='"+dirName+"' class=mainA  href='clouddisk_list.jsp?op=editarticle&dir_code="+dirCode+"' style='color:#888888' onmouseup=\"onMouseUp('"+dirCode+"', '"+dirName+"')\">"+dirName+"</a>");
		spanObj.innerHTML = "<a title='"+dirName+"' class=mainA  href='clouddisk_list.jsp?op=editarticle&dir_code="+dirCode+"' style='color:#888888' onmouseup=\"onMouseUp('"+dirCode+"', '"+dirName+"')\">"+dirName+"</a>";
		//var newName = curAttachId+"";
		//addform.newName.focus();
		//addform.newName.select() ;
	} 
}


function doChange(dirCode, newName, oldName, spanObj) {
	if (newName.value=="") {
		alert("目录名称不能为空！");
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
		alert("目录名称不能为空！");
		return;
	}
	if (newName.value!=oldName) {
		form10.op.value = "changeName";
		form10.dir_code.value = dirCode;
		//form10.newName.value = newName.value;
		form10.root_code.value = "<%=root_code%>";		
		//form10.submit();
		
		// 下句发过去会有中文问题
		// window.location.href="?op=changeName&dirCode=" + dirCode + "&newName=" + newName + "&root_code=<%=StrUtil.UrlEncode(root_code)%>";
		// alert(window.location.href);
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

function delFolder(){
	if (curDirCode!="") {
		if (!confirm("您确定要删除文件夹“" + curDirName + "”吗？")){
			removeMenu();
			return;
		}
		else{
			var docId = curDirCode;
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			$.ajax({
			 		type:"get",
			 		url:"clouddisk_list_do.jsp",
			 		data:{"op":"isExitFile","type":type, "dirCode":docId},
			 		success:function(data,status){
			 			data = $.parseJSON(data);
			 			if(data.ret == "1"){
			 				delFolderInfo(data.dirCode);
			 				alert(data.msg);
			 				//window.location.href="clouddisk_list.jsp"; 
			 			}else{
			 				if(!confirm(data.msg)){
			 					removeMenu();
				 				return;
			 				}else{
			 					confirmDelFolder();
			 					removeMenu();
			 				}
			 			}
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
			});
		//window.location.href="clouddisk_list_do.jsp?op=delFile&dirCode=" + curDirCode + "&root_code=<%=StrUtil.UrlEncode(root_code)%>&dir_code=<%=dir_code%>";
		//curDirCode = "";
		}
	}
}

function confirmDelFolder(){
	var docId = curDirCode;
	var type="<%=Leaf.TYPE_DOCUMENT%>";
	$.ajax({
	 		type:"get",
	 		url:"clouddisk_list_do.jsp",
	 		data:{"op":"delFile","type":type, "dirCode":docId},
	 		success:function(data,status){
	 			data = $.parseJSON(data);
	 			if(data.ret == "1"){
	 				delFolderInfo(data.dirCode);
	 				alert(data.msg);
	 				//window.location.href="clouddisk_list.jsp"; 
	 			}else{
	 				alert(data.msg);
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
		});
	//curDirCode = "";
}

function openFile() {
	window.open("netdisk_getfile.jsp?id=" + id + "&attachId=" + curAttachId);
}

function download() {
	window.open("clouddisk_downloaddir.jsp?code=" + id );
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

function delFolderInfo(doc_code){
	if(doc_code != ""){
		var fileObj = $("#size_folder"+doc_code);
		removeMenu();
		$(fileObj).remove();
	}
}

function delAttach(attach_id, doc_id) {
	if (!window.confirm("您确定要删除吗？")) {
		return;
	}else{
		var attId = attach_id;
		var docId = doc_id;
		var type="<%=Leaf.TYPE_DOCUMENT%>";
		$.ajax({
		 		type:"get",
		 		url:"clouddisk_list_do.jsp",
		 		data:{"op":"delAttach","type":type,"att_id":attId, "att_docId":docId},
		 		success:function(data,status){
		 			data = $.parseJSON(data);
		 			if(data.ret == "1"){
		 				delAttachInfo(data.att_id);
		 				alert(data.msg);
		 				//window.location.href="clouddisk_list.jsp"; 
		 			}else{
		 				alert(data.msg);
		 			}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
		});
	}
	//document.frames.hideframe.location.href = "clouddisk_list_do.jsp?op=delAttach&page_num=1&doc_id=" + doc_id + "&attach_id=" + attach_id
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

function delBatch() {
	var ids = getCheckboxValue("ids"); 
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	if (!confirm("您确定要删除么？"))
		return;
	// document.frames.hideframe.location.href = "dir_list_do.jsp?op=delAttachBatch&page_num=1&doc_id=<%=doc.getID()%>&ids=" + ids	
	var type="<%=Leaf.TYPE_DOCUMENT%>";
	$.ajax({
	 		type:"get",
	 		url:"clouddisk_list_do.jsp",
	 		data:{"op":"delBatch","type":type,"att_ids":ids},
	 		success:function(data,status){
	 			data = $.parseJSON(data);
	 			if(data.ret == "1"){
	 				var att_ids = data.att_ids;
	 				var attId = new Array();
	 				attId = att_ids.split(",");
	 				for(var i=0; i<attId.length; i++ ){
	 					delAttachInfo(attId[i]);
	 				}
	 				alert(data.msg);
	 			}
	 				//window.location.href="clouddisk_list.jsp"; 
	 			else{
	 				alert(data.msg);
	 			}
	 		},
	 		error:function(XMLHttpRequest, textStatus){
	 			alert(XMLHttpRequest.responseText);
	 		}
	});
}

function moveBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
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
		alert("请先选择文件！");
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
		alert("请先选择文件！");
		return;
	}
	window.top.opener.setNetdiskFiles(ids);
	window.top.close();
}
function send() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请先选择文件！");
		return;
	}
	// window.location.href = "../message_oa/message_ext/send.jsp?netdiskFiles=" + ids;
	hidForm.action = "../message_oa/message_ext/send.jsp";
	hidForm.netdiskFiles.value = ids;
	hidForm.submit();
}

function showDialogTree(attId,attOldId, docId) {
	document.getElementById("treeBackground").style.display='block';
	curAttachId = attId;
	attId_old = attOldId;
	$('#SD_window').css({"display":"block"});
	//$(body).css({"filter":"alpha(opacity = 70)", "background-color": "#000"}); 
	//var a = document.getElementById("SD_window"); alert(a.innerHTML);
	showDialogDirTree('window', 500); 
	shrink();
}

var templ = "<%=sbTemplate%>";
function template(){
	showDialogTemplate('info',templ,'角色模板', 350); 
}

function closeDialog(){
	$('#SD_window').hide();
	$('.treeBackground').hide();
}

function closeBackGround(){
	$('.treeBackground').hide();
}

function treeMove(){
	$('#SD_window').hide();
	var treeObj = findObj("tree"+attId_old);
	$(treeObj).html("");
}

function selectTree(dirCode, dirName){
	curDirCode = dirCode;
	curDirName = dirName;
	if (curDirCode!="") { 
		spanObj = findObj("spanTree" + curDirCode);
		spanInnerHTML = spanObj.innerHTML;  
		//spanObj.innerHTML = "<input id='"+curDirCode+"' class='singleboarder' size=22 value='" + curDirName + "' onfocus='this.select()'>";
		$('.selectTree').css({"background":"#FFF","color":"#000"});	
		//$(spanObj).html("<span id='spanTree"+curDirCode+"' name='spanTree"+curDirCode+"' class='selectTree' style='background:#39F; color:#FFF'><a onclick='selectTree(\"" + curDirCode + "\",\"" + curDirName +"\")'>"+ curDirName+"</a></span>");
		spanObj.innerHTML = "<span class='selectTree' style='background:#39F; color:#FFF'><a onclick='selectTree(\"" + curDirCode + "\",\"" + curDirName +"\")'> " + curDirName+ "</a></span>";
	}
}

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>
