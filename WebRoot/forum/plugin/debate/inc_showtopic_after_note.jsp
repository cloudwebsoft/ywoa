<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.debate.*"%>
<%@ page import="com.redmoon.forum.plugin.score.*"%>
<%
MsgMgr mm = new MsgMgr();
long rootid = ParamUtil.getLong(request, "rootid");
MsgDb rootMsgDb = mm.getMsgDb(rootid);

String boardcode = rootMsgDb.getboardcode();
UserSession.setBoardCode(request, boardcode);

Leaf msgLeaf = new Leaf();
msgLeaf = msgLeaf.getLeaf(boardcode);

String querystring = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring);

// 取得皮肤路径
String skincode = msgLeaf.getSkin();
if (skincode.equals("") || skincode.equals(UserSet.defaultSkin)) {
	skincode = UserSet.getSkin(request);
	if (skincode==null || skincode.equals(""))
		skincode = UserSet.defaultSkin;
}	
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

DebateDb dd = new DebateDb();
dd = dd.getDebateDb(rootid);
%>
<style type="text/css">
<!--
.STYLE1 {font-size: 14px}
.STYLE3 {font-size: 14px; font-weight: bold; }
.STYLE4 {
	color: #FF0000;
	font-weight: bold;
	font-size: 18px;
}

a.viewpoint {
color:#FF0000;
}
a.viewpoint:visited {
color:#FF0000;
}
-->
</style>
<table width="98%"  border="1" align="center" cellpadding="5" cellspacing="0" bordercolor="<%=skin.getTableBorderClr()%>">
  <tr>
    <td height="24" colspan="2" align="center" background="<%=skinPath%>/images/bg1.gif" class="text_title"><strong>辩论</strong></td>
  </tr>
  <tr>
    <td width="50%" valign="top" background="plugin/debate/images/bg_l.gif">
	<div style="height:40px; width:100%"><img src="plugin/debate/images/br_1.gif" align="absmiddle" />
	<span class="STYLE1"><%=rootMsgDb.getTitle()%></span>	</div>
	<span style="line-height:150%"><span class="STYLE1"><strong>背景资料</strong></span><br />
    正方：<%=StrUtil.toHtml(dd.getViewpoint1())%><br />	
    <br />
	<span>反方：</span><%=StrUtil.toHtml(dd.getViewpoint2())%><br />
	<br />
	<table width="98%" height="100">
      <tr>
        <td width="3%" height="46">&nbsp;</td>
        <td width="42%" align="center" bgcolor="D6EBF7"><span class="STYLE3">支持方得票数 <%=dd.getVoteCount1()%> 票 </span></td>
        <td width="9%" align="center"><span class="STYLE4">VS</span></td>
        <td width="42%" align="center" bgcolor="D6EBF7"><span class="STYLE3">反对方票数 <%=dd.getVoteCount2()%> 票 </span></td>
        <td width="4%">&nbsp;</td>
      </tr>
      <tr>
        <td>&nbsp;</td>
        <td colspan="3" align="center"><%
// 到期检查
boolean isExpired = false;
if (DateUtil.compare(dd.getEndDate(), new java.util.Date())==2) {
	if (!DateUtil.isSameDay(dd.getEndDate(), new java.util.Date())) {
		isExpired = true;
	}
}
if (isExpired) {
	String s = "";
	if (dd.getUserCount1()>dd.getUserCount2())
		s  = "正方";
	else
		s = "反方";
	out.print("该辩论已过期，" + s + "获胜！");
}
else {
%>
          <img src="images/emot/em42.gif" />&nbsp;<a href="#" onclick="hopenWin('plugin/debate/debate_do.jsp?op=vote_support&msgId=<%=rootid%>', 480, 320)">
		  投支持票
		  </a>&nbsp;&nbsp;<img src="images/emot/em41.gif" />&nbsp;<a href="#" onclick="hopenWin('plugin/debate/debate_do.jsp?op=vote_oppose&msgId=<%=rootid%>', 480, 320)">投反对票</a>
          <%}%></td>
        <td>&nbsp;</td>
      </tr>
    </table>	</td>
    <td valign="top" background="plugin/debate/images/bg_r.gif"><table width="98%">
        <tr>
          <td width="15%" rowspan="2" valign="top"><object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,19,0" width="85" height="81">
            <param name="movie" value="plugin/debate/images/clock1.swf" />
            <param name="quality" value="high" />
			<PARAM NAME=wmode value=transparent>
            <embed src="plugin/debate/images/clock1.swf" quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" width="85" height="81"></embed>
          </object></td>
          <td width="85%"><span class="STYLE1">辩论时间：</span></td>
        </tr>
        <tr>
          <td height="65"><span class="STYLE1"><strong>开始时间：<%=ForumSkin.formatDate(request, dd.getBeginDate())%></strong><strong><br />
结束时间：<%=ForumSkin.formatDate(request, dd.getEndDate())%></strong></span></td>
        </tr>
        <tr>
          <td colspan="2" valign="top" height="2px" background="plugin/debate/images/br_line.gif"></td>
        </tr>
      </table>
      <strong><span class="STYLE1"><br />
      </span></strong><span class="STYLE1">
    人气：<br />
    <br />
    <strong>正方人数：<%=dd.getUserCount1()%>人<br />
    反方人数：<%=dd.getUserCount2()%>人<br />
    第三方：<%=dd.getUserCount3()%>人</strong></span></td>
  </tr>
  <tr>
    <td height="31" valign="top" background="plugin/debate/images/bg_l.gif"><table border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td width="65" height="22" align="center" style="background-image:url(plugin/debate/images/btn_bg.gif)">
<%
String viewpointType = ParamUtil.get(request, "viewpointType");
%>
		<a class="<%=viewpointType.equals("")?"viewpoint":""%>" href="showtopic.jsp?rootid=<%=rootid%>">全部观点</a></td>
        <td width="5" align="center">&nbsp;</td>
        <td width="65" align="center" style="background-image:url(plugin/debate/images/btn_bg.gif)"><a class="<%=viewpointType.equals("" + DebateViewpointDb.TYPE_SUPPORT)?"viewpoint":""%>" href="showtopic.jsp?rootid=<%=rootid%>&amp;pluginCode=<%=DebateUnit.code%>&amp;viewpointType=<%=DebateViewpointDb.TYPE_SUPPORT%>">支持观点</a></td>
        <td width="5" align="center">&nbsp;</td>
        <td width="65" align="center" style="background-image:url(plugin/debate/images/btn_bg.gif)"><a class="<%=viewpointType.equals("" + DebateViewpointDb.TYPE_OPPOSE)?"viewpoint":""%>" href="showtopic.jsp?rootid=<%=rootid%>&amp;pluginCode=<%=DebateUnit.code%>&amp;viewpointType=<%=DebateViewpointDb.TYPE_OPPOSE%>">反对观点</a></td>
        <td width="5" align="center">&nbsp;</td>
        <td width="65" align="center" style="background-image:url(plugin/debate/images/btn_bg.gif)"><a class="<%=viewpointType.equals("" + DebateViewpointDb.TYPE_OTHERS)?"viewpoint":""%>" href="showtopic.jsp?rootid=<%=rootid%>&amp;pluginCode=<%=DebateUnit.code%>&amp;viewpointType=<%=DebateViewpointDb.TYPE_OTHERS%>">第三方观点</a></td>
      </tr>
    </table>
    </td>
    <td valign="top" background="plugin/debate/images/bg_r.gif">
	<%
		String replypage = "addreply_new.jsp";
		if (com.redmoon.forum.Config.getInstance().getBooleanProperty("forum.isWebeditTopicEnabled") && msgLeaf.getWebeditAllowType()==Leaf.WEBEDIT_ALLOW_TYPE_REDMOON_FIRST) {
			replypage = "addreply_we.jsp";
		}
	%>
	<table border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td width="65" height="22" align="center" style="background-image:url(plugin/debate/images/btn_bg.gif)"><a href="<%=replypage%>?viewpoint_type=<%=DebateViewpointDb.TYPE_SUPPORT%>&boardcode=<%=boardcode%>&replyid=<%=rootid%>&privurl=<%=privurl%>">我支持</a></td>
        <td width="5" align="center">&nbsp;</td>
        <td width="65" align="center" style="background-image:url(plugin/debate/images/btn_bg.gif)"><a href="<%=replypage%>?viewpoint_type=<%=DebateViewpointDb.TYPE_OPPOSE%>&boardcode=<%=boardcode%>&replyid=<%=rootid%>&privurl=<%=privurl%>">我反对</a></td>
        <td width="5" align="center">&nbsp;</td>
        <td width="65" align="center" style="background-image:url(plugin/debate/images/btn_bg.gif)"><a href="<%=replypage%>?viewpoint_type=<%=DebateViewpointDb.TYPE_OTHERS%>&boardcode=<%=boardcode%>&replyid=<%=rootid%>&privurl=<%=privurl%>">第三方观点</a></td>
        </tr>
    </table>
	</td>
  </tr>
</table>
<br />
