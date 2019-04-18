<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = ParamUtil.get(request, "formCode");
%>
<%if (!formCode.equals("")) {%>
  <div id="divCalcuField0">
  <select id="calcFieldCode" name="calcFieldCode">
  <option value="">无</option>
  <%
  FormDb fd = new FormDb();
  fd = fd.getFormDb(formCode);
  Iterator ir = fd.getFields().iterator();
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
  <select id="calcFunc" name="calcFunc">
  <option value="0">求和</option>
  <option value="1">求平均值</option>
  </select>
  <a href='javascript:;' onclick="var pNode=this.parentNode; pNode.parentNode.removeChild(pNode);">×</a>
  </div>          
<%}%>