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
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<title><lt:Label res="res.label.forum.showtopic" key="tag"/> - <%=Global.AppName%></title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<SCRIPT>
// 展开帖子
function loadThreadFollow(b_id,t_id,getstr){
	var targetImg2 =eval("document.all.followImg" + t_id);
	var targetTR2 =eval("document.all.follow" + t_id);
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
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%@ include file="inc/position.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
TagDb td = new TagDb();
com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
%>
<BR>
  <table width="98%" border="1" cellspacing="1" cellpadding="3" align="center" class="tableCommon">
  	<thead>
    <tr> 
      <td height="23">
        <lt:Label res="res.label.forum.showtopic" key="tag"/>
      </td>
    </tr>
	</thead>
    <tr>
      <td height="23" style="line-height:180%">
	  <%
	  boolean tagOnlySystemAllowed = cfg1.getBooleanProperty("forum.tagOnlySystemAllowed");
	  String sqlListTag;
	  if (tagOnlySystemAllowed) {
		sqlListTag = td.getTable().getSql("listSystemTag");
	  }
	  else {
		sqlListTag = td.getTable().getSql("listTag");
	  }
	  
	  Vector v = td.list(sqlListTag);
	  Iterator ir = v.iterator();
	  while (ir.hasNext()) {
	  	td = (TagDb)ir.next();
		%>
		<a class="linkTag" href="listtag.jsp?tagId=<%=td.getLong("id")%>"><%=TagMgr.render(request, td)%><font color="disabled">(<%=td.getInt("count")%>)</font></a>&nbsp;&nbsp;
	  <%}
	  %>
	  </td>
    </tr>
</table>            
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</BODY></HTML>
