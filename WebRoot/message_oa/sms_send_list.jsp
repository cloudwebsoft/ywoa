<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.message.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ include file="../inc/inc.jsp"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>短信发送列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
String sendMobile = ParamUtil.get(request, "sendMobile");
String msgText = ParamUtil.get(request, "msgText");
String strFromSendTime = ParamUtil.get(request, "fromSendTime");
String strToSendTime = ParamUtil.get(request, "toSendTime");
String receiver = ParamUtil.get(request, "receiver");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "sendtime";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<%@ include file="sms_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<br>
<form action="sms_send_list.jsp?op=search" method="post">
<div class="spacerH"></div>
<table width="80%"  border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
    <tr>
      <td height="24" colspan="4" class="tabStyle_1_title">&nbsp;&nbsp;查询短信</td>
    </tr>
    <tr>
      <td width="13%" height="24">&nbsp;用户名</td>
      <td width="22%"><input type="text" name="userName" size="20" maxlength="25" value="<%=userName%>"></td>
      <td width="11%">&nbsp;手机号码</td>
      <td width="54%"><input type="text" name="sendMobile" size="20" maxlength="25" value="<%=sendMobile%>"></td>
    </tr>
    <tr>
      <td height="24">&nbsp;发送内容</td>
      <td><input type="text" name="msgText" size="20" maxlength="25" value="<%=msgText%>"></td>
      <td>&nbsp;接收者</td>
      <td><input type="text" name="receiver" size="20" maxlength="25" value="<%=receiver%>"></td>
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

  <br>
  <%		
        String op = ParamUtil.get(request, "op");
		
		if (op.equals("del")) {
			String strIds = ParamUtil.get(request, "ids");
			if (strIds.equals("")) {
				out.print(StrUtil.Alert_Back("请选择短信！"));
				return;
			}
			String[] ids = strIds.split(",");
			SMSSendRecordDb ssrd2 = new SMSSendRecordDb();
			for (int i=0; i<ids.length; i++) {
				SMSSendRecordDb ssrd = ssrd2.getSMSSendRecordDb(StrUtil.toInt(ids[i]));
				ssrd.del();
			}
			out.print(StrUtil.Alert_Redirect("操作完成！", "sms_send_list.jsp"));
			return;
		}
		
		String sql = "";
		if(op.equals("search")){
			// sql = SMSSendRecordMgr.getSearchSendSMSSQL(request);
       
			sql = "select id from sms_send_record";
			String con = "";
	
			userName = ParamUtil.get(request, "userName");
			if (!userName.equals("")) {
				if (!con.equals(""))
					con += " and ";
				con += "userName like " + StrUtil.sqlstr("%" + userName + "%");
			}
	
			if (!sendMobile.equals("")) {
				if (!con.equals(""))
					con += " and ";
				con += "sendMobile like " + StrUtil.sqlstr("%" + sendMobile + "%");
			}
	
			if (!msgText.equals("")) {
				if (!con.equals(""))
					con += " and ";
				con += "msgText like " + StrUtil.sqlstr("%" + msgText + "%");
			}
	
			if (!receiver.equals("")) {
				if (!con.equals(""))
					con += " and ";
				con += "receiver like " + StrUtil.sqlstr("%" + receiver + "%");
			}
			
			java.util.Date fromSendTime = DateUtil.parse(strFromSendTime, "yyyy-MM-dd");
			java.util.Date toSendTime = DateUtil.parse(strToSendTime, "yyyy-MM-dd");
			if (fromSendTime != null && toSendTime != null) {
				if (!con.equals(""))
					con += " and ";
				con += "sendTime >= " + SQLFilter.getDateStr(strFromSendTime, "yyyy-MM-dd") + " and sendTime <= " +
						SQLFilter.getDateStr(strToSendTime, "yyyy-MM-dd");
			} else {
				if (fromSendTime != null) {
					if (!con.equals(""))
						con += " and ";
					con += "sendTime >= " + SQLFilter.getDateStr(strFromSendTime, "yyyy-MM-dd");
				}
				if (toSendTime != null) {
					if (!con.equals(""))
						con += " and ";
					con += "sendTime <= " + SQLFilter.getDateStr(strToSendTime, "yyyy-MM-dd");
				}
			}
	
			if (!con.equals("")) {
				con = " where " + con;
			}
	
			sql += con;			
		}else{
			// sql = SMSSendRecordMgr.getSendSMSSQL();
			sql = "select id from sms_send_record";
		}
		
		sql += " group by batch,id," + orderBy + " order by " + orderBy + " " + sort;
		
		//out.print(sql);
		
	    String querystr = "";	
		querystr += "orderBy=" + orderBy + "&sort=" + sort;
		if(op.equals("search")){
		    querystr += "&op=" + op + "userName=" + StrUtil.UrlEncode(ParamUtil.get(request, "userName")) + "&sendMobile=" + StrUtil.UrlEncode(ParamUtil.get(request, "sendMobile")) + "&fromSendTime=" + ParamUtil.get(request, "fromSendTime")
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
    <td width="41%" height="30" align="left">
    <!-- <input name="button3" class="btn" type="button" onClick="selAllCheckBox('ids')" value="全选">
&nbsp;&nbsp;
<input name="button3" class="btn" type="button" onClick="clearAllCheckBox('ids')" value="全不选">
&nbsp;&nbsp; -->
<!-- <input name="button22" class="btn" type="button" onClick="delSms()" value="删除"> --></td>
    <td width="59%" align="right"> 找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %> </td>
  </tr>
</table>
<table width="98%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent98">
  <tr>
  	<td width="3%" align="center" class="tabStyle_1_title" style="display:none">
    <input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" />
  	</td>
    <td width="9%" align="center" class="tabStyle_1_title" onClick="doSort('sendtime')" style="cursor:hand">
    	发送时间
      <%if (orderBy.equals("sendtime")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>
    </td>
    <td width="10%" align="center" class="tabStyle_1_title">发送者</td>
    <td width="10%" align="center" class="tabStyle_1_title">接收者</td>
    <td width="13%" align="center" class="tabStyle_1_title">手机号码</td>
    <td width="6%" align="center" class="tabStyle_1_title" onClick="doSort('is_sended')" style="cursor:hand">已发送
      <%if (orderBy.equals("is_sended")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>
    </td>
    <td width="39%" align="center" class="tabStyle_1_title">内容</td>
    <td width="10%" align="center" class="tabStyle_1_title">数量</td>
  </tr>
<%	
	    UserMgr um = new UserMgr();
	    int count = 0;
		while (ir.hasNext()) {
			ssrd = (SMSSendRecordDb)ir.next();
			long batch = ssrd.getBatch();
			count = ssrd.getCount(batch);
			//count = ssrd.getSMSCountOfBatch(batch);
			
			// System.out.println(getClass()+":::"+batch+"====="+count);
			String userRealName = "";
			UserDb ud = um.getUserDb(ssrd.getUserName());
			if (!ssrd.getUserName().equals(MessageDb.SENDER_SYSTEM))
				userRealName = ud.getRealName();
			else
				userRealName = MessageDb.SENDER_SYSTEM;
	  %>
  <tr class="highlight">
  <td width="3%" align="center" style="display:none"><input name="ids" type="checkbox" value="<%=ssrd.getId()%>"></td>
    <td width="9%" align="center">
    <a href="sms_send_detail_list.jsp?batch=<%=batch%>"><%=DateUtil.format(ssrd.getSendTime(), "yy-MM-dd HH:mm")%></a></td>
    <td width="10%" align="center"><%=userRealName%></td>
    <td width="10%" align="center"><%=(ssrd.getReceiver()==null||ssrd.getReceiver().equals(""))?"":ssrd.getReceiver()+"等"%></td>
    <td width="13%"><%=StrUtil.getNullStr(ssrd.getSendMobile()).equals("")?"":StrUtil.getNullStr(ssrd.getSendMobile())+"等"%></td>
    <td width="6%" align="center">
	<%=ssrd.isSended()?"是":"否"%>
	</td>
    <td width="39%" align="left"><%=ssrd.getMsgText()%></td>
    <td width="10%" align="left"><%=count %></td>
  </tr>
<%}%>
</table>
<table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
  <tr>
    <td height="23" align="right">&nbsp;
        <%
			  out.print(paginator.getCurPageBlock("?"+querystr));
		  %>
      &nbsp;&nbsp;</td>
  </tr>
</table>
<form name=formDel action="sms_send_list.jsp?op=del" method=post><input name=ids type=hidden>
</form>
</body>
<script>
$(function () {
	$('#fromSendTime').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
	});
	$('#toSendTime').datetimepicker({
		lang:'ch',
		timepicker:false,
        format:'Y-m-d'
	});
});

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "sms_send_list.jsp?op=<%=op%>&userName=<%=userName%>&sendMobile=<%=sendMobile%>&strFromSendTime=<%=StrUtil.UrlEncode(strFromSendTime)%>&strToSendTime=<%=strToSendTime%>&orderBy=" + orderBy + "&sort=" + sort + "&msgText=<%=msgText%>";
}
</script>
<script>
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
	if (!window.confirm("您确定要删除么？")) {
		return;
	}
	else {
		formDel.ids.value = getCheckboxValue("ids");
		if (formDel.ids.value=="") {
			alert("请选择短信");
			return;
		}
		formDel.submit();
	}
}
</script>
</html>
