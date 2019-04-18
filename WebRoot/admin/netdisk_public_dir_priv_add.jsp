<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理文件柜权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=Global.getRootPath(request) %>/netdisk/clouddisk.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script>
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function setUsers(user, userRealName) {
	form1.name.value = user;
	form1.userRealName.value = userRealName;
}

function setPerson(deptCode, deptName, user, userRealName)
{
	form1.name.value = user;
	form1.userRealName.value = userRealName;
}

function setRoles(roles, descs) {
	formRole.roleCodes.value = roles;
	formRole.roleDescs.value = descs
}

function openWinUsers() {
	selUserNames = form1.name.value;
	selUserRealNames = form1.userRealName.value;
	showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;');
}

</script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dirCode");
PublicLeaf leaf = new PublicLeaf();
leaf = leaf.getLeaf(dirCode);
%>
 <div class="privBack">
 	<a href='../netdisk/clouddisk_public_dir_priv.jsp?dirCode=<%=StrUtil.UrlEncode(dirCode)%>'>
		<img src="../netdisk/images/clouddisk/back.png"/>
	</a>
	<a href='../netdisk/clouddisk_public_dir_priv.jsp?dirCode=<%=StrUtil.UrlEncode(dirCode)%>'>设置<%=leaf.getName()%>权限</a>
</div>
<form name="formRole" method="post" action="../netdisk/clouddisk_public_dir_priv.jsp?op=setrole">

<table  width="80%"  align="center" class="dirTable">
   <tr>
    	<th>角色</th>
  </tr>
      <%
		RoleMgr roleMgr = new RoleMgr();		
		PublicLeafPriv lp = new PublicLeafPriv();
		Vector vrole = lp.getRolesOfLeafPriv(leaf.getCode());
		
		String roleCode;
		String roleCodes = "";
		String descs = "";
		Iterator irrole = vrole.iterator();
		while (irrole.hasNext()) {
			RoleDb rd = (RoleDb)irrole.next();
			roleCode = rd.getCode();
			if (roleCodes.equals(""))
				roleCodes += roleCode;
			else
				roleCodes += "," + roleCode;
			if (descs.equals(""))
				descs += rd.getDesc();
			else
				descs += "," + rd.getDesc();
		}
		%>
      <tr class="roleContent">
        <td><textarea name=roleDescs cols="60" rows="3" class="roleNames"><%=descs%></textarea>
            <input name="roleCodes" value="<%=roleCodes%>" type=hidden>
            <input name="dirCode" value="<%=dirCode%>" type=hidden></td>
      </tr>
      <tr class="btnTr" >
        <td>
        	<input name="Submit3" type="submit" class="sub" value="提 交 ">
        	<input name="button2" type="button" class="btn" onClick="showModalDialog('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;')" value="选择角色">
          	
        </td>
      </tr>
</table>
</form>
<%
String code;
String desc;
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
%>

<table  width="80%" align="center" class="privTable">
  <tr>
    <th>用户组</th>
    <th>描述</th>
    <th>操作</th>
  </tr>
<%
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
	%>
    <tr class="btnTr">
      <td><%=code%></td>
      <td><%=desc%></td>
      <td>
      	<input onclick="window.location.href('../netdisk/clouddisk_public_dir_priv.jsp?op=add&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=LeafPriv.TYPE_USERGROUP%>')"  type="button" value="添加用户"  class="btn"/>
      </td>
    </tr>
<%}%>
</table>
<br>
<form name="form1" action="../netdisk/clouddisk_public_dir_priv.jsp?op=add" method=post>
	<table width="80%"  align="center" class="dirTable" >
	 <tr>
    	<th>添加用户</th>
  	 </tr>
  	  <tr class="roleContent">
	    <td>
	    	<span class="colTitle">用户名</span>
	    	<input name="userRealName" value="" readonly class="colInput" />
	        <input name="name" value="" type="hidden" />
	        <input type=hidden name=type value=1 />
		  	<input type=hidden name=dirCode value="<%=leaf.getCode()%>" />
	    </td>
	  </tr>
	  <tr class="btnTr">
	    <td>
	    	<input type="submit" value="添加" class="sub"/>
	        <input type="button" value="选择用户"  onClick="openWinUsers()" class="btn"/> 
		</td>
	
	</table>
</form>
</body>
</html>