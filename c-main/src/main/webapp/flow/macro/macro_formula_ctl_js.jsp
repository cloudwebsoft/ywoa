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
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");

    Privilege pvg = new Privilege();
    int flowId = ParamUtil.getInt(request, "flowId", -1);
    long id = ParamUtil.getLong(request, "id", -1);
    String fieldName = ParamUtil.get(request, "fieldName");
    String formCode = ParamUtil.get(request, "formCode");
    boolean isHidden = ParamUtil.getBoolean(request, "isHidden", false);
    boolean editable = ParamUtil.getBoolean(request, "editable", false);

    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    if (!fd.isLoaded()) {
        DebugUtil.e("macro_formula_ctl_js.jsp", "表单", "不存在，编码：" + formCode);
        return;
    }

    FormField ff = fd.getFormField(fieldName);
    if (ff==null) {
        DebugUtil.e("macro_formula_ctl_js.jsp", "字段", "不存在，字段：" + fieldName + "，表单：" + fd.getName());
        return;
    }

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

    // 解析形参params，并取得其值，例：user_name, "12,15,16"
    // 先将其中的,号改为%co，以免在split的时候出现问题
    Pattern p = Pattern.compile("\"(.*?)\"",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(params);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
        String str = m.group(1);
        
        // 将其中的逗号改为%co
        String patternStr = ","; //
        String replacementStr = "%co";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(str);
        str = matcher.replaceAll(replacementStr);
        
        m.appendReplacement(sb, "\"" + str + "\"");
    }
    m.appendTail(sb);
    
    String[] paramAry = StrUtil.split(sb.toString(), ",");
    if (paramAry==null) {
        return;
    }
    // split后将字符串中的逗号还原
    if (paramAry!=null) {
        for (int i = 0; i < paramAry.length; i++) {
            paramAry[i] = paramAry[i].replaceAll("%co", ",").trim();
        }
    }

    String formName = ParamUtil.get(request, "cwsFormName");

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
                StrUtil.concat(sbParams, "+','+", "$(findObj('" + fieldTitle + "')).val()"); // @task 模块编辑页面中没有元素cws_status
            }
            else if ("cws_id".equalsIgnoreCase(fieldTitle)) {
                StrUtil.concat(sbParams, "+','+", "$(findObj('" + fieldTitle + "')).val()"); // @task 模块编辑页面中没有元素cws_status
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
            StrUtil.concat(sbParams, "+','+", "findObj('" + fieldTitle + "').value");
        }
%>
        function initCheckChange_<%=field.getName()%>() {
            // 函数宏控件在进入页面时，必进行计算
            // var curFormId = getCurFormUtil().get();
            var curFormId = '<%=formName%>';
            var oldValue_<%=field.getName()%> = "cws-65536"; // 一个不存在的值
            if (fo("<%=field.getName()%>", curFormId)) { // 防止此控件也是SQL控件，并且此时还不存在
                oldValue_<%=field.getName()%> = fo("<%=field.getName()%>", curFormId).value;
            }
            var sint = setInterval(function(){
				if (o(curFormId)) {
                    if (fo("<%=field.getName()%>", curFormId)) {
                        if (oldValue_<%=field.getName()%> != fo("<%=field.getName()%>", curFormId).value) {
                            oldValue_<%=field.getName()%> = fo("<%=field.getName()%>", curFormId).value;
                            onFormulaCtlRelateFieldChange_<%=fieldName%>();
                        }
                    } else {
                        // 使关闭抽屉时，能够销毁setInterval，否则仅管前端removeScript了，但检测代码仍会运行
                        window.clearInterval(sint);
                        console.log('macro_formula_ctl_js clearInterval <%=field.getName()%>');
                    }
                }
            }, 200);
            // 当菜单项不启用缓存时，只能通过如下方法才能清除interval
			getCurFormUtil().addInterval(sint, '<%=formName%>');
        }
        initCheckChange_<%=field.getName()%>();
<%
    }
    // System.out.println(getClass() + " sbParams=" + sbParams);
%>
// 取得本表单中相应的值
function getFieldVal(fieldName) {
    if (findObj(fieldName)) {
        return findObj(fieldName).value;
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
            console.log("有可能是初始化时，onFormulaCtlRelateFieldChange中 <%=StrUtil.toHtml(sbParams.toString())%> 生成的公式 " + formulaStr + " 存在为null的对象");
        }
        // return;
    }
    if (formulaStr == "") {
        if (isIE11) {
            console.log("onFormulaCtlRelateFieldChange中 <%=StrUtil.toHtml(sbParams.toString())%> 存在JS错误");
        }
        return;
    }

    var ajaxData = {
        formula: formulaStr,
    };
    ajaxPost('/visual/formula/doFormula', ajaxData).then((data) => {
		console.log('data', data);
        if (data.ret=="1") {
            var obj = findObj("<%=fieldName%>");
            if (obj) {
                $(obj).val(data.value);

                setTimeout(function () {
                    if(findObj("<%=fieldName%>_show")) {
                        findObj("<%=fieldName%>_show").innerHTML = data.value;
                    }
                }, 500);

                var frm = getCurForm();

                // 删除原来的验证，否则会因为原验证中存储的对象不存在而导致验证失效
                var formObj = LiveValidationForm.getInstance(frm);
                if (formObj) {
                    formObj.removeFieldByName('<%=fieldName%>');
                }
                <%
                    ParamChecker pck = new ParamChecker(request);
                    out.print(com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, ff));
                    // DebugUtil.i(getClass(), "doFormula", com.redmoon.oa.visual.FormUtil.getCheckFieldJS(pck, ff));
                %>
            }

            try {
                initCalculator();
            }
            catch(e) {}
        }
        else {
            console.error(data.msg);
            myMsg(data.msg, 'warning');
        }
	});
}

<%
// nest_sheet_edit_relat.jsp中传过来时是edit
if (!"flowShow".equals(pageType) && !"show".equals(pageType)) { // && !"edit".equals(pageType)) {
%>
    // $(function() {
        onFormulaCtlRelateFieldChange_<%=fieldName%>();
    // });
<%}%>