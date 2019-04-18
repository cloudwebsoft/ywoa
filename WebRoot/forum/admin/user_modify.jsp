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
//-->
</script></head>
<body>
<jsp:useBean id="us" scope="page" class="com.redmoon.forum.person.userservice"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="prision" scope="page" class="com.redmoon.forum.life.prision.Prision"/>
<%
if (!Privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String op = StrUtil.getNullString(request.getParameter("op"));
String username = ParamUtil.get(request, "username");
UserDb user = new UserDb();
user = user.getUser(username);

UserPrivDb upd = new UserPrivDb();
upd = upd.getUserPrivDb(username);

if (op.equals("priv")) {
/*
	int maxAttachDayCount = ParamUtil.getInt(request, "attach_day_count");
	int maxAttachmentSize = ParamUtil.getInt(request, "attach_size");
	String add_topic = ParamUtil.get(request, "add_topic");
	if (add_topic.equals(""))
		add_topic = "0";
	String reply_topic = ParamUtil.get(request, "reply_topic");
	if (reply_topic.equals(""))
		reply_topic = "0";
	
	upd.set("attach_day_count", new Integer(maxAttachDayCount));
	upd.set("attach_size", new Integer(maxAttachmentSize));
	upd.set("add_topic", add_topic);
	upd.set("reply_topic", reply_topic);
	if (upd.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
*/
	QObjectMgr qom = new QObjectMgr();
	if (qom.save(request, upd, "sq_user_priv_save"))
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	
}

if (op.equals("priv_reset")) {
	upd.del();
	upd.init(username);
	upd = upd.getUserPrivDb(username);
}

String privurl = StrUtil.getNullString(request.getParameter("privurl"));
// 防XSS
try {
	com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();		
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "privurl", privurl, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (op.equals("pwd")) {
	String pwd = StrUtil.getNullString(request.getParameter("pwd"));
	try {
		if (us.ModifyPWD(response,username, pwd, privurl))
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

if (op.equals("modify")) {
	String nick = ParamUtil.get(request, "nick").trim();
	if (nick.equals("")) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.user_m", "need_nick")));
		return;
	}
	
	String isValid = request.getParameter("isValid");
	if (isValid==null)
		isValid = "true";
	String groupCode = ParamUtil.get(request, "groupCode");
	if (isValid.equals("false"))
		user.setValid(false);
	else
		user.setValid(true);
	
	user.setGroupCode(groupCode);
	
	int diskSpaceAllowed = (int)user.getDiskSpaceAllowed();
	try {
		diskSpaceAllowed = ParamUtil.getInt(request, "diskSpaceAllowed");
	}
	catch (Exception e) {
	}
	user.setDiskSpaceAllowed((long)diskSpaceAllowed);
	user.setNick(nick);
	String strCheckStatus = ParamUtil.get(request, "checkStatus");
	if (StrUtil.isNumeric(strCheckStatus)) {
		user.setCheckStatus(Integer.parseInt(strCheckStatus));
	}
	else
		user.setCheckStatus(UserDb.CHECK_STATUS_NOT);
		
	boolean canRename = ParamUtil.get(request, "canRename").equals("true");
	user.setCanRename(canRename);

	if (user.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}

if (op.equals("modifyValue")) {
	int credit = ParamUtil.getInt(request, "credit");
	int experience = ParamUtil.getInt(request, "experience");
	int gold = ParamUtil.getInt(request, "gold");
	user = user.getUser(username);
	user.setCredit(credit);
	user.setExperience(experience);
	user.setGold(gold);
	if (user.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}

if (op.equals("delmsg")) {
	// user.del();
	MsgDb md = new MsgDb();
	int count = md.delMessagesOfUser(user.getName());
	out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.forum.admin.user_m", "del_count")));
}

if (op.equals("deluser")) {
	com.redmoon.forum.ucenter.UCenterConfig ucf = com.redmoon.forum.ucenter.UCenterConfig.getInstance();
	// @task:需整合至UserMgr的del方法中
	if (ucf.getBooleanProperty("uc.isActive")) {
		try {
			com.redmoon.forum.ucenter.UC.delete(username);
		} catch(ErrMsgException e) {
			out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
			return;
		}
	}
	if (user.del())
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "user_m.jsp"));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}

if (op.equals("arrest")) {
	boolean isvalid = true;
	String arresttime = ParamUtil.get(request, "arresttime");
	String arrestreason = ParamUtil.get(request, "arrestreason");
	int arrestday = 0;
	String errmsg = "";
	try {
		arrestday = ParamUtil.getInt(request, "arrestday");
	}
	catch (ErrMsgException e) {
		errmsg += SkinUtil.LoadString(request, "res.label.forum.admin.user_m", "err_day_count") + "\\r\\n";
		isvalid = false;
	}
	if (arresttime.equals("") || arrestreason.equals("")) {
		errmsg += SkinUtil.LoadString(request, "res.label.forum.admin.user_m", "err_date_reason") + "\\r\\n";
		isvalid = false;
	}
	if (arrestday<=0) {
		errmsg += SkinUtil.LoadString(request, "res.label.forum.admin.user_m", "day_count_big_than_zero") + "\\r\\n";
		isvalid = false;
	}

	if (!errmsg.equals(""))
		out.print(StrUtil.Alert(errmsg));

	if (isvalid)
	{
		try {
			isvalid = prision.arrest(Privilege.getUser(request), username, arresttime, arrestreason, arrestday);
			if (isvalid)
				out.println(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
			else
				out.println(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
		}
		catch (ResKeyException e) {
			out.print(StrUtil.Alert(e.getMessage(request)));
		}		
	}
}
if (op.equals("release")) {
	try {
		boolean isvalid = prision.release(username);
		if (isvalid)
			out.println(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
		else
			out.println(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ResKeyException e) {
		out.print(StrUtil.Alert(e.getMessage(request)));
	}
}
%>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%">&nbsp;<a href="user_m.jsp"><lt:Label res="res.label.forum.admin.user_m" key="back_to_user_list"/></a></TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD align="center" style="PADDING-LEFT: 10px"><table width="550" border="0" align="center" class="frame_gray">
        <form name=form1 action="user_modify.jsp?op=pwd" method="post" onSubmit="return form1_onsubmit()">
          <tr>
            <td width="12%" height="23">&nbsp;</td>
            <td align="center" colspan="2" height="23"><b><lt:Label res="res.label.forum.admin.user_m" key="modify_pwd"/></b></td>
            </tr>
          <tr>
            <td width="12%">&nbsp;</td>
            <td width="21%" align="left"><lt:Label res="res.label.forum.admin.user_m" key="user_name"/></td>
            <td width="67%" align="left"><%=user.getNick()%><input name="username" value="<%=username%>" type=hidden> 
            &nbsp;&nbsp;&nbsp;[<a href="../../myinfo.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>" target="_blank">
            <lt:Label key="op_edit"/></a>]</td>
            </tr>
          <tr>
            <td width="12%">&nbsp;</td>
            <td width="21%" align="left"><lt:Label res="res.label.forum.admin.user_m" key="pwd"/></td>
            <td width="67%" align="left"><input name=pwd type=password class="singleboarder">            </td>
            </tr>
          <tr>
            <td width="12%">&nbsp;</td>
            <td width="21%" align="left"><lt:Label res="res.label.forum.admin.user_m" key="confirm_pwd"/></td>
            <td width="67%" align="left"><input name=pwd_confirm type=password class="singleboarder">
              <input type=hidden name="privurl" value="<%=privurl%>">            </td>
            </tr>
          <tr>
            <td width="12%">&nbsp;</td>
            <td colspan="2" align="center"><input name="submit" type=submit value="<lt:Label key="ok"/>">
&nbsp;&nbsp;&nbsp;
        <input name="reset" type=reset value="<lt:Label key="reset"/>">            </td>
            </tr>
        </form>
      </table>
        <br>
        <table width="550" border="0" align="center" class="frame_gray">
          <form name=form_priv action="user_modify.jsp?op=priv" method="post">
            <tr>
              <td width="5%" height="23">&nbsp;</td>
              <td height="23" colspan="2" align="center"><b>
                <lt:Label res="res.label.forum.admin.user_m" key="modify_privilege"/>
              </b></td>
            </tr>
            <tr>
              <td>&nbsp;</td>
              <td colspan="2" align="left">
			  <input name="add_topic" type="checkbox" value=1 <%=upd.getBoolean("add_topic")?"checked":""%>>
			  <lt:Label res="res.forum.person.UserPrivDb" key="add_topic"/>			  &nbsp;&nbsp;
			  <input name="reply_topic" type="checkbox" value=1 <%=upd.getBoolean("reply_topic")?"checked":""%>>
			  <lt:Label res="res.forum.person.UserPrivDb" key="reply_topic"/>			  &nbsp;&nbsp;&nbsp;
			  <input name="vote" type="checkbox" value=1 <%=upd.getBoolean("vote")?"checked":""%>>
			  <lt:Label res="res.forum.person.UserPrivDb" key="vote"/>			  &nbsp;&nbsp;
			  <input name="attach_upload" type="checkbox" value=1 <%=upd.getBoolean("attach_upload")?"checked":""%>>
			  <lt:Label res="res.forum.person.UserPrivDb" key="attach_upload"/>
			  &nbsp;
<input name="attach_download" type="checkbox" value=1 <%=upd.getBoolean("attach_download")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="attach_download"/>
<input name="search" type="checkbox" value=1 <%=upd.getBoolean("search")?"checked":""%>>
<lt:Label res="res.forum.person.UserPrivDb" key="search"/></td>
            </tr>
            <tr>
              <td>&nbsp;</td>
              <td width="28%" align="left">                <lt:Label res="res.forum.person.UserPrivDb" key="attach_size"/>              </td>
              <td width="67%" align="left"><input name="attach_size" value="<%=upd.getInt("attach_size")%>">
                （K）
                <input name="username" type=hidden value="<%=username%>">
                <input name="attach_today" type=hidden value="<%=upd.getString("attach_today")%>">
                <input name="attach_today_upload_count" type=hidden value="<%=upd.getInt("attach_today_upload_count")%>"></td>
            </tr>
            <tr>
              <td>&nbsp;</td>
              <td align="left">                <lt:Label res="res.forum.person.UserPrivDb" key="attach_day_count"/>              </td>
              <td align="left"><input name="attach_day_count" value="<%=upd.getInt("attach_Day_count")%>">
              &nbsp;&nbsp;
			  <input name="is_default" value="1" type="checkbox" <%=upd.getBoolean("is_default")?"checked":""%>>
			  <lt:Label res="res.label.forum.admin.user_m" key="use_default_priv"/></td>
            </tr>
            <tr>
              <td>&nbsp;</td>
              <td colspan="2" align="center">
			  <input name="submit23" type=submit value="<lt:Label key="ok"/>">
  			  <input name="submit23" type=button value="<lt:Label res="res.label.forum.admin.user_m" key="reset_privilege"/>" onclick="window.location.href='user_modify.jsp?username=<%=StrUtil.UrlEncode(username)%>&op=priv_reset'">			  </td>
            </tr>
          </form>
        </table>
        <br>
      <table width="550" border="0" align="center" class="frame_gray">
        <form name=form2 action="user_modify.jsp?op=modify" method="post">
          <tr>
            <td width="4%" height="23">&nbsp;</td>
            <td align="center" colspan="2" height="23"><b><lt:Label res="res.label.forum.admin.user_m" key="user_setup"/></b></td>
            </tr>
          <tr>
            <td width="4%">&nbsp;</td>
            <td width="20%" align="left"><lt:Label res="res.label.forum.admin.user_m" key="nick"/></td>
            <td width="52%" align="left"><input name="nick" value="<%=user.getNick()%>"><input name="username" type=hidden value="<%=username%>">            <label></label></td>
            </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left"><lt:Label res="res.label.forum.admin.user_m" key="disk_space"/>&nbsp;&nbsp;&nbsp;</td>
            <td align="left"><input name="diskSpaceAllowed" class="singleboarder" id="diskSpaceAllowed" value="<%=user.getDiskSpaceAllowed()%>">
              <lt:Label res="res.label.forum.admin.user_m" key="byte"/>
              (用户的磁盘空间以所在组允许的磁盘空间和此处磁盘空间两者中较大的为准，实际为
              <%=Privilege.getDiskSpaceAllowed(user)%>
              <lt:Label res="res.label.forum.admin.user_m" key="byte"/>
              &nbsp;)</td>
            </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left"><lt:Label res="res.label.forum.admin.user_m" key="disk_space_used"/></td>
            <td align="left"><%=user.getDiskSpaceUsed()%><lt:Label res="res.label.forum.admin.user_m" key="byte"/></td>
            </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left"><lt:Label res="res.label.forum.admin.user_m" key="user_group"/></td>
            <td align="left"><select name="groupCode">
                <%
			UserGroupDb ugroup = new UserGroupDb();
			Vector result = ugroup.list();
			Iterator ir = result.iterator();
			String opts = "";
			while (ir.hasNext()) {
				ugroup = (UserGroupDb) ir.next();
				opts += "<option value='" + ugroup.getCode() + "'>" + ugroup.getDesc() + "</option>";
			}
			%>
				<option value=""><lt:Label res="res.label.forum.admin.user_m" key="none"/></option>
                <%=opts%>
              </select>
              <br>
			  <script>
			  form2.groupCode.value = "<%=user.getGroupCode()%>";
			  </script>
			  <lt:Label res="res.label.forum.admin.user_m" key="auto_level_group"/>
			  <%
				  UserGroupDb ugd = user.getUserGroupDb();
				  out.print(ugd.getDesc());		  
			  %></td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left"><lt:Label res="res.label.forum.admin.user_m" key="canRename"/></td>
            <td align="left">
			<select name="canRename">
			<option value="true"><lt:Label key="yes"/></option>
			<option value="false"><lt:Label key="no"/></option>
			</select>
			<script>
			form2.canRename.value = "<%=user.isCanRename()?"true":"false"%>";
			</script>			
			</td>
          </tr>
          <tr>
            <td>&nbsp;&nbsp;&nbsp;</td>
            <td colspan="2" align="left">
			<input type="checkbox" name="isValid" value="false" <%=user.isValid()?"":"checked"%>>
			<lt:Label res="res.label.forum.admin.user_m" key="shield_user_and_topic"/>
			<input type="checkbox" name="checkStatus" value="<%=UserDb.CHECK_STATUS_PASS%>" <%=user.getCheckStatus()==UserDb.CHECK_STATUS_PASS?"checked":""%>>
			<lt:Label res="res.label.forum.admin.user_m" key="check_pass"/>			</td>
            </tr>
          <tr>
            <td>&nbsp;</td>
            <td colspan="2" align="center"><input name="submit2" type=submit value="<lt:Label key="ok"/>"></td>
            </tr>
        </form>
      </table>
      <br>
      <table width="550" border="0" align="center" class="frame_gray">
        <form name=form3 action="user_modify.jsp?op=modifyValue" method="post">
          <tr>
            <td width="4%" height="23">&nbsp;</td>
            <td align="center" colspan="2" height="23"><b><lt:Label res="res.label.forum.admin.user_m" key="modify_user_score"/></b></td>
            <td width="24%" height="23">&nbsp;</td>
          </tr>
          <tr>
            <td width="4%">&nbsp;</td>
            <td width="15%" align="left"><lt:Label res="res.label.forum.admin.user_m" key="credit"/></td>
            <td width="57%" align="left"><input name="credit" class="singleboarder" value="<%=user.getCredit()%>">
                <input name="username" type=hidden value="<%=username%>">
              <lt:Label res="res.label.forum.admin.user_m" key="level"/>
              &nbsp;
              <%
out.print(user.getLevelDesc());
%>
              <img src="../images/<%=user.getLevelPic()%>"> </td>
            <td width="24%">&nbsp;</td>
          </tr>
          <tr>
            <td width="4%">&nbsp;</td>
            <td align="left"><lt:Label res="res.label.forum.admin.user_m" key="experience"/>&nbsp;</td>
            <td align="left"><input name="experience" class="singleboarder" value="<%=user.getExperience()%>"></td>
            <td width="24%">&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left"><lt:Label res="res.label.forum.admin.user_m" key="add_topic_count"/>&nbsp;</td>
            <td align="left"><%=user.getAddCount()%></td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left">
			<%
			ScoreMgr sm = new ScoreMgr();
			ScoreUnit su = sm.getScoreUnit("gold");
			out.print(su.getName());
			%>			</td>
            <td align="left"><input name="gold" class="singleboarder" value="<%=user.getGold()%>"></td>
            <td>&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td align="left">&nbsp;</td>
            <td align="left"><input name="submit22" type=submit value="<lt:Label key="ok"/>"></td>
            <td>&nbsp;</td>
          </tr>
        </form>
      </table>
      <br>
      <table width="550" border="0" cellspacing="1" class="frame_gray">
        <TR align=center bgColor=#f8f8f8>
          <TD width="83" height="24" bgcolor="#E2E0DC"><lt:Label res="res.label.forum.admin.user_m" key="arrest_date"/></TD>
          <TD width="114" bgcolor="#E2E0DC"><lt:Label res="res.label.forum.admin.user_m" key="arrest_reason"/></TD>
          <TD width="51" bgcolor="#E2E0DC"><lt:Label res="res.label.forum.admin.user_m" key="arrest_day"/></TD>
          <TD width="99" bgcolor="#E2E0DC"><lt:Label res="res.label.forum.admin.user_m" key="arrest_person"/></TD>
          <TD width="43" bgcolor="#E2E0DC"><lt:Label res="res.label.forum.admin.user_m" key="arrest_status"/></TD>
          <TD width="100" bgcolor="#E2E0DC"><lt:Label key="op"/></TD>
        </TR>
        <TR align=center bgColor=#f8f8f8>
          <form name="formarrest" id="formarrest" action="?op=arrest" method=post target="_self">
            <TD><%
		int arrestday = user.getArrestDay();
		String arrestreason = user.getArrestReason();
		java.util.Date arresttime = user.getArrestTime();
		String arrestpolice = StrUtil.getNullString(user.getArrestPolice());
			
		String artime = "";
		if (arresttime==null)
			artime = DateUtil.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss");
		else
			artime = StrUtil.FormatDate(arresttime,"yyyy-MM-dd HH:mm:ss");
		%>
                <input name=arresttime value="<%=artime%>" size=10></TD>
            <TD><input name=arrestreason value="<%=StrUtil.getNullStr(arrestreason)%>" size=20></TD>
            <TD><input name=arrestday value="<%=arrestday%>" size=3></TD>
            <TD><%=StrUtil.getNullStr(user.getUser(arrestpolice).getNick())%>
                <input type=hidden name=username value="<%=user.getName()%>">
              </TD>
            <TD><%
		Calendar c1 = DateUtil.add(arresttime, arrestday); // 释放日期
		Calendar c2 = Calendar.getInstance(); // 当前日期
		if (DateUtil.compare(c1,c2)==1)
			out.println("<font color=red>" + SkinUtil.LoadString(request, "yes") + "</font>");
		else
			out.println(SkinUtil.LoadString(request, "no"));
		%>            </TD>
            <TD>
			  <input type="submit" name="Submit2" value="<lt:Label res="res.label.forum.admin.user_m" key="arrest"/>">
              &nbsp;
              <input name="Submit22" type="button" id="Submit22" onClick="window.location.href='?op=release&username=<%=StrUtil.UrlEncode(user.getName())%>';" value="<lt:Label res="res.label.forum.admin.user_m" key="release"/>">            </TD>
          </form>
        </TR>
      </table>
      <br>
      <br>
      <a href="?op=deluser&username=<%=StrUtil.UrlEncode(user.getName())%>"><lt:Label res="res.label.forum.admin.user_m" key="del_user_and_topic"/></a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="?op=delmsg&username=<%=StrUtil.UrlEncode(username)%>"><lt:Label res="res.label.forum.admin.user_m" key="del_user_topic"/></a></TD>
    </TR>
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
</html>
