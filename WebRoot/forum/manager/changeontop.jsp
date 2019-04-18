<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
long id = ParamUtil.getLong(request, "id");
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
	MsgMgr mm = new MsgMgr();
	MsgDb md = mm.getMsgDb(id);	
	if (!privilege.canManage(request, id)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"res.label.forum.manager","error_pvg") + "\\n" + SkinUtil.LoadString(request,"res.label.forum.manager","check_ok"), "../treasure_use.jsp?boardcode=" + StrUtil.UrlEncode(md.getboardcode()) + "&id="+id));
		return;
	}
	String querystring = StrUtil.getNullString(request.getQueryString());
	String privurl = ParamUtil.get(request, "privurl");
	String boardcode = ParamUtil.get(request, "boardcode");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><lt:Label res="res.label.forum.manager" key="toptic_op"/> - <%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<br>
<FORM action="manage.jsp" method="post" name="form1">
<TABLE class="tableCommon60" width="55%" align="center" cellPadding=3 cellSpacing=0 border=1>
	<thead>
        <TR>
          <TD height=22 colSpan=2 align="center"><lt:Label res="res.label.forum.manager" key="setontop"/></TD>
        </TR>
	</thead>
      <TBODY>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="topic"/></TD>
          <TD height=22 align="left">
        <a href="../showtopic.jsp?rootid=<%=md.getRootid()%>"><%=StrUtil.toHtml(md.getTitle())%></a><BR>		</TD>
        </TR>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="msg_level"/></TD>
          <TD height=22 align="left">
		  <select name="value">
		  <%if (privilege.isMasterLogin(request)) {%>
		  <option value="<%=MsgDb.LEVEL_TOP_FORUM%>"><lt:Label res="res.label.forum.showtopic" key="top_forum"/></option>
		  <%}%>
		  <option value="<%=MsgDb.LEVEL_TOP_BOARD%>"><lt:Label res="res.label.forum.showtopic" key="top_board"/></option>
		  <%if (md.getLevel()!=MsgDb.LEVEL_NONE) {%>
		  <option value="<%=MsgDb.LEVEL_NONE%>"><lt:Label res="res.label.forum.showtopic" key="top_none"/></option>
		  <%}%>
		  </select>
		  <%if (md.getLevel()!=MsgDb.LEVEL_NONE) {%>
		  <script>
		  form1.value.value="<%=md.getLevel()%>";
		  </script>
		  <%}%>		  </TD>
        </TR>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="clr_bold_to_date"/></TD>
          <TD height=22 align="left"><input type=hidden name=id value="<%=id%>">
		  <input type=hidden name=privurl value="<%=privurl%>">
		  <input readonly type="text" id="levelExpire" name="levelExpire" size="20" value="<%=DateUtil.format(md.getLevelExpire(), "yyyy-MM-dd HH:mm:ss")%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "levelExpire",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script><input type=hidden name=action value="setOnTop"></TD>
        </TR>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="reason"/></TD>
          <TD height=22 align="left"><select name="onTopReason" onChange="form1.reason.value+=this.value">
            <option value="">
              <lt:Label key="wu"/>
            </option>
            <%
			BasicDataMgr bdm = new BasicDataMgr();
			%>
            <%=bdm.getOptionsStr("onTopReason", false)%>
          </select></TD>
        </TR>
        <TR align="center">
          <TD width="18%" height=22 align="left"><lt:Label res="res.label.forum.manager" key="desc"/></TD>
          <TD width="82%" height=22 align="left"><textarea name="reason" cols="40" rows="6" ></textarea></TD>
        </TR>
        <TR align="center">
          <TD colSpan=2 height=32><input type="submit" name="Submit2" value="<%=SkinUtil.LoadString(request,"ok")%>"></TD>
        </TR>
      </TBODY>
</TABLE>
</FORM>
<br>
</div>
<%@ include file="../inc/footer.jsp"%>
</div>
</BODY></HTML>
