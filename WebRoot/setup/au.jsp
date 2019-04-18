<%@ page contentType="text/html;charset=utf-8"
import = "com.redmoon.oa.upgrade.service.*"
%>

<%
// 手工调用在线升级
SpringHelper.getBean(IUpgradeService.class).execute(true);
%>