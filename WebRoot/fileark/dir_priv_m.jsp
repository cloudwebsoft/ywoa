<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dirCode");
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "name";
String sort = ParamUtil.get(request, "sort");

String op = ParamUtil.get(request, "op");

String isAll = ParamUtil.get(request, "isAll");
if (dirCode.equals("")) {
	isAll = "y";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理目录权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "dir_priv_m.jsp?dirCode=<%=dirCode%>&isAll=<%=isAll%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

</head>
<body>
<%@ include file="dir_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<%
if (isAll.equals("y")) {
	dirCode = Leaf.ROOTCODE;
	if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
		out.print(StrUtil.jAlert_Back(privilege.MSG_INVALID,"提示"));
		return;
	}
}
%>
<jsp:useBean id="leafPriv" scope="page" class="com.redmoon.oa.fileark.LeafPriv"/>
<%
//if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
//	out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
//	return;
//}

leafPriv.setDirCode(dirCode);
if (!(leafPriv.canUserDel(privilege.getUser(request)) || leafPriv.canUserExamine(privilege.getUser(request)))) {
	out.print(StrUtil.jAlert_Back(Privilege.MSG_INVALID + " 用户需对该节点拥有删除或审核的权限！","提示"));
	return;
}

Leaf leaf = new Leaf();
leaf = leaf.getLeaf(dirCode);

if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		out.print(StrUtil.jAlert_Back("名称不能为空！","提示"));
		return;
	}
	int type = ParamUtil.getInt(request, "type");
	String[] names = name.split("\\,");
	boolean re = false;
	for (String um : names) {
		if (type == LeafPriv.TYPE_USER) {
			UserDb user = new UserDb();
			user = user.getUserDb(um);
			if (!user.isLoaded()) {
				continue;
			}
		}
		try {
			re = leafPriv.add(um, type);
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败", "提示"));
	}
	return;
}
else if (op.equals("setrole")) {
	try {
		String roleCodes = ParamUtil.get(request, "roleCodes");
		String leafCode = ParamUtil.get(request, "dirCode");
		LeafPriv lp = new LeafPriv(leafCode);
		lp.setRoles(leafCode, roleCodes);
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
else if (op.equals("modify")) {
	int id = ParamUtil.getInt(request, "id");
	int see = 0, append=0, del=0, modify=0, examine=0, download = 0;
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
	String strdownload = ParamUtil.get(request, "downLoad");
	if (StrUtil.isNumeric(strdownload)) {
		download = Integer.parseInt(strdownload);
	}
	
	leafPriv.setId(id);
	leafPriv.setAppend(append);
	leafPriv.setModify(modify);
	leafPriv.setDel(del);
	leafPriv.setSee(see);
	leafPriv.setExamine(examine);
	leafPriv.setDownLoad(download);
	if (leafPriv.save()) {
		if (isAll.equals("y"))
			out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "dir_priv_m.jsp?isAll=" + isAll));
		else	
			out.print(StrUtil.jAlert_Redirect("修改成功！","提示", "dir_priv_m.jsp?isAll=" + isAll + "&dirCode=" + StrUtil.UrlEncode(dirCode)));
	}
	else
		out.print(StrUtil.jAlert_Back("修改失败！","提示"));
	return;
}
else if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	LeafPriv lp = new LeafPriv();
	lp = lp.getLeafPriv(id);
	if (lp.del())
		out.print(StrUtil.jAlert("删除成功！","提示"));
	else
		out.print(StrUtil.jAlert("删除失败！","提示"));
}


String sql = "select id from dir_priv" + " order by " + orderBy + " " + sort;
Vector result = null;
if (isAll.equals("y")) {
	result = leafPriv.list(sql);
}
else{
	sql = "select id from dir_priv" + " where dir_code = " + StrUtil.sqlstr(dirCode) + " order by " + orderBy + " " + sort;
	result = leafPriv.list(sql);
}
Iterator ir = result.iterator();
%>
<br />
<%if (!isAll.equals("y")) {%>
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="right">
    <input class="btn" name="button" type="button" onclick="javascript:location.href='dir_priv_add.jsp?dirCode=<%=StrUtil.UrlEncode(leafPriv.getDirCode())%>';" value="添加权限" width=80 height=20 />
    <help:HelpDocTag id="915" type="content" size="200"></help:HelpDocTag>
</td>
  </tr>
</table>
<%}%>
<table class="tabStyle_1 percent98" cellspacing="0" cellpadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" nowrap width="14%" style="cursor:pointer" onclick="doSort('name')">名称
        <%if (orderBy.equals("name")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>	  
	  </td>
      <td class="tabStyle_1_title" nowrap width="13%" style="cursor:pointer" onclick="doSort('priv_type')">类型<span class="right-title" style="cursor:pointer">
        <%if (orderBy.equals("priv_type")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>
      </span></td>
      <td class="tabStyle_1_title" nowrap width="14%" onclick="doSort('dir_code')" style="cursor:pointer">目录<span class="right-title" style="cursor:pointer">
        <%if (orderBy.equals("dir_code")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>
      </span></td>
      <td class="tabStyle_1_title" noWrap width="40%">权限</td>
      <td width="19%" nowrap class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
Directory dir = new Directory();
while (ir.hasNext()) {
 	LeafPriv lp = (LeafPriv)ir.next();
	Leaf lf = dir.getLeaf(lp.getDirCode());	
	if (lf==null)
		continue;
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
    <tr class="highlight" id="tr<%=i%>">
      <td>
      <%
	  if (lp.getType()==LeafPriv.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(lp.getName());
		out.print(ud.getRealName());
	  }else if (lp.getType()==LeafPriv.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(lp.getName());
	  	out.print(rd.getDesc());
	  }
	  else if (lp.getType()==LeafPriv.TYPE_USERGROUP) {
	  	UserGroupDb ug = new UserGroupDb();
		ug = ug.getUserGroupDb(lp.getName());
	  	out.print(ug.getDesc());
	  }
	  %>
	  <input type=hidden name="id" value="<%=lp.getId()%>" />
      <input type=hidden name="dirCode" value="<%=lp.getDirCode()%>" />
	  <input type=hidden name="isAll" value="<%=isAll%>" /></td>
      <td>
      <%
	  if (lp.getType()==LeafPriv.TYPE_USER) {
	  	%>
		用户
		<%
	  }else if (lp.getType()==LeafPriv.TYPE_ROLE) {
	  	%>
		角色
		<%
	  }
	  else if (lp.getType()==LeafPriv.TYPE_USERGROUP) {
	  	%>
		用户组
		<%
	  }
	  %>      
      </td>
      <td><a href="document_list_m.jsp?dirCode=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a></td>
      <td>
	  <input id="see" name="see" type=checkbox <%=lp.getSee()==1?"checked":""%> value="1" onclick="checkPrivSee('tr<%=i%>')" />浏览&nbsp;
	  <input name="append" type=checkbox <%=lp.getAppend()==1?"checked":""%> value="1" onclick="checkPrivAppend('tr<%=i%>')" /> 
	  添加 &nbsp;
	  <input name="del" type=checkbox <%=lp.getDel()==1?"checked":""%> value="1" onclick="checkPrivDel('tr<%=i%>')" />
	  删除&nbsp;
	  <input name="modify" type=checkbox <%=lp.getModify()==1?"checked":""%> value="1" onclick="checkPrivModify('tr<%=i%>')" /> 
	  修改 
	  <input name="downLoad" title="下载附件" type=checkbox <%=lp.getDownLoad()==1?"checked":""%> value="1" onclick="checkPrivModify('tr<%=i%>')" /> 
	  下载 
	  <input name="examine" type=checkbox <%=lp.getExamine()==1?"checked":""%> value="1" onclick="checkPrivExamine('tr<%=i%>')" />
	  管理 </td>
      <td align="center">
	  <input class="btn" type=submit value="修改" />
	  &nbsp;<input class="btn" type=button onclick="jConfirm('您确定要删除吗?','提示',function(r){if(!r){return;}else{window.location.href='dir_priv_m.jsp?op=del&isAll=<%=isAll%>&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&id=<%=lp.getId()%>' }}) " value="删除" /></td>
    </tr></form>
<%}%>     
</table>
<br />
</body>
<script>
function checkPrivSee(trId) {
	var isChecked = $("#" + trId + " input[name='see']").attr("checked");
	if (!isChecked) {
		$("#" + trId + " input[name='append']").attr("checked", false);
		$("#" + trId + " input[name='del']").attr("checked", false);
		$("#" + trId + " input[name='modify']").attr("checked", false);
		// $("#" + trId + " input[name='examine']").attr("checked", false);
	}
}

function checkPrivAppend(trId) {
	var isChecked = $("#" + trId + " input[name='append']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='see']").attr("checked", true);
	}
}

function checkPrivDel(trId) {
	var isChecked = $("#" + trId + " input[name='del']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='see']").attr("checked", true);
	}
}

function checkPrivModify(trId) {
	var isChecked = $("#" + trId + " input[name='modify']").attr("checked");
	if (isChecked) {
		$("#" + trId + " input[name='see']").attr("checked", true);
	}
}

function checkPrivExamine(trId) {
	var isChecked = $("#" + trId + " input[name='examine']").attr("checked");
	if (isChecked) {
		// $("#" + trId + " input[name='see']").attr("checked", true);
		// $("#" + trId + " input[name='append']").attr("checked", true);
		// $("#" + trId + " input[name='del']").attr("checked", true);
		// $("#" + trId + " input[name='modify']").attr("checked", true);
	}
}
</script>
</html>