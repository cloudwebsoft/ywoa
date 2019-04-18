<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
Map map = new HashMap();
%>
<%!
public void addMap(Map map, com.redmoon.oa.ui.menu.Leaf lf) {
	// 取可以看到lf的权限
	String pvgCodes = lf.getPvg();
	String[] ary = StrUtil.split(pvgCodes, ",");
	if (ary!=null) {
		for (int i=0; i<ary.length; i++) {
			if (ary[i].equals("admin")) {
				continue;
			}
			Vector vItem = (Vector)map.get(ary[i]);
			if (vItem==null) {
				vItem = new Vector();
				vItem.addElement(lf);
				map.put(ary[i], vItem);
			}
			else {
				vItem.addElement(lf);
			}
		}
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理用户组权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<style>
	.layer1 {
		background-color:#E1E0FE;
	}
	.layer2 {
		background-color:#ffffff;
	}
</style>
</head>
<body>
<jsp:useBean id="privmgr" scope="page" class="com.redmoon.oa.pvg.PrivMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String group_code = ParamUtil.get(request, "group_code");
if (group_code.equals("")) {
	out.print(StrUtil.makeErrMsg("用户组编码不能为空！"));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("setgrouppriv")) {
	com.redmoon.oa.pvg.Privilege privg = new com.redmoon.oa.pvg.Privilege();
	try {
		if (privg.setGroupPriv(request))
			out.print(StrUtil.Alert_Redirect("修改用户组权限成功！", "user_group_priv.jsp?group_code=" + StrUtil.UrlEncode(group_code)));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("setusergroupofrole")) {
	String groupCode = ParamUtil.get(request, "group_code");
	if (groupCode.equals("")) {
		StrUtil.Alert_Back("用户组编码名不能为空！");
		return;
	}
	UserGroupMgr usergroupmgr = new UserGroupMgr();
	UserGroupDb ugd = usergroupmgr.getUserGroupDb(groupCode);
	try {
		if (ugd.setRoles(request))
			out.print(StrUtil.Alert("修改用户组角色成功！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}

UserGroupDb ug = new UserGroupDb();
ug = ug.getUserGroupDb(group_code);
%>
<%@ include file="user_group_op_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<table class="tabTitle" width="98%" align="center" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td align="center">用户组&nbsp;-&nbsp;<%=ug.getDesc()%>&nbsp;权限</td>
    </tr>
  </tbody>
</table>
<%
String[] grouppriv = ug.getGroupPriv(group_code);

PrivDb[] privs = privmgr.getAllPriv();
String priv;
%>
<br>
<form name="form1" method="post" action="?op=setusergroupofrole">
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="48%" align="center">
    <tbody>
      <tr>
        <td colspan="2" align="center" noWrap class="tabStyle_1_title">所属角色</td>
      </tr>
      <%
RoleMgr roleMgr = new RoleMgr();		  
RoleDb[] roles = null; // roleMgr.getAllRoles();
RoleDb roleDb = new RoleDb();
java.util.Vector vt = roleDb.getRolesOfUnit(ug.getUnitCode());
roles = new RoleDb[vt.size()];
int n=0;
java.util.Iterator irrole = vt.iterator();
while (irrole.hasNext()) {
	roleDb = (RoleDb)irrole.next();
	roles[n] = roleDb;
	n++;
}
int len = 0;
if (roles!=null)
	len = roles.length;

RoleDb[] ugroles = ug.getRoles();
int ulen = 0;
if (ugroles!=null)
	ulen = ugroles.length;

String roleCode, desc;

for (int i=0; i<len; i++) {
	RoleDb rd = roles[i];
	roleCode = rd.getCode();
	desc = rd.getDesc();
	%>
      <tr>
        <td width="9%" align="center">
          <%
	  boolean isChecked = false;
	  for (int k=0; k<ulen; k++) {
	  	if (ugroles[k].getCode().equals(roleCode)) {
			isChecked = true;
			break;
		}
	  }
	  if (isChecked)
	  	out.print("<input type=checkbox name=roleCode value='" + roleCode + "' checked>");
	  else
	  	out.print("<input type=checkbox name=roleCode value='" + roleCode + "'>");
	  %>
        </td>
        <td width="91%"><%=desc%></td>
      </tr>
      <%}%>
      <tr align="center">
        <td colspan="2"><input type=hidden name="group_code" value="<%=ug.getCode()%>">
            <input name="Submit2" type="submit" class="btn" value="确定">
          &nbsp;&nbsp;&nbsp;
          <input name="Submit2" type="reset" class="btn" value="重置"></td>
      </tr>
    </tbody>
</table>
</form>

<form name="form1" method="post" action="?op=setgrouppriv">
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="48%" align="center">
  <tbody>
    <tr>
      <td colspan="3" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px">权限</td>
      </tr>
<%
com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(com.redmoon.oa.ui.menu.Leaf.CODE_ROOT);
Vector v1 = lccm.getChildren();
Iterator ir = v1.iterator();
while (ir.hasNext()) {
	com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)ir.next();
	addMap(map, lf);
	com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm2 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(lf.getCode());
	Vector v2 = lccm2.getChildren();
	Iterator ir2 = v2.iterator();
	while (ir2.hasNext()) {
		com.redmoon.oa.ui.menu.Leaf lf2 = (com.redmoon.oa.ui.menu.Leaf)ir2.next();
		addMap(map, lf2);

		com.redmoon.oa.ui.menu.LeafChildrenCacheMgr lccm3 = new com.redmoon.oa.ui.menu.LeafChildrenCacheMgr(lf2.getCode());
		Vector v3 = lccm3.getChildren();
		Iterator ir3 = v3.iterator();
		while (ir3.hasNext()) {
			com.redmoon.oa.ui.menu.Leaf lf3 = (com.redmoon.oa.ui.menu.Leaf)ir3.next();
			addMap(map, lf3);
		}
	}
}

len = 0;
if (privs!=null)
	len = privs.length;
int privlen = 0;
if (grouppriv!=null)
	privlen = grouppriv.length;
	
for (int i=0; i<len; i++) {
	PrivDb pv = privs[i];
	priv = pv.getPriv();
	desc = pv.getDesc();
	%>
    <tr class="row<%=pv.getLayer()==1?" layer1":" layer2"%>">
      <td width="10%" align="center" style="PADDING-LEFT: 10px">
        <%
	  boolean isChecked = false;
	  for (int k=0; k<privlen; k++) {
	  	if (grouppriv[k].equals(priv)) {
			isChecked = true;
			break;
		}
	  }
	  if (pv.getLayer()==2) {
		  if (!pv.isAdmin()) {	  
			  if (isChecked)
				out.print("<input type=checkbox name=priv value='" + priv + "' checked>");
			  else
				out.print("<input type=checkbox name=priv value='" + priv + "'>");
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
	  }
	  %>
      </td>
      <td width="49%"><%=desc%></td>
      <td width="41%"><style>
.menu_item a{color:#608acc;background:#fff;overflow:hidden; display:block; width:200px;height:20px;line-height:15px;border:1px solid #b4c6dc;padding:0 20px 0 5px; margin-top:3px;}

                  </style>
        <div class="menu_item">
          <%
	Vector vtMap = (Vector)map.get(priv);
	if (vtMap!=null) {
		Iterator irItem = vtMap.iterator();
		while (irItem.hasNext()) {
			com.redmoon.oa.ui.menu.Leaf lf = (com.redmoon.oa.ui.menu.Leaf)irItem.next();
			String link = lf.getLink(request);
			String onclick = "";
			if (!"".equals(link)) {
				onclick = "onclick=\"addTab('" + lf.getName() + "', '" + request.getContextPath() + "/" + link + "')\"";
			}
			String mName = lf.getName();
			if (lf.getLayer()==3) {
				com.redmoon.oa.ui.menu.Leaf pLf = lf.getLeaf(lf.getParentCode());
				mName = pLf.getName() + "\\" + mName;
			}
			if (lf.getLayer()==4) {
				com.redmoon.oa.ui.menu.Leaf pLf = lf.getLeaf(lf.getParentCode());
				mName = pLf.getName() + "\\" + mName;
				pLf = lf.getLeaf(pLf.getParentCode());
				mName = pLf.getName() + "\\" + mName;
			}
	%>
          <a href="javascript:;" <%=onclick%> ><%=mName%></a>
          <%	}
	}%>
        </div></td>
    </tr>
<%}%>
    <tr align="center" class="row">
      <td colspan="3" style="PADDING-LEFT: 10px">
	  <input type=hidden name=group_code value="<%=group_code%>">
	  <input name="Submit" type="submit" class="btn" value="确定">
&nbsp;&nbsp;&nbsp;
<input name="Submit" type="reset" class="btn" value="重置"></td>
    </tr>
  </tbody>
</table>
<br />
<br />
</form>
</body>
</html>