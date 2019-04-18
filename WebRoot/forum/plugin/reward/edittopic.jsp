<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.reward.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<%
long msgId = ParamUtil.getLong(request, "msgId");
MsgDb md = new MsgDb();
md = md.getMsgDb(msgId);
if (md.getReplyid()!=-1) {
	// 回复贴无需编辑
	return;
}
RewardDb rd = new RewardDb();
rd = rd.getRewardDb(msgId);
if (rd.isEnd()) {
 	out.print("已结贴，分值不可编辑");
	return;
}

if (rd.getScoreGiven()>0) {
	out.print("已送出分数，分值不可再编辑");
	return;
}
%>
<script src="<%=request.getContextPath()%>/inc/common.js"></script>
<script>
function selMoneyCode() {
   	var ary = new Array();
	ary[0] = getRadioValue("moneyCode");
	ary[1] = frmAnnounce.sum.value;
	if (ary[0]==null) {
		alert("请选择一个币种！");
		return;
	}
	else {
		if (!isNumeric(frmAnnounce.sum.value)) {
			alert("分值格式错误！");
			return;
		}
	}
}
</script>
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#CCCCCC">
<TBODY>
      <TR>
        <TD width="20%" align="left" bgcolor="#F9FAF3">分值种类：</TD> 
        <TD width="80%" height=23 align="left" bgcolor="#F9FAF3">
<%	  
        ScoreMgr sm = new ScoreMgr();
        Vector v = sm.getAllScore();
        Iterator ir = v.iterator();
        String str = "";
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
			String checked = "";
            if (su.isExchange()) {
				if (rd.getMoneyCode().equals(su.getCode()))
					checked = "checked";
%>
		<input name="moneyCode" type="radio" value="<%=su.getCode()%>" <%=checked%>>
          <%=su.getName()%>
<%	  
          }
      }
%></TD>
    </TR><TR>
  <TD align="left" bgcolor="#F9FAF3">分值：</TD>
  <TD height=23 align="left" bgcolor="#F9FAF3"><input name="sum" size=6 value="<%=rd.getScore()%>" onChange="selMoneyCode()">
    <input type="hidden" name="pluginCode" value="<%=RewardUnit.code%>" /></TD>
</TR>
  </TBODY>
</TABLE>