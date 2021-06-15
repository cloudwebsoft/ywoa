<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.UserDb"%>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.db.Paginator"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
// 用户短消息发送时选择网盘文件
String ids = ParamUtil.get(request, "ids");
if (!ids.equals("")) {
	String[] ary = StrUtil.split(ids, ",");
	Attachment att = new Attachment();
	for (int i=0; i<ary.length; i++) {
		att = att.getAttachment(StrUtil.toInt(ary[i]));
	%>
	<div id="netdiskFile<%=att.getId()%>"><input name="netdiskFiles" value="<%=att.getId()%>" type="hidden" /><%=att.getName()%>&nbsp;&nbsp;<a target="_self" href="javascript:;" onclick="o('netdiskFile<%=att.getId()%>').outerHTML=''" style="color:red; font-size:18px">×</a></div>
	<%
	}
}
%>