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
<%@ page import="com.redmoon.forum.plugin.activity.*"%>
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
        <TD width="20%" align="left" bgcolor="#F9FAF3">截止时间：</TD>
        <TD height=23 align="left" bgcolor="#F9FAF3"><input readonly="readonly" type="text" id="activityExpireDate" name="activityExpireDate" size="10"/>
        <input type="hidden" name="pluginCode" value="<%=ActivityUnit.code%>" />
        <img src="<%=request.getContextPath()%>/util/calendar/calendar.gif" align="absmiddle" style="cursor:hand" onclick="SelectDate('activityExpireDate','yyyy-mm-dd')" /></TD>
      </TR>
      <TR>
        <TD align="left" bgcolor="#F9FAF3">联系电话：</TD>
        <TD height=23 align="left" bgcolor="#F9FAF3"><input name="tel" value=""/></TD>
      </TR>
      <TR>
        <TD align="left" bgcolor="#F9FAF3">组织者：</TD>
        <TD height=23 align="left" bgcolor="#F9FAF3"><input name="organizer" value=""/></TD>
      </TR>
    <TR>
      <TD align="left" bgcolor="#F9FAF3">参与者人数：</TD>
      <TD height=23 align="left" bgcolor="#F9FAF3"><input name="userCount" size="6" value="-1"/>
        (-1表示不限)</TD>
    </TR>
    <TR>
      <TD align="left" bgcolor="#F9FAF3">参与者等级：</TD>
      <TD height=23 align="left" bgcolor="#F9FAF3">
	  <select name="userLevel">
	  
<%
	UserLevelDb uld = new UserLevelDb();
	Vector v = uld.getAllLevel();
	Iterator ir = v.iterator();
	int i = 0;
	while (ir.hasNext()) {
		i ++;
		uld = (UserLevelDb)ir.next();
%>
<option value="<%=uld.getLevel()%>"><%=uld.getDesc()%></option>
<%		
	}
%>	  </select>
	  </TD>
    </TR>
<%
Privilege activityPriv = new Privilege();
String bCode = ParamUtil.get(request, "boardcode");
if (activityPriv.isManager(request, bCode)) {
%>	
      <TR>
        <TD align="left" bgcolor="#F9FAF3">参与计分币种：</TD> 
        <TD height=23 align="left" bgcolor="#F9FAF3">
		<input name="moneyCode" type="radio" value="" checked>不计分
<%	  
        ScoreMgr sm = new ScoreMgr();
        v = sm.getAllScore();
        ir = v.iterator();
        String str = "";
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
            if (su.isExchange()) {
%>
			<input name="moneyCode" type="radio" value="<%=su.getCode()%>">
    	    <%=su.getName()%>
<%	  
          }
      }
%></TD>
    </TR><TR>
  <TD align="left" bgcolor="#F9FAF3">参与分值：</TD>
  <TD height=23 align="left" bgcolor="#F9FAF3"><input name="attendMoneyCount" size=6 value=""/></TD>
</TR>
    <TR>
      <TD align="left" bgcolor="#F9FAF3">退出分值：</TD>
      <TD height=23 align="left" bgcolor="#F9FAF3"><input name="exitMoneyCount" size="6" value=""/></TD>
    </TR>
<%}%>		
  </TBODY>
</TABLE>
