<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="java.util.Iterator" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String formCode = ParamUtil.get(request, "formCode");
%>
<div id="divCalcuField0" style="float:left">
    <select id="calcFieldCode" name="calcFieldCode">
        <option value="">无</option>
        <%
            FormDb fd = new FormDb();
            fd = fd.getFormDb(formCode);
            Iterator irField = fd.getFields().iterator();
            while (irField.hasNext()) {
                FormField ff = (FormField) irField.next();
                int fieldType = ff.getFieldType();
                if (fieldType == FormField.FIELD_TYPE_INT
                        || fieldType == FormField.FIELD_TYPE_FLOAT
                        || fieldType == FormField.FIELD_TYPE_DOUBLE
                        || fieldType == FormField.FIELD_TYPE_PRICE
                        || fieldType == FormField.FIELD_TYPE_LONG
                ) {
        %>
        <option value="<%=ff.getName()%>"><%=ff.getTitle()%>
        </option>
        <%
                }
            }
        %>
    </select>
    <select id="calcFunc" name="calcFunc">
        <option value="0">求和</option>
        <option value="1">求平均值</option>
    </select>
    <a href='javascript:;' onclick="var pNode=this.parentNode; pNode.parentNode.removeChild(pNode);">×</a>
    &nbsp;
</div>