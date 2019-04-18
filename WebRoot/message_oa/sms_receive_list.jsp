<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理收到的短信</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin.sms";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
int curpage = ParamUtil.getInt(request, "CPages", 1);

if (op.equals("del")) {
	long id = ParamUtil.getLong(request, "id");
	SMSReceiveRecordDb srd = new SMSReceiveRecordDb();
	srd = (SMSReceiveRecordDb)srd.getQObjectDb(new Long(id));
	if (srd.del()) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "sms_receive_list.jsp?CPages=" + curpage));
		return;
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
		return;
	}
}

String mobile = ParamUtil.get(request, "mobile");
String msgText = ParamUtil.get(request, "msgText");
String strFromSendTime = ParamUtil.get(request, "fromTime");
String strToSendTime = ParamUtil.get(request, "toTime");
%>
<%@ include file="sms_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<form action="sms_receive_list.jsp?op=search" method="post">
<table width="80%"  border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
      <tr>
        <td height="24" colspan="4" class="tabStyle_1_title">&nbsp;&nbsp;查询短信</td>
      </tr>
      <tr>
        <td width="13%" height="24">&nbsp;手机号码</td>
        <td width="22%"><input type="text" name="mobile" size="20" maxlength="25" value="<%=mobile%>" /></td>
        <td width="11%">短信内容</td>
        <td width="54%"><input type="text" name="msgText" size="20" maxlength="25" value="<%=msgText%>" /></td>
      </tr>
      <tr>
        <td height="24">&nbsp;发送时间</td>
        <td colspan="3"><input maxlength="10" size="20" id="fromTime" name="fromTime" value="<%=strFromSendTime%>" />
          &nbsp;至
          <input maxlength="10" size="20" id="toTime" name="toTime" value="<%=strToSendTime%>" />
            </td>
      </tr>
      <tr>
        <td height="24" colspan="4" align="center"><input class="btn" type="submit" name="Submit" value=" 查 询 " /></td>
      </tr>
  </table>
</form>
    <%
		int pagesize = 20;
		String realname = "";
		SMSReceiveRecordDb srd = new SMSReceiveRecordDb();
		
		String sql = "select id from " + srd.getTable().getName() + " order by mydate desc";
		if(op.equals("search")){       
			sql = "select id from " + srd.getTable().getName();
			String con = "";
			if (!mobile.equals("")) {
				con += "mobile like " + StrUtil.sqlstr("%" + mobile + "%");
			}
			if (!msgText.equals("")) {
				if (!con.equals(""))
					con += " and ";
				con += "content like " + StrUtil.sqlstr("%" + msgText + "%");
			}
			
			java.util.Date fromSendTime = DateUtil.parse(strFromSendTime,
					"yyyy-MM-dd");
			java.util.Date toSendTime = DateUtil.parse(strToSendTime, "yyyy-MM-dd");
			if (fromSendTime != null && toSendTime != null) {
				strFromSendTime = SQLFilter.getDateStr(strFromSendTime, "yyyy-MM-dd");
				strToSendTime = SQLFilter.getDateStr(strToSendTime, "yyyy-MM-dd");
				if (!con.equals(""))
					con += " and ";
				con += "mydate >= " + strFromSendTime + " and mydate <= " +
						strToSendTime;
			} else {
				if (fromSendTime != null) {
					strFromSendTime = SQLFilter.getDateStr(strFromSendTime, "yyyy-MM-dd");
					if (!con.equals(""))
						con += " and ";
					con += "mydate >= " + StrUtil.sqlstr(strFromSendTime);
				}
				if (toSendTime != null) {
					strToSendTime = SQLFilter.getDateStr(strToSendTime, "yyyy-MM-dd");				
					if (!con.equals(""))
						con += " and ";
					con += "mydate <= " + StrUtil.sqlstr(strToSendTime);
				}
			}
	
			if (!con.equals("")) {
				con = " where " + con;
			}

			sql += con;
			sql += " order by mydate desc";
		}
						
		ListResult lr = srd.listResult(sql, curpage, pagesize);
		
	    int total = lr.getTotal();

		int totalpages;
		Paginator paginator = new Paginator(request, total, pagesize);
        // 设置当前页数和总页数
	    totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
      <table width="92%" border="0" align="center" class="p9">
        <tr>
          <td height="24" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
        </tr>
      </table>
        <table width="98%" align="center" cellpadding="3" cellspacing="1" class="tabStyle_1 percent98">
          <tbody>
            <tr>
              <td class="tabStyle_1_title" nowrap width="15%">发送者</td>
              <td class="tabStyle_1_title" nowrap width="17%">发送者手机</td>
              <td class="tabStyle_1_title" nowrap width="17%">信息内容</td>
              <td class="tabStyle_1_title" nowrap width="17%">回复时间</td>
              <td class="tabStyle_1_title" nowrap width="21%">目标地址</td>
              <td class="tabStyle_1_title" nowrap width="13%">操作</td>
            </tr>
            <%
java.util.Iterator ir = lr.getResult().iterator();	
int k = 0;
UserDb ud2 = new UserDb();
while (ir.hasNext()) {
	k++;
 	srd = (SMSReceiveRecordDb)ir.next();
	UserDb user = ud2.getUserDbByMobile(srd.getString("mobile"));
	realname = "";
	if (user!=null)
		realname = user.getRealName();
%>
<form name=form<%=k%> action="sms_receive_list.jsp?op=edit&id=<%=srd.getLong("id")%>" method=post>
            <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
              <td bgcolor="#FFFFFF"><%=realname%></td>
              <td bgcolor="#FFFFFF"><%=srd.getString("mobile")%></td>
              <td bgcolor="#FFFFFF"><%=srd.getString("content")%></td>
              <td bgcolor="#FFFFFF"><%=DateUtil.format(srd.getDate("mydate"), "yyyy-MM-dd HH:mm")%></td>
              <td bgcolor="#FFFFFF">
			  <%
			  String destAddr=srd.getString("dest_addr");
			  out.print(destAddr);
			  %>
			  &nbsp;<%
			  if (destAddr.length()>4) {
				  int userId = StrUtil.toInt(destAddr.substring(destAddr.length()-4));
				  user = ud2.getUserDb(userId);
			  }
			  if (user!=null)
			  	out.print(user.getRealName());			  
			  %>
			  <input name="CPages" value="<%=curpage%>" type="hidden">			  </td>
              <td align="center" bgcolor="#FFFFFF"><a href="#" onClick="if (confirm('您确定要删除吗？')) window.location.href='sms_receive_list.jsp?op=del&id=<%=srd.getLong("id")%>'">删除</a></td>
            </tr></form>
            <%}%>
          <tr></tbody>            
        </table>
        <table width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td align="right">
<%
	    String querystr = "";	
		if(op.equals("search")){
		    querystr += "&op=" + op + "&mobile=" + StrUtil.UrlEncode(ParamUtil.get(request, "mobile")) + "&fromTime=" + ParamUtil.get(request, "fromTime")
			+ "&toTime=" + ParamUtil.get(request, "toTime") + "&msgText=" + StrUtil.UrlEncode(ParamUtil.get(request, "msgText"));
		}
	    out.print(paginator.getCurPageBlock("sms_receive_list.jsp?"+querystr));
%></td>
          </tr>
    </table>
</body>
<script>
$(function () {
	$('#fromTime').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
	});
	$('#toTime').datetimepicker({
		lang:'ch',
		timepicker:false,
        format:'Y-m-d'
	});
});

</script>
</html>