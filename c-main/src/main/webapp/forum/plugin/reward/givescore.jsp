<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.reward.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<%
RewardMsgAction rma = new RewardMsgAction();
try {
	int r = rma.pay(request);
	if (r>0) {
		if (r==1)
			out.print(SkinUtil.makeInfo(request, "送分成功！"));
		else
			out.print(SkinUtil.makeInfo(request, "分数已送完，贴子已结贴！"));
	}
	else
		out.print(SkinUtil.makeErrMsg(request, "送分失败！"));
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
}
%>