<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="org.json.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="com.redmoon.oa.flow.Leaf" %>
<%@ page import="com.redmoon.oa.flow.LeafPriv" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>流程查询条件设置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script src="../inc/common.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../inc/map.js"></script>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
    <script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>
    <script src="../js/select2/select2.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/jquery.toaster.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
    <script type="text/javascript" src="../js/jquery.flexbox.js"></script>
    <style>
        .form-box {
            width: 100%;
            height: 400px;
            margin: 0px 0px 10px 0px;
            padding-left: 10px;
            border: 1px solid #eeeeee;
            overflow-x: auto;
            overflow-y: auto;
            float: left;
        }
    </style>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String userName = privilege.getUser(request);
    String op = ParamUtil.get(request, "op");
    String typeCode = ParamUtil.get(request, "typeCode"); // 流程编码
    Leaf lf = new Leaf();
    lf = lf.getLeaf(typeCode);
    if (lf == null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
        return;
    }

    if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    LeafPriv leafPriv = new LeafPriv(typeCode);
    if (!leafPriv.canUserQuery(userName)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String formCode = lf.getFormCode();

    FormMgr fm = new FormMgr();
    FormDb fd = fm.getFormDb(formCode);
    if (!fd.isLoaded()) {
        out.print(StrUtil.jAlert_Back("该表单不存在！", "提示"));
        return;
    }

    if (op.equals("setCond")) {
        boolean re = false;
/*        try {
            re = mvm.modifyBtn(request, code);
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
            return;
        }*/

        int isToolbar = ParamUtil.getInt(request, "isToolbar", 0);

        String[] queryFields = ParamUtil.getParameters(request, "queryFields");
        if (queryFields==null) {
            throw new ErrMsgException("请选择条件！");
        }
        JSONObject json = new JSONObject();
        try {
            // 因为json是无序的，所以需要用fields记录条件字段的顺序
            String fields = "";
            for (int i=0; i<queryFields.length; i++) {
                String token = ParamUtil.get(request, queryFields[i] + "_cond");
                json.put(queryFields[i], token);
                if (fields.equals(""))
                    fields = queryFields[i];
                else
                    fields += "," + queryFields[i];
            }
            json.put("fields", fields);
            json.put("isToolbar", isToolbar);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        lf.setCondProps(json.toString());
        re = lf.update();
        if (re) {
            out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "flow_field_conds.jsp?typeCode=" + typeCode + "&formCode=" + formCode));
        } else {
            out.print(StrUtil.jAlert_Back("操作失败！", "提示"));
        }
        return;
    }

    MacroCtlMgr mm = new MacroCtlMgr();
    JSONObject json;
    if (!"".equals(lf.getCondProps())) {
        json = new JSONObject(lf.getCondProps());
    }
    else {
        json = new JSONObject();
    }
%>
</div>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">条件设置</td>
    </tr>
    </tbody>
</table>
<div class="spacerH"></div>
<form action="flow_field_conds.jsp?op=setCond" method="post" name="formCond" id="formCond">
    <table cellspacing="0" class="tabStyle_1 percent60" cellpadding="3" width="95%" align="center">
        <tr>
            <td align="center" class="tabStyle_1_title"><%=fd.getName()%></td>
        </tr>
        <tr style="display: none;">
            <td align="left">
                <%
                    boolean isToolbar = true;
                    if (json.has("isToolbar")) {
                        isToolbar = json.getInt("isToolbar") == 1;
                    }
                %>
                <input type="checkbox" id="isToolbar" name="isToolbar" value="1" <%=isToolbar ? "checked" : ""%> />&nbsp;置于工具条
            </td>
        </tr>
        <tr>
            <td>
                <div class="form-box">
                    <%
                        Iterator ir = fd.getFields().iterator();
                        while (ir.hasNext()) {
                            FormField ff = (FormField) ir.next();
                            if (!ff.isCanQuery())
                                continue;
                            String fieldDesc = ff.getName();
                    %>
                    <div>
                        <input type="checkbox" name="queryFields" value="<%=fieldDesc%>"/>
                        <%=ff.getTitle()%>&nbsp;&nbsp;
                        <%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {%>
                        <select name="<%=fieldDesc%>_cond">
                            <option value="0">时间段</option>
                            <option value="1">时间点</option>
                        </select>
                        <%
                        } else if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        %>
                        <select name="<%=fieldDesc%>_cond">
                            <option value="1">等于</option>
                            <option value="0" selected="selected">包含</option>
                        </select>
                        <%
                        } else if (ff.getFieldType() == FormField.FIELD_TYPE_INT || ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE || ff.getFieldType() == FormField.FIELD_TYPE_FLOAT || ff.getFieldType() == FormField.FIELD_TYPE_LONG || ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
                        %>
                        <select name="<%=fieldDesc%>_cond">
                            <option value="=" selected="selected">等于</option>
                            <option value="&gt;">大于</option>
                            <option value="&lt;">小于</option>
                            <option value="&gt;=">大于等于</option>
                            <option value="&lt;=">小于等于</option>
                        </select>
                        <input name="<%=fieldDesc%>" type="hidden"/>
                        <%
                        } else {%>
                        <select name="<%=fieldDesc%>_cond">
                            <option value="1">等于</option>
                            <%if (ff.getType().equals(FormField.TYPE_TEXTFIELD) || ff.getType().equals(ff.TYPE_TEXTAREA)) {%>
                            <option value="0" selected="selected">包含</option>
                            <%}%>
                        </select>
                        <%}%>
                    </div>
                    <%
                        }
                    %>
                    <%--<div>
                        <input type="checkbox" name="queryFields" value="cws_status"/>
                        记录状态
                        <select name="cws_status_cond">
                            <option value="=" selected="selected">等于</option>
                        </select>
                        <input name="cws_status" type="hidden"/>
                    </div>
                    <div>
                        <input type="checkbox" name="queryFields" value="cws_flag"/>
                        冲抵状态
                        <select name="cws_flag_cond">
                            <option value="=" selected="selected">等于</option>
                        </select>
                        <input name="cws_flag" type="hidden"/>
                    </div>--%>
                </div>
            </td>
        </tr>
        <tr>
            <td align="center">
                <input class="btn btn-default" type="submit" value="确定"/>
                <input name="typeCode" value="<%=typeCode%>" type="hidden"/>
            </td>
        </tr>
    </table>
</form>
<script>
    <%
    if (json.has("fields")) {
        String queryFields = json.getString("fields");
        String[] ary = StrUtil.split(queryFields, ",");
        if (ary!=null) {
            for (int k=0; k<ary.length; k++) {
                String cond = json.getString(ary[k]);
    %>
    setCheckboxChecked("queryFields", "<%=ary[k]%>");
    o("<%=ary[k]%>_cond").value = "<%=cond%>";
    <%
            }
        }
    }
    %>
</script>
</body>
</html>