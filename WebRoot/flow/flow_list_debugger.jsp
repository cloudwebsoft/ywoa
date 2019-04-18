<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import="rtx.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>流程调试  - 节点列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

if (op.equals("test")) {
	long myActionId = ParamUtil.getLong(request, "myActionId");
	MyActionDb mad = new MyActionDb();
	mad = mad.getMyActionDb(myActionId);	
	String userName = mad.getUserName();
	UserDb user = new UserDb();
	user = user.getUserDb(userName);
	String mainPage = "../flow_dispose.jsp?myActionId=" + myActionId;
	// 置 session中的调试标志
	Privilege.setAttribute(request, Privilege.SESSION_OA_FLOW_TESTER, privilege.getUser(request));
	privilege.doLogin(request, user.getName(), user.getPwdMD5());
	%>
	<script>
	// window.top.location.href = "../oa.jsp?mainTitle=<%=StrUtil.UrlEncode("待办流程")%>&mainPage=<%=mainPage%>";
	window.top.location.href = "<%=mainPage%>";
	</script>
	<%
	//response.sendRedirect("../oa.jsp?mainTitle=" + StrUtil.UrlEncode("待办流程") + "&mainPage=" + mainPage);
	return;
}

long myActionId = ParamUtil.getLong(request, "myActionId");
MyActionDb mad = new MyActionDb();
mad = mad.getMyActionDb(myActionId);
long actionId = mad.getActionId();
WorkflowActionDb wad = new WorkflowActionDb();
wad = wad.getWorkflowActionDb((int)actionId);

WorkflowDb wf = new WorkflowDb();
wf = wf.getWorkflowDb((int)mad.getFlowId());

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String flowExpireUnit = cfg.get("flowExpireUnit");
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class="tdStyle_1">流程调试</TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<table width="100%" class="tabStyle_1 percent80" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" class="tabStyle_1_title">请选择节点</td>
  </tr>
    <tr>
      <td width="80%">
      <table id="designerTable" width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td align="center">
            <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" codebase="../activex/cloudym.CAB#version=1,3,0,0" style="width:0px; height:0px;">
                <param name="Workflow" value="<%=wf.getFlowString()%>" />
                <param name="Mode" value="view" />
                <param name="CurrentUser" value="<%=privilege.getUser(request)%>" />
                <param name="ExpireUnit" value="<%=flowExpireUnit%>">
				  <%
                  com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();	  
                  %>
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />            
		          <param name="LicenseType" value="<%=license.getType()%>" />                        
            </object>
            </td>
          </tr>
        </table></td>
      <td valign="top">
	  <strong>下一节点</strong>：<BR />
	  <%
		UserDb user = new UserDb();
        Iterator ir = wad.getLinkToActions().iterator();
		while (ir.hasNext()) {
			WorkflowActionDb nextwad = (WorkflowActionDb)ir.next();
			Iterator irmad = mad.getActionDoing(nextwad.getId()).iterator();
			%>
       		<%=nextwad.getTitle()%>&nbsp;:&nbsp;
        	<%
			while (irmad.hasNext()) {
				MyActionDb nextmad = (MyActionDb)irmad.next();
				%>
        		<a href="flow_list_debugger.jsp?op=test&myActionId=<%=nextmad.getId()%>"><%=user.getUserDb(nextmad.getUserName()).getRealName()%></a>
      		<%
			}
			%>
			<BR/>
			<%
		}
      	%>
        <br />
        <strong>全部待办节点</strong>&nbsp;：<br />
        <%
		Iterator irmad = mad.getFlowDoingWithoutAction(mad.getFlowId()).iterator();
		while (irmad.hasNext()) {
			MyActionDb nextmad = (MyActionDb)irmad.next();
			WorkflowActionDb nextwad = wad.getWorkflowActionDb((int)nextmad.getActionId());			
			%>
			<%=nextwad.getTitle()%>&nbsp;:&nbsp;
			<a href="flow_list_debugger.jsp?op=test&myActionId=<%=nextmad.getId()%>"><%=user.getUserDb(nextmad.getUserName()).getRealName()%></a><br />
		<%
		}
		%>
        <br />
     	<a target="_top" href="../index.jsp"><span style="font-family:'宋体'">>>&nbsp;</span>重新登录</a>

        </td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>
    </tr>
</table> 
</body>
<script>
function ShowDesigner() {
	if (!o("Designer"))
		return;
	if (o("Designer").style.width=="0px") {
		o("Designer").style.width = "100%";
		o("Designer").style.height = "515px";
		o("Designer").style.marginTop = "10px";
	}
	else {
		o("Designer").style.width = "0px";
		o("Designer").style.height = "0px";
		o("Designer").style.marginTop = "0px";
	}
}

$(function() {
	ShowDesigner();
});
</script>
</html>