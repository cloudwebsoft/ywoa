<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
<jsp:useBean id="docmanager" scope="page"
	class="com.redmoon.oa.fileark.DocumentMgr" />
<jsp:useBean id="privilege" scope="page"
	class="com.redmoon.oa.pvg.Privilege" />
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="dir" scope="page"
	class="com.redmoon.oa.netdisk.Directory" />
<%
	NetDiskCooperate cooperate = new NetDiskCooperate();
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	String userName = privilege.getUser(request);
	String dir_code = ParamUtil.get(request,"dir_code");
	String doc_id = ParamUtil.get(request,"doc_id");
	String rootCode = ParamUtil.get(request, "root_code");
	ResultIterator ri = cooperate.queryMyAttendCooperate(userName);//动态获得我参与的协作
	HashMap<String,Integer> hashMap = cooperate.queryMyAttendCooperateRootCode(userName);//我参与的协作的根节点
	String op = ParamUtil.get(request,"op");//操作标志位 查询我参与的协作文件中的所有子文件
	Iterator<Leaf> cooperateChildLeafIr = null;
	Iterator<Attachment> attachIterator = null;
	NetDiskMyShared netDiskMyShared = new NetDiskMyShared();
	if(op!=null && !op.equals("")){
		if(op.equals("cooperateChildFile")){
			Leaf cooperateLeaf = new Leaf();
			cooperateLeaf = cooperateLeaf.getLeaf(dir_code);
			cooperateChildLeafIr = cooperateLeaf.getChildren().iterator();//获得所有我参与的协作文件中的子文件
			if(doc_id!=null && !doc_id.equals("")){//获得所有我参与的写作的文件中子文件夹的附件
				String orderBy = "uploadDate";
				String sort = "desc";
				Vector<Attachment> vectorAttach = netDiskMyShared.queryChildAttachByDocId(doc_id,orderBy,sort);//获得所有文件下的附件
				attachIterator = vectorAttach.iterator();
			}
		}
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
<html>
<head>
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>网盘————我的协作</title>
<link type="text/css" rel="stylesheet" href="clouddisk.css" />
<script src="../inc/common.js"></script>
<link type="text/css" rel="stylesheet" href="../js/showDialog/showDialog.css"/>
<script language=JavaScript src='../js/showDialog/jquery.min.js'></script>
<script language=JavaScript src='../js/showDialog/showDialog.js'></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src= "js/clouddisk_list.js"></script>
<script language=JavaScript src='formpost.js'></script>
<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />
<script type="text/javascript">
	$(function(){
		//回到顶部
		$(window).goToTop({
			showHeight : 1,//设置滚动高度时显示
			speed : 500 //返回顶部的速度以毫秒为单位
		});
		$(".cooperateChange").click(function(e){
			e.stopPropagation();
			var dirCodeLog = $(this).attr("dir_code");
			$.ajax({
				type:"post",
				url:"clouddisk_cooperate_do.jsp",
				dataType:"json",
				data :{"op":"cooperateLog","dir_code":dirCodeLog},
				success: function(data, status){
					if(data.result == 1){
						if(data.total == 0){
							showDialog('info','<div style="font-size:12px;color:#666;margin:15px;">您暂时没有最新的协作动态</div>');
						}else{
							var contentCenter =' <div class="windowContainCenter">';
							var row ="";
							var tr = "";
							var title = "您最近有"+data.total+"条协作动态";
							$.each(data.cooperateLogs,function(index,data){
								row +='<div class="row"><span class="cols first" >'+data.actionDate+'</span><span  class="cols name">'+data.userName+'</span><span class="cols name2">'+data.actionDesc+'</span><span class="cols actionName">'+data.actionName+'</span></div>';
							});
							var result = contentCenter+row+"</div>";
							showDialog('window',result,title,560,342);
						}
					}else{
						showDialog('alert','请求失败');
					}
	
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
			});
		});
	});
	
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
</script>
</head>
<body>
<div id="Right" class="Right"></div>
	<div class="fixedDivCooperate">
	<div class="rHead2Cooperate">
		<%
			String showPath = "";
			Leaf leaf = new Leaf(dir_code);
			String dir_name = leaf.getName();
			Leaf rleaf = new Leaf(rootCode);
			boolean backFlag = true;
			if (dir_code.equals("")) {
				backFlag = false;
				showPath = "<a href='clouddisk_cooperate.jsp'>所有文件</a>";
			} else if (leaf.getCode().equals(rootCode)) {
				showPath = "<a href='clouddisk_cooperate.jsp'>所有文件</a>    >>    <a href='clouddisk_cooperate.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=cooperateChildFile&doc_id=" + leaf.getDocId() + "&root_code=" + rootCode + "'>" + dir_name + "</a>";
			} else {
				Leaf pleaf = new Leaf(leaf.getParentCode());
				if (leaf.getLayer() - rleaf.getLayer() == 1) {
					showPath = "<a href='clouddisk_cooperate.jsp'>所有文件</a>    >>    <a href='clouddisk_cooperate.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "&op=cooperateChildFile&doc_id=" + pleaf.getDocId() + "&root_code=" + rootCode + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_cooperate.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=cooperateChildFile&doc_id=" + leaf.getDocId() + "&root_code=" + rootCode + "'>" + dir_name + "</a>";
				} else {
					showPath = "<a href='clouddisk_cooperate.jsp'>所有文件</a>    >>    ...>>    <a href='clouddisk_cooperate.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "&op=cooperateChildFile&doc_id=" + pleaf.getDocId() + "&root_code=" + rootCode + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_cooperate.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "&op=cooperateChildFile&doc_id=" + leaf.getDocId() + "&root_code=" + rootCode + "'>" + dir_name + "</a>";
				}
			}
			 %>
			 <%if(backFlag) {%>
			 <div style="float:left;margin-left:15px;margin-top:4px;"><a href="clouddisk_cooperate.jsp<%=leaf.getCode().equals(rootCode) ? "" : "?dir_code=" + StrUtil.UrlEncode(leaf.getParentCode()) + "&op=childLeaf&doc_id=" + leaf.getDocId() + "&root_code=" + rootCode %>"><img src="images/clouddisk/back.png"/></a></div>
			 <%} %>
			 <div class='all_file'><%=showPath %></div>
	</div>
	</div>
	<div class="containCenterCooperate">
	<dl class="fileTitleDl">
		<dd class="fileTitle">
			<div class="fileNameDetailTitle">
                <span class="cbox_all"></span>
				<input id="filename_input" name="checkbox" class="title_cbox"
								type="checkbox" />
				<span class="fnameTitle" >文件名</span>
             </div>
            <div  class="colTitle">
              <span >分享者</span>
            </div>
            <div class="colTitle">
              <span>修改日期</span>
            </div>
            <div class="colTitle" >
              <span >协作动态</span>
            </div>
          </dd>
    </dl>
<% if(dir_code!=null && !dir_code.equals("")){
%>

  <% 
   	if(dir_code!=null && !dir_code.equals("")){
   		String info = null;
   		if(hashMap.containsKey(dir_code)){
   			info = "clouddisk_cooperate.jsp";
   		}else{
       		Leaf leafParent = new Leaf(dir_code);
       		String parentCode = leafParent.getParentCode();
       		int parentDocId = new Leaf(parentCode).getDocId();
       		info = "clouddisk_cooperate.jsp?op=cooperateChildFile&dir_code="+parentCode+"&doc_id="+parentDocId;
   	   }
    %>
  
    <%}%>
 <%
 	if(cooperateChildLeafIr!=null){
 		while(cooperateChildLeafIr.hasNext()){
 			Leaf leafCooperateChild = (Leaf)cooperateChildLeafIr.next();
 			if (rootCode.equals("")) {
 				rootCode = leafCooperateChild.getCode();
 			}
 			String leafUrl = "clouddisk_cooperate.jsp?op=cooperateChildFile&dir_code="+leafCooperateChild.getCode()+"&doc_id="+leafCooperateChild.getDocId() + "&root_code=" + rootCode;
 		%>
 	<dl>
	  <dd class="fileGroup" >	
	  	<div class="fileNameDetail">
	  		<div class="file_action" >
               <ul>
                 <li><a target="_blank" href="clouddisk_downloaddir.jsp?code=<%=leafCooperateChild.getCode() %>"><img src="images/clouddisk/download_1.gif" title="下载"/></a></li>
               </ul>
             </div>
              <span class="cbox_icon"></span>
			  <input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" />
			  <img  src="images/sort/folder.png"   class="extImg"/>
			  <span class="fname"><a href="<%=leafUrl %>" ><%=leafCooperateChild.getName() %></a></span>      
		</div>
	      <div class="col">
	         <span></span>
	       </div>
	       <div class="col">
	         <span>文件夹</span>
	       </div>
	       <div class="col">
	         <span></span>
	       </div>
	    </dd>
	  </dl>	
 	<%}}
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
      				<span class="fname"><a href="javascript:editdoc('<%=attach.getDocId()%>', '<%=attach.getId()%>', '<%=attach.getExt() %>')" ><%=attach.getName() %></a></span>      
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
 	<%		
 		}}
 	%>
 	
<%}else{ %>
  <% 
 	while(ri.hasNext()){
 		ResultRecord record = (ResultRecord)ri.next();
 		String name = record.getString("name");
 		String shareUser = record.getString("share_user");
 		Date cooperateDate = record.getDate("cooperate_date");
 		String dirCodeLog = record.getString("dir_code");
 		UserDb userDb = new UserDb();
 		userDb = userDb.getUserDb(shareUser);
 		String shareUserRealName = "";
 		String dirCode = record.getString("dir_code");//文件夹code
 		String docId = record.getString("doc_id");//文件附件doc_id
 		if(shareUser.equals(userName)){
 			shareUserRealName = "我";
 		}else{
 			shareUserRealName = userDb.getRealName();
 		}
 		String cooperateDate2 = DateUtil.format(cooperateDate,"yyyy-MM-dd HH:mm");
 		String url = "clouddisk_cooperate.jsp?op=cooperateChildFile&dir_code="+dirCode+"&doc_id="+docId;
 		
 		
 %>
	<dl>
	  <dd class="fileGroup" >	
	  	<div class="fileNameDetail">
	  		<div class="file_action" >
                <ul>
                  <li><a target="_blank" href="clouddisk_downloaddir.jsp?code=<%=dirCode %>"><img src="images/clouddisk/download_1.gif" title="下载"/></a></li>
                </ul>
	         </div>	
	      <span class="cbox_icon"></span>
		 <input class="cbox"  name="floder_ids" style="display: none;" type="checkbox" />
		 <img  src="images/sort/folder.png"  class="extImg" />
		 <span class="fname"><a href="<%=url%>" ><%=name %></a></span>      
		</div>
	      <div class="col">
	         <span><%=shareUserRealName %></span>
	       </div>
	       <div class="col">
	         <span><%=cooperateDate2 %></span>
	       </div>
	       <div class="col cooperateCol">
	       <img  src="images/cooperate_change.png" dir_code="<%=dirCodeLog%>" class="cooperateChange" />
	       </div>
	    </dd>
	  </dl>
 	<% }%>
 <%}%>
</div>
</div>
</body>
</html>
