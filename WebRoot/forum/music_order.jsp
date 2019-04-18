<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.base.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.music.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("order")) {
	MusicUserMgr mum = new MusicUserMgr();
	String userName = ParamUtil.get(request, "userName");
	long musicId = ParamUtil.getLong(request, "musicId");
	boolean re = false;
	try {
		re = mum.orderMusicForUser(request, userName, musicId);
		if (re) {
			out.print(StrUtil.Alert_Redirect("点歌成功！", "music_order.jsp?userName=" + StrUtil.UrlEncode(userName)));
		}
		else
			out.print(StrUtil.Alert_Back("点歌失败！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}

String boardcode = ParamUtil.get(request, "boardcode");

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><%=Global.AppName%> - <lt:Label res="res.label.forum.showonline" key="view_online"/></title>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
if (!cfg.getBooleanProperty("forum.isOrderMusic")) {
	out.print(StrUtil.Alert_Back("点歌服务尚未开通！"));
	return;
}

int gold = cfg.getIntProperty("forum.orderMusicGold");
ScoreMgr sm = new ScoreMgr();
ScoreUnit su = sm.getScoreUnit("gold");

String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	out.print(StrUtil.Alert_Back("请选择用户"));
	return;
}
UserMgr um = new UserMgr();
UserDb user = um.getUser(userName);
if (!user.isLoaded()) {
	out.print(StrUtil.Alert_Back("用户" + userName + "不存在！"));
	return;
}
%>
<div class="tableTitle">点歌送给&nbsp;-&nbsp;<%=user.getNick()%></div>
<%
		String what = ParamUtil.get(request, "what");
		String dirCode = ParamUtil.get(request, "dirCode");
		
		int pagesize = 20;
		String sql = "select id from sq_forum_music_file";
		if (!dirCode.equals(""))
			sql += " where dir_code=" + StrUtil.sqlstr(dirCode);
		if (op.equals("search")) {
			if (dirCode.equals(""))
				sql = "select id from sq_forum_music_file where name like " + StrUtil.sqlstr("%" + what + "%");
			else
				sql = "select id from sq_forum_music_file where dir_code=" + StrUtil.sqlstr(dirCode) + " and name like " + StrUtil.sqlstr("%" + what + "%");
		}
		sql += " order by name";
		
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		MusicFileDb mfd = new MusicFileDb();
		
		long total = mfd.getObjectCount(sql);
		com.cloudwebsoft.framework.base.ObjectBlockIterator oir = mfd.getObjects(sql, (curpage-1)*pagesize, curpage*pagesize);
		
		paginator.init(total, pagesize);
		
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
<form name=form1 action="music_order.jsp?op=search" method=post>
<table width="98%" border="0" align="center" class="per100">
    <tr>
      <td align="right">名称
        <input name="what" value="<%=what%>">
        <input name="dirCode" value="<%=dirCode%>" type=hidden>
        <input name="boardcode" value="<%=boardcode%>" type=hidden>        &nbsp;&nbsp;
        <input name="submit" type="submit" value="搜索">      </td>
    </tr>
</table>
</form>
<TABLE width="98%" border=1 align=center cellPadding=1 cellSpacing=0 class="tableCommon">
    <thead>
      <TR> 
        <TD width="31%" height=23>歌曲</TD>
        <TD width="32%" height=23>目录</TD>
        <TD width="25%" height=23>点歌次数</TD>
        <TD width="12%" height=23>操作</TD>
      </TR>
	</thead>
    <TBODY>
<%
MusicDirMgr mdm = new MusicDirMgr();
while (oir.hasNext()) {
 	    mfd = (MusicFileDb)oir.next();
		MusicDirDb mdd = mdm.getMusicDirDb(mfd.getDirCode());
		String dirName = "";
		if (mdd!=null) {
			dirName = mdd.getName();
		}
%>
      <TR align=center> 
        <TD height=23 align="left"><%=mfd.getName()%></TD>
        <TD height=23><a href="music_order.jsp?dirCode=<%=mdd.getCode()%>"><%=dirName%></a></TD>
        <TD height=23><%=mfd.getDownloadCount()%></TD>
        <TD height=23><a href="#" onClick="if (confirm('您确定要点歌送给<%=StrUtil.toHtml(user.getNick())%>吗？这将消耗您<%=su.getName(request)%> <%=gold%>')) window.location.href='music_order.jsp?op=order&userName=<%=userName%>&musicId=<%=mfd.getId()%>'">点歌</a></TD>
      </TR>
<%}%>
    </TBODY>
  </TABLE>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td width="2%" height="23">&nbsp;</td>
      <td height="23" valign="baseline"> <div align="right"> 
    <%
	  String querystr = "op=" + op + "&what=" + StrUtil.UrlEncode(what) + "&dirCode=" + dirCode + "&boardcode=" + boardcode;
 	  out.print(paginator.getCurPageBlock(request, "music_order.jsp?"+querystr));
	%>
	</div>	  </td>
    </tr>
  </table>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
