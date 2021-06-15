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

<!--  <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">-->
<html>
<head>
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>网盘————历史记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="clouddisk.css"/>
<link type="text/css" rel="stylesheet" href="showDialog/showDialog.css"/>
<script src= "js/clouddisk_list.js"></script>


<style>
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
</style>
 <script type="text/javascript">
 document.onmousemove = function () {
 var divx = window.event.clientX+"px";
 var divy = window.event.clientY+"px";
 //var ie5menua = document.getElementById("ie5menu");

 }
 </script>
<script src="../inc/common.js"></script>
<script src="../inc/upload.js"></script>

<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/activebar2.js"></script>
<script language=JavaScript src='formpost.js'></script>
<script language=JavaScript src='showDialog/jquery.min.js'></script>
<script language=JavaScript src='showDialog/showDialog.js'></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  
<script src= "js/clouddisk.js"></script>
<link href="../js/contextMenu/css/ContextMenu.css" rel="stylesheet"
	type="text/css" />
<script src="../js/contextMenu/jquery.contextMenu.js"
	type="text/javascript"></script>
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

String dir_code = ParamUtil.get(request, "dir_code");
String attachName = ParamUtil.get(request,"attachName");//查看历史版本的附件名称
if("".equals(dir_code)){ 
	dir_code = root_code;
}
// 防XSS
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "attachName", attachName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}


String dir_name = "";
 
int id = 0;

String correct_result = "操作成功！";

Document doc = new Document();
doc = docmanager.getDocumentByCode(request, dir_code, privilege);
Document template = null;
int newCurId = ParamUtil.getInt(request,"attachId",0);
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "attachId", newCurId+"", getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
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
int currentId  = 0;
String op = ParamUtil.get(request, "op");
if("".equals(op)){
	op="editarticle";
}
String mode = ParamUtil.get(request, "mode"); // select
String work = ParamUtil.get(request, "work"); // init modify
try {
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

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk");
%>
<script>


	function restoreCurrent(newId, attId){
		jConfirm("您确定要还原文件吗？","提示",function(r){
			if(!r){
				return;
			}else{
				window.location.href="clouddisk_list_do.jsp?op=restoreCurrent&newCurId="+newId+"&attachId="+attId;
			}
		});
	}


</script>

</head>

<body > &nbsp;	
<%
String orderBy = "version_date";
String sort = "desc";
Leaf dlf = new Leaf();
if (doc!=null) {
	dlf = dlf.getLeaf(doc.getDirCode());
}

%>
    
 
 <div id="Right" class="Right">
      <div class="rHead">
		<div class="rHead2" style=" background:#f7f7f7;  z-index:20" >
			<%
				String showPath = "";
				boolean backFlag = true;
				if (dir_code.equals(root_code)) {
					backFlag = false;
					showPath = "<a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>";
				} else if (leaf.getParentCode().equals(root_code)) {
					showPath = "<a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    <a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
				} else {
					Leaf pleaf = new Leaf(leaf.getParentCode());
					if (leaf.getLayer() == 3) {
						showPath = "<a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    <a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
					} else {
						showPath = "<a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"'>所有文件</a>    >>    ...>>    <a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_list.jsp?userName="+StrUtil.UrlEncode(userName)+"&dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + dir_name + "</a>";
					}
				}
				 %>
				 <%if(backFlag) {%>
				 <div style="float:left;margin-left:15px;margin-top:4px;"><a href="clouddisk_list.jsp?userName=<%=StrUtil.UrlEncode(userName) %>&dir_code=<%=StrUtil.UrlEncode(leaf.getParentCode()) %>"><img src="images/clouddisk/back.png"/></a></div>
				 <%}%>
				 <div class='all_file'><%=showPath %></div>
		  
		</div>
	  </div>
      <div class="containtCenter">
        <dl  class="fileTitleDl">
          <dd  class="fileTitle">
            <div class="fileNameDetailTitle" >
             <span class="cbox_all"></span>
			<input id="filename_input" name="checkbox" class="title_cbox"
					type="checkbox" />
              <span class="fnameTitle">文件名</span>
            </div>
            <div class="colTitle">
              <span >大小</span>
            </div>
            <div class="colTitle">
              <span>版本</span>
            </div>
            <div class="colTitle">
              <span >日期</span>
            </div>
          </dd>
        </dl>
        <dl>
		  <dd class="searchFileTitle" >	
		  	<div class="searchDetail">
           	 	<span class="searchInfo">
           	 	您正在搜索<span class="searchTitle"><%=attachName %></span> 的历史版本：以下是您的搜索结果：
           	 	</span>		
			</div>
		    </dd>
		  </dl>	
        
        <%
			String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
			int current_id = ParamUtil.getInt(request,"attachId",-1);
			
			try {
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "CPages", strcurpage, getClass().getName());
				com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "attachId", current_id+"", getClass().getName());
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
			am = am.getAttachment(current_id);
			long fileLength = -1;
			int pagesize = 50;
			String sql = "SELECT id FROM netdisk_document_attach WHERE name=" + StrUtil.sqlstr(am.getName()) + " and user_name = " + StrUtil.sqlstr(root_code)+ " and page_num=1 and visualPath = "+StrUtil.sqlstr(am.getVisualPath()) +" and is_deleted = 0 order by is_current desc,";
			sql += orderBy + " " + sort;
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
				  int resultLen = attachments.size(); 
				  while (ir.hasNext()) {
				  	am = (Attachment) ir.next(); 
				  	String theLength = UtilTools.getFileSize(am.getSize());
			%>
					
		<dl id="tree<%=am.getId()%>" class="fileDl"> 
		<dd class="fileGroup" >	
			<div class="fileNameDetail">
				<span class="cbox_icon"></span>
					<input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" value="<%=am.getId()%>" />
					<img  src="images/sort/<%=Attachment.getIcon(am.getExt())%>" class="extImg"/>
					 <span class="fname" id="span<%=am.getId() %>" name="span<%=am.getId() %>" >
					 	<a href="javascript:editdoc('<%=id%>', '<%=am.getId()%>', '<%=am.getExt() %>')"><%=am.getName()%></a>
					 </span>
				    <div class="file_action">
		                <ul>
			              	 <li><a id="download<%=am.getId() %> " name="download<%=am.getId()%>" target="_blank" href="clouddisk_downloadfile.jsp?attachId=<%=am.getId()%>"><img src="images/clouddisk/download_1.gif" title="下载"/></a></li>
			              		<% if(!am.isCurrent()){
              					%>
			              	 <li><a id="restore<%=am.getId() %> " name="restore<%=am.getId()%>" onclick="restoreCurrent('<%=newCurId %>','<%=am.getId() %>')" ><img src="images/clouddisk/restore_1.gif" title="还原"/></a></li>
			              	 <%} %>
						 </ul>
					</div>
			</div>
			<div class="col">
              <span><%=theLength%></span>
            </div>
            <div class="col">
            	<% if(am.isCurrent()){
              	%>
              	<span style="color:red">最新</span>
             	<% }else{%>
             	<span><%=--resultLen %></span>
             	<%} %>
            </div>
            <div class="col">
              <span><%=DateUtil.format(am.getVersionDate(), "yyyy-MM-dd HH:mm")%></span>
            </div>
          </dd>
        </dl>
        <%}
        } %>
      </div>
</html>
