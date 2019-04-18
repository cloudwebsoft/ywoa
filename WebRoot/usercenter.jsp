<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.module.cms.site.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.miniplugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.treasure.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.blog.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String skinPath = SkinMgr.getSkinPath(request);

if (!privilege.isUserLogin(request)) {
	response.sendRedirect("info.jsp?op=login&privurl=" + StrUtil.getUrl(request) + "&info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "err_not_login")));
	return;
}

String userName = privilege.getUser(request);
UserDb user = new UserDb();
user = user.getUser(userName);

String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="pragma" content="no-cache">
<LINK href="forum/<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.usercenter" key="user_center"/> - <%=Global.AppName%></title>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<%
String name="",lydate="",content="",topic="";
String RegDate="",Gender="",RealPic="",email="",sign="",myface="";
int addcount=0;

RealPic = user.getRealPic();
Gender = StrUtil.getNullStr(user.getGender());
if (Gender.equals("M"))
	Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_man"); // "男";
else if (Gender.equals("F"))
	Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_woman"); // "女";
else
	Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_none"); // "不详";
RegDate = com.redmoon.forum.ForumSkin.formatDate(request, user.getRegDate());
addcount = user.getAddCount();
email = user.getEmail(); 
sign = user.getSign();
myface = StrUtil.getNullStr(user.getMyface());

int pagesize = 10;
Paginator paginator = new Paginator(request);

TreasureUserDb tu = new TreasureUserDb();
String sql = "select userName,treasureCode from " + tu.getTableName() + " where userName=" + StrUtil.sqlstr(userName);
int total = tu.getObjectCount(sql);
int curpage = paginator.getCurPage();
paginator.init(total, pagesize);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<br>
<table width="98%" border="0" align="center" class="tableCommon">
  <thead>
  <tr>
    <td colspan="2" height="25" align="center"><lt:Label res="res.label.usercenter" key="user_center"/></td>
  </tr>
  </thead>
  <tr>
    <td width="18%" align="center" valign="top" class="userInfo">
       <ul>
	   <li style="line-height:22px;font-weight:700; font-size:14px"><%=user.getNick()%></li>
		<%
		UserGroupDb ugd = user.getUserGroupDb();
		if (!ugd.getCode().equals(UserGroupDb.EVERYONE)) {
		out.print("<li>" + ugd.getDesc() + "</li>");
		}
		%>
		<li>
		  <%if (myface.equals("")) {%>
		  <img src="forum/images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>">
		  <%}else{%>
		  <img src="<%=user.getMyfaceUrl(request)%>">
		  <%}%>
		</li>
		<li><img src="forum/images/<%=user.getLevelPic()%>"></li>
        <li><font color="#2174c4">性别：</font><%=Gender%></li>
		<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="rank"/></font><%=user.getLevelDesc()%></li>
<%
		ScoreMgr sm = new ScoreMgr();
		Vector vscore = sm.getAllScore();
		Iterator irscore = vscore.iterator();
		String str = "";
		while (irscore.hasNext()) {
			ScoreUnit scoreUnit = (ScoreUnit) irscore.next();
			// if (scoreUnit.getScore()==null)
			//	continue;
			if (scoreUnit.isDisplay()) {
				out.print("<li><font color='#2174c4'>" + scoreUnit.getName(request) + "：</font>" + (int)scoreUnit.getScore().getUserSum(user.getName()) + "</li>");
			}
		}
%>
		<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="topic_count"/></font><%=addcount%></li>
		<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="topic_elite"/></font><%=user.getEliteCount()%></li>
		<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="reg_date"/></font><%=RegDate%></li>
		<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="online_status"/></font><%
		OnlineUserDb ou = new OnlineUserDb();
		ou = ou.getOnlineUserDb(user.getName());
		if (ou.isLoaded())
			out.print(SkinUtil.LoadString(request, "res.label.forum.showtopic", "online_status_yes")); // "在线");
		else
			out.print(SkinUtil.LoadString(request, "res.label.forum.showtopic", "online_status_no")); // "离线");
		%>
		</li>
		<%
		com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
		%>
		<%if (cfg1.getBooleanProperty("forum.isOnlineTimeRecord")) {%>
		<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="online_time"/></font><%=(int)user.getOnlineTime()%><lt:Label res="res.label.forum.showtopic" key="hour"/></li>
		<%}%>
		<%if (cfg1.getBooleanProperty("forum.isFactionUsed")) {
			UserPropDb up = new UserPropDb();
			up = up.getUserPropDb(user.getName());
			String faction = StrUtil.getNullStr(up.getString("faction"));
			if (!faction.equals("")) {
				FactionDb fd = new FactionDb();
				fd = fd.getFactionDb(faction);
				if (fd!=null)
					faction = fd.getName();
			}
			if (!faction.equals("")) {						
		%>
		<li><font color="#2174c4"><lt:Label res="res.label.forum.showtopic" key="faction"/></font><%=faction%></li>
		<%	}
		}
								
		if (cfg1.getBooleanProperty("forum.showFlowerEgg")) {
			UserPropDb up = new UserPropDb();
			up = up.getUserPropDb(user.getName());
		%>
		<li><img src="images/flower.gif">&nbsp;(<%=up.getInt("flower_count")%>)&nbsp;&nbsp;&nbsp; <img src="images/egg.gif">&nbsp;(<%=up.getInt("egg_count")%>)</li>
		<%}%>
		</ul>
    </td>
    <td width="82%" align="center" valign="top"><table width="100%" border="0" cellpadding="3">
      <tr>
        <td align="left" valign="top"><a href="myinfo.jsp"><lt:Label res="res.label.usercenter" key="modify_myinfo"/></a>&nbsp;</td>
        <td width="55%" align="left" valign="top"><a href="javascript:hopenWin('../message/message.jsp',320,260)"></a><a href="forum/myfriend.jsp"><lt:Label res="res.label.usercenter" key="list_myfriend"/></a>&nbsp;&nbsp;<a href="forum/myfriend_apply.jsp"><lt:Label res="res.label.usercenter" key="friend_apply"/>
        </a></td>
      </tr>
      <tr>
        <td align="left" valign="top"><a href="forum/mytopic.jsp?action=mytopic"><lt:Label res="res.label.usercenter" key="mytopic"/></a></td>
        <td align="left" valign="top"><a href="javascript:hopenWin('message/message.jsp',320,260)"><lt:Label res="res.label.usercenter" key="send_bbs_message"/></a></td>
      </tr>
      <tr>
        <td align="left" valign="top"><a href="forum/myfavoriate.jsp"><lt:Label res="res.label.usercenter" key="myfavoriate"/></a>&nbsp;</td>
        <td align="left" valign="top"><%
if (com.redmoon.blog.Config.getInstance().isBlogOpen) {
	UserConfigDb ucd = new UserConfigDb();
	ucd = ucd.getUserConfigDbByUserName(userName);
	if (ucd!=null && ucd.isLoaded()) {
	%>
          <a href="blog/user/frame.jsp?blogId=<%=ucd.getId()%>"><lt:Label res="res.label.usercenter" key="blog_mgr"/></a>
          <%}else{%>
          <a href="blog/user/userconfig_add.jsp"><lt:Label res="res.label.usercenter" key="userconfig_add"/></a>
    <%}
	if (ucd==null)
		ucd = new UserConfigDb();
	cn.js.fan.base.ObjectBlockIterator oi = ucd.getGroupBlogsOwnedByUser(userName);
	while (oi.hasNext()) {
		ucd = (UserConfigDb)oi.next();
		out.print("&nbsp;<a target=_blank href='blog/myblog.jsp?blogId=" + ucd.getId() + "'>" + ucd.getTitle() + "</a>");		
	}
	BlogGroupUserDb bgu = new BlogGroupUserDb();
	Iterator qoi = bgu.getBlogGroupUserAttend(userName);
	while (qoi.hasNext()) {
		bgu = (BlogGroupUserDb)qoi.next();
		UserConfigDb gucd = ucd.getUserConfigDb(bgu.getLong("blog_id"));
		if (gucd.isLoaded())
			out.print("&nbsp;<a target=_blank href='blog/myblog.jsp?blogId=" + gucd.getId() + "'>" + gucd.getTitle() + "</a>");
	}
}
%></td>
      </tr>
      <tr>
        <td align="left" valign="top"><a href="forum/mytopic.jsp?action=myreply">
          <lt:Label res="res.label.forum.index" key="mytopic_attend"/>
          </a></td>
        <td align="left" valign="top">&nbsp;</td>
      </tr>
      <tr>
        <td align="left" valign="top"><a href="forum/score_exchange.jsp"><lt:Label res="res.label.usercenter" key="score_exchange"/></a></td>
        <td align="left" valign="top"><a href="forum/score_record_list.jsp?operate=<%=ScoreRecordDb.OPERATION_EXCHANGE%>"><lt:Label res="res.label.usercenter" key="score_exchange_list"/></a></td>
      </tr>
	  <tr>
        <td align="left" valign="top"><a href="forum/score_transfer.jsp"><lt:Label res="res.label.usercenter" key="score_transfer"/></a></td>
        <td align="left" valign="top"><a href="forum/score_record_list.jsp?operate=<%=ScoreRecordDb.OPERATION_TRANSFER%>"><lt:Label res="res.label.usercenter" key="score_transfer_list"/></a></td>
      </tr>
      <tr>
        <td align="left" valign="top"><a href="forum/score_record_list.jsp?operate=<%=ScoreRecordDb.OPERATION_PAY%>">
          <lt:Label res="res.label.usercenter" key="score_pay_list"/>
          </a></td>
        <td align="left" valign="top"><a href="forum/tag_user.jsp">
          <lt:Label res="res.label.forum.showtopic" key="tag_mine"/>
          </a></td>
      </tr>
      <tr>
        <td colspan="2" align="center" valign="top">
  <%if (com.redmoon.blog.Config.getInstance().isBlogOpen || com.redmoon.forum.Config.getInstance().getBooleanProperty("forum.isWebeditTopicEnabled")) {%>
          <table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
              <td height="22" align="left">
                <span style="float:left"><lt:Label res="res.label.usercenter" key="disk_space"/></span>
                <table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableBorder1" style="float:left;width:200px">
                  <tr>
                    <td height=16 align="left"><%
	long spaceAllowed = privilege.getDiskSpaceAllowed(user);
	int wpercent = (int)Math.round((double)user.getDiskSpaceUsed()/spaceAllowed*100);
	%>
                      <img src=forum/images/vote/bar1.gif width="<%=wpercent%>%" height=16></td>
                    </tr>
                  </table>
                &nbsp;
                <lt:Label res="res.label.usercenter" key="disk_space_used"/>
                <%=wpercent%>%&nbsp;(<%=user.getDiskSpaceUsed()/1024%>K)&nbsp;
                <lt:Label res="res.label.usercenter" key="disk_space_allowed"/>
                <%=NumberUtil.round((double)spaceAllowed/1024000, 1)%>M</td>
              </tr>
            </table>
  <%}%>				</td>
      </tr>
    </table>
        <table width="100%" border="0" cellspacing="1" class="tableBorder1">
		  <thead>
          <tr>
            <td height="24" colspan="6"><lt:Label res="res.label.usercenter" key="user_priv"/></td>
          </tr>
		  </thead>
          <tr>
            <td width="18%" height="24" align="center">
              <lt:Label res="res.forum.person.UserPrivDb" key="add_topic"/>            </td>
            <td width="17%" align="center">              <lt:Label res="res.forum.person.UserPrivDb" key="reply_topic"/>            </td>
            <td width="17%" align="center">              <lt:Label res="res.forum.person.UserPrivDb" key="attach_upload"/>            </td>
            <td width="15%" align="center">              <lt:Label res="res.forum.person.UserPrivDb" key="attach_download"/>            </td>
            <td width="16%" align="center">              <lt:Label res="res.forum.person.UserPrivDb" key="vote"/>            </td>
            <td width="17%" align="center">              <lt:Label res="res.forum.person.UserPrivDb" key="search"/>            </td>
          </tr>
          <tr>
            <td height="24" align="center">
			<%
			UserPrivDb upd = new UserPrivDb();
			upd = upd.getUserPrivDb(user.getName());
			if (upd.getBoolean("add_topic"))
				out.print("√");
			else
				out.print("×");
			%>			</td>
            <td align="center"><%
			if (upd.getBoolean("reply_topic"))
				out.print("√");
			else
				out.print("×");
			%></td>
            <td align="center"><%
			if (upd.getBoolean("attach_upload"))
				out.print("√");
			else
				out.print("×");
			%></td>
            <td align="center"><%
			if (upd.getBoolean("attach_download"))
				out.print("√");
			else
				out.print("×");
			%></td>
            <td align="center"><%
			if (upd.getBoolean("vote"))
				out.print("√");
			else
				out.print("×");
			%></td>
            <td align="center"><%
			if (upd.getBoolean("search"))
				out.print("√");
			else
				out.print("×");
			%></td>
          </tr>
          <tr>
            <td height="24" align="center">              <lt:Label res="res.forum.person.UserPrivDb" key="attach_day_count"/>            </td>
            <td align="center">              <lt:Label res="res.forum.person.UserPrivDb" key="attach_size"/>            </td>
            <td align="center">
              <%if (cfg1.getBooleanProperty("forum.isBkMusic")) {%>
			  <lt:Label res="res.label.usercenter" key="music"/>
			  <%}%>
            </td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
          </tr>
          <tr>
            <td height="24" align="center"><%=upd.getInt("attach_day_count")%></td>
            <td align="center"><%=upd.getInt("attach_size")%>(K)</td>
            <td align="center"><%
			if (cfg1.getBooleanProperty("forum.isBkMusic")) {
				int musicLevel = cfg1.getIntProperty("forum.bkMusicUserLevel");
				if (user.getUserLevelDb().getLevel()>=musicLevel) {
					out.print("√");
				}
				else
					out.print("×");
			}
			%></td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
          </tr>
        </table>
<%
PluginMgr pm = new PluginMgr();
Vector vplugin = pm.getAllPlugin();
if (vplugin.size()>0) {
	Iterator irplugin = vplugin.iterator();
	while (irplugin.hasNext()) {
		PluginUnit pu = (PluginUnit)irplugin.next();
		if (!pu.getUserCenterPage().equals("")) {
%>
			<jsp:include page="<%=pu.getUserCenterPage()%>" flush="true"/>
<%		}
	}
}%>
<%
EntranceMgr em = new EntranceMgr();
Vector eplugin = em.getAllEntrance();
if (eplugin.size()>0) {
	Iterator irplugin = eplugin.iterator();
	while (irplugin.hasNext()) {
		EntranceUnit pu = (EntranceUnit)irplugin.next();
		if (!pu.getUserCenterPage().equals("")) {
%>
			<jsp:include page="<%=pu.getUserCenterPage()%>" flush="true"/>
<%		}
	}
}%>
		<%if (paginator.getTotal()>0) {%>
        <table width="100%" height="24" border="0" align="center" cellpadding="0" cellspacing="0">
		  <tr>
			<td align="right"><lt:Label res="res.label.usercenter" key="right_records"/><%=paginator.getTotal() %><lt:Label res="res.label.usercenter" key="per_page"/><%=paginator.getPageSize()%><lt:Label res="res.label.usercenter" key="page"/><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></td>
		  </tr>
        </table>
		<%}%>
		<table class="tableBorder1" width="100%"  border="0" align="center" cellpadding="0" cellspacing="1">
		<thead>
		  <tr align="center">
			<td width="41%" height="22"><lt:Label res="res.label.usercenter" key="baby_name"/></td>
			<td width="26%" height="22"><lt:Label res="res.label.usercenter" key="buy_date"/></td>
			<td width="18%"><lt:Label res="res.label.usercenter" key="count"/></td>
			<td width="15%"><%=SkinUtil.LoadString(request,"op")%></td>
		  </tr>
		</thead>
		<%
		Vector v = tu.list(sql, (curpage-1)*pagesize, curpage*pagesize-1);
		TreasureMgr tmg = new TreasureMgr();
		Iterator ir = v.iterator();
		int i = 0;
		while (ir.hasNext()) {
		tu = (TreasureUserDb)ir.next();
		String treasureCode = tu.getTreasureCode();
		TreasureUnit tun = tmg.getTreasureUnit(treasureCode);
		if (tun==null)
		continue;
		i++;
		%>
		<form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method="post">
			<tr align="center">
			  <td height="22"><%
		out.print(tun.getName());
		%>
		</td>
		<td height="22"><%=DateUtil.format(tu.getBuyDate(), "yy-MM-dd")%> </td>
		<td><%=tu.getAmount()%></td>
		<td height="22"><a href="forum/treasure_show.jsp?code=<%=StrUtil.UrlEncode(tu.getTreasureCode())%>"><lt:Label res="res.label.usercenter" key="view"/></a></td>
		</tr>
		</form>
		<%}%>
		</table>
              <table width="100%" border="0" align="center" cellpadding="3" cellspacing="1" class="9black">
                  <tr>
                    <td height="23" align="right">
                        <%
	String querystr = "userName=" + StrUtil.UrlEncode(userName);
    out.print(paginator.getCurPageBlock("?"+querystr));
%>
                    </td>
                  </tr>
        </table></td>
  </tr>
</table>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
<script>
function operateT(action) {
	if (action=="topen") {
		if (!confirm("您确定要开通么？"))
			return;
	}
	else {
		if (!confirm("您确定要关闭么？"))
			return;
	}
	window.location.href = "usercenter.jsp?op=" + action;
}
</script>
</html>