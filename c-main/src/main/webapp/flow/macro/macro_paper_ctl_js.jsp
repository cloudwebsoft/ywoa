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
    String desc = ParamUtil.get(request, "desc");
    String fieldName = ParamUtil.get(request, "fieldName");
    String[] ary = StrUtil.split(desc, ",");
    int len = 0;
    if (ary != null)
        len = ary.length;

// System.out.println(getClass() + " " + ary + " desc=" + desc + " fieldName=" + fieldName + " flowId=" + flowId + " len=" + len);

    String op = ParamUtil.get(request, "op");
    if (op.equals("getNo")) {
        PaperNoPrefixDb pnpn = new PaperNoPrefixDb();
        String noPrefix = ParamUtil.get(request, ary[0]);
        pnpn = pnpn.getPaperNoPrefixDbByName(noPrefix);
        if (pnpn != null) {
            // 组合
            int num = pnpn.getInt("cur_num") + 1;
            String str = noPrefix + "[" + ParamUtil.get(request, ary[1]) + "]" + num + "号";
            out.print(str);
        }
    }

    for (int i = 0; i < len; i++) {
        String field = ary[i];
        System.out.print(getClass() + " " + field);
%>
$("input[name='<%=field%>']").change(function() {
	onchange();
});
<%
    }
%>

function onchange() {
    $.ajax({
        type: "post",
        url: "flow/finishAction.do",
        data: {
            op: "getNo",
            flowId: "<%=flowId%>",
            fieldName: "<%=fieldName%>",
            <%=ary[0]%>: o("<%=ary[0]%>").value,
            <%=ary[1]%>: o("<%=ary[1]%>").value,
            desc: "<%=desc%>"
        },
        dataType: "html",
        beforeSend: function(XMLHttpRequest){
            $('#bodyBox').showLoading();
        },
        success: function(data, status){
            $("#<%=fieldName%>").val(data.trim());
        },
        complete: function(XMLHttpRequest, status){
            $('#bodyBox').hideLoading();				
        },
        error: function(XMLHttpRequest, textStatus){
            // 请求出错处理
            alert(XMLHttpRequest.responseText);
        }
}


