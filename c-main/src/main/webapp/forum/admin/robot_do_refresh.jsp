<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.robot.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 注意request传值为来时，不能用id，否则会与模板中从request中取id造成混淆
int id = ParamUtil.getInt(request, "robotId");

Roboter rt = new Roboter();
int ret = 0;
try {
	ret = rt.gatherOneByOne(request, id);
}
catch (ErrMsgException e) {
%>
	<script>
	window.parent.addDocUrl("<%=e.getMessage()%>");
	</script>
<%
	return;	
}
out.print("ret=" + ret + "<BR>");
if (ret==1 || ret==2) {
	if (rt.getResult().size()>0) {
		out.print((String)rt.getResult().elementAt(0));
	%>
		<span id="ret"><%=(String)rt.getResult().elementAt(0)%></span>
		<script>
		window.parent.addDocUrl(ret.innerHTML);
		</script>
	<%
	}
	return;
}
else if (ret==0 || ret==-1) {
	if (rt.getResult().size()>0) {
		out.print((String)rt.getResult().elementAt(0));
	%>
		<meta http-equiv=refresh content=0;url=robot_do_refresh.jsp?robotId=<%=id%>>
		<span id="ret"><%=(String)rt.getResult().elementAt(0)%></span>
		<script>
		window.parent.addDocUrl(ret.innerHTML);
		</script>
	<%
	}
	// 使用下行上面的script将会不起作用
	// response.sendRedirect("robot_do_refresh.jsp?robotId=" + id);
	return;
}
%>
