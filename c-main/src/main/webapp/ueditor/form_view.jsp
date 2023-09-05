<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");
    String formCode = ParamUtil.get(request, "formCode");

    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title>视图设计器</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <link href="css/bootstrap/css/bootstrap.css?2023" rel="stylesheet" type="text/css"/>
    <!--[if lte IE 6]>
    <link rel="stylesheet" type="text/css" href="css/bootstrap/css/bootstrap-ie6.css?2023">
    <![endif]-->
    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="css/bootstrap/css/ie.css?2023">
    <![endif]-->
    <link href="css/site.css?2023" rel="stylesheet" type="text/css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/form_designer/form_designer.css"/>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script type="text/javascript" charset="utf-8" src="js/ueditor/ueditor.config.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="js/ueditor/ueditor.all.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>
    <script type="text/javascript" charset="utf-8" src="js/ueditor/formdesign/formdesign.jsp?formCode=<%=formCode%>"></script>

    <!--style>
        .list-group-item{padding:0px;}
    </style-->

    <script>
        var op = "<%=op%>";

        function getContent() {
            return window.opener.getFormContent();
        }

        var uEditor;
        function window_onload() {
            if (!UE.browser.ie) {
                // jAlert("设计器只能在IE内核浏览器使用，窗口将关闭！","提示");
                // window.close();
                // return false;
            }
            if (op == "edit") {
                document.getElementById("myFormDesign").value = getContent();
            }
            uEditor = UE.getEditor('myFormDesign', {
                //allowDivTransToP: false,//阻止转换div 为p
                toolleipi: true,//是否显示，设计器的 toolbars
                textarea: 'design_content',
                enableAutoSave: false,
                //选择自己需要的工具按钮名称,此处仅选择如下五个
                //toolbars:[[
                //'fullscreen', 'source', '|', 'undo', 'redo', '|','bold', 'italic', 'underline', 'fontborder', 'strikethrough',  'removeformat', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist','|', 'fontfamily', 'fontsize', '|', 'indent', '|', 'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|',  'link', 'unlink',  '|',  'horizontal',  'spechars',  'wordimage', '|', 'inserttable', 'deletetable',  'mergecells',  'splittocells']],
                toolbars: [[
                    'fullscreen', 'source', '|', 'undo', 'redo', '|',
                    'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'selectall', 'cleardoc', '|',
                    'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                    'paragraph', 'fontfamily', 'fontsize', '|',
                    'directionalityltr', 'directionalityrtl', 'indent', '|',
                    'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                    'link', 'unlink', 'anchor', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                    'simpleupload', 'insertimage', 'insertvideo', 'emotion', 'map', 'insertframe', 'insertcode', 'pagebreak', 'template', '|',
                    'horizontal', 'date', /*'time'*/, 'spechars', '|',
                    'inserttable', 'deletetable', 'insertparagraphbeforetable', 'insertrow', 'deleterow', 'insertcol', 'deletecol', 'mergecells', 'mergeright', 'mergedown', 'splittocells', 'splittorows', 'splittocols', '|',
                    'print', 'preview', 'searchreplace', 'help'
                ]],
                //focus时自动清空初始化时的内容
                //autoClearinitialContent:true,
                //关闭字数统计
                wordCount: false,
                //关闭elementPath
                elementPathEnabled: false,
                //默认的编辑区域高度
                initialFrameHeight: 300,
                disabledTableInTable: false
                ///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
                //更多其他参数，请参考ueditor.config.js中的配置项
            });


            UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
            UE.Editor.prototype.getActionUrl = function (action) {
                if (action == 'uploadimage' || action == 'uploadscrawl') {
                    return '<%=request.getContextPath()%>/ueditor/UploadFile?op=formDesigner';
                } else if (action == 'uploadvideo') {
                    return '<%=request.getContextPath()%>/ueditor/UploadFile';
                } else {
                    return this._bkGetActionUrl.call(this, action);
                }
            }

            //设置左侧宽度
            document.getElementById("content").style.width = (document.body.clientWidth - 260) + "px";
            //隐藏priview template save leipi四个按钮(模板 预览 保存 表单设计器)
            try {
                getByClass("edui-for-button_template")[0].style.visibility = "hidden";
                getByClass("edui-for-button_template")[0].style.width = '0px';
                getByClass("edui-for-button_preview")[0].style.visibility = "hidden";
                getByClass("edui-for-button_preview")[0].style.width = '0px';
                getByClass("edui-for-button_save")[0].style.visibility = "hidden";
                getByClass("edui-for-button_save")[0].style.width = '0px';
                getByClass("edui-for-button_leipi")[0].style.visibility = "hidden";
                getByClass("edui-for-button_leipi")[0].style.width = '0px';

            } catch (e) {
                //alert(e);
            }
        }

        function getByClass(sClass) {
            var aResult = [];
            var aEle = document.getElementsByTagName('*');
            /*正则模式*/
            var re = new RegExp("\\b" + sClass + "\\b", "g");
            for (var i = 0; i < aEle.length; i++) {
                /*字符串search方法判断是否存在匹配*/
                if (aEle[i].className.search(re) != -1) {
                    aResult.push(aEle[i]);
                }
            }
            return aResult;
        };
    </script>

</head>
<body onload="window_onload()">

<!-- Docs page layout -->

<div class="container">
    <form method="post" id="saveform" name="saveform" action="">
        <input type="hidden" name="fields" id="fields" value="0">
        <div class="row">

            <div class="span10" id="content">
                <textarea id="myFormDesign" name="myFormDesign" style="width:100%;"></textarea>
            </div>

            <!-- fixed navbar -->
            <div class="span2">
                <div class="formdesigner-menu">
                    <div class="formdesigner-menutitle">视图设计器</div>
                    <div class="formdesigner-menubox">
                        <ul>
                            <%
                                Iterator ir = fd.getFields().iterator();
                                while (ir.hasNext()) {
                                    FormField ff = (FormField) ir.next();
                            %>
                            <li><img src="<%=SkinMgr.getSkinPath(request)%>/images/form_designer/formdesigner_1.png" width="18" height="18"/>
                                &nbsp;&nbsp;
                                <a href="javascript:;" onClick="createTxtCtl('<%=ff.getName()%>', '<%=ff.getTitle()%>')"><%=ff.getTitle()%></a>
                            </li>
                            <%
                                }
                            %>
                            <div class="formdesigner-menu-btn">
                                <div class="blue_btn_90" onclick="save()">确定</div>
                                <div class="grey_btn_90" onclick="window.close()">取消</div>
                            </div>
                        </ul>
                    </div>
                </div>
            </div>
        </div>

    </form>
</div>
<!--end container-->

<!-- script start-->
<script type="text/javascript">
    function save() {
        if (uEditor.queryCommandState('source'))
            uEditor.execCommand('source');//切换到编辑模式才提交，否则有bug

        if (uEditor.hasContents()) {
            uEditor.sync();/*同步内容*/
            var html = uEditor.getContent().replace(eval("/" + "\"selected\"" + "/gi"), "\"\"").replace("<p>&nbsp;</p>", "");//替换selected为"" 删除头部空行
            window.opener.setFormContent(html);
        } else {
            var html = uEditor.getContent();
            window.opener.setFormContent(html);
        }
        window.close();
    }

    var cwsFormDesigner = {
        /*执行控件*/
        exec: function (method) {
            uEditor.execCommand(method);
        },
        /*
            Javascript 解析表单
            template 表单设计器里的Html内容
            fields 字段总数
        */
        parse_form: function (template, fields) {
            //正则  radios|checkboxs|select 匹配的边界 |--|  因为当使用 {} 时js报错
            var preg = /(\|-<span(((?!<span).)*cwsplugins=\"(radios|checkboxs|select)\".*?)>(.*?)<\/span>-\||<(img|input|textarea|select).*?(<\/select>|<\/textarea>|\/>))/gi, preg_attr = /(\w+)=\"(.?|.+?)\"/gi, preg_group = /<input.*?\/>/gi;
            if (!fields) fields = 0;

            var template_parse = template, template_data = new Array(), add_fields = new Object(), checkboxs = 0;

            var pno = 0;
            template.replace(preg, function (plugin, p1, p2, p3, p4, p5, p6) {
                var parse_attr = new Array(), attr_arr_all = new Object(), name = '', select_dot = '', is_new = false;
                var p0 = plugin;
                var tag = p6 ? p6 : p4;
                //alert(tag + " \n- t1 - "+p1 +" \n-2- " +p2+" \n-3- " +p3+" \n-4- " +p4+" \n-5- " +p5+" \n-6- " +p6);

                if (tag == 'radios' || tag == 'checkboxs') {
                    plugin = p2;
                } else if (tag == 'select') {
                    plugin = plugin.replace('|-', '');
                    plugin = plugin.replace('-|', '');
                }
                plugin.replace(preg_attr, function (str0, attr, val) {
                    if (attr == 'name') {
                        if (val == 'leipiNewField') {
                            is_new = true;
                            fields++;
                            val = 'data_' + fields;
                        }
                        name = val;
                    }

                    if (tag == 'select' && attr == 'value') {
                        if (!attr_arr_all[attr]) attr_arr_all[attr] = '';
                        attr_arr_all[attr] += select_dot + val;
                        select_dot = ',';
                    } else {
                        attr_arr_all[attr] = val;
                    }
                    var oField = new Object();
                    oField[attr] = val;
                    parse_attr.push(oField);
                })
                /*alert(JSON.stringify(parse_attr));return;*/
                if (tag == 'checkboxs') /*复选组  多个字段 */
                {
                    plugin = p0;
                    plugin = plugin.replace('|-', '');
                    plugin = plugin.replace('-|', '');
                    var name = 'checkboxs_' + checkboxs;
                    attr_arr_all['parse_name'] = name;
                    attr_arr_all['name'] = '';
                    attr_arr_all['value'] = '';

                    attr_arr_all['content'] = '<span cwsplugins="checkboxs"  title="' + attr_arr_all['title'] + '">';
                    var dot_name = '', dot_value = '';
                    p5.replace(preg_group, function (parse_group) {
                        var is_new = false, option = new Object();
                        parse_group.replace(preg_attr, function (str0, k, val) {
                            if (k == 'name') {
                                if (val == 'leipiNewField') {
                                    is_new = true;
                                    fields++;
                                    val = 'data_' + fields;
                                }

                                attr_arr_all['name'] += dot_name + val;
                                dot_name = ',';

                            } else if (k == 'value') {
                                attr_arr_all['value'] += dot_value + val;
                                dot_value = ',';

                            }
                            option[k] = val;
                        });

                        if (!attr_arr_all['options']) attr_arr_all['options'] = new Array();
                        attr_arr_all['options'].push(option);
                        if (!option['checked']) option['checked'] = '';
                        var checked = option['checked'] ? 'checked="checked"' : '';
                        attr_arr_all['content'] += '<input type="checkbox" name="' + option['name'] + '" value="' + option['value'] + '"  ' + checked + '/>' + option['value'] + '&nbsp;';

                        if (is_new) {
                            var arr = new Object();
                            arr['name'] = option['name'];
                            arr['cwsplugins'] = attr_arr_all['cwsplugins'];
                            add_fields[option['name']] = arr;

                        }

                    });
                    attr_arr_all['content'] += '</span>';

                    //parse
                    template = template.replace(plugin, attr_arr_all['content']);
                    template_parse = template_parse.replace(plugin, '{' + name + '}');
                    template_parse = template_parse.replace('{|-', '');
                    template_parse = template_parse.replace('-|}', '');
                    template_data[pno] = attr_arr_all;
                    checkboxs++;

                } else if (name) {
                    if (tag == 'radios') /*单选组  一个字段*/
                    {
                        plugin = p0;
                        plugin = plugin.replace('|-', '');
                        plugin = plugin.replace('-|', '');
                        attr_arr_all['value'] = '';
                        attr_arr_all['content'] = '<span cwsplugins="radios" name="' + attr_arr_all['name'] + '" title="' + attr_arr_all['title'] + '">';
                        var dot = '';
                        p5.replace(preg_group, function (parse_group) {
                            var option = new Object();
                            parse_group.replace(preg_attr, function (str0, k, val) {
                                if (k == 'value') {
                                    attr_arr_all['value'] += dot + val;
                                    dot = ',';
                                }
                                option[k] = val;
                            });
                            option['name'] = attr_arr_all['name'];
                            if (!attr_arr_all['options']) attr_arr_all['options'] = new Array();
                            attr_arr_all['options'].push(option);
                            if (!option['checked']) option['checked'] = '';
                            var checked = option['checked'] ? 'checked="checked"' : '';
                            attr_arr_all['content'] += '<input type="radio" name="' + attr_arr_all['name'] + '" value="' + option['value'] + '"  ' + checked + '/>' + option['value'] + '&nbsp;';

                        });
                        attr_arr_all['content'] += '</span>';

                    } else {
                        attr_arr_all['content'] = is_new ? plugin.replace(/leipiNewField/, name) : plugin;
                    }
                    //attr_arr_all['itemid'] = fields;
                    //attr_arr_all['tag'] = tag;
                    template = template.replace(plugin, attr_arr_all['content']);
                    template_parse = template_parse.replace(plugin, '{' + name + '}');
                    template_parse = template_parse.replace('{|-', '');
                    template_parse = template_parse.replace('-|}', '');
                    if (is_new) {
                        var arr = new Object();
                        arr['name'] = name;
                        arr['cwsplugins'] = attr_arr_all['cwsplugins'];
                        add_fields[arr['name']] = arr;
                    }
                    template_data[pno] = attr_arr_all;


                }
                pno++;
            })
            var parse_form = new Object({
                'fields': fields,//总字段数
                'template': template,//完整html
                'parse': template_parse,//控件替换为{data_1}的html
                'data': template_data,//控件属性
                'add_fields': add_fields//新增控件
            });
            return JSON.stringify(parse_form);
        },
        /*type  =  save 保存设计 versions 保存版本  close关闭 */
        fnCheckForm: function (type) {
            if (uEditor.queryCommandState('source'))
                uEditor.execCommand('source');//切换到编辑模式才提交，否则有bug

            if (uEditor.hasContents()) {
                uEditor.sync();/*同步内容*/

                formeditor = uEditor.getContent();

                window.close();
                // alert("你点击了保存,这里可以异步提交，请自行处理....");
                return false;

                //--------------以下仅参考-----------------------------------------------------------------------------------------------------
                var type_value = '', formid = 0, fields = $("#fields").val(), formeditor = '';

                if (typeof type !== 'undefined') {
                    type_value = type;
                }
                //获取表单设计器里的内容
                //解析表单设计器控件
                var parse_form = this.parse_form(formeditor, fields);
                //alert(parse_form);

            } else {
                jAlert('表单内容不能为空！', '提示')
                $('#submitbtn').button('reset');
                return false;
            }
        },
        /*预览表单*/
        fnReview: function () {
            if (uEditor.queryCommandState('source'))
                uEditor.execCommand('source');/*切换到编辑模式才提交，否则部分浏览器有bug*/

            if (uEditor.hasContents()) {
                uEditor.sync();       /*同步内容*/

                jAlert("你点击了预览,请自行处理....", "提示");
                return false;
                //--------------以下仅参考-------------------------------------------------------------------


                /*设计form的target 然后提交至一个新的窗口进行预览*/
                document.saveform.target = "mywin";
                window.open('', 'mywin', "menubar=0,toolbar=0,status=0,resizable=1,left=0,top=0,scrollbars=1,width=" + (screen.availWidth - 10) + ",height=" + (screen.availHeight - 50) + "\"");

                document.saveform.action = "/index.php?s=/index/preview.html";
                document.saveform.submit(); //提交表单
            } else {
                jAlert('表单内容不能为空！', '提示');
                return false;
            }
        }
    };

    function createTxtCtl(fieldName, fieldTitle) {
        var content = '<input title="' + fieldTitle + '" value="' + fieldTitle + '" name="' + fieldName + '" type="text"/>';
        UE.getEditor('myFormDesign').focus();
        UE.getEditor('myFormDesign').execCommand('inserthtml', content);
    }
</script>
</body>
</html>