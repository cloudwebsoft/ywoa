<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.forum.person.*" %>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="com.redmoon.forum.plugin.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="GENERATOR" content="Microsoft FrontPage 4.0">
<meta name="ProgId" content="FrontPage.Editor.Document">
<LINK href="../common.css" type=text/css rel=stylesheet>
<LINK href="default.css" type=text/css rel=stylesheet>
<title><lt:Label res="res.label.forum.admin.user_m" key="user_manage"/></title>
<SCRIPT language=javascript>
<!--
function form1_onsubmit() {
	if (form1.pwd.value!=form1.pwd_confirm.value) {
		alert('<lt:Label res="res.label.forum.admin.user_m" key="pwd_not_equal"/>');
		return false;
	}
}

function payForDownloadAtt() {
	var ary = showModalDialog('../point_sel.jsp',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;');
	if (ary==null)
		return;
	var moneyCode = ary[0];
	var sum = ary[1];
}
//-->
</script></head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="us" scope="page" class="com.redmoon.forum.person.userservice"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="prision" scope="page" class="com.redmoon.forum.life.prision.Prision"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String op = StrUtil.getNullString(request.getParameter("op"));
String groupCode = ParamUtil.get(request, "groupCode");
String boardCode = ParamUtil.get(request, "boardCode");

if (groupCode.equals("")) {
	out.print(SkinUtil.makeInfo(request, SkinUtil.LoadString(request, "res.label.forum.admin.user_group_m", "need_code")));
	return;
}

Leaf lf = new Leaf();
if (!boardCode.equals("") && !boardCode.equals(UserGroupPrivDb.ALLBOARD)) {
	lf = lf.getLeaf(boardCode);
	if (!boardCode.equals(Leaf.CODE_ROOT) && lf.getType()==lf.TYPE_DOMAIN) {
		out.print(SkinUtil.makeInfo(request, SkinUtil.LoadString(request, "res.label.forum.admin.user_group_m", "need_boardcode")));
		return;
	}
}

if (boardCode.equals("") || boardCode.equals(Leaf.CODE_ROOT))
	boardCode = UserGroupPrivDb.ALLBOARD;

UserGroupDb ugd = new UserGroupDb();
ugd = ugd.getUserGroupDb(groupCode);

UserGroupPrivDb upd = new UserGroupPrivDb();
upd = upd.getUserGroupPrivDb(groupCode, boardCode);

if (op.equals("priv")) {
	QObjectMgr qom = new QObjectMgr();
	if (qom.save(request, upd, "sq_user_group_priv_save"))
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}

if (op.equals("priv_reset")) {
	upd.del();
	upd.init(groupCode, boardCode);
	upd = upd.getUserGroupPrivDb(groupCode, boardCode);
}
%>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><lt:Label res="res.label.forum.admin.user_group_m" key="user_group"/><%=ugd.getDesc()%>
	  &nbsp;&nbsp;<lt:Label res="res.label.forum.admin.user_group_m" key="board"/>
	  <%if (!boardCode.equals(UserGroupPrivDb.ALLBOARD)) {%>
	  	<%=lf.getName()%>
	  <%}else{%>
	  	<lt:Label res="res.label.forum.admin.user_group_m" key="allboard"/>
	  <%}%>
	  &nbsp;&nbsp;
	  <lt:Label res="res.label.forum.admin.user_group_m" key="privilege"/></TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD align="center" style="PADDING-LEFT: 10px"><br>
<%if (boardCode.equals(UserGroupPrivDb.ALLBOARD)) {%>
<table width="550" border="0" align="center" class="frame_gray">
          <form name=form_priv action="user_group_priv.jsp?op=priv" method="post">
            <tr>
              <td width="5%">&nbsp;</td>
              <td width="95%" colspan="2" align="left"><input name="groupCode" type=hidden value="<%=groupCode%>">
<input name="boardCode" type=hidden value="<%=boardCode%>">
<input name="search" type="checkbox" value=1 <%=upd.getBoolean("search")?"checked":""%>>&nbsp;<lt:Label res="res.forum.person.UserPrivDb" key="search"/>
<input name="view_topic" type="checkbox" value=1 <%=upd.getBoolean("view_topic")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="view_topic"/>
<br>
<input name="is_default" value="1" type="checkbox" <%=upd.getBoolean("is_default")?"checked":""%>>&nbsp;<lt:Label res="res.forum.person.UserPrivDb" key="is_default"/></td>
            </tr>
            <tr>
              <td>&nbsp;</td>
              <td colspan="2" align="left">-----------------------------------------------------------------<br>
                <input name="view_listmember" type="checkbox" value=1 <%=upd.getBoolean("view_listmember")?"checked":""%>>
                <lt:Label res="res.forum.person.UserPrivDb" key="view_listmember"/>
                <input name="view_online" type="checkbox" value=1 <%=upd.getBoolean("view_online")?"checked":""%>>
                <lt:Label res="res.forum.person.UserPrivDb" key="view_online"/>
				<input name="view_userinfo" type="checkbox" value=1 <%=upd.getBoolean("view_userinfo")?"checked":""%>>
                <lt:Label res="res.forum.person.UserPrivDb" key="view_userinfo"/>				
                <lt:Label res="res.forum.person.UserPrivDb" key="disk_space_allowed"/>				
				<input name="disk_space_allowed" size=10 value="<%=upd.get("disk_space_allowed")%>">
				</td>
            </tr>
            <tr>
              <td>&nbsp;</td>
              <td colspan="2" align="center">
                  <input name="submit23" type=submit value="<lt:Label key="ok"/>">
                  &nbsp;&nbsp;&nbsp;&nbsp;
                  <input name="submit23" type=button value="<lt:Label res="res.label.forum.admin.user_m" key="reset_privilege"/>" onclick="window.location.href='?groupCode=<%=StrUtil.UrlEncode(groupCode)%>&boardCode=<%=StrUtil.UrlEncode(boardCode)%>&op=priv_reset'">			  </td>
            </tr>
          </form>
        </table>
<%} else if (groupCode.equals("dd" + UserGroupDb.GUEST)) {%>
        <table width="550" border="0" align="center" class="frame_gray">
          <form name=form_priv action="user_group_priv.jsp?op=priv" method="post">
            <tr>
              <td width="5%">&nbsp;</td>
              <td width="95%" colspan="2" align="left"><input name="enter_board" type="checkbox" value=1 <%=upd.getBoolean("enter_board")?"checked":""%>>
                <lt:Label res="res.forum.person.UserPrivDb" key="enter_board"/>
&nbsp;
<input name="view_topic" type="checkbox" value=1 <%=upd.getBoolean("view_topic")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="view_topic"/>
&nbsp;
<input name="attach_download" type="checkbox" value=1 <%=upd.getBoolean("attach_download")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="attach_download"/>
<input name="groupCode" type=hidden value="<%=groupCode%>">
<input name="boardCode" type=hidden value="<%=boardCode%>"></td>
            </tr>
            <tr>
              <td>&nbsp;</td>
                <td colspan="2" align="center">
                  <input name="submit23" type=submit value="<lt:Label key="ok"/>">
                  &nbsp;&nbsp;&nbsp;&nbsp;
                  <input name="submit23" type=button value="<lt:Label res="res.label.forum.admin.user_m" key="reset_privilege"/>" onclick="window.location.href='?groupCode=<%=StrUtil.UrlEncode(groupCode)%>&boardCode=<%=StrUtil.UrlEncode(boardCode)%>&op=priv_reset'">			  </td>
            </tr>
          </form>
        </table>
<%} else {%>
        <table width="550" border="0" align="center" class="frame_gray">
          <form name=form_priv action="user_group_priv.jsp?op=priv" method="post">
            <tr>
              <td width="5%">&nbsp;</td>
              <td width="95%" colspan="2" align="left"><input name="enter_board" type="checkbox" value=1 <%=upd.getBoolean("enter_board")?"checked":""%>>
                <lt:Label res="res.forum.person.UserPrivDb" key="enter_board"/>
                <input name="view_topic" type="checkbox" value=1 <%=upd.getBoolean("view_topic")?"checked":""%>>
                <lt:Label res="res.forum.person.UserPrivDb" key="view_topic"/>
&nbsp;
                <input name="add_topic" type="checkbox" value=1 <%=upd.getBoolean("add_topic")?"checked":""%>>
                  <lt:Label res="res.forum.person.UserPrivDb" key="add_topic"/>
&nbsp;&nbsp;
<input name="reply_topic" type="checkbox" value=1 <%=upd.getBoolean("reply_topic")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="reply_topic"/>
&nbsp;&nbsp;&nbsp;
<input name="vote" type="checkbox" value=1 <%=upd.getBoolean("vote")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="vote"/>
&nbsp;&nbsp;
<input name="attach_upload" type="checkbox" value=1 <%=upd.getBoolean("attach_upload")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="attach_upload"/>
&nbsp;
<input name="attach_download" type="checkbox" value=1 <%=upd.getBoolean("attach_download")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="attach_download"/>
<input name="groupCode" type=hidden value="<%=groupCode%>">
<input name="boardCode" type=hidden value="<%=boardCode%>">
<br>
<lt:Label res="res.forum.person.UserPrivDb" key="attach_pay"/>
<select name="money_code">
<option value=""><lt:Label key="wu"/></option>
<%	  
        ScoreMgr sm = new ScoreMgr();
        Vector v = sm.getAllScore();
        Iterator ir = v.iterator();
        String str = "";
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
            if (su.isExchange()) {
%>
      <option value="<%=su.getCode()%>"><%=su.getName()%></option>
<%	  
          }
      }
%>
</select>
<script>
form_priv.money_code.value = "<%=StrUtil.getNullString(upd.getString("money_code"))%>";
</script>
<lt:Label res="res.forum.person.UserPrivDb" key="money_sum"/>
<input name="money_sum" size=3 value="<%=upd.get("money_sum")%>">
<input name="is_default" value="1" type="checkbox" <%=upd.getBoolean("is_default")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="is_default"/></td>
            </tr>
            <tr>
              <td>&nbsp;</td>
                <td colspan="2" align="center">
                  <input name="submit23" type=submit value="<lt:Label key="ok"/>">
                  &nbsp;&nbsp;&nbsp;&nbsp;
                  <input name="submit23" type=button value="<lt:Label res="res.label.forum.admin.user_m" key="reset_privilege"/>" onclick="window.location.href='?groupCode=<%=StrUtil.UrlEncode(groupCode)%>&boardCode=<%=StrUtil.UrlEncode(boardCode)%>&op=priv_reset'">			  </td>
            </tr>
          </form>
        </table>
<%}%>		
          <br>
          <br>
        <br></TD>
    </TR>
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
</html>
