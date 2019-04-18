<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.forbidip" key="forbidip"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	String ip = ParamUtil.get(request, "ip").trim();
	if (!StrUtil.isValidIP(ip)) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.forbidip", "ip_format_error")));
		return;
	}
	String userName = ParamUtil.get(request, "userName").trim();
	String reason = ParamUtil.get(request, "reason").trim();
	if (ip.equals("")) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.forbidip", "ip_can_not_empty")));
	}
	else {
		ForbidIPDb fid = new ForbidIPDb();
		fid = fid.getForbidIPDb(ip);
		if (fid.isLoaded())
			out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.forum.admin.forbidip", "ip_forbided"))); 
		else {
			fid.setIp(ip);
			fid.setUserName(userName);
			fid.setReason(reason);
			if (fid.create())
				out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
			else
				out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
		}
	}
}

if (op.equals("del")) {
	String ip = ParamUtil.get(request, "ip").trim();
	ForbidIPDb fid = new ForbidIPDb();
	fid = fid.getForbidIPDb(ip);
	if (fid.del())
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.forbidip" key="forbid_single_ip"/>&nbsp;&nbsp;<a href="forbidiprange.jsp"><lt:Label res="res.label.forum.admin.forbidip" key="forbid_single_ip_range"/></a>&nbsp;</td>
  </tr>
</table>
<br>
<%
	ForbidIPDb fid = new ForbidIPDb();
	
	String sql = "select ip from sq_forbid_ip order by add_date desc";
	int total = fid.getObjectCount(sql);	
	int pagesize = 20;
	
	int curpage,totalpages;
	Paginator paginator = new Paginator(request, total, pagesize);
	//设置当前页数和总页数
	totalpages = paginator.getTotalPages();
	curpage	= paginator.getCurrentPage();
	if (totalpages==0)
	{
		curpage = 1;
		totalpages = 1;
	}	
%>
<table width="98%" height="227" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.forbidip" key="ip_manage"/> </td>
  </tr>  
  <tr> 
    <td valign="top"><br>
      <table width="92%" border="0" align="center">
      <tr>
        <td align="right"><%=paginator.getPageStatics(request)%></td>
      </tr>
    </table>
      <table width="92%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999">
      <tr align="center" bgcolor="#F8F7F9">
        <td width="13%" height="24" bgcolor="#EFEBDE"><strong>IP</strong></td>
        <td width="16%" height="24" bgcolor="#EFEBDE"><span class="thead">
          <lt:Label res="res.label.forum.admin.forbidip" key="ip_manage"/>
        </span></td>
        <td width="38%" height="24" bgcolor="#EFEBDE"><strong><lt:Label res="res.label.forum.admin.forbidip" key="reason"/></strong></td>
        <td width="21%" bgcolor="#EFEBDE"><strong><lt:Label res="res.label.forum.admin.forbidip" key="forbid_date"/></strong></td>
        <td width="12%" bgcolor="#EFEBDE"><strong><lt:Label key="op"/></strong></td>
      </tr>
	<%
	ObjectBlockIterator oir = fid.getObjects(sql, (curpage-1)*pagesize, curpage*pagesize);
	
	while (oir.hasNext()) {
		fid = (ForbidIPDb)oir.next();
	%>
      <tr align="center">
        <td height="24" bgcolor="#FFF7FF"><%=fid.getIp()%></td>
      	<td height="24" bgcolor="#FFF7FF"><%=StrUtil.getNullStr(fid.getUserName())%></td>
        <td height="24" bgcolor="#FFF7FF"><%=StrUtil.getNullStr(fid.getReason())%></td>
      <td bgcolor="#FFF7FF">
	  	<%=DateUtil.format(fid.getAddDate(), "yyyy-MM-dd HH:mm:ss")%>
	  </td>
        <td height="24" bgcolor="#FFF7FF"><a href="?op=del&ip=<%=fid.getIp()%>"><lt:Label key="op_del"/></a>		</td>
      </tr>
	<%}%>
    </table>
      <table width="92%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr>
          <td height="23" align="right">
              <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("forbidip.jsp?"+querystr));
	%>          </td>
        </tr>
      </table>
      <br>
    <table width="50%" border="0" align="center" cellpadding="2" cellspacing="0" class="tableframe_gray">
	<form name=form1 action="?op=add" method=post>
      <tr>
        <td colspan="2" align="center" class="thead"><lt:Label res="res.label.forum.admin.forbidip" key="add_ip"/></td>
        </tr>
      <tr>
        <td width="22%">IP</td>
        <td width="78%"><input name="ip" maxlength="15" ></td>
      </tr>
      <tr>
        <td><lt:Label res="res.label.forum.admin.forbidip" key="user_name"/>          </td>
        <td><input name="userName" maxlength="20" ></td>
      </tr>
      <tr>
        <td><lt:Label res="res.label.forum.admin.forbidip" key="reason"/></td>
        <td><input name="reason" size="50" ></td>
      </tr>
      <tr>
        <td colspan="2" align="center"><input name="submit" type="submit" value="<lt:Label key="op_add"/>"></td>
        </tr>
	</form>
    </table> 
    <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
                               
</body>                                        
</html>                            
  