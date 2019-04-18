<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.person.UserDb"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="bsh.StringUtil"%>
<jsp:useBean id="docmanager" scope="page"
	class="com.redmoon.oa.fileark.DocumentMgr" />
<jsp:useBean id="privilege" scope="page"
	class="com.redmoon.oa.pvg.Privilege" />
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="dir" scope="page"
	class="com.redmoon.oa.netdisk.Directory" />
<%
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	NetDiskMyShared netDiskMyShared = new NetDiskMyShared();
	String username = privilege.getUser(request);
	Vector<Leaf> vector = netDiskMyShared.queryMySharedFile(username);
	Iterator<Leaf> iterator = null;
	Iterator<Attachment> attachIterator = null;
	HashMap<String,Integer> rootLeafCode = netDiskMyShared.queryRootCode(username);//获得所有共享文件根节点code
	String op = ParamUtil.get(request,"op");//操作标志位 查询分享文件中的所有子文件
	String dir_code = ParamUtil.get(request,"dir_code");//当前文件夹的code
	String doc_id = ParamUtil.get(request,"doc_id");
	String rootCode = ParamUtil.get(request, "root_code");
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "doc_id", doc_id, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}	
	
	//获取排序列
	String orderBy = "";
	String sort = "";
	orderBy = ParamUtil.get(request,"orderBy");
	sort = ParamUtil.get(request,"sort");
	if(op!=null && !op.trim().equals("")){
		if(op.equals("childLeaf")){
			Leaf leafFile = new Leaf();
			leafFile = leafFile.getLeaf(dir_code);
			iterator = 	leafFile.getChildren().iterator();//获得所有子文件
			if(doc_id!=null && !doc_id.equals("")){
				Vector<Attachment> vectorAttach = netDiskMyShared.queryChildAttachByDocId(doc_id,orderBy,sort);//获得所有文件下的附件
				attachIterator = vectorAttach.iterator();
			}
		}
	}else{
		iterator = vector.iterator();
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
	// 防XSS
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "dir_code", dir_code, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "doc_id", doc_id, getClass().getName());
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "rootCode", rootCode, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>

<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>云盘————我的协作</title>
<link type="text/css" rel="stylesheet" href="clouddisk.css"/>
<script src="../inc/common.js"></script>
<script src="../js/jquery1.7.2.min.js"></script>
<script src= "js/clouddisk_list.js"></script>
<script language=JavaScript src='formpost.js'></script>
<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />
<script type="text/javascript">
	$(function(){
		//回到顶部
		$(window).goToTop({
			showHeight : 1,//设置滚动高度时显示
			speed : 500 //返回顶部的速度以毫秒为单位
		});
	});
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

</script>
</head>
<body>
	<div id="Right" class="Right">
		<!-- <div class="rHead">
			<div class="rHead1" style="position: relative">
				<div class="sort">
					<span>排序：</span>
					<span id="sort_sel"><span></span><img
						src="images/clouddisk/sort_1.gif" height="11" width="10"
						style="margin-left: 5px" />
				</span>
				<div id="sort_sel_pull" style="right: 45px;">
					<p style="padding-top: 4px;padding-bottom:4px; ">
						<a style="cursor: pointer" class="uploadDate"><span>时间<img
									src="images/clouddisk/sort_1.gif" height="11" width="10"
									style="margin-left: 5px;" />
						</span>
						</a>
					</p>
					<p style="padding-top: 4px;padding-bottom:4px;">
						<a style="cursor: pointer" class="file_size"><span>大小<img
									src="images/clouddisk/sort_1.gif" height="11" width="10"
									style="margin-left: 5px" />
						</span>
						</a>
					</p>
					<p style="padding-top: 4px;padding-bottom:4px;">
						<a style="cursor: pointer" class="name"><span>名称<img
									src="images/clouddisk/sort_1.gif" height="11" width="10"
									style="margin-left: 5px" />
						</span>
						</a>
					</p>
				</div>
			
        

			</div>
		</div> -->
		<div class="fixedDivShare">
		<div class="rHead2Share">
			<%
			String showPath = "";
			Leaf lf = new Leaf(dir_code);
			String dir_name = lf.getName();
			Leaf rleaf = new Leaf(rootCode);
			boolean backFlag = true;
			if (dir_code.equals("")) {
				backFlag = false;
				showPath = "<a href='clouddisk_myshare.jsp'>所有文件</a>";
			} else if (lf.getCode().equals(rootCode)) {
				showPath = "<a href='clouddisk_myshare.jsp'>所有文件</a>    >>    <a href='clouddisk_myshare.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=childLeaf&doc_id=" + lf.getDocId() + "&root_code=" + rootCode + "'>" + dir_name + "</a>";
			} else {
				Leaf pleaf = new Leaf(lf.getParentCode());
				if (lf.getLayer() - rleaf.getLayer() == 1) {
					showPath = "<a href='clouddisk_myshare.jsp'>所有文件</a>    >>    <a href='clouddisk_myshare.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "&op=childLeaf&doc_id=" + pleaf.getDocId() + "&root_code=" + rootCode + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_myshare.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=childLeaf&doc_id=" + lf.getDocId() + "&root_code=" + rootCode + "'>" + dir_name + "</a>";
				} else {
					showPath = "<a href='clouddisk_myshare.jsp'>所有文件</a>    >>    ...>>    <a href='clouddisk_myshare.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "&op=childLeaf&doc_id=" + pleaf.getDocId() + "&root_code=" + rootCode + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_myshare.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=childLeaf&doc_id=" + lf.getDocId() + "&root_code=" + rootCode + "'>" + dir_name + "</a>";
				}
			}
			 %>
			 <%if(backFlag) {%>
			 <div style="float:left;margin-left:15px;margin-top:4px;"><a href="clouddisk_myshare.jsp<%=lf.getCode().equals(rootCode) ? "" : "?dir_code=" + StrUtil.UrlEncode(lf.getParentCode()) + "&op=childLeaf&doc_id=" + lf.getDocId() + "&root_code=" + rootCode  %>"><img src="images/clouddisk/back.png"/></a></div>
			 <%} %>
			 <div class='all_file'><%=showPath %></div>
			
		</div>
		<dl class="fileTitleDl">
		<dd class="fileTitle">
			<div class="fileNameDetailTitle">
                <span class="cbox_all"></span>
				<input id="filename_input" name="checkbox" class="title_cbox"
								type="checkbox" />
				<span class="fnameTitle" >文件名</span>
             </div>
            <div  class="colTitle">
              <span >大小</span>
            </div>
            <div class="colTitle">
              <span>类型</span>
            </div>
            <div class="colTitle" >
              <span >创建日期</span>
            </div>
          </dd>
    </dl>
	</div>
	</div>
	<div class="containCenterShare">
  
                <% 
                	while(iterator.hasNext()){
                		Leaf leaf =	iterator.next();
                		if (rootCode.equals("")) {
                			rootCode = leaf.getCode();
                		}
                %>
                <dl>
			  <dd class="fileGroup" >	
			  	<div class="fileNameDetail">
			  		  <div class="file_action" >
		                <ul style="display:none">
		                  <li ><a target="_blank"  href="clouddisk_downloaddir.jsp?code=<%=leaf.getCode() %>"><img src="images/clouddisk/download_1.gif" title="下载"/></a></li>
		                </ul>
		              </div>
		              <span class="cbox_icon"></span>
					  <input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" />
					 <img  src="images/sort/folder.png" class="extImg"/>
            	 			 <span class="fname"><a href="clouddisk_myshare.jsp?op=childLeaf&dir_code=<%=leaf.getCode()%>&doc_id=<%=leaf.getDocId()%>&root_code=<%=rootCode%>"><%=leaf.getName() %></a></span>		
				</div>
			      <div class="col" >
			         <span></span>
			       </div>
			       <div class="col">
			         <span>文件夹</span>
			       </div>
			       <div class="col">
			      	 <span><%=DateUtil.format((DateUtil.parse(leaf.getAddDate(),"yyyy-MM-dd HH:mm")),"yyyy-MM-dd HH:mm")%></span>
			       </div>
			    </dd>
			  </dl>	
        		
                <%}  
	 		if(attachIterator!=null){
	 			while(attachIterator.hasNext()){
	 				Attachment attach = (Attachment)attachIterator.next();
	 				String fileSize = UtilTools.getFileSize(attach.getSize());
	 				String ext = attach.getExt();
	 				 
	 	 %>
	 	 
        <dl>
         <dd class="fileGroup" >	
         	<div class="fileNameDetail">
         		  <div class="file_action" >
	                <ul>
	                  <li><a target="_blank" href="clouddisk_downloadfile.jsp?attachId=<%=attach.getId()%>"><img src="images/clouddisk/download_1.gif" title="下载"/></a></li>
	                </ul>
	              </div>
	               <span class="cbox_icon"></span>
	               <input class="cbox" name="filename"  type="checkbox" />	
	             <img  src="images/sort/<%=Attachment.getIcon(attach.getExt())%>" class="extImg"/>
            	<span class="fname"><a href="javascript:editdoc('<%=attach.getDocId()%>', '<%=attach.getId()%>', '<%=attach.getExt() %>')"><%=attach.getName() %></a></span>
    		</div>
           <div class="col">
              <span><%=fileSize %></span>
            </div>
            <div class="col">
              <span><%=ext%></span>
            </div>
            <div class="col">
              <span><%=DateUtil.format(attach.getUploadDate(), "yyyy-MM-dd HH:mm")%></span>
            </div>
          </dd>
        </dl>
        <%}}%>
		</div>
	</div>

</body>
</html>
