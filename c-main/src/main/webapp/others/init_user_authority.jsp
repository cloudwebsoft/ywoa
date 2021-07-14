<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.cloudweb.oa.service.IUserAuthorityService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="java.util.Iterator" %>
<%
    IUserAuthorityService userAuthorityService = SpringUtil.getBean(IUserAuthorityService.class);
    UserDb user = new UserDb();
    Iterator ir = user.list().iterator();
    while (ir.hasNext()) {
        user = (UserDb) ir.next();
        userAuthorityService.refreshUserAuthority(user.getName());
    }
%>
<title>初始化用户权限</title>
初始化用户权限结束！