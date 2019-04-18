<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<jsp:useBean id="privmgr" scope="page" class="com.redmoon.oa.pvg.PrivMgr"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>设置用户权限</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
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
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='<%=request.getContextPath() %>/images/loading.gif'></div>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
boolean re = false;
String op = ParamUtil.get(request, "op");
boolean isEdit = false;
String name = ParamUtil.get(request, "name");
UserDb user = new UserDb();
if (!name.equals(""))
	user = user.getUserDb(name);
else {
	int userId = ParamUtil.getInt(request, "userId", -1);
	if (userId!=-1) {
		user = user.getUserDb(userId);
	}
	else {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
		return;
	}
}
if (op.equals("edit")) {
	isEdit = true;
	name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		StrUtil.jAlert_Back("用户名不能为空！","提示");
		return;
	}
}
else if (op.equals("setuserofgroup")) {
	isEdit = true;
	name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		out.print(StrUtil.jAlert_Back("用户名不能为空！","提示"));
		return;
	}
	UserMgr usermgr = new UserMgr();
	user = usermgr.getUserDb(name);
	// System.out.println("user=" + user.getName());
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = user.setGroups(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		if (re)
			out.print(StrUtil.jAlert_Redirect("修改用户组成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
		else
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (op.equals("setuserofrole")) {
	isEdit = true;
	name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		out.print(StrUtil.jAlert_Back("用户名不能为空！","提示"));
		return;
	}
	UserMgr usermgr = new UserMgr();
	user = usermgr.getUserDb(name);
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = user.setRoles(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		if (re)
			out.print(StrUtil.jAlert_Redirect("修改用户角色成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
		else
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));			
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (op.equals("setprivs")) {
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		String username = ParamUtil.get(request, "name");
		user = user.getUserDb(username);
		re = user.setPrivs(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		if (re)
			out.print(StrUtil.jAlert_Redirect("修改用户权限成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
		else
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));			
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (op.equals("modifyLeafPriv")) {
	int id = ParamUtil.getInt(request, "id");
	int see = 0, append=0, del=0, modify=0, examine=0;
	String strsee = ParamUtil.get(request, "see");
	if (StrUtil.isNumeric(strsee)) {
		see = Integer.parseInt(strsee);
	}
	String strappend = ParamUtil.get(request, "append");
	if (StrUtil.isNumeric(strappend)) {
		append = Integer.parseInt(strappend);
	}
	String strmodify = ParamUtil.get(request, "modify");
	if (StrUtil.isNumeric(strmodify)) {
		modify = Integer.parseInt(strmodify);
	}
	String strdel = ParamUtil.get(request, "del");
	if (StrUtil.isNumeric(strdel)) {
		del = Integer.parseInt(strdel);
	}
	String strexamine = ParamUtil.get(request, "examine");
	if (StrUtil.isNumeric(strexamine)) {
		examine = Integer.parseInt(strexamine);
	}
	
	LeafPriv leafPriv = new LeafPriv();
	leafPriv.setId(id);
	leafPriv.setAppend(append);
	leafPriv.setModify(modify);
	leafPriv.setDel(del);
	leafPriv.setSee(see);
	leafPriv.setExamine(examine);
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	re = leafPriv.save();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;			
}
else if (op.equals("delFlowLeafPriv")) {
	int id = ParamUtil.getInt(request, "id");
	com.redmoon.oa.flow.LeafPriv lp = new com.redmoon.oa.flow.LeafPriv();
	lp = lp.getLeafPriv(id);
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	re = lp.del();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
else if (op.equals("delLeafPriv")) {
	int id = ParamUtil.getInt(request, "id");
	LeafPriv lp = new LeafPriv();
	lp = lp.getLeafPriv(id);
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	re = lp.del();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
else if (op.equals("setAdminDept")) {
	
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = user.setAdminDepts(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}

UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(name);

if (op.equals("setMessage")) {
	String depts = ParamUtil.get(request, "depts");
	String userGroups = ParamUtil.get(request, "userGroups");
	String userRoles = ParamUtil.get(request, "userRoles");
	int messageToMaxUser = ParamUtil.getInt(request, "messageToMaxUser", -1);
	if (messageToMaxUser<=-1) {
		out.print(StrUtil.jAlert_Back("短消息群发的最大用户数需为大于0的整数！","提示"));
		return;
	}
	int messageUserMaxCount = ParamUtil.getInt(request, "messageUserMaxCount", -1);
	if (messageUserMaxCount<=-1) {
		out.print(StrUtil.jAlert_Back("短消息信箱容量需为大于0的整数！","提示"));
		return;
	}
	
	usd.setMessageToDept(depts);
	usd.setMessageToUserGroup(userGroups);
	usd.setMessageToUserRole(userRoles);
	usd.setMessageToMaxUser(messageToMaxUser);
	usd.setMessageUserMaxCount(messageUserMaxCount);
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	re = usd.save();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_privilege.jsp?name=" + StrUtil.UrlEncode(name)));
	else
		out.print(StrUtil.jAlert("操作失败！","提示"));
	return;
}
%>
<script>
</script>
<div class="spacerH"></div>
<TABLE cellSpacing=0 class="tabStyle_1" cellPadding=3 width="95%" align=center>
  <TBODY>
    <TR>
      <TD class="tabStyle_1_title">
	  <%if (user!=null) {%>
	  	  用户 <%=user.getRealName()%> 的权限
	  <%}%>	  </TD>
    </TR>
    <TR>
      <TD height="175" align="center">
        <%if (user!=null) {%>
        <table style="display:" class="tabStyle_1_subTab percent80" cellspacing="0" cellpadding="3" align="center">
          <form name="formRole" method="post" action="?op=setuserofrole">
            <tbody>
              <tr>
                <td align="left" nowrap class="tabStyle_1_subTab_title">设置角色</td>
              </tr>
              <%
RoleMgr roleMgr = new RoleMgr();		  
RoleDb[] userroles = user.getRolesRaw();
int ulen = 0;
if (userroles!=null)
	ulen = userroles.length;

String roleCode, desc;
String roleCodes = "";
String descs = "";

for (int i=0; i<ulen; i++) {
	RoleDb rd = userroles[i];
	roleCode = rd.getCode();
	desc = rd.getDesc();
	//因为“全部用户”是数组最后一个，如果前面有“全部用户”，数组最后一个就不用加载
    if(i == ulen-1){
        if(descs.indexOf("全部用户") != -1){
            break;
        }
    }
	if (roleCodes.equals(""))
		roleCodes += roleCode;
	else
		roleCodes += "," + roleCode;
	if (descs.equals(""))
		descs += desc;
	else
		descs += "," + desc;
	
}		
%>
              <tr>
                <td align="center"><textarea name=roleDescs cols="40" rows="3"><%=descs%></textarea>
                  <input name="roleCodes" value="<%=roleCodes%>" type=hidden /></td>
              </tr>
              <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
                <td align="left" style="padding-top:5px">
				<div>所属用户组的角色：</div>
				  <%
				   // 取出用户所属用户组
				   UserGroupDb[] ugds = user.getGroups();
				   for (int k=0; k<ugds.length; k++) {
				   		UserGroupDb ugd = ugds[k];
				   		RoleDb[] rds = ugd.getRoles();
					    String roleDesc = "";
						for (int n=0; n<rds.length; n++) {
							if (roleDesc.equals(""))
								roleDesc = rds[n].getDesc();
							else
								roleDesc += "，" + rds[n].getDesc();
						}
					    %>
						<%=ugd.getDesc()%>：&nbsp;<%=roleDesc%>
						<%
				   }
				%>
				</td>
              </tr>
              <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
                <td style="PADDING-LEFT: 10px"><input type=hidden name="name" value="<%=user.getName()%>">
                <%
				String urlUnitCode = "";
				if (!privilege.isUserPrivValid(request, "admin")) {
					urlUnitCode = user.getUnitCode();
				}
				%>
                  <input name="button2" class="btn" type="button" onClick="showModalDialog('<%=request.getContextPath() %>/role_multi_sel.jsp?roleCodes=<%=roleCodes%>&unitCode=<%=urlUnitCode%>',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;')" value="选择角色">
&nbsp;&nbsp;&nbsp;&nbsp;
                <input name="Submit3" class="btn" type="submit" value="确定"></td>
              </tr>
            </tbody>
          </form>
        </table>
        <%}%>
        <%if (user!=null) {%>
        <table style="display:none" class="tabStyle_1_subTab percent80" cellSpacing="0" cellPadding="3" width="50%" align="center">
          <form name="form1" method="post" action="?op=setuserofgroup">
            <tbody>
              <tr>
                <td colspan="2" noWrap class="tabStyle_1_subTab_title">设置用户组</td>
              </tr>
<%
UserGroupMgr usergroupmgr = new UserGroupMgr();	
DeptUserDb dud = new DeptUserDb();
DeptDb unit = dud.getUnitOfUser(name);	  
UserGroupDb[] ugs = usergroupmgr.getAllUserGroupOfUnit(unit.getCode(), false);
int len = 0;
if (ugs!=null)
	len = ugs.length;
UserGroupDb[] userofgroups = user.getGroups();
int ulen = 0;
if (userofgroups!=null)
	ulen = userofgroups.length;

String group_code, desc;

for (int i=0; i<len; i++) {
	UserGroupDb ug = ugs[i];
	group_code = ug.getCode();
	desc = ug.getDesc();
	%>
              <tr class="row">
                <td width="9%" style="PADDING-LEFT: 10px">
	<%
	  boolean isChecked = false;
	  for (int k=0; k<ulen; k++) {
	  	if (userofgroups[k].getCode().equals(group_code)) {
			isChecked = true;
			break;
		}
	  }
	  if (group_code.equals(UserGroupDb.EVERYONE)) {
	  	  out.print("<input type=checkbox disabled name=group_code value='" + UserGroupDb.EVERYONE + "' checked>");
	  }
	  else {
		  if (isChecked)
			out.print("<input type=checkbox name=group_code value='" + group_code + "' checked>");
		  else
			out.print("<input type=checkbox name=group_code value='" + group_code + "'>");
	  }%>       </td>
                <td width="91%" align="left"><%=desc%></td>
              </tr>
              <%}%>
              <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
                <td colspan="2" style="PADDING-LEFT: 10px"><input type=hidden name="name" value="<%=user.getName()%>">
                  <input class="btn" name="Submit22" type="submit" value="确定" />
                  &nbsp;&nbsp;&nbsp;
                  <input class="btn" name="Submit2" type="reset" value="重置"></td>
              </tr>
            </tbody>
          </form>
        </table>
        <%}%>
        <%
if (user!=null) {		
%>
        <br>
        <table id="mainTable" class="tabStyle_1_subTab percent80" cellSpacing="0" cellPadding="3" width="50%" align="center">
                    <form name="form1" method="post" action="?op=setprivs">
                      <tbody>
                        <tr>
                          <td class="tabStyle_1_subTab_title" noWrap width="5%">
                          <input type=hidden name="name" value="<%=user.getName()%>">                          </td>
                  <td width="88%" align="center" noWrap class="tabStyle_1_subTab_title">权限</td>
                  <td align="center" noWrap class="tabStyle_1_subTab_title">是否拥有</td>
                        </tr>
  <%
String[] userprivs = user.getPrivs();
PrivDb[] privs = privmgr.getAllPriv();
String priv, desc;
			  
int len = 0;
if (privs!=null)
	len = privs.length;
int privlen = 0;
if (userprivs!=null)
	privlen = userprivs.length;
	
for (int i=0; i<len; i++) {
	PrivDb pv = privs[i];
	priv = pv.getPriv();
	desc = pv.getDesc();
	%>
                        <tr class="row" style="BACKGROUND-COLOR: #ffffff">
                          <td style="PADDING-LEFT: 10px">
                            &nbsp;
                            <%
	  boolean isChecked = false;
	  for (int k=0; k<privlen; k++) {
	  	if (userprivs[k].equals(priv)) {
			isChecked = true;
			break;
		}
	  }
	  if (!pv.isAdmin()) {
		  if (isChecked) {
			out.print("<input type=checkbox name=priv value='" + priv + "' checked>");
		  }
		  else {
			out.print("<input type=checkbox name=priv value='" + priv + "'>");
		  }
	  }
	  else {
		  if (privilege.isUserPrivValid(request, "admin")) {
			  if (isChecked) {
				out.print("<input type=checkbox name=priv value='" + priv + "' checked>");
			  }
			  else {
				out.print("<input type=checkbox name=priv value='" + priv + "'>");
			  }
		  }
		  else {
			  if (isChecked) {
				out.print("<input type=checkbox name=priv2 value='" + priv + "' checked disabled><input name='priv' value='" + priv + "' type='hidden'>");
			  }
			  else {
				out.print("<input type=checkbox name=priv2 value='" + priv + "' disabled>");
			  }
		  }
	  }
	  %>                          </td>
                  <td align="left" title="<%=priv%>"><%=desc%></td>
                  <td align="center"><%=privilege.isUserPrivValid(name, priv)?"<font color='red' style='font-size:16px'>√</font>":"<font style='font-size:16px'>×</font>"%></td>
                        </tr>
                        <%}%>
                        <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
                          <td colspan="3" style="PADDING-LEFT: 10px"><input type=hidden name=username value="<%=user.getName()%>">
                            <input class="btn" name="Submit" type="submit" value="提交">
  &nbsp;&nbsp;&nbsp;
                          <input class="btn" name="Submit" type="reset" value="重置"></td>
                </tr>
                      </tbody>
                    </form>
        </table>
  <%}%>
  <br>
  <%
String[] depts = user.getAdminDepts();
String strdepts = "";
String strDeptNames = "";
int deptslen = 0;
if (depts!=null)
	deptslen = depts.length;
DeptDb ddb = new DeptDb();	
for (int i=0; i<deptslen; i++) {
	if (strdepts.equals("")) {
		strdepts = depts[i];
		strDeptNames = ddb.getDeptDb(depts[i]).getName();
	}
	else {
		strdepts += "," + depts[i];
		strDeptNames += "," + ddb.getDeptDb(depts[i]).getName();
	}
}  
 %>
  <table class="tabStyle_1_subTab percent80" border="0" align="center" cellpadding="3" cellspacing="0">
    <form action="?op=setAdminDept" name="formAdminDept" method="post">
      <tr>
        <td align="center" class="tabStyle_1_subTab_title"><%=user.getRealName()%>&nbsp;管理的部门</td>
      </tr>
      <tr>
        <td><span class="TableData">
          <input type="hidden" name="depts" value="<%=strdepts%>">
          <input type="hidden" name="name" value="<%=name%>">
          <textarea name="textarea" cols="45" rows="3" readOnly wrap="yes" id="deptNames"><%=strDeptNames%></textarea>
          &nbsp;</span><br>
          <div></div></td>
        </tr>
      <tr>
        <td align="left" style="padding:3px">用户所属角色管理的部门：<br />
		<%
		RoleDb[] allRole = user.getRoles();
		for (int i=0; i<allRole.length; i++) {
			RoleDb rd = allRole[i];
			depts = rd.getAdminDepts();
			strDeptNames = "";
			deptslen = 0;
			
			//因为“全部用户”是数组最后一个，如果前面有“全部用户”，数组最后一个就不用加载
		    if(allRole.length !=1){
		    	if(i == allRole.length-1){
			        break;
			    }
		    }
			if (depts!=null)
				deptslen = depts.length;
			for (int j=0; j<deptslen; j++) {
				if (strdepts.equals("")) {
					strDeptNames = ddb.getDeptDb(depts[j]).getName();
				}
				else {
					strDeptNames += "，" + ddb.getDeptDb(depts[j]).getName();
				}
			}
			
			String[] adminDepts = rd.getDeptsOfManager(user.getName());
			if (adminDepts != null){
				for (String adminDept : adminDepts){
	                if (strDeptNames.equals("")) {
	                    strDeptNames = ddb.getDeptDb(adminDept).getName();
	                }
	                else {
	                    strDeptNames += "，" + ddb.getDeptDb(adminDept).getName();
	                }
	            }
			}
			if (rd.isDeptManager()){
			%>
			<%=rd.getDesc()%>：<%=strDeptNames%> (角色可管理本部门)<br />
			<%
			}else{%>
			 <%=rd.getDesc()%>：<%=strDeptNames%> (角色不可管理本部门)<br />
			<%}
		}
		%>
		</td>
      </tr>
      <tr>
        <td align="center">
          <input class="btn" title="添加部门" onclick="openWinDepts(formAdminDept)" type="button" value="选择部门" name="button3" />
&nbsp;&nbsp;
          <input class="btn" name="submit2" type="submit" value="确 定"></td>
        </tr>
    </form>
  </table>
  <%if (user!=null) {%>
  <br>
  <table class="tabStyle_1_subTab percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tbody>
      <tr>
        <td class="tabStyle_1_subTab_title" noWrap width="23%">文件柜目录</td>
        <td class="tabStyle_1_subTab_title" noWrap width="47%">权限</td>
        <td width="30%" noWrap class="tabStyle_1_subTab_title">操作</td>
      </tr>
<%
LeafPriv leafPriv = new LeafPriv();	
Vector result = leafPriv.listUserPriv(user.getName());
Iterator ir = result.iterator();
int i = 0;
Leaf lf = new Leaf();
while (ir.hasNext()) {
 	LeafPriv lp = (LeafPriv)ir.next();
	lf = lf.getLeaf(lp.getDirCode());
	i++;
	%>
    <form id="form<%=i%>" name="form<%=i%>" action="?op=modifyLeafPriv" method=post>
      <tr class="row" style="BACKGROUND-COLOR: #ffffff">
        <td style="PADDING-LEFT: 10px">&nbsp;<img src="images/arrow.gif" align="absmiddle">&nbsp;<%=lf.getName()%>
            <input type=hidden name="op" value="edit">
            <input type=hidden name="id" value="<%=lp.getId()%>">
            <input type=hidden name="dirCode" value="<%=lp.getDirCode()%>">
            <input type=hidden name="name" value="<%=user.getName()%>">        </td>
        <td><input name=see type=checkbox <%=lp.getSee()==1?"checked":""%> value="1">
          浏览&nbsp;
          <input name=append type=checkbox <%=lp.getAppend()==1?"checked":""%> value="1">
          添加 &nbsp;
          <input name=del type=checkbox <%=lp.getDel()==1?"checked":""%> value="1">
          删除&nbsp;
          <input name=modify type=checkbox <%=lp.getModify()==1?"checked":""%> value="1">
          修改
          <input name=examine type=checkbox <%=lp.getExamine()==1?"checked":""%> value="1">
          审核 </td>
        <td><input name="submit" type=submit value="修改">
          &nbsp;
          <input name="button" type=button onClick="window.location.href='user_privilege.jsp?op=delLeafPriv&op=edit&name=<%=StrUtil.UrlEncode(user.getName())%>&dirCode=<%=StrUtil.UrlEncode(lp.getDirCode())%>&id=<%=lp.getId()%>'" value=删除>        </td>
      </tr>
    </form>
    <%}%>
  </table>
  <%}%>
  <%if (user!=null) {%>
  <br>
  <table class="tabStyle_1_subTab percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
    <tbody>
      <tr>
        <td class="tabStyle_1_subTab_title" noWrap width="23%">流程类型</td>
        <td class="tabStyle_1_subTab_title" noWrap width="47%">权限</td>
        <td width="30%" noWrap class="tabStyle_1_subTab_title">操作</td>
      </tr>
      <%
com.redmoon.oa.flow.LeafPriv fleafPriv = new com.redmoon.oa.flow.LeafPriv();	
Vector result = fleafPriv.listUserPriv(user.getName());
Iterator ir = result.iterator();
int i = 0;
com.redmoon.oa.flow.Leaf flf2 = new com.redmoon.oa.flow.Leaf();
while (ir.hasNext()) {
 	com.redmoon.oa.flow.LeafPriv flp = (com.redmoon.oa.flow.LeafPriv)ir.next();
	com.redmoon.oa.flow.Leaf flf = flf2.getLeaf(flp.getDirCode());
	i++;
	%>
    <form id="form<%=i%>" name="form<%=i%>" action="?" method=post>
      <tr class="row" style="BACKGROUND-COLOR: #ffffff">
        <td style="PADDING-LEFT: 10px">&nbsp;<img src="images/arrow.gif" align="absmiddle">&nbsp;<%=flf!=null?flf.getName():""%>
            <input type=hidden name="op" value="edit">
            <input type=hidden name="id" value="<%=flp.getId()%>">
            <input type=hidden name="dirCode" value="<%=flp.getDirCode()%>">
            <input type=hidden name="name" value="<%=user.getName()%>">        </td>
        <td><input name=see type=checkbox <%=flp.getSee()==1?"checked":""%> value="1">
          管理流程</td>
        <td><input name="button4" type=button onClick="jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return;}else{window.location.href='user_privilege.jsp?op=delFlowLeafPriv&name=<%=StrUtil.UrlEncode(user.getName())%>&dirCode=<%=StrUtil.UrlEncode(flp.getDirCode())%>&id=<%=flp.getId()%>'}}) " style="cursor:pointer" value=删除>        </td></tr>
    </form>
    <%}%>
  </table>
  <%}%>
  <br>
  <form name="formDept" action="?op=setMessage" method="post">
  <table border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1_subTab percent80">
    <tr>
      <td align="center" class="tabStyle_1_subTab_title">
        用户能发送短消息至部门、用户组、用户角色的设置，空表示没有限制</td>
      </tr>
    <tr>
      <td align="left">
	  <%
	  String messageToDept = "";
	  String messageToUserGroup = "";
	  String messageToUserRole = "";
	  if (usd!=null && usd.isLoaded()) {
	  	messageToDept = usd.getMessageToDept();
		messageToUserGroup = usd.getMessageToUserGroup();
		messageToUserRole = usd.getMessageToUserRole();
	  }
	  String deptNames = "";
	  String userGroupNames = "";
	  String userRoleNames = "";
	  if (!messageToDept.equals("")) {
	  	String[] ary = messageToDept.split(",");
		DeptDb dd = new DeptDb();
		int len = ary.length;
		for (int i=0; i<len; i++) {
			dd = dd.getDeptDb(ary[i]);
			if (deptNames.equals(""))
				deptNames = dd.getName();
			else
				deptNames += "," + dd.getName();
		}
	  }
	  if (!messageToUserGroup.equals("")) {
	  	String[] ary = messageToUserGroup.split(",");
		UserGroupDb dd = new UserGroupDb();
		int len = ary.length;
		for (int i=0; i<len; i++) {
			dd = dd.getUserGroupDb(ary[i]);
			if (userGroupNames.equals(""))
				userGroupNames = dd.getDesc();
			else
				userGroupNames += "," + dd.getDesc();
		}
	  }	  
	  if (!messageToUserRole.equals("")) {
	  	String[] ary = messageToUserRole.split(",");
		RoleDb dd2 = new RoleDb();
		int len = ary.length;
		for (int i=0; i<len; i++) {
			RoleDb dd = dd2.getRoleDb(ary[i]);
			if (!dd.isLoaded()) {
				continue;
			}
			if (userRoleNames.equals(""))
				userRoleNames = dd.getDesc();
			else
				userRoleNames += "," + dd.getDesc();
		}
	  }	  
	  %>
	  <input type="hidden" name="depts" value="<%=messageToDept%>">
	  <textarea name="deptNames" cols="50" rows="3" readonly><%=deptNames%></textarea>
        <a href="javascript:;" onclick="openWinDepts(formDept)">选择部门</a>          <br>
        (配合“配置管理”中的“限制用户所见部门”选项，可以限制用户在组织机构中只能看到允许的部门)<br>
	    <input type="hidden" name="userGroups" value="<%=messageToUserGroup%>">
        <textarea name="userGroupNames" cols="50" rows="3" readonly><%=userGroupNames%></textarea>
        <a href="javascript:;" onClick="openWinUserGroups()">选择用户组</a><br>
	    <input type="hidden" name="userRoles" value="<%=messageToUserRole%>">
        <textarea name="userRoleNames" cols="50" rows="3" readonly><%=userRoleNames%></textarea>
        <a href="javascript:;" onClick="openWinUserRoles()">选择角色</a><br>
        短消息群发的最大用户数
        <input name="messageToMaxUser" value="<%=usd.getMessageToMaxUser()%>" size="3">
        <br>
        短消息信箱容量
        <input name="messageUserMaxCount" value="<%=usd.getMessageUserMaxCount()%>" size="3">
        条(超出部分的最早收到的消息将会被系统定期删除)<br></td>
      </tr>
    <tr>
      <td align="center"><input class="btn" type="submit" name="Submit4" value="确定">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input class="btn" type="reset" name="Submit42" value="重置">
        <input type=hidden name="name" value="<%=user.getName()%>">        </td>
      </tr>
  </table>
  </form>
  </TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<br>

</body>
<script language="javascript">
<!--
function form1_onsubmit()
{

}

var curForm;

function getDepts() {
	return curForm.depts.value;
}

function getUserGroups() {
	return formDept.userGroups.value;
}

function getUserRoles() {
	return formDept.userRoles.value;
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinDepts(formDept) {
	curForm = formDept;
	var ret = showModalDialog('<%=request.getContextPath()%>/dept_multi_sel.jsp',window.self,'dialogWidth:500px;dialogHeight:480px;status:no;help:no;')
	if (ret==null)
		return;
	formDept.deptNames.value = "";
	formDept.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (formDept.deptNames.value=="") {
			formDept.depts.value += ret[i][0];
			formDept.deptNames.value += ret[i][1];
		}
		else {
			formDept.depts.value += "," + ret[i][0];
			formDept.deptNames.value += "," + ret[i][1];
		}
	}
	/*
	if (formDept.depts.value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		formDept.depts.value = "<%=DeptDb.ROOTCODE%>";
		formDept.deptNames.value = "全部";
	}
	*/
}

function openWinUserGroups() {
	var ret = showModalDialog('<%=request.getContextPath()%>/usergroup_multi_sel.jsp',window.self,'dialogWidth:500px;dialogHeight:480px;status:no;help:no;')
	if (ret==null)
		return;
	formDept.userGroupNames.value = "";
	formDept.userGroups.value = "";
	for (var i=0; i<ret.length; i++) {
		if (formDept.userGroupNames.value=="") {
			formDept.userGroups.value += ret[i][0];
			formDept.userGroupNames.value += ret[i][1];
		}
		else {
			formDept.userGroups.value += "," + ret[i][0];
			formDept.userGroupNames.value += "," + ret[i][1];
		}
	}
}

function openWinUserRoles() {
	var ret = showModalDialog('<%=request.getContextPath()%>/userrole_multi_sel.jsp',window.self,'dialogWidth:500px;dialogHeight:480px;status:no;help:no;')
	if (ret==null)
		return;
	formDept.userRoleNames.value = "";
	formDept.userRoles.value = "";
	for (var i=0; i<ret.length; i++) {
		if (formDept.userRoleNames.value=="") {
			formDept.userRoles.value += ret[i][0];
			formDept.userRoleNames.value += ret[i][1];
		}
		else {
			formDept.userRoles.value += "," + ret[i][0];
			formDept.userRoleNames.value += "," + ret[i][1];
		}
	}
}

function setRoles(roles, descs) {
	formRole.roleCodes.value = roles;
	formRole.roleDescs.value = descs
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
//-->
</script>
</html>