<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>饼图</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
	out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
	return;
}

int id = ParamUtil.getInt(request, "id" , -1);
if (id == -1) {
	out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "id不能为空！"));
	return;
}
FormQueryDb aqd = new FormQueryDb();
aqd = aqd.getFormQueryDb(id);

String formCode = aqd.getTableCode();
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String fieldDesc = aqd.getChartPie();
boolean isSeted = !fieldDesc.equals("");
String fieldCodeDb = "";
String fieldOptDb = "";
String calcFieldCode = "";
String calcFunc = "0";

if (isSeted) {
	String[] ary = StrUtil.split(fieldDesc, ";");
	
	fieldCodeDb = ary[0];
	fieldOptDb = ary[1];
	fieldOptDb = "," + fieldOptDb + ",";
	
	if (ary.length>2) {
		calcFieldCode = ary[2];
		calcFunc = ary[3];
	}
}

FormQueryConditionDb aqcd = new FormQueryConditionDb();
String sql = FormSQLBuilder.getFormQueryCondition(id);

String op = ParamUtil.get(request, "op");
if (op.equals("set")) {
	String field = ParamUtil.get(request, "field");
	if (field.equals("")) {
		out.print(StrUtil.jAlert_Back("请选择字段！","提示"));
		return;
	}
	String[] ary = ParamUtil.getParameters(request, "field" + field);
	if (ary==null) {
		out.print(StrUtil.jAlert_Back("请选择字段选项！","提示"));
		return;
	}
	
	String pieDesc = field;
	int len = ary.length;
	String opt = "";
	for (int i=0; i<len; i++) {
		if (opt.equals(""))
			opt = ary[i];
		else
			opt += "," + ary[i];
	}
	
	calcFieldCode = ParamUtil.get(request, "calcFieldCode");
	calcFunc = ParamUtil.get(request, "calcFunc");
	
	aqd.setChartPie(pieDesc + ";" + opt + ";" + calcFieldCode + ";" + calcFunc);
	
	if (aqd.save()) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_query_chart_pie.jsp?id=" + id));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
else if (op.equals("clear")) {
	aqd.setChartPie("");
	if (aqd.save()) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_query_chart_pie.jsp?id=" + id));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}
%>
<%@ include file="form_query_chart_nav.jsp"%>
<script>
$("menu0").className="current"; 
</script>
<div class="spacerH"></div>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td valign="top" background="images/tab-b-back.gif">
	<form name="form1" action="?op=set" method="post">
	<table class="tabStyle_1 percent98" width="97%" border="0" align="center" cellpadding="2" cellspacing="0" >
          <tr>
            <td height="24" class="tabStyle_1_title" ><%=aqd.getQueryName()%>&nbsp;-&nbsp;饼图配置</td>
          </tr>
          <tr>
            <td height="24" ><br />
			  <strong>请指定选择型条件字段：</strong><br />
			<%
			Iterator ir = aqcd.list(sql).iterator();
			HashMap m = new HashMap();
			MacroCtlMgr mm = new MacroCtlMgr();
			SelectMgr sm = new SelectMgr();
			
			int k = 0;
			while (ir.hasNext()) {
				aqcd = (FormQueryConditionDb)ir.next();
				String conditionFieldCode = aqcd.getConditionFieldCode();
				
				FormField ff = fd.getFormField(aqcd.getConditionFieldCode());
				String fieldCode = ff.getName();
				
				// System.out.println(getClass() + " " + tableShortCode + "--" + fieldCode);
				// 避免象select类型字段有或者条件时，存在多条记录的情况
				if (m.containsKey(aqcd.getConditionFieldCode()))
					continue;
				m.put(aqcd.getConditionFieldCode(), "");
				if (aqcd.getConditionType().equals("SELECTED")) {
					boolean isChecked = false;
					if (isSeted && conditionFieldCode.equals(fieldCodeDb))
						isChecked = true;
			%>
					<input <%=isChecked?"checked":""%> name="field" value="<%=fieldCode%>" type="radio"><%=ff.getTitle()%><BR />
					<div style="border:1px dotted #cccccc">
					<%
						String[][] r = null; // bdm.getOptions(tfi.getFieldCode());
						if(ff.getType().equals(FormField.TYPE_SELECT)) {
							r = FormParser.getOptionsArrayOfSelect(fd, ff);
						}
						else {
							if(ff.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
								if (mu.getCode().equals("macro_flow_select")) {
									SelectDb sd = sm.getSelect(ff.getDefaultValue());				   
									if (sd.getType() == SelectDb.TYPE_LIST) {
										Vector v = sd.getOptions(new JdbcTemplate());
										Iterator irsel = v.iterator();
										r = new String[v.size()][2];
										int i = 0;
										while (irsel.hasNext()) {
											SelectOptionDb sod = (SelectOptionDb) irsel.next();
											r[i][0] = sod.getName();
											r[i][1] = sod.getValue();
											i ++;
										}
									}									
								}
							}
						}

						if (r==null)
							continue;
						int len = r.length;
						for (int i=0; i<len; i++) {
							String fieldOptChecked = "";
							if (isSeted) {
								if (fieldOptDb.indexOf("," + r[i][1] + ",")!=-1)
									fieldOptChecked = "checked";
							}
							else
								fieldOptChecked = "checked";
								
						%>
						<input <%=fieldOptChecked%> name="field<%=fieldCode%>" type="checkbox" value="<%=r[i][1]%>" /><%=r[i][0]%>
						<%}%>
					</div>
				<%
					k++;
				}
			}
			
			if (k==0) {%>
				<BR><font color="red">在查询字段中未找到选择型字段，选择型字段的值是通过下拉菜单选择的</font>
			<%}
			%>
			</td>
          </tr>
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
            (如果为“无”，则表示统计记录条数)
            </td>
          </tr>
          <tr>
            <td height="24" align="left" >计算方法：
            <input id="calcFunc" type="radio" name="calcFunc" value="0" checked />求和
            <input id="calcFunc" type="radio" name="calcFunc" value="1" />求平均值
            <script>
			o("calcFieldCode").value = "<%=calcFieldCode%>";
			setRadioValue("calcFunc", "<%=calcFunc%>");
			</script>
            </td>
          </tr>
          <tr>
            <td height="24" align="center" >
			<input class="btn" type="submit" value="确定" />
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input class="btn" type="button" value="清除设置" onclick="jConfirm('您确定要清除么？','提示',function(r){if(!r){return;}else{window.location.href='form_query_chart_pie.jsp?op=clear&id=<%=id%>'}}) " />
			&nbsp;&nbsp;&nbsp;&nbsp;
			<input class="btn" <%=!isSeted?"disabled":""%> type="button" value="预览报表" onclick="window.open('form_query_chart_pie_show.jsp?id=<%=id%>')" />
			<input name="id" value="<%=id%>" type="hidden" />			</td>
          </tr>
      </table>
    </form>
    </td>
  </tr>
</table>
</body>
</html>
