<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.json.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.api.INestTableCtl" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>导入Excel</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%
        String parentFormCode = ParamUtil.get(request, "parentFormCode");
        String formCode = ParamUtil.get(request, "formCode");
        int flowId = ParamUtil.getInt(request, "flowId", -1);
        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        INestTableCtl ntc = macroCtlService.getNestTableCtl();
        String op = ParamUtil.get(request, "op");
        String nestType = ParamUtil.get(request, "nestType");
        String nestFieldName = ParamUtil.get(request, "nestFieldName");
        long parentId = ParamUtil.getLong(request, "parentId", -1);

        if (op.equals("add")) {
            try {
                int r = ntc.uploadExcel(application, request);
                if (r > 0) {
    %>
    <script>
        <%if (nestType.equals("detaillist")) {
            JSONArray jsonAry = ntc.getCellJsonArray();
        %>
        window.opener.insertRow(null, '<%=jsonAry%>', "<%=nestFieldName%>");
        <%} else {%>
        if (window.opener != null) {
            window.opener.refreshNestTableCtl<%=nestFieldName%>();
            window.close();
        }
        <%}%>

        // alert("导入成功!");
        window.close();
    </script>
    <% } else
        out.print(StrUtil.Alert("文件不能为空！"));
    } catch (ErrMsgException e) {
        out.print(StrUtil.Alert_Back(e.getMessage()));
    }
    }
    %>
</head>
<body>
<form name="form1" action="nest_table_import_excel.jsp?op=add&flowId=<%=flowId%>&formCode=<%=formCode%>&parentId=<%=parentId%>&parentFormCode=<%=parentFormCode%>&nestFieldName=<%=nestFieldName%>&nestType=<%=nestType%>" method="post" encType="multipart/form-data">
    <table width="409" border="0" align="center" cellspacing="0" class="percent98">
        <thead>
        <tr>
            <td align="left" class="right-title tabStyle_1_title">&nbsp;请选择需导入的文件</td>
        </tr>
        </thead>
        <tr>
            <td width="319" align="left">
                <input title="选择附件文件" type="file" size="20" name="excel">
                <input name="formCode" value="<%=formCode%>" type=hidden>
                <input class="btn" name="submit" type="submit" value="确  定"/>
            </td>
        </tr>
        <tr>
            <td align="left">
                Excel文件表头：
                <a target="_blank" href="nest_import_excel_template.jsp?parentFormCode=<%=parentFormCode%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=nestFieldName%>">下载模板</a>
                <br/>
                <%
                    JSONObject json = null;
                    int formViewId = -1;
                    FormField nestField = null;
                    String nestFormCode = "";
                    if (!nestFieldName.equals("")) {
                        FormDb parentFd = new FormDb();
                        parentFd = parentFd.getFormDb(parentFormCode);
                        nestField = parentFd.getFormField(nestFieldName);
                        if (nestField == null) {
                            out.print("父表单（" + parentFormCode + "）中的嵌套表字段：" + nestFieldName + " 不存在");
                            return;
                        }
                        try {
                            String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
                            json = new JSONObject(defaultVal);
                            nestFormCode = json.getString("destForm");
                            if (!json.isNull("formViewId")) {
                                formViewId = StrUtil.toInt((String) json.get("formViewId"), -1);
                            }
                            else {
                                out.print(SkinUtil.makeErrMsg(request, "嵌套表格未视定视图"));
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    String viewContent = "";
                    if (formViewId != -1) {
                        FormViewDb formViewDb = new FormViewDb();
                        formViewDb = formViewDb.getFormViewDb(formViewId);
                        viewContent = formViewDb.getString("content");
                    } else {
                        ModuleSetupDb msd = new ModuleSetupDb();
                        msd = msd.getModuleSetupDb(nestFormCode);
                        viewContent = FormViewMgr.makeViewContent(msd);
                    }

                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(nestFormCode);
                    String str = "";
                    Vector fields = NestTableCtl.parseFieldsFromView(fd, viewContent);
                    Iterator ir = fields.iterator();
                    while (ir.hasNext()) {
                        FormField ff = (FormField) ir.next();
                        if (str.equals("")) {
                            str = ff.getTitle();
                        } else {
                            str += "&nbsp;|&nbsp;" + ff.getTitle();
                        }
                    }
                    out.print(str);
                %>
            </td>
        </tr>
    </table>
</form>
</body>
</html>
