<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.message.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.group_send_message" key="group_send_message"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("send")) {
	MessageMgr mm = new MessageMgr();
	try{
		if (!mm.AddGroupMsg(request)){
			out.println(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
		}
		else{
			out.println(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
		}
	}catch(ErrMsgException e){
		out.println(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.group_send_message" key="group_send_message"/></td>
  </tr>
</table>
<br>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <form name="form1" method="post" action="group_send_message.jsp?op=send">
  <TBODY>
    <TR>
		<TD colspan="2" align="left" class="thead"><lt:Label res="res.label.forum.admin.group_send_message" key="group_send_message"/></TD>
    </TR>
    <TR>
      <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.group_send_message" key="user_name"/></TD>
      <TD bgcolor="#FFFFFF"><INPUT size=30 name="receiver"></TD>
    </TR>
    <TR>
      <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.group_send_message" key="user_group"/></TD>
      <TD bgcolor="#FFFFFF">
	    <input type="checkbox" name="receivers" value="isToAll"><lt:Label res="res.label.forum.admin.group_send_message" key="is_to_all"/><br>
<%
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
%>
		<input type="checkbox" name="receivers" value="<%=ug.getCode()%>"><%=ug.getDesc()%><br>
<%
}
%>
		<input type="checkbox" name="receivers" value="boardManager"><lt:Label res="res.label.forum.admin.group_send_message" key="board_manager"/>
		</TD>
    </TR>
    <TR>
      <TD width="35%" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.group_send_message" key="title"/></TD>
      <TD width="65%" bgcolor="#FFFFFF"><INPUT size=30 name="title"></TD>
    </TR>
    <TR>
      <TD bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.group_send_message" key="content"/></TD>
      <TD bgcolor="#FFFFFF"><textarea name="content" rows=6 style="width:98%"></textarea></TD>
    </TR>
    <TR>
    	<TD colspan="2" bgcolor="#FFFFFF" align="center">
    	  <input type="submit" name="Submit" value="<lt:Label res="res.label.forum.admin.group_send_message" key="send"/>"></TD>
	</TR>
  </TBODY>
  </form>
</TABLE>
</body>                                        
</html>                            
  