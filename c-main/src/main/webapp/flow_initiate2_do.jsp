<%@ page contentType="text/html; charset=utf-8" %><%@ page import="com.redmoon.oa.flow.*"%><%@ page import="cn.js.fan.util.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.flow.DocumentMgr"/><%
if (!privilege.isUserLogin(request)) {
	// out.print("对不起，请先登录！");
	// return;
}
String op = ParamUtil.get(request, "op");
if (op.equals("changeattachname")) {
	int doc_id = ParamUtil.getInt(request, "doc_id");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	int page_num = ParamUtil.getInt(request, "page_num");
	String newname = ParamUtil.get(request, "newname");
	Document doc = new Document();
	doc = doc.getDocument(doc_id);
	DocContent dc = doc.getDocContent(page_num);
	boolean re = dc.updateAttachmentName(attach_id, newname);
	
	if (re) {
		out.print(StrUtil.Alert_Back("修改成功！"));
		%>
		<script>
		// window.parent.location.reload(true);
		</script>
		<%
	}
	else
		out.print(StrUtil.Alert_Back("修改失败！"));
	
	return;
}

if (op.equals("delAttach")) {
	int doc_id = ParamUtil.getInt(request, "doc_id");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	int page_num = ParamUtil.getInt(request, "page_num");
	Document doc = new Document();
	doc = doc.getDocument(doc_id);
	DocContent dc = doc.getDocContent(page_num);
	boolean re = dc.delAttachment(attach_id);
	if (re) {
		%>
		<script>
		if (window.confirm("删除成功！点击确定可刷新页面"))
			window.parent.location.reload(true);
		</script>
		<%
	}
	else
		out.print(StrUtil.Alert("删除失败！"));
	
	return;
}

boolean re = false;
try {
	re = docmanager.Operate(application, request, privilege);
}
catch(ErrMsgException e) {
	out.print(e.getMessage());
}
if (re) {
	out.print("操作成功！");
}
%>