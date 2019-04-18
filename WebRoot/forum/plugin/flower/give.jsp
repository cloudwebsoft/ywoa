<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.flower.*"%>
<%
FlowerMsgAction rma = new FlowerMsgAction();
try {
	boolean re = rma.give(request);
	if (re) {
		out.print(SkinUtil.makeInfo(request, "操作成功！"));
	}
	else
		out.print(SkinUtil.makeErrMsg(request, "操作失败！"));
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
}
%>