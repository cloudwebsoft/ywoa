<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.sms.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="org.json.*" %>
<%
    Privilege pvg = new Privilege();
    int flowId = ParamUtil.getInt(request, "flowId", -1);
    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
    boolean isHidden = ParamUtil.getBoolean(request, "isHidden", false);
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
        FormField field = fd.getFormField(fieldTitle);
        if (field == null) {
            field = fd.getFormFieldByTitle(fieldTitle);
        }
        if (field == null) {
            StrUtil.concat(sbParams, ",", fieldTitle);
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
    // System.out.println(getClass() + " fieldPairs=" + fieldPairs);
%>
// 取得本表单中相应的值
function getFieldVal(fieldName) {
    // 先从当前表单中取，如果取不到则从父表单中取
    if (o(fieldName)) {
        return o(fieldName).value;
    }
}

function onFormulaCtlRelateFieldChange_<%=fieldName%>() {
    var params = "<%=params%>";
    var formulaStr = '#<%=code%>(' + <%=sbParams%> + ')';

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

		            var frm = o("visualForm");
		            if (frm==null) {
		            	frm = o("flowForm");
		            }
		
                    // 删除原来的验证，否则会因为原验证中存储的对象不存在而导致验证失效
                    var formObj = LiveValidationForm.getInstance(frm);
                    if (formObj) {
                        formObj.removeFieldByName('<%=fieldName%>');
                    }
		            <%
		                ParamChecker pck = new ParamChecker(request);
		                out.print(com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, ff));
		            %>
			    }
			
			    try {
			    	initCalculator();
			    }
			    catch(e) {}
		    }
            else {
                jAlert(data.msg, "提示");
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
// nest_sheet_edit_relat.jsp中传过来时是edit
if (!"flowShow".equals(pageType) && !"show".equals(pageType)) { // && !"edit".equals(pageType)) {
%>
    $(function() {
        onFormulaCtlRelateFieldChange_<%=fieldName%>();
    });
<%}%>