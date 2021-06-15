<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>线状图</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");
FormQueryDb aqd = new FormQueryDb();
aqd = aqd.getFormQueryDb(id);

String formCode = aqd.getTableCode();
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String fieldCodeDb = "";
String fieldOptDb = "";
String calcFieldCode = "";
String calcFunc = "0";

String fieldDesc = aqd.getChartLine();
boolean isSeted = !fieldDesc.equals("");
int beginYear = 2000;
int endYear = DateUtil.getYear(new java.util.Date());
if (isSeted) {
	String[] ary = StrUtil.split(fieldDesc, ";");
	
	fieldCodeDb = ary[0];
	fieldOptDb = ary[1];
	
	if (ary.length>2) {
		calcFieldCode = ary[2];
		calcFunc = ary[3];
	}
		
	String[] ary2 = fieldOptDb.split("-");
	beginYear = StrUtil.toInt(ary2[0]);
	endYear = StrUtil.toInt(ary2[1]);
}

FormQueryConditionDb aqcd = new FormQueryConditionDb();
String sql = FormSQLBuilder.getFormQueryCondition(id);

String op = ParamUtil.get(request, "op");
if (op.equals("set")) {
	int bYear = ParamUtil.getInt(request, "beginYear");
	int eYear = ParamUtil.getInt(request, "endYear");
	String field = ParamUtil.get(request, "field");

	if (bYear<1949 || eYear<1949) {
		out.print(StrUtil.Alert_Back("年份填写错误，请填写四位数字！"));
		return;
	}
	
	if (bYear>=eYear) {
		out.print(StrUtil.Alert_Back("开始年份需小于结束年份！"));
		return;
	}
	
	calcFieldCode = ParamUtil.get(request, "calcFieldCode");
	calcFunc = ParamUtil.get(request, "calcFunc");
		
	aqd.setChartLine(field + ";" + bYear + "-" + eYear + ";" + calcFieldCode + ";" + calcFunc);
	if (aqd.save()) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "form_query_chart_line.jsp?id=" + id));
	}
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}
else if (op.equals("clear")) {
	aqd.setChartLine("");
	if (aqd.save()) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "form_query_chart_line.jsp?id=" + id));
	}
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}
%>
<%@ include file="form_query_chart_nav.jsp"%>
<script>
$("menu2").className="current"; 
</script>
<div class="spacerH"></div>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td valign="top" background="images/tab-b-back.gif">
	<form name="form1" action="?op=set" method="post" onsubmit="return form1_onsubmit()">
	<table class="tabStyle_1 percent98" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td height="24" class="tabStyle_1_title" ><%=aqd.getQueryName()%>&nbsp;-&nbsp;柱状图配置</td>
          </tr>
          <%
			HashMap m = new HashMap();
		  	
			boolean hasDateField = false; // 是否有日期型字段
			Iterator ir = aqcd.list(sql).iterator();
			while (ir.hasNext()) {
				aqcd = (FormQueryConditionDb)ir.next();
				FormField ff = fd.getFormField(aqcd.getConditionFieldCode());

				// 避免象select类型字段有或者条件时，存在多条记录的情况
				if (m.containsKey(aqcd.getConditionFieldCode()))
					continue;
				m.put(aqcd.getConditionFieldCode(), "");
								
				if (aqcd.getConditionType().equals("SELEDATE")) {
					hasDateField = true;
					boolean isChecked = false;
					if (isSeted && ff.getName().equals(fieldCodeDb))
						isChecked = true;					
		  %>
          <tr>
            <td height="24" >
            <input <%=isChecked?"checked":""%> name="field" value="<%=ff.getName()%>" type="radio"><%=ff.getTitle()%>&nbsp;&nbsp;&nbsp;&nbsp;
            时间段：
			  从
              <input id="beginYear" name="beginYear" size="5" value="<%=isChecked?""+beginYear:""%>" />
              年至
              <input id="endYear" name="endYear" size="5" value="<%=isChecked?""+endYear:""%>" />
              年
            </td>
          </tr>
					<%
				}
			}
          %>
          <tr>
            <td height="24" align="left" >表单字段：
            <select id="calcFieldCode" name="calcFieldCode">
            <option value="">无</option>
            <%
			ir = fd.getFields().iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField)ir.next();
				if (!ff.isCanQuery())
					continue;
				if (ff.getFieldType()==FormField.FIELD_TYPE_INT
					|| ff.getFieldType()==FormField.FIELD_TYPE_FLOAT
					|| ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE
					|| ff.getFieldType()==FormField.FIELD_TYPE_PRICE
					|| ff.getFieldType()==FormField.FIELD_TYPE_LONG
					) {
				%>
				<option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                <%}				
			}
			%>
            </select>
            (如果为“无”，则表示统计记录条数) </td>
          </tr>
          <tr>
            <td height="24" align="left" >计算方法：
            <input id="calcFunc" type="radio" name="calcFunc" value="0" checked />求和
            <input id="calcFunc" type="radio" name="calcFunc" value="1" />求平均值
            <script>
			o("calcFieldCode").value = "<%=calcFieldCode%>";
			setRadioValue("calcFunc", "<%=calcFunc%>");
			</script>
            <%if (!hasDateField) {%>
            <font color="red">表单中无日期型字段，无法统计折线图</font>
            <%}%>
            </td>
          </tr>
          <tr>
            <td height="24" align="center" >
			<input class="btn" type="submit" value="确定" <%=hasDateField?"":"disabled"%> />
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input class="btn" type="button" value="清除设置" onclick="if (confirm('您确定要清除么？')) window.location.href='form_query_chart_line.jsp?op=clear&id=<%=id%>'" />
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input class="btn" <%=!isSeted?"disabled":""%> type="button" value="预览报表" onclick="window.open('form_query_chart_line_show.jsp?id=<%=id%>')" />
			<input name="id" value="<%=id%>" type="hidden" />
            </td>
          </tr>
      </table>
	  </form>    </td>
  </tr>
</table>
</body>
<script>
function form1_onsubmit() {
	if (!isNumeric(o("beginYear").value)) {
		alert("年份请填数字！");
		o("beginYear").focus();
		return false;
	}
	if (!isNumeric(o("endYear").value)) {
		alert("年份请填数字！");
		o("endYear").focus();
		return false;
	}	
}
</script>
</html>
