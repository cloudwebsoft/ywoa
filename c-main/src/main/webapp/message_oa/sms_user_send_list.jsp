<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.message.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ include file="../inc/inc.jsp"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>短信发送记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String sendMobile = ParamUtil.get(request, "sendMobile");
String msgText = ParamUtil.get(request, "msgText");
String strFromSendTime = ParamUtil.get(request, "fromSendTime");
String strToSendTime = ParamUtil.get(request, "toSendTime");
String receiver = ParamUtil.get(request, "receiver");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "sendtime";
	
String op = ParamUtil.get(request, "op");

// 防SQL注入	
if (!cn.js.fan.db.SQLFilter.isValidSqlParam(orderBy)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ monitor/yoaacc_excel.jsp orderBy=" + orderBy);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "toSendTime", strToSendTime, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "receiver", receiver, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "fromSendTime", strFromSendTime, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "sendMobile", sendMobile, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "msgText", msgText, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
	
String sort = ParamUtil.get(request, "sort");
if (sort.equals("")) {
	sort = "desc";
}
%>
<%@ include file="sms_user_inc_menu_top.jsp"%>
<script>
o("menu4").className="current";
</script>
<br>
<form action="sms_user_send_list.jsp?op=search" method="post">
<div class="spacerH"></div>
<table width="80%"  border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tr>
      <td height="24" colspan="4" class="tabStyle_1_title">&nbsp;&nbsp;查询短信</td>
    </tr>
    <tr>
      <td width="13%" height="24">&nbsp;手机号码</td>
      <td colspan="3"><input type="text" name="sendMobile" size="20" maxlength="25" value="<%=sendMobile%>" /></td>
    </tr>
    <tr>
      <td height="24">&nbsp;发送内容</td>
      <td width="22%"><input type="text" name="msgText" size="20" maxlength="25" value="<%=msgText%>"></td>
      <td width="11%">接收者</td>
      <td width="54%"><input type="text" name="receiver" size="20" maxlength="25" value="<%=receiver%>"></td>
    </tr>
    <tr>
      <td height="24">&nbsp;发送时间</td>
      <td colspan="3"><input maxLength="10" size="20" id="fromSendTime" name="fromSendTime" value="<%=strFromSendTime%>">
		&nbsp;至
        <input maxLength="10" size="20" id="toSendTime" name="toSendTime" value="<%=strToSendTime%>">
	</td>
    </tr>
    <tr>
      <td height="24" colspan="4" align="center"><input class="btn" type="submit" name="Submit" value=" 查 询 "></td>
    </tr>
</table>
</form>
<%				
		if (op.equals("del")) {
			String strIds = ParamUtil.get(request, "ids");
			if (strIds.equals("")) {
				out.print(StrUtil.jAlert_Back("请选择短信！","提示"));
				return;
			}
			String[] ids = strIds.split(",");
			SMSSendRecordDb ssrd2 = new SMSSendRecordDb();
			for (int i=0; i<ids.length; i++) {
				SMSSendRecordDb ssrd = ssrd2.getSMSSendRecordDb(StrUtil.toInt(ids[i]));
				ssrd.del();
			}
			out.print(StrUtil.jAlert_Redirect("操作完成！","提示", "sms_user_send_list.jsp"));
			return;
		}
		
		String sql = "";
		if(op.equals("search")){
			// sql = SMSSendRecordMgr.getSearchSendSMSSQL(request);
       
			sql = "select id from sms_send_record where userName=" + StrUtil.sqlstr(privilege.getUser(request));
			String con = "";
	
			if (!sendMobile.equals("")) {
				con += " and sendMobile like " + StrUtil.sqlstr("%" + sendMobile + "%");
			}
	
			if (!msgText.equals("")) {
				con += " and msgText like " + StrUtil.sqlstr("%" + msgText + "%");
			}
	
			if (!receiver.equals("")) {
				con += " and receiver like " + StrUtil.sqlstr("%" + receiver + "%");
			}
			
			java.util.Date fromSendTime = DateUtil.parse(strFromSendTime,
					"yyyy-MM-dd");
			java.util.Date toSendTime = DateUtil.parse(strToSendTime, "yyyy-MM-dd");
			if (fromSendTime != null && toSendTime != null) {
				con += " and sendTime >= " + StrUtil.sqlstr(strFromSendTime) + " and sendTime <= " +
						StrUtil.sqlstr(strToSendTime);
			} else {
				if (fromSendTime != null) {
					con += " and sendTime >= " + StrUtil.sqlstr(strFromSendTime);
				}
				if (toSendTime != null) {
					con += " and sendTime <= " + StrUtil.sqlstr(strToSendTime);
				}
			}
			sql += con;			
		}else{
			// sql = SMSSendRecordMgr.getSendSMSSQL();
			sql = "select id from sms_send_record where userName=" + StrUtil.sqlstr(privilege.getUser(request));
		}
		
		sql += " group by batch,id," + orderBy + " order by " + orderBy + " " + sort;
		
		System.out.println(getClass() + " " + sql);
		
	    String querystr = "";	
		querystr += "orderBy=" + orderBy + "&sort=" + sort;
		if(op.equals("search")){
		    querystr += "&op=" + op + "&sendMobile=" + StrUtil.UrlEncode(ParamUtil.get(request, "sendMobile")) + "&fromSendTime=" + ParamUtil.get(request, "fromSendTime")
			+ "&toSendTime=" + ParamUtil.get(request, "toSendTime") + "&msgText=" + StrUtil.UrlEncode(ParamUtil.get(request, "msgText"));
		}
				
		int pagesize = 30;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		SMSSendRecordDb ssrd = new SMSSendRecordDb();
		ListResult lr = ssrd.listResult(sql, curpage, pagesize);
		int total = 0;
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (v!=null){
			ir = v.iterator();
			total = v.size();
		}
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>

<table width="95%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
  <tr>
    <td width="41%" height="30" align="left">&nbsp;&nbsp;&nbsp;&nbsp;
<!-- <input class="btn" name="button22" type="button" onClick="delSms()" value="删除"> --></td>
    <td width="59%" align="right"> 找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %> </td>
  </tr>
</table>
<table width="98%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent98">
  <tr>
    <td width="14%" align="center" class="tabStyle_1_title" onClick="doSort('sendtime')" style="cursor:hand">发送时间
      <%if (orderBy.equals("sendtime")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>    </td>
    <td width="11%" align="center" class="tabStyle_1_title">接收者</td>
    <td width="9%" align="center" class="tabStyle_1_title">短信数量</td>
    <td width="17%" align="center" class="tabStyle_1_title">手机号码</td>
    <td width="7%" align="center" class="tabStyle_1_title" onClick="doSort('is_sended')" style="cursor:hand">已发送
      <%if (orderBy.equals("is_sended")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>    </td>
    <td width="42%" align="center" class="tabStyle_1_title">内容</td>
  </tr>
<%	
	    UserMgr um = new UserMgr();
	    int count = 0;
		while (ir.hasNext()) {
			ssrd = (SMSSendRecordDb)ir.next();
			long batch = ssrd.getBatch();
			count = ssrd.getCount(batch);
			//count = ssrd.getSMSCountOfBatch(batch);
			String userRealName = "";
			UserDb ud = um.getUserDb(ssrd.getUserName());
			if (!ssrd.getUserName().equals(MessageDb.SENDER_SYSTEM))
				userRealName = ud.getRealName();
			else
				userRealName = MessageDb.SENDER_SYSTEM;
			count = ssrd.getCount(batch,ssrd.getUserName());
			String smsReceiver = StrUtil.getNullStr(ssrd.getReceiver());
	  %>
  <tr class="highlight">
    <td width="14%" align="center">
    <%if (count>0) {%>
    <a href="sms_user_send_detail_list.jsp?batch=<%=batch%>"><%=DateUtil.format(ssrd.getSendTime(), "yy-MM-dd HH:mm")%></a>
    <%}else{%>
    <%=DateUtil.format(ssrd.getSendTime(), "yy-MM-dd HH:mm")%>
    <%}%>
    </td>
    <td width="11%" align="center">
	<%=smsReceiver%>
    <%
	if (!smsReceiver.equals("") && count>1) {
		out.print("等");
	}%>
    </td>
    <td width="9%" align="left"><%=count%></td>
    <td width="17%">
	<%=ssrd.getSendMobile()%>
    <%
	if (count>1) {
		out.print("等");
	}%>
    </td>
    <td width="7%" align="center">
	<%=ssrd.isSended()?"是":"否"%>	</td>
    <td width="42%" align="left"><%=ssrd.getMsgText()%></td>
  </tr>
<%}%>
</table>
<table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
  <tr>
    <td height="23" align="right">&nbsp;
        <%
			  out.print(paginator.getCurPageBlock("sms_user_send_list.jsp?"+querystr));
		  %>
      &nbsp;&nbsp;</td>
  </tr>
</table>
<form name=formDel action="sms_user_send_list.jsp?op=del" method=post><input name=ids type=hidden>
</form>
</body>
<script>
$(function(){
	$('#fromSendTime').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
    $('#toSendTime').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
})
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "sms_user_send_list.jsp?op=<%=op%>&sendMobile=<%=sendMobile%>&strFromSendTime=<%=StrUtil.UrlEncode(strFromSendTime)%>&strToSendTime=<%=strToSendTime%>&orderBy=" + orderBy + "&sort=" + sort + "&msgText=<%=msgText%>";
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = false;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = false;
		}
	}
}

function delSms() {
	jConfirm("您确定要选择么？","提示",function(r){
		if(!r){return;}
		else{
			formDel.ids.value = getCheckboxValue("ids");
		formDel.submit();
		}
	})
}
</script>
</html>
