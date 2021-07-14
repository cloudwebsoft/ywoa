<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
DocTemplateDb ld = new DocTemplateDb();
String sql = "select id from " + ld.getTableName() + " where unit_code=" + StrUtil.sqlstr(com.redmoon.oa.flow.Leaf.UNIT_CODE_PUBLIC) + " or unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by sort";
Iterator ir = ld.list(sql).iterator();
%>
<select id="template" name="template">
<%
DocTemplateMgr dtm = new DocTemplateMgr();
DeptDb dd = new DeptDb();
while (ir.hasNext()) {
 	ld = (DocTemplateDb)ir.next();
	if (dtm.canUserSee(request, ld)) {
	%>
	<option value="<%=ld.getId()%>"><%=ld.getTitle()%></option>
	<%
	}
}
%>
</select>