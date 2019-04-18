<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "RegDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
String op = ParamUtil.get(request, "op");
String nick = ParamUtil.get(request, "nick");
int ageBegin = ParamUtil.getInt(request, "ageBegin", -1);
int ageEnd = ParamUtil.getInt(request, "ageEnd", -1);
String gender = ParamUtil.get(request, "gender");
String career = ParamUtil.get(request, "career");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="forum/<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<title><lt:Label res="res.label.listmember" key="list_member"/> - <%=Global.AppName%></title>
<script src="inc/common.js"></script>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
	window.location.href = "listmember.jsp?op=<%=op%>&nick=<%=StrUtil.UrlEncode(nick)%>&gender=<%=gender%>&ageBegin=<%=ageBegin%>&ageEnd=<%=ageEnd%>&career=<%=career%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">  
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.canUserDo(request, "", "view_listmember")) {
	response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
	<form name="formSearch" action="listmember.jsp" method="post">	
    <table width="98%" border="0" cellpadding="3" cellspacing="1" class="tableCommon">
      <tr>
        <td align="center"><lt:Label res="res.label.forum.user" key="RegName"/>
          <input name="nick" value="<%=StrUtil.toHtml(nick)%>" size="10">
          <input name="op" value="search" type="hidden">
		  <lt:Label res="res.label.forum.user" key="Career"/>
		  <select name=career size=1>
            <option value="" selected>
              <lt:Label res="res.label.forum.user" key="select"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="government"/>">
              <lt:Label res="res.label.forum.user" key="government"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="student"/>">
              <lt:Label res="res.label.forum.user" key="student"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="communication"/>">
              <lt:Label res="res.label.forum.user" key="communication"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="computer"/>">
              <lt:Label res="res.label.forum.user" key="computer"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="web"/>">
              <lt:Label res="res.label.forum.user" key="web"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="trade"/>">
              <lt:Label res="res.label.forum.user" key="trade"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="bank"/>">
              <lt:Label res="res.label.forum.user" key="bank"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="tax"/>">
              <lt:Label res="res.label.forum.user" key="tax"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="refer"/>">
              <lt:Label res="res.label.forum.user" key="refer"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="service"/>">
              <lt:Label res="res.label.forum.user" key="service"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="tour"/>">
              <lt:Label res="res.label.forum.user" key="tour"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="health"/>">
              <lt:Label res="res.label.forum.user" key="health"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="realty"/>">
              <lt:Label res="res.label.forum.user" key="realty"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="transport"/>">
              <lt:Label res="res.label.forum.user" key="transport"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="law"/>">
              <lt:Label res="res.label.forum.user" key="law"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="culture"/>">
              <lt:Label res="res.label.forum.user" key="culture"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="ad"/>">
              <lt:Label res="res.label.forum.user" key="ad"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="education"/>">
              <lt:Label res="res.label.forum.user" key="education"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="agriculture"/>">
              <lt:Label res="res.label.forum.user" key="agriculture"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="manufacturing"/>">
              <lt:Label res="res.label.forum.user" key="manufacturing"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="soho"/>">
              <lt:Label res="res.label.forum.user" key="soho"/>
              </option>
            <option value="<lt:Label res="res.label.forum.user" key="other"/>">
              <lt:Label res="res.label.forum.user" key="other"/>
              </option>
            </select>
          &nbsp;&nbsp;          
          <lt:Label res="res.label.listmember" key="age_scope"/>
            <select name="ageBegin">
            <option value="-1">无</option>
            <option value="15">15</option>
            <option value="20">20</option>
            <option value="25" selected>25</option>
            <option value="30">30</option>
            <option value="35">35</option>
            <option value="40">40</option>
            <option value="45">45</option>
            <option value="50">50</option>
            <option value="55">55</option>
            <option value="60">60</option>
            <option value="65">65</option>
            <option value="70">70</option>
            </select>
至
<select name="ageEnd">
  <option value="-1">无</option>
  <option value="15">15</option>
  <option value="20">20</option>
  <option value="25">25</option>
  <option value="30" selected>30</option>
  <option value="35">35</option>
  <option value="40">40</option>
  <option value="45">45</option>
  <option value="50">50</option>
  <option value="55">55</option>
  <option value="60">60</option>
  <option value="65">65</option>
  <option value="70">70</option>
</select>
&nbsp;&nbsp;
<lt:Label res="res.label.forum.user" key="Gender"/>
<input type="radio" name="gender" value="M">
          <lt:Label res="res.label.listmember" key="man"/>
          <input type="radio" name="gender" value="F" checked>
          <lt:Label res="res.label.listmember" key="woman"/>
          <input type="radio" name="gender" value="">
          <lt:Label res="res.label.listmember" key="bu_xian"/>
          <script>
		 formSearch.career.value = "<%=career%>";
		formSearch.ageBegin.value = "<%=ageBegin%>";
		formSearch.ageEnd.value = "<%=ageEnd%>";
		setRadioValue("gender", "<%=gender%>");
		  </script>          <input name="Submit" type="submit" class="singleboarder" value="<lt:Label res="res.label.forum.admin.user_m" key="search"/>">
		  &nbsp;</td>
        </tr>
    </table>
  	</form><br />
  <%
		String sql = "select name from sq_user where isValid=1 ORDER BY " + orderBy + " " + sort;
		
		if (op.equals("search")) {
			sql = "select name from sq_user where isValid=1";
			
			if (!nick.equals(""))
				sql += " and nick like " + StrUtil.sqlstr("%" + nick + "%");

			String cond = "";
			if (!career.equals("")) {
				cond = " and career=" + StrUtil.sqlstr(career);
			}
			if (ageBegin!=-1) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, -ageBegin);
				cond += " and Birthday<=" + DateUtil.toLongString(cal.getTime());
			}
			if (ageEnd!=-1) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, -ageEnd);
				cond += " and Birthday>=" + DateUtil.toLongString(cal.getTime());
			}
			if (!gender.equals("")) {
				cond += " and Gender=" + StrUtil.sqlstr(gender);
			}
			if (!cond.equals(""))
				sql += " " + cond;	
				
			sql += " ORDER BY " + orderBy + " " + sort;		
		}
		
		int pagesize = 10;
		ResultRecord rr = null;
		Paginator paginator = new Paginator(request);
		
		UserDb user = new UserDb();
		
		long total = user.getObjectCount(sql);
		paginator.init(total, pagesize);
		int totalpages = paginator.getTotalPages();
		int curpage = paginator.getCurPage();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}			
		ObjectBlockIterator oir = user.getObjects(sql, (curpage-1)*pagesize, curpage*pagesize);
%>
  <table width="98%" border="0" align="center" class="p9">
    <tr>
      <td width="36%" align="left">&nbsp;</td>
      <td width="64%" align="right"><lt:Label res="res.label.listmember" key="count"/>
        <b><%=paginator.getTotal() %></b>
        <lt:Label res="res.label.listmember" key="per_page"/>
        <b><%=paginator.getPageSize() %></b>
        <lt:Label res="res.label.listmember" key="page"/>
        <b><%=curpage %>/<%=totalpages %></b></td>
    </tr>
  </table>    
  <TABLE class="tableCommon" width="98%" border=0 align=center cellPadding=0 cellSpacing=0>
  <thead>
      <TR align=center> 
        <TD width=23% height=23><strong><lt:Label res="res.label.listmember" key="user_name"/></strong></TD>
        <TD width=6% height=23><strong>
        <lt:Label res="res.label.listmember" key="sex"/></strong></TD>
        <TD width=15% height=23 onClick="doSort('lastTime')" style="cursor:hand"><strong><lt:Label res="res.label.listmember" key="lastTime"/>
            <span style="cursor:hand">
            <%if (orderBy.equals("lastTime")) {
			if (sort.equals("asc")) 
				out.print("<img src='forum/admin/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='forum/admin/images/arrow_down.gif' width=8px height=7px>");
		}%>
        </span></strong></TD>
        <TD width=15%><strong>
        <lt:Label res="res.label.listmember" key="level"/></strong></TD>
        <TD width=26%><lt:Label res="res.label.forum.user" key="home"/></TD>
        <TD width=15% height=23 onClick="doSort('RegDate')" style="cursor:hand"><strong>
          <lt:Label res="res.label.listmember" key="regist_date"/></strong>
		<%if (orderBy.equals("RegDate")) {
			if (sort.equals("asc")) 
				out.print("<img src='forum/admin/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='forum/admin/images/arrow_down.gif' width=8px height=7px>");
		}%></TD>
      </TR></thead>
    <TBODY>	  
      <%		
String id="",name="",RegDate="",Gender="",OICQ="",State="",myface="";
int layer = 1;
int i = 0;
String RealPic = "";
while (oir.hasNext()) {
 	    user = (UserDb)oir.next(); 
	    i++;
		name = user.getName();
		RegDate = DateUtil.format(user.getRegDate(), "yyyy-MM-dd HH:mm");
		Gender = StrUtil.getNullString(user.getGender());
		if (Gender.equals("M"))
			Gender = SkinUtil.LoadString(request,"res.label.listmember","man");
		else if (Gender.equals("F"))
			Gender = SkinUtil.LoadString(request,"res.label.listmember","woman");
		else
			Gender = SkinUtil.LoadString(request,"res.label.listmember","bu_xian");
		
		OICQ = StrUtil.getNullString(user.getOicq());
		State = StrUtil.getNullString(user.getState());
		if (State.equals("0"))
			State = SkinUtil.LoadString(request,"res.label.listmember","bu_xian");
		RealPic = StrUtil.getNullString(user.getRealPic());
		myface = StrUtil.getNullString(user.getMyface());
%>
      <TR align=center> 
        <TD height=23 align="left"> &nbsp;
		<%if (myface.equals("")) {%>
		  <img src="forum/images/face/<%=RealPic%>" width=16 height=16> 
		<%}else{%>
		  <img src="<%=user.getMyfaceUrl(request)%>" width=16 height=16>
		<%}%>
        <a href="userinfo.jsp?username=<%=StrUtil.UrlEncode(StrUtil.toHtml(name),"utf-8")%>"><%=user.getNick()%></a></TD>
        <TD width=6% height=23><%=Gender%></TD>
        <TD width=15% height=23 align="left">&nbsp;<%=com.redmoon.forum.ForumSkin.formatDateTime(request, user.getLastTime())%></TD>
        <TD width=15%><%=user.getLevelDesc()%></TD>
        <TD width=26%><a href="<%=user.getHome()%>" target="_blank"><%=user.getHome()%></a></TD>
        <TD width=15% height=23><%=RegDate%></TD>
      </TR>
<%}%>
    </TBODY>
  </TABLE>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
    <tr> 
      <td height="23" align="right">
    <%
	  String querystr = "op=" + op + "&nick=" + StrUtil.UrlEncode(nick) + "&orderBy=" + orderBy + "&sort=" + sort;
	  querystr += "&gender=" + gender + "&career=" + StrUtil.UrlEncode(career) + "&ageBegin=" + ageBegin + "&ageEnd=" + ageEnd;
 	  out.print(paginator.getCurPageBlock("listmember.jsp?"+querystr));
	%></td>
    </tr>
  </table>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
</html>
