<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%
    com.redmoon.oa.pvg.Privilege privilegeInc = new com.redmoon.oa.pvg.Privilege();
    String mynameTop = ParamUtil.get(request, "userName");
    if (mynameTop.equals("")) {
        mynameTop = privilegeInc.getUser(request);
    }
    String myrealTop = new UserDb(mynameTop).getRealName();
    boolean isMe = mynameTop.equals(privilegeInc.getUser(request));
%>
<div class="tabs1Box">
    <div id="tabs1">
        <ul>
            <li id="menu1"><a
                    href="workplan_list.jsp?userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span><%=isMe ? "我" : myrealTop %>参与的计划</span></a>
            </li>
            <li id="menu2"><a
                    href="workplan_list.jsp?op=mine&userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span><%=isMe ? "我" : myrealTop %>拟定的计划</span></a>
            </li>
            <li id="menu5"><a
                    href="workplan_task_list.jsp?userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span><%=isMe ? "我" : myrealTop %>的计划项</span></a>
            </li>
            <%if (isMe) { %>
            <li id="menu6"><a href="workplan_list.jsp?op=favorite&userName=<%=StrUtil.UrlEncode(mynameTop)%>"><span>关注的计划</span></a>
            </li>
            <li id="menu4"><a href="workplan_query.jsp"><span>查询计划</span></a></li>
            <%
                if (privilegeInc.isUserPrivValid(request, "admin.workplan")) {
            %>
            <li id="menu7"><a href="workplantype_list.jsp"><span>计划类型</span></a></li>
            <%
                }
                if (privilegeInc.isUserPrivValid(request, "workplan")) {
            %>
            <li id="menu3"><a href="workplan_add.jsp"><span>添加计划</span></a></li>
            <%
                    }
                }
            %>
        </ul>
    </div>
</div>