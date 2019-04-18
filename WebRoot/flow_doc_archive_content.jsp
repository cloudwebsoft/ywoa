<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb"%>
<%@ page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@ page import="com.redmoon.oa.flow.Render"%>
<%@ page import="com.redmoon.oa.flow.WorkflowMgr"%>
<%@ page import="com.redmoon.oa.flow.WorkflowActionDb"%>
<%@ page import="com.redmoon.oa.fileark.DirView"%>
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script src="inc/flow_dispose_js.jsp"></script>
<script src="inc/map.js"></script>
<script>
function getFormReportContent() {
	return formDiv.innerHTML;
}
</script>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int flowId = ParamUtil.getInt(request, "flowId");
int actionId = ParamUtil.getInt(request, "actionId");
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);
WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + flowId);
// System.out.println(getClass() + " cws_id=" + flowId);
request.setAttribute("pageType", "flow");
%>
<table width="98%" border="0" cellpadding="5" cellspacing="0" class="tableframe">
  <form id="flowForm" name="flowForm" action="flow_doc_archive_save_do.jsp" method="post" onsubmit="return flowForm_onsubmit()" target="_self">
    <tr>
      <td><%
				  int doc_id = wf.getDocId();
				  com.redmoon.oa.flow.DocumentMgr dm = new com.redmoon.oa.flow.DocumentMgr();
				  com.redmoon.oa.flow.Document doc = dm.getDocument(doc_id);
				  Render rd = new Render(request, wf, doc);
				  out.print(rd.report());
					%>      </td>
    </tr>
  </form>
</table>
