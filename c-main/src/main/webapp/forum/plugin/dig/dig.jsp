<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<%@ page import="com.redmoon.forum.plugin.dig.*"%>
<%@ page import="org.jdom.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);

long msgId = ParamUtil.getLong(request, "msgId");

String op = ParamUtil.get(request, "op");
if (op.equals("dig")) {
	DigMsgAction dma = new DigMsgAction();
	boolean re = false;
	try {
		re = dma.dig(request);
		if (re) {
			out.print(StrUtil.Alert("挖掘成功! 点击确定将关闭本窗口"));
%>
<script>
window.close();
</script>
<%			
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	catch (ResKeyException e) {
		out.print(StrUtil.Alert_Back(e.getMessage(request)));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title>顶</title>
<script src="../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
DigConfig dc = DigConfig.getInstance();
MsgDb md = new MsgDb();
md = md.getMsgDb(msgId);
if (privilege.isUserLogin(request)) {
	if (!dc.getBooleanProperty("canDigSelf")) {
		if (privilege.getUser(request).equals(md.getName())) {
			out.print(SkinUtil.makeErrMsg(request, "您不能挖掘自己的贴子!"));
			return;
		}
	}
}
else {
	out.print(SkinUtil.makeErrMsg(request, "请先登录!"));
	return;
}
%>
<form name="form1" action="dig.jsp" method="post">
<TABLE width="100%" border=0 align=center cellPadding=2 cellSpacing=1 class="tableCommon">
<thead>
      <TR> 
        <TD height=23 colspan="2" align="center" class="td_title">请选择使用何种积分挖贴，贴子当前分值<%=NumberUtil.round(md.getScore(), 3)%></TD>
      </TR>
</thead>
<TBODY>
      <TR>
        <TD height=23 colspan="2" align="center"><table width="442" cellspacing="0" cellpadding="4">
          <tr>
            <td width="112">积分种类</td>
            <td width="166">挖掘一次消耗</td>
            <td width="140">贴子将得分</td>
          </tr>
          <%
  ScoreMgr sm = new ScoreMgr();		  
  Element root = dc.getRoot();
  Iterator ir = root.getChild("dig").getChildren("score").iterator();
  while (ir.hasNext()) {
  	Element e = (Element)ir.next();
	String type = e.getAttributeValue("type");
	ScoreUnit su = sm.getScoreUnit(type);
	String pay = e.getChildText("pay");
	String reward = e.getChildText("reward");
%>
          <tr>
            <td><%=su.getName(request)%></td>
            <td><%=pay%></td>
            <td><%=reward%></td>
            <%
  }
  %>
          </tr>
        </table></TD>
      </TR>
      <TR>
        <TD height=23 colspan="2" align="center">分值种类：
          <%	  
        Vector v = sm.getAllScore();
        ir = v.iterator();
        String str = "";
%>
<select name="scoreCode">
<%			
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
            if (su.isExchange()) {
%>
      <option value="<%=su.getCode()%>">
      <%=su.getName(request)%>      </option>
<%	  
          }
%>
<%		  
      }
%>     		
</select>
<span class="td_title"></span><img src="images/dig.gif" width="15" height="15">
<input name="msgId" type="hidden" value="<%=msgId%>">
<input name="op" type="hidden" value="dig"></TD>
      </TR>

<TR>
        <TD height=23 colspan="2" align="center">
		<input type="submit" value="<lt:Label key="ok"/>">
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input name="button" type="button" onClick="window.close()" value="<lt:Label key="cancel"/>"></TD>
      </TR>    </TBODY>
</TABLE>
</form>	  
</body>
</html>
