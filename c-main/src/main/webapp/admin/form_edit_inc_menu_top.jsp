<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%
    String formCodeTop = ParamUtil.get(request, "code");
    FormDb fdTop = new FormDb();
    fdTop = fdTop.getFormDb(formCodeTop);
%>
<div id="tabs1">
    <ul>
        <li id="menu1"><a href="<%=request.getContextPath()%>/admin/form_edit.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>表单编辑</span></a></li>
        <li id="menu2"><a href="<%=request.getContextPath()%>/admin/form_field_m.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>字段管理</span></a></li>
        <%
            if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
        %>
        <li id="menu4"><a
                href="<%=request.getContextPath()%>/admin/form_remind_list.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>到期提醒</span></a>
        </li>
        <%
            }
            if (!License.getInstance().isCloud()) {
        %>
        <li id="menu5"><a
                href="<%=request.getContextPath()%>/admin/form_view_setup.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>显示规则</span></a>
        </li>
        <%
            }

            if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
        %>
        <li id="menu6"><a
                href="<%=request.getContextPath()%>/admin/form_check_setup.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>验证规则</span></a>
        </li>
        <%
            }
            if (com.redmoon.oa.kernel.License.getInstance().isSrc()) {
        %>
        <li id="menu7"><a
                href="<%=request.getContextPath()%>/visual/module_scripts_iframe.jsp?code=<%=formCodeTop%>&formCode=<%=formCodeTop%>"><span>事件脚本</span></a>
        </li>
        <%
            }
            if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {
        %>
        <li id="menu3"><a href="javascript:;"
                          onclick="addTab('<%=fdTop.getName()%>-模块', '<%=request.getContextPath()%>/visual/module_setup_list.jsp?formCode=<%=StrUtil.UrlEncode(formCodeTop)%>')"><span>模块管理</span></a>
        </li>
        <%}%>
    </ul>
</div>