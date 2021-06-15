<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="cn.js.fan.db.Conn"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "RegDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String op = StrUtil.getNullString(request.getParameter("op"));	
String username = ParamUtil.get(request, "username");

com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "username", username, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String type = ParamUtil.get(request, "type");
String what = ParamUtil.get(request, "what");
String groupCode = ParamUtil.get(request, "groupCode");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="GENERATOR" content="Microsoft FrontPage 4.0">
<meta name="ProgId" content="FrontPage.Editor.Document">
<LINK href="../common.css" type=text/css rel=stylesheet>
<LINK href="default.css" type=text/css rel=stylesheet>
<title><lt:Label res="res.label.forum.admin.user_m" key="user_manage"/></title>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "user_m.jsp?op=<%=op%>&username=<%=StrUtil.UrlEncode(username)%>&type=<%=type%>&what=<%=StrUtil.UrlEncode(what)%>&groupCode=<%=StrUtil.UrlEncode(groupCode)%>&orderBy=" + orderBy + "&sort=" + sort;
}

function selPerson(userNames) {
	window.location.href = "user_m.jsp?op=addGroupUser&type=userName&groupCode=<%=StrUtil.UrlEncode(groupCode)%>&userNames=" + userNames;
}
</script>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fdate" scope="page" class="cn.js.fan.util.DateUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="prision" scope="page" class="com.redmoon.forum.life.prision.Prision"/>
<div id="newdiv" name="newdiv">
  <table width='100%' cellpadding='0' cellspacing='0' >
    <tr>
      <td class="head"><lt:Label res="res.label.forum.admin.user_m" key="user_manage"/></td>
    </tr>
  </table><br>
<%
//安全验证
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String privurl = request.getRequestURL()+"?"+request.getQueryString();
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "privurl", privurl, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (op.equals("addGroupUser")) {
	String userNames = ParamUtil.get(request, "userNames");
	String[] ary = StrUtil.split(userNames, ",");
	if (ary==null) {
		out.print(StrUtil.Alert_Back("请选择用户！"));
		return;
	}
	UserDb user = new UserDb();
	
	for (int i=0; i<ary.length; i++) {
		user = user.getUser(ary[i]);
		user.setGroupCode(groupCode);
		user.save();
	}
	out.print(StrUtil.Alert_Redirect("操作成功！", "user_m.jsp?op=search&isShow=1&type=userName&groupCode=" + StrUtil.UrlEncode(groupCode)));
	return;
}
else if (op.equals("delFromGroup")) {
	String userName = ParamUtil.get(request, "userName");
	UserDb user = new UserDb();
	user = user.getUser(userName);
	user.setGroupCode("");
	user.save();
	out.print(StrUtil.Alert_Redirect("操作成功！", "user_m.jsp?op=search&isShow=1&type=userName&groupCode=" + StrUtil.UrlEncode(groupCode)));
	return;
}
else if (op.equals("setpolice")) {
	String uName = ParamUtil.get(request, "username");
	int value = ParamUtil.getInt(request, "value");
	
	UserDb user = new UserDb();
	user = user.getUser(uName);
	user.setIsPolice(value);
	if (user.save())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}

int pagesize = 20;
String sql = "select name from sq_user";
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = null;
if (op.equals("search")) {
	if (type.equals("userName")) {
		if (groupCode.equals(""))
			sql = "select name from sq_user where nick like "+StrUtil.sqlstr("%"+what+"%");
		else
			sql = "select name from sq_user where nick like "+StrUtil.sqlstr("%"+what+"%")+" and group_code=" + StrUtil.sqlstr(groupCode);
			
		sql += " order by " + orderBy + " " + sort;
		ri = jt.executeQuery(sql, curpage, pagesize);
	}
	else if (type.equals("arrested")) {
		if (groupCode.equals(""))
			sql = "select name from sq_user where nick like ? and releasetime>?";
		else
			sql = "select name from sq_user where nick like ? and releasetime>? and group_code=" + StrUtil.sqlstr(groupCode);
			
		sql += " order by " + orderBy + " " + sort;					
		ri = jt.executeQuery(sql, new Object[] {"%" + what + "%", "" + new java.util.Date().getTime()}, curpage, pagesize);
	}
	else if (type.equals("invalid")) {
		if (groupCode.equals(""))
			sql = "select name from sq_user where nick like ? and isValid=0";
		else
			sql = "select name from sq_user where nick like ? and isValid=0 and group_code=" + StrUtil.sqlstr(groupCode);
		sql += " order by " + orderBy + " " + sort;					
		ri = jt.executeQuery(sql, new Object[] {"%" + what + "%"}, curpage, pagesize);
	}
	else if (type.equals("ispolice")) {
		if (groupCode.equals(""))
			sql = "select name from sq_user where nick like ? and ispolice=1";
		else
			sql = "select name from sq_user where nick like ? and ispolice=1 and group_code=" + StrUtil.sqlstr(groupCode);
		sql += " order by " + orderBy + " " + sort;					
		ri = jt.executeQuery(sql, new Object[] {"%" + what + "%"}, curpage, pagesize);
	}
	else if (type.equals("needCheck")) {
		if (groupCode.equals(""))
			sql = "select name from sq_user where nick like ? and check_status=" + UserDb.CHECK_STATUS_NOT;
		else
			sql = "select name from sq_user where nick like ? and check_status=" + UserDb.CHECK_STATUS_NOT + " and group_code=" + StrUtil.sqlstr(groupCode);
		sql += " order by " + orderBy + " " + sort;					
		ri = jt.executeQuery(sql, new Object[] {"%" + what + "%"}, curpage, pagesize);
	}
}
else {
	sql += " order by " + orderBy + " " + sort;		
	ri = jt.executeQuery(sql, curpage, pagesize);
}

%>
<table width="100%" border="0">
	<form id=formsearch name=formsearch action="?op=search" method=post>
        <tr>
        <td align="center"><lt:Label res="res.label.forum.admin.user_m" key="by"/> 
		  <select name="type">
		  <option value="userName"><lt:Label res="res.label.forum.admin.user_m" key="user_name"/></option>
		  <option value="arrested"><lt:Label res="res.label.forum.admin.user_m" key="user_in_prison"/></option>
		  <option value="invalid"><lt:Label res="res.label.forum.admin.user_m" key="user_invalid"/></option>
		  <option value="ispolice"><lt:Label res="res.label.forum.admin.user_m" key="user_police"/></option>
		  <option value="needCheck"><lt:Label res="res.label.forum.admin.user_m" key="user_need_check"/></option>
		  </select>
			<lt:Label res="res.label.forum.admin.user_m" key="user_group"/>
			<select name="groupCode">
            <%
			UserGroupDb ugroup = new UserGroupDb();
			Vector result = ugroup.list();
			Iterator ir = result.iterator();
			String opts = "";
			while (ir.hasNext()) {
				ugroup = (UserGroupDb) ir.next();
				if (ugroup.getCode().equals(UserGroupDb.EVERYONE))
					continue;
				opts += "<option value=" + ugroup.getCode() + ">" + ugroup.getDesc() + "</option>";
			}
			%>
			<option value=""><lt:Label res="res.label.forum.admin.user_m" key="all"/></option>
                <%=opts%>
          </select>		  
          <input name="what" type="text" class="singleboarder">
          &nbsp; 
          <input name="Submit" type="submit" class="singleboarder" value="<lt:Label res="res.label.forum.admin.user_m" key="search"/>">
          <input type=hidden name=orderBy value="<%=orderBy%>">
          <input type=hidden name=sort value="<%=sort%>"></td>
        </tr></form>
  </table>
<%if (op.equals("search")) {%>  
  <script>
  formsearch.type.value = "<%=type%>";
  formsearch.what.value = "<%=what%>";
  formsearch.groupCode.value = "<%=groupCode%>";
  </script>
<%}%>
  <div align="center"></div>
<%		
		int credit = 0;
			
		ResultRecord rr = null;
		// PageConn pageconn = new PageConn(Global.getDefaultDB(), curpage, pagesize);
		paginator.init(jt.getTotal(), pagesize);
		//设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}%>
<table width="98%" border="0" align="center" class="p9">
  <tr>
    <td align="right"><% 
	 	if(!groupCode.equals("")){
	 %>
      <input value="选择用户" type="button" onClick="window.open('user_sel_multi.jsp','','toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=800,height=750')">
      <%
	  }
	 %></td>
  </tr>
</table>
  <table width="98%" border="0" align="center" class="p9">
    <tr>
      <td align="right"><%=paginator.getPageStatics(request)%></td>
    </tr>
  </table>    
  <TABLE width="98%" 
border=0 align=center cellPadding=2 cellSpacing=1 bgcolor="#edeced">
    <TBODY>
      <TR align=center bgColor=#f8f8f8> 
        <TD width=2% height=23 bgcolor="#E2E0DC"><font color="#525252">&nbsp;</font></TD>
        <TD width=14% bgcolor="#E2E0DC"><font color="#525252">
        <lt:Label res="res.label.forum.admin.user_m" key="user_name"/></font></TD>
        <TD width=3% height=23 bgcolor="#E2E0DC"><font color="#525252">
        <lt:Label res="res.label.forum.admin.user_m" key="gender"/></font></TD>
        <TD width=15% height=23 bgcolor="#E2E0DC" onClick="doSort('lastTime')" style="cursor:hand"><font color="#525252">
          <lt:Label res="res.label.listmember" key="lastTime"/></font>
		<%if (orderBy.equals("lastTime")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
						}%>		</TD>
        <TD width=8% bgcolor="#E2E0DC"><lt:Label res="res.label.forum.admin.user_m" key="police"/></TD>
        <TD width=5% bgcolor="#E2E0DC"><font color="#525252">
        <lt:Label res="res.label.forum.admin.user_m" key="credit"/></font></TD>
        <TD width=10% height=23 bgcolor="#E2E0DC" onClick="doSort('RegDate')" style="cursor:hand"><font color="#525252">
        <lt:Label res="res.label.forum.admin.user_m" key="reg_date"/></font>
						<%if (orderBy.equals("RegDate")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
						}%>
		</TD>
        <TD width=10% bgcolor="#E2E0DC" onClick="doSort('group_code')" style="cursor:hand"><lt:Label res="res.label.forum.admin.user_m" key="user_group"/>
          <span style="cursor:hand">
          <%if (orderBy.equals("group_code")) {
							if (sort.equals("asc")) 
								out.print("<img src='images/arrow_up.gif' width=8px height=7px>");
							else
								out.print("<img src='images/arrow_down.gif' width=8px height=7px>");
						}%>
        </span></TD>
        <TD width=5% bgcolor="#E2E0DC"><lt:Label res="res.label.forum.admin.user_m" key="user_in_prison"/></TD>
        <TD width=14% bgcolor="#E2E0DC">IP</TD>
        <TD width=14% bgcolor="#E2E0DC"><lt:Label key="op"/></TD>
      </TR>
      <%		
String id="",name="",RegDate="",Gender="",OICQ="",State="",myface="",arrestreason="",arrestpolice="";
Date arresttime = null;
int layer = 1,ispolice=0,arrestday=0;
int i = 0;
String RealPic = "";
UserDb user = new UserDb();
UserGroupMgr ugm = new UserGroupMgr();
IPStoreDb ipd = new IPStoreDb();
while (ri.hasNext()) {
 	    rr = (ResultRecord)ri.next(); 
		i++;
		name = rr.getString(1);
		user = user.getUser(name);
		RegDate = DateUtil.format(user.getRegDate(), "yyyy-MM-dd");
		Gender = user.getGender();
		if (Gender.equals("M"))
			Gender = "男";
		else if (Gender.equals("F"))
			Gender = "女";
		else
			Gender = "不详";
		
		OICQ = StrUtil.getNullString(user.getOicq());
		State = user.getState();
		if (State.equals("0"))
			State = "不详";
		RealPic = user.getRealPic();
		myface = user.getMyface();
		ispolice = user.getIsPolice();
		arrestday = user.getArrestDay();
		arrestreason = user.getArrestReason();
		arresttime = user.getArrestTime();
		arrestpolice = StrUtil.getNullString(user.getArrestPolice());
		credit = user.getCredit();
%>
      <TR align=center onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1"> 
        <TD height=23 align="left"> &nbsp; <%if (myface.equals("")) {%> <img src="../images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>" width=16 height=16> 
          <%}else{%> <img src="<%=user.getMyfaceUrl(request)%>" width=16 height=16> 
        <%}%>	      </TD>
        <TD height=23 align="left"><a target="_blank" href="../../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=user.getNick()%></a>
          <input type=hidden name=username value="<%=name%>">
          <input type=hidden name=CPages value="<%=curpage%>"></TD>
        <TD width=3% height=23><%=Gender%></TD>
        <TD width=15% height=23 align="left" style="padding-left:3px"><%=com.redmoon.forum.ForumSkin.formatDateTime(request, user.getLastTime())%></TD>
        <TD width=8%>
		<%
		if (ispolice>0)
			out.println("<font color=red>" + SkinUtil.LoadString(request, "yes") + "</font>[<a href='user_m.jsp?op=setpolice&value=0&CPages="+curpage+"&username="+StrUtil.UrlEncode(name,"utf-8")+"'>" + SkinUtil.LoadString(request, "no") + "</a>]");
		else
			out.println(SkinUtil.LoadString(request, "no") + "[<a href='user_m.jsp?op=setpolice&value=1&CPages="+curpage+"&username="+StrUtil.UrlEncode(name,"utf-8")+"'>" + SkinUtil.LoadString(request, "yes") + "</a>]");
		%>		</TD>
        <TD width=5%><%=credit%></TD>
        <TD width=10% height=23><%=RegDate%></TD>
        <TD width=10%>
		<%
		  UserGroupDb ugd = user.getUserGroupDb();
		  out.print(ugd.getDesc());
		%>		</TD>
        <TD width=5%><%
		Calendar c1 = fdate.add(arresttime, arrestday);//释放日期
		Calendar c2 = Calendar.getInstance();//当前日期
		if (fdate.compare(c1,c2)==1)
			out.println("<font color=red>" + SkinUtil.LoadString(request, "yes") + "</font>");
		else
			out.println(SkinUtil.LoadString(request, "no"));
		%>        </TD>
        <TD width=14%><%=user.getIp()%><br>
		<%=ipd.getPosition(user.getIp())%>
		</TD>
        <TD width=14% height=23>&nbsp;
        <a href="user_modify.jsp?username=<%=StrUtil.UrlEncode(name)%>&privurl=<%=privurl%>">
        <lt:Label res="res.label.forum.admin.user_m" key="manage"/></a>
		<%
			if(!groupCode.equals("")){
		%>
			&nbsp;&nbsp;<a onclick="return confirm('您确定要删除么？')" href="user_m.jsp?op=delFromGroup&groupCode=<%=StrUtil.UrlEncode(groupCode)%>&userName=<%=StrUtil.UrlEncode(name)%>">从用户组删除</a>
		<%
			}
		%>        
        </TD>
      </TR>
<%}%>
    </TBODY>
  </TABLE>
	
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td width="2%" height="23">&nbsp;</td>
      <td height="23" valign="baseline"> <div align="right"> 
          <%
	  String querystr = "op=" + op + "&username=" + StrUtil.UrlEncode(username) + "&type=" + type + "&what=" + StrUtil.UrlEncode(what) + "&groupCode=" + StrUtil.UrlEncode(groupCode) + "&orderBy=" + orderBy + "&sort=" + sort;
 	  out.print(paginator.getPageBlock(request, "user_m.jsp?"+querystr));
	  %>
	</div>	  </td>
    </tr>
  </table>
</div>
</body>
<SCRIPT language=javascript>
<!--

//-->
</script>
</html>
