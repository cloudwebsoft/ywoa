<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>文件柜 - 类别编辑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");

DirKindDb dkd = new DirKindDb();
dkd = dkd.getDirKindDb(id);

Leaf lf = new Leaf();
lf = lf.getLeaf(dkd.getDirCode());
if (lf==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "目录不存在！"));
	return;
}

LeafPriv lp = new LeafPriv(dkd.getDirCode());
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	DirKindMgr wptm = new DirKindMgr();
	boolean re = false;
	try {
		re = wptm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "dir_kind_edit.jsp?id=" + id));
		return;
	}
}
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><%=lf.getName()%>&nbsp;-&nbsp;类别管理</td>
    </tr>
  </tbody>
</table>
<form action="dir_kind_edit.jsp?op=modify" method=post> 
<table width="494" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
  <tr> 
    <td height="23" colspan="2" class="tabStyle_1_title">类别</td>
  </tr>
  <tr> 
    <td width="232" align="center">
		名称
	  </td>
    <td align="left"><select id="kind" name="kind">
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
    <script>
	o("kind").value = "<%=dkd.getKind()%>";
	</script>
    <input name="id" value="<%=id%>" type=hidden /></td>
  </tr>
  <tr>
    <td align="center">序号</td>
    <td align="left"><input name="orders" value="<%=dkd.getOrders()%>" size="3" /></td>
  </tr>
  <tr>
    <td colspan="2" align="center">
    <input class="btn" name="submit" type=submit value="确定" />
    &nbsp;&nbsp;&nbsp;&nbsp;
<input class="btn" type="button" value="返回" onclick="window.location.href='dir_kind_list.jsp?dirCode=<%=StrUtil.UrlEncode(lf.getCode())%>'" />
    </td>
    </tr>
</table>
</form>
<br>
</body>
</html>
