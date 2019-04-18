<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.menu.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%
	// 防跨站点请求伪造
	String callingPage = request.getHeader("Referer");
	if (callingPage == null
			|| callingPage.indexOf(request.getServerName()) != -1) {
	} else {
		Privilege privilege = new Privilege();		
		com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF ajax_online.jsp");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "op_invalid")));
		return;
	}
	
	String menuCode = ParamUtil.get(request, "menu_code");
	String userName = ParamUtil.get(request, "user_name");
	
	MostRecentlyUsedMenuDb mrum = new MostRecentlyUsedMenuDb();
	mrum.storeMenu(menuCode, userName);
%>