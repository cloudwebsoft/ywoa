<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>计算控件</title>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">
    <link rel="stylesheet" href="bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="calculator.css">
    <!-- <link rel="stylesheet" type="text/css" href="../../../../jCalculator/css/style.css" media="screen">    
    <link rel="stylesheet" type="text/css" href="../../../../jCalculator/css/jcalculator.css" media="screen"> -->
    <!--[if lte IE 6]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-ie6.css">
    <![endif]-->
    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/ie.css">
    <![endif]-->
    <link rel="stylesheet" href="leipi.style.css">

    <script type="text/javascript" src="../dialogs/internal.js"></script>

    <script src="../../../../inc/common.js"></script>
    <script src="../../../../js/jquery-1.9.1.min.js"></script>
    <script src="../../../../js/jquery-migrate-1.2.1.min.js"></script>

    <script type="text/javascript" src="../../../../js/activebar2.js"></script>
    <script type="text/javascript" src="../../../../js/formpost.js"></script>

    <link href="../../../../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../../../../js/jquery-showLoading/jquery.showLoading.js"></script>

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

        function getContextPath() {
            var pathName = document.location.pathname;
            var index = pathName.substr(1).indexOf("/");
            var result = pathName.substr(0, index + 1);
            return result;
        }
    </script>
</head>

<body>
<div class="content" style="overflow:auto">
    <table class="table table-bordered table-striped table-hover">
        <tr>
            <th><span>控件字段&nbsp;<span class="label label-important">*</span></span></th>
            <th><span>控件名称&nbsp;</span><span class="label label-important">*</span></th>
        </tr>
        <tr>
            <th><input type="text" id="orgname" placeholder="必填项"></th>
            <th><input type="text" id="orgtitle" placeholder="必填项"></th>
        </tr>
        <tr>
            <th><span>算式&nbsp;<span class="label label-important">*</span></span></th>
            <th><span>数据类型/嵌套表编码</span></th>
        </tr>
        <tr>
            <td>
                <input type="text" id="orgFormula" placeholder="必填项" readonly>
                <input type="hidden" name="fhType" id="fhType" value=""/>
            </td>
            <td>
                <span id="spanFieldTypeDesc">双精度型</span>
                &nbsp;
                <span id="formCodeShow"></span>
                <input id="formCode" name="formCode" type="hidden"/>
            </td>
        </tr>
        <tr>
            <th><span>四舍五入</span></th>
            <th><span>小数点后位数</span></th>
        </tr>
        <tr>
            <td>
                <input id="isRoundTo5" name="isRoundTo5" checked type="checkbox" value="0"/>
            </td>
            <td>
                <input type="text" id="orgDigit" value="2">(需与四舍五入联用）
            </td>
        </tr>
        <tr>
            <th><span>格式</span></th>
            <th><span>无穷大 / NaN 时显示值</span></th>
        </tr>
        <tr>
            <td>
                <select id="format">
                    <option value=""></option>
                    <option value="0">千分位</option>
                </select>
                <br/>
                （需与价格型联用）
            </td>
            <td>
				<input id="valForInfinity" name="valForInfinity" type="text" style="width:50px" title="无穷大时显示"/>&nbsp;/&nbsp;
				<input id="valForNaN" name="valForNaN" type="text" style="width:50px" title="NaN时显示（即0/0时）"/>
            </td>
        </tr>
        <tr>
            <th><span>必填项</span></th>
            <th><span></span></th>
        </tr>
        <tr>
            <td>
                <input id="canNull" name="canNull" type="checkbox" value="0" checked/>
            </td>
            <td>
            </td>
        </tr>
        <tr>
            <th><span>只读</span></th>
            <th><span>只读类型</span></th>
        </tr>
        <tr>
            <td>
                <input id="isReadOnlyShow" name="isReadOnlyShow" disabled checked type="checkbox" value="1"/>
                <input style="display:none" id="isReadOnly" name="isReadOnly" checked type="checkbox" value="1"/>
            </td>
            <td>
                <select id="readOnlyType" disabled>
                    <option value="">不限</option>
                    <option value="0">仅添加时</option>
                    <option value="1">仅编辑时</option>
                </select>
                <br/>仅编辑引用记录时适用于”嵌套表格2“宏控件选取的记录
            </td>
        </tr>
        <tr>
            <th><span>提示</span></th>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>
                <textarea id="tip" name="tip" style="width:250px; height:100px"></textarea>
            </td>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <th><span>&nbsp;&nbsp;&nbsp;&nbsp;长&nbsp;&nbsp;X&nbsp;&nbsp;宽</span></th>
            <th></th>
        </tr>
        <tr>
            <td>
                <input id="orgwidth" type="text" value="150" class="input-small span1" placeholder="auto"/>
                X
                <input id="orgheight" type="text" value="" class="input-small span1" placeholder="auto"/>
                <span style="display:none">
                &
                <input id="orgfontsize" type="text" value="" class="input-small span1" placeholder="auto"/> px
				</span>
            </td>
            <td>
            </td>
        </tr>
        <tr style="display:none">
            <th><span>可见性</span></th>
            <th><span>长度/大小</span></th>
        </tr>
        <tr style="display:none">
            <td><label class="checkbox inline"><input id="orghide" type="checkbox"/> 隐藏 </label>
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
                <input id="maxV" name="maxV" type="text" style="width:40px">
                <select id="orgalign" style="display:none">
                    <option value="left">左对齐</option>
                    <option value="center">居中对齐</option>
                    <option value="right">右对齐</option>
                </select>
            </td>
        </tr>
    </table>
</div>
<div class="jcalculator_wrap">
    <div id="showColumn" class="jcalculator" style="display:none;">
        <div id="jcalculator">
            <span id="C">C</span>
            <!-- <span id="sum">sum</span>-->
            <span id="(">(</span>
            <span id=")">)</span>
            <span id="+">+</span>
            <span id="-">-</span>
            <span id="*">*</span>
            <span id=",">,</span>
            <span id="subDate">subDate</span>
            <span id="addDate">addDate</span>
            <span id="/">/</span>
            <span id="9">9</span>
            <span id="8">8</span>
            <span id="7">7</span>
            <span id="6">6</span>
            <span id="5">5</span>
            <span id="4">4</span>
            <span id="3">3</span>
            <span id="2">2</span>
            <span id="1">1</span>
            <span id="0">0</span>
            <span id=".">.</span>
        </div>
        <div class="jcalculator_1" style="overflow-y:auto;overflow-x:hidden;">
            <div id="jcalculator_1">

            </div>
        </div>
    </div>
</div>
<%
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    boolean isServerConnectWithCloud = cfg.getBooleanProperty("isServerConnectWithCloud");
    if (!isServerConnectWithCloud) {
%>
<iframe id="ifrm" src="../../../../util/form_parser.jsp" style="width:100%; margin-top: 150px" frameborder="0"></iframe>
<%
    }
%>
</body>
<script type="text/javascript">
    var oNode = null, thePlugins = 'calculation';
    window.onload = function () {
        if (UE.plugins[thePlugins].editdom) {
            oNode = UE.plugins[thePlugins].editdom;

            var gValue = oNode.getAttribute('value');
            var gFormula = oNode.getAttribute('formula');
            if (gValue)
                gValue = gValue.replace(/&quot;/g, "\"");
            if (gFormula)
                gFormula = gFormula.replace(/&quot;/g, "\"");
            var gName = oNode.getAttribute('name').replace(/&quot;/g, "\"");
            var gTitle = oNode.getAttribute('title').replace(/&quot;/g, "\"");
            var gHidden = oNode.getAttribute('orghide');
            var gFontSize = oNode.getAttribute('orgfontsize');
            var gAlign = oNode.getAttribute('orgalign');
            var gWidth = oNode.getAttribute('orgwidth');
            var gHeight = oNode.getAttribute('orgheight');

            var gCanNull = oNode.getAttribute("canNull");
            var gMinT = oNode.getAttribute("minT");
            var gMinV = oNode.getAttribute("minV");
            var gMaxT = oNode.getAttribute("maxT");
            var gMaxV = oNode.getAttribute("maxV");
            var gDigit = oNode.getAttribute("digit");
            var gRoundTo5 = oNode.getAttribute("isRoundTo5");
            var gFormat = oNode.getAttribute("format");
            var gValForInfinity = oNode.getAttribute("valForInfinity");
            var gValForNaN = oNode.getAttribute("valForNaN");

            /*var isReadOnly = oNode.getAttribute("readonly");
            if (isReadOnly != null) {
                $G('isReadOnly').checked = true;
            } else {
                $G('isReadOnly').checked = false;
            }*/

            var gTip = oNode.getAttribute("tip");
            if (gTip == null) {
                gTip = '';
            }
            var gReadOnlyType = oNode.getAttribute("readOnlyType");
            if (gReadOnlyType == null) {
                gReadOnlyType = '';
            }

            var gFormCode = oNode.getAttribute("formCode");
            if (gFormCode == null) {
                gFormCode = '';
            }

            gValue = gValue == null ? '' : gValue;
            gTitle = gTitle == null ? '' : gTitle;
            gFormula = gFormula == null ? '' : gFormula;
            $G('orgname').value = gName;
            $G('orgtitle').value = gTitle;
            $G('orgFormula').value = gFormula;
            if (gHidden == '1') {
                $G('orghide').checked = true;
            }
            $G('orgname').setAttribute("readonly", true);
            $G('orgfontsize').value = gFontSize;
            $G('orgwidth').value = gWidth;
            $G('orgheight').value = gHeight;
            $G('orgalign').value = gAlign;
            $G('orgDigit').value = gDigit;
            if (gRoundTo5 == 1) {
                $G('isRoundTo5').checked = true;
            } else {
                $G('isRoundTo5').checked = false;
            }
            if (gCanNull == 0) {
                $G('canNull').checked = true;
            } else {
                $G('canNull').checked = false;
            }
            $G('tip').value = gTip;
            $G('formCode').value = gFormCode;
            $G('formCodeShow').innerHTML = gFormCode;
            $G('readOnlyType').value = gReadOnlyType;
            $G('format').value = gFormat;
		gValForInfinity = gValForInfinity == null ? '' : gValForInfinity;
		$G('valForInfinity').value = gValForInfinity;
		gValForNaN = gValForNaN == null ? '' : gValForNaN;
		$G('valForNaN').value = gValForNaN;
        }

        $("#orgFormula").click(function () {
            $("#showColumn").css("display", "block");
        });

        $(document).bind("click", function (e) {
            e = e || window.event;
            var dom = e.srcElement || e.target;
            if (dom.id != "orgFormula") {
                if (dom.parentNode.id != "jcalculator_1" && dom.id != 'jcalculator_1' && dom.id != "showColumn" && dom.parentNode.id != "jcalculator" && dom.parentNode.id != "showColumn" && document.getElementById("showColumn").style.display == "block") {
                    document.getElementById("showColumn").style.display = "none";
                }
            }
        });

        var orgFormulaVal = $("#orgFormula").val();
        var fh = $("#fhType").val();
        $("span").live('click', function () {
            var code = $(this).attr("id"); // 当前输入的字符
            var gFormCode = $(this).attr('formCode');

            orgFormulaVal = $("#orgFormula").val();
            if (orgFormulaVal.indexOf("addDate") == 0) {
                $('#spanFieldTypeDesc').html("日期型");
            } else {
                $('#spanFieldTypeDesc').html("双精度型");
            }
            fh = $("#fhType").val(); // 上一次输入的字符

            if (isNaN(code)) {//如果不是数字的时候
                if (code == "sum") {
                    if (fh != undefined && fh != "") {
                        if (fh != "+" && fh != "-" && fh != "*" && fh != "/") {
                            return;
                        }
                    }
                }
                if (orgFormulaVal != "") {
                    if (fh != undefined && fh != "") {//至少点击输入一次
                        if (code == fh) { //如果两次输的一样的时候，返回
                            return;
                        }
                        if (!isNaN(fh)) {
                            if (!isNaN(code)) {
                                $("#orgFormula").val(orgFormulaVal + code);
                            } else {
                                if (code != "+" && code != "-" && code != "*" && code != "/" && code != "C" && code != "." && code != ")") {
                                    return;
                                }
                            }
                        } else {
                            if (fh != "+" && fh != "-" && fh != "*" && fh != "/" && fh != "C" && fh != "(" && fh != ")" && fh != "," && fh != ".") {
                                if (code != "+" && code != "-" && code != "*" && code != "/" && code != "C" && code != "(" && code != ")" && code != ",") {
                                    return;
                                }
                            }

                            if (fh == "+" || fh == "-" || fh == "*" || fh == "/") {//如果两次都是输入符号
                                if (code == "+" || code == "-" || code == "*" || code == "/") {
                                    return;
                                }
                            }
                        }
                    } else {
                        if (code == "C") {
                            $("#fhType").val("");
                            $("#orgFormula").val("");
                            $("#formCode").val("");
                            $("#formCodeShow").html("");
                            return;
                        }

                        if (code != "+" && code != "-" && code != "*" && code != "/") {
                            return;
                        }
                    }
                } else {
                    if (fh != undefined && fh != "") {//至少点击输入一次
                        if (code == fh) { //如果两次输的一样的时候，返回
                            return;
                        }

                        if (fh == "+" || fh == "-" || fh == "*" || fh == "/") {//如果两次都是输入符号
                            if (code == "+" || code == "-" || code == "*" || code == "/") {
                                return;
                            }
                        }

                        if (fh != "+" && fh != "-" && fh != "*" && fh != "/" && fh != "C" && fh != "(" && fh != ")" && fh != "," && fh != ".") {
                            if (code != "+" && code != "-" && code != "*" && code != "/" && code != "C" && code != "(" && code != ")" && code != ",") {
                                return;
                            }
                        }
                    } else {
                        if (code == "+" || code == "-" || code == "*" || code == "/") {
                            return;
                        }
                    }
                }

                if (code == "C") {
                    $("#fhType").val("");
                    $("#orgFormula").val("");
                    $("#formCode").val("");
                    $("#formCodeShow").html("");
                    return;
                }
            }

            if (!isNaN(code)) {//是数字的时候
                if (code == "sum") {
                    if (fh != undefined && fh != "") {
                        if (fh != "+" && fh != "-" && fh != "*" && fh != "/" && fh != ".") {
                            return;
                        }
                    }
                }

                if (orgFormulaVal != "") {
                    if (fh != undefined && fh != "") {//至少点击输入一次
                        if (!isNaN(fh)) {
                            if (!isNaN(code)) {
                                $("#orgFormula").val(orgFormulaVal + code);
                            } else {
                                if (code != "+" && code != "-" && code != "*" && code != "/" && code != "C") {
                                    return;
                                }
                            }
                        } else {
                            if (fh != "+" && fh != "-" && fh != "*" && fh != "/" && fh != "C" && fh != "(" && fh != ")" && fh != "," && fh != ".") {
                                if (code != "+" && code != "-" && code != "*" && code != "/" && code != "C" && code != "(" && code != ")" && code != ",") {
                                    return;
                                }
                            }
                        }
                    } else {
                        if (code != "+" && code != "-" && code != "*" && code != "/") {
                            return;
                        }
                    }
                }
            }

            if (fh == "(") {
                //if(code.indexOf("nest.") == -1){
                //	return;
                //}

                if (code == "+" || code == "-" || code == "*" || code == "/") {
                    return;
                }
            }

            if (fh == ")") {
                if (code != "+" && code != "-" && code != "*" && code != "/") {
                    return;
                }
            }
            if (fh == "sum") {
                if (code != "+" && code != "-" && code != "*" && code != "/" && code != ".") {
                    return;
                }
                // 嵌套表格中字段的累加，已改为直接点击“嵌套表格（字段名）”
                /*if (code != "(") {
                    return;
                }*/
            }

            if (code != "C") {
                $("#fhType").val(code);
            } else {
                $("#orgFormula").val("");
                $("#formCode").val("");
                $("#formCodeShow").html("");
                $("#fhType").val("");
            }

            if (gFormCode == null || gFormCode == '') {
                gFormCode = '';
                $G('formCodeShow').innerHTML = '';
            } else {
                $G('formCode').value = gFormCode;
                $G('formCodeShow').innerHTML = gFormCode;
            }
            // console.log('orgFormulaVal=' + orgFormulaVal + ' code=' + code);
            $("#orgFormula").val(orgFormulaVal + code);
        });
        //$('#orgFormula').calculator();
    }
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
        var gFormula = $G('orgFormula').value.replace(/\"/g, "&quot;");
        if (gFormula == '') {
            alert('请输入控件算式');
            return false;
        }

        var lowerFormula = gFormula.toLowerCase();
        if (lowerFormula.indexOf(",") != -1) {
            if (lowerFormula.indexOf("subdate") == -1 && lowerFormula.indexOf("adddate") == -1) {
                alert('时间计算公式输入错误!');
                return false;
            }
        }

        if (lowerFormula.indexOf("subdate") != -1) {
            if (lowerFormula.indexOf(",") == -1) {
                alert('时间计算公式输入出错!');
                return false;
            }
        }

        if (lowerFormula.indexOf("adddate") != -1) {
            if (lowerFormula.indexOf(",") == -1) {
                alert('时间计算公式输入出错!');
                return false;
            }
        }

        var gFontSize = $G('orgfontsize').value;
        var gAlign = $G('orgalign').value;
        var gWidth = $G('orgwidth').value;
        var gHeight = $G('orgheight').value;

        var gCanNull = $G('canNull').checked ? 0 : 1;
        var gMinT = $G('minT').value;
        var gMinV = $G('minV').value;
        var gMaxT = $G('maxT').value;
        var gMaxV = $G('maxV').value;
        var gDigit = $G('orgDigit').value;
        var gFormCode = $G('formCode').value;
        var gRoundTo5 = $G('isRoundTo5').checked ? 1 : 0;
        var gTip = $G('tip').value;
        var gReadOnlyType = $G('readOnlyType').value;
        var gFormat = $G('format').value;
        var gValForInfinity = $G('valForInfinity').value;
        var gValForNaN = $G('valForNaN').value;

        if (!oNode) {
            try {
                debugger;
                oNode = createElement('input', gName);
                oNode.setAttribute('title', gTitle);
                oNode.setAttribute('value', gFormula);
                oNode.setAttribute('cwsPlugins', thePlugins);
                if ($G('orghide').checked) {
                    oNode.setAttribute('orghide', 1);
                } else {
                    oNode.setAttribute('orghide', 0);
                }
                if (gFontSize != '') {
                    //style += 'font-size:' + gFontSize + 'px;';
                    oNode.setAttribute('orgfontsize', gFontSize);
                }
                if (gAlign != '') {
                    //style += 'text-align:' + gAlign + ';';
                    oNode.setAttribute('orgalign', gAlign);
                }
                if (gWidth != '') {
                    oNode.style.width = gWidth + 'px';
                    //style += 'width:' + gWidth + 'px;';
                    oNode.setAttribute('orgwidth', gWidth);
                }
                if (gHeight != '') {
                    oNode.style.height = gHeight + 'px';
                    //style += 'height:' + gHeight + 'px;';
                    oNode.setAttribute('orgheight', gHeight);
                }

                oNode.setAttribute("canNull", gCanNull);
                oNode.setAttribute("minT", gMinT);
                oNode.setAttribute("minV", gMinV);
                oNode.setAttribute("maxT", gMaxT);
                oNode.setAttribute("maxV", gMaxV);
                oNode.setAttribute("formula", gFormula);
                oNode.setAttribute("kind", "CALCULATOR");
                if (gFormula.indexOf("addDate") == 0) {
                    oNode.setAttribute("fieldtype", "<%=FormField.FIELD_TYPE_DATE%>");
                } else {
                    oNode.setAttribute("fieldtype", "<%=FormField.FIELD_TYPE_DOUBLE%>");
                }
                oNode.setAttribute("digit", gDigit);
                oNode.setAttribute("isRoundTo5", gRoundTo5);

                if ($G('isReadOnly').checked) {
                    oNode.setAttribute("readonly", "readonly");
                } else {
                    oNode.removeAttribute("readonly");
                }
                oNode.setAttribute("tip", gTip);
                oNode.setAttribute("readOnlyType", gReadOnlyType);

                oNode.setAttribute("formCode", gFormCode);
                // if (gFormat != '') {
                    oNode.setAttribute('format', gFormat);
                // }

			oNode.setAttribute('valForInfinity', gValForInfinity);
			oNode.setAttribute('valForNaN', gValForNaN);
                editor.execCommand('insertHtml', oNode.outerHTML);
            } catch (e) {
                try {
                    editor.execCommand('error');
                } catch (e) {
                    alert('控件异常！');
                }

                return false;
            }
        } else {
            oNode.setAttribute('name', gName);
            oNode.setAttribute('title', gTitle);
            //oNode.setAttribute('value', $G('orgvalue').value);
            if ($G('orghide').checked) {
                oNode.setAttribute('orghide', 1);
            } else {
                oNode.setAttribute('orghide', 0);
            }
            if (gFontSize != '') {
                //oNode.style.fontSize = gFontSize+ 'px';
                oNode.setAttribute('orgfontsize', gFontSize);
            } else {
                //oNode.style.fontSize = '';
                oNode.setAttribute('orgfontsize', '');
            }
            if (gAlign != '') {
                oNode.setAttribute('orgalign', gAlign);
            } else {
                oNode.setAttribute('orgalign', '');
            }
            if (gWidth != '') {
                oNode.style.width = gWidth + 'px';
                oNode.setAttribute('orgwidth', gWidth);
            } else {
                oNode.style.width = '';
                oNode.setAttribute('orgwidth', '');
            }
            if (gHeight != '') {
                oNode.style.height = gHeight + 'px';
                oNode.setAttribute('orgheight', gHeight);
            } else {
                //oNode.style.height = '';
                oNode.setAttribute('orgheight', '');
            }

            oNode.setAttribute("canNull", gCanNull);
            oNode.setAttribute("value", gFormula);
            oNode.setAttribute("formula", gFormula);
            oNode.setAttribute("digit", gDigit);
            oNode.setAttribute("isRoundTo5", gRoundTo5);
            oNode.setAttribute("tip", gTip);
            oNode.setAttribute("readOnlyType", gReadOnlyType);
            oNode.setAttribute("formCode", gFormCode);

            if ($G('isReadOnly').checked) {
                oNode.setAttribute("readonly", "readonly");
            } else {
                oNode.removeAttribute("readonly");
            }

            // if (gFormat != '') {
                oNode.setAttribute('format', gFormat);
            // }

		oNode.setAttribute('valForInfinity', gValForInfinity);
		oNode.setAttribute('valForNaN', gValForNaN);
            delete UE.plugins[thePlugins].editdom;
        }
    };

    function getFields() {
        var re = true;
        <%
        if (isServerConnectWithCloud) {
        %>
        $.ajax({
            async: false,
            type: "post",
            url: "<%=request.getContextPath()%>/form/parseForm.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                content: UE.getEditor("myFormDesign").getContent()
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    // $('#fieldsAry').val(JSON.stringify(data.fields));
                    $('#jcalculator_1').html(getCols(data.fields));
                } else {
                    jAlert(data.msg, "提示");
                    re = false;
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
        <%
        }
        else {
            %>
        var sint = window.setInterval(function () {
            if (typeof (o("ifrm").contentWindow.parseFields) == "function") {
                window.clearInterval(sint);
                // 通过iframe中的页面获取fields
                var fields = o("ifrm").contentWindow.parseFields(UE.getEditor("myFormDesign").getContent());
                // consoleLog(JSON.stringify(jsonAry));
                $('#jcalculator_1').html(getCols(fields));
            }
        }, 200);
        <%
    }
    %>
        return re;
    }

    function getCols(jsonAry) {
        var str = "";
        for (var i in jsonAry) {
            var title = jsonAry[i].title;
            var fieldType = jsonAry[i].fieldType;
            var macroType = jsonAry[i].macroType;
            var defaultValue = jsonAry[i].defaultValue;
            var description = jsonAry[i].description;
            var type = jsonAry[i].type;
            // consoleLog(title + " type=" + type + " macroType=" + macroType);
            if (fieldType == <%=FormField.FIELD_TYPE_INT%> || fieldType == <%=FormField.FIELD_TYPE_LONG%> || fieldType == <%=FormField.FIELD_TYPE_FLOAT%> || fieldType == <%=FormField.FIELD_TYPE_DOUBLE%> || fieldType == <%=FormField.FIELD_TYPE_PRICE%> || type == "<%=FormField.TYPE_DATE%>" || type == "<%=FormField.TYPE_DATE_TIME%>") {
                str += '<span id="' + jsonAry[i].name + '">' + jsonAry[i].title + '</span><br/>';
            } else if (macroType == "macro_sql") {
                str += '<span id="' + jsonAry[i].name + '">' + jsonAry[i].title + '</span><br/>';
            } else if (type == "macro" && (macroType == "<%=MacroCtlUnit.NEST_TABLE%>" || macroType == "<%=MacroCtlUnit.NEST_SHEET%>" || macroType == "<%=MacroCtlUnit.NEST_DETAIL%>")) { //  || macroType=="<%=MacroCtlUnit.NEST_TYPE_NORMAIL%>" || macroType=="<%=MacroCtlUnit.NEST_DETAIL_LIST%>" || macroType=="<%=MacroCtlUnit.NEST_TYPE_TABLE%>")) {
                $.ajax({
                    async: false,
                    type: "post",
                    url: "<%=request.getContextPath()%>/form/getNestFieldsForCal.do",
                    contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        macroType: macroType,
                        defaultValue: defaultValue,
                        description: description
                    },
                    dataType: "json",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        // data = $.parseJSON(data);
                        if (data.ret == "1") {
                            var ary = data.jsonAry;
                            // console.log(title);
                            // console.log(JSON.stringify(data.jsonAry));

                            for (var i in ary) {
                                var fieldType = ary[i].fieldType;
                                var macroType = ary[i].macroType;
                                var defaultValue = ary[i].defaultValue;
                                var description = ary[i].description;
                                var formCode = ary[i].formCode;
                                var formName = ary[i].formName;
                                var type = ary[i].type;
                                if (fieldType == <%=FormField.FIELD_TYPE_INT%> || fieldType == <%=FormField.FIELD_TYPE_LONG%> || fieldType == <%=FormField.FIELD_TYPE_FLOAT%> || fieldType == <%=FormField.FIELD_TYPE_DOUBLE%> || fieldType == <%=FormField.FIELD_TYPE_PRICE%> || type == "<%=FormField.TYPE_DATE%>" || type == "<%=FormField.TYPE_DATE_TIME%>") {
                                    str += '<span id="sum(nest.' + ary[i].name + ')" formCode="' + formCode + '" formName="' + formName + '">' + ary[i].title + '(' + formName + ')</span><br/>';
                                } else if (macroType == "macro_sql" || macroType == "macro_formula_ctl") {
                                    str += '<span id="sum(nest.' + ary[i].name + ')" formCode="' + formCode + '" formName="' + formName + '">' + ary[i].title + '(' + formName + ')</span><br/>';
                                }
                            }
                        } else {
                            // consoleLog(data.msg);
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
        }
        return str;
    }

    $(function () {
        getFields();
    })
</script>
</html>