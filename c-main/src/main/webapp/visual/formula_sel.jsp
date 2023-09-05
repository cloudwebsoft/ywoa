<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONException" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>函数选择</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function insertString(tbId, str){
            var tb = document.getElementById(tbId);
            tb.focus();
            if (isIE()) {
                var r = document.selection.createRange();
                document.selection.empty();
                r.text = str;
                r.collapse();
                r.select();
            }
            else{
                var newstart = tb.selectionStart+str.length;
                tb.value=tb.value.substr(0,tb.selectionStart)+str+tb.value.substring(tb.selectionEnd);
                tb.selectionStart = newstart;
                tb.selectionEnd = newstart;
            }
        }
    </script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String formCode = ParamUtil.get(request, "formCode");

    String jsonStr = ParamUtil.get(request, "params");
    JSONObject jsonObject;
    String formulaCode = "", params = "", formulaName = "";
    boolean isAutoWhenList = true;
    if (!"".equals(jsonStr)) {
        jsonObject = new JSONObject(jsonStr);
        try {
            formulaCode = jsonObject.getString("code");
            params = jsonObject.getString("params");
            formulaName = jsonObject.getString("name");
            isAutoWhenList = jsonObject.getBoolean("isAutoWhenList");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
%>
<table class="tabStyle_1" style="padding:0px; margin:0px;" width="100%" cellPadding="0" cellSpacing="0">
    <tbody>
    <tr>
        <td height="28" colspan="2" class="tabStyle_1_title">&nbsp;请选择</td>
    </tr>
    <tr>
        <td width="13%" align="center">函数</td>
        <td width="87%" align="left"><%
            FormDAO fdao = new FormDAO();
            String sql = "select id from ft_formula order by id desc";
            java.util.Iterator ir = fdao.list("formula", sql).iterator();
            String opts = "";
            while (ir.hasNext()) {
                fdao = (FormDAO) ir.next();
                opts += "<option value='" + fdao.getFieldValue("code") + "'>" + fdao.getFieldValue("name") + "</option>";
            }
        %>
            <select id="sel" name="sel" style="width:200px">
                <option value="">无</option>
                <%=opts%>
            </select>
        </td>
    </tr>
    <tr>
        <td align="center">形参</td>
        <td align="left">
            <span id="spanParams"></span>
        </td>
    </tr>
    <tr>
        <td align="center">实参</td>
        <td align="left"><input id="params" name="params" style="width: 100%"/></td>
    </tr>
    <tr>
        <td align="center">列表中自动生成</td>
        <td align="left">
            <select id="isAutoWhenList" name="isAutoWhenList">
                <option value="false">否</option>
                <option value="true" selected>是</option>
            </select>
            （选是，则列表内容多时，可能会影响性能；选否，则可能因数据未实时计算出现不一致的情况）
        </td>
    </tr>
    <tr>
        <td align="center">参数字段</td>
        <td align="left">
            <style>
                .fieldLink {
                    width: 120px;
                    float: left;
                    display: block;
                }
            </style>
            <a class="fieldLink" href="javascript:;" onclick="addParam('id')">-ID-</a>
            <a class="fieldLink" href="javascript:;" onclick="addParam('cws_id')">-关联ID-</a>
            <a class="fieldLink" href="javascript:;" onclick="addParam('cws_status')">-记录状态-</a>
            <a class="fieldLink" href="javascript:;" onclick="addParam('cws_quote_id')">-引用记录ID-</a>
            <a class="fieldLink" href="javascript:;" onclick="addParam('formCode')">-表单编码-</a>
            <%
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                ir = fd.getFields().iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
            %>
            <a class="fieldLink" href="javascript:;" onclick="addParam('<%=ff.getName()%>')"><%=ff.getTitle()%>
            </a>
            <%
                }
            %>
        </td>
    </tr>
    <tr>
        <td colspan="2" align="center"><input type="button" class="btn btn-default" value="确定" onclick="doSel()"/></td>
    </tr>
    </tbody>
</table>
</body>
<script language="javascript">
    function addParam(fieldName) {
        insertString('params', fieldName)
    }

    function doSel() {
        var result = {
            "code": $('#sel').val(),
            "params": $('#params').val(),
            "name": sel.options[sel.selectedIndex].text,
            "isAutoWhenList": $('#isAutoWhenList').val() == "true"
        };
        window.opener.setSequence(JSON.stringify(result), sel.options[sel.selectedIndex].text);
        window.close();
    }

    $('#sel').change(function () {
        if ($(this).val() == "") {
            return;
        }
        getParams($(this).val(), false);
    })

    /**
     * 取得形参
     */
    function getParams(code, isEdit) {
        $.ajax({
            type: "post",
            url: "formula/getParams.do",
            data: {
                code: code
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "0") {
                    jAlert(data.msg, "提示");
                } else {
                    if (!isEdit) {
                        $('#params').val(data.params);
                    }
                    $('#spanParams').html(data.params);
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    $(function () {
        var formulaCode = '<%=formulaCode%>';
        // 如果不为空，说明传入了参数，是在编辑函数宏控件属性
        if (formulaCode != '') {
            $('#sel').val(formulaCode);
            $('#params').val('<%=params%>');
            $('#isAutoWhenList').val('<%=isAutoWhenList%>');
            getParams(formulaCode, true);
        }

        $('#sel').select2();
    })
</script>
</html>