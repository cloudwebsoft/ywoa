<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.present.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, "请先登录!"));
	return;
}
%>
<%
long msgId = ParamUtil.getLong(request, "msgId");
MsgMgr mm = new MsgMgr();
MsgDb md = mm.getMsgDb(msgId);
if (!md.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该贴不存在!"));
	return;
}
UserMgr um = new UserMgr();
UserDb user = um.getUser(md.getName());
if (!user.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该用户不存在！"));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("give")) {
	PresentMsgAction rma = new PresentMsgAction();
	try {
		boolean re = rma.give(request);
		if (re) {
			out.print(StrUtil.Alert("操作成功！"));
			out.print("<script>window.close()</script>");
			return;
		}
		else
			out.print(StrUtil.Alert_Back("操作失败！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link href="../../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<form name="form1" action="give.jsp?op=give" method="post">
<table class="tableCommon" width="100%" border="0" align="center" cellpadding="2" cellspacing="1">
  <thead>
    <tr>
      <td colspan="2" align="center" >
	  赠分给 <%=user.getNick()%></td>
    </tr>
	</thead>
    <%	  
        ScoreMgr sm = new ScoreMgr();
        Vector v = sm.getAllScore();
        Iterator ir = v.iterator();
        String str = "";
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
            if (su.isExchange()) {
%>
    <tr>
      <td width="2%" height="23" align="center"><input name="moneyCode" type="radio" value="<%=su.getCode()%>" /></td>
      <td width="98%" align="left"><%=su.getName(request)%>&nbsp;(您的余额：<%=(int)su.getScore().getUserSum(privilege.getUser(request))%>)</td>
    </tr>
    <%	  
          }
      }
%>
    <tr>
      <td height="23" colspan="2" align="left"><lt:Label res="res.label.forum.point_sel" key="sum"/>
          <input name="score" id="score" size="6" />
		  <input name="msgId" value="<%=msgId%>" type="hidden" />
	  原因：
	  <input name="reason" id="reason" size="36" /></td>
    </tr>
    <tr>
      <td height="23" colspan="2" align="center"><input name="button" type="submit" value="<lt:Label key="ok"/>" />
        &nbsp;&nbsp;&nbsp;&nbsp;
        <input name="button" type="button" value="取消" /></td>
    </tr>
  </tbody>
</table>
</form>
</body>
</head>