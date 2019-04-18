<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计 - 导入设置</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>

<script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />  
<script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script src="<%=request.getContextPath()%>/js/powerFloat/jquery-powerFloat.js"></script>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/js/powerFloat/powerFloat.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common.css" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script>
includFile("<%=request.getContextPath()%>/js/colorpicker/",['jquery.bigcolorpicker.css']);
includFile("<%=request.getContextPath()%>/js/colorpicker/",['jquery.bigcolorpicker.min.js']);
</script>
<style>
.target_box{width:780px; position:relative; top:100px; left:300px; padding:10px; border:1px solid #aaa; background-color:#fff;}
.target_list{padding:4px; border-bottom:1px dotted #ddd; overflow:hidden; _zoom:1;}
.target_list span{width:150px; line-height:20px; margin-right:5px; padding:1px; color:#333; font-size:12px; text-align:left; text-decoration:none; float:left;}
.custom_container{position:absolute; background-color:rgba(0, 0, 0, .5); background-color:#999\9;}
.custom_container img{padding:0; position:relative; top:-5px; left:-5px;}
.shadow{-moz-box-shadow:1px 1px 3px rgba(0,0,0,.4); -webkit-box-shadow:1px 1px 3px rgba(0,0,0,.4); box-shadow:1px 1px 3px rgba(0,0,0,.4);}
</style>
</head>
<body><jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String code = ParamUtil.get(request, "code"); // 模块编码
String formCode = ParamUtil.get(request, "formCode");
String resource = ParamUtil.get(request, "resource");//来源

ModuleSetupDb vsd = new ModuleSetupDb();
vsd = vsd.getModuleSetupDbOrInit(code);
formCode = vsd.getString("form_code");

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！", "提示"));
	return;
}
%>
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu9").className="current"; 
</script>
<div class="spacerH"></div>
<form id="form1" method="post" enctype="multipart/form-data">
<table class="tabStyle_1 percent98" width="98%" align="center">
    <tr>
      <td colspan="3" align="left" class="tabStyle_1_title">导出配置</td>
    </tr>
    <tr>
      <td align="center">名称</td>
      <td colspan="2" align="left">
        <input id="name" name="name" />
        &nbsp;&nbsp;
        <input id="is_serial_no" name="is_serial_no" value="1" type="checkbox" checked />
        显示序号
      </td>
    </tr>
    <tr>
	<td align="center">标题栏文字</td>
      <td colspan="2" align="left">
        <input id="bar_name" name="bar_name" />
        (空表示无标题栏）
      </td>
    </tr>
    <tr>
      <td align="center">标题栏格式</td>
      <td align="left">
		字体
      <%
      com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
      String fontFamily = cfg.get("fontFamily");
      fontFamily = fontFamily.replaceAll("，", ",");
      String[] aryFont = StrUtil.split(fontFamily, ",");
      %>
      <select id="bar_font_family" name="bar_font_family">
      <%
	  if (aryFont!=null) {
		  for (String f : aryFont) {
		  	%>
		  	<option value="<%=f %>"><%=f %></option>
		  	<%
		  }
	  }
	  %>
      </select>
      	大小
      <input id="bar_font_size" name="bar_font_size" size="5" value="16" />px    
      	行高
      <input id="bar_line_height" name="bar_line_height" value="30" size=5 />px      
		 &nbsp;&nbsp;&nbsp;&nbsp;
        <div id="bar_color_show" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block;"></div>
         背景色
		<input name="bar_back_color" id="bar_back_color" type="hidden"/>
		<script>
		$('#bar_color_show').bigColorpicker(function(el,color) {
			$(el).css("background-color",color);
			$('#bar_back_color').val(color);
			$('#previewBar').css("background-color", color);			
		});
		</script>		
		<div id="barForeColor" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block;"></div>	
		字体颜色
		<input name="bar_fore_color" id="bar_fore_color" type="hidden"/>
		<script>
		$(function() {			
			$('#barForeColor').bigColorpicker(function(el,color){
				$(el).css("background-color",color);
				$('#bar_fore_color').val(color);
				$('#previewBar').css("color", color);
			});			
			$('#bar_font_family').change(function() {
				$('#previewBar').css("font-family", $(this).val());			
			});
			$('#bar_font_size').change(function() {
				$('#previewBar').css("font-size", $(this).val() + "px");			
			});		
			$('#bar_is_bold').click(function() {
				var fw = $(this).prop("checked")?"bold":"normal";
				$('#previewBar').css("font-weight", fw);			
			});	
			$('#bar_line_height').change(function() {
				$('#previewBar').css("line-height", $(this).val() + "px");			
				$('#previewBar').css("height", $(this).val() + "px");			
			});			
		});
		</script>	        
      	<input id="bar_is_bold" name="bar_is_bold" value="1" type="checkbox" checked />
     	 加粗      
      <script>
		// var bar_ = new LiveValidation('bar_name');
		// bar_.add( Validate.Presence );
 		var bar_line_height = new LiveValidation('bar_line_height');
		bar_line_height.add( Validate.Presence );
		bar_line_height.add( Validate.Numericality );
 		var bar_font_size = new LiveValidation('bar_font_size');
		bar_font_size.add( Validate.Presence );
		bar_font_size.add( Validate.Numericality );
      </script>         
      </td>
      <td align="left">
        <div id="previewBar" style="width:200px; float:left; text-align:center; padding:3px 0px; height:30px; font-weight:bold; border:1px solid #ccc">标题栏预览</div>            
      </td>    
    </tr>
    <tr>
      <td width="12%" align="center">表头</td>
      <td width="53%" align="left">
      	字体
      <select id="font_family" name="font_family">
      <%
	  if (aryFont!=null) {
		  for (String f : aryFont) {
		  	%>
		  	<option value="<%=f %>"><%=f %></option>
		  	<%
		  }
	  }
	  %>
      </select>
      	大小
      <input id="font_size" name="font_size" value="16" size="5" />px
      	行高
      <input id="line_height" name="line_height" value="30" size=5 />px
      <script>
		var name = new LiveValidation('name');
		name.add( Validate.Presence );
 		var line_height = new LiveValidation('line_height');
		line_height.add( Validate.Presence );
		line_height.add( Validate.Numericality );
 		var font_size = new LiveValidation('font_size');
		font_size.add( Validate.Presence );
		font_size.add( Validate.Numericality );
      </script>        
     	 &nbsp;&nbsp;&nbsp;&nbsp;
		<div id="color_show" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: rgb(255, 255, 255);"></div>	
         背景色
		<input name="back_color" id="back_color" type="hidden" value="#ffffff"/>
		<script>
		$('#color_show').bigColorpicker(function(el,color){
			$(el).css("background-color",color);
			$('#back_color').val(color);
			$('#preview').css("background-color", color);			
		});
		</script>
		<div id="foreColor" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: rgb(0, 0, 0);"></div>	
        字体颜色 
		<input name="fore_color" id="fore_color" type="hidden" value="#000000"/>
		<script>
		$('#foreColor').bigColorpicker(function(el,color){
			$(el).css("background-color",color);
			$('#fore_color').val(color);
			$('#preview').css("color", color);
		});
		$(function() {
			$('#font_family').change(function() {
				$('#preview').css("font-family", $(this).val());			
			});
			$('#font_size').change(function() {
				$('#preview').css("font-size", $(this).val() + "px");			
			});		
			$('#is_bold').click(function() {
				var fw = $(this).prop("checked")?"bold":"normal";
				$('#preview').css("font-weight", fw);			
			});	
			$('#line_height').change(function() {
				$('#preview').css("line-height", $(this).val() + "px");			
				$('#preview').css("height", $(this).val() + "px");			
			});			
		});
		</script>	        
      	<input id="is_bold" name="is_bold" value="1" type="checkbox" checked />
     	 加粗
      </td>
      <td width="35%" align="left">
        <div id="preview" style="width:200px; float:left; text-align:center; padding:3px 0px; height:30px; font-weight:bold; border:1px solid #ccc">表头预览</div>      
      </td>
    </tr>
    <tr>
      <td align="center">角色</td>
      <td colspan="2" align="left">
		<textarea title="为空则表示角色不限，均可以使用此模板" id="roleDescs" name="roleDescs" style="width:40%; height:60px" readonly="readonly"></textarea>
        <input id="roleCodes" name="roleCodes" type="hidden" />
        <a href="javascript:;" onclick="selRoles()">选择角色</a>       
      </td>
    </tr>
    <tr>
      <td colspan="3">&nbsp;
        <div id="customContainer" class="custom_container"></div>
        <div style="margin:0 auto; width:98%;">
        预览列表（拖动可以调整列宽和位置）
        <input type="button" id="trigger" src="targetBox" name="selBtn" title="选择列" class="grey_btn_55" value="选择列" />
        </div>    
        <div id="viewBox" class="" style="margin:0 auto; width:98%">
            <div class="view grid expGrid"></div>
        </div>          
      </td>
    </tr>
    <tr>
      <td colspan="3" align="center">
          <input type="button" class="btn btn-default btn-ok" value="确定"/>
          <input name="code" value="<%=code%>" type="hidden"/>
          <input name="formCode" value="<%=formCode%>" type="hidden"/>        
      </td>
    </tr>
</table>
</form>

<div id="targetBox" class="shadow target_box" style="display:none">
	<div class="target_list">
<%
String listField = StrUtil.getNullStr(vsd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(vsd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(vsd.getString("list_field_order"));
String[] fieldOrder = StrUtil.split(listFieldOrder, ",");
String listFieldLink = StrUtil.getNullStr(vsd.getString("list_field_link"));
String[] fieldsLink = StrUtil.split(listFieldLink, ",");

int len = 0;
if (fields!=null)
	len = fields.length;
int i;
Vector v3 = fd.getFields();
Iterator ir3 = v3.iterator();
List<String> list = null;
if (len == 0){
	list = new ArrayList<String>();
	String checked = "checked";
	// 如果列中字段为空，则默认初始化为前6个字段
	if (v3.size() >= 6){
		int j = 0;
		while (ir3.hasNext()) {
			FormField ff = (FormField) ir3.next();
			if (j>=6){
				%>
				<span><input type="checkbox" id="<%=ff.getName()%>" name="<%=ff.getName()%>" title="<%=ff.getTitle()%>" /><%=ff.getTitle()%></span>
				<%
			}else{
				list.add(ff.getName());
		%>
				<span><input type="checkbox" id="<%=ff.getName()%>" name="<%=ff.getName()%>" title="<%=ff.getTitle()%>" checked="<%=checked%>" /><%=ff.getTitle()%></span>
		<%	
			}
			j++;
		}
	}else{
		while (ir3.hasNext()) {
			FormField ff = (FormField) ir3.next();
			list.add(ff.getName());
        %>
        <span><input type="checkbox" id="<%=ff.getName()%>" name="<%=ff.getName()%>" title="<%=ff.getTitle()%>" checked="<%=checked%>" /><%=ff.getTitle()%></span>
        <%  
        }
	}
	
}else{
	while (ir3.hasNext()) {
		FormField ff = (FormField) ir3.next();
	
		String checked = "";
		for (i=0; i<len; i++) {
			String fieldName = fields[i];
			if (fieldName.equals(ff.getName())) {
				checked = "checked";
				break;
			}
		}
		%>
		<span><input type="checkbox" id="<%=ff.getName()%>" name="<%=ff.getName()%>" title="<%=ff.getTitle()%>" <%=checked%> />&nbsp;<%=ff.getTitle()%></span>
		<%
    }
}
%>
	<span><input type="button" class="btn" value="确定" onclick="checkFields()" />&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" class="btn" value="取消" onclick="cancel()" /></span>
    </div>
</div>
<script type="text/javascript">
var Msg = {}; //declare this or modify line 1 of core.js
var gd;
var moduleCols;

$("#trigger").powerFloat({
	eventType: "click",
	targetMode: null,
	targetAttr: "src",
	// position: "1-4", // 显示于下方，默认上方
	container: $("#customContainer")
});

function checkField(obj) {
	if (obj.checked) {
		var isFound = false;
		// 检查是否已有含有此列，如果没有则添加
		$.each(gd.config.cols, function(n, c){
			if(c.name == obj.name) {
				isFound = true;
				return;
			}
		});
		if (!isFound) {
			gd.remove();
			$('#viewBox').html('');
			$('#viewBox').append('<div class="view grid expGrid"></div>');

			var a = JSON.parse("{\"field\":\"" + obj.name + "\", \"title\":\"" + obj.getAttribute("title") + "\", \"width\":150, \"name\":\"" + obj.getAttribute("name") + "\", \"link\":\"#\"}");
			moduleCols.push(a);
			
			macGrid('grid', moduleCols);

			setCols();
		}
	}
	else {
		// 检查是否已有含有此列，如果有则删除
		var k = -1;
		$.each(gd.config.cols, function(n, c){
			if(c.name == obj.name) {		
				k = n;
				return;
			}
		});
		
		if (k==-1)
			return;
		
		gd.remove();
		$('#viewBox').html('');
		$('#viewBox').append('<div class="view grid expGrid"></div>');
		
		moduleCols.splice(k, 1); //删除指定子对象，参数：开始位置,删除个数

		macGrid('grid', moduleCols);
		
		setCols();		
	}
}

function checkFields() {
	$("#targetBox").find("input").each(function(){
		var obj = $(this);
		if (obj.attr("checked")=="checked") {
			var isFound = false;
			// 检查是否已有含有此列，如果没有则添加
			$.each(gd.config.cols, function(n, c){
				if(c.name == obj.attr("name")) {
					isFound = true;
					return;
				}
			});
			if (!isFound) {
				gd.remove();
				$('#viewBox').html('');
				$('#viewBox').append('<div class="view grid expGrid"></div>');
	
				var a = JSON.parse("{\"field\":\"" + obj.attr("name") + "\", \"title\":\"" + obj.attr("title") + "\", \"width\":150, \"name\":\"" + obj.attr("name") + "\", \"link\":\"#\"}");
				moduleCols.push(a);
				
				macGrid('grid', moduleCols);
	
				//setCols();
			}
		}
		else {
			// 检查是否已有含有此列，如果有则删除
			var k = -1;
			$.each(gd.config.cols, function(n, c){
				if(c.name == obj.attr("name")) {		
					k = n;
					return;
				}
			});
			
			if (k==-1)
				return;
			
			gd.remove();
			$('#viewBox').html('');
			$('#viewBox').append('<div class="view grid expGrid"></div>');
			
			moduleCols.splice(k, 1); //删除指定子对象，参数：开始位置,删除个数
	
			macGrid('grid', moduleCols);
			
			//setCols();		
		}
	});
	setCols();
}
 
</script>
<%
JSONArray jsonAry = new JSONArray();
for (i=0; i<len; i++) {
	String fieldName = fields[i];
	String fieldNameRaw = fieldName;
	String title = "";
	if (fieldName.equals("cws_creator")) {
		title = "创建者";
	}
	else if (fieldName.equals("ID")) {
		title = "ID";
	}
	else if (fieldName.equals("cws_progress")) {
		title = "进度";
	}
	else if (fieldName.equals("cws_status")) {
		title = "状态";
	}
	else if (fieldName.equals("cws_flag")) {
		title = "冲抵状态";
	}	
	else {
		if (fieldName.startsWith("main")) {
			String[] ary = StrUtil.split(fieldName, ":");
			fieldName = fieldName.substring(5);
			if (ary.length > 1) {
				FormDb mainFormDb = fm.getFormDb(ary[1]);
				title = mainFormDb.getName() + "：" + mainFormDb.getFieldTitle(ary[2]);
			}
		}
		else if (fieldName.startsWith("other")) {
			String[] ary = StrUtil.split(fieldName, ":");
			if (fieldName.length()>6) {
				fieldName = fieldName.substring(6);
			}
			if (ary.length<5) {
				title = "<font color='red'>格式非法</font>";
			}
			else {
				FormDb otherFormDb = fm.getFormDb(ary[2]);
				if (ary.length>=5)
					title = otherFormDb.getName() + "：" + otherFormDb.getFieldTitle(ary[4]);
				
				if (ary.length>=8) {
					FormDb oFormDb = fm.getFormDb(ary[5]);
					title += "：" + oFormDb.getFieldTitle(ary[7]);
				}
			}
		}
		else {
			title = fd.getFieldTitle(fieldName);
		}
	}

	// field: 'subject', title : 'Subject', width: 140
	JSONObject json = new JSONObject();
	json.put("field", fieldName);
	json.put("title", title);
	json.put("width", StrUtil.toInt(fieldsWidth[i].equals("#")?"":fieldsWidth[i], 150));
	if (fieldsLink != null){
		   json.put("link", (fieldsLink[i]!=null && fieldsLink[i].equals(""))?"#":fieldsLink[i]);
	}else{
		json.put("link", "#");
	}
    jsonAry.put(json);
}
%>
<link href="../js/mac/css/default.css" type="text/css" rel="stylesheet" />
<script src="../js/mac/core.js"></script>
<script src="../js/mac/mousewheel.js"></script>
<script src="../js/mac/pager.js"></script>
<script src="../js/mac/grid.js"></script>
<script src="../js/json2.js"></script>
<script type="text/javascript">
moduleCols = <%=jsonAry%>;
$(function(){
	macGrid('grid', moduleCols);
	<%
	if (list != null){
		for(String name : list){
	%>
			checkField($('#<%=name%>')[0]);
	<%
	 	}
	}
	%>
});

function macGrid(gridName, moduleCols) {
	gd = $('.view');
	gd.mac('grid', {
		cols : moduleCols,
		loader: {
			url: '/javascript/grid/data.jsp',
			params: { pageNo: 1, pageSize: 50 },
			autoLoad: false
		},
		pagerLength: 10
	});
}

$(function() {
	$('.btn-ok').click(function() {
		add();
	});
});

function add() {
	var data = $('#form1').serialize();
	data += "&cols=" + encodeURI(JSON.stringify(gd.config.cols));
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath()%>/visual/addExport.do",
		data: data,
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",		
		dataType: "html",
		beforeSend: function(XMLHttpRequest) {
			$('body').showLoading();
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re==null)
				re = data;
			if (re.ret=="1") {
				window.location.href = "module_export_list.do?code=<%=code%>&formCode=<%=formCode%>";
			}		
			else {
				jAlert(re.msg, "提示");
			}
		},
		complete: function(XMLHttpRequest, status){
			$('body').hideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});	
}

function cancel() {
	$.powerFloat.hide();
}

function selRoles() {
	var objCode = o("roleCodes");
	var objDesc = o("roleDescs");
	openWin('../role_multi_sel.jsp?roleCodes=' + objCode.value + '&unitCode=<%=StrUtil.UrlEncode(privilege.getUserUnitCode(request))%>', 526, 435);
}

function setRoles(roles, descs) {
	var objCode = o("roleCodes");
	var objDesc = o("roleDescs");	
	objCode.value = roles;
	objDesc.value = descs;
}
</script>
<br/>
</body>
</html>