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
<script src="js/clouddisk_tiled.js"></script>

<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />
<script type="text/javascript" src="../js/jquery.toaster.netdisk.js"></script> 
<!-- swfupload 文件普通上传 -->
<script src= "swfupload/swfupload.js"></script>
<script type="text/javascript" src="swfupload/swfupload.queue.js"></script>
<script src= "js/swfupload.js"></script>
<script src= "js/clouddisk_public_share.js"></script> 
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
String unit_code = privilege.getUserUnitCode(request);
String dir_code = ParamUtil.get(request, "dir_code");
if("".equals(dir_code)){ 
	dir_code = PublicLeaf.ROOTCODE;
}
String select_sort = ParamUtil.get(request,"select_sort"); //搜索判断
String text_content = ParamUtil.get(request,"select_content");//搜索内容
String which = ParamUtil.get(request,"select_which");
String select_file = ParamUtil.get(request,"select_file");//文件类别的判断
PublicLeafPriv lp = new PublicLeafPriv(dir_code);
if (!lp.canUserSeeByAncestor(privilege.getUser(request))){
	out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
boolean canManage = false;
boolean canInsert = false;
boolean isMappingDir = false;//判断是不是映射目录

PublicLeaf publicLeaf = new PublicLeaf(dir_code);
String mappingAddress = ParamUtil.get(request,"mappingAddress");
String mappingCode = ParamUtil.get(request,"mappingCode");
if(mappingAddress.equals("")){
	mappingAddress = publicLeaf.getMappingAddress();//获得物理地址
}
if(mappingCode.equals("")){
	mappingCode = publicLeaf.getCode();//被映射的文件夹的code
}
Vector dvt = new Vector();
Vector fvt = new Vector();
File curFile = null;

//如果mappAddress不为空，说明是映射目录
if(!mappingAddress.trim().equals("")&&mappingAddress!=null){
	canManage = true;
	canInsert = false;
	isMappingDir = true;
	curFile = new File(mappingAddress);
	File[] FileDirectoryArr = null;
	FileDirectoryArr = curFile.listFiles();
	if(FileDirectoryArr != null && FileDirectoryArr.length>0){
		for(File fileDirAtt:FileDirectoryArr){
			if(fileDirAtt.isDirectory()){
				dvt.addElement(fileDirAtt);
			}else{
				fvt.addElement(fileDirAtt);
			}
		}
	}

}else{
	canManage = lp.canUserManage(userName);
	canInsert = lp.canUserAppend(userName);
}
String correct_result = "操作成功！";
String filePath = publicLeaf.getFilePath();
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String isUseNTKO = cfg.get("isUseNTKO");
HashMap<String,String> explorerFileType = new HashMap<String,String>();
explorerFileType = UtilTools.uploadFileTypeByExplorer("netdisk_ext");
com.redmoon.clouddisk.Config myconfig = com.redmoon.clouddisk.Config.getInstance();
String file_size_limit = myconfig.getProperty("file_size_limit");
int file_upload_limit = myconfig.getIntProperty("file_upload_limit");
String upload_file_types = explorerFileType.get("ie_upload_file_types");
String fixfox_upload_file_types = explorerFileType.get("fixfox_upload_file_types");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_sort", select_sort, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "text_content", text_content, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "which", which, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_file", select_file, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mappingAddress", mappingAddress, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "mappingCode", mappingCode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

%>

</head>

<body> &nbsp;	
 
 <div id="Right" class="Right">
	 	 <!-- lzm -->
		<input type="hidden" value="<%=select_sort %>" id="isSearch"/>
		<input type="hidden" value="<%=text_content%>"  id="content"/>
		<input type="hidden" value="<%=which%>" id="which"/>
		<input type="hidden" value="<%=select_file %>" id="fileType"/>
		<input type="hidden" value="<%=userName %>" id="userName" />
		<input type="hidden" value="<%=dir_code %>" id="dirCode"/>
		<input type="hidden" value="<%=canManage %>" id="canManage" />
		<input type="hidden" value="<%=canInsert %>" id="canInsert" />
		<input type="hidden" value="2" id="pageNo"/> 
		<input type="hidden" value="<%=isUseNTKO %>" id="isUseNTKO" />
		<input type="hidden" value="netdisk_public_attach_do.jsp;jsessionid=<%=session.getId() %>" id="uploadUrl" />
		<input type="hidden" value="<%=file_size_limit %>" id="fileSizeLimit" />
		<input type="hidden" value="<%=file_upload_limit %>" id="fileUploadLimit" />
		<input type="hidden" value="<%=upload_file_types %>" id="uploadFileType" />
		<input type="hidden" value="<%=fixfox_upload_file_types %>" id="FixfoxUploadFileType" />
		<input type="hidden" value="<%=isMappingDir %>" id="isMappingDir"/>
		<input type="hidden" value="<%=StrUtil.UrlEncode(mappingAddress) %>" id="mappingAddress"/>
		<input type="hidden" value="<%=StrUtil.UrlEncode(mappingCode)%>" id="mappingCode" />

      <div class="rHead">
	      	<div class="fixedDivPublicTiled">
			    <div class="rHead1" >
			     <form action="netdisk/netdisk_public_attach_do.jsp" method="post" name="thisform" enctype="multipart/form-data">
						<div class="uploadFile_c">
							  <div class="upload_sel" id="upload_sel" style="display:none;" >
							     <ul>
									<li>
										 <a><span class="TextStyle" id="spanButtonPlaceholder"></span></a>
									</li>
									<li>
										<a onclick="showDigFile()">极速</a>
									</li>
								</ul>
							  </div>
					  	</div>
				  	</form>
				  	<div class="manageFolder managerRoot"></div>
				 	<div class="deleteFile_c"  onclick="delBatch()" >
				 	</div>
					<div class="view"  >
						<div id="view_1"  style="cursor: pointer; background: url(images/clouddisk/view_list_1.gif); width: 30px; height: 24px; border-right: 1px solid #cacaca; float: left"></div>
						<div id="view_2" style="cursor: pointer; background: url(images/clouddisk/view_thumbnail_2.gif); width: 29px; height: 24px; float: left"></div>
					</div>
				</div>
				<div class="rHead2" style=" background:#f7f7f7;  z-index:20; " >
					<div class= "checkboxDetail">
						<span class="length"></span>
						<span class="cancle"></span>
					</div>
					<div style="float:left;width:10px;margin-left:10px;">
						<span id="dirAttAllCboxIcon"></span>
						<input id="dirAttAllCbox" name="checkbox"  type="checkbox"/>
					</div>
				
    <%
			String showPath = "";
			boolean backFlag = true;
			if(!isMappingDir){
				if (dir_code.equals(PublicLeaf.ROOTCODE)) {
					backFlag = false;
					showPath = "<a href='clouddisk_public_share_tiled.jsp'>所有文件</a>";
				} else if (publicLeaf.getParentCode().equals(PublicLeaf.ROOTCODE)) {
					showPath = "<a href='clouddisk_public_share_tiled.jsp'>所有文件</a>    >>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
				} else {
					PublicLeaf pleaf = new PublicLeaf(publicLeaf.getParentCode());
					if (publicLeaf.getLayer() == 3) {
						showPath = "<a href='clouddisk_public_share_tiled.jsp'>所有文件</a>    >>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
						
					} else {
						showPath = "<a href='clouddisk_public_share_tiled.jsp?'>所有文件</a>    >>    ...>>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
					}
				}
			}else{
				PublicLeaf pl = new PublicLeaf(mappingCode);
				PublicLeaf pleaf = new PublicLeaf(pl.getParentCode());
				int layer = pl.getLayer();
				JSONObject json = new JSONObject();
				json.put("layer",0);
				json.put("isExists",false);
				json = UtilTools.getLayerByDir(pl.getMappingAddress(),curFile.getName(),json);
				boolean isExists = json.getBoolean("isExists");
				int dirLayer = json.getInt("layer");
				if(layer ==2){
					if(!isExists){
						showPath = "<a href='clouddisk_public_share_tiled.jsp'>所有文件</a>    >>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
					}else{
						if(dirLayer == 1){
							showPath = "<a href='clouddisk_public_share_tiled.jsp?'>所有文件</a>    >>    ...>>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(pl.getCode()) + "'>" + pl.getName() + "</a>    >>    <a href='clouddisk_public_share_tiled.jsp?mappingAddress="+StrUtil.UrlEncode(mappingAddress)+"&mappingCode=" + StrUtil.UrlEncode(mappingCode) + "'>" + curFile.getName() + "</a>";	
						}else if(dirLayer >1){
							File parent = curFile.getParentFile();
							showPath = "<a href='clouddisk_public_share_tiled.jsp?'>所有文件</a>    >>    ...>>    <a href='clouddisk_public_share_tiled.jsp?mappingCode="+StrUtil.UrlEncode(mappingCode)+"&mappingAddress=" + StrUtil.UrlEncode(parent.getAbsolutePath()) + "'>" + parent.getName() + "</a>    >>    <a href='clouddisk_public_share_tiled.jsp?mappingAddress="+StrUtil.UrlEncode(mappingAddress)+"&mappingCode=" + StrUtil.UrlEncode(mappingCode) + "'>" + curFile.getName() + "</a>";	
						}else{
							showPath = "<a href='clouddisk_public_share_tiled.jsp'>所有文件</a>    >>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
						}
					}
				}else{
					if(layer >=3){
						if(!isExists){
							showPath = "<a href='clouddisk_public_share_tiled.jsp?'>所有文件</a>    >>    ...>>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(mappingCode) + "'>" + pl.getName()+ "</a>";
						}else{
							if(dirLayer == 1){
								showPath = "<a href='clouddisk_public_share_tiled.jsp?'>所有文件</a>    >>    ...>>    <a href='clouddisk_public_share_tiled.jsp?dir_code=" + StrUtil.UrlEncode(pl.getCode()) + "'>" + pl.getName() + "</a>    >>    <a href='clouddisk_public_share_tiled.jsp?mappingAddress="+StrUtil.UrlEncode(mappingAddress)+"&mappingCode=" + StrUtil.UrlEncode(mappingCode) + "'>" + curFile.getName() + "</a>";	
							}else{
								File parent = curFile.getParentFile();
								showPath = "<a href='clouddisk_public_share_tiled.jsp?'>所有文件</a>    >>    ...>>    <a href='clouddisk_public_share_tiled.jsp?mappingCode="+StrUtil.UrlEncode(mappingCode)+"&mappingAddress=" + StrUtil.UrlEncode(parent.getAbsolutePath()) + "'>" + parent.getName() + "</a>    >>    <a href='clouddisk_public_share_tiled.jsp?mappingAddress="+StrUtil.UrlEncode(mappingAddress)+"&mappingCode=" + StrUtil.UrlEncode(mappingCode) + "'>" + curFile.getName() + "</a>";
							}
						}
					}
				}
			}
			
			 %>
			 <%if(backFlag) {%>
			 	<div style="float:left;margin-left:15px;margin-top:4px;">
			 		<% if(!isMappingDir){ %>
				 		<a href="clouddisk_public_share_tiled.jsp?dir_code=<%=StrUtil.UrlEncode(publicLeaf.getParentCode()) %>">
				 			<img src="images/clouddisk/back.png"/>
				 		</a>
			 		<%}else{
			 			JSONObject json = new JSONObject();
			 			PublicLeaf pl = new PublicLeaf(mappingCode);
						json.put("layer",0);
						json.put("isExists",false);
						json = UtilTools.getLayerByDir(pl.getMappingAddress(),curFile.getName(),json);
						boolean isExists = json.getBoolean("isExists");
						int dirLayer = json.getInt("layer");
			 			%>
			 			<% if(!isExists){ %>
			 				<a href="clouddisk_public_share_tiled.jsp?dir_code=<%=StrUtil.UrlEncode(publicLeaf.getParentCode()) %>">
				 				<img src="images/clouddisk/back.png"/>
				 			</a>
			 			<%}else{ 
				 			if(dirLayer==1){%>
				 				<a href="clouddisk_public_share_tiled.jsp?dir_code=<%=StrUtil.UrlEncode(mappingCode) %>">
						 			<img src="images/clouddisk/back.png"/>
						 		</a>
				 			<%}else{ %>
				 				<a href="clouddisk_public_share_tiled.jsp?mappingCode=<%=StrUtil.UrlEncode(mappingCode) %>&mappingAddress=<%=StrUtil.UrlEncode(curFile.getParentFile().getAbsolutePath()) %>">
						 			<img src="images/clouddisk/back.png"/>
						 		</a>
				 			<% }%>
			 			<% }%>
				 	<%} %>
			 	</div>
			 <%}
			%>
				
				<div class='all_file'><%=showPath %></div>
				<div class="search"><input maxlength="30" value="<%=text_content%>" id="select_content"  onblur="select_con()"  onKeyDown="if (event.keyCode==13) {this.blur()}" onfocus="if(this.value=='请输入文件名搜索...') this.value=''"/></div>
				<a>
	            	<div id="search_one" style=" float:right;width:36px;height:26px;position:relative; top:5px;right:-184px" onclick="select_con()"> </div>
	         	</a>
	         	<div class="sortShow">
					<span>分类显示：</span>
					<ul>
						<li class="imgShow">
							<img id="img_sort" style="cursor: pointer"
								src="images/clouddisk/imgShow_1.gif">
						</li>
						<li class="fileShow">
							<img id="file_sort" style="cursor: pointer"
								src="images/clouddisk/fileShow_1.gif" />
						</li>
						<li class="videoShow">
							<img id="video_sort" style="cursor: pointer"
								src="images/clouddisk/videoShow_1.gif" />
						</li>
						<li class="musicShow">
							<img id="music_sort" style="cursor: pointer"
								src="images/clouddisk/musicShow_1.gif" />
						</li>
					</ul>
				</div>
				
				</div>
				
		</div>
		 <ul class="attDirGroup" id="publicShareAttDirUl">
		
       		<% 
	       	 if(!isMappingDir){
	       		if(!select_file.trim().equals("select_file")){
					PublicDirectory pdDir = new PublicDirectory();
					Iterator itPl = pdDir.queryPublicDirectory(request,dir_code);
					boolean canUserManage = false;
					if(itPl != null){
						while(itPl.hasNext()) {
							PublicLeaf pl = (PublicLeaf)itPl.next();
							PublicLeafPriv lf = new PublicLeafPriv(pl.getCode());
							canUserManage = lf.canUserManage(userName);
							if(lf.canUserSee(userName)){
								PublicLeaf parentPublicLeaf = new PublicLeaf(pl.getParentCode());
				%>
				         <li class="publicShareDir"  canMappingAddress="false" id="dirAtt<%=pl.getCode()%>"   canUserManage=<%=canUserManage %>   title="<%=com.cloudwebsoft.framework.security.AntiXSS.clean(pl.getName())%>"  dirCode="<%=pl.getCode()%>" dirName="<%=com.cloudwebsoft.framework.security.AntiXSS.clean(pl.getName())%>">
				         	<!--
				    		<input class="attDirCheckBox"  name="floder_ids" type="checkbox" value="<%=pl.getCode()%>" />
					    	<div class="attDirCheckIcon" ></div>  -->
					  
					   	 	<div class="attDirIcon">
					   	 		 <a url ="clouddisk_public_share_tiled.jsp?dir_code=<%=StrUtil.UrlEncode(pl.getCode())%>"  href="clouddisk_public_share_tiled.jsp?dir_code=<%=StrUtil.UrlEncode(pl.getCode())%>" >
					   	 			<img src="images/clouddisk_tiled/folder.png" />
					   	 		 </a>
					        </div>
					        <div class="attDirName">
					        	 <a href="clouddisk_public_share_tiled.jsp?dir_code=<%=StrUtil.UrlEncode(pl.getCode())%>">
					        	 	<%=com.cloudwebsoft.framework.security.AntiXSS.clean(pl.getName())%>
					        	 </a>
					        </div>
						  </li>
			  <%
				}
			  	}
			  		}
			  			}%>
			  <% 
				//附件列表
	  				PublicAttachmentMgr pam = new PublicAttachmentMgr();
	  				Vector attVec = pam.getAttachmentList(dir_code,request);
	  				Iterator attIt = null;
	  				if( attVec!=null && attVec.size() >0 ){
	  					attIt = attVec.iterator();
	  					while(attIt.hasNext()) {
	  						PublicAttachment publicAtt = (PublicAttachment)attIt.next();
	  						//获得附件所在的dirCode
	  						PublicLeafPriv lfAtt = new PublicLeafPriv(publicAtt.getPublicDir()); 
	  						//用户允许删除文件夹下附件
	  						if(lfAtt.canUserSee(userName)){
		  						boolean canUserManage = lfAtt.canUserManage(userName);
		  						String theLength = UtilTools.getFileSize(publicAtt.getSize());
		  						PublicLeaf parentLeaf = new PublicLeaf(publicAtt.getPublicDir());
		  						String ext = publicAtt.getExt();
	  						
		  
		  %>
						 <li class="publicShareAtt" attName="<%=publicAtt.getName()%>"  id="publicShareAtt<%=publicAtt.getId()%>" canMappingAddress="false"  canUserManage=<%=canUserManage %>  title="<%=publicAtt.getName()%>" attId="<%=publicAtt.getId()%>"  attExt="<%=ext %>"  extType="<%=UtilTools.getConfigType(ext) %>">
						    	<% if(canUserManage){%>
							    	<input class="attDirCheckBox"  name="att_ids" type="checkbox" value="<%=publicAtt.getId()%>" />
							    	<div class="attDirCheckIcon" ></div>
							    <%}%>
						   	 	<div class="attDirIcon">
				                    <a target="_blank" url="netdisk_public_downloadfile.jsp?id=<%=publicAtt.getId()%>"  href="netdisk_public_downloadfile.jsp?id=<%=publicAtt.getId()%>">
				                    <img src="images/clouddisk_tiled/<%=Attachment.getIcon(publicAtt.getExt()) %>"  class="extImg"/></a>                               	
						        </div>
						        <div class="attDirName" id="attName<%=publicAtt.getId() %>">
						        	<a href="netdisk_public_downloadfile.jsp?id=<%=publicAtt.getId()%>">
						        	 	<%=publicAtt.getName()%>
						        	</a>
						        </div>
						    </li>
			  <%}
			  		}
			  			}
	       	 }else{
	       		Iterator mappDirIt = null;
			 	mappDirIt = dvt.iterator();
			 	while(mappDirIt!=null && mappDirIt.hasNext()){
			 		File file = (File)mappDirIt.next();
		  			%>
		  			  <li class="publicShareDir"   canMappingAddress="true" mappingAddress="<%=StrUtil.UrlEncode(file.getAbsolutePath())%>" >
				   	 	<div class="attDirIcon">
				   	 		 <a url ="clouddisk_public_share_tiled.jsp?mappingCode=<%=StrUtil.UrlEncode(mappingCode) %>&mappingAddress=<%=StrUtil.UrlEncode(file.getAbsolutePath())%>"  href="clouddisk_public_share_tiled.jsp?mappingCode=<%=StrUtil.UrlEncode(mappingCode)%>&mappingAddress=<%=StrUtil.UrlEncode(file.getAbsolutePath())%>" >
				   	 			<img src="images/clouddisk_tiled/folder.png" />
				   	 		 </a>
				        </div>
				        <div class="attDirName">
				        	 <a href="clouddisk_public_share_tiled.jsp?mappingCode=<%=StrUtil.UrlEncode(mappingCode) %>&mappingAddress=<%=StrUtil.UrlEncode(file.getAbsolutePath())%>">
				        	 	<%=file.getName()%>
				        	 </a>
				        </div>
					  </li>
		  				
		  		<%}
	 			Iterator mappingAttIt = null;
	 			mappingAttIt = fvt.iterator();
	 			while(mappingAttIt!=null && mappingAttIt.hasNext()){
	 				File mappingAttFile = (File)mappingAttIt.next();
	 				String theLength = UtilTools.getFileSize(mappingAttFile.length());
	 				String path = mappingAttFile.getAbsolutePath();
	 				String name = mappingAttFile.getName();
	 				String ext = StrUtil.getFileExt(name);
	 		%>
		  		 <li class="publicShareAtt"  canMappingAddress="true"  mappingAddress="<%=StrUtil.UrlEncode(path)%>" title="<%=name%>" >
			   	 	<div class="attDirIcon">
	                    <a target="_blank" url="netdisk_mapping_downloadfile.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>"  href="netdisk_mapping_downloadfile.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>">
	                    <img src="images/clouddisk_tiled/<%=Attachment.getIcon(ext) %>"  class="extImg"/></a>                               	
			        </div>
			        <div class="attDirName">
			        	<a href="netdisk_mapping_downloadfile.jsp?mappingAddress=<%=StrUtil.UrlEncode(path)%>">
			        	 	<%=name%>
			        	</a>
			        </div>
			    </li>
		  		
		  	<%} 
		  		}%>
        </ul>
	</div>
</div>
	<table id="SD_share_table" cellspacing='0' cellpadding='0'  style='position:fixed;  z-index:1911; top:32.5px; left:20%;margin: 2000px;'>
 	<tbody >
    	<tr>
        	<td class='SD_bg'></td>
            <td class='SD_bg'></td>
            <td class='SD_bg'></td>
        </tr>
        <tr>
        	<td class='SD_bg'></td>
            <td id='SD_share_container'>
            	<h3 id='SD_share_title'>附件上传</h3>
                <div id='SD_share_body' style='height:75px; width:385px;overflow:auto; border:#999 4px solid;'>
                    <div id='SD_share_content' >
                    <object classid="CLSID:DE757F80-F499-48D5-BF39-90BC8BA54D8C"
			           codebase="../activex/cloudym.CAB#version=1,2,0,1" width="400" style="height:75px;" align="middle" id="webedit">
			            <param name="Encode" value="utf-8" />
			            <param name="MaxSize" value="<%=Global.MaxSize%>" />
			            <!--上传字节-->
			            <param name="ForeColor" value="(0,0,0)" />
			            <param name="BgColor" value="(255,255,255)" />
			            <param name="ForeColorBar" value="(255,255,255)" />
			            <param name="BgColorBar" value="(104,181,200)" />
			            <param name="ForeColorBarPre" value="(0,0,0)" />
			            <param name="BgColorBarPre" value="(230,230,230)" />
			            <param name="FilePath" value="<%=filePath%>" />
			            <param name="Relative" value="1" />
			            <!--上传后的文件需放在服务器上的路径-->
			            <param name="Server" value="<%=request.getServerName()%>" />
			            <param name="Port" value="<%=request.getServerPort()%>" />
			            <param name="VirtualPath" value="<%=Global.virtualPath%>" />
			            <param name="PostScript" value="<%=Global.virtualPath%>/netdisk/netdisk_public_attach_do.jsp" />
			            <param name="PostScriptDdxc" value="" />
			            <param name="SegmentLen" value="204800" />
			            <param name="info" value="文件拖放区" />
						<%
			            License license = License.getInstance();	  
			            %>
			            <param name="Organization" value="<%=license.getCompany()%>">
			            <param name="Key" value="<%=license.getKey()%>">            
			          </object>
                    </div>
                 </div>
                 <div id='SD_share_button'>
                 	<div class='SD_button'>
                        <a id='SD_share_cancel' >取消</a>
                    </div>
                  </div>
                  <a href='javascript:void(0);' id='SD_share_close' title='关闭'></a>
             </td>
             <td class='SD_bg'></td>
         </tr>
         <tr>
         	<td class='SD_bg'></td>
            <td class='SD_bg'></td>
            <td class='SD_bg'></td>
         </tr>
         </tbody>
  	 </table> 
	<ul id="contextPublicDirMenu" class="contextMenu">
		<li id="down" class="down" >
			<a>下载</a>
		</li>
		<li id="priv" class="setup">
			<a>管理</a>
		</li>
	</ul>
	<ul id="contextPublicAttach" class="contextMenu">
		<li id="see" class="open">
			<a>打开</a>
		</li>
		<li id="edit" class="edit">
			<a>编辑</a>
		</li>
		<li id="down" class="down">
			<a>下载</a>
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
	</ul>
</body>

</html>
