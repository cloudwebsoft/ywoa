<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.fileark.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.pvg.*"
%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<%
String op = ParamUtil.get(request, "op");
RMCache rmcache = RMCache.getInstance();
Runtime runtime = Runtime.getRuntime();
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

%>
<%!	// global variables

	// decimal formatter for cache values
	static final DecimalFormat mbFormat = new DecimalFormat("#0.00");
	static final DecimalFormat percentFormat = new DecimalFormat("#0.0");
    // variable for the VM memory monitor box
    static final int NUM_BLOCKS = 50;
%>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="backup" scope="page" class="cn.js.fan.util.Backup"/>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<%
if (op.equals("gc")) {
	runtime.gc();
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "cache.jsp"));
	return;
}
String priv="admin";
if (!privilege.isUserPrivValid(request,priv))
{
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="config_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<TABLE class="tabStyle_1 percent80" cellSpacing=0 cellPadding=3 width="95%" align=center>
  <TBODY>
    <TR>
      <TD class="tabStyle_1_title" noWrap width="70%"><font size="-1"><b>Java VM （Java虚拟机）内存</b></font> </TD>
    </TR>
    <TR>
      <TD height="175" align="center">
          <%
    double freeMemory = (double)runtime.freeMemory()/(1024*1024);
	double totalMemory = (double)runtime.totalMemory()/(1024*1024);
	double maxMemory = Runtime.getRuntime().maxMemory()/(1024*1024);
	double usedMemory = totalMemory - freeMemory;
	double percentFree = ((double)freeMemory/(double)totalMemory)*100.0;
    int free = 100-(int)Math.round(percentFree);
%><br>
          <table border=0 style="margin:0px;padding:0px">
            <td style="border:0px;margin:0px;padding:0px"><table style="margin:0px;padding:0px" bgcolor="#000000" cellpadding="1" cellspacing="0" border="0" width="200" align=left>
          <td style="margin:0px;padding:0px"><table style="margin:0px;padding:0px" bgcolor="#000000" cellpadding="1" cellspacing="1" border="0" width="100%">
			<%for (int i=0; i<NUM_BLOCKS; i++) {
				if ((i*(100/NUM_BLOCKS)) < free) {
				%>
				<td style="border:0px;margin:0px;padding:0px;height:15px" bgcolor="#00ff00" width="<%=(100/NUM_BLOCKS) %>%"><img src="images/blank.gif" width="1" height="15" border="0"></td>
				<%} else { %>
				<td style="border:0px;margin:0px;padding:0px;height:15px" bgcolor="#006600" width="<%=(100/NUM_BLOCKS) %>%"><img src="images/blank.gif" width="1" height="15" border="0"></td>
				<%}
			}%>
            </table></td>
              </table></td>
                <td style="border:0px"><font size="-1"> &nbsp;<b><%= percentFormat.format(percentFree) %>% 空闲</b> </font> </td>
          </table>
          <br />
          <table width="422" border="0">
            <tr>
              <td width="85" align="left" style="border:0px">已用内存：</td>
              <td width="108" align="left" style="border:0px"><%= mbFormat.format(usedMemory) %> MB</td>
              <td width="109" align="left" style="border:0px">控件上传：</td>
              <td width="102" align="left" style="border:0px">最大<%=NumberUtil.round((double)Global.MaxSize/1024000, 1)%>M</td>
            </tr>
            <tr>
              <td align="left" style="border:0px">内存总量：</td>
              <td align="left" style="border:0px"><%= mbFormat.format(totalMemory) %> MB</td>
              <td align="left" style="border:0px">单个文件上传：</td>
              <td align="left" style="border:0px">最大<%=NumberUtil.round((double)Global.FileSize/1000, 1)%>M</td>
            </tr>
            <tr>
              <td align="left" style="border:0px">最大内存：</td>
              <td align="left" style="border:0px"><%= mbFormat.format(maxMemory) %> MB</td>
              <td align="left" style="border:0px">
缓存：</td>
              <td align="left" style="border:0px"><%if (rmcache.getCanCache()) {%><a href="cache.jsp?op=stopcache">停用</a>&nbsp;&nbsp;
<%}else{%><a href="cache.jsp?op=startcache">启用</a>&nbsp;&nbsp;
<%}%>
<a href="cache.jsp?op=clear">清除</a></td>
            </tr>
            <tr>
              <td align="left" style="border:0px">处理器数：</td>
              <td align="left" style="border:0px"><%=runtime.availableProcessors()%> 个
              <%
	// Destroy the runtime reference
	runtime = null;
%></td>
              <td align="left" style="border:0px">内存垃圾：</td>
              <td align="left" style="border:0px"><a href="cache.jsp?op=gc">收集</a></td>
            </tr>
          </table>
          <br />
          <table width="465"  border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td height="48" align="center" style="border:0px">(&nbsp;注意垃圾收集器是低线程级的，不能被强制执行&nbsp;)</td>
          </tr>
        </table>
          <br /></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
</html>