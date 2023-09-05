<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<!DOCTYPE html>
<html>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>表单域选择</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
.delSpan {
	font-size:18px;
	margin-left:10px;
	color:red;
	cursor:pointer;
}
</style>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script language="JavaScript">
function sel() {
	// dialogArguments.setField(o("field").value, $("#field").find("option:selected").text());
	var fields = "";
	var fieldNames = "";
	$("#selDiv").children().each(function() {
		if (fields=="") {
			fields = $(this).attr("val");
			fieldNames = $(this).attr("text");
		}
		else {
			fields += "," + $(this).attr("val");
			fieldNames += "," + $(this).attr("text");
		}
	});
	
	if (fields=="") {
		fields = o("field").value;
		fieldNames = $("#field").find("option:selected").text()
	}
	
	window.opener.setField(fields, fieldNames);
	window.close();
}

function selNest() {
	window.opener.setField(o("fieldNest").value, $("#fieldNest").find("option:selected").text());
	window.close();
}
</script>
</HEAD>
<BODY>
<table width="273" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent98" id="mainTable" style="margin-top:3px;">
  <thead>
  <tr>
    <td height="22" align="center">选择表单域</td>
  </tr>
  </thead>
  <tr>
    <td height="22" align="center">
    <%
	String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
	Leaf lf = new Leaf();
	lf = lf.getLeaf(flowTypeCode);
	FormDb fd = new FormDb();
	fd = fd.getFormDb(lf.getFormCode());
	Vector v = fd.getFields();
	Iterator ir = v.iterator();
	String options = "";
	while (ir.hasNext()) {
		FormField ff = (FormField) ir.next();
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			if (ff.getMacroType().equals("nest_table")) {
				continue;
			}
		}
		options += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
	}
	%>
    <select id="field" name="field">
    <%=options%>
    </select>
    &nbsp;
    <input type="button" class="btn" value="增加" onclick="addField()" />
    <div id="selDiv"></div>
    </td>
  </tr>
  <tr>
    <td height="22" align="center">
    <input type="button" class="btn" value="确定" onclick="sel()" />
    </td>
  </tr>
</table>
<%
// 取出嵌套表
options = "";
ir = v.iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
		// System.out.println(getClass() + " ff.getMacroType()=" + ff.getMacroType());
		if (ff.getMacroType().equals("nest_table") || ff.getMacroType().equals("nest_sheet")) {
			String nestFormCode = ff.getDefaultValue();
			try {
				String defaultVal = StrUtil.decodeJSON(ff.getDescription());
				JSONObject json = new JSONObject(defaultVal);
				nestFormCode = json.getString("destForm");
			} catch (JSONException e) {
				// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
			}

			
			FormDb nestfd = new FormDb();
			nestfd = nestfd.getFormDb(nestFormCode);
			
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(nestFormCode);
			
			String[] fields = msd.getColAry(false, "list_field");
			String listField = "," + StrUtil.getNullStr(StringUtils.join(fields, ",")) + ",";
			Iterator ir2 = nestfd.getFields().iterator();
			while (ir2.hasNext()) {
				FormField ff2 = (FormField)ir2.next();
				// 判断是否在模块中已设置为显示于列表中
				// if (listField.indexOf("," + ff2.getName() + ",")!=-1) {
					options += "<option value='nest." + ff2.getName() + "'>" + ff2.getTitle() + "(嵌套表)</option>";
				// }
			}
			break;
		}
	}
}
%>
<table align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent98" style="margin-top:3px;">
  <thead>
  <tr>
    <td height="22" align="center">选择嵌套表单域</td>
  </tr>
  </thead>
  <tr>
    <td height="22" align="center">
    <select id="fieldNest" name="fieldNest">
    <%=options%>
    </select>
    </td>
  </tr>
  <tr>
    <td height="22" align="center">
    <input type="button" class="btn" value="确定" onclick="selNest()" />
    </td>
  </tr>
</table>
</BODY>
<script>
var c = 0;
function addField() {
	var val = o("field").value;
	var text = $("#field").find("option:selected").text();
	$("#selDiv").html($("#selDiv").html() + "<div id='fieldDiv" + c + "' val='" + val + "' text='" + text + "'>" + text + "<span class='delSpan' onclick=\"$('#fieldDiv" + c + "').remove()\">×</span></div>");
	c++;
}

</script>
</HTML>