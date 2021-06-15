<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "com.redmoon.oa.person.UserDb"%>
<%@ page import = "org.json.JSONObject"%>
<%
	/*
    - 功能描述：人员基本信息表
    - 访问规则：通过include script访问
    - 过程描述：
    - 注意事项：
    - 创建者：hw
    - 创建时间：2015-08-06
    ==================
    - 修改者：
    - 修改时间：
    - 修改原因:
    - 修改点:
    */

	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");

    String op = ParamUtil.get(request, "op");
    if ("check".equals(op)) {
		response.setContentType("text/html;charset=utf-8");
		JSONObject json = new JSONObject();
        String userName = ParamUtil.get(request, "userName");
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        if (user.isLoaded()) {
            json.put("ret", 0);
			if (!user.isValid()) {
				json.put("msg", "帐户已赋给：" + user.getRealName() + "，该用户原先已离职，将启用该帐户");
			}
			else {
				json.put("msg", "帐户已赋给：" + user.getRealName() + "，该用户在职，继续添加将关联此用户");				
			}
        }
        else {
            json.put("ret", 1);
            json.put("msg", "");
        }
        out.print(json);
        return;
    }

	response.setContentType("text/javascript;charset=utf-8");

    String pageType = ParamUtil.get(request, "pageType");
    if (!"add".equals(pageType)) {
%>
$(document).ready(function() {
    $('input[name="user_name"]').each(function() {
    	if ($(this).val() != '') {
    		$(this).attr('readOnly', true);
    	}
    });
});
<%
    }
    else {
%>        
        $(function() {

			return;

            $('input[name="user_name"]').change(function() {
                var userName = $(this).val();
    			$.ajax({
    				type: "post",
    				url: "<%=request.getContextPath()%>/flow/form_js/form_js_personbasic.jsp",
    				contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
    				data: {
    					op: "check",
    					userName: userName
    				},
    				dataType: "html",
    				beforeSend: function(XMLHttpRequest){
    					$('body').showLoading();
    				},
    				success: function(data, status){
    					data = $.parseJSON(data);
    					if (data.ret=="0") {
    						$('input[name="user_name"]').parent().append("<div id='msgBox' style='color:red'>" + data.msg + "</div>");
    					}
    					else {
    					    $('#msgBox').remove();
    					}
    				},
    				complete: function(XMLHttpRequest, status){
    					$('body').hideLoading();
    				},
    				error: function(XMLHttpRequest, textStatus){
    					// 请求出错处理
    					alert(XMLHttpRequest.responseText);
    				}
    			});
            });
        });
<%
    }
%>
