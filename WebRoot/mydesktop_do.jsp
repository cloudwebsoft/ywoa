<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.cloudwebsoft.framework.base.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String action = ParamUtil.get(request,"action");
String priv="read";
if (privilege.isUserPrivValid(request, priv))
	;
else {
	// 防止因未分配计划权限，而ajax处理时在parseJSON的时候报异常，如workplan_task中评价时
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("openWidget")) {
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(privilege.getUser(request));

	String id = ParamUtil.get(request, "id");
	String left = ParamUtil.get(request, "left");
	String top = ParamUtil.get(request, "top");
	
	JSONObject json;
	if (!usd.getMydesktopProp().equals(""))
		json = new JSONObject(usd.getMydesktopProp());
	else
		json = new JSONObject();
	JSONArray ary = null;
	try {
		ary = (JSONArray)json.get("widgets");
	}
	catch (Exception e) {
	}
	
	if (ary==null) {
		ary = new JSONArray();
		json.put("widgets", ary);
	}
	JSONObject jsonToFind = null;
	for (int i=0; i<ary.length(); i++) {
		JSONObject wjson = (JSONObject)ary.get(i);
		if (wjson.get("id").equals(id)) {
			jsonToFind = wjson;
			break;
		}
	}
	if (jsonToFind==null) {
		jsonToFind = new JSONObject();
		jsonToFind.put("id", id);
		jsonToFind.put("left", left);
		jsonToFind.put("top", top);
		ary.put(jsonToFind);
	}
	else {
		jsonToFind.put("left", left);
		jsonToFind.put("top", top);
	}
		
	boolean re = false;
	usd.setMydesktopProp(json.toString());
	re = usd.save();
	
	json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}	
	out.print(json);
	return;
}
else if (op.equals("closeWidget")) {
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(privilege.getUser(request));
	
	String id = ParamUtil.get(request, "id");
	
	JSONObject json;
	if (usd.getMydesktopProp().equals("")) {
		json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);		
		return;
	}
	
	json = new JSONObject(usd.getMydesktopProp());	
	
	JSONArray ary = null;
	try {
		ary = (JSONArray)json.get("widgets");
	}
	catch (Exception e) {
	}
	
	if (ary==null) {
		json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);				
		return;
	}
	JSONObject wjson = null;
	JSONArray list = new JSONArray();     
	for (int i=0; i<ary.length(); i++) {
		wjson = (JSONObject)ary.get(i);
		if (wjson.get("id").equals(id)) {
			// ary.remove(i);
			continue;
		}
        list.put(ary.get(i));
	}
	json.put("widgets", list);

	boolean re = false;
	usd.setMydesktopProp(json.toString());
	re = usd.save();
	
	json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}	
	out.print(json);
	return;
}
else if (op.equals("moveWidget")) {
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(privilege.getUser(request));

	String id = ParamUtil.get(request, "id");
	String left = ParamUtil.get(request, "left");
	String top = ParamUtil.get(request, "top");
	
	JSONObject json;
	if (!usd.getMydesktopProp().equals(""))
		json = new JSONObject(usd.getMydesktopProp());
	else
		json = new JSONObject();
	JSONArray ary = null;
	try {
		ary = (JSONArray)json.get("widgets");
	}
	catch (Exception e) {
	}
	
	if (ary==null) {
		ary = new JSONArray();
		json.put("widgets", ary);
	}
	JSONObject wjson = null;
	for (int i=0; i<ary.length(); i++) {
		wjson = (JSONObject)ary.get(i);
		if (wjson.get("id").equals(id))
			break;
	}
	if (wjson==null) {
		wjson = new JSONObject();
		wjson.put("id", id);
		wjson.put("left", left);
		wjson.put("top", top);
		ary.put(wjson);
	}
	else {
		wjson.put("left", left);
		wjson.put("top", top);
	}
	boolean re = false;
	usd.setMydesktopProp(json.toString());
	re = usd.save();
	
	json = new JSONObject();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
	}	
	out.print(json);
	return;
}
%>