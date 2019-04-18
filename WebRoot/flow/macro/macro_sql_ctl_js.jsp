<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "java.util.regex.*"%>
<%@ page import = "org.json.*"%>
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
boolean isList = false;
if ("moduleList".equals(pageType)){
	isList = true;
}
String op = ParamUtil.get(request, "op");
if (op.equals("onChange")) {
	JSONObject json = new JSONObject();
	String html = SQLCtl.getCtlHtml(request, flowId, ff);
	
	if (html.indexOf("<input")!=-1) {
		json.put("type", "input");
	}
	else {
		json.put("type", "select");
	}

	json.put("ret", "1");
	json.put("html", html);
	out.print(json);

	return;
}

String[] ary;
String desc = StrUtil.getNullStr(ff.getDescription());
if ("".equals(desc)) {
	ary = SQLCtl.getSql(ff.getDefaultValue());
}
else {
	ary = SQLCtl.getSql(desc);
}
		
String sql = ary[0];
String fieldPairs = "";
// 从sql中找出表单中的变量 {$field}
Pattern p = Pattern.compile(
		"\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
		Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
Matcher m = p.matcher(sql);
StringBuffer sb = new StringBuffer();
while (m.find()) {
	String fieldTitle = m.group(1);
	// 向下兼容，可能旧版的会用{$title}作为条件字段
	FormField field = fd.getFormField(fieldTitle);
	if (field == null) {
		field = fd.getFormFieldByTitle(fieldTitle);
	}
	if (field!=null) {            	
		fieldPairs += field.getName() + ": getPFieldVal('" + field.getName() + "'),\n";
	}
	else {
		fieldPairs += fieldTitle + ": getPFieldVal('" + fieldTitle + "'),\n";	
		continue;
	}
	%>
    $(function() {
    	// ie8 attachEvent无法检测到javascript赋值的onchange事件
    	if (true || isIE11 || isIE8) {
				var oldValue_<%=field.getName()%> = "cws-65536"; // 一个不存在的值
				if (o("<%=field.getName()%>")) { // 防止此控件也是SQL控件，并且此时还不存在
					oldValue_<%=field.getName()%> = o("<%=field.getName()%>").value;
				}
                setInterval(function(){
            					if (o("<%=field.getName()%>")) {
                                	if (oldValue_<%=field.getName()%> != o("<%=field.getName()%>").value) {
                                    	oldValue_<%=field.getName()%> = o("<%=field.getName()%>").value;
                                    	onSQLCtlRelateFieldChange_<%=fieldName%>();
                                	}
            					}
                            },500);

        }
        else {
        	// chrome下不支持，所以统一通过定时器检测
           	$('input[name=<%=field.getName()%>]').bind('input propertychange', function() {
               	onSQLCtlRelateFieldChange_<%=fieldName%>();
           	});
        }
    });	
	<%
}

// System.out.println(getClass() + " fieldPairs=" + fieldPairs);

%>
// 取得本表单或父表单中相应的值
function getPFieldVal(fieldName) {
	// 先从当前表单中取，如果取不到则从父表单中取
	if (o(fieldName)) {
		return o(fieldName).value;
	}
	else {
		var dlg = window.opener ? window.opener : window.parent;
		if (dlg) {
			if (dlg.o(fieldName)) {
				// console.log(fieldName + "=" + dlg.o(fieldName).value);
				return dlg.o(fieldName).value;
			}
			else {
				if (isIE11) {
					console.log("macro_sql_ctl_js.jsp getPFieldVal:" + fieldName + " is not found!");
				}
				return "";
			}
		}
		else {
			return "";
		}
	}
}

function onSQLCtlRelateFieldChange_<%=fieldName%>() {
    $.ajax({
        type: "post",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",        
        url: "<%=request.getContextPath()%>/flow/macro/macro_sql_ctl_js.jsp",
		async: false,
        data: {
            op: "onChange",
            flowId: "<%=flowId%>",
            fieldName: "<%=fieldName%>",
            isHidden: "<%=isHidden%>",
            editable: "<%=editable %>",
            <%=fieldPairs%>
            formCode: "<%=formCode%>"
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest){
            // $('#bodyBox').showLoading();
        },
        success: function(data, status) {
            data = $.parseJSON(data);
            if (data.ret=="1") {
            	if (o("<%=fieldName%>")) {
						// 记录当前值
						var curVal = $("#<%=fieldName%>").val();
	                    $("#<%=fieldName%>").parent().replaceWith(data.html);
	                    
	                    if (data.type=="select") {
	                    	$("#<%=fieldName%>").val(curVal);
	                    }
	                    
						var frm = o("visualForm");
						if (frm==null) {
							frm = o("flowForm");
						}

						// 如果为列表页面，不需要验证
						if(!<%=isList%>){
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
                }
                
				try {
					initCalculator();
				}
				catch(e) {}                
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
	onSQLCtlRelateFieldChange_<%=fieldName%>();
});
<%}%>
