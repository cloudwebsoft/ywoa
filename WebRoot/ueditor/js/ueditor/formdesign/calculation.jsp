<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="org.json.*" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%
String formCode = ParamUtil.get(request, "formCode");
if(formCode.equals("")){
	String op = ParamUtil.get(request, "op");
	if(op.equals("showAddColumn")){
	String content = ParamUtil.get(request, "content");
	JSONObject json = new JSONObject();
	FormParser formParser = new FormParser(content);
	Vector v = formParser.getFields();
	String strToghter = "";
	for (int i = 0; i < v.size(); i++) {
	   FormField ff = (FormField)v.get(i);
	   int fieldType = ff.getFieldType();
	   if(fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG || fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_PRICE || ff.getType().equals(FormField.TYPE_DATE)){
	   if((i+1) == v.size()){
	   	   strToghter += ff.getName()+":"+ff.getTitle();
	   }else{
	      strToghter += ff.getName()+":"+ff.getTitle()+",";
	   }
	  	
	  }
	}
	if(strToghter.equals("")){
		json.put("ret","0");
		json.put("msg",strToghter);
		out.print(json);
	}else{
		json.put("ret","1");
		json.put("msg",strToghter);
		out.print(json);
	}
	return;
  }
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>计算控件</title>
    
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1" >
    <link rel="stylesheet" href="bootstrap/css/bootstrap.css">
    <link rel="stylesheet" href="calculator.css">
    <!-- <link rel="stylesheet" type="text/css" href="../../../../jCalculator/css/style.css" media="screen">    
    <link rel="stylesheet" type="text/css" href="../../../../jCalculator/css/jcalculator.css" media="screen"> -->
    <!--[if lte IE 6]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-ie6.css">
    <![endif]-->
    <!--[if lte IE 7]>
    <link rel="stylesheet" type="text/css" href="bootstrap/css/ie.css">
    <![endif]-->
    <link rel="stylesheet" href="leipi.style.css">
    
    <script type="text/javascript" src="../dialogs/internal.js"></script>
    <script src="../../../../js/jquery.js"></script>
	
    <script type="text/javascript">
	function createElement(type, name)
	{     
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
	function getContextPath(){ 
	 var pathName = document.location.pathname; 
	 var index = pathName.substr(1).indexOf("/"); 
	 var result = pathName.substr(0,index+1); 
	 return result; 
	} 
    </script>
</head>

<body>
<div class="content" style="overflow:auto">
    <table class="table table-bordered table-striped table-hover">
     <tr>
       <th><span>控件字段&nbsp;<span class="label label-important">*</span></span></th>
       <th><span>控件名称&nbsp;</span><span class="label label-important">*</span></th>
     </tr>
     <tr>
       <th><input type="text" id="orgname" placeholder="必填项"></th>
       <th><input type="text" id="orgtitle" placeholder="必填项"></th>
     </tr>
     <tr>
        <th><span>算式&nbsp;<span class="label label-important">*</span></span></th>
        <th><span>数据类型</span> </th>
    </tr>
    <tr>
        <td>
        	<input type="text" id="orgFormula" placeholder="必填项"  readonly>
        	<input type="hidden" name="fhType" id="fhType" value=""/>
        </td>
        <td>
        	<span id="spanFieldTypeDesc">双精度型</span>
        </td>
    </tr>
     <tr>
        <th><span>小数点后位数</span></th>
        <th><span>四舍五入</span> </th>
    </tr>
    <tr>
        <td>
        	<input type="text" id="orgDigit" value="2">
        </td>
        <td>
        	<input id="isRoundTo5" name="isRoundTo5" type="checkbox" value="0"/>
        </td>
    </tr>
    <tr>
         <th><span>必填项</span></th>
        <th><span>只读</span></th>
    </tr>
    <tr>
        <td>
            <input id="canNull" name="canNull" type="checkbox" value="0" checked />   
        </td>
        <td>
        <input id="isReadOnly" name="isReadOnly" type="checkbox" value="1" />
    </td>
    </tr>
    <tr style="display:none">
        <th><span>&nbsp;&nbsp;&nbsp;&nbsp;长&nbsp;&nbsp;X&nbsp;&nbsp;宽&nbsp;&nbsp;&nbsp;&&nbsp;&nbsp;&nbsp;字体大小</span> </th>
        <th><span>可见性</span> </th>
    </tr>
    <tr style="display:none">
        <td>
            <input id="orgwidth" type="text" value="90" class="input-small span1" placeholder="auto"/>
            X
            <input id="orgheight" type="text" value="" class="input-small span1" placeholder="auto"/>
            &
            <input id="orgfontsize" type="text"  value="" class="input-small span1" placeholder="auto"/> px

        </td>
        <td>
            <label class="checkbox inline"><input id="orghide" type="checkbox"/> 隐藏 </label>
        </td>
    </tr>
    <tr style="display:none">
      <th></th>
      <th><span>长度/大小</span></th>
    </tr>
    <tr style="display:none">
      <td></td>
      <td>
<select id="minT" name="minT" style="width:60px">
        <option value="d=">>=</option>
        <option value="d">></option>
        <option value="=">=</option>
        </select>
        <input id="minV" name="minV" type="text" style="width:40px">
        <select id="maxT" name="maxT" style="width:60px">
          <option value="x="><=</option>
          <option value="x"><</option>
        </select>
	<input id="maxV" name="maxV" type="text" style="width:40px">      
            <select id="orgalign" style="display:none">
                <option value="left" >左对齐</option>
                <option value="center">居中对齐</option>
                <option value="right">右对齐</option>
            </select>      
      </td>
    </tr>
    </table>
</div>
<div class="jcalculator_wrap">
	<div id="showColumn" class="jcalculator" style="display:none;" >
		<div id="jcalculator">
			<span id="C">C</span>
			<!-- <span id="sum">sum</span>-->
			<span id="(">(</span>
			<span id=")">)</span> 
			<span id="+">+</span>
			<span id="-">-</span>
			<span id="*">*</span>
			<span id=",">,</span>
			<span id="subDate">subDate</span>
			<span id="addDate">addDate</span>
			<span id="/">/</span>
			<span id="9">9</span>
		    <span id="8">8</span>
		    <span id="7">7</span>
		    <span id="6">6</span>
		    <span id="5">5</span>
		    <span id="4">4</span>
		    <span id="3">3</span>
		    <span id="2">2</span>
		    <span id="1">1</span>
		    <span id="0">0</span>
		    <span id=".">.</span>
		</div>
	    <div class="jcalculator_1"  style="overflow-y:auto;overflow-x:hidden;">
	    	<div id="jcalculator_1">
	    		<%
	    		if(!formCode.equals("")){
	    			MacroCtlMgr mm = new MacroCtlMgr();	    		
	    			FormDb fd = new FormDb();
					fd = fd.getFormDb(formCode);
					String content = fd.getContent();
					FormParser formParser = new FormParser(content);
					Vector v = formParser.getFields();
	    			for (int i = 0; i < v.size(); i++) {
					   FormField ff = (FormField)v.get(i);
					   int fieldType = ff.getFieldType();
					   if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG || fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_PRICE || ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)){
				%>
						<span id="<%=ff.getName() %>"><%=ff.getTitle() %></span><br/>
				<%      
					  }
					  
						if (ff.getType().equals(FormField.TYPE_MACRO)) {
							// System.out.println(getClass() + " ff.getMacroType()=" + ff.getMacroType());
					        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());		
							// System.out.println(getClass() + " mu.getNestType()=" + mu.getNestType());
							if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
								String nestFormCode = ff.getDefaultValue();
								try {
									String defaultVal;
									if (mu.getNestType()==MacroCtlUnit.NEST_DETAIL_LIST) {
										defaultVal = StrUtil.decodeJSON(ff.getDescription());				
									}
									else {
										String desc = ff.getDefaultValueRaw();
										if ("".equals(desc)) {
											desc = ff.getDescription();
										}
										defaultVal = StrUtil.decodeJSON(desc);
									}
									// 20131123 fgf 添加
									JSONObject json = new JSONObject(defaultVal);
									nestFormCode = json.getString("destForm");
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
								}
								
								FormDb nestfd = new FormDb();
								nestfd = nestfd.getFormDb(nestFormCode);
								
								ModuleSetupDb msd = new ModuleSetupDb();
								msd = msd.getModuleSetupDbOrInit(nestFormCode);
								
								// System.out.println(getClass() + " nestFormCode=" + nestFormCode);
								
								String[] fields = msd.getColAry(false, "list_field");
								String listField = "," + StrUtil.getNullStr(StringUtils.join(fields, ",")) + ",";
								
								Iterator ir2 = nestfd.getFields().iterator();
								while (ir2.hasNext()) {
									FormField ff2 = (FormField)ir2.next();
									// 判断是否在模块中已设置为显示于列表中
									if (true || listField.indexOf("," + ff2.getName() + ",")!=-1) {
					   				   fieldType = ff2.getFieldType();
									   if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG || fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_PRICE ){
								%>
										<span id="sum(nest.<%=ff2.getName() %>)"><%=ff2.getTitle() %>(嵌套表)</span><br/>
								<%      
									  }
									
									}
								}
								// break;
							}
							else if (mu.getIFormMacroCtl() instanceof SQLCtl) {
								%>
								<span id="<%=ff.getName() %>"><%=ff.getTitle() %></span><br/>								
								<%
							}
						}					  
					}
				}
	    		 %>
	    	</div>
	    </div>
	</div>
</div>
<script type="text/javascript">
var oNode = null,thePlugins = 'calculation';
window.onload = function() {

    if( UE.plugins[thePlugins].editdom ){
        oNode = UE.plugins[thePlugins].editdom;
		
		var gValue=oNode.getAttribute('value');
		var gFormula = oNode.getAttribute('formula');
		if (gValue)
			gValue = gValue.replace(/&quot;/g,"\"");
		if (gFormula)
			gFormula = gFormula.replace(/&quot;/g,"\"");
		var gName=oNode.getAttribute('name').replace(/&quot;/g,"\"");
		var gTitle=oNode.getAttribute('title').replace(/&quot;/g,"\"");
		var gHidden=oNode.getAttribute('orghide');
		var gFontSize=oNode.getAttribute('orgfontsize');
		var gAlign=oNode.getAttribute('orgalign');
		var gWidth=oNode.getAttribute('orgwidth');
		var gHeight=oNode.getAttribute('orgheight');
		
		var gCanNull = oNode.getAttribute("canNull");
		var gMinT = oNode.getAttribute("minT");
		var gMinV = oNode.getAttribute("minV");
		var gMaxT = oNode.getAttribute("maxT");
		var gMaxV = oNode.getAttribute("maxV");
		var gDigit = oNode.getAttribute("digit");
		var gRoundTo5 = oNode.getAttribute("isRoundTo5");
		
		var isReadOnly = oNode.getAttribute("readonly");
		if (isReadOnly) {
			$G('isReadOnly').checked = true;
		}
		else {
			$G('isReadOnly').checked = false;
		}
		
		gValue = gValue==null ? '' : gValue;
        gTitle = gTitle==null ? '' : gTitle;
        gFormula = gFormula==null ? '' : gFormula;
        $G('orgname').value = gName;
        $G('orgtitle').value = gTitle;
        $G('orgFormula').value=gFormula;
        if (gHidden == '1') {
            $G('orghide').checked = true;
        }
        $G('orgname').setAttribute("readonly", true);
        $G('orgfontsize').value = gFontSize;
        $G('orgwidth').value = gWidth;
        $G('orgheight').value = gHeight;
        $G('orgalign').value = gAlign;
        $G('orgDigit').value = gDigit;
        if (gRoundTo5 == 1){
        	$G('isRoundTo5').checked = true;
        }
        if (gCanNull == 0)
        {
        	 $G('canNull').checked = true;
        }
        else{
        	 $G('canNull').checked = false;
        }
    }
    
    $("#orgFormula").click(function() {
    	$("#showColumn").css("display","block");
    	if("<%=formCode%>" == ""){
    	$.ajax({
				type: "post",
				url: "calculation.jsp",
				data : {
					op:"showAddColumn",
					content: UE.getEditor("myFormDesign").getContent()
		        },
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
				},
				success: function(data, status){
					data = $.parseJSON(data);
					if (data.ret=="1") {
						$("#jcalculator_1").html("");
						var columnArr = data.msg.split(",");
						for(var i=0;i<columnArr.length;i++){
							var name = columnArr[i].split(":")[0];
							var title = columnArr[i].split(":")[1];
							$("#jcalculator_1").append("<span id='"+name+"'>"+title+"</span><br/>");
						}
					}
					
				},
				complete: function(XMLHttpRequest, status){
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					//alert(XMLHttpRequest.responseText);
				}
			});
    	}
    });
     
    $(document).bind("click", function(e) {  
	    e = e || window.event; 
	    var dom =  e.srcElement|| e.target; 
	    if(dom.id != "orgFormula"){
	    	if(dom.parentNode.id !="jcalculator_1" && dom.id!='jcalculator_1' && dom.id!="showColumn" && dom.parentNode.id!= "jcalculator" && dom.parentNode.id !="showColumn" && document.getElementById("showColumn").style.display == "block") 
		    { 
		      document.getElementById("showColumn").style.display = "none"; 
		    } 
	    }
   }); 
   
   var orgFormulaVal = $("#orgFormula").val();
   var fh = $("#fhType").val();
   $("span").live('click',function(){
   		var code = $(this).attr("id"); // 当前输入的字符
   		orgFormulaVal = $("#orgFormula").val();
   		if (orgFormulaVal.indexOf("addDate")==0) {
   			$('#spanFieldTypeDesc').html("日期型");
   		}
   		else {
   			$('#spanFieldTypeDesc').html("双精度型");
   		}
   		fh = $("#fhType").val(); // 上一次输入的字符
   		
   		if(isNaN(code)){//如果不是数字的时候
   			if(code == "sum") {
	          	if(fh != undefined && fh != ""){
	          		if(fh != "+" && fh != "-" && fh!="*" && fh!="/"){
						return;
					}
	          	}
	         }
   			if(orgFormulaVal != ""){
        		if(fh != undefined && fh != ""){//至少点击输入一次
					if(code == fh){ //如果两次输的一样的时候，返回
						return;
					}
					if(!isNaN(fh)){
						if(!isNaN(code)){
							$("#orgFormula").val(orgFormulaVal+code);
						}else{
							if(code != "+" && code != "-" && code!="*" && code!="/" && code!="C" && code != "." && code != ")"){
								return;
							}
						}
					}else{
						if(fh != "+" && fh != "-" && fh!="*" && fh!="/" && fh!="C" && fh != "(" && fh != ")" && fh != "," && fh != "."){
							if(code != "+" && code != "-" && code!="*" && code!="/" && code!="C" && code != "(" && code != ")" && code !=","){
								return;
							}
						}
						
						if(fh == "+" || fh == "-" || fh=="*" || fh=="/"){//如果两次都是输入符号
							if(code == "+" || code == "-" || code=="*" || code=="/" ){
								return;
							}
						}
					}
				 }else{
				 	if(code == "C"){
					 	$("#fhType").val("");
					 	$("#orgFormula").val("");
					 	return;
					}
				 
				 	if(code != "+" && code != "-" && code!="*" && code!="/" ){
						return;
					}
				 }
        	}else{
        		if(fh != undefined && fh != ""){//至少点击输入一次
					if(code == fh){ //如果两次输的一样的时候，返回
						return;
					}
					
					if(fh == "+" || fh == "-" || fh=="*" || fh=="/"){//如果两次都是输入符号
						if(code == "+" || code == "-" || code=="*" || code=="/" ){
							return;
						}
					}
					
					if(fh != "+" && fh != "-" && fh!="*" && fh!="/" && fh!="C"  && fh != "(" && fh != ")" && fh !="," && fh != "."){
						if(code != "+" && code != "-" && code!="*" && code!="/" && code!="C" && code != "(" && code != ")" && code !=","){
							return;
						}
					}
				 }else{
				 	if(code == "+" || code == "-" || code=="*" || code=="/" ){
						return;
					}
				 }
        	}

			 if(code == "C"){
			 	$("#fhType").val("");
			 	$("#orgFormula").val("");
			 	return;
			 }
           }
          
          
          if(!isNaN(code)){//是数字的时候
          	if(code == "sum"){
	          	if(fh != undefined && fh != ""){
	          		if(fh != "+" && fh != "-" && fh!="*" && fh!="/" && fh != "."){
						return;
					}
	          	}
	        }
          
        	if(orgFormulaVal != ""){
        		
        		if(fh != undefined && fh != ""){//至少点击输入一次
					
					if(!isNaN(fh)){
						if(!isNaN(code)){
							$("#orgFormula").val(orgFormulaVal+code);
						}else{
							if(code != "+" && code != "-" && code!="*" && code!="/" && code!="C"){
								return;
							}
						}
					}else{
						if(fh != "+" && fh != "-" && fh!="*" && fh!="/" && fh!="C" && fh != "(" && fh != ")" && fh != "," && fh != "."){
							if(code != "+" && code != "-" && code!="*" && code!="/" && code!="C" && code != "(" && code != ")" && code != ","){
								return;
							}
						}
					}
				 }else{
				 	if(code != "+" && code != "-" && code!="*" && code!="/" ){
						return;
					}
				 }
        	}
        
        }
          
          if(fh == "("){
          	//if(code.indexOf("nest.") == -1){
          	//	return;
          	//}
          
          	if(code == "+" || code == "-" || code=="*" || code=="/" ){
				return;
			}
          }
          
          if(fh == ")"){
          	if(code != "+" && code != "-" && code!="*" && code!="/" ){
				return;
			}
          }
          if(fh == "sum"){
          	if(code != "("){
				return;
			}
          }

           if(code != "C"){
           		$("#fhType").val(code);
           }else{
           		$("#orgFormula").val("");
           		$("#fhType").val("");
           }
        
        
        $("#orgFormula").val(orgFormulaVal+code);
   });
    //$('#orgFormula').calculator();
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
    var gFormula = $G('orgFormula').value.replace(/\"/g,"&quot;");
	 if(gFormula==''){
        alert('请输入控件算式');
        return false;
    }

	var lowerFormula = gFormula.toLowerCase();
	if(lowerFormula.indexOf(",") != -1){
		if(lowerFormula.indexOf("subdate") == -1 && lowerFormula.indexOf("adddate") == -1){
			 alert('时间计算公式输入错误!');
		     return false;
		}
	}
	
	if(lowerFormula.indexOf("subdate") != -1){
		if(lowerFormula.indexOf(",") == -1){
			 alert('时间计算公式输入出错!');
		     return false;
		}
	}
	    
	if(lowerFormula.indexOf("adddate") != -1){
		if(lowerFormula.indexOf(",") == -1){
			 alert('时间计算公式输入出错!');
		     return false;
		}
	}	    
    
	var gFontSize=$G('orgfontsize').value;
	var gAlign=$G('orgalign').value;
	var gWidth=$G('orgwidth').value;
	var gHeight=$G('orgheight').value;

	var gCanNull = $G('canNull').checked?0:1;
	var gMinT=$G('minT').value;
	var gMinV=$G('minV').value;
	var gMaxT=$G('maxT').value;
	var gMaxV=$G('maxV').value;
	var gDigit = $G('orgDigit').value;
	var gRoundTo5 = $G('isRoundTo5').checked?1:0;
	
    
    if( !oNode ) {
        try {
        	debugger;
            oNode = createElement('input', gName);
            oNode.setAttribute('title',gTitle);
            oNode.setAttribute('value',gFormula);
            oNode.setAttribute('cwsPlugins',thePlugins);
            if ( $G('orghide').checked ) {
                oNode.setAttribute('orghide',1);
            } else {
                oNode.setAttribute('orghide',0);
            }
            if( gFontSize != '' ) {
                //style += 'font-size:' + gFontSize + 'px;';
                oNode.setAttribute('orgfontsize',gFontSize );
            }
            if( gAlign != '' ) {
                //style += 'text-align:' + gAlign + ';';
                oNode.setAttribute('orgalign',gAlign );
            }
            if( gWidth != '' ) {
                oNode.style.width = gWidth+ 'px';
                //style += 'width:' + gWidth + 'px;';
                oNode.setAttribute('orgwidth',gWidth );
            }
            if( gHeight != '' ) {
                oNode.style.height = gHeight+ 'px';
                //style += 'height:' + gHeight + 'px;';
                oNode.setAttribute('orgheight',gHeight );
            }

			oNode.setAttribute("canNull", gCanNull);
			oNode.setAttribute("minT", gMinT);
			oNode.setAttribute("minV", gMinV);
			oNode.setAttribute("maxT", gMaxT);
			oNode.setAttribute("maxV", gMaxV);
			oNode.setAttribute("formula", gFormula);
			oNode.setAttribute("kind", "CALCULATOR");
			if (gFormula.indexOf("addDate")==0) {
				oNode.setAttribute("fieldtype", "<%=FormField.FIELD_TYPE_DATE%>");
			}
			else {
				oNode.setAttribute("fieldtype", "<%=FormField.FIELD_TYPE_DOUBLE%>");
			}
			oNode.setAttribute("digit", gDigit);
			oNode.setAttribute("isRoundTo5", gRoundTo5);
			
			if ($G('isReadOnly').checked) {
				oNode.setAttribute("readonly", "readonly");
			}
			else {
				oNode.removeAttribute("readonly");
			}			
				
	        editor.execCommand('insertHtml',oNode.outerHTML);
        } catch (e) {
            try {
                editor.execCommand('error');
            } catch ( e ) {
                alert('控件异常！');
            }
            
            return false;
        }
    } else {
    	oNode.setAttribute('name', gName);
        oNode.setAttribute('title', gTitle);
        //oNode.setAttribute('value', $G('orgvalue').value);
        if( $G('orghide').checked ) {
            oNode.setAttribute('orghide', 1);
        } else {
            oNode.setAttribute('orghide', 0);
        }
        if( gFontSize != '' ) {
            //oNode.style.fontSize = gFontSize+ 'px';
            oNode.setAttribute('orgfontsize',gFontSize );
        }else{
            //oNode.style.fontSize = '';
            oNode.setAttribute('orgfontsize', '');
        }
        if( gAlign != '' ) {
            oNode.setAttribute('orgalign',gAlign );
        }else{
            oNode.setAttribute('orgalign', '');
        }
        if( gWidth != '' ) {
            //oNode.style.width = gWidth+ 'px';
            oNode.setAttribute('orgwidth',gWidth );
        }else{
            //oNode.style.width = '';
            oNode.setAttribute('orgwidth', '');
        }
        if( gHeight != '' ) {
            //oNode.style.height = gHeight+ 'px';
            oNode.setAttribute('orgheight',gHeight );
        }else{
            //oNode.style.height = '';
            oNode.setAttribute('orgheight', '');
        }
		
		oNode.setAttribute("canNull", gCanNull);		
        oNode.setAttribute("value", gFormula);
		oNode.setAttribute("formula", gFormula);
		oNode.setAttribute("digit", gDigit);
		oNode.setAttribute("isRoundTo5", gRoundTo5);

		if ($G('isReadOnly').checked) {
			oNode.setAttribute("readonly", "readonly");
		}
		else {
			oNode.removeAttribute("readonly");
		}
					
        delete UE.plugins[thePlugins].editdom;
    }
};
</script>
</body>
</html>