<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id"; // "find_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String userName = ParamUtil.get(request, "userName");
if (userName.equals(""))
	userName = privilege.getUser(request);
String customer = ParamUtil.get(request, "customer");
String find_dateFromDate = ParamUtil.get(request, "find_dateFromDate");
String find_dateToDate = ParamUtil.get(request, "find_dateToDate");

String enterType = ParamUtil.get(request, "enterType");
String tel = ParamUtil.get(request, "tel");
// String category = ParamUtil.get(request, "category");
String star = ParamUtil.get(request, "star");

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式

String dept_code = ParamUtil.get(request, "dept_code");
String[] deptArr1 = null;
String cond = "";
String deptstrs = "";
if("".equals(dept_code)){
	if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
		deptstrs = "'"+getDeptCode(userName)+"'";
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
		dept_code = deptstrs;
	}
}

if (deptstrs.equals("") && !dept_code.equals("")) {
	deptstrs = StrUtil.sqlstr(dept_code);
}

try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
 
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")&&!privilege.isUserPrivValid(request, "sales.manager")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = "sales_customer";
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales")&& !privilege.isUserPrivValid(request, "sales.manager")) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

if (!userName.equals(privilege.getUser(request))) {
	// 检查是否有管理用户所在部门的权限
	DeptUserDb dud = new DeptUserDb();
	Vector vDept = dud.getDeptsOfUser(userName);
	Iterator vIr = vDept.iterator();
	boolean canAdminUser = false;
	while (vIr.hasNext()) {
		DeptDb dd = (DeptDb)vIr.next();
		if (com.redmoon.oa.pvg.Privilege.canUserAdminDept(request, dd.getCode())) {
			canAdminUser = true;
			break;
		}
	}
	if (!canAdminUser) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "您对该用户没有管理权限！"));
		return;
	}
}

MacroCtlMgr mm = new MacroCtlMgr();
String query = ParamUtil.get(request, "query");
String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
String sql = "select id from " + fd.getTableNameByForm() + " where 1=1";		

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
if (!query.equals("")) { // 分页传过来的sql
	sql = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.getKey(), query);
	if (op.equals("sort")) {
		int p = sql.lastIndexOf("order by");
		if (p!=-1) {
			sql = sql.substring(0, p);
			if(action.equals("manage")){
				if(!privilege.isUserPrivValid(request, "sales")){
					String salesPerson = getSalesPerson(privilege.getUser(request));
					if(!salesPerson.equals("")){
						sql += " and sales_person in ("+salesPerson+")";
					}
				}
			}
			sql += " order by " + orderBy + " " + sort;		
			query = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg.getKey(), sql);
			
			op = "";
		}
	}
}
else {
	if (op.equals("search")) {
		if (action.equals("")){
			cond = "sales_person=" + StrUtil.sqlstr(userName);
		}else {
			cond = "unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request));
		}
		
		/*
		String cond = "";
		if (!customer.equals(""))
			cond += " and customer like " + StrUtil.sqlstr("%" + customer + "%");
		if (!customerType.equals(""))
			cond += " and customer_type=" + customerType;
		if (!enterType.equals(""))
			cond += " and enterType=" + enterType;
		if (!tel.equals(""))
			cond += " and tel like " + StrUtil.sqlstr("%" + tel + "%");
		if (!cond.equals(""))
			sql += cond;
		*/
		
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			String value = ParamUtil.get(request, ff.getName());
			String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
						
			String ctlType = ff.getType();
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu.getIFormMacroCtl()!=null) {
					ctlType = mu.getIFormMacroCtl().getControlType();
				}
			}
			
			// System.out.println(getClass() + " " + ff.getName() + " name_cond=" + name_cond);
			if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
				if (name_cond.equals("0")) {
					// 时间段
					String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
					String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
					
					if (!tDate.equals("")) {
						java.util.Date d = DateUtil.parse(tDate, "yyyy-MM-dd");
						if (d!=null) {
							d = DateUtil.addDate(d, 1);
							tDate = DateUtil.format(d, "yyyy-MM-dd");
						}
					}
					
					if (!fDate.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + ">=" + StrUtil.sqlstr(fDate);
						else
							cond += " and " + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
					}
					if (!tDate.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + "<" + StrUtil.sqlstr(tDate);
						else
							cond += " and " + ff.getName() + "<" + StrUtil.sqlstr(tDate);
					}
				}
				else {
					// 时间点
					String d = ParamUtil.get(request, ff.getName());
					if (!d.equals("")) {
						cond = SQLFilter.concat(cond, "and", ff.getName() + "=" + StrUtil.sqlstr(d));
					}
				}
			}
			else if (ctlType.equals(FormField.TYPE_SELECT)) {
				String[] ary = ParamUtil.getParameters(request, ff.getName());
				if (ary!=null) {
					int len = ary.length;
					if (len==1) {
						if (!ary[0].equals("")) {
							if (cond.equals(""))
								cond += ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
							else
								cond += " and " + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
						}
					}
					else {
						String orStr = "";
						for (int n=0; n<len; n++) {
							if (!ary[n].equals(""))
								orStr = SQLFilter.concat(orStr, "or", ff.getName() + "=" + StrUtil.sqlstr(ary[n]));
						}
						if (!orStr.equals("")) {					
							cond = SQLFilter.concat(cond, "and", "(" + orStr + ")");
						}
					}
				}
			}
			else {
				// out.print(ff.getTitle() + "--"  + ff.getName() + "--" + name_cond + "<BR>");
				if (name_cond.equals("0")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
						else
							cond += " and " + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
					}
				}
				else if (name_cond.equals("1")) {
					if (!value.equals("")) {
						if (cond.equals(""))
							cond += ff.getName() + "=" + StrUtil.sqlstr(value);
						else
							cond += " and " + ff.getName() + "=" + StrUtil.sqlstr(value);
					}
				}
			}
		}
		if (!cond.equals("")){
			sql = sql + " and " + cond;
		}
		if(!dept_code.equals("") && !dept_code.equals(DeptDb.ROOTCODE)){
			sql += " and dept_code in ("+ deptstrs +")";
		}
	}else{
		if(action.equals("manage")){
			sql += " and unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request));
			if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && !privilege.isUserPrivValid(request, "sales.manager")){
				String salesPerson = getSalesPerson(privilege.getUser(request));
				if(!salesPerson.equals("")){
					sql += " and sales_person in ("+salesPerson+")";
				}
			}
			else if(!privilege.isUserPrivValid(request, "admin") && !privilege.isUserPrivValid(request, "sales") && privilege.isUserPrivValid(request, "sales.manager")){
				sql += " and dept_code in ("+dept_code+")";
			}
		}
		else {
			sql += " and sales_person=" + StrUtil.sqlstr(userName);			
		}
	}
	sql += " order by " + orderBy + " " + sort;
	
	query = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg.getKey(), sql);
}

// out.print("op=" + op + " " + sql);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>客户列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
	</style>
<script src="../inc/common.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery.js"></script>
<script src="../inc/flow_dispose_js.jsp"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="<%=request.getContextPath()%>/js/jquery.raty.min.js"></script>
<script>

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "customer_list.jsp?op=sort&action=<%=action%>&userName=<%=StrUtil.UrlEncode(userName)%>&orderBy=" + orderBy + "&sort=" + sort + "&customer=<%=StrUtil.UrlEncode(customer)%>&tel=<%=tel%>&enterType=<%=enterType%>&query=<%=query%>";
}

function openWin(url,width,height) {
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function doShare() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请选择客户！","提示");
		return;
	}

	openWin("customer_share_batch.jsp?ids=" + ids, 300, 50);
}

function selAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
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
</script>
<style>
.share{
	line-height: 30px;
	background-image: url(images/share.png);
	background-repeat: no-repeat;
	text-align: right;
	height: 30px;
	width: 90px;
	padding-left:0px !important;
	color:#4e96f0;
}
</style>
</head>
<%@ include file="../inc/nocache.jsp"%>
<body>
<%@ include file="customer_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<%!
// 如果map中存在，则不从request中获取
public String getUrlStr(HttpServletRequest request, Map map) {
    String queryStrTmp = "";
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
        String paramName = (String) paramNames.nextElement();
        if (!map.containsKey(paramName)) {
	        String[] paramValues = ParamUtil.getParameters(request, paramName);
	        if (paramValues!=null) {
	        	int len = paramValues.length;
	        	/*
	        	if (len==1) {
		            String paramValue = ParamUtil.get(request, paramName);
			        if (queryStrTmp.equals("")) {
		        		queryStrTmp = paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}
		        	else {
		        	    queryStrTmp += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}	        		
	        	}
	        	*/
	        	for (int i=0; i<len; i++) {
		            String paramValue = paramValues[i];
			        if (queryStrTmp.equals("")) {
		        		queryStrTmp = paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}
		        	else {
		        	    queryStrTmp += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}	        	
	        	}
	        }
        }
    }	

    return queryStrTmp;
}
%>
<%
    // 将request中其它参数也传至url中
    String queryStrTmp = "";
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
        String paramName = (String) paramNames.nextElement();
        String[] paramValues = request.getParameterValues(paramName);
        if (paramValues.length == 1) {
            String paramValue = paramValues[0];
            // 过滤掉formCode
            if (paramName.equals("op") || paramName.equals("action") || paramName.equals("orderBy") || paramName.equals("sort") || paramName.equals("query"))
                ;
            else
                queryStrTmp += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
        }
    }
        
	// querystr = "op="+op + "&action=" + StrUtil.UrlEncode(action) + "&orderBy=" + orderBy + "&sort=" + sort + "&query=" + StrUtil.UrlEncode(query) + queryStrTmp;
	// querystr = "op=" + op + "&action=" + action + "&userName=" + StrUtil.UrlEncode(userName) + "&customer=" + StrUtil.UrlEncode(customer) + "&tel=" + tel + "&enterType=" + enterType + "&customerType=" + customerType + "&orderBy=" + orderBy + "&sort=" + sort;
	
	// System.out.print("op=" + op + " sql=" + sql);

	int pagesize = ParamUtil.getInt(request, "pagesize", 20);
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();

	FormDAO fdao = new FormDAO();
			
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
%>
<div class="spacerH"></div>
<form id="formSearch" name="formSearch" class="search-form" action="customer_list.jsp" method="get">
  <table class="percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
    <tbody>
      <tr>
        <td align="center">
        <table width="98%" border="0">
  <tr>
    <td width="4%">类型：
        &nbsp;</td>
    <td width="44%"><%
      SelectMgr sm = new SelectMgr();
      SelectDb sd = sm.getSelect("sales_customer_category");
      Vector vsd = sd.getOptions();
      Iterator irsd = vsd.iterator();
      while (irsd.hasNext()) {
          SelectOptionDb sod = (SelectOptionDb)irsd.next();
          if (sod.isOpen()) {
          %>
      <input id="category" name="category" value="<%=sod.getValue()%>" type="checkbox" onclick="o('formSearch').submit()" />
      <%=sod.getName()%>
      <%
          }
      }
      %>
      <input name="category_cond" type="hidden" value="1" /></td>
    <td width="4%">意向：</td>
    <td width="43%"><input name="customer_type_cond" value="1" type="hidden" />
      <%
      sd = sm.getSelect("sales_customer_type");
      vsd = sd.getOptions();
      irsd = vsd.iterator();
      while (irsd.hasNext()) {
          SelectOptionDb sod = (SelectOptionDb)irsd.next();
          if (sod.isOpen()) {
          %>
      <input id="customer_type" name="customer_type" value="<%=sod.getValue()%>" type="checkbox" onclick="o('formSearch').submit()" />
      <%=sod.getName()%>
      <%
          }
      }
      %>
      &nbsp;&nbsp;&nbsp;</td>
  </tr>
  <tr>
    <td>        星级：</td>
    <td><select id="star" name="star">
      <option value="">不限</option>
      <option value="0">无</option>
      <option value="1">一星</option>
      <option value="2">二星</option>
      <option value="3">三星</option>
      <option value="4">四星</option>
      <option value="5">五星</option>
    </select>
      <input name="star_cond" type="hidden" value="1" />
      &nbsp;
      <input name="enterType_cond" value="1" type="hidden" />
企业性质：
<select id="enterType" name="enterType">
  <option value="">不限</option>
  <%
			sd = sm.getSelect("qyxz");
			vsd = sd.getOptions();
			irsd = vsd.iterator();
			while (irsd.hasNext()) {
				SelectOptionDb sod = (SelectOptionDb)irsd.next();
				if (!sod.isOpen())
					continue;
				String clr = "";
				if (!sod.getColor().equals(""))
					clr = " style='color:" + sod.getColor() + "' ";	
				%>
  <option value="<%=sod.getValue()%>" <%=clr%>><%=sod.getName()%></option>
  <%
			}
		  %>
</select></td>
    <td>方式：      </td>
    <td><input name="sellMode_cond" value="1" type="hidden" />
      <%
      sd = sm.getSelect("xsfs");
      vsd = sd.getOptions();
      irsd = vsd.iterator();
      while (irsd.hasNext()) {
          SelectOptionDb sod = (SelectOptionDb)irsd.next();
          if (sod.isOpen()) { 
          %>
      <input id="sellMode" name="sellMode" value="<%=sod.getValue()%>" type="checkbox" onclick="o('formSearch').submit()" />
      <%=sod.getName()%>
      <%
          }
      }
      %></td>
  </tr>
  <tr>
    <td>客户：</td>
    <td><input type="text" name="customer" size="10" value="<%=customer%>" />
      <input name="customer_cond" value="0" type="hidden" />
      <%if (privilege.isUserPrivValid(request, "sales")) {%>
&nbsp;&nbsp;部门：
<select id="dept_code" name="dept_code" style="height:24px">
  <%
DeptDb lf = new DeptDb(DeptDb.ROOTCODE);
DeptView dv = new DeptView(lf);
dv.ShowDeptAsOptions(out, lf, lf.getLayer()); 
%>
</select>
&nbsp;&nbsp;
<script>
        $("#dept_code").find("option[value='<%=dept_code%>']").attr("selected", true);
		</script>
<%} %>
<%
        if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
        	//String deptCodes = getManageDepts(userName);
        	//String deptArr[] = deptCodes.split(",");
        	DeptDb ddb = new DeptDb();
        	UserDb udb = new UserDb();
			udb = udb.getUserDb(userName);
			String[] deptArr = udb.getAdminDepts();
        	if(deptArr.length>1){
        %>
所属部门：
<select id="dept_code" name="dept_code" style="height:24px">
  <option value="" selected="selected">不限</option>
  <%
        	for(int t = 0 ;t<deptArr.length;t++){
        		ddb = ddb.getDeptDb(deptArr[t]);
        %>
  <option value="<%=deptArr[t] %>"><%=ddb.getName() %></option>
  <%} %>
</select>
&nbsp;&nbsp;
<script>
		o("dept_code").value = "<%=dept_code %>";
		</script>
<%}} %></td>
    <td title="发现时间">时间：</td>
    <td><input name="find_date_cond" value="0" type="hidden" />
从
  <input type="text" id="find_dateFromDate" size="10" name="find_dateFromDate" value="<%=find_dateFromDate%>" />
至
<input type="text" id="find_dateToDate" name="find_dateToDate" size="10" value="<%=find_dateToDate%>" />
<script>
o("enterType").value = "<%=enterType%>";
o("star").value = "<%=star%>";

<%
String[] categories = ParamUtil.getParameters(request, "category");
if (categories!=null) {
	for (int i=0; i<categories.length; i++) {
		%>
		setCheckboxChecked("category", '<%=categories[i]%>');
		<%
	}
}

String[] customer_types = ParamUtil.getParameters(request, "customer_type");
if (customer_types!=null) {
	for (int i=0; i<customer_types.length; i++) {
		%>
		setCheckboxChecked("customer_type", '<%=customer_types[i]%>');
		<%
	}
}	

String[] sellModes = ParamUtil.getParameters(request, "sellMode");
if (sellModes!=null) {
	for (int i=0; i<sellModes.length; i++) {
		%>
		setCheckboxChecked("sellMode", '<%=sellModes[i]%>');
		<%
	}
}	
%>	
</script>
<input class="btn" type="submit" value="查  询" />
<input type="hidden" name="action" value="<%=action%>" />
<input type="hidden" name="userName" value="<%=userName%>" />
<input type="hidden" name="op" value="search" />
<input type="hidden" name="pagesize" value="<%=pagesize%>" />
<input type="hidden" name="orderBy" value="<%=orderBy%>" />
<input type="hidden" name="sort" value="<%=sort%>" /></td>
  </tr>
</table></td>
      </tr>
    </tbody>
  </table>
</form>
<table width="98%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr>
    <th width="20" style="cursor:pointer"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else clearAllCheckBox('ids')" /></th>
    <th width="200" style="cursor:pointer" abbr="customer">客户名称</th>
    <th width="68" style="cursor:pointer" abbr="category">类型</th>
    <th width="68" style="cursor:pointer" abbr="customer_type">意向</th>
    <th width="100" style="cursor:pointer" abbr="star">星级</th>
    <th width="68" style="cursor:pointer" abbr="enterType">性质</th>
    <th width="68" style="cursor:pointer" abbr="sales_person">销售员</th>
	<th width="80" style="cursor:pointer" abbr="find_date">发现时间</th>
	<th width="72" style="cursor:pointer" abbr="dept_code">所属部门</th>
    <th width="80" style="cursor:pointer" abbr="last_visit_date">最后联系</th>
    <th width="380">联系结果</th>
    <th width="190">操作</th>
  </tr>
  </thead>
  <%
	  	int i = 0;
		SelectOptionDb sod = new SelectOptionDb();
		
		String code = "khlb";
		String visitFormCode = "day_lxr";

		UserMgr um = new UserMgr();
		ArrayList<String> list = new ArrayList<String>();
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			long id = fdao.getId();
			String realName = "";
			String founderRealName = "";
			String saler = StrUtil.getNullStr(fdao.getFieldValue("sales_person"));
			if (!saler.equals("")) {
				UserDb user = um.getUserDb(saler);
				realName = user.getRealName();
			}
			String starLevel = RatyCtl.render(request, fdao.getFormField("star"), true);
			list.add(starLevel);
			/*
			String founder = StrUtil.getNullStr(fdao.getFieldValue("founder"));
			if (!founder.equals("")) {
				UserDb user = um.getUserDb(founder);
				if (user.isLoaded())
					founderRealName = user.getRealName();
			}
			*/
		%>
  <tr >
    <td><input type="checkbox" name="ids" value="<%=id%>" /></td>
    <td align="left"><a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_show.jsp?id=<%=id%>&amp;action=<%=action%>&formCode=<%=formCode%>')"><%=fdao.getFieldValue("customer")%></a></td>
    <td align="center"><%=sod.getOptionName("sales_customer_category", fdao.getFieldValue("category"))%></td>
    <td align="center"><%=sod.getOptionName("sales_customer_type", fdao.getFieldValue("customer_type"))%></td>
    <td align="center"><%=starLevel %></td>
    <td align="center"><%=sod.getOptionName("qyxz", fdao.getFieldValue("enterType"))%></td>
    <td align="center"><%=realName%></td>
    <td align="center"><%=fdao.getFieldValue("find_date")%></td>
    <%
	i++;
	String sql2 = "select d.id from form_table_" + visitFormCode + " d, form_table_sales_linkman l where d.lxr=l.id and l.customer=" + id + " order by visit_date desc"; //  + " and d.is_visited='是'";
	Iterator ir2 = fdao.listResult(visitFormCode, sql2, 1, 1).getResult().iterator();
	String result = "", visitDate="";
	if (ir2.hasNext()) {
		FormDAO fdao2 = (FormDAO)ir2.next();
		result = fdao2.getFieldValue("contact_result");
		visitDate = fdao2.getFieldValue("visit_date");
	}
	%>
	<%
    	DeptDb dd = new DeptDb(fdao.getFieldValue("dept_code"));
     %>
	<td><%=dd.getName()%></td>
    <td align="center"><%=DateUtil.format(DateUtil.parse(fdao.getFieldValue("last_visit_date"), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd")%></td>
    <td title="<%=result%>"><%=result%></td>
    <td >
      <a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_visit_list.jsp?customerId=<%=id%>')">行动</a>&nbsp;
      <a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_edit.jsp?id=<%=id%>&amp;action=<%=action%>&formCode=<%=StrUtil.UrlEncode(formCode)%>')">编辑</a>&nbsp;
<%
if (!action.equals("manage")) {
	if (privilege.isUserPrivValid(request, "sales")) {
		%>
          <a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='../visual_del.jsp?id=<%=id%>&amp;action=<%=action%>&formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}})" style='cursor:pointer'>删除</a>&nbsp;
		<%
	}
}
%>
      <a href="javascript:;" onclick="window.open('customer_share_list.jsp?id=<%=id%>','','height=480, width=640, menubar=no, scrollbars=yes,resizable=auto,location=no,status=no')">共享</a>&nbsp;
      <a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/linkman_list.jsp?op=listOfCustomer&customerId=<%=id%>')">联系人</a>
    </td>
  </tr>
  <%
		}
%>
</table>
</body>
<script>
		$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
			{name: '共享用户', bclass: 'share', onpress : actions}],
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
                
		<%for (int j = 0; j < list.size(); j++) {
			int index1 = list.get(j).indexOf("star_raty");
			String rand="",scpt="";
			if(index1 > 0){
				rand = list.get(j).substring(index1, index1 + 14);
				index1 = list.get(j).indexOf("<script>");
				int index2 = list.get(j).indexOf("</script>");
				if (index2 > 0)
					scpt = list.get(j).substring(index1 + 8, index2);
			}
		%>
		$('#<%=rand%>').html('');
		<%=scpt%>
		<%}%>
		
		
		$('#find_dateFromDate').datetimepicker({
                    lang:'ch',
                    timepicker:false,
                    format:'Y-m-d',
                    formatDate:'Y/m/d'
          });
        $('#find_dateToDate').datetimepicker({
                    lang:'ch',
                    timepicker:false,
                    format:'Y-m-d',
                    formatDate:'Y/m/d'
          });
});

function changeSort(sortname, sortorder) {
<%
Map map = new HashMap();
map.put("pageSize", "");
map.put("orderBy", "");
map.put("sort", "");
querystr = getUrlStr(request, map);
%>
	window.location.href = "customer_list.jsp?pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&<%=querystr%>";
}

function changePage(newp) {
<%
map.clear();
map.put("CPages", "");
map.put("pageSize", "");
querystr = getUrlStr(request, map);
%>
	if (newp){
		window.location.href = "customer_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp + "&<%=querystr%>";
	}
}

function rpChange(pageSize) {
<%
map.clear();
map.put("CPages", "");
map.put("pageSize", "");
querystr = getUrlStr(request, map);
%>
	window.location.href = "customer_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize + "&<%=querystr%>";
}

function onReload() {
	window.location.reload();
}
function actions(com, grid) {
  if (com=='共享用户') {
  	doShare();
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
			for(String dept : depts){
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

public String getDeptCode(String uName){
	String sql = "select dept_code from dept_user where user_name=?";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = null;
	ResultRecord rd = null;
	String deptCode = "";
	String deptCodes = "";
	try{
		ri = jt.executeQuery(sql,new Object[]{uName});
		if(ri.hasNext()){
			rd = (ResultRecord)ri.next();
			deptCodes = rd.getString(1);
			String codeAry[] = deptCodes.split(",");
			if(codeAry.length>0){
				deptCode = codeAry[0];
			}
		}
	}catch(Exception e){
		e.printStackTrace();
	}
	return deptCode;
}

public String getManageDepts(String uName){
	String sql = "select dept_code from dept_user where user_name=?";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = null;
	ResultRecord rd = null;
	String deptCode = "";
	try{
		ri = jt.executeQuery(sql,new Object[]{uName});
		if(ri.hasNext()){
			rd = (ResultRecord)ri.next();
			deptCode = rd.getString(1);
		}
	}catch(Exception e){
		e.printStackTrace();
	}
	return deptCode;
}
%>