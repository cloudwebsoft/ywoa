<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.WorkflowDb"%>
<%@ page import = "com.redmoon.oa.flow.Leaf"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%--
- 功能描述：查看嵌套表格
- 访问规则：
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
--%>
<%
com.redmoon.oa.android.Privilege pvg = new com.redmoon.oa.android.Privilege();
String skey = ParamUtil.get(request, "skey");
boolean re = pvg.Auth(skey);
if(re){
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

pvg.doLogin(request, skey);

/**if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}*/

int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
WorkflowDb wf = new WorkflowDb();
wf = wf.getWorkflowDb(flowId);

Leaf lf = new Leaf();
lf = lf.getLeaf(wf.getTypeCode());
String parentFormCode = lf.getFormCode();
if (parentFormCode.equals("")) {
	out.print("嵌套表参数：父模块编码为空！");
	return;
}

FormDb parentFd = new FormDb();
parentFd = parentFd.getFormDb(lf.getFormCode());
if (!parentFd.isLoaded()) {
	out.print("主表单不存在！");
	return;
}

com.redmoon.oa.flow.FormDAO fdaoParent = new com.redmoon.oa.flow.FormDAO();
fdaoParent = fdaoParent.getFormDAO(flowId, parentFd);

// 注意此处不同于nest_table_view控件，需取得相关联的父表单字段的值，nest_table_view控件只会关联cwsId即父表单记录的id
String cwsId = String.valueOf(fdaoParent.getId());

// 20131123 fgf 添加nestFieldName，因为其中存储了"选择"按钮需要的配置信息
String formCode = null;
String nestFieldName = ParamUtil.get(request, "nestFieldName");
JSONObject json = null;
JSONArray mapAry = new JSONArray();
int queryId = -1;
if (!nestFieldName.equals("")) {
	FormField nestField = parentFd.getFormField(nestFieldName);	
	try {
		// 20131123 fgf 添加
		String defaultVal = StrUtil.decodeJSON(nestField.getDescription());		
		formCode = defaultVal;
		json = new JSONObject(defaultVal);
		if (!json.isNull("maps"))
			mapAry = (JSONArray)json.get("maps");
		if (!json.isNull("queryId"))
			queryId = StrUtil.toInt((String)json.get("queryId"));
			
		formCode = json.getString("destForm");
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
	}
}

if (formCode==null) {
	out.print("明细表单编码为空！");
	return;	
}

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
/**ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}*/

/*
用于显示嵌套表格于父表单中，由NestTableCtl.converToHTML通过url连接调用，注意需在用到此文件的页面中，置request属性cwsId、pageType、action
*/
String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>查看明细</title>
<meta name="viewport" content="width=device-width" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
#cwsNestTable{
	font-size: 9pt;
	word-break:break-all;
	cursor: default;
	BORDER: 1px solid #cccccc;
	border-collapse:collapse;
	border-Color:#cccccc;
	align:center;
}
.cwsThead {
background-color:#51ade5;
height:20px;
}
#cwsNestTable td {
	border:1px solid #cccccc;
	height:20px;
	font-size:16px;
}
</style>
</head>
<body>
<%
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
String[] fieldsWidth = msd.getColAry(false, "list_field_width");

int len = 0;
if (fields!=null)
	len = fields.length;

String ondblclickTitle = "";
String ondblclickScript = "";
MacroCtlMgr mm = new MacroCtlMgr();
com.redmoon.oa.flow.FormMgr fm = new com.redmoon.oa.flow.FormMgr();
com.redmoon.oa.visual.FormDAO fdao = new FormDAO();

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(parentFormCode);
// System.out.println(getClass() + " parentFormCode=" + parentFormCode);
// String relateFieldValue = fdm.getRelateFieldValue(StrUtil.toInt(cwsId), formCode);

String relateFieldValue = String.valueOf(fdaoParent.getId());

String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(relateFieldValue);
sql += " order by cws_order";

//System.out.print(getClass() + " cwsId=" + cwsId + " relateFieldValue=" + relateFieldValue + " sql=" + sql);

Vector fdaoV = fdao.list(formCode, sql);
Iterator ir = fdaoV.iterator();
%>
  <table id="cwsNestTable_<%=formCode%>" class="tabStyle_1 percent98" style="margin-top: 15px;" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
	<thead>
	<tr ondblclick="<%=ondblclickScript%>" title="<%=ondblclickTitle%>" align="center" class="cwsThead">
	<%if (true) {%>
	  <td style="width:30px;">
	  ID
	  </td>
	<%}%>
	  <%
  for (int i=0; i<len; i++) {
	  String fieldName = fields[i];
	  String title = "创建者";
	  
	  if (!fieldName.equals("cws_creator")) {
		  if (fieldName.startsWith("main")) {
			  String[] ary = StrUtil.split(fieldName, ":");
			  FormDb mainFormDb = fm.getFormDb(ary[1]);
			  title = mainFormDb.getFieldTitle(ary[2]);			
		  }
		  else if (fieldName.startsWith("other")) {
			  String[] ary = StrUtil.split(fieldName, ":");
			  if (ary.length>=8) {
				  FormDb oFormDb = fm.getFormDb(ary[5]);
				  title = oFormDb.getFieldTitle(ary[7]);
			  }
			  else {
				  FormDb otherFormDb = fm.getFormDb(ary[2]);
				  title = otherFormDb.getFieldTitle(ary[4]);
			  }				
		  }
		  else {
			  title = fd.getFieldTitle(fieldName);
		  }
	  }
	  
	  FormField ff = fd.getFormField(fieldName);
	  String macroType = "";
	  if (ff.getType().equals(FormField.TYPE_MACRO)) {
		  macroType = ff.getMacroType();
	  }		
  %>
	  <td fieldName="<%=fieldName%>" macroType="<%=macroType%>" width="<%=fieldsWidth[i]%>"><%=title%></td>
	  <%}%>
	  <%if (op.equals("edit")) {%>
	  <%}%>
	</tr>
	</thead>
	<tbody>
	<%
		  int k = 0;
		  UserMgr um = new UserMgr();

		  ondblclickTitle = "";
		  ondblclickScript = "";
		  if (op.equals("edit")) {
			  // ondblclickTitle = "双击本行可以编辑数据";
		  }
		  
		  while (ir!=null && ir.hasNext()) {
			  fdao = (FormDAO)ir.next();
			  k++;
			  long id = fdao.getId();
	%>
	<tr title="<%=ondblclickTitle%>" align="center">
	<%if (true) {%>	  
	  <td editable=0 style=""><%=fdao.getId()%></td>
	<%}%>
	  <%
	  for (int i=0; i<len; i++) {
		  String fieldName = fields[i];
	  %>
	  <td align="left">
	  <%if (!fieldName.equals("cws_creator")) {
		  if (fieldName.startsWith("main")) {
			  String[] ary = StrUtil.split(fieldName, ":");
			  FormDb mainFormDb = fm.getFormDb(ary[1]);
			  com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
			  out.print(fdmMain.getFieldValueOfMain(StrUtil.toInt(cwsId), ary[2]));
		  }
		  else if (fieldName.startsWith("other:")) {
			  // System.out.println(getClass() + " fieldName=" + fieldName);
			  // String[] ary = StrUtil.split(fieldName, ":");
			  
			  // FormDb otherFormDb = fm.getFormDb(ary[2]);
			  // com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
			  // out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
			  out.print(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName));
		  }
		  else{
			  FormField ff = fd.getFormField(fieldName);
			  if (ff.getType().equals(FormField.TYPE_MACRO)) {
				  MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				  // System.out.println(getClass() + " fieldName22=" + fieldName + " ff.getType()=" + ff.getType() + " mu=" + mu);
				  if (mu != null) {
					  out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
				  }
			  }else{%>
				  <%=fdao.getFieldValue(fieldName)%>
			  <%}
		  }%>
	  <%}else{
	  %>
		  <%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>
	  <%}%>
	  </td>
	  <%}%>
	  <%if (op.equals("edit")) {%>
	  <%}%>
	</tr>
	<%
	}
  %>
  </tbody>
</table>
</body>
</html>