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
<head >
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>云盘————回收站</title>
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
<script src= "js/clouddisk.js"></script>
<script src= "js/clouddisk_list.js"></script>
<script src= "js/clouddisk_recycler.js"></script>
<script type="text/javascript" src="../js/jquery.toaster.netdisk.js"></script> 
<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />
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
	}else if(sort0.equals("version_date")){
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
			//回到顶部
			$(window).goToTop({
				showHeight : 1,//设置滚动高度时显示
				speed : 500 //返回顶部的速度以毫秒为单位
			});
		
			//批量删除鼠标按下 按上
			$('.deleteFile_c').mousedown(function(){
					$(this).css({"background":"url(images/clouddisk/deleteFile_2.gif)"});
				}).mouseup(function(){
					$(this).css({"background":"url(images/clouddisk/deleteFile_1.gif)"});
				});
			//批量恢复文件夹	
			$('.restoreFile_c').mousedown(function(){
				$(this).css({"background":"url(images/clouddisk/restoreBatch_2.gif)"});
			}).mouseup(function(){
				$(this).css({"background":"url(images/clouddisk/restoreBatch_1.gif)"});
			});
			//文件夹单击右键 出现功能菜单 json数据
		});
		//分类搜索
		function select_file(which){
			window.location.href="?select_file=select_file&select_which="+which;
		}
		//分类搜索
		function select_con(){
			var content = document.getElementById("select_content").value;
			if(content == "请输入文件名搜索..."){
				return;
			}else{
				window.location.href="clouddisk_search.jsp?select_sort=select_one&select_content="+content;
			}
		}

		
			      
	
	
	</script>
</head>

<body oncontextmenu="return false"> &nbsp;	
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
	// OA中封装的防SQL注入
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "sort", sort, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
%>
    
 
 <div id="Right" class="Right">
      <div class="rHead">
      <div class="fixedDivRecycler">
	    <div class="rHead1" >
		  <a onclick="removeBatch()"><div class="deleteFile_c"></div></a>
		  <a onclick="restoreBatch()"><div class="restoreFile_c"></div></a>
		  <div class="sort"  >
		    <!-- <span>排序：</span>
			<span id="sort_sel"><%=sort0 %><img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span>
			<div id="sort_sel_pull" style="right:16px;">
			  <p style="margin-top:5px "><a style="cursor:pointer" onClick="doSort2('version_date')"><span>时间<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px; "/></span></a></p>
			  <p style="margin-top:5px"><a style="cursor:pointer" onClick="doSort2('file_size')"><span>大小<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span></a></p>
			  <p style="margin-top:5px"><a style="cursor:pointer" onClick="doSort2('name')"><span>名称<img  src="images/clouddisk/sort_1.gif" height="11" width="10" style="margin-left:5px"/></span></a></p>
			</div>
			 -->
		  </div>
		</div>
		<div class="rHead2" style=" background:#f7f7f7;  z-index:20" >
		  <a href="clouddisk_recycler.jsp"><div class="all_file">所有文件</div></a>
          <% 
		  	String text_content = ParamUtil.get(request,"select_content");
          	if("".equals(text_content)){ text_content = "请输入文件名搜索..."; }
			try {
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "text_content", text_content, getClass().getName());
			}
			catch (ErrMsgException e) {
				out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
				return;
			}	
          %>
          <!--  
		  <div class="search"><input maxlength="30" value="<%=text_content %>" id="select_content" onblur="select_con()" onKeyDown="if (event.keyCode==13) {this.blur()}" onfocus="if(this.value=='请输入文件名搜索...') this.value=''"/></div>
		  <a>
            <div id="search_one" style=" float:right;width:36px;height:26px;position:relative; top:5px;right:-184px" onclick="select_con()"></div>
          </a>
		  <div class="sortShow" style="display: none;" >
		    <span>分类显示：</span>
			<ul>
			  <%String select_file = ParamUtil.get(request,"select_file");
			    String which = ParamUtil.get(request,"select_which");
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
		  </div>-->
		</div>
		<dl class="fileTitleDl">
          <dd class="fileTitle">
          	<div class="fileNameDetailTitle">
         	<span class="cbox_all"></span>
         		 <input id="filename_input" name="checkbox" class="title_cbox"
							type="checkbox" /> 
				   <span class="fnameTitle">文件名</span>
          	</div>
            <div class="colTitle">
              <span >大小</span>
            </div>
            <div class="colTitle">
              <span>删除时间</span>
            </div>
     
          </dd>
        </dl>
       </div>
		<form name="addform" action="fwebedit_do.jsp" method="post" style="padding:0px; margin:0px">
		
	  </div>
      <div class="containtCenterRecycler">
               
		 <%
			String select_sort = ParamUtil.get(request,"select_sort"); 
			String fileCurrent = ParamUtil.get(request,"cur");
			try {
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "select_sort", select_sort, getClass().getName());
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "cur", fileCurrent, getClass().getName());
			}
			catch (ErrMsgException e) {
				out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
				return;
			}
			if(fileCurrent.equals("current")){
				String currentName =  ParamUtil.get(request,"attachName");
				try {
					com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "attachName", currentName, getClass().getName());
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
           <dl>
			  <dd class="searchFileTitle" >	
			  	<div class="searchDetail">
            	 	<span class="searchInfo">
            	 	您搜索的内容是：<span  class="searchTitle"><%=text_content %></span>以下是您的搜索结果：
            	 	</span>		
				</div>
			    </dd>
			  </dl>	
           
           <%}else  if("请输入文件名搜索...".equals(text_content)&&!("select_file".equals(select_file))){
				Iterator irch = leaf.getRecyclerChildren().iterator();
				while (irch.hasNext()) {
					Leaf clf = (Leaf)irch.next();
				%>
                 <dl id="floder<%=clf.getCode()%>">
					  <dd class="fileGroup" id="folder<%=clf.getCode()%>"  type='file'>	
					  	<div class="fileNameDetail">
					  		 <span class="cbox_icon"></span>
							 <input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" value="<%=clf.getCode()%>" />
							 <img  src="images/sort/folder.png" class="extImg"/>
		            	 	<span class="fname floderNameInfo" id="span<%=clf.getCode()%>" dirCode="<%=clf.getCode()%>"  name="span<%=clf.getCode()%>">
		            	 	<a  title="<%=clf.getName()%>" href="javascript:void(0);" ><%=clf.getName()%></a></span>
		            	 	<div class="file_action">
				                <ul>
				                <li>
				                	<img  src="images/clouddisk/restore_1.gif" amId='<%=clf.getCode()%>' docId='<%=clf.getCode() %>'  title="恢复" onclick="restoreFolder('<%=clf.getCode() %>','<%=clf.getName() %>')"/>
				                </li>
		                 		 <li>
		                 		 	<img  src="images/clouddisk/recycler_1.gif" amId='<%=clf.getCode()%>' docId='<%=clf.getCode() %>'  title="彻底删除" onclick="delFolder('<%=clf.getCode() %>','<%=clf.getName() %>')"/>
		                 		 </li>
								 </ul>
							</div>		
						</div>
					      <div class="col" >
					         <span></span>
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
			String sql = "SELECT id FROM netdisk_document_attach WHERE page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=1 and is_deleted=1 order by ";
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
				sql = "SELECT id FROM netdisk_document_attach WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")+" and page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=0 and is_deleted=1 order by ";
				sql += orderBy + " " + sort;
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
				sql = "SELECT id FROM netdisk_document_attach WHERE page_num=1 and user_name = " + StrUtil.sqlstr(root_code)+ " and is_current=0 and is_deleted=1 and ext in" 
					+ (sb.toString().equals("") ? "" : "(" + sb.toString() + ")")
					+ " order by " + orderBy + " " + sort;
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
					 <input class="cbox" name="att_ids"  type="checkbox" value="<%=am.getId()%>" />
					<img  src="images/sort/<%=Attachment.getIcon(am.getExt())%>" class="extImg"/>
					 <span class="fname attNameInfo" id="span<%=am.getId() %>" attId="<%=am.getId() %>" name="span<%=am.getId() %>" >
					 	<a href="javascript:void(0);"><%=am.getName()%></a>
					 </span>
				    <div class="file_action">
		                <ul>
		                <li>
		                	<img class="restoreFile" src="images/clouddisk/restore_1.gif" amId='<%=am.getId()%>' docId='<%=am.getDocId() %>'  title="恢复"/>
		                </li>
                 		 <li>
                 		 	<img class="removeFile" src="images/clouddisk/recycler_1.gif" amId='<%=am.getId()%>' docId='<%=am.getDocId() %>'  title="彻底删除"/>
                 		 </li>
						 </ul>
					</div>
			
				</div>
				<div class="col">
	              <span><%=theLength%></span>
	            </div>
	            <div class="col">
	              <span><%=DateUtil.format(am.getDeleteDate(),"yyyy-MM-dd HH:mm")%></span>
	            </div>
	          </dd>
	        </dl>
        <%}
        } %>
      </div>
    </div>
    <ul id="contextMenu" class="contextMenu">
		<li id="restore" class="restore">
			<a>还原</a>
		</li>
		<li id="delete" class="tdelete">
			<a>彻底删除</a>
		</li>
	</ul>
	<div id="treeBackground" class="treeBackground"></div>
	<div id='loading' class='loading'><img src='images/loading.gif'></div>
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
</html>
