<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.manager_list" key="sq_boardmanager"/></title>
<script language="javascript">
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
function setPerson(userName, userNick){
	form1.name.value = userNick;
}
</script>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

BoardManagerDb bmd = new BoardManagerDb();
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "name", name, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	String boardCode = ParamUtil.get(request, "boardCode");
	boolean isHide = ParamUtil.get(request, "isHide").equals("true");
	boolean isCanCheck = ParamUtil.get(request, "canCheck").equals("1");	
	if (boardCode.equals("not")) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.manager_list", "need_board")));
		return;
	}
	UserDb user = new UserDb();
	user = user.getUserDbByNick(name);
	if (user==null || !user.isLoaded()) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.manager_m", "user_not_exist") + name));
	}
	else {
		bmd.setName(user.getName());
		bmd.setBoardCode(boardCode);
		bmd.setHide(isHide);
		bmd.setCanCheck(isCanCheck);
		
		boolean re = bmd.create();
		if (!re)
			out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
		else
			out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "manager_list.jsp"));
	}
	return;
}
if (op.equals("del")) {
	String userName = ParamUtil.get(request, "userName");
	int count = bmd.delManager(userName);	
	if (count <= 0)
		out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	else
		out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "manager_list.jsp"));
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.manager_list" key="sq_boardmanager"/></td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="sq_boardmanager"/></td>
  </tr>  
  <tr> 
    <td valign="top"><br>
      <table width="99%"  border="0" align="center" cellpadding="2" cellspacing="1" bgcolor="#CCCCCC">
		<tr align="center" bgcolor="#F8F7F9">
		  <td width="12%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="user_name"/></td>
		  <td width="43%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="board"/></td>
		  <td width="16%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="last_time"/></td>
		  <td width="6%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="experience"/></td>
		  <td width="5%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="add_count"/></td>
		  <td width="6%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="elite_count"/></td>
		  <td width="8%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="online_time"/></td>
		  <td width="4%" bgcolor="#EFEBDE" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="op"/></td>
      </tr>
<%
String sql = "", sql1 = "";	
Vector vt = null;
Vector vt1 = null;
Iterator ir = null;
Iterator ir1 = null;
UserDb ud = new UserDb();
sql = "select distinct name from sq_boardmanager";
Leaf lf = null;
Directory dir = new Directory();
vt = ud.listResult(sql, 1, 300).getResult();
ir = vt.iterator();
while(ir.hasNext()){
	ud = (UserDb)ir.next();
	sql1 = "select boardcode, name from sq_boardmanager where name=" + StrUtil.sqlstr(ud.getName());
	vt1 = bmd.list(sql1);
	ir1 = vt1.iterator();
%>
      <tr align="center">
	    <td height="24" bgcolor="#FFF7FF"><a href="../../userinfo.jsp?username=<%=StrUtil.UrlEncode(ud.getName())%>" target="_blank"><%=ud.getNick()%></a></td>
		<td align="left" bgcolor="#FFF7FF">
<%
	while(ir1.hasNext()){
		bmd = (BoardManagerDb)ir1.next();
		lf = dir.getLeaf(bmd.getBoardCode());
		if (lf==null) {
			out.print(bmd.getBoardCode());
			continue;
		}
		out.print("<a href='manager_m.jsp?boardcode=" + bmd.getBoardCode() + "&boardname=" + StrUtil.UrlEncode(lf.getName()) + "'>" + lf.getName() + "</a>");
		if (bmd.isHide()) {
			out.print("(" + SkinUtil.LoadString(request, "res.label.forum.admin.manager_list", "hide") + ")");
		}
		out.print("&nbsp;&nbsp;");
	}
%>		  
	    </td>
        <td bgcolor="#FFF7FF"><%=DateUtil.format(ud.getLastTime(), "yyyy-MM-dd HH:mm")%></td>
        <td bgcolor="#FFF7FF" align="center"><%=ud.getExperience()%></td>
        <td bgcolor="#FFF7FF" align="center"><%=ud.getAddCount()%></td>
        <td bgcolor="#FFF7FF" align="center"><%=ud.getEliteCount()%></td>
        <td bgcolor="#FFF7FF" align="center"><%=(int)ud.getOnlineTime()%></td>
        <td bgcolor="#FFF7FF" align="center"><a href="javascript: if (confirm('<%=SkinUtil.LoadString(request, "confirm_del")%>')) window.location.href='manager_list.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>&op=del'"><lt:Label res="res.label.cms.dir" key="del"/></a></td>
      </tr>
<%}%>
    </table>
	<br>
	&nbsp;&nbsp;<lt:Label res="res.label.forum.admin.manager_list" key="description"/>
	<br>
   </td>
 </tr>
</table> 
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.manager_list" key="add_boardmanager"/></td>
  </tr>  
  <tr> 
    <td align="center">
   		<table width="90%">
		<form name="form1" method="post" action="manager_list.jsp?op=add">
		  <tr>
			<td width="6%" align="center"><lt:Label res="res.label.forum.admin.manager_list" key="nick"/></td>
		    <td align="left"><input type="text" size=20 name="name" style="border:1pt solid #636563;font-size:9pt">
	        <a href="#" onClick="openWin('forum_user_sel.jsp', 480, 420)"><lt:Label res="res.label.forum.admin.manager_list" key="select"/></a>&nbsp;&nbsp;&nbsp;&nbsp;
	        <lt:Label res="res.label.forum.admin.manager_list" key="board"/>	        
			<select name="boardCode" onChange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' <lt:Label res="res.label.forum.admin.move_board" key="can_not_select"/>'); return false;}">
	          <option value="not" selected><lt:Label res="res.label.forum.admin.move_board" key="sel_board"/></option>
	          <%
				com.redmoon.forum.Directory directory = new com.redmoon.forum.Directory();
				com.redmoon.forum.Leaf lf2 = directory.getLeaf("root");
				com.redmoon.forum.DirectoryView dv = new com.redmoon.forum.DirectoryView(lf2);
				dv.ShowDirectoryAsOptionsWithCode(out, lf2, lf2.getLayer());
				%>
              </select>
		    <lt:Label res="res.label.forum.admin.manager_list" key="is_hide"/>
			<select name="isHide">
			<option value="false" selected><lt:Label key="no"/></option>
			<option value="true"><lt:Label key="yes"/></option>
			</select>
			审核权限
            <select name="canCheck">
              <option value="0">
              <lt:Label key="no"/>
              </option>
              <option value="1">
              <lt:Label key="yes"/>
              </option>
            </select>
		    <input type="submit" value="<lt:Label key="op_add"/>"></td>
		  </tr>
		</form>
      </table>
	</td>
 </tr>
</table>                                                                   
</body>                                        
</html>                            
  