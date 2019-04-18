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
<link href="../js/contextMenu/css/ContextMenu.css" rel="stylesheet" type="text/css" />
<script src="../js/contextMenu/jquery.contextMenu.js" type="text/javascript"></script>
<script src="js/role_template.js" type="text/javascript"></script>
<script src="js/clouddisk.js" type="text/javascript"></script>
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
	String userName = ParamUtil.get(request,"userName");
	if("".equals(userName)){
		userName = privilege.getUser(request);
	}else{
		Leaf leaf = new Leaf(userName); 
		if (leaf==null || !leaf.isLoaded()) {
			leaf.AddUser(userName); //初始化虚拟用户
		}
		
	}
	String root_code = ParamUtil.get(request, "root_code");
	if (root_code.equals("")) {
		 root_code = userName;
	}
	String dir_code = ParamUtil.get(request, "dir_code");
	if("".equals(dir_code)){ 
		dir_code = root_code;
	}
 	String sort0 = ParamUtil.get(request, "sort0");
	if("".equals(sort0)){
		sort0 = "时间";
	}else if(sort0.equals("file_size")){
		sort0 = "大小";
	}else if(sort0.equals("name")){
		sort0 = "名称";
	}else if(sort0.equals("uploadDate")){
		sort0 = "时间";
	}
	String dir_name = "";
	int id = 0;
	String correct_result = "操作成功！";
	Document doc = new Document();
	doc = docmanager.getDocumentByCode(request, dir_code, privilege);
	Document template = null;
	Leaf leaf = dir.getLeaf(dir_code);
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
	String text_content = ParamUtil.get(request,"select_content");

	if (op.equals("editarticle")) {
		op = "edit";
		try {
			doc = docmanager.getDocumentByCode(request, dir_code, privilege);
			dir_code = doc.getDirCode();
		} catch (ErrMsgException e) {
			out.print(strutil.makeErrMsg(e.getMessage(), "red", "green"));
			return;
		}
	}else if (op.equals("myclouddisk")) {
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
			out.print(strutil.makeErrMsg(e.getMessage(), "red", "green"));
			return;
		}
	}
	String action = ParamUtil.get(request, "action");
	if (doc!=null) {
		id = doc.getID();
		Leaf lfn = new Leaf();
		lfn = lfn.getLeaf(doc.getDirCode());
		dir_name = lfn.getName();
	}
	// 防XSS
		try {
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "work", work, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mode", mode, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "sort0", sort0, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
			//com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "name", fileName, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
		}
		catch (ErrMsgException e) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
			return;
		}
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	String file_netdisk = cfg.get("file_netdisk");
	%>
	<script>
		$(function(){
			//文件夹单击右键 出现功能菜单 json数据
			var contextMenuInfo = {
			 		menuId: 'contextMenu',
		 		onContextMenuItemSelected:function(menuItemId, $triggerElement){
	 				curDirCode = $triggerElement.attr('dirCode');
	 				var dirCode = curDirCode;
	 				var floderName = $triggerElement.find("a").text();//获得文件夹名
		 			if( menuItemId == 'reName' ){
			 			var fileInput = "<input id='"+dirCode+"' class='singleboarder' oldName='"+floderName+"' size=22 value='" + floderName + "' />";
						$triggerElement.html(fileInput);
			 			$(".singleboarder").get(0).select();
					}else if(menuItemId == 'download'){
						window.open("clouddisk_downloaddir.jsp?code=" + dirCode );
					}else if(menuItemId == 'delete'){
						delFolder(floderName,dirCode);//删除文件夹

					}else if(menuItemId == 'refresh'){
						window.location.href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&root_code="+'<%=StrUtil.UrlEncode(root_code)%>';

					}else if(menuItemId == 'template'){
						template();
					}
			 
				},
				onContextMenuShow:function($triggerElement){
				
				},
				showShadow:false
			    }
		    //所有文件夹绑定右键操作
			$(".floderNameInfo").contextMenu(contextMenuInfo);
			
			
			 
			//文件单击重命名 出现input框
		  $(".fileReName").click(function(){
			  var oldAmId = $(this).attr("oldAmId");//文件重命名后重命名
			  var item_id = $(this).attr("amId");
			  var docId = $(this).attr("docId");
			  var $root = $(this).parents("dd");
			  var $fileName =  $root.find(".fileNameDetail").find(".fname")//获得文件
			  var fileNameInfo =$fileName.find("a").text();//获得默认文件名
			  $fileName.html("<input id='"+ item_id +"' oldAmId='"+ oldAmId +"' docId='"+docId+"' name='newName' class='single_item'   size=22 value='"+fileNameInfo+"'>");
			  curAttachName = fileNameInfo;
		 	  curAttachId = item_id;
			  $(".single_item").get(0).select();
		   });
		  //文件重命名  文本框移除事件
			 $(".single_item").live("blur",function(){
			 	attName = $(this).val();
			 	attId = $(this).attr("id");//获得id
			 	attDocId = $(this).attr("docId");//获得Docid
			 	var oldAmId = $(this).attr("oldAmId");//文件重命名后重命名
			 	attId_old = attId;
			 	var type="<%=Leaf.TYPE_DOCUMENT%>";
			 	if(curAttachName == attName){
				 	//如果文件名称未变
			 		changeFileNameAfter(curAttachId,curAttachId,curAttachName,attDocId,oldAmId); 
			 		return;
			 	}
			 	$.ajax({
			 		type:"get",
			 		url:"clouddisk_list_do.jsp",
			 		data:{"op":"changeFileName","type":type,"att_id":attId,"att_name":attName,"att_oldId":attId, "att_oldId":attId_old, "att_docId":attDocId},
			 		success:function(data,status){
			 			data = $.parseJSON(data);
			 			if(data.ret == "1"){
			 				changeFileNameAfter(data.attId,data.attOldId,data.name,data.attDocId,oldAmId,data.ext,data.url);
			 				
			 			}else{
			 				jAlert(data.msg,"提示");
			 			}
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
			 	});
			 });
			//删除单个文件
			$(".deleteFile").click(function(){
				var $restore = $(this);
				var amId = $restore.attr("amId");
				var docId = $restore.attr("docId");
				var type="<%=Leaf.TYPE_DOCUMENT%>";
				jConfirm("您确定要彻底删除吗？","提示",function(r){
					if(!r){
						return;
					}else{
						$.ajax({
					 		type:"get",
					 		url:"clouddisk_list_do.jsp",
					 		data:{"op":"removeAttach","type":type,"att_id":amId, "att_docId":docId},
					 		success:function(data,status){
					 			data = $.parseJSON(data);
					 			if(data.ret == "1"){
					 				$("#tree"+data.att_id).remove(); //删除该行
					 				jAlert("彻底删除成功!","提示");
					 			}else{
					 				jAlert(data.msg,"提示");
					 			}
					 		},
					 		error:function(XMLHttpRequest, textStatus){
					 			alert(XMLHttpRequest.responseText);
					 		}
					});
					}
				});
			});

		});
		
		function showDialogTree(attId,attOldId, docId) {
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			var user_name = "<%=userName%>";
			var mode = "<%=mode%>";
			$.ajax({
			 		type:"get",
			 		url:"clouddisk_list_do.jsp",
			 		data:{"op":"showTree","type":type,"user_name":user_name,"mode":mode},
			 		success:function(data,status){
			 			data = $.parseJSON(data);
			 			if(data.ret == "1"){
			 				showTreeDialog("confirm",data.msg,"提示",500);
			 				//window.location.href="clouddisk_list.jsp"; 
			 			}else{
			 				jAlert(data.msg,"提示");
			 			}
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
			});
			document.getElementById("treeBackground").style.display='block';
			curAttachId = attId;
			attId_old = attOldId;
			//$('#SD_window').css({"display":"block"});
			//$(body).css({"filter":"alpha(opacity = 70)", "background-color": "#000"}); 
			//var a = document.getElementById("SD_window"); alert(a.innerHTML);
			//showDialogDirTree('window', 500); 
			shrink();
		}
		
		//findObj找元素方法 
		function findObj(theObj, theDoc){
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
			
		//单个逻辑删除
			function delAttach(attach_id, doc_id,oldAmId) {
				jConfirm("您确定要删除吗？","提示",function(r){
					if(!r){
						return;
					}
					else{
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
						 				delAttachInfo(oldAmId);
						 				jAlert(data.msg,"提示");
						 				//window.location.href="clouddisk_list.jsp"; 
						 			}else{
						 				jAlert(data.msg,"提示");
						 			}
						 		},
						 		error:function(XMLHttpRequest, textStatus){
						 			alert(XMLHttpRequest.responseText);
						 		}
						});
					}
				})
			}
			
			//删除文件夹
			function delFolder(curDirName,dirCode){
				var type="<%=Leaf.TYPE_DOCUMENT%>";
				$.ajax({
				 		type:"get",
				 		url:"clouddisk_list_do.jsp",
				 		data:{"op":"isExitFile","type":type, "dirCode":dirCode},
				 		success:function(data,status){
				 			data = $.parseJSON(data);
				 			if(data.ret == "1"){
					 			//页面上删除文件夹
					 			var $dd = $("#folder"+data.dirCode);
					 			var $dl = $dd.parent();
				 				$dl.remove();
				 				jAlert(data.msg,"提示");
				 			} else {
				 				jConfirm("文件夹内有文件，您确定要删除吗？","提示",function(r){
				 					if(!r){
				 						return;
				 					}
				 					else{
				 						confirmDelFolder(dirCode);
				 					}
				 				});
				 			}
				 		},
				 		error:function(XMLHttpRequest, textStatus){
				 			alert(XMLHttpRequest.responseText);
				 		}
				});
			}
		/**
		 * 文件名重命名
		 */
		function changeFileNameAfter(item_id, item_oldId, newName, item_docId,oldAmId,ext,url){
		if(item_id != "" && item_oldId!=""){//如果当前id与最新id都不为空
			var $dl = $("#tree"+oldAmId);
			var $fname = $dl.find("dd").find(".fileNameDetail").find(".fname");
			var name ;
			if(item_id == item_oldId){//如果当前id与最新id一致说明 文件名相同
				name = "<a href='javascript:editdoc("+item_docId+", "+item_id+", \""+ext+"\")'>"+newName+"</a>";
			}else{
				name = "<a href='javascript:editdoc("+item_docId+", "+item_id+", \""+ext+"\")'>"+newName+"</a>";
				var $cbox = $dl.find("dd").find(".fileNameDetail").find(".cbox");//更新checkbox属性
				$cbox.attr("name","filename"+item_id);
				$cbox.val(item_id);
				//更新extImg图标
				var $extImg = $dl.find("dd").find(".fileNameDetail").find(".extImg");
				$extImg.attr("src","images/sort/"+url);
				$fname.attr({"id":"span"+item_id,"name":"span"+item_id});
				var $file_action = $dl.find("dd").find(".fileNameDetail").find(".file_action").find("ul");
				$file_action.find("li:eq(0)").find("a").attr({"href":"javascript:editdoc("+item_docId+", "+item_id+", \""+ext+"\")"});
				$file_action.find("li:eq(1)").find("a").attr({"id":"history"+item_id,"name":"history"+item_id,"href":"clouddisk_history_list.jsp?cur=current&attachName="+newName+"&attachId="+item_id});
				//$file_action.find("li:eq(1)").find("a").attr({"id":"cooperate"+item_id,"name":"cooperate"+item_id,"href":"netdisk_public_share.jsp?attachId"+item_id});
				$file_action.find("li:eq(2)").find("a").attr({"id":"download"+item_id,"name":"download"+item_id,"href":"clouddisk_downloadfile.jsp?attachId="+item_id});
				var $op = $dl.find("dd").find(".fileNameDetail").find(".op");
				$op.attr("id","operate"+item_id);
				$op.find("li:eq(0)").find("a").attr({"amOldId":item_oldId,"amId":item_id,"docId":item_docId});
				$op.find("li:eq(1)").find("a").attr({"href":"javascript:showDialogTree('"+item_id+"','"+oldAmId+"',"+item_docId+")"});
				$op.find("li:eq(2)").find("a").attr({"href":"javascript:delAttach('"+item_id+"','"+item_docId+"','"+oldAmId+"')"});
				
			}
			$fname.html(name);
		}
	}
		//文件夹重命名
			function changeNameInfo(oldCode,dirName) { 
				if(oldCode!=""){
					var $dd = $("#folder"+oldCode);
					var $fname = $dd.find(".fileNameDetail").find(".fname");
					var name = "<a title='"+dirName+"'  href='clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code="+oldCode+"' >"+dirName+"</a>";
					$fname.html(name);
					
				}
			}
		//确认移动树文件
		 $('#SD_confirm').live("click",function(){
			attId = curAttachId;
			dirCode = curDirCode;
			dirName = curDirName;
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			if(dirCode == ""){
				closeDialog();
				return;
			}
			$.ajax({
				type:"post",
				url:"clouddisk_list_do.jsp",
				data:{"op":"moveFile", "type":type, "att_id":attId, "dirCode":dirCode, "dirName":dirName},
				success:function(data,status){
					data = $.parseJSON(data);
					if(data.ret == "1"){ 
						if(data.doc_id == data.doc_oldId){
							closeDialog();
							return;
						}
						treeMove();
						jAlert(data.msg,"提示");
					}else if(data.ret == "2"){
						closeDialog();
						jAlert(data.msg,"提示");
					}
					else{
						treeMove();
						jAlert(data.msg,"提示");
					}
				},
				error:function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});
		 });
		 
		//批量逻辑删除
			function delBatch() {
				//var ids = getCheckboxValue("ids"); 
				var ids = getAllCheckedValue("input[fileType='files']");
				if (ids=="") {
					jAlert("请先选择文件！","提示");
					return;
				}
				jConfirm("您确定要删除么？","提示",function(r){
					if(!r){return;}
					else{
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
						 				jAlert(data.msg,"提示");
						 			}
						 				//window.location.href="clouddisk_list.jsp"; 
						 			else{
						 				jAlert(data.msg,"提示");
						 			}
						 		},
						 		error:function(XMLHttpRequest, textStatus){
						 			alert(XMLHttpRequest.responseText);
						 	
						 		}
						});
					}
				})
			}
			
		function delAttachInfo(attId){
		if (attId!="") {
			$("#tree" +attId).remove();
			//spanInnerHTML = spanObj.innerHTML;  
			//$(spanObj).html("<a title='"+dirName+"' class=mainA  href='clouddisk_list.jsp?op=editarticle&dir_code="+dirCode+"' style='color:#888888' onmouseup=\"onMouseUp('"+dirCode+"', '"+dirName+"')\">"+dirName+"</a>");
			//spanObj.outerHTML = "";
			//var newName = curAttachId+"";
			//addform.newName.focus();
			//addform.newName.select() ;
		} 
	}
		//文件夹中包含子文件夹  确认是否删除 
		function confirmDelFolder(dirCode){
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			$.ajax({
			 		type:"get",
			 		url:"clouddisk_list_do.jsp",
			 		data:{"op":"delFile","type":type, "dirCode":dirCode},
			 		success:function(data,status){
			 			data = $.parseJSON(data);
			 			if(data.ret == "1"){
			 				//页面上删除文件夹
				 			var $dd = $("#folder"+data.dirCode);
				 			var $dl = $dd.parent();
			 				$dl.remove();
			 				jAlert(data.msg,"提示");
			 			
			 				
			 			}else{
			 				jAlert(data.msg,"提示");
			 			}
			 		},
			 		error:function(XMLHttpRequest, textStatus){
			 			alert(XMLHttpRequest.responseText);
			 		}
				});
		}
		//文件夹恢复
		function restoreFolder(dirCode,dirName){
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			jConfirm("您确定要恢复文件夹"+dirName+"吗？","提示",function(r){
				if(!r){
					return;
				}else{
					$.ajax({
						type:"post",
						url:"clouddisk_list_do.jsp",
						data :{"op":"restoreFolder","type":type,"dir_code":dirCode},
						success: function(data, status){
							data = $.parseJSON(data);
							if (data.ret=="1") {
								$("#floder"+dirCode).remove();
								jAlert("恢复文件夹成功!","提示");
							}
							else{
								jAlert(data.msg,"提示");
							}
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});
				}
			});
      }
	  //删除文件夹Floder
		function delFloder(dirCode,dirName){
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			jConfirm("您确定要彻底删除文件夹"+dirName+"吗？","提示",function(r){
				if(!r){
					return;
				}else{
					$.ajax({
						type:"post",
						url:"clouddisk_list_do.jsp",
						data :{"op":"removeFolder","type":type,"dir_code":dirCode},
						success: function(data, status){
							data = $.parseJSON(data);
							if (data.ret=="1") {
								$("#floder"+dirCode).remove();
								jAlert("删除文件夹成功!","提示");
							}
							else{
								jAlert(data.msg,"提示");
							}
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});
				}
			});
		}
		//批量恢复文件夹
		function restoreBatch() {
			var ids = getCheckboxValue("ids"); 
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			if (ids=="") {
				jAlert("请先选择需要恢复的文件!","提示");
				return;
			}
			jConfirm("您确定要恢复文件吗?","提示",function(r){
				if(!r){
					return;
				}else{
					$.ajax({
				 		type:"get",
				 		url:"clouddisk_list_do.jsp",
				 		data:{"op":"restoreBatch","type":type,"att_ids":ids},
				 		success:function(data,status){
				 			data = $.parseJSON(data);
				 			if(data.ret == "1"){
				 				var att_ids = data.att_ids;
				 				var attId = new Array();
				 				attId = att_ids.split(",");
				 				for(var i=0; i<attId.length; i++ ){
				 					$("#tree"+attId[i]).remove(); //删除该行
				 				}
				 				jAlert("恢复成功","提示");
				 			}
				 			else{
				 				jAlert(data.msg,"提示");
				 			}
				 		},
				 		error:function(XMLHttpRequest, textStatus){
				 			alert(XMLHttpRequest.responseText);
				 		}
				});
				}
			});
		}
		//批量删除
		function removeBatch() {
			var ids = getCheckboxValue("ids"); 

			
			var type="<%=Leaf.TYPE_DOCUMENT%>";
			if (ids=="") {
				jAlert("请先选择需要删除的文件!","提示");
				return;
			}
			jConfirm("您确定要删除文件吗?","提示",function(r){
				if(!r){
					return;
				}else{
					$.ajax({
				 		type:"get",
				 		url:"clouddisk_list_do.jsp",
				 		data:{"op":"removeBatch","type":type,"att_ids":ids},
				 		success:function(data,status){
				 			data = $.parseJSON(data);
				 			if(data.ret == "1"){
				 				var att_ids = data.att_ids;
				 				var attId = new Array();
				 				attId = att_ids.split(",");
				 				for(var i=0; i<attId.length; i++ ){
				 					$("#tree"+attId[i]).remove(); //删除该行
				 				}
				 				jAlert("删除成功","提示");
				 			}
				 			else{
				 				jAlert(data.msg,"提示");
				 			}
				 		},
				 		error:function(XMLHttpRequest, textStatus){
				 			alert(XMLHttpRequest.responseText);
				 		}
				});
				}
			});
		}
	
	//文件夹单击右键 出现功能菜单 json数据
		var contextMenuInfo = {
		 		menuId: 'contextMenu',
		 		onContextMenuItemSelected:function(menuItemId, $triggerElement){
	 				curDirCode = $triggerElement.attr('dirCode');
	 				var dirCode = curDirCode;
	 				var floderName = $triggerElement.find("a").text();//获得文件夹名
		 			if( menuItemId == 'reName' ){
			 			var fileInput = "<input id='"+dirCode+"' class='singleboarder' oldName='"+floderName+"' size=22 value='" + floderName + "' />";
						$triggerElement.html(fileInput);
			 			$(".singleboarder").get(0).select();
					}else if(menuItemId == 'download'){
						window.open("clouddisk_downloaddir.jsp?code=" + dirCode );
					}else if(menuItemId == 'delete'){
						delFolder(floderName,dirCode);//删除文件夹

					}else if(menuItemId == 'refresh'){
						window.location.href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&root_code="+'<%=StrUtil.UrlEncode(root_code)%>';

					}else if(menuItemId == 'template'){
						template();
					}
			 
				},
				onContextMenuShow:function($triggerElement){
				
				},
				showShadow:false
		    }
	    //所有文件夹绑定右键操作
		$(".floderNameInfo").contextMenu(contextMenuInfo);
		//文件夹中文本输入框移除事件
		 $(".singleboarder").live("blur",function(){
			    var $floder = $(this);
			 	curDirCode = '<%=dir_code%>';
				type='<%=Leaf.TYPE_DOCUMENT%>';
				root_code = '<%=root_code%>';
				var code = $(this).attr("id"); //获得id
				var oldName = $(this).attr("oldName");//获得原来名称
			 	var name = $(this).val();  
			 	// 判断是否修改
			 	if (oldName == name) {
			 		changeNameInfo(code, oldName);
			 		return;		 	
			 	}else{
			 		$.ajax({
				 		type:"post",
				 		url:"clouddisk_list_do.jsp",
				 		data :{"op":"changeName","type":type,"parent_code":curDirCode,"root_code":root_code,"code":code,"name":name},
				 		success:function(data, status){
				 			data = $.parseJSON(data);
				 			if(data.ret == "1"){
				 				changeNameInfo(data.code,data.name);
				 				return;
				 			} else {
					 			$floder.val(oldName);
					 			jAlert("与现有文件名重复","提示");
					 			return;
							}
				 		},
				 		error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
				 	});
				}
			 });
		//分类搜索
		function select_file(which){
			window.location.href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&select_file=select_file&select_file_content="+select_file_content+"&select_which="+which;
		}
		//分类搜索
		function select_con(){
			var content = document.getElementById("select_content").value;
			window.location.href="?userName=<%=StrUtil.UrlEncode(userName)%>&select_sort=select_one&select_content="+content;
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
	
	
	</script>
</head>

<body oncontextmenu="return false"> &nbsp;	
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
	String select_file_content = ParamUtil.get(request,"select_file_content");
	String all_file = "clouddisk_search.jsp?userName="+StrUtil.UrlEncode(userName)+"&select_sort=select_one&select_content="+select_file_content;
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_file_content", select_file_content, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	if(!select_file_content.equals("")){
		all_file = "clouddisk_search.jsp?userName="+StrUtil.UrlEncode(userName)+"&select_sort=select_one&select_content="+select_file_content;
	}else{
		all_file = "javascript:void(0)";
	}
%>
    
 
 <div id="Right" class="Right">
      <div class="rHead">
	    <div class="rHead1" style="position:relative">
		   <a onclick="delBatch()"><div class="deleteFile_c"></div></a> 
		  <div class="view">
			<div id="view_1"
				style="cursor: pointer; background: url(images/clouddisk/view_list_1.gif); width: 30px; height: 24px; border-right: 1px solid #cacaca; float: left"
				onclick="cloud_list()"></div>
			<div id="view_2"
				style="cursor: pointer; background: url(images/clouddisk/view_thumbnail_2.gif); width: 29px; height: 24px; float: left"
				onclick="tiled_list()"></div>
			</div>
			<script>
	        	var expdate = new Date();
				var expday = 60;
				expdate.setTime(expdate.getTime() +  (24 * 60 * 60 * 1000 * expday));
            	function tiled_list(){ 
            		document.cookie="netdiskDefaultStatus"+"="+escape(1)+";expires="+expdate.toGMTString();//1代表平铺界面
            		window.location.href="clouddisk_tiled.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&select_sort=select_one&select_content=<%=StrUtil.UrlEncode(text_content)%>";
            	}
            	
            	function cloud_list(){

            		window.location.href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&select_sort=select_one&select_content=<%=StrUtil.UrlEncode(text_content)%>";
            	}
            </script>
		</div>
		<form name="addform" action="fwebedit_do.jsp" method="post" style="padding:0px; margin:0px">
		<div class="rHead2" style=" background:#f7f7f7;  z-index:20" >
		 <%
			String showPath = "";
			boolean backFlag = true;

			showPath = "<a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >    <a href='clouddisk_search.jsp?userName="+StrUtil.UrlEncode(userName)+"&select_sort=select_one&select_content="+StrUtil.UrlEncode(text_content)+"'>“" + text_content + "”的搜索结果</a>";
			 %>
			<%if(backFlag) {%>
			<div style="float: left; margin-left: 15px; margin-top: 4px;">
				<a
					href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>"><img
						src="images/clouddisk/back.png" />
				</a>
			</div>
			<%} %>
			<div class='all_file'><%=showPath %></div>

		  
		  
		  
          <% 
          	
          	if("".equals(text_content)){ text_content = "请输入文件名搜索..."; }
			try {
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "text_content", text_content, getClass().getName());
			}
			catch (ErrMsgException e) {
				out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
				return;
			}	
			if(!select_file_content.equals("")&&!select_file_content.equals("请输入文件名搜索...")){
				text_content = select_file_content;
			}
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
			   try {
					com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_file", select_file, getClass().getName());
					com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "which", which, getClass().getName());
				}
				catch (ErrMsgException e) {
					out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
					return;
				}
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
	  <%
	  	String select_sort = ParamUtil.get(request,"select_sort"); 
		String fileCurrent = ParamUtil.get(request,"cur");
		try {
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_sort", select_sort, getClass().getName());
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "fileCurrent", fileCurrent, getClass().getName());
		}
		catch (ErrMsgException e) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
			return;
		}
	   %>
      <div class="containtCenter">
        <dl class="fileTitleDl">
          <dd class="fileTitle">
            <div class="fileNameDetailTitle" >
            	<span class="cbox_all"></span>
				<input id="filename_input" name="checkbox" class="title_cbox"  type="checkbox" />
               <span class="fnameTitle">文件名</span>
            </div>
            <div class="colTitle">
              <span onClick="doSort('file_size')">大小</span>
              <%if (orderBy.equals("file_size")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' onClick=doSort('file_size') width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' onClick=doSort('file_size') width=8px height=7px>");
				}%>
            </div>
            <div class="colTitle">
              <span onClick="doSort('ext')">类型</span>
              <%if (orderBy.equals("ext")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' onClick=doSort('ext') width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' onClick=doSort('ext') width=8px height=7px>");
				}%>
             
            </div>
     		<div class="colTitle">
              <span>所在目录</span>
            </div>
          </dd>
        </dl>       
		 <%
			if(fileCurrent.equals("current")){
				String currentName =  ParamUtil.get(request,"attachName");
				try {
					com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "attachName", currentName, getClass().getName());
				}
				catch (ErrMsgException e) {
					out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
					return;
				}
				text_content = "";
			%>
			 <dl>
			  <dd class="searchFileTitle" >	
			  	<div class="searchDetail">
            	 	<span class="searchInfo">
            	 	您正在搜索<span class="searchTitle"><%=currentName %></span> 的历史版本：以下是您的搜索结果：
            	 	</span>		
				</div>
			    </dd>
			  </dl>	
			<%}
			if(select_sort.equals("select_one")){
				if("请输入文件名搜索...".equals(text_content)){
				text_content="";}
			%>
           <!--<dl>
			  <dd class="searchFileTitle" >	
			  	<div class="searchDetail">
            	 	<span class="searchInfo">
            	 	您搜索的内容是：<span  class="searchTitle"><%=text_content %></span>以下是您的搜索结果：
            	 	</span>		
				</div>
			    </dd>
			  </dl>	
           -->
           <%}else  if("请输入文件名搜索...".equals(text_content)&&!("select_file".equals(select_file))){
				Iterator irch = leaf.getRecyclerChildren().iterator();
				while (irch.hasNext()) {
					Leaf clf = (Leaf)irch.next();
					Leaf parentLeaf = new Leaf(clf.getParentCode());
				%>
                 <dl id="floder<%=clf.getCode()%>">
					  <dd class="fileGroup" id="folder<%=clf.getCode()%>"  type='file'>	
					  	<div class="fileNameDetail">
							 <img  src="images/sort/folder.png" class="extImg"/>
		            	 	<span class="fname floderNameInfo" id="span<%=clf.getCode()%>" dirCode="<%=clf.getCode()%>" name="span<%=clf.getCode()%>">
		            	 	<a  title="<%=clf.getName()%>" href="javascript:void(0);" ><%=clf.getName()%></a></span>		
						</div>
					      <div class="col" >
					         <span><%=parentLeaf.getName() %></span>
					       </div>
					       <div class="col">
					         <span><%=clf.getDeletedDate()%></span>
					       </div>
					    </dd>
				  </dl>	
                <%
                }
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
			long fileLength = -1;
			int pagesize = 50;
			String sql = "SELECT id FROM netdisk_document_attach WHERE page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=0 and is_deleted=1 order by ";
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
				sql = "SELECT id FROM netdisk_document_attach WHERE name = " + StrUtil.sqlstr(currentName)+ " and page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=0 and is_deleted=0 order by ";
				sql += orderBy + " " + sort;
			}
			if (select_sort.equals("select_one")) {
				if ((text_content.trim()).equals("请输入文件名搜索...")) {
					text_content = "";
				}
				sql = "SELECT code FROM netdisk_directory WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and root_code = " + StrUtil.sqlstr(root_code)+ " and isDeleted=0 order by add_date desc";
				
				//sql = "SELECT id FROM netdisk_document_attach WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=1 and is_deleted=0 order by ";
				//sql += orderBy + " " + sort;
			}
			if (select_file.equals("select_file")){
				com.redmoon.clouddisk.Config rcfg = com.redmoon.clouddisk.Config.getInstance();
				String extType = rcfg.getProperty("exttype_" + which);
				String[] exts = extType.split(",");
				StringBuilder sb = new StringBuilder();
				for (String ext : exts) {
					sb.append(StrUtil.sqlstr(ext)).append(",");
				}
				if (sb.toString().endsWith(",")) {
					sb.deleteCharAt(sb.toString().length() - 1);
				}
				sql = "SELECT id FROM netdisk_document_attach WHERE page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and name like "+ StrUtil.sqlstr("%"+select_file_content+"%") +" and is_current=1 and is_deleted=0 and ext in" 
					+ (sb.toString().equals("") ? "" : "(" + sb.toString() + ")")
					+ " order by " + orderBy + " " + sort;
			}
			
			
			//ListResult lr = clf.listResult(sql, curpage, pagesize);
			//int total = lr.getTotal();
			//Paginator paginator = new Paginator(request, total, pagesize);
			// 设置当前页数和总页数
			//int totalpages = paginator.getTotalPages();
			//if (totalpages==0)
			//{
			//	curpage = 1;
			//	totalpages = 1;
			//}
		  if (doc!=null && !select_file.equals("select_file")) {
			  // Vector attachments = doc.getAttachments(1);
			  
			  Iterator irch = leaf.getSearchChildren(sql).iterator();
				while (irch.hasNext()) {
				Leaf clf = (Leaf)irch.next();
				Leaf parentLeaf = new Leaf(clf.getParentCode());
				//String theLength = UtilTools.getFileSize(am.getSize());
				%>
			<dl id="tree<%=clf.getCode()%>" class="fileDl"> 
			<dd class="fileGroup" id="folder<%=clf.getCode()%>"  type='file' >	
				<div class="fileNameDetail">
				   	<span class="cbox_icon"></span>
				 	<input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" value="<%=clf.getCode()%>" />
					<img  src="images/sort/folder.png" class="extImg"/>
					 <!-- <span class="fname" id="span<%=clf.getCode() %>" name="span<%=clf.getCode() %>" > -->
					 <span class="fname floderNameInfo" id="span<%=clf.getCode()%>" dirCode="<%=clf.getCode()%>" name="span<%=clf.getCode()%>">
					 	<a href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&op=editarticle&dir_code=<%=clf.getCode() %>"><%=clf.getName()%></a>
					 </span>
				</div>
				<div class="col">
	              <span></span>
	            </div>
	            <div class="col">
	              <span>文件夹</span>
	              <!-- <%=DateUtil.format(am.getDeleteDate(),"yyyy-MM-dd HH:mm")%> -->
	            </div>
	            <div class="col">
	            	<span><%=parentLeaf.getName() %></span>
	            </div>
	          </dd>
	        </dl>
        <%}
         }
        	if (select_sort.equals("select_one")) {
				if ((text_content.trim()).equals("请输入文件名搜索...")) {
					text_content = "";
				}
				sql = "SELECT id FROM netdisk_document_attach WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=1 and is_deleted=0 order by ";
				sql += orderBy + " " + sort;
			}
			
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
				String theLength = UtilTools.getFileSize(am.getSize());
				%>
			<dl id="tree<%=am.getId()%>" class="fileDl"> 
			<dd class="fileGroup" >	
				<div class="fileNameDetail">
				   <span class="cbox_icon"></span>
					<input class="cbox" name="att_ids" 
								type="checkbox" value="<%=am.getId()%>" />
					<img  src="images/sort/<%=Attachment.getIcon(am.getExt())%>" class="extImg"/>
					 <span class="fname" id="span<%=am.getId() %>" name="span<%=am.getId() %>" >
					 	<a href="javascript:editdoc('<%=id%>', '<%=am.getId()%>', '<%=am.getExt() %>')"><%=am.getName()%></a>
					 </span>
					  <div class="file_action">
		                <ul>
		                <li><a href="javascript:editdoc('<%=id%>', '<%=am.getId()%>', '<%=am.getExt() %>')"><img src="images/clouddisk/edit.gif"  title="打开文本" /></a></li>
		                <li><a id="history<%=am.getId() %>" name="history<%=am.getId() %>" href="clouddisk_history_list.jsp?cur=current&attachName=<%=am.getName() %>&attachId=<%=am.getId() %>" ><img src="images/clouddisk/look_1.gif"  title="查看历史版本" /></a></li>
		                 <!--  <li><a id="cooperate<%=am.getId() %>" name="cooperate<%=am.getId() %>" href="netdisk_public_share.jsp?attachId=<%=am.getId()%>"><img src="images/clouddisk/share_1.gif" title="协作"/></a></li>-->
						 <li><a id="download<%=am.getId() %> " name="download<%=am.getId()%>" target="_blank" href="clouddisk_downloadfile.jsp?attachId=<%=am.getId()%>"><img src="images/clouddisk/download_1.gif"  title="下载"/></a></li>
						 <li><a><img  class="pulldown" src="images/clouddisk/pulldown_1.gif"  title="更多操作"/></a></li>
		                <!-- <li>
		                	<img class="restoreFile" src="images/clouddisk/restore_1.gif" amId='<%=am.getId()%>' docId='<%=am.getDocId() %>'  title="恢复"/>
		                </li>
                 		 <li>
                 		 	<img src="images/clouddisk/recycler_1.gif" amId='<%=am.getId()%>' docId='<%=am.getDocId() %>' class="deleteFile" title="彻底删除"/>
                 		 </li>
                 		  -->
						 </ul>
					</div>
					<ul class="op" id = "operate<%=am.getId() %>">
				    	<li><a  href="javascript:void(0)" class="fileReName" oldAmId="<%=am.getId()%>"  amId="<%=am.getId()%>"  docId="<%=am.getDocId() %>">重命名</a></li>
				        <li><a  href="javascript:showDialogTree('<%=am.getId()%>','<%=am.getId()%>','<%=am.getDocId() %>')" >移动</a></li>
				        <li><a href="javascript:delAttach('<%=am.getId()%>', '<%=doc.getID()%>','<%=am.getId()%>')">删除</a></li>
			    	</ul>
				</div>
				<div class="col">
	              <span><%=theLength%></span>
	            </div>
	            <div class="col">
	              <span><%=am.getExt() %>
	              <!-- <%=DateUtil.format(am.getDeleteDate(),"yyyy-MM-dd HH:mm")%> --></span>
	            </div>
	            <div class="col">
	            	<span><a href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&dir_code=<%=am.getCodeByDocId() %>"><%=am.getParentDir() %></a></span>
	            </div>
	          </dd>
	        </dl>
        <% 
        	}
        }%>
        
      </div>
    </div>
    <ul id="contextMenu" class="contextMenu">
		<li id="download" class="down">
			<a>下载</a>
		</li>
		<li id="delete" class="delete">
			<a>删除</a>
		</li>
		<li id="reName" class="rename">
			<a>重命名</a>
		</li>
		<!-- <li id="template" class="up">
			<a>角色模板</a>
		</li>
		 
		<li id="refresh" class="refresh">
			<a>刷新</a>
		</li>
		-->
	</ul>
	
	<!-- <div style="height:auto; position:fixed; top:50px;left:400px; z-index:1001"  id="dirTree" >
		<div id='SD_window' style='height:500px;dispaly:none'>
			<table cellspacing='0' cellpadding='0'  style='position:fixed; z-index:2311; top:100px; left:350px;'>
			<tbody >
			<tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr>
			<tr><td class='SD_bg'></td>
				<td id='SD_container'><h3 id='SD_title'>移动文件</h3>
					<div id='SD_body' style='height:400px;width:450px;overflow:auto; border:#999 4px solid;'>
						<div id='SD_content' >  
							<table width="100%"  border="0">
							  <tr>
							  	<td width="5" align="left">&nbsp;</td>
								  	<td align="left"><%
								  	    leaf = dir.getLeaf(userName);
										DirectoryView tv = new DirectoryView(leaf);
										UserSetupDb usd = new UserSetupDb();
										usd = usd.getUserSetupDb(privilege.getUser(request));
										String pageUrl = usd.isWebedit()?"dir_list.jsp":"dir_list_new.jsp";
										
										tv.ListSimple(out, "mainFileFrame", pageUrl, "op=editarticle&mode=" + mode, "", "" ); // "tbg1", "tbg1sel");
										%>
									</td>
								</tr>
							</table>
						</div>
					</div>
					<div id='SD_button'>
						<div class='SD_button'><a id='SD_confirm' onclick='closeBackGround();' >确定</a><a id='SD_cancel' onclick='closeDialog();'>取消</a></div>
					</div>
					<a href='javascript:closeDialog();' id='SD_close' title='关闭'></a>
				</td>
				<td class='SD_bg'></td>
			</tr>
			<tr>
				<td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td>
			</tr>
			</tbody>
			</table>
		</div>
	</div>	  -->
	<div id="treeBackground" class="treeBackground"></div>
</body>
<script>




var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
var select_file_content = $("#select_content").val();
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	//window.location.href = "?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle&orderBy=" + orderBy + "&sort=" + sort;
	window.location.href = "?select_sort=select_one&select_content=<%=text_content%>&orderBy=" + orderBy + "&sort=" + sort;
}

function doSort2(orderBy) {
	var ss = orderBy;
	if (orderBy==curOrderBy)
			sort = "desc";
	window.location.href = "?dir_code=<%=StrUtil.UrlEncode(dir_code)%>&op=editarticle&orderBy=" + orderBy + "&sort=" + sort + "&sort0="+ ss ;
	
}








</script>
</html>
