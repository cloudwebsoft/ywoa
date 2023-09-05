<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.jdom.input.SAXBuilder" %>
<%@ page import="org.xml.sax.InputSource" %>
<%@ page import="org.jdom.Element" %>
<%@ page import="java.io.StringReader" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%
    String op = ParamUtil.get(request, "op");
    if (op.equals(WorkflowLinkDb.COND_TYPE_ROLE)) {
        String codes = ParamUtil.get(request, "codes");
        String[] ary = StrUtil.split(codes, ",");
        String names = "";
        if (ary != null) {
            RoleDb rd = new RoleDb();
            for (int i = 0; i < ary.length; i++) {
                rd = rd.getRoleDb(ary[i]);
                if ("".equals(names)) {
                    names = rd.getDesc();
                } else {
                    names += "," + rd.getDesc();
                }
            }
            out.print(names);
        }
        return;
    } else if (op.equals(WorkflowLinkDb.COND_TYPE_DEPT) || op.equals(WorkflowLinkDb.COND_TYPE_DEPT_BELONG)) {
        String codes = ParamUtil.get(request, "codes");
        String[] ary = StrUtil.split(codes, ",");
        String names = "";
        if (ary != null) {
            DeptDb rd = new DeptDb();
            for (int i = 0; i < ary.length; i++) {
                rd = rd.getDeptDb(ary[i]);
                if ("".equals(names)) {
                    names = rd.getName();
                } else {
                    names += "," + rd.getName();
                }
            }
            out.print(names);
        }
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程连接属性</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>

    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <%
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
        String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
        String conditionType = ParamUtil.get(request, "conditionType");
        String linkProp = ParamUtil.get(request, "linkProp");

        com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
    %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script language="JavaScript">
    function ClearCond() {
        if (confirm("您确定要清除条件么？")) {
            window.parent.SetSelectedLinkProperty("title", "");
            window.parent.SetSelectedLinkProperty("desc", "");
            o("conditionType").value = "-1"; // 无条件
            conditionType_onchange(false);
            $('#rawTitle').html('');
        }
    }

    //@task: 注意在控件的link中不能存储"号
    function encodeLinkStr(str) {
        str = str.replaceAll(":", "\\colon");
        str = str.replaceAll(";", "\\semicolon");
        str = str.replaceAll(",", "\\comma");

        str = str.replaceAll("\n\r", "\\newline");
        str = str.replaceAll("\n", "\\newline"); // textarea中的换行是\n
        str = str.replaceAll("\r", "\\newline"); // IE8 textarea中的换行是\r

        return str.replaceAll("\"", "\\quot");
    }

    function decodeLinkStr(str) {
        str = str.replaceAll("\\\\colon", ":");
        str = str.replaceAll("\\\\semicolon", ";");
        str = str.replaceAll("\\\\comma", ",");
        str = str.replaceAll("\\\\newline", "\n");
        str = str.replaceAll("\\\\newline", "\r\n");

        return str.replaceAll("\\\\quot", "\"");
    }

    function ModifyLink() {
        if (o("expireHour").value.trim() != "") {
            if (!isNumeric(o("expireHour").value.trim())) {
                alert("到期时间必须为大于1的数字！");
                return;
            }
            if (o("expireHour").value < 0) {
                alert("到期时间必须为大于或等于0的数字！");
                return;
            }
        }
        window.parent.SetSelectedLinkProperty("conditionType", o("conditionType").value);
        window.parent.SetSelectedLinkProperty("desc", o("desc").value);
        window.parent.SetSelectedLinkProperty("expireHour", o("expireHour").value);
        window.parent.SetSelectedLinkProperty("expireAction", o("expireAction").value);

        var t = "";
        if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_FORM%>") {
            t = o("fields").value + o("compare").value + o("condValue").value;
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT%>") {
            t = o("depts").value;
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT_BELONG%>") {
            t = o("depts").value;
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_ROLE%>") {
            t = o("depts").value;
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>") {
            t = o("deptNames").value;
        }
        // alert(encodeLinkStr(t));
        window.parent.SetSelectedLinkProperty("title", encodeLinkStr(t));

        window.parent.setCondition($("#hiddenCondition").val());
        window.parent.submitDesigner();
    }

    function window_onload() {
        var t = decodeLinkStr(window.parent.GetSelectedLinkProperty("title"));
        conditionType.value = window.parent.GetSelectedLinkProperty("conditionType");
        if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>") {
            rawTitle.innerHTML = "脚本";
        } else {
            if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_ROLE%>" || conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT%>" || conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT_BELONG%>") {
                $.ajax({
                    type: "post",
                    url: "flow_designer_link_prop.jsp",
                    data: {
                        op: conditionType.value,
                        codes: t
                    },
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                    },
                    success: function (data, status) {
                        rawTitle.innerHTML = data;
                    },
                    complete: function (XMLHttpRequest, status) {
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        alert(XMLHttpRequest.responseText);
                    }
                });
            } else {
                rawTitle.innerHTML = t;
            }
        }

        o("desc").value = window.parent.GetSelectedLinkProperty("desc");
        o("expireHour").value = window.parent.GetSelectedLinkProperty("expireHour");
        o("expireAction").value = window.parent.GetSelectedLinkProperty("expireAction");

        //alert( window.parent.GetSelectedLinkProperty("name") );
        conditionType_onchange(true);
        if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_FORM%>") {
            if (t == "")
                return;
            if (t.indexOf(">=") != -1)
                compare.value = ">=";
            else if (t.indexOf("<=") != -1)
                compare.value = "<=";
            else if (t.indexOf("<>") != -1)
                o("compare").value = "<>";
            else if (t.indexOf(">") != -1)
                compare.value = ">";
            else if (t.indexOf("<") != -1)
                compare.value = "<";
            else if (t.indexOf("=") != -1)
                compare.value = "=";
            var ary = t.split(compare.value);
            o("fields").value = ary[0];
            o("condValue").value = ary[1];
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT%>" || conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT_BELONG%>") {
            o("depts").value = t;
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_ROLE%>") {
            o("depts").value = t;
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>") {
            o("deptNames").value = t;
        }

        var fromVal = window.parent.GetSelectedLinkProperty("from");
        var toVal = window.parent.GetSelectedLinkProperty("to");

        <%
            if(!"".equals(linkProp)){
                SAXBuilder parser = new SAXBuilder();
                org.jdom.Document doc = parser.build(new InputSource(new StringReader(linkProp)));
                Element root = doc.getRootElement();
                List<Element> v = root.getChildren();
                   for (Element e : v) {
                       String from = e.getChildText("from");
                       String to = e.getChildText("to");
        %>
        if (fromVal == "<%=from%>" && toVal == "<%=to%>") {
            document.getElementById("imgId").style.display = "inline-block";
        }
        <%
                   }
            }
        %>
    }

    function conditionType_onchange(isOnLoad) {
        if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_FORM%>") {
            $(".toHide").show();
            $(".linkShow").hide();

            $("#deptRoleSelTr").hide();
            $("#formSelTr").show();
            $("#condTr").show();

            o("deptNames").disabled = true;
            o("deptAddBtn").disabled = true;
            o("deptClearBtn").disabled = true;
            o("roleAddBtn").disabled = true;
            o("compare").disabled = false;
            o("condValue").disabled = false;
            o("fields").disabled = false;
            o("deptNames").readOnly = true;
            o("deptNames").value = "";
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT%>" || conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_DEPT_BELONG%>") {
            $(".toHide").show();
            $(".linkShow").hide();

            $("#deptRoleSelTr").show();
            $("#formSelTr").hide();
            $("#condTr").hide();

            compare.disabled = true;
            o("condValue").disabled = true;
            o("fields").disabled = true;
            o("deptNames").disabled = false;
            o("deptAddBtn").disabled = false;
            o("deptClearBtn").disabled = false;
            o("roleAddBtn").disabled = true;
            o("deptNames").readOnly = true;

            $(o("deptAddBtn")).show();
            $(o("roleAddBtn")).hide();
            $(o("deptClearBtn")).show();
            $("#scriptBtn").hide();

            if (!isOnLoad) {
                o("deptNames").value = "";
            }

            o("condName").innerHTML = "部门";

        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_ROLE%>") {
            $(".toHide").show();
            $(".linkShow").hide();

            $("#deptRoleSelTr").show();
            $("#formSelTr").hide();
            $("#condTr").hide();

            compare.disabled = true;
            o("condValue").disabled = true;
            o("fields").disabled = true;
            o("deptNames").disabled = false;
            deptAddBtn.disabled = true;
            deptClearBtn.disabled = false;
            o("roleAddBtn").disabled = false;
            o("deptNames").readOnly = true;

            $('#deptAddBtn').hide();
            $('#roleAddBtn').show();
            $('#deptClearBtn').show();
            $("#scriptBtn").hide();

            if (!isOnLoad)
                deptNames.value = "";

            o("condName").innerHTML = "角色";

        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>") {
            $(".toHide").show();
            $(".linkShow").hide();

            $("#deptRoleSelTr").show();
            $("#formSelTr").hide();
            $("#condTr").hide();

            compare.disabled = true;
            o("condValue").disabled = true;
            o("fields").disabled = true;
            deptAddBtn.disabled = true;
            deptClearBtn.disabled = false;
            o("roleAddBtn").disabled = true;

            o("deptNames").disabled = false;
            o("deptNames").value = "";
            o("deptNames").readOnly = false;

            $('#deptAddBtn').hide();
            $('#roleAddBtn').hide();
            $('#deptClearBtn').show();
            $("#scriptBtn").show();

            o("condName").innerHTML = "脚本";
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_NONE%>") {
            $(".toHide").hide();
            $(".linkShow").hide();
        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_MUST%>") {
            $(".toHide").show();
            $(".linkShow").hide();

            $("#deptRoleSelTr").hide();
            $("#formSelTr").hide();
            $("#condTr").hide();

        } else if (conditionType.value == "<%=WorkflowLinkDb.COND_TYPE_COMB_COND%>") {
            $(".toHide").hide();
            $(".linkShow").show();
            //openWin('combination_condition.jsp', 600, 480);
        }
    }

    function getDepts() {
        return o("depts").value;
    }

    function selectNode(code, name) {
        o("depts").value = code;
        o("deptNames").value = name;

        if (o("depts").value.indexOf("<%=DeptDb.ROOTCODE%>") != -1) {
            o("depts").value = "<%=DeptDb.ROOTCODE%>";
            o("deptNames").value = "全部";
        }
    }

    function openWinDepts() {
        openWin('../deptMultiSel.do', 520, 350);
    }

    function setRoles(roles, descs) {
        o("depts").value = roles;
        deptNames.value = descs
    }

    function openWin(url, width, height) {
        var newwin = window.open(url, "fieldWin", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
        return newwin;
    }

    function openCondition() {
        var fromValue = window.parent.GetSelectedLinkProperty("from");
        var toValue = window.parent.GetSelectedLinkProperty("to");
        var hiddenLinkProp = $("#hiddenLinkProp").html();
        var hiddenConditionVal = $('#hiddenCondition').val();
        if (hiddenConditionVal != '') {
            hiddenLinkProp = hiddenConditionVal;
        }
        if (fromValue == "" && toValue == "") {
            alert("请选择分支");
        } else {
            // 注意不能用此方式，因为通过get方式传参,linkProp中的&lt;会在传至combination_condition.jsp中的时候被自动转义为 <，导致XML在解析时出错
            // openWin("combination_condition.jsp?flowTypeCode=<%=flowTypeCode%>&linkProp=" + hiddenLinkProp + "&fromValue=" + fromValue + "&toValue=" + toValue,800,600);
            openWin("", 1024, 668);

            var url = "combination_condition.jsp";
            var tempForm = document.createElement("form");
            tempForm.id = "tempForm1";
            tempForm.method = "post";
            tempForm.action = url;

            var hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "linkProp";
            hideInput.value = hiddenLinkProp;
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "fromValue";
            hideInput.value = fromValue;
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "toValue";
            hideInput.value = toValue;
            tempForm.appendChild(hideInput);

            hideInput = document.createElement("input");
            hideInput.type = "hidden";
            hideInput.name = "flowTypeCode";
            hideInput.value = "<%=flowTypeCode%>";
            tempForm.appendChild(hideInput);

            document.body.appendChild(tempForm);
            tempForm.target = "fieldWin";
            tempForm.submit();
            document.body.removeChild(tempForm);
        }
    }

    function setCondition(str) {
        $("#hiddenCondition").val(str);
    }
    </script>
</head>
<body onLoad="window_onload()">
<table border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1" style="margin-top:3px;width:100%">
    <tr>
        <td height="22" colspan="2" align="center" style="border-left:0px"><input name="okbtn2" type="button" class="btn" onclick="ClearCond()" value=" 清除条件 "/>
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input name="okbtn" type="button" class="btn" onclick="ModifyLink()" value=" 保存 "/></td>
    </tr>
    <tr>
        <td width="50" height="22" align="center" style="border-left:0px">描述</td>
        <td height="22" style="border-right:0px"><input type="text" name="desc" size="20"/></td>
    </tr>
    <tr>
        <td height="22" align="center">到期</td>
        <td height="22" style="border-right:0px"><input title="下一节点人员处理的到期时间" type="text" name="expireHour" style="width: 60px" value="0"/>
            <%
                Config cfg = new Config();
                String flowExpireUnit = cfg.get("flowExpireUnit");
                if (flowExpireUnit.equals("day")) {
                    out.print("天");
                } else {
                    out.print("小时");
                }
            %>
            (0表示不限时)
        </td>
    </tr>
    <tr>
        <td height="22" align="center">超期</td>
        <td height="22" style="border-right:0px"><select name="expireAction">
            <option value="">等待</option>
            <option value="next">交办至后续节点</option>
            <option value="starter">返回给发起人</option>
        </select></td>
    </tr>
    <tr>
        <td height="22" align="center">类型</td>
        <td style="border-right:0px" height="22" align="left">
            <select id="conditionType" name="conditionType" onchange="conditionType_onchange(false)" title="如仅有1个无条件分支则视为默认条件">
                <option value="-1">无条件（或默认条件）</option>
                <option value="">根据表单</option>
                <option value="<%=WorkflowLinkDb.COND_TYPE_DEPT%>">根据上一节点用户所在部门</option>
                <option value="<%=WorkflowLinkDb.COND_TYPE_DEPT_BELONG%>">根据上一节点用户所属的部门</option>
                <option value="<%=WorkflowLinkDb.COND_TYPE_ROLE%>">根据上一节点用户角色</option>
                <option value="<%=WorkflowLinkDb.COND_TYPE_COMB_COND%>">组合条件</option>
                <%
                    if (license.isSrc()) {
                %>
                <option value="<%=WorkflowLinkDb.COND_TYPE_SCRIPT%>">根据脚本</option>
                <%
                    }
                %>
                <option value="<%=WorkflowLinkDb.COND_TYPE_MUST%>">必须执行</option>
            </select>
        </td>
    </tr>
    <tr id="condTr" class="toHide">
        <td height="22" align="center">条件</td>
        <td style="border-right:0px" height="22" align="left"><span id="rawTitle"></span></td>
    </tr>
    <tr id="formSelTr" class="toHide">
        <td height="22" align="center">表单</td>
        <td style="border-right:0px;line-height:1.5" height="22">
            <%

                Leaf lf = new Leaf();
                lf = lf.getLeaf(flowTypeCode);
                FormDb fd = new FormDb();
                fd = fd.getFormDb(lf.getFormCode());
                Vector v = fd.getFields();
                Iterator ir = v.iterator();
                String options = "";
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
                    options += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
                }
            %>
            <select name="fields">
                <%=options%>
            </select>
            <select id="compare" name="compare" style="font-family:'宋体'">
                <option value=">=">>=</option>
                <option value="<="><=</option>
                <option value=">">></option>
                <option value="&lt;"><</option>
                <option value="=">=</option>
                <option value="<>"><></option>
            </select>
            <input name="condValue" style="width:40px">
            <br>
            当为等号时，如果有多个满足条件的值可以用逗号分隔，
            空表示为默认条件
        </td>
    </tr>
    <tr id="deptRoleSelTr" class="toHide">
        <td height="22" align="center">
            <span id="condName">部门</span></td>
        <td style="line-height:1.5; border-right:0px" height="22">
	    <textarea name="deptNames" cols="25" rows="8" readOnly wrap="yes" id="deptNames" style="width:100%"><%
            String depts = "";
            if (conditionType.equals(WorkflowLinkDb.COND_TYPE_DEPT) || conditionType.equals(WorkflowLinkDb.COND_TYPE_DEPT_BELONG)) {
                depts = ParamUtil.get(request, "title");
                String[] arydepts = StrUtil.split(depts, ",");
                int len = 0;
                String deptNames = "";
                if (arydepts != null) {
                    len = arydepts.length;
                    DeptDb dd = new DeptDb();
                    for (int i = 0; i < len; i++) {
                        if (deptNames.equals("")) {
                            dd = dd.getDeptDb(arydepts[i]);
                            deptNames = dd.getName();
                        } else {
                            dd = dd.getDeptDb(arydepts[i]);
                            deptNames += "," + dd.getName();
                        }
                    }
                }
                out.print(deptNames);
            } else if (conditionType.equals(WorkflowLinkDb.COND_TYPE_ROLE)) {
                depts = ParamUtil.get(request, "title");
                String[] aryroles = StrUtil.split(depts, ",");
                int len = 0;
                String roleNames = "";
                if (aryroles != null) {
                    len = aryroles.length;
                    RoleDb rd = new RoleDb();
                    for (int i = 0; i < len; i++) {
                        rd = rd.getRoleDb(aryroles[i]);
                        if (roleNames.equals("")) {
                            roleNames = rd.getDesc();
                        } else {
                            roleNames += "," + rd.getDesc();
                        }
                    }
                }
                out.print(roleNames);
            }
        %></textarea>
            <input type="hidden" name="depts" value="<%=depts%>">
            <input type="hidden" name="hiddenCondition" id="hiddenCondition" value="">
            <xmp name="hiddenLinkProp" id="hiddenLinkProp" style="display:none"><%=linkProp %></xmp>
            <br>
            <input id="deptAddBtn" title="选择部门" onClick="openWinDepts()" class="btn" type="button" value="选择" name="button">
            <input id="roleAddBtn" name="roleAddBtn" type="button" class="btn" onClick="openWin('../role_multi_sel.jsp?roleCodes=', 526, 435)" value="添加">
            <input id="scriptBtn" type="button" value="设计器" class="btn" onclick="openIdeWin()"/>
            &nbsp;&nbsp;
            <input id="deptClearBtn" title="清空" class="btn" onClick="deptNames.value='';depts.value=''" type="button" value="清空" name="button">
            <br>
            <!--空表示除其它分支条件外的部门或角色--></td>
    </tr>
    <tr class="linkShow" style="display:none;background-color:#dbe1f3;">
        <td colspan="2" align="center"><img src="images/combination.png" style="margin-bottom:-5px;"/>&nbsp;<a href="javascript:;" onclick="openCondition()">配置组合条件</a>&nbsp;<img src="images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:none;" id="imgId"/></td>
    </tr>
</table>
</body>
<script>
    function getScript() {
        return $('#deptNames').val();
    }

    function setScript(script) {
        $('#deptNames').val(script);
    }

    <%
    com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
    com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
    String version = StrUtil.getNullStr(oaCfg.get("version"));
    String spVersion = StrUtil.getNullStr(spCfg.get("version"));
    %>
    var ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>";
    var ideWin;
    var cwsToken = "";

    function openIdeWin() {
        ideWin = openWinMax(ideUrl);
    }

    var onMessage = function (e) {
        var d = e.data;
        var data = d.data;
        var type = d.type;
        if (type == "setScript") {
            setScript(data);
            if (d.cwsToken!=null) {
                cwsToken = d.cwsToken;
                ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>&cwsToken=" + cwsToken;
            }
        } else if (type == "getScript") {
            var data = {
                "type": "openerScript",
                "version": "<%=version%>",
                "spVersion": "<%=spVersion%>",
                "scene": "flow.condition",
                "data": getScript()
            }
            ideWin.leftFrame.postMessage(data, '*');
        } else if (type == "setCwsToken") {
            cwsToken = d.cwsToken;
            ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>&cwsToken=" + cwsToken;
        }
    };

    $(function () {
        if (window.addEventListener) { // all browsers except IE before version 9
            window.addEventListener("message", onMessage, false);
        } else {
            if (window.attachEvent) { // IE before version 9
                window.attachEvent("onmessage", onMessage);
            }
        }
    });
</script>
</html>
