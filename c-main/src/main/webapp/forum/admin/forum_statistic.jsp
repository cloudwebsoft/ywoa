<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ page import="java.lang.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.forum.ForumDb"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>论坛统计</title>
<LINK href="default.css" type=text/css rel=stylesheet>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
.STYLE1 {color: #FF0000}
.ttl {	CURSOR: hand; COLOR: #ffffff; PADDING-TOP: 4px
}
-->
</style>
</head>

<body bgcolor="#FFFFFF">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

ForumStatisticDb fsd = new ForumStatisticDb();
int memberTotal = 0,addCount = 0,eliteCount = 0,userAddCount = 0,userEliteCount = 0,allCount = 0;
memberTotal = fsd.getMemberTotal();
addCount = fsd.getAddCount();
eliteCount = fsd.getEliteCount(); 
allCount = fsd.getAllCount();

String op = ParamUtil.get(request,"op");
if(op.equals("updateAllUser")){
	boolean re = false;
	try {
		re = fsd.save();
	}
	catch (ErrMsgException e) {
		out.print(e.getMessage());
	}
	if (re) {
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_operate_success")));
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}

if(op.equals("updateUser")){
	boolean re = false;
	String name = ParamUtil.get(request,"name");
	re = fsd.save(name);
	if (re) {
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_operate_success")));
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}

ForumDb fd = new ForumDb();
fd = fd.getForumDb();
if (op.equals("setInfo")) {
	Date createDate = DateUtil.parse(ParamUtil.get(request, "createDate"), "yyyy-MM-dd HH:mm:ss");
	int maxOnlineCount = ParamUtil.getInt(request, "maxOnlineCount");
	Date maxOnlineDate = DateUtil.parse(ParamUtil.get(request, "maxOnlineDate"), "yyyy-MM-dd HH:mm:ss");
	int topicCount = ParamUtil.getInt(request, "topicCount");
	int postCount = ParamUtil.getInt(request, "postCount");
	int yestodayCount = ParamUtil.getInt(request, "yestodayCount");
	int todayCount = ParamUtil.getInt(request, "todayCount");
	int maxCount = ParamUtil.getInt(request, "maxCount");
	Date maxDate = DateUtil.parse(ParamUtil.get(request, "maxDate"), "yyyy-MM-dd HH:mm:ss");
	fd.setCreateDate(createDate);
	fd.setMaxOnlineCount(maxOnlineCount);
	fd.setMaxOnlineDate(maxOnlineDate);
	fd.setTopicCount(topicCount);
	fd.setPostCount(postCount);
	fd.setYestodayCount(yestodayCount);
	fd.setTodayCount(todayCount);
	fd.setMaxCount(maxCount);
	fd.setMaxDate(maxDate);
	if (fd.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}

fd = ForumDb.getInstance();
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head>
        <lt:Label res="res.label.forum.admin.forum_statistic" key="forum_statistic"/>
      </TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<FORM METHOD=POST id="form1" name="form1" ACTION='?op=updateAllUser'>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr> 
    <td width="100%" height="23" class="thead">
      <lt:Label res="res.label.forum.admin.forum_statistic" key="user_post_statistic"/>
    </td>
  </tr>
  <tr> 
    <td>
      <table width="100%" border="0" align="center" cellpadding="2" cellspacing="1" bgcolor="#FFFFFF">
          <tr> 
            <td width='20%'>
              <lt:Label res="res.label.forum.admin.forum_statistic" key="user_count"/>
            <%=memberTotal%></td>
            <td width='20%'><lt:Label res="res.label.forum.admin.forum_statistic" key="topic_count"/>
            <%=addCount%></td>
            <td width='20%'><lt:Label res="res.label.forum.admin.forum_statistic" key="post_count"/>
            <%=allCount%></td>
            <td width="20%"><lt:Label res="res.label.forum.admin.forum_statistic" key="elite_count"/>
            <%=eliteCount%></td>
            <td width="20%" align=center><input type=submit value='<lt:Label res="res.label.forum.admin.forum_statistic" key="count_again"/>'/></td>
          </tr>
      </table>    </td>
  </tr>
  <tr class=row style="BACKGROUND-COLOR: #fafafa">
    <td valign="top" bgcolor="#FFFFFF" class="thead">&nbsp;</td>
  </tr>
</table>
</FORM>
<br>
<br>
<FORM METHOD=POST id="form2" name="form2" ACTION='?op=search'>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr> 
    <td width="100%" height="23" class="thead"><lt:Label res="res.label.forum.admin.forum_statistic" key="user_statistic"/></td>
  </tr>
  <tr> 
    <td>
      <table width="100%" border="0" align="center" cellpadding="2" cellspacing="1" bgcolor="#FFFFFF">
          <tr> 
            <td width='10%' align="center"><lt:Label res="res.label.forum.admin.forum_statistic" key="user_name"/></td>
            <td width='24%'><input type=text value="" name="nick"></td>
			<td width="66%" align=left><input type="submit" value='<lt:Label res="res.label.forum.admin.forum_statistic" key="count_again"/>'/></td>
          </tr>
      </table>    </td>
  </tr>
  <tr class=row style="BACKGROUND-COLOR: #fafafa">
    <td valign="top" bgcolor="#FFFFFF" class="thead">&nbsp;</td>
  </tr>
</table>
</FORM>
<br>
<br>
<FORM METHOD=POST id="form3" name="form3" ACTION='?op=updateUser'>
<table width="80%" align="center" cellPadding="3" cellSpacing="0" bgcolor="#FFFFFF" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <tbody>
    <tr>
      <td class="thead" style="PADDING-LEFT: 10px" noWrap width="20%"><lt:Label res="res.label.forum.admin.forum_statistic" key="user_name"/></td>
      <td width="22%" noWrap class="thead" style="PADDING-LEFT: 10px"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
        <lt:Label res="res.label.forum.admin.forum_statistic" key="user_topic"/></td>
      <td width="29%" noWrap class="thead" style="PADDING-LEFT: 10px"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
        <lt:Label res="res.label.forum.admin.forum_statistic" key="user_elite"/></td>
      <td width="29%" noWrap class="thead" style="PADDING-LEFT: 10px">&nbsp;</td>
    </tr>
<%
String name = "";
String nick = ParamUtil.get(request,"nick");
if(op.equals("search")){
	if(!nick.equals("")){
		UserDb ud = new UserDb();
		ud = ud.getUserDbByNick(nick);
		if(ud != null){
			name = ud.getName();
			userAddCount = fsd.getAddCount(name);
			userEliteCount = fsd.getEliteCount(name);
%>
    <tr>
      <td width="20%"><%=nick%></td>
      <td width="22%"><%=userAddCount%></td>
      <td width="29%"><%=userEliteCount%></td>
      <td width="29%"><input type="submit" value='<lt:Label res="res.label.forum.admin.forum_statistic" key="count_again"/>'/>
      <input type="hidden" name="name" value="<%=name%>"/></td>
    </tr>
<%
		}else{
%>
    <tr>
      <td colspan="4" bgcolor=#F6F6F6><lt:Label res="res.label.forum.admin.forum_statistic" key="user_none"/>
        ！</td>
    </tr>
<%
		}
	}
}
%>
  </tbody>
</table>
</FORM>
<br />
<table width="80%" border="0" align="center" cellpadding="3" cellspacing="0" bgcolor="#FFFFFF" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <form action="?op=setInfo" method="post" name="form6" id="form6">
    <tr>
      <td height="26" colspan="2" align="left" class="thead">&nbsp;</td>
    </tr>
    <tr>
      <td width="26%" height="26" align="left"><lt:Label res="res.label.forum.index" key="create_date"/></td>
      <td width="74%" align="left"><input name="createDate" value="<%=com.redmoon.forum.ForumSkin.formatDateTime(request, fd.getCreateDate())%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="online_max_count"/></td>
      <td height="22" align="left"><input name="maxOnlineCount" value="<%=fd.getMaxOnlineCount()%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="online_max_date"/></td>
      <td height="22" align="left"><input name="maxOnlineDate" value="<%=com.redmoon.forum.ForumSkin.formatDateTime(request, fd.getMaxOnlineDate())%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="topic_count"/></td>
      <td height="22" align="left"><input name="topicCount" value="<%=fd.getTopicCount()%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="post_count"/></td>
      <td height="22" align="left"><input name="postCount" value="<%=fd.getPostCount()%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="yestoday_post"/></td>
      <td height="22" align="left"><input name="yestodayCount" value="<%=fd.getYestodayCount()%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="today_post"/></td>
      <td height="22" align="left"><input name="todayCount" value="<%=fd.getTodayCount()%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="most_post"/></td>
      <td height="22" align="left"><input name="maxCount" value="<%=fd.getMaxCount()%>" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="most_post_date"/></td>
      <td height="22" align="left"><input name="maxDate" value="<%=com.redmoon.forum.ForumSkin.formatDateTime(request, fd.getMaxDate())%>" /></td>
    </tr>
    <tr>
      <td height="28" colspan="2" align="center"><input name="submit22" type="submit" value="<lt:Label key="ok"/>" /></td>
    </tr>
  </form>
</table>
</body>
</html>
