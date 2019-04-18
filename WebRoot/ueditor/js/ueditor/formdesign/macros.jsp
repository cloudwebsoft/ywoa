<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%
String formCode = ParamUtil.get(request, "formCode");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>宏控件</title>
	<%@ include file="../../../../inc/nocache.jsp"%>    
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1" >
    <meta name="generator" content="www.leipi.org" />
    <link rel="stylesheet" href="bootstrap/css/bootstrap.css">
    <!--[if lte IE 6]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-ie6.css">
    <![endif]-->
    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/ie.css">
    <![endif]-->
    <link rel="stylesheet" href="leipi.style.css">
    <script type="text/javascript" src="../dialogs/internal.js"></script>
    <script src="../../../../inc/map.js"></script>
	<script type="text/javascript" src="../../../../js/jquery1.7.2.min.js"></script>
	<link href="../../../../js/select2/select2.css" rel="stylesheet" />
	<script src="../../../../js/select2/select2.js"></script>    
    <script type="text/javascript">
	function createElement(type, name) {     
		var element = null;     
		try {        
			element = document.createElement('<'+type+' name="'+name+'">');     
		} catch (e) {}   
		if(element==null) {     
			element = document.createElement(type);     
			element.name = name;     
		} 
		return element;     
	}
	
	var map = new Map();
	<%
	MacroCtlMgr mm = new MacroCtlMgr();
	Vector v = mm.getAllMacroUnit();
	Iterator ir = v.iterator();
	while (ir.hasNext()) {
		MacroCtlUnit mu = (MacroCtlUnit)ir.next();	
		%>
		map.put('<%=mu.getCode()%>', <%=mu.getVersion()%>);
		<%
	}
	%>    
    </script>
</head>
<body>
<div class="content">
    <table class="table table-bordered table-striped table-hover">
        <tr>
            <th>控件字段&nbsp;<span class="label label-important">*</span></th>
            <th><span>控件名称</span><span class="label label-important">*</span></th>
        </tr>
        <tr>
            <td>
                <input id="orgname" type="text" placeholder="必填项"/>
            </td>
            <td><input type="text" id="orgtitle" placeholder="必填项"></td>
        </tr>
        <tr>
          <th>类型</th>
          <th>必填项 </th>
        </tr>
        <tr>
          <td>
          <span style="display:none">
         	 类型<select name="orgtype" id="orgtype" class="span7">
            <option value="0">字符串型</option>
            <option value="1">文本型</option>
            <option value="2">整型</option>
            <option value="3">长整型</option>
            <option value="4">布尔型</option>
            <option value="5">浮点型</option>
            <option value="6">双精度型</option>
            <option value="7">日期型</option>
            <option value="8">日期时间型</option>
            <option value="9">价格型</option>
          </select>
          </span>
          <select name="macroType" id="macroType" onChange="onMacroTypeChange(this)">
            <%
			ir = v.iterator();			
			while (ir.hasNext()) {
				MacroCtlUnit mu = (MacroCtlUnit)ir.next();
				if (!mu.isDisplay())
					continue;	
				out.print("<option value=\"" + mu.getCode() + "\">" + mu.getName() + "</option>");
			}%>
          </select>      
          </td>
          <td><input id="canNull" name="canNull" type="checkbox" value="0" /></td>
        </tr>
        <tr>
          <th>只读</th>
          <th> </th>
        </tr> 
        <tr>
          <td>
			<input id="isReadOnly" name="isReadOnly" type="checkbox" value="1" />          
          </td>
          <td></td>
        </tr>       
        <tr>
            <td colspan="2">
             <a id="edit" title="编辑" class="btn btn-primary" onclick="editMap()">编辑</a>
            </td>
        </tr>
    <tr id="desc" >
    <td colspan="2">
     <table class="table table-hover table-condensed" id="options_table">
        <tr>
            <th>默认值</th>
            <th>描述</th>
        </tr>
        <tr>
            <td>
            <textarea type="text" id="orgvalue" placeholder="无则不填" style="width:260px; height:100px;"></textarea>
            <span style="display:none">
            控件样式宽
<input id="orgwidth" type="text" value="150" class="input-small span1" placeholder="auto"/> px
                &nbsp;&nbsp;
                字体大小 <input id="orgfontsize" type="text" value="" class="input-small span1" placeholder="auto"/> px
                </span></td>
            <td>
			<textarea type="text" id="description" placeholder="无则不填" style="width:260px; height:100px;"></textarea>            
            <span style="display:none">
                <label class="checkbox"> 可见性
                <input id="orghide" type="checkbox"> 隐藏 </label>
            </span>
            </td>
        </tr>
        </table>
           
        </td>
    </tr>
    </table>
</div>
<script type="text/javascript">
var oNode = null,thePlugins = 'macros';
window.onload = function() {
    if( UE.plugins[thePlugins].editdom ) {
        oNode = UE.plugins[thePlugins].editdom;
        var gName=oNode.getAttribute('name').replace(/&quot;/g,"\"");
		var gTitle=oNode.getAttribute('title').replace(/&quot;/g,"\"");

        var gHidden=oNode.getAttribute('orghide'),gFontSize=oNode.getAttribute('orgfontsize'),gWidth=oNode.getAttribute('orgwidth');

		var gType=oNode.getAttribute('fieldType');
		var gCanNull = oNode.getAttribute("canNull");
        
		gTitle = gTitle==null ? '' : gTitle;
        $G('orgname').value = gName;
        $G('orgtitle').value = gTitle;
        
		
        //if( oNode.tagName == 'INPUT' ) {}
        if(oNode.getAttribute('orghide')=='1'){
            $G('orghide').checked = true;
        }
        $G('orgtype').value    = gType; 
        $G('orgwidth').value = gWidth;
        $G('orgfontsize').value = gFontSize;
		
		$G('orgname').setAttribute("readonly", true);
		
		$G('macroType').value = oNode.getAttribute("macroType");
		$G('macroType').disabled = true;
        if (oNode.getAttribute("macroType") == "macro_current_user" || oNode.getAttribute("macroType") == "macro_image" || oNode.getAttribute("macroType") == "nest_table" || oNode.getAttribute("macroType") == "nest_sheet" || oNode.getAttribute("macroType") == "macro_detaillist_ctl" || oNode.getAttribute("macroType") == "module_field_select") {
            $G('desc').style.display = "none";
            $G("edit").style.display = '';
        } else {
            $G("edit").style.display = 'none';
        }
		
		var isReadOnly = oNode.getAttribute("readonly");
		if (isReadOnly) {
			$G('isReadOnly').checked = true;
		}
		else {
			$G('isReadOnly').checked = false;
		}		

		if ($G('macroType').value == 'macro_sql') {
			$G('orgvalue').value = decodeURI(oNode.getAttribute("macroDefaultValue"));
		} else {
			$G('orgvalue').value = oNode.getAttribute("macroDefaultValue");
		}
		if (oNode.getAttribute("description"))
			$G('description').value = oNode.getAttribute("description");

		if (gCanNull==0) {
			$G('canNull').checked = true;
		}
		else {
			$G('canNull').checked = false;
		}	
		
    }else{
        $G("edit").style.display = 'none';
    }
    
    $('#macroType').select2();
}

dialog.oncancel = function () {
    if( UE.plugins[thePlugins].editdom ) {
        delete UE.plugins[thePlugins].editdom;
    }
};
dialog.onok = function (){
    var gName=$G('orgname').value.replace(/\"/g,"&quot;");
    if(gName==''){
        alert('请输入控件字段');
        return false;
    }
	var gTitle=$G('orgtitle').value.replace(/\"/g,"&quot;");
    if(gTitle==''){
        alert('请输入控件名称');
        return false;
    }	

	var gType=$G('orgtype').value;
	var gCanNull = $G('canNull').checked?0:1;
	
	var gFontSize=$G('orgfontsize').value,gWidth=$G('orgwidth').value;
	
    if( !oNode ) {
        try {
			/*
			if ( $G('orgtype').value.indexOf('sys_list')>0 ) {
				oNode = document.createElement("select");
				var objOption = new Option('{macros}', '');
				oNode.options[oNode.options.length] = objOption;
			} else {
				//input
			}*/
			oNode = createElement('input',gName);
			oNode.setAttribute('title',gTitle);
			var mactoTypeText = $("#macroType  option:selected").text();
			oNode.setAttribute("value", "宏控件：" + mactoTypeText);
			oNode.setAttribute("canNull", gCanNull);
			if ($('#macroType').val() == 'macro_sql') {
				oNode.setAttribute("macroDefaultValue", encodeURI(orgvalue.value));
			} else {
				oNode.setAttribute("macroDefaultValue", orgvalue.value);
			}
			oNode.setAttribute("description", description.value);
			oNode.setAttribute("macroType", macroType.value);
			oNode.setAttribute('kind','macro'); 
			oNode.setAttribute('cwsPlugins',thePlugins);
			oNode.setAttribute('fieldType',gType); 
			
			if( $G('orghide').checked ) {
				oNode.setAttribute('orghide', '1' ) ;
			} else {
				oNode.setAttribute('orghide', '0' ) ;
			}
			if( gFontSize != '' ) {
				oNode.style.fontSize = gFontSize + 'px';
				oNode.setAttribute('orgfontsize',gFontSize );
			}
			if( gWidth != '' ) {
				// 如果不是默认值150，则赋予宽度
				if (gWidth!=150) {
					oNode.style.width = gWidth + 'px';
				}
				oNode.setAttribute('orgwidth',gWidth );
			}
			
			if ($G('isReadOnly').checked) {
				oNode.setAttribute("readonly", "readonly");
			}
			else {
				oNode.removeAttribute("readonly");
			}
						
			editor.execCommand('insertHtml',oNode.outerHTML);
			return true;
		} catch ( e ) {
			try {
				editor.execCommand('error');
			} catch ( e ) {
				alert('控件异常！');
			}
			return false;
		}
    } else {
		oNode.setAttribute('title',gTitle);
		oNode.setAttribute("fieldType", gType);
		oNode.setAttribute("value", "宏控件：" + $("#macroType  option:selected").text());
		oNode.setAttribute("macroType", macroType.value);
		if (macroType.value == 'macro_sql') {
			oNode.setAttribute("macroDefaultValue", encodeURI(orgvalue.value));
		} else {
			oNode.setAttribute("macroDefaultValue", orgvalue.value);
		}
		oNode.setAttribute("canNull", gCanNull);
		oNode.setAttribute('kind','macro');	
		oNode.setAttribute('description',$G('description').value);   			

		if ($G('isReadOnly').checked) {
			oNode.setAttribute("readonly", "readonly");
		}
		else {
			oNode.removeAttribute("readonly");
		}
		
        delete UE.plugins[thePlugins].editdom;
		return true;
    }
};

function setSequence(id, name) {
	if (orgtitle.value=="")
		orgtitle.value = name;
				
	if (map.get(macroType.value).value>1) {
		description.value = id;
	}
	else {
		orgvalue.value = id;
	}
	//下拉框change后，清空选择内容
	if (id==""){
	   description.value = id;
	}

	var mType = $G("macroType").value;
    if (mType=="module_field_select" || mType=="macro_image") {
        // 置默认值为空，清空老版的默认值，如macro_image的默认值为200,200，否则在生成表单字段时会赋予默认值
        orgvalue.value = '';
    }
}

function openWin(url,width,height) {
  	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
}

function onMacroTypeChange(obj) {
	if(obj.options[obj.selectedIndex].value=='macro_flow_number' || obj.options[obj.selectedIndex].value=='macro_flow_sequence'){
		$G("canNull").disabled = false;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
		openWin('../../../../flow/flow_sequence_sel.jsp', 300, 40);
	}
	else if (obj.options[obj.selectedIndex].value=='macro_flow_select') {
		$G("canNull").disabled = false;
		$G("edit").style.display = 'none';
		setSequence("","");
		$G("desc").style.display = '';
		openWin('../../../../flow/basic_select_sel.jsp', 640, 280);
	}
	else if (obj.options[obj.selectedIndex].value=='nest_table') {
		$G('canNull').checked = false;
		$G("canNull").disabled = true;
		$G("edit").style.display = 'none';
		$G("desc").style.display = 'none';
		setSequence("","");
		// openWin('../../../../visual/module_sel.jsp', 300, 40);
		openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_table&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}
	else if (obj.options[obj.selectedIndex].value=='nest_form') {
		$G('canNull').checked = false;
		$G("canNull").disabled = true;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
		openWin('../../../../visual/module_sel.jsp', 300, 40);
	}
	else if (obj.options[obj.selectedIndex].value=='nest_sheet') {
		$G('canNull').checked = false;
		$G("canNull").disabled = true;
		$G("edit").style.display = '';
		$G("desc").style.display = 'none';
		setSequence("","");
		openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}
	
	else if (obj.options[obj.selectedIndex].value=='macro_detaillist_ctl') {
		$G('canNull').checked = false;
		$G("canNull").disabled = true;
		$G("edit").style.display = '';
		$G("desc").style.display = 'none';
		setSequence("","");
		openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}
	else if (obj.options[obj.selectedIndex].value=='module_field_select') {
		$G("canNull").disabled = false;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
		openWin('../../../../visual/module_field_sel.jsp?openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}
    else if (obj.options[obj.selectedIndex].value=='macro_image') {
        $G("canNull").disabled = false;
        $G("edit").style.display = 'none';
        $G("desc").style.display = '';
        setSequence("","");
        openWin('image_ctl_prop.jsp', 300, 200);
    }
	else if (obj.options[obj.selectedIndex].value=='role_user_select') {
		$G("canNull").disabled = false;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
		openWin('../../../../flow/role_sel.jsp', 300, 40);
	}	
	else if (obj.options[obj.selectedIndex].value=='macro_form_data_map') {
		$G("canNull").disabled = false;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
		openWin('../../../../flow/form_data_map.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}	
	else if (obj.options[obj.selectedIndex].value=='macro_queryfield_select') {
		$G("canNull").disabled = false;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
		openWin('../../../../flow/macro/macro_query_field_sel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 300);
	}
	else if (obj.options[obj.selectedIndex].value=='macro_formula_ctl') {
		$G('canNull').checked = false;
		$G("canNull").disabled = true;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
		openWin('../../../../visual/formula_sel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 300);
	}
	else if (obj.options[obj.selectedIndex].value=='macro_current_user') {
        $G("canNull").disabled = false;
        $G("edit").style.display = 'none';
        $G("desc").style.display = '';
        setSequence("","");
        openWin('../../../../flow/macro/curent_user_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
    }			
	else{
		$G("canNull").disabled = false;
		$G("edit").style.display = 'none';
		$G("desc").style.display = '';
		setSequence("","");
	}
}
function editMap(){
    if ($G("macroType").value=="macro_image") {
        openWin('image_ctl_prop.jsp', 400, 230);
        return;
    }
    if ($G("macroType").value=="macro_current_user") {
        openWin('../../../../flow/macro/curent_user_ctl_prop.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 330);
        return;
    }
    

    var jsonStr = ""; 
    if (map.get(macroType.value).value>1) {
        jsonStr = description.value ;
        if (jsonStr=="") {
        	// 向下兼容，因为ModuleFieldSelectCtl改为version为2之前设计的控件，配置信息仍保存在macrodefaultvalue中
        	jsonStr = orgvalue.value;
        }
    }
    else {
        jsonStr = orgvalue.value;
    }
    if (jsonStr != ""){
	   jsonStr = decodeJSON(jsonStr);
	   //含有queryId 为修改查询，若sourceForm不为空，则为表单，否则为嵌套表选择
	   if (jsonStr.indexOf("queryId") != -1){
	        openPostWindow('../../../../visual/module_field_sel_query_nest.jsp','nest_sheet','<%=StrUtil.UrlEncode(formCode)%>',jsonStr,"嵌套表域选择");
	        //openWin('../../../../visual/module_field_sel_query_nest.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&params=' + jsonStr, 800, 600);
	   }
	   else if (jsonStr.indexOf("idField") != -1 && jsonStr.indexOf("showField")!=-1){
	        openPostWindow('../../../../visual/module_field_sel.jsp','nest_table','<%=StrUtil.UrlEncode(formCode)%>',jsonStr,"模块表单域选择");
	        //openWin('../../../../visual/module_field_sel.jsp&params=' + jsonStr, 900, 700);
	   }	   
	   else if (jsonStr.indexOf("sourceForm") != -1){
	   		// console.log("jsonString=" + jsonStr);
	       	var jsonStrs = eval('('+jsonStr+')');		   
		   	if (jsonStrs.sourceForm != null && jsonStrs.sourceForm != ""){
		    	openPostWindow('../../../../visual/module_field_sel_nest.jsp','nest_sheet','<%=StrUtil.UrlEncode(formCode)%>',jsonStr,"嵌套表域选择");
		       	//openWin('../../../../visual/module_field_sel_nest.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&params=' + jsonStr, 800, 600);
		   	}else{
				var isTab = 0;
				if (jsonStrs.isTab) {
					isTab = 1;
				}				
		       	openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_sheet&isTab=' + isTab + '&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>&editFlag=true&params=' + jsonStrs.destForm + "&oldRelateCode=" + jsonStrs.destForm+"&jsonStr=" + encodeURI(jsonStr), 800, 600);
		   	}
	   }
   }else{
   		if ($G("macroType").value=="module_field_select") {
			openWin('../../../../visual/module_field_sel.jsp?openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);   		
   		}
   		else {
        	openWin('../../../../visual/module_field_sel_nest1.jsp?nestType=nest_table&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
        }
   }
   
}

// 对字符串中的引号进行解码
function decodeJSON(jsonString) {
    jsonString = jsonString.replace(/%dq/gi, '"');
    jsonString = jsonString.replace(/%sq/gi, "'");
	
	// 不能解码回车换行，否则会导致过滤条件为脚本型时，如果有回车换行，会导致点击编辑按钮时，eval报错
	// jsonString = jsonString.replace(/%rn/g, "\r\n")    
	// jsonString = jsonString.replace(/%n/g, "\n")    
    return jsonString;
}
function openPostWindow(url,nestType,openerFormCode, data, name){    
    var tempForm = document.createElement("form");    
    tempForm.id="tempForm1";    
    tempForm.method="post";    
    tempForm.action=url;    
    tempForm.target=name;    
 
    var paramHideInput = document.createElement("input");    
    paramHideInput.type="hidden";    
    paramHideInput.name= "params" ;
    paramHideInput.value= data;  
    tempForm.appendChild(paramHideInput); 
    
    var nestTypeHideInput = document.createElement("input");    
    nestTypeHideInput.type="hidden";    
    nestTypeHideInput.name= "nestType";  
    nestTypeHideInput.value= nestType;  
    tempForm.appendChild(nestTypeHideInput); 
    
    var openerFormCodeHideInput = document.createElement("input");    
    openerFormCodeHideInput.type="hidden";    
    openerFormCodeHideInput.name= "openerFormCode";
    openerFormCodeHideInput.value= openerFormCode;  
    tempForm.appendChild(openerFormCodeHideInput);
    
    var editFlagHideInput = document.createElement("input");    
    editFlagHideInput.type="hidden";    
    editFlagHideInput.name= "editFlag"; 
    editFlagHideInput.value= "edit";  
    tempForm.appendChild(editFlagHideInput);
        
    //tempForm.attachEvent("onsubmit",function(){ openWindow(name); });  
    $("#tempForm1").bind("onsubmit",function(){ openWindow(name); });
    document.body.appendChild(tempForm);    
    //tempForm.fireEvent("onsubmit");  
    tempForm.submit();  
    document.body.removeChild(tempForm);  
  
}  
  
function openWindow(name){    
     window.open('about:blank',name,'height=900, width=700, top=0, left=0, toolbar=yes, menubar=no, scrollbars=yes, resizable=yes,location=yes, status=yes');     
}    

</script>
</body>
</html>
