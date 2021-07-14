<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*,
				 java.text.*,
				 com.redmoon.blog.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.fileark.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.meeting.*"
%>
<HTML><HEAD><TITLE>会议室列表</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="default.css" type=text/css rel=stylesheet>
<META content="MSHTML 6.00.3790.259" name=GENERATOR>
<style type="text/css">
<!--
.style1 {	font-size: 14px;
	font-weight: bold;
}
-->
</style>
</HEAD>
<BODY text=#000000 bgColor=#eeeeee leftMargin=0 topMargin=0>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
  <TR>
    <TD class=head>会议室管理</TD>
  </TR></TBODY></TABLE>
<br>
<%
int pagesize = 10;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

BoardroomDb bd = new BoardroomDb();
String sql = "select id from " + bd.getTableName();
int total = bd.getObjectCount(sql);
paginator.init(total, pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr>
    <td height=20 align="center" class="thead style1">会议室列表</td>
  </tr>
  <tr>
    <td valign="top"><br>
        <table width="86%" height="24" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td align="right"><div>找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></div></td>
          </tr>
        </table>
      <table width="86%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#666666"">
          <tr align="center" bgcolor="#F1EDF3">
            <td width="17%" height="22">名称</td>
            <td width="22%" height="22">人数</td>
            <td width="21%">地点</td>
            <td width="26%">操作</td>
          </tr>
          <%
Vector v = bd.list(sql, (curpage-1)*pagesize, curpage*pagesize-1);
Iterator ir = v.iterator();
int i = 0;
while (ir.hasNext()) {
	bd = (BoardroomDb)ir.next();
	i++;
%>
          <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method="post">
            <tr align="center">
              <td height="22" bgcolor="#FFFFFF"><a target=_blank href="../myblog.jsp?userName"><%=bd.getName()%></a>              </td>
              <td height="22" bgcolor="#FFFFFF"><%=bd.getPersonNum()%></td>
              <td bgcolor="#FFFFFF"><%=bd.getAddress()%></td>
              <td height="22" bgcolor="#FFFFFF"><a href="boardroom_edit.jsp?id=<%=bd.getId()%>">编辑</a>&nbsp;&nbsp;<a title="删除该会议室" href="boardroom_do.jsp?op=del&id=<%=bd.getId()%>">删除</a>&nbsp;&nbsp;<a href="../boardroom_status.jsp?boardroomId=<%=bd.getId()%>">使用情况</a></td>
            </tr>
          </form>
          <%}%>
      </table>
      <table width="86%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr>
            <td height="23"><div align="right">
                <%
	String querystr = "";
    out.print(paginator.getCurPageBlock("?"+querystr));
%>
            </div></td>
          </tr>
      </table>
      <DIV style="WIDTH: 92%" align=right>
        <INPUT name="image" type=image onClick="javascript:location.href='boardroom_add.jsp'" src="images/btn_add.gif" width=80 height=20>
      </DIV></td>
  </tr>
</table>
<br>
</BODY></HTML>
