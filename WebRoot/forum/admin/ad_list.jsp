<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ad.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>AD List</title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head"><lt:Label res="res.label.forum.admin.ad_list" key="ad_mgr"/></td>
    </tr>
  </tbody>
</table>
<%
AdDb ad = new AdDb();

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	ad = (AdDb)ad.getQObjectDb(new Integer(id));
	boolean re = ad.del();
	if (re)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "ad_list.jsp"));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));	
}

String sql = ad.getTable().getQueryList();

int total = (int)ad.getQObjectCount(sql);

int pagesize = total; 	// 20;

int curpage,totalpages;
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
totalpages = paginator.getTotalPages();
curpage	= paginator.getCurrentPage();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}	

QObjectBlockIterator oir = ad.getQObjects(sql, (curpage-1)*pagesize, curpage*pagesize);

String[] types = new String[] {SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "top_banner"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "footer_banner"),  SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "inner_words"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "float_ad"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "topic_footer"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "door_ad"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "topic_inner_ad"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "door_ad_right"), SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "topic_outer_ad")};
%>
<table width="98%" align="center">
  <tr>
    <td align="center" height="5"></td>
  </tr>
  <tr>
    <td align="center">
<%
int typesLen = types.length;
for (int k=0; k<typesLen; k++) {
%>
        <INPUT name="button" type="button" 
onclick="javascript:location.href='ad_add.jsp?ad_type=<%=k%>';" value="<%=types[k]%>">
      &nbsp;
      <%}%>    </td>
  </tr>
</table>
<br>
<table style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing="1" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="thead" noWrap width="18%"><lt:Label res="res.label.forum.admin.ad_list" key="name"/></td>
      <td class="thead" noWrap width="19%"><img src="images/tl.gif" align="absMiddle" width="10" height="15"><lt:Label res="res.label.forum.admin.ad_list" key="sort"/></td>
      <td class="thead" noWrap width="24%"><img src="images/tl.gif" align="absMiddle" width="10" height="15"><lt:Label res="res.label.forum.admin.ad_list" key="board"/></td>
      <td class="thead" noWrap width="14%"><img src="images/tl.gif" align="absMiddle" width="10" height="15"><lt:Label res="res.label.forum.admin.ad_list" key="begin_date"/></td>
      <td class="thead" noWrap width="13%"><img src="images/tl.gif" align="absMiddle" width="10" height="15"><lt:Label res="res.label.forum.admin.ad_list" key="end_date"/></td>
      <td width="12%" noWrap class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15"><lt:Label res="res.label.forum.admin.ad_list" key="oper"/></td>
    </tr>
<%
Directory dir = new Directory();
while (oir.hasNext()) {
 	ad = (AdDb)oir.next();
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td><%=ad.getString("title")%></td>
      <td><%=types[ad.getInt("ad_type")]%></td>
      <td>
	  <%
	  String[] boards = StrUtil.split(ad.getString("boardcodes"), ",");
	  String boardNames = "";
	  if (boards!=null) {
	  	int len = boards.length;
		for (int i=0; i<len; i++) {
	  		Leaf lf = dir.getLeaf(boards[i]);
			if (lf!=null) {
				if (boardNames.equals(""))
					boardNames = lf.getName();
				else
					boardNames += "," + lf.getName();
			}
		}
	  }
	  else {
	  	boardNames = SkinUtil.LoadString(request, "res.label.forum.admin.ad_list", "forum_index");;
	  }
	  out.print(boardNames);
	  %>
	  </td>
      <td><%=DateUtil.format(ad.getDate("begin_date"), "yyyy-MM-dd")%></td>
      <td><%=DateUtil.format(ad.getDate("end_date"), "yyyy-MM-dd")%></td>
      <td>
	  [<a href="ad_edit.jsp?id=<%=ad.getInt("id")%>"><lt:Label res="res.label.cms.dir" key="modify"/></a>]&nbsp;[<a href="ad_list.jsp?op=del&id=<%=ad.getInt("id")%>"><lt:Label res="res.label.cms.dir" key="del"/></a>]&nbsp;</td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
</html>