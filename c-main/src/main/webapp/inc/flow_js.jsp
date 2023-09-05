<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@ page import="com.redmoon.oa.flow.WorkflowMgr" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%
/*
- 功能描述：流程中的js处理，宏控件中的脚本调用（如：打开窗口）一般放在此文件中
- 访问规则：include
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
*/

// 防漏洞：1; mode=block 启用XSS保护，并在检查到XSS攻击时，停止渲染页面
response.setHeader("X-XSS-Protection", "1; mode=block");
String rootpath = request.getContextPath();
Privilege privilege = new Privilege();
String unitCode = "";
if (!privilege.isValid(request, "notice")) {
	unitCode = privilege.getUserUnitCode(request);
}

String op = cn.js.fan.util.ParamUtil.get(request, "op");
JSONObject json = new JSONObject();
if ("user_department".equals(op)) {
	response.setContentType("text/html;charset=utf-8");
	String userName = cn.js.fan.util.ParamUtil.get(request,"user_name");
	com.redmoon.oa.dept.DeptUserDb du = new com.redmoon.oa.dept.DeptUserDb();
	java.util.Vector v = du.getDeptsOfUser(userName);
	if (v.size() <= 0) {
		json.put("ret", "0");
	} else {
		com.redmoon.oa.dept.DeptDb dd = (com.redmoon.oa.dept.DeptDb)v.get(0);
		String department_real = dd.getName();
		String department_id = dd.getCode();
		json.put("ret", "1");
		json.put("department_real", department_real);
		json.put("department_id", department_id);
	}
	out.print(json);
	return;
}
else if ("dept_name".equals(op)) {
	response.setContentType("text/html;charset=utf-8");
	String dept_code = cn.js.fan.util.ParamUtil.get(request,"dept_code");
	com.redmoon.oa.dept.DeptDb dd = new com.redmoon.oa.dept.DeptDb(dept_code);
	String dept_name = dd.getName();
	json.put("dept_name", dept_name);
	out.print(json);
	return;
}
else if ("show_customer".equals(op)) {
		response.setContentType("text/html;charset=utf-8");
	String linkmanName = StrUtil.getNullStr(cn.js.fan.util.ParamUtil.get(request,"linkmanName"));
	String id = StrUtil.getNullStr(cn.js.fan.util.ParamUtil.get(request,"id"));
	FormDb customerfd = new FormDb();
	customerfd = customerfd.getFormDb("sales_linkman");
	String customerId = "";
	if (customerfd !=null && customerfd.isLoaded()) {
  		FormDAO fdaoCustomer = new FormDAO();
  		fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toInt(id), customerfd);
  		if (fdaoCustomer != null && fdaoCustomer.isLoaded()) {
			customerId = fdaoCustomer.getFieldValue("customer");
		}
	}
  	FormDb linkfd = new FormDb();
	linkfd = linkfd.getFormDb("sales_customer");
	String linkName = "";
	if (linkfd !=null && linkfd.isLoaded()) {
  		FormDAO linkdaoCustomer = new FormDAO();
  		linkdaoCustomer = linkdaoCustomer.getFormDAO(StrUtil.toInt(customerId), linkfd);
  		if (linkdaoCustomer != null && linkdaoCustomer.isLoaded()) {
			linkName = linkdaoCustomer.getFieldValue("customer");
		}
	}
	json.put("customerId", customerId);
	json.put("linkName", linkName);	
	out.print(json);
	return;
}

response.setContentType("text/javascript;charset=utf-8");
// nest_sheet_add_relate.jsp、nest_sheet_edit_relate.jsp、module_add_relate.jsp、module_edit_relate.jsp这几个文件中传入了parentFormCode
String parentFormCode = ParamUtil.get(request, "parentFormCode");
%>
function openWinForFlowAccess(url,width,height) {
	var newwin = window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=250,left=350,width="+width+",height="+height);
}

// module_list_sel.jsp中替换掉宏控件，checkJs 为字段的校验脚本
function replaceValue(openerField, val, sourceValue, checkJs) {
	// consoleLog("openerField=" + openerField + " type=" + o(openerField).getAttribute("type") + " val=" + val + " sourceValue=" + sourceValue);
	// $(o(openerField)).parent().html(val); // 如果元素是放在td中，则td中其它的隐藏字段会被清掉
	$('#' + openerField + "_realshow").remove(); // 删除realshow元素，如表单域宏控件中的显示值
	$('#' + openerField + "_btn").remove(); // 删除btn元素，如表单域宏控件中的搜索按钮
	if (o(openerField)==null) {
		consoleLog("openerField:" + openerField + " is not found");
		return;
	}
	// 如果是文件控件
	if (o(openerField).getAttribute("type")=="file") {
        // 清除映射过来的文件信息
		$('#helper_' + openerField).remove();
		$('#' + openerField + '_mapped').remove();
		$(o(openerField)).before(val);
	}
	else {
		// 如果是图标控件
		if (o(openerField + "_wrapper")) {
			// 通过outerHTML方式，无法调用val中的js
			// $(o(openerField + "_wrapper")).prop('outerHTML', val);
			// $(o(openerField)).val(sourceValue);

			// 通过.html(val)方法，能够调用val中的js，但是会使其父节点下的其它孩子节点，如提示语句被清掉
			<%--
			var $p = $(o(openerField + "_wrapper")).parent();
			$p.html(val);
			var arr = new Array();
			arr[0] = sourceValue;
			$(o(openerField)).val(arr).trigger('change');
			--%>

			$(o(openerField + "_wrapper")).prop('outerHTML', '<span id="temp_' + openerField + '"></span>');
			var tmp = o('temp_' + openerField);
			var $tmp = $(tmp);
			$tmp.after(val);
			$tmp.remove();
			var arr = new Array();
			arr[0] = sourceValue;
			$(o(openerField)).val(arr).trigger('change');
		}
		else {
			$(o(openerField)).prop('outerHTML', val);
			$(o(openerField)).val(sourceValue);
		}
	}

	var frm = o("visualForm");
	if (frm==null) {
		frm = o("flowForm");
	}

	// console.log('openerField=' + openerField + ' checkJs=' + checkJs);
	// 删除原来的验证，否则会因为原验证中存储的对象不存在而导致验证失效
	var formObj = LiveValidationForm.getInstance(frm);
	if (formObj) {
		if (checkJs) {
			formObj.removeFieldByName(openerField);
			var script = $('<script>' + checkJs + '</script>');
			$('body').append(script);
		}
		else {
			// 删除原来的验证，因为似乎原来绑定的元素不存在了，但是校验还在
			formObj.removeFieldByName(openerField);
		}
	}
}

var inputObj;

function setIntpuObjValue(id, v) {
	inputObj.value = id;
	if(!v && v!='') {
		return;
	}
	// 在其父元素里面找对应的_realshow，因为在新的嵌套表格宏控件中会clone表格行，不同行之间存在有同名的元素
	var inputObjName = inputObj.name;
	var objRealShow = $(inputObj).parent().find("[id='" + inputObjName + "_realshow'],[name='" + inputObjName + "_realshow']")[0];
    if (objRealShow) {
    	if (typeof(objRealShow.value)=="string") {
			objRealShow.value = v;
            if(o("lxr")!=null && o("customer")!=null && o("customer_realshow")!=null){ // 在销售行动表单中使用
             $(function(){
	            	$.ajax({
					type:"get",
					url:"<%=rootpath %>/inc/flow_js.jsp",
					data:{"op":"show_customer", "linkmanName":v, "id":id},
					success:function(data,status){
						data = $.parseJSON(data);
						o("customer_realshow").value = data.linkName;
						o("customer").value = data.customerId;
					}
					});
	            });
	            }
	           
            if(o("dept_name") != null && o("dept_code") != null){
	            $(function(){
	            	$.ajax({
					type:"get",
					url:"<%=rootpath %>/inc/flow_js.jsp",
					data:{"op":"user_department", "user_name":id},
					success:function(data,status){
						data = $.parseJSON(data);
						if ($("#dept_name")!= null) {
							$("#dept_name").html(data.department_real);
							try {
								o("dept_code").value = data.department_id;
							} catch (err) {
								$("#dept_name").html(data.department_real + "<input id='dept_code' name='dept_code' type='hidden' value=" + data.department_id + ">");
							}
						}
					}
					});
	            })
            }
        }
        else {
			objRealShow.innerHTML = v;
        }
    }
}

$(function(){
	//查看某通知公告流程 进行颜色变化的显示
	if(o("t_color_show")!= null) {
		var clr = o("t_color_show").innerHTML;
		$(o("t_color_show")).prop("outerHTML", "<div style='width:30px; height:15px; float:left; background:" + clr + "'></div>");
	}
	var dept_code = $("#cws_span_dept_code").html();
})

// 用于多用户选择窗体的调用
function getMultiSelUserNames() {
	return inputObj.value;
}

function getMultiSelUserRealNames() {
	return o(inputObj.name + "_realshow").value;
}

// function addInputObjValue(v) {
//	 inputObj.value += v;
// }

function openWinSign(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/macro/macro_ctl_sign_win.jsp", 320, 100);
}

function openWinIdea(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/macro/macro_ctl_idea_win.jsp", 400, 200);
}

function openWinCustomerList(obj) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/sales/customer_list_sel.jsp", 640, 480);
}

function openWinLinkmanList(obj, customerId) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/sales/linkman_list_sel.jsp?customerId=" + customerId, 800, 600);
}

function openWinProviderList(obj) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/sales/provider_info_list_sel.jsp", 520, 480);
}

function openWinProductList(obj) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/sales/product_list_sel.jsp", 520, 480);
}

function openWinProductServiceList(obj) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/sales/product_service_list_sel.jsp", 520, 480);
}

function openWinModuleFieldList(obj, formCode, byFieldName, showFieldName, filter, openerFormCode, flowId, pageType) {
	filter = "";
	inputObj = obj
	var openerFieldName = obj.getAttribute('field');
	openWinForFlowAccess("<%=rootpath%>/visual/moduleListSelPage.do?formCode=" + formCode + "&byFieldName=" + byFieldName + "&showFieldName=" + showFieldName + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + openerFieldName + "&flowId=" + flowId + "&pageType=" + pageType + "&filter=" + filter, 1024, 768);
}

function openWinProjectList(obj) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/project/project_list_sel.jsp", 640, 480);
}

function openWinSalesChangeList(obj, customerId) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/sales/sales_chance_list_sel.jsp?customerId=" + customerId, 640, 480);
}

function openWinSalesOrderList(obj, customerId) {
	inputObj = obj
	openWinForFlowAccess("<%=rootpath%>/sales/sales_order_list_sel.jsp", 640, 480);
}

// 多部门选择窗体
function openWinDeptsSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/admin/organize/organize_dept_sel.jsp?deptCode="+obj.value, 800, 600, "yes");
}

// 多角色选择窗体
function openWinRoleMultiSel(obj) {
	inputObj = obj;
	openWin("<%=rootpath%>/roleMultilSelBack.do?roleCodes=" + obj.value, 800, 600);
}

// 四则运算拆分算式
function getSymbolsWithBracket(str) {
    // 去除空格
    str = str.replaceAll(" ", "");
    if (str.indexOf("+")==0)
        str = str.substring(1); // 去掉开头的+号
    var list = new Array();
    var curPos = 0;
    var prePos = 0;
    var k = 0;
    for (var i = 0; i < str.length; i++) {
        var s = str.charAt(i);
        if (s == '+' || s == '-' || s == '*' || s == '/' || s=='(' || s==')') {
            if (prePos < curPos) {
                list[k] = str.substring(prePos, curPos).trim();
                k++;
            }
            list[k] = "" + s;
            k++;
            prePos = curPos + 1;
        }
        curPos++;
    }
    if (prePos <= str.length - 1)
        list[k] = str.substring(prePos).trim();
    return list;
}
// 是否四则运算符
function isOperator(str) {
    if (str=="+" || str=="*" || str=="/" || str=="-" || str=="(" || str==")") {
        return true;
    }
    else
        return false;
}

function doCalculate(jqueryObj) {
    var formula = jqueryObj.attr('formula');
    var digit = jqueryObj.attr('digit');
    var isRoundTo5 = jqueryObj.attr('isRoundTo5');
	if (formula.toLowerCase().indexOf("subdate") != -1 || formula.toLowerCase().indexOf("adddate") != -1){
		// 对日期加减进行计算，有可能会出现 subDate(...) + 1 的情况，所以需要eval
		jqueryObj.val(eval(callFunc(formula)));
    	return;
    }
	var format = jqueryObj.attr('format');
	var valForInfinity = jqueryObj.attr('valForInfinity');
	var valForNaN = jqueryObj.attr('valForNaN');

    var ary = getSymbolsWithBracket(formula);

    for (var i=0; i < ary.length; i++) {
        if (!isOperator(ary[i])) {
            // ary[i]可能为0.2这样的系数
		    if (!isNumeric(ary[i])) {
            	var $obj = $("input[name='" + ary[i] + "']");
                if (!$obj[0]) {
                	$obj = $("select[name='" + ary[i] + "']");
                }
                if (!$obj[0]) {
                	$obj = $("#cws_textarea_" + ary[i]);
                }
				var v = $obj.val();
				// 去掉千分位逗号
				if (v) {
					v = v.replaceAll(',', '');
				}

				if (v=="")
					ary[i] = 0;
				else if (isNaN(v))
					ary[i] = 0;
				else
					ary[i] = "(" + v + ")";
			}
        }
    }
    formula = "";
    for (var i=0; i < ary.length; i++) {
        formula += ary[i];
    }
    try {
	    var calValue = parseFloat(eval(formula));
	    var strValue = calValue.toString();

	    if (isNaN(calValue)) {
			if (valForNaN!=null && valForNaN!='') {
				jqueryObj.val(valForNaN);
			}
			return;
	    }

		if (calValue == Infinity || calValue == -Infinity) {
			if (valForInfinity!=null && valForInfinity!='') {
				jqueryObj.val(valForInfinity);
			}
			return;
		}

	    // 判断是否四舍五入 1：是 0：否
	    if (isRoundTo5 != null && isRoundTo5 == 1){
	    	var digitNum = parseFloat(digit);
	    	if (!isNaN(digitNum)) {
				calValue = calValue.toFixed(digitNum);
			}
	    	
	    }
	    else if(isRoundTo5 != null && isRoundTo5 == 0){
	    	var digitNum = parseFloat(digit);
	    	if (!isNaN(digitNum)) {
				calValue = calValue.toFixed(digitNum + 1);
				calValue = changeTwoDecimal_f(calValue,digitNum);
			}
	    }
		// console.log('formula=' + formula + ' format=' + format + ' calValue=' + calValue);
		// 千分位处理
		if ("0" == format) {
			calValue = thousandth(calValue);
		}
   
    	jqueryObj.val(calValue);
    }
    catch (e) {}
}

function changeTwoDecimal_f(floatvar,digit) {
	var f_x = parseFloat(floatvar);
	if (isNaN(f_x))
	{
	alert('function:changeTwoDecimal->parameter error');
	return false;
	}
	//var f_x = Math.round(f_x*100)/100;
	var s_x = f_x.toString();
	var pos_decimal = s_x.indexOf('.');
	if (pos_decimal < 0)
	{
		pos_decimal = s_x.length;
		s_x += '.';
	}
	else
	{
		var subString = s_x.substr(pos_decimal+1);
		if (subString.length >= digit)
		{
			s_x= s_x.substr(0,pos_decimal+ 1 + digit)
		}
	
	}
	while (s_x.length <= pos_decimal + digit)
	{
		s_x += '0';
	}
    
    // 去掉当digit为0时，末尾多余的.
    if (s_x.lastIndexOf(".")==s_x.length-1) {
     	s_x = s_x.substring(0, s_x.length-1);
    }
	
	return s_x;
}

function initCalculator() {
 	$("input[kind='CALCULATOR']").each(function(){
        var calObj = $(this);
        if ($(this).attr('formula')) {
           var formula = $(this).attr('formula');
           var isSum = false;
           var regStr = /(sum\(([\w|\.]+)\))/gi;
           var mactches = formula.match(regStr)
           var len = 0;
           if (mactches) {
           		len = mactches.length;
                isSum = true;
           }
           if (isSum) {
           	   // 累加列 
               var field = RegExp.$2;
               if (field.indexOf("nest.")==0) {
               		var p = field.indexOf(".");
                    field = field.substring(p+1);
                    // 当数据列有更新时，更新计算控件的值，该部分在nest_table_view.jsp中实现
               }
           }
		   else {
		      if (formula.toLowerCase().indexOf("subdate") != -1 || formula.toLowerCase().indexOf("adddate") != -1){
                    // 对日期绑定事件
                    formula = initFuncFieldEvent(formula,calObj);
                }else{
	                var ary = getSymbolsWithBracket(formula);
	
	                for (var i=0; i < ary.length; i++) {
	                	// ary[i]可能为0.2这样的系数
	                    if (!isOperator(ary[i]) && !isNumeric(ary[i])) {
	                        /*
	                        // change在更改后，点击别处时，才会更新
	                        $("input[name='" + ary[i] + "']").change(function(){
	                              doCalculate(calObj);
	                        });
	                        */
	                        
	                        /* IE9不支持下列写法
	                        $("input[name='" + ary[i] + "']").bind("propertychange", function() { 
	                              doCalculate(calObj);
	                        });
	                        */
	                        var isSelect = false;
	                        var o = $("input[name='" + ary[i] + "']")[0];
							if(!o) {
	                        	o = $("select[name='" + ary[i] + "']")[0];
	                            if (!o) {
                                	// 因为字段可能为不可写状态，所以o可能为null，所以此处把提示注释掉，而且表单设计时是选择字段，所以字段名不会错
									// alert("计算控件" + calObj.attr("name") + "，算式" + formula + "中的字段：" + ary[i] + " 不存在！");
	                            }
	                            else {
	                            	isSelect = true; 
	                            }
							}
	                        if (o) {
	                        	bindEvent(o, calObj, isSelect);
	                        }
	                    }
	                }
                }
            }
        }
    });

    $("input[kind='CALCULATOR']").each(function(){
        if ($(this).attr('formula')) {
            doCalculate($(this));
        }
    });
}

function bindEvent(obj, calObj, isSelect) {
    if (true || isIE()) {
        if (true || isIE11) {
            if (isSelect) {
                obj.addEventListener("change", function(event){ doCalculate(calObj); }, false); 
            }
            else {                
                var oldValue = obj.value;                
                setInterval(function(){
                                if (oldValue != obj.value) {
                                    oldValue = obj.value;
                                    doCalculate(calObj);
                                }
                            },500);                 
            }
        }
        else {
        	// 如果控件关联多的话，IE8上面当关联的表单域获取输入焦点时会很卡，所以改为上面的绑定方式
            obj.attachEvent("onpropertychange", function(event){ doCalculate(calObj); }, true);
        }
    }
    else {
        if (isSelect) {
            obj.addEventListener("change", function(event){ doCalculate(calObj); }, false); 
        }
        else {
            obj.addEventListener("input", function(event){ doCalculate(calObj); }, false); 
        }
    }
}

// 对日期相减中涉及的字段的相关事件进行初始化
function initFuncFieldEvent(str, calObj) {
	// 时间相减方法subdate(d1, d2)
    var pat = /subdate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
    str.replace(pat, function(p1, date1, date2){
        var isSelect = false;
        var o = $("input[name='" + date1 + "']")[0];
        if(!o) {
            o = $("select[name='" + date1 + "']")[0];
            if (!o) {
                alert("计算控件算式" + p1 + "中的字段：" + date1 + " 不存在！");
            }
            else {
                isSelect = true; 
            }
        }    
		bindEvent(o, calObj, isSelect);
        
        o = $("input[name='" + date2 + "']")[0];
        if(!o) {
            o = $("select[name='" + date2 + "']")[0];
            if (!o) {
                alert("计算控件算式" + p1 + "中的字段：" + date2 + " 不存在！");
            }
            else {
                isSelect = true; 
            }
        }    
		bindEvent(o, calObj, isSelect);        
    });
    
	// 时间相加方法addDate(d1, d2)
    var pat = /adddate\(([a-z0-9_-]+),([0-9-]+)\)/ig;
    str.replace(pat, function(p1, date, days){
        var isSelect = false;
        var o = $("input[name='" + date + "']")[0];
        if(!o) {
            o = $("select[name='" + date + "']")[0];
            if (!o) {
                alert("计算控件算式" + p1 + "中的字段：" + date + " 不存在！");
            }
            else {
                isSelect = true; 
            }
        }    
		bindEvent(o, calObj, isSelect);      
    });    
    
    return str;
}

function callFunc(str) {
	var isDate = false;
	// 时间相减方法subdate(d1, d2)
    var pat = /subdate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
    str = str.replace(pat, function(p1, date1, date2){ 
        if (o(date1)==null) {
        	alert("字段" + date1 + "不存在！");
            return 0;
        }
        if (o(date2)==null) {
        	alert("字段" + date2 + "不存在！");
            return 0;
        }

		isDate = true;
        var mode = "day";
        if (o(date1).getAttribute("kind")=="DATE_TIME" || o(date2).getAttribute("kind")=="DATE_TIME") {
        	mode = "hour";
        }

        var v1 = o(date1).value;
        var v2 = o(date2).value;
        if (""==v1 || ""==v2)
        	return 0;
        else {
        	return subDate(v1, v2, mode);
        }
    });

	// 时间相加方法addDate(d1, d2)
    var pat = /adddate\(([a-z0-9_-]+),([a-z0-9_-]+)\)/ig;
    str = str.replace(pat, function(p1, date, days){ 
        if (o(date)==null) {
        	alert("字段" + date1 + "不存在！");
            return "";
        }
        if (o(days)==null) {
	        if (!isNumeric(days)) {
	        	alert(days + "不是数字！");
	            return "";
	        }
        }
        else {
        	days = o(days).value;
	        if (!isNumeric(days)) {
	        	alert("字段" + days + "不是数字！");
	            return "";
	        }        	
        }

		isDate = true;
        var v = o(date).value;        
        if (""==v)
        	return "";
        else {        
        	return addDate(v, days);
        }
    });
	if (isDate) {
		return str;
	}
	else {
		var v = eval(str);
		return v;
	}
}

function subDate(strDate1, strDate2, mode) {
    strDate1=strDate1.replace(/-/g,"/");
    strDate2=strDate2.replace(/-/g,"/");
    
    var date1 = Date.parse(strDate1);
    var date2 = Date.parse(strDate2); 

    var dt = Math.abs(date2 - date1);   //时间差的毫秒数   

	if (mode=="day") {
	    return parseInt((dt)/(24*60*60*1000));
    }
    else {     
	    var days = Math.floor((dt)/(24*60*60*1000));
        // 计算出小时数
        var leave = dt % (24*3600*1000)    //计算天数后剩余的毫秒数
        var hours=leave/(3600*1000) + days * 24;
		return parseInt(hours);
    }
}

function addDate(date, days) {
	var d = new Date(date); 
	days = parseInt(days);
	// console.log(d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate() + " days=" + days);
	
	d.setDate(d.getDate() + days); 
	var month = d.getMonth()+1; 
	var day = d.getDate(); 
	if(month<10) { 
		month = "0"+month; 
	} 
	if(day<10) { 
		day = "0"+day; 
	}
	var val = d.getFullYear()+"-"+month+"-"+day;
	return val; 
}

//callFunc(str);

// 基础数据宏控件当change时的默认调用
function onBasciCtlChange(ctlObj) {
}

try {
	$(document).ready(function(){
		try {
			// 因为某些页面中没有导入jquery.js
			initCalculator();
		}
		catch(e) {}
	});
}
catch (e) {}

function openWinUserSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/user_sel.jsp?isForm=true", 800, 600);
}

function openWinUserMultiSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/user_multi_sel.jsp?unitCode=<%=unitCode %>&isForm=true", 800, 600);
}

function openWinLocationSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/map/location_list_mine.jsp?isForm=true&op=sel", 800, 600);
}

function openWinWorkflowMineSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/flowListPage.do?displayMode=<%=WorkflowMgr.DISPLAY_MODE_ATTEND%>&action=sel", 1024, 768);
}

function openWinQueryFieldList(objName, openerFormCode, fieldName, isScript, queryId) {
	inputObj = o(objName);
    if (isScript) {
        openWinForFlowAccess("<%=rootpath%>/flow/form_query_script_list_do.jsp?mode=selField&id=" + queryId + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + fieldName, 800, 600);
    }
    else {
        openWinForFlowAccess("<%=rootpath%>/flow/form_query_list_do.jsp?mode=selField&id=" + queryId + "&openerFormCode=" + openerFormCode + "&openerFieldName=" + fieldName, 800, 600);
    }
}

function openWinPrivilegeSelect(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/admin/priv_list_sel.jsp?isForm=true", 520, 480);
}

function loadNestCtl(ajaxPath, divId, curPage, pageSize, conds) {
	if (curPage) {
		// 去掉&curPage=...&pageSize=...
		var p = ajaxPath.indexOf("&curPage=");
		if (p!=-1) {
			ajaxPath = ajaxPath.substring(0, p);
		}
		ajaxPath += "&curPage=" + curPage;
	}
	if (pageSize) {
		ajaxPath += "&pageSize=" + pageSize;
	}
	if (conds) {
		if ('' != conds) {
			ajaxPath += "&" + conds;
		}
	}
	ajaxpage("<%=rootpath%>/" + ajaxPath, divId);
}

function bindFuncFieldRelateChangeEvent(formCode, targetFieldName, fieldNames) {
	var ary = fieldNames.split(",");
	var len = ary.length;
	for (var i=0; i < len; i++) {
		var field = ary[i];
		if (field=="") {
			continue;
		}
		if (o(field)) {
			var oldValue = o(field).value;
			checkFuncRelateOnchange(formCode, targetFieldName, fieldNames, field, oldValue);
		}
		else {
			if (isIE11) {
				console.log(field + " is not exist");
			}
		}
	}
	
	// 初始化值
	doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames);	
}

function checkFuncRelateOnchange(formCode, targetFieldName, fieldNames, field, oldValue) {
	    setInterval(function(){
			if (oldValue != o(field).value) {
				oldValue = o(field).value;
				doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames);
			}
	    },500);	
}

function doFuncFieldRelateOnChange(formCode, targetFieldName, fieldNames) {
	var ary = fieldNames.split(",");
	var len = ary.length;
	var data = "formCode=" + formCode + "&fieldName=" + targetFieldName;
	for (var i=0; i < len; i++) {
		var field = ary[i];
		if (field=="") {
			continue;
		}
		data += "&" + field + "=" + o(field).value;
	}
	
	data += "&fieldNames=" + fieldNames;
	
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath() %>/visual/getFuncVal.do",
		data: data,
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		complete: function(XMLHttpRequest, status){
		},
		success: function(data, status){
			var ret = $.parseJSON(data);
			o(targetFieldName).value = ret.val;				
		},
		error: function() {
			jAlert(XMLHttpRequest.responseText, '提示');
		}
	});		
}

// 唯一性检查
function checkFieldIsUnique(id, formCode, fieldName, fieldsUnique) {
    var eType = "";
    if (o(fieldName).tagName=="SELECT") {
        eType = "select";
    }
    else {
        eType = "input";
    }
    $(eType + '[name="' + fieldName + '"]').change(function() {
        var datas = {"id":id, "formCode":formCode, "fieldName":fieldName};
        var ary = fieldsUnique.split(",");
		var obj = this;
        for (var i in ary) {
            datas[ary[i]] = o(ary[i]).value;
        }
        $.ajax({
            type: "post",
            url: "<%=request.getContextPath()%>/module_check/checkFieldIsUnique.do",
            contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: datas,
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
                // $('body').showLoading();
            },
            success: function(data, status){
                data = $.parseJSON(data);
                if (data.ret==0) {
                    $(obj).parent().append("<span id='errMsgBox_" + fieldName + "' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
                }
                else {
                    $('#errMsgBox' + fieldName).remove();
                }
            },
            complete: function(XMLHttpRequest, status){
                // $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    });
}

// 嵌套表中字段唯一性检查
function checkFieldIsUniqueNest(id, formCode, fieldName, fieldsUnique) {
    var eType = "";
    if (o(fieldName).tagName=="SELECT") {
        eType = "select";
    }
    else {
        eType = "input";
    }
    $(eType + '[name="' + fieldName + '"]').change(function() {
        var datas = {"id":id, "formCode":formCode, "fieldName":fieldName, "cwsId": $("#cws_id").val(), "parentFormCode":"<%=parentFormCode%>"};
        var ary = fieldsUnique.split(",");
		var obj = this;
        for (var i in ary) {
            datas[ary[i]] = o(ary[i]).value;
        }
        $.ajax({
            type: "post",
            url: "<%=request.getContextPath()%>/module_check/checkFieldIsUniqueNest.do",
            contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
            data: datas,
            dataType: "html",
            beforeSend: function(XMLHttpRequest){
                // $('body').showLoading();
            },
            success: function(data, status){
                data = $.parseJSON(data);
                if (data.ret==0) {
                    $(obj).parent().append("<span id='errMsgBox_" + fieldName + "' class='LV_validation_message LV_invalid'>" + data.msg + "</span>");
                }
                else {
                    $('#errMsgBox' + fieldName).remove();
                }
            },
            complete: function(XMLHttpRequest, status){
                // $('body').hideLoading();
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    });
}

function thousandth(num) {
    num = num + '';
	// 先清掉逗号，再转为千分位，以避已转千分位后再转的问题
	num = num.replaceAll(',', '');

	var p = num.indexOf('.');
    if (p == -1) {
        num += '.'
    }
    return num.replace(/(\d)(?=(\d{3})+\.)/g, function ($0, $1) {
        return $1 + ',';
    }).replace(/\.$/, '');
}

// 千分位处理
function doFormat(fieldName) {
	$('[name="' + fieldName + '"]').change(function() {
		if ($(this).val()!='') {
			$(this).val(thousandth($(this).val()));
		}
	});
}

<%--
function eventHandler(action, moduleCode) {

}

function initEventEmitter(moduleCode, data) {
	// 创建一个jQuery自定义事件对象
	var eventEmitter = $({});
	// 监听事件 add
	eventEmitter.on('add', function () {
		eventHandler('add', moduleCode, data);
	});
	// 监听事件 edit
	eventEmitter.on('edit', function() {
		eventHandler('edit', moduleCode, data);
	});
	eventEmitter.del('del', function() {
		eventHandler('del', moduleCode, data);
	});

	// 触发事件 add
	// eventEmitter.trigger('add');
}

var evtEmitter = initEventEmitter();
function getEventEmitter() {
	return evtEmitter;
}--%>

//自定义事件构造函数
function EventTarget(){
	//事件处理程序数组集合
	this.handlers = {};
}

var EVENT_TYPE = {
	"NEST_ADD": "nest_add",	// 添加一行
	"NEST_EDIT": "nest_edit",	// 编辑一行
	"NEST_DEL": "nest_del",	// 删除一行
	"NEST_AUTO_SEL_DONE": "nest_auto_sel_done",	// 自动拉单
	"NEST_SELECT": "nest_select" // 批量选择
}

//自定义事件的原型对象
EventTarget.prototype = {
	// 设置原型构造函数链
	constructor: EventTarget,
	// 注册给定类型的事件处理程序
	// type -> 自定义事件类型， handler -> 自定义事件回调函数
	addEvent: function (type, handler) {
		// 判断事件处理数组是否有该类型事件
		if (typeof this.handlers[type] == 'undefined') {
			this.handlers[type] = [];
		}
		// 将处理事件push到事件处理数组里面
		this.handlers[type].push(handler);
	},
	// 触发一个事件
	// event -> 为一个js对象，属性中至少包含type属性，
	// 因为类型是必须的，其次可以传一些处理函数需要的其他变量参数。（这也是为什么要传js对象的原因）
	fireEvent: function(event){
		//模拟真实事件的event
		if(!event.target){
			event.target = this;
		}
		// 判断是否存在该事件类型
		if(this.handlers[event.type] instanceof Array){
			var handlers = this.handlers[event.type];
			//在同一个事件类型下的可能存在多种处理事件，找出本次需要处理的事件
			for(var i = 0; i < handlers.length; i++){
				// 执行触发
				handlers[i](event);
			}
		}
	},
	// 注销事件
	// type -> 自定义事件类型， handler -> 自定义事件回调函数
	removeEvent: function(type, handler){
		//判断是否存在该事件类型
		if(this.handlers[type] instanceof Array){
			var handlers = this.handlers[type];
			//在同一个事件类型下的可能存在多种处理事件
			for(var i = 0; i < handlers.length; i++){
				//找出本次需要处理的事件下标
				if(handlers[i] == handler){
					break;
				}
			}
			//从事件处理数组里面删除
			handlers.splice(i, 1);
		}
	}
};

var eventTarget = new EventTarget();
function getEventTarget() {
	return eventTarget;
}
