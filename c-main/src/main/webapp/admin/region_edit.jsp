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
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>地域信息 - 修改</title>
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

int id = ParamUtil.getInt(request,"id");
RegionDb d = new RegionDb();
d = d.getRegionDB(id);
%>
<form name="form1" action="region_add_do.jsp?op=edit" method="post">
<input type="hidden" name="id" value="<%=id%>" />
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><a href="region_list.jsp?parentId=<%=d.getInt("parent_id")%>">地区信息</a></td>
       <td align="right" class="tdStyle_1">&nbsp;</td>
    </tr>
  </tbody>
</table>
<TABLE width="100%" align="center" cellPadding=0 cellSpacing=0 class="tabStyle_1 percent60" >
<TBODY>
<TR>
<TD class=tabStyle_1_title colSpan=4>修改地区信息</TD></TR>
<TR>
<TD align=right width="25%">地区类型：</TD>
<TD align=left width="25%" colspan="3">&nbsp;
<select name="region_type">
<option value="1" selected>省</option>
<option value="2">市</option>
<option value="3">区/县</option>
</select>   
<script>
o("region_type").value = "<%=d.getInt("region_type")%>";
</script> 
</TD>
</TR>
<TR>
<TD align=right width="25%">父级：</TD>
<TD align=left width="25%"  colspan="3">&nbsp;
<select name="parent_id">
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
<script>
o("parent_id").value = "<%=d.getInt("parent_id")%>";
</script> 
</select> 
</TD>
</TR>
<TR>
<TD align=right>地区名称：</TD>
<TD align=left colspan="3">&nbsp;&nbsp;<INPUT title=区域名称 name=region_name id="region_name" value="<%=d.getString("region_name")%>">
<script>
var region_name = new LiveValidation('region_name');
region_name.add(Validate.Presence);	
</script></TD>
</TR>
<TR>
  <TD colspan="4" align=center><input type="submit" value="确定" class="btn" /></TD>
  </TR>
</TBODY>
</TABLE>
</form>
</body>
</html>
