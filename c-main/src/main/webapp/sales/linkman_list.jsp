<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = "sales_linkman";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
	</style>
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/sortabletable.js"></script>
<script type="text/javascript" src="../inc/columnlist.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<%@ include file="../inc/nocache.jsp"%>
<script>
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if(checkboxboxs[i].disabled==false)
				checkboxboxs[i].checked = true;
		}
	}
}
function clearAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
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
 
function sendSms() {
	var mobiles = getCheckboxValue("mobiles");
	if (mobiles=="") {
		jAlert("请选择人员！","提示");
		return;
	}
	window.location.href = "../message_oa/sms_send.jsp?mobile=" + mobiles;
}
</script>
</head>
<body>
<%
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
int customerId=0;
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales"))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
%>
<%if (op.equals("listOfCustomer")) {%>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<%}else{%>
<%@ include file="linkman_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<%}%>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
if (userName.equals(""))
	userName = privilege.getUser(request);

if (!userName.equals(privilege.getUser(request))) {
	if (!privilege.isUserPrivValid(request, "sales")) {
		if (!privilege.canAdminUser(request, userName)) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "您对该用户没有管理权限！"));
			return;
		}
	}
}
String querystr = "";

FormDb customerfd = new FormDb();
customerfd = customerfd.getFormDb("sales_customer");

String unitCode = privilege.getUserUnitCode(request);
//String sql = "select id from " + fd.getTableNameByForm() + " where unit_code=" + StrUtil.sqlstr(unitCode);	
String[] deptArr1 = null;
String deptstrs = "";
if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
	UserDb udb1 = new UserDb();
	udb1 = udb1.getUserDb(userName);
	deptArr1 = udb1.getAdminDepts();
	if(deptArr1.length>0){
		for(int t=0;t<deptArr1.length;t++){
			if("".equals(deptstrs)){
				deptstrs = "'"+deptArr1[t]+"'";
			}else{
				deptstrs += ",'"+deptArr1[t]+"'";
			}
		}
	}
}

String sql = "";
String query = ParamUtil.get(request, "query");
if (!query.equals(""))
	sql = query;
else
	if (op.equals("search")) {
		Iterator ir = fd.getFields().iterator();
		String cond = "";
		if (action.equals("")) {
			sql = "select a.id from " + fd.getTableNameByForm() + " a, " + customerfd.getTableNameByForm() + " b";
			cond = "a.customer=b.id and b.sales_person=" + StrUtil.sqlstr(userName);
		}
		else {
			//sql = "select id from " + fd.getTableNameByForm();
			sql = "select a.id from " + fd.getTableNameByForm() + " a, " + customerfd.getTableNameByForm() + " b";
			cond = " a.customer=b.id and a.unit_code="+StrUtil.sqlstr(privilege.getUserUnitCode(request));
			//cond = "unit_code=" + StrUtil.sqlstr(unitCode);
		}
		
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			String value = ParamUtil.get(request, ff.getName());
			String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
			if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
				if (name_cond.equals("0")) {
					// 时间段
					String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
					String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
					if (!fDate.equals("")) {
						if (cond.equals(""))
							cond += " a." + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
						else
							cond += " and a." + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
					}
					if (!tDate.equals("")) {
						if (cond.equals(""))
							cond += " a." + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
						else
							cond += " and a." + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
					}
				}
				else {
					// 时间点
					String d = ParamUtil.get(request, ff.getName());
					if (!d.equals("")) {
						cond = SQLFilter.concat(cond, "and", " a."+ff.getName() + "=" + StrUtil.sqlstr(d));
					}
				}
			}
			else if (ff.getType().equals(FormField.TYPE_SELECT)) {
				String[] ary = ParamUtil.getParameters(request, ff.getName());
				if (ary!=null) {
					int len = ary.length;
					if (len==1) {
						if (!ary[0].equals("")) {
							if (cond.equals(""))
								cond += " a." + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
							else
								cond += " and a." + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
						}
					}
					else {
						String orStr = "";
						for (int n=0; n<len; n++) {
							if (!ary[n].equals(""))
								orStr = SQLFilter.concat(orStr, "or", " a." + ff.getName() + "=" + StrUtil.sqlstr(ary[n]));
						}
						if (!orStr.equals(""))
							cond = SQLFilter.concat(cond, "and", orStr);
					}
				}
			}
			else {
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += " a." + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
						else
						    if(ff.getName().equals("customer")){
						    	if(!value.equals("")){
									long value_id = Integer.parseInt(value);
									FormDb fd_value = new FormDb("sales_customer");
									FormDAO fdao_value = new FormDAO(value_id,fd_value);
									value = fdao_value.getFieldValue("customer");
								}
								cond += " and b." + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
							}else{
								cond += " and a." + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
							}
					}
				}
				else if (name_cond.equals("1")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += "a." + ff.getName() + "=" + StrUtil.sqlstr(value);
						else
					        if(ff.getName().equals("customer"))
								cond += " and b." + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
							else
								cond += " and a." + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
					}
				}
			}
		}
		if(action.equals("manage")){
			if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager")){
				String salesPerson = getSalesPerson(privilege.getUser(request));
				if(!salesPerson.equals("")){
					cond += " and b.sales_person in ("+salesPerson+")";
				}
			} else if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && privilege.isUserPrivValid(request, "sales.manager")){
				if (!deptstrs.equals("")) {
					cond += " and b.dept_code in ("+deptstrs+")";
				}
			}
		}
		if (!cond.equals("")){
			sql = sql + " where " + cond;
		}
	}
	else {
		if (action.equals("")) {
			sql = "select a.id from " + fd.getTableNameByForm() + " a, " + customerfd.getTableNameByForm() + " b";
			sql += " where a.customer=b.id and b.sales_person=" + StrUtil.sqlstr(userName);
		}
		else {
			sql = "select a.id from " + fd.getTableNameByForm() + " a, " + customerfd.getTableNameByForm() + " b";
			sql += " where a.customer=b.id and a.unit_code="+StrUtil.sqlstr(privilege.getUserUnitCode(request));

			if(action.equals("manage")){
				if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager")){
					String salesPerson = getSalesPerson(privilege.getUser(request));
					if(!salesPerson.equals("")){
						sql += " and b.sales_person in ("+salesPerson+")";
					}
				} else if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && privilege.isUserPrivValid(request, "sales.manager")){
					if (!deptstrs.equals("")) {
						sql += " and b.dept_code in ("+deptstrs+")";
					}
				}
			}else{
				sql += " and b.sales_person=" + StrUtil.sqlstr(userName);
			}
			//sql = "select id from " + fd.getTableNameByForm() + " where unit_code=" + StrUtil.sqlstr(unitCode);
		}
		if (op.equals("listOfCustomer")) {
			customerId = ParamUtil.getInt(request, "customerId"); 
			sql = "select id from " + fd.getTableNameByForm() + " where customer=" + customerId;
		}
	}
	
querystr = "query=" + StrUtil.UrlEncode(sql) + "&action=" + action + "&op=" + op;	
if (customerId != -1) {
	querystr += "&customerId=" + customerId;
}

// out.print(sql);

		int pagesize = ParamUtil.getInt(request, "pagesize", 20);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		FormDAO fdao = new FormDAO();
		
		ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (v!=null)
			ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}
%>
      <table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
      	<thead>
        <tr>
        	<th width="20" style="cursor:pointer;display:<%=com.redmoon.oa.sms.SMSFactory.isUseSMS() ? "" : "none" %>"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('mobiles'); else clearAllCheckBox('mobiles')" /></th>
          <th width="40" style="cursor:pointer">编号</th> 
          <th width="120" style="cursor:pointer">联系人</th> 
          <th width="400" style="cursor:pointer">客户</th>
          <th width="120" style="cursor:pointer">业务员</th>
          <th width="120" style="cursor:pointer">手机</th>
          <th width="120" style="cursor:pointer">办公电话</th>
          <th width="180" style="cursor:pointer">操作</th>
        </tr>
        </thead>
      <%
	  	UserMgr um = new UserMgr();
	  	FormDAO fdaoCustomer = new FormDAO();
	  	int i = 0;
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long id = fdao.getId();
			String mob = fdao.getFieldValue("mobile");
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toLong(fdao.getFieldValue("customer")), customerfd);
			
			String realName = "";
			String saler = StrUtil.getNullStr(fdaoCustomer.getFieldValue("sales_person"));
			if (!saler.equals("")) {
				UserDb user = um.getUserDb(saler);
				realName = user.getRealName();
			}
		%>
        <tr>
		  <td style="display:<%=com.redmoon.oa.sms.SMSFactory.isUseSMS() ? "" : "none" %>">
		  <input type="checkbox" name="mobiles" <%if (mob == null || "".equals(mob)) {%>disabled="disabled"<%}%> value="<%=mob%>" />
		  </td>
	      <td><%=id%></td>
          <td><a href="linkman_show.jsp?id=<%=id%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=<%=formCode%>"><%=fdao.getFieldValue("linkmanName")%></a></td> 
          <td><a href="customer_show.jsp?id=<%=fdao.getFieldValue("customer")%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=sales_customer" target="_blank"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
          <td><%=realName%></td>
          <td><a href=../visual_show.jsp?id=<%=id%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=<%=formCode%>><%=StrUtil.getNullStr(fdao.getFieldValue("mobile"))%></a></td>
          <td><a href=../visual_show.jsp?id=<%=id%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=<%=formCode%>><%=StrUtil.getNullStr(fdao.getFieldValue("telNoWork"))%></a></td>
          <td align="center">
          <%if (op.equals("listOfCustomer")) {%>
          <a href="linkman_visit_add.jsp?op=<%=op%>&customerId=<%=ParamUtil.getLong(request, "customerId")%>&linkmanId=<%=id%>&formCode=day_lxr">添加行动</a>
          <%}else{%>
          <a href="linkman_visit_list.jsp?action=<%=StrUtil.UrlEncode(action)%>&linkmanId=<%=id%>">行动</a>
          <%}%>
          &nbsp;&nbsp;
          <a href="linkman_edit.jsp?id=<%=id%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=<%=StrUtil.UrlEncode(formCode)%>">编辑</a>
          <%
		    ModulePrivDb mpd = new ModulePrivDb(formCode);
		    if(mpd.canUserManage(new Privilege().getUser(request))) { %>
          &nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='../visual_del.jsp?id=<%=id%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=<%=formCode%>&privurl=<%=StrUtil.getUrl(request)%>'}})" style="cursor:pointer">删除</a>
          <%} %>
          &nbsp;&nbsp;
            <%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
<a href="#" onclick="window.open('../message_oa/sms_send.jsp?mobile=<%=fdao.getFieldValue("mobile")%>')">短信</a>
<%}%></td>
        </tr>
      <%
		}
%>
      </table> 
<script>
var o = new WebFXColumnList();
var ary = [ALIGN_CENTER,ALIGN_CENTER,ALIGN_CENTER,ALIGN_CENTER,ALIGN_CENTER,ALIGN_CENTER,ALIGN_CENTER];
o.setColumnAlignment(ary);
var rc = o.bind(document.getElementById('container'), document.getElementById('head'), document.getElementById('body'));
</script>
</body>
<script language="JavaScript" type="text/javascript">
function expandWindow(){
    var pageWidth = 400;
    if(document.documentElement&&(document.documentElement.clientWidth||document.documentElement.clientHeight)){
        pageWidth = document.documentElement.clientWidth;
    }
    else if(document.body&&(document.body.clientWidth||document.body.clientHeight)){
        pageWidth = document.documentElement.clientWidth;
    }
    if(document.body){
        document.body.style.width=pageWidth;
    }
}
window.onload=expandWindow;

function edit() {
	var id = o.getCellValue(o.getSelectedRow(), 0);
	if (id!=null && id!="undefined")
		window.location.href = "linkman_edit.jsp?id=" + id + "&formCode=<%=formCode%>&privurl=<%=StrUtil.getUrl(request)%>";
	else
		jAlert("请点击选择要编辑的行！","提示");
}

function del() {
	var ids = "";
	var rows = o.getSelectedRange();
	if (typeof rows == 'number') {
		if (rows==-1) {
			jAlert("请点击选择要删除的行！","提示");
			return;
		}
	}
	else {
		// ids = o.getCellValue(o.getSelectedRow(), 0);
		for (var i=0; i<rows.length; i++) {
			if (ids=="")
				ids += o.getCellValue(rows[i], 0);
			else
				ids += "," + o.getCellValue(rows[i], 0);
		}
	}
	if (ids=="") {
		jAlert("请点击选择要删除的行！","提示");
		return;
	}
	jConfirm("您确定要删除吗？","提示",function(r){
		if(!r){return;}
		else{ 
			window.location.href = "../visual_del.jsp?id=" + ids + "&formCode=<%=formCode%>&privurl=<%=StrUtil.getUrl(request)%>";
		}
	});
}
$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
			<%
			boolean isNeedComma = false;
			if (true || op.equals("listOfCustomer")) {
				isNeedComma = true;
			%>
			{name: '添加', bclass: 'add', onpress : actions}
		  	<%}%>
		  	<%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
				if (isNeedComma) {	
					out.print(",");
				}			
			%>
				{name: '群发短信', bclass: '', onpress : actions}
		  	<%}%>
			],
			/*
			searchitems : [
				{display: 'ISO', name : 'iso'},
				{display: 'Name', name : 'name', isdefault: true}
				],
			*/
			url: false,
			usepager: true,
			checkbox : false,
			page: <%=curpage%>,
			total: <%=total%>,
			useRp: true,
			rp: <%=pagesize%>,
			
			// title: "通知",
			singleSelect: true,
			resizable: false,
			showTableToggleBtn: true,
			showToggleBtn: false,
			
			onChangeSort: changeSort,
			
			onChangePage: changePage,
			onRpChange: rpChange,
			onReload: onReload,
			/*
			onRowDblclick: rowDbClick,
			onColSwitch: colSwitch,
			onColResize: colResize,
			onToggleCol: toggleCol,
			*/
			autoHeight: true,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 84
			}
		);
});

function changeSort(sortname, sortorder) {
	window.location.href = "linkman_list.jsp?pagesize=" + flex.getOptions().rp + "&<%=querystr%>";
}

function changePage(newp) {
	if (newp){
		window.location.href = "linkman_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp + "&<%=querystr%>";
		}
}

function rpChange(pageSize) {
	window.location.href = "linkman_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize + "&<%=querystr%>";
}

function onReload() {
	window.location.reload();
}
function actions(com, grid) {
	if(com=='添加'){
		window.location.href='linkman_add.jsp?op=listOfCustomer&formCode=sales_linkman&customerId=<%=customerId%>';
	}
  else if (com=='全选') {
  	selAllCheckBox('mobiles');
	}
	else if (com=='群发短信')
	{
		sendSms();
	}
}
</script>
</html>
<%!
	String getSalesPerson(String userName){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(StrUtil.sqlstr(userName));
		
		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
		String[] depts = ud.getAdminDepts();
		DeptUserDb dud = null;
		Vector v = null;
		Iterator ir = null;
		
		if(depts!=null){
			for(String dept:depts){
				dud = new DeptUserDb();
				v = dud.list(dept);
				if(v!=null){
					ir = v.iterator();
					while(ir.hasNext()){
						dud = (DeptUserDb)ir.next();
						buffer.append(",");
						buffer.append(StrUtil.sqlstr(dud.getUserName()));
					}
				}
			}
		}
		
		return buffer.toString();
	}
%>