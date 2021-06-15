<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.nav.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.err.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="com.redmoon.forum.miniplugin.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%
// 判断是否该显示边栏
String strIsSideBar = ParamUtil.get(request, "isSideBar");
if (!strIsSideBar.equals("")) {
	int maxAge = 60 * 60 * 24 * 30; // 保存一月
	if (strIsSideBar.equals("y")) {
		// CookieBean.addCookie(response, com.redmoon.forum.ui.ForumPage.COOKIE_IS_SIDEBAR, "y", "/", maxAge);
	}
	else {
		strIsSideBar = "n";
		// CookieBean.addCookie(response, com.redmoon.forum.ui.ForumPage.COOKIE_IS_SIDEBAR, "n", "/", maxAge);
	}
	%>
	<script>
	set_cookie("<%=com.redmoon.forum.ui.ForumPage.COOKIE_IS_SIDEBAR%>", "<%=strIsSideBar%>");
	</script>
	<%
}

Privilege sidePvg = new Privilege();
com.redmoon.forum.Config cfgSideBar = com.redmoon.forum.Config.getInstance();
// 判断是否该显示框架
if (strIsSideBar.equals(""))
	strIsSideBar = CookieBean.getCookieValue(request, com.redmoon.forum.ui.ForumPage.COOKIE_IS_SIDEBAR);
boolean isSideBar = false;
if (strIsSideBar.equals("y"))
	isSideBar = true;
else {
	if (strIsSideBar.equals(""))
		isSideBar = cfgSideBar.getBooleanProperty("forum.isSideBar");
}
if (isSideBar) {
%>
		<div id="sideBar" isDisplayed=1>
			<div id="userInfo">
			<%
			if (sidePvg.isUserLogin(request)) {
				UserDb me = new UserDb();
				me = me.getUser(com.redmoon.forum.Privilege.getUser(request));
				int msgCountSide = 0;
				Integer msgCountObj = (Integer)request.getAttribute("msgCount");
				if (msgCountObj!=null) {
					msgCountSide = msgCountObj.intValue();
				}
			%>
              <div id="closeSide">
					<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
						<tr>
                            <td width="50%"></td>
							<td style="line-height:18px" align="right"><a href="#" onClick="switchSide()"><lt:Label res="res.label.forum.index" key="close_side"/></a></td>
							<td width="5%" align="right"><a href="#" onClick="switchSide()"><img onMouseOver="this.src='<%=SkinMgr.getSkinPath(request)%>/images/close_onhover.gif'" onMouseOut="this.src='<%=SkinMgr.getSkinPath(request)%>/images/close.gif'" src="<%=SkinMgr.getSkinPath(request)%>/images/close.gif" /></a></td>
						</tr>
				  </table>
				</div>
                <div id="userImg"><a href="<%=Global.getRootPath()%>/usercenter.jsp"><%
				String myface = me.getMyface();
				String RealPic = me.getRealPic(); 
				if (myface.equals("")) {%>
                    <img src="images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>"/>
                <%}else{%>
                    <img src="<%=me.getMyfaceUrl(request)%>" />
                <%}%></a></div>
                
                <div id="sideLogin">
				<a href="<%=Global.getRootPath()%>/usercenter.jsp"><%=me.getNick()%></a><br />
				<img src="images/<%=me.getLevelPic()%>" /><br />
				<%=me.getLevelDesc()%><br />
				<a href="javascript:hopenWin('<%=Global.getRootPath()%>/message/message.jsp',320,260)"><lt:Label res="res.label.forum.index" key="msgbox"/>(<font class="redfont"><%=msgCountSide%></font>)</a>
				</div>		
			<%
			}else{
			%>
                <div id="closeSide">
					<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
						<tr>
                            <td width="50%"></td>
							<td style="line-height:18px" align="right"><a href="#" onClick="switchSide()"><lt:Label res="res.label.forum.index" key="close_side"/></a></td>
							<td width="5%" align="right"><a href="#" onClick="switchSide()"><img onMouseOver="this.src='<%=SkinMgr.getSkinPath(request)%>/images/close_onhover.gif'" onMouseOut="this.src='<%=SkinMgr.getSkinPath(request)%>/images/close.gif'" src="<%=SkinMgr.getSkinPath(request)%>/images/close.gif" /></a></td>
						</tr>
				  </table>
				</div>
				<div id="userImg"><img src="<%=SkinMgr.getSkinPath(request)%>/images/default.jpg" /></div>
				
				<div id="sideUnLogin">
				<a href="javascript:openWinLayer('', '', false, 500, 300, -1, 'loginSaveDate,covered');ajaxpage('ajax_login.jsp','popLayer')"><img width="66" height="27" src="<%=SkinMgr.getSkinPath(request)%>/images/box_login.gif"/></a><a href="../regist.jsp"><img width="66" height="27" src="<%=SkinMgr.getSkinPath(request)%>/images/box_register.gif"/></a>
				</div>
			<%}%>
			</div>
			<%
			Home home = Home.getInstance();
			Iterator irHome = home.getBlocks().iterator();
			String sideItem = "sideItem";
			int sideM = 0;
			while (irHome.hasNext()) {
				String[] ary = (String[])irHome.next();
			%>
			<div class="sideBox">
				<div class="menu">
					<ul id="<%=sideItem+sideM%>">
						<li class="currentItem" onMouseOver="switchMenu('<%=sideItem+sideM%>', this)"><%=home.getDesc(request, ary[0])%></li>
						<li class="item" onMouseOver="switchMenu('<%=sideItem+sideM%>', this)"><%=home.getDesc(request, ary[1])%></li>
						<li class="item" onMouseOver="switchMenu('<%=sideItem+sideM%>', this)"><%=home.getDesc(request, ary[2])%></li>
					</ul>
				</div>
				<div class="content">
					<%for (int sideI=0; sideI<3; sideI++) {%>
						<ul id="<%=sideItem+sideM%>Content<%=sideI%>" style="display:<%=sideI!=0?"none":""%>">
						<%
						int sideK = 1;
						Iterator irBlock = home.getBlockList(request, ary[sideI], StrUtil.toInt(ary[3])).iterator();
						while (irBlock.hasNext()) {
							String linkSide = (String)irBlock.next();
						%>
							<%if (ary[4].equals("true")) {%>
							<li>
							<img src="<%=SkinMgr.getSkinPath(request)%>/images/<%=sideK%>.gif" />&nbsp;
							<%}%>
							<%=linkSide%>
							<%if (ary[4].equals("true")) {%>
							</li>
							<%}%>
						<%
							sideK++;
						}
						%>
						</ul>
					<%}%>
				</div>
			</div>
			
			<%
				sideM ++;
			}
			%>
			
		</div>
<%}else{%>
<script>
adjustWithoutSide()
</script>
<%}%>