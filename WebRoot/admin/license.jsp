<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.fileark.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.Config,
				 com.redmoon.oa.SpConfig"
%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%
String op = ParamUtil.get(request, "op");
RMCache rmcache = RMCache.getInstance();
Runtime runtime = Runtime.getRuntime();
Config oaCfg = new Config();
SpConfig spCfg = new SpConfig();
if (op.equals("startcache")) {
	rmcache.setCanCache(true);
}
else if (op.equals("stopcache")) {
	rmcache.setCanCache(false);
}
else if (op.equals("clear")) {
	rmcache.clear();
}
else if (op.equals("refreshfulltext")) {
	DocCacheMgr dcm = new DocCacheMgr();
	dcm.refreshFulltext();
}
else if (op.equals("reloadConfig")) {
	Global.init();
}
else if (op.equals("gc")) {
	runtime.gc();
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "cache.jsp"));
	return;
}
String version = StrUtil.getNullStr(oaCfg.get("version"));
String spVersion = StrUtil.getNullStr(spCfg.get("version"));
%>
<%!
	static final DecimalFormat mbFormat = new DecimalFormat("#0.00");
	static final DecimalFormat percentFormat = new DecimalFormat("#0.0");
    // variable for the VM memory monitor box
    static final int NUM_BLOCKS = 50;
%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="backup" scope="page" class="cn.js.fan.util.Backup"/>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    // out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
%>
<%@ include file="config_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<br />
<div class="spacerH"></div>
<table width="53%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tbody>
      <tr>
        <td colspan="2" align="left" class="tabStyle_1_title">&nbsp;许可证信息 </td>
      </tr>
      <tr>
        <td width="17%" align="left">授权单位</td>
        <td width="83%" align="left">
        <%
	  	License license = License.getInstance();
	  	%>
        <%=license.getCompany()%>
        </td>
      </tr>
      <tr>
        <td width="17%" align="left">使用单位</td>
        <td width="83%" align="left">
        <%=license.getName()%>
        </td>
      </tr>
      <tr>
        <td align="left">企业号</td>
        <td align="left"><%=license.getEnterpriseNum() != null ? license.getEnterpriseNum() : license.getName()%></td>
      </tr>
      <tr>
        <td align="left">用户数</td>
        <td align="left"><%=license.getUserCount()%></td>
      </tr>
      <tr>
        <td align="left">类型</td>
        <td align="left"><%=license.getType().equals(License.TYPE_COMMERICAL) ? "免费版" : license.getType()%></td>
      </tr>
      <tr>
        <td align="left">到期时间</td>
        <td align="left"><%=DateUtil.format(license.getExpiresDate(), "yyyy-MM-dd")%></td>
      </tr>
      <tr>
          <td align="left">域名</td>
          <td align="left"><%=license.getDomain()%></td>
      </tr>
      <tr>
          <td align="left">流程最大节点数</td>
          <td align="left"><%=license.getActionCount()%></td>
      </tr>
      <%--<tr>--%>
          <%--<td align="left">许可证版本</td>--%>
          <%--<td align="left"><%=license.getVersion()%></td>--%>
      <%--</tr>--%>
      <tr>
        <td align="left">系统版本</td>
        <td align="left"><%=version%></td>
      </tr>
      <tr>
        <td align="left">补丁版本</td>
        <td align="left"><%=spVersion%></td>
      </tr>
    </tbody>
</table>
</body>
</html>