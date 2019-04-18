<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ include file="../inc/inc.jsp"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>	
<jsp:useBean id="ppm" scope="page" class="com.redmoon.oa.person.PlanPeriodicityMgr"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>日程安排</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</HEAD>
<BODY>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
	
String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	userName = privilege.getUser(request);
}

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
}
String op = ParamUtil.get(request,"op");
if(op.equals("del")){
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = ppm.del(request);
	}
	catch (ErrMsgException e) {
		out.println(fchar.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	if (re){
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		out.println(fchar.jAlert_Redirect("删除周期性任务成功","提示", "plan_periodicity.jsp?userName=" + StrUtil.UrlEncode(userName)));
	}
}
%>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<script type="text/javascript" language="javascript">
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=200,left=350,width="+width+",height="+height);
}
</script>
<%
	String sql = "select id from user_plan_periodicity where user_name=" + StrUtil.sqlstr(userName) + " order by id desc";
	int pagesize = 10;int curpage = 0;
	Paginator paginator = new Paginator(request);
	curpage = paginator.getCurPage();
	PlanPeriodicityDb ppd = new PlanPeriodicityDb();
	ListResult lr = ppd.listResult(sql, curpage, pagesize);
	int total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = null;
	if(v != null) {
		ir = v.iterator();
	}
	paginator.init(total, pagesize);
	int totalpages = paginator.getTotalPages();
	if(totalpages == 0) {
		curpage = 1;
		totalpages = 1;
	}
	
	 Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH)+1;   
		int day = c.get(Calendar.DATE);
		int weekDate1 = c.get(Calendar.DAY_OF_WEEK)-1;
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
  <tr>
    <td width="19%" height="28" align="left"><input name="button" type="button" class="btn" onclick="open1()" value="添加" /></td>
    <td width="81%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tr>
    <td width="12%" class="tabStyle_1_title"> 标题</td>
    <td width="10%" class="tabStyle_1_title">起始时间</td>
	<td width="11%" class="tabStyle_1_title">结束时间</td>
	<td width="9%" class="tabStyle_1_title">提醒类型</td>
	<td width="11%" class="tabStyle_1_title">提醒日期</td>
	<td width="8%" class="tabStyle_1_title">提醒时间</td>
	<td width="26%" class="tabStyle_1_title">事务内容</td>
	<td width="13%" class="tabStyle_1_title">操作</td>
  </tr>
  <%	
  	String[] weekDate = {"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
	String title = "",content = "",startdate = "",enddate = "";
	int type = 0,id = 0,num = 0;
	String remindDate = "";String remindtype = "",remindTime = "";
	String remind = "";ResultRecord rr = null;
	while (ir!=null && ir.hasNext()) {
 		ppd = (PlanPeriodicityDb)ir.next();
		id = ppd.getInt("id");
		title = ppd.getString("title");
		content = ppd.getString("content");
		startdate = DateUtil.format(ppd.getDate("begin_date"),"yyyy-MM-dd HH:mm:ss");
		enddate = DateUtil.format(ppd.getDate("end_date"),"yyyy-MM-dd HH:mm:ss");
		type = ppd.getInt("remind_type");
		remindTime = ppd.getString("remind_time");
		remind = ppd.getString("remind_date");
		if(type == 1){
			remindtype = "按日提醒";
			remindDate = "";
		}
		if(type == 2){
			remindtype = "按周提醒";
			remindDate = weekDate[StrUtil.toInt(remind)];
		}
		if(type == 3){
			remindtype = "按月提醒";
			remindDate = remind+"日";
		}
		if(type == 4){
			remindtype = "按年提醒";
			num = remind.indexOf("-");
			remindDate = remind.substring(0,num)+"月"+remind.substring(num)+"日";
		}
	%>
	<tr>
	<td align="center"><%=title%></td>
	<td align="center"><%=startdate.substring(2,19)%></td>
	<td align="center"><%=enddate.substring(2,19)%></td>
	<td align="center"><%=remindtype%></td>
	<td align="center"><%=remindDate%></td>
	<td align="center"><%=remindTime%></td>
	<td align="center"><%=content%></td>
	<td align="center"><a href="plan_periodicity_edit.jsp?id=<%=id%>&userName=<%=StrUtil.UrlEncode(userName)%>">编辑</a>&nbsp;&nbsp;<a onClick="isDel()" style="cursor:pointer">删除</a></td>
	</tr>
<%}%>
</table>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="23" align="right">&nbsp;
      <%
				String querystr = "userName=" + StrUtil.UrlEncode(userName);
				out.print(paginator.getCurPageBlock("plan_periodicity.jsp?"+querystr));
				%>
      &nbsp;&nbsp;</td>
  </tr>
</table>
<script language="javascript">
function open1(){
	window.location.href = 'plan_periodicity_new.jsp?userName=<%=StrUtil.UrlEncode(userName)%>';
}
function isDel(){
	jConfirm("确定要删除吗？","提示",function(r){
		if(!r){return;}
		else{
			window.location.href="plan_periodicity.jsp?op=del&id=<%=id%>&userName=<%=StrUtil.UrlEncode(userName)%>&CPages=<%=curpage%>";
		}
	})
}
</script>
</BODY>
</HTML>

