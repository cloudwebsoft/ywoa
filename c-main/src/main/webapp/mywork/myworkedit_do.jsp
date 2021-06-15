<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="com.redmoon.oa.worklog.WorkLogMgr"%>
<%
	String  id = ParamUtil.get(request, "id");
	String  op = ParamUtil.get(request, "op");
	//String  attachId = ParamUtil.get(request, "attachId");
	if(op.equals("delAttach")){
		boolean re = false;
		WorkLogMgr workLogMgr = new WorkLogMgr();
		re = workLogMgr.delAttachment(request);
		if(re){
			out.print(StrUtil.Alert_Redirect("操作成功！","mywork_edit.jsp?id="+id));
		}else{
			out.print(StrUtil.Alert_Back("操作失败！"));
		}
		return;
	}
%>