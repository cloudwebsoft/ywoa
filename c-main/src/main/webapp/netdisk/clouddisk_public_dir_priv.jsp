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
<%@ page import="com.redmoon.clouddisk.*"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>

<%@page import="java.text.DecimalFormat"%>
<%@page import="cn.js.fan.util.file.FileUtil"%>

<html>
	<head>
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>网盘————我的公共共享</title>
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet" href="css/reset.css" />
		<link type="text/css" rel="stylesheet" href="<%=Global.getRootPath(request) %>/netdisk/clouddisk.css" />
		<link type="text/css" rel="stylesheet"href="<%=Global.getRootPath(request) %>/netdisk/showDialog/showDialog.css" />
		
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
		<script type="text/javascript" src="../js/jquery.toaster.netdisk.js"></script> 
		<script src= "js/clouddisk_public_dir_priv.js"></script>
		<script type="text/javascript" src="../js/goToTop/goToTop.js"></script>
		<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css" />
		<jsp:useBean id="leafPriv" scope="page" class="com.redmoon.oa.netdisk.PublicLeafPriv"/>
		<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
		<script type="text/javascript">
				 document.onmousemove = function () {
				 var divx = window.event.clientX+"px";
				 var divy = window.event.clientY+"px";
				 }
				 $(function(){
					//回到顶部
						$(window).goToTop({
							showHeight : 1,//设置滚动高度时显示
							speed : 500 //返回顶部的速度以毫秒为单位
						});
								
					 })
		</script>
	<%
		if (!privilege.isUserLogin(request)) {
			out.print("对不起，请先登录！");
			 return;
		}
		String priv="read";
		System.out.print(privilege.isUserPrivValid(request,priv));
		if (!privilege.isUserPrivValid(request,priv)) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
		String dir_code = ParamUtil.get(request, "dirCode");
		leafPriv.setDirCode(dir_code);
		if (!(leafPriv.canUserManage(privilege.getUser(request)))) {
			out.print(StrUtil.jAlert_Back(privilege.MSG_INVALID + " 用户对节点没有管理权限！","提示"));
			return;
		}
		PublicLeaf publicLeaf = new PublicLeaf();
		publicLeaf = publicLeaf.getLeaf(dir_code);
		String mappingAddress = publicLeaf.getMappingAddress();
		String op = ParamUtil.get(request, "op");

		if (op.equals("add")) {
			String name = ParamUtil.get(request, "name");
			if (name.equals("")) {
				out.print(StrUtil.Alert_Back("名称不能为空！"));
				return;
			}
			int type = ParamUtil.getInt(request, "type");
			String[] names = name.split("\\,");
			boolean re = false;
			for (String um : names) {
				if (type == PublicLeafPriv.TYPE_USER) {
					UserDb user = new UserDb();
					user = user.getUserDb(um);
					if (!user.isLoaded()) {
						continue;
					}
				}
				try {
					re = leafPriv.add(um, type);
				} catch (ErrMsgException e) {
					out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
					return;
				}
			}
			if (re) {
				out.print(StrUtil.jAlert_Redirect("添加成功", "提示", "clouddisk_public_dir_priv.jsp?dirCode=" + StrUtil.UrlEncode(dir_code)));
			} else {
				out.print(StrUtil.jAlert_Back("操作失败", "提示"));
			}
			return;
		}else if (op.equals("setrole")) {
			try {
				String roleCodes = ParamUtil.get(request, "roleCodes");
				String leafCode = ParamUtil.get(request, "dirCode");
				PublicLeafPriv lp = new PublicLeafPriv(leafCode);
				lp.setRoles(leafCode, roleCodes);
				out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "clouddisk_public_dir_priv.jsp?dirCode=" + StrUtil.UrlEncode(dir_code)));
			}
			catch (Exception e) {
				out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			}
			return;
		}
		
		
%>
<script>
	$(function(){
		$(".privManage").mouseover(function(){
			$(this).css("background","url(images/clouddisk/priv2.gif)");
		}).mouseleave(function(){
			$(this).css("background","url(images/clouddisk/priv1.gif)");
		})
	})
</script>

</head>

<body>

<div id="Right" class="Right">
	  <div class="rHead">
	  	<div class="fixedDivPublic">
		<div class="rHead1" style="position: relative">
			<div class="privManage addPriv"></div>
			<input type="hidden" name="urlDirCode" class="urlDirCode" value="<%=StrUtil.UrlEncode(dir_code) %>" /> 
		</div>
		<div class="rHead2" style="background: #f7f7f7; z-index: 20">
		<%--
			String showPath = "";
			boolean backFlag = true;
			if (dir_code.equals(PublicLeaf.ROOTCODE)) {
				backFlag = false;
				showPath = "<a href='clouddisk_pubilc_share.jsp'>公共共享文件</a>";
			} else if (publicLeaf.getParentCode().equals(PublicLeaf.ROOTCODE)) {
				showPath = "<a href='clouddisk_pubilc_share.jsp'>公共共享文件</a>    >>    <a href='clouddisk_public_dir_priv.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
			} else {
				PublicLeaf pleaf = new PublicLeaf(publicLeaf.getParentCode());
				if (publicLeaf.getLayer() == 3) {
					showPath = "<a href='clouddisk_pubilc_share.jsp'>公共共享文件</a>    >>    <a href='clouddisk_public_dir_priv.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_public_dir_priv.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
					
				} else {
					showPath = "<a href='clouddisk_pubilc_share.jsp?'>公共共享文件</a>    >>    ...>>    <a href='clouddisk_public_dir_priv.jsp?dir_code=" + StrUtil.UrlEncode(pleaf.getCode()) + "'>" + pleaf.getName() + "</a>    >>    <a href='clouddisk_public_dir_priv.jsp?dir_code=" + StrUtil.UrlEncode(dir_code) + "'>" + publicLeaf.getName() + "</a>";
				}
			}
		 --%>
		 	<div style="float:left;margin-left:15px;margin-top:4px;">
		 		<a href="../admin/netdisk_public_dir_frame.jsp?root_code=<%=dir_code%>">
		 			<img src="images/clouddisk/back.png"/>
		 		</a>
		 	</div>
			 <div class='all_file'>
			 	<a href='../admin/netdisk_public_dir_frame.jsp?root_code=<%=dir_code %>'><%=publicLeaf.getName() %></a>
			 </div>
		</div>
	</div>
	<div class="containtCenterPriv">
		<dl class="privPdir">
			<dd >
				<div class="privUser">
					<span>用户</span>
				</div>
				<div class="privCate privCol">
					<span >类型</span>
				</div>
				<div class="privDetail privCol">
					<span >权限</span>
				</div>
				<div class="privOption privCol" >
					<span >操作</span>
				</div>
			</dd>
			<% 
				Vector vec = leafPriv.list();
				Iterator it = null;
				if( vec != null && vec.size()>0){
					it = vec.iterator();
					while( it.hasNext()){
						PublicLeafPriv lp = (PublicLeafPriv)it.next();
						String privUser = "";
						String backUrlUncheck = "url('images/clouddisk/checkbox_1.png')";
						String backUrlCheck = "url('images/clouddisk/checkbox_3.png')";
			
						 if (lp.getType()==lp.TYPE_USER) {
						  	UserDb ud = new UserDb();
							ud = ud.getUserDb(lp.getName());
							privUser = ud.getRealName();
						  }else if (lp.getType()==lp.TYPE_ROLE) {
						    RoleDb rd = new RoleDb();
							rd = rd.getRoleDb(lp.getName());
							privUser = rd.getDesc();
						  }else if (lp.getType()==lp.TYPE_USERGROUP) {
						  	UserGroupDb ug = new UserGroupDb();
							ug = ug.getUserGroupDb(lp.getName());
							privUser = ug.getDesc();
						  }
				%>
			<form id="privForm<%=lp.getId() %>" method=post>
			<dd  id="<%=lp.getId() %>">
				<div class="privUser">
					<span><%=privUser %></span>
				</div>
				<div class="privCate privColContent">
					<span ><%=lp.getTypeDesc() %></span>
				</div>
				<input type="hidden" name="dirCode" value="<%=dir_code %>" /> 
				<div class="privDetailContent privColContent">
					
					<span class="see privOp">
						<input name=see type=checkbox  value="<%=lp.getSee()==1?1:0%>"  <%=lp.getSee()==1?"checked='checked'":""%>    class="cboxPriv"/>
						<span class="cboxIcon" style="background:<%=lp.getSee()==1?backUrlCheck:backUrlUncheck%> "></span> 
						<span class="opText">浏览</span>
					</span>
					<% 
						if(mappingAddress.equals("")){
					%>
					
					<span class="append privOp">
						<input name=append type=checkbox  value="<%=lp.getAppend()==1?1:0%>"  <%=lp.getAppend()==1?"checked='checked'":""%>   class="cboxPriv"/>
						<span class="cboxIcon" style="background:<%=lp.getAppend()==1?backUrlCheck:backUrlUncheck%> "></span> 
						<span class="opText">添加</span>
					</span>
					<!--  
					<span class="del privOp" style="display:none;">
						<input name=del type=checkbox  value="<%=lp.getDel()==1?1:0%>"  <%=lp.getDel()==1?"checked='checked'":""%> class="cboxPriv"/>
						<span class="cboxIcon"   style="background:<%=lp.getDel()==1?backUrlCheck:backUrlUncheck%> "></span> 
						<span class="opText">删除</span>
					</span>
					<span class="modify privOp" style="display:none;">
						<input name=modify type=checkbox   value="<%=lp.getModify()==1?1:0%>"  <%=lp.getModify()==1?"checked='checked'":""%>  class="cboxPriv" />
						<span class="cboxIcon" style="background:<%=lp.getModify()==1?backUrlCheck:backUrlUncheck%> "></span> 
						<span class="opText">修改</span>
					</span>
					-->
					<span class="examine privOp">
						<input name=examine type=checkbox  value="<%=lp.getExamine()==1?1:0%>" class="cboxPriv"  <%=lp.getExamine()== 1?"checked='checked'":""%>/>
						<span class="cboxIcon" style="background:<%=lp.getExamine()==1?backUrlCheck:backUrlUncheck%> "></span> 
						<span class="opText">管理</span>
					</span>
					<% }%>
				</div>
				<div class="privOptionContent privColContent" >
					<a href="#" class="modifyOp" ><span>[修改]</span></a>
					<a href="#" class="delOp" ><span>[删除]</span></a>
				</div>
			</dd>
			</form>
			<%
					}
				}
			%>
		</dl>
</div>

</html>
