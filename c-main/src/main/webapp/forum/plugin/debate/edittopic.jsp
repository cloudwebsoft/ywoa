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

function SetDate() { 
	findObj(ObjName).value = GetDate; 
} 
</script>
<%
long msgId = ParamUtil.getLong(request, "editid");
MsgMgr mm = new MsgMgr();
MsgDb md = mm.getMsgDb(msgId);
if (md.isRootMsg()) {
DebateDb atd = new DebateDb();
atd = atd.getDebateDb(msgId);
if (!atd.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该贴不是辩论贴！"));
	return;
}
%>
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
  <tr>
    <td width="20%" align="left" bgcolor="#F9FAF3">开始时间：</td>
    <td height="23" align="left" bgcolor="#F9FAF3"><input readonly="readonly" type="text" id="debateBeginDate" name="debateBeginDate" size="10" value="<%=DateUtil.format(atd.getBeginDate(), "yyyy-MM-dd")%>"/>
        <img src="<%=request.getContextPath()%>/util/calendar/calendar.gif" align="absmiddle" style="cursor:hand" onclick="SelectDate('debateBeginDate','yyyy-mm-dd')" />
        <input type="hidden" name="pluginCode" value="<%=DebateUnit.code%>" /></td>
  </tr>
  <tr>
    <td align="left" bgcolor="#F9FAF3">结束时间：</td>
    <td height="23" align="left" bgcolor="#F9FAF3"><input readonly="readonly" type="text" id="debateEndDate" name="debateEndDate" size="10" value="<%=DateUtil.format(atd.getEndDate(), "yyyy-MM-dd")%>"/>
        <img src="<%=request.getContextPath()%>/util/calendar/calendar.gif" align="absmiddle" style="cursor:hand" onclick="SelectDate('debateEndDate','yyyy-mm-dd')" /></td>
  </tr>
  <tr>
    <td align="left" bgcolor="#F9FAF3">正方：</td>
    <td height="23" align="left" bgcolor="#F9FAF3"><textarea name="viewpoint1" cols="30" rows="4"><%=atd.getViewpoint1()%></textarea></td>
  </tr>
  <tr>
    <td align="left" bgcolor="#F9FAF3">反方：</td>
    <td height="23" align="left" bgcolor="#F9FAF3"><textarea name="viewpoint2" cols="30" rows="4"><%=atd.getViewpoint2()%></textarea></td>
  </tr>
</TABLE>
<%}else{
	DebateViewpointDb dvd = new DebateViewpointDb();
	dvd = dvd.getDebateViewpointDb(msgId);
%>
<table width="100%">
  <tr>
    <td>选择支持哪一方：
	<input name="viewpoint_type" value="<%=dvd.getType()%>" type="hidden" />
      <select name="viewpoint_type2" disabled>
        <option value="<%=DebateViewpointDb.TYPE_SUPPORT%>">正方</option>
        <option value="<%=DebateViewpointDb.TYPE_OPPOSE%>">反方</option>
        <option value="<%=DebateViewpointDb.TYPE_OTHERS%>">第三方</option>
      </select>
      <input type="hidden" name="pluginCode" value="<%=DebateUnit.code%>" />
	  <script>
	  frmAnnounce.viewpoint_type2.value = "<%=dvd.getType()%>";
	  </script>
	  </td>
  </tr>
</table>
<%}%>