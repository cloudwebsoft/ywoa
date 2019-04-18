<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.debate.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<script src="<%=request.getContextPath()%>/inc/common.js"></script>
<script>
var GetDate=""; 
function SelectDate(ObjName,FormatDate) {
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("<%=request.getContextPath()%>/util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
} 
</script>
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
<TBODY>
      <TR>
        <TD width="20%" align="left" bgcolor="#F9FAF3">开始时间：</TD>
        <TD height=23 align="left" bgcolor="#F9FAF3"><input readonly="readonly" type="text" id="debateBeginDate" name="debateBeginDate" size="10"/>
        <img src="<%=request.getContextPath()%>/util/calendar/calendar.gif" align="absmiddle" style="cursor:hand" onclick="SelectDate('debateBeginDate','yyyy-mm-dd')" />
        <input type="hidden" name="pluginCode" value="<%=DebateUnit.code%>" /></TD>
      </TR>
      <TR>
        <TD align="left" bgcolor="#F9FAF3">结束时间：</TD>
        <TD height=23 align="left" bgcolor="#F9FAF3"><input readonly="readonly" type="text" id="debateEndDate" name="debateEndDate" size="10"/>
        <img src="<%=request.getContextPath()%>/util/calendar/calendar.gif" align="absmiddle" style="cursor:hand" onclick="SelectDate('debateEndDate','yyyy-mm-dd')" /></TD>
      </TR>
      <TR>
        <TD align="left" bgcolor="#F9FAF3">正方：</TD>
        <TD height=23 align="left" bgcolor="#F9FAF3"><textarea name="viewpoint1" cols="30" rows="4"></textarea></TD>
      </TR>
      <TR>
        <TD align="left" bgcolor="#F9FAF3">反方：</TD>
        <TD height=23 align="left" bgcolor="#F9FAF3"><textarea name="viewpoint2" cols="30" rows="4"></textarea></TD>
      </TR>
<%
Privilege activityPriv = new Privilege();
String bCode = ParamUtil.get(request, "boardcode");
if (activityPriv.isManager(request, bCode)) {
%>	
    
<%}%>		
  </TBODY>
</TABLE>
