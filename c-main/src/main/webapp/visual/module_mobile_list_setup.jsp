<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.redmoon.oa.util.CSSUtil" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.cloudweb.oa.cond.CondUtil" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.cloudweb.oa.api.ICondUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%
    String moduleCode = ParamUtil.get(request, "moduleCode");
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(moduleCode);
    if (msd == null) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
        return;
    }
    FormDb fd = new FormDb();
    fd = fd.getFormDb(msd.getString("form_code"));

    String[] fields = msd.getColAry(false, "list_field");
    if (fields == null || fields.length==0) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "显示列未配置！"));
        return;
    }

    ICondUtil condUtil = SpringUtil.getBean(ICondUtil.class);
    StringBuilder opts = new StringBuilder();
    String[] fieldsTitle = msd.getColAry(false, "list_field_title");
    int len = fields.length;
    for (int i=0; i<len; i++) {
        String fieldName = fields[i];
        String fieldTitle = fieldsTitle[i];

        Object[] aryTitle = condUtil.getFieldTitle(fd, fieldName, fieldTitle);
        opts.append("<option value='" + fieldName + "'>" + aryTitle[0] + "</option>");
    }
    /*for (FormField ff : fd.getFields()) {
        opts.append("<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>");
    }*/

    String iconOpts = "";
    ArrayList<String[]> fontAry = CSSUtil.getFontBefore();
    int fontAryLen = fontAry.size();
    for (int m = 0; m < fontAryLen; m++) {
        String[] ary = fontAry.get(m);
        iconOpts += "<option value='" + ary[0] + "'>";
        iconOpts += "<i class='fa " + ary[0] + "'></i>";
        iconOpts += ary[0];
        iconOpts += "</option>";
    }

    String pageMobileList = StrUtil.getNullStr(msd.getString("page_mobile_list"));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>模块列表设置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link rel='stylesheet' href='../js/bootstrap/css/bootstrap4.1.3.min.css'>
    <link rel="stylesheet" href="../js/flexwidget/style.css">
    <link rel="stylesheet" href="../js/flexwidget/css/font-awesome.css">
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <link rel="stylesheet" href="../js/color-picker/colorpicker.css">
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
    <style>
        .fields-box input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
            width: 120px;
            margin-left: 5px;
            line-height: 20px; /*否则输入框的文字会偏下*/
        }

        .picker-label {
            height: 32px;
            margin-right: 10px;
            float: left;
        }
        .picker-box {
            width: 32px;
            height: 32px;
            float: left;
        }
        .fields-box-row {
            margin-bottom: 10px;
            height: 32px;
        }
        .picker {
            width: 30px;
            height: 30px;
            cursor: pointer;
        }
        .fg-widget-inner {
            text-align: center;
            padding-top: 20px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/color-picker/colorpicker.js"></script>
</head>
<body>
    <%@ include file="module_setup_inc_menu_top.jsp" %>
    <script>
        o("menu13").className = "current";
    </script>
    <div class="col-sm-12 form-group" style="margin-top: 40px; clear: both">
    </div>
    <div class="col-sm-12">
        <div class="row">
            <div class="grid-settings">
                <div class="form-check display-inline view-gridlines-div" style="display: none">
                    <label class="view-gridlines-label">显示网格线?</label>
                    <div class="toggle">
                        <input type="checkbox" id="toggle1" class="togglegridlines" checked>
                        <label for="toggle1"></label>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-12">
                <div id="zone_div">
                    <div class="flexgrid-container" style="width: 500px;">
                        <div class="flexgrid-helper">
                            <%--<div class="add-row"></div>
                            <div class="remove-row"></div>--%>
                            <button class="btn btn-sm clear-flexgrid">清除</button>
                            <button class="btn btn-sm fg-add-widget">添加</button>
                            <button class="btn btn-sm save-flexgrid"><i class="fa fa-save"></i></button>
                        </div>
                        <div class="flexgrid-grid"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <form id="form1" name="form1" enctype="multipart/form-data">
        <div id="fieldsBox" class="fields-box" style="padding: 20px; width: 420px; height: 500px; position: absolute; background-color: #eee;">
            <div class="fields-box-row">
                坐标&nbsp;X<input id="x" name="x" readonly/>&nbsp;Y<input id="y" name="y" readonly/>
            </div>
            <div class="fields-box-row">
                类型
                <select id="type" name="type">
                    <option value="field">字段</option>
                    <option value="img">图片</option>
                    <option value="text">文本</option>
                    <option value="comb">组合</option>
                    <option value="line">线条</option>
                    <option value="blank">空白</option>
                </select>
                <span id="fieldsWrapper">
                <select id="field" name="field">
                <%=opts.toString()%>
                </select>
            </span>
                <span id="imgUrlWrapper" style="display: none">
                <input id="imgUrl" name="imgUrl"/>
            </span>
            </div>
            <div class="fields-box-row">
                <div class="picker-label">字体颜色</div>
                <div class="picker-box" title="点击这里获取颜色">
                    <div class="picker" id="pickerFont">
                    </div>
                </div>
                <a href="javascript:;" onclick="clearColor('pickerFont')">清除</a>
            </div>
            <div class="fields-box-row">
                <div class="picker-label">背景颜色</div>
                <div class="picker-box" title="点击这里获取颜色">
                    <div class="picker" id="pickerBg">
                    </div>
                </div>
                <a href="javascript:;" onclick="clearColor('pickerBg')">清除</a>
            </div>
            <div class="fields-box-row">
                <div class="picker-label">字体图标</div>
                <div class="picker-box">
                    <select id="fontIcon" name="fontIcon" style="width:150px" class="js-example-templating js-states">
                        <option>无</option>
                        <%=iconOpts%>
                    </select>
                </div>
            </div>
            <div class="fields-box-row">
                对齐
                <select id="align" name="align">
                    <option value="left" selected>居左</option>
                    <option value="center">居中</option>
                    <option value="right">居右</option>
                </select>
                padding
                <input id="paddingTop" name="paddingTop" style="width: 30px"/>&nbsp;上
                <input id="paddingRight" name="paddingRight" style="width: 30px"/>&nbsp;右
                <input id="paddingBottom" name="paddingBottom" style="width: 30px"/>&nbsp;下
                <input id="paddingLeft" name="paddingLeft" style="width: 30px"/>&nbsp;左
            </div>
            <div class="fields-box-row">
                标题<input id="label" name="label" title="标题中可加入%s，用以替换为字段的值，如果含有%s，则不再输出字段值；如果为组合式，则可通过{$field}引入字段值或者也可以使用表达式" style="width:300px"/>
            </div>
            <div class="fields-box-row">
                字体大小<input id="fontSize" name="fontSize"/>
                &nbsp;<input id="isBold" name="isBold" type="checkbox"/>是否加粗
            </div>
            <div class="fields-box-row">
                背景图片
                <input id="bgImgUrl" name="bgImgUrl"/>
                <a href="javascript:;" onclick="$('#bgImgUrl').val('')">清空</a>
                <a href="javascript:;" onclick="if ($('#bgImgUrl').val()=='') {layer.msg('请输入背景图片地址');return;}  showImg($('#bgImgUrl').val())">预览</a>
            </div>
            <div class="fields-box-row">
                上传图片
                <input id="bgImg" name="bgImg" type="file" style="width: 180px"/>
            </div>
            <div class="fields-box-row text-center" style="text-align: center">
                <button id="pickerBtnOk" class="btn btn-default">确定</button>
                <button id="pickerBtnCancel" class="btn btn-default">取消</button>
            </div>
        </div>
    </form>
    <!-- partial -->
    <script src='../js/flexwidget/jquery.min.js'></script>
    <script src="../js/layui/layui.js" charset="utf-8"></script>
    <script src="../js/select2/select2.js"></script>
    <%--会使得无法拉宽--%>
    <%--<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>--%>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src='../js/flexwidget/jquery-ui.js'></script>
    <script src='../js/flexwidget/jquery.ui.touch-punch.min.js'></script>
    <script>
        var layer;
        layui.use('layer', function() {
            layer = layui.layer;
        });
    </script>
    <script src="../js/flexwidget/script.js"></script>
	<script>
	var zone = $('.flexgrid-container');
	var zoneInner = zone.find('.flexgrid-grid');
	zoneInner.setFlexGrid({
		cols: 6, 
		rows: 6, 
		defaultHeight: 1,
		defaultWidth: 1,
		minWidth: 1, 
		minHeight: 1,
	});
	// console.log(zoneInner.getOption());
	zoneInner.buildGrid();
	
	$(document).on('click', '.add-row', function() { 
		zoneInner.addRow();
	});
	$(document).on('click', '.remove-row', function() {
		zoneInner.removeRow();
	});
	$(document).on('click', '.fg-add-widget', function() {
		// widget.find('.fg-widget-inner').css('background', options.background != null ? options.background[Math.floor(Math.random() * options.background.length)] : '');
		var widget = $('<div class="fg-widget"><i class="fa fa-chevron-right fg-resize-widget"></i><i class="fa fa-times fg-remove-widget" title="remove this widget"></i><i class="fa fa-arrows-alt move-widget fg-widget-handle"></i><div class="fg-widget-inner" style="background: #406fff !important;"></div></div>');
		zoneInner.addWidget({widget:widget, maxHeight:6, maxWidth:6});
	});
	$(document).on('click', '.fg-remove-widget', function() {
		var widget = $(this).closest('.fg-widget');
		zoneInner.removeWidget(widget);
	});
	$(document).on('click', '.togglegridlines', function() {
		zoneInner.toggleGridlines();
	});
	$(document).on('click', '.clear-flexgrid', function() {
		zoneInner.clearGrid();
	});

	function saveWidgets() {
        var grid = zoneInner.saveGrid();
        var widgets = grid[0]['widgets'];
        console.log(widgets);

        var formData = new FormData($('#form1')[0]);
        formData.append("moduleCode", "<%=moduleCode%>");
        formData.append("widgets", JSON.stringify(widgets));
        $.ajax({
            type: "post",
            url: "savePageMobileList.do",
            data: formData,
            // 下面三个参数要指定，如果不指定，会报一个JQuery的错误
            cache: false,
            processData: false,
            contentType: false,
            dataType: "json",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                if (data.isUploadFile) {
                    layer.open({
                        type: 1
                        , offset: 'auto'
                        , id: 'dlgSuccess' // 防止重复弹出
                        , content: '<div style="padding: 20px 100px;">' + data.msg + '</div>'
                        , btn: ['确定']
                        , btnAlign: 'c' //按钮居中
                        , shade: 0 //不显示遮罩
                        , yes: function (index, layero) {
                            window.location.reload();
                        }
                    });
                }
                else {
                    layer.msg(data.msg);
                    // layer.open({
                    //     type: 1
                    //     // , area: ['520px', '250px']
                    //     , offset: 'auto'
                    //     , id: 'dlgSuccess' //防止重复弹出
                    //     , content: '<div style="padding: 20px 100px;">' + data.msg + '</div>'
                    //     , btn: '确定'
                    //     , btnAlign: 'c' //按钮居中
                    //     , shade: 0 //不显示遮罩
                    //     , zIndex: '99'
                    // });
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function () {
                //请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

	$(document).on('click', '.save-flexgrid', function() {
        saveWidgets();
	});

    <%
	JSONArray arr = JSONArray.parseArray(pageMobileList);
	if (arr != null) {
        for (int i=0; i<arr.size(); i++) {
            JSONObject json = arr.getJSONObject(i);
            String dataX = json.getString("data_x");
            String dataY = json.getString("data_y");
            String dataWidth = json.getString("data_width");
            String innerHtml = json.getString("innerHtml");
            String blockId = "";
            if (json.containsKey("blockId")) {
                blockId = json.getString("blockId");
            }
            String align = "left";
            if (json.containsKey("align")) {
                align = json.getString("align");
            }
            String paddingLeft = "";
            if (json.containsKey("paddingLeft")) {
                paddingLeft = json.getString("paddingLeft");
            }
            String paddingRight = "";
            if (json.containsKey("paddingRight")) {
                paddingRight = json.getString("paddingRight");
            }
            String paddingTop = "";
            if (json.containsKey("paddingTop")) {
                paddingTop = json.getString("paddingTop");
            }
            String paddingBottom = "";
            if (json.containsKey("paddingBottom")) {
                paddingBottom = json.getString("paddingBottom");
            }
            %>
            var innerHtml = '<%=innerHtml%>';
            var widget = $('<div class="fg-widget custom-blue-widget"><i class="fa fa-chevron-right fg-resize-widget" aria-hidden="true"></i><i class="fa fa-times fg-remove-widget" title="remove this widget"></i><i class="fa fa-arrows-alt move-widget fg-widget-handle"></i><div class="fg-widget-inner" style="background: #8eb5fa !important;">'+innerHtml+'</div></div>');
            zoneInner.addWidget({
                widget: widget,
                x: <%=dataX%>,
                y: <%=dataY%>,
                width: <%=dataWidth%>,
                height:1,
                minWidth:1,
                minHeight: 1,
                maxWidth: 6,
                maxHeight: 1
            });

            widget.attr('blockId', '<%=blockId%>');
            widget.attr('type', '<%=json.getString("type")%>');
            widget.attr('isBold', '<%=json.getString("isBold")%>');
            widget.attr('fontSize', '<%=json.getString("fontSize")%>');
            widget.attr('bgImgUrl', '<%=json.getString("bgImgUrl")%>');
            widget.attr('fontIcon', '<%=json.getString("fontIcon")%>');
            widget.attr('bgColor', '<%=json.getString("bgColor")%>');
            widget.attr('fontColor', '<%=json.getString("fontColor")%>');
            widget.attr('imgUrl', '<%=json.getString("imgUrl")%>');
            widget.attr('fieldName', '<%=json.getString("fieldName")%>');
            widget.attr('label', '<%=json.getString("label")%>');
            widget.attr('fieldTitle', '<%=json.getString("innerHtml")%>');
            widget.attr('align', '<%=align%>');
            widget.attr('paddingLeft', '<%=paddingLeft%>');
            widget.attr('paddingRight', '<%=paddingRight%>');
            widget.attr('paddingTop', '<%=paddingTop%>');
            widget.attr('paddingBottom', '<%=paddingBottom%>');
            <%
            if (!StringUtils.isEmpty(json.getString("bgImgUrl"))) {
            %>
                // widget.css('background-image', 'url(<%=request.getContextPath()%>/<%=json.getString("bgImgUrl")%>)');
            <%
            }
            %>

            <%
        }
	}
	%>

	var pickF, pickB;

	function onTypeChange(typeObj) {
        if ($(typeObj).val() == 'line') {
            $('#fieldsWrapper').hide();
            $('#imgUrlWrapper').hide();
        }
        else if ($(typeObj).val() == 'img') {
            $('#imgUrlWrapper').show();
            $('#fieldsWrapper').hide();
        }
        else if ($(typeObj).val() == 'text' || $(typeObj).val() == 'comb') {
            $('#imgUrlWrapper').hide();
            $('#fieldsWrapper').hide();
        }
        else if ($(typeObj).val() == 'field') {
            $('#fieldsWrapper').show();
            $('#imgUrlWrapper').hide();
        }
        else if ($(typeObj).val() == 'blank') {
            $('#fieldsWrapper').hide();
            $('#imgUrlWrapper').hide();
        }
    }

	$(function() {
	    $('#field').select2();

        $("#fontIcon").select2({
            width: 200,
            templateResult: formatState,
            templateSelection: formatState
        });

        $('.fields-box').css({
            position:'absolute',
            left: ($(window).width() - $('.fields-box').outerWidth())/2,
            top: ($(window).height() - $('.fields-box').outerHeight())/2 + $(document).scrollTop()
        });

        pickF = Colorpicker.create({
            el: "pickerFont",
            color: "rgba(201, 201, 201, 1.0)", // 201表示未设颜色
            change: function (elem, rgba) {
                elem.style.backgroundColor = rgba;
            }
        })

        pickB = Colorpicker.create({
            el: "pickerBg",
            color: "rgba(201, 201, 201, 1.0)",
            change: function (elem, rgba) {
                elem.style.backgroundColor = rgba;
            }
        })

        $('.fields-box').hide();

        $('#pickerBtnOk').click(function(e) {
            e.preventDefault();

            $('.fields-box').hide();

            if ($curWidget.attr('blockId') == null || $curWidget.attr('blockId') == '') {
                $curWidget.attr('blockId', generateUUID());
            }
            $curWidget.attr('type', $('#type').val());
            $curWidget.attr('isBold', $('#isBold').prop('checked'));
            $curWidget.attr('fontSize', $('#fontSize').val());
            $curWidget.attr('align', $('#align').val());
            $curWidget.attr('bgImgUrl', $('#bgImgUrl').val());
            $curWidget.attr('fontIcon', $('#fontIcon').val());
            $curWidget.attr('bgColor', JSON.stringify(pickB.rgba)=='{}' ? '' :JSON.stringify(pickB.rgba));
            $curWidget.attr('fontColor', JSON.stringify(pickF.rgba)=='{}' ? '' :JSON.stringify(pickF.rgba));
            $curWidget.attr('imgUrl', $('#imgUrl').val());
            $curWidget.attr('fieldName', $('#field').val());
            $curWidget.attr('label', $('#label').val());
            $curWidget.attr('fieldTitle', $("#field option:checked").text());
            var type = $('#type').val();
            if (type == 'field') {
                $curWidget.find('.fg-widget-inner').html($("#field option:checked").text());
            }
            else if (type == 'img') {
                $curWidget.find('.fg-widget-inner').html('图片');
            }
            else if (type == 'text' || type=='comb') {
                $curWidget.find('.fg-widget-inner').html($('#label').val());
            }
            else if (type == 'line') {
                $curWidget.find('.fg-widget-inner').html('线条');
            }
            else {
                $curWidget.find('.fg-widget-inner').html('空白');
            }
            $curWidget.attr('paddingLeft', $('#paddingLeft').val());
            $curWidget.attr('paddingRight', $('#paddingRight').val());
            $curWidget.attr('paddingTop', $('#paddingTop').val());
            $curWidget.attr('paddingBottom', $('#paddingBottom').val());

            saveWidgets();
        });

        $('#pickerBtnCancel').click(function(e) {
            e.preventDefault();
            $('.fields-box').hide();
        });

        $('#type').change(function() {
            onTypeChange(this);
        })
    });

    function formatState(state) {
        if (!state.id) {
            return state.text;
        }
        var $state = $(
            '<span><i class="fa ' + state.id + '"></i>&nbsp;&nbsp;' + state.text + '</span>'
        );
        return $state;
    }

    var $curWidget;
	$('.flexgrid-grid').on('dblclick', '.fg-widget', function() {
	    $curWidget = $(this);
	    console.log(this);
        var x = $curWidget.attr('data-fg-x');
        var y = $curWidget.attr('data-fg-y');
        $('#x').val(x);
        $('#y').val(y);
        $('#type').val($curWidget.attr('type'));
        onTypeChange(o('type'));

        $('#isBold').prop('checked', $curWidget.attr('isBold')=='true');
        $('#fontSize').val($curWidget.attr('fontSize'));
        var align = $curWidget.attr('align');
        if (align == null) {
            align = "left";
        }
        $('#align').val(align);
        $('#bgImgUrl').val($curWidget.attr('bgImgUrl'));
        $("#fontIcon").select2("val", [$curWidget.attr('fontIcon')]);

        // pickB.rgba = $.parseJSON($curWidget.attr('bgColor'));
        // pickF.rgba = $.parseJSON($curWidget.attr('fontColor'));
        if ($curWidget.attr('bgColor') != null) {
            try {
                var rgba = $.parseJSON($curWidget.attr('bgColor'));
                $('#pickerBg').css('background-color', 'rgba(' + rgba.r + ',' + rgba.g + ',' + rgba.b + ',' + rgba.a + ')');
            }
            catch (e) {}
        }
        if ($curWidget.attr('fontColor') != null) {
            try {
                var rgba = $.parseJSON($curWidget.attr('fontColor'));
                $('#pickerFont').css('background-color', 'rgba(' + rgba.r + ',' + rgba.g + ',' + rgba.b + ',' + rgba.a + ')');
            }
            catch (e) {}
        }

        $('#imgUrl').val($curWidget.attr('imgUrl'));
        $('#label').val($curWidget.attr('label'));
        $("#field").select2("val", [$curWidget.attr('fieldName')]);
        // console.log("$curWidget.attr('paddingLeft')=" + $curWidget.attr('paddingLeft'));
        $('#paddingLeft').val($curWidget.attr('paddingLeft'));
        $('#paddingRight').val($curWidget.attr('paddingRight'));
        $('#paddingTop').val($curWidget.attr('paddingTop'));
        $('#paddingBottom').val($curWidget.attr('paddingBottom'));

        $('#bgImg').attr('name', 'bgImg_' + x + '_' + y);
        // 不采用layyi及jquery ui dialog，因为都会使得colorpicker无法正确定位
        $('.fields-box').show();
	});

	function clearColor(objId) {
	    $('#' + objId).css('background-color', 'rgba(201, 201, 201, 1.0)');
	    if (objId == 'pickerFont') {
            pickF.rgba = {'r':201, 'g':201, 'b':201, 'a':1.0};
        }
	    else {
            pickB.rgba = {'r':201, 'g':201, 'b':201, 'a':1.0};
        }
    }

    function generateUUID() {
        var d = new Date().getTime();
        if (window.performance && typeof window.performance.now === "function") {
            d += performance.now(); //use high-precision timer if available
        }
        // UUID正常格式：xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx，此处进行了简化
        var uuid = 'xxx-xxxx-4xxx-yxxx-xxx'.replace(/[xy]/g, function (c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
        return uuid;
    }

    function showImg(path) {
	    openWin("<%=request.getContextPath()%>/showImg.do?path=" + path, 800, 600);
    }
	</script>
</body>
</html>
