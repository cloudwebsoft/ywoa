<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.stamp.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>印章日志</title>
<script src="../inc/common.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script language=javascript>
<!--
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null){
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

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.all.item(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

function formSearch_onsubmit() {
	formSearch.pageSize.value = form1.pageSize.value;
}
//-->
</script>
</head>
<body>
<%@ include file="stamp_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<%
String userName = ParamUtil.get(request, "userName");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String stampId = ParamUtil.get(request, "stampId");
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="100%" valign="top"><br>
      <form name="formSearch" action="stamp_log.jsp" onSubmit="return formSearch_onsubmit()" method="get">	
	<table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td align="center">
		用户名&nbsp;
		<input name="userName" size="6" value="<%=userName%>">
		&nbsp;印章名称
		<select id="stampId" name="stampId">
		<option value="">不限</option>
		<%
		StampDb sd = new StampDb();
		String sql = sd.getListSql(StampDb.KIND_DEFAULT);
		Iterator ir = sd.list(sql).iterator();
		while (ir.hasNext()) {
			sd = (StampDb)ir.next();
		%>
		<option value="<%=sd.getId()%>"><%=sd.getTitle()%></option>
		<%
		}
		%>		
		</select>
		<script>
		document.getElementById("stampId").value = "<%=stampId%>";
		</script>
		&nbsp;开始时间
		<input id="beginDate" name="beginDate" size="10" value="<%=beginDate%>">
		结束时间
		<input id="endDate" name="endDate" size="10" value="<%=endDate%>">
          <input name="pageSize" type="hidden">
          <input name="action" value="search" type="hidden">
          &nbsp;
		<input type="submit" value="搜索">
		</td>
      </tr>
    </table>
	</form>
      <%
		int pageSize = ParamUtil.getInt(request, "pageSize", 20);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		StampLogDb ld = new StampLogDb();
		String op = ParamUtil.get(request, "op");
		if (op.equals("del")) {
			int delid = ParamUtil.getInt(request, "id");
			StampLogDb ldb = ld.getStampLogDb(delid);
			if (ldb.del())
				out.print(StrUtil.Alert_Redirect("操作成功", "stamp_log.jsp?pageSize=" + pageSize + "&CPages=" + curpage));
			else
				out.print(StrUtil.Alert_Back("操作失败"));
			return;
		}
		else if (op.equals("delBatch")) {
			String[] ids = ParamUtil.getParameters(request, "ids");
			if (ids!=null) {
				int len = ids.length;
				for (int i=0; i<len; i++) {
					StampLogDb ldb = ld.getStampLogDb(StrUtil.toInt(ids[i]));
					ldb.del();
				}
				out.print(StrUtil.Alert_Redirect("操作成功", "stamp_log.jsp?pageSize=" + pageSize + "&CPages=" + curpage));
				return;
			}
		}
		String myname = privilege.getUser(request);
		sql = "select id from oa_stamp_log";
				
		String action = ParamUtil.get(request, "action");
		if (action.equals("search")) {
			String cond = "";
			if (!userName.equals("")) {
				if (cond.equals(""))
					cond = "user_name=" + StrUtil.sqlstr(userName);
				else
					cond += " and user_name=" + StrUtil.sqlstr(userName);
			}
			if (!beginDate.equals("")) {
				if (cond.equals(""))
					cond = "create_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
				else
					cond += " and create_date>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
			}
			if (!endDate.equals("")) {
				if (cond.equals(""))
					cond = "create_date<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
				else
					cond += " and create_date<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
			}
			if (!stampId.equals("")) {
				if (cond.equals(""))
					cond = "stamp_id=" + stampId;
				else
					cond += " and stamp_id=" + stampId;
			}
			if (!cond.equals(""))
				sql += " where " + cond;	
		}
		
		sql += " order by id desc";
		
		// System.out.println(sql);
				
		ListResult lr = ld.listResult(sql, curpage, pageSize);
		long total = lr.getTotal();
		Vector v = lr.getResult();
		if (v!=null)
		ir = v.iterator();
		paginator.init(total, pageSize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
      <br />
      <form name="form1" action="stamp_log.jsp?op=delBatch" method="post">
      <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
          <td width="50%" align="left">每页条数：<input name="pageSize" size="3" value="<%=pageSize%>" onChange="setPageSize()"><input type="submit" value="确定"></td>
          <td width="50%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %> </b></td>
        </tr>
      </table> 
      <table width="98%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent98">
        <tr>
          <td width="6%" align="center" class="tabStyle_1_title"><input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
          <td width="14%" align="center" class="tabStyle_1_title">日 期</td> 
          <td width="19%" align="center" class="tabStyle_1_title">用户名</td>
          <td width="12%" align="center" class="tabStyle_1_title">IP</td>
          <td width="10%" align="center" class="tabStyle_1_title">印章</td>
          <td width="9%" align="center" class="tabStyle_1_title">操 作</td>
        </tr>
      <%	
	    UserMgr um = new UserMgr();
		while (ir.hasNext()) {
			ld = (StampLogDb)ir.next();
			UserDb ud = um.getUserDb(ld.getString("user_name"));
			sd = sd.getStampDb(ld.getInt("stamp_id"));
		%>
        <tr class="highlight">
          <td width="6%" align="center"><input type="checkbox" name="ids" value="<%=ld.getLong("id")%>"></td>
          <td width="14%" align="center"><%=DateUtil.format(ld.getDate("create_date"), "yyyy-MM-dd HH:mm:ss")%></td> 
          <td width="19%" align="center"><%=ud.getName()%></td>
          <td width="12%" align="center"><%=ld.getString("ip")%></td>
          <td width="10%" align="center"><%=sd.getTitle()%></td>
          <td width="9%" align="center"><a onclick="return confirm('您确定要删除么？')" href="?op=del&id=<%=ld.getLong("id")%>">删除</a></td>
        </tr>
<%}%>
      </table>
      <table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
        <tr> 
          <td width="50%" height="23" align="left">&nbsp;
            <input onclick="del()" value="删除" class="btn" type="button"></td>
          <td width="50%" align="right"><%
				String querystr = "action=" + action + "&pageSize=" + pageSize + "&userName=" + StrUtil.UrlEncode(userName) + "&beginDate=" + beginDate + "&endDate=" + endDate;
				out.print(paginator.getCurPageBlock("?"+querystr));
				%></td>
        </tr>
      </table>
	  <input name="CPages" value="<%=curpage%>" type="hidden">
	  </form>
      <br>    </td>
  </tr>
</table>
</body>
<script>
$(function () {
	$('#beginDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
	});
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
        format:'Y-m-d'
	});
});

function del(){
    var checkedboxs = 0;
	var checkboxboxs = document.all.item("ids");
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked){
			   checkedboxs = 1;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].checked){
			   checkedboxs = 1;
			}
		}
	}
	if (checkedboxs==0){
	    alert("请先选择记录！");
		return;
	}
	if (confirm("您确定要删除吗？"))
		form1.submit();
}

function setPageSize() {
	window.location.href="stamp_log.jsp?action=<%=action%>&userName=<%=StrUtil.UrlEncode(userName)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>&pageSize=" + form1.pageSize.value + "&CPages=<%=curpage%>";
}
</script>
</html>
