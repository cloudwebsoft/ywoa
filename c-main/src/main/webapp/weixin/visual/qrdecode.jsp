<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%><%@page import="com.redmoon.oa.android.Privilege"%><%@page import="cn.js.fan.util.ParamUtil"%><%@page import="org.json.*"%><%@page import="cn.js.fan.util.StrUtil"%><%
String qrcodeData = ParamUtil.get(request, "qrCode");
String param = ParamUtil.get(request, "param");
System.out.println(getClass() + " " + qrcodeData + " param=" + param);
if (qrcodeData.startsWith("{")) {
	String skey = ParamUtil.get(request, "skey");
	try {
		String queryStr = "";
		JSONObject json = new JSONObject(qrcodeData);
		Iterator ir = json.keys();
		while (ir.hasNext()) {
			String key = (String)ir.next();
			String val = String.valueOf(json.get(key));
			if (queryStr.equals("")) {
				queryStr = key + "=" + StrUtil.UrlEncode(val);
			}
			else {
				queryStr += "&" + key + "=" + StrUtil.UrlEncode(val);
			}
		}
		
		String url = "module_add_edit.jsp?skey=" + skey + "&" + queryStr;
		
		if (json.has("formCode")) {
			String formCode = json.getString("formCode");
			if ("contractor_device".equals(formCode)) {
				url = "../flow/flow_dispose.jsp?code=device_check&name=" + json.getLong("id");
			}
		}
		
		response.sendRedirect(url);
	} catch (JSONException e) {
		e.printStackTrace();
	}
}
%>