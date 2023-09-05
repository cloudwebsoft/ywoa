<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.visual.ModuleUtil" %>
<%@ page import="com.cloudweb.oa.service.IRoleService" %>
<%@ page import="com.cloudweb.oa.entity.Role" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%
    String op = ParamUtil.get(request, "op");
    String flowTypeCode = ParamUtil.get(request, "dirCode");

    Leaf lf = new Leaf();
    lf = lf.getLeaf(flowTypeCode);
    if (lf == null) {
        out.print(SkinUtil.makeErrMsg(request, "流程类型不存在！"));
        return;
    }
    String formCode = lf.getFormCode();
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    if (!fd.isLoaded()) {
        out.print(SkinUtil.makeErrMsg(request, "表单不存在！"));
        return;
    }

    WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getPredefineFlowOfFree(flowTypeCode);

    if ("save".equals(op)) {
        String xml = ParamUtil.get(request, "xml");
        wpd.setViews(xml);
        boolean re = wpd.save();
        JSONObject json = new JSONObject();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    } else if ("setViewRule".equals(op)) {
        boolean isUseFormViewRule = ParamUtil.getBoolean(request, "isUseFormViewRule", true);
        wpd.setUseFormViewRule(isUseFormViewRule);
        boolean re = wpd.save();
        JSONObject json = new JSONObject();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    } else if ("copyViewRule".equals(op)) {
        String xml = fd.getViewSetup();
        int p = xml.indexOf("<views>");
        int q = xml.indexOf("</views>");
        if (p == -1 || q == -1) {
            JSONObject json = new JSONObject();
            json.put("ret", "0");
            json.put("msg", "表单中的显示规则不存在或格式错误！");
            out.print(json);
            return;
        }
        xml = xml.substring(p + 7, q);
        xml = "<actions><action internalName=\"defaultNode\">" + xml + "</action></actions>";
        wpd.setViews(xml);
        boolean re = wpd.save();
        JSONObject json = new JSONObject();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    } else if ("syncViewRule".equals(op)) {
        String xml = wpd.getViews();
        String token = "<action internalName=\"defaultNode\">";
        int p = xml.indexOf(token);
        int q = xml.indexOf("</action>");
        if (p == -1 || q == -1) {
            JSONObject json = new JSONObject();
            json.put("ret", "0");
            json.put("msg", "流程中的显示规则不存在或格式错误！");
            out.print(json);
            return;
        }

        xml = xml.substring(p + token.length(), q);
        xml = "<views>" + xml + "</views>";
        fd.setViewSetup(xml);
        boolean re = fd.saveContent();
        JSONObject json = new JSONObject();
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    }

    IRoleService roleService = SpringUtil.getBean(IRoleService.class);
    List<Role> roleList = roleService.getAll();
    StringBuilder roleOptions = new StringBuilder();
    for (Role role : roleList) {
        roleOptions.append("<option value='" + role.getCode() + "'>" + role.getDescription() + "</option>");
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>流程显示设定</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%
        Vector fieldV = fd.getFields();
        Iterator fieldIr = fieldV.iterator();
        String options = "";
        options += "<option value=''>无</option>";
        options += "<option value='cws_role'>- 角色 -</option>";
        while (fieldIr.hasNext()) {
            FormField ff = (FormField) fieldIr.next();
            options += "<option value='" + ff.getName() + "' id='" + ff.getFieldType() + "' name='" + ff.getMacroType() + "' lrc='" + ff.getType() + "'>" + ff.getTitle() + "</option>";
        }

        // 解析表单中的元素，包括控件、表格行、span、div
        StringBuffer opts = new StringBuffer();
        opts.append("<option value=''>无</option>");
        JSONArray ary = FormParser.getElements(request, fd);
        for (int i = 0; i < ary.length(); i++) {
            JSONObject json = ary.getJSONObject(i);
            String objId = json.getString("id");
            String objName = json.getString("name");
            if ("".equals(objId)) {
                objId = objName;
            }
            String objTitle = json.getString("title").replaceAll("&nbsp;", "").trim();
            if (!"".equals(objName)) {
                objTitle = objName;
            } else {
                if ("".equals(objTitle)) {
                    objTitle = objId;
                }
            }

            String tagName = json.getString("tagName");
            if (tagName.equals("tr")) {
                tagName = "表格行";
            } else if (tagName.equals("input")) {
                tagName = "输入框";
            } else if (tagName.equals("textarea")) {
                tagName = "多行输入框";
            } else if (tagName.equals("select")) {
                tagName = "下拉菜单";
            } else if (tagName.equals("checkbox")) {
                tagName = "复选框";
            } else if (tagName.equals("radio")) {
                tagName = "单选框";
            }
            opts.append("<option value='" + objId + "'>" + tagName.toUpperCase() + ": " + objTitle + "(" + objId + ")</option>");
        }

        // 节点在控件中的内部名称
        String internalName = ParamUtil.get(request, "internalName");
        if ("".equals(internalName)) {
            // out.print(SkinUtil.makeInfo(request, "请选择节点！"));
            // return;
        }
        // 不再根据节点来控制，而是采用全局控制
        internalName = "defaultNode";
    %>
    <jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
    <jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
    <jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <%
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }

        String views = wpd.getViews();
        String ne = "&lt;&gt;"; // <> 不等于符号
        views = views.replaceAll("\r\n", "");
        views = views.replaceAll("<>", ne);

        boolean isUseFormViewRule = true;
        if (wpd != null) {
            isUseFormViewRule = wpd.isUseFormViewRule();
        }
    %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>

    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <script src="../js/bootstrap/js/bootstrap.min.js"></script>

    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../js/jquery.xmlext.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <script src="../js/layui/layui.js" charset="utf-8"></script>
    <script language="JavaScript">
        var divCount = 0;
        var count = 0;
        var rowCount = 1; // 因为默认有一行空的规则，实际在遍历中rowCount++后从2开始

        function onload() {
            // var xml = $.parseXML(o("views").value); // o("views").value 中如果有被转义的<>则会被恢复成<>，致xml解析出错
            var xml = $.parseXML('<%=views%>');
            $xml = $(xml);

            var arrFields = [];

            $xml.find("actions").children().each(function (i) {
                if ($(this).attr("internalName") == "<%=internalName%>") {
                    if ($(this).children().size() > 0) {
                        // 删除ready事件中addCond中加入的第一行
                        $("#views1").remove();
                    }

                    $(this).children().each(function () {
                        count++;
                        rowCount++;
                        var json = $.parseJSON($(this).find("display").text());

                        var jsonLen = 0;
                        for (var key in json) {
                            jsonLen++;
                        }

                        if (jsonLen == 0)
                            return;

                        var str = "";
                        console.log('if', $(this).find("if").text());
                        var ifAry = $.parseJSON($(this).find("if").text());
                        var conditionText = $(this).find("condition").text();
                        var fieldName = $(this).find("fieldName").text();
                        var operator = $(this).find("operator").text();
                        var val = $(this).find("value").text();
                        console.log('onload val', val, 'rowCount', rowCount);

                        str += "<tr id=\"views" + rowCount + "\">";
                        str += "  <td width=\"9%\" height=\"22\" align=\"left\">";
                        str += "  如果</td>";

                        var options = "<%=options%>";
                        str += '<td width="40%">';
                        str += "<input id=\"condition" + rowCount + "\" name=\"condition" + rowCount + "\" value='" + conditionText + "' class='condition' style='display:none' />";

                        if (ifAry && ifAry.length > 0) {
                            var m = 0;
                            for (var index in ifAry) {
                                console.log('ifAry' + index, ifAry[index]);
                                arrFields.push('fieldName' + count);

                                let item = ifAry[index];

                                str += "<div id='divtd" + count + "'>";
                                str += "<span id='cond" + count + "'>";
                                str += '<select name="fieldName' + count + '" id="fieldName' + count + '" num="' + count + '" style="display:inline-block;" onChange="changeFieldName(this)">';
                                str += options;
                                str += '</select>';
                                str += '<select name="compare' + count + '" id="compare' + count + '">';
                                str += '    <option value=">=">>=</option>';
                                str += '    <option value="<="><=</option>';
                                str += '    <option value=">">></option>';
                                str += '    <option value="&lt;"><</option>';
                                str += '    <option value="<>"><></option>';
                                str += '    <option value="=">=</option>';
                                str += '</select>';

                                str += '<div id="columnInput' + count + '" style="display:inline">';
                                str += '    <input type="text" name="columnName' + count + '" id="columnName' + count + '" value="' + decodeJSON(item.val).replaceAll("\"", "&quot;") + '" style="display:inline-block;width:100px;"/>';
                                str += '</div>';
                                str += '<div class="dropdown" style="display:inline">';
                                str += '    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">';
                                str += '        <span class="caret"></span>';
                                str += '    </button>';
                                str += '    <ul num="' + count + '" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">';
                                str += '        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">';
                                str += '            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>';
                                str += '       </li>';
                                str += '       <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">';
                                str += '           <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>';
                                str += '        </li>';
                                str += '       <li role="presentation" val="mainFormOpt">';
                                str += '           <a role="menuitem" tabindex="-1" href="#">表单字段</a>';
                                str += '        </li>';
                                str += '       <li role="presentation" val="null">';
                                str += '           <a role="menuitem" tabindex="-1" href="#">空值</a>';
                                str += '        </li>';
                                str += '   </ul>';
                                str += '</div>';
                                str += '</span>';

                                if (m == 0)
                                    str += "  &nbsp;<a href=\"javascript:;\" onclick=\"addIf('views" + rowCount + "')\">+</a>";
                                else
                                    str += "  &nbsp;<a href='javascript:;' onclick=\"$('#divtd" + count + "').remove()\" class='delBtn'>×</a>";
                                str += "  </div>";

                                str += "<script>";
                                str += "$('#fieldName" + count + "').val('" + item.field + "');";
                                str += "$('#compare" + count + "').val('" + item.operator + "');";
                                // str += "$('#columnName" + count + "').val(\"" + decodeJSON(item.val).replaceAll("\"", "&quot;") + "\");";
                                str += "<\/script>";

                                m++;
                                count++;
                            }
                        } else {
                            // 向下兼容
                            str += "<span id='cond" + count + "'>";
                            str += '<select name="fieldName' + count + '" id="fieldName' + count + '" num="' + count + '" style="display:inline-block;" onChange="changeFieldName(this)">';
                            str += options;
                            str += '</select>';
                            str += '<select name="compare' + count + '" id="compare' + count + '">';
                            str += '    <option value=">=">>=</option>';
                            str += '    <option value="<="><=</option>';
                            str += '    <option value=">">></option>';
                            str += '    <option value="&lt;"><</option>';
                            str += '    <option value="<>"><></option>';
                            str += '    <option value="=">=</option>';
                            str += '</select>';

                            str += '<div id="columnInput' + count + '" style="display:inline">';
                            str += '    <input type="text" name="columnName' + count + '" id="columnName' + count + '" value="" style="display:inline-block;width:100px;"/>';
                            str += '</div>';
                            str += '<div class="dropdown" style="display:inline">';
                            str += '    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">';
                            str += '        <span class="caret"></span>';
                            str += '    </button>';
                            str += '    <ul num="' + count + '" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">';
                            str += '        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">';
                            str += '            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>';
                            str += '       </li>';
                            str += '       <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">';
                            str += '           <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>';
                            str += '        </li>';
                            str += '       <li role="presentation" val="mainFormOpt">';
                            str += '           <a role="menuitem" tabindex="-1" href="#">表单字段</a>';
                            str += '        </li>';
                            str += '       <li role="presentation" val="null">';
                            str += '           <a role="menuitem" tabindex="-1" href="#">空值</a>';
                            str += '        </li>';
                            str += '   </ul>';
                            str += '</div>';
                            str += '</span>';

                            // 如果condition不为空，则显示，以向下兼容
                            if ("" != conditionText) {
                                str += "<script>";
                                str += "$('#cond" + count + "').hide();";
                                str += "$('#condition" + count + "').show();";
                                str += "<\/script>";
                            } else {
                                str += "<script>";
                                str += "$('#cond" + count + "').show();";
                                str += "$('#condition" + count + "').hide();";
                                str += "<\/script>";
                            }
                            str += "<script>";
                            str += "$('#fieldName" + count + "').val('" + fieldName + "');";
                            str += "$('#compare" + count + "').val('" + operator + "');";
                            str += "$('#columnName" + count + "').val('" + val + "');";
                            str += "<\/script>";
                        }

                        str += '</td>';
                        str += "<td width=\"4%\">则";
                        str += "</td>";
                        str += "<td width=\"40%\" id=\"td" + rowCount + "\">";

                        arrFields.push('fieldName' + count);

                        var m = 0;
                        for (var key in json) {
                            str += "  <div id=\"divtd" + count + divCount + "\">";
                            str += "  <span id=\"span" + count + "\">";
                            str += "  <select id='field" + count + divCount + "' name='field" + rowCount + "' value='" + key + "' class='field'><%=opts%></select>";
                            str += "  <select id=\"field" + count + divCount + "_display\" name=\"field" + rowCount + "_display\">";
                            str += "    <option value=\"show\" " + ((json[key] == "show") ? "selected" : "") + ">显示</option>";
                            str += "    <option value=\"hide\" " + ((json[key] == "hide") ? "selected" : "") + ">隐藏</option>";
                            str += "  </select></span>";

                            if (m == 0)
                                str += "  &nbsp;<a href=\"javascript:;\" onclick=\"addDisplay('td" + rowCount + "', o('span" + count + "').innerHTML)\">+</a>";
                            else
                                str += "  &nbsp;<a href='javascript:;' onclick=\"$('#divtd" + count + divCount + "').remove()\" class='delBtn'>×</a>";

                            str += "  </div>";

                            str += "<script>";
                            str += "$('#field" + count + divCount + "').val('" + key + "');";
                            str += "<\/script>";

                            arrFields.push('field' + count + divCount);

                            m++;
                            divCount++;
                        }

                        str += "  </td>";
                        str += "<td class='delBtn'>";
                        str += "<a href='javascript:;' title='删除' onclick=\"$('#views" + rowCount + "').remove();\">×</a>";

                        // 第一行的count值为2，因为在页面载入时调用了addCond增加了一行，而在onload事件中又删除了这一行
                        str += "&nbsp;&nbsp;<a href='javascript:;' title='上移' onclick=\"moveUp('views" + rowCount + "')\">↑</a>";
                        str += "&nbsp;&nbsp;<a href='javascript:;' title='下移' onclick=\"moveDown('views" + rowCount + "')\">↓</a>";

                        str += "</td>";
                        str += "</tr>";

                        $("#tabCond").append(str);
                    });

                    for (var k = 0; k < arrFields.length; k++) {
                        $('#' + arrFields[k]).select2();
                    }

                    if (count == 0)
                        count = 1;

                    initDropdownMenu();
                }
            });
        }

        function moveUp(trId) {
            if ($('#' + trId).prev().index() == 0) {
                layer.msg("已到顶部！", {
                    offset: '6px'
                });
                return;
            }
            $('#' + trId).prev().before($('#' + trId));
        }

        function moveDown(trId) {
            if ($('#' + trId).next()[0] == null) {
                layer.msg("已到底部！", {
                    offset: '6px'
                });
                return;
            }
            $('#' + trId).next().after($('#' + trId));
        }
    </script>
</head>
<body onload="onload()">
<%@ include file="flow_inc_menu_top.jsp" %>
<script>
    o("menu10").className = "current";
</script>
<div class="spacerH"></div>
<textarea id="views" name="views" style="display:none"><%=wpd != null ? wpd.getViews() : ""%></textarea>
<table class="tabStyle_1" style="padding:0px; margin:0px; margin-top:3px; width:100%">
    <tr>
        <td align="center">
            <input id="isUseFormViewRule" name="isUseFormViewRule" type="checkbox"
                   value="1" <%=isUseFormViewRule ? "checked" : ""%> onchange="setViewRule(this)" />
            使用表单中的规则
            <%
                if (!isUseFormViewRule) {
            %>
            &nbsp;&nbsp;
            <input type="button" class="btn btn-default" onclick="copyViewRule()" title="复制表单中的显示规则" value="从表单复制"/>
            &nbsp;&nbsp;
            <input type="button" class="btn btn-default" onclick="syncViewRule()" title="将显示规则同步至表单" value="同步至表单"/>
            &nbsp;&nbsp;
            <input type="button" class="btn btn-default" onclick="ModifyView()" value="保存"/>
            &nbsp;&nbsp;
            <input type="button" value="增加" onclick="addCond()" class="btn btn-default"/>
            <%
                }
            %>
        </td>
    </tr>
</table>
<table id="tabCond" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1" style="display:<%=isUseFormViewRule?"none":""%>;padding:0px; margin:0px; margin-top:3px; width:100%">
    <tr>
        <td height="22" colspan="5" align="center">

        </td>
    </tr>
</table>
<div id="mainFormOptDiv" style="display:none"><%=options %></div>
<div style="display:<%=isUseFormViewRule?"none":""%> ;width: 98%; margin: 20px auto">
    注：日期型字段格式，例：2020-10-08，2020-10-09 15:30:10，current表示当前日期
</div>
</body>
<style>
    .delBtn {
        font-size: 16px;
    }

    .condition {
        width: 200px;
    }

    .field {
        width: 230px;
        margin-right: 10px;
    }
</style>
<script>
    function addDisplay(tdId, html) {
        html = "<div id='div" + tdId + divCount + "'>" + html + "&nbsp;<a href='javascript:;' onclick=\"$('#div" + tdId + divCount + "').remove()\" class='delBtn' title='删除'>×</a>";

        var $obj = $(html);
        // 删除原来生成的select2
        $obj.find('.select2-container').remove();
        $("#" + tdId).append($obj.prop('outerHTML'));

        // 重新生成select2
        $('#div' + tdId + divCount).find('.field').removeClass('select2-hidden-accessible');
        $('#div' + tdId + divCount).find('.field').select2();

        divCount++;
    }

    // 添加“如果”部分
    function addIf(tdId) {
        count++;

        // 取该tdId中if部分的第一行
        var spanCond = $('#' + tdId).find('span[id^=cond]')[0];

        var newSpanCond = spanCond.cloneNode();
        newSpanCond.id = "cond" + count;

        var aryChild = spanCond.children;
        for (i = 0; i < aryChild.length; i++) {
            // console.log(aryChild[i]);
            var subObj = aryChild[i].cloneNode(true);
            if (aryChild[i].id.indexOf("fieldName") == 0) {
                subObj.id = "fieldName" + count;
                subObj.name = "fieldName" + count;
                subObj.setAttribute("num", count);
            } else if (aryChild[i].id.indexOf("compare") == 0) {
                subObj.id = "compare" + count;
                subObj.name = "compare" + count;
            } else if (aryChild[i].id.indexOf("dropdown") == 0) {
                subObj.id = "dropdown" + count;
                subObj.setAttribute("num", count);
            } else if (aryChild[i].id.indexOf("columnInput") == 0) {
                subObj.id = "columnInput" + count;

                var columnNameObj = subObj.children[0];
                columnNameObj.id = "columnName" + count;
                columnNameObj.name = "columnName" + count;
            }
            newSpanCond.appendChild(subObj);
        }

        // 删除除已生成的select2
        $(newSpanCond).find('.select2-container').remove();
        $(newSpanCond).find('select[name=fieldName' + count + ']').removeClass();

        var html = "<div id='div" + tdId + count + "'>" + $(newSpanCond).prop("outerHTML") + "&nbsp;<a href='javascript:;' onclick=\"$('#div" + tdId + count + "').remove()\" class='delBtn' title='删除'>×</a>";
        // $('#' + tdId).append(html);
        $(spanCond).parent().append(html);

        // 重新生成select2
        $('#fieldName' + count).select2();

        initDropdownMenu();
    }

    function addCond() {
        count++;

        var str = "<tr id='views" + count + "'>";
        str += "<td>如果</td>";
        // str += "<td ><input id='condition" + count + "' name='condition" + count + "' class='condition' size=6 /></td>";

        var options = "<%=options%>";
        str += '<td>';
        str += "<input id=\"condition" + count + "\" name=\"condition" + count + "\" value='' class='condition' style='display:none' />";
        str += "<span id='cond" + count + "'>";
        str += '<select name="fieldName' + count + '" id="fieldName' + count + '" num="' + count + '" style="display:inline-block;" onChange="changeFieldName(this)">';
        str += options;
        str += '</select>';
        str += '<select name="compare' + count + '" id="compare' + count + '">';
        str += '    <option value=">=">>=</option>';
        str += '    <option value="<="><=</option>';
        str += '    <option value=">">></option>';
        str += '    <option value="&lt;"><</option>';
        str += '    <option value="<>"><></option>';
        str += '    <option value="=">=</option>';
        str += '</select>';

        str += '<div id="columnInput' + count + '" style="display:inline">';
        str += '    <input type="text" name="columnName" id="columnName' + count + '" value="" style="display:inline-block;width:100px;"/>';
        str += '</div>';
        str += '<div class="dropdown" style="display:inline">';
        str += '    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">';
        str += '        <span class="caret"></span>';
        str += '    </button>';
        str += '    <ul num="' + count + '" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">';
        str += '        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">';
        str += '            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>';
        str += '       </li>';
        str += '       <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">';
        str += '           <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>';
        str += '        </li>';
        str += '       <li role="presentation" val="mainFormOpt">';
        str += '           <a role="menuitem" tabindex="-1" href="#">表单字段</a>';
        str += '        </li>';
        str += '       <li role="presentation" val="null">';
        str += '           <a role="menuitem" tabindex="-1" href="#">空值</a>';
        str += '        </li>';
        str += '   </ul>';
        str += '</div>';
        str += '</span>';

        str += "&nbsp;<a href='javascript:;' onclick=\"addIf('views" + count + "')\">+</a>";

        str += '</td>';

        str += "<td>则</td>";
        str += "<td id='td" + count + "'>";
        str += "<div><span id='span" + count + "'><select class='field' id='field" + count + "' name='field" + count + "'><%=opts%></select>";
        str += "<select id='field" + count + "_display' name='field" + count + "_display'><option value='show'>显示</option><option value='hide'>隐藏</option></select></span>";
        str += "&nbsp;<a href='javascript:;' onclick=\"addDisplay('td" + count + "', span" + count + ".innerHTML)\">+</a>";
        str += "</div>";
        str += "</td>";
        str += "<td class='delBtn'><a href='javascript:;' onclick=\"$('#views" + count + "').remove();\" title='删除'>×</a>";

        str += "&nbsp;&nbsp;<a href='javascript:;' title='上移' onclick=\"moveUp('views" + count + "')\">↑</a>";
        str += "&nbsp;&nbsp;<a href='javascript:;' title='下移' onclick=\"moveDown('views" + count + "')\">↓</a>";

        str += "</td>";
        str += "</tr>";
        $("#tabCond").append(str);

        $('#fieldName' + count).select2();
        $('#field' + count).select2();

        initDropdownMenu();
    }

    function changeFieldName(obj) {
        var n = obj.getAttribute("num");
        // console.log('changeFieldName obj', obj);
        var index = obj.selectedIndex;
        var val = obj.options[index].getAttribute("value");
        if (val == "cws_status") {
            var htmlStr = "<select name='columnName' id='columnName" + n + "'>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>";
            htmlStr += "</select>";

            $("#columnInput" + n).html(htmlStr);

            $("#compare" + n).empty();
            $("#compare" + n).append("<option value='='>=</option><option value='<>'><></option>");

            return;
        } else if (val == "cws_flag") {
            var htmlStr = "<select name='columnName' id='columnName" + n + "'>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_NO%>'>未冲抵</option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_YES%>'>已冲抵</option>";
            htmlStr += "</select>";

            $("#columnInput" + n).html(htmlStr);

            $("#compare" + n).empty();
            $("#compare" + n).append("<option value='='>=</option>");

            return;
        } else if (val == "cws_role") {
            var htmlStr = "<select name='columnName' id='columnName" + n + "'>";
            // htmlStr += "<option value=''>无</option>";
            htmlStr += "<%=roleOptions%>";
            htmlStr += "</select>";
            $("#columnInput" + n).html(htmlStr);
            return;
        }

        var fieldType = obj.options[index].getAttribute("id");
        if (fieldType == "<%=FormField.FIELD_TYPE_TEXT%>" || fieldType == "<%=FormField.FIELD_TYPE_VARCHAR%>") {
            $("#compare" + n).empty();
            $("#compare" + n).append("<option value='='>=</option><option value='<>'><></option>");
        } else {
            $("#compare" + n).empty();
            $("#compare" + n).append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
        }

        var fieldNameType = obj.options[index].getAttribute("name");
        var lrc = obj.options[index].getAttribute("lrc");
        $.ajax({
            type: "post",
            url: "../visual/module_combination_condition.jsp",
            data: {
                op: "selectMactl",
                fieldNameType: fieldNameType,
                val: val,
                isMacro: lrc,
                moduleCode: "<%=fd.getCode()%>",
                mainFormCode: "<%=fd.getCode()%>"
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    $("#columnInput" + n).html("<select name='columnName' id='columnName" + n + "'>" + data.msg + "</select>");
                } else if (data.ret == "2") {
                    $("#compare" + n).empty();
                    $("#compare" + n).append("<option value='='>=</option><option value='<>'><></option>");
                    $("#columnInput" + n).html("<select name='columnName' id='columnName" + n + "'>" + data.msg + "</select>");
                } else {
                    $("#columnInput" + n).html("<input type='text' name='columnName' id='columnName" + n + "' value=''/>");
                }
            },
            complete: function (XMLHttpRequest, status) {
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                //alert(XMLHttpRequest.responseText);
            }
        });
    }

    function initDropdownMenu() {
        $('.dropdown-menu li').on('click', function () {
            var num = $(this).parent().attr("num");
            if ($(this).attr("val") == "mainFormOpt") {
                $("#columnInput" + num).html("<select name='columnName' id='columnName" + num + "'>" + $('#mainFormOptDiv').html() + "</select>");
            } else if ($(this).attr("val") == "null") {
                $("#columnInput" + num).html("<input name='columnName' id='columnName" + num + "' value='null' />");
            } else {
                $("#columnInput" + num).html("<input type='text' name='columnName' id='columnName" + num + "' readonly val='" + $(this).attr("val") + "' value='" + $(this).children(0).text() + "'/>");
            }
        });
    }

    $(function () {
        addCond();
    })

    function makeViews() {
        var str = "";
        var isValid = true;
        $("tr[id^='views']").each(function () {
            var trId = $(this).attr("id");
            var id = trId.substring("views".length);
            var ifs = "";

            // 向下兼容, condition为旧的写法
            if ($('#condition' + id).val() == '') {
                $("#" + trId + " select[name^='fieldName']").each(function (k) {
                    if ($(this).val().trim() == "") {
                        layer.msg("请选择如果项！", {
                            offset: '6px'
                        });
                        isValid = false;
                        return false;
                    }

                    var num = $(this).attr("num");
                    var val = "";
                    if ($("#columnName" + num).attr("val")) {
                        val = $("#columnName" + num).attr("val");
                        console.log('val attr', val);
                    } else {
                        val = $("#columnName" + num).val();
                        console.log('val', val);
                    }

                    val = encodeJSON(val);
                    console.log('val encodeJSON', val);

                    var operator = $("#compare" + num).val();
                    operator = operator.replaceAll("<", "&lt;");
                    operator = operator.replaceAll(">", "&gt;");

                    if (ifs == "")
                        ifs += "{\"field\":\"" + $(this).val() + "\", \"operator\":\"" + operator + "\", \"val\":\"" + val + "\"}";
                    else
                        ifs += ", {\"field\":\"" + $(this).val() + "\", \"operator\":\"" + operator + "\", \"val\":\"" + val + "\"}";
                });
                ifs = "[" + ifs + "]";
                console.log('ifs', ifs);
            }

            var displays = "";
            console.log('row id', id);
            $("#td" + id + " select[name='field" + id + "']").each(function (k) {
                if ($(this).val().trim() == "") {
                    layer.msg("请选择显示或隐藏项！", {
                        offset: '6px'
                    });
                    isValid = false;
                    return false;
                }

                console.log('field this', this);
                var nm = $(this).attr("name");

                var ary = document.getElementsByName(nm + "_display");

                var dis = ary[k].value;
                if (displays == "")
                    displays += "\"" + $(this).val() + "\":\"" + dis + "\"";
                else
                    displays += ",\"" + $(this).val() + "\":\"" + dis + "\"";
            });
            displays = "{" + displays + "}";

            str += "<view>";

            /*str += "<condition>" + $("#condition" + id).val().replaceAll("'", "\"") + "</condition>";
            str += "<fieldName>"+$("#fieldName"+id).val()+"</fieldName>";
            var compare = $("#compare"+id).val();
            // > < &gt; &lt;
            compare = compare.replaceAll("<", "&lt;");
            compare = compare.replaceAll(">", "&gt;");
            str += "<operator>"+compare+"</operator>";
            var val;
            if ($("#columnName"+id).attr("val")) {
                str += "<value>"+$("#columnName"+id).attr("val")+"</value>";
            }
            else {
                str += "<value>"+$("#columnName"+id).val()+"</value>";
            }*/

            str += "<if>" + ifs + "</if>";
            str += "<display>" + displays + "</display>";
            str += "</view>";
        });

        console.log('str', str);

        if (!isValid)
            return;

        // 查找internalName对应的项，如果有，则删除
        var viewsText = ""; // o("views").value; o("views").value 中如果有被转义的<>则会被恢复成<>，致xml解析出错
        if (viewsText == "") {
            viewsText = "<actions></actions>";
        }
        var xml = $.parseXML(viewsText);
        $xml = $(xml);
        var isFound = false;
        $xml.find("actions").children().each(function (i) {
            if ($(this).attr("internalName") == "<%=internalName%>") {
                $(this).remove();
                return false;
            }
        });

        var $elem = $($.parseXML("<action internalName='<%=internalName%>'>" + str + "</action>"));
        var newNode = null;
        if (typeof document.importNode == 'function') {
            newNode = document.importNode($elem.find('action').get(0), true);
        } else {
            newNode = $elem.find('action').get(0);
        }

        $xml.find("actions").get(0).appendChild(newNode);

        $.ajax({
            type: "post",
            url: "flow_designer_action_view.jsp",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                op: "save",
                dirCode: "<%=flowTypeCode%>",
                code: "<%=fd.getCode()%>",
                xml: $xml.xml()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                layer.msg(data.msg, {
                    offset: '6px'
                });
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

    function ModifyView() {
        makeViews();
    }

    function encodeJSON(jsonString) {
        jsonString = jsonString.replace(/=/gi, "%eq");
        jsonString = jsonString.replace(/\{/gi, "%lb");
        jsonString = jsonString.replace(/\}/gi, "%rb");
        jsonString = jsonString.replace(/,/gi, "%co"); // 逗号
        jsonString = jsonString.replace(/\"/gi, "%dq");
        jsonString = jsonString.replace(/'/gi, "%sq");
        jsonString = jsonString.replace(/\r\n/g, "%rn"); // 回车换行
        jsonString = jsonString.replace(/\n/g, "%n");
        return jsonString;
    }

    // 对字符串中的引号进行解码
    function decodeJSON(jsonString) {
        jsonString = jsonString.replace(/%dq/gi, '"');
        jsonString = jsonString.replace(/%sq/gi, "'");

        // jsonString = jsonString.replace(/%rn/g, "\r\n")
        // jsonString = jsonString.replace(/%n/g, "\n")
        return jsonString;
    }

    function setViewRule(chkObj) {
        $.ajax({
            type: "post",
            url: "flow_designer_action_view.jsp",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                op: "setViewRule",
                dirCode: "<%=flowTypeCode%>",
                code: "<%=fd.getCode()%>",
                isUseFormViewRule: $(chkObj).prop('checked')
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    layer.alert(data.msg, {
                        yes: function() {
                            window.location.reload();
                        }
                    });
                } else {
                    layer.msg(data.msg, {
                        offset: '6px'
                    });
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

    function syncViewRule() {
        layer.confirm('您确认要同步显示规则至表单中么?', {icon: 3, title: '提示'}, function (index) {
            $.ajax({
                type: "post",
                url: "flow_designer_action_view.jsp",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                data: {
                    op: "syncViewRule",
                    dirCode: "<%=flowTypeCode%>",
                    code: "<%=fd.getCode()%>"
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    layer.msg(data.msg, {
                        offset: '6px'
                    });
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    alert(XMLHttpRequest.responseText);
                }
            });
        });
    }

    function copyViewRule() {
        layer.confirm('您确认要复制表单中的显示规则么?', {icon: 3, title: '提示'}, function (index) {
            $.ajax({
                type: "post",
                url: "flow_designer_action_view.jsp",
                contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                data: {
                    op: "copyViewRule",
                    dirCode: "<%=flowTypeCode%>",
                    code: "<%=fd.getCode()%>"
                },
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == 1) {
                        layer.alert(data.msg, {
                            yes: function() {
                                window.location.reload();
                            }
                        });
                    } else {
                        layer.msg(data.msg, {
                            offset: '6px'
                        });
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
        });
    }
</script>
</HTML>