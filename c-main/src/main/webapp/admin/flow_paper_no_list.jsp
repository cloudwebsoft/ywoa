<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<!DOCTYPE html>
<html>
<title>公文文号管理</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="/oads_spark/skin/blue/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

int prefixId = ParamUtil.getInt(request, "prefixId", -1);
PaperNoPrefixDb pnpd = new PaperNoPrefixDb();
pnpd = (PaperNoPrefixDb)pnpd.getPaperNoPrefixDb(prefixId);

PaperNoDb pdd = new PaperNoDb();

if(op.equals("edit")){
	int id = ParamUtil.getInt(request, "id");
	int curNum = ParamUtil.getInt(request, "cur_num", 0);
	pdd = pdd.getPaperNoDb(id);
	pdd.set("cur_num", new Integer(curNum));
	boolean re = pdd.save();

	if(re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_paper_no_list.jsp?prefixId=" + prefixId));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}		

	return;
}
else if(op.equals("del")){
	int id = ParamUtil.getInt(request, "id");
	pdd = pdd.getPaperNoDb(id);
	boolean re = pdd.del();

	if(re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_paper_no_list.jsp?prefixId=" + prefixId));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}	
	return;
}
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><%=pnpd.getString("name")%>&nbsp;文号管理</td>
    </tr>
  </tbody>
</table>
<br>
<table id="mainTable" cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1 percent60">
  <thead>
    <tr>
      <td width="30%">年份</td>
	  <td width="29%">值</td>
	  <td width="41%">操作</td>
    </tr>
  </thead>
  <tbody>
<%
String sql = "select id from " + pdd.getTable().getName() + " where prefix_id=" + prefixId;
Iterator ir = pdd.list(sql).iterator();
UserMgr um = new UserMgr();
int i = 100;
DeptDb dd = new DeptDb();
PaperNoPrefixDeptDb pnpdd = new PaperNoPrefixDeptDb();
while (ir.hasNext()) {
	pdd = (PaperNoDb)ir.next();
	
	long id = pdd.getLong("id");
	
	i++;


	%>
  <form name="form<%=id%>" action="?op=edit" method="post" >
    <tr>
      <td><%=pdd.getInt("cur_year")%></td>
      <td><input name="cur_num" value="<%=pdd.getInt("cur_num")%>" /></td>
      <td align="center">
        <a href="javascript:form<%=id%>.submit()">编辑</a>
        &nbsp;&nbsp;
        <a onClick="if (!confirm('您确定要删除么')) return false" href="flow_paper_no_list.jsp?op=del&id=<%=id%>&prefixId=<%=prefixId%>">删除</a>
        &nbsp;&nbsp;
      <input name="id" value="<%=id%>" type="hidden">
      <input name="prefixId" value="<%=prefixId%>" type="hidden">
      </td>
    </tr>
  </form>
<%}%>
  <form id="formAdd" name="formAdd" action="flow_paper_no_prefix_list.jsp?op=add" method="post" onsubmit="return formAdd_onsubmit()" >
  </form>
</table>
</body>
<script>
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
</script>
</html>