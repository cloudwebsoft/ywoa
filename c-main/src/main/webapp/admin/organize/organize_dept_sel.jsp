<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@page import="org.json.JSONObject" %>
<%
    String deptCode = ParamUtil.get(request, "deptCode");
    String op = ParamUtil.get(request, "op");
    JSONObject json = new JSONObject();
    if (op.equals("getNames")) {

        String codes = ParamUtil.get(request, "code");
        String[] codesArr = codes.split(",");
        String names = "";
        for (int i = 0; i < codesArr.length; i++) {
            String code = codesArr[i];
            DeptDb deptDb = new DeptDb();
            deptDb = deptDb.getDeptDb(code);
            String name = deptDb.getName();
            if ((i + 1) == codesArr.length) {
                names += name;
            } else {
                names += name + ",";
            }
        }

        json.put("ret", "0");
        json.put("msg", names);
        out.print(json.toString());
        return;
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>选择部门</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../../inc/nocache.jsp" %>
    <script src="<%=request.getContextPath() %>/inc/common.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
    <script src="<%=request.getContextPath() %>/js/jstree/jstree.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/jstree/themes/default/style.css"/>
    <script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function selectNode(code, name) {
            if (typeof (window.opener.setIntpuObjValue) == "function" || typeof (window.opener.setIntpuObjValue) == "object") {
                window.opener.setIntpuObjValue(code, name);
            }
            if (typeof (window.opener.selectNode) == "function" || typeof (window.opener.selectNode) == "object") {
                window.opener.selectNode(code, name);
            }
            window.close();
        }
    </script>
</head>
<body leftMargin=4 topMargin=8 rightMargin=0 class=menubar>
<table width="450" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
    <tr>
        <td height="24" colspan="2" align="center" class="tdStyle_1"><strong>请选择部门</strong></td>
    </tr>
    <form id="form1" name="form1" method="post">
        <tr>
            <td width="24" height="20">&nbsp;</td>
            <td width="249" valign="top">
                <%
                    com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

                    String dirCode = ParamUtil.get(request, "dirCode");
                    if (dirCode.equals("")) {
                        dirCode = privilege.getUserUnitCode(request);
                    }

                    DeptMgr dir = new DeptMgr();
                    DeptDb leaf = dir.getDeptDb(dirCode);
                    DeptView tv = new DeptView(request, leaf);
//tv.SelectSingleAjax(out, "selectNode", "", "", true );
                    boolean isOpenAll = true; // 展开所有节点
                    String jsonData = tv.getJsonString(isOpenAll);
                %></td>
        </tr>
        <tr>
            <td colspan="2">
                <div>
                    <div id="deptTree"></div>
                </div>
            </td>
        </tr>
    </form>
</table>
<br/>
<table align="center">
    <tr>
        <td>
            <input type="button" class="btn" value="确定" onclick="get_authchecked()" style="margin-left:100px;"/>
            <input type="button" class="btn" value="取消" onclick="cancel()" style="margin-left:100px;"/>
        </td>
    </tr>
</table>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</body>
<script>
    function bindClick() {
        $("a").bind("click", function () {
            $("a").css("color", "");
            $(this).css("color", "red");
        });
    }

    $(document).ready(function () {
        var a = $('#deptTree').jstree({
            "core": {
                "data":  <%=jsonData%>,
                "themes": {
                    "theme": "default",
                    "dots": true,
                    "icons": true
                },
                "check_callback": false,
            },
            "checkbox": {
                "keep_selected_style": true,
                "real_checkboxes": true
            },

            "plugins": ["wholerow", "themes", "checkbox", "ui", "types", "state"],
        })
            .bind('ready.jstree', function () {
                $("#deptTree").find("li").each(function () {
                    var $this = $(this);
                    $("#deptTree").jstree("uncheck_node", $this);
                });
                <%
                    String[] deptCodes = deptCode.split(",");
                    for(int i=0;i<deptCodes.length;i++){
                %>
                $("#deptTree").find("li").each(function () {
                    var $this = $(this);
                    if ($this.attr("id") == "<%=deptCodes[i]%>") {
                        $("#deptTree").jstree("check_node", $this);
                    }

                    $("#deptTree").jstree("save_selected");
                });
                <%}%>


            });
        bindClick();
    });

    //获得被选中项的ID串，以空格隔开
    function get_authchecked() {
        var codes = $("#deptTree").jstree("get_checked");

        $.ajax({
            type: "post",
            url: "organize_dept_sel.jsp",
            data: {
                op: "getNames",
                code: codes.toString()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                selectNode(codes, data.msg);
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });


    }

    function cancel() {
        window.close();
    }

</script>
</HTML>
