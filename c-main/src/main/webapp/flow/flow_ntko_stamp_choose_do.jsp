<%@ page contentType="text/html;charset=utf-8" %><%@page import="com.redmoon.oa.post.*"%><%@page import="com.redmoon.oa.stamp.*"%><%@page import="com.redmoon.oa.person.UserDb"%><%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%><%@page import="net.sf.json.*"%><%@page import="cn.js.fan.util.*"%><%@page import="com.redmoon.oa.pvg.Privilege"%><%@page import="com.redmoon.oa.person.UserDb"%><%@page import="com.redmoon.oa.dept.*"%><%@page import="java.util.*"%><%
String op = ParamUtil.get(request, "op");
boolean re = false;
JSONObject json = new JSONObject();
String action = ParamUtil.get(request, "action");
String pwd = ParamUtil.get(request, "pwd");
int stampId = ParamUtil.getInt(request, "stampId",-1);
if (op.equals("getstamp")) {
    StampDb sd = new StampDb(stampId);
    UserDb userDb = new UserDb();
	Privilege privilege = new Privilege();
	userDb = userDb.getUserDb(privilege.getUser(request));
    if (sd == null || !sd.isLoaded()) {
    	json.put("ret",-1);
    	out.print(json.toString());
       return;
    }
    if (!sd.canUserUse(userDb.getName())) {
    	json.put("ret",-2);
    	out.print(json.toString());
		return;
	}
	if (userDb.getPwdRaw().equals(pwd)) {
		StampLogDb sld = new StampLogDb();
		sld.create(new com.cloudwebsoft.framework.db.JdbcTemplate(), new Object[]{new Long(com.redmoon.oa.db.SequenceManager.nextID(com.redmoon.oa.db.SequenceManager.OA_STAMP_LOG)),userDb.getName(),new Integer(stampId),new java.util.Date(),StrUtil.getIp(request)});
		json.put("ret",0);
		String link = sd.getImageUrl(request);
		json.put("link",link);
		out.print(json.toString());
		return;
	}else{
		json.put("ret",-3);
    	out.print(json.toString());
		return;
	}

} 
%>
