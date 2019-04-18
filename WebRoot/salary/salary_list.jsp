<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "archive.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
//获取表单
String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
//判断表单是否存在
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "";

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);

String unitCode = privilege.getUserUnitCode(request);

String sql = "SELECT id FROM form_table_gzd WHERE unit_code='" + unitCode + "'";
//年份下拉框中获取年份
String year = ParamUtil.get(request, "year");
//从月份下拉框中获取月份
String month = ParamUtil.get(request, "month");
//从输入框中获取员工姓名
String empName=ParamUtil.get(request,"empName");

String deptCode = ParamUtil.get(request, "deptCode");

if(!empName.equals("")){
	sql = "SELECT f.id FROM form_table_gzd f, users u WHERE f.unit_code='" + unitCode + "' and f.xm=u.name";
	sql += " AND u.realName like " + StrUtil.sqlstr("%" + empName + "%");
}

if(!year.equals("")){
    sql += " AND nf=" +StrUtil.sqlstr(year);	
}

if(!month.equals("")){
    sql += " AND yf=" + StrUtil.sqlstr(month);	
}
if (!deptCode.equals("") && !deptCode.equals(DeptDb.ROOTCODE)) {
	sql += " and bm=" + StrUtil.sqlstr(deptCode);
}

String employeeName= ParamUtil.get(request, "employeeName");
if(employeeName != null && !employeeName.equals("")){
	UserDb ud = new UserDb();
	employeeName = ud.getUserDbByRealName(employeeName) != null?ud.getUserDbByRealName(employeeName).getName() : employeeName;
}
String year_search=ParamUtil.get(request,"year_search");
String month_search=ParamUtil.get(request,"month_search");
//部门名可以从数据库中取得
String deptName=ParamUtil.get(request,"deptName");
String code=ParamUtil.get(request,"deptCode");
//取得基本工资的范围
//String symbolOfBasicSalary=ParamUtil.get(request,"symbolOfBasicSalary");
//基本工资可以从数据库中取得
//String basicSalary=ParamUtil.get(request,"basicSalary");
String basicWageFrom=ParamUtil.get(request,"basicWageFrom");
String basicWageTo=ParamUtil.get(request,"basicWageTo");

//取得应付工资的范围符号
//String symbolOfShouldPay=ParamUtil.get(request,"symbolOfShouldPay");
//应该支付的工资可以从数据库中取得
//String shouldPay=ParamUtil.get(request,"shouldPay");
String shouldPayFrom=ParamUtil.get(request,"shouldPayFrom");
String shouldPayTo=ParamUtil.get(request,"shouldPayTo");
//取得扣掉工资的范围符号
//String symbolOfDeductionWage=ParamUtil.get(request,"symbolOfDeductionWage");
//System.out.println(symbolOfDeductionWage instanceof String);
//扣掉的工资可以从数据库中取得
//String deductionWage=ParamUtil.get(request,"deductionWage");
String deductionWageFrom=ParamUtil.get(request,"deductionWageFrom");
String deductionWageTo=ParamUtil.get(request,"deductionWageTo");
//取得实际工资的范围符号
//String symbolOfRealWage=ParamUtil.get(request,"symbolOfRealWage");
//实际工资可以从数据库中取得
//String realWage=ParamUtil.get(request,"realWage");
String realWageFrom=ParamUtil.get(request,"realWageFrom");
String realWageTo=ParamUtil.get(request,"realWageTo");


//取得请假次数的范围符号
String symbolOfLeaveTime=ParamUtil.get(request,"symbolOfLeaveTimes");
//缺勤次数可以由病假,事假,漏打卡加总决定
String leaveTimes=ParamUtil.get(request,"leaveTimes");
if(!employeeName.equals("")){
	sql += " and xm="+ StrUtil.sqlstr(employeeName);
}
if(!deptName.equals("")){
	sql += " and bm=" +StrUtil.sqlstr(code);  
}
	  
if(!year_search.equals("")){
	sql += " and nf="+StrUtil.sqlstr(year_search);  
}
if(!month_search.equals("")){
	sql += " and yf="+StrUtil.sqlstr(month_search);  
}
if((!basicWageFrom.equals(""))&& (!basicWageTo.equals(""))){
		sql=sql+" and jbgz>="+ Integer.parseInt(basicWageFrom)+" and jbgz<="+Integer.parseInt(basicWageTo); 
}

if((!shouldPayFrom.equals(""))&&(!shouldPayFrom.equals(""))){
	/*
	if(symbolOfShouldPay.equals(">")){
		sql=sql+" and yfgz>"+ Integer.parseInt(shouldPay);
	}else if(symbolOfShouldPay.equals("<")){
		sql=sql+ " and yfgz<"+Integer.parseInt(shouldPay);
	}else{
		sql=sql+" and yfgz="+Integer.parseInt(shouldPay); 
	}*/
	sql=sql+" and yfgz>="+ Integer.parseInt(shouldPayFrom)+" and yfgz<=" +Integer.parseInt(shouldPayTo);
}

//有问题
if((!deductionWageFrom.equals(""))&& (!deductionWageTo.equals(""))){
	/*
	if(symbolOfDeductionWage.equals(">")){
		sql=sql+" and kk>"+Integer.parseInt(deductionWage);
	}else if(symbolOfDeductionWage.equals("<")){
	    sql=sql+" and kk<"+Integer.parseInt(deductionWage);
	}else{
		sql=sql+" and kk="+Integer.parseInt(deductionWage);  
	} */ 
	sql=sql+" and kk>"+Integer.parseInt(deductionWageFrom)+" and kk<="+Integer.parseInt(deductionWageTo);
}
if((!realWageFrom.equals(""))&&(!realWageTo.equals(""))){
	/*
	if(symbolOfRealWage.equals("<")){
		sql=sql+" and bysf<"+Integer.parseInt(realWage);
	}else if(symbolOfRealWage.equals(">")){
		sql=sql+ " and bysf>"+Integer.parseInt(realWage);
	}else{
		sql=sql+ " and bysf="+Integer.parseInt(realWage);
	}*/
	sql=sql+" and bysf>="+Integer.parseInt(realWageFrom)+" and kk<="+Integer.parseInt(realWageTo);
}
//缺勤次数可以由病假,事假,漏打卡加总决定
if(!leaveTimes.equals("")){
	if(symbolOfLeaveTime.equals("<")){
        sql=sql+" and (sj+bj)<"+Integer.parseInt(leaveTimes);  
	}else if(symbolOfLeaveTime.equals(">")){
	    sql=sql+" and (sj+bj)>"+Integer.parseInt(leaveTimes); 
	}else{
	    sql=sql+" and (sj+bj)="+Integer.parseInt(leaveTimes); 
    }
}

sql+=" order by id desc" ;	

String sqlUrlStr = ary[1];

querystr = "op=" + op + "&code=" + formCode + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort + "&year=" + year + "&month=" + month + "&empName=" + StrUtil.UrlEncode(empName) + "&deptCode=" + deptCode ;
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;

String action = ParamUtil.get(request, "action");
if (action.equals("del")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
		
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (fdm.del(request)) {
			out.print(StrUtil.Alert_Redirect("删除成功！", "salary_list.jsp?" + querystr + "&CPages=" + curpage));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="salary_inc_menu_top.jsp"%>
<script>
$("menu1").className="current"; 
</script>
<%
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
int total = lr.getTotal();
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
		
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
String[] fieldsOrder = StrUtil.split(listFieldOrder, ",");

MacroCtlMgr mm = new MacroCtlMgr();
%>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td><div style="float:left">
    <form id="DateQequest" name="DateQequest" method="post"  action="">
     &nbsp;
<select name="year" id="year">
<option value="">-请选择-</option>
              <option value="2008">2008</option>
              <option value="2009">2009</option>
              <option value="2010">2010</option>
              <option value="2011">2011</option>
              <option value="2012">2012</option>
              <option value="2013">2013</option>
              <option value="2014">2014</option>
              <option value="2015">2015</option>
              <option value="2016">2016</option>
              <option value="2017">2017</option>
              <option value="2018">2018</option>
              <option value="2019">2019</option>
              <option value="2020">2020</option>
              <option value="2021">2021</option>
              <option value="2022">2022</option>
              <option value="2023">2023</option>
              <option value="2024">2024</option>
              <option value="2025">2025</option>
              <option value="2026">2026</option>
              <option value="2027">2027</option>
    </select>
     年
     <select name="month" id="month" >
    <option value="">-请选择-</option>
             <option value="1月">1月</option>
             <option value="2月">2月</option>
             <option value="3月">3月</option>
             <option value="4月">4月</option>
             <option value="5月">5月</option>
             <option value="6月">6月</option>
             <option value="7月">7月</option>
             <option value="8月">8月</option>
             <option value="9月">9月</option>
             <option value="10月">10月</option>
             <option value="11月">11月</option>
             <option value="12月">12月</option>
      </select>
     月
	  <select id="deptCode" name="deptCode" >
	    <%
		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(unitCode);
		DeptView dv = new DeptView(dd);
		dv.ShowDeptAsOptions(out, dd, 1);
		%>
    </select>        
        姓名
        <input type="text" id="empName" name="empName" size="8"/>
        <input class="tSearch" name="submit" type=submit value="搜索">
    </form></td>
    <td height="23" align="left">
<%
String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, ",");
int len = 0;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
	%>
	&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn" type="button" value="<%=btnNames[i]%>" onclick="<%=StrUtil.HtmlEncode(btnScripts[i])%>" />
	<%
	}
}
%>	  
    </td>
  </tr>
</table>
<table id="grid" border="0" cellpadding="2" cellspacing="0">
  <thead>
  <tr>
<%
len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	String title = "创建者";
	if (!fieldName.equals("cws_creator"))
		title = fd.getFieldTitle(fieldName);
	String w = fieldsWidth[i];
	int wid = StrUtil.toInt(w, 50);
	if (w.indexOf("%")==w.length()-1) {
		w = w.substring(0, w.length()-1);
		wid = 800*StrUtil.toInt(w, 20)/100;
	}
%>
    <th width="<%=wid%>" style="cursor:hand" abbr="<%=fieldName%>">
	<%=title%>	
	</th>
<%}%>
    <th width="120" style="cursor:hand" align="center">操作</th>
  </tr>
  </thead>
  <tbody>
  <%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
		%>
  <tr align="center" id="<%=id%>">
<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>	
		<td align="left">
		<%if (!fieldName.equals("cws_creator")) {
			FormField ff = fd.getFormField(fieldName);
			if (ff==null) {
				out.print(fieldName + " 已不存在！");
			}
			else {
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				}
				else {%>		
					<%=fdao.getFieldValue(fieldName)%>
				<%}
			}%>
		<%}else{
			String realName = "";
			if (fdao.getCreator()!=null) {
				UserDb user = um.getUserDb(fdao.getCreator());
				if (user!=null)
					realName = user.getRealName();
			}
		%>
			<%=realName%>
		<%}%>
        </td>
	<%}%>
	<td>
		<a href="javascript:;" onclick="addTab('查看工资', '<%=request.getContextPath()%>/salary/salary_show.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>')">		
    	查看&nbsp;&nbsp;
        </a>
		<%if (mpd.canUserManage(privilege.getUser(request))) {%>
		<a href="javascript:;" onclick="addTab('编辑工资', '<%=request.getContextPath()%>/salary/salary_edit.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>')">编辑</a>&nbsp;&nbsp;<a onclick="if (!confirm('您确定要删除么？')) event.returnValue=false;" href="<%=request.getContextPath()%>/salary/salary_list.jsp?action=del&op=<%=op%>&id=<%=id%>&formCode=<%=formCode%>&CPages=<%=curpage%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=sqlUrlStr%>">删除</a>
		<%}%>
    </td>
  </tr>
  <%
  }
%>
  </tbody>
</table>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "<%=request.getContextPath()%>/salary/salary_list.jsp?op=<%=op%>&formCode=<%=formCode%>&pageSize=" +  flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/salary/salary_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" +  flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "<%=request.getContextPath()%>/salary/salary_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '添加', bclass: 'add', onpress : action},
		{name: '删除', bclass: 'delete', onpress : action},
		{name: '导入', bclass: 'import1', onpress : action},		
		{name: '导出', bclass: 'export', onpress : action},
		{separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	*/
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
	url: false,
	usepager: true,
	checkbox : true,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	// title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
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

function action(com, grid) {
	if (com=="导出") {
		window.open('<%=request.getContextPath()%>/visual/module_excel.jsp?isAll=true&<%=querystr%>');
	}
	else if (com=="导入") {
		var url = "<%=request.getContextPath()%>/visual/module_import_excel.jsp?isAll=true&formCode=<%=formCode%>";
		openWin(url,360,50);		
	}	
	else if (com=="添加") {
		window.location.href = "salary_add.jsp?formCode=<%=formCode%>";
	}
	else if (com=='删除') {
		selectedCount = $(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).length;	
		if (selectedCount == 0) {
			alert('请选择一条记录!');
			return;
		}
		
		if (!confirm("您确定要删除么？"))
			return;
		
		var ids = "";
		$(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).each(function(i) {
			if (ids=="")
				ids = $(this).val();
			else
				ids += "," + $(this).val();
		});
		window.location.href = "salary_list.jsp?action=del&formCode=<%=formCode%>&<%=querystr%>&CPages=<%=curpage%>&id=" + ids + "&pageSize=" + flex.getOptions().rp;
	}
}

$(function() {
	$("#deptCode").val("<%=deptCode%>");
	$("#year").val("<%=year%>");
	$("#month").val("<%=month%>");
	$("#empName").val("<%=empName%>");
});
</script>
</html>
