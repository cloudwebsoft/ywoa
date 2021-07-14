<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // 用于有sales管理权限的人员管理时
String formCode = ParamUtil.get(request, "formCode");
String formCodeRelated = ParamUtil.get(request, "formCodeRelated"); // 从模块编码
String menuItem = ParamUtil.get(request, "menuItem");
int parentId = ParamUtil.getInt(request, "parentId", -1);
if (parentId==-1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " parentId=" + parentId));
	return;
}

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "menuItem", menuItem, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "formCode", formCode, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "formCodeRelated", formCodeRelated, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCodeRelated);

MacroCtlMgr mm = new MacroCtlMgr();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计-查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

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
<%
int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
if (isShowNav==1) {
%>
<%@ include file="module_mode1_inc_menu_top.jsp"%>
<script>
o("menu<%=menuItem%>").className="current"; 
</script>
<div class="spacerH"></div>
<%}%>
<form action="module_mode1_list_relate.jsp" method="get" name="form2" id="form2">
    <table class="tabStyle_1 percent80" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td colspan="3" class="tabStyle_1_title">表单数据信息（表单名称：<%=fd.getName()%>） </td>
      </tr>
      <%
			Iterator ir = fd.getFields().iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField)ir.next();
			%>
      <tr>
        <td width="29%"><%=ff.getTitle()%>：</td>
        <td width="19%" nowrap="nowrap"><%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
          从
          <input size="20" name="<%=ff.getName()%>FromDate" id="<%=ff.getName() %>FromDate"/>
          <script>
          $(function(){
          	<%if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
          	$('#<%=ff.getName() %>FromDate').datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:true,
				format:'Y-m-d H:i:00',
				step:10
			});
          <%} else { %>
          	$('#<%=ff.getName() %>FromDate').datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:false,
				format:'Y-m-d'
			});
          <%} %>
          })
          </script>
          <%}else{%>
          <select name="<%=ff.getName()%>_cond">
            <option value="1">等于</option>
            <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(FormField.TYPE_TEXTAREA) || ff.getType().equals(FormField.TYPE_MACRO) || ff.getType().equals(FormField.TYPE_SELECT)) {%>
            <option value="0" selected="selected">包含</option>
            <%}%>
          </select>
          <%}%>
        </td>
        <td width="52%"><%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
          至
          <input size="20" name="<%=ff.getName()%>ToDate" id="<%=ff.getName()%>ToDate" />
          <script>
          $(function(){
          <%if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
          	$('#<%=ff.getName()%>ToDate').datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:true,
				format:'Y-m-d H:i:00',
				step:10
			});
          <%} else { %>
          	$('#<%=ff.getName()%>ToDate').datetimepicker({
				lang:'ch',
				datepicker:true,
				timepicker:false,
				format:'Y-m-d'
			});
          <%} %>
          })
          </script>
          <%}else if(ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
			  	}else{%>
          <input name="<%=ff.getName()%>" />
          <%}%>
          (<%=ff.getTypeDesc()%>) </td>
      </tr>
      <%}%>
      <tr>
        <td colspan="3" align="center"><input name="submit" type="submit" class="btn"  value="查  询" />
          &nbsp;&nbsp;&nbsp;
          <input type="hidden" name="op" value="search" />
          <input type="hidden" name="action" value="<%=action%>" />
          <input type="hidden" name="formCode" value="<%=formCode%>" />
          <input type="hidden" name="formCodeRelated" value="<%=formCodeRelated%>" />
          <input type="hidden" name="menuItem" value="<%=menuItem%>" />
          <input type="hidden" name="parentId" value="<%=parentId%>" />
          <input type="hidden" name="isShowNav" value="<%=isShowNav%>" />
        </td>
      </tr>
</table>
</form>
</body>
</html>