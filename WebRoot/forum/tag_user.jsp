<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<title><lt:Label res="res.label.forum.showtopic" key="tag"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<%@ include file="../inc/nocache.jsp"%>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<SCRIPT>
// 展开帖子
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2=eval("document.all.followImg" + t_id);
	var targetTR2=eval("document.all.follow" + t_id);
	if (targetImg2.src.indexOf("nofollow")!=-1){return false;}
	if ("object"==typeof(targetImg2)){
		if (targetTR2.style.display!="")
		{
			targetTR2.style.display="";
			targetImg2.src="images/minus.gif";
			if (targetImg2.loaded=="no"){
				document.frames["hiddenframe"].location.replace("listtree.jsp?id="+b_id+getstr);
			}
		}else{
			targetTR2.style.display="none";
			targetImg2.src="images/plus.gif";
		}
	}
}
</SCRIPT>
<BODY>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.canUserDo(request, "", "search")) {
	response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
TagDb td = new TagDb();

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	TagMsgDb tmd = new TagMsgDb();
	long tagId = ParamUtil.getLong(request, "tagId");
	tmd.delTagOfUser(tagId, userName);
	out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "tag_user.jsp"));
	return;	
}
%>
  <TABLE height=25 cellSpacing=0 cellPadding=1 width="98%" align=center border=1 class="tableCommon">
  <TBODY>
  <TR>
  <TD><img src="images/userinfo.gif" width="9" height="9">&nbsp;
    <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
    <a href="<%=request.getContextPath()%>/forum/index.jsp">
    <lt:Label res="res.label.forum.inc.position" key="forum_home"/>
    </a>&nbsp;<b>&raquo;</b>&nbsp;<a href="<%=request.getContextPath()%>/usercenter.jsp">
    <lt:Label res="res.label.forum.menu" key="user_center"/>&nbsp;<b>&raquo;</b>&nbsp;
    </a><a href="<%=request.getContextPath()%>/forum/tag.jsp">
    <lt:Label res="res.label.forum.showtopic" key="tag"/></a>
   </TD>
    </TR></TBODY></TABLE>
  <BR>
  <table width="98%" cellspacing="1" cellpadding="3" align="center" class="tableCommon">
  	<thead>
    <tr> 
      <td height="23">
        <lt:Label res="res.label.forum.showtopic" key="tag"/>
      </td>
    </tr>
	</thead>
    <tr>
      <td height="23">
	  <%
	  Vector v = td.getTagsOfUser(userName);
	  Iterator ir = v.iterator();
	  while (ir.hasNext()) {
	  	td = (TagDb)ir.next();
		%>
		<a class="linkTag" href="listtag.jsp?tagId=<%=td.getLong("id")%>" target="_blank"><%=td.getString("name")%></a>(<a href="<%=request.getContextPath()%>/forum/tag.jsp">
		</a><a href="javascript:if (confirm('<%=SkinUtil.LoadString(request, "confirm_del")%>')) window.location.href='tag_user.jsp?op=del&tagId=<%=td.getLong("id")%>'"><lt:Label key="op_del"/></a>)&nbsp;&nbsp;
	  <%}
	  %>
	  </td>
    </tr>
</table>            
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</BODY></HTML>
