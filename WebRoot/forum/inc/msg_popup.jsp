<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="ltpop" %>
<script type="text/javascript" src="<%=request.getContextPath()%>/forum/inc/msg_popup.js"></script>
<%
int msgCnt = ((Integer)request.getAttribute("msgCount")).intValue();
String msgStr = cn.js.fan.web.SkinUtil.LoadString(request, "res.label.forum.listtopic", "new_short_msg");
msgStr = cn.js.fan.util.StrUtil.format(msgStr, new Object[] {(Integer)request.getAttribute("msgCount")});

com.redmoon.forum.Config cfgPop = com.redmoon.forum.Config.getInstance();
%>
<div id="winpop">
<div class="title">短消息<span class="close" onClick="msgPopup()">×</span></div>
<div class="con">
<%if (cfgPop.getBooleanProperty("forum.isNewMsgPlaySound")) {%>
<object NAME='player' classid=clsid:22d6f312-b0f6-11d0-94ab-0080c74c7e95 width=350 height=70 style="display:none">
<param name=showstatusbar value=1>
<param name=filename value='<%=request.getContextPath()%>/message/msg.wav'>
<param name="AUTOSTART" value="true" />
<embed src='<%=request.getContextPath()%>/message/msg.wav'>
</embed></object>
<%}%>
<A href='#' onclick='window.open("../message/message.jsp","","toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width=320,height=260,resizable=yes");msgPopup()' title='<ltpop:Label res="res.label.forum.inc.header" key="view_msg"/>'><%=msgStr%></A>
</div>
</div>