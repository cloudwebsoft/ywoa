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
<script>
    function restore(id) {
        myConfirm('提示', '您确定要恢复么？', function() {
            var ajaxData = {
                id: id
            }
            ajaxPost('/visual/restore', ajaxData).then((data) => {
                console.log('data', data);
                if (data.ret=="1") {
                    myMsg(data.msg);
                } else {
                    myMsg(data.msg, 'error');
                }
            });
        });
    }
</script>