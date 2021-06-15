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
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

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

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(code);

long id = ParamUtil.getLong(request, "id", -1);
ModuleExportTemplateDb metd = new ModuleExportTemplateDb();
metd = metd.getModuleExportTemplateDb(id);

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
        <input id="name" name="name" value="<%=metd.getString("name")%>" />
		&nbsp;&nbsp;
		<input id="is_serial_no" name="is_serial_no" value="1" type="checkbox" <%=metd.getInt("is_serial_no")==1?"checked":""%> />
		显示序号
      </td>
    </tr>
    <tr>
      <td align="center">标题栏文字</td>
      <td colspan="2" align="left">
        <input id="bar_name" name="bar_name" value="<%=metd.getString("bar_name")%>" />
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
      <input id="bar_font_size" name="bar_font_size" value="<%=metd.getString("bar_font_size")%>" size="5" />px
		行高
	  <input id="bar_line_height" name="bar_line_height" value="<%=metd.getInt("bar_line_height")%>" size=5 />px		 
         &nbsp;&nbsp;&nbsp;&nbsp;
        <div id="bar_color_show" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: <%=metd.getString("bar_back_color")%>;"></div>
         	背景色
		<input name="bar_back_color" id="bar_back_color" type="hidden" value="<%=metd.getString("bar_back_color")%>"/>
		<script>
		$('#bar_color_show').bigColorpicker(function(el,color) {
			$(el).css("background-color",color);
			$('#bar_back_color').val(color);
			$('#previewBar').css("background-color", color);			
		});
		</script>		
		<div id="barForeColor" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: <%=metd.getString("bar_fore_color")%>;"></div>	
		字体颜色
		<input name="bar_fore_color" id="bar_fore_color" type="hidden" value="<%=metd.getString("bar_fore_color")%>"/>
		<script>
		$('#barForeColor').bigColorpicker(function(el,color){
			$(el).css("background-color",color);
			$('#bar_fore_color').val(color);
			$('#previewBar').css("color", color);
		});
		$(function() {
			$('#bar_font_family').val('<%=metd.getString("bar_font_family")%>');
			$('#bar_is_bold').prop("checked", <%=metd.getInt("bar_is_bold")==1?true:false%>);
			
			$('#previewBar').css("font-family", "<%=metd.getString("bar_font_family")%>");			
			$('#previewBar').css("font-size", "<%=metd.getString("bar_font_size")%>px");			
			$('#previewBar').css("font-weight", "<%=metd.getInt("bar_is_bold")==1?"bold":"normal"%>");			
			$('#previewBar').css("line-height", "<%=metd.getString("bar_line_height")%>px");			
			$('#previewBar').css("height", "<%=metd.getString("bar_line_height")%>px");	
			$('#previewBar').css("background-color", "<%=metd.getString("bar_back_color")%>");	
			$('#previewBar').css("color", "<%=metd.getString("bar_fore_color")%>");	
			
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
      	<input id="bar_is_bold" name="bar_is_bold" value="1" type="checkbox" />
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
      <td width="14%" align="center">表头格式</td>
      <td width="51%" align="left">
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
      <input id="font_size" name="font_size" value="<%=metd.getString("font_size")%>" size="5" />px
      行高
	  <input id="line_height" name="line_height" value="<%=metd.getInt("line_height")%>" size=5 />px
      <script>
		var tname = new LiveValidation('name');
		tname.add( Validate.Presence );
 		var line_height = new LiveValidation('line_height');
		line_height.add( Validate.Presence );
		line_height.add( Validate.Numericality );
 		var font_size = new LiveValidation('font_size');
		font_size.add( Validate.Presence );
		font_size.add( Validate.Numericality );
      </script>      
		 &nbsp;&nbsp;&nbsp;&nbsp;
        <div id="color_show" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: <%=metd.getString("back_color")%>;"></div>
         背景色
		<input name="back_color" id="back_color" type="hidden" value="<%=metd.getString("back_color")%>"/>
		<script>
		$('#color_show').bigColorpicker(function(el,color){
			$(el).css("background-color",color);
			$('#back_color').val(color);
			$('#preview').css("background-color", color);			
		});
		</script>		
		<div id="foreColor" style="border: 1px solid rgb(204, 204, 204); cursor:pointer; border-image: none; width: 16px; height: 16px; vertical-align: middle; display: inline-block; background-color: <%=metd.getString("fore_color")%>;"></div>	
		字体颜色
		<input name="fore_color" id="fore_color" type="hidden" value="<%=metd.getString("fore_color")%>"/>
		<script>
		$('#foreColor').bigColorpicker(function(el,color){
			$(el).css("background-color",color);
			$('#fore_color').val(color);
			$('#preview').css("color", color);
		});
		$(function() {
			$('#font_family').val('<%=metd.getString("font_family")%>');
			$('#is_bold').prop("checked", <%=metd.getInt("is_bold")==1?true:false%>);
			
			$('#preview').css("font-family", "<%=metd.getString("font_family")%>");			
			$('#preview').css("font-size", "<%=metd.getString("font_size")%>px");			
			$('#preview').css("font-weight", "<%=metd.getInt("is_bold")==1?"bold":"normal"%>");			
			$('#preview').css("line-height", "<%=metd.getString("line_height")%>px");			
			$('#preview').css("height", "<%=metd.getString("line_height")%>px");	
			$('#preview').css("background-color", "<%=metd.getString("back_color")%>");	
			$('#preview').css("color", "<%=metd.getString("fore_color")%>");	
			
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
      	<input id="is_bold" name="is_bold" value="1" type="checkbox" />
     	 加粗
      </td>
      <td width="35%" align="left">
        <div id="preview" style="width:200px; float:left; text-align:center; padding:3px 0px; height:30px; font-weight:bold; border:1px solid #ccc">表头预览</div>      
      </td>
    </tr>
    <tr>
      <td align="center">角色</td>
      <td colspan="2" align="left">
		<%
	  	String roleCodes = "", descs = "";
		String[] roleAry = StrUtil.split(StrUtil.getNullStr(metd.getString("roles")), ",");
		if (roleAry!=null) {
			for (int k=0; k<roleAry.length; k++) {
				RoleDb rd = new RoleDb();
				rd = rd.getRoleDb(roleAry[k]);
				String roleCode = rd.getCode();
				String desc = rd.getDesc();
				if (roleCodes.equals(""))
					roleCodes += roleCode;
				else
					roleCodes += "," + roleCode;
				if (descs.equals(""))
					descs += desc;
				else
					descs += "," + desc;		
			}	 
		}		
		%>      
		<textarea title="为空则表示角色不限，均可以使用此模板" id="roleDescs" name="roleDescs" style="width:40%; height:60px" readonly="readonly"><%=descs %></textarea>
        <input id="roleCodes" name="roleCodes" type="hidden" value="<%=roleCodes %>" />
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
          <input name="id" value="<%=id%>" type="hidden"/>
          <input name="code" value="<%=code%>" type="hidden"/>
          <input name="formCode" value="<%=formCode%>" type="hidden"/>        
      </td>
    </tr>
</table>
</form>

<div id="targetBox" class="shadow target_box" style="display:none">
	<div class="target_list">
<%
	String cols = metd.getString("cols");
	JSONArray ary = new JSONArray(cols);
	
	int i;
	Vector v3 = fd.getFields();
	Iterator ir3 = v3.iterator();

	while (ir3.hasNext()) {
		FormField ff = (FormField) ir3.next();
	
		String checked = "";
		for (i=0; i<ary.length(); i++) {
			String fieldName = (String)ary.getJSONObject(i).get("field");
			if (fieldName.equals(ff.getName())) {
				checked = "checked";
				break;
			}
		}
		%>
		<span><input type="checkbox" onclick="checkField(this)" id="<%=ff.getName()%>" name="<%=ff.getName()%>" title="<%=ff.getTitle()%>" <%=checked%> />&nbsp;<%=ff.getTitle()%></span>
		<%
    }
			
	String[] fields = msd.getColAry(true, "list_field");
	int fieldsLen = fields.length;
	// 增加映射型字段
	for (i=0; i<fieldsLen; i++) {
		String fieldName = fields[i];
		
		String title = "";
		boolean isMap = false;
		if (fieldName.startsWith("main:")) {
			String[] aryField = StrUtil.split(fieldName, ":");
			if (aryField.length > 1) {
				FormDb mainFormDb = fm.getFormDb(aryField[1]);
				title = mainFormDb.getName() + "：" + mainFormDb.getFieldTitle(aryField[2]);
				isMap = true;
			}
		}
		else if (fieldName.startsWith("other:")) {
			String[] aryField = StrUtil.split(fieldName, ":");
			if (aryField.length<5) {
				title = "<font color='red'>格式非法</font>";
			}
			else {
				FormDb otherFormDb = fm.getFormDb(aryField[2]);
				if (aryField.length>=5) {
					title = otherFormDb.getName() + "：" + otherFormDb.getFieldTitle(aryField[4]);
					isMap = true;
				}
				
				if (aryField.length>=8) {
					FormDb oFormDb = fm.getFormDb(aryField[5]);
					title += "：" + oFormDb.getFieldTitle(aryField[7]);
					isMap = true;
				}
			}
		}
		if (isMap) {
            String checked = "";
            for (int j=0; j<ary.length(); j++) {
                String fieldNameAry = (String)ary.getJSONObject(j).get("field");
                if (fieldNameAry.equals(fieldName)) {
                    checked = "checked";
                    break;
                }
            }
		%>
			<span><input type="checkbox" id="<%=fieldName%>" name="<%=fieldName%>" onclick="checkField(this)" title="<%=title%>" <%=checked%> />&nbsp;<%=title%></span>
		<%
		}
	}
%>
	<span><input type="button" class="btn" value="关闭" onclick="cancel()" /></span>
    </div>
</div>
<%
	List list = new ArrayList();
	boolean isDel = false;
	// 找出已不存在的列，并自动清除
	for (i = 0; i < ary.length(); i++) {
		String fieldName = (String) ary.getJSONObject(i).get("field");
		if (fieldName.toLowerCase().equals("id")) {
			continue;
		}
		else if (fieldName.equals("flowId")) {
			continue;
		}
		if (fieldName.equals("cws_creator")) {
			continue;
		}
		else if (fieldName.equals("ID")) {
			continue;
		}
		else if (fieldName.equals("cws_progress")) {
			continue;
		}
		else if (fieldName.equals("cws_status")) {
			continue;
		}
		else if (fieldName.equals("cws_flag")) {
			continue;
		}
		else if (fieldName.equals("colOperate")) {
			continue;
		}
		else if (fieldName.equals("cws_create_date")) {
			continue;
		}
		else if (fieldName.equals("flow_begin_date")) {
			continue;
		}
		else if (fieldName.equals("flow_end_date")) {
			continue;
		}
		else if (fieldName.startsWith("main:") || fieldName.startsWith("other:")) {
			continue;
		}
		
		boolean isFound = false;
		ir3 = v3.iterator();
		while (ir3.hasNext()) {
			FormField ff = (FormField) ir3.next();
			if (fieldName.equals(ff.getName())) {
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			isDel = true;
%>
<div style="padding-left: 30px"><%=ary.getJSONObject(i).get("title")%>(<%=fieldName%>) 不存在，已删除</div>
<%
		}
		else {
			list.add(ary.getJSONObject(i));
		}
	}
	if (isDel) {
		ary = new JSONArray(list);
		metd.set("cols", ary.toString());
		metd.save();
	}
	
	// 因为:号在jquery选择器中被使用，所以需换成#
	for (i = 0; i < ary.length(); i++) {
		String name = (String) ary.getJSONObject(i).get("name");
		name = name.replaceAll(":", "#");
		ary.getJSONObject(i).put("name", name);
	}
%>
<script type="text/javascript">
var Msg = {}; //declare this or modify line 1 of core.js
var gd;
var moduleCols;

$("#trigger").powerFloat({
	eventType: "click",
	targetMode: null,
	targetAttr: "src",
	position: "1-4", // 显示于下方，默认上方
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

			var objName = obj.getAttribute("name");
			objName = objName.replaceAll(":", "#");
			var a = JSON.parse("{\"field\":\"" + obj.name + "\", \"title\":\"" + obj.getAttribute("title") + "\", \"width\":150, \"name\":\"" + objName + "\", \"link\":\"#\"}");
			moduleCols.push(a);
			
			macGrid('grid', moduleCols);
		}
	}
	else {
		// 检查是否已有含有此列，如果有则删除
		var k = -1;
		$.each(gd.config.cols, function(n, c){
			if(c.field == obj.name) {
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
	}
}
</script>
<link href="../js/mac/css/default.css" type="text/css" rel="stylesheet" />
<script src="../js/mac/core.js"></script>
<script src="../js/mac/mousewheel.js"></script>
<script src="../js/mac/pager.js"></script>
<script src="../js/mac/grid.js"></script>
<script src="../js/json2.js"></script>
<script type="text/javascript">
moduleCols = <%=ary%>;
$(function(){
	macGrid('grid', moduleCols);
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
		edit();
	});
});

function edit() {
	var data = $('#form1').serialize();
	data += "&cols=" + encodeURI(JSON.stringify(gd.config.cols));
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath()%>/visual/editExport.do",
		data: data,
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",		
		dataType: "html",
		beforeSend: function(XMLHttpRequest) {
			$('body').showLoading();
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			jAlert(re.msg, "提示");
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