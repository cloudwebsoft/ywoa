<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
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
<script src="../inc/common.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
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
%>
<%@ include file="sms_user_inc_menu_top.jsp"%>
<script>
$("menu5").className="current";
</script>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="100%" valign="top"><%
		int pagesize = 20;
		String realname = "";
		SMSReceiveRecordDb srd = new SMSReceiveRecordDb();
		
		String sql = "select id from " + srd.getTable().getName() + " order by mydate desc";
				
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
              <td class="tabStyle_1_title" nowrap width="14%">发送者</td>
              <td class="tabStyle_1_title" nowrap width="16%">信息内容</td>
              <td class="tabStyle_1_title" nowrap width="16%">回复时间</td>
              <td class="tabStyle_1_title" nowrap width="20%">目标地址</td>
              <td width="22%" nowrap class="tabStyle_1_title">发送者手机</td>
              <td class="tabStyle_1_title" nowrap width="12%">操作</td>
            </tr>
            <%
java.util.Iterator ir = lr.getResult().iterator();	
int k = 0;
UserDb ud2 = new UserDb();
while (ir.hasNext()) {
	k++;
 	srd = (SMSReceiveRecordDb)ir.next();
	UserDb user = ud2.getUserDbByMobile(srd.getString("mobile"));
	if (user!=null)
		realname = user.getRealName();
%>
<form name=form<%=k%> action="sms_receive_list.jsp?op=edit&id=<%=srd.getLong("id")%>" method=post>
            <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
              <td bgcolor="#FFFFFF"><%=realname%></td>
              <td bgcolor="#FFFFFF"><%=srd.getString("content")%></td>
              <td bgcolor="#FFFFFF"><%=DateUtil.format(srd.getDate("mydate"), "yyyy-MM-dd HH:mm")%></td>
              <td bgcolor="#FFFFFF">
			  <%
			  String destAddr=srd.getString("dest_addr");
			  out.print(destAddr);
			  %>
			  &nbsp;<%
			  user = ud2.getUserDb(StrUtil.toInt(destAddr.substring(destAddr.length()-4)));
			  if (user!=null)
			  	out.print(user.getRealName());			  
			  %>
			  <input name="CPages" value="<%=curpage%>" type="hidden">
			  </td>
              <td bgcolor="#FFFFFF"><%=srd.getString("mobile")%></td>
              <td align="center" bgcolor="#FFFFFF"><a href="#" onClick="if (confirm('您确定要删除吗？')) window.location.href='sms_user_receive_list.jsp?op=del&id=<%=srd.getLong("id")%>'">删除</a></td>
            </tr></form>
            <%}%>
          </tbody>
        </table>
        <table width="96%"  border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td align="right">&nbsp;</td>
          </tr>
          <tr>
            <td align="right">
<%
	String querystr = "";
    out.print(paginator.getCurPageBlock("sms_user_receive_list.jsp?"+querystr));
%></td>
          </tr>
    </table></td></tr>
</table>
</body>
</html>