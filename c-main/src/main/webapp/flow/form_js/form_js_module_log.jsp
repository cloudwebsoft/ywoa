<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%
/*
- 功能描述：模块日志表
- 访问规则：从module_list.jsp中通过include script访问
- 过程描述：
- 注意事项：
- 创建者：bluewind
- 创建时间：2018-09-12
==================
- 修改者：
- 修改时间：
- 修改原因:
- 修改点:
*/
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
    response.setContentType("text/javascript;charset=utf-8");
%>
function restore(id) {
    jConfirm('您确定要恢复么？', '提示', function(r) {
        if (r) {	
            $.ajax({
                type: "post",
                contentType:"application/x-www-form-urlencoded; charset=iso8859-1",        
                url: "<%=request.getContextPath()%>/visual/restore.do",
                async: false,
                data: {
                	id: id
                },
                dataType: "html",
                beforeSend: function(XMLHttpRequest){
                    // $('#bodyBox').showLoading();
                },
                success: function(data, status) {
                    data = $.parseJSON(data);
                    jAlert(data.msg, "提示");
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
    });		   
}