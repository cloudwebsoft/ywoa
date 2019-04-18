<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int flow_id = ParamUtil.getInt(request, "flowId");
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flow_id);

boolean isFlowManager = false;
LeafPriv lp = new LeafPriv(wf.getTypeCode());
if (privilege.isUserPrivValid(request, "admin.flow")) {
	if (lp.canUserExamine(privilege.getUser(request))) {
		isFlowManager = true;
	}
}

WorkflowRuler wr = new WorkflowRuler();
if (privilege.isUserPrivValid(request, "admin") || isFlowManager || wr.canUserModifyFlow(request, wf))
	;
else {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>修改流程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
</head>
<body>
<%@ include file="flow_modify_inc_menu_top.jsp"%>
<script>
$("menu6").className="current"; 
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60">
  <form id=form1 name="form1" action="flow_modify1_do.jsp" method=post>
    <tr>
      <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="typeProcess"/>：
        <%
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
%>
		<%=lf.getName()%>
        <input type="hidden" name="typeCode" value="<%=wf.getTypeCode()%>">
        <input type=hidden name=flowId value="<%=flow_id%>">      </td>
    </tr>
    <tr>
      <td><lt:Label res="res.flow.Flow" key="levelProcess"/>：
        <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_NORMAL%>" checked="checked" /><img src="images/general.png" align="absmiddle" />
        <lt:Label res="res.flow.Flow" key="ordi"/>
          <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_IMPORTANT%>" />
          <img src="images/important.png" align="absmiddle" />&nbsp;<lt:Label res="res.flow.Flow" key="impor"/>
          <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_URGENT%>" />
      <img src="images/urgent.png" align="absmiddle" />&nbsp;<lt:Label res="res.flow.Flow" key="emergent"/>
      <script>
	  setRadioValue("level", "<%=wf.getLevel()%>");
	  </script>
      </td>
    </tr>
    <tr>
      <td><lt:Label res="res.flow.Flow" key="nameProcess"/>：
        <input type="text" name="title" value="<%=wf.getTitle()%>" style="width:500px"></td>
    </tr>
    <tr>
      <td align="center"><input class="btn" type="submit" name="next" value='<lt:Label res="res.flow.Flow" key="sure"/>' /></td>
    </tr>
  </form>
</table>
</body>
</html>
