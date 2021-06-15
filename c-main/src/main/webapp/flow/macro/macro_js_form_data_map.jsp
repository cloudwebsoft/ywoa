<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

    Privilege pvg = new Privilege();
    int flowId = ParamUtil.getInt(request, "flowId", -1);
    String formCode = ParamUtil.get(request, "formCode"); // destForm
%>
function doFormDataMap(fieldName) {
	if (o(fieldName).value.trim()=="") {
    	alert("请输入流程ID");
        return;
    }
    	
    $.ajax({
        type: "post",
        url: "<%=request.getContextPath()%>/flow/ajax_form_data_map.jsp",
        data : {
        	sourceFlowId: o(fieldName).value,
        	flowId: "<%=flowId%>",
            fieldName: fieldName,
            formCode: "<%=formCode%>"
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest){
            //ShowLoading();
        },
        success: function(data, status){
            // alert(data.trim());
            data = $.parseJSON(data.trim());
            var dataRaw = data;
            data = dataRaw.result;

			for (var one in data) {
            	var field = data[one].fieldName;
                var macroType = data[one].macroType;
                if (macroType!="nest_table") {
                	// alert(field + " " + data[one].value);
                	setCtlValue(field, data[one].type, data[one].value);
                    var canSaveFormValue = true; // 能否保存草稿
                    if (data[one].editable=="false") {
                    	canSaveFormValue = false;
                    	DisableCtl(field, data[one].type, data[one].value, data[one].value);
                        // 取消livevalidation检测，否则会因未通过检测，无法submit
                        var fobj;
                        try {
                        	fobj = eval("f_" + field);
                        }
                        catch (e) {
                        	// alert("e=" + e.message);
                        }
                        if (fobj) {
                        	fobj.destroy();
                        }
                    }
                    if (!canSaveFormValue) {
                    	// 如果存在不能被修改的映射表单域，则置保存草稿按钮为false
                        toolbar.setDisabled(0, true);
                    }
                }
                else {
                    // 清空嵌套表原来的行，连默认的空行也删除
                    $("#cwsNestTable tr").each(function(i){
                    	if (i>0)
	                        $(this).remove();
                    });
                    
                    // 加入新行
                    var ary = data[one].ary;
                    var cellEditable = data[one].editable=="true";
                    var cellAppendable = data[one].appendable=="true";
                    for (var key in ary) {
                    	var rowData = ary[key];
                        // 插入空行
                    	add_row(Main_Tab);
                        // 置表格的行为不可编辑状态
                        if (!cellEditable)
                            $("#cwsNestTable tr:last").attr("editable", "0");
                        
                        // 遍历表头，获取字段的值
                        $("#cwsNestTable tr:first").children().each(function(k) {
                        	var fName = $(this).attr("fieldName")
                        	if (!$(this)[0].getAttribute("fieldName")) {
                            	// 不能加ID，加了在编辑时就会被认为是之前已保存的数据
                            	// $("#cwsNestTable tr:last").children("td:eq(0)").html(rowData.id); // ID列
                            }
                            else {
                                var c = $("#cwsNestTable tr:last").children("td:eq(" + k + ")");
                            	c.html(eval("rowData." + fName + "_html"));
                                if ($(this).attr("type")=="<%=FormField.TYPE_MACRO%>") {
                                	c.attr("value", eval("rowData." + fName + "_value"));
                                }
                                if (!cellEditable) {
                                	c.attr("editable", "0");
                                }
                            }
                        });
                    }
                    
                    // 判断是否禁止编辑
                    if (!cellAppendable) {
                    	o("addBtn").disabled = true;
                    }
                }
            }
        },
        complete: function(XMLHttpRequest, status){
            // HideLoading();
        },
        error: function(XMLHttpRequest, textStatus){
            // 请求出错处理
            alert(XMLHttpRequest.responseText);
        }
    });	
}