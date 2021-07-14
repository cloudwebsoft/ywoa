<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int formId = ParamUtil.getInt(request, "form_id");
String op = ParamUtil.get(request, "op");

QuestionnaireFormDb qfd = new QuestionnaireFormDb();
qfd = qfd.getQuestionnaireFormDb(formId);
QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>添加题目</title>
<style>
a:link {
text-decoration:none;
color:#4B4B4B;
}
a:visited {
text-decoration:none;
color:#4B4B4B;
}
a:hover {
text-decoration:none;
color:#FF3300;
}
</style>
<script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js?2023"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js?2023"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>
<script>
var count = 1;
function checkItemType() {
	if(document.getElementById('item_type').value==2 || document.getElementById('item_type').value==3) {
		for(var i=0; i<=count; i++) {
			var optionId = "option" + i;
			var option = document.getElementById(optionId);
			if(option==null)continue;
			option.disabled = "";
			option.style.backgroundColor = "";
			var aId = "a" + i;
			var a = document.getElementById(aId);
			if(a==null)continue;
			a.disabled = "";
			a.href = "javascript:delOption(" + i + ")";
		}
		document.getElementById('item_add').disabled = "";
	} else {
		for(var i=0; i<=count; i++) {
			var optionId = "option" + i;
			var option = document.getElementById(optionId);
			if(option==null)continue;
			option.disabled = "disabled";
			option.style.backgroundColor = "#cccccc";
			var aId = "a" + i;
			var a = document.getElementById(aId);
			if(a==null)continue;
			a.disabled = "disabled";
			a.href = "#";
		}
		document.getElementById('item_add').disabled = "disabled";
	}
}
function addOption() {
	var option = document.createElement("input");
	option.id = "option" + count;
	option.name = "option" + count;
	option.type = "text";
	option.style.width = "200px"
	document.getElementById('options').appendChild(option);
	var a = document.createElement("a");
	a.id = "a" + count;
	a.href = "javascript:delOption(" + count + ")";
	a.innerHTML = "&nbsp;[删除]";
	a.style.color = "red";
	document.getElementById('options').appendChild(a);
	var br = document.createElement("br");
	br.id = "br" + count;
	document.getElementById('options').appendChild(br);
	count++;
}
function delOption(id) {
	var optionId = "option" + id;
	var aId = "a" + id;
	var brId = "br" + id;
	var option = document.getElementById(optionId);
	var a = document.getElementById(aId);
	var br = document.getElementById(brId);
	document.getElementById('options').removeChild(option);
	document.getElementById('options').removeChild(a);
	document.getElementById('options').removeChild(br);
}
function submitAddItemForm() {
	var itemOptions = document.getElementById('item_options');
	for(var i=0; i<=count; i++) {
		var optionId = "option" + i;
		var option = document.getElementById(optionId);
		if(option==null || option.value=="")continue;
		if(itemOptions.value == "") {
			itemOptions.value += option.value;
		} else {
			itemOptions.value += ":,:" + option.value;
		}
	}
	addItemForm.submit();
}
function editItem(itemName,itemId) {
	document.getElementById('form_title').innerText = "修改项目：“" + itemName + "”";
	document.getElementById('form_title').style.color = "#FF0000";
	document.getElementById('add_or_edit').value = "修改问卷项目";
	document.getElementById('add_or_edit').style.color = "#0000FF";
	document.getElementById('op').value = "edit";
	document.getElementById('item_id').value = itemId;
}
</script>
</head>
<body>
<%@ include file="questionnaire_subject_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table class="percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="center"><%=qfd.getFormName()%></td>
  </tr>
</table>
<form id="addItemForm" name="addItemForm" action="questionnaire_subject_list.jsp" method="post">
  <table class="tabStyle_1 percent80" align="center" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" id="form_title" colspan="2" align="center">添加题目</td>
    </tr>
    <tr>
      <td width="84" align="right">名称：</td>
      <td width="304" align="left">
      <textarea id="item_name" name="item_name" style="width:100%" /></textarea>
<script>
var uEditor;
$(function() {
	uEditor = UE.getEditor('item_name',{
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
				initialFrameHeight:200,
				disabledTableInTable:false 
				///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
				//更多其他参数，请参考ueditor.config.js中的配置项
			});
			
	UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
	UE.Editor.prototype.getActionUrl = function(action) {
	    if (action == 'uploadimage' || action == 'uploadscrawl') {
	        return '<%=request.getContextPath()%>/ueditor/UploadFile?op=notice';
	    } else if (action == 'uploadvideo') {
	        return '<%=request.getContextPath()%>/ueditor/UploadFile?op=notice';
	    } else {
	        return this._bkGetActionUrl.call(this, action);
	    }
	}
});
</script>        
	<input id="op" name="op" type="hidden" value="add" />
    <input id="item_id" name="item_id" type="hidden" />
    <input name="form_id" type="hidden" value="<%=formId%>" /></td>
    </tr>
    <tr>
      <td align="right">类型：</td>
      <td align="left"><select id="item_type" name="item_type" onchange="checkItemType()">
        <option value="0">填空</option>
        <option value="1">问答</option>
        <option value="2">单选</option>
        <option value="3">多选</option>
      </select>
      </td>
    </tr>
    <tr>
      <td align="right">是否必填项：</td>
      <td align="left"><select name="checked_type">
        <option value="0">是</option>
        <option value="1">否</option>
      </select>
      </td>
    </tr>
    <tr>
      <td align="right">排序号：</td>
      <td align="left"><input name="item_index" title="项目在问卷中按索引升序排列" size="3" value="0" /></td>
    </tr>
    <tr>
      <td align="right">选项：</td>
      <td align="left" id="options"><input type="hidden" id="item_options" name="item_options" />
          <input disabled="disabled" id="option0" name="option0" style="width:200px; background-color:#cccccc" />
          <input name="button2" type="button" disabled="disabled" id="item_add" style="height:22px;padding-top:1px;color:#4B4B4B;font-family:'宋体';font-size:12px" onclick="addOption()" value="增加选项" />
        <br />
      </td>
    <tr>
      <td colspan="2" align="center"><input class="btn" name="button2" type="button" id="add_or_edit" onclick="submitAddItemForm()" value="确定" /></td>
    </tr>
</table>
</form>
</body>
</html>
