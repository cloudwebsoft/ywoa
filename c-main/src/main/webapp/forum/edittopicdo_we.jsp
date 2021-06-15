<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.*"
import = "cn.js.fan.web.*"
import = "com.redmoon.forum.*"
import="java.util.Calendar"
%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="form" scope="page" class="cn.js.fan.security.Form" />
<%
String op = ParamUtil.get(request, "op");
if (op.equals("changeattachname")) {
	int msgId = ParamUtil.getInt(request, "msgId");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	String newname = ParamUtil.get(request, "newname");
	String newDesc = ParamUtil.get(request, "newDesc");	
	MsgDb msgDb = new MsgDb();
	msgDb = msgDb.getMsgDb(msgId);
	boolean re = msgDb.updateAttachmentName(attach_id, newname, newDesc);
	
	if (re) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_success")));
		%>
		<script>
		// window.parent.location.reload(true);
		</script>
		<%
	}
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	
	return;
}

if (op.equals("delAttach")) {
	int msgId = ParamUtil.getInt(request, "msgId");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	MsgDb msgDb = new MsgDb();
	msgDb = msgDb.getMsgDb(msgId);
	boolean re = msgDb.delAttachment(attach_id);
	if (re) {
		%>
		<script>
		if (window.confirm("<%=SkinUtil.LoadString(request, "res.label.forum.deltopic", "del_attach_success")%>"))
			window.parent.location.reload(true);
		</script>
		<%
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));

	return;
}

if (op.equals("changeAttachOrders")) {
	int msgId = ParamUtil.getInt(request, "id");
	int attachId = ParamUtil.getInt(request, "attachId");
	String direction = ParamUtil.get(request, "direction");
	// 取得第一页的内容
	MsgDb dc = new MsgDb();
	dc = dc.getMsgDb(msgId);
	boolean re = dc.moveAttachment(attachId, direction);		
	if (re) {
		%>
		<script>
		if (window.confirm("<%=SkinUtil.LoadString(request, "res.label.forum.deltopic", "change_attach_order_success")%>"))
			window.parent.location.reload(true);
		</script>
		<%
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	
	return;
}

boolean isSuccess = false;
String privurl = "";
String boardcode = "";
boolean cansubmit = true;
if (cansubmit) {
	MsgMgr Topic = new MsgMgr();
	try {
		isSuccess = Topic.editTopicWE(application, request);
		privurl = Topic.getprivurl();
		boardcode = Topic.getCurBoardCode();
	}
	catch (ErrMsgException e) {
		out.println("-" + SkinUtil.LoadString(request, "info_op_fail") + e.getMessage());
	}
}
if (isSuccess)
{%>+<lt:Label key="info_op_success"/><%}%>

