<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.help.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.help.DocumentMgr"/>
<%
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
		out.print(StrUtil.Alert_Back("操作成功！"));
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
//String isuploadfile = StrUtil.getNullString(request.getParameter("isuploadfile"));
try {
	//if (isuploadfile.equals("false"))
		//re = docmanager.UpdateWithoutFile(request);
	//else
		re = docmanager.Operate(application, request, privilege);
}
catch(ErrMsgException e) {
	//if (isuploadfile.equals("false")) {
	//	out.print(StrUtil.Alert(e.getMessage()));
	//}
	//else
		out.print(e.getMessage());
}
if (re) {
	String action = ParamUtil.get(request, "action");

	if (action.equals("fckwebedit_new")) {
		com.redmoon.kit.util.FileUpload fu = docmanager.getFileUpload();
		op = fu.getFieldValue("op");
		int id = StrUtil.toInt(fu.getFieldValue("id"), 0);
%>
        <link href="../common.css" rel="stylesheet" type="text/css"><BR />
<%	
		Leaf lf = new Leaf();
		lf = lf.getLeaf(docmanager.getDirCode());
		if (op.equals("edit")) {
			String dirLink = "";
			if (lf.getType()==Leaf.TYPE_LIST) {
				dirLink = "<font style='font-family:宋体'>>></font>&nbsp;&nbsp;<a href=\"" + request.getContextPath() + "/help/document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(lf.getCode()) + "\">" + lf.getName() + "</a><BR><BR>";
			}
			out.println(SkinUtil.waitJump(request, dirLink + "<a href='fwebedit_new.jsp?op=edit&id=" + id + "&dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode()) + "'>" + SkinUtil.LoadString(request,"info_op_success") + "点击此处返回</a>",3,"fwebedit_new.jsp?op=edit&id=" + id + "&dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode())));			
		}
		else {
			String pageUrl = "document_list_m.jsp?";
			Document doc = docmanager.getDocument(); // 取得新创建的文档
					
			out.print(SkinUtil.waitJump(request, "<font style='font-family:宋体'>>></font>&nbsp;<a href='" + pageUrl + "&dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode()) + "'>" + lf.getName() + "</a>", 3, pageUrl + "&dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode())));
			if (doc.getExamine()==Document.EXAMINE_NOT)
				out.print("<ol>正在等待审核中...</ol>");		
			out.print("<ol><a href='fwebedit_new.jsp?op=add&dir_code=" + docmanager.getDirCode() + "&dir_name=" + StrUtil.UrlEncode(lf.getName()) + "'>" + SkinUtil.LoadString(request,"info_op_success") + "继续添加！</a></ol>");		
		}
	}
	else {
		Document doc = docmanager.getDocument(); // 取得新创建的文档
		if (doc.getExamine()==Document.EXAMINE_NOT)
			out.print("操作成功，正在等待审核中...");		
		else
			out.print("操作成功！");
	}
}
%>