<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%
Privilege pvg = new Privilege();
int flowId = ParamUtil.getInt(request, "flowId", -1);
String fieldName = ParamUtil.get(request, "fieldName");
%>
$(function() {
	// 如果不允许为空，则在此需取消掉，因为在submit事件前，livevalidation先作了检测
	if (typeof(f_<%=fieldName%>)!="undefined") {
    	f_<%=fieldName%>.formObj.removeField(f_<%=fieldName%>);
    }
});

$(function() {
    var uEditor = UE.getEditor('<%=fieldName%>',{
        //allowDivTransToP: false,//阻止转换div 为p
        toolleipi:true,//是否显示，设计器的 toolbars
        textarea: 'htmlcode',
        enableAutoSave: false,
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
        wordCount:false,
        //关闭elementPath
        elementPathEnabled:false,
        //默认的编辑区域高度
        initialFrameHeight:300,
        initialFrameWidth:'100%',
        disabledTableInTable:false
        ///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
        //更多其他参数，请参考ueditor.config.js中的配置项
    });

    UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
    UE.Editor.prototype.getActionUrl = function(action) {
        if (action == 'uploadimage' || action == 'uploadscrawl') {
            return '<%=request.getContextPath()%>/ueditor/UploadFile?op=UEditorCtl&flowId=<%=flowId%>';
        } else if (action == 'uploadvideo') {
            return '<%=request.getContextPath()%>/ueditor/UploadFile?op=UEditorCtl&flowId=<%=flowId%>';
        } else {
            return this._bkGetActionUrl.call(this, action);
        }
    }

    uEditor.addListener('beforepaste', myEditor_paste);
    function myEditor_paste(type, data) {
        if (o("webedit")) {
            var imgPath = o("webedit").SavePasteToFile();
            if (imgPath!="") {
                UE.getEditor('<%=fieldName%>').focus();
                UE.getEditor('<%=fieldName%>').execCommand('inserthtml', "<img src='" + imgPath + "' />");
            }
        }
    }
});