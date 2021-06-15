<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.flow.FormQueryDb" %>
<%@ page import="com.redmoon.oa.flow.query.QueryScriptUtil" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    int id = ParamUtil.getInt(request, "id", -1);
    if (id != -1) {
        FormQueryDb fqd = new FormQueryDb();
        fqd = fqd.getFormQueryDb(id);
        QueryScriptUtil qsu = new QueryScriptUtil();
        Map mapFieldTitle = qsu.getCols(request, fqd);

        Map mapFieldType = qsu.getMapFieldType();
        if (mapFieldType == null) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "查询结果错误！"));
            return;
        }
%>
<div id="divCalcuField0" style="float:left">
    <select id="calcFieldCode" name="calcFieldCode">
        <option value="">无</option>
        <%
            Iterator irField = mapFieldTitle.keySet().iterator();
            while (irField.hasNext()) {
                String fieldName = (String) irField.next();

                Integer iType = (Integer) mapFieldType.get(fieldName.toUpperCase());
                int fieldType = FormField.FIELD_TYPE_VARCHAR;
                fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());

                if (fieldType == FormField.FIELD_TYPE_INT
                        || fieldType == FormField.FIELD_TYPE_FLOAT
                        || fieldType == FormField.FIELD_TYPE_DOUBLE
                        || fieldType == FormField.FIELD_TYPE_PRICE
                        || fieldType == FormField.FIELD_TYPE_LONG
                ) {
        %>
        <option value="<%=fieldName%>"><%=mapFieldTitle.get(fieldName.toUpperCase())%>
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
<%
    }
%>