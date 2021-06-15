<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.lang.System"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import = "com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.rank" key="rank"/> - <%=Global.AppName%></title>
</head>
<body>
<div id="wrapper">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
	String sql = "", nick = "", sql1 = "";	
	int start = 0,end = 9;
	Vector vt = null;
	Vector vt1 = null;
	Iterator ir = null;
	Iterator ir1 = null;
	UserDb ud = new UserDb();
	String type = ParamUtil.get(request, "type");
%>
<%@ include file="inc/header.jsp"%>
<div id="main" style="padding-top:30px;">
<table width="100%" border="0" align="center">
  <tr>
    <td width="20%" valign="top">
		<table width="95%" border="0" align="center" class="tableCommon">
		  <tr>
			<td align="center" height="28"><span style="font-size:16px;color:#008dd9"><lt:Label res="res.label.forum.stats" key="stats_option"/></span></td>
		  </tr>
		  
		  <tr>
			<td align="center" height="28"><a style="font-size:16px;color:#008dd9" href="stats.jsp?type=postsrank"><lt:Label res="res.label.forum.stats" key="credits_rank"/></a></td>
		  </tr>
		  <tr>
		    <td align="center" height="28"><a style="font-size:16px;color:#008dd9" href="stats.jsp?type=online"><lt:Label res="res.label.forum.stats" key="online_rank"/></a></td>
	      </tr>
		  <tr>
			<td align="center" height="28"><a style="font-size:16px;color:#008dd9" href="stats.jsp?type=creditsrank"><lt:Label res="res.label.forum.stats" key="posts_rank"/></a></td>
		  </tr>
		  <tr>
			<td align="center" height="28"><a style="font-size:16px;color:#008dd9" href="stats.jsp?type=team"><lt:Label res="res.label.forum.stats" key="team"/></a></td>
		  </tr>
	  </table>
	</td>
    <td width="80%" valign="top">	
<%
	if(type.equals("postsrank")){
%>
		<table width="100%" border="0" cellpadding="0" cellspacing="0" class="tableCommon">
		  <thead>
		  <tr>
			<td colspan="2" align="center" height="26"><lt:Label res="res.label.forum.stats" key="credits_rank"/></td>
		  </tr>
		  </thead>
		  <tr>
			<td width="48%">
				<table width="100%" border="0" class="tableBorder1">
				  <tr height="25">
					<td colspan="2" align="center"><lt:Label res="res.label.forum.stats" key="add_count_rank"/></td>
			      </tr>
<%
	int addCount = 0;
	vt = ud.list(SQLBuilder.getRankAddCount(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		addCount = ud.getAddCount();
%>
				  <tr height="25">
				  	<td align="center" width="50%"><a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>"><%=nick%></a></td>
				    <td align="center" width="50%"><%=addCount%></td>
				  </tr>
<%
	}
%>				
				</table>			</td>
			<td width="48%">
				<table width="100%" border="0" class="tableBorder1">
				  <tr height="25">
					<td colspan="2" align="center"><lt:Label res="res.label.forum.stats" key="elite_count_rank"/></td>
			      </tr>
<%
	int eliteCount = 0;
	sql = "select name from sq_user order by eliteCount desc";
	vt = ud.list(sql,start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		eliteCount = ud.getEliteCount();
%>
				  <tr height="25">
					<td align="center" width="50%"><a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>"><%=nick%></a></td>
				    <td align="center" width="50%"><%=eliteCount%></td>
				  </tr>
<%
	}
%>				
				</table>						</td>
		  </tr>
	  </table>
<%
}else{
	if(type.equals("creditsrank")){
%>	  


		<table width="100%" class="tableCommon">
		  <thead>
		  <tr>
			<td colspan="3" align="center" height="26"><lt:Label res="res.label.forum.stats" key="posts_rank"/></td>
		  </tr>
		  </thead>
		  <tr>
			<td width="33%">
				<table width="100%" class="tableBorder1">
				  <tr height="25">
					<td colspan="2" align="center"><lt:Label res="res.label.forum.stats" key="credit_rank"/></td>
			      </tr>
<%
    int credit = 0;
	vt = ud.list(SQLBuilder.getRankCredit(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		credit = ud.getCredit();
%>
				  <tr height="25">
				  	<td align="center" width="50%"><a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>"><%=nick%></a></td>
				    <td align="center" width="50%"><%=credit%></td>
				  </tr>
<%
	}
%>				
				</table>			</td>
			<td width="33%">
				<table width="100%" class="tableBorder1">
				  <tr height="25">
					<td colspan="2" align="center"><lt:Label res="res.label.forum.stats" key="experience_rank"/></td>
			      </tr>
<%
    int experience = 0;
	vt = ud.list(SQLBuilder.getRankExperience(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		experience = ud.getExperience();
%>
				  <tr height="25">
					<td align="center" width="50%"><a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>"><%=nick%></a></td>
				    <td align="center" width="50%"><%=experience%></td>
				  </tr>
<%
	}
%>				
				</table>			
			</td>
			<td width="33%">
				<table width="100%" class="tableBorder1">
				  <tr height="25">
					<td colspan="2" align="center"><lt:Label res="res.label.forum.stats" key="gold_rank"/></td>
			      </tr>
<%
    int gold = 0;
	vt = ud.list(SQLBuilder.getRankGold(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		gold = ud.getGold();
%>
				  <tr height="25">
					<td align="center" width="50%"><a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>"><%=nick%></a></td>
				    <td align="center" width="50%"><%=gold%></td>
				  </tr>
<%
	}
%>				
				</table>
			</td>
		  </tr>
	  </table>
<%
	}else if (type.equals("online")){%>
	 <table width="100%" class="tableCommon">
     <thead>
		  <tr>
			<td colspan="6" align="center" height="26"><lt:Label res="res.label.forum.stats" key="online_rank"/></td>
		  </tr>
          </thead>
		  <tr height="25" align="center">
			<td width="12%"><lt:Label res="res.label.forum.stats" key="user_name"/></td>
			<td width="26%"><lt:Label res="res.label.forum.stats" key="last_time"/></td>
			<td width="11%"><lt:Label res="res.label.forum.stats" key="experience"/></td>
			<td width="12%"><lt:Label res="res.label.forum.stats" key="add_count"/></td>
			<td width="12%"><lt:Label res="res.label.forum.stats" key="elite_count"/></td>
		    <td><lt:Label res="res.label.forum.stats" key="online_time"/></td>
	      </tr>
<%
        ObjectBlockIterator oi = ud.listUserRank("online_time", 10);
        while (oi.hasNext()) {
            ud = (UserDb) oi.next();
%>
		  <tr height="25" align="center">
			<td width="12%"><a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>"><%=ud.getNick()%></a></td>
			<td width="16%"><%=DateUtil.format(ud.getLastTime(), "yyyy-MM-dd HH:mm")%></td>
			<td width="11%"><%=ud.getExperience()%></td>
			<td width="12%"><%=ud.getAddCount()%></td>
			<td width="12%"><%=ud.getEliteCount()%></td>
		    <td><%=(int)ud.getOnlineTime()%></td>
	      </tr>
<%
		}%>
	  </table>	
	<%}
	else {
%>
	 <table width="100%" class="tableCommon">
     <thead>
		  <tr>
			<td colspan="7" align="center" height="26"><lt:Label res="res.label.forum.stats" key="team"/></td>
		  </tr>
          </thead>
		  <tr height="25" align="center">
			<td width="12%"><lt:Label res="res.label.forum.stats" key="user_name"/></td>
			<td width="20%"><lt:Label res="res.label.forum.stats" key="manage"/></td>
			<td width="16%"><lt:Label res="res.label.forum.stats" key="last_time"/></td>
			<td width="11%"><lt:Label res="res.label.forum.stats" key="experience"/></td>
			<td width="12%"><lt:Label res="res.label.forum.stats" key="add_count"/></td>
			<td width="12%"><lt:Label res="res.label.forum.stats" key="elite_count"/></td>
		    <td><lt:Label res="res.label.forum.stats" key="online_time"/></td>
	      </tr>
<%
		sql = "select distinct name from sq_boardmanager";
		BoardManagerDb bmd = new BoardManagerDb();
		Leaf lf = null;
		Directory dir = new Directory();
		vt = ud.list(sql);
		ir = vt.iterator();
		while(ir.hasNext()){
			ud = (UserDb)ir.next();
			sql1 = "select boardcode, name from sq_boardmanager where name=" + StrUtil.sqlstr(ud.getName());
			vt1 = bmd.list(sql1);
			ir1 = vt1.iterator();
%>
		  <tr height="25" align="center">
			<td width="12%"><a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>"><%=ud.getNick()%></a></td>
			<td width="20%">
			<%
				while(ir1.hasNext()){
					bmd = (BoardManagerDb)ir1.next();
					lf = dir.getLeaf(bmd.getBoardCode());
					if (lf==null) {
						out.print(bmd.getBoardCode());
						continue;
					}
					out.print("<a href='listtopic.jsp?boardcode=" + StrUtil.UrlEncode(lf.getCode()) + "' target=_blank>" + lf.getName() + "</a>&nbsp;&nbsp;");
				}
			%>			</td>
			<td width="16%"><%=DateUtil.format(ud.getLastTime(), "yyyy-MM-dd HH:mm")%></td>
			<td width="11%"><%=ud.getExperience()%></td>
			<td width="12%"><%=ud.getAddCount()%></td>
			<td width="12%"><%=ud.getEliteCount()%></td>
		    <td><%=(int)ud.getOnlineTime()%></td>
	      </tr>
<%
		}%>
	  </table>
<%	}
}
%>	  
	  
	</td>
  </tr>
</table>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
