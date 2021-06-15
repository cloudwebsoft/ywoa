<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>流程查询</title>
<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<style type="text/css"> @import url("util/jscalendar/calendar-win2k-2.css"); </style>
</head>
<body>
<%
String dirCode = ParamUtil.get(request, "dirCode");
if (dirCode.equals("")) {
    response.sendRedirect("flow/flow_list.jsp?displayMode=" + WorkflowMgr.DISPLAY_MODE_SEARCH);
	// out.print(StrUtil.p_center("<BR>请选择流程类型！"));
	return;
}
Leaf lf = new Leaf();
lf = lf.getLeaf(dirCode);
if (lf==null || !lf.isLoaded()) {
	String str = LocalUtil.LoadString(request,"res.flow.Flow","notExistNode");
	out.print(SkinUtil.makeErrMsg(request, str));
	return;
}
if (lf.getType()==lf.TYPE_NONE)
	return;

FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());
%>
<table class="tabStyle_1 percent98">
  <form name="form1" action="flow_query_result.jsp?op=queryFlow" method="post">
    <tbody>
      <tr>
        <td class="tabStyle_1_title" colspan="3"><lt:Label res="res.flow.Flow" key="workflowBasics"/></td>
      </tr>
      <tr>
        <td><lt:Label res="res.flow.Flow" key="typeProcess"/>：</td>
        <td colSpan="2">
		  <%=lf.getName()%><input type="hidden" name="typeCode" value="<%=lf.getCode()%>" /></td>
      </tr>
      <tr>
        <td><lt:Label res="res.flow.Flow" key="procStatus"/>：</td>
        <td colSpan="2"><select name="status">
            <option value="1000" selected><lt:Label res="res.flow.Flow" key="limited"/></option>
            <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
            <option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
            <option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
            <option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
            <option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
          </select></td>
      </tr>
      <tr>
        <td><lt:Label res="res.flow.Flow" key="nameProcess"/>：</td>
        <td><select name="title_cond">
            <option value="0" selected><lt:Label res="res.flow.Flow" key="contain"/></option>
            <option value="1"><lt:Label res="res.flow.Flow" key="equal"/></option>
        </select></td>
        <td><input maxLength="100" size="30" name="title"></td>
      </tr>
      <tr>
        <td><lt:Label res="res.flow.Flow" key="processStartDate"/>：</td>
        <td colSpan="2">
		  <lt:Label res="res.flow.Flow" key="from"/> <input size="10" id="fromDate" name="fromDate">
			<script type="text/javascript">
                Calendar.setup({
                    inputField     :    "fromDate",      // id of the input field
                    ifFormat       :    "%Y-%m-%d",       // format of the input field
                    showsTime      :    false,            // will display a time selector
                    singleClick    :    false,           // double-click mode
                    align          :    "Tl",           // alignment (defaults to "Bl")		
                    step           :    1                // show all years in drop-down boxes (instead of every other year as default)
                });
            </script>          
          <lt:Label res="res.flow.Flow" key="to"/> <input size="10" id="toDate" name="toDate">
			<script type="text/javascript">
                Calendar.setup({
                    inputField     :    "toDate",      // id of the input field
                    ifFormat       :    "%Y-%m-%d",       // format of the input field
                    showsTime      :    false,            // will display a time selector
                    singleClick    :    false,           // double-click mode
                    align          :    "Tl",           // alignment (defaults to "Bl")		
                    step           :    1                // show all years in drop-down boxes (instead of every other year as default)
                });
            </script>           
          </td>
      </tr>
      <tr>
        <td><lt:Label res="res.flow.Flow" key="processEndSate"/>：</td>
        <td colSpan="2"><lt:Label res="res.flow.Flow" key="from"/>
          <input size="10" id="fromEndDate" name="fromEndDate" />
			<script type="text/javascript">
                Calendar.setup({
                    inputField     :    "fromEndDate",      // id of the input field
                    ifFormat       :    "%Y-%m-%d",       // format of the input field
                    showsTime      :    false,            // will display a time selector
                    singleClick    :    false,           // double-click mode
                    align          :    "Tl",           // alignment (defaults to "Bl")		
                    step           :    1                // show all years in drop-down boxes (instead of every other year as default)
                });
            </script>          
			<lt:Label res="res.flow.Flow" key="to"/>
          <input size="10" id="toEndDate" name="toEndDate" />
			<script type="text/javascript">
                Calendar.setup({
                    inputField     :    "toEndDate",      // id of the input field
                    ifFormat       :    "%Y-%m-%d",       // format of the input field
                    showsTime      :    false,            // will display a time selector
                    singleClick    :    false,           // double-click mode
                    align          :    "Tl",           // alignment (defaults to "Bl")		
                    step           :    1                // show all years in drop-down boxes (instead of every other year as default)
                });
            </script>    
        </td>
      </tr>
      <tr>
        <td colSpan="3" align="center"><input class="btn"  type="submit" value="<lt:Label res='res.flow.Flow' key='query'/>">&nbsp;&nbsp;&nbsp;&nbsp; 
          <input type="reset" value="<lt:Label res='res.flow.Flow' key='reset'/>" name="back1" class="btn"></td>
      </tr>
  </form>
	  <form method="post" name=form2 action="flow_query_result.jsp?op=queryForm">
      <tr>
        <td class="tabStyle_1_title" colspan="3"><lt:Label res='res.flow.Flow' key='formData'/>（<lt:Label res='res.flow.Flow' key='formName'/><%=fd.getName()%>）</td>
	  </tr>
<%
	MacroCtlMgr mm = new MacroCtlMgr();
	
	Iterator ir = fd.getFields().iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		if (!ff.isCanQuery())
			continue;
%>
		<tr>
            <td><%=ff.getTitle()%>：</td>
		    <td>
			  <%if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {%>
				<lt:Label res='res.flow.Flow' key='from'/> <input size="10" id="<%=ff.getName()%>FromDate" name="<%=ff.getName()%>FromDate">
				<script type="text/javascript">
                    Calendar.setup({
                        inputField     :    "<%=ff.getName()%>FromDate",      // id of the input field
                        ifFormat       :    "%Y-%m-%d",       // format of the input field
                        showsTime      :    false,            // will display a time selector
                        singleClick    :    false,           // double-click mode
                        align          :    "Tl",           // alignment (defaults to "Bl")		
                        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
                    });
                </script>                 
		  	  <%}else{%>
				<select name="<%=ff.getName()%>_cond">
				  <option value="1"><lt:Label res='res.flow.Flow' key='equal'/></option>
				  <%if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR || ff.getFieldType()==FormField.FIELD_TYPE_TEXT || ff.getType().equals(ff.TYPE_MACRO)) {%>
				  <option value="0" selected><lt:Label res='res.flow.Flow' key='contain'/></option>
				  <%}
				  else if (ff.getFieldType()!=FormField.FIELD_TYPE_BOOLEAN) {
				  %>
				  <option value=">=" selected>>=</option>
				  <option value=">" selected>></option>
				  <option value="<=" selected><=</option>
				  <option value="&lt;" selected><</option>
				  <%}%>
				</select>
			 <%}%></td>
		    <td>
			<%if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {%>
          		<lt:Label res='res.flow.Flow' key='to'/>
                <input size="10" id="<%=ff.getName()%>ToDate" name="<%=ff.getName()%>ToDate">
				<script type="text/javascript">
                    Calendar.setup({
                        inputField     :    "<%=ff.getName()%>ToDate",      // id of the input field
                        ifFormat       :    "%Y-%m-%d",       // format of the input field
                        showsTime      :    false,            // will display a time selector
                        singleClick    :    false,           // double-click mode
                        align          :    "Tl",           // alignment (defaults to "Bl")		
                        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
                    });
                </script>                 
			<%}else if(ff.getType().equals(FormField.TYPE_MACRO)) {
			   	MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null)
					out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
				// 以下不用显示，因为当宏控件不存在时下面的ff.getTypeDesc()中描述
				// else
				// 	out.print("宏控件：" + ff.getMacroType() + "不存在！");
			  }else{%>
				<input name="<%=ff.getName()%>" />
			<%}%>
		    (<%=ff.getTypeDesc()%>、<%=ff.getFieldTypeDesc()%>)</td>
	    </tr>
		  <%}%>
        <tr>
            <td align="left"><lt:Label res='res.flow.Flow' key='total'/>：</td>
            <td colSpan="2" align="left">
            <div>
            <span id="sumDiv">
            <select name="sumField">
            <option value=""><lt:Label res='res.flow.Flow' key='wu'/></option>
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
				<%
				}
			}
			%>
            </select>
            </span>
            <input type="button" class="btn" value="<lt:Label res='res.flow.Flow' key='increase'/>" onclick="addSumField()" />
            </div>
            <div id="moreDivField"></div>
            </td>
        </tr>
        <tr>
        <td colSpan="3" align="center"><input class="btn" type="submit" value="<lt:Label res='res.flow.Flow' key='query'/>">&nbsp;&nbsp;&nbsp;&nbsp; 
          <input type="hidden" name="typeCode" value="<%=lf.getCode()%>" class="btn" /></td>
      </tr>
    </tbody>
  </form>
</table>
</body>
<script>
function addSumField() {
	moreDivField.innerHTML += "<div>" + sumDiv.innerHTML + "</div>";
}
</script>
</html>
