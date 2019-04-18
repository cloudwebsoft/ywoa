<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理流程类型权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<jsp:useBean id="selectKindPriv" scope="page" class="com.redmoon.oa.basic.SelectKindPriv"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
//if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
//	out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
//	return;
//}
String op = ParamUtil.get(request, "op");
int kindId = ParamUtil.getInt(request, "kindId", -1);
String tabIdOpener = ParamUtil.get(request, "tabIdOpener");

if (kindId!=-1) {
	if (!(selectKindPriv.canUserExamine(privilege.getUser(request), kindId))) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid") + " 用户需对该节点拥有管理的权限"));	
		return;
	}
}

String curName = "";
SelectKindDb skd = new SelectKindDb();
if (kindId!=-1) {
	skd = skd.getSelectKindDb(kindId);
	curName = skd.getName();
}

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
		if (type == SelectKindPriv.TYPE_USER) {
			UserDb user = new UserDb();
			user = user.getUserDb(um);
			if (!user.isLoaded()) {
				continue;
			}
		}
		try {
			selectKindPriv.setKindId(kindId);
			re = selectKindPriv.add(um, type);
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("添加成功！","提示", "basic_select_kind_priv_m.jsp?kindId=" + kindId + "&tabIdOpener=" + tabIdOpener));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败", "提示"));
	}
	return;
}
else if (op.equals("setrole")) {
	try {
		String roleCodes = ParamUtil.get(request, "roleCodes");
		String leafCode = ParamUtil.get(request, "dirCode");
		SelectKindPriv lp = new SelectKindPriv();
		lp.setKindId(kindId);
		lp.setRoles(leafCode, roleCodes);
		out.print(StrUtil.jAlert("操作成功！","提示"));
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
}
else if (op.equals("modify")) {
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
	
	selectKindPriv.setId(id);
	selectKindPriv.setAppend(append);
	selectKindPriv.setModify(modify);
	selectKindPriv.setDel(del);
	selectKindPriv.setSee(see);
	selectKindPriv.setExamine(examine);
	if (selectKindPriv.save())
		out.print(StrUtil.jAlert("修改成功！","提示"));
	else
		out.print(StrUtil.jAlert("修改失败！","提示"));
}
else if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	SelectKindPriv lp = new SelectKindPriv();
	lp = lp.getSelectKindPriv(id);
	if (lp.del())
		out.print(StrUtil.jAlert("删除成功！","提示"));
	else
		out.print(StrUtil.jAlert("删除失败！","提示"));
}

String action = ParamUtil.get(request, "action");
Vector result = null;
String what = ParamUtil.get(request, "what");

String sql = "select id from oa_select_kind_priv where kind_id=" + kindId + " order by id desc";
result = selectKindPriv.list(sql);

Iterator ir = result.iterator();
%>
<table width="98%" align="center" class="percent98">
  <tr>
    <td align="right">
<form action="basic_select_kind_priv_m.jsp" method="get">
&nbsp;&nbsp;<input name="button" class="btn" type="button" onclick="javascript:location.href='basic_select_kind_priv_add.jsp?kindId=<%=kindId%>&tabIdOpener=<%=tabIdOpener%>';" value="添加" />
</form>
    </td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellspacing="0" cellpadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" nowrap="nowrap" width="18%">用户</td>
      <td class="tabStyle_1_title" nowrap="nowrap" width="8%">增加</td>
      <td class="tabStyle_1_title" nowrap="nowrap" width="9%">修改</td>
      <td class="tabStyle_1_title" nowrap="nowrap" width="8%">删除</td>
      <td class="tabStyle_1_title" nowrap="nowrap" width="13%">类型</td>
      <td width="19%" nowrap="nowrap" class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
SelectKindDb skdm = new SelectKindDb();
while (ir.hasNext()) {
 	SelectKindPriv lp = (SelectKindPriv)ir.next();
	i++;
	SelectKindDb lf = skdm.getSelectKindDb(lp.getKindId());
	String kindName = lf==null?"":lf.getName();
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method="post">
    <tr class="highlight">
      <td style="PADDING-LEFT: 10px"><%
	  if (lp.getType()==lp.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(lp.getName());
		out.print(ud.getRealName());
	  }else if (lp.getType()==lp.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(lp.getName());
	  	out.print(rd.getDesc());
	  }
	  else if (lp.getType()==lp.TYPE_USERGROUP) {
	  	UserGroupDb ug = new UserGroupDb();
		ug = ug.getUserGroupDb(lp.getName());
	  	out.print(ug.getDesc());
	  }
	  %>
          <input type="hidden" name="id" value="<%=lp.getId()%>" />
          <input type="hidden" name="kindId" value="<%=lp.getKindId()%>" />
          <input type="hidden" name="tabIdOpener" value="<%=tabIdOpener%>" />
      </td>
      <td align="center" style="PADDING-LEFT: 10px"><input name="append" type="checkbox" <%=lp.getAppend()==1?"checked":""%> value="1" />
      </td>
      <td align="center" style="PADDING-LEFT: 10px"><input name="modify" type="checkbox" <%=lp.getModify()==1?"checked":""%> value="1" /></td>
      <td align="center" style="PADDING-LEFT: 10px"><input name="del" type="checkbox" <%=lp.getDel()==1?"checked":""%> value="1" /></td>
      <td align="center"><%=lp.getTypeDesc()%></td>
      <td align="center">
        <input class="btn" type=submit value="修改" />
  		&nbsp;
  		<input class="btn" type=button onclick="jConfirm('您确定要删除吗?','提示',function(r){ if(!r){return;}else{window.location.href='basic_select_kind_priv_m.jsp?op=del&kindId=<%=kindId %>&id=<%=lp.getId()%>&tabIdOpener=<%=tabIdOpener%>'}}) " style="cursor:pointer" value="删除" /></td>
    </tr></form>
<%}%>
  </tbody>
</table>
</body>
<script>
$(function() {
	if (window.top.o("content-main")) {
		window.top.reloadTabFrame("<%=tabIdOpener%>");
	}
	else {
		window.top.mainFrame.reloadTabById("<%=tabIdOpener%>");
	}
});
</script>
</html>