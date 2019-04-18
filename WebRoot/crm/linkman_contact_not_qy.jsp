<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String curUser = ParamUtil.get(request, "curUser");
if (curUser.equals(""))
	curUser = privilege.getUser(request);

// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String deptCode = ParamUtil.get(request, "deptCode");
if (!privilege.canAdminDept(request, deptCode)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

DeptDb dd = new DeptDb();
dd = dd.getDeptDb(deptCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>区域 - 未联系客户</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<%@ include file="../inc/nocache.jsp"%>
<script>
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null)
	{
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}
 
function sendSms() {
	var mobiles = getCheckboxValue("mobiles");
	if (mobiles=="") {
		alert("请选择人员！");
		return;
	}
	window.location.href = "../message_oa/sms_send.jsp?mobile=" + mobiles;
}
</script>
</head>
<body>
<%@ include file="crm_dept_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<div class="spacerH"></div>
<%
String op = ParamUtil.get(request, "op");
String sql = "select l.lxr,w.mydate,w.id,l.id,w.cur_user from form_table_day_lxr l, form_table_day_work_plan w where w.id=l.cws_id and l.is_visited='否' and w.dept=?";
sql += " order by w.id desc";		

String formCode = "sales_linkman";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

FormDb fdCustomer = new FormDb();
fdCustomer = fdCustomer.getFormDb("sales_customer");

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
			
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql, new Object[]{deptCode}, curpage, pagesize);

int total = (int)jt.getTotal();

paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

UserMgr um = new UserMgr();

String privurl = StrUtil.getUrl(request);
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
	<tr> 
	  <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></td>
	</tr>
</table>
      <table width="98%" align="center" cellspacing="0" cellpadding="0" class="tabStyle_1 percent98">
        <tr>
          <td width="6%" class="tabStyle_1_title">编号</td> 
          <td width="13%" class="tabStyle_1_title">联系人</td> 
          <td width="13%" class="tabStyle_1_title">制定日期</td>
          <td width="14%" class="tabStyle_1_title">客户</td>
          <td width="15%" class="tabStyle_1_title">手机</td>
          <td width="12%" class="tabStyle_1_title">客户经理</td>
          <td width="14%" class="tabStyle_1_title">操作</td>
        </tr>
      <%	
	  	int i = 0;
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			i++;
			int id = StrUtil.toInt(rr.getString(1));
			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(id, fd);
			String mob = fdao.getFieldValue("mobile");
			com.redmoon.oa.visual.FormDAO fdaoCustomer = new com.redmoon.oa.visual.FormDAO(StrUtil.toInt(fdao.getFieldValue("customer")), fdCustomer);
		%>
        <tr>
          <td><%if (!mob.equals("")) {%>
              <input type="checkbox" name="mobiles" value="<%=mob%>" />
              <%}%>
              <%=id%></td>
          <td><a href="../sales/linkman_show.jsp?id=<%=id%>&amp;formCode=<%=formCode%>" target="_blank"><%=fdao.getFieldValue("linkmanName")%></a></td>
          <td align="center"><%=DateUtil.format(rr.getDate(2), "yyyy-MM-dd")%></td>
          <td><%=fdaoCustomer.getFieldValue("customer")%></td>
          <td><%=StrUtil.getNullStr(fdao.getFieldValue("mobile"))%></td>
          <td>
		  <%
		  UserDb user = um.getUserDb(rr.getString("cur_user"));
		  if (user.isLoaded())
			  out.print(user.getRealName());
		  %></td>
	      <td align="center">
			<%if (curUser.equals(privilege.getUser(request)) || privilege.canAdminUser(request, curUser)) {%>
			<a target="_blank" href="../visual/module_edit_relate.jsp?parentId=<%=rr.getInt(3)%>&id=<%=rr.getInt(4)%>&menuItem=&formCodeRelated=day_lxr&formCode=day_work_plan&isShowNav=0">编辑</a>&nbsp;&nbsp;&nbsp;&nbsp;
			<a onclick="return confirm('您确定要删除么？')" href="../visual/module_list_relate.jsp?action=del&op=&id=<%=rr.getInt(4)%>&formCode=day_work_plan&formCodeRelated=day_lxr&menuItem=&parentId=<%=rr.getInt(3)%>&isShowNav=0&privurl=<%=privurl%>">删除</a>
			<%}%>		  </td>
        </tr>
      <%
		}
%>
      </table> 
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
	<tr> 
		<td width="50%"><input class="btn" type="button" onclick="selAllCheckBox('mobiles')" value="全选" />
&nbsp;
<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
<input class="btn" type="button" onclick="sendSms()" value="短信" />
<%}%></td>
	    <td width="50%" align="right"><%
String querystr = "";
out.print(paginator.getCurPageBlock("?" + querystr));
%></td>
	</tr>
</table>
</body>
</html>
