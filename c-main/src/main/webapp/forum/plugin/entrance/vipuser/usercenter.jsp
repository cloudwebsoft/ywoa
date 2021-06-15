<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.entrance.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String rootPath = request.getContextPath();
String userName = privilege.getUser(request);

VIPUserDb vu = new VIPUserDb();
vu = vu.getVIPUserDb(userName);
if (vu.isLoaded() && vu.isValid()) {
%>
<table width="100%" border="0" cellpadding="5" cellspacing="1">
  <tr>
    <td colspan="2" align="left" valign="top">
	<strong>VIP通行证</strong>　
<%
		  String boards = vu.getBoards();
		  String boardNames = "";
		  if (boards!=null && !boards.equals("")) {
		  	String[] boardary = boards.split(",");
			int len = boardary.length;
			Directory dir = new Directory();
			Leaf lf = null;
			for (int i=0; i<len; i++) {
				lf = dir.getLeaf(boardary[i]);
				// System.out.println("VIPCard_edit.jsp " + kindAry[i]);
				if (lf!=null) {
					if (boardNames.equals(""))
						boardNames = lf.getName();
					else
						boardNames += "," + lf.getName();
				}
			}
		  }
%></td>
  </tr>
  <tr>
    <td colspan="2" align="left" valign="top">能通行的版块：<%=boardNames%> </td>
  </tr>
  <tr>
    <td colspan="2" align="left" valign="top">有效期开始：<%=ForumSkin.formatDate(request, vu.getBeginDate())%> </td>
  </tr>
  <tr>
    <td colspan="2" align="left" valign="top">有效期结束：<%=ForumSkin.formatDate(request, vu.getEndDate())%> </td>
  </tr>
</table>
<%}%>
