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
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");

    String code = ParamUtil.get(request, "code");
    FormDb fd = new FormDb();
    fd = fd.getFormDb(code);

    if ("save".equals(op)) {
        String xml = ParamUtil.get(request, "xml");
        fd.setCheckSetup(xml);
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

    Vector fieldV = fd.getFields();
    Iterator fieldIr = fieldV.iterator();
    String options = "";
    String optionsVar = "";
    Map<String, FormField> checkboxGroupMap = new HashMap<String, FormField>();
    while (fieldIr.hasNext()) {
        FormField ff = (FormField) fieldIr.next();
        options += "<option value='" + ff.getName() + "' id='" + ff.getFieldType() + "' name='" + ff.getMacroType() + "' lrc='" + ff.getType() + "'>" + ff.getTitle() + "</option>";
        optionsVar += "<option value='{$" + ff.getName() + "}' id='" + ff.getFieldType() + "' name='" + ff.getMacroType() + "' lrc='" + ff.getType() + "'>" + ff.getTitle() + "</option>";
    
        if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
            String desc = StrUtil.getNullStr(ff.getDescription());
            String chkGroup = StrUtil.getNullStr(ff.getDescription());
            if (!"".equals(chkGroup)) {
                if (!checkboxGroupMap.containsKey(chkGroup)) {
                    checkboxGroupMap.put(chkGroup, ff);
                }
                else {
                    continue;
                }
            }
        }
    }
    if (checkboxGroupMap.size()>0) {
        Iterator ir = checkboxGroupMap.keySet().iterator();
        while (ir.hasNext()) {
            String key = (String)ir.next();
            FormField ff = checkboxGroupMap.get(key);
            options += "<option value='" + ModuleUtil.CHECKBOX_GROUP_PREFIX + key + "' id='" + ff.getFieldType() + "' name='" + ff.getMacroType() + "' lrc='" + ff.getType() + "'>复选框组：" + key + "</option>";
        }
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>表单校验规则</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />  
	<script src="../js/bootstrap/js/bootstrap.min.js"></script>	
    
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../js/jquery.xmlext.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <style>
        input[name^="columnName"] {
            width: 110px;
        }
        select[name^="columnName"] {
            width: 110px;
        }
        .delBtn {
            font-size: 16px;
        }
        .field {
            width: 200px;
            margin-right: 10px;
        }
        .desc {
            width:200px;
            margin-right: 10px;
        }
    </style>
    <script language="JavaScript">
        var valCount = 0;
        var count = 0;
        var rowCount = 0;

        function onload() {
            var rules = '<%=fd.getCheckSetup()%>';
            if (rules == "") {
                return;
            }
            // console.log(rules);
            valCount = 0;
            count = 0;
            rowCount = 0;

            var xml = $.parseXML(rules);
            $xml = $(xml);

            // 删除第一行
            $("#rules0").remove();
            
            $xml.find("rules").children().each(function (i) {
                var str = "";
                var desc = $(this).find("desc").text();
                var ifAry = $.parseJSON($(this).find("if").text());
                var jsonAry = $.parseJSON($(this).find("then").text());
                if (ifAry.length==0 || jsonAry.length == 0)
                    return;

                var options = "<%=options%>";

                str += "<tr id=\"rules" + rowCount + "\">";
                str += "  <td width=\"25%\" height=\"22\" align=\"left\">";
                str += "  规则：&nbsp;<input id='desc" + rowCount + "' name='desc' class='desc' value='" + desc + "' />如果</td>";
                str += '  <td width="30%" id="tdCond' + rowCount + '">';
                
                var m = 0;
                for (var index in ifAry) {
                    str += "  <div id='divtd" + count + "'>";
                    str += "  <span id=\"cond" + count + "\">";
                    str += "  <select id='fieldName" + count + "' name='fieldName' num='" + count + "' onChange='changeFieldName(this)'><%=options%></select>";
                    str += "  <select id=\"compare" + count + "\" name=\"compare\">";
                    str += "    <option value='<>' " + ((ifAry[index].cond == "<>") ? "selected" : "") + "> <> </option>";
                    str += "    <option value='>=' " + ((ifAry[index].cond == ">=") ? "selected" : "") + "> >= </option>";
                    str += "    <option value='>' " + ((ifAry[index].cond == ">") ? "selected" : "") + "> > </option>";
                    str += "    <option value='<=' " + ((ifAry[index].cond == "<=") ? "selected" : "") + "> <= </option>";
                    str += "    <option value='<' " + ((ifAry[index].cond == "<") ? "selected" : "") + "> < </option>";
                    str += "    <option value='=' " + ((ifAry[index].cond == "=") ? "selected" : "") + "> = </option>";
                    str += "  </select>";
                    str += "<input id='columnName" + count + "' name='columnName' value='' />";
                    str += '<div id="dropdown' + count + '" num="' + count + '" class="dropdown" style="display:inline">';
                    str += '    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">';
                    str += '        <span class="caret"></span>';
                    str += '    </button>';
                    str += '    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">';
                    str += '        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">';
                    str += '            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>';
                    str += '       </li>';
                    str += '       <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">';
                    str += '           <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>';
                    str += '        </li>';
                    str += '       <li role="presentation" val="mainFormOpt">';
                    str += '           <a role="menuitem" tabindex="-1" href="#">表单字段</a>';
                    str += '        </li>';
                    str += '   </ul>';
                    str += '</div>';
                    str += "  </span>";

                    if (m == 0)
                        str += "  &nbsp;<a href=\"javascript:;\" onclick=\"addIf('tdCond" + rowCount + "')\">+</a>";
                    else
                        str += "  &nbsp;<a href='javascript:;' onclick=\"$('#divtd" + count + "').remove()\" class='delBtn'>×</a>";
                    str += "  </div>";
                    str += "<script>";
                    str += "$('#fieldName" + count + "').val('" + ifAry[index].field + "');";
                    str += "$('#compare" + count + "').val('" + ifAry[index].operator + "');";
                    str += "$('#columnName" + count + "').val('" + ifAry[index].val + "');";
                    str += "<\/script>";

                    m++;
                    count++;
                }
                
                str += '</td>';
                str += "  <td width=\"4%\">则";
                str += "  </td>";
                str += "  <td width=\"34%\" id=\"tdVal" + rowCount + "\">";

                m = 0;
                for (var index in jsonAry) {
                    str += "<div id='divtdVal" + rowCount + valCount + "'>";
                    str += "  <span id=\"spanVal" + valCount + "\">";
                    str += "  <select id='field" + valCount + "' name='field' num='" + valCount + "' onChange='changeField(this)'><%=options%></select>";
                    str += "  <select id=\"operator" + valCount + "\" name=\"operator\">";
                    str += "    <option value='<>' " + ((jsonAry[index].operator == "<>") ? "selected" : "") + "> <> </option>";
                    str += "    <option value='>=' " + ((jsonAry[index].operator == ">=") ? "selected" : "") + "> >= </option>";
                    str += "    <option value='>' " + ((jsonAry[index].operator == ">") ? "selected" : "") + "> > </option>";
                    str += "    <option value='<=' " + ((jsonAry[index].operator == "<=") ? "selected" : "") + "> <= </option>";
                    str += "    <option value='<' " + ((jsonAry[index].operator == "<") ? "selected" : "") + "> < </option>";
                    str += "    <option value='=' " + ((jsonAry[index].operator == "=") ? "selected" : "") + "> = </option>";
                    str += "  </select>";
                    str += "  <input id='val" + valCount + "' name='val' value='' />";
                    str += '  <div id="dropdownVal' + valCount + '" num="' + valCount + '" class="dropdown" style="display:inline">';
                    str += '    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">';
                    str += '        <span class="caret"></span>';
                    str += '    </button>';
                    str += '    <ul kind="val" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">';
                    str += '        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">';
                    str += '            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>';
                    str += '       </li>';
                    str += '       <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">';
                    str += '           <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>';
                    str += '        </li>';
                    str += '       <li role="presentation" val="mainFormOpt">';
                    str += '           <a role="menuitem" tabindex="-1" href="#">表单字段</a>';
                    str += '        </li>';
                    str += '   </ul>';
                    str += '  </div>';
                    str += "  </span>";

                    if (m == 0)
                        str += "  &nbsp;<a href=\"javascript:;\" onclick=\"addThen('tdVal" + rowCount + "')\">+</a>";
                    else
                        str += "  &nbsp;<a href='javascript:;' onclick=\"$('#divtdVal" + rowCount + valCount + "').remove()\" class='delBtn'>×</a>";
                    str += "</div>";
                    str += "<script>";
                    str += "$('#field" + valCount + "').val('" + jsonAry[index].field + "');";
                    str += "$('#operator" + valCount + "').val('" + jsonAry[index].operator + "');";
                    str += "$('#val" + valCount + "').val('" + jsonAry[index].val + "');";
                    str += "<\/script>";

                    m++;
                    valCount++;
                }

                str += "  </td>";
                str += "<td class='delBtn'><a href='javascript:;' onclick=\"$('#rules" + rowCount + "').remove();\">×</a></td>";

                str += "</tr>";
                
                rowCount ++;

                $("#tabCond").append(str);
            });

            initDropdownMenuEvent();
        }
    </script>
</head>
<body onload="onload()">
<%@ include file="form_edit_inc_menu_top.jsp" %>
<script>
    o("menu6").className = "current";
</script>
<div class="spacerH"></div>
<table width="100%" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent80" id="tabCond" style="padding:0px; margin:0px; margin-top:3px; width:100%">
    <tr>
        <td height="22" colspan="5" align="center"><input name="okbtn" type="button" onclick="ModifyRules()" value="保存"/>
            &nbsp;&nbsp;
            <input type="button" value="增加" onclick="addCond()"/></td>
    </tr>
</table>
<div id="mainFormOptDiv" style="display:none"><%=optionsVar %></div>
<pre id="rules" style="display: none"><%=fd.getCheckSetup()%></pre>
</BODY>
<script>
    // 添加“则”部分
    function addThen(tdId) {
        valCount++;

        // 取该tdId中then部分的第一行
        var spanVal = $('#' + tdId).find('span[id^=spanVal]')[0];
        var newSpanVal = spanVal.cloneNode();
        newSpanVal.id = "spanVal" + valCount;

        var aryChild = spanVal.children;
        for (i=0; i<aryChild.length; i++) {
            console.log(aryChild[i]);
            var subObj = aryChild[i].cloneNode(true);
            if (aryChild[i].id.indexOf("field")==0) {
                subObj.id = "field" + valCount;
                subObj.setAttribute("num", valCount);
            }
            else if (aryChild[i].id.indexOf("operator")==0) {
                subObj.id = "operator" + valCount;
            }
            else if (aryChild[i].id.indexOf("val")==0) {
                subObj.id = "val" + valCount;
            }
            else if (aryChild[i].id.indexOf("dropdownVal")==0) {
                subObj.id = "dropdownVal" + valCount;
                subObj.setAttribute("num", valCount);
            }
            newSpanVal.appendChild(subObj);
        }

        var html = "<div id='div" + tdId + valCount + "'>" + $(newSpanVal).prop("outerHTML") + "&nbsp;<a href='javascript:;' onclick=\"$('#div" + tdId + valCount + "').remove()\" class='delBtn' title='删除'>×</a>";
        $('#' + tdId).append(html);

        initDropdownMenuEvent();
    }

    // 添加“如果”部分
    function addIf(tdId) {
        count++;
        
        // 取该tdId中if部分的第一行
        var spanCond = $('#' + tdId).find('span[id^=cond]')[0];
        
        var newSpanCond = spanCond.cloneNode();
        newSpanCond.id = "cond" + count;
        
        var aryChild = spanCond.children;
        for (i=0; i<aryChild.length; i++) {
            console.log(aryChild[i]);
            var subObj = aryChild[i].cloneNode(true);
            if (aryChild[i].id.indexOf("fieldName")==0) {
                subObj.id = "fieldName" + count;
                subObj.setAttribute("num", count);
            }
            else if (aryChild[i].id.indexOf("compare")==0) {
                subObj.id = "compare" + count;
            }
            else if (aryChild[i].id.indexOf("columnName")==0) {
                subObj.id = "columnName" + count;
            }
            else if (aryChild[i].id.indexOf("dropdown")==0) {
                subObj.id = "dropdown" + count;
                subObj.setAttribute("num", count);
            }
            newSpanCond.appendChild(subObj);
        }
        
        var html = "<div id='div" + tdId + count + "'>" + $(newSpanCond).prop("outerHTML") + "&nbsp;<a href='javascript:;' onclick=\"$('#div" + tdId + count + "').remove()\" class='delBtn' title='删除'>×</a>";
        $('#' + tdId).append(html);

        initDropdownMenuEvent();
    }

    // 添加一条规则
    function addCond() {
        var str = "<tr id='rules" + rowCount + "'>";
        str += "<td>规则：&nbsp;<input id='desc" + rowCount + "' name='desc' class='desc' />如果</td>";
        var options = "<%=options%>";
        str += '<td id="tdCond' + rowCount + '">';
        str += "<span id='cond" + count + "'>";
        str += '<select name="fieldName" id="fieldName' + count + '" num="' + count + '" style="display:inline-block;" onChange="changeFieldName(this)">';
        str += options;
        str += '</select>';
        str += '<select name="compare" id="compare' + count + '">';
        str += '    <option value=">=">>=</option>';
        str += '    <option value="<="><=</option>';
        str += '    <option value=">">></option>';
        str += '    <option value="&lt;"><</option>';
        str += '    <option value="<>"><></option>';
        str += '    <option value="=">=</option>';
        str += '</select>';
        str += '<input type="text" name="columnName" id="columnName' + count + '" value="" style="display:inline-block;"/>';
        str += '<div id="dropdownVal' + count + '" num="' + count + '" class="dropdown" style="display:inline">';
        str += '    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">';
        str += '        <span class="caret"></span>';
        str += '    </button>';
        str += '    <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">';
        str += '        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">';
        str += '            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>';
        str += '       </li>';
        str += '       <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">';
        str += '           <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>';
        str += '        </li>';
        str += '       <li role="presentation" val="mainFormOpt">';
        str += '           <a role="menuitem" tabindex="-1" href="#">表单字段</a>';
        str += '        </li>';
        str += '   </ul>';
        str += '</div>';
        str += '</span>';

        str += "&nbsp;<a href='javascript:;' onclick=\"addIf('tdCond" + count + "')\">+</a>";

        str += '</td>';

        str += "<td>则</td>";
        str += "<td id='tdVal" + rowCount + "'>";
        str += "<div><span id='spanVal" + valCount + "'><select id='field" + valCount + "' name='field" + "' num='" + valCount + "' onchange='changeField(this)'><%=options%></select>";
        str += "<select id='operator" + valCount + "' name='operator'>";
        str += "    <option value='<>'> <> </option>";
        str += "    <option value='>='> >= </option>";
        str += "    <option value='>'> > </option>";
        str += "    <option value='<='> <= </option>";
        str += "    <option value='<'> < </option>";
        str += "    <option value='='> = </option>";
        str += "</select>";
        str += "<input id='val" + valCount + "' name='val' value='' />";
        str += '<div id="dropdownVal' + valCount + '" class="dropdown" num="' + valCount + '" style="display:inline">';
        str += '    <button type="button" class="btn dropdown-toggle" id="dropdownMenu1" data-toggle="dropdown">';
        str += '        <span class="caret"></span>';
        str += '    </button>';
        str += '    <ul kind="val" class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1">';
        str += '        <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_DATE %>">';
        str += '            <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_DATE) %></a>';
        str += '       </li>';
        str += '       <li role="presentation" val="<%=ModuleUtil.FILTER_CUR_USER %>">';
        str += '           <a role="menuitem" tabindex="-1" href="#"><%=ModuleUtil.getFilterDesc(request, ModuleUtil.FILTER_CUR_USER) %></a>';
        str += '       </li>';
        str += '       <li role="presentation" val="mainFormOpt">';
        str += '           <a role="menuitem" tabindex="-1" href="#">表单字段</a>';
        str += '       </li>';
        str += '   </ul>';
        str += '</div>';
        
        str += "</span>";
        str += "&nbsp;<a href='javascript:;' onclick=\"addThen('tdVal" + rowCount + "')\">+</a>";
        str += "</div>";
        str += "</td>";
        str += "<td class='delBtn'><a href='javascript:;' onclick=\"$('#rules" + rowCount + "').remove();\" title='删除'>×</a></td>";
        str += "</tr>";
        $("#tabCond").append(str);

        rowCount++;
        count++;
        valCount++;

        initDropdownMenuEvent();
    }

    // 当如果部分的表单域变化时
    function changeFieldName(obj) {
        var n = obj.getAttribute("num");
        var index = obj.selectedIndex;
        var val = obj.options[index].getAttribute("value");
        if (val=="cws_status") {
            var htmlStr = "<select name='columnName' id='columnName"+n+"'>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>";
            htmlStr += "</select>";

            $("#columnName"+n).prop("outerHTML", htmlStr);
            $("#compare"+n).empty();
            $("#compare"+n).append("<option value='='>=</option><option value='<>'><></option>");
            return;
        }
        else if (val=="cws_flag") {
            var htmlStr = "<select name='columnName' id='columnName"+n+"'>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_NO%>'>未冲抵</option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_YES%>'>已冲抵</option>";
            htmlStr += "</select>";

            $("#columnInput"+n).html(htmlStr);
            $("#compare"+n).empty();
            $("#compare"+n).append("<option value='='>=</option>");
            return;
        }

        var fieldType = obj.options[index].getAttribute("id");
        if (fieldType == "<%=FormField.FIELD_TYPE_TEXT%>" || fieldType == "<%=FormField.FIELD_TYPE_VARCHAR%>"){
            $("#compare"+n).empty();
            $("#compare"+n).append("<option value='='>=</option><option value='<>'><></option>");
        }else{
            $("#compare"+n).empty();
            $("#compare"+n).append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
        }

        var fieldNameType = obj.options[index].getAttribute("name");
        var lrc = obj.options[index].getAttribute("lrc");
        $.ajax({
            type: "post",
            url: "../visual/module_combination_condition.jsp",
            data : {
                op:"selectMactl",
                fieldNameType: fieldNameType,
                val:val,
                isMacro:lrc,
                moduleCode:"<%=code%>",
                mainFormCode:"<%=code%>"
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
            },
            success: function(data, status){
                data = $.parseJSON(data);
                // console.log(data);
                if (data.ret=="1") {
                    $("#columnName"+n).prop("outerHTML", "<select name='columnName' id='columnName"+n+"'>"+data.msg+"</select>");
                }else if (data.ret == "2"){
                    $("#compare"+n).empty();
                    $("#compare"+n).append("<option value='='>=</option><option value='<>'><></option>");
                    $("#columnName"+n).prop("outerHTML", "<select name='columnName' id='columnName"+n+"'>"+data.msg+"</select>");
                }else{
                    $("#columnName"+n).prop("outerHTML", "<input type='text' name='columnName' id='columnName"+n+"' value=''/>");
                }
            },
            complete: function(XMLHttpRequest, status){
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                //alert(XMLHttpRequest.responseText);
            }
        });
    }

    // 当则部分的表单域变化时
    function changeField(obj) {
        var n = obj.getAttribute("num");

        var index = obj.selectedIndex;
        var val = obj.options[index].getAttribute("value");
        if (val=="cws_status") {
            var htmlStr = "<select name='columnName' id='columnName"+n+"'>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>";
            htmlStr += "</select>";

            $("#val"+n).prop("outerHTML", htmlStr);
            $("#operator"+n).empty();
            $("#operator"+n).append("<option value='='>=</option><option value='<>'><></option>");
            return;
        }
        else if (val=="cws_flag") {
            var htmlStr = "<select name='columnName' id='columnName"+n+"'>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_NO%>'>未冲抵</option>";
            htmlStr += "<option value='<%=com.redmoon.oa.flow.FormDAO.FLAG_AGAINST_YES%>'>已冲抵</option>";
            htmlStr += "</select>";

            $("#val"+n).html(htmlStr);
            $("#operator"+n).empty();
            $("#operator"+n).append("<option value='='>=</option>");
            return;
        }

        var fieldType = obj.options[index].getAttribute("id");
        if (fieldType == "<%=FormField.FIELD_TYPE_TEXT%>" || fieldType == "<%=FormField.FIELD_TYPE_VARCHAR%>"){
            $("#operator"+n).empty();
            $("#operator"+n).append("<option value='='>=</option><option value='<>'><></option>");
        }else{
            $("#operator"+n).empty();
            $("#operator"+n).append("<option value='>='>>=</option><option value='<='><=</option><option value='>'>></option><option value='<'><</option><option value='='>=</option><option value='<>'><></option>");
        }

        var fieldNameType = obj.options[index].getAttribute("name");
        var lrc =obj.options[index].getAttribute("lrc");
        $.ajax({
            type: "post",
            url: "../visual/module_combination_condition.jsp",
            data : {
                op:"selectMactl",
                fieldNameType: fieldNameType,
                val:val,
                isMacro:lrc,
                moduleCode:"<%=code%>",
                mainFormCode:"<%=code%>"
            },
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
            },
            success: function(data, status){
                data = $.parseJSON(data);
                console.log(data);
                if (data.ret=="1") {
                    $("#val"+n).prop("outerHTML", "<select name='val' id='val"+n+"'>"+data.msg+"</select>");
                }else if (data.ret == "2"){
                    $("#operator"+n).empty();
                    $("#operator"+n).append("<option value='='>=</option><option value='<>'><></option>");
                    $("#val"+n).prop("outerHTML", "<select name='val' id='val"+n+"'>"+data.msg+"</select>");
                }else{
                    $("#val"+n).prop("outerHTML", "<input type='text' name='val' id='val"+n+"' value=''/>");
                }
            },
            complete: function(XMLHttpRequest, status){
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                //alert(XMLHttpRequest.responseText);
            }
        });
    }

    function initDropdownMenuEvent() {
        $('.dropdown-menu li').on('click', function() {
            var kind = $(this).parent().attr("kind");
            if (kind=="val") {
                var num = $(this).parent().parent().attr("num");
                if ($(this).attr("val") == "mainFormOpt") {
                    $("#val" + num).prop("outerHTML", "<select name='val' id='val" + num + "'>" + $('#mainFormOptDiv').html() + "</select>");
                } else {
                    $("#val" + num).prop("outerHTML", "<input type='text' name='val' id='val" + num + "' readonly val='" + $(this).attr("val") + "' value='" + $(this).children(0).text() + "'/>");
                }
            }
            else {
                var num = $(this).parent().parent().attr("num");
                if ($(this).attr("val") == "mainFormOpt") {
                    $("#columnName" + num).prop("outerHTML", "<select name='columnName' id='columnName" + num + "'>" + $('#mainFormOptDiv').html() + "</select>");
                } else {
                    $("#columnName" + num).prop("outerHTML", "<input type='text' name='columnName' id='columnName" + num + "' readonly val='" + $(this).attr("val") + "' value='" + $(this).children(0).text() + "'/>");
                }
            }
        });
    }

    $(function() {
        addCond();
    })

    function makeRules() {
        var str = "";
        var isValid = true;
        $("tr[id^='rules']").each(function () {
            var trId = $(this).attr("id");
            var id = trId.substring("rules".length);
            var desc = $('#desc' + id).val();
            if (desc=="") {
                jAlert("规则不能为空", "提示");
                isValid = false;
                return false;
            }
            desc = desc.replaceAll("<", "&lt;");
            desc = desc.replaceAll(">", "&gt;");
            
            var ifs = "";
            $("#" + trId + " select[name='fieldName']").each(function (k) {
                if ($(this).val().trim() == "") {
                    jAlert("请选择如果项", "提示");
                    isValid = false;
                    return false;
                }

                var num = $(this).attr("num");
                var val = "";
                if ($("#columnName"+num).attr("val")) {
                    val = $("#columnName" + num).attr("val");
                }
                else {
                    val = $("#columnName" + num).val();
                }
                var operator = $("#compare" + num).val();

                operator = operator.replaceAll("<", "&lt;");
                operator = operator.replaceAll(">", "&gt;");

                if (ifs == "")
                    ifs += "{\"field\":\"" + $(this).val() + "\", \"operator\":\"" + operator + "\", \"val\":\"" + val + "\"}";
                else
                    ifs += ", {\"field\":\"" + $(this).val() + "\", \"operator\":\"" + operator + "\", \"val\":\"" + val + "\"}";
            });
            ifs = "[" + ifs + "]";
            
            
            var thens = "";
            $("#" + trId + " select[name='field']").each(function (k) {
                if ($(this).val().trim() == "") {
                    jAlert("请选择结果项", "提示");
                    isValid = false;
                    return false;
                }
                
                var num = $(this).attr("num");
                var val = "";
                if ($("#val"+num).attr("val")) {
                    val = $("#val" + num).attr("val");
                }
                else {
                    val = $("#val" + num).val();
                }
                var operator = $("#operator" + num).val();
                
                // console.log($(this).val() + " num=" + num)

                operator = operator.replaceAll("<", "&lt;");
                operator = operator.replaceAll(">", "&gt;");

                if (thens == "")
                    thens += "{\"field\":\"" + $(this).val() + "\", \"operator\":\"" + operator + "\", \"val\":\"" + val + "\"}";
                else
                    thens += ", {\"field\":\"" + $(this).val() + "\", \"operator\":\"" + operator + "\", \"val\":\"" + val + "\"}";
            });
            thens = "[" + thens + "]";

            str += "<rule>";
            str += "<desc>" + desc + "</desc>";
            str += "<if>" + ifs + "</if>";
            str += "<then>" + thens + "</then>";
            str += "</rule>";
        });

        if (!isValid)
            return;
        
        // console.log(str);

        var xmlStr = "<config></config>";
        var xml = $.parseXML(xmlStr);
        $xml = $(xml);

        var $elem = $($.parseXML("<rules>" + str + "</rules>"));
        var newNode = null;
        if (typeof document.importNode == 'function') {
            newNode = document.importNode($elem.find('rules').get(0), true);
        } else {
            newNode = $elem.find('rules').get(0);
        }
        $xml.find("config").get(0).appendChild(newNode);
        $.ajax({
            type: "post",
            url: "form_check_setup.jsp",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                op: "save",
                code: "<%=code%>",
                xml: $xml.xml()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                jAlert(data.msg, "提示");
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

    function ModifyRules() {
        makeRules();
    }
</script>
</HTML>