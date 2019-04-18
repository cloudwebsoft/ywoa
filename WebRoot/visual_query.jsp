<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<link href="common.css" rel="stylesheet" type="text/css">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // 用于有sales管理权限的人员管理时
try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String formCode = ParamUtil.get(request, "formCode");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);

MacroCtlMgr mm = new MacroCtlMgr();
%>
<title>智能设计-查询</title>
<script src="<%=Global.getRootPath()%>/js/jquery-1.9.1.min.js"></script>
<script src="<%=Global.getRootPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="<%=Global.getRootPath()%>/js/datepicker/jquery.datetimepicker.css"/>
<script src="<%=Global.getRootPath()%>/js/datepicker/jquery.datetimepicker.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>

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
<%
com.redmoon.oa.visual.Config cfg = new com.redmoon.oa.visual.Config();
String listViewPage = Global.getRootPath() + "/" + cfg.getView(formCode, "list");
RequestAttributeMgr ramVisual = new RequestAttributeMgr();
%>
  <form action="<%=listViewPage%>?op=search" method="post" name="form2" id="form2">
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
        <td width="17%"><%=ff.getTitle()%>：</td>
        <td width="83%" nowrap="nowrap">
		<%if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {%>
		  <select name="<%=ff.getName()%>_cond" onchange="displayDateSel('<%=ff.getName()%>', this.value)">
            <option value="0">时间段</option>
            <option value="1">时间点</option>
          </select>
		  <span id="span<%=ff.getName()%>_seg">
          大于
          <input id='<%=ff.getName()%>FromDate' size="10" name="<%=ff.getName()%>FromDate" />
          <!-- <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>FromDate', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
          小于
          <input id='<%=ff.getName()%>ToDate' name="<%=ff.getName()%>ToDate" size="10" />
          <!-- <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>ToDate', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
		  </span>
		  <script>
		  $(function(){
			  	$('#<%=ff.getName()%>FromDate').datetimepicker({
					lang:'ch',
					datepicker:true,
					timepicker:false,
					format:'Y-m-d'
				});
				$('#<%=ff.getName()%>ToDate').datetimepicker({
					lang:'ch',
					datepicker:true,
					timepicker:false,
					format:'Y-m-d'
				});	
			})
		  </script>
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
		  <input value="或者" onclick="addOrCond(this, '<%=ff.getName()%>')" type="button" class="btn">
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
        (<%=ff.getTypeDesc()%>) </td>
      </tr>
      <%}%>
      <tr>
        <td colspan="2" align="center"><input name="submit" type="submit" class="btn"  value="查  询" />
          &nbsp;&nbsp;&nbsp;
          <input type="hidden" name="action" value="<%=action%>" />
          <%=ramVisual.render(request)%>
          </td>
      </tr>
</table>
</form>
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
