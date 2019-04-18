<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>

<%

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>销售商机更新记录列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script>

</script>
</head>
<body>

<%	
	int sid = ParamUtil.getInt(request,"sid",-1);
%>
	<table><tr><td>&nbsp;</td></tr></table>
      <table style="width:80%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1 percent98">
        
        <tr>
          <td width="36%" class="tabStyle_1_title">更新时间</td>
          <td width="24%" class="tabStyle_1_title">更新操作人</td>
          <td width="24%" class="tabStyle_1_title">更新操作动作</td>
          <td width="16%" class="tabStyle_1_title">操作</td>
		
        </tr>
      <%
      	String sql = "select * from sales_chance_history where chanceId=?";
      	JdbcTemplate jt = new JdbcTemplate();
      	ResultIterator ri = jt.executeQuery(sql,new Object[]{sid});
      	ResultRecord rd = null;
      	while(ri.hasNext()){
      		rd = (ResultRecord)ri.next();
      %>
        <tr>
          <td align="center"><%=DateUtil.format(rd.getDate("update_date"),"yyyy-MM-dd HH:mm:ss") %></td>
          <td align="center"><%=rd.getString("creator") %></td>
          <td align="center"><%=rd.getString("action") %></td>
          <td align="center"><a href="javascript:;" onclick="addTab('历史记录','sales/sales_chance_history_record.jsp?hid=<%=rd.getInt("hid") %>')">查看</a></td>
        </tr>
	<%} %>
</body>

</html>
