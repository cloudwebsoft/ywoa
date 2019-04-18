<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%
String codeTop = ParamUtil.get(request, "code");
if (codeTop.equals("")) {
	codeTop = ParamUtil.get(request, "dirCode");
}
Leaf lfTop = new Leaf();
lfTop = lfTop.getLeaf(codeTop);
if (lfTop==null) {
	return;
}
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="dir_right.jsp?op=modify&code=<%=StrUtil.UrlEncode(codeTop)%>"><span><%=lfTop.getName() %></span></a></li>
    <li id="menu2"><a href="dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(codeTop)%>"><span>权限</span></a></li>
    <%if (License.getInstance().isSrc() && codeTop.equals(Leaf.ROOTCODE)) {%>
    <li id="menu3"><a href="dir_scripts_iframe.jsp?dirCode=<%=StrUtil.UrlEncode(codeTop)%>"><span>脚本</span></a></li>
    <%}%>
  </ul>
</div>

