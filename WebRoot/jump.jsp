<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.*"
%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" /><%
String fromWhere = ParamUtil.get(request, "fromWhere");
String toWhere = ParamUtil.get(request, "toWhere");
String action = ParamUtil.get(request, "action");
String rootpath = request.getContextPath();
String rootid = ParamUtil.get(request, "rootid");

try {
	if (privilege.jump(request, response, fromWhere, toWhere)) {
		if (toWhere.equals("forum")) {
			if (action.equals("usercenter"))
				response.sendRedirect(rootpath + "/usercenter.jsp");
			else if (action.equals("wap")) {
				response.sendRedirect(rootpath + "/wap/forum/index.jsp");
			}
			else if (action.equals("board")) {
				String boardcode = ParamUtil.get(request, "boardcode");
				response.sendRedirect(rootpath + "/forum/listtopic.jsp?boardcode=" + StrUtil.UrlEncode(boardcode));
			}
			else {
				if (rootid.equals(""))
					response.sendRedirect(rootpath + "/forum/index.jsp");
				else
					response.sendRedirect(rootpath + "/forum/showtopic.jsp?rootid=" + rootid);				
			}
		}
		else if (toWhere.equals("blog")) {
			if (rootid.equals(""))
				response.sendRedirect(rootpath + "/blog/index.jsp");
			else
				response.sendRedirect(rootpath + "/blog/showblog.jsp?rootid=" + rootid);			
		}
		else if (toWhere.equals("oa"))
			response.sendRedirect(rootpath + "/oa.jsp");
	}
}
catch (ErrMsgException e) {
	out.print(StrUtil.Alert(e.getMessage()));
}
%>

