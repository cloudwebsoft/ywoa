<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="prision" scope="page" class="com.redmoon.forum.life.prision.Prision"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	if (!prision.isPolice(privilege.getUser(request))) {
		out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request,"res.label.prision","no_right_login")));
		return;
	}
}

String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.prision" key="vist_prisionor"/> - <%=Global.AppName%></title>
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fdate" scope="page" class="cn.js.fan.util.DateUtil"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
int baildlt = prision.getBailDlt();
if (op.equals("arrest")) {
	boolean re = true;
	String username = ParamUtil.get(request, "username").trim();
	String police = privilege.getUser(request);
	String arrestreason = ParamUtil.get(request, "arrestreason").trim();
	String arrestday = ParamUtil.get(request, "arrestday").trim();
	boolean isvalid = true;
	if (username.equals("") || arrestreason.equals("") || arrestday.equals("")) {
		out.println(StrUtil.Alert(SkinUtil.LoadString(request,"res.label.prision","can_not_null")));
		isvalid = false;
	}
	UserDb ud = new UserDb();
	ud = ud.getUserDbByNick(username);
	if (ud==null) {
		out.println(StrUtil.Alert(SkinUtil.LoadString(request,"res.forum.life.prision.Prision","none_user")));
		isvalid = false;
	}
	
	if (isvalid && !StrUtil.isNumeric(arrestday)) {
		out.println(StrUtil.Alert(SkinUtil.LoadString(request,"res.label.prision","day_must_be_number")));
		isvalid = false;
	}
	if (isvalid) {
		try {
			int arday = Integer.parseInt(arrestday);
			if (arday==0)
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"res.label.prision","day_can_not_be_0")));
			else
				isvalid = prision.arrest(police,ud.getName(),arrestreason,arday);
			if (isvalid)
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_success")));
			else
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_fail")));	
		}
		catch (ResKeyException e) {
			out.print(StrUtil.Alert(e.getMessage(request)));
		}
	}
}
if (op.equals("release")) {
	boolean re = true;
	String username = ParamUtil.get(request, "username").trim();
	boolean isvalid = true;
	if (username.equals("")) {
		out.println(StrUtil.Alert(SkinUtil.LoadString(request,"res.label.prision","user_name_can_not_be_null")));
		isvalid = false;
	}
	if (isvalid) {
		try {
			isvalid = prision.release(username);
			if (isvalid)
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_success")));
			else
				out.println(StrUtil.Alert(SkinUtil.LoadString(request,"info_operate_fail")));	
		}
		catch (ResKeyException e) {
			out.print(StrUtil.Alert(e.getMessage(request)));
		}
	}
}
%>
<form action="?op=arrest" method=post>
<table height="28"  border="0" align="center" cellpadding="0" cellspacing="0" class="tableCommon">
  <tr align="center">
    <td width="6%"><lt:Label res="res.label.prision" key="prisionor"/></td>
    <td width="11%"><input name="username" type="text" size=10></td>
    <td width="47%"><lt:Label res="res.label.prision" key="arrest_reason"/>
      <input name="arrestreason" type="text" size=30></td>
    <td width="23%"><lt:Label res="res.label.prision" key="arrest_days"/>
      <input name="arrestday" type="text" size=4></td>
  <td width="13%"><input type="submit" name="Submit" value="<%=SkinUtil.LoadString(request,"res.label.prision","arrest")%>"></td>
  </tr>
</table></form>
<br>
<form id=formsearch name=formsearch action="?op=search" method=post>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableCommon">
        <tr>
        <td height="24" align="center"><lt:Label res="res.label.prision" key="search_user"/> 
          <input name="username" type="text">
          &nbsp; 
          <input name="Submit" type="submit" value="<%=SkinUtil.LoadString(request,"res.label.prision","user_name_search")%>">
          </td>
        </tr>
  </table></form>
  <br />
  <div class="tableTitle"><lt:Label res="res.label.prision" key="list_prisionor"/></div>
<%
		String sql = "select name from sq_user where arrestday>0 and releasetime > " + System.currentTimeMillis() + " ORDER BY RegDate";
		if (op.equals("search")) {
			String username = ParamUtil.get(request, "username");
			sql = "select name from sq_user where arrestday>0 and releasetime > " + System.currentTimeMillis() + " and nick like "+StrUtil.sqlstr("%"+username+"%")+" ORDER BY RegDate";
		}
		int credit = 0;

		int pagesize = 10;
		ResultRecord rr = null;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
		ResultIterator ri = pageconn.getResultIterator(sql);
		paginator.init(pageconn.getTotal(), pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
  <table width="100%" border="0" class="per100">
    <tr>
      <td align="right"><lt:Label res="res.label.prision" key="count"/> <b><%=paginator.getTotal() %></b> <lt:Label res="res.label.prision" key="per_page"/> <b><%=paginator.getPageSize() %></b> 
        <lt:Label res="res.label.prision" key="page"/> <b><%=curpage %>/<%=totalpages %></b></td>
    </tr>
  </table>    
  <TABLE width="98%" align=center cellPadding=0 cellSpacing=1 class="tableCommon">
      <thead> 
        <TD width=18% height=23><lt:Label res="res.label.prision" key="user_name"/></TD>
        <TD width=10%><lt:Label res="res.label.prision" key="arrested_time"/></TD>
        <TD width=31%><lt:Label res="res.label.prision" key="arrested_reason"/></TD>
        <TD width=9%><lt:Label res="res.label.prision" key="arrested_days"/></TD>
        <TD width=10%><lt:Label res="res.label.prision" key="arrester"/></TD>
        <TD width=13%><lt:Label res="res.label.prision" key="bail_money"/></TD>
        <TD width=9%><%=SkinUtil.LoadString(request,"op")%></TD>
      </thead>
    <TBODY>
      <%		
String id="",name="",RegDate="",Gender="",OICQ="",State="",myface="",arrestreason="",arrestpolice="";
Date arresttime = null;
int layer = 1,ispolice=0,arrestday=0;
int i = 0;
String RealPic = "";
UserDb user = new UserDb();

while (ri.hasNext()) {
 	    rr = (ResultRecord)ri.next(); 
	    i++;
		name = rr.getString("name");
		user = user.getUser(name);
		
		RegDate = DateUtil.format(user.getRegDate(), "yyyy-MM-dd");
		Gender = StrUtil.getNullString(user.getGender());
		if (Gender.equals("M"))
			Gender = SkinUtil.LoadString(request,"res.label.prision","man");
		else if (Gender.equals("F"))
			Gender = SkinUtil.LoadString(request,"res.label.prision","woman");
		else
			Gender = SkinUtil.LoadString(request,"res.label.prision","not_in_detail");
		
		
		OICQ = StrUtil.getNullString(user.getOicq());
		State = StrUtil.getNullString(user.getState());
		if (State.equals("0"))
			State = SkinUtil.LoadString(request,"res.label.prision","not_in_detail");
		RealPic = StrUtil.getNullString(user.getRealPic());
		myface = StrUtil.getNullString(user.getMyface());
		ispolice = user.getIsPolice();
		arrestday = user.getArrestDay();
		arrestreason = StrUtil.getNullString(user.getArrestReason());
		arresttime = user.getArrestTime();
		arrestpolice = StrUtil.getNullString(user.getArrestPolice());
		credit = user.getCredit();
%>
      <TR align=center bgColor=#f8f8f8> 
        <TD width=18% height=23 align="left"> &nbsp; <%if (myface.equals("")) {%> <img src="forum/images/face/<%=RealPic%>" width=16 height=16> 
          <%}else{%> <img src="<%=user.getMyfaceUrl(request)%>" width=16 height=16> 
          <%}%> <a href="userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=user.getNick()%></a>
		  <input type=hidden name=username value="<%=name%>">
		  <input type=hidden name=CPages value="<%=curpage%>">
	    </TD>
        <TD width=10%><%
		String artime = "";
		if (arresttime==null)
			artime = "";
		else
			artime = StrUtil.FormatDate(arresttime,"yyyy-MM-dd");
		%>
        <%=artime%></TD>
        <TD width=31%><%=arrestreason%></TD>
        <TD width=9%><%=arrestday%></TD>
      <TD width=10%><a href="userinfo.jsp?username=<%=arrestpolice%>"><%=user.getUser(arrestpolice).getNick()%></a>        </TD>
        <TD width=13%>
		<%
		int bailfee = baildlt*arrestday;
		%>
		<%=bailfee%>
		</TD>
        <TD width=9%>
	  <a href="prision_arrest.jsp?op=release&username=<%=StrUtil.UrlEncode(name)%>&CPages=<%=curpage%>"><lt:Label res="res.label.prision" key="to_libreate"/></a>      </TD>
      </TR>
<%}%>
    </TBODY>
  </TABLE>
	
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="per100">
    <tr> 
      <td width="2%" height="23">&nbsp;</td>
      <td width="76%" valign="baseline" height="23"> <div align="right"> 
          <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("prision_arrest.jsp?"+querystr));
	  %>
	</div></td>
      <td width="22%" height="23"> 
	  </td>
    </tr>
  </table>

</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
</html>
