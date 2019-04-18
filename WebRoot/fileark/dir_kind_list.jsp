<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>文件柜 - 目录类别管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
</head>
<body>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String dirCode = ParamUtil.get(request, "dirCode");
if (dirCode.equals("")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请选择目录！"));
	return;
}

Leaf lf = new Leaf();
lf = lf.getLeaf(dirCode);
if (lf==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "目录不存在！"));
	return;
}

LeafPriv lp = new LeafPriv(dirCode);
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	DirKindMgr dkm = new DirKindMgr();
	boolean re = false;
	try {
		re = dkm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert_Redirect("操作成功！", "dir_kind_list.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	return;
}
else if (op.equals("del")) {
	DirKindMgr dkm = new DirKindMgr();
	boolean re = false;
	try {
		re = dkm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert_Redirect("操作成功！", "dir_kind_list.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	return;
}
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><%=lf.getName()%>&nbsp;-&nbsp;类别管理</td>
    </tr>
  </tbody>
</table>
<br />
<table class="tabStyle_1 percent60" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="3" class="tabStyle_1_title">类别</td>
  </tr>
  <tr>
    <td colspan="3" align="center"><form id=form1 name="form1" action="?op=add" method=post>
        类别：
<select id="kind" name="kind">
<%
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("fileark_kind");
Vector vsd = sd.getOptions();
Iterator irsd = vsd.iterator();
while (irsd.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)irsd.next();
	%>
	<option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
	<%
}
%>        
</select>
		序号：
		<input name="orders" size="3" value="1" />
        &nbsp;
        <input class="btn" name="submit" type=submit value="添加">
        <input type="hidden" name="dirCode" value="<%=dirCode%>" />
	</form></td>
  </tr>
  <%
  SelectOptionDb sod = new SelectOptionDb();  
  DirKindDb dkd = new DirKindDb();
  String sql = "select id from dir_kind where dir_code=" + StrUtil.sqlstr(dirCode) + " order by orders";
  Iterator ir = dkd.list(sql).iterator();
  while (ir.hasNext()) {
	dkd = (DirKindDb)ir.next();%>
  <tr>
    <td width="38%"><%=sod.getOptionName("fileark_kind", dkd.getKind())%></td>
    <td width="36%">序号：<%=dkd.getOrders()%></td>
    <td width="26%" align="center">
    <a href="dir_kind_edit.jsp?id=<%=dkd.getId()%>">编辑</a>
    &nbsp;&nbsp;
    <a onclick="return confirm('您确定要删除么？')" href="dir_kind_list.jsp?op=del&id=<%=dkd.getId()%>&dirCode=<%=StrUtil.UrlEncode(dkd.getDirCode())%>">删除</a></td>
  </tr>
  <%}%>
</table>
</body>
</html>
