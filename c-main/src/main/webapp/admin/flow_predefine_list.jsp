<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>预定义流程列表</title>
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

var GetDate=""; 
function SelectDate(ObjName,FormatDate){
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);
	GetDate = showModalDialog("util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}

</script>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<script>
function err(msg){
	jAlert(msg,"提示"); 
}
</script>
<%
String priv = "admin.flow";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String dirCode = ParamUtil.get(request, "dirCode");

if (dirCode.equals("")) {
	out.print(SkinUtil.makeInfo(request, "请选择流程类型！"));
	return;
}
Leaf lf = new Leaf();
lf = lf.getLeaf(dirCode);
if (lf==null || !lf.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "节点不存在！"));
	return;
}

// 如果是分类节点，则重定向至表单处理页面
if (lf.getType()==Leaf.TYPE_NONE) {
	// response.sendRedirect("form_m.jsp?flowTypeCode=" + StrUtil.UrlEncode(lf.getCode()));
	response.sendRedirect("flow_predefine_dir.jsp?op=modify&code=" + StrUtil.UrlEncode(lf.getCode()));
	return;
}
else if (lf.getType()==Leaf.TYPE_FREE) {
	response.sendRedirect("flow_predefine_free.jsp?flowTypeCode=" + StrUtil.UrlEncode(lf.getCode()));
	return;
}
else {
	response.sendRedirect("flow_predefine_init.jsp?flowTypeCode=" + StrUtil.UrlEncode(lf.getCode()));
}
if (true)
	return;

LeafPriv lp = new LeafPriv(dirCode);
if (!(lp.canUserSee(privilege.getUser(request)))) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

WorkflowPredefineDb fd = new WorkflowPredefineDb();
		
String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	WorkflowPredefineMgr ftm = new WorkflowPredefineMgr();
	boolean re = false;
	try {
		re = ftm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (re) {
		out.print(StrUtil.jAlert("删除成功！","提示"));
	}
	else {
		out.print(StrUtil.jAlert("删除失败！","提示"));
	}
}		

if (op.equals("setDefault")) {
	int id = ParamUtil.getInt(request, "id");
	fd = fd.getWorkflowPredefineDb(id);
	fd.setDefaultFlow(true);
	if (fd.save())
		out.print(StrUtil.jAlert("操作成功！","提示"));
	else
		out.print(StrUtil.jAlert("操作失败！","提示"));
}
%>
<%@ include file="flow_inc_menu_top.jsp"%>
<form action="flow_predefine_list.jsp" method="post">
  <table width="98%" cellpadding="0" cellspacing="1" class="tabStyle_1 percent98">
    <tr>
      <td class="tabStyle_1_title" width="11%" height="25" align="center" >启用</td>
      <td class="tabStyle_1_title" width="36%" align="center" >名称</td>
      <td class="tabStyle_1_title" width="36%" align="center">类别</td>
      <td class="tabStyle_1_title" width="17%" height="25" align="center" >操作</td>
    </tr>
    <%
		WorkflowPredefineDb ftd = new WorkflowPredefineDb();
		String sql = "select id from flow_predefined where typeCode=" + StrUtil.sqlstr(dirCode);
		Iterator ir = ftd.list(sql).iterator();
		Directory dir = new Directory();
		while (ir.hasNext()) {
			ftd = (WorkflowPredefineDb) ir.next();
			%>
    <tr>
      <td width="11%" align="center" ><input type="checkbox" name="isDefault" value="true" onclick="setDefault(this, '<%=ftd.getId()%>', '<%=dirCode%>')" <%=ftd.isDefaultFlow()?"checked":""%>/></td>
      <td width="36%" ><%=ftd.getTitle()%></td>
      <td width="36%" ><%=dir.getLeaf(ftd.getTypeCode()).getName()%></td>
      <td width="17%" align="center" ><a href="flow_predefine_init.jsp?op=edit&amp;id=<%=ftd.getId()%>">修改</a>&nbsp;&nbsp;<a href="#" onclick="if (window.confirm('您确定要删除类型吗？')) window.location.href='flow_predefine_list.jsp?op=del&amp;dirCode=<%=StrUtil.UrlEncode(dirCode)%>&amp;id=<%=ftd.getId()%>'">删除</a></td>
    </tr>
    <%}%>
  </table>
</form>
<br />
<br />
</body>
<script>
function setDefault(chkObj, id, typeCode) {
	if (!chkObj.checked) {
		jAlert("请选择您需要置为默认的预定义流程！","提示");
		chkObj.checked = true;
	}
	if (chkObj.checked) {
		window.location.href = "flow_predefine_list.jsp?op=setDefault&id=" + id + "&dirCode=" + typeCode;
	}
}

</script>
</html>
