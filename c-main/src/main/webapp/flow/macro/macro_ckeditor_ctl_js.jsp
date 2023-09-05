<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");
    String fieldName = ParamUtil.get(request, "fieldName");
%>
// ajaxForm序列化提交数据之前的回调函数
function ctlOnBeforeSerialize() {
	// ajaxForm提交前，必须先提取赋值
    $('#<%=fieldName%>').val(CKEDITOR.instances.<%=fieldName%>.getData());
}

CKEDITOR.plugins.registered['save'] = {
    init: function (editor) {
       var command = editor.addCommand('save',
       {
            modes: { wysiwyg: 1, source: 1 },
            exec: function (editor) { // Add here custom function for the save button
              saveDraft();
            }
       });
       // 注释掉可删除save按钮
       // editor.ui.addButton('Save', { label: 'Save', command: 'save' });
    }
}

$(function() {
	// 如果不允许为空，则在此需取消掉，因为在submit事件前，livevalidation先作了检测
	if (typeof(f_<%=fieldName%>)!="undefined") {
    	f_<%=fieldName%>.formObj.removeField(f_<%=fieldName%>);
    }
});