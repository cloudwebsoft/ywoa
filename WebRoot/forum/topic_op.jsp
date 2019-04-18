<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.topic_op" key="topic_op"/> - <%=Global.AppName%></title>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div class="tableTitle"><lt:Label res="res.label.forum.topic_op" key="topic_op"/></div>
<%
		long msgId = ParamUtil.getLong(request, "msgId");
		MsgOperateDb mod = new MsgOperateDb();
		
		String op = ParamUtil.get(request, "op");
		if (op.equals("del")) {
			long id = ParamUtil.getLong(request, "id");
			mod = mod.getMsgOperateDb(id);
			if (mod!=null) {
				if (mod.del()) {
					out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "topic_op.jsp?msgId=" + msgId));
				}
			}
			return;
		}
		
		int pagesize = 20;
		String sql = "select id from " + mod.getTable().getName() + " where msg_id=" + msgId + " ORDER BY op_date desc";
					
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		ListResult lr = mod.listResult(new JdbcTemplate(), sql, curpage, pagesize);
		int total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (v!=null)
		ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
<TABLE class="tableCommon" border=1 align=center cellPadding=3 cellSpacing=0>
    <thead>
      <TR align=center> 
        <TD width=192 height=23><strong>
          <lt:Label res="res.label.forum.topic_op" key="operator"/>
        </strong></TD>
        <TD width=197 height=23><strong>
          <lt:Label res="res.label.forum.topic_op" key="date"/>
        </strong></TD>
        <TD width=160 height=23><strong>
          <lt:Label res="res.label.forum.topic_op" key="op"/>
        </strong></TD>
        <TD width=201 height=23><strong>
          <lt:Label res="res.label.forum.topic_op" key="expire"/>
        </strong></TD>
      </TR>
    </thead>
   <%	
UserMgr um = new UserMgr();   	
while (ir.hasNext()) {
 	    mod = (MsgOperateDb)ir.next(); 
%>
      <TR align=center bgColor=#f8f8f8> 
        <TD width=192 height=23 align="left">
		<%
		if (mod.getString("user_name").equals(MsgOperateDb.OPERATOR_MASTER))
			out.print("Administrator");
		else {
			UserDb user = um.getUser(mod.getString("user_name"));
		%>
			<%=user.getNick()%>
		<%}%>
		</TD>
        <TD width=197 height=23>
		<%=ForumSkin.formatDateTime(request, mod.getDate("op_date"))%>
		</TD>
        <TD width=160 height=23><%=mod.getOperate(request)%></TD>
        <TD width=201 height=23>
		<%
		if (mod.getDate("expire_date")!=null)
			out.print(ForumSkin.formatDateTime(request, mod.getDate("expire_date")));
		%>&nbsp;
		<%
		if (privilege.isMasterLogin(request)) {
		%>
			<a href="#" onClick="if (confirm('<lt:Label key="confirm_del"/>')) window.location.href='topic_op.jsp?op=del&id=<%=mod.getLong("id")%>&msgId=<%=msgId%>'"><lt:Label key="op_del"/></a>
		<%}
		%>
		</TD>
      </TR>
<%}%>
    </TBODY>
  </TABLE>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
    <tr> 
      <td width="2%" height="23">&nbsp;</td>
      <td height="23" align="right">
    <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock(request, "top_op.jsp?"+querystr));
	%>
	</td>
    </tr>
  </table>
</div>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
