<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	FormQueryReportMgr rm = new FormQueryReportMgr();
	FormQueryReportDb rd = new FormQueryReportDb();
	JSONObject json = new JSONObject();
	boolean re = false;
	try {
		re = rm.create(request, rd, "form_query_report_create");
		rd = rd.getLastFormQueryReportDb(privilege.getUser(request));
	}
	catch (ErrMsgException e) {
		// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage(), true));
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("id", rd.getLong("id"));
		out.print(json);
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		out.print(json);
	}
	return;
}
else if (op.equals("edit")) {
	FormQueryReportMgr rm = new FormQueryReportMgr();
	FormQueryReportDb rd = new FormQueryReportDb();
	long id = ParamUtil.getLong(request, "id");
	rd = (FormQueryReportDb)rd.getQObjectDb(new Long(id));
	JSONObject json = new JSONObject();
	boolean re = false;
	try {
		re = rm.save(request, rd, "form_query_report_save");
	}
	catch (ErrMsgException e) {
		e.printStackTrace();
		// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage(), true));
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("id", id);
		out.print(json);
		return;
	}
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("id", id);
		out.print(json);
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		json.put("id", id);
		out.print(json);
	}
	return;
}

long id = ParamUtil.getLong(request, "id", -1);
int queryId = ParamUtil.getInt(request, "query_id", -1);
String title = "";
String content = "";
if (id!=-1) {
	FormQueryReportDb rd = new FormQueryReportDb();
	rd = (FormQueryReportDb)rd.getQObjectDb(new Long(id));
	if (rd == null) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请先创建查询！"));
		return;
	}	
	queryId = rd.getInt("query_id");
	title = rd.getString("title");
	content = rd.getString("content");
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>报表设计器</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/designer.css" />
<script type="text/javascript" src="../../inc/common.js"></script>
<script type="text/javascript" src="../../js/jquery.js"></script>
<script src="../../js/jquery.form.js"></script>
<script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language="javascript" type="text/javascript" src="../../js/tiny_mce/tiny_mce.js"></script>
<style>
html,body{height:100%}
</style>
<script>
function onBeforeSubmit() {
	// alert(tinyMCE.getInstanceById('content').getBody().innerHTML);
	// alert(tinyMCE.get('content').getContent());
	// alert(tinyMCE.activeEditor.getContent({source_view : true}));
	// alert(tinyMCE.activeEditor.getContent({format : 'raw'}));
}

$(document).ready(function() {
    var options = { 
        //target:        '#output2',   // target element(s) to be updated with server response 
        // beforeSubmit:  automaticOnmodule_codeSubmit,  // pre-submit callback 
        beforeSubmit:  onBeforeSubmit,  // pre-submit callback 
        success:       showResponse  // post-submit callback 
 
        // other available options: 
        //url:       url         // override for form's 'action' attribute 
        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
        //dataType:  null        // 'xml', 'script', or 'json' (expected server response type) 
        //clearForm: true        // clear all form fields after successful submit 
        //resetForm: true        // reset the form after successful submit 
 
        // $.ajax options can be used here too, for example: 
        //timeout:   3000 
    }; 

    // bind to the form's submit event
    $('#formDesigner').submit(function() {
		// 必须加上此句才能提交成功
		$('#formDesigner').bind('form-pre-serialize', function(event, form, options, veto) {
			tinyMCE.triggerSave();
		});
		
        $(this).ajaxSubmit(options);
			
        return false;
    });
});

function showResponse(responseText, statusText, xhr, $form) {
	var data = $.parseJSON(responseText);
	alert(data.msg);
	if (data.ret=="1") {
		window.location.href = "designer.jsp?id=" + data.id;
	}

	/*
	// 会导致右边栏的样式变化
	jAlert(data.msg, '提示', function(r) {
		if (data.ret=="1")
			window.location.href = "designer.jsp?id=" + data.id;
	});
	*/
}

function selQuery() {
	openWin("../form_query_list_sel.jsp", 800, 600);
}

function doSelQuery(id, title) {
	if (id==<%=queryId%>)
		return;
	
	<%if (id!=-1) {%>
	if (!confirm("您确定要重新选择么？"))
		return;
	<%}%>
	
	$("#query_id").val(id);
	
	// $("#divRelatedQuery").html($("#divRelatedQuery").html() + "<div id='divQuery" + id + "'><a href='javascript:;' onclick=\"\">" + title + "</a><span style='color:red; font-size:14px; padding-left:5px; cursor:pointer; display:none' title='删除' onclick=\"delQuery('" + id + "')\">×</span></div>");
	$("#formDesigner").submit();
}

$(document).ready(function() {
	rendTinyMCE();
});
</script>
</head>
<body style="margin:0px; padding:0px">
<div id="mainDiv" style="width:100%; height:100%">
<form id="formDesigner" action="designer.jsp" method="post">
<table id="mainTable" border="0" cellspacing="0" style="width:100%;height:100%;margin:0px;padding:0px">
<tr>
	<td id="tdTinyMCE">
    <textarea id="content" name="content"><%=content%></textarea>
	</td>
  	<td id="rightTd" valign="top" style="width:300px; padding:0px; font-size:12px">
      <div id="item_name" class="rebbonItem">
          <div>
            <div class="item_title">报表名称</div>
            <div class="item_content">
                <input id="title" name="title" value="<%=title%>" />
                
                <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
                <input type="submit" value="保存" />
				<%if (id==-1) {%>
                <input name="op" value="add" type="hidden" />
                <%}else{%>
                <input name="op" value="edit" type="hidden" />
                <input type="button" value="查看" onClick="addTab('<%=title%>', 'flow/report/form_report_show_jqgrid.jsp?reportId=<%=id%>')" />
               <%}%>
                <input name="id" value="<%=id%>" type="hidden" />
                <input id="query_id" name="query_id" value="<%=queryId%>" type="hidden" />
            </div>
          </div>
          
          <div>
            <div class="item_title">查询结果集&nbsp;&nbsp;[<a href="javascript:;" onClick="selQuery()">选择</a>]</div>
            <div class="item_content">
				<div id="divRelatedQuery">
                <%
				if (queryId!=-1) {
					FormQueryDb fqd = new FormQueryDb();
					fqd = fqd.getFormQueryDb((int)queryId);
					%>
					<a href="javascript:;" onClick="addTab('<%=fqd.getQueryName()%>', 'flow/designer/designer.jsp?id=<%=fqd.getId()%>')"><%=fqd.getQueryName()%></a>
					<%
				}
				%>
                </div>

            </div>
          </div>

          <div>
            <div class="item_title">X轴</div>
            <div class="item_content">
				<%
				String optsX = "";
				String opts = "";
				if (queryId!=-1) {
					FormQueryDb fqd = new FormQueryDb();
					fqd = fqd.getFormQueryDb(queryId);
					String formCode = fqd.getTableCode();
					FormDb fd = new FormDb();
					fd = fd.getFormDb(formCode);
					%>
						<%=fd.getName()%>
						<br />
						X轴
					<%
					// 取得关联查询中默认的colProps
					String queryRelated = fqd.getQueryRelated();
					if (!queryRelated.equals("")) {
						int queryRelatedId = StrUtil.toInt(queryRelated, -1);
						FormQueryDb aqdRelated = fqd.getFormQueryDb(queryRelatedId);
						FormDb fdRelated = fd.getFormDb(aqdRelated.getTableCode());
						Iterator ir = fdRelated.getFields().iterator();
						while (ir.hasNext()) {
							FormField ff = (FormField)ir.next();
							// if (!ff.isCanList())
							// 	continue;
							opts += "<option value='rel." + ff.getName() + "'>" + ff.getTitle() + "</option>";
						}
					}
					
					Iterator ir = fd.getFields().iterator();
					while (ir.hasNext()) {
						FormField ff = (FormField)ir.next();
						// if (!ff.isCanList())
						// 	continue;
						opts += "<option value='" + ff.getName() + "'>[ " + ff.getTitle() + " ]</option>";
						optsX += "<option value='" + ff.getName() + "'>[ " + ff.getTitle() + " ]</option>";
					}					
					%>
					<select id="fieldName" name="fieldName">
					<%=optsX%>
                    </select>
                    <select id="formula" name="formula">
                      <option value="sum">求和</option>
                      <option value="average">平均值</option>
                      <option value="count">数量</option>
                    </select>
                    <input type="button" value="确定" onClick="insertXCol()" />
                <%}%>
            </div>
          </div>

          <div>
            <div class="item_title">Y轴</div>
            <div class="item_content">
            	<%if (queryId!=-1) {%>
                    Y轴
                    <select id="fieldNameY" name="fieldNameY">
                    <%=opts%>
                    </select>
                    
                    <select id="fieldtype" name="fieldtype">
                    <option value="">请选择类型</option>
                    <option value="<%=FormQueryReportDb.TYPE_USER%>">姓名</option>
                    <option value="<%=FormQueryReportDb.TYPE_DEPT%>">部门</option>
                    <option value="<%=FormQueryReportDb.TYPE_FIELD%>">字段</option>
                    </select>
                    <input value="确定" onClick="insertYCol()" type="button" />
                <%}%>
            </div>
          </div>          
          
      </div>
    </td>
</tr>
</table>
</form>
</div>
<div id="result" style="display:none"></div>
</body>

<script>
function insertXCol() {
	// 注意fieldname必须为小写，否则会被tinyMCE过滤掉
	// alert($("#fieldName").find("option:selected").text());
	var str = "<input name='xCol' axis='X' fieldname='" + $("#fieldName").val() + "' value='" + $("#fieldName").find("option:selected").text() + "' formula='" + $("#formula").val() + "'>";
	// 这样会使得插入表格的单元格时，表格会消失
	// tinyMCE.execCommand('mceInsertRawHTML', false, str);
	
	tinyMCE.activeEditor.focus();
	tinyMCE.activeEditor.selection.setContent(str);
}

function insertYCol() {
	var selection = tinyMCE.activeEditor.selection.getNode().getAttribute("fieldname");
	if (selection) {
		var obj = tinyMCE.activeEditor.selection.getNode();
		obj.setAttribute("fieldname", $("#fieldNameY").val());
		obj.setAttribute("value", $("#fieldNameY").find("option:selected").text());
		obj.setAttribute("fieldtype", $("#fieldtype").val());
		obj.setAttribute("axis", "Y");
		// 先删再加上才能正确显示，否则不刷新
		// obj.outerHTML = "";
		var str = obj.outerHTML;
		$(obj).remove();
		
		tinyMCE.activeEditor.selection.setContent(str);
		
		// 按以下写法也不刷新
		// tinyMCE.isNotDirty = true;
		// tinyMCE.activeEditor.nodeChanged();
		// tinyMCE.activeEditor.selection.collapse();
		// tinyMCE.activeEditor.execCommand("mceRepaint");
		// tinyMCE.activeEditor.render();
		
		return;
	}
	
	// 注意fieldname必须为小写，否则会被tinyMCE过滤掉
	// alert($("#fieldName").find("option:selected").text());
	if ($('#fieldtype').val()=="") {
		alert("请选择Y轴的类型！");
		return;
	}
	
	var str = "<input name='yCol' axis='Y' fieldname='" + $("#fieldNameY").val() + "' value='" + $("#fieldNameY").find("option:selected").text() + "' fieldtype='" + $("#fieldtype").val() + "'>";
	// 这样会使得插入至表格的单元格时，表格会消失
	// tinyMCE.execCommand('mceInsertRawHTML', false, str);
	
	tinyMCE.activeEditor.focus();
	tinyMCE.activeEditor.selection.setContent(str);
}

function test() {
	// tinyMCE.execCommand('mceInsertContent',false,"<p><img src=\"../images/house.jpg\" alt=\"\" width=\"588\" height=\"419\" /></p>");
	// alert(tinyMCE.getInstanceById('content').getBody().innerHTML);
	// var selection = tinyMCE.activeEditor.selection.getContent({format : 'raw'});
	
	var selection = tinyMCE.activeEditor.selection.getNode().fieldname;
	alert(selection);
}

function reinitIframe() {
    try {
        // var iframe = document.getElementsByTagName("iframe")[0];
        var iframe = document.getElementById("content")
        iframe.style.height = iframe.contentWindow.document.documentElement.scrollHeight + "px";
       
    } catch (ex) { }
}

// window.setInterval("reinitIframe()", 200);

function toScreenHeight(id, minus) {
    var height;

    if (typeof(window.innerHeight) == "number") //non-IE
    	height = window.innerHeight;
    else if (document.documentElement && document.documentElement.clientHeight) //IE 6+ strict mode
    	height = document.documentElement.clientHeight;
    else if (document.body && document.body.clientHeight) //IE 4 compatible / IE quirks mode
    	height = document.body.clientHeight;

    document.getElementById(id).style.height = (height - minus) + "px";
}

function resizeTiny() {	
	if (!tinyMCE) return;
	var w = o("tdTinyMCE").clientWidth;
	// 减去110，可能是相当于工具栏部分的高度
	var h = o("mainDiv").clientHeight - 130;
	tinyMCE.get("content").theme.resizeTo(w, h);
}

function myCustomOnChangeHandler(inst) {
	// alert("The HTML is now:" + inst.getBody().innerHTML);
}

function rendTinyMCE() {

	tinyMCE.init({
		mode : "textareas",
		language :"zh-cn",
		theme : "advanced",
		width : "100%",
		height : "100%",
		// plugins : "fullpage, preview",
		// plugins : "autolink,lists,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,cell", 
		plugins : "autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist,autosave,visualblocks,cell",

		// Theme options 
		theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect,fontselect,fontsizeselect", 
		theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor", 
		theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen", 
		// theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,spellchecker,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,blockquote,pagebreak,|,insertfile,insertimage", 
		theme_advanced_buttons4 : "cell", 
		theme_advanced_toolbar_location : "top", 
		theme_advanced_toolbar_align : "left", 
		theme_advanced_statusbar_location : "bottom", 
		theme_advanced_resizing : true, 
		
		theme_advanced_fonts : "Arial=arial,helvetica,sans-serif;Courier New=courier new,courier,monospace",
		
		oninit : "resizeTiny",
		
		// skin: "o2k7",
		// skin_variant : "silver",
		// Example content CSS (should be your site CSS)
		content_css : "css/example.css",
	
		/*
		setup : function(ed) { 
		  ed.onClick.add(function(ed, e) { 
			  alert('Editor was clicked: ' + e.target.nodeName); 
		  }); 
		},
		*/
		
		onchange_callback : "myCustomOnChangeHandler",
		
		handle_event_callback: function(e) {
				if (e.target.nodeName=="INPUT") {
					if (e.type=="mousedown") {
						if (e.srcElement.getAttribute("axis")=="X") {
							$("#fieldName").val(e.srcElement.getAttribute("fieldname"));
							$("#formula").val(e.srcElement.getAttribute("formula"));
						}
						else {
							$("#fieldNameY").val(e.srcElement.getAttribute("fieldname"));
							$("#fieldtype").val(e.srcElement.getAttribute("fieldtype"));
						}
					}
					else
						; // alert(e.type);
					// alert(e.target.nodeName);
					// alert(e.srcElement.getAttribute("value"));
				}
			},
		
		// extended_valid_elements : "input[class|type=text|name|value|checked|maxlength|onclick|type|fieldName]",
		// valid_elements: "@[id|class|title|style|onmouseover|name|value]," + "module," + "a[name|href|target|title|alt]," + "#p,blockquote,-ol,-ul,-li,br,img[src|height|width],-sub,-sup,-b,-i,-u," + "-span[data-mce-type],hr,input[class|type=text|name|value|checked|maxlength|onclick|type|fieldname|formula|fieldtype|axis],select,table[width|height],thead,tr,td[colspan|rowspan|width|height]"
		valid_elements : '*[*]'
	});
	
	
	
}
/*
tinymce.get('content').onClick.add(function(ed, e) { 
   ed.windowManager.alert('Hello world!'); 
});
*/

tinyMCE.dom.Event.add(window,'resize',function(){
		  resizeTiny();
	  });

</script>
</html>