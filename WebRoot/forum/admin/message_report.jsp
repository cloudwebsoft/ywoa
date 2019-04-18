<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.message_report" key="message_report_m"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	MessageReportDb mrd = new MessageReportDb();
	mrd = mrd.getMessageReportDb(id);
	boolean re = mrd.del();
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "message_report.jsp"));
		return;
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
		return;
	}
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.message_report" key="message_report_m"/></td>
  </tr>
</table>
<br>
<%
	int pagesize = 20;
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();	
		
	MessageReportDb mrd = new MessageReportDb();
	String userName = privilege.getUser(request);
	String sql = "select id from " + mrd.getTable().getName() + " order by submit_date desc";
	ListResult lr = mrd.listResult(sql, curpage, pagesize);
	
	paginator.init(lr.getTotal(), pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.message_report" key="report_message"/></td>
  </tr>  
  <tr> 
    <td valign="top"><br>
      <table width="95%" border="0" align="center">
      <tr>
        <td align="right"><%=paginator.getPageStatics(request)%></td>
      </tr>
    </table>
      <table width="99%"  border="0" align="center" cellpadding="3" cellspacing="1" bgcolor="#CCCCCC">
      <tr align="center" bgcolor="#F8F7F9">
        <td width="8%" height="24" bgcolor="#EFEBDE"><lt:Label res="res.label.forum.admin.message_report" key="id"/></td>
        <td width="25%" height="24" bgcolor="#EFEBDE"><lt:Label res="res.label.forum.admin.message_report" key="title"/></td>
        <td width="36%" height="24" bgcolor="#EFEBDE"><lt:Label res="res.label.forum.admin.message_report" key="report_reason"/></td>
        <td width="10%" bgcolor="#EFEBDE"><lt:Label res="res.label.forum.admin.message_report" key="report_user_name"/></td>
        <td width="16%" bgcolor="#EFEBDE"><lt:Label res="res.label.forum.admin.message_report" key="report_date"/></td>
        <td width="5%" bgcolor="#EFEBDE"><lt:Label res="res.label.forum.admin.message_report" key="op"/></td>
      </tr>
	<%
    Iterator ir = lr.getResult().iterator();
	while (ir.hasNext()) {
		mrd = (MessageReportDb)ir.next();
		MsgMgr mm = new MsgMgr();
		MsgDb md = mm.getMsgDb(mrd.getLong("msg_id"));
		UserMgr um = new UserMgr();
		UserDb ud = um.getUser(mrd.getString("user_name"));
	%>
      <tr align="center">
        <td height="24" bgcolor="#FFF7FF"><%=mrd.getLong("msg_id")%></td>
      	<td height="24" align="left" bgcolor="#FFF7FF"><a href="../showtopic_tree.jsp?showid=<%=md.getId()%>" target="_blank"><%=md.isLoaded()?DefaultRender.RenderFullTitle(request, md):""%></a></td>
        <td height="24" align="left" bgcolor="#FFF7FF"><%=mrd.getString("report_reason")%></td>
        <td bgcolor="#FFF7FF"><a href="../../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>" target="_blank"><%=ud.getNick()%></a></td>
        <td height="24" bgcolor="#FFF7FF"><%=ForumSkin.formatDateTime(request, mrd.getDate("submit_date"))%></td>
        <td bgcolor="#FFF7FF"><a href="javascript: if (confirm('<%=SkinUtil.LoadString(request, "confirm_del")%>')) window.location.href='message_report.jsp?id=<%=mrd.getInt("id")%>&op=del'"><lt:Label res="res.label.cms.dir" key="del"/></a></td>
      </tr>
	<%}%>
    </table>
      <table width="95%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right">
              <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("message_report.jsp?"+querystr));
	%>          </td>
        </tr>
      </table>
      <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
  