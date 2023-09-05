<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

    int flowId = ParamUtil.getInt(request, "flowId", -1);
    long id = ParamUtil.getLong(request, "id", -1);
    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
    boolean editable = ParamUtil.getBoolean(request, "editable", false);

    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);

    FormField ff = fd.getFormField(fieldName);

    String pageType = ParamUtil.get(request, "pageType");

    String desc = ff.getDescription();
    String params = null;
    String code = "";
    try {
        JSONObject json = new JSONObject(desc);
        code = json.getString("code");
        params = json.getString("params");
    } catch (JSONException e) {
        e.printStackTrace();
        return;
    }

    String[] paramAry = StrUtil.split(params, ",");
    if (paramAry==null) {
        return;
    }

    StringBuffer sbParams = new StringBuffer();
    for (String fieldTitle : paramAry) {
        fieldTitle = fieldTitle.trim();
        FormField field = fd.getFormField(fieldTitle);
        if (field == null) {
            field = fd.getFormFieldByTitle(fieldTitle);
        }
        if (field == null) {
            if ("id".equalsIgnoreCase(fieldTitle)) {
                if (ConstUtil.PAGE_TYPE_FLOW.equals(pageType) || ConstUtil.PAGE_TYPE_FLOW_SHOW.equals(pageType)) {
                    com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                    fdao = fdao.getFormDAO(flowId, fd);
                    StrUtil.concat(sbParams, "+','+", String.valueOf(fdao.getId())); // @task 模块添加页面中没有元素id
                }
                else {
                    StrUtil.concat(sbParams, "+','+", String.valueOf(id)); // @task 模块添加页面中没有元素id
                }
            }
            else if ("cws_status".equalsIgnoreCase(fieldTitle)) {
                StrUtil.concat(sbParams, "+','+", "$(o('" + fieldTitle + "')).val()"); // @task 模块编辑页面中没有元素cws_status
            }
            else if ("cws_id".equalsIgnoreCase(fieldTitle)) {
                StrUtil.concat(sbParams, "+','+", "$(o('" + fieldTitle + "')).val()"); // @task 模块编辑页面中没有元素cws_status
            }
            else if ("formCode".equalsIgnoreCase(fieldTitle)) {
                StrUtil.concat(sbParams, "+','+", formCode);
            }
            else if ("cws_quote_id".equalsIgnoreCase(fieldTitle)) {
                long cwsQuoteId = -1;
                if (ConstUtil.PAGE_TYPE_FLOW.equals(pageType) || ConstUtil.PAGE_TYPE_FLOW_SHOW.equals(pageType)) {
                    com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
                    fdao = fdao.getFormDAO(flowId, fd);
                    cwsQuoteId = fdao.getCwsQuoteId();
                }
                else if (id!=-1) {
                    com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
                    fdao = fdao.getFormDAO(id, fd);
                    cwsQuoteId = fdao.getCwsQuoteId();
                }
                StrUtil.concat(sbParams, "+','+", String.valueOf(cwsQuoteId));
            }
            else {
                StrUtil.concat(sbParams, "+','+", "'" + fieldTitle + "'");
            }
            continue;
        }
        else {
            StrUtil.concat(sbParams, "+','+", "o('" + fieldTitle + "').value");
        }
%>
        $(function() {
            var oldValue_<%=field.getName()%> = "cws-65536"; // 一个不存在的值
            if (o("<%=field.getName()%>")) { // 防止此控件也是SQL控件，并且此时还不存在
                oldValue_<%=field.getName()%> = o("<%=field.getName()%>").value;
            }
            setInterval(function(){
                if (o("<%=field.getName()%>")) {
                    if (oldValue_<%=field.getName()%> != o("<%=field.getName()%>").value) {
                        oldValue_<%=field.getName()%> = o("<%=field.getName()%>").value;
                        onFormulaCtlRelateFieldChange_<%=fieldName%>();
                    }
                }
            },500);
        });
<%
    }
    // System.out.println(getClass() + " sbParams=" + sbParams);
%>
// 取得本表单中相应的值
function getFieldVal(fieldName) {
    // 先从当前表单中取，如果取不到则从父表单中取
    if (o(fieldName)) {
        return o(fieldName).value;
    }
}

function onFormulaCtlRelateFieldChange_<%=fieldName%>() {
    var params = '<%=params%>'; // 必须得用单引号，因为可能有些参数会是字符串，带有双引号
    var formulaStr = '';
    try {
        formulaStr = '#<%=code%>(' + <%=sbParams%> + ')';
    }
    catch (e) {}

    if (formulaStr.indexOf("undefined")!=-1) {
        if (isIE11) {
            console.log("有可能是初始化时，onFormulaCtlRelateFieldChange中 <%=StrUtil.toHtml(sbParams.toString())%> 生成的函数 " + formulaStr + " 存在为null的对象");
        }
        // return;
    }
    if (formulaStr == "") {
        if (isIE11) {
            console.log("onFormulaCtlRelateFieldChange中 <%=StrUtil.toHtml(sbParams.toString())%> 存在JS错误");
        }
        return;
    }

    $.ajax({
        type: "post",
        contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
        url: "<%=request.getContextPath()%>/visual/formula/doFormula.do",
        async: false,
        data: {
            formula: formulaStr,
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest) {
            // $('#bodyBox').showLoading();
        },
        success: function(data, status) {
            data = $.parseJSON(data);
            if (data.ret=="1") {
		    	if (o("<%=fieldName%>")) {
                    $("#<%=fieldName%>").val(data.value);

                    setTimeout(function () {
                        if(o("<%=fieldName%>_show")) {
                            o("<%=fieldName%>_show").innerHTML = data.value;
                        }
                    }, 500);

		            var frm = o("visualForm");
		            if (frm==null) {
		            	frm = o("flowForm");
		            }
			    }
		    }
            else {
                mui.alert(data.msg);
            }
	    },
	    complete: function(XMLHttpRequest, status){
	    	// $('#bodyBox').hideLoading();
	    },
	    error: function(XMLHttpRequest, textStatus){
	    	// 请求出错处理
	    	alert(XMLHttpRequest.responseText);
	    }
    });
}

<%
if (editable) {
%>
    $(function() {
        onFormulaCtlRelateFieldChange_<%=fieldName%>();
    });
<%}%>