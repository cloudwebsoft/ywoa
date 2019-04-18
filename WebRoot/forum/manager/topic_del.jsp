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
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
String privurl = ParamUtil.get(request, "privurl");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE><lt:Label res="res.label.forum.showtopic" key="topic_del"/> - <%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<BODY>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<%@ include file="../inc/position.jsp"%>
<br>
<FORM action="../deltopic.jsp" method="post" name="form1">
<TABLE class="tableCommon60" width="55%" height=144 align="center" cellPadding=3 cellSpacing=0>
      <thead>
        <TR>
          <TD height=22 colSpan=2 align="center"><lt:Label res="res.label.forum.showtopic" key="topic_del"/></TD>
        </TR>
	  </thead>
      <TBODY>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="topic"/></TD>
          <TD height=22 align="left">
        <%
		long delid = ParamUtil.getLong(request, "delid");
		MsgDb md = new MsgDb();
		md = md.getMsgDb(delid);
		if (!md.isLoaded()) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "res.forum.MsgDb", "err_topic_del_lost")));
			return;			
		}
		%>
        <a href="../showtopic.jsp?rootid=<%=md.getRootid()%>"><%=StrUtil.toHtml(md.getTitle())%></a></TD>
        </TR>
        <TR align="center">
          <TD height=22 align="left"><lt:Label res="res.label.forum.manager" key="reason"/></TD>
          <TD height=22 align="left">
		  <select name="delReason" onChange="form1.reason.value+=this.value">
		  <option value=""><lt:Label key="wu"/></option>
            <%
			BasicDataMgr bdm = new BasicDataMgr();
			%>
			<%=bdm.getOptionsStr("delReason", false)%>
          </select>
          </TD>
        </TR>
        <TR align="center">
          <TD width="16%" height=22 align="left"><lt:Label res="res.label.forum.manager" key="desc"/></TD>
          <TD width="84%" height=22 align="left"><textarea name="reason" cols="40" rows="6" ></textarea>
          <input type=hidden name=boardcode value="<%=ParamUtil.get(request, "boardcode")%>">
          <input type=hidden name=delid value="<%=delid%>">
          <input type=hidden name=privurl value="<%=privurl%>"></TD>
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
