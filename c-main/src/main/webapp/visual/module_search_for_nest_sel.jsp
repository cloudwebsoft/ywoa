<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：模块高级查询
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
*/

String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // 用于有sales管理权限的人员管理时

String nestType = ParamUtil.get(request, "nestType");
String parentFormCode = ParamUtil.get(request, "parentFormCode");
String nestFieldName = ParamUtil.get(request, "nestFieldName");
long parentId = ParamUtil.getLong(request, "parentId", -1);

FormDb pForm = new FormDb();
pForm = pForm.getFormDb(parentFormCode);
FormField nestField = pForm.getFormField(nestFieldName);

JSONObject json = null;
try {
	// 20131123 fgf 添加
	String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
	json = new JSONObject(defaultVal);
} catch (JSONException e) {
	// TODO Auto-generated catch block
	// e.printStackTrace();
	out.print(SkinUtil.makeErrMsg(request, "JSON解析失败！"));
	return;
}

String formCode = json.getString("sourceForm");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);

MacroCtlMgr mm = new MacroCtlMgr();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计-查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../inc/flow_dispose_js.jsp"></script>
<script>
function setradio(myitem,v)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio")
              {
                 if (radioboxs[i].value==v)
				 	radioboxs[i].checked = true;
              }
          }
     }
}
</script>
</head>
<body>
<%@ include file="module_nest_sel_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<div class="spacerH"></div>
  <form action="module_list_nest_sel.jsp" method="get" name="form2" id="form2">
    <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td colspan="2" class="tabStyle_1_title">表单数据信息（表单名称：<%=fd.getName()%>） </td>
      </tr>
      <%
			Iterator ir = fd.getFields().iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField)ir.next();
			%>
      <tr>
        <td width="13%"><%=ff.getTitle()%>：</td>
        <td width="87%" nowrap="nowrap">
		<%if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {%>
		  <select name="<%=ff.getName()%>_cond" onchange="displayDateSel('<%=ff.getName()%>', this.value)">
            <option value="0">时间段</option>
            <option value="1">时间点</option>
          </select>
		  <span id="span<%=ff.getName()%>_seg">
          大于
          <input size="10" name="<%=ff.getName()%>FromDate" />
          <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>FromDate', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" />
          小于
          <input name="<%=ff.getName()%>ToDate" size="6" />
          <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>ToDate', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" />
		  </span>
		  <span id="span<%=ff.getName()%>_point" style="display:none">
		  <input name="<%=ff.getName()%>" size="6" />
          <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" />
		  </span>
        <%}
		else if (ff.getType().equals(FormField.TYPE_SELECT)) {
			String opts = FormParser.getOptionsOfSelect(fd, ff);
			opts = opts.replaceAll("selected", "");
		%>
          <select name="<%=ff.getName()%>_cond">
            <option value="1">等于</option>
          </select>
		  <select name="<%=ff.getName()%>">
		  <option value="">请选择</option>
		  <%=opts%>
		  </select>
		  <input value="或者" onclick="addOrCond(this, '<%=ff.getName()%>')" type="button">
        <%}
		else if(ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
			%>
          <select name="<%=ff.getName()%>_cond">
            <option value="1">等于</option>
            <option value="0" selected="selected">包含</option>
          </select>
			<%
			out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
		}else{%>
          <select name="<%=ff.getName()%>_cond">
            <option value="1">等于</option>
            <%if (ff.getType().equals(ff.TYPE_TEXTFIELD) || ff.getType().equals(ff.TYPE_TEXTAREA)) {%>
            <option value="0" selected="selected">包含</option>
            <%}%>
          </select>
          <input name="<%=ff.getName()%>" />
        <%}%>
          (<%=ff.getTypeDesc()%>)
		  </td>
      </tr>
      <%}%>
      <tr>
        <td colspan="2" align="center"><input name="submit" type="submit" class="BigButton"  value="查  询" />
          &nbsp;&nbsp;&nbsp;
          <input type="hidden" name="op" value="search" />
          <input type="hidden" name="action" value="<%=action%>" />
          <input type="hidden" name="formCode" value="<%=formCode%>" />
          <input type="hidden" name="nestType" value="<%=nestType%>" />
          <input type="hidden" name="parentFormCode" value="<%=parentFormCode%>" />
          <input type="hidden" name="nestFieldName" value="<%=nestFieldName%>" />
          <input type="hidden" name="parentId" value="<%=parentId%>" />
		  </td>
      </tr>
</table>
</form>
</body>
<script>
function displayDateSel(fieldName, flag) {
	if (flag=="0") {
		$("span" + fieldName + "_seg").style.display = "";
		$("span" + fieldName + "_point").style.display = "none";
	}
	else {
		$("span" + fieldName + "_seg").style.display = "none";
		$("span" + fieldName + "_point").style.display = "";
	}
}
function addOrCond(btnObj,name){
    var text = "&nbsp;或者&nbsp;<select name='" + name + "'>" + $(name).innerHTML + "</select>";
	btnObj.insertAdjacentHTML("BeforeBegin", text);
}
</script>
</html>