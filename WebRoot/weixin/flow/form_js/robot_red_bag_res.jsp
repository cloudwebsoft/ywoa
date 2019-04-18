<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.sms.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%
    int flowId = ParamUtil.getInt(request, "flowId", -1);
    int myActionId = ParamUtil.getInt(request, "myActionId", -1);
    long id = ParamUtil.getInt(request, "id", -1);
    String pageType = ParamUtil.get(request, "pageType");
    boolean isShowModule = "showModule".equals(pageType);
    if (isShowModule) {
        String formCode = "robot_red_bag_res";
        FormDAO fdao = new FormDAO();
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        fdao = fdao.getFormDAO(id, fd);
        String kind = fdao.getFieldValue("kind");
        if ("0".equals(kind)) {
            return;
        }
%>
        function appendBtn() {
            // 因为载入js顺序是无序的，所以用append的时候有时会出现在前面
            var str = '<li class="mui-table-view-cell"><div class="mui-table"><div class="mui-table-cell mui-col-xs-12" style="text-align:center"><button id="btnShare" type="button" class="mui-btn mui-btn-blue">晒红包</button></div></div></li>';
            $('#formDetail').prepend(str);
        }
        mui.ready(function() {
            appendBtn();

            mui('#formDetail').on('tap', '.mui-btn', function() {
                var btnId = this.getAttribute('id');
                if (btnId=="btnShare") {
                    window.location.href = "<%=request.getContextPath()%>/public/robot/redbag_share.jsp?id=<%=id%>";
                }
            });
        });
<%
    }
%>

