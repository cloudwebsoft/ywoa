<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Date"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.module.cms.site.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.blog.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.canUserDo(request, "", "view_userinfo")) {
	response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String username = ParamUtil.get(request, "username");
UserDb user = new UserDb();
if (username.equals("")) {
	String nick = ParamUtil.get(request, "nick");
	user = user.getUserDbByNick(nick);
	if (user==null || !user.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "res.label.userinfo", "user_name_can_not_be_null")));
		return;
	}
	else
		username = user.getName();
}

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();

String name = privilege.getUser(request);
user = user.getUser(username);
if (!user.isLoaded()) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,"res.label.userinfo","user_not_exsist")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.userinfo" key="user_info"/> - <%=user.getNick()%> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script>
function New(para_URL){var URL=new String(para_URL);window.open(URL,'','resizable,scrollbars')}
function CheckRegName(){
	var Name=document.form.RegName.value;
	window.open("checkregname.jsp?RegName="+Name,"","width=200,height=20");
}

function check_checkbox(myitem,myvalue){
     var checkboxs = document.all.item(myitem);
     if (checkboxs!=null)
     {
       for (i=0; i<checkboxs.length; i++)
          {
            if (checkboxs[i].type=="checkbox" && checkboxs[i].value==myvalue)
              {
                 checkboxs[i].checked = true
              }
          }
     }
}
</script>
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<%
String RegName="",Question="",Answer="";
String RealName="",Career="";
String Gender="",Job="";
int BirthYear = 0;
int BirthMonth = 0;
int BirthDay = 0;
Date Birthday = null;
String Marriage="",Phone="",Mobile="";
String State="",City="",Address="";
String PostCode="",IDCard="",RealPic="";
String Email="",OICQ="";
String Hobbies="",myface="";
String RegDate = "";

		RegName = user.getName();
		Question = user.getQuestion();
		Answer = user.getAnswer();
		RealName = user.getRealName();
		Career = user.getCareer();
		Gender = user.getGender();
		if (Gender.equals("M"))
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_man"); // "男";
		else if (Gender.equals("F"))
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_woman"); // "女";
		else
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_none"); // "不详";
		
		Job = user.getJob();
		Birthday = user.getBirthday();
		if (Birthday!=null) {
			Calendar cld = Calendar.getInstance();
			cld.setTime(Birthday);
			BirthYear = cld.get(Calendar.YEAR);
			BirthMonth = cld.get(Calendar.MONTH)+1;
			BirthDay = cld.get(Calendar.DAY_OF_MONTH);			
		}
		int mar = user.getMarriage();
		if (mar==1)
			Marriage = SkinUtil.LoadString(request,"res.label.userinfo","married");
		else if (mar==0)
			Marriage = SkinUtil.LoadString(request,"res.label.userinfo","not_married");
		else
			Marriage = SkinUtil.LoadString(request,"res.label.userinfo","married_none");
		Phone = user.getPhone();
		Mobile = user.getMobile();
		State = user.getState();
		City = user.getCity();
		Address = user.getAddress();
		PostCode = user.getPostCode();
		IDCard = user.getIDCard();
		RealPic = user.getRealPic();
		RegDate = com.redmoon.forum.ForumSkin.formatDateTime(request, user.getRegDate());

		Hobbies = user.getHobbies();
		Email = user.getEmail();
		OICQ = user.getOicq();
		myface = user.getMyface();
%>
<br>
<br>
<table width="80%" align="center" class="tableCommon80">
  <thead>
  <tr>
    <td colspan="5" align="center"><lt:Label res="res.label.userinfo" key="user_info"/></td>
  </tr>
  </thead>
  <tr valign="middle">
    <td width=25% align=left class="userInfo">
          <ul>
		  <li><%=user.getNick()%></li>
			<%
			  UserGroupDb ugd = user.getUserGroupDb();
			  if (!ugd.getCode().equals(UserGroupDb.EVERYONE)) {
				out.print("<li>" + ugd.getDesc() + "</li>");
			  }
			%>
			<li>
			<%if (myface.equals("")) {%>
              <img src="forum/images/face/<%=RealPic%>">
            <%}else{%>
              <img src="<%=user.getMyfaceUrl(request)%>">
            <%}%>
			</li>
			<li><img src="forum/images/<%=user.getLevelPic()%>"> <%=Gender%></li>
			<li><lt:Label res="res.label.forum.showtopic" key="rank"/><%=user.getLevelDesc()%></li>
            <%
			ScoreMgr sm = new ScoreMgr();
			Vector vscore = sm.getAllScore();
			Iterator irscore = vscore.iterator();
			String str = "";
			while (irscore.hasNext()) {
				ScoreUnit scoreUnit = (ScoreUnit) irscore.next();
				if (scoreUnit.isDisplay()) {
					out.print("<li>" + scoreUnit.getName(request) + "：" + (int)scoreUnit.getScore().getUserSum(user.getName()) + "</li>");
				}
			}
			%>
            <li><lt:Label res="res.label.forum.showtopic" key="topic_count"/><%=user.getAddCount()%></li>
            <li><lt:Label res="res.label.forum.showtopic" key="topic_elite"/><%=user.getEliteCount()%></li>
            <li><lt:Label res="res.label.forum.showtopic" key="reg_date"/><%=RegDate%></li>
            <li><lt:Label res="res.label.forum.showtopic" key="online_status"/><%
			OnlineUserDb ou = new OnlineUserDb();
			ou = ou.getOnlineUserDb(user.getName());
			if (ou.isLoaded())
				out.print(SkinUtil.LoadString(request, "res.label.forum.showtopic", "online_status_yes")); // "在线");
			else
				out.print(SkinUtil.LoadString(request, "res.label.forum.showtopic", "online_status_no")); // "离线");
			%></li>
            <%if (cfg.getBooleanProperty("forum.isOnlineTimeRecord")) {%>
			<li>
            <lt:Label res="res.label.forum.showtopic" key="online_time"/><%=(int)user.getOnlineTime()%>
            <lt:Label res="res.label.forum.showtopic" key="hour"/></li>
            <%}%>
			<%if (cfg.getBooleanProperty("forum.isFactionUsed")) {
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
				<li><lt:Label res="res.label.forum.showtopic" key="faction"/><%=faction%></li>
			<%	}
			}
						
			if (cfg.getBooleanProperty("forum.showFlowerEgg")) {
				UserPropDb up = new UserPropDb();
				up = up.getUserPropDb(user.getName());
			%>
			<li><img src="images/flower.gif">&nbsp;(<%=up.getInt("flower_count")%>)&nbsp;&nbsp;&nbsp; <img src="images/egg.gif">&nbsp;(<%=up.getInt("egg_count")%>)</li>
			<%}%>
	    </ul>	</td>
    <td colspan="4" valign="top">
<table class="tableBorder1" width="100%" border="0" cellpadding="0" cellspacing="0">
<thead>
<tr><td colspan="4">基本信息</td></tr>
</thead>
  <tr valign="middle">
    <td width="21%" height="22" align="left"><lt:Label res="res.label.forum.user" key="Gender"/></td>
    <td width="29%"><% if (Gender.equals("M"))
						out.println(SkinUtil.LoadString(request,"res.label.prision","man"));
					else if (Gender.equals("F"))
						out.println(SkinUtil.LoadString(request,"res.label.prision","woman"));
					else
						out.println(SkinUtil.LoadString(request,"res.label.prision","not_in_detail"));
				  %></td>
    <td width="21%" align="left"><lt:Label res="res.label.forum.user" key="Career"/></td>
    <td width="29%" class="l15"><%=StrUtil.toHtml(Career)%> &nbsp;</td>
  </tr>
  <tr valign="middle">
    <td height="22" align="left">E-mail</td>
    <td><%if (!user.isSecret()) {%>
        <%=StrUtil.toHtml(Email)%>
        <%}else{%>
        <lt:Label res="res.label.userinfo" key="secret"/>
        <%}%></td>
    <td width="20%" align="left"><lt:Label res="res.label.forum.user" key="Job"/></td>
    <td class="l15"><%=Job%>&nbsp;</td>
  </tr>
  <tr valign="middle">
    <td height="22" align="left">QQ</td>
    <td><%if (!user.isSecret()) {%>
        <%=OICQ%>
        <%}else{%>
        <lt:Label res="res.label.userinfo" key="secret"/>
        <%}%></td>
    <td width="20%" align="left">MSN</td>
    <td class="l15"><%if (!user.isSecret()) {%>
        <%=user.getMsn()%>
        <%}else{%>
        <lt:Label res="res.label.userinfo" key="secret"/>
        <%}%>
        &nbsp;</td>
  </tr>
  <tr valign="middle">
    <td height="22" align="left"><lt:Label res="res.label.forum.user" key="State"/></td>
    <td><%=StrUtil.toHtml(State)%>&nbsp;</td>
    <td align="left"><lt:Label res="res.label.forum.user" key="City"/></td>
    <td><%=City%>&nbsp;</td>
  </tr>
  <tr valign="middle">
    <td height="22" align="left"><lt:Label res="res.label.forum.user" key="PostCode"/></td>
    <td><%=StrUtil.toHtml(PostCode)%>&nbsp;</td>
    <td align="left"><lt:Label res="res.label.forum.user" key="Hobbies"/></td>
    <td><%=Hobbies%>&nbsp;</td>
  </tr>
  <tr valign="middle">
    <td height="22" align="left"><lt:Label res="res.label.forum.user" key="home"/></td>
    <td><a href="<%=StrUtil.toHtml(user.getHome())%>" target="_blank"><%=StrUtil.toHtml(user.getHome())%>&nbsp;</a></td>
    <td align="left"><lt:Label res="res.label.forum.user" key="marry_status"/></td>
    <td><%if (!user.isSecret()) {%>
        <%=StrUtil.toHtml(Marriage)%>
        <%}else{%>
        <lt:Label res="res.label.userinfo" key="secret"/>
        <%}%>
    </td>
  </tr>
  <tr valign="middle">
    <td height="22" colspan="4" align="left"><a href="forum/search_do.jsp?searchtype=byauthor&amp;searchwhat=<%=StrUtil.UrlEncode(user.getNick())%>">
      <lt:Label res="res.label.forum.user" key="search_user_topic"/>
      </a>&nbsp;&nbsp;<a href="forum/addfriend.jsp?friend=<%=StrUtil.UrlEncode(user.getName())%>">
        <lt:Label res="res.label.forum.showtopic" key="add_friend"/>
        </a>&nbsp;&nbsp;
      <%if (cfg.getBooleanProperty("forum.isOrderMusic")) {%>
      <a href="forum/music_order.jsp?userName=<%=StrUtil.UrlEncode(username)%>">
        <lt:Label res="res.label.userinfo" key="order_music"/>
        </a>
      <%}%></td>
  </tr>
  <tr valign="middle">
    <td height="22" colspan="4" align="left"><a href="#" onclick="hopenWin('message/send.jsp?receiver=<%=StrUtil.UrlEncode(user.getNick())%>', 320, 260)">
      <lt:Label res="res.label.userinfo" key="send_message"/>
      </a>&nbsp;&nbsp;
      <%if (com.redmoon.blog.Config.getInstance().isBlogOpen) {
	  	UserConfigDb ucd = new UserConfigDb();
		ucd = ucd.getUserConfigDbByUserName(user.getName());
		if (ucd!=null) {
	  %>
      <a target="_blank" title="<lt:Label res="res.label.forum.showtopic" key="blog"/>" href="blog/myblog.jsp?blogId=<%=ucd.getId()%>">
        <lt:Label res="res.label.forum.showtopic" key="blog"/>
        </a>
      <%}
		if (ucd==null)
			ucd = new UserConfigDb();
		cn.js.fan.base.ObjectBlockIterator oi = ucd.getGroupBlogsOwnedByUser(user.getName());
		while (oi.hasNext()) {
			ucd = (UserConfigDb)oi.next();
			out.print("&nbsp;<a target=_blank href='blog/myblog.jsp?blogId=" + ucd.getId() + "'>" + StrUtil.toHtml(ucd.getTitle()) + "</a>");		
		}	  
		BlogGroupUserDb bgu = new BlogGroupUserDb();
		Iterator qoi = bgu.getBlogGroupUserAttend(user.getName());
		UserConfigDb gucd = new UserConfigDb();
		while (qoi.hasNext()) {
			bgu = (BlogGroupUserDb)qoi.next();
			ucd = gucd.getUserConfigDb(bgu.getLong("blog_id"));
			if (ucd.isLoaded())
				out.print("&nbsp;<a target=_blank href='blog/myblog.jsp?blogId=" + ucd.getId() + "'>" + StrUtil.toHtml(ucd.getTitle()) + "</a>");
		}	  
	  }%>	  
	  </td>
  </tr>
</table>
<%
PluginMgr pm = new PluginMgr();
Vector vplugin = pm.getAllPlugin();
if (vplugin.size()>0) {
%>
<table class="tableBorder1" width="100%" style="margin-top:10px">
<thead>
<tr>
  <td>其它信息</td>
</tr>
</thead>
<tr><td>
<%
	Iterator irplugin = vplugin.iterator();
	while (irplugin.hasNext()) {
		PluginUnit pu = (PluginUnit)irplugin.next();
		if (!pu.getUserInfoPage().equals("")) {
%>
			<div style="margin-top:10px"><jsp:include page="<%=pu.getUserInfoPage()%>" flush="true" /></div>
<%		}
	}
%>
</td></tr></table>
<%	
}%>	
<div>
<%
if (privilege.isMasterLogin(request)) {
%>
<a onclick="return confirm('<lt:Label key="confirm_del"/>')" href="forum/admin/user_modify.jsp?op=deluser&username=<%=StrUtil.UrlEncode(user.getName())%>" target="_blank">
<lt:Label res="res.label.forum.admin.user_m" key="del_user_and_topic"/>
</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onclick="return confirm('<lt:Label key="confirm_del"/>')" href="forum/admin/user_modify.jsp?op=delmsg&username=<%=StrUtil.UrlEncode(username)%>" target="_blank">
<lt:Label res="res.label.forum.admin.user_m" key="del_user_topic"/>
</a>
<%
}
%>
</div></td>
  </tr>
</table>
<br>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
</html>