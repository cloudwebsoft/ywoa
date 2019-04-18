<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.person.UserSetupDb"%>
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
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加附言</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script src="inc/upload.js"></script>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<%
String userName1 = privilege.getUser(request);
UserSetupDb userSetupDb = new UserSetupDb();
userSetupDb = userSetupDb.getUserSetupDb(userName1);
String str = userSetupDb.getLocal();
if(str.equals("en-US")){
%>
<script id='uploadJs' src="inc/upload.js" local="en"></script>
<%} %>
</head>
<body>
<%@ include file="flow_modify_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<div class="spacerH"></div>
<form action="flow_modify.jsp?op=addAnnex&amp;flowId=<%=flow_id%>" method="post" enctype="multipart/form-data" name="addform1" id="addform1">
  <table class="tabStyle_1 percent60" width="60%">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" colspan="2"><lt:Label res="res.flow.Flow" key="addPostscript"/></td>
    </tr>
    <tr>
      <td width="80px" align="center"><lt:Label res="res.flow.Flow" key="content"/>：</td>
      <td>
          <textarea id="content" name="content" style="display:none"></textarea>
          <script>
			CKEDITOR.replace('content',
				{
					// skin : 'kama',
					toolbar : 'Middle',
					enterMode : Number(2)					
				});
		  </script>
      </td>
    </tr>
    <tr>
      <td align="center"><lt:Label res="res.flow.Flow" key="accessory"/>：</td>
      <td><script>initUpload()</script>
          <input name="flow_id" value="<%=flow_id%>" type="hidden" />
          <input name="parent_id" value="-1" type="hidden" />
          <input name="reply_name" value="<%=privilege.getUser(request)%>" type="hidden" />
          <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
      </td>
    </tr>
    <tr>
      <td align="center" colspan="2"><input class="btn" name="submit" type="submit" value='<lt:Label res="res.flow.Flow" key="sure"/>' width="80" height="20" /></td>
    </tr>
    </tbody>
  </table>
</form>
</body>
</html>
