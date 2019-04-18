<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.miniplugin.index.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt"%>
<table width="98%" border="1" align="center" cellpadding="4" cellspacing="0">
    <TBODY>
      <tr> 
      <td width="33%" align="center" class="td_title">
        <lt:Label res="res.label.forum.miniplugin.newelitetop" key="newtopic"/>
      </td>
      <td width="33%" align="center" class="td_title">
        <lt:Label res="res.label.forum.miniplugin.newelitetop" key="elitetopic"/>
      </td>
      <td width="34%" align="center" class="td_title">
        <lt:Label res="res.label.forum.miniplugin.newelitetop" key="toptopic"/>
      </td>
      </tr>
    </TBODY>
    <TBODY>
      <tr>
        <td>
		<%
		int n = 5;
		NewEliteTop net = new NewEliteTop();
		java.util.Iterator newir = net.listNewMsg(5).iterator();
		while (newir.hasNext()) {
			com.redmoon.forum.MsgDb msg = (com.redmoon.forum.MsgDb)newir.next();
		%>
			<table width="100%" border="0" cellspacing="0">
		  <tr><td><a href="showtopic.jsp?rootid=<%=msg.getId()%>"><%=cn.js.fan.util.StrUtil.getLeft(msg.getTitle(), 20)%></a></td>
		  </tr></table>
		<%}
		%>
		</td>
        <td><%
		n = 5;
		newir = net.listEliteMsg(5).iterator();
		while (newir.hasNext()) {
			com.redmoon.forum.MsgDb msg = (com.redmoon.forum.MsgDb)newir.next();
		%>
          <table width="100%" border="0" cellspacing="0">
            <tr>
              <td><a href="showtopic.jsp?rootid=<%=msg.getId()%>"><%=cn.js.fan.util.StrUtil.getLeft(msg.getTitle(), 20)%></a></td>
            </tr>
          </table>
        <%}
		%></td>
        <td><%
		n = 5;
		newir = net.listTopMsg(5).iterator();
		while (newir.hasNext()) {
			com.redmoon.forum.MsgDb msg = (com.redmoon.forum.MsgDb)newir.next();
		%>
          <table width="100%" border="0" cellspacing="0">
            <tr>
              <td><a href="showtopic.jsp?rootid=<%=msg.getId()%>"><%=cn.js.fan.util.StrUtil.getLeft(msg.getTitle(), 20)%></a></td>
            </tr>
          </table>
        <%}
		%></td>
      </tr>
    </TBODY>
</table>
