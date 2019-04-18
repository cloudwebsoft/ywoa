<%@ page contentType="text/html; charset=utf-8" %><%@ page import="cn.js.fan.util.*"%><%@ page import="com.redmoon.oa.emailpop3.pop3.*"%><%
	response.setContentType("text/xml;charset=UTF-8");
	String email = ParamUtil.get(request, "email");
	GetMailStatus gms = GetMailStatus.getFromSession(request, email);
	int count = -1;
	int storedCount = -1;
	boolean over = false;
	if (gms!=null) {
		count = gms.getCount();
		storedCount = gms.getStoredCount();
		over = gms.isOver();
		if (over) {
			GetMailStatus.removeFromSession(request, email);
		}
	}

	String str = "";
	str += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	str += "<ret>\n";
	str += "<item>\n";
	str += "<count>" + count + "</count>\n";
	str += "<storedCount>" + storedCount + "</storedCount>\n";
	str += "<over>" + over + "</over>\n";
	str += "</item>\n";
	str += "</ret>";
	out.print(str);
%>