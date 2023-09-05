<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "cn.js.fan.util.*"%>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");

	String fieldName = ParamUtil.get(request, "fieldName");
%>

/*
window.document.onclick = function() {
	var controls = document.getElementsByTagName("input");
	for (var i=0; i < controls.length; i++) {
		if (controls[i].type=="text" && controls[i].getAttribute("lowerFieldCode")!=null) {
			var lowerCtls = document.getElementsByName(controls[i].getAttribute("lowerFieldCode"));
			var lv = lowerCtls[0].value;
			if (lv!=""){
				controls[i].value = cmycurd(lv);
			}
		}
	}
}
*/

function onExchangeRatechange(event, obj, moneyTypeField, moneyValueField){
	var srcElement = event.srcElement || event.target;
	//alert(srcElement.value);	
		
	/*		
	var fobj;
	try {
		fobj = eval("f_" + moneyTypeField);
	}
	catch (e) {
		// alert("e=" + e.message);
	}
	if (fobj) {
		// alert(field + "-here");
		fobj.destroy();
	}
	*/		
		
	obj.value = exchange(moneyTypeField, moneyValueField);

}

function exchange(moneyTypeField,moneyValueField ){
	var bz = $("select[name='"+moneyTypeField+"']").val();
	var num = $("input[name='"+moneyValueField+"']").val();
	ajaxExchange(bz,num);
}

function ajaxExchange(bz, num) {
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath()%>/flow/macro/macro_exchange_rate_ajax.jsp",
		data : {
			op: "getMoney",
        	bz : bz,
			num : num
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("<%=fieldName%>").value = re.money_result;
			}
			else {
				alert(re.msg);
			}
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});	
}	

// 在flow_dispose_free.jsp中，用jQuery(document).ready就会报 缺少对象
// 原因是flow_doc_archive_content.jsp中缺少引入jquery.js造成的
$(document).ready(function(){
	$("input[moneyValueField]").each(function(){											 
		var obj = $(this);
		var moneyTypeField = $(this).attr("moneyTypeField");
		var moneyValueField = $(this).attr("moneyValueField");
		/*
		// IE9下无效，但是IE8则可行
		$("input[name='" + lowerCtl + "']").bind("propertychange", function() { 
			  obj.val(cmycurd($(this).val()));
		});
		*/

		var o = $("select[name='" + moneyTypeField + "']")[0];
		var o2 = $("input[name='" + moneyValueField + "']")[0];

		if (isIE()){
			o.attachEvent("onpropertychange", function(event){ onExchangeRatechange(event, obj[0], moneyTypeField, moneyValueField); }, true); // onpropertychange
			o2.attachEvent("onpropertychange", function(event){ onExchangeRatechange(event, obj[0], moneyTypeField, moneyValueField); }, true);
		}else{
			o.addEventListener("input", function(event){ onExchangeRatechange(event, obj[0], moneyTypeField, moneyValueField); }, false); 
			o2.addEventListener("input", function(event){ onExchangeRatechange(event, obj[0], moneyTypeField, moneyValueField); }, false); 
		}
		
	});
});
