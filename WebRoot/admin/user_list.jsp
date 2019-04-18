<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.base.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.netdisk.UtilTools"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@page import="com.redmoon.oa.video.VideoMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals("")) skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String searchUnitCode = ParamUtil.get(request, "searchUnitCode");
String unitCode = searchUnitCode;
if (unitCode.equals(""))
	unitCode = privilege.getUserUnitCode(request);
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户管理</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<style>
  .unit {
	  background-color:#CCC;
  }
</style>
<script language="JScript.Encode" src="../js/browinfo.js"></script>				
<script language="JScript.Encode" src="../js/rtxint.js"></script>
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<body>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
boolean is_bind_mobile = cfg.getBooleanProperty("is_bind_mobile");

String op = StrUtil.getNullString(request.getParameter("op"));
String type = ParamUtil.get(request, "type");
int curpage	= ParamUtil.getInt(request, "CPages", 1);
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
VideoMgr vmgr = new VideoMgr();                             
String content = ParamUtil.get(request,"content");
content = content.replaceAll("，", ",");
String condition = ParamUtil.get(request,"condition");
if (condition.equals(""))
	condition = "realname";

String querystr = "op=" + op + "&pageSize=" + pagesize + "&condition=" + condition + "&content=" + StrUtil.UrlEncode(content) + "&type=" + type + "CPages=" + curpage;

if (op.equals("del")) {
	UserMgr um = new UserMgr();
	String userName = ParamUtil.get(request, "userName");
	if (um.del(request)) {
		if (vmgr.validate()){
		%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		vmgr.delUser(userName);                                   //删除视频会议用户
		}
		//删除成功之后要更新表user_recently_selected，此表用来记录最近选择的用户
		String delSql = "delete from user_recently_selected where name = "+ StrUtil.sqlstr(userName)+" or userName = "+ StrUtil.sqlstr(userName);
		JdbcTemplate jt = new JdbcTemplate();
		try {
			jt.executeUpdate(delSql);
		} catch (Exception e) {
		} finally {
			jt.close();
		}
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_list.jsp?CPages=" + curpage + "&pageSize=" + pagesize));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
else if (op.equals("delBatch")) {
	String[] ary = StrUtil.split(ParamUtil.get(request, "ids"), ",");
	if (ary!=null) {
		UserMgr um = new UserMgr();
		%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		for (int i=0; i<ary.length; i++) {
			um.del(ary[i]);
		}
		if(vmgr.validate()) 
			vmgr.delUserByArr(ary);            //校验成功，删除视频会议用户  
			%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_list.jsp?pageSize=" + pagesize));
		return;
	}
}
else if (op.equals("changeDept")) {
	String[] ary = StrUtil.split(ParamUtil.get(request, "ids"), ",");
	if (ary!=null) {
		String deptCode = ParamUtil.get(request, "deptCode");
		DeptUserMgr dum = new DeptUserMgr();
		%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		for (int i=0; i<ary.length; i++) {
			dum.changeDeptOfUser(ary[i], deptCode, privilege.getUser(request));
		}
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_list.jsp?CPages=" + curpage + "&pageSize=" + pagesize));
		return;
	}
}else if(op.equals("leaveOffice")){
	String userName = ParamUtil.get(request,"name");
	UserMgr um = new UserMgr();
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	boolean isSuccess = um.leaveOffice(userName, privilege.getUser(request));
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if(isSuccess)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_list.jsp?CPages=" + curpage + "&pageSize=" + pagesize));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}else if(op.equals("leaveOffBatch")){
	UserMgr um = new UserMgr();
	try{%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		String[] ary = StrUtil.split(ParamUtil.get(request, "ids"), ",");
		UserDb user = new UserDb();
		if (ary!=null) {
			for (int i=0; i<ary.length; i++) {
				um.leaveOffice(ary[i], privilege.getUser(request));
			}
		}
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_list.jsp?CPages=" + curpage + "&pageSize=" + pagesize));
	}catch(ErrMsgException e){
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	return;
}else if(op.equals("bind")){ //绑定手机 
	UserSetupDb usd = new UserSetupDb();
	String userName = ParamUtil.get(request,"userName");
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	usd = usd.getUserSetupDb(userName);
	usd.setBindMobile(true);
	boolean flag = usd.save();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if(flag){
		out.print(StrUtil.jAlert_Redirect("已成功绑定员工账号！","提示", "user_list.jsp?CPages=" + curpage + "&pageSize=" + pagesize));
	}else{
		out.print(StrUtil.jAlert_Back("绑定失败！","提示"));
	}
	
}else if(op.equals("unbind")){ //解除绑定
	UserSetupMgr usm = new UserSetupMgr();
	String userName = ParamUtil.get(request,"userName");
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	boolean flag = usm.unbindMoible(userName);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if(flag){
		out.print(StrUtil.jAlert_Redirect("已成功解除绑定！","提示", "user_list.jsp?CPages=" + curpage + "&pageSize=" + pagesize));
	}else{
		out.print(StrUtil.jAlert_Back("解绑失败！","提示"));
	}
}
%>
<%@ include file="user_list_inc_menu_top.jsp"%>
<script>
<%if (type.equals("notvalid")) {%>
o("menu2").className="current";
<%}else{%>
o("menu1").className="current";
<%}%> 
</script>
<div class="spacerH"></div>
<form name="form1" method="post" action="user_list.jsp?op=search">
          <table width="90%" border="0" align="center">
            <tr>
              <td height="25" align="center">
              单位
              <select id="searchUnitCode" name="searchUnitCode">
              <option value="">不限</option>
              <%
			  if (License.getInstance().isGroup() || License.getInstance().isPlatform()) {
				  DeptDb dd = new DeptDb();
				  DeptView dv = new DeptView(request, dd);
				  StringBuffer sb = new StringBuffer();
				  dd = dd.getDeptDb(privilege.getUserUnitCode(request));
				  if (dd!=null) {
				  %>
				  <%=dv.getUnitAsOptions(sb, dd, dd.getLayer())%>
				  <%
				  }
			  }
			  %>
              </select>
              <script>
			  o("searchUnitCode").value = "<%=searchUnitCode%>";
			  </script>
              用户
                <input type="text" name="content" style="height:18px;width:100px" title="输入用户名或真实姓名时，可以用逗号分隔，一次查询多个用户" value="<%=content%>">
                &nbsp;&nbsp;
                <select name="condition">
                  <option value="realname" selected>真实姓名</option>
                  <option value="nick">用户名</option>
                </select>
				<script>
				form1.condition.value = "<%=condition%>";
				</script>				
                &nbsp;&nbsp;
                <input name="type" type="hidden" value="<%=type%>" />
				每页
				<input name="pageSize" value="<%=pagesize%>" style="width:25px" size="2" />
				&nbsp;条
            <input class="btn" type="submit" name="Submit" value="查找">
               	  &nbsp;&nbsp;
<input class="btn" type="button" name="export" value="导出用户" onclick="exportAll()">                </td>
            </tr>
          </table>
</form>	
        <%
		String sql = "select name from users where isvalid=1 and name<>'system'";
		if (type.equals("notvalid"))
			sql = "select name from users where isvalid=0 and name<>'system'";
		if(op.equals("search")) {
		    if(condition.equals("realname")) {
			   String[] ary = StrUtil.split(content, ",");
			   if (ary!=null) {
				   if (ary.length==1) {
						sql += " and realname like "+StrUtil.sqlstr("%" + content + "%");
				   }
				   else {
					   String cond = "";
					   for (int i=0; i<ary.length; i++) {
						if (cond.equals(""))
							cond = StrUtil.sqlstr(ary[i]);
						else
							cond += "," + StrUtil.sqlstr(ary[i]);
					   }
					   sql += " and realname in (" + cond + ")";
				   }
			   }
			}
			else {
			   String[] ary = StrUtil.split(content, ",");
			   if (ary!=null) {
				   if (ary.length==1) {
					   sql += " and name like "+ StrUtil.sqlstr("%" + content + "%");
				   }
				   else {
					   String cond = "";
					   for (int i=0; i<ary.length; i++) {
						if (cond.equals(""))
							cond = StrUtil.sqlstr(ary[i]);
						else
							cond += "," + StrUtil.sqlstr(ary[i]);
					   }
					   sql += " and name in (" + cond + ")";
				   }
			   }
			}
		    querystr += "&content=" + StrUtil.UrlEncode(content) + "&condition=" + condition + "&searchUnitCode=" + ParamUtil.get(request, "searchUnitCode");
		}
		
		if (!searchUnitCode.equals("")) {
			sql += " and unit_code=" + StrUtil.sqlstr(searchUnitCode);
		}
		else {
			boolean isAdmin = privilege.isUserPrivValid(request, "admin.user") && unitCode.equals(DeptDb.ROOTCODE);
			if (!isAdmin)
				sql += " and unit_code=" + StrUtil.sqlstr(unitCode);
		}
		
		sql += " order by regDate desc";
		
		// out.print(sql);
		
		UserDb userdb = new UserDb();
	    int total = userdb.getUserCount(sql);
		int totalpages;
		Paginator paginator = new Paginator(request, total, pagesize);
        //设置当前页数和总页数
	    totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
		boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
%>
        <table border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
          <tr>
            <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
          </tr>
</table>
        <table id="mainTable" width="99%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1">
          <thead>
          <tr align="center">
            <td width="3%" class="tabStyle_1_title"><span class="right-title">
              <input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" />
            </span></td>
            <td width="6%" class="tabStyle_1_title">工号</td>
            <td width="9%" class="tabStyle_1_title">用户名</td>
            <td width="8%" class="tabStyle_1_title">真实姓名</td>
            <td width="4%" class="tabStyle_1_title">性别</td>
            <td width="12%" class="tabStyle_1_title">云盘空间</td>
            <td width="13%" class="tabStyle_1_title">内部邮箱空间</td>
            <td width="18%" class="tabStyle_1_title">所属部门</td>
            <td width="18%" class="tabStyle_1_title">角色</td>
            <td width="9%" class="tabStyle_1_title">操作</td>
          </tr>  </thead>      <%
	  	// Vector v = userdb.list(sql, (curpage-1)*pagesize, curpage*pagesize);
		// Iterator ir = v.iterator();
		int start = (curpage-1)*pagesize;
		int end = curpage*pagesize;
		
		DeptMgr dm = new DeptMgr();		
		DeptUserDb du = new DeptUserDb();
        ObjectBlockIterator ir = userdb.getObjects(sql, start, end);		
		int i = 0;
		AccountDb acc2 = new AccountDb();
		UserSetupDb usd = new UserSetupDb();
		while (ir.hasNext()) {
			i++;
			UserDb user = (UserDb)ir.next();
			AccountDb acc = acc2.getUserAccount(user.getName());
			usd = usd.getUserSetupDb(user.getName());
		%>
          <tr align="left">
            <td width="3%" align="center">
            <%if (!user.getName().equals("admin") && !user.getName().equals("system")) {%>
            <input type="checkbox" id="ids" name="ids" value="<%=user.getName()%>" />
            <%}%>
            </td>
            <td width="6%" align="left">
			<%if (acc!=null && acc.isLoaded()) {
				out.print(acc.getName());
			}%></td>
            <td width="9%" height="22" align="left">
			&nbsp;
			<%if (isRTXUsed) {%>
			<img align="absbottom" width=16 height=16 src="../images/blank.gif" onload="RAP('<%=user.getName()%>');"> 
			<%}%>
			<a href="user_edit.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getName()%></a></td>
            <td width="8%" align="left"><a href="user_edit.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getRealName()%></a></td>
            <td width="4%" align="center">
			<%=user.getGender()==0?"男":"女"%>
            </td>
            <td width="12%" align="left">
			<%=UtilTools.getFileSize(user.getDiskSpaceAllowed())%>，已用<%=UtilTools.getFileSize(user.getDiskSpaceUsed())%>		</td>
            <td width="13%" align="left"><%=UtilTools.getFileSize(usd.getMsgSpaceAllowed())%>，已用<%=UtilTools.getFileSize(usd.getMsgSpaceUsed())%></td>
            <td width="18%" align="left"><%
			Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
			int k = 0;
			while (ir2.hasNext()) {
				DeptDb dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {					
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
				}
				else
					deptName = dd.getName();
				if (k==0) {
					out.print("<a href='#' onClick=\"openWin('dept_user.jsp?deptCode=" + StrUtil.UrlEncode(dd.getCode()) + "', 620, 420)\">" + deptName + "</a>");
				}
				else {
					out.print("，&nbsp;" + "<a href='#' onClick=\"openWin('dept_user.jsp?deptCode=" + StrUtil.UrlEncode(dd.getCode()) + "', 620, 420)\">" + deptName + "</a>");
				}
				k++;
			} 
			%></td>
            <td align="left"><%
com.redmoon.oa.pvg.RoleDb[] rld = user.getRoles();
int rolelen = 0;
if (rld!=null)
	rolelen = rld.length;
String roleDescs = "";
for (int m=0; m<rolelen; m++) {
	if (rld[m]==null)
		continue;
	if (rld[m].getCode().equals(com.redmoon.oa.pvg.RoleDb.CODE_MEMBER)) {
		continue;
	}

	if (roleDescs.equals("")) {
		roleDescs = StrUtil.getNullStr(rld[m].getDesc());
	}
	else {
		roleDescs += "，" + StrUtil.getNullStr(rld[m].getDesc());
	}
	
	
}
out.print(roleDescs);
%></td>
            <td width="9%" align="center">
            <%if (!user.getName().equals("system")) {%>
            <a href="javascript:;" onclick="addTab('<%=user.getRealName()%> 信息', '<%=request.getContextPath()%>/admin/user_edit.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>')">编辑</a>&nbsp;&nbsp;
            <%if (false && user.isValid()) {%>
            <a href="javascript:;" onclick="addTab('<%=user.getRealName()%> 权限', '<%=request.getContextPath()%>/admin/user_op.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>')">权限</a>&nbsp;&nbsp;
            <a href="javascript:;" onclick="addTab('<%=user.getRealName()%> 部门', '<%=request.getContextPath()%>/admin/user_dept_modify.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')">部门</a>&nbsp;&nbsp;
            <%}%>
        	<%if(false && !op.equals("notvalid")){%>
				<%if (!user.getName().equals("admin")) {%>
            	<a href="javascript:;" onclick="jConfirm('您确定要操作离职么，离职将删除用户相关的权限？','提示',function(r){if(!r){return;}else{window.location.href='user_list.jsp?op=leaveOffice&name=<%=StrUtil.UrlEncode(user.getName())%>&pageSize=<%=pagesize%>&CPages=<%=curpage%>'}}) " style="cursor:pointer">离职</a>	&nbsp;&nbsp;
           		<%}%>
            <%}%>
            <!--<a href="user_post.jsp">职位</a>-->
            <%if (false && !user.getName().equals("admin")) {%>
            <a href="#" onClick="jConfirm('您确定要删除吗？删除用户将同时清除论坛帐号！\n请确认该用户是否已使用过系统，否则可能带来未知问题！','提示',function(r){ if(!r){return;}else{window.location.href='?op=del&userName=<%=StrUtil.UrlEncode(user.getName())%>&<%=querystr%>'}}) " style="cursor:pointer">删除</a>
            <%}}%>
             <% 
             	if(is_bind_mobile){
            		if(usd.isBindMobile()){
              %>
             <a title="解除绑定后即可更换手机登录" href="javascript:;" onClick="jConfirm('解除绑定后即可更换手机登录，您确认要解除绑定么？','提示',function(r){if(!r){return;}else{window.location.href='?op=unbind&userName=<%=StrUtil.UrlEncode(user.getName())%>&CPages=<%=curpage%>&pageSize=<%=pagesize %>'}}) " style="cursor:pointer">解绑</a>
             	<%}else{ %>
             <a title="绑定手机后仅允许首次登录的手机使用" href="javascript:;" onClick="jConfirm('绑定手机后仅允许首次登录的手机使用，您确定要绑定么？','提示',function(r){if(!r){return;}else{window.location.href='?op=bind&userName=<%=StrUtil.UrlEncode(user.getName())%>&CPages=<%=curpage%>&pageSize=<%=pagesize %>'}}) " style="cursor:pointer">绑定</a>
          		 <%}
            	}%>
            </td>
          </tr>
        <%
	}
%>
</table>
        <table class="percent98" width="92%" border="0" cellspacing="1" cellpadding="3" align="center">
          <tr>
            <td width="41%" height="23" align="left">
			<input class="btn" style="margin-left:3px" name="button2" type="button" onclick="delBatch()" value="删除" />
            <%if(!type.equals("notvalid")){%>            
			&nbsp;
			<input class="btn" type="button" value="调动" onclick="changeDept()" />
            &nbsp;
            <input class="btn" type="button" value="离职" onclick="leaveOffice()" />
            <%}%>
			</td>
            <td width="59%" align="right"><%
    out.print(paginator.getCurPageBlock("user_list.jsp?"+querystr));
%></td>
          </tr>
        </table>
<form name="hidForm" action="" method="post">
<input name="op" type="hidden" />
<input name="CPages" value="<%=curpage%>" type="hidden" />
<input name="pageSize" value="<%=pagesize%>" type="hidden" />
<input name="ids" type="hidden" />
</form>

<form name="changeDeptForm" action="" method="post">
<input name="op" type="hidden" />
<input name="deptCode" value="1" type="hidden" />
<input name="ids" type="hidden" />
<input name="CPages" value="<%=curpage%>" type="hidden" />
<input name="pageSize" value="<%=pagesize%>" type="hidden" />
</form>
<form name="leaveOfficeForm" action="" method="post">
<input name="op" type="hidden" />
<input name="ids" type="hidden" />
<input name="CPages" value="<%=curpage%>" type="hidden" />
<input name="pageSize" value="<%=pagesize%>" type="hidden" />
</form>
</body>
<script>
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
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

function delBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择用户！","提示");
		return;
	}
	jConfirm("您确定要删除么？","提示",function(r){
		if(!r){return;}
		else{
			hidForm.action = "user_list.jsp";
			hidForm.op.value = "delBatch";
			hidForm.ids.value = ids;
			hidForm.submit();
		}
	})
}

function selectNode(code, name) {
	// if (!confirm("您确定要调动么？"))
	//	return;
	jConfirm("您确定要选择 " + name + " 么？","提示",function(r){
		if(!r){return;}
		else{
		var ids = getCheckboxValue("ids");
		changeDeptForm.action = "user_list.jsp";
		changeDeptForm.op.value = "changeDept";
		changeDeptForm.deptCode.value = code;
		changeDeptForm.ids.value = ids;
		changeDeptForm.submit();
		}
	})
}

function changeDept() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择要调动的用户！","提示");
		return;
	}
	openWin("../dept_sel.jsp", 450, 400, "yes");
}

function leaveOffice() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择要离职的用户！","提示");
		return;
	}
	jConfirm("您确定要操作离职么，离职将删除用户相关的权限？","提示",function(r){
		if(!r){return;}
		else{
			leaveOfficeForm.action = "user_list.jsp";
			leaveOfficeForm.op.value = "leaveOffBatch";
			leaveOfficeForm.ids.value = ids;
			leaveOfficeForm.submit();
		}
	})
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

function exportAll() {
	window.open("user_excel.jsp");
}
</script>
</html>