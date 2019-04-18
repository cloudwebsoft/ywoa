<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.message.*"%>
<%@ page import="java.util.*"%>
<html>
<head>
<title>消息中心</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../common.css" type=text/css rel=stylesheet>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/><%
if (!privilege.isUserLogin(request))
{ %>
<table width="320" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
  <tr> 
    <td><li>您的登录已过期，请重新登录，如果不是会员请先注册。</td>
  </tr>
</table>
<% } 
String name = privilege.getUser(request);
%>
<table width="320" border="0" cellspacing="1" cellpadding="3" align="center" bgcolor="#99CCFF" class="9black" height="260">
  <tr> 
    <td bgcolor="#CEE7FF" height="23">
        <div align="center"><b>发 件 箱</b></div>
    </td>
  </tr>
  <tr> 
    <td bgcolor="#FFFFFF" height="50"> 
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr> 
            <td width="75"> 
              <div align="center"><a href="message.jsp?page=1"><img src="images/inboxpm.gif" width="40" height="40" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><img src="images/m_outbox.gif" width="40" height="40" border="0"></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="send.jsp"><img src="images/newpm.gif" width="40" height="40" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"> <img src="images/m_delete.gif" width="40" height="40"></div>
            </td>
          </tr>
        </table>
    </td>
  </tr>
  <tr> 
      <td bgcolor="#FFFFFF" height="152" valign="top">
	  <table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <form name="form1" method="post" action="delmsg.jsp">
        <tr>
          <td><%
		MessageDb md = new MessageDb();
		  
		String sql = "select id from message where sender="+StrUtil.sqlstr(name)+" order by isreaded asc,rq desc";
		int pagesize = 5;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		int total = md.getObjectCount(sql);
		paginator.init(total, pagesize);
		//设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}

int id,type;
String title="",sender="",receiver="",rq="";
String bg = "";
int i = 0;
Iterator ir = md.list(sql, (curpage-1)*pagesize, curpage*pagesize-1).iterator();
while (ir.hasNext()) {
 	      md = (MessageDb)ir.next(); 
		  i++;
		  id = md.getId();
		  title = md.getTitle();
		  sender = md.getSender();
		  receiver = md.getReceiver();
		  rq = DateUtil.format(md.getRq(), "yyyy-MM-dd HH:mm:ss");
		  type = md.getType();
		 %>
            <table width="300" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
              <%
		      if(i%2==0)
			   bg="#E6F7FF";
			  else
			   bg="#ffffff"; 
			   i++; %>
              <tr bgcolor="<%=bg%>">
                <td >·<a href="showmsg.jsp?id=<%=id%>" class="9black2"> <%=title%></a> </td>
                <td width="54" ><div align="center">
                    <%
			    switch(type) {
			     case 0:
				  { out.print("个人消息");
				    break; }
				 case 1:
				  { out.print("公共消息");
				    break; }
				 case 2:
				  { out.print("简历回复");
				    break; }
				 case 3:
				  { out.print("面试通知");
                    break;}
				}
			  %>
                </div></td>
              </tr>
            </table>
            <%}%></td>
        </tr></form>
      </table>
<% if(paginator.getTotal()>0){ %>
        <table width="310" border="0" cellspacing="0" cellpadding="0" align="center" class="p9" height="24">
          <tr> 
            <td height="24" valign="bottom"> <div align="right">共 <b><%=paginator.getTotal() %></b> 
                条　每页<b><%=paginator.getPageSize() %></b> 条　<b><%=curpage %>/<%=totalpages %></b>	
              </div>
              <div align="right"> 
                <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("mysend.jsp?"+querystr));
	  %>
		</div></td>
          </tr>
        </table>
        <%}%>
      </td>
  </tr>
  <tr> 
    <td bgcolor="#CEE7FF" height="6"></td>
  </tr>
</table>
</body>
</html>
