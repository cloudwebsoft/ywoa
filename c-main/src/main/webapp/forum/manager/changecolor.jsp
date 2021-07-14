<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
	long id = 0;
	try {
		id = ParamUtil.getLong(request, "id");
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	MsgMgr mm = new MsgMgr();
	MsgDb md = mm.getMsgDb(id);
	String boardCode = md.getboardcode();
	
	if (!privilege.canManage(request, id)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"res.label.forum.manager","error_pvg") + "\\n" + SkinUtil.LoadString(request,"res.label.forum.manager","check_ok"), "../treasure_use.jsp?boardcode=" + StrUtil.UrlEncode(boardCode) + "&id="+id));
		return;
	}

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><lt:Label res="res.label.forum.manager" key="toptic_op"/> - <%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<meta http-equiv="x-ua-compatible" content="ie=7" />
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<%@ include file="../inc/position.jsp"%>
<jsp:useBean id="topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
	String querystring = StrUtil.getNullString(request.getQueryString());
	String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");

	boolean re = false;
	String op = ParamUtil.get(request, "op");
	if (op.equals("changeColor")) {
		try {
			re = topic.ChangeColor(request);
			if (re) {
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_success")));
			}
			else {
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_fail")));
			}
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.p_center(e.getMessage(),"red"));
		}
	}
	if (op.equals("changeBold")) {
		try {
			re = topic.ChangeBold(request);
			if (re) {
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_success")));
			}
			else {
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_fail")));
			}
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.p_center(e.getMessage(),"red"));
		}
	}
%>
<br>
<TABLE class="tableCommon60" width="55%" height=144 align="center" cellPadding=3 cellSpacing=0>
      <thead>
        <TR>
          <TD height=22 colSpan=2 align="center"><lt:Label res="res.label.forum.manager" key="topic_color"/></TD>
        </TR>
	  </thead>
      <TBODY>
        <TR align="center">
          <TD width="18%" height=22 align="left"><%
		Leaf lf = new Leaf();
		lf = lf.getLeaf(boardCode);
		%>
              <lt:Label res="res.label.forum.manager" key="board"/></TD>
          <TD width="82%" height=22 align="left"><a href="../listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(boardCode)%>"><%=lf.getName()%></a> <b>&raquo;</b>
              <%if (md.isBold()) out.print("<B>");%>
              <%
		String color = StrUtil.getNullString(md.getColor());
		if (color.equals("")) {%>
              <a href="../showtopic_tree.jsp?rootid=<%=md.getRootid()%>&showid=<%=id%>"><%=StrUtil.toHtml(md.getTitle())%></a>
              <%}else{%>
              <a href="../showtopic_tree.jsp?rootid=<%=md.getRootid()%>&showid=<%=id%>"><font color="<%=color%>"><%=StrUtil.toHtml(md.getTitle())%></font></a>
              <%}%>
              <%if (md.isBold()) out.print("</B>");%></TD>
        </TR><form name="form1" method="post" action="?op=changeColor">
        <TR align="center">
          <TD height=11 align="left">
  <select name="color">
    <option value="" style="COLOR: black" selected><lt:Label res="res.label.forum.manager" key="clear_color"/></option>
    <option style="BACKGROUND: #000088" value="#000088"></option>
    <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
    <option style="BACKGROUND: #008800" value="#008800"></option>
    <option style="BACKGROUND: #008888" value="#008888"></option>
    <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
    <option style="BACKGROUND: #00a010" value="#00a010"></option>
    <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
    <option style="BACKGROUND: #111111" value="#111111"></option>
    <option style="BACKGROUND: #333333" value="#333333"></option>
    <option style="BACKGROUND: #50b000" value="#50b000"></option>
    <option style="BACKGROUND: #880000" value="#880000"></option>
    <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
    <option style="BACKGROUND: #888800" value="#888800"></option>
    <option style="BACKGROUND: #888888" value="#888888"></option>
    <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
    <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
    <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
    <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
    <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
    <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
    <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
    <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
    <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
    <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
    <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
    <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
    <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
    <option style="BACKGROUND: #000000" value="#000000"></option>
  </select>
  <script>
		form1.color.value = "<%=color%>";
  </script>
  <input type=hidden name="id" value="<%=id%>">
  <input type=hidden name="boardcode" value="<%=boardCode%>"></TD>
          <TD height=11 align="left">
            <lt:Label res="res.label.forum.manager" key="clr_bold_to_date"/>
            <input name=colorExpire size=10 readonly value="<%=DateUtil.format(md.getColorExpire(), "yyyy-MM-dd")%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "colorExpire",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
<input type="submit" name="Submit" value="<%=SkinUtil.LoadString(request,"ok")%>"></TD>
        </TR></form>
<form name="form2" method="post" action="?op=changeBold">
        <TR align="center">
          <TD height=11 align="left">
  
  <input type=hidden name="id" value="<%=id%>">
  <input type=checkbox name=isBold value="1" <%=md.isBold()?"checked":""%>>
  <lt:Label res="res.label.forum.manager" key="blod_display"/>		  <input type=hidden name="boardcode" value="<%=boardCode%>"></TD>
          <TD height=11 align="left">
  <lt:Label res="res.label.forum.manager" key="clr_bold_to_date"/>
  <input name=boldExpire size=10 readonly value="<%=DateUtil.format(md.getBoldExpire(), "yyyy-MM-dd")%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "boldExpire",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
<input type="submit" name="Submit2" value="<%=SkinUtil.LoadString(request,"ok")%>">&nbsp;</TD>
        </TR></form>
        <TR align="center">
          <TD colSpan=2 height=32>&nbsp;</TD>
        </TR>
      </TBODY>
  </TABLE>
  <br>
</div>
<%@ include file="../inc/footer.jsp"%>
</div>
</BODY></HTML>
