<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="java.text.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>地域信息 - 添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
<script src="../inc/livevalidation_standalone.js"></script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int parentId = ParamUtil.getInt(request, "parentId", -1);
%>
<form name="form1" action="region_add_do.jsp?op=add" method="post">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><a href="region_list.jsp?parentId=<%=parentId==-1?"":parentId+""%>">地区信息</a></td>
      </tr>
  </tbody>
</table>
<TABLE width="100%" align="center" cellPadding=0 cellSpacing=0 class="tabStyle_1 percent60">
<TBODY>
<TR>
<TD class=tabStyle_1_title colSpan=4>添加地区信息</TD></TR>
<TR>
<TD align=right width="25%">地区类型：</TD>
<TD align=left width="25%"  colspan="3">&nbsp;
<select name="region_type">
<option value="1"  selected="selected">省</option>
<option value="2" selected>市</option>
<option value="3" selected>区/县</option>
</select></TD>
</TR>
<TR>
<TD align=right width="25%">父级：</TD>
<TD align=left width="25%"  colspan="3">&nbsp;
<select id="parent_id" name="parent_id">
<%
	    String sql = "select region_id,region_name from oa_china_region where region_type<>'3'";
	    JdbcTemplate jd = new JdbcTemplate();
		ResultIterator ri = jd.executeQuery(sql);
		ResultRecord rr = null;
		String region_name ="";
		int region_id = 0;
		while(ri.hasNext()){
			rr  = (ResultRecord)ri.next();
			region_id = rr.getInt(1);
			region_name = rr.getString(2);
		
%>
<option value="<%=region_id%>"><%=region_name%></option>
<%}%>
</select>
<%if (parentId!=-1) {%>
<script>
o("parent_id").value = "<%=parentId%>";
</script> 
<%}%>
</TD>
</TR>
<TR>
<TD align=right>地区名称：</TD>
<TD align=left colspan="3">&nbsp;&nbsp;<INPUT title=区域名称 name=region_name id="region_name" canNull="1" maxV="" maxT="x=" minV="" minT="d=" fieldType="0" >
 <script>
var region_name = new LiveValidation('region_name');
region_name.add(Validate.Presence);	
</script></TD>
</TR>
<TR>
  <TD height="19" colspan="4" align=center><input type="submit" value="确定" class="btn" /></TD>
  </TR>
</TBODY>
</TABLE>
</form>
</body>
</html>
