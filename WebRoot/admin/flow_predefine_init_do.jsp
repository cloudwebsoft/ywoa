<%@ page contentType="text/html;charset=utf-8"
import = "cn.js.fan.util.*"
import = "cn.js.fan.web.*"
import = "com.redmoon.oa.pvg.*"
import = "com.redmoon.oa.flow.*"
import = "java.io.File"
import = "com.redmoon.oa.ui.*"
import = "org.json.*"
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin.flow";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean isSuccess = false;
String typeCode = ParamUtil.get(request, "typeCode");
WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
String op = ParamUtil.get(request, "op");
if (op.equals("")) {
	try {
		isSuccess = wpm.create(request);
	}
	catch (ErrMsgException e) {
		// out.println(fchar.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
	}
	if (isSuccess) {
		// out.print(fchar.Alert_Redirect("操作成功！", "flow_predefine_init.jsp?flowTypeCode=" + StrUtil.UrlEncode(typeCode)));
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("newId", wpm.getNewId());
		out.print(json);
	}
}
else if (op.equals("edit")) {
	int id = ParamUtil.getInt(request, "id");
	try {
		isSuccess = wpm.modify(request);
	}
	catch (ErrMsgException e) {
		// out.print(fchar.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
	}
	if (isSuccess)
	{
		// out.print(fchar.Alert_Back("操作成功！"));
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);
	}
}
%>