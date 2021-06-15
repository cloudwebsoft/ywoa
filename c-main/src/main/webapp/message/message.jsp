<%@ page contentType="text/html;charset=utf-8"%>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.forum.message.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<title><lt:Label res="res.label.message.message" key="message_center"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../common.css" type=text/css rel=stylesheet>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request))
{ %>
<p><BR></p>
<table width="320" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
  <tr> 
    <td><li><%=SkinUtil.LoadString(request,"res.label.message.message","msg")%></td>
  </tr>
</table>
<% 
return;
}
String name = privilege.getUser(request);
%>
<table width="320" border="0" cellspacing="1" cellpadding="3" align="center" bgcolor="#99CCFF" class="9black" height="260">
  <tr> 
    <td bgcolor="#CEE7FF" height="23">
      <div align="center"><b><lt:Label res="res.label.message.message" key="message_center"/></b></div>
    </td>
  </tr>
  <tr> 
    <td bgcolor="#FFFFFF" height="50"> 
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr> 
            <td width="75"> 
              <div align="center"><img src="images/m_inbox.gif" width="40" height="40" border="0" alt="<lt:Label res="res.label.message.message" key="recever_mail"/>"></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="mysend.jsp"><img src="images/m_outbox.gif" alt="<lt:Label res="res.label.message.message" key="send_mail"/>" width="40" height="40" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="send.jsp"><img src="images/newpm.gif" width="40" height="40" border="0" alt="<lt:Label res="res.label.message.message" key="write_mail"/>"></a></div>
            </td>
            <td width="75"> 
              <div align="center"> 
                <img border="0" name="imageField" alt="<lt:Label res="res.label.message.message" key="del_mail"/>" src="images/m_delete.gif" width="40" height="40" onClick="form1.submit()" style="cursor:hand">
              </div>
            </td>
          </tr>
        </table>
    </td>
  </tr>
  <tr> 
      <td bgcolor="#FFFFFF" height="152" valign="top">
		<table width="100%"  border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td><table width="100%"  border="0">
  <form name="form1" method="post" action="delmsg.jsp">
              <tr>
                <td>
		<%
		MessageDb md = new MessageDb();
		
		String sql = "select id from message where receiver="+StrUtil.sqlstr(name)+" order by isreaded asc,rq desc";
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
boolean isreaded = true;
int i = 0;
com.redmoon.forum.person.UserMgr um = new com.redmoon.forum.person.UserMgr();
Iterator ir = md.list(sql, (curpage-1)*pagesize, curpage*pagesize-1).iterator();
while (ir.hasNext()) {
 	      md = (MessageDb)ir.next(); 
		  i++;
		  id = md.getId();
		  title = md.getTitle();
		  sender = md.getSender();
		  if (!sender.equals(md.USER_SYSTEM))
		  	sender = um.getUser(sender).getNick();
		  receiver = md.getReceiver();
		  rq = DateUtil.format(md.getRq(), "yyyy-MM-dd HH:mm:ss");
		  type = md.getType();
		  isreaded = md.isReaded();
		 %>
                  <table width="310" border="0" cellspacing="1" cellpadding="3" align="center" class="p9">
                    <tr>
                      <td width="24" ><input type="checkbox" name="ids" value="<%=id%>">
                      </td>
                      <td width="210">&nbsp;<a href="showmsg.jsp?id=<%=id%>" class="9black2">
                        <%if (isreaded) {%>
                        <%=StrUtil.getLeft(title, 22)%>
                        <%}else{%>
                        <b><%=StrUtil.getLeft(title, 20)%></b>
                        <%}%>
                        </a> <font color="#666666"> [<%=sender%>]
                        <!-- <%=rq%>]-->
                      </font></td>
                      <td width="54" ><div align="center">
              <%
			    switch(type) {
			     case 0:
				  { out.print(SkinUtil.LoadString(request,"res.label.message.message","message_person"));
				    break; }
				 case 1:
				  { out.print(SkinUtil.LoadString(request,"res.label.message.message","company_center"));
				    break; }
				 case MessageDb.TYPE_SYSTEM:
				  { out.print(SkinUtil.LoadString(request,"res.label.message.message","message"));
				    break; }
				 case 8:
				  { out.print(SkinUtil.LoadString(request,"res.label.message.message","admin"));
                    break;}
				}
			  %>
                      </div></td>
                    </tr>
                  </table>
                  <%}%></td>
              </tr></form>
            </table>
            </td>
          </tr>
        </table>
<% if(paginator.getTotal()>0){ %>
        <table width="310" border="0" cellspacing="0" cellpadding="0" align="center" class="p9" height="24">
          <tr>
            <td height="24" valign="bottom"> <div align="right"><lt:Label res="res.label.forum.listtopic" key="find_record"/><b><%=paginator.getTotal() %></b> 
                <lt:Label res="res.label.forum.listtopic" key="tiao"/>　<lt:Label res="res.label.forum.listtopic" key="per_page"/><b><%=paginator.getPageSize() %></b> <lt:Label res="res.label.forum.listtopic" key="tiao"/>　<b><%=curpage %>/<%=totalpages %></b>	
              </div>
              <div align="right">
	  <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("message.jsp?"+querystr));
	  %>
	</div>
            </td>
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
