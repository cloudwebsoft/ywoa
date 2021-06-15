<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../../inc/nocache.jsp"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String name = privilege.getUser(request);
MessageDb md = new MessageDb();

String sql = "select id from oa_message where sender="+StrUtil.sqlstr(name)+" and box="+ MessageDb.DRAFT +" order by rq desc";
int pagesize = 10;
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
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>消息中心</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">草稿箱</td>
    </tr>
  </tbody>
</table>
<table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td width="75"><div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><img src="../images/m_draftbox.gif" width="60" height="60" border="0"></div></td>
    <td width="75"><div align="center"><a href="listoutbox.jsp"><img src="../images/m_outbox.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><a href="listrecycle.jsp"><img src="../images/m_recycle.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><a href="send.jsp"><img src="../images/newpm.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><img border="0" name="imageField" src="../images/m_delete.gif" width="60" height="60" onClick="if (confirm('您确定要删除么？')) form1.submit()" style="cursor:hand"></div></td>
  </tr>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
  <tr>
    <td align="right">共 <b><%=paginator.getTotal() %></b> 条　每页<b><%=paginator.getPageSize() %></b> 条　<b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<form name="form1" method="post" action="msg_do.jsp">
  <table width="98%" align="center" class="tabStyle_1 percent98">
    <tr>
      <td width="4%" align="center" class="tabStyle_1_title"><input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td class="tabStyle_1_title" width="28%">标题</td>
      <td class="tabStyle_1_title" width="15%">接收者</td>
      <td class="tabStyle_1_title" width="17%">创建时间</td>
      <td class="tabStyle_1_title" width="7%">定时</td>
      <td class="tabStyle_1_title" width="18%">定时发送时间</td>
      <td width="11%" class="tabStyle_1_title">操作<input type="hidden" name="box" value="<%=MessageDb.DRAFT%>" /></td>
    </tr>
    <%
UserMgr um = new UserMgr();	
while (ir.hasNext()) {
 	      md = (MessageDb)ir.next(); 
		  i++;
		  id = md.getId();
		  title = md.getTitle();
		  sender = md.getSender();
		  receiver = md.getReceiver();
		  rq = md.getRq();
		  type = md.getType();
		  int isSent = md.getIsSent();
		  
		  String receiversAll = md.getReceiversAll();
		  String[] ary = StrUtil.split(receiversAll, ",");
		  String realNames = "";
		  for (int k=0; k<ary.length; k++) {
		  	UserDb user = um.getUserDb(ary[k]);
			if (!user.isLoaded())
				continue;
		  	if (realNames.equals(""))
				realNames = user.getRealName();
			else
				realNames += "," + user.getRealName();
		  }		  
		 %>
    <tr>
      <td align="center"><input type="checkbox" name="ids" value="<%=id%>"></td>
      <td><a href="draft_edit.jsp?id=<%=id%>"><%=title%></a></td>
      <td align="center" style="word-wrap:break-word;word-break:break-all;"><%=realNames%></td>
      <td align="center">
	  <%=rq%>
	  </td>
      <td align="center"><%=isSent==0?"是":"否"%></td>
      <td align="center"><%=isSent==0?md.getSendTime():"-"%></td>
      <td align="center"><a href="draft_edit.jsp?id=<%=id%>">编辑</a></td>
    </tr>
    <%}%>
  </table>
</form>
<% if(paginator.getTotal()>0){ %>
  <table width="98%" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td align="right">
        <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("listdraft.jsp?"+querystr));
	  %>
</td>
  </tr>
</table>
<%}%>
</body>
</html>
