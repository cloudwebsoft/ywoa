<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.Leaf" %>
<%@page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%
    String dirCodeTop = ParamUtil.get(request, "dirCode");
    if (dirCodeTop.equals("")) {
        dirCodeTop = ParamUtil.get(request, "code");
    }
    if (dirCodeTop.equals("")) {
        dirCodeTop = ParamUtil.get(request, "flowTypeCode");
    }
    if (dirCodeTop.equals("")) {
        dirCodeTop = ParamUtil.get(request, "parent_code");
    }
    if (dirCodeTop.equals("")) {
        dirCodeTop = ParamUtil.get(request, "typeCode");
    }

    com.redmoon.oa.flow.LeafPriv lpTop = new com.redmoon.oa.flow.LeafPriv(dirCodeTop);
    com.redmoon.oa.flow.Leaf lfTop = new com.redmoon.oa.flow.Leaf();
    lfTop = lfTop.getLeaf(dirCodeTop);
    if (lfTop == null) {
        out.print(SkinUtil.makeInfo(request, "请选择流程类型"));
        return;
    }
%>
<style>
    #tabs1 ul li.current {
        width: 80px !important;
    }
    #tabs1 ul li {
        width: 80px !important;
    }
</style>
<div id="tabs1">
    <ul>
        <%
            com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
            if (lfTop.getType() == com.redmoon.oa.flow.Leaf.TYPE_LIST) {
        %>
        <%if (lpTop.canUserExamine(pvgTop.getUser(request))) {%>
        <li id="menu2"><a href="<%=request.getContextPath()%>/admin/flow_predefine_init_myflow.jsp?flowTypeCode=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>流程图</span></a></li>
        <%}%>
        <%} else if (lfTop.getType() == com.redmoon.oa.flow.Leaf.TYPE_FREE) {%>
        <%if (lpTop.canUserExamine(pvgTop.getUser(request))) {%>
        <li id="menu2"><a href="<%=request.getContextPath()%>/admin/flow_predefine_free.jsp?flowTypeCode=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>设置</span></a></li>
        <%}%>
        <%}%>
        <%if (lpTop.canUserExamine(pvgTop.getUser(request))) {%>
        <li id="menu3"><a href="<%=request.getContextPath()%>/admin/flow_predefine_dir.jsp?op=modify&code=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>属性</span></a></li>
        <%}%>
        <%
            if (!"".equals(lfTop.getFormCode())) {
                FormDb lfTopfd = new FormDb();
                lfTopfd = lfTopfd.getFormDb(lfTop.getFormCode());
                if (lfTopfd.isLoaded() && (pvgTop.isUserPrivValid(request, "admin") || lpTop.canUserExamine(pvgTop.getUser(request)))) {
        %>
        <li id="menu4"><a href="javascript:" formCode="<%=lfTop.getFormCode()%>" formName="<%=lfTopfd.getName()%>"><span>表单</span></a></li>
        <%
                }
            }
        %>
        <%if (lpTop.canUserExamine(pvgTop.getUser(request))) {%>
        <li id="menu6"><a href="<%=request.getContextPath()%>/admin/flow_dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>权限</span></a></li>
        <%}%>
        <%
            if (lfTop.getLayer() > 2 && !License.getInstance().isCloud()) {
        %>
        <li id="menu10"><a href="<%=request.getContextPath()%>/admin/flow_designer_action_view.jsp?dirCode=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>显示规则</span></a></li>
        <%
            }
        %>
        <%if (lfTop.getLayer() <= 2) {%>
        <li id="menu7"><a href="<%=request.getContextPath()%>/admin/flow_predefine_dir.jsp?parent_code=<%=StrUtil.UrlEncode(dirCodeTop)%>&op=AddChild"><span>添加</span></a></li>
        <%}%>
        <%
            boolean isShowRecord = lfTop.getLayer() > 2 || Leaf.CODE_ROOT.equals(lfTop.getCode());
            if (lpTop.canUserQuery(pvgTop.getUser(request)) || lpTop.canUserExamine(pvgTop.getUser(request))) {
                if (isShowRecord) {
        %>
        <li id="menu5"><a href="javascript:;"><span>记录</span></a></li>
        <%
            }
            if (lfTop.getLayer() > 2) {
        %>
        <li id="menu8"><a href="<%=request.getContextPath()%>/admin/flow_stat_month.jsp?typeCode=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>月统计</span></a></li>
        <li id="menu9"><a href="<%=request.getContextPath()%>/admin/flow_stat_year.jsp?typeCode=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>年统计</span></a></li>
        <%
            if (!"".equals(dirCodeTop)) {
                if (lfTop.getType() != Leaf.TYPE_FREE) {
        %>
        <li id="menu11"><a href="<%=request.getContextPath()%>/flow/flow_analysis_year.jsp?typeCode=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>效率分析</span></a></li>
        <%
                        }
                    }
                }
            }
        %>
    </ul>
</div>
<script>
    $(function() {
        // 必须要通过jQuery绑定click，如果直接通过onclick事件addTab，则左侧树形菜单中的链接在addTab后将无法点击
        $("a[formCode]").click(function(e) {
            e.preventDefault(); // 必须要加上，否则仍会无法点击
            addTab($(this).attr('formName'), '<%=request.getContextPath()%>/admin/form_edit.jsp?code=' + $(this).attr('formCode'));
        })

        <%
        if (isShowRecord) {
        %>
        $('#menu5').click(function() {
            addTab("<%=lfTop.getName()%>", "<%=request.getContextPath()%>/admin/flow_list.jsp?typeCode=<%=StrUtil.UrlEncode(dirCodeTop)%>");
        });
        <%
        }
        %>
    })
</script>