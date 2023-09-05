<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%
    String formCode = ParamUtil.get(request, "formCode");
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>宏控件</title>
    <%@ include file="../../../../inc/nocache.jsp" %>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">
    <meta name="generator" content="www.leipi.org"/>
    <link rel="stylesheet" href="bootstrap/css/bootstrap.css">
    <!--[if lte IE 6]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-ie6.css">
    <![endif]-->
    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/ie.css">
    <![endif]-->
    <link rel="stylesheet" href="leipi.style.css">
    <script type="text/javascript" src="../dialogs/internal.js"></script>
    <script src="../../../../inc/map.js"></script>
    <script src="../../../../js/jquery-1.9.1.min.js"></script>
    <script src="../../../../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../../../../js/select2/select2.css" rel="stylesheet"/>
    <script src="../../../../js/select2/select2.js"></script>
    <script type="text/javascript">
        function createElement(type, name) {
            var element = null;
            try {
                element = document.createElement('<' + type + ' name="' + name + '">');
            } catch (e) {
            }
            if (element == null) {
                element = document.createElement(type);
                element.name = name;
            }
            return element;
        }

        var map = new Map();
        <%
        MacroCtlMgr mm = new MacroCtlMgr();
        Vector v = mm.getAllMacroUnit();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            MacroCtlUnit mu = (MacroCtlUnit)ir.next();
        %>
        map.put('<%=mu.getCode()%>', <%=mu.getVersion()%>);
        <%
    }
    %>
    </script>
</head>
<body>
<div class="content">
    <table class="table table-bordered table-striped table-hover">
        <tr>
            <th>控件字段&nbsp;<span class="label label-important">*</span></th>
            <th><span>控件名称</span><span class="label label-important">*</span></th>
        </tr>
        <tr>
            <td>
                <input id="orgname" type="text" placeholder="必填项"/>
            </td>
            <td><input type="text" id="orgtitle" placeholder="必填项"></td>
        </tr>
        <tr>
            <th>类型</th>
            <th>必填项</th>
        </tr>
        <tr>
            <td>
                <select name="macroType" id="macroType" onChange="onMacroTypeChange(this)">
                    <%
                        ir = v.iterator();
                        while (ir.hasNext()) {
                            MacroCtlUnit mu = (MacroCtlUnit) ir.next();
                            if (!mu.isDisplay()) {
                                continue;
                            }
                            out.print("<option value=\"" + mu.getCode() + "\">" + mu.getName() + "</option>");
                        }%>
                </select>
            </td>
            <td><input id="canNull" name="canNull" type="checkbox" value="0"/></td>
        </tr>
        <tr>
            <th>只读</th>
            <th>只读类型</th>
        </tr>
        <tr>
            <td>
                <input id="isReadOnly" name="isReadOnly" type="checkbox" value="1"/>
            </td>
            <td>
                <select id="readOnlyType">
                    <option value="">不限</option>
                    <option value="0">仅添加时</option>
                    <option value="1">仅编辑时</option>
                    <option value="2">仅编辑引用记录时</option>
                </select>
                <br/>仅编辑引用记录时适用于”嵌套表格2“宏控件选取的记录
            </td>
        </tr>
        <tr>
            <th><span id="spanOrgtype">字段类型</span></th>
            <th>默认值</th>
        </tr>
        <tr>
            <td>
                <select name="orgtype" id="orgtype" class="span7">
                    <option value="0">字符串型</option>
                    <option value="1">文本型</option>
                    <option value="2">整型</option>
                    <option value="3">长整型</option>
                    <option value="4">布尔型</option>
                    <option value="5">浮点型</option>
                    <option value="6">双精度型</option>
                    <option value="7">日期型</option>
                    <option value="8">日期时间型</option>
                    <option value="9">价格型</option>
                    <option value="10">长文本型</option>
                </select>
            </td>
            <td>
                <textarea type="text" id="orgvalue" placeholder="无则不填" style="width:260px; height:100px;"></textarea>
                <br/>
                默认值以宏控件内置默认的为准，如：基础数据的默认值，如果为空，则使用此处的默认值
            </td>
        </tr>
        <tr>
            <th>
                控件样式宽
                <span style="display:none">
                &nbsp;&nbsp;
                字体大小 <input id="orgfontsize" type="text" value="" class="input-small span1" placeholder="auto"/> px
                </span></th>
            <th><span>长度/大小</span></th>
        </tr>
        <tr>
            <td>
                <input id="orgwidth" type="text" value="150" class="input-small span1" placeholder="auto"/>
                <select id="unit" style="width:60px">
                    <option value="px" selected>px</option>
                    <option value="%">%</option>
                </select>
            </td>
            <td>
                <select id="minT" name="minT" style="width:60px">
                    <option value="d=">>=</option>
                    <option value="d">></option>
                    <option value="=">=</option>
                </select>
                <input id="minV" name="minV" type="text" style="width:40px">
                <select id="maxT" name="maxT" style="width:60px">
                    <option value="x="><=</option>
                    <option value="x"><</option>
                </select>
                <input id="maxV" name="maxV" type="text" value="100" style="width:40px">
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <a id="edit" title="编辑" class="btn btn-primary" onclick="editMap()">编辑</a>
            </td>
        </tr>
        <tr id="desc">
            <td colspan="2">
                <table class="table table-hover table-condensed" id="options_table">
                    <tr>
                        <th>描述</th>
                    </tr>
                    <tr>
                        <td>
                            <textarea type="text" id="description" placeholder="无则不填" style="width:100%; height:100px;"></textarea>
                            <span style="display:none">
                                <label class="checkbox"> 可见性
                                <input id="orghide" type="checkbox"> 隐藏 </label>
                            </span>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>
<script type="text/javascript">
    var oNode = null, thePlugins = 'macros';
    window.onload = function () {
        if (UE.plugins[thePlugins].editdom) {
            oNode = UE.plugins[thePlugins].editdom;
            var gName = oNode.getAttribute('name').replace(/&quot;/g, "\"");
            var gTitle = oNode.getAttribute('title').replace(/&quot;/g, "\"");

            var gHidden = oNode.getAttribute('orghide'), gFontSize = oNode.getAttribute('orgfontsize'), gWidth = oNode.getAttribute('orgwidth');
            var gUnit = 'px';
            if (gWidth.endsWith('%')) {
                gUnit = '%';
                gWidth = gWidth.substring(0, gWidth.length - 1);
            } else if (gWidth.endsWith('px')) {
                gWidth = gWidth.substring(0, gWidth.length - 2);
            }

            var gType = oNode.getAttribute('fieldType');
            var gCanNull = oNode.getAttribute("canNull");

            gTitle = gTitle == null ? '' : gTitle;
            $G('orgname').value = gName;
            $G('orgtitle').value = gTitle;

            //if( oNode.tagName == 'INPUT' ) {}
            if (oNode.getAttribute('orghide') == '1') {
                $G('orghide').checked = true;
            }
            $G('orgtype').value = gType;
            $G('orgwidth').value = gWidth;
            $G('unit').value = gUnit;
            $G('orgfontsize').value = gFontSize;

            $G('orgname').setAttribute("readonly", true);

            var gReadOnlyType = oNode.getAttribute("readOnlyType");
            if (gReadOnlyType == null) {
                gReadOnlyType = '';
            }
            $G('readOnlyType').value = gReadOnlyType;

            $G('macroType').value = oNode.getAttribute("macroType");
            $G('macroType').disabled = true;
            if (oNode.getAttribute("macroType") == "macro_current_user"
                || oNode.getAttribute("macroType") == "macro_user_select_win"
                || oNode.getAttribute("macroType") == "macro_image"
                || oNode.getAttribute("macroType") == "nest_table"
                || oNode.getAttribute("macroType") == "nest_sheet"
                || oNode.getAttribute("macroType") == "macro_detaillist_ctl"
                || oNode.getAttribute("macroType") == "module_field_select"
                || oNode.getAttribute("macroType") == "macro_ntko_ctl"
                || oNode.getAttribute("macroType") == "macro_writepad_ctl"
                || oNode.getAttribute("macroType") == "macro_icon_ctl"
                || oNode.getAttribute("macroType") == "macro_formula_ctl"
                || oNode.getAttribute("macroType") == "macro_flow_select"
                || oNode.getAttribute("macroType") == "macro_basic_tree_select_ctl"
            ) {
                $G('desc').style.display = "none";
                $G("edit").style.display = '';
            } else {
                $G("edit").style.display = 'none';
            }

            var isReadOnly = oNode.getAttribute("readonly");
            if (isReadOnly != null) {
                $G('isReadOnly').checked = true;
            } else {
                $G('isReadOnly').checked = false;
            }

            if ($G('macroType').value == 'macro_sql') {
                $G('orgvalue').value = decodeURI(oNode.getAttribute("macroDefaultValue"));
            } else {
                $G('orgvalue').value = oNode.getAttribute("macroDefaultValue");
            }
            if (oNode.getAttribute("description"))
                $G('description').value = oNode.getAttribute("description");

            if (gCanNull == 0) {
                $G('canNull').checked = true;
            } else {
                $G('canNull').checked = false;
            }

            $G('orgtype').disabled = true;
            if ($G('macroType').value == "macro_flow_select" || $G('macroType').value == "macro_basic_tree_select_ctl") {
                $G("orgtype").style.display = '';
                $G("spanOrgtype").style.display = '';
            }
            getFieldType('<%=formCode%>', gName);

            var gMinT = oNode.getAttribute("minT");
            var gMinV = oNode.getAttribute("minV");
            var gMaxT = oNode.getAttribute("maxT");
            var gMaxV = oNode.getAttribute("maxV");
            if (gMinT) {
                $G('minT').value = gMinT;
                $G('minV').value = gMinV;
                $G('maxT').value = gMaxT;
                $G('maxV').value = gMaxV;
            }
        } else {
            $G("edit").style.display = 'none';
        }

        $('#macroType').select2();
    };

    dialog.oncancel = function () {
        if (UE.plugins[thePlugins].editdom) {
            delete UE.plugins[thePlugins].editdom;
        }
    };

    dialog.onok = function () {
        var gName = $G('orgname').value.replace(/\"/g, "&quot;");
        if (gName == '') {
            alert('请输入控件字段');
            return false;
        }
        var gTitle = $G('orgtitle').value.replace(/\"/g, "&quot;");
        if (gTitle == '') {
            alert('请输入控件名称');
            return false;
        }

        var gType = $G('orgtype').value;
        var gCanNull = $G('canNull').checked ? 0 : 1;

        var gFontSize = $G('orgfontsize').value, gWidth = $G('orgwidth').value;
        var gUnit = $G('unit').value;

        var gMinT = $G('minT').value;
        var gMinV = $G('minV').value;
        var gMaxT = $G('maxT').value;
        var gMaxV = $G('maxV').value;

        // 字符串型字段的字数最大为21845，相当于：最大长度65532字节/3
        // 此处将边界值设为20000，是考虑到还有其它字段，如果一个表中的字段总长度超长会报错，表一行最多支持65535字符(不包括text等)
        if (gType == 0) {
            if (gMinV >= 20000) {
                alert('最小长度不能超过20000');
                return false;
            }
            if (gMinV!='' && gMaxV!='') {
                if (parseInt(gMinV) > parseInt(gMaxV)) {
                    alert('最小长度不能大于最大长度');
                    return false;
                }
            }

            if (gMaxV == '') {
                alert('请输入最大长度');
                return false;
            }
            else if (gMaxV > 20000) {
                alert('最大长度不能超过20000');
                return false;
            }
        }

        var gReadOnlyType = $G('readOnlyType').value;

        if (!oNode) {
            try {
                /*
                if ( $G('orgtype').value.indexOf('sys_list')>0 ) {
                    oNode = document.createElement("select");
                    var objOption = new Option('{macros}', '');
                    oNode.options[oNode.options.length] = objOption;
                } else {
                    //input
                }*/
                oNode = createElement('input', gName);
                oNode.setAttribute('title', gTitle);
                var mactoTypeText = $("#macroType  option:selected").text();
                oNode.setAttribute("value", "宏控件：" + mactoTypeText);
                oNode.setAttribute("canNull", gCanNull);
                if ($('#macroType').val() == 'macro_sql') {
                    oNode.setAttribute("macroDefaultValue", encodeURI(orgvalue.value));
                } else {
                    oNode.setAttribute("macroDefaultValue", orgvalue.value);
                }
                oNode.setAttribute("description", description.value);
                oNode.setAttribute("macroType", macroType.value);
                oNode.setAttribute('kind', 'macro');
                oNode.setAttribute('cwsPlugins', thePlugins);
                oNode.setAttribute('fieldType', gType);

                if ($G('orghide').checked) {
                    oNode.setAttribute('orghide', '1');
                } else {
                    oNode.setAttribute('orghide', '0');
                }
                if (gFontSize != '') {
                    oNode.style.fontSize = gFontSize + 'px';
                    oNode.setAttribute('orgfontsize', gFontSize);
                }

                if (gWidth != '') {
                    oNode.style.width = gWidth + gUnit;
                    oNode.setAttribute('orgwidth', gWidth + gUnit);
                } else {
                    oNode.style.width = '';
                    oNode.setAttribute('orgwidth', '');
                }

                if ($G('isReadOnly').checked) {
                    oNode.setAttribute("readonly", "readonly");
                } else {
                    oNode.removeAttribute("readonly");
                }

                oNode.setAttribute("minT", gMinT);
                oNode.setAttribute("minV", gMinV);
                oNode.setAttribute("maxT", gMaxT);
                oNode.setAttribute("maxV", gMaxV);
                oNode.setAttribute("readOnlyType", gReadOnlyType);

                editor.execCommand('insertHtml', oNode.outerHTML);
                return true;
            } catch (e) {
                try {
                    editor.execCommand('error');
                } catch (e) {
                    alert('控件异常！');
                }
                return false;
            }
        } else {
            oNode.setAttribute('title', gTitle);
            oNode.setAttribute("fieldType", gType);
            oNode.setAttribute("value", "宏控件：" + $("#macroType  option:selected").text());
            oNode.setAttribute("macroType", macroType.value);
            if (macroType.value == 'macro_sql') {
                oNode.setAttribute("macroDefaultValue", encodeURI(orgvalue.value));
            } else {
                oNode.setAttribute("macroDefaultValue", orgvalue.value);
            }
            oNode.setAttribute("canNull", gCanNull);
            oNode.setAttribute('kind', 'macro');
            oNode.setAttribute('description', $G('description').value);
            if (gWidth != '') {
                oNode.style.width = gWidth + gUnit;
                oNode.setAttribute('orgwidth', gWidth + gUnit);
            } else {
                oNode.style.width = '';
                oNode.setAttribute('orgwidth', '');
            }
            if ($G('isReadOnly').checked) {
                oNode.setAttribute("readonly", "readonly");
            } else {
                oNode.removeAttribute("readonly");
            }

            oNode.setAttribute("minT", gMinT);
            oNode.setAttribute("minV", gMinV);
            oNode.setAttribute("maxT", gMaxT);
            oNode.setAttribute("maxV", gMaxV);
            oNode.setAttribute("readOnlyType", gReadOnlyType);

            delete UE.plugins[thePlugins].editdom;
            return true;
        }
    };

    function setSequence(id, name) {
        if (orgtitle.value == "")
            orgtitle.value = name;

        if (map.get(macroType.value).value > 1) {
            description.value = id;
        } else {
            orgvalue.value = id;
        }
        //下拉框change后，清空选择内容
        if (id == "") {
            description.value = id;
        }

        var mType = $G("macroType").value;
        if (mType == "module_field_select" || mType == "macro_image") {
            // 置默认值为空，清空老版的默认值，如macro_image的默认值为200,200，否则在生成表单字段时会赋予默认值
            orgvalue.value = '';
        }
    }

    function openWin(url, width, height) {
        window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
    }

    function onMacroTypeChange(obj) {
        if (obj.options[obj.selectedIndex].value == 'macro_flow_number' || obj.options[obj.selectedIndex].value == 'macro_flow_sequence') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/flow_sequence_sel.jsp', 300, 40);
        } else if (obj.options[obj.selectedIndex].value == 'macro_flow_select' || obj.options[obj.selectedIndex].value == 'macro_basic_tree_select_ctl') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            setSequence("", "");
            $G("desc").style.display = '';
            $G("orgtype").style.display = '';
            $G("spanOrgtype").style.display = '';
            openWin('../../../../flow/basic_select_sel.jsp?macroType=' + $G("macroType").value, 640, 280);
        } else if (obj.options[obj.selectedIndex].value == 'nest_table') {
            $G('canNull').checked = false;
            $G("canNull").disabled = true;
            $G("edit").style.display = 'none';
            $G("desc").style.display = 'none';
            setSequence("", "");
            // openWin('../../../../visual/module_sel.jsp', 300, 40);
            openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_table&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
        } else if (obj.options[obj.selectedIndex].value == 'nest_form') {
            $G('canNull').checked = false;
            $G("canNull").disabled = true;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../visual/module_sel.jsp', 300, 40);
        } else if (obj.options[obj.selectedIndex].value == 'nest_sheet') {
            $G('canNull').checked = false;
            $G("canNull").disabled = true;
            $G("edit").style.display = '';
            $G("desc").style.display = 'none';
            setSequence("", "");
            openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
        } else if (obj.options[obj.selectedIndex].value == 'macro_detaillist_ctl') {
            $G('canNull').checked = false;
            $G("canNull").disabled = true;
            $G("edit").style.display = '';
            $G("desc").style.display = 'none';
            setSequence("", "");
            openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
        } else if (obj.options[obj.selectedIndex].value == 'module_field_select') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../visual/module_field_sel.jsp?openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&fieldName=' + $G('orgname').value, 800, 600);
        } else if (obj.options[obj.selectedIndex].value == 'macro_image') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('image_ctl_prop.jsp', 450, 250);
        } else if (obj.options[obj.selectedIndex].value == 'role_user_select') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/role_sel.jsp', 300, 200);
        } else if (obj.options[obj.selectedIndex].value == 'macro_form_data_map') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/form_data_map.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
        } else if (obj.options[obj.selectedIndex].value == 'macro_queryfield_select') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/macro/macro_query_field_sel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 300);
        } else if (obj.options[obj.selectedIndex].value == 'macro_formula_ctl') {
            $G('canNull').checked = false;
            $G("canNull").disabled = true;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../visual/formula_sel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 300);
        } else if (obj.options[obj.selectedIndex].value == 'macro_current_user') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/macro/current_user_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
        }  else if (obj.options[obj.selectedIndex].value == 'macro_user_select_win') {
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/macro/user_select_win_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
        } else if (obj.options[obj.selectedIndex].value == 'macro_ntko_ctl') {
            $G("canNull").disabled = false;
            $G("edit").style.display = '';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/macro/macro_ntko_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
        } else if (obj.options[obj.selectedIndex].value == 'macro_writepad_ctl') {
            $G("canNull").disabled = false;
            $G("edit").style.display = '';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/macro/macro_writepad_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
        } else if (obj.options[obj.selectedIndex].value == 'macro_icon_ctl') {
            $G("canNull").disabled = false;
            $G("edit").style.display = '';
            $G("desc").style.display = '';
            setSequence("", "");
            openWin('../../../../flow/macro/icon_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 450);
        }
        else if (obj.options[obj.selectedIndex].value == 'macro_opinion') {
            $('#maxV').val('2000');
        }
        else if (obj.options[obj.selectedIndex].value == 'macro_ueditor') {
            $G("canNull").disabled = false;
            $G('orgtype').value = '1';
            $('#maxV').val('');
        }
        else if (obj.options[obj.selectedIndex].value == 'macro_year_ctl') {
            $G('orgtype').value = '2';
            $('#maxV').val('');
        }
        else if (obj.options[obj.selectedIndex].value == 'macro_month_ctl') {
            $G('orgtype').value = '2';
            $('#maxV').val('');
        }
        else {
            $('#maxV').val('100');
            $G("canNull").disabled = false;
            $G("edit").style.display = 'none';
            $G("desc").style.display = '';
            setSequence("", "");
            // $G("orgtype").style.display = 'none';
            // $G("spanOrgtype").style.display = 'none';
        }
    }

    function editMap() {
        if ($G("macroType").value == "macro_image") {
            openWin('image_ctl_prop.jsp', 450, 250);
            return;
        } else if ($G("macroType").value == "macro_current_user") {
            openWin('../../../../flow/macro/current_user_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
            return;
        } else if ($G("macroType").value == "macro_user_select_win") {
            openWin('../../../../flow/macro/user_select_win_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
        }
        else if ($G("macroType").value == "macro_ntko_ctl") {
            openWin('../../../../flow/macro/macro_ntko_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
            return;
        } else if ($G("macroType").value == "macro_writepad_ctl") {
            openWin('../../../../flow/macro/macro_writepad_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
            return;
        } else if ($G("macroType").value == "macro_icon_ctl") {
            openWin('../../../../flow/macro/icon_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 450);
            return;
        } else if ($G("macroType").value == "macro_flow_select" || $G("macroType").value == "macro_basic_tree_select_ctl") {
            openWin('../../../../flow/basic_select_sel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>&macroType=' + $G("macroType").value, 800, 450);
            return;
        }

        var jsonStr = "";
        if (map.get(macroType.value).value > 1) {
            jsonStr = description.value;
            if (jsonStr == "") {
                // 向下兼容，因为ModuleFieldSelectCtl改为version为2之前设计的控件，配置信息仍保存在macrodefaultvalue中
                jsonStr = orgvalue.value;
            }
        } else {
            jsonStr = orgvalue.value;
        }
        if (jsonStr != "") {
            jsonStr = decodeJSON(jsonStr);
            if ($G("macroType").value == "macro_formula_ctl") {
                openPostWindow('../../../../visual/formula_sel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 'macro_formula_ctl', '<%=StrUtil.UrlEncode(formCode)%>', jsonStr, "winFormula");
                return;
            }

            //含有queryId 为修改查询，若sourceForm不为空，则为表单，否则为嵌套表选择
            if (jsonStr.indexOf("queryId") != -1) {
                openPostWindow('../../../../visual/module_field_sel_query_nest.jsp', 'nest_sheet', '<%=StrUtil.UrlEncode(formCode)%>', jsonStr, "嵌套表域选择");
                //openWin('../../../../visual/module_field_sel_query_nest.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&params=' + jsonStr, 800, 600);
            } else if (jsonStr.indexOf("idField") != -1 && jsonStr.indexOf("showField") != -1) {
                openPostWindow('../../../../visual/module_field_sel.jsp?fieldName=' + $G('orgname').value, 'nest_table', '<%=StrUtil.UrlEncode(formCode)%>', jsonStr, "模块表单域选择");
                //openWin('../../../../visual/module_field_sel.jsp&params=' + jsonStr, 900, 700);
            } else if (jsonStr.indexOf("sourceForm") != -1) {
                // console.log("jsonString=" + jsonStr);
                var jsonStrs = eval('(' + jsonStr + ')');
                var nestType = "nest_sheet";
                if ($('#macroType').val() == "nest_table") {
                    nestType = "nest_table";
                }
                if (jsonStrs.sourceForm != null && jsonStrs.sourceForm != "") {
                    openPostWindow('../../../../visual/module_field_sel_nest.jsp?nestType=' + nestType, 'nest_sheet', '<%=StrUtil.UrlEncode(formCode)%>', jsonStr, "winNestSheet");
                    //openWin('../../../../visual/module_field_sel_nest.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&params=' + jsonStr, 800, 600);
                } else {
                    var isTab = 0;
                    if (jsonStrs.isTab) {
                        isTab = 1;
                    }
                    openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=' + nestType + '&isTab=' + isTab + '&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&editFlag=true&params=' + jsonStrs.destForm + "&oldRelateCode=" + jsonStrs.destForm + "&jsonStr=" + encodeURI(jsonStr), 800, 600);
                }
            }
        } else {
            if ($G("macroType").value == "module_field_select") {
                openWin('../../../../visual/module_field_sel.jsp?openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&fieldName=' + $G('orgname').value, 800, 600);
            } else {
                openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_table&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
            }
        }
    }

    // 对字符串中的引号进行解码
    function decodeJSON(jsonString) {
        jsonString = jsonString.replace(/%dq/gi, '"');
        jsonString = jsonString.replace(/%sq/gi, "'");

        // 不能解码回车换行，否则会导致过滤条件为脚本型时，如果有回车换行，会导致点击编辑按钮时，eval报错
        // jsonString = jsonString.replace(/%rn/g, "\r\n")
        // jsonString = jsonString.replace(/%n/g, "\n")
        return jsonString;
    }

    function openPostWindow(url, nestType, openerFormCode, data, name) {
        var tempForm = document.createElement("form");
        tempForm.id = "tempForm1";
        tempForm.method = "post";
        tempForm.action = url;
        tempForm.target = name;

        var paramHideInput = document.createElement("input");
        paramHideInput.type = "hidden";
        paramHideInput.name = "params";
        paramHideInput.value = data;
        tempForm.appendChild(paramHideInput);

        var nestTypeHideInput = document.createElement("input");
        nestTypeHideInput.type = "hidden";
        nestTypeHideInput.name = "nestType";
        nestTypeHideInput.value = nestType;
        tempForm.appendChild(nestTypeHideInput);

        var openerFormCodeHideInput = document.createElement("input");
        openerFormCodeHideInput.type = "hidden";
        openerFormCodeHideInput.name = "openerFormCode";
        openerFormCodeHideInput.value = openerFormCode;
        tempForm.appendChild(openerFormCodeHideInput);

        var editFlagHideInput = document.createElement("input");
        editFlagHideInput.type = "hidden";
        editFlagHideInput.name = "editFlag";
        editFlagHideInput.value = "edit";
        tempForm.appendChild(editFlagHideInput);

        document.body.appendChild(tempForm);
        $(tempForm).submit(function() {
            openNewWindow(name);
        });
        $(tempForm).submit();
        document.body.removeChild(tempForm);
    }

    function openNewWindow(name) {
        window.open('about:blank', name, "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=900,height=700");
    }

    function getFieldType(formCode, fieldName) {
        $.ajax({
            type: "post",
            url: "../../../../form/getFieldType.do",
            data: {
                formCode: formCode,
                fieldName: fieldName
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                // $('#container').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    var oldOrgType = $G('orgtype').value;
                    $G('orgtype').value = data.fieldType;

                    // 如果ajax获取到的不是FIELD_TYPE_VARCHAR，而原来的值为FIELD_TYPE_VARCHAR，则清空minV、maxV
                    if (data.fieldType != <%=FormField.FIELD_TYPE_VARCHAR%> && oldOrgType == <%=FormField.FIELD_TYPE_VARCHAR%>) {
                        $G('minV').value = '';
                        $G('maxV').value = '';
                    }
                } else {
                    // 增加字段，尚未提交，重新再编辑时，也会获取不到
                    console.error(data.msg);
                }
            },
            complete: function (XMLHttpRequest, status) {
                // $('#container').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    $(function() {
        $('#orgtype').change(function() {
            if ($(this).val() === '2' || $(this).val() === '3' || $(this).val() === '4' || $(this).val() === '5'
                || $(this).val() === '6' || $(this).val() === '9') {
                $('#maxV').val('');
            }
            else if ($(this).val() === '0') {
                // 如果是意见输入框，则最大长度置为2000
                if ($('#macroType').val() === 'macro_opinion') {
                    $('#maxV').val('2000');
                }
                else {
                    $('#maxV').val('100');
                }
            }
            else if ($(this).val() === '1') {
                $('#maxV').val('65536');
            }
        });
    });
</script>
</body>
</html>
