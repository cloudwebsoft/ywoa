<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.cloudweb.oa.utils.SysUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

    Privilege pvg = new Privilege();
    int flowId = ParamUtil.getInt(request, "flowId", -1);
    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
    long id = ParamUtil.getLong(request, "id", -1); // id为智能模块中所编辑的记录的ID

    SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
%>
$(function() {
	// 如果不允许为空，则在此需取消掉，因为在submit事件前，livevalidation先作了检测
	if (typeof(f_<%=fieldName%>)!="undefined") {
    	f_<%=fieldName%>.formObj.removeField(f_<%=fieldName%>);
    }
});

// 20200701 UE.Editor.prototype.getActionUrl配置放在$(function())中，会导致在智能模块中上传图片按钮发灰
// $(function() {
    // 使用过的Ueditor ID被删除后，再次用之前的ID创建ueditor实例会失败
    // 所以在vue中需先销毁再创建，否则只能调用一次
    if (uEditor) {
        UE.getEditor('<%=fieldName%>').destroy();
    }

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
// });


if (!isUeditorActionUrlInited) {
    // 此处不能初始化两次，否则会导致_bkGetActionUrl的地址在第二次初始化时，成为了其本身，当调用getActionUrl时陷入循环，致堆栈溢出
    var isUeditorActionUrlInited = false;
    window.setTimeout("initUeditorActionUrl()", 0);
    function initUeditorActionUrl() {
        isUeditorActionUrlInited = true;
        UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
        UE.Editor.prototype.getActionUrl = function(action) {
            if (action == 'uploadimage' || action == 'uploadscrawl') {
                return '<%=sysUtil.getRootPath()%>/ueditor/UploadFile?op=UEditorCtl&flowId=<%=flowId%>&id=<%=id%>&formCode=<%=formCode%>';
            } else if (action == 'uploadvideo') {
                return '<%=sysUtil.getRootPath()%>/ueditor/UploadFile?op=UEditorCtl&flowId=<%=flowId%>&id=<%=id%>&formCode=<%=formCode%>';
            } else {
                return this._bkGetActionUrl.call(this, action);
            }
        }
    }
}
