<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.security.ThreeDesUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程查询结果导出</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%
        String typeCode = ParamUtil.get(request, "typeCode");
        WorkflowDb wf = new WorkflowDb();
        String sql = wf.getSqlSearch(request);
        com.redmoon.oa.sso.Config ssocfg = new com.redmoon.oa.sso.Config();
        String query = ThreeDesUtil.encrypt2hex(ssocfg.get("key"), sql);

        String op = ParamUtil.get(request, "op");
        Leaf lf = new Leaf();
        lf = lf.getLeaf(typeCode);
        FormDb fd = new FormDb();
        fd = fd.getFormDb(lf.getFormCode());

        String options = "";
        String selOptions = "";
        Vector v = fd.getFields();

        String exportColProps = lf.getExportColProps();
        if ("".equals(exportColProps)) {
            Iterator ir = v.iterator();
            StringBuffer sb = new StringBuffer();

            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.isCanList()) {
                    selOptions += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
                    StrUtil.concat(sb, ",", ff.getName());
                } else {
                    options += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
                }
            }
            lf.setExportColProps(sb.toString());
            lf.update();
        }
        else {
            String[] ary = StrUtil.split(exportColProps, ",");
            if (ary!=null) {
                for (String fieldName : ary) {
                    FormField ff = fd.getFormField(fieldName);
                    if (ff == null) {
                        DebugUtil.w(getClass(), "exportColProps", "字段: " + fieldName + " 不存在");
                    } else {
                        selOptions += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
                    }
                }

                List list = Arrays.asList(ary);
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
                    if (!list.contains(ff.getName())) {
                        options += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
                    }
                }
            }
        }
    %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script language="JavaScript">
        function getFields() {
            var fieldsSelected = o("fieldsSelected");
            var str = "";
            var strText = "";
            var opts = fieldsSelected.options;
            var len = opts.length;
            for (var i = 0; i < len; i++) {
                if (str == "") {
                    str = opts[i].value;
                    strText = opts[i].text;
                } else {
                    str += "," + opts[i].value;
                    strText += "," + opts[i].text;
                }
            }
            o("fields").value = str;
        }

        function sel() {
            var fieldsNotSelected = o("fieldsNotSelected");
            var fieldsSelected = o("fieldsSelected");
            var opts = fieldsNotSelected.options;
            var len = opts.length;
            var ary = new Array(len);
            for (var i = 0; i < len; i++) {
                ary[i] = "0";
                if (opts[i].selected) {
                    fieldsSelected.options.add(new Option(opts[i].text, opts[i].value));
                    ary[i] = opts[i].value;
                }
            }
            for (var i = 0; i < len; i++) {
                for (var j = 0; j < len; j++) {
                    if (ary[i] != "0") {
                        try {
                            // 删除项目后，options会变短，因此用异常捕获来防止出错
                            if (opts[j].value == ary[i])
                                opts.remove(j);
                        } catch (e) {
                        }
                    }
                }
            }
        }

        function notsel() {
            var fieldsNotSelected = o("fieldsNotSelected");
            var fieldsSelected = o("fieldsSelected");

            var opts = fieldsSelected.options;
            var len = opts.length;
            var ary = new Array(len);
            for (var i = 0; i < len; i++) {
                ary[i] = "0";
                if (opts[i].selected) {
                    fieldsNotSelected.options.add(new Option(opts[i].text, opts[i].value));
                    ary[i] = opts[i].value;
                }
            }

            for (var i = 0; i < len; i++) {
                for (var j = 0; j < len; j++) {
                    if (ary[i] != "0") {
                        try {
                            // 删除项目后，options会变短，因此用异常捕获来防止出错
                            if (opts[j].value == ary[i])
                                opts.remove(j);
                        } catch (e) {
                        }
                    }
                }
            }
        }
    </script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1"><lt:Label res="res.flow.Flow" key="exportQueryResult"/></td>
    </tr>
    </tbody>
</table>
<div class="spacerH"></div>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(SkinUtil.makeErrMsg(request, "警告非法用户，你无访问此页的权限！"));
        return;
    }
%>
<form action="flow_query_result_excel.jsp" name="form1" method="post" target="_blank" onsubmit="return getFields()">
    <table width="501" height="293" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
        <tr>
            <td height="23" colspan="3" class="tabStyle_1_title">&nbsp;&nbsp;<span><lt:Label res="res.flow.Flow" key="exportFields"/> (<lt:Label res="res.flow.Flow" key="formName"/><%=fd.getName()%>)</span></td>
        </tr>
        <tr>
            <td width="231" height="22" align="right"><lt:Label res="res.flow.Flow" key="selectDomain"/></td>
            <td width="37">&nbsp;</td>
            <td width="231" height="22"><lt:Label res="res.flow.Flow" key="alternativeDomain"/></td>
        </tr>
        <tr>
            <td align="right">
                <select id="fieldsSelected" name="fieldsSelected" size=15 multiple style="width:200px;height:300px" ondblclick="notsel();">
                    <%=selOptions%>
                </select>
            </td>
            <td align="center" valign="middle">
                <input type="button" class="btn" name="selBtn" value="←" onclick="sel()"/>
                <br/>
                <br/>
                <input type="button" class="btn" name="notselBtn" value="→" onclick="notsel()"/>
                <br/>
                <br/>
                <input type="button" class="btn" name="notselBtn" value=" ↑ " onclick="moveup()"/>
                <br/>
                <br/>
                <input type="button" class="btn" name="notselBtn" value=" ↓ " onclick="movedown()"/>
            </td>
            <td>
                <select id="fieldsNotSelected" name="fieldsNotSelected" size=15 multiple style="width:200px;height:300px" ondblclick="sel();">
                    <%=options%>
                </select>
                <input id="fields" name="fields" type="hidden"/>
                <input name="query" value="<%=query%>" type="hidden"/>
                <input name="typeCode" value="<%=typeCode%>" type="hidden"/>
            </td>
        </tr>
        <tr align="center">
            <td height="28" colspan="3">
                <input class="btn" type="submit" name="okbtn" value="<lt:Label res='res.flow.Flow' key='sure'/>"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input class="btn" type="button" name="cancelbtn" value="返回" onclick="history.back()"/>
            </td>
        </tr>
    </table>
</form>
<script type="text/javascript" src="../js/jquery.toaster.js"></script>
<script>
    function moveup() {
        var $opt = $("#fieldsSelected").find("option:selected");
        if ($opt.length > 1) {
            $.toaster({
                "priority": "info",
                "message": "只能选择一条数据"
            });
            return;
        }
        var $popt = $opt.prev();
        if ($popt[0]) {
            $opt.after($popt);
        } else {
            $.toaster({
                "priority": "info",
                "message": "已移动到最顶端了"
            });
        }
    }

    function movedown() {
        var $opt = $("#fieldsSelected").find("option:selected");
        if ($opt.length > 1) {
            $.toaster({
                "priority": "info",
                "message": "只能选择一条数据"
            });
            return;
        }
        var $nopt = $opt.next();
        if ($nopt[0]) {
            $nopt.after($opt);
        } else {
            $.toaster({
                "priority": "info",
                "message": "已移动到最底端了"
            });
        }
    }
</script>
</body>
</HTML>
