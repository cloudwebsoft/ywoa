<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="java.lang.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<%
String dir_code = ParamUtil.get(request, "dir_code");
if (dir_code.equals(""))
	dir_code = Leaf.CODE_WIKI;
Directory dir = new Directory();
Leaf leaf = dir.getLeaf(dir_code);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>发布文章统计</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../../../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../../../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../../../../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
LeafPriv lp = new LeafPriv(Leaf.CODE_WIKI);
if (!lp.canUserExamine(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="wiki_rank_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%
String sDate = ParamUtil.get(request, "date");
if (!sDate.equals(""))
	sDate += "-01";
java.util.Date date = DateUtil.parse(sDate, "yyyy-MM-dd");
if (date==null)
	date = new java.util.Date();
	
int row = ParamUtil.getInt(request, "row", 20);
%>
<form name="form1" action="wiki_doc_edit_rank.jsp" method="get">
<table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td>选择日期：
    <input type="text" id="date" name="date" size="10" value="<%=DateUtil.format(date, "yyyy-MM")%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "date", 
        ifFormat       :    "%Y-%m",
        showsTime      :    false,
        singleClick    :    false,
        align          :    "Tl",
        step           :    1
    });
</script>
显示前<input name="row" value="<%=row%>" size="3">
<input name="dir_code" value="<%=dir_code%>" type="hidden">
条&nbsp;<input class="btn" value="确定" type="submit"></td>
  </tr>
</table>
</form>
<br>
<table class="tabStyle_1 percent98"cellSpacing="0" cellPadding="3" width="95%" align="center">
  <thead>
    <tr>
      <td noWrap width="63%">文章</td>
      <td noWrap width="13%">作者</td>
      <td width="11%" noWrap>编辑次数</td>
      <td width="13%" noWrap>创建日期</td>
    </tr>
  </thead>
  <tbody>
<%
WikiStatistic st = new WikiStatistic();
int[][] ary = st.getRankDocEditCountMonth(date, row);

DocumentMgr dm = new DocumentMgr();
Document doc = null;
for (int i = 0; i < ary.length; i++) {
	int docId = ary[i][0];
	if (docId == 0) // 行数不足row
		break;
	doc = dm.getDocument(docId);
	if (doc==null)
		continue;
	int count = ary[i][1];
%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td>&nbsp;<a href="<%=request.getContextPath()%>/wiki_show.jsp?id=<%=docId%>" target="_blank"><%=doc.getTitle()%></a></td>
      <td align="center"><%=doc.getNick()%></td>
      <td align="center"><%=ary[i][1]%></td>
      <td align="center"><%=DateUtil.format(doc.getCreateDate(), "yyyy-MM-dd")%></td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
</html>